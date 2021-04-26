/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/
package org.botlibre.thought.forgetfulness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.BasicVertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.thought.BasicThought;
import org.botlibre.util.Utils;
import org.eclipse.persistence.internal.helper.IdentityHashSet;

/**
 * Forgetfulness is a sub-conscious thought that cleans up the memory to remove unused vertices and reduce relationship size.
 */
public class Forgetfulness extends BasicThought {
	public static int PAGE = 5000;
	
	/** Min number of days to keep conversation and context data for. */
	public static long EXPIRY = 7;
	public static int MAX_SIZE = 100000;
	public static int MAX_RELATIONSHIPS = 150;
	public static long TIME_TO_LIVE = (10L * Utils.MINUTE);
	
	/** Min number of days to keep conversation and context data for. */
	public long expiry = EXPIRY;
	public int maxSize = MAX_SIZE;
	public int maxRelationships = MAX_RELATIONSHIPS;
	
	public enum ForgetType { Unreferenced, UnreferencedData, OldConversations, LeastReferenced, UnreferencedPinned, Grammar, FixResponses, FixRelationships }
	
	public Forgetfulness() { }
	
	/**
	 * Analyse the active memory.
	 * Output the active article to the senses.
	 */
	@Override
	public void think() {
		try {
			if (!this.bot.mind().isConscious()) {
				return;
			}
			if (this.isStopped) {
				return;
			}
			if (this.isEnabled) {
				// Only count 1 in 20, to help concurrency.
				if (Utils.random().nextInt(20) >= 19) {
					Network memory = this.bot.memory().newMemory();
					Vertex forgetfulness = memory.createVertex(getPrimitive());
					Vertex activeCount = forgetfulness.getRelationship(Primitive.COUNT);
					int accessCount = 0;
					if (activeCount == null) {
						forgetfulness.addRelationship(Primitive.COUNT, memory.createVertex(accessCount));
					} else {
						accessCount = ((Number)activeCount.getData()).intValue();
					}
					accessCount = accessCount + 20;
					memory.save();
					log("accessCount", Level.FINE, accessCount);
					if (accessCount < (this.maxRelationships * 2)) {
						forgetfulness.setRelationship(Primitive.COUNT, memory.createVertex(accessCount));
						memory.save();
						return;
					} else {
						forgetfulness.setRelationship(Primitive.COUNT, memory.createVertex(0));						
					}
					memory.save();
					forget(memory, false);
				}
			}
		} catch (Exception failure) {
			log(failure);
		}
	}
	
	public int forget(ForgetType type, int numberToDelete, Network memory) throws Exception {
		return forget(type, numberToDelete, TIME_TO_LIVE, memory);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int forget(ForgetType type, int numberToDelete, long timeToLive, Network memory) throws Exception {
		if (!this.bot.mind().isConscious()) {
			return 0;
		}
		int errors = 0;
		boolean found = true;
		boolean first = true;
		long start = System.currentTimeMillis();
		while (found && (numberToDelete > 0) && this.bot.mind().isConscious()) {
			if (this.isStopped) {
				break;
			}
			long batchStart = System.currentTimeMillis();
			List<Vertex> unreferenced = new ArrayList<Vertex>();
			if (type == ForgetType.Unreferenced) {
				log("Searching for unreferenced vertices with no data", Level.INFO);
				unreferenced = memory.findByNativeQuery(
						"SELECT v.* FROM VERTEX v LEFT OUTER JOIN RELATIONSHIP r ON (((r.TARGET_ID = v.ID) OR (r.TYPE_ID = v.ID)) OR (r.META_ID = v.ID)) WHERE r.ID IS NULL and ((v.PINNED = false) AND (v.DATAVALUE IS NULL)) LIMIT " + PAGE,
						BasicVertex.class, PAGE);
				//int rowCount = memory.executeNativeQuery(
				//		"DELETE FROM VERTEX v WHERE v.ID IN (" + 
				//		"SELECT v.ID FROM VERTEX v LEFT OUTER JOIN RELATIONSHIP r ON (((r.TARGET_ID = v.ID) OR (r.TYPE_ID = v.ID)) OR (r.META_ID = v.ID)) WHERE r.ID IS NULL and ((v.PINNED = false) AND (v.DATAVALUE IS NULL)))");
				log("Unreferenced verticies with no data query time", Level.INFO, System.currentTimeMillis() - batchStart);
				log("Removing unreferenced vertices with no data", Level.WARNING, unreferenced.size());
			} else if (type == ForgetType.UnreferencedData) {
				log("Searching for unreferenced vertices", Level.INFO);
				unreferenced = memory.findByNativeQuery(
						"SELECT v.* FROM VERTEX v LEFT OUTER JOIN RELATIONSHIP r ON (((r.TARGET_ID = v.ID) OR (r.TYPE_ID = v.ID)) OR (r.META_ID = v.ID)) WHERE r.ID IS NULL and ((v.PINNED = false) AND (v.DATATYPE <> 'Primitive')) LIMIT " + PAGE,
						BasicVertex.class, PAGE);
				//int rowCount = memory.executeNativeQuery(
				//		"DELETE FROM VERTEX v WHERE v.ID IN (" + 
				//		"SELECT v.ID FROM VERTEX v LEFT OUTER JOIN RELATIONSHIP r ON (((r.TARGET_ID = v.ID) OR (r.TYPE_ID = v.ID)) OR (r.META_ID = v.ID)) WHERE r.ID IS NULL and ((v.PINNED = false) AND (v.DATATYPE <> 'Primitive')) LIMIT " + numberToDelete + ")");
				log("Unreferenced vertices query time", Level.INFO, System.currentTimeMillis() - batchStart);
				log("Removing unreferenced vertices", Level.WARNING, unreferenced.size());
			} else if (type == ForgetType.OldConversations) {
				log("Searching for old conversation vertices", Level.INFO);
				Vertex instantiation = memory.createVertex(Primitive.INSTANTIATION).detach();
				Vertex context = memory.createVertex(Primitive.CONTEXT).detach();
				Vertex conversation = memory.createVertex(Primitive.CONVERSATION).detach();
				Vertex input = memory.createVertex(Primitive.INPUT).detach();
				java.sql.Date date = new java.sql.Date(System.currentTimeMillis() - (Utils.DAY * this.expiry));
				Map parameters = new HashMap();
				parameters.put("type", instantiation);
				parameters.put("context", context);
				parameters.put("conversation", conversation);
				parameters.put("input", input);
				parameters.put("date", date);
				unreferenced = memory.findAllQuery(
						"Select v FROM Vertex v join v.allRelationships r where r.type = :type and v.creationDate < :date and (r.target = :context or r.target = :conversation or r.target = :input) "
							+ "order by v.creationDate", parameters, PAGE, 0);
				log("Old conversation query time", Level.INFO, System.currentTimeMillis() - batchStart);
				log("Removing old conversation vertices", Level.WARNING, unreferenced.size());
				// Check if still too many conversations and delete more.
				if (unreferenced.isEmpty()) {
					parameters = new HashMap();
					parameters.put("type", instantiation);
					parameters.put("context", context);
					parameters.put("conversation", conversation);
					parameters.put("input", input);
					unreferenced = memory.findAllQuery(
							"Select v FROM Vertex v join v.allRelationships r where r.type = :type and (r.target = :context or r.target = :conversation or r.target = :input) "
								+ "order by v.creationDate", parameters, PAGE, 0);
					log("Old conversation query time", Level.INFO, System.currentTimeMillis() - batchStart);
					if (unreferenced.size() < PAGE) {
						unreferenced = new ArrayList<Vertex>();
					} else {
						// Keep a minimum of a 1/2 page.
						for (int index = 0; index < (PAGE / 2); index++) {
							unreferenced.remove(unreferenced.size() - 1);
						}
					}
					log("Removing old conversation vertices", Level.WARNING, unreferenced.size());
				}
			} else if (type == ForgetType.LeastReferenced) {
				log("Searching for vertices with fewest references", Level.INFO, numberToDelete);
				//List<Object[]> byReferences  = memory.findAllQuery("Select count(v2) c, v from Vertex v, Vertex v2 join v2.allRelationships r2 "
				//		+ "where v.pinned = false and (v.dataType is null or v.dataType <> 'Primitive') and (r2.target = v or r2.type = v or r2.meta = v) "
				//		+ "group by v order by c, v.accessCount, v.accessDate", 5000);
				unreferenced = memory.findByNativeQuery(
						"SELECT v.* FROM VERTEX v JOIN RELATIONSHIP r ON (((r.TARGET_ID = v.ID) OR (r.TYPE_ID = v.ID)) OR (r.META_ID = v.ID)) "
								+ "WHERE ((v.PINNED = false) AND ((v.DATATYPE IS NULL) OR ((v.DATATYPE <> 'Primitive') AND (v.DATATYPE <> 'Meta')))) "
								+ "GROUP BY v.ID ORDER BY COUNT(r.SOURCE_ID), v.ACCESSCOUNT, v.ACCESSDATE LIMIT " + PAGE, BasicVertex.class, PAGE);
				log("Fewest references query time", Level.INFO, System.currentTimeMillis() - batchStart);
				log("Found vertices with fewest references", Level.INFO, unreferenced.size());
			} else if (type == ForgetType.UnreferencedPinned) {
				log("Searching for unreferenced pinned vertices", Level.INFO);
				unreferenced = memory.findByNativeQuery(
						"SELECT v.* FROM VERTEX v LEFT OUTER JOIN RELATIONSHIP r ON (((r.TARGET_ID = v.ID) OR (r.TYPE_ID = v.ID)) OR (r.META_ID = v.ID)) WHERE r.ID IS NULL and ((v.PINNED = true) AND (v.DATAVALUE IS NULL OR v.DATATYPE <> 'Primitive')) LIMIT " + PAGE,
						BasicVertex.class, PAGE);
				log("Unreferenced pinned verticies query time", Level.INFO, System.currentTimeMillis() - batchStart);
				log("Removing unreferenced pinned vertices", Level.WARNING, unreferenced.size());
			} else if (type == ForgetType.Grammar) {
				log("Searching for grammar data", Level.INFO);
				Vertex next = memory.createVertex(Primitive.NEXT);
				Vertex previous = memory.createVertex(Primitive.PREVIOUS);
				Vertex sentence = memory.createVertex(Primitive.SENTENCE);
				Vertex instantiation = memory.createVertex(Primitive.INSTANTIATION);
				Vertex word = memory.createVertex(Primitive.WORD);
				unreferenced = memory.findByNativeQuery(
						"SELECT DISTINCT v.* FROM VERTEX v JOIN RELATIONSHIP r ON (r.SOURCE_ID = v.ID) JOIN RELATIONSHIP r2 ON (r2.SOURCE_ID = v.ID) WHERE (r.TYPE_ID = "
									+ next.getId() + " OR r.TYPE_ID = " + previous.getId() + " OR r.TYPE_ID = " + sentence.getId() + ") AND r2.TYPE_ID = "
									+ instantiation.getId() + " AND r2.TARGET_ID = " + word.getId() + " LIMIT " + PAGE,
						BasicVertex.class, PAGE);
				if (first) {
					unreferenced.add(memory.createVertex(Primitive.NULL));
				}
				log("Grammar verticies query time", Level.INFO, System.currentTimeMillis() - batchStart);
				log("Removing grammar data", Level.WARNING, unreferenced.size());
			} else if (type == ForgetType.FixResponses) {
				log("Searching for response question with no word references", Level.INFO);
				Vertex response = memory.createVertex(Primitive.RESPONSE);
				Vertex sentence = memory.createVertex(Primitive.SENTENCE);
				Vertex instantiation = memory.createVertex(Primitive.INSTANTIATION);
				unreferenced = memory.findByNativeQuery(
						"SELECT DISTINCT v.* FROM VERTEX v JOIN RELATIONSHIP r ON (r.SOURCE_ID = v.ID) JOIN RELATIONSHIP r2 ON (r2.SOURCE_ID = v.ID) WHERE (r.TYPE_ID = "
									+ response.getId() + " AND r2.TYPE_ID = "
									+ instantiation.getId() + " AND r2.TARGET_ID = " + sentence.getId() + ") LIMIT " + PAGE,
						BasicVertex.class, PAGE);
				log("Response verticies query time", Level.INFO, System.currentTimeMillis() - batchStart);
				log("Fixing response data", Level.WARNING, unreferenced.size());
			} else if (type == ForgetType.FixRelationships) {
				log("Searching all relationships", Level.INFO);
				unreferenced = memory.findByNativeQuery(
						"SELECT v.* FROM VERTEX v ORDER BY random() LIMIT " + PAGE,
						BasicVertex.class, PAGE);
				log("All relationships query time", Level.INFO, System.currentTimeMillis() - batchStart);
				log("Fixing relationships data", Level.WARNING, unreferenced.size());
			}
			found = unreferenced.size() > 0;
			int failures = 0;
			for (Vertex vertex : unreferenced) {
				if (vertex == null) {
					continue;
				}
				if (vertex.getCreationDate() != null && ((System.currentTimeMillis() - vertex.getCreationDate().getTime()) < timeToLive)) {
					log("Ignoring new vertex", Level.FINER, vertex, numberToDelete);
					numberToDelete--;
					failures++;
					continue;
				}
				log("Processing vertex", Level.FINER, vertex);
				try {
					if (this.isStopped) {
						return 0;
					}
					if (type == ForgetType.Grammar) {
						vertex.internalRemoveRelationships(Primitive.NEXT);
						vertex.internalRemoveRelationships(Primitive.PREVIOUS);
						vertex.internalRemoveRelationships(Primitive.SENTENCE);
					} else if (type == ForgetType.FixRelationships) {
						for (Iterator<Relationship> iterator = vertex.allRelationships(); iterator.hasNext(); ) {
							Relationship relationship = iterator.next();
							if (relationship.checkHashCode()) {
								log("Fixing relationship hashcode", Level.INFO, relationship);
							}
						}
					} else if (type == ForgetType.FixResponses) {
						if (!vertex.instanceOf(Primitive.PATTERN) && vertex.hasRelationship(Primitive.RESPONSE)) {
							// Associate each word in each question with the question.
							Collection<Relationship> words = vertex.getRelationships(Primitive.WORD);
							if (words != null) {
								boolean missing = false;
								for (Relationship word : words) {
									if (! word.getTarget().hasRelationship(Primitive.QUESTION, vertex)) {
										word.getTarget().addRelationship(Primitive.QUESTION, vertex);
										if (!missing) {
											log("Fixing response", Level.INFO, vertex);
										}
										missing = true;
									}
								}
							}
						}
					} else if (type == ForgetType.OldConversations) {
						log("Removing conversation", Level.FINE, vertex);
						memory.removeVertex(vertex);
						//memory.removeVertexAndReferences(vertex);
					} else {
						log("Removing vertex", Level.FINE, vertex);
						memory.removeVertex(vertex);
					}
				} catch (Exception failure) {
					errors++;
					failures++;
					if (errors > 5) {
						throw failure;
					}
					log(failure);
				}
				numberToDelete--;
				if (numberToDelete <= 0) {
					break;
				}
			}
			if (failures == unreferenced.size()) {
				found = false;
			}
			if (this.isStopped) {
				return 0;
			}
			try {
				memory.save();
				memory.clear();
			} catch (Exception failure) {
				errors++;
				if (errors > 5) {
					throw failure;
				}
				log(failure);
			}
			log("Processing batch time", Level.INFO, System.currentTimeMillis() - batchStart);
			first = false;
			if (type == ForgetType.FixRelationships || type == ForgetType.FixResponses) {
				break;
			}
		}
		log("Processing total time", Level.INFO, System.currentTimeMillis() - start);
		return numberToDelete;
	}

	public void forget(Network memory) throws Exception {
		forget(memory, true);
	}

	public void forget(Network memory, boolean force) throws Exception {
		forget(memory, force, TIME_TO_LIVE);
	}
	
	public void forget(Network memory, boolean force, long timeToLive) throws Exception {
		if (this.isStopped) {
			return;
		}
		forgetRelationships(memory);
		if (this.isStopped) {
			return;
		}
		int count = ((Number)memory.findAllQuery("Select count(v) from Vertex v").get(0)).intValue();
		// Only run if much bigger, otherwise let service handle it at night.
		int max = this.maxSize;
		if (!force) {
			max = (int)(max * 1.5);
		}
		log("Current number of vertices (current, max, threshold)", Level.INFO, count, this.maxSize, max);
		if (count > max) {
			int numberToDelete = count - this.maxSize + (this.maxSize / 20);
			log("Max number of vertices exceeded (max, current, deletions)", Level.WARNING, this.maxSize, count, numberToDelete);
			numberToDelete = forget(ForgetType.Unreferenced, numberToDelete, timeToLive, memory);
			if (numberToDelete == 0) {
				return;
			}
			numberToDelete = forget(ForgetType.UnreferencedData, numberToDelete, timeToLive, memory);
			if (numberToDelete == 0) {
				return;
			}
			numberToDelete = forget(ForgetType.OldConversations, numberToDelete, timeToLive, memory);
			if (numberToDelete == 0) {
				return;
			}
			// May have more unreferenced data.
			numberToDelete = forget(ForgetType.Unreferenced, numberToDelete, timeToLive, memory);
			if (numberToDelete == 0) {
				return;
			}
			numberToDelete = forget(ForgetType.UnreferencedData, numberToDelete, timeToLive, memory);
			if (numberToDelete == 0) {
				return;
			}
			// Record potential destructive forget.
			memory.createVertex(getPrimitive()).setRelationship(Primitive.LAST, memory.createTimestamp());
			memory.save();
			numberToDelete = forget(ForgetType.LeastReferenced, numberToDelete, timeToLive, memory);
			memory.clear();
			this.bot.memory().freeMemory();
		}
	}

	@SuppressWarnings("unchecked")
	public void forgetRelationships(Network memory) throws Exception {
		if (this.isStopped) {
			return;
		}
		long start = System.currentTimeMillis();
		List<Vertex> tooManyReferences = memory.findAllQuery("Select v from Vertex v where v.dirty = true");
		//List<Vertex> tooManyReferences = memory.findAllQuery("Select v from Relationship r join r.source v group by r.type, v having count(r) > " + MAX_RELATIONSHIPS);
		//List<Vertex> tooManyReferences = memory.findByNativeQuery(
		//		"SELECT t0.* FROM VERTEX t0, RELATIONSHIP t1 WHERE (t0.ID = t1.SOURCE_ID) AND (t1.PINNED = false) GROUP BY t0.ID, t1.TYPE_ID HAVING (COUNT(t1.TYPE_ID) > " + this.maxRelationships + ") LIMIT " + PAGE,
		//		BasicVertex.class, PAGE);
		log("Max relationships check query time", Level.FINE, System.currentTimeMillis() - start);
		if ((System.currentTimeMillis() - start) > 5000) {
			log("Max relationships check query time", Level.WARNING, System.currentTimeMillis() - start);
		}
		int errors = 0;
		if (tooManyReferences.size() > 0) {
			log("Veticies exceeding max number of relationships detected", Level.INFO, this.maxRelationships, tooManyReferences.size());
			for (Vertex vertex : tooManyReferences) {
				if (this.isStopped) {
					break;
				}
				log("Vertex has too many relationships", Level.FINER, vertex);
				// Check for corruption.
				if (vertex.getAllRelationships().size() != vertex.totalRelationships()) {
					log("Vertex has corrupt relationships", Level.FINER, vertex, vertex.getAllRelationships().size(), vertex.totalRelationships());
					Set<Relationship> valid = new IdentityHashSet();
					for (Iterator<Relationship> iterator = vertex.allRelationships(); iterator.hasNext(); ) {
						valid.add(iterator.next());
					}
					for (Relationship relationship : new ArrayList<Relationship>(vertex.getAllRelationships())) {
						if (!valid.contains(relationship)) {
							log("Removing corrupt relationship", Level.FINER, relationship);
							vertex.internalRemoveRelationship(relationship);
						}
					}
				}
				// Check hashcodes
				for (Relationship relationship : new ArrayList<Relationship>(vertex.getAllRelationships())) {
					if (relationship.checkHashCode()) {
						log("Fixing relationship hashcode", Level.FINER, relationship);
					}
				}
				// Remove all references.
				for (Entry<Vertex, Map<Relationship, Relationship>> entry : vertex.getRelationships().entrySet()) {
					log("Relationship size", Level.FINER, entry.getKey(), entry.getValue().size());
					if (entry.getValue().size() > this.maxRelationships) {
						int numberToDelete = entry.getValue().size() - this.maxRelationships;
						log("Removing vertex relationships exceeding max size for type", Level.FINER, vertex, entry.getKey(), numberToDelete);
						List<Relationship> sorted = new ArrayList<Relationship>(entry.getValue().values());
						Collections.sort(sorted, new Comparator<Relationship>() {
							public int compare(Relationship one, Relationship two) {
								if (one.isPinned() && !two.isPinned()) {
									return 1;
								}
								if (!one.isPinned() && two.isPinned()) {
									return -1;
								}
								if (one.getCorrectness() == two.getCorrectness()) {
									if (one.getAccessCount() == two.getAccessCount()) {
										if (one.getAccessDate() == null && two.getAccessDate() == null) {
											return 0;
										} else if (two.getAccessDate() == null) {
											return 1;
										} else if (one.getAccessDate() == null) {
											return -1;
										} else if (one.getAccessDate().getTime() > two.getAccessDate().getTime()) {
											return 1;
										} else if (one.getAccessDate().getTime() == two.getAccessDate().getTime()) {
											return 0;
										}
										return -1;
									} else {
										if (one.getAccessCount() > two.getAccessCount()) {
											return 1;
										}
										return -1;
									}
								} else {
									if (one.getCorrectness() > two.getCorrectness()) {
										return 1;
									}
									return -1;
								}
							}
						});
						for (int index = 0; index < sorted.size(); index++) {
							if (this.isStopped) {
								break;
							}
							if (numberToDelete <= 0) {
								break;
							}
							Relationship toBeDeleted = sorted.get(index);
							if (!toBeDeleted.isPinned()) {
								numberToDelete--;
								log("Removing relationship with least correctness", Level.FINER, toBeDeleted);
								toBeDeleted.getSource().internalRemoveRelationship(toBeDeleted);
							}
						}
						// Fix indexes.
						// TODO: Remove for now, seems to bottleneck db.
						//vertex.fixRelationships(entry.getKey());
					}
				}
				vertex.setIsDirty(false);
			}
			if (this.isStopped) {
				return;
			}
			try {
				memory.save();
				memory.clear();
			} catch (Exception failure) {
				errors++;
				if (errors > 5) {
					throw failure;
				}
				log(failure);
			}
			log("Max relationships total time", Level.INFO, System.currentTimeMillis() - start);
		}
	}
	
	/**
	 * Thoughts can be conscious or sub-conscious.
	 * A conscious thought is run by the mind single threaded with exclusive access to the short term memory.
	 * A sub-conscious thought is run concurrently, and must run in its own memory space.
	 */
	@Override
	public boolean isConscious() {
		return false;
	}

	/**
	 * Return if this thought must run even under stress.
	 */
	@Override
	public boolean isCritical() {
		return true;
	}

	public long getExpiry() {
		return expiry;
	}

	public void setExpiry(long expiry) {
		this.expiry = expiry;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public int getMaxRelationships() {
		return maxRelationships;
	}

	public void setMaxRelationships(int maxRelationships) {
		this.maxRelationships = maxRelationships;
	}
}

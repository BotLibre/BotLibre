/******************************************************************************
 *
 *  Copyright 2013-2019 Paphus Solutions Inc.
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
package org.botlibre.web.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.aiml.AIMLParser;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.api.thought.Thought;
import org.botlibre.knowledge.BasicVertex;
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfCompiler;
import org.botlibre.self.SelfDecompiler;
import org.botlibre.self.SelfParseException;
import org.botlibre.sense.http.Wiktionary;
import org.botlibre.sense.wikidata.Wikidata;
import org.botlibre.thought.forgetfulness.Forgetfulness;
import org.botlibre.thought.forgetfulness.Forgetfulness.ForgetType;
import org.botlibre.thought.language.Language;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.service.BotManager;
import org.botlibre.web.service.BotStats;

public class MemoryBean extends ServletBean {

	List<Vertex> results;
	List<Vertex> selection;
	boolean selectAll;
	boolean warning;
	
	String input = "*";
	String web = "Web";
	String webMode = "Data";
	String webXPath = "";
	String webInput = "";
	String type = "";
	String classification = "";
	String sort = "value";
	String order = "asc";
	boolean pinned = false;
	
	String query;
	@SuppressWarnings("rawtypes")
	Map parameters;
	String code = "";
	
	BrowseMode mode = BrowseMode.Search;
	public enum BrowseMode {Search, Reports, Selection, Worksheet, Graph}
	
	public MemoryBean() {
		this.pageSize = 500;
	}
	
	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public boolean isPinned() {
		return pinned;
	}

	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}

	public BrowseMode getMode() {
		return mode;
	}

	public void setMode(BrowseMode mode) {
		this.mode = mode;
	}

	public boolean isSelectAll() {
		return selectAll;
	}


	public void setSelectAll(boolean selectAll) {
		this.selectAll = selectAll;
	}


	public boolean isWarning() {
		return warning;
	}


	public void setWarning(boolean warning) {
		this.warning = warning;
	}

	public void runForgetfullness() {
		reset();
		this.mode = BrowseMode.Reports;
		try {
			getBot().mind().getThought(Forgetfulness.class).forget(getBot().memory().newMemory(), true);
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void deleteUnreferenced() {
		reset();
		this.mode = BrowseMode.Reports;
		try {
			getBot().mind().getThought(Forgetfulness.class).forget(ForgetType.Unreferenced, 20000, getBot().memory().newMemory());
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void deleteUnreferencedData() {
		reset();
		this.mode = BrowseMode.Reports;
		try {
			getBot().mind().getThought(Forgetfulness.class).forget(ForgetType.UnreferencedData, 20000, getBot().memory().newMemory());
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void deleteUnreferencedPinned() {
		reset();
		this.mode = BrowseMode.Reports;
		try {
			getBot().mind().getThought(Forgetfulness.class).forget(ForgetType.UnreferencedPinned, 20000, getBot().memory().newMemory());
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void deleteGrammar() {
		reset();
		this.mode = BrowseMode.Reports;
		try {
			getBot().mind().getThought(Forgetfulness.class).forget(ForgetType.Grammar, 20000, getBot().memory().newMemory());
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void fixResponses() {
		reset();
		this.mode = BrowseMode.Reports;
		try {
			getBot().mind().getThought(Forgetfulness.class).forget(ForgetType.FixResponses, 20000, getBot().memory().newMemory());
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void fixRelationships() {
		reset();
		this.mode = BrowseMode.Reports;
		try {
			getBot().mind().getThought(Forgetfulness.class).forget(ForgetType.FixRelationships, 20000, getBot().memory().newMemory());
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void deleteOldData() {
		reset();
		this.mode = BrowseMode.Reports;
		try {
			getBot().mind().getThought(Forgetfulness.class).forget(ForgetType.OldConversations, 10000, getBot().memory().newMemory());
		} catch (Exception exception) {
			error(exception);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void processBrowse(String input, String type, String classification, boolean pinned, String sort, String order) {
		reset();
		this.mode = BrowseMode.Search;
		this.input = input;
		this.type = type;
		this.classification = classification;
		this.pinned = pinned;
		this.sort = sort;
		this.order = order;
		if (input == null) {
			this.results = getBot().memory().newMemory().findAll();
			this.resultsSize = getBot().memory().newMemory().countAll();
			return;
		}
		this.input = input.trim();		
		if ((input.length() > 1) && (input.charAt(0) == ':')) {
			this.results = getBot().memory().newMemory().findAllQuery(input.substring(1));
			this.resultsSize = this.results.size();
			return;
		} else if ((input.length() > 1) && (input.charAt(0) == '=')) {
			Long id = null;
			try {
				id = Long.valueOf(input.substring(1));
			} catch (NumberFormatException exception) {}
			if (id != null) {
				Vertex result = getBot().memory().newMemory().findById(id);
				this.results = new ArrayList<Vertex>();
				if (result != null) {
					this.results.add(result);
					this.resultsSize = 1;
				} else {
					this.resultsSize = 0;
				}
				return;
			}
		}
		this.parameters = new HashMap();
		String where = "";
		String from = "";
		String orderBy = "";
		if ((input.length() > 0) && !input.equals("*")) {
			if (input.indexOf('*') == -1) {
				parameters.put("input", input);
				where = "v.dataValue = :input";
			} else {
				parameters.put("input", input.replace('*', '%'));
				where = "v.dataValue like :input";
			}
		} else {
			where = where + "v.id = v.id";
		}
		if (type != null && type.length() > 0) {
			parameters.put("type", type);
			where = where + " and v.dataType = :type";			
		}
		if (classification != null && classification.length() > 0) {
			parameters.put("classification", classification);
			where = where + " and r.correctness > 0 and r.target.dataValue = :classification and r.type.dataValue = \"instantiation\"";
			from = "join v.allRelationships r ";
		}
		if (pinned) {
			where = where + " and v.pinned = true";			
		}
		if (sort != null && sort.length() > 0) {
			orderBy = " order by v." + sort + " " + order;
		}
		this.query = "Select v from Vertex v " + from + "where " + where + orderBy;
		String count = "Select count(v) from Vertex v " + from + "where " + where;
		this.results = getBot().memory().newMemory().findAllQuery(this.query, this.parameters, this.pageSize, this.page);
		this.resultsSize = ((Number)getBot().memory().newMemory().findAllQuery(count, this.parameters, this.pageSize, 0).get(0)).intValue();
		// Check for id reference.
		Long id = null;
		try {
			id = Long.valueOf(input);
		} catch (NumberFormatException exception) {}
		if (id != null) {
			Vertex result = getBot().memory().newMemory().findById(id);
			if (result != null) {
				this.results.add(result);
				this.resultsSize++;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void processBrowsePage(String page) {
		this.page = Integer.valueOf(page);
		this.results = null;
		this.selection = null;
		this.selectAll = false;
		this.warning = false;
		if ((this.query == null) || (this.query.length() == 0)) {
			this.results = new ArrayList<Vertex>();
			return;
		}
		this.results = getBot().memory().newMemory().findAllQuery(this.query, this.parameters, this.pageSize, this.page);
	}

	public String pagingString() {
		StringWriter writer = new StringWriter();
		if (this.page > 0) {
			writer.write("<a class=\"menu\" href=\"memory?page=" + (getPage() - 1)  + "\">Previous</a>\n");
		}
		if (this.resultsSize > (this.pageSize * (this.page + 1))) {
			if (this.page > 0) {
				writer.write(" | ");
			}
			writer.write(" <a class=\"menu\" href=\"memory?page=" + (getPage() + 1) + "\">Next</a>");
		}
		if (this.resultsSize > this.pageSize) {
			if (this.resultsSize > (20 * this.pageSize)) {
				int max =  this.resultsSize / (this.pageSize);
				if ((getPage() - 5) <= 5) {
					for (int index = 0; index < (getPage() + 5); index++) {
						writer.write(" | ");
						if (index == this.page) {
							writer.write("<a class=\"menu\" href=\"memory?page=" + index + "\"><b>" + (index + 1) + "</b></a>");
						} else {
							writer.write("<a class=\"menu\" href=\"memory?page=" + index + "\">" + (index + 1) + "</a>");
						}
					}					
				} else {
					for (int index = 0; index < 5; index++) {
						writer.write(" | ");
						if (index == this.page) {
							writer.write("<a class=\"menu\" href=\"memory?page=" + index + "\"><b>" + (index + 1) + "</b></a>");
						} else {
							writer.write("<a class=\"menu\" href=\"memory?page=" + index + "\">" + (index + 1) + "</a>");
						}
					}
					if ((this.page + 10) < max) {
						writer.write(" ... ");
						boolean first = true;
						for (int index = this.page - 5; index < (this.page + 5); index++) {
							if (!first) {
								writer.write(" | ");
							} else {
								first = false;
							}
							if (index == this.page) {
								writer.write("<a class=\"menu\" href=\"memory?page=" + index + "\"><b>" + (index + 1) + "</b></a>");
							} else {
								writer.write("<a class=\"menu\" href=\"memory?page=" + index + "\">" + (index + 1) + "</a>");
							}
						}
					}
				}
				writer.write(" ... ");
				boolean first = true;
				if ((this.page + 10) >= max) {
					for (int index = this.page - 5 ; index <= max; index++) {
						if (!first) {
							writer.write(" | ");
						} else {
							first = false;
						}
						if (index == this.page) {
							writer.write("<a class=\"menu\" href=\"memory?page=" + index + "\"><b>" + (index + 1) + "</b></a>");
						} else {
							writer.write("<a class=\"menu\" href=\"memory?page=" + index + "\">" + (index + 1) + "</a>");
						}
					}					
				} else {
					for (int index = max - 4; index <= max; index++) {
						if (!first) {
							writer.write(" | ");
						} else {
							first = false;
						}
						if (index == this.page) {
							writer.write("<a class=\"menu\" href=\"memory?page=" + index + "\"><b>" + (index + 1) + "</b></a>");
						} else {
							writer.write("<a class=\"menu\" href=\"memory?page=" + index + "\">" + (index + 1) + "</a>");
						}
					}
				}
			} else {
				for (int index = 0; (index * this.pageSize) < getResultsSize(); index++) {
					writer.write(" | ");
					if (index == this.page) {
						writer.write("<a class=\"menu\" href=\"memory?page=" + index + "\"><b>" + (index + 1) + "</b></a>");
					} else {
						writer.write("<a class=\"menu\" href=\"memory?page=" + index + "\">" + (index + 1) + "</a>");
					}
				}
			}
		}
		return writer.toString();
	}

	@SuppressWarnings("rawtypes")
	public int getRelationshipsSize() {
		List result = getBot().memory().newMemory().findByNativeQuery(
				"SELECT COUNT(*) FROM RELATIONSHIP",
				null, 100);
		return ((Number)result.get(0)).intValue();
	}

	@SuppressWarnings("rawtypes")
	public int getUnmigratedRelationshipsSize() {
		List result = getBot().memory().newMemory().findByNativeQuery(
				"SELECT COUNT(*) FROM RELATIONSHIP WHERE HASHCODE IS NULL",
				null, 100);
		return ((Number)result.get(0)).intValue();
	}

	public void migrate() {
		checkLogin();
		getBotBean().checkInstance();
		getBotBean().checkAdmin();
		BotInstance instance = getBotBean().getInstance();
		if (instance.isExternal() && instance.isArchived()) {
			throw new BotException("Bot is external or archived");
		}
		if (instance.isSchema()) {
			throw new BotException("Bot already using a schema");
		}
		try {
			Network memory = getBot().memory().newMemory();
			memory.executeNativeQuery("update relationship r set meta_id = null where r.meta_id is not null and r.meta_id not in (select v.id from vertex v)");			
			getBotBean().disconnectInstance();
					
			BotManager.manager().forceShutdown(instance.getDatabaseName());
			Utils.sleep(1000);
			Bot.forceShutdown(instance.getDatabaseName());
			
			Bot bot = Bot.createInstance();
			bot.memory().createMemoryFromTemplate(instance.getDatabaseName(), true, instance.getDatabaseName(), false);
			bot.memory().switchMemory(instance.getDatabaseName(), true);
			bot.shutdown();
			
			getBotBean().setInstance(AdminDatabase.instance().updateInstanceSchema(instance.getId(), true));

			BotManager.manager().forceShutdown(instance.getDatabaseName());
			Utils.sleep(1000);
			Bot.forceShutdown(instance.getDatabaseName());
			bot = Bot.createInstance();
			try {
				bot.memory().destroyMemory(instance.getDatabaseName(), false);
			} finally {
				bot.shutdown();
			}
			
			getBotBean().connect(ClientType.WEB);
		} catch (Exception failed) {
			error(failed);
		}		
	}

	@SuppressWarnings("unchecked")
	public void processUnreferenced() {
		reset();
		this.mode = BrowseMode.Reports;
		this.results = getBot().memory().newMemory().findByNativeQuery(
				"SELECT v.* FROM VERTEX v LEFT OUTER JOIN RELATIONSHIP r ON (((r.TARGET_ID = v.ID) OR (r.TYPE_ID = v.ID)) OR (r.META_ID = v.ID)) WHERE r.ID IS NULL and ((v.PINNED = false) AND (v.DATAVALUE IS NULL)) LIMIT 5000",
				BasicVertex.class, 1000);
		this.page = 0;
		this.resultsSize = this.results.size();
	}

	@SuppressWarnings("unchecked")
	public void processUnreferencedPinned() {
		reset();
		this.mode = BrowseMode.Reports;
		this.results = getBot().memory().newMemory().findByNativeQuery(
				"SELECT v.* FROM VERTEX v LEFT OUTER JOIN RELATIONSHIP r ON (((r.TARGET_ID = v.ID) OR (r.TYPE_ID = v.ID)) OR (r.META_ID = v.ID)) WHERE r.ID IS NULL and ((v.PINNED = true) AND (v.DATAVALUE IS NULL OR v.DATATYPE <> 'Primitive')) LIMIT 5000",
				BasicVertex.class, 1000);
		this.page = 0;
		this.resultsSize = this.results.size();
	}

	@SuppressWarnings("unchecked")
	public void processUnreferencedWithData() {
		reset();
		this.mode = BrowseMode.Reports;
		this.results = getBot().memory().newMemory().findByNativeQuery(
				"SELECT v.* FROM VERTEX v LEFT OUTER JOIN RELATIONSHIP r ON (((r.TARGET_ID = v.ID) OR (r.TYPE_ID = v.ID)) OR (r.META_ID = v.ID)) WHERE r.ID IS NULL and ((v.PINNED = false) AND (v.DATATYPE <> 'Primitive')) LIMIT 5000",
				BasicVertex.class, 1000);
		this.page = 0;
		this.resultsSize = this.results.size();
	}
	
	@SuppressWarnings("unchecked")
	public void processLeastReferenced() {
		reset();
		this.mode = BrowseMode.Reports;
		this.results = getBot().memory().newMemory().findAllQuery("Select count(v2) c, v.accessCount, v.accessDate, v from Vertex v, Vertex v2 join v2.allRelationships r2 "
				+ "where v.pinned = false and (v.dataType is null or (v.dataType <> 'Primitive' and v.dataType <> 'Meta')) and (r2.target = v or r2.type = v or r2.meta = v) "
				+ "group by v order by c, v.accessCount, v.accessDate", 1000);
		this.page = 0;
		this.resultsSize = this.results.size();
	}
	
	@SuppressWarnings("unchecked")
	public void processMostRelationships() {
		reset();
		this.mode = BrowseMode.Reports;
		this.results = getBot().memory().newMemory().findAllQuery("Select count(r2) c, v from Vertex v join v.allRelationships r2 "
				+ "group by v order by c desc", 1000);
		this.page = 0;
		this.resultsSize = this.results.size();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void processOldData() {
		reset();
		this.mode = BrowseMode.Reports;
		this.sort = "creationDate";
		
		Network memory = getBot().memory().newMemory();
		Vertex instantiation = memory.createVertex(Primitive.INSTANTIATION).detach();
		Vertex context = memory.createVertex(Primitive.CONTEXT).detach();
		Vertex conversation = memory.createVertex(Primitive.CONVERSATION).detach();
		Vertex input = memory.createVertex(Primitive.INPUT).detach();
		java.sql.Date date = new java.sql.Date(System.currentTimeMillis() - (Utils.DAY * 7));
		Map parameters = new HashMap();
		parameters.put("type", instantiation);
		parameters.put("context", context);
		parameters.put("conversation", conversation);
		parameters.put("input", input);
		parameters.put("date", date);
		this.results = memory.findAllQuery(
				"Select v FROM Vertex v join v.allRelationships r where r.type = :type and v.creationDate < :date and (r.target = :context or r.target = :conversation or r.target = :input) "
					+ "order by v.creationDate", parameters, 1000, 0);
		this.page = 0;
		this.resultsSize = this.results.size();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> lookupVertices(HttpServletRequest request, Network memory) {
		List<Object[]> vertices = new ArrayList<Object[]>();
		for (Object parameter : request.getParameterMap().entrySet()) {
			Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>)parameter;
			String key = entry.getKey();
			int index = key.indexOf('-');
			if (index != -1) {
				String type = key.substring(0, index);
				int index2 = key.indexOf("-", index + 1);
				String id2 = "";
				String id3 = "";
				if (index2 == -1) {
					index2 = key.length();
				} else {
					int index3 = key.indexOf("-", index2 + 1);
					if (index3 == -1) {
						index3 = key.length();
					} else {
						id3 = key.substring(index3 + 1, key.length());
					}
					id2 = key.substring(index2 + 1, index3);
				}
				String id = key.substring(index + 1, index2);
				if ("on".equals(entry.getValue()[0])) {
					if (type.equals("v")) {
						Object[] data = new Object[1];
						data[0] = (memory.findById(Long.valueOf(id)));
						if (data[0] != null) {
							vertices.add(data);
						}
					} else if (type.equals("r")) {
						Object[] data = new Object[3];
						data[0] = (memory.findById(Long.valueOf(id)));
						data[1] = (memory.findById(Long.valueOf(id2)));
						data[2] = (memory.findById(Long.valueOf(id3)));
						if ((data[0] != null) && (data[1] != null)) {
							vertices.add(data);
						}
					}
				}
			}
		}
		return vertices;
	}
	
	public List<Vertex> lookupVerticesList(HttpServletRequest request) {
		Network memory = getBot().memory().newMemory();
		List<Object[]> vertices = lookupVertices(request, memory);
		List<Vertex> result = new ArrayList<Vertex>();
		for (Object[] data : vertices) {
			if (data.length == 1) {
				result.add((Vertex)data[0]);
			} else {
				Vertex source = (Vertex)data[0];
				Relationship relationship = source.getRelationship((Vertex)data[1], (Vertex)data[2]);
				result.add(relationship.getTarget());
			}
		}
		return result;
	}
	
	public void processSelection(HttpServletRequest request) {
		this.selectAll = false;
		this.mode = BrowseMode.Selection;
		this.selection = lookupVerticesList(request);
	}

	@SuppressWarnings("unchecked")
	public void processReferences(HttpServletRequest request) {
		reset();
		this.mode = BrowseMode.Search;
		this.results = new ArrayList<Vertex>();
		for (Vertex vertex : lookupVerticesList(request)) {
			this.results.addAll(getBot().memory().newMemory().findAllQuery("Select v from Vertex v join v.allRelationships r where r.target.id = " + vertex.getId()));
		}
		this.page = 0;
		this.resultsSize = this.results.size();
	}

	public void processDelete(HttpServletRequest request) {
		this.selectAll = false;
		Network memory = getBot().memory().newMemory();
		for (Object[] data : lookupVertices(request, memory)) {
			if (data.length == 1) {
				Vertex vertex = (Vertex)data[0];
				if (vertex.isPrimitive()) {
					throw new BotException("Cannot delete primitives");
				}
				List<Relationship> relationships = memory.findAllRelationshipsTo(vertex);
				if (!this.warning && (relationships.size() > 100)) {
					this.warning = true;
					throw new BotException("WARNING: " + vertex.displayString() + " has " + relationships.size() + " references.");
				}				
				memory.removeVertexAndReferences(vertex);
				if (this.results != null) {
					if ((this.results.size() > 0) && (((Object)this.results.get(0)) instanceof Object[])) {
						for (int index = 0; index < this.results.size();  index++) {
							Object[] result = (Object[])(Object)this.results.get(index);
							if (Arrays.asList(result).contains(vertex)) {
								this.results.remove(index);
								break;
							}
						}
					} else {
						this.results.remove(vertex);
					}
				}
				if (this.selection != null) {
					this.selection.remove(vertex);
				}
			} else {
				Vertex source = (Vertex)data[0];
				Relationship relationship = source.getRelationship((Vertex)data[1], (Vertex)data[2]);
				if (relationship != null) {
					source.internalRemoveRelationship(relationship);
				} else {
					Collection<Relationship> relationships = new ArrayList<Relationship>(source.getRelationships((Vertex)data[1]));
					for (Relationship r : relationships) {
						if (r.getTarget().equals(data[2])) {
							source.internalRemoveRelationship(r);
						}						
					}
				}
				if (this.selection != null) {
					int index = this.selection.indexOf(source);
					if (index != -1) {
						this.selection.set(index, source);
					}
				}
			}
			memory.save();
		}
	}

	public void processPin(HttpServletRequest request) {
		this.selectAll = false;
		Network memory = getBot().memory().newMemory();
		Set<Vertex> pinned = new HashSet<Vertex>();
		for (Object[] data : lookupVertices(request, memory)) {
			if (data.length == 1) {
				Vertex vertex = (Vertex)data[0];
				vertex.setPinned(true);
				pinned.add(vertex);
			}
			memory.save();
		}
		if (this.selection != null) {
			for (Vertex vertex : this.selection) {
				if (pinned.contains(vertex)) {
					vertex.setPinned(true);				
				}
			}
		}
	}

	public void processUnpin(HttpServletRequest request) {
		this.selectAll = false;
		Network memory = getBot().memory().newMemory();
		Set<Vertex> unpinned = new HashSet<Vertex>();
		for (Object[] data : lookupVertices(request, memory)) {
			if (data.length == 1) {
				Vertex vertex = (Vertex)data[0];
				vertex.setPinned(false);
				unpinned.add(vertex);
			}
			memory.save();
		}
		if (this.selection != null) {
			for (Vertex vertex : this.selection) {
				if (unpinned.contains(vertex)) {
					vertex.setPinned(false);
				}
			}
		}
	}

	public void processDeleteAll() {
		AdminDatabase.instance().log(Level.FINE, "delete memory", getBotBean().getInstance());
		BotManager.manager().forceShutdown(getBotBean().getInstance().getDatabaseName());
		Utils.sleep(1000);
		Bot.forceShutdown(getBotBean().getInstance().getDatabaseName());
		
		getBot().memory().deleteMemory();
		new Bootstrap().bootstrapSystem(getBot(), false);
		getBot().mood().saveProperties();
		for (Thought thought : getBot().mind().getThoughts().values()) {
			thought.saveProperties();
		}
		for (Thought thought : getBot().mind().getThoughts().values()) {
			thought.saveProperties();
		}
		
		processClearCache();
		reset();
		this.loginBean.getBean(ChatLogBean.class).reset();
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void executeCode(String code) {
		if (code == null) {
			return;
		}
		this.code = code;
		if (code.isEmpty()) {
			return;
		}
		this.results = new ArrayList<Vertex>();
		Bot bot = getBot();
		Network memory = bot.memory().newMemory();
		bot.log(SelfCompiler.getCompiler(), "eval", Level.INFO, code);
		Vertex result = SelfCompiler.getCompiler().evaluateExpression(
				code, memory.createVertex(Primitive.SELF), memory.createVertex(Primitive.SELF), false, false, memory);
		memory.save();
		if (result.instanceOf(Primitive.ARRAY)) {
			List<Vertex> elements = result.orderedRelations(Primitive.ELEMENT);
			if (elements == null) {
				this.results.add(result);
			} else {
				this.results.addAll(elements);
			}
		} else {
			this.results.add(result);
		}
		this.resultsSize = this.results.size();
		bot.log(SelfCompiler.getCompiler(), "eval result", Level.INFO, result);
	
	}
	
	public void processClearCache() {
		AdminDatabase.instance().log(Level.FINE, "clear cache", getBotBean().getInstance());
		getBot().memory().freeMemory();
		BotManager.manager().forceShutdown(getBotBean().getInstance().getDatabaseName());
		Utils.sleep(1000);
		Bot.forceShutdown(getBotBean().getInstance().getDatabaseName());
	}
	
	public void export(HttpServletResponse response, String format) throws IOException {
		List<Vertex> objects = this.results;
		if (this.mode == BrowseMode.Selection) {
			objects = this.selection;
		}
		if (objects.isEmpty()) {
			throw new BotException("No objects to export");
		}
		response.setContentType("text/plain");
		PrintWriter writer = response.getWriter();
		boolean json = true;
		boolean cvs = (format != null) && format.equals("csv");
		String ext = ".json";
		if (cvs) {
			json = false;
			ext = ".csv";
		}

		response.setHeader("Content-disposition","attachment; filename=" + encodeURI(getBotBean().getInstanceName()) + ext);

		if (json) {
			if (objects.size() > 1) {
				writer.write("[");
			}
			int index = 0;
			for (Vertex vertex : objects) {
				boolean first = true;
				writer.write("{");
				if (vertex.hasData()) {
					writer.write(" #data : ");
					SelfDecompiler.getDecompiler().printData(vertex, writer);
					first = false;
				}
				for (Iterator<Relationship> iterator = vertex.orderedAllRelationships(); iterator.hasNext(); ) {
					Relationship relationship = iterator.next();
					if (relationship.isInverse()) {
						continue;
					}
					if (!relationship.getType().isPrimitive()) {
						continue;
					}
					String name = relationship.getType().getDataValue();
					if (first) {
						first = false;
					} else {
						writer.write(", ");
					}
					writer.write(name);
					writer.write(" : ");
					if (relationship.getTarget().hasData()) {
						SelfDecompiler.getDecompiler().printData(relationship.getTarget(), writer);
					} else {
						writer.write("Object(");
						writer.write(relationship.getTarget().getId().toString());
						writer.write(")");
					}
				}
				writer.write("}");
				index++;
				if (index < objects.size()) {
					writer.write(",\n");
				}
			}
			if (objects.size() > 1) {
				writer.write("]");
			}
		} else if (cvs) {
			Set<Vertex> relationships = new HashSet<Vertex>();
			//boolean data = false;
			for (Vertex vertex : objects) {
				//if (vertex.hasData()) {
				//	data = true;
				//}
				for (Vertex relationship : vertex.getRelationships().keySet()) {
					if (relationship.isPrimitive()) {
						relationships.add(relationship);
					}
				}
			}
			List<Vertex> columns = new ArrayList<Vertex>(relationships);
			boolean first = true;
			//if (data) {
			//	writer.write("\"data\"");
			//	first = false;
			//}
			for (Vertex column : columns) {
				if (first) {
					writer.write("\"");
					first = false;
				} else {
					writer.write(",\"");
				}
				writer.write(((Primitive)column.getData()).getIdentity());
				writer.write("\"");
			}
			writer.write("\n");
			
			for (Vertex vertex : objects) {
				first = true;
				/*if (data) {
					if (vertex.hasData()) {
						writer.write("\"");
						SelfDecompiler.getDecompiler().printData(vertex, writer);
						writer.write("\"");
						first = false;
					} else {
						writer.write("\"\"");
					}
				}*/
				for (Vertex column : columns) {
					if (first) {
						writer.write("\"");
						first = false;
					} else {
						writer.write(",\"");
					}
					Vertex value = vertex.mostConscious(column);
					if (value != null) {
						writer.write(value.printString().replace("\n", " "));
					}
					writer.write("\"");
				}
				writer.write("\n");
			}
		}
		writer.flush();
	}

	public void checkMemory() {
		if ((getBotBean().getInstance().getMemoryLimit() > 0) && (getBot().memory().getLongTermMemory().size() > getBotBean().getInstance().getMemoryLimit() * 1.2)) {
			throw new BotException("Memory size exceeded, importing has been disable until nightly forgetfullness task runs");
		}
	}

	public void importFile(String fileName, InputStream inputStream, String format, String encoding, boolean pin) {
		String text = Utils.loadTextFile(inputStream, encoding, Site.MAX_UPLOAD_SIZE);
		importData(fileName, text, format, pin);
	}
	
	public void importData(String fileName, String inputData, String format, boolean pin) {
		checkMemory();
		BotStats stats = BotStats.getStats(getBotBean().getInstanceId(), getBotBean().getInstanceName());
		stats.imports++;
		Network memory = getBot().memory().newMemory();
		Network readOnlyMemory = getBot().memory().getLongTermMemory();
		Vertex result = null;
		List<Vertex> columns = new ArrayList<Vertex>();
		if (format != null) {
			format = format.toLowerCase();
		}
		if ("csv".equals(format)) {
			this.mode = BrowseMode.Search;
			this.results = new ArrayList<Vertex>();
			TextStream stream = new TextStream(inputData);
			boolean first = true;
			while (!stream.atEnd()) {
				String line = stream.nextLine().trim();
				if (first && line.indexOf("<?xml") != -1) {
					throw new SelfParseException("csv format must be text, not XML", stream);
				}
				// Skip blank lines.
				while (line.isEmpty()) {
					if (stream.atEnd()) {
						return;
					}
					line = stream.nextLine().trim();
					if (!line.isEmpty()) {
						memory = getBot().memory().newMemory();
					}
				}
				// Allow either ',' or '","' separators.
				boolean quotes = line.contains("\"");
				// "questions","answer","topic"
				// "What is this? What's this?","This is Open Bot.","Bot"
				TextStream lineStream = new TextStream(line);
				if (quotes) {
					lineStream.skipTo('"');
					lineStream.skip();
					if (lineStream.atEnd()) {
						getBot().log(getBot().memory(), "Expecting \" character", Level.WARNING, line);
						continue;
					}
				}
				if (first) {
					// Process columns
					while (!lineStream.atEnd()) {
						String value = null;
						if (quotes) {
							value = lineStream.upToAll("\",\"").trim();
							lineStream.skip("\",\"".length());
						} else {
							value = lineStream.upTo(',').trim();
							lineStream.skip();
						}
						columns.add(memory.createVertex(new Primitive(value)));
					}
					first = false;
				} else {
					Vertex object = null;
					// Process values
					int index = 0;
					while (!lineStream.atEnd()) {
						String value = null;
						if (quotes) {
							value = lineStream.upToAll("\",\"").trim();
							lineStream.skip("\",\"".length());
						} else {
							value = lineStream.upTo(',').trim();
							lineStream.skip();
						}
						if (lineStream.atEnd() && !value.isEmpty() && value.charAt(value.length() - 1) == '"') {
							value = value.substring(0, value.length() - 1);
						}
						Vertex column = columns.get(index);
						boolean data = false; //column.is(Primitive.DATA);
						if (object == null) {
							//if (data) {
							//	object = memory.createVertex(value);
							//} else {
								object = memory.createVertex();
							//}
						}
						if (!data && !value.isEmpty()) {
							object.addRelationship((Primitive)column.getData(), memory.createVertex(value));
						}
						index++;
					}
					if (object != null) {
						if (pin) {
							object.setPinned(true);
							object.pinChildren();
						}
						memory.save();
						this.results.add(readOnlyMemory.createVertex(object));
					}
				}
			}
		} else if ("json".equals(format)) {
			result = SelfCompiler.getCompiler().evaluateExpression(inputData, memory.createVertex(Primitive.SELF), memory.createVertex(Primitive.SELF), false, false, memory);
			if (pin) {
				if (result.isArray()) {
					for (Relationship relationship : result.getRelationships(Primitive.ELEMENT)) {
						relationship.getTarget().setPinned(true);
						relationship.getTarget().pinChildren();
					}
				} else {
					result.setPinned(true);
					result.pinChildren();
				}
			}
			this.mode = BrowseMode.Search;
			this.results = new ArrayList<Vertex>();
			if (result == null) {
				return;
			}
			if (result.isArray()) {
				for (Relationship relationship : result.getRelationships(Primitive.ELEMENT)) {
					this.results.add(relationship.getTarget());
				}
			} else {
				this.results.add(result);
			}
		} else if ("set".equals(format)) {
			this.results = AIMLParser.parser().parseSET(inputData, Utils.upTo(fileName, "."), pin, memory);
			this.mode = BrowseMode.Search;
		} else if ("map".equals(format)) {
			this.results = AIMLParser.parser().parseMAP(inputData, Utils.upTo(fileName, "."), pin, memory);
			this.mode = BrowseMode.Search;
		} else if ("properties".equals(format)) {
			this.results = AIMLParser.parser().parseProperties(inputData, pin, memory);
			this.mode = BrowseMode.Search;
			this.results.add(readOnlyMemory.createVertex(Primitive.SELF));
		} else {
			throw new BotException("Invalid file format - " + format);
		}
		this.resultsSize = this.results.size();
		memory.save();
	}
	
	@SuppressWarnings("unchecked")
	public void processGraph(HttpServletRequest request) {
		reset();
		this.mode = BrowseMode.Graph;
		
		this.results = new ArrayList<Vertex>();
		for (Vertex vertex : lookupVerticesList(request)) {
			this.results.addAll(getBot().memory().newMemory().findAllQuery("Select v from Vertex v where v.id = " + vertex.getId()));
		}
		this.resultsSize = this.results.size();
	}

	public String displayGraph(Vertex root, int depth) {
		StringWriter writer = new StringWriter();
		writeGraph(writer, root, "", depth);
		return writer.toString();
	}
	
	private void writeGraph(StringWriter writer, Vertex root, String parentId, int depth) {
		writer.write("var i = data.addRow([{v:\"" + root.getId().toString() +"\", f:\"" + root.getId().toString() +"<div>" + root.getDataType() + "</div><div><b>" + (root.getDataValue() == null ? "" : Utils.escapeHTML(root.getDataValue())) + "</b></div>\"}, \"" + parentId + "\", \"" + Utils.escapeHTML(root.toString()) + "\"]);\n");	
		if(root.isMeta()) {
			writer.write("data.setRowProperty(i, 'style', 'border: 3px solid orange');\n");
		}
		else {
			writer.write("data.setRowProperty(i, 'style', 'border: 3px solid blue');\n");
		}
		if(depth > 0) {
			for(Relationship r : root.getAllRelationships()) {
				writer.write("var j = data.addRow([{v:\"" + r.getId().toString() +"\", f:\"" + r.getId().toString() +"<div>" + r.getType() + "</div>\"}, \"" + root.getId() + "\", \"" + Utils.escapeHTML(r.toString()) + "\"]);\n");
				writer.write("data.setRowProperty(j, 'style', 'border: 3px solid green');\n");
				
				if(depth > 0) {
					writeGraph(writer, r.getTarget(), r.getId().toString(), depth-1);
					if(r.hasMeta()) {
						writeGraph(writer, r.getMeta(), r.getId().toString(), depth-1);
					}
				}
			}
		}
	}
	
	/**
	 * Import the data from the website.
	 */
	public void processWebImport(String input, String web, String mode, String xpath) throws MalformedURLException  {
		checkMemory();
		this.webInput = Utils.sanitize(input);
		this.webXPath = Utils.sanitize(xpath);
		this.web = Utils.sanitize(web);
		this.webMode = Utils.sanitize(mode);
		this.mode = BrowseMode.Search;
		this.results = new ArrayList<Vertex>();
		this.loginBean.getBotBean().clearLog();
		input = input.trim();
		if (input.length() == 0) {
			return;
		}
		Network network = getBot().memory().newMemory();
		Vertex result = null;
		if (web.equals("Wiktionary")) {
			this.webMode = "Data";
			result = getBot().awareness().getSense(Wiktionary.class).importWord(input.trim(), network);
		} else if (web.equals("WikiData")) {
			this.webMode = "Data";
			result = getBot().awareness().getSense(Wikidata.class).processSearch(input.trim(), 0, false, null, network, new HashMap<String, Vertex>());
		} else {
			if (!input.startsWith("http")) {
				input = "http://" + input;
			}
			this.results = getBot().mind().getThought(Language.class).importHTML(input, xpath, mode, network);
		}
		if (result != null) {
			if (result.instanceOf(Primitive.ARRAY)) {
				List<Vertex> elements = result.orderedRelations(Primitive.ELEMENT);
				if (elements != null) {
					for (Vertex element : elements) {
						this.results.add(element);
					}
					network.save();
				}
			} else {
				this.results.add(result);
			}
		}
		this.resultsSize = this.results.size();
	}

	public List<Vertex> getResults() {
		return results;
	}

	public void setResults(List<Vertex> results) {
		this.results = results;
	}

	public List<Vertex> getSelection() {
		return selection;
	}

	public void setSelection(List<Vertex> selection) {
		this.selection = selection;
	}
	
	public boolean hasSelection() {
		return (this.selection != null) && !this.selection.isEmpty();
	}

	public String getWeb() {
		return web;
	}

	public void setWeb(String web) {
		this.web = web;
	}

	public String getWebMode() {
		return webMode;
	}

	public void setWebMode(String webMode) {
		this.webMode = webMode;
	}

	public String getWebXPath() {
		return webXPath;
	}

	public void setWebXPath(String webXPath) {
		this.webXPath = webXPath;
	}

	public String getWebInput() {
		return webInput;
	}

	public void setWebInput(String webInput) {
		this.webInput = webInput;
	}

	@Override
	public void disconnectInstance() {
		disconnect();
	}
	
	@Override
	public void disconnect() {
		reset();
	}
	
	public void reset() {
		this.mode = BrowseMode.Search;
		this.page = 0;
		this.resultsSize = 0;
		this.results = null;
		this.selection = null;
		this.selectAll = false;
		this.warning = false;
		this.query = null;
		this.code = "";
		this.parameters = null;
		this.input = "*";
		this.web = "Web";
		this.webMode = "Data";
		this.webXPath = "";
		this.webInput = "";
		this.type = "";
		this.classification = "";
		this.sort = "value";
		this.order = "asc";
		this.pinned = false;
	}
}

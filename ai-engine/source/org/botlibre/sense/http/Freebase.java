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
package org.botlibre.sense.http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;


/**
 * Process Freebase pages and data.
 * This sense can load data from the Freebase database into Bot.
 * It uses the Freebase web service to query with MQL for JSON data.
 */

public class Freebase extends Http { //implements DiscoverySense {	
	protected boolean isBatch = true;
	protected int depth = 1;
	protected int batchSize = 100;
	protected List<String> typeFilters;
	protected List<String> discoveryIgnoreWords;

	public static String URL_PREFIX = "http://www.freebase.com/";
	public static int SLEEP = 100; // Sleep between type fetches, to avoid Freebase throttle.
	
	public static String KEY = "";
	
	public Freebase() {
		this.typeFilters = new ArrayList<String>();
		this.typeFilters.add("base");
		this.typeFilters.add("user");
		this.typeFilters.add("type");
		this.typeFilters.add("media_common");
		this.typeFilters.add("common");
		this.typeFilters.add("book");
		this.typeFilters.add("film");
		this.typeFilters.add("music");
		this.typeFilters.add("radio");
		this.typeFilters.add("opera");
		this.typeFilters.add("award");
		this.typeFilters.add("fictional_universe");
		this.typeFilters.add("visual_art");
		
		this.discoveryIgnoreWords = new ArrayList<String>();
		this.discoveryIgnoreWords.add("there");
		this.discoveryIgnoreWords.add("up");
		this.discoveryIgnoreWords.add("going");
	}

	@Override
	public void awake() {
		super.awake();
		Http http = (Http)getBot().awareness().getSense(Http.class.getName());
		http.getDomains().put("www.freebase.com", this);
	}
	
	/**
	 * Get and process the URL.
	 */
	@Override
	public void input(Object input, Network network) throws Exception {
		if (!isEnabled()) {
			return;
		}
		log("Input", Level.INFO, input);
		URL url = (URL)input;
		String domain = url.getPath();
		if (domain.length() > 5 && domain.substring(0, 5).equals("/view")) {
			domain = domain.substring(5, domain.length());
		}
		processId(domain, this.depth, false, "", network, new HashMap<String, Vertex>());
	}

	
	/**
	 * Search for the best object describing the keywords.
	 */
	public Vertex processSearch(String keywords, int cascade, boolean fork, String filter, Network network, Map<String, Vertex> processed) {
		log("Processing search", Level.INFO, keywords);
		
		// First query the object's types.
		JSONObject json = null;
		try {
			json = (JSONObject)processQuery("https://www.googleapis.com/freebase/v1/search?query="
					+ URLEncoder.encode(keywords, "UTF-8"));
		} catch (IOException exception) {
			log("https request failed", Level.WARNING, exception);
			return null;
		}
		if (json.isNullObject()) {
			return null;
		}
		// Get the first result.
		// TODO most conscious result? or top three?
		JSONArray results = json.getJSONArray("result");
		if (results.isEmpty()) {
			return null;
		}
		JSONObject first = results.getJSONObject(0);
		String id = first.getString("mid");
		return processId(id, cascade, fork, filter, network, processed);
	}

	public String replaceKey(String string) {
		if (!KEY.isEmpty()) {
			string = string.replace(KEY, "key");
		}
		return string;
	}
	
	/**
	 * Process the object for the Freebase id.
	 */
	@SuppressWarnings("rawtypes")
	public Vertex processId(String id, final int cascade, boolean fork, String filter, Network network, Map<String, Vertex> processed) {
		id = id.toLowerCase();
		id.replace(' ', '_');
		Vertex object = processed.get(id);
		if (object != null) {
			return object;
		}
		log("Processing Id", Level.INFO, id);
		
		// First query the object's types.
		JSONObject json = null;
		String query = "https://www.googleapis.com/freebase/v1/topic" + id + "?";
		if (fork || cascade < 0) {
			query = query + "filter=/type/object/name&filter=/common/topic/description";
			if (filter != null && !filter.isEmpty()) {
				query=query + "&filter=" + filter;
			}
		}
		try {
			json = (JSONObject)processQuery(query);
		} catch (IOException exception) {
			log("https request failed", Level.WARNING, replaceKey(exception.toString()));
			return null;
		}
		object = fetchDescription(json, cascade, network, processed);		
		if (object == null) {
			log("Id not found", Level.FINE, id);
			return null;
		}
		log("Processed", Level.INFO, id, object);
		network.save();
		
		if (cascade >= 0) {
			if (fork) {
				// Fork the rest to avoid delay.
				final Network threadNetwork = network.getBot().memory().newMemory();
				final Vertex threadObject = threadNetwork.createVertex(object);
				final Map<String, Vertex> threadProcessed = new HashMap<String, Vertex>();
				final String threadId = id;
				Thread thread = new Thread() {
					public void run() {
						int attempt = 0;
						Exception failure = null;
						while (attempt < RETRY) {
							try {
								Utils.sleep(500);
								JSONObject jsonDetails = null;
								try {
									jsonDetails = (JSONObject)processQuery("https://www.googleapis.com/freebase/v1/topic" + threadId + "?");
								} catch (IOException exception) {
									log("https request failed", Level.WARNING, replaceKey(exception.toString()));
									return;
								}
								fetchDetails(threadObject, jsonDetails, cascade, threadProcessed, threadNetwork);
								break;
							} catch (Exception failed) {
								failure = failed;
								log(replaceKey(failed.toString()), Level.WARNING);
								log("Retrying", Level.WARNING);
							}
						}
						if (attempt == RETRY) {
							log("Retry failed", Level.WARNING);
							log(failure);
						}
					}
				};
				thread.start();
			}
			// Fetch filter properties.
			fetchDetails(object, json, cascade, processed, network);

		}
		
		if (! this.isBatch) {
			getBot().memory().addActiveMemory(object);
		} else {
			String domain = extractDomain(id);
			boolean isType = !(domain.equals("m") || domain.equals("en"));
			// Batch process all instances if a type.
			if (isType) {
				log("Batch processing type instances:", Bot.FINE, id);
				int cursor = 0;
				boolean more = true;
				while (more) {
					// Query instances of type in batches of 100
					JSONObject instancesResult = null;
					try {
						instancesResult = (JSONObject)processQuery("https://www.googleapis.com/freebase/v1/search?filter=(any%20type:" + id + ")&?cursor=" + cursor + "&?limit=" + this.batchSize);
					} catch (IOException exception) {
						log(exception);
						return object;
					}
					JSONArray instances = instancesResult.getJSONArray("result");
					if (instances.size() < this.batchSize) {
						more = false;
					} else {
						cursor = cursor + this.batchSize;
					}
					// Create a queue and worker thread for multi-threading.
					final Queue<String> queue = new ConcurrentLinkedQueue<String>();
					final Map<String, Vertex> concurrentProcessed = new ConcurrentHashMap<String, Vertex>();
					final int depth = this.depth;
					Runnable worker = new Runnable() {
						@Override
						public void run() {
							String nextId = queue.poll();
							while (nextId != null) {
								Utils.sleep(SLEEP);
								int attempt = 0;
								Exception failure = null;
								while (attempt < RETRY) {
									try {
										Network memory = getBot().memory().newMemory();
										processId(nextId, depth, false, "", memory, concurrentProcessed);
										memory.save();
										break;
									} catch (Exception failed) {
										failure = failed;
										log(replaceKey(failed.toString()), Level.WARNING);
										log("Retrying", Level.WARNING);
									}
								}
								if (attempt == RETRY) {
									log("Retry failed", Level.WARNING);
									log(failure);
								}
								nextId = queue.poll();
							}
						}
					};
					// Add all ids to the queue.
					for (Iterator iterator = instances.iterator(); iterator.hasNext(); ) {
						JSONObject instance = (JSONObject)iterator.next();
						String instanceId = instance.getString("mid");
						queue.add(instanceId);
					}
					// Process a couple first to avoid contention.
					for (int index = 0; index < 10; index++) {
						String nextId = queue.poll();
						if (nextId != null) {
							Network memory = getBot().memory().newMemory();
							processId(nextId, depth, false, "", memory, concurrentProcessed);
							memory.save();
						}
					}
					if (queue.size() > 0) {
						// Start workers.
						Thread threads[] = new Thread[WORKER_THREADS];
						for (int index = 0; index < WORKER_THREADS; index++) {
							threads[index] = new Thread(worker);
							threads[index].start();
						}
						// Wait for threads to be done.
						for (int index = 0; index < WORKER_THREADS; index++) {
							try {
								threads[index].join();
							} catch (InterruptedException ignore) {}
						}
						// Flush to save memory.
						network.save();
					}
				}
			}
		}
		return object;
	}


	
	/**
	 * Process the compound relationship.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Vertex processCompoundRelationship(JSONObject json, int cascade, Network network, Map<String, Vertex> processed) {
		// TODO: do not process these yet, as of little use.
		if (cascade < 100) {
			return null;
		}
		if (json.isNullObject()) {
			return null;
		}
		// Get the object's name.
		Object id = json.get("id");		
		Vertex object = processed.get(id);
		if (object != null) {
			return object;
		}
		JSONObject properties = (JSONObject)json.get("property");
		
		log("Processing compound relationship", Bot.FINE, id);		
		try {
			object = network.createVertex();
			object.addRelationship(Primitive.INSTANTIATION, Primitive.RELATIONSHIP);
			object.addRelationship(Primitive.INSTANTIATION, Primitive.COMPOUND_RELATIONSHIP);
			// Add relevant properties.
			List<String> keys = new ArrayList<String>(properties.keySet());
			List<String> filteredKeys = new ArrayList<String>();
			for (String key : keys) {
				String domain = extractDomain(key);
				if (!getTypeFilters().contains(domain)) {
					filteredKeys.add(key);
				}
			}
			for (String key : filteredKeys) {
				List values = extractPropertyValues(properties.get(key), Collections.EMPTY_LIST, cascade, network, processed);
				for (Object value : values) {
					String relationship = key;
					// Construct relationship name.
					if (key instanceof String) {
						TextStream stream = new TextStream(key);
						while (!stream.atEnd()) {
							stream.next();								
							relationship = stream.upTo('/');
						}
						relationship = relationship.replace('_', ' ');
					}
					Vertex valueVertex = null;
					if (value instanceof String) {
						valueVertex = network.createObject((String)value);
						valueVertex.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
					} else if (value instanceof Vertex) {
						valueVertex = (Vertex)value;							
					} else {
						valueVertex = network.createVertex(value);
						valueVertex.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
					}
					Vertex relationshipVertex = network.createPrimitive(relationship);
					relationshipVertex.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
					relationshipVertex.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
					relationshipVertex.addRelationship(Primitive.INSTANTIATION, Primitive.RELATIONSHIP);
					object.addRelationship(relationshipVertex, valueVertex);
					log("Processing relationship:", Bot.FINE, object, relationshipVertex, valueVertex);
				}
			}
		} catch (Exception ioException) {
			log(ioException);
		}
		return object;
		
	}
	
	/**
	 * Extract the domain from the property.
	 */
	public String extractDomain(String key) {
		TextStream stream = new TextStream(key);
		stream.next();
		return stream.upTo('/');
	}
	
	/**
	 * Extract the relevant data from the Freebase property.
	 */
	@SuppressWarnings("rawtypes")
	public List<Object> extractPropertyValues(Object data, List<String> filters, int cascade, Network network, Map<String, Vertex> processed) {
		List<Object> values = new ArrayList<Object>();
		Set<Object> valuesSet = new HashSet<Object>();
		if (data instanceof JSONObject) {
			if (((JSONObject)data).get("values") == null) {
				return values;
			}
			JSONArray array = ((JSONObject)data).getJSONArray("values");
			Object type = ((JSONObject)data).get("valuetype");
			for (Iterator iterator = array.iterator(); iterator.hasNext(); ) {
				Object value = iterator.next();
				if (value instanceof JSONObject) {
					if (type.equals("compound")) {
						value = processCompoundRelationship((JSONObject)value, cascade, network, processed);
						if ((value != null) && !valuesSet.contains(value)) {
							valuesSet.add(value);
							values.add(value);
						}
					} else {
						Object id = ((JSONObject)value).get("id");
						if (id instanceof String) {
							// Filter ids.
							String domain = extractDomain((String)id);
							if (filters.contains(domain)) {
								continue;
							}
							// Freebase seems to be use 'm' to donate topics.
							if ((cascade > 0) && domain.equals("m")) {
								Vertex nested = processId((String)id, cascade - 1, false, "", network, processed);
								if (!valuesSet.contains(nested)) {
									valuesSet.add(nested);
									values.add(nested);
								}
								continue;
							}
						}
						Object text = ((JSONObject)value).get("value");
						if (text == null) {
							text = ((JSONObject)value).get("text");
						}
						if (text != null) {
							if (!valuesSet.contains(text)) {
								valuesSet.add(text);
								values.add(text);
							}
						}
					}
				}
			}
		}
			
		return values;
	}
	
	/**
	 * Fetch only the name and description of the object.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public Vertex fetchDescription(JSONObject json, int cascade, Network network, Map<String, Vertex> processed) {
		if (json.isNullObject()) {
			return null;
		}
		// Get the object's name.
		Object id = json.get("id");		
		Vertex object = processed.get(id);
		if (object != null) {
			return object;
		}
		JSONObject properties = (JSONObject)json.get("property");
		if (properties == null) {
			return null;
		}
		List names = extractPropertyValues(properties.get("/type/object/name"), Collections.EMPTY_LIST, cascade, network, processed);
		Object name = null;
		if (names.size() > 0) {
			name = names.get(0);
		}
		List descriptions = extractPropertyValues(properties.get("/common/topic/description"), Collections.EMPTY_LIST, cascade, network, processed);
		log("Processing object:", Bot.FINE, id, names);
		
		try {
			if ((name instanceof String) && (((String)name).length() > 0)) {
				// Add names.
				object = network.createObject((String)name);
				if (object.hasRelationship(getPrimitive())) {
					return object;
				}
				object.addRelationship(getPrimitive(), network.createVertex(id));
				processed.put((String)id, object);
				object.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
				for (Object eachName : names) {
					if (eachName instanceof String) {
						Vertex word = network.createWord((String)eachName);
						word.addRelationship(Primitive.MEANING, object);
						object.addRelationship(Primitive.WORD, word);
						network.associateCaseInsensitivity((String)eachName, object);
					}
				}
				for (Object description : descriptions) {
					// Add descriptions.
					if (description instanceof String) {
						Vertex paragraph = network.createParagraph((String)description);
						if (paragraph.instanceOf(Primitive.PARAGRAPH)) {
							object.addRelationship(Primitive.PARAGRAPH, paragraph);
							Vertex sentence = paragraph.orderedRelations(Primitive.SENTENCE).get(0);
							object.addRelationship(Primitive.SENTENCE, sentence);							
						} else {
							object.addRelationship(Primitive.SENTENCE, paragraph);
						}
					}
				}
			} else {
				object = network.createVertex();
			}

			network.save();
		} catch (Exception ioException) {
			log(ioException);
		}
		return object;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void fetchDetails(Vertex object, JSONObject json, int cascade, Map<String, Vertex> processed, Network network) {
		try {
			JSONObject properties = (JSONObject)json.get("property");
			List types = extractPropertyValues(properties.get("/type/object/type"), getTypeFilters(), cascade, network, processed);
			List names = extractPropertyValues(properties.get("/type/object/name"), Collections.EMPTY_LIST, cascade, network, processed);
			Object name = null;
			if (names.size() > 0) {
				name = names.get(0);
			}
			
			// For each type, add instantiation relationship.
			for (Object type : types) {
				if (type instanceof String) {
					// Create vertex for type.
					Vertex typeVertex = network.createPrimitive((String)type);
					typeVertex.addRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION);
					typeVertex.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
					// Associate object with type.
					object.addRelationship(Primitive.INSTANTIATION, typeVertex);
					// Check if it is a person.
					if (((String) type).equalsIgnoreCase("person")) {
						object.addRelationship(Primitive.INSTANTIATION, Primitive.SPEAKER);
						if (name instanceof String) {
							Vertex word = network.createVertex(name);
							word.addRelationship(Primitive.INSTANTIATION, Primitive.NAME);
							object.addRelationship(Primitive.NAME, word);
							for (Object eachName : names) {
								if (eachName instanceof String) {
									word = network.createVertex(eachName);
									word.addRelationship(Primitive.INSTANTIATION, Primitive.NAME);
									object.addRelationship(Primitive.NAME, word);
								}
							}	
						}
					}
				}
			}
			// Extra relevant properties.
			List<String> keys = new ArrayList<String>(properties.keySet());
			List<String> filteredKeys = new ArrayList<String>();
			for (String key : keys) {
				String domain = extractDomain(key);
				if (!getTypeFilters().contains(domain)) {
					filteredKeys.add(key);
				}
			}
			for (String key : filteredKeys) {
				List values = extractPropertyValues(properties.get(key), Collections.EMPTY_LIST, cascade, network, processed);
				for (Object value : values) {
					String relationship = key;
					// Construct relationship name.
					if (key instanceof String) {
						TextStream stream = new TextStream(key);
						while (!stream.atEnd()) {
							stream.next();								
							relationship = stream.upTo('/');
						}
						relationship = relationship.replace('_', ' ');
					}
					Vertex valueVertex = null;
					if (value instanceof String) {
						valueVertex = network.createObject((String)value);
						valueVertex.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
					} else if (value instanceof Vertex) {
						valueVertex = (Vertex)value;							
					} else {
						valueVertex = network.createVertex(value);
						valueVertex.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
					}
					// Add relationship.
					Vertex relationshipVertex = network.createPrimitive(relationship);
					relationshipVertex.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
					relationshipVertex.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
					relationshipVertex.addRelationship(Primitive.INSTANTIATION, Primitive.RELATIONSHIP);
					object.addRelationship(relationshipVertex, valueVertex);
					log("Processing reltionship:", Bot.FINE, object, relationshipVertex, valueVertex);
				}
			}
			network.save();
		} catch (Exception ioException) {
			log(ioException);
		}
	}
	
	/**
	 * Process the mql query and convert the result to a JSON object.
	 */
	public JSON processQuery(String query) throws IOException {
		log("MQL", Level.FINEST, query);
		URL get = null;
		if (KEY.isEmpty()) {
			get = Utils.safeURL(query);
		} else {
			get = Utils.safeURL(query + "&key=" + KEY);
		}
		Reader reader = new InputStreamReader(get.openStream(), "UTF-8");
		StringWriter output = new StringWriter();
		int next = reader.read();
		while (next != -1) {
			output.write(next);
			next = reader.read();
		}
		String result = output.toString();
		log("JSON", Level.FINEST, result);
		return JSONSerializer.toJSON(result);
	}
	
	/**
	 * Post, process the post request.
	 */
	@Override
	public void output(Vertex output) {
		
	}
	
	/**
	 * Attempt to discover information on the sentence words.
	 * Only check upper case words for now.
	 */
	@Override
	public void discoverSentence(Vertex sentence, Network network, Vertex currentTime) {
		List<Relationship> words = sentence.orderedRelationships(Primitive.WORD);
		if (words != null) {
			for (int index = 1; index < words.size(); index++) {
				Relationship relationship = words.get(index);
				String word = relationship.getTarget().getDataValue();
				if (word != null) {
					if (Utils.isCapitalized(word)) {
						String token = word.toLowerCase();
						index++;
						while (index < words.size()) {
							Relationship nextRelationship = words.get(index);
							String nextWord = nextRelationship.getTarget().getDataValue();
							if (nextWord != null) {
								if (Utils.isCapitalized(nextWord)) {
									word = word + " " + nextWord;
									token = token + "_" + nextWord.toLowerCase();
								}
							}
							index++;
						}
						Vertex compoundWord = network.createWord(word);
						Vertex lastChecked = compoundWord.getRelationship(getPrimitive());
						if (lastChecked == null) {
							compoundWord.addRelationship(getPrimitive(), currentTime);
							try {
								processId("/en/" + token, this.depth, false, "", network, new HashMap<String, Vertex>());
							} catch (Exception failed) {
								log(failed);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Discover the meaning of the word including the filter.
	 * Called from Self.
	 */
	public Vertex search(Vertex source, Vertex filter, Vertex vertex) {
		return search(source, filter, vertex, null, null, null, null);
	}
	
	/**
	 * Discover the meaning of the word including the filter.
	 * Called from Self.
	 */
	public Vertex search(Vertex source, Vertex filter, Vertex vertex, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5) {
		return discover(true, true, (String)filter.getData(), vertex, vertex2, vertex3, vertex4, vertex5);
	}
	
	/**
	 * Discover the meaning of the word including the filter.
	 * Called from Self.
	 */
	public Vertex define(Vertex source, Vertex vertex) {
		return define(source, vertex, null);
	}
	
	/**
	 * Discover the meaning of the word only.
	 * Called from Self.
	 */
	public Vertex define(Vertex source, Vertex vertex, Vertex vertex2) {
		return define(source, vertex, vertex2, null, null, null);
	}
	
	/**
	 * Discover the meaning of the word only.
	 * Called from Self.
	 */
	public Vertex define(Vertex source, Vertex vertex, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5) {
		return discover(false, false, null, vertex, vertex2, vertex3, vertex4, vertex5);
	}
	
	/**
	 * Discover the meaning of the word including all details.
	 * Called from Self.
	 */
	public Vertex details(Vertex source, Vertex vertex) {
		return details(source, vertex, null);
	}
	
	/**
	 * Discover the meaning of the word including all details.
	 * Called from Self.
	 */
	public Vertex details(Vertex source, Vertex vertex, Vertex vertex2) {
		return define(source, vertex, vertex2, null, null, null);
	}
	
	/**
	 * Discover the meaning of the word including all details.
	 * Called from Self.
	 */
	public Vertex details(Vertex source, Vertex vertex, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5) {
		return discover(true, false, null, vertex, vertex2, vertex3, vertex4, vertex5);
	}
	
	/**
	 * Discover the meaning of the word.
	 * Called from Self.
	 */
	public Vertex discover(Vertex source, Vertex vertex) {
		return discover(source, vertex, null);
	}
	
	/**
	 * Discover the meaning of the word.
	 * Called from Self.
	 */
	public Vertex discover(Vertex source, Vertex vertex, Vertex vertex2) {
		return discover(source, vertex, vertex2, null, null, null);
	}
	
	/**
	 * Discover the meaning of the word.
	 * Called from Self.
	 */
	public Vertex discover(Vertex source, Vertex vertex, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5) {
		return discover(true, true, null, vertex, vertex2, vertex3, vertex4, vertex5);
	}
	
	/**
	 * Discover the meaning of the word.
	 * Called from Self.
	 */
	public Vertex discover(boolean details, boolean fork, String filter, Vertex vertex, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5) {
		String keywords = vertex.getDataValue();
		String keywordscaps = Utils.capitalize(vertex.getDataValue());
		if ((vertex2 != null) && !vertex2.is(Primitive.NULL)) {
			keywords =  keywords + " " + vertex2.getDataValue();
			keywordscaps =  keywordscaps + " " + Utils.capitalize(vertex2.getDataValue());
		}
		if ((vertex3 != null) && !vertex3.is(Primitive.NULL)) {
			keywords =  keywords + " " + vertex3.getDataValue();
			keywordscaps =  keywordscaps + " " + Utils.capitalize(vertex3.getDataValue());
		}
		if ((vertex4 != null) && !vertex4.is(Primitive.NULL)) {
			keywords =  keywords + " " + vertex4.getDataValue();
			keywordscaps =  keywordscaps + " " + Utils.capitalize(vertex4.getDataValue());
		}
		if ((vertex5 != null) && !vertex5.is(Primitive.NULL)) {
			keywords =  keywords + " " + vertex5.getDataValue();
			keywordscaps =  keywordscaps + " " + Utils.capitalize(vertex5.getDataValue());
		}
		if (keywords != null) {
			if (vertex.instanceOf(Primitive.PRONOUN)
					|| vertex.instanceOf(Primitive.ARTICLE)
					|| vertex.instanceOf(Primitive.PUNCTUATION)
					|| (vertex.hasRelationship(Primitive.MEANING) && (vertex.getRelationship(Primitive.MEANING).instanceOf(Primitive.NUMBER)))
					|| vertex.instanceOf(Primitive.QUESTION)
					|| this.discoveryIgnoreWords.contains(vertex.getData())) {
				return null;
			}
			Vertex compoundWord = vertex.getNetwork().createVertex(keywords);
			Vertex lastChecked = compoundWord.getRelationship(getPrimitive());
			if (lastChecked == null) {
				compoundWord.addRelationship(getPrimitive(), compoundWord.getNetwork().createTimestamp());
				try {
					int cascade = 0;
					if (!details) {
						cascade = -1;
					}
					Vertex result = processId("/en/" + keywords, cascade, fork, filter, vertex.getNetwork(), new HashMap<String, Vertex>());
					if (result == null) {
						result = processSearch(keywords, cascade, fork, filter, vertex.getNetwork(), new HashMap<String, Vertex>());
					}
					if (result != null) {
						compoundWord = vertex.getNetwork().createFragment(keywords);
						compoundWord.addRelationship(Primitive.MEANING, result);
						compoundWord = vertex.getNetwork().createFragment(keywords.toLowerCase());
						compoundWord.addRelationship(Primitive.MEANING, result);
						compoundWord = vertex.getNetwork().createFragment(keywordscaps);
						compoundWord.addRelationship(Primitive.MEANING, result);
					}
					return result;
				} catch (Exception failed) {
					log(failed);
				}
			}
			return compoundWord.mostConscious(Primitive.MEANING);
		}
		return null;
	}


	public List<String> getTypeFilters() {
		return typeFilters;
	}

	public void setTypeFilters(List<String> typeFilters) {
		this.typeFilters = typeFilters;
	}
}
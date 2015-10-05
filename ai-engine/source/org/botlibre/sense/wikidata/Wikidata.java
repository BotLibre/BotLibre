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
package org.botlibre.sense.wikidata;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.http.Http;
import org.botlibre.util.Utils;


/**
 * This sense queries and loads data from Wikidata.
 */

public class Wikidata extends Http { //implements DiscoverySense {	
	protected int depth = 1;
	protected List<String> discoveryIgnoreWords;
	protected static Set<String> globalExcludedProperties;
	protected Set<String> excludedProperties;
	
	protected static Map<String, String> globalPropertiesMap;
	protected Map<String, String> propertiesMap;
	
	static {
		globalExcludedProperties = new HashSet<String>();
		globalExcludedProperties.add("P434");
		globalExcludedProperties.add("P271");
		globalExcludedProperties.add("P227");
		globalExcludedProperties.add("P269");
		globalExcludedProperties.add("P268");
		globalExcludedProperties.add("P349");
		globalExcludedProperties.add("P691");
		globalExcludedProperties.add("P345");
		globalExcludedProperties.add("P646");
		globalExcludedProperties.add("P1280");
		globalExcludedProperties.add("P935");
		globalExcludedProperties.add("P214");
		globalExcludedProperties.add("P1151");
		globalExcludedProperties.add("P213");
		globalExcludedProperties.add("P906");
		globalExcludedProperties.add("P396");
		globalExcludedProperties.add("P244");
		globalExcludedProperties.add("P910");
		globalExcludedProperties.add("P866");
		globalExcludedProperties.add("P373");
		globalExcludedProperties.add("P1375");
		globalExcludedProperties.add("P1472");
		globalExcludedProperties.add("P949");
		globalExcludedProperties.add("P1284");
		globalExcludedProperties.add("P1343");
		globalExcludedProperties.add("P1207");
		globalExcludedProperties.add("P1670");
		globalExcludedProperties.add("P409");
		globalExcludedProperties.add("P1296");
		globalExcludedProperties.add("P1185");
		globalExcludedProperties.add("P1309");
		globalExcludedProperties.add("P1006");
		globalExcludedProperties.add("P1005");
		globalExcludedProperties.add("P998");
		globalExcludedProperties.add("P1368");
		globalExcludedProperties.add("P1185");
		globalExcludedProperties.add("P1890");
		globalExcludedProperties.add("P1417");
		globalExcludedProperties.add("P1695");
		globalExcludedProperties.add("P1263");
		globalExcludedProperties.add("P1749");
		globalExcludedProperties.add("P1842");
		globalExcludedProperties.add("P948");
		globalExcludedProperties.add("P982");
		globalExcludedProperties.add("P1188");
		globalExcludedProperties.add("P1465");
		globalExcludedProperties.add("P1792");
		globalExcludedProperties.add("P1464");
		globalExcludedProperties.add("P1740");
		globalExcludedProperties.add("P1566");
		globalExcludedProperties.add("P982");
		globalExcludedProperties.add("P948");
		globalExcludedProperties.add("P1465");
		globalExcludedProperties.add("P1464");
		globalExcludedProperties.add("P1740");
		globalExcludedProperties.add("P1566");
	}
	
	static {
		globalPropertiesMap = new HashMap<String, String>();
		globalPropertiesMap.put("P21", "gender");
	}

	public static String URL_PREFIX = "http://www.wikidata.org/";
	public static int SLEEP = 100; // Sleep between type fetches, to avoid Wikidata throttle.
		
	public Wikidata() {
		this.excludedProperties = globalExcludedProperties;
		this.propertiesMap = globalPropertiesMap;
		
		this.discoveryIgnoreWords = new ArrayList<String>();
		this.discoveryIgnoreWords.add("there");
		this.discoveryIgnoreWords.add("up");
		this.discoveryIgnoreWords.add("going");
	}

	@Override
	public void awake() {
		super.awake();
		Http http = (Http)getBot().awareness().getSense(Http.class.getName());
		http.getDomains().put("www.wikidata.org", this);
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
			json = (JSONObject)processQuery("https://www.wikidata.org/w/api.php?action=wbsearchentities&format=json&limit=1&language=en&search="
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
		JSONArray results = json.getJSONArray("search");
		if (results.isEmpty()) {
			return null;
		}
		JSONObject first = results.getJSONObject(0);
		String id = first.getString("id");
		return processId(id, cascade, fork, filter, network, processed);
	}
	
	/**
	 * Lookup the wikidata item by id, and load its data.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Vertex processId(String id, final int cascade, boolean fork, String filter, Network network, Map<String, Vertex> processed) {
		Vertex object = processed.get(id);
		if (object != null) {
			return object;
		}
		log("Processing Id", Level.INFO, id);
		
		// First query the object's types.
		JSONObject json = null;
		String query = "https://www.wikidata.org/w/api.php?action=wbgetentities&languages=en&format=json&ids=" + id;
		if (fork || cascade < 0) {
			query = query + "&props=labels|descriptions";
			if (filter != null && !filter.isEmpty()) {
				query = query + "|claims";
			}
		} else {
			query = query + "&props=labels|descriptions|claims";
		}
		try {
			json = (JSONObject)processQuery(query);
		} catch (IOException exception) {
			log("https request failed", Level.WARNING, exception.toString());
			return null;
		}
		json = json.getJSONObject("entities");
		if (json == null) {
			return null;
		}
		json = json.getJSONObject(id);
		if (json == null) {
			return null;
		}
		object = fetchDescription(json, cascade, network, processed);		
		if (object == null) {
			log("Id not found", Level.FINE, id);
			return null;
		}
		network.save();
		if (filter != null && !filter.isEmpty()) {
			// Also fetch filter.
			JSONObject properties = (JSONObject)json.get("claims");
			if (properties != null) {
				List<String> keys = new ArrayList<String>(properties.keySet());
				Map<String, String> propertyMap = fetchPropertyLabels(keys, network, processed);
				String propertyId = null;
				for (Iterator<Map.Entry<String, String>> iterator = propertyMap.entrySet().iterator(); iterator.hasNext(); ) {
					Map.Entry<String, String> entry = iterator.next();
					if (!entry.getValue().equals(filter)) {
						iterator.remove();
					} else {
						propertyId = entry.getKey();
					}
				}
				if (propertyId != null) {
					List<String> ids = extractPropertyValueIds(properties, propertyMap, network, processed);
					Map<String, String> valueMap = fetchPropertyLabels(ids, network, processed);					
					List values = extractPropertyValues(properties.get(propertyId), valueMap, cascade, network, processed);
					for (Object value : values) {
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
						Vertex relationshipVertex = network.createPrimitive(filter);
						relationshipVertex.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
						relationshipVertex.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
						relationshipVertex.addRelationship(Primitive.INSTANTIATION, Primitive.RELATIONSHIP);
						object.addRelationship(relationshipVertex, valueVertex);
						log("Processing reltionship:", Bot.FINE, filter, object, relationshipVertex, valueVertex);
					}
				}
			}
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
									jsonDetails = (JSONObject)processQuery("https://www.wikidata.org/w/api.php?action=wbgetentities&format=json&languages=en&props=labels|descriptions|aliases|claims&ids=" + threadId);
								} catch (IOException exception) {
									log("https request failed", Level.WARNING, exception.toString());
									return;
								}
								jsonDetails = jsonDetails.getJSONObject("entities");
								if (jsonDetails == null) {
									return;
								}
								jsonDetails = jsonDetails.getJSONObject(threadId.toUpperCase());
								if (jsonDetails == null) {
									return;
								}
								fetchDetails(threadObject, jsonDetails, cascade, threadProcessed, threadNetwork);
								break;
							} catch (Exception failed) {
								failure = failed;
								log(failed.toString(), Level.WARNING);
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
		return object;
	}

	
	/**
	 * Lookup the wikidata properites by id.
	 */
	public Map<String, String> fetchPropertyLabels(List<String> properties, Network network, Map<String, Vertex> processed) {
		log("Fetching properties", Level.FINE, properties);
		Map<String, String> values = new HashMap<String, String>(properties.size());
		if (properties.isEmpty()) {
			return values;
		}
		int start = 0;
		int end = 50;
		// Wikidata API has 50 id limit, so must page.
		while (start < properties.size()) {
			List<String> page = properties.subList(start, Math.min(end, properties.size()));
			StringWriter writer = new StringWriter();
			boolean first = true;
			for (String property : page) {
				if (this.excludedProperties.contains(property)) {
					continue;
				}
				if (!first) {
					writer.write("|");
				} else {
					first  = false;
				}
				writer.write(property);
			}
			String ids = writer.toString();
			start = end;
			end = end + 50;
			if (ids.isEmpty()) {
				continue;
			}
			JSONObject json = null;
			String query = "https://www.wikidata.org/w/api.php?action=wbgetentities&languages=en&format=json&props=labels&ids=" + ids;
			try {
				json = (JSONObject)processQuery(query);
			} catch (IOException exception) {
				log("https request failed", Level.WARNING, exception.toString());
				return values;
			}
			Object entities = json.get("entities");
			if (!(entities instanceof JSONObject)) {
				return values;
			}
			json = (JSONObject)entities;
			for (String property : page) {
				String mapping = this.propertiesMap.get(property);
				if (mapping != null) {
					values.put(property, mapping);					
				} else {
					JSONObject data = json.getJSONObject(property);
					if (data.isNullObject()) {
						continue;
					}
					List<String> names = extractText(data.get("labels"));
					if (names.size() > 0) {
						values.put(property, names.get(0));
					}
				}
			}
		}
		
		return values;
	}
	
	/**
	 * Extract the language text from the JSON object.
	 */
	public List<String> extractText(Object data) {
		List<String> values = new ArrayList<String>();
		if (!(data instanceof JSONObject)) {
			return values;
		}
		Object en = ((JSONObject)data).get("en");
		if (en == null) {
			return values;
		} else if (en instanceof JSONObject) {
			values.add(String.valueOf(((JSONObject)en).get("value")));
		} else if (en instanceof JSONArray) {
			for (Object value : ((JSONArray)en)) {
				values.add(String.valueOf(((JSONObject)value).get("value")));
			}
		}
		return values;
	}
	
	/**
	 * Extract the relevant data from the wikidata claims.
	 */
	public List<Object> extractPropertyValues(Object data, Map<String, String> valueMap, int cascade, Network network, Map<String, Vertex> processed) {
		List<Object> values = new ArrayList<Object>();
		Set<Object> valuesSet = new HashSet<Object>();
		if (data instanceof JSONArray) {
			for (Object value : ((JSONArray)data)) {
				if (value instanceof JSONObject) {
					value = ((JSONObject)value).get("mainsnak");
					if (value instanceof JSONObject) {
						value = ((JSONObject)value).get("datavalue");
						if (value instanceof JSONObject) {
							value = ((JSONObject)value).get("value");
							if (value instanceof JSONObject) {
								Object id = ((JSONObject)value).get("numeric-id");
								if (id instanceof Integer) {
									String qid = "Q" + id;
									if (cascade > 0) {
										Vertex nested = processId((String)qid, cascade - 1, false, "", network, processed);
										if (!valuesSet.contains(nested)) {
											valuesSet.add(nested);
											values.add(nested);
										}
									} else if (valueMap != null) {
										String label = valueMap.get(qid);
										if (label != null) {
											if (!valuesSet.contains(label)) {
												valuesSet.add(label);
												values.add(label);
											}											
										}
									}
									continue;
								}
								Object propertyValue = ((JSONObject)value).get("text");
								if (propertyValue == null) {
									propertyValue = ((JSONObject)value).get("time");
									if (propertyValue instanceof String) {
										try {
											propertyValue = Utils.parseDate(((String)propertyValue).substring(1,((String) propertyValue).indexOf('T')));
										} catch (Exception exception) {}
									}
								} else if (propertyValue instanceof String) {
									// Text is normally a name or word.
									propertyValue = network.createWord((String)propertyValue);
								}
								if (propertyValue != null) {
									if (!valuesSet.contains(propertyValue)) {
										valuesSet.add(propertyValue);
										values.add(propertyValue);
									}
								}
							}
						}
					}
				}
			}
		}
			
		return values;
	}
	
	/**
	 * Extract the ids from the wikidata claims.
	 */
	public List<String> extractPropertyValueIds(JSONObject data, Map<String, String> propertyMap, Network network, Map<String, Vertex> processed) {
		List<String> values = new ArrayList<String>();
		Set<String> valuesSet = new HashSet<String>();
		for (Map.Entry<String, String> property : propertyMap.entrySet()) {
			Object propertyValue = data.get(property.getKey());
			if (propertyValue instanceof JSONArray) {
				for (Object value : ((JSONArray)propertyValue)) {
					if (value instanceof JSONObject) {
						value = ((JSONObject)value).get("mainsnak");
						if (value instanceof JSONObject) {
							value = ((JSONObject)value).get("datavalue");
							if (value instanceof JSONObject) {
								value = ((JSONObject)value).get("value");
								if (value instanceof JSONObject) {
									Object id = ((JSONObject)value).get("numeric-id");
									if (id instanceof Integer) {
										String nested = "Q" + id;
										if (!valuesSet.contains(nested)) {
											valuesSet.add(nested);
											values.add(nested);
										}
									}
								}
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
	@SuppressWarnings("rawtypes")
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
		List names = extractText(json.get("labels"));
		Object name = null;
		if (names.size() > 0) {
			name = names.get(0);
		}
		List descriptions = extractText(json.get("descriptions"));
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
			JSONObject properties = (JSONObject)json.get("claims");
			if (properties == null) {
				return;
			}
			// Extra relevant properties.
			List<String> keys = new ArrayList<String>(properties.keySet());
			Map<String, String> propertyMap = fetchPropertyLabels(keys, network, processed);
			List<String> ids = extractPropertyValueIds(properties, propertyMap, network, processed);
			Map<String, String> valueMap = null;
			if (cascade <= 0) {
				valueMap = fetchPropertyLabels(ids, network, processed);
			}
			
			List types = extractPropertyValues(properties.get("P31"), valueMap, cascade, network, processed);
			propertyMap.remove("P31");
			List names = extractText(json.get("labels"));
			List aliases = extractText(json.get("aliases"));
			Object name = null;
			if (names.size() > 0) {
				name = names.get(0);
			}
			
			// For each type, add instantiation relationship.
			for (Object type : types) {
				if (type instanceof String) {
					type = network.createObject((String)type);
				}
				if (type instanceof Vertex) {
					// Create vertex for type.
					Vertex typeVertex = (Vertex)type;
					typeVertex.addRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION);
					typeVertex.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
					// Associate object with type.
					object.addRelationship(Primitive.INSTANTIATION, typeVertex);
					// Check if it is a person.
					if ((typeVertex.getName() != null && typeVertex.getName().equalsIgnoreCase("human"))) {
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
			// Add aliases
			for (Object alias : aliases) {
				if (alias instanceof String) {
					Vertex word = network.createWord((String)alias);
					word.addRelationship(Primitive.MEANING, object);
					object.addRelationship(Primitive.WORD, word);
					network.associateCaseInsensitivity((String)alias, object);
				}
			}
			for (Map.Entry<String, String> property : propertyMap.entrySet()) {
				List values = extractPropertyValues(properties.get(property.getKey()), valueMap, cascade, network, processed);
				for (Object value : values) {
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
					Vertex relationshipVertex = network.createPrimitive(property.getValue());
					relationshipVertex.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
					relationshipVertex.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
					relationshipVertex.addRelationship(Primitive.INSTANTIATION, Primitive.RELATIONSHIP);
					object.addRelationship(relationshipVertex, valueVertex);
					log("Processing reltionship:", Bot.FINE, property.getKey(), object, relationshipVertex, valueVertex);
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
		log("API", Level.FINEST, query);
		URL get = new URL(query);
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
	 * Discover the meaning of the word including the filter.
	 * Called from Self.
	 */
	public Vertex search(Vertex filter, Vertex vertex) {
		return search(filter, vertex, null, null, null, null);
	}
	
	/**
	 * Discover the meaning of the word including the filter.
	 * Called from Self.
	 */
	public Vertex search(Vertex filter, Vertex vertex, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5) {
		return discover(true, true, (String)filter.getData(), vertex, vertex2, vertex3, vertex4, vertex5);
	}
	
	/**
	 * Discover the meaning of the word including the filter.
	 * Called from Self.
	 */
	public Vertex define(Vertex vertex) {
		return define(vertex, null);
	}
	
	/**
	 * Discover the meaning of the word only.
	 * Called from Self.
	 */
	public Vertex define(Vertex vertex, Vertex vertex2) {
		return define(vertex, vertex2, null, null, null);
	}
	
	/**
	 * Discover the meaning of the word only.
	 * Called from Self.
	 */
	public Vertex define(Vertex vertex, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5) {
		return discover(false, false, null, vertex, vertex2, vertex3, vertex4, vertex5);
	}
	
	/**
	 * Discover the meaning of the word including all details.
	 * Called from Self.
	 */
	public Vertex details(Vertex vertex) {
		return details(vertex, null);
	}
	
	/**
	 * Discover the meaning of the word including all details.
	 * Called from Self.
	 */
	public Vertex details(Vertex vertex, Vertex vertex2) {
		return define(vertex, vertex2, null, null, null);
	}
	
	/**
	 * Discover the meaning of the word including all details.
	 * Called from Self.
	 */
	public Vertex details(Vertex vertex, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5) {
		return discover(true, false, null, vertex, vertex2, vertex3, vertex4, vertex5);
	}
	
	/**
	 * Discover the meaning of the word.
	 * Called from Self.
	 */
	public Vertex discover(Vertex vertex) {
		return discover(vertex, null);
	}
	
	/**
	 * Discover the meaning of the word.
	 * Called from Self.
	 */
	public Vertex discover(Vertex vertex, Vertex vertex2) {
		return discover(vertex, vertex2, null, null, null);
	}
	
	/**
	 * Discover the meaning of the word.
	 * Called from Self.
	 */
	public Vertex discover(Vertex vertex, Vertex vertex2, Vertex vertex3, Vertex vertex4, Vertex vertex5) {
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
					Vertex result = null;
					if (result == null) {
						result = processSearch(keywords, cascade, fork, filter, vertex.getNetwork(), new HashMap<String, Vertex>());
					}
					if (result != null) {
						compoundWord = vertex.getNetwork().createWord(keywords);
						compoundWord.addRelationship(Primitive.MEANING, result);
						compoundWord = vertex.getNetwork().createWord(keywords.toLowerCase());
						compoundWord.addRelationship(Primitive.MEANING, result);
						compoundWord = vertex.getNetwork().createWord(keywordscaps);
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

	public Set<String> getExcludedProperties() {
		return excludedProperties;
	}

	public void setExcludedProperties(Set<String> excludedProperties) {
		this.excludedProperties = excludedProperties;
	}
	
}
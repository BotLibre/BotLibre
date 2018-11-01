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
package org.botlibre.sense.service;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicSense;
import org.botlibre.sense.http.Freebase;
import org.botlibre.sense.http.Http;
import org.botlibre.sense.http.Wiktionary;
import org.botlibre.sense.wikidata.Wikidata;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Process remote service requests, such as SRAIX, Pannous, BotBots, Freebase, Wikidata, HTML, XML, Twiter, Facebook, RSS.
 */

public class RemoteService extends BasicSense {
	public static String PANNOUS = "http://weannie.pannous.com";
	public static String SERVER = "http://www.botlibre.com";
	
	protected ThreadLocal<DocumentBuilder> parser = new ThreadLocal<DocumentBuilder>();

	public RemoteService() {
	}
	
	/**
	 * Invoke the remote service request, and return the result.
	 */
	public String request(String message, String bot, String botid, String server, Primitive service, String apikey, int limit, String hint, Network network) throws Exception {
		if (!isEnabled()) {
			return null;
		}
		try {
			log("Request", Level.INFO, message);
			if (service != null) {
				if (service.equals(Primitive.PANNOUS)) {
					return requestPannous(message, botid, server, apikey, limit);
				} else if (service.equals(Primitive.BOTLIBRE)) {
					server = SERVER;					
				} else if (service.equals(Primitive.BOTLIBRETWITTER)) {
					server = "http://twitter.botlibre.com";					
				} else if (service.equals(Primitive.PAPHUS)) {
					server = "http://www.botlibre.biz";					
				} else if (service.equals(Primitive.WIKIDATA)) {
					return requestWikidata(message, botid, server, apikey, limit, hint, network);
				} else if (service.equals(Primitive.FREEBASE)) {
					return requestFreebase(message, botid, server, apikey, limit, hint, network);
				} else if (service.equals(Primitive.WIKTIONARY)) {
					return requestWiktionary(message, botid, server, apikey, limit, hint, network);
				} else if (service.equals(Primitive.XML)) {
					return requestXML(message, botid, server, apikey, limit, hint, network);
				} else if (service.equals(Primitive.JSON)) {
					return requestJSON(message, botid, server, apikey, limit, hint, network);
				} else if (service.equals(Primitive.HTML)) {
					return requestHTML(message, botid, server, apikey, limit, hint, network);
				} else if (service.equals(Primitive.FORGE)) {
					return requestFORGE(message, botid, server, apikey, limit, hint, network);
				}
			}
			if ((server == null || server.isEmpty()) && (botid == null || botid.isEmpty()) && (bot == null || bot.isEmpty())) {
				return requestPannous(message, botid, server, apikey, limit);
			}
			if (server != null && !server.isEmpty()) {
				server = server.toLowerCase();
				if (!server.startsWith("http")) {
					server = "http://" + server;
				}
			} else {
				server = SERVER;
			}
			String url = server + "/pandora/talk-xml?";
			if (botid != null && !botid.isEmpty()) {
				url = url + "botid=" + botid;
			} else if (bot != null && !bot.isEmpty()) {
				url = url + "botid=" + Utils.encodeURL(bot);
			}
			if (apikey != null && !apikey.isEmpty()) {
				url = url + "&custid=" + apikey;
			}
			url = url + "&input=" + Utils.encodeURL(message);
			log("SERVICE", Level.INFO, url);
			InputStream stream = Utils.openStream(new URL(url), 20000);
			String result = Utils.loadTextFile(stream, "UTF-8", 1000000);
			log("Response", Level.FINE, result);
			Element dom = parseXML(result);
			log("Response", Level.FINE, result);
			if (result == null) {
				return null;
			}
			NodeList thats = dom.getElementsByTagName("that");
			if (thats == null || thats.getLength() == 0) {
				return null;
			}
			String text = thats.item(0).getTextContent().trim();
			if (limit > 0) {
				StringWriter writer = new StringWriter();
				TextStream textStream = new TextStream(text);
				for (int index = 0; index < limit; index++) {
					if (textStream.atEnd()) {
						break;
					}
					writer.write(textStream.nextSentence());
				}
				text = writer.toString();
			}
			return text;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}
	
	/**
	 * Invoke the Wikidata sense.
	 */
	public String requestWikidata(String message, String botid, String server, String apikey, int limit, String hint, Network network) throws Exception {
		try {
			log("WIKIDATA", Level.INFO, message);
			Vertex result = getBot().awareness().getSense(Wikidata.class).processSearch(message, -1, false, hint, network, new HashMap<String, Vertex>());
			if (result != null) {
				if (hint != null && !hint.isEmpty()) {
					Vertex value = result.getRelationship(network.createPrimitive(hint));
					if (value != null) {
						Vertex word = value.getRelationship(Primitive.WORD);
						return word.printString();
					}
				} else {
					Vertex description = result.getRelationship(Primitive.SENTENCE);
					if (description != null) {
						return description.printString();
					}
				}
			}
		} catch (Exception exception) {
			log(exception);
		}
		return null;
	}
	
	/**
	 * Invoke the XML HTTP request.
	 */
	public String requestXML(String message, String botid, String server, String apikey, int limit, String hint, Network network) throws Exception {
		try {
			log("XML", Level.INFO, message);
			Vertex result = null;
			if (hint != null && !hint.isEmpty()) {
				result = getBot().awareness().getSense(Http.class).requestXML(message, hint, null, network);
			} else {
				result = getBot().awareness().getSense(Http.class).requestXML(message, null, null, network);
			}
			if (result == null) {
				return null;
			}
			return result.printString();
		} catch (Exception exception) {
			log(exception);
		}
		return null;
	}
	
	/**
	 * Invoke the XML HTTP request.
	 */
	public String requestJSON(String message, String botid, String server, String apikey, int limit, String hint, Network network) throws Exception {
		try {
			log("JSON", Level.INFO, message);
			Vertex result = null;
			if (hint != null && !hint.isEmpty()) {
				result = getBot().awareness().getSense(Http.class).requestJSON(message, hint, null, network);
				if (result == null) {
					return null;
				}
			} else {
				result = getBot().awareness().getSense(Http.class).requestJSON(message, null, null, network);
			}
			if (result == null) {
				return null;
			}
			return result.printString();
		} catch (Exception exception) {
			log(exception);
		}
		return null;
	}
	
	/**
	 * Invoke the XML HTTP request.
	 */
	public String requestFORGE(String message, String botid, String server, String apikey, int limit, String hint, Network network) throws Exception {
		try {
			log("FORGE", Level.INFO, message);
			String url = "";
			if (server == null || server.isEmpty()) {
				server = "http://www.personalityforge.com";
			} else {
				server = server.toLowerCase();
				if (!server.startsWith("http")) {
					server = "http://" + server;
				}
			}
			url = server + "/api/chat/?apiKey=" + apikey + "&chatBotID=" + botid + "&message=" + message + "&externalID=123";
			String json = Utils.httpGET(url);
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json);
			if (root == null) {
				return null;
			}
			Object response = root.get("message");
			if (!(response instanceof JSONObject)) {
				return null;
			}
			response = ((JSONObject)response).get("message");
			if (!(response instanceof String)) {
				return null;
			}
			return (String)response;
		} catch (Exception exception) {
			log(exception);
		}
		return null;
	}
	
	/**
	 * Invoke the HTML HTTP request.
	 */
	public String requestHTML(String message, String botid, String server, String apikey, int limit, String hint, Network network) throws Exception {
		try {
			log("HTML", Level.INFO, message);
			Vertex result = null;
			if (hint != null && !hint.isEmpty()) {
				result = getBot().awareness().getSense(Http.class).requestHTML(message, hint, "text", null, network);
			}
			if (result == null) {
				return null;
			}
			return result.printString();
		} catch (Exception exception) {
			log(exception);
		}
		return null;
	}
	
	/**
	 * Invoke the Freebase sense.
	 */
	public String requestFreebase(String message, String botid, String server, String apikey, int limit, String hint, Network network) throws Exception {
		try {
			log("FREEBASE", Level.INFO, message);
			Vertex result = getBot().awareness().getSense(Freebase.class).processSearch(message, -1, false, hint, network, new HashMap<String, Vertex>());
			if (result != null) {
				if (hint != null && !hint.isEmpty()) {
					Vertex value = result.getRelationship(network.createPrimitive(hint));
					if (value != null) {
						Vertex word = value.getRelationship(Primitive.WORD);
						return word.printString();
					}
				} else {
					Vertex description = result.getRelationship(Primitive.SENTENCE);
					if (description != null) {
						return description.printString();
					}
				}
			}
		} catch (Exception exception) {
			log(exception);
		}
		return null;
	}
	
	/**
	 * Invoke the Wiktionary sense.
	 */
	public String requestWiktionary(String message, String botid, String server, String apikey, int limit, String hint, Network network) throws Exception {
		try {
			log("WIKTIONARY", Level.INFO, message);
			Vertex word = network.createWord(message);
			Vertex result = getBot().awareness().getSense(Wiktionary.class).define(word, word);
			if (result != null) {
				Vertex description = result.getRelationship(Primitive.SENTENCE);
				if (description != null) {
					return description.printString();
				}
			}
		} catch (Exception exception) {
			log(exception);
		}
		return null;
	}
	
	/**
	 * Invoke the Pannous service.
	 */
	public String requestPannous(String message, String botid, String server, String apikey, int limit) throws Exception {
		try {
			if (server != null && !server.isEmpty()) {
				server = server.toLowerCase();
				if (!server.startsWith("http")) {
					server = "http://" + server;
				}
			} else {
				server = PANNOUS;
			}
			String url = server + "/api?input=" + Utils.encodeURL(message);
			log("PANNOUS", Level.INFO, url);
			InputStream stream = Utils.openStream(new URL(url));
			String result = Utils.loadTextFile(stream, "UTF-8", 1000000);
			log("Response", Level.INFO, result);
			JSONObject json = (JSONObject)JSONSerializer.toJSON(result);
			if (json == null || json.isNullObject()) {
				return null;
			}
			JSONArray outputs = json.getJSONArray("output");
			if (outputs == null || outputs.isEmpty()) {
				return null;
			}
			JSONObject output = (JSONObject)outputs.get(0);
			if (output == null || output.isNullObject()) {
				return null;
			}
			JSONObject actions = output.getJSONObject("actions");
			if (actions == null || actions.isNullObject()) {
				return null;
			}
			JSONObject value = actions.getJSONObject("say");
			if (value == null || value.isNullObject()) {
				return null;
			}
			String text = value.getString("text");
			if (text == null) {
				return null;
			}
			if (limit > 0) {
				StringWriter writer = new StringWriter();
				TextStream textStream = new TextStream(text);
				for (int index = 0; index < limit; index++) {
					if (textStream.atEnd()) {
						break;
					}
					writer.write(textStream.nextSentence());
				}
				text = writer.toString();
			}
			return text;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}
	
	/**
	 * Stop sensing.
	 */
	@Override
	public void shutdown() {
		super.shutdown();
		disconnect();
	}
	
	/**
	 * Reset state when instance is pooled.
	 */
	@Override
	public void pool() {
		disconnect();
	}
	
	public void disconnect() {
		this.parser.remove();
	}

	public DocumentBuilder getParser() throws Exception {
		if (this.parser.get() == null) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			this.parser.set(factory.newDocumentBuilder());
		}
		return this.parser.get();
	}
	
	/**
	 * Parse the input XML stream into a DOM.
	 */
	public Element parseXML(String xml) throws Exception {
		InputSource input = new InputSource();
		input.setCharacterStream(new StringReader(xml));
		Document document = getParser().parse(input);
		return document.getDocumentElement();
	}

	/**
	 * Post, process the post request.
	 */
	@Override
	public void output(Vertex output) {
		
	}
	
}
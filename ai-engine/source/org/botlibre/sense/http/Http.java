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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicSense;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Process http requests, gets and puts.
 */

public class Http extends BasicSense {
	public static int WORKER_THREADS = 1;
	
	protected ThreadLocal<DocumentBuilder> parser = new ThreadLocal<DocumentBuilder>();
	protected ThreadLocal<HtmlCleaner> htmlCleaner = new ThreadLocal<HtmlCleaner>();
	
	protected Map<String, Http> domains;
	
	/**
	 * Thread implementation to allow multi-threading in URL processing.
	 */
	class WorkerThread implements Runnable {
		Queue<URL> urls;
		
		@Override
		public void run() {
			Network memory = getBot().memory().newMemory();
			while (!urls.isEmpty()) {
				URL url = urls.poll();
				batchProcessURL(url, memory);
			}
		}
	}

	/**
	 * Process the URL as part of a batch.
	 */
	public void batchProcessURL(URL url, Network network) {
		if (url == null) {
			return;
		}
		log("Input", Level.FINE, url);
		Element root = parseURL(url);
		if (root != null) {
			int attempt = 0;
			Exception failure = null;
			while (attempt < RETRY) {
				attempt++;
				try {
					processRoot(root, url, network);
					network.save();
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
	}

	public Http() {
		this.domains = new HashMap<String, Http>();
	}

	/**
	 * Convert the HTML input stream into DOM parsable XHTML.
	 */
	public StringReader convertToXHTML(InputStream input) throws IOException {
		StringWriter output = new StringWriter();
		
		/*int next = input.read();
		while (next != -1) {
			output.write(next);
			next = input.read();
		}
		String result = output.toString();
		System.out.println(result);*/
		
		TagNode node = getHtmlCleaner().clean(input, "UTF-8");
		//TagNode node = getHtmlCleaner().clean(result);
		node.serialize(new SimpleXmlSerializer(getHtmlCleaner().getProperties()), output);
		output.flush();
		String xhtml = output.toString();
		return new StringReader(xhtml);
	}

	/**
	 * Convert the HTML input stream into DOM parsable XHTML.
	 */
	public String convertToXHTML(String html) throws IOException {
		StringWriter output = new StringWriter();
		TagNode node = getHtmlCleaner().clean(html);
		node.serialize(new SimpleXmlSerializer(getHtmlCleaner().getProperties()), output);
		output.flush();
		return output.toString();
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

	public HtmlCleaner getHtmlCleaner() {
		if (this.htmlCleaner.get() == null) {
			this.htmlCleaner.set(new HtmlCleaner());
		}
		return this.htmlCleaner.get();
	}
	
	/**
	 * Parse the input HTML into a DOM.
	 */
	public Element parseHTML(String html) throws Exception {
		String xhtml = convertToXHTML(html);
		StringReader reader = new StringReader(xhtml);
		Document document = getParser().parse(new InputSource(reader));
		return document.getDocumentElement();
	}
	
	/**
	 * Parse the input XHTML stream into a DOM.
	 */
	public Element parseXHTML(StringReader input) throws Exception {
		Document document = getParser().parse(new InputSource(input));
		return document.getDocumentElement();
	}
	
	/**
	 * Parse the input XML stream into a DOM.
	 */
	public Element parseXML(InputStream input) throws Exception {
		Document document = getParser().parse(input, "UTF-8");
		return document.getDocumentElement();
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
		URL url = null;
		if (input instanceof URL) {
			url = (URL)input;
		} else if (input instanceof URI) {
			url = (URL)((URI)input).toURL();
		} else {
			return;
		}
		// Redirect to specialization.
		String domain = url.getHost();
		Http domainSense = getDomains().get(domain);
		if (domainSense != null) {
			domainSense.input(url);
			return;
		}
		// Parse the HTML as a DOM.
		Element root = parseURL(url);
		processRoot(root, url, network);
	}

	/**
	 * Parse the HTML as a DOM.
	 */
	public Element parseURL(URL url) {
		try {
			InputStream stream = Utils.openStream(url);
			StringReader reader = convertToXHTML(stream);
			return parseXHTML(reader);
		} catch (FileNotFoundException notFound) {
			log(notFound.toString(), Level.INFO);
			return null;
		} catch (Exception ioException) {
			if (getBot().isDebugFine()) {
				log(ioException);
			} else {
				log(ioException.toString(), Level.WARNING);
			}
			return null;
		}
	}

	/**
	 * Self API.
	 * Return the XML data from the URL.
	 */
	public Vertex requestXML(Vertex source, Vertex url, Vertex xpath) {
		Network network = source.getNetwork();
		return requestXML(url.printString(), xpath.is(Primitive.NULL) ? null : xpath.printString(), null, network);
	}

	/**
	 * Self API.
	 * Return the XML data from the URL.
	 */
	public Vertex requestXML(Vertex source, Vertex url, Vertex xpath, Vertex headerObject) {
		Network network = source.getNetwork();
		Map<String, String> headers = new HashMap<>();
		for (Relationship relationship : headerObject.getAllRelationships()) {
			if (relationship.getType().getData() instanceof String) {
				headers.put((String)relationship.getType().getData(), relationship.getTarget().printString());
			}
		}
		return requestXML(url.printString(), xpath.is(Primitive.NULL) ? null : xpath.printString(), headers, network);
	}

	/**
	 * Self API.
	 * Return the XML data from the URL.
	 */
	public Vertex requestXMLAuth(Vertex source, Vertex url, Vertex user, Vertex password, Vertex xpath) {
		Network network = source.getNetwork();
		return requestXMLAuth(url.printString(), user.printString(), password.printString(), xpath.is(Primitive.NULL) ? null : xpath.printString(), network);
	}

	/**
	 * Self API.
	 * Return the XML data from the URL.
	 */
	public Vertex requestXMLAuth(Vertex source, Vertex url, Vertex user, Vertex password, Vertex agent, Vertex xpath) {
		Network network = source.getNetwork();
		return requestXMLAuth(url.printString(), user.printString(), password.printString(), agent.printString(), xpath.is(Primitive.NULL) ? null : xpath.printString(), network);
	}

	/**
	 * Self API.
	 * Return the HTML data from the URL.
	 */
	public Vertex requestHTML(Vertex source, Vertex url, Vertex xpath) {
		Network network = source.getNetwork();
		return requestHTML(url.printString(), xpath.printString(), null, null, network);
	}

	/**
	 * Self API.
	 * Return the HTML data from the URL.
	 */
	public Vertex requestHTML(Vertex source, Vertex url, Vertex xpath, Vertex format) {
		Network network = source.getNetwork();
		return requestHTML(url.printString(), xpath.printString(), format.printString(), null, network);
	}

	/**
	 * Self API.
	 * Return the HTML data from the URL.
	 */
	public Vertex requestHTML(Vertex source, Vertex url, Vertex xpath, Vertex format, Vertex subformat) {
		Network network = source.getNetwork();
		return requestHTML(url.printString(), xpath.printString(), format.printString(), subformat.printString(), network);
	}

	/**
	 * Self API.
	 * Return the XML data from the URL.
	 */
	public Vertex requestXML(Vertex source, Vertex url) {
		Network network = source.getNetwork();
		return requestXML(url.printString(), null, null, network);
	}

	/**
	 * Return the XML data from the URL.
	 */
	public Vertex requestXML(String url, String xpath, Map<String, String> headers, Network network) {
		log("GET XML", Level.INFO, url, xpath);
		try {
			String xml = Utils.httpGET(url, headers);
			log("XML", Level.FINE, xml);
			InputStream stream = new ByteArrayInputStream(xml.getBytes("utf-8"));
			Element element = parseXML(stream);
			if (element == null) {
				return null;
			}
			if (xpath == null) {
				return convertElement(element, network);
			}
			XPathFactory factory = XPathFactory.newInstance();
			XPath path = factory.newXPath();
			Object node = path.evaluate(xpath, element, XPathConstants.NODE);
			if (node instanceof Element) {
				return convertElement((Element)node, network);
			} else if (node instanceof Attr) {
				return network.createVertex(((Attr)node).getValue());
			} else if (node instanceof org.w3c.dom.Text) {
				return network.createVertex(((org.w3c.dom.Text)node).getTextContent());
			}
			return null;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Return the text data from the URL.
	 */
	public Vertex requestText(String url, Map<String, String> headers, Network network) {
		log("GET TEXT", Level.INFO, url);
		try {
			String text = Utils.httpGET(url, headers);
			log("TEXT", Level.FINE, text);
			return network.createVertex(text);
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Self API.
	 * Return the text data from the URL.
	 */
	public Vertex requestText(Vertex source, Vertex url) {
		Network network = source.getNetwork();
		return requestText(url.printString(), null, network);
	}

	/**
	 * Self API.
	 * Return the text data from the URL.
	 */
	public Vertex requestText(Vertex source, Vertex url, Vertex headerObject) {
		Network network = source.getNetwork();
		Map<String, String> headers = new HashMap<>();
		for (Relationship relationship : headerObject.getAllRelationships()) {
			if (relationship.getType().getData() instanceof String) {
				headers.put((String)relationship.getType().getData(), relationship.getTarget().printString());
			}
		}
		return requestText(url.printString(), headers, network);
	}

	/**
	 * Return the HTML data from the URL.
	 */
	public Vertex requestHTML(String url, String xpath, String format, String subformat, Network network) {
		log("GET HTML", Level.INFO, url, xpath, format, subformat);
		try {
			Element element = parseURL(new URL(url));
			if (element == null) {
				return null;
			}
			XPathFactory factory = XPathFactory.newInstance();
			XPath path = factory.newXPath();
			if ("#array".equals(format)) {
				NodeList nodes = (NodeList)path.evaluate(xpath, element, XPathConstants.NODESET);
				Vertex array = network.createInstance(Primitive.ARRAY);
				for (int index = 0; index < nodes.getLength(); index++) {
					Node node = nodes.item(index);
					Vertex item = null;
					if (node instanceof Element) {
						if ("#text".equals(subformat)) {
							item = network.createVertex(((Element)node).getTextContent());
						} else if ("#xml".equals(subformat) || "#html".equals(subformat)) {
							item = network.createVertex(Utils.printXML((Element)node));
						} else {
							item = convertElement((Element)node, network);
						}
					} else if (node instanceof Attr) {
						item = network.createVertex(((Attr)node).getValue());
					} else if (node instanceof org.w3c.dom.Text) {
						item = network.createVertex(((org.w3c.dom.Text)node).getTextContent());
					}
					if (item != null) {
						array.appendRelationship(Primitive.ELEMENT, item);
					}
				}
				return array;
			}
			Object node = null;
			try {
				node = path.evaluate(xpath, element, XPathConstants.NODE);
			} catch (Exception exception) {
				String text = (String)path.evaluate(xpath, element, XPathConstants.STRING);
				return network.createVertex(text);
			}
			if (node instanceof Element) {
				if ("#text".equals(format)) {
					return network.createVertex(((Element)node).getTextContent());
				} else if ("#xml".equals(format) || "#html".equals(format)) {
					return network.createVertex(Utils.printXML((Element)node));
				}
				return convertElement((Element)node, network);
			} else if (node instanceof Attr) {
				return network.createVertex(((Attr)node).getValue());
			} else if (node instanceof org.w3c.dom.Text) {
				return network.createVertex(((org.w3c.dom.Text)node).getTextContent());
			}
			return null;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Self API.
	 * Return the JSON data object from the URL.
	 */
	public Vertex requestJSON(Vertex source, Vertex url) {
		Network network = source.getNetwork();
		return requestJSON(url.printString(), null, null, network);
	}

	/**
	 * Self API.
	 * Return the JSON data object from the URL.
	 */
	public Vertex requestJSONAuth(Vertex source, Vertex url, Vertex user, Vertex password) {
		Network network = source.getNetwork();
		return requestJSONAuth(url.printString(), user.printString(), password.printString(), network);
	}

	/**
	 * Self API.
	 * Return the JSON data object from the URL.
	 */
	public Vertex requestJSONAuth(Vertex source, Vertex url, Vertex user, Vertex password, Vertex agent) {
		Network network = source.getNetwork();
		return requestJSONAuth(url.printString(), user.printString(), password.printString(), agent.printString(), network);
	}

	/**
	 * Self API.
	 * Send a DELTE request to the URL.
	 */
	public Vertex delete(Vertex source, Vertex url) {
		Network network = source.getNetwork();
		return delete(url.printString(), network);
	}

	/**
	 * Self API.
	 * Return the CSV data object from the URL.
	 */
	public Vertex requestCSV(Vertex source, Vertex url) {
		Network network = source.getNetwork();
		return requestCSV(url.printString(), network);
	}

	/**
	 * Self API.
	 * Return the JSON data object from the URL.
	 */
	public Vertex requestJSON(Vertex source, Vertex attribute, Vertex url) {
		Network network = source.getNetwork();
		return requestJSON(url.printString(), attribute.is(Primitive.NULL) ? null : attribute.printString(), null, network);
	}

	/**
	 * Self API.
	 * Return the JSON data object from the URL.
	 */
	public Vertex requestJSON(Vertex source, Vertex attribute, Vertex url, Vertex headerObject) {
		Network network = source.getNetwork();
		Map<String, String> headers = new HashMap<>();
		for (Relationship relationship : headerObject.getAllRelationships()) {
			if (relationship.getType().getData() instanceof String) {
				headers.put((String)relationship.getType().getData(), relationship.getTarget().printString());
			}
		}
		return requestJSON(url.printString(), attribute.is(Primitive.NULL) ? null : attribute.printString(), headers, network);
	}

	/**
	 * Return the CSV data object from the URL.
	 */
	public Vertex requestCSV(String url, Network network) {
		log("GET CSV", Level.INFO, url);
		try {
			String csv = Utils.httpGET(url);
			Vertex rows = network.createInstance(Primitive.ARRAY);

			TextStream stream = new TextStream(csv);
			boolean first = true;
			List<Vertex> columns = new ArrayList<Vertex>();
			while (!stream.atEnd()) {
				String line = stream.nextLine().trim();
				// Skip blank lines.
				while (line.isEmpty()) {
					if (stream.atEnd()) {
						return rows;
					}
					line = stream.nextLine().trim();
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
						getBot().log(this, "Expecting \" character", Level.WARNING, line);
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
						if (lineStream.atEnd() && !value.isEmpty() && value.charAt(value.length() - 1) == '"') {
							value = value.substring(0, value.length() - 1);
						}
						columns.add(network.createVertex(new Primitive(value)));
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
							// Replace \" escape.
							value = value.replace("\\\"", "\"");
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
								object = network.createVertex();
							//}
						}
						if (!data && !value.isEmpty()) {
							object.addRelationship((Primitive)column.getData(), network.createVertex(value));
						}
						index++;
					}
					if (object != null) {
						rows.addRelationship(Primitive.ELEMENT, network.createVertex(object));
					}
				}
			}
			return rows;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Send a DELETE request to the URL.
	 */
	public Vertex delete(String url, Network network) {
		log("DELETE", Level.INFO, url);
		try {
			Utils.httpDELETE(url);
			return network.createVertex(Primitive.TRUE);
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Return the JSON data object from the URL.
	 */
	public Vertex requestJSON(String url, String attribute, Map<String, String> headers, Network network) {
		log("GET JSON", Level.INFO, url, attribute);
		try {
			String json = Utils.httpGET(url, headers);
			log("JSON", Level.FINE, json);
			JSON root = (JSON)JSONSerializer.toJSON(json.trim());
			if (root == null) {
				return null;
			}
			Object value = root;
			if (attribute != null) {
				value = ((JSONObject)root).get(attribute);
				if (value == null) {
					return null;
				}
			}
			Vertex object = convertElement(value, network);
			return object;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Return the count of the JSON result array.
	 */
	public int countJSON(String url, String attribute, Network network) {
		log("COUNT JSON", Level.INFO, url, attribute);
		try {
			String json = Utils.httpGET(url);
			log("JSON", Level.FINE, json);
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json.trim());
			if (root == null) {
				return 0;
			}
			Object value = root.get(attribute);
			if (value == null) {
				return 0;
			}
			if (value instanceof JSONArray) {
				return ((JSONArray)value).size();
			}
			return 1;
		} catch (Exception exception) {
			log(exception);
			return 0;
		}
	}

	/**
	 * Self API.
	 * POST the JSON object and return the JSON data from the URL.
	 */
	public Vertex postJSON(Vertex source, Vertex url, Vertex jsonObject) {
		Network network = source.getNetwork();
		return postJSON(url.printString(), jsonObject, network);
	}

	/**
	 * Self API.
	 * PUT the JSON object and return the JSON data from the URL.
	 */
	public Vertex putJSON(Vertex source, Vertex url, Vertex jsonObject) {
		Network network = source.getNetwork();
		return putJSON(url.printString(), jsonObject, network);
	}

	/**
	 * Self API.
	 * Post the JSON object and return the JSON data from the URL.
	 */
	public Vertex postJSONAuth(Vertex source, Vertex url, Vertex user, Vertex password, Vertex jsonObject) {
		Network network = source.getNetwork();
		return postJSONAuth(url.printString(), user.printString(), password.printString(), jsonObject, network);
	}

	/**
	 * Self API.
	 * Post the JSON object and return the JSON data from the URL.
	 */
	public Vertex postJSONAuth(Vertex source, Vertex url, Vertex user, Vertex password, Vertex agent, Vertex jsonObject) {
		Network network = source.getNetwork();
		return postJSONAuth(url.printString(), user.printString(), password.printString(), agent.printString(), jsonObject, network);
	}

	/**
	 * Self API.
	 * POST the XML document object and return the XML data from the URL.
	 */
	public Vertex postXML(Vertex source, Vertex url, Vertex xmlObject) {
		Network network = source.getNetwork();
		return postXML(url.printString(), xmlObject, network);
	}

	/**
	 * Post the XML document object and return the XML data from the URL.
	 */
	public Vertex postXML(String url, Vertex xmlObject, Network network) {
		log("POST XML", Level.INFO, url);
		try {
			String data = convertToXML(xmlObject);
			log("POST XML", Level.FINE, data);
			String xml = Utils.httpPOST(url, "application/xml", data);
			log("XML", Level.FINE, xml);
			InputStream stream = new ByteArrayInputStream(xml.getBytes("utf-8"));
			Element element = parseXML(stream);
			if (element == null) {
				return null;
			}
			Vertex root = convertElement(element, network);
			return root;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Post the XML document object and return the XML data from the URL.
	 */
	public Vertex postXMLAuth(String url, String user, String password, Vertex xmlObject, String xpath, Network network) {
		log("POST XML Auth", Level.INFO, url);
		try {
			String data = convertToXML(xmlObject);
			log("POST XML", Level.FINE, data);
			String xml = Utils.httpAuthPOST(url, user, password, "application/xml", data);
			log("XML", Level.FINE, xml);
			InputStream stream = new ByteArrayInputStream(xml.getBytes("utf-8"));
			Element element = parseXML(stream);
			if (element == null) {
				return null;
			}
			XPathFactory factory = XPathFactory.newInstance();
			XPath path = factory.newXPath();
			Object node = path.evaluate(xpath, element, XPathConstants.NODE);
			if (node instanceof Element) {
				return convertElement((Element)node, network);
			} else if (node instanceof Attr) {
				return network.createVertex(((Attr)node).getValue());
			} else if (node instanceof org.w3c.dom.Text) {
				return network.createVertex(((org.w3c.dom.Text)node).getTextContent());
			}
			return null;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Post the XML document object and return the XML data from the URL.
	 */
	public Vertex postXMLAuth(String url, String user, String password, String agent, Vertex xmlObject, String xpath, Network network) {
		log("POST XML Auth", Level.INFO, url);
		try {
			String data = convertToXML(xmlObject);
			log("POST XML", Level.FINE, data);
			String xml = Utils.httpAuthPOST(url, user, password, agent, "application/xml", data);
			log("XML", Level.FINE, xml);
			InputStream stream = new ByteArrayInputStream(xml.getBytes("utf-8"));
			Element element = parseXML(stream);
			if (element == null) {
				return null;
			}
			XPathFactory factory = XPathFactory.newInstance();
			XPath path = factory.newXPath();
			Object node = path.evaluate(xpath, element, XPathConstants.NODE);
			if (node instanceof Element) {
				return convertElement((Element)node, network);
			} else if (node instanceof Attr) {
				return network.createVertex(((Attr)node).getValue());
			} else if (node instanceof org.w3c.dom.Text) {
				return network.createVertex(((org.w3c.dom.Text)node).getTextContent());
			}
			return null;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * POST the JSON object and return the JSON data from the URL.
	 */
	public Vertex postJSON(String url, Vertex jsonObject, Network network) {
		log("POST JSON", Level.INFO, url);
		try {
			String data = convertToJSON(jsonObject);
			log("POST JSON", Level.FINE, data);
			String json = Utils.httpPOST(url, "application/json", data);
			log("JSON", Level.FINE, json);
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json.trim());
			if (root == null) {
				return null;
			}
			Vertex object = convertElement(root, network);
			return object;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * PUT the JSON object and return the JSON data from the URL.
	 */
	public Vertex putJSON(String url, Vertex jsonObject, Network network) {
		log("PUT JSON", Level.INFO, url);
		try {
			String data = convertToJSON(jsonObject);
			log("PUT JSON", Level.FINE, data);
			String json = Utils.httpPUT(url, "application/json", data);
			log("JSON", Level.FINE, json);
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json.trim());
			if (root == null) {
				return null;
			}
			Vertex object = convertElement(root, network);
			return object;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Post the JSON object and return the JSON data from the URL.
	 */
	public Vertex postJSONAuth(String url, String user, String password, Vertex jsonObject, Network network) {
		log("POST JSON Auth", Level.INFO, url);
		try {
			String data = convertToJSON(jsonObject);
			log("POST JSON", Level.FINE, data);
			String json = Utils.httpAuthPOST(url, user, password, "application/json", data);
			log("JSON", Level.FINE, json);
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json.trim());
			if (root == null) {
				return null;
			}
			Vertex object = convertElement(root, network);
			return object;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Post the JSON object and return the JSON data from the URL.
	 */
	public Vertex postJSONAuth(String url, String user, String password, String agent, Vertex jsonObject, Network network) {
		log("POST JSON Auth", Level.INFO, url);
		try {
			String data = convertToJSON(jsonObject);
			log("POST JSON", Level.FINE, data);
			String json = Utils.httpAuthPOST(url, user, password, agent, "application/json", data);
			log("JSON", Level.FINE, json);
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json.trim());
			if (root == null) {
				return null;
			}
			Vertex object = convertElement(root, network);
			return object;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * GET the JSON data from the URL.
	 */
	public Vertex requestJSONAuth(String url, String user, String password, Network network) {
		log("GET JSON Auth", Level.INFO, url);
		try {
			String json = Utils.httpAuthGET(url, user, password);
			log("JSON", Level.FINE, json);
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json.trim());
			if (root == null) {
				return null;
			}
			Vertex object = convertElement(root, network);
			return object;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * GET the JSON data from the URL.
	 */
	public Vertex requestJSONAuth(String url, String user, String password, String agent, Network network) {
		log("GET JSON Auth", Level.INFO, url);
		try {
			String json = Utils.httpAuthGET(url, user, password, agent);
			log("JSON", Level.FINE, json);
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json.trim());
			if (root == null) {
				return null;
			}
			Vertex object = convertElement(root, network);
			return object;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * GET the XML data from the URL.
	 */
	public Vertex requestXMLAuth(String url, String user, String password, String xpath, Network network) {
		log("GET XML Auth", Level.INFO, url);
		try {
			String xml = Utils.httpAuthGET(url, user, password);
			log("XML", Level.FINE, xml);
			InputStream stream = new ByteArrayInputStream(xml.getBytes("utf-8"));
			Element element = parseXML(stream);
			if (element == null) {
				return null;
			}
			if (xpath == null) {
				return convertElement(element, network);
			}
			XPathFactory factory = XPathFactory.newInstance();
			XPath path = factory.newXPath();
			Object node = path.evaluate(xpath, element, XPathConstants.NODE);
			if (node instanceof Element) {
				return convertElement((Element)node, network);
			} else if (node instanceof Attr) {
				return network.createVertex(((Attr)node).getValue());
			} else if (node instanceof org.w3c.dom.Text) {
				return network.createVertex(((org.w3c.dom.Text)node).getTextContent());
			}
			return null;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * GET the XML data from the URL.
	 */
	public Vertex requestXMLAuth(String url, String user, String password, String agent, String xpath, Network network) {
		log("GET XML Auth", Level.INFO, url);
		try {
			String xml = Utils.httpAuthGET(url, user, password, agent);
			log("XML", Level.FINE, xml);
			InputStream stream = new ByteArrayInputStream(xml.getBytes("utf-8"));
			Element element = parseXML(stream);
			if (element == null) {
				return null;
			}
			if (xpath == null) {
				return convertElement(element, network);
			}
			XPathFactory factory = XPathFactory.newInstance();
			XPath path = factory.newXPath();
			Object node = path.evaluate(xpath, element, XPathConstants.NODE);
			if (node instanceof Element) {
				return convertElement((Element)node, network);
			} else if (node instanceof Attr) {
				return network.createVertex(((Attr)node).getValue());
			} else if (node instanceof org.w3c.dom.Text) {
				return network.createVertex(((org.w3c.dom.Text)node).getTextContent());
			}
			return null;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Self API.
	 * Post the HTML forms params and return the HTML data from the URL.
	 */
	public Vertex postHTML(Vertex source, Vertex url, Vertex paramsObject, Vertex xpath) {
		Network network = source.getNetwork();
		return postHTML(url.printString(), paramsObject, xpath.printString(), network);
	}

	/**
	 * Post the HTML forms params and return the HTML data from the URL.
	 */
	public Vertex postHTML(String url, Vertex paramsObject, String xpath, Network network) {
		log("POST HTML", Level.INFO, url, xpath);
		try {
			Map<String, String> data = convertToMap(paramsObject);
			log("POST params", Level.FINE, data);
			String html = Utils.httpPOST(url, data);
			InputStream stream = new ByteArrayInputStream(html.getBytes("utf-8"));
			StringReader reader = convertToXHTML(stream);
			Element element = parseXHTML(reader);
			if (element == null) {
				return null;
			}
			XPathFactory factory = XPathFactory.newInstance();
			XPath path = factory.newXPath();
			Object node = path.evaluate(xpath, element, XPathConstants.NODE);
			if (node instanceof Element) {
				return convertElement((Element)node, network);
			} else if (node instanceof Attr) {
				return network.createVertex(((Attr)node).getValue());
			} else if (node instanceof org.w3c.dom.Text) {
				return network.createVertex(((org.w3c.dom.Text)node).getTextContent());
			}
			return null;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Self API.
	 * Post the XML document object and return the XML data from the URL.
	 */
	public Vertex postXML(Vertex source, Vertex url, Vertex xmlObject, Vertex xpath) {
		Network network = source.getNetwork();
		return postXML(url.printString(), xmlObject, xpath.printString(), network);
	}

	/**
	 * Self API.
	 * Post the XML document object and return the XML data from the URL.
	 */
	public Vertex postXMLAuth(Vertex source, Vertex url, Vertex user, Vertex password, Vertex xmlObject, Vertex xpath) {
		Network network = source.getNetwork();
		return postXMLAuth(url.printString(), user.printString(), password.printString(), xmlObject, xpath.printString(), network);
	}

	/**
	 * Self API.
	 * Post the XML document object and return the XML data from the URL.
	 */
	public Vertex postXMLAuth(Vertex source, Vertex url, Vertex user, Vertex password, Vertex agent, Vertex xmlObject, Vertex xpath) {
		Network network = source.getNetwork();
		return postXMLAuth(url.printString(), user.printString(), password.printString(), agent.printString(), xmlObject, xpath.printString(), network);
	}

	/**
	 * Self API.
	 * Convert the object to a JSON string.
	 */
	public Vertex toJSON(Vertex source, Vertex jsonObject) {
		try {
			Network network = source.getNetwork();
			String data = convertToJSON(jsonObject);
			return network.createVertex(data);
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Self API.
	 * Convert the object to an XML string.
	 */
	public Vertex toXML(Vertex source, Vertex xmlObject) {
		try {
			Network network = source.getNetwork();
			String data = convertToXML(xmlObject);
			return network.createVertex(data);
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Self API.
	 * URL encode the string.
	 */
	public Vertex encode(Vertex source, Vertex text) {
		return text.getNetwork().createVertex(org.botlibre.util.Utils.encodeURL(text.printString()));
	}

	/**
	 * Post the XML document object and return the XML data from the URL.
	 */
	public Vertex postXML(String url, Vertex xmlObject, String xpath, Network network) {
		log("POST XML", Level.INFO, url, xpath);
		try {
			String data = convertToXML(xmlObject);
			log("POST XML", Level.FINE, data);
			String xml = Utils.httpPOST(url, "application/xml", data);
			log("XML", Level.FINE, xml);
			InputStream stream = new ByteArrayInputStream(xml.getBytes("utf-8"));
			Element element = parseXML(stream);
			if (element == null) {
				return null;
			}
			XPathFactory factory = XPathFactory.newInstance();
			XPath path = factory.newXPath();
			Object node = path.evaluate(xpath, element, XPathConstants.NODE);
			if (node instanceof Element) {
				return convertElement((Element)node, network);
			} else if (node instanceof Attr) {
				return network.createVertex(((Attr)node).getValue());
			} else if (node instanceof org.w3c.dom.Text) {
				return network.createVertex(((org.w3c.dom.Text)node).getTextContent());
			}
			return null;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	public String convertToXML(Vertex object) {
		if (object.hasData()) {
			return object.printString();
		}
		try {
			StringWriter writer = new StringWriter();
			Vertex root = object.getRelationship(Primitive.ROOT);
			String elementName = "root";
			if (root != null) {
				elementName = root.printString();
			}
			convertToXML(object, elementName, writer, 0);
			return writer.toString();
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	public String convertToJSON(Vertex object) {
		try {
			StringWriter writer = new StringWriter();
			convertToJSON(object, writer, 0);
			return writer.toString();
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}
	
	public static String printDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		StringWriter writer = new StringWriter();
		writer.write(String.valueOf(calendar.get(Calendar.YEAR)));
		writer.write("-");
		writer.write(String.valueOf(calendar.get(Calendar.MONTH) + 1));
		writer.write("-");
		writer.write(String.valueOf(calendar.get(Calendar.DATE)));
		writer.write("T");
		writer.write(String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)));
		writer.write(":");
		writer.write(String.valueOf(calendar.get(Calendar.MINUTE)));
		writer.write(":");
		writer.write(String.valueOf(calendar.get(Calendar.SECOND)));
		long offset = calendar.getTimeZone().getOffset(calendar.getTimeInMillis());
		int offsetHours = (int)(offset / Utils.HOUR);
		int offsetMinutes = Math.abs((int)(offset / Utils.MINUTE) - (offsetHours * 60));
		if (offsetHours > 0) {
			writer.write("+");
		} else {
			writer.write("-");
		}
		if (offsetHours < 10) {
			writer.write("0");
		}
		writer.write(String.valueOf(Math.abs(offsetHours)));
		writer.write(":");
		if (offsetMinutes < 10) {
			writer.write("0");
		}
		writer.write(String.valueOf(offsetMinutes));
		return writer.toString();
	}

	public void convertToJSON(Vertex object, Writer writer, int depth) throws Exception {
		if (depth > 100) {
			throw new BotException("Max JSON size exceeded");
		}
		if (object.hasData()) {
			if (object.getData() instanceof Number) {
				// Use JSON format.
				writer.write(String.valueOf(object.getData()));
			} else if (object.getData() instanceof Boolean) {
				// Use JSON format.
				writer.write(String.valueOf(object.getData()));
			} else if (object.is(Primitive.NULL)) {
				// Use JSON format.
				writer.write("null");
				return;
			} else if (object.is(Primitive.TRUE)) {
				// Use JSON format.
				writer.write("true");
				return;
			} else if (object.is(Primitive.FALSE)) {
				// Use JSON format.
				writer.write("false");
				return;
			} else {
				writer.write("\"");
				if (object.getData() instanceof Timestamp) {
					// Use JSON format.
					writer.write(printDate((Timestamp)object.getData()));
				} else {
					writer.write(object.printString());
				}
				writer.write("\"");
			}
			return;
		} else {
			boolean first = true;
			if (object.isArray()) {
				writer.write("[");
				List<Relationship> elements = object.orderedRelationships(Primitive.ELEMENT);
				if (elements != null) { 
					for (Relationship relationship : elements) {
						if (first) {
							first = false;
						} else {
							writer.write(", ");
						}
						convertToJSON(relationship.getTarget(), writer, depth++);
					}
				}
				writer.write("]");
			} else {
				writer.write("{");
				for (Iterator<Relationship> iterator = object.orderedAllRelationships(); iterator.hasNext(); ) {
					Relationship relationship = iterator.next();
					if (relationship.isInverse()) {
						continue;
					}
					String name = relationship.getType().getDataValue();
					if (!name.equals("instantiation")) {
						if (first) {
							first = false;
						} else {
							writer.write(", ");
						}
						writer.write("\"");
						writer.write(name);
						writer.write("\":");
						convertToJSON(relationship.getTarget(), writer, depth++);
					}
				}
				writer.write("}");
			}
		}
	}

	public void convertToXML(Vertex object, String elementName, Writer writer, int depth) throws Exception {
		if (depth > 100) {
			throw new BotException("Max XML size exceeded");
		}
		writer.write("<");
		writer.write(elementName);

		for (Iterator<Relationship> iterator = object.orderedAllRelationships(); iterator.hasNext(); ) {
			Relationship relationship = iterator.next();
			if (relationship.isInverse()) {
				continue;
			}
			String name = relationship.getType().getDataValue();
			if (name.startsWith("@")) {
				writer.write(" ");
				writer.write(name.substring(1, name.length()));
				writer.write("=\"");
				writer.write(relationship.getTarget().printString());
				writer.write("\"");
			}
		}
		writer.write(">");
		if (object.hasData()) {
			writer.write(object.printString());
		} else {
			for (Iterator<Relationship> iterator = object.orderedAllRelationships(); iterator.hasNext(); ) {
				Relationship relationship = iterator.next();
				if (relationship.isInverse()) {
					continue;
				}
				String name = relationship.getType().getDataValue();
				if (!name.startsWith("@") && !name.equals("root") && !name.equals("instantiation")) {
					convertToXML(relationship.getTarget(), name, writer, depth++);
				}
			}
		}
		
		writer.write("</");
		writer.write(elementName);
		writer.write(">");
	}

	public Map<String, String> convertToMap(Vertex object) {
		Map<String, String> map = new HashMap<String, String>();
		for (Iterator<Relationship> iterator = object.orderedAllRelationships(); iterator.hasNext(); ) {
			Relationship relationship = iterator.next();
			if (relationship.isInverse()) {
				continue;
			}
			String name = relationship.getType().getDataValue();
			if (!name.equals("instantiation")) {
				map.put(name, relationship.getTarget().printString());
			}
			
		}
		return map;
	}
	
	public Vertex convertElement(Element element, Network network) {
		try {
			if (element == null) {
				return null;
			}
			NamedNodeMap attributes = element.getAttributes();
			NodeList list = element.getChildNodes();
			if (attributes.getLength() == 0 && list.getLength() == 0) {
				return network.createVertex("");
			}
			if (list.getLength() == 1) {
				Node child = list.item(0);
				if (child.getNodeType() == Node.TEXT_NODE) {
					return network.createVertex(child.getNodeValue());
				}
			}
			Vertex root = network.createVertex();
			for (int index = 0; index < attributes.getLength(); index++) {
				Node attribute = attributes.item(index);
				Primitive key = new Primitive(attribute.getNodeName());
				Vertex value = network.createVertex(attribute.getNodeValue());
				root.addRelationship(key, value);
			}
			for (int index = 0; index < list.getLength(); index++) {
				Node child = list.item(index);
				String name = child.getNodeName();
				Primitive key = new Primitive(name);
				Vertex value = null;
				if (child.getNodeType() == Node.TEXT_NODE) {
					value = network.createVertex(child.getNodeValue());
				} else if (child instanceof Element) {
					value = convertElement((Element)child, network);
				} else if (child instanceof CDATASection) {
					value = network.createVertex(((CDATASection)child).getNodeValue());
				}
				if (value != null) {
					root.addRelationship(key, value);
				}
			}
			return root;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	public Vertex convertElement(Object json, Network network) {
		try {
			if (json == null) {
				return null;
			}
			Vertex object = null;
			if (json instanceof JSONObject) {
				object = network.createVertex();
				for (Iterator iterator = ((JSONObject)json).keys(); iterator.hasNext(); ) {
					String name = (String)iterator.next();
					Object value = ((JSONObject)json).get(name);
					if (value == null) {
						continue;
					}
					Primitive key = new Primitive(name);
					Vertex target = convertElement(value, network);
					object.addRelationship(key, target);
				}
			} else if (json instanceof JSONArray) {
				object = network.createInstance(Primitive.ARRAY);
				JSONArray array = (JSONArray)json;
				for (int index = 0; index < array.size(); index++) {
					Vertex element = convertElement(array.get(index), network);
					object.addRelationship(Primitive.ELEMENT, element, index);
				}
			} else if (json instanceof JSONNull) {
				object = network.createInstance(Primitive.NULL);
			} else if (json instanceof String) {
				object = network.createVertex(json);
			} else if (json instanceof Number) {
				object = network.createVertex(json);
			} else if (json instanceof Date) {
				object = network.createVertex(json);
			} else if (json instanceof Boolean) {
				object = network.createVertex(json);
			} else {
				log("Unknown JSON object", Level.INFO, json);
			}
			return object;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}
	
	/**
	 * Parse the XML as a DOM.
	 */
	public Element parseXMLURL(URL url) {
		try {
			InputStream stream = Utils.openStream(url);
			return parseXML(stream);
		} catch (FileNotFoundException notFound) {
			log(notFound.toString(), Level.WARNING);
			return null;
		} catch (Exception ioException) {
			if (getBot().isDebugFine()) {
				log(ioException);
			} else {
				log(ioException.toString(), Bot.WARNING, url);
			}
			return null;
		}
	}

	/**
	 * Self API.
	 * Return the top RSS feed.
	 */
	public Vertex rss(Vertex source, Vertex url) {
		log("RSS", Level.INFO, url);
		try {
			Network network = source.getNetwork();
			List<Map<String, Object>> result = parseRSSFeed(new URL(url.printString()), 0);
			if (result == null) {
				return null;
			}
			for (Map<String, Object> element : result) {
				Vertex rss = network.createInstance(Primitive.RSS);
				for (Entry<String, Object> entry : element.entrySet()) {
					rss.addRelationship(new Primitive(entry.getKey()), network.createVertex(entry.getValue()));
				}
				return rss;
			}
			return null;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Self API.
	 * Return the entire RSS feed.
	 */
	public Vertex rssFeed(Vertex source, Vertex url) {
		log("RSS feed", Level.INFO, url);
		try {
			Network network = source.getNetwork();
			List<Map<String, Object>> result = parseRSSFeed(new URL(url.printString()), 0);
			Vertex list = network.createInstance(Primitive.ARRAY);
			int index = 0;
			for (Map<String, Object> element : result) {
				Vertex rss = network.createInstance(Primitive.RSS);
				for (Entry<String, Object> entry : element.entrySet()) {
					rss.addRelationship(new Primitive(entry.getKey()), network.createVertex(entry.getValue()));
				}
				list.addRelationship(Primitive.ELEMENT, rss, index);
				index++;
			}
			return list;
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Parse RSS feed.
	 */
	public List<Map<String, Object>> parseRSSFeed(URL url, long fromTime) {
		try {
			Element root = parseXMLURL(url);
			List<Map<String, Object>> feed = new ArrayList<Map<String, Object>>();
			if (root == null) {
				return null;
			}
			NodeList list = root.getElementsByTagName("entry");
			// There are several RSS feed formats.
			if ((list != null) && (list.getLength() > 0)) {
				// Blogger feed.
				for (int index = 0; index < list.getLength(); index++) {
					Element entry = (Element)list.item(index);
					Map<String, Object> map = new HashMap<String, Object>(4);
					NodeList children = entry.getElementsByTagName("published");
					if ((children != null) && (children.getLength() > 0)) {
						String date = children.item(0).getTextContent();
						long time = System.currentTimeMillis();
						try {
							time = Utils.parseDate(date, "yyyy-MM-dd'T'HH:mm:ss.SSS").getTimeInMillis();
						} catch (Exception exception) {
							try {
								time = Utils.parseDate(date, "yyyy-MM-dd'T'HH:mm:ssX").getTimeInMillis();
							} catch (Exception exception2) {
								try {
									time = Utils.parseDate(date, "EEE, dd MMM yyyy HH:mm:ss X").getTimeInMillis();
								} catch (Exception exception3) {
									try {
										time = Utils.parseDate(date, "EEE, dd MMM yyyy").getTimeInMillis();
									} catch (Exception exception4) {
										log(exception);
									}
								}
							}
						}
				    	if (time <= fromTime) {
				    		break;
				    	}
						map.put("published", time);
					} else {
						continue;
					}
					children = entry.getElementsByTagName("title");
					if ((children != null) && (children.getLength() > 0)) {
						map.put("title", children.item(0).getTextContent());
					} else {
						continue;
					}
					children = entry.getElementsByTagName("content");
					if ((children != null) && (children.getLength() > 0)) {
						map.put("content", children.item(0).getTextContent());
					} else {
						continue;
					}
					NodeList links = entry.getElementsByTagName("link");
					for (int index2 = 0; index2 < links.getLength(); index2++) {
						Element link = (Element)links.item(index2);
						String rel = link.getAttribute("rel");
						if ((rel != null) && rel.equals("alternate")) {
							map.put("link", link.getAttribute("href"));							
						}						
					}
					feed.add(map);
				}
			} else {
				list = root.getElementsByTagName("channel");
				if ((list != null) && (list.getLength() > 0)) {
					list = ((Element)list.item(0)).getElementsByTagName("item");
					if ((list != null) && (list.getLength() > 0)) {
						// Standard feed.
						for (int index = 0; index < list.getLength(); index++) {
							Element entry = (Element)list.item(index);
							Map<String, Object> map = new HashMap<String, Object>(3);
							NodeList children = entry.getElementsByTagName("pubDate");
							if ((children != null) && (children.getLength() > 0)) {
								String date = children.item(0).getTextContent();
								long time = System.currentTimeMillis();
								try {
									time = Utils.parseDate(date, "yyyy-MM-dd'T'HH:mm:ss.SSS").getTimeInMillis();
								} catch (Exception exception) {
									try {
										time = Utils.parseDate(date, "yyyy-MM-dd'T'HH:mm:ssX").getTimeInMillis();
									} catch (Exception exception2) {
										try {
											time = Utils.parseDate(date, "EEE, dd MMM yyyy HH:mm:ss X").getTimeInMillis();
										} catch (Exception exception3) {
											try {
												time = Utils.parseDate(date, "EEE, dd MMM yyyy").getTimeInMillis();
											} catch (Exception exception4) {
												log(exception);
											}
										}
									}
								}
						    	if (time <= fromTime) {
						    		break;
						    	}
								map.put("published", time);
							} else {
								continue;
							}
							children = entry.getElementsByTagName("title");
							if ((children != null) && (children.getLength() > 0)) {
								map.put("title", children.item(0).getTextContent());
							} else {
								continue;
							}
							children = entry.getElementsByTagName("link");
							if ((children != null) && (children.getLength() > 0)) {
								map.put("link", children.item(0).getTextContent());
							} else {
								continue;
							}
							feed.add(map);
						}
					}
				}
			}
			return feed;
		} catch (Exception ioException) {
			log(ioException.getMessage(), Level.WARNING, url);
			return null;
		}
	}
	
	/**
	 * Process the list of URLs as a batch using multi threading.
	 */
	public void input(Collection<URL> input) {
		Queue<URL> urls = new ConcurrentLinkedQueue<URL>();
		urls.addAll(input);
		WorkerThread worker = new WorkerThread();
		worker.urls = urls;
		Thread threads[] = new Thread[WORKER_THREADS];
		// Process a couple to avoid contention on common data.
		Network memory = getBot().memory().newMemory();
		URL url = urls.poll();
		batchProcessURL(url, memory);
		url = urls.poll();
		batchProcessURL(url, memory);
		for (int index = 0; index < WORKER_THREADS; index++) {
			threads[index] = new Thread(worker);
			threads[index].start();
		}
		boolean alive = true;
		while (alive) {
			// Check if all of the workers are done.
			for (int index = 0; index < WORKER_THREADS; index++) {
				if (threads[index].isAlive()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException exception) {
						log(exception);
					}
					break;
				} else if (index == (WORKER_THREADS - 1)) {
					alive = false;
				}
			}
		}
	}

	/**
	 * Process the XHTML DOM.
	 * This should extract the useful context from the page.
	 * This should normally be overridden by a subclass to process a specific type of page, i.e. Wikipedia entry.
	 */
	public void processRoot(Node node, URL url, Network network) {
		Vertex vertex = createURL(url, network);
		processHeaders(node, vertex, network);
	}
	
	/**
	 * Process the header nodes and associate their topics with the URL.
	 */
	public void processHeaders(Node node, Vertex url, Network network) {
		// Find the main header, and associate the url with it.
		Set<String> headers = new HashSet<String>(3);
		headers.add("h1");
		headers.add("h2");
		headers.add("h3");
		headers.add("h4");
		Node header = findTag(headers, null, node);
		Vertex urlType = network.createVertex(Primitive.URL);
		Vertex topicType = network.createVertex(Primitive.TOPIC);
		Vertex instantiationType = network.createVertex(Primitive.INSTANTIATION);
		Vertex sentenceType = network.createVertex(Primitive.SENTENCE);
		Vertex contentType = network.createVertex(Primitive.CONTENT);
		Vertex h1 = null;
		Vertex h2 = null;
		while (header != null) {
			log("Header", Bot.FINE, header);
			Vertex topic = network.createVertex();
			topic.addRelationship(instantiationType, topicType);
			Vertex sentence = getSentence(header, network);
			topic.setName(sentence.getDataValue());
			log("Topic", Bot.FINE, sentence);
			topic.addRelationship(sentenceType, sentence);
			sentence.addRelationship(topicType, topic);
			if (header.getNodeName().equals("h1")) {
				topic.addRelationship(urlType, url);
				url.addRelationship(topicType, topic);
				h1 = topic;
			} else if (header.getNodeName().equals("h2")) {
				if (h1 != null) {
					h1.addRelationship(contentType, topic);
					topic.addRelationship(topicType, h1);
				}
				h2 = topic;
			} else if (header.getNodeName().equals("h3")) {
				if (h2 != null) {
					h2.addRelationship(contentType, topic);
					topic.addRelationship(topicType, h2);
				} else if (h1 != null) {
					h1.addRelationship(contentType, topic);
					topic.addRelationship(topicType, h1);
				}
			}
			// Need to walk back up to parent if no more siblings.
			header = findNextTag(headers, null, header, node);
		}
		network.save();
		getBot().memory().addActiveMemory(url);
	}
	
	/**
	 * Return the next sibling or parent sibling node.
	 * Only walk up to root at most.
	 */
	public Node nextNode(Node node, Node root) {
		if (node == null) {
			return null;
		}
		// Need to walk back up to parent if no more siblings.
		Node nextNode = node.getNextSibling();
		Node parent = node.getParentNode();
		while ((nextNode == null) && (parent != null)) {
			if (parent == root) {
				return null;
			}
			nextNode = parent.getNextSibling();
			parent = parent.getParentNode();
		}
		return nextNode;
	}
	
	/**
	 * Find the next node for the tag, search children, siblings and cousins.
	 * Only walk up to root at most.
	 */
	public Node findNextTag(Set<String> tags, String value, Node node, Node root) {
		Node header = node;
		Node nextNode = nextNode(header, root);
		header = findTag(tags, null, nextNode);
		while ((header == null) && (nextNode != null)) {	
			nextNode = nextNode(nextNode, root);
			header = findTag(tags, null, nextNode);
		}
		return header;
	}
	
	/**
	 * Find the next node for the tag.
	 */
	public Node findTag(String tag, Node node) {
		return findTag(tag, null, node);
	}
	
	/**
	 * Find the next node for the tag.
	 */
	public Node findTag(String tag, String value, Node node) {
		Set<String> tags = new HashSet<String>(1);
		tags.add(tag);
		return findTag(tags, value, node);
	}
	
	/**
	 * Find the next node for any of the tags whose text contains the value.
	 */
	public Node findTag(Set<String> tags, String value, Node node) {
		if (node == null) {
			return null;
		}
		Node nextNode = node;
		while (!tags.contains(nextNode.getNodeName())
					|| ((value != null) && (nextNode.getTextContent().indexOf(value) == -1))) {
			NodeList nodes = nextNode.getChildNodes();
			// Only need to process first child, as it will process siblings.
			if (nodes.getLength() > 0) {
				Node child = findTag(tags, value, nodes.item(0));
				if (child != null) {
					return child;
				}
			}
			nextNode = nextNode.getNextSibling();
			if (nextNode == null) {
				return null;
			}
		}
		return nextNode;
	}

	/**
	 * Return the complete node text.
	 */
	public String getText(Node node) {
		String text = "";
		NodeList nodes = node.getChildNodes();
		for (int index = 0; index < nodes.getLength(); index++) {
			Node child = nodes.item(index);
			text = text + child.getTextContent();
		}
		return text.trim();
	}

	/**
	 * Return a sentence of all the words, or a word is a single word.
	 */
	public Vertex getSentence(Node node, Network network) {
		String text = getText(node);
		return network.createSentence(text);
	}
	
	public String stripBrackets(String text) {
		TextStream stream = new TextStream(text);
		text = stream.upTo('(').trim();
		while (text.isEmpty() && !stream.atEnd()) {
			stream.skipTo(')');
			if (stream.peek() == ':') {
				stream.skip();
			}
			text = stream.upTo('(').trim();
		}
		return text;
	}

	/**
	 * Parse the text values from the next paragrpah.
	 */
	public String getNextParagraph(Node node) {
		Node p = findTag("p", node);
		if (p == null) {
			return "";
		}
		return p.getTextContent();
	}

	/**
	 * Parse the text values from the next bullet list.
	 */
	public List<String> getNextBulletList(Node node) {
		List<String> words = new ArrayList<String>();
		Node ul = findTag("ul", node);
		if (ul != null) {
			NodeList nodes = ul.getChildNodes();
			for (int index = 0; index < nodes.getLength(); index++) {
				Node child = nodes.item(index);
				if (child.getNodeName().equals("li")) {
					String text = child.getTextContent().trim();
					words.add(text);
				}
			}
		}
		return words;
	}

	/**
	 * Parse the text values from the next numbered list.
	 */
	public List<String> getNextNumberedList(Node node) {
		List<String> words = new ArrayList<String>();
		Node ul = findTag("ol", node);
		if (ul != null) {
			NodeList nodes = ul.getChildNodes();
			for (int index = 0; index < nodes.getLength(); index++) {
				Node child = nodes.item(index);
				if (child.getNodeName().equals("li")) {
					String text = child.getTextContent().trim();
					words.add(text);
				}
			}
		}
		return words;
	}

	/**
	 * Parse the text values from the next bullet list.
	 */
	public List<String> getAllBullets(Node node) {
		List<String> words = new ArrayList<String>();
		Set<String> tags = new HashSet<String>(1);
		tags.add("ul");
		Node ul = findNextTag(tags, null, node, node.getParentNode());
		while (ul != null) {
			NodeList nodes = ul.getChildNodes();
			for (int index = 0; index < nodes.getLength(); index++) {
				Node child = nodes.item(index);
				String text = child.getTextContent().trim();
				words.add(text);
			}
			ul = findNextTag(tags, null, ul, node.getParentNode());
		}
		return words;
	}

	/**
	 * Parse the text values from the next bullet list.
	 */
	public List<String> getAllURLBullets(Node node) {
		List<String> urls = new ArrayList<String>();
		Set<String> tags = new HashSet<String>(1);
		tags.add("ul");
		Node ul = findNextTag(tags, null, node, node.getParentNode());
		while (ul != null) {
			NodeList nodes = ul.getChildNodes();
			for (int index = 0; index < nodes.getLength(); index++) {
				Node child = nodes.item(index);
				NodeList chilren = child.getChildNodes();
				for (int childIndex = 0; childIndex < chilren.getLength(); childIndex++) {
					Node url = chilren.item(childIndex);
					if (url.getNodeName().equals("a")) {
						urls.add(url.getAttributes().getNamedItem("href").getTextContent());
					}
				}
			}
			ul = findNextTag(tags, null, ul, node.getParentNode());
		}
		return urls;
	}

	/**
	 * Create the URL vertex.
	 */	
	protected Vertex createURL(URL url, Network network) {
		try {
			Vertex vertex = network.createVertex(url.toURI());
			vertex.addRelationship(Primitive.INSTANTIATION, Primitive.URL);
			return vertex;
		} catch (URISyntaxException exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Post, process the post request.
	 */
	@Override
	public void output(Vertex output) {
		
	}

	/**
	 * Return the map of registered domain processing senses,
	 * keyed by their URL domains they accept.
	 */
	public Map<String, Http> getDomains() {
		return domains;
	}
	
}
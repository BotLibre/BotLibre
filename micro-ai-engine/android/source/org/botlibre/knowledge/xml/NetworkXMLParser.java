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
package org.botlibre.knowledge.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.BasicNetwork;
import org.botlibre.knowledge.BasicVertex;
import org.botlibre.knowledge.Primitive;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Utility class that converts a network to add from xml.
 * The xml schema is defined in network.xsd.
 * A basic network and vertices are used for instantiation from xml.
 */
 
public class NetworkXMLParser 
{	
	// toDo: use xsi:type for data
	private static NetworkXMLParser instance;
	
	public static NetworkXMLParser instance() {
		if (instance == null) {
			instance = new NetworkXMLParser();
		}
		return instance;
	}
	
	/**
	 * Parse a new network from an xml file.
	 * This makes use of the xerces parser.
	 */
	public Network parse(File file) {
		try {
			return parse(new URL("file:///" + file.getAbsolutePath()));
		} catch (Exception exception) {
			throw new BotException("Parsing error while parsing network xml file.", exception);
		}
	}
	
	/**
	 * Parse a new network from an xml uri.
	 * This makes use of the xerces parser.
	 */
	@SuppressWarnings("unchecked")
	public Network parse(URL uri) {
		try {		
			// Parse xml document with JAXP.
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			Document document = parser.parse(uri.toString());
			Element root = document.getDocumentElement();
			BasicNetwork network = new BasicNetwork();
			SimpleDateFormat formater = new SimpleDateFormat();

			// Parse all vertices and index by id.
			NodeList vertexElements = ((Element) root.getElementsByTagName("vertices").item(0)).getElementsByTagName("vertex");
			Map<Number, Vertex> verticiesById = new HashMap<Number, Vertex>();
			for (int index = 0; index < vertexElements.getLength(); index++) {
				Element vertexElement = (Element) vertexElements.item(index);
				BasicVertex vertex = new BasicVertex();
				Long id = new Long(vertexElement.getAttribute("id"));
				vertex.setId(id);
				NodeList creationDateElements = vertexElement.getElementsByTagName("creation-date");
				if (creationDateElements != null && creationDateElements.getLength() > 0) {
					String date = (String) creationDateElements.item(0).getFirstChild().getNodeValue();					
					vertex.setCreationDate(formater.parse(date));
				} else { // Missing creation date, set to current date. (importing old files)
					vertex.setCreationDate(new Date());
				}
				NodeList accessDateElements = vertexElement.getElementsByTagName("access-date");
				if (accessDateElements != null && accessDateElements.getLength() > 0) {
					String date = (String) accessDateElements.item(0).getFirstChild().getNodeValue();					
					vertex.setAccessDate(formater.parse(date));
				} else { // Missing access date, set to current date. (importing old files)
					vertex.setAccessDate(new Date());
				}
				String count = vertexElement.getAttribute("access-count");
				if (count != "") {
					vertex.setAccessCount(Integer.parseInt(count));
				} else { // Missing count. (importing old files)
					vertex.setAccessCount(1);
				}
				NodeList dataElements = vertexElement.getElementsByTagName("data");
				if (dataElements != null && dataElements.getLength() > 0) {
					Element dataNode = (Element)dataElements.item(0);
					String type = dataNode.getAttribute("type");
					String value = dataNode.getTextContent();
					Object data = null;
					if (type.equals("Primitive")) {
						data = new Primitive(value);
					} else if (type.equals("String")) {
						data = value;
					} else if (type.equals("Date")) {
						data = Calendar.getInstance();
						((Calendar)data).setTime(formater.parse(value));
					} else {
						try {
							Class<Object> typeClass = (Class<Object>)Class.forName(type);							
							data = typeClass.getConstructor(String.class).newInstance(value);
						} catch (Exception error) {
							System.out.println(error);
							data = value;
						}
					}
					vertex.setData(data);
				}
				// Check for existing vertex in case of corrupted file.
				Vertex existingVertex = network.findByData(vertex.getData());
				if (existingVertex == null) {
					network.addVertex(vertex);
					verticiesById.put(id, vertex);
				} else {
					verticiesById.put(id, existingVertex);
				}
			}
			
			// Parse all relations and add to source vertex.
			NodeList relationElements = ((Element) root.getElementsByTagName("relations").item(0)).getElementsByTagName("relationship");
			for (int index = 0; index < relationElements.getLength(); index++) {
			  Element relationElement = (Element) relationElements.item(index);
			  Long sourceId = new Long(relationElement.getAttribute("source-id"));
			  Long targetId = new Long(relationElement.getAttribute("target-id"));
			  Long typeId = new Long(relationElement.getAttribute("type-id"));
			  Float correctness = 0.5f;
			  try {
				  correctness = new Float(relationElement.getAttribute("correctness"));
			  } catch (Exception backwardCompatibility) {
				  // Ignore.
			  }
			  Vertex source = (Vertex) verticiesById.get(sourceId);
			  Vertex target = (Vertex) verticiesById.get(targetId);
			  Vertex type = (Vertex) verticiesById.get(typeId);
			  Relationship relationship = source.addRelationship(type, target);
			  relationship.setCorrectness(correctness);
			}

			return network;
		} catch (Exception exception) {
			throw new BotException("Parsing error while parsing network xml file.", exception);
		}
	}

	/**
	 * Covert the network into xml.
	 */
	public String toXML(Network network) {
		StringWriter writer = new StringWriter();
		writeXML(network, writer);
		writer.flush();
		return writer.toString();
	}
	
	/**
	 * Covert the network into xml and write to the file.
	 */
	public void toXML(Network network, File file) {
		Writer writer = null;
		try {
			writer = new FileWriter(file);
			writeXML(network, writer);
			writer.flush();
		} catch (Exception exception) {
			throw new BotException("IO error while writing a network to xml.", exception);
		} finally {
			try {
				writer.close();
			} catch (Exception ignore) {}
		}
	}
	
	/**
	 * Covert the network into xml using the writer.
	 */
	public void writeXML(Network network, Writer writer) {
		try {
			// Write xml document with JAXP.
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			Document document = parser.newDocument();
			Element root = document.createElement("network");
			document.appendChild(root);
			Element verticesNode = document.createElement("vertices");
			root.appendChild(verticesNode);			
			// Vertices.
			SimpleDateFormat formater = new SimpleDateFormat();
			for (Vertex vertex : network.findAll()) {
				Element vertexNode = document.createElement("vertex");
				verticesNode.appendChild(vertexNode);
				vertexNode.setAttribute("id", vertex.getId().toString());
				Element creationDate = document.createElement("creation-date");
				creationDate.setTextContent(formater.format(vertex.getCreationDate().getTime()));
				vertexNode.appendChild(creationDate);
				Element accessDate = document.createElement("access-date");
				accessDate.setTextContent(formater.format(vertex.getAccessDate().getTime()));
				vertexNode.appendChild(accessDate);
				vertexNode.setAttribute("access-count", String.valueOf(vertex.getAccessCount()));
				if (vertex.hasData()) {
					Element dataNode = document.createElement("data");
					vertexNode.appendChild(dataNode);
					// Type
					Object data = vertex.getData();
					String type = "";
					if (data instanceof String) {
						type = "String";
					} else if (data instanceof Primitive) {
						type = "Primitive";
					} else if (data instanceof Calendar) {
						type = "Date";
					} else {
						type = vertex.getData().getClass().getName();
					}
					dataNode.setAttribute("type", type);
					// Value
					String value = "";
					if (data instanceof String) {
						value = (String) data;
					} else if (data instanceof Primitive) {
						value = ((Primitive) data).getIdentity();
					} else if (data instanceof Calendar) {
						value = formater.format(((Calendar)data).getTime());
					} else {
						value = data.toString();
					}
					dataNode.setTextContent(new String(value.getBytes("UTF8")));
				}
			}
			// Relations.
			Element relationsNode = document.createElement("relations");
			root.appendChild(relationsNode);			
			for (Vertex vertex : network.findAll()) {
				for (Iterator<Relationship> relations = vertex.orderedAllRelationships(); relations.hasNext(); ) {
					Relationship relationship = (Relationship) relations.next();
					Element relationshipNode = document.createElement("relationship");
					relationsNode.appendChild(relationshipNode);
					relationshipNode.setAttribute("source-id", relationship.getSource().getId().toString());
					relationshipNode.setAttribute("target-id", relationship.getTarget().getId().toString());
					relationshipNode.setAttribute("type-id", relationship.getType().getId().toString());
					relationshipNode.setAttribute("correctness", String.valueOf(relationship.getCorrectness()));
				}
			}
			// Transform to writer.
            Source source = new DOMSource(document);
            Result result = new StreamResult(writer);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(source, result);
            
		} catch (Exception exception) {
			throw new BotException("IO error while writing a network to xml.", exception);
		}
	}
}
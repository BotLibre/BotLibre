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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;
import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicSense;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
		return new StringReader(output.toString());
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
		Document document = getParser().parse(input);
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
								log(exception);
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
						    	long time = Utils.parseDate(date, "EEE, dd MMM yyyy HH:mm:ss zzz").getTimeInMillis();
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
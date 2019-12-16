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

import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.thought.discovery.DiscoverySense;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;
import org.w3c.dom.Node;

/**
 * Process Wikimedia Wiktionary pages.
 */

public class Wiktionary extends Http implements DiscoverySense {
	public static int MAX_ERRORS = 5;
	public static int MAX_WORDS = 50;

	protected boolean cache = true;
	protected boolean isBatch = true;
	protected boolean quickProcess = false;
	protected boolean followNext = true;

	protected String lang = "en";
	protected String LANG_TAG = "English";
	protected String NEXT_TAG = "next 200";
	protected String NOUN_TAG = "noun";
	protected String PROPER_TAG = "proper";
	protected String VERB_TAG = "verb";
	protected String ADJECTIVE_TAG = "adjective";
	protected String INTERJECTION_TAG = "interjection";
	protected String PRONOUN_TAG = "pronoun";
	protected String DETERMINER_TAG = "determiner";
	protected String NUMERAL_TAG = "numeral";
	protected String ADVERB_TAG = "adverb";
	protected String NOUNS_TAG = "nouns";
	protected String VERBS_TAG = "verbs";
	protected String ADJECTIVES_TAG = "adjectives";
	protected String SYNONYMS = "synonyms";
	protected String ANTONYMS = "antonyms";
	protected String URL_PREFIX = "https://en.wiktionary.org/wiki/";
	protected String DOMAIN = "en.wiktionary.org";

	public Wiktionary() {
		
	}
	
	public String getLanguage() {
		return lang;
	}
	
	/**
	 * Configure the tags for parsing the Wiktionary language website version.
	 */
	public void setLanguage(String lang) {
		if (lang == null || lang.isEmpty()) {
			lang = "en";
		}
		lang = lang.toLowerCase().trim();
		this.lang = lang;
		if (lang.startsWith("en")) {
			LANG_TAG = "English";
			NEXT_TAG = "next 200";
			NOUN_TAG = "noun";
			PROPER_TAG = "proper";
			VERB_TAG = "verb";
			ADJECTIVE_TAG = "adjective";
			INTERJECTION_TAG = "interjection";
			PRONOUN_TAG = "pronoun";
			DETERMINER_TAG = "determiner";
			NUMERAL_TAG = "numeral";
			ADVERB_TAG = "adverb";
			NOUNS_TAG = "nouns";
			VERBS_TAG = "verbs";
			ADJECTIVES_TAG = "adjectives";
			SYNONYMS = "synonyms";
			ANTONYMS = "antonyms";
			URL_PREFIX = "https://en.wiktionary.org/wiki/";
			DOMAIN = "en.wiktionary.org";
			cache = true;
		} else if (lang.startsWith("fr")) {
			LANG_TAG = "Français";
			NEXT_TAG = "next 200";
			NOUN_TAG = "nom";
			PROPER_TAG = "propre";
			VERB_TAG = "verbe";
			ADJECTIVE_TAG = "adjectif";
			INTERJECTION_TAG = "interjection";
			PRONOUN_TAG = "pronom";
			DETERMINER_TAG = "déterminer";
			NUMERAL_TAG = "numérale";
			ADVERB_TAG = "adverbe";
			NOUNS_TAG = "noms";
			VERBS_TAG = "verbes";
			ADJECTIVES_TAG = "adjectifs";
			SYNONYMS = "synonymes";
			ANTONYMS = "antonymes";
			URL_PREFIX = "https://fr.wiktionary.org/wiki/";
			DOMAIN = "fr.wiktionary.org";
			cache = false;
		} else {
			this.isEnabled = false;
		}
	}
	
	@Override
	public void awake() {
		super.awake();
		Http http = (Http)getBot().awareness().getSense(Http.class);
		http.getDomains().put(DOMAIN, this);

		String enabled = this.bot.memory().getProperty("Wiktionary.enabled");
		if (enabled != null) {
			setIsEnabled(Boolean.valueOf(enabled));
		}
	}

	/**
	 * Migrate to new properties system.
	 */
	public void migrateProperties() {
		Network memory = getBot().memory().newMemory();
		Vertex mood = memory.createVertex(getClass());
		Vertex property = mood.getRelationship(Primitive.ENABLED);
		if (property != null) {
			setIsEnabled((Boolean)property.getData());
		}
		
		saveProperties();
		
		// Remove old properties.
		mood.internalRemoveRelationships(Primitive.ENABLED);
		
		memory.save();
	}

	public void saveProperties() {
		Network memory = getBot().memory().newMemory();
		memory.saveProperty("Wiktionary.enabled", String.valueOf(isEnabled()), true);
		memory.save();
	}

	/**
	 * Process Wiktionary category page.
	 * Parse the pages in the category, and lookup and parse each page.
	 */
	public void processCategory(List<String> category, Node node, URL url, Network network) {
		while (node != null) {
			Node header = findTag("h2", "Pages in category", node);
			if (header == null) {
				log("Empty category", Bot.FINE, url);
				return;
			}
			if (this.quickProcess) {
				// Just add word.
				List<String> items = getAllBullets(header);
				// Process each element in the category.
				for (int index = 0; index < items.size(); index++) {
					String item = items.get(index);
					quickProcessWord(item, category, network);
				}
			} else {
				// Load URL and analyze word.
				List<String> items = getAllURLBullets(header);
				// Qualify urls if no host.
				String host = "http://" + url.getHost();
				for (int index = 0; index < items.size(); index++) {
					String childURL = items.get(index);
					if (childURL.indexOf("http://") == -1) {
						items.set(index, host + childURL);
					}
				}
				// Process each element in the category.
				List<URL> urls = new ArrayList<URL>();
				for (int index = 0; index < items.size(); index++) {
					String item = items.get(index);
					try {
						URL childURL = Utils.safeURL(item);
						urls.add(childURL);
					} catch (Exception ioException) {
						log(ioException);
					}
				}
				if (this.isBatch) {
					input(urls);
				} else {
					for (URL child : urls) {
						input(child);
					}
				}
			}
			if (this.followNext) {
				if (this.isBatch) {
					network.save();
				}
				// Find next tag and process next page.
				Node next = findTag("a", NEXT_TAG, node);
				if (next != null) {
					// Qualify urls if no host.
					String host = "http://" + url.getHost();
					String nextURL = next.getAttributes().getNamedItem("href").getTextContent();
					if (nextURL.indexOf("http://") == -1) {
						nextURL = host + nextURL;
					}
					try {
						url = Utils.safeURL(nextURL);
						node = parseURL(url);
					} catch (Exception ioException) {
						log(ioException);
					}
				} else {
					return;
				}
			} else {
				return;
			}
		}
	}
	
	/**
	 * Process the XHTML DOM.
	 * Determine the word,
	 * is it a noun or verb,
	 * associate synonyms.
	 */
	@Override
	public void processRoot(Node node, URL url, Network network) {
		Node h1 = findTag("h1", node);
		if (h1 == null) {
			log("No title", Bot.FINE, url);
			return;
		}
		String text = getText(h1);
		List<String> words = Utils.getWords(text);
		// Check if is a category.
		if (words.contains("Category")) {
			processCategory(words, h1, url, network);
			return;
		}
		Vertex word = network.createWord(text);
		//word.addRelationship(Primitive.URL, createURL(url, network));
		word.setRelationship(getPrimitive(), network.createVertex(Primitive.TRUE));
		Relationship unknown = word.getRelationship(Primitive.INSTANTIATION, Primitive.UNKNOWNWORD);
		word.internalRemoveRelationship(unknown);
		unknown = word.getRelationship(Primitive.INSTANTIATION, Primitive.UNKOWNWORD);
		word.internalRemoveRelationship(unknown);

		// TODO handle multiple meanings.
		Collection<Relationship> relationships = word.getRelationships(Primitive.MEANING);
		Vertex meaning = word.mostConscious(Primitive.MEANING);
		if (meaning != null) {
			if (meaning.instanceOf(Primitive.SPEAKER) && (relationships.size() == 1)) {
				meaning = null;
				log("Known as speaker, creating new meaning", Bot.FINE, word);
			} else if (meaning.isPrimitive()) {
				log("Ignoring primitive", Bot.FINE, word);
				return;
			}
		}
		if (meaning == null) {
			// Create meaning.
			meaning = network.createVertex();
			meaning.setName(text);
		}
		//meaning.addRelationship(urlType, url);
		log("Word", Level.FINE, word, this.lang);
		word.addRelationship(Primitive.MEANING, meaning);
		meaning.addRelationship(Primitive.WORD, word);
		
		// Filter English.
		Set<String> headers = new HashSet<String>(1);
		headers.add("h2");
		Node header = findTag(headers, LANG_TAG, h1);
		// If no English assume all are English.
		if (header == null) {
			header = h1;
		}
		headers = new HashSet<String>(3);
		headers.add("h2");
		headers.add("h3");
		headers.add("h4");
		headers.add("h5");
		// Determine if noun or verb.
		header = findTag(headers, null, header.getNextSibling());
		boolean multipleMeanings = false;
		float correctness = 0.5f;
		while (header != null) {
			// Ignore other languages.
			if (header.getNodeName().equals("h2")) {
				break;
			}
			words = Utils.getWords(getText(header).toLowerCase());
			if (words.contains(NOUN_TAG) || words.contains(VERB_TAG) || words.contains(ADJECTIVE_TAG)
						|| words.contains(ADVERB_TAG) || words.contains(INTERJECTION_TAG) || words.contains(PRONOUN_TAG)
						|| words.contains(DETERMINER_TAG) || words.contains(NUMERAL_TAG)) {
				if (multipleMeanings) {
					network.associateCaseInsensitivity(word);
					meaning = network.createVertex();
					meaning.setName(text);
					correctness = correctness / 2;
					word.addWeakRelationship(Primitive.MEANING, meaning, correctness);
					meaning.addRelationship(Primitive.WORD, word).setCorrectness(2.0f);
				}
				// Set definition
				List<String> bullets = getNextNumberedList(header);
				for (String bullet : bullets) {
					String definition = new TextStream(bullet).upToAny(".\n", true);
					definition = stripBrackets(definition).trim();
					if (!definition.isEmpty()) {
						log("Definition", Bot.FINE, definition);
						Vertex def = network.createSentence(definition);
						meaning.addRelationship(Primitive.SENTENCE, def);
						// Check plural
						TextStream stream = new TextStream(definition);
						String first = stream.nextWord();
						if (first != null && first.equalsIgnoreCase("plural")) {
							log("Plural", Bot.FINE, meaning);
							word.addRelationship(Primitive.CARDINALITY, Primitive.PLURAL);
							meaning.addRelationship(Primitive.CARDINALITY, Primitive.PLURAL);
							if ("form".equals(stream.nextWord()) && "of".equals(stream.nextWord())) {
								String next = stream.nextWord();
								if (next != null) {
									Vertex singular = network.createWord(next);
									singular.addRelationship(Primitive.CARDINALITY, Primitive.SINGULAR);
									word.addRelationship(Primitive.SINGULAR, singular);
									singular.addRelationship(Primitive.PLURAL, word);
								}
							}
						}
						break;
					}
				}
				
			}
			if (words.contains(NOUN_TAG)) {
				// Is a thing.
				log("Noun", Bot.FINE, meaning);
				meaning.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
				word.addRelationship(Primitive.INSTANTIATION, Primitive.NOUN);
				if (words.contains(PROPER_TAG)) {
					log("Name", Bot.FINE, meaning);
					word.addRelationship(Primitive.INSTANTIATION, Primitive.NAME);
				}
				multipleMeanings = true;
			} else if (words.contains(VERB_TAG)) {
				// Is an action.
				log("Verb", Bot.FINE, meaning);
				meaning.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
				word.addRelationship(Primitive.INSTANTIATION, Primitive.VERB);
				multipleMeanings = true;
			} else if (words.contains(ADJECTIVE_TAG)) {
				// Is an description.
				log("Adjective", Bot.FINE, meaning);
				meaning.addRelationship(Primitive.INSTANTIATION, Primitive.DESCRIPTION);
				word.addRelationship(Primitive.INSTANTIATION, Primitive.ADJECTIVE);
				multipleMeanings = true;
			} else if (words.contains(ADVERB_TAG)) {
				// Is an adverb.
				log("Adverb", Bot.FINE, meaning);
				meaning.addRelationship(Primitive.INSTANTIATION, Primitive.ADVERB);
				word.addRelationship(Primitive.INSTANTIATION, Primitive.ADVERB);
				multipleMeanings = true;
			} else if (words.contains(INTERJECTION_TAG)) {
				// Is an interjection.
				log("Interjection", Bot.FINE, meaning);
				meaning.addRelationship(Primitive.INSTANTIATION, Primitive.INTERJECTION);
				word.addRelationship(Primitive.INSTANTIATION, Primitive.INTERJECTION);
				multipleMeanings = true;
			} else if (words.contains(PRONOUN_TAG)) {
				// Is an pronoun.
				log("Pronoun", Bot.FINE, meaning);
				meaning.addRelationship(Primitive.INSTANTIATION, Primitive.PRONOUN);
				word.addRelationship(Primitive.INSTANTIATION, Primitive.PRONOUN);
				multipleMeanings = true;
			} else if (words.contains(DETERMINER_TAG)) {
				// Is an determiner.
				log("Determiner", Bot.FINE, meaning);
				meaning.addRelationship(Primitive.INSTANTIATION, Primitive.DETERMINER);
				word.addRelationship(Primitive.INSTANTIATION, Primitive.DETERMINER);
				multipleMeanings = true;
			} else if (words.contains(NUMERAL_TAG)) {
				// Is an numeral.
				log("Numeral", Bot.FINE, meaning);
				meaning.addRelationship(Primitive.INSTANTIATION, Primitive.NUMBER);
				word.addRelationship(Primitive.INSTANTIATION, Primitive.NUMERAL);
				multipleMeanings = true;
			} else if (words.contains(SYNONYMS)) {
				List<String> bullets = getNextBulletList(header);
				for (String bullet: bullets) {
					bullet = stripBrackets(bullet);
					TextStream stream = new TextStream(bullet);
					while (!stream.atEnd()) {
						List<String> synonyms = Utils.getWords(stream.upTo(','));
						stream.skip();
						if (synonyms.size() == 1) {
							if (synonyms.size() == 1) {
								Vertex synonym = network.createWord(synonyms.get(0));
								log("Synonym", Bot.FINE, meaning, synonym);
								meaning.addRelationship(Primitive.SYNONYM, synonym);
								word.addRelationship(Primitive.SYNONYM, synonym);
							}
						}
					}
				}
			} else if (words.contains(ANTONYMS)) {
				List<String> bullets = getNextBulletList(header);
				for (String bullet: bullets) {
					bullet = stripBrackets(bullet);
					TextStream stream = new TextStream(bullet);
					while (!stream.atEnd()) {
						List<String> antonyms = Utils.getWords(stream.upTo(','));
						stream.skip();
						if (antonyms.size() == 1) {
							if (antonyms.size() == 1) {
								Vertex antonym = network.createWord(antonyms.get(0));
								log("Antonym", Bot.FINE, meaning, antonym);
								meaning.addRelationship(Primitive.ANTONYM, antonym);
								word.addRelationship(Primitive.ANTONYM, antonym);
							}
						}
					}
				}
			}
			header = findNextTag(headers, null, header, h1.getParentNode());
		}
		network.associateCaseInsensitivity(word);
		
		if (!isBatch) {
			network.save();
			getBot().memory().addActiveMemory(meaning);
		}
	}

	
	/**
	 * Bypass loading the word's page, and just define it from the category info.
	 */
	public void quickProcessWord(String wordText, List<String> categoryText, Network network) {
		// Ignore empty bullets.
		if ((wordText.length() == 0)
				|| (wordText.indexOf("\n") != -1)) {
			return;
		}

		Vertex word = network.createWord(wordText);
		// TODO handle multiple meanings.
		if (word.getRelationships(Primitive.MEANING) != null) {
			log("Already known", Bot.FINE, word);
			return;
		}
		// Create meaning.
		Vertex meaning = network.createVertex();
		meaning.setName(wordText);
		log("Word", Bot.FINE, word);
		word.addRelationship(Primitive.MEANING, meaning);
		meaning.addRelationship(Primitive.WORD, word);
		
		// Only associate first word type, as is the primary meaning.
		if (categoryText.contains(NOUNS_TAG)) {
			// Is a thing.
			log("Noun", Bot.FINE, meaning);
			meaning.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
			word.addRelationship(Primitive.INSTANTIATION, Primitive.NOUN);
		} else if (categoryText.contains(VERBS_TAG)) {
			// Is an action.
			log("Verb", Bot.FINE, meaning);
			meaning.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
			word.addRelationship(Primitive.INSTANTIATION, Primitive.VERB);
		} else if (categoryText.contains(ADJECTIVES_TAG)) {
			// Is an description.
			log("Adjective", Bot.FINE, meaning);
			meaning.addRelationship(Primitive.INSTANTIATION, Primitive.DESCRIPTION);
			word.addRelationship(Primitive.INSTANTIATION, Primitive.ADJECTIVE);
		}
	}
	
	/**
	 * Post, process the post request.
	 */
	@Override
	public void output(Vertex output) {
		
	}
	
	public String stripBrackets(String text) {
		StringWriter writer = new StringWriter();
		TextStream stream = new TextStream(text);
		text = stream.upToAny("[({").trim();
		writer.write(text);
		while (!stream.atEnd()) {
			String comment = stream.upToAny("])}");
			if (comment.contains("obsolete")) {
				return "";
			}
			if (stream.atEnd()) {
				break;
			}
			stream.skip();
			if (stream.atEnd()) {
				break;
			}
			if (stream.peek() == ':') {
				stream.skip();
			}
			if (stream.atEnd()) {
				break;
			}
			text = stream.upToAny("[({").trim();
			writer.write(" ");
			writer.write(text);
		}
		return writer.toString();
	}
	
	public void copyDataRelationships(Vertex source, Primitive type, Vertex target, Network network) {
		Collection<Relationship> relationships = source.getRelationships(type);
		if (relationships != null) {
			for (Relationship relationship : relationships) {
				Object data = relationship.getTarget().getData();
				if (data != null) {
					target.addRelationship(type, network.createVertex(data));
				}
			}
		}
	}

	/**
	 * Import the word from Wiktionary.
	 */
	public Vertex importWord(String word, Network network) {
		try {
			input(new URL(URL_PREFIX + URLEncoder.encode(word, "UTF-8")), network);
		} catch (Exception failed) {
			throw new BotException(failed);
		}
		network.save();
		Vertex result = network.createVertex(word);
		network.save();
		return result;
	}
	
	public boolean isPossibleWord(String text) {
		if (this.lang == null || this.lang.startsWith("en") && !Utils.isEnglish(text)) {
			return false;
		}
		if (Utils.containsAny(text, "1234567890@")) {
			return false;
		}
		return true;
	}

	/**
	 * Self API
	 * Lookup the meaning of the word.
	 * Called from Self.
	 */
	public Vertex define(Vertex source, Vertex word) {
		String text = word.getDataValue();
		if (text != null) {
			Vertex checked = word.getRelationship(getPrimitive());
			Collection<Relationship> meanings = word.getRelationships(Primitive.MEANING);
			// Ignore if already discovered.
			// Also check if the meaning may have been forgotten.
			if (checked == null || (meanings == null && (!word.instanceOf(Primitive.UNKNOWNWORD) && !word.instanceOf(Primitive.UNKOWNWORD)))) {
				if (!isPossibleWord(text)) {
					return null;
				}
				Network network = word.getNetwork();
				word.setRelationship(getPrimitive(), network.createVertex(Primitive.TRUE));

				// TODO handle multiple meanings.
				Collection<Relationship> relationships = word.getRelationships(Primitive.MEANING);
				Vertex existing = word.getRelationship(Primitive.MEANING);
				if (existing != null) {
					if (!existing.instanceOf(Primitive.SPEAKER) || (relationships.size() > 1)) {
						return null;
					}
				}
				
				// Check cache first.
				boolean found = false;
				if (this.cache && getBot().getParent() != null) {
					Network cache = getBot().getParent().memory().newMemory();
					Vertex cacheWord = cache.createWord(text);
					Vertex cacheChecked = cacheWord.getRelationship(getPrimitive());
					meanings = cacheWord.getRelationships(Primitive.MEANING);
					if (cacheChecked != null && (meanings != null || (cacheWord.instanceOf(Primitive.UNKNOWNWORD) || word.instanceOf(Primitive.UNKOWNWORD)))) {
						found = true;
						log("Importing word from cache", Bot.FINE, text);
						Vertex newWord = network.createVertex(word);
						copyDataRelationships(cacheWord, Primitive.INSTANTIATION, newWord, network);
						copyDataRelationships(cacheWord, Primitive.SYNONYM, newWord, network);
						copyDataRelationships(cacheWord, Primitive.ANTONYM, newWord, network);
						copyDataRelationships(cacheWord, Primitive.CARDINALITY, newWord, network);
						meanings = cacheWord.getRelationships(Primitive.MEANING);
						if (meanings != null) {
							for (Relationship cacheMeaningRelationship : meanings) {
								Vertex cacheMeaning = cacheMeaningRelationship.getTarget();
								Vertex newMeaning = network.createVertex();
								newMeaning.setName(text);
								newWord.addWeakRelationship(Primitive.MEANING, newMeaning, cacheMeaningRelationship.getCorrectness());
								newMeaning.addRelationship(Primitive.WORD, newWord);
								copyDataRelationships(cacheMeaning, Primitive.SYNONYM, newMeaning, network);
								copyDataRelationships(cacheMeaning, Primitive.ANTONYM, newMeaning, network);
								copyDataRelationships(cacheMeaning, Primitive.CARDINALITY, newMeaning, network);
								copyDataRelationships(cacheMeaning, Primitive.INSTANTIATION, newMeaning, network);
								Vertex definition = cacheMeaning.getRelationship(Primitive.SENTENCE);
								if (definition != null && definition.getData() instanceof String) {
									newMeaning.addRelationship(Primitive.SENTENCE, network.createSentence((String)definition.getData()));
								}
							}
						}
						//Vertex url = cacheWord.getRelationship(Primitive.URL);
						//if (url != null && url.getData() != null) {
						//	newWord.addRelationship(Primitive.URL, network.createVertex(url.getData()));
						//}
					} else {
						cacheWord.addRelationship(getPrimitive(), cache.createVertex(Primitive.TRUE));
						cacheWord.addRelationship(Primitive.INSTANTIATION, Primitive.UNKNOWNWORD);
						cache.save();
						Wiktionary wiktionary = getBot().getParent().awareness().getSense(Wiktionary.class);
						try {
							wiktionary.input(new URL(URL_PREFIX + URLEncoder.encode(text, "UTF-8")));
						} catch (Exception failed) {
							log(failed);
							return null;
						}
					}
				}
				// Lookup locally.
				if (!found) {
					try {
						input(new URL(URL_PREFIX + URLEncoder.encode(text, "UTF-8")), network);
					} catch (Exception failed) {
						log(failed);
						return null;
					}
				}
			}
		}
		return word.mostConscious(Primitive.MEANING);
	}
	
	/**
	 * Attempt to discover information on the vertex.
	 */
	@Override
	public void discoverSentence(Vertex sentence, Network network, Vertex currentTime) {
		if (!isEnabled()) {
			return;
		}
		if (!getBot().mind().isConscious()) {
			return;
		}
		List<Relationship> words = sentence.orderedRelationships(Primitive.WORD);
		if (words != null) {
			int count = 0;
			int errors = 0;
			int index = 0;
			Boolean allCaps = null;
			for (Relationship relationship : words) {
				if (!getBot().mind().isConscious()) {
					return;
				}
				Vertex word = relationship.getTarget();
				index++;
				if ((count >= MAX_WORDS) || (errors >= MAX_ERRORS)) {
					break;
				}
				String text = word.getDataValue();
				if (text != null) {
					Vertex checked = word.getRelationship(getPrimitive());
					Collection<Relationship> meanings = word.getRelationships(Primitive.MEANING);
					// Ignore if already discovered.
					// Also check if the meaning may have been forgotten.
					if (checked == null || (meanings == null && (!word.instanceOf(Primitive.UNKNOWNWORD) && !word.instanceOf(Primitive.UNKOWNWORD)))) {
						if (!isPossibleWord(text)) {
							count++;
							continue;
						}
						word.setRelationship(getPrimitive(), network.createVertex(Primitive.TRUE));

						// TODO handle multiple meanings.
						Collection<Relationship> relationships = word.getRelationships(Primitive.MEANING);
						Vertex existing = word.getRelationship(Primitive.MEANING);
						if (existing != null) {
							if (!existing.instanceOf(Primitive.SPEAKER) || (relationships.size() > 1)) {
								continue;
							}
						}
						
						// Check cache first.
						boolean found = false;
						if (this.cache && getBot().getParent() != null) {
							Network cache = getBot().getParent().memory().newMemory();
							Vertex cacheWord = cache.createWord(text);
							Vertex cacheChecked = cacheWord.getRelationship(getPrimitive());
							meanings = cacheWord.getRelationships(Primitive.MEANING);
							if (cacheChecked != null && (meanings != null || (cacheWord.instanceOf(Primitive.UNKOWNWORD)))) {
								found = true;
								log("Importing word from cache", Level.FINE, text);
								Network newNetwork = getBot().memory().newMemory();
								Vertex newWord = newNetwork.createVertex(word);
								copyDataRelationships(cacheWord, Primitive.INSTANTIATION, newWord, newNetwork);
								copyDataRelationships(cacheWord, Primitive.SYNONYM, newWord, newNetwork);
								copyDataRelationships(cacheWord, Primitive.ANTONYM, newWord, newNetwork);
								copyDataRelationships(cacheWord, Primitive.CARDINALITY, newWord, newNetwork);
								meanings = cacheWord.getRelationships(Primitive.MEANING);
								if (meanings != null) {
									for (Relationship cacheMeaningRelationship : meanings) {
										if (!getBot().mind().isConscious()) {
											return;
										}
										Vertex cacheMeaning = cacheMeaningRelationship.getTarget();
										Vertex newMeaning = newNetwork.createVertex();
										newMeaning.setName(text);
										newWord.addWeakRelationship(Primitive.MEANING, newMeaning, cacheMeaningRelationship.getCorrectness());
										newMeaning.addRelationship(Primitive.WORD, newWord).setCorrectness(2.0f);
										copyDataRelationships(cacheMeaning, Primitive.SYNONYM, newMeaning, newNetwork);
										copyDataRelationships(cacheMeaning, Primitive.ANTONYM, newMeaning, newNetwork);
										copyDataRelationships(cacheMeaning, Primitive.CARDINALITY, newMeaning, newNetwork);
										copyDataRelationships(cacheMeaning, Primitive.INSTANTIATION, newMeaning, newNetwork);
										Vertex definition = cacheMeaning.getRelationship(Primitive.SENTENCE);
										if (definition != null && definition.getData() instanceof String) {
											newMeaning.addRelationship(Primitive.SENTENCE, newNetwork.createSentence((String)definition.getData()));
										}
									}
								}
								//Vertex url = cacheWord.getRelationship(Primitive.URL);
								//if (url != null && url.getData() != null) {
								//	newWord.addRelationship(Primitive.URL, newNetwork.createVertex(url.getData()));
								//}
								if (meanings != null) {
									// Don't associate case if unknown, other cases may be word (URL).
									newNetwork.associateCaseInsensitivity(newWord);
								}
								if (!getBot().mind().isConscious()) {
									return;
								}
								newNetwork.save();
							} else {
								if (!getBot().mind().isConscious()) {
									return;
								}
								cacheWord.addRelationship(getPrimitive(), cache.createVertex(Primitive.TRUE));
								cacheWord.addRelationship(Primitive.INSTANTIATION, Primitive.UNKNOWNWORD);
								cache.save();
								Wiktionary wiktionary = getBot().getParent().awareness().getSense(Wiktionary.class);
								try {
									wiktionary.input(new URL(URL_PREFIX + URLEncoder.encode(text, "UTF-8")));
								} catch (Exception failed) {
									log(failed);
									errors++;
								}
								if (!text.equals(text.toLowerCase())) {
									try {
										wiktionary.input(new URL(URL_PREFIX + URLEncoder.encode(text.toLowerCase(), "UTF-8")));
										count++;
									} catch (Exception failed) {
										log(failed);
										errors++;
									}
								}
							}
						}
						// Lookup locally.
						if (!found) {
							word.addRelationship(Primitive.INSTANTIATION, Primitive.UNKNOWNWORD);
							if (!getBot().mind().isConscious()) {
								return;
							}
							network.save();
							try {
								input(new URL(URL_PREFIX + URLEncoder.encode(text, "UTF-8")));
								count++;
							} catch (Exception failed) {
								log(failed);
								errors++;
							}
							if (!text.equals(text.toLowerCase())) {
								try {
									input(new URL(URL_PREFIX + URLEncoder.encode(text.toLowerCase(), "UTF-8")));
									count++;
								} catch (Exception failed) {
									log(failed);
									errors++;
								}
							}
						}
						// Check if it is a name.
						Network newNetwork = getBot().memory().newMemory();
						Vertex newWord = newNetwork.createVertex(word);
						if ((index > 1) && allCaps != Boolean.TRUE && Utils.isCapitalized(text) && !newWord.hasRelationship(Primitive.MEANING)) {
							if (allCaps == null) {
								allCaps = Boolean.TRUE;
								for (Relationship relationship2 : words) {
									if (!(relationship2.getTarget().getData() instanceof String) || !Utils.isCapitalized((String)relationship2.getTarget().getData())) {
										allCaps = Boolean.FALSE;
										break;
									}
								}
							}
							if (!allCaps) {
								log("Defining name", Bot.FINE, text);
								Vertex meaning = newNetwork.createVertex();
								meaning.setName(text);
								meaning.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
								meaning.addRelationship(Primitive.WORD, newWord);
								newWord.addRelationship(Primitive.MEANING, meaning);
								newWord.addRelationship(Primitive.INSTANTIATION, Primitive.NAME);
								Relationship previous = words.get(index - 2);
								if (previous.getTarget().instanceOf(Primitive.NAME)) {
									String compoundText = previous.getTarget().getDataValue() + " " + text;
									log("Defining compound name", Bot.FINE, compoundText);
									Vertex compoundWord = newNetwork.createWord(compoundText);
									meaning = newNetwork.createVertex();
									meaning.setName(compoundText);
									meaning.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
									meaning.addRelationship(Primitive.WORD, compoundWord);
									meaning.addRelationship(Primitive.WORD, previous.getTarget());
									previous.getTarget().addRelationship(Primitive.MEANING, meaning);
									compoundWord.addRelationship(Primitive.MEANING, meaning);
									compoundWord.addRelationship(Primitive.INSTANTIATION, Primitive.NAME);
								}
								if (!getBot().mind().isConscious()) {
									return;
								}
								newNetwork.save();
							}
						}
					}
				}
			}
		}
	}
	
}
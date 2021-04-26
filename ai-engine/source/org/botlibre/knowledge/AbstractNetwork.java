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
package org.botlibre.knowledge;

import java.io.Serializable;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.aiml.AIMLParser;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.self.SelfByteCodeCompiler;
import org.botlibre.self.SelfCompiler;
import org.botlibre.self.SelfParseException;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;


/**
 * An interconnected set of vertices,
 * representing and knowledge-space.
 * Basic implementation to allow subclasses to avoid defining some of the basic stuff.
 */

public abstract class AbstractNetwork implements Network, Cloneable, Serializable  {

	private static final long serialVersionUID = 1L;

	/** Define max text size for data value. */
	public static final int MAX_TEXT = 1000;
	
	public static int MAX_SIZE = 500;
	
	protected static long nextId = 1;
	protected static long nextRelationshipId = 1;
	protected static long nextDataId = 1;

	protected static long nextDataId() {
		return nextDataId++;
	}

	protected static long nextRelationshipId() {
		return nextRelationshipId++;
	}
	
	protected static long nextId() {
		return nextId++;
	}
	
	protected boolean isShortTerm;
	
	protected Map<Object, Vertex> verticesByData = null;
	
	/** Back reference to Bot instance. **/
	protected transient Bot bot;

	public AbstractNetwork(boolean isShortTerm) {
		this.isShortTerm = isShortTerm;
		this.verticesByData = new HashMap<Object, Vertex>();
	}
	
	public boolean isReadOnly() {
		return false;
	}

	public Map<Object, Vertex> getVerticesByData() {
		return verticesByData;
	}

	protected void setVerticesByData(Map<Object, Vertex> verticesByData) {
		this.verticesByData = verticesByData;
	}

	/**
	 * Save the property setting to the current transaction.
	 */
	public void saveProperty(String propertyName, String value, boolean startup) {
		this.bot.memory().setProperty(propertyName, value);
	}

	/**
	 * Remove the property setting to the current transaction.
	 */
	public void removeProperty(String propertyName) {
		this.bot.memory().removeProperty(propertyName);
	}
	
	/**
	 * Return a copy of the network.
	 */
	public synchronized AbstractNetwork clone() {
		AbstractNetwork clone = null;
		try {
			clone = (AbstractNetwork)super.clone();
		} catch (CloneNotSupportedException exception) {
			throw new Error(exception);
		}
		return clone;
	}

	public String toString() {
		StringWriter writer = new StringWriter();
		writer.write(getClass().getSimpleName());
		writer.write("(");
		writer.write(String.valueOf(size()));
		writer.write(")");

		return writer.toString();
	}
	
	public abstract void addVertex(Vertex vertex);
	
	protected abstract void addRelationship(Relationship relationship);
	
	/**
	 * Create a new vertex in this network,
	 * assign the id and creation date.
	 */
	public synchronized Vertex createVertex() {
		BasicVertex vertex = new BasicVertex();
		vertex.init();
		addVertex(vertex);
		return vertex;
	}
	
	/**
	 * Create a temporary, non-persistent vertex.
	 */
	public Vertex createTemporyVertex() {
		BasicVertex vertex = new BasicVertex();
		vertex.setIsTemporary(true);
		vertex.setNetwork(this);
		return vertex;
	}
	
	/**
	 * Create a new vertex from the source.
	 * The source is from another network.
	 */
	public synchronized Vertex createVertex(Vertex source) {
		Vertex vertex = findById(source.getId());
		if (vertex == null) {
			vertex = findByData(source.getData());
		}
		if (vertex == null) {
			vertex = new BasicVertex();
			vertex.setName(source.getName());
			vertex.setData(source.getData());
			vertex.setAccessCount(source.getAccessCount());
			vertex.setAccessDate(source.getAccessDate());
			vertex.setPinned(source.isPinned());
			vertex.setCreationDate(source.getCreationDate());
			vertex.setConsciousnessLevel(source.getConsciousnessLevel());
			addVertex(vertex);
			// Set id from database.
			source.setId(vertex.getId());
		}
		return vertex;
	}
	
	/**
	 * Create a new vertex in this network with the data,
	 * If a vertex with the data already exists, then it is returned as the data must be unique.
	 */
	@SuppressWarnings("rawtypes")
	public synchronized Vertex createVertex(Object data) {
		if ((data instanceof String) && ((String)data).length() > MAX_TEXT) {
			data = ((String)data).substring(0, MAX_TEXT);
		}
		if (data instanceof Class) {
			data = new Primitive(((Class)data).getName());
		}
		Vertex vertex = findByData(data);
		Vertex meaning = null;
		Vertex similar = null;
		if ((vertex == null) && (data instanceof String)) {
			// Perform case insensitive lookup, and associate the same meaning.
			String word = ((String)data).toLowerCase();
			if (!word.equals(data)) {
				similar = findByData(word);
			}
			if (similar == null) {
				word = Utils.capitalize((String)data);
				if (!word.equals(data)) {
					similar = findByData(word);
				}
			}
			if (similar == null) {
				word = ((String)data).toUpperCase();
				if (!word.equals(data)) {
					similar = findByData(word);
				}
			}
			if (similar != null) {
				meaning = similar.getRelationship(Primitive.MEANING);
			}
		}
		if (vertex == null) {
			vertex = new BasicVertex();
			vertex.init();
			vertex.setData(data);
			addVertex(vertex);
			if (meaning != null) {
				vertex.addRelationship(Primitive.MEANING, meaning);
				meaning.addWeakRelationship(Primitive.WORD, vertex, 0.2f);
				// Associate word type from similar word.
				Collection<Relationship> relationships = similar.getRelationships(Primitive.INSTANTIATION);
				if (relationships != null) {
					for (Relationship type : relationships) {
						if (!type.isInverse()) {
							vertex.addRelationship(Primitive.INSTANTIATION, type.getTarget());
						}
					}
				}
			}
		}
		// TODO: better way/place to do this
		if ((data instanceof BigInteger) && !vertex.instanceOf(Primitive.INTEGER)) {
			boolean isNegative = false;
			vertex.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
			vertex.addRelationship(Primitive.INSTANTIATION, Primitive.NUMBER);
			vertex.addRelationship(Primitive.INSTANTIATION, Primitive.INTEGER);
			String digits = data.toString();
			int length = digits.length();
			for (int index = 0; index < length; index++) {
				String digit = digits.substring(length - index - 1, length - index);
				if (digit.equals("-")) {
					vertex.addRelationship(Primitive.INSTANTIATION, Primitive.NEGATIVE);
					isNegative = true;
					continue;
				}
				if (Character.isDigit(digit.charAt(0))) {
					vertex.addRelationship(Primitive.DIGIT, createVertex(new BigInteger(digit)), index);
				}
			}
			Vertex word = createWord(digits);
			word.addRelationship(Primitive.MEANING, vertex);
			vertex.addRelationship(Primitive.WORD, word);
			if (!isNegative) {
				char last = digits.charAt(digits.length() - 1);
				Vertex ordinal = null;
				if (last == '1') {
					ordinal = createWord(digits + "st");
				} else if (last == '2') {
					ordinal = createWord(digits + "nd");
				} else if (last == '3') {
					ordinal = createWord(digits + "rd");
				} else {
					ordinal = createWord(digits + "th");
				}
				ordinal.addRelationship(Primitive.MEANING, vertex);
				vertex.addRelationship(Primitive.ORDINAL, ordinal);
				
				word = createWord("+" + digits);
				word.addRelationship(Primitive.MEANING, vertex);
				vertex.addWeakRelationship(Primitive.WORD, word, 0.2f);
			}
		}
		if ((data instanceof BigDecimal) && !vertex.instanceOf(Primitive.DECIMAL)) {
			boolean isNegative = false;
			vertex.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
			vertex.addRelationship(Primitive.INSTANTIATION, Primitive.NUMBER);
			vertex.addRelationship(Primitive.INSTANTIATION, Primitive.DECIMAL);
			String digits = ((BigDecimal) data).toPlainString();
			int length = digits.length();
			boolean decimal = false;
			for (int index = 0; index < length; index++) {
				String digit = digits.substring(length - index - 1, length - index);
				if (digit.equals(".")) {
					decimal = true;
					continue;
				}
				if (digit.equals("-")) {
					vertex.addRelationship(Primitive.INSTANTIATION, Primitive.NEGATIVE);
					isNegative = true;
					continue;
				}
				if (Character.isDigit(digit.charAt(0))) {
					if (decimal) {
						vertex.addRelationship(Primitive.DECIMAL, createVertex(new BigInteger(digit)), index);
					} else {
						vertex.addRelationship(Primitive.INTEGER, createVertex(new BigInteger(digit)), index);
					}
				}
			}
			Vertex word = createWord(digits);
			word.addRelationship(Primitive.MEANING, vertex);
			vertex.addRelationship(Primitive.WORD, word);
			if (!isNegative) {
				word = createWord("+" + digits);
				word.addRelationship(Primitive.MEANING, vertex);
				vertex.addWeakRelationship(Primitive.WORD, word, 0.2f);
			}
		}
		if (data instanceof Time) {
			Time time = (Time)data;
			vertex.addRelationship(Primitive.INSTANTIATION, Primitive.TIME);
			
			String text = Utils.printTime(time, "h:mm:ss a");
			Vertex word = createWord(text);
			word.addRelationship(Primitive.MEANING, vertex);
			vertex.addRelationship(Primitive.WORD, word);
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(time);
			int hour = calendar.get(Calendar.HOUR);
			if (hour == 0) {
				hour = 12;
			}
			vertex.addRelationship(Primitive.HOUR, createVertex(BigInteger.valueOf(hour)));
			vertex.addRelationship(Primitive.MINUTE, createVertex(BigInteger.valueOf(calendar.get(Calendar.MINUTE))));
			vertex.addRelationship(Primitive.SECOND, createVertex(BigInteger.valueOf(calendar.get(Calendar.SECOND))));
			if (calendar.get(Calendar.AM_PM) == Calendar.PM) {
				vertex.addRelationship(Primitive.AM_PM, Primitive.PM);
			} else {
				vertex.addRelationship(Primitive.AM_PM, Primitive.AM);
			}
		}
		if (data instanceof java.sql.Date) {
			java.sql.Date date = (java.sql.Date)data;
			vertex.addRelationship(Primitive.INSTANTIATION, Primitive.DATE);
			
			String text = Utils.printDate(date, "EEEE MMMM d y");
			Vertex word = createWord(text);
			word.addRelationship(Primitive.MEANING, vertex);
			vertex.addRelationship(Primitive.WORD, word);
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			vertex.addRelationship(Primitive.DAY_OF_YEAR, createVertex(BigInteger.valueOf(calendar.get(Calendar.DAY_OF_YEAR))));
			vertex.addRelationship(Primitive.DAY, createVertex(BigInteger.valueOf(calendar.get(Calendar.DATE))));
			vertex.addRelationship(Primitive.DAY_OF_WEEK, createVertex(Primitive.DAYS_OF_WEEK[calendar.get(Calendar.DAY_OF_WEEK) - 1]));
			vertex.addRelationship(Primitive.MONTH, createVertex(Primitive.MONTHS[calendar.get(Calendar.MONTH)]));
			vertex.addRelationship(Primitive.YEAR, createVertex(BigInteger.valueOf(calendar.get(Calendar.YEAR))));
		}
		return vertex;
	}

	/**
	 * Merge the vertices and relations of the network into this network.
	 */
	// Obsolete with DatabaseMemory
	public synchronized void merge(Network network) {
		for (Vertex vertex : network.findAll()) {
			merge(vertex);
		}
	}

	/**
	 * Merge the vertex into this network.
	 */
	// Obsolete with DatabaseMemory
	public synchronized void merge(Vertex sourceVertex) {
		Vertex targetVertex = createVertex(sourceVertex);
		if (targetVertex.getId() == null) {
			// Don't assign id until merge so other networks can assign differently.
			targetVertex.setId(Long.valueOf(nextId()));
			sourceVertex.setId(targetVertex.getId());
		} else if (sourceVertex.getId() == null) {
			// Must assign id, so source can be associated with target.
			sourceVertex.setId(targetVertex.getId());
		}
		if (sourceVertex.hasRelationships()) {
			List<Relationship> targetRelationships = new ArrayList<Relationship>();
			// Add and update relationships.
			for (Iterator<Relationship> relationships = sourceVertex.allRelationships(); relationships.hasNext();) {
				Relationship sourceRelationship = relationships.next();
				Vertex targetRelationshipType = createVertex(sourceRelationship.getType());
				Vertex targetRelationshipTarget = createVertex(sourceRelationship.getTarget());
				Relationship targetRelationship = targetVertex.addRelationship(targetRelationshipType, targetRelationshipTarget, sourceRelationship.getIndex(), true);
				if (!sourceRelationship.isInverse()) {
					targetRelationship.setCorrectness(Math.max(targetRelationship.getCorrectness(), sourceRelationship.getCorrectness()));
				} else {
					targetRelationship.setCorrectness(Math.min(targetRelationship.getCorrectness(), sourceRelationship.getCorrectness()));
				}
				targetRelationship.setIndex(sourceRelationship.getIndex());
				if (sourceRelationship.hasMeta()) {
					Vertex meta = createVertex(sourceRelationship.getMeta());
					targetRelationship.setMeta(meta);
				}
				addRelationship(targetRelationship);
				targetRelationships.add(targetRelationship);
			}
			if (sourceVertex.totalRelationships() != targetVertex.totalRelationships()) {
				targetVertex.getRelationships().clear();
				// Remove the removed relationships.
				for (Relationship targetRelationship : targetRelationships) {
					targetVertex.addRelationship(targetRelationship, true);
				}
			}
		}
		if (!targetVertex.isPrimitive()) {
			targetVertex.setAccessCount(sourceVertex.getAccessCount());
			targetVertex.setAccessDate(sourceVertex.getAccessDate());
			targetVertex.setPinned(sourceVertex.isPinned());
			targetVertex.setConsciousnessLevel(sourceVertex.getConsciousnessLevel());
		}
	}

	/**
	 * Find the vertex matching the source, or create a new one.
	 * This is used from importing another memory.
	 */
	public synchronized Vertex importVertex(Vertex source, Map<Vertex, Vertex> identitySet) {
		Vertex target = identitySet.get(source);
		if (target != null) {
			return target;
		}
		target = findByData(source.getData());
		if (target == null) {
			target = findByName(source.getName());
			if (target != null) {
				// If the source is not the same classification as the target, then may be something different.
				for (Relationship classification : target.getRelationships(Primitive.INSTANTIATION)) {
					if (!source.hasRelationship(createVertex(Primitive.INSTANTIATION), classification.getTarget())) {
						target = null;
						break;
					}
				}
			}
			if (target == null) {
				target = new BasicVertex();
				target.setName(source.getName());
				target.setData(source.getData());
				target.setAccessCount(source.getAccessCount());
				target.setAccessDate(source.getAccessDate());
				target.setPinned(source.isPinned());
				target.setCreationDate(source.getCreationDate());
				target.setConsciousnessLevel(source.getConsciousnessLevel());
				addVertex(target);
			}
		}
		identitySet.put(source, target);
		return target;
	}

	/**
	 * Merge the vertex into this network from an import.
	 * This is used from importing another memory.
	 */
	public synchronized void importMerge(Vertex source, Map<Vertex, Vertex> identitySet) {
		Vertex target = importVertex(source, identitySet);
		for (Iterator<Relationship> relationships = source.allRelationships(); relationships.hasNext();) {
			Relationship sourceRelationship = relationships.next();
			Vertex targetRelationshipType = importVertex(sourceRelationship.getType(), identitySet);
			Vertex targetRelationshipTarget = importVertex(sourceRelationship.getTarget(), identitySet);
			Relationship targetRelationship = target.addRelationship(targetRelationshipType, targetRelationshipTarget, sourceRelationship.getIndex(), true);
			if (!sourceRelationship.isInverse()) {
				targetRelationship.setCorrectness(Math.max(targetRelationship.getCorrectness(), sourceRelationship.getCorrectness()));
			} else {
				targetRelationship.setCorrectness(Math.min(targetRelationship.getCorrectness(), sourceRelationship.getCorrectness()));
			}
			targetRelationship.setIndex(sourceRelationship.getIndex());
			addRelationship(targetRelationship);
		}
	}

	/**
	 * Remove the relationship from the network.
	 * Note that the relationship must be no longer referenced by any other vertex in the network.
	 */
	public void removeRelationship(Relationship relationship) {
		// Nothing required by default.
	}
	
	/**
	 * Return the relationship meta vertex.
	 */
	public Vertex createMeta(Relationship relationship) {
		Vertex meta = relationship.getMeta();
		if (meta == null) {
			meta = createInstance(Primitive.META);
			meta.setType("Meta");
			relationship.setMeta(meta);
		}
		return meta;
	}
	
	/**
	 * Create a new instance of the type.
	 */
	public Vertex createInstance(Primitive type) {
		return createInstance(createVertex(type));
	}
	
	/**
	 * Create a new instance of the type.
	 */
	public Vertex createInstance(Vertex type) {
		Vertex instance = createVertex();
		instance.addRelationship(Primitive.INSTANTIATION, type);
		return instance;
	}

	/**
	 * Tokenize the text into its words and create a vertex representation of the word or compound word.
	 */
	public synchronized Vertex createWord(String text) {
		text = text.trim();
		List<String> wordsText = Utils.getWords(text);
		// Allow # to represent a primitive.
		if ((wordsText.size() == 1) && (text.length() > 0) && (text.charAt(0) == '#')) {
			Vertex primitive = createVertex(new Primitive(text));
			Vertex word = createVertex(text);
			word.addRelationship(Primitive.INSTANTIATION, Primitive.WORD);
			word.addRelationship(Primitive.MEANING, primitive);
			return word;
		}
		Vertex compoundWord = createVertex(text);
		// Check if is a compound word.
		if (wordsText.size() > 1) {
			compoundWord.addRelationship(Primitive.INSTANTIATION, Primitive.COMPOUND_WORD);
			compoundWord.addRelationship(Primitive.INSTANTIATION, Primitive.WORD);
			for (int index = 0; index < wordsText.size(); index++) {
				String wordText = wordsText.get(index);
				Vertex word = createVertex(wordText);
				word.addRelationship(Primitive.INSTANTIATION, Primitive.WORD);
				// Only associate first word for indexing.
				if (index == 0) {
					word.addRelationship(Primitive.COMPOUND_WORD, compoundWord);
				}
				compoundWord.addRelationship(Primitive.WORD, word, index);
			}
		} else {
			compoundWord.addRelationship(Primitive.INSTANTIATION, Primitive.WORD);
		}
		return compoundWord;
	}

	/**
	 * Create the word as a name.
	 */
	public synchronized Vertex createName(String text) {
		Vertex name = createFragment(text);
		name.addRelationship(Primitive.INSTANTIATION, Primitive.NAME);
		return name;
	}


	/**
	 * Associate alternative cases of the word with the meaning.
	 */
	public synchronized void associateCaseInsensitivity(String word, Vertex meaning) {
		Collection<Relationship> classifications = createVertex(word).getRelationships(Primitive.INSTANTIATION);
		String lower = word.toLowerCase();
		Language language = getBot().mind().getThought(Language.class);
		if (language.getTrackCase()) {
			// Old case over sensitivity code.
			Vertex capWord = createWord(Utils.capitalize(lower));
			capWord.addRelationship(Primitive.MEANING, meaning);
			meaning.addWeakRelationship(Primitive.WORD, capWord, 0.2f);
			if (classifications != null) {
				for (Relationship classification : classifications) {
					if (!classification.isInverse()) {
						capWord.addRelationship(Primitive.INSTANTIATION, classification.getTarget());
					}
				}
			}
			Vertex upperWord = createWord(lower.toUpperCase());
			upperWord.addRelationship(Primitive.MEANING, meaning);
			meaning.addWeakRelationship(Primitive.WORD, upperWord, 0.2f);
			if (classifications != null) {
				for (Relationship classification : classifications) {
					if (!classification.isInverse()) {
						upperWord.addRelationship(Primitive.INSTANTIATION, classification.getTarget());
					}
				}
			}
		}
		Vertex lowerWord = createWord(lower);
		lowerWord.addRelationship(Primitive.MEANING, meaning);
		meaning.addWeakRelationship(Primitive.WORD, lowerWord, 0.2f);
		if (classifications != null) {
			for (Relationship classification : classifications) {
				if (!classification.isInverse()) {
					lowerWord.addRelationship(Primitive.INSTANTIATION, classification.getTarget());
				}
			}
		}
	}


	/**
	 * Associate alternative cases of the word with the meaning, types, conjugations.
	 */
	public synchronized void associateCaseInsensitivity(Vertex word) {
		Collection<Relationship> meanings = word.getRelationships(Primitive.MEANING);
		Collection<Relationship> classifications = word.getRelationships(Primitive.INSTANTIATION);
		Collection<Relationship> types = word.getRelationships(Primitive.TYPE);
		Collection<Relationship> tenses = word.getRelationships(Primitive.TENSE);
		Collection<Relationship> conjugations = word.getRelationships(Primitive.CONJUGATION);
		String lower = word.getDataValue().toLowerCase();
		Language language = getBot().mind().getThought(Language.class);
		if (language.getTrackCase()) {
			// Old case over sensitivity code.
			String caps = Utils.capitalize(lower);
			Vertex capWord = createWord(caps);
			if (meanings != null) {
				for (Relationship meaning : meanings) {
					if (!meaning.isInverse()) {
						capWord.addRelationship(Primitive.MEANING, meaning.getTarget());
						meaning.getTarget().addWeakRelationship(Primitive.WORD, capWord, 0.2f);
					}
				}
			}
			if (classifications != null) {
				for (Relationship classification : classifications) {
					if (!classification.isInverse()) {
						capWord.addRelationship(Primitive.INSTANTIATION, classification.getTarget());
					}
				}
			}
			if (types != null) {
				for (Relationship type : types) {
					if (!type.isInverse()) {
						capWord.addRelationship(Primitive.TYPE, type.getTarget());
					}
				}
			}
			if (tenses != null) {
				for (Relationship tense : tenses) {
					if (!tense.isInverse()) {
						capWord.addRelationship(Primitive.TENSE, tense.getTarget());
					}
				}
			}
			if (conjugations != null) {
				for (Relationship conjugation : conjugations) {
					if (!conjugation.isInverse()) {
						capWord.addRelationship(Primitive.CONJUGATION, conjugation.getTarget());
					}
				}
			}
			Vertex upperWord = createWord(lower.toUpperCase());
			if (meanings != null) {
				for (Relationship meaning : meanings) {
					if (!meaning.isInverse()) {
						upperWord.addRelationship(Primitive.MEANING, meaning.getTarget());
						meaning.getTarget().addWeakRelationship(Primitive.WORD, upperWord, 0.2f);
					}
				}
			}
			if (classifications != null) {
				for (Relationship classification : classifications) {
					if (!classification.isInverse()) {
						upperWord.addRelationship(Primitive.INSTANTIATION, classification.getTarget());
					}
				}
			}
			if (types != null) {
				for (Relationship type : types) {
					if (!type.isInverse()) {
						upperWord.addRelationship(Primitive.TYPE, type.getTarget());
					}
				}
			}
			if (tenses != null) {
				for (Relationship tense : tenses) {
					if (!tense.isInverse()) {
						upperWord.addRelationship(Primitive.TENSE, tense.getTarget());
					}
				}
			}
			if (conjugations != null) {
				for (Relationship conjugation : conjugations) {
					if (!conjugation.isInverse()) {
						upperWord.addRelationship(Primitive.CONJUGATION, conjugation.getTarget());
					}
				}
			}
		}
		Vertex lowerWord = createWord(lower);
		if (meanings != null) {
			for (Relationship meaning : meanings) {
				if (!meaning.isInverse()) {
					lowerWord.addRelationship(Primitive.MEANING, meaning.getTarget());
					meaning.getTarget().addWeakRelationship(Primitive.WORD, lowerWord, 0.2f);
				}
			}
		}
		if (classifications != null) {
			for (Relationship classification : classifications) {
				if (!classification.isInverse()) {
					lowerWord.addRelationship(Primitive.INSTANTIATION, classification.getTarget());
				}
			}
		}
		if (types != null) {
			for (Relationship type : types) {
				if (!type.isInverse()) {
					lowerWord.addRelationship(Primitive.TYPE, type.getTarget());
				}
			}
		}
		if (tenses != null) {
			for (Relationship tense : tenses) {
				if (!tense.isInverse()) {
					lowerWord.addRelationship(Primitive.TENSE, tense.getTarget());
				}
			}
		}
		if (conjugations != null) {
			for (Relationship conjugation : conjugations) {
				if (!conjugation.isInverse()) {
					lowerWord.addRelationship(Primitive.CONJUGATION, conjugation.getTarget());
				}
			}
		}
	}
	
	/**
	 * Convert the sentence to a paragraph if it has multiple phrases.
	 */
	public Vertex createParagraph(Vertex sentence) {
		if (sentence.hasRelationship(Primitive.INSTANTIATION, Primitive.PARAGRAPH) && sentence.hasRelationship(Primitive.SENTENCE)) {
			return sentence;
		}
		if (sentence.hasInverseRelationship(Primitive.INSTANTIATION, Primitive.PARAGRAPH)) {
			return sentence;
		}
		if (sentence.getData() instanceof String) {
			return createParagraph(sentence.getDataValue());
		}
		return sentence;
	}
	
	/**
	 * Tokenize the paragraph into its sentences and create a vertex representation.
	 */
	public Vertex createParagraph(String text) {
		TextStream stream = new TextStream(text);
		String current = stream.nextSentence();
		Vertex paragraph = null;
		Vertex previous = null;
		int index = 0;
		while (current != null) {
			if (current.length() > MAX_TEXT) {
				current = current.substring(0, MAX_TEXT);
			}
			Vertex sentence = createSentence(current);
			sentence.removeRelationship(Primitive.INSTANTIATION, Primitive.PARAGRAPH);
			String next = stream.nextSentence();
			if (paragraph == null) {
				if (next == null) {
					return sentence;
				}
				if (text.length() > MAX_TEXT) {
					paragraph = createVertex();
				} else {
					paragraph = createVertex(text);
					if (paragraph.hasRelationship(Primitive.INSTANTIATION, Primitive.PARAGRAPH) && paragraph.hasRelationship(Primitive.SENTENCE)) {
						return paragraph;
					}
				}
				paragraph.addRelationship(Primitive.INSTANTIATION, Primitive.PARAGRAPH);
			}
			paragraph.addRelationship(Primitive.SENTENCE, sentence, index);
			boolean learnGrammar = true;
			Language language = getBot().mind().getThought(Language.class);
			if (language != null) {
				learnGrammar = language.getLearnGrammar();
			}
			if (learnGrammar) {
				sentence.addRelationship(Primitive.PARAGRAPH, paragraph);
				if (previous != null) {
					sentence.addRelationship(Primitive.PREVIOUS, previous);
					previous.addRelationship(Primitive.NEXT, sentence);
				}
			}
			previous = sentence;
			current = next;
			index++;
		}
		if (paragraph == null) {
			return createSentence("");
		}
		return paragraph;
	}
	
	/**
	 * Tokenize the sentence into its words and create a vertex representation.
	 */
	public Vertex createSentence(String text) {
		return createSentence(text, false);
	}
	
	/**
	 * Tokenize the sentence pattern into its words and wildcards, and create a vertex representation.
	 */
	public Vertex createPattern(String text) {
		return createPattern(text, SelfCompiler.getCompiler());
	}
	
	/**
	 * Tokenize the sentence pattern into its words and wildcards, and create a vertex representation.
	 */
	public Vertex createPattern(String text, SelfCompiler compiler) {
		if (text.indexOf('"') != -1) {
			text = text.replace("\"", "\"\"");
		}
		String code = null;
		if (compiler.getVersion() <= 2) {
			code = "Pattern:\"" + text + "\"";
		} else {
			code = "Pattern(\"" + text + "\")";
		}
		Vertex pattern  = null;
		if (text.length() < MAX_TEXT) {
			pattern = createVertex(code);
			if (pattern.instanceOf(Primitive.PATTERN)) {
				Collection<Relationship> words = pattern.getRelationships(Primitive.WORD);
				if (words != null && words.size() == pattern.getWordCount()) {
					return pattern;
				}
			}
		} else {
			pattern = createVertex();
			pattern.setName(text);
		}
		pattern.addRelationship(Primitive.INSTANTIATION, Primitive.PATTERN);
		int index = 0;
		TextStream stream = new TextStream(text);
		String special = "*_#^$[]{}/";
		Vertex list = null;
		int listindex = 0;
		boolean precedence = false;
		Map<String, Map<String, Vertex>> elements = null;
		while (!stream.atEnd()) {
			String word = stream.nextWord();
			if (word != null) {
				Vertex element = null;
				if (word.equals("\\")) {
					// Escape next char
					String escape = stream.nextWord();
					if (escape != null && (special.indexOf(escape) != -1)) {
						word = escape;
						element = createWord(word);
						element.addRelationship(Primitive.PATTERN, pattern);
					} else {
						element = createWord(word);
						element.addRelationship(Primitive.PATTERN, pattern);
						if (list != null) {
							list.addRelationship(Primitive.ELEMENT, element, listindex);
							listindex++;
						} else {
							pattern.addRelationship(Primitive.WORD, element, index);
							index++;
						}
						word = escape;
						element = createWord(word);
						element.addRelationship(Primitive.PATTERN, pattern);
					}
				} else if (word.equals("$")) {
					precedence = true;
					continue;
				} else if (word.equals("*")) {
					element = createVertex(Primitive.WILDCARD);
				} else if (word.equals("_")) {
					element = createVertex(Primitive.UNDERSCORE);
				} else if (word.equals("^")) {
					element = createVertex(Primitive.HATWILDCARD);
				} else if (word.startsWith("#")) {
					// Primitives, pound wildcard
					if (word.length() > 1) {
						String name = word.substring(1, word.length());
						element = createVertex(new Primitive(name));
					} else {
						element = createVertex(Primitive.POUNDWILDCARD);
					}
				} else if (word.equals("[") || word.equals("(")) {
					// Lists
					element = createInstance(Primitive.ARRAY);
					if (word.equals("[")) {
						element.addRelationship(Primitive.TYPE, Primitive.REQUIRED);
					}
					list = element;
					listindex = 0;
				} else if (word.equals("]") || word.equals(")")) {
					list = null;
					continue;
				} else if (word.equals("{")) {
					// Self code
					if (elements == null) {
						elements = compiler.buildElementsMap(this);
					}
					element = compiler.parseElement(stream, elements, false, this);
					stream.skipWhitespace();
					compiler.ensureNext('}', stream);
				} else if (word.equals("/") && stream.peek() != ' ') {
					// Regex
					String expression = stream.upTo(' ');
					element = createInstance(Primitive.REGEX);
					element.setName(expression);
					element.addRelationship(Primitive.REGEX, createVertex(expression));
				} else if (word.equals("\"")) {
					// Fragment
					stream.skipQuotes();
					String fragmentText = stream.nextStringDoubleQuotes();
					element = createFragment(fragmentText);
					element.addRelationship(Primitive.PATTERN, pattern);
					stream.skip();
				} else {
					element = createWord(word);
					element.addRelationship(Primitive.PATTERN, pattern);
				}
				if (list != null && element != list) {
					list.addRelationship(Primitive.ELEMENT, element, listindex);
					listindex++;
				} else {
					Relationship relationship = pattern.addRelationship(Primitive.WORD, element, index);
					if (precedence) {
						createMeta(relationship).addRelationship(Primitive.TYPE, Primitive.PRECEDENCE);
						pattern.addRelationship(Primitive.TYPE, Primitive.PRECEDENCE);
					}
					index++;
				}
				precedence = false;
			}
		}
		Collection<Relationship> words = pattern.getRelationships(Primitive.WORD);
		if (words == null) {
			pattern.setWordCount(0);
		} else {
			pattern.setWordCount(words.size());
		}
		return pattern;
	}
	
	/**
	 * Tokenize the sentence into its words and create a vertex representation.
	 * If the sentence was generated, then don't add prev/next.
	 */
	public Vertex createSentence(String text, boolean generated) {
		return createSentence(text, generated, false);
	}
	
	/**
	 * Tokenize the sentence into its words and create a vertex representation.
	 * If the sentence was generated, then don't add prev/next.
	 */
	public Vertex createSentence(String text, boolean generated, boolean reduction) {
		return createSentence(text, generated, false, false);
	}
	
	/**
	 * Compile the template response.
	 */
	public Vertex createTemplate(String code) {
		Vertex formula = null;
		if (code.length() < MAX_TEXT) {
			formula = createVertex(code);
			if (formula.instanceOf(Primitive.FORMULA)) {
				return formula;
			}
			formula.addRelationship(Primitive.INSTANTIATION, Primitive.FORMULA);
		}
		TextStream stream = new TextStream(code);
		stream.setPosition(9);
		formula = SelfCompiler.getCompiler().parseTemplate(formula, stream, false, this);
		return formula;
	}
	
	/**
	 * Compile the forumla response.
	 */
	public Vertex createFormula(String code) {
		Vertex formula = null;
		if (code.length() < MAX_TEXT) {
			formula = createVertex(code);
			if (formula.instanceOf(Primitive.FORMULA)) {
				return formula;
			}
			formula.addRelationship(Primitive.INSTANTIATION, Primitive.FORMULA);
		}
		TextStream stream = new TextStream(code);
		stream.setPosition(8);
		formula = new SelfByteCodeCompiler().parseFormula(formula, stream, false, this);
		return formula;
	}
	
	/**
	 * Tokenize the sentence into its words and create a vertex representation.
	 * If the sentence was generated, then don't add prev/next.
	 */
	public Vertex createSentence(String text, boolean generated, boolean reduction, boolean whitespace) {
		if (text.length() > MAX_TEXT) {
			return createParagraph(text);
		}
		if ((text.length() >= 10) && ("pPfFtT<".indexOf(text.charAt(0)) != -1)) {
			String header = text.substring(0, 8);
			if (header.equalsIgnoreCase("PATTERN(")) {
				if (text.charAt(8) != '"') {
					throw new SelfParseException("Pattern must start with '\"' character - " + text);
				}
				if (text.charAt(text.length() - 1) != ')') {
					throw new SelfParseException("Pattern must end with ')' character - " + text);
				}
				if (text.charAt(text.length() - 2) != '"') {
					throw new SelfParseException("Pattern must end with '\")' characters - " + text);
				}
				return createPattern(text.substring(9, text.length() - 2));
			} else if (header.equalsIgnoreCase("PATTERN:")) {
				return createPattern(text.substring(9, text.length() - 1), new SelfByteCodeCompiler());
			} else if (header.equalsIgnoreCase("FORMULA:")) {
				return createFormula(text);
			} else if (header.equalsIgnoreCase("TEMPLATE") && text.charAt(8) == '(') {
				return createTemplate(text);
			} else if (text.substring(0, 10).equalsIgnoreCase("<template>")) {				
				Vertex formula = AIMLParser.parser().parseAIMLTemplate(text, this);
				if (formula != null) {
					return formula;
				}
			} else if ((text.substring(0, 9).equalsIgnoreCase("<pattern>") && (text.substring(text.length() - 10, text.length()).equalsIgnoreCase("</pattern>")))) {	
				return createPattern(text.substring(9, text.length() - 10));
			}
		}
		Vertex sentence = null;
		if (whitespace) {
			sentence = createVertex();
			sentence.setName(text);
		} else {
			sentence = createVertex(text);
			if (sentence.instanceOf(Primitive.SENTENCE)) {
				Collection<Relationship> words = sentence.getRelationships(Primitive.WORD);
				if (words != null && words.size() == sentence.getWordCount()) {
					return sentence;
				}
			}
		}
		sentence.addRelationship(Primitive.INSTANTIATION, Primitive.SENTENCE);
		parseFragment(sentence, text, generated, whitespace);
		return sentence;
	}
	
	/**
	 * Check if the sentence has been reduced, if not, then reduce.
	 */
	public void checkReduction(Vertex sentence) {
		if (!sentence.instanceOf(Primitive.SENTENCE) || sentence.instanceOf(Primitive.PATTERN)) {
			return;
		}
		String text = (String)sentence.getData();
		if (!sentence.hasRelationship(Primitive.REDUCTION) && !sentence.hasRelationship(Primitive.TYPE, Primitive.REDUCTION)) {
			String reduced = Utils.reduce(text);
			if (!text.equals(reduced) && !reduced.isEmpty()) {
				Vertex reduction = createSentence(reduced, true, true, false);
				if (sentence.isPinned()) {
					SelfCompiler.getCompiler().pin(reduction);
				}
				reduction.addRelationship(Primitive.TYPE, Primitive.REDUCTION);
				sentence.addRelationship(Primitive.REDUCTION, reduction);
			}
		}
	}
	
	/**
	 * Tokenize the fragment into its words and create a vertex representation.
	 */
	public Vertex createFragment(String text) {
		Vertex fragment = createVertex(text);
		if (!fragment.instanceOf(Primitive.FRAGMENT)) {
			fragment.addRelationship(Primitive.INSTANTIATION, Primitive.FRAGMENT);
		}
		Collection<Relationship> words = fragment.getRelationships(Primitive.WORD);
		if (words != null && words.size() == fragment.getWordCount()) {
			return fragment;
		}
		parseFragment(fragment, text, true, false);
		return fragment;
	}
	
	/**
	 * Tokenize the fragment into its words and create a vertex representation.
	 */
	public void parseFragment(Vertex fragment, String text, boolean generated, boolean whitespace) {
		boolean learnGrammar = true;
		Language language = getBot().mind().getThought(Language.class);
		if (language != null) {
			learnGrammar = language.getLearnGrammar();
		}
		TextStream stream = new TextStream(text);
		Vertex lastWord = null;
		Vertex nullValue = createVertex(Primitive.NULL);
		int index = 0;
		if (whitespace) {
			fragment.addRelationship(Primitive.TYPE, Primitive.SPACE);
		}
		while (!stream.atEnd()) {
			if (whitespace && (index > 0) && stream.skipWhitespace()) {
				fragment.addRelationship(Primitive.WORD, Primitive.SPACE, index);
				index++;
			}
			String wordText = stream.nextWord();
			if (wordText == null) {
				break;
			}
			Vertex word = createVertex(wordText);
			// Check if the word is a number.
			if ((wordText.length() > 0) && (Character.isDigit(wordText.charAt(0))
					|| ((wordText.length() > 1) && ((wordText.charAt(0) == '-') || (wordText.charAt(0) == '+'))))) {
				try {
					String numeric = wordText;
					if (numeric.indexOf(',') != -1) {
						numeric = numeric.replace(",", "");
					}
					Vertex number = null;
					if (wordText.indexOf('.') == -1) {
						BigInteger value = new BigInteger(numeric);
						// Create vertex associates back to the word.
						number = createVertex(value);
					} else {
						BigDecimal value = new BigDecimal(numeric);
						// Create vertex associates back to the word.
						number = createVertex(value);
					}
					word.addRelationship(Primitive.MEANING, number);
				} catch (NumberFormatException exception) {
					// Not a number.
				}
			// Check for URLs.
			} else if ((wordText.length() > 8) && wordText.substring(0, 7).equals("http://") || (wordText.length() > 9) && wordText.substring(0, 8).equals("https://")) {
				try {
					Vertex url = createVertex(new URI(wordText));
					url.addRelationship(Primitive.WORD, word);
					word.addRelationship(Primitive.INSTANTIATION, Primitive.URL);
					url.addRelationship(Primitive.INSTANTIATION, Primitive.URL);
					url.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
					word.addRelationship(Primitive.MEANING, url);
				} catch (Exception badURL) {
					// Ignore.
				}
			// Check for Twitter address
			} else if ((wordText.length() >= 2) && wordText.charAt(0) == '@') {
				try {
					Vertex speaker = createWord(wordText);
					word.addRelationship(Primitive.INSTANTIATION, Primitive.TWITTERADDRESS);
					word.addRelationship(Primitive.INSTANTIATION, Primitive.NOUN);
					word.addRelationship(Primitive.WORD, word);
					word.addRelationship(Primitive.MEANING, speaker);
				} catch (Exception badURL) {
					// Ignore.
				}
			// Check for email address
			} else if ((wordText.length() > 4) && (wordText.indexOf('@') != -1) && (wordText.indexOf('.') != -1)) {
				try {
					Vertex speaker = createWord(wordText);
					word.addRelationship(Primitive.INSTANTIATION, Primitive.EMAILADDRESS);
					word.addRelationship(Primitive.INSTANTIATION, Primitive.NOUN);
					word.addRelationship(Primitive.WORD, word);
					word.addRelationship(Primitive.MEANING, speaker);
				} catch (Exception badURL) {
					// Ignore.
				}
			}
			word.addRelationship(Primitive.INSTANTIATION, Primitive.WORD);
			if (!generated) {
				if (learnGrammar) {
					word.addRelationship(Primitive.SENTENCE, fragment);
					if (lastWord != null) {
						lastWord.addRelationship(Primitive.NEXT, word);
						word.addRelationship(Primitive.PREVIOUS, lastWord);
					} else {
						nullValue.addRelationship(Primitive.NEXT, word);
						word.addRelationship(Primitive.PREVIOUS, nullValue);
					}
				}
			}
			lastWord = word;
			fragment.addRelationship(Primitive.WORD, word, index);
			index++;
		}
		if (!generated && learnGrammar && (lastWord != null)) {
			lastWord.addRelationship(Primitive.NEXT, nullValue);
		}
		Collection<Relationship> words = fragment.getRelationships(Primitive.WORD);
		if (words == null) {
			fragment.setWordCount(0);
		} else {
			fragment.setWordCount(words.size());
		}
	}
	
	/**
	 * Create the word, and its meaning.
	 * If the word or meaning exist, use the existing one.
	 */
	public synchronized Vertex createObject(String name) {
		Vertex word = createWord(name);
		// TODO: handle multiple meanings.
		Vertex meaning = word.mostConscious(Primitive.MEANING);
		if (meaning == null) {
			meaning = createVertex();
			meaning.setName(name);
			log("Created meaning", Level.FINEST, word);
		} else {
			log("Existing meaning", Level.FINEST, word);
			return meaning;
		}
		word.addRelationship(Primitive.MEANING, meaning);
		meaning.addRelationship(Primitive.WORD, word);
		associateCaseInsensitivity(name, meaning);
		return meaning;
	}
	
	/**
	 * Create the primitive and associate the word to it.
	 */
	public synchronized Vertex createPrimitive(String name) {
		Vertex object = createVertex(new Primitive(name.toLowerCase().replace(' ', '_')));
		Vertex word = createWord(name);
		word.addRelationship(Primitive.MEANING, object);
		object.addRelationship(Primitive.WORD, word);
		associateCaseInsensitivity(name, object);
		return object;
	}


	/**
	 * Create the word, and a new meaning.
	 */
	public synchronized Vertex createNewObject(String name) {
		Vertex word = createFragment(name);
		Vertex meaning = createVertex();
		meaning.setName(name);
		log("Created meaning", Level.FINEST, word);
		//word.addRelationship(Primitive.MEANING, meaning);
		meaning.addRelationship(Primitive.WORD, word);
		//associateCaseInsensitivity(name, meaning);
		return meaning;
	}
	
	/**
	 * Find or create the speaker with the name.
	 */
	public synchronized Vertex createSpeaker(String name) {
		Vertex speaker = null;
		Vertex word = findByData(name);
		if (word != null) {
			speaker = word.getRelationship(Primitive.SPEAKER);
		}
		if (speaker == null) {
			speaker = createNewObject(Utils.capitalize(name));
			speaker.addRelationship(Primitive.INSTANTIATION, Primitive.SPEAKER);
			speaker.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
			if (!name.equals(TextEntry.DEFAULT_SPEAKER)) {
				speaker.addRelationship(Primitive.NAME, createVertex(Utils.capitalize(name)));
				word = createFragment(name);
				speaker.addRelationship(Primitive.WORD, word);
				word.addRelationship(Primitive.SPEAKER, speaker);
			}
		} else if (!speaker.hasRelationship(Primitive.NAME)) {
			speaker.addRelationship(Primitive.NAME, createFragment(Utils.capitalize(name)));
			if (!name.equals(TextEntry.DEFAULT_SPEAKER)) {
				speaker.addRelationship(Primitive.NAME, createVertex(Utils.capitalize(name)));
				word = createFragment(name);
				speaker.addRelationship(Primitive.WORD, word);
			}
		}
		return speaker;
	}
	
	/**
	 * Find or create the speaker from the unique id.
	 */
	public synchronized Vertex createUniqueSpeaker(Primitive id, Primitive type) {
		if (id.equals(Primitive.ANONYMOUS)) {
			Vertex speaker = createVertex();
			speaker.addRelationship(Primitive.INSTANTIATION, Primitive.SPEAKER);
			speaker.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
			speaker.addRelationship(Primitive.TYPE, type);
			return speaker;
		}
		Vertex identifier = createVertex(id);
		Vertex speaker = identifier.getRelationship(type);
		if (speaker == null) {
			speaker = createVertex();
			identifier.setRelationship(type, speaker);
			speaker.addRelationship(Primitive.INSTANTIATION, Primitive.SPEAKER);
			speaker.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
			speaker.addRelationship(Primitive.TYPE, type);
			speaker.addRelationship(Primitive.ID, createVertex(id.getIdentity()));
			identifier.setRelationship(type, speaker);
		}
		return speaker;
	}
	
	/**
	 * Find or create the speaker from the unique id.
	 */
	public synchronized Vertex createUniqueSpeaker(Primitive id, Primitive type, String name) {
		if (id.equals(Primitive.ANONYMOUS)) {
			Vertex speaker = createVertex();
			speaker.addRelationship(Primitive.INSTANTIATION, Primitive.SPEAKER);
			speaker.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
			speaker.addRelationship(Primitive.TYPE, type);
			speaker.addRelationship(Primitive.NAME, createFragment(Utils.capitalize(name)));
			Vertex word = createFragment(name);
			speaker.addRelationship(Primitive.WORD, word);
			return speaker;
		}
		Vertex identifier = createVertex(id);
		Vertex speaker = identifier.getRelationship(type);
		if (speaker == null) {
			speaker = createVertex();
			identifier.setRelationship(type, speaker);
			speaker.addRelationship(Primitive.INSTANTIATION, Primitive.SPEAKER);
			speaker.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
			speaker.addRelationship(Primitive.TYPE, type);
			speaker.addRelationship(Primitive.ID, createVertex(id.getIdentity()));
			speaker.addRelationship(Primitive.NAME, createFragment(Utils.capitalize(name)));
			Vertex word = createFragment(name);
			//word.addRelationship(Primitive.MEANING, speaker);
			speaker.addRelationship(Primitive.WORD, word);
			//associateCaseInsensitivity(name, speaker);
		} else if (!speaker.hasRelationship(Primitive.NAME)) {
			speaker.addRelationship(Primitive.NAME, createFragment(Utils.capitalize(name)));
			Vertex word = createFragment(name);
			speaker.addRelationship(Primitive.WORD, word);
		}
		return speaker;
	}
	
	/**
	 * Create a new anonymous speaker.
	 */
	public synchronized Vertex createAnonymousSpeaker() {
		String name = "anonymous";
		Vertex speaker = createVertex();
		Vertex word = createWord(name);
		speaker.addRelationship(Primitive.INSTANTIATION, Primitive.SPEAKER);
		speaker.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		speaker.addRelationship(Primitive.ASSOCIATED, Primitive.ANONYMOUS);
		//word.addRelationship(Primitive.MEANING, speaker);
		speaker.addRelationship(Primitive.WORD, word);
		//associateCaseInsensitivity(name, speaker);
		return speaker;
	}

	
	/**
	 * Create a timestamp based on the current nanos.
	 */
	public synchronized Vertex createTimestamp() {
		long nanos = System.nanoTime() % 1000000000;
		long millis = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(millis);
		timestamp.setNanos((int)nanos);
		return createVertex(timestamp);
	}
	
	/**
	 * Log the message if the debug level is greater or equal to the level.
	 */
	protected void log(String message, Level level, Object... arguments) {
		getBot().log(this, message, level, arguments);
	}

	/**
	 * Return the associated Bot instance.
	 */
	public Bot getBot() {
		return bot;
	}

	/**
	 * Set the associated Bot instance.
	 */
	public void setBot(Bot bot) {
		this.bot = bot;
	}

	public boolean isShortTerm() {
		return isShortTerm;
	}

	public void setShortTerm(boolean isShortTerm) {
		this.isShortTerm = isShortTerm;
	}
}
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
package org.botlibre.self;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.knowledge.Primitive;
import org.botlibre.knowledge.TextData;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

/**
 * Utility class for printing the Self programming model.
 * Self is the language that Bot programs herself in.
 */
public class SelfCompiler {
	public static int MAX_FILE_SIZE = 10000000; // 10meg
	public static int MAX_LOAD_SIZE = 20000;
	
	public static final String PRIMITIVE_TOKENS =" \t\n\r\f,:;!()?[]{}+=^&*\"`~|/\\<>";
	
	public static final String IF = "if";
	public static final String WHILE = "while";
	public static final String GREATER = "greater";
	public static final String LESS = "less";
	public static final String EQUAL = "equal";
	public static final String OR = "or";
	public static final String CASE = "case";
	public static final String PATTERN = "pattern";
	public static final String CALL = "call";
	public static final String SRAI = "srai";
	public static final String SRAIX = "sraix";
	public static final String REDIRECT = "redirect";
	public static final String REQUEST = "request";
	public static final String SERVICE = "service";
	public static final String LEARN = "learn";
	public static final String EVAL = "eval";
	public static final String FOR = "for";
	public static final String EACH = "each";
	public static final String ASSIGN = "assign";
	public static final String TO = "to";
	public static final String ON = "on";
	public static final String WITH = "with";
	public static final String BY = "by";
	public static final String OF = "of";
	public static final String AS = "as";
	public static final String AT = "at";
	public static final String LAST = "last";
	public static final String AND = "and";
	public static final String GET = "get";
	public static final String ALL = "all";
	public static final String COUNT = "count";
	public static final String SET = "set";
	public static final String EXCLUDE = "exclude";
	public static final String INCLUDE = "include";
	public static final String IS = "is";
	public static final String RELATED = "related";
	public static final String RELATION = "relation";
	public static final String WEAK = "weak";
	public static final String ASSOCIATE = "associate";
	public static final String WEAKASSOCIATE = "weakassociate";
	public static final String DISSOCIATE = "dissociate";
	public static final String META = "meta";
	public static final String DO = "do";
	public static final String THINK = "think";
	public static final String INPUT = "input";
	public static final String PART = "part";
	public static final String NEW = "new";
	public static final String APPEND = "append";
	public static final String NOT = "not";
	public static final String GOTO = "goto";
	public static final String RETURN = "return";
	public static final String TEMPLATE = "template";
	public static final String THAT = "that";
	public static final String TOPIC = "topic";
	public static final String QUOTIENT = "quotient";
	public static final String ANSWER = "answer";
	public static final String FROM = "from";
	public static final String ASSOCIATED = "associated";
	public static final String PREVIOUS = "previous";
	public static final String WORD = "word";
	public static final String SENTENCE = "sentence";
	public static final String UPPERCASE = "uppercase";
	public static final String LOWERCASE = "lowercase";
	public static final String FORMAT = "format";
	public static final String PRIMITIVE = "primitive";
	public static final String DEFINE = "define";
	public static final String RANDOM = "random";
	public static final String DEBUG = "debug";
	public static final String PUSH = "push";
	public static final String FINALLY = "finally";
	
	public static final String THEN = "then";
	public static final String ELSE = "else";
	
	public static List<String> OPERATORS;
	static {
		OPERATORS = new ArrayList<String>();
		OPERATORS.add(IF);
		OPERATORS.add(WHILE);
		OPERATORS.add(GREATER);
		OPERATORS.add(LESS);
		OPERATORS.add(EQUAL);
		OPERATORS.add(CALL);
		OPERATORS.add(SRAI);
		OPERATORS.add(SRAIX);
		OPERATORS.add(REDIRECT);
		OPERATORS.add(REQUEST);
		OPERATORS.add(FOR);
		OPERATORS.add(ASSIGN);
		OPERATORS.add(GET);
		OPERATORS.add(ALL);
		OPERATORS.add(COUNT);
		OPERATORS.add(SET);
		OPERATORS.add(IS);
		OPERATORS.add(RELATED);
		OPERATORS.add(WEAK);
		OPERATORS.add(ASSOCIATE);
		OPERATORS.add(DISSOCIATE);
		OPERATORS.add(DO);
		OPERATORS.add(NEW);
		OPERATORS.add(APPEND);
		OPERATORS.add(NOT);
		OPERATORS.add(GOTO);
		OPERATORS.add(RETURN);
		OPERATORS.add(WORD);
		OPERATORS.add(SENTENCE);
		OPERATORS.add(UPPERCASE);
		OPERATORS.add(LOWERCASE);
		OPERATORS.add(FORMAT);
		OPERATORS.add(PRIMITIVE);
		OPERATORS.add(DEFINE);
		OPERATORS.add(RANDOM);
		OPERATORS.add(DEBUG);
		OPERATORS.add(THINK);
		OPERATORS.add(INPUT);
		OPERATORS.add(LEARN);
		OPERATORS.add(EVAL);
	}
	
	public static final String VARIABLE = "variable";
	public static final String VERTEX = "vertex";
	public static final String VAR = ":";
	public static final String STATE = "state";
	public static final String EQUATION = "equation";
	public static final String FUNCTION = "function";
	public static final String FORMULA = "formula";
	
	public static List<String> TYPES;
	static {
		TYPES = new ArrayList<String>();
		TYPES.add(STATE);
		TYPES.add(VARIABLE);
		TYPES.add(VERTEX);
		TYPES.add(VAR);
		TYPES.add(EQUATION);
		TYPES.add(FUNCTION);
		TYPES.add(FORMULA);
		TYPES.add(PATTERN);
	}

	public static List<Primitive> PINNED;
	static {
		PINNED = new ArrayList<Primitive>();
		PINNED.add(Primitive.DO);
		PINNED.add(Primitive.FOR);
		PINNED.add(Primitive.GOTO);
		PINNED.add(Primitive.QUOTIENT);
		PINNED.add(Primitive.ARGUMENT);
		PINNED.add(Primitive.CONDITION);
		PINNED.add(Primitive.TEMPLATE);
		PINNED.add(Primitive.PATTERN);
		PINNED.add(Primitive.THAT);
		PINNED.add(Primitive.THEN);
		PINNED.add(Primitive.ELSE);
		PINNED.add(Primitive.AS);
		PINNED.add(Primitive.TOPIC);
		PINNED.add(Primitive.CASE);
		PINNED.add(Primitive.AS);
		PINNED.add(Primitive.SOURCECODE);
		PINNED.add(Primitive.BOT);
		PINNED.add(Primitive.BOTID);
		PINNED.add(Primitive.SERVER);
		PINNED.add(Primitive.SERVICE);
		PINNED.add(Primitive.APIKEY);
		PINNED.add(Primitive.HINT);
		PINNED.add(Primitive.DEFAULT);
	}
	
	protected static SelfCompiler compiler;

	public static SelfCompiler getCompiler() {
		if (compiler == null) {
			compiler = new SelfCompiler();
		}
		return compiler;
	}

	public static void setCompiler(SelfCompiler compiler) {
		SelfCompiler.compiler = compiler;
	}
	
	public static void addGlobalVariables(Vertex input, Vertex sentence, Network network, Map<Vertex, Vertex> variables) {		
		Vertex globals = network.createVertex(Primitive.INPUT_VARIABLE);
		variables.put(globals, input);
		Vertex relation = input.getRelationship(Primitive.SPEAKER);
		if (relation != null) {
			variables.put(globals.getRelationship(Primitive.SPEAKER), relation);
		}
		relation = input.getRelationship(Primitive.TARGET);
		if (relation != null) {
			variables.put(globals.getRelationship(Primitive.TARGET), relation);
		}
		if (sentence != null) {
			variables.put(globals.getRelationship(Primitive.INPUT), sentence);
		} else {
			relation = input.getRelationship(Primitive.INPUT);
			if (relation != null) {
				variables.put(globals.getRelationship(Primitive.INPUT), relation);
			}
		}
		relation = input.getRelationship(Primitive.CONVERSATION);
		if (relation != null) {
			variables.put(globals.getRelationship(Primitive.CONVERSATION), relation);
		}
	}
	
	/**
	 * Parse the code into a vertex state machine defined in the network.
	 */
	public Vertex parseStateMachine(String code, boolean debug, Network network) {
		TextStream stream = new TextStream(code);
		try {
			Map<String, Map<String, Vertex>> elements = buildElementsMap(network);
			List<String> comments = getComments(stream);
			Vertex state = parseState(stream, elements, debug, network);
			for (String comment : comments) {
				state.addRelationship(Primitive.COMMENT, network.createVertex(comment));
			}
			TextData text = new TextData();
			text.setText(code);
			state.addRelationship(Primitive.SOURCECODE, network.createVertex(text));
			network.getBot().log(this, "Compiled new state machine", Level.INFO, state);
			return state;
		} catch (SelfParseException exception) {
			throw exception;
		} catch (Exception exception) {
			network.getBot().log(this, exception);
			throw new SelfParseException("Parsing error occurred", stream, exception);
		}
	}
	
	/**
	 * Parse and evaluate the code.
	 */
	public Vertex evaluateEquation(String code, Vertex speaker, Vertex target, boolean debug, Network network) {
		Vertex equation = SelfCompiler.getCompiler().parseEquation(code, speaker, target, debug, network);
		Map<Vertex, Vertex> variables = new HashMap<Vertex, Vertex>();
		return equation.applyQuotient(variables);
	}
	
	/**
	 * Parse the code into a vertex equation defined in the network.
	 */
	public Vertex parseEquation(String code, Vertex speaker, Vertex target, boolean debug, Network network) {
		TextStream stream = new TextStream(code);
		try {
			Map<String, Map<String, Vertex>> elements = new HashMap<String, Map<String, Vertex>>();
			elements.put(VARIABLE, new HashMap<String, Vertex>());
			elements.get(VARIABLE).put("speaker", speaker);
			elements.get(VARIABLE).put("target", target);
			elements.put(EQUATION, new HashMap<String, Vertex>());
			List<String> comments = getComments(stream);
			Vertex equation = null;
			String peek = stream.peekWord();
			if (peek.equalsIgnoreCase(EQUATION) || peek.equalsIgnoreCase(FUNCTION)) {
				equation = parseEquation(stream, elements, debug, network);
			} else {
				equation = network.createInstance(Primitive.EQUATION);
				Vertex operator = network.createVertex(Primitive.DO);
				equation.addRelationship(Primitive.OPERATOR, operator);
				stream.skipWhitespace();
				stream.skipWhitespace();
				Vertex element = parseElement(stream, elements, debug, network);
				equation.addRelationship(Primitive.ARGUMENT, element, 0);				
			}
			for (String comment : comments) {
				equation.addRelationship(Primitive.COMMENT, network.createVertex(comment));
			}
			network.getBot().log(this, "Compiled new equation", Level.INFO, equation);
			return equation;
		} catch (SelfParseException exception) {
			throw exception;
		} catch (Exception exception) {
			network.getBot().log(this, exception);
			throw new SelfParseException("Parsing error occurred", stream, exception);
		}
	}
	
	/**
	 * Get the contents of the URL to a .self file and parse it.
	 */
	public Vertex parseStateMachine(URL url, String encoding, boolean debug, Network network) {
		try {
			return parseStateMachine(Utils.openStream(url), debug, network, encoding, MAX_FILE_SIZE);
		} catch (IOException exception) {
			throw new SelfParseException("Parsing error occurred", exception);
		}
	}
	
	/**
	 * Get the contents of the URL to a .self file and parse it.
	 */
	public Vertex parseStateMachine(File file, String encoding, boolean debug, Network network) {
		try {
			return parseStateMachine(new FileInputStream(file), debug, network, encoding, MAX_FILE_SIZE);
		} catch (IOException exception) {
			throw new SelfParseException("Parsing error occurred", exception);
		}
	}
	
	/**
	 * Get the contents of the stream to a .self file and parse it.
	 */
	public Vertex parseStateMachine(InputStream stream, boolean debug, Network network, String encoding, int maxSize) {
		String text = Utils.loadTextFile(stream, encoding, maxSize);
		return parseStateMachine(text, debug, network);
	}
	
	public List<String> getComments(TextStream stream) {
		List<String> comments = new ArrayList<String>();
		while (!stream.atEnd()) {
			stream.skipWhitespace();
			String comment = stream.peek(2);
			if (comment.equals("//"))  {
				comments.add(stream.nextLine());
			} else {
				return comments;
			}
		}
		return comments;
	}

	public void pin(Vertex element) {
		Set<Vertex> processed = new HashSet<Vertex>();
		pin(element, PINNED, element.getId(), processed);
	}
	
	public void pin(Vertex element, List<Primitive> relations, long groupId, Set<Vertex> processed) {
		if (processed.contains(element)) {
			return;
		}
		processed.add(element);
		element.setPinned(true);
		element.setGroupId(groupId);
		if (element.instanceOf(Primitive.VARIABLE)) {
			for (Iterator<Relationship> iterator = element.allRelationships(); iterator.hasNext(); ) {
				Vertex variable = iterator.next().getTarget();
				variable.setPinned(true);
				if (variable.instanceOf(Primitive.VARIABLE)) {
					pin(element, relations, groupId, processed);
				}
			}
		} else if (element.instanceOf(Primitive.FORMULA)) {
			Collection<Relationship> relationships = element.getRelationships(Primitive.WORD);
			if (relationships != null) {
				for (Relationship relationship : relationships) {
					pin(relationship.getTarget(), relations, groupId, processed);
				}
			}
		} else {
			for (Primitive primitive : relations) {
				Collection<Relationship> relationships = element.getRelationships(primitive);
				boolean isDo = primitive.equals(Primitive.DO);
				if (relationships != null) {
					for (Relationship relationship : relationships) {
						if (isDo) {
							relationship.setPinned(true);
						}
						pin(relationship.getTarget(), relations, groupId, processed);
					}
				}
			}
		}
	}

	public void fastUnpin(Vertex state) {
		state.getNetwork().getBot().log(this, "Fast unpin state machine", Level.INFO, state);
		if (state.getGroupId() == 0) {
			unpin(state);
		} else {
			int rowCount = state.getNetwork().executeNativeQuery("update vertex set pinned = false where groupId = " + state.getGroupId());
			if (rowCount == 0) {
				unpin(state);
			}
		}
	}

	public void fastLoad(Vertex state) {
		if (state.getGroupId() != 0) {
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("groupId", state.getGroupId());
			state.getNetwork().findAllQuery("Select v from Vertex v where v.groupId = :groupId", parameters, MAX_LOAD_SIZE, 0);
		}
	}

	public void fastLoadChildren(Vertex state) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("parent", state.detach());
		state.getNetwork().findAllQuery("Select c from Vertex c, Relationship r where r.target = c and r.source = :parent", parameters, MAX_LOAD_SIZE, 0);
	}

	public void unpin(Vertex element) {
		element.getNetwork().getBot().log(this, "Unpin state machine", Level.INFO, element);
		Set<Vertex> processed = new HashSet<Vertex>();
		unpin(element, PINNED, processed);
	}
	
	public void unpin(Vertex element, List<Primitive> relations, Set<Vertex> processed) {
		if (processed.contains(element)) {
			return;
		}
		if (element.isPrimitive()) {
			return;
		}
		if (!element.isPinned()) {
			return;
		}
		processed.add(element);
		if (!element.hasData() || element.instanceOf(Primitive.PATTERN)) {
			element.setPinned(false);
		}
		if (element.instanceOf(Primitive.VARIABLE)) {
			for (Iterator<Relationship> iterator = element.allRelationships(); iterator.hasNext(); ) {
				Vertex variable = iterator.next().getTarget();
				if (variable.instanceOf(Primitive.VARIABLE)) {
					unpin(element, relations, processed);
				}
			}
		} else if (element.instanceOf(Primitive.FORMULA)) {
			Collection<Relationship> relationships = element.getRelationships(Primitive.WORD);
			for (Relationship relationship : relationships) {
				if (relationship.getTarget().instanceOf(Primitive.EQUATION)) {
					unpin(relationship.getTarget(), relations, processed);
				}
			}
		} else {
			for (Primitive primitive : relations) {
				Collection<Relationship> relationships = element.getRelationships(primitive);
				if (relationships != null) {
					for (Relationship relationship : relationships) {
						if (relationship.isPinned()) {
							relationship.setPinned(false);
						}
						unpin(relationship.getTarget(), relations, processed);
					}
				}
			}
		}
		if (element.instanceOf(Primitive.STATE) || element.instanceOf(Primitive.EQUATION)) {
			element.internalRemoveAllRelationships();
		}
	}
	
	/**
	 * Parse the state and any referenced states or variables.
	 */
	public Vertex parseState(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		List<String> comments = null;
		Vertex state = parseElement(stream, elements, debug, network);
		stream.skipWhitespace();
		ensureNext('{', stream);
		stream.skipWhitespace();
		String element = stream.peekWord();
		while (!("}".equals(element))) {
			if (element == null) {
				throw new SelfParseException("Unexpected end of state, missing '}'", stream);
			}
			Vertex vertex = state;
			element = element.toLowerCase();
			if (element.equals(CASE)) {
				vertex = parseCase(stream, elements, debug, network);
				state.addRelationship(Primitive.DO, vertex);
			} else if (element.equals(PATTERN)) {
				vertex = parsePattern(stream, elements, debug, network);
				state.addRelationship(Primitive.DO, vertex);
			} else if (element.equals(STATE)) {
				vertex = parseState(stream, elements, debug, network);
			} else if (element.equals(VAR) || element.equals(VARIABLE)) {
				vertex = parseVariable(stream, elements, debug, network);
			} else if (element.equals(QUOTIENT) || element.equals(ANSWER)) {
				parseQuotient(state, stream, elements, debug, network);
			} else if (element.equals(EQUATION) || element.equals(FUNCTION)) {
				vertex = parseEquation(stream, elements, debug, network);
			} else if (element.equals(DO)) {
				vertex = network.createInstance(Primitive.DO);
				Vertex equation = network.createInstance(Primitive.EQUATION);
				vertex.addRelationship(Primitive.DO, equation);
				parseOperator(equation, stream, elements, debug, network);
				state.addRelationship(Primitive.DO, vertex);
				ensureNext(';', stream);
			} else if (element.equals(GOTO)) {
				vertex = parseGoto(stream, elements, debug, network);
				state.addRelationship(Primitive.DO, vertex);
				ensureNext(';', stream);
			} else if (element.equals(PUSH)) {
				vertex = parsePush(stream, elements, debug, network);
				state.addRelationship(Primitive.DO, vertex);
				ensureNext(';', stream);
			} else if (element.equals(RETURN)) {
				vertex = parseReturn(stream, elements, debug, network);
				state.addRelationship(Primitive.DO, vertex);
				ensureNext(';', stream);
			} else if (element.equals("/")) {
				comments = getComments(stream);
				if (comments.isEmpty()) {
					throw new SelfParseException("Unknown element: " + element, stream);					
				}
				vertex = null; // Associate the comments with the next element parsed.
			} else {
				throw new SelfParseException("Unknown element: " + element, stream);
			}
			if ((comments != null) && (vertex != null)) {
				for (String comment : comments) {
					vertex.addRelationship(Primitive.COMMENT, network.createVertex(comment));
				}	
				comments = null;
			}
			element = stream.peekWord();
		}
		ensureNext('}', stream);
		return state;
	}

	/**
	 * Parse the quotient.
	 * QUOTIENT:0.5:"World" { previous is "Hello"; previous is not "Hi"; }
	 */
	public void parseQuotient(Vertex state, TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		stream.nextWord();
		ensureNext(':', stream);
		float correctness = 1.0f;
		if (Character.isDigit(stream.peek())) {
			String correctnessText = stream.upTo(':');
			try {
				correctness = Float.valueOf(correctnessText);
			} catch (NumberFormatException exception) {
				throw new SelfParseException("Invalid correctness: " + correctnessText, stream);
			}
			ensureNext(':', stream);
		}
		Vertex value = parseElement(stream, elements, debug, network);
		Relationship relationship = state.addWeakRelationship(Primitive.QUOTIENT, value, correctness);
		stream.skipWhitespace();
		if (stream.peek() == '{') {
			Vertex meta = network.createMeta(relationship);
			stream.skip();
			String next = stream.nextWord();
			while (!("}".equals(next))) {
				if (next == null) {
					throw new SelfParseException("Unexpected end of quotient, missing '}'", stream);				
				}
				next = next.toLowerCase();
				if (!(PREVIOUS.equals(next))) {
					throw new SelfParseException("Unexpected word: '" + next + "' expected 'PREVIOUS'", stream);				
				}
				ensureNext("is", stream);
				boolean not = false;
				next = stream.peekWord();
				if (NOT.equals(next)) {
					not = true;
					stream.nextWord();
				}				
				Vertex previous = parseElement(stream, elements, debug, network);				
				ensureNext(';', stream);
				if (not) {
					meta.removeRelationship(Primitive.PREVIOUS, previous);
				} else {
					meta.addRelationship(Primitive.PREVIOUS, previous);
				}
				next = stream.nextWord();
			}
		}
		ensureNext(';', stream);
	}

	/**
	 * Parse the reference to either a state, variable, equation, or raw data.
	 * One of,
	 * STATE:1234("name"), VARIABLE:1234("name"), EQUATION:1234("name"),
	 * 1234, "string", DATE("1972,01,01"), ...
	 */
	@SuppressWarnings("unchecked")
	public Vertex parseElement(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		List<String> comments = getComments(stream);
		stream.skipWhitespace();
		boolean bracket = false;
		if (stream.peek() == '(') {
			bracket = true;
			stream.skip();
			stream.skipWhitespace();
		}
		try {
			// Check if reference or data.
			String token = stream.peekWord();
			if (token == null) {
				throw new SelfParseException("Unexpected end, element expected", stream);
			}
			token = token.toLowerCase();
			if (token.equals(FUNCTION)) {
				token = EQUATION;
			}
			if (OPERATORS.contains(token)) {
				Vertex equation = network.createInstance(Primitive.EQUATION);
				//equation.setName(token); - cannot set name as named functions can return
				for (String comment : comments) {
					equation.addRelationship(Primitive.COMMENT, network.createVertex(comment));
				}
				parseOperator(equation, stream, elements, debug, network);
				if (debug) {
					String source = stream.currentLine();
					int lineNumber = stream.currentLineNumber();
					equation.addRelationship(Primitive.SOURCE, network.createVertex(source));
					equation.addRelationship(Primitive.LINE_NUMBER, network.createVertex(lineNumber));
				}
				return equation;
			} else if (TYPES.contains(token)) {
				stream.nextWord();
				if (token.equals(VAR)) {
					token = VARIABLE;
				} else {
					ensureNext(':', stream);
				}
				if (token.equals(FORMULA)) {
					return parseFormula(null, stream, elements, debug, network);
				}
				if (token.equals(PATTERN)) {
					ensureNext('"', stream);
					return network.createPattern(stream.nextQuotesExcludeDoubleQuote());
				}
				Long id = null;
				// Check for id or name.
				if (Character.isDigit(stream.peek())) {
					String idText = stream.nextWord();
					try {
						id = Long.valueOf(idText);
					} catch (NumberFormatException exception) {
						throw new SelfParseException("Invalid id: " + idText, stream);
					}
				}
				// Check for #, primitive variable word short cut.
				boolean isPrimitiveShortCut = false;
				boolean isInstanceShortCut = false;
				char peek = stream.peek();
				if ('#' == peek) {
					isPrimitiveShortCut = true;
					stream.skip();
					peek = stream.peek();
				} else if ('^' == peek) {
					isInstanceShortCut = true;
					stream.skip();
					peek = stream.peek();
				}
				String name = null;
				if ((id == null) || (peek == ':')) {
					if (id != null) {
						stream.skip();
					}
					name = stream.nextWord();
				}
				Vertex vertex = null;
				Map<String, Vertex> elementsForType = elements.get(token);
				if (name != null) {
					if (elementsForType != null) {
						vertex = elementsForType.get(name);
						if (vertex != null) {
							return vertex;
						}
					}
				}
				if (id != null) {
					vertex = network.findById(id);
					if (vertex == null) {
						throw new SelfParseException("Id element reference not found: " + id, stream);
					}
					if ((elementsForType != null) && (name != null)) {
						elementsForType.put(name, vertex);
					}
					return vertex;
				}
				if (token.equals(STATE)) {
					vertex = network.createInstance(Primitive.STATE);
					vertex.setName(name);
				} else if (token.equals(VARIABLE)) {
					vertex = network.createInstance(Primitive.VARIABLE);
					vertex.setName(name);
					if (isPrimitiveShortCut) {
						vertex.addRelationship(Primitive.MEANING, new Primitive(name));
					}
					if (isInstanceShortCut) {
						Vertex meaning = network.createInstance(Primitive.VARIABLE);
						meaning.addRelationship(Primitive.INSTANTIATION, new Primitive(name));
						vertex.addRelationship(Primitive.MEANING, meaning);
					}
				} else if (token.equals(EQUATION)) {
					vertex = network.createInstance(Primitive.EQUATION);
					vertex.setName(name);
				} else {
					throw new SelfParseException("Invalid element: " + token, stream);
				}
				if (name != null) {
					elementsForType = elements.get(token);
					if (elementsForType != null) { 
						elementsForType.put(name, vertex);
					}
				}
				return vertex;
			}
			char next = stream.peek();
			try {
				if (next == '#') {
					stream.skip();
					String data = stream.upToAny(PRIMITIVE_TOKENS);
					return network.createVertex(new Primitive(data));
				} else if (next == '"') {
					stream.skip();
					String data = stream.nextQuotesExcludeDoubleQuote();
					return network.createVertex(data);
				} else if (Character.isDigit(next) || next == '-' || next == '+') {
					String data = stream.nextWord();
					return network.createVertex(new BigInteger(data));				
				} else {
					String dataType = stream.upTo('(', false, true);
					if (dataType.isEmpty()) {
						throw new SelfParseException("Invalid element: " + stream.nextWord(), stream);
					}
					String word = stream.nextWord();
					if (word.equals("(")) {
						throw new SelfParseException("Invalid element: " + dataType, stream);
					}
					word = stream.nextWord();
					if (word.equals("\"")) {
						throw new SelfParseException("Invalid element: " + dataType, stream);
					}
					String dataValue = stream.upTo('"', false, true);
					ensureNext('"', stream);
					while ('"' == stream.peek()) {
						dataValue = dataValue + "\"" + stream.upTo('"', false, true);
						ensureNext('"', stream);
					}
					ensureNext(')', stream);
					Object data = null;
					if (dataType.equalsIgnoreCase("DATE")) {
						data = Utils.parseDate(dataValue);
					} else if (dataType.equalsIgnoreCase("TIME")) {
						data = Utils.parseTime(dataValue);
					} else if (dataType.equalsIgnoreCase("TIMESTAMP")) {
						data = Utils.parseTimestamp(dataValue);
					} else{				
						Class<Object> typeClass = (Class<Object>)Class.forName(dataType);							
						data = typeClass.getConstructor(String.class).newInstance(dataValue);
					}
					return network.createVertex(data);
				}
			} catch (SelfParseException exception) {
				throw exception;
			} catch (Exception exception) {
				throw new SelfParseException("Invalid data: " + next, stream, exception);
			}
		} finally {
			if (bracket) {
				stream.skipWhitespace();
				ensureNext(')', stream);
			}
		}
	}
	
	/**
	 * Throw a parse error if the next character does not match what is expected.
	 */
	public void ensureNext(char expected, TextStream stream) {
		ensureNext(expected, expected, stream);
	}
	
	/**
	 * Throw a parse error if the next character does not match what is expected.
	 */
	public void ensureNext(char expected, char other, TextStream stream) {
		stream.skipWhitespace();
		if (stream.atEnd()) {
			throw SelfParseException.unexpectedEndOfFile(expected, stream);			
		}
		char next = stream.next();
		if ((next != expected) && (next != other)) {
			throw SelfParseException.invalidCharacter(next, expected, stream);
		}
	}
	
	/**
	 * Throw a parse error if the next word does not match what is expected.
	 */
	public void ensureNext(String expected, TextStream stream) {
		stream.skipWhitespace();
		String next = stream.nextWord().toLowerCase();
		if (!expected.equals(next)) {
			throw SelfParseException.invalidWord(next, expected, stream);
		}
	}
	
	/**
	 * Throw a parse error if the number of arguments does not match what is expected.
	 */
	protected void ensureArguments(String operator, int expected, List<Vertex> arguments, TextStream stream) {
		if (arguments.size() != expected) {
			throw new SelfParseException("'" + operator + "' requires " + expected + " arguments not: " + arguments.size(), stream);
		}
	}
	
	/**
	 * Return true if the next character matches what is expected.
	 */
	protected boolean checkNext(char expected, TextStream stream) {
		stream.skipWhitespace();
		char next = stream.peek();
		if (next == expected) {
			stream.skip();
			return true;
		}
		return false;
	}
		
	/**
	 * Parse the variable.
	 */
	public Vertex parseVariable(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		Vertex variable = parseElement(stream, elements, debug, network);
		stream.skipWhitespace();
		ensureNext('{', stream);
		String next = stream.nextWord();
		while (!("}".equals(next))) {
			if (next == null) {
				throw new SelfParseException("Unexpected end of variable, missing '}'", stream);				
			}
			next = next.toLowerCase();
			if (!(SET.equals(next) || EXCLUDE.equals(next) || INCLUDE.equals(next))) {
				throw new SelfParseException("Unexpected word: '" + next + "' expected 'SET', 'EXCLUDE', or 'INCLUDE'", stream);				
			}
			Vertex type = parseElement(stream, elements, debug, network);
			String token = stream.peekWord().toLowerCase();
			if (";".equals(token)) {
				ensureNext(';', stream);
				if (!EXCLUDE.equals(next)) {
					variable.addRelationship(Primitive.EQUALS, type);
				} else {
					variable.removeRelationship(Primitive.EQUALS, type);
				}				
			} else {
				if (TO.equals(token) || FROM.equals(token)) {
					stream.nextWord();
				}
				Vertex target = parseElement(stream, elements, debug, network);
				ensureNext(';', stream);
				if (!EXCLUDE.equals(next)) {
					variable.addRelationship(type, target);
				} else {
					variable.removeRelationship(type, target);				
				}
			}
			next = stream.nextWord();
		}
		return variable;
	}
	
	/**
	 * Parse the arguments to the equation.
	 */
	protected List<Vertex> parseArguments(Vertex equation, Primitive type, int index, TextStream stream, Map<String, Map<String, Vertex>> elements, boolean bracket, boolean debug, Network network) {
		List<Vertex> arguments = new ArrayList<Vertex>();
		if (!bracket) {
			bracket = checkNext('(', stream);
		}
		boolean moreArguments = true;
		stream.skipWhitespace();
		char peek = stream.peek();
		if (peek == ')') {
			moreArguments = false;
		}
		while (moreArguments) {
			stream.skipWhitespace();
			peek = stream.peek();
			if (peek == ')' || peek == '}') {
				break;
			}
			if ((peek == ',') || (peek == ';'))  {
				break;
			}
			Vertex argument = parseElement(stream, elements, debug, network);
			arguments.add(argument);
			equation.addRelationship(type, argument, index);
			if (!bracket) {
				break;
			}
			stream.skipWhitespace();
			peek = stream.peek();
			if ((peek == ',') || (peek == ';'))  {
				stream.skip();
			} else {
				moreArguments = false;
			}
			index++;
		}
		if (bracket) {
			ensureNext(')', stream);
		}
		return arguments;
	}
	
	/**
	 * Parse the equation.
	 */
	public Vertex parseEquation(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		Vertex equation = parseElement(stream, elements, debug, network);
		stream.skipWhitespace();
		ensureNext('{', stream);
		Vertex operator = network.createVertex(Primitive.DO);
		equation.addRelationship(Primitive.OPERATOR, operator);
		stream.skipWhitespace();
		char peek = stream.peek();
		List<Vertex> operations = new ArrayList<Vertex>();
		while (peek != '}') {
			stream.skipWhitespace();
			Vertex element = parseElement(stream, elements, debug, network);
			operations.add(element);
			ensureNext(';', ',', stream);
			stream.skipWhitespace();
			peek = stream.peek();
		}
		boolean unravel = false;
		// Unravel nested DO
		if (operations.size() == 1) {
			Vertex operation = operations.get(0);
			operator = operation.getRelationship(Primitive.OPERATOR);
			if (operator != null && operator.is(Primitive.DO)  && !operation.instanceOf(Primitive.EQUATION)) {
				unravel = true;
				int index = 0;
				for (Vertex nestedOperation : operation.orderedRelations(Primitive.ARGUMENT)) {
					equation.addRelationship(Primitive.ARGUMENT, nestedOperation, index);
					index++;					
				}
			}
		}
		if (!unravel) {
			int index = 0;
			for (Vertex operation : operations) {
				equation.addRelationship(Primitive.ARGUMENT, operation, index);
				index++;					
			}			
		}
		ensureNext('}', stream);
		return equation;
	}

	/**
	 * Parse the equation.
	 */
	public Vertex parseFormula(Vertex formula, TextStream stream, boolean debug, Network network) {
		Map<String, Map<String, Vertex>> elements = buildElementsMap(network);
		return parseFormula(formula, stream, elements, debug, network);
	}
	
	public Map<String, Map<String, Vertex>> buildElementsMap(Network network) {
		Map<String, Map<String, Vertex>> elements = new HashMap<String, Map<String, Vertex>>();
		Map<String, Vertex> variables = new HashMap<String, Vertex>();
		elements.put(VARIABLE, variables);
		elements.put(STATE, new HashMap<String, Vertex>());
		elements.put(EQUATION, new HashMap<String, Vertex>());
		elements.put(FORMULA, new HashMap<String, Vertex>());
		
		Vertex input = network.createVertex(Primitive.INPUT_VARIABLE);
		Bootstrap.checkInputVariable(input, network);
		variables.put("input", input);
		variables.put("star", network.createVertex(Primitive.WILDCARD));
		variables.put("thatstar", network.createVertex(Primitive.THATWILDCARD));
		variables.put("topicstar", network.createVertex(Primitive.TOPICWILDCARD));
		variables.put("speaker", input.getRelationship(Primitive.SPEAKER));
		variables.put("target", input.getRelationship(Primitive.TARGET));
		variables.put("sentence", input.getRelationship(Primitive.INPUT));
		variables.put("conversation", input.getRelationship(Primitive.CONVERSATION));
		return elements;
	}
	
	/**
	 * Parse the equation.
	 */
	public Vertex parseFormula(Vertex formula, TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {		
		String name = "Formula:";
		stream.skipWhitespace();
		ensureNext('"', stream);
		int position = stream.getPosition();
		String text = stream.nextQuotes();
		Map<String, Vertex> cache = elements.get(FORMULA);
		if (formula == null && cache != null) {
			formula = cache.get(text);
			if (formula != null) {
				return formula;
			}
		}
		try {
			TextStream formulaStream = new TextStream(text);
			if (formula == null) {
				formula = network.createInstance(Primitive.FORMULA);
			}
			if (cache != null) {
				cache.put(text, formula);
			}
			String token = formulaStream.nextWord();
			char peek = formulaStream.peek();
			int index = 0;
			Vertex space = network.createVertex(Primitive.SPACE);
			formula.addRelationship(Primitive.TYPE, space);
			while ((token != null) && ((!token.equals("\"") || (peek == '"')))) {
				Vertex word = null;
				if (token.equals("{")) {
					word = parseElement(formulaStream, elements, debug, network);
					formulaStream.skipWhitespace();
					ensureNext('}', formulaStream);
				} else {
					word = network.createWord(token);
				}
				formula.addRelationship(Primitive.WORD, word, index);
				if (token.equals("\"") && (peek == '"')) {
					formulaStream.skip();
				}
				if (formulaStream.skipWhitespace()) {
					index++;
					formula.addRelationship(Primitive.WORD, space, index);
				}
				token = formulaStream.nextWord();
				peek = formulaStream.peek();
				index++;
			}
		} catch (SelfParseException exception) {
			int newPosition = stream.getPosition();
			stream.setPosition(position);
			exception.initFromStream(stream);
			stream.setPosition(newPosition);
			throw exception;
		}
		formula.setName(name + "\"" + text + "\"");
		return formula;
	}
	
	/**
	 * Parse the operator.
	 */
	public Vertex parseOperator(Vertex equation, TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		String next = stream.nextWord();
		next = next.toLowerCase();
		if (!OPERATORS.contains(next)) {
			throw new SelfParseException("Invalid operator: '" + next + "' valid operators are: " + OPERATORS, stream);						
		}
		if (next.equals(IS)) {
			next = Primitive.RELATION.getIdentity();
		} else if (next.equals(FOR)) {
			ensureNext(EACH, stream);
		} else if (next.equals(WEAK)) {
			ensureNext(ASSOCIATE, stream);
			next = WEAKASSOCIATE;
		} else if (next.equals(RELATED)) {
			ensureNext(TO, stream);
		}
		String last = next.toLowerCase();
		Vertex operator = network.createVertex(new Primitive(next));
		equation.addRelationship(Primitive.OPERATOR, operator);
		stream.skipWhitespace();
		next = lower(stream.peekWord());
		if (NOT.equals(next)) {
			stream.nextWord();
			equation.addRelationship(Primitive.NOT, Primitive.NOT);
		}
		List<Vertex> arguments = parseArguments(equation, Primitive.ARGUMENT, 0, stream, elements, false, debug, network);
		if (last.equals(IF)) {
			if (arguments.size() != 1) {
				ensureArguments(IF, 2, arguments, stream);
			}
			next = lower(stream.peekWord());
			List<Vertex> stack = new ArrayList<Vertex>();
			stack.add(equation);
			Vertex top = equation;
			while (OR.equals(next) || AND.equals(next)) {
				boolean or = OR.equals(next);
				boolean and = AND.equals(next);
				stream.nextWord();
				Vertex condition = network.createInstance(Primitive.EQUATION);
				next = lower(stream.peekWord());
				if (NOT.equals(next)) {
					stream.nextWord();
					condition.addRelationship(Primitive.NOT, Primitive.NOT);
					next = lower(stream.peekWord());
				}
				boolean bracket = false;
				while ("(".equals(next)) {
					bracket = true;
					stack.add(condition);
					stream.nextWord();
					next = lower(stream.peekWord());
				}
				if (or) {
					condition.addRelationship(Primitive.OPERATOR, Primitive.OR);
				} else if (and) {
					condition.addRelationship(Primitive.OPERATOR, Primitive.AND);
				}
				top.addRelationship(Primitive.CONDITION, condition);
				parseArguments(condition, Primitive.ARGUMENT, 0, stream, elements, bracket, debug, network);
				next = lower(stream.peekWord());
				if (bracket) {
					stack.remove(stack.size() - 1);
					top = stack.get(stack.size() - 1);
					while (")".equals(next)) {
						stack.remove(stack.size() - 1);
						top = stack.get(stack.size() - 1);
						stream.nextWord();
						next = lower(stream.peekWord());
					}
				}
			}
			if (THEN.equals(next)) {
				stream.nextWord();
				parseArguments(equation, Primitive.THEN, 0, stream, elements, false, debug, network);
				next = lower(stream.peekWord());					
			}
			if (ELSE.equals(next)) {
				stream.nextWord();
				parseArguments(equation, Primitive.ELSE, 0, stream, elements, false, debug, network);
			}
		} else if (last.equals(WHILE)) {
			if (arguments.size() != 1) {
				ensureArguments(WHILE, 2, arguments, stream);
			}
			ensureNext(DO, stream);
			parseArguments(equation, Primitive.DO, 0, stream, elements, false, debug, network);
		} else if (last.equals(FOR)) {
			ensureArguments(FOR, 1, arguments, stream);
			ensureNext(OF, stream);
			arguments = parseArguments(equation, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(OF, 1, arguments, stream);
			ensureNext(AS, stream);
			arguments = parseArguments(equation, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
			ensureArguments(AS, 1, arguments, stream);
			next = lower(stream.peekWord());
			int index = 3;
			while (AND.equals(next)) {
				stream.nextWord();
				ensureNext(EACH, stream);
				arguments = parseArguments(equation, Primitive.ARGUMENT, index++, stream, elements, false, debug, network);
				ensureArguments(EACH, 1, arguments, stream);
				ensureNext(OF, stream);
				arguments = parseArguments(equation, Primitive.ARGUMENT, index++, stream, elements, false, debug, network);
				ensureArguments(OF, 1, arguments, stream);
				ensureNext(AS, stream);
				arguments = parseArguments(equation, Primitive.ARGUMENT, index++, stream, elements, false, debug, network);
				ensureArguments(AS, 1, arguments, stream);
				next = lower(stream.peekWord());
			}
			if (DO.equals(next)) {
				stream.nextWord();
				parseArguments(equation, Primitive.DO, 0, stream, elements, false, debug, network);
			}
		} else if (last.equals(GREATER)) {
			ensureArguments(GREATER, 2, arguments, stream);
		} else if (last.equals(LESS)) {
			ensureArguments(LESS, 2, arguments, stream);
		} else if (last.equals(EQUAL)) {
			ensureArguments(EQUAL, 2, arguments, stream);
		} else if (last.equals(GET)) {
			ensureArguments(GET, 1, arguments, stream);
			ensureNext(FROM, stream);
			arguments = parseArguments(equation, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(FROM, 1, arguments, stream);
			next = stream.peekWord();
			if ((next != null) && ASSOCIATED.equals(next.toLowerCase())) {
				stream.nextWord();
				next = lower(stream.peekWord());
				ensureNext(TO, stream);
				arguments = parseArguments(equation, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
				ensureArguments(TO, 1, arguments, stream);
				ensureNext(BY, stream);
				arguments = parseArguments(equation, Primitive.ARGUMENT, 3, stream, elements, false, debug, network);
				ensureArguments(BY, 1, arguments, stream);
			} else if ((next != null) && AT.equals(next.toLowerCase())) {
				stream.nextWord();
				next = lower(stream.peekWord());
				if ((next != null) && LAST.equals(next.toLowerCase())) {
					stream.nextWord();
					arguments = parseArguments(equation, Primitive.LASTINDEX, 0, stream, elements, false, debug, network);
					ensureArguments(AT, 1, arguments, stream);
				} else {
					arguments = parseArguments(equation, Primitive.INDEX, 0, stream, elements, false, debug, network);
					ensureArguments(AT, 1, arguments, stream);
				}
			}
		} else if (last.equals(LEARN)) {
			ensureArguments(LEARN, 1, arguments, stream);
			next = stream.peekWord();
			if ((next != null) && THAT.equals(next.toLowerCase())) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.THAT, 0, stream, elements, false, debug, network);
				ensureArguments(THAT, 1, arguments, stream);
				next = stream.peekWord();
			}
			if ((next != null) && TOPIC.equals(next.toLowerCase())) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.TOPIC, 0, stream, elements, false, debug, network);
				ensureArguments(TOPIC, 1, arguments, stream);
			}
			ensureNext(TEMPLATE, stream);
			arguments = parseArguments(equation, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(TEMPLATE, 1, arguments, stream);
		} else if (last.equals(INPUT)) {
			ensureArguments(INPUT, 1, arguments, stream);
			next = stream.peekWord();
			int forIndex = 1;
			if ((next != null) && PART.equals(next.toLowerCase())) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
				ensureArguments(PART, 1, arguments, stream);
				next = stream.peekWord();
				forIndex = 2;
			}
			if ((next != null) && FOR.equals(next.toLowerCase())) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.ARGUMENT, forIndex, stream, elements, false, debug, network);
				ensureArguments(FOR, 1, arguments, stream);
			}
		} else if (last.equals(ALL)) {
			ensureArguments(ALL, 1, arguments, stream);
			ensureNext(FROM, stream);
			arguments = parseArguments(equation, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(FROM, 1, arguments, stream);
			next = stream.peekWord();
			if ((next != null) && ASSOCIATED.equals(next.toLowerCase())) {
				stream.nextWord();
				next = lower(stream.peekWord());
				ensureNext(TO, stream);
				arguments = parseArguments(equation, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
				ensureArguments(TO, 1, arguments, stream);
				ensureNext(BY, stream);
				arguments = parseArguments(equation, Primitive.ARGUMENT, 3, stream, elements, false, debug, network);
				ensureArguments(BY, 1, arguments, stream);
			}
		} else if (last.equals(COUNT)) {
			ensureArguments(COUNT, 1, arguments, stream);
			next = stream.peekWord();
			if ((next != null) && OF.equals(next.toLowerCase())) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
				ensureArguments(OF, 1, arguments, stream);
			}
		} else if (last.equals(SET)) {
			ensureArguments(last, 1, arguments, stream);
			ensureNext(TO, stream);
			arguments = parseArguments(equation, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(TO, 1, arguments, stream);
			next = lower(stream.peekWord());
			if (ON.equals(next)) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
				ensureArguments(ON, 1, arguments, stream);
			}
		} else if (last.equals(RELATION)) {
			ensureArguments(IS, 1, arguments, stream);
			ensureNext(RELATED, stream);
			ensureNext(TO, stream);
			arguments = parseArguments(equation, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(RELATED, 1, arguments, stream);
			next = lower(stream.peekWord());
			if (BY.equals(next)) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
				ensureArguments(BY, 1, arguments, stream);
			}
		} else if (last.equals(RELATED)) {
			ensureArguments(RELATED, 1, arguments, stream);
			next = lower(stream.peekWord());
			if (BY.equals(next)) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
				ensureArguments(BY, 1, arguments, stream);
			}
		} else if (last.equals(ASSOCIATE) || last.equals(DISSOCIATE) || last.equals(WEAKASSOCIATE)) {
			ensureArguments(last, 1, arguments, stream);
			ensureNext(TO, stream);
			arguments = parseArguments(equation, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(TO, 1, arguments, stream);
			ensureNext(BY, stream);
			arguments = parseArguments(equation, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
			ensureArguments(BY, 1, arguments, stream);
			next = lower(stream.peekWord());
			if (WITH.equals(next)) {
				stream.nextWord();
				ensureNext(META, stream);
				arguments = parseArguments(equation, Primitive.ARGUMENT, 3, stream, elements, false, debug, network);
				ensureArguments(META, 1, arguments, stream);
				ensureNext(AS, stream);
				arguments = parseArguments(equation, Primitive.ARGUMENT, 4, stream, elements, false, debug, network);
				ensureArguments(AS, 1, arguments, stream);
			}
		} else if (last.equals(ASSIGN)) {
			ensureArguments(ASSIGN, 1, arguments, stream);
			ensureNext(TO, stream);
			arguments = parseArguments(equation, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(TO, 1, arguments, stream);
		} else if (last.equals(DEFINE)) {
			ensureArguments(ASSIGN, 1, arguments, stream);
			ensureNext(AS, stream);
			arguments = parseArguments(equation, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
		} else if (last.equals(EVAL)) {
			ensureArguments(EVAL, 1, arguments, stream);
		} else if (last.equals(NOT)) {
			ensureArguments(NOT, 1, arguments, stream);
		} else if (last.equals(APPEND)) {
			ensureArguments(APPEND, 1, arguments, stream);
			ensureNext(TO, stream);
			arguments = parseArguments(equation, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(TO, 1, arguments, stream);
			ensureNext(OF, stream);
			arguments = parseArguments(equation, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
			ensureArguments(OF, 1, arguments, stream);
			next = lower(stream.peekWord());
			if (WITH.equals(next)) {
				stream.nextWord();
				ensureNext(META, stream);
				arguments = parseArguments(equation, Primitive.ARGUMENT, 3, stream, elements, false, debug, network);
				ensureArguments(META, 1, arguments, stream);
				ensureNext(AS, stream);
				arguments = parseArguments(equation, Primitive.ARGUMENT, 4, stream, elements, false, debug, network);
				ensureArguments(AS, 1, arguments, stream);
			}
		} else if (last.equals(CALL)) {
			ensureArguments(CALL, 1, arguments, stream);
			ensureNext(ON, stream);
			arguments = parseArguments(equation, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(ON, 1, arguments, stream);
			next = lower(stream.peekWord());
			if (WITH.equals(next)) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
			}
		} else if (last.equals(FORMAT)) {
			ensureNext(AS, stream);
			arguments = parseArguments(equation, Primitive.AS, 1, stream, elements, false, debug, network);
			ensureArguments(AS, 1, arguments, stream);
		} else if (last.equals(SRAI) || last.equals(REDIRECT)) {
			ensureArguments(SRAI, 1, arguments, stream);
		} else if (last.equals(SRAIX)  || last.equals(REQUEST)) {
			ensureArguments(SRAI, 1, arguments, stream);
			next = lower(stream.peekWord());
			if ("bot".equals(next)) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.BOT, 0, stream, elements, false, debug, network);
			}
			next = lower(stream.peekWord());
			if ("botid".equals(next)) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.BOTID, 0, stream, elements, false, debug, network);
			}
			next = lower(stream.peekWord());
			if ("service".equals(next)) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.SERVICE, 0, stream, elements, false, debug, network);
			}
			next = lower(stream.peekWord());
			if ("server".equals(next)) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.SERVER, 0, stream, elements, false, debug, network);
			}
			next = lower(stream.peekWord());
			if ("apikey".equals(next)) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.APIKEY, 0, stream, elements, false, debug, network);
			}
			next = lower(stream.peekWord());
			if ("limit".equals(next)) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.LIMIT, 0, stream, elements, false, debug, network);
			}
			next = lower(stream.peekWord());
			if ("hint".equals(next)) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.HINT, 0, stream, elements, false, debug, network);
			}
			next = lower(stream.peekWord());
			if ("default".equals(next)) {
				stream.nextWord();
				arguments = parseArguments(equation, Primitive.DEFAULT, 0, stream, elements, false, debug, network);
			}
		}
		return equation;
	}
	
	public String lower(String token) {
		if (token == null) {
			return null;
		}
		return token.toLowerCase();
	}
	
	/**
	 * Parse the IF condition.
	 */
	public Vertex parseCase(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		stream.nextWord();
		stream.skipWhitespace();
		Vertex equation = network.createInstance(Primitive.CASE);
		Vertex variable = parseElement(stream, elements, debug, network);
		equation.addRelationship(Primitive.CASE, variable);
		String next = stream.nextWord().toLowerCase();
		if (next.equals(AS)) {
			Vertex as = parseElement(stream, elements, debug, network);
			equation.addRelationship(Primitive.AS, as);
			next = stream.nextWord().toLowerCase();
		}
		if (next.equals(TOPIC)) {
			Vertex topic = parseElement(stream, elements, debug, network);
			equation.addRelationship(Primitive.TOPIC, topic);
			next = stream.nextWord().toLowerCase();
		}
		if (next.equals(THAT)) {
			Vertex template = parseElement(stream, elements, debug, network);
			equation.addRelationship(Primitive.THAT, template);
			next = stream.nextWord().toLowerCase();
		}
		if (next.equals(GOTO)) {
			List<Vertex> thens = new ArrayList<Vertex>();
			stream.skipWhitespace();
			boolean parseGoto = true;
			while (parseGoto) {
				thens.add(parseElement(stream, elements, debug, network));
				stream.skipWhitespace();
				if (stream.peek() == ',') {
					stream.skip();
				} else {
					parseGoto = false;
				}
			}
			for (Vertex then : thens) {
				equation.addRelationship(Primitive.GOTO, then);				
			}
		} else if (next.equals(TEMPLATE) || next.equals(ANSWER)) {
			Vertex template = parseElement(stream, elements, debug, network);
			equation.addRelationship(Primitive.TEMPLATE, template);
		} else if (next.equals(RETURN)) {
			equation.addRelationship(Primitive.GOTO, Primitive.RETURN);
		} else {
			stream.setPosition(stream.getPosition() - next.length());
			throw new SelfParseException("expected one of GOTO, TEMPLATE, ANSWER, RETURN, THAT, TOPIC, found: " + next, stream);
		}
		next = stream.peekWord().toLowerCase();
		if (next.equals(FOR)) {
			stream.nextWord();
			ensureNext(EACH, stream);
			equation.addRelationship(Primitive.FOR, parseElement(stream, elements, debug, network));
			ensureNext(OF, stream);
			equation.addRelationship(Primitive.FOR, parseElement(stream, elements, debug, network));
		}
		ensureNext(';', stream);
		return equation;
	}
	
	/**
	 * Parse the PATTERN condition.
	 */
	public Vertex parsePattern(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		stream.nextWord();
		stream.skipWhitespace();
		Vertex equation = network.createInstance(Primitive.CASE);
		Vertex pattern = null;
		if (stream.peek() == '"') {
			stream.skip();
			pattern = network.createPattern(stream.nextQuotesExcludeDoubleQuote());
		} else {
			pattern = parseElement(stream, elements, debug, network);
		}
		equation.addRelationship(Primitive.PATTERN, pattern);
		String next = stream.nextWord().toLowerCase();
		if (next.equals(TOPIC)) {
			Vertex topic = parseElement(stream, elements, debug, network);
			equation.addRelationship(Primitive.TOPIC, topic);
			next = stream.nextWord().toLowerCase();
		}
		if (next.equals(THAT)) {
			Vertex that = null;
			stream.skipWhitespace();
			if (stream.peek() == '"') {
				stream.skip();
				that = network.createPattern(stream.nextQuotesExcludeDoubleQuote());
			} else {
				that = parseElement(stream, elements, debug, network);
			}
			equation.addRelationship(Primitive.THAT, that);
			next = stream.nextWord().toLowerCase();
		}
		if (next.equals(GOTO)) {
			List<Vertex> thens = new ArrayList<Vertex>();
			stream.skipWhitespace();
			boolean parseGoto = true;
			while (parseGoto) {
				thens.add(parseElement(stream, elements, debug, network));
				stream.skipWhitespace();
				if (stream.peek() == ',') {
					stream.skip();
				} else {
					parseGoto = false;
				}
			}
			for (Vertex then : thens) {
				equation.addRelationship(Primitive.GOTO, then);				
			}
		} else if (next.equals(RETURN)) {
			equation.addRelationship(Primitive.GOTO, Primitive.RETURN);
		} else if (next.equals(TEMPLATE) || next.equals(ANSWER)) {
			Vertex template = parseElement(stream, elements, debug, network);
			equation.addRelationship(Primitive.TEMPLATE, template);
		} else {
			stream.setPosition(stream.getPosition() - next.length());
			throw new SelfParseException("expected one of GOTO, TEMPLATE, RETURN, THAT, TOPIC, found: " + next, stream);
		}
		ensureNext(';', stream);
		return equation;
	}
	
	/**
	 * Parse the RETURN condition.
	 */
	public Vertex parseReturn(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		stream.nextWord();
		stream.skipWhitespace();
		Vertex equation = network.createInstance(Primitive.RETURN);
		if (stream.peek() != ';') {
			boolean with = stream.peekWord().toLowerCase().equals(WITH);
			if (!with) {
				Vertex result = parseElement(stream, elements, debug, network);
				equation.addRelationship(Primitive.RETURN, result);
				stream.skipWhitespace();
				with = stream.peekWord().toLowerCase().equals(WITH);
			}
			if (with) {
				stream.skipWord();
				stream.skipWhitespace();
				if (stream.peek() == '(') {
					stream.skip();
					stream.skipWhitespace();
					Vertex argument = parseElement(stream, elements, debug, network);
					equation.addRelationship(Primitive.ARGUMENT, argument);
					stream.skipWhitespace();
					while (stream.peek() == ',') {
						stream.skip();
						stream.skipWhitespace();
						argument = parseElement(stream, elements, debug, network);
						equation.addRelationship(Primitive.ARGUMENT, argument);
					}
					ensureNext(')', stream);
				} else {
					Vertex argument = parseElement(stream, elements, debug, network);
					equation.addRelationship(Primitive.ARGUMENT, argument);
				}
			}
		}
		return equation;
	}
	
	/**
	 * Parse the GOTO condition.
	 */
	public Vertex parseGoto(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		stream.nextWord();
		Vertex equation = network.createInstance(Primitive.GOTO);
		stream.skipWhitespace();
		boolean gotoFinally = stream.peekWord().toLowerCase().equals(FINALLY);
		if (gotoFinally) {
			stream.nextWord();
			equation.addRelationship(Primitive.FINALLY, Primitive.FINALLY);			
		}
		Vertex value = parseElement(stream, elements, debug, network);
		equation.addRelationship(Primitive.GOTO, value);
		if (stream.peek() != ';') {
			if (stream.peekWord().toLowerCase().equals(WITH)) {
				stream.skipWord();
				stream.skipWhitespace();
				if (stream.peek() == '(') {
					stream.skip();
					stream.skipWhitespace();
					Vertex argument = parseElement(stream, elements, debug, network);
					equation.addRelationship(Primitive.ARGUMENT, argument);
					stream.skipWhitespace();
					while (stream.peek() == ',') {
						stream.skip();
						stream.skipWhitespace();
						argument = parseElement(stream, elements, debug, network);
						equation.addRelationship(Primitive.ARGUMENT, argument);
					}
					ensureNext(')', stream);
				} else {
					Vertex argument = parseElement(stream, elements, debug, network);
					equation.addRelationship(Primitive.ARGUMENT, argument);
				}
			}
		}
		return equation;
	}
	
	/**
	 * Parse the PUSH condition.
	 */
	public Vertex parsePush(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		stream.nextWord();
		Vertex equation = network.createInstance(Primitive.PUSH);
		Vertex value = parseElement(stream, elements, debug, network);
		equation.addRelationship(Primitive.ARGUMENT, value);
		return equation;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}

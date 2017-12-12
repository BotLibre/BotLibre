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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.aiml.AIMLParser;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.BinaryData;
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.knowledge.Primitive;
import org.botlibre.knowledge.TextData;
import org.botlibre.thought.language.Language;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

/**
 * Self scripting language compiler.
 * This compiler compiles Self to state, function and expression knowledge objects.
 */
public class Self4Compiler extends SelfCompiler {

	public static final String NULL = "null";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String UNKNOWN = "unknown";
	public static final String BREAK = "break";
	public static final String CONTINUE = "continue";
	public static final String VAR = "var";
	public static final String EQUALS = "equals";
	public static final String NOTEQUAL = "not-equal";
	public static final String ADD = "add";
	public static final String REMOVE = "remove";
	public static final String ANY = "any";
	public static final String NONE = "none";
	public static final String NOT = "!";
	public static final String LESSTHAN = "lessthan";
	public static final String GREATERTHAN = "greaterthan";
	public static final String LESSTHANEQUAL = "lessthanequal";
	public static final String GREATERTHANEQUAL = "greaterthanequal";
	public static final String SYMBOL = "symbol";
	public static final String OBJECT = "object";
	public static final String EVALCOPY = "evalcopy";
	public static final String DATE = "date";
	public static final String TIME = "time";
	public static final String BINARY = "binary";
	public static final String TEXT = "text";
	public static final String TIMESTAMP = "timestamp";
	public static final String NUMBER = "number";
	public static final String INCREMENT = "increment";
	public static final String DECREMENT = "decrement";
	
	public static List<String> OPERATORS;
	static {
		OPERATORS = new ArrayList<String>();
		OPERATORS.add(IF);
		OPERATORS.add(WHILE);
		OPERATORS.add(FOR);
		OPERATORS.add(DO);
		OPERATORS.add(NEW);
		OPERATORS.add(GOTO);
		OPERATORS.add(RETURN);
		OPERATORS.add(RANDOM);
		OPERATORS.add(DEBUG);
		OPERATORS.add(SRAI);
		OPERATORS.add(SRAIX);
		OPERATORS.add(REDIRECT);
		OPERATORS.add(REQUEST);
		OPERATORS.add(NOT);
		OPERATORS.add(SYMBOL);
		OPERATORS.add(THINK);
		OPERATORS.add(EVAL);
		OPERATORS.add(EVALCOPY);
		OPERATORS.add(LEARN);
	}

	public static Map<String, Primitive> BINARY_OPERATORS;
	static {
		BINARY_OPERATORS = new HashMap<String, Primitive>();
		BINARY_OPERATORS.put("==", Primitive.EQUALS);
		BINARY_OPERATORS.put("!=", Primitive.NOTEQUALS);
		BINARY_OPERATORS.put("||", Primitive.OR);
		BINARY_OPERATORS.put("&&", Primitive.AND);
		BINARY_OPERATORS.put(">", Primitive.GREATERTHAN);
		BINARY_OPERATORS.put("<", Primitive.LESSTHAN);
		BINARY_OPERATORS.put(">=", Primitive.GREATERTHANEQUAL);
		BINARY_OPERATORS.put("<=", Primitive.LESSTHANEQUAL);
		BINARY_OPERATORS.put("-", Primitive.MINUS);
		BINARY_OPERATORS.put("+", Primitive.PLUS);
		BINARY_OPERATORS.put("*", Primitive.MULTIPLY);
		BINARY_OPERATORS.put("/", Primitive.DIVIDE);
	}

	public static List<Primitive> BINARY_PRECEDENCE;
	static {
		BINARY_PRECEDENCE = new ArrayList<Primitive>();
		BINARY_PRECEDENCE.add(Primitive.OR);
		BINARY_PRECEDENCE.add(Primitive.AND);
		BINARY_PRECEDENCE.add(Primitive.EQUALS);
		BINARY_PRECEDENCE.add(Primitive.NOTEQUALS);
		BINARY_PRECEDENCE.add(Primitive.GREATERTHAN);
		BINARY_PRECEDENCE.add(Primitive.LESSTHAN);
		BINARY_PRECEDENCE.add(Primitive.GREATERTHANEQUAL);
		BINARY_PRECEDENCE.add(Primitive.LESSTHANEQUAL);
		BINARY_PRECEDENCE.add(Primitive.MINUS);
		BINARY_PRECEDENCE.add(Primitive.PLUS);
		BINARY_PRECEDENCE.add(Primitive.MULTIPLY);
		BINARY_PRECEDENCE.add(Primitive.DIVIDE);
	}
		
	public static List<String> TYPES;
	static {
		TYPES = new ArrayList<String>();
		TYPES.add(STATE);
		TYPES.add(VARIABLE);
		TYPES.add(VERTEX);
		TYPES.add(VAR);
		TYPES.add(FUNCTION);
		TYPES.add(TEMPLATE);
		TYPES.add(PATTERN);
		TYPES.add(OBJECT);
		TYPES.add(DATE);
		TYPES.add(TIME);
		TYPES.add(TIMESTAMP);
		TYPES.add(BINARY);
		TYPES.add(TEXT);
	}

	@Override
	public int getVersion() {
		return 4;
	}
	
	/**
	 * Parse the code into a vertex state machine defined in the network.
	 */
	@Override
	public Vertex parseStateMachine(String code, boolean debug, Network network) {
		TextStream stream = new TextStream(code);
		try {
			Map<String, Map<String, Vertex>> elements = buildElementsMap(network);
			List<String> comments = getComments(stream);
			stream.skipWhitespace();
			Vertex state = null;
			if (stream.peek(6).equalsIgnoreCase("state:")) {
				state = new SelfCompiler().parseState(stream, elements, debug, network);
				state.addRelationship(Primitive.LANGUAGE, network.createVertex(Primitive.SELF));
				state.addRelationship(Primitive.LANGUAGE, network.createVertex(Primitive.SELF2));
			} else {
				state = parseState(stream, elements, debug, network);
				state.addRelationship(Primitive.LANGUAGE, network.createVertex(Primitive.SELF));
				state.addRelationship(Primitive.LANGUAGE, network.createVertex(Primitive.SELF4));
			}
			if (debug) {
				for (String comment : comments) {
					state.addRelationship(Primitive.COMMENT, network.createVertex(comment), Integer.MAX_VALUE);
				}
			}
			// Avoid setting source code to reduce file size.
			/*TextData text = new TextData();
			text.setText(code);
			Vertex sourceCode = network.createVertex(text);
			sourceCode.setPinned(true);
			state.addRelationship(Primitive.SOURCECODE, sourceCode);*/
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
	public Vertex evaluateExpression(String code, Vertex speaker, Vertex target, boolean debug, Network network) {
		Vertex expression = parseExpressionForEvaluation(code, speaker, target, debug, network);
		Map<Vertex, Vertex> variables = new HashMap<Vertex, Vertex>();
		return SelfInterpreter.getInterpreter().evaluateExpression(expression, variables, network, System.currentTimeMillis(), Language.MAX_STATE_PROCESS, 0);
	}

	/**
	 * Parse the code into a temporary expression so it can be evaluated.
	 */
	public Vertex parseExpressionForEvaluation(String code, Vertex speaker, Vertex target, boolean debug, Network network) {
		TextStream stream = new TextStream(code);
		try {
			Map<String, Map<String, Vertex>> elements = new HashMap<String, Map<String, Vertex>>();
			elements.put(VARIABLE, new HashMap<String, Vertex>());
			elements.get(VARIABLE).put("speaker", speaker);
			elements.get(VARIABLE).put("target", target);
			elements.put(FUNCTION, new HashMap<String, Vertex>());
			getComments(stream);
			// Create a temporary equation to execute.
			Vertex expression = network.createTemporyVertex();
			expression.addRelationship(Primitive.INSTANTIATION, Primitive.EXPRESSION);
			expression.addRelationship(Primitive.OPERATOR, Primitive.DO);
			stream.skipWhitespace();
			Vertex element = parseElement(stream, elements, debug, network);
			expression.addRelationship(Primitive.DO, element, Integer.MAX_VALUE);
			stream.skipWhitespace();
			while (stream.peek() == ';') {
				stream.skip();
				stream.skipWhitespace();
				if (stream.atEnd()) {
					break;
				}
				element = parseElement(stream, elements, debug, network);
				expression.addRelationship(Primitive.DO, element, Integer.MAX_VALUE);
				stream.skipWhitespace();
			}
			if (!stream.atEnd()) {
				throw new SelfParseException("Unexpect element " + stream.peekWord(), stream);
			}
			network.getBot().log(this, "Compiled new expression for evaluation", Level.INFO, expression);
			return expression;
		} catch (SelfParseException exception) {
			throw exception;
		} catch (Exception exception) {
			network.getBot().log(this, exception);
			throw new SelfParseException("Parsing error occurred", stream, exception);
		}
	}

	@Override
	public List<String> getComments(TextStream stream) {
		List<String> comments = new ArrayList<String>();
		while (!stream.atEnd()) {
			stream.skipWhitespace();
			String comment = stream.peek(2);
			if (comment.equals("//"))  {
				comments.add(stream.nextLine());
			} else if (comment.equals("/*"))  {
				comments.add(stream.upToAll("*/", true) + "\n");
			} else {
				return comments;
			}
		}
		return comments;
	}
	
	/**
	 * Parse the state and any referenced states or variables.
	 */
	@Override
	public Vertex parseState(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		List<String> comments = null;
		String next = stream.nextWord();
		if (next == null || !next.equalsIgnoreCase("state")) {
			throw new SelfParseException("Expecting state not: " + next, stream);			
		}
		Vertex state = parseElementName(Primitive.STATE, stream, elements, debug, network);
		if (!elements.containsKey("root")) {
			HashMap<String, Vertex> root = new HashMap<String, Vertex>(1);
			root.put("root", state);
			elements.put("root", root);
		}
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
				state.addRelationship(Primitive.DO, vertex, Integer.MAX_VALUE);
			} else if (element.equals(PATTERN)) {
				vertex = parsePattern(stream, elements, debug, network);
			} else if (element.equals(STATE)) {
				vertex = parseState(stream, elements, debug, network);
			} else if (element.equals(VAR) || element.equals(VARIABLE)) {
				vertex = parseVariable(stream, elements, debug, network);
			} else if (element.equals(ANSWER)) {
				parseAnswer(state, stream, elements, debug, network);
			} else if (element.equals(FUNCTION) || element.equals(EQUATION)) {
				vertex = parseFunction(stream, elements, debug, network);
			} else if (element.equals(DO)) {
				vertex = network.createInstance(Primitive.DO);
				Vertex expression = parseOperator(stream, elements, debug, network);
				vertex.addRelationship(Primitive.DO, expression, Integer.MAX_VALUE);
				state.addRelationship(Primitive.DO, vertex, Integer.MAX_VALUE);
			} else if (element.equals(GOTO)) {
				vertex = parseGoto(stream, elements, debug, network);
				state.addRelationship(Primitive.DO, vertex, Integer.MAX_VALUE);
				ensureNext(';', stream);
			} else if (element.equals(PUSH)) {
				vertex = parsePush(stream, elements, debug, network);
				state.addRelationship(Primitive.DO, vertex, Integer.MAX_VALUE);
				ensureNext(';', stream);
			} else if (element.equals(RETURN)) {
				vertex = parseReturn(stream, elements, debug, network);
				state.addRelationship(Primitive.DO, vertex, Integer.MAX_VALUE);
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
			if (debug && (comments != null) && (vertex != null)) {
				for (String comment : comments) {
					vertex.addRelationship(Primitive.COMMENT, network.createVertex(comment), Integer.MAX_VALUE);
				}	
				comments = null;
			}
			stream.skipWhitespace();
			element = stream.peekWord();
		}
		ensureNext('}', stream);
		return state;
	}

	/**
	 * Parse the quotient.
	 * answer:0.5 "World" { previous "Hello"; previous ! "Hi"; }
	 */
	public void parseAnswer(Vertex state, TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		stream.nextWord();
		stream.skipWhitespace();
		float correctness = 1.0f;
		if (stream.peek() == ':') {
			stream.skip();
			stream.skipWhitespace();
			if (Character.isDigit(stream.peek())) {
				String correctnessText = stream.nextWord();
				try {
					correctness = Float.valueOf(correctnessText);
				} catch (NumberFormatException exception) {
					throw new SelfParseException("Invalid correctness: " + correctnessText, stream);
				}
			}
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
	 * Parse the reference to either a state, variable, expression, or data.
	 * One of,
	 * state:1234:name, variable:1234:name, function:1234:name,
	 * var variable = "value", variable.attribute = "value", function(), function(arg, arg2).attribute = variable
	 * if (value == "value") {}, for () {}, new (class1, class2), variable.function()
	 * 1234, "string", 'string', #primitive, DATE("1972,01,01"), ...
	 */
	@Override
	public Vertex parseElement(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		return parseElement(stream, elements, null, debug, network);
	}

	/**
	 * Parse the reference to either a state, variable, expression, or data.
	 * One of,
	 * state:1234:name, variable:1234:name, function:1234:name,
	 * var variable = "value", variable.attribute = "value", function(), function(arg, arg2).attribute = variable
	 * if (value == "value") {}, for () {}, new (class1, class2), variable.function()
	 * 1234, "string", 'string', #primitive, DATE("1972,01,01"), ...
	 */
	public Vertex parseElement(TextStream stream, Map<String, Map<String, Vertex>> elements, Primitive lastBinary, boolean debug, Network network) {
		List<String> comments = getComments(stream);
		stream.skipWhitespace();
		int brackets = 0;
		while (stream.peek() == '(') {
			lastBinary = null;
			brackets++;
			stream.skip();
			stream.skipWhitespace();
		}
		Vertex expression = null;
		String source = "";
		int lineNumber = 0;
		if (debug) {
			source = stream.currentLine();
			lineNumber = stream.currentLineNumber();
		}
		if (stream.peek() == '[') {
			stream.skip();
			// Parse array.
			Vertex array = network.createInstance(Primitive.ARRAY);
			stream.skipWhitespace();
			if (stream.peek() == ']') {
				stream.skip();
				array.addRelationship(Primitive.LENGTH, network.createVertex(0));
				return array;
			}
			boolean more = true;
			int index = 0;
			while (more) {
				Vertex element = parseElement(stream, elements, debug, network);
				array.addRelationship(Primitive.ELEMENT, element, index);
				index++;
				stream.skipWhitespace();
				if (stream.peek() == ',') {
					stream.skip();
				} else {
					more = false;
				}
			}
			stream.skipWhitespace();
			ensureNext(']', stream);
			// Need to evaluate expressions inside the object.
			expression = network.createInstance(Primitive.EXPRESSION);
			Vertex operator = network.createVertex(new Primitive(EVALCOPY));
			expression.addRelationship(Primitive.OPERATOR, operator);
			expression.setName(EVALCOPY);
			expression.addRelationship(Primitive.ARGUMENT, array);
		} else if (stream.peek() == '{') {
			stream.skip();
			// Parse object.
			Vertex object = null;
			stream.skipWhitespace();
			if (stream.peek() == '}') {
				stream.skip();
				object = network.createVertex();
				return object;
			}
			boolean more = true;
			while (more) {
				String attribute = stream.nextWord();
				if (attribute.equals("\"")) {
					attribute = stream.nextWord();
					ensureNext('"', stream);
				}
				ensureNext(':', stream);
				Vertex element = parseElement(stream, elements, debug, network);
				if (object == null) {
					if (attribute.equals("#data")) {
						object = element;
					} else {
						object = network.createVertex();
					}
				}
				object.addRelationship(new Primitive(attribute), element);
				stream.skipWhitespace();
				if (stream.peek() == ',') {
					stream.skip();
				} else {
					more = false;
				}
			}
			if (object == null) {
				object = network.createVertex();
			}
			stream.skipWhitespace();
			ensureNext('}', stream);
			// Need to evaluate expressions inside the object.
			expression = network.createInstance(Primitive.EXPRESSION);
			Vertex operator = network.createVertex(new Primitive(EVALCOPY));
			expression.addRelationship(Primitive.OPERATOR, operator);
			expression.setName(EVALCOPY);
			expression.addRelationship(Primitive.ARGUMENT, object);
		} else {
			// Check if reference or data.
			String token = stream.peekWord();
			if (token == null) {
				throw new SelfParseException("Unexpected end, element expected", stream);
			}
			token = token.toLowerCase();
			if (token.equals(VAR)) {
				token = VARIABLE;
			}
			if (OPERATORS.contains(token)) {
				expression = parseOperator(stream, elements, debug, network);
			} else if (token.equals("^")) {
				stream.nextWord();
				expression = parseElementName(Primitive.VARIABLE, stream, elements, debug, network);
				Vertex meaning = network.createInstance(Primitive.VARIABLE);
				meaning.addRelationship(Primitive.INSTANTIATION, new Primitive(expression.getName()));
				expression.addRelationship(Primitive.MEANING, meaning);
			} else if (TYPES.contains(token)) {
				stream.nextWord();
				if (token.equals(TEMPLATE)) {
					stream.skipWhitespace();
					ensureNext('(', stream);
					expression = parseTemplate(null, stream, elements, debug, network);
					stream.skipWhitespace();
					ensureNext(')', stream);
				} else if (token.equals(PATTERN)) {
					stream.skipWhitespace();
					ensureNext('(', stream);
					ensureNext('"', stream);
					expression = network.createPattern(stream.nextStringWithBracketsDoubleQuotes(), this);
					stream.skipWhitespace();
					ensureNext(')', stream);
				} else if (token.equals(VARIABLE)) {
					expression = parseElementName(Primitive.VARIABLE, stream, elements, debug, network);
				} else if (token.equals(TIME)) {
					stream.skipWhitespace();
					ensureNext('(', stream);
					ensureNext('"', stream);
					String value = stream.nextQuotesExcludeDoubleQuote();
					expression = network.createVertex(Utils.parseTime(value));
					stream.skipWhitespace();
					ensureNext(')', stream);
				} else if (token.equals(DATE)) {
					stream.skipWhitespace();
					ensureNext('(', stream);
					ensureNext('"', stream);
					String value = stream.nextQuotesExcludeDoubleQuote();
					expression = network.createVertex(Utils.parseDate(value));
					stream.skipWhitespace();
					ensureNext(')', stream);
				} else if (token.equals(TIMESTAMP)) {
					stream.skipWhitespace();
					ensureNext('(', stream);
					ensureNext('"', stream);
					String value = stream.nextQuotesExcludeDoubleQuote();
					expression = network.createVertex(Utils.parseTimestamp(value));
					stream.skipWhitespace();
					ensureNext(')', stream);
				} else if (token.equals(BINARY)) {
					stream.skipWhitespace();
					ensureNext('(', stream);
					ensureNext('"', stream);
					String value = stream.nextQuotesExcludeDoubleQuote();
					expression = network.createVertex(new BinaryData(value));
					stream.skipWhitespace();
					ensureNext(')', stream);
				} else if (token.equals(TEXT)) {
					stream.skipWhitespace();
					ensureNext('(', stream);
					ensureNext('"', stream);
					String value = stream.nextQuotesExcludeDoubleQuote();
					expression = network.createVertex(new TextData(value));
					stream.skipWhitespace();
					ensureNext(')', stream);
				} else {
					stream.skipWhitespace();
					if (stream.peek() != '(') {
						throw new SelfParseException("Expected '(' in " + token + " declaration", stream);
					}
					stream.skip();
					Long id = null;
					// Check for id or name.
					if (Character.isDigit(stream.peek())) {
						String idText = stream.nextWord();
						try {
							id = Long.valueOf(idText);
						} catch (NumberFormatException exception) {
							throw new SelfParseException("Invalid " + token + " id: " + idText, stream);
						}
					}
					char peek = stream.peek();
					String name = null;
					if ((id == null) || (peek == ':')) {
						if (id != null) {
							stream.skip();
						}
						name = stream.nextWord();
						if (name != null && Character.isLetter(name.charAt(0))) {
							throw new SelfParseException("Invalid " + token + " declaration: " + name, stream);
						}
					}
					Map<String, Vertex> elementsForType = elements.get(token);
					if (name != null) {
						if (elementsForType != null) {
							expression = elementsForType.get(name);
						}
					}
					if (expression == null) {
						if (id != null) {
							expression = network.findById(id);
							if (expression == null) {
								throw new SelfParseException("Id element reference not found: " + id, stream);
							}
							if ((elementsForType != null) && (name != null)) {
								elementsForType.put(name, expression);
							}
						} else if (name != null) {
							if (token.equals(STATE)) {
								expression = network.createInstance(Primitive.STATE);
								expression.setName(name);
							} else if (token.equals(VARIABLE)) {
								expression = network.createInstance(Primitive.VARIABLE);
								expression.setName(name);
							} else if (token.equals(FUNCTION)) {
								expression = network.createInstance(Primitive.FUNCTION);
								expression.setName(name);
							} else {
								throw new SelfParseException("Invalid element: " + token, stream);
							}
							if (name != null) {
								elementsForType = elements.get(token);
								if (elementsForType != null) { 
									elementsForType.put(name, expression);
								}
							}
						} else {
							throw new SelfParseException("Invalid element: " + token, stream);
						}
					}
					stream.skipWhitespace();
					ensureNext(')', stream);
				}
			} else {
				char next = stream.peek();
				try {
					if (next == '#') {
						stream.skip();
						String data = stream.upToAny(PRIMITIVE_TOKENS);
						expression = network.createVertex(new Primitive(data));
					} else if (next == '"') {
						stream.skip();
						String data = stream.nextStringDoubleQuotes();
						data = data.replace("\\\"", "\"");
						expression = network.createVertex(data);
					} else if (next == '\'') {
						stream.skip();
						String data = stream.nextStringQuotes();
						data = data.replace("\\'", "'");
						expression = network.createVertex(data);
					} else if (Character.isDigit(next) || next == '-' || next == '+') {
						int position = stream.getPosition();
						String data = stream.nextWord();
						if (data.indexOf(',') != -1) {
							stream.setPosition(position);
							data = stream.upTo(',');
						}
						int index = data.indexOf('.');
						if ((index != -1) && (index + 1 < data.length())) {
							// Check for 4.next
							if (!Character.isDigit(data.charAt(index + 1))) {
								stream.setPosition(position);
								data = stream.upTo('.');
							}
						}
						if (index != -1) {
							expression = network.createVertex(new BigDecimal(data));
						} else {
							expression = network.createVertex(new BigInteger(data));
						}				
					} else {
						expression = parseElementName(null, stream, elements, debug, network);
					}
				} catch (SelfParseException exception) {
					throw exception;
				} catch (Exception exception) {
					throw new SelfParseException("Invalid data: " + next, stream, exception);
				}
			}
		}
		stream.skipWhitespace();
		char peek = stream.peek();
		while (".=!&|)[<>+-*/".indexOf(peek) != -1) {
			String operator1 = stream.peek(1);
			String operator = stream.peek(2);
			if (peek == ')') {
				if (brackets > 0) {
					brackets--;
					stream.skip();
				} else {
					break;
				}
			} else if (peek == '.') {
				stream.skip();
				int position = stream.getPosition();
				String attribute = stream.nextWord();
				if (!Character.isLetter(attribute.charAt(0)) && attribute.charAt(0) != '@') {
					throw new SelfParseException("Invalid attribute name: " + attribute, stream);
				}
				if (attribute.indexOf('.') != -1) {
					stream.setPosition(position);
					stream.skipWhitespace();
					attribute = stream.upTo('.');
				}
				Vertex newExpression = network.createInstance(Primitive.EXPRESSION);
				stream.skipWhitespace();
				Vertex associate = null;
				Vertex associateRelationship = null;
				peek = stream.peek();
				if (peek == '(') {
					newExpression.addRelationship(Primitive.OPERATOR, Primitive.CALL);
					newExpression.setName(CALL);
					newExpression.addRelationship(Primitive.THIS, expression, 0);
					newExpression.addRelationship(Primitive.FUNCTION, network.createVertex(new Primitive(attribute)), 1);
					parseArguments(newExpression, Primitive.ARGUMENT, 0, stream, elements, false, debug, network);
				} else {
					if (peek == '[') {
						stream.skip();
						Vertex index = parseElement(stream, elements, debug, network);
						stream.skipWhitespace();
						if (stream.peek() == ',') {
							associate = index;
							associateRelationship = parseElement(stream, elements, debug, network);
						} else {
							newExpression.addRelationship(Primitive.INDEX, index);
						}
						ensureNext(']', stream);
						peek = stream.peek();
					}
					boolean isSet = false;
					String peek2 = stream.peek(2);
					if (peek == '=' && peek2.equals("=+")) {
						isSet = true;
						stream.skip(2);
						newExpression.addRelationship(Primitive.OPERATOR, Primitive.ADD);
						newExpression.setName(ADD);
					} else if (peek == '=' && peek2.equals("=-")) {
						isSet = true;
						stream.skip(2);
						newExpression.addRelationship(Primitive.OPERATOR, Primitive.REMOVE);
						newExpression.setName(REMOVE);
					} else if (peek == '=' && !peek2.equals("==")) {
						isSet = true;
						stream.skip();
						newExpression.addRelationship(Primitive.OPERATOR, Primitive.SET);
						newExpression.setName(SET);
					} else {
						newExpression.addRelationship(Primitive.OPERATOR, Primitive.GET);
						newExpression.setName(GET);
					}
					newExpression.addRelationship(Primitive.ARGUMENT, expression, 0);
					newExpression.addRelationship(Primitive.ARGUMENT, network.createVertex(new Primitive(attribute)), 1);
					if (isSet) {
						Vertex argument = parseElement(stream, elements, debug, network);
						newExpression.addRelationship(Primitive.ARGUMENT, argument, 2);
					}
					if (associate != null) {
						newExpression.addRelationship(Primitive.ARGUMENT, associate, 3);
						newExpression.addRelationship(Primitive.ARGUMENT, associateRelationship, 4);
					}
				}
				expression = newExpression;
			} else if (peek == '[') {
				stream.skip();
				Vertex newExpression = network.createInstance(Primitive.EXPRESSION);
				Vertex variable = parseElement(stream, elements, debug, network);
				stream.skipWhitespace();
				ensureNext(']', stream);
				stream.skipWhitespace();
				peek = stream.peek();
				if (peek == '(') {
					newExpression.addRelationship(Primitive.OPERATOR, Primitive.CALL);
					newExpression.setName(CALL);
					newExpression.addRelationship(Primitive.THIS, expression, 0);
					newExpression.addRelationship(Primitive.FUNCTION, variable, 1);
					parseArguments(newExpression, Primitive.ARGUMENT, 0, stream, elements, false, debug, network);
				} else {
					boolean isSet = false;
					String peek2 = stream.peek(2);
					if (peek == '=' && peek2.equals("=+")) {
						isSet = true;
						stream.skip(2);
						newExpression.addRelationship(Primitive.OPERATOR, Primitive.ADD);
						newExpression.setName(ADD);
					} else if (peek == '=' && peek2.equals("=-")) {
						isSet = true;
						stream.skip(2);
						newExpression.addRelationship(Primitive.OPERATOR, Primitive.REMOVE);
						newExpression.setName(REMOVE);
					} else if (stream.peek() == '=' && !stream.peek(2).equals("==")) {
						isSet = true;
						stream.skip();
						newExpression.addRelationship(Primitive.OPERATOR, Primitive.SET);
						newExpression.setName(SET);
					} else {
						newExpression.addRelationship(Primitive.OPERATOR, Primitive.GET);
						newExpression.setName(GET);
					}
					newExpression.addRelationship(Primitive.ARGUMENT, expression, 0);
					newExpression.addRelationship(Primitive.ARGUMENT, variable, 1);
					if (isSet) {
						Vertex argument = parseElement(stream, elements, debug, network);
						newExpression.addRelationship(Primitive.ARGUMENT, argument, 2);
					}
				}
				expression = newExpression;
			} else {
				Primitive operation = BINARY_OPERATORS.get(operator);
				Primitive operation1 = null;
				if (operation == null) {
					operation1 = BINARY_OPERATORS.get(operator1);
				}
				if (operator.equals("//")) {
					break;
				} else if (operator.equals("++")) {
					stream.skip(2);
					Vertex newExpression = network.createInstance(Primitive.EXPRESSION);
					newExpression.addRelationship(Primitive.OPERATOR, Primitive.INCREMENT);
					newExpression.setName(INCREMENT);
					newExpression.addRelationship(Primitive.ARGUMENT, expression, 0);
					expression = newExpression;
				} else if (operator.equals("--")) {
					stream.skip(2);
					Vertex newExpression = network.createInstance(Primitive.EXPRESSION);
					newExpression.addRelationship(Primitive.OPERATOR, Primitive.DECREMENT);
					newExpression.setName(DECREMENT);
					newExpression.addRelationship(Primitive.ARGUMENT, expression, 0);
					expression = newExpression;
				} else if (operation != null || operation1 != null) {
					if (lastBinary != null) {
						// Check order of binary operations.
						int lastIndex = BINARY_PRECEDENCE.indexOf(lastBinary);
						int index = 0;
						if (operation == null) {
							index = BINARY_PRECEDENCE.indexOf(operation1);
						} else {
							index = BINARY_PRECEDENCE.indexOf(operation);
						}
						if (index <= lastIndex) {
							break;
						}
					}
					if (operation == null) {
						stream.skip();
						operator = operator1;
						operation = operation1;
					} else {
						stream.skip(2);
					}
					Vertex newExpression = network.createInstance(Primitive.EXPRESSION);
					newExpression.addRelationship(Primitive.OPERATOR, operation);
					newExpression.setName(operator);
					newExpression.addRelationship(Primitive.ARGUMENT, expression, 0);
					Vertex argument = parseElement(stream, elements, operation, debug, network);
					newExpression.addRelationship(Primitive.ARGUMENT, argument, 1);
					expression = newExpression;
				} else if (peek == '=' && expression.isVariable()) {
					stream.skip();
					Vertex newExpression = network.createInstance(Primitive.EXPRESSION);
					newExpression.addRelationship(Primitive.OPERATOR, Primitive.ASSIGN);
					newExpression.setName(ASSIGN);
					newExpression.addRelationship(Primitive.ARGUMENT, expression, 0);
					Vertex argument = parseElement(stream, elements, null, debug, network);
					newExpression.addRelationship(Primitive.ARGUMENT, argument, 1);
					expression = newExpression;
				} else {
					throw new SelfParseException("Invalid operator: " + operator, stream);				
				}
			}
			stream.skipWhitespace();
			peek = stream.peek();
		}
		stream.skipWhitespace();
		while (brackets > 0) {
			stream.skipWhitespace();
			ensureNext(')', stream);
			brackets--;
		}
		if (debug && expression.instanceOf(Primitive.EXPRESSION)) {
			for (String comment : comments) {
				expression.addRelationship(Primitive.COMMENT, network.createVertex(comment));
			}
			expression.addRelationship(Primitive.SOURCE, network.createVertex(source));
			expression.addRelationship(Primitive.LINE_NUMBER, network.createVertex(lineNumber));
		}
		return expression;
	}
	
	/**
	 * Parse the element name (state, function, variable)
	 */
	public Vertex parseElementName(Primitive type, TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		stream.skipWhitespace();
		int position = stream.getPosition();
		String name = stream.nextWord();
		if (name.indexOf('.') != -1) {
			stream.setPosition(position);
			name = stream.upTo('.');
		}
		if ("}".equals(name)) {
			stream.previous();
			return network.createVertex(Primitive.NULL);
		}
		if (name == null || !Character.isLetter(name.charAt(0))) {
			throw new SelfParseException("Invalid element: " + name, stream);
		}
		if (name.equalsIgnoreCase(NULL)) {
			return network.createVertex(Primitive.NULL);
		} else if (name.equalsIgnoreCase(TRUE)) {
			return network.createVertex(Primitive.TRUE);
		} else if (name.equalsIgnoreCase(FALSE)) {
			return network.createVertex(Primitive.FALSE);
		} else if (name.equalsIgnoreCase(UNKNOWN)) {
			return network.createVertex(Primitive.UNKNOWN);
		} else if (name.equalsIgnoreCase(BREAK)) {
			return network.createVertex(Primitive.BREAK);
		} else if (name.equalsIgnoreCase(CONTINUE)) {
			return network.createVertex(Primitive.CONTINUE);
		}
		Long id = null;
		if (stream.peek() == ':') {
			// Check for id.
			if (Character.isDigit(stream.peek())) {
				String idText = stream.nextWord();
				try {
					id = Long.valueOf(idText);
				} catch (NumberFormatException exception) {
					throw new SelfParseException("Invalid id: " + idText, stream);
				}
			}
		}
		if (type == null) {
			if (stream.peek() == '(') {
				type = Primitive.FUNCTION;
			} else {
				// By default assume capitalized words are global primitives, lower case are variables.
				if (Character.isUpperCase(name.charAt(0))) {
					Map<String, Vertex> elementsForType = elements.get(Primitive.VARIABLE.getIdentity());
					Vertex variable = elementsForType.get(name);
					if (variable != null) {
						return variable;
					}
					return network.createVertex(new Primitive(name.toLowerCase()));
				}
				type = Primitive.VARIABLE;				
			}
		}
		String typeName = type.getIdentity();
		Vertex vertex = null;
		Map<String, Vertex> elementsForType = elements.get(typeName);
		if (name != null) {
			if (elementsForType != null) {
				vertex = elementsForType.get(name);
			}
		}
		if (vertex == null && id != null) {
			vertex = network.findById(id);
			if (vertex == null) {
				throw new SelfParseException("Id element reference not found: " + id, stream);
			}
			if ((elementsForType != null) && (name != null)) {
				elementsForType.put(name, vertex);
			}
			return vertex;
		}
		if (vertex == null) {
			vertex = network.createInstance(type);
			vertex.setName(name);
			if (name != null) {
				elementsForType = elements.get(typeName);
				if (elementsForType != null) { 
					elementsForType.put(name, vertex);
				}
			}
		}
		if (type == Primitive.FUNCTION) {
			if (stream.peek() == '(') {
				position = stream.getPosition();
				stream.skip();
				stream.skipWhitespace();
				if (stream.peek() != ')') {
					stream.setPosition(position);
					parseArguments(vertex, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
				} else {
					stream.skip();
				}
				
			}
		}
		return vertex;
	}
		
	/**
	 * Parse the variable.
	 */
	@Override
	public Vertex parseVariable(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		stream.nextWord();
		Vertex variable = parseElementName(Primitive.VARIABLE, stream, elements, debug, network);
		stream.skipWhitespace();
		ensureNext('{', stream);
		stream.skipWhitespace();
		while (stream.peek() != '}') {
			if (stream.atEnd()) {
				throw new SelfParseException("Unexpected end of variable, missing '}'", stream);				
			}
			if (stream.peek() == ':') {
				stream.skip();
				boolean more = true;
				while (more) {
					boolean not = false;
					stream.skipWhitespace();
					if (stream.peek() == '!') {
						not = true;
						stream.next();
					}
					Vertex value = parseElement(stream, elements, debug, network);
					if (not) {
						variable.removeRelationship(Primitive.EQUALS, value);
					} else {
						variable.addRelationship(Primitive.EQUALS, value);
					}
					stream.skipWhitespace();
					more = stream.peek() == ',';
					if (more) {
						stream.next();
					}
				}
			} else {
				String name = stream.nextWord();
				if (name == null || !Character.isLetter(name.charAt(0))) {
					throw new SelfParseException("Invalid variable attribute: " + name, stream);
				}
				Vertex attribute = network.createVertex(new Primitive(name));
				stream.skipWhitespace();
				ensureNext(':', stream);
				boolean more = true;
				while (more) {
					boolean not = false;
					stream.skipWhitespace();
					if (stream.peek() == '!') {
						not = true;
						stream.skip();
					}
					Vertex value = parseElement(stream, elements, debug, network);
					if (not) {
						variable.removeRelationship(attribute, value);
					} else {
						variable.addRelationship(attribute, value);
					}
					stream.skipWhitespace();
					more = stream.peek() == ',';
					if (more) {
						stream.skip();
						stream.skipWhitespace();
					}
				}
			}
			stream.skipWhitespace();
			ensureNext(';', stream);
			stream.skipWhitespace();
		}
		ensureNext('}', stream);
		return variable;
	}
	
	/**
	 * Parse the arguments to the expression.
	 */
	@Override
	protected List<Vertex> parseArguments(Vertex expression, Primitive type, int index, TextStream stream, Map<String, Map<String, Vertex>> elements, boolean outerBracket, boolean debug, Network network) {
		List<Vertex> arguments = new ArrayList<Vertex>();
		boolean bracket = false;
		if (!outerBracket) {
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
			expression.addRelationship(type, argument, index);
			if (!bracket && !outerBracket) {
				break;
			}
			stream.skipWhitespace();
			peek = stream.peek();
			if ((peek == ',') || (peek == ';'))  {
				stream.skip();
			} else {
				String previous = stream.peekPreviousWord();
				if (!"}".equals(previous)) {
					moreArguments = false;
				}
			}
			index++;
		}
		if (bracket) {
			ensureNext(')', stream);
		}
		return arguments;
	}
	
	/**
	 * Parse the function.
	 */
	public Vertex parseFunction(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		stream.nextWord();
		Vertex function = parseElementName(Primitive.FUNCTION, stream, elements, debug, network);
		stream.skipWhitespace();
		ensureNext('{', stream);
		function.addRelationship(Primitive.OPERATOR, new Primitive(function.getName()));
		stream.skipWhitespace();
		char peek = stream.peek();
		int index = 0;
		while (peek != '}') {
			stream.skipWhitespace();
			Vertex element = parseElement(stream, elements, debug, network);
			function.addRelationship(Primitive.DO, element, index);
			String previous = stream.peekPreviousWord();
			stream.skipWhitespace();
			if (!"}".equals(previous)) {
				ensureNext(';', ',', stream);
			}
			stream.skipWhitespace();
			peek = stream.peek();
			index++;
		}
		ensureNext('}', stream);
		return function;
	}

	/**
	 * Parse the template.
	 */
	@Override
	public Vertex parseTemplate(Vertex formula, TextStream stream, boolean debug, Network network) {
		Map<String, Map<String, Vertex>> elements = buildElementsMap(network);
		return parseTemplate(formula, stream, elements, debug, network);
	}

	@Override
	public Map<String, Map<String, Vertex>> buildElementsMap(Network network) {
		Map<String, Map<String, Vertex>> elements = new HashMap<String, Map<String, Vertex>>();
		Map<String, Vertex> variables = new HashMap<String, Vertex>();
		elements.put(VARIABLE, variables);
		elements.put(STATE, new HashMap<String, Vertex>());
		elements.put(FUNCTION, new HashMap<String, Vertex>());
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
	 * Parse the template.
	 */
	@Override
	public Vertex parseTemplate(Vertex formula, TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {		
		String name = "Template(";
		stream.skipWhitespace();
		ensureNext('"', stream);
		int position = stream.getPosition();
		String text = stream.nextStringWithBracketsDoubleQuotes();
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
			while ((token != null) && ((!token.equals("\\") || (peek == '"')))) {
				Vertex word = null;
				if (token.equals("\\") && (peek == '"')) {
					token = formulaStream.nextWord();
				} else  if (token.endsWith("\\") && (peek == '"')) {
					token = token.substring(0, token.length() - 1);
				}
				if (token.equals("{")) {
					word = parseElement(formulaStream, elements, debug, network);
					formulaStream.skipWhitespace();
					if (formulaStream.peek() == ';') {
						Vertex expression = network.createVertex();
						expression.addRelationship(Primitive.INSTANTIATION, Primitive.EXPRESSION);
						expression.addRelationship(Primitive.OPERATOR, Primitive.DO);
						expression.addRelationship(Primitive.DO, word, Integer.MAX_VALUE);
						while (formulaStream.peek() == ';') {
							formulaStream.skip();
							formulaStream.skipWhitespace();
							if (formulaStream.peek() == '}') {
								break;
							}
							word = parseElement(formulaStream, elements, debug, network);
							expression.addRelationship(Primitive.DO, word, Integer.MAX_VALUE);
							formulaStream.skipWhitespace();
						}
						word = expression;
					}
					ensureNext('}', formulaStream);
				} else {
					word = network.createWord(token);
				}
				formula.addRelationship(Primitive.WORD, word, index);
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
			int column = exception.getColumnNumber();
			exception.initFromStream(stream);
			exception.setColumnNumber(position + column);
			stream.setPosition(newPosition);
			throw exception;
		}
		formula.setName(name + "\"" + text + "\")");
		return formula;
	}
	
	/**
	 * Parse the operator.
	 */
	@Override
	public Vertex parseOperator(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		Vertex expression = network.createInstance(Primitive.EXPRESSION);
		String next = stream.nextWord();
		next = next.toLowerCase();
		if (!OPERATORS.contains(next)) {
			throw new SelfParseException("Invalid operator: '" + next + "' valid operators are: " + OPERATORS, stream);						
		}
		String last = next.toLowerCase();
		if (next.equals(NOT)) {
			next = "not";
		}
		Vertex operator = network.createVertex(new Primitive(next));
		expression.addRelationship(Primitive.OPERATOR, operator);
		expression.setName(next);
		if (last.equals(IF)) {
			List<Vertex> arguments = parseArguments(expression, Primitive.ARGUMENT, 0, stream, elements, false, debug, network);
			ensureArguments(IF, 1, arguments, stream);
			stream.skipWhitespace();
			ensureNext('{', stream);
			parseArguments(expression, Primitive.THEN, 0, stream, elements, true, debug, network);
			stream.skipWhitespace();
			ensureNext('}', stream);
			next = lower(stream.peekWord());
			boolean elseif = true;
			while (elseif) {
				elseif = false;
				if (ELSE.equals(next)) {
					stream.nextWord();
					next = lower(stream.peekWord());
					if (IF.equals(next)) {
						elseif = true;
						Vertex elseifExpression = parseOperator(stream, elements, debug, network);
						expression.addRelationship(Primitive.ELSEIF, elseifExpression);
					} else {
						stream.skipWhitespace();
						ensureNext('{', stream);
						parseArguments(expression, Primitive.ELSE, 0, stream, elements, true, debug, network);
						stream.skipWhitespace();
						ensureNext('}', stream);
					}
				}
			}
		} else if (last.equals(WHILE)) {
			List<Vertex> arguments = parseArguments(expression, Primitive.ARGUMENT, 0, stream, elements, false, debug, network);
			ensureArguments(WHILE, 1, arguments, stream);
			stream.skipWhitespace();
			ensureNext('{', stream);
			parseArguments(expression, Primitive.DO, 0, stream, elements, true, debug, network);
			stream.skipWhitespace();
			ensureNext('}', stream);
		} else if (last.equals(DO) || last.equals(THINK)) {
			stream.skipWhitespace();
			ensureNext('{', stream);
			parseArguments(expression, Primitive.DO, 0, stream, elements, true, debug, network);
			stream.skipWhitespace();
			ensureNext('}', stream);
		} else if (last.equals(FOR)) {
			stream.skipWhitespace();
			ensureNext('(', stream);
			boolean more = true;
			while (more) {
				Vertex variable = parseElement(stream, elements, debug, network);
				stream.skipWhitespace();
				ensureNext("in", stream);
				Vertex object = parseElement(stream, elements, debug, network);
				expression.addRelationship(Primitive.ARGUMENT, variable, Integer.MAX_VALUE);
				expression.addRelationship(Primitive.ARGUMENT, object, Integer.MAX_VALUE);
				stream.skipWhitespace();
				if (stream.peek() == ',') {
					stream.skip();
				} else {
					more = false;
				}
			}
			ensureNext(')', stream);
			stream.skipWhitespace();
			ensureNext('{', stream);
			parseArguments(expression, Primitive.DO, 0, stream, elements, true, debug, network);
			stream.skipWhitespace();
			ensureNext('}', stream);
		} else if (last.equals(NEW)) {
			// Handle new Date() vs new (#human, #thing)
			stream.skipWhitespace();
			if (Character.isUpperCase(stream.peek())) {
				int position = stream.getPosition();
				String type = stream.nextWord();
				if (stream.peek() == '(') {
					// TODO handle constructors.
					stream.skip();
					stream.skipWhitespace();
					ensureNext(')', stream);
					expression.addRelationship(Primitive.ARGUMENT, new Primitive(type.toLowerCase()));
				} else {
					stream.setPosition(position);
					parseArguments(expression, Primitive.ARGUMENT, 0, stream, elements, false, debug, network);
				}
			} else {
				parseArguments(expression, Primitive.ARGUMENT, 0, stream, elements, false, debug, network);
			}
		} else {
			parseArguments(expression, Primitive.ARGUMENT, 0, stream, elements, false, debug, network);
		}
		return expression;
	}
	
	/**
	 * Parse the CASE condition.
	 */
	@Override
	public Vertex parseCase(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		stream.nextWord();
		stream.skipWhitespace();
		Vertex expression = network.createInstance(Primitive.CASE);
		String next = stream.peekWord();
		boolean anyOrNone = false;
		if (next.equalsIgnoreCase(ANY)) {
			stream.nextWord();
			next = stream.peekWord();
			if (next.equalsIgnoreCase(OR)) {
				stream.nextWord();
				stream.skipWhitespace();
				ensureNext(NONE, stream);
				anyOrNone = true;
			}
		}
		Vertex variable = parseElement(stream, elements, debug, network);
		if (!anyOrNone && variable.instanceOf(Primitive.ARRAY)) {
			variable.addRelationship(Primitive.TYPE, Primitive.REQUIRED);
		}
		expression.addRelationship(Primitive.CASE, variable);
		next = stream.nextWord();
		if (next.equalsIgnoreCase(AS)) {
			Vertex as = parseElement(stream, elements, debug, network);
			expression.addRelationship(Primitive.AS, as);
			next = stream.nextWord();
		}
		if (next.equalsIgnoreCase(TOPIC)) {
			Vertex topic = parseElement(stream, elements, debug, network);
			expression.addRelationship(Primitive.TOPIC, topic);
			next = stream.nextWord();
		}
		if (next.equalsIgnoreCase(THAT)) {
			Vertex template = parseElement(stream, elements, debug, network);
			expression.addRelationship(Primitive.THAT, template);
			next = stream.nextWord();
		}
		if (next.equalsIgnoreCase(GOTO)) {
			List<Vertex> thens = new ArrayList<Vertex>();
			stream.skipWhitespace();
			boolean parseGoto = true;
			while (parseGoto) {
				thens.add(parseElementName(Primitive.STATE, stream, elements, debug, network));
				stream.skipWhitespace();
				if (stream.peek() == ',') {
					stream.skip();
				} else {
					parseGoto = false;
				}
			}
			for (Vertex then : thens) {
				expression.addRelationship(Primitive.GOTO, then);				
			}
		} else if (next.equalsIgnoreCase(TEMPLATE) || next.equalsIgnoreCase(ANSWER)) {
			Vertex template = parseElement(stream, elements, debug, network);
			expression.addRelationship(Primitive.TEMPLATE, template);
		} else if (next.equalsIgnoreCase(RETURN)) {
			expression.addRelationship(Primitive.GOTO, Primitive.RETURN);
		} else {
			stream.setPosition(stream.getPosition() - next.length());
			throw new SelfParseException("expected one of 'goto, template, answer, return, that, topic', found: " + next, stream);
		}
		next = stream.peekWord();
		if (next.equalsIgnoreCase(FOR)) {
			stream.nextWord();
			ensureNext(EACH, stream);
			expression.addRelationship(Primitive.FOR, parseElement(stream, elements, debug, network));
			ensureNext(OF, stream);
			expression.addRelationship(Primitive.FOR, parseElement(stream, elements, debug, network));
		}
		ensureNext(';', stream);
		return expression;
	}
	
	/**
	 * Parse the PATTERN condition.
	 */
	@Override
	public Vertex parsePattern(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		stream.nextWord();
		stream.skipWhitespace();
		Vertex expression = network.createInstance(Primitive.CASE);
		Vertex pattern = null;
		if (stream.peek() == '"') {
			stream.skip();
			pattern = network.createPattern(stream.nextStringWithBracketsDoubleQuotes(), this);
		} else {
			pattern = parseElement(stream, elements, debug, network);
		}
		expression.addRelationship(Primitive.PATTERN, pattern);
		String next = stream.nextWord().toLowerCase();
		Vertex topic = null;
		if (next.equals(TOPIC)) {
			topic = parseElement(stream, elements, debug, network);
			expression.addRelationship(Primitive.TOPIC, topic);
			next = stream.nextWord().toLowerCase();
		}
		Vertex that = null;
		if (next.equals(THAT)) {
			stream.skipWhitespace();
			if (stream.peek() == '"') {
				stream.skip();
				that = network.createPattern(stream.nextStringWithBracketsDoubleQuotes(), this);
			} else {
				that = parseElement(stream, elements, debug, network);
			}
			expression.addRelationship(Primitive.THAT, that);
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
				expression.addRelationship(Primitive.GOTO, then);				
			}
		} else if (next.equals(RETURN)) {
			expression.addRelationship(Primitive.GOTO, Primitive.RETURN);
		} else if (next.equals(TEMPLATE) || next.equals(ANSWER)) {
			Vertex template = parseElement(stream, elements, debug, network);
			expression.addRelationship(Primitive.TEMPLATE, template);
		} else {
			stream.setPosition(stream.getPosition() - next.length());
			throw new SelfParseException("expected one of GOTO, TEMPLATE, RETURN, THAT, TOPIC, found: " + next, stream);
		}
		ensureNext(';', stream);
		Vertex sentenceState = AIMLParser.parser().createSentenceState(elements.get("root").get("root"), network);
		Vertex state = AIMLParser.parser().createState(pattern, sentenceState, network);
		state.addRelationship(Primitive.DO, expression);
		return expression;
	}
	
	/**
	 * Parse the RETURN condition.
	 */
	@Override
	public Vertex parseReturn(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		stream.nextWord();
		stream.skipWhitespace();
		Vertex expression = network.createInstance(Primitive.RETURN);
		if (stream.peek() != ';') {
			boolean with = stream.peekWord().toLowerCase().equals(WITH);
			if (!with) {
				Vertex result = parseElement(stream, elements, debug, network);
				expression.addRelationship(Primitive.RETURN, result);
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
					expression.addRelationship(Primitive.ARGUMENT, argument, Integer.MAX_VALUE);
					stream.skipWhitespace();
					while (stream.peek() == ',') {
						stream.skip();
						stream.skipWhitespace();
						argument = parseElement(stream, elements, debug, network);
						expression.addRelationship(Primitive.ARGUMENT, argument, Integer.MAX_VALUE);
					}
					ensureNext(')', stream);
				} else {
					Vertex argument = parseElement(stream, elements, debug, network);
					expression.addRelationship(Primitive.ARGUMENT, argument, Integer.MAX_VALUE);
				}
			}
		}
		return expression;
	}
	
	/**
	 * Parse the GOTO condition.
	 */
	@Override
	public Vertex parseGoto(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		stream.nextWord();
		Vertex expression = network.createInstance(Primitive.GOTO);
		stream.skipWhitespace();
		boolean gotoFinally = stream.peekWord().toLowerCase().equals(FINALLY);
		if (gotoFinally) {
			stream.nextWord();
			expression.addRelationship(Primitive.FINALLY, Primitive.FINALLY);
		}
		Vertex value = parseElementName(Primitive.STATE, stream, elements, debug, network);
		expression.addRelationship(Primitive.GOTO, value);
		if (stream.peek() != ';') {
			if (stream.peekWord().toLowerCase().equals(WITH)) {
				stream.skipWord();
				stream.skipWhitespace();
				if (stream.peek() == '(') {
					stream.skip();
					stream.skipWhitespace();
					Vertex argument = parseElement(stream, elements, debug, network);
					expression.addRelationship(Primitive.ARGUMENT, argument, Integer.MAX_VALUE);
					stream.skipWhitespace();
					while (stream.peek() == ',') {
						stream.skip();
						stream.skipWhitespace();
						argument = parseElement(stream, elements, debug, network);
						expression.addRelationship(Primitive.ARGUMENT, argument, Integer.MAX_VALUE);
					}
					ensureNext(')', stream);
				} else {
					Vertex argument = parseElement(stream, elements, debug, network);
					expression.addRelationship(Primitive.ARGUMENT, argument, Integer.MAX_VALUE);
				}
			}
		}
		return expression;
	}
	
	/**
	 * Parse the PUSH condition.
	 */
	@Override
	public Vertex parsePush(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		stream.nextWord();
		Vertex expression = network.createInstance(Primitive.PUSH);
		Vertex value = parseElement(stream, elements, debug, network);
		expression.addRelationship(Primitive.ARGUMENT, value, Integer.MAX_VALUE);
		return expression;
	}
}

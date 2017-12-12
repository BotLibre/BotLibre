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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.BinaryData;
import org.botlibre.knowledge.Primitive;
import org.botlibre.knowledge.TextData;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

/**
 * Self scripting language compiler.
 * This compiler optimizes states, functions and expressions to compiled byte-code.
 */
public class Self4ByteCodeCompiler extends Self4Compiler {

	/**
	 * Parse the code into a temporary expression so it can be evaluated.
	 */
	@Override
	public Vertex parseExpressionForEvaluation(String code, Vertex speaker, Vertex target, boolean debug, Network network) {
		TextStream stream = new TextStream(code);
		try {
			Map<String, Map<String, Vertex>> elements = new HashMap<String, Map<String, Vertex>>();
			elements.put(VARIABLE, new HashMap<String, Vertex>());
			elements.get(VARIABLE).put("speaker", speaker);
			elements.get(VARIABLE).put("target", target);
			elements.put(EQUATION, new HashMap<String, Vertex>());
			getComments(stream);
			// Create a temporary equation to execute.
			Vertex expression = network.createTemporyVertex();
			expression.addRelationship(Primitive.INSTANTIATION, Primitive.EXPRESSION);
			BinaryData byteCode = new BinaryData();
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			DataOutputStream dataStream = new DataOutputStream(byteStream);
			dataStream.writeLong(network.createVertex(Primitive.EXPRESSION).getId());
			dataStream.writeLong(network.createVertex(Primitive.DO).getId());
			dataStream.writeLong(network.createVertex(Primitive.DO).getId());
			stream.skipWhitespace();
			parseElementByteCode(stream, dataStream, elements, debug, network);
			stream.skipWhitespace();
			while (stream.peek() == ';') {
				stream.skip();
				stream.skipWhitespace();
				if (stream.atEnd()) {
					break;
				}
				parseElementByteCode(stream, dataStream, elements, debug, network);
				stream.skipWhitespace();
			}
			if (!stream.atEnd()) {
				throw new SelfParseException("Unexpect element " + stream.peekWord(), stream);
			}
			dataStream.writeLong(0l);
			dataStream.writeLong(0l);
			dataStream.writeLong(0l);
			byteCode.setBytes(byteStream.toByteArray());
			expression.setData(byteCode);
			network.getBot().log(this, "Compiled new expression for evaluation", Level.INFO, expression);
			return expression;
		} catch (SelfParseException exception) {
			throw exception;
		} catch (Exception exception) {
			network.getBot().log(this, exception);
			throw new SelfParseException("Parsing error occurred", stream, exception);
		}
	}
	
	/**
	 * Parse the state and any referenced states or variables.
	 */
	@Override
	public Vertex parseState(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		try {
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
			BinaryData byteCode = new BinaryData();
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			DataOutputStream dataStream = new DataOutputStream(byteStream);
			dataStream.writeLong(network.createVertex(Primitive.SELF4).getId());
			stream.skipWhitespace();
			ensureNext('{', stream);
			stream.skipWhitespace();
			String element = stream.peekWord();
			while (!("}".equals(element))) {
				if (element == null) {
					throw new SelfParseException("Unexpected end of state, missing '}'", stream);
				}
				element = element.toLowerCase();
				if (element.equals(CASE)) {
					parseCaseByteCode(stream, dataStream, elements, debug, network);
				} else if (element.equals(PATTERN)) {
					parsePatternByteCode(stream, dataStream, elements, debug, network);
				} else if (element.equals(STATE)) {
					parseState(stream, elements, debug, network);
				} else if (element.equals(VAR) || element.equals(VARIABLE)) {
					parseVariable(stream, elements, debug, network);
				} else if (element.equals(ANSWER)) {
						parseAnswerByteCode(stream, dataStream, elements, debug, network);
				} else if (element.equals(FUNCTION) || element.equals(EQUATION)) {
					parseFunctionByteCode(stream, elements, debug, network);
				} else if (element.equals(DO)) {
					parseDoByteCode(stream, dataStream, elements, debug, network);
				} else if (element.equals(GOTO)) {
					parseGotoByteCode(stream, dataStream, elements, debug, network);
				} else if (element.equals(PUSH)) {
					parsePushByteCode(stream, dataStream, elements, debug, network);
				} else if (element.equals(RETURN)) {
					parseReturnByteCode(stream, dataStream, elements, debug, network);
				} else if (element.equals("/")) {
					comments = getComments(stream);
					if (comments.isEmpty()) {
						throw new SelfParseException("Unknown element: " + element, stream);					
					}
				} else {
					throw new SelfParseException("Unknown element: " + element, stream);
				}
				element = stream.peekWord();
			}
			ensureNext('}', stream);
			dataStream.writeLong(0l);
			byteCode.setBytes(byteStream.toByteArray());
			state.setData(byteCode);
			network.addVertex(state);
			return state;
		} catch (IOException exception) {
			throw new SelfParseException("IO Error", stream, exception);
		}
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
				state = new SelfByteCodeCompiler().parseState(stream, elements, debug, network);
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
			state.addRelationship(Primitive.SOURCECODE, network.createVertex(text));
			Vertex sourceCode = state.getRelationship(Primitive.SOURCECODE);
			if (sourceCode != null) {
				sourceCode.setPinned(true);
			}*/
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
	 * Parse the quotient.
	 * answer:0.5 "World" { previous "Hello"; previous ! "Hi"; }
	 */
	public void parseAnswerByteCode(TextStream stream, DataOutputStream dataStream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network)
				throws IOException {
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
		dataStream.writeLong(network.createVertex(Primitive.QUOTIENT).getId());
		dataStream.writeFloat(correctness);
		parseElementByteCode(stream, dataStream, elements, debug, network);
		stream.skipWhitespace();
		if (stream.peek() == '{') {
			stream.skip();
			String next = stream.nextWord();
			dataStream.writeLong(network.createVertex(Primitive.PREVIOUS).getId());
			while (!("}".equals(next))) {
				if (next == null) {
					throw new SelfParseException("Unexpected end of quotient, missing '}'", stream);				
				}
				next = next.toLowerCase();
				if (!(PREVIOUS.equals(next))) {
					throw new SelfParseException("Unexpected word: '" + next + "' expected 'PREVIOUS'", stream);				
				}
				next = stream.peekWord();
				if (NOT.equals(next)) {
					dataStream.writeLong(network.createVertex(Primitive.NOT).getId());
					stream.nextWord();
				}
				parseElementByteCode(stream, dataStream, elements, debug, network);			
				ensureNext(';', stream);
				next = stream.nextWord();
			}
			dataStream.writeLong(0l);
		}
		dataStream.writeLong(0l);
		ensureNext(';', stream);
	}
	
	/**
	 * Parse the reference to either a state, variable, expression, or data.
	 * One of,
	 * Object(1234), Symbol(x)
	 * var variable = "value", variable.attribute = "value", function(), function(arg, arg2).attribute = variable
	 * if (value == "value") {}, for () {}, new Array(), new (x, y), variable.function()
	 * 1234, "string", 'string', #primitive, ...
	 */
	public Vertex parseElementByteCode(TextStream stream, DataOutputStream dataStream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network)
				throws IOException {
		return parseElementByteCode(stream, dataStream, elements, null, debug, network);
	}

	/**
	 * Override to catch expressions in templates, patterns, and other places.
	 * Optimize bytecode if element in an expression.
	 */
	@Override
	public Vertex parseElement(TextStream stream, Map<String, Map<String, Vertex>> elements, Primitive binary, boolean debug, Network network) {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			DataOutputStream dataStream = new DataOutputStream(byteStream);
			Vertex element = parseElementByteCode(stream, dataStream, elements, binary, debug, network);
			if (element != null) {
				return element;
			}
			Vertex expression = network.createVertex();
			expression.addRelationship(Primitive.INSTANTIATION, Primitive.EXPRESSION);
			BinaryData byteCode = new BinaryData();
			dataStream.writeLong(0l);
			byteCode.setBytes(byteStream.toByteArray());
			expression.setData(byteCode);
			network.addVertex(expression);
			return expression;
		} catch (IOException exception) {
			throw new SelfParseException("IO Error", stream, exception);
		}
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

					try {
						ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
						ByteArrayOutputStream byteStream2 = null;
						DataOutputStream dataStream = new DataOutputStream(byteStream);
						word = parseElementByteCode(formulaStream, dataStream, elements, null, debug, network);
						boolean bytecode = false;
						if (word == null) {
							bytecode = true;
						}
						formulaStream.skipWhitespace();
						if (formulaStream.peek() == ';') {
							bytecode = true;
							byteStream2 = new ByteArrayOutputStream();
							DataOutputStream dataStream2 = new DataOutputStream(byteStream2);
							dataStream2.writeLong(network.createVertex(Primitive.EXPRESSION).getId());
							dataStream2.writeLong(network.createVertex(Primitive.DO).getId());
							dataStream2.writeLong(network.createVertex(Primitive.DO).getId());
							dataStream2.write(byteStream.toByteArray());
							while (formulaStream.peek() == ';') {
								formulaStream.skip();
								formulaStream.skipWhitespace();
								if (formulaStream.peek() == '}') {
									break;
								}
								parseElementByteCode(formulaStream, dataStream2, elements, null, debug, network);
								formulaStream.skipWhitespace();
							}
							dataStream2.writeLong(0l);
							dataStream2.writeLong(0l);
							dataStream2.writeLong(0l);
						}
						if (bytecode) {
							word = network.createVertex();
							word.addRelationship(Primitive.INSTANTIATION, Primitive.EXPRESSION);
							BinaryData byteCode = new BinaryData();
							if (byteStream2 == null) {
								dataStream.writeLong(0l);
								byteCode.setBytes(byteStream.toByteArray());
							} else {
								byteCode.setBytes(byteStream2.toByteArray());
							}
							word.setData(byteCode);
							network.addVertex(word);
						}
					} catch (IOException exception) {
						throw new SelfParseException("IO Error", stream, exception);
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
	 * Parse the reference to either a state, variable, expression, or data.
	 * One of,
	 * Object(1234), Symbol(x)
	 * var variable = "value", variable.attribute = "value", function(), function(arg, arg2).attribute = variable
	 * if (value == "value") {}, for () {}, new Array(), new (x, y), variable.function()
	 * 1234, "string", 'string', #primitive, ...
	 */
	public Vertex parseElementByteCode(TextStream stream, DataOutputStream dataStream, Map<String, Map<String, Vertex>> elements, Primitive lastBinary, boolean debug, Network network)
				throws IOException {
		getComments(stream);
		stream.skipWhitespace();
		int brackets = 0;
		while (stream.peek() == '(') {
			lastBinary = null;
			brackets++;
			stream.skip();
			stream.skipWhitespace();
		}
		Vertex element = null;
		if (stream.peek() == '[') {
			stream.skip();
			// Parse array.
			Vertex array = network.createInstance(Primitive.ARRAY);
			stream.skipWhitespace();
			if (stream.peek() == ']') {
				stream.skip();
				array.addRelationship(Primitive.LENGTH, network.createVertex(0));
				dataStream.writeLong(array.getId());
				return array;
			}
			boolean more = true;
			int index = 0;
			while (more) {
				Vertex value = parseElement(stream, elements, debug, network);
				array.addRelationship(Primitive.ELEMENT, value, index);
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
			dataStream.writeLong(network.createVertex(Primitive.EXPRESSION).getId());
			Vertex operator = network.createVertex(new Primitive(EVALCOPY));
			dataStream.writeLong(operator.getId());
			dataStream.writeLong(network.createVertex(Primitive.ARGUMENT).getId());
			dataStream.writeLong(array.getId());
			dataStream.writeLong(0l);
			dataStream.writeLong(0l);
		} else if (stream.peek() == '{') {
			stream.skip();
			// Parse object.
			Vertex object = null;
			stream.skipWhitespace();
			if (stream.peek() == '}') {
				stream.skip();
				object = network.createVertex();
				dataStream.writeLong(object.getId());
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
				Vertex attributeValue = parseElement(stream, elements, debug, network);
				attributeValue.getRelationship(Primitive.NULL);
				if (object == null) {
					if (attribute.equals("#data")) {
						object = attributeValue;
					} else {
						object = network.createVertex();
						object.addRelationship(new Primitive(attribute), attributeValue);
					}
				} else {
					object.addRelationship(new Primitive(attribute), attributeValue);
				}
				stream.skipWhitespace();
				if (stream.peek() == ',') {
					stream.skip();
				} else {
					more = false;
				}
			}
			stream.skipWhitespace();
			ensureNext('}', stream);
			// Need to evaluate expressions inside the object.
			dataStream.writeLong(network.createVertex(Primitive.EXPRESSION).getId());
			Vertex operator = network.createVertex(new Primitive(EVALCOPY));
			dataStream.writeLong(operator.getId());
			dataStream.writeLong(network.createVertex(Primitive.ARGUMENT).getId());
			dataStream.writeLong(object.getId());
			dataStream.writeLong(0l);
			dataStream.writeLong(0l);
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
				dataStream.writeLong(network.createVertex(Primitive.EXPRESSION).getId());
				parseOperatorByteCode(dataStream, stream, elements, debug, network);
			} else if (token.equals("^")) {
				stream.nextWord();
				element = parseElementName(Primitive.VARIABLE, stream, elements, debug, network);
				Vertex meaning = network.createInstance(Primitive.VARIABLE);
				meaning.addRelationship(Primitive.INSTANTIATION, new Primitive(element.getName()));
				element.addRelationship(Primitive.MEANING, meaning);
				dataStream.writeLong(element.getId());
			} else if (TYPES.contains(token)) {
				stream.nextWord();
				if (token.equals(TEMPLATE)) {
					stream.skipWhitespace();
					ensureNext('(', stream);
					element = parseTemplate(null, stream, elements, debug, network);
					dataStream.writeLong(element.getId());
					stream.skipWhitespace();
					ensureNext(')', stream);
				} else if (token.equals(PATTERN)) {
					stream.skipWhitespace();
					ensureNext('(', stream);
					ensureNext('"', stream);
					element = network.createPattern(stream.nextQuotesExcludeDoubleQuote(), this);
					dataStream.writeLong(element.getId());
					stream.skipWhitespace();
					ensureNext(')', stream);
				} else if (token.equals(VARIABLE)) {
					element = parseElementName(Primitive.VARIABLE, stream, elements, debug, network);
					dataStream.writeLong(element.getId());
				} else if (token.equals(DATE)) {
					stream.skipWhitespace();
					ensureNext('(', stream);
					ensureNext('"', stream);
					String value = stream.nextQuotesExcludeDoubleQuote();
					element = network.createVertex(Utils.parseDate(value));
					dataStream.writeLong(element.getId());
					stream.skipWhitespace();
					ensureNext(')', stream);
				} else if (token.equals(TIME)) {
					stream.skipWhitespace();
					ensureNext('(', stream);
					ensureNext('"', stream);
					String value = stream.nextQuotesExcludeDoubleQuote();
					element = network.createVertex(Utils.parseTime(value));
					dataStream.writeLong(element.getId());
					stream.skipWhitespace();
					ensureNext(')', stream);
				} else if (token.equals(TIMESTAMP)) {
					stream.skipWhitespace();
					ensureNext('(', stream);
					ensureNext('"', stream);
					String value = stream.nextQuotesExcludeDoubleQuote();
					element = network.createVertex(Utils.parseTimestamp(value));
					dataStream.writeLong(element.getId());
					stream.skipWhitespace();
					ensureNext(')', stream);
				} else if (token.equals(BINARY)) {
					stream.skipWhitespace();
					ensureNext('(', stream);
					ensureNext('"', stream);
					String value = stream.nextQuotesExcludeDoubleQuote();
					element = network.createVertex(new BinaryData(value));
					dataStream.writeLong(element.getId());
					stream.skipWhitespace();
					ensureNext(')', stream);
				} else if (token.equals(TEXT)) {
					stream.skipWhitespace();
					ensureNext('(', stream);
					ensureNext('"', stream);
					String value = stream.nextQuotesExcludeDoubleQuote();
					element = network.createVertex(new TextData(value));
					dataStream.writeLong(element.getId());
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
					element = null;
					if (name != null) {
						if (elementsForType != null) {
							element = elementsForType.get(name);
							if (element != null) {
								dataStream.writeLong(element.getId());
							}
						}
					}
					if (element == null) {
						if (id != null) {
							element = network.findById(id);
							if (element == null) {
								throw new SelfParseException("Id element reference not found: " + id, stream);
							}
							if ((elementsForType != null) && (name != null)) {
								elementsForType.put(name, element);
							}
							dataStream.writeLong(element.getId());
						} else if (name != null) {
							if (token.equals(STATE)) {
								element = network.createInstance(Primitive.STATE);
								element.setName(name);
							} else if (token.equals(VARIABLE)) {
								element = network.createInstance(Primitive.VARIABLE);
								element.setName(name);
							} else if (token.equals(FUNCTION)) {
								element = network.createInstance(Primitive.FUNCTION);
								element.setName(name);
							} else {
								throw new SelfParseException("Invalid element: " + token, stream);
							}
							if (name != null) {
								elementsForType = elements.get(token);
								if (elementsForType != null) { 
									elementsForType.put(name, element);
								}
							}
							dataStream.writeLong(element.getId());
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
						element = network.createVertex(new Primitive(data));
						dataStream.writeLong(element.getId());
					} else if (next == '"') {
						stream.skip();
						String data = stream.nextStringDoubleQuotes();
						data = data.replace("\\\"", "\"");
						element = network.createVertex(data);
						dataStream.writeLong(element.getId());
					} else if (next == '\'') {
						stream.skip();
						String data = stream.nextStringQuotes();
						element = network.createVertex(data);
						data = data.replace("\\\"", "\"");
						dataStream.writeLong(element.getId());
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
							element = network.createVertex(new BigDecimal(data));
						} else {
							element = network.createVertex(new BigInteger(data));
						}
						dataStream.writeLong(element.getId());
					} else {
						element = parseElementName(null, stream, elements, debug, network);
						dataStream.writeLong(element.getId());
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
		Vertex push = network.createVertex(Primitive.PUSH);
		Vertex pop = network.createVertex(Primitive.POP);
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
				element = null;
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
				stream.skipWhitespace();
				Vertex associate = null;
				Vertex associateRelationship = null;
				peek = stream.peek();
				if (peek == '(') {
					dataStream.writeLong(push.getId());
					dataStream.writeLong(network.createVertex(Primitive.CALL).getId());
					dataStream.writeLong(network.createVertex(Primitive.THIS).getId());
					dataStream.writeLong(pop.getId());
					dataStream.writeLong(0l);
					dataStream.writeLong(network.createVertex(Primitive.FUNCTION).getId());
					dataStream.writeLong(network.createVertex(new Primitive(attribute)).getId());
					dataStream.writeLong(0l);
					parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 0, stream, elements, false, debug, network);
					dataStream.writeLong(0l);
				} else {
					Vertex index = null;
					if (peek == '[') {
						stream.skip();
						index = parseElement(stream, elements, debug, network);
						stream.skipWhitespace();
						if (stream.peek() == ',') {
							associate = index;
							associateRelationship = parseElement(stream, elements, debug, network);
						}
						ensureNext(']', stream);
						peek = stream.peek();
					}
					boolean isSet = false;
					String peek2 = stream.peek(2);
					dataStream.writeLong(push.getId());
					if (peek == '=' && peek2.equals("=+")) {
						isSet = true;
						stream.skip(2);
						dataStream.writeLong(network.createVertex(Primitive.ADD).getId());
					} else if (peek == '=' && peek2.equals("=-")) {
						isSet = true;
						stream.skip(2);
						dataStream.writeLong(network.createVertex(Primitive.REMOVE).getId());
					} else if (peek == '=' && !peek2.equals("==")) {
						isSet = true;
						stream.skip();
						dataStream.writeLong(network.createVertex(Primitive.SET).getId());
					} else {
						dataStream.writeLong(network.createVertex(Primitive.GET).getId());
					}
					if (index != null) {
						dataStream.writeLong(network.createVertex(Primitive.INDEX).getId());
						dataStream.writeLong(index.getId());
						dataStream.writeLong(0l);
					}
					dataStream.writeLong(network.createVertex(Primitive.ARGUMENT).getId());
					dataStream.writeLong(pop.getId());
					dataStream.writeLong(network.createVertex(new Primitive(attribute)).getId());
					if (isSet) {
						parseElementByteCode(stream, dataStream, elements, debug, network);
					}
					if (associate != null) {
						dataStream.writeLong(associate.getId());
						dataStream.writeLong(associateRelationship.getId());
					}
					dataStream.writeLong(0l);
					dataStream.writeLong(0l);
				}
			} else if (peek == '[') {
				element = null;
				stream.skip();
				Vertex variable = parseElement(stream, elements, debug, network);
				stream.skipWhitespace();
				ensureNext(']', stream);
				stream.skipWhitespace();
				peek = stream.peek();
				if (peek == '(') {
					dataStream.writeLong(push.getId());
					dataStream.writeLong(network.createVertex(Primitive.CALL).getId());
					dataStream.writeLong(network.createVertex(Primitive.THIS).getId());
					dataStream.writeLong(pop.getId());
					dataStream.writeLong(0l);
					dataStream.writeLong(network.createVertex(Primitive.FUNCTION).getId());
					dataStream.writeLong(variable.getId());
					dataStream.writeLong(0l);
					parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 0, stream, elements, false, debug, network);
					dataStream.writeLong(0l);
				} else {
					boolean isSet = false;
					String peek2 = stream.peek(2);
					dataStream.writeLong(push.getId());
					if (peek == '=' && peek2.equals("=+")) {
						isSet = true;
						stream.skip(2);
						dataStream.writeLong(network.createVertex(Primitive.ADD).getId());
					} else if (peek == '=' && peek2.equals("=-")) {
						isSet = true;
						stream.skip(2);
						dataStream.writeLong(network.createVertex(Primitive.REMOVE).getId());
					} else if (stream.peek() == '=' && !stream.peek(2).equals("==")) {
						isSet = true;
						stream.skip();
						dataStream.writeLong(network.createVertex(Primitive.SET).getId());
					} else {
						dataStream.writeLong(network.createVertex(Primitive.GET).getId());
					}
					dataStream.writeLong(network.createVertex(Primitive.ARGUMENT).getId());
					dataStream.writeLong(pop.getId());
					dataStream.writeLong(variable.getId());
					if (isSet) {
						parseElementByteCode(stream, dataStream, elements, debug, network);
					}
					dataStream.writeLong(0l);
					dataStream.writeLong(0l);
				}
			} else {
				element = null;
				Primitive operation = BINARY_OPERATORS.get(operator);
				Primitive operation1 = null;
				if (operation == null) {
					operation1 = BINARY_OPERATORS.get(operator1);
				}
				if (operator.equals("//")) {
					break;
				} else if (operator.equals("++")) {
					stream.skip(2);
					dataStream.writeLong(push.getId());
					dataStream.writeLong(network.createVertex(Primitive.INCREMENT).getId());
					dataStream.writeLong(network.createVertex(Primitive.ARGUMENT).getId());
					dataStream.writeLong(pop.getId());
					dataStream.writeLong(0l);
					dataStream.writeLong(0l);
				} else if (operator.equals("--")) {
					stream.skip(2);
					dataStream.writeLong(push.getId());
					dataStream.writeLong(network.createVertex(Primitive.DECREMENT).getId());
					dataStream.writeLong(network.createVertex(Primitive.ARGUMENT).getId());
					dataStream.writeLong(pop.getId());
					dataStream.writeLong(0l);
					dataStream.writeLong(0l);
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
					dataStream.writeLong(push.getId());
					dataStream.writeLong(network.createVertex(operation).getId());
					dataStream.writeLong(network.createVertex(Primitive.ARGUMENT).getId());
					dataStream.writeLong(pop.getId());
					parseElementByteCode(stream, dataStream, elements, operation, debug, network);
					dataStream.writeLong(0l);
					dataStream.writeLong(0l);
				} else if (peek == '=') {
					stream.skip();
					dataStream.writeLong(push.getId());
					dataStream.writeLong(network.createVertex(Primitive.ASSIGN).getId());
					dataStream.writeLong(network.createVertex(Primitive.ARGUMENT).getId());
					dataStream.writeLong(pop.getId());
					parseElementByteCode(stream, dataStream, elements, null, debug, network);
					dataStream.writeLong(0l);
					dataStream.writeLong(0l);
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
		return element;
	}
	
	/**
	 * Parse the arguments to the expression.
	 */
	protected int parseArgumentsByteCode(DataOutputStream dataStream, Primitive type, int index, TextStream stream, Map<String, Map<String, Vertex>> elements, boolean outerBracket, boolean debug, Network network)
				throws IOException {
		dataStream.writeLong(network.createVertex(type).getId());
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
		int count = 0;
		while (moreArguments) {
			stream.skipWhitespace();
			peek = stream.peek();
			if (peek == ')' || peek == '}') {
				break;
			}
			if ((peek == ',') || (peek == ';'))  {
				break;
			}
			parseElementByteCode(stream, dataStream, elements, debug, network);
			count++;
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
		dataStream.writeLong(0l);
		return count;
	}

	/**
	 * Parse the function.
	 */
	public Vertex parseFunctionByteCode(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		stream.nextWord();
		try {
			Vertex function = parseElementName(Primitive.FUNCTION, stream, elements, debug, network);
			BinaryData byteCode = new BinaryData();
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			DataOutputStream dataStream = new DataOutputStream(byteStream);
			stream.skipWhitespace();
			ensureNext('{', stream);
			stream.skipWhitespace();
			char peek = stream.peek();
			while (peek != '}') {
				stream.skipWhitespace();
				parseElementByteCode(stream, dataStream, elements, debug, network);
				String previous = stream.peekPreviousWord();
				stream.skipWhitespace();
				if (!"}".equals(previous)) {
					ensureNext(';', ',', stream);
				}
				stream.skipWhitespace();
				peek = stream.peek();
				while (peek == ';') {
					stream.skip();
					stream.skipWhitespace();
					peek = stream.peek();
				}
			}
			ensureNext('}', stream);
			dataStream.writeLong(0l);
			byteCode.setBytes(byteStream.toByteArray());
			function.setData(byteCode);
			network.addVertex(function);
			return function;
		} catch (IOException exception) {
			throw new SelfParseException("IO Error", stream, exception);
		}
	}
	
	/**
	 * Parse the operator.
	 */
	public void parseOperatorByteCode(DataOutputStream dataStream, TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network)
				throws IOException {
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
		dataStream.writeLong(operator.getId());
		if (last.equals(IF)) {
			int arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 0, stream, elements, false, debug, network);
			ensureArguments(IF, 1, arguments, stream);
			stream.skipWhitespace();
			ensureNext('{', stream);
			parseArgumentsByteCode(dataStream, Primitive.THEN, 0, stream, elements, true, debug, network);
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
						dataStream.writeLong(network.createVertex(Primitive.ELSEIF).getId());
						parseElementByteCode(stream, dataStream, elements, debug, network);
						dataStream.writeLong(0l);
					} else {
						stream.skipWhitespace();
						ensureNext('{', stream);
						parseArgumentsByteCode(dataStream, Primitive.ELSE, 0, stream, elements, true, debug, network);
						stream.skipWhitespace();
						ensureNext('}', stream);
					}
				}
			}
		} else if (last.equals(WHILE)) {
			int arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 0, stream, elements, false, debug, network);
			ensureArguments(WHILE, 1, arguments, stream);
			stream.skipWhitespace();
			ensureNext('{', stream);
			parseArgumentsByteCode(dataStream, Primitive.DO, 0, stream, elements, true, debug, network);
			stream.skipWhitespace();
			ensureNext('}', stream);
		} else if (last.equals(DO) || last.equals(THINK)) {
			stream.skipWhitespace();
			ensureNext('{', stream);
			parseArgumentsByteCode(dataStream, Primitive.DO, 0, stream, elements, true, debug, network);
			stream.skipWhitespace();
			ensureNext('}', stream);
		} else if (last.equals(FOR)) {
			stream.skipWhitespace();
			ensureNext('(', stream);
			boolean more = true;
			dataStream.writeLong(network.createVertex(Primitive.ARGUMENT).getId());
			while (more) {
				parseElementByteCode(stream, dataStream, elements, debug, network);
				stream.skipWhitespace();
				ensureNext("in", stream);
				parseElementByteCode(stream, dataStream, elements, debug, network);
				stream.skipWhitespace();
				if (stream.peek() == ',') {
					stream.skip();
				} else {
					more = false;
				}
			}
			dataStream.writeLong(0l);
			ensureNext(')', stream);
			stream.skipWhitespace();
			ensureNext('{', stream);
			parseArgumentsByteCode(dataStream, Primitive.DO, 0, stream, elements, true, debug, network);
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
					dataStream.writeLong(network.createVertex(Primitive.ARGUMENT).getId());
					dataStream.writeLong(network.createVertex(new Primitive(type.toLowerCase())).getId());
					dataStream.writeLong(0l);
				} else {
					stream.setPosition(position);
					parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 0, stream, elements, false, debug, network);
				}
			} else {
				parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 0, stream, elements, false, debug, network);
			}
		} else {
			parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 0, stream, elements, false, debug, network);
		}
		dataStream.writeLong(0l);
	}
	
	/**
	 * Throw a parse error if the number of arguments does not match what is expected.
	 */
	protected void ensureArguments(String operator, int expected, int arguments, TextStream stream) {
		if (arguments != expected) {
			throw new SelfParseException("'" + operator + "' requires " + expected + " arguments not: " + arguments, stream);
		}
	}
	
	/**
	 * Parse the CASE condition.
	 */
	public void parseCaseByteCode(TextStream stream, DataOutputStream dataStream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network)
				throws IOException {
		stream.nextWord();
		stream.skipWhitespace();
		dataStream.writeLong(network.createVertex(Primitive.CASE).getId());
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
		Vertex variable = parseElementByteCode(stream, dataStream, elements, debug, network);
		if (!anyOrNone && variable != null && variable.instanceOf(Primitive.ARRAY)) {
			variable.addRelationship(Primitive.TYPE, Primitive.REQUIRED);
		}
		next = stream.nextWord();
		if (next.equalsIgnoreCase(AS)) {
			dataStream.writeLong(network.createVertex(Primitive.AS).getId());
			parseElementByteCode(stream, dataStream, elements, debug, network);
			next = stream.nextWord();
		}
		if (next.equalsIgnoreCase(TOPIC)) {
			dataStream.writeLong(network.createVertex(Primitive.TOPIC).getId());
			parseElementByteCode(stream, dataStream, elements, debug, network);
			next = stream.nextWord();
		}
		if (next.equalsIgnoreCase(THAT)) {
			dataStream.writeLong(network.createVertex(Primitive.THAT).getId());
			parseElementByteCode(stream, dataStream, elements, debug, network);
			next = stream.nextWord();
		}
		if (next.equalsIgnoreCase(GOTO)) {
			dataStream.writeLong(network.createVertex(Primitive.GOTO).getId());
			stream.skipWhitespace();
			boolean parseGoto = true;
			while (parseGoto) {
				dataStream.writeLong(parseElementName(Primitive.STATE, stream, elements, debug, network).getId());
				stream.skipWhitespace();
				if (stream.peek() == ',') {
					stream.skip();
				} else {
					parseGoto = false;
				}
			}
			dataStream.writeLong(0l);
		} else if (next.equalsIgnoreCase(TEMPLATE) || next.equalsIgnoreCase(ANSWER)) {
			dataStream.writeLong(network.createVertex(Primitive.TEMPLATE).getId());
			parseElementByteCode(stream, dataStream, elements, debug, network);
		} else if (next.equals(RETURN)) {
			dataStream.writeLong(network.createVertex(Primitive.GOTO).getId());
			dataStream.writeLong(network.createVertex(Primitive.RETURN).getId());
			dataStream.writeLong(0l);
		} else {
			stream.setPosition(stream.getPosition() - next.length());
			throw new SelfParseException("expected one of 'goto, template, answer, return, that, topic', found: " + next, stream);
		}
		next = stream.peekWord();
		if (next.equalsIgnoreCase(FOR)) {
			dataStream.writeLong(network.createVertex(Primitive.FOR).getId());
			stream.nextWord();
			ensureNext(EACH, stream);
			parseElementByteCode(stream, dataStream, elements, debug, network);
			ensureNext(OF, stream);
			parseElementByteCode(stream, dataStream, elements, debug, network);
			dataStream.writeLong(0l);
		}
		dataStream.writeLong(0l);
		ensureNext(';', stream);
	}
	
	/**
	 * Parse the PATTERN condition.
	 */
	public void parsePatternByteCode(TextStream stream, DataOutputStream dataStream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network)
					throws IOException {
		stream.nextWord();
		stream.skipWhitespace();
		dataStream.writeLong(network.createVertex(Primitive.CASE).getId());
		dataStream.writeLong(network.createVertex(Primitive.PATTERN).getId());
		Vertex pattern = null;
		if (stream.peek() == '"') {
			stream.skip();
			pattern = network.createPattern(stream.nextStringWithBracketsDoubleQuotes(), this);
			dataStream.writeLong(pattern.getId());
		} else {
			parseElementByteCode(stream, dataStream, elements, debug, network);
		}
		String next = stream.nextWord().toLowerCase();
		if (next.equals(TOPIC)) {
			dataStream.writeLong(network.createVertex(Primitive.TOPIC).getId());
			parseElementByteCode(stream, dataStream, elements, debug, network);
			next = stream.nextWord().toLowerCase();
		}
		if (next.equals(THAT)) {
			dataStream.writeLong(network.createVertex(Primitive.THAT).getId());
			Vertex that = null;
			stream.skipWhitespace();
			if (stream.peek() == '"') {
				stream.skip();
				that = network.createPattern(stream.nextStringWithBracketsDoubleQuotes(), this);
				dataStream.writeLong(that.getId());
			} else {
				parseElementByteCode(stream, dataStream, elements, debug, network);
			}
			next = stream.nextWord().toLowerCase();
		}
		if (next.equals(GOTO)) {
			dataStream.writeLong(network.createVertex(Primitive.GOTO).getId());
			stream.skipWhitespace();
			boolean parseGoto = true;
			while (parseGoto) {
				parseElementByteCode(stream, dataStream, elements, debug, network);
				stream.skipWhitespace();
				if (stream.peek() == ',') {
					stream.skip();
				} else {
					parseGoto = false;
				}
			}
			dataStream.writeLong(0l);
		} else if (next.equals(RETURN)) {
			dataStream.writeLong(network.createVertex(Primitive.GOTO).getId());
			dataStream.writeLong(network.createVertex(Primitive.RETURN).getId());
			dataStream.writeLong(0l);
		} else if (next.equals(TEMPLATE) || next.equals(ANSWER)) {
			dataStream.writeLong(network.createVertex(Primitive.TEMPLATE).getId());
			parseElementByteCode(stream, dataStream, elements, debug, network);
		} else {
			stream.setPosition(stream.getPosition() - next.length());
			throw new SelfParseException("expected one of GOTO, TEMPLATE, RETURN, THAT, TOPIC, found: " + next, stream);
		}
		dataStream.writeLong(0l);
		ensureNext(';', stream);
	}
	
	/**
	 * Parse the RETURN condition.
	 */
	public void parseReturnByteCode(TextStream stream, DataOutputStream dataStream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network)
					throws IOException {
		stream.nextWord();
		stream.skipWhitespace();
		dataStream.writeLong(network.createVertex(Primitive.RETURN).getId());
		if (stream.peek() != ';') {
			boolean with = stream.peekWord().toLowerCase().equals(WITH);
			if (!with) {
				parseElementByteCode(stream, dataStream, elements, debug, network);
				stream.skipWhitespace();
				with = stream.peekWord().toLowerCase().equals(WITH);
			}
			if (with) {
				stream.skipWord();
				stream.skipWhitespace();
				dataStream.writeLong(network.createVertex(Primitive.ARGUMENT).getId());
				if (stream.peek() == '(') {
					stream.skip();
					stream.skipWhitespace();
					parseElementByteCode(stream, dataStream, elements, debug, network);
					stream.skipWhitespace();
					while (stream.peek() == ',') {
						stream.skip();
						stream.skipWhitespace();
						parseElementByteCode(stream, dataStream, elements, debug, network);
					}
					ensureNext(')', stream);
				} else {
					parseElementByteCode(stream, dataStream, elements, debug, network);
				}
				dataStream.writeLong(0l);
			}
		}
		dataStream.writeLong(0l);
		ensureNext(';', stream);
	}
	
	/**
	 * Parse the GOTO condition.
	 */
	public void parseGotoByteCode(TextStream stream, DataOutputStream dataStream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network)
					throws IOException {
		stream.nextWord();
		dataStream.writeLong(network.createVertex(Primitive.GOTO).getId());
		stream.skipWhitespace();
		boolean gotoFinally = stream.peekWord().toLowerCase().equals(FINALLY);
		if (gotoFinally) {
			stream.nextWord();
			dataStream.writeLong(network.createVertex(Primitive.FINALLY).getId());
		}
		dataStream.writeLong(parseElementName(Primitive.STATE, stream, elements, debug, network).getId());
		if (stream.peek() != ';') {
			if (stream.peekWord().toLowerCase().equals(WITH)) {
				dataStream.writeLong(network.createVertex(Primitive.ARGUMENT).getId());
				stream.skipWord();
				stream.skipWhitespace();
				if (stream.peek() == '(') {
					stream.skip();
					stream.skipWhitespace();
					parseElementByteCode(stream, dataStream, elements, debug, network);
					stream.skipWhitespace();
					while (stream.peek() == ',') {
						stream.skip();
						stream.skipWhitespace();
						parseElementByteCode(stream, dataStream, elements, debug, network);
					}
					ensureNext(')', stream);
					dataStream.writeLong(0l);
				} else {
					parseElementByteCode(stream, dataStream, elements, debug, network);
					dataStream.writeLong(0l);
				}
			}
		}
		dataStream.writeLong(0l);
		ensureNext(';', stream);
	}
	
	/**
	 * Parse the PUSH condition.
	 */
	public void parsePushByteCode(TextStream stream, DataOutputStream dataStream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network)
					throws IOException {
		stream.nextWord();
		dataStream.writeLong(network.createVertex(Primitive.PUSH).getId());
		parseElementByteCode(stream, dataStream, elements, debug, network);
		ensureNext(';', stream);
	}
	
	/**
	 * Parse the DO condition.
	 */
	public void parseDoByteCode(TextStream stream, DataOutputStream dataStream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network)
					throws IOException {
		dataStream.writeLong(network.createVertex(Primitive.DO).getId());
		parseOperatorByteCode(dataStream, stream, elements, debug, network);
	}
}

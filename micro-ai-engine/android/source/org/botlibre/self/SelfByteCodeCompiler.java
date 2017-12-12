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
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

/**
 * Self scripting language compiler.
 * This compiler optimizes equations and states to compiled byte-code.
 */
public class SelfByteCodeCompiler extends SelfCompiler {

	/**
	 * Parse the code into a temporary equation so it can be evaluated.
	 */
	public Vertex parseEquationForEvaluation(String code, Vertex speaker, Vertex target, boolean debug, Network network) {
		TextStream stream = new TextStream(code);
		try {
			Map<String, Map<String, Vertex>> elements = new HashMap<String, Map<String, Vertex>>();
			elements.put(VARIABLE, new HashMap<String, Vertex>());
			elements.get(VARIABLE).put("speaker", speaker);
			elements.get(VARIABLE).put("target", target);
			elements.put(EQUATION, new HashMap<String, Vertex>());
			// Create a temporary equation to execute.
			Vertex equation = network.createTemporyVertex();
			equation.addRelationship(Primitive.INSTANTIATION, Primitive.EQUATION);
			BinaryData byteCode = new BinaryData();
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			DataOutputStream dataStream = new DataOutputStream(byteStream);
			dataStream.writeLong(network.createVertex(Primitive.DO).getId());
			dataStream.writeLong(network.createVertex(Primitive.ARGUMENT).getId());
			stream.skipWhitespace();
			char peek = stream.peek();
			if (peek == '{') {
				stream.next();
				peek = stream.peek();
				stream.skipWhitespace();
				while (peek != '}') {
					stream.skipWhitespace();
					parseElementByteCode(stream, dataStream, elements, debug, network);
					ensureNext(';', ',', stream);
					stream.skipWhitespace();
					peek = stream.peek();
				}
				ensureNext('}', stream);
			} else {
				parseElementByteCode(stream, dataStream, elements, debug, network);
				stream.skipWhitespace();
				if (!stream.atEnd()) {
					throw new SelfParseException("Unexpect element " + stream.peekWord(), stream);
				}
			}
			dataStream.writeLong(0l);
			dataStream.writeLong(0l);
			byteCode.setBytes(byteStream.toByteArray());
			equation.setData(byteCode);
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
	 * Parse the state and any referenced states or variables.
	 */
	public Vertex parseState(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		try {
			List<String> comments = null;
			Vertex state = parseElement(stream, elements, debug, network);
			BinaryData byteCode = new BinaryData();
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			DataOutputStream dataStream = new DataOutputStream(byteStream);
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
				} else if (element.equals(QUOTIENT) || element.equals(ANSWER)) {
					parseQuotientByteCode(stream, dataStream, elements, debug, network);
				} else if (element.equals(EQUATION) || element.equals(FUNCTION)) {
					parseEquation(stream, elements, debug, network);
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
	 * Parse the quotient.
	 * QUOTIENT:0.5:"World" { previous is "Hello"; previous is not "Hi"; }
	 */
	public void parseQuotientByteCode(TextStream stream, DataOutputStream dataStream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network)
				throws IOException {
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
				ensureNext("is", stream);
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
	 * Parse the reference to either a state, variable, equation, or raw data.
	 * One of,
	 * STATE:1234("name"), VARIABLE:1234("name"), EQUATION:1234("name"),
	 * 1234, "string", DATE("1972,01,01"), ...
	 */
	@SuppressWarnings("unchecked")
	public void parseElementByteCode(TextStream stream, DataOutputStream dataStream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network)
				throws IOException {
		getComments(stream);
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
				dataStream.writeLong(network.createVertex(Primitive.EQUATION).getId());
				//equation.setName(token); - cannot set name as named functions can return
				parseOperatorByteCode(dataStream, stream, elements, debug, network);
				return;
			} else if (TYPES.contains(token)) {
				stream.nextWord();
				if (token.equals(VAR)) {
					token = VARIABLE;
				} else {
					ensureNext(':', stream);
				}
				if (token.equals(FORMULA)) {
					dataStream.writeLong(parseFormula(null, stream, elements, debug, network).getId());
					return;
				}
				if (token.equals(PATTERN)) {
					ensureNext('"', stream);
					dataStream.writeLong(network.createPattern(stream.nextQuotesExcludeDoubleQuote(), this).getId());
					return;
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
							dataStream.writeLong(vertex.getId());
							return;
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
					dataStream.writeLong(vertex.getId());
					return;
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
				dataStream.writeLong(vertex.getId());
				return;
			}
			char next = stream.peek();
			try {
				if (next == '#') {
					stream.skip();
					String data = stream.upToAny(PRIMITIVE_TOKENS);
					dataStream.writeLong(network.createVertex(new Primitive(data)).getId());
					return;
				} else if (next == '"') {
					stream.skip();
					String data = stream.nextQuotesExcludeDoubleQuote();
					dataStream.writeLong(network.createVertex(data).getId());
					return;
				} else if (Character.isDigit(next) || next == '-' || next == '+') {
					String data = stream.nextWord();
					Vertex element = null;
					int index = data.indexOf('.');
					if (index != -1) {
						element = network.createVertex(new BigDecimal(data));
					} else {
						element = network.createVertex(new BigInteger(data));
					}
					dataStream.writeLong(element.getId());
					return;
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
					dataStream.writeLong(network.createVertex(data).getId());
					return;
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
	 * Parse the arguments to the equation.
	 */
	protected int parseArgumentsByteCode(DataOutputStream dataStream, Primitive type, int index, TextStream stream, Map<String, Map<String, Vertex>> elements, boolean bracket, boolean debug, Network network)
				throws IOException {
		dataStream.writeLong(network.createVertex(type).getId());
		if (!bracket) {
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
		dataStream.writeLong(0l);
		return count;
	}

	/**
	 * Parse the equation.
	 */
	public Vertex parseEquation(TextStream stream, Map<String, Map<String, Vertex>> elements, boolean debug, Network network) {
		try {
			Vertex equation = parseElement(stream, elements, debug, network);
			BinaryData byteCode = new BinaryData();
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			DataOutputStream dataStream = new DataOutputStream(byteStream);
			dataStream.writeLong(network.createVertex(Primitive.DO).getId());
			dataStream.writeLong(network.createVertex(Primitive.ARGUMENT).getId());
			stream.skipWhitespace();
			ensureNext('{', stream);
			stream.skipWhitespace();
			char peek = stream.peek();
			while (peek != '}') {
				stream.skipWhitespace();
				parseElementByteCode(stream, dataStream, elements, debug, network);
				ensureNext(';', ',', stream);
				stream.skipWhitespace();
				peek = stream.peek();
			}
			ensureNext('}', stream);
			dataStream.writeLong(0l);
			dataStream.writeLong(0l);
			byteCode.setBytes(byteStream.toByteArray());
			equation.setData(byteCode);
			network.addVertex(equation);
			return equation;
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
		dataStream.writeLong(operator.getId());
		stream.skipWhitespace();
		next = lower(stream.peekWord());
		if (NOT.equals(next)) {
			stream.nextWord();
			dataStream.writeLong(network.createVertex(Primitive.NOT).getId());
		}
		int arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 0, stream, elements, false, debug, network);
		if (last.equals(IF)) {
			if (arguments != 1) {
				ensureArguments(IF, 2, arguments, stream);
			}
			next = lower(stream.peekWord());
			while (OR.equals(next) || AND.equals(next)) {
				boolean or = OR.equals(next);
				boolean and = AND.equals(next);
				if (or) {
					dataStream.writeLong(network.createVertex(Primitive.OR).getId());
				} else if (and) {
					dataStream.writeLong(network.createVertex(Primitive.AND).getId());
				}
				stream.nextWord();
				next = lower(stream.peekWord());
				if (NOT.equals(next)) {
					stream.nextWord();
					dataStream.writeLong(network.createVertex(Primitive.NOT).getId());
					next = lower(stream.peekWord());
				}
				boolean bracket = false;
				while ("(".equals(next)) {
					bracket = true;
					dataStream.writeLong(network.createVertex(Primitive.LEFTBRACKET).getId());
					stream.nextWord();
					next = lower(stream.peekWord());
				}
				parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 0, stream, elements, bracket, debug, network);
				next = lower(stream.peekWord());
				if (bracket) {
					while (")".equals(next)) {
						dataStream.writeLong(network.createVertex(Primitive.RIGHTBRACKET).getId());
						stream.nextWord();
						next = lower(stream.peekWord());
					}
				}
			}
			if (THEN.equals(next)) {
				stream.nextWord();
				parseArgumentsByteCode(dataStream, Primitive.THEN, 0, stream, elements, false, debug, network);
				next = lower(stream.peekWord());					
			}
			if (ELSE.equals(next)) {
				stream.nextWord();
				parseArgumentsByteCode(dataStream, Primitive.ELSE, 0, stream, elements, false, debug, network);
			}
		} else if (last.equals(WHILE)) {
			if (arguments != 1) {
				ensureArguments(WHILE, 2, arguments, stream);
			}
			ensureNext(DO, stream);
			parseArgumentsByteCode(dataStream, Primitive.DO, 0, stream, elements, false, debug, network);
		} else if (last.equals(FOR)) {
			ensureArguments(FOR, 1, arguments, stream);
			ensureNext(OF, stream);
			arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(OF, 1, arguments, stream);
			ensureNext(AS, stream);
			arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
			ensureArguments(AS, 1, arguments, stream);
			next = lower(stream.peekWord());
			int index = 3;
			while (AND.equals(next)) {
				stream.nextWord();
				ensureNext(EACH, stream);
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, index++, stream, elements, false, debug, network);
				ensureArguments(EACH, 1, arguments, stream);
				ensureNext(OF, stream);
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, index++, stream, elements, false, debug, network);
				ensureArguments(OF, 1, arguments, stream);
				ensureNext(AS, stream);
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, index++, stream, elements, false, debug, network);
				ensureArguments(AS, 1, arguments, stream);
				next = lower(stream.peekWord());
			}
			if (DO.equals(next)) {
				stream.nextWord();
				parseArgumentsByteCode(dataStream, Primitive.DO, 0, stream, elements, false, debug, network);
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
			arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(FROM, 1, arguments, stream);
			next = stream.peekWord();
			if ((next != null) && ASSOCIATED.equals(next.toLowerCase())) {
				stream.nextWord();
				next = lower(stream.peekWord());
				ensureNext(TO, stream);
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
				ensureArguments(TO, 1, arguments, stream);
				ensureNext(BY, stream);
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 3, stream, elements, false, debug, network);
				ensureArguments(BY, 1, arguments, stream);
			} else if ((next != null) && AT.equals(next.toLowerCase())) {
				stream.nextWord();
				next = lower(stream.peekWord());
				if ((next != null) && LAST.equals(next.toLowerCase())) {
					stream.nextWord();
					arguments = parseArgumentsByteCode(dataStream, Primitive.LASTINDEX, 0, stream, elements, false, debug, network);
					ensureArguments(AT, 1, arguments, stream);
				} else {
					arguments = parseArgumentsByteCode(dataStream, Primitive.INDEX, 0, stream, elements, false, debug, network);
					ensureArguments(AT, 1, arguments, stream);
				}
			}
		} else if (last.equals(LEARN)) {
			ensureArguments(LEARN, 1, arguments, stream);
			next = stream.peekWord();
			if ((next != null) && THAT.equals(next.toLowerCase())) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.THAT, 0, stream, elements, false, debug, network);
				ensureArguments(THAT, 1, arguments, stream);
				next = stream.peekWord();
			}
			if ((next != null) && TOPIC.equals(next.toLowerCase())) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.TOPIC, 0, stream, elements, false, debug, network);
				ensureArguments(TOPIC, 1, arguments, stream);
			}
			ensureNext(TEMPLATE, stream);
			arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(TEMPLATE, 1, arguments, stream);
		} else if (last.equals(INPUT)) {
			ensureArguments(INPUT, 1, arguments, stream);
			next = stream.peekWord();
			int forIndex = 1;
			if ((next != null) && PART.equals(next.toLowerCase())) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
				ensureArguments(PART, 1, arguments, stream);
				next = stream.peekWord();
				forIndex = 2;
			}
			if ((next != null) && FOR.equals(next.toLowerCase())) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, forIndex, stream, elements, false, debug, network);
				ensureArguments(FOR, 1, arguments, stream);
			}
		} else if (last.equals(ALL)) {
			ensureArguments(ALL, 1, arguments, stream);
			ensureNext(FROM, stream);
			arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(FROM, 1, arguments, stream);
			next = stream.peekWord();
			if ((next != null) && ASSOCIATED.equals(next.toLowerCase())) {
				stream.nextWord();
				next = lower(stream.peekWord());
				ensureNext(TO, stream);
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
				ensureArguments(TO, 1, arguments, stream);
				ensureNext(BY, stream);
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 3, stream, elements, false, debug, network);
				ensureArguments(BY, 1, arguments, stream);
			}
		} else if (last.equals(COUNT)) {
			ensureArguments(COUNT, 1, arguments, stream);
			next = stream.peekWord();
			if ((next != null) && OF.equals(next.toLowerCase())) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
				ensureArguments(OF, 1, arguments, stream);
			}
		} else if (last.equals(SET)) {
			ensureArguments(last, 1, arguments, stream);
			ensureNext(TO, stream);
			arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(TO, 1, arguments, stream);
			next = lower(stream.peekWord());
			if (ON.equals(next)) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
				ensureArguments(ON, 1, arguments, stream);
			}
		} else if (last.equals(RELATION)) {
			ensureArguments(IS, 1, arguments, stream);
			ensureNext(RELATED, stream);
			ensureNext(TO, stream);
			arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(RELATED, 1, arguments, stream);
			next = lower(stream.peekWord());
			if (BY.equals(next)) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
				ensureArguments(BY, 1, arguments, stream);
			}
		} else if (last.equals(RELATED)) {
			ensureArguments(RELATED, 1, arguments, stream);
			next = lower(stream.peekWord());
			if (BY.equals(next)) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
				ensureArguments(BY, 1, arguments, stream);
			}
		} else if (last.equals(ASSOCIATE) || last.equals(DISSOCIATE) || last.equals(WEAKASSOCIATE)) {
			ensureArguments(last, 1, arguments, stream);
			ensureNext(TO, stream);
			arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(TO, 1, arguments, stream);
			ensureNext(BY, stream);
			arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
			ensureArguments(BY, 1, arguments, stream);
			next = lower(stream.peekWord());
			if (WITH.equals(next)) {
				stream.nextWord();
				ensureNext(META, stream);
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 3, stream, elements, false, debug, network);
				ensureArguments(META, 1, arguments, stream);
				ensureNext(AS, stream);
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 4, stream, elements, false, debug, network);
				ensureArguments(AS, 1, arguments, stream);
			}
		} else if (last.equals(ASSIGN)) {
			ensureArguments(ASSIGN, 1, arguments, stream);
			ensureNext(TO, stream);
			arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(TO, 1, arguments, stream);
		} else if (last.equals(DEFINE)) {
			ensureArguments(ASSIGN, 1, arguments, stream);
			ensureNext(AS, stream);
			arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
		} else if (last.equals(EVAL)) {
			ensureArguments(EVAL, 1, arguments, stream);
		} else if (last.equals(NOT)) {
			ensureArguments(NOT, 1, arguments, stream);
		} else if (last.equals(APPEND)) {
			ensureArguments(APPEND, 1, arguments, stream);
			ensureNext(TO, stream);
			arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(TO, 1, arguments, stream);
			ensureNext(OF, stream);
			arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
			ensureArguments(OF, 1, arguments, stream);
			next = lower(stream.peekWord());
			if (WITH.equals(next)) {
				stream.nextWord();
				ensureNext(META, stream);
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 3, stream, elements, false, debug, network);
				ensureArguments(META, 1, arguments, stream);
				ensureNext(AS, stream);
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 4, stream, elements, false, debug, network);
				ensureArguments(AS, 1, arguments, stream);
			}
		} else if (last.equals(CALL)) {
			ensureArguments(CALL, 1, arguments, stream);
			ensureNext(ON, stream);
			arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 1, stream, elements, false, debug, network);
			ensureArguments(ON, 1, arguments, stream);
			next = lower(stream.peekWord());
			if (WITH.equals(next)) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.ARGUMENT, 2, stream, elements, false, debug, network);
			}
		} else if (last.equals(FORMAT)) {
			ensureNext(AS, stream);
			arguments = parseArgumentsByteCode(dataStream, Primitive.AS, 1, stream, elements, false, debug, network);
			ensureArguments(AS, 1, arguments, stream);
		} else if (last.equals(SRAI) || last.equals(REDIRECT)) {
			ensureArguments(SRAI, 1, arguments, stream);
		} else if (last.equals(SRAIX)  || last.equals(REQUEST)) {
			ensureArguments(SRAI, 1, arguments, stream);
			next = lower(stream.peekWord());
			if ("bot".equals(next)) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.BOT, 0, stream, elements, false, debug, network);
			}
			next = lower(stream.peekWord());
			if ("botid".equals(next)) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.BOTID, 0, stream, elements, false, debug, network);
			}
			next = lower(stream.peekWord());
			if ("service".equals(next)) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.SERVICE, 0, stream, elements, false, debug, network);
			}
			next = lower(stream.peekWord());
			if ("server".equals(next)) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.SERVER, 0, stream, elements, false, debug, network);
			}
			next = lower(stream.peekWord());
			if ("apikey".equals(next)) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.APIKEY, 0, stream, elements, false, debug, network);
			}
			next = lower(stream.peekWord());
			if ("limit".equals(next)) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.LIMIT, 0, stream, elements, false, debug, network);
			}
			next = lower(stream.peekWord());
			if ("hint".equals(next)) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.HINT, 0, stream, elements, false, debug, network);
			}
			next = lower(stream.peekWord());
			if ("default".equals(next)) {
				stream.nextWord();
				arguments = parseArgumentsByteCode(dataStream, Primitive.DEFAULT, 0, stream, elements, false, debug, network);
			}
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
		Vertex variable = parseElement(stream, elements, debug, network);
		dataStream.writeLong(variable.getId());
		String next = stream.nextWord().toLowerCase();
		if (next.equals(AS)) {
			dataStream.writeLong(network.createVertex(Primitive.AS).getId());
			parseElementByteCode(stream, dataStream, elements, debug, network);
			next = stream.nextWord().toLowerCase();
		}
		if (next.equals(TOPIC)) {
			dataStream.writeLong(network.createVertex(Primitive.TOPIC).getId());
			parseElementByteCode(stream, dataStream, elements, debug, network);
			next = stream.nextWord().toLowerCase();
		}
		if (next.equals(THAT)) {
			dataStream.writeLong(network.createVertex(Primitive.THAT).getId());
			parseElementByteCode(stream, dataStream, elements, debug, network);
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
		} else if (next.equals(TEMPLATE) || next.equals(ANSWER)) {
			dataStream.writeLong(network.createVertex(Primitive.TEMPLATE).getId());
			parseElementByteCode(stream, dataStream, elements, debug, network);
		} else if (next.equals(RETURN)) {
			dataStream.writeLong(network.createVertex(Primitive.GOTO).getId());
			dataStream.writeLong(network.createVertex(Primitive.RETURN).getId());
			dataStream.writeLong(0l);
		} else {
			stream.setPosition(stream.getPosition() - next.length());
			throw new SelfParseException("expected one of GOTO, TEMPLATE, ANSWER, RETURN, THAT, TOPIC, found: " + next, stream);
		}
		next = stream.peekWord().toLowerCase();
		if (next.equals(FOR)) {
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
			pattern = network.createPattern(stream.nextQuotesExcludeDoubleQuote(), this);
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
				that = network.createPattern(stream.nextQuotesExcludeDoubleQuote(), this);
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
		parseElementByteCode(stream, dataStream, elements, debug, network);
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
		ensureNext(';', stream);
	}
}

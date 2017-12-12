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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Writer;
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
import org.botlibre.knowledge.BinaryData;
import org.botlibre.knowledge.Primitive;
import org.botlibre.util.Utils;

/**
 * Decompiler for the Self scripting language object code.
 * Self is compiled into Vertex objects, this converts the object code back into Self code text.
 */
public class Self4Decompiler extends SelfDecompiler {
	
	public static Map<Primitive, String> BINARY_OPERATORS;
	static {
		BINARY_OPERATORS = new HashMap<Primitive, String>();
		BINARY_OPERATORS.put(Primitive.EQUALS, "==");
		BINARY_OPERATORS.put(Primitive.NOTEQUALS, "!=");
		BINARY_OPERATORS.put(Primitive.OR, "||");
		BINARY_OPERATORS.put(Primitive.AND, "&&");
		BINARY_OPERATORS.put(Primitive.GREATERTHAN, ">");
		BINARY_OPERATORS.put(Primitive.LESSTHAN, "<");
		BINARY_OPERATORS.put(Primitive.GREATERTHANEQUAL, ">=");
		BINARY_OPERATORS.put(Primitive.LESSTHANEQUAL, "<=");
		BINARY_OPERATORS.put(Primitive.MINUS, "-");
		BINARY_OPERATORS.put(Primitive.PLUS, "+");
		BINARY_OPERATORS.put(Primitive.MULTIPLY, "*");
		BINARY_OPERATORS.put(Primitive.DIVIDE, "/");
	}

	/**
	 * Print the Self code for the state machine.
	 */
	@Override
	public void printStateMachine(Vertex state, Writer writer, Network network, long start, long timeout) {
		try {
			if (!state.hasRelationship(Primitive.LANGUAGE) || state.hasRelationship(Primitive.LANGUAGE, Primitive.SELF2)) {
				new SelfDecompiler().printStateMachine(state, writer, network, start, timeout);
				return;
			}
			Set<Vertex> elements = new HashSet<Vertex>();
			elements.add(state);
			printState(state, writer, "", elements, network, start, timeout);
			writer.write("\r\n");
		} catch (IOException error) {
			network.getBot().log(this, error);
			return;
		}
	}
	
	/**
	 * Print the state and any referenced states or variables that have not yet been printed.
	 */
	@Override
	public void printState(Vertex state, Writer writer, String indent, Set<Vertex> elements, Network network, long start, long timeout) throws IOException {
		if (state.getData() instanceof BinaryData) {
			Vertex detached = parseStateByteCode(state, (BinaryData)state.getData(), network);
			elements.add(detached);
			printState(detached, writer, indent, elements, network, start, timeout);
			return;
		}
		printComments(state, writer, indent, false, network);
		writer.write(indent);
		writer.write("state ");
		printElement(state, writer, indent, null, null, elements, network);
		writer.write(" {\r\n");
		if ((System.currentTimeMillis() - start) > timeout) {
			writer.write(indent);
			writer.write("\t");
			writer.write("** decompile timeout reached **\r\n");
			writer.write(indent);
			writer.write("}\r\n");
			return;
		}
		String childIndent = indent + "\t";
		Collection<Relationship> expressions = state.orderedRelationships(Primitive.DO);
		List<Vertex> newFunctions = new ArrayList<Vertex>();
		List<Vertex> newVariables = new ArrayList<Vertex>();
		List<Vertex> newStates = new ArrayList<Vertex>();
		if (expressions != null) {
			for (Relationship expression : expressions) {
				printComments(expression.getTarget(), writer, childIndent, false, network);
				if (expression.getTarget().instanceOf(Primitive.CASE)) {
					printCase(expression.getTarget(), writer, childIndent, elements, newVariables, newFunctions, newStates, network);
				} else if (expression.getTarget().instanceOf(Primitive.DO)) {
					printDo(expression.getTarget().getRelationship(Primitive.DO), writer, indent, elements, newVariables, newFunctions, newStates, network);
				} else if (expression.getTarget().instanceOf(Primitive.GOTO)) {
					printGoto(expression.getTarget(), writer, childIndent, elements, newVariables, newFunctions, newStates, network);
				} else if (expression.getTarget().instanceOf(Primitive.PUSH)) {
					printPush(expression.getTarget(), writer, childIndent, elements, newVariables, newFunctions, newStates, network);
				} else if (expression.getTarget().instanceOf(Primitive.RETURN)) {
					printReturn(expression.getTarget(), writer, childIndent, elements, newVariables, newFunctions, newStates, network);
				}
			}
		}
		for (Vertex variable : newVariables) {
			printVariable(variable, writer, childIndent, elements, network);
		}
		for (Vertex function : newFunctions) {
			printFunction(function, writer, childIndent, elements, network);
		}
		newFunctions = new ArrayList<Vertex>();
		newVariables = new ArrayList<Vertex>();
		Collection<Relationship> quotients = state.orderedRelationships(Primitive.QUOTIENT);
		if (quotients != null) {
			for (Relationship quotient : quotients) {
				writer.write(childIndent);
				writer.write("answer");
				if (quotient.getCorrectness() < 1.0f) {
					writer.write(":");
					writer.write(String.format("%.02f", quotient.getCorrectness()));
				}
				writer.write(" ");
				printElement(quotient.getTarget(), writer, indent, newFunctions, newVariables, elements, network);
				if (quotient.hasMeta()) {
					writer.write(" {\r\n");
					Collection<Relationship> previousRelationships = quotient.getMeta().orderedRelationships(Primitive.PREVIOUS);
					if (previousRelationships != null) {
						for (Relationship previous : previousRelationships) {
							writer.write(childIndent);
							if (previous.getCorrectness() > 0) {
								writer.write("\tprevious ");
							} else {
								writer.write("\tprevious ! ");								
							}
							printElement(previous.getTarget(), writer, indent + 1, newFunctions, newVariables, elements, network);
							writer.write(";\r\n");							
						}
					}
					writer.write(childIndent);
					writer.write("}");
				}
				writer.write(";\r\n");
			}
			writer.write("\r\n");
		}
		Collection<Relationship> possibleQuotients = state.orderedRelationships(Primitive.POSSIBLE_QUOTIENT);
		if (possibleQuotients != null) {
			for (Relationship quotient : possibleQuotients) {
				writer.write(childIndent);
				writer.write("//Possible answer:");
				printElement(quotient.getTarget(), writer, indent, newFunctions, newVariables, elements, network);
				writer.write(";\r\n");
			}
			writer.write("\r\n");
		}
		for (Vertex variable : newVariables) {
			printVariable(variable, writer, childIndent, elements, network);
		}
		for (Vertex function : newFunctions) {
			printFunction(function, writer, childIndent, elements, network);
		}
		for (Vertex element : newStates) {
			if (element.instanceOf(Primitive.STATE)) {
				printState(element, writer, childIndent, elements, network, start, timeout);
			}
		}
		writer.write(indent);
		writer.write("}\r\n");
		writer.write("\r\n");
	}

	/**
	 * Print the vertex, either a state, variable, expression, or data.
	 */
	@Override
	public void printData(Vertex vertex, Writer writer) throws IOException {
		Object data = vertex.getData();
		if (data instanceof Primitive) {
			if (!data.equals(Primitive.NULL) && !data.equals(Primitive.TRUE) && !data.equals(Primitive.FALSE)) {
				writer.write("#");
			}
			writer.write(((Primitive)vertex.getData()).getIdentity());
		} else if (data instanceof String) {
			writer.write("\"");
			String text = (String)vertex.getData();
			if (text.indexOf('"') != -1) {
				text = text.replace("\"", "\\\"");
			}
			writer.write(text);
			writer.write("\"");
		} else if (data instanceof Number) {
			// TODO number type.
			writer.write(vertex.getData().toString());
		} else {
			writer.write(vertex.getDataType());
			writer.write("(\"");
			writer.write(vertex.getDataValue());
			writer.write("\")");
		}
	}
	
	/**
	 * Print the vertex, either a state, variable, expression, or data.
	 */
	@Override
	public void printElement(Vertex vertex, Writer writer, String indent, List<Vertex> functions, List<Vertex> variables, Set<Vertex> elements, Network network) throws IOException {
		if (vertex == null) {
			writer.write("null");
			return;			
		}
		if (vertex.instanceOf(Primitive.STATE)) {
			// Just print name.
		} else if (vertex.instanceOf(Primitive.VARIABLE)) {
			// Just print name.
		} else if (vertex.instanceOf(Primitive.FORMULA)) {
			if (vertex.hasData()) {
				writer.write(String.valueOf(vertex.getData()));
			} else {
				printTemplate(vertex, writer, indent, functions, variables, elements, network);
			}
			return;
		} else if (vertex.instanceOf(Primitive.PATTERN)) {
			writer.write(String.valueOf(vertex.getData()));
			return;
		} else if (vertex.instanceOf(Primitive.FUNCTION)) {
			if (!elements.contains(vertex)) {
				functions.add(vertex);
				elements.add(vertex);
			}
		} else if (vertex.instanceOf(Primitive.EXPRESSION)) {
			if (vertex.getData() instanceof BinaryData) {
				Vertex detached = parseExpressionByteCode(vertex, (BinaryData)vertex.getData(), network);
				elements.add(detached);
				vertex = detached;
			}
			printOperator(vertex, writer, indent, functions, variables, elements, network);
			return;
		} else if (vertex.instanceOf(Primitive.PARAGRAPH)) {
			writer.write("\"");
			String text = vertex.printString();
			if (text.indexOf('"') != -1) {
				text = text.replace("\"", "\\\"");
			}
			writer.write(text);
			writer.write("\"");
			return;
		} else if (vertex.hasData()) {
			Object data = vertex.getData();
			if (data instanceof Primitive) {
				if (!data.equals(Primitive.NULL) && !data.equals(Primitive.TRUE) && !data.equals(Primitive.FALSE)) {
					writer.write("#");
				}
				writer.write(((Primitive)vertex.getData()).getIdentity());
			} else if (data instanceof String) {
				writer.write("\"");
				String text = (String)vertex.getData();
				if (text.indexOf('"') != -1) {
					text = text.replace("\"", "\\\"");
				}
				writer.write(text);
				writer.write("\"");
			} else if (data instanceof Number) {
				// TODO number type.
				writer.write(vertex.getData().toString());
			} else {
				writer.write(vertex.getDataType());
				writer.write("(\"");
				writer.write(vertex.getDataValue());
				writer.write("\")");
			}
			return;
		} else if (vertex.instanceOf(Primitive.ARRAY)) {
			writer.write("[");
			List<Vertex> values = vertex.orderedRelations(Primitive.ELEMENT);
			if (values != null) {
				boolean first = true;
				for (Vertex value : values) {
					if (!first) {
						writer.write(", ");
					} else {
						first = false;
					}
					printElement(value, writer, indent, functions, variables, elements, network);
				}
			}
			writer.write("]");
			return;
		} else {
			writer.write("Object(");
			writer.write(vertex.getId().toString());
			writer.write(")");
			return;
		}
		if (vertex.hasName()) {
			String name = Utils.compress(vertex.getName(), 100);
			writer.write(name);
		} else {
			writer.write("v");
			writer.write(vertex.getId().toString());
		}
		if (vertex.instanceOf(Primitive.FUNCTION)) {
			List<Relationship> arguments = vertex.orderedRelationships(Primitive.ARGUMENT);
			if (arguments == null) {
				writer.write("()");
			} else {
				printArguments(vertex, Primitive.ARGUMENT, 0, null, false, false, false, true, writer, indent + "\t", variables, functions, elements, false, network);
			}
		}
	}

	@Override
	public void printTemplate(Vertex formula, Writer writer, String indent, List<Vertex> expressions, List<Vertex> variables, Set<Vertex> elements, Network network) throws IOException {
		writer.write("Template(");
		writer.write("\"");
		boolean first = true;
		List<Vertex> words = formula.orderedRelations(Primitive.WORD);
		if (words == null) {
			writer.write("\"");
			return;
		}
		boolean inferWhitespace = !formula.hasRelationship(Primitive.TYPE, Primitive.SPACE) && !formula.hasRelationship(Primitive.WORD, Primitive.SPACE);
		for (Vertex word : words) {
			if (inferWhitespace) {
				if (!first) {
					if (!word.instanceOf(Primitive.PUNCTUATION)) {
						writer.write(" ");
					}
				} else {
					first = false;
				}
			} else if (word.is(Primitive.SPACE)) {
				writer.write(" ");
				continue;
			}
			if (word.instanceOf(Primitive.WORD)) {
				String text = String.valueOf(word.getData());
				if (text.equals("\"")) {
					writer.write("\\");					
				}
				writer.write(text);
			} else {
				writer.write("{");
				printElement(word, writer, indent, expressions, variables, elements, network);
				writer.write("}");				
			}
		}
		writer.write("\")");
	}
	
	/**
	 * Print the variable and any variables it references that have not been printed.
	 */
	@Override
	public void printVariable(Vertex variable, Writer writer, String indent, Set<Vertex> elements, Network network) throws IOException {
		String name = variable.getName();
		if (name != null && (name.equals("input") || name.equals("sentence") || name.equals("conversation") || name.equals("speaker") || name.equals("target"))) {
			// Don't print globals.
			return;
		}
		boolean hasComments = printComments(variable, writer, indent, false, network);
		Iterator<Relationship> iterator = variable.allRelationships();
		if (!hasComments && variable.totalRelationships() <= 1) {
			return; // Nothing to print.
		}
		List<Vertex> localElements = new ArrayList<Vertex>();
		writer.write(indent);
		writer.write("var ");
		printElement(variable, writer, indent, null, null, elements, network);
		writer.write(" {\r\n");
		writer.write(indent);
		while (iterator.hasNext()) {
			Relationship relationship = iterator.next();
			Vertex type = relationship.getType();
			Vertex target = relationship.getTarget();
			if ((type.is(Primitive.INSTANTIATION) && (target.is(Primitive.VARIABLE)))) {
				continue;
			}
			if (type.is(Primitive.COMMENT)) {
				continue;
			}
			if (target.instanceOf(Primitive.VARIABLE) && !elements.contains(target)) {
				localElements.add(target);
				elements.add(target);
			}
			if (type.is(Primitive.EQUALS)) {
				writer.write("\t");
				if (relationship.isInverse()) {
					writer.write(" : ! ");
				} else {
					writer.write(" : ");				
				}
				printElement(target, writer, indent, null, null, elements, network);
			} else {
				writer.write("\t");
				if (type.isPrimitive()) {
					writer.write(((Primitive)type.getData()).getIdentity());					
				} else {
					printElement(type, writer, indent, null, null, elements, network);
				}
				if (relationship.isInverse()) {
					writer.write(" : ! ");
				} else {
					writer.write(" : ");				
				}
				printElement(target, writer, indent, null, null, elements, network);
			}
			writer.write(";\r\n");
			writer.write(indent);
		}
		writer.write("}\r\n");
		writer.write("\r\n");
		for (Vertex element : localElements) {
			printVariable(element, writer, indent, elements, network);
		}
	}

	/**
	 * Print the operation arguments.
	 */
	public void printArguments(Vertex expression, Primitive type, int start, String[] tokens, boolean reverse, boolean newLine, boolean unravel, boolean brackets, Writer writer, String indent, List<Vertex> variables, List<Vertex> functions, Set<Vertex> elements, boolean space, Network network) throws IOException {
		List<Relationship> arguments = expression.orderedRelationships(type);
		if (brackets) {
			writer.write("(");
		}
		if (arguments != null) {
			boolean needsBrackets = !unravel && !brackets;			
			if ((arguments.size() == 1) || (tokens != null)) {
				needsBrackets = false;
			}
			if (arguments.size() <= (start + 1)) {
				newLine = false;
			} else if (arguments.size() > (3 + start)) {
				newLine = true;
			}
			if (!unravel && space) {
				writer.write(" ");
			}
			if (needsBrackets) {
				writer.write("(");
			}
			int size = arguments.size();
			boolean isDo = false;
			for (int index = start; index < size; index++) {
				if (newLine && (!unravel || (index > 0))) {
					writer.write("\r\n");
					writer.write(indent);
					writer.write("\t");
					writer.write("\t");
				}
				Vertex argument = null;
				isDo = false;
				if (reverse) {
					argument = arguments.get(size - index - 1).getTarget();
				} else {
					argument = arguments.get(index).getTarget();
				}
				if (argument.instanceOf(Primitive.VARIABLE) && !elements.contains(argument)) {
					variables.add(argument);
					elements.add(argument);
				}
				boolean isExpression = argument.instanceOf(Primitive.EXPRESSION);
				if (!unravel && !needsBrackets && isExpression && !brackets) {
					writer.write("(");					
				}
				printElement(argument, writer, indent, functions, variables, elements, network);
				if (!unravel && !needsBrackets && isExpression && !brackets) {
					writer.write(")");
				}
				Vertex operator = argument.getRelationship(Primitive.OPERATOR);
				if (operator != null && !argument.instanceOf(Primitive.FUNCTION)
							&& (operator.is(Primitive.DO) || operator.is(Primitive.IF) || operator.is(Primitive.WHILE) || operator.is(Primitive.FOR))) {
					isDo = true;
				}
				if (index < (size - 1)) {
					if (tokens != null) {
						writer.write(" ");
						writer.write(tokens[index]);
						writer.write(" ");
					} else if (!isDo) {
						if (unravel) {
							writer.write(";");
						} else {
							writer.write(",");
						}
						if (!newLine) {
							writer.write(" ");						
						}
					}
				}
			}
			if (unravel) {
				if (!isDo) {
					writer.write(";");					
				}
				writer.write("\r\n");				
			}
			if (!unravel && newLine) {
				writer.write("\r\n");
				writer.write(indent);
				writer.write("\t");
			}
			if (needsBrackets) {
				writer.write(")");
			}
		}
		if (brackets) {
			writer.write(")");
		}
	}

	/**
	 * Parse the operation arguments.
	 */
	@Override
	public void parseArgumentsByteCode(Vertex expression, DataInputStream dataStream, Vertex type, Network network) throws IOException {
		parseArgumentsByteCode(expression, dataStream, type, null, network);
	}

	/**
	 * Parse the operation arguments.
	 */
	public void parseArgumentsByteCode(Vertex expression, DataInputStream dataStream, Vertex type, Vertex pop, Network network) throws IOException {
		long id = dataStream.readLong();
		Object[]  result = new Object[3];
		result[0] = id;
		while (id > 0) {
			parseArgumentByteCode(result, dataStream,  pop, network);
			Vertex argument = (Vertex)result[2];
			id = (Long)result[0];
			if (argument != null) {
				expression.addRelationship(type, argument, Integer.MAX_VALUE);
			}
		}
	}

	/**
	 * Parse the operation argument.
	 */
	public void parseArgumentByteCode(Object[] result, DataInputStream dataStream, Vertex pop, Network network) throws IOException {
		Vertex last = null;
		Vertex next = (Vertex)result[1];
		Long id = (Long)result[0];
		if (id == 0l) {
			result[0] = id;
			result[1] = null;
			result[2] = null;
			return;
		}
		Vertex element = next;
		if (element == null) {
			element = network.findById(id);
		}
		if (element == null) {
			result[0] = dataStream.readLong();
			result[1] = null;
			result[2] = null;
			return;
		}
		if (element.is(Primitive.EXPRESSION)) {
			element = parseOperatorByteCode(dataStream, network);
		}
		if (element.is(Primitive.POP)) {
			element = pop;
		}
		id = dataStream.readLong();
		if (id == 0l) {
			result[0] = id;
			result[1] = null;
			result[2] = element;
			return;
		}
		last = element;
		next = network.findById(id);
		while ((next != null) && (next.is(Primitive.PUSH))) {
			element = parseOperatorByteCode(dataStream, last, network);
			id = dataStream.readLong();
			if (id == 0l) {
				next = null;
				break;
			}
			last = element;
			next = network.findById(id);
		}
		result[0] = id;
		result[1] = next;
		result[2] = element;
	}
	
	/**
	 * Check if the function is bytecode and decompile.
	 */
	@Override
	public Vertex decompileFunction(Vertex function, Network network) {
		if (function.getData() instanceof BinaryData) {
			try {
				return parseFunctionByteCode(function, (BinaryData)function.getData(), network);
			} catch (IOException exception) {
				throw new SelfExecutionException(function, exception);
			}
		}
		return function;
	}
	
	/**
	 * Check if the expression is bytecode and decompile.
	 */
	@Override
	public Vertex decompileExpression(Vertex expression, Network network) {
		if (expression.getData() instanceof BinaryData) {
			try {
				return parseExpressionByteCode(expression, (BinaryData)expression.getData(), network);
			} catch (IOException exception) {
				throw new SelfExecutionException(expression, exception);
			}
		}
		return expression;
	}
	
	/**
	 * Check if the state is bytecode and decompile.
	 */
	@Override
	public Vertex decompileState(Vertex state, Network network) {
		if (state.getData() instanceof BinaryData) {
			try {
				return parseStateByteCode(state, (BinaryData)state.getData(), network);
			} catch (Exception exception) {
				throw new SelfExecutionException(state, exception);
			}
		}
		return state;
	}
	
	/**
	 * Print the function and any functions it references that have not been printed.
	 */
	public void printFunction(Vertex function, Writer writer, String indent, Set<Vertex> elements, Network network) throws IOException {
		if (function.getData() instanceof BinaryData) {
			Vertex detached = parseFunctionByteCode(function, (BinaryData)function.getData(), network);
			elements.add(detached);
			printFunction(detached, writer, indent, elements, network);
			return;
		}
		printComments(function, writer, indent, false, network);
		List<Vertex> functions = new ArrayList<Vertex>();
		List<Vertex> variables = new ArrayList<Vertex>();
		writer.write(indent);
		writer.write("function ");
		printElement(function, writer, indent, functions, variables, elements, network);
		writer.write(" {\r\n");
		writer.write(indent);
		writer.write("\t");
		printArguments(function, Primitive.DO, 0, null, false, true, true, false, writer, indent.substring(0, indent.length() - 1), variables, functions, elements, true, network);
		writer.write(indent);
		writer.write("}\r\n");
		writer.write("\r\n");
		for (Vertex element : variables) {
			printVariable(element, writer, indent, elements, network);
		}
		for (Vertex element : functions) {
			printFunction(element, writer, indent, elements, network);
		}
	}
	
	/**
	 * Parse the function from bytecode.
	 */
	public Vertex parseFunctionByteCode(Vertex function, BinaryData data, Network network) throws IOException {
		if (data.getCache() != null) {
			return (Vertex)data.getCache();
		}
		BinaryData bytes = data;
		if (!function.isTemporary()) {
			bytes = (BinaryData)network.findData(data);
			if (bytes == null) {
				bytes = data;
			}
		}
		ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes.getBytes());
		DataInputStream dataStream = new DataInputStream(byteStream);

		Vertex cache = network.createTemporyVertex();
		cache.setName(function.getName());
		cache.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);
		cache.addRelationship(Primitive.OPERATOR, new Primitive(function.getName()));
		parseArgumentsByteCode(cache, dataStream, network.createVertex(Primitive.DO), network);
		
		data.setCache(cache);
		bytes.setCache(cache);
		return cache;
	}
	
	/**
	 * Parse the expression from bytecode.
	 */
	@Override
	public Vertex parseExpressionByteCode(Vertex expression, BinaryData data, Network network) throws IOException {
		if (data.getCache() != null) {
			return (Vertex)data.getCache();
		}
		BinaryData bytes = data;
		if (!expression.isTemporary()) {
			bytes = (BinaryData)network.findData(data);
			if (bytes == null) {
				bytes = data;
			}
		}
		if (bytes.getBytes() == null) {
			return network.createVertex(Primitive.NULL);
		}
		ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes.getBytes());
		DataInputStream dataStream = new DataInputStream(byteStream);
		
		Object[] result = new Object[3];
		result[0] = dataStream.readLong();
		parseArgumentByteCode(result, dataStream, null, network);
		Vertex cache = (Vertex)result[2];
		if (cache == null) {
			return expression;
		}
		
		cache.setName(expression.getName());
		data.setCache(cache);
		bytes.setCache(cache);
		return cache;
	}
	
	/**
	 * Parse the Self2 equation from bytecode.
	 */
	@Override
	public Vertex parseEquationByteCode(Vertex equation, BinaryData data, Network network) throws IOException {
		return new SelfDecompiler().parseEquationByteCode(equation, data, network);
	}
	
	/**
	 * Parse the state from bytecode.
	 */
	@Override
	public Vertex parseStateByteCode(Vertex state, BinaryData data, Network network) throws IOException {
		if (data.getCache() != null) {
			return (Vertex)data.getCache();
		}
		BinaryData bytes = data;
		if (!state.isTemporary()) {
			bytes = (BinaryData)network.findData(data);
			if (bytes == null) {
				bytes = data;
			}
		}
		ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes.getBytes());
		DataInputStream dataStream = new DataInputStream(byteStream);
		long id = dataStream.readLong();
		Vertex vertex = network.findById(id);
		if (vertex != null && !vertex.is(Primitive.SELF4)) {
			// Parse old state machine.
			return new SelfDecompiler().parseStateByteCode(state, data, network);
		}
		Vertex cache = parseStateByteCode(dataStream, network);
		// Add any dynamically added cases.
		Collection<Relationship> cases = state.getRelationships(Primitive.DO);
		if (cases != null) {
			for (Relationship expression : cases) {
				cache.addRelationship(expression, true);
			}
		}
		cache.setName(state.getName());
		data.setCache(cache);
		bytes.setCache(cache);
		return cache;
	}

	
	/**
	 * Parse the state and its cases from bytecode.
	 */
	@Override
	public Vertex parseStateByteCode(DataInputStream dataStream, Network network) throws IOException {
		Vertex state = network.createTemporyVertex();
		state.addRelationship(Primitive.INSTANTIATION, Primitive.STATE);
		try {
			long id = dataStream.readLong();
			while (id > 0) {
				Vertex next = network.findById(id);
				Vertex vertex = null;
				if (next == null) {
					id = dataStream.readLong();
					continue;
				}
				if (next.is(Primitive.CASE)) {
					vertex = parseCaseByteCode(dataStream, network);
					state.addRelationship(Primitive.DO, vertex, Integer.MAX_VALUE);
				} else if (next.is(Primitive.QUOTIENT)) {
					parseQuotientByteCode(state, dataStream, network);
				} else if (next.is(Primitive.DO)) {
					vertex = parseDoByteCode(dataStream, network);
					state.addRelationship(Primitive.DO, vertex, Integer.MAX_VALUE);
				} else if (next.is(Primitive.GOTO)) {
					vertex = parseGotoByteCode(dataStream, network);
					state.addRelationship(Primitive.DO, vertex, Integer.MAX_VALUE);
				} else if (next.is(Primitive.PUSH)) {
					vertex = parsePushByteCode(dataStream, network);
					state.addRelationship(Primitive.DO, vertex, Integer.MAX_VALUE);
				} else if (next.is(Primitive.RETURN)) {
					vertex = parseReturnByteCode(dataStream, network);
					state.addRelationship(Primitive.DO, vertex, Integer.MAX_VALUE);
				}
				id = dataStream.readLong();
			}
		} catch (Exception exception) {
			network.getBot().log(this, "Error parsing state bytecode", Level.WARNING, state);
			network.getBot().log(this, exception);
		}
		
		return state;
	}
	
	/**
	 * Parse the CASE bytecode.
	 */
	@Override
	public Vertex parseCaseByteCode(DataInputStream dataStream, Network network) throws IOException {
		Vertex vertex = network.createTemporyVertex();
		vertex.addRelationship(Primitive.INSTANTIATION, Primitive.CASE);
		long id = dataStream.readLong();
		if (id == 0) {
			return vertex;
		}
		Object[] result = new Object[3];
		result[0] = id;
		parseArgumentByteCode(result, dataStream, null, network);
		id = (Long)result[0];
		Vertex variable = (Vertex)result[2];
		if (variable == null) {
			return vertex;
		}
		if (variable.is(Primitive.PATTERN)) {
			if (id == 0) {
				return vertex;
			}
			variable = network.findById(id);
			if (variable == null) {
				return vertex;
			}
			vertex.addRelationship(Primitive.PATTERN, variable);
			id = dataStream.readLong();
		} else {
			vertex.addRelationship(Primitive.CASE, variable);
		}
		while (id > 0) {
			Vertex type = network.findById(id);
			if (type == null) {
				return vertex;
			}
			id = dataStream.readLong();
			if (type.is(Primitive.GOTO) || type.is(Primitive.FOR)) {
				while (id > 0) {
					Vertex element = network.findById(id);
					if (element == null) {
						id = dataStream.readLong();
						continue;
					}
					vertex.addRelationship(type, element);
					id = dataStream.readLong();
				}
				id = dataStream.readLong();
				continue;
			} else {
				result[0] = id;
				result[1] = null;
				result[2] = null;
				parseArgumentByteCode(result, dataStream, null, network);
				id = (Long)result[0];
				Vertex argument = (Vertex)result[2];
				if (argument != null) {
					vertex.addRelationship(type, argument, Integer.MAX_VALUE);
				}
			}
		}
		return vertex;
	}
	
	/**
	 * Parse the PUSH bytecode.
	 */
	@Override
	public Vertex parsePushByteCode(DataInputStream dataStream, Network network) throws IOException {
		Vertex expression = network.createTemporyVertex();
		expression.addRelationship(Primitive.INSTANTIATION, Primitive.PUSH);
		long id = dataStream.readLong();
		if (id == 0) {
			return expression;
		}
		Vertex element = network.findById(id);
		if (element != null) {
			expression.addRelationship(Primitive.ARGUMENT, element, Integer.MAX_VALUE);
		}
		return expression;
	}
	
	/**
	 * Parse the DO bytecode.
	 */
	@Override
	public Vertex parseDoByteCode(DataInputStream dataStream, Network network) throws IOException {
		Vertex expression = network.createTemporyVertex();
		expression.addRelationship(Primitive.INSTANTIATION, Primitive.DO);
		Vertex operation = parseOperatorByteCode(dataStream, network);
		expression.addRelationship(Primitive.DO, operation, Integer.MAX_VALUE);
		return expression;
	}
	
	/**
	 * Parse the RETURN bytecode.
	 */
	@Override
	public Vertex parseReturnByteCode(DataInputStream dataStream, Network network) throws IOException {
		Vertex expression = network.createTemporyVertex();
		expression.addRelationship(Primitive.INSTANTIATION, Primitive.RETURN);
		long id = dataStream.readLong();
		if (id == 0) {
			return expression;
		}
		Vertex element = network.findById(id);
		if (element != null) {
			if (element.is(Primitive.ARGUMENT)) {
				expression.addRelationship(Primitive.RETURN, element);
				id = dataStream.readLong();
				while (id > 0) {
					element = network.findById(id);
					if (element != null) {
						expression.addRelationship(Primitive.ARGUMENT, element, Integer.MAX_VALUE);
					}
					id = dataStream.readLong();
				}
			} else {
				expression.addRelationship(Primitive.RETURN, element);
			}
		}
		return expression;
	}
	
	/**
	 * Parse the GOTO bytecode.
	 */
	@Override
	public Vertex parseGotoByteCode(DataInputStream dataStream, Network network) throws IOException {
		Vertex expression = network.createTemporyVertex();
		expression.addRelationship(Primitive.INSTANTIATION, Primitive.GOTO);
		long id = dataStream.readLong();
		if (id == 0) {
			return expression;
		}
		Vertex element = network.findById(id);
		if (element == null) {
			return expression;
		}
		if (element.is(Primitive.FINALLY)) {
			expression.addRelationship(Primitive.FINALLY, Primitive.FINALLY);
			id = dataStream.readLong();
			if (id == 0) {
				return expression;
			}
			element = network.findById(id);
			if (element == null) {
				return expression;
			}
		}
		expression.addRelationship(Primitive.GOTO, element);
		id = dataStream.readLong();
		if (id == 0) {
			return expression;
		}
		element = network.findById(id);
		if (element == null) {
			return expression;
		}
		if (element.is(Primitive.ARGUMENT)) {
			id = dataStream.readLong();
			while (id > 0) {
				element = network.findById(id);
				if (element != null) {
					expression.addRelationship(Primitive.ARGUMENT, element, Integer.MAX_VALUE);
				}
				id = dataStream.readLong();
			}
			id = dataStream.readLong();
		}
		return expression;
	}

	
	/**
	 * Parse the GOTO bytecode.
	 */
	@Override
	public void parseQuotientByteCode(Vertex state, DataInputStream dataStream, Network network) throws IOException {
		float correctness = dataStream.readFloat();
		long id = dataStream.readLong();
		if (id == 0) {
			return;
		}
		Object[] result = new Object[3];
		result[0] = id;
		parseArgumentByteCode(result, dataStream, null, network);
		id = (Long)result[0];
		Vertex element = (Vertex)result[2];
		if (element == null) {
			return;
		}
		Relationship relationship = state.addWeakRelationship(Primitive.QUOTIENT, element, correctness);
		if (id == 0) {
			return;
		}
		result[0] = id;
		result[1] = null;
		result[2] = null;
		parseArgumentByteCode(result, dataStream, null, network);
		id = (Long)result[0];
		element = (Vertex)result[2];
		if (element == null) {
			return;
		}
		if (element.is(Primitive.PREVIOUS)) {
			Vertex meta = network.createTemporyVertex();
			relationship.setMeta(meta);
			while (id > 0) {
				result[0] = id;
				result[1] = null;
				result[2] = null;
				parseArgumentByteCode(result, dataStream, null, network);
				id = (Long)result[0];
				element = (Vertex)result[2];
				if (element != null) {
					if (element.is(Primitive.NOT)) {
						if (id == 0) {
							return;
						}
						result[0] = id;
						result[1] = null;
						result[2] = null;
						parseArgumentByteCode(result, dataStream, null, network);
						id = (Long)result[0];
						element = (Vertex)result[2];
						if (element == null) {
							continue;
						}
						meta.removeRelationship(Primitive.PREVIOUS, element);
					} else {
						meta.addRelationship(Primitive.PREVIOUS, element);						
					}
				}
			}
		}
	}
	
	/**
	 * Print the expression and any expressions it references that have not been printed.
	 */
	@Override
	public void printOperator(Vertex expression, Writer writer, String indent, List<Vertex> functions, List<Vertex> variables, Set<Vertex> elements, Network network) throws IOException {
		printComments(expression, writer, indent + "\t\t", true, network);
		Vertex operator = expression.getRelationship(Primitive.OPERATOR);
		if (operator == null) {
			return;			
		}
		List<Relationship> arguments = expression.orderedRelationships(Primitive.ARGUMENT);
		if (operator.is(Primitive.CALL)) {
			Vertex source = expression.getRelationship(Primitive.THIS);
			if (source != null) {
				printElement(source, writer, indent, functions, variables, elements, network);				
			}
			Vertex function = expression.getRelationship(Primitive.FUNCTION);
			if (function != null) {
				if (source == null) {
					if (function.isPrimitive()) {
						writer.write(((Primitive)function.getData()).getIdentity());
					} else {
						printElement(function, writer, indent, functions, variables, elements, network);
					}
				} else {
					if (function.isPrimitive() && !(source.getData() instanceof Number)) {
						writer.write(".");
						writer.write(((Primitive)function.getData()).getIdentity());
					} else {
						writer.write("[");
						printElement(function, writer, indent, functions, variables, elements, network);
						writer.write("]");
					}
				}
			} else {
				writer.write("** missing function **");
			}
			printArguments(expression, Primitive.ARGUMENT, 0, null, false, false, false, true, writer, indent + "\t", variables, functions, elements, false, network);
			return;
		} else if (operator.is(Primitive.ASSIGN)) {
			if (arguments != null && !arguments.isEmpty()) {
				printElement(arguments.get(0).getTarget(), writer, indent, functions, variables, elements, network);				
			}
			if (expression.hasRelationship(Primitive.NOT, Primitive.NOT)) {
				writer.write(" = ! ");
			} else {
				writer.write(" = ");				
			}
			if (arguments != null && arguments.size() > 1) {
				printElement(arguments.get(1).getTarget(), writer, indent, functions, variables, elements, network);				
			}
			return;
		} else if (operator.is(Primitive.INCREMENT)) {
			if (arguments != null && !arguments.isEmpty()) {
				printElement(arguments.get(0).getTarget(), writer, indent, functions, variables, elements, network);				
			}
			writer.write(" ++");
			return;
		} else if (operator.is(Primitive.DECREMENT)) {
			if (arguments != null && !arguments.isEmpty()) {
				printElement(arguments.get(0).getTarget(), writer, indent, functions, variables, elements, network);				
			}
			writer.write(" --");
			return;
		} else if (operator.is(Primitive.GET)) {
			Vertex source = null;
			if (arguments != null && !arguments.isEmpty()) {
				source = arguments.get(0).getTarget();
				printElement(source, writer, indent, functions, variables, elements, network);				
			}
			if (arguments != null && arguments.size() > 1) {
				Vertex variable = arguments.get(1).getTarget();
				if (variable.isPrimitive()) {
					writer.write(".");
					writer.write(((Primitive)variable.getData()).getIdentity());
					Vertex index = expression.getRelationship(Primitive.INDEX);
					if ((index != null) && (index.getData() instanceof Number)) {
						writer.write("[");
						writer.write(index.getData().toString());
						writer.write("]");
					} else if (arguments.size() > 3) {
						Vertex associate = arguments.get(2).getTarget();
						Vertex associateRelationship = arguments.get(3).getTarget();
						writer.write("[");
						printElement(associate, writer, indent, functions, variables, elements, network);
						writer.write(", ");
						printElement(associateRelationship, writer, indent, functions, variables, elements, network);
						writer.write("]");
					}
				} else {
					writer.write("[");
					printElement(variable, writer, indent, functions, variables, elements, network);
					writer.write("]");
				}
			}
			return;
		} else if (operator.is(Primitive.SET)) {
			Vertex source = null;
			if (arguments != null && !arguments.isEmpty()) {
				source = arguments.get(0).getTarget();
				printElement(source, writer, indent, functions, variables, elements, network);				
			}
			if (arguments != null && arguments.size() > 2) {
				Vertex variable = arguments.get(1).getTarget();
				Vertex value = arguments.get(2).getTarget();
				if (variable.isPrimitive() && !(source.getData() instanceof Number)) {
					writer.write(".");
					writer.write(((Primitive)variable.getData()).getIdentity());
					Vertex index = expression.getRelationship(Primitive.INDEX);
					if ((index != null) && (index.getData() instanceof Number)) {
						writer.write("[");
						writer.write(index.getData().toString());
						writer.write("]");
					}
				} else {
					writer.write("[");
					printElement(variable, writer, indent, functions, variables, elements, network);
					writer.write("]");
				}
				writer.write(" = ");
				printElement(value, writer, indent, functions, variables, elements, network);
			}
			return;
		} else if (operator.is(Primitive.ADD)) {
			Vertex source = null;
			if (arguments != null && !arguments.isEmpty()) {
				source = arguments.get(0).getTarget();
				printElement(source, writer, indent, functions, variables, elements, network);				
			}
			if (arguments != null && arguments.size() > 2) {
				Vertex variable = arguments.get(1).getTarget();
				Vertex value = arguments.get(2).getTarget();
				if (variable.isPrimitive() && !(source.getData() instanceof Number)) {
					writer.write(".");
					writer.write(((Primitive)variable.getData()).getIdentity());
					Vertex index = expression.getRelationship(Primitive.INDEX);
					if ((index != null) && (index.getData() instanceof Number)) {
						writer.write("[");
						writer.write(index.getData().toString());
						writer.write("]");
					}
				} else {
					writer.write("[");
					printElement(variable, writer, indent, functions, variables, elements, network);
					writer.write("]");
				}
				writer.write(" =+ ");
				printElement(value, writer, indent, functions, variables, elements, network);
			}
			return;
		} else if (operator.is(Primitive.REMOVE)) {
			Vertex source = null;
			if (arguments != null && !arguments.isEmpty()) {
				source = arguments.get(0).getTarget();
				printElement(source, writer, indent, functions, variables, elements, network);				
			}
			if (arguments != null && arguments.size() > 2) {
				Vertex variable = arguments.get(1).getTarget();
				Vertex value = arguments.get(2).getTarget();
				if (variable.isPrimitive() && !(source.getData() instanceof Number)) {
					writer.write(".");
					writer.write(((Primitive)variable.getData()).getIdentity());
					Vertex index = expression.getRelationship(Primitive.INDEX);
					if ((index != null) && (index.getData() instanceof Number)) {
						writer.write("[");
						writer.write(index.getData().toString());
						writer.write("]");
					}
				} else {
					writer.write("[");
					printElement(variable, writer, indent, functions, variables, elements, network);
					writer.write("]");
				}
				writer.write(" =- ");
				printElement(value, writer, indent, functions, variables, elements, network);
			}
			return;
		} else if (BINARY_OPERATORS.containsKey(operator.getData())) {
			if (arguments != null && !arguments.isEmpty()) {
				Vertex left = arguments.get(0).getTarget();
				boolean bracket = left.instanceOf(Primitive.EXPRESSION);
				if (bracket) {
					writer.write("(");
				}
				printElement(left, writer, indent, functions, variables, elements, network);
				if (bracket) {
					writer.write(")");
				}
			}
			writer.write(" ");
			writer.write(BINARY_OPERATORS.get(operator.getData()));
			writer.write(" ");
			if (arguments != null && arguments.size() > 1) {
				Vertex right = arguments.get(1).getTarget();
				boolean bracket = right.instanceOf(Primitive.EXPRESSION);
				if (bracket) {
					writer.write("(");
				}
				printElement(arguments.get(1).getTarget(), writer, indent, functions, variables, elements, network);
				if (bracket) {
					writer.write(")");
				}				
			}
			return;
		} else if (operator.is(Primitive.NOT)) {
			writer.write("! ");
			if (arguments != null && arguments.size() > 0) {
				printElement(arguments.get(0).getTarget(), writer, indent, functions, variables, elements, network);				
			}
			return;
		} else {
			writer.write(((Primitive)operator.getData()).getIdentity());
		}
		// Print arguments.
		if (operator.is(Primitive.WHILE)) {
			writer.write(" ");
			// Print arguments.
			printArguments(expression, Primitive.ARGUMENT, 0, null, false, false, false, true, writer, indent, variables, functions, elements, false, network);
			// Print do.
			Collection<Relationship> dos = expression.orderedRelationships(Primitive.DO);
			if (dos != null) {
				String newIndent = indent + "\t";
				writer.write(" {\r\n");
				writer.write(newIndent);
				writer.write("\t");
				writer.write("\t");
				printArguments(expression, Primitive.DO, 0, null, false, true, true, false, writer, newIndent, variables, functions, elements, true, network);
				writer.write(newIndent);
				writer.write("\t");
				writer.write("}");
			} else {
				writer.write(" {}");				
			}
		} else if (operator.is(Primitive.DO) || operator.is(Primitive.THINK)) {
			// Print do.
			Collection<Relationship> dos = expression.orderedRelationships(Primitive.DO);
			if (dos != null) {
				String newIndent = indent + "\t";
				writer.write(" {\r\n");
				writer.write(newIndent);
				writer.write("\t");
				writer.write("\t");
				printArguments(expression, Primitive.DO, 0, null, false, true, true, false, writer, newIndent, variables, functions, elements, true, network);
				writer.write(newIndent);
				writer.write("\t");
				writer.write("}");
			} else {
				writer.write(" {}");
			}
		} else if (operator.is(Primitive.FOR)) {
			writer.write(" (");
			int index = 0;
			while (arguments != null && (arguments.size() > index)) {
				if (index > 0) {
					writer.write(", ");
				}
				printElement(arguments.get(index++).getTarget(), writer, indent, functions, variables, elements, network);
				writer.write(" in ");
				printElement(arguments.get(index++).getTarget(), writer, indent, functions, variables, elements, network);
				index = index + 2;
			}
			writer.write(") ");
			// Print do.
			Collection<Relationship> dos = expression.orderedRelationships(Primitive.DO);
			if (dos != null) {
				String newIndent = indent + "\t";
				writer.write(" {\r\n");
				writer.write(newIndent);
				writer.write("\t");
				writer.write("\t");
				printArguments(expression, Primitive.DO, 0, null, false, true, true, false, writer, newIndent, variables, functions, elements, true, network);
				writer.write(newIndent);
				writer.write("\t");
				writer.write("}");
			} else {
				writer.write(" {}");				
			}
		} else if (operator.is(Primitive.IF)) {
			writer.write(" ");
			// Print arguments.
			printArguments(expression, Primitive.ARGUMENT, 0, null, false, false, false, true, writer, indent, variables, functions, elements, false, network);
			// Print then.
			Collection<Relationship> thens = expression.orderedRelationships(Primitive.THEN);
			Collection<Relationship> elses = expression.orderedRelationships(Primitive.ELSE);
			Collection<Relationship> elseifs = expression.orderedRelationships(Primitive.ELSEIF);
			if (thens == null && elseifs == null && elses == null) {
				writer.write(" {}\r\n");
			} else {
				String newIndent = indent + "\t";
				writer.write(" {\r\n");
				if (thens != null) {
					writer.write(newIndent);
					writer.write("\t");
					writer.write("\t");
					printArguments(expression, Primitive.THEN, 0, null, false, true, true, false, writer, newIndent, variables, functions, elements, true, network);
					if (elses == null && elseifs == null) {
						writer.write(newIndent);
						writer.write("\t");
						writer.write("}");
					}
				}
				// Print else ifs.
				if (elseifs != null) {
					writer.write(newIndent);
					writer.write("\t");
					writer.write("} else ");
					for (Relationship elseif : elseifs) {
						printOperator(elseif.getTarget(), writer, indent, functions, variables, elements, network);
					}
				}
				// Print else.
				if (elses != null) {
					if (elseifs == null) {
						writer.write(newIndent);
						writer.write("\t");
						writer.write("}");
					}
					writer.write(" else {\r\n");
					writer.write(newIndent);
					writer.write("\t");
					writer.write("\t");
					printArguments(expression, Primitive.ELSE, 0, null, false, true, true, false, writer, newIndent, variables, functions, elements, true, network);
					writer.write(newIndent);
					writer.write("\t");
					writer.write("}");
				}
			}
		} else {
			writer.write(" ");
			// Print arguments.
			printArguments(expression, Primitive.ARGUMENT, 0, null, false, (operator.is(Primitive.DO)), false, true, writer, indent, variables, functions, elements, false, network);
		}
	}
	
	/**
	 * Parse the operator and its arguments from bytecode.
	 */
	@Override
	public Vertex parseOperatorByteCode(DataInputStream dataStream, Network network) throws IOException {
		return parseOperatorByteCode(dataStream, null, network);
	}
	
	/**
	 * Parse the operator and its arguments from bytecode.
	 */
	public Vertex parseOperatorByteCode(DataInputStream dataStream, Vertex pop, Network network) throws IOException {
		Vertex expression = network.createTemporyVertex();
		expression.addRelationship(Primitive.INSTANTIATION, Primitive.EXPRESSION);
		long id = dataStream.readLong();
		Vertex operator = network.findById(id);
		if (operator == null) {
			return expression;
		}
		expression.setName(operator.getDataValue());
		expression.addRelationship(Primitive.OPERATOR, operator);
		id = dataStream.readLong();
		if (id == 0) {
			return expression;
		}
		while (id > 0) {
			Vertex next = network.findById(id);
			if (next == null) {
				return expression;
			}
			parseArgumentsByteCode(expression, dataStream, next, pop, network);
			id = dataStream.readLong();
		}
		return expression;
	}
	
	/**
	 * Print the IF condition and any variables and states that it references.
	 */
	@Override
	public void printCase(Vertex expression, Writer writer, String indent, Set<Vertex> elements,
					List<Vertex> newVariables, List<Vertex> newFunctions, List<Vertex> newStates, Network network)
					throws IOException {
		Vertex variable = expression.getRelationship(Primitive.CASE);
		Vertex pattern = expression.getRelationship(Primitive.PATTERN);
		Vertex template = expression.getRelationship(Primitive.TEMPLATE);
		Vertex that = expression.getRelationship(Primitive.THAT);
		Vertex topic = expression.getRelationship(Primitive.TOPIC);
		Vertex as = expression.getRelationship(Primitive.AS);
		List<Relationship> states = expression.orderedRelationships(Primitive.GOTO);
		List<Relationship> fors = expression.orderedRelationships(Primitive.FOR);
		if (variable == null && pattern == null) {
			return;
		}
		if ((variable != null) && variable.instanceOf(Primitive.VARIABLE) && (!elements.contains(variable))) {
			newVariables.add(variable);
			elements.add(variable);
		}
		writer.write(indent);
		if (pattern != null) {
			writer.write("pattern ");
			variable = pattern;
		} else {
			writer.write("case ");
		}
		if (variable.instanceOf(Primitive.EXPRESSION)) {
			writer.write("(");
		} else if (variable.instanceOf(Primitive.ARRAY)) {
			writer.write("any ");
			if (!variable.hasRelationship(Primitive.TYPE, Primitive.REQUIRED)) {
				writer.write("or none ");
			}
		}
		printElement(variable, writer, indent, newFunctions, newVariables, elements, network);
		if (variable.instanceOf(Primitive.EXPRESSION)) {
			writer.write(")");
		}
		if (as != null) {
			writer.write(" as ");
			printElement(as, writer, indent, newFunctions, newVariables, elements, network);
		}
		if (topic != null) {
			writer.write("\r\n");
			writer.write(indent);
			writer.write("\t");
			writer.write("topic ");
			printElement(topic, writer, indent, newFunctions, newVariables, elements, network);
		}
		if (that != null) {
			writer.write("\r\n");
			writer.write(indent);
			writer.write("\t");
			writer.write("that ");
			printElement(that, writer, indent, newFunctions, newVariables, elements, network);
		}
		if (template != null) {
			writer.write("\r\n");
			writer.write(indent);
			writer.write("\t");
			writer.write("template ");
			if (template.instanceOf(Primitive.EXPRESSION)) {
				writer.write("(");			
			}
			printElement(template, writer, indent, newFunctions, newVariables, elements, network);
			if (template.instanceOf(Primitive.EXPRESSION)) {
				writer.write(")");			
			}
		}
		if (states != null) {
			if ((states.size() == 1) && (states.get(0).getTarget().is(Primitive.RETURN))) {
				writer.write(" return");				
			} else {
				writer.write(" goto ");
				for (Iterator<Relationship> iterator = states.iterator(); iterator.hasNext(); ) {
					Vertex state = iterator.next().getTarget();
					if (!elements.contains(state)) {
						newStates.add(state);
						elements.add(state);
					}
					printElement(state, writer, indent, null, newVariables, elements, network);
					if (iterator.hasNext()) {			
						writer.write(", ");
					}
				}
			}
		}
		if (fors != null) {
			writer.write(" for each ");
			for (Iterator<Relationship> iterator = fors.iterator(); iterator.hasNext(); ) {
				Vertex argument = iterator.next().getTarget();
				if (argument.instanceOf(Primitive.VARIABLE) && (!elements.contains(argument))) {
					newVariables.add(argument);
					elements.add(argument);
				}
				printElement(argument, writer, indent, null, newVariables, elements, network);
				if (iterator.hasNext()) {			
					writer.write(" of ");
				}
			}
		}
		writer.write(";\r\n\r\n");
	}
	
	/**
	 * Print the GOTO condition and any variables and states that it references.
	 */
	public void printGoto(Vertex expression, Writer writer, String indent, Set<Vertex> elements,
				List<Vertex> newVariables, List<Vertex> newFunctions, List<Vertex> newStates, Network network) throws IOException {
		Vertex state = expression.getRelationship(Primitive.GOTO);
		writer.write(indent);
		writer.write("goto ");
		if (expression.hasRelationship(Primitive.FINALLY)) {
			writer.write("finally ");
		}
		if (!elements.contains(state)) {
			newStates.add(state);
			elements.add(state);
		}
		printElement(state, writer, indent, newFunctions, newVariables, elements, network);
		Collection<Relationship> arguments = expression.getRelationships(Primitive.ARGUMENT);
		if (arguments != null) {
			writer.write(" with (");
			for (Iterator<Relationship> iterator = arguments.iterator(); iterator.hasNext(); ) {
				Relationship argument = iterator.next();
				printElement(argument.getTarget(), writer, indent, newFunctions, newVariables, elements, network);
				if (iterator.hasNext()) {
					writer.write(", ");					
				}
			}
			writer.write(")");
		}
		writer.write(";\r\n\r\n");
	}
	
	/**
	 * Print the PUSH condition and any variables and states that it references.
	 */
	public void printPush(Vertex expression, Writer writer, String indent, Set<Vertex> elements,
				List<Vertex> newVariables, List<Vertex> newFunctions, List<Vertex> newStates, Network network) throws IOException {
		Vertex state = expression.getRelationship(Primitive.ARGUMENT);
		writer.write(indent);
		writer.write("push ");
		if (!elements.contains(state)) {
			newStates.add(state);
			elements.add(state);
		}
		printElement(state, writer, indent, newFunctions, newVariables, elements, network);
		writer.write(";\r\n\r\n");
	}
	
	/**
	 * Print the DO operation.
	 */
	public void printDo(Vertex expression, Writer writer, String indent, Set<Vertex> elements,
				List<Vertex> newVariables, List<Vertex> newFunctions, List<Vertex> newStates, Network network) throws IOException {
		printComments(expression, writer, indent, false, network);
		writer.write(indent);
		writer.write("\t");
		writer.write("do");
		Collection<Relationship> dos = expression.getRelationships(Primitive.DO);
		if (dos != null) {
			writer.write(" {\r\n");
			writer.write(indent);
			writer.write("\t");
			writer.write("\t");
			printArguments(expression, Primitive.DO, 0, null, false, true, true, false, writer, indent, newVariables, newFunctions, elements, true, network);
			writer.write(indent);
			writer.write("\t");
			writer.write("}\r\n");
		} else {
			writer.write(" {}\r\n");				
		}
	}
	
	/**
	 * Print the RETURN condition and any variables it references.
	 */
	public void printReturn(Vertex expression, Writer writer, String indent, Set<Vertex> elements,
			List<Vertex> newVariables, List<Vertex> newFunctions, List<Vertex> newStates, Network network) throws IOException {
		Vertex result = expression.getRelationship(Primitive.RETURN);
		writer.write(indent);
		writer.write("return");
		if (result != null) {
			writer.write(" ");
			printElement(result, writer, indent, newFunctions, newVariables, elements, network);
		}
		Collection<Relationship> arguments = expression.getRelationships(Primitive.ARGUMENT);
		if (arguments != null) {
			writer.write(" with (");
			for (Iterator<Relationship> iterator = arguments.iterator(); iterator.hasNext(); ) {
				Relationship argument = iterator.next();
				printElement(argument.getTarget(), writer, indent, newFunctions, newVariables, elements, network);
				if (iterator.hasNext()) {
					writer.write(", ");					
				}
			}
			writer.write(")");
		}
		writer.write(";\r\n\r\n");
	}
}

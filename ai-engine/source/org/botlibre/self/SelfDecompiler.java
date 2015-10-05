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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.AbstractNetwork;
import org.botlibre.knowledge.Primitive;
import org.botlibre.util.Utils;

/**
 * Utility class for printing the Self programming model.
 * Self is the language that Bot programs herself in.
 */
public class SelfDecompiler {
	public static long TIMEOUT = 10000;
	protected static SelfDecompiler decompiler;
	
	/** Printing ids doesn't work, recompile will link back to same state. */
	protected boolean printIds;

	public static SelfDecompiler getDecompiler() {
		if (decompiler == null) {
			decompiler = new SelfDecompiler();
		}
		return decompiler;
	}

	public static void setDecompiler(SelfDecompiler compiler) {
		SelfDecompiler.decompiler = compiler;
	}
	
	/**
	 * Print the Self code for the state machine.
	 */
	public String decompileStateMachine(Vertex state, Network network) {
		long start = System.currentTimeMillis();
		StringWriter writer = new StringWriter();
		printStateMachine(state, writer, network, start, TIMEOUT);
		writer.flush();
		network.getBot().log(state, "Decompile time", Level.INFO, System.currentTimeMillis() - start);
		return writer.toString();
	}
	
	/**
	 * Print the Self code for the state machine.
	 */
	public void printStateMachine(Vertex state, Writer writer, Network network, long start, long timeout) {
		try {
			Set<Vertex> writtenElements = new HashSet<Vertex>();
			Set<Vertex> elements = new HashSet<Vertex>();
			writtenElements.add(state);
			printState(state, writer, "", elements, network, start, timeout);
			writer.write("\r\n");
		} catch (IOException error) {
			network.getBot().log(this, error);
			return;
		}
	}
	
	/**
	 * Print comments
	 */
	public boolean printComments(Vertex element, Writer writer, String indent, boolean newLine, Network network) throws IOException {
		List<Relationship> comments = element.orderedRelationships(Primitive.COMMENT);
		if (comments != null && !comments.isEmpty()) {
			if (newLine) {
				writer.write("\r\n");				
			}
			for (Relationship comment : comments) {
				writer.write(indent);
				writer.write((String)comment.getTarget().getData());
			}
			if (newLine) {
				writer.write(indent);				
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Print the state and any referenced states or variables that have not yet been printed.
	 */
	public void printState(Vertex state, Writer writer, String indent, Set<Vertex> elements, Network network, long start, long timeout) throws IOException {
		printComments(state, writer, indent, false, network);
		writer.write(indent);
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
		Collection<Relationship> equations = state.orderedRelationships(Primitive.DO);
		List<Vertex> newEquations = new ArrayList<Vertex>();
		List<Vertex> newVariables = new ArrayList<Vertex>();
		List<Vertex> newStates = new ArrayList<Vertex>();
		if (equations != null) {
			for (Relationship equation : equations) {
				printComments(equation.getTarget(), writer, indent, true, network);
				if (equation.getTarget().instanceOf(Primitive.CASE)) {
					printCase(equation.getTarget(), writer, childIndent, elements, newVariables, newEquations, newStates, network);
				} else if (equation.getTarget().instanceOf(Primitive.DO)) {
					writer.write(childIndent);
					printOperator(equation.getTarget().getRelationship(Primitive.DO), writer, indent, newEquations, newVariables, elements, network);
					writer.write(";\r\n");
				} else if (equation.getTarget().instanceOf(Primitive.GOTO)) {
					printGoto(equation.getTarget(), writer, childIndent, elements, network, start, timeout);
				} else if (equation.getTarget().instanceOf(Primitive.PUSH)) {
					printPush(equation.getTarget(), writer, childIndent, elements, network, start, timeout);
				} else if (equation.getTarget().instanceOf(Primitive.RETURN)) {
					printReturn(equation.getTarget(), writer, childIndent, elements, network, start, timeout);
				}
			}
		}
		for (Vertex variable : newVariables) {
			printVariable(variable, writer, childIndent, elements, network);
		}
		for (Vertex newEquation : newEquations) {
			printEquation(newEquation, writer, childIndent, elements, network);
		}
		newEquations = new ArrayList<Vertex>();
		newVariables = new ArrayList<Vertex>();
		Collection<Relationship> quotients = state.orderedRelationships(Primitive.QUOTIENT);
		if (quotients != null) {
			for (Relationship quotient : quotients) {
				writer.write(childIndent);
				writer.write("Answer:");
				writer.write(String.format("%.02f", quotient.getCorrectness()));
				writer.write(":");
				printElement(quotient.getTarget(), writer, indent, newEquations, newVariables, elements, network);
				if (quotient.hasMeta()) {
					writer.write(" {\r\n");
					Collection<Relationship> previousRelationships = quotient.getMeta().orderedRelationships(Primitive.PREVIOUS);
					if (previousRelationships != null) {
						for (Relationship previous : previousRelationships) {
							writer.write(childIndent);
							if (previous.getCorrectness() > 0) {
								writer.write("\tprevious is ");
							} else {
								writer.write("\tprevious is not ");								
							}
							printElement(previous.getTarget(), writer, indent + 1, newEquations, newVariables, elements, network);
							writer.write(";\r\n");							
						}
					}
					writer.write(childIndent);
					writer.write("}");
				}
				writer.write(";\r\n");
				for (Vertex variable : newVariables) {
					printVariable(variable, writer, childIndent, elements, network);
				}
				for (Vertex newEquation : newEquations) {
					printEquation(newEquation, writer, childIndent, elements, network);
				}
			}
		}
		Collection<Relationship> possibleQuotients = state.orderedRelationships(Primitive.POSSIBLE_QUOTIENT);
		if (possibleQuotients != null) {
			for (Relationship quotient : possibleQuotients) {
				writer.write(childIndent);
				writer.write("//Possible Quotient:");
				printElement(quotient.getTarget(), writer, indent, newEquations, newVariables, elements, network);
				writer.write(";\r\n");
			}
		}
		for (Vertex element : newStates) {
			if (element.instanceOf(Primitive.STATE)) {
				printState(element, writer, childIndent, elements, network, start, timeout);
			}
		}
		writer.write(indent);
		writer.write("}\r\n");
	}

	/**
	 * Print the vertex, either a state, variable, equation, or raw data.
	 */
	public void printElement(Vertex vertex, Writer writer, String indent, List<Vertex> equations, List<Vertex> variables, Set<Vertex> elements, Network network) throws IOException {
		if (vertex == null) {
			writer.write("#null");
			return;			
		}
		boolean printId = getPrintIds();
		if (vertex.instanceOf(Primitive.STATE)) {
			writer.write("State:");
		} else if (vertex.instanceOf(Primitive.VARIABLE)) {
			writer.write(":");
		} else if (vertex.instanceOf(Primitive.FORMULA)) {
			if (vertex.hasData()) {
				writer.write(String.valueOf(vertex.getData()));
			} else {
				printFormula(vertex, writer, indent, equations, variables, elements, network);
			}
			return;
		} else if (vertex.instanceOf(Primitive.PATTERN)) {
			writer.write(String.valueOf(vertex.getData()));
			return;
		} else if (vertex.instanceOf(Primitive.EQUATION)) {
			// If unnamed, then just inline.
			if (vertex.getName() == null) {
				if (!elements.contains(vertex)) {
					String newIndent = indent + "\t";
					printOperator(vertex, writer, newIndent, equations, variables, elements, network);
					return;
				} else {
					// No name, but was used twice, change it to a named equation and reference it.
					vertex.setName("dangling");
					elements.add(vertex);					
				}
			}
			writer.write("Function:");
			if (!elements.contains(vertex)) {
				equations.add(vertex);
				elements.add(vertex);
			}
		} else if (vertex.instanceOf(Primitive.PARAGRAPH)) {
			writer.write("\"");
			String text = vertex.printString();
			if (text.indexOf('"') != -1) {
				text = text.replace("\"", "\"\"");
			}
			writer.write(text);
			writer.write("\"");
			return;
		} else if (vertex.hasData()) {
			Object data = vertex.getData();
			if (data instanceof Primitive) {
				writer.write("#");
				writer.write(((Primitive)vertex.getData()).getIdentity());
			} else if (data instanceof String) {
				writer.write("\"");
				String text = (String)vertex.getData();
				if (text.indexOf('"') != -1) {
					text = text.replace("\"", "\"\"");
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
		} else {
			writer.write("Vertex:");
			printId = true;
		}
		if (printId || (vertex.getName() == null)) {
			writer.write(vertex.getId().toString());
		}
		if (vertex.getName() != null) {
			String name = Utils.compress(vertex.getName(), 100);
			if (!name.isEmpty()) {
				if (printId) {
					writer.write(":");
				}
				writer.write(name);
			}
		}
	}

	public void printFormula(Vertex formula, Writer writer, String indent, List<Vertex> equations, List<Vertex> variables, Set<Vertex> elements, Network network) throws IOException {
		writer.write("Formula:");
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
				writer.write(text);
				if (text.equals("\"")) {
					writer.write("\"");					
				}
			} else {
				writer.write("{");
				printElement(word, writer, indent, equations, variables, elements, network);
				writer.write("}");				
			}
		}
		writer.write("\"");
	}

	/**
	 * Print the formula to create a unique instance of it.
	 */
	public Vertex createUniqueFormula(Vertex formula, Network network) {
		try {
			StringWriter writer = new StringWriter();
			SelfDecompiler.getDecompiler().printFormula(formula, writer, "", new ArrayList<Vertex>(), new ArrayList<Vertex>(), new HashSet<Vertex>(), network);
			String source = writer.toString();
			if (source.length() > AbstractNetwork.MAX_TEXT) {
				return formula;
			}
			// Maintain identity on formulas through printing them.
			Vertex existingFormula = network.createVertex(source);
			if (!existingFormula.instanceOf(Primitive.FORMULA)) {
				for (Iterator<Relationship> iterator = formula.orderedAllRelationships(); iterator.hasNext(); ) {
					Relationship relationship = iterator.next();
					existingFormula.addRelationship(relationship.getType(), relationship.getTarget(), relationship.getIndex());
				}
			}
			return existingFormula;
		} catch (IOException ignore) {
			throw new BotException(ignore);
		}
	}
	
	/**
	 * Print the variable and any variables it references that have not been printed.
	 */
	public void printVariable(Vertex variable, Writer writer, String indent, Set<Vertex> elements, Network network) throws IOException {
		boolean hasComments = printComments(variable, writer, indent, false, network);
		Iterator<Relationship> iterator = variable.allRelationships();
		if (!hasComments && variable.totalRelationships() <= 1) {
			return; // Nothing to print.
		}
		List<Vertex> localElements = new ArrayList<Vertex>();
		writer.write(indent);
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
			if (type.instanceOf(Primitive.VARIABLE) && !elements.contains(type)) {
				localElements.add(type);
				elements.add(type);
			}
			if (target.instanceOf(Primitive.VARIABLE) && !elements.contains(target)) {
				localElements.add(target);
				elements.add(target);
			}
			boolean equality = type.is(Primitive.EQUALS);
			if (relationship.isInverse()) {
				writer.write("\texclude ");
			} else if (equality) {
				writer.write("\tinclude ");
			} else {
				writer.write("\tset ");
			}
			if (equality) {
				printElement(target, writer, indent, null, null, elements, network);
			} else {
				printElement(type, writer, indent, null, null, elements, network);
				if (relationship.isInverse()) {
					writer.write(" from ");
				} else {
					writer.write(" to ");				
				}
				printElement(target, writer, indent, null, null, elements, network);
			}
			writer.write(";\r\n");
			writer.write(indent);
		}
		writer.write("}\r\n");
		for (Vertex element : localElements) {
			printVariable(element, writer, indent, elements, network);
		}
	}

	/**
	 * Print the operation arguments.
	 */
	public void printArguments(Vertex equation, Primitive type, String[] tokens, boolean reverse, boolean newLine, boolean unravel, Writer writer, String indent, List<Vertex> variables, List<Vertex> equations, Set<Vertex> elements, boolean space, Network network) throws IOException {
		List<Relationship> arguments = equation.orderedRelationships(type);
		if (arguments != null) {
			boolean needsBrackets = !unravel;			
			if ((arguments.size() == 1) || (tokens != null)) {
				needsBrackets = false;
			}
			if (arguments.size() == 1) {
				newLine = false;
			} else if (arguments.size() > 3) {
				newLine = true;
			}
			if (!unravel && space) {
				writer.write(" ");
			}
			if (needsBrackets) {
				writer.write("(");
			}
			int size = arguments.size();
			for (int index = 0; index < size; index++) {
				if (newLine && (!unravel || (index > 0))) {
					writer.write("\r\n");
					writer.write(indent);
					writer.write("\t");
					writer.write("\t");
				}
				Vertex argument = null;
				if (reverse) {
					argument = arguments.get(size - index - 1).getTarget();
				} else {
					argument = arguments.get(index).getTarget();
				}
				if (argument.instanceOf(Primitive.VARIABLE) && !elements.contains(argument)) {
					variables.add(argument);
					elements.add(argument);
				}
				boolean isEquation = argument.instanceOf(Primitive.EQUATION);
				if (!unravel && !needsBrackets && isEquation) {
					writer.write("(");					
				}
				printElement(argument, writer, indent, equations, variables, elements, network);
				if (!unravel && !needsBrackets && isEquation) {
					writer.write(")");
				}
				if (index < (size - 1)) {
					if (tokens != null) {
						writer.write(" ");
						writer.write(tokens[index]);
						writer.write(" ");
					} else {
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
			if (!unravel && newLine) {
				writer.write("\r\n");
				writer.write(indent);
				writer.write("\t");
			}
			if (needsBrackets) {
				writer.write(")");
			}
		}
	}
	
	/**
	 * Print the equation and any equations it references that have not been printed.
	 */
	public void printEquation(Vertex equation, Writer writer, String indent, Set<Vertex> elements, Network network) throws IOException {
		printComments(equation, writer, indent, false, network);
		List<Vertex> equations = new ArrayList<Vertex>();
		List<Vertex> variables = new ArrayList<Vertex>();
		writer.write(indent);
		printElement(equation, writer, indent, equations, variables, elements, network);
		writer.write(" {\r\n");
		writer.write(indent);
		Vertex operator = equation.getRelationship(Primitive.OPERATOR);
		if (operator == null) {
			writer.write("}\r\n");
			return;			
		}
		if (operator.is(Primitive.DO)) {
			// Unravel DO.
			writer.write("\t");
			printArguments(equation, Primitive.ARGUMENT, null, false, true, true, writer, indent.substring(0, indent.length() - 1), variables, equations, elements, true, network);
			writer.write(";\r\n");
		} else {
			writer.write("\t");
			printOperator(equation, writer, indent, equations, variables, elements, network);
			writer.write(";\r\n");
		}
		writer.write(indent);
		writer.write("}\r\n");
		for (Vertex element : variables) {
			printVariable(element, writer, indent, elements, network);
		}
		for (Vertex element : equations) {
			printEquation(element, writer, indent, elements, network);
		}
	}
	
	/**
	 * Print the equation and any equations it references that have not been printed.
	 */
	public void printOperator(Vertex equation, Writer writer, String indent, List<Vertex> equations, List<Vertex> variables, Set<Vertex> elements, Network network) throws IOException {
		printComments(equation, writer, indent + "\t", true, network);
		Vertex operator = equation.getRelationship(Primitive.OPERATOR);
		if (operator == null) {
			return;			
		}
		if (operator.is(Primitive.RELATION)) {
			writer.write("is");
		} else if (operator.is(Primitive.WEAKASSOCIATE)) {
			writer.write("weak associate");
		} else {
			writer.write(((Primitive)operator.getData()).getIdentity());
		}
		if (equation.hasRelationship(Primitive.NOT, Primitive.NOT)) {
			writer.write(" not");
		}
		// Print arguments.
		if (operator.is(Primitive.ASSIGN)) {
			String[] tokens = {"to"};
			printArguments(equation, Primitive.ARGUMENT, tokens, false, false, false, writer, indent, variables, equations, elements, true, network);
		} else if (operator.is(Primitive.DEFINE)) {
			String[] tokens = {"as"};
			printArguments(equation, Primitive.ARGUMENT, tokens, false, false, false, writer, indent, variables, equations, elements, true, network);
		} else if (operator.is(Primitive.GET)) {
			String[] tokens = {"from", "associated to", "by"};
			printArguments(equation, Primitive.ARGUMENT, tokens, false, false, false, writer, indent, variables, equations, elements, true, network);
			// Print AT.
			Collection<Relationship> index = equation.getRelationships(Primitive.INDEX);
			if (index != null) {
				writer.write(" at");
				printArguments(equation, Primitive.INDEX, null, false, false, false, writer, indent, variables, equations, elements, true, network);
			}
			Collection<Relationship> lastindex = equation.getRelationships(Primitive.LASTINDEX);
			if (lastindex != null) {
				writer.write(" at last");
				printArguments(equation, Primitive.LASTINDEX, null, false, false, false, writer, indent, variables, equations, elements, true, network);
			}
		} else if (operator.is(Primitive.INPUT)) {
			String[] tokens = {"for"};
			if (equation.getRelationships(Primitive.ARGUMENT).size() == 3) {
				tokens = new String[]{"part", "for"};
			}
			printArguments(equation, Primitive.ARGUMENT, tokens, false, false, false, writer, indent, variables, equations, elements, true, network);
		} else if (operator.is(Primitive.ALL)) {
			String[] tokens = {"from", "associated to", "by"};
			printArguments(equation, Primitive.ARGUMENT, tokens, false, false, false, writer, indent, variables, equations, elements, true, network);
		} else if (operator.is(Primitive.COUNT)) {
			String[] tokens = {"of"};
			printArguments(equation, Primitive.ARGUMENT, tokens, false, false, false, writer, indent, variables, equations, elements, true, network);
		} else if (operator.is(Primitive.APPEND)) {
			String[] tokens = {"to", "of", "with meta", "as"};
			printArguments(equation, Primitive.ARGUMENT, tokens, false, false, false, writer, indent, variables, equations, elements, true, network);
		} else if (operator.is(Primitive.ASSOCIATE) || operator.is(Primitive.DISSOCIATE) || operator.is(Primitive.WEAKASSOCIATE)) {
			String[] tokens = {"to", "by", "with meta", "as"};
			printArguments(equation, Primitive.ARGUMENT, tokens, false, false, false, writer, indent, variables, equations, elements, true, network);
		} else if (operator.is(Primitive.SET)) {
			String[] tokens = {"to", "on"};
			printArguments(equation, Primitive.ARGUMENT, tokens, false, false, false, writer, indent, variables, equations, elements, true, network);
		} else if (operator.is(Primitive.RELATION)) {
			String[] tokens = {"related to", "by"};
			printArguments(equation, Primitive.ARGUMENT, tokens, false, false, false, writer, indent, variables, equations, elements, true, network);
		} else if (operator.is(Primitive.RELATED)) {
			writer.write(" to ");
			String[] tokens = {"by"};
			printArguments(equation, Primitive.ARGUMENT, tokens, false, false, false, writer, indent, variables, equations, elements, true, network);
		} else if (operator.is(Primitive.WHILE)) {
			// Print arguments.
			printArguments(equation, Primitive.ARGUMENT, null, false, (operator.is(Primitive.DO)), false, writer, indent, variables, equations, elements, true, network);
			// Print do.
			Collection<Relationship> dos = equation.orderedRelationships(Primitive.DO);
			if (dos != null) {
				String newIndent = indent + "\t";
				writer.write("\r\n");
				writer.write(newIndent);
				writer.write("\tdo");
				printArguments(equation, Primitive.DO, null, false, true, false, writer, newIndent, variables, equations, elements, true, network);
			}
		} else if (operator.is(Primitive.FOR)) {
			writer.write(" each ");
			List<Relationship> arguments = equation.orderedRelationships(Primitive.ARGUMENT);
			printElement(arguments.get(0).getTarget(), writer, indent, equations, variables, elements, network);
			writer.write(" of ");
			printElement(arguments.get(1).getTarget(), writer, indent, equations, variables, elements, network);
			writer.write(" as ");
			printElement(arguments.get(2).getTarget(), writer, indent, equations, variables, elements, network);
			int index = 3;
			if (arguments.size() > index) {
				writer.write(" and each ");
				printElement(arguments.get(index++).getTarget(), writer, indent, equations, variables, elements, network);
				writer.write(" of ");
				printElement(arguments.get(index++).getTarget(), writer, indent, equations, variables, elements, network);
				writer.write(" as ");
				printElement(arguments.get(index++).getTarget(), writer, indent, equations, variables, elements, network);				
			}
			// Print do.
			Collection<Relationship> dos = equation.orderedRelationships(Primitive.DO);
			if (dos != null) {
				String newIndent = indent + "\t";
				writer.write("\r\n");
				writer.write(newIndent);
				writer.write("\tdo");
				printArguments(equation, Primitive.DO, null, false, true, false, writer, newIndent, variables, equations, elements, true, network);
			}
		} else if (operator.is(Primitive.CALL)) {
			List<Relationship> arguments = equation.orderedRelationships(Primitive.ARGUMENT);
			writer.write(" ");
			printElement(arguments.get(0).getTarget(), writer, indent, equations, variables, elements, network);
			writer.write(" on ");
			printElement(arguments.get(1).getTarget(), writer, indent, equations, variables, elements, network);
			if (arguments.size() > 2) {
				writer.write(" with (");
				for (int index = 2; index < arguments.size(); index++) {
					printElement(arguments.get(index).getTarget(), writer, indent, equations, variables, elements, network);
					if (index == (arguments.size() - 1)) {
						writer.write(")");
					} else {
						writer.write(", ");
					}
				}
			}
		} else if (operator.is(Primitive.LEARN)) {
			List<Relationship> arguments = equation.orderedRelationships(Primitive.ARGUMENT);
			writer.write(" ");
			printElement(arguments.get(0).getTarget(), writer, indent, equations, variables, elements, network);
			// Print that.
			Collection<Relationship> that = equation.orderedRelationships(Primitive.THAT);
			if (that != null) {
				String newIndent = indent + "\t";
				writer.write("\r\n");
				writer.write(newIndent);
				writer.write("\tthat");
				printArguments(equation, Primitive.THAT, null, false, false, false, writer, newIndent, variables, equations, elements, true, network);
			}
			// Print topic.
			Collection<Relationship> topic = equation.orderedRelationships(Primitive.TOPIC);
			if (topic != null) {
				String newIndent = indent + "\t";
				writer.write("\r\n");
				writer.write(newIndent);
				writer.write("\ttopic");
				printArguments(equation, Primitive.TOPIC, null, false, false, false, writer, newIndent, variables, equations, elements, true, network);
			}
			String newIndent = indent + "\t";
			writer.write("\r\n");
			writer.write(newIndent);
			writer.write("\ttemplate ");
			printElement(arguments.get(1).getTarget(), writer, newIndent, equations, variables, elements, network);
		} else if (operator.is(Primitive.IF)) {
			// Print arguments.
			printArguments(equation, Primitive.ARGUMENT, null, false, (operator.is(Primitive.DO)), false, writer, indent, variables, equations, elements, true, network);
			// Print ands/ors brackets.
			Collection<Relationship> conditions = equation.orderedRelationships(Primitive.CONDITION);
			if (conditions != null) {
				for (Relationship condition : conditions) {
					writer.write(" ");
					printOperator(condition.getTarget(), writer, indent, equations, variables, elements, network);
				}
			}
			// Print then.
			Collection<Relationship> thens = equation.orderedRelationships(Primitive.THEN);
			if (thens != null) {
				String newIndent = indent + "\t";
				writer.write("\r\n");
				writer.write(newIndent);
				writer.write("\tthen");
				printArguments(equation, Primitive.THEN, null, false, false, false, writer, newIndent, variables, equations, elements, true, network);
			}
			// Print else.
			Collection<Relationship> elses = equation.orderedRelationships(Primitive.ELSE);
			if (elses != null) {
				String newIndent = indent + "\t";
				writer.write("\r\n");
				writer.write(newIndent);
				writer.write("\telse");
				printArguments(equation, Primitive.ELSE, null, false, false, false, writer, newIndent, variables, equations, elements, true, network);
			}
		} else if (operator.is(Primitive.REQUEST) || operator.is(Primitive.SRAIX)) {
			// Print arguments.
			printArguments(equation, Primitive.ARGUMENT, null, false, (operator.is(Primitive.DO)), false, writer, indent, variables, equations, elements, true, network);
			// Print parameters.
			Collection<Relationship> param = equation.orderedRelationships(Primitive.BOT);
			if (param != null) {
				writer.write(" bot");
				printArguments(equation, Primitive.BOT, null, false, false, false, writer, indent, variables, equations, elements, true, network);
			}
			param = equation.orderedRelationships(Primitive.BOTID);
			if (param != null) {
				writer.write(" botid");
				printArguments(equation, Primitive.BOTID, null, false, false, false, writer, indent, variables, equations, elements, true, network);
			}
			param = equation.orderedRelationships(Primitive.SERVICE);
			if (param != null) {
				writer.write(" service");
				printArguments(equation, Primitive.SERVICE, null, false, false, false, writer, indent, variables, equations, elements, true, network);
			}
			param = equation.orderedRelationships(Primitive.SERVER);
			if (param != null) {
				writer.write(" server");
				printArguments(equation, Primitive.SERVER, null, false, false, false, writer, indent, variables, equations, elements, true, network);
			}
			param = equation.orderedRelationships(Primitive.APIKEY);
			if (param != null) {
				writer.write(" apikey");
				printArguments(equation, Primitive.APIKEY, null, false, false, false, writer, indent, variables, equations, elements, true, network);
			}
			param = equation.orderedRelationships(Primitive.LIMIT);
			if (param != null) {
				writer.write(" limit");
				printArguments(equation, Primitive.LIMIT, null, false, false, false, writer, indent, variables, equations, elements, true, network);
			}
			param = equation.orderedRelationships(Primitive.HINT);
			if (param != null) {
				writer.write(" hint");
				printArguments(equation, Primitive.HINT, null, false, false, false, writer, indent, variables, equations, elements, true, network);
			}
			param = equation.orderedRelationships(Primitive.DEFAULT);
			if (param != null) {
				writer.write(" default");
				printArguments(equation, Primitive.DEFAULT, null, false, false, false, writer, indent, variables, equations, elements, true, network);
			}
		} else {
			Collection<Relationship> conditions = equation.orderedRelationships(Primitive.CONDITION);
			if (conditions != null) {
				writer.write(" (");
			}
			// Print arguments.
			printArguments(equation, Primitive.ARGUMENT, null, false, (operator.is(Primitive.DO)), false, writer, indent, variables, equations, elements, conditions == null, network);
			// Print ands/ors brackets.
			if (conditions != null) {
				for (Relationship condition : conditions) {
					writer.write(" ");
					printOperator(condition.getTarget(), writer, indent, equations, variables, elements, network);
				}
				writer.write(")");
			}
			if (operator.is(Primitive.FORMAT)) {
				// Print AS.
				Collection<Relationship> as = equation.orderedRelationships(Primitive.AS);
				if (as != null) {
					writer.write(" as ");
					printArguments(equation, Primitive.AS, null, false, false, false, writer, indent, variables, equations, elements, true, network);
				}			
			}
		}
	}
	
	/**
	 * Print the IF condition and any variables and states that it references.
	 */
	public void printCase(Vertex equation, Writer writer, String indent, Set<Vertex> elements,
					List<Vertex> newVariables, List<Vertex> newEquations, List<Vertex> newStates, Network network)
					throws IOException {
		Vertex variable = equation.getRelationship(Primitive.CASE);
		Vertex pattern = equation.getRelationship(Primitive.PATTERN);
		Vertex template = equation.getRelationship(Primitive.TEMPLATE);
		Vertex that = equation.getRelationship(Primitive.THAT);
		Vertex topic = equation.getRelationship(Primitive.TOPIC);
		Vertex as = equation.getRelationship(Primitive.AS);
		List<Relationship> states = equation.orderedRelationships(Primitive.GOTO);
		List<Relationship> fors = equation.orderedRelationships(Primitive.FOR);
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
		if (variable.instanceOf(Primitive.EQUATION)) {
			writer.write("(");			
		}
		printElement(variable, writer, indent, newEquations, newVariables, elements, network);
		if (variable.instanceOf(Primitive.EQUATION)) {
			writer.write(")");			
		}
		if (as != null) {
			writer.write(" as ");
			printElement(as, writer, indent, newEquations, newVariables, elements, network);
		}
		if (topic != null) {
			writer.write("\r\n");
			writer.write(indent);
			writer.write("\t");
			writer.write("topic ");
			printElement(topic, writer, indent, newEquations, newVariables, elements, network);
		}
		if (that != null) {
			writer.write("\r\n");
			writer.write(indent);
			writer.write("\t");
			writer.write("that ");
			printElement(that, writer, indent, newEquations, newVariables, elements, network);
		}
		if (template != null) {
			writer.write("\r\n");
			writer.write(indent);
			writer.write("\t");
			writer.write("template ");
			if (template.instanceOf(Primitive.EQUATION)) {
				writer.write("(");			
			}
			printElement(template, writer, indent, newEquations, newVariables, elements, network);
			if (template.instanceOf(Primitive.EQUATION)) {
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
	public void printGoto(Vertex equation, Writer writer, String indent, Set<Vertex> elements, Network network, long start, long timeout) throws IOException {
		Vertex state = equation.getRelationship(Primitive.GOTO);
		List<Vertex> localElements = new ArrayList<Vertex>();
		writer.write(indent);
		writer.write("goto ");
		if (equation.hasRelationship(Primitive.FINALLY)) {
			writer.write("finally ");
		}
		if (!elements.contains(state)) {
			localElements.add(state);
			elements.add(state);
		}
		printElement(state, writer, indent, null, null, elements, network);
		Collection<Relationship> arguments = equation.getRelationships(Primitive.ARGUMENT);
		if (arguments != null) {
			writer.write(" with (");
			for (Iterator<Relationship> iterator = arguments.iterator(); iterator.hasNext(); ) {
				Relationship argument = iterator.next();
				printElement(argument.getTarget(), writer, indent, null, null, elements, network);
				if (iterator.hasNext()) {
					writer.write(", ");					
				}
			}
			writer.write(")");
		}
		writer.write(";\r\n\r\n");
		for (Vertex element : localElements) {
			if (element.instanceOf(Primitive.VARIABLE)) {
				printVariable(element, writer, indent, elements, network);
			} else if (element.instanceOf(Primitive.STATE)) {
				printState(element, writer, indent, elements, network, start, timeout);
			}
		}
	}
	
	/**
	 * Print the PUSH condition and any variables and states that it references.
	 */
	public void printPush(Vertex equation, Writer writer, String indent, Set<Vertex> elements, Network network, long start, long timeout) throws IOException {
		Vertex state = equation.getRelationship(Primitive.ARGUMENT);
		List<Vertex> localElements = new ArrayList<Vertex>();
		writer.write(indent);
		writer.write("push ");
		if (!elements.contains(state)) {
			localElements.add(state);
			elements.add(state);
		}
		printElement(state, writer, indent, null, null, elements, network);
		writer.write(";\r\n\r\n");
		for (Vertex element : localElements) {
			if (element.instanceOf(Primitive.VARIABLE)) {
				printVariable(element, writer, indent, elements, network);
			} else if (element.instanceOf(Primitive.STATE)) {
				printState(element, writer, indent, elements, network, start, timeout);
			}
		}
	}
	
	/**
	 * Print the RETURN condition and any variables it references.
	 */
	public void printReturn(Vertex equation, Writer writer, String indent, Set<Vertex> elements, Network network, long start, long timeout) throws IOException {
		Vertex result = equation.getRelationship(Primitive.RETURN);
		writer.write(indent);
		writer.write("return");
		if (result != null) {
			writer.write(" ");
			printElement(result, writer, indent, null, null, elements, network);
		}
		Collection<Relationship> arguments = equation.getRelationships(Primitive.ARGUMENT);
		if (arguments != null) {
			writer.write(" with (");
			for (Iterator<Relationship> iterator = arguments.iterator(); iterator.hasNext(); ) {
				Relationship argument = iterator.next();
				printElement(argument.getTarget(), writer, indent, null, null, elements, network);
				if (iterator.hasNext()) {
					writer.write(", ");					
				}
			}
			writer.write(")");
		}
		writer.write(";\r\n\r\n");
	}

	public boolean getPrintIds() {
		return printIds;
	}

	public void setPrintIds(boolean printIds) {
		this.printIds = printIds;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}

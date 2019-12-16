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
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.botlibre.aiml.AIMLParser;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.BinaryData;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.http.Http;
import org.botlibre.sense.service.RemoteService;
import org.botlibre.thought.language.Language;
import org.botlibre.util.Utils;

/**
 * Self language runtime interpreter.
 * This class evaluates a compiled Self expression or function.
 */
public class SelfInterpreter {
	public static long TIMEOUT = 10000;
	public static int MAX_STACK = 500;
	protected static SelfInterpreter interpreter;

	public static SelfInterpreter getInterpreter() {
		if (interpreter == null) {
			interpreter = new SelfInterpreter();
		}
		return interpreter;
	}

	public static void setInterpreter(SelfInterpreter interpreter) {
		SelfInterpreter.interpreter = interpreter;
	}
	
	/**
	 * Evaluate the function and return the result.
	 */
	public Vertex evaluateFunction(Vertex function, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		try {
			// Check for byte-code.
			if (function.getData() instanceof BinaryData) {
				function = SelfDecompiler.getDecompiler().parseFunctionByteCode(function, (BinaryData)function.getData(), function.getNetwork());
				return evaluateFunction(function, variables, network, startTime, maxTime, stack);
			}
		} catch (IOException exception) {
			network.getBot().log(this, exception);
			throw new SelfExecutionException(function, exception);
		}
		// function { x; y; z; }
		// Apply each expression in the function.
		Vertex result = null;
		Vertex returnPrimitive = network.createVertex(Primitive.RETURN);
		List<Relationship> operations = function.orderedRelationships(Primitive.DO);
		if (operations != null) {
			for (Relationship expression : operations) {
				result = evaluateExpression(expression.getTarget(), variables, network, startTime, maxTime, stack);
				if (variables.containsKey(returnPrimitive)) {
					variables.remove(returnPrimitive);
					return result;
				}
			}
		}
		if (result == null) {
			result = function;
		}
		return result;
	}
	
	/**
	 * Evaluate the expression and return the result.
	 */
	@SuppressWarnings("unchecked")
	public Vertex evaluateExpression(Vertex expression, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (stack > MAX_STACK) {
			throw new SelfExecutionException(expression, "Stack overflow: " + stack);			
		}
		stack++;
		if ((System.currentTimeMillis() - startTime) > maxTime) {
			throw new SelfExecutionException(expression, "Max time exceeded: " + maxTime);			
		}
		Vertex result = null;
		boolean isDebug = network.getBot().isDebugFiner();
		if (expression.isVariable()) {
			result = variables.get(expression);
			if (result == null) {
				if (expression.hasName()) {
					result = variables.get(expression.getName());
				}
				if (result == null) {
					result = network.createVertex(Primitive.NULL);
				}
			}
		} else if (expression.instanceOf(Primitive.EXPRESSION)) {
			try {
				// Check for byte-code.
				if (expression.getData() instanceof BinaryData) {
					Vertex equation = SelfDecompiler.getDecompiler().parseExpressionByteCode(expression, (BinaryData)expression.getData(), expression.getNetwork());
					if (!(equation.getData() instanceof BinaryData)) {
						return evaluateExpression(equation, variables, network, startTime, maxTime, stack);
					}
				}
				Vertex operator = expression.getRelationship(Primitive.OPERATOR);
				if (operator == null) {
					return network.createVertex(Primitive.NULL);
				}
				List<Relationship> arguments = expression.orderedRelationships(Primitive.ARGUMENT);
				if (arguments == null) {
					arguments = Collections.EMPTY_LIST;
				}
				if (isDebug) {
					Vertex source = expression.getRelationship(Primitive.SOURCE);
					String sourceCode = "";
					if (source != null) {
						sourceCode = String.valueOf(source.getData()).trim();
					} else if (operator.isPrimitive()) {
						if (operator.is(Primitive.CALL)) {
							sourceCode = ((Primitive)operator.getData()).getIdentity().toUpperCase() + "("
									+ String.valueOf(expression.getRelationship(Primitive.FUNCTION))
									+ expression.orderedRelations(Primitive.ARGUMENT) + ")";
						} else {
							sourceCode = ((Primitive)operator.getData()).getIdentity().toUpperCase() + "(" + expression.orderedRelations(Primitive.ARGUMENT) + ")";
						}
					}
					Vertex number = expression.getRelationship(Primitive.LINE_NUMBER);
					if (number != null) {
						sourceCode = String.valueOf(number.getData()) + ":" + sourceCode;
					}
					network.getBot().log(this, sourceCode, Level.FINER);
				}
				// NOT :0
				// Check if negated.
				if (operator.is(Primitive.NOT)) {
					result = evaluateNOT(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.FOR)) {
					result = evaluateFOR(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.WHILE)) {
					result = evaluateWHILE(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.ASSIGN)) {
					// x = y
					// Assign a variable a new value.
					Vertex variable = arguments.get(0).getTarget();
					Vertex value = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
					if (value != null) {
						variables.put(variable, value);
					}
					if (isDebug) {
						network.getBot().log(this, "" + variable + " = " + value, Level.FINER);
					}
					result = value;
				} else if (operator.is(Primitive.INCREMENT)) {
					// x++
					// Increment the value.
					Vertex variable = arguments.get(0).getTarget();
					Vertex value = evaluateExpression(variable, variables, network, startTime, maxTime, stack);
					if (value != null && value.getData() instanceof Number) {
						value = network.createVertex(((Number)value.getData()).intValue() + 1);
						variables.put(variable, value);
					}
					if (isDebug) {
						network.getBot().log(this, "" + variable + " = " + value, Level.FINER);
					}
					result = value;
				} else if (operator.is(Primitive.DECREMENT)) {
					// x--
					// Decrement the value.
					Vertex variable = arguments.get(0).getTarget();
					Vertex value = evaluateExpression(variable, variables, network, startTime, maxTime, stack);
					if (value != null && value.getData() instanceof Number) {
						value = network.createVertex(((Number)value.getData()).intValue() - 1);
						variables.put(variable, value);
					}
					if (isDebug) {
						network.getBot().log(this, "" + variable + " = " + value, Level.FINER);
					}
					result = value;
				} else if (operator.is(Primitive.IF)) {
					result = evaluateIF(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.EQUALS)) {
					result = evaluateEQUALS(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.NOTEQUALS)) {
					result = evaluateNOTEQUALS(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.OR)) {
					result = evaluateOR(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.AND)) {
					result = evaluateAND(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.DO)) {
					result = evaluateDO(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.THINK)) {
					result = evaluateTHINK(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.GET)) {
					result = evaluateGET(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.SET)) {
					result = evaluateSET(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.ADD)) {
					result = evaluateADD(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.REMOVE)) {
					result = evaluateREMOVE(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.INSTANCEOF)) {
					result = evaluateINSTANCEOF(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.NEW)) {
					result = evaluateNEW(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.CALL)) {
					result = evaluateCALL(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.RETURN)) {
					// RETURN :0
					if (arguments == null || arguments.isEmpty()) {
						result = network.createVertex(Primitive.NULL);
					} else {
						result = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
					}
					variables.put(network.createVertex(Primitive.RETURN), result);
				} else if (operator.is(Primitive.BREAK)) {
					return operator;
				} else if (operator.is(Primitive.CONTINUE)) {
					return operator;
				} else if (operator.is(Primitive.RANDOM)) {
					result = evaluateRANDOM(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.DEBUG)) {
					result = evaluateDEBUG(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.GREATERTHAN)) {
					result = evaluateGREATERTHAN(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.GREATERTHANEQUAL)) {
					result = evaluateGREATERTHANEQUAL(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.LESSTHAN)) {
					result = evaluateLESSTHAN(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.LESSTHANEQUAL)) {
					result = evaluateLESSTHANEQUAL(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.PLUS)) {
					result = evaluatePLUS(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.MINUS)) {
					result = evaluateMINUS(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.MULTIPLY)) {
					result = evaluateMULTIPLY(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.DIVIDE)) {
					result = evaluateDIVIDE(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.SYMBOL)) {
					result = evaluateSYMBOL(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.EVALCOPY)) {
					result = evaluateEVALCOPY(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.LEARN)) {
					result = evaluateLEARN(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.SRAI) || operator.is(Primitive.REDIRECT)) {
					result = evaluateSRAI(expression, arguments, variables, network, startTime, maxTime, stack);
				} else if (operator.is(Primitive.SRAIX) || operator.is(Primitive.REQUEST)) {
					result = evaluateSRAIX(expression, arguments, variables, network, startTime, maxTime, stack);
				}
			} catch (SelfExecutionException exception) {
				throw exception;
			} catch (Exception exception) {
				network.getBot().log(this, exception);
				throw new SelfExecutionException(expression, exception);
			}
		} else if (expression.instanceOf(Primitive.FUNCTION)) {
			result = evaluateFunction(expression, variables, network, startTime, maxTime, stack);
		} else if (expression.instanceOf(Primitive.EQUATION)) {
			result = expression.applyQuotient(variables, network);
		} else {
			result = expression;
		}
		if (result == null) {
			result = network.createVertex(Primitive.NULL);
		}
		if (result.getNetwork() != network) {
			result = network.createVertex(result);
		}
		// Check for formula and transpose
		if (result.instanceOf(Primitive.FORMULA)) {
			Language language = network.getBot().mind().getThought(Language.class);
			Vertex newResult = language.evaluateFormula(result, variables, network);
			if (newResult == null) {
				language.log("Formula cannot be evaluated", Level.FINE, result);
				result = network.createVertex(Primitive.NULL);
			} else {
				result = language.getWord(newResult, network);
			}
		}
		if (isDebug && !result.equals(expression)) {
			network.getBot().log(this, "result: ", Level.FINER, result, expression);
		}
		return result;
	}
	
	public boolean checkArguments(Vertex expression, List<Relationship> arguments, int expected, Network network) {
		if (arguments.size() != expected) {
			network.getBot().log(this, "Invalid number of arguments (operation, arguments, expected)", Level.WARNING, expression, arguments.size(), expected);
			return false;
		}
		return true;
	}
	
	public boolean checkMinArguments(Vertex expression, List<Relationship> arguments, int expected, Network network) {
		if (arguments.size() < expected) {
			network.getBot().log(this, "Invalid number of arguments (operation, arguments, expected)", Level.WARNING, expression, arguments.size(), expected);
			return false;
		}
		return true;
	}

	/**
	 * Evaluate the IF operation.
	 * if (x == y || z != q) { x; y; } else { z; q; }
	 */
	public Vertex evaluateIF(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 1, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex value = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex result = network.createVertex(Primitive.NULL);
		if (value.is(Primitive.TRUE)) {
			List<Vertex> thens = expression.orderedRelations(Primitive.THEN);
			if (thens != null) {
				Vertex returnPrimitive = network.createVertex(Primitive.RETURN);
				for (Vertex then :thens) {
					result = evaluateExpression(then, variables, network, startTime, maxTime, stack);
					if (variables.containsKey(returnPrimitive)) {
						return result;
					}
				}
			}
		} else {
			List<Vertex> elseifs = expression.orderedRelations(Primitive.ELSEIF);
			boolean match = false;
			if (elseifs != null) {
				for (Vertex elseif : elseifs) {
					Vertex returnPrimitive = network.createVertex(Primitive.RETURN);
					result = evaluateExpression(elseif, variables, network, startTime, maxTime, stack);
					if (variables.containsKey(returnPrimitive)) {
						return result;
					}
					if (!result.is(Primitive.NULL)) {
						match = true;
						break;
					}
				}
			}
			if (!match) {
				List<Vertex> elses = expression.orderedRelations(Primitive.ELSE);
				if (elses != null) {
					Vertex returnPrimitive = network.createVertex(Primitive.RETURN);
					for (Vertex elseExpressions :elses) {
						result = evaluateExpression(elseExpressions, variables, network, startTime, maxTime, stack);
						if (variables.containsKey(returnPrimitive)) {
							return result;
						}
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Evaluate the WHILE operation.
	 * while (x < 100) { x = x + 1; }
	 */
	public Vertex evaluateWHILE(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 1, network)) {
			return network.createVertex(Primitive.NULL);
		}
		// Process a while loop, repeat the operation until true or max depth
		int depth = 0;
		boolean condition = true;
		List<Relationship> doEquations = expression.orderedRelationships(Primitive.DO);
		Vertex result = network.createVertex(Primitive.NULL);
		while (condition && depth < Language.MAX_LOOP)  {
			Vertex first = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
			if (arguments.size() == 1) {
				condition = first.is(Primitive.TRUE);
			}
			if (condition) {
				for (Relationship doEquation: doEquations) {
					result = evaluateExpression(doEquation.getTarget(), variables, network, startTime, maxTime, stack);
					if (variables.containsKey(network.createVertex(Primitive.RETURN))) {
						return result;
					} else if (result.is(Primitive.BREAK)) {
						return result;
					} else if (result.is(Primitive.CONTINUE)) {
						break;
					}
				}
			}
			depth++;
		}
		if (depth >= Language.MAX_LOOP) {
			network.getBot().log(this, "Max loop exceeded on while loop", Level.WARNING, Language.MAX_LOOP);
		}
		return result;
	}
	
	/**
	 * Evaluate the FOR operation.
	 * for (word in sentence.word) {}
	 * for (element in array) {}
	 * for (attribute in object) {}
	 * for (i = 0; i < 10; i++) {}
	 */
	public Vertex evaluateFOR(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		// Check if it is a 2 or 3 argument for loop.
		if (arguments.size() == 2) {
			Vertex variable = arguments.get(0).getTarget();
			Vertex getExpression = arguments.get(1).getTarget();
			Vertex source = null;
			Vertex relationship = null;
			List<Vertex> elements = null;
			// The for loop may be over an attribute, array, or object.
			if (getExpression.isVariable()) {
				// for (element in array) {}
				source = evaluateExpression(getExpression, variables, network, startTime, maxTime, stack);
				if (source.isArray()) {
					relationship = network.createVertex(Primitive.ELEMENT);
					elements = source.orderedRelations(relationship);
				} else {
					elements = new ArrayList<Vertex>();
					// for (attribute in object) {}
					for (Vertex value : source.getRelationships().keySet()) {
						if (!value.is(Primitive.INSTANTIATION)) {
							elements.add(value);
						}
					}
				}
			} else {
				// for (word in sentence.word) {}
				List<Vertex> values = getExpression.orderedRelations(Primitive.ARGUMENT);
				if (values == null) {
					return network.createVertex(Primitive.NULL);
				}
				source = evaluateExpression(values.get(0), variables, network, startTime, maxTime, stack);
				relationship = evaluateExpression(values.get(1), variables, network, startTime, maxTime, stack);
				elements = source.orderedRelations(relationship);
			}
			// Ignore #null in case it had relations.
			if (elements == null || source.is(Primitive.NULL)) {
				elements = new ArrayList<Vertex>(0);
			}
			List<Relationship> doEquations = expression.orderedRelationships(Primitive.DO);
			Vertex result;
			for (int index = 0; index < elements.size(); index++)  {
				variables.put(variable, elements.get(index));
				for (Relationship doEquation: doEquations) {
					result = evaluateExpression(doEquation.getTarget(), variables, network, startTime, maxTime, stack);
					if (variables.containsKey(network.createVertex(Primitive.RETURN))) {
						return result;
					} else if (result.is(Primitive.BREAK)) {
						return network.createVertex(Primitive.NULL);
					} else if (result.is(Primitive.CONTINUE)) {
						break;
					}
				}
			}
		} else if (arguments.size() == 3) {
			Vertex declaration = arguments.get(0).getTarget();
			Vertex condition = arguments.get(1).getTarget();
			Vertex operation = arguments.get(2).getTarget();
			List<Relationship> doEquations = expression.orderedRelationships(Primitive.DO);
			evaluateExpression(declaration, variables, network, startTime, maxTime, stack);
			Vertex value = evaluateExpression(condition, variables, network, startTime, maxTime, stack);
			Vertex result;
			while (value.is(Primitive.TRUE))  {
				for (Relationship doEquation: doEquations) {
					result = evaluateExpression(doEquation.getTarget(), variables, network, startTime, maxTime, stack);
					if (variables.containsKey(network.createVertex(Primitive.RETURN))) {
						return result;
					} else if (result.is(Primitive.BREAK)) {
						return network.createVertex(Primitive.NULL);
					} else if (result.is(Primitive.CONTINUE)) {
						break;
					}
				}
				evaluateExpression(operation, variables, network, startTime, maxTime, stack);
				value = evaluateExpression(condition, variables, network, startTime, maxTime, stack);
			}
		}
		return network.createVertex(Primitive.NULL);
	}
	
	/**
	 * Evaluate the literal object into a copy.
	 * evalcopy { name : speaker.name.toUpperCase(), salary : speaker.salary }
	 */
	public Vertex evaluateEVALCOPY(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 1, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Relationship literal = arguments.get(0);
		if (literal.getTarget().hasData()) {
			// Return
			return literal.getTarget();
		}
		// Copy.
		Vertex result = network.createVertex();
		for (Iterator<Relationship> iterator = literal.getTarget().orderedAllRelationships(); iterator.hasNext(); ) {
			Relationship relationship = iterator.next();
			Vertex value = evaluateExpression(relationship.getTarget(), variables, network, startTime, maxTime, stack);
			result.addRelationship(network.createVertex(relationship.getType()), value);
		}
		return result;
	}
	
	/**
	 * Evaluate the DO operation.
	 * do { x; y; z; }
	 */
	public Vertex evaluateDO(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		List<Relationship> doEquations = expression.orderedRelationships(Primitive.DO);
		Vertex result = network.createVertex(Primitive.NULL);
		if (doEquations == null) {
			return result;
		}
		Vertex returnPrimitive = network.createVertex(Primitive.RETURN);
		for (Relationship doEquation: doEquations) {
			result = evaluateExpression(doEquation.getTarget(), variables, network, startTime, maxTime, stack);
			if (variables.containsKey(returnPrimitive)) {
				variables.remove(returnPrimitive);
				return result;
			} else if (result.is(Primitive.BREAK)) {
				return result;
			}
		}
		return result;
	}
	
	/**
	 * Evaluate the THINK operation.
	 * think { x; y; z; }
	 * THINK is the same as DO but returns #return to avoid printing the result in a template.
	 */
	public Vertex evaluateTHINK(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		List<Relationship> doEquations = expression.orderedRelationships(Primitive.DO);
		Vertex result = network.createVertex(Primitive.NULL);
		Vertex returnPrimitive = network.createVertex(Primitive.RETURN);
		for (Relationship doEquation: doEquations) {
			result = evaluateExpression(doEquation.getTarget(), variables, network, startTime, maxTime, stack);
			if (variables.containsKey(returnPrimitive)) {
				variables.remove(returnPrimitive);
				return result;
			} else if (result.is(Primitive.BREAK)) {
				return result;
			}
		}
		return returnPrimitive;
	}

	/**
	 * Evaluate the LEARN operation.
	 * learn ({pattern : "hello", template : "Hello world"})
	 * Evaluate and add the new response.
	 */
	public Vertex evaluateLEARN(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) throws Exception {
		if (! checkArguments(expression, arguments, 1, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex learning = arguments.get(0).getTarget();
		// Cannot evaluate evalcopy as will evaluate templates.
		if (learning.instanceOf(Primitive.EXPRESSION)) {
			// Check for byte-code.
			if (learning.getData() instanceof BinaryData) {
				learning = SelfDecompiler.getDecompiler().parseExpressionByteCode(learning, (BinaryData)learning.getData(), learning.getNetwork());
			}
			Vertex operator = learning.getRelationship(Primitive.OPERATOR);
			if (operator == null) {
				return network.createVertex(Primitive.NULL);
			}
			if (operator.is(Primitive.EVALCOPY)) {
				List<Relationship> learnArguments = learning.orderedRelationships(Primitive.ARGUMENT);
				if (learnArguments != null && !learnArguments.isEmpty()) {
					learning = learnArguments.get(0).getTarget();
				}
			} else {
				learning = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
			}
		} else {
			learning = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		}
		Vertex pattern = learning.getRelationship(Primitive.PATTERN);
		if (pattern == null) {
			return network.createVertex(Primitive.NULL);
		}
		pattern = evaluateEVAL(pattern, arguments, variables, network, startTime, maxTime, stack);
		Vertex template = learning.getRelationship(Primitive.TEMPLATE);
		if (template == null) {
			return network.createVertex(Primitive.NULL);
		}
		template = evaluateEVAL(template, arguments, variables, network, startTime, maxTime, stack);
		Relationship relationship = pattern.addRelationship(Primitive.RESPONSE, template);
		template.addRelationship(Primitive.RESPONSE_QUESTION, pattern);
		Vertex that = learning.getRelationship(Primitive.THAT);
		if (that != null) {
			that = evaluateEVAL(that, arguments, variables, network, startTime, maxTime, stack);
			Vertex meta = network.createMeta(relationship);
			meta.addRelationship(Primitive.PREVIOUS, that);
			meta.addRelationship(Primitive.REQUIRE, Primitive.PREVIOUS);
		}
		Vertex topic = learning.getRelationship(Primitive.TOPIC);
		if (topic != null) {
			topic = evaluateEVAL(topic, arguments, variables, network, startTime, maxTime, stack);
			Vertex meta = network.createMeta(relationship);
			meta.addRelationship(Primitive.TOPIC, topic);
			meta.addRelationship(Primitive.REQUIRE, Primitive.TOPIC);
		}
		network.getBot().log(this, "New response learned", Level.FINER, pattern, template, that, topic);
		if (!pattern.instanceOf(Primitive.PATTERN)) {
			pattern.associateAll(Primitive.WORD, pattern, Primitive.QUESTION);
		} else {
			// Check for state and extend.
			Vertex state = variables.get(network.createVertex(Primitive.STATE));
			if (state != null) {
				// Get first case that gets sentence from input.
				List<Vertex> instructions = state.orderedRelations(Primitive.DO);
				Vertex sentenceState = null;
				if (instructions != null) {
					for (Vertex instruction : instructions) {
						if (instruction.instanceOf(Primitive.CASE)) {
							Vertex variable = instruction.getRelationship(Primitive.CASE);
							if ((variable != null) && variable.isVariable() && variable.hasRelationship(Primitive.INPUT)) {
								sentenceState = instruction.getRelationship(Primitive.GOTO);
								break;
							}
						}
					}				
				}
				if (sentenceState != null) {
					if (sentenceState.getNetwork() != network) {
						sentenceState = network.createVertex(sentenceState);
					}
					Vertex child = AIMLParser.parser().createState(pattern, sentenceState, network);
					Vertex equation = network.createInstance(Primitive.CASE);
					equation.addRelationship(Primitive.PATTERN, pattern);
					if (that != null) {
						equation.addRelationship(Primitive.THAT, that);
					}
					if (topic != null) {
						equation.addRelationship(Primitive.TOPIC, topic);
					}
					equation.addRelationship(Primitive.TEMPLATE, template);
					child.addRelationship(Primitive.DO, equation);
				}
			}
		}
		return pattern;
	}

	/**
	 * Evaluate the SRAI operation.
	 * srai ("Hello")
	 * Return the response to processing the input.
	 */
	public Vertex evaluateSRAI(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) throws Exception {
		if (! checkArguments(expression, arguments, 1, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex sentence = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		if (!sentence.instanceOf(Primitive.SENTENCE) && sentence.instanceOf(Primitive.FRAGMENT)) {
			sentence.addRelationship(Primitive.INSTANTIATION, Primitive.SENTENCE);
		}
		Vertex input = variables.get(network.createVertex(Primitive.INPUT_VARIABLE));
		input = input.copy();
		input.setRelationship(Primitive.INPUT, sentence);
		Vertex response = network.getBot().mind().getThought(Language.class).input(input, sentence, variables, network);
		if (response == null) {
			return network.createVertex(Primitive.NULL);
		}
		return response;
	}
	
	/**
	 * Evaluate the SRAIX operation.
	 * sraix ("what is love", {bot : "Brain Bot", limit : 5, service : #botlibre, apikey : 12345, botid : 12345, hint : "google", default : "Brain Bot is offline"})
	 * Execute the remote service call, and return the response.
	 */
	public Vertex evaluateSRAIX(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) throws Exception {
		if (! checkMinArguments(expression, arguments, 1, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex sentence = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		String apikeyValue = null;
		int limitValue = -1;
		String botValue = null;
		String botidValue = null;
		String serverValue = null;
		Primitive serviceValue = null;
		String hintValue = null;
		String defaultValue = null;
		if (arguments.size() > 1) {
			Vertex argument = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
			Vertex apikey = argument.getRelationship(Primitive.APIKEY);
			if (apikey != null) {
				apikey = evaluateExpression(apikey, variables, network, startTime, maxTime, stack);
				apikeyValue = apikey.printString();
			}
			Vertex limit = argument.getRelationship(Primitive.LIMIT);
			if (limit != null) {
				limit = evaluateExpression(limit, variables, network, startTime, maxTime, stack);
				limitValue = Integer.parseInt(limit.getDataValue());
			}
			Vertex bot = argument.getRelationship(Primitive.BOT);
			if (bot != null) {
				bot = evaluateExpression(bot, variables, network, startTime, maxTime, stack);
				botValue = bot.printString();
			}
			Vertex botid = argument.getRelationship(Primitive.BOTID);
			if (botid != null) {
				botid = evaluateExpression(botid, variables, network, startTime, maxTime, stack);
				botidValue = botid.printString();
			}
			Vertex server = argument.getRelationship(Primitive.SERVER);
			if (server != null) {
				server = evaluateExpression(server, variables, network, startTime, maxTime, stack);
				serverValue = server.printString();
			}
			Vertex service = argument.getRelationship(Primitive.SERVICE);
			if (service != null) {
				service = evaluateExpression(service, variables, network, startTime, maxTime, stack);
				if (service.isPrimitive()) {
					serviceValue = (Primitive)service.getData();
				}
			}
			Vertex hint = argument.getRelationship(Primitive.HINT);
			if (hint != null) {
				hint = evaluateExpression(hint, variables, network, startTime, maxTime, stack);
				hintValue = hint.printString();
			}
			Vertex defaultResponse = argument.getRelationship(Primitive.DEFAULT);
			if (defaultResponse != null) {
				defaultResponse = evaluateExpression(defaultResponse, variables, network, startTime, maxTime, stack);
				defaultValue = defaultResponse.printString();
			}
		}
		try {
			String message = sentence.printString();
			String response = network.getBot().awareness().getSense(RemoteService.class).request(message, botValue, botidValue, serverValue, serviceValue, apikeyValue, limitValue, hintValue, network);
			if (response == null) {
				if (defaultValue != null && !defaultValue.isEmpty()) {
					return network.createSentence(defaultValue);					
				}
				return network.createVertex(Primitive.NULL);
			}
			return network.createSentence(response);
		} catch (Exception exception) {
			network.getBot().log(this, exception);
			if (defaultValue != null && !defaultValue.isEmpty()) {
				return network.createSentence(defaultValue);					
			}
			return network.createVertex(Primitive.NULL);
		}
	}

	/**
	 * Evaluates any eval functions in the equation or formula..
	 * This is used by learn.
	 */
	public Vertex evaluateEVAL(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		Vertex result = null;
		try {
			if (expression.isVariable()) {
				result = variables.get(this);
				if (result == null) {
					if (expression.hasName()) {
						result = variables.get(expression.getName());
					}
					if (result == null) {
						result = network.createVertex(Primitive.NULL);
					}
				}
			} else if (expression.instanceOf(Primitive.EXPRESSION)) {
				// Check for byte-code.
				if (expression.getData() instanceof BinaryData) {
					expression = SelfDecompiler.getDecompiler().parseExpressionByteCode(expression, (BinaryData)expression.getData(), network);
					return evaluateEVAL(expression, arguments, variables, network, startTime, maxTime, stack);
				}
				Vertex operator = expression.getRelationship(Primitive.OPERATOR);
				List<Relationship> evalArguments = expression.orderedRelationships(Primitive.ARGUMENT);
				if (operator.is(Primitive.EVAL)) {
					// eval (x)
					return evaluateExpression(evalArguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
				}
			} else {
				result = (Vertex)(Object)expression;
			}
			if (result == null) {
				result = network.createVertex(Primitive.NULL);
			}
			if (result.getNetwork() != network) {
				result = network.createVertex(result);
			}
			boolean formula = result.instanceOf(Primitive.FORMULA);
			boolean pattern = result.instanceOf(Primitive.PATTERN);
			// Check for formula and transpose
			if (formula || pattern) {
				List<Vertex> words = result.orderedRelations(Primitive.WORD);
				if (words == null) {
					return result;
				}
				List<Vertex> newWords = new ArrayList<Vertex>(words.size());
				boolean eval = false;
				boolean formulaRequired = false;
				for (Vertex word: words) {
					if (word.instanceOf(Primitive.EXPRESSION)) {
						// Check for byte-code.
						if (word.getData() instanceof BinaryData) {
							word = SelfDecompiler.getDecompiler().parseExpressionByteCode(word, (BinaryData)word.getData(), network);
						}
						Vertex operator = word.getRelationship(Primitive.OPERATOR);
						if (operator != null && operator.is(Primitive.EVAL)) {
							eval = true;
							Vertex newWord = evaluateEVAL(word, arguments, variables, network, startTime, maxTime, stack);
							if (newWord.instanceOf(Primitive.EXPRESSION) || newWord.instanceOf(Primitive.FORMULA)) {
								formulaRequired = true;
							}
							newWords.add(newWord);
						} else {
							formulaRequired = true;
							newWords.add(word);
						}
					} else if (word.instanceOf(Primitive.VARIABLE)) {
						formulaRequired = true;
						newWords.add(word);
					} else {
						newWords.add(word);
					}
				}
				if (eval) {
					if (pattern) {
						result = network.createTemporyVertex();
						result.addRelationship(Primitive.INSTANTIATION, Primitive.PATTERN);
					} else if (formulaRequired) {
						result = network.createInstance(Primitive.FORMULA);
					} else {
						result = network.createTemporyVertex();
						result.addRelationship(Primitive.INSTANTIATION, Primitive.SENTENCE);
					}
					int index = 0;
					for (Vertex word : newWords) {
						result.addRelationship(Primitive.WORD, word, index);
						index++;
					}
					if (!formulaRequired) {
						Language language = network.getBot().mind().getThought(Language.class);
						result = language.createSentenceText(result, network);
						if (pattern) {
							result = network.createSentence(Utils.reduce(result.printString()));
						}
					}
				}
			}
		} catch (SelfExecutionException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new SelfExecutionException(expression, exception);
		}
		return result;
	}
	
	/**
	 * Evaluate the GET operation.
	 * x.y, x[#y], x.y[2], x[#y, #z, #q], x.get(#y), x.get(#y, 2), x.getLast(#y, 2), x.getAssociate(#y, #z, #q)
	 * Get the single relationship value of the type.
	 */
	public Vertex evaluateGET(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkMinArguments(expression, arguments, 2, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex result = null;
		Vertex source = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex relationship = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex index = expression.getRelationship(Primitive.INDEX);
		if ((index != null) && (index.getData() instanceof Number)) {
			int position = ((Number)index.getData()).intValue();
			List<Vertex> values = source.orderedRelations(relationship);
			if (values == null) {
				return network.createVertex(Primitive.NULL);
			}
			if (position < 0) {
				if ((position * -1) > values.size()) {
					return network.createVertex(Primitive.NULL);
				}
				// Negative means from end
				result = values.get(values.size() + position);
			} else {
				if (position >= values.size()) {
					return network.createVertex(Primitive.NULL);
				}
				result = values.get(position);
			}
		} else {
			if (arguments.size() > 3) {
				Vertex associate = evaluateExpression(arguments.get(2).getTarget(), variables, network, startTime, maxTime, stack);
				Vertex associateRelationship = evaluateExpression(arguments.get(3).getTarget(), variables, network, startTime, maxTime, stack);
				result = source.mostConsciousWithAssoiate(relationship, associate, associateRelationship);
			} else if (relationship.getData() instanceof Number && source.instanceOf(Primitive.ARRAY)) {
				List<Vertex> values = source.orderedRelations(Primitive.ELEMENT);
				if (values == null) {
					return network.createVertex(Primitive.NULL);
				}
				int position = ((Number)relationship.getData()).intValue();
				if (position < 0) {
					if ((position * -1) >= values.size()) {
						return network.createVertex(Primitive.NULL);
					}
					// Negative means from end
					result = values.get(values.size() + position);
				} else {
					if (position >= values.size()) {
						return network.createVertex(Primitive.NULL);
					}
					result = values.get(position);
				}
			} else if (relationship.getData() instanceof Number
						&& (source.instanceOf(Primitive.FRAGMENT) || source.instanceOf(Primitive.SENTENCE))) {
				List<Vertex> values = source.orderedRelations(Primitive.WORD);
				if (values == null) {
					return network.createVertex(Primitive.NULL);
				}
				int position = ((Number)relationship.getData()).intValue();
				if (position < 0) {
					if ((position * -1) >= values.size()) {
						return network.createVertex(Primitive.NULL);
					}
					// Negative means from end
					result = values.get(values.size() + position);
				} else {
					if (position >= values.size()) {
						return network.createVertex(Primitive.NULL);
					}
					result = values.get(position);
				}
			} else {
				result = source.mostConscious(relationship);
			}
		}
		if (result == null) {
			// Check all meanings of all words.
			Collection<Relationship> words = relationship.getRelationships(Primitive.WORD);
			if (words != null) {
				Set<Vertex> processed = new HashSet<Vertex>();
				processed.add(relationship);
				for (Relationship word : words) {
					Collection<Relationship> otherMeanings = word.getTarget().getRelationships(Primitive.MEANING);
					if (otherMeanings != null) {
						for (Relationship meaning : otherMeanings) {
							if (!processed.contains(meaning.getTarget())) {
								processed.add(meaning.getTarget());
								result = source.mostConscious(meaning.getTarget());
								if (result != null) {
									break;
								}
							}
						}
					}
				}
			}
			if (result == null) {
				result = network.createVertex(Primitive.NULL);
			}
		}
		return result;
	}

	/**
	 * Evaluate the SET operation.
	 * x.y = q
	 * SET the left with the right by the relationship, replace any existing relationship.
	 */
	public Vertex evaluateSET(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 3, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex source = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex relationship = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex target = evaluateExpression(arguments.get(2).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex index = expression.getRelationship(Primitive.INDEX);
		if ((index != null) && (index.getData() instanceof Number)) {
			int position = ((Number)index.getData()).intValue();
			if (position < 0) {
				// Negative means from end
				List<Vertex> values = source.orderedRelations(relationship);
				source.addRelationship(relationship, target, (values.size() + position));
			} else {
				source.addRelationship(relationship, target, position);
			}
		} else if ((relationship.getData() instanceof Number) && (source.isArray())) {
			// Check for setting array index.
			int position = ((Number)relationship.getData()).intValue();
			if (position < 0) {
				// Negative means from end
				List<Vertex> values = source.orderedRelations(relationship);
				source.addRelationship(network.createVertex(Primitive.ELEMENT), target, (values.size() + position));
			} else {
				source.addRelationship(network.createVertex(Primitive.ELEMENT), target, position);
			}
		} else {
			if (target.is(Primitive.NULL)) {
				source.internalRemoveRelationships(relationship);
			} else {
				source.setRelationship(relationship, target);
			}
		}
		// Following some crazy AIML implied rules here...
		if (relationship.isPrimitive() && (relationship.is(Primitive.IT) || relationship.is(Primitive.HE) || relationship.is(Primitive.SHE))) {
			return relationship;
		}
		return target;
	}

	/**
	 * Evaluate the ADD operation.
	 * x.y =+ q
	 * ADD the relationship from the left to the right.
	 */
	public Vertex evaluateADD(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 3, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex source = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex relationship = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex target = evaluateExpression(arguments.get(2).getTarget(), variables, network, startTime, maxTime, stack);
		source.addRelationship(relationship, target);
		return source;
	}

	/**
	 * Evaluate the REMOVE operation.
	 * x.y =- q
	 * REMOVE the relationship from the left to the right.
	 * This will define an inverse relationship.
	 */
	public Vertex evaluateREMOVE(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 3, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex source = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex relationship = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex target = evaluateExpression(arguments.get(2).getTarget(), variables, network, startTime, maxTime, stack);
		source.removeRelationship(relationship, target);
		return source;
	}

	/**
	 * Evaluate the EQUALS operation.
	 * x == y
	 * Return if the left is matches the right, or not.
	 */
	public Vertex evaluateEQUALS(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 2, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex left = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex right = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		Boolean matches = left.matches(right, variables);
		if (matches == null) {
			matches = false;
		}
		if (matches) {
			return network.createVertex(Primitive.TRUE);
		} else {
			return network.createVertex(Primitive.FALSE);
		}
	}

	/**
	 * Evaluate the NOTEQUALS operation.
	 * x != y
	 * Return if the left is matches the right, or not.
	 */
	public Vertex evaluateNOTEQUALS(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 2, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex left = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex right = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		Boolean matches = left.matches(right, variables);
		if (matches == null) {
			matches = false;
		}
		if (!matches) {
			return network.createVertex(Primitive.TRUE);
		} else {
			return network.createVertex(Primitive.FALSE);
		}
	}

	/**
	 * Evaluate the PLUS operation.
	 * x + y
	 * Add the two numbers.
	 */
	public Vertex evaluatePLUS(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 2, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex left = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex right = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		if ((left.getData() instanceof Number) && (right.getData() instanceof Number)) {
			return new org.botlibre.tool.Math().instance.plus(expression, left, right);
		}
		return network.createVertex(left.printString() + right.printString());
	}

	/**
	 * Evaluate the MINUS operation.
	 * x - y
	 * Subtract the two numbers.
	 */
	public Vertex evaluateMINUS(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 2, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex left = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex right = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		if ((left.getData() instanceof Number) && (right.getData() instanceof Number)) {
			return new org.botlibre.tool.Math().instance.minus(expression, left, right);
		}
		expression.getNetwork().getBot().log(this, "Invalid numbers for operation", Level.WARNING, expression, left, right);
		return network.createVertex(Primitive.NULL);
	}

	/**
	 * Evaluate the MULTIPLY operation.
	 * x * y
	 * Multiply the two numbers.
	 */
	public Vertex evaluateMULTIPLY(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 2, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex left = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex right = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		if ((left.getData() instanceof Number) && (right.getData() instanceof Number)) {
			return new org.botlibre.tool.Math().instance.multiply(expression, left, right);
		}
		expression.getNetwork().getBot().log(this, "Invalid numbers for operation", Level.WARNING, expression, left, right);
		return network.createVertex(Primitive.NULL);
	}

	/**
	 * Evaluate the DIVIDE operation.
	 * x / y
	 * Divide the two numbers.
	 */
	public Vertex evaluateDIVIDE(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 2, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex left = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex right = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		if ((left.getData() instanceof Number) && (right.getData() instanceof Number)) {
			return new org.botlibre.tool.Math().instance.divide(expression, left, right);
		}
		expression.getNetwork().getBot().log(this, "Invalid numbers for operation", Level.WARNING, expression, left, right);
		return network.createVertex(Primitive.NULL);
	}

	/**
	 * Evaluate the LESSTHAN operation.
	 * x < y
	 * Return if the left is less than the right, or not.
	 */
	public Vertex evaluateLESSTHAN(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 2, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex left = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex right = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		if ((left.getData() instanceof Number) && (right.getData() instanceof Number)) {
			if (((Number)left.getData()).doubleValue() < ((Number)right.getData()).doubleValue()) {
				return network.createVertex(Primitive.TRUE);
			} else {
				return network.createVertex(Primitive.FALSE);
			}
		}
		if ((left.getData() instanceof String) && (right.getData() instanceof String)) {
			if (((String)left.getData()).compareTo((String)right.getData()) < 0) {
				return network.createVertex(Primitive.TRUE);
			} else {
				return network.createVertex(Primitive.FALSE);
			}
		}
		if ((left.getData() instanceof java.util.Date) && (right.getData() instanceof java.util.Date)) {
			if (((java.util.Date)left.getData()).compareTo((java.util.Date)right.getData()) < 0) {
				return network.createVertex(Primitive.TRUE);
			} else {
				return network.createVertex(Primitive.FALSE);
			}
		}
		return network.createVertex(Primitive.UNKNOWN);
	}

	/**
	 * Evaluate the LESSTHANEQUAL operation.
	 * x <= y
	 * Return if the left is less than or equal the right, or not.
	 */
	public Vertex evaluateLESSTHANEQUAL(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 2, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex left = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex right = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		if ((left.getData() instanceof Number) && (right.getData() instanceof Number)) {
			if (((Number)left.getData()).doubleValue() <= ((Number)right.getData()).doubleValue()) {
				return network.createVertex(Primitive.TRUE);
			} else {
				return network.createVertex(Primitive.FALSE);
			}
		}
		if ((left.getData() instanceof String) && (right.getData() instanceof String)) {
			if (((String)left.getData()).compareTo((String)right.getData()) <= 0) {
				return network.createVertex(Primitive.TRUE);
			} else {
				return network.createVertex(Primitive.FALSE);
			}
		}
		if ((left.getData() instanceof java.util.Date) && (right.getData() instanceof java.util.Date)) {
			if (((java.util.Date)left.getData()).compareTo((java.util.Date)right.getData()) <= 0) {
				return network.createVertex(Primitive.TRUE);
			} else {
				return network.createVertex(Primitive.FALSE);
			}
		}
		if (left.matches(right, variables) == Boolean.TRUE) {
			return network.createVertex(Primitive.TRUE);
		}
		return network.createVertex(Primitive.UNKNOWN);
	}

	/**
	 * Evaluate the GREATERTHANEQUAL operation.
	 * x >= y
	 * Return if the left is greater than or equal the right, or not.
	 */
	public Vertex evaluateGREATERTHANEQUAL(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 2, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex left = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex right = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		if ((left.getData() instanceof Number) && (right.getData() instanceof Number)) {
			if (((Number)left.getData()).doubleValue() >= ((Number)right.getData()).doubleValue()) {
				return network.createVertex(Primitive.TRUE);
			} else {
				return network.createVertex(Primitive.FALSE);
			}
		}
		if ((left.getData() instanceof String) && (right.getData() instanceof String)) {
			if (((String)left.getData()).compareTo((String)right.getData()) >= 0) {
				return network.createVertex(Primitive.TRUE);
			} else {
				return network.createVertex(Primitive.FALSE);
			}
		}
		if ((left.getData() instanceof java.util.Date) && (right.getData() instanceof java.util.Date)) {
			if (((java.util.Date)left.getData()).compareTo((java.util.Date)right.getData()) >= 0) {
				return network.createVertex(Primitive.TRUE);
			} else {
				return network.createVertex(Primitive.FALSE);
			}
		}
		if (left.matches(right, variables) == Boolean.TRUE) {
			return network.createVertex(Primitive.TRUE);
		}
		return network.createVertex(Primitive.UNKNOWN);
	}

	/**
	 * Evaluate the GREATERTHAN operation.
	 * x > y
	 * Return if the left is greater than the right, or not.
	 */
	public Vertex evaluateGREATERTHAN(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 2, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex left = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		Vertex right = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		if ((left.getData() instanceof Number) && (right.getData() instanceof Number)) {
			if (((Number)left.getData()).doubleValue() > ((Number)right.getData()).doubleValue()) {
				return network.createVertex(Primitive.TRUE);
			} else {
				return network.createVertex(Primitive.FALSE);
			}
		}
		if ((left.getData() instanceof String) && (right.getData() instanceof String)) {
			if (((String)left.getData()).compareTo((String)right.getData()) > 0) {
				return network.createVertex(Primitive.TRUE);
			} else {
				return network.createVertex(Primitive.FALSE);
			}
		}
		if ((left.getData() instanceof java.util.Date) && (right.getData() instanceof java.util.Date)) {
			if (((java.util.Date)left.getData()).compareTo((java.util.Date)right.getData()) > 0) {
				return network.createVertex(Primitive.TRUE);
			} else {
				return network.createVertex(Primitive.FALSE);
			}
		}
		return network.createVertex(Primitive.UNKNOWN);
	}

	/**
	 * Evaluate the NOT operation.
	 * ! (x == y)
	 */
	public Vertex evaluateNOT(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 1, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex argument = arguments.get(0).getTarget();
		Vertex result = evaluateExpression(argument, variables, network, startTime, maxTime, stack);
		if (result.is(Primitive.TRUE)) {
			result = network.createVertex(Primitive.FALSE);
		} else if (result.is(Primitive.FALSE)) {
			result = network.createVertex(Primitive.TRUE);
		} else if (result.is(Primitive.UNKNOWN)) {
			result = network.createVertex(Primitive.TRUE);
		}
		return result;
	}

	/**
	 * Evaluate the OR condition.
	 * (x == y || y == z)
	 */
	public Vertex evaluateOR(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		Vertex first = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		if (first.is(Primitive.TRUE)) {
			return first;
		}
		if (arguments.size() == 1) {
			return network.createVertex(Primitive.FALSE);
		}
		Vertex second = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		if (second.is(Primitive.TRUE)) {
			return second;
		}
		return network.createVertex(Primitive.FALSE);
	}

	/**
	 * Evaluate the OR condition.
	 * (x == y || y == z)
	 */
	public Vertex evaluateAND(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		Vertex first = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		if (!first.is(Primitive.TRUE)) {
			return network.createVertex(Primitive.FALSE);
		}
		if (arguments.size() == 1) {
			return network.createVertex(Primitive.TRUE);
		}
		Vertex second = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		if (second.is(Primitive.TRUE)) {
			return network.createVertex(Primitive.TRUE);
		}
		return network.createVertex(Primitive.FALSE);
	}

	/**
	 * Evaluate the OR condition.
	 * (x instanceof Keyword)
	 */
	public Vertex evaluateINSTANCEOF(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		Vertex first = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		if (arguments.size() == 1) {
			return network.createVertex(Primitive.FALSE);
		}
		Vertex second = evaluateExpression(arguments.get(1).getTarget(), variables, network, startTime, maxTime, stack);
		if (first.instanceOf(second)) {
			return network.createVertex(Primitive.TRUE);
		}
		return network.createVertex(Primitive.FALSE);
	}
	
	/**
	 * Evaluate the NEW operation.
	 * new (#number, #sequence)
	 * Create a new vertex as an instance of the argument types.
	 */
	public Vertex evaluateNEW(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		Vertex newVertex = null;
		newVertex = network.createVertex();
		for (Relationship argument : arguments) {
			Vertex type = evaluateExpression(argument.getTarget(), variables, network, startTime, maxTime, stack);
			newVertex.addRelationship(Primitive.INSTANTIATION, type);
			// Assign the name of the type to the default name of the instance.
			/*Collection<Relationship> names = type.getRelationships(Primitive.WORD);
			if (names != null) {
				for (Relationship name : names) {
					newVertex.addRelationship(Primitive.WORD, name.getTarget());
				}
			}*/						
			// Check if the type is a classification, if not, make its instantiations, specializations.
			if (!type.hasRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION)) {
				Collection<Relationship> specializations = type.getRelationships(Primitive.INSTANTIATION);
				if (specializations != null) {
					for (Relationship specialization : specializations) {
						type.addRelationship(Primitive.SPECIALIZATION, specialization.getTarget());
					}
					type.addRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION);
				}
			}
		}
		return newVertex;
	}

	/**
	 * Evaluate the function invocation.
	 * x.y(a, b, c)
	 * i.e. #Context.push(x)
	 * Invoke the function declared on the object, or Sense/Thought/Tool, or primitive.
	 */
	@SuppressWarnings("rawtypes")
	public Vertex evaluateCALL(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) throws Exception {
		Vertex source = evaluateExpression(expression.getRelationship(Primitive.THIS), variables, network, startTime, maxTime, stack);
		Vertex function = evaluateExpression(expression.getRelationship(Primitive.FUNCTION), variables, network, startTime, maxTime, stack);
		Object sourceObject = null;
		if (source.isPrimitive()) {
			String name = ((Primitive)source.getData()).getIdentity().toLowerCase();
			if (name.equals("avatar")) {
				sourceObject = network.getBot().avatar();
			} else if (name.equals("memory")) {
				sourceObject = network.getBot().memory();
			} else if (name.equals("mood")) {
				sourceObject = network.getBot().mood();
			} else if (name.equals("mind")) {
				sourceObject = network.getBot().mind();
			} else if (name.equals("awareness")) {
				sourceObject = network.getBot().awareness();
			}
			if (sourceObject == null) {
				sourceObject = network.getBot().awareness().getSense(name);
			}
			if (sourceObject == null) {
				sourceObject = network.getBot().mind().getThought(name);
			}
			if (sourceObject == null) {
				sourceObject = network.getBot().awareness().getTool(name);
			}
		}
		if (sourceObject == null) {
			sourceObject = this;
		}
		if (!function.isPrimitive()) {
			return network.createVertex(Primitive.NULL);
		}
		String functionName = ((Primitive)function.getData()).getIdentity();
		Object[] methodArguments = new Object[arguments.size() + 1];
		Class[] argumentTypes = new Class[arguments.size() + 1];
		methodArguments[0] = source;
		argumentTypes[0] = Vertex.class;
		Vertex[] values = new Vertex[arguments.size()];
		for (int index = 0; index < arguments.size(); index++) {
			Vertex argument = evaluateExpression(arguments.get(index).getTarget(), variables, network, startTime, maxTime, stack);
			values[index] = argument;
			methodArguments[index + 1] = argument;
			argumentTypes[index + 1] = Vertex.class;
		}
		Method method = null;
		try {
			method = sourceObject.getClass().getMethod(functionName, argumentTypes);
		} catch (Exception missing) {
			// If a native object, then also try object methods.
			boolean found = false;
			if (sourceObject != this) {
				Object originalSource = sourceObject;
				sourceObject = this;
				try {
					method = sourceObject.getClass().getMethod(functionName, argumentTypes);
					found = true;
				} catch (Exception objectMissing) {
					// Try as array next.
					sourceObject = originalSource;
				}
			}
			if (!found) {
				methodArguments = new Object[2];
				argumentTypes = new Class[2];
				methodArguments[0] = source;
				methodArguments[1] = values;
				argumentTypes[0] = Vertex.class;
				argumentTypes[1] = Vertex[].class;
				try {
					method = sourceObject.getClass().getMethod(functionName, argumentTypes);
				} catch (Exception reallyMissing) {
					if (sourceObject != this) {
						// If a native object, then also try object methods.
						sourceObject = this;
						try {
							method = sourceObject.getClass().getMethod(functionName, argumentTypes);
						} catch (Exception objectMissing) {
							throw new SelfExecutionException(expression, "Missing function: " + functionName + " on: " + source);
						}
					} else {
						throw new SelfExecutionException(expression, "Missing function: " + functionName + " on: " + source);
					}
				}
			}
		}
		Vertex result = null;
		try {
			result = (Vertex)method.invoke(sourceObject, methodArguments);
		} catch (Exception exception) {
			network.getBot().log(sourceObject, exception);
		}
		if (result == null) {
			result = network.createVertex(Primitive.NULL);
		} else {
			result = network.createVertex(result);
		}
		return result;
	}

	/**
	 * Evaluate the RANDOM operation.
	 * random (x, y, z)
	 * Return a random argument.
	 * Only evaluate the selected argument.
	 */
	public Vertex evaluateRANDOM(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (arguments.isEmpty()) {
			return network.createVertex(Primitive.NULL);
		}
		return evaluateExpression(Utils.random(arguments).getTarget(), variables, network, startTime, maxTime, stack);
	}

	/**
	 * Evaluate the SYMBOL operation.
	 * Symbol(x)
	 * Return a random argument.
	 * Only evaluate the selected argument.
	 */
	public Vertex evaluateSYMBOL(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		if (! checkArguments(expression, arguments, 1, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex argument = arguments.get(0).getTarget();
		Vertex result = evaluateExpression(argument, variables, network, startTime, maxTime, stack);
		return network.createVertex(new Primitive(((String.valueOf(result.getData()).toLowerCase()))));
	}

	/**
	 * Evaluate the REDIRECT or SRAI operation.
	 * redirect("Hello"), srai("Hello")
	 * Return the response to processing the input.
	 */
	public Vertex evaluateREDIRECT(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) throws Exception {
		if (! checkArguments(expression, arguments, 1, network)) {
			return network.createVertex(Primitive.NULL);
		}
		Vertex sentence = evaluateExpression(arguments.get(0).getTarget(), variables, network, startTime, maxTime, stack);
		if (!sentence.instanceOf(Primitive.SENTENCE) && sentence.instanceOf(Primitive.FRAGMENT)) {
			sentence.addRelationship(Primitive.INSTANTIATION, Primitive.SENTENCE);
		}
		Vertex input = variables.get(network.createVertex(Primitive.INPUT_VARIABLE));
		input = input.copy();
		input.setRelationship(Primitive.INPUT, sentence);
		Vertex response = network.getBot().mind().getThought(Language.class).input(input, sentence, variables, network);
		if (response == null) {
			return network.createVertex(Primitive.NULL);
		}
		return response;
	}

	/**
	 * Evaluate the DEBUG operation.
	 * debug ("debug", :0, :2)
	 * Log the arguments to the log.
	 */
	public Vertex evaluateDEBUG(Vertex expression, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, long startTime, long maxTime, int stack) {
		StringWriter writer = new StringWriter();
		boolean first = true;
		Level level = null;
		for (Relationship argument : arguments) {
			if (first) {
				first = false;
				if (argument.getTarget().isPrimitive()) {
					// Allow log level to be set as first parameter.
					if (argument.getTarget().is(Primitive.FINEST)) {
						level = Level.FINEST;
						if (!network.getBot().isDebugFine()) {
							return network.createVertex(Primitive.NULL);
						}
					} else if (argument.getTarget().is(Primitive.FINER)) {
						level = Level.FINER;
						if (!network.getBot().isDebugFine()) {
							return network.createVertex(Primitive.NULL);
						}
					} else if (argument.getTarget().is(Primitive.FINE)) {
						level = Level.FINE;
						if (!network.getBot().isDebugFine()) {
							return network.createVertex(Primitive.NULL);
						}
					} else if (argument.getTarget().is(Primitive.INFO)) {
						level = Level.INFO;
					} else if (argument.getTarget().is(Primitive.WARNING)) {
						level = Level.WARNING;
					} else if (argument.getTarget().is(Primitive.SEVERE)) {
						level = Level.SEVERE;
					}
					if (level != null) {
						continue;
					}
					if (!network.getBot().isDebugFine()) {
						return network.createVertex(Primitive.NULL);
					}
				}
			} else{
				writer.write(" : ");
			}
			Vertex value = evaluateExpression(argument.getTarget(), variables, network, startTime, maxTime, stack);
			writer.write(value.printString());
		}
		if (level == null) {
			level = Level.FINE;
		}
		network.getBot().log("DEBUG", writer.toString(), level);
		return network.createVertex(Primitive.NULL);
	}

	/**
	 * Set the relationship from the source of the type, to the target object.
	 * This first clears any existing relationships of the same type.
	 */
	public Vertex set(Vertex source, Vertex type, Vertex target) {
		if (target.is(Primitive.NULL)) {
			source.internalRemoveRelationships(type);
		} else {
			source.setRelationship(type, target);
		}
		// Following some crazy AIML implied rules here...
		if (type.isPrimitive() && (type.is(Primitive.IT) || type.is(Primitive.HE) || type.is(Primitive.SHE))) {
			return type;
		}
		return target;
	}
	
	/**
	 * Array or Set add operation.
	 * Array elements use the #element type.
	 */
	public Vertex add(Vertex source, Vertex value) {
		if (source.instanceOf(Primitive.SET)) {
			source.addRelationship(source.getNetwork().createVertex(Primitive.ELEMENT), value);
		} else {
			source.addRelationship(source.getNetwork().createVertex(Primitive.ELEMENT), value, Integer.MAX_VALUE);
		}
		return source;
	}
	
	/**
	 * Add the relationship from the source of the type, to the target object.
	 * If the relationship already exists, increase its correctness.
	 */
	public Vertex add(Vertex source, Vertex type, Vertex target) {
		source.addRelationship(type, target);
		return source;
	}
	
	/**
	 * Add a weak relationship from the source of the type, to the target object.
	 * Weak relationships have a low correctness value.
	 */
	public Vertex weakAdd(Vertex source, Vertex type, Vertex target) {
		source.addWeakRelationship(type, target, 0.1f);
		return source;
	}
	
	public Vertex addWithMeta(Vertex source, Vertex relationship, Vertex value, Vertex metaType, Vertex metaValue) {
		Relationship relation = source.addRelationship(relationship, value);
		if (!metaValue.is(Primitive.NULL)) {
			Vertex meta = source.getNetwork().createMeta(relation);
			meta.addRelationship(metaType, metaValue);
		}
		return source;
	}
	
	public Vertex weakAddWithMeta(Vertex source, Vertex relationship, Vertex value, Vertex metaType, Vertex metaValue) {
		Relationship relation = source.addWeakRelationship(relationship, value, 0.1f);
		if (!metaValue.is(Primitive.NULL)) {
			Vertex meta = source.getNetwork().createMeta(relation);
			meta.addRelationship(metaType, metaValue);
		}
		return source;
	}
	
	public Vertex append(Vertex source, Vertex relationship, Vertex value) {
		source.addRelationship(relationship, value, Integer.MAX_VALUE);
		return source;
	}
	
	public Vertex appendWithMeta(Vertex source, Vertex relationship, Vertex value, Vertex metaType, Vertex metaValue) {
		Relationship relation = source.addRelationship(relationship, value, Integer.MAX_VALUE);
		if (!metaValue.is(Primitive.NULL)) {
			Vertex meta = source.getNetwork().createMeta(relation);
			meta.addRelationship(metaType, metaValue);
		}
		return source;
	}
	
	public Vertex remove(Vertex source, Vertex type, Vertex target) {
		source.removeRelationship(type, target);
		return source;
	}
	
	public Vertex removeWithMeta(Vertex source, Vertex relationship, Vertex value, Vertex metaType, Vertex metaValue) {
		Relationship relation = source.removeRelationship(relationship, value);
		if (!metaValue.is(Primitive.NULL)) {
			Vertex meta = source.getNetwork().createMeta(relation);
			meta.addRelationship(metaType, metaValue);
		}
		return source;
	}

	/**
	 * Determine the size of the relationship type.
	 */
	public Vertex size(Vertex source, Vertex type) {
		Collection<Relationship> elements = source.getRelationships(type);
		if (elements == null) {
			return source.getNetwork().createVertex(0);
		}
		return source.getNetwork().createVertex(elements.size());
	}

	/**
	 * Determine the size, by elements for an array, words for sentence, otherwise total relationships.
	 */
	public Vertex size(Vertex source) {
		if (source.instanceOf(Primitive.ARRAY)) {
			Collection<Relationship> elements = source.getRelationships(Primitive.ELEMENT);
			if (elements == null) {
				return source.getNetwork().createVertex(0);
			}
			return source.getNetwork().createVertex(elements.size());
		} else if (source.instanceOf(Primitive.SENTENCE) || source.instanceOf(Primitive.FRAGMENT)) {
			Collection<Relationship> elements = source.getRelationships(Primitive.WORD);
			if (elements == null) {
				return source.getNetwork().createVertex(0);
			}
			return source.getNetwork().createVertex(elements.size());
		} else if (source.getData() instanceof String) {
			return source.getNetwork().createVertex(((String)source.getData()).length());
		}
		return source.getNetwork().createVertex(source.getAllRelationships().size());
	}

	/**
	 * Determine the length, by elements for an array, or text/printstring size.
	 */
	public Vertex length(Vertex source) {
		if (source.instanceOf(Primitive.ARRAY)) {
			Collection<Relationship> elements = source.getRelationships(Primitive.ELEMENT);
			if (elements == null) {
				return source.getNetwork().createVertex(0);
			}
			return source.getNetwork().createVertex(elements.size());
		}
		return source.getNetwork().createVertex(source.printString().length());
	}

	/**
	 * Array or Set delete operation.
	 * Array elements use the #element type.
	 */
	public Vertex delete(Vertex source, Vertex target) {
		return delete(source, source.getNetwork().createVertex(Primitive.ELEMENT), target);
	}
	
	public Vertex deleteAll(Vertex source, Vertex type) {
		source.internalRemoveRelationships(type);
		return source;
	}
	
	public Vertex deleteAll(Vertex source) {
		List<Relationship> relationships = new ArrayList<Relationship>();
		for (Iterator<Relationship> iterator = source.allRelationships(); iterator.hasNext(); ) {
			relationships.add(iterator.next());
		}
		for (Relationship relationship : relationships) {
			source.internalRemoveRelationship(relationship);
		}
		return source;
	}
	
	public Vertex remove(Vertex source, Vertex type) {
		source.internalRemoveRelationships(type);
		return source;
	}
	
	public Vertex delete(Vertex source, Vertex type, Vertex target) {
		source.internalRemoveRelationship(type, target);
		return source;
	}
	
	public Vertex delete(Vertex source) {
		source.getNetwork().removeVertex(source);
		return source.getNetwork().createVertex(Primitive.NULL);
	}
	
	/**
	 * Search what references the object by the relationship.
	 * x.findReferenceBy(y)
	 */
	public Vertex findReferenceBy(Vertex source, Vertex type) {
		Vertex result = null;
		List<Relationship> relationships = source.getNetwork().findAllRelationshipsTo(source, type);
		for (Relationship relationship : relationships) {
			if (!relationship.isInverse() && ((result == null) || (relationship.getSource().getConsciousnessLevel() > result.getConsciousnessLevel()))) {
				result = relationship.getSource();
			}
		}
		if (result != null) {
			source.getNetwork().getBot().log(this, "Found reference", Level.FINER, source, type, result);
		} else {
			result = source.getNetwork().createVertex(Primitive.NULL);
			source.getNetwork().getBot().log(this, "No references", Level.FINER, source, type);
		}
		return result;
	}
	
	/**
	 * Search what references the object by the relationship.
	 * x.findReferencesBy(y)
	 */
	public Vertex findReferencesBy(Vertex source, Vertex type) {
		Network network = source.getNetwork();
		Vertex result = network.createInstance(Primitive.ARRAY);
		List<Relationship> relationships = network.findAllRelationshipsTo(source, type);
		int count = 0;
		for (Relationship relationship : relationships) {
			if (!relationship.isInverse()) {
				result.addRelationship(Primitive.ELEMENT, relationship.getSource(), Integer.MAX_VALUE);
				count++;
				if (count > 1000) {
					break;
				}
			}
		}
		network.getBot().log(this, "Found references", Level.FINER, source, type, count);
		return result;
	}
	
	/**
	 * Search what references the object.
	 * x.findReferences(y)
	 */
	public Vertex findReferences(Vertex source, Vertex type) {
		Network network = source.getNetwork();
		Vertex result = network.createInstance(Primitive.ARRAY);
		List<Relationship> relationships = network.findAllRelationshipsTo(source);
		int count = 0;
		for (Relationship relationship : relationships) {
			if (!relationship.isInverse()) {
				result.addRelationship(Primitive.ELEMENT, relationship.getSource(), Integer.MAX_VALUE);
				count++;
				if (count > 1000) {
					break;
				}
			}
		}
		network.getBot().log(this, "Found references", Level.FINER, source, type, count);
		return result;
	}
	
	/**
	 * Search what references the object.
	 * x.findReference()
	 */
	public Vertex findReference(Vertex source) {
		Vertex result = null;
		List<Relationship> relationships = source.getNetwork().findAllRelationshipsTo(source);
		for (Relationship relationship : relationships) {
			if (!relationship.isInverse() && ((result == null) || (relationship.getSource().getConsciousnessLevel() > result.getConsciousnessLevel()))) {
				result = relationship.getSource();
			}
		}
		if (result != null) {
			source.getNetwork().getBot().log(this, "Found reference", Level.FINER, source, result);
		} else {
			result = source.getNetwork().createVertex(Primitive.NULL);
			source.getNetwork().getBot().log(this, "No references", Level.FINER, source);
		}
		return result;
	}
	
	public Vertex set(Vertex source, Vertex type, Vertex target, Vertex index) {
		List<Vertex> values = source.orderedRelations(type);
		int size = 0;
		if (values != null) {
			size = values.size();
		}
		int indexValue = size;
		if (index.getData() instanceof Number) {
			indexValue = ((Number)index.getData()).intValue();
			if (indexValue < 0) {
				indexValue = size + indexValue;
			}
		} else {
			try {
				indexValue = Integer.valueOf(index.printString());
				if (indexValue < 0) {
					indexValue = size + indexValue;
				}
			} catch (Exception invalid) {};
		}
		source.addRelationship(type, target, indexValue);
		return source;
	}
	
	public Vertex all(Vertex source, Vertex type) {
		List<Vertex> values = source.orderedRelations(type);
		if (values == null) {
			return source.getNetwork().createVertex(Primitive.NULL);
		}
		Vertex all = source.getNetwork().createVertex();
		all.addRelationship(Primitive.INSTANTIATION, Primitive.ARRAY);
		int index = 0;
		for (Vertex value : values) {
			all.addRelationship(Primitive.ELEMENT, value, index);
			index++;
		}
		all.addRelationship(Primitive.LENGTH, source.getNetwork().createVertex(values.size()));
		return all;
	}
	
	public Vertex keys(Vertex source) {
		Vertex all = source.getNetwork().createVertex();
		all.addRelationship(Primitive.INSTANTIATION, Primitive.ARRAY);
		int index = 0;
		for (Vertex value : source.getRelationships().keySet()) {
			all.addRelationship(Primitive.ELEMENT, value, index);
			index++;
		}
		return all;
	}
	
	public Vertex getAccessDate(Vertex source, Vertex type, Vertex target) {
		Relationship relationship = source.getRelationship(type, target);
		if (relationship == null) {
			return source.getNetwork().createVertex(Primitive.NULL);
		} else {
			return source.getNetwork().createVertex(relationship.getAccessDate());
		}
	}
	
	public Vertex getIndex(Vertex source, Vertex type, Vertex target) {
		Relationship relationship = source.getRelationship(type, target);
		if (relationship == null) {
			return source.getNetwork().createVertex(Primitive.NULL);
		} else {
			return source.getNetwork().createVertex(relationship.getIndex());
		}
	}
	
	public Vertex getId(Vertex source, Vertex type, Vertex target) {
		Relationship relationship = source.getRelationship(type, target);
		if (relationship == null) {
			return source.getNetwork().createVertex(Primitive.NULL);
		} else {
			return source.getNetwork().createVertex(relationship.getId());
		}
	}
	
	public Vertex hasData(Vertex source) {
		if (source.hasData()) {
			return source.getNetwork().createVertex(Primitive.TRUE);
		} else {
			return source.getNetwork().createVertex(Primitive.FALSE);
		}
	}
	
	public Vertex dataType(Vertex source) {
		if (source.hasData()) {
			return source.getNetwork().createVertex(Primitive.NULL);
		} else {
			return source.getNetwork().createVertex(new Primitive(source.getDataType()));
		}
	}
	
	public Vertex pin(Vertex source) {
		source.setPinned(true);
		return source;
	}
	
	public Vertex unpin(Vertex source) {
		source.setPinned(false);
		return source;
	}
	
	public Vertex getId(Vertex source) {
		return source.getNetwork().createVertex(source.getId());
	}
	
	public Vertex isPinned(Vertex source) {
		return source.getNetwork().createVertex(source.isPinned());
	}
	
	public Vertex isPrimitive(Vertex source) {
		return source.getNetwork().createVertex(source.isPrimitive());
	}
	
	public Vertex isSymbol(Vertex source) {
		return source.getNetwork().createVertex(source.isPrimitive());
	}
	
	public Vertex isArray(Vertex source) {
		return source.getNetwork().createVertex(source.isArray());
	}
	
	public Vertex getCreationDate(Vertex source) {
		return source.getNetwork().createVertex(source.getCreationDate());
	}
	
	public Vertex getGroupId(Vertex source) {
		return source.getNetwork().createVertex(source.getGroupId());
	}
	
	public Vertex getAccessDate(Vertex source) {
		return source.getNetwork().createVertex(source.getAccessDate());
	}
	
	public Vertex getAccessCount(Vertex source) {
		return source.getNetwork().createVertex(source.getAccessCount());
	}
	
	public Vertex getConsciousnessLevel(Vertex source) {
		return source.getNetwork().createVertex(source.getConsciousnessLevel());
	}
	
	public Vertex copy(Vertex source) {
		return source.copy();
	}
	
	public Vertex hashCode(Vertex source) {
		return source.getNetwork().createVertex(source.hashCode());
	}
	
	public Vertex getName(Vertex source) {
		return source.getNetwork().createVertex(source.getName());
	}
	
	public Vertex getKey(Vertex source, Vertex target) {
		Vertex value = source.getRelationshipType(target);
		if (value == null) {
			return source.getNetwork().createVertex(Primitive.NULL);
		}
		return value;
	}
	
	public Vertex aimlGet(Vertex source, Vertex type) {
		Vertex value = source.mostConscious(type);
		if (value == null) {
			if (source.getNetwork().getBot().mind().getThought(Language.class).getAimlCompatibility()) {
				return source.getNetwork().createVertex("unknown");
			} else {
				return source.getNetwork().createVertex(Primitive.NULL);
			}
		}
		return value;
	}
	
	public Vertex mapGet(Vertex source, Vertex type) {
		Vertex value = source.mostConscious(type);
		if (value == null) {
			// Check lower case as well.
			if (source.getData() instanceof String) {
				Vertex lower = source.getNetwork().findByData(((String)source.getData()).toLowerCase());
				if (lower != null) {
					value = lower.mostConscious(type);
					if (value != null) {
						return value;
					}
				}
			}
			if (source.getNetwork().getBot().mind().getThought(Language.class).getAimlCompatibility()) {
				return source.getNetwork().createVertex("unknown");
			} else {
				return source.getNetwork().createVertex(Primitive.NULL);
			}
		}
		return value;
	}
	
	public Vertex get(Vertex source, Vertex type) {
		Vertex value = source.mostConscious(type);
		if (value == null) {
			return source.getNetwork().createVertex(Primitive.NULL);
		}
		return value;
	}
	
	public Vertex get(Vertex source, Vertex type, Vertex index) {
		List<Vertex> values = source.orderedRelations(type);
		if (values == null) {
			return source.getNetwork().createVertex(Primitive.NULL);
		}
		int indexValue = 0;
		if (index.getData() instanceof Number) {
			indexValue = ((Number)index.getData()).intValue();
		} else {
			try {
				indexValue = Integer.valueOf(index.printString());
			} catch (Exception invalid) {};
		}
		if (indexValue < 0) {
			indexValue = values.size() + indexValue;
		}
		if (indexValue >= 0 && indexValue < values.size()) {
			return values.get(indexValue);
		}
		source.getNetwork().getBot().log(this, "Invalid GET index", Level.FINE, source, type, index);
		return source.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Vertex getWithAssociate(Vertex source, Vertex type, Vertex associate, Vertex associateType) {
		Vertex value = source.mostConsciousWithAssoiate(type, associate, associateType);
		if (value == null) {
			return source.getNetwork().createVertex(Primitive.NULL);
		}
		return value;
	}
	
	public Vertex has(Vertex source, Vertex type, Vertex target) {
		Relationship relationship = source.getRelationship(type, target);
		if (relationship == null) {
			return source.getNetwork().createVertex(Primitive.UNKNOWN);
		} else if (relationship.isInverse()) {
			return source.getNetwork().createVertex(Primitive.FALSE);
		} else {
			return source.getNetwork().createVertex(Primitive.TRUE);
		}
	}
	
	public Vertex hasOrInherits(Vertex source, Vertex type, Vertex target) {
		if (source.hasOrInheritsRelationship(type, target)) {
			return source.getNetwork().createVertex(Primitive.TRUE);
		} else if (source.hasOrInheritsInverseRelationship(type, target)) {
			return source.getNetwork().createVertex(Primitive.FALSE);
		}
		return source.getNetwork().createVertex(Primitive.UNKNOWN);
	}
	
	public Vertex hasOtherMeaning(Vertex source, Vertex type, Vertex target) {
		Relationship relationship = source.getRelationship(type, target);
		if (relationship != null) {
			if (relationship.isInverse()) {
				return source.getNetwork().createVertex(Primitive.FALSE);
			} else {
				return source.getNetwork().createVertex(Primitive.TRUE);
			}
		}

		if (source.hasOrInheritsRelationship(type, target)) {
			// Left has the relationship, return true.
			return source.getNetwork().createVertex(Primitive.TRUE);
		} else if (source.hasOrInheritsInverseRelationship(type, target)) {
			// Left has an inverse relationship to the right, return false.
			return source.getNetwork().createVertex(Primitive.FALSE);
		} else {
			if (type.is(Primitive.IS)) {
				if (source.hasAnyRelationshipToTarget(target)) {
					// Left has a relationship to right, return true.
					return source.getNetwork().createVertex(Primitive.TRUE);
				}
			}
			if (target.getData() instanceof String) {
				// Check case.
				Vertex lower = source.getNetwork().createVertex(((String)target.getData()).toLowerCase());
				if (source.hasOrInheritsRelationship(type, lower)) {
					// Left has the relationship, return true.
					return source.getNetwork().createVertex(Primitive.TRUE);
				} else if (source.hasOrInheritsInverseRelationship(type, lower)) {
					// Left has an inverse relationship to the right, return false.
					return source.getNetwork().createVertex(Primitive.FALSE);
				}
				Vertex caps = source.getNetwork().createVertex(Utils.capitalize(((String)target.getData()).toLowerCase()));
				if (source.hasOrInheritsRelationship(type, caps)) {
					// Left has the relationship, return true.
					return source.getNetwork().createVertex(Primitive.TRUE);
				} else if (source.hasOrInheritsInverseRelationship(type, caps)) {
					// Left has an inverse relationship to the right, return false.
					return source.getNetwork().createVertex(Primitive.FALSE);
				}
			}
			Vertex result = null;
			// Check all meanings of all words.
			Collection<Relationship> words = target.getRelationships(Primitive.WORD);
			result = checkRelationTargetForAllWords(words, source, type, target, source.getNetwork());
			if (result != null) {
				return result;
			}
			
			// Check synonyms as well.
			words = target.getRelationships(Primitive.SYNONYM);
			result = checkRelationTargetForAllWords(words, source, type, target, source.getNetwork());
			if (result != null) {
				return result;
			}
			
			// Check all meanings of all words for relation.
			// Check all meanings of all words.
			words = type.getRelationships(Primitive.WORD);
			result = checkRelationRelationshipForAllWords(words, source, target, source.getNetwork());
			if (result != null) {
				return result;
			}

			// Check synonyms as well.
			words = type.getRelationships(Primitive.SYNONYM);
			result = checkRelationRelationshipForAllWords(words, source, target, source.getNetwork());
			if (result != null) {
				return result;
			}
		}
		// TODO: clean this up, and handle all other cases.
		return source.getNetwork().createVertex(Primitive.UNKNOWN);
	}
	
	public Vertex meta(Vertex source, Vertex type, Vertex target) {
		Relationship relationship = source.getRelationship(type, target);
		if (relationship == null) {
			return source.getNetwork().createVertex(Primitive.NULL);
		} else {
			return source.getNetwork().createMeta(relationship);
		}
	}
	
	public Vertex hasMeta(Vertex source, Vertex type, Vertex target) {
		Relationship relationship = source.getRelationship(type, target);
		if (relationship == null) {
			return source.getNetwork().createVertex(Primitive.FALSE);
		} else if (relationship.hasMeta()) {
			return source.getNetwork().createVertex(Primitive.TRUE);
		} else {
			return source.getNetwork().createVertex(Primitive.FALSE);
		}
	}
	
	public Vertex getCorrectness(Vertex source, Vertex type, Vertex target) {
		Relationship relationship = source.getRelationship(type, target);
		if (relationship == null) {
			return source.getNetwork().createVertex(0F);
		} else {
			return source.getNetwork().createVertex(relationship.getCorrectness());
		}
	}
	
	public Vertex setCorrectness(Vertex source, Vertex type, Vertex target, Vertex correctness) {
		Relationship relationship = source.getRelationship(type, target);
		if (relationship == null || !(correctness.getData() instanceof Number)) {
			return source.getNetwork().createVertex(Primitive.NULL);
		} else {
			relationship.setCorrectness(((Number)correctness.getData()).floatValue());
			return source;
		}
	}
	
	public Vertex getConsciousnessLevel(Vertex source, Vertex type, Vertex target) {
		Relationship relationship = source.getRelationship(type, target);
		if (relationship == null) {
			return source.getNetwork().createVertex(0);
		} else {
			return source.getNetwork().createVertex(relationship.getConsciousnessLevel());
		}
	}
	
	public Vertex getCreationDate(Vertex source, Vertex type, Vertex target) {
		Relationship relationship = source.getRelationship(type, target);
		if (relationship == null) {
			return source.getNetwork().createVertex(Primitive.NULL);
		} else {
			return source.getNetwork().createVertex(relationship.getCreationDate());
		}
	}
	
	public Vertex pin(Vertex source, Vertex type, Vertex target) {
		Relationship relationship = source.getRelationship(type, target);
		if (relationship == null) {
			return source.getNetwork().createVertex(Primitive.NULL);
		} else {
			relationship.setPinned(true);
			return source;
		}
	}
	
	public Vertex unpin(Vertex source, Vertex type, Vertex target) {
		Relationship relationship = source.getRelationship(type, target);
		if (relationship == null) {
			return source.getNetwork().createVertex(Primitive.NULL);
		} else {
			relationship.setPinned(false);
			return source;
		}
	}
	
	public Vertex isPinned(Vertex source, Vertex type, Vertex target) {
		Relationship relationship = source.getRelationship(type, target);
		if (relationship == null) {
			return source.getNetwork().createVertex(Primitive.FALSE);
		} else if (relationship.isPinned()) {
			return source.getNetwork().createVertex(Primitive.TRUE);
		} else {
			return source.getNetwork().createVertex(Primitive.FALSE);
		}
	}
	
	/**
	 * Check if any of the words have the relationship.
	 */
	public Vertex checkRelationRelationshipForAllWords(Collection<Relationship> words, Vertex source, Vertex target, Network network) {
		// Check all meanings of all words.
		if (words != null && !target.instanceOf(Primitive.WORD)) {
			Set<Vertex> processed = new HashSet<Vertex>();
			processed.add(target);
			for (Relationship word : words) {
				Collection<Relationship> otherMeanings = word.getTarget().getRelationships(Primitive.MEANING);
				if (otherMeanings != null) {
					for (Relationship meaning : otherMeanings) {
						if (!processed.contains(meaning.getTarget())) {
							processed.add(meaning.getTarget());
							if (source.hasOrInheritsRelationship(meaning.getTarget(), target)) {
								// Left has the relationship, return true.
								return network.createVertex(Primitive.TRUE);
							} else if (source.hasOrInheritsInverseRelationship(meaning.getTarget(), target)) {
								// Left has an inverse relationship to the right, return false.
								return network.createVertex(Primitive.FALSE);
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Check if any of the words have the relationship.
	 */
	public Vertex checkRelationTargetForAllWords(Collection<Relationship> words, Vertex source, Vertex type, Vertex target, Network network) {
		// Check all meanings of all words.
		if (words != null && !target.instanceOf(Primitive.WORD)) {
			Set<Vertex> processed = new HashSet<Vertex>();
			processed.add(target);
			for (Relationship word : words) {
				Collection<Relationship> otherMeanings = word.getTarget().getRelationships(Primitive.MEANING);
				if (otherMeanings != null) {
					for (Relationship meaning : otherMeanings) {
						if (!processed.contains(meaning.getTarget())) {
							processed.add(meaning.getTarget());
							if (source.hasOrInheritsRelationship(type, meaning.getTarget())) {
								// Left has the relationship, return true.
								return network.createVertex(Primitive.TRUE);
							} else if (source.hasOrInheritsInverseRelationship(type, meaning.getTarget())) {
								// Left has an inverse relationship to the right, return false.
								return network.createVertex(Primitive.FALSE);
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Array or Set has operation.
	 * Array elements use the #element type.
	 */
	public Vertex has(Vertex source, Vertex target) {
		return has(source, source.getNetwork().createVertex(Primitive.ELEMENT), target);
	}
	
	public Vertex hasAny(Vertex source, Vertex type) {
		if (source.hasRelationship(type)) {
			return source.getNetwork().createVertex(Primitive.TRUE);
		} else {
			return source.getNetwork().createVertex(Primitive.FALSE);
		}
	}
	
	public Vertex random(Vertex source) {
		Collection<Relationship> relationships = source.getRelationships(Primitive.ELEMENT);
		if (relationships == null) {
			return source.getNetwork().createVertex(Primitive.NULL);
		}
		return Utils.random(relationships).getTarget();
	}
	
	public Vertex random(Vertex source, Vertex type) {
		Collection<Relationship> relationships = source.getRelationships(type);
		if (relationships == null) {
			return source.getNetwork().createVertex(Primitive.NULL);
		}
		return Utils.random(relationships).getTarget();
	}
	
	public Vertex getLast(Vertex source, Vertex relationship) {
		List<Vertex> values = source.orderedRelations(relationship);
		if (values.isEmpty()) {
			return source.getNetwork().createVertex(Primitive.NULL);
		}
		return values.get(values.size() - 1);
	}
	
	public Vertex getLast(Vertex source, Vertex relationship, Vertex index) {
		List<Vertex> values = source.orderedRelations(relationship);
		if (values.isEmpty()) {
			return source.getNetwork().createVertex(Primitive.NULL);
		}
		int value = 1;
		if (index.getData() instanceof Number) {
			value = ((Number)index.getData()).intValue();
			if (value <= 0 || value > values.size()) {
				return source.getNetwork().createVertex(Primitive.NULL);
			}
		} else {
			return source.getNetwork().createVertex(Primitive.NULL);
		}
		return values.get(values.size() - value);
	}

	public Vertex toString(Vertex vertex) {
		return vertex.getNetwork().createVertex(vertex.printString());
	}

	public Vertex toJSON(Vertex source) {
		Network network = source.getNetwork();
		String data = network.getBot().awareness().getSense(Http.class).convertToJSON(source);
		return network.createVertex(data);
	}

	public Vertex toXML(Vertex source) {
		Network network = source.getNetwork();
		String data = network.getBot().awareness().getSense(Http.class).convertToXML(source);
		return network.createVertex(data);
	}

	public Vertex toSymbol(Vertex vertex) {
		return vertex.getNetwork().createVertex(new Primitive(vertex.printString().trim()));
	}

	public Vertex toUpperCase(Vertex text) {
		Vertex fragment = text.getNetwork().createFragment(text.printString().toUpperCase());
		fragment.addRelationship(Primitive.TYPE, Primitive.CASESENSITVE);
		return fragment;
	}

	public Vertex trim(Vertex text) {
		return text.getNetwork().createVertex(text.printString().trim());
	}
	
	/**
	 * Convert the pattern to a regex and test if the text matches the pattern.
	 */
	public Vertex test(Vertex pattern, Vertex text) {
		try {
			if (pattern.isPrimitive()) {
				pattern = pattern.getRelationship(Primitive.REGEX);
				if (pattern == null) {
					return null;
				}
			}
			Pattern p = Pattern.compile(pattern.printString());
			Matcher m = p.matcher(text.printString());
			if (m.find()) {
				return pattern.getNetwork().createVertex(Primitive.TRUE);
			} else {
				return pattern.getNetwork().createVertex(Primitive.FALSE);
			}
		} catch (Exception exception) { }
		return null;
	}
	
	/**
	 * Convert the pattern to a regex and test if the text matches the pattern.
	 */
	public Vertex exec(Vertex pattern, Vertex text) {
		try {
			if (pattern.isPrimitive()) {
				pattern = pattern.getRelationship(Primitive.REGEX);
				if (pattern == null) {
					return null;
				}
			}
			Pattern p = Pattern.compile(pattern.printString());
			Matcher m = p.matcher(text.printString());
			if (m.find()) {
				return pattern.getNetwork().createVertex(m.group());
			}
		} catch (Exception exception) { }
		return null;
	}
	
	/**
	 * Convert the pattern to a regex and extract all values from the text that match.
	 */
	public Vertex match(Vertex text, Vertex pattern) {
		try {
			if (pattern.isPrimitive()) {
				pattern = pattern.getRelationship(Primitive.REGEX);
				if (pattern == null) {
					return null;
				}
			}
			Pattern p = Pattern.compile(pattern.printString());
			Matcher m = p.matcher(text.printString());
			if (!m.find()) {
				return null;
			}
			Vertex result = pattern.getNetwork().createInstance(Primitive.ARRAY);
			result.appendRelationship(Primitive.ELEMENT, pattern.getNetwork().createVertex(m.group()));
			while (m.find()) {
				result.appendRelationship(Primitive.ELEMENT, pattern.getNetwork().createVertex(m.group()));
			}
			return result;
		} catch (Exception exception) { }
		return null;
	}

	public Vertex concat(Vertex source, Vertex argument) {
		return source.getNetwork().createVertex(source.printString() + argument.printString());
	}

	public Vertex includes(Vertex source, Vertex argument) {
		if (source.printString().indexOf(argument.printString()) == -1) {
			return source.getNetwork().createVertex(Primitive.FALSE);
		} else {
			return source.getNetwork().createVertex(Primitive.TRUE);
		}
	}

	public Vertex startsWith(Vertex source, Vertex argument) {
		if (source.printString().startsWith(argument.printString()) == false) {
			return source.getNetwork().createVertex(Primitive.FALSE);
		} else {
			return source.getNetwork().createVertex(Primitive.TRUE);
		}
	}

	public Vertex endsWith(Vertex source, Vertex argument) {
		if (source.printString().endsWith(argument.printString()) == false) {
			return source.getNetwork().createVertex(Primitive.FALSE);
		} else {
			return source.getNetwork().createVertex(Primitive.TRUE);
		}
	}

	public Vertex charAt(Vertex source, Vertex index) {
		if (!(index.getData() instanceof Number)) {
			return source.getNetwork().createVertex(Primitive.NULL);
		}
		return source.getNetwork().createVertex(String.valueOf(source.printString().charAt(((Number)index.getData()).intValue())));
	}

	public Vertex setCharAt(Vertex source, Vertex index, Vertex character) {
		if (!(index.getData() instanceof Number)) {
			return source.getNetwork().createVertex(Primitive.NULL);
		}
		int position = ((Number)index.getData()).intValue();
		String text = source.printString();
		String start = text.substring(0, position);
		String end = "";
		if ((position + 1) < text.length()) {
			end = text.substring(position + 1, text.length());
		}
		return source.getNetwork().createVertex(start + character.printString() + end);
	}

	public Vertex indexOf(Vertex source, Vertex argument) {
		if (source.instanceOf(Primitive.ARRAY)) {
			Relationship relationship = source.getRelationship(Primitive.ELEMENT, argument);
			if (relationship == null) {
				return source.getNetwork().createVertex(-1);
			}
			return source.getNetwork().createVertex(relationship.getIndex());
		}
		int index = source.printString().indexOf(argument.printString());
		return source.getNetwork().createVertex(index);
	}

	public Vertex indexOf(Vertex source, Vertex argument, Vertex start) {
		int startIndex = 0;
		if (start.getData() instanceof Number) {
			startIndex = ((Number)start.getData()).intValue();
		}
		if (source.instanceOf(Primitive.ARRAY)) {
			List<Vertex> elements = source.orderedRelations(Primitive.ELEMENT);
			if (elements == null) {
				return source.getNetwork().createVertex(-1);
			}
			for (int index = startIndex; index < elements.size(); index++) {
				Vertex element = elements.get(index);
				if (element.equals(argument)) {
					return source.getNetwork().createVertex(index);
				}
			}
			return source.getNetwork().createVertex(-1);
		}
		int index = source.printString().indexOf(argument.printString(), startIndex);
		return source.getNetwork().createVertex(index);
	}

	public Vertex lastIndexOf(Vertex source, Vertex argument) {
		if (source.instanceOf(Primitive.ARRAY)) {
			List<Vertex> elements = source.orderedRelations(Primitive.ELEMENT);
			if (elements == null) {
				return source.getNetwork().createVertex(-1);
			}
			for (int index = elements.size() - 1; index >= 0; index--) {
				Vertex element = elements.get(index);
				if (element.equals(argument)) {
					return source.getNetwork().createVertex(index);
				}
			}
			return source.getNetwork().createVertex(-1);
		}
		int index = source.printString().lastIndexOf(argument.printString());
		return source.getNetwork().createVertex(index);
	}

	public Vertex substr(Vertex source, Vertex start, Vertex end) {
		return substring(source, start, end);
	}

	public Vertex substring(Vertex source, Vertex start, Vertex end) {
		String text = source.printString();
		int startIndex = 0;
		int endIndex = text.length();
		if (start.getData() instanceof Number) {
			startIndex = ((Number)start.getData()).intValue();
		}
		if (end.getData() instanceof Number) {
			endIndex = ((Number)end.getData()).intValue();
		}
		return source.getNetwork().createVertex(text.substring(startIndex, endIndex));
	}

	public Vertex replace(Vertex source, Vertex token, Vertex replacement) {
		return source.getNetwork().createVertex(source.printString().replace(token.printString(), replacement.printString()));
	}

	public Vertex toLowerCase(Vertex text) {
		Vertex fragment = text.getNetwork().createFragment(text.printString().toLowerCase());
		fragment.addRelationship(Primitive.TYPE, Primitive.CASESENSITVE);
		return fragment;
	}

	public Vertex toNumber(Vertex text) {
		try {
			String value = text.printString();
			if (value.indexOf('.') != -1) {
				return text.getNetwork().createVertex(new BigDecimal(value));
			} else {
				return text.getNetwork().createVertex(new BigInteger(value));
			}
		} catch (Exception exception) {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}

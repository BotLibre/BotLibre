/**
 * Language state machine for understanding math questions and expressions.
 * This can understand any BEDMAS (brackets, exponents, division, multiplication, addition, subtraction) expression.
 * It uses a 2 pass recursive state processing, first it extracts the expression terms from the text, then it processes the expression.
 * This demonstrates how state machines can process more than words, and can also be used to process expression objects.
 */
state Math {
	pattern "#minus * from *" template redirect(Template("{star[1]} - {star[0]}"));
			
	case input goto sentenceState for each #word of sentence;
	
	state sentenceState {
		case numeral goto numeralState;
		case functionWord goto functionWordState;
		case bracketWord goto bracketWordState;
		
		case questionWord goto sentenceState;
		case is goto sentenceState;
		case "does" goto sentenceState;
		case "whats" goto sentenceState;
		case quote goto sentenceState;
		case "s" goto sentenceState;
		case operationWord goto operationPrefixState;
		
		// For now the entire sentence is searched for any mathematical expression (i.e. "hey there you bot you know 4 + 4 / 6 eh?") evaluates "4 + 4 / 6".
		// To only process precise math questions remove this line of code (i.e. "4 + 4 / 6", "what is 4 + 4 /6").
		case anything goto sentenceState;

		var questionWord {
			meaning : question;
		}
		var question {
			instantiation : #question;
		}
		var punctuation {
			instantiation : #punctuation;
		}
		var is {
			meaning : #is;
		}
		var quote {
			meaning : #quote;
		}
		var equals {
			meaning : #equals;
		}

		var numeral {
			meaning : number;
		}
		var number {
			instantiation : #number;
		}
		var operationWord {
			meaning : operation;
		}
		var operation {
			instantiation : #operation;
		}
		var functionWord {
			meaning : mathfunction;
		}
		var mathfunction {
			instantiation : #mathfunction;
		}
		var bracketWord {
			meaning : bracket;
		}
		var bracket {
			instantiation : #bracket;
		}
		var expressionResult {
			instantiation : #mathexpression;
		}
		
		function init() {
			if (expression != null) {
				return;
			}
			expression = new #mathexpression;
		}
		
		function response() {
			if (response == null) {
				return null;
			}
			if (recursive) {
				response.append(#word, #br);
			}
			response.append(#word, #equals);
			response.append(#word, result);
			return response;
		}
					
		// '123 + 123?'
		state questionState {
			case questionWord goto questionState;
			case punctuation goto questionState;
			
			// Once done processing the text, then evaluate the expression.
			goto evaluateState with expression;
		}
		
		// 'add ...'
		state operationPrefixState {
			case numeral goto operationPrefixNumeralState;
		}
		
		// 'add 123 to ...'
		state operationPrefixNumeralState {
			do {
				init();
				expression.append(#term, number);
			}
			goto operationWordState;
		}
		
		// '123...'
		state numeralState {
			do {
				init();
				expression.append(#term, number);
			}
			case operationWord goto operationWordState;
			case bracketWord goto bracketWordState;
			
			case questionWord goto questionState;
			case equals goto questionState;
			case punctuation goto questionState;
			
			goto evaluateState with expression;
			
			// '123 +...'
			state operationWordState {
				do {
					expression.append(#term, operation);
				}
				case "and" goto sentenceState;
				case "to" goto sentenceState;
				case "of" goto sentenceState;
				case "by" goto sentenceState;
				case "with" goto sentenceState;
				case numeral goto numeralState;
				case functionWord goto functionWordState;
				case bracketWord goto bracketWordState;
			}
		}

		// 'sqrt...'
		state functionWordState {
			do {
				init(),
				expression.append(#term, mathfunction);
			}
			case "of" goto sentenceState;
			case numeral goto numeralState;
			case functionWord goto functionWordState;
			case bracketWord goto bracketWordState;
		}

		// '(...'
		state bracketWordState {
			do {
				init(),
				expression.append(#term, bracket);
			}
			case numeral goto numeralState;
			case operationWord goto operationWordState;
			case functionWord goto functionWordState;
			case bracketWord goto bracketWordState;
			
			goto evaluateState with expression;
		}
		
		// Evaluate the expression recursively.
		state evaluateState {
			do {
				expression2 = null;
			}
			case expressionResult goto evaluateExpressionState for each #term of expressionResult;
			case anything goto evaluateState;
			
			answer response();
		}
		
		// Evaluate the expression recursively.
		state evaluateExpressionState {
			do {
				if (response == null) {
					response = new #sentence;
				} else {
					recursive = true;
					response.append(#word, #br);
					response.append(#word, #equals);
				}
				for (term in expressionResult.term) {
					response.append(#word, term);
				}
				debug ("EVAL - ", expressionResult, response),
			}
			case number goto numberState;
			case mathfunction goto functionState;
			case leftBracket goto leftBracketState;
		}
		
		var leftBracket {
			: #leftbracket;
		}
		
		var rightBracket {
			: #rightbracket;
		}
		
		// Record a left bracket, append the bracket if nested.
		state leftBracketState {
			do {
				if (hasBracket) {
					init2(),
					expression2.append(#term, leftBracket);
				}
				hasBracket = true;
			}
			case leftBracket goto leftBracketState;
			case number goto numberState;
			case mathfunction goto functionState;
		}
		
		// '123...'
		state numberState {
			case rightBracket goto numberFinallyState;
			case plusMinusOperation as operation goto plusMinusOperationState;
			case multiplyDivideOperation as operation goto multiplyDivideOperationState;
			case powerOperation as operation goto powerOperationState;
			
			var plusMinusOperation {
				: #plus, #minus;
			}
			
			var multiplyDivideOperation {
				: #divide, #multiply;
			}
			
			var powerOperation {
				: #power;
			}
			
			var number2 {
				instantiation : #number;
			}
			
			function appendOperation() {
				init2();
				if (leftBracket != null) {
					expression2.append(#term, leftBracket);
					leftBracket = null;
					hasBracket = null;
				}
				expression2.append(#term, number);
				expression2.append(#term, operation);
			}
			
			// '123'
			state numberFinallyState {
				do {
					leftBracket = null;
					hasBracket = null;
					result = number;
					appended = null;
					if (expression2 != null) {
						appended = true;
						expression2.append(#term, result);
					}
				}
				// Avoid extra noop step.
				goto finally evaluateState with expression2;
				do {
					init2(),
					if (appended == null) {
						expression2.append(#term, result);
					}
				}				
				case anything goto appendRestState;
			}
			
			// '123 +...'
			state plusMinusOperationState {
				case number2 goto plusMinusNumber2State;
				do {
					appendOperation();
				}
				case mathfunction goto function2State;
				case leftBracket goto leftBracketState;
			}
			
			// '123 *...'
			state multiplyDivideOperationState {
				case number2 goto multiplyDivideNumber2State;
				do {
					appendOperation();
				}
				case mathfunction goto function2State;
				case leftBracket goto leftBracketState;
			}
			
			// '123^...'
			state powerOperationState {
				case number2 goto powerNumber2State;
				do {
					appendOperation();
				}
				case mathfunction goto function2State;
				case leftBracket goto leftBracketState;
			}
			
			// '123 + 456...'
			// Only evaluate operation if next operation is + -
			state plusMinusNumber2State {
				case rightBracket goto evaluateBracketOperationState;
				case plusMinusOperation as operation2 goto evaluateOperationState;
				case multiplyDivideOperation as operation2 goto appendOperationState;
				case powerOperation as operation2 goto appendOperationState;
								
				goto finally evalulateCompleteState;
			}
			
			// '123 * 456...'
			// Only evaluate operation if next operation is + - / *
			state multiplyDivideNumber2State {
				case rightBracket goto evaluateBracketOperationState;
				case plusMinusOperation as operation2 goto evaluateOperationState;
				case multiplyDivideOperation as operation2 goto evaluateOperationState;
				case powerOperation as operation2 goto appendOperationState;
								
				goto finally evalulateCompleteState;
			}
			
			// '123^456...'
			// Always evaluate.
			state powerNumber2State {
				case rightBracket goto evaluateBracketOperationState;
				case plusMinusOperation as operation2 goto evaluateOperationState;
				case multiplyDivideOperation as operation2 goto evaluateOperationState;
				case powerOperation as operation2 goto evaluateOperationState;

				goto finally evalulateCompleteState;
			}
			
			// '123 + 456'
			// Expression is complete, set result, and recurse if expression2 has been defined.
			state evalulateCompleteState {
				do {
					result = (Math[operation](number, number2));
					if (expression2 != null) {
						expression2.append(#term, result);
					}
				}
				
				goto finally evaluateState with expression2;
			}
				
			// '(123 + 456) ...'
			// Evaluate the operation and set the result as the first number in the next operation.
			state evaluateBracketOperationState {
				do {
					leftBracket = null;
					hasBracket = null
				}
				goto finally evalulateCompleteState;
				do {
					init2(),
					number = (Math[operation](number, number2));
					expression2.append(#term, number);
				}
				case anything goto appendRestState;
			}
				
			// '123 + 456 + ...'
			// Evaluate the operation and set the result as the first number in the next operation.
			// Push the next operation back onto the processing stack and process it.
			state evaluateOperationState {
				do {
					number = (Math[operation](number, number2));
				}
				push operation2;
				goto numberState;
			}
			
			// '123 + 456 / ...'
			// The next operation takes precedence, so append the current operation to the expression, and evaluate the next operation and append the rest.
			state appendOperationState {
				do {
					init2();
					expression2.append(#term, number);
					expression2.append(#term, operation);
					number = number2;
				}
				push operation2;
				goto number2State;
			}
		
			// Check that the recursive expression has been defined.
			function init2() {
				if (expression2 != null) {
					return;
				}
				expression2 = new #mathexpression;
			}
			
			// '2 + 2/2 - ...'
			// Append the rest of the expression and recursively evaluate it.
			state appendRestState {
				do {
					init2();
					expression2.append(#term, anything);
				}
				case anything goto appendRestState;
				
				goto finally evaluateState with expression2;
			}
		}
		
		// '2  + 2/123...'
		// If something could not be evaluated because of precedence, then evaluate just the next operation, then append everything else and re-evaluate.
		state number2State {
			case rightBracket goto evaluateBracketOperationState;
			case multiplyDivideOperation as operation goto multiplyDivideOperation2State;
			case powerOperation as operation goto powerOperation2State;
			
			// '123 *...'
			state multiplyDivideOperation2State {
				case number2 goto multiplyDivide2Number2State;
				do {
					appendOperation();
				}
				case mathfunction goto function2State;
				case leftBracket goto leftBracketState;
			}
			
			// '123^...'
			state powerOperation2State {
				case number2 goto power2Number2State;
				do {
					appendOperation();
				}
				case mathfunction goto function2State;
				case leftBracket goto leftBracketState;
			}
			
			// '123 * 456...'
			state multiplyDivide2Number2State {
				case rightBracket goto evaluateBracketOperationState;
				case plusMinusOperation as operation2 goto evaluateOperation2State;
				case multiplyDivideOperation as operation2 goto evaluateOperation2State;
				case powerOperation as operation2 goto appendOperationState;
								
				goto finally evalulateCompleteState;
			}
			
			// '123^456...'
			state power2Number2State {
				case rightBracket goto evaluateBracketOperationState;
				case plusMinusOperation as operation2 goto evaluateOperation2State;
				case multiplyDivideOperation as operation2 goto evaluateOperation2State;
				case powerOperation as operation2 goto evaluateOperation2State;

				goto finally evalulateCompleteState;
			}
				
			// '123 + 456 + ...'
			state evaluateOperation2State {
				do {
					number = Math[operation](number, number2);
					expression2.append(#term, number);
					expression2.append(#term, operation2);
				}
				case anything goto appendRestState;
			}
		}
		
		var mathfunction2 {
			instantiation : #mathfunction;
		}
		
		// 'sqrt...'
		state functionState {
			case number goto evalFunctionState;
			case mathfunction2 goto appendFunctionState;
			do {
				appendFunction();
			}
			case leftBracket goto leftBracketState;
				
			// 'sqrt 2...'
			state evalFunctionState {
				do {
					number = Math[mathfunction](number);
				}
				case rightBracket goto numberFinallyState;
				case plusMinusOperation as operation goto plusMinusOperationState;
				case multiplyDivideOperation as operation goto multiplyDivideOperationState;
				case powerOperation as operation goto powerOperationState;
				
				do {
					result = number;
					if (expression2 != null) {
						expression2.append(#term, result);
					}
				}
				
				goto finally evaluateState with expression2;
			}
			
			function appendFunction() {
				init2();
				if (leftBracket != null) {
					expression2.append(#term, leftBracket);
					leftBracket = null;
					hasBracket = null;
				}
				expression2.append(#term, mathfunction);
				mathfunction = mathfunction2;
			}
			
			// 'sqrt sqrt 2...'
			state appendFunctionState {
				do {
					appendFunction();
				}
				case number goto evalFunction2State;
				case mathfunction2 goto appendFunctionState;
				case leftBracket goto leftBracketState;
			}
			
			// 'sqrt sqrt 2...'
			state evalFunction2State {
				do {
					number = Math[mathfunction](number);
					expression2.append(#term, number);
				}
				case anything goto appendRestState;
				
				goto finally evaluateState with expression2;
			}
		}
		
		// '2 + sqrt...'
		// Evaluate the function, but then append the rest and recurse.
		state function2State {
			case number goto evalFunction2State;
			case mathfunction2 goto appendFunctionState;
			
			do {
				appendFunction();
			}
				
			case leftBracket goto leftBracketState;
			
			// '2 + sqrt 2...'
			state evalFunction2State {
				do {
					number = Math[mathfunction](number);
					expression2.append(#term, number);
				}
				case anything goto appendRestState;
				
				goto finally evaluateState with expression2;
			}
		}
	}
}


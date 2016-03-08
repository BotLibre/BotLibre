// Language state machine for understanding math.
// This can understand any BEDMAS (brackets, exponents, division, multiplication, addition, subtraction) expression.
// It uses a 2 pass recursive state processing, first it extracts the expression terms from the text, then it processes the expression.
State:Calculator {
	case :input goto State:sentenceState for each #word of :sentence;

	:input {
		set #input to :sentence;
	}
	
	:sentence {
		set #instantiation to #sentence;
	}
	
	State:sentenceState {
		case :numeral goto State:numeralState;
		case :functionWord goto State:functionWordState;
		case :bracketWord goto State:bracketWordState;
		
		case :questionWord goto State:sentenceState;
		case :is goto State:sentenceState;
		case "does" goto State:sentenceState;
		case "whats" goto State:sentenceState;
		case :quote goto State:sentenceState;
		case "s" goto State:sentenceState;
		case :operationWord goto State:operationPrefixState;
		
		// For now the entire sentence is searched for any mathematical expression.
		// To only process precise math questions remove this.
		case :anything goto State:sentenceState;

		:questionWord {
			set #meaning to :question;
		}
		:question {
			set #instantiation to #question;
		}
		:punctuation {
			set #instantiation to #punctuation;
		}
		:is {
			set #meaning to #is;
		}
		:quote {
			set #meaning to #quote;
		}
		:equals {
			set #meaning to #equals;
		}

		:numeral {
			set #meaning to :number;
		}
		:number {
			set #instantiation to #number;
		}
		:operationWord {
			set #meaning to :operation;
		}
		:operation {
			set #instantiation to #operation;
		}
		:functionWord {
			set #meaning to :function;
		}
		:function {
			set #instantiation to #function;
		}
		:bracketWord {
			set #meaning to :bracket;
		}
		:bracket {
			set #instantiation to #bracket;
		}
		:expressionResult {
			set #instantiation to #expression;
		}
		
		Function:init {
			if not (:expression, #null)
				then return;
			assign :expression to (new #expression);
		}
		
		Function:response {
			if (:response, #null)
				then (return #null);
			if (:recursive, #true)
				then (append #br to #word of :response);
			append #equals to #word of :response;
			append :result to #word of :response;
			return :response;
		}
					
		// '123 + 123?'
		State:questionState {
			case :questionWord goto State:questionState;
			case :punctuation goto State:questionState;
			
			// Once done processing the text, then evaluate the expression.
			goto State:evaluateState with :expression;
		}
		
		// 'add ...'
		State:operationPrefixState {
			case :numeral goto State:operationPrefixNumeralState;
		}
		
		// 'add 123 to ...'
		State:operationPrefixNumeralState {
			pattern "#minus * from *" answer redirect (formula:"{get #word from :star at 2} - {get #word from :star at 1}");
			do (
				Function:init,
				append :number to #term of :expression
			);
			goto State:operationWordState;
		}
		
		// '123...'
		State:numeralState {
			do (
				Function:init,
				append :number to #term of :expression
			);
			case :operationWord goto State:operationWordState;
			case :bracketWord goto State:bracketWordState;
			
			case :questionWord goto State:questionState;
			case :equals goto State:questionState;
			case :punctuation goto State:questionState;
			
			goto State:evaluateState with :expression;
			
			// '123 +...'
			State:operationWordState {
				do (
					append :operation to #term of :expression
				);
				case "and" goto State:sentenceState;
				case "to" goto State:sentenceState;
				case "of" goto State:sentenceState;
				case "by" goto State:sentenceState;
				case "with" goto State:sentenceState;
				case :numeral goto State:numeralState;
				case :functionWord goto State:functionWordState;
				case :bracketWord goto State:bracketWordState;
			}
		}

		// 'sqrt...'
		State:functionWordState {
			do (
				Function:init,
				append :function to #term of :expression
			);
			case "of" goto State:sentenceState;
			case :numeral goto State:numeralState;
			case :functionWord goto State:functionWordState;
			case :bracketWord goto State:bracketWordState;
		}

		// '(...'
		State:bracketWordState {
			do (
				Function:init,
				append :bracket to #term of :expression
			);
			case :numeral goto State:numeralState;
			case :operationWord goto State:operationWordState;
			case :functionWord goto State:functionWordState;
			case :bracketWord goto State:bracketWordState;
			
			goto State:evaluateState with :expression;
		}
		
		// Evaluate the expression recursively.
		State:evaluateState {
			do (
				assign :expression2 to #null
			);
			case :expressionResult goto State:evaluateExpressionState for each #term of :expressionResult;
			case :anything goto State:evaluateState;
			
			Answer:Function:response;
		}
		
		// Evaluate the expression recursively.
		State:evaluateExpressionState {
			do (
				if (:response, #null)
					then (assign :response to (new #sentence))
					else (do (
						assign :recursive to #true,
						append #br to #word of :response,
						append #equals to #word of :response));
				for each #term of :expressionResult as :term do (
					append :term to #word of :response
				);
				debug ("EVAL - ", :expressionResult, :response),
			);
			case :number goto State:numberState;
			case :function goto State:functionState;
			case :leftBracket goto State:leftBracketState;
		}
		
		:leftBracket {
			include #leftbracket;
		}
		
		:rightBracket {
			include #rightbracket;
		}
		
		// Record a left bracket, append the bracket if nested.
		State:leftBracketState {
			do (
				if (:hasBracket, #true)
					then (do (
						Function:init2,
						append :leftBracket to #term of :expression2
					)),
				assign :hasBracket to #true
			);
			case :leftBracket goto State:leftBracketState;
			case :number goto State:numberState;
			case :function goto State:functionState;
		}
		
		// '123...'
		State:numberState {
			case :rightBracket goto State:numberFinallyState;
			case :plusMinusOperation as :operation goto State:plusMinusOperationState;
			case :multiplyDivideOperation as :operation goto State:multiplyDivideOperationState;
			case :powerOperation as :operation goto State:powerOperationState;
			
			:plusMinusOperation {
				include #plus;
				include #minus;
			}
			
			:multiplyDivideOperation {
				include #divide;
				include #multiply;
			}
			
			:powerOperation {
				include #power;
			}
			
			:number2 {
				set #instantiation to #number;
			}
			
			Function:appendOperation {
				Function:init2;
				if not (:leftBracket, #null)
					then (do (
						append :leftBracket to #term of :expression2,
						assign :leftBracket to #null,
						assign :hasBracket to #null));
				append :number to #term of :expression2;
				append :operation to #term of :expression2;
			}
			
			// '123'
			State:numberFinallyState {
				do (
					assign :leftBracket to #null,
					assign :hasBracket to #null,
					assign :result to :number,
					assign :appended to #null,
					if not (:expression2, #null)
						then (do (
							assign :appended to #true,
							append :result to #term of :expression2))
				);
				// Avoid extra noop step.
				goto finally State:evaluateState with :expression2;
				do (
					Function:init2,
					if (:appended, #null)
						then (append :result to #term of :expression2)
				);				
				case :anything goto State:appendRestState;
			}
			
			// '123 +...'
			State:plusMinusOperationState {
				case :number2 goto State:plusMinusNumber2State;
				do (
					Function:appendOperation
				);
				case :function goto State:function2State;
				case :leftBracket goto State:leftBracketState;
			}
			
			// '123 *...'
			State:multiplyDivideOperationState {
				case :number2 goto State:multiplyDivideNumber2State;
				do (
					Function:appendOperation
				);
				case :function goto State:function2State;
				case :leftBracket goto State:leftBracketState;
			}
			
			// '123^...'
			State:powerOperationState {
				case :number2 goto State:powerNumber2State;
				do (
					Function:appendOperation
				);
				case :function goto State:function2State;
				case :leftBracket goto State:leftBracketState;
			}
			
			// '123 + 456...'
			// Only evaluate operation if next operation is + -
			State:plusMinusNumber2State {
				case :rightBracket goto State:evaluateBracketOperationState;
				case :plusMinusOperation as :operation2 goto State:evaluateOperationState;
				case :multiplyDivideOperation as :operation2 goto State:appendOperationState;
				case :powerOperation as :operation2 goto State:appendOperationState;
								
				goto finally State:evalulateCompleteState;
			}
			
			// '123 * 456...'
			// Only evaluate operation if next operation is + - / *
			State:multiplyDivideNumber2State {
				case :rightBracket goto State:evaluateBracketOperationState;
				case :plusMinusOperation as :operation2 goto State:evaluateOperationState;
				case :multiplyDivideOperation as :operation2 goto State:evaluateOperationState;
				case :powerOperation as :operation2 goto State:appendOperationState;
								
				goto finally State:evalulateCompleteState;
			}
			
			// '123^456...'
			// Always evaluate.
			State:powerNumber2State {
				case :rightBracket goto State:evaluateBracketOperationState;
				case :plusMinusOperation as :operation2 goto State:evaluateOperationState;
				case :multiplyDivideOperation as :operation2 goto State:evaluateOperationState;
				case :powerOperation as :operation2 goto State:evaluateOperationState;

				goto finally State:evalulateCompleteState;
			}
			
			// '123 + 456'
			// Expression is complete, set result, and recurse if expression2 has been defined.
			State:evalulateCompleteState {
				do (
					assign :result to (call :operation on #Calculator with (:number, :number2)),
					if not (:expression2, #null)
						then (append :result to #term of :expression2)
				);
				
				goto finally State:evaluateState with :expression2;
			}
				
			// '(123 + 456) ...'
			// Evaluate the operation and set the result as the first number in the next operation.
			State:evaluateBracketOperationState {
				do (
					assign :leftBracket to #null,
					assign :hasBracket to #null
				);
				goto finally State:evalulateCompleteState;
				do (
					Function:init2,
					assign :number to (call :operation on #Calculator with (:number, :number2)),
					append :number to #term of :expression2
				);
				case :anything goto State:appendRestState;
			}
				
			// '123 + 456 + ...'
			// Evaluate the operation and set the result as the first number in the next operation.
			// Push the next operation back onto the processing stack and process it.
			State:evaluateOperationState {
				do (
					assign :number to (call :operation on #Calculator with (:number, :number2))
				);
				push :operation2;
				goto State:numberState;
			}
			
			// '123 + 456 / ...'
			// The next operation takes precedence, so append the current operation to the expression, and evaluate the next operation and append the rest.
			State:appendOperationState {
				do (
					Function:init2,
					append :number to #term of :expression2,
					append :operation to #term of :expression2,
					assign :number to :number2
				);
				push :operation2;
				goto State:number2State;
			}
		
			// Check that the recursive expression has been defined.
			Function:init2 {
				if not (:expression2, #null)
					then return;
				assign :expression2 to (new #expression);
			}
			
			// '2 + 2/2 - ...'
			// Append the rest of the expression and recursively evaluate it.
			State:appendRestState {
				do (
					Function:init2,
					append :anything to #term of :expression2
				);
				case :anything goto State:appendRestState;
				
				goto finally State:evaluateState with :expression2;
			}
		}
		
		// '2  + 2/123...'
		// If something could not be evaluated because of precedence, then evaluate just the next operation, then append everything else and re-evaluate.
		State:number2State {
			case :rightBracket goto State:evaluateBracketOperationState;
			case :multiplyDivideOperation as :operation goto State:multiplyDivideOperation2State;
			case :powerOperation as :operation goto State:powerOperation2State;
			
			// '123 *...'
			State:multiplyDivideOperation2State {
				case :number2 goto State:multiplyDivide2Number2State;
				do (
					Function:appendOperation
				);
				case :function goto State:function2State;
				case :leftBracket goto State:leftBracketState;
			}
			
			// '123^...'
			State:powerOperation2State {
				case :number2 goto State:power2Number2State;
				do (
					Function:appendOperation
				);
				case :function goto State:function2State;
				case :leftBracket goto State:leftBracketState;
			}
			
			// '123 * 456...'
			State:multiplyDivide2Number2State {
				case :rightBracket goto State:evaluateBracketOperationState;
				case :plusMinusOperation as :operation2 goto State:evaluateOperation2State;
				case :multiplyDivideOperation as :operation2 goto State:evaluateOperation2State;
				case :powerOperation as :operation2 goto State:appendOperationState;
								
				goto finally State:evalulateCompleteState;
			}
			
			// '123^456...'
			State:power2Number2State {
				case :rightBracket goto State:evaluateBracketOperationState;
				case :plusMinusOperation as :operation2 goto State:evaluateOperation2State;
				case :multiplyDivideOperation as :operation2 goto State:evaluateOperation2State;
				case :powerOperation as :operation2 goto State:evaluateOperation2State;

				goto finally State:evalulateCompleteState;
			}
				
			// '123 + 456 + ...'
			State:evaluateOperation2State {
				do (
					assign :number to (call :operation on #Calculator with (:number, :number2)),
					append :number to #term of :expression2,
					append :operation2 to #term of :expression2
				);
				case :anything goto State:appendRestState;
			}
		}
		
		:function2 {
			set #instantiation to #function;
		}
		
		// 'sqrt...'
		State:functionState {
			case :number goto State:evalFunctionState;
			case :function2 goto State:appendFunctionState;
			do (
				Function:appendFunction
			);
			case :leftBracket goto State:leftBracketState;
				
			// 'sqrt 2...'
			State:evalFunctionState {
				do (
					assign :number to (call :function on #Calculator with :number)
				);
				case :rightBracket goto State:numberFinallyState;
				case :plusMinusOperation as :operation goto State:plusMinusOperationState;
				case :multiplyDivideOperation as :operation goto State:multiplyDivideOperationState;
				case :powerOperation as :operation goto State:powerOperationState;
				
				do (
					assign :result to :number,
					if not (:expression2, #null)
						then (append :result to #term of :expression2)
				);
				
				goto finally State:evaluateState with :expression2;
			}
			
			Function:appendFunction {
				Function:init2;
				if not (:leftBracket, #null)
					then (do (
						append :leftBracket to #term of :expression2,
						assign :leftBracket to #null,
						assign :hasBracket to #null));
				append :function to #term of :expression2;
				assign :function to :function2;
			}
			
			// 'sqrt sqrt 2...'
			State:appendFunctionState {
				do (
					Function:appendFunction
				);
				case :number goto State:evalFunction2State;
				case :function2 goto State:appendFunctionState;
				case :leftBracket goto State:leftBracketState;
			}
			
			// 'sqrt sqrt 2...'
			State:evalFunction2State {
				do (
					assign :number to (call :function on #Calculator with :number),
					append :number to #term of :expression2
				);
				case :anything goto State:appendRestState;
				
				goto finally State:evaluateState with :expression2;
			}
		}
		
		// '2 + sqrt...'
		// Evaluate the function, but then append the rest and recurse.
		State:function2State {
			case :number goto State:evalFunction2State;
			case :function2 goto State:appendFunctionState;
			
			do (
				Function:appendFunction
			);
				
			case :leftBracket goto State:leftBracketState;
			
			// '2 + sqrt 2...'
			State:evalFunction2State {
				do (
					assign :number to (call :function on #Calculator with :number),
					append :number to #term of :expression2
				);
				case :anything goto State:appendRestState;
				
				goto finally State:evaluateState with :expression2;
			}
		}
	}
}


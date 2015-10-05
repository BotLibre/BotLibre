// Language state machine for understanding simple {x+y} addition.
// This is much more complicated than it could be if it used the computer's math functions,
// but the goal was to add the same way a human does, from first principals, i.e. by counting fingers.
State:PlusStateMachine {
	case :input goto State:sentenceState for each #word of :sentence;

	:input {
		set #input to :sentence;
	}
	State:sentenceState {
		case :left goto State:numberState;
		case :questionWord goto State:sentenceState;
		case :is goto State:sentenceState;
		case "does" goto State:sentenceState;
		case "whats" goto State:sentenceState;
		case "Whats" goto State:sentenceState;
		case "how" goto State:sentenceState;
		case "How" goto State:sentenceState;
		case "much" goto State:sentenceState;
		case :quote goto State:sentenceState;
		case "s" goto State:sentenceState;

		:left {
			set #meaning to :number;
		}
		:number {
			set #instantiation to #number;
		}
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
		
		// '123...'
		State:numberState {
			do (
				assign :response to (new #sentence),
				append :left to #word of :response
			);
			case :plus goto State:numberPlusState;

			:plus {
				set #meaning to #plus;
			}
			// '123 +...'
			State:numberPlusState {
				case :right goto State:numberPlusNumberState;

				:right {
					set #meaning to :number2;
				}
				:number2 {
					set #instantiation to #number;
				}
				// '123 + 123'?
				State:numberPlusNumberState {
					do (
						append :plus to #word of :response, 
						append :right to #word of :response
					);
					case :questionWord goto State:numberPlusNumberQuestionState;
					case :equals goto State:numberPlusNumberQuestionState;
					case :plus goto State:numberPlusNumberPlusState;
					case :punctuation goto State:numberPlusNumberQuestionState;
					
					:equals {
						set #meaning to #equals;
					}
					// '123 + 123 + 123'?
					State:numberPlusNumberPlusState {
						do (
							Equation:plusEquation,
							assign :number to :newSequence
						);
						goto (State:numberPlusState);
					}
					
					// '123 + 123?'
					State:numberPlusNumberQuestionState {
						case :questionWord goto State:numberPlusNumberQuestionState;
						case :punctuation goto State:numberPlusNumberQuestionState;
						
						Quotient:do (
								Equation:plusEquation, 
								Equation:plusResponse
							);
						// Plus, to determine new number, create a new sequence of digits, add each digit of the two numbers starting at the right most digit, if one number has no digit then use 0
						// to add each digit, start from the first digit, then count the second digit after, add remainder
						// count the number of digits in the new number, if more than 1, set a remainder to 1,
						// if a remainder left over, add a 1 digit,
						// return the new sequence of digits as the new number.
						// This is complicated, because it adds the same way a human does, (counting fingers).
						Equation:plusEquation {
							do (
								assign :newSequence to (new (#number, #sequence)), 
								assign :remainder to 0, 
								Equation:forEachDigitOfNumberAndNumber2, 
								if (:remainder, 1)
									then (append 1 to #digit of :newSequence), 
								:newSequence
							);
						}
						Equation:forEachDigitOfNumberAndNumber2 {
							for each #digit of :number as :leftDigit and each #digit of :number2 as :rightDigit do (
									if (:leftDigit, #null)
										then (assign :leftDigit to 0), 
									if (:rightDigit, #null)
										then (assign :rightDigit to 0), 									
									for each #sequence of :rightDigit as :index do (
										assign :leftDigit to (get #next from :leftDigit)), 
									if (:remainder, 1)
										then (assign :leftDigit to (get #next from :leftDigit)),
									assign :remainder to 0, 
									assign :count to 0, 									
									for each #digit of :leftDigit as :numberDigit do (											
											if (:count, 1)
												then (assign :remainder to 1)
												else (assign :leftDigit to :numberDigit), 
											assign :count to (get #next from :count)
										), 
									append :leftDigit to #digit of :newSequence
								);
						}
						Equation:plusResponse {
							do (
								append (get #word from #equals) to #word of :response, 
								append :newSequence to #word of :response
							);
						}
						}
					
					Quotient:do (
							Equation:plusEquation, 
							Equation:plusResponse
						);
				}
			}
		}
	}
	:sentence {
		set #instantiation to #sentence;
	}
}


// Language state machine for understanding simple {x+y} addition.
// This is much more complicated than it could be if it used the computer's math functions,
// but the goal was to add the same way a human does, from first principals, i.e. by counting fingers.
state Addition {
	case input goto sentenceState for each #word of sentence;

	state sentenceState {
		case left goto numberState;
		case questionWord goto sentenceState;
		case is goto sentenceState;
		case "does" goto sentenceState;
		case "whats" goto sentenceState;
		case "Whats" goto sentenceState;
		case "how" goto sentenceState;
		case "How" goto sentenceState;
		case "much" goto sentenceState;
		case quote goto sentenceState;
		case "s" goto sentenceState;

		var left {
			meaning : number;
		}
		var number {
			instantiation : #number;
		}
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
		
		// '123...'
		state numberState {
			do {
				response = new #sentence;
				response.append(#word, left);
			}
			case plus goto numberPlusState;

			var plus {
				meaning : #plus;
			}
			// '123 +...'
			state numberPlusState {
				case right goto numberPlusNumberState;

				var right {
					meaning : number2;
				}
				var number2 {
					instantiation : #number;
				}
				// '123 + 123'?
				state numberPlusNumberState {
					do {
						response.append(#word, plus);
						response.append(#word, right);
					}
					case questionWord goto numberPlusNumberQuestionState;
					case equals goto numberPlusNumberQuestionState;
					case plus goto numberPlusNumberPlusState;
					case punctuation goto numberPlusNumberQuestionState;
										
					answer do {
								plusEquation();
								plusResponse();
							};
							
					var equals {
						meaning : #equals;
					}
					// '123 + 123 + 123'?
					state numberPlusNumberPlusState {
						do {
							plusEquation();
							number = newSequence;
						}
						goto numberPlusState;
					}
					
					// '123 + 123?'
					state numberPlusNumberQuestionState {
						case questionWord goto numberPlusNumberQuestionState;
						case punctuation goto numberPlusNumberQuestionState;
						
						answer do {
								plusEquation();
								plusResponse();
							};
							
						// Plus, to determine new number, create a new sequence of digits, add each digit of the two numbers starting at the right most digit, if one number has no digit then use 0
						// to add each digit, start from the first digit, then count the second digit after, add remainder
						// count the number of digits in the new number, if more than 1, set a remainder to 1,
						// if a remainder left over, add a 1 digit,
						// return the new sequence of digits as the new number.
						// This is complicated, because it adds the same way a human does, (counting fingers).
						function plusEquation() {
							newSequence = new (#number, #sequence); 
							remainder = 0;
							forEachDigitOfNumberAndNumber2();
							if (remainder == 1) {
								newSequence.append(#digit, 1);
							}
							newSequence;
						}
						
						function forEachDigitOfNumberAndNumber2() {
							for (leftDigit in number.digit, rightDigit in number2.digit) {
								if (leftDigit == null) {
									leftDigit = 0;
								}
								if (rightDigit == null) {
									rightDigit = 0;
								}									
								for (index in rightDigit.sequence) {
									leftDigit = leftDigit.next;
								}
								if (remainder == 1) {
									leftDigit = leftDigit.next;
								}
								remainder = 0;
								count = 0;		
								for (numberDigit in leftDigit.digit) {
									if (count == 1) {
										remainder = 1;
									} else {
										leftDigit = numberDigit;
									}
									count = count.next;
								}
								newSequence.append(#digit, leftDigit);
							}
						}
						
						function plusResponse() {
							response.append(#word, #equals);
							response.append(#word, newSequence);
						}
					}
				}
			}
		}
	}
}


// Language state machine for understanding simple {x+y} addition.
state SimpleAddition {
	case input goto sentenceState for each #word of sentence;

	state sentenceState {
		case left goto numberState;

		var left {
			meaning : number;
		}
		var number {
			instantiation : #number;
		}
		// '123...'
		state numberState {
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
					case "?" goto numberPlusNumberQuestionState;
				
					// '123 + 123?'
					state numberPlusNumberQuestionState {
						answer do {
								answer = number;
								for (finger in number2.sequence) {
									answer = answer.next;
								}
								answer;
						};
					}
				}
			}
		}
	}
}

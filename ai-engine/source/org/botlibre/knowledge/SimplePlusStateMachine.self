// Language state machine for understanding simple {x+y} addition.
State:SimplePlusStateMachine {
	case :input goto State:sentenceState for each #word of :sentence;

	:input {
		set #input to :sentence;
	}
	:sentence {
		set #instantiation to #sentence;
	}
	State:sentenceState {
		case :left goto State:numberState;

		:left {
			set #meaning to :number;
		}
		:number {
			set #instantiation to #number;
		}
		// '123...'
		State:numberState {
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
					// '123 + 123?'
					State:numberPlusNumberQuestionState {
						Quotient:do (
								assign :answer to :number,
								for each #sequence of :number2 as :finger do (
									assign :answer to (get #next from :answer)
								),
								:answer
						);
					}
				}
			}
		}
	}
}

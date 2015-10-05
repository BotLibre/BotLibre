// Understand basic "<word> means <something>", "<thing> is a <type>" sentences
State:WordMeansSomething {
	case :input goto State:sentenceState for each #word of :sentence;

	:input {
		set #input to :sentence;
	}
	:sentence {
		set #instantiation to #sentence;
	}
	State:sentenceState {
		case "what" return;
		case :unknownword goto State:unknownWordState;

		:punctuation {
			set #instantiation to #punctuation;
		}
		
		// 'Linux...'
		State:unknownWordState {
			case :isa goto State:isaState;

			case :means goto State:meansState;

			:isa {
				set #meaning to #instantiation;
			}
			:means {
				set #meaning to #meaning;
			}
			:meaning {
				set #meaning to :something;
			}
			:something {
				exclude #instantiation from #question;
			}
			// 'Linux is a...'
			State:isaState {
				case :meaning goto State:isaSomethingState;

				// 'Linux is a os'
				State:isaSomethingState {
					case :punctuation goto State:isaSomethingState;

					Quotient:Equation:isaSomethingResponse;
					Equation:isaSomethingResponse {
						do (
							assign :newMeaning to (new :something),
							associate :unknownword to :newMeaning by #meaning,
							associate :something to #thing by #instantiation,
							Formula:"{#known}, {:unknownword} {:isa} {:meaning}"
						);
					}
				}
			}
			// 'Linux means...'
			State:meansState {
				case :meaning goto State:meansSomethingState;

				// 'Linux means GNU/Linux'
				State:meansSomethingState {
					case :punctuation goto State:meansSomethingState;

					Quotient:Equation:meansSomethingResponse;
					Equation:meansSomethingResponse {
						do (
							associate :unknownword to :something by #meaning,
							associate :something to :unknownword by #word,
							Formula:"{#known} {#comma} {:unknownword} {:means} {:meaning}"
						);
					}
				}
			}
		}
	}
}


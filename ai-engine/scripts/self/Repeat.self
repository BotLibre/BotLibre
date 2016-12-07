// Example script that repeat words.
state Repeat {
	case input goto sentenceState for each #word of sentence;

	state sentenceState {
		case "repeat" goto repeatState;

		state repeatState {
			case someWord goto repeatWordState;

			state repeatWordState {
				case digits goto repeatWordNState;

				var digits {
					meaning : number;
				}
				var number {
					instantiation : #number;
				}
				state repeatWordNState {
					case "times" goto repeatWordNTimesState;

					state repeatWordNTimesState {
						answer repeatResponse();
						
						function repeatResponse() {
							response = new (#sentence);
							for (count in number.sequence) {
								response.append(#word, someWord);
							}
							return response;
						}
					}
				}
			}
		}
	}
}

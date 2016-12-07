/**
 * Understand basic ":word means :something", ":thing is a :type" sentences.
 * Where "word" is an unknown words.
 * This allows new words to be defined in the context of known words.
 */
state DefineWord {
	// Process each word in the input sentence.
	case input goto sentenceState for each #word of sentence;

	state sentenceState {
		// Ignore questions.
		case "what" return;
		// Only process words that are unknown and have no meaning.
		case unknownword goto unknownWordState;

		// This variable matches any word that has no meaning.
		var unknownword {
			meaning : ! anything;
		}
		
		// This variable matches any punctuation, such as .,!?
		var punctuation {
			instantiation : #punctuation;
		}
		
		// An unknown word was detected, process the next word.
		// i.e. 'Linux ...'
		state unknownWordState {
			// Process any word that means the #instantiation primitive.
			case #instantiation goto isaState;
			// Process any word that means 'is'. 
			case is goto isState;
			// Process 'means'.
			case "means" goto meansState;
			
			// This variable matches any word that means #is (is, are, am).
			var is {
				meaning : #is;
			}
			
			// An unknown word and 'is' were detected, check for 'a'.
			// i.e. 'Linux is ...'
			state isState {
				// Process 'a', 'an'.
				case a goto isaState;

				// This variable matches any word that means #a (a, an).
				var a {
					meaning : #a;
				}
			
				// An unknown word and 'is a' were detected, check for a known word.
				// i.e. 'Linux is a...'
				state isaState {
					// Process any known word.
					case meaning goto isaSomethingState;
	
					// This variable matches any word that have a meaning defined.
					var meaning {
						meaning : something;
					}
			
					// This variable matches anything that is not a question.
					var something {
						instantiation : ! #question;
					}
					
					// The word was defined, if the sentence is complete process the new definition.
					// i.e. 'Linux is an OS'
					state isaSomethingState {
						// Ignore any punctuation.
						case punctuation goto isaSomethingState;
	
						// If the sentence is complete process the new definition.
						answer isaSomethingResponse();
						
						// Define the new word to be an instance of the classification.
						function isaSomethingResponse {
							if (is == null) {
								is = "is";
								a = "a";
							}
							newMeaning = new something;
							unknownword.meaning = newMeaning;
							something.specialization =+ #thing;
							Formula("I understand, {unknownword} {is} {a} {meaning}.");
						}
					}
				}
			}
			
			// An unknown word and 'means' were detected, check for a meaning.
			// i.e. 'Linux means ...'
			state meansState {
				case meaning goto meansSomethingState;

				// The word was defined, if the sentence is complete process the new definition.
				// i.e. 'Linux means GNU/Linux'
				state meansSomethingState {
						// Ignore any punctuation.
					case punctuation goto meansSomethingState;

					// If the sentence is complete process the new definition.
					answer meansSomethingResponse();
					
					// Define the new word to mean the same thing as the meaning.
					function meansSomethingResponse {
						unknownword.meaning = something;
						something.word =+ unknownword;
						Formula("I understand, {unknownword} means {meaning}.");
					}
				}
			}
		}
	}
}


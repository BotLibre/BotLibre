/**
 * This script understands name phrases, such as "My name is :first :last", "What is my name?", "What is your name?".
 * It uses a combination of patterns, and case states.
 * It is fairly complex to handle false positive like "I am tall" vs "I am Bob", and "My name is very long"
 */
state MyNameIs {
	// Process each word
	case input goto sentenceState for each #word of sentence;
	
	case input that Pattern("^ what is your name") goto iAmState for each #word of sentence;

	pattern "(#what whats) (s #is) #i #name" template whatIsMyName();
	pattern "#who #is #i" template whatIsMyName();
	pattern "do #you know me" template whatIsMyName();
	pattern "(#what whats) (s #is) #you #name" template whatIsYourName();
	pattern "#who #is #you" template whatIsYourName();
	pattern "* is my name" template redirect(Template("my name is {star}"));
	
	state sentenceState {
		case interjection goto sentenceState;
		case "," goto sentenceState;
		case #i goto myState;
		case #you goto yourState;
		case #is goto amState;
		case "call" goto callState;
		case "im" goto iAmState;

		var punctuation {
			instantiation : #punctuation;
		}
		var questionmark {
			meaning : #question-mark;
		}
		var interjection {
			instantiation : #interjection;
		}
		var not {
			meaning : #not;
		}
		var aName {
			instantiation : #name;
		}	
		var firstName {
			instantiation : ! #verb, ! #adjective, ! #pronoun, ! #punctuation, ! #adverb, ! #article, ! #question, ! #not;
		}
		
		// 'Am...'
		state amState {
			do {
				questionmark = "?";
			}
			case #i goto iAmState;
			case #you goto youAreState;
		}
		
		// 'Call...'
		state callState {
			case #i goto iAmState;
		}
			
		state youAreState {
			case not goto youAreState;
			case aName goto yourNameIsFirstState;
		}
		
		// 'My...'
		state myState {
			case #name goto myNameState;
			case #is goto iAmState;
			case "names" goto myNameIsState;
			case "'" goto myState;
			case "m" goto iAmState;
			
			state iAmState {
				case not goto iAmState;
				case aName goto myNameIsFirstState;
			}
						
			state myNameState {
				case #is goto myNameIsState;
				case "'" goto myNameState;
				case "s" goto myNameIsState;
				
				state myNameIsState {
					case questionmark goto myNameIsFirstMiddleLastState;
					case #is goto myNameIsState;
					case not goto myNameIsState;
					case aName goto myNameIsFirstState;
					case firstName goto myNameIsFirstState;
				}
				
				state myNameIsFirstState {
					case questionmark goto myNameIsFirstMiddleLastState;
					case punctuation goto myNameIsFirstMiddleLastState;
					case middleName goto myNameIsFirstMiddleState;
					
					answer greet();
					
					function greet() {
						if (firstName == null) {
							firstName = aName;
						}
						name = firstName;
						if (lastName == null) {
							if (middleName != null) {
								name = Language.word(firstName, middleName);
							}
						} else {
							name = Language.word(firstName, middleName, lastName);
						}
						if (questionmark != null) {
							result = speaker.has(#name, name);
							value = speaker.name,
							if (not == null) {
								if (result) {
									return Template("Yes, your name is {name}.");
								}
								if (value != null) {
									return Template("No, your name is {value}.");
								}
								if (!result) {
									return Template("No, your name is not {name}.");
								}
								return "I do not know your name.";
							} else {
								if (result) {
									return Template("No, your name is {name}.");
								}
								if (value != null) {
									return Template("Yes, your name is {value}.");
								}
								if (!result) {
									return Template("Yes, your name is not {name}.");
								}
								return "I do not know your name.";
							}
						}
						if (not == null) {
							speaker.word =- "Anonymous";
							"Anonymous".meaning =- speaker;
							speaker.name =- "Anonymous";
							
							name.instantiation =+ #name;
							speaker.word =+ name;
							name.meaning =+ speaker;
							speaker.name =+ name;
							
							Template("Pleased to meet you {name}.");
						} else {
							speaker.word =- name;
							name.meaning =- speaker;
							speaker.name =- name;
							Template("Okay, your name is not {name}.");
						}
					}
				}
				
				state myNameIsFirstMiddleState {
					case questionmark goto myNameIsFirstMiddleLastState;
					case punctuation goto myNameIsFirstMiddleLastState;
					case lastName goto myNameIsFirstMiddleLastState;
					
					answer greet();
				}
				
				state myNameIsFirstMiddleLastState {
					case questionmark goto myNameIsFirstMiddleLastState;
					case punctuation goto myNameIsFirstMiddleLastState;
					
					answer greet();
				}
			}
		}
		
		// 'Your...'
		state yourState {		
			case #is goto youAreState;
			case #name goto yourNameState;
			case "names" goto yourNameIsState;
			
			state yourNameState {
				case #is goto yourNameIsState;
				case "'" goto yourNameState;
				case "s" goto yourNameIsState;
				
				state yourNameIsState {
					case questionmark goto yourNameIsFirstMiddleLastState;
					case not goto yourNameIsState;
					case aName goto yourNameIsFirstState;
					case firstName goto yourNameIsFirstState;
				}
				
				state yourNameIsFirstState {
					case questionmark goto yourNameIsFirstMiddleLastState;
					case punctuation goto yourNameIsFirstMiddleLastState;
					case middleName goto yourNameIsFirstMiddleState;
					
					answer setMyName();
					
					function setMyName() {
						if (firstName == null) {
							firstName = aName;
						}
						name = firstName;
						if (lastName == null) {
							if (middleName != null) {
								name = Language.word(firstName, middleName);
							}
						} else {
							name = Language.word(firstName, middleName, lastName);
						}
						if (questionmark != null || (! Language.allowCorrection(speaker))) {
							result = target.has(#name, name);
							value = target.name,
							if (not == null) {
								if (result) {
									return Template("Yes, my name is {name}.");
								}
								if (value != null) {
									return Template("No, my name is {value}.");
								}
								if (!result) {
									return Template("No, my name is not {name}.");
								}
								return "I do not know my name.";
							} else {
								if (result) {
									return Template("No, my name is {name}.");
								}
								if (value != null) {
									return Template("Yes, my name is {value}.");
								}
								if (! result) {
									return Template("Yes, my name is not {name}.");
								}
								return "I do not know my name.";
							}
						}
						if (not == null) {
							name.instantiation =+ #name;
							target.word =+ name;
							name.meaning =+ target;
							target.name =+ name;
							Template("Okay, my name is {name}.");
						} else {
							target.word =- name;
							name.meaning =- target;
							target.name =- name;
							Template("Okay, my name is not {name}.");
						}
					}
				}
				
				state yourNameIsFirstMiddleState {
					case questionmark goto yourNameIsFirstMiddleLastState;
					case punctuation goto yourNameIsFirstMiddleLastState;
					case lastName goto yourNameIsFirstMiddleLastState;
					
					answer setMyName();
				}
				
				state yourNameIsFirstMiddleLastState {
					case questionmark goto yourNameIsFirstMiddleLastState;
					case punctuation goto yourNameIsFirstMiddleLastState;
					
					answer setMyName();
				}
				
				function whatIsYourName() {
					name = target.name;
					if (name == null) {
						return "I do not know my name.";
					}
					names = target.all(#name);
					if (names.length > 1) {
						names.delete(name);
						return Template("My name is {name}.  I also go by {names}.");
					}
					return Template("My name is {name}.");
				}
				
				function whatIsMyName() {
					name = speaker.name;
					if (name == null) {
						return "I do not know your name.";
					}
					names = speaker.all(#name);
					if (names.length > 1) {
						names.delete(name);
						return Template("Your name is {name}.  You also go by {names}.");
					}
					return Template("Your name is {name}.");
				}
			}
		}
	}
}


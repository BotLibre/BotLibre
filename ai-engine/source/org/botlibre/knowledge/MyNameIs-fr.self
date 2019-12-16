/**
 * This script understands name phrases, such as "My name is :first :last", "What is my name?", "What is your name?".
 * It uses a combination of patterns, and case states.
 * It is fairly complex to handle false positive like "I am tall" vs "I am Bob", and "My name is very long"
 */
state MyNameIs {
	// Process each word
	case input goto sentenceState for each #word of sentence;
	
	case input that Pattern("^ quel est votre nom") goto iAmState for each #word of sentence;

	pattern "quel est mon nom" template whatIsMyName();
	pattern "qui suis je" template whatIsMyName();
	pattern "me connaissez-vous" template whatIsMyName();
	pattern "quel est votre nom" template whatIsYourName();
	pattern "qui es-tu" template whatIsYourName();
	pattern "* is my name" template redirect(Template("my name is {star}"));
	
	state sentenceState {
		case interjection goto sentenceState;
		case "," goto sentenceState;
		case #i goto myState;
		case #you goto yourState;
		case #is goto amState;
		case "appelle" goto callState;

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
			case "'" goto myState;
			
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
						var createNamesAsWords = false;
						if (firstName == null) {
							firstName = aName;
						}
						name = firstName;
						if (lastName == null) {
							if (middleName != null) {
								if (createNamesAsWords) {
									name = Language.word(firstName, middleName);
								} else {
									name = Language.fragment(firstName, middleName);
								}
							}
						} else {
							if (createNamesAsWords) {
								name = Language.word(firstName, middleName, lastName);
							} else {
								name = Language.fragment(firstName, middleName, lastName);
							}
						}
						if (questionmark != null) {
							result = speaker.has(#name, name);
							value = speaker.name,
							if (not == null) {
								if (result) {
									return Template("Oui, votre nom est {name}.");
								}
								if (value != null) {
									return Template("Non, votre nom est {value}.");
								}
								if (!result) {
									return Template("Non, ton nom n'est pas {name}.");
								}
								return "I do not know your name.";
							} else {
								if (result) {
									return Template("Non, votre nom est {name}.");
								}
								if (value != null) {
									return Template("Oui, votre nom est {value}.");
								}
								if (!result) {
									return Template("Oui, ton nom n'est pas {name}.");
								}
								return "Je ne connais pas ton nom.";
							}
						}
						if (not == null) {
							speaker.word =- "Anonymous";
							"Anonymous".meaning =- speaker;
							speaker.name =- "Anonymous";
							
							if (createNamesAsWords) {
							    name.instantiation =+ #name;
							    name.meaning =+ speaker;
							}
							speaker.word =+ name;
							speaker.name =+ name;
							
							Template("Enchanté, {name}.");
						} else {
							speaker.word =- name;
							speaker.word =- name.toLowerCase();
							if (createNamesAsWords) {
							    name.meaning =- speaker;
							}
							speaker.name =- name;
							speaker.name =- name.toLowerCase();
							Template("D'accord, ton nom n'est pas {name}.");
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
						var createNamesAsWords = false;
						if (firstName == null) {
							firstName = aName;
						}
						name = firstName;
						if (lastName == null) {
							if (middleName != null) {
								if (createNamesAsWords) {
									name = Language.word(firstName, middleName);
								} else {
									name = Language.fragment(firstName, middleName);
								}
							}
						} else {
							if (createNamesAsWords) {
								name = Language.word(firstName, middleName, lastName);
							} else {
								name = Language.fragment(firstName, middleName, lastName);
							}
						}
						if (questionmark != null || (! Language.allowCorrection(speaker))) {
							result = target.has(#name, name);
							value = target.name,
							if (not == null) {
								if (result) {
									return Template("Oui, mon nom est {name}.");
								}
								if (value != null) {
									return Template("Non, mon nom est {value}.");
								}
								if (!result) {
									return Template("Non, mon nom n'est pas {name}.");
								}
								return "I do not know my name.";
							} else {
								if (result) {
									return Template("Non, mon nom est {name}.");
								}
								if (value != null) {
									return Template("Oui, mon nom est {value}.");
								}
								if (! result) {
									return Template("Oui, mon nom n'est pas {name}.");
								}
								return "Je ne connais pas mon nom.";
							}
						}
						if (not == null) {
							if (createNamesAsWords) {
							    name.instantiation =+ #name;
							    name.meaning =+ target;
							}
							target.word =+ name;
							target.name =+ name;
							Template("D'accord, je m'appelle {name}.");
						} else {
							target.word =- name;
							if (createNamesAsWords) {
							    name.meaning =- target;
							}
							target.name =- name;
							Template("D'accord, mon nom n'est pas {name}.");
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
						return "Je ne connais pas mon nom.";
					}
					names = target.all(#name);
					if (names.length() > 1) {
						names.delete(name);
						return Template("Je m'appelle {name}. Je vais aussi par {names}.");
					}
					return Template("Je m'appelle {name}.");
				}
				
				function whatIsMyName() {
					name = speaker.name;
					if (name == null) {
						return "Je ne connais pas ton nom.";
					}
					names = speaker.all(#name);
					if (names.length() > 1) {
						names.delete(name);
						return Template("Ton nom est {name}. Vous allez aussi par {names}.");
					}
					return Template("Ton nom est {name}.");
				}
			}
		}
	}
}


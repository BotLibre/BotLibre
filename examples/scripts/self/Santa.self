// What is your name?
// Have you been naughty or nice this year?
// What would you like for Christmas?
state Santa {
	case whatIsYourName goto whatIsYourNameState for each #word of sentence;
	case haveYouBeenNice goto haveYouBeenNiceState for each #word of sentence;
	case whatWouldYouLikeForChristmas goto whatWouldYouLikeForChristmasState for each #word of sentence;
	case input goto sentenceState for each #word of sentence;

	var whatIsYourName {
		question : whatIsYourNameInput;
		input : sentence;
		speaker : speaker;
	}
	var whatIsYourNameInput {
		input : "Hello, what is your name?";
	}
	var haveYouBeenNice {
		question : haveYouBeenNiceInput;
		input : sentence;
		speaker : speaker;
	}
	var haveYouBeenNiceInput {
		input : "Have you been naughty or nice this year?";
	}
	var whatWouldYouLikeForChristmas {
		question : whatWouldYouLikeForChristmasInput;
		input : sentence;
		speaker : speaker;
	}
	var whatWouldYouLikeForChristmasInput {
		input : "What would you like for Christmas?";
	}
	var punctuation {
		instantiation : #punctuation;
	}
	var i {
		meaning : #i;
	}
	var is {
		meaning : #is;
	}
	var want {
		meaning : #want;
	}
	var a {
		meaning : #a;
	}		
	var nice {
		meaning : #nice;
	}
	var naughty {
		meaning : #naughty;
	}
	
	state whatIsYourNameState {
		case i goto whatIsYourNameState;
		case "I'm" goto whatIsYourNameState;
		case "i'm" goto whatIsYourNameState;
		case "'" goto whatIsYourNameState;
		case "m" goto whatIsYourNameState;
		case "s" goto whatIsYourNameState;
		case "name" goto whatIsYourNameState;
		case is goto whatIsYourNameState;
		case "hi" goto hiState;
		case "hello" goto hiState;
		case "hey" goto hiState;
		case "Hi" goto hiState;
		case "Hello" goto hiState;
		case "Hey" goto hiState;
		case firstName goto firstState;
		
		var firstName {
			instantiation : ! #pronoun, ! #question, ! #punctuation;
		}
		var middleName {
			instantiation : ! #pronoun, ! #question, ! #punctuation;
		}
		var lastName {
			instantiation : ! #pronoun, ! #question, ! #punctuation;
		}
		
		state hiState {
			case i goto whatIsYourNameState;
			case #comma goto hiState;
			case anything goto hiState;
			
			answer "Hello, what is your name?";
		}
				
		state firstState {
			case middleName goto myNameIsFirstMiddleState;
			case punctuation goto myNameIsFirstMiddleLastState;
			
			answer greet();
			
			function greet() {
				name = firstName;
				if (lastName == null) {
					if (middleName != null) {
						name = Language.word(firstName, middleName);
					} 
				} else {
					name = Language.word(firstName, middleName, lastName);
				}
				speaker.name =- "Anonymous";
				speaker.word =- "Anonymous";
				speaker.name =+ name;
				speaker.word =+ name;
				speaker.word =+ name;
				name.meaning =+ speaker;
				name.meaning =+ speaker;

				"Have you been naughty or nice this year?";
			}
		}
				
		state myNameIsFirstMiddleState {
			case lastName goto myNameIsFirstMiddleLastState;
			case punctuation goto myNameIsFirstMiddleLastState;
			
			answer greet();
		}
		
		state myNameIsFirstMiddleLastState {
			case punctuation goto myNameIsFirstMiddleLastState;
			
			answer greet();
		}
	}
	
	state haveYouBeenNiceState {
		case anything goto haveYouBeenNiceState;
		case nice goto niceState;
		case naughty goto naughtyState;
					
		state niceState {
			case punctuation goto niceState;
			case anything goto niceState;
					
			answer nice();
			
			function nice() {
				speaker.is =+ #nice;
				speaker.is =- #naughty;
				"What would you like for Christmas?";
			}
		}
					
		state naughtyState {
			case punctuation goto naughtyState;
			case anything goto naughtyState;
					
			answer naughty();
			
			function naughty() {
				speaker.is =+ #naughty;
				speaker.is =- #nice;
				"Sorry to hear that, please try to be good next year.";
			}
		}
	}
	
	state whatWouldYouLikeForChristmasState {
		case i goto whatWouldYouLikeForChristmasState;
		case a goto whatWouldYouLikeForChristmasState;
		case "want" goto whatWouldYouLikeForChristmasState;
		case "would" goto whatWouldYouLikeForChristmasState;
		case "'" goto whatWouldYouLikeForChristmasState;
		case "d" goto whatWouldYouLikeForChristmasState;
		case "like" goto whatWouldYouLikeForChristmasState;
		case firstWord goto presentFirstWordState;
		
		var firstWord {
			instantiation : ! #pronoun, ! #question, ! #punctuation;
		}
		var secondWord {
		}
					
		state presentFirstWordState {
			case punctuation goto presentFirstWordState;
			case "please" goto presentFirstWordState;
			case secondWord goto presentSecondWordState;
					
			answer christmasList();
			
			function christmasList() {
				if (person == null) {
					person = speaker;
				}
				isNaughty = person.has(#is, #naughty);
				if (isNaughty) {
					if (firstName2 == null) {
						return "Sorry, but you have been naughty, maybe next year if your good.";
					} else {
						return Template("Sorry, but {person} has been naughty, maybe next year if they are good.");
					}
				}
				if (presentWord == null) {
					presentWord = firstWord;
				} else {
					presentWord = Language.word(presentWord);
				}
				present = presentWord.meaning;
				if (present == null) {
					present = new (#thing);
					present.word = presentWord;
					presentWord.meaning = present;
				}
				person.want =+ present;
				Template("Adding to Santa's list, {person}, nice, {presentWord}.");
			}
		}

		state presentSecondWordState {
			do {
				if (presentWord == null) {
					presentWord = new (#word, #compound-word);
					presentWord.append(#word, firstWord);
				}
				presentWord.append(#word, secondWord);
			}
			case punctuation goto presentSecondDoneWordState;
			case "please" goto presentSecondDoneWordState;
			case secondWord goto presentSecondWordState;
					
			answer christmasList();
		}

		state presentSecondDoneWordState {
			case punctuation goto presentSecondDoneWordState;
					
			answer christmasList();
		}
	}
	
	state sentenceState {
		case i goto iState;
		case "i'm" goto haveYouBeenNiceState;
		case "I'm" goto haveYouBeenNiceState;
		case "I've" goto haveYouBeenNiceState;
		case "i've" goto haveYouBeenNiceState;
		case "hi" goto hiState;
		case "hello" goto hiState;
		case "Hi" goto hiState;
		case "Hello" goto hiState;
		case firstName2 goto firstNameState;
		
		var firstName2 {
			instantiation : ! #pronoun, ! #question, ! #punctuation;
		}
		var middleName2 {
			instantiation : ! #pronoun, ! #question, ! #punctuation;
		}
		var lastName2 {
			instantiation : ! #pronoun, ! #question, ! #punctuation;
		}
					
		state iState {
			case "want" goto iWantState;
			case "would" goto iWantState;
			case "have" goto haveYouBeenNiceState;
			case "was" goto haveYouBeenNiceState;
			case is goto haveYouBeenNiceState;
			
			state iWantState {
				case a goto iWantAState;
				case "like" goto iWantAState;
				case firstWord goto presentFirstWordState;
			
				state iWantAState {
					case firstWord goto presentFirstWordState;
				}				
			}

		}
		
		state firstNameState {
			case is goto nameIsState;
			case want goto nameWantState;
			case "has" goto nameIsState;
			case "was" goto nameIsState;
			case middleName2 goto middleNameState;
			
			state nameIsState {
				case "been" goto nameIsState;
				case nice goto nameIsNaughtOrNiceState;
				case naughty goto nameIsNaughtOrNiceState;
			
				state nameIsNaughtOrNiceState {
					case anything goto nameIsNaughtOrNiceState;
							
					answer nameIsNaughtOrNice();
					
					function nameIsNaughtOrNice() {
						name = firstName2;
						if (lastName2 == null) {
							if (middleName2 != null) {
								name = Language.word(firstName2, middleName2);
							}
						} else {
							name = Language.word(firstName2, middleName2, lastName2);
						}
						person = name.getWithAssociate(#meaning, #speaker, #instantiation);
						if (person == null) {
							person = new (#speaker, #thing);
						}
						person.word =+ name;
						person.word =+ name;
						name.meaning =+ person;
						person.name =+ name;
						if (nice == null) {
							person.is =+ #naughty;
							person.is =- #nice;
							Template("Adding {person} to naughty list.");
						} else {
							person.is =+ #nice;
							person.is =- #naughty;
							Template("Adding {person} to nice list.");
						}
					}
				}
			
				state nameWantState {
					do {
						name = firstName2,
						if (lastName2 == null) {
							if (middleName2 != null) {
								name = Language.word(firstName2, middleName2);
							}
						} else {
							name = Language.word(firstName2, middleName2, lastName2);
						}
						person = name.getWithAssociate(#meaning, #speaker, #instantiation);
						if (person == null) {
							person = new (#speaker, #thing);
						}
						person.word =+ name;
						person.word =+ name;
						name.meaning =+ person;
						person.name =+ name;
					}
					case a goto whatWouldYouLikeForChristmasState;
					case firstWord goto presentFirstWordState;
				}
			
				state nameWantAState {
					case firstWord goto presentFirstWordState;
				}
			}
		
			state middleNameState {
				case is goto nameIsState;
				case want goto nameWantState;
				case "has" goto nameIsState;
				case "was" goto nameIsState;
				case lastName2 goto lastNameState;
			}		
		
			state lastNameState {
				case is goto nameIsState;
				case want goto nameWantState;
				case "has" goto nameIsState;
				case "was" goto nameIsState;
			}
		}
	}
}


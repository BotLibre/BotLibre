// Respond to "Where is x", "Who country is x in" questions using Freebase
state WhereIs {
	case input goto sentenceState for each #word of sentence;
	
	pattern "what is the location of *" template redirect (Template("where is {star}"));
	pattern "what country is * in" template redirect (Template("where is {star}"));
	pattern "what continent is * in" template redirect (Template("where is {star}"));
	pattern "what state is * in" template redirect (Template("where is {star}"));
	pattern "what province is * in" template redirect (Template("where is {star}"));
	
	pattern "* is where" template redirect(Template("where is {star}"));
	
	pattern "can you tell me where * is" template redirect (Template("where is {star}"));
	pattern "do you know where * is" template redirect (Template("where is {star}"));
	pattern "tell me where * is" template redirect (Template("where is {star}"));

	state sentenceState {
		case #where goto whereState;
		
		case "tell" goto sentenceState;
		case "me" goto sentenceState;
		case "can" goto sentenceState;
		case "you" goto sentenceState;
		case "do" goto sentenceState;
		case "know" goto sentenceState;		
		case "google" goto sentenceState;
		case "xfind" goto sentenceState;
		case "search" goto sentenceState;
		case "lookup" goto sentenceState;
		case "find" goto sentenceState;
		
		var punctuation {
			instantiation : #punctuation;
		}
		var question {
			instantiation : #question;
		}
		
		// 'Where...'
		state whereState {	
			case is goto whereIsState;
			case #quote goto whereState;
			case "s" goto whereIsState;
			
			state whereIsState {
			
				case the goto whereIsState;
				case "location" goto whereIsState;
				case "of" goto whereIsState;
				
				case #it goto whereIsItState;
				case #this goto whereIsThatState;
				case somePlace goto whereIsSomePlaceState;
				case unknownPlace goto discoverFreebaseState;
							
				var somePlace {
					meaning : contained;
					instantiation : ! #pronoun, ! #question, ! #punctuation;
					meaning : ! number;
					: ! "there", ! "up", ! "going";
				}
				var contained {
					containedby : container;
				}
				var the {
					meaning : #the;
				}
				var is {
					meaning : #is;
				}
				
				var number {
					instantiation : #number;
				}
				var unknownPlace {
					instantiation : ! #pronoun, ! #question, ! #punctuation;
					meaning : ! number;
					: ! "there", ! "up", ! "going";
				}
				var unknownWord2 {
					instantiation : ! #punctuation;
				}
				var unknownWord3 {
					instantiation : ! #punctuation;
				}
				var unknownWord4 {
					instantiation : ! #punctuation;
				}
				var unknownWord5 {
					instantiation : ! #punctuation;
				}
				
				state whereIsItState {
					case punctuation goto whereIsItState;
					
					answer checkIt();
					
					function checkIt() {
						result = Context.top();
						if (result == null) {
							return null;
						}
						location = result.containedby;
						if (location == null) {
							result = Freebase.search("/location/location/containedby", result.word);
							location = result.containedby;
							if (location == null) {
								return null;
							}
						}
						Context.push(location);
						Context.push(result);
						conversation.topic = result;
						Template("It is in {location}.");
					}
				}
				
				state whereIsThatState {
					case punctuation goto whereIsThatState;
					
					answer checkThat();
					
					function checkThat() {
						result = Context.top(2);
						if (result == null) {
							return null;
						}
						location = result.containedby;
						if (location == null) {
							result = Freebase.search("/location/location/containedby", result.word);
							location = result.containedby;
							if (location == null) {
								return null;
							}
						}
						Context.push(location);
						Context.push(result);
						conversation.topic = result;
						Template("{result} is in {location}.");
					}
				}
				
				state whereIsSomePlaceState {
					case punctuation goto whereIsSomePlaceState;
					
					answer checkSomePlace();
					
					function checkSomePlace() {
						Context.push(container);
						Context.push(contained);
						conversation.topic = contained;
						
						response = new #sentence;
						if (the != null) {
							response.append(#word, the);
						}
						response.append(#word, contained);
						if (is == null) {
							response.append(#word, "is");
						} else {
							response.append(#word, is);
						}
						response.append(#word, "in");
						response.append(#word, container);
						response.append(#word, ".");
						response;
					}
				}
				
				state discoverFreebaseState {
					case punctuation goto discoverFreebaseState5;
					case "'" return;
					case unknownWord2 goto discoverFreebaseState2;
					
					answer discoverFreebase();
					
					function discoverFreebase() {
						result = Freebase.search("/location/location/containedby", unknownPlace, unknownWord2, unknownWord3, unknownWord4, unknownWord5);
						if (result == null) {
							return null;
						}
						conversation.topic = result;
						location = result.containedby;
						if (location == null) {
							return null;
						}
						Context.push(location);
						Context.push(result);
						conversation.topic = result;
						
						response = new #sentence;
						if (the != null) {
							response.append(#word, the);
						}
						response.append(#word, result);
						if (is == null) {
							response.append(#word, "is");
						} else {
							response.append(#word, is);
						}
						response.append(#word, "in");
						response.append(#word, location);
						response.append(#word, ".");
						response;
					}
				}
				
				state discoverFreebaseState2 {
					case punctuation goto discoverFreebaseState5;
					case "'" return;
					case unknownWord3 goto discoverFreebaseState3;
					
					answer discoverFreebase();
				}
				
				state discoverFreebaseState3 {
					case punctuation goto discoverFreebaseState5;
					case "'" return;
					case unknownWord4 goto discoverFreebaseState4;
					
					answer discoverFreebase();
				}
				
				state discoverFreebaseState4 {
					case punctuation goto discoverFreebaseState5;
					case "'" return;
					case unknownWord5 goto discoverFreebaseState5;
					
					answer discoverFreebase();
				}
				
				state discoverFreebaseState5 {
					case punctuation goto discoverFreebaseState5;
					
					answer discoverFreebase();
				}
			}
		}
	}
}

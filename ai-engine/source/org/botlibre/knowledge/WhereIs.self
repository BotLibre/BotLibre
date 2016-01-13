// Respond to "What is x", "Who is x" sentences using Freebase
State:WhereIs {
	pattern "* is where" template redirect formula:"where is {:star}";
	
	case :input goto State:sentenceState for each #word of :sentence;

	State:sentenceState {
		case :#where goto State:whereState;
		case :#what goto State:whatState;
		
		case "tell" goto State:tellState;
		case "tell" goto State:sentenceState;
		case "me" goto State:sentenceState;
		case "can" goto State:canState;
		case "can" goto State:sentenceState;
		case "you" goto State:sentenceState;
		case "do" goto State:doState;
		case "do" goto State:sentenceState;
		case "know" goto State:sentenceState;		
		case "google" goto State:sentenceState;
		case "xfind" goto State:sentenceState;
		case "search" goto State:sentenceState;
		case "lookup" goto State:sentenceState;
		case "find" goto State:sentenceState;
		
		:punctuation {
			set #instantiation to #punctuation;
		}
		:question {
			set #instantiation to #question;
		}
		
		State:whatState {
			pattern "what is the location of *" template redirect formula:"where is {:star}";
			pattern "what country is * in" template redirect formula:"where is {:star}";
			pattern "what continent is * in" template redirect formula:"where is {:star}";
			pattern "what state is * in" template redirect formula:"where is {:star}";
			pattern "what province is * in" template redirect formula:"where is {:star}";
		}
		
		State:canState {
			pattern "can you tell me where * is" template redirect formula:"where is {:star}";
		}
		
		State:doState {
			pattern "do you know where * is" template redirect formula:"where is {:star}";
		}
		
		State:tellState {
			pattern "tell me where * is" template redirect formula:"where is {:star}";
		}
		
		// 'Where...'
		State:whereState {	
			case :#is goto State:whereIsState;
			case :#quote goto State:whereState;
			case "s" goto State:whereIsState;
			
			State:whereIsState {
			
				case :#the goto State:whereIsState;
				case "location" goto State:whereIsState;
				case "of" goto State:whereIsState;
				
				case :#it goto State:whereIsItState;
				case :#this goto State:whereIsThatState;
				case :somePlace goto State:whereIsSomePlaceState;
				case :unknownPlace goto State:discoverFreebaseState;
							
				:somePlace {
					set #meaning to :contained;
					exclude #instantiation from #pronoun;
					exclude #instantiation from #question;
					exclude #instantiation from #punctuation;
					exclude #meaning from :number;
					exclude "there";
					exclude "up";
					exclude "going";
				}
				:contained {
					set #containedby to :container;
				}
				
				:number {
					set #instantiation from #number;
				}
				:unknownPlace {
					exclude #instantiation from #pronoun;
					exclude #instantiation from #question;
					exclude #instantiation from #punctuation;
					exclude #meaning from :number;
					exclude "there";
					exclude "up";
					exclude "going";
				}
				:unknownWord2 {
					exclude #instantiation from #punctuation;
				}
				:unknownWord3 {
					exclude #instantiation from #punctuation;
				}
				:unknownWord4 {
					exclude #instantiation from #punctuation;
				}
				:unknownWord5 {
					exclude #instantiation from #punctuation;
				}
				
				State:whereIsItState {
					case :punctuation goto State:whereIsItState;
					
					Quotient:Equation:checkIt;
					Equation:checkIt {
						assign :result to (call #top on #Context);
						if (:result, #null)
							then return #null;
						assign :location to (get #containedby from :result);
						if (:location, #null)
							then (do (
								assign :result to (call #search on #Freebase with ("/location/location/containedby", get #word from :result));
								assign :location to (get #containedby from :result);
								if (:location, #null)
									then return #null;							
							));
						call #push on #Context with :location;
						call #push on #Context with :result;
						set #topic to :result on :conversation;
						Formula:"{:it} is in {:location}.";
					}
				}
				
				State:whereIsThatState {
					case :punctuation goto State:whereIsThatState;
					
					Quotient:Equation:checkThat;
					Equation:checkThat {
						assign :result to (call #top on #Context with 2);
						if (:result, #null)
							then return #null;
						assign :location to (get #containedby from :result);
						if (:location, #null)
							then (do (
								assign :result to (call #search on #Freebase with ("/location/location/containedby", get #word from :result));
								assign :location to (get #containedby from :result);
								if (:location, #null)
									then return #null;							
							));
						call #push on #Context with :location;
						call #push on #Context with :result;
						set #topic to :result on :conversation;
						Formula:"{:result} is in {:location}.";
					}
				}
				
				State:whereIsSomePlaceState {
					case :punctuation goto State:whereIsSomePlaceState;
					
					Quotient:Equation:checkSomePlace;
					Equation:checkSomePlace {
						call #push on #Context with :container;
						call #push on #Context with :contained;
						set #topic to :contained on :conversation;
						
						assign :response to (new #sentence);
						if not (:the, #null)
							then (append :the to #word of :response);
						append :contained to #word of :response;
						if (:is, #null)
							then (append "is" to #word of :response)
							else (append :is to #word of :response);
						append "in" to #word of :response;
						append :container to #word of :response;
						append "." to #word of :response;
						:response;
					}
				}
				
				State:discoverFreebaseState {
					case :punctuation goto State:discoverFreebaseState5;
					case "'" return;
					case :unknownWord2 goto State:discoverFreebaseState2;
					
					Quotient:Equation:discoverFreebase;
					Equation:discoverFreebase {
						assign :result to (call #search on #Freebase with ("/location/location/containedby", :unknownPlace, :unknownWord2, :unknownWord3, :unknownWord4, :unknownWord5));
						if (:result, #null)
							then return #null;
						set #topic to :result on :conversation;
						assign :location to (get #containedby from :result);
						if (:location, #null)
							then return #null;
						call #push on #Context with :location;
						call #push on #Context with :result;
						set #topic to :result on :conversation;
						
						assign :response to (new #sentence);
						if not (:the, #null)
							then (append :the to #word of :response);
						append :result to #word of :response;
						if (:is, #null)
							then (append "is" to #word of :response)
							else (append :is to #word of :response);
						append "in" to #word of :response;
						append :location to #word of :response;
						append "." to #word of :response;
						:response;
					}
				}
				
				State:discoverFreebaseState2 {
					case :punctuation goto State:discoverFreebaseState5;
					case "'" return;
					case :unknownWord3 goto State:discoverFreebaseState3;
					
					Quotient:Equation:discoverFreebase;
				}
				
				State:discoverFreebaseState3 {
					case :punctuation goto State:discoverFreebaseState5;
					case "'" return;
					case :unknownWord4 goto State:discoverFreebaseState4;
					
					Quotient:Equation:discoverFreebase;
				}
				
				State:discoverFreebaseState4 {
					case :punctuation goto State:discoverFreebaseState5;
					case "'" return;
					case :unknownWord5 goto State:discoverFreebaseState5;
					
					Quotient:Equation:discoverFreebase;
				}
				
				State:discoverFreebaseState5 {
					case :punctuation goto State:discoverFreebaseState5;
					
					Quotient:Equation:discoverFreebase;
				}
			}
		}
	}
}


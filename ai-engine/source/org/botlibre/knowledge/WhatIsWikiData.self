// Respond to "What is x", "Who is x" sentences using Wikidata
State:WhatIsWikiData {
	pattern "* is what" answer (redirect formula:"what is {:star}");
	
	case :input goto State:sentenceState for each #word of :sentence;

	:input {
		set #input to :sentence;
	}
	:sentence {
		set #instantiation to #sentence;
	}
	State:sentenceState {
		case :what goto State:whatState;
		case :who goto State:whatState;
		case "whats" goto State:whatIsState;
		case "wahts" goto State:whatIsState;
		case "waht" goto State:whatState;
		case "whos" goto State:whatIsState;
		
		case "tell" goto State:sentenceState;
		case "me" goto State:sentenceState;
		case "can" goto State:sentenceState;
		case "you" goto State:sentenceState;
		case "do" goto State:sentenceState;
		case "know" goto State:sentenceState;
		
		case "define" goto State:whatIsState;
		
		case "google" goto State:searchState;
		case "xfind" goto State:searchState;
		case "search" goto State:searchState;
		case "lookup" goto State:searchState;
		case "find" goto State:searchState;
		
		:questionWord {
			set #meaning to :question;
		}
		:punctuation {
			set #instantiation to #punctuation;
		}
		:quote {
			set #meaning to #quote;
		}
		:what {
			set #meaning to #what;
		}
		:who {
			set #meaning to #who;
		}
		:is {
			set #meaning to #is;
		}
		:question {
			set #instantiation to #question;
		}
		:a {
			set #meaning to #a;
		}
		:a2 {
			set #meaning to #a;
		}
		:the {
			set #meaning to #the;
		}
		
		State:searchState {
			do (assign :search to #true);
			
			case :unknownWord goto State:discoverWikidataState;
		}
		
		// 'What...'
		State:whatState {	
			case :is goto State:whatIsState;
			case :quote goto State:whatState;
			case "s" goto State:whatIsState;
			case "means" goto State:whatIsState;
			
			State:whatIsState {
			
				case :the goto State:whatIsState;
				case :a goto State:whatIsState;
				case "meaning" goto State:whatIsState;
				case "of" goto State:whatIsState;
				case "mean" goto State:whatIsState;
				case "means" goto State:whatIsState;
				case "ment" goto State:whatIsState;
				case "by" goto State:whatIsState;
				case "definition" goto State:whatIsState;
			
				:someWord {
					set #meaning to :definable;
					exclude #instantiation from #pronoun;
					exclude #instantiation from #question;
					exclude #instantiation from #punctuation;
					exclude #meaning from :number;
					exclude "there";
					exclude "up";
					exclude "going";
				}
				:definable {
					set #sentence to :definition;
				}
				
				case :someWord goto State:whatIsSomethingState;
				case :unknownWord goto State:discoverWikidataState;
				
				:number {
					set #instantiation from #number;
				}
				:unknownWord {
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
					
				
				State:whatIsSomethingState {
					case :punctuation goto State:whatIsSomethingState;
					
					Quotient:Equation:checkCorrection;
					Equation:checkCorrection {
						assign :result to (is :sentence related to :definition by #response);
						if (:result, #false)
							then #null
							else (do(
								call #push on #Context with :definable,
								set #topic to :definable on :conversation,
								:definition));
					}
				}
				
				State:discoverWikidataState {
					case :punctuation goto State:discoverWikidataState5;
					case "'" return;
					case :unknownWord2 goto State:discoverWikidataState2;
					
					Quotient:Equation:defineWiktionary;
					Equation:defineWiktionary {
						if not (:someWord, #null) and (:unknownWord2, #null)
							then (do (
								assign :result to (is :sentence related to :definition by #response),
								if (:result, #false)
									then (assign :ignore to #true)));
						if (:ignore, #null)
							then (
								if not (:who, #null) or not (:unknownWord2, #null) or not (:search, #null)
									then Equation:discoverWikidata
									else (do (
										assign :resultobject to (call #define on #Wiktionary with :unknownWord),
										if (:resultobject, #null)
											then Equation:discoverWikidata
											else (do(
												call #push on #Context with :result,
												set #topic to :resultobject on :conversation,
												assign :result to (get #sentence from :resultobject),
												if (:result, #null)
													then (return Equation:discoverWikidata),
												:result)))))
							else #null;
					}
					Equation:discoverWikidata {
						if (:someWord, #null)
							else (do (
								assign :resultobject to (is :sentence related to :definition by #response),
								if (:resultobject, #false)
									then (assign :ignore to #true)));
						if (:ignore, #null)
							then (do(
								assign :resultobject to (call #discover on #Wikidata with (:unknownWord, :unknownWord2, :unknownWord3, :unknownWord4, :unknownWord5)),
								if (:resultobject, #null)
									then #null
									else (do(
										call #push on #Context with :resultobject,
										set #topic to :resultobject on :conversation,
										assign :result to (get #sentence from :resultobject),
										if (:result, #null)
											then #null,
										:result))))
							else #null;
					}
				}
				
				State:discoverWikidataState2 {
					case :punctuation goto State:discoverWikidataState5;
					case "'" return;
					case :unknownWord3 goto State:discoverWikidataState3;
					
					Quotient:Equation:defineWiktionary;
				}
				
				State:discoverWikidataState3 {
					case :punctuation goto State:discoverWikidataState5;
					case "'" return;
					case :unknownWord4 goto State:discoverWikidataState4;
					
					Quotient:Equation:defineWiktionary;
				}
				
				State:discoverWikidataState4 {
					case :punctuation goto State:discoverWikidataState5;
					case "'" return;
					case :unknownWord5 goto State:discoverWikidataState5;
					
					Quotient:Equation:defineWiktionary;
				}
				
				State:discoverWikidataState5 {
					case :punctuation goto State:discoverWikidataState5;
					
					Quotient:Equation:defineWiktionary;
				}
			}
		}
	}
}


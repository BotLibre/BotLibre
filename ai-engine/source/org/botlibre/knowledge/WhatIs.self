// Respond to "What is x", "Who is x" sentences using Wikidata
state WhatIs {
	case input goto sentenceState for each #word of sentence;
	
	pattern "* is what" template (redirect (Template("what is {star}")));
	pattern "(can) (you) tell me (something) about *" template (redirect (Template("what is {star}")));
	pattern "(do what) you know (anything) about *" template (redirect (Template("what is {star}")));
	
	pattern "(do) (you) (know) (please) (tell) (me) who * is" template (redirect (Template("who is {star}")));
	pattern "(do) (you) (know) (please) (tell) (me) what * is" template (redirect (Template("what is {star}")));

	state sentenceState {
		case what goto whatState;
		case who goto whatState;
		case "whats" goto whatIsState;
		case "wahts" goto whatIsState;
		case "waht" goto whatState;
		case "whos" goto whatIsState;
		
		case "tell" goto sentenceState;
		case "me" goto sentenceState;
		case "can" goto sentenceState;
		case "you" goto sentenceState;
		case "do" goto sentenceState;
		case "know" goto sentenceState;
		
		case "define" goto whatIsState;
		
		case "google" goto searchState;
		case "xfind" goto searchState;
		case "search" goto searchState;
		case "lookup" goto searchState;
		case "find" goto searchState;
		
		var questionWord {
			meaning : question;
		}
		var punctuation {
			instantiation : #punctuation;
		}
		var quote {
			meaning : #quote;
		}
		var what {
			meaning : #what;
		}
		var who {
			meaning : #who;
		}
		var is {
			meaning : #is;
		}
		var question {
			instantiation : #question;
		}
		var a {
			meaning : #a;
		}
		var a2 {
			meaning : #a;
		}
		var the {
			meaning : #the;
		}
		
		state searchState {
			do {
				search = true;
			}
			
			case unknownWord goto discoverWikidataState;
		}
		
		// 'What...'
		state whatState {	
			case is goto whatIsState;
			case quote goto whatState;
			case "s" goto whatIsState;
			case "means" goto whatIsState;
			
			state whatIsState {
			
				case the goto whatIsState;
				case a goto whatIsState;
				case "meaning" goto whatIsState;
				case "of" goto whatIsState;
				case "mean" goto whatIsState;
				case "means" goto whatIsState;
				case "ment" goto whatIsState;
				case "by" goto whatIsState;
				case "definition" goto whatIsState;
			
				var someWord {
					meaning : definable;
					instantiation : ! #pronoun, ! #question, ! #punctuation;
					meaning : ! number;
					: ! "there", ! "up", ! "going";
				}
				var definable {
					sentence : definition;
				}
				
				case someWord goto whatIsSomethingState;
				case unknownWord goto discoverWikidataState;
				
				var number {
					instantiation : #number;
				}
				var unknownWord {
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
				
				state whatIsSomethingState {
					case punctuation goto whatIsSomethingState;
					
					answer checkCorrection();
					
					function checkCorrection() {
						result = sentence.has(#response, definition);
						if (result == false) {
							return null;
						} else {
							Context.push(definable);
							conversation.topic = definable;
							return definition;
						}
					}
				}
				
				state discoverWikidataState {
					case punctuation goto discoverWikidataState5;
					case "'" return;
					case unknownWord2 goto discoverWikidataState2;
					
					answer defineWiktionary();
					
					function defineWiktionary() {
						if ((someWord != null) && (unknownWord2 == null)) {
							result = sentence.has(#response, definition);
							if (result == false) {
								ignore = true;
							}
						}
						if (ignore == null) {
							if ((who != null) || (unknownWord2 != null) || (search != null)) {
								discoverWikidata();
							} else {
								result = Wiktionary.define(unknownWord);
								if (result == null) {
									discoverWikidata();
								} else {
									Context.push(result);
									conversation.topic = result;
									result = result.sentence;
									if (result == null) {
										return discoverWikidata();
									}
									return result;
								}
							}
						} else {
							return null;
						}
					}
					
					function discoverWikidata() {
						if (someWord != null) {
							result = sentence.has(#response, definition);
							if (result == false) {
								ignore = true;
							}
						}
						if (ignore == null) {
							result = Wikidata.discover(unknownWord, unknownWord2, unknownWord3, unknownWord4, unknownWord5),
							if (result == null) {
								return null;
							} else {
								Context.push(result);
								conversation.topic = result;
								result = result.sentence;
								return result;
							}
						} else {
							return null;
						}
					}
				}
				
				state discoverWikidataState2 {
					case punctuation goto discoverWikidataState5;
					case "'" return;
					case unknownWord3 goto discoverWikidataState3;
					
					answer defineWiktionary();
				}
				
				state discoverWikidataState3 {
					case punctuation goto discoverWikidataState5;
					case "'" return;
					case unknownWord4 goto discoverWikidataState4;
					
					answer defineWiktionary();
				}
				
				state discoverWikidataState4 {
					case punctuation goto discoverWikidataState5;
					case "'" return;
					case unknownWord5 goto discoverWikidataState5;
					
					answer defineWiktionary();
				}
				
				state discoverWikidataState5 {
					case punctuation goto discoverWikidataState5;
					
					answer defineWiktionary();
				}
			}
		}
	}
}

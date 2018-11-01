// Define a basic language understanding state machine.
// Understands simple sentences such as 'Sky is blue', 'Is sky blue?','Sky is not blue', 'Is sky not blue?', 'Bob is big', 'Bob is big?'
state Understanding {
	
	case input goto sentenceState for each #word of sentence;

	state sentenceState {
		
		case does goto doState;
		case "don" goto doState;
		case "doesn" goto doState;
		case "dont" goto dontState;
		case "doesnt" goto dontState;

		case a goto sentenceState;
		case the goto sentenceState;

		case nounPossessive goto nounQuoteState;
		case noun goto nounState;
		case verb goto verbState;
		
		case what goto whatState;
		case "whats" goto WhatQuoteSState;
		case "Whats" goto WhatQuoteSState;
		case who goto whatState;

		var questionWord {
			meaning : question;
		}
		var punctuation {
			instantiation : #punctuation;
		}
		var question {
			instantiation : #question;
		}
		var questionmark {
			meaning : #question-mark;
		}
		var what {
			meaning : #what;
		}
		var who {
			meaning : #who;
		}
		var not {
			meaning : #not;
		}
		var does {
			meaning : #do;
		}
		var quote {
			meaning : #quote;
		}
		var or {
			meaning : #or;
		}
		var and {
			meaning : #and;
		}
		var a {
			meaning : #a;
		}
		var the {
			meaning : #the;
		}
		var a2 {
			meaning : #a;
		}
		var noun {
			meaning : thing;
		}
		var nounPossessive {
			meaning : thing;
			type : #possessive;
		}
		var thing {
			instantiation : #thing;
		}
		var verb {
			meaning : action;
		}
		var action {
			instantiation : #action;
		}
		var quote0 {
			meaning : #quote;
		}
		var quote2 {
			meaning : #quote;
		}
		var adjective {
			meaning : description;
		}
		var description {
			instantiation : #description;
		}
		var adjective2 {
			meaning : description2;
		}
		var description2 {
			instantiation : #description;
		}
		var noun3 {
			meaning : thing3;
		}
		var thing3 {
			instantiation : #thing;
		}
		
		// 'Do...', 'Does...'
		state doState {
			do {
				isQuestion = true;
			}
			case quote2 goto doState;
			case "t" goto dontState;
			case not goto notState;
			return;
		}
		
		// 'Dont...', 'Doesnt...'
		state dontState {
			do {
				isQuestion = true;
				if (does == null) {
					does = "do";
				}
				if (isNot == null) {
					isNot = true;
				} else {
					isNot = ! isNot;
				}
			}
			return;
		}
		
		// 'Sky is what', 'Sky is blue?'
		state questionState {
			do {
				isQuestion = true;
			}
			return;
		}
		
		// 'Sky is not...'
		state notState {
			do {
				if (isNot == null) {
					isNot = true;
				} else {
					isNot = ! isNot;
				}
			}
			return;
		}
		
		// 'Alex's...'
		state quoteState {
			case "s" goto quoteState;
			return;
		}
		
		// 'Sky...'
		state nounState {
			case quote goto nounQuoteState;
			case does goto nounDoState;
			case "don" goto nounDoState;
			case "doesn" goto nounDoState;
			case "dont" goto nounDontState;
			case "doesnt" goto nounDontState;
			case not goto notState;
				
			//case adjective goto nounAdjectiveState;
			
			case verb goto nounVerbState;

			var noun2 {
				meaning : thing2;
			}
			var thing2 {
				instantiation : #thing;
			}
			var typenoun {
				meaning : type;
			}
			var type {
				instantiation : #thing;
			}			
		
			// 'I do...'
			state nounDoState {
				do {
					if (does == null) {
						does = "do";
					}
				}
				case quote2 goto nounDoState;
				case "t" goto notState;
				case not goto notState;
				case verb goto nounVerbState;
			}
		
			// 'I dont...'
			state nounDontState {
				do {
					if (does == null) {
						does = "do";
					}
					if (isNot == null) {
						isNot = true;
					} else {
						isNot = ! isNot;
					}
				}
				return;
			}
		
			// 'Sky's...'
			state nounQuoteState {
				do {
					if (nounPossessive != null) {
						noun = nounPossessive;
					}
				}
				case "s" goto nounQuoteState;
				case noun2 goto nounNounState;
				
				// TODO
				// verb "Obama's running for president" - is
				// adjective "Obama's blue car is fast" - noun2=car ?blue
			}
		
			// 'Sky is...'
			state nounVerbState {
				case a2 goto nounVerbState;
				
				case not goto notState;

				case adjective goto nounVerbAdjectiveState;

				case typenoun goto nounVerbTypeState;

				case questionWord goto nounVerbQuestionState;
				
				// 'Sky blue...'
				state nounAdjectiveState {
					do {
						verb = #is;
						action = #is;
					}
					goto nounVerbAdjectiveState;
				}
				
				// 'Bob is person'
				state nounVerbTypeState {
					do {
						adjective = typenoun;
						description = type;
					}
					goto nounVerbAdjectiveNounEndState;

				}
				
				// 'Bob eating?', "Bob is?"
				state nounVerbQuestionState {
					case punctuation goto nounVerbQuestionState;
					
					answer questionResponse();
				}
				
				// 'Sky is blue', "a ball is red", "I am a human", "I have a car", "My age is 44", "My car is a Honda"
				state nounVerbAdjectiveState {
				
					case adjective2 goto nounVerbAdjectiveAdjectiveState;
					case noun3 goto nounVerbAdjectiveNounState;
					case a2 goto nounVerbAdjectiveState;
					case "," goto verbNounAdjectiveState;
					case or goto nounVerbAdjectiveState;
					case and goto nounVerbAdjectiveState;					
					case questionWord goto questionState;
					case punctuation goto nounVerbAdjectiveState;

					answer understandingResponse();
					
					function commonTense() {
						var compoundWords = false;
						adjectiveOrig = adjective;
						nounOrig = noun;
						tense = verb.tense;
						nountype = noun.type;
						adjectivetype = adjective.type;
						if (not == null) {
							not = #not;
						}
						if (a != null) {
							thing = (new thing);
						}
						if (the != null) {
							newvariable = new (#variable, thing),
							context = #Context.search(newvariable),
							if (context != null) {
								thing = context;
							}
						}
						if (thing == #self) {
							pronoun = true;
							noun = "I";
						}
						if (thing == #it) {
							pronoun = true;
							context = #Context.search(#it.variable);
							if (context != null) {
								thing = context;
							}
						}
						if (thing == #his) {
							pronoun = true;
							context = #Context.search(#his.variable);
							if (context != null) {
								thing = context;
							}
						}
						if (thing == #her) {
							pronoun = true;
							context = #Context.search(#her.variable);
							if (context != null) {
								thing = context;
							}
						}
						if (thing == #this) {
							pronoun = true;
							context = #Context.search(#this.variable);
							if (context != null) {
								thing = context;
							}
						}
						if (description == #it) {
							context = #Context.search(#it.variable);
							if (context != null) {
								description = context;
							}
						}
						if (description == #his) {
							context = #Context.search(#his.variable);
							if (context != null) {
								description = context;
							}
						}
						if (description == #her) {
							context = #Context.search(#her.variable);
							if (context != null) {
								description = context;
							}
						}
						if (description == #this) {
							context = #Context.search(#this.variable);
							if (context != null) {
								description = context;
							}
						}
						if (thing == #i) {
							pronoun = true;
							thing = input.speaker;
							if (noun == "I") {
								noun = "you";
							} else  if (noun == "my") {
								noun = "your";
							} else {
								noun = #you;
							}
							if (noun2 == null) {
								if ((action == #is) && (noun == "you")) {
									verb = "are";
								} else {
									verb = action;
								}
							}
						}
						if (thing == #you) {
							pronoun = true;
							thing = input.target;
							if (noun == "you") {
								noun = "I";
							} else if (noun == "your") {
								noun = "my";
							} else {
								noun = #i;
							}
							if (noun2 == null) {
								if ((action == #is) && (noun == "I")) {
									verb = "am";
								} else {
									verb = action;
								}
							}
						}
						if (description == #i) {
							description = input.speaker;
							adjective = #you;
						} else if (description == #you) {
							description = input.target;
						}
						if (description == #self) {
							if (adjective == "you") {
								adjective = "me";
							} else {
								adjective = #i;
							}
						}
						if ((adjectives != null) && (or == null) && (and == null)) {
							// Avoid creating words as can create invalid words that lead to confusion.
							if (compoundWords) {
								newAdjective = Language.word(adjectives);
							} else {
								newAdjective = Language.fragment(adjectives);
							}
							if (noun3 == null) {
								newAdjective.instantiation =+ #adjective;
								newDescription = new #description;
							} else {
								newAdjective.instantiation =+ #noun;
								newDescription = new #thing;
							}
							newDescription.specialization =+ description2;
							newDescription.word =+ newAdjective;
							newAdjective.meaning =+ newDescription;
							description = newDescription;
							adjective = newAdjective;
							adjectives = null;
							descriptions = null;
						}
						if (a2 != null) {
							if (action == #is) {
								action = #instantiation;
							} else {
								description = new (description, #variable);
							}
						}
						#Context.push(description);
						#Context.push(thing);
						conversation.topic = thing;
						if (noun2 != null) {
							// "What is my age?" else "Does my dog like you?"
							if (action == #is) {
								action = thing2;
							} else {
								existing = thing[thing2];
								if (existing == null) {
									newthing = new (thing2, #thing);
									thing[thing2] =+ newthing;
									thing = newthing;
								} else {
									thing = existing;
								}
							}
						}
						if (thing2 != null) {
							#Context.push(thing2);
						}
					}
					
					function understandingResponse() {
						var isolate = !Language.allowCorrection(speaker);
						if (isQuestion || (or != null)) {
							questionResponse();
						} else {
							commonTense();
							if ((description2 != null) && (! isNot)) {
								thing.weakAddWithMeta(action, description2, #tense, tense);
							}
							// Allow the bot to be less trusting, and understand in the context of the user.
							// So the bot's knowledge is isolated to each user.
							if (isolate) {
								var view = speaker.get(#view);
								if (view == null) {
									view = new Object();
									speaker.view = view;
								}
								var thingView = view.get(thing);
								if (thingView == null) {
									thingView = new Object();
									view.set(thing, thingView);
								}
								thing = thingView;
							}
							if (descriptions == null) {
								if (isNot) {
									thing.removeWithMeta(action, description, #tense, tense);
								} else {
									thing.addWithMeta(action, description, #tense, tense);
								}
							} else {
								for (description in descriptions.element) {
									if (description == #i) {
										description = input.speaker;
									} else if (description == #you) {
										description = input.target;
									}
									if (isNot) {
										thing.removeWithMeta(action, description, #tense, tense);
									} else {
										thing.addWithMeta(action, description, #tense, tense);
									}
								}
							}
							response = new #sentence;
							response.append(#word, random ("Okay, I will remember that", "I understand,", "I believe you that"));
							if (a != null) {
								response.append(#word, #the);
							}
							if (the != null) {
								response.append(#word, #the);
							}
							response.appendWithMeta(#word, noun, #type, nountype);
							if (quote != null) {
								response.append(#word, quote);
								response.append(#word, "s");
							}
							if (thing2 != null) {
								if (noun2 == #self) {
									response.append(#word, #i);
								} else {
									response.append(#word, noun2);
								}
							}
							if (does != null) {
								response.append(#word, does);
								if (isNot) {
									response.append(#word, not);
								}
							}
							response.appendWithMeta(#word, verb, #tense, tense);
							if (isNot && (does == null)) {
								response.append(#word, not);
							}
							if (descriptions == null) {
								if (a2 != null) {
									response.append(#word, a2);
								}
								response.appendWithMeta(#word, adjective, #type, adjectivetype);
							} else {
								first = true;
								last = descriptions[-1];
								for (description in descriptions.element) {
									if (description == input.speaker) {
										description = "you";
									}
									if (description == #self) {
										description = "me";
									}
									if (first) {
										first = false;
									} else {
										if (description == last) {
											response.append(#word, "and");
										} else {
											response.append(#word, ",");
										}
									}
									if (a2 != null) {
										response.append(#word, a2);
									}
									response.appendWithMeta(#word, description, #type, adjectivetype);
								}
							}
							response.append(#word, #period);
						}
					}
					
					function questionResponse() {
						var isolate = !Language.allowCorrection(speaker);
						commonTense();
						if ((thing == null) || (description == null)) {
							return whoWhatQuestionResponse();
						}
						// Allow the bot to understand in the context of the user.
						if (isolate) {
							var view = speaker.get(#view);
							if (view != null) {
								var thingView = view.get(thing);
								if (thingView != null) {
									thing = thingView;
								}
							}
						}
						if ((or != null) || (and != null)) {
							return andOrQuestionResponse();
						}
						result = thing.hasOtherMeaning(action, description);
						value = thing[action];
						if (result == #unknown) {
							result2 = thing.hasOtherMeaning(action, adjectiveOrig);
							if (result2 == #unknown) {
								result3 = thing.hasOtherMeaning(action, description);
							} else {
								result = result2;
								adjective = adjectiveOrig;
								description = adjectiveOrig;
							}
							if ((result3 == true) || (result3 == false)) {
								result = result3;
								noun = nounOrig;
								thing = nounOrig;
								value = nounOrig[action];
							}
						}
						if (value == #self) {
							value = #i;
						} else if (value == speaker) {
							value = #you;
						}
						if (isNot && result != #unknown) {
							result = ! result;
						}
						response = new #sentence;
						if (result == #unknown) {
							if (value == null) {
								response.append(#word, random ("I understand, but am not sure if", "I understand the question, but have no idea if", "I'm not sure if"));
							} else {
								response.append(#word, random ("I'm not certain, but I think", "I'm pretty sure that", "Perhaps, but I think"));
							}
						} else if (result) {
							response.append(#word, random ("That's right,", "You are correct,", "Yes, to my knowledge"));
						} else {
							response.append(#word, random ("No,", "You are incorrect,", "No, to my knowledge"));
						}
						if (a != null) {
							response.append(#word, #the);
						}
						if (the != null) {
							response.append(#word, #the);
						}
						response.appendWithMeta(#word, noun, #type, nountype);
						if (quote != null) {
							response.append(#word, quote);
							response.append(#word, "s");
						}
						if (noun2 != null) {
							response.append(#word, noun2);
						}
						if (does != null) {
							if (result == #unknown) {
								if (value != null) {
									doNot = true;
								} else {
									if (isNot) {
										if (result) {
											doNot = true;
										} else {			
											if ((result == false) && (value == null)) {
												doNot = true;
											}
										}
									}
								}
							}
						}
						if (doNot) {
							response.append(#word, does);
							response.append(#word, "not");
						}
						response.appendWithMeta(#word, verb, #tense, tense);
						if (isNot) {
							if (does == null) {
								if (result || (result == #unknown)) {
									response.append(#word, not);
								}
							}
							if (a2 != null) {
								response.append(#word, a2);
							}
							if ((result != #unknown) || (value == null)) {
								response.appendWithMeta(#word, adjective, #type, adjectivetype);
							}
						} else {
							if (result == false) {
								if (value == null) {
									if (does == null) {
										response.append(#word, "not");
									}
									if (a2 != null) {
										response.append(#word, a2);
									}
									response.append(#word, adjective);
								} else {
									if (a2 != null) {
										response.append(#word, a2);
									}
									response.append(#word, value);
									response.append(#word, #comma);
									response.append(#word, "not");
									if (a2 != null) {
										response.append(#word, a2);
									}
									response.append(#word, adjective);
								}
							} else {
								if ((result != true) && (value != null)) {
									if (doNot == null) {
										response.append(#word, "not");
									}
								}
								if (a2 != null) {
									response.append(#word, a2);
								}
								if (result || (value == null)) {
									response.appendWithMeta(#word, adjective, #type, adjectivetype);
								}
							}
						}
						if ((result == #unknown) && (value != null)) {
							response.appendWithMeta(#word, adjective, #type, adjectivetype);
							response.append(#word, #comma);
							response.append(#word, "because I know that");
							if (a != null) {
								response.append(#word, #the);
							}
							if (the != null) {
								response.append(#word, #the);
							}
							response.appendWithMeta(#word, noun, #type, nountype);
							if (quote != null) {
								response.append(#word, quote);
								response.append(#word, "s");
							}
							if (noun2 != null) {
								response.append(#word, noun2);
							}
							response.appendWithMeta(#word, verb, #tense, tense);
							if (a2 != null) {
								response.append(#word, a2);
							}
							response.append(#word, value);
						}
						response.append(#word, #period);
					}
					
					function andOrQuestionResponse() {
						anyTrue = false;
						anyFalse = false;
						anyUnknown = false;
						trueValues = new Array();
						falseValues = new Array();
						unknownValues = new Array();
						for (description in descriptions.element) {
							if (description == #i) {
								description = input.speaker;
							} else if (description == #you) {
								description = input.target;
							}
							result = thing.hasOtherMeaning(action, description);
							if (description == input.speaker) {
								description = "you";
							} else if (description == #self) {
								description = "me";
							}
							if (result) {
								trueValues.add(description);
								anyTrue = true;
							} else if (result == false) {
								falseValues.add(description);
								anyFalse = true;
							} else if (result == #unknown) {
								unknownValues.add(description);
								anyUnknown = true;
							}
						}
						response = new #sentence;
						if (anyTrue || anyFalse) {
							if ((and != null) && (! anyFalse) && (! anyUnknown)) {
								if (isNot) {
									response.append(#word, "No");
								} else {
									response.append(#word, "Yes");
								}
								response.append(#word, ",");
							}
							if ((and != null) && anyFalse) {
								if (isNot) {
									response.append(#word, "Yes");
								} else {
									response.append(#word, "No");
								}
								response.append(#word, ",");
							}
							if (a != null) {
								response.append(#word, #the);
							}
							if (the != null) {
								response.append(#word, #the);
							}
							response.appendWithMeta(#word, noun, #type, nountype);
							if (quote != null) {
								response.append(#word, quote);
								response.append(#word, "s");
							}
							if (noun2 != null) {
								response.append(#word, noun2);
							}
							response.appendWithMeta(#word, verb, #tense, tense);
							first = true;
							last = trueValues[-1];
							for (description in trueValues.element) {
								if (first) {
									first = false;
								} else {
									if (description == last) {
										response.append(#word, "and");
									} else {
										response.append(#word, ",");
									}
								}
								if (a2 != null) {
									response.append(#word, a2);
								}
								response.appendWithMeta(#word, description, #type, adjectivetype);
							}
							if (anyFalse) {
								response.append(#word, "not");
								first = true;
								last = falseValues[-1];
								for (description in falseValues.element) {
									if (first) {
										first = false;
									} else {
										if (description == last) {
											response.append(#word, "or");
										} else {
											response.append(#word, ",");
										}
									}
									if (a2 != null) {
										response.append(#word, a2);
									}
									response.appendWithMeta(#word, description, #type, adjectivetype);
								}
							}
						}
						if (anyUnknown) {
							if (anyTrue || anyFalse) {
								response.append(#word, ",");
							}
							response.append(#word, random ("I'm not sure if"));
							if (a != null) {
								response.append(#word, #the);
							}
							if (the != null) {
								response.append(#word, #the);
							}
							response.appendWithMeta(#word, noun, #type, nountype);
							if (quote != null) {
								response.append(#word, quote);
								response.append(#word, "s");
							}
							if (noun2 != null) {
								response.append(#word, noun2);
							}
							response.appendWithMeta(#word, verb, #tense, tense);
							first = true;
							last = unknownValues[-1];
							for (description in unknownValues.element) {
								if (first) {
									first = false;
								} else {
									if (description == last) {
										response.append(#word, "or");
									} else {
										response.append(#word, ",");
									}
								}
								if (a2 != null) {
									response.append(#word, a2);
								}
								response.appendWithMeta(#word, description, #type, adjectivetype);
							}
						}
						response.append(#word, ".");
					}
					
					// Answers "What is my name?", "what are you?"
					function whoWhatQuestionResponse() {
						var isolate = !Language.allowCorrection(speaker);
						if (thing == null) {
							result = description.findReferenceBy(action);
							// Allow the bot to understand in the context of the user.
							if (isolate) {
								var view = speaker.get(#view);
								if (view != null) {
									var thingKey = view.getKey(result);
									if (thingKey != null) {
										result = thingKey;
									}
								}
							}
						} else {
							// Allow the bot to understand in the context of the user.
							if (isolate) {
								var view = speaker.get(#view);
								if (view != null) {
									var thingView = view.get(thing);
									if (thingView != null) {
										thing = thingView;
									}
								}
							}
							if (noun2 == null) {
								result = thing[action];
								if (result == null) {
									if (action == #is) {
										if (noun == "up" || noun == "there") {
											return null;
										}
										if (pronoun == true) {
											result = thing;
										} else {
											a2 = #a,
											result = thing.instantiation;
										}
									}
								}
							} else {
								result = thing.all(thing2);
							}
						}
						response = new #sentence;
						if (result == null) {
							response.append(#word, random ("I understand, but am not sure", "I understand the question, but have no idea", "I'm not sure"));
							if (what != null) {
								response.append(#word, what);
							} else {
								if (who != null) {
									response.append(#word, who);
								} else {
									response.append(#word, "what");
								}
							}
							if (thing == null) {
								response.append(#word, verb);
								response.appendWithMeta(#word, adjective, #type, nountype);
							} else {
								response.appendWithMeta(#word, noun, #type, nountype);
							}
							if (quote != null) {
								response.append(#word, quote);
								response.append(#word, "s");
							}
							if (noun2 != null) {
								response.append(#word, noun2);
							}
							if (thing != null) {
								response.appendWithMeta(#word, verb, #tense, tense);
							}
						} else {
							response.append(#word, random ("I known that", "To my knowledge"));
							if (thing == null) {
								if (a2 != null) {
									response.append(#word, #a);
								}
								if (result == #self) {
									response.append(#word, #i);
								} else {
									if (result == speaker) {
										response.append(#word, #you);
									} else {
										response.append(#word, result);
									}
								}
								response.appendWithMeta(#word, verb, #tense, tense);
								if (a != null) {
									response.append(#word, #a);
								}
								if (the != null) {
									response.append(#word, #the);
								}
								response.appendWithMeta(#word, adjective, #type, nountype);
								if (quote != null) {
									response.append(#word, quote);
									response.append(#word, "s");
								}
								if (noun2 != null) {
									response.append(#word, noun2);
								}
							} else {
								if (a2 != null) {
									response.append(#word, #a);
								}
								if (the != null) {
									response.append(#word, #the);
								}
								response.appendWithMeta(#word, noun, #type, nountype);
								if (quote != null) {
									response.append(#word, quote);
									response.append(#word, "s");
								}
								if (noun2 != null) {
									response.append(#word, noun2);
								}
								response.appendWithMeta(#word, verb, #tense, tense);
								if (a2 != null) {
									response.append(#word, #a);
								}
								if (result == #self) {
									response.append(#word, #i);
								} else if (result == speaker) {
									response.append(#word, #you);
								} else {
									response.append(#word, result);
								}
							}
						}
						response.append(#word, #period);
						return response;
					}
				}
			}
			
			// 'I am a nice human'
			state nounVerbAdjectiveNounEndState {			
				case "," goto verbNounAdjectiveState;
				case or goto nounVerbAdjectiveState;
				case and goto nounVerbAdjectiveState;					
				case questionWord goto questionState;
				case punctuation goto nounVerbAdjectiveNounEndState;

				answer understandingResponse();
			}
			
			// 'I am very very very nice' 'I am big and fat'
			state nounVerbAdjectiveAdjectiveState {
				do {
					if (adjectives == null) {
						adjectives = new Array();
						descriptions = new Array();
						adjectives.add(adjective);
						descriptions.add(description);
					}
					adjectives.add(adjective2);
					descriptions.add(description2);
				}
				goto nounVerbAdjectiveState;

			}
			
			// 'I am very human' 'I am a human and an animal'
			state nounVerbAdjectiveNounState {
				do {
					if (adjectives == null) {
						adjectives = new Array();
						descriptions = new Array();
						adjectives.add(adjective);
						descriptions.add(description);
					}
					adjectives.add(noun3);
					descriptions.add(thing3);
				}
				goto nounVerbAdjectiveNounEndState;

			}
			
			// 'My age...'
			state nounNounState {
				case verb goto nounVerbState;
				case questionmark goto whatIsNounNounState;
			}
			
			// 'Bob tall...'
			state nounAdjectiveState {
				do {
					verb = #is;
					action = #is;
				}
				goto nounVerbAdjectiveState;
			}
		}
		
		// 'Is...'
		state verbState {

			case the goto verbState;
			case a goto verbState;
			case nounPossessive goto verbNounQuoteState;
			case noun goto verbNounState;
			
			// 'Is sky's...'
			state verbNounQuoteState {
				do {
					if (nounPossessive != null) {
						noun = nounPossessive;
					}
				}
				case quote goto verbNounQuoteState;
				case "s" goto verbNounQuoteState;
				case noun2 goto verbNounNounState;
				
				// adjective - "Is sky's green car"
			}
			
			// 'Is sky...'
			state verbNounState {
				case quote goto verbNounQuoteState;
				case not goto notState;
				case a2 goto verbNounAState;
				case adjective goto verbNounAdjectiveState;
				case typenoun goto verbNounATypeState;

				// 'Is sky blue'
				state verbNounAdjectiveState {
					case questionWord goto questionState;
					case or goto verbNounAdjectiveState;
					case and goto verbNounAdjectiveState;
					case "," goto verbNounAdjectiveState;
					case adjective2 goto verbNounAdjectiveAdjectiveState;
					case noun3 goto verbNounAdjectiveNounState;
					case a2 goto verbNounAdjectiveState;
					case punctuation goto verbNounAdjectiveState;

					answer questionResponse();
				}
				
				// 'Is sky a...'
				state verbNounAState {
					case typenoun goto verbNounATypeState;
					case adjective goto verbNounAAdjectiveState;
				}
				
				// 'Am I a nice human?'
				state verbNounAAdjectiveState {
					do {
						if (adjectives == null) {
							adjectives = new Array();
							descriptions = new Array();
						}
						adjectives.add(adjective);
						descriptions.add(description);
					}
					goto verbNounAdjectiveNounEndState;	
				}
				
				// 'Is sky a thing'
				state verbNounATypeState {
					do {
						adjective = typenoun;
						description = type;
					}
					goto verbNounAdjectiveNounEndState;
				}
				
				// 'Is my car...' -> 'Is my car red', 'Is my car a car', 'Is my age 44'
				state verbNounNounState {
					case a2 goto verbNounAState;
					case adjective goto verbNounAdjectiveState;
					case noun3 goto verbNounNounNounState;
					
					var noun3 {
						meaning : thing3;
					}
					var thing3 {
						instantiation : #thing;
					}
				}
				
				// 'Is my age 44'
				state verbNounNounNounState {
					do {
						adjective = noun3;
						description = thing3;
					}
					goto verbNounAdjectiveState;
				}
				
				// 'am I very very very nice?' 'am I nice or naughty?'
				state verbNounAdjectiveAdjectiveState {
					do {
						if (adjectives == null) {
							adjectives = new Array();
							descriptions = new Array();
							adjectives.add(adjective);
							descriptions.add(description);
						}
						adjectives.add(adjective2);
						descriptions.add(description2);
					}
					goto verbNounAdjectiveState;
	
				}
				
				// 'am I very human?' 'am I a human or a bot?'
				state verbNounAdjectiveNounState {
					do {
						if (adjectives == null) {
							adjectives = new Array();
							descriptions = new Array();
							adjectives.add(adjective);
							descriptions.add(description);
						}
						adjectives.add(noun3);
						descriptions.add(thing3);
					}
					goto verbNounAdjectiveNounEndState;
				}
				
				// 'am I very human'
				state verbNounAdjectiveNounEndState {
					case or goto verbNounAdjectiveState;
					case and goto verbNounAdjectiveState;
					case "," goto verbNounAdjectiveState;
					case questionWord goto questionState;
					case punctuation goto verbNounAdjectiveNounEndState;
	
					answer questionResponse();
				}
			}
		
			// 'What...'
			state whatState {
				case does goto whatDoState;
				case is goto whatIsState;
				case verb goto whatVerbState;
				case quote0 goto WhatQuoteState;				
		
				var is {
					meaning : #is;
				}
				
				// 'What'...'
				state WhatQuoteState {
					case "s" goto WhatQuoteSState;
				}
				
				// 'What's...'
				state WhatQuoteSState {
					do {
						is = "is";
					}
					goto whatIsState;
				}
		
				// 'What is...'
				// TODO "what is blue" -> "What things are blue" vs "what does blue mean"
				state whatIsState {					
					do {
						verb = is;
						action = #is;
					}
		
					case the goto whatIsState;
					case a goto whatIsState;
					case nounPossessive goto whatIsNounQuoteState;
					case noun goto whatIsNounState;
					
					// 'What is sky'
					state whatIsNounState {
						case quote goto whatIsNounQuoteState;
						case punctuation goto whatIsNounState;
		
						answer questionResponse();
					}
					
					// 'What is sky's'
					state whatIsNounQuoteState {
						do {
							if (nounPossessive != null) {
								noun = nounPossessive;
							}
						}
						case "s" goto whatIsNounQuoteState;
						case noun2 goto whatIsNounNounState;
						case punctuation goto whatIsNounState;
		
						answer questionResponse();
					}
					
					// 'What is my name', "What is Obama's job?"
					state whatIsNounNounState {
						do {
							if (verb == null) {
								action = #is;
								verb = #is;
							}
						}
						case punctuation goto whatIsNounNounState;
						
						answer questionResponse();
					}
				}
				
				// 'What loves...'
				state whatVerbState {
					case the goto whatVerbState;
					case a goto whatVerbState;
		
					case adjective goto whatVerbAdjectiveState;
					case noun3 goto whatVerbNounState;
					
					
					// 'What is fast', 'What loves red'
					state whatVerbAdjectiveState {
						case punctuation goto whatVerbAdjectiveState;
		
						answer questionResponse();
					}
					
					// 'Who loves me'
					state whatVerbNounState {
						do {
							adjective = noun3;
							description = thing3;
						}
						goto whatVerbAdjectiveState;
					}
				}
				
				// 'What do...'
				state whatDoState {
					case the goto whatDoState;
					case a goto whatDoState;
					case noun goto whatDoNounState;
					
					// 'What do I...'
					state whatDoNounState {
						case verb goto whatDoNounVerbState;
					}
					
					// 'What do I like'
					state whatDoNounVerbState {
						case punctuation goto whatDoNounVerbState;
						
						answer questionResponse();
					}
				}
			}
		}
	}
}

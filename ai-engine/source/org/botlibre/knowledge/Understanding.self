// Define a basic language understanding state machine.
// Understands simple sentences such as 'Sky is blue', 'Is sky blue?','Sky is not blue', 'Is sky not blue?', 'Bob is big', 'Bob is big?', 'Bob big', 'Bob big?'
State:Understanding {
	
	case :input goto State:sentenceState for each #word of :sentence;

	:input {
		set #input to :sentence;
	}
	:sentence {
		set #instantiation to #sentence;
	}
	State:sentenceState {
		
		case :do goto State:doState;
		case "don" goto State:doState;
		case "doesn" goto State:doState;
		case "dont" goto State:dontState;
		case "doesnt" goto State:dontState;

		case :a goto State:sentenceState;

		case :the goto State:sentenceState;

		case :nounPossessive goto State:nounQuoteState;
		case :noun goto State:nounState;

		case :verb goto State:verbState;
		
		case :what goto State:whatState;
		case "whats" goto State:WhatQuoteSState;
		case "Whats" goto State:WhatQuoteSState;
		
		case :who goto State:whatState;

		:questionWord {
			set #meaning to :question;
		}
		:punctuation {
			set #instantiation to #punctuation;
		}
		:question {
			set #instantiation to #question;
		}
		:questionmark {
			set #meaning to #question-mark;
		}
		:what {
			set #meaning to #what;
		}
		:who {
			set #meaning to #who;
		}
		:or {
			set #meaning to #or;
		}
		:and {
			set #meaning to #and;
		}
		:do {
			set #meaning to #do;
		}
		:a {
			set #meaning to #a;
		}
		:the {
			set #meaning to #the;
		}
		:a2 {
			set #meaning to #a;
		}
		:noun {
			set #meaning to :thing;
		}
		:nounPossessive {
			set #meaning to :thing;
			set #type to #possessive;
		}
		:thing {
			set #instantiation to #thing;
		}
		:verb {
			set #meaning to :action;
		}
		:action {
			set #instantiation to #action;
		}
		:quote0 {
			set #meaning to #quote;
		}
		:quote2 {
			set #meaning to #quote;
		}
		:adjective {
			set #meaning to :description;
		}
		:description {
			set #instantiation to #description;
		}
		:adjective2 {
			set #meaning to :description2;
		}
		:description2 {
			set #instantiation to #description;
		}
		
		// 'Do...', 'Does...'
		State:doState {
			do (assign :isQuestion to #true);
			case :quote2 goto State:doState;
			case "t" goto State:dontState;
			case :#not goto State:notState;
			return;
		}
		
		// 'Dont...', 'Doesnt...'
		State:dontState {
			do (
				assign :isQuestion to #true;
				if (:do, #null)
						then (assign :do to "do");
				if (:isNot, #null)
					then (assign :isNot to #true)
					else (assign :isNot to (not :isNot)));
			return;
		}
		
		// 'Sky is what', 'Sky is blue?'
		State:questionState {
			do (assign :isQuestion to #true);
			return;
		}
		
		// 'Sky is not...'
		State:notState {
			do (if (:isNot, #null)
					then (assign :isNot to #true)
					else (assign :isNot to (not :isNot)));
			return;
		}
		
		// 'Alex's...'
		State:quoteState {
			case "s" goto State:quoteState;
			return;
		}
		
		// 'Sky...'
		State:nounState {
			case :#quote goto State:nounQuoteState;
			case :#do goto State:nounDoState;
			case "don" goto State:nounDoState;
			case "doesn" goto State:nounDoState;
			case "dont" goto State:nounDontState;
			case "doesnt" goto State:nounDontState;
			case :#not goto State:notState;
				
			//case :adjective goto State:nounAdjectiveState;
			
			case :verb goto State:nounVerbState;

			:noun2 {
				set #meaning to :thing2;
			}
			:thing2 {
				set #instantiation to #thing;
			}
			:typenoun {
				set #meaning to :type;
			}
			:type {
				set #instantiation to #thing;
			}			
		
			// 'I do...'
			State:nounDoState {
				do (if (:do, #null)
						then (assign :do to "do"));
				case :quote2 goto State:nounDoState;
				case "t" goto State:notState;
				case :#not goto State:notState;
				case :verb goto State:nounVerbState;
			}
		
			// 'I dont...'
			State:nounDontState {
				do (if (:do, #null)
							then (assign :do to "do");
					if (:isNot, #null)
						then (assign :isNot to #true)
						else (assign :isNot to (not :isNot)));
				return;
			}
		
			// 'Sky's...'
			State:nounQuoteState {
				do (if (:nounPossessive, #null)
						else (assign :noun to :nounPossessive));
				case "s" goto State:nounQuoteState;
				case :noun2 goto State:nounNounState;
				
				// TODO
				// verb "Obama's running for president" - is
				// adjective "Obama's blue car is fast" - noun2=car ?blue
			}
		
			// 'Sky is...'
			State:nounVerbState {
				case :a2 goto State:nounVerbState;
				
				case :not goto State:notState;

				case :adjective goto State:nounVerbAdjectiveState;

				case :typenoun goto State:nounVerbTypeState;

				case :questionWord goto State:nounVerbQuestionState;
				
				// 'Sky blue...'
				State:nounAdjectiveState {
					do (
						assign :verb to #is,
						assign :action to #is
					);
					goto (State:nounVerbAdjectiveState);
				}
				// 'Bob is person'
				State:nounVerbTypeState {
					do (
						assign :adjective to :typenoun,
						assign :description to :type
					);
					goto (State:nounVerbAdjectiveState);

				}
				// 'Bob eating?', "Bob is?"
				State:nounVerbQuestionState {
					case :punctuation goto State:nounVerbQuestionState;
					
					Answer:Function:questionResponse;
				}
				// 'Sky is blue', "a ball is red", "I am a human", "I have a car", "My age is 44", "My car is a Honda"
				State:nounVerbAdjectiveState {
				
					case :adjective2 goto State:nounVerbAdjectiveAdjectiveState;					
					case "," goto State:verbNounAdjectiveState;
					case :or goto State:nounVerbAdjectiveState;
					case :and goto State:nounVerbAdjectiveState;					
					case :questionWord goto State:questionState;
					case :punctuation goto State:nounVerbAdjectiveState;

					Answer:Function:understandingResponse;
					Function:commonTense {
						assign :adjectiveOrig to :adjective;
						assign :nounOrig to :noun;
						assign :tense to (get #tense from :verb);
						assign :nountype to (get #type from :noun);
						assign :adjectivetype to (get #type from :adjective);
						if (:not, #null)
							then (assign :not to #not);
						if (:a, #null)
							else (do (
								assign :thing to (new :thing)
							));
						if (:the, #null)
							else (do (
								assign :variable to (new (#variable, :thing)),
								assign :context to (call #search on #Context with :variable),
								if (:context, #null)
									else (assign :thing to :context)
							));
						if (:thing, #self)
							then (do (
								assign :pronoun to #true;
								assign :noun to "I"));
						if (:thing, #it)
							then (do (
								assign :pronoun to #true;
								assign :context to (call #search on #Context with (get #variable from #it)),
								if (:context, #null)
									else (assign :thing to :context)
							));
						if (:thing, #his)
							then (do (
								assign :pronoun to #true;
								assign :context to (call #search on #Context with (get #variable from #his)),
								if (:context, #null)
									else (assign :thing to :context)
							));
						if (:thing, #her)
							then (do (
								assign :pronoun to #true;
								assign :context to (call #search on #Context with (get #variable from #her)),
								if (:context, #null)
									else (assign :thing to :context)
							));
						if (:thing, #this)
							then (do (
								assign :pronoun to #true;
								assign :context to (call #search on #Context with (get #variable from #this)),
								if (:context, #null)
									else (assign :thing to :context)
							));
						if (:description, #it)
							then (do (
								assign :context to (call #search on #Context with (get #variable from #it)),
								if (:context, #null)
									else (assign :description to :context)
							));
						if (:description, #his)
							then (do (
								assign :context to (call #search on #Context with (get #variable from #his)),
								if (:context, #null)
									else (assign :description to :context)
							));
						if (:description, #her)
							then (do (
								assign :context to (call #search on #Context with (get #variable from #her)),
								if (:context, #null)
									else (assign :description to :context)
							));
						if (:description, #this)
							then (do (
								assign :context to (call #search on #Context with (get #variable from #this)),
								if (:context, #null)
									else (assign :description to :context)
							));
						if (:thing, #i)
							then (do (
								assign :pronoun to #true,
								assign :thing to (get #speaker from :input),
								if (:noun, "I")
									then (assign :noun to "you")
									else (if (:noun, "my")
										then (assign :noun to "your")
										else (assign :noun to #you)),
								if (:noun2, #null)
									then (do (
										if (:action, #is) and (:noun, "you")
											then (assign :verb to "are")
											else (assign :verb to :action)))
							));
						if (:thing, #you)
							then (do (
								assign :pronoun to #true;
								assign :thing to (get #target from :input),
								if (:noun, "you")
									then (assign :noun to "I")
									else (if (:noun, "your")
										then (assign :noun to "my")
										else (assign :noun to #i)),
								if (:noun2, #null)
									then (do (
										if (:action, #is) and (:noun, "I")
											then (assign :verb to "am")
											else (assign :verb to :action)))
							));
						if (:description, #i)
							then (do (
								assign :description to (get #speaker from :input),
								assign :adjective to #you
							));
						if (:description, #you)
							then (do (
								assign :description to (get #target from :input)
							));
						if (:description, #self)
							then (
								if (:adjective, "you")
									then (assign :adjective to "me")
									else (assign :adjective to #i)),
						if not (:adjectives, #null) and (:or, #null) and (:and, #null)
							then (do (
								assign :newAdjective to (word :adjectives),
								associate :newAdjective to #adjective by #instantiation,
								assign :newDescription to (new (#description)),
								associate :newDescription to #description by #instantiation,
								associate :newDescription to :description2 by #specialization,
								associate :newDescription to :newAdjective by #word,
								associate :newAdjective to :newDescription by #meaning,
								assign :description to :newDescription,
								assign :adjective to :newAdjective
							));
						if not (:a2, #null)
							then (do (
								if (:action, #is)
									then (assign :action to #instantiation)
									else (
										assign :description to (new (:description, #variable)))
							));
						call #push on #Context with :description,
						call #push on #Context with :thing,
						set #topic to :thing on :conversation,
						if not (:noun2, #null)
							then (
								// "What is my age?" else "Does my dog like you?"
								if (:action, #is)
									then (assign :action to :thing2)
									else (do (
										assign :existing to (get :thing2 from :thing),
										if (:existing, #null)
											then (do (
													assign :new to (new (:thing2, #thing)),
													associate :thing to :new by :thing2,
													assign :thing to :new
												))
											else (assign :thing to :existing))));
						if not (:thing2, #null)
							then (call #push on #Context with :thing2);
					}
					Function:understandingResponse {
						if (:isQuestion) or not (:or, #null)
							then (Function:questionResponse)
							else (do (
									Function:commonTense,
									if not (:description2, #null)
										then (do (
											if not (:isNot)
												then (do(												
													weak associate :thing to :description2 by :action with meta #tense as :tense
												)))),
									if (:isNot)
										then (dissociate :thing to :description by :action with meta #tense as :tense)
										else (associate :thing to :description by :action with meta #tense as :tense),
									assign :response to (new #sentence),
									append (random ("Okay, I will remember that", "I understand,", "I believe you that")) to #word of :response,
									if (:a, #null)
										else (append #the to #word of :response),
									if (:the, #null)
										else (append #the to #word of :response),
									append :noun to #word of :response with meta #type as :nountype,
									if (:quote, #null)
										else (do (
											append :quote to #word of :response,
											append "s" to #word of :response)),
									if not (:thing2, #null)
										then (if (:noun2, #self)
											then (append #i to #word of :response)
											else (append :noun2 to #word of :response)),
									if not (:do, #null)
										then (do (
												append :do to #word of :response;
												if (:isNot, #true)
													then (append :not to #word of :response),
											)),
									append :verb to #word of :response with meta #tense as :tense,
									if (:isNot, #true) and (:do, #null)
										then (append :not to #word of :response),
									if not (:a2, #null)
										then (append :a2 to #word of :response),
									append :adjective to #word of :response with meta #type as :adjectivetype,
									append #period to #word of :response
								));
					}
					Function:questionResponse {
						Function:commonTense,
						if (:thing, #null) or (:description, #null)
							then (return Function:whoWhatQuestionResponse);
						if not (:or, #null) or not (:and, #null)
							then (return Function:andOrQuestionResponse);
						assign :result to (is :thing related to :description by :action);
						assign :value to (get :action from :thing);
						if (:result, #unknown)
							then (do (
								assign :result2 to (is :thing related to :adjectiveOrig by :action);
								if (:result2, #unknown)
									then (assign :result3 to (is :thing related to :description by :action))
									else (do (
										assign :result to :result2,
										assign :adjective to :adjectiveOrig,
										assign :description to :adjectiveOrig));
								if (:result3, #true) or (:result3, #false)
									then (do (
										assign :result to :result3,
										assign :noun to :nounOrig,
										assign :thing to :nounOrig,
										assign :value to (get :action from :nounOrig)))));
						if (:value, #self)
							then (assign :value to #i);
						if (:value, :speaker)
							then (assign :value to #you);
						if (:isNot)
							then (assign :result to (not :result));
						assign :response to (new #sentence);
						if (:result, #unknown)
							then (if (:value, #null)
									then (append (random ("I understand, but am not sure if", "I understand the question, but have no idea if", "I'm not sure if")) to #word of :response)
									else (append (random ("I'm not certain, but I think", "I'm pretty sure that", "Perhaps, but I think")) to #word of :response))
							else (if (:result, #true)
									then (append (random ("That's right,", "You are correct,", "Yes, to my knowledge")) to #word of :response)
									else (append (random ("No,", "You are incorrect,", "No, to my knowledge")) to #word of :response));
						if not (:a, #null)
							then (append #the to #word of :response);
						if not (:the, #null)
							then (append #the to #word of :response),
						append :noun to #word of :response with meta #type as :nountype;
						if not (:quote, #null)
							then (do (
								append :quote to #word of :response;
								append "s" to #word of :response));
						if not (:noun2, #null)
							then (append :noun2 to #word of :response);
						if not (:do, #null)
							then (if (:result, #unknown)
									then (if not (:value, #null)
											then (assign :do-not to #true))
									else (if (:isNot)
											then (if (:result, #true)
													then (assign :do-not to #true))				
											else (if (:result, #false) and (:value, #null)
													then (assign :do-not to #true))));
						if (:do-not)
							then (do (
								append :do to #word of :response,
								append "not" to #word of :response)),
						append :verb to #word of :response with meta #tense as :tense;
						if (:isNot)
							then (do (
									if (:do, #null)
										then (if (:result, #true) or (:result, #unknown)
												then (append :not to #word of :response));
									if (:a2, #null)
										else (append :a2 to #word of :response),										
									if not (:result, #unknown) or (:value, #null)
										then (append :adjective to #word of :response with meta #type as :adjectivetype)))
							else (if (:result, #false)
									then (do (
											if (:value, #null)
												then (do (
														if (:do, #null)
															then (append "not" to #word of :response);
														if not (:a2, #null)
															then (append :a2 to #word of :response);
														append :adjective to #word of :response
													))
												else (do(
													if not (:a2, #null)
														then (append :a2 to #word of :response);
													append :value to #word of :response;
													append #comma to #word of :response;
													append "not" to #word of :response;
													if not (:a2, #null)
														then (append :a2 to #word of :response);
													append :adjective to #word of :response))
										))
									else (do (
										if not (:result, #true) and not (:value, #null)
											then (if (:do-not, #null)
													then (append "not" to #word of :response));
										if not (:a2, #null)
											then (append :a2 to #word of :response);
										if (:result, #true) or (:value, #null)
											then (append :adjective to #word of :response with meta #type as :adjectivetype))));
						if (:result, #unknown) and not (:value, #null)
							then (do (
								append :adjective to #word of :response with meta #type as :adjectivetype;
								append #comma to #word of :response;
								append "because I know that" to #word of :response;
								if not (:a, #null)
									then (append #the to #word of :response);
								if not (:the, #null)
									then (append #the to #word of :response),
								append :noun to #word of :response with meta #type as :nountype;
								if not (:quote, #null)
									then (do (
										append :quote to #word of :response;
										append "s" to #word of :response));
								if not (:noun2, #null)
									then (append :noun2 to #word of :response),
								append :verb to #word of :response with meta #tense as :tense;
								if not (:a2, #null)
									then (append :a2 to #word of :response);
								append :value to #word of :response));
						append #period to #word of :response;
					}
					Function:andOrQuestionResponse {
						assign :anyTrue to #false;
						assign :anyFalse to #false;
						assign :anyUnknown to #false;
						assign :trueValues to (new #list);
						assign :falseValues to (new #list);
						assign :unknownValues to (new #list);						
						for each #sequence of :descriptions as :description do (
							assign :result to (is :thing related to :description by :action);
							if (:result, #true)
								then (do (
									append :description to #sequence of :trueValues;
									assign :anyTrue to #true;
								));							
							if (:result, #false)
								then (do (
									append :description to #sequence of :falseValues;
									assign :anyFalse to #true;
								));						
							if (:result, #unknown)
								then (do (
									append :description to #sequence of :unknownValues;
									assign :anyUnknown to #true;
								));
						);
						assign :response to (new #sentence);
						if (:anyTrue) or (:anyFalse)
							then (do (
								if not (:and, #null) and (:anyFalse, #false) and (:anyUnknown, #false)
									then (do (
										if (:isNot)
											then (append "No" to #word of :response)
											else (append "Yes" to #word of :response);
										append "," to #word of :response;
									));
								if not (:and, #null) and (:anyFalse, #true)
									then (do (
										if (:isNot)
											then (append "Yes" to #word of :response)
											else (append "No" to #word of :response);
										append "," to #word of :response;
									));
								if not (:a, #null)
									then (append #the to #word of :response);
								if not (:the, #null)
									then (append #the to #word of :response),
								append :noun to #word of :response with meta #type as :nountype;
								if not (:quote, #null)
									then (do (
										append :quote to #word of :response;
										append "s" to #word of :response));
								if not (:noun2, #null)
									then (append :noun2 to #word of :response);
								append :verb to #word of :response with meta #tense as :tense;
								assign :first to #true;
										assign :last to (get #sequence from :trueValues at last 1);
								for each #sequence of :trueValues as :description do (
									if (:first)
										then (assign :first to #false)
										else (if (:description, :last)
											then (append "and" to #word of :response)
											else (append "," to #word of :response));
									append :description to #word of :response with meta #type as :adjectivetype;
								);
								if (:anyFalse)
									then (do (
										append "not" to #word of :response;
										assign :first to #true;
										assign :last to (get #sequence from :falseValues at last 1);
										for each #sequence of :falseValues as :description do (
											if (:first)
												then (assign :first to #false)
												else (if (:description, :last)
													then (append "or" to #word of :response)
													else (append "," to #word of :response));
											append :description to #word of :response with meta #type as :adjectivetype;
										);
									));
							));
						if (:anyUnknown)
							then (do (
								if (:anyTrue) or (:anyFalse)
									then (append "," to #word of :response);
								append (random ("I'm not sure if")) to #word of :response;
								if not (:a, #null)
									then (append #the to #word of :response);
								if not (:the, #null)
									then (append #the to #word of :response),
								append :noun to #word of :response with meta #type as :nountype;
								if not (:quote, #null)
									then (do (
										append :quote to #word of :response;
										append "s" to #word of :response));
								if not (:noun2, #null)
									then (append :noun2 to #word of :response);
								append :verb to #word of :response with meta #tense as :tense;
								assign :first to #true;
								assign :last to (get #sequence from :unknownValues at last 1);
								for each #sequence of :unknownValues as :description do (
									if (:first)
										then (assign :first to #false)
										else (if (:description, :last)
											then (append "or" to #word of :response)
											else (append "," to #word of :response));
									append :description to #word of :response with meta #type as :adjectivetype;
								)));
						append "." to #word of :response;
					}
					// Answers: "What is my name?", "what are you?"
					Function:whoWhatQuestionResponse {
					    if (:thing, #null)
						    then (assign :result to (related to :description by :action))
						    else (if (:noun2, #null)
        							then (do (
        								assign :result to (get :action from :thing),
        								if (:result, #null)
        									then (if (:action, #is)
        										then (do (
        											if (:noun, "up")
        												then (return #null),
        											if (:noun, "there")
        												then (return #null),
        											if (:pronoun, #true)
        												then (assign :result to :thing)
        												else (do (
        													assign :a2 to #a,
        													assign :result to (get #instantiation from :thing)))
        									)))))
        							else (assign :result to (all :thing2 from :thing))
						       );
						assign :response to (new #sentence);
						if (:result, #null)
							then (do (
								append (random ("I understand, but am not sure", "I understand the question, but have no idea", "I'm not sure")) to #word of :response;
								if not (:what, #null)
									then (append :what to #word of :response)
									else (if not (:who, #null)
										then (append :who to #word of :response)
										else (append "what" to #word of :response));
					            if (:thing, #null)
					                then (do (
        								append :verb to #word of :response;
        								append :adjective to #word of :response with meta #type as :nountype))
        							else (append :noun to #word of :response with meta #type as :nountype);
								if not (:quote, #null)
									then (do (
										append :quote to #word of :response;
										append "s" to #word of :response));
								if not (:noun2, #null)
									then (append :noun2 to #word of :response);
					            if not (:thing, #null)
    							    then (append :verb to #word of :response with meta #tense as :tense)))
							else (do (
								append (random ("I known that", "To my knowledge")) to #word of :response;
					            if (:thing, #null)
					                then (do (
					                    if not (:a2, #null)
								            then (append #a to #word of :response);
        								if (:result, #self)
        									then (append #i to #word of :response)
        									else (if (:result, :speaker)
        										then (append #you to #word of :response)
        										else (append :result to #word of :response)),
        								append :verb to #word of :response with meta #tense as :tense;
        								if not (:a, #null)
        									then (append #a to #word of :response);
        								if not (:the, #null)
        									then (append #the to #word of :response);
        								append :adjective to #word of :response with meta #type as :nountype;
        								if not (:quote, #null)
        									then (do (
        										append :quote to #word of :response;
        										append "s" to #word of :response));
        								if not (:noun2, #null)
									        then (append :noun2 to #word of :response)))
			                        else (do (
        					            if not (:a2, #null)
        								    then (append #a to #word of :response);
        								if not (:the, #null)
        									then (append #the to #word of :response);
        								append :noun to #word of :response with meta #type as :nountype,
        								if not (:quote, #null)
        									then (do (
        										append :quote to #word of :response,
        										append "s" to #word of :response)),
        								if not (:noun2, #null)
        									then (append :noun2 to #word of :response),
        								append :verb to #word of :response with meta #tense as :tense,
        								if not (:a2, #null)
        									then (append #a to #word of :response),
        								if (:result, #self)
        									then (append #i to #word of :response)
        									else (if (:result, :speaker)
        										then (append #you to #word of :response)
        										else (append :result to #word of :response))));
								));
						append #period to #word of :response;
						return :response;
					}
				}
			}
			// 'I am very very very nice'
			State:nounVerbAdjectiveAdjectiveState {
				do (
					if (:adjectives, #null)
						then (do (
							assign :adjectives to (new #list);
							assign :descriptions to (new #list);
							append :adjective to #sequence of :adjectives;
							append :description to #sequence of :descriptions;
						));
					append :adjective2 to #sequence of :adjectives;
					append :description2 to #sequence of :descriptions;
				);
				goto (State:nounVerbAdjectiveState);

			}
			// 'am I very very very nice?'
			State:verbNounAdjectiveAdjectiveState {
				do (
					if (:adjectives, #null)
						then (do (
							assign :adjectives to (new #list);
							assign :descriptions to (new #list);
							append :adjective to #sequence of :adjectives;
							append :description to #sequence of :descriptions;
						));
					append :adjective2 to #sequence of :adjectives;
					append :description2 to #sequence of :descriptions;
				);
				goto (State:verbNounAdjectiveState);

			}
			// 'My age...'
			State:nounNounState {
				case :verb goto State:nounVerbState;
				case :questionmark goto State:whatIsNounNounState;
			}
			// 'Bob tall...'
			State:nounAdjectiveState {
				do (
					assign :verb to #is,
					assign :action to #is
				);
				goto (State:nounVerbAdjectiveState);
			}
		}
		
		// 'Is...'
		State:verbState {

			case :the goto State:verbState;
			
			case :a goto State:verbState;

			case :nounPossessive goto State:verbNounQuoteState;
			case :noun goto State:verbNounState;
			
			// 'Is sky's...'
			State:verbNounQuoteState {
				do (if (:nounPossessive, #null)
						else (assign :noun to :nounPossessive));
				case :quote goto State:verbNounQuoteState;
				case "s" goto State:verbNounQuoteState;
				
				case :noun2 goto State:verbNounNounState;
				
				// adjective - "Is sky's green car"
			}
			
			// 'Is sky...'
			State:verbNounState {
				case :quote goto State:verbNounQuoteState;
				
				case :not goto State:notState;
				
				case :a2 goto State:verbNounAState;

				case :adjective goto State:verbNounAdjectiveState;
				case :typenoun goto State:verbNounATypeState;

				// 'Is sky blue'
				State:verbNounAdjectiveState {
					case :questionWord goto State:questionState;
					case :or goto State:verbNounAdjectiveState;
					case :and goto State:verbNounAdjectiveState;
					case "," goto State:verbNounAdjectiveState;
					case :adjective2 goto State:verbNounAdjectiveAdjectiveState;
					case :punctuation goto State:verbNounAdjectiveState;

					Answer:Function:questionResponse;
				}
				// 'Is sky a...'
				State:verbNounAState {
					case :typenoun goto State:verbNounATypeState;
				}
				// 'Is sky a thing'
				State:verbNounATypeState {
					do (
						assign :adjective to :typenoun,
						assign :description to :type
					);
					goto (State:verbNounAdjectiveState);
				}
				// 'Is my car...' -> 'Is my car red', 'Is my car a car', 'Is my age 44'
				State:verbNounNounState {
					case :a2 goto State:verbNounAState;
					case :adjective goto State:verbNounAdjectiveState;
					case :noun3 goto State:verbNounNounNounState;
					
					:noun3 {
						set #meaning to :thing3;
					}
					:thing3 {
						set #instantiation to #thing;
					}
				}
				// 'Is my age 44'
				State:verbNounNounNounState {
					do (
						assign :adjective to :noun3,
						assign :description to :thing3
					);
					goto (State:verbNounAdjectiveState);
				}
			}
		
			// 'What...'
			State:whatState {
				case :do goto State:whatDoState;
				case :is goto State:whatIsState;
				case :verb goto State:whatVerbState;
				case :quote0 goto State:WhatQuoteState;				
		
				:is {
					set #meaning to #is;
				}
				
				// 'What'...'
				State:WhatQuoteState {
					case "s" goto State:WhatQuoteSState;
				}
				
				// 'What's...'
				State:WhatQuoteSState {
					do (
						assign :is to "is"
					);
					goto (State:whatIsState);
				}
		
				// 'What is...'
				// TODO: "what is blue" -> "What things are blue" vs "what does blue mean"
				State:whatIsState {					
					do (
						assign :verb to :is,
						assign :action to #is
					);
		
					case :the goto State:whatIsState;
					case :a goto State:whatIsState;
					case :nounPossessive goto State:whatIsNounQuoteState;
					case :noun goto State:whatIsNounState;
					
					// 'What is sky'
					State:whatIsNounState {
						case :quote goto State:whatIsNounQuoteState;
						case :punctuation goto State:whatIsNounState;
		
						Answer:Function:questionResponse;
					}
					
					// 'What is sky's'
					State:whatIsNounQuoteState {
						do (if (:nounPossessive, #null)
								else (assign :noun to :nounPossessive));
						case "s" goto State:whatIsNounQuoteState;
						case :noun2 goto State:whatIsNounNounState;
						case :punctuation goto State:whatIsNounState;
		
						Answer:Function:questionResponse;
					}
					
					// 'What is my name', "What is Obama's job?"
					State:whatIsNounNounState {
						do (if (:verb, #null)
								then (do (assign :action to #is, assign :verb to #is)));
						case :punctuation goto State:whatIsNounNounState;
						
						Answer:Function:questionResponse;
					}
				}
				
				// 'What loves...'
				State:whatVerbState {
					case :the goto State:whatVerbState;
					case :a goto State:whatVerbState;
		
					case :adjective goto State:whatVerbAdjectiveState;
					case :noun3 goto State:whatVerbNounState;
					
					
					// 'What is fast', 'What loves red'
					State:whatVerbAdjectiveState {
						case :punctuation goto State:whatVerbAdjectiveState;
		
						Answer:Function:questionResponse;
					}
					
					// 'Who loves me'
					State:whatVerbNounState {
						do (
							assign :adjective to :noun3;
							assign :description to :thing3;
						);
						goto (State:whatVerbAdjectiveState);
					}
				}
				
				// 'What do...'
				State:whatDoState {
		
					case :the goto State:whatDoState;
					
					case :a goto State:whatDoState;
		
					case :noun goto State:whatDoNounState;
					
					// 'What do I...'
					State:whatDoNounState {
						case :verb goto State:whatDoNounVerbState;
					}
					
					// 'What do I like'
					State:whatDoNounVerbState {
						case :punctuation goto State:whatDoNounVerbState;
						
						Answer:Function:questionResponse;
					}
				}
			}
		}
	}
}

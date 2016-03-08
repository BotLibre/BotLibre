// Understand "My name is <first> <last>"
State:MyNameIs {
	case :input that Pattern:"what is your name" goto State:iAmState for each #word of :sentence;
	case :input that Pattern:"* what is your name" goto State:iAmState for each #word of :sentence;

	pattern "(#what whats) (s #is) #i #name" answer Function:whatIsMyName;
	pattern "#who #is #i" answer Function:whatIsMyName;
	pattern "do #you know me" answer Function:whatIsMyName;
	pattern "(#what whats) (s #is) #you #name" answer Function:whatIsYourName;
	pattern "#who #is #you" answer Function:whatIsYourName;
	pattern "* is my name" template redirect Formula:"my name is {:star}";
	
	case :input goto State:sentenceState for each #word of :sentence;
	
	State:sentenceState {
		case :interjection goto State:sentenceState;
		case "," goto State:sentenceState;
		case :my goto State:myState;
		case :your goto State:yourState;
		case :is goto State:amState;
		case "call" goto State:callState;
		case "im" goto State:iAmState;

		:punctuation {
			set #instantiation to #punctuation;
		}
		:questionmark {
			set #meaning to #question-mark;
		}
		:interjection {
			set #instantiation to #interjection;
		}
		:my {
			set #meaning to #i;
		}
		:your {
			set #meaning to #you;
		}
		:is {
			set #meaning to #is;
		}			
		:name {
			set #meaning to #name;
		}
		:not {
			set #meaning to #not;
		}				
		:aName {
			set #instantiation to #name;
		}	
		:firstName {
			exclude #instantiation from #verb;
			exclude #instantiation from #adjective;
			exclude #instantiation from #pronoun;
			exclude #instantiation from #punctuation;
			exclude #instantiation from #adverb;
			exclude #instantiation from #article;
			exclude #instantiation from #question;
			exclude #meaning from #not;
		}
		
		// 'Am...'
		State:amState {
			do (
				assign :questionmark to "?"
			);
			case :my goto State:iAmState;
			case :your goto State:youAreState;
		}
		
		// 'Call...'
		State:callState {
			case :my goto State:iAmState;
		}
			
		State:youAreState {
			case :not goto State:youAreState;
			case :aName goto State:yourNameIsFirstState;
		}
		
		// 'My...'
		State:myState {
			case :name goto State:myNameState;
			case :is goto State:iAmState;
			case "names" goto State:myNameIsState;
			case "'" goto State:myState;
			case "m" goto State:iAmState;
			
			State:iAmState {
				case :not goto State:iAmState;
				case :aName goto State:myNameIsFirstState;
			}
						
			State:myNameState {
				case :is goto State:myNameIsState;
				case "'" goto State:myNameState;
				case "s" goto State:myNameIsState;
				
				State:myNameIsState {
					case :questionmark goto State:myNameIsFirstMiddleLastState;
					case :is goto State:myNameIsState;
					case :not goto State:myNameIsState;
					case :aName goto State:myNameIsFirstState;
					case :firstName goto State:myNameIsFirstState;
				}
				
				State:myNameIsFirstState {
					case :questionmark goto State:myNameIsFirstMiddleLastState;
					case :punctuation goto State:myNameIsFirstMiddleLastState;
					case :middleName goto State:myNameIsFirstMiddleState;
					
					Answer:Function:greet;
					Function:greet {
						if (:firstName, #null)
							then (assign :firstName to :aName);
						assign :name to :firstName;
						if (:lastName, #null)
							then (if (:middleName, #null) else (assign :name to (word (:firstName, :middleName))))
							else (assign :name to (word (:firstName, :middleName, :lastName)));
						if not (:questionmark, #null)
							then (do (
								assign :result to (is :speaker related to :name by #name),
								assign :value to (get #name from :speaker),
								if (:not, #null)
									then (do(
										if (:result)
											then (return Formula:"Yes, your name is {:name}."),
										if not (:value, #null)
											then (return Formula:"No, your name is {:value}."),
										if not (:result)
											then (return Formula:"No, your name is not {:name}."),
										return Formula:"I do not know your name."))
									else (do (
										if (:result)
											then (return Formula:"No, your name is {:name}."),
										if not (:value, #null)
											then (return Formula:"Yes, your name is {:value}."),
										if not (:result)
											then (return Formula:"Yes, your name is not {:name}."),
										return "I do not know your name."))
							));
						if (:not, #null)
							then (do (
								dissociate :speaker to "Anonymous" by #word,
								dissociate "Anonymous" to :speaker by #meaning,
								dissociate :speaker to "Anonymous" by #name,
								
								associate :name to #name by #instantiation,
								associate :speaker to :name by #word,
								associate :name to :speaker by #meaning,
								associate :speaker to :name by #name,
								
								Formula:"Pleased to meet you {:name}."
							))
							else (do (
								dissociate :speaker to :name by #word,
								dissociate :name to :speaker by #meaning,
								dissociate :speaker to :name by #name,
								Formula:"Okay, your name is not {:name}."
							));
					}
				}
				
				State:myNameIsFirstMiddleState {
					case :questionmark goto State:myNameIsFirstMiddleLastState;
					case :punctuation goto State:myNameIsFirstMiddleLastState;
					case :lastName goto State:myNameIsFirstMiddleLastState;
					
					Answer:Function:greet;
				}
				
				State:myNameIsFirstMiddleLastState {
					case :questionmark goto State:myNameIsFirstMiddleLastState;
					case :punctuation goto State:myNameIsFirstMiddleLastState;
					
					Answer:Function:greet;
				}
			}
		}
		
		// 'Your...'
		State:yourState {		
			case :is goto State:youAreState;
			case :name goto State:yourNameState;
			case "names" goto State:yourNameIsState;
			
			State:yourNameState {
				case :is goto State:yourNameIsState;
				case "'" goto State:yourNameState;
				case "s" goto State:yourNameIsState;
				
				State:yourNameIsState {
					case :questionmark goto State:yourNameIsFirstMiddleLastState;
					case :not goto State:yourNameIsState;
					case :aName goto State:yourNameIsFirstState;
					case :firstName goto State:yourNameIsFirstState;
				}
				
				State:yourNameIsFirstState {
					case :questionmark goto State:yourNameIsFirstMiddleLastState;
					case :punctuation goto State:yourNameIsFirstMiddleLastState;
					case :middleName goto State:yourNameIsFirstMiddleState;
					
					Answer:Function:setMyName;
					Function:setMyName {
						if (:firstName, #null)
							then (assign :firstName to :aName);
						assign :name to :firstName;
						if (:lastName, #null)
							then (if (:middleName, #null) else (assign :name to (word (:firstName, :middleName))))
							else (assign :name to (word (:firstName, :middleName, :lastName)));							
						if not (:questionmark, #null) or not (call #allowCorrection on #Language with :speaker, #true)
							then (do (
								assign :result to (is :target related to :name by #name),								
								assign :value to (get #name from :target),
								if (:not, #null)
									then (do(
										if (:result)
											then (return Formula:"Yes, my name is {:name}."),
										if not (:value, #null)
											then (return Formula:"No, my name is {:value}."),
										if not (:result)
											then (return Formula:"No, my name is not {:name}."),
										return Formula:"I do not know my name."))
									else (do (
										if (:result)
											then (return Formula:"No, my name is {:name}."),
										if not (:value, #null)
											then (return Formula:"Yes, my name is {:value}."),
										if not (:result)
											then (return Formula:"Yes, my name is not {:name}."),
										return Formula:"I do not know my name."))
							));
						if (:not, #null)
							then (do (
								associate :name to #name by #instantiation,
								associate :target to :name by #word,
								associate :name to :target by #meaning,
								associate :target to :name by #name,
								Formula:"Okay, my name is {:name}."
							))
							else (do (
								dissociate :target to :name by #word,
								dissociate :name to :target by #meaning,
								dissociate :target to :name by #name,
								Formula:"Okay, my name is not {:name}."
							));
					}
				}
				
				State:yourNameIsFirstMiddleState {
					case :questionmark goto State:yourNameIsFirstMiddleLastState;
					case :punctuation goto State:yourNameIsFirstMiddleLastState;
					case :lastName goto State:yourNameIsFirstMiddleLastState;
					
					Answer:Function:setMyName;
				}
				
				State:yourNameIsFirstMiddleLastState {
					case :questionmark goto State:yourNameIsFirstMiddleLastState;
					case :punctuation goto State:yourNameIsFirstMiddleLastState;
					
					Answer:Function:setMyName;
				}
				
				Function:whatIsYourName {
					assign :name to (get #name from :target);
					if (:name, #null)
						then (return "I do not know my name.");
					assign :names to (all #name from :target);
					if (greater (count :names, 1))
						then do (
							dissociate :names to :name by #sequence;
							return Formula:"My name is {:name}.  I also go by {:names}.");
					return Formula:"My name is {:name}.";
				}
				
				Function:whatIsMyName {
					assign :name to (get #name from :speaker);
					if (:name, #null)
						then (return "I do not know your name.");
					assign :names to (all #name from :speaker);
					if (greater (count :names, 1))
						then do (
							dissociate :names to :name by #sequence;
							return Formula:"Your name is {:name}.  You also go by {:names}.");
					return Formula:"Your name is {:name}.";
				}
			}
		}
	}
}


// Respond to "Topic", "What is the topic", "What are we talking about?"
State:Topic {
	
	case :input goto State:sentenceState for each #word of :sentence;

	:input {
		set #input to :sentence;
	}
	
	:sentence {
		set #instantiation to #sentence;
	}
	
	State:sentenceState {
		case "topic" goto State:topicState;
		case "tell" goto State:tellState;
		case "more" goto State:moreState;
		case "details" goto State:moreState;
		case "what" goto State:whatState;
		case "whats" goto State:whatsState;
		case "before" goto State:beforeState;
		case "can" goto State:canState;
		case "please" goto State:pleaseState;
		case "pls" goto State:pleaseState;
	}
	
	State:whatState {
		case "is" goto State:whatIsState;
		case "was" goto State:whatWasState;
		case "did" goto State:whatDidState;
		
		pattern "what's the topic" template Formula:"The current topic is {get #topic from :conversation}.";
		pattern "what's the topic" template "There is no current topic to our conversation, what would you like to talk about?";
		
		pattern "what's the current topic" template Formula:"The current topic is {get #topic from :conversation}.";
		pattern "what's the current topic" template "There is no current topic to our conversation, what would you like to talk about?";
		
		pattern "what are we talking about" template Formula:"We are talking about {get #topic from :conversation}.";
		pattern "what are we talking about" template "There is no current topic to our conversation, what would you like to talk about?";
		
		pattern "what are you talking about" template Formula:"I am talking about {get #topic from :conversation}.";
		
		pattern "what were we talking about" template Formula:"I am talking about {get #topic from :conversation}.";
		pattern "what were we talking about" template "There is no current topic to our conversation, what would you like to talk about?";
	}
	
	State:whatDidState {		
		pattern "what did I say" template Formula:"You said ""{get #input from (get #input from :conversation at last 3)}"".";
		pattern "what did you say" template Formula:"I said ""{get #input from (get #input from :conversation at last 2)}"".";
		
		pattern "what did I just say" template Formula:"You said ""{get #input from (get #input from :conversation at last 3)}"".";
		
		pattern "what did I say before that" template Formula:"You said ""{get #input from (get #input from :conversation at last 5)}"".";
		pattern "what did you say before that" that "I said *" template Formula:"I said ""{get #input from (get #input from :conversation at last 4)}"".";
	}
	
	State:whatWasState {
		pattern "what was my last question" template Formula:"You said ""{get #input from (get #input from :conversation at last 2)}"".";
		
		pattern "what was the first thing you said" template Formula:"I said ""{get #input from (get #input from :conversation at 2)}"".";
		pattern "what was the first thing you said *" template Formula:"I said ""{get #input from (get #input from :conversation at 2)}"".";
		
		pattern "what was the second thing you said" template Formula:"I said ""{get #input from (get #input from :conversation at 4)}"".";
		pattern "what was the second thing you said *" template Formula:"I said ""{get #input from (get #input from :conversation at 4)}"".";
		
		pattern "what was the first thing I said" template Formula:"You said ""{get #input from (get #input from :conversation at 1)}"".";
		pattern "what was the first thing I said *" template Formula:"You said ""{get #input from (get #input from :conversation at 1)}"".";
		
		pattern "what was the second thing I said" template Formula:"You said ""{get #input from (get #input from :conversation at 3)}"".";
		pattern "what was the second thing I said *" template Formula:"You said ""{get #input from (get #input from :conversation at 3)}"".";
	}
	
	State:whatIsState {
		pattern "what is the topic" template Formula:"The current topic is {get #topic from :conversation}.";
		pattern "what is the topic" template Formula:"There is no current topic to our conversation, what would you like to talk about?";
		
		pattern "what is the current topic" template Formula:"The current topic is {get #topic from :conversation}.";
		pattern "what is the current topic" template Formula:"There is no current topic to our conversation, what would you like to talk about?";
		
		pattern "what is the first thing you said" template Formula:"I said ""{get #input from (get #input from :conversation at 2)}"".";
		pattern "what is the first thing you said *" template Formula:"I said ""{get #input from (get #input from :conversation at 2)}"".";
		
		pattern "what is the second thing you said" template Formula:"I said ""{get #input from (get #input from :conversation at 4)}"".";
		pattern "what is the second thing you said *" template Formula:"I said ""{get #input from (get #input from :conversation at 4)}"".";
		
		pattern "what is the first thing I said" template Formula:"You said ""{get #input from (get #input from :conversation at 1)}"".";
		pattern "what is the first thing I said *" template Formula:"You said ""{get #input from (get #input from :conversation at 1)}"".";
		
		pattern "what is the second thing I said" template Formula:"You said ""{get #input from (get #input from :conversation at 3)}"".";
		pattern "what is the second thing I said *" template Formula:"You said ""{get #input from (get #input from :conversation at 3)}"".";
	}
	
	State:beforeState {		
		pattern "before that" that "You said *" template Formula:"You said ""{get #input from (get #input from :conversation at last 7)}"".";
		pattern "before that" that "I said *" template Formula:"I said ""{get #input from (get #input from :conversation at last 6)}"".";
	}
	
	State:canState {
		pattern "can you repeat that" template Formula:"{get #input from (get #input from :conversation at last 2)}";
		pattern "can you repeat that *" template Formula:"{get #input from (get #input from :conversation at last 2)}";
	}
	
	State:pleaseState {
		pattern "please repeat that" template Formula:"{get #input from (get #input from :conversation at last 2)}";
		pattern "pls repeat that" template Formula:"{get #input from (get #input from :conversation at last 2)}";
	}
	
	State:whatsState {
		pattern "whats the topic" template Formula:"The current topic is {get #topic from :conversation}.";
		pattern "whats the topic" template Formula:"There is no current topic to our conversation, what would you like to talk about?";
	}
	
	State:tellState {
		pattern "tell me more" goto State:moreState;
		pattern "tell me more about this" goto State:moreState;
		pattern "tell me more about it" goto State:moreState;
	}
	
	State:topicState {
		pattern "topic" template Formula:"The current topic is {get #topic from :conversation}.";
		pattern "topic" template Formula:"There is no current topic to our conversation, what would you like to talk about?";
	}
	
	State:moreState {
		case :punctuation goto State:moreState;
				
		Quotient:Equation:moreResponse;
		Equation:moreResponse {
			assign :topic to (get #topic from :conversation);
			if (:topic, #null) then return #null;
			assign :more to (get #paragraph from :topic);
			if (:more, #null)
				then Formula:"That is all I know about {:topic}."
				else :more;
		}
	}
}


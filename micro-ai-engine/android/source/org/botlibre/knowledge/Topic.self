// Respond to "Topic", "What is the topic", "What are we talking about?"
state Topic {
	pattern "what's the (current) topic" template Template("The current topic is {conversation.topic}.");
	pattern "what's the (current) topic" template "There is no current topic to our conversation, what would you like to talk about?";
	
	pattern "what (were are) we talking about" template Template("We are talking about {conversation.topic}.");
	pattern "what (were are) we talking about" template "There is no current topic to our conversation, what would you like to talk about?";
	
	pattern "what are you talking about" template Template("I am talking about {conversation.topic}.");

	pattern "what did I (just) say" template Template("You said \"{conversation.getLast(#input, 3).input}\".");
	pattern "what did you say" template Template("I said \"{conversation.getLast(#input, 2).input}\".");
	
	pattern "what did I say before that" template Template("You said \"{conversation.getLast(#input, 5).input}\".");
	pattern "what did you say before that" template Template("I said \"{conversation.getLast(#input, 7).input}\".");

	pattern "what (was is) my last question" template Template("You said \"{conversation.getLast(#input, 3).input}\".");
	
	pattern "what (was is) the first thing you said ^" template Template("I said \"{conversation.input[1].input}\".");
	
	pattern "what was the second thing you said ^" template Template("I said \"{conversation.input[3].input}\".");
	
	pattern "what (was is) the first thing I said ^" template Template("You said \"{conversation.input[0].input}\".");
	
	pattern "what (was is) the second thing I said ^" template Template("You said \"{conversation.input[2].input}\".");

	pattern "what is the (current) topic" template Template("The current topic is {conversation.topic}.");
	pattern "what is the (current) topic" template "There is no current topic to our conversation, what would you like to talk about?";
		
	pattern "before that" that "You said *" template Template("You said \"{conversation.getLast(#input, 7).input}\".");
	pattern "before that" that "I said *" template Template("I said \"{conversation.getLast(#input, 6).input}\".");
	
	pattern "(please pls) (can) (you) repeat that ^" template Template("{conversation.getLast(#input, 2).input}");		
	pattern "(please pls) (can) (you) repeat" template Template("{conversation.getLast(#input, 2).input}");

	pattern "whats the topic" template Template("The current topic is {conversation.topic}.");
	pattern "whats the topic" template "There is no current topic to our conversation, what would you like to talk about?";

	pattern "details" template detailsResponse();
	
	pattern "(tell) (me) more (about) (this it)" template moreResponse();
	pattern "(tell) (me) more" template moreResponse();
	
	pattern "topic" template Template("The current topic is {conversation.topic}.");
	pattern "topic" template "There is no current topic to our conversation, what would you like to talk about?";
		
	function moreResponse() {
		topic = conversation.topic;
		if (topic == null) {
			return null;
		}
		more = topic.paragraph;
		if (more == null) {
			Template("That is all I know about {topic}.");
		} else {
			more;
		}
	}
	
	function detailsResponse() {
		topic = conversation.topic;
		if (topic == null) {
			return null;
		}
		return Language.details(topic);
	}
}


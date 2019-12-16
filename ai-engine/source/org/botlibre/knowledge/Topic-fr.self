// Respond to "Topic", "What is the topic", "What are we talking about?"
state Topic {
	pattern "quel est le sujet (actuel)" template currentTopic();
	pattern "quel est le sujet (actuel)" template noTopic();
	
	pattern "de quoi parle-t-on" template currentTopic();
	pattern "de quoi parle-t-on" template noTopic();
	
	pattern "qu'est-ce que tu racontes" template currentTopic();

	pattern "qu'est-ce que j'ai dis" template Template("Vous avez dit \"{conversation.getLast(#input, 3).input}\".");
	pattern "qu'est-ce que vous avez dit" template Template("J'ai dit \"{conversation.getLast(#input, 2).input}\".");
	
	pattern "qu'est-ce que j'ai dit avant" template Template("Vous avez dit \"{conversation.getLast(#input, 5).input}\".");
	pattern "qu'as-tu dit avant ça" template Template("J'ai dit \"{conversation.getLast(#input, 7).input}\".");

	pattern "quelle était ma dernière question" template Template("Vous avez dit \"{conversation.getLast(#input, 3).input}\".");
	
	pattern "quelle a été la première chose que vous avez dite ^" template Template("J'ai dit \"{conversation.input[1].input}\".");
	
	pattern "quelle est la deuxième chose que vous avez dite ^" template Template("J'ai dit \"{conversation.input[3].input}\".");
	
	pattern "quelle était la première chose que j'ai dite ^" template Template("Vous avez dit \"{conversation.input[0].input}\".");
	
	pattern "quelle était la deuxième chose que j'ai dite ^" template Template("Vous avez dit \"{conversation.input[2].input}\".");
		
	pattern "avant ça" that "Vous avez dit *" template Template("Vous avez dit \"{conversation.getLast(#input, 7).input}\".");
	pattern "avant ça" that "J'ai dit *" template Template("J'ai dit \"{conversation.getLast(#input, 6).input}\".");
	
	pattern "peux-tu répéter (cela)" template Template("{conversation.getLast(#input, 2).input}");

	pattern "détails" template detailsResponse();
	
	pattern "dis m'en plus sur le sujet" template moreResponse();
	pattern "(dis) (m) (en) plus" template moreResponse();
	
	pattern "sujet" template currentTopic();
	pattern "sujet" template noTopic();
	
	function currentTopic() {
		return Template("Le sujet actuel est {conversation.topic}.");
	}
	
	function noTopic() {
		return Template("Il n'y a pas de sujet d'actualité dans notre conversation, de quoi voudriez-vous parler?");
	}
	
	function moreResponse() {
		topic = conversation.topic;
		if (topic == null) {
			return null;
		}
		more = topic.paragraph;
		if (more == null) {
			Template("C'est tout ce que je sais sur {topic}.");
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


// Respond to "Where is x", "Who country is x in" questions using Wikidata
state WhereIs {
	case input goto sentenceState for each #word of sentence;
	
	pattern "quel est l'emplacement de *" answer discoverWikidata();
	pattern "dans quel pays est *" answer discoverWikidata();
	pattern "dans quel continent est *" answer discoverWikidata();
	pattern "dans quel état est *" answer discoverWikidata();
	pattern "dans quelle province est *" answer discoverWikidata();
	pattern "où est-ce" answer checkIt();
	pattern "(google recherche trouver) où est *" answer discoverWikidata();
	pattern "* est où" answer discoverWikidata();
	
	function discoverWikidata() {
		// Ignore long sentences, problem not a specific thing.
		if (star.size(#word) > 4) {
			return null;
		}
		// Ignore certain types of words, pronouns/etc.
		for (word in star.word) {
			if (word.has(#instantiation, #pronoun) || word.has(#instantiation, #question) || word.has(#instantiation, #numeral)) {
				return null;
			}
		}
		result = Wikidata.search("pays", star.toString()),
		if (result == null) {
			return null;
		}
		conversation.topic = result;
		location = result.pays;
		if (location == null) {
			return null;
		}
		Context.push(location);
		Context.push(result);
		conversation.topic = result;
		
		return Template("{result} est au {location}.");
	}
	
	function checkIt() {
		result = Context.top();
		if (result == null) {
			return null;
		}
		location = result.containedby;
		if (location == null) {
			result = Wikidata.search("pays", result.word);
			location = result.pays;
			if (location == null) {
				return null;
			}
		}
		Context.push(location);
		Context.push(result);
		conversation.topic = result;
		Template("C'est au {location}.");
	}
}

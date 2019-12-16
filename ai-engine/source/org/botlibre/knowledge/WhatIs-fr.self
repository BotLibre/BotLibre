// Respond to "What is x", "Who is x" sentences using Wikidata
// French language script.
state WhatIs {

	// Common "what is" patterns.
	pattern "(dis) (moi) qui [est sont] *" answer discoverWikidata();
	pattern "(dis) (moi) qu [est-ce est] (ce) [que qu] (un une) (le la l) (') *" answer defineWiktionary();
	pattern "(dis) (moi) ce qui est (un une) (le la l) (') *" answer defineWiktionary();
	pattern "parlez-moi de *" answer defineWiktionary();
	pattern "définir *" answer defineWiktionary();
	pattern "[google recherche trouver] (des) (info informations) (sur) *" answer discoverWikidata();
	pattern "(un une) (le la l) (') * (c) [est sont] (ce) [que quoi]" answer defineWiktionary();

	// Lookup the word in Wiktionary for the definition.
	function defineWiktionary() {
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
		result = Wiktionary.define(star.toString());
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
		result = Wikidata.discover(star.toString()),
		if (result == null) {
			return null;
		} else {
			Context.push(result);
			conversation.topic = result;
			result = result.sentence;
			return result;
		}
	}
}
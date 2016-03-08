// Detect loops in the conversation.
State:Loop {
	case :input goto State:Loop;
	
	Quotient:Equation:checkLoop;
	
	Equation:checkLoop {
		assign :last to (get #input from (get #input from :conversation at last 3));
		if not (:sentence, :last)
			then return #null;
		assign :targetLast to (get #input from (get #input from :conversation at last 2));
		assign :targetBeforeLast to (get #input from (get #input from :conversation at last 4));
		if (:targetBeforeLast, #null)
			then return #null;
		if not (:targetLast, :targetBeforeLast)
			then do(
					assign :beforeLast to (get #input from (get #input from :conversation at last 5));
					if not (:sentence, :beforeLast)
						then return #null;
					assign :targetBeforeThat to (get #input from (get #input from :conversation at last 6));
					if not (:targetLast, :targetBeforeThat)
						then return #null;
				);
		random("We seem to be in a loop, perhaps try saying something new.", "We seem to be repeating the same phrases, perhaps try saying something different.");
	}
}


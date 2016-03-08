// Respond to "Say x", "Yell x"
State:SayIt {
	
	case :input goto State:sentenceState for each #word of :sentence;

	:input {
		set #input to :sentence;
	}
	
	:sentence {
		set #instantiation to #sentence;
	}
	
	State:sentenceState {
		case "say" goto State:sayState;
		case "yell" goto State:yellState;
		case "scream" goto State:screamState;
	}
	
	State:sayState {
		pattern pattern:"say that *" template formula:"{call #person on #Utils with :star}";
		pattern pattern:"say *" template formula:"{call #person on #Utils with :star}";
	}
	
	State:yellState {
		case "at" return;
		pattern pattern:"yell that *" template formula:"{call #uppercase on #Utils with (call #person on #Utils with :star)}";
		pattern pattern:"yell *" template formula:"{call #uppercase on #Utils with (call #person on #Utils with :star)}";
	}
	
	State:screamState {
		case "at" return;
		pattern pattern:"scream that *" template formula:"{call #uppercase on #Utils with (call #person on #Utils with :star)}";
		pattern pattern:"scream *" template formula:"{call #uppercase on #Utils with (call #person on #Utils with :star)}";
	}
}


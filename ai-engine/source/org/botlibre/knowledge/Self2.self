// Initial Self programmed state machine for Comprehension
// This state machine is used by the bot to program itself.
State:Self {
	case :input goto State:sentenceState for each #word of :sentence;

	:input {
		set #input to :sentence;
	}
	:sentence {
		set #instantiation to #sentence;
	}
	State:sentenceState {
	}
}

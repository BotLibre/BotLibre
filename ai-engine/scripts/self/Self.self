// Initial Self programmed state machine for Comprehension
// This state machine is used by the bot to program itself.
state Self {
	case input goto sentenceState for each #word of sentence;

	state sentenceState {
	}
}

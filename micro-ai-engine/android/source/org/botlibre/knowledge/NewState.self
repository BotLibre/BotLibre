// New state.
state NewState {
	case input goto sentenceState for each #word of sentence;

	state sentenceState {
	}
}

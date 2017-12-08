/**
 * Detect loops in the conversation.
 * Loop state will ignore the input and always evaluate the checkLoop function that checks if the conversation is in a loop.
 * If no loop is detected, null is returned to continue processing the next state.
 */
state Loop {
	// Always invoke the function to check for loops.
	case input answer checkLoop();
	
	function checkLoop() {
		var last = (conversation.getLast(#input, 3)).input;
		// Check if current input equals the last user input.
		if (sentence != last) {
			return null;
		}
		var targetLast = (conversation.getLast(#input, 2)).input;
		var targetBeforeLast = (conversation.getLast(#input, 4)).input;
		if (targetBeforeLast == null) {
			return null;
		}
		// Check if bot's last input equals the previous input.
		if (targetLast != targetBeforeLast) {
			// Check for double repeats.
			var beforeLast = (conversation.getLast(#input, 5)).input;
			if (sentence != beforeLast) {
				return null;
			}
			var targetBeforeThat = (conversation.getLast(#input, 6)).input;
			if (targetLast != targetBeforeThat) {
				return null;
			}
		}
		random ("We seem to be in a loop, perhaps try saying something new.", "We seem to be repeating the same phrases, perhaps try saying something different.");
	}
}

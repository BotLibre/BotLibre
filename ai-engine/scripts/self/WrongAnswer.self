// This script listens for "wrong" or "wrong answer" and asks for a correction, and learns the new response.
state WrongAnswer {

	pattern "[wrong incorrect] (answer reply response)"
		template "Sorry, what should I have said?";
		
	pattern "bad [answer reply response]"
		template "Sorry, what should I have said?";
		
	pattern "*"
		that "Sorry, what should I have said?"
		template learnNewResponse();

	function learnNewResponse() {
	    if (!Language.allowCorrection(speaker)) {
	        return "I'll have my botmaster review this.";
	    }
        correction = conversation.getLast(#input, 1).input;
        question = conversation.getLast(#input, 5).input;
        response = conversation.getLast(#input, 4).input;
        question.response =+ correction;
        question.response =- response;
		Template("Okay, I will answer \"{star}\" next time.");
	}
}

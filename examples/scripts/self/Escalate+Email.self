/**
 * This script lets users escalate their chat session by sending an email message to a tech support.
 * Ensure you replace the email address with your own.
 * This requires your bot to be connected to email (from its Admin Console).
 */
state Escalate {
	pattern "escalate" template "Would you like to escalate this issue to tech support?";
	pattern "yes" that "Would you like to escalate this issue to tech support?" template "Enter your email address?";
	
	case input goto sentenceState for each #word of sentence;
	
	pattern "*" that "Enter your email address?" template "Invalid email address.";

	state sentenceState {
	    case email that "Enter your email address?" answer sendEmail();
	}
	
	var email {
	    instantiation : #emailaddress;
	}

	function sendEmail() {
	    message = speaker.name + " - " + conversation.input[-1] + " - " + conversation.input[-7];
	    // ** Make sure you change the phone number **
		Email.email("support@foo.com", "Chat escalation follow up requested", message);
		"I have escalated this issue, tech support should be in contact with you shortly.";
	}
}
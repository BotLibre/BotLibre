/**
 * This script lets users escalate their chat session by sending an SMS message to an operator cell phone number.
 * Ensure you reaplce the phone number with your own.
 * This requires your bot to be connected to SMS (from its Admin Console).
 */
state Escalate {
	pattern "escalate" template "Would you like to escalate this issue to a human operator?";
	pattern "yes" that "Would you like to escalate this issue to a human operator?" template "Enter your email address?";
	
	case input goto sentenceState for each #word of sentence;
	
	pattern "*" that "Enter your email address?" template "Invalid email address.";

	state sentenceState {
	    case email that "Enter your email address?" answer sendSMS();
	}
	
	var email {
	    instantiation : #emailaddress;
	}

	function sendSMS() {
	    message = speaker.name + " - " + conversation.input[-1] + " - " + conversation.input[-7];
	    // ** Make sure you change the phone number **
		Twilio.sms("+16131234567", message);
		"I have escalated this issue, an operator should be in contact with you shortly.";
	}
}

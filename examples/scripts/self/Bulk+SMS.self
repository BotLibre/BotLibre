// Bluk SMS
state BulkSMS {
	pattern "SMS" answer start();
	pattern "[exit stop reset]" answer exit();
	pattern "*" topic "SMS" answer askContacts();
	pattern "[yes y ok yep yeah]" topic "confirm" answer sendSMS();
	pattern "*" topic "confirm" answer askContacts();
	case input topic "contacts" goto processNumbers for each #word of sentence;
	
	var url {
		instantiation : #url;
	}
	
	state processNumbers {
	    case url answer importCSV();
	    case "," goto processNumbers;
	    case name goto processName;
	    answer confirmSMS();
	}
	
	state processName {
	    case "," goto processNumber;
	    case lastname goto appendName;
	    answer confirmSMS();
	}
	
	state processNumber {
	    case number goto appendNumber;
	    answer confirmSMS();
	}
	
	state appendName {
	    do {
	        if (fullname == null) {
	            fullname = new Fragment();
	            fullname.word = name;
	        }
	        fullname.word =+ lastname;
	    }
	    return;
	}
	
	state appendNumber {
	    do {
    	    if (fullname != null) {
    	        name = fullname;
    	        fullname = null;
    	    }
    	    contact = new Contact();
    	    contact.name = name;
    	    contact.number = number;
    	    conversation.contact =+ contact;
	    }
	    goto processNumbers;
	}

	function start() {
	    conversation.topic = "SMS";
	    return "Please enter the SMS message you would like to send.";
	}
	
	function askContacts() {
	    conversation.deleteAll(#contact);
	    if (conversation.topic == "SMS") {
	        conversation.message = star;
	    }
	    conversation.topic = "contacts";
	    return "Please upload a CSV (comma sepated values) formatted file, or enter the list of names and phone numbers in the format 'Jon Dow, 6137921991, Jane Smith, 8189991234'.";
	}
	
	function importCSV() {
	    xml = Http.requestXML(url);
	    for (contact in xml.contact) {
	        conversation.contact =+ contact;
	    }
	    confirmSMS();
	}
	
	function confirmSMS() {
	    response = "Confirm numbers by typing 'yes' or type 'no' to try again: ";
	    first = true;
	    count = conversation.size(#contact);
	    response = response + count + " results; ";
	    index = 0;
	    for (contact in conversation.contact) {
	        if (index > 5) {
	            contact = conversation.getLast(#contact);
	            response = response + ", ... , " + contact.name + " : " + contact.number;
	            break;
	        }
	        if (first) {
	            first = false;
	        } else {
	            response = response + ", ";
	        }
	        response = response + contact.name + " : " + contact.number;
	        index++;
	    }
	    conversation.topic = "confirm";
	    return response;
	}
	
	function sendSMS() {
	    for (contact in conversation.contact) {
	        message = "" + contact.name + " - " + conversation.message;
	        debug(message);
	        Twilio.sms(contact.number, message)
	    }
	    conversation.deleteAll(#topic);
	    conversation.deleteAll(#contact);
	    return "SMS messages sent.";
	}
	
	function exit() {
	    conversation.deleteAll(#topic);
	    conversation.deleteAll(#contact);
	    return "Canceling SMS message.";
	}
}

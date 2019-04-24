/**
 * This script lets the bot book appointments or service calls using Google Calendar.
 */
state BookAppointment {
	pattern "calendar" answer calendar();
	pattern "book" answer start();
	pattern "cancel" answer startCancel();
	pattern "[stop exit quit]" answer quit();
	pattern "*" topic "schedule" answer schedule();
	pattern "*" topic "cancel" answer cancel();
	pattern "[yes y yep yeah please ok okay sure]" topic "book" answer book();
	pattern "*" topic "book" answer rebook();
	
	function resetTopic() {
        conversation.topic = null;
	    conversation.start = null;
	    conversation.end = null;
	    conversation.name = null;
	    conversation.email = null;
        conversation.from = null;
        conversation.confirm = null;
	}
	
	function calendar() {
	    resetTopic();
	    return "Welcome to calendar, please select one of the following<br/> <button>Book</button> <button>Cancel</button> <button>Exit</button>";
	}
	
	function start() {
	    resetTopic();
	    conversation.topic = "schedule";
	    return "Please enter your name.";
	}
	
	function quit() {
	    resetTopic();
	    return "Exiting. Please type 'calendar' to restart.";
	}
	
	function rebook() {
	    conversation.topic = "schedule";
	    conversation.start = null;
	    conversation.end = null;
        conversation.confirm = null;
	    return "Please enter the date and time you would like to book an appointment for, for example '2016/10/11', '2016/10/11 1:30 pm'<br/> <button>Earliest</button>";
	}
	
	function schedule() {
        if (conversation.name == null) {
            conversation.name = star;
            return "Please enter your email address."
        }
        if (conversation.email == null) {
            if (star.has(#instantiation, #emailaddress) != true) {
                return "Invalid email address, please enter a valid email address.";
            }
            conversation.email = star;
            return "Please enter the date and time you would like to book an appointment for, for example '2016/10/11', '2016/10/11 1:30 pm'<br/> <button>Earliest</button>"
        }
        if (star == "more") {
            index = 1;
            while (index < 7) {
                from = conversation.start;
                if (from == null) {
                    from = conversation.from;
                }
                if (from == null) {
                    from = Date.timestamp();
                }
                from = Date.set(from, #hour, 0);
                from = Date.set(from, #minute, 0);
                from = Date.set(from, #second, 0);
                from = Date.add(from, #day, index);
                fromDate = Date.date(from);
                hours = [9,10,11,12,13,14,15,16];
                hours2 = [9,10,11,12,13,14,15,16];
                for (hour in hours2.element) {
                    check = Date.set(from, #hour, hour);
                    to = Date.add(check, #minute, 50);
                    bookings = GoogleCalendar.getEvents(check, to);
                    if (bookings.size() != 0) {
                        hours.delete(hour);
                    }
                }
                if (hours.size() != 0) {
                    break;
                }
                index++;
            }
            if (hours.size() == 0) {
                return "There are no spots available in the next 7 days, please enter a date to check.";
            }
        	conversation.start = from;
            buttons = "";
            for (hour in hours.element) {
                buttons = buttons + " <button>" + hour + "</button>";
            }
            return "There are spots available on " + fromDate + ". Please choose from the following available times, for example enter 13 for 1pm<br/>" + buttons + " <button>More</button>";
        } else if (conversation.start != null) {
            spot = star.toNumber();
            if (spot == null) {
                return "Invalid number, please enter the hour, for example 9";
            }
            from = Date.set(conversation.start, #hour, spot);
        } else if (star == "earliest") {
            index = 0;
            while (index < 7) {
                from = Date.timestamp();
                from = Date.set(from, #hour, 0);
                from = Date.set(from, #minute, 0);
                from = Date.set(from, #second, 0);
                from = Date.add(from, #day, index);
                fromDate = Date.date();
                fromDate = Date.add(fromDate, #day, index);
                hours = [9,10,11,12,13,14,15,16];
                hours2 = [9,10,11,12,13,14,15,16];
                for (hour in hours2.element) {
                    check = Date.set(from, #hour, hour);
                    to = Date.add(check, #minute, 50);
                    bookings = GoogleCalendar.getEvents(check, to);
                    if (bookings.size() != 0) {
                        hours.delete(hour);
                    }
                }
                if (hours.size() != 0) {
                    break;
                }
                index++;
            }
            if (hours.size() == 0) {
                return "There are no spots available in the next 7 days, please enter a date to check.";
            }
        	conversation.start = from;
            buttons = "";
            for (hour in hours.element) {
                buttons = buttons + " <button>" + hour + "</button>";
            }
            return "There are spots available on " + fromDate + ". Please choose from the following available times, for example enter 13 for 1pm<br/>" + buttons + " <button>More</button>";
        } else if (conversation.confirm == null) {
            from = Date.any(star);
            if (from == null) {
                return "Invalid date format, please enter a date in the format yyyy/mm/dd hh:mm am/pm, for example '2016/10/11', '2016/10/11 1:30 pm'<br/> <button>Earliest</button>";
            }
            conversation.from = from;
            conversation.confirm = true;
            hour = Date.get(from, #hour);
            if (hour == 0) {
                from = Date.date(from);
            }
            return Template("Please confirm the date {from}<br/> <button>Yes</button> <button>No</button>");
        } else {
            if (star == "no") {
                conversation.from = null;
                conversation.confirm = null;
                return "Please enter a date in the format yyyy/mm/dd hh:mm am/pm, for example '2016/10/11', '2016/10/11 1:30 pm'<br/> <button>Earliest</button>";
            }
            from = conversation.from;
            if (from == null) {
                conversation.confirm = null;
                return "Invalid date format, please enter a date in the format yyyy/mm/dd hh:mm am/pm, for example '2016/10/11', '2016/10/11 1:30 pm'<br/> <button>Earliest</button>";
            }
            
            hour = Date.get(from, #hour);
            if (hour == 0) {
        	    conversation.start = from;
                hours = [9,10,11,12,13,14,15,16];
                hours2 = [9,10,11,12,13,14,15,16];
                for (hour in hours2.element) {
                    check = Date.set(from, #hour, hour);
                    to = Date.add(check, #minute, 50);
                    bookings = GoogleCalendar.getEvents(check, to);
                    if (bookings.size() != 0) {
                        hours.delete(hour);
                    }
                }
                if (hours.size() == 0) {
                    conversation.start = null;
                    conversation.confirm = null;
                    return "That date is fully booked, please enter another date."
                }
                buttons = "";
                for (hour in hours.element) {
                    buttons = buttons + " <button>" + hour + "</button>";
                }
                return "Please choose from the following available times, for example enter 13 for 1pm<br/>" + buttons + " <button>More</button>";
            }
        }
        to = Date.add(from, #minute, 50);
        bookings = GoogleCalendar.getEvents(from, to);
        if (bookings == null) {
	        resetTopic();
            return "Sorry, their was an error accessing the calendar, please try again.";
        }
        if (bookings.size() == 0) {
	        conversation.start = from;
	        conversation.end = to;
            conversation.topic = "book";
            return Template("This time {from} to {to} is available, would you like to book it?<br/><button>Yes</button> <button>No</button>")
        } else {
            conversation.start = null;
            conversation.confirm = null;
            return "That date/time is not available, please choose another date/time.";
        }
	}
	
	function book() {
	    start = conversation.start;
	    end = conversation.end;
        event = new Object();
        event.start = { dateTime : start};
        event.end = { dateTime : end };
        event.summary = "Appointment with " + conversation.name;
        event.description = "appointment with " + conversation.name;
        event.attendees = [ { email : conversation.email } ];
        result = GoogleCalendar.insertEvent(event);
        if (result == null) {
	        resetTopic();
            return "Sorry, their was an error accessing the calendar, please try again.";
        }
		Email.email(conversation.email, "Appointment booked", "Your appointment for " + start + " has been booked.");
	    resetTopic();
	    return Template("Your appointment has been scheduled for: {start}.");
	}
	
	function startCancel() {
	    conversation.topic = "cancel";
        return "Please enter your email address.";
	}
	
	function cancel() {
        if (conversation.email == null) {
            if (star.has(#instantiation, #emailaddress) != true) {
                return "Invalid email address, please enter a valid email address.";
            }
            conversation.email = star;
	        return "Please enter the date and time you would like to cancel an appointment for, for example 2016/10/11 1:30pm";
        }
        if (conversation.confirm == null) {
            from = Date.any(star);
            if (from == null) {
                return "Invalid date format, please enter a date in the format yyyy/mm/dd hh:mm am/pm, for example 2016/10/11 1:30pm";
            }
            conversation.from = from;
            conversation.confirm = true;
            return Template("Please confirm the date {from}<br/> <button>Yes</button> <button>No</button>");
        }
        if (star == "no") {
            conversation.from = null;
            conversation.confirm = null;
            return "Please enter a date in the format yyyy/mm/dd hh:mm am/pm, for example 2016/10/11 1:30pm";
        }
        from = conversation.from;
        to = Date.add(from, #minute, 50);
        bookings = GoogleCalendar.getEvents(from, to);
        if (bookings == null) {
	        conversation.topic = null;
            return "Sorry, their was an error accessing the calendar, please try again.";
        }
        if (bookings.size() == 0) {
	        resetTopic();
            return Template("There are no scheduled appointments on {from}.");
        } else {
            booking = bookings[0];
            attendees = booking.attendees;
            confirm = false;
            for (attendee in attendees.element) {
                if (attendee.email == conversation.email) {
                    confirm = true;
                }
            }
            if (!confirm) {
                return Template("There are no scheduled appointments for your email address on {from}.");
            }
            result = GoogleCalendar.deleteEvent(booking.id);
            if (result == null) {
	            resetTopic();
                return "Sorry, their was an error accessing the calendar, please try again.";
            }
		    Email.email(conversation.email, "Appointment Canceled", "Your appointment for " + from + " has been canceled.");
	        resetTopic();
            return Template("Your appointment for {from} has been canceled.");
        }
	}
}

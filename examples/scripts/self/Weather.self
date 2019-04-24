// This script listens to questions about the weather, and redirects them to the Pannous web service.
state Weather {
	pattern "^ weather ^" template (request (sentence, {service : #pannous}));
}
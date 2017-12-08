// Scripts can be used to give programmatic responses to patterns, or process state machines.
state MyScript {
	pattern "hello" template "Hello there.";
	pattern "start" template start();
	pattern "*" topic "mytopic" template myTopic();

	function start() {
		conversation.topic = "mytopic";
		return Template("Welcome {speaker}.");
	}
	
	function myTopic() {
		return "We are talking about mytopic.";
	}
}

// This script lets you play I Spy with your bot, and it will learn colors from playing.
state ISpy {
	pattern "init i spy" answer init();
	pattern "^ I Spy" answer askPlay();
	pattern "[exit stop]" topic "I Spy" answer quit();
	pattern "Would you like to play I Spy with me?" answer "Yes.";
	pattern "Would you like to play again?" answer "Yes.";
	pattern "Okay, start by saying 'I spy something that is...'" answer start();
	pattern "Would you like to spy or guess?" answer Template("{random ("spy", "guess")}");
	pattern "[yes ok okay y yep please sure]" that "Would you like to play I Spy with me?" answer "Would you like to spy or guess?";
	pattern "[yes ok okay y yep please sure]" that "Would you like to play again?" answer "Would you like to spy or guess?";
	pattern "[no n nope]" that "Would you like to play again?" answer "Okay, thank you for playing with me.";
	pattern "^ spy" that "Would you like to spy or guess?" answer "Okay, start by saying 'I spy something that is...'";
	pattern "^ guess" that "Would you like to spy or guess?" answer start();
	pattern "I spy ^ something that is *" answer guess();
	pattern "I spy ^ something *" answer guess();
	pattern "I spy *" answer guess();
	pattern "^ [yes correct yep] ^" topic "I Spy" answer done();
	pattern "^ quit ^" topic "I Spy" answer giveUp();
	pattern "^ give up ^" topic "I Spy" answer giveUp();
	pattern "I was thinking of *" topic "I Spy" answer play();
	pattern "(is it) (it is) (a an the) *" topic "I Spy" answer play();
	pattern "*" topic "I Spy" answer play();
	
	function init() {
	    // This loads some colors and objects using the JSON (SSON) format.
	    { #data : #ispy, learn : true, safe: true }
	    
	    { #data : #color, instantiation : #thing, word : "color" }
	    { #data : "color", meaning : #color }
	    
	    { #data : #blue, instantiation : #color, instantiation : #description, word : "blue", thing : #sky }
	    { #data : "blue", instantiation : #adjective, meaning : #blue }
	    
	    { #data : #green, instantiation : #color, instantiation : #description, word : "green", thing : #tree }
	    { #data : "green", instantiation : #adjective, meaning : #green }
	    
	    { #data : #red, instantiation : #color, instantiation : #description, word : "red", thing : #apple }
	    { #data : "red", instantiation : #adjective, meaning : #red }
	    
	    { #data : #white, instantiation : #color, instantiation : #description, word : "white", thing : #snow }
	    { #data : "white", instantiation : #adjective, meaning : #white }
	    
	    { #data : #black, instantiation : #color, instantiation : #description, word : "black" }
	    { #data : "black", instantiation : #adjective, meaning : #black }
	    
	    { #data : #yellow, instantiation : #color, instantiation : #description, word : "yellow", thing : #sun }
	    { #data : "yellow", instantiation : #adjective, meaning : #yellow }
	    
	    { #data : #orange, instantiation : #color, instantiation : #description, word : "orange", thing : #pumpkin }
	    { #data : "orange", instantiation : #adjective, meaning : #orange }
	    
	    { #data : #sky, instantiation : #thing, word : "sky", color : #blue }
	    { #data : "sky", instantiation : #noun, meaning : #sky }
	    
	    { #data : #tree, instantiation : #thing, word : "tree", color : #green }
	    { #data : "tree", instantiation : #noun, meaning : #tree }
	    
	    { #data : #snow, instantiation : #thing, word : "snow", color : #white }
	    { #data : "snow", instantiation : #noun, meaning : #snow }
	    
	    { #data : #apple, instantiation : #thing, word : "apple", color : #red }
	    { #data : "apple", instantiation : #noun, meaning : #apple }
	    
	    { #data : #sun, instantiation : #thing, word : "sun", color : #yellow }
	    { #data : "sun", instantiation : #noun, meaning : #sun }
	    
	    { #data : #pumpkin, instantiation : #thing, word : "pumpkin", color : #orange }
	    { #data : "pumpkin", instantiation : #noun, meaning : #pumpkin }
	    
	    { #data : #thing, instance : #sky, instance : #snow, instance : #apple, instance : #sun, instance : #pumpkin }
	    
	    return "init complete";
	}
	
	function play() {
	    if (conversation.player == #self) {
	        var color = conversation.color;
	        if (conversation.giveUp == true) {
	            // Add the object and associate the color so the bot remembers/learns it.
	            var word = Language.sentence(Utils.person(star));
	            // Only learn approved nouns in safe mode.
	            if (#ispy.learn && ((!#ispy.safe) || (word.has(#instantiation, #noun) && color.word.has(#instantiation, #adjective)))) {
    	            if (word.meaning == null) {
    	                word.meaning = new Thing();
    	                word.meaning.word = word;
    	            }
    	            var thing = word.meaning;
    	            thing.color = color;
    	            color.thing =+ thing;
    	            #thing.instance =+ thing;
	            }
	            conversation.deleteAll(#topic);
	            return "Would you like to play again?";
	        }
	        var guess = color.random(#thing);
	        if (conversation.has(#guess, guess)) {
	            guess = color.random(#thing);
	            if (conversation.has(#guess, guess)) {
	                guess = null;
	                for (thing in color.thing) {
	                    if (conversation.has(#guess, thing) != true) {
	                        guess = thing;
	                        break;
	                    }
	                }
	            }
	        }
	        if (guess == null) {
	            conversation.giveUp = true;
	            return "I give up, what is it?";
	        }
	        conversation.guess =+ guess;
	        return Template("Is it {guess}?");
	    }
	    var word = Language.sentence(Utils.person(star));
	    if (conversation.spy.has(#word, word)) {
	        conversation.deleteAll(#topic);
	        return "Correct! You win. Would you like to play again?";
	    }
	    return "No, guess again or type 'quit' to stop.";
	}
	
	function start() {
	    var spy = #thing.random(#instance);
	    var count = 0;
	    while (spy != null && spy.color == null && count < 10) {
	        // Clean up garbage.
	        #thing.remove(#instance, spy);
	        spy = #thing.random(#instance);
	        count++;
	    }
	    if (spy == null) {
	        return "Missing data, please type 'init i spy'";
	    }
	    conversation.spy = spy;
	    conversation.topic = "I Spy";
	    conversation.player = speaker;
	    conversation.giveUp = false;
	    conversation.guess = null;
	    var color = spy.color;
	    return Template("I spy with my little eye something that is {color}.");
	}
	
	function guess() {
	    conversation.topic = "I Spy";
	    conversation.player = #self;
	    conversation.color = null;
	    conversation.giveUp = false;
	    conversation.guess = null;
	    var color = star[1];
	    if (color.meaning == null) {
	        color.meaning = new Color();
	        color.meaning.word = color;
	        conversation.color = color.meaning;
	    } else {
	        // Handle multiple meanings
	        for (meaning in color.meaning) {
	            if (meaning.has(#instantiation, #color)) {
	                conversation.color = meaning;
	                break;
	            }
	        }
	        if (conversation.color == null) {
	            meaning = color.meaning;
	            conversation.color = meaning;
	            meaning.instantiation =+ #color;
	        }
	    }
	    return play();
	}
	
	function askPlay() {
	    if (conversation.topic == "I Spy") {
	        return null;
	    }
	    return "Would you like to play I Spy with me?";
	}
	
	function quit() {
	    conversation.deleteAll(#topic);
	    return "Okay, I was getting bored with that anyway.";
	}
	
	function giveUp() {
	    if (conversation.player != speaker) {
	        return null;
	    }
	    conversation.deleteAll(#topic);
	    return Template("I was thinking of {conversation.spy}. Would you like to play again?");
	}
	
	function done() {
	    if (conversation.player != #self) {
	        return null;
	    }
	    conversation.deleteAll(#topic);
	    return "Yah! I win. Would you like to play again?";
	}
}

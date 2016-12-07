// This script lets you play I Spy with your bot, and it will learn colors from playing.
state ISpy {
	pattern "init i spy" answer init();
	pattern "^ I Spy" answer askPlay();
	pattern "[quit exit stop]" topic "I Spy" answer quit();
	pattern "Would you like to play I Spy with me?" answer "Yes.";
	pattern "Would you like to play again?" answer "Yes.";
	pattern "Okay, start by saying 'I spy something that is...'" answer start();
	pattern "Would you like to spy or guess?" answer Template("{random ("spy", "guess")}");
	pattern "[yes ok okay y yep please]" that "Would you like to play I Spy with me?" answer "Would you like to spy or guess?";
	pattern "[yes ok okay y yep please]" that "Would you like to play again?" answer "Would you like to spy or guess?";
	pattern "no" that "Would you like to play again?" answer "Okay, thank you for playing with me.";
	pattern "^ spy" that "Would you like to spy or guess?" answer "Okay, start by saying 'I spy something that is...'";
	pattern "^ guess" that "Would you like to spy or guess?" answer start();
	pattern "I spy ^ something that is *" answer guess();
	pattern "^ [yes correct yep] ^" topic "I Spy" answer done();
	pattern "^ give up ^" topic "I Spy" answer giveUp();
	pattern "I was thinking of *" topic "I Spy" answer play();
	pattern "(is it) (it is) (a an the) *" topic "I Spy" answer play();
	pattern "*" topic "I Spy" answer play();
	
	function init() {
	    // This loads some colors and objects using the JSON (SSON) format.
	    { #data : #color, instantiation : #thing, word : "color" }
	    { #data : "color", meaning : #color }
	    
	    { #data : #blue, instantiation : #color, instantiation : #adjective, word : "blue", thing : #sky }
	    { #data : "blue", meaning : #blue }
	    
	    { #data : #green, instantiation : #color, instantiation : #adjective, word : "green", thing : #tree }
	    { #data : "green", meaning : #green }
	    
	    { #data : #red, instantiation : #color, instantiation : #adjective, word : "red", thing : #apple }
	    { #data : "red", meaning : #red }
	    
	    { #data : #white, instantiation : #color, instantiation : #adjective, word : "white", thing : #snow }
	    { #data : "white", meaning : #white }
	    
	    { #data : #black, instantiation : #color, instantiation : #adjective, word : "black" }
	    { #data : "black", meaning : #black }
	    
	    { #data : #yellow, instantiation : #color, instantiation : #adjective, word : "yellow", thing : #sun }
	    { #data : "yellow", meaning : #yellow }
	    
	    { #data : #orange, instantiation : #color, instantiation : #adjective, word : "orange", thing : #pumpkin }
	    { #data : "orange", meaning : #orange }
	    
	    { #data : #sky, instantiation : #thing, word : "sky", color : #blue }
	    { #data : "sky", meaning : #sky }
	    
	    { #data : #tree, instantiation : #thing, word : "tree", color : #green }
	    { #data : "tree", meaning : #tree }
	    
	    { #data : #snow, instantiation : #thing, word : "snow", color : #white }
	    { #data : "snow", meaning : #snow }
	    
	    { #data : #apple, instantiation : #thing, word : "apple", color : #red }
	    { #data : "apple", meaning : #apple }
	    
	    { #data : #sun, instantiation : #thing, word : "sun", color : #yellow }
	    { #data : "sun", meaning : #sun }
	    
	    { #data : #pumpkin, instantiation : #thing, word : "pumpkin", color : #orange }
	    { #data : "pumpkin", meaning : #pumpkin }
	    
	    { #data : #thing, instance : #sky, instance : #snow, instance : #apple, instance : #sun, instance : #pumpkin }
	    
	    return "init complete";
	}
	
	function play() {
	    if (conversation.player == #self) {
	        var color = conversation.color;
	        if (conversation.giveUp == true) {
	            // Add the object and associate the color so the bot remembers/learns it.
	            var word = Language.word(star);
	            if (word.meaning == null) {
	                word.meaning = new Thing();
	                word.meaning.word = word;
	            }
	            var thing = word.meaning;
	            thing.color = color;
	            color.thing =+ thing;
	            #thing.instance =+ thing;
	            conversation.topic = null;
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
	    var word = Language.word(star);
	    if (conversation.spy.has(#word, word)) {
	        conversation.topic = null;
	        return "Correct! You win. Would you like to play again?";
	    }
	    return "No, guess again or type 'give up' to stop.";
	}
	
	function start() {
	    conversation.spy = #thing.random(#instance);
	    if (conversation.spy == null) {
	        return "Missing data, please type 'init i spy'";
	    }
	    conversation.topic = "I Spy";
	    conversation.player = speaker;
	    conversation.giveUp = false;
	    conversation.guess = null;
	    var color = conversation.spy.color;
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
	    conversation.topic = null;
	    return "Okay, I was getting bored with that anyway.";
	}
	
	function giveUp() {
	    if (conversation.player != speaker) {
	        return null;
	    }
	    conversation.topic = null;
	    return Template("I was thinking of {conversation.spy}. Would you like to play again?");
	}
	
	function done() {
	    if (conversation.player != #self) {
	        return null;
	    }
	    conversation.topic = null;
	    return "Yah! I win. Would you like to play again?";
	}
}

greeting: the topic is sports
topic: sports
actions: smile
poses: waving
emotions: happy
command: { type: "intent", value: "buzz" }

greeting: Template("{srai "greeting srai"}")
condition: #self.test == true

greeting srai
Template("the topic is fun {think{conversation.topic = "fun";}}")

greeting srai test
Template("setup done {think{#self.test = true}}")

default: I love hockey!
topic: hockey
actions: yell
poses: cheer
emotions: happy
command: { type: "intent", value: "beep" }

default: the topic is ai
require topic: ai
no repeat:

default: ask question
require topic: ai

default: ask another question
previous: ask question
require topic: ai

default: I said I love hockey
condition: conversation.input[-2].input == "I love hockey!"

default: I said I really love hockey
condition: conversation.input[-2].input == "topic cleared"

talk about ai
ok
topic: ai

clear topic
Template("topic cleared{think{conversation.topic = null}}")

topic
Template("{if (conversation.topic==null) {""} else {conversation.topic}}")

this is a very complicated sentence
this is a good reply to that

the dog barks all night
let him in then

this is a very very long sentence that is very long, yes, very long, it has one two three four five size seven eight nine ten or more words
how long?
keywords: sentence

is the sky blue
Yes, the sky is blue.
keywords: sky
required: sky blue

is the sky ocean blue
No, the ocean is ocean blue, the sky is sky blue.
keywords: sky
required: sky "ocean blue"

is the grass green
Yes, the grass is green.
keywords: green
required: [grass grazz] green

is the sun yellow
Yes, the sun is yellow.
keywords: sun yellow
required: Pattern("is the *") sun [yellow yllow]

are you a chat bot
Yes, I am a bot.
keywords: "chat bot" chatbot "chatter bot" "chat robot" chatterbot 

pattern: redirect *
template: {redirect(star)}

hi
do you like me?
label: #likeme
on repeat: whats up?

hi there
#likeme
on repeat: whats up?

yes
what do you like about me?
require previous: do you like me?

no
why not?
require previous: #likeme
require previous: whats up?

hey
are you ok?

yes
are you sure?
require previous: are you ok?

yes
no

no
yes

yes
no

Pattern("do you like {^color}")
Yes, it is a nice color.

What are you?
Template("I am a {#self.instantiation}.")
required: you

empty
Template("")
keywords: na

test self
template: {1 + 1} {1*1} {1-1} {1 / 2} {var count = 0; count++; count++; count --; count;}

test command
template: {var detached = { name : "dont garbage collect me" }; detached.name }
command: { type: "intent", value: "open angry birds" }

test command2
template: {var detached = { nested: {name : "dont garbage collect me"} }; detached.nested.name }
command: { quick_replies: [{ nested: {value: "open angry birds"} }] }

test command3
datatypes
command: { reply_markup: [ {boolean : true}, {number : 3.14}, {null : null}, [false, -123, "zxv", null] ] }

script: #bot.word = "bot"; "bot".meaning = #bot;
script: #self.instantiation =+ #bot

script: "black".meaning = #black
script: #black.instantiation =+ #color
script: "red".meaning = #red
script: #red.instantiation =+ #color

email mom
sending email
command: {type : "email", recipient : "mom@family.org", subject : "hello", message : "hi mom"}

you are my friend
ok, were friends
think: #self.friend =+ speaker

am I your friend?
yes
condition: #self.has(#friend, speaker)

am I your friend?
no
condition: #self.has(#friend, speaker) != true

sports
lets talk about sports
exclusive topic: sports

hockey
Who is your favorite team?
require topic: sports

test
hello world

repeat
listen the first time
no repeat:

repeat repeat
okay, fine

repeat 2
on repeat 1
on repeat: on repeat 2
on repeat: on repeat 3

test 2
success

test 2
sports
require topic: sports

test 3
template: hello {"world"}
no repeat:

test 3
goodbye world

bad template
template: {#self.invalid}

bad template 2
okay

exit
template: Exiting{think{conversation.topic=null}}
require topic: sports

default: Are we still talking about sports?
require topic: sports

suck
I'm doing my best

word: suck
keyword: true

phrase: you suck
sentiment: bad
emotions: anger

you're evil
No, I'm nice.
keywords: evil
confidence: 100

word: are
synonyms: re am is

word: evil
sentiment: terrible
synonyms: terrible horrible "very mean" "not very nice"

word: horrible
sentiment: terrible
synonyms: terrible evil

word: terrible
sentiment: terrible
synonyms: horrible evil

word: very mean
sentiment: terrible
synonyms: horrible evil

word: not very nice
sentiment: terrible
synonyms: horrible evil

pattern: * evil *
I'm not evil!

question: test 4
response: s1
response: s2

help
Do you want help with bots or chat?

	bots
	What kind of bot?
		chatbot
		Trying chatting more
		topic: chatbots
		
		twitterbot
		Try tweeting more
		topic: twitter
		keywords: twitter twitterbot tweet
		
	chat
	Live chat or a chat room?
	intent: live
	
        live
        Try speaking to a human
        emotions: anger
        actions: wave
        command: { "type" : "wave" }
        
		chat room
		What is the room's name?
        
		live chat room
		What is the operator's name?
		required: live chat
		
		pop
		Lets try that again
		think: conversation.next = conversation.previous
		
		bounce
		Lets try that again
		think: conversation.next = conversation.current
	pattern: [neither no nope]
	What is the issue with?
	
	exit
	Goodbye
	
	default: I can only help with bots and chat

default: For creating a Twitterbot see, https://...
require topic: twitter

exit
Please don't go

paragraph
"Lorem ipsum dolor sit amet,
consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum"
keywords: para paragraph

Fine, but  tell  me  your  name...
Formula:"my name is {:target}"
keywords: name 

要
问题：要

word: 好
synonyms: 要

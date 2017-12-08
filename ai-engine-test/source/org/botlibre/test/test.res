this is a very complicated sentence
this is a good reply to that

the dog barks all night
let him in then

this is a very very long sentence that is very long, yes, very long, it has one two three four five size seven eight nine ten or more words
how long?
keywords: sentence

hi
do you like me?
label: #likeme

yes
what do you like about me?
require previous: do you like me?

no
why not?
require previous: #likeme

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

empty
Template("")
keywords: na

test self
Template("{1 + 1} {1*1} {1-1} {1 / 2} {var count = 0; count++; count++; count --; count;}")

test command
Template("{var detached = { name : "dont garbage collect me" }; detached.name }")
command: { type: "intent", value: "open angry birds" }

test command2
Template("{var detached = { nested: {name : "dont garbage collect me"} }; detached.nested.name }")
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

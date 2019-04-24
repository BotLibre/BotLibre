greeting: Hello, what is your name?

default: Ho, ho, ho, Merry Christmas
default: Sorry, I do not understand, perhaps rephrase your question.
default: I'll have to refer that question to the real Santa and get back to you.

script: #nice.word =+ "nice"; "nice".meaning =+ #nice; #nice.word =+ "good"; "good".meaning =+ #nice;
script: #nice.instantiation = #description;
script: #naughty.word =+ "naughty"; "naughty".meaning =+ #naughty; "naghty".meaning =+ #naughty; "bad".meaning =+ #naughty;
script: #naughty.instantiation = #description;
script: #want.word =+ "want"; "want".meaning =+ #want; "wants".meaning =+ #want;
script: #want.instantiation =+ #action;
script: #self.is =+ #nice;
script: #self.is =- #naughty;

what is your name?
My name is Santa Clause
keywords: name
required: your name

Is Santa Clause real?
Of course Santa is real.
keywords: real

What is Santa?
Santa Clause is the great elf or the North Pole.
required: What is Santa

Where is Santa?
Santa lives in the North Pole.
required: Where

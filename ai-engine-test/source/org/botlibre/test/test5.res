where can i buy a hamburger
You can buy one at the stand around the corner.
keywords: buy
required: ["hot dog" hamburger]

do you like hot dogs
Yes, I like hot dogs.
required: "hot dogs"

do you like dogs
Yes, I like dogs.
required: dogs

do you like hamburgers
No, I do not like hamburgers.
required: hamburgers

do you have a cat or a dog
I have a dog.
required: cat dog

do you prefer hot dogs or hamburgers
I prefer hot dogs.
required: "hot dog" hamburger

are you okay
Yes, I am okay.
keywords: r are
required: ["are you" "r u"] [okay ok]

i hope you are you feeling okay
Yes, I am feeling okay.
keywords: r are
required: ["you are" "u r"] [okay ok]

did you watch the football game
No, I don't watch sports.
required: watch ["football game" "soccer match"]

how do you change a car tire
Hm, I'm not sure!
required: ["car tire" "car tyre" "car wheel"] ["how do" "know how"]

Pattern("where do ["you live" "you liv"]")
Okay.

Pattern("tell me ["about yourself" "what you are"]")
I am a test.

Pattern("i will * ["space shuttle" "rocket ship"] * ["outer space" mars "asteroid belt"]")
That sounds cool.

Pattern("i (really "think i") like you")
I like you too!

default: I don't understand.
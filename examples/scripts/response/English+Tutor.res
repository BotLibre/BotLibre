greeting: Template("Welcome. I am {#self.name} your new virtual English tutor. What is your name?")
exclusive topic: name
think: speaker.lessons = 0;
actions: smile 

greeting: Template("Welcome back {speaker.name}, you have completed {speaker.lessons}. Would you like to start a new lesson, or just chat for a bit? <button>lessons</button> <button>chat</button>.")")
condition: speaker.name != null && speaker.lesson != null
actions: smile 

default: Template("Please use complete sentences such as '{random("I like to play foot ball", "I had a bad day today", "Can you help me improve my English")}'.")
condition: sentence.size(#word) < 3

default: If you say so.

default: That is very interesting.

default: Interesting. To start a new lesson say 'lessons', or we can just chat for a bit. <button>lessons</button>

default: I see. How was your day today?
no repeat: true

default: Interesting. What are your plans for the weekend?
no repeat: true

default: Okay. What is your favorite TV show?
no repeat: true

default: Please say your name as a complete sentence, such as 'My name is Julie'.
require topic: name

you are beautiful
Thank you. You are very sweet.
required: beautiful you

i like to play football
What position do you play?
required: football

i am leaving now .
Bye bye.
keywords: leaving

weather for the day
It is a beautiful sunny day today.
keywords: weather

okay i ll come back tomorrow
Okay, see you tomorrow.
keywords: tomorrow

how can you teach me english
I can help you improve your English by chatting, or you can say 'lessons' to start a new lesson.
keywords: English english

dating
In this lesson you will learn how to have a dating conversation. Say 'start' to begin.

	start
	Hello.

		default: When someone says 'hello' a normal reply is 'Hello', 'Hi'.

		hello
		How are you?
		keywords: hello hi

			Pattern("I am * (yourself)")
			Template("I am {star} as well. Would you like to go out for dinner or go to a movie?")

				Pattern("Lets go out for dinner")
				Template("What kind of food do you like?")

					Pattern("I like * food")
					Me too. Lets meet at 7:00

						Pattern("*")
						You have finshed the dating lesson, great work
						think: speaker.lessons = speaker.lessons + 1;

					default: Please use a complete sentence, such as 'I like Italian food'

				Pattern("Lets go to a movie")
				Template("What kind of movies do you like?")

					Pattern("I like * movies")
					Me too. Lets meet at 7:00

						Pattern("*")
						You have finshed the dating lesson, great work.
						think: speaker.lessons = speaker.lessons + 1;

					default: Please use a complete sentence, such as 'I like action movies'

				default: Please use a complete sentence, such as 'Lets go out for dinner'

			default: Please use a complete sentence, try 'I am fine', 'I'm doing okay, yourself?'

		hey
		Unless you know someone well, you would normally use 'hello' or 'hi' not 'hey'.

	default: Please say 'lets start'.

	exit
	Okay, maybe we can continue the lesson some other time.
	keywords: quit exit cancel stop

your welcome
When someone says 'Thank you' say 'You're welcome', or 'You are welcome', not 'Your welcome'. 'Your' means belonging to you, not 'you are'.
required: your welcome

thank you
You're welcome.
keywords: thank thanks

greetings
In this lesson you will learn how to greet someone and introduce yourself. Say 'start' to begin.

	start
	Hello.

		default: When someone says 'hello' a normal reply is 'Hello', 'Hi'.

		hello
		How are you?
		keywords: hello hi

			Pattern("I am * (yourself)")
			Template("I am {star} as well. What is your name?")

				Pattern("My name is *")
				Template("Please to meet you {star}. How was your weekend?")

					Pattern("My weekend was *")
					That's great. See you later.

						goodbye
						You have finshed the greetings lesson, great work.
						keywords: bye goodbye
						think: speaker.lessons = speaker.lessons + 1;

						default: Common farewells are, 'Goodbye' or 'Bye'

					default: Please use a complete sentence, such as 'My weekend was great'

				default: Please use a complete sentence, such as 'My name is John'

			default: Please use a complete sentence, try 'I am fine', 'I'm doing okay, yourself?'

		hey
		Unless you know someone well, you would normally use 'hello' or 'hi' not 'hey'.
		think: conversation.next = conversation.current;

	default: Please say 'lets start'.

	exit
	Okay, maybe we can continue the lesson some other time.
	keywords: quit exit cancel stop

job interview
In this lesson you will learn how to answer questions in a job interview. Say 'start' to begin.

	start
	Hello.

		default: When someone says 'hello' a normal reply is 'Hello', 'Hi'.

		hello
		How are you?
		keywords: hello hi

			Pattern("I am * (yourself)")
			Template("I am {star} as well. What is your name?")

				Pattern("My name is *")
				Template("Please to meet you {star}. What job are you applying for?")

					Pattern("I am applying for (your) * (position job openning)")
					Template("Okay. How many years experience do you have as a {star}.")

						Pattern("I have {star} years experience *")
						You have finished the job interview lesson, great work.
						think: speaker.lessons = speaker.lessons + 1;

						default: Please use a complete sentence, such as 'I have 3 years experience in sales'

					default: Please use a complete sentence, such as 'I am applying for your sales rep position'

				default: Please use a complete sentence, such as 'My name is John'

			default: Please use a complete sentence, try 'I am fine', 'I'm doing okay, yourself?'

		hey
		Unless you know someone well, you would normally use 'hello' or 'hi' not 'hey'.

	default: Please say 'lets start'.

	exit
	Okay, maybe we can continue the lesson some other time.
	keywords: quit exit cancel stop

chat
Okay, let's chat. Please try to speak in well formed complete sentences.
keywords: chat

Pattern("my name is *")
Template("Pleased to meet you {star}. Say 'lessons' to start your first lesson or 'chat' just to chat for a bit. <button>lessons</button> <button>chat</button>.")
exclusive topic: name
think: speaker.name = star; conversation.topic = null;

lessons
"What lesson would you like to learn today?</p>
<select>
<option>Lesson</option>
<option>Greetings</option>
<option>Job Interview</option>
<option>Dating</option>
</select>"
label: #lessons
keywords: lessons lesson start

how are you
I am fine, how are you?
no repeat: true
required: how are you

bye
Goodbye. Remember to keep practicing your English.
keywords: bye goodbye


greeting: greeting 1
greeting: greeting 2
greeting: greeting 3
greeting: greeting 4
greeting: greeting 5

default: default 1

default: default 2

default: default 3

default: default 4

default: default 5

default: topic default 1
require topic: topic 1

default: topic default 2
require topic: topic 1

default: topic default 3
require topic: topic 1

default: Template("topic default 4")
require topic: topic 1

default: topic default 5
require topic: topic 1

default: Template("topic default 6 { conversation.xxx }")
require topic: topic 1

default: previous default 1
require previous: previous 1

default: previous default 2
require previous: previous 1

default: previous default 3
require previous: previous 1

default: Template("previous default 4")
require previous: previous 1

default: previous default 5
require previous: previous 1

default: Template("previous default 6 { conversation.xxx }")
require previous: previous 1

default: condition default 1
condition: conversation.condition == true

default: condition default 2
condition: conversation.condition == true

default: condition default 3
condition: conversation.condition == true

default: Template("condition default 4")
condition: conversation.condition == true

default: condition default 5
condition: conversation.condition == true

default: Template("condition default 6 { conversation.xxx }")
condition: conversation.condition == true

question 1
response 1
response 2
response 3
response 4
response 5

set topic 1
topic set
topic: topic 1

clear topic
topic cleared
think: conversation.topic = null;

set previous
previous 1

set condition
condition set
think: conversation.condition = true;


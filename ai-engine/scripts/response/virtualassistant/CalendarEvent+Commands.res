//Patterns for creating events and opening the calendar

Create event
Creating event
command: { type: "calendar", action: "insert" }
required: create event
keywords: create event

//Create event with name: "Create event Mom's Birthday Party"

Pattern("Create event *")
Creating event
command: { type: "calendar", action: "insert", name:""+star }
required: create event
keywords: create event

//Create event with name and location: "Create event My Birthday at the pub"

Pattern("Create event * at *")
Creating event
command: { type: "calendar", action: "insert", name:""+star[0], location:""+star[1] }
required: create event at
keywords: create event at

//Create event with begin time: "Create event on August 4th 12:30 pm"

Pattern("Create event on *")
Creating event
command: { type: "calendar", action: "insert", begin:""+star }
required: create event on
keywords: create event on

//Create event with begin and end time: "Create event from August 20th to September 1st"

Pattern("Create event from * to *")
Creating event
command: { type: "calendar", action: "insert", begin:""+star[0], end:""+star[1] }
required: create event from to
keywords: create event from to

//Create event with name, location, begin and end time: "Create event My Birthday at My Fave Restaurant from August 19th 7pm to August 19th 8pm"

Pattern("Create event * at * from * to *")
Creating event
command: { type: "calendar", action: "insert", name: ""+star[0], location: ""+star[1], begin: ""+star[2], end: ""+star[3] }
required: create event at from to
keywords: create event at from to

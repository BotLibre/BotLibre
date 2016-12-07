//Patterns for setting alarms

Open alarms
Opening existing alarms
command: { type: "alarm", action: "show" }
required: alarms
keywords: open alarms

Show timers
Opening existing timers
command: { type: "alarm", action: "show" }
required: show timers
keywords: show timers

// Example: Set alarm for 7:30 pm

Pattern("Set alarm for *:* *")
Setting alarm 
command: { type: "alarm", action: "alarm", hour: star[0], minutes: star[1], ampm: star[2] }
required: alarm
keywords: set alarm

// Example: Set alarm for 6 am

Pattern("Set alarm for * *")
Setting alarm
command: { type: "alarm", action: "alarm", hour: star[0], ampm: star[1] }
required: alarm
keywords: set alarm

// Example: Set alarm for 8 o'clock am

Pattern("Set alarm for * o'clock *")
Setting alarm
command: { type: "alarm", action: "alarm", hour: star[0], ampm: star[1] }
required: alarm
keywords: set alarm

// Set an alarm with a name: Set Nap alarm for 4 pm

Pattern("Set * alarm for * *")
Setting alarm
command: { type: "alarm", action: "alarm", name: star[0], hour: star[1], ampm: star[2]}
required: alarm
keywords: set alarm


// Set a repeating alarm for weekdays/ends: Set alarm on monday, tuesday and thursday for 7 am

Pattern("Set alarm on * for * *")
Setting alarm for specified days
command: { type: "alarm", action: "alarm", day:""+star[0], hour: star[1], ampm: star[2] }
required: tuesday saturday friday alarm for wednesday thursday monday sunday
keywords: set alarm on

//Patterns for setting a timer

Set a timer
Opening timer
command: { type: "alarm", action: "timer" }
required: timer
keywords: set timer

Pattern("Set timer for * minutes")
Setting timer
command: { type: "alarm", action: "timer", minutes: star }
required: timer minutes
keywords: set timer minutes

Pattern("Set timer for * minute")
Setting timer
command: { type: "alarm", action: "timer", minutes: star }
required: timer minute
keywords: set timer minute

Pattern("Set timer for * hours")
Setting timer
command: { type: "alarm", action: "timer", hour: star }
required: timer hours
keywords: set timer hours

Pattern("Set timer for * hour")
Setting timer
command: { type: "alarm", action: "timer", hour: star }
required: timer hour
keywords: set timer hour

Pattern("Set timer for * hour * minutes")
Setting timer
command: { type: "alarm", action: "timer", hour: star[0], minutes: star[1] }
required: timer hour minutes
keywords: set timer hour minutes

Pattern("Set timer for * hours * minutes")
Setting timer
command: { type: "alarm", action: "timer", hour: star[0], minutes: star[1] }
required: timer hours minutes
keywords: set timer hours minutes

//Name your timer: Set Laundry timer for 30 minutes

Pattern("Set * timer for * minutes")
Setting timer
command: { type: "alarm", action: "timer", name: ""+star[0], minutes: star[1] }
required: timer minutes
keywords: set timer minutes

//Set Laundry timer for 1 hour or Set Homework timer for 3 hours

Pattern("Set * timer for * hour")
Setting timer
command: { type: "alarm", action: "timer", name: ""+star[0], hour: star[1] }
required: timer hour
keywords: set timer hour

Pattern("Set * timer for * hours")
Setting timer
command: { type: "alarm", action: "timer", name: ""+star[0], hour: star[1] }
required: timer hours
keywords: set timer hours

//Example commands:

//Set a specific and reusable alarm, "set work alarm": { type: "alarm", action: "alarm", hour: 7, minutes: 15, ampm: am}

//Set a specific and reusable timer, "set laundry timer": { type: "alarm", action: "timer", minutes: 30 }





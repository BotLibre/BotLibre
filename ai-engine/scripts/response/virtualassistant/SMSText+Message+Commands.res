//SMS Commands

Send text
Opening contacts
command: { type: "sms", action: "send" }
required: send text
keywords: send text

//Send to a phone number, must be in "18882225555" format

Pattern("Send text to * with message *")
Sending text
command: { type: "sms", action: "send", number: star[0], message: ""+star[1] }
required: send text to with message
keywords: text message

//Example command for common contacts, "Send text to mom": { type: "sms", action: "send", number: "18882225555" }
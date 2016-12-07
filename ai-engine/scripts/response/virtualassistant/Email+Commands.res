//Send email to one email address

Pattern("Send email to *")
Sending email
command: { type : "email", action : "send", address:""+ star }
required: email to
keywords: send email

//Send email with subject

Pattern("Send email to * with subject *")
Sending email
command: { type : "email", action : "send", address:""+ star[0], subject: ""+star[1] }
required: email with subject
keywords: send email subject

//Send email with subject and message body

Pattern("Send email to * with subject * and message *")
Sending email
command: { type : "email", action : "send", address:""+ star[0], subject: ""+star[1], message: ""+star[2] }
required: email with subject and message
keywords: send email subject message

//Example command to set a contact to email: { type : "email", action : "send", address : "mom22@rogers.com", subject: "Hey", message : "Hi mom!"}

//Example command to email a group: { type : "email", action : "send", subject: "Gaming this week", address : "walkerChrisranger@gmail.com, gamer64@bell.ca, toldYaSo554@hotmail.com", message : "Hey guys, "}
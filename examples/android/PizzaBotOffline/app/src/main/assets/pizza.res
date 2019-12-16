greeting: Welcome to Joe's Pizza. Would you like to order a pizza? <button>Yes</button>
label: #greet

default: Sorry, I can only answer questions on Joe's Pizza.

what is your location
Joe's Pizza is located on 23 Main St.
keywords: location address

yes
Template("{redirect("start")}")
keywords: yes yep okay ok y ya yah
previous: #greet

order pizza
Template("{redirect("start")}")
keywords: order pizza
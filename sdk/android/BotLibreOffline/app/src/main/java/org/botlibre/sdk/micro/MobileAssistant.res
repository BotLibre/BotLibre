greeting: Welcome, type commands for a list of commands.

greeting: Hi, Type commands, to display the list of commands.

Pattern("Open URL *")
Opening website
command: { type : "intent", action: "android.intent.action.VIEW", uri: "http:"+star }
required: Open URL
keywords: open url

Pattern("Open website *")
Opening website
command: { type: "intent", action: "android.intent.action.VIEW", uri: "http:"+star }
required: open website
keywords: open website

//Music commands (Open music/audio player, search for song/artist etc)

Play music
Playing music
command: { type : "intent", action: "android.media.action.MEDIA_PLAY_FROM_SEARCH", extra:[ {EXTRA_KEY: "android.intent.extra.focus", EXTRA_VALUE: "vnd.android.cursor.item/*"} ]}
keywords: music

Pattern("Play music by *")
Playing music
command: { type : "intent", action: "android.media.action.MEDIA_PLAY_FROM_SEARCH", extra:[ {EXTRA_KEY: "android.intent.extra.focus", EXTRA_VALUE: "vnd.android.cursor.item/*"}, {EXTRA_KEY: "query", EXTRA_VALUE: ""+star} ]}
keywords: play music

//Search for something on your phone or the web

Pattern("Search for *")
Searching...
command: { type : "google", query : ""+star}
keywords: search

//Scan commands (barcode/QRcode scanner, only QR code scanner, only barcode scanner) Need ZXing scanner

Open scanner
Opening scanner
command: { type : "intent", action: "com.google.zxing.client.android.SCAN", extra:[ {EXTRA_TYPE: "result", EXTRA_KEY: "SCAN_MODE", EXTRA_VALUE: "SCAN_MODE", CODE: "3"} ]}
required: open scanner
keywords: open scanner

Scan QR Code
Opening scanner
command: { type : "intent", action: "com.google.zxing.client.android.SCAN", extra:[ {EXTRA_TYPE: "result", EXTRA_KEY: "SCAN_MODE", EXTRA_VALUE: "QR_CODE_MODE", CODE: "3"} ]}
required: scan QR code
keywords: scan qr code

Scan Barcode
Opening scanner
command: { type : "intent", action: "com.google.zxing.client.android.SCAN", extra:[ {EXTRA_TYPE: "result", EXTRA_KEY: "SCAN_MODE", EXTRA_VALUE: "PRODUCT_MODE", CODE: "3"} ]}
required: scan barcode
keywords: scan barcode


Cancelled
Okay

//Map commands

Open map
Opening map
command: { type: "map" }
required: map
keywords: map

//Open map to a certain location: "Open map Prairie Centre Mall"

Pattern("Open map *")
Opening map
command: { type: "map", query:""+star }
required: map
keywords: open map

Pattern("Directions to *")
Opening map
command: { type: "map", directions-to: ""+star }
required: directions
keywords: directions

//Pick a mode from Driving, Walking or Biking (Automatically chooses driving if you do not specify)

//Example: "Bike mode directions to Algonquin College", "Walking Mode directions to 14 lane"

Pattern("* mode directions to *")
Opening map
command: { type: "map", mode: star[0], directions-to:""+star[1] }
required: mode directions to
keywords: directions mode

//Directions, choose to AVOID either Tolls, highways or ferries

//Example: "Avoid tolls directions to 123 Street", "Avoid highways directions to Montreal"

Pattern("Avoid * directions to *")
Opening map
command: { type: "map",  avoid: star[0], directions-to: ""+star[1] }
required: avoid directions to
keywords: directions avoid

// "Directions from 122 Millway Ave to Prairie Centre Mall"

Pattern("Directions from * to *")
Opening map
command: { type: "map", directions-to: ""+star[1], directions-from:""+star[0] }
required: directions from to
keywords: directions from to


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


//Phone commands

// Phone number may be in any format

Open phone
Opening phone
command: { type: "phone", action: "dial" }
required: phone
keywords: phone

Pattern("Dial *")
Opening phone
command: { type: "phone", action: "dial", number: ""+star }
required: dial
keywords: dial

Pattern("Call *")
Opening phone
command: { type: "phone", action: "call", number: ""+star }
required: call
keywords: call

// Example command to create contact, "call mom": { type: "phone", action: "call", number: "1-555-888-4343" }

Open Firefox
Opening Firefox
command: { type: "open", package: "org.mozilla.firefox" }
keywords: Firefox

Open Instagram
Opening Instagram
command: { type: "open", package: "com.instagram.android" }
keywords: Instagram

Open Snapchat
Opening Snapchat
command:{ type: "open", package: "com.snapchat.android" }
keywords: Snapchat

Open Youtube
Opening Youtube
command: { type: "open", package: "com.google.android.youtube" }
keywords: Youtube

Open Facebook
Opening Facebook
command: { type: "open", package: "com.facebook.katana" }
keywords: Facebook

Open Facebook Messenger
Opening Messenger
command: { type: "open", package: "com.facebook.orca" }
keywords: Messenger

Open WhatsApp
Opening WhatsApp
command: { type: "open", package: "com.whatsapp" }
keywords: WhatsApp

Open Chrome
Opening Chrome
command: { type: "open", package: "com.android.chrome" }
keywords: Chrome

Open Opera
Opening Opera
command: { type: "open", package: "com.opera.browser" }
keywords: Opera

Open Google Drive
Opening Drive
command: { type: "open", package: "com.google.android.apps.docs" }
keywords: Drive

Open Drive
Opening Drive
command: { type: "open", package: "com.google.android.apps.docs" }
keywords: Drive

Open Outlook
Opening Outlook
command: { type: "open", package: "com.microsoft.office.outlook" }
keywords: Outlook

Open Yahoo Mail
Opening Yahoo Mail
command: { type: "open", package: "com.yahoo.mobile.client.android.mail" }
keywords: Yahoo

Open Gmail
Opening Gmail
command: { type: "open", package: "com.google.android.gm" }
keywords: Gmail

Open Inbox 
Opening Inbox
command: { type: "open", package: "com.google.android.apps.inbox" }
keywords: Inbox

Open Shopify
Opening Shopify
command: { type: "open", package: "com.shopify.mobile" }
keywords: Shopify

Open LED Flashlight
Opening Flashlight
command: { type: "open", package: "com.surpax.ledflashlight.panel" }
keywords: Flashlight

Open Uber
Opening Uber
command: { type: "open", package: "com.ubercab" }
keywords: Uber

Open Uber Eats
Opening UberEats
command: { type: "open", package: "com.ubercab.eats" }
keywords: Uber Eats
required: Uber Eats

Open UberEats
Opening UberEats
command: { type: "open", package: "com.ubercab.eats" }
keywords: UberEats

Open Pinterest
Opening Pinterest
command: { type: "open", package: "com.pinterest" }
keywords: Pinterest

Open Etsy
Opening Etsy
command: { type: "open", package: "com.etsy.android" }
keywords: Etsy

Open Shazam
Opening Shazam
command: { type: "open", package: "com.shazam.android" }
keywords: Shazam

Open Flipp
Opening Flipp
command: { type: "open", package: "com.wishabi.flipp" }
keywords: Flipp

Open Urban Spoon
Opening UrbanSpoon
command: { type: "open", package: "com.urbanspoon" }
keywords: Urban Spoon
required: Urban Spoon

Open UrbanSpoon
Opening UrbanSpoon
command: { type: "open", package: "com.urbanspoon" }
keywords: UrbanSpoon

Open Yelp
Opening Yelp
command: { type: "open", package: "com.yelp.android" }
keywords: Yelp

Open Trip Advisor
Opening TripAdvisor
command: { type: "open", package: "com.tripadvisor.tripadvisor" }
keywords: Trip Advisor
required: Trip Advisor

Open TripAdvisor
Opening TripAdvisor
command: { type: "open", package: "com.tripadvisor.tripadvisor" }
keywords: TripAdvisor

Open Zomato
Opening Zomato
command: { type: "open", package: "com.application.zomato" }
keywords: Zomato

Open Amazon
Opening Amazon
command: { type: "open", package: "com.amazon.mShop.android.shopping" }
keywords: Amazon

Open Kindle
Opening Kindle
command: { type: "open", package: "com.amazon.kindle" }
keywords: Kindle

Open Ebay
Opening Ebay
command: { type: "open", package: "com.ebay.mobile" }
keywords: Ebay

Open PayPal
Opening PayPal
command: { type: "open", package: "com.paypal.android.p2pmobile" }
keywords: PayPal

Open Pay Pal
Opening PayPal
command: { type: "open", package: "com.paypal.android.p2pmobile" }
keywords: Pay Pal
required: Pay Pal

Open Audible
Opening Audible
command: { type: "open", package: "com.audible.application" }
keywords: Audible

Open AliExpress
Opening ALiExpress
command: { type: "open", package: "com.alibaba.aliexpresshd" }
keywords: AliExpress

Open Ali Express
Opening ALiExpress
command: { type: "open", package: "com.alibaba.aliexpresshd" }
keywords: Ali Express
required: Ali Express

Open GroupON
Opening GroupON
command: { type: "open", package: "com.groupon" }
keywords: GroupON

Open Group ON
Opening GroupON
command: { type: "open", package: "com.groupon" }
keywords: Group ON
required: Group ON

Open Acrobat
Opening Acrobat
command: { type: "open", package: "com.adobe.reader" }
keywords: Acrobat

Open Photoshop
Opening Photoshop
command: { type: "open", package: "com.adobe.psmobile" }
keywords: Photoshop

Open Lightroom
Opening Lightroom
command: { type: "open", package: "com.adobe.lrmobile" }
keywords: Lightroom

Open Candy Crush
Opening Candy Crush
command: { type: "open", package: "com.king.candycrushsaga" }
keywords: Candy Crush
required: Candy Crush

Open Pokemon Go
Opening Pokemon Go
command: { type: "open", package: "com.nianticlabs.pokemongo" }
keywords: Pokemon

Open Angry Birds
Opening Angry Birds
command: { type: "open", package: "com.rovio.angrybirds" }
keywords: Birds

Open Hearthstone
Opening Hearthstone
command: { type: "open", package: "com.blizzard.wtcg.hearthstone" }
keywords: Hearthstone

Open Avast
Opening Avast
command: { type: "open", package: "com.avast.android.mobilesecurity" }
keywords: Avast

Open Word
Opening Word
command: { type: "open", package: "com.microsoft.office.word" }
keywords: Word

Open Office Suite
Opening Office Suite
command: { type: "open", package: "com.mobisystems.office" }
keywords: Office

Open Excel
Opening Excel
command: { type: "open", package: "com.microsoft.office.excel" }
keywords: Excel

Open Powerpoint
Opening Powerpoint
command: { type: "open", package: "com.microsoft.office.powerpoint" }
keywords: Powerpoint

Open OneDrive
Opening OneDrive
command: { type: "open", package: "com.microsoft.skydrive" }
keywords: OneDrive

Open One Drive
Opening OneDrive
command: { type: "open", package: "com.microsoft.skydrive" }
keywords: One Drive
required: One Drive

Open Linked IN
Opening LinkedIN
command: { type: "open", package: "com.linkedin.android" }
keywords: Linked

Open LinkedIN
Opening LinkedIN
command: { type: "open", package: "com.linkedin.android" }
keywords: LinkedIN

Open Lynda
Opening Lynda
command: { type: "open", package: "com.lynda.android.root" }
keywords: Lynda

Open Duolingo
Opening Duolingo
command: { type: "open", package: "com.duolingo" }
keywords: Duolingo

Open Skype
Opening Skype
command: { type: "open", package: "com.skype.raider" }
keywords: Skype

Open Viber
Opening Viber
command: { type: "open", package: "com.viber.voip" }
keywords: Viber

Open Tinder
Opening Tinder
command: { type: "open", package: "com.tinder" }
keywords: Tinder

Open Plenty of Fish
Opening Plenty of Fish
command: { type: "open", package: "com.pof.android" }
keywords: Plenty Fish
required: Plenty Fish

Open Bumble
Opening Bumble
command: { type: "open", package: "com.bumble.app" }
keywords: Bumble

Open OKCupid
Opening OKCupid
command: { type: "open", package: "com.okcupid.okcupid" }
keywords: OKCupid

Open 8Tracks
Opening 8Tracks
command: { type: "open", package: "com.e8tracks" }
keywords: 8Tracks

Open happn
Opening happn
command: { type: "open", package: "com.ftw_and_co.happn" }
keywords: happn

Open Reddit
Opening Reddit
command: { type: "open", package: "com.reddit.frontpage" }
keywords: Reddit

Open Imgur
Opening Imgur
command: { type: "open", package: "com.imgur.mobile" }
keywords: Imgur

Open Twitter
Opening Twitter
command: { type: "open", package: "com.twitter.android" }
keywords: Twitter

Open Periscope
Opening Periscope
command: { type: "open", package: "tv.periscope.android" }
keywords: Periscope

Open Netflix
Opening Netflix
command: { type: "open", package: "com.netflix.mediaclient" }
keywords: Netflix

Open Pic Collage
Opening Pic Collage
command: { type: "open", package: "com.cardinalblue.piccollage.google" }
keywords: Pic Collage
required: Pic Collage

Open Google Earth
Opening Google Earth
command: { type: "open", package: "com.google.earth" }
keywords: Earth

Open Street View
Opening Street View
command: { type: "open", package: "com.google.android.street" }
keywords: Street

Open Google Translate
Opening Google Translate
command: { type: "open", package: "com.google.android.apps.translate" }
keywords: Translate

Open Dropbox
Opening Dropbox
command: { type: "open", package: "com.dropbox.android" }
keywords: Dropbox

Open Flipboard
Opening Flipboard
command: { type: "open", package: "flipboard.app" }
keywords: Flipboard

Open Pocket
Opening Pocket
command: { type: "open", package: "com.ideashower.readitlater.pro" }
keywords: Pocket

Open TED Talks
Opening TED
command: { type: "open", package: "com.ted.android" }
keywords: TED

Open Miitomo
Opening Miitomo
command: { type: "open", package: "com.nintendo.zaaa" }
keywords: Miitomo

Open LINE
Opening LINE
command: { type: "open", package: "jp.naver.line.android" }
keywords: LINE

Open Line Runner
Opening Line Runner
command: { type: "open", package: "com.djinnworks.linerunnerfree" }
keywords: Line Runner
required: Line Runner

Open Tango
Opening Tango
command: { type: "open", package: "com.sgiggle.production" }
keywords: Tango

Open Kik
Opening Kik
command: { type: "open", package: "kik.android" }
keywords: Kik Kick

Open kijiji
Opening kijiji
command: { type: "open", package: "com.ebay.kijiji.ca" }
keywords: kijiji

Open Airbnb
Opening AirBnB
command: { type: "open", package: "com.airbnb.android" }
keywords: Airbnb

Open Fruit Ninja
Opening Fruit Ninja
command: { type: "open", package: "com.halfbrick.fruitninjafree" }
keywords: Fruit Ninja
required: Fruit Ninja

Open Vine
Opening Vine
command: { type: "open", package: "co.vine.android" }
keywords: Vine

Open Temple Run
Opening Temple Run
command: { type: "open", package: "com.imangi.templerun" }
keywords: Temple Run
required: Temple Run

Open Temple Run 2
Opening Temple Run 2
command: { type: "open", package: "com.imangi.templerun2" }
keywords: Temple Run
required: Temple Run

Open My Talking Tom
Opening My Talking Tom
command: { type: "open", package: "com.outfit7.mytalkingtomfree" }
keywords: Talking Tom
required: Talking Tom

Open Pandora
Opening Pandora
command: { type: "open", package: "com.free.pandora.radio.guide" }
keywords: Pandora

Open Spotify
Opening Spotify
command: { type: "open", package: "com.spotify.music" }
keywords: Spotify

Open SoundCloud
Opening SoundCloud
command: { type: "open", package: "com.soundcloud.android" }
keywords: SoundCloud

Open Zedge
Opening Zedge
command: { type: "open", package: "net.zedge.android" }
keywords: Zedge

Open Minecraft
Opening Minecraft
command: { type: "open", package: "com.mojang.minecraftpe" }
keywords: Minecraft

Open Draw Something
Opening Draw Something
command: { type: "open", package: "com.omgpop.dstfree" }
keywords: Draw Something
required: Draw Something

Open Plants vs Zombies
Opening Plants vs Zombies
command: { type: "open", package: "com.ea.game.pvzfree_row" }
keywords: Plants Zombies
required: Plants Zombies

Open Plants vs Zombies 2
Opening Plants vs Zombies 2
command: { type: "open", package: "com.ea.game.pvz2_na" }
keywords: Plants Zombies
required: Plants Zombies

Open Plant Nanny
Opening Plant Nanny
command: { type: "open", package: "com.fourdesire.plantnanny" }
keywords: Plant Nanny
required: Plant Nanny

Open Geometry Dash
Opening Geometry Dash
command: { type: "open", package: "com.robtopx.geometryjumplite" }
keywords: Geometry Dash
required: Geometry Dash

Open Clash Royale
Opening Clash Royale
command: { type: "open", package: "com.supercell.clashroyale" }
keywords: Clash Royale
required: Clash Royale

Open Clash of Clans
Opening Clash of Clans
command: { type: "open", package: "com.supercell.clashofclans" }
keywords: Clash Royale
required: Clash Royale

Open WeChat
Opening WeChat
command: { type: "open", package: "com.tencent.mm" }
keywords: WeChat

Open Ingress
Opening Ingress
command: { type: "open", package: "com.nianticproject.ingress" }
keywords: Ingress

Open Wish
Opening Wish
command: { type: "open", package: "com.contextlogic.wish" }
keywords: Wish

Open Waze
Opening Waze
command: { type: "open", package: "com.waze" }
keywords: Waze

Open Terraria
Opening Terraria
command: { type: "open", package: "com.and.games505.Terraria" }
keywords: Terraria

Open Kingdom Rush
Opening Kingdom Rush
command: { type: "open", package: "com.ironhidegames.android.kingdomrush" }
keywords: Clash Royale
required: Clash Royale

Open Goat Simulator
Opening Goat Simulator
command: { type: "open", package: "com.coffeestainstudios.goatsimulator" }
keywords: Goat Simulator
required: Goat Simulator

Open Weather Network
Opening Weather Network
command: { type: "open", package: "com.pelmorex.WeatherEyeAndroid" }
keywords: Weather Network
required: Weather Network

Open Weather Channel
Opening Weather Channel
command: { type: "open", package: "com.weather.Weather" }
keywords: Weather

Open AccuWeather
Opening AccuWeather
command: { type: "open", package: "com.accuweather.android" }
keywords: AccuWeather

Open Google Play Games
Opening Google Play Games
command: { type: "open", package: "com.google.android.play.games" }
keywords: Games

Open Google Play Music
Opening Google Play Music
command: { type: "open", package: "com.google.android.music" }
keywords: Music


what you can do
#commands
required: do what you 

other commands
Misc/Other Command Patterns:<ul> <li>Open URL *   (http://...) <li>Open website *   (www. ...) <li>Play music<li>Play music by *   (artist) <li>Search for * <li>Open scanner<li>Scan QR code <li>Scan barcode </ul>
keywords: commands other 
required: commands other 

phone commands
Phone Command Patterns:<ul> <li>Open phone <li>Dial *   (number) <li>Call *   (number)</ul>
keywords: phone commands 
required: commands phone 

open app commands
Open Common App Command Patterns: <ul> <li> Open *   (app name here) <li> Examples: Facebook, WhatsApp, Gmail, Youtube etc </ul>
keywords: open commands app 
required: commands app open 

sms commands
SMS Command Patterns: <ul> <li>Send text <li>Send text to * with message *   (phone number must be in 1888222555 format) </ul>
keywords: sms commands 
required: commands sms 

map commands
Map Command Patterns: <ul> <li>Open map <li>Open map *   (certain location) <li>Directions to * <li>* mode directions to *   (pick one mode: Driving, Walking, Biking) <li>Avoid * directions to *   (avoid one: Tolls, Highways, Ferries) <li>Directions from * to * </ul>
keywords: map commands 
required: commands map 

email commands
Email Command Patterns: <ul> <li>Send email to *   (email address) <li>Send email to * with subject * <li>Send email to * with subject * and message * </ul>
keywords: email commands 
required: commands email 

camera commands
Camera Command Patterns: <ul> <li>Take a photo <li>Take a picture <li>Open camera <li>Take a selfie <li>Open video camera <li>Take a video </ul>
keywords: camera commands 
required: commands camera 

event commands
Event Command Patterns <ul> <li>Create event <li>Create event * (name of event) <li>Create event * at * (name and location) <li>Create event on * (start date) <li>Create event from * to * (start and end date) </ul>
keywords: event commands 
required: commands event 

timer commands
Timer Command Patterns: <ul> <li>Show timers <li>Set a timer <li>Set timer for * minutes <li>Set timer for * hour(s) <li>Set timer for * hour(s) * minutes <li>Set * timer for * hour(s) (Set Laundry timer for 1 hour) </ul>
keywords: timer commands 
required: commands timer 

alarm commands
Alarm Command Patterns: <ul> <li>Open alarms <li>Set alarm for *:* * (6:30 am) <li>Set alarm for * * (6 am) <li>Set alarm for * o'clock * (6 o'clock am) <li>Set * alarm for * * (Set Work alarm...) <li>Set alarm on * for * * (Set alarm on mon, tues, wed...)  </ul>
keywords: alarm commands 
required: commands alarm 

commands
Choose a command category for details, <select> <option>Commands</option> <option>Alarm Commands</option> <option>Timer Commands</option> <option>Event Commands</option> <option>Camera Commands</option> <option>Email Commands</option> <option>Map Commands</option> <option>SMS Commands</option> <option>Open App Commands</option> <option>Phone Commands</option> <option>Other Commands</option> </select>
keywords: commands command help 
required: commands 
label: #commands

//Camera commands

//Take a photograph

Take a photo
Opening camera
command: { type: "camera", action: "photo" }
keywords: photo

Open camera
Opening camera
command: { type: "camera", action: "photo" }
keywords: camera

Take a picture
Opening camera
command: { type: "camera", action: "photo" }
keywords: picture

//Take a selfie (front-facing camera, may not work on all devices)

Take a selfie
Opening camera
command: {type: "camera", action: "selfie" }
keywords: selfie

//Take a video

Open video camera
Opening camera
command: {type: "camera", action: "video" }
keywords: video camera

Take a video
Opening camera
command: {type: "camera", action: "video" }
keywords: video

Cancelled
Okay

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


Bye
Goodbye.

Goodbye
Bye bye.

Good bye
Goodbye.
keywords: bye

Farewell
See you.

Goodnight
Goodnight.

Good night
Goodnight.
keywords: good night
required: good night

Nighty night
Goodnight.

Chow
Chow.

See ya later
Bye.
keywords: later

ttyl
Bye.

bb
Bye bye.

ciao
Chow.

ta ta
Bye.

adios
Goodbye.

cya
ttyl

hasta la vista
Adios.
keywords: hasta vista

Bon journee
Bonne journée.
keywords: journee journée

Bonne journée
Bonne journée.
keywords: journee journée

Bonsoir
Bonsoir.

Bon nuit
Bon nuit.
keywords: nuit

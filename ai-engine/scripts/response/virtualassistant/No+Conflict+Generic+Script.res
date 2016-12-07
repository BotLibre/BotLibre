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
required: play music
keywords: play music

Pattern("Play music by *")
Playing music
command: { type : "intent", action: "android.media.action.MEDIA_PLAY_FROM_SEARCH", extra:[ {EXTRA_KEY: "android.intent.extra.focus", EXTRA_VALUE: "vnd.android.cursor.item/*"}, {EXTRA_KEY: "query", EXTRA_VALUE: ""+star} ]}
required: play music by
keywords: play music by

//Search for something on your phone or the web

Pattern("Search for *")
Searching...
command: { type : "intent", action: "android.intent.action.SEARCH", extra:[ {EXTRA_KEY: "query", EXTRA_VALUE: ""+star} ]}
required: search for
keywords: search for

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

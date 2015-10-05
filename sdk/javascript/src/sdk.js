/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

/**
 * Project libre open SDK.
 * This JavaScript SDK lets you access chat bot, live chat, chatroom, and forum services on
 * the Project libre supporting websites, including:
 * - Paphus Live Chat
 * - BOT libre!
 * - LIVE CHAT libre!
 * - FORUMS libre!
 * 
 * This JavaScript script can be used directly, or copied/modified on your own website.
 * 
 * The SDK consist of two main class, SDKConnection and LiveChatConnection.
 * 
 * SDKConnection uses AJAX calls to provide access to the libre REST API.
 * This is used for chat bots, forums, user admin, and domains.
 * 
 * LiveChatConnection uses web sockets to provide access to live chat and chatrooms.
 * Version: 2.7.8-2015-08-10
 */

/**
 * Static class for common util functions and static properties.
 * @class
 */
var SDK = {};

SDK.DOMAIN = "www.botlibre.com";
//SDK.DOMAIN = window.location.host;
SDK.APP = "";
//SDK.APP = "/botlibre";
SDK.PATH = "/rest/api";
SDK.MAX_FILE_UPLOAD = 2000000;

SDK.host = SDK.DOMAIN;
SDK.app = SDK.APP;
SDK.scheme = 'https:' == document.location.protocol ? "https" : "http";
SDK.url = SDK.scheme + "://" + SDK.DOMAIN + SDK.APP;
SDK.rest = SDK.url + SDK.PATH;

/**
 * You must set your application ID to use the SDK.
 * You can obtain your application ID from your user page.
 * @static
 */
SDK.applicationId = null;

/**
 * Enable debug logging.
 * @static
 */
SDK.debug = false;

/**
 * Force avatars to use canvas for video (currently used only for Chrome and Firefox).
 * @static
 */
SDK.useCanvas = null;

/**
 * Attempt to fix grey mp4 video background (only used for Chrome).
 * @static
 */
SDK.fixBrightness = null;

/**
 * Set the error static field to trap or log any errors.
 */
SDK.error = function(message) {
	console.log(message);
}

SDK.currentAudio = null;
SDK.recognition = null;
SDK.recognitionActive = false;
SDK.backgroundAudio = null;
SDK.timers = {};

/**
 * Play the audio file given the url.
 */
SDK.play = function(file, channelaudio) {
	SDK.pauseSpeechRecognition();
	var audio = new Audio(file);
	if (SDK.recognitionActive) {
		audio.addEventListener('ended', function() {
			SDK.startSpeechRecognition();
		}, false);
	}
	if (channelaudio == false) {
		audio.play();
		return audio;
	}
	if (SDK.currentAudio != null && !SDK.currentAudio.ended) {
		SDK.currentAudio.addEventListener('pause', function() {
			SDK.currentAudio = audio;
			audio.play();
		}, false);
		SDK.currentAudio.pause();
	} else {
		SDK.currentAudio = audio;
		audio.play();
	}
	return audio;
}

SDK.playChime = true;
/**
 * Play the chime sound.
 */
SDK.chime = function() {
	if (SDK.playChime) {
		this.play(SDK.url + '/chime.wav');
		SDK.playChime = false;
		var timer = setInterval(function () {
			SDK.playChime = true;
			clearInterval(timer);
		}, 1000);
	}
}

/**
 * Convert the text to speech and play it either using the browser native TTS support, or as server generated an audio file.
 * The voice is optional and can be any voice supported by the server (see the voice page for a list of voices).
 * For native voices a language code can be given.
 * If the browser supports TTS the native voice will be used by default.
 */
SDK.tts = function(text, voice, native, lang, nativeVoice) {
	try {
		if ((native || (native == null && voice == null)) && ('speechSynthesis' in window)) {
			var utterance = new SpeechSynthesisUtterance(text);
			SDK.nativeTTS(utterance, lang, nativeVoice);
		} else {		
			var url = SDK.rest + '/form-speak?&text=';
			url = url + encodeURIComponent(text);
			if (voice != null) {
				url = url + '&voice=' + voice;
			}
	
			var request = new XMLHttpRequest();
			var self = this;
			request.onreadystatechange = function() {
				if (request.readyState != 4) return;
				if (request.status != 200) {
					console.log('Error: Speech web request failed');
					return;
				}
				self.play(SDK.url + "/" + request.responseText);
			}
			
			request.open('GET', url, true);
			request.send();
		}
	} catch (error) {
		console.log('Error: Speech web request failed');
	}
}

/**
 * Speak the native utterance first setting the voice and language.
 */
SDK.nativeTTS = function(utterance, lang, voice) {
	speechSynthesis.cancel();
	if (lang == null && voice == null) {
		// Events don't always get fired unless this is done...
		setTimeout(function() {
			speechSynthesis.speak(utterance);
		},100);
		return;
	}
	var voices = speechSynthesis.getVoices();
	var foundVoice = null;
	var foundLang = null;
	var spoken = false;
	if (voices.length == 0) {
		speechSynthesis.onvoiceschanged = function() {
			if (spoken) {
				return;
			}
			voices = speechSynthesis.getVoices();
	    	for (i = 0; i < voices.length; i++) {
	    		if (voice != null && (voice.length != 0) && voices[i].name.toLowerCase().indexOf(voice.toLowerCase()) != -1) {
	    			if (foundVoice == null || voices[i].name == voice) {
		    			foundVoice = voices[i];	    				
	    			}
	    		} else if (lang != null && (lang.length != 0) && voices[i].lang.toLowerCase().indexOf(lang.toLowerCase()) != -1) {
	    			if (foundLang == null || voices[i].lang == lang) {
	    				foundLang = voices[i];	    				
	    			}
	    		}
	    	}
	    	if (foundVoice != null) {
	    		utterance.voice = foundVoice;
	    	} else if (foundLang != null) {
	    		utterance.voice = foundLang;	    		
	    	}
	    	spoken = true;
			setTimeout(function() {
				speechSynthesis.speak(utterance);
			},100);
	    };
	} else {
    	for (i = 0; i < voices.length; i++) {
    		if (voice != null && (voice.length != 0) && voices[i].name.toLowerCase().indexOf(voice.toLowerCase()) != -1) {
    			if (foundVoice == null || voices[i].name == voice) {
	    			foundVoice = voices[i];	    				
    			}
    		} else if (lang != null && (lang.length != 0) && voices[i].lang.toLowerCase().indexOf(lang.toLowerCase()) != -1) {
    			if (foundLang == null || voices[i].lang == lang) {
    				foundLang = voices[i];	    				
    			}
    		}
    	}
    	if (foundVoice != null) {
    		utterance.voice = foundVoice;
    	} else if (foundLang != null) {
    		utterance.voice = foundLang;	    		
    	}
		setTimeout(function() {
			speechSynthesis.speak(utterance);
		},100);
	}
}

/**
 * Detect Chrome browser.
 */
SDK.isChrome = function() {
	return navigator.userAgent.toLowerCase().indexOf('chrome') > -1;
}

/**
 * Detect Firefox browser.
 */
SDK.isFirefox = function() {
	return navigator.userAgent.toLowerCase().indexOf('firefox') > -1;
}

/**
 * Detect mobile browser.
 */
SDK.isMobile = function() {
	if (navigator.userAgent.match(/Android/i)
		 || navigator.userAgent.match(/webOS/i)
		 || navigator.userAgent.match(/iPhone/i)
		 || navigator.userAgent.match(/iPad/i)
		 || navigator.userAgent.match(/iPod/i)
		 || navigator.userAgent.match(/BlackBerry/i)
		 || navigator.userAgent.match(/Windows Phone/i)) {
		return true;
	} else {
		return false;
	}
}

/**
 * Insert the text into the input field.
 */
SDK.insertAtCaret = function(element, text) {
    if (document.selection) {
        element.focus();
        var sel = document.selection.createRange();
        sel.text = text;
        element.focus();
    } else if (element.selectionStart || element.selectionStart == 0) {
        var startPos = element.selectionStart;
        var endPos = element.selectionEnd;
        var scrollTop = element.scrollTop;
        element.value = element.value.substring(0, startPos) + text + element.value.substring(endPos, element.value.length);
        element.focus();
        element.selectionStart = startPos + text.length;
        element.selectionEnd = startPos + text.length;
        element.scrollTop = scrollTop;
    } else {
        element.value += text;
        element.focus();
    }
}

/**
 * Fix innerHTML for IE and Safari.
 */
SDK.innerHTML = function(element) {
	var html = element.innerHTML;
	if (html == null) {
		var div = document.createElement("div");
		div.appendChild(element);
		html = div.innerHTML;
	}
	return html;
}

/**
 * Strip HTML tags from text.
 * Return plain text.
 */
SDK.stripTags = function(html) {
	var element = document.createElement("p");
	element.innerHTML = html;
	return element.innerText || element.textContent;
}

SDK.escapeHTML = function(html) {
	return html.replace(/&/g, "&amp;")
    	.replace(/</g, "&lt;")
    	.replace(/>/g, "&gt;")
    	.replace(/"/g, "&quot;")
    	.replace(/'/g, "&#039;");
}

SDK.linkURLs = function(text) {
	var http = text.indexOf("http") != -1;
	var www = text.indexOf("www.") != -1;
	var email = text.indexOf("@") != -1;
	if (!http && !www && !email) {
		return text;
	}
	if (text.indexOf("<") != -1 && text.indexOf(">") != -1) {
		return text;
	}
	if (http) {
	    var regex = /\b(?:https?|ftp|file):\/\/[a-z0-9-+&@#\/%?=~_|!:,.;]*[a-z0-9-+&@#\/%=~_|]/gim;
	    text = text.replace(regex, function(url, b, c) {
	    	var lower = url.toLowerCase();
	    	if (lower.indexOf(".png") != -1 || lower.indexOf(".jpg") != -1 || lower.indexOf(".jpeg") != -1 || lower.indexOf(".gif") != -1) {
	    		return '<a href="' + url + '" target="_blank"><img src="' + url + '" height="50"></a>';
	    	} else if (lower.indexOf(".mp4") != -1 || lower.indexOf(".webm") != -1 || lower.indexOf(".ogg") != -1) {
	    		return '<a href="' + url + '" target="_blank"><video src="' + url + '" height="50"></a>';
	    	} else if (lower.indexOf(".wav") != -1 || lower.indexOf(".mp3") != -1) {
	    		return '<a href="' + url + '" target="_blank"><audio src="' + url + '" controls>audio</a>';
	    	} else {
	    		return '<a href="' + url + '" target="_blank">' + url + '</a>';
	    	}
	    });
	} else if (www) {
	    var regex = /((www\.)[^\s]+)/gim;
	    text = text.replace(regex, function(url, b, c) {
	        return '<a href="http://' + url + '" target="_blank">' + url + '</a>';
	    });
	}
    
    // http://, https://, ftp://
    //var urlPattern = /\b(?:https?|ftp):\/\/[a-z0-9-+&@#\/%?=~_|!:,.;]*[a-z0-9-+&@#\/%=~_|]/gim;

    // www. 
    // var wwwPattern = /(^|[^\/])(www\.[\S]+(\b|$))/gim;

    // name@domain.com
	if (email) {
    	var emailPattern = /(([a-zA-Z0-9_\-\.]+)@[a-zA-Z_]+?(?:\.[a-zA-Z]{2,6}))+/gim;
    	text = text.replace(emailPattern, '<a target="_blank" href="mailto:$1">$1</a>');
	}
	return text;
}

/**
 * Enable speech recognition if supported by the browser, and insert the voice to text to the input field.
 * Optionally call click() on the button.
 */
SDK.registerSpeechRecognition = function(input, button) {
	if (SDK.recognition == null) {
		if ('webkitSpeechRecognition' in window) {
			SDK.recognition = new webkitSpeechRecognition();
			SDK.recognition.continuous = true;
			SDK.recognition.onresult = function (event) {
			    for (var i = event.resultIndex; i < event.results.length; ++i) {
			        if (event.results[i].isFinal) {
			        	SDK.insertAtCaret(input, event.results[i][0].transcript);	        	
			        }
			    }
			    if (button != null) {
			    	button.click();
				}
			};
		} else {
			return;
		}
	}
}

SDK.startSpeechRecognition = function() {
	if (SDK.recognition != null) {
		SDK.recognition.start();
		SDK.recognitionActive = true;
	}
}

SDK.pauseSpeechRecognition = function() {
	if (SDK.recognition != null) {
		SDK.recognition.stop();
	}
}

SDK.stopSpeechRecognition = function() {
	if (SDK.recognition != null) {
		SDK.recognition.stop();
		SDK.recognitionActive = false;
	}
}

SDK.popupwindow = function(url, title, w, h) {
	var left = (screen.width)-w-10;
	var top = (screen.height)-h-100;
	window.open(url, title, 'scrollbars=yes, resizable=yes, toolbar=no, location=no, directories=no, status=no, menubar=no, copyhistory=no, width='+w+', height='+h+', top='+top+', left='+left);
	return false;
}

SDK.dataURLToBlob = function(dataURL) {
    var marker = ';base64,';
    if (dataURL.indexOf(marker) == -1) {
        var parts = dataURL.split(',');
        var contentType = parts[0].split(':')[1];
        var raw = parts[1];

        return new Blob([raw], {type: contentType});
    }

    var parts = dataURL.split(marker);
    var contentType = parts[0].split(':')[1];
    var raw = window.atob(parts[1]);
    var rawLength = raw.length;

    var blobarray = new Uint8Array(rawLength);

    for (var i = 0; i < rawLength; ++i) {
    	blobarray[i] = raw.charCodeAt(i);
    }

    return new Blob([blobarray], {type: contentType});
}

/**
 * Credential used to establish a connection.
 * Defines the url, host, app, rest, which are all defaulted and should not need to be changed,
 * Requires an application id.
 * You can obtain your application id from your user details page on the hosting website.
 * @class
 */
function Credentials() {
	this.host = SDK.host;
	this.app = SDK.app;
	this.url = SDK.url;
	this.rest = SDK.rest;
	this.applicationId = SDK.applicationId;
}

/**
 * Credentials for use with hosted services on the BOT libre website, a free bot hosting service.
 * http://www.botlibre.com
 * @class
 */
function BOTlibreCredentials()  {
	this.DOMAIN = "www.botlibre.com";
	//this.DOMAIN = window.location.host;
	this.APP = "";
	//this.APP = "/botlibre";
	this.PATH = "/rest/botlibre";
	
	this.host = this.DOMAIN;
	this.app = this.APP;
	this.url = "http://" + this.DOMAIN + this.APP;
	this.rest = this.url + this.PATH;
	this.applicationId = SDK.applicationId;
}

/**
 * Credentials for use with hosted services on the Paphus Live Chat website,
 * a commercial live chat, chatroom, forum, and chat bot, hosting service.
 * http://www.paphuslivechat.com
 * @class
 */
function PaphusCredentials()  {
	this.DOMAIN = "www.paphuslivechat.com";
	this.APP = "";
	this.PATH = "/rest/livechat";
	
	this.host = this.DOMAIN;
	this.app = this.APP;
	this.url = "http://" + this.DOMAIN + this.APP;
	this.rest = this.url + this.PATH;
	this.applicationId = SDK.applicationId;
}

/**
 * Credentials for use with hosted services on the LIVE CHAT libre website, a free live chat, chatrooms, forum, and chat bots that learn.
 * http://www.livechatlibre.com
 * @class
 */
function LIVECHATlibreCredentials()  {
	this.DOMAIN = "www.livechatlibre.com";
	this.APP = "";
	this.PATH = "/rest/livechatlibre";
	
	this.host = this.DOMAIN;
	this.app = this.APP;
	this.url = "http://" + this.DOMAIN + this.APP;
	this.rest = this.url + this.PATH;
	this.applicationId = SDK.applicationId;
}

/**
 * Credentials for use with hosted services on the FORUMS libre website, a free embeddable forum hosting service.
 * http://www.forumslibre.com
 * @class
 */
function FORUMSlibreCredentials()  {
	this.DOMAIN = "www.forumslibre.com";
	this.APP = "";
	this.PATH = "/rest/forumslibre";
	
	this.host = this.DOMAIN;
	this.app = this.APP;
	this.url = "http://" + this.DOMAIN + this.APP;
	this.rest = this.url + this.PATH;
	this.applicationId = SDK.applicationId;
}

/**
 * Listener interface for a LiveChatConnection.
 * This gives asynchronous notification when a channel receives a message, or notice.
 * @class
 */
function LiveChatListener() {
	/**
	 * A user message was received from the channel.
	 */
	this.message = function(message) {};
	
	/**
	 * An informational message was received from the channel.
	 * Such as a new user joined, private request, etc.
	 */	
	this.info = function(message) {};

	/**
	 * An error message was received from the channel.
	 * This could be an access error, or message failure.
	 */	
	this.error = function(message) {};
	
	/**
	 * Notification that the connection was closed.
	 */
	this.closed = function() {};
	
	/**
	 * The channels users changed (user joined, left, etc.)
	 * This contains a comma separated values (CSV) list of the current channel users.
	 * It can be passed to the SDKConnection.getUsers() API to obtain the UserConfig info for the users.
	 */
	this.updateUsers = function(usersCSV) {};

	/**
	 * The channels users changed (user joined, left, etc.)
	 * This contains a HTML list of the current channel users.
	 * It can be inserted into an HTML document to display the users.
	 */
	this.updateUsersXML = function(usersXML) {};
}

/**
 * The WebLiveChatListener provides an integration between a LiveChatConnection and an HTML document.
 * It updates the document to message received from the connection, and sends messages from the document's form.
 * The HTML document requires the following elements:
 * - chat - <input type='text'> chat text input for sending messages
 * - send - <input type='submit'> button for sending chat input
 * - response - <p> paragraph for last chat message
 * - console - <table> table for chat log, and user log
 * - scroller - <div> div for chat log scroll pane
 * - online - <table> table for chat user list
 * @class
 */
function WebLiveChatListener() {
	this.switchText = true;
	this.playChime = true;
	this.speak = false;
	this.voice = null;
	this.nativeVoice = null;
	this.nativeVoiceName = null;
	this.lang = null;
	this.nick = "";
	this.connection = null;
	this.sdk = null;
	/** Configure if chat should be given focus after message. */
	this.focus = true;
	
	/**
	 * A user message was received from the channel.
	 */
	this.message = function(message) {
		var index = message.indexOf(':');
		var speaker = '';
		if (index != -1) {
			speaker = message.substring(0, index + 1);
			responseText = message.substring(index + 2, message.length);
		} else {
			responseText = message;
		}
		if (speaker != (this.nick + ':')) {
			if (this.playChime) {
				SDK.chime();
			}
			if (this.speak) {
				SDK.tts(SDK.stripTags(responseText), this.voice, this.nativeVoice, this.lang, this.nativeVoiceName);
			}
		}
		document.getElementById('response').innerHTML = SDK.linkURLs(message);
		var scroller = document.getElementById('scroller');
		var consolepane = document.getElementById('console');
		if (scroller == null || consolepane == null) {
			return;
		}
		var tr = document.createElement('tr');
		var td = document.createElement('td');
		var tr2 = document.createElement('tr');
		var td2 = document.createElement('td');
		var span = document.createElement('span');
		var span2 = document.createElement('span');
		var chatClass = 'chat-1';
		if (this.switchText) {
			chatClass = 'chat-2';
		}
		span.className = chatClass;
		span.innerHTML = speaker;
		span2.className = chatClass;
		span2.innerHTML = SDK.linkURLs(responseText);
		td.className = 'chat-user';
		td.setAttribute('nowrap', 'nowrap');
		td2.className = chatClass;
		td2.setAttribute('align', 'left');
		td2.setAttribute('width', '100%');
		consolepane.appendChild(tr);
		tr.appendChild(td);
		td.appendChild(span);
		tr.appendChild(td2);
		td2.appendChild(span2);
		this.switchText = !this.switchText;
		while (consolepane.childNodes.length > 500) {
			consolepane.removeChild(consolepane.firstChild);
		}
		scroller.scrollTop = scroller.scrollHeight;
		if (this.focus) {
			document.getElementById('chat').focus();
		}
	};

	
	/**
	 * An informational message was received from the channel.
	 * Such as a new user joined, private request, etc.
	 */	
	this.info = function(message) {
		if (this.nick == null) {
			this.nick = this.connection.nick;
		}
		this.message(message);
	};

	/**
	 * An error message was received from the channel.
	 * This could be an access error, or message failure.
	 */	
	this.error = function(message) {
		this.message(message);
	};
	
	/**
	 * Notification that the connection was closed.
	 */
	this.closed = function() {};
	
	/**
	 * The channels users changed (user joined, left, etc.)
	 * This contains a comma separated values (CSV) list of the current channel users.
	 * It can be passed to the SDKConnection.getUsers() API to obtain the UserConfig info for the users.
	 */
	this.updateUsers = function(usersCSV) {};

	/**
	 * The channels users changed (user joined, left, etc.)
	 * This contains a HTML list of the current channel users.
	 * It can be inserted into an HTML document to display the users.
	 */
	this.updateUsersXML = function(usersXML) {
		var onlineList = document.getElementById('online');
		if (onlineList == null) {
			return;
		}
		onlineList.innerHTML = '';
		var div = document.createElement('div');
		div.innerHTML = usersXML;
		var children = div.childNodes[0].childNodes;
		var count = 0;
		var length = children.length;
		var ids = {};
		// Add missing user
		for (var i = 0; i < length; i++) {
			var child = children[i - count];
			ids[child.id] = child.id;
			if (document.getElementById(child.id) == null) {
				onlineList.appendChild(child);
				count++;
			}
		}
		// Remove missing users
		var onlineDiv = document.getElementById('online-div');
		if (onlineDiv == null) {
			return;
		}
		children = onlineDiv.childNodes;
		count = 0;
		length = children.length;
		for (var i = 0; i < length; i++) {
			var child = children[i - count];
			if (child.id != 'online' && ids[child.id] == null) {
				onlineDiv.removeChild(child);
				count++;
			}
		}
	};

	/**
	 * Decrease the size of the video element for the userid.
	 */
	this.shrinkVideo = function(user) {
	    var id = 'user-' + encodeURIComponent(user);
	    var userdiv = document.getElementById(id);
	    if (userdiv != null) {
	    	var media = userdiv.firstElementChild;	    	
			if (media != null) {
				media.height = media.height / 1.5;
			}
		}
	};

	/**
	 * Increase the size of the video element for the userid.
	 */
	this.expandVideo = function(user) {
	    var id = 'user-' + encodeURIComponent(user);
	    var userdiv = document.getElementById(id);
	    if (userdiv != null) {
	    	var media = userdiv.firstElementChild;	    	
			if (media != null) {
				media.height = media.height * 1.5;
			}
		}
	};

	/**
	 * Mute the audio for the userid.
	 */
	this.muteAudio = function(user) {
	    var id = 'user-' + encodeURIComponent(user);
	    var userdiv = document.getElementById(id);
	    if (userdiv != null) {
	    	var media = userdiv.firstElementChild;	    	
			if (media != null) {
				if (media.muted) {
					if (user != this.nick) {
						media.muted = false;
					}
				} else {
					media.muted = true;					
				}
			}
		}
	};

	/**
	 * Mute the video for the userid.
	 */
	this.muteVideo = function(user) {
	    var id = 'user-' + encodeURIComponent(user);
	    var userdiv = document.getElementById(id);
	    if (userdiv != null) {
	    	var media = userdiv.firstElementChild;	    	
			if (media != null) {
				if (media.paused) {
					media.play();
					media.style.opacity = 100;
				} else {
					media.pause();
					media.style.opacity = 0;					
				}
			}
		}
	};

	this.toggleChime = function() {
		this.playChime = !this.playChime;
	}

	this.toggleSpeak = function() {
		this.speak = !this.speak;
	}

	this.toggleKeepAlive = function() {
		this.connection.toggleKeepAlive();
	}
	
	this.sendMessage = function() {
		var message = document.getElementById('chat').value;
		if (message != '') {
			this.connection.sendMessage(message);
			document.getElementById('chat').value = '';
		}
		return false;
	};

	this.sendImage = function() {
		if (!(window.File && window.FileReader && window.FileList && window.Blob)) {
			alert('The File APIs are not fully supported in this browser.');
			return false;
		}
		var form = document.createElement("form");
		form.enctype = "multipart/form-data";
		form.method = "post";
		form.name = "fileinfo";
		var fileInput = document.createElement("input");
		var self = this;
		fileInput.name = "file";
		fileInput.type = "file";
		form.appendChild(fileInput);
		fileInput.onchange = function() {
			var file = fileInput.files[0];
			self.connection.sendAttachment(file, true, form);
		}
		fileInput.click();
		return false;
	};

	this.sendAttachment = function() {
		if (!(window.File && window.FileReader && window.FileList && window.Blob)) {
			alert('The File APIs are not fully supported in this browser.');
			return false;
		}
		var form = document.createElement("form");
		form.enctype = "multipart/form-data";
		form.method = "post";
		form.name = "fileinfo";
		var fileInput = document.createElement("input");
		var self = this;
		fileInput.name = "file";
		fileInput.type = "file";
		form.appendChild(fileInput);
		fileInput.onchange = function() {
			var file = fileInput.files[0];
			self.connection.sendAttachment(file, false, form);
		}
		fileInput.click();
		return false;
	};

	this.ping = function() {
		this.connection.ping();
		document.getElementById('chat').value = '';
		return false;
	};

	this.accept = function() {
		this.connection.accept();
		document.getElementById('chat').value = '';
		return false;
	};

	this.exit = function() {
		this.connection.exit();
		document.getElementById('chat').value = '';
		return false;
	};

	this.boot = function() {
		document.getElementById('chat').value = 'boot: user';
		return false;
	};

	this.whisper = function(user) {
		if (user == null) {
			user = 'user';
		}
		document.getElementById('chat').value = 'whisper: ' + user + ': message';
		return false;
	};

	this.flag = function(user) {
		if (user != null) {
			document.getElementById('chat').value = 'flag: ' + user + ': reason';
			return false;
		}
		document.getElementById('chat').value = 'flag: user: reason';
		return false;
	};

	this.pvt = function(user) {
		if (user != null) {
			this.connection.pvt(user);
			return false;
		}
		document.getElementById('chat').value = 'private: user';
		return false;
	};

	this.clear = function() {
		document.getElementById('response').innerHTML = '';
		var console = document.getElementById('console');
		if (console != null) {
			console.innerHTML = '';
		}
		return false;
	};
}

/**
 * Shared method for updating an avatar image/video/audio from the chat response.
 */
SDK.updateAvatar = function(responseMessage, speak, urlprefix, elementPrefix, channelaudio, afterFunction, nativeVoice, lang, voice) {
	nativeVoice = nativeVoice && ('speechSynthesis' in window);
	if (elementPrefix == null) {
		elementPrefix = "";
	}
	var avatarStatus = document.getElementById(this.elementPrefix + "avatar-status");
	if (avatarStatus != null) {
		var status = "";
		if (responseMessage.emote != null && responseMessage.emote != "" && responseMessage.emote != "NONE") {
			status = responseMessage.emote.toLowerCase();
		}
		if (responseMessage.action != null && responseMessage.action != "") {
			if (status != "") {
				status = status + " : ";
			}
			status = status + responseMessage.action;
		}
		if (responseMessage.pose != null && responseMessage.pose != "") {
			if (status != "") {
				status = status + " : ";
			}
			status = status + responseMessage.pose;
		}
		avatarStatus.innerHTML = status;
	}
	if (responseMessage.avatarActionAudio != null) {
		var audio = new Audio(urlprefix + responseMessage.avatarActionAudio);
		audio.play();
	}
	if (SDK.backgroundAudio != null) {
		SDK.backgroundAudio.pause();
	}
	if (responseMessage.avatarAudio != null) {
		SDK.backgroundAudio = new Audio(urlprefix + responseMessage.avatarAudio);
		SDK.backgroundAudio.loop = true;
		SDK.backgroundAudio.play();
	}
	if (responseMessage.avatarType != null && responseMessage.avatarType.indexOf("video") != -1) {
		var div = document.getElementById(elementPrefix + "avatar-image-div");
		if (div != null) {
			div.style.display = "none";
		}
		div = document.getElementById(elementPrefix + "avatar-video-div");
		var canvas = null;
		if (div != null) {
			div.style.display = "inline-block";
			if (responseMessage.avatarBackground != null) {
				div.style.backgroundImage = "url(" + urlprefix + responseMessage.avatarBackground + ")";
			}
			var canvasDiv = document.getElementById(elementPrefix + "avatar-canvas-div");
			if (((SDK.isChrome() && !SDK.isMobile()) || SDK.isFirefox() || SDK.useCanvas == true) && SDK.useCanvas != false && canvasDiv != null) {
				div.style.position = "fixed";
				div.style.top = "-1000";
				div.style.left = "-1000";
				div.style.opacity = "0";
				div.style.zIndex = "-1";
				canvasDiv.style.display = "inline-block";
				canvas = document.getElementById(elementPrefix + "avatar-canvas");
			}
		}
		var video = document.getElementById(elementPrefix + "avatar-video");
		if (video == null) {
			if (speak) {
				if (nativeVoice) {
					SDK.tts(SDK.stripTags(responseMessage.message), null, true, lang, voice);
				} else {
					var audio = SDK.play(urlprefix + responseMessage.speech, channelaudio);
					audio.onended = afterFunction;
				}
			}
			return;
		} else {
			if (responseMessage.avatar.indexOf("mp4") != -1 && (SDK.isChrome() || SDK.isFirefox() || SDK.fixBrightness != true) && SDK.fixBrightness != false) {
				// Hack to fix grey background in Chrome.
				if (SDK.isChrome()) {
					video.style.webkitFilter = "brightness(108.5%)";
				} else {
					video.style["filter"] = "brightness(1.085)";
				}
				if (canvas != null) {
					if (SDK.isChrome()) {
						canvas.style.webkitFilter = "brightness(108.5%)";
					} else {
						video.style["filter"] = "brightness(1.085)";
					}
				}
			}
			if (canvas == null) {
				if (responseMessage.avatarBackground != null) {
					video.poster = urlprefix + responseMessage.avatarBackground;				
				}
			}
		}
		var context = null;
		var drawCanvas = null;
		if (canvas != null) {
		    context = canvas.getContext('2d');
			if (SDK.timers[elementPrefix + "avatar-canvas"] == null) {
				drawCanvas = function() {
				    if (!video.paused && !video.ended && video.currentTime > 0) {
				    	if (canvas.width != video.offsetWidth) {
				    		canvas.width = video.offsetWidth;
				    	}
				    	if (canvas.height != video.offsetHeight) {
				    		canvas.height = video.offsetHeight;
				    	}
				    	context.clearRect(0, 0, canvas.width, canvas.height);
				    	context.drawImage(video, 0, 0, video.offsetWidth, video.offsetHeight);
				    }
				}
				SDK.timers[elementPrefix + "avatar-canvas"] = drawCanvas;
				setInterval(drawCanvas, 20);				
			}
		}
		var end = function() {
			video.src = urlprefix + responseMessage.avatar;
			video.loop = true;
			video.play();
			if (afterFunction != null) {
				afterFunction();
			}
		}
		var talk = function() {
			if (responseMessage.avatarTalk != null) {
				if (speak) {
					if (responseMessage.speech == null && !nativeVoice) {
						end();
					} else {
						video.src = urlprefix + responseMessage.avatar;
						video.loop = true;
						var playing = false;
						video.play();

						if (nativeVoice) {
							var utterance = new SpeechSynthesisUtterance(SDK.stripTags(responseMessage.message));
							utterance.onstart = function() {
								if (playing) {
									return false;
								}
								speechSynthesis.pause();
								video.src = urlprefix + responseMessage.avatarTalk;
								video.loop = true;
								video.oncanplay = function() {
									if (playing) {
										return false;
									}
									playing = true;
									speechSynthesis.resume();
								}
								video.play();
							}
							utterance.onerror = function() {
								console.log("error");
								end();
							}
							utterance.onend = function() {
								end();
							}
							SDK.nativeTTS(utterance, lang, voice);
						} else {
							//var audio = new Audio(urlprefix + responseMessage.speech, channelaudio);
							var audio = SDK.play(urlprefix + responseMessage.speech, channelaudio);
							//audio.onabort = function() {console.log("abort");}
							audio.oncanplay = function() {
								if (playing) {
									return false;
								}
								audio.pause();
								video.src = urlprefix + responseMessage.avatarTalk;
								video.loop = true;
								video.oncanplay = function() {
									if (playing) {
										return false;
									}
									playing = true;
									audio.play();
								}
								video.play();
							}
							audio.onerror = function() {
								console.log("error");
								end();
							}
							//audio.onloadeddata = function() {console.log("loadeddata");}
							//audio.onloadedmetadata = function() {console.log("loadedmetadata");}
							//audio.onpause = function() {console.log("pause");}
							//audio.onplay = function() {console.log("play");}
							//audio.onplaying = function() {console.log("playing");}
							//audio.ontimeupdate = function() {console.log("timeupdate");}
							audio.onended = function() {
								end();
							}
							audio.play();
							video.play();
						}
					}
				} else {
					video.src = urlprefix + responseMessage.avatarTalk;
					video.loop = false;
					video.play();
					video.onended = function() {
						end();
					}
				}
			} else {
				video.src = urlprefix + responseMessage.avatar;
				video.loop = true;
				video.play();
				if (speak) {
					if (nativeVoice) {
						var utterance = new SpeechSynthesisUtterance(SDK.stripTags(responseMessage.message));
						utterance.onend = afterFunction;
						SDK.nativeTTS(utterance, lang, voice);
					} else {
						var audio = SDK.play(urlprefix + responseMessage.speech, channelaudio);
						audio.onended = afterFunction;						
					}
				} else if (afterFunction != null) {
					afterFunction();			
				}
			}
		}
		
		if (responseMessage.avatarAction != null) {
			video.src = urlprefix + responseMessage.avatarAction;
			video.loop = false;
			video.play();
			video.onended = function() {
				talk();
			}
		} else {
			talk();
		}
	} else {
		var div = document.getElementById(elementPrefix + "avatar-video-div");
		if (div != null) {
			div.style.display = "none";
		}
		div = document.getElementById(elementPrefix + "avatar-canvas-div");
		if (div != null) {
			div.style.display = "none";
		}
		div = document.getElementById(elementPrefix + "avatar-image-div");
		if (div != null) {
			div.style.display = "inline-block";
		}
		var img = document.getElementById(elementPrefix + 'avatar');
		if (img != null) {
			img.src = urlprefix + responseMessage.avatar;
		}
		img = document.getElementById(elementPrefix + 'avatar2');
		if (img != null) {
			img.src = urlprefix + responseMessage.avatar;
		}
		if (speak) {
			if (nativeVoice) {
				SDK.tts(SDK.stripTags(responseMessage.message), null, true, lang, voice);
			} else {
				var audio = SDK.play(urlprefix + responseMessage.speech, channelaudio);
				audio.onended = afterFunction;
			}
		} else if (afterFunction != null) {
			afterFunction();			
		}
	}
}

/**
 * The WebChatbotListener provides an integration between a chat bot conversation through a SDKConnection and an HTML document.
 * It updates the document to messages received from the connection, and sends messages from the document's form.
 * The HTML document requires the following elements:
 * <ul>
 * <li> chat - input (type='text') element for sending messages
 * <li> send - input (type='submit') button for sending chat input
 * <li> response - p element paragraph for last chat message
 * <li> console - table element for chat log, and avatar
 * <li> scroller - div element for chat log scroll pane
 * <li> avatar - img element for the bot's avatar (optional)
 * <li> avatar2 - img element hover img for the bot's avatar (optional)
 * <li> avatar-image-div - div element for the bot's image (optional)
 * <li> avatar-video - video element for the bot's video (optional)
 * <li> avatar-video-div - div element for the bot's video (optional)
 * <li> avatar-status - span element for the bot's current status (optional)
 * </ul>
 * Or you can call createBox() to have the WebChatbotListener create its own components in the current page.
 * @class
 */
function WebChatbotListener() {
	/** Set the caption for the button bar button. */
	this.caption = null;
	/** Disallow speech. */
	this.allowSpeech = true;
	/** Enable or disable speech. */
	this.speak = true;
	/** Configure if the browser's native voice TTS should be used. */
	this.nativeVoice = false;
	/** Set the voice name for the native voice. */
	this.nativeVoiceName = null;
	/** Set the language for the native voice. */
	this.lang = null;
	/** Enable or disable avatar. */
	this.avatar = true;
	/** A SDK connection must be set, be sure to include your app id. */
	this.connection = null;
	/** The id or name of the bot instance to connect to. */
	this.instance = null;
	/** The name to display for the bot. */
	this.instanceName = "Bot";
	/** The name to display for the user. */
	this.userName = "You";
	/** Allow the button color to be set. */
	this.color = "#009900";
	/** Allow the background color to be set. */
	this.background = null;
	/** Avatar image/video width. */
	this.width = 300;
	/** Avatar image/video height. */
	this.height = null;
	/** Only apply the background color if not Chrome. */
	this.backgroundIfNotChrome = false;
	/** onresponse event is raised after a response is received. */
	this.onresponse = null;
	/** Configure if chat should be given focus after response. */
	this.focus = true;
	/** Override the URL used in the chat bot box popup. */
	this.popupURL = null;
	/** Print response in chat bubble. */
	this.bubble = false;
	/** Initial message to display. */
	this.greeting = "loading...";
	/** Element id prefix. This allows an id prefix to avoid name collisions on the element names for the chat, response, console, and avatar elements.*/
	this.elementPrefix = "";
	/** Allows the bot's thumbnail image to be set for chat log. */
	this.botThumb;
	/** Allows the user's thumbnail image to be set for chat log. */
	this.userThumb;
	/** Set styles explictly to avoid inheriting page styles. Disable this to be able to override styles. */
	this.forceStyles = true;
	
	this.switchText = true;
	this.big = false;
	this.conversation = null;
	this.voiceInit = null;
		
	/**
	 * Create an embedding bar and div in the current webpage.
	 */
	this.createBox = function() {
		if (this.caption == null) {
			this.caption = this.instanceName;
		}
		var backgroundstyle = "";
		var buttonstyle = "";
		var hidden = "hidden";
		var border = "";
		if (this.backgroundIfNotChrome && SDK.isChrome()) {
			this.background = null;
		}
		if (this.background != null) {
			backgroundstyle = " style='background-color:" + this.background + "'";
			hidden = "visible";
			border = "border:1px;border-style:solid;border-color:black;";
		}
		if (this.color != null) {
			buttonstyle = "background-color:" + this.color + ";";
		}
		var minWidth = "";
		var divWidth = "";
		var background = "";
		var minHeight = "";
		var divHeight = "";
		if (this.width != null) {
			minWidth = "width:" + this.width + "px;";
			background = "background-size:" + this.width + "px auto;";
			divWidth = minWidth;
			divHeight = "min-height:" + this.width + "px;";
			responseWidth = "width:" + (this.width - 16) + "px;";
		}
		if (this.height != null) {
			minHeight = "height:" + this.height + "px;";
			divHeight = minHeight;
			if (this.width != null) {
				background = "background-size:" + this.width + "px " + this.height + "px;";
			} else {
				background = "background-size: auto " + this.height + "px;";
				divWidth = "min-width:" + this.height + "px;";
			}
		}
		var box = document.createElement('div');
		var html =
			"<style>\n"
				+ ".box { position:fixed;bottom:10px;right:10px;z-index:52;margin:2px;display:none;" + border + " }\n"
				+ ".box:hover { border:1px;border-style:solid;border-color:black; }\n"
				+ ".boxmenu { visibility:" + hidden + "; margin-bottom:12px; }\n"
				+ ".box:hover .boxmenu { visibility:visible; }\n"
				+ ".boxclose, .boxmin, .boxmax { font-size:22px;margin:2px;padding:0px;text-decoration:none; }\n"
				+ (this.forceStyles ? "#" : ".") + "boxbarmax { font-size:18px;margin:2px;padding:0px;text-decoration:none;color:white; }\n"
				+ ".boxclose:hover, .boxmin:hover, .boxmax:hover { color: #fff;background: grey; }\n"
				+ ".boxbar { position:fixed;bottom:2px;right:30px;z-index:52;margin:0;padding:6px;" + buttonstyle + " }\n"
				+ ".no-bubble { margin:4px; padding:8px; border:1px; border-style:solid; border-color:black; background-color:white; }\n"
				+ ".no-bubble-plain { margin:4px; padding:8px; border:1px; }\n"
				+ ".no-bubble-text { " + responseWidth + "; max-height:100px; overflow:auto; }\n"
				+ ".boxbutton { width:20px;height:20px;margin2px; }\n"
				+ ".bubble-div { padding-bottom:15px;position:relative; }\n"
				+ ".bubble { margin:4px; padding:8px; border:1px; border-style:solid; border-color:black; border-radius:10px; background-color:white; }\n"
				+ ".bubble-text { " + responseWidth + "; max-height:100px; overflow:auto; }\n"
				+ ".bubble:before { content:''; position:absolute; bottom:0px; left:40px; border-width:20px 0 0 20px; border-style:solid; border-color:black transparent; display:block; width:0;}\n"
				+ ".bubble:after { content:''; position:absolute; bottom:3px; left:42px; border-width:18px 0 0 16px; border-style:solid; border-color:white transparent; display:block; width:0;}\n"
				+ (this.forceStyles ? "#" : ".") + "chat { width:100%;height:22px; }\n"
				+ ".box-input-span { display:block; overflow:hidden; margin:4px; padding-right:4px; }\n"
				+ "#boxtable { background:none; border:none; margin:0; }\n"
			+ "</style>\n"
			+ "<div id='box' class='box' " + backgroundstyle + ">"
				+ "<div class='boxmenu'>"
					+ "<span style='float:right'><a id='boxmin' class='boxmin' href='#'>&#95;</a><a id='boxmax' class='boxmax' href='#'>&square;</a><a id='boxclose' class='boxclose' href='#'>&times;</a></span><br/>"
				+ "</div>";
		
		if (this.avatar) {
			html = html
				+ "<div id='" + this.elementPrefix + "avatar-image-div' style='display:none;" + minHeight + minWidth + "'>"
					+ "<img id='" + this.elementPrefix + "avatar' style='" + minHeight + minWidth + "'/>"
				+ "</div>"
				+ "<div id='" + this.elementPrefix + "avatar-video-div' style='display:none;" + divHeight + divWidth + background + "background-repeat: no-repeat;'>"
					+ "<video id='" + this.elementPrefix + "avatar-video' autoplay preload='auto' style='background:transparent;" + minHeight + minWidth + "'>"
						+ "Video format not supported by your browser (try Chrome)"
					+ "</video>"
				+ "</div>"
				+ "<div id='" + this.elementPrefix + "avatar-canvas-div' style='display:none;" + divHeight + divWidth + "'>"
					+ "<canvas id='" + this.elementPrefix + "avatar-canvas' style='background:transparent;" + minHeight + minWidth + "'>"
						+ "Canvas not supported by your browser (try Chrome)"
					+ "</canvas>"
				+ "</div>";
		}
		var urlprefix = this.connection.credentials.url + "/";
		html = html
				+ "<div>"
					+ "<div " + (this.bubble ? "class='bubble-div'" : "") + ">"
					+ "<div class='" + (this.bubble ? "bubble" : (this.background == null ? "no-bubble" : "no-bubble-plain") ) + "'><div class='" + (this.bubble ? "bubble-text" : "no-bubble-text" ) + "'>"
						+ "<span id='" + this.elementPrefix + "response'>" + this.greeting + "</span><br/>"
					+ "</div></div></div>"
					+ (this.allowSpeech ? "<table id='boxtable' class='boxtable' style='width:100%'><tr><td>" : "")
					+ "<span class='box-input-span'><input id='" + this.elementPrefix + "chat' type='text' class='box-input'/></span>"
					+ (this.allowSpeech ?
						("<td><a href='#' title='Speech'><img id='boxspeak' class='boxbutton' src='"
								+ urlprefix
								+ (this.speak ? "images/sound.png": "images/mute.png") +"'></a></td></tr></table>")
						: "")
				+ "</div>"
			+ "</div>"
			+ "<div id='boxbar' class='boxbar'>"
				+ "<span><a id='boxbarmax' class='boxbarmax' " + (this.forceStyles ? "style='color:white' " : "") + "href='#'>" + this.caption + "</a></span>"
			+ "</div>";
		
		box.innerHTML = html;
		document.body.appendChild(box);
		
		var self = this;
		document.getElementById(this.elementPrefix + "chat").addEventListener("keypress", function(event) {
			if (event.keyCode == 13) {
				self.sendMessage();
				return false;
			}
		});
		document.getElementById("boxclose").addEventListener("click", function() {
			self.closeBox();
			return false;
		});
		document.getElementById("boxmin").addEventListener("click", function() {
			self.minimizeBox();
			return false;
		});
		document.getElementById("boxmax").addEventListener("click", function() {
			self.popup();
			return false;
		});
		document.getElementById("boxbarmax").addEventListener("click", function() {
			self.maximizeBox();
			return false;
		});
		document.getElementById("boxspeak").addEventListener("click", function() {
			self.speak = !self.speak;
			var urlprefix = self.connection.credentials.url + "/";
			if (self.speak) {
				document.getElementById("boxspeak").src = urlprefix + "images/sound.png";
			} else {
				document.getElementById("boxspeak").src = urlprefix + "images/mute.png";				
			}
			return false;
		});
	}
	
	/**
	 * Create a live chat bar beside the bot bar.
	 */
	this.createLiveChatBox = function(channel, label, position) {
		var box = document.createElement('div');
		if (this.color != null) {
			buttonstyle = "background-color:" + this.color + ";";
		}
		if (label == null) {
			label = "Live Chat";
		}
		if (position == null) {
			position = (this.caption.length + label.length) * 8;
			position = "right:" + position + "px";
		}
		var html =
			"<style>\n"
				+ ".livechatboxbar { position:fixed;bottom:2px;" + position + ";z-index:52;margin:0;padding:6px;" + buttonstyle + " }\n"
				+ (this.forceStyles ? "#" : ".") + "livechatboxmax { color:white;font-size:18px;margin:2px;padding:0px;text-decoration:none; }\n"
			+ "</style>\n"
			+ "<div id='livechatboxbar' class='livechatboxbar'>"
			+ "<span><a id='livechatboxmax' class='livechatboxmax' href='#'>" + label + "</a></span>"
			+ "</div>";
		
		box.innerHTML = html;
		document.body.appendChild(box);
		
		document.getElementById("livechatboxmax").addEventListener("click", function() {
			SDK.popupwindow(SDK.url + '/livechat?id=' + channel + '&embedded&chat','child', 700, 520);
			return false;
		});
	}
	
	/**
	 * Minimize the embedding div in the current webpage.
	 */
	this.minimizeBox = function() {
		document.getElementById("box").style.display = 'none';
		document.getElementById("boxbar").style.display = 'inline';
		var livechatbot = document.getElementById("livechatboxbar");
		if (livechatbot != null) {
			livechatbot.style.display = 'inline';
		}
		this.exit();
		return false;		
	}
	
	/**
	 * Maximize the embedding div in the current webpage.
	 */
	this.maximizeBox = function() {
		document.getElementById("boxbar").style.display = 'none';
		document.getElementById("box").style.display = 'inline';
		var livechatbot = document.getElementById("livechatboxbar");
		if (livechatbot != null) {
			livechatbot.style.display = 'none';
		}
		this.greet();
		return false;		
	}
	
	/**
	 * Close the embedding div in the current webpage.
	 */
	this.closeBox = function() {
		document.getElementById("boxbar").style.display = 'none';
		document.getElementById("box").style.display = 'none';
		var livechatbot = document.getElementById("livechatboxbar");
		if (livechatbot != null) {
			livechatbot.style.display = 'none';
		}
		this.exit();
		return false;		
	}
	
	/**
	 * Create a popup window chat session with the bot.
	 */
	this.popup = function() {
		var box = document.getElementById("box");
		if (box != null) {
			box.style.display = 'none';
		}
		var speech = this.speak;
		if (!this.allowSpeech) {
			speech = "disable";
		}
		var height = 520;
		if (!this.avatar) {
			height = 220;
		}
		if (this.popupURL != null) {
			SDK.popupwindow(this.popupURL,'child', 700, height);
		} else {			
			var form = document.createElement("form");
            form.setAttribute("method", "post");
            form.setAttribute("action", SDK.url + "/chat");
            form.setAttribute("target", 'child');
 
            var input = document.createElement('input');
            input.type = 'hidden';
            input.name = "id";
            input.value = this.instance;
            form.appendChild(input);
 
            input = document.createElement('input');
            input.type = 'hidden';
            input.name = "embedded";
            input.value = "embedded";
            form.appendChild(input);
 
            input = document.createElement('input');
            input.type = 'hidden';
            input.name = "speak";
            input.value = speech;
            form.appendChild(input);
 
            input = document.createElement('input');
            input.type = 'hidden';
            input.name = "avatar";
            input.value = this.avatar;
            form.appendChild(input);
 
            input = document.createElement('input');
            input.type = 'hidden';
            input.name = "application";
            input.value = this.connection.credentials.applicationId;
            form.appendChild(input);
            
            document.body.appendChild(form);
            
			SDK.popupwindow('','child', 700, height);
			
			form.submit();
            document.body.removeChild(form);
		}
		this.minimizeBox();
		return false;
	}
	
	/**
	 * A chat message was received from the bot.
	 */
	this.response = function(user, message) {
		document.getElementById(this.elementPrefix + 'response').innerHTML = SDK.linkURLs(message);
		this.message(user, message);
		if (this.focus) {
			document.getElementById(this.elementPrefix + 'chat').focus();
		}
		if (this.onresponse != null) {
			this.onresponse(message);
		}
	}
	
	/**
	 * A chat message was received from the bot.
	 */
	this.message = function(user, message) {
		var speaker = user;
		var scroller = document.getElementById(this.elementPrefix + 'scroller');
		var chatconsole = document.getElementById(this.elementPrefix + 'console');
		if (scroller == null || chatconsole == null) {
			return;
		}
		var tr = document.createElement('tr');
		var td = document.createElement('td');
		var tr2 = document.createElement('tr');
		var td2 = document.createElement('td');
		var span2 = document.createElement('span');
		var chatClass = 'chat-1';
		if (this.switchText) {
			chatClass = 'chat-2';
		}
		if (this.botThumb == null || this.userThumb == null) {
			var span = document.createElement('span');
			span.className = chatClass;
			span.innerHTML = speaker;
			td.appendChild(span);
		} else {
			var img = document.createElement('img');
			img.className = 'chat-user';
			img.setAttribute('alt', speaker);
			if (user == this.userName) {
				img.setAttribute('src', this.userThumb);
			} else {
				img.setAttribute('src', this.botThumb);
			}
			td.appendChild(img);			
		}
		span2.className = chatClass;
		span2.innerHTML = SDK.linkURLs(message);
		td.className = 'chat-user';
		td.setAttribute('nowrap', 'nowrap');
		td2.className = chatClass;
		td2.setAttribute('align', 'left');
		td2.setAttribute('width', '100%');
		chatconsole.appendChild(tr);
		tr.appendChild(td);
		tr.appendChild(td2);
		td2.appendChild(span2);
		this.switchText = !this.switchText;
		while (chatconsole.childNodes.length > 500) {
			chatconsole.removeChild(chatconsole.firstChild);
		}
		scroller.scrollTop = scroller.scrollHeight;
	};

	/**
	 * Update the bot's avatar's image/video/audio from the chat response.
	 */
	this.updateAvatar = function(responseMessage) {
		var urlprefix = this.connection.credentials.url + "/";
		SDK.updateAvatar(responseMessage, this.speak, urlprefix, this.elementPrefix, null, null, this.nativeVoice, this.lang, this.nativeVoiceName);
	};

	this.toggleSpeak = function() {
		this.speak = !this.speak;
	}
	
	/**
	 * Initialize the bot listener.
	 */
	this.start = function() {
		var self = this;
		this.connection.error = function(message) {
			self.response("Error", message);
		}
	}
	
	/**
	 * Send the bot an empty message to let it greet the user.
	 * This will have the bot respond with any defined greeting it has.
	 */
	this.greet = function() {
		this.start();
		var chat = new ChatConfig();
		chat.instance = this.instance;
		chat.speak = this.speak;
		var self = this;
		this.connection.chat(chat, function(responseMessage) {
			self.conversation = responseMessage.conversation;
			self.updateAvatar(responseMessage);
			if (responseMessage.message != null) {
				self.response(self.instanceName, responseMessage.message);
			}
		});
		return false;
	};
	
	/**
	 * Send the current text from the chat input as a message to the bot, and process the response.
	 */
	this.sendMessage = function() {
		var message = document.getElementById(this.elementPrefix + 'chat').value;
		if (message != '') {
			this.message(this.userName, message);
			var chat = new ChatConfig();
			chat.message = message;
			chat.instance = this.instance;
			chat.speak = this.speak;
			chat.conversation = this.conversation;
			var correction = document.getElementById('correction');
			if (correction != null && correction.checked) {
				chat.correction = true;
				correction.checked = false;
			}
			var learning = document.getElementById('learning');
			if (learning != null) {
				chat.learn = learning.checked;
			}
			var debug = document.getElementById('debug');
			if (debug != null && debug.checked) {
				chat.debug = true;
				var debugLevel = document.getElementById('debugLevel');
				if (debugLevel != null) {
					chat.debugLevel = debugLevel.value;
				}
			}
			var offensive = document.getElementById('offensive');
			if (offensive != null && offensive.checked) {
				chat.offensive = true;
				offensive.checked = false;
			}
			var emote = document.getElementById('emote');
			if (emote != null && emote.value != null && emote.value != "" && emote.value != "NONE") {
				chat.emote = emote.value.toUpperCase();
				emote.value = "NONE";
			}
			var action = document.getElementById('action');
			if (action != null && action.value != null && action.value != "") {
				chat.action = action.value;
				action.value = "";
			}
			var self = this;
			document.getElementById(this.elementPrefix + 'response').innerHTML = '<i>thinking</i>';
			document.getElementById(this.elementPrefix + 'chat').value = '';
			this.connection.chat(chat, function(responseMessage) {
				self.conversation = responseMessage.conversation;
				self.response(self.instanceName, responseMessage.message);
				self.updateAvatar(responseMessage);
				var log = document.getElementById('log');
				var logText = responseMessage.log;
				if (log != null && logText != null) {
					log.innerHTML = logText;
				}
			});
		}
		return false;
	};

	/**
	 * Exit the conversation.
	 */
	this.exit = function() {
		if (this.conversation == null) {
			return false;
		}
		var chat = new ChatConfig();
		chat.disconnect = true;
		chat.instance = this.instance;
		chat.conversation = this.conversation;
		var self = this;
		this.connection.chat(chat, function(responseMessage) {
			self.clear();
			self.conversation = null;
		});
		return false;
	};

	/**
	 * Clear the chat console.
	 */
	this.clear = function() {
		document.getElementById(this.elementPrefix + 'response').innerHTML = '';
		var console = document.getElementById(this.elementPrefix + 'console');
		if (console != null) {
			console.innerHTML = '';
		}
		return false;
	};

	this.resizeAvatar = function () {
		var avatar = document.getElementById(this.elementPrefix + "avatar");
		var avatarDiv = document.getElementById(this.elementPrefix + "avatar-image-div");
		var avatarVideo = document.getElementById(this.elementPrefix + "avatar-video");
		var avatarVideoDiv = document.getElementById(this.elementPrefix + "avatar-video-div");
		var avatarCanvas = document.getElementById(this.elementPrefix + "avatar-canvas");
		var avatarCanvasDiv = document.getElementById(this.elementPrefix + "avatar-canvas-div");
		var scroller = document.getElementById(this.elementPrefix + "scroller");
		if (!this.big) {
			if (avatar != null) {
				avatar.className = "avatar-big";
			}
			if (avatarVideo != null) {
				avatarVideo.className = "avatar-video-big";
			}
			if (avatarVideoDiv != null) {
				avatarVideoDiv.className = "avatar-video-div-big";
			}
			if (avatarCanvas != null) {
				avatarCanvas.className = "avatar-canvas-big";
			}
			if (avatarCanvasDiv != null) {
				avatarCanvasDiv.className = "avatar-canvas-div-big";
			}
			if (scroller != null) {
				scroller.style.display = "none";
			}
			this.big = true;
		} else {
			if (avatar != null) {
				avatar.className = "avatar";
			}
			if (avatarVideo != null) {
				avatarVideo.className = "avatar-video";
			}
			if (avatarVideoDiv != null) {
				avatarVideoDiv.className = "avatar-video-div";
			}
			if (avatarCanvas != null) {
				avatarCanvas.className = "avatar-canvas";
			}
			if (avatarCanvasDiv != null) {
				avatarCanvasDiv.className = "avatar-canvas-div";
			}
			if (scroller != null) {
				scroller.style.display = "inline-block";
			}
			this.big = false;
		}
		return false;
	}
}

/**
 * The WebAvatar provides access to an avatar and binds it to elements in an HTML document.
 * It lets you use a bot avatar without having a bot.  You can tell the avatar what to say, and what actions and poses to display.
 * The HTML document requires the following elements:
 * <ul>
 * <li> avatar - img element for the avatar
 * <li> avatar-image-div - div element for the avatar's image
 * <li> avatar-video - video element for the avatar's video
 * <li> avatar-video-div - div element for the avatar's video
 * </ul>
 * Or you can call createBox() to have the WebAvatar create its own components in the current page.
 * @class
 */
function WebAvatar() {
	/** Enable or disable speech. */
	this.speak = true;
	/** Configure if the browser's native voice TTS should be used. */
	this.nativeVoice = false;
	/** Set the language for the native voice. */
	this.lang = null;
	/** Set the voice for the native voice. */
	this.nativeVoiceName = null;
	/** An SDK connection object must be set. */
	this.connection = null;
	/** The id or name of the avatar object to use. */
	this.avatar = null;
	/** The name of the voice to use. */
	this.voice = null;
	/** Allow the background color to be set. */
	this.background = null;
	/** Avatar image/video width. */
	this.width = 300;
	/** Avatar image/video height. */
	this.height = null;
	/** Only apply the background color if not Chrome. */
	this.backgroundIfNotChrome = false;
	/** An optional close event. */
	this.onclose = null;
	/** Return if the avatar box is in a closed state. */
	this.closed = true;
	/** Can be used to have multiple avatars in the same page. */
	this.elementPrefix = "WebAvatar-";
	/** Store list of messages to output. */
	this.messages = null;
	/** Function to invoke when processing all messages is complete. */
	this.ended = null;		
	
	/**
	 * Create an embedding bar and div in the current webpage.
	 */
	this.createBox = function() {
		var backgroundstyle = "";
		var hidden = "hidden";
		var border = "";
		if ((this.background != null) && (!this.backgroundIfNotChrome || !SDK.isChrome())) {
			backgroundstyle = " style='background-color:" + this.background + "'";
			hidden = "visible";
			border = "border:1px;border-style:solid;border-color:black;";
		}
		var box = document.createElement('div');
		var minWidth = "";
		var minHeight = "";
		var divWidth = "";
		var divHeight = "";
		var background = "";
		if (this.width != null) {
			minWidth = "width:" + this.width + "px;";
			background = "background-size:" + this.width + "px;";
			divWidth = minWidth;
			divHeight = "min-height:" + this.width + "px;";
		}
		if (this.height != null) {
			minHeight = "height:" + this.height + "px;";
			divHeight = minHeight;
			if (this.width != null) {
				background = "background-size:" + this.width + "px " + this.height + "px;";
			} else {
				background = "background-size: auto " + this.height + "px;";
				divWidth = "min-width:" + this.height + "px;";
			}
		}
		var html =
			"<style>\n"
				+ ".avatarbox { position:fixed;bottom:10px;left:10px;z-index:52;margin:2px;" + border + " }\n"
				+ ".avatarbox:hover { border:1px;border-style:solid;border-color:black; }\n"
				+ ".avatarbox .avatarboxmenu { visibility:" + hidden + "; }\n"
				+ ".avatarbox:hover .avatarboxmenu { visibility:visible; }\n"
				+ "#avatarboxclose { font-size:22px;margin:2px;padding:0px;text-decoration:none; }\n"
				+ "#avatarboxclose:hover { color: #fff;background: grey; }\n"
			+ "</style>\n"
			+ "<div id='avatarbox' class='avatarbox' " + backgroundstyle + ">"
				+ "<div class='avatarboxmenu'>"
					+ "<span style='float:right'><a id='avatarboxclose' href='#'>&times;</a></span><br/>"
				+ "</div>"
				+ "<div id='" + this.elementPrefix + "avatar-image-div' style='display:none;" + minWidth + minHeight + "'>"
					+ "<img id='" + this.elementPrefix + "avatar' style='" + minWidth + minHeight + "'/>"
				+ "</div>"
				+ "<div id='" + this.elementPrefix + "avatar-video-div' style='display:none;" + divWidth + divHeight + background + "background-repeat: no-repeat;'>"
					+ "<video id='" + this.elementPrefix + "avatar-video' autoplay preload='auto' style='background:transparent;" + minWidth + minHeight + "'>"
						+ "Video format not supported by your browser (try Chrome)"
					+ "</video>"
				+ "</div>"
				+ "<div id='" + this.elementPrefix + "avatar-canvas-div' style='display:none;" + divWidth + divHeight + "'>"
					+ "<canvas id='" + this.elementPrefix + "avatar-canvas' style='background:transparent;" + minWidth + minHeight + "'>"
						+ "Canvas not supported by your browser (try Chrome)"
					+ "</canvas>"
				+ "</div>"				
			+ "</div>";
		
		box.innerHTML = html;
		document.body.appendChild(box);
		
		var self = this;
		document.getElementById("avatarboxclose").addEventListener("click", function() {
			self.closeBox();
			return false;
		});
		this.closed = false;
	}
	
	/**
	 * Open the embedding div in the current webpage.
	 */
	this.openBox = function() {
		document.getElementById("avatarbox").style.display = 'inline';
		this.speak = true;
		this.closed = false;
		return false;		
	}
	
	/**
	 * Close the embedding div in the current webpage.
	 */
	this.closeBox = function() {
		document.getElementById("avatarbox").style.display = 'none';
		this.speak = false;
		if (this.onclose != null) {
			this.onclose();
		}
		this.closed = true;
		return false;		
	}

	/**
	 * Update the avatar's image/video/audio from the message response.
	 */
	this.updateAvatar = function(responseMessage, afterFunction) {
		var urlprefix = this.connection.credentials.url + "/";
		SDK.updateAvatar(responseMessage, this.speak, urlprefix, this.elementPrefix, false, afterFunction, this.nativeVoice, this.lang, this.nativeVoiceName);
	};
	
	/**
	 * Add the message to the avatars message queue.
	 * The messages will be spoken when processMessages() is called.
	 */
	this.addMessage = function(message, emote, action, pose) {
		var config = new AvatarMessage();
		config.message = message;
		config.avatar = this.avatar;
		if (this.nativeVoice && ('speechSynthesis' in window)) {
			config.speak = false;
		} else {
			config.speak = this.speak;
			config.voice = this.voice;
		}
		config.emote = emote;
		config.action = action;
		config.pose = pose;
		if (this.messages == null) {
			this.messages = [];
		}
		this.messages[this.messages.length] = config;
		return false;
	};
	
	/**
	 * Add the message to the avatars message queue.
	 * The messages will be spoken when runMessages() is called.
	 */
	this.processMessages = function(pause) {
		if (this.messages == null || this.messages.length == 0) {
			if (this.ended != null) {
				this.ended();
			}
			return false;
		}
		if (pause == null) {
			pause = 500;
		}
		var self = this;
		var message = this.messages[0];
		this.messages = this.messages.splice(1, this.messages.length);
		this.connection.avatarMessage(message, function(responseMessage) {
			self.updateAvatar(responseMessage, function() {
				setTimeout(function() {
					self.processMessages(pause);
				}, pause);
			});
		});
		return false;
	}
	
	/**
	 * Have the avatar speak the message with voice and animation.
	 * The function will be called at the end of the speech.
	 */
	this.message = function(message, emote, action, pose, afterFunction) {
		var config = new AvatarMessage();
		config.message = message;
		config.avatar = this.avatar;
		if (this.nativeVoice && ('speechSynthesis' in window)) {
			config.speak = false;
		} else {
			config.speak = this.speak;
			config.voice = this.voice;
		}
		config.emote = emote;
		config.action = action;
		config.pose = pose;
		var self = this;
		this.connection.avatarMessage(config, function(responseMessage) {
			self.updateAvatar(responseMessage, afterFunction);
		});
		return false;
	};
}

/**
 * Connection class for a Live Chat, or chatroom connection.
 * A live chat connection is different than an SDKConnection as it is asynchronous,
 * and uses web sockets for communication.
 * @class
 * @property channel
 * @property user
 * @property credentials
 * @property listener
 * @property keepAlive
 * @property onMediaStream
 * @property onMediaStreamEnded
 * @property nick
 * @property channelToken
 * @property onNewChannel
 * @property nick
 */
function LiveChatConnection() {
	this.channel = null;
	this.user = null;
	this.credentials = new Credentials();
	this.socket = null;
	this.listener = null;
	this.keepAlive = false;
	this.keepAliveInterval = null;
	this.mediaConnection = null;
	this.onMediaStream = null;
	this.onMediaStreamEnded = null;
	this.nick = null;
	this.channelToken = null;
	this.onNewChannel = null;
	this.onMessageCallbacks = {};
		
	/**
	 * Connect to the live chat server channel.
	 * Validate the user credentials.
	 * This call is asynchronous, any error or success with be sent as a separate message to the listener.
	 */
	this.connect = function(channel, user) {
		if (this.credentials == null) {
			throw "Mising credentials";
		}
		this.channel = channel;
		this.user = user;
		if (this.nick == null && this.user != null) {
			this.nick = this.user.user;
		}
		var host = null;
		if (SDK.scheme == "https") {
			host = "wss://" + this.credentials.host + this.credentials.app + "/live/chat";
		} else {
			host = "ws://" + this.credentials.host + this.credentials.app + "/live/chat";			
		}
		if ('WebSocket' in window) {
			this.socket = new WebSocket(host);
		} else if ('MozWebSocket' in window) {
			this.socket = new MozWebSocket(host);
		} else {
			throw 'Error: WebSocket is not supported by this browser.';
		}
		
		this.listener.connection = this;
		var self = this;
		
		this.socket.onopen = function () {
			if (self.channel != null) {
				var appId = self.credentials.applicationId;
				if (appId == null) {
					appId = '';
				}
				if (self.user == null) {
					self.socket.send("connect " + self.channel.id + " " + appId);
				} else if (user.token == null) {
					self.socket.send(
							"connect " + self.channel.id + " " + self.user.user + " " + self.user.password + " " + appId);						
				} else {
					self.socket.send(
							"connect " + self.channel.id + " " + self.user.user + " " + self.user.token + " " + appId);						
				}
			}
			self.setKeepAlive(this.keepAlive);
		};
		
		this.socket.onclose = function () {
			self.listener.message("Info: Closed");
			self.listener.closed();
			self.disconnectMedia();
		};
		
		this.socket.onmessage = function (message) {
	    	user = "";
	    	data = message.data;
	    	text = data;
	    	index = text.indexOf(':');
	    	if (index != -1) {
	    		user = text.substring(0, index);
	    		data = text.substring(index + 2, text.length);
	    	}
			if (user == "Media") {
			    data = JSON.parse(data);

			    if (data.sender == self.nick) {
			    	return;
			    }
			    if (data.channel != self.channelToken) {
			    	return;
			    }

			    if (self.onMessageCallbacks[data.channel]) {
			    	self.onMessageCallbacks[data.channel](data.message);
			    };
			    return;
			}
			if (user == "Online-xml") {
				self.listener.updateUsersXML(data);
				return;
			}
			if (user == "Online") {
				self.listener.updateUsers(data);
				return;
			}
			if (user == "Channel") {
				self.channelToken = data;
				if (self.onNewChannel != null) {
					self.onNewChannel(data);
				}
				return;
			}
			if (user == "Nick") {
				if (self.nick == null) {
					self.nick = data;
				}
				return;
			}
			
			if (self.keepAlive && user == "Info" && text.contains("pong")) {
				return;
			}
			if (user == "Info") {
				self.listener.info(text);
				return;
			}
			if (user == "Error") {
				self.listener.error(text);
				return;
			}
			self.listener.message(text);
		};
	};

	/**
	 * Connect to the active channels media feed (video, audio).
	 */
	this.connectMedia = function(mediaChannel, shareAudio, shareVideo) {
		if (this.mediaConnection != null) {
			this.mediaConnection.leave();
		}
		this.mediaConnection = new RTCMultiConnection(mediaChannel);
		var self = this;
		var open = false;
		this.mediaConnection.session = {
		    audio: shareAudio,
		    video: shareVideo
		};
		/*this.mediaConnection.mediaConstraints.audio = {
		    mandatory: {},
		    optional: [{
		        googEchoCancellation: true,
		        googAutoGainControl: true,
		        googNoiseSuppression: true,
		        googHighpassFilter: true,
		        googTypingNoiseDetection: true,
		        googAudioMirroring: true
		    }]
		};*/
		/*this.mediaConnection.privileges = {
		    canStopRemoteStream: true,
		    canMuteRemoteStream: true
		};*/

		this.mediaConnection.openSignalingChannel = function (config) {
		    var channel = config.channel || this.channel;
		    self.onMessageCallbacks[channel] = config.onmessage;

		    if (config.onopen) {
		    	setTimeout(config.onopen, 1000);
		    }

		    // directly returning socket object using "return" statement
		    return {
		        send: function (message) {
		            self.socket.send("Media: " + JSON.stringify({
		                sender: self.nick,
		                channel: channel,
		                message: message
		            }));
		        },
		        channel: channel
		    };
		};
		this.mediaConnection.onstream = function(stream) {
			open = true;
			if (self.onMediaStream != null) {
				self.onMediaStream(stream);
			}
		};
		this.mediaConnection.onstreamended = function(stream) {
			if (self.onMediaStreamEnded != null) {
				self.onMediaStreamEnded(stream);
			}
		};
		this.mediaConnection.onNewSession = function(session) {
		    session.join({
			    audio: shareAudio,
			    video: shareVideo
			});
		};
		if (this.nick != null) {
			this.mediaConnection.userid = this.nick;
		}
		//connection.log = false;
		this.mediaConnection.onerror = function(error) {
			SDK.error(error);
		}
		this.mediaConnection.onMediaError = function(error) {
			SDK.error(error);
		}
		this.mediaConnection.connect();
	    setTimeout(function() {
	    	if (!open) {
	    		self.mediaConnection.open("room");
	    	}
	    }, 5000);
	}
	
	/**
	 * Disconnect from the active channels media feed (video, audio).
	 */
	this.disconnectMedia = function() {
		if (this.mediaConnection != null) {
			this.mediaConnection.leave();
			this.mediaConnection = null;
		}
	}
	
	/**
	 * Reset the media feed (audio, video).
	 */
	this.resetMedia = function(shareAudio, shareVideo) {
		this.mediaConnection.session = {
		    audio: shareAudio,
		    video: shareVideo
		};
		for (var streamid in this.mediaConnection.localStreams) {
			var stream = this.mediaConnection.streams[streamid];
			if (!shareAudio || !shareVideo) {
				stream.mute({
				    audio: !shareAudio,
				    video: !shareVideo
				});
			}
			if (shareAudio || shareVideo) {
				stream.unmute({
				    audio: shareAudio,
				    video: shareVideo
				});
			}
		}
	}

	/**
	 * Decrease the size of the video element for the userid.
	 */
	this.shrinkVideo = function(user) {
		var streams = this.mediaConnection.streams.selectAll({remote:true, local:true});
		for (i = 0; i < streams.length; i++) {
			stream = streams[i];
			if (stream.userid == user) {
			    stream.mediaElement.height = stream.mediaElement.height / 1.5;
			}
		}
	};

	/**
	 * Increase the size of the video element for the userid.
	 */
	this.expandVideo = function(user) {
		var streams = this.mediaConnection.streams.selectAll({remote:true, local:true});
		for (i = 0; i < streams.length; i++) {
			stream = streams[i];
			if (stream.userid == user) {
			    stream.mediaElement.height = stream.mediaElement.height * 1.5;
			}
		}
	};

	/**
	 * Mute the audio for the userid.
	 */
	this.muteAudio = function(user) {
		var streams = this.mediaConnection.streams.selectAll({remote:true, local:true});
		for (i = 0; i < streams.length; i++) {
			stream = streams[i];
			if (stream.userid == user) {
			    stream.mute({
			        audio: true
			    });
			}
		}
	};

	/**
	 * Mute the video for the userid.
	 */
	this.muteVideo = function(user) {
		var streams = this.mediaConnection.streams.selectAll({remote:true, local:true});
		for (i = 0; i < streams.length; i++) {
			stream = streams[i];
			if (stream.userid == user) {
			    stream.mute({
			        video: true
			    });
			}
		}			
	};

	/**
	 * Sent a text message to the channel.
	 * This call is asynchronous, any error or success with be sent as a separate message to the listener.
	 * Note, the listener will receive its own messages.
	 */
	this.sendMessage = function(message) {
		this.checkSocket();
		this.socket.send(message);
	};

	this.sendAttachment = function(file, resize, form) {
		var self = this;
		var media = new MediaConfig();
		if (this.channel == null) {
			this.listener.error("Missing channel property");
			return false;
		}
		media.instance = this.channel.id;
		media.name = file.name;
		media.type = file.type;
		if (!resize && file.size > SDK.MAX_FILE_UPLOAD) {
			this.listener.error("File exceeds maximum upload size of " + (SDK.MAX_FILE_UPLOAD / 1000000) + "meg");
		} else {
			this.sdk.error = function(message) {
				self.listener.error(message);
			}
			this.sdk.createChannelAttachment(media, file, resize, form, function(media) {
				var message = "file: " + file.name + " : " + file.type + " : " + self.sdk.fetchLink(media.file);
				self.sendMessage(message);
			})
		}
		return false;
	};

	/**
	 * Accept a private request.
	 * This is also used by an operator to accept the top of the waiting queue.
	 * This can also be used by a user to chat with the channel bot.
	 * This call is asynchronous, any error or success with be sent as a separate message to the listener.
	 */
	this.accept = function() {
		this.checkSocket();
		this.socket.send("accept");
	};

	/**
	 * Test the connection.
	 * A pong message will be returned, this message will not be broadcast to the channel.
	 * This call is asynchronous, any error or success with be sent as a separate message to the listener.
	 */
	this.ping = function() {
		this.checkSocket();
		this.socket.send("ping");
	};

	/**
	 * Exit from the current private channel.
	 * This call is asynchronous, any error or success with be sent as a separate message to the listener.
	 */
	this.exit = function() {
		this.checkSocket();
		this.socket.send("exit");
	};

	/**
	 * Request a private chat session with a user.
	 * This call is asynchronous, any error or success with be sent as a separate message to the listener.
	 */
	this.pvt = function(user) {
		this.checkSocket();
		this.socket.send("pvt: " + user);
	};

	/**
	 * Boot a user from the channel.
	 * You must be a channel administrator to boot a user.
	 * This call is asynchronous, any error or success with be sent as a separate message to the listener.
	 */
	this.boot = function(user) {
		this.checkSocket();
		this.socket.send("boot: " + user);
	};

	/**
	 * Send a private message to a user.
	 * This call is asynchronous, any error or success with be sent as a separate message to the listener.
	 */
	this.whisper = function(user, message) {
		this.checkSocket();
		this.socket.send("whisper:" + user + ": " + message);
	};

	/**
	 * Disconnect from the channel.
	 */
	this.disconnect = function() {
    	this.setKeepAlive(false);
    	if (this.socket != null) {
    		this.socket.disconnect();
    	}
    	disconnectMedia();
	};
	
	this.checkSocket = function() {
		if (this.socket == null) {
			throw "Not connected";
		}
	};

	this.toggleKeepAlive = function() {
		this.setKeepAlive(!this.keepAlive);
	}

	this.setKeepAlive = function(keepAlive) {
		this.keepAlive = keepAlive;
		if (!keepAlive && this.keepAliveInterval != null) {
			clearInterval(this.keepAliveInterval);
		} else if (keepAlive && this.keepAliveInterval == null) {
			this.keepAliveInterval = setInterval(
					function() {
						this.ping()
					},
					600000);
		}
	}
}

/**
* Connection class for a REST service connection.
* The SDK connection gives you access to the Paphus Live Chat or libre server services using a REST API.
* <p>
* The services include:
* <ul>
* <li> User management (account creation, validation)
* <li> Bot access, chat, and administration
* <li> Forum access, posting, and administration
* <li> Live chat access, chat, and administration
* <li> Domain access, and administration
* </ul>
 * @class
 * @property user
 * @property domain
 * @property credentials
 * @property debug
 * @property error
*/
function SDKConnection() {
	this.user;
	this.domain;
	this.credentials = new Credentials();
	this.debug = SDK.debug;
	this.error = SDK.error;
	
	this.exception;
	
	/**
	 * Validate the user credentials (password, or token).
	 * The user details are returned (with a connection token, password removed).
	 * The user credentials are soted in the connection, and used on subsequent calls.
	 * An SDKException is thrown if the connect failed.
	 */
	this.connect = function(config, processor) {
		var self = this;
		this.fetchUser(config, function(user) {
			self.user = user;
			processor(user);
		});
	}
	
	/**
	 * Connect to the live chat channel and return a LiveChatConnection.
	 * A LiveChatConnection is separate from an SDKConnection and uses web sockets for
	 * asynchronous communication.
	 * The listener will be notified of all messages.
	 */
	this.openLiveChat = function(channel, listener) {
		var connection = new LiveChatConnection();
		connection.sdk = this;
		connection.credentials = this.credentials;
		connection.listener = listener;
		connection.connect(channel, this.user);
		return connection;
	}
	
	/**
	 * Connect to the domain.
	 * A domain is an isolated content space.
	 * Any browse or query request will be specific to the domain's content.
	 */	
	this.switchDomain = function(config, processor) {
		var self = this;
		this.fetch(config, function(domain) {
			self.domain = domain;
			processor(domain);
		});
	}
	
	/**
	 * Disconnect from the connection.
	 * An SDKConnection does not keep a live connection, but this resets its connected user and domain.
	 */	
	this.disconnect = function() {
		this.user = null;
		this.domain = null;
	}
	
	/**
	 * Fetch the user details for the user credentials.
	 * A token or password is required to validate the user.
	 */	
	this.fetchUser = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/check-user", config.toXML(), function(xml) {
			if (xml == null) {
				return;
			}
			var user = new UserConfig();
			user.parseXML(xml);
			processor(user);
		});
	}
	
	/**
	 * Fetch the URL for the image from the server.
	 */	
	this.fetchImage = function(image) {
		return this.credentials.url + "/" + image;
	}
	
	/**
	 * Fetch the URL for the image from the server.
	 */	
	this.fetchLink = function(image) {
		return this.credentials.url + "/" + image;
	}
	
	/**
	 * Fetch the forum post details for the forum post id.
	 */	
	this.fetchForumPost = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/check-forum-post", config.toXML(), function(xml) {
			if (xml == null) {
				return;
			}
			var post = new ForumPostConfig();
			post.parseXML(xml);
			processor(post);
		});
	}
	
	/**
	 * Create a new user.
	 */
	this.createUser = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/create-user", config.toXML(), function(xml) {
			if (xml == null) {
				return null;
			}
			var user = new UserConfig();
			user.parseXML(xml);
			this.user = user;
			processor(user);
		});
	}

	/**
	 * Create a new file/image/media attachment for a chat channel.
	 */
	this.createChannelAttachment = function(config, file, resize, form, processor) {
		config.addCredentials(this);
		if (resize) {
			this.POST_IMAGE(this.credentials.rest + "/create-channel-attachment", file, form, config.toXML(), function(xml) {
				if (xml == null) {
					return null;
				}
				var media = new MediaConfig();
				media.parseXML(xml);
				processor(media);
			});
		} else {
			this.POST_FILE(this.credentials.rest + "/create-channel-attachment", form, config.toXML(), function(xml) {
				if (xml == null) {
					return null;
				}
				var media = new MediaConfig();
				media.parseXML(xml);
				processor(media);
			});
		}
	}

	/**
	 * Create a new file/image/media attachment for a forum.
	 */
	this.createForumAttachment = function(config, file, resize, form, processor) {
		config.addCredentials(this);
		if (resize) {
			this.POST_IMAGE(this.credentials.rest + "/create-forum-attachment", file, form, config.toXML(), function(xml) {
				if (xml == null) {
					return null;
				}
				var media = new MediaConfig();
				media.parseXML(xml);
				processor(media);
			});
		} else {
			this.POST_FILE(this.credentials.rest + "/create-forum-attachment", form, config.toXML(), function(xml) {
				if (xml == null) {
					return null;
				}
				var media = new MediaConfig();
				media.parseXML(xml);
				processor(media);
			});
		}
	}

	/**
	 * Create a new file/image/media attachment for a forum and insert the http link into the textarea.
	 */
	this.uploadForumAttachment = function(forum, resize, processor) {
		if (!(window.File && window.FileReader && window.FileList && window.Blob)) {
			this.error('The File APIs are not fully supported in this browser.');
			return false;
		}
		var form = document.createElement("form");
		form.enctype = "multipart/form-data";
		form.method = "post";
		form.name = "fileinfo";
		var fileInput = document.createElement("input");
		var self = this;
		fileInput.name = "file";
		fileInput.type = "file";
		form.appendChild(fileInput);
		fileInput.onchange = function() {
			var file = fileInput.files[0];
			self.uploadForumFile(file, forum, resize, form, processor);
		}
		fileInput.click();
	};

	/**
	 * Create a new file/image/media attachment for a forum.
	 */
	this.uploadForumFile = function(file, forum, resize, form, processor) {
		var self = this;
		var media = new MediaConfig();
		media.instance = forum;
		media.name = file.name;
		media.type = file.type;
		if (!resize && file.size > SDK.MAX_FILE_UPLOAD) {
			this.error("File exceeds maximum upload size of " + (SDK.MAX_FILE_UPLOAD / 1000000) + "meg");
		} else {
			this.createForumAttachment(media, file, resize, form, function(media) {
				var link = self.fetchLink(media.file);
				if (processor != null) {
					processor(link, file.name);
				}
			})
		}
	};
	
	/**
	 * Create a new forum post.
	 * You must set the forum id for the post.
	 */
	this.createForumPost = function(config, processor) {
		config.addCredentials(this);
		var xml = POST(this.credentials.rest + "/create-forum-post", config.toXML(), function(xml) {
			if (xml == null) {
				return null;
			}
			var post = new ForumPostConfig();
			post.parseXML(xml);
			processor(post);
		});
	}
	
	/**
	 * Create a reply to a forum post.
	 * You must set the parent id for the post replying to.
	 */
	this.createReply = function(config, processor) {
		config.addCredentials(this);
		var xml = POST(this.credentials.rest + "/create-reply", config.toXML(), function(xml) {
			if (xml == null) {
				return null;
			}
			var reply = new ForumPostConfig();
			reply.parseXML(xml);
			processor(reply);			
		});
	}
	
	/**
	 * Fetch the content details from the server.
	 * The id or name and domain of the object must be set.
	 */
	this.fetch = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/check-" + config.type, config.toXML(), function(xml) {
			if (xml == null) {
				return null;
			}
			var config2 = new config.constructor();
			config2.parseXML(xml);
			processor(config2)
		});
	}
	
	/**
	 * Update the forum post.
	 */
	this.updateForumPost = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/update-forum-post", config.toXML(), function(xml) {
			if (xml == null) {
				return null;
			}
			var config = new ForumPostConfig();
			config.parseXML(xml);
			processor(config);			
		});
	}
	
	/**
	 * Update the user details.
	 * The password must be passed to allow the update.
	 */
	this.updateUser = function(config) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/update-user", config.toXML(), function(xml) {
			return;
		});
	}
	
	/**
	 * Permanently delete the forum post with the id.
	 */
	this.deleteForumPost = function(config) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/delete-forum-post", config.toXML(), function(xml) {
			return;
		});
	}
	
	/**
	 * Flag the content as offensive, a reason is required.
	 */
	this.flag = function(config) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/flag-" + config.getType(), config.toXML(), function(xml) {
			return;
		});
	}
	
	/**
	 * Flag the forum post as offensive, a reason is required.
	 */
	this.flagForumPost = function(config) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/flag-forum-post", config.toXML(), function(xml) {
			return;
		});
	}
	
	/**
	 * Flag the user post as offensive, a reason is required.
	 */
	this.flagUser = function(config) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/flag-user", config.toXML(), function(xml) {
			return;
		});
	}
	
	/**
	 * Process the bot chat message and return the bot's response.
	 * The ChatConfig should contain the conversation id if part of a conversation.
	 * If a new conversation the conversation id is returned in the response. 
	 */
	this.chat = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/post-chat", config.toXML(), function(xml) {
			if (xml == null) {
				return null;
			}
			var responseMessage = new ChatResponse();
			responseMessage.parseXML(xml);
			processor(responseMessage);			
		});
	}
	
	/**
	 * Process the avatar message and return the avatar's response.
	 */
	this.avatarMessage = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/avatar-message", config.toXML(), function(xml) {
			if (xml == null) {
				return null;
			}
			var responseMessage = new ChatResponse();
			responseMessage.parseXML(xml);
			processor(responseMessage);			
		});
	}
	
	/**
	 * Return the list of user details for the comma separated values list of user ids.
	 */
	this.fetchAllUsers = function(usersCSV, processor) {
		var config = new UserConfig();
		config.user = usersCSV;
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/get-users", config.toXML(), function(xml) {
			var users = [];
			if (xml != null) {
				for (var index = 0; index < xml.childNodes.length; index++) {
					var child = xml.childNodes[index];
					var userConfig = new UserConfig();
					userConfig.parseXML(child);
					users[user.length] = userConfig;
				}
			}
			processor(users);
		});
	}
	
	/**
	 * Return the list of forum posts for the forum browse criteria.
	 */
	this.fetchPosts = function(config, processor) {
		config.addCredentials(this);
		var xml = this.POST(this.credentials.rest + "/get-forum-posts", config.toXML(), function(xml) {
			var instances = [];
			if (xml != null) {
				for (var index = 0; index < xml.childNodes.length; index++) {
					var child = xml.childNodes[index];
					var post = new ForumPostConfig();
					post.parseXML(child);
					instances[instances.length] = post;
				}
			}
			processor(instances);
		});
	}
	
	/**
	 * Return the list of categories for the type, and domain.
	 */
	this.fetchCategories = function(config, processor) {
		config.addCredentials(this);
		var xml = this.POST(this.credentials.rest + "/get-categories", config.toXML(), function(xml) {
			var categories = [];
			categories[0] = "";
			if (xml != null) {
				for (var index = 0; index < xml.childNodes.length; index++) {
					categories[categories.length] = (xml.childNodes[index].getAttribute("name"));
				}
			}
			processor(categories);
		});
	}
	
	/**
	 * Return the list of tags for the type, and domain.
	 */
	this.fetchTags = function(config, processor) {
		config.addCredentials(this);
		var xml = this.POST(this.credentials.rest + "/get-tags", config.toXML(), function(xml) {
			var tags = [];
			tags[0] = "";
			if (xml != null) {
				for (var index = 0; index < xml.childNodes.length; index++) {
					tags[tags.length] = (xml.childNodes[index].getAttribute("name"));
				}
			}
			processor(tags);			
		});
	}
	
	/**
	 * Return the users for the content.
	 */
	this.fetchUsers = function(config, processor) {
		config.addCredentials(this);
		var xml = this.POST(this.credentials.rest + "/get-" + config.getType() + "-users", config.toXML(), function(xml) {
			var users = [];
			if (xml != null) {
				for (var index = 0; index < xml.childNodes.length; index++) {
					var user = new UserConfig();
					user.parseXML(xml.childNodes[index]);
					users[users.length] = (user.user);
				}
			}
			processor(users);
			
		});
	}
	
	/**
	 * Return the bot's voice configuration.
	 */
	this.trainInstance = function(config) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/train-instance", config.toXML(), function(xml) {
			return;
		});
	}
	
	/**
	 * Return the bot's voice configuration.
	 */
	this.fetchVoice = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/get-voice", config.toXML(), function(xml) {
			if (xml == null) {
				return;
			}
			var voice = new VoiceConfig();
			voice.parseXML(xml);
			processor(voice);
		});
	}
	
	/**
	 * Return the list of content for the browse criteria.
	 * The type defines the content type (one of Bot, Forum, Channel, Domain).
	 */
	this.browse = function(config, processor) {
		config.addCredentials(this);
		var type = "";
		if (config.type == "Bot") {
			type = "/get-instances";
		} else if (config.type == "Forum") {
			type = "/get-forums";
		} else if (config.type == "Channel") {
			type = "/get-channels";
		} else if (config.type == "Domain") {
			type = "/get-domains";
		}
		this.POST(this.credentials.rest + type, config.toXML(), function(xml) {
			var instances = [];
			if (xml != null) {
				for (var index = 0; index < xml.childNodes.length; index++) {
					var instance = null;
					if (config.type == "Bot") {
						instance = new InstanceConfig();
					} else if (config.type == "Forum") {
						instance = new ForumConfig();
					} else if (config.type == "Channel") {
						instance = new ChannelConfig();
					} else if (config.type == "Domain") {
						instance = new DomainConfig();
					}
					instance.parseXML(xml.childNodes[index]);
					instances[instances.length] = (instance);
				}
			}
			processor(instances);
		});
	}

	this.GET = function(url, processor) {	
		if (this.debug) {
			console.log("GET: " + url);
		}
		var xml = null;
		var request = new XMLHttpRequest();
		var debug = this.debug;
		var self = this;
		request.onreadystatechange = function() {
			if (request.readyState != 4) return;
			if (request.status != 200) {
				console.log('Error: SDK GET web request failed');
				if (debug) {
					console.log(request.statusText);
					console.log(request.responseText);
					console.log(request.responseXML);
				}
				self.error(request.responseText);
				return;
			}
			processor(request.responseXML.childNodes[0]);
		}
		
		request.open('GET', url, true);
		request.send();
	}

	this.POST = function(url, xml, processor) {
		if (this.debug) {
			console.log("POST: " + url);
			console.log("XML: " + xml);
		}
		var request = new XMLHttpRequest();
		var debug = this.debug;
		var self = this;
		request.onreadystatechange = function() {
			if (debug) {
				console.log(request.readyState);
				console.log(request.status);
				console.log(request.statusText);
				console.log(request.responseText);
				console.log(request.responseXML);
			}
			if (request.readyState != 4) return;
			if (request.status != 200 && request.status != 204) {
				console.log('Error: SDK POST web request failed');
				if (debug) {
					console.log(request.statusText);
					console.log(request.responseText);
					console.log(request.responseXML);
				}
				self.error(request.responseText);
				return;
			}
			processor(request.responseXML.childNodes[0]);
		};
		
		request.open('POST', url, true);
		request.setRequestHeader("Content-Type", "application/xml");
		request.send(xml);
	}
	
	this.POST_FILE = function(url, form, xml, processor) {
		if (this.debug) {
			console.log("POST FILE: " + url);
			console.log("FORM: " + form);
			console.log("XML: " + xml);
		}
		var request = new XMLHttpRequest();
		var formData = new FormData(form);
		formData.append("xml", xml);
		var debug = this.debug;
		var self = this;
		request.onreadystatechange = function() {
			if (debug) {
				console.log(request.readyState);
				console.log(request.status);
				console.log(request.statusText);
				console.log(request.responseText);
				console.log(request.responseXML);
			}
			if (request.readyState != 4) return;
			if (request.status != 200 && request.status != 204) {
				console.log('Error: SDK POST web request failed');
				if (debug) {
					console.log(request.statusText);
					console.log(request.responseText);
					console.log(request.responseXML);
				}
				self.error(request.responseText);
				return;
			}
			processor(request.responseXML.childNodes[0]);
		};
		
		request.open('POST', url, true);
		//request.setRequestHeader("Content-Type", "multipart/form-data");
		request.send(formData);
	}
	
	this.POST_IMAGE = function(url, file, form, xml, processor) {
		var self = this;
		var debug = this.debug;
		var reader = new FileReader();
		reader.onloadend = function() {
			var tempImg = new Image();
			tempImg.src = reader.result;
			tempImg.onload = function() {
				var MAX_WIDTH = 300;
				var MAX_HEIGHT = 300;
				var tempW = tempImg.width;
				var tempH = tempImg.height;
				if (tempW > tempH) {
					if (tempW > MAX_WIDTH) {
						 tempH *= MAX_WIDTH / tempW;
						 tempW = MAX_WIDTH;
					}
				} else {
					if (tempH > MAX_HEIGHT) {
						 tempW *= MAX_HEIGHT / tempH;
						 tempH = MAX_HEIGHT;
					}
				}
				var canvas = document.createElement('canvas');
				canvas.width = tempW;
				canvas.height = tempH;
				var ctx = canvas.getContext("2d");
				ctx.fillStyle = '#fff';
				ctx.fillRect(0, 0, canvas.width, canvas.height);				
				ctx.drawImage(this, 0, 0, tempW, tempH);
	            var dataUrl = canvas.toDataURL('image/jpeg');
	            var blob = SDK.dataURLToBlob(dataUrl);

				var request = new XMLHttpRequest();
				var formData = new FormData();
				formData.append("xml", xml);
				formData.append('file', blob, file.name);
				request.onreadystatechange = function() {
					if (debug) {
						console.log(request.readyState);
						console.log(request.status);
						console.log(request.statusText);
						console.log(request.responseText);
						console.log(request.responseXML);
					}
					if (request.readyState != 4) return;
					if (request.status != 200 && request.status != 204) {
						console.log('Error: SDK POST web request failed');
						if (debug) {
							console.log(request.statusText);
							console.log(request.responseText);
							console.log(request.responseXML);
						}
						self.error(request.responseText);
						return;
					}
					processor(request.responseXML.childNodes[0]);
				};
				
				request.open('POST', url, true);
				//request.setRequestHeader("Content-Type", "multipart/form-data");
				request.send(formData);
			}
		 }
		 reader.readAsDataURL(file);
	}
}

/**
 * DTO for XML config.
 * @class
 * @property application
 * @property domain
 * @property user
 * @property token
 * @property instance
 * @property type
 */
function Config() {
	this.application;
	this.domain;
	this.user;
	this.token;
	this.instance;
	this.type;
	
	this.addCredentials = function(connection) {
		this.application = connection.credentials.applicationId;
		if (connection.user != null) {
			this.user = connection.user.user;
			this.token = connection.user.token;
		}
		if (connection.domain != null) {
			this.domain = connection.domain.id;
		}
	}
	
	this.writeCredentials = function(xml) {
		if (this.user != null && this.user.length > 0) {
			xml = xml + (" user=\"" + this.user + "\"");
		}
		if (this.token != null && this.token.length > 0) {
			xml = xml + (" token=\"" + this.token + "\"");
		}
		if (this.type != null && this.type.length > 0) {
			xml = xml + (" type=\"" + this.type + "\"");
		}
		if (this.instance != null && this.instance.length > 0) {
			xml = xml + (" instance=\"" + this.instance + "\"");
		}
		if (this.application != null && this.application.length > 0) {
			xml = xml + (" application=\"" + this.application + "\"");
		}
		if (this.domain != null && this.domain.length > 0) {
			xml = xml + (" domain=\"" + this.domain + "\"");
		}
		return xml;
	}
}

/**
 * DTO for XML user creation config.
 * @class
 * @property password
 * @property newPassword
 * @property hint
 * @property name
 * @property showName
 * @property email
 * @property website
 * @property bio
 * @property over18
 * @property avatar
 * @property connects
 * @property bots
 * @property posts
 * @property messages
 * @property joined
 * @property lastConnect
 */
function UserConfig() {
	this.password;
	this.newPassword;
	this.hint;
	this.name;
	this.showName;
	this.email;
	this.website;
	this.bio;
	this.over18;
	this.avatar;
	
	this.connects;
	this.bots;
	this.posts;
	this.messages;
	this.joined;
	this.lastConnect;
	
	this.addCredentials = function(connection) {
		this.application = connection.credentials.applicationId;
		if (connection.domain != null) {
			this.domain = connection.domain.id;
		}
	}

	this.parseXML = function(element) {
		this.user = element.getAttribute("user");
		this.name = element.getAttribute("name");
		this.showName = element.getAttribute("showName");
		this.token = element.getAttribute("token");
		this.email = element.getAttribute("email");
		this.hint = element.getAttribute("hint");
		this.website = element.getAttribute("website");
		this.connects = element.getAttribute("connects");
		this.bots = element.getAttribute("bots");
		this.posts = element.getAttribute("posts");
		this.messages = element.getAttribute("messages");
		this.joined = element.getAttribute("joined");
		this.lastConnect = element.getAttribute("lastConnect");
		
		var node = element.getElementsByTagName("bio")[0];
		if (node != null) {
			this.bio = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("avatar")[0];
		if (node != null) {
			this.avatar = SDK.innerHTML(node);
		}
	}
	
	this.toXML = function() {
		var xml = "<user";
		xml = this.writeCredentials(xml);
		if (this.password != null) {
			xml = xml + (" password=\"" + this.password + "\"");
		}
		if (this.newPassword != null) {
			xml = xml + (" newPassword=\"" + this.newPassword + "\"");
		}
		if (this.hint != null) {
			xml = xml + (" hint=\"" + this.hint + "\"");
		}
		if (this.name != null) {
			xml = xml + (" name=\"" + this.name + "\"");
		}
		if (this.showName) {
			xml = xml + (" showName=\"" + this.showName + "\"");
		}
		if (this.email != null) {
			xml = xml + (" email=\"" + this.email + "\"");
		}
		if (this.website != null) {
			xml = xml + (" website=\"" + this.website + "\"");
		}
		if (this.over18) {
			xml = xml + (" over18=\"" + this.over18 + "\"");
		}
		xml = xml + (">");
		
		if (this.bio != null) {
			xml = xml + ("<bio>");
			xml = xml + (SDK.escapeHTML(this.bio));
			xml = xml + ("</bio>");
		}
		xml = xml + ("</user>");
		return xml;
	}
		
}
UserConfig.prototype = new Config();
UserConfig.prototype.constructor = UserConfig;
UserConfig.constructor = UserConfig;

/**
 * DTO for XML chat message config.
 * @class
 * @property conversation
 * @property speak
 * @property correction
 * @property offensive
 * @property disconnect
 * @property emote
 * @property action
 * @property message
 */
function ChatConfig() {	
	this.conversation;
	this.speak;
	this.correction;
	this.offensive;
	this.disconnect;
	this.emote;
	this.action;
	this.message;
	this.debug;
	this.debugLevel;
	this.learn;
	
	this.toXML = function() {
		var xml = "<chat";
		xml = this.writeCredentials(xml);
		if (this.conversation != null) {
			xml = xml + (" conversation=\"" + this.conversation + "\"");
		}
		if (this.emote != null) {
			xml = xml + (" emote=\"" + this.emote + "\"");
		}
		if (this.action != null) {
			xml = xml + (" action=\"" + this.action + "\"");
		}
		if (this.speak) {
			xml = xml + (" speak=\"" + this.speak + "\"");
		}
		if (this.correction) {
			xml = xml + (" correction=\"" + this.correction + "\"");
		}
		if (this.offensive) {
			xml = xml + (" offensive=\"" + this.offensive + "\"");
		}
		if (this.learn != null) {
			xml = xml + (" learn=\"" + this.learn + "\"");
		}
		if (this.debug) {
			xml = xml + (" debug=\"" + this.debug + "\"");
		}
		if (this.debugLevel != null) {
			xml = xml + (" debugLevel=\"" + this.debugLevel + "\"");
		}
		if (this.disconnect) {
			xml = xml + (" disconnect=\"" + this.disconnect + "\"");
		}
		xml = xml + (">");
		
		if (this.message != null) {
			xml = xml + ("<message>");
			xml = xml + (SDK.escapeHTML(this.message));
			xml = xml + ("</message>");
		}
		xml = xml + ("</chat>");
		return xml;
	}
}
ChatConfig.prototype = new Config();
ChatConfig.prototype.constructor = ChatConfig;
ChatConfig.constructor = ChatConfig;

/**
 * DTO for XML chat response config.
 * @class
 * @property conversation
 * @property avatar
 * @property avatarType
 * @property avatarTalk
 * @property avatarTalkType
 * @property avatarAction
 * @property avatarActionType
 * @property avatarActionAudio
 * @property avatarActionAudioType
 * @property avatarAudio
 * @property avatarAudioType
 * @property avatarBackground
 * @property speech
 * @property message
 * @property emote
 * @property action
 * @property pose
 */
function ChatResponse() {	
	this.conversation;
	this.avatar;
	this.avatarType;
	this.avatarTalk;
	this.avatarTalkType;
	this.avatarAction;
	this.avatarActionType;
	this.avatarActionAudio;
	this.avatarActionAudioType;
	this.avatarAudio;
	this.avatarAudioType;
	this.avatarBackground;
	this.speech;
	this.message;
	this.emote;
	this.action;
	this.pose;
	this.log;

	this.parseXML = function(element) {
		this.conversation = element.getAttribute("conversation");
		this.avatar = element.getAttribute("avatar");
		this.avatarType = element.getAttribute("avatarType");
		this.avatarTalk = element.getAttribute("avatarTalk");
		this.avatarTalkType = element.getAttribute("avatarTalkType");
		this.avatarAction = element.getAttribute("avatarAction");
		this.avatarActionType = element.getAttribute("avatarActionType");
		this.avatarActionAudio = element.getAttribute("avatarActionAudio");
		this.avatarActionAudioType = element.getAttribute("avatarActionAudioType");
		this.avatarAudio = element.getAttribute("avatarAudio");
		this.avatarAudioType = element.getAttribute("avatarAudioType");
		this.avatarBackground = element.getAttribute("avatarBackground");
		this.emote = element.getAttribute("emote");
		this.action = element.getAttribute("action");
		this.pose = element.getAttribute("pose");
		this.speech = element.getAttribute("speech");

		var node = element.getElementsByTagName("message")[0];
		if (node != null) {
			this.message = SDK.innerHTML(node);
			if (this.message == null) {
				var div = document.createElement("div");
				div.appendChild(node);
				this.message = div.innerHTML;
			}
			var index = this.message.indexOf("&lt;");
			var index2 = this.message.indexOf("&gt;")
			if (index != -1 && index2 > index) {
				this.message = this.message.replace(/&lt;/g, "<");
				this.message = this.message.replace(/&gt;/g, ">");
			}
		}
		
		node = element.getElementsByTagName("log")[0];
		if (node != null) {
			this.log = SDK.innerHTML(node);
			if (this.log == null) {
				var div = document.createElement("div");
				div.appendChild(node);
				this.log = div.innerHTML;
			}
		}
	}
}
ChatResponse.prototype = new Config();
ChatResponse.prototype.constructor = ChatResponse;
ChatResponse.constructor = ChatResponse;

/**
 * DTO for XML avatar message config.
 * @class
 * @property avatar
 * @property speak
 * @property voice
 * @property message
 * @property emote
 * @property action
 * @property pose
 */
function AvatarMessage() {	
	this.avatar;
	this.speak;
	this.voice;
	this.message;
	this.emote;
	this.action;
	this.pose;
	
	this.toXML = function() {
		var xml = "<avatar-message";
		xml = this.writeCredentials(xml);
		if (this.avatar != null) {
			xml = xml + (" avatar=\"" + this.avatar + "\"");
		}
		if (this.emote != null) {
			xml = xml + (" emote=\"" + this.emote + "\"");
		}
		if (this.action != null) {
			xml = xml + (" action=\"" + this.action + "\"");
		}
		if (this.pose != null) {
			xml = xml + (" pose=\"" + this.pose + "\"");
		}
		if (this.voice != null) {
			xml = xml + (" voice=\"" + this.voice + "\"");
		}
		if (this.speak) {
			xml = xml + (" speak=\"" + this.speak + "\"");
		}
		xml = xml + (">");
		
		if (this.message != null) {
			xml = xml + ("<message>");
			xml = xml + (SDK.escapeHTML(this.message));
			xml = xml + ("</message>");
		}
		xml = xml + ("</avatar-message>");
		return xml;
	}
}
AvatarMessage.prototype = new Config();
AvatarMessage.prototype.constructor = AvatarMessage;
AvatarMessage.constructor = AvatarMessage;

/**
 * DTO for XML browse options.
 * @class
 * @property type
 * @property typeFilter
 * @property category
 * @property tag
 * @property filter
 * @property sort
 */
function BrowseConfig() {
	this.type;
	this.typeFilter;
	this.category;
	this.tag;
	this.filter;
	this.sort;
	
	this.toXML = function() {
		var xml = "<browse";
		xml = this.writeCredentials(xml);
		xml = xml + (" type=\"" + this.type + "\"");
		if (this.typeFilter != null) {
			xml = xml + (" typeFilter=\"" + this.typeFilter + "\"");
		}
		if (this.sort != null) {
			xml = xml + (" sort=\"" + this.sort + "\"");
		}
		if ((this.category != null) && this.category != "") {
			xml = xml + (" category=\"" + this.category + "\"");
		}
		if ((this.tag != null) && this.tag != "") {
			xml = xml + (" tag=\"" + this.tag + "\"");
		}
		if ((this.filter != null) && this.filter != "") {
			xml = xml + (" filter=\"" + this.filter + "\"");
		}
		xml = xml + ("/>");
		return xml;
	}
}
BrowseConfig.prototype = new Config();
BrowseConfig.prototype.constructor = BrowseConfig;
BrowseConfig.constructor = BrowseConfig;

/**
 * DTO for XML web medium config.
 * @class
 * @property id
 * @property name
 * @property isAdmin
 * @property isAdult
 * @property isPrivate
 * @property isHidden
 * @property accessMode
 * @property isFlagged
 * @property description
 * @property details
 * @property disclaimer
 * @property tags
 * @property categories
 * @property flaggedReason
 * @property creator
 * @property creationDate
 * @property lastConnectedUser
 * @property license
 * @property avatar
 * @property connects
 * @property dailyConnects
 * @property weeklyConnects
 * @property monthlyConnects
 */
function WebMediumConfig() {
	this.id;
	this.name;
	this.isAdmin;
	this.isAdult;
	this.isPrivate;
	this.isHidden;
	this.accessMode;
	this.isFlagged;
	this.description;
	this.details;
	this.disclaimer;
	this.tags;
	this.categories;
	this.flaggedReason;
	this.creator;
	this.creationDate;
	this.lastConnectedUser;
	this.license;
	this.avatar;
	this.connects;
	this.dailyConnects;
	this.weeklyConnects;
	this.monthlyConnects;

	this.writeWebMediumXML = function(xml) {
		xml = this.writeCredentials(xml);
		if (this.id != null) {
			xml = xml + (" id=\"" + this.id + "\"");
		}
		if (this.name != null) {
			xml = xml + (" name=\"" + this.name + "\"");
		}
		if (this.isPrivate) {
			xml = xml + (" isPrivate=\"true\"");
		}
		if (this.isHidden) {
			xml = xml + (" isHidden=\"true\"");
		}
		if (this.accessMode != null && this.accessMode != "") {
			xml = xml + (" accessMode=\"" + this.accessMode + "\"");
		}
		if (this.isAdult) {
			xml = xml + (" isAdult=\"true\"");
		}
		if (this.isFlagged) {
			xml = xml + (" isFlagged=\"true\"");
		}
		xml = xml + (">");
		if (this.description != null) {
			xml = xml + ("<description>");
			xml = xml + (SDK.escapeHTML(this.description));
			xml = xml + ("</description>");
		}
		if (this.details != null) {
			xml = xml + ("<details>");
			xml = xml + (SDK.escapeHTML(this.details));
			xml = xml + ("</details>");
		}
		if (this.disclaimer != null) {
			xml = xml + ("<disclaimer>");
			xml = xml + (this.disclaimer);
			xml = xml + ("</disclaimer>");
		}
		if (this.categories != null) {
			xml = xml + ("<categories>");
			xml = xml + (SDK.escapeHTML(this.categories));
			xml = xml + ("</categories>");
		}
		if (this.tags != null) {
			xml = xml + ("<tags>");
			xml = xml + (SDK.escapeHTML(this.tags));
			xml = xml + ("</tags>");
		}
		if (this.license != null) {
			xml = xml + ("<license>");
			xml = xml + (SDK.escapeHTML(this.license));
			xml = xml + ("</license>");
		}
		if (this.flaggedReason != null) {
			xml = xml + ("<flaggedReason>");
			xml = xml + (SDK.escapeHTML(this.flaggedReason));
			xml = xml + ("</flaggedReason>");
		}
		return xml;
	}
	
	this.parseWebMediumXML = function(element) {
		this.id = element.getAttribute("id");
		this.name = element.getAttribute("name");
		this.creationDate = element.getAttribute("creationDate");
		this.isPrivate = element.getAttribute("isPrivate");
		this.isHidden = element.getAttribute("isHidden");
		this.accessMode = element.getAttribute("accessMode");
		this.isAdmin = element.getAttribute("isAdmin");
		this.isAdult = element.getAttribute("isAdult");
		this.isFlagged = element.getAttribute("isFlagged");
		this.creator = element.getAttribute("creator");
		this.creationDate = element.getAttribute("creationDate");
		this.connects = element.getAttribute("connects");
		this.dailyConnects = element.getAttribute("dailyConnects");
		this.weeklyConnects = element.getAttribute("weeklyConnects");
		this.monthlyConnects = element.getAttribute("monthlyConnects");
		
		var node = element.getElementsByTagName("description")[0];
		if (node != null) {
			this.description = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("details")[0];
		if (node != null) {
			this.details = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("disclaimer")[0];
		if (node != null) {
			this.disclaimer = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("categories")[0];
		if (node != null) {
			this.categories = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("tags")[0];
		if (node != null) {
			this.tags = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("flaggedReason")[0];
		if (node != null) {
			this.flaggedReason = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("lastConnectedUser")[0];
		if (node != null) {
			this.lastConnectedUser = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("license")[0];
		if (node != null) {
			this.license = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("avatar")[0];
		if (node != null) {
			this.avatar = SDK.innerHTML(node);
		}
	}
}
WebMediumConfig.prototype = new Config();
WebMediumConfig.prototype.constructor = WebMediumConfig;
WebMediumConfig.constructor = WebMediumConfig;

/**
 * DTO for XML channel config.
 * @class
 * @property type
 * @property messages
 * @property usersOnline
 * @property adminsOnline
 */
function ChannelConfig() {
	this.type;
	this.messages;
	this.usersOnline;
	this.adminsOnline;
	
	this.type = "channel";
	
	this.credentials = function() {
		var config = new ChannelConfig();
		config.id = this.id;
		return config;
	}
	
	this.toXML = function() {
		var xml = "<channel";
		if (this.type != null && this.type != "") {
			xml = xml + (" type=\"" + this.type + "\"");
		}
		xml = this.writeWebMediumXML(xml);
		xml = xml + ("</channel>");
		return xml;
	}
	
	this.parseXML = function(element) {
		this.parseWebMediumXML(element);
		this.type = element.getAttribute("type");
		this.messages = element.getAttribute("messages");
		this.usersOnline = element.getAttribute("usersOnline");
		this.adminsOnline = element.getAttribute("adminsOnline");
	}
}
ChannelConfig.prototype = new WebMediumConfig();
ChannelConfig.prototype.constructor = ChannelConfig;
ChannelConfig.constructor = ChannelConfig;

/**
 * DTO for XML content config.
 * @class
 * @property type
 */
function ContentConfig() {	
	this.type;	
	
	this.parseXML = function(element) {		
		this.type = element.getAttribute("type");
	}

	
	this.toXML = function() {
		var xml = "<content";
		xml = this.writeCredentials(xml);

		if (this.type != null) {
			xml = xml + (" type=\"" + this.type + "\"");
		}
		
		xml = xml + ("/>");
		return xml;
	}
}
ContentConfig.prototype = new Config();
ContentConfig.prototype.constructor = ContentConfig;
ContentConfig.constructor = ContentConfig;

/**
 * DTO for XML domain config.
 * @class
 * @property creationMode
 */
function DomainConfig() {
	this.creationMode;
	
	this.type = "domain";
	
	this.credentials = function() {
		var config = new DomainConfig();
		config.id = this.id;
		return config;
	}
	
	this.toXML = function() {
		var xml = "<domain";
		if (this.creationMode != null && this.creationMode != "") {
			xml = xml + (" creationMode=\"" + this.creationMode + "\"");
		}
		this.xml = this.writeWebMediumXML(xml);
		xml = xml + ("</domain>");
		return xml;
	}
	
	this.parseXML = function(element) {
		this.parseWebMediumXML(element);
		this.creationMode = element.getAttribute("creationMode");
	}
}
DomainConfig.prototype = new WebMediumConfig();
DomainConfig.prototype.constructor = DomainConfig;
DomainConfig.constructor = DomainConfig;

/**
 * DTO for XML forum config.
 * @class
 * @property replyAccessMode
 * @property postAccessMode
 * @property posts
 */
function ForumConfig() {
	this.replyAccessMode;
	this.postAccessMode;
	this.posts;
	
	this.type = "forum";
	
	this.credentials = function() {
		var config = new ForumConfig();
		config.id = this.id;
		return config;
	}
	
	this.toXML = function() {
		var xml = xml + ("<forum");
		if (this.replyAccessMode != null && !this.replyAccessMode == "") {
			xml = xml + (" replyAccessMode=\"" + this.replyAccessMode + "\"");
		}
		if (this.postAccessMode != null && !this.postAccessMode == "") {
			xml = xml + (" postAccessMode=\"" + this.postAccessMode + "\"");
		}
		xml = this.writeWebMediumXML(xml);
		xml = xml + ("</forum>");
		return xml;
	}
	
	this.parseXML = function(element) {
		this.parseWebMediumXML(element);
		this.replyAccessMode = element.getAttribute("replyAccessMode");
		this.postAccessMode = element.getAttribute("postAccessMode");
		this.posts = element.getAttribute("posts");
	}
}
ForumConfig.prototype = new WebMediumConfig();
ForumConfig.prototype.constructor = ForumConfig;
ForumConfig.constructor = ForumConfig;

/**
 * DTO for XML forum post config.
 * @class
 * @property id
 * @property topic
 * @property summary
 * @property details
 * @property detailsText
 * @property forum
 * @property tags
 * @property isAdmin
 * @property isFlagged
 * @property flaggedReason
 * @property isFeatured
 * @property creator
 * @property creationDate
 * @property views
 * @property dailyViews
 * @property weeklyViews
 * @property monthlyViews
 * @property replyCount
 * @property parent
 * @property replies
 */
function ForumPostConfig() {	
	this.id;
	this.topic;
	this.summary;
	this.details;
	this.detailsText;
	this.forum;
	this.tags;
	this.isAdmin;
	this.isFlagged;
	this.flaggedReason;
	this.isFeatured;
	this.creator;
	this.creationDate;
	this.views;
	this.dailyViews;
	this.weeklyViews;
	this.monthlyViews;
	this.replyCount;
	this.parent;
	this.avatar;
	this.replies;
	
	this.toXML = function() {
		var xml = "<forum-post";
		xml = this.writeCredentials(xml);
		if (this.id != null) {
			xml = xml + (" id=\"" + this.id + "\"");
		}
		if (this.parent != null) {
			xml = xml + (" parent=\"" + this.parent + "\"");
		}
		if (this.forum != null) {
			xml = xml + (" forum=\"" + this.forum + "\"");
		}
		if (this.isFeatured) {
			xml = xml + (" isFeatured=\"true\"");
		}
		xml = xml + (">");
		if (this.topic != null) {
			xml = xml + ("<topic>");
			xml = xml + (SDK.escapeHTML(this.topic));
			xml = xml + ("</topic>");
		}
		if (this.details != null) {
			xml = xml + ("<details>");
			xml = xml + (SDK.escapeHTML(this.details));
			xml = xml + ("</details>");
		}
		if (this.tags != null) {
			xml = xml + ("<tags>");
			xml = xml + (SDK.escapeHTML(this.tags));
			xml = xml + ("</tags>");
		}
		xml = xml + ("</forum-post>");
	}
	
	this.parseXML = function(element) {
		this.id = element.getAttribute("id");
		this.parent = element.getAttribute("parent");
		this.forum = element.getAttribute("forum");
		this.views = element.getAttribute("views");
		this.dailyViews = element.getAttribute("dailyViews");
		this.weeklyViews = element.getAttribute("weeklyViews");
		this.monthlyViews = element.getAttribute("monthlyViews");
		this.isAdmin = element.getAttribute("isAdmin");
		this.replyCount = element.getAttribute("replyCount");
		this.isFlagged = element.getAttribute("isFlagged");
		this.isFeatured = element.getAttribute("isFeatured");
		this.creator = element.getAttribute("creator");
		this.creationDate = element.getAttribute("creationDate");
		
		var node = element.getElementsByTagName("summary")[0];
		if (node != null) {
			this.summary = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("details")[0];
		if (node != null) {
			this.details = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("detailsText")[0];
		if (node != null) {
			this.detailsText = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("topic")[0];
		if (node != null) {
			this.topic = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("tags")[0];
		if (node != null) {
			this.tags = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("flaggedReason")[0];
		if (node != null) {
			this.flaggedReason = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("avatar")[0];
		if (node != null) {
			this.avatar = SDK.innerHTML(node);
		}
		var nodes = element.getElementsByTagName("replies");
		if (nodes != null && nodes.length > 0) {
			this.replies = [];
			for (var index = 0; index < nodes.length; index++) {
				var reply = nodes[index];
				var config = new ForumPostConfig();
				config.parseXML(reply);
				this.replies[replies.length] = (config);
			}
		}
	}
}
ForumPostConfig.prototype = new Config();
ForumPostConfig.prototype.constructor = ForumPostConfig;
ForumPostConfig.constructor = ForumPostConfig;

/**
 * The Instance config object defines the settings for a bot instance.
 * It is used to create, edit, and reference a bot.
 * It inherits from the WebMediumConfig class.
 * @see {@link WebMediumConfig}
 * @class
 * @property size
 * @property allowForking
 * @property template
 */
function InstanceConfig() {
	this.size;
	this.allowForking;
	this.template;
	
	this.type = "instance";
	
	this.credentials = function() {
		var config = new InstanceConfig();
		config.id = this.id;
		return config;
	}
	
	this.toXML = function() {
		var xml = "<instance";
		if (this.allowForking) {
			xml = xml + (" allowForking=\"true\"");
		}
		xml = this.writeWebMediumXML(xml);
		if (this.template != null) {
			xml = xml + ("<template>");
			xml = xml + (this.template);
			xml = xml + ("</template>");
		}
		xml = xml + ("</instance>");
		return xml;
	}
	
	this.parseXML = function(element) {
		this.parseWebMediumXML(element);
		this.allowForking = element.getAttribute("allowForking");
		this.size = element.getAttribute("size");
		
		var node = element.getElementsByTagName("template")[0];
		if (node != null) {
			this.template = SDK.innerHTML(node);
		}
	}
}
InstanceConfig.prototype = new WebMediumConfig();
InstanceConfig.prototype.constructor = InstanceConfig;
InstanceConfig.constructor = InstanceConfig;

/**
 * The Media config object is used the send and retrieve image, video, audio, and file attachments with the server.
 * Media and file attachments can be sent linked in chat messages or forum posts.
 * It inherits from the Config class.
 * @see {@link Config}
 * @class
 * @property id
 * @property name
 * @property type
 * @property file
 * @property key
 */
function MediaConfig() {
	this.id;	
	this.name;
	this.type;
	this.file;
	this.key;
	
	this.parseXML = function (element) {		
		this.id = element.getAttribute("id");
		this.name = element.getAttribute("name");
		this.type = element.getAttribute("type");
		this.file = element.getAttribute("file");
		this.key = element.getAttribute("key");
	}
	
	this.toXML = function() {
		var xml = "<media";
		xml = this.writeCredentials(xml);

		if (this.id != null) {
			xml = xml + (" id=\"" + this.id + "\"");
		}
		if (this.name != null) {
			xml = xml + (" name=\"" + this.name + "\"");
		}
		if (this.file != null) {
			xml = xml + (" file=\"" + this.file + "\"");
		}
		if (this.key != null) {
			xml = xml + (" key=\"" + this.key + "\"");
		}
		
		xml = xml + ("/>");
		return xml;
	}
}
MediaConfig.prototype = new Config();
MediaConfig.prototype.constructor = MediaConfig;
MediaConfig.constructor = MediaConfig;

/**
 * The Voice config object allows the bot's voice to be configured.
 * It inherits from the Config class.
 * @see {@link Config} Config
 * @class
 * @property language
 * @property pitch
 * @property speechRate
 */
function VoiceConfig() {
	this.language;
	this.pitch;
	this.speechRate;
	
	this.parseXML = function (element) {		
		this.language = element.getAttribute("language");
		this.pitch = element.getAttribute("pitch");
		this.speechRate = element.getAttribute("speechRate");
	}

	
	this.toXML = function() {
		var xml = "<voice";
		xml = this.writeCredentials(xml);

		if (this.language != null) {
			xml = xml + (" language=\"" + this.language + "\"");
		}
		if (this.pitch != null) {
			xml = xml + (" pitch=\"" + this.pitch + "\"");
		}
		if (this.speechRate != null) {
			xml = xml + (" speechRate=\"" + this.speechRate + "\"");
		}
		
		xml = xml + ("/>");
		return xml;
	}
}
VoiceConfig.prototype = new Config();
VoiceConfig.prototype.constructor = VoiceConfig;
VoiceConfig.constructor = VoiceConfig;

/**
 * The Training config object allows new responses to be added to the bot.
 * It supports four operations, AddGreeting, RemoveGreeting, AddDefaultResponse, RemoveDefaultResponse, and AddResponse.
 * It inherits from the Config class.
 * @see {@link Config} Config
 * @class
 * @property operation
 * @property pitch
 * @property speechRate
 */
function TrainingConfig() {
	this.operation;
	this.question;
	this.response;
	
	this.parseXML = function (element) {		
		this.operation = element.getAttribute("operation");
		var node = element.getElementsByTagName("question")[0];
		if (node != null) {
			this.question = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("response")[0];
		if (node != null) {
			this.response = SDK.innerHTML(node);
		}
	}
	
	this.toXML = function() {
		var xml = "<training";
		xml = this.writeCredentials(xml);

		if (this.operation != null) {
			xml = xml + (" operation=\"" + this.operation + "\"");
		}
		xml = xml + (">");
		if (this.question != null) {
			xml = xml + ("<question>");
			xml = xml + (SDK.escapeHTML(this.question));
			xml = xml + ("</question>");
		}
		if (this.response != null) {
			xml = xml + ("<response>");
			xml = xml + (SDK.escapeHTML(this.response));
			xml = xml + ("</response>");
		}
		xml = xml + ("</training>");
		
		xml = xml + ("/>");
		return xml;
	}
}
TrainingConfig.prototype = new Config();
TrainingConfig.prototype.constructor = TrainingConfig;
TrainingConfig.constructor = TrainingConfig;

/**
 * Allow async loading callback.
 */
if (typeof SDK_onLoaded !== 'undefined') {
	SDK_onLoaded();
}
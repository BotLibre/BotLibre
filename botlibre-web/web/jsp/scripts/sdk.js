/******************************************************************************
 *
 *  Copyright 2014-2019 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *	  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

/**
 * Bot Libre open SDK.
 * This JavaScript SDK lets you access chat bot, live chat, chatroom, forum, script, graphic, user services on
 * the Bot Libre compatible websites, including:
 * - Bot Libre!
 * - Bot Libre for Business
 * - Live Chat libre!
 * - Forums libre!
 * 
 * This JavaScript script can be used directly, or copied/modified on your own website.
 * 
 * The SDK consist of two main class, SDKConnection and LiveChatConnection.
 * 
 * SDKConnection uses AJAX calls to provide access to the Bot Libre REST API.
 * This is used for chat bots, live chat, chatrooms, forums, issue tracking, user admin, and workspaces.
 * 
 * LiveChatConnection uses web sockets to provide access to live chat and chatrooms.
 * 
 * Version: 8.0.0-2019-12-10
 */

/**
 * Static class for common util functions and static properties.
 * @class
 */
var SDK = {};

SDK.DOMAIN = "bot.domain.com";
SDK.NAME = "botlibreplatform";
SDK.APP = "";

SDK.DOMAIN = window.location.host;
//SDK.DOMAIN = "192.168.0.16:9080";
//SDK.APP = "/botlibreplatform";

SDK.PATH = "/rest/api";
SDK.MAX_FILE_UPLOAD = 5000000;

SDK.host = SDK.DOMAIN;
SDK.app = SDK.APP;
SDK.scheme = "https:" == document.location.protocol ? "https" : "http";
//SDK.scheme = "https";
SDK.url = SDK.scheme + "://" + SDK.DOMAIN + SDK.APP;
SDK.rest = SDK.url + SDK.PATH;
SDK.backlinkURL = SDK.url;
SDK.backlink = false;
SDK.commands = true;

/**
 * You must set your application ID to use the SDK.
 * You can obtain your application ID from your user page.
 * @static
 */
SDK.applicationId = null;

/**
 * Set the active language code.
 * This is used for voice recognition.
 */
SDK.lang = "en";

/**
 * Enable debug logging.
 * @static
 */
SDK.debug = false;

/**
 * Escape and filter bot messages for HTML and JavaScript content for XSS security.
 * This prevents bots sending advanced HTML and JavaScript in their messages.
 * Set this to false to allow your bot to send JavaScript.
 * @static
 */
SDK.secure = true;

/**
 * Force avatars to enable or disable canvas for video (currently used only for Chrome and Firefox).
 * @static
 */
SDK.useCanvas = null;

/**
 * Force avatars to enable or disable video (currently disabled for Safari on iPhone).
 * @static
 */
SDK.useVideo = null;

/**
 * Set the background for video avatars.
 * @static
 */
SDK.videoBackground = true;

/**
 * Attempt to fix grey mp4 video background (only used for Chrome).
 * @static
 */
SDK.fixBrightness = null;

/**
 * Attempt to fix an issue with Chrome not processing the CSS after correctly when the chat bubble resizes.
 * @static
 */
SDK.fixChromeResizeCSS = false;

/**
 * Set the error static field to trap or log any errors.
 */
SDK.error = function(message) {
	console.log(message);
}

/**
 * Allow our native speech API to use the third party ResponsiveVoice API.
 * You must create an account with ResponsiveVoice to use their API, see https://responsivevoice.com
 * @static
 */
SDK.responsiveVoice = false;
SDK.speechSynthesis = 'speechSynthesis' in window;
/**
 * The speechRate can be set to change the native speech voice speed.
 * It can range between 0.1 (lowest) and 10.0 (highest).
 * 1.0 is the default rate for the current platform or voice.
 * Other values act as a percentage relative to this, so for example 2.0 is twice as fast, 0.5 is half as fast.
 */
SDK.speechRate = null;
/**
 * The speechPitch can be set to change the native speech voice pitch.
 * It can range between 0.0 (lowest) and 2.0 (highest), with 1.0 being the default pitch for the current platform or voice.
 */
SDK.speechRate = null;
SDK.initResponsiveVoice = function() {
	if (!('responsiveVoice' in window)) {
		console.log("ResponsiveVoice missing, you must load its script first");
		return;
	}
	SDK.responsiveVoice = true;
	SDK.speechSynthesis = true;
}
if (!('SpeechSynthesisUtterance' in window)) {
	function SpeechSynthesisUtterance2(text) {
		this.text = text;
	}
}

/**
 * Allow our native speech API to use the third party Bing Speech API. You must
 * create an account with Bing Speech to use their API, see
 * https://azure.microsoft.com/en-us/try/cognitive-services
 * 
 * @static
 */
SDK.bingSpeech = false;
SDK.initBingSpeech = function(instanceId, type) {
	SDK.bingSpeech = true;
	SDK.speechSynthesis = true;
	SDK.speechInstance = instanceId;
	SDK.speechType = type;
	console.log("Initializing Bing speech.");
}

/**
 * Allow our native speech API to use the third party QQ Speech API. 
 * 
 * @static
 */
SDK.qqSpeech = false;
SDK.initQQSpeech = function(instanceId, type) {
	SDK.qqSpeech = true;
	SDK.speechSynthesis = true;
	SDK.speechInstance = instanceId;
	SDK.speechType = type;
	console.log("Initializing QQ speech.");	
}

SDK.currentAudio = null;
SDK.recognition = null;
SDK.recognitionActive = false;
SDK.backgroundAudio = null;
SDK.currentBackgroundAudio = null;
SDK.timers = {};
/**
 * Track if auto play of media is enabled in the browser (mobile Chrome/Safari)
 * Enable or disable to force audio auto play.
 */
SDK.canPlayAudio = null;
/**
 * Track if auto play of media is enabled in the browser (mobile Chrome/Safari)
 * Enable or disable to force video auto play.
 */
SDK.canPlayVideo = null;
SDK.disableAudioAutoPlay = false;
SDK.audio = null;
SDK.autoPlayActionAudio = null;
SDK.autoPlayBackgroundAudio = null;
SDK.autoPlayDelay = 2000;

/**
 * For some browsers audio must be initialized from a click event.
 */
SDK.initAudio = function() {
	SDK.canPlayVideo = true;
	SDK.canPlayAudio = true;
	if (SDK.audio == null) {
		SDK.audio = new Audio(SDK.url + '/chime.mp3');
		SDK.audio.load();
	}
	if (SDK.autoPlayActionAudio == null) {
		SDK.autoPlayActionAudio = new Audio(SDK.url + '/chime.mp3');
		SDK.autoPlayActionAudio.load();
		SDK.autoPlayBackgroundAudio = new Audio(SDK.url + '/chime.mp3');
		SDK.autoPlayBackgroundAudio.load();
	}
}

/**
 * Play the audio file given the url.
 */
SDK.play = function(file, channelaudio) {
	SDK.pauseSpeechRecognition();
	var audio = null;
	if (SDK.audio != null) {
		audio = SDK.audio;
		audio.pause();
		audio.onended = null;
		audio.onpause = null;
		audio.oncanplay = null;
		audio.src = file;
	} else {
		audio = new Audio(file);
	}
	if (SDK.recognitionActive) {
		audio.onended = function() {
			SDK.startSpeechRecognition();
			if (channelaudio != false) {
				SDK.currentAudio = null;
			}
		};
	} else if (channelaudio != false) {
		audio.onended = function() {
			SDK.currentAudio = null;
		};
	}
	var playPromise = null;
	if (channelaudio == false) {
		playPromise = audio.play();
	} else {
		if (SDK.currentAudio != null && !SDK.currentAudio.ended && !SDK.currentAudio.paused) {
			SDK.currentAudio.onpause = function() {
				SDK.currentAudio = audio;
				playPromise = audio.play();
			};
			SDK.currentAudio.pause();
		} else {
			SDK.currentAudio = audio;
			playPromise = audio.play();
		}
	}
	if (playPromise !== undefined && SDK.canPlayAudio == null) {
		playPromise.then(function() {
			SDK.canPlayAudio = true;
		}).catch(function(error) {
			if (SDK.canPlayAudio == null) {
				SDK.canPlayAudio = false;
				SDK.playInitAudio = function() {
					if (SDK.playInitVideo != null) {
						SDK.playInitVideo();
					} else {
						SDK.audio = new Audio(file);
						SDK.currentAudio = SDK.audio;
						SDK.currentAudio.onended = function() {
							SDK.currentAudio = null;
						};
						SDK.canPlayAudio = true;
						SDK.audio.play();
						document.getElementById("sdkplaybutton").style.display = "none";
						var sdkvideoplaybutton2 = document.getElementById("sdkvideoplaybutton2");
						if (sdkvideoplaybutton2 != null) {
							sdkvideoplaybutton2.style.display = "none";
						}
					}
				}
				var playButton = document.createElement('div');
				var html = "<div id='sdkplaybutton' style='position:fixed;bottom:32px;left:32px;z-index:164;'><img onclick='SDK.playInitAudio()' width='64' src='"
					+ SDK.url + "/images/playsound.png'/></div>"
				playButton.innerHTML = html;
				SDK.body().appendChild(playButton);
				setTimeout(function() {
					document.getElementById("sdkplaybutton").style.display = "none";
				}, 10000);
			}
		});
	}
	return audio;
}

SDK.playChime = true;

/**
 * Play the chime sound.
 */
SDK.chime = function() {
	if (SDK.playChime) {
		this.play(SDK.url + '/chime.mp3');
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
SDK.tts = function(text, voice, native, lang, nativeVoice, mod, apiKey = null, apiEndpoint = null) {
	try {
		if ((native || (native == null && voice == null)) && SDK.speechSynthesis) {
			var utterance = null;
			if ('SpeechSynthesisUtterance' in window) {
				utterance = new SpeechSynthesisUtterance(text);
			} else {
				utterance = new SpeechSynthesisUtterance2(text);
			}
			SDK.nativeTTS(utterance, lang, nativeVoice, apiKey, apiEndpoint);
		} else {
			var url = SDK.rest + '/form-speak?&text=';
			url = url + encodeURIComponent(text);
			if (voice != null) {
				url = url + '&voice=' + voice;
			}
			if (mod != null) {
				url = url + '&mod=' + mod;
			}
			if (SDK.applicationId != null) {
				url = url + '&application=' + SDK.applicationId;
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
 * Use the ResponsiveVoice API.
 */
SDK.responsiveVoiceTTS = function(utterance, lang, voice) {
	var events = {};
	try {
		SDK.pauseSpeechRecognition();
		if (voice == null || voice == "") {
			voice = "US English Female";
		}
		if (utterance.onend != null) {
			events.onend = utterance.onend;
		}
		if (SDK.recognitionActive) {
			events.onend = function() {
				SDK.startSpeechRecognition();
				if (utterance.onend != null) {
					utterance.onend();
				}
			}
		}
		if (utterance.onstart != null) {
			events.onstart = utterance.onstart;
		}
		responsiveVoice.speak(utterance.text, voice, events);
	} catch (error) {
		console.log(error);
	}
}

/**
 * Use the Bing Speech API.
 */
SDK.bingSpeechTTS = function(utterance, lang, voice, apiKey = null, apiEndpoint = null) {
	
	try {
		if(utterance==null || utterance.text=="") {
			return;
		}
		
		SDK.pauseSpeechRecognition();
		if (voice == null || voice == "") {
			voice = "en-US, JessaRUS";
		}
		
		var url = SDK.rest + '/form-speak?&text=';
		url = url + encodeURIComponent(utterance.text);

		if (SDK.applicationId != null) {
			url = url + '&application=' + SDK.applicationId;
		}
		if (SDK.speechInstance != null) {
			url = url + '&instance=' + SDK.speechInstance;
		}
		if(SDK.speechType == "avatar") {
			url = url + '&embeddedAvatar=true';
		}
		
		if (apiKey != null) {
			url = url + '&apiKey=' + apiKey;
		}
		if (apiEndpoint != null) {
			url = url + '&apiEndpoint=' + apiEndpoint;
		}
		
		url = url + '&voice=' + encodeURIComponent(voice);
		
		url = url + '&provider=bing';
			
		var request = new XMLHttpRequest();
		var self = this;
		request.onreadystatechange = function() {
			if (request.readyState != 4) return;
			if (request.status != 200) {
				if('bingApiKeyTr' in window) {
					SDK.showError("Invalid API Key or API Endpoint");
				}
				console.log('Error: Bing Speech web request failed: ' + request.statusText);
			}
			
			var audio = self.play(SDK.url + "/" + request.responseText);
			audio.onplay = utterance.onstart;
			audio.onended = utterance.onend;
		}
		
		request.open('GET', url, true);
		request.send();
		
	} catch (error) {
		console.log(error);
	}
}

/**
 * Use the QQ Speech API.
 */
SDK.qqSpeechTTS = function(utterance, lang, voice) {

	try {
		SDK.pauseSpeechRecognition();
		
		var url = SDK.rest + '/form-speak?&text=';
		url = url + encodeURIComponent(utterance.text);

		if (SDK.applicationId != null) {
			url = url + '&application=' + SDK.applicationId;
		}
		if (SDK.speechInstance != null) {
			url = url + '&instance=' + SDK.speechInstance;
		}
		if(SDK.speechType == "avatar") {
			url = url + '&embeddedAvatar=true';
		}
		if (voice != null) {
			url = url + '&voice=' + voice;
		}
		
		url = url + '&provider=qq';

		var request = new XMLHttpRequest();
		var self = this;
		request.onreadystatechange = function() {
			if (request.readyState != 4) return;
			if (request.status != 200) {
				console.log('Error: QQ Speech web request failed.');
				return;
			}
			var audio = self.play(SDK.url + "/" + request.responseText);
			audio.onplay = utterance.onstart;
			audio.onended = utterance.onend;
		}
		
		request.open('GET', url, true);
		request.send();
		
	} catch (error) {
		console.log(error);
	}
}

/**
 * Speak the native utterance first setting the voice and language.
 */
SDK.nativeTTS = function(utterance, lang, voice, apiKey = null, apiEndpoint = null) {
	if (SDK.speechRate != null) {
		utterance.rate = SDK.speechRate;
	}
	if (SDK.speechPitch != null) {
		utterance.pitch = SDK.speechPitch;
	}
	if (SDK.responsiveVoice) {
		SDK.responsiveVoiceTTS(utterance, lang, voice);
		return;
	} else if (SDK.bingSpeech) {
		SDK.bingSpeechTTS(utterance, lang, voice, apiKey, apiEndpoint);
		return;
	}
	else if (SDK.qqSpeech) {
		SDK.qqSpeechTTS(utterance, lang, voice);
		return;
	}
	if (lang == null) {
		lang = SDK.lang;
	}
	SDK.pauseSpeechRecognition();
	if (SDK.recognitionActive) {
		utterance.addEventListener("end", function() {
			SDK.startSpeechRecognition();
		});
	}
	speechSynthesis.cancel();
	if (lang == null && voice == null) {
		// Events don't always get fired unless this is done...
		setTimeout(function() {
			speechSynthesis.speak(utterance);
		}, 100);
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
 * Allow text to be translated into another language is the interface elements.
 */
SDK.translator = null;
SDK.translate = function(text) {
	if (SDK.translator == null) {
		SDK.translator = SDK.translators[SDK.lang];
		if (SDK.translator == null) {
			SDK.translator = {};
		}
	}
	var translated = SDK.translator[text];
	if (translated != null) {
		return translated;
	}
	return text
}
SDK.translators = {
	"pt" : {
		"Yes" : "Sim",
		"No" : "Não",
		"Name" : "Nome",
		"Email" : "O email",
		"Phone" : "Telemóvel",
		"Connect" : "Ligar",
		"Speech" : "Discurso",
		"Enter name" : "Insira o nome",
		"Chat Log" : "Registro de bate-papo",
		"Choose Language" : "Escolha o seu idioma",
		"Enter valid email" : "Digite e-mail válido",
		"Ping server" : "Servidor Ping",
		"Flag user" : "Flag user",
		"Whisper user" : "Usuário Whisper",
		"Request private" : "Pedido privado",
		"Clear log" : "Log clara",
		"Accept private" : "Aceite privado",
		"Send image" : "Enviar imagem",
		"Send file" : "Enviar arquivo",
		"Email Chat Log" : "Registro de bate-papo por email",
		"Chime" : "Chime",
		"Exit chat" : "Sair do bate-papo",
		"Text to speech" : "Texto para fala",
		"Speech recognition" : "Reconhecimento de fala",
		"Speech Recognition" : "Reconhecimento de fala",
		"Quit private channel" : "Saia do canal privado",
		"Quit private or channel" : "Sair privado ou canal",
		"Would you like a copy of the chat log sent to your email?" : "Gostaria de uma cópia do registro de bate-papo enviado para o seu e-mail?"
	},
	"fr" : {
		"Yes" : "Oui",
		"No" : "Non",
		"Name" : "Prénom",
		"Email" : "Email",
		"Phone" : "Téléphone",
		"Connect" : "Relier",
		"Speech" : "Discours",
		"Enter name" : "Entrez le nom",
		"Chat Log" : "Journal de chat",
		"Choose Language" : "Choisir la langue",
		"Enter valid email" : "Entrez une adresse email valide",
		"Ping server" : "Serveur ping",
		"Flag user" : "Utilisateur du drapeau",
		"Whisper user" : "Whisper utilisateur",
		"Request private" : "Demander privé",
		"Clear log" : "Effacer le journal",
		"Accept private" : "Accepter privé",
		"Send image" : "Envoyer une image",
		"Send file" : "Envoyer le fichier",
		"Email Chat Log" : "Journal de messagerie électronique",
		"Chime" : "Carillon",
		"Exit chat" : "Quitter le chat",
		"Text to speech" : "Texte pour parler",
		"Speech recognition" : "Reconnaissance vocale",
		"Speech Recognition" : "Reconnaissance vocale",
		"Quit private channel" : "Quitter la chaîne privée",
		"Quit private or channel" : "Quitter privé ou canal",
		"Would you like a copy of the chat log sent to your email?" : "Souhaitez-vous recevoir une copie du journal de chat envoyé à votre adresse e-mail?"
	},
	"es" : {
		"Yes" : "Sí",
		"No" : "No",
		"Name" : "Nombre",
		"Email" : "Email",
		"Phone" : "Teléfono",
		"Connect" : "Conectar",
		"Speech" : "Discurso",
		"Enter name" : "Ingrese el nombre",
		"Chat Log" : "Registro de chat",
		"Choose Language" : "Elija el idioma",
		"Enter valid email" : "Ingrese un correo electrónico válido",
		"Ping server" : "Servidor Ping",
		"Flag user" : "Usuario de bandera",
		"Whisper user" : "Usuario de Whisper",
		"Request private" : "Solicitud privada",
		"Clear log" : "Borrar registro",
		"Accept private" : "Aceptar privado",
		"Send image" : "Enviar imagen",
		"Send file" : "Enviar archivo",
		"Email Chat Log" : "Registro de chat de correo electrónico",
		"Chime" : "Campaneo",
		"Exit chat" : "Salir de chat",
		"Text to speech" : "Texto a voz",
		"Speech recognition" : "Reconocimiento de voz",
		"Speech Recognition" : "Reconocimiento de voz",
		"Quit private channel" : "Salir del canal privado",
		"Quit private or channel" : "Salir de privado o canal",
		"Would you like a copy of the chat log sent to your email?" : "Desea enviar un mensaje a su dirección de correo electrónico?"
	},
	"de" : {
		"Yes" : "Ja",
		"No" : "Nein",
		"Name" : "Name",
		"Email": "Email",
		"Phone" : "Telefon",
		"Connect" : "Verbinden",
		"Speech" : "Rede",
		"Enter name" : "Name eingeben",
		"Chat Log" : "Chat Protokoll",
		"Choose Language" : "Wähle eine Sprache",
		"Enter valid email" : "Geben Sie gültige E-Mail-Adresse ein",
		"Ping server" : "Ping-Server",
		"Flag user" : "Benutzer kennzeichnen",
		"Whisper user" : "Flüstern Benutzer",
		"Request private" : "Privat anfragen",
		"Clear log" : "Protokoll löschen",
		"Accept private" : "Akzeptiere privat",
		"Send image" : "Bild senden",
		"Send file" : "Datei senden",
		"Email Chat Log" : "E-Mail-Chatprotokoll",
		"Chime" : "Glockenspiel",
		"Exit chat" : "Chat beenden",
		"Text to speech" : "Text zu Sprache",
		"Speech recognition" : "Spracherkennung",
		"Speech Recognition" : "Spracherkennung",
		"Quit private channel" : "Beenden Sie den privaten Kanal",
		"Quit private or channel" : "Beenden Sie private oder Kanal",
		"Would you like a copy of the chat log sent to your email?" : "Möchten Sie eine Kopie des Chat-Protokolls an Ihre E-Mail-Adresse senden?"
	},
	"zh" : {
		"Yes" : "是",
		"No" : "沒有",
		"Name" : "名稱",
		"Email" : "電子郵件",
		"Phone" : "電話",
		"Connect" : "連",
		"Speech" : "言語",
		"Enter name" : "輸入名字",
		"Chat Log" : "聊天記錄",
		"Choose Language" : "選擇語言",
		"Enter valid email" : "輸入有效的郵件",
		"Ping server" : "叮噹服務器",
		"Flag user" : "標記用戶",
		"Whisper user" : "耳語用戶",
		"Request private" : "請求私人",
		"Clear log" : "清除日誌",
		"Accept private" : "接受私人",
		"Send image" : "發送圖像",
		"Send file" : "發送文件",
		"Email Chat Log" : "電子郵件聊天日誌",
		"Chime" : "鐘",
		"Exit chat" : "退出聊天",
		"Text to speech" : "文字轉語音",
		"Speech recognition" : "語音識別",
		"Speech Recognition" : "語音識別",
		"Quit private channel" : "退出私人頻道",
		"Quit private or channel" : "退出私人或頻道",
		"Would you like a copy of the chat log sent to your email?" : "你想要發送到你的電子郵件的聊天記錄的副本嗎?"
	},
	"ja" : {
		"Yes" : "はい",
		"No" : "いいえ",
		"Name" : "名",
		"Email" : "Eメール",
		"Phone" : "電話",
		"Connect" : "接続する",
		"Disconnect" : "切断する",
		"Speech" : "スピーチ",
		"Enter name" : "名前を入力",
		"Chat Log" : "チャットログ",
		"Clear log" : "ログをクリアする",
		"Choose Language" : "言語を選択する",
		"Enter valid email" : "有効なメールアドレスを入力",
		"Ping server" : "リングサーバー",
		"Flag user" : "ユーザーにフラグを設定する",
		"Whisper user" : "ささやくユーザー",
		"Request private" : "プライベートをリクエストする",
		"Accept private" : "プライベートを受け入れる",
		"Send image" : "画像を送る",
		"Send file" : "ファイルを送信",
		"Upload image" : "画像をアップロードする",
		"Upload file" : "ファイルをアップロードする",
		"Email Chat Log" : "メールチャットログ",
		"Chime" : "チャイム",
		"Exit chat" : "チャットを終了",
		"Text to speech" : "スピーチテキスト",
		"Speech recognition" : "音声認識",
		"Speech Recognition" : "音声認識",
		"Quit private channel" : "プライベートチャンネルを終了する",
		"Quit private or channel" : "プライベートまたはチャンネルを終了する",
		"Would you like a copy of the chat log sent to your email?" : "チャットログのコピーをメールに送信しますか?"
	},
	"ar" : {
		"Yes" : "نعم فعلا",
		"No" : "لا",
		"Name" : "اسم",
		"Email" : "البريد الإلكتروني",
		"Phone" : "",
		"Connect" : "هاتف",
		"Disconnect" : "قطع الاتصال",
		"Speech" : "خطاب",
		"Enter name" : "أدخل الاسم",
		"Chat Log" : "سجل الدردشة",
		"Clear log" : "سجل نظيف",
		"Choose Language" : "اختر اللغة",
		"Enter valid email" : "أدخل بريد إلكتروني صالحا",
		"Ping server" : "خادم بينغ",
		"Flag user" : "مستخدم العلم",
		"Whisper user" : "الهمس المستخدم",
		"Request private" : "طلب خاص",
		"Clear Log" : "سجل نظيف",
		"Accept private" : "قبول خاص",
		"Send image" : "إرسال صورة",
		"Send file" : "إرسال ملف",
		"Email Chat Log" : "سجل الدردشة عبر البريد الإلكتروني",
		"Chime" : "قرع الأجراس",
		"Exit chat" : "الخروج من الدردشة",
		"Text to speech" : "النص إلى الكلام",
		"Speech Recognition" : "التعرف على الكلام",
		"Quit private channel" : "إنهاء القناة الخاصة",
		"Quit private or channel": "إنهاء خاص أو قناة",
		"Would you like a copy of the chat log sent to your email?" : "هل تريد إرسال نسخة من سجل الدردشة إلى بريدك الإلكتروني؟"
	},
	"ru" : {
		"Yes" : "Да",
		"No" : "Hет",
		"Name" : "Имя",
		"Email": "Эл. почта",
		"Phone" : "Телефон",
		"Connect" : "Cоединить",
		"Disconnect" : "Отключить",
		"Speech" : "Pечь",
		"Enter name" : "Введите имя",
		"Chat Log" : "Журнал чата",
		"Choose Language" : "Выберите язык",
		"Enter valid email" : "Введите действующий адрес электронной почты",
		"Ping server" : "Пинг сервер",
		"Flag user" : "Oтметить пользователя",
		"Whisper user" : "Прошептать пользователю",
		"Request private" : "Частный запрос",
		"Clear log" : "Очистить журнал",
		"Clear Log" : "Очистить журнал",
		"Accept private" : "Принять конфиденциальнo",
		"Send image" : "Отправить изображение",
		"Send file" : "Отправить файл",
		"Email Chat Log" : "Oтправить журнал чата по электронной почте",
		"Chime" : "Звонок",
		"Exit chat" : "Выход из чата",
		"Text to speech" : "Текст в речь",
		"Speech Recognition" : "Распознавание речи",
		"Quit private channel" : "Выйти из частного канала",
		"Quit private or channel": "Выйти из частного канала",
		"Would you like a copy of the chat log sent to your email?" : "Oтправить копю журнала чата на ваш адрес электронной почты?"
	}
}

/**
 * Detect Chrome browser.
 */
SDK.isChrome = function() {
	var agent = navigator.userAgent.toLowerCase()
	return agent.indexOf('chrome') != -1 && agent.indexOf('edge') == -1;
}

/**
 * Detect Firefox browser.
 */
SDK.isFirefox = function() {
	return navigator.userAgent.toLowerCase().indexOf('firefox') != -1;
}

/**
 * Detect Safari browser.
 */
SDK.isSafari = function() {
	return navigator.userAgent.toLowerCase().indexOf('safari') != -1;
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
 * Detect iPhone OS.
 */
SDK.isIPhone = function() {
	if (navigator.userAgent.match(/iPhone/i)) {
		return true;
	}
	return false;
}

/**
 * Detect Mac OS.
 */
SDK.isMac = function() {
	return navigator.platform.toLowerCase().indexOf('mac') != -1;
}

SDK.hd = false;
SDK.format = (SDK.isChrome() || SDK.isFirefox()) ? "webm" : "mp4";
// Safari displays HTML5 video very poorly on iPhone.
if (SDK.isSafari() && SDK.isIPhone()) {
	SDK.format = "img";
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
 * Get the document body, and create one if missing.
 */
SDK.body = function() {
	var body = document.body || document.getElementsByTagName('body')[0];
	if (body == null) {
		body = document.createElement("body");
		document.body = body;
	}
	return body;
}

/**
 * Fix innerHTML for IE and Safari.
 */
SDK.innerHTML = function(element) {
	var html = element.innerHTML;
	if (html == null) {
		var serializer = new XMLSerializer();
		html = "";
		for (var index = 0; index < element.childNodes.length; index++) {
			html = html + serializer.serializeToString(element.childNodes[index]);
		}
		if (html.indexOf("&quot;") != -1) {
			html = html.replace(/&quot;/g, '"');
		}
	}
	var index = html.indexOf("&lt;");
	var index2 = html.indexOf("&gt;")
	if (index != -1 && index2 > index) {
		html = html.replace(/&lt;/g, "<");
		html = html.replace(/&gt;/g, ">");
	}
	if (html.indexOf("&amp;") != -1) {
		html = html.replace(/&amp;/g, "&");
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
	SDK.removeTags(element);
	return element.innerText || element.textContent;
}

SDK.removeTags = function(node) {
	if (node.className == 'nospeech' || node.tagName == 'SCRIPT' || node.tagName == 'SELECT' || node.tagName == 'BUTTON' || node.tagName == 'OPTION') {
		node.parentNode.removeChild(node);
	} else {
		var index = 0;
		var childNodes = node.childNodes;
		var children = [];
		while (index < childNodes.length) {
			children[index] = childNodes[index];
			index++;
		}
		var index = 0;
		while (index < children.length) {
			SDK.removeTags(children[index]);
			index++;
		}
	}
	return node;
}

/**
 * Replace reserved HTML character with their HTML escape codes.
 */
SDK.escapeHTML = function(html) {
	return html.replace(/&/g, "&amp;")
		.replace(/</g, "&lt;")
		.replace(/>/g, "&gt;")
		.replace(/"/g, "&quot;")
		.replace(/'/g, "&#039;");
}

SDK.unescapeHTML = function(html) {
	return html.replace(/&amp;/g, "&")
		.replace(/&lt;/g, "<")
		.replace(/&gt;/g, ">")
		.replace(/&quot;/g, "\"")
		.replace(/&#39;/g, "'")
		.replace(/&#34;/g, "\"");
}

/**
 * Replace URL and email references in the text with HTML links.
 */
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
			if (SDK.lang != null) {
				SDK.recognition.lang = SDK.lang;
			}
			SDK.recognition.continuous = true;
			SDK.recognition.onresult = function (event) {
				for (var i = event.resultIndex; i < event.results.length; ++i) {
					if (event.results[i].isFinal) {
						SDK.insertAtCaret(input, event.results[i][0].transcript);				
					}
				}
				if (button != null && button.click != null) {
					button.click();
				} else if (button != null) {
					button();
				}
			};
		} else {
			return;
		}
	}
}

SDK.startSpeechRecognition = function() {
	if (SDK.recognition != null) {
		if (SDK.lang != null) {
			SDK.recognition.lang = SDK.lang;
		}
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
	var wleft = (screen.width)-w-100;
	var wtop = (screen.height)-h-200;
	window.open(url, title, 'scrollbars=yes, resizable=yes, toolbar=no, location=no, directories=no, status=no, menubar=no, copyhistory=no, width='+w+', height='+h+', top='+wtop+', left='+wleft);
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

SDK.uploadImage = function(fileInput, url, width, height, properties, onFinish) {
	if (window.File && window.FileReader && window.FileList && window.Blob) {
		// Copy over form properties.
		if (properties == null) {
			properties = {};
		}
		if (fileInput.form != null) {
			var data = new FormData(fileInput.form);
			for (var [key, value] of data.entries()) {
				if (key != 'file') {
					if (properties[key] == null) {
						properties[key] = value;
					}
				}
			}
		}
		var files = fileInput.files;
		for (var i = 0; i < files.length; i++) {
			SDK.resizeAndUploadImage(files[i], url, width, height, properties, ((i == (files.length - 1) ? onFinish : null)))
		}
		return false;
	} else {
		alert('The File APIs are not fully supported in this browser.');
		return false;
	}
}
			
SDK.resizeAndUploadImage = function(file, url, width, height, properties, onFinish) {
	var reader = new FileReader();
	reader.onloadend = function() {
		var tempImg = new Image();
		tempImg.src = reader.result;
		tempImg.onload = function() {
			var MAX_WIDTH = width;
			var MAX_HEIGHT = height;
			if (width == null) {
				MAX_WIDTH = tempImg.width;
			}
			if (height == null) {
				MAX_HEIGHT = tempImg.height;
			}
			var tempW = tempImg.width;
			var tempH = tempImg.height;
			if (tempW > MAX_WIDTH) {
				 tempH *= MAX_WIDTH / tempW;
				 tempW = MAX_WIDTH;
			}
			if (tempH > MAX_HEIGHT) {
				 tempW *= MAX_HEIGHT / tempH;
				 tempH = MAX_HEIGHT;
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
			var formData = new FormData();
			if (properties != null) {
				for (property in properties) {
					formData.append(property, properties[property]);
				}
			}
			formData.append('file', blob, file.name);
			var request = new XMLHttpRequest();
			request.onreadystatechange = function() {
				if (request.readyState != 4) {
					return;
				}
				if (onFinish != null) {
					onFinish();
				}
			}
			request.open("POST", url);
			request.send(formData);
		}
 
	 }
	 reader.readAsDataURL(file);
}


/**
 * Open a JQuery error message dialog.
 */
SDK.showError = function(message, title) {
	if (title == null) {
		title = "Error";
	}
	$("<div></div>").html(message).dialog({
		title: title,
		resizable: false,
		modal: true,
		buttons: [
			{
				text: "OK",
				click: function() {
					$(this).dialog("close");
				},
				class: "okbutton"
			}
		]
	});
}

/**
 * Open a JQuery confirm dialog.
 */
SDK.showConfirm = function(message, title, onYes, onNo) {
	if (title == null) {
		title = "Confirm";
	}
	$("<div></div>").html(message).dialog({
		title: title,
		resizable: false,
		modal: true,
		buttons: {
			"Yes": function() {
				onYes();
				$(this).dialog("close");
			},
			"No": function() {
				onNo();
				$(this).dialog("close");
			}
		}
	});
}

/**
 * Evaluate any script tags in the node's descendants.
 * This is required when innerHtml contains script nodes as they are not evaluated.
 */
SDK.evalScripts = function(node) {
	if (node.tagName == 'SCRIPT') {
		var script  = document.createElement("script");
		script.text = node.innerHTML;
		for (var index = node.attributes.length-1; index >= 0; i--) {
			script.setAttribute(node.attributes[index].name, node.attributes[index].value);
		}
		node.parentNode.replaceChild(script, node);
	} else {
		var index = 0;
		var children = node.childNodes;
		while (index < children.length) {
			SDK.evalScripts(children[index]);
			index++;
		}
	}
	return node;
}

/**
 * Remove any script tags from the node.
 */
SDK.removeScripts = function(node) {
	if (node.tagName == 'SCRIPT') {
		node.parentNode.removeChild(node);
	} else {
		var index = 0;
		var children = node.childNodes;
		while (index < children.length) {
			SDK.removeScripts(children[index]);
			index++;
		}
	}
	return node;
}

/**
 * Add a stylesheet link to the page.
 */
SDK.addStylesheet = function(fileName) {
  var head = document.head;
  var link = document.createElement('link');
  link.type = 'text/css';
  link.rel = 'stylesheet';
  link.href = fileName;
  head.appendChild(link);
}

/**
 * Add a style tag to the page.
 */
SDK.addStyle = function(css) {
	var style = document.createElement('style');
	style.type = 'text/css';
	if (style.styleSheet) {
		style.styleSheet.cssText = css;
	} else {
		style.appendChild(document.createTextNode(css));
	}
	SDK.body().appendChild(style);
}

/**
 * Graphics upload dialog and shared repositry browser.
 * This provides a generic media upload dialog with many features:
 * <ul>
 *	 <li>Upload dialog UI
 *	 <li>Locally resize images before upload
 *	 <li>Upload from a web URL
 *	 <li>Upload a media file from a shared graphics repository
 * </ul>
 * @class
 */
function GraphicsUploader() {
	this.id = "graphics-browser";
	this.title = "Media Browser";
	this.browserClass = "dialog";
	this.dialogId = "graphics-uploader";
	this.dialogTitle = "Upload Media";
	this.dialogClass = "dialog";
	this.uploadURL = "upload-media";
	this.uploadFormProperties;
	this.reloadOnSubmit = true;
	this.fileInput;
	this.urlInput;
	this.prefix = "uploader-";
	this.renderedDialog = false;
	this.submit = true;
	this.showFile = true;
	this.showURL = true;
	this.showBrowse = true;
	this.sdk = null;
	this.url;
	this.multiple = false;
	/**
	 * Open JQyery upload dialog.
	 */
	this.openUploadDialog = function() {
		if (!this.renderedDialog) {
			this.renderUploadDialog();
		}
		$( '#' + this.dialogId ).dialog("open");
	}
	
	/**
	 * Open JQyery browser dialog.
	 */
	this.openBrowser = function() {
		var browser = document.getElementById(this.id);
		if (browser != null) {
			$( '#' + this.id ).remove();
		}
		this.renderBrowser();
		$( '#' + this.id ).dialog("open");
		this.fetchMedia();
	}

	/**
	 * Render JQyery upload dialog.
	 */
	this.renderUploadDialog = function() {
		var uploadDialog = document.createElement('div');
		uploadDialog.setAttribute('id', this.dialogId);
		uploadDialog.setAttribute('title', this.dialogTitle);
		uploadDialog.setAttribute('class', this.dialogClass);
		uploadDialog.style.display = "none";
		var html =
				"<style>\n"
				+ "." + this.prefix + "button { text-decoration:none; padding: 12px 2px 12px 2px; }\n"
				+ "." + this.prefix + "dialog-div { margin-top: 10px;margin-bottom: 10px; }\n"
				+ "</style>\n";
		if (this.showBrowse) {
			html = html
				+ "<div class='" + this.prefix + "dialog-div'>\n"
					+ "<a id='" + this.prefix + "browse-library' onclick='return false;' href='#' class='" + this.prefix + "button' title='Browse our shared media library'>\n"
					+ "<img src='images/importr.svg' style='width:40px;vertical-align: middle'>\n"
					+ "Browse media library\n"
					+ "</a>\n"
				+ "</div>\n";
		}
		if (this.showFile) {
			if (this.showBrowse) {
				html = html + "<hr>\n";
			}
			var multipleStr = "";
			if (this.multiple) {
				multipleStr = "multiple='multiple'";
			}
			html = html
				+ "<div class='" + this.prefix + "dialog-div'>\n"
					+ "<a id='" + this.prefix + "upload-media' onclick='return false;' href='#' class='" + this.prefix + "button' title='Upload an image or media file from your computer or device'>\n"
					+ "<img src='images/upload.svg' style='width:40px;vertical-align: middle'>\n"
					+ "Upload from computer or device</a>\n"
				+ "</div>\n"
				+ "<div class='" + this.prefix + "dialog-div'>\n"
					+ "<input id='" + this.prefix + "file-input' " + multipleStr + " style='display:none' type='file' name='file' style='display:none'/>\n"
					+ "<input id='" + this.prefix + "resize' type='checkbox' title='Resize the image file locally to the max pixel width, to srink large images, and save upload bandwidth (only use on image files)'>\n"
					+ "Resize to <input id='" + this.prefix + "resize-width' type='number' value='600' style='width:60px;height:25px' title='Image resize width in pixels'> pixels\n"
				+ "</div>\n";
		}
		if (this.showURL) {
			if (this.showFile || this.showBrowse) {
				html = html + "<hr>\n";
			}
			html = html
				+ "<div class='" + this.prefix + "dialog-div'>\n"
					+ "<a id='" + this.prefix + "upload-url' onclick='return false;' href='#' class='" + this.prefix + "button' title='Import an image or media file from the web URL'>\n"
					+ "<img src='images/importr.svg' style='width:40px;vertical-align: middle'>\n"
					+ "Import from web URL\n"
					+ "</a>\n"
				+ "</div>\n"
				+ "<input id='" + this.prefix + "url-input' type='text' style='width:100%'>\n";
		}
		uploadDialog.innerHTML = html;
		SDK.body().appendChild(uploadDialog);

		var self = this;
		var element = document.getElementById(this.prefix + "upload-media");
		if (element != null) {
			element.addEventListener("click", function(event) {
				if (document.getElementById(self.prefix + 'resize').checked) {
					document.getElementById(self.prefix + 'file-input').click();
				} else {
					self.fileInput.click();
				}
				return false;
			});
		}
		element = document.getElementById(this.prefix + "file-input");
		if (element != null) {
			element.addEventListener("change", function(event) {
				var width = parseInt(document.getElementById(self.prefix + 'resize-width').value);
				SDK.uploadImage(
						document.getElementById(self.prefix + 'file-input'),
						self.uploadURL,
						width,
						null,
						self.uploadFormProperties,
						function() {
							if (self.reloadOnSubmit) {
								location.reload();
							}
						});
				return false;
			});
		}
		element = document.getElementById(this.prefix + "upload-url");
		if (element != null) {
			element.addEventListener("click", function(event) {
				self.urlInput.value = document.getElementById(self.prefix + "url-input").value;
				if (self.submit) {
					self.urlInput.form.submit();
				}
				return false;
			});
		}
		element = document.getElementById(this.prefix + "browse-library");
		if (element != null) {
			element.addEventListener("click", function(event) {
				self.openBrowser(function(url) {
					if (url == null) {
						return false;
					}
					self.urlInput.value = url;
					if (self.submit) {
						self.urlInput.form.submit();
					}
				});
				return false;
			});
		}

		$( '#' + this.dialogId ).dialog({
			autoOpen: false,
			modal: true,
			buttons: {
				"Cancel": function() {
					$(this).dialog("close");
				}
			}
		});
		this.renderedDialog = true;
	}
	
	/**
	 * Render JQyery browser dialog.
	 */
	this.renderBrowser = function() {
		var browser = document.createElement('div');
		browser.setAttribute('id', this.id);
		browser.setAttribute('title', this.title);
		browser.setAttribute('class', this.browserClass);
		browser.style.display = "none";

		var self = this;
		GraphicsUploader.updateSearch = function() {
			self.fetchMedia();
		}
		var height = window.innerHeight - (window.innerHeight * 0.2);
		var width = window.innerWidth - (window.innerWidth * 0.2);
		var html =
				"<style>\n"
				+ "." + this.prefix + "button { text-decoration:none; padding: 12px 2px 12px 2px; }\n"
				+ "." + this.prefix + "browser-div { }\n"
				+ "." + this.prefix + "search-div { width:264px;margin:2px;display:inline-block;font-size:13px; }\n"
				+ "." + this.prefix + "search-span { display:inline-block;width:78px; }\n"
				+ "." + this.prefix + "browse-categories, ." + this.prefix + "browse-tags, , ." + this.prefix + "browse-filter { width:150px; }\n"
				+ "." + this.prefix + "browse-sort { width:150px; }\n"
				+ "." + this.prefix + "browse-div { display:inline-block;margin:2px;vertical-align:top; }\n"
				+ "." + this.prefix + "browse-details { font-size:12px;color:grey; }\n"
				+ "." + this.prefix + "browse-img { max-width:100px;max-height:100px; }\n"
				+ "." + this.prefix + "browse-span div { position:absolute;margin:-1px 0 0 0;padding:3px 3px 3px 3px;background:#fff;border-style:solid;border-color:black;border-width:1px;max-width:300px;min-width:100px;z-index:152;visibility:hidden;opacity:0;transition:visibility 0s linear 0.3s, opacity 0.3s linear; } \n"
				+ "." + this.prefix + "browse-span:hover div { display:inline;visibility:visible;opacity:1;transition-delay:0.5s; }\n"
				+ "</style>\n"
				+ "<div><div class='" + this.prefix + "search-div'><span class='" + this.prefix + "search-span'>Categories</span><input id='" + this.prefix + "browse-categories' type='text'/></div>"
				+ " <div class='" + this.prefix + "search-div'><span class='" + this.prefix + "search-span'>Tags</span><input id='" + this.prefix + "browse-tags' type='text'/></div>"
				+ " <div class='" + this.prefix + "search-div'><span class='" + this.prefix + "search-span'>Filter</span><input id='" + this.prefix + "browse-filter' type='text'/></div>"
				+ " <div class='" + this.prefix + "search-div'><span class='" + this.prefix + "search-span'>Sort</span><select id='" + this.prefix + "browse-sort' onchange='GraphicsUploader.updateSearch()'><option value='name'>name</option><option value='Date'>date</option><option value='thumbs up'>thumbs up</option>\n"
				+ "<option value='thumbs down'>thumbs down</option><option value='Stars'>stars</option><option value='connects'>connects</option></select>\n"
				+ "<a href='#' onclick='GraphicsUploader.updateSearch()' title='Search'><img src='images/inspect.svg' style='width:40px;vertical-align: middle'></a></div>\n"
				+ "</div>\n"
				+ "<div id='" + this.prefix + "browser-div' class='" + this.prefix + "browser-div'>\n"
				+ "</div>\n";
		browser.innerHTML = html;
		SDK.body().appendChild(browser);

		if (this.sdk == null) {
			this.sdk = new SDKConnection();
		}
		var autocompleteEvent = function(event) {
			var self = this;
			$(self).autocomplete('search', '');
		}
		if (GraphicsUploader.tags.length == 0) {
			var contentConfig = new ContentConfig();
			contentConfig.type = "Graphic";
			this.sdk.fetchTags(contentConfig, function(results) {
				GraphicsUploader.tags = results;
				$( "#" + self.prefix + "browse-tags" ).autocomplete({ source: GraphicsUploader.tags, minLength: 0, appendTo: $("#" + self.prefix + "browse-tags").parent() }).on('focus', autocompleteEvent);
			});
		} else {
			$( "#" + this.prefix + "browse-tags" ).autocomplete({ source: GraphicsUploader.tags, minLength: 0, appendTo: $("#" + self.prefix + "browse-tags").parent() }).on('focus', autocompleteEvent);
		}
		if (GraphicsUploader.categories.length == 0) {
			var contentConfig = new ContentConfig();
			contentConfig.type = "Graphic";
			this.sdk.fetchCategories(contentConfig, function(results) {
				GraphicsUploader.categories = results;
				$( "#" + self.prefix + "browse-categories" ).autocomplete({ source: GraphicsUploader.categories, minLength: 0, appendTo: $("#" + self.prefix + "browse-categories").parent() }).on('focus', autocompleteEvent);
			});
		} else {
			$( "#" + this.prefix + "browse-categories" ).autocomplete({ source: GraphicsUploader.categories, minLength: 0, appendTo: $("#" + self.prefix + "browse-categories").parent() }).on('focus', autocompleteEvent);
		}
		var keyPressed = function search(e) {
			if (e.keyCode == 13) {
				self.fetchMedia();
			}
		}
		$( "#" + this.prefix + "browse-tags" ).on("keydown", keyPressed);
		$( "#" + this.prefix + "browse-categories" ).on("keydown", keyPressed);
		$( "#" + this.prefix + "browse-filter" ).on("keydown", keyPressed);
		
		$( '#' + this.id ).dialog({
			autoOpen: false,
			modal: true,
			height: height,
			width: width,
			buttons: {
				"Cancel": function() {
					$(this).dialog("close");
				}
			}
		});
	}
	
	/**
	 * Query and display graphics.
	 */
	this.fetchMedia = function() {
		var browseConfig = new BrowseConfig();
		browseConfig.type = "Graphic";
		browseConfig.category = document.getElementById(this.prefix + 'browse-categories').value;
		browseConfig.tag = document.getElementById(this.prefix + 'browse-tags').value;
		browseConfig.filter = document.getElementById(this.prefix + 'browse-filter').value;
		browseConfig.sort = document.getElementById(this.prefix + 'browse-sort').value;
		var self = this;
		var urlprefix = self.sdk.credentials.url + "/";
		GraphicsUploader.chooseMedia = function(id) {
			var config = new GraphicConfig();
			config.id = id;
			self.sdk.fetch(config, function(result) {
				self.url = urlprefix + result.media;
				if (self.urlInput != null) {
					self.urlInput.value = self.url;
					if (self.submit) {
						self.urlInput.form.submit();
					}
				}
			});
		}
		this.sdk.browse(browseConfig, function(results) {
			var div = document.getElementById(self.prefix + "browser-div");
			while (div.firstChild) {
				div.removeChild(div.firstChild);
			}
			for (var index = 0; index < results.length; index++) {
				var result = results[index];
				var graphicDiv = document.createElement('div');
				graphicDiv.setAttribute('id', self.prefix + 'browse-div');
				graphicDiv.setAttribute('class', self.prefix + 'browse-div');
				var html =
					"<span id='" + self.prefix + "browse-span' class='" + self.prefix + "browse-span'>"
					+ "<table style='border-style:solid;border-color:grey;border-width:1px'><tr><td style='height:100px;width:100px;' align='center' valign='middle'>"
					+ "<a href='#' onclick='GraphicsUploader.chooseMedia(" + result.id + ")'><img id='" + self.prefix + "browse-img' class='" + self.prefix + "browse-img' src='" + urlprefix + result.avatar + "'></a>\n"
					+ "</td></tr></table>"
					+ "<div>"
					+ "<span><b>" + result.name + "</b><br/>" + result.description + "</span><br/>"
					+ "<span id='" + self.prefix + "browse-details' class='" + self.prefix + "browse-details'>";
				if (result.categories != null && result.categories != "") {
					html = html + "Categories: " + result.categories + "<br/>";
				}
				if (result.tags != null && result.tags != "") {
					html = html + "Tags: " + result.tags + "<br/>";
				}
				if (result.license != null && result.license != "") {
					html = html + "License: " + result.license + "<br/>";
				}
				html = html
					+ "</div>"
					+ "</span>\n"
					+ "<div style='max-width:100px'><a href='#' style='text-decoration:none;' onclick='GraphicsUploader.chooseMedia(" + result.id + ")'><span id='" + self.prefix + "browse-details' class='" + self.prefix + "browse-details'>" + result.name + "</span></div></a>\n";
				graphicDiv.innerHTML = html;
				var img = document.createElement('img');
				img.setAttribute('src', urlprefix + result.avatar);
				div.appendChild(graphicDiv);
			}
		});
	}
}

GraphicsUploader.map = {};

GraphicsUploader.tags = [];
GraphicsUploader.categories = [];

/**
 * Open a media uploader dialog initialized with a form.
 * The dialog will use the form's action to upload media as 'file' for a file, or 'upload-url' for a URL.
 * The form should define an ID if to be used on multiple forms in the same document.
 * This can be used on the onclick on an input element to open the dialog i.e. <input type="submit" onclick="return GraphicsUploader.openUploadDialog(this.form)" value="Upload">
 * This will create a hidden input of type file ('file'), and a hidden input of type text ('upload-url'), these will be passed to your server when the dialog submits the form.
 */
GraphicsUploader.openUploadDialog = function(form, title, showFile, showURL, showBrowse, multiple) {
	var id = form.getAttribute('id');
	var prefix = "uploader-";
	var dialogId = "graphics-uploader";
	var browserId = "graphics-browser";
	if (id == null) {
		id = "uploader";
	} else {
		prefix = id + '-' + prefix;
		dialogId = id + '-' + dialogId;
		browserId = id + '-' + browserId;
	}
	var uploader = GraphicsUploader.map[id];
	if (uploader == null) {
		uploader = new GraphicsUploader();
		uploader.multiple = multiple;
		var multipleStr = "";
		if (multiple) {
			multipleStr = "multiple='multiple'";
		}
		GraphicsUploader.map[id] = uploader;
		var div = document.createElement('div');
		var html =
			"<input id='" + id + "file-input' style='display:none' " + multipleStr + " onchange='this.form.submit()' type='file' name='file'/>\n"
			+ "<input id='" + id + "url-input' style='display:none' name='upload-url' type='text'>";
		div.innerHTML = html;
		form.appendChild(div);
		if (title != null) {
			uploader.dialogTitle = title;
		}
		if (showFile != null) {
			uploader.showFile = showFile;
		}
		if (showURL != null) {
			uploader.showURL = showURL;
		}
		if (showBrowse != null) {
			uploader.showBrowse = showBrowse;
		}
		uploader.prefix = prefix;
		uploader.dialogId = dialogId;
		uploader.id= browserId;
		uploader.fileInput = document.getElementById(id + 'file-input');
		uploader.urlInput = document.getElementById(id + 'url-input');
		uploader.uploadURL = form.action;
		// Copy over form properties.
		uploader.uploadFormProperties = {};
		var data = new FormData(form);
		for (var [key, value] of data.entries()) { 
			uploader.uploadFormProperties[key] = value;
		}
	}
	uploader.openUploadDialog();
	return false;
}

/**
 * Credentials used to establish a connection.
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
 * Credentials for use with hosted services on the Bot Libre website, a free bot hosting service.
 * https://www.botlibre.com
 * @class
 */
function BOTlibreCredentials()  {
	this.DOMAIN = "www.botlibre.com";
	this.APP = "";
	this.PATH = "/rest/api";
	
	this.host = this.DOMAIN;
	this.app = this.APP;
	this.url = "https://" + this.DOMAIN + this.APP;
	this.rest = this.url + this.PATH;
	this.applicationId = SDK.applicationId;
}

/**
 * Credentials for use with hosted services on the Bot Libre for Business website,
 * a commercial bot, live chat, chatroom, and forum, hosting service.
 * https://www.botlibre.biz
 * @class
 */
function BotLibreBizCredentials()  {
	this.DOMAIN = "www.botlibre.biz";
	this.APP = "";
	this.PATH = "/rest/api";
	
	this.host = this.DOMAIN;
	this.app = this.APP;
	this.url = "https://" + this.DOMAIN + this.APP;
	this.rest = this.url + this.PATH;
	this.applicationId = SDK.applicationId;
}

/**
 * Credentials for use with hosted services on the Bot Libre for Business website,
 * a commercial bot, live chat, chatroom, and forum, hosting service.
 * https://www.botlibre.biz
 * @class
 */
function PaphusCredentials()  {
	this.DOMAIN = "www.botlibre.biz";
	this.APP = "";
	this.PATH = "/rest/api";
	
	this.host = this.DOMAIN;
	this.app = this.APP;
	this.url = "https://" + this.DOMAIN + this.APP;
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
	this.PATH = "/rest/api";
	
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
	this.PATH = "/rest/api";
	
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
	/** Set the caption for the button bar button. */
	this.caption = null;
	this.switchText = true;
	this.playChime = true;
	/** The name of the voice to use. */
	this.voice = null;
	/** The name of the voice mod to use. */
	this.voiceMod = null;
	/** Enable or disable speech. */
	this.speak = false;
	/** Configure if the browser's native voice TTS should be used. */
	this.nativeVoice = false;
	/** Set the voice name for the native voice. */
	this.nativeVoiceName = null;
	/** Set the language for the native voice. */
	this.lang = null;
	/** Enable or disable avatar (you must also set the avatar ID). */
	this.avatar = false;
	/** Set the avatar. */
	this.avatarId = null;
	/** Set if the avatar should request HD (high def) video/images. */
	this.hd = null;
	/** Set if the avatar should request a specific video or image format. */
	this.format = null;
	this.lang = null;
	this.nick = "";
	this.connection = null;
	this.sdk = null;
	/** Configure if chat should be given focus after message. */
	this.focus = !SDK.isMobile();
	/** Element id and class prefix. Can be used to have multiple avatars in the same page, or avoid naming collisions. */
	this.prefix = "";
	/** Allow the chat box button color to be set. */
	this.color = "#009900";
	/** Allow the user to modify style sheets. */
	this.version = null;
	/** Allow the chat box hover button color to be set. */
	this.hoverColor = "grey";
	/** Allow the chat box background color to be set. */
	this.background = null;
	/** Chat box width. */
	this.width = 300;
	/** Chat box height. */
	this.height = 320;
	/** Chat box offset from side. */
	this.offset = 30;
	/** Chat Button Vertial Offset*/
	this.verticalOffset = 0;
	/** Print response in chat bubble. */
	this.bubble = false;
	/** Set the location of the button and box, one of "bottom-right", "bottom-left", "top-right", "top-left". */
	this.boxLocation = "bottom-right";
	/** Set styles explicitly to avoid inheriting page styles. Disable this to be able to override styles. */
	this.forceStyles = true;
	/** Override the URL used in the chat box popup. */
	this.popupURL = null;
	/** Set if the box chat log should be shown. */
	this.chatLog = true;
	/** Box chat loading message to display. */
	this.loading = "loading...";
	/** Box chat show online users option. */
	this.online = false;
	/** Link to online user list users to their profile page. */
	this.linkUsers = true;
	/** Configure message log format (table or log). */
	this.chatLogType = "log";
	/** Configure the chat to auto accept a bot after waiting */
	this.autoAccept = null;
	/** Prompt for name/email before connecting. */
	this.promptContactInfo = false;
	this.hasContactInfo = false;
	/** Set if the back link should be displayed. */
	this.backlink = SDK.backlink;
	this.contactName = null;
	this.contactEmail = null;
	this.contactPhone = null;
	this.contactInfo = "";
	/** Allow the close button on the box button bar to be removed, and maximize on any click to the button bar. */
	this.showClose = true;
	/** Provide an email chat log menu item. */
	this.emailChatLog = false;
	/** Ask the user if they want an email of the chat log on close. */
	this.promptEmailChatLog = false;
	this.windowTitle = document.title;
	this.isActive = true;
	/** Variables used to get the user and bot images. */
	this.botThumb = {};
	this.userThumb = {};
	self = this;
	/** JSON object of all currently logged in users in the chat room take from updateUsersXML. */
	this.users = {};
	/** Box chat chat room option. */
	this.chatroom = false;
	/** Show and hides menu bar */
	this.showMenubar = true;
	/** Show Box Max */
	this.showBoxmax = true;
	/** Show Send Image */
	this.showSendImage = true;
	
	/**
	 * Create an embedding bar and div in the current web page.
	 */
	this.createBox = function() {
		if (this.prefix == "" && this.elementPrefix != null) {
			this.prefix = this.elementPrefix;
		}
		if (this.caption == null) {
			this.caption = this.instanceName;
		}
		var backgroundstyle = "";
		var backgroundstyle2 = "";
		var buttonstyle = "";
		var buttonHoverStyle = "";
		var hidden = "hidden";
		var border = "";
		if (this.backgroundIfNotChrome && (SDK.isChrome() || SDK.isFirefox())) {
			this.background = null;
		}
		if (this.background != null) {
			backgroundstyle = " style='background-color:" + this.background + "'";
			hidden = "visible";
			border = "border:1px;border-style:solid;border-color:black;";
		} else {
			border = "border:1px;border-style:solid;border-color:transparent;";
		}
		if (this.color != null) {
			buttonstyle = "background-color:" + this.color + ";";
		}
		if (this.hoverColor != null) {
			buttonHoverStyle = "background-color:" + this.hoverColor + ";";
		}
		
		var minWidth = "";
		var divWidth = "";
		var background = "";
		var minHeight = "";
		var divHeight = "";
		var maxDivWidth = "";
		var maxHeight = null;
		var responseWidth = "";
		var chatWidth = "";
		var hideAvatar = "";
		var avatarWidth = this.width;
		var minAvatarWidth = "";
		var scrollerHeight = this.height;
		var scrollerMinHeight = "";
		if (this.width != null) {
			if (typeof this.width === "string") {
				this.width = parseInt(this.width);
			}
			// Auto correct for a short window or screen (assuming square).
			// 250 is total box height minus avatar.
			if ((this.width + 280) > window.innerHeight) {
				avatarWidth = window.innerHeight - 280;
				if (avatarWidth < 100) {
					hideAvatar = "display:none";
				}
			}
			minWidth = "width:" + this.width + "px;";
			minAvatarWidth = "width:" + avatarWidth + "px;";
			background = "background-size:" + avatarWidth + "px auto;";
			divWidth = minWidth;
			divHeight = "min-height:" + avatarWidth + "px;";
			responseWidth = "width:" + (this.width - 32) + "px;";
			maxDivWidth = "max-width:" + (this.width - 50) + "px;";
			scrollerHeight = avatarWidth;
		}
		if (this.height != null) {
			if (typeof this.height === "string") {
				this.height = parseInt(this.height);
			}
			minHeight = "height:" + this.height + "px;";
			divHeight = minHeight;
			if (this.width != null) {
				background = "background-size:" + this.width + "px " + this.height + "px;";
			} else {
				background = "background-size: auto " + this.height + "px;";
				divWidth = "min-width:" + this.height + "px;";
				responseWidth = "width:" + (this.height - 16) + "px;";
				chatWidth = "width:" + this.height + "px;";
			}
		} else {
			scrollerMinHeight = "height:" + scrollerHeight + "px;";
		}
		
		var boxloc = "bottom:10px;right:10px";
		if (this.boxLocation == "top-left") {
			boxloc = "top:10px;left:10px";
		} else if (this.boxLocation == "top-right") {
			boxloc = "top:10px;right:10px";
		} else if (this.boxLocation == "bottom-left") {
			boxloc = "bottom:10px;left:10px";
		} else if (this.boxLocation == "bottom-right") {
			boxloc = "bottom:10px;right:10px";
		}
		var locationBottom = 20;
		if (this.version < 6.0 || this.prefix != "botplatformchat") {
			locationBottom = 2;
		}
		var boxbarloc = "bottom:" + (locationBottom + this.verticalOffset) + "px;right:" + this.offset + "px";
		if (this.boxLocation == "top-left") {
			boxbarloc = "top:" + (locationBottom + this.verticalOffset) + "px;left:" + this.offset + "px";
		} else if (this.boxLocation == "top-right") {
			boxbarloc = "top:" + (locationBottom + this.verticalOffset) + "px;right:" + this.offset + "px";
		} else if (this.boxLocation == "bottom-left") {
			boxbarloc = "bottom:" + (locationBottom + this.verticalOffset) + "px;left:" + this.offset + "px";
		} else if (this.boxLocation == "bottom-right") {
			boxbarloc = "bottom:" + (locationBottom + this.verticalOffset) + "px;right:" + this.offset + "px";
		}
		var box = document.createElement('div');
		var html =
			"<style>\n"
				+ "." + this.prefix + "box { position:fixed;" + boxloc + ";z-index:152;margin:2px;display:none;" + border + " }\n"
				+ "." + this.prefix + "boxmenu { visibility:" + hidden + "; }\n" //margin-bottom:12px;
				+ (this.forceStyles ? "#" : ".") + "" + this.prefix + "boxbarmax { font-size:18px;margin:2px;padding:0px;text-decoration:none; }\n"
				+ "." + this.prefix + "boxbar { position:fixed;" + boxbarloc + ";z-index:152;margin:0;padding:6px;" + buttonstyle + " }\n"
				+ "." + this.prefix + "boxbar:hover { " + buttonHoverStyle + " }\n"
				+ "#" + this.prefix + "emailchatlogdialog { " + minWidth + " }\n"
				+ "#" + this.prefix + "contactinfo { " + minHeight + minWidth + " }\n"
				+ "." + this.prefix + "contactconnect { margin:4px;padding:8px;color:white;text-decoration:none;" + buttonstyle + " }\n"
				+ "." + this.prefix + "no-bubble-text { " + responseWidth + "; max-height:100px; overflow:auto; }\n"
				+ "." + this.prefix + "bubble-text { " + responseWidth + "; margin:4px; max-height:100px; overflow:auto; }\n"
				+ (this.forceStyles ? "#" : ".") + this.prefix + "chat { width:99%;min-height:22px; }\n"
				+ "." + this.prefix + "chat-1-div { " + maxDivWidth + "}\n"
				+ "." + this.prefix + "chat-2-div { " + maxDivWidth + "}\n"
				+ "." + this.prefix + "online-div { " + minWidth + " overflow-x: auto; overflow-y: hidden; white-space: nowrap; }\n"
				+ "." + this.prefix + "scroller { overflow-x:hidden;" + minHeight + minWidth + " }\n"
				+ "." + this.prefix + "box:hover ." + this.prefix + "boxmenu { visibility:visible; }\n";
		if (this.version < 6.0 || this.prefix != "botplatformchat") {
			html = html
						+ "." + this.prefix + "box img { display:inline; }\n"
						+ "." + this.prefix + "boxbar img { display:inline; }\n"
						+ "." + this.prefix + "box:hover { border:1px;border-style:solid;border-color:black; }\n"
						+ "." + this.prefix + "box:hover ." + this.prefix + "boxmenu { visibility:visible; }\n"
						+ "." + this.prefix + "boxclose, ." + this.prefix + "boxmin, ." + this.prefix + "boxmax { font-size:22px;margin:2px;padding:0px;text-decoration:none; }\n"
						+ "." + this.prefix + "boxclose:hover, ." + this.prefix + "boxmin:hover, ." + this.prefix + "boxmax:hover { color: #fff;background: grey; }\n"
						+ "#" + this.prefix + "emailchatlogdialog span { margin-left:0px;margin-top:4px; }\n"
						+ "#" + this.prefix + "emailchatlogdialog input { margin:4px;font-size:13px;height:33px;width:90%;border:1px solid #d5d5d5; }\n"
						+ "." + this.prefix + "emailconfirm { margin:4px;padding:8px;color:white;background-color:grey;text-decoration:none; }\n"
						+ "#" + this.prefix + "contactinfo span { margin-left:4px;margin-top:4px; }\n"
						+ "#" + this.prefix + "contactinfo input { margin:4px;font-size:13px;height:33px;width:90%;border:1px solid #d5d5d5; }\n"
						+ "." + this.prefix + "no-bubble { margin:4px; padding:8px; border:1px; border-style:solid; border-color:black; background-color:white; color:black; }\n"
						+ "." + this.prefix + "boxbutton { width:20px;height:20px;margin:2px; }\n"
						+ "." + this.prefix + "bubble-div { padding-bottom:15px;position:relative; }\n"
						+ "." + this.prefix + "no-bubble-plain { margin:4px; padding:8px; border:1px; }\n"
						+ "." + this.prefix + "bubble { margin:4px; padding:8px; border:1px; border-style:solid; border-color:black; border-radius:10px; background-color:white; color:black; }\n"
						+ "." + this.prefix + "bubble:before { content:''; position:absolute; bottom:0px; left:40px; border-width:20px 0 0 20px; border-style:solid; border-color:black transparent; display:block; width:0;}\n"
						+ "." + this.prefix + "bubble:after { content:''; position:absolute; bottom:3px; left:42px; border-width:18px 0 0 16px; border-style:solid; border-color:white transparent; display:block; width:0;}\n"
						+ "." + this.prefix + "box-input-span { display:block; overflow:hidden; margin:4px; padding-right:4px; }\n"
						+ "a." + this.prefix + "menuitem { text-decoration: none;display: block;color: #585858; }\n"
						+ "a." + this.prefix + "menuitem:hover { color: #fff;background: grey; }\n"
						+ "tr." + this.prefix + "menuitem:hover { background: grey; }\n"
						+ "." + this.prefix + "powered { margin:4px;font-size:10px; }\n"
						+ "img." + this.prefix + "menu { width: 24px;height: 24px;margin: 2px;cursor: pointer;vertical-align: middle; }\n"
						+ "span." + this.prefix + "menu { color: #818181;font-size: 12px; }\n"
						+ "img." + this.prefix + "toolbar { width: 25px;height: 25px;margin: 1px;padding: 1px;cursor: pointer;vertical-align: middle;border-style: solid;border-width: 1px;border-color: #fff; }\n"
						+ "td." + this.prefix + "toolbar { width: 36px;height: 36px }\n"
						/*+ "." + this.prefix + "online { height: 97px;width: 300px;overflow-x: auto;overflow-y: hidden;white-space: nowrap; }\n"*/
						+ "." + this.prefix + "menupopup div { position:absolute;margin: -1px 0 0 0;padding: 3px 3px 3px 3px;background: #fff;border-style:solid;border-color:black;border-width:1px;width:160px;max-width:300px;z-index:152;visibility:hidden;opacity:0;transition:visibility 0s linear 0.3s, opacity 0.3s linear; }\n"
						+ "." + this.prefix + "menupopup:hover div { display:inline;visibility:visible;opacity:1;transition-delay:0.5s; }\n"
						+ "img.chat-user-thumb { height: 50px; }\n"
						+ "a.user { text-decoration: none; }\n"
						+ "td." + this.prefix + "chat-1 { width:100%;background-color: #d5d5d5;}\n"
						+ "span." + this.prefix + "chat-1 { color:#333;}\n"
						+ "span." + this.prefix + "chat-user { color:grey;font-size:small; }\n"
						+ "." + this.prefix + "console { width:100%; }\n"
						+ "." + this.prefix + "online-div { display: none; }\n"
						+ "." + this.prefix + "-channel-title { display: none; }\n"
						+ "#" + this.prefix + "boxtable { background:none; border:none; margin:0; }\n"
						+ "#" + this.prefix + "boxbar3 { display:none; }\n"
						+ "#" + this.prefix + "boxbarmax { color: white; }\n"
						+ "img." + this.prefix + "chat-user { height:40px; max-width:40px; }\n";
		}
		html = html
			+ "</style>\n"
			+ "<div id='" + this.prefix + "box' class='" + this.prefix + "box' " + backgroundstyle + ">"
				+ "<div class='" + this.prefix + "boxmenu'>"
					+ (this.backlink ? "<span class='" + this.prefix + "powered'>powered by <a href='" + SDK.backlinkURL + "' target='_blank'>" + SDK.NAME + "</a></span>" : "")
					+ "<span class=" + this.prefix + "-channel-title>" + this.instanceName + "</span>"
					+ "<span style='float:right'><a id='" + this.prefix + "boxmin' class='" + this.prefix + "boxmin' onclick='return false;' href='#'><img src='" + SDK.url + "/images/minimize.png'></a>";
					if (this.showBoxmax) {
						html = html + "<a id='" + this.prefix + "boxmax' class='" + this.prefix + "boxmax' onclick='return false;' href='#'><img src='" + SDK.url + "/images/open.png'></a></span><br/>";
					} else {
						html = html + "</span><br/>";
					}	
				html = html + "</div>";
		if (this.online) {
			html = html
				+ "<div id='" + this.prefix + "online-div' class='" + this.prefix + "online-div'>"
					+ "<div id='" + this.prefix + "online' class='" + this.prefix + "online'" + "style='display:none;'>"
						+ "<table></table>"
					+ "</div>"
				+ "</div>";
		}
		if (this.chatLog) {
			html = html
				+ "<div id='" + this.prefix + "scroller' class='" + this.prefix + "scroller'>"
				+ "<table id='" + this.prefix + "console' class='" + this.prefix + "console' width=100% cellspacing=2></table>"
				+ "</div>"
		}
		if (this.avatar) {
			html = html
				+ "<div id='" + this.prefix + "avatar-div' style='" + hideAvatar + "'>"
					+ "<div id='" + this.prefix + "avatar-image-div' style='display:none;text-align:center;" + minHeight + minAvatarWidth + "'>"
						+ "<img id='" + this.prefix + "avatar' style='" + minHeight + minAvatarWidth + "'/>"
					+ "</div>"
					+ "<div id='" + this.prefix + "avatar-video-div' style='display:none;text-align:center;background-position:center;" + divHeight + divWidth + background + "background-repeat: no-repeat;'>"
						+ "<video muted='true' id='" + this.prefix + "avatar-video' autoplay preload='auto' style='background:transparent;" + minHeight + minAvatarWidth + "'>"
							+ "Video format not supported by your browser (try Chrome)"
						+ "</video>"
					+ "</div>"
					+ "<div id='" + this.prefix + "avatar-canvas-div' style='display:none;text-align:center;" + divHeight + divWidth + "'>"
						+ "<canvas id='" + this.prefix + "avatar-canvas' style='background:transparent;" + minHeight + minAvatarWidth + "'>"
							+ "Canvas not supported by your browser (try Chrome)"
						+ "</canvas>"
					+ "</div>"
					+ "<div id='" + this.prefix + "avatar-game-div' style='display:none;text-align:center;" + divHeight + divWidth + "'>"
					+ "</div>"
				+ "</div>";
		}
		var urlprefix = this.sdk.credentials.url + "/";
		html = html
				+ "<div>\n"
					+ "<div " + (this.bubble ? "id='" + this.prefix + "bubble-div' class='" + this.prefix + "bubble-div'" : "") + ">"
					+ "<div "
					+ "id='" + this.prefix + (this.bubble ? "bubble" : (this.background == null ? "no-bubble" : "no-bubble-plain") )
					+ "' class='" + this.prefix + (this.bubble ? "bubble" : (this.background == null ? "no-bubble" : "no-bubble-plain") )
					+ "'><div id='" + this.prefix + (this.bubble ? "bubble-text" : "no-bubble-text" ) + "' "
						+ "class='" + this.prefix + (this.bubble ? "bubble-text" : "no-bubble-text" ) + "'>"
						+ "<span id='" + this.prefix + "response'>" + this.loading + "</span><br/>"
					+ "</div></div></div>\n";
		html = html
			+ "<table id='" + this.prefix + "boxtable' class='" + this.prefix + "boxtable' style='width:100%'><tr>\n";
			if (this.showMenubar) {
				html = html
					+ "<td class='" + this.prefix + "toolbar'><span class='" + this.prefix + "menu'>\n"
					+ "<div style='inline-block;position:relative'>\n"
					+ "<span id='" + this.prefix + "menupopup' class='" + this.prefix + "menupopup'>\n"
					+ "<div style='text-align:left;bottom:28px'>\n"
					+ "<table>\n"
					+ "<tr class='" + this.prefix + "menuitem'>"
					+ "<td><a id='" + this.prefix + "ping' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img class='" + this.prefix + "menu' src='" + SDK.url + "/images/ping.svg' title='" + SDK.translate("Verify your connection to the server") + "'> " + SDK.translate("Ping server") + "</a></td>"
					+ "</tr>\n"
					+ "<tr class='" + this.prefix + "menuitem'>"
					+ "<td><a id='" + this.prefix + "toggleKeepAlive' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img id='boxkeepalive' class='" + this.prefix + "menu' src='" + SDK.url + "/images/empty.png' title='" + SDK.translate("Ping the server every minute to keep the connection alive") + "'> " + SDK.translate("Keep Alive") + "</a></td>"
					+ "</tr>\n";
				if (this.chatroom) {
					html = html
						+ "<tr class='" + this.prefix + "menuitem'>"
							+ "<td><a id='" + this.prefix + "flag' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img class='" + this.prefix + "menu' src='" + SDK.url + "/images/flag2.svg' title='" + SDK.translate("Flag a user for offensive content") + "'> " + SDK.translate("Flag user") + "</a></td>"
						+ "</tr>\n"
						+ "<tr class='" + this.prefix + "menuitem'>"
							+ "<td><a id='" + this.prefix + "whisper' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img class='" + this.prefix + "menu' src='" + SDK.url + "/images/whisper.png' title='" + SDK.translate("Send a private message to another user") + "'> " + SDK.translate("Whisper user") + "</a></td>"
						+ "</tr>\n"
						+ "<tr class='" + this.prefix + "menuitem'>"
							+ "<td><a id='" + this.prefix + "pvt' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img class='" + this.prefix + "menu' src='" + SDK.url + "/images/accept.svg' title='" + SDK.translate("Invite another user to a private channel") + "'> " + SDK.translate("Request private") + "</a></td>"
						+ "</tr>\n"
				}
				html = html
					+ "<tr class='" + this.prefix + "menuitem'>"
						+ "<td><a id='" + this.prefix + "clear' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img class='" + this.prefix + "menu' src='" + SDK.url + "/images/empty.png' title='" + SDK.translate("Clear the local chat log") + "'> " + SDK.translate("Clear log") + "</a></td>"
					+ "</tr>\n"
					+ "<tr class='" + this.prefix + "menuitem'>"
						+ "<td><a id='" + this.prefix + "accept' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img class='" + this.prefix + "menu' src='" + SDK.url + "/images/accept.svg' title='" + SDK.translate("Accept a private request from an operator, bot, or another user") + "'> " + SDK.translate("Accept private") + "</a></td>"
					+ "</tr>\n";
					if (this.showSendImage) {
						html = html
						+ "<tr class='" + this.prefix + "menuitem'>"
							+ "<td><a id='" + this.prefix + "sendImage' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img class='" + this.prefix + "menu' src='" + SDK.url + "/images/image.svg' title='" + SDK.translate("Resize and send an image attachment") + "'> " + SDK.translate("Send image") + "</a></td>"
						+ "</tr>\n"
						+ "<tr class='" + this.prefix + "menuitem'>"
							+ "<td><a id='" + this.prefix + "sendAttachment' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img class='" + this.prefix + "menu' src='" + SDK.url + "/images/attach.svg' title='" + SDK.translate("Send an image or file attachment") + "'> " + SDK.translate("Send file") + "</a></td>"
						+ "</tr>\n";
					}
				if (this.emailChatLog) {
					html = html
						+ "<tr class='" + this.prefix + "menuitem'>"
						+ "<td><a id='" + this.prefix + "emailChatLog' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img id='email' class='" + this.prefix + "menu' src='" + SDK.url + "/images/message.svg' title='" + SDK.translate("Send yourself an email of the conversation log") + "'> " + SDK.translate("Email Chat Log") + "</a></td>"
				}
				html = html
					+ "<tr id='" + this.prefix + "showChatLog' class='" + this.prefix + "menuitem'>"
						+ "<td><a class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img class='" + this.prefix + "menu' src='" + urlprefix + "/images/chat_log.svg' title='Chat log'> " + SDK.translate("Chat Log") + "</a></td>"
					+ "</tr>\n"
					+ "<tr id='" + this.prefix + "showAvatarBot' class='" + this.prefix + "menuitem' style='display:none;'>"
						+ "<td><a class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img class='" + this.prefix + "menu' src='" + urlprefix + "/images/avatar-icon.png' title='Avatar Bot'> " + SDK.translate("Show Avatar") + "</a></td>"
					+ "</tr>\n";
				html = html
					+ "<tr class='" + this.prefix + "menuitem'>"
						+ "<td><a id='" + this.prefix + "toggleChime' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img id='boxchime' class='" + this.prefix + "menu' src='" + SDK.url + "/images/sound.svg' title='" + SDK.translate("Play a chime when a message is recieved") + "'> " + SDK.translate("Chime") + "</a></td>"
					+ "</tr>\n"
					+ "<tr class='" + this.prefix + "menuitem'>"
						+ "<td><a id='" + this.prefix + "toggleSpeak' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img id='boxtalk' class='" + this.prefix + "menu' src='" + SDK.url + "/images/talkoff.svg' title='" + SDK.translate("Speak each message using voice synthesis") + "'> " + SDK.translate("Text to speech") + "</a></td>"
					+ "</tr>\n"
					+ "<tr class='" + this.prefix + "menuitem'>"
						+ "<td><a id='" + this.prefix + "toggleListen' class='" + this.prefix + "menuitem' onclick='return false;' href='#'>"
								+ "<img id='boxmic' class='" + this.prefix + "menu' src='" + SDK.url + "/images/micoff.svg' title='" + SDK.translate("Enable speech recognition (browser must support HTML5 speech recognition, such as Chrome)") + "'> " + SDK.translate("Speech recognition") + "</a>"
						+ "</td>"
					+ "</tr>\n"
					+ "<tr class='" + this.prefix + "menuitem'>"
						+ "<td><a id='" + this.prefix + "exit' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img class='" + this.prefix + "menu' src='" + SDK.url + "/images/quit.svg' title='" + SDK.translate("Exit the channel or active private channel") + "'> " + SDK.translate("Quit private or channel") + "</a></td>"
					+ "</tr>\n"
					+ "</table>\n"
					+ "</div>\n"
					+ "<img class='" + this.prefix + "toolbar' src='" + SDK.url + "/images/menu.png'>\n"
					+ "</span>\n"
					+ "</div>\n"
					+ "</span></td>\n";
			}
		html = html
			+ "<td><span class='" + this.prefix + "box-input-span'><input id='" + this.prefix
				+ "chat' type='text' id='" + this.prefix + "box-input' "
				+ "class='" + this.prefix + "box-input'/></span></td>"
			+ "</tr></table>"
			+ "</div>\n"
			+ "</div>\n"
			+ "<div id='" + this.prefix + "boxbar' class='" + this.prefix + "boxbar'>"
				+ "<div id='" + this.prefix + "boxbar2' class='" + this.prefix + "boxbar2'>"
					+ "<span><a id='" + this.prefix + "boxbarmax' class='" + this.prefix + "boxbarmax' " + " onclick='return false;' href='#'><img id='" + this.prefix + "boxbarmaximage' " + "src='" + SDK.url + "/images/maximizew.png'> " + this.caption + " </a>";
		if (this.showClose) {
			html = html + " <a id='" + this.prefix + "boxclose' class='" + this.prefix + "boxclose' onclick='return false;' href='#'> <img src='" + SDK.url + "/images/closeg.png'></a>";
		}
		html = html
				+ "</span><br>"
			+ "</div>\n"
			+ "<div id='" + this.prefix + "boxbar3' class='" + this.prefix + "boxbar3'" + ">"
				+ "<span><a id='" + this.prefix + "boxbarmax2' class='" + this.prefix + "boxbarmax2' " + (this.forceStyles ? "style='color:white' " : "") + " onclick='return false;' href='#'>" + "</a>"
				+ "</span><br>";
		if (this.showClose) {
			html = html + " <a id='" + this.prefix + "boxclose2' class='" + this.prefix + "boxclose2' onclick='return false;' href='#'> <img src='" + SDK.url + "/images/closeg.png'></a>";
		}
		html = html
			+ "</div>\n"
			+ "</span>"
			+ "</div>\n";
		
		if (this.promptContactInfo) {
			html = html
				+ "<div id='" + this.prefix + "contactinfo' class='" + this.prefix + "box' " + backgroundstyle + ">"
					+ "<div class='" + this.prefix + "boxmenu'>"
						+ "<span style='float:right'><a id='" + this.prefix + "contactboxmin' class='" + this.prefix + "contactboxmin' onclick='return false;' href='#'><img src='" + SDK.url + "/images/minimize.png'></a>"
					+ "</div>\n"
					+ "<div style='margin:10px'>\n"
						+ "<span>" + SDK.translate("Name") + "</span><br/><input id='" + this.prefix + "contactname' type='text' /><br/>\n"
						+ "<span>" + SDK.translate("Email") + "</span><br/><input id='" + this.prefix + "contactemail' type='email' /><br/>\n"
						+ "<span>" + SDK.translate("Phone") + "</span><br/><input id='" + this.prefix + "contactphone' type='text' /><br/>\n"
						+ "<br/><a id='" + this.prefix + "contactconnect' class='" + this.prefix + "contactconnect' " + (this.forceStyles ? "style='color:white' " : "") + " onclick='return false;' href='#'>" + SDK.translate("Connect") + "</a>\n"
					+ "</div>\n"
				+ "</div>";
		}
		if (this.promptEmailChatLog) {
			html = html
				+ "<div id='" + this.prefix + "emailchatlogdialog' class='" + this.prefix + "box' " + backgroundstyle + ">"
					+ "<div class='" + this.prefix + "boxmenu'>"
						+ "<span style='float:right'><a id='" + this.prefix + "emailchatlogdialogmin' class='" + this.prefix + "boxmin' onclick='return false;' href='#'><img src='" + SDK.url + "/images/minimize.png'></a>"
					+ "</div>\n"
					+ "<div style='margin:10px;margin-bottom:20px;margin-top:20px;'>\n"
						+ "<span>" + SDK.translate("Would you like a copy of the chat log sent to your email?") + "</span><br/><input id='" + this.prefix + "emailchatlogemail' type='email' /><br/>\n"
						+ "<br/><a id='" + this.prefix + "emailchatlogdialogyes' class='" + this.prefix + "emailconfirm' " + (this.forceStyles ? "style='color:white' " : "") + " onclick='return false;' href='#'> " + SDK.translate("Yes") + "</a>\n"
						+ " <a id='" + this.prefix + "emailchatlogdialogno' class='" + this.prefix + "emailconfirm' " + (this.forceStyles ? "style='color:white' " : "") + " onclick='return false;' href='#'> " + SDK.translate("No") + "</a>\n"
					+ "</div>\n"
				+ "</div>";
		}
		
		box.innerHTML = html;
		SDK.body().appendChild(box);

		if (this.avatar && this.chatLog) {
			var online = document.getElementById(this.prefix + "online");
			if (online != null) {
				online.style.display = "none";
			}
			var scroller = document.getElementById(this.prefix + "scroller");
			if (scroller != null) {
				scroller.style.display = "none";
			}
			var chatLogDiv = document.getElementById(this.prefix + "showChatLog");
			if (chatLogDiv != null) {
				chatLogDiv.style.display = "block";
			}
		} else if (this.avatar && !this.chatLog) {
			var online = document.getElementById(this.prefix + "online");
			if (online != null) {
				online.style.display = "none";
			}
			var scroller = document.getElementById(this.prefix + "scroller");
			if (scroller != null) {
				scroller.style.display = "none";
			}
			var chatLogDiv = document.getElementById(this.prefix + "showChatLog");
			if (chatLogDiv != null) {
				chatLogDiv.style.display = "none";
			}
		} else if (!this.avatar && this.chatLog) {
			var online = document.getElementById(this.prefix + "online");
			if (online != null) {
				online.style.display = "inline";
			}
			var scroller = document.getElementById(this.prefix + "scroller");
			if (scroller != null) {
				scroller.style.display = "inline-block";
			}
			var avatarDiv = document.getElementById(this.prefix + "avatar-div");
			if (avatarDiv != null) {
				avatarDiv.style.display = "none";
			}
			var chatLogDiv = document.getElementById(this.prefix + "showChatLog");
			if (chatLogDiv != null) {
				chatLogDiv.style.display = "none";
			}
			var bubbleDiv = document.getElementById(this.prefix + "bubble-div");
			if (bubbleDiv != null) {
				bubbleDiv.style.display = "none";	
			}
			var noBubblePlain = document.getElementsByClassName(this.prefix + "no-bubble-plain");
			if (noBubblePlain != null && noBubblePlain.length != 0) {
				noBubblePlain[0].style.display = "none";
			}
		} else {
			var online = document.getElementById(this.prefix + "online");
			if (online != null) {
				online.style.display = "none";
			}
			var scroller = document.getElementById(this.prefix + "scroller");
			if (scroller != null) {
				scroller.style.display = "none";
			}
			var avatarDiv = document.getElementById(this.prefix + "avatar-div");
			if (avatarDiv != null) {
				avatarDiv.style.display = "none";
			}
			var chatLogDiv = document.getElementById(this.prefix + "showChatLog");
			if (chatLogDiv != null) {
				chatLogDiv.style.display = "none";
			}
		}
		
		if (this.online && this.chatroom) {
			document.getElementById(this.prefix + "online").style.height = "95px";
		}
		if (this.chatLog && !this.bubble) {
			var bubbleDiv = document.getElementById(this.prefix + 'bubble-div');
			if (bubbleDiv != null) {
				bubbleDiv.style.display = "none";
			}
			document.getElementById(this.prefix + 'no-bubble-plain').style.display = "none";
			document.getElementById(this.prefix + 'response').style.display = "none";
		}
		
		var self = this;
		var listen = false;
		if (document.getElementById(this.prefix + "showChatLog") != null) {
			document.getElementById(this.prefix + "showChatLog").addEventListener("click", function() {
				self.showChatLog();
				return false;
			});
		}
		if (document.getElementById(this.prefix + "showAvatarBot") != null) {
			document.getElementById(this.prefix + "showAvatarBot").addEventListener("click", function() {
				self.showAvatarBot();
				return false;
			});
		}
		if (document.getElementById(this.prefix + "chat") != null) {
			document.getElementById(this.prefix + "chat").addEventListener("keypress", function(event) {
				if (event.keyCode == 13) {
					self.sendMessage();
					return false;
				}
			});
		}
		if (document.getElementById(this.prefix + "exit") != null) {
			document.getElementById(this.prefix + "exit").addEventListener("click", function() {
				self.exit();
				return false;
			});
		}
		if (document.getElementById(this.prefix + "ping")!= null) {
			document.getElementById(this.prefix + "ping").addEventListener("click", function() {
				self.ping();
				return false;
			});
		}
		if (document.getElementById(this.prefix + "clear") != null) {
			document.getElementById(this.prefix + "clear").addEventListener("click", function() {
				self.clear();
				return false;
			});
		}
		if (document.getElementById(this.prefix + "accept") != null) {
			document.getElementById(this.prefix + "accept").addEventListener("click", function() {
				self.accept();
				return false;
			});
		}
		if (document.getElementById(this.prefix + "sendImage") != null) {
			document.getElementById(this.prefix + "sendImage").addEventListener("click", function() {
				self.sendImage();
				return false;
			});
		}
		if (document.getElementById(this.prefix + "sendAttachment") != null) {
			document.getElementById(this.prefix + "sendAttachment").addEventListener("click", function() {
				self.sendAttachment();
				return false;
			});
		}
		if (this.emailChatLog) {
			if (document.getElementById(this.prefix + "emailChatLog") != null) {
				document.getElementById(this.prefix + "emailChatLog").addEventListener("click", function() {
					self.emailChatLog();
					return false;
				});
			}
		}
		if (this.promptEmailChatLog) {
			document.getElementById(this.prefix + "emailchatlogdialogyes").addEventListener("click", function() {
				self.sendEmailChatLogBox();
				return false;
			});
			document.getElementById(this.prefix + "emailchatlogdialogno").addEventListener("click", function() {
				self.minimizeEmailChatLogBox();
				return false;
			});
			document.getElementById(this.prefix + "emailchatlogdialogmin").addEventListener("click", function() {
				self.minimizeEmailChatLogBox();
				return false;
			});
		}
		var menu = document.getElementById(this.prefix + "flag");
		if (menu != null) {
			menu.addEventListener("click", function() {
				self.flag();
				return false;
			});
		}
		menu = document.getElementById(this.prefix + "whisper");
		if (menu != null) {
			menu.addEventListener("click", function() {
				self.whisper();
				return false;
			});
		}
		menu = document.getElementById(this.prefix + "pvt");
		if (menu != null) {
			menu.addEventListener("click", function() {
				self.pvt();
				return false;
			});
		}
		if (document.getElementById(this.prefix + "toggleKeepAlive") != null) {
			document.getElementById(this.prefix + "toggleKeepAlive").addEventListener("click", function() {
				self.toggleKeepAlive();
				if (self.connection.keepAlive) {
					document.getElementById('boxkeepalive').src = SDK.url + "/images/ping.svg";
				} else {
					document.getElementById('boxkeepalive').src = SDK.url + "/images/empty.png";
				}
			});
		}
		if (document.getElementById(this.prefix + "toggleChime") != null) {
			document.getElementById(this.prefix + "toggleChime").addEventListener("click", function() {
				self.toggleChime();
				if (self.playChime) {
					document.getElementById('boxchime').src = SDK.url + "/images/sound.svg";
				} else {
					document.getElementById('boxchime').src = SDK.url + "/images/mute.svg";
				}
			});
		}
		if (document.getElementById(this.prefix + "toggleSpeak") != null) {
			document.getElementById(this.prefix + "toggleSpeak").addEventListener("click", function() {
				self.toggleSpeak();
				if (self.speak) {
					document.getElementById('boxtalk').src = SDK.url + "/images/talk.svg";
				} else {
					document.getElementById('boxtalk').src = SDK.url + "/images/talkoff.svg";
				}
				return false;
			});
		}
		if (document.getElementById(this.prefix + "toggleListen") != null) {
			document.getElementById(this.prefix + "toggleListen").addEventListener("click", function() {
				listen = !listen;
				if (listen) {
					SDK.startSpeechRecognition();
					document.getElementById('boxmic').src = SDK.url + "/images/mic.svg";
				} else {
					SDK.stopSpeechRecognition();
					document.getElementById('boxmic').src = SDK.url + "/images/micoff.svg";
				}
				return false;
			});
		}
		document.getElementById(this.prefix + "boxmin").addEventListener("click", function() {
			self.minimizeBox();
			return false;
		});
		if (this.promptContactInfo) {
			document.getElementById(this.prefix + "contactboxmin").addEventListener("click", function() {
				self.minimizeContactInfoBox();
				return false;
			});
			document.getElementById(this.prefix + "contactconnect").addEventListener("click", function() {
				self.contactConnect();
				return false;
			});
		}
		if (document.getElementById(this.prefix + "boxmax") != null) {
			document.getElementById(this.prefix + "boxmax").addEventListener("click", function() {
				self.popup();
				return false;
			});
		}
		if (this.showClose) {
			document.getElementById(this.prefix + "boxclose").addEventListener("click", function() {
				self.closeBox();
				return false;
			});
			document.getElementById(this.prefix + "boxclose2").addEventListener("click", function() {
				self.closeBox();
				return false;
			});
			document.getElementById(this.prefix + "boxbarmax").addEventListener("click", function() {
				self.maximizeBox();
				return false;
			});
			document.getElementById(this.prefix + "boxbarmax2").addEventListener("click", function() {
				self.maximizeBox();
				return false;
			});
		} else {
			document.getElementById(this.prefix + "boxbar").addEventListener("click", function() {
				self.maximizeBox();
				return false;
			});
		}
	}
	
	this.showChatLog = function() {
		var avatarDiv = document.getElementById(this.prefix + "avatar-div");
		if (avatarDiv != null) {
			avatarDiv.style.display = "none";
		}
		var online = document.getElementById(this.prefix + "online");
		if (online != null) {
			online.style.display = "inline";
		}
		var bubbleDiv = document.getElementById(this.prefix + "bubble-div");
		if (bubbleDiv != null) {
			bubbleDiv.style.display = "none";
		}
		var noBubblePlain = document.getElementsByClassName(this.prefix + "no-bubble-plain");
		if (noBubblePlain != null && noBubblePlain.length != 0) {
			noBubblePlain[0].style.display = "none";
		}
		var scroller = document.getElementById(this.prefix + "scroller");
		if (scroller != null) {
			scroller.style.display = "inline-block";
		}
		document.getElementById(this.prefix + "showChatLog").style.display = "none";
		document.getElementById(this.prefix + "showAvatarBot").style.display = "block";
		if (this.background == null && this.backgroundIfNotChrome) {
			var box = document.getElementById(this.prefix + "box");
			if (box != null) {
				box.style.backgroundColor = "#fff";
			}
		}
	}
	
	this.showAvatarBot  = function() {
		var online = document.getElementById(this.prefix + "online");
		if (online != null) {
			online.style.display = "none";
		}
		var scroller = document.getElementById(this.prefix + "scroller");
		if (scroller != null) {
			scroller.style.display = "none";
		}
		var avatarDiv = document.getElementById(this.prefix + "avatar-div");
		if (avatarDiv != null) {
			avatarDiv.style.display = "inline-block";
		}
		var bubbleDiv = document.getElementById(this.prefix + "bubble-div");
		if (bubbleDiv != null) {
			bubbleDiv.style.display = "inherit";
		}
		var noBubblePlain = document.getElementsByClassName(this.prefix + "no-bubble-plain");
		if (noBubblePlain != null && noBubblePlain.length != 0) {
			noBubblePlain[0].style.display = "inherit";
		}
		document.getElementById(this.prefix + "showChatLog").style.display = "block";
		document.getElementById(this.prefix + "showAvatarBot").style.display = "none";
		if (this.background == null && this.backgroundIfNotChrome) {
			var box = document.getElementById(this.prefix + "box");
			if (box != null) {
				box.style.backgroundColor = null;
				box.style.border = null;
			}
		}
	}
	
	/**
	 * Minimize the live chat embedding box.
	 */
	this.minimizeBox = function() {
		this.onlineBar = false;
		var element = null;
		if (this.promptContactInfo) {
			element = document.getElementById(this.prefix + "contactinfo");
			if (element != null) {
				element.style.display = 'none';
			}
		}
		element = document.getElementById(this.prefix + "box");
		if (element != null) {
			element.style.display = 'none';
		}
		var onlineDiv = document.getElementById(this.prefix + "online");
		if (onlineDiv != null) {
			onlineDiv.style.display = 'none';
		}
		if (this.promptEmailChatLog) {
			element = document.getElementById(this.prefix + "emailchatlogemail");
			if (element != null) {
				element.value = this.contactEmail;
			}
			element = document.getElementById(this.prefix + "emailchatlogdialog");
			if (element != null) {
				element.style.display = 'inline';
			}
			return false;
		}
		element = document.getElementById(this.prefix + "boxbar");
		if (element != null) {
			element.style.display = 'inline';
		}
		if (this.prefix.indexOf("livechat") != -1) {
			element = document.getElementById(this.prefix.substring(0, this.prefix.indexOf("livechat")) + "boxbar");
			if (element != null) {
				element.style.display = 'inline';
			}
		}
		if (this.prefix.indexOf("chat") != -1) {
			element = document.getElementById(this.prefix.substring(0, this.prefix.indexOf("chat")) + "boxbar");
			if (element != null) {
				element.style.display = 'inline';
			}
		}
		this.exit();
		setTimeout(function() {
			self.exit();
		}, 100);
		return false;
	}
	
	/**
	 * Minimize the email chat log confirm dialog.
	 */
	this.minimizeEmailChatLogBox = function() {
		if (this.promptContactInfo) {
			document.getElementById(this.prefix + "contactinfo").style.display = 'none';
		}
		document.getElementById(this.prefix + "box").style.display = 'none';
		document.getElementById(this.prefix + "emailchatlogdialog").style.display = 'none';
		document.getElementById(this.prefix + "boxbar").style.display = 'inline';
		if (this.prefix.indexOf("livechat") != -1) {
			var chatbot = document.getElementById(this.prefix.substring(0, this.prefix.indexOf("livechat")) + "boxbar");
			if (chatbot != null) {
				chatbot.style.display = 'inline';
			}
		}
		if (this.prefix.indexOf("chat") != -1) {
			var chatbot = document.getElementById(this.prefix.substring(0, this.prefix.indexOf("chat")) + "boxbar");
			if (chatbot != null) {
				chatbot.style.display = 'inline';
			}
		}
		this.exit();
		setTimeout(function() {
			self.exit();
		}, 100);
		return false;
	}
	
	/**
	 * Minimize the email chat log confirm dialog.
	 */
	this.sendEmailChatLogBox = function() {
		this.contactEmail = document.getElementById(this.prefix + "emailchatlogemail").value;
		this.sendEmailChatLog();
		if (this.promptContactInfo) {
			document.getElementById(this.prefix + "contactinfo").style.display = 'none';
		}
		document.getElementById(this.prefix + "box").style.display = 'none';
		document.getElementById(this.prefix + "emailchatlogdialog").style.display = 'none';
		document.getElementById(this.prefix + "boxbar").style.display = 'inline';
		if (this.prefix.indexOf("livechat") != -1) {
			var chatbot = document.getElementById(this.prefix.substring(0, this.prefix.indexOf("livechat")) + "boxbar");
			if (chatbot != null) {
				chatbot.style.display = 'inline';
			}
		}
		if (this.prefix.indexOf("chat") != -1) {
			var chatbot = document.getElementById(this.prefix.substring(0, this.prefix.indexOf("chat")) + "boxbar");
			if (chatbot != null) {
				chatbot.style.display = 'inline';
			}
		}
		setTimeout(function() {
			self.exit();
		}, 100);
		return false;
	}
	
	/**
	 * Minimize the contact info box.
	 */
	this.minimizeContactInfoBox = function() {
		if (this.promptContactInfo) {
			document.getElementById(this.prefix + "contactinfo").style.display = 'none';
		}
		document.getElementById(this.prefix + "box").style.display = 'none';
		document.getElementById(this.prefix + "boxbar").style.display = 'inline';
		if (this.prefix.indexOf("livechat") != -1) {
			var chatbot = document.getElementById(this.prefix.substring(0, this.prefix.indexOf("livechat")) + "boxbar");
			if (chatbot != null) {
				chatbot.style.display = 'inline';
			}
		}
		if (this.prefix.indexOf("chat") != -1) {
			var chatbot = document.getElementById(this.prefix.substring(0, this.prefix.indexOf("chat")) + "boxbar");
			if (chatbot != null) {
				chatbot.style.display = 'inline';
			}
		}
		return false;
	}
	
	/**
	 * Check contact info and connect.
	 */
	this.contactConnect = function() {
		this.hasContactInfo = true;
		this.contactName = document.getElementById(this.prefix + "contactname").value;
		var ok = true;
		if (this.contactName != null && this.contactName == "") {
			ok = false;
			document.getElementById(this.prefix + "contactname").style.borderColor = "red";
			document.getElementById(this.prefix + "contactname").placeholder = SDK.translate("Enter name");
		}
		this.contactEmail = document.getElementById(this.prefix + "contactemail").value;
		if (this.contactEmail != null && this.contactEmail.indexOf("@") == -1) {
			ok = false;
			document.getElementById(this.prefix + "contactemail").style.borderColor = "red";
			document.getElementById(this.prefix + "contactemail").placeholder = SDK.translate("Enter valid email");
		}
		this.contactPhone = document.getElementById(this.prefix + "contactphone").value;
		this.contactInfo = this.contactName + " " + this.contactEmail + " " + this.contactPhone;
		if (ok) {
			this.maximizeBox();
		}
	}
	
	/**
	 * Maximize the embedding div in the current webpage.
	 */
	this.maximizeBox = function() {
		this.onlineBar = true;
		if (this.promptContactInfo && !this.hasContactInfo) {
			document.getElementById(this.prefix + "contactinfo").style.display = 'inline';
			document.getElementById(this.prefix + "boxbar").style.display = 'none';
			document.getElementById(this.prefix + "box").style.display = 'none';
			if (this.prefix.indexOf("livechat") != -1) {
				var chatbot = document.getElementById(this.prefix.substring(0, this.prefix.indexOf("livechat")) + "boxbar");
				if (chatbot != null) {
					chatbot.style.display = 'none';
				}
			}
			if (this.prefix.indexOf("chat") != -1) {
				var chatbot = document.getElementById(this.prefix.substring(0, this.prefix.indexOf("chat")) + "boxbar");
				if (chatbot != null) {
					chatbot.style.display = 'none';
				}
			}
		} else {
			if (this.promptContactInfo) {
				document.getElementById(this.prefix + "contactinfo").style.display = 'none';
			}
			document.getElementById(this.prefix + "boxbar").style.display = 'none';
			document.getElementById(this.prefix + "box").style.display = 'inline';
			if (this.prefix.indexOf("livechat") != -1) {
				var chatbot = document.getElementById(this.prefix.substring(0, this.prefix.indexOf("livechat")) + "boxbar");
				if (chatbot != null) {
					chatbot.style.display = 'none';
				}
			}
			if (this.prefix.indexOf("chat") != -1) {
				var chatbot = document.getElementById(this.prefix.substring(0, this.prefix.indexOf("chat")) + "boxbar");
				if (chatbot != null) {
					chatbot.style.display = 'none';
				}
			}
			var chat = new LiveChatConnection();
			chat.sdk = this.sdk;
			if (this.contactInfo != null) {
				chat.contactInfo = this.contactInfo;
			}
			var channel = new ChannelConfig();
			channel.id = this.instance;
			chat.listener = this;
			chat.connect(channel, this.sdk.user);
			var self = this;
			if (this.autoAccept != null) {
				setTimeout(function() {
					chat.accept();
				}, self.autoAccept);
			}
		}
		return false;
	}
	
	/**
	 * Close the embedding div in the current webpage.
	 */
	this.closeBox = function() {
		if (this.promptContactInfo) {
			document.getElementById(this.prefix + "contactinfo").style.display = 'none';
		}
		document.getElementById(this.prefix + "boxbar").style.display = 'none';
		document.getElementById(this.prefix + "box").style.display = 'none';
		if (this.prefix.indexOf("livechat") != -1) {
			var chatbot = document.getElementById(this.prefix.substring(0, this.prefix.indexOf("livechat")) + "boxbar");
			if (chatbot != null) {
				chatbot.style.display = 'none';
			}
		}
		if (this.prefix.indexOf("chat") != -1) {
			var chatbot = document.getElementById(this.prefix.substring(0, this.prefix.indexOf("chat")) + "boxbar");
			if (chatbot != null) {
				chatbot.style.display = 'none';
			}
		}
		this.exit();
		var self = this;
		setTimeout(function() {
			self.exit();
		}, 100);
		return false;
	}
	
	/**
	 * Create a popup window live chat session.
	 */
	this.popup = function() {
		var box = document.getElementById(this.prefix + "box");
		if (box != null) {
			box.style.display = 'none';
		}
		if (this.popupURL != null) {
			var popupURL = this.popupURL;
			if (popupURL.indexOf("livechat?") != -1) {
				if (this.contactInfo != null && this.contactInfo != "") {
					popupURL = popupURL + "&info=" + encodeURI(this.contactInfo);
				}
				if (this.translate == true && this.lang != null && this.lang != "") {
					popupURL = popupURL + "&translate=" + encodeURI(this.lang);
				}
			}
			SDK.popupwindow(popupURL, 'child', 700, 520);
		} else {
			var form = document.createElement("form");
			form.setAttribute("method", "post");
			form.setAttribute("action", SDK.url + "/livechat");
			form.setAttribute("target", 'child');
 
			var input = document.createElement('input');
			input.type = "hidden";
			input.name = "id";
			input.value = this.instance;
			form.appendChild(input);

			input = document.createElement('input');
			input.type = "hidden";
			input.name = "embedded";
			input.value = "embedded";
			form.appendChild(input);
 
			input = document.createElement('input');
			input.type = "hidden";
			input.name = "chat";
			input.value = "true";
			form.appendChild(input);
 
			input = document.createElement('input');
			input.type = "hidden";
			input.name = "application";
			input.value = this.connection.credentials.applicationId;
			form.appendChild(input);
 
			if (this.css != null) {
				input = document.createElement('input');
				input.type = "hidden";
				input.name = "css";
				input.value = this.css;
				form.appendChild(input);
			}
 
			var input = document.createElement('input');
			input.type = 'hidden';
			input.name = "language";
			input.value = SDK.lang;
			form.appendChild(input);
			
			if (this.translate == true && this.lang != null && this.lang != "") {
				var input = document.createElement('input');
				input.type = 'hidden';
				input.name = "translate";
				input.value = this.lang;
				form.appendChild(input);
			}
			
			input = document.createElement('input');
			input.type = "hidden";
			input.name = "info";
			input.value = this.contactInfo;
			form.appendChild(input);
			
			SDK.body().appendChild(form);
			
			SDK.popupwindow('','child', 700, 520);
			
			form.submit();
			SDK.body().removeChild(form);
		}
		this.minimizeBox();
		return false;
	}
	
	window.onfocus = function() {
		self.isActive = true;
		if (document.title != self.windowTitle) {
			document.title = self.windowTitle;
		}
	}; 

	window.onblur = function() {
		self.isActive = false;
	};

	
	/**
	 * Search for link using <a href="chat:yes">...
	 * Switch them to use an onclick to post the chat back to the chat.
	 */
	this.linkChatPostbacks = function(node) {
		var self = this;
		var links = node.getElementsByTagName("a");
		for (var index = 0; index < links.length; index++) {
			var a = links[index];
			var href = a.getAttribute("href");
			if (href != null && href.indexOf("chat:") != -1) {
				var chat = href.substring("chat:".length, href.length).trim();
				var temp = function(param, element) {
					element.onclick = function() {
						self.connection.sendMessage(param);
						return false;
					};
				}
				temp(chat, a);
			}
		}
		var buttons = node.getElementsByTagName("button");
		for (var index = 0; index < buttons.length; index++) {
			var button = buttons[index];
			if (button.parentNode.nodeName == "A") {
				continue;
			}
			var chat = button.textContent;
			if (chat != null && chat.length > 0) {
				var temp = function(param, element) {
					element.onclick = function() {
						self.connection.sendMessage(param);
						return false;
					};
				}
				temp(chat, button);
			}
		}
		var choices = node.getElementsByTagName("select");
		for (var index = 0; index < choices.length; index++) {
			var choice = choices[index];
			var temp = function(param) {
				param.addEventListener("change", function() {
					self.connection.sendMessage(param.value);
					return false;
				});
			}
			temp(choice);
		}
	}
		
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
			if (this.avatar && this.sdk != null && this.avatarId != null) {
				var config = new AvatarMessage();
				config.message = SDK.stripTags(responseText);
				config.avatar = this.avatarId;
				if (this.nativeVoice && SDK.speechSynthesis) {
					config.speak = false;
				} else {
					config.speak = this.speak;
					config.voice = this.voice;
					config.voiceMod = this.voiceMod;
				}
				//config.emote = emote;
				//config.action = action;
				//config.pose = pose;
				var self = this;
				this.sdk.avatarMessage(config, function(responseMessage) {
					self.updateAvatar(responseMessage, null);
				});
			} else if (this.speak) {
				SDK.tts(SDK.stripTags(responseText), this.voice, this.nativeVoice, this.lang, this.nativeVoiceName);
			}
			document.getElementById(this.prefix + 'response').innerHTML = SDK.linkURLs(responseText);
			this.linkChatPostbacks(document.getElementById(this.prefix + 'response'));
			// Fix Chrome bug,
			if (SDK.fixChromeResizeCSS && SDK.isChrome()) {
				var padding = document.getElementById(this.prefix + 'response').parentNode.parentNode.style.padding;
				document.getElementById(this.prefix + 'response').parentNode.parentNode.style.padding = "7px";
				var self = this;
				setTimeout(function() {
					document.getElementById(self.prefix + 'response').parentNode.parentNode.style.padding = padding;
				}, 10);
			}
			this.switchText = false;
		} else {
			this.switchText = true;
		}
		var scroller = document.getElementById(this.prefix + 'scroller');
		var consolepane = document.getElementById(this.prefix + 'console');
		if (scroller == null || consolepane == null) {
			return;
		}
		if (this.chatLog) {
			var tr = document.createElement('tr');
			tr.style.verticalAlign = "top";
			var td = document.createElement('td');
			var td2 = document.createElement('td');
			var div = document.createElement('div');
			var span = document.createElement('span');
			var br = document.createElement('br');
			var span2 = document.createElement('span');
			var div2 = document.createElement('div');
			var chatClass = this.prefix + 'chat-1';
			div.className = this.prefix + 'chat-1-div';
			div2.className = this.prefix + 'chat-1-div-2';
			span.className = this.prefix + 'chat-user-1';
			td.className = this.prefix + 'chat-user-1';
			if (this.switchText) {
				td.className = this.prefix + 'chat-user-2';
				chatClass = this.prefix + 'chat-2';
				div.className = this.prefix + 'chat-2-div';
				div2.className = this.prefix + 'chat-2-div-2';
				span.className = this.prefix + 'chat-user-2';
			}
			var userImg = document.createElement('img');
			userImg.className = this.prefix + 'chat-user';
			var speakerName = speaker.slice(0, -1);
			if (speakerName != "Info" && speakerName != "Error") {
				for(var key in this.users) {
					if (key === speakerName) {
						userImg.setAttribute('alt', speakerName);
						userImg.setAttribute('src', this.users[key]);
						break;
					}
				}
			}
			td.appendChild(userImg);
			td.setAttribute('nowrap', 'nowrap');
			td2.className = chatClass;
			td2.setAttribute('align', 'left');
			td2.setAttribute('width', '100%');
			
			var date = new Date(); 
			var time = date.getHours() + ":" + ((date.getMinutes() < 10)? "0" : "") + date.getMinutes() + ":" + ((date.getSeconds() < 10)? "0" : "") + date.getSeconds();
			span.innerHTML = speaker + " <small>" + time + "</small>";
			span2.className = chatClass;
			span2.innerHTML = SDK.linkURLs(responseText);
			this.linkChatPostbacks(span2);
			consolepane.appendChild(tr);
			
			tr.appendChild(td);
			tr.appendChild(td2);
			div.appendChild(span);
			div.appendChild(br);
			div.appendChild(div2);
			td2.appendChild(div);
			div2.appendChild(span2);
		}
		this.switchText = !this.switchText;
		while (consolepane.childNodes.length > 500) {
			consolepane.removeChild(consolepane.firstChild);
		}
		scroller.scrollTop = scroller.scrollHeight;
		if (this.focus) {
			document.getElementById(this.prefix + 'chat').focus();
		}
		if (!this.isActive) {
			document.title = SDK.stripTags(responseText);
		}
	};

	/**
	 * Update the avatar's image/video/audio from the message response.
	 */
	this.updateAvatar = function(responseMessage, afterFunction) {
		var urlprefix = this.connection.credentials.url + "/";
		SDK.updateAvatar(responseMessage, this.speak, urlprefix, this.prefix, null, afterFunction, this.nativeVoice, this.lang, this.nativeVoiceName);
	};
	
	/**
	 * An informational message was received from the channel.
	 * Such as a new user joined, private request, etc.
	 */	
	this.info = function(message) {
		if (this.connection.nick != null && this.connection.nick != "") {
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
		if (!this.linkUsers) {
			usersXML = usersXML.split('<a').join('<span');
			usersXML = usersXML.split('</a>').join('</span>');
		}
		var onlineList = document.getElementById(this.prefix + 'online');
		if (onlineList == null) {
			return;
		}
		if (!this.chatLog) {
			onlineList.style.height = "60px";
		}
		var div = document.createElement('div');
		div.innerHTML = usersXML;
		var children = div.childNodes[0].childNodes;
		var usersArray = {};
		var size = children.length;
		for(var i = 0; i < size; i++) {
			var userName = children[i].innerText;
			var child = children[i].childNodes;
			var imageSrc = child[0].getAttribute('src');
			usersArray[userName] = imageSrc;
		}
		this.users = usersArray;
		if (this.onlineBar) {
			var onlineBar = onlineList;
			onlineBar.innerHTML = '';
			if (this.chatroom || this.isFrame) { // displaying list of users on top
				var count = 0;
				var ids = {};
				var length = children.length;
				for (var i = 0; i < length; i++) {
					var child = children[i - count];
					ids[child.id] = child.id;
					if (document.getElementById(child.id) == null) {
						onlineList.appendChild(child);
						count++;
					}
				}
				onlineList.style.margin = "0";
				onlineList.style.display = 'inline';
			}
			else { // displaying only single bot on top
				var length = children.length;
				var child = children[length - 1];
				var keys = [];
				for(var keyItem in this.users) {
					keys.push(keyItem);
				}
				var botName = keys[keys.length - 1];
				var botImageSrc = this.users[botName];
				if (typeof botName === 'undefined' || typeof botImageSrc === 'undefined') {
					return;
				}
				var botImage = document.createElement('img');
				botImage.className = this.prefix + "-bot-image";
				botImage.setAttribute('alt', botName);
				botImage.setAttribute('src', botImageSrc);
				var botSpan = document.createElement('span');
				botSpan.className = this.prefix + "user-bot";
				botSpan.innerHTML = botName;
				onlineBar.append(botImage);
				onlineBar.append(botSpan);
				if (!this.isFrame) {
					if (!this.avatar) {
						onlineList.style.display = 'block';
					}
					var line = document.createElement('hr');
					var onlineDiv = document.getElementById(this.prefix + 'online-div');
					onlineDiv.appendChild(line);
				}
			}
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
		var onlineDiv = document.getElementById(this.prefix + 'online-div');
		if (onlineDiv == null) {
			return;
		}
		children = onlineDiv.childNodes;
		count = 0;
		length = children.length;
		for (var i = 0; i < length; i++) {
			var child = children[i - count];
			if (child.id != (this.prefix + 'online') && ids[child.id] == null) {
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
		var message = document.getElementById(this.prefix + 'chat').value;
		if (message != '') {
			this.connection.sendMessage(message);
			document.getElementById(this.prefix + 'chat').value = '';
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
		document.getElementById(this.prefix + 'chat').value = '';
		return false;
	};

	this.accept = function() {
		this.connection.accept();
		document.getElementById(this.prefix + 'chat').value = '';
		return false;
	};

	this.exit = function() {
		if (this.connection != null) {
			this.connection.exit();
			document.getElementById(this.prefix + 'chat').value = '';
		}
		return false;
	};

	this.spyMode = function() {
		this.connection.spyMode();
		document.getElementById(this.prefix + 'chat').value = '';
		return false;
	};

	this.normalMode = function() {
		this.connection.normalMode();
		document.getElementById(this.prefix + 'chat').value = '';
		return false;
	};

	this.boot = function() {
		document.getElementById(this.prefix + 'chat').value = 'boot: user';
		return false;
	};

	this.emailChatLog = function() {
		document.getElementById(this.prefix + 'chat').value = 'email: ' + (this.contactEmail == null ? 'user@domain.com' : this.contactEmail);
		return false;
	};

	this.sendEmailChatLog = function() {
		this.connection.sendMessage('email: ' + this.contactEmail);
		return false;
	};

	this.whisper = function(user) {
		if (user == null) {
			user = 'user';
		}
		document.getElementById(this.prefix + 'chat').value = 'whisper: ' + user + ': message';
		return false;
	};

	this.flag = function(user) {
		if (user != null) {
			document.getElementById(this.prefix + 'chat').value = 'flag: ' + user + ': reason';
			return false;
		}
		document.getElementById(this.prefix + 'chat').value = 'flag: user: reason';
		return false;
	};

	this.pvt = function(user) {
		if (user != null) {
			this.connection.pvt(user);
			return false;
		}
		document.getElementById(this.prefix + 'chat').value = 'private: user';
		return false;
	};

	this.clear = function() {
		document.getElementById(this.prefix + 'response').innerHTML = '';
		var console = document.getElementById(this.prefix + 'console');
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
	try {

	var noMedia = false;
	// Some browsers disable playing of audio and video, so need to handle promise exception to request permission.
	if (SDK.canPlayVideo == null) {
		SDK.disableAudioAutoPlay = true;
	} else {
		SDK.disableAudioAutoPlay = false;
	}
	var playPromise = null;
	var end = null;
	var playFailed = false;
	var initVideoPromise = function() {
		if (playPromise !== undefined && playPromise != null && SDK.canPlayVideo == null) {
			playPromise.then(function() {
				//SDK.canPlayVideo = true;
				var sdkvideoplaybutton2 = document.getElementById("sdkvideoplaybutton2");
				if (sdkvideoplaybutton2 != null) {
					//sdkvideoplaybutton2.style.display = "none";
				}
			}).catch(function(error) {
				if (SDK.canPlayVideo == null) {
					playFailed = true;
					if (SDK.canPlayVideo == null) {
						SDK.canPlayVideo = false;
						SDK.playInitVideo = function() {
							SDK.initAudio();
							SDK.updateAvatar(responseMessage, speak, urlprefix, elementPrefix, channelaudio, afterFunction, nativeVoice, lang, voice);
							document.getElementById("sdkvideoplaybutton2").style.display = "none";
							var sdkplaybutton = document.getElementById("sdkplaybutton");
							if (sdkplaybutton != null) {
								sdkplaybutton.style.display = "none";
							}
							SDK.disableAudioAutoPlay = false;
						};
						var playButton = document.createElement('div');
						var html = "<div id='sdkvideoplaybutton2' style='position:fixed;bottom:32px;left:32px;z-index:164;'><img onclick='SDK.playInitVideo()' width='64' src='"
							+ SDK.url + "/images/playsound.png'/></div>"
						playButton.innerHTML = html;
						SDK.body().appendChild(playButton);
						setTimeout(function() {
							document.getElementById("sdkvideoplaybutton2").style.display = "none";
						}, 10000);
					}
				}
			});
		}
	};
	nativeVoice = nativeVoice && SDK.speechSynthesis;
	if (elementPrefix == null) {
		elementPrefix = "";
	}
	var avatarStatus = document.getElementById(elementPrefix + "avatar-status");
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
	if (responseMessage.avatarActionAudio != null && speak) {
		var audio = SDK.autoPlayActionAudio;
		if (audio == null) {
			audio = new Audio(urlprefix + responseMessage.avatarActionAudio);
		} else {
			audio.src = urlprefix + responseMessage.avatarActionAudio;
		}
		playPromise = audio.play();
		initVideoPromise();
	}
	if (!speak || SDK.currentBackgroundAudio != responseMessage.avatarAudio) {
		// Only switch if different audio.
		if (SDK.backgroundAudio != null) {
			SDK.backgroundAudio.pause();
			SDK.currentBackgroundAudio = null;
		}
		if (responseMessage.avatarAudio != null && speak) {
			SDK.currentBackgroundAudio = responseMessage.avatarAudio;
			SDK.backgroundAudio = SDK.autoPlayBackgroundAudio;
			if (SDK.backgroundAudio == null) {
				SDK.backgroundAudio = new Audio(urlprefix + responseMessage.avatarAudio);
			} else {
				SDK.backgroundAudio.src = urlprefix + responseMessage.avatarAudio;
			}
			SDK.backgroundAudio.loop = true;
			playPromise = SDK.backgroundAudio.play();
			initVideoPromise();
		}
	}
	var video = document.getElementById(elementPrefix + "avatar-video");
	var isVideo = responseMessage.avatarType != null && responseMessage.avatarType.indexOf("video") != -1;
	var useVideo = video != null && SDK.useVideo != false && (SDK.useVideo == true || !(SDK.isIPhone()));
	if (isVideo && useVideo) {
		var div = document.getElementById(elementPrefix + "avatar-image-div");
		if (div != null) {
			div.style.display = "none";
		}
		div = document.getElementById(elementPrefix + "avatar-game-div");
		if (div != null) {
			div.style.display = "none";
		}
		div = document.getElementById(elementPrefix + "avatar-video-div");
		var canvas = null;
		if (div != null) {
			div.style.display = "inline-block";
			if (SDK.videoBackground && responseMessage.avatarBackground != null) {
				div.style.backgroundImage = "url(" + urlprefix + responseMessage.avatarBackground + ")";
			}
			var canvasDiv = document.getElementById(elementPrefix + "avatar-canvas-div");
			if ((SDK.isChrome() || (SDK.isFirefox() && !SDK.isMac()) || SDK.useCanvas == true) && SDK.useCanvas != false && canvasDiv != null) {
				div.style.position = "fixed";
				div.style.top = "-1000";
				div.style.left = "-1000";
				div.style.opacity = "0";
				div.style.zIndex = "-1";
				canvasDiv.style.display = "inline-block";
				canvas = document.getElementById(elementPrefix + "avatar-canvas");
			}
		}
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
		end = function() {
			video.src = urlprefix + responseMessage.avatar;
			if (responseMessage.avatar2 == null) {
				video.loop = true;
			} else {
				video.loop = false;
				video.onended = function() {
					var index = Math.floor(Math.random() * 5);
					if (index == 4 && responseMessage.avatar5 != null) {
						video.src = urlprefix + responseMessage.avatar5;
					} else if (index == 3 && responseMessage.avatar4 != null) {
						video.src = urlprefix + responseMessage.avatar4;
					} else if (index == 2 && responseMessage.avatar3 != null) {
						video.src = urlprefix + responseMessage.avatar3;
					} else if (index == 1 && responseMessage.avatar2 != null) {
						video.src = urlprefix + responseMessage.avatar2;
					} else {
						video.src = urlprefix + responseMessage.avatar;
					}
					video.play();
				}
			}
			playPromise = video.play();
			initVideoPromise();
			if (afterFunction != null) {
				afterFunction();
			}
		}
		var talk = function() {
			if (responseMessage.message != null && responseMessage.message.length > 0) {
				if (responseMessage.avatarTalk != null) {
					if (speak) {
						if (responseMessage.speech == null && !nativeVoice) {
							end();
						} else {
							video.src = urlprefix + responseMessage.avatar;
							video.loop = true;
							var playing = false;
							playPromise = video.play();
							initVideoPromise();
	
							if (nativeVoice) {
								if ('SpeechSynthesisUtterance' in window) {
									utterance = new SpeechSynthesisUtterance(SDK.stripTags(responseMessage.message));
								} else {
									utterance = new SpeechSynthesisUtterance2(SDK.stripTags(responseMessage.message));
								}
								SDK.utterance = utterance;
								// Hack for Chrome Android bug
								if (SDK.isChrome() && SDK.isMobile()) {
									video.src = urlprefix + responseMessage.avatarTalk;
									video.loop = true;
									playPromise = video.play();
									initVideoPromise();
								} else {
									utterance.onstart = function() {
										if (playing) {
											return false;
										}
										if ('speechSynthesis' in window) {
											speechSynthesis.pause();
										}
										video.src = urlprefix + responseMessage.avatarTalk;
										video.loop = true;
										video.oncanplay = function() {
											if (playing) {
												return false;
											}
											playing = true;
											if ('speechSynthesis' in window) {
												speechSynthesis.resume();
											}
										}
										playPromise = video.play();
										initVideoPromise();
									}
								}
								utterance.onerror = function() {
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
									if (playing || playFailed) {
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
										playPromise = audio.play();
										initVideoPromise();
									}
									playPromise = video.play();
									initVideoPromise();
								}
								audio.onerror = function() {
									end();
								}
								//audio.onloadeddata = function() {console.log("loadeddata");}
								//audio.onloadedmetadata = function() {console.log("loadedmetadata");}
								//audio.onpause = function() {console.log("pause");}
								//audio.onplay = function() {console.log("play");}
								//audio.onplaying = function() {console.log("playing");}
								//audio.ontimeupdate = function() {console.log("timeupdate");}
								var onended = audio.onended;
								audio.onended = function() {
									if (onended != null) {
										onended();
									}
									end();
								}
								playPromise = audio.play();
								initVideoPromise();
								playPromise = video.play();
								initVideoPromise();
							}
						}
					} else {
						video.src = urlprefix + responseMessage.avatarTalk;
						video.loop = false;
						playPromise = video.play();
						initVideoPromise();
						var onended = video.onended;
						video.onended = function() {
							// This causes a talk loop
							//if (onended != null) {
							//	onended();
							//}
							end();
						}
					}
				} else {
					video.src = urlprefix + responseMessage.avatar;
					video.loop = true;
					playPromise = video.play();
					initVideoPromise();
					if (speak) {
						if (nativeVoice) {
							if ('SpeechSynthesisUtterance' in window) {
								utterance = new SpeechSynthesisUtterance(SDK.stripTags(responseMessage.message));
							} else {
								utterance = new SpeechSynthesisUtterance2(SDK.stripTags(responseMessage.message));
							}
							utterance.onend = afterFunction;
							SDK.nativeTTS(utterance, lang, voice);
						} else {
							var audio = SDK.play(urlprefix + responseMessage.speech, channelaudio);
							var onended = audio.onended;
							audio.onended = function() {
								// This causes a talk loop
								//if (onended != null) {
								//	onended();
								//}
								if (afterFunction != null) {
									afterFunction();
								}
							}
						}
					} else if (afterFunction != null) {
						afterFunction();
					}
				}
			} else {
				end();
			}
		}
		
		if (responseMessage.avatarAction != null) {
			video.src = urlprefix + responseMessage.avatarAction;
			video.loop = false;
			playPromise = video.play();
			initVideoPromise();
			//var onended = video.onended;
			video.onended = function() {
				// This causes a talk loop
				//if (onended != null) {
				//	onended();
				//}
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
		div = document.getElementById(elementPrefix + "avatar-game-div");
		if (div != null) {
			div.style.display = "none";
		}
		div = document.getElementById(elementPrefix + "avatar-image-div");
		if (div != null) {
			div.style.display = "inline-block";
		}
		var img = document.getElementById(elementPrefix + 'avatar');
		if (img != null) {
			if (isVideo) {
				img.src = urlprefix + responseMessage.avatarBackground;
			} else {
				img.src = urlprefix + responseMessage.avatar;
			}
		}
		img = document.getElementById(elementPrefix + 'avatar2');
		if (img != null) {
			if (isVideo) {
				img.src = urlprefix + responseMessage.avatarBackground;
			} else {
				img.src = urlprefix + responseMessage.avatar;
			}
		}
		if (speak && responseMessage.message != null && responseMessage.message.length > 0) {
			if (nativeVoice) {
				noMedia = true;
				SDK.tts(SDK.stripTags(responseMessage.message), null, true, lang, voice);
			} else if (responseMessage.speech != null) {
				var audio = SDK.play(urlprefix + responseMessage.speech, channelaudio);
				var onended = audio.onended;
				audio.onended = function() {
					if (onended != null) {
						onended();
					}
					if (afterFunction != null) {
						afterFunction();
					}
				}
			}
		} else {
			noMedia = true;
			if (afterFunction != null) {
				afterFunction();
			}
		}
	}
	
	} catch(err) {
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
 * If a prefix is set, these id will be prefixed by the prefix.
 * Or you can call createBox() to have the WebChatbotListener create its own components in the current page.
 * @class
 */
function WebChatbotListener() {
	/** Set the caption for the button bar button. */
	this.caption = null;
	/** Disallow speech. */
	this.allowSpeech = true;
	/** Enable/disallow speech recogition. Speech recogition requires the browser support HTML5 Speech (such as Chrome). */
	this.allowSpeechRecognition = null;
	/** Disallow image/file upload menus and buttons. */
	this.allowFiles = true;
	/** Add image/file upload buttons to toolbar. */
	this.showFileButtons = false;
	/** Auto submit image/file upload. */
	this.sendFiles = true;
	/** Remove menubar. */
	this.showMenubar = true;
	/** Show Box Max*/
	this.showBoxmax = true;
	/** Show Send Image*/
	this.showSendImage = true;
	/** Remove language choice. */
	this.showChooseLanguage = true;
	/** Enable or disable speech. */
	this.speak = true;
	/** Configure if the browser's native voice TTS should be used. */
	this.nativeVoice = false;
	/** Set the voice name for the native voice. */
	this.nativeVoiceName = null;
	/** Set the language for the native voice. */
	this.lang = null;
	/** Translate between the user's language, and the bot's language. */
	this.translate = false;
	/** Enable or disable avatar. */
	this.avatar = true;
	/** Set the avatar. */
	this.avatarId = null;
	/** Set if the avatar should request HD (high def) video/images. */
	this.hd = null;
	/** Set if the avatar should request a specific video or image format. */
	this.format = null;
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
	/** Allow the different style sheet options */
	this.version = null;
	/** Set if the box chat log should be shown. */
	this.chatLog = true;
	/** Allow the hover button color to be set. */
	this.hoverColor = "grey";
	/** Allow the background color to be set. */
	this.background = null;
	/** Avatar image/video width. */
	this.width = 300;
	/** Avatar image/video height. */
	this.height = null;
	/** Chat bar offest from side. */
	this.offset = 30;
	/** Chat Button Vertial Offset*/
	this.verticalOffset = 0;
	/** Only apply the background color if not Chrome. */
	this.backgroundIfNotChrome = false;
	/** onresponse event is raised after a response is received. */
	this.onresponse = null;
	/** Configure if chat should be given focus after response. */
	this.focus = !SDK.isMobile();
	/** Override the URL used in the chat bot box popup. */
	this.popupURL = null;
	/** Print response in chat bubble. */
	this.bubble = false;
	/** Initial message to send to the bot. */
	this.greetingMessage = null;
	/** Initial message to display from the bot. (it is normally better to set a greeting in the bot instead). */
	this.greeting = null;
	/** Loading message to display. */
	this.loading = "loading...";
	/** Element id and class prefix. This allows an id and class prefix to avoid name collisions on the element names for the chat, response, console, and avatar elements.*/
	this.prefix = "";
	/** This can be used to keep the bot's chat bar in synch with a livechat bar. */
	this.livechatPrefix = null;
	/** Allows the bot's thumbnail image to be set for chat log. */
	this.botThumb = {};
	/** Allows the user's thumbnail image to be set for chat log. */
	this.userThumb = {};
	/** Set styles explictly to avoid inheriting page styles. Disable this to be able to override styles. */
	this.forceStyles = true;
	/** Add additional css style code. */
	this.style = "";
	/** Set the location of the button and box, one of "bottom-right", "bottom-left", "top-right", "top-left". */
	this.boxLocation = "bottom-right";
	
	/** Prompt for name/email before connecting. */
	this.promptContactInfo = false;
	this.hasContactInfo = false;
	this.contactName = null;
	this.contactEmail = null;
	this.contactPhone = null;
	this.contactInfo = "";
	/** Set if the backlink should be displayed. */
	this.backlink = SDK.backlink;

	/** Support connections to external bots through their web API. */
	this.external = false;
	this.apiURL = null;
	this.apiPost = null;
	this.apiResponse = null;
	
	this.switchText = true;
	this.big = false;
	this.conversation = null;
	this.voiceInit = null;
	this.listen = false;
	/** Enable debug in popup. */
	this.debug = false;
		
	/**
	 * Create an embedding bar and div in the current webpage.
	 */
	this.createBox = function() {
		if (this.livechatPrefix == null) {
			if (this.version >= 6.0) {
				this.livechatPrefix = "chat";
			} else {
				this.livechatPrefix = "livechat";
			}
		}
		if (this.prefix == "" && this.elementPrefix != null) {
			this.prefix = this.elementPrefix;
		}
		if (this.caption == null) {
			this.caption = this.instanceName;
		}
		var backgroundstyle = "";
		var buttonstyle = "";
		var buttonHoverStyle = "";
		var hidden = "hidden";
		var border = "";
		if (this.backgroundIfNotChrome && (SDK.isChrome() || SDK.isFirefox())) {
			this.background = null;
		}
		var backgroundColor = "";
		if (this.background != null) {
			backgroundstyle = " style='background-color:" + this.background + "'";
			backgroundColor = "background-color:" + this.background + ";";
			hidden = "visible";
			border = "border:1px;border-style:solid;border-color:black;";
		} else {
			border = "border:1px;border-style:solid;border-color:transparent;";
			backgroundColor = "background-color:#fff;";
		}
		if (this.color != null) {
			buttonstyle = "background-color:" + this.color + ";";
		}
		if (this.hoverColor != null) {
			buttonHoverStyle = "background-color:" + this.hoverColor + ";";
		}
		var minWidth = "";
		var divWidth = "";
		var background = "";
		var minHeight = "";
		var divHeight = "";
		var maxDivWidth = "";
		var maxHeight = null;
		var responseWidth = "";
		var chatWidth = "";
		var hideAvatar = "";
		var avatarWidth = this.width;
		var minAvatarWidth = "";
		var scrollerHeight = this.height;
		var scrollerMinHeight = "";
		if (this.width != null) {
			if (typeof this.width === "string") {
				this.width = parseInt(this.width);
			}
			// Auto correct for a short window or screen (assuming square).
			// 250 is total box height minus avatar.
			if ((this.width + 280) > window.innerHeight) {
				avatarWidth = window.innerHeight - 280;
				if (avatarWidth < 100) {
					hideAvatar = "display:none";
				}
			}
			minWidth = "width:" + this.width + "px;";
			minAvatarWidth = "width:" + avatarWidth + "px;";
			background = "background-size:" + avatarWidth + "px auto;";
			divWidth = minWidth;
			divHeight = "min-height:" + avatarWidth + "px;";
			responseWidth = "width:" + (this.width - 16) + "px;";
			chatWidth = "width:" + this.width + "px;";
			maxDivWidth = "max-width:" + (this.width - 50) + "px;";
			scrollerHeight = avatarWidth;
		}
		if (this.height != null) {
			if (typeof this.height === "string") {
				this.height = parseInt(this.height);
			}
			minHeight = "height:" + this.height + "px;";
			divHeight = minHeight;
			if (this.width != null) {
				background = "background-size:" + this.width + "px " + this.height + "px;";
			} else {
				background = "background-size: auto " + this.height + "px;";
				divWidth = "min-width:" + this.height + "px;";
				responseWidth = "width:" + (this.height - 16) + "px;";
				chatWidth = "width:" + this.height + "px;";
			}
		} else {
			scrollerMinHeight = "height:" + scrollerHeight + "px;";
		}
		var inputFont = "";
		if (SDK.isMobile()) {
			inputFont = "font-size: 16px;";
		}
		var boxloc = "bottom:10px;right:10px";
		if (this.boxLocation == "top-left") {
			boxloc = "top:10px;left:10px";
		} else if (this.boxLocation == "top-right") {
			boxloc = "top:10px;right:10px";
		} else if (this.boxLocation == "bottom-left") {
			boxloc = "bottom:10px;left:10px";
		} else if (this.boxLocation == "bottom-right") {
			boxloc = "bottom:10px;right:10px";
		}
		var locationBottom = 20;
		if (this.version < 6.0 || this.prefix != "botplatform") {
			locationBottom = 2;
		}
		var boxbarloc = "bottom:" + (locationBottom + this.verticalOffset) + "px;right:" + this.offset + "px";
		if (this.boxLocation == "top-left") {
			boxbarloc = "top:" + (locationBottom + this.verticalOffset) + "px;left:" + this.offset + "px";
		} else if (this.boxLocation == "top-right") {
			boxbarloc = "top:" + (locationBottom + this.verticalOffset) + "px;right:" + this.offset + "px";
		} else if (this.boxLocation == "bottom-left") {
			boxbarloc = "bottom:" + (locationBottom + this.verticalOffset) + "px;left:" + this.offset + "px";
		} else if (this.boxLocation == "bottom-right") {
			boxbarloc = "bottom:" + (locationBottom + this.verticalOffset) + "px;right:" + this.offset + "px";
		}
		var box = document.createElement('div');	
		var html =
			"<style>\n"
				+ "." + this.prefix + "box { position:fixed;" + boxloc + ";z-index:1502;margin:2px;display:none;" + border + " }\n"
				+ "." + this.prefix + "boxmenu { visibility:" + hidden + "; }\n"
				+ (this.forceStyles ? "#" : ".") + "" + this.prefix + "boxbarmax { font-size:18px;margin:2px;padding:0px;text-decoration:none; }\n"
				+ "." + this.prefix + "boxbar { position:fixed;" + boxbarloc + ";z-index:152;margin:0;padding:6px;" + buttonstyle + " }\n"
				+ "." + this.prefix + "boxbar:hover { " + buttonHoverStyle + " }\n"
				+ "." + this.prefix + "no-bubble-text { " + responseWidth + "; max-height:100px; overflow:auto; }\n"
				+ "#" + this.prefix + "contactinfo { " + minHeight + minWidth + " }\n"
				+ "." + this.prefix + "contactconnect { margin:4px;padding:8px;color:white;text-decoration:none;" + buttonstyle + " }\n"
				+ "." + this.prefix + "scroller { overflow-x:hidden;" + scrollerMinHeight + minWidth + " }\n"
				+ "." + this.prefix + "bubble-text { " + responseWidth + "; max-height:100px; overflow:auto; }\n"
				+ "." + this.prefix + "chatchat-1-div { " + maxDivWidth + "}\n"
				+ "." + this.prefix + "chatchat-2-div { " + maxDivWidth + "}\n"
				+ (this.forceStyles ? "#" : ".") + this.prefix + "chat { width:99%;min-height:22px; }\n"
				+ "." + this.prefix + "box:hover { " + backgroundColor + " }\n"
				+ "." + this.prefix + "box:hover ." + this.prefix + "boxmenu { visibility:visible; }\n";
		if (this.version < 6.0 || this.prefix != "botplatform") {
			html = html
				+ "." + this.prefix + "box:hover { border:1px;border-style:solid;border-color:black; }\n"
				+ "." + this.prefix + "box:hover ." + this.prefix + "boxmenu { visibility:visible; }\n"
				+ "." + this.prefix + "boxclose, ." + this.prefix + "boxmin, ." + this.prefix + "boxmax { font-size:22px;margin:2px;padding:0px;text-decoration:none; }\n"
				+ "." + this.prefix + "boxclose:hover, ." + this.prefix + "boxmin:hover, ." + this.prefix + "boxmax:hover { color: #fff;background: grey; }\n"
				+ "." + this.prefix + "no-bubble { margin:4px; padding:8px; border:1px; border-style:solid; border-color:black; background-color:white; color:#585858; }\n"
				+ "." + this.prefix + "no-bubble-plain { margin:4px; padding:8px; border:1px; }\n"
				+ "#" + this.prefix + "contactinfo span { margin-left:4px;margin-top:4px; }\n"
				+ "#" + this.prefix + "contactinfo input { margin:4px;font-size:13px;height:33px;width:90%;border:1px solid #d5d5d5; }\n"
				+ "." + this.prefix + "boxbutton { width:20px;height:20px;margin:4px; }\n"
				+ "." + this.prefix + "menupopup div { position:absolute;margin: -1px 0 0 0;padding: 3px 3px 3px 3px;background: #fff;border-style:solid;border-color:black;border-width:1px;width:180px;max-width:300px;z-index:152;visibility:hidden;opacity:0;transition:visibility 0s linear 0.3s, opacity 0.3s linear; }\n"
				+ "." + this.prefix + "menupopup:hover div { display:inline;visibility:visible;opacity:1;transition-delay:0.5s; }\n"
				+ "a." + this.prefix + "menuitem { text-decoration: none;display: block;color: #585858; }\n"
				+ "a." + this.prefix + "menuitem:hover { color: #fff;background: grey; }\n"
				+ "tr." + this.prefix + "menuitem:hover { background: grey; }\n"
				+ "." + this.prefix + "yandex { display:none; }\n"
				+ "." + this.prefix + "chatpowered { margin:4px;color:grey;font-size:10px; }\n"
				+ "img." + this.prefix + "menu { width: 24px;height: 24px;margin: 2px;cursor: pointer;vertical-align: middle; }\n"
				+ "span." + this.prefix + "menu { color: #818181;font-size: 12px; }\n"
				+ "." + this.prefix + "bubble-div { padding-bottom:15px;position:relative; }\n"
				+ "." + this.prefix + "bubble { margin:4px; padding:8px; border:1px; border-style:solid; border-color:black; border-radius:10px; background-color:white; color:#585858; }\n"
				+ "." + this.prefix + "bubble:before { content:''; position:absolute; bottom:0px; left:40px; border-width:20px 0 0 20px; border-style:solid; border-color:black transparent; display:block; width:0;}\n"
				+ "." + this.prefix + "bubble:after { content:''; position:absolute; bottom:3px; left:42px; border-width:18px 0 0 16px; border-style:solid; border-color:white transparent; display:block; width:0;}\n"
				+ "." + this.prefix + "box-input-span { display:block; overflow:hidden; margin:4px; padding-right:4px; }\n"
				+ "#" + this.prefix + "boxtable { background:none; border:none; margin:0; }\n"
				+ "#" + this.prefix + "showChatLog { display:none; }\n"
				+ "#" + this.prefix + "showChatLogButton { display:none; }\n"
				+ "#" + this.prefix + "boxbar3 { display:none; }\n"
				+ "#" + this.prefix + "boxbarmax { color: white; }\n"
				+ "img." + this.prefix + "chat-user { height:40px; max-width:40px; }\n";
		}
		html = html + this.style
			+ "</style>\n"
			+ "<div id='" + this.prefix + "box' class='" + this.prefix + "box' " + backgroundstyle + ">"
				+ "<div class='" + this.prefix + "boxmenu'>"
					+ (this.backlink ? "<span class='" + this.prefix + "chatpowered'>powered by <a href='" + SDK.backlinkURL + "' target='_blank'>" + SDK.NAME + "</a></span>" : "")
					+ "<span style='float:right'><a id='" + this.prefix + "boxmin' class='" + this.prefix + "boxmin' onclick='return false;' href='#'><img src='" + SDK.url + "/images/minimize.png'></a> ";
		if (this.showBoxmax) {
			html = html + "<a id='" + this.prefix + "boxmax' class='" + this.prefix + "boxmax' onclick='return false;' href='#'><img src='" + SDK.url + "/images/open.png'> </a></span><br/>";
		} else {
			html = html + "</span><br/>";
		}
		html = html + "</div>";
		html = html
			+ "<div id='" + this.prefix + "online' class='" + this.prefix + "online'>"
			+ "</div>"
			+ "<div id='" + this.prefix + "scroller' class='" + this.prefix + "scroller'>"
			+ "<table id='" + this.prefix + "console' class='" + this.prefix + "console' width=100% cellspacing=2></table>"
			+ "</div>";
		html = html
			+ "<div id='" + this.prefix + "avatar-div' style='" + hideAvatar + "'>"
				+ "<div id='" + this.prefix + "avatar-image-div' style='display:none;text-align:center;" + minHeight + minAvatarWidth + "'>"
					+ "<img id='" + this.prefix + "avatar' style='" + minHeight + minAvatarWidth + "'/>"
				+ "</div>"
				+ "<div id='" + this.prefix + "avatar-video-div' style='display:none;text-align:center;background-position:center;" + divHeight + divWidth + background + "background-repeat: no-repeat;'>"
					+ "<video muted='true' id='" + this.prefix + "avatar-video' autoplay preload='auto' style='background:transparent;" + minHeight + minAvatarWidth + "'>"
						+ "Video format not supported by your browser (try Chrome)"
					+ "</video>"
				+ "</div>"
				+ "<div id='" + this.prefix + "avatar-canvas-div' style='display:none;text-align:center;" + divHeight + divWidth + "'>"
					+ "<canvas id='" + this.prefix + "avatar-canvas' style='background:transparent;" + minHeight + minAvatarWidth + "'>"
						+ "Canvas not supported by your browser (try Chrome)"
					+ "</canvas>"
				+ "</div>"
				+ "<div id='" + this.prefix + "avatar-game-div' style='display:none;text-align:center;" + divHeight + divWidth + "'>"
				+ "</div>"
			+ "</div>";
		
		var urlprefix = this.connection.credentials.url + "/";
		html = html
				+ "<div>"
					+ "<div " + (this.bubble ? "id='" + this.prefix + "bubble-div'" : "") + " " + (this.bubble ? "class='" + this.prefix + "bubble-div'" : "") + ">"
					+ "<div class='" + this.prefix + "" + (this.bubble ? "bubble" : (this.background == null ? "no-bubble" : "no-bubble-plain") )
					+ "'><div id='" + this.prefix + (this.bubble ? "bubble-text" : "no-bubble-text" ) + "' class='" + this.prefix + (this.bubble ? "bubble-text" : "no-bubble-text" ) + "'>"
						+ "<span id='" + this.prefix + "response'>" + (this.greeting == null ? this.loading : this.greeting) + "</span><br/>"
					+ "</div></div></div>"
					+ "<div><span class='" + this.prefix + "box-input-span'><input id='" + this.prefix + "chat' type='text' class='" + this.prefix + "box-input'/></span></div>";
		if (this.showMenubar) {
			html = html
				+ "<div id='" + this.prefix + "menudiv' class='" + this.prefix + "menudiv'>"
				+ "<span class='" + this.prefix + "menu'>\n"
				+ "<div style='inline-block;position:relative'>\n"
				+ "<span class='" + this.prefix + "menupopup'>"
				+ "<div style='text-align:left;bottom:28px'>"
				+ "<table>\n";
				if (this.showChooseLanguage) {
					html = html
					+ "<tr id='" + this.prefix + "chooselanguageMenu' class='" + this.prefix + "menuitem'>"
					+ "<td><a id='" + this.prefix + "boxlanguagemenu' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img class='" + this.prefix + "menu' src='" + urlprefix + "images/language.svg' title='Translate to and from your selected language'>"
						+ " <select id='" + this.prefix + "chooselanguage'>"
							+ "<option value='none'>" + SDK.translate("Choose Language") + "</option>"
							+ "<option value='en'>English</option>"
							+ "<option value='zh'>Chinese</option>"
							+ "<option value='es'>Spanish</option>"
							+ "<option value='pt'>Portuguese</option>"
							+ "<option value='de'>German</option>"
							+ "<option value='fr'>French</option>"
							+ "<option value='ja'>Japanese</option>"
							+ "<option value='ar'>Arabic</option>"
							+ "<option value='none'>None</option>"
							+ "<option value='none'></option>"
							+ "<option value='af'>Afrikaans</option>"
							+ "<option value='sq'>Albanian</option>"
							+ "<option value='hy'>Armenian</option>"
							+ "<option value='az'>Azerbaijani</option>"
							+ "<option value='ba'>Bashkir</option>"
							+ "<option value='eu'>Basque</option>"
							+ "<option value='be'>Belarusian</option>"
							+ "<option value='bn'>Bengali</option>"
							+ "<option value='bs'>Bosnian</option>"
							+ "<option value='bg'>Bulgarian</option>"
							+ "<option value='ca'>Catalan</option>"
							+ "<option value='za'>Chinese</option>"
							+ "<option value='hr'>Croatian</option>"
							+ "<option value='cs'>Czech</option>"
							+ "<option value='da'>Danish</option>"
							+ "<option value='nl'>Dutch</option>"
							+ "<option value='en'>English</option>"
							+ "<option value='et'>Estonian</option>"
							+ "<option value='fi'>Finnish</option>"
							+ "<option value='fr'>French</option>"
							+ "<option value='gl'>Galician</option>"
							+ "<option value='ka'>Georgian</option>"
							+ "<option value='de'>German</option>"
							+ "<option value='gu'>Gujarati</option>"
							+ "<option value='ht'>Haitian</option>"
							+ "<option value='he'>Hebrew</option>"
							+ "<option value='hi'>Hindi</option>"
							+ "<option value='hu'>Hungarian</option>"
							+ "<option value='id'>Indonesian</option>"
							+ "<option value='ga'>Irish</option>"
							+ "<option value='it'>Italian</option>"
							+ "<option value='ja'>Japanese</option>"
							+ "<option value='kn'>Kannada</option>"
							+ "<option value='kk'>Kazakh</option>"
							+ "<option value='ky'>Kirghiz</option>"
							+ "<option value='ko'>Korean</option>"
							+ "<option value='la'>Latin</option>"
							+ "<option value='lv'>Latvian</option>"
							+ "<option value='lt'>Lithuanian</option>"
							+ "<option value='mk'>Macedonian</option>"
							+ "<option value='mg'>Malagasy</option>"
							+ "<option value='ms'>Malay</option>"
							+ "<option value='mt'>Maltese</option>"
							+ "<option value='mn'>Mongolian</option>"
							+ "<option value='no'>Norwegian</option>"
							+ "<option value='fa'>Persian</option>"
							+ "<option value='pl'>Polish</option>"
							+ "<option value='pt'>Portuguese</option>"
							+ "<option value='pa'>Punjabi</option>"
							+ "<option value='ro'>Romanian</option>"
							+ "<option value='ru'>Russian</option>"
							+ "<option value='sr'>Serbian</option>"
							+ "<option value='si'>Sinhalese</option>"
							+ "<option value='sk'>Slovak</option>"
							+ "<option value='es'>Spanish</option>"
							+ "<option value='sw'>Swahili</option>"
							+ "<option value='sv'>Swedish</option>"
							+ "<option value='tl'>Tagalog</option>"
							+ "<option value='tg'>Tajik</option>"
							+ "<option value='ta'>Tamil</option>"
							+ "<option value='tt'>Tatar</option>"
							+ "<option value='th'>Thai</option>"
							+ "<option value='tr'>Turkish</option>"
							+ "<option value='uk'>Ukrainian</option>"
							+ "<option value='ur'>Urdu</option>"
							+ "<option value='uz'>Uzbek</option>"
							+ "<option value='cy'>Welsh</option>"
					  	+ "</select>"
					+ "</a></td>"
					+ "</tr>\n";
				}
			if (this.allowFiles) {
				if (this.showSendImage) {
					html = html
						+ "<tr id='" + this.prefix + "sendImageMenu' class='" + this.prefix + "menuitem'>"
							+ "<td><a id='" + this.prefix + "sendImage' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img class='" + this.prefix + "menu' src='" + urlprefix + "/images/image.svg' title='Resize and send an image attachment'> " + SDK.translate("Send image") + "</a></td>"
						+ "</tr>\n"
						+ "<tr id='" + this.prefix + "sendAttachmentMenu' class='" + this.prefix + "menuitem'>"
							+ "<td><a id='" + this.prefix + "sendAttachment' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img class='" + this.prefix + "menu' src='" + urlprefix + "/images/attach.svg' title='Send an image or file attachment'> " + SDK.translate("Send file") + "</a></td>"
						+ "</tr>\n";
				}
			}
			html = html
				+ "<tr id='" + this.prefix + "showChatLog' class='" + this.prefix + "menuitem'>"
					+ "<td><a class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img class='" + this.prefix + "menu' src='" + urlprefix + "/images/chat_log.svg' title='Chat log'> " + SDK.translate("Chat Log") + "</a></td>"
				+ "</tr>\n"
				+ "<tr id='" + this.prefix + "showAvatarBot' class='" + this.prefix + "menuitem' style='display:none;'>"
					+ "<td><a class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img class='" + this.prefix + "menu' src='" + urlprefix + "/images/avatar-icon.png' title='Avatar Bot'> " + SDK.translate("Show Avatar") + "</a></td>"
				+ "</tr>\n";
			
			if (this.allowSpeech) {
				html = html
					+ "<tr id='" + this.prefix + "speechMenu' class='" + this.prefix + "menuitem'>"
						+ "<td><a id='" + this.prefix + "boxspeakmenu' class='" + this.prefix + "menuitem' onclick='return false;' href='#'><img id='" + this.prefix + "boxspeak2' class='" + this.prefix + "menu' src='" + urlprefix + (this.speak ? "images/sound.svg": "images/mute.svg") +"' title='Speech'> " + SDK.translate("Speech") + "</a></td>"
					+ "</tr>\n";
			}
			if (this.allowSpeechRecognition == true || (this.allowSpeechRecognition == null && this.allowSpeech && SDK.isChrome())) {
				html = html
					+ "<tr id='" + this.prefix + "speechRecognitionMenu' class='" + this.prefix + "menuitem'>"
						+ "<td><a id='" + this.prefix + "boxspeakrecognitionmenu' class='" + this.prefix + "menuitem' onclick='return false;' href='#'>"
								+ "<img id='" + this.prefix + "boxspeakrecognition2' class='" + this.prefix + "menu' src='" + urlprefix + "/images/micoff.svg' title='Speech recognition (browser must support HTML5 speech recognition, such as Chrome)'> " + SDK.translate("Speech Recognition") + "</a>"
						+ "</td>"
					+ "</tr>\n";
			}
			html = html
				+ "</table>\n"
				+ "</div>"
				+ "<img id='" + this.prefix + "boxmenubutton' class='" + this.prefix + "boxbutton' src='" + urlprefix + "/images/menu.png'>";
				if (this.showChooseLanguage) {
					html = html + "<img id='" + this.prefix + "boxlanguage' class='" + this.prefix + "boxbutton' src='" + urlprefix + "/images/language.svg'>";
				}
				html = html + "</span>";
			if (this.allowSpeech) {
				html = html
					+ "<a '" + this.prefix + "speechButton' onclick='return false;' href='#' title='Speech'><img id='" + this.prefix + "boxspeak' class='" + this.prefix + "boxbutton' src='"
									+ urlprefix
									+ (this.speak ? "images/sound.svg": "images/mute.svg") +"'></a></td>";
			}
			if (this.allowSpeechRecognition == true || (this.allowSpeechRecognition == null && this.allowSpeech && SDK.isChrome())) {
				html = html
					+ "<a id='" + this.prefix + "speechRecognitionButton' onclick='return false;' href='#' title='Speech Recognition'><img id='" + this.prefix + "boxspeakrecognition' class='" + this.prefix + "boxbutton' src='"
									+ urlprefix
									+ "images/micoff.svg'></a>";
			}
			html = html + "<a id='" + this.prefix + "showChatLogButton' onclick='return false;' href='#' title='Show chat log'>"
					+ "<img class='" + this.prefix + "boxbutton' src='" + urlprefix + "/images/chat_log.svg' title='Chat log'></a>"
					+ "<a id='" + this.prefix + "showAvatarBotButton' style='display:none;' onclick='return false;' href='#' title='Show Avatar Bot'>"
					+ "<img class='" + this.prefix + "boxbutton' src='" + urlprefix + "/images/avatar-icon.png' title='Avatar Bot'></a>";
		}
		if (this.allowFiles && this.showFileButtons) {
			html = html
				+ "<a id='" + this.prefix + "sendImageTool' onclick='return false;' href='#'><img class='" + this.prefix + "boxbutton' src='" + urlprefix + "/images/image.svg' title='Resize and send an image attachment'></a>"
				+ "<a id='" + this.prefix + "sendAttachmentTool' onclick='return false;' href='#'><img class='" + this.prefix + "boxbutton' src='" + urlprefix + "/images/attach.svg' title='Send an image or file attachment'></a>";
		}
		html = html
			+ "</span>"
			+ "</div>"
			+ "</div>"
			+ "<div id='" + this.prefix + "yandex' class='" + this.prefix + "yandex'><span>Powered by <a target='_blank' href='http://translate.yandex.com/'>Yandex.Translate</a></span></div>"
			+ "</div>"
			+ "</div>"
			+ "<div id='" + this.prefix + "boxbar' class='" + this.prefix + "boxbar'>"
				+ "<div id='" + this.prefix + "boxbar2' class='" + this.prefix + "boxbar2'>"
					+ "<span><a id='" + this.prefix + "boxbarmax' class='" + this.prefix + "boxbarmax' " + " onclick='return false;' href='#'><img id='" + this.prefix + "boxbarmaximage' " + "src='" + SDK.url + "/images/maximizew.png'> " + this.caption + " </a>"
					+ " <a id='" + this.prefix + "boxclose' class='" + this.prefix + "boxclose' onclick='return false;' onclick='return false;' href='#'> <img src='" + SDK.url + "/images/closeg.png'></a></span><br/>"
				+ "</div>";
		html = html
				+ "<div id='" + this.prefix + "boxbar3' class='" + this.prefix + "boxbar3'" + ">"
					+ "<span><a id='" + this.prefix + "boxbarmax2' class='" + this.prefix + "boxbarmax2' " + (this.forceStyles ? "style='color:white' " : "") + " onclick='return false;' href='#'>" + "</a></span><br>"
					+ " <a id='" + this.prefix + "boxclose2' class='" + this.prefix + "boxclose2' onclick='return false;' onclick='return false;' href='#'> <img src='" + SDK.url + "/images/closeg.png'></a></span><br/>"
				+ "</div>\n"
			+ "</div>\n";
		
		if (this.promptContactInfo) {
			html = html
				+ "<div id='" + this.prefix + "contactinfo' class='" + this.prefix + "box' " + backgroundstyle + ">"
					+ "<div class='" + this.prefix + "boxmenu'>"
						+ "<span style='float:right'><a id='" + this.prefix + "contactboxmin' class='" + this.prefix + "contactboxmin' onclick='return false;' href='#'><img src='" + SDK.url + "/images/minimize.png'></a>"
					+ "</div>\n"
					+ "<div style='margin:10px'>\n"
						+ "<span>" + SDK.translate("Name") + "</span><br/><input id='" + this.prefix + "contactname' type='text' /><br/>\n"
						+ "<span>" + SDK.translate("Email") + "</span><br/><input id='" + this.prefix + "contactemail' type='email' /><br/>\n"
						+ "<span>" + SDK.translate("Phone") + "</span><br/><input id='" + this.prefix + "contactphone' type='text' /><br/>\n"
						+ "<br/><a id='" + this.prefix + "contactconnect' class='" + this.prefix + "contactconnect' " + (this.forceStyles ? "style='color:white' " : "") + " onclick='return false;' href='#'>" + SDK.translate("Connect") + "</a>\n"
					+ "<br/><br/></div>\n"
				+ "</div>";
		}
		
		box.innerHTML = html;
		SDK.body().appendChild(box);
		
		var self = this;
		document.getElementById(this.prefix + "chat").addEventListener("keypress", function(event) {
			if (event.keyCode == 13) {
				self.sendMessage();
				return false;
			}
		});
		document.getElementById(this.prefix + "boxclose").addEventListener("click", function() {
			self.closeBox();
			return false;
		});
		document.getElementById(this.prefix + "boxclose2").addEventListener("click", function() {
			self.closeBox();
			return false;
		});
		document.getElementById(this.prefix + "boxmin").addEventListener("click", function() {
			self.minimizeBox();
			return false;
		});
		if (this.promptContactInfo) {
			document.getElementById(this.prefix + "contactboxmin").addEventListener("click", function() {
				self.minimizeBox();
				return false;
			});
			document.getElementById(this.prefix + "contactconnect").addEventListener("click", function() {
				self.contactConnect();
				return false;
			});
		}
		if (document.getElementById(this.prefix + "boxmax") != null) {
			document.getElementById(this.prefix + "boxmax").addEventListener("click", function() {
				self.popup();
				return false;
			});
		}
		document.getElementById(this.prefix + "boxbarmax").addEventListener("click", function() {
			self.maximizeBox();
			return false;
		});
		
		document.getElementById(this.prefix + "boxbarmax2").addEventListener("click", function() {
			self.maximizeBox();
			return false;
		});
		
		var langOrig = null;
		var nativeVoiceOrig = null;
		var voiceOrig = null;
		if (document.getElementById(this.prefix + "chooselanguage") != null) {
			document.getElementById(this.prefix + "chooselanguage").addEventListener("change", function() {
				if (nativeVoiceOrig == null && langOrig == null) {
					langOrig = self.lang;
					nativeVoiceOrig = self.nativeVoice;
					voiceOrig = self.voice;
				}
				var element = document.getElementById(self.prefix + 'chooselanguage');
				self.lang = element.value;
				if (self.lang != "none") {
					document.getElementById(self.prefix + 'yandex').style.display = "inline";
					self.nativeVoice = true;
					self.translate = true;
					self.voice = null;
				} else {
					document.getElementById(self.prefix + 'yandex').style.display = "none";
					self.translate = false;
					self.lang = langOrig;
					self.nativeVoice = nativeVoiceOrig;
					self.voice = voiceOrig;
				}
			});
		}
		if (document.getElementById(this.prefix + "sendImage") != null) {
			document.getElementById(this.prefix + "sendImage").addEventListener("click", function() {
				self.sendImage();
				return false;
			});
		}
		if (document.getElementById(this.prefix + "sendAttachment") != null) {
			document.getElementById(this.prefix + "sendAttachment").addEventListener("click", function() {
				self.sendAttachment();
				return false;
			});
		}
		if (document.getElementById(this.prefix + "sendImageTool") != null) {
			document.getElementById(this.prefix + "sendImageTool").addEventListener("click", function() {
				self.sendImage();
				return false;
			});
		}
		if (document.getElementById(this.prefix + "sendAttachmentTool") != null) {
			document.getElementById(this.prefix + "sendAttachmentTool").addEventListener("click", function() {
				self.sendAttachment();
				return false;
			});
		}

		if (this.avatar && this.chatLog) {
			document.getElementById(this.prefix + "online").style.display = "none";
			document.getElementById(this.prefix + "scroller").style.display = "none";
			if (this.version >= 6.0 && this.prefix == "botplatform") {
				var chatLogDiv = document.getElementById(this.prefix + "showChatLog");
				if (chatLogDiv != null) {
					chatLogDiv.style.display = "block";
				}
				var chatLogButtonDiv = document.getElementById(this.prefix + "showChatLogButton");
				if (chatLogButtonDiv != null) {
					chatLogButtonDiv.style.display = "inline-block";
				}
			}
		} else if (this.avatar && !this.chatLog) {
			document.getElementById(this.prefix + "online").style.display = "none";
			document.getElementById(this.prefix + "scroller").style.display = "none";
			var chatLogDiv = document.getElementById(this.prefix + "showChatLog");
			var chatLogButtonDiv = document.getElementById(this.prefix + "showChatLogButton");
			if (chatLogDiv != null) {
				chatLogDiv.style.display = "none";
			}
			if (chatLogButtonDiv != null) {
				chatLogButtonDiv.style.display = "none";
			}
		} else if (!this.avatar && this.chatLog) {
			document.getElementById(this.prefix + "online").style.display = "inline";
			document.getElementById(this.prefix + "scroller").style.display = "inline-block";
			document.getElementById(this.prefix + "avatar-div").style.display = "none";
			var chatLogDiv = document.getElementById(this.prefix + "showChatLog");
			var chatLogButtonDiv = document.getElementById(this.prefix + "showChatLogButton");
			if (chatLogDiv != null) {
				chatLogDiv.style.display = "none";
			}
			if (chatLogButtonDiv != null) {
				chatLogButtonDiv.style.display = "none";
			}
			var bubbleDiv = document.getElementById(this.prefix + "bubble-div");
			if (bubbleDiv != null) {
				bubbleDiv.style.display = "none";	
			}
			var noBubblePlain = document.getElementsByClassName(this.prefix + "no-bubble-plain");
			if (noBubblePlain != null && noBubblePlain.length != 0) {
				noBubblePlain[0].style.display = "none";
			}
		} else {
			document.getElementById(this.prefix + "online").style.display = "none";
			document.getElementById(this.prefix + "scroller").style.display = "none";
			document.getElementById(this.prefix + "avatar-div").style.display = "none";
			var chatLogDiv = document.getElementById(this.prefix + "showChatLog");
			var chatLogButtonDiv = document.getElementById(this.prefix + "showChatLogButton");
			if (chatLogDiv != null) {
				chatLogDiv.style.display = "none";
			}
			if (chatLogButtonDiv != null) {
				chatLogButtonDiv.style.display = "none";
			}
		}
		
		if (document.getElementById(this.prefix + "showChatLog") != null) {
			document.getElementById(this.prefix + "showChatLog").addEventListener("click", function() {
				self.showChatLog();
				return false;
			});
		}
		if (document.getElementById(this.prefix + "showChatLogButton") != null) {
			document.getElementById(this.prefix + "showChatLogButton").addEventListener("click", function() {
				self.showChatLog();
				return false;
			});
		}
		if (document.getElementById(this.prefix + "showAvatarBot") != null) {
			document.getElementById(this.prefix + "showAvatarBot").addEventListener("click", function() {
				self.showAvatarBot();
				return false;
			});
		}
		if (document.getElementById(this.prefix + "showAvatarBotButton") != null) {
			document.getElementById(this.prefix + "showAvatarBotButton").addEventListener("click", function() {
				self.showAvatarBot();
				return false;
			});
		}
		
		if (document.getElementById(this.prefix + "boxspeak") != null) {
			document.getElementById(this.prefix + "boxspeak").addEventListener("click", function() {
				self.speak = !self.speak;
				var urlprefix = self.connection.credentials.url + "/";
				if (self.speak) {
					SDK.initAudio();
					document.getElementById(self.prefix + "boxspeak").src = urlprefix + "images/sound.svg";
					document.getElementById(self.prefix + "boxspeak2").src = urlprefix + "images/sound.svg";
				} else {
					document.getElementById(self.prefix + "boxspeak").src = urlprefix + "images/mute.svg";
					document.getElementById(self.prefix + "boxspeak2").src = urlprefix + "images/mute.svg";
				}
				return false;
			});
			document.getElementById(this.prefix + "boxspeakmenu").addEventListener("click", function() {
				self.speak = !self.speak;
				var urlprefix = self.connection.credentials.url + "/";
				if (self.speak) {
					SDK.initAudio();
					document.getElementById(self.prefix + "boxspeak").src = urlprefix + "images/sound.svg";
					document.getElementById(self.prefix + "boxspeak2").src = urlprefix + "images/sound.svg";
				} else {
					document.getElementById(self.prefix + "boxspeak").src = urlprefix + "images/mute.svg";
					document.getElementById(self.prefix + "boxspeak2").src = urlprefix + "images/mute.svg";
				}
				return false;
			});
		}
		if (document.getElementById(this.prefix + "boxspeakrecognition") != null) {
			SDK.registerSpeechRecognition(document.getElementById(self.prefix + 'chat'), function() {
				self.sendMessage();
			});
			document.getElementById(this.prefix + "boxspeakrecognition").addEventListener("click", function() {
				self.listen = !self.listen;
				if (self.listen) {
					SDK.startSpeechRecognition();
					document.getElementById(self.prefix + 'boxspeakrecognition').src = urlprefix + "images/mic.svg";
					document.getElementById(self.prefix + 'boxspeakrecognition2').src = urlprefix + "images/mic.svg";
				} else {
					SDK.stopSpeechRecognition();
					document.getElementById(self.prefix + 'boxspeakrecognition').src = urlprefix + "images/micoff.svg";
					document.getElementById(self.prefix + 'boxspeakrecognition2').src = urlprefix + "images/micoff.svg";
				}
			});
			document.getElementById(this.prefix + "boxspeakrecognitionmenu").addEventListener("click", function() {
				self.listen = !self.listen;
				if (self.listen) {
					SDK.startSpeechRecognition();
					document.getElementById(self.prefix + 'boxspeakrecognition').src = urlprefix + "images/mic.svg";
					document.getElementById(self.prefix + 'boxspeakrecognition2').src = urlprefix + "images/mic.svg";
				} else {
					SDK.stopSpeechRecognition();
					document.getElementById(self.prefix + 'boxspeakrecognition').src = urlprefix + "images/micoff.svg";
					document.getElementById(self.prefix + 'boxspeakrecognition2').src = urlprefix + "images/micoff.svg";
				}
			});
		}
	}
	
	this.showChatLog = function() {
		document.getElementById(this.prefix + "avatar-div").style.display = "none";
		document.getElementById(this.prefix + "online").style.display = "inline";
		var bubbleDiv = document.getElementById(this.prefix + "bubble-div");
		if (bubbleDiv != null) {
			bubbleDiv.style.display = "none";
		}
		var noBubblePlain = document.getElementsByClassName(this.prefix + "no-bubble-plain");
		if (noBubblePlain != null && noBubblePlain.length != 0) {
			noBubblePlain[0].style.display = "none";
		}
		document.getElementById(this.prefix + "scroller").style.display = "inline-block";
		if (this.version >= 6.0 && this.prefix == "botplatform") {
			document.getElementById(this.prefix + "showChatLog").style.display = "none";
			document.getElementById(this.prefix + "showChatLogButton").style.display = "none";
			document.getElementById(this.prefix + "showAvatarBot").style.display = "block";
			document.getElementById(this.prefix + "showAvatarBotButton").style.display = "inline-block";
		}
		if (this.background == null && this.backgroundIfNotChrome) {
			var box = document.getElementById(this.prefix + "box");
			if (box != null) {
				box.style.backgroundColor = "#fff";
			}
		}
	}
	
	this.showAvatarBot  = function() {
		document.getElementById(this.prefix + "online").style.display = "none";
		document.getElementById(this.prefix + "scroller").style.display = "none";
		document.getElementById(this.prefix + "avatar-div").style.display = "inline-block";
		var bubbleDiv = document.getElementById(this.prefix + "bubble-div");
		if (bubbleDiv != null) {
			bubbleDiv.style.display = "inherit";
		}
		var noBubblePlain = document.getElementsByClassName(this.prefix + "no-bubble-plain");
		if (noBubblePlain != null && noBubblePlain.length != 0) {
			noBubblePlain[0].style.display = "inherit";
		}
		if (this.version >= 6.0 && this.prefix == "botplatform") {
			document.getElementById(this.prefix + "showChatLog").style.display = "block";
			document.getElementById(this.prefix + "showChatLogButton").style.display = "inline-block";
			document.getElementById(this.prefix + "showAvatarBot").style.display = "none";
			document.getElementById(this.prefix + "showAvatarBotButton").style.display = "none";
		}
		if (this.background == null && this.backgroundIfNotChrome) {
			var box = document.getElementById(this.prefix + "box");
			if (box != null) {
				box.style.backgroundColor = null;
				box.style.border = null;
			}
		}
	}
	
	/**
	 * Create a live chat bar beside the bot bar.
	 */
	this.createLiveChatBox = function(channel, label, position) {
		var box = document.createElement('div');
		var buttonstyle = "";
		if (this.color != null) {
			buttonstyle = "background-color:" + this.color + ";";
		}
		var buttonHoverStyle = "";
		if (this.hoverColor != null) {
			buttonHoverStyle = "background-color:" + this.hoverColor + ";";
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
				+ "." + this.prefix + this.livechatPrefix + "boxbar { position:fixed;bottom:2px;" + position + ";z-index:152;margin:0;padding:6px;" + buttonstyle + " }\n"
				+ "." + this.prefix + this.livechatPrefix + "boxbar:hover { " + buttonHoverStyle + " }\n"
				+ (this.forceStyles ? "#" : ".") + this.prefix + this.livechatPrefix + "boxmax { color:white;font-size:18px;margin:2px;padding:0px;text-decoration:none; }\n"
			+ "</style>\n"
			+ "<div id='" + this.prefix + this.livechatPrefix + "boxbar' class='" + this.prefix + this.livechatPrefix + "boxbar'>"
			+ "<span><a id='" + this.prefix + this.livechatPrefix + "boxmax' class='" + this.prefix + this.livechatPrefix + "boxmax' onclick='return false;' href='#'>" + label + "</a></span>"
			+ "</div>";
		
		box.innerHTML = html;
		SDK.body().appendChild(box);
		
		document.getElementById(this.prefix + this.livechatPrefix + "boxmax").addEventListener("click", function() {
			SDK.popupwindow(SDK.url + '/livechat?id=' + channel + '&embedded&chat','child', 700, 520);
			return false;
		});
	}
	
	/**
	 * Minimize the embedding div in the current webpage.
	 */
	this.minimizeBox = function() {
		if (this.promptContactInfo) {
			document.getElementById(this.prefix + "contactinfo").style.display = 'none';
		}
		document.getElementById(this.prefix + "box").style.display = 'none';
		document.getElementById(this.prefix + "boxbar").style.display = 'inline';
		var livechatbot = document.getElementById(this.prefix + this.livechatPrefix + "boxbar");
		if (livechatbot != null) {
			livechatbot.style.display = 'inline';
		}
		this.exit();
		return false;
	}

	/**
	 * Check contact info and connect.
	 */
	this.contactConnect = function() {
		this.hasContactInfo = true;
		this.contactName = document.getElementById(this.prefix + "contactname").value;
		var ok = true;
		if (this.contactName != null && this.contactName == "") {
			ok = false;
			document.getElementById(this.prefix + "contactname").style.borderColor = "red";
			document.getElementById(this.prefix + "contactname").placeholder = "Enter name";
		}
		this.contactEmail = document.getElementById(this.prefix + "contactemail").value;
		if (this.contactEmail != null && this.contactEmail.indexOf("@") == -1) {
			ok = false;
			document.getElementById(this.prefix + "contactemail").style.borderColor = "red";
			document.getElementById(this.prefix + "contactemail").placeholder = "Enter valid email";
		}
		this.contactPhone = document.getElementById(this.prefix + "contactphone").value;
		this.contactInfo = this.contactName + " " + this.contactEmail + " " + this.contactPhone;
		if (ok) {
			this.maximizeBox();
		}
	}
	
	/**
	 * Maximize the embedding div in the current webpage.
	 */
	this.maximizeBox = function() {
		SDK.initAudio();
		if (this.promptContactInfo && !this.hasContactInfo) {
			document.getElementById(this.prefix + "contactinfo").style.display = 'inline';
			document.getElementById(this.prefix + "boxbar").style.display = 'none';
			document.getElementById(this.prefix + "box").style.display = 'none';
			var livechatbot = document.getElementById(this.prefix + this.livechatPrefix + "boxbar");
			if (livechatbot != null) {
				livechatbot.style.display = 'none';
			}
		} else {
			if (this.promptContactInfo) {
				document.getElementById(this.prefix + "contactinfo").style.display = 'none';
			}
			document.getElementById(this.prefix + "boxbar").style.display = 'none';
			document.getElementById(this.prefix + "box").style.display = 'inline';
			var livechatbot = document.getElementById(this.prefix + this.livechatPrefix + "boxbar");
			if (livechatbot != null) {
				livechatbot.style.display = 'none';
			}
			this.greet();
		}
		return false;
	}
	
	/**
	 * Close the embedding div in the current webpage.
	 */
	this.closeBox = function() {
		if (this.promptContactInfo) {
			document.getElementById(this.prefix + "contactinfo").style.display = 'none';
		}
		document.getElementById(this.prefix + "boxbar").style.display = 'none';
		document.getElementById(this.prefix + "box").style.display = 'none';
		var livechatbot = document.getElementById(this.prefix + this.livechatPrefix + "boxbar");
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
		var box = document.getElementById(this.prefix + "box");
		if (box != null) {
			box.style.display = 'none';
		}
		var speech = this.speak;
		if (!this.allowSpeech) {
			speech = "disable";
		}
		var height = 520;
		if (!this.avatar && !this.chatLog) {
			height = 260;
		}
		var width = 700;
		if (this.debug) {
			width = 900;
		}
		if (this.popupURL != null) {
			var popupURL = this.popupURL;
			if (popupURL.indexOf("chat?") != -1) {
				if (this.contactInfo != null && this.contactInfo != "") {
					popupURL = popupURL + "&info=" + encodeURI(this.contactInfo);
				}
				if (this.translate == true && this.lang != null && this.lang != "") {
					popupURL = popupURL + "&translate=" + encodeURI(this.lang);
				}
			}
			SDK.popupwindow(popupURL, 'child', width, height);
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
			
			if (this.debug) {
				input = document.createElement('input');
				input.type = 'hidden';
				input.name = "debug";
				input.value = "debug";
				form.appendChild(input);
			}
 
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
			input.type = "hidden";
			input.name = "bubble";
			input.value = this.bubble;
			form.appendChild(input);
 
			if (this.css != null) {
				input = document.createElement('input');
				input.type = "hidden";
				input.name = "css";
				input.value = this.css;
				form.appendChild(input);
			}
			
			var input = document.createElement('input');
			input.type = 'hidden';
			input.name = "language";
			input.value = SDK.lang;
			form.appendChild(input);
			
			if (this.translate == true && this.lang != null && this.lang != "") {
				var input = document.createElement('input');
				input.type = 'hidden';
				input.name = "translate";
				input.value = this.lang;
				form.appendChild(input);
			}
			
			input = document.createElement('input');
			input.type = 'hidden';
			input.name = "info";
			input.value = this.contactInfo;
			form.appendChild(input);
 
			input = document.createElement('input');
			input.type = 'hidden';
			input.name = "application";
			input.value = this.connection.credentials.applicationId;
			form.appendChild(input);
			
			SDK.body().appendChild(form);
			
			SDK.popupwindow('','child', width, height);
			
			form.submit();
			SDK.body().removeChild(form);
		}
		this.minimizeBox();
		return false;
	}

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
			self.sendFileAttachment(file, true, form);
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
			self.sendFileAttachment(file, false, form);
		}
		fileInput.click();
		return false;
	};

	this.sendFileAttachment = function(file, resize, form) {
		var self = this;
		var media = new MediaConfig();
		if (this.instance == null) {
			this.connection.error("Missing instance property");
			return false;
		}
		media.instance = this.instance;
		media.name = file.name;
		media.type = file.type;
		if (!resize && file.size > SDK.MAX_FILE_UPLOAD) {
			this.connection.error("File exceeds maximum upload size of " + (SDK.MAX_FILE_UPLOAD / 1000000) + "meg");
		} else {
			this.connection.createBotAttachment(media, file, resize, form, function(media) {
				//var message = "file: " + file.name + " : " + file.type + " : " + self.connection.fetchLink(media.file);
				var message = self.connection.fetchLink(media.file);
				message = document.getElementById(self.prefix + 'chat').value + " " + message;
				document.getElementById(self.prefix + 'chat').value = message;
				if (self.sendFiles) {
					self.sendMessage();
				}
			})
		}
		return false;
	};
	
	/**
	 * Search for link using <a href="chat:yes">...
	 * Switch them to use an onclick to post the chat back to the bot.
	 */
	this.linkChatPostbacks = function(node) {
		var self = this;
		var links = node.getElementsByTagName("a");
		for (var index = 0; index < links.length; index++) {
			var a = links[index];
			var href = a.getAttribute("href");
			if (href != null && href.indexOf("chat:") != -1) {
				var chat = href.substring("chat:".length, href.length).trim();
				var temp = function(param, element) {
					element.onclick = function() {
						self.sendMessage(param);
						return false;
					};
				}
				temp(chat, a);
			}
		}
		var buttons = node.getElementsByTagName("button");
		for (var index = 0; index < buttons.length; index++) {
			var button = buttons[index];
			if (button.parentNode.nodeName == "A") {
				continue;
			}
			var chat = button.textContent;
			if (chat != null && chat.length > 0) {
				var temp = function(param, element) {
					element.onclick = function() {
						self.sendMessage(param);
						return false;
					};
				}
				temp(chat, button);
			}
		}
		var choices = node.getElementsByTagName("select");
		for (var index = 0; index < choices.length; index++) {
			var choice = choices[index];
			var temp = function(param) {
				param.addEventListener("change", function() {
					self.sendMessage(param.value);
					return false;
				});
			}
			temp(choice);
		}
	}
	
	/**
	 * A chat message was received from the bot.
	 */
	this.response = function(user, message) {
		var responseDiv = document.getElementById(this.prefix + 'response');
		if (responseDiv != null) {
			responseDiv.innerHTML = SDK.linkURLs(message);
			this.linkChatPostbacks(responseDiv);
			if (!SDK.secure) {
				SDK.evalScripts(responseDiv);
			}
			this.message(user, message);
			if (this.focus) {
				var chat = document.getElementById(this.prefix + 'chat');
				if (chat != null) {
					chat.focus();
				}
			}
		}
		if (this.onresponse != null) {
			this.onresponse(message);
		}
		if (responseDiv != null) {
			var self = this;
			// Fix Chrome bug,
			if (SDK.fixChromeResizeCSS && SDK.isChrome()) {
				var padding = responseDiv.parentNode.parentNode.style.padding;
				responseDiv.parentNode.parentNode.style.padding = "7px";
				setTimeout(function() {
					responseDiv.parentNode.parentNode.style.padding = padding;
				}, 10);
			}
		}
	}
	
	/**
	 * A chat message was received from the bot.
	 */
	this.message = function(user, message) {
		var speaker = user;
		var scroller = document.getElementById(this.prefix + 'scroller');
		var chatconsole = document.getElementById(this.prefix + 'console');
		if (scroller == null || chatconsole == null) {
			return;
		}
		var tr = document.createElement('tr');
		tr.style.verticalAlign = "top";
		var td = document.createElement('td');
		var td2 = document.createElement('td');
		var div = document.createElement('div');
		var div2 = document.createElement('div');
		var span = document.createElement('span');
		var span2 = document.createElement('span');
		var br = document.createElement('br');
		var chatClass = this.prefix + 'chatchat-1';
		div.className = this.prefix + 'chatchat-1-div';
		div2.className = this.prefix + 'chatchat-1-div-2';
		span.className = this.prefix + 'chatchat-user-1';
		td.className = this.prefix + 'chat-user-1';
		var userImg;
		if (speaker != this.userThumb.name) {
			userImg = document.createElement('img');
			userImg.className = this.prefix + 'chat-user';
			userImg.setAttribute('alt', this.botThumb.name);
			userImg.setAttribute('src', this.botThumb.avatar);
			td.appendChild(userImg);
		} else {
			td.className = this.prefix + 'chat-user-2';
			chatClass = this.prefix + 'chatchat-2';
			div.className = this.prefix + 'chatchat-2-div';
			div2.className = this.prefix + 'chatchat-2-div-2';
			span.className = this.prefix + 'chatchat-user-2';
			userImg = document.createElement('img');
			userImg.className = this.prefix + 'chat-user';
			userImg.setAttribute('alt', this.userThumb.name);
			userImg.setAttribute('src', this.userThumb.avatar);
			td.appendChild(userImg);
		}
		td.setAttribute('nowrap', 'nowrap');
		td2.className = chatClass;
		td2.setAttribute('align', 'left');
		td2.setAttribute('width', '100%');
		var date = new Date(); 
		var time = date.getHours() + ":" + ((date.getMinutes() < 10)? "0" : "") + date.getMinutes() + ":" + ((date.getSeconds() < 10)? "0" : "") + date.getSeconds();
		span.innerHTML = speaker + " <small>" + time + "</small>";
		span2.className = chatClass;
		span2.innerHTML = SDK.linkURLs(message);
		this.linkChatPostbacks(span2);
		chatconsole.appendChild(tr);
		tr.appendChild(td);
		tr.appendChild(td2);
		div.appendChild(span);
		div.appendChild(br);
		div.appendChild(div2);
		td2.appendChild(div);
		div2.appendChild(span2);
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
		SDK.updateAvatar(responseMessage, this.speak, urlprefix, this.prefix, null, null, this.nativeVoice, this.lang, this.nativeVoiceName);
		if (SDK.commands && responseMessage.command != null) {
			var command = JSON.parse(responseMessage.command);
			if (command.start != null) {
				this.game = new window [command.start]();
				this.initGame(this.game)
			} else if (command.type == "web" && command.src != null) {
				var webpreview = document.getElementById(this.prefix + "webpreview");
				if (webpreview != null) {
					webpreview.style.display = "inline-block";
				}
				var webframe = document.getElementById(this.prefix + "webpreview-iframe");
				if (webframe != null) {
					webframe.style.display = "inline-block";
					webframe.src = command.src;
				}
				var webimage = document.getElementById(this.prefix + "webpreview-image");
				if (webimage != null) {
					webimage.style.display = "none";
				}
			} else if (command.type == "image" && command.src != null) {
				var webpreview = document.getElementById(this.prefix + "webpreview");
				if (webpreview != null) {
					webpreview.style.display = "inline-block";
				}
				var webframe = document.getElementById(this.prefix + "webpreview-iframe");
				if (webframe != null) {
					webframe.style.display = "none";
				}
				var webimage = document.getElementById(this.prefix + "webpreview-image");
				if (webimage != null) {
					webimage.style.display = "block";
					webimage.src = command.src;
				}
			}
		}
		if (this.game != null) {
			this.game.updateAvatar(responseMessage);
		}
		console.log(responseMessage);
	};
	
	this.initGame = function(game) {
		this.game = game;
		game.init(this);
	}
	
	this.toggleSpeak = function() {
		this.speak = !this.speak;
	}
	
	/**
	 * Initialize the bot listener.
	 */
	this.start = function() {
		if (this.prefix == "" && this.elementPrefix != null) {
			this.prefix = this.elementPrefix;
		}
		var self = this;
		this.connection.error = function(message) {
			self.response("Error", message);
		}
		if (this.avatar) {
			var config = new ChatConfig();
			config.instance = this.instance;
			if (this.translate) {
				config.language = this.lang;
			}
			if (this.format != null) {
				config.avatarFormat = this.format;
			}
			if (this.hd != null) {
				config.avatarHD = this.hd;
			}
			this.connection.initChat(config, function(responseMessage) {
				if (this.conversation == null) {
					self.updateAvatar(responseMessage);
				}
			});
		}
		if (self.userThumb['name'] == null) {
			if (this.connection.user != null && this.connection.user.user != null) {
				this.connection.viewUser(this.connection.user, function(user) {
					var urlprefix = self.connection.credentials.url + "/";
					var userName = user.user;
					var userAvatar = user.avatar;
					self.userThumb['name'] = userName;
					self.userThumb['avatar'] = urlprefix + userAvatar;
				});
			} else {
				var urlprefix = this.connection.credentials.url + "/";
				self.userThumb['name'] = "You:";
				self.userThumb['avatar'] = urlprefix + "images/user-thumb.jpg";
			}
		}
		var instanceConfig = new InstanceConfig();
		instanceConfig.id = this.instance;
		this.connection.fetch(instanceConfig, function(instanceConfig) {
			var botName = instanceConfig.name;
			var botAvatar = instanceConfig.avatar;
			var urlprefix = self.connection.credentials.url + "/";
			self.botThumb['name'] = botName;
			self.botThumb['avatar'] = urlprefix + botAvatar;
			var onlineDiv = document.getElementById(self.prefix + 'online');
			if (onlineDiv == null) {
				return;
			}
			if (!onlineDiv.hasChildNodes()) {
				var div = document.createElement('div');
				div.className = self.prefix + "online-user";
				var botImage = document.createElement('img');
				botImage.className = self.prefix + "chat-bot";
				botImage.setAttribute('alt', self.botThumb.name);
				botImage.setAttribute('src', self.botThumb.avatar);
				var span = document.createElement('span');
				span.className = self.prefix + "user-bot";
				span.innerHTML = self.botThumb.name;
				div.appendChild(botImage);
				div.appendChild(span);
				onlineDiv.appendChild(div);
				var line = document.createElement('hr');
				onlineDiv.appendChild(line);
			}
		});
	}
	
	/**
	 * Send the bot an empty message to let it greet the user.
	 * This will have the bot respond with any defined greeting it has.
	 */
	this.greet = function() {
		this.start();
		var chat = new ChatConfig();
		chat.info = this.contactInfo;
		chat.instance = this.instance;
		if (this.greetingMessage != null) {
			chat.message = this.greetingMessage;
		}
		if (this.translate) {
			chat.language = this.lang;
		}
		if (this.avatarId != null) {
			chat.avatar = this.avatarId;
		}
		if (this.hd != null) {
			chat.avatarHD = this.hd;
		} else if ((this.width != null && this.width > 400) || (this.height != null && this.height > 400)) {
			chat.avatarHD = true;
		}
		if (this.format != null) {
			chat.avatarFormat = this.format;
		}
		if (this.nativeVoice && SDK.speechSynthesis) {
			chat.speak = false;
		} else {
			chat.speak = this.speak;
		}
		var self = this;
		if (this.external) {
			this.externalChat(chat);
		} else {
			this.connection.chat(chat, function(responseMessage) {
				self.conversation = responseMessage.conversation;
				self.updateAvatar(responseMessage);
				if (responseMessage.message != null && responseMessage.message != "") {
					self.response(self.instanceName, responseMessage.message);
				} else if (self.greeting == null) {
					document.getElementById(self.prefix + 'response').innerHTML = "Hi";
				}
			});
		}
		return false;
	};
	
	/**
	 * Send the current text from the chat input as a message to the bot, and process the response.
	 */
	this.sendMessage = function(message) {
		if (message == null) {
			var chat = document.getElementById(this.prefix + 'chat');
			if (chat != null) {
				message = chat.value;
			}
		}
		if (message != '') {
			this.message(this.userThumb.name, message);
			var chat = new ChatConfig();
			chat.message = message;
			chat.instance = this.instance;
			if (this.translate) {
				chat.language = this.lang;
			}
			if (this.avatarId != null) {
				chat.avatar = this.avatarId;
			}
			if (this.hd != null) {
				chat.avatarHD = this.hd;
			} else if ((this.width != null && this.width > 400) || (this.height != null && this.height > 400)) {
				chat.avatarHD = true;
			}
			if (this.format != null) {
				chat.avatarFormat = this.format;
			}
			if (this.nativeVoice && SDK.speechSynthesis) {
				chat.speak = false;
			} else {
				chat.speak = this.speak;
			}
			chat.conversation = this.conversation;
			var correction = document.getElementById('correction');
			if (correction != null && correction.checked) {
				chat.correction = true;
				correction.checked = false;
			}
			var learning = document.getElementById('learning');
			if (learning != null && learning.style.display != "none") {
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
			var responseDiv = document.getElementById(this.prefix + 'response');
			if (responseDiv != null) {
				responseDiv.innerHTML = '<i>thinking</i>';
			}
			var chatInput = document.getElementById(this.prefix + 'chat');
			if (chatInput != null) {
				chatInput.value = '';
			}
			var self = this;
			if (this.external) {
				this.externalChat(chat);
			} else {
				this.connection.chat(chat, function(responseMessage) {
					self.conversation = responseMessage.conversation;
					self.response(self.instanceName, responseMessage.message);
					self.updateAvatar(responseMessage);
					var log = document.getElementById('log');
					var logText = responseMessage.log;
					if (log != null) {
						if (logText != null) {
							log.style.display = "inline-block";
							log.innerHTML = logText;
						} else {
							log.innerHTML = "";
						}
					}
				});
			}
		}
		return false;
	};
	
	/**
	 * Send the json command to the bot, and process the response.
	 */
	this.sendCommand = function(json, processor) {
		if (json != '') {
			var command = new CommandConfig();
			command.command = json;
			command.instance = this.instance;
			if (this.translate) {
				command.language = this.lang;
			}
			if (this.avatarId != null) {
				command.avatar = this.avatarId;
			}
			if (this.hd != null) {
				command.avatarHD = this.hd;
			} else if ((this.width != null && this.width > 400) || (this.height != null && this.height > 400)) {
				command.avatarHD = true;
			}
			if (this.format != null) {
				command.avatarFormat = this.format;
			}
			if (this.nativeVoice && SDK.speechSynthesis) {
				command.speak = false;
			} else {
				command.speak = this.speak;
			}
			command.conversation = this.conversation;
			var correction = document.getElementById('correction');
			if (correction != null && correction.checked) {
				command.correction = true;
				correction.checked = false;
			}
			var learning = document.getElementById('learning');
			if (learning != null && learning.style.display != "none") {
				command.learn = learning.checked;
			}
			var debug = document.getElementById('debug');
			if (debug != null && debug.checked) {
				command.debug = true;
				var debugLevel = document.getElementById('debugLevel');
				if (debugLevel != null) {
					chat.debugLevel = debugLevel.value;
				}
			}
			var offensive = document.getElementById('offensive');
			if (offensive != null && offensive.checked) {
				command.offensive = true;
				offensive.checked = false;
			}
			var emote = document.getElementById('emote');
			if (emote != null && emote.value != null && emote.value != "" && emote.value != "NONE") {
				command.emote = emote.value.toUpperCase();
				emote.value = "NONE";
			}
			var action = document.getElementById('action');
			if (action != null && action.value != null && action.value != "") {
				command.action = action.value;
				action.value = "";
			}
			var responseDiv = document.getElementById(this.prefix + 'response');
			if (responseDiv != null) {
				responseDiv.innerHTML = '<i>thinking</i>';
			}
			var chatInput = document.getElementById(this.prefix + 'chat');
			if (chatInput != null) {
				chatInput.value = '';
			}
			var self = this;
			if (this.external) {
				return false;
			} else {
				this.connection.command(command, function(responseMessage) {
					self.conversation = responseMessage.conversation;
					self.response(self.instanceName, responseMessage.message);
					self.updateAvatar(responseMessage);
					var log = document.getElementById('log');
					var logText = responseMessage.log;
					if (log != null) {
						if (logText != null) {
							log.style.display = "inline-block";
							log.innerHTML = logText;
						} else {
							log.innerHTML = "";
						}
					}
					processor(responseMessage.command);
				});
			}
		}
		return false;
	};
	
	/**
	 * Send an external API chat request.
	 */
	this.externalChat = function(chat) {
		var url = this.apiURL;
		if (chat.message == null) {
			url = url.replace(":message", "");			
		} else {
			url = url.replace(":message", encodeURIComponent(chat.message));
		}
		if (chat.conversation == null) {
			url = url.replace(":conversation", "");
		} else {
			url = url.replace(":conversation", encodeURIComponent(chat.conversation));
		}
		if (chat.speak) {
			url = url.replace(":speak", "true");
		} else {
			url = url.replace(":speak", "");
		}
		var self = this;
		this.connection.GET(url, function(xml) {
			if (xml == null) {
				return null;
			}
			var responseMessage = new ChatResponse();
			responseMessage.parseXML(xml);
			self.conversation = responseMessage.conversation;
			self.response(self.instanceName, responseMessage.message);
			var urlprefix = self.apiURL.substring(0, self.apiURL.indexOf("/rest/api/form-chat")) + "/";
			SDK.updateAvatar(responseMessage, self.speak, urlprefix, self.prefix, null, null, self.nativeVoice, self.lang, self.nativeVoiceName);
			var log = document.getElementById('log');
			var logText = responseMessage.log;
			if (log != null) {
				if (logText != null) {
					log.innerHTML = logText;
				} else {
					log.innerHTML = "";
				}
			}
		});
	}

	/**
	 * Exit the conversation.
	 */
	this.exit = function() {
		if (SDK.audio != null) {
			SDK.audio.pause();
		}
		if (SDK.currentAudio != null) {
			SDK.currentAudio.pause();
		}
		speechSynthesis.cancel();
		if (this.conversation == null || this.external) {
			return false;
		}
		var chat = new ChatConfig();
		chat.disconnect = true;
		chat.instance = this.instance;
		chat.conversation = this.conversation;
		var self = this;
		this.connection.chat(chat, function(responseMessage) {
			self.conversation = null;
		});
		return false;
	};

	/**
	 * Clear the chat console.
	 */
	this.clear = function() {
		document.getElementById(this.prefix + 'response').innerHTML = '';
		var console = document.getElementById(this.prefix + 'console');
		if (console != null) {
			console.innerHTML = '';
		}
		return false;
	};

	this.resizeAvatar = function () {
		var avatar = document.getElementById(this.prefix + "avatar");
		var avatarDiv = document.getElementById(this.prefix + "avatar-image-div");
		var avatarVideo = document.getElementById(this.prefix + "avatar-video");
		var avatarVideoDiv = document.getElementById(this.prefix + "avatar-video-div");
		var avatarCanvas = document.getElementById(this.prefix + "avatar-canvas");
		var avatarCanvasDiv = document.getElementById(this.prefix + "avatar-canvas-div");
		var scroller = document.getElementById(this.prefix + "scroller");
		var chatBubbleDiv = document.getElementById(web.prefix + 'bubble-div');
		var chatBubble = document.getElementById(this.prefix + 'bubble');
		if (!this.big) {
			this.hd = true;
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
				document.getElementById(this.prefix + 'response').style.display = "inline";
			}
			if (chatBubbleDiv != null) {
				chatBubbleDiv.style.display = "block";
			}
			if (chatBubble != null) {
				chatBubble.style.display = "inherit";
			}
			this.big = true;
		} else {
			this.hd = false;
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
				if (this.chatLog && window.innerWidth > 480) {
					scroller.style.display = "inline-block";
				} else {
					scroller.style.display = "none";
				}
			}
			if (chatBubbleDiv != null) {
				if (!this.chatLog || window.innerWidth < 480) {
					chatBubbleDiv.style.display = "block";
				} else {
					chatBubbleDiv.style.display = "none";
				}
			}
			if (chatBubble != null) {
				if (!this.chatLog || window.innerWidth < 480) {
					chatBubble.style.display = "block";
				} else {
					chatBubble.style.display = "none";
				}
			}
			this.big = false;
		}
		if (window.onresize != null) {
			setTimeout(window.onresize(), 100);
		}
		return false;
	}
	
	/**
	 * Create a web/image/video preview browser.
	 * This can be accessed through commands form the bot if initialized.
	 * This is disabled by default, so the initialize must be called for web preview support.
	 */
	this.initWebPreview = function() {
		var browser = document.createElement('div');
		var id = this.prefix + "webpreview";
		browser.setAttribute('id', id);

		var self = this;
		var height = window.innerHeight - (window.innerHeight * 0.2);
		var width = window.innerWidth - (window.innerWidth * 0.2);
		var top = window.innerHeight * 0.1;
		var left = window.innerWidth * 0.1;
		browser.style = "display:none;position:absolute;z-index:101;width:" + width + "px;height:" + height + "px;top:" + top + "px;left:" + left + "px;";
		var html = "<span style='float:right'><img id='" + this.prefix + "webpreview-close' src='" + SDK.url + "/images/closeg.png'></span>"
			+ "<iframe id='" + this.prefix + "webpreview-iframe' style='display:none;width:100%;height:100%;border:2px solid;'></iframe>"
			+ "<img id='" + this.prefix + "webpreview-image' style='display:none;max-width:100%;max-height:100%;border:2px solid;margin:auto'></iframe>";
		browser.innerHTML = html;
		SDK.body().appendChild(browser);

		var self = this;
		document.getElementById(this.prefix + "webpreview-image").addEventListener("click", function() {
			document.getElementById(self.prefix + "webpreview").style.display = "none";
			return false;
		});
		
		document.getElementById(this.prefix + "webpreview-close").addEventListener("click", function() {
			document.getElementById(self.prefix + "webpreview").style.display = "none";
			return false;
		});
		
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
 * If a prefix is set, these id will be prefixed by the prefix.
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
	/** The name of the voice mod to use. */
	this.voiceMod = null;
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
	/** Element id and class prefix. Can be used to have multiple avatars in the same page, or avoid naming collisions. */
	this.prefix = "avatar-";
	/** Store list of messages to output. */
	this.messages = null;
	/** Function to invoke when processing all messages is complete. */
	this.ended = null;
	/** Set if the avatar should request HD (high def) video/images. */
	this.hd = null;
	/** Set if the avatar should request a specific video or image format. */
	this.format = null;
	/** Set the location of the avatar div, one of "bottom-right", "bottom-left", "top-right", "top-left". */
	this.boxLocation = "bottom-left";
	/** Avatar div vertical offset*/
	this.verticalOffset = 10;
	/** Avatar div offset*/
	this.offset = 10;
	
	/**
	 * Create an embedding bar and div in the current webpage.
	 */
	this.createBox = function() {
		if (this.prefix == "" && this.elementPrefix != null) {
			this.prefix = this.elementPrefix;
		}
		var backgroundstyle = "";
		var hidden = "hidden";
		var border = "";
		if ((this.background != null) && (!this.backgroundIfNotChrome || !(SDK.isChrome() || SDK.isFirefox()))) {
			backgroundstyle = " style='background-color:" + this.background + "'";
			hidden = "visible";
			border = "border:1px;border-style:solid;border-color:black;";
		} else {
			border = "border:1px;border-style:solid;border-color:transparent;";
		}
		var box = document.createElement('div');
		var minWidth = "";
		var minHeight = "";
		var divWidth = "";
		var divHeight = "";
		var background = "";
		if (this.width != null) {
			if (typeof this.width === "string") {
				this.width = parseInt(this.width);
			}
			if ((this.width + 30) > window.innerWidth) {
				this.width = window.innerWidth - 40;
			}
			if (((this.width * 1.2) + 60) > window.innerHeight) {
				this.height = window.innerHeight - 60;
				this.width = null;
			}
			if (this.width != null) {
				minWidth = "width:" + this.width + "px;";
				background = "background-size:" + this.width + "px;";
				divWidth = minWidth;
				divHeight = "min-height:" + this.width + "px;";
			}
		}
		if (this.height != null) {
			if (typeof this.height === "string") {
				this.height = parseInt(this.height);
			}
			if ((this.height + 30) > window.innerHeight) {
				this.height = window.innerHeight - 40;
			}
			minHeight = "height:" + this.height + "px;";
			divHeight = minHeight;
			if (this.width != null) {
				background = "background-size:" + this.width + "px " + this.height + "px;";
			} else {
				background = "background-size: auto " + this.height + "px;";
				divWidth = "min-width:" + this.height + "px;";
			}
		}

		var boxbarloc = "bottom:" + this.verticalOffset + "px;right:" + this.offset + "px";
		if (this.boxLocation == "top-left") {
			boxbarloc = "top:" + this.verticalOffset + "px;left:" + this.offset + "px";
		} else if (this.boxLocation == "top-right") {
			boxbarloc = "top:" + this.verticalOffset + "px;right:" + this.offset + "px";
		} else if (this.boxLocation == "bottom-left") {
			boxbarloc = "bottom:" + this.verticalOffset + "px;left:" + this.offset + "px";
		} else if (this.boxLocation == "bottom-right") {
			boxbarloc = "bottom:" + this.verticalOffset + "px;right:" + this.offset + "px";
		}
		var html =
			"<style>\n"
				+ "." + this.prefix + "avatarbox { position:fixed;" + boxbarloc + ";z-index:152;margin:2px;" + border + divWidth + " }\n"
				+ "." + this.prefix + "avatarbox:hover { border:1px;border-style:solid;border-color:black; }\n"
				+ "." + this.prefix + "avatarbox ." + this.prefix + "avatarboxmenu { visibility:" + hidden + "; }\n"
				+ "." + this.prefix + "avatarbox:hover ." + this.prefix + "avatarboxmenu { visibility:visible; }\n"
				+ "img." + this.prefix + "avatarboxclose { margin:4px }\n"
				+ "#" + this.prefix + "avatarboxclose { margin:0px;font-size:26px; }\n"
				+ "#" + this.prefix + "avatarboxclose:hover { color: #fff;background: grey; }\n"
			+ "</style>\n"
			+ "<div id='" + this.prefix + "avatarbox' class='" + this.prefix + "avatarbox' " + backgroundstyle + ">"
				+ "<div class='" + this.prefix + "avatarboxmenu'>"
					+ "<span style='float:right'><a id='" + this.prefix + "avatarboxclose' onclick='return false;' href='#'><img class='" + this.prefix + "avatarboxclose' src='" + SDK.url + "/images/closeg.png'></a></span><br/>"
				+ "</div>"
				+ "<div id='" + this.prefix + "avatar-image-div' style='display:none;" + minWidth + minHeight + "'>"
					+ "<img id='" + this.prefix + "avatar' style='" + minWidth + minHeight + "'/>"
				+ "</div>"
				+ "<div id='" + this.prefix + "avatar-video-div' style='display:none;" + divWidth + divHeight + background + "background-repeat: no-repeat;'>"
					+ "<video muted='true' id='" + this.prefix + "avatar-video' autoplay preload='auto' style='background:transparent;" + minWidth + minHeight + "'>"
						+ "Video format not supported by your browser (try Chrome)"
					+ "</video>"
				+ "</div>"
				+ "<div id='" + this.prefix + "avatar-canvas-div' style='display:none;" + divWidth + divHeight + "'>"
					+ "<canvas id='" + this.prefix + "avatar-canvas' style='background:transparent;" + minWidth + minHeight + "'>"
						+ "Canvas not supported by your browser (try Chrome)"
					+ "</canvas>"
				+ "</div>"
			+ "</div>";
		
		box.innerHTML = html;
		SDK.body().appendChild(box);
		
		var self = this;
		document.getElementById(this.prefix + "avatarboxclose").addEventListener("click", function() {
			self.closeBox();
			return false;
		});
		this.closed = false;
	}
	
	/**
	 * Open the embedding div in the current webpage.
	 */
	this.openBox = function() {
		document.getElementById(this.prefix + "avatarbox").style.display = 'inline';
		this.speak = true;
		this.closed = false;
		return false;
	}
	
	/**
	 * Close the embedding div in the current webpage.
	 */
	this.closeBox = function() {
		document.getElementById(this.prefix + "avatarbox").style.display = 'none';
		if (SDK.audio != null) {
			SDK.audio.pause();
		}
		if (SDK.currentAudio != null) {
			SDK.currentAudio.pause();
		}
		speechSynthesis.cancel();
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
		SDK.updateAvatar(responseMessage, this.speak, urlprefix, this.prefix, null, afterFunction, this.nativeVoice, this.lang, this.nativeVoiceName);
	};
	
	/**
	 * Add the message to the avatars message queue.
	 * The messages will be spoken when processMessages() is called.
	 */
	this.addMessage = function(message, emote, action, pose) {
		var config = new AvatarMessage();
		config.message = message;
		config.avatar = this.avatar;
		if (this.hd != null) {
			config.hd = this.hd;
		} else if ((this.width != null && this.width > 400) || (this.height != null && this.height > 400)) {
			config.hd = true;
		}
		if (this.format != null) {
			config.format = this.format;
		}
		if (this.nativeVoice && SDK.speechSynthesis) {
			config.speak = false;
		} else {
			config.speak = this.speak;
			config.voice = this.voice;
			config.voiceMod = this.voiceMod;
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
		if (this.nativeVoice && SDK.speechSynthesis) {
			config.speak = false;
		} else {
			config.speak = this.speak;
			config.voice = this.voice;
			config.voiceMod = this.voiceMod;
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
	this.contactInfo = null;
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
				var connectString = "connect " + self.channel.id;
				if (self.user == null) {
					connectString = connectString + " " + appId;
				} else if (user.token == null) {
					connectString = connectString + " " + self.user.user + " " + self.user.password + " " + appId;						
				} else {
					connectString = connectString + " " + self.user.user + " " + self.user.token + " " + appId;						
				}
				if (self.contactInfo != null) {
					connectString = connectString + " @info " + self.contactInfo;
				}
				self.socket.send(connectString);
			}
			self.setKeepAlive(self.keepAlive);
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
			
			if (self.keepAlive && user == "Info" && text.includes("pong")) {
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
		if (SDK.audio != null) {
			SDK.audio.pause();
		}
		if (SDK.currentAudio != null) {
			SDK.currentAudio.pause();
		}
		speechSynthesis.cancel();
	};

	/**
	 * Change to spy mode.
	 * This allows admins to monitor the entire channel.
	 */
	this.spyMode = function() {
		this.checkSocket();
		this.socket.send("mode: spy");
	};

	/**
	 * Change to normal mode.
	 */
	this.normalMode = function() {
		this.checkSocket();
		this.socket.send("mode: normal");
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
		var self = this;
		this.keepAlive = keepAlive;
		if (!keepAlive && this.keepAliveInterval != null) {
			clearInterval(this.keepAliveInterval);
		} else if (keepAlive && this.keepAliveInterval == null) {
			this.keepAliveInterval = setInterval(
					function() {
						self.ping()
					},
					60000);
		}
	}
}

/**
* Connection class for a REST service connection.
* The SDK connection gives you access to the Bot Libre services using a REST web API.
* <p>
* The services include:
* <ul>
* <li> User management (account creation, validation)
* <li> Bot access, chat, and administration
* <li> Forum access, posting, and administration
* <li> Live chat access, chat, and administration
* <li> Script, Graphic, and Domain access, and administration
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
	 * testAnalytic will upload an image to memmory and try to recognize the image.
	 */
	this.testAnalytic = function(config, file, resize, form, processor, image_size) {
		var self = this;
		if (!resize && file.size > SDK.MAX_FILE_UPLOAD) {
			this.error("File exceeds maximum upload size of " + (SDK.MAX_FILE_UPLOAD / 1000000) + "meg");
		} else {
			config.addCredentials(this);
			if (resize) {
				this.POST_IMAGE_SIZED(this.credentials.rest + "/test-analytic", file, form, config.toXML(), function(xml) {
					if (xml == null) {
						return null;
					}
					var media = new AnalyticResponse();
					media.parseXML(xml);
					processor(media);

				}, image_size);
			} else {
				this.POST_FILE(this.credentials.rest + "/test-analytic", form, config.toXML(), function(xml) {
					if (xml == null) {
						return null;
					}
					var media = new AnalyticResponse();
					media.parseXML(xml);
					processor(media);
				});
			}
		}
		return false;
	};
	
	/**
	 * testAnalytic will upload an image to memmory and try to recognize the image.
	 */
	this.testAudioAnalytic = function(config, file, form, processor) {
		if (file.size > SDK.MAX_FILE_UPLOAD) {
			this.error("File exceeds maximum upload size of " + (SDK.MAX_FILE_UPLOAD / 1000000) + "meg");
		} else {
			config.addCredentials(this);
			this.POST_FILE(this.credentials.rest + "/test-audio-analytic", form, config.toXML(), function(xml) {
				if (xml == null) {
					return null;
				}
				var media = new AnalyticAudioResponse();
				media.parseXML(xml);
				processor(media);
			});
		}
		return false;
	};
	
	this.testObjectDetectionAnalytic = function(config, file, resize, form, processor, image_size) {
		if (!resize && file.size > SDK.MAX_FILE_UPLOAD) {
			this.error("File exceeds maximum upload size of " + (SDK.MAX_FILE_UPLOAD / 1000000) + "meg");
		} else {
			config.addCredentials(this);
			if (resize) {
				this.POST_IMAGE_SIZED(this.credentials.rest + "/test-object-detection-analytic", file, form, config.toXML(), function(xml) {
					if (xml == null) {
						return null;
					}
					var media = new AnalyticObjectDetectionResponse();
					media.parseXML(xml);
					processor(media);

				}, image_size);
			} else {
				this.POST_FILE(this.credentials.rest + "/test-object-detection-analytic", form, config.toXML(), function(xml) {
					if (xml == null) {
						return null;
					}
					var media = new AnalyticObjectDetectionResponse();
					media.parseXML(xml);
					processor(media);
				});
			}
		}
		return false;
	};

	this.sendAnalyticImage = function(analyticConfig, processor, file, form, image_size) {
		if (!(window.File && window.FileReader && window.FileList && window.Blob)) {
			alert('The File APIs are not fully supported in this browser.');
			return false;
		}
		this.testAnalytic(analyticConfig, file, true, form, processor, image_size);
	};
	
	this.sendObjectDetectionAnalyticImage = function(analyticConfigTest, processor, file, form, image_size) {
		if (!(window.File && window.FileReader && window.FileList && window.Blob)) {
			alert('The File APIs are not fully supported in this browser.');
			return false;
		}
		this.testObjectDetectionAnalytic(analyticConfigTest, file, true, form, processor, image_size);
	};
	
	this.sendAudioAnalytic = function(analyticConfig, processor, file, form) {
		if (!(window.File && window.FileReader && window.FileList && window.Blob)) {
			alert('The File APIs are not fully supported in this browser.');
			return false;
		}
		this.testAudioAnalytic(analyticConfig, file, form, processor);
	};
	
	this.getTestMediaResult = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/get-test-media-result", config.toXML(), function(xml) {
			if(xml == null){
				return null;
			}
			var config = new AnalyticTestMediaResponse();
			config.parseXML(xml);
			processor(config)
		});
	}
	
	this.checkTraining = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/check-training", config.toXML(), function(xml) {
			if (xml == null) {
				return null;
			}
			var config = new AnalyticConfig();
			config.parseXML(xml);
			processor(config);
		});
	}
	
	this.trainAnalytic = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/train-analytic", config.toXML(), function(xml) {
			if (xml == null) {
				return null;
			}
			var analytic = new AnalyticConfig();
			analytic.parseXML(xml);
			processor(analytic);
		});
	}
	
	/**
	 * processing test media
	 */
	this.reportMediaAnalytic = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/report-media-analytic", config.toXML(), function(xml) {
			if (xml == null) {
				return null;
			}
			var analytic = new AnalyticConfig();
			analytic.parseXML(xml);
			processor(analytic);
		});
	}
	
	this.deleteAnalyticMedia = function(config, done) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/delete-analytic-media", config.toXML(), function(xml) {
			return;
		});
		done();
	}
	
	this.deleteAnalyticTestMedia = function(config, done) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/delete-analytic-test-media", config.toXML(), function(xml) {
			return;
		});
		done();
	}
	
	this.createAnalyticLabel = function(config) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/create-analytic-label", config.toXML(), function(xml) {
			return;
		});
	}
	
	this.createAnalyticTestMediaLabel = function(config) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/create-analytic-test-media-label", config.toXML(), function(xml) {
			return;
		});
	}
	
	this.deleteAnalyticLabel = function(config) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/delete-analytic-label", config.toXML(), function(xml) {
			return;
		});
	}
	
	this.deleteAnalyticTestMediaLabel = function(config) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/delete-analytic-test-media-label", config.toXML(), function(xml) {
			return;
		});
	}
	
	/**
	 * Return the list of content for the Analytic media repository
	 */
	this.getAnalyticMedia = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/get-analytic-media", config.toXML(), function(xml) {
			var instances = [];
			if (xml != null) {
				for (var index = 0; index < xml.childNodes.length; index++) {
					var instance = null;
					
					instance = new AnalyticMediaConfig();
					
					instance.parseXML(xml.childNodes[index]);
					instances[instances.length] = (instance);
				}
			}
			processor(instances);
		});
	}
	
	/**
	 * Return the list of content for the Analytic test media repository
	 */
	this.getAnalyticTestMedia = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/get-analytic-test-media", config.toXML(), function(xml) {
			var instances = [];
			if (xml != null) {
				for (var index = 0; index < xml.childNodes.length; index++) {
					var instance = null;
					
					instance = new AnalyticMediaConfig();
					
					instance.parseXML(xml.childNodes[index]);
					instances[instances.length] = (instance);
				}
			}
			processor(instances);
		});
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
	 * Move bot's script up.
	 */
	this.upBotScript = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/up-bot-script", config.toXML(), function(xml) {
			processor();
		});
	}
	
	/**
	 * Move bot's script down.
	 */
	this.downBotScript = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/down-bot-script", config.toXML(), function(xml) {
			processor();
		});
	}
	
	/**
	 * Delete bot's script.
	 */
	this.deleteBotScript = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/delete-bot-script", config.toXML(), function(xml) {
			processor();
		});
	}
	
	/**
	 * Fetch the user details for the user id.
	 */	
	this.viewUser = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/view-user", config.toXML(), function(xml) {
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
	 * Create a new file/image/media attachment for a bot.
	 */
	this.createBotAttachment = function(config, file, resize, form, processor) {
		config.addCredentials(this);
		if (resize) {
			this.POST_IMAGE(this.credentials.rest + "/create-bot-attachment", file, form, config.toXML(), function(xml) {
				if (xml == null) {
					return null;
				}
				var media = new MediaConfig();
				media.parseXML(xml);
				processor(media);
			});
		} else {
			this.POST_FILE(this.credentials.rest + "/create-bot-attachment", form, config.toXML(), function(xml) {
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
	 * Create a new file/image/media attachment for a bot.
	 */
	this.createAudioAttachment = function(config, file, form, processor) {
		config.addCredentials(this);
			this.POST_FILE(this.credentials.rest + "/create-bot-attachment", form, config.toXML(), function(xml) {
				if (xml == null) {
					return null;
				}
				var media = new MediaConfig();
				media.parseXML(xml);
				processor(media);
			});
		
	}

	/**
	 * Create a new file/image/media attachment for an issue tracker.
	 */
	this.createIssueTrackerAttachment = function(config, file, resize, form, processor) {
		config.addCredentials(this);
		if (resize) {
			this.POST_IMAGE(this.credentials.rest + "/create-issuetracker-attachment", file, form, config.toXML(), function(xml) {
				if (xml == null) {
					return null;
				}
				var media = new MediaConfig();
				media.parseXML(xml);
				processor(media);
			});
		} else {
			this.POST_FILE(this.credentials.rest + "/create-issuetracker-attachment", form, config.toXML(), function(xml) {
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
	 * Create a new file/image/media attachment for an issue tracker and insert the http link into the textarea.
	 */
	this.uploadIssueTrackerAttachment = function(forum, resize, processor) {
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
			self.uploadIssueTrackerFile(file, forum, resize, form, processor);
		}
		fileInput.click();
	};

	/**
	 * Create a new file/image/media attachment for an issue tracker.
	 */
	this.uploadIssueTrackerFile = function(file, forum, resize, form, processor) {
		var self = this;
		var media = new MediaConfig();
		media.instance = forum;
		media.name = file.name;
		media.type = file.type;
		if (!resize && file.size > SDK.MAX_FILE_UPLOAD) {
			this.error("File exceeds maximum upload size of " + (SDK.MAX_FILE_UPLOAD / 1000000) + "meg");
		} else {
			this.createIssueTrackerAttachment(media, file, resize, form, function(media) {
				var link = self.fetchLink(media.file);
				if (processor != null) {
					processor(link, file.name);
				}
			})
		}
	};

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
	};

	/**
	 * Create a new file/image/media attachment for a bot.
	 */
	this.createBotAttachment = function(config, file, resize, form, processor) {
		config.addCredentials(this);
		if (resize) {
			this.POST_IMAGE(this.credentials.rest + "/create-bot-attachment", file, form, config.toXML(), function(xml) {
				if (xml == null) {
					return null;
				}
				var media = new MediaConfig();
				media.parseXML(xml);
				processor(media);
			});
		} else {
			this.POST_FILE(this.credentials.rest + "/create-bot-attachment", form, config.toXML(), function(xml) {
				if (xml == null) {
					return null;
				}
				var media = new MediaConfig();
				media.parseXML(xml);
				processor(media);
			});
		}
	};
	
	/**
	 * Create a new file/image/media attachment for a bot and insert the http link into the textarea.
	 */
	this.uploadBotAttachment = function(bot, resize, processor) {
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
			self.uploadBotFile(file, bot, resize, form, processor);
		}
		fileInput.click();
	};
	
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
	 * Create a new file/image/media attachment for a bot.
	 */
	this.uploadBotFile = function(file, bot, resize, form, processor) {
		var self = this;
		var media = new MediaConfig();
		media.instance = bot;
		media.name = file.name;
		media.type = file.type;
		if (!resize && file.size > SDK.MAX_FILE_UPLOAD) {
			this.error("File exceeds maximum upload size of " + (SDK.MAX_FILE_UPLOAD / 1000000) + "meg");
		} else {
			this.createBotAttachment(media, file, resize, form, function(media) {
				var link = self.fetchLink(media.file);
				if (processor != null) {
					processor(link, file.name);
				}
			})
		}
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
	 * Update the content of the graphic.
	 */
	this.updateGraphic = function(config) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/update-graphic", config.toXML(), function(xml) {
			return;
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
		this.POST(this.credentials.rest + "/chat", config.toXML(), function(xml) {
			if (xml == null) {
				return null;
			}
			var responseMessage = new ChatResponse();
			responseMessage.parseXML(xml);
			processor(responseMessage);			
		});
	}
	
	/**
	 * Process the bot command message and return the bot's response.
	 * The CommandConfig should contain the conversation id if part of a conversation.
	 * If a new conversation the conversation id is returned in the response. 
	 */
	this.command = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/command", config.toXML(), function(xml) {
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
	 * 
	 */
	this.userAdmin = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/user-admin", config.toXML(), function(xml) {
			processor();
		});
	}
	
	/**
	 * 
	 */
	this.userFriendship = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/user-friendship", config.toXML(), function(xml) {
			if (xml != null) {
				var userFriendsConfig = new UserFriendsConfig();
				userFriendsConfig.parseXML(xml);
				processor(userFriendsConfig);
			} else {
				processor();
			}
		});
	}
	
	/**
	 * Function polls new user messages
	 */
	this.pollUserToUserMessages = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/check-user-new-messages", config.toXML(), function(xml) {
			if (xml != null) {
				var userMessageConfig = new UserMessageConfig();
				userMessageConfig.parseXML(xml);
				processor(userMessageConfig);
			} else {
				processor();
			}
		});
	}
	
	/**
	 * Function creates new user message
	 */
	this.createUserMessage = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/create-user-message", config.toXML(), function(xml) {
			if (xml != null) {
				var userMessageConfig = new UserMessageConfig();
				userMessageConfig.parseXML(xml);
				processor(userMessageConfig);
			} else {
				processor();
			}
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
	 * Initialize the bot's avatar for a chat session.
	 * This can be done before processing the first message for a quick response.
	 * @deprecated replaced by initChat()
	 */
	this.initAvatar = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/init-avatar", config.toXML(), function(xml) {
			if (xml == null) {
				return;
			}
			var response = new ChatResponse();
			response.parseXML(xml);
			processor(response);
		});
	}
	
	/**
	 * Initialize the bot's avatar for a chat session.
	 * This can be done before processing the first message for a quick response.
	 */
	this.initChat = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/init-chat", config.toXML(), function(xml) {
			if (xml == null) {
				return;
			}
			var response = new ChatResponse();
			response.parseXML(xml);
			processor(response);
		});
	}
	
	/**
	 * Return the conversation's chat settings.
	 */
	this.chatSettings = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/chat-settings", config.toXML(), function(xml) {
			if (xml == null) {
				return;
			}
			var settings = new ChatSettings();
			settings.parseXML(xml);
			processor(settings);
		});
	}
	
	/**
	 * Add or remove a response for a bot.
	 */
	this.trainInstance = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/train-instance", config.toXML(), function(xml) {
			if (processor != null) {
				processor();
			}
		});
	}
	
	/**
	 * Add next question response to bot's responses.
	 */
	this.addQuestionResponse = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/save-response", config.toXML(), function(xml) {
			if (xml == null) {
				return;
			}
			var responseConfig = new ResponseConfig();
			responseConfig.parseXML(xml);
			processor(responseConfig);
		});
	}
	
	/**
	 * Delete bot's next question response.
	 */
	this.deleteQuestionResponse = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/delete-response", config.toXML(), function(xml) {
			if (processor != null) {
				processor();
			}
		});
	}
	
	/**
	 * Return the bot's next question response
	 */
	this.getQuestionResponse = function(config, processor) {
		config.addCredentials(this);
		this.POST(this.credentials.rest + "/get-response", config.toXML(), function(xml) {
			if (xml == null) {
				return;
			}
			var response = new ResponseConfig();
			response.parseXML(xml);
			processor(response);
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
		} else if (config.type == "Graphic") {
			type = "/get-graphics";
		} else if (config.type == "Avatar") {
			type = "/get-avatars";
		} else if (config.type == "Script") {
			type = "/get-scripts";
		} else if (config.type == "Analytic") {
			type = "/get-analytics";
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
					} else if (config.type == "Avatar") {
						instance = new AvatarConfig();
					} else if (config.type == "Script") {
						instance = new ScriptConfig();
					} else if (config.type == "Analytic") {
						instance = new AnalyticConfig();
					} else if (config.type == "Graphic") {
						instance = new GraphicConfig();
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
				if (request.statusText != null && request.responseText != null && request.responseText.indexOf("<html>") != -1) {
					self.error(request.statusText);
				} else {
					self.error(request.responseText);
				}
				return;
			}
			if (request.responseXML == null) {
				processor();
			} else {
				processor(request.responseXML.childNodes[0]);
			}
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
				if (request.statusText != null && request.responseText != null && request.responseText.indexOf("<html>") != -1) {
					self.error(request.statusText);
				} else {
					self.error(request.responseText);
				}
				return;
			}
			if (request.responseXML == null) {
				processor();
			} else {
				processor(request.responseXML.childNodes[0]);
			}
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
				if (request.statusText != null && request.responseText != null && request.responseText.indexOf("<html>") != -1) {
					self.error(request.statusText);
				} else {
					self.error(request.responseText);
				}
				return;
			}
			if (request.responseXML == null) {
				processor();
			} else {
				processor(request.responseXML.childNodes[0]);
			}
		};
		
		request.open('POST', url, true);
		//request.setRequestHeader("Content-Type", "multipart/form-data");
		request.send(formData);
	}
	
	this.POST_IMAGE_SIZED = function(url, file, form, xml, processor, image_size) {
		var self = this;
		var debug = this.debug;
		var reader = new FileReader();
		if (this.debug) {
			console.log("POST FILE: " + url);
			console.log("FORM: " + form);
			console.log("XML: " + xml);
		}
		reader.onloadend = function() {
			var tempImg = new Image();
			tempImg.src = reader.result;
			tempImg.onload = function() {
				var MAX_WIDTH = image_size;
				var MAX_HEIGHT = image_size;
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
						if (request.statusText != null && request.responseText != null && request.responseText.indexOf("<html>") != -1) {
							self.error(request.statusText);
						} else {
							self.error(request.responseText);
						}
						return;
					}
					if (request.responseXML == null) {
						processor();
					} else {
						processor(request.responseXML.childNodes[0]);
					}
				};
				
				request.open('POST', url, true);
				//request.setRequestHeader("Content-Type", "multipart/form-data");
				request.send(formData);
			}
		 }
		 reader.readAsDataURL(file);
	}
	
	this.POST_IMAGE = function(url, file, form, xml, processor) {
		var self = this;
		var debug = this.debug;
		var reader = new FileReader();
		reader.onloadend = function() {
			var tempImg = new Image();
			tempImg.src = reader.result;
			tempImg.onload = function() {
				var MAX_WIDTH = 600;
				var MAX_HEIGHT = 600;
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
						if (request.statusText != null && request.responseText != null && request.responseText.indexOf("<html>") != -1) {
							self.error(request.statusText);
						} else {
							self.error(request.responseText);
						}
						return;
					}
					if (request.responseXML == null) {
						processor();
					} else {
						processor(request.responseXML.childNodes[0]);
					}
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
 * Abstract root class for all web API message objects.
 * Defines the required application id, and common fields.
 * @class
 * @property application
 * @property domain
 * @property user
 * @property token
 * @property instance
 * @property type
 */
function Config() {
	/** The application ID.  This is require to authenticate the API usage.  You can obtain your application ID from your user page. */
	this.application;
	/** Optional domain id, if object is not on the server's default domain. */
	this.domain;
	/** User ID, required for content creation, secure content access, or to identify the user. */
	this.user;
	/** User's access token, returned from connect web API, can be used in place of password in subsequent calls, and stored in a cookie.   The user's password should never be stored. */
	this.token;
	/** The id or name of the bot or content instance to access. */
	this.instance;
	/** Type of instance to access, ("Bot", "Forum", "Channel", "Domain") */
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
 * This object models a user.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself to/from XML for web API usage.
 * This can be used to connect, create, edit, or browse a user instance.
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
	/** Password, require to connect a user, or create a user. */
	this.password;
	/** New password for editting a user's password (password is old password). */
	this.newPassword;
	/** Optional password hint, in case password is forgotten. */
	this.hint;
	/** Optional real name of the user. */
	this.name;
	/** The real name can be hidden from other users. */
	this.showName;
	/** Email, required for message notification, and to reset password. */
	this.email;
	/** Optional user's website. */
	this.website;
	/** Optional user's bio. */
	this.bio;
	this.over18;
	/** Read-only, server local URL for user's avatar image. */
	this.avatar;
	this.avatarThumb;

	/** Read-only, total user connects. */
	this.connects;
	/** Read-only, total bots created. */
	this.bots;
	/** Read-only, total forum posts. */
	this.posts;
	/** Read-only, total chat messages. */
	this.messages;
	/** Read-only, date user joined. */
	this.joined;
	/** Read-only, date of user's last connect. */
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
		this.showName = element.getAttribute("showName") == "true";
		this.token = element.getAttribute("token");
		this.email = element.getAttribute("email");
		this.hint = element.getAttribute("hint");
		this.website = element.getAttribute("website");
		this.connects = element.getAttribute("connects");
		this.bots = element.getAttribute("bots");
		this.posts = element.getAttribute("posts");
		this.messages = element.getAttribute("messages");
		this.joined = element.getAttribute("joined") == "true";
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
 * This object models a user message.
 * It can be used from a messages UI, or with the Libre Web API.
 * It can convert itself to/from XML for web API usage.
 * This is used to create new user message instance.
 * @class
 */
function UserMessageConfig() {
	this.id;
	this.creationDate;
	this.owner;
	this.creator;
	this.target;
	this.parent;
	this.avatar;
	this.page;
	this.pageSize;
	this.resultsSize;
	this.subject;
	this.message;
	
	this.addCredentials = function(connection) {
		this.application = connection.credentials.applicationId;
		if (connection.domain != null) {
			this.domain = connection.domain.id;
		}
	}

	this.parseXML = function(element) {
		var userMessageNode = element.getElementsByTagName("user-message")[0];
		if (userMessageNode != null) {
			this.id = userMessageNode.getAttribute("id");
			this.creationDate = userMessageNode.getAttribute("creationDate");
			this.owner = userMessageNode.getAttribute("owner");
			this.creator = userMessageNode.getAttribute("creator");
			this.target = userMessageNode.getAttribute("target");
			this.parent = userMessageNode.getAttribute("parent");
			this.avatar = userMessageNode.getAttribute("avatar");
			this.page = userMessageNode.getAttribute("page");
			this.pageSize = userMessageNode.getAttribute("pageSize");
			this.resultsSize = userMessageNode.getAttribute("resultsSize");
		}
		
		var subjectNode = element.getElementsByTagName("subject")[0];
		if (subjectNode != null) {
			this.subject = SDK.innerHTML(subjectNode);
		}
		
		var messageNode = element.getElementsByTagName("message")[0];
		if (messageNode != null) {
			this.message = SDK.innerHTML(messageNode);
		}
	}
	
	this.toXML = function() {
		var xml = "<user-message";
		xml = this.writeCredentials(xml);
		if (this.id != null) {
			xml = xml + (" id=\"" + this.id + "\"");
		}
		if (this.creationDate != null) {
			xml = xml + (" creationDate=\"" + this.creationDate + "\"");
		}
		if (this.owner != null) {
			xml = xml + (" owner=\"" + this.owner + "\"");
		}
		if (this.creator != null) {
			xml = xml + (" creator=\"" + this.creator + "\"");
		}
		if (this.target) {
			xml = xml + (" target=\"" + this.target + "\"");
		}
		if (this.page != null) {
			xml = xml + (" page=\"" + this.page + "\"");
		}
		if (this.pageSize != null) {
			xml = xml + (" pageSize=\"" + this.pageSize + "\"");
		}
		if (this.resultsSize) {
			xml = xml + (" resultsSize=\"" + this.resultsSize + "\"");
		}
		xml = xml + (">");
		
		if (this.subject != null) {
			xml = xml + ("<subject>");
			xml = xml + (SDK.escapeHTML(this.subject));
			xml = xml + ("</subject>");
		}
		if (this.message != null) {
			xml = xml + ("<message>");
			xml = xml + (SDK.escapeHTML(this.message));
			xml = xml + ("</message>");
		}
		xml = xml + ("</user-message>");
		return xml;
	}
}
UserMessageConfig.prototype = new Config();
UserMessageConfig.prototype.constructor = UserMessageConfig;
UserMessageConfig.constructor = UserMessageConfig;

/**
 * This object models a response config.
 * It is used to add bot's question and response and other data associate with response.
 * It can convert itself to/from XML for web API usage.
 * This can be used to create, edit, bot's responses.
 * @class
 * @property type
 * @property questionId
 * @property responseId
 * @property question
 * @property response
 * @property previous
 * @property next
 * @property onRepeat
 * @property command
 * @property think
 * @property condition
 * @property label
 * @property topic
 * @property keywords
 * @property required
 * @property emotions
 * @property actions
 * @property poses
 * @property noRepeat
 * @property requirePrevious
 * @property requireTopic
 * @property flagged
 * @property correctness
 */
function ResponseConfig() {
	this.type;
	this.parentQuestionId;
	this.parentResponseId;
	this.questionId;
	this.responseId;
	this.metaId;
	this.question;
	this.response;
	this.previous;
	this.next;
	this.onRepeat;
	this.command;
	this.think;
	this.condition;
	this.label;
	this.topic;
	this.keywords;
	this.required;
	this.emotions;
	this.actions;
	this.poses;
	this.noRepeat;
	this.requirePrevious;
	this.requireTopic;
	this.flagged;
	this.correctness;
	this.sentiment;
	this.exclusiveTopic;
	this.displayHTML;
	this.autoReduce;

	this.parseXML = function(element) {
		this.type = element.getAttribute("type");
		this.parentQuestionId = element.getAttribute("parentQuestionId");
		this.parentResponseId = element.getAttribute("parentResponseId");
		this.questionId = element.getAttribute("questionId");
		this.responseId = element.getAttribute("responseId");
		this.metaId = element.getAttribute("metaId");
		this.question = element.getAttribute("question");
		this.response = element.getAttribute("response");
		this.previous = element.getAttribute("previous");
		this.next = element.getAttribute("next");
		this.onRepeat = element.getAttribute("onRepeat");
		this.command = element.getAttribute("command");
		this.think = element.getAttribute("think");
		this.condition = element.getAttribute("condition");
		this.label = element.getAttribute("label");
		this.topic = element.getAttribute("topic");
		this.keywords = element.getAttribute("keywords");
		this.required = element.getAttribute("required");
		this.emotions = element.getAttribute("emotions");
		this.actions = element.getAttribute("actions");
		this.poses = element.getAttribute("poses");
		this.noRepeat = element.getAttribute("noRepeat");
		this.requirePrevious = element.getAttribute("requirePrevious");
		this.requireTopic = element.getAttribute("requireTopic");
		this.flagged = element.getAttribute("flagged") == "true";
		this.correctness = element.getAttribute("correctness");
		this.sentiment = element.getAttribute("sentiment");
		this.exclusiveTopic = element.getAttribute("exclusiveTopic");
		this.displayHTML = element.getAttribute("displayHTML") == "true";
		this.autoReduce = element.getAttribute("autoReduce") == "true";
		
		var node = element.getElementsByTagName("question")[0];
		if (node != null) {
			this.question = SDK.innerHTML(node);
		}
		
		var node = element.getElementsByTagName("response")[0];
		if (node != null) {
			this.response = SDK.innerHTML(node);
		}
		
		var node = element.getElementsByTagName("previous")[0];
		if (node != null) {
			this.previous = SDK.innerHTML(node);
		}
		
		var node = element.getElementsByTagName("next")[0];
		if (node != null) {
			this.next = SDK.innerHTML(node);
		}
		
		var node = element.getElementsByTagName("onRepeat")[0];
		if (node != null) {
			this.onRepeat = SDK.innerHTML(node);
		}
		
		var node = element.getElementsByTagName("command")[0];
		if (node != null) {
			this.command = SDK.innerHTML(node);
		}
		
		var node = element.getElementsByTagName("think")[0];
		if (node != null) {
			this.think = SDK.innerHTML(node);
		}
		
		var node = element.getElementsByTagName("condition")[0];
		if (node != null) {
			this.condition = SDK.innerHTML(node);
		}
	}
	
	this.toXML = function() {
		var xml = "<response";
		xml = this.writeCredentials(xml);
		//if (this.type != null) {
		//	xml = xml + (" type=\"" + this.type + "\"");
		//}
		if (this.parentQuestionId != null) {
			xml = xml + (" parentQuestionId=\"" + this.parentQuestionId + "\"");
		}
		if (this.parentResponseId != null) {
			xml = xml + (" parentResponseId=\"" + this.parentResponseId + "\"");
		}
		if (this.questionId != null) {
			xml = xml + (" questionId=\"" + this.questionId + "\"");
		}
		if (this.responseId != null) {
			xml = xml + (" responseId=\"" + this.responseId + "\"");
		}
		if (this.metaId != null) {
			xml = xml + (" metaId=\"" + this.metaId + "\"");
		}
		if (this.label) {
			xml = xml + (" label=\"" + SDK.escapeHTML(this.label) + "\"");
		}
		if (this.topic) {
			xml = xml + (" topic=\"" + SDK.escapeHTML(this.topic) + "\"");
		}
		if (this.keywords) {
			xml = xml + (" keywords=\"" + SDK.escapeHTML(this.keywords) + "\"");
		}
		if (this.required) {
			xml = xml + (" required=\"" + SDK.escapeHTML(this.required) + "\"");
		}
		if (this.emotions) {
			xml = xml + (" emotions=\"" + SDK.escapeHTML(this.emotions) + "\"");
		}
		if (this.actions) {
			xml = xml + (" actions=\"" + SDK.escapeHTML(this.actions) + "\"");
		}
		if (this.poses) {
			xml = xml + (" poses=\"" + SDK.escapeHTML(this.poses) + "\"");
		}
		if (this.noRepeat) {
			xml = xml + (" noRepeat=\"" + this.noRepeat + "\"");
		}
		if (this.requirePrevious) {
			xml = xml + (" requirePrevious=\"" + this.requirePrevious + "\"");
		}
		if (this.requireTopic) {
			xml = xml + (" requireTopic=\"" + this.requireTopic + "\"");
		}
		if (this.flagged) {
			xml = xml + (" flagged=\"" + this.flagged + "\"");
		}
		if (this.correctness) {
			xml = xml + (" correctness=\"" + this.correctness + "\"");
		}
		if (this.sentiment) {
			xml = xml + (" sentiment=\"" + SDK.escapeHTML(this.sentiment) + "\"");
		}
		if (this.exclusiveTopic) {
			xml = xml + (" exclusiveTopic=\"" + this.exclusiveTopic + "\"");
		}
		if (this.autoReduce) {
			xml = xml + (" autoReduce=\"" + this.autoReduce + "\"");
		}
		xml = xml + (">");

		if (this.question != null) {
			xml = xml + ("<question>" + SDK.escapeHTML(this.question) + "</question>");
		}
		if (this.response != null) {
			xml = xml + ("<response>" + SDK.escapeHTML(this.response) + "</response>");
		}
		if (this.previous) {
			xml = xml + ("<previous>" + SDK.escapeHTML(this.previous) + "</previous>");
		}
		if (this.next != null) {
			xml = xml + ("<next>" + SDK.escapeHTML(this.next) + "</next>");
		}
		if (this.onRepeat != null) {
			xml = xml + ("<onRepeat>" + SDK.escapeHTML(this.onRepeat) + "</onRepeat>");
		}
		if (this.command) {
			xml = xml + ("<command>" + SDK.escapeHTML(this.command) + "</command>");
		}
		if (this.think) {
			xml = xml + ("<think>" + SDK.escapeHTML(this.think) + "</think>");
		}
		if (this.condition) {
			xml = xml + ("<condition>" + SDK.escapeHTML(this.condition) + "</condition>");
		}
		
		xml = xml + ("</response>");
		return xml;
	}
		
}
ResponseConfig.prototype = new Config();
ResponseConfig.prototype.constructor = ResponseConfig;
ResponseConfig.constructor = ResponseConfig;

/**
 * This object models a chat message sent to a chat bot instance.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself to XML for web API usage.
 * @class
 * @property conversation
 * @property speak
 * @property correction
 * @property offensive
 * @property disconnect
 * @property emote
 * @property action
 * @property message
 * @property debug
 * @property debugLevel
 * @property learn
 */
function ChatConfig() {
	/** The conversation id for the message.  This will be returned from the first response, and must be used for all subsequent messages to maintain the conversational state.  Without the conversation id, the bot has no context for the reply. */
	this.conversation;
	/** Sets if the voice audio should be generated for the bot's response. */
	this.speak;
	/** Sets the message to be a correction to the bot's last response. */
	this.correction;
	/** Flags the bot's last response as offensive. */
	this.offensive;
	/** Ends the conversation. Conversation should be terminated to converse server resources.  The message can be blank. */
	this.disconnect;
	/** 
	 * Attaches an emotion to the user's message, one of:
	 *  NONE,
	 *  LOVE, LIKE, DISLIKE, HATE,
	 *	RAGE, ANGER, CALM, SERENE,
	 *	ECSTATIC, HAPPY, SAD, CRYING,
	 *	PANIC, AFRAID, CONFIDENT, COURAGEOUS,
	 *	SURPRISE, BORED,
	 *	LAUGHTER, SERIOUS
	 */
	this.emote;
	/** Attaches an action to the user's messages, such as "laugh", "smile", "kiss". */
	this.action;
	/** The user's message text. */
	this.message;
	/** Include the message debug log in the response. */
	this.debug;
	/** Set the debug level, one of: SEVER, WARNING, INFO, CONFIG, FINE, FINER. */
	this.debugLevel;
	/** Enable or disable the bot's learning for this message. */
	this.learn;
	/** Escape and filter the response message HTML content for XSS security. */
	this.secure = SDK.secure;
	/** Strip any HTML tags from the response message. */
	this.plainText;
	/** Send extra info with the message, such as the user's contact info (name email phone). */
	this.info;
	/** Request a specific avatar (by ID). */
	this.avatar;
	/** Request the response avatar media in HD. */
	this.avatarHD = SDK.hd;
	/** Request the response avatar media in a video or image format. */
	this.avatarFormat = SDK.format;
	/** Translate between the user's language and the bot's language. */
	this.language;
	
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
		if (this.language != null) {
			xml = xml + (" language=\"" + this.language + "\"");
		}
		if (this.avatar) {
			xml = xml + (" avatar=\"" + this.avatar + "\"");
		}
		if (this.avatarHD) {
			xml = xml + (" avatarHD=\"" + this.avatarHD + "\"");
		}
		if (this.avatarFormat != null) {
			xml = xml + (" avatarFormat=\"" + this.avatarFormat + "\"");
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
		if (this.secure != null) {
			xml = xml + (" secure=\"" + this.secure + "\"");
		}
		if (this.plainText != null) {
			xml = xml + (" plainText=\"" + this.plainText + "\"");
		}
		if (this.debug) {
			xml = xml + (" debug=\"" + this.debug + "\"");
		}
		if (this.info) {
			xml = xml + (" info=\"" + SDK.escapeHTML(this.info) + "\"");
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
 * This object models a command message sent to a bot instance.
 * It can be used to send JSON events and commands to the bot to process.
 * It can convert itself to XML for web API usage.
 * @class
 * @property conversation
 * @property speak
 * @property correction
 * @property offensive
 * @property disconnect
 * @property emote
 * @property action
 * @property command
 * @property debug
 * @property debugLevel
 * @property learn
 */
function CommandConfig() {
	/** The conversation id for the message.  This will be returned from the first response, and must be used for all subsequent messages to maintain the conversational state.  Without the conversation id, the bot has no context for the reply. */
	this.conversation;
	/** Sets if the voice audio should be generated for the bot's response. */
	this.speak;
	/** Sets the message to be a correction to the bot's last response. */
	this.correction;
	/** Flags the bot's last response as offensive. */
	this.offensive;
	/** Ends the conversation. Conversation should be terminated to converse server resources.  The message can be blank. */
	this.disconnect;
	/** 
	 * Attaches an emotion to the user's message, one of:
	 *  NONE,
	 *  LOVE, LIKE, DISLIKE, HATE,
	 *	RAGE, ANGER, CALM, SERENE,
	 *	ECSTATIC, HAPPY, SAD, CRYING,
	 *	PANIC, AFRAID, CONFIDENT, COURAGEOUS,
	 *	SURPRISE, BORED,
	 *	LAUGHTER, SERIOUS
	 */
	this.emote;
	/** Attaches an action to the user's messages, such as "laugh", "smile", "kiss". */
	this.action;
	/** The json command. */
	this.command;
	/** Include the message debug log in the response. */
	this.debug;
	/** Set the debug level, one of: SEVER, WARNING, INFO, CONFIG, FINE, FINER. */
	this.debugLevel;
	/** Enable or disable the bot's learning for this message. */
	this.learn;
	/** Escape and filter the response message HTML content for XSS security. */
	this.secure = SDK.secure;
	/** Strip any HTML tags from the response message. */
	this.plainText;
	/** Send extra info with the message, such as the user's contact info (name email phone). */
	this.info;
	/** Request a specific avatar (by ID). */
	this.avatar;
	/** Request the response avatar media in HD. */
	this.avatarHD = SDK.hd;
	/** Request the response avatar media in a video or image format. */
	this.avatarFormat = SDK.format;
	/** Translate between the user's language and the bot's language. */
	this.language;
	
	this.toXML = function() {
		var xml = "<command";
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
		if (this.language != null) {
			xml = xml + (" language=\"" + this.language + "\"");
		}
		if (this.avatarHD) {
			xml = xml + (" avatarHD=\"" + this.avatarHD + "\"");
		}
		if (this.avatarFormat != null) {
			xml = xml + (" avatarFormat=\"" + this.avatarFormat + "\"");
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
		if (this.secure != null) {
			xml = xml + (" secure=\"" + this.secure + "\"");
		}
		if (this.plainText != null) {
			xml = xml + (" plainText=\"" + this.plainText + "\"");
		}
		if (this.debug) {
			xml = xml + (" debug=\"" + this.debug + "\"");
		}
		if (this.info) {
			xml = xml + (" info=\"" + SDK.escapeHTML(this.info) + "\"");
		}
		if (this.debugLevel != null) {
			xml = xml + (" debugLevel=\"" + this.debugLevel + "\"");
		}
		if (this.disconnect) {
			xml = xml + (" disconnect=\"" + this.disconnect + "\"");
		}
		xml = xml + (">");
		
		if (this.command != null) {
			xml = xml + ("<command>");
			xml = xml + (SDK.escapeHTML(this.command));
			xml = xml + ("</command>");
		}
		xml = xml + ("</command>");
		return xml;
	}
}
CommandConfig.prototype = new Config();
CommandConfig.prototype.constructor = CommandConfig;
CommandConfig.constructor = CommandConfig;

/**
 * This object models a chat message received from a chat bot instance.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself from XML for web API usage.
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
 * @property question
 * @property emote
 * @property action
 * @property pose
 * @property command
 * @property log
 */
function ChatResponse() {	
	/** The conversation id for the message.  This will be returned from the first response, and must be used for all subsequent messages to maintain the conversational state.  Without the conversation id, the bot has no context for the reply. */
	this.conversation;
	/** Server relative URL for the avatar image or video. */
	this.avatar;
	/** Second avatar animation. */
	this.avatar2;
	/** Third avatar animation. */
	this.avatar3;
	/** Forth avatar animation. */
	this.avatar4;
	/** Fifth avatar animation. */
	this.avatar5;
	/** Avatar MIME file type, (mpeg, webm, ogg, jpeg, png) */
	this.avatarType;
	/** Server relative URL for the avatar talking image or video. */
	this.avatarTalk;
	/** Avatar talk MIME file type, (mpeg, webm, ogg, jpeg, png) */
	this.avatarTalkType;
	/** Server relative URL for the avatar action image or video. */
	this.avatarAction;
	/** Avatar action MIME file type, (mpeg, webm, ogg, jpeg, png) */
	this.avatarActionType;
	/** Server relative URL for the avatar action audio image or video. */
	this.avatarActionAudio;
	/** Avatar action audio MIME file type,  (mpeg, wav) */
	this.avatarActionAudioType;
	/** Server relative URL for the avatar audio image or video. */
	this.avatarAudio;
	/** Avatar audio MIME file type,  (mpeg, wav) */
	this.avatarAudioType;
	/** Server relative URL for the avatar background image. */
	this.avatarBackground;
	/** Server relative URL for the avatar speech audio file. */
	this.speech;
	/** The bot's message text. */
	this.message;
	/** Optional text to the original question. */
	this.question;
	/**
	 * Emotion attached to the bot's message, one of:
	 *  NONE,
	 *  LOVE, LIKE, DISLIKE, HATE,
	 *	RAGE, ANGER, CALM, SERENE,
	 *	ECSTATIC, HAPPY, SAD, CRYING,
	 *	PANIC, AFRAID, CONFIDENT, COURAGEOUS,
	 *	SURPRISE, BORED,
	 *	LAUGHTER, SERIOUS
	 */
	this.emote;
	/** Action for the bot's messages, such as "laugh", "smile", "kiss", or mobile directive (for virtual assistants). */
	this.action;
	/** Pose for the bot's messages, such as "dancing", "sitting", "sleeping". */
	this.pose;
	/** JSON Command for the bot's message. This can be by the client for mobile virtual assistant functionality, games integration, or other uses.  */
	this.command;
	/** The debug log of processing the message. */
	this.log;

	this.parseXML = function(element) {
		this.conversation = element.getAttribute("conversation");
		this.avatar = element.getAttribute("avatar");
		this.avatar2 = element.getAttribute("avatar2");
		this.avatar3 = element.getAttribute("avatar3");
		this.avatar4 = element.getAttribute("avatar4");
		this.avatar5 = element.getAttribute("avatar5");
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
		this.command = element.getAttribute("command");
		this.speech = element.getAttribute("speech");

		var node = element.getElementsByTagName("message")[0];
		if (node != null) {
			this.message = SDK.innerHTML(node);
		}
		
		node = element.getElementsByTagName("log")[0];
		if (node != null) {
			this.log = SDK.innerHTML(node);
		}
	}
}
ChatResponse.prototype = new Config();
ChatResponse.prototype.constructor = ChatResponse;
ChatResponse.constructor = ChatResponse;

/**
 * This object models an analytic message received from an analytic instance.
 * @class
 * @property label
 * @property confidence
 */
function AnalyticResponse() {
	/** result that is comming from the trained labels, the name of the image */
	this.labels = [];
	/**  result that is coming from the trained graph, the percentage of the image*/
	this.confidences = [];
	
	this.parseXML = function(element) {
		this.parseWebMediumXML(element);
		var nodes = element.getElementsByTagName("result");
		for (var i = 0; i < nodes.length; i++) { 
			if(nodes[i] !=null){
				this.labels.push(nodes[i].getAttribute("label"));
				this.confidences.push(nodes[i].getAttribute("confidence"));
			}
		}
	}
}
AnalyticResponse.prototype = new WebMediumConfig();
AnalyticResponse.prototype.constructor = AnalyticResponse;
AnalyticResponse.constructor = AnalyticResponse;


/**
 * This object models an analytic test media received from an analytic instance.
 * @class
 * @property actualLabel
 * @property actualConfidecne
 * @property expectedLabel
 * @property ExpectedConfidence
 */
function AnalyticTestMediaResponse() {
	
	this.listOfResponses = [];
	
	this.parseXML = function(element) {
		this.parseWebMediumXML(element);
		var nodes = element.getElementsByTagName("analytic-test-result");
		if (nodes.length == 0) {
			nodes = element.getElementsByTagName("analytic-object-detection-response");
		}
		var i;
		for (i = 0; i < nodes.length; i++) { 
			if(nodes[i] !=null){
				var singleResult = {};
				singleResult['name'] = nodes[i].getAttribute("name");
				singleResult['image'] = nodes[i].getAttribute("image");
				singleResult['actualLabel'] = nodes[i].getAttribute("actuallabel");
				singleResult['actualConfidence'] = nodes[i].getAttribute("actualconfidence");
				singleResult['expectedLabel'] = nodes[i].getAttribute("expectedlabel");
				singleResult['expectedConfidence'] = nodes[i].getAttribute("expectedconfidence");
				this.listOfResponses.push(singleResult);
			}
		}
	}
}
AnalyticTestMediaResponse.prototype = new WebMediumConfig();
AnalyticTestMediaResponse.prototype.constructor = AnalyticTestMediaResponse;
AnalyticTestMediaResponse.constructor = AnalyticTestMediaResponse;


/**
 * This object models an analytic message received from an analytic instance.
 * @class
 * @property label
 * @property confidence
 */
function AnalyticAudioResponse() {
	
	this.label;
	this.confidence;

	this.parseXML = function(element) {
		this.parseWebMediumXML(element);
		this.label = element.getAttribute("label");
		this.confidence = element.getAttribute("confidence");
	}

	this.toXML = function() {
		var xml = "<analytic-audio-response";
			if (this.label != null) {
				xml = xml + (" label=\"" + this.label + "\"");
			}
			if (this.confidence != null) {
				xml = xml + (" confidence=\"" + this.confidence + "\"");
			}
			xml = this.writeWebMediumXML(xml);
			xml = xml + ("</analytic-audio-response>");
			return xml;
	}
}
AnalyticAudioResponse.prototype = new WebMediumConfig();
AnalyticAudioResponse.prototype.constructor = AnalyticAudioResponse;
AnalyticAudioResponse.constructor = AnalyticAudioResponse;

/**
 * This object models an analytic message received from an analytic instance.
 * @class
 * @property label
 * @property confidence
 */
function AnalyticObjectDetectionResponse() {
	this.image;
	
	this.numberOfClasses;
	
	this.result = [];
	
	this.bottom;
	this.left;
	this.top;
	this.right;

	this.parseXML = function(element) {
		
		//this.name = element.getAttribute("name");
		console.log(element);
		var node = element.getElementsByTagName("image")[0];
		if (node != null) {
			this.image = SDK.innerHTML(node);
		}
		node = element.getElementsByTagName("numberOfClasses")[0];
		if (node != null) {
			this.numberOfClasses = SDK.innerHTML(node);
		}
		
		var nodes = element.getElementsByTagName("result");
		for (var i = 0; i < nodes.length; i++) { 
			if(nodes[i] != null){
				this.result.push(nodes[i].getAttribute("label") + ": " + nodes[i].getAttribute("confidence") + "%");
				let box = nodes[i].getElementsByTagName("box")[0];
				this.bottom = box.getAttribute("bottom");
				this.left = box.getAttribute("left");
				this.top = box.getAttribute("top");
				this.right = box.getAttribute("right");
				//console.log(box.getAttribute("bottom") + ", " + box.getAttribute("left")+ ", " + box.getAttribute("top")+ ", " + box.getAttribute("right"));
			}
		}
		
	}

	this.toXML = function() {
		var xml = "<analytic-object-detection-response>";
			if (this.image != null) {
				xml = xml + ("<image>" + this.image + "<image>");
			}
			if (this.numberOfClasses != null) {
				xml = xml + ("<numberofclasses>" + this.numberOfClasses + "<numberofclasses>");
			}
			result.forEach(function(x){
				if(x != null){
					xml = xml + ("<result>" + x + "<result>")
				}
			});
			xml = this.writeWebMediumXML(xml);
			xml = xml + ("</analytic-object-detection-response>");
			return xml;
	}
}
AnalyticObjectDetectionResponse.prototype = new Config();
AnalyticObjectDetectionResponse.prototype.constructor = AnalyticObjectDetectionResponse;
AnalyticObjectDetectionResponse.constructor = AnalyticObjectDetectionResponse;


/**
 * This object is returned from the SDK chatSettings() API to retrieve a conversation's chat settings.
 * It can convert itself from XML for web API usage.
 * @class
 * @property conversation
 * @property allowEmotes
 * @property allowCorrection
 * @property allowLearning
 * @property learning
 */
function ChatSettings() {
	this.conversation;
	this.allowEmotes;
	this.allowCorrection;
	this.allowLearning;
	this.learning;

	this.toXML = function() {
		var xml = "<chat-settings";
		xml = this.writeCredentials(xml);
		if (this.conversation != null) {
			xml = xml + (" conversation=\"" + this.conversation + "\"");
		}
		xml = xml + ("/>");
		return xml;
	}
	
	this.parseXML = function(element) {
		this.allowEmotes = "true" == (element.getAttribute("allowEmotes"));
		this.allowCorrection = "true" == (element.getAttribute("allowCorrection"));
		this.allowLearning = "true" == (element.getAttribute("allowLearning"));
		this.learning = "true" == (element.getAttribute("learning"));
	}
}
ChatSettings.prototype = new Config();
ChatSettings.prototype.constructor = ChatSettings;
ChatSettings.constructor = ChatSettings;

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
	this.voiceMod;
	this.message;
	this.emote;
	this.action;
	this.pose;
	this.hd = SDK.hd;
	this.format = SDK.format;
	
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
		if (this.voiceMod != null) {
			xml = xml + (" voiceMod=\"" + this.voiceMod + "\"");
		}
		if (this.format != null) {
			xml = xml + (" format=\"" + this.format + "\"");
		}
		if (this.speak) {
			xml = xml + (" speak=\"" + this.speak + "\"");
		}
		if (this.hd) {
			xml = xml + (" hd=\"" + this.hd + "\"");
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
 * This object models the web API browse operation.
 * It can be used to search a set of instances (bots, forums, or channels).
 * @class
 * @property type
 * @property typeFilter
 * @property category
 * @property tag
 * @property filter
 * @property sort
 */
function BrowseConfig() {
	/** Filters instances by access type, "Public", "Private", "Personal". */
	this.typeFilter;
	/** Filters instances by categories (csv) */
	this.category;
	/** Filters instances by tags (csv) */
	this.tag;
	/** Filters instances by name */
	this.filter;
	/** Sorts instances, "name", "date", "size", "stars", "thumbs up", "thumbs down", "last connect", "connects", "connects today", "connects this week ", "connects this month" */
	this.sort;
	
	this.toXML = function() {
		var xml = "<browse";
		xml = this.writeCredentials(xml);
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
 * Abstract content class.
 * This object models a content object such as a bot, forum, or channel.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself to/from XML for web API usage.
 * This can be used to create, edit, or browse a content.
 * @class
 * @property id
 * @property name
 * @property isAdmin
 * @property isAdult
 * @property isPrivate
 * @property isHidden
 * @property accessMode
 * @property isFlagged
 * @property flaggedReason
 * @property isExternal
 * @property description
 * @property details
 * @property disclaimer
 * @property tags
 * @property categories
 * @property creator
 * @property creationDate
 * @property lastConnectedUser
 * @property website
 * @property license
 * @property avatar
 * @property connects
 * @property dailyConnects
 * @property weeklyConnects
 * @property monthlyConnects
 */
function WebMediumConfig() {
	/** Instance ID. */
	this.id;
	/** Instance name. */
	this.name;
	/** Read-only, returns if connected user is the content's admin. */
	this.isAdmin;
	this.isAdult;
	/** Sets if the content is private to the creator, and its members. */
	this.isPrivate;
	/** Sets if the conent will be visible and searchable in the content directory. */
	this.isHidden;
	/** Sets the access mode for the content, ("Everyone", "Users", "Members", "Administrators"). */
	this.accessMode;
	/** Returns if the content has been flagged, or used to flag content as offensive (reason required). */
	this.isFlagged;
	/** Returns why the content has been flagged, or used to flag content as offensive. */
	this.flaggedReason;
	/** Can be used to create a link to external content in the content directory. */
	this.isExternal;
	/** Optional description of the content. */
	this.description;
	/** Optional restrictions or details of the content. */
	this.details;
	/** Optional warning or disclaimer of the content. */
	this.disclaimer;
	/** Tags to classify the content (csv). */
	this.tags;
	/** Categories to categorize the content under (csv). */
	this.categories;
	/** Read-only, returns content's creator's user ID. */
	this.creator;
	/** Read-only, returns content's creation date. */
	this.creationDate;
	/** Read-only, returns last user to access content */
	this.lastConnectedUser;
	/** Optional license to license the content under. */
	this.license;
	/** Optional website related to the content. */
	this.website = "";
	/** Read-only, server local URL to content's avatar image. */
	this.avatar;
	/** Read-only, returns content's toal connects. */
	this.connects;
	/** Read-only, returns content's daily connects. */
	this.dailyConnects;
	/** Read-only, returns content's weekly connects. */
	this.weeklyConnects;
	/** Read-only, returns content's monthly connects. */
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
			xml = xml + (SDK.escapeHTML(this.disclaimer));
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
		this.isPrivate = element.getAttribute("isPrivate") == "true";
		this.isHidden = element.getAttribute("isHidden") == "true";
		this.accessMode = element.getAttribute("accessMode");
		this.isAdmin = element.getAttribute("isAdmin") == "true";
		this.isAdult = element.getAttribute("isAdult") == "true";
		this.isFlagged = element.getAttribute("isFlagged") == "true";
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
 * This object models a live chat channel or chatroom instance.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself to/from XML for web API usage.
 * This can be used to create, edit, or browse a channel instance.
 * @class
 * @property type
 * @property messages
 * @property usersOnline
 * @property adminsOnline
 */
function ChannelConfig() {
	/** Sets type, "ChatRoom", "OneOnOne". */
	this.type;
	/** Read-only: total number of messages. */
	this.messages;
	/** Read-only: current users online. */
	this.usersOnline;
	/** Read-only: current admins or operators online. */
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
 * DTO to parse response of a list of names.
 * This is used for categories, tags, and templates.
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
		
		xml = xml + ("/>");
		return xml;
	}
}
ContentConfig.prototype = new Config();
ContentConfig.prototype.constructor = ContentConfig;
ContentConfig.constructor = ContentConfig;

/**
 * This object models a domain.
 * It can be used from a chat UI, or with the Libre Web API.
 * A domain is an isolated content space to create bots and other content in (such as a commpany, project, or school).
 * It can convert itself to/from XML for web API usage.
 * This can be used to create, edit, or browse a domain instance.
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
		xml = this.writeWebMediumXML(xml);
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
 * This object models an avatar.
 * An avatar represents a bot's visual image, but can also be used independently with TTS.
 * It can convert itself to/from XML for web API usage.
 * This can be used to create, edit, or browse an avatar instance.
 * @class
 */
function AvatarConfig() {
	
	this.type = "avatar";
	
	this.credentials = function() {
		var config = new AvatarConfig();
		config.id = this.id;
		return config;
	}
	
	this.toXML = function() {
		var xml = "<avatar";
		xml = this.writeWebMediumXML(xml);
		xml = xml + ("</avatar>");
		return xml;
	}
	
	this.parseXML = function(element) {
		this.parseWebMediumXML(element);
	}
}
AvatarConfig.prototype = new WebMediumConfig();
AvatarConfig.prototype.constructor = AvatarConfig;
AvatarConfig.constructor = AvatarConfig;

/**
 * This object models a script from the script library.
 * It can convert itself to/from XML for web API usage.
 * This can be used to create, edit, or browse an avatar instance.
 * @class
 */
function ScriptConfig() {
	
	this.type = "script";
	
	this.credentials = function() {
		var config = new AvatarConfig();
		config.id = this.id;
		return config;
	}
	
	this.toXML = function() {
		var xml = "<script";
		xml = this.writeWebMediumXML(xml);
		xml = xml + ("</script>");
		return xml;
	}
	
	this.parseXML = function(element) {
		this.parseWebMediumXML(element);
	}
}
ScriptConfig.prototype = new WebMediumConfig();
ScriptConfig.prototype.constructor = ScriptConfig;
ScriptConfig.constructor = ScriptConfig;

/**
 * This object models a script source from the script library.
 * It can convert itself to/from XML for web API usage.
 * This can be used to create, edit, or browse bot's scripts.
 * @class
 */
function ScriptSourceConfig() {
	
	this.type = "script";
	var id;
	var creationDate;
	var updateDate;
	var version;
	var versionName;
	var creator;
	var source;
	var instance;
	
	this.credentials = function() {
		var config = new ScriptSourceConfig();
		config.id = this.id;
		config.creationDate = this.creationDate;
		config.updateDate = this.updateDate;
		config.version = this.version;
		config.versionName = this.versionName;
		config.creator = this.creator;
		config.source = this.source;
		config.instance = this.instance;
		return config;
	}
	
	this.toXML = function() {
		var xml = "<script-source";
		xml = this.writeCredentials(xml);
		if (this.id != null) {
			xml = xml + (" id=\"" + this.id + "\"");
		}
		if (this.creationDate != null) {
			xml = xml + (" creationDate=\"" + this.creationDate + "\"");
		}
		if (this.updateDate != null) {
			xml = xml + (" updateDate=\"" + this.updateDate + "\"");
		}
		if (this.version) {
			xml = xml + (" version=\"" + this.version + "\"");
		}
		if (this.versionName != null) {
			xml = xml + (" versionName=\"" + this.versionName + "\"");
		}
		if (this.creator != null) {
			xml = xml + (" creator=\"" + this.creator + "\"");
		}
		if (this.source != null) {
			xml = xml + (" source=\"" + this.source + "\"");
		}
		xml = xml + (">");
		xml = xml + ("</script-source>");
		return xml;
	}
	
	this.parseXML = function(element) {
		this.parseWebMediumXML(element);
	}
}
ScriptSourceConfig.prototype = new Config();
ScriptSourceConfig.prototype.constructor = ScriptSourceConfig;
ScriptSourceConfig.constructor = ScriptSourceConfig;

/**
 * Adds or removes bot's users
 * @class
 */
function UserAdminConfig() {
	this.type;
	this.operation;
	this.operationUser;
	
	this.credentials = function() {
		var config = new UserAdminConfig();
		config.type = this.type;
		config.operation = this.operation;
		config.operationUser = this.operationUser;
		config.instance = this.instance;
		return config;
	}
	
	this.toXML = function() {
		var xml = "<user-admin";
		xml = this.writeCredentials(xml);
		if (this.operation != null) {
			xml = xml + (" operation=\"" + this.operation + "\"");
		}
		if (this.operationUser != null) {
			xml = xml + (" operationUser=\"" + this.operationUser + "\"");
		}
		if (this.source != null) {
			xml = xml + (" source=\"" + this.source + "\"");
		}
		xml = xml + (">");
		xml = xml + ("</user-admin>");
		return xml;
	}
	
	this.parseXML = function(element) {
		this.parseWebMediumXML(element);
	}
}
UserAdminConfig.prototype = new Config();
UserAdminConfig.prototype.constructor = UserAdminConfig;
UserAdminConfig.constructor = UserAdminConfig;

/**
 * Adds or removes user's friends
 * @class
 */
function UserFriendsConfig() {
	this.action;
	this.userFriend;
	this.friendship;
	this.instance;
	
	this.credentials = function() {
		var config = new UserFriendsConfig();
		config.action = this.action;
		config.userFriend = this.userFriend;
		config.friendship = this.friendship;
		config.instance = this.instance;
		return config;
	}
	
	this.toXML = function() {
		var xml = "<user-friends";
		xml = this.writeCredentials(xml);
		if (this.action != null) {
			xml = xml + (" action=\"" + this.action + "\"");
		}
		if (this.userFriend != null) {
			xml = xml + (" userFriend=\"" + this.userFriend + "\"");
		}
		if (this.friendship != null) {
			xml = xml + (" friendship=\"" + this.friendship + "\"");
		}
		if (this.source != null) {
			xml = xml + (" source=\"" + this.source + "\"");
		}
		xml = xml + (">");
		xml = xml + ("</user-friends>");
		return xml;
	}
	
	this.parseXML = function(element) {
		this.userFriend = element.getAttribute("userFriend");
		this.friendship = element.getAttribute("friendship");
	}
}
UserFriendsConfig.prototype = new Config();
UserFriendsConfig.prototype.constructor = UserFriendsConfig;
UserFriendsConfig.constructor = UserFriendsConfig;

/**
 * This object models a graphic from the graphics library.
 * It can convert itself to/from XML for web API usage.
 * This can be used to create, edit, or browse an avatar instance.
 * @class
 */
function GraphicConfig() {
	this.media;
	
	this.type = "graphic";
	
	this.credentials = function() {
		var config = new AvatarConfig();
		config.id = this.id;
		return config;
	}
	
	this.toXML = function() {
		var xml = "<graphic";
		xml = this.writeWebMediumXML(xml);
		xml = xml + ("</graphic>");
		return xml;
	}
	
	this.parseXML = function(element) {
		this.parseWebMediumXML(element);
		this.media = element.getAttribute("media");
	}
}
GraphicConfig.prototype = new WebMediumConfig();
GraphicConfig.prototype.constructor = GraphicConfig;
GraphicConfig.constructor = GraphicConfig;

/**
 * This object models a forum instance.
 * It can be used from a chat UI, or with the Libre Web API.
 * It can convert itself to/from XML for web API usage.
 * This can be used to create, edit, or browse a forum instance.
 * @class
 * @property replyAccessMode
 * @property postAccessMode
 * @property posts
 */
function ForumConfig() {
	/** Sets the access mode for forum post replies, ("Everyone", "Users", "Members", "Administrators"). */
	this.replyAccessMode;
	/** Sets the access mode for forum posts, ("Everyone", "Users", "Members", "Administrators"). */
	this.postAccessMode;
	/** Read-only property for the total number of posts to the forum. */
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
 * This object models a forum post.
 * It can be used from a forum UI, or with the Libre Web API.
 * It can convert itself to/from XML for web API usage.
 * You must set the forum id as the forum of the forum post.
 * A forum post that has a parent (parent forum post id) is a reply.
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
		this.isAdmin = element.getAttribute("isAdmin") == "true";
		this.replyCount = element.getAttribute("replyCount");
		this.isFlagged = element.getAttribute("isFlagged") == "true";
		this.isFeatured = element.getAttribute("isFeatured") == "true";
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
	/** Read-only : the current size of the bot's knowledge base. **/
	this.size;
	/** Sets if the bot can be forked. */
	this.allowForking;
	/** Sets the name or id of a bot to clone to create a new bot. */
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
		this.allowForking = element.getAttribute("allowForking") == "true";
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
 * The analytic config object is used the send the image and retrieve the results of the image.
 * @see {@link Config}
 * @class
 */
function AnalyticConfig() {
	this.analyticType;
	this.analyticFeed;
	this.analyticFetch;
	this.imageSize;
	this.trainingStatus;
	this.processingTestMediaStatus;
	this.isTraining;
	this.isProcessingMedia;
	this.audioInputName;
	this.audioOutputName;

	this.parseXML = function (element) {
		this.parseWebMediumXML(element);
		this.analyticType = element.getAttribute("analyticType");
		this.analyticFeed = element.getAttribute("analyticFeed");
		this.analyticFetch = element.getAttribute("analyticFetch");
		this.imageSize = element.getAttribute("imageSize");
		this.trainingStatus = element.getAttribute("trainingStatus");
		this.isTraining = element.getAttribute("isTraining") == "true";
		this.audioInputName = element.getAttribute("audioInputName");
		this.audioOutputName = element.getAttribute("audioOutputName");
		this.processingTestMediaStatus = element.getAttribute("processingTestMediaStatus");
		this.isProcessingMedia = element.getAttribute("isProcessingMedia") == "true";
	}

	this.credentials = function() {
		var config = new AnalyticConfig();
		config.id = this.id;
		return config;
	}
	this.toXML = function() {
		var xml = "<analytic";
		if (this.analyticType != null) {
			xml = xml + (" analyticType=\"" + this.analyticType + "\"");
		}
		if (this.analyticFeed != null) {
			xml = xml + (" analyticFeed=\"" + this.analyticFeed + "\"");
		}
		if (this.analyticFetch != null) {
			xml = xml + (" analyticFetch=\"" + this.analyticFetch + "\"");
		}
		if (this.imageSize != null) {
			xml = xml + (" imageSize=\"" + this.imageSize + "\"");
		}
		if (this.trainingStatus != null) {
			xml = xml + (" trainingStatus=\"" + this.trainingStatus + "\"");
		}
		if (this.processingTestMediaStatus != null) {
			xml = xml + (" processingTestMediaStatus=\"" + this.processingTestMediaStatus + "\"");
		}
		if (this.isTraining != null) {
			xml = xml + (" isTraining=\"" + this.isTraining + "\"");
		}
		if (this.isProcessingMedia != null) {
			xml = xml + (" isProcessingMedia=\"" + this.isProcessingMedia + "\"");
		}
		if (this.audioInputName != null) {
			xml = xml + (" audioInputName=\"" + this.audioInputName + "\"");
		}
		if (this.audioOutputName != null) {
			xml = xml + (" audioOutputName=\"" + this.audioOutputName + "\"");
		}
		xml = this.writeWebMediumXML(xml);
		xml = xml + ("</analytic>");
		return xml;
	}
}
AnalyticConfig.prototype = new WebMediumConfig();
AnalyticConfig.prototype.constructor = AnalyticConfig;
AnalyticConfig.constructor = AnalyticConfig;


/**
 * The analytic config object is used the send the image and retrieve the results of the image.
 * @see {@link Config}
 * @class
 */
function AnalyticConfigTest() {
	this.resultImage;
	this.threshold;

	this.parseXML = function (element) {
		this.resultImage = element.getAttribute("result-image");
		this.threshold = element.getAttribute("threshold");
	}

	this.credentials = function() {
		var config = new AnalyticConfigTest();
		config.instance= this.instance;
		return config;
	}
	this.toXML = function() {
		var xml = "<analytic-object-test";
		if (this.resultImage != null) {
			xml = xml + (" result-image=\"" + this.resultImage + "\"");
		}
		if (this.threshold != null) {
			xml = xml + (" threshold=\"" + this.threshold + "\"");
		}
		xml = this.writeCredentials(xml);
		xml = xml + ("></analytic-object-test>");
		return xml;
	}
}
AnalyticConfigTest.prototype = new Config();
AnalyticConfigTest.prototype.constructor = AnalyticConfigTest;
AnalyticConfigTest.constructor = AnalyticConfigTest;


/**
 * The analytic media config object is used upload and delete images from MediaRepository
 * @see {@link Config}
 * @class
 */
function AnalyticMediaConfig() {
	this.mediaId;
	this.label;
	this.media;
	this.name;
	this.mediaType;

	this.parseXML = function (element) {
		this.mediaId = element.getAttribute("mediaId");
		this.label = element.getAttribute("label");
		this.media = element.getAttribute("media");
		this.name = element.getAttribute("name");
		this.type = element.getAttribute("mediaType");
	}
	
	this.toXML = function() {
		var xml = "<analytic-media";
		xml = this.writeCredentials(xml);
		if (this.mediaId != null) {
			xml = xml + (" mediaId=\"" + this.mediaId + "\"");
		}
		if (this.label != null) {
			xml = xml + (" label=\"" + this.label + "\"");
		}
		if (this.media != null) {
			xml = xml + (" media=\"" + this.media + "\"");
		}
		if (this.name != null) {
			xml = xml + (" name=\"" + this.name + "\"");
		}
		if (this.mediaType != null) {
			xml = xml + (" mediaType=\"" + this.mediaType + "\"");
		}
		xml = xml + ("></analytic-media>");
		return xml;
	}
}
AnalyticMediaConfig.prototype = new Config();
AnalyticMediaConfig.prototype.constructor = AnalyticMediaConfig;
AnalyticMediaConfig.constructor = AnalyticMediaConfig;

/**
 * The analytic media config object is used upload and delete images from MediaRepository
 * @see {@link Config}
 * @class
 */
function LabelConfig() {
	this.label;

	this.parseXML = function (element) {
		this.label = element.getAttribute("label");
	}
	
	this.toXML = function() {
		var xml = "<label";
		xml = this.writeCredentials(xml);
		if (this.label != null) {
			xml = xml + (" label=\"" + this.label + "\"");
		}
		xml = xml + ("></label>");
		return xml;
	}
}
LabelConfig.prototype = new Config();
LabelConfig.prototype.constructor = LabelConfig;
LabelConfig.constructor = LabelConfig;

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
	/** Voice language code (en, fr, en_US, etc.) */
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
 * @property question
 * @property response
 */
function TrainingConfig() {
	/** Type of response ("Response", "Greeting", "DefaultResponse"). */
	this.operation;
	/** The question phrase or pattern (i.e. "hello", "what is your name", "Pattern:^ help ^"). */
	this.question;
	/** The response phrase or formula (i.e. "Hello there.", "Formula:"My name is {:target}."", "What would you like help with?"). */
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

/**
 * Allow for botlibre.SDK namespace.
 */
var botlibre = {};
botlibre.SDK = {};
for (var attr in SDK) {
	botlibre.SDK[attr] = SDK[attr];
}

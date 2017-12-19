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

package org.botlibre.sdk.activity;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpChatAction;
import org.botlibre.sdk.activity.actions.HttpFetchChatAvatarAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.activity.actions.HttpGetVideoAction;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ChatConfig;
import org.botlibre.sdk.config.ChatResponse;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.VoiceConfig;
import org.botlibre.sdk.micro.TextToSpeech;
import org.botlibre.sdk.util.Command;
import org.botlibre.sdk.util.TextStream;
import org.botlibre.sdk.util.Utils;
import org.json.JSONObject;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
import org.botlibre.sdk.R;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.VideoView;
import marytts.util.data.audio.AudioPlayer.Status;

/**
 * Activity for chatting with a bot.
 * To launch this activity from your app you can use the HttpFetchAction passing the bot id or name as a config, and launch=true.
 */
public class ChatActivity extends LibreActivity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
	protected static final int RESULT_SPEECH = 1;
	protected static final int CAPTURE_IMAGE = 2;
	protected static final int RESULT_SCAN = 3;
	protected static final int CAPTURE_VIDEO = 4;
	protected static boolean DEBUG;
	private boolean active = true;
	private boolean isPlaying = false;
	public static Activity activity;
	
	private boolean isRecording;
	public static boolean isListening;
	
	public class WebAppInterface {
	    Context context;

	    WebAppInterface(Context context) {
	    	this.context = context;
	    }
	    
	    public void postback(final String message) {
	    	try {
				final EditText messageText = (EditText) findViewById(R.id.messageText);
		    	messageText.post(new Runnable() {
					@Override
					public void run() {
						try {
					    	messageText.setText(message);
					    	submitChat();
				    	} catch (Throwable error) {
				    		error.printStackTrace();
				    	}
					}
				});
	    	} catch (Throwable error) {
	    		error.printStackTrace();
	    	}
	    }
	}

	protected TextToSpeech tts;
	protected boolean ttsInit = false;
	public VideoView videoView;
	public View videoLayout;
	public ImageView imageView;
	protected EditText textView;
	private boolean volumeChecked = true;
	private Thread thread;
	
	
	private int stateLayouts = 0;
	private View scrollView;
	public boolean music = false;
	
	private double lastReply =  System.currentTimeMillis();
	
	public static List<Object> messages = new ArrayList<Object>();
	public ChatResponse response;
	public MediaPlayer audioPlayer;
	public String currentAudio;

	public boolean videoError;
	protected volatile boolean wasSpeaking;
	protected InstanceConfig instance;
	
	protected AvatarConfig avatar;
	protected String avatarId;
	protected boolean changingVoice;
	MediaPlayer speechPlayer;
	protected Random random = new Random();
	Thread videoThread;
	protected Menu menu;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		this.instance = (InstanceConfig)MainActivity.instance;
		if (this.instance == null) {
			return;
		}
		
		setTitle(MainActivity.launchInstanceName);
		((TextView) findViewById(R.id.title)).setText(MainActivity.launchInstanceName);
        ImageView.setImage(MainActivity.offlineSelectedImage, findViewById(R.id.icon),32,32);
		
		tts = new TextToSpeech();
		if ("Eddie".equals(MainActivity.launchInstanceName)) {
			tts.setVoice("dfki-spike-hsmm");
		} else {
			tts.setVoice("cmu-slt-hsmm");
		}
//		speech = SpeechRecognizer.createSpeechRecognizer(this);
//		speech.setRecognitionListener(this);
		//scrollVie added and stuff
		scrollView = findViewById(R.id.chatList);
//		menuMLayout = (LinearLayout) findViewById(R.id.menuMLayout);
//		chatCLayout = (LinearLayout) findViewById(R.id.chatCLayout);
//		responseLayout = (LinearLayout) findViewById(R.id.responseLayout);
//		chatToolBar = (LinearLayout) findViewById(R.id.chatToolBar);
		
		videoView = (VideoView)findViewById(R.id.videoView);
		resetVideoErrorListener();
		videoError = false;
		
		imageView = (ImageView)findViewById(R.id.imageView);
		videoLayout = findViewById(R.id.videoLayout);
		
		textView = (EditText) findViewById(R.id.messageText);
		textView.setOnEditorActionListener(new OnEditorActionListener() {			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				submitChat();
				return false;
			}
		});

		if (MainActivity.translate) {
			//findViewById(R.id.yandex).setVisibility(View.VISIBLE);
		} else {
			//findViewById(R.id.yandex).setVisibility(View.GONE);
		}

		Spinner emoteSpin = (Spinner) findViewById(R.id.emoteSpin);
		emoteSpin.setAdapter(new EmoteSpinAdapter(this, R.layout.emote_list, Arrays.asList(EmotionalState.values())));
		
		ListView list = (ListView) findViewById(R.id.chatList);
		list.setAdapter(new ChatListAdapter(this, R.layout.chat_list, this.messages));
		list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		list.clear();
		/*ImageButton	button = (ImageButton) findViewById(R.id.speakButton);
		button.setOnClickListener(new View.OnClickListener() { 
			@Override
			public void onClick(View v) { 
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); 
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, MainActivity.voice.language);
 
				try {
					startActivityForResult(intent, RESULT_SPEECH);
					textView.setText("");
				} catch (ActivityNotFoundException a) {
					//new toast updated
					new Toast (Activity.active.frame, true,"Your device doesn't support Speech to Text").showToast(Toast.LENGTH_SHORT);
				}
			}
		});*/
		
		
		//adding functionality on clicking the image  
		imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(stateLayouts == 4){
					stateLayouts = 0;
					}
				switch(stateLayouts) {
				case 0:
					scrollView.setVisibility(View.VISIBLE);
//					chatCLayout.setVisibility(View.VISIBLE);
//					menuMLayout.setVisibility(View.VISIBLE);
//					responseLayout.setVisibility(View.VISIBLE);
//					chatToolBar.setVisibility(View.VISIBLE);
					break;
				case 1:
					scrollView.setVisibility(View.GONE);
					break;
				case 2:
//					responseLayout.setVisibility(View.GONE);
//					chatToolBar.setVisibility(View.GONE);
					break;
				case 3:
//					menuMLayout.setVisibility(View.GONE);
//					chatCLayout.setVisibility(View.GONE);
					break;
				}
				stateLayouts++;
			}
		});
		
		
		
		//adding functionality on clicking the image 
		videoLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(stateLayouts == 4){
					stateLayouts = 0;
					}
				switch(stateLayouts) {
				case 0:
					scrollView.setVisibility(View.VISIBLE);
//					chatCLayout.setVisibility(View.VISIBLE);
//					menuMLayout.setVisibility(View.VISIBLE);
//					responseLayout.setVisibility(View.VISIBLE);
//					chatToolBar.setVisibility(View.VISIBLE);
					break;
				case 1:
					scrollView.setVisibility(View.GONE);
					break;
				case 2:
//					responseLayout.setVisibility(View.GONE);
//					chatToolBar.setVisibility(View.GONE);
					break;
				case 3:
//					menuMLayout.setVisibility(View.GONE);
//					chatCLayout.setVisibility(View.GONE);
					break;
				}
				stateLayouts++;
			}
		});

//		findViewById(R.id.responseImageView).setOnClickListener(new View.OnClickListener() {			
//			@Override
//			public void onClick(View v) {
//				View avatarLayout = findViewById(R.id.avatarLayout);
//				if (avatarLayout.getVisibility() == View.VISIBLE) {
//					avatarLayout.setVisibility(View.GONE);
//				} else {
//					avatarLayout.setVisibility(View.VISIBLE);
//				}
//			}
//		});
		
		
		if(!instance.name.equals("Help Bot")){
			ImageView.setImage(MainActivity.offlineSelectedImage, this.imageView, 0, 0);
//					ImageView.setImage(MainActivity.offlineSelectedImage, findViewById(R.id.responseImageView),80,80);
//					ImageView.setImage(MainActivity.offlineSelectedImage, findViewById(R.id.icon),80,80);
		} else if (instance.name.equals("Help Bot")) {
			HttpGetImageAction.fetchImage(this, this.instance.avatar, findViewById(R.id.icon));
			HttpGetImageAction.fetchImage(this, instance.avatar, this.imageView);
			HttpGetImageAction.fetchImage(this, instance.avatar, (ImageView)findViewById(R.id.responseImageView));
		}
		
		ChatConfig config = new ChatConfig();
		config.instance = instance.id;
		config.avatar = this.avatarId;
		if (MainActivity.translate && MainActivity.voice != null) {
			config.language = MainActivity.voice.language;
		}
		if (MainActivity.disableVideo) {
			config.avatarFormat = "image";
		} else {
			config.avatarFormat = MainActivity.webm ? "webm" : "mp4";
		}
		config.avatarHD = MainActivity.hd;
		config.speak = !MainActivity.deviceVoice;
		HttpAction action = new HttpChatAction(ChatActivity.this, config);
		action.execute();
	}

	public void resetVideoErrorListener() {
		videoView.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.wtf("Video error", "what:" + what + " extra:" + extra);
				videoError = true;
				return true;
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
 
		switch (requestCode) {
			case RESULT_SPEECH: {
				if (resultCode == RESULT_OK && null != data) {
	 
					ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	 
					textView.setText(text.get(0));
					submitChat();
				}
				break;
			}
		}
//		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//		if (scanResult != null) {
//			textView.setText("lookup " + scanResult.getContents());
//			submitChat();
//			if (scanResult.getContents().startsWith("http")) {
//				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scanResult.getContents()));
//				startActivity(intent);
//			}
//		}
	}

	public void flagResponse() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to flag a response", this);
        	return;
        }
        final EditText text = new EditText(this);
        MainActivity.prompt("Enter reason for flagging response as offensive", this, text, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	    		ChatConfig config = new ChatConfig();
	    		config.instance = instance.id;
	    		config.conversation = MainActivity.conversation;
	    		config.speak = !MainActivity.deviceVoice;
	    		config.avatar = avatarId;
	    		if (MainActivity.translate && MainActivity.voice != null) {
	    			config.language = MainActivity.voice.language;
	    		}
	    		if (MainActivity.disableVideo) {
	    			config.avatarFormat = "image";
	    		} else {
	    			config.avatarFormat = MainActivity.webm ? "webm" : "mp4";
	    		}
	    		config.avatarHD = MainActivity.hd;
	    		
	    		config.message = text.getText().toString().trim();
	    		if (config.message.equals("")) {
	    			return;
	    		}
	    		messages.add(config);
	    		ListView list = (ListView) findViewById(R.id.chatList);
	    		list.invalidateViews();
	    		
	    		config.offensive = true;

	    		Spinner emoteSpin = (Spinner) findViewById(R.id.emoteSpin);
	    		config.emote = emoteSpin.getSelectedItem().toString();
	    		
	    		HttpChatAction action = new HttpChatAction(ChatActivity.this, config);
	    		action.execute();

	    		EditText v = (EditText) findViewById(R.id.messageText);
	    		v.setText("");
	    		emoteSpin.setSelection(0);
	    		resetToolbar();
	    		
	    		WebView responseView = (WebView) findViewById(R.id.responseText);
	    		responseView.loadDataWithBaseURL(null, "thinking...", "text/html", "utf-8", null);
	        }
        });
	}

	public void submitCorrection() {
        final EditText text = new EditText(this);
        MainActivity.prompt("Enter correction to the bot's response (what it should have said)", this, text, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	    		ChatConfig config = new ChatConfig();
	    		config.instance = instance.id;
	    		config.conversation = MainActivity.conversation;
	    		config.speak = !MainActivity.deviceVoice;
	    		config.avatar = avatarId;
	    		if (MainActivity.disableVideo) {
	    			config.avatarFormat = "image";
	    		} else {
	    			config.avatarFormat = MainActivity.webm ? "webm" : "mp4";
	    		}
	    		config.avatarHD = MainActivity.hd;
	    		
	    		config.message = text.getText().toString().trim();
	    		if (config.message.equals("")) {
	    			return;
	    		}
	    		messages.add(config);
	    		ListView list = (ListView) findViewById(R.id.chatList);
	    		list.invalidateViews();
	    		
	    		config.correction = true;

	    		Spinner emoteSpin = (Spinner) findViewById(R.id.emoteSpin);
	    		config.emote = emoteSpin.getSelectedItem().toString();
	    		
	    		HttpChatAction action = new HttpChatAction(ChatActivity.this, config);
	    		action.execute();

	    		EditText v = (EditText) findViewById(R.id.messageText);
	    		v.setText("");
	    		emoteSpin.setSelection(0);
	    		resetToolbar();
	    		
	    		WebView responseView = (WebView) findViewById(R.id.responseText);
	    		responseView.loadDataWithBaseURL(null, "thinking...", "text/html", "utf-8", null);
	        }
        });
	}

	public void submitChat() {
		ChatConfig config = new ChatConfig();
		config.instance = this.instance.id;
		config.conversation = MainActivity.conversation;
		config.speak = !MainActivity.deviceVoice;
		config.avatar = this.avatarId;
		if (MainActivity.translate && MainActivity.voice != null) {
			config.language = MainActivity.voice.language;
		}
		if (MainActivity.disableVideo) {
			config.avatarFormat = "image";
		} else {
			config.avatarFormat = MainActivity.webm ? "webm" : "mp4";
		}
		config.avatarHD = MainActivity.hd;
		
		EditText v = (EditText) findViewById(R.id.messageText);
		config.message = v.getText().toString().trim();
		if (config.message.equals("")) {
			return;
		}
		this.messages.add(config);
		ListView list = (ListView) findViewById(R.id.chatList);
		
		//add list to the list
		list.addItem("You: "+config.message);
		
		list.invalidateViews();

		Spinner emoteSpin = (Spinner) findViewById(R.id.emoteSpin);
		config.emote = emoteSpin.getSelectedItem().toString();
		
		HttpChatAction action = new HttpChatAction(ChatActivity.this, config);
		action.execute();

		v.setText("");
		emoteSpin.setSelection(0);
		resetToolbar();
		
		WebView responseView = (WebView) findViewById(R.id.responseText);
		responseView.loadDataWithBaseURL(null, "thinking...", "text/html", "utf-8", null);
	}

	public void toggleSound(View view) {
		toggleSound();
	}
	
	public void toggleSound() {
		MainActivity.sound = !MainActivity.sound;
		resetToolbar();
	}
	
	public void toggleDisableVideo() {
		if (this.videoError) {
			this.videoError = false;
			MainActivity.disableVideo = false;
		} else {
			MainActivity.disableVideo = !MainActivity.disableVideo;
		}
	}
	
	public void changeAvatar() {
		MainActivity.browsing = true;
		BrowseConfig config = new BrowseConfig();
		config.type = "Avatar";
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}
	
	public void changeVoice() {
        this.changingVoice = true;
        Intent intent = new Intent(this, ChangeVoiceActivity.class);		
        startActivity(intent);
	}
	
	public void toggleDeviceVoice() {
		MainActivity.deviceVoice = !MainActivity.deviceVoice;
	}

	public void toggleFlag(View view) {
		flagResponse();
	}

	public void toggleCorrection(View view) {
		submitCorrection();
	}

	public void menu(View view) {
		openOptionsMenu();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.layout.menu_chat, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		this.menu = menu;
		resetMenu();
		return true;
	}
	
	public void resetMenu() {
		if (this.menu == null) {
			return;
		}
		this.menu.findItem(R.id.menuSound).setChecked(MainActivity.sound);
		this.menu.findItem(R.id.menuDeviceVoice).setChecked(MainActivity.deviceVoice);
		this.menu.findItem(R.id.menuDisableVideo).setChecked(MainActivity.disableVideo || this.videoError);
		this.menu.findItem(R.id.menuHD).setChecked(MainActivity.hd);
		this.menu.findItem(R.id.menuWebm).setChecked(MainActivity.webm);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		 
		switch (item.getItemId())
		{
	    case R.id.menuScanBarcode:
//	    	scanBarcode(null);
	    	return true;
	    case R.id.menuChangeLanguage:
	    	MainActivity.changeLanguage(this, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					resetTTS();
					if (MainActivity.translate) {
						findViewById(R.id.yandex).setVisibility(View.VISIBLE);
					} else {
						findViewById(R.id.yandex).setVisibility(View.GONE);
					}
				}
			});
	        return true;
		case R.id.menuChangeVoice:
			changeVoice();
			return true;
		case R.id.menuSound:
			toggleSound();
			return true;
		case R.id.menuDeviceVoice:
			toggleDeviceVoice();
			return true;
		case R.id.menuDisableVideo:
			toggleDisableVideo();
			return true;
		case R.id.menuCorrection:
			submitCorrection();
			return true;
		case R.id.menuFlag:
			flagResponse();
			return true;
		case R.id.menuHD:
			MainActivity.hd = !MainActivity.hd;
			return true;
		case R.id.menuWebm:
			MainActivity.webm = !MainActivity.webm;
			return true;
		case R.id.menuChangeAvatar:
			changeAvatar();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Disconnect from the conversation.
	 */
	public void disconnect(View view) {		
		finish();
	}

	public void resetToolbar() {
		if (MainActivity.sound) {
			findViewById(R.id.soundButton).setBackgroundResource(R.drawable.sound);
		} else {
			findViewById(R.id.soundButton).setBackgroundResource(R.drawable.mute);
		}
	}
	
	/**
	 * Clear the log.
	 */
	public void clear(View view) {
		WebView log = (WebView) findViewById(R.id.logText);
		log.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
	}
	
	@Override
	public void onResume() {
		MainActivity.searching = false;
		MainActivity.searchingPosts = false;
		if (MainActivity.browsing && (MainActivity.instance instanceof AvatarConfig)) {
			if (MainActivity.user == null || MainActivity.user.type == null || MainActivity.user.type.isEmpty() || MainActivity.user.type.equals("Basic")) {
				//MainActivity.showMessage("You must upgrade to get access to this avatar", this);
				//super.onResume();
				//return;
			}
			this.avatar = (AvatarConfig)MainActivity.instance;
			this.avatarId = this.avatar.id;
			View imageView = findViewById(R.id.imageView);
			View videoLayout = findViewById(R.id.videoLayout);
			if (imageView.getVisibility() == View.GONE) {
				imageView.setVisibility(View.VISIBLE);
			}
			if (videoLayout.getVisibility() == View.VISIBLE) {
				videoLayout.setVisibility(View.GONE);
			}
			
			HttpGetImageAction.fetchImage(this, this.avatar.avatar, imageView);
	        
        	AvatarConfig avatarConfig = (AvatarConfig)this.avatar.credentials();
        	HttpFetchChatAvatarAction action = new HttpFetchChatAvatarAction(this, avatarConfig);
            action.execute();
		}
		MainActivity.browsing = false;
		if ((MainActivity.instance instanceof InstanceConfig) && MainActivity.instance.id.equals(this.instance.id)) {
			this.instance = (InstanceConfig)MainActivity.instance;
		} else {
			MainActivity.instance = this.instance;
		}
		if (this.changingVoice) {
			this.changingVoice = false;
			resetTTS();
		}
		super.onResume();
	}
	
	public void resetTTS() {
		try {
			this.tts.stop();
		} catch (Exception exception) {}
	}
	
	public String getAvatarIcon() {
		if (this.avatar == null) {
			return this.instance.avatar;
		}
		return this.avatar.avatar;
	}
	
	public void resetAvatar(AvatarConfig config) {
		this.avatar = config;
		HttpGetImageAction.fetchImage(this, config.avatar, findViewById(R.id.imageView));
		HttpGetImageAction.fetchImage(this, config.avatar, findViewById(R.id.responseImageView));
	}
 
	@Override
	public void onDestroy() {
		ChatConfig config = new ChatConfig();
		config.instance = this.instance.id;
		config.conversation = MainActivity.conversation;
		config.disconnect = true;
		
		HttpChatAction action = new HttpChatAction(ChatActivity.this, config);
		action.execute();
		
		if (this.tts != null) {
			try {
				this.tts.stop();
			} catch (Exception ignore) {}
			try {
//				this.tts.shutdown();
			} catch (Exception ignore) {}
		}
		if (this.audioPlayer != null) {
			try {
				this.audioPlayer.stop();
			} catch (Exception ignore) {}
			try {
				this.audioPlayer.release();
			} catch (Exception ignore) {}
		}
		super.onDestroy();
	}
 
	@Override
	public void onInit(int status) {
 
		if (status == 1) {
 
			Locale locale = null;
			VoiceConfig voice = MainActivity.voice;
			if (voice.language != null && voice.language.length() > 0) {
				locale = new Locale(voice.language);
			} else {
				locale = Locale.US;
			}
//			int result = this.tts.setLanguage(locale);
			
			float pitch = 1;
			if (voice.pitch != null && voice.pitch.length() > 0) {
				try {
					pitch = Float.valueOf(voice.pitch);
				} catch (Exception exception) {}
			}
			float speechRate = 1;
			if (voice.speechRate != null && voice.speechRate.length() > 0) {
				try {
					speechRate = Float.valueOf(voice.speechRate);
				} catch (Exception exception) {}
			}
//			this.tts.setPitch(pitch);
//			this.tts.setSpeechRate(speechRate);
 
//			if (result == TextToSpeech.LANG_MISSING_DATA
//					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
//				Log.e("TTS", "This Language is not supported");
//			}
//			
//			this.tts.setOnUtteranceCompletedListener(this);
			
		} else {
			Log.e("TTS", "Initilization Failed!");
		}
		
	}
	
	/**
	 * Add JavaScript to the HTML to raise postback events to send messages to the bot.
	 */
	public String linkPostbacks(String html) {
		if (html.contains("button")) {
			TextStream stream = new TextStream(html);
			StringWriter writer = new StringWriter();
			while (!stream.atEnd()) {
				writer.write(stream.upToAll("<button", true));
				if (!stream.atEnd()) {
					String element = stream.upTo('>', true);
					String button = stream.upTo('<', false);
					writer.write(" onclick=\"Android.postback('" + button + "')\" ");
					writer.write(element);
					writer.write(button);
				}
			}
			html = writer.toString();
		}
		if (html.contains("chat:")) {
			TextStream stream = new TextStream(html);
			StringWriter writer = new StringWriter();
			while (!stream.atEnd()) {
				writer.write(stream.upToAll("href=\"", true));
				if (stream.atEnd()) {
					break;
				}
				String protocol = stream.upTo(':', true);
				if (!protocol.equals("chat:")) {
					writer.write(protocol);
					continue;
				}
				String chat = stream.upTo('"', false);
				writer.write("#\"");
				writer.write(" onclick=\"Android.postback('" + chat + "')\" ");
			}
			html = writer.toString();
		}
		if (html.contains("select")) {
			TextStream stream = new TextStream(html);
			StringWriter writer = new StringWriter();
			while (!stream.atEnd()) {
				writer.write(stream.upToAll("<select", true));
				if (!stream.atEnd()) {
					writer.write(" onchange=\"Android.postback(this.value)\" ");
				}
			}
			html = writer.toString();
		}
		return html;
	}

	public void response(final ChatResponse response) {
		
		try {
			this.response = response;
			
			String status = "";
			if (response.emote != null && !response.emote.equals("NONE")) {
				status = status + response.emote.toLowerCase();
			}
			if (response.action != null) {
				if (!status.isEmpty()) {
					status = status + " ";
				}
				status = status + response.action;
			}
			if (response.pose != null) {
				if (!status.isEmpty()) {
					status = status + " ";
				}
				status = status + response.pose;
			}
			if (response.command != null) {
				JSONObject jsonObject = response.getCommand();
				Command command = new Command(this, jsonObject);

			}


			TextView statusView = (TextView) findViewById(R.id.statusText);
			statusView.setText(status);
			
			final String text = response.message;
			final ListView list = (ListView) findViewById(R.id.chatList);
			
			list.addItem(MainActivity.launchInstanceName+": "+text);
			list.transcriptMode();
			if (text == null) {
				list.post(new Runnable() {
					@Override
					public void run() {
//						ChatResponse ready = new ChatResponse();
//						ready.message = "";
//						messages.add(ready);
//						((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
//						list.invalidateViews();
						if (list.getCount() > 2) {
							list.setSelection(list.getCount() - 2);
						}
//						beginListening();
					}
				});
				return;
			}
			list.post(new Runnable() {
				@Override
				public void run() {
					messages.add(response);
					if (!MainActivity.sound) {
						cycleVideo(response);
						return;
					}
					if(response.avatarTalk==null&& response.avatar == null){//while there isn't any avatar loaded.
						tts.speak(response.message, 2.0f, false, false);//play the voice only.
						return;
					}
					//thread for syncing video and voice together.
					videoThread = new Thread(new Runnable() {
						@Override
						public void run() {
							while (active) {
								if (tts.getTalking()) {
									System.out.print("");//to let the thread keeps looping until talking = false;
									playVideo(response.avatarTalk, false);

								} else {
									cycleVideo(response);
									active = false;
								}
							}
						}
					});
					isPlaying = false;//for the talking video. so it can play only once.
					tts.setTalking(true);//make sure that it's talking before starting the thread.
					active = true;//make sure the thread loop is active before the thread starts.
					videoThread.start();//starting the thread.
					tts.speak(response.message, 2.0f, false, false);//play the voice
				
					if (list.getCount() > 2) {
						list.setSelection(list.getCount() - 2);
					}
				}
			});
			
			WebView responseView = (WebView) findViewById(R.id.responseText);
			String html = Utils.linkHTML(text);
			if (html.contains("<") && html.contains(">")) {
				html = linkPostbacks(html);
			}
			responseView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);

			boolean talk = (text.trim().length() > 0) && (MainActivity.deviceVoice || (this.response.speech != null && this.response.speech.length() > 0));
			if (MainActivity.sound && talk) {
				if (!MainActivity.disableVideo && !videoError && this.response.isVideo() && this.response.isVideoTalk()) {
					
					videoView.setOnPreparedListener(new OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mp) {
							try {
								mp.setLooping(true);
								if (!MainActivity.deviceVoice) {
									// Voice audio
									if (MainActivity.online) {
									speechPlayer = playAudio(response.speech, false, false, false);
									speechPlayer.setOnCompletionListener(new OnCompletionListener() {
										@Override
										public void onCompletion(MediaPlayer mp) {
											mp.release();
											videoView.post(new Runnable() {
												public void run() {
													cycleVideo(response);
												}
											});
										
										}
													
									});
									}
									speechPlayer.start();
								} else {
									HashMap<String, String> params = new HashMap<String, String>();
//									params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");
									tts.speak(Utils.stripTags(text), 2.0f,false,false);
//									tts.speak(Utils.stripTags(text), TextToSpeech.QUEUE_FLUSH, params);
								}
							} catch (Exception exception) {
								Log.wtf(exception.getMessage(), exception);
							}
						}
					});
					playVideo(this.response.avatarTalk, false);
				} else if (talk) {
					if (!MainActivity.deviceVoice) {
						// Voice audio
						if (MainActivity.online) {
						playAudio(this.response.speech, false, false, true);
						}
					} else {
						HashMap<String, String> params = new HashMap<String, String>();
//						params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");
						this.tts.speak(Utils.stripTags(text), 2.0f,false,false);
					}
				}
			} else if (talk && (!MainActivity.disableVideo && !videoError && this.response.isVideo() && this.response.avatarTalk != null)) {
				videoView.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						mp.setLooping(false);
					}
				});
				videoView.setOnCompletionListener(new OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						videoView.setOnCompletionListener(null);
						cycleVideo(response);
					}
				});
				playVideo(this.response.avatarTalk, false);
			}
		} catch (Exception exception) {
			Log.wtf(exception.getMessage(), exception);
		}
	}
	
	public void playVideo(String video, boolean loop) {
			if(!isPlaying){
				if (loop) {
					videoView.setOnPreparedListener(new OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mp) {
							mp.setLooping(true);
						}
					});
				}
				try {
					Uri videoUri = HttpGetVideoAction.fetchVideo(this, video);
					if (videoUri == null) {
						if(MainActivity.online){videoUri = Uri.parse(MainActivity.connection.fetchImage(video).toURI().toString());}
					}
					videoView.setVideoURI(videoUri);
					videoView.start();
				} catch (Exception exception) {
					Log.wtf(exception.toString(), exception);
				}
				isPlaying = true;//to Play this video only once.
			}
	}
	
	public void cycleVideo(final ChatResponse response) {
		isPlaying = false;
		if ((response.avatar2 == null || response.avatar3 == null || response.avatar4 == null || response.avatar5 == null)
				|| (response.avatar2.isEmpty() || response.avatar3.isEmpty() || response.avatar4.isEmpty() || response.avatar5.isEmpty())
				|| (response.avatar.equals(response.avatar2) && response.avatar2.equals(response.avatar3)
						&& response.avatar3.equals(response.avatar4) && response.avatar4.equals(response.avatar5))) {
			playVideo(response.avatar, true);
			return;
		}
		videoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.setLooping(false);
			}
		});
		videoView.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				cycleVideo(response);
			}
		});
		int value = random.nextInt(5);
		String avatar = response.avatar;
		switch (value) {
			case 1 :
				avatar = response.avatar2;
				break;
			case 2 :
				avatar = response.avatar3;
				break;
			case 3 :
				avatar = response.avatar5;
				break;
			case 14 :
				avatar = response.avatar4;
				break;
		}
		
		try {
			Uri videoUri = HttpGetVideoAction.fetchVideo(this, avatar);
			if (videoUri == null) {
				if(MainActivity.online){
				videoUri = Uri.parse(MainActivity.connection.fetchImage(avatar).toURI().toString());
				}
			}
			videoView.setVideoURI(videoUri);
			videoView.start();
		} catch (Exception exception) {
			Log.wtf(exception.toString(), exception);
		}
	}
	
	public MediaPlayer playAudio(String audio, boolean loop, boolean cache, boolean start) {
		
		try {
			Uri audioUri = null;
			if (cache) {
				audioUri = HttpGetVideoAction.fetchVideo(this, audio);
			}
			if (audioUri == null) {
				audioUri = Uri.parse(MainActivity.connection.fetchImage(audio).toURI().toString());
			}
			final MediaPlayer audioPlayer = new MediaPlayer();
			audioPlayer.setDataSource(getApplicationContext(), audioUri);
			audioPlayer.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.wtf("Audio error", "what:" + what + " extra:" + extra);
					audioPlayer.stop();
					audioPlayer.release();
					return true;
				}
			});
			audioPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					audioPlayer.release();
				}
			});
			audioPlayer.prepare();
			audioPlayer.setLooping(loop);
			if (start) {
				audioPlayer.start();
			}
			return audioPlayer;
		} catch (Exception exception) {
			Log.wtf(exception.toString(), exception);
			return null;
		}
	}
	

	public void onUtteranceCompleted(String utteranceId) {
		try {
			if (!MainActivity.disableVideo && !videoError && this.response.isVideo()) {
				this.videoView.post(new Runnable() {
					public void run() {
						cycleVideo(response);
					}
				});
			}
		} catch (Exception exception) {
			Log.wtf(exception.toString(), exception);
		}
	}

	public TextToSpeech getTts() {
		return tts;
	}

	public void setTts(TextToSpeech tts) {
		this.tts = tts;
	}

	public VideoView getVideoView() {
		return videoView;
	}

	public void setVideoView(VideoView videoView) {
		this.videoView = videoView;
	}

	public List<Object> getMessages() {
		return messages;
	}

	public void setMessages(List<Object> messages) {
		this.messages = messages;
	}

	public ChatResponse getResponse() {
		return response;
	}

	public void setResponse(ChatResponse response) {
		this.response = response;
	}

	public MediaPlayer getAudioPlayer() {
		return audioPlayer;
	}

	public void setAudioPlayer(MediaPlayer audioPlayer) {
		this.audioPlayer = audioPlayer;
	}

	public String getCurrentAudio() {
		return currentAudio;
	}

	public void setCurrentAudio(String currentAudio) {
		this.currentAudio = currentAudio;
	}

	public boolean getWasSpeaking() {
		return wasSpeaking;
	}

	public void setWasSpeaking(boolean wasSpeaking) {
		this.wasSpeaking = wasSpeaking;
	}
	
}

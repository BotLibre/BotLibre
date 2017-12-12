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

import org.botlibre.sdk.activity.MainActivity.LaunchType;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpChatAction;
import org.botlibre.sdk.activity.actions.HttpFetchChatAvatarAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetVideoAction;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ChatConfig;
import org.botlibre.sdk.config.ChatResponse;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.VoiceConfig;
import org.botlibre.sdk.util.Command;
import org.botlibre.sdk.util.TextStream;
import org.botlibre.sdk.util.Utils;
import org.json.JSONObject;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import org.botlibre.santabot.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * Activity for chatting with a bot.
 * To launch this activity from your app you can use the HttpFetchAction passing the bot id or name as a config, and launch=true.
 */
@SuppressWarnings("deprecation")
public class ChatActivity extends LibreActivity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener, RecognitionListener {
	protected static final int RESULT_SPEECH = 1;
	protected static final int CAPTURE_IMAGE = 2;
	protected static final int RESULT_SCAN = 3;
	protected static final int CAPTURE_VIDEO = 4;
	protected static boolean DEBUG;

	private boolean isRecording;
	public static boolean isListening;
	
	public class WebAppInterface {
	    Context context;

	    WebAppInterface(Context context) {
	    	this.context = context;
	    }
	    
	    @JavascriptInterface
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
	
	private SpeechRecognizer speech;
	private LinearLayout menuMLayout;
	private LinearLayout chatCLayout;
	private LinearLayout responseLayout;
	private LinearLayout chatToolBar;
	
	private int stateLayouts = 0;
	private View scrollView;
	public boolean music = false;
	
	private double lastReply =  System.currentTimeMillis();
	
	public List<Object> messages = new ArrayList<Object>();
	public ChatResponse response;
	public MediaPlayer audioPlayer;
	public String currentAudio;

	public boolean videoError;
	protected volatile boolean wasSpeaking;
	protected InstanceConfig instance;
	
	private boolean active = true;
	
	protected AvatarConfig avatar;
	protected String avatarId;
	protected boolean changingVoice;
	MediaPlayer speechPlayer;
	protected Random random = new Random();
	
	protected Menu menu;	
	
	protected Bitmap icon;
	
	//flag will check if the mic is ON or OFF
	public static boolean micLastStat;
	
	private boolean failedOfflineLanguage = false;
	private boolean threadIsOn = false;
	
	public void superOnCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		// Remove flag button if a single bot app.
		if (MainActivity.launchType == LaunchType.Bot) {
			//findViewById(R.id.flagButton).setVisibility(View.GONE);
		}
		
		//permission required.
		ActivityCompat.requestPermissions(ChatActivity.this, new String[] { Manifest.permission.RECORD_AUDIO }, 1);
		
		//set/Save the current volume from the device.
		setStreamVolume();
		//Music Volume is Enabled.
		muteMicBeep(false);
		
		//For "scream" issue
		micLastStat = MainActivity.listenInBackground;
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		this.instance = (InstanceConfig)MainActivity.instance;
		if (this.instance == null) {
			return;
		}
		/*if (MainActivity.showAds) {
	        AdView mAdView = (AdView) findViewById(R.id.adView);
	        AdRequest adRequest = new AdRequest.Builder().build();
	        mAdView.loadAd(adRequest);
		} else {
			AdView mAdView = (AdView) findViewById(R.id.adView);
			mAdView.setVisibility(View.GONE);
		}*/

		setTitle(this.instance.name);
		((TextView) findViewById(R.id.title)).setText(this.instance.name);
		HttpGetImageAction.fetchImage(this, this.instance.avatar, findViewById(R.id.icon));
		ttsInit = false;
		tts = new TextToSpeech(this, this);
		
		if (!MainActivity.handsFreeSpeech){
			setMicIcon(false, false);
		} else if (!MainActivity.listenInBackground){
			setMicIcon(false, false);
		}
		
		//Last time will be saved for the MIC.
		if (MainActivity.listenInBackground && MainActivity.handsFreeSpeech){
			microphoneThread(thread);
		}
		
		speech = SpeechRecognizer.createSpeechRecognizer(this);
		speech.setRecognitionListener(this);
		//scrollVie added and stuff
		scrollView = findViewById(R.id.chatList);
		menuMLayout = (LinearLayout) findViewById(R.id.menuMLayout);
		chatCLayout = (LinearLayout) findViewById(R.id.chatCLayout);
		responseLayout = (LinearLayout) findViewById(R.id.responseLayout);
		chatToolBar = (LinearLayout) findViewById(R.id.chatToolBar);
		
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
			findViewById(R.id.yandex).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.yandex).setVisibility(View.GONE);
		}

		Spinner emoteSpin = (Spinner) findViewById(R.id.emoteSpin);
		emoteSpin.setAdapter(new EmoteSpinAdapter(this, R.layout.emote_list, Arrays.asList(EmotionalState.values())));

		ListView list = (ListView) findViewById(R.id.chatList);
		list.setAdapter(new ChatListAdapter(this, R.layout.chat_list, this.messages));
		list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

		ImageButton	button = (ImageButton) findViewById(R.id.speakButton);
		button.setOnClickListener(new View.OnClickListener() { 
			@TargetApi(23)
			@Override
			public void onClick(View v) { 
				if (MainActivity.handsFreeSpeech) {
					//set the current volume to the setting.
					setStreamVolume();
					//if its ON Or OFF - Switching back and forth 
					MainActivity.listenInBackground = !MainActivity.listenInBackground;
					
					//saving the boolean data of MainActivity.listeningInBackground
			    	SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
			    	cookies.putBoolean("listenInBackground", MainActivity.listenInBackground);
			    	cookies.commit();
					if (MainActivity.listenInBackground) {
						micLastStat = true;
						try {microphoneThread(thread);} catch (Exception ignore){}
						beginListening();
					} else {
						micLastStat = false;
						microphoneThread(thread);
						stopListening();
					}
				} else {
					Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); 
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, MainActivity.voice.language);
					try {
						startActivityForResult(intent, RESULT_SPEECH);
						textView.setText("");
					} catch (ActivityNotFoundException a) {
						Toast t = Toast.makeText(getApplicationContext(),
								"Your device doesn't support Speech to Text",
								Toast.LENGTH_SHORT);
						t.show();
					}
				}
			}
		});
		
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
					chatCLayout.setVisibility(View.VISIBLE);
					menuMLayout.setVisibility(View.VISIBLE);
					responseLayout.setVisibility(View.VISIBLE);
					chatToolBar.setVisibility(View.VISIBLE);
					break;
				case 1:
					scrollView.setVisibility(View.GONE);
					break;
				case 2:
					responseLayout.setVisibility(View.GONE);
					chatToolBar.setVisibility(View.GONE);
					break;
				case 3:
					menuMLayout.setVisibility(View.GONE);
					chatCLayout.setVisibility(View.GONE);
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
					chatCLayout.setVisibility(View.VISIBLE);
					menuMLayout.setVisibility(View.VISIBLE);
					responseLayout.setVisibility(View.VISIBLE);
					chatToolBar.setVisibility(View.VISIBLE);
					break;
				case 1:
					scrollView.setVisibility(View.GONE);
					break;
				case 2:
					responseLayout.setVisibility(View.GONE);
					chatToolBar.setVisibility(View.GONE);
					break;
				case 3:
					menuMLayout.setVisibility(View.GONE);
					chatCLayout.setVisibility(View.GONE);
					break;
				}
				stateLayouts++;
			}
		});
		
		GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTapEvent(MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					boolean isVideo = !MainActivity.disableVideo && !videoError && response != null
							&& response.isVideo();
					View imageView = findViewById(R.id.imageView);
					View videoLayout = findViewById(R.id.videoLayout);
					if (imageView.getVisibility() == View.VISIBLE) {
						imageView.setVisibility(View.GONE);
					} else if (!isVideo) {
						imageView.setVisibility(View.VISIBLE);
					}
					if (videoLayout.getVisibility() == View.VISIBLE) {
						videoLayout.setVisibility(View.GONE);
					} else if (isVideo) {
						videoLayout.setVisibility(View.VISIBLE);
					}
					return true;
				}
				return false;
			}
		};
		final GestureDetector detector = new GestureDetector(this, listener);		
		findViewById(R.id.chatList).setOnTouchListener(new View.OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return detector.onTouchEvent(event);
			}
		});

		listener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTapEvent(MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					View avatarLayout = findViewById(R.id.avatarLayout);
					if (avatarLayout.getVisibility() == View.VISIBLE) {
						avatarLayout.setVisibility(View.GONE);
					} else {
						avatarLayout.setVisibility(View.VISIBLE);
					}
					return true;
				}
				return false;
			}
		};
		final GestureDetector detector2 = new GestureDetector(this, listener);
		/*findViewById(R.id.responseText).setOnTouchListener(new View.OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return detector2.onTouchEvent(event);
			}
		});*/
		WebView responseView = (WebView) findViewById(R.id.responseText);
		responseView.getSettings().setJavaScriptEnabled(true);
		responseView.getSettings().setDomStorageEnabled(true);
		responseView.addJavascriptInterface(new WebAppInterface(this), "Android");

		findViewById(R.id.responseImageView).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				View avatarLayout = findViewById(R.id.avatarLayout);
				if (avatarLayout.getVisibility() == View.VISIBLE) {
					avatarLayout.setVisibility(View.GONE);
				} else {
					avatarLayout.setVisibility(View.VISIBLE);
				}
			}
		});

		HttpGetImageAction.fetchImage(this, instance.avatar, this.imageView);
		HttpGetImageAction.fetchImage(this, instance.avatar, (ImageView)findViewById(R.id.responseImageView));

		final ChatConfig config = new ChatConfig();
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
		// This is required because of a bug in TextToSpeech that prevents onInit being called if an AsynchTask is called...
				Thread thread1 = new Thread() {
					public void run() {
						for (int count = 0; count < 5; count++) {
							if (ttsInit) {
								break;
							}
							try {
								Thread.sleep(1000);
							} catch (Exception exception) {}
						}
						HttpAction action = new HttpChatAction(ChatActivity.this, config);
						action.execute();
					}
				};
				thread1.start();
	}
	
	//thread for the Microphone
	public Thread microphoneThread(Thread thread) {
		//make sure its on if it didn't turn off by the user. if 'sleep' is called it will turn the mic off.
		if(MainActivity.listenInBackground && threadIsOn){
			return thread;
		}
		//if the user clicked on the Mic while its ON it will turn off and turn the thread off as well.
		if(threadIsOn){
			threadIsOn = false;
			active = false;
			try{
				thread.stop();
			}catch(Exception ignore){}
			return thread;
		}
		//if the user clicked on the Mic while its off it will turn ON the thread.
		if (!threadIsOn) {
			threadIsOn = true;
			active = true;
			thread = new Thread() {
				@Override
				public void run() {
					Log.e("Thread","RUNNING");
					while (active) {
						Log.e("Thread","ACTIVE");
						if (!isRecording && isListening && (System.currentTimeMillis() - lastReply) > 5000) {
							lastReply = System.currentTimeMillis();
							debug("speech death restart");
							restartListening();
						}
						try {
							Thread.sleep(1500);
						} catch (Exception exception) {
						}
					}
				}
			};
			thread.start();
		}
		
		return thread;
	}
	
	@SuppressLint("Override")
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
		case 1: {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			} else {
				Toast.makeText(ChatActivity.this, "Permission denied to Record Audio", Toast.LENGTH_SHORT).show();
			}
			return;
		}
		}
	}

	public void resetChat(View view) {
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

		EditText v = (EditText) findViewById(R.id.messageText);
		v.setText("");
		this.messages.clear();
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				ListView list = (ListView) findViewById(R.id.chatList);
				((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
				list.invalidateViews();
			}
			
		});
		
		WebView responseView = (WebView) findViewById(R.id.responseText);
		responseView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
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
			case CAPTURE_IMAGE: {
	
				if (resultCode == RESULT_OK) {
					//TODO Make camera intent stop app from reseting
					//Uri photoUri = data.getData();
					//Do what we like with the photo - send to bot, etc
	
				} else if (resultCode == RESULT_CANCELED) {
					textView.setText("Cancelled");
					submitChat();
				} 
	
				break;
			}
			case CAPTURE_VIDEO: {
				if (resultCode == RESULT_OK) {
					Uri videoUri = data.getData();
					//Do what we would like with the video
					
				} else if (resultCode == RESULT_CANCELED) {
					textView.setText("Cancelled");
					submitChat();
				} 
				break;
			}
			case RESULT_SPEECH: {
				if (resultCode == RESULT_OK && data != null) {
	
					ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	
	
					textView.setText(text.get(0));
					submitChat();
				}
				break;
			}
		}
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (scanResult != null) {
			textView.setText("lookup " + scanResult.getContents());
			submitChat();
			if (scanResult.getContents().startsWith("http")) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scanResult.getContents()));
				startActivity(intent);
			}
		}
	}

	public void flagResponse() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to flag a response", this);
        	return;
        }
        final EditText text = new EditText(this);
        MainActivity.prompt("Enter reason for flagging response as offensive", this, text, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            if (instance == null) {
	            	return;
	            }
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
	    		runOnUiThread(new Runnable(){
	    			@Override
	    			public void run() {
	    				ListView list = (ListView) findViewById(R.id.chatList);
	    				((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
	    				list.invalidateViews();
	    			}
	    			
	    		});
	    		
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
	            if (instance == null) {
	            	return;
	            }
	            
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
	    		runOnUiThread(new Runnable(){
	    			@Override
	    			public void run() {
	    				ListView list = (ListView) findViewById(R.id.chatList);
	    				((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
	    				list.invalidateViews();
	    			}
	    			
	    		});
	    		
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
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				ListView list = (ListView) findViewById(R.id.chatList);
				((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
				list.invalidateViews();
			}
			
		});
		

		Spinner emoteSpin = (Spinner) findViewById(R.id.emoteSpin);
		config.emote = emoteSpin.getSelectedItem().toString();

		HttpChatAction action = new HttpChatAction(ChatActivity.this, config);
		action.execute();

		v.setText("");
		emoteSpin.setSelection(0);
		resetToolbar();

		WebView responseView = (WebView) findViewById(R.id.responseText);
		responseView.loadDataWithBaseURL(null, "thinking...", "text/html", "utf-8", null);
	
		//Check the volume
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		if(volume <= 3 && volumeChecked){Toast.makeText(this, "Please check 'Media' volume", Toast.LENGTH_LONG).show(); volumeChecked=false;}
		
		
		
		//stop letting the mic on.
		stopListening();
		//its Important for "sleep" "scream" ...etc commands.
		//this will turn off the mic
		MainActivity.listenInBackground = false;
	}

	public void toggleSound(View view) {
		toggleSound();
	}

	public void toggleSound() {
		MainActivity.sound = !MainActivity.sound;
		resetToolbar();
	}
	public void toggleHandsFreeSpeech() {
		MainActivity.handsFreeSpeech = !MainActivity.handsFreeSpeech;
		if (!MainActivity.handsFreeSpeech) {
			stopListening();
		} else if (MainActivity.handsFreeSpeech) {
			beginListening();
		}
		SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
    	cookies.putBoolean("handsfreespeech", MainActivity.handsFreeSpeech);
    	cookies.commit();
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
		menuInflater.inflate(R.menu.menu_chat, menu);
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
		this.menu.findItem(R.id.menuHandsFreeSpeech).setChecked(MainActivity.handsFreeSpeech);
		this.menu.findItem(R.id.menuDisableVideo).setChecked(MainActivity.disableVideo || this.videoError);
		this.menu.findItem(R.id.menuHD).setChecked(MainActivity.hd);
		this.menu.findItem(R.id.menuWebm).setChecked(MainActivity.webm);
	}

	public void changeLanguage(View view) {
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
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		switch (item.getItemId())
		{
		case R.id.menuChangeLanguage:
			changeLanguage(null);
			return true;
//		case R.id.menuChangeVoice:
//			changeVoice();
//			return true;
		case R.id.menuHandsFreeSpeech:
			toggleHandsFreeSpeech();
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
		case R.id.MicConfig:
				MicConfiguration();
				finish();
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
			if (this.imageView.getVisibility() == View.GONE) {
				this.imageView.setVisibility(View.VISIBLE);
			}
			if (this.videoLayout.getVisibility() == View.VISIBLE) {
				this.videoLayout.setVisibility(View.GONE);
			}
			
			HttpGetImageAction.fetchImage(this, this.avatar.avatar, this.imageView);

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
		try {
			this.tts.shutdown();
		} catch (Exception exception) {}
		this.tts = new TextToSpeech(this, this);
	}
	
	public String getAvatarIcon(ChatResponse config) {
		if (this.avatar != null) {
			return this.avatar.avatar;
		}
		if (config == null || config.isVideo()) {
			return this.instance.avatar;
		}
		return config.avatar;
	}

	public void resetAvatar(AvatarConfig config) {
		HttpGetImageAction.fetchImage(this, config.avatar, this.imageView);
		HttpGetImageAction.fetchImage(this, config.avatar, findViewById(R.id.responseImageView));
	}
	
	
	public void MicConfiguration(){
		Intent i = new Intent(this, MicConfiguration.class);
		startActivity(i);
		finish();
	}

	@Override
	public void onDestroy() {
		try{
		active = false;
		stopListening();
		try{
		thread = null;
		}catch(Exception ex){
			Log.e("micError",ex.toString());
		}
		if (this.instance != null) {
			ChatConfig config = new ChatConfig();
			config.instance = this.instance.id;
			config.conversation = MainActivity.conversation;
			config.disconnect = true;
			
			HttpChatAction action = new HttpChatAction(this, config);
			action.execute();
		}
		muteMicBeep(false);
		if (this.tts != null) {
			try {
				this.tts.stop();
			} catch (Exception ignore) {}
			try {
				this.tts.shutdown();
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
		if (this.speechPlayer != null) {
			try {
				this.speechPlayer.stop();
			} catch (Exception ignore) {}
			try {
				this.speechPlayer.release();
			} catch (Exception ignore) {}
		}
	
		}catch(Exception ex){Log.e("micError",ex.toString());}
		super.onDestroy();
	}

	@Override
	public void onInit(int status) {

		if (status == TextToSpeech.SUCCESS) {
		
			Locale locale = null;
			VoiceConfig voice = MainActivity.voice;
			if (voice != null && voice.language != null && voice.language.length() > 0) {
				locale = new Locale(voice.language);
			} else {
				locale = Locale.US;
			}
			int result = this.tts.setLanguage(locale);

			float pitch = 1;
			if (voice != null && voice.pitch != null && voice.pitch.length() > 0) {
				try {
					pitch = Float.valueOf(voice.pitch);
				} catch (Exception exception) {}
			}
			float speechRate = 1;
			if (voice != null && voice.speechRate != null && voice.speechRate.length() > 0) {
				try {
					speechRate = Float.valueOf(voice.speechRate);
				} catch (Exception exception) {}
			}
			this.tts.setPitch(pitch);
			this.tts.setSpeechRate(speechRate);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
			}

			this.tts.setOnUtteranceCompletedListener(this);

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
		if(speechPlayer != null|| tts != null){
			try{
			tts.stop();
			speechPlayer.pause();
			}catch(Exception ignore){Log.e("RESPONSE","Error: " + ignore.getMessage());}
		}
		//needs when calling "sleep" or the its not going to let the mic off
		//also to stop the mic until the bot finish the sentence
		try {
			stopListening();
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
			if (text == null) {
				list.post(new Runnable() {
					@Override
					public void run() {
						ChatResponse ready = new ChatResponse();
						ready.message = "ready";
						messages.add(ready);
						((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
						list.invalidateViews();
						if (list.getCount() > 2) {
							list.setSelection(list.getCount() - 2);
						}
						beginListening();
					}
				});
				return;
			}
			list.post(new Runnable() {
				@Override
				public void run() {
					messages.add(response);
					((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
					list.invalidateViews();
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
											runOnUiThread(new Runnable() {
												public void run() {
													if(!music){
													beginListening();
													}
												}
											});
										}
									});
									
									speechPlayer.start();
								} else {
									HashMap<String, String> params = new HashMap<String, String>();
									params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");
									
									tts.speak(Utils.stripTags(text), TextToSpeech.QUEUE_FLUSH, params);
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
						playAudio(this.response.speech, false, false, true);
					} else {
						HashMap<String, String> params = new HashMap<String, String>();
						params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");
						
						this.tts.speak(Utils.stripTags(text), TextToSpeech.QUEUE_FLUSH, params);
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
				runOnUiThread(new Runnable() {
					public void run() {
						beginListening();
					}
				});
			} else {
				runOnUiThread(new Runnable() {
					public void run() {
						beginListening();
					}
				});
			}
		} catch (Exception exception) {
			Log.wtf(exception.getMessage(), exception);
		}
		if(micLastStat){
			MainActivity.listenInBackground = true;
		}
	}

	@Override
	 public void onPause() {
		stopListening();
		muteMicBeep(false);
		super.onPause();
	 }
	
	public void playVideo(String video, boolean loop) {
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
				videoUri = Uri.parse(MainActivity.connection.fetchImage(video).toURI().toString());
			}
			videoView.setVideoURI(videoUri);
			videoView.start();
		} catch (Exception exception) {
			Log.wtf(exception.toString(), exception);
		}
	}
	
	public void cycleVideo(final ChatResponse response) {
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
				videoUri = Uri.parse(MainActivity.connection.fetchImage(avatar).toURI().toString());
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
					runOnUiThread(new Runnable() {
						public void run() {
							try {
								beginListening();
							} catch (Exception e) {
								Log.e("ChatActivity", "MediaPlayer: " + e.getMessage());
							}
						}
					});
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
	

	@Override
	public void onUtteranceCompleted(String utteranceId) {
		debug("onUtteranceCompleted");
		runOnUiThread(new Runnable() {
			public void run() {
				try {
				beginListening();
				} catch(Exception ignore) {}
			}
		});
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
	
	private void stopListening() {
		debug("stopListening");
		try {
			muteMicBeep(false);
			isListening = false;
			this.speech.stopListening();
			setMicIcon(false, false);
		} catch (Exception ignore) {
			Log.e("StopListening", "Error" + ignore.getMessage());
		}
	}
	
	private void restartListening() {
		lastReply = System.currentTimeMillis();
		debug("restartListening");
		if (!MainActivity.listenInBackground) {
			return;
		}
		if (!isListening) {
			return;
		}
		this.runOnUiThread(new Runnable() {
			public void run() {
				try{
					Log.e("ChatActivity","Start Listening from Restart");
				beginListening();
				}catch(Exception e){Log.e("ErrorChatActivity","Error: " + e.getMessage()); }
			}
		});
	}
	
	public void scanBarcode(View v) {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();
	}

	private void setMicIcon(boolean on, boolean recording) {
		try{
		View micButton = findViewById(R.id.speakButton);
		
		if (!on) {
			((ImageButton) micButton).setImageResource(R.drawable.micoff);
		} else if (on && recording) {
			((ImageButton) micButton).setImageResource(R.drawable.micrecording);
		} else {
			((ImageButton) micButton).setImageResource(R.drawable.mic);
		}
		}catch(Exception e){Log.e("ChatActivity.setMicIcon","" + e.getMessage());}
	}
	
	
	
	@Override
	public void onBeginningOfSpeech() {
		debug("onBeginningOfSpeech");
		Log.e("onBeginningOfSpeech","");
		setMicIcon(true, true);
		isRecording = true;
	}

	@Override
	public void onBufferReceived(byte[] arg0) {
		Log.e("onBufferReceived","");
		}

	@Override
	public void onEndOfSpeech() {
		debug("onEndOfSpeech:");
		isRecording = false;
		lastReply = System.currentTimeMillis();
		Log.e("onEndOfSpeech","");
		setMicIcon(false, false);
	}
	public void debug(final String text) {
		if (!DEBUG) {
			return;
		}
		final ListView list = (ListView) findViewById(R.id.chatList);
		list.post(new Runnable() {
			@Override
			public void run() {
				ChatResponse ready = new ChatResponse();
				ready.message = text;
				messages.add(ready);
				((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
				list.invalidateViews();
				if (list.getCount() > 2) {
					list.setSelection(list.getCount() - 2);
				}
			}
		});
		return;
	}

	@Override
	public void onError(int error) {
		debug("onError:" + error);
		Log.d("onError Info", "ChatActivity on error executes here!");
		try{
		isRecording = false;

		lastReply = System.currentTimeMillis();
		this.speech.destroy();
		this.speech = SpeechRecognizer.createSpeechRecognizer(this);
		this.speech.setRecognitionListener(this);
		
		setMicIcon(false, false);

		muteMicBeep(false);

		setStreamVolume();
		
		if (error == SpeechRecognizer.ERROR_AUDIO) {
			Log.d("System.out", "Error: Audio Recording Error");
		} else if (error == SpeechRecognizer.ERROR_CLIENT) {
			Log.d("System.out", "Error: Other client side error");
			restartListening();
		} else if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
			Log.d("System.out", "Error: INsufficient permissions");
		} else if (error == SpeechRecognizer.ERROR_NETWORK) {
			Log.d("System.out", "Error: Other network Error");
		} else if (error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT) {
			Log.d("System.out", "Error: Network operation timed out");
		} else if (error == SpeechRecognizer.ERROR_NO_MATCH) {
			Log.d("System.out", "Error: No recognition result matched");
			restartListening();
		} else if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
			Log.d("System.out", "Error: Recognition service busy");
			restartListening();
		} else if (error == SpeechRecognizer.ERROR_SERVER) {
			Log.d("System.out", "Error: Server Error");
			failedOfflineLanguage = true;
			restartListening();
		} else if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
			Log.d("System.out", "Error: NO speech input");
			isListening = true;
			restartListening();
		}
		}catch(Exception e){Log.e("micError",e.getMessage());}
	}

	@Override
	public void onEvent(int arg0, Bundle arg1) {
		Log.e("OnEvent","Listening OnEvent");
		debug("onEvent:" + arg0);
	}

	@Override
	public void onPartialResults(Bundle arg0) {
		debug("onPartialResults:");
	}

	@Override
	public void onReadyForSpeech(Bundle arg0) {
		debug("onReadyForSpeech:");
		Log.e("onReadyForSpeech","");
		setMicIcon(true, false);
	}

	@Override
	public void onResults(Bundle results) {
		debug("onResults:");
		muteMicBeep(false);
		List<String> text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		this.textView.setText(text.get(0));
		submitChat();
	}

	@Override
	public void onRmsChanged(float arg0) {}
	
	private void muteMicBeep(boolean mute) {
		debug("muteMicBeep:" + mute + ":" + MainActivity.volume);
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);	
		if (mute) {
			//if its true then the Volume will be zero.
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
		} else {
			//if its false, the Volume will put back on
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, MainActivity.volume, 0);
		}
	}

	private void setStreamVolume() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		if (volume != 0) {
			debug("setStreamVolume:" + volume);
			Log.d("ChatActivity","The volume changed and saved to : " + volume);
			MainActivity.volume = volume;
		} 
	}
	@TargetApi(23)
	private void beginListening() {
		lastReply = System.currentTimeMillis();
		setStreamVolume();
		debug("beginListening:");
		
		
		try {
			if(!MainActivity.handsFreeSpeech){return;}
			if (MainActivity.handsFreeSpeech) {
				muteMicBeep(true);
				isListening = true;
			}

			if (!MainActivity.listenInBackground) {
				muteMicBeep(false);
				return;
			}

		} catch (Exception ignore) {
			Log.e("Error", "BeginListening");
		}
		
		
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		if (MainActivity.offlineSpeech) {
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, MainActivity.voice.language);

			if (!this.failedOfflineLanguage) {
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
				// intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
			}
			intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
		} else {
			if (MainActivity.voice.language != null && !MainActivity.voice.language.isEmpty()) {
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, MainActivity.voice.language);
				if (!this.failedOfflineLanguage) {
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, MainActivity.voice.language);
				}
			} else {
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en");
				if (!this.failedOfflineLanguage) {
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
				}
			}
		}
		
		
		
		try {
			Log.d("BeginListening","StartListening");
			this.speech.startListening(intent);
			setMicIcon(true, false);
		} catch (ActivityNotFoundException a) {
			Log.d("BeginListening","CatchError: " + a.getMessage());
			Toast t = Toast.makeText(getApplicationContext(),
					"Your device doesn't support Speech to Text",
					Toast.LENGTH_SHORT);
			t.show();
		}		
	}
	/*private static Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(saveImageVideo(type));
	}

	private static File saveImageVideo(int type) {
		File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "BotLibre");
		//Create a storage directory if it does not exist
		if (! directory.exists()) {
			if (! directory.mkdirs()) {
				Log.d("BotLibre", "failed to create directory");
				return null;
			}
		}
		//Create a media file name
		File savedFile;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		if (type == MEDIA_TYPE_IMAGE) {
			savedFile = new File(directory.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
		}
		//else if video add code here
		else {
			return null;
		}

		return savedFile;


	}*/
}

package org.botlibre.sdk.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.VideoView;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpChatAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetVideoAction;
import org.botlibre.sdk.config.ChatConfig;
import org.botlibre.sdk.config.ChatResponse;
import org.botlibre.sdk.config.VoiceConfig;
import org.botlibre.sdk.util.Utils;

/**
 * Activity for chatting with a bot.
 * To launch this activity from your app you can use the HttpFetchAction passing the bot id or name as a config, and launch=true.
 */
public class ChatActivity extends Activity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
	protected static final int RESULT_SPEECH = 1;
	
	protected TextToSpeech tts;
	protected VideoView videoView;
	protected EditText textView;
	
	public List<Object> messages = new ArrayList<Object>();
	public ChatResponse response;
	public MediaPlayer audioPlayer;
	public String currentAudio;

	public boolean videoError;
	protected boolean correction;
	protected boolean flag;
	protected volatile boolean wasSpeaking;
	
	Menu menu;	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		if (MainActivity.instance.showAds) {
	        //AdView mAdView = (AdView) findViewById(R.id.adView);
	        //AdRequest adRequest = new AdRequest.Builder().build();
	        //mAdView.loadAd(adRequest);
		} else {
	        //AdView mAdView = (AdView) findViewById(R.id.adView);
    		//mAdView.setVisibility(View.GONE);
		}

		setTitle(MainActivity.instance.name);
		
		tts = new TextToSpeech(this, this);

		videoView = (VideoView)findViewById(R.id.videoView);
		resetVideoErrorListener();
		videoError = false;
		
		textView = (EditText) findViewById(R.id.messageText);
		textView.setOnEditorActionListener(new OnEditorActionListener() {			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				submitChat();
				return false;
			}
		});

		Spinner emoteSpin = (Spinner) findViewById(R.id.emoteSpin);
		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, EmotionalState.values());
		emoteSpin.setAdapter(adapter);
		
		ListView list = (ListView) findViewById(R.id.chatList);
		list.setAdapter(new ChatListAdapter(this, R.layout.chat_list, this.messages));
		list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

		ImageButton	button = (ImageButton) findViewById(R.id.speakButton);
		button.setOnClickListener(new View.OnClickListener() { 
			@Override
			public void onClick(View v) { 
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); 
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
 
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
		});
		
		findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				View scrollView = findViewById(R.id.chatList);
				if (scrollView.getVisibility() == View.VISIBLE) {
					scrollView.setVisibility(View.GONE);
				} else {
					scrollView.setVisibility(View.VISIBLE);					
				}
			}
		});
		
		findViewById(R.id.videoLayout).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				View scrollView = findViewById(R.id.chatList);
				if (scrollView.getVisibility() == View.VISIBLE) {
					scrollView.setVisibility(View.GONE);
				} else {
					scrollView.setVisibility(View.VISIBLE);					
				}
			}
		});
		
		GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTapEvent(MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					boolean isVideo = !MainActivity.disableVideo && !videoError && response != null && response.isVideo();
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
		findViewById(R.id.responseText).setOnTouchListener(new View.OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return detector2.onTouchEvent(event);
			}
		});

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

		HttpGetImageAction.fetchImage(this, MainActivity.instance.avatar, (ImageView)findViewById(R.id.imageView));
		HttpGetImageAction.fetchImage(this, MainActivity.instance.avatar, (ImageView)findViewById(R.id.responseImageView));
		
		ChatConfig config = new ChatConfig();
		config.instance = MainActivity.instance.id;
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
	}

	public void submitChat() {
		ChatConfig config = new ChatConfig();
		config.instance = MainActivity.instance.id;
		config.conversation = MainActivity.conversation;
		config.speak = !MainActivity.deviceVoice;
		
		EditText v = (EditText) findViewById(R.id.messageText);
		config.message = v.getText().toString().trim();
		if (config.message.equals("")) {
			return;
		}
		this.messages.add(config);
		ListView list = (ListView) findViewById(R.id.chatList);
		list.invalidateViews();
		
		config.correction = this.correction;
		config.offensive = this.flag;

		Spinner emoteSpin = (Spinner) findViewById(R.id.emoteSpin);
		config.emote = emoteSpin.getSelectedItem().toString();
		
		HttpChatAction action = new HttpChatAction(ChatActivity.this, config);
		action.execute();

		v.setText("");
		this.correction = false;
		this.flag = false;
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
	
	public void toggleDeviceVoice() {
		MainActivity.deviceVoice = !MainActivity.deviceVoice;
	}

	public void toggleFlag(View view) {
		toggleFlag();
	}
	
	public void toggleFlag() {
		this.flag = !this.flag;
		resetToolbar();
	}

	public void toggleCorrection(View view) {
		toggleCorrection();
	}
	
	public void toggleCorrection() {
		this.correction = !this.correction;
		resetToolbar();
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
		this.menu.findItem(R.id.menuCorrection).setChecked(this.correction);
		this.menu.findItem(R.id.menuFlag).setChecked(this.flag);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		 
		switch (item.getItemId())
		{
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
			toggleCorrection();
			return true;
		case R.id.menuFlag:
			toggleFlag();
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
		if (this.flag) {
			findViewById(R.id.flagButton).setBackgroundResource(R.drawable.flag);
		} else {
			findViewById(R.id.flagButton).setBackgroundResource(R.drawable.flag2);
		}
		if (this.correction) {
			findViewById(R.id.correctionButton).setBackgroundResource(R.drawable.check);
		} else {
			findViewById(R.id.correctionButton).setBackgroundResource(R.drawable.check2);
		}
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
	public void onDestroy() {
		ChatConfig config = new ChatConfig();
		config.instance = MainActivity.instance.id;
		config.conversation = MainActivity.conversation;
		config.disconnect = true;
		
		HttpChatAction action = new HttpChatAction(ChatActivity.this, config);
		action.execute();
		
		if (this.tts != null) {
			this.tts.stop();
			this.tts.shutdown();
		}
		if (this.audioPlayer != null) {
			this.audioPlayer.stop();
			this.audioPlayer.release();
		}
		super.onDestroy();
	}
 
	@Override
	public void onInit(int status) {
 
		if (status == TextToSpeech.SUCCESS) {
 
			Locale locale = null;
			VoiceConfig voice = MainActivity.voice;
			if (voice.language != null && voice.language.length() > 0) {
				locale = new Locale(MainActivity.voice.language);
			} else {
				locale = Locale.US;
			}
			int result = this.tts.setLanguage(locale);
			
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
 
	public void response(final ChatResponse response) {
		try {
			this.response = response;	        
			if (this.response.message == null) {
				return;
			}
			final String text = this.response.message;

			this.messages.add(response);
			final ListView list = (ListView) findViewById(R.id.chatList);
			list.invalidateViews();
			list.post(new Runnable() {
		        @Override
		        public void run() {
		        	if (list.getCount() > 2) {
			        	list.setSelection(list.getCount() - 2);
		        	}
		        }
		    });
			
			WebView responseView = (WebView) findViewById(R.id.responseText);
			responseView.loadDataWithBaseURL(null, Utils.linkHTML(text), "text/html", "utf-8", null);
			
			boolean talk = (text.trim().length() > 0) && (MainActivity.deviceVoice || (this.response.speech != null && this.response.speech.length() > 0));
			if (MainActivity.sound && talk) {
				if (!MainActivity.disableVideo && !videoError && this.response.isVideo() && this.response.avatarTalk != null) {
					final VideoView videoView = (VideoView)this.findViewById(R.id.videoView);
					videoView.setOnPreparedListener(new OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mp) {
							try {
								mp.setLooping(true);
								if (!MainActivity.deviceVoice) {
									// Voice audio
									MediaPlayer mediaPlayer = playAudio(response.speech, false, false, false);
									mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
										@Override
										public void onCompletion(MediaPlayer mp) {
											mp.release();
											videoView.post(new Runnable() {
												public void run() {
													playVideo(response.avatar, true);
												}
											});
										}
									});
									mediaPlayer.start();
								} else {
									HashMap<String, String> params = new HashMap<String, String>();
									params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");
									tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
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
						this.tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
					}
				}
			} else if (talk && (!MainActivity.disableVideo && !videoError && this.response.isVideo() && this.response.avatarTalk != null)) {
				final VideoView videoView = (VideoView)this.findViewById(R.id.videoView);
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
						playVideo(response.avatar, true);
					}
				});
				playVideo(this.response.avatarTalk, false);
			}
		} catch (Exception exception) {
			Log.wtf(exception.getMessage(), exception);
		}
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
	
	@Override
	public void onUtteranceCompleted(String utteranceId) {
		try {
			if (!MainActivity.disableVideo && !videoError && this.response.isVideo()) {
				videoView.post(new Runnable() {
					public void run() {
						playVideo(response.avatar, true);
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

	public boolean isCorrection() {
		return correction;
	}

	public void setCorrection(boolean correction) {
		this.correction = correction;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public boolean getWasSpeaking() {
		return wasSpeaking;
	}

	public void setWasSpeaking(boolean wasSpeaking) {
		this.wasSpeaking = wasSpeaking;
	}
}

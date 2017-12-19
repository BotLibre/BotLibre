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

package org.botlibre.sdk.activity.avatar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import org.botlibre.sdk.activity.EmoteSpinAdapter;
import org.botlibre.sdk.activity.EmotionalState;
import org.botlibre.sdk.activity.LibreActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAvatarMessageAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetVideoAction;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.AvatarMessage;
import org.botlibre.sdk.config.ChatResponse;
import org.botlibre.sdk.config.VoiceConfig;

import org.botlibre.sdk.R;

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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * Activity for testing an avatar.
 */
public class AvatarTestActivity extends LibreActivity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
	protected static final int RESULT_SPEECH = 1;
	protected static String message = "Hello, how are you today?";
	
	protected TextToSpeech tts;
	protected VideoView videoView;
	protected EditText textView;
	
	public ChatResponse response;
	public MediaPlayer audioPlayer;
	public String currentAudio;

	public boolean videoError;
	protected volatile boolean wasSpeaking;
	
	private ImageButton btnAction,btnPose,btnLang;
	private AutoCompleteTextView poseText,langText,actionText;
	
	Menu menu;	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_avatar_test);

		AvatarConfig instance = (AvatarConfig)MainActivity.instance;

		setTitle(instance.name);
		((TextView) findViewById(R.id.title)).setText(instance.name);
        HttpGetImageAction.fetchImage(this, instance.avatar, findViewById(R.id.icon));
		
        actionText = (AutoCompleteTextView)findViewById(R.id.actionText);
        poseText = (AutoCompleteTextView)findViewById(R.id.poseText);
        langText = (AutoCompleteTextView)findViewById(R.id.langText);
        
        
		tts = new TextToSpeech(this, this);
		//Button's
		btnPose = (ImageButton) findViewById(R.id.btnPose);
		btnAction = (ImageButton) findViewById(R.id.btnAction);
		btnLang = (ImageButton) findViewById(R.id.btnLang);
		
		CheckBox checkbox = (CheckBox) findViewById(R.id.deviceVoiceCheckBox);
		checkbox.setChecked(MainActivity.deviceVoice);
		checkbox = (CheckBox) findViewById(R.id.hdCheckBox);
		checkbox.setChecked(MainActivity.hd);
		checkbox = (CheckBox) findViewById(R.id.webmCheckBox);
		checkbox.setChecked(MainActivity.webm);
		
		
		btnAction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(AvatarTestActivity.this,AvatarTestItemSelection.class);
				i.putExtra("type", "action");
				startActivityForResult(i,5);
			}
		});
		
		btnPose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(AvatarTestActivity.this,AvatarTestItemSelection.class);
				i.putExtra("type", "pose");
				startActivityForResult(i,6);
			}
		});
		
		btnLang.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(AvatarTestActivity.this,AvatarTestItemSelection.class);
				i.putExtra("type", "lang");
				startActivityForResult(i,7);
			}
		});
		
		
		videoView = (VideoView)findViewById(R.id.videoView);
		resetVideoErrorListener();
		videoError = false;
		
		findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				View settingsView = findViewById(R.id.settingsView);
				if (settingsView.getVisibility() == View.VISIBLE) {
					settingsView.setVisibility(View.GONE);
				} else {
					settingsView.setVisibility(View.VISIBLE);					
				}
			}
		});
		
		findViewById(R.id.videoLayout).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				View settingsView = findViewById(R.id.settingsView);
				if (settingsView.getVisibility() == View.VISIBLE) {
					settingsView.setVisibility(View.GONE);
				} else {
					settingsView.setVisibility(View.VISIBLE);					
				}
			}
		});
		
		textView = (EditText) findViewById(R.id.messageText);
		textView.setText(message);
		textView.setOnEditorActionListener(new OnEditorActionListener() {			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				test();
				return false;
			}
		});

		Spinner emoteSpin = (Spinner) findViewById(R.id.emoteSpin);
		emoteSpin.setAdapter(new EmoteSpinAdapter(this, R.layout.emote_list, Arrays.asList(EmotionalState.values())));

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
					new Toast(Activity.active.frame,true,"Your device doesn't support Speech to Text").showToast(Toast.LENGTH_SHORT);
				}
			}
		});
		
		


		Spinner voiceSpin = (Spinner) findViewById(R.id.voiceSpin);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.voiceNames);
		voiceSpin.setAdapter(adapter);
		
        
		HttpGetImageAction.fetchImage(this, instance.avatar, (ImageView)findViewById(R.id.imageView));
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
				test();
			}
			break;
		}
		case 5:
			if(resultCode == RESULT_OK){
				actionText.setText(data.getExtras().getString("info"));
			}
			break;
		case 6:
			if(resultCode == RESULT_OK){
				poseText.setText(data.getExtras().getString("info"));
			}
			break;
		case 7:
			if(resultCode == RESULT_OK){
				langText.setText(data.getExtras().getString("info"));
			}
			break;
		}
	}
	
	public void toggleProperties(View v) {
		View settingsView = findViewById(R.id.settingsView);
		if (settingsView.getVisibility() == View.VISIBLE) {
			settingsView.setVisibility(View.GONE);
		} else {
			settingsView.setVisibility(View.VISIBLE);					
		}
	}

	public void test(View view) {
		test();
	}
	
	public void test() {
		this.videoError = false;
		
		AvatarMessage config = new AvatarMessage();
		config.instance = MainActivity.instance.id;
		config.avatar = MainActivity.instance.id;
		config.speak = !MainActivity.deviceVoice;
		
		EditText textView = (EditText) findViewById(R.id.messageText);
		config.message = textView.getText().toString().trim();
		message = config.message;

		Spinner emoteSpin = (Spinner) findViewById(R.id.emoteSpin);
		config.emote = emoteSpin.getSelectedItem().toString();

		AutoCompleteTextView actionText = (AutoCompleteTextView) findViewById(R.id.actionText);
		config.action = actionText.getText().toString().trim();

		AutoCompleteTextView poseText = (AutoCompleteTextView) findViewById(R.id.poseText);
		config.pose = poseText.getText().toString().trim();
		
		CheckBox checkbox = (CheckBox) findViewById(R.id.deviceVoiceCheckBox);
		config.speak = !checkbox.isChecked();
		MainActivity.deviceVoice = !config.speak;
		
		checkbox = (CheckBox) findViewById(R.id.hdCheckBox);
		config.hd = !checkbox.isChecked();
		MainActivity.hd = !config.hd;
		
		checkbox = (CheckBox) findViewById(R.id.webmCheckBox);
		config.format = checkbox.isChecked() ? "webm" : "mp4";
		MainActivity.webm = checkbox.isChecked();
				
		if (config.speak) {
			Spinner voiceSpin = (Spinner) findViewById(R.id.voiceSpin);
			config.voice = MainActivity.voices[Arrays.asList(MainActivity.voiceNames).indexOf(voiceSpin.getSelectedItem().toString())];
		} else {
			AutoCompleteTextView langText = (AutoCompleteTextView) findViewById(R.id.langText);
			String lang = langText.getText().toString().trim();
			Locale locale;
			if (lang.length() > 0) {
				locale = new Locale(lang);
			} else {
				locale = Locale.US;
			}
			this.tts.setLanguage(locale);
		}
		
		HttpAvatarMessageAction action = new HttpAvatarMessageAction(AvatarTestActivity.this, config);
		action.execute();

		findViewById(R.id.settingsView).setVisibility(View.GONE);
	}
 
	@Override
	public void onDestroy() {		
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
		super.onDestroy();
	}
 
	@Override
	public void onInit(int status) {
 
		if (status == TextToSpeech.SUCCESS) {
 
			Locale locale = null;
			VoiceConfig voice = MainActivity.voice;
			if (voice.language != null && voice.language.length() > 0) {
				locale = new Locale(voice.language);
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
			final String text = response.message;
			
			boolean talk = (text.trim().length() > 0) && (MainActivity.deviceVoice || (this.response.speech != null && this.response.speech.length() > 0));
			if (MainActivity.sound && talk) {
				if (!MainActivity.disableVideo && !videoError && this.response.isVideo() && this.response.isVideoTalk()) {
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
		System.out.println("playVideo:" + video);
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
			System.out.println("videoUri:" + videoUri);
			if (videoUri == null) {
				videoUri = Uri.parse(MainActivity.connection.fetchImage(video).toURI().toString());
				System.out.println("null videoUri:" + videoUri);
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

/******************************************************************************
 *
 *  Copyright 2017 Paphus Solutions Inc.
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


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.botlibre.sdk.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MicConfiguration extends LibreActivity implements RecognitionListener{
	//Name - used for Log.d(classname, "")
	private final String CLASSNAME = "MicConfiguration";
	
	public boolean active;
	private boolean isRecording;
	private SpeechRecognizer speech;
	private double lastReply =  System.currentTimeMillis();
	private Button btn;
	private TextView txt;
	private boolean failedOfflineLanguage = false;
	private int counter = 0;
	private EditText editTextForGoogle, editTextSpeech;
	private Spinner spinOption;
	private ImageButton micButton;
	private Thread thread;
	
	
	MediaPlayer m = new MediaPlayer();
	
	//Recorder
	private boolean recording = true;
	MediaRecorder myAudioRecorder;
    private String outputFile = null;
    private Button play;
    private CheckBox ckOfflineSpeech, ckDebug;
	private boolean checked = true;


	private boolean clicked = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//set up the view
		setContentView(R.layout.activity_mic_configuration);
		btn = (Button)findViewById(R.id.btnNextTest);
		txt = (TextView) findViewById(R.id.txtMicSt);
		editTextForGoogle = (EditText) findViewById(R.id.googleText);
		editTextSpeech = (EditText) findViewById(R.id.txtSpeech);
		spinOption = (Spinner) findViewById(R.id.spinOptions);
		play = (Button)findViewById(R.id.btnPlayBack);
		micButton = (ImageButton)findViewById(R.id.micButton);
		ckOfflineSpeech = (CheckBox) findViewById(R.id.ckOfflineSpeech);
		ckDebug = (CheckBox) findViewById(R.id.ckDebug);
		
		//Load data
		ckOfflineSpeech.setChecked(MainActivity.offlineSpeech);
		ckDebug.setChecked(ChatActivity.DEBUG);
		
		//disabling buttons for recording sound
        play.setEnabled(false);
        //file saved
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/myrec.3gp";

		//setting the adapter for the dropdown menu
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, new String[]{"Hands Free","Google","Test Mic"});
		spinOption.setAdapter(adapter);
		
		//Creating Speech (Hands Free)
		speech = SpeechRecognizer.createSpeechRecognizer(this);
		speech.setRecognitionListener(this);
		
		spinOption.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				txt.setText("Status: OFF");
				
				spinOption.setSelection(pos);
				LinearLayout test1 = (LinearLayout) findViewById(R.id.test1);
				LinearLayout test2 = (LinearLayout) findViewById(R.id.test2);
				LinearLayout test3 = (LinearLayout) findViewById(R.id.test3);
				switch(pos){
				case 0:
					test1.setVisibility(View.VISIBLE);
					test2.setVisibility(View.GONE);
					test3.setVisibility(View.GONE);
					break;
				case 1:
					test1.setVisibility(View.GONE);
					test3.setVisibility(View.GONE);
					test2.setVisibility(View.VISIBLE);
					break;
				case 2:
					ActivityCompat.requestPermissions(MicConfiguration.this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 2);
					test1.setVisibility(View.GONE);
					test3.setVisibility(View.VISIBLE);
					test2.setVisibility(View.GONE);
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		
		//CheckBox OfflineSpeech
		ckOfflineSpeech.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor cookies = MainActivity.current.getPreferences(MODE_PRIVATE).edit();
				if (isChecked) {
					MainActivity.offlineSpeech = true;
					cookies.putBoolean("offlineSpeech", true);
					cookies.commit();
					return;
				}
				//else
				MainActivity.offlineSpeech = false;
				cookies.putBoolean("offlineSpeech", false);
				cookies.commit();
			}
		});
		//CheckBox Debugger
		ckDebug.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor cookies = MainActivity.current.getPreferences(MODE_PRIVATE).edit();
				if (isChecked) {
					ChatActivity.DEBUG = true;
					cookies.putBoolean("debug", true);
					cookies.commit();
					return;
				}
				//else
				ChatActivity.DEBUG = false;
				cookies.putBoolean("debug", false);
				cookies.commit();
			}
		});
	}

	// permission
	@SuppressLint("Override")
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 2: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					View micButton1 = findViewById(R.id.micButton1);
					((ImageButton) micButton1).setEnabled(true);
				} else {
					Toast.makeText(MicConfiguration.this, "Permission denied to read your External storage",
							Toast.LENGTH_SHORT).show();
					View micButton1 = findViewById(R.id.micButton1);
					((ImageButton) micButton1).setEnabled(false);
				}
				return;
			}
		}
	}
	
	private void setStreamVolume() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		if (volume != 0) {
			Log.d("ChatActivity","The volume changed and saved to : " + volume);
			MainActivity.volume = volume;
		} 
	}
	
	@Override
	protected void onResume() {
		//remember the option selected from the spinner
		if (checked) {
			if (MainActivity.handsFreeSpeech) {
				spinOption.setSelection(0);
			} else {
				spinOption.setSelection(1);
			}
			checked = false;
		}
		super.onResume();
	}
	
	public void MicClicked(View v) {
		if (txt.getText().equals("Status: Timeout!")) {
			Intent intent = getIntent();
			finish();
			startActivity(intent);
			return;
		}
		if (clicked) {
			clicked = false;
			
			txt.setText("Status: Loading...");
			micButton.setEnabled(false);
			active = true;
			lastReply = System.currentTimeMillis();
			thread = new Thread() {
				@Override
				public void run() {
					Log.e("Thread Run", "Thread running!");
					while (active) {
						Log.e("Thread - Active", "Thread is active.");
						if (!isRecording && (System.currentTimeMillis() - lastReply) > 6000) {
							restartListening();
							timeout();
						}
						try {
							// For CPU and Battery
							Thread.sleep(2000);
						} catch (Exception exception) {
						}
					}
				}
			};
			thread.start();
		}
	}
	
	public void timeout() {
		counter++;
		if (counter == 3) {
			this.runOnUiThread(new Runnable() {
				@SuppressWarnings("deprecation")
				public void run() {
					try {
						lastReply = System.currentTimeMillis();
						active = false;
						stopListening();
						try {
							if (thread != null) {
								thread.stop();
							}
						} catch(Exception e) {Log.d(CLASSNAME, "e: " + e.getMessage());}
						txt.setText("Status: Timeout!");
						isRecording = false;
						counter = 0;
						clicked = true;
					} catch (Exception e) {
						Log.e("ErrorChatActivity", "Error: " + e.getMessage());
					}
				}
			});
		}
	}
	
	public void googleListening(View v){
		txt.setText("Status: ON");
		setMicIcon(true, false);
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); 
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, MainActivity.voice.language);
		try {
			startActivityForResult(intent, 1);
			editTextForGoogle.setText("");
		} catch (ActivityNotFoundException a) {
			Toast t = Toast.makeText(getApplicationContext(),
					"Your device doesn't support Speech to Text",
					Toast.LENGTH_SHORT);
			t.show();
		}
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
		finish();
		super.onBackPressed();
	}
	
	public boolean deleteRecordedFile(String outputFile) {
		boolean deleted = false;
		File file = new File(outputFile);
		if (file.exists()) {
			deleted = file.delete();
		}
		return deleted;
	}
	
	public void play(View v) throws IOException {
		try {
			m.reset();
			m.setDataSource(outputFile);
			m.prepare();
			m.start();
			txt.setText("Status: Done");
		} catch (Exception e) {
		}
	}
	
	public void recording (View v){
		if(recording){
			setMicIcon(true, true);
			try {
				//mediaPlayer
		        myAudioRecorder = new MediaRecorder();
		        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
		        myAudioRecorder.setOutputFile(outputFile);
		        
	            myAudioRecorder.prepare();
	            myAudioRecorder.start();
	        } catch (IllegalStateException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			play.setEnabled(false);
	        txt.setText("Status: Recording...");
			recording = false;
		}else{
			myAudioRecorder.stop();
	        myAudioRecorder.release();
	        play.setEnabled(true);
	        txt.setText("Status: Audio Recorded");
			setMicIcon(false, false);
			recording = true;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
 
		switch (requestCode) {
			
			case 1: {
				if (resultCode == RESULT_OK && data != null) {
					setMicIcon(false, false);
					ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					editTextForGoogle.setText(text.get(0));
					txt.setText("Status: Done!");
				}
				break;
			}
		}
	}
	
	public void select(View v) {
		if (spinOption.getSelectedItem().equals("Hands Free")) {
			MainActivity.handsFreeSpeech = true;
			MainActivity.listenInBackground = true;
			SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
	    	cookies.putBoolean("handsfreespeech", MainActivity.handsFreeSpeech);
	    	cookies.putBoolean("listenInBackground", MainActivity.listenInBackground);
	    	cookies.commit();
	    	
		} else if (spinOption.getSelectedItem().equals("Google")) {
			MainActivity.handsFreeSpeech = false;
			SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
	    	cookies.putBoolean("handsfreespeech", MainActivity.handsFreeSpeech);
	    	cookies.commit();
		}
		if (btn.getText().equals("Select")) {
			Intent intent = new Intent(this, ChatActivity.class);
	        startActivity(intent);
			finish();
		}
	}
	
	private void stopListening() {
		try {
			txt.setText("Status: OFF");
			setMicIcon(false, false);
			muteMicBeep(false);
			this.speech.stopListening();
		} catch (Exception ignore) {
			Log.e("StopListening", "Error" + ignore.getMessage());
		}
	}
	
	@Override
	public void onBeginningOfSpeech() {
		txt.setText("Status: Recording...");
		lastReply = System.currentTimeMillis();
		isRecording = true;
		setMicIcon(true, true);
	}

	@Override
	public void onBufferReceived(byte[] arg0) {}

	@Override
	public void onEndOfSpeech() {
		txt.setText("Status: Done!");
		lastReply = System.currentTimeMillis();
		isRecording = false;
		setMicIcon(false, false);
		active = false;
		try {
			if (thread != null) {
				thread.stop();
			}
		} catch(Exception e) {Log.d(CLASSNAME, "e: " + e.getMessage());}
		clicked = true;
		muteMicBeep(false);
	}
	
	private void restartListening() {
		lastReply = System.currentTimeMillis();
		if(!active){
			setMicIcon(false, false);
			return;
		}
		this.runOnUiThread(new Runnable() {
			public void run() {
				try{
				beginListening();
				}catch(Exception e){
					
				}
			}
		});
	}
	
	@Override
	public void onError(int error) {
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
			restartListening();
		} else if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
			Log.d("System.out", "Error: NO speech input");
			restartListening();
		}
		
	}

	@Override
	public void onEvent(int arg0, Bundle arg1) {}

	@Override
	public void onPartialResults(Bundle arg0) {}

	@Override
	public void onReadyForSpeech(Bundle arg0) {
		micButton.setEnabled(true);
		txt.setText("Status: ON");
		lastReply = System.currentTimeMillis();
	}

	@Override
	public void onResults(Bundle results) {
		List<String> text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		editTextSpeech.setText(text.get(0));
	}

	@Override
	public void onRmsChanged(float arg0) {
		
	}
	
	private void setMicIcon(boolean on, boolean recording) {
		try {
			View micButton = findViewById(R.id.micButton);
			View micButton1 = findViewById(R.id.micButton1);
			
			if (!on) {
				((ImageButton) micButton).setImageResource(R.drawable.micoff);
				((ImageButton) micButton1).setImageResource(R.drawable.micoff);
			} else if (on && recording) {
				((ImageButton) micButton).setImageResource(R.drawable.micrecording);
				((ImageButton) micButton1).setImageResource(R.drawable.micrecording);
			} else {
				((ImageButton) micButton).setImageResource(R.drawable.mic);
			}
		} catch (Exception e) {Log.e("ChatActivity.setMicIcon","" + e.getMessage());}
	}
	
	private void muteMicBeep(boolean mute) {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);	
		if (mute) {
			//if its true then the Volume will be zero.
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
		} else {
			//if its false, the Volume will put back no
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, MainActivity.volume, 0);
		}
	}
	
	@TargetApi(23)
	private void beginListening() {
		setStreamVolume();
		lastReply = System.currentTimeMillis();
		
		muteMicBeep(true);
		
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		if (MainActivity.offlineSpeech) {
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, MainActivity.voice.language);

			if (!this.failedOfflineLanguage) {
				//en-US will use the English in offline.
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
			txt.setText("Status: Your device doesn't support Speech to text.");
		}		
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onDestroy() {
		deleteRecordedFile(outputFile);
		active = false;
		muteMicBeep(false);
		try {
			if (thread != null) {
				thread.stop();
			}
		} catch(Exception e) {Log.d(CLASSNAME, "e: " + e.getMessage());}
		super.onDestroy();
	}

}

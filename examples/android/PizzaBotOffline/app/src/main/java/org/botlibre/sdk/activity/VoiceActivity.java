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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetVideoAction;
import org.botlibre.sdk.activity.actions.HttpGetVoiceAction;
import org.botlibre.sdk.activity.actions.HttpSaveVoiceAction;
import org.botlibre.sdk.activity.actions.HttpSpeechAction;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.Speech;
import org.botlibre.sdk.config.VoiceConfig;

import org.botlibre.offline.pizzabot.R;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Activity for administering a bot's voice.
 */
public class VoiceActivity extends LibreActivity implements TextToSpeech.OnInitListener {
    protected static final int RESULT_SPEECH = 1;
    
    private TextToSpeech tts;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        
        HttpGetImageAction.fetchImage(this, MainActivity.instance.avatar, findViewById(R.id.icon));
		this.tts = new TextToSpeech(this, this);
        
        HttpGetVoiceAction action = new HttpGetVoiceAction(this, (InstanceConfig)MainActivity.instance.credentials());
    	action.execute();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetView(){
		try {
	        if (MainActivity.voice == null) {
	        	MainActivity.voice = new VoiceConfig();
	        }

			Spinner voiceSpin = (Spinner) findViewById(R.id.voiceSpin);
			Spinner voiceSpinMod = (Spinner)findViewById(R.id.voiceModSpin);
			
			ArrayAdapter adapter = new ArrayAdapter(this,
	                android.R.layout.simple_spinner_dropdown_item, MainActivity.voiceNames);
			voiceSpin.setAdapter(adapter);
			int index = Arrays.asList(MainActivity.voices).indexOf(MainActivity.voice.voice);
			if (index != -1) {
				voiceSpin.setSelection(index);
			}
			
			ArrayAdapter adapter1 = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
					MainActivity.voiceMods);
			voiceSpinMod.setAdapter(adapter1);
			index = Arrays.asList(MainActivity.voiceMods).indexOf(MainActivity.voice.mod);
			if (index != -1) {
				voiceSpinMod.setSelection(index);
			}

			CheckBox checkBox = (CheckBox) findViewById(R.id.deviceVoiceCheckBox);
			checkBox.setChecked(MainActivity.voice.nativeVoice);
			EditText text = (EditText) findViewById(R.id.pitchText);
			text.setText(MainActivity.voice.pitch);
			text = (EditText) findViewById(R.id.speechRateText);
			text.setText(MainActivity.voice.speechRate);
		} catch (Exception e) {
			MainActivity.error(e.getMessage(), e, this);
		}
	}
 
    @Override
    public void onDestroy() {
        if (this.tts != null) {
        	this.tts.stop();
        	this.tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public void onInit(int status) { 
        if (status == TextToSpeech.SUCCESS) {
            List<String> locales = new ArrayList<String>();
            if (MainActivity.voice.language != null) {
            	locales.add(MainActivity.voice.language);
            }
            locales.add(Locale.US.toString());
            locales.add(Locale.UK.toString());
            locales.add(Locale.FRENCH.toString());
            locales.add(Locale.GERMAN.toString());
            locales.add("ES");
            locales.add("PT");
            locales.add(Locale.ITALIAN.toString());
            locales.add(Locale.CHINESE.toString());
            locales.add(Locale.JAPANESE.toString());
            locales.add(Locale.KOREAN.toString());
            for (Locale locale : Locale.getAvailableLocales()) {
            	try {
	            	int code = this.tts.isLanguageAvailable(locale);
	                if (code != TextToSpeech.LANG_NOT_SUPPORTED) {
	                	locales.add(locale.toString());
	                }
            	} catch (Exception ignore) {}
            }
    		Spinner spin = (Spinner) findViewById(R.id.languageSpin);
    		ArrayAdapter adapter = new ArrayAdapter(this,
                    android.R.layout.simple_spinner_dropdown_item, locales.toArray());
    		spin.setAdapter(adapter);
            if (MainActivity.voice.language != null) {
            	spin.setSelection(locales.indexOf(MainActivity.voice.language));
            }
    		
            int result = this.tts.setLanguage(Locale.US); 
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
 
    }

	public void save(View view) {
		VoiceConfig config = new VoiceConfig();
        config.instance = MainActivity.instance.id;

        Spinner spin = (Spinner) findViewById(R.id.voiceSpin);
		config.voice = MainActivity.voices[Arrays.asList(MainActivity.voiceNames).indexOf(spin.getSelectedItem().toString())];
		spin = (Spinner) findViewById(R.id.voiceModSpin);
		config.mod = spin.getSelectedItem().toString();
		Log.e("VOICE SETTINGS","MODE: " + config.mod);
        spin = (Spinner) findViewById(R.id.languageSpin);
        config.language = spin.getSelectedItem().toString();
		EditText text = (EditText) findViewById(R.id.pitchText);
		config.pitch = text.getText().toString();
		text = (EditText) findViewById(R.id.speechRateText);
		config.speechRate = text.getText().toString();		
		CheckBox checkbox = (CheckBox) findViewById(R.id.deviceVoiceCheckBox);
		config.nativeVoice = checkbox.isChecked();
		MainActivity.deviceVoice = config.nativeVoice;
        
        HttpSaveVoiceAction action = new HttpSaveVoiceAction(this, config);
		action.execute();
	}
 
    public void test(View view) {

    	EditText text = (EditText) findViewById(R.id.testText);
		String test = text.getText().toString();

		CheckBox deviceVoiceCheckBox = (CheckBox) findViewById(R.id.deviceVoiceCheckBox);
		if (deviceVoiceCheckBox.isChecked()) {
	        Spinner spin = (Spinner) findViewById(R.id.languageSpin);

	        int result = this.tts.setLanguage(new Locale((String)spin.getSelectedItem()));
	        if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
	            MainActivity.error("This Language is not supported", null, this);
	        }
			text = (EditText) findViewById(R.id.pitchText);
			String value = text.getText().toString();
			float pitch = 1;
			if (value.trim().length() > 0) {
				try {
					pitch = Float.valueOf(value);
				} catch (Exception exception) {}
			}
			this.tts.setPitch(pitch);
			text = (EditText) findViewById(R.id.speechRateText);
			value = text.getText().toString();
			float speechRate = 1;
			if (value.trim().length() > 0) {
				try {
					speechRate = Float.valueOf(value);
				} catch (Exception exception) {}
			}
			tts.setSpeechRate(speechRate);
		
			this.tts.speak(test, TextToSpeech.QUEUE_FLUSH, null);
		} else {
	        Spinner spin = (Spinner) findViewById(R.id.voiceSpin);
			Speech config = new Speech();
			config.voice = MainActivity.voices[Arrays.asList(MainActivity.voiceNames).indexOf(spin.getSelectedItem().toString())];
			spin = (Spinner) findViewById(R.id.voiceModSpin);
			config.mod = spin.getSelectedItem().toString();
			config.text = test;
			
			HttpSpeechAction action = new HttpSpeechAction(VoiceActivity.this, config);
			action.execute();
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
}

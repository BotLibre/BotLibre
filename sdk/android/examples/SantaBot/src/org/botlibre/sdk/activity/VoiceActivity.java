package org.botlibre.sdk.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.paphus.botlibre.client.android.santabot.R;
import org.botlibre.sdk.activity.actions.HttpSaveVoiceAction;
import org.botlibre.sdk.config.VoiceConfig;

/**
 * Activity for administering a bot's voice.
 */
public class VoiceActivity extends Activity implements TextToSpeech.OnInitListener {
    protected static final int RESULT_SPEECH = 1;
    
    private TextToSpeech tts;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        
        setTitle("Voice: " + MainActivity.instance.name);
        
        this.tts = new TextToSpeech(this, this);
        
        if (MainActivity.voice == null) {
        	MainActivity.voice = new VoiceConfig();
        }
		EditText text = (EditText) findViewById(R.id.pitchText);
		text.setText(MainActivity.voice.pitch);
		text = (EditText) findViewById(R.id.speechRateText);
		text.setText(MainActivity.voice.speechRate);
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
            List<Locale> locales = new ArrayList<Locale>();
            locales.add(Locale.US);
            locales.add(Locale.UK);
            locales.add(Locale.FRENCH);
            locales.add(Locale.GERMAN);
            locales.add(new Locale("ES"));
            locales.add(Locale.ITALIAN);
            locales.add(Locale.CHINESE);
            locales.add(Locale.JAPANESE);
            locales.add(Locale.KOREAN);
            for (Locale locale : Locale.getAvailableLocales()) {
            	try {
	            	int code = this.tts.isLanguageAvailable(locale);
	                if (code != TextToSpeech.LANG_NOT_SUPPORTED) {
	                	locales.add(locale);
	                }
            	} catch (Exception ignore) {}
            }
    		Spinner spin = (Spinner) findViewById(R.id.languageSpin);
    		ArrayAdapter adapter = new ArrayAdapter(this,
                    android.R.layout.simple_spinner_dropdown_item, locales.toArray());
    		spin.setAdapter(adapter);
    		spin.setSelection(locales.indexOf(MainActivity.voice.language));
    		
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
        
        Spinner spin = (Spinner) findViewById(R.id.languageSpin);
        config.language = spin.getSelectedItem().toString();
		EditText text = (EditText) findViewById(R.id.pitchText);
		config.pitch = text.getText().toString();
		text = (EditText) findViewById(R.id.speechRateText);
		config.speechRate = text.getText().toString();
        
        HttpSaveVoiceAction action = new HttpSaveVoiceAction(this, config);
		action.execute();
	}
 
    public void test(View view) {
        Spinner spin = (Spinner) findViewById(R.id.languageSpin);

        int result = this.tts.setLanguage((Locale)spin.getSelectedItem());
        if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
            MainActivity.error("This Language is not supported", null, this);
        }
		EditText text = (EditText) findViewById(R.id.pitchText);
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

		text = (EditText) findViewById(R.id.testText);
		String test = text.getText().toString();
		
		this.tts.speak(test, TextToSpeech.QUEUE_FLUSH, null);
    }
}

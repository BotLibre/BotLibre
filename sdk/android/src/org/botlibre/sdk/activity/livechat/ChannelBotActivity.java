package org.botlibre.sdk.activity.livechat;

import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpGetChannelBotModeAction;
import org.botlibre.sdk.activity.actions.HttpSaveChannelBotModeAction;
import org.botlibre.sdk.config.BotModeConfig;
import org.botlibre.sdk.config.ChannelConfig;

/**
 * Activity for administering a channel's bot.
 */
public class ChannelBotActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_bot);

        setTitle("Bot: " + MainActivity.instance.name);
        
        HttpAction action = new HttpGetChannelBotModeAction(this, (ChannelConfig)MainActivity.instance.credentials());
    	action.execute();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetView() {
		Spinner spin = (Spinner) findViewById(R.id.botModeSpin);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.botModes);
		spin.setAdapter(adapter);
		spin.setSelection(Arrays.asList(MainActivity.botModes).indexOf(MainActivity.botMode.mode));
		
		EditText text = (EditText) findViewById(R.id.botText);
		text.setText(MainActivity.botMode.bot);
	}

	public void save(View view) {
		BotModeConfig config = new BotModeConfig();
        config.instance = MainActivity.instance.id;
        
        Spinner spin = (Spinner) findViewById(R.id.botModeSpin);
        config.mode = spin.getSelectedItem().toString();
        
		EditText text = (EditText) findViewById(R.id.botText);
		config.bot = text.getText().toString();
        
        HttpAction action = new HttpSaveChannelBotModeAction(this, config);
		action.execute();
	}
}

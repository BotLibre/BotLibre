package org.botlibre.sdk.activity.livechat;

import java.util.Arrays;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.EditWebMediumActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpUpdateAction;
import org.botlibre.sdk.config.ChannelConfig;

/**
 * Activity for editing a channel's details.
 */
public class EditChannelActivity extends EditWebMediumActivity {

	@Override
	public String getType() {
		return "Channel";
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_channel);

		ChannelConfig instance = (ChannelConfig)MainActivity.instance;
		
		resetView();

		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.channelTypes);
		spin.setAdapter(adapter);
		spin.setSelection(Arrays.asList(MainActivity.channelTypes).indexOf(instance.type));
	}

    public void save(View view) {
    	ChannelConfig instance = new ChannelConfig();
    	saveProperties(instance);

    	Spinner spin = (Spinner) findViewById(R.id.typeSpin);
    	instance.type = (String)spin.getSelectedItem();
        
    	HttpAction action = new HttpUpdateAction(this, instance);
        action.execute();
    }
}

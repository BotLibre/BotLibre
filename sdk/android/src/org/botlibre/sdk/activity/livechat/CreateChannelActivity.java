package org.botlibre.sdk.activity.livechat;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.CreateWebMediumActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpCreateAction;
import org.botlibre.sdk.config.ChannelConfig;

/**
 * Activity for creating a new channel.
 */
public class CreateChannelActivity extends CreateWebMediumActivity {
	
	@Override
	public String getType() {
		return "Channel";
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_channel);
        
        resetView();

		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.channelTypes);
		spin.setAdapter(adapter);
	}
    
    /**
     * Create the instance.
     */
    public void create(View view) {
    	ChannelConfig instance = new ChannelConfig();
    	saveProperties(instance);

    	Spinner spin = (Spinner) findViewById(R.id.typeSpin);
    	instance.type = (String)spin.getSelectedItem();
		
        HttpAction action = new HttpCreateAction(this, instance);
        action.execute();
    }
}

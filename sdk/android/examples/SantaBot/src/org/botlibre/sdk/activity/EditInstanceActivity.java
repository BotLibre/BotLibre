package org.botlibre.sdk.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.paphus.botlibre.client.android.santabot.R;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpUpdateAction;
import org.botlibre.sdk.config.InstanceConfig;

/**
 * Activity for editing a bot's details.
 */
public class EditInstanceActivity extends EditWebMediumActivity {

	@Override
	public String getType() {
		return "Bot";
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_instance);

		resetView();
		
		InstanceConfig instance = (InstanceConfig)MainActivity.instance;
		
		CheckBox checkbox = (CheckBox) findViewById(R.id.forkingCheckBox);
		checkbox.setChecked(instance.allowForking);
	}

    public void save(View view) {
    	InstanceConfig instance = new InstanceConfig();   	
    	saveProperties(instance);
    	
		CheckBox checkbox = (CheckBox) findViewById(R.id.forkingCheckBox);
		instance.allowForking = checkbox.isChecked();
        
        HttpAction action = new HttpUpdateAction(this, instance);
        action.execute();
    }
}

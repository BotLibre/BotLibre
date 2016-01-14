package org.botlibre.sdk.activity.forum;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.CreateWebMediumActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpCreateAction;
import org.botlibre.sdk.config.ForumConfig;

/**
 * Activity for creating a new forum.
 */
public class CreateForumActivity extends CreateWebMediumActivity {

	@Override
	public String getType() {
		return "Forum";
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_forum);
        
        resetView();

        Spinner spin = (Spinner) findViewById(R.id.postModeSpin);
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.accessModes);
		spin.setAdapter(adapter);

		spin = (Spinner) findViewById(R.id.replyModeSpin);
		adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.accessModes);
		spin.setAdapter(adapter);
	}
    
    /**
     * Create the instance.
     */
    public void create(View view) {
    	ForumConfig instance = new ForumConfig();
    	saveProperties(instance);
    	
    	Spinner spin = (Spinner) findViewById(R.id.postModeSpin);
    	instance.postAccessMode = (String)spin.getSelectedItem();
    	spin = (Spinner) findViewById(R.id.replyModeSpin);
    	instance.replyAccessMode = (String)spin.getSelectedItem();
		
    	HttpAction action = new HttpCreateAction(this, instance);
        action.execute();
    }
}

package org.botlibre.sdk.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.actions.HttpCreateUserAction;
import org.botlibre.sdk.config.UserConfig;

/**
 * Activity for creating a new user.
 */
public class CreateUserActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);		
	}
    
    /**
     * Create the user.
     */
    public void create(View view) {
    	UserConfig config = new UserConfig();
    	
        EditText text = (EditText) findViewById(R.id.userText);
        config.user = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.passwordText);
        config.password = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.hintText);
        config.hint = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.nameText);
        config.name = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.emailText);
        config.email = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.websiteText);
        config.website = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.bioText);
        config.bio = text.getText().toString().trim();
		CheckBox checkbox = (CheckBox)findViewById(R.id.showNameCheckBox);
		config.showName = checkbox.isChecked();
        
    	HttpCreateUserAction action = new HttpCreateUserAction(this, config);
    	action.execute();
    }
    
    /**
     * Cancel
     */
    public void cancel(View view) {        
    	finish();
    }
}

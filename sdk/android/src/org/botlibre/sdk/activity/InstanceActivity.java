package org.botlibre.sdk.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import org.botlibre.sdk.R;
import org.botlibre.sdk.config.InstanceConfig;

/**
 * Activity for viewing a bot's details.
 * To launch this activity from your app you can use the HttpFetchAction passing the bot id or name as a config.
 */
public class InstanceActivity extends WebMediumActivity {
	
	public String getType() {
		return "Bot";
	}

	public void resetView() {
        setContentView(R.layout.activity_instance);
		
        InstanceConfig instance = (InstanceConfig)MainActivity.instance;

        super.resetView();

        if (instance.isExternal) {
        	findViewById(R.id.chatButton).setVisibility(View.GONE);
        }
        
    	TextView text = (TextView) findViewById(R.id.sizeLabel);
        if (instance.size != null && instance.size.length() > 0) {
	        text.setText(instance.size + " neurons");
        } else {
	        text.setText("");
        }

    	text = (TextView) findViewById(R.id.connectsLabel);
        if (instance.connects != null && instance.connects.length() > 0) {
	        text.setText(instance.connects + " conects, " + instance.dailyConnects + " today, " + instance.weeklyConnects + " week, " + instance.monthlyConnects + " month");
        } else {
	        text.setText("");
        }
	}

	public void admin() {
        Intent intent = new Intent(this, AdminActivity.class);		
        startActivity(intent);
	}

	public void fork() {
		MainActivity.template = MainActivity.instance.name;
        Intent intent = new Intent(this, CreateInstanceActivity.class);
        startActivity(intent);
        
        finish();
	}
	
	/**
	 * Start a chat session with the selected instance and the user.
	 */
	public void openChat(View view) {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }
	
}

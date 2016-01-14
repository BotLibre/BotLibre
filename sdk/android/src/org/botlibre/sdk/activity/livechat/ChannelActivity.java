package org.botlibre.sdk.activity.livechat;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.WebMediumActivity;
import org.botlibre.sdk.config.ChannelConfig;

/**
 * Activity for viewing a channels details.
 * To launch this activity from your app you can use the HttpFetchAction passing the channel id or name as a config.
 */
public class ChannelActivity extends WebMediumActivity {

	public void admin() {
        Intent intent = new Intent(this, ChannelAdminActivity.class);		
        startActivity(intent);
	}

	public void resetView() {
        setContentView(R.layout.activity_channel);
		
        ChannelConfig instance = (ChannelConfig)MainActivity.instance;

        super.resetView();

        if (instance.isExternal) {
        	findViewById(R.id.chatButton).setVisibility(View.GONE);
        }

    	TextView text = (TextView) findViewById(R.id.messagesLabel);
        if (instance.messages != null && instance.messages.length() > 0) {
	        text.setText(instance.messages + " messages");
        } else {
	        text.setText("");
        }

    	text = (TextView) findViewById(R.id.onlineLabel);
        if (instance.usersOnline != null && instance.messages.length() > 0) {
	        text.setText(instance.usersOnline + " users online, " + instance.adminsOnline + " admins");
        } else {
	        text.setText("");
        }

    	text = (TextView) findViewById(R.id.typeLabel);
        text.setText(instance.type);
	}
	
	public String getType() {
		return "Channel";
	}
	
	/**
	 * Start a chat session with the selected instance and the user.
	 */
	public void chat(View view) {
        Intent intent = new Intent(this, LiveChatActivity.class);
        startActivity(intent);
    }
	
}

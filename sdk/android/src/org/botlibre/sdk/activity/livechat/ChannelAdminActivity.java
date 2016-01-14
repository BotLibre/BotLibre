package org.botlibre.sdk.activity.livechat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.WebMediumAdminActivity;

/**
 * Activity for a channel's admin functions.
 */
public class ChannelAdminActivity extends WebMediumAdminActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_channel);
		
        resetView();
        
	}

	public void adminUsers(View view) {
        Intent intent = new Intent(this, ChannelUsersActivity.class);		
        startActivity(intent);
	}

	public void adminBot(View view) {
        Intent intent = new Intent(this, ChannelBotActivity.class);		
        startActivity(intent);
	}

	public void editInstance(View view) {
        Intent intent = new Intent(this, EditChannelActivity.class);		
        startActivity(intent);
	}
	
}

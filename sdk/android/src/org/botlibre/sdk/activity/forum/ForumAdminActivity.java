package org.botlibre.sdk.activity.forum;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.WebMediumAdminActivity;

/**
 * Activity for a forum's admin functions.
 */
public class ForumAdminActivity extends WebMediumAdminActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_forum);
		
        resetView();        
	}

	public void adminUsers(View view) {
        Intent intent = new Intent(this, ForumUsersActivity.class);		
        startActivity(intent);
	}

	public void adminBot(View view) {
        Intent intent = new Intent(this, ForumBotActivity.class);		
        startActivity(intent);
	}

	public void editInstance(View view) {
        Intent intent = new Intent(this, EditForumActivity.class);		
        startActivity(intent);
	}
	
}

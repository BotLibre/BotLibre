package org.botlibre.sdk.activity;

import com.paphus.botlibre.client.android.santabot.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Activity for a bot's admin functions.
 */
public class AdminActivity extends WebMediumAdminActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
		
        resetView();        
	}

	public void adminAvatars(View view) {
        Intent intent = new Intent(this, AvatarActivity.class);		
        startActivity(intent);
	}

	public void adminVoice(View view) {
        Intent intent = new Intent(this, VoiceActivity.class);		
        startActivity(intent);
	}

	public void adminLearning(View view) {
        Intent intent = new Intent(this, LearningActivity.class);		
        startActivity(intent);
	}

	public void adminTraining(View view) {
        Intent intent = new Intent(this, TrainingActivity.class);		
        startActivity(intent);
	}

	public void adminUsers(View view) {
        Intent intent = new Intent(this, UsersActivity.class);		
        startActivity(intent);
	}

	public void editInstance(View view) {
        Intent intent = new Intent(this, EditInstanceActivity.class);		
        startActivity(intent);
	}
	
}

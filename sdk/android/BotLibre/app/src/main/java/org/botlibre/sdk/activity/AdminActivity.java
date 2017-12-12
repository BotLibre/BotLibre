/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package org.botlibre.sdk.activity;

import org.botlibre.sdk.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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

	public void adminAvatar(View view) {
        Intent intent = new Intent(this, BotAvatarActivity.class);		
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
	
	public void adminScripts(View view) {
		Intent intent = new Intent(this, BotScriptsActivity.class);
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

	public void adminTwitter(View view) {
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setMessage("You can connect your bot to Twitter from our website, login and go to your bot's Admin Console.");
		dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	  openWebsite();
		      }
		    });
		dialog.show();
	}

	public void adminFacebook(View view) {
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setMessage("You can connect your bot to Facebook from our website, login and go to your bot's Admin Console.");
		dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	  openWebsite();
		      }
		    });
		dialog.show();
	}

	public void adminTelegram(View view) {
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setMessage("You can connect your bot to Telegram from our website, login and go to your bot's Admin Console.");
		dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	  openWebsite();
		      }
		    });
		dialog.show();
	}

	public void adminSlack(View view) {
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setMessage("You can connect your bot to Slack from our website, login and go to your bot's Admin Console.");
		dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	  openWebsite();
		      }
		    });
		dialog.show();
	}

	public void adminSMS(View view) {
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setMessage("You can connect your bot to SMS from our website, login and go to your bot's Admin Console.");
		dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	  openWebsite();
		      }
		    });
		dialog.show();
	}

	public void adminEmail(View view) {
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setMessage("You can connect your bot to email from our website, login and go to your bot's Admin Console.");
		dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	  openWebsite();
		      }
		    });
		dialog.show();
	}

	public void adminIRC(View view) {
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setMessage("You can connect your bot to IRC from our website, login and go to your bot's Admin Console.");
		dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int which) {
		    	  openWebsite();
		      }
		    });
		dialog.show();
	}

	public void editInstance(View view) {
        Intent intent = new Intent(this, EditBotActivity.class);		
        startActivity(intent);
	}
	
	public void openWebsite() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.WEBSITE + "/browse?id=" + MainActivity.instance.id));
		startActivity(intent);
	}
	
}

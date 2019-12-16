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

package org.botlibre.sdk.activity.forum;

import java.util.Arrays;

import org.botlibre.sdk.activity.LibreActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpGetForumBotModeAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpSaveForumBotModeAction;
import org.botlibre.sdk.config.BotModeConfig;
import org.botlibre.sdk.config.ForumConfig;

import org.botlibre.offline.pizzabot.R;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Activity for administering a forum's bot.
 */
public class ForumBotActivity extends LibreActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_bot);
        
        HttpGetImageAction.fetchImage(this, MainActivity.instance.avatar, findViewById(R.id.icon));
        
        HttpAction action = new HttpGetForumBotModeAction(this, (ForumConfig)MainActivity.instance.credentials());
    	action.execute();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetView() {
		Spinner spin = (Spinner) findViewById(R.id.botModeSpin);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.botModes);
		spin.setAdapter(adapter);
		spin.setSelection(Arrays.asList(MainActivity.botModes).indexOf(MainActivity.botMode.mode));
		
		EditText text = (EditText) findViewById(R.id.botText);
		text.setText(MainActivity.botMode.bot);
	}

	public void save(View view) {
		BotModeConfig config = new BotModeConfig();
        config.instance = MainActivity.instance.id;
        
        Spinner spin = (Spinner) findViewById(R.id.botModeSpin);
        config.mode = spin.getSelectedItem().toString();
        
		EditText text = (EditText) findViewById(R.id.botText);
		config.bot = text.getText().toString();
        
        HttpAction action = new HttpSaveForumBotModeAction(this, config);
		action.execute();
	}
}

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

package org.botlibre.sdk.activity.war;

import java.util.ArrayList;
import java.util.List;

import org.botlibre.sdk.activity.ImageListAdapter;
import org.botlibre.sdk.activity.LibreActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpFetchWarAction;
import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.WebMediumConfig;

import org.botlibre.sdk.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity for user login.
 */
public class StartWarActivity extends LibreActivity {
	protected int browsing = 0;
	public static InstanceConfig winner;
	public static InstanceConfig looser;
	public static InstanceConfig bot1;
	public static InstanceConfig bot2;
	public static String topic = "Hello";
	
	private static long SECRET = 4357845875643L;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_war);

        ((EditText)findViewById(R.id.topicText)).setText(topic);
        winner = null;
        looser = null;
        
		if (bot1 == null) {
	    	SharedPreferences cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE);
	    	String last = cookies.getString("instance", null);
			if (last != null && !last.isEmpty()) {
				InstanceConfig config = new InstanceConfig();
				config.name = last;
				HttpFetchWarAction action = new HttpFetchWarAction(this, config);
				action.execute();
			}
		}
	}
	
	public void resetView() {
		List<WebMediumConfig> instances = new ArrayList<WebMediumConfig>();
		if (bot1 != null) {
			instances.add(bot1);
		}
		ListView list = (ListView) findViewById(R.id.bot1List);
		list.setAdapter(new ImageListAdapter(this, R.layout.image_list, instances));
		instances = new ArrayList<WebMediumConfig>();
		if (bot2 != null) {
			instances.add(bot2);
		}
		list = (ListView) findViewById(R.id.bot2List);
		list.setAdapter(new ImageListAdapter(this, R.layout.image_list, instances));
	}
	
	/**
	 * Start a new war.
	 */
	public void war(View view) {
        if (bot1 == null || bot2 == null) {
			MainActivity.showMessage("Please select two bots to start a war", this);
			return;
        }
        topic = ((EditText)findViewById(R.id.topicText)).getText().toString();
        if (topic == null || topic.isEmpty()) {
			MainActivity.showMessage("Please enter a topic", this);
			return;
        }
        MainActivity.instance = bot1;
        MainActivity.connection.setUser(null);
		Intent intent = new Intent(this, WarActivity.class);
        startActivity(intent);
    }

	public void browseBot1(View view) {
		BrowseConfig config = new BrowseConfig();
		config.type = "Bot";
		config.sort = "rank";
		
		MainActivity.browsing = true;
		this.browsing = 0;
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}

	public void browseBot2(View view) {
		BrowseConfig config = new BrowseConfig();
		config.type = "Bot";
		config.sort = "rank";

		MainActivity.browsing = true;
		MainActivity.instance = null;
		this.browsing = 1;
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}
	
	@Override
	public void onResume() {
		MainActivity.connection.setUser(MainActivity.user);
		if (MainActivity.browsing) {
			if ((MainActivity.instance instanceof InstanceConfig)) {
				if (this.browsing == 0) {
					bot1 = (InstanceConfig)MainActivity.instance;
		        	SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
		        	cookies.putString(bot1.getType(), bot1.name);
		        	cookies.commit();
				} else {
					bot2 = (InstanceConfig)MainActivity.instance;
				}
			}
		} else if (winner != null) {
			String text = "Last war " + winner.name + " beat " + looser.name + ".";
			((TextView)findViewById(R.id.winner)).setText(text);
			ChatWarConfig config = new ChatWarConfig();
			config.winner = winner.id;
			config.looser = looser.id;
			config.topic = topic;
			config.secret = String.valueOf(SECRET + MainActivity.user.user.length());
			HttpChatWarAction action = new HttpChatWarAction(this, config);
			action.execute();
		}
		MainActivity.browsing = false;
		resetView();
		super.onResume();
	}
}

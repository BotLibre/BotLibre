/******************************************************************************
 *
 *  Copyright 2016 Paphus Solutions Inc.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.botlibre.sdk.activity.actions.HttpDeleteBotScriptAction;
import org.botlibre.sdk.activity.actions.HttpDownBotScriptAction;
import org.botlibre.sdk.activity.actions.HttpGetBotScriptsAction;
import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.activity.actions.HttpUpBotScriptAction;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.ScriptConfig;
import org.botlibre.sdk.config.ScriptSourceConfig;

import org.botlibre.sdk.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

/**
 * Activity for viewing, editing and importing Bot scripts
 */

public class BotScriptsActivity extends LibreActivity {
	protected List<ScriptConfig> scripts = new ArrayList<ScriptConfig>();
	protected InstanceConfig instance;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bot_scripts);

		this.instance = (InstanceConfig)MainActivity.instance;

		ListView list = (ListView) findViewById(R.id.botScriptList);
		GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTapEvent(MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					ListView list = (ListView) findViewById(R.id.botScriptList);
			        int index = list.getCheckedItemPosition();
			        if (index < 0) {
						return false;
			        } else {
			        	editBotScript();
			        }
					return true;
				}
				return false;
			}
		};
		final GestureDetector listDetector = new GestureDetector(this, listener);
		list.setOnTouchListener(new View.OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return listDetector.onTouchEvent(event);
			}
		});

		HttpGetBotScriptsAction action = new HttpGetBotScriptsAction(this, instance);
		action.execute();

	}
	
	@Override
	public void onResume() {
		super.onResume();
		MainActivity.browsing = false;
		MainActivity.importingBotScript = false;
		resetScripts();
	}

	public void resetView() {

		TextView title = (TextView) findViewById(R.id.title);
		//Could set title to bot's name, see AvatarEditorActivity

		ListView scriptList = (ListView)findViewById(R.id.botScriptList);
		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, this.scripts.toArray());
		scriptList.setAdapter(adapter);
	}
	
	public void resetScripts() {
		HttpGetBotScriptsAction action = new HttpGetBotScriptsAction(this, instance);
		action.execute();
	}

	public List<ScriptConfig> getScriptList() {
		return scripts;
	}

	public void setScriptList(List<ScriptConfig> scripts) {
		this.scripts = scripts;
	}

	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.layout.menu_bot_scripts, popup.getMenu());
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				return onOptionsItemSelected(item);
			}
		});
		popup.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.layout.menu_bot_scripts, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menuUp:
			upScript();
			return true;
		case R.id.menuDown:
			downScript();
			return true;
		case R.id.menuAdd:
			addBotScript();
			return true;
		case R.id.menuEdit:
			editBotScript();
			return true;
		case R.id.menuImport:
			importBotScript();
			return true;
		case R.id.menuDelete:
			deleteBotScript();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void upScript(View view) {
		upScript();
	}

	public void upScript() {
		ListView list = (ListView)findViewById(R.id.botScriptList);
		int index = list.getCheckedItemPosition();
		if (index < 0) {
			System.out.println("The other index: " + index);
			MainActivity.showMessage("Select a script to move up in precedence", this);
			return;
		}

		if (index != 0) {
			ScriptConfig script = this.scripts.get(index);
			
			
			ScriptSourceConfig botScript = new ScriptSourceConfig();
			InstanceConfig bot = this.instance;
			botScript.id = script.id;
			botScript.instance = bot.id;
			
			System.out.println("Bot id: " + bot.id + "Bot instance: " + bot.instance 
					+ "ScriptConfig id: " + script.id + "ScriptConfig instance: " + script.instance);
			//This code should change the index of the thing?
			Collections.swap(scripts, index, index-1);

			HttpUpBotScriptAction action = new HttpUpBotScriptAction(this, botScript);
			action.execute();

		}

	}

	public void downScript(View view) {
		downScript();
	}

	public void downScript() {
		ListView list = (ListView)findViewById(R.id.botScriptList);
		int index = list.getCheckedItemPosition();
		if (index < 0) {
			MainActivity.showMessage("Select a script to move down in precedence", this);
			return;
		}

		if (index != scripts.size()-1) {
			ScriptConfig script = this.scripts.get(index);
			ScriptSourceConfig botScript = new ScriptSourceConfig();
			InstanceConfig bot = this.instance;
			botScript.id = script.id;
			botScript.instance = bot.id;
			
			Collections.swap(scripts, index, index+1);

			HttpDownBotScriptAction action = new HttpDownBotScriptAction(this, botScript);
			action.execute();
		}
	}

	public void editBotScript(View view) {
		editBotScript();
	}

	public void editBotScript() {
		
		ListView list = (ListView)findViewById(R.id.botScriptList);
		int index = list.getCheckedItemPosition();
		
		if (index < 0) {
			MainActivity.showMessage("Select a script to edit", this);
			return;
		}
		
		ScriptConfig script = this.scripts.get(index);
		ScriptSourceConfig botScript = new ScriptSourceConfig();
		InstanceConfig bot = this.instance;
		botScript.id = script.id;
		botScript.instance = bot.id;
		
		MainActivity.script = botScript;
		
		Intent intent = new Intent(this, BotScriptEditorActivity.class);
		startActivity(intent);
	}

	public void addBotScript(View view) {
		addBotScript();
	}

	public void addBotScript() {
		MainActivity.script = null;
		Intent intent = new Intent(this, BotScriptEditorActivity.class);
		startActivity(intent);
	}
	
	public void importBotScript(View view){
		importBotScript();
	}
	
	public void importBotScript() {
		MainActivity.browsing = true;
		MainActivity.importingBotScript = true;	
		
		BrowseConfig config = new BrowseConfig();
		config.type = "Script";
		config.typeFilter = "Featured";

		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}

	public void deleteBotScript(View view) {
		deleteBotScript();
	}

	public void deleteBotScript() {
		ListView list = (ListView)findViewById(R.id.botScriptList);
		int index = list.getCheckedItemPosition();
		
		if (index < 0) {
			MainActivity.showMessage("Select a script to delete", this);
			return;
		}
		
		ScriptConfig script = this.scripts.get(index);
		ScriptSourceConfig botScript = new ScriptSourceConfig();
		InstanceConfig bot = this.instance;
		botScript.id = script.id;
		botScript.instance = bot.id;
		
		scripts.remove(index);
		
		HttpDeleteBotScriptAction action = new HttpDeleteBotScriptAction(this, botScript);
		action.execute();
		
		
	}



}
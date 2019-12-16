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

import org.botlibre.sdk.activity.actions.HttpGetBotScriptSourceAction;
import org.botlibre.sdk.activity.actions.HttpImportBotScriptAction;
import org.botlibre.sdk.activity.actions.HttpSaveBotScriptSourceAction;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.ScriptConfig;
import org.botlibre.sdk.config.ScriptSourceConfig;
import org.botlibre.sdk.util.Utils;

import org.botlibre.offline.pizzabot.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/*
 * This class allows the user to edit/create new/save bot the script
 */
public class BotScriptEditorActivity extends LibreActivity{
	protected ScriptConfig config;
	protected ScriptSourceConfig source;
	protected InstanceConfig instance;

	public String getType() {
		return "Script";
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bot_edit_script_source);

		Intent intent = getIntent();
		String scriptConfig = intent.getStringExtra("scriptID");


		this.instance = (InstanceConfig)MainActivity.instance;
		this.source = (ScriptSourceConfig)MainActivity.script;
		this.config = new ScriptConfig();
		this.config.id = scriptConfig;

		if (MainActivity.importingBotScript) {
			HttpImportBotScriptAction action = new HttpImportBotScriptAction(this, config);
			action.execute();
		} else if (MainActivity.script != null) {

			HttpGetBotScriptSourceAction action = new HttpGetBotScriptSourceAction(this, source);
			action.execute();
		} else {
			EditText editScript = (EditText)findViewById(R.id.scriptSource);
			editScript.setText("state NewState {\n\tcase input goto sentenceState for each #word of sentence;\n\nstate sentenceState {\n\t}\n}");
		}

	}

	public void resetView(){
		this.source = (ScriptSourceConfig)MainActivity.script;
		this.instance = (InstanceConfig)MainActivity.instance;

		TextView title = (TextView)findViewById(R.id.title);
		title.setText(Utils.stripTags(this.instance.name));

		String script = source.source;

		EditText editScript = (EditText)findViewById(R.id.scriptSource);
		Button saveButton = (Button)findViewById(R.id.saveScriptButton);

		if (script != null && !script.equals("")) {
			editScript.setText(script);
		}
		else {
			editScript.setText("");
		}

		boolean isAdmin = (MainActivity.user != null) && instance.isAdmin;
		if (!isAdmin || instance.isExternal) {
			editScript.setFocusable(false);
			saveButton.setEnabled(false);
			saveButton.setVisibility(View.INVISIBLE);
		} else {
			editScript.setFocusableInTouchMode(true);
			saveButton.setEnabled(true);
		}
	}


	public void saveScript(View view) {
		ScriptSourceConfig config = new ScriptSourceConfig();
		config.instance = instance.id;
		if (source != null) {
			config.id = source.id;
		}

		EditText editScript = (EditText)findViewById(R.id.scriptSource);
		config.source = editScript.getText().toString();

		HttpSaveBotScriptSourceAction action = new HttpSaveBotScriptSourceAction(this, config);
		action.execute();

	}
	
	public void didCompile() {
		MainActivity.importingBotScript = false;
	}
	

}

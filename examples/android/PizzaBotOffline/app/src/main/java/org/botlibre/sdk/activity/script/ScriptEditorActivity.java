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
package org.botlibre.sdk.activity.script;

import org.botlibre.sdk.activity.LibreActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpGetScriptSourceAction;
import org.botlibre.sdk.activity.actions.HttpSaveScriptSourceAction;
import org.botlibre.sdk.config.ScriptConfig;
import org.botlibre.sdk.config.ScriptSourceConfig;
import org.botlibre.sdk.util.Utils;

import org.botlibre.offline.pizzabot.R;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/*
 * This class allows the user to edit/create new/save the script
 */
public class ScriptEditorActivity extends LibreActivity{
	protected ScriptConfig instance;
	protected ScriptSourceConfig source;
	
	public String getType() {
		return "Script";
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_script_source);
		
		this.instance = (ScriptConfig)MainActivity.instance;
		
		HttpGetScriptSourceAction action = new HttpGetScriptSourceAction(this, instance);
		action.execute();
		
		
	}
	
	public void resetView(){
		this.source = (ScriptSourceConfig)MainActivity.script;
		
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
			saveButton.setVisibility(View.GONE);
		} else {
			editScript.setFocusableInTouchMode(true);
			saveButton.setEnabled(true);
		}
	}
	
	
	
	public void saveScript(View view) {
		ScriptSourceConfig config = new ScriptSourceConfig();
		config.instance = MainActivity.instance.id;
		
		EditText editScript = (EditText)findViewById(R.id.scriptSource);
		config.source = editScript.getText().toString();
	
		HttpSaveScriptSourceAction action = new HttpSaveScriptSourceAction(this, config);
		action.execute();
		
		
	}

}

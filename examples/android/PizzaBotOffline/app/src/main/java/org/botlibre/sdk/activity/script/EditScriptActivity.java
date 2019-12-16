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

import java.util.Arrays;

import org.botlibre.sdk.activity.EditWebMediumActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpUpdateAction;
import org.botlibre.sdk.config.ScriptConfig;

import org.botlibre.offline.pizzabot.R;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Activity for editing a script's details.
 */
public class EditScriptActivity extends EditWebMediumActivity{

	@Override
	public String getType() {
		return "Script";
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_script);
		
		ScriptConfig instance = (ScriptConfig)MainActivity.instance;
		
		resetView();
		
		Spinner spin = (Spinner) findViewById(R.id.scriptLanguageSpin);
		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.scriptLanguages);
		spin.setAdapter(adapter);
		spin.setSelection(Arrays.asList(MainActivity.scriptLanguages).indexOf(instance.language));
	}
	
	public void save(View view){
		ScriptConfig instance = new ScriptConfig();
		saveProperties(instance);
		
		Spinner spin = (Spinner) findViewById(R.id.scriptLanguageSpin);
		instance.language = (String)spin.getSelectedItem();
		
		HttpAction action= new HttpUpdateAction(this, instance);
		action.execute();
	}

}

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

import org.botlibre.sdk.activity.BrowseActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchAction;
import org.botlibre.sdk.activity.actions.HttpImportBotLogAction;
import org.botlibre.sdk.activity.actions.HttpImportBotScriptAction;
import org.botlibre.sdk.config.ScriptConfig;

import org.botlibre.sdk.R;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

/*
 * Activity to choose a script from the search results
 */
public class BrowseScriptActivity extends BrowseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public String getType() {
		return "Script";
	} 



	public void selectInstance(View view){
		ListView list = (ListView) findViewById(R.id.instancesList);
		int index = list.getCheckedItemPosition();
		this.instance = this.instances.get(index); 
		ScriptConfig config = new ScriptConfig();
		//setting the scriptconfig's instance to be the bot id
		if (MainActivity.instance != null) {
			config.instance = MainActivity.instance.id;
		}
		//setting the scriptconfig id to be the selected script
		config.id = this.instance.id;

		if (MainActivity.importingBotScript) {
			HttpImportBotScriptAction action = new HttpImportBotScriptAction(this, config);
			action.execute();
		} else if (MainActivity.importingBotLog) {
			HttpImportBotLogAction action = new HttpImportBotLogAction(this, config);
			action.execute();
		} else {
			HttpAction action = new HttpFetchAction(this, config);
			action.execute();
		}

	}


}

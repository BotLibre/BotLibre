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

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpUpdateAction;
import org.botlibre.sdk.config.InstanceConfig;

/**
 * Activity for editing a bot's details.
 */
public class EditBotActivity extends EditWebMediumActivity {

	@Override
	public String getType() {
		return "Bot";
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bot);

		resetView();
		
		InstanceConfig instance = (InstanceConfig)MainActivity.instance;
		
		CheckBox checkbox = (CheckBox) findViewById(R.id.forkingCheckBox);
		checkbox.setChecked(instance.allowForking);
	}

    public void save(View view) {
    	InstanceConfig instance = new InstanceConfig();   	
    	saveProperties(instance);
    	
		CheckBox checkbox = (CheckBox) findViewById(R.id.forkingCheckBox);
		instance.allowForking = checkbox.isChecked();
        
        HttpAction action = new HttpUpdateAction(this, instance);
        action.execute();
    }
}

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


import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.botlibre.offline.R;
import org.botlibre.sdk.activity.EditWebMediumActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpUpdateAction;
import org.botlibre.sdk.config.ForumConfig;

/**
 * Activity for editing a forum's details.
 */
public class EditForumActivity extends EditWebMediumActivity {
		
	@Override
	public String getType() {
		return "Forum";
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_forum);

		ForumConfig instance = (ForumConfig)MainActivity.instance;
		
		resetView();

		Spinner spin = (Spinner) findViewById(R.id.postModeSpin);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.accessModes);
		spin.setAdapter(adapter);
		spin.setSelection(Arrays.asList(MainActivity.accessModes).indexOf(instance.postAccessMode));

		spin = (Spinner) findViewById(R.id.replyModeSpin);
		adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.accessModes);
		spin.setAdapter(adapter);
		spin.setSelection(Arrays.asList(MainActivity.accessModes).indexOf(instance.replyAccessMode));
	}
	
    public void save(View view) {
    	ForumConfig instance = new ForumConfig();
    	saveProperties(instance);

    	Spinner spin = (Spinner) findViewById(R.id.postModeSpin);
    	instance.postAccessMode = (String)spin.getSelectedItem();
    	
    	spin = (Spinner) findViewById(R.id.replyModeSpin);
    	instance.replyAccessMode = (String)spin.getSelectedItem();
        
    	HttpAction action = new HttpUpdateAction(this, instance);
        action.execute();
    }
}

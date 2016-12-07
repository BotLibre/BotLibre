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

import org.botlibre.sdk.activity.actions.HttpCreateUserAction;
import org.botlibre.sdk.config.UserConfig;

import org.botlibre.sdk.R;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Activity for creating a new user.
 */
public class CreateUserActivity extends LibreActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);		
	}
    
    /**
     * Create the user.
     */
    public void create(View view) {
    	UserConfig config = new UserConfig();
    	
        EditText text = (EditText) findViewById(R.id.userText);
        config.user = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.passwordText);
        config.password = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.hintText);
        config.hint = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.nameText);
        config.name = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.emailText);
        config.email = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.websiteText);
        config.website = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.bioText);
        config.bio = text.getText().toString().trim();
		CheckBox checkbox = (CheckBox)findViewById(R.id.showNameCheckBox);
		config.showName = checkbox.isChecked();
        
    	HttpCreateUserAction action = new HttpCreateUserAction(this, config);
    	action.execute();
    }
    
    /**
     * Cancel
     */
    public void cancel(View view) {        
    	finish();
    }
}

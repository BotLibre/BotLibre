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

import org.botlibre.sdk.R;

import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpUpdateUserAction;
import org.botlibre.sdk.config.UserConfig;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Activity for editing a user's details.
 */
public class EditUserActivity extends CreateUserActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        UserConfig user = MainActivity.user;
        
        setTitle("Edit: " + user.user);
        
        TextView text = (EditText) findViewById(R.id.hintText);
        text.setText(user.hint);
        text = (EditText) findViewById(R.id.nameText);
        text.setText(user.name);
        text = (EditText) findViewById(R.id.emailText);
        text.setText(user.email);
        text = (EditText) findViewById(R.id.websiteText);
        text.setText(user.website);
        text = (EditText) findViewById(R.id.bioText);
        text.setText(user.bio);
		CheckBox checkbox = (CheckBox)findViewById(R.id.showNameCheckBox);
		checkbox.setChecked(user.showName);

        text = (TextView) findViewById(R.id.title);
        text.setText(user.user);
        
        HttpGetImageAction.fetchImage(this, MainActivity.viewUser.avatar, (ImageView)findViewById(R.id.icon));
	}
    
    /**
     * Create the user.
     */
    public void save(View view) {
    	UserConfig config = new UserConfig();
    	config.user = MainActivity.connection.getUser().user;
    	
    	EditText text = (EditText) findViewById(R.id.passwordText);
    	config.password = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.newPasswordText);
        config.newPassword = text.getText().toString().trim();
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
        
    	HttpUpdateUserAction action = new HttpUpdateUserAction(this, config);
    	action.execute();
    }
    
    /**
     * Cancel
     */
    public void cancel(View view) {        
    	finish();
    }
}

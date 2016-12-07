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

import java.util.ArrayList;
import java.util.List;

import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpGetAdminsAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetUsersAction;
import org.botlibre.sdk.activity.actions.HttpUserAdminAction;
import org.botlibre.sdk.config.UserAdminConfig;
import org.botlibre.sdk.config.WebMediumConfig;

import org.botlibre.sdk.R;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Generic activity for administering a content's users.
 */
public abstract class WebMediumUsersActivity extends LibreActivity {
	
	public List<String> users = new ArrayList<String>();
	public List<String> admins = new ArrayList<String>();
	
	public abstract String getType();
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        
        HttpGetImageAction.fetchImage(this, MainActivity.instance.avatar, findViewById(R.id.icon));

        WebMediumConfig instance = MainActivity.instance.credentials();
        
        HttpAction action = new HttpGetUsersAction(this, instance);
    	action.execute();
        action = new HttpGetAdminsAction(this, instance);
    	action.execute();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetView() {
		ListView list = (ListView) findViewById(R.id.usersList);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, this.users.toArray());
		list.setAdapter(adapter);
		
		list = (ListView) findViewById(R.id.adminList);
		adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, this.admins.toArray());
		list.setAdapter(adapter);
	}

	public void addUser(View view) {
		UserAdminConfig config = new UserAdminConfig();
        config.instance = MainActivity.instance.id;
        config.type = getType();
        
        config.operation = "AddUser";
        EditText text = (EditText) findViewById(R.id.userText);
        config.operationUser = text.getText().toString().trim();
		if (config.operationUser.length() == 0) {
			MainActivity.error("Enter user to add", null, this);
			return;
		}
        
		HttpUserAdminAction action = new HttpUserAdminAction(this, config);
		action.execute();
	}

	public void removeUser(View view) {
		UserAdminConfig config = new UserAdminConfig();
        config.instance = MainActivity.instance.id;
        config.type = getType();
        
        config.operation = "RemoveUser";
        ListView list = (ListView) findViewById(R.id.usersList);
        int index = list.getCheckedItemPosition();
		if (index < 0) {
			MainActivity.error("Select user to remove", null, this);
			return;
		}
        config.operationUser = (String)this.users.get(index);
        
		HttpUserAdminAction action = new HttpUserAdminAction(this, config);
		action.execute();
	}

	public void addAdmin(View view) {
		UserAdminConfig config = new UserAdminConfig();
        config.instance = MainActivity.instance.id;
        config.type = getType();
        
        config.operation = "AddAdmin";
        EditText text = (EditText) findViewById(R.id.adminText);
        config.operationUser = text.getText().toString().trim();
		if (config.operationUser.length() == 0) {
			MainActivity.error("Enter admin to add", null, this);
			return;
		}

		HttpUserAdminAction action = new HttpUserAdminAction(this, config);
		action.execute();
	}

	public void removeAdmin(View view) {
		UserAdminConfig config = new UserAdminConfig();
        config.instance = MainActivity.instance.id;
        config.type = getType();
        
        config.operation = "RemoveAdmin";
        
        ListView list = (ListView) findViewById(R.id.adminList);
        int index = list.getCheckedItemPosition();
		if (index < 0) {
			MainActivity.error("Select admin to remove", null, this);
			return;
		}
        config.operationUser = (String)this.admins.get(index);

		HttpUserAdminAction action = new HttpUserAdminAction(this, config);
		action.execute();
	}
}

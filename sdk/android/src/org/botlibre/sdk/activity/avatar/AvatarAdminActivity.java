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

package org.botlibre.sdk.activity.avatar;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.WebMediumAdminActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpGetAvatarMediaAction;
import org.botlibre.sdk.config.AvatarConfig;

import org.botlibre.sdk.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Activity for a avatar's admin functions.
 */
public class AvatarAdminActivity extends WebMediumAdminActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_avatar);
		
        resetView();        
	}

	public void adminUsers(View view) {
        Intent intent = new Intent(this, AvatarUsersActivity.class);		
        startActivity(intent);
	}

	public void editInstance(View view) {
        Intent intent = new Intent(this, EditAvatarActivity.class);		
        startActivity(intent);
	}
	
	public void openEditor(View view) {
		HttpAction action = new HttpGetAvatarMediaAction(this, (AvatarConfig)MainActivity.instance.credentials(), false);
		action.execute();
	}
	
}

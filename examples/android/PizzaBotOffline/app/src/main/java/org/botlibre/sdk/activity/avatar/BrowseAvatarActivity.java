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

import org.botlibre.sdk.activity.BrowseActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchAction;
import org.botlibre.sdk.config.AvatarConfig;

import org.botlibre.offline.pizzabot.R;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

/**
 * Activity for choosing an avatar from the search results.
 */
public class BrowseAvatarActivity extends BrowseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public String getType() {
		return "Avatar";
	}
	
	public void selectInstance(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        this.instance = this.instances.get(index);
		if (MainActivity.browsing) {
			MainActivity.instance = this.instance;
			finish();
			return;
		}
        AvatarConfig config = new AvatarConfig();
        config.id = this.instance.id;
		
        HttpAction action = new HttpFetchAction(this, config);
    	action.execute();
	}
}

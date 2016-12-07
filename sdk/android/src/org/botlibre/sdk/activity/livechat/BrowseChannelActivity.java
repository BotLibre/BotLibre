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

package org.botlibre.sdk.activity.livechat;

import org.botlibre.sdk.activity.BrowseActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchAction;
import org.botlibre.sdk.config.ChannelConfig;

import org.botlibre.sdk.R;

import android.view.View;
import android.widget.ListView;

/**
 * Activity for choosing a channel from the search results.
 */
public class BrowseChannelActivity extends BrowseActivity {

	public void selectInstance(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select a channel", this);
        	return;
        }
        this.instance = instances.get(index);
        ChannelConfig config = new ChannelConfig();
        config.id = this.instance.id;
        config.name = this.instance.name;
		
        HttpAction action = new HttpFetchAction(this, config);
    	action.execute();
	}

	@Override
	public String getType() {
		return "Channel";
	}

	public void chat(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select a channel", this);
        	return;
        }
        this.instance = instances.get(index);
        ChannelConfig config = new ChannelConfig();
        config.id = this.instance.id;
        config.name = this.instance.name;
		
        HttpAction action = new HttpFetchAction(this, config, true);
    	action.execute();
	}
}

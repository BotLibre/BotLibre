/******************************************************************************
 *
 *  Copyright 2017 Paphus Solutions Inc.
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

import java.util.ArrayList;

import org.botlibre.sdk.activity.ChatActivity;
import org.botlibre.sdk.activity.CustomListViewAdapter;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.OfflineTemplateConfig;
import org.botlibre.offline.pizzabot.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity used for selecting an offline avatar.
 */
public class AvatarSelection extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_view);
		final int [] imges = {};
		final String [] names = {};
		((TextView) findViewById(R.id.theTitle)).setText("Select Avatar");
		final ListView listView = (ListView) (findViewById(R.id.theListView));
		ArrayList<OfflineTemplateConfig> items = new ArrayList<OfflineTemplateConfig>();
		for (int i = 0; i < names.length; i++) {
			items.add(new OfflineTemplateConfig(imges[i],names[i],null,null));
		}
		CustomListViewAdapter adapter = new CustomListViewAdapter(this, R.layout.list_item_imager, items);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				OfflineTemplateConfig template = (OfflineTemplateConfig) (listView.getItemAtPosition(position));
				saveSelectedAvatar(template.getTitle());
				MainActivity.readZipAvatars(AvatarSelection.this, MainActivity.nameOfAvatar);
				finish();
			}});
	}

	public static void saveSelectedAvatar(String nameOfAvatar) {
		MainActivity.nameOfAvatar = nameOfAvatar;
		SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
		cookies.putString("nameOfAvatar", nameOfAvatar);
		cookies.commit();
	}

}

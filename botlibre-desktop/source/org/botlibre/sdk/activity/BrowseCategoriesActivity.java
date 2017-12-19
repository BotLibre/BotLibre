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

import java.util.List;

import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ContentConfig;

import org.botlibre.sdk.R;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Activity for browsing a category.
 */
public class BrowseCategoriesActivity extends BrowseActivity {
	public static String type = MainActivity.defaultType;	
	public static List<ContentConfig> instances;
	
	public ContentConfig instance;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.superOnCreate(savedInstanceState);
		setContentView(R.layout.activity_browse);
		
		TextView title = (TextView) findViewById(R.id.title);
		title.setText("Browse Categories");

		ListView list = (ListView) findViewById(R.id.instancesList);
		list.setAdapter(new CategoryListAdapter(this, R.layout.image_list, instances));

		
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ListView list = (ListView) findViewById(R.id.instancesList);
				selectInstance(list);
			}
		});
		

		findViewById(R.id.nextButton).setVisibility(View.GONE);
		findViewById(R.id.previousButton).setVisibility(View.GONE);
	}
	
	@Override
	public void onResume() {
		ListView list = (ListView) findViewById(R.id.instancesList);
		list.setAdapter(new CategoryListAdapter(this, R.layout.image_list, instances));
		
		super.superOnResume();
	}

	@Override
	public String getType() {
		return type;
	}
	
	@Override
	public void selectInstance(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select a category", this);
        	return;
        }
        this.instance = instances.get(index);
        
		BrowseConfig config = new BrowseConfig();
		config.typeFilter = "Public";
		config.category = this.instance.name;
		config.type = getType();
		
		HttpAction action = new HttpGetInstancesAction(this, config, MainActivity.browsing);
		action.execute();
	}

	@Override
	public void chat(View view) {
		selectInstance(view);
	}
}

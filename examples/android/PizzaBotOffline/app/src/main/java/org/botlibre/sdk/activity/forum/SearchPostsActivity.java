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

import org.botlibre.sdk.activity.LibreActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpGetCategoriesAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetPostsAction;
import org.botlibre.sdk.activity.actions.HttpGetTagsAction;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ForumConfig;
import org.botlibre.sdk.util.Utils;

import org.botlibre.offline.pizzabot.R;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;


/**
 * Search activity for searching for a forum post.
 */
public class SearchPostsActivity extends LibreActivity {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MainActivity.searchingPosts = true;
		setContentView(R.layout.activity_search_posts);

		if (MainActivity.instance instanceof ForumConfig) {
	        setTitle(Utils.stripTags(MainActivity.instance.name));
			((TextView) findViewById(R.id.title)).setText(Utils.stripTags(MainActivity.instance.name));
	        HttpGetImageAction.fetchImage(this, MainActivity.instance.avatar, findViewById(R.id.icon));
			findViewById(R.id.categoriesText).setVisibility(View.GONE);
		} else {
			((ImageView)findViewById(R.id.icon)).setImageResource(R.drawable.icon);
		}

		CheckBox checkbox = (CheckBox)findViewById(R.id.imagesCheckBox);
		checkbox.setChecked(MainActivity.showImages);
    	
		Spinner sortSpin = (Spinner) findViewById(R.id.sortSpin);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, new String[]{
    				"date",
        			"name",
        			"thumbs up",
        			"thumbs down",
        			"stars",
        			"views",
        			"views today",
        			"views this week",
        			"views this month"
        });
		sortSpin.setAdapter(adapter);

    	HttpAction action = new HttpGetTagsAction(this, "Post");
    	action.execute();
    	
    	action = new HttpGetCategoriesAction(this, "Forum");
    	action.execute();
	}
	
	public void onDestroy() {
		super.onDestroy();
		MainActivity.searchingPosts = false;
	}
	
	public void browse(View view) {
		BrowseConfig config = new BrowseConfig();
		
		config.typeFilter = "Public";
		RadioButton radio = (RadioButton)findViewById(R.id.personalRadio);
		if (radio.isChecked()) {
			config.typeFilter = "Personal";
		}
		Spinner sortSpin = (Spinner)findViewById(R.id.sortSpin);
		config.sort = (String)sortSpin.getSelectedItem();
		AutoCompleteTextView tagText = (AutoCompleteTextView)findViewById(R.id.tagsText);
		config.tag = (String)tagText.getText().toString();
		AutoCompleteTextView categoryText = (AutoCompleteTextView)findViewById(R.id.categoriesText);
		config.category = (String)categoryText.getText().toString();
		EditText filterEdit = (EditText)findViewById(R.id.filterText);
		config.filter = filterEdit.getText().toString();
		CheckBox checkbox = (CheckBox)findViewById(R.id.imagesCheckBox);
		MainActivity.showImages = checkbox.isChecked();

		config.type = "Post";
		if (MainActivity.instance != null) {
			config.instance = MainActivity.instance.id;
		}
		
		HttpAction action = new HttpGetPostsAction(this, config);
		action.execute();
	}
}

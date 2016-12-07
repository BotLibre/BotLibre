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

import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpBrowseCategoriesAction;
import org.botlibre.sdk.activity.actions.HttpGetCategoriesAction;
import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.activity.actions.HttpGetTagsAction;
import org.botlibre.sdk.config.BrowseConfig;

import org.botlibre.sdk.R;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Search activity for searching instances.
 */
public abstract class SearchActivity extends LibreActivity {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		CheckBox checkbox = (CheckBox)findViewById(R.id.imagesCheckBox);
		checkbox.setChecked(MainActivity.showImages);
		
		TextView title = (TextView) findViewById(R.id.title);
		title.setText("Search " + getType() + "s");
    	
		Spinner sortSpin = (Spinner) findViewById(R.id.sortSpin);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, new String[]{
        			"connects",
        			"connects today",
        			"connects this week",
        			"connects this month",
        			"last connect",
        			"name",
        			"date",
            		"thumbs up",
            		"thumbs down",
            		"stars"
        });
		sortSpin.setAdapter(adapter);
		
		Spinner restrictSpin = (Spinner) findViewById(R.id.restrictSpin);
		adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, new String[]{
                	"",
        			"has website",
        			"has subdomain",
        			"external link",
        			"Diamond",
        			"Platinum",
        			"Gold",
        			"Bronze"
        		});
		restrictSpin.setAdapter(adapter);

    	HttpAction action = new HttpGetTagsAction(this, getType());
    	action.execute();
    	
    	action = new HttpGetCategoriesAction(this, getType());
    	action.execute();
		MainActivity.searching = !MainActivity.browsing;
	}
	
	public void onDestroy() {
		super.onDestroy();
		MainActivity.searching = false;
	}
	
	public void browse(View view) {
		BrowseConfig config = new BrowseConfig();
		
		config.typeFilter = "Public";
		RadioButton radio = (RadioButton)findViewById(R.id.privateRadio);
		if (radio.isChecked()) {
			config.typeFilter = "Private";
		}
		radio = (RadioButton)findViewById(R.id.personalRadio);
		if (radio.isChecked()) {
			config.typeFilter = "Personal";
		}
		Spinner sortSpin = (Spinner)findViewById(R.id.sortSpin);
		config.sort = (String)sortSpin.getSelectedItem();
		Spinner restrictSpin = (Spinner)findViewById(R.id.restrictSpin);
		config.restrict = (String)restrictSpin.getSelectedItem();
		AutoCompleteTextView tagText = (AutoCompleteTextView)findViewById(R.id.tagsText);
		config.tag = (String)tagText.getText().toString();
		AutoCompleteTextView categoryText = (AutoCompleteTextView)findViewById(R.id.categoriesText);
		config.category = (String)categoryText.getText().toString();
		EditText filterEdit = (EditText)findViewById(R.id.filterText);
		config.filter = filterEdit.getText().toString();
		CheckBox checkbox = (CheckBox)findViewById(R.id.imagesCheckBox);
		MainActivity.showImages = checkbox.isChecked();
		
		config.type = getType();
		
		HttpAction action = new HttpGetInstancesAction(this, config, MainActivity.browsing);
		action.execute();
	}
	
	public abstract String getType();


	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.layout.menu_search, popup.getMenu());
	    onPrepareOptionsMenu(popup.getMenu());
	    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
	        @Override
	        public boolean onMenuItemClick(MenuItem item) {
	            return onOptionsItemSelected(item);
	        }
	    });
	    popup.show();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.menu_search, menu);
        return true;
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
	    case R.id.menuMyBots:
	    	browseMyBots();
	        return true;
	    case R.id.menuFeatured:
	    	browseFeatured();
	        return true;
        case R.id.menuCategories:
        	browseCategories();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.menuMyBots);
        if (MainActivity.user == null) {
        	item.setEnabled(false);
        }
        item.setTitle("My " + getType() + "s");
	    return true;
	}

	public void browseMyBots() {
		BrowseConfig config = new BrowseConfig();
		config.type = getType();
		config.typeFilter = "Personal";
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config, MainActivity.browsing);
		action.execute();
	}

	public void browseFeatured() {
		BrowseConfig config = new BrowseConfig();
		config.type = getType();
		config.typeFilter = "Featured";
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config, MainActivity.browsing);
		action.execute();
	}

	public void browseCategories() {		
		HttpAction action = new HttpBrowseCategoriesAction(this, getType(), MainActivity.browsing);
		action.execute();
	}
}

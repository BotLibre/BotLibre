/******************************************************************************
 *
 *  Copyright 2014-2017 Paphus Solutions Inc.
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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Search activity for searching instances.
 */
public abstract class SearchActivity extends LibreActivity {

		final static int TAGTEXT = 3,CATTEXT = 4;
		
		private AutoCompleteTextView tagsText,categoriesText; 
		private ImageButton btnTag, btnCat;

		private Spinner sortSpin;
		private boolean sortSpinOption = true;
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

        btnCat = (ImageButton)findViewById(R.id.btnCat);
        btnTag = (ImageButton)findViewById(R.id.btnTag);
        tagsText = (AutoCompleteTextView)findViewById(R.id.tagsText);
        categoriesText = (AutoCompleteTextView)findViewById(R.id.categoriesText);
        
		
		CheckBox checkbox = (CheckBox)findViewById(R.id.imagesCheckBox);
		checkbox.setChecked(MainActivity.showImages);
		
		TextView title = (TextView) findViewById(R.id.title);
		title.setText("Search " + getType() + "s");
    	
		sortSpin = (Spinner) findViewById(R.id.sortSpin);
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

		Spinner contentRating = (Spinner) findViewById(R.id.ContentRatingSpin);
		adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.contentRatings);
		contentRating.setAdapter(adapter);
		if(MainActivity.contentRating != null){
			for (int i = 0; i < adapter.getCount(); i++) {
				if(contentRating.getItemAtPosition(i).equals(MainActivity.contentRating)){
					contentRating.setSelection(i);
				}
			}
		}
		
        btnTag.setOnClickListener(new View.OnClickListener() {
        	
			@Override
			public void onClick(View v) {
				Intent i = new Intent(SearchActivity.this,ListTagsView.class);
				i.putExtra("type", getType());
				startActivityForResult(i,TAGTEXT);
			}
		});
        btnCat.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(SearchActivity.this,ListCategoriesView.class);
				i.putExtra("type", getType());
				startActivityForResult(i,CATTEXT);
			}
		});
		
		
    	HttpAction action = new HttpGetTagsAction(this, getType());
    	action.execute();
    	
    	action = new HttpGetCategoriesAction(this, getType());
    	action.execute();
		MainActivity.searching = !MainActivity.browsing;
	}
	@Override
	protected void onResume() {
		if(sortSpinOption){
			sortSpin.setSelection(3);
			sortSpinOption = false;
		}
	super.onResume();
	}
	
	//Getting Info from the Opened Activities
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
			case TAGTEXT:
				if(resultCode==RESULT_OK){
					tagsText.setText(tagsText.getText()+data.getExtras().getString("tag") + ", "); //as Tags
				}
				break;
			case CATTEXT:
				if(resultCode==RESULT_OK){
					categoriesText.setText(categoriesText.getText()+data.getExtras().getString("cat") + ", ");//as Categories
				}
				break;
		}
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
		//added contentRating
		Spinner contentRating = (Spinner) findViewById(R.id.ContentRatingSpin);
		MainActivity.contentRating = (String) contentRating.getSelectedItem();
		config.contentRating = MainActivity.contentRating;
		//save contentRating
		SharedPreferences.Editor editor = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
		editor.putString("contentRating", MainActivity.contentRating);
		editor.commit();
		
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
	    inflater.inflate(R.menu.menu_search, popup.getMenu());
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
        menuInflater.inflate(R.menu.menu_search, menu);
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
		config.contentRating = "Mature";
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config, MainActivity.browsing);
		action.execute();
	}

	public void browseFeatured() {
		BrowseConfig config = new BrowseConfig();
		config.type = getType();
		config.typeFilter = "Featured";
		config.contentRating = MainActivity.contentRating;
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config, MainActivity.browsing);
		action.execute();
	}

	public void browseCategories() {		
		HttpAction action = new HttpBrowseCategoriesAction(this, getType(), MainActivity.browsing);
		action.execute();
	}
}

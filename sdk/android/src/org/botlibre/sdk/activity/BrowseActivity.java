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
import org.botlibre.sdk.activity.actions.HttpBrowseCategoriesAction;
import org.botlibre.sdk.activity.actions.HttpFetchAction;
import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.activity.actions.HttpPageInstancesAction;
import org.botlibre.sdk.activity.avatar.AvatarSearchActivity;
import org.botlibre.sdk.activity.forum.ForumSearchActivity;
import org.botlibre.sdk.activity.livechat.ChannelSearchActivity;
import org.botlibre.sdk.activity.script.ScriptSearchActivity;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.WebMediumConfig;

import org.botlibre.sdk.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

/**
 * Activity for choosing an instance from the search results.
 */
public class BrowseActivity extends LibreActivity {
	
	public BrowseConfig browse;
	public List<WebMediumConfig> instances;
	public WebMediumConfig instance;
	protected int page = 0;

	public void superOnCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse);
		
		this.instances = MainActivity.instances;
		this.browse = MainActivity.browse;
		
		TextView title = (TextView) findViewById(R.id.title);
		title.setText("Browse " + getType() + "s");

		if (MainActivity.browsing) {
			findViewById(R.id.chatButton).setVisibility(View.GONE);
		}

		ListView list = (ListView) findViewById(R.id.instancesList);
		GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTapEvent(MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					ListView list = (ListView) findViewById(R.id.instancesList);
			        int index = list.getCheckedItemPosition();
			        if (index < 0) {
						return false;
			        } else {
			        	selectInstance(list);
			        }
					return true;
				}
				return false;
			}
		};
		final GestureDetector listDetector = new GestureDetector(this, listener);
		list.setOnTouchListener(new View.OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return listDetector.onTouchEvent(event);
			}
		});
		
		resetView();
	}
	
	public void resetView() {
		ListView list = (ListView) findViewById(R.id.instancesList);
		list.setAdapter(new ImageListAdapter(this, R.layout.image_list, this.instances));
		View next = (View) findViewById(R.id.nextButton);
		if (this.instances.size() >= 56 || this.page > 0) {
			if (this.instances.size() >= 56) {
				next.setVisibility(View.VISIBLE);
			} else {
				next.setVisibility(View.GONE);
			}
		} else {
			next.setVisibility(View.GONE);
		}
		View previous = (View) findViewById(R.id.previousButton);
		if (this.page > 0) {
			previous.setVisibility(View.VISIBLE);
		} else {
			previous.setVisibility(View.GONE);
		}
	}
	
	public void previousPage(View view) {
		this.page--;
		this.browse.page = String.valueOf(this.page);
		HttpAction action = new HttpPageInstancesAction(this, this.browse);
    	action.execute();
	}
	
	public void nextPage(View view) {
		this.page++;
		this.browse.page = String.valueOf(this.page);
		HttpAction action = new HttpPageInstancesAction(this, this.browse);
    	action.execute();
	}
	
	public void superOnResume() {
		super.onResume();
	}
	
	@Override
	public void onResume() {
		if (!this.instances.isEmpty() && !MainActivity.instances.isEmpty()) {
			if (this.instances.get(0).getClass() == MainActivity.instances.get(0).getClass()) {
				this.instances = MainActivity.instances;
			}
		} else {
			this.instances = MainActivity.instances;
		}

		resetView();
		
		super.onResume();
	}

	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.layout.menu_browse, popup.getMenu());
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
        menuInflater.inflate(R.layout.menu_browse, menu);
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
	    case R.id.menuSearch:
	    	search(null);
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

	public void browseMyBots() {
		BrowseConfig config = new BrowseConfig();
		config.type = getType();
		config.typeFilter = "Personal";
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config, true);
		action.execute();
	}

	public void browseFeatured() {
		BrowseConfig config = new BrowseConfig();
		config.type = getType();
		config.typeFilter = "Featured";
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config, true);
		action.execute();
	}

	public void browseCategories() {		
		HttpAction action = new HttpBrowseCategoriesAction(this, getType(), true);
		action.execute();
	}
	
	public String getType() {
		return "Bot";
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

	public void selectInstance(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select a bot", this);
        	return;
        }
        this.instance = instances.get(index);
		if (MainActivity.browsing) {
			MainActivity.instance = this.instance;
			finish();
			return;
		}
        InstanceConfig config = new InstanceConfig();
        config.id = this.instance.id;
        config.name = this.instance.name;
		
        HttpAction action = new HttpFetchAction(this, config);
    	action.execute();
	}

	public void chat(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select a bot", this);
        	return;
        }
        this.instance = instances.get(index);
        InstanceConfig config = new InstanceConfig();
        config.id = this.instance.id;
        config.name = this.instance.name;
		
        HttpAction action = new HttpFetchAction(this, config, true);
    	action.execute();
	}

	public void search(View view) {
		finish();
		if (!MainActivity.searching) {
			Intent intent = null;
			if (getType().equals("Domain")) {
		        intent = new Intent(this, DomainSearchActivity.class);
			} else if (getType().equals("Forum")) {
		        intent = new Intent(this, ForumSearchActivity.class);
			} else if (getType().equals("Channel")) {
		        intent = new Intent(this, ChannelSearchActivity.class);
			} else if (getType().equals("Avatar")) {
		        intent = new Intent(this, AvatarSearchActivity.class);
			} else if (getType().equals("Script")) {
				intent = new Intent(this, ScriptSearchActivity.class);
			} else {
				intent = new Intent(this, BotSearchActivity.class);
			}
	        startActivity(intent);
		}
	}
}

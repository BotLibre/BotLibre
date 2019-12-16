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

import org.botlibre.sdk.activity.BrowseActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchAction;
import org.botlibre.sdk.activity.actions.HttpGetPostsAction;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ForumConfig;

import org.botlibre.offline.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;

/**
 * Activity for choosing a forum from the search results.
 */
public class BrowseForumActivity extends BrowseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public String getType() {
		return "Forum";
	}
	
	public void selectInstance(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        this.instance = this.instances.get(index);
        ForumConfig config = new ForumConfig();
        config.id = this.instance.id;
		
        HttpAction action = new HttpFetchAction(this, config);
    	action.execute();
	}

	public void chat(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        this.instance = instances.get(index);
        MainActivity.instance = this.instance;
		BrowseConfig config = new BrowseConfig();
		
		config.typeFilter = "Public";
		config.type = "Post";
		config.instance = this.instance.id;
		config.sort = "date";
		
		HttpAction action = new HttpGetPostsAction(this, config);
		action.execute();
	}

	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.menu.menu_browse_forum, popup.getMenu());
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
        menuInflater.inflate(R.menu.menu_browse_forum, menu);
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
        case R.id.menuBrowseAllPosts:
        	browseAllPosts();
            return true;
        case R.id.menuBrowseFeaturedPosts:
        	browseFeaturedPosts();
            return true;
        case R.id.menuSearchAllPosts:
        	searchAllPosts();
            return true;
        case R.id.menuMyPosts:
        	browseMyPosts();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.menuMyPosts);
        if (MainActivity.user == null) {
        	item.setEnabled(false);
        }
	    return super.onPrepareOptionsMenu(menu);
	}

	public void searchAllPosts() {
		finish();
		MainActivity.instance = null;
		Intent intent = new Intent(this, SearchPostsActivity.class);
        startActivity(intent);
	}

	public void browseMyPosts() {
		MainActivity.instance = null;
		BrowseConfig config = new BrowseConfig();
		config.type = "Post";
		config.typeFilter = "Personal";
		config.sort = "date";
		HttpGetPostsAction action = new HttpGetPostsAction(this, config, true);
		action.execute();
	}

	public void browseFeaturedPosts() {
		BrowseConfig config = new BrowseConfig();
		config.type = "Post";
		config.typeFilter = "Featured";
		config.sort = "date";
		HttpGetPostsAction action = new HttpGetPostsAction(this, config, true);
		action.execute();
	}

	public void browseAllPosts() {
		BrowseConfig config = new BrowseConfig();
		config.type = "Post";
		config.typeFilter = "Public";
		config.sort = "date";
		HttpGetPostsAction action = new HttpGetPostsAction(this, config, true);
		action.execute();
	}
}

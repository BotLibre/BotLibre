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

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.WebMediumActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpGetPostsAction;
import org.botlibre.sdk.activity.actions.HttpSubscribeForumAction;
import org.botlibre.sdk.activity.actions.HttpUnsubscribeForumAction;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ForumConfig;

import org.botlibre.offline.R;

import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

/**
 * Activity for viewing a forum's details.
 * To launch this activity from your app you can use the HttpFetchAction passing the forum id or name as a config.
 */
public class ForumActivity extends WebMediumActivity {
	
	@Override
	public void onResume() {
		MainActivity.searchingPosts = false;
		super.onResume();
	}

	public void admin() {
        Intent intent = new Intent(this, ForumAdminActivity.class);		
        startActivity(intent);
	}
	
	public void searchPosts() {
        Intent intent = new Intent(this, SearchPostsActivity.class);		
        startActivity(intent);
	}
	
	public void myPosts() {
		BrowseConfig config = new BrowseConfig();
		
		config.typeFilter = "Personal";
		config.type = "Post";
		config.instance = this.instance.id;
		config.sort = "date";
		
		HttpAction action = new HttpGetPostsAction(this, config);
		action.execute();
	}
	
	public void browsePosts(View view) {
		BrowseConfig config = new BrowseConfig();
		
		config.typeFilter = "Public";
		config.type = "Post";
		config.instance = this.instance.id;
		config.sort = "date";
		
		HttpAction action = new HttpGetPostsAction(this, config);
		action.execute();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_forum, menu);
        return true;
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (MainActivity.user == null) {
        	menu.findItem(R.id.menuMyPosts).setEnabled(false);
        }
	    return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.menu.menu_forum, popup.getMenu());
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {  
	    case R.id.menuSearch:
	    	searchPosts();
	        return true;
	    case R.id.menuMyPosts:
	    	myPosts();
	        return true;
	    case R.id.menuAdmin:
	    	admin();
	        return true;
        case R.id.menuFlag:
        	flag();
            return true;
        case R.id.menuCreator:
        	viewCreator();
            return true;
        case R.id.menuStar:
        	star();
            return true;
        case R.id.menuThumbsUp:
        	thumbsUp();
            return true;
        case R.id.menuThumbsDown:
        	thumbsDown();
            return true;
        case R.id.menuSubscribe:
        	subscribe();
            return true;
        case R.id.menuUnsubscribe:
        	unsubscribe();
            return true;
        case R.id.website:
        	openWebsite();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
	public void newPost(View view) {
		if (MainActivity.user == null) {
			MainActivity.showMessage("You must sign in first", this);
			return;
		}
        Intent intent = new Intent(this, CreateForumPostActivity.class);		
        startActivity(intent);
	}

	public void resetView() {
        setContentView(R.layout.activity_forum);
		
        ForumConfig instance = (ForumConfig)MainActivity.instance;

        super.resetView();

        if (instance.isExternal) {
        	findViewById(R.id.newPostButton).setVisibility(View.GONE);
        	findViewById(R.id.postsButton).setVisibility(View.GONE);
        }

    	TextView text = (TextView) findViewById(R.id.postsLabel);
        if (instance.posts != null && instance.posts.length() > 0) {
	        text.setText(instance.posts + " posts");
        } else {
	        text.setText("");        	
        }
	}
	
	public void subscribe() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to subscribe for email updates", this);
        	return;
        }
        HttpSubscribeForumAction action = new HttpSubscribeForumAction(this, (ForumConfig)this.instance.credentials());
    	action.execute();
	}
	
	public void unsubscribe() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to unsubscribe from email updates", this);
        	return;
        }
        HttpUnsubscribeForumAction action = new HttpUnsubscribeForumAction(this, (ForumConfig)this.instance.credentials());
    	action.execute();
	}
	
	public void openWebsite() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.WEBSITE + "/forum?id=" + MainActivity.instance.id));
		startActivity(intent);
	}
	
	public String getType() {
		return "Forum";
	}
	
}

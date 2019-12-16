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

package org.botlibre.sdk.activity.issuetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.botlibre.sdk.activity.LibreActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchIssueAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetIssuesAction;
import org.botlibre.sdk.activity.actions.HttpGetPostsAction;
import org.botlibre.sdk.activity.actions.HttpPagePostsAction;
import org.botlibre.sdk.activity.forum.CreateForumPostActivity;
import org.botlibre.sdk.activity.forum.SearchPostsActivity;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ForumConfig;
import org.botlibre.sdk.config.IssueConfig;
import org.botlibre.sdk.config.IssueTrackerConfig;
import org.botlibre.sdk.util.Utils;

import java.util.List;

import org.botlibre.offline.pizzabot.R;

/**
 * Activity for choosing an issue from the search results.
 */
public class BrowseIssueActivity extends LibreActivity {
	
	public List<IssueConfig> instances;
	public IssueConfig instance;
	protected int page = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse_issue);
		if (MainActivity.instance instanceof IssueTrackerConfig) {
			//setTitle(Utils.stripTags(MainActivity.instance.name));
			//((TextView) findViewById(R.id.title)).setText(Utils.stripTags(MainActivity.instance.name));
	        //HttpGetImageAction.fetchImage(this, MainActivity.instance.avatar, findViewById(R.id.icon));
		} else {
			//((ImageView)findViewById(R.id.icon)).setImageResource(R.drawable.icon);
		}
		
		this.instances = MainActivity.issues;

		ListView list = (ListView) findViewById(R.id.instancesList);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ListView list = (ListView) findViewById(R.id.instancesList);
				selectInstance(list);
			}
		});
		
		resetView();
	}
	
	@Override
	public void onResume() {
		this.instances = MainActivity.issues;

		ListView list = (ListView) findViewById(R.id.instancesList);
		list.setAdapter(new IssueImageListAdapter(this, R.layout.issue_list, this.instances));
		
		super.onResume();
	}

	public void selectInstance(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select an issue", this);
        	return;
        }
        this.instance = instances.get(index);
		IssueConfig config = new IssueConfig();
        config.id = this.instance.id;
		
        HttpAction action = new HttpFetchIssueAction(this, config);
    	action.execute();
	}

	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.menu.menu_browse_issue, popup.getMenu());
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
        menuInflater.inflate(R.menu.menu_browse_issue, menu);
        return true;
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
	    case R.id.menuMyIssues:
	    	browseMyIssues();
	        return true;
	    case R.id.menuSearch:
	    	search(null);
	        return true;
	    case R.id.menuPriority:
	    	browsePriority();
	        return true;
	    case R.id.menuNewIssue:
	    	newIssue();
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
	    return true;
	}
	
	public void resetView() {
		ListView list = (ListView) findViewById(R.id.instancesList);
		list.setAdapter(new IssueImageListAdapter(this, R.layout.issue_list, this.instances));

		View next = findViewById(R.id.nextButton);
		if (this.instances.size() >= 56 || this.page > 0) {
			if (this.instances.size() >= 56) {
				next.setVisibility(View.VISIBLE);
			} else {
				next.setVisibility(View.GONE);
			}
		} else {
			next.setVisibility(View.GONE);
		}
		View previous = findViewById(R.id.previousButton);
		if (this.page > 0) {
			previous.setVisibility(View.VISIBLE);
		} else {
			previous.setVisibility(View.GONE);
		}
	}
	
	public void previousPage(View view) {
		this.page--;
		MainActivity.browseIssues.page = String.valueOf(this.page);
		HttpAction action = new HttpPagePostsAction(this, MainActivity.browseIssues);
    	action.execute();
	}
	
	public void nextPage(View view) {
		this.page++;
		MainActivity.browseIssues.page = String.valueOf(this.page);
		HttpAction action = new HttpPagePostsAction(this, MainActivity.browseIssues);
    	action.execute();
	}

	public void search(View view) {
		finish();
		if (!MainActivity.searchingIssues) {
			Intent intent = new Intent(this, SearchIssuesActivity.class);
	        startActivity(intent);
		}
	}

	public void browseMyIssues() {
		BrowseConfig config = new BrowseConfig();
		if (MainActivity.instance instanceof IssueTrackerConfig) {
			config.instance = MainActivity.instance.id;
		}
		config.type = "Issue";
		config.typeFilter = "Personal";
		config.sort = "date";
		HttpGetIssuesAction action = new HttpGetIssuesAction(this, config, true);
		action.execute();
	}

	public void browsePriority() {
		BrowseConfig config = new BrowseConfig();
		if (MainActivity.instance instanceof IssueTrackerConfig) {
			config.instance = MainActivity.instance.id;
		}
		config.type = "Issue";
		config.typeFilter = "Priority";
		config.sort = "date";
		HttpGetIssuesAction action = new HttpGetIssuesAction(this, config, true);
		action.execute();
	}
    
	public void newIssue() {
		if (MainActivity.user == null) {
			MainActivity.showMessage("You must sign in first", this);
			return;
		}
        Intent intent = new Intent(this, CreateIssueActivity.class);
        startActivity(intent);
	}
}

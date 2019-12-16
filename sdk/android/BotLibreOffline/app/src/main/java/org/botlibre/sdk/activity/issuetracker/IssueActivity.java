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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.botlibre.sdk.activity.LibreActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpDeleteIssueAction;
import org.botlibre.sdk.activity.actions.HttpFetchIssueAction;
import org.botlibre.sdk.activity.actions.HttpFetchUserAction;
import org.botlibre.sdk.activity.actions.HttpFlagIssueAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpStarIssueAction;
import org.botlibre.sdk.activity.actions.HttpSubscribeIssueAction;
import org.botlibre.sdk.activity.actions.HttpThumbsDownIssueAction;
import org.botlibre.sdk.activity.actions.HttpThumbsUpIssueAction;
import org.botlibre.sdk.activity.actions.HttpUnsubscribeIssueAction;
import org.botlibre.sdk.config.IssueConfig;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.util.Utils;

import org.botlibre.offline.R;

/**
 * Activity for viewing an issue.
 */
@SuppressLint("DefaultLocale")
public class IssueActivity extends LibreActivity {
	Menu menu;
	IssueConfig instance;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		resetView();
	}

	@Override
	public void onResume() {
		if (MainActivity.issue != null && MainActivity.issue.id.equals(this.instance.id)) {
			this.instance = MainActivity.issue;
		} else {
			MainActivity.issue = this.instance;
		}
		resetView();
		resetMenu();
		super.onResume();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_issue, menu);
        return true;
    }

	public void resetMenu() {
		if (this.menu == null) {
			return;
		}
        for (int index = 0; index < menu.size(); index++) {
    	    menu.getItem(index).setEnabled(true);
        }

        boolean isAdmin = (MainActivity.user != null) && instance.isAdmin;
        if (!isAdmin) {
        	menu.findItem(R.id.menuEdit).setEnabled(false);
        	menu.findItem(R.id.menuDelete).setEnabled(false);
        }
        if (instance.isFlagged) {
        	menu.findItem(R.id.menuFlag).setEnabled(false);
        }
        int position = MainActivity.issues.indexOf(this.instance);
        if (position < 0) {
        	menu.findItem(R.id.menuViewNext).setEnabled(false);
        	menu.findItem(R.id.menuViewPrevious).setEnabled(false);
        } else if (position == 0) {
        	menu.findItem(R.id.menuViewNext).setEnabled(false);
        }
        if ((position + 1) == MainActivity.issues.size()) {
        	menu.findItem(R.id.menuViewPrevious).setEnabled(false);
        }
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		this.menu = menu;
        resetMenu();
	    return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {
        case R.id.menuViewNext:
        	viewNext();
            return true;
        case R.id.menuViewPrevious:
        	viewPrevious();
            return true;
        case R.id.menuViewUser:
        	viewUser();
            return true;
        case R.id.menuEdit:
        	editIssue();
            return true;
        case R.id.menuDelete:
        	deleteIssue();
            return true;
        case R.id.menuFlag:
        	flag();
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
        case R.id.menuStar:
        	star();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	public void resetView() {
        this.instance = MainActivity.issue;
        setContentView(R.layout.activity_issue);

        IssueConfig instance = MainActivity.issue;

        if (!instance.isFlagged) {
	        findViewById(R.id.flaggedLabel).setVisibility(View.GONE);
        }

		((TextView) findViewById(R.id.title)).setText(Utils.stripTags(instance.title));

        TextView textView = (TextView) findViewById(R.id.tagsLabel);
        textView.setText("Department: " + instance.tags);
        if (instance.tags == null || instance.tags.isEmpty()) {
        	textView.setVisibility(View.GONE);
        }
        textView = (TextView) findViewById(R.id.priorityLabel);
		textView.setText("Priority: " + instance.priority);
		textView = (TextView) findViewById(R.id.creationDateLabel);
        textView.setText("Created " + instance.displayCreationDate() + " by " + instance.creator);

        //textView = (TextView) findViewById(R.id.thumbsupLabel);
        //textView.setText(String.valueOf(this.instance.thumbsUp));
        //textView = (TextView) findViewById(R.id.thumbsdownLabel);
        //textView.setText(String.valueOf(this.instance.thumbsDown));
        //textView = (TextView) findViewById(R.id.starsLabel);
        //textView.setText(String.valueOf(this.instance.stars));

        int position = MainActivity.issues.indexOf(this.instance);
        if (position < 0) {
        	findViewById(R.id.nextButton).setEnabled(false);
        	findViewById(R.id.nextButton).setAlpha(0.5f);
        	findViewById(R.id.previousButton).setEnabled(false);
        	findViewById(R.id.previousButton).setAlpha(0.5f);
        } else if (position == 0) {
        	findViewById(R.id.nextButton).setEnabled(false);
        	findViewById(R.id.nextButton).setAlpha(0.5f);
        }
        if ((position + 1) == MainActivity.issues.size()) {
        	findViewById(R.id.previousButton).setEnabled(false);
        	findViewById(R.id.previousButton).setAlpha(0.5f);
        }

        final WebView web = (WebView) findViewById(R.id.detailsLabel);
        web.loadDataWithBaseURL(null, instance.detailsText, "text/html", "utf-8", null);
        web.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
            	try {
            		view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            	} catch (Exception failed) {
            		return false;
            	}
                return true;
            }
        });

		//ImageView iconView = (ImageView)findViewById(R.id.icon);
        //HttpGetImageAction.fetchImage(this, this.instance.avatar, iconView);
	}

	public void viewUser() {
		UserConfig user = new UserConfig();
		user.user = this.instance.creator;

        HttpAction action = new HttpFetchUserAction(this, user);
    	action.execute();
	}

	public void flag() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to flag an issue", this);
        	return;
        }
        final EditText text = new EditText(this);
        MainActivity.prompt("Enter reason for flagging the issue as offensive", this, text, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            IssueConfig config = new IssueConfig();
	    		config.id = instance.id;
	    		config.flaggedReason = text.getText().toString();
	            if (config.flaggedReason.trim().length() == 0) {
	            	MainActivity.error("You must enter a valid reason for flagging the issue", null, IssueActivity.this);
	            	return;
	            }
	            
	            HttpAction action = new HttpFlagIssueAction(IssueActivity.this, config);
	        	action.execute();
	        }
        });
	}

	public void edit(View view) {
		editIssue();
	}

	public void editIssue() {
        Intent intent = new Intent(this, EditIssueActivity.class);
        startActivity(intent);
	}

	public void deleteIssue() {
		MainActivity.confirm("Are you sure you want to delete this issue?", this, true, new Dialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		        IssueConfig config = new IssueConfig();
		        config.id = instance.id;
		        
		        HttpAction action = new HttpDeleteIssueAction(IssueActivity.this, config);
		    	action.execute();
			}
		});
	}

	public void viewPrevious() {
	    int index = MainActivity.issues.indexOf(this.instance) + 1;
        if (index >= MainActivity.issues.size()) {
        	MainActivity.showMessage("At end", this);
        	return;
        }
		IssueConfig issue = MainActivity.issues.get(index);

		IssueConfig config = new IssueConfig();
        config.id = issue.id;
        
        HttpAction action = new HttpFetchIssueAction(this, config);
    	action.execute();
	}

	public void viewNext() {
	    int index = MainActivity.issues.indexOf(this.instance) - 1;
        if (index < 0) {
        	MainActivity.showMessage("At start", this);
        	return;
        }
		IssueConfig issue = MainActivity.issues.get(index);

		IssueConfig config = new IssueConfig();
        config.id = issue.id;
		        
        HttpAction action = new HttpFetchIssueAction(this, config);
    	action.execute();
	}

	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.menu_issue, popup.getMenu());
		onPrepareOptionsMenu(popup.getMenu());
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				return onOptionsItemSelected(item);
			}
		});
		popup.show();
	}
	
	public void thumbsUp() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to thumbs up an issue", this);
        	return;
        }
		HttpAction action = new HttpThumbsUpIssueAction(this, this.instance.credentials());
    	action.execute();
	}

	public void thumbsDown(View view) {
		thumbsDown();
	}

	public void thumbsUp(View view) {
		thumbsUp();
	}

	public void star(View view) {
		star();
	}

	public void viewNext(View view) {
		viewNext();
	}

	public void viewPrevious(View view) {
		viewPrevious();
	}
	
	public void subscribe() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to subscribe for email updates", this);
        	return;
        }
		HttpSubscribeIssueAction action = new HttpSubscribeIssueAction(this, this.instance.credentials());
    	action.execute();
	}
	
	public void unsubscribe() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to unsubscribe from email updates", this);
        	return;
        }
		HttpUnsubscribeIssueAction action = new HttpUnsubscribeIssueAction(this, this.instance.credentials());
    	action.execute();
	}
	
	public void thumbsDown() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to thumbs down an issue", this);
        	return;
        }
		HttpThumbsDownIssueAction action = new HttpThumbsDownIssueAction(this, this.instance.credentials());
    	action.execute();
	}
	
	public void openWebsite() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.WEBSITE + "/issue?id=" + this.instance.id));
		startActivity(intent);
	}
	
	public void star() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to rate an issue", this);
        	return;
        }
        
        final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog_stars);
		dialog.setTitle("Rate Issue");
		final int[] stars = new int[1];
		stars[0] = 0;
		dialog.findViewById(R.id.oneStar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
				star(1);
			}
		});
		dialog.findViewById(R.id.twoStar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
				star(2);
			}
		});
		dialog.findViewById(R.id.threeStar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
				star(3);
			}
		});
		dialog.findViewById(R.id.fourStar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
				star(4);
			}
		});
		dialog.findViewById(R.id.fiveStar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
				star(5);
			}
		});

		dialog.show();
	}

	public void star(int stars) {
		if (stars == 0) {
			return;
		}
		
        IssueConfig config = this.instance.credentials();
        config.stars = String.valueOf(stars);
		HttpStarIssueAction action = new HttpStarIssueAction(this, config);
    	action.execute();
	}
	
}

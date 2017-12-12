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
import org.botlibre.sdk.activity.actions.HttpDeleteForumPostAction;
import org.botlibre.sdk.activity.actions.HttpFetchForumPostAction;
import org.botlibre.sdk.activity.actions.HttpFetchUserAction;
import org.botlibre.sdk.activity.actions.HttpFlagForumPostAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpStarPostAction;
import org.botlibre.sdk.activity.actions.HttpSubscribeAction;
import org.botlibre.sdk.activity.actions.HttpThumbsDownPostAction;
import org.botlibre.sdk.activity.actions.HttpThumbsUpPostAction;
import org.botlibre.sdk.activity.actions.HttpUnsubscribeAction;
import org.botlibre.sdk.config.ForumPostConfig;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.util.Utils;

import org.botlibre.sdk.R;

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
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity for viewing a forum post.
 */
@SuppressLint("DefaultLocale")
public class ForumPostActivity extends LibreActivity {
	Menu menu;
	ForumPostConfig instance;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		resetView();
	}
	
	@Override
	public void onResume() {
		if (MainActivity.post != null && MainActivity.post.id.equals(this.instance.id)) {
			this.instance = MainActivity.post;
		} else {
			MainActivity.post = this.instance;
		}
		resetView();
		resetMenu();
		super.onResume();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_post, menu);
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
        if (instance.parent == null || instance.parent.length() == 0) {
        	menu.findItem(R.id.menuViewParent).setEnabled(false);        	
        }
        if (instance.replies == null || instance.replies.isEmpty()) {
        	menu.findItem(R.id.menuViewReply).setEnabled(false);
        }
        int position = MainActivity.posts.indexOf(this.instance);
        if (position < 0) {
        	menu.findItem(R.id.menuViewNext).setEnabled(false);
        	menu.findItem(R.id.menuViewPrevious).setEnabled(false);
        } else if (position == 0) {
        	menu.findItem(R.id.menuViewNext).setEnabled(false);        	
        }
        if ((position + 1) == MainActivity.posts.size()) {
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
        case R.id.menuViewReply:
        	viewReply();
            return true;
        case R.id.menuViewParent:
        	viewParent();
            return true;
        case R.id.menuViewNext:
        	viewNext();
            return true;
        case R.id.menuViewPrevious:
        	viewPrevious();
            return true;
        case R.id.menuViewUser:
        	viewUser();
            return true;
        case R.id.menuReply:
        	reply();
            return true;
        case R.id.menuEdit:
        	editPost();
            return true;
        case R.id.menuDelete:
        	deletePost();
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
        this.instance = MainActivity.post;
        setContentView(R.layout.activity_forumpost);
	
        ForumPostConfig instance = MainActivity.post;
        
        if (!instance.isFlagged) {
	        findViewById(R.id.flaggedLabel).setVisibility(View.GONE);
        }
        
		((TextView) findViewById(R.id.title)).setText(Utils.stripTags(instance.topic));
        
        TextView textView = (TextView) findViewById(R.id.tagsLabel);
        textView.setText(instance.tags);
        if (instance.tags == null || instance.tags.isEmpty()) {
        	textView.setVisibility(View.GONE);
        }
        textView = (TextView) findViewById(R.id.creationDateLabel);
        textView.setText("by " + instance.creator + " posted " + instance.displayCreationDate());

        textView = (TextView) findViewById(R.id.thumbsupLabel);
        textView.setText(String.valueOf(this.instance.thumbsUp));
        textView = (TextView) findViewById(R.id.thumbsdownLabel);
        textView.setText(String.valueOf(this.instance.thumbsDown));
        textView = (TextView) findViewById(R.id.starsLabel);
        textView.setText(String.valueOf(this.instance.stars));
        
        int position = MainActivity.posts.indexOf(this.instance);
        if (position < 0) {
        	findViewById(R.id.nextButton).setEnabled(false);
        	findViewById(R.id.nextButton).setAlpha(0.5f);
        	findViewById(R.id.previousButton).setEnabled(false);
        	findViewById(R.id.previousButton).setAlpha(0.5f);
        } else if (position == 0) {
        	findViewById(R.id.nextButton).setEnabled(false);
        	findViewById(R.id.nextButton).setAlpha(0.5f);
        }
        if ((position + 1) == MainActivity.posts.size()) {
        	findViewById(R.id.previousButton).setEnabled(false);
        	findViewById(R.id.previousButton).setAlpha(0.5f);
        }
        if (this.instance.parent == null || this.instance.parent.length() == 0) {
        	findViewById(R.id.postButton).setVisibility(View.GONE);        	
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
		GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTapEvent(MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					View imageLayout = findViewById(R.id.repliesList);
					if (imageLayout.getVisibility() == View.VISIBLE) {
						imageLayout.setVisibility(View.GONE);
					} else {
						imageLayout.setVisibility(View.VISIBLE);
					}
					return true;
				}
				return false;
			}
		};
		final GestureDetector detector = new GestureDetector(this, listener);
		web.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return detector.onTouchEvent(event);
			}
		});
        
		ListView list = (ListView) findViewById(R.id.repliesList);
		if (instance.replies == null || instance.replies.isEmpty()) {
			list.setVisibility(View.GONE);
		} else {
			list.setAdapter(new ForumReplyImageListAdapter(this, R.layout.forumreply_list, instance.replies));
		}
		listener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTapEvent(MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					ListView list = (ListView) findViewById(R.id.repliesList);
			        int index = list.getCheckedItemPosition();
			        if (index < 0) {
						return false;
			        } else {
			        	viewReply();
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

		ImageView iconView = (ImageView)findViewById(R.id.icon);
        HttpGetImageAction.fetchImage(this, this.instance.avatar, iconView);
        iconView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewUser();
			}
		});
	}

	public void viewUser() {
		UserConfig user = new UserConfig();
		user.user = this.instance.creator;

        HttpAction action = new HttpFetchUserAction(this, user);
    	action.execute();
	}

	public void flag() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to flag a post", this);
        	return;
        }
        final EditText text = new EditText(this);
        MainActivity.prompt("Enter reason for flagging the post as offensive", this, text, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            ForumPostConfig config = new ForumPostConfig();
	    		config.id = instance.id;
	    		config.flaggedReason = text.getText().toString();
	            if (config.flaggedReason.trim().length() == 0) {
	            	MainActivity.error("You must enter a valid reason for flagging the post", null, ForumPostActivity.this);
	            	return;
	            }
	            
	            HttpAction action = new HttpFlagForumPostAction(ForumPostActivity.this, config);
	        	action.execute();
	        }
        });
	}

	public void viewReply() {
		ListView list = (ListView) findViewById(R.id.repliesList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select reply", this);
        	return;
        }
        ForumPostConfig reply = this.instance.replies.get(index);

        ForumPostConfig config = new ForumPostConfig();
        config.id = reply.id;
		        
        HttpAction action = new HttpFetchForumPostAction(this, config);
    	action.execute();
	}

	public void editPost() {
        Intent intent = new Intent(this, EditForumPostActivity.class);		
        startActivity(intent);
	}

	public void reply() {
		if (MainActivity.user == null) {
			MainActivity.showMessage("You must sign in first", this);
			return;
		}
        Intent intent = new Intent(this, CreateReplyActivity.class);		
        startActivity(intent);
	}

	public void deletePost() {
		MainActivity.confirm("Are you sure you want to delete this post?", this, new Dialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {				
		        ForumPostConfig config = new ForumPostConfig();
		        config.id = instance.id;
		        
		        HttpAction action = new HttpDeleteForumPostAction(ForumPostActivity.this, config);
		    	action.execute();
			}
		});
	}

	public void viewPrevious() {
	    int index = MainActivity.posts.indexOf(this.instance) + 1;
        if (index >= MainActivity.posts.size()) {
        	MainActivity.showMessage("At end", this);
        	return;
        }
        ForumPostConfig post = MainActivity.posts.get(index);

        ForumPostConfig config = new ForumPostConfig();
        config.id = post.id;
        
        HttpAction action = new HttpFetchForumPostAction(this, config);
    	action.execute();
	}

	public void viewNext() {
	    int index = MainActivity.posts.indexOf(this.instance) - 1;
        if (index < 0) {
        	MainActivity.showMessage("At start", this);
        	return;
        }
        ForumPostConfig post = MainActivity.posts.get(index);

        ForumPostConfig config = new ForumPostConfig();
        config.id = post.id;
		        
        HttpAction action = new HttpFetchForumPostAction(this, config);
    	action.execute();
	}

	public void viewParent(View view) {
		viewParent();
	}
	
	public void viewParent() {
        if (this.instance.parent == null || this.instance.parent.length() == 0) {
        	MainActivity.showMessage("Not a reply", this);
        	return;
        }

        ForumPostConfig config = new ForumPostConfig();
        config.id = this.instance.parent;
		
        HttpAction action = new HttpFetchForumPostAction(this, config);
    	action.execute();
	}

	public void menu(View view) {
		openOptionsMenu();
	}
	
	public void thumbsUp() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to thumbs up a post or reply", this);
        	return;
        }
        HttpThumbsUpPostAction action = new HttpThumbsUpPostAction(this, this.instance.credentials());
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

	public void reply(View view) {
		reply();
	}
	
	public void subscribe() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to subscribe for email updates", this);
        	return;
        }
        HttpSubscribeAction action = new HttpSubscribeAction(this, this.instance.credentials());
    	action.execute();
	}
	
	public void unsubscribe() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to unsubscribe from email updates", this);
        	return;
        }
        HttpUnsubscribeAction action = new HttpUnsubscribeAction(this, this.instance.credentials());
    	action.execute();
	}
	
	public void thumbsDown() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to thumbs down a post or reply", this);
        	return;
        }
        HttpThumbsDownPostAction action = new HttpThumbsDownPostAction(this, this.instance.credentials());
    	action.execute();
	}
	
	public void openWebsite() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.WEBSITE + "/forum-post?id=" + this.instance.id));
		startActivity(intent);
	}
	
	public void star() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to rate a post or reply", this);
        	return;
        }
        
        final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog_stars);
		if (this.instance.parent != null )
		dialog.setTitle("Rate Post");
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
		
        ForumPostConfig config = this.instance.credentials();
        config.stars = String.valueOf(stars);
        HttpStarPostAction action = new HttpStarPostAction(this, config);
    	action.execute();
	}
	
}

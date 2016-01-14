package org.botlibre.sdk.activity.forum;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpDeleteForumPostAction;
import org.botlibre.sdk.activity.actions.HttpFetchForumPostAction;
import org.botlibre.sdk.activity.actions.HttpFlagForumPostAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.ForumPostConfig;

/**
 * Activity for viewing a forum post.
 */
@SuppressLint("DefaultLocale")
public class ForumPostActivity extends Activity {
	Menu menu;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		resetView();
	}
	
	@Override
	public void onResume() {
		resetView();
		resetMenu();
		super.onResume();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.menu_post, menu);
        return true;
    }
	
	public void resetMenu() {
		if (this.menu == null) {
			return;
		}
        ForumPostConfig instance = MainActivity.post;
        for (int index = 0; index < menu.size(); index++) {
    	    menu.getItem(index).setEnabled(true);        	
        }
        
        boolean isAdmin = (MainActivity.user != null) && instance.isAdmin;
        if (!isAdmin) {
    	    menu.getItem(5).setEnabled(false);
    	    menu.getItem(7).setEnabled(false);
        }
        if (instance.isFlagged) {
    	    menu.getItem(6).setEnabled(false);
        }
        if (instance.parent == null || instance.parent.length() == 0) {
    	    menu.getItem(1).setEnabled(false);        	
        }
        if (instance.replies == null || instance.replies.isEmpty()) {
    	    menu.getItem(0).setEnabled(false);
        }
        int position = MainActivity.posts.indexOf(MainActivity.post);
        if (position < 0) {
    	    menu.getItem(3).setEnabled(false);
    	    menu.getItem(2).setEnabled(false);
        } else if (position == 0) {
    	    menu.getItem(3).setEnabled(false);        	
        } else if ((position + 1) == MainActivity.posts.size()) {
    	    menu.getItem(2).setEnabled(false);
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
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	public void resetView() {
        setContentView(R.layout.activity_forumpost);
	
        ForumPostConfig instance = MainActivity.post;
        
        if (!instance.isFlagged) {
	        findViewById(R.id.flaggedLabel).setVisibility(View.GONE);
        }
        
        TextView text = (TextView) findViewById(R.id.topicLabel);
        text.setText(instance.topic);
        text = (TextView) findViewById(R.id.tagsLabel);
        text.setText(instance.tags);
        text = (TextView) findViewById(R.id.creatorLabel);
        text.setText(instance.creator);
        text = (TextView) findViewById(R.id.creationDateLabel);
        text.setText(instance.creationDate);
        
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

        HttpGetImageAction.fetchImage(this, instance.avatar, (ImageView)findViewById(R.id.imageView));
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
	    		config.id = MainActivity.post.id;
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
        ForumPostConfig reply = MainActivity.post.replies.get(index);

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
		        config.id = MainActivity.post.id;
		        
		        HttpAction action = new HttpDeleteForumPostAction(ForumPostActivity.this, config);
		    	action.execute();
			}
		});
	}

	public void viewNext() {
	    int index = MainActivity.posts.indexOf(MainActivity.post) + 1;
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

	public void viewPrevious() {
	    int index = MainActivity.posts.indexOf(MainActivity.post) - 1;
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

	public void viewParent() {
        if (MainActivity.post.parent == null || MainActivity.post.parent.length() == 0) {
        	MainActivity.showMessage("Not a reply", this);
        	return;
        }

        ForumPostConfig config = new ForumPostConfig();
        config.id = MainActivity.post.parent;
		
        HttpAction action = new HttpFetchForumPostAction(this, config);
    	action.execute();
	}

	public void menu(View view) {
		openOptionsMenu();
	}
	
}

package org.botlibre.sdk.activity.forum;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import org.botlibre.sdk.activity.R;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchForumPostAction;
import org.botlibre.sdk.config.ForumPostConfig;

/**
 * Activity for choosing a forum post from the search results.
 */
public class ChoosePostActivity extends Activity {
	
	public List<ForumPostConfig> instances;
	public ForumPostConfig instance;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choosepost);
		
        setTitle("Posts: " + MainActivity.instance.name);
		
		this.instances = MainActivity.posts;

		ListView list = (ListView) findViewById(R.id.instancesList);
		list.setAdapter(new ForumPostImageListAdapter(this, R.layout.forumpost_list, this.instances));
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
	}
	
	@Override
	public void onResume() {
		this.instances = MainActivity.posts;

		ListView list = (ListView) findViewById(R.id.instancesList);
		list.setAdapter(new ForumPostImageListAdapter(this, R.layout.forumpost_list, this.instances));
		
		super.onResume();
	}

	public void selectInstance(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select a post", this);
        	return;
        }
        this.instance = instances.get(index);
        ForumPostConfig config = new ForumPostConfig();
        config.id = this.instance.id;
		
        HttpAction action = new HttpFetchForumPostAction(this, config);
    	action.execute();
	}
}

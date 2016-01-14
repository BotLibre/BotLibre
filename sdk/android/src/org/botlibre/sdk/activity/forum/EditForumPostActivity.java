package org.botlibre.sdk.activity.forum;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpGetTagsAction;
import org.botlibre.sdk.activity.actions.HttpUpdateForumPostAction;
import org.botlibre.sdk.config.ForumPostConfig;

/**
 * Activity for editing a forum post.
 */
public class EditForumPostActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_forumpost);
        
        resetView();
	}

	public void resetView() {

    	HttpAction action = new HttpGetTagsAction(this, "Post");
    	action.execute();
		
		ForumPostConfig instance = MainActivity.post;
        
        final AutoCompleteTextView tagsText = (AutoCompleteTextView)findViewById(R.id.tagsText);
        tagsText.setText(instance.tags);
    	
        EditText text = (EditText) findViewById(R.id.topicText);
        text.setText(instance.topic);
        text = (EditText) findViewById(R.id.detailsText);
        text.setText(instance.details);

		CheckBox checkbox = (CheckBox) findViewById(R.id.featuredCheckBox);
		checkbox.setChecked(instance.isFeatured);
        
        if (!MainActivity.instance.isAdmin) {
        	findViewById(R.id.featuredCheckBox).setVisibility(View.GONE);
        }
	}
    
    /**
     * Create the instance.
     */
    public void save(View view) {
    	ForumPostConfig config = new ForumPostConfig();
    	saveProperties(config);
		
    	HttpAction action = new HttpUpdateForumPostAction(
        		this, 
        		config);
        action.execute();
    }

    public void saveProperties(ForumPostConfig config) {    	
        EditText text = (EditText) findViewById(R.id.topicText);
        config.topic = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.detailsText);
        config.details = text.getText().toString().trim();
    	text = (EditText) findViewById(R.id.tagsText);
    	config.tags = text.getText().toString().trim();

		CheckBox checkbox = (CheckBox) findViewById(R.id.featuredCheckBox);
		config.isFeatured = checkbox.isChecked();

		config.id = MainActivity.post.id;
		config.forum = MainActivity.instance.id;
    }

    public void cancel(View view) {        
    	finish();
    }
}

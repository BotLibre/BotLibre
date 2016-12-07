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
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetTagsAction;
import org.botlibre.sdk.activity.actions.HttpUpdateForumPostAction;
import org.botlibre.sdk.config.ForumConfig;
import org.botlibre.sdk.config.ForumPostConfig;

import org.botlibre.sdk.R;

import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Activity for editing a forum post.
 */
public class EditForumPostActivity extends LibreActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_forumpost);
        
        resetView();
	}

	public void resetView() {
        HttpGetImageAction.fetchImage(this, MainActivity.post.avatar, findViewById(R.id.icon));

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
        
        if (!(MainActivity.instance instanceof ForumConfig) || !MainActivity.instance.isAdmin) {
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
		config.forum = MainActivity.post.forum;
    }

    public void cancel(View view) {        
    	finish();
    }
}

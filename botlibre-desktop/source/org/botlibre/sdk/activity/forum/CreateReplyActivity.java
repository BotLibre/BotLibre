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
import org.botlibre.sdk.activity.actions.HttpCreateReplyAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.ForumConfig;
import org.botlibre.sdk.config.ForumPostConfig;

import org.botlibre.sdk.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Activity for creating a forum post reply.
 */
public class CreateReplyActivity extends LibreActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reply);
        
        if (MainActivity.instance instanceof ForumConfig) {
        	HttpGetImageAction.fetchImage(this, MainActivity.instance.avatar, findViewById(R.id.icon));
        } else {
        	((ImageView)findViewById(R.id.icon)).setImageResource(R.drawable.icon,80,80);
        }
        
        TextView text = (TextView) findViewById(R.id.topicText);
        text.setText(MainActivity.post.topic);
        
        CheckBox checkbox = (CheckBox) findViewById(R.id.replyToParentCheckBox);
        if (MainActivity.post.parent != null && MainActivity.post.parent.length() != 0) {
        	checkbox.setChecked(true);
        } else {
        	checkbox.setVisibility(View.GONE);
        }
        
        final WebView web = (WebView) findViewById(R.id.detailsLabel);
        web.loadDataWithBaseURL(null, MainActivity.post.detailsText, "text/html", "utf-8", null);
        
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
	}
    
    /**
     * Create the instance.
     */
    public void create(View view) {
    	ForumPostConfig config = new ForumPostConfig();
    	saveProperties(config);
    	
    	config.forum = MainActivity.post.forum;
        CheckBox checkbox = (CheckBox) findViewById(R.id.replyToParentCheckBox);
        if (checkbox.isChecked() && MainActivity.post.parent != null && MainActivity.post.parent.length() != 0) {
        	config.parent = MainActivity.post.parent;
        } else {
        	config.parent = MainActivity.post.id;        	
        }
		
    	HttpAction action = new HttpCreateReplyAction(
        		this, 
        		config);
        action.execute();
    }

    public void saveProperties(ForumPostConfig config) {
    	EditText text = (EditText) findViewById(R.id.detailsText);
    	config.details = text.getText().toString().trim();
    }

    public void cancel(View view) {        
    	finish();
    }
}

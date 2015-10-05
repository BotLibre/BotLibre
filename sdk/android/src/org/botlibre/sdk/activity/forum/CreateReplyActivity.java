package org.botlibre.sdk.activity.forum;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.botlibre.sdk.activity.R;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpCreateReplyAction;
import org.botlibre.sdk.config.ForumPostConfig;

/**
 * Activity for creating a forum post reply.
 */
public class CreateReplyActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reply);
        
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
    	
    	config.forum = MainActivity.instance.id;
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

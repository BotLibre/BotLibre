package org.botlibre.sdk.activity.actions;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.paphus.botlibre.client.android.santabot.R;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.ContentConfig;

public class HttpGetTagsAction extends HttpAction {
	ContentConfig config;
	Object[] tags;

	public HttpGetTagsAction(Activity activity, String type) {
		super(activity);
		this.config = new ContentConfig();
		this.config.type = type;
	}

	@Override
	protected String doInBackground(Void... params) {
		if (this.config.type.equals("Bot") && MainActivity.tags != null) {
			this.tags = MainActivity.tags;
		} else if (this.config.type.equals("Forum") && MainActivity.forumTags != null) {
			this.tags = MainActivity.forumTags;
		} else if (this.config.type.equals("Post") && MainActivity.forumPostTags != null) {
			this.tags = MainActivity.forumPostTags;
		} else if (this.config.type.equals("Channel") && MainActivity.channelTags != null) {
			this.tags = MainActivity.channelTags;
		} else if (this.config.type.equals("Domain")) {
			this.tags = new Object[0];
		} else {
			try {
				this.tags = MainActivity.connection.getTags(this.config).toArray();
			} catch (Exception exception) {
				this.exception = exception;
			}
		}
		return "";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onPostExecute(String xml) {
		if (this.exception != null) {
			return;
		}
		if (this.config.type.equals("Bot")) {
			MainActivity.tags = this.tags;
		} else if (this.config.type.equals("Forum")) {
			MainActivity.forumTags = this.tags;
		} else if (this.config.type.equals("Post")) {
			MainActivity.forumPostTags = this.tags;
		} else if (this.config.type.equals("Channel")) {
			MainActivity.channelTags = this.tags;
		}
		
        final AutoCompleteTextView tagsText = (AutoCompleteTextView)this.activity.findViewById(R.id.tagsText);
        if (tagsText != null) {
	        ArrayAdapter adapter = new ArrayAdapter(this.activity,
	                android.R.layout.select_dialog_item, this.tags);
	        tagsText.setThreshold(0);
	        tagsText.setAdapter(adapter);
	        tagsText.setOnTouchListener(new View.OnTouchListener() {
		    	   @Override
		    	   public boolean onTouch(View v, MotionEvent event){
		    		   tagsText.showDropDown();
		    		   return false;
		    	   }
		    	});
        }
	}
}
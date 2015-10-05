package org.botlibre.sdk.activity.actions;

import android.app.Activity;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.ForumPostConfig;

public class HttpDeleteForumPostAction extends HttpUIAction {
	
	ForumPostConfig config;

	public HttpDeleteForumPostAction(Activity activity, ForumPostConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		MainActivity.connection.delete(this.config);
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	@Override
	public void onPostExecute(String xml) {
		super.onPostExecute(xml);
		if (this.exception != null) {
			return;
		}
        MainActivity.posts.remove(this.config);
    	
		this.activity.finish();
    }

}
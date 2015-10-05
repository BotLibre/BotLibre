package org.botlibre.sdk.activity.actions;

import android.app.Activity;
import android.content.Intent;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.forum.ForumPostActivity;
import org.botlibre.sdk.config.ForumPostConfig;

public class HttpFetchForumPostAction extends HttpUIAction {
	ForumPostConfig config;

	public HttpFetchForumPostAction(Activity activity, ForumPostConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.config = MainActivity.connection.fetch(this.config);
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
		try {
			MainActivity.post = this.config;
			
			if (this.activity instanceof ForumPostActivity) {
				this.activity.finish();
			}

	        Intent intent = new Intent(this.activity, ForumPostActivity.class);
	        this.activity.startActivity(intent);
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;
		}
	}
	
}
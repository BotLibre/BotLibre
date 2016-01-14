package org.botlibre.sdk.activity.actions;

import android.app.Activity;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.forum.ForumBotActivity;
import org.botlibre.sdk.config.BotModeConfig;
import org.botlibre.sdk.config.ForumConfig;

public class HttpGetForumBotModeAction extends HttpUIAction {
	ForumConfig config;
	BotModeConfig botMode;

	public HttpGetForumBotModeAction(Activity activity, ForumConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.botMode = MainActivity.connection.getForumBotMode(this.config);
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
			MainActivity.botMode = this.botMode;
			
			((ForumBotActivity)this.activity).resetView();
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;
		}
	}
	
}
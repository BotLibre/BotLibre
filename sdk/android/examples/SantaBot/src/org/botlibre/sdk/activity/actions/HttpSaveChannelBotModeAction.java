package org.botlibre.sdk.activity.actions;

import android.app.Activity;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.BotModeConfig;

public class HttpSaveChannelBotModeAction extends HttpUIAction {

	BotModeConfig config;
	
	public HttpSaveChannelBotModeAction(Activity activity, BotModeConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		MainActivity.connection.saveChannelBotMode(this.config);
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
		MainActivity.botMode = this.config;
		
		this.activity.finish();
    }
}
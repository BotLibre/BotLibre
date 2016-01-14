package org.botlibre.sdk.activity.actions;

import android.app.Activity;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.livechat.ChannelBotActivity;
import org.botlibre.sdk.config.BotModeConfig;
import org.botlibre.sdk.config.ChannelConfig;

public class HttpGetChannelBotModeAction extends HttpUIAction {
	ChannelConfig config;
	BotModeConfig botMode;

	public HttpGetChannelBotModeAction(Activity activity, ChannelConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.botMode = MainActivity.connection.getChannelBotMode(this.config);
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
			MainActivity.botMode = botMode;
			
			((ChannelBotActivity)this.activity).resetView();
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;
		}
	}
	
}
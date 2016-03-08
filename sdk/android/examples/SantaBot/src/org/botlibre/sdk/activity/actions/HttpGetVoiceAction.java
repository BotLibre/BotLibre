package org.botlibre.sdk.activity.actions;

import android.app.Activity;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.VoiceConfig;

public class HttpGetVoiceAction extends HttpAction {
	InstanceConfig config;
	VoiceConfig voice;

	public HttpGetVoiceAction(Activity activity, InstanceConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.voice = MainActivity.connection.getVoice(this.config);
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	@Override
	public void onPostExecute(String xml) {
		if (this.exception != null) {
			return;
		}
		try {
			MainActivity.voice = this.voice;
		} catch (Exception error) {
			this.exception = error;
			return;
		}
	}
	
}
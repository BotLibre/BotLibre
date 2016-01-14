package org.botlibre.sdk.activity.actions;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.VoiceConfig;

import android.app.Activity;

public class HttpSaveVoiceAction extends HttpUIAction {

	VoiceConfig config;
	
	public HttpSaveVoiceAction(Activity activity, VoiceConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		MainActivity.connection.saveVoice(this.config);
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
		MainActivity.voice = this.config;
		
		this.activity.finish();
    }
}
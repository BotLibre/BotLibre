package org.botlibre.sdk.activity.actions;

import android.app.Activity;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.UserConfig;

public class HttpUpdateUserAction extends HttpUIAction {
	UserConfig config;

	public HttpUpdateUserAction(Activity activity, UserConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			MainActivity.connection.update(this.config);
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	@Override
	protected void onPostExecute(String xml) {
		super.onPostExecute(xml);
		if (this.exception != null) {
			return;
		}
		
		this.config.password = null;
		this.config.newPassword = null;
		
		this.activity.finish();
	}
	
}
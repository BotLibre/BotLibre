package org.botlibre.sdk.activity.actions;

import android.app.Activity;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.ViewUserActivity;
import org.botlibre.sdk.config.UserConfig;

public class HttpChangeUserIconAction extends HttpUIAction {

	UserConfig config;
	String file;
	
	public HttpChangeUserIconAction(Activity activity, String file, UserConfig config) {
		super(activity);
		this.config = config;
		this.file = file;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.config = MainActivity.connection.updateIcon(this.file, this.config);
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
			MainActivity.user = this.config;
			MainActivity.viewUser = this.config;
			((ViewUserActivity)this.activity).resetView();
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;
		}
    }
}
package org.botlibre.sdk.activity.actions;

import java.util.List;

import android.app.Activity;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.WebMediumUsersActivity;
import org.botlibre.sdk.config.WebMediumConfig;

public class HttpGetAdminsAction extends HttpUIAction {

	WebMediumConfig config;
	List<String> users;
	
	public HttpGetAdminsAction(Activity activity, WebMediumConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.users = MainActivity.connection.getAdmins(this.config);
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
			((WebMediumUsersActivity)this.activity).admins = this.users;
			((WebMediumUsersActivity)this.activity).resetView();
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;			
		}
	}
}
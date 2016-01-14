package org.botlibre.sdk.activity.actions;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.AvatarConfig;

import android.app.Activity;

public class HttpAddSharedAvatarAction extends HttpUIAction {

	AvatarConfig config;
	
	public HttpAddSharedAvatarAction(Activity activity, AvatarConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		MainActivity.connection.addSharedAvatar(this.config);
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
		this.activity.finish();
    }
}
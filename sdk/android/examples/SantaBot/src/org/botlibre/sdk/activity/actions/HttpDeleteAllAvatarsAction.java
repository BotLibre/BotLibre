package org.botlibre.sdk.activity.actions;

import org.botlibre.sdk.activity.AvatarActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.InstanceConfig;

import android.app.Activity;

public class HttpDeleteAllAvatarsAction extends HttpUIAction {

	InstanceConfig config;
	
	public HttpDeleteAllAvatarsAction(Activity activity, InstanceConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		MainActivity.connection.deleteAllAvatars(this.config);
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
		((AvatarActivity)this.activity).resetAvatars();
    }
}
package org.botlibre.sdk.activity.actions;

import android.app.Activity;

import org.botlibre.sdk.activity.AvatarActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.InstanceConfig;

public class HttpAddAvatarAction extends HttpUIAction {

	InstanceConfig config;
	String file;
	
	public HttpAddAvatarAction(Activity activity, String file, InstanceConfig config) {
		super(activity);
		this.config = config;
		this.file = file;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		MainActivity.connection.addAvatar(this.file, this.config);
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
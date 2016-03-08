package org.botlibre.sdk.activity.actions;

import android.app.Activity;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.UserConfig;

public class HttpFlagUserAction extends HttpUIAction {
	UserConfig config;

	public HttpFlagUserAction(Activity activity, UserConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		MainActivity.connection.flag(this.config);
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
	    //MainActivity.viewUser.isFlagged = true;
	    //this.activity.findViewById(R.id.flaggedLabel).setVisibility(View.VISIBLE);
	}
	
}
package org.botlibre.sdk.activity.war;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpUIAction;
import org.botlibre.sdk.config.InstanceConfig;

import android.app.Activity;

public class HttpChatWarAction extends HttpUIAction  {
	ChatWarConfig config;
	InstanceConfig instance;

	public HttpChatWarAction(Activity activity, ChatWarConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			InstanceConfig instance = new InstanceConfig();
			this.instance = (InstanceConfig)MainActivity.connection.custom("chat-war", this.config, instance);
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
		MainActivity.instance = this.instance;
	}
	
}
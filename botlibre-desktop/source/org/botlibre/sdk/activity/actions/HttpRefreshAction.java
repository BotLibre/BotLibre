package org.botlibre.sdk.activity.actions;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.GraphicConfig;

import android.app.Activity;

public class HttpRefreshAction extends HttpFetchAction {

	
	GraphicConfig instance;
	
	
	public HttpRefreshAction(Activity activity, GraphicConfig config) {
		super(activity, config);
		instance = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			this.config = MainActivity.connection.fetch(this.config);
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
			MainActivity.instance = this.config;
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;
		}
	}
	
}

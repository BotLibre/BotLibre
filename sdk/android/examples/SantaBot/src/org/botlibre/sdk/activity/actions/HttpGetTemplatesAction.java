package org.botlibre.sdk.activity.actions;

import java.util.List;

import android.app.Activity;

import org.botlibre.sdk.activity.MainActivity;

public class HttpGetTemplatesAction extends HttpAction {
	List<String> templates;

	public HttpGetTemplatesAction(Activity activity) {
		super(activity);
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.templates = MainActivity.connection.getTemplates();
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	public void postExecute(String xml) {
		if (this.exception != null) {
			return;
		}
		MainActivity.templates = this.templates.toArray();
	}
}
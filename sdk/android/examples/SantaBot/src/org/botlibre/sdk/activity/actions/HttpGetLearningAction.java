package org.botlibre.sdk.activity.actions;

import android.app.Activity;

import org.botlibre.sdk.activity.LearningActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.LearningConfig;

public class HttpGetLearningAction extends HttpUIAction {
	InstanceConfig config;
	LearningConfig learning;

	public HttpGetLearningAction(Activity activity, InstanceConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.learning = MainActivity.connection.getLearning(this.config);
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
			MainActivity.learning = this.learning;
			
			((LearningActivity)this.activity).resetView();
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;
		}
	}
	
}
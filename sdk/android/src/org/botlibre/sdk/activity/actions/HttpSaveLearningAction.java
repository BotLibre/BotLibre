package org.botlibre.sdk.activity.actions;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.LearningConfig;

import android.app.Activity;

public class HttpSaveLearningAction extends HttpUIAction {

	LearningConfig config;
	
	public HttpSaveLearningAction(Activity activity, LearningConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		MainActivity.connection.saveLearning(this.config);
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
		MainActivity.learning = this.config;
		
		this.activity.finish();
    }
}
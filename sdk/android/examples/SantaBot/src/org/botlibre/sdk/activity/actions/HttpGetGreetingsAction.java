package org.botlibre.sdk.activity.actions;

import java.util.List;

import android.app.Activity;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.TrainingActivity;
import org.botlibre.sdk.config.InstanceConfig;

public class HttpGetGreetingsAction extends HttpUIAction {

	InstanceConfig config;
	List<String> greetings;
	
	public HttpGetGreetingsAction(Activity activity, InstanceConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.greetings = MainActivity.connection.getGreetings(this.config);
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
			((TrainingActivity)this.activity).greetings = this.greetings;
			((TrainingActivity)this.activity).resetView();
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;			
		}
	}
}
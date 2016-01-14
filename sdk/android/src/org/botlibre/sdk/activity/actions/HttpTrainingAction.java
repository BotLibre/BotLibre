package org.botlibre.sdk.activity.actions;

import android.app.Activity;
import android.widget.EditText;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.TrainingActivity;
import org.botlibre.sdk.config.TrainingConfig;

public class HttpTrainingAction extends HttpUIAction {

	TrainingConfig config;
	
	public HttpTrainingAction(Activity activity, TrainingConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		MainActivity.connection.train(this.config);
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
		if (this.config.operation.equals("AddGreeting")) {
			((TrainingActivity)this.activity).greetings.add(this.config.response);
		} else if (this.config.operation.equals("RemoveGreeting")) {
			((TrainingActivity)this.activity).greetings.remove(this.config.response);
		} else if (this.config.operation.equals("AddDefaultResponse")) {
			((TrainingActivity)this.activity).defaultResponses.add(this.config.response);
		} else if (this.config.operation.equals("RemoveDefaultResponse")) {
			((TrainingActivity)this.activity).defaultResponses.remove(this.config.response);
		}
        EditText text = (EditText) this.activity.findViewById(R.id.greetingText);
		text.setText("");
        text = (EditText) this.activity.findViewById(R.id.defaultResponseText);
		text.setText("");
        text = (EditText) this.activity.findViewById(R.id.questionText);
		text.setText("");
        text = (EditText) this.activity.findViewById(R.id.responseText);
		text.setText("");
		((TrainingActivity)this.activity).resetView();
	}
}
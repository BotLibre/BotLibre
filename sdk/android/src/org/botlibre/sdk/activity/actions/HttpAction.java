package org.botlibre.sdk.activity.actions;

import android.app.Activity;
import android.os.AsyncTask;

public abstract class HttpAction extends AsyncTask<Void, Void, String> {	
	protected Activity activity;
	protected Exception exception;

	public HttpAction(Activity activity) {
		this.activity = activity;
	}
		
	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
}
package org.botlibre.sdk.activity.actions;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;

import org.botlibre.sdk.activity.MainActivity;

public abstract class HttpUIAction extends HttpAction {
	protected ProgressDialog dialog;

	public HttpUIAction(Activity activity) {
		super(activity);
	}
	
	@Override
	protected void onPreExecute() {
		this.dialog = new ProgressDialog(this.activity);
		this.dialog.setMessage("Processing..."); 
		this.dialog.show();
	}
	
	@Override
	protected void onPostExecute(String result) {
		try {
			if (this.dialog != null) {
				this.dialog.dismiss();
			}
			if (this.exception != null) {
				MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			}
		} catch (Exception exception) {
			Log.wtf("HttpUIAction", exception);
		}
	}
}
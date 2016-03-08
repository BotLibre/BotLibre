package org.botlibre.sdk.activity.actions;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.WebMediumConfig;

public class HttpDeleteAction extends HttpUIAction {
	WebMediumConfig config;

	public HttpDeleteAction(Activity activity, WebMediumConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		MainActivity.connection.delete(this.config);
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
        MainActivity.wasDelete = true;
        MainActivity.instances.remove(this.config);
        
    	SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
    	cookies.putString(this.config.getType(), null);
    	cookies.commit();
    	
		this.activity.finish();
    }
	
}
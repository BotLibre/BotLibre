package org.botlibre.sdk.activity.actions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.botlibre.sdk.activity.DomainActivity;
import org.botlibre.sdk.activity.InstanceActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.forum.ForumActivity;
import org.botlibre.sdk.activity.livechat.ChannelActivity;
import org.botlibre.sdk.config.ChannelConfig;
import org.botlibre.sdk.config.DomainConfig;
import org.botlibre.sdk.config.ForumConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.WebMediumConfig;

public class HttpCreateAction extends HttpUIAction {
	
	WebMediumConfig config;

	public HttpCreateAction(Activity activity, WebMediumConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.config = MainActivity.connection.create(this.config);
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onPostExecute(String xml) {
		super.onPostExecute(xml);
		if (this.exception != null) {
			return;
		}
		try {
			MainActivity.instance = this.config;
			
			this.activity.finish();

	    	SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
	    	cookies.putString(this.config.getType(), this.config.name);
	    	cookies.commit();

        	Class childActivity = null;
        	if (this.config instanceof ChannelConfig) {
        		childActivity = ChannelActivity.class;
        	} else if (this.config instanceof ForumConfig) {
        		childActivity = ForumActivity.class;
        	} else if (this.config instanceof InstanceConfig) {
        		childActivity = InstanceActivity.class;
        	} else if (this.config instanceof DomainConfig) {
        		childActivity = DomainActivity.class;
        	}
	        Intent intent = new Intent(this.activity, childActivity);	
	        this.activity.startActivity(intent);
	        
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;
		}
	}

}
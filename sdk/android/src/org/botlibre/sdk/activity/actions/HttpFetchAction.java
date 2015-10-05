package org.botlibre.sdk.activity.actions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.botlibre.sdk.activity.ChatActivity;
import org.botlibre.sdk.activity.DomainActivity;
import org.botlibre.sdk.activity.InstanceActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.forum.ForumActivity;
import org.botlibre.sdk.activity.livechat.ChannelActivity;
import org.botlibre.sdk.activity.livechat.LiveChatActivity;
import org.botlibre.sdk.config.ChannelConfig;
import org.botlibre.sdk.config.DomainConfig;
import org.botlibre.sdk.config.ForumConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.WebMediumConfig;

public class HttpFetchAction extends HttpUIAction {
	WebMediumConfig config;
	boolean launch;

	public HttpFetchAction(Activity activity, WebMediumConfig config) {
		super(activity);
		this.config = config;
	}
	
	public HttpFetchAction(Activity activity, WebMediumConfig config, boolean launch) {
		super(activity);
		this.config = config;
		this.launch = launch;
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

	@SuppressWarnings("rawtypes")
	@Override
	public void onPostExecute(String xml) {
		super.onPostExecute(xml);
		if (this.exception != null) {
			return;
		}
		try {
			MainActivity.instance = this.config;
			
        	SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
        	cookies.putString(this.config.getType(), this.config.name);
        	cookies.commit();

        	Class childActivity = null;
        	if (this.launch && !this.config.isExternal) {
	        	if (this.config instanceof ChannelConfig) {
	        		childActivity = LiveChatActivity.class;
	        	} else if (this.config instanceof ForumConfig) {
	        		childActivity = ForumActivity.class;
	        	} else if (this.config instanceof InstanceConfig) {
	        		childActivity = ChatActivity.class;
	        		
	        		HttpAction action = new HttpGetVoiceAction(this.activity, (InstanceConfig)MainActivity.instance.credentials());
	        		action.execute();
	        	} else if (this.config instanceof DomainConfig) {
	        		childActivity = DomainActivity.class;
	        		MainActivity.connection.setDomain((DomainConfig)this.config);
	    			MainActivity.domain = (DomainConfig)this.config;
	    			MainActivity.tags = null;
	    			MainActivity.categories = null;
	    			MainActivity.forumTags = null;
	    			MainActivity.forumCategories = null;
	    			MainActivity.channelTags = null;
	    			MainActivity.channelCategories = null;

	    			MainActivity.type = MainActivity.defaultType;	    			
	    	        Intent intent = new Intent(this.activity, MainActivity.class);
	    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    			this.activity.startActivity(intent);
	    			return;
	        	}
        	} else {
	        	if (this.config instanceof ChannelConfig) {
	        		childActivity = ChannelActivity.class;
	        	} else if (this.config instanceof ForumConfig) {
	        		childActivity = ForumActivity.class;
	        	} else if (this.config instanceof InstanceConfig) {
	        		childActivity = InstanceActivity.class;
	        		
	        		HttpAction action = new HttpGetVoiceAction(this.activity, (InstanceConfig)MainActivity.instance.credentials());
	        		action.execute();
	        	} else if (this.config instanceof DomainConfig) {
	        		childActivity = DomainActivity.class;
	        		MainActivity.connection.setDomain((DomainConfig)this.config);
	    			MainActivity.domain = (DomainConfig)this.config;
	    			MainActivity.tags = null;
	    			MainActivity.categories = null;
	    			MainActivity.forumTags = null;
	    			MainActivity.forumCategories = null;
	    			MainActivity.channelTags = null;
	    			MainActivity.channelCategories = null;
	        	}
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
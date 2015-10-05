package org.botlibre.sdk.activity.actions;

import java.util.List;

import android.app.Activity;
import android.content.Intent;

import org.botlibre.sdk.activity.ChooseBotActivity;
import org.botlibre.sdk.activity.ChooseDomainActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.forum.ChooseForumActivity;
import org.botlibre.sdk.activity.livechat.ChooseChannelActivity;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.WebMediumConfig;

public class HttpGetInstancesAction extends HttpUIAction {
	BrowseConfig config;
	List<WebMediumConfig> instances;

	public HttpGetInstancesAction(Activity activity, BrowseConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.instances = MainActivity.connection.browse(config);
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
		MainActivity.instances = this.instances;
		
		if (config.type.equals("Bot")) {
	        Intent intent = new Intent(this.activity, ChooseBotActivity.class);		
	        this.activity.startActivity(intent);
		} else if (config.type.equals("Forum")) {
	        Intent intent = new Intent(this.activity, ChooseForumActivity.class);		
	        this.activity.startActivity(intent);
		} else if (config.type.equals("Channel")) {
	        Intent intent = new Intent(this.activity, ChooseChannelActivity.class);		
	        this.activity.startActivity(intent);
		} else if (config.type.equals("Domain")) {
	        Intent intent = new Intent(this.activity, ChooseDomainActivity.class);		
	        this.activity.startActivity(intent);
		}
	}
}
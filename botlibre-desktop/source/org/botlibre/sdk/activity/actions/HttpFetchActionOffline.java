/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package org.botlibre.sdk.activity.actions;

import org.botlibre.sdk.activity.ChatActivity;
import org.botlibre.sdk.activity.ListTemplateView;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.livechat.LiveChatActivity;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ChannelConfig;
import org.botlibre.sdk.config.DomainConfig;
import org.botlibre.sdk.config.ForumConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.WebMediumConfig;
import org.botlibre.sdk.micro.MicroConnection;

import android.app.Activity;
//import android.app.LocalActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class HttpFetchActionOffline extends HttpUIAction {
	WebMediumConfig config;
	MicroConnection microConnection = new MicroConnection();
	boolean launch;

	public HttpFetchActionOffline(Activity activity, WebMediumConfig config) {
		super(activity);
		this.config = config;
	}

	
	public HttpFetchActionOffline(Activity activity, WebMediumConfig config, boolean launch) {
		super(activity);
		this.config = config;
		this.launch = launch;
	}
	
	@Override
	protected void onPreExecute() {
		this.dialog = new ProgressDialog(this.activity);
		this.dialog.setCancelable(false);
		this.dialog.setMessage("Initializing Bot"); 
		this.dialog.show();
	}

	@Override
	protected String doInBackground(Void... params) {
	
		try {
			//switch connections.
			MainActivity.setOnline(false);
			microConnection = (MicroConnection)MainActivity.connection;
			this.config = microConnection.fetch(this.config);
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}
	
	public void superOnPostExecute(String xml) {
		super.onPostExecute(xml);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onPostExecute(String xml) {
		super.onPostExecute(xml);
		if(this.activity instanceof ListTemplateView){
			this.activity.finish();
		}
		
//		if (this.exception != null) {
//			return;
//		}
		try {
			MainActivity.instance = this.config;
        	Class childActivity = MainActivity.getActivity(this.config);
        	if (this.launch && !this.config.isExternal) {
	        	if (this.config instanceof InstanceConfig) {
	        		childActivity = ChatActivity.class;
//	        		HttpAction action = new HttpGetVoiceAction(this.activity, (InstanceConfig)MainActivity.instance.credentials());
//	        		action.execute();
	        	}
	        }
	        Intent intent = new Intent(this.activity, childActivity);
	        this.activity.startActivity(intent);
		} catch (Exception error) {
			this.exception = error;
//			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;
		}
	}
	
}
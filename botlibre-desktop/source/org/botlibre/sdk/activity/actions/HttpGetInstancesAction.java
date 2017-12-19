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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;

import org.botlibre.sdk.activity.BrowseActivity;
import org.botlibre.sdk.activity.BrowseDomainActivity;
import org.botlibre.sdk.activity.CategoryListAdapter;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.avatar.BrowseAvatarActivity;
import org.botlibre.sdk.activity.forum.BrowseForumActivity;
//import org.botlibre.sdk.activity.graphic.BrowseGraphicActivity;
import org.botlibre.sdk.activity.livechat.BrowseChannelActivity;
//import org.botlibre.sdk.activity.script.BrowseScriptActivity;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ContentConfig;
//import org.botlibre.sdk.config.ScriptConfig;
import org.botlibre.sdk.config.WebMediumConfig;

import android.app.Activity;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
public class HttpGetInstancesAction extends HttpUIAction {
	ListView llview;

	BrowseConfig config;
	List<WebMediumConfig> instances;
	boolean finish = false;
	boolean CustomAvatar = false;

	public HttpGetInstancesAction(Activity activity, BrowseConfig config) {
		super(activity);
		this.config = config;
	}

	public HttpGetInstancesAction(Activity activity, BrowseConfig config, boolean finish) {
		super(activity);
		this.config = config;
		this.finish = finish;
	}
	public HttpGetInstancesAction(Activity activity, BrowseConfig config, boolean finish, boolean CustomAvatar) {
		super(activity);
		this.config = config;
		this.finish = finish;
		this.CustomAvatar = CustomAvatar;
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
		
		
		
		if (this.finish) {
			this.activity.finish();
		}
		Intent intent = null;
		MainActivity.browse = this.config;
		if (config.type.equals("Forum")) {
	        intent = new Intent(this.activity, BrowseForumActivity.class);
		} else if (config.type.equals("Channel")) {
	        intent = new Intent(this.activity, BrowseChannelActivity.class);
		} else if (config.type.equals("Domain")) {
	        intent = new Intent(this.activity, BrowseDomainActivity.class);
		} else if (config.type.equals("Avatar")) {
	        intent = new Intent(this.activity, BrowseAvatarActivity.class);
		} else if (config.type.equals("Graphic")) {
//			intent = new Intent(this.activity, BrowseGraphicActivity.class);
			if(this.CustomAvatar){
				//only happens when CustomAvatar is called
				intent.putExtra("dataName", "Custom Avatar");
				intent.putExtra("dataCustomAvatar", true);
			}else{intent.putExtra("dataCustomAvatar", false);}
	        
		} else if (config.type.equals("Script")) {
			if (MainActivity.importingBotScript) {
				List<WebMediumConfig> onlyScripts = new ArrayList<WebMediumConfig>();
				
				for (int i = 0; i < MainActivity.instances.size(); i++) {
//					ScriptConfig config = (ScriptConfig) MainActivity.instances.get(i);
//					if (config.categories.contains("Self") || config.categories.contains("AIML")) {
//						onlyScripts.add(config);
//					}
				}
				MainActivity.instances = onlyScripts;
			} else if (MainActivity.importingBotLog) {
				List<WebMediumConfig> onlyScripts = new ArrayList<WebMediumConfig>();
				
				for (int i = 0; i < MainActivity.instances.size(); i++) {
//					ScriptConfig config = (ScriptConfig) MainActivity.instances.get(i);
//					if (config.categories.contains("Log") || config.categories.contains("AIML") || config.categories.contains("Response")) {
//						onlyScripts.add(config);
					}
				}
//				MainActivity.instances = onlyScripts;
			}
//			intent = new Intent(this.activity, BrowseScriptActivity.class);
//		} else {
//	        intent = new Intent(this.activity, BrowseActivity.class);
//		}		
		
		//for result only happens when RequestActivity calls it.
		if(this.CustomAvatar){
			this.activity.startActivityForResult(intent, 1);}else{
        this.activity.startActivity(intent);}
	}
}
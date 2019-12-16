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
package org.botlibre.sdk.activity;

import java.util.ArrayList;
import java.util.List;

import org.botlibre.knowledge.micro.MicroMemory;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchActionOffline;
import org.botlibre.sdk.activity.actions.HttpGetCategoriesAction;
import org.botlibre.sdk.activity.actions.HttpGetTemplatesAction;
import org.botlibre.sdk.activity.avatar.AvatarSelection;
import org.botlibre.sdk.activity.avatar.GetAvatarAction;
import org.botlibre.sdk.config.ContentConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.OfflineTemplateConfig;
import org.botlibre.sdk.config.WebMediumConfig;
import org.botlibre.sdk.micro.MicroConnection;

import org.botlibre.offline.pizzabot.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListTemplateView extends CreateWebMediumActivity {
	public static boolean offlineTemplate = false;
	List<OfflineTemplateConfig> items;
	final static String listOfBots [] = {"Empty" , "Basic", "Personal Assistance"};
	final static int imagesId [] = {R.drawable.bot, R.drawable.bot , R.drawable.bot};
	final static String listOfDec [] = {"A completely empty template loaded with no knowledge or scripts.",
			"A basic bot template with only responses to common greetings and farewells (hello, goodbye, etc.), and the default bootstrap Self language scripts that understand basic language, 'what is' and 'where is' questions, names, dates, math, and topical questions.",
			"Template for a Virtual Assistant. This template has command scripts for performing common tasks on Android such as opening apps, scheduling appointments, and send email."};
	
	ArrayAdapter adapter;
	ListView llview;
	Intent data = new Intent();
	String temp;
	TextView txt;
	@Override
	public String getType() {
		return "Bot";
	}
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_view);
		llview = (ListView) findViewById(R.id.theListView);
		txt=(TextView) findViewById(R.id.theTitle);
		txt.setText("Select Template");
		
		
		if(offlineTemplate){
			items = retriveTemplates();
			
			adapter = new CustomListViewAdapter(this, R.layout.list_item_imager, items);
			llview.setAdapter(adapter);
			
			llview.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					WebMediumConfig config = new InstanceConfig();
					OfflineTemplateConfig templates = (OfflineTemplateConfig) (llview.getItemAtPosition(arg2));
					//saving a template number for getting the icons and pictures of the bot
					saveAllData(MainActivity.launchInstanceName = templates.getTitle(), MainActivity.launchInstanceId = templates.getId(), MainActivity.templateID = arg2);
					config.id = MainActivity.launchInstanceId;
					config.name = MainActivity.launchInstanceName;
					AvatarSelection.saveSelectedAvatar(templates.getTitle());
					MainActivity.readZipAvatars(ListTemplateView.this, templates.getTitle());
					MainActivity.offlineSelectedImage = templates.getImageId();
					HttpAction action = new HttpFetchActionOffline(ListTemplateView.this, config, true);
					action.execute();
				}
			});
			offlineTemplate = false;
			return;
		}
		

		
		adapter = new ImageListAdapter(this, R.layout.image_list, (List) MainActivity.getAllTemplates(this));
		llview.setAdapter(adapter);
		
		llview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				InstanceConfig templates = (InstanceConfig) (llview.getItemAtPosition(arg2));
				Toast.makeText(ListTemplateView.this, templates.name + " Selected", Toast.LENGTH_SHORT).show();
				data.putExtra("template", templates.name);
				setResult(RESULT_OK,data);
				finish();
			}
		});
		
		HttpAction action = new HttpGetTemplatesAction(this);
    	action.execute();
		
	}
	
	public void saveAllData(String instanceId, String instanceName, int id){
		SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
		cookies.putString("instanceID", instanceId);
		cookies.putString("instanceName", instanceName);
		cookies.putInt("tempId", id);
		cookies.commit();
	}

	public static List<OfflineTemplateConfig> retriveTemplates(){
		List<OfflineTemplateConfig> items = new ArrayList<OfflineTemplateConfig>();
		for (int i = 0; i < listOfBots.length; i++) {
			OfflineTemplateConfig item = new OfflineTemplateConfig(imagesId[i],listOfBots[i],listOfDec[i],""+i,i);
			items.add(item);
		}
		return items;
	}

}

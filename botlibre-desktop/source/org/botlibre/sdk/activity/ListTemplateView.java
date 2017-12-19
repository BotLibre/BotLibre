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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.Timer;

import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchActionOffline;
import org.botlibre.sdk.activity.actions.HttpGetCategoriesAction;
import org.botlibre.sdk.activity.actions.HttpGetTemplatesAction;
import org.botlibre.sdk.activity.avatar.AvatarSelection;
import org.botlibre.sdk.config.ContentConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.OfflineTemplateConfig;
import org.botlibre.sdk.config.WebMediumConfig;
import org.botlibre.sdk.micro.ListRender;
import org.botlibre.sdk.micro.Preferences;

import org.botlibre.sdk.R;

import android.app.Activity;
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
	final static String listOfBots [] = {"Empty" , "Basic", "AI", "Julie", "Eddie","Alice"};
	final static String listOfDec [] = {" A completely empty template loaded with no knowledge or scripts.",
			" A basic bot template with only responses to common greetings and farewells (hello, goodbye, etc.), and the default bootstrap Self language scripts that understand basic language, 'what is' and 'where is' questions, names, dates, math, and topical questions.",
			" This template has learning and comprehension enabled to let the bot learn from users. This uses advanced and somewhat experimental artificial intelligence. Be careful using this template, as other users may train your bot in <br>undesirable ways, and the bot make take longer to respond.",
			" Template of the Julie chat bot.",
			" A template of the Eddie virtual boyfriend bot.", 
			" A template of the ALICE AIML bot." ,
			" Template for a Virtual Assistant. This template has command scripts for performing common tasks on Android such as opening apps, scheduling appointments, and send email.",
			" A template that combines a mobile virtual assistant, and the Julie chatbot personality."};
	
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_view);
		llview = (ListView) findViewById(R.id.theListView);
		txt=(TextView) findViewById(R.id.theTitle);
		txt.setText("Select Template");
		
		
//		if(offlineTemplate){
//			items = retriveTemplates();
//			
//			adapter = new CustomListViewAdapter(this, R.layout.list_item_imager, items);
//			llview.setAdapter(adapter);
//			
//			offlineTemplate = false;
//			return;
//		}
		if(offlineTemplate){
			ListRender lr = new ListRender();
			lr.imageMap = lr.createImageMap(listOfBots);
			llview.setList(listOfBots);
			llview.setCellRender(lr);
			offlineTemplate = false;
			return;
		}
		

		
		adapter = new ImageListAdapter(this, R.layout.image_list, MainActivity.getAllTemplates(this));
		llview.setAdapter(adapter);
		
		llview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				InstanceConfig templates = (InstanceConfig) (llview.getItemAtPosition(arg2));
				
//				data.putExtra("template", templates.name);
				finish();
			}
		});
		
		HttpAction action = new HttpGetTemplatesAction(this);
    	action.execute();
		
	}
	public void chatNow(View v){
		v.getComponent().setVisible(false);
		try{
		WebMediumConfig config = new InstanceConfig();
		System.out.println("this is "+(llview.getItemAtPosition(llview.getCheckedItemPosition())));
		String name = (String) (llview.getItemAtPosition(llview.getCheckedItemPosition()));
		new Toast(Activity.active.frame, true,name +" Selected").showToast(Toast.LENGTH_SHORT);
		MainActivity.launchInstanceName = name;
		System.out.println("The ID number is : "+ llview.getCheckedItemPosition());
		MainActivity.launchInstanceId = ""+llview.getCheckedItemPosition();
		config.id = MainActivity.launchInstanceId;
		config.name = MainActivity.launchInstanceName;
		AvatarSelection.saveSelectedAvatar(name);
		MainActivity.readZipAvatars(ListTemplateView.this, name);
		Preferences pref = MainActivity.savePref(MainActivity.launchInstanceName, MainActivity.launchInstanceId,name);
		MainActivity.writeObject(pref);
		MainActivity.offlineSelectedImage = name;
		HttpAction action = new HttpFetchActionOffline(ListTemplateView.this, config, true);
		action.execute();
		}catch(Exception e){v.getComponent().setVisible(true);}
        
	}

}

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
package org.botlibre.sdk.activity.avatar;

import org.botlibre.sdk.activity.EmotionalState;
import org.botlibre.sdk.activity.LibreActivity;

import org.botlibre.offline.pizzabot.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public class AvatarTestItemSelection extends LibreActivity{
	ListView llview;
	TextView title;
	Intent data = new Intent();
	String typeOfList;
	ArrayAdapter<String> adapter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_view);
		
		String[] values = new String[EmotionalState.values().length];
		for (int index = 0; index < EmotionalState.values().length; index++) {
			values[index] = EmotionalState.values()[index].name().toLowerCase();
		}
		
		
		//getting info TYPE from caller class
    	Bundle bundle = getIntent().getExtras();
    	typeOfList = bundle.getString("type");
    	title = (TextView) findViewById(R.id.theTitle);
		llview = (ListView) findViewById(R.id.theListView);
		if(typeOfList.equals("action")){
			title.setText("Select Action");
			adapter = new ArrayAdapter<String>(this,
	                android.R.layout.select_dialog_item, new String[]{
	                	"smile",
	                	"frown",
	                	"laugh",
	                	"scream",
	                	"sit",
	                	"jump",
	                	"bow",
	                	"nod",
	                	"shake-head",
	                	"slap",
	        			"kiss",
	        			"burp",
	        			"fart"
				    
	        });
			llview.setAdapter(adapter);
		}else if(typeOfList.equals("pose")){
			title.setText("Select Pose");
			adapter = new ArrayAdapter<String>(this,
	                android.R.layout.select_dialog_item, new String[]{
	                	"sitting",
	                	"lying",
	                	"walking",
	                	"running",
	                	"jumping",
	                	"fighting",
	                	"sleeping",
	                	"dancing"
				    
	        });
			llview.setAdapter(adapter);
		}else if(typeOfList.equals("lang")){
			title.setText("Select Language");
			adapter = new ArrayAdapter<String>(this,
	                android.R.layout.select_dialog_item, new String[]{
	                	"en-US",
	                	"en-GB",
	                	"fr",
	                	"es",
	                	"it",
	                	"de",
	                	"pt",
	                	"ru",
	                	"zh",
	                	"ja",
	                	"te"
				    
	        });
			llview.setAdapter(adapter);
		}else if(typeOfList.equals("emotions")){
			
			title.setText("Select Emotions");
			adapter = new ArrayAdapter<String>(this,
	                android.R.layout.select_dialog_item, values);
			llview.setAdapter(adapter);
		}
		
		
    	llview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				data.putExtra("info", (String) llview.getItemAtPosition(arg2));
				setResult(RESULT_OK,data);
				finish();
			}
		});
	}
}

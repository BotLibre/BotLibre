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

import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpGetCategoriesAction;
import org.botlibre.sdk.config.ContentConfig;

import android.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ListCategoriesView extends CreateWebMediumActivity {
	ListView llview;
	String types;
	Intent data = new Intent();
	TextView txt;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_view);
		
		
		
		Bundle bundle = getIntent().getExtras();
    	setType(bundle.getString("type"));
    	
    	
    	
		llview = (ListView) findViewById(R.id.theListView);
		txt = (TextView) findViewById(R.id.theTitle);
		
		
		txt.setText("Select Categories");
		llview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				//Getting Info from the main class of the categories.EX name, icon, ...etc
				ContentConfig categories = (ContentConfig) (llview.getItemAtPosition(arg2));
				//using categories to pass the data for all
				new Toast(Activity.active.frame, true,categories.name  + " Selected").showToast(Toast.LENGTH_SHORT);
				data.putExtra("cat", categories.name);
//				setResult(RESULT_OK,data);
				finish();
			}
		});
		HttpAction action = new HttpGetCategoriesAction(this, getType());
    	action.execute();
		
	}
	@Override
	public String getType() {
		return types;
	}
	public void setType(String type){this.types = type;}
	

}

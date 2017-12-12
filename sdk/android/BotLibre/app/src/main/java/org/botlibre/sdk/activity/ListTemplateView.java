/******************************************************************************
 *
 *  Copyright 2017 Paphus Solutions Inc.
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

import java.util.List;

import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpGetTemplatesAction;
import org.botlibre.sdk.config.InstanceConfig;

import org.botlibre.sdk.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListTemplateView extends CreateWebMediumActivity {
	
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

		adapter = new ImageListAdapter(this,
                R.layout.image_list, (List) MainActivity.getAllTemplates(this));
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

}

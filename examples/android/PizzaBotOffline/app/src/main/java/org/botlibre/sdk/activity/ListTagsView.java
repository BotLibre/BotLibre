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
import org.botlibre.sdk.activity.actions.HttpGetTagsAction;
import org.botlibre.offline.pizzabot.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListTagsView extends CreateWebMediumActivity {
	ListView llview;
	Intent data = new Intent();
	String type;
	TextView txt;
	@Override
	public String getType() {
		return type;
	}
	public void setType(String type){
		this.type = type;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_view);
		
		//getting info TYPE from caller class
    	Bundle bundle = getIntent().getExtras();
    	setType(bundle.getString("type"));
		
    	
    	
    	
		llview = (ListView) findViewById(R.id.theListView);
    	txt = (TextView) findViewById(R.id.theTitle);
    	
    	
    	txt.setText("Select Tags");
    	
		llview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Toast.makeText(ListTagsView.this, llview.getItemAtPosition(arg2)  + " Selected", Toast.LENGTH_SHORT).show();
				data.putExtra("tag", llview.getItemAtPosition(arg2)+"");
				setResult(RESULT_OK,data);
				finish();
			}
		});
    	
    	HttpAction action = new HttpGetTagsAction(this, getType());
    	action.execute();
  
	}
}

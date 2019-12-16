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

import org.botlibre.offline.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListLicenseView extends CreateWebMediumActivity {

	@Override
	public String getType() {
		return "Bot";
	}
	ListView llview;
	Intent data= new Intent();
	String [] listS = {
			"Copyright " + MainActivity.user.user + " all rights reserved",
			"Public Domain", "Creative Commons Attribution 3.0 Unported License",
			"GNU General Public License 3.0",
			"Apache License, Version 2.0",
			"Eclipse Public License 1.0"
};
	TextView txt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_view);
		llview = (ListView) findViewById(R.id.theListView);
		txt = (TextView) findViewById(R.id.theTitle);
		
		txt.setText("Select License");
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ArrayAdapter adapter = new ArrayAdapter(this,
		android.R.layout.select_dialog_item, listS);
		llview.setAdapter(adapter);
		
		
		
		llview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Toast.makeText(ListLicenseView.this, listS[arg2] + " Selected", Toast.LENGTH_SHORT).show();
				data.putExtra("temp", listS[arg2]);
				setResult(RESULT_OK,data);
				finish();
			}
		});
	}
}

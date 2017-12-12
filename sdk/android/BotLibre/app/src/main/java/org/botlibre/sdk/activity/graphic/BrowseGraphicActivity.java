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
package org.botlibre.sdk.activity.graphic;

import org.botlibre.sdk.activity.BrowseActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchAction;
import org.botlibre.sdk.config.GraphicConfig;
import org.botlibre.sdk.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


public class BrowseGraphicActivity extends BrowseActivity{
	private boolean CustomAvatar = false;
	private String titleName = "";
	
	//used in CustomAvatarActivity/ prevent from a  bug.
	public static boolean selected = false;
	public Intent data = new Intent();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		try{
    	CustomAvatar = bundle.getBoolean("dataCustomAvatar");
		}catch(Exception e){MainActivity.showMessage(e.getMessage(), this);}
    	if(CustomAvatar){
    	
    		titleName = bundle.getString("dataName");
    		TextView title = (TextView) findViewById(R.id.title);
    		title.setText(titleName);
    		findViewById(R.id.menuButton).setVisibility(View.GONE);
    		findViewById(R.id.searchButton).setVisibility(View.GONE);
    		final ListView llview = (ListView) findViewById(R.id.instancesList);
    		llview.setOnItemClickListener(new OnItemClickListener() {

    			@Override
    			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    				//Getting Info from the main class of the categories.EX name, icon, ...etc
    				select(arg1);
    				setResult(RESULT_OK,data);
    			}
    		});
    	}
    	
	}
	@Override
	public String getType() {
		return "Graphic";
	} 

	
	public void select(View view){
		ListView list = (ListView) findViewById(R.id.instancesList);
		int index = list.getCheckedItemPosition();
		this.instance = this.instances.get(index); 

		GraphicConfig config = new GraphicConfig();
		//setting the graphicconfig's instance to be the bot id
		if (MainActivity.instance != null) {
			config.instance = MainActivity.instance.id;
		}
		//setting the graphicconfig id to be the selected script
		config.id = this.instance.id;

		
		//calling for higher res picture from GraphicConfig
		HttpAction action = new ImageFetchAction(this, config);
		action.execute();
		

	}
	
	@Override
	public void selectInstance(View view){
		ListView list = (ListView) findViewById(R.id.instancesList);
		int index = list.getCheckedItemPosition();
		this.instance = this.instances.get(index); 

		GraphicConfig config = new GraphicConfig();
		//setting the graphicconfig's instance to be the bot id
		if (MainActivity.instance != null) {
			config.instance = MainActivity.instance.id;
		}
		//setting the graphicconfig id to be the selected script
		config.id = this.instance.id;

		HttpAction action = new HttpFetchAction(this, config);
		action.execute();
		

	}
}

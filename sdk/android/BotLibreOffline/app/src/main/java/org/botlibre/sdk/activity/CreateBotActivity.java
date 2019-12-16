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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import org.botlibre.offline.R;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpCreateAction;
import org.botlibre.sdk.config.InstanceConfig;

/**
 * Activity for creating a new bot instance.
 */
public class CreateBotActivity extends CreateWebMediumActivity {

	@Override
	public String getType() {
		return "Bot";
	}

	private EditText templateText; 
	private ImageButton btnTemp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_bot);
        
        
        btnTemp= (ImageButton)findViewById(R.id.btnTemp);
        templateText = (EditText) findViewById(R.id.templateText);
        
        resetView();
        
        btnTemp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(CreateBotActivity.this,ListTemplateView.class);
				startActivityForResult(i,2);
			}
		});
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case 2:
			if(resultCode==RESULT_OK){
				templateText.setText(data.getExtras().getString("template"));
			}
			break;
		}
	}
    
    /**
     * Create the instance.
     */
    public void create(View view) {
    	InstanceConfig instance = new InstanceConfig();
    	saveProperties(instance);
    	
    	EditText text = (EditText) findViewById(R.id.templateText);
    	instance.template = text.getText().toString().trim();
    	
		
        HttpAction action = new HttpCreateAction(this, instance);
        action.execute();
    }
}

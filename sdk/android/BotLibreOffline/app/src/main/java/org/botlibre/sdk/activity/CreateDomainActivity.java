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

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.botlibre.offline.R;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpCreateAction;
import org.botlibre.sdk.config.DomainConfig;

/**
 * Activity for creating a new domain.
 */
public class CreateDomainActivity extends CreateWebMediumActivity {
	
	@Override
	public String getType() {
		return "Domain";
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_domain);

        resetView();

        Spinner spin = (Spinner) findViewById(R.id.creationModeSpin);
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.accessModes);
		spin.setAdapter(adapter);
	}
    
    /**
     * Create the instance.
     */
    public void create(View view) {
    	DomainConfig instance = new DomainConfig();
    	saveProperties(instance);
    	
    	Spinner creationSpin = (Spinner) findViewById(R.id.creationModeSpin);
    	instance.creationMode = (String)creationSpin.getSelectedItem();

        HttpAction action = new HttpCreateAction(this, instance);
        action.execute();
    }
}

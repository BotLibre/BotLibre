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

package org.botlibre.sdk.activity.livechat;

import java.util.Arrays;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.EditWebMediumActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpUpdateAction;
import org.botlibre.sdk.config.ChannelConfig;

/**
 * Activity for editing a channel's details.
 */
public class EditChannelActivity extends EditWebMediumActivity {

	@Override
	public String getType() {
		return "Channel";
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_channel);

		ChannelConfig instance = (ChannelConfig)MainActivity.instance;
		
		resetView();

		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.channelTypes);
		spin.setAdapter(adapter);
		spin.setSelection(Arrays.asList(MainActivity.channelTypes).indexOf(instance.type));

		Spinner videoAccessModeSpin = (Spinner) findViewById(R.id.videoAccessModeSpin);
		adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.mediaAccessModes);
		videoAccessModeSpin.setAdapter(adapter);
		videoAccessModeSpin.setSelection(Arrays.asList(MainActivity.mediaAccessModes).indexOf(instance.videoAccessMode));

		Spinner audioAccessModeSpin = (Spinner) findViewById(R.id.audioAccessModeSpin);
		adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.mediaAccessModes);
		audioAccessModeSpin.setAdapter(adapter);
		audioAccessModeSpin.setSelection(Arrays.asList(MainActivity.mediaAccessModes).indexOf(instance.audioAccessMode));
	}

    public void save(View view) {
    	ChannelConfig instance = new ChannelConfig();
    	saveProperties(instance);

    	Spinner spin = (Spinner) findViewById(R.id.typeSpin);
    	instance.type = (String)spin.getSelectedItem();
    	
    	spin = (Spinner) findViewById(R.id.videoAccessModeSpin);
    	instance.videoAccessMode = (String)spin.getSelectedItem();
    	
    	spin = (Spinner) findViewById(R.id.audioAccessModeSpin);
    	instance.audioAccessMode = (String)spin.getSelectedItem();
        
    	HttpAction action = new HttpUpdateAction(this, instance);
        action.execute();
    }
}

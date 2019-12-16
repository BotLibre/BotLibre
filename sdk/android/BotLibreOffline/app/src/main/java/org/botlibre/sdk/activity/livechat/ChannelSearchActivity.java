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

import org.botlibre.sdk.activity.SearchActivity;

import org.botlibre.offline.R;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;

/**
 * Browse activity for searching for a channel.
 */
public class ChannelSearchActivity extends SearchActivity {

	@SuppressWarnings("rawtypes")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		RadioButton radio = (RadioButton)findViewById(R.id.personalRadio);
		radio.setText("My Channels");
    	
		Spinner sortSpin = (Spinner) findViewById(R.id.sortSpin);
		@SuppressWarnings("unchecked")
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, new String[]{
        			"connects",
        			"connects today",
        			"connects this week",
        			"connects this month",
        			"last connect",
        			"name",
        			"date",
            		"messages",
            		"users online",
            		"thumbs up",
            		"thumbs down",
            		"stars"
        });
		sortSpin.setAdapter(adapter);
	}
	
	public String getType() {		
		return "Channel";
	}
}

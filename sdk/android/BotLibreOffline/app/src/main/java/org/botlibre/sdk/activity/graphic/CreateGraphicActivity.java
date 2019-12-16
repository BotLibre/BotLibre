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
package org.botlibre.sdk.activity.graphic;

import org.botlibre.sdk.activity.CreateWebMediumActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpCreateAction;
import org.botlibre.sdk.config.GraphicConfig;

import org.botlibre.offline.R;
import android.os.Bundle;
import android.view.View;

import android.widget.Spinner;

public class CreateGraphicActivity extends CreateWebMediumActivity {

		
	@Override
	public String getType() {
		return "Graphic";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_graphic);
        resetView();
	}
	 /**
     * Create the instance.
     */
    public void create(View view) {
    	GraphicConfig instance = new GraphicConfig();
    	saveProperties(instance);
    	
		HttpAction action = new HttpCreateAction(this, instance);
        action.execute();
    }
}

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
import org.botlibre.sdk.activity.actions.HttpGetTagsAction;
import org.botlibre.sdk.config.WebMediumConfig;

import org.botlibre.sdk.R;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Generic activity for creating new content.
 */
public abstract class CreateWebMediumActivity extends LibreActivity {
	
	public abstract String getType();
		
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetView() {
        
        final AutoCompleteTextView licenseText = (AutoCompleteTextView)findViewById(R.id.licenseText);
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.select_dialog_item, new String[]{
        			"Copyright " + MainActivity.user.user + " all rights reserved",
        			"Public Domain", "Creative Commons Attribution 3.0 Unported License",
        			"GNU General Public License 3.0",
        			"Apache License, Version 2.0",
        			"Eclipse Public License 1.0"
			    
        });
        licenseText.setThreshold(0);
        licenseText.setAdapter(adapter);
        licenseText.setOnTouchListener(new View.OnTouchListener() {
	    	   @Override
	    	   public boolean onTouch(View v, MotionEvent event) {
	    		   licenseText.showDropDown();
	    		   return false;
	    	   }
	    	});

    	HttpAction action = new HttpGetTagsAction(this, getType());
    	action.execute();
    	
    	action = new HttpGetCategoriesAction(this, getType());
    	action.execute();

		Spinner accessModeSpin = (Spinner) findViewById(R.id.accessModeSpin);
		adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.accessModes);
		accessModeSpin.setAdapter(adapter);
	}

    public void saveProperties(WebMediumConfig instance) {
    	
        EditText text = (EditText) findViewById(R.id.nameText);
        instance.name = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.descriptionText);
        instance.description = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.detailsText);
        instance.details = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.disclaimerText);
        instance.disclaimer = text.getText().toString().trim();
    	text = (EditText) findViewById(R.id.categoriesText);
    	if (text != null) {
    		instance.categories = text.getText().toString().trim();
    	}
    	text = (EditText) findViewById(R.id.tagsText);
    	instance.tags = text.getText().toString().trim();
    	text = (EditText) findViewById(R.id.licenseText);
    	instance.license = text.getText().toString().trim();
    	
    	Spinner spin = (Spinner) findViewById(R.id.accessModeSpin);
    	instance.accessMode = (String)spin.getSelectedItem();

		CheckBox checkbox = (CheckBox) findViewById(R.id.privateCheckBox);
		instance.isPrivate = checkbox.isChecked();
		checkbox = (CheckBox) findViewById(R.id.hiddenCheckBox);
		instance.isHidden = checkbox.isChecked();
    }

    public void cancel(View view) {        
    	finish();
    }
}

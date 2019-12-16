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

import java.util.Arrays;

import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpGetCategoriesAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetTagsAction;
import org.botlibre.sdk.config.WebMediumConfig;
import org.botlibre.offline.pizzabot.R;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Generic activity for editing a content's details.
 */
public abstract class EditWebMediumActivity extends LibreActivity {
	private EditText licenseText,tagsText,categoriesText;
	private ImageButton btnLic, btnTag, btnCat;
	ArrayAdapter<String> adapter;
	public abstract String getType();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetView() {
       
        btnCat = (ImageButton)findViewById(R.id.btnCat);
        btnLic = (ImageButton)findViewById(R.id.btnLic);
        btnTag = (ImageButton)findViewById(R.id.btnTag);
        licenseText = (EditText)findViewById(R.id.licenseText);
        tagsText = (EditText)findViewById(R.id.tagsText);
        categoriesText = (EditText)findViewById(R.id.categoriesText);
		
      
        btnLic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(EditWebMediumActivity.this,ListLicenseView.class);
	    		startActivityForResult(i,1);
			}
		});
        btnTag.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(EditWebMediumActivity.this,ListTagsView.class);
				i.putExtra("type", getType());
				startActivityForResult(i,3);
			}
		});
        btnCat.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(EditWebMediumActivity.this,ListCategoriesView.class);
				i.putExtra("type", getType());
				startActivityForResult(i,4);
			}
		});
		
    	HttpAction action = new HttpGetTagsAction(this, getType());
    	action.execute();
    	
    	action = new HttpGetCategoriesAction(this, getType());
    	action.execute();
    	
		WebMediumConfig instance = (WebMediumConfig)MainActivity.instance;

		((TextView) findViewById(R.id.title)).setText(instance.name);
        HttpGetImageAction.fetchImage(this, instance.avatar, findViewById(R.id.icon));

        EditText text = (EditText) findViewById(R.id.nameText);
        text.setText(instance.name);
        text = (EditText) findViewById(R.id.descriptionText);
        text.setText(instance.description);
        text = (EditText) findViewById(R.id.detailsText);
        text.setText(instance.details);
        text = (EditText) findViewById(R.id.disclaimerText);
        text.setText(instance.disclaimer);
        text = (EditText) findViewById(R.id.descriptionText);
        text.setText(instance.description);
        text = (EditText) findViewById(R.id.websiteText);
        text.setText(instance.website);
        text = (EditText) findViewById(R.id.subdomainText);
        if (text != null) {
        	text.setText(instance.subdomain);
        }
		CheckBox checkbox = (CheckBox) findViewById(R.id.privateCheckBox);
		checkbox.setChecked(instance.isPrivate);
		checkbox = (CheckBox) findViewById(R.id.hiddenCheckBox);
		checkbox.setChecked(instance.isHidden);
		

        licenseText = (EditText)findViewById(R.id.licenseText);
        licenseText.setText(instance.license);
        
        tagsText = (EditText)findViewById(R.id.tagsText);
        tagsText.setText(instance.tags);
        
        categoriesText = (EditText)findViewById(R.id.categoriesText);
        if (categoriesText != null) {
	        categoriesText.setText(instance.categories);
        }

		Spinner accessModeSpin = (Spinner) findViewById(R.id.accessModeSpin);
		adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.accessModes);
		accessModeSpin.setAdapter(adapter);
		accessModeSpin.setSelection(Arrays.asList(MainActivity.accessModes).indexOf(instance.accessMode));
		
		Spinner contentRating = (Spinner) findViewById(R.id.ContentRatingSpin);
		adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,MainActivity.contentRatings);
		contentRating.setAdapter(adapter);
		contentRating.setSelection(Arrays.asList(MainActivity.contentRatings).indexOf(instance.contentRating));
		
		Spinner forkAccModSpin = (Spinner) findViewById(R.id.forkAccessModeSpin);
		adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,MainActivity.forkAccMode);
		forkAccModSpin.setAdapter(adapter);
		forkAccModSpin.setSelection(Arrays.asList(MainActivity.forkAccMode).indexOf(instance.forkAccessMode));
	}
	
			@Override
			protected void onActivityResult(int requestCode, int resultCode, Intent data) {
				super.onActivityResult(requestCode, resultCode, data);
				switch(requestCode){
				case 1:
					if(resultCode == RESULT_OK){
						licenseText.setText(data.getExtras().getString("temp"));
					}
					break;
				case 3:
					if(resultCode==RESULT_OK){
						tagsText.setText(tagsText.getText()+data.getExtras().getString("tag") + ", "); 
					}
					break;
				case 4:
					if(resultCode==RESULT_OK){
						categoriesText.setText(categoriesText.getText()+data.getExtras().getString("cat") + ", ");
					}
					break;
				}
			}

    public void saveProperties(WebMediumConfig instance) {

		instance.id = MainActivity.instance.id;
				
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
    	text = (EditText) findViewById(R.id.websiteText);
    	instance.website = text.getText().toString().trim();
    	text = (EditText) findViewById(R.id.subdomainText);
    	if (text != null) {
    		instance.subdomain = text.getText().toString().trim();
    	}

    	Spinner spin = (Spinner) findViewById(R.id.accessModeSpin);
    	instance.accessMode = (String)spin.getSelectedItem();
    	
    	spin = (Spinner) findViewById(R.id.ContentRatingSpin);
    	instance.contentRating = (String)spin.getSelectedItem();
    	
    	spin = (Spinner) findViewById(R.id.forkAccessModeSpin);
    	instance.forkAccessMode = (String)spin.getSelectedItem();
    	
		CheckBox checkbox = (CheckBox) findViewById(R.id.privateCheckBox);
		instance.isPrivate = checkbox.isChecked();
		checkbox = (CheckBox) findViewById(R.id.hiddenCheckBox);
		instance.isHidden = checkbox.isChecked();
    }

    public void cancel(View view) {        
    	finish();
    }
}

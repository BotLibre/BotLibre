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

import org.botlibre.offline.R;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

/**
 * Generic activity for creating new content.
 */
public abstract class CreateWebMediumActivity extends LibreActivity {
	private EditText licenseText,tagsText,categoriesText; //was final
	private ImageButton btnLic, btnTag, btnCat;
	public abstract String getType();
		
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetView() {
		
		 //Assigning Vars
        btnCat = (ImageButton)findViewById(R.id.btnCat);
        btnLic = (ImageButton)findViewById(R.id.btnLic);
        btnTag = (ImageButton)findViewById(R.id.btnTag);
        licenseText = (EditText)findViewById(R.id.licenseText);
        tagsText = (EditText)findViewById(R.id.tagsText);
        categoriesText = (EditText)findViewById(R.id.categoriesText);
		
        
        //Click Listeners
        btnLic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(CreateWebMediumActivity.this,ListLicenseView.class);
	    		startActivityForResult(i,1);
			}
		});
        btnTag.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(CreateWebMediumActivity.this,ListTagsView.class);
				i.putExtra("type", getType());
				startActivityForResult(i,3);
			}
		});
        btnCat.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(CreateWebMediumActivity.this,ListCategoriesView.class);
				i.putExtra("type", getType());
				startActivityForResult(i,4);
			}
		});
    	HttpAction action = new HttpGetTagsAction(this, getType());
    	action.execute();
    
    	action = new HttpGetCategoriesAction(this, getType());
    	action.execute();

		Spinner accessModeSpin = (Spinner) findViewById(R.id.accessModeSpin);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.accessModes);
		accessModeSpin.setAdapter(adapter);
		
		Spinner contentRating = (Spinner) findViewById(R.id.ContentRatingSpin);
		adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,MainActivity.contentRatings);
		contentRating.setAdapter(adapter);
		
		Spinner forkAccModSpin = (Spinner) findViewById(R.id.forkAccessModeSpin);
		adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,MainActivity.forkAccMode);
		forkAccModSpin.setAdapter(adapter);

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case 1:
			if(resultCode == RESULT_OK){
				licenseText.setText(data.getExtras().getString("temp"));//as Licenses
			}
			break;
		case 3:
			if(resultCode==RESULT_OK){
				tagsText.setText(tagsText.getText()+data.getExtras().getString("tag") + ", "); //as Tags
			}
			break;
		case 4:
			if(resultCode==RESULT_OK){
				categoriesText.setText(categoriesText.getText()+data.getExtras().getString("cat") + ", ");//as Categories
			}
			break;
		}
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
    	
    	Spinner contentRating = (Spinner) findViewById(R.id.ContentRatingSpin);
    	instance.contentRating = (String)contentRating.getSelectedItem();
    	
    	Spinner spin = (Spinner) findViewById(R.id.accessModeSpin);
    	instance.accessMode = (String)spin.getSelectedItem();

    	Spinner forkAccModSpin = (Spinner) findViewById(R.id.forkAccessModeSpin);
    	instance.forkAccessMode = (String)forkAccModSpin.getSelectedItem();
    	
		CheckBox checkbox = (CheckBox) findViewById(R.id.privateCheckBox);
		instance.isPrivate = checkbox.isChecked();
		checkbox = (CheckBox) findViewById(R.id.hiddenCheckBox);
		instance.isHidden = checkbox.isChecked();
    }

    public void cancel(View view) {        
    	finish();
    }
}

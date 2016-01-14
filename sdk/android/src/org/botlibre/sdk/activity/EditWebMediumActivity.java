package org.botlibre.sdk.activity;

import java.util.Arrays;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpGetCategoriesAction;
import org.botlibre.sdk.activity.actions.HttpGetTagsAction;
import org.botlibre.sdk.config.WebMediumConfig;

/**
 * Generic activity for editing a content's details.
 */
public abstract class EditWebMediumActivity extends Activity {
	
	public abstract String getType();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetView() {

    	HttpAction action = new HttpGetTagsAction(this, getType());
    	action.execute();
    	
    	action = new HttpGetCategoriesAction(this, getType());
    	action.execute();
    	
        setTitle("Edit: " + MainActivity.instance.name);

		WebMediumConfig instance = (WebMediumConfig)MainActivity.instance;
		
        EditText text = (EditText) findViewById(R.id.descriptionText);
        text.setText(instance.description);
        text = (EditText) findViewById(R.id.detailsText);
        text.setText(instance.details);
        text = (EditText) findViewById(R.id.disclaimerText);
        text.setText(instance.disclaimer);
		CheckBox checkbox = (CheckBox) findViewById(R.id.privateCheckBox);
		checkbox.setChecked(instance.isPrivate);
		checkbox = (CheckBox) findViewById(R.id.hiddenCheckBox);
		checkbox.setChecked(instance.isHidden);
        
        final AutoCompleteTextView tagsText = (AutoCompleteTextView)findViewById(R.id.tagsText);
        tagsText.setText(instance.tags);
        tagsText.setThreshold(0);
        tagsText.setOnTouchListener(new View.OnTouchListener() {
	    	   @Override
	    	   public boolean onTouch(View v, MotionEvent event){
	    		   tagsText.showDropDown();
	    		   return false;
	    	   }
	    	});
        
        final AutoCompleteTextView categoriesText = (AutoCompleteTextView)findViewById(R.id.categoriesText);
        if (categoriesText != null) {
	        categoriesText.setText(instance.categories);
	        categoriesText.setThreshold(0);
	        categoriesText.setOnTouchListener(new View.OnTouchListener() {
		    	   @Override
		    	   public boolean onTouch(View v, MotionEvent event){
		    		   categoriesText.showDropDown();
		    		   return false;
		    	   }
		    	});
        }

		Spinner accessModeSpin = (Spinner) findViewById(R.id.accessModeSpin);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.accessModes);
		accessModeSpin.setAdapter(adapter);
		accessModeSpin.setSelection(Arrays.asList(MainActivity.accessModes).indexOf(instance.accessMode));
	}

    public void saveProperties(WebMediumConfig instance) {

		instance.name = MainActivity.instance.name;
    	
    	EditText text = (EditText) findViewById(R.id.descriptionText);
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

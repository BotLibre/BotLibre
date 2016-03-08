package org.botlibre.sdk.activity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;

import com.paphus.botlibre.client.android.santabot.R;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpCreateAction;
import org.botlibre.sdk.config.InstanceConfig;

/**
 * Activity for creating a new bot instance.
 */
public class CreateInstanceActivity extends CreateWebMediumActivity {

	@Override
	public String getType() {
		return "Bot";
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_instance);
        
        resetView();
        
        final AutoCompleteTextView templateText = (AutoCompleteTextView) findViewById(R.id.templateText);
        templateText.setText(MainActivity.template);        
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.select_dialog_item, MainActivity.getAllTemplates(this));
        templateText.setThreshold(0);
        templateText.setAdapter(adapter);
        templateText.setOnTouchListener(new View.OnTouchListener() {
	    	   @Override
	    	   public boolean onTouch(View v, MotionEvent event){
	    		   templateText.showDropDown();
	    		   return false;
	    	   }
	    	});
	}
    
    /**
     * Create the instance.
     */
    public void create(View view) {
    	InstanceConfig instance = new InstanceConfig();
    	saveProperties(instance);
    	
    	EditText text = (EditText) findViewById(R.id.templateText);
    	instance.template = text.getText().toString().trim();
    	
    	CheckBox checkbox = (CheckBox) findViewById(R.id.forkingCheckBox);
		instance.allowForking = checkbox.isChecked();
		
        HttpAction action = new HttpCreateAction(this, instance);
        action.execute();
    }
}

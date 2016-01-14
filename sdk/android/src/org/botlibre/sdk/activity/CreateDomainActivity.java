package org.botlibre.sdk.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.botlibre.sdk.R;
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
    	
    	Spinner spin = (Spinner) findViewById(R.id.creationModeSpin);
    	instance.creationMode = (String)spin.getSelectedItem();
		
        HttpAction action = new HttpCreateAction(this, instance);
        action.execute();
    }
}

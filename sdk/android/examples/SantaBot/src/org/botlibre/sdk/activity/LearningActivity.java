package org.botlibre.sdk.activity;

import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.paphus.botlibre.client.android.santabot.R;
import org.botlibre.sdk.activity.actions.HttpGetLearningAction;
import org.botlibre.sdk.activity.actions.HttpSaveLearningAction;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.LearningConfig;

/**
 * Activity for administering a bot's learning.
 */
public class LearningActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);
        
        setTitle("Learning: " + MainActivity.instance.name);
        
        HttpGetLearningAction action = new HttpGetLearningAction(this, (InstanceConfig)MainActivity.instance.credentials());
    	action.execute();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetView() {
		Spinner spin = (Spinner) findViewById(R.id.learningModeSpin);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.learningModes);
		spin.setAdapter(adapter);
		spin.setSelection(Arrays.asList(MainActivity.learningModes).indexOf(MainActivity.learning.learningMode));

		spin = (Spinner) findViewById(R.id.correctionModeSpin);
		adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, MainActivity.correctionModes);
		spin.setAdapter(adapter);
		spin.setSelection(Arrays.asList(MainActivity.correctionModes).indexOf(MainActivity.learning.correctionMode));
		
		CheckBox checkbox = (CheckBox)findViewById(R.id.enableComprehensionCheckBox);
		checkbox.setChecked(MainActivity.learning.enableComprehension);
		checkbox = (CheckBox)findViewById(R.id.enableEmoteCheckBox);
		checkbox.setChecked(MainActivity.learning.enableEmoting);
	}

	public void save(View view) {
		LearningConfig config = new LearningConfig();
        config.instance = MainActivity.instance.id;
        
        Spinner spin = (Spinner) findViewById(R.id.learningModeSpin);
        config.learningMode = spin.getSelectedItem().toString();
        spin = (Spinner) findViewById(R.id.correctionModeSpin);
        config.correctionMode = spin.getSelectedItem().toString();
        
		CheckBox checkbox = (CheckBox)findViewById(R.id.enableComprehensionCheckBox);
		config.enableComprehension = checkbox.isChecked();
		checkbox = (CheckBox)findViewById(R.id.enableEmoteCheckBox);
		config.enableEmoting = checkbox.isChecked();
        
        HttpSaveLearningAction action = new HttpSaveLearningAction(this, config);
		action.execute();
	}
}

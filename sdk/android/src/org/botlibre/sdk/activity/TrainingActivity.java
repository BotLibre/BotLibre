package org.botlibre.sdk.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpGetDefaultResponsesAction;
import org.botlibre.sdk.activity.actions.HttpGetGreetingsAction;
import org.botlibre.sdk.activity.actions.HttpTrainingAction;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.TrainingConfig;

/**
 * Activity for training a bot's responses.
 */
public class TrainingActivity extends Activity {
	
	public List<String> greetings = new ArrayList<String>();
	public List<String> defaultResponses = new ArrayList<String>();
			
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        
        setTitle("Training: " + MainActivity.instance.name);

        InstanceConfig instance = (InstanceConfig)MainActivity.instance.credentials();
        
        HttpAction action = new HttpGetGreetingsAction(this, instance);
    	action.execute();
        action = new HttpGetDefaultResponsesAction(this, instance);
    	action.execute();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetView() {
		ListView list = (ListView) findViewById(R.id.greetingsList);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, this.greetings.toArray());
		list.setAdapter(adapter);
		
		list = (ListView) findViewById(R.id.defaultResponsesList);
		adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, this.defaultResponses.toArray());
		list.setAdapter(adapter);
	}

	public void addGreeting(View view) {
		TrainingConfig config = new TrainingConfig();
        config.instance = MainActivity.instance.id;
        
        config.operation = "AddGreeting";
        EditText text = (EditText) findViewById(R.id.greetingText);
        config.response = text.getText().toString().trim();
		if (config.response.length() == 0) {
			MainActivity.error("Enter greeting", null, this);
			return;
		}
        
		HttpAction action = new HttpTrainingAction(this, config);
		action.execute();
	}

	public void removeGreeting(View view) {
		TrainingConfig config = new TrainingConfig();
        config.instance = MainActivity.instance.id;
        
        config.operation = "RemoveGreeting";
        ListView list = (ListView) findViewById(R.id.greetingsList);
        int index = list.getCheckedItemPosition();
		if (index < 0) {
			MainActivity.error("Select greeting to remove", null, this);
			return;
		}
        config.response = (String)this.greetings.get(index);
        
		HttpAction action = new HttpTrainingAction(this, config);
		action.execute();
	}

	public void addDefaultResponse(View view) {
		TrainingConfig config = new TrainingConfig();
        config.instance = MainActivity.instance.id;
        
        config.operation = "AddDefaultResponse";
        EditText text = (EditText) findViewById(R.id.defaultResponseText);
        config.response = text.getText().toString().trim();
		if (config.response.length() == 0) {
			MainActivity.error("Enter default response", null, this);
			return;
		}
        
		HttpAction action = new HttpTrainingAction(this, config);
		action.execute();
	}

	public void removeDefaultResponse(View view) {
		TrainingConfig config = new TrainingConfig();
        config.instance = MainActivity.instance.id;
        
        config.operation = "RemoveDefaultResponse";
        ListView list = (ListView) findViewById(R.id.defaultResponsesList);
        int index = list.getCheckedItemPosition();
		if (index < 0) {
			MainActivity.error("Select default response to remove", null, this);
			return;
		}
        config.response = (String)this.defaultResponses.get(index);
        
		HttpAction action = new HttpTrainingAction(this, config);
		action.execute();
	}

	public void addResponse(View view) {
		TrainingConfig config = new TrainingConfig();
        config.instance = MainActivity.instance.id;
        
        config.operation = "AddResponse";
        EditText text = (EditText) findViewById(R.id.questionText);
        config.question = text.getText().toString().trim();
		if (config.question.length() == 0) {
			MainActivity.error("Enter question", null, this);
			return;
		}
        text = (EditText) findViewById(R.id.responseText);
        config.response = text.getText().toString().trim();
		if (config.response.length() == 0) {
			MainActivity.error("Enter response", null, this);
			return;
		}
        
		HttpAction action = new HttpTrainingAction(this, config);
		action.execute();
	}
}

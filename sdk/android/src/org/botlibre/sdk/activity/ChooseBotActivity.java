package org.botlibre.sdk.activity;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchAction;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.WebMediumConfig;

/**
 * Activity for choosing a bot from the search results.
 */
public class ChooseBotActivity extends Activity {
	
	public List<WebMediumConfig> instances;
	public WebMediumConfig instance;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choosebot);
		
		this.instances = MainActivity.instances;

		ListView list = (ListView) findViewById(R.id.instancesList);
		list.setAdapter(new ImageListAdapter(this, R.layout.image_list, this.instances));
		GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTapEvent(MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					ListView list = (ListView) findViewById(R.id.instancesList);
			        int index = list.getCheckedItemPosition();
			        if (index < 0) {
						return false;
			        } else {
			        	selectInstance(list);
			        }
					return true;
				}
				return false;
			}
		};
		final GestureDetector listDetector = new GestureDetector(this, listener);
		list.setOnTouchListener(new View.OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return listDetector.onTouchEvent(event);
			}
		});
	}
	
	@Override
	public void onResume() {
		this.instances = MainActivity.instances;

		ListView list = (ListView) findViewById(R.id.instancesList);
		list.setAdapter(new ImageListAdapter(this, R.layout.image_list, this.instances));
		
		super.onResume();
	}

	public void selectInstance(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select a bot", this);
        	return;
        }
        this.instance = instances.get(index);
        InstanceConfig config = new InstanceConfig();
        config.id = this.instance.id;
        config.name = this.instance.name;
		
        HttpAction action = new HttpFetchAction(this, config);
    	action.execute();
	}

	public void chat(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select a bot", this);
        	return;
        }
        this.instance = instances.get(index);
        InstanceConfig config = new InstanceConfig();
        config.id = this.instance.id;
        config.name = this.instance.name;
		
        HttpAction action = new HttpFetchAction(this, config, true);
    	action.execute();
	}
}

package org.botlibre.sdk.activity.forum;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;

import org.botlibre.sdk.activity.R;
import org.botlibre.sdk.activity.BrowseActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchAction;
import org.botlibre.sdk.activity.actions.HttpGetCategoriesAction;
import org.botlibre.sdk.activity.actions.HttpGetTagsAction;
import org.botlibre.sdk.config.ForumConfig;

/**
 * Browse activity for searching for a forum.
 */
public class ForumBrowseActivity extends BrowseActivity {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.superOnCreate(savedInstanceState);
		setContentView(R.layout.activity_browse);

		resetLast();

		RadioButton radio = (RadioButton)findViewById(R.id.personalRadio);
		radio.setText("My Forums");
    	
		Spinner sortSpin = (Spinner) findViewById(R.id.sortSpin);
		ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, new String[]{
        			"connects",
        			"connects today",
        			"connects this week",
        			"connects this month",
        			"name",
        			"date",
        			"posts"
			    
        });
		sortSpin.setAdapter(adapter);

    	HttpAction action = new HttpGetTagsAction(this, getType());
    	action.execute();
    	
    	action = new HttpGetCategoriesAction(this, getType());
    	action.execute();
	}
	
	public void resetLast() {
		Button button = (Button)findViewById(R.id.lastButton);
    	SharedPreferences cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE);
    	String last = cookies.getString("forum", null);
    	if (last != null) {
    		button.setText(last);
    		button.setVisibility(View.VISIBLE);
    	} else {
    		button.setVisibility(View.GONE);
    	}
	}

	public void openLast(View view) {
    	SharedPreferences cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE);
    	String last = cookies.getString("forum", null);
        if (last == null) {
        	MainActivity.showMessage("Forum is invalid", this);
        	return;
        }

        ForumConfig config = new ForumConfig();
        config.name = last;
        HttpAction action = new HttpFetchAction(this, config);
    	action.execute();
	}
	
	public String getType() {		
		return "Forum";
	}
}

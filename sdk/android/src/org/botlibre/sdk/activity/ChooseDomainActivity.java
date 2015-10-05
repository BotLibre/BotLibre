package org.botlibre.sdk.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import org.botlibre.sdk.activity.R;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchAction;
import org.botlibre.sdk.config.DomainConfig;

/**
 * Activity for choosing a domain from the search results.
 */
public class ChooseDomainActivity extends ChooseBotActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((Button)findViewById(R.id.chatButton)).setText("Browse");
	}
	
	@Override
	public void selectInstance(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select a domain", this);
        	return;
        }
        this.instance = instances.get(index);
        DomainConfig config = new DomainConfig();
        config.id = this.instance.id;
		
        HttpAction action = new HttpFetchAction(this, config);
    	action.execute();
	}

	@Override
	public void chat(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select a domain", this);
        	return;
        }
        this.instance = instances.get(index);
        DomainConfig config = new DomainConfig();
        config.id = this.instance.id;
		
        HttpAction action = new HttpFetchAction(this, config, true);
    	action.execute();
	}
}

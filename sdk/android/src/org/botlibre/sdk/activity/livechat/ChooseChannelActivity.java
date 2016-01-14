package org.botlibre.sdk.activity.livechat;

import android.view.View;
import android.widget.ListView;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.ChooseBotActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchAction;
import org.botlibre.sdk.config.ChannelConfig;

/**
 * Activity for choosing a channel from the search results.
 */
public class ChooseChannelActivity extends ChooseBotActivity {

	public void selectInstance(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select a channel", this);
        	return;
        }
        this.instance = instances.get(index);
        ChannelConfig config = new ChannelConfig();
        config.id = this.instance.id;
        config.name = this.instance.name;
		
        HttpAction action = new HttpFetchAction(this, config);
    	action.execute();
	}

	public void chat(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select a channel", this);
        	return;
        }
        this.instance = instances.get(index);
        ChannelConfig config = new ChannelConfig();
        config.id = this.instance.id;
        config.name = this.instance.name;
		
        HttpAction action = new HttpFetchAction(this, config, true);
    	action.execute();
	}
}

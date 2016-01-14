package org.botlibre.sdk.activity;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.actions.HttpAddSharedAvatarAction;
import org.botlibre.sdk.activity.actions.HttpGetSharedAvatarsAction;
import org.botlibre.sdk.config.AvatarConfig;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

/**
 * Activity for choosing a shared avatar for a bot.
 */
public class AddAvatarActivity extends Activity {
	AvatarConfig avatar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_avatar);

		if (MainActivity.sharedAvatars == null) {
	        HttpGetSharedAvatarsAction action = new HttpGetSharedAvatarsAction(this);
			try {
				String xml = action.execute().get();
				action.postExecute(xml);
	    		if (action.getException() != null) {
	    			throw action.getException();
	    		}
			} catch (Exception exception) {
				MainActivity.error(exception.getMessage(), exception, this);
				return;
			}
		}

		ListView list = (ListView) findViewById(R.id.imagesList);
		list.setAdapter(new AvatarsListAdapter(this, R.layout.avatars_list, MainActivity.sharedAvatars));
	}

	public void pickImage(View view) {

		ListView list = (ListView) findViewById(R.id.imagesList);
		int index = list.getCheckedItemPosition();
		if (index < 0) {
			MainActivity.showMessage("Select image", this);
			return;
		}
		this.avatar = MainActivity.sharedAvatars.get(index);
		this.avatar.instance = MainActivity.instance.id;
		
		HttpAddSharedAvatarAction action = new HttpAddSharedAvatarAction(this, this.avatar);
		action.execute();
	}

}

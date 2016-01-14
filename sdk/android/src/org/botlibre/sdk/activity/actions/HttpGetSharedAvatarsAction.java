package org.botlibre.sdk.activity.actions;

import java.util.List;

import android.app.Activity;
import android.widget.ListView;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.AvatarsListAdapter;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.AvatarConfig;

public class HttpGetSharedAvatarsAction extends HttpAction {
	List<AvatarConfig> avatars;
	
	public HttpGetSharedAvatarsAction(Activity activity) {
		super(activity);
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.avatars = MainActivity.connection.getSharedAvatars();
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	public void postExecute(String xml) {
		if (this.exception != null) {
			return;
		}
		try {
			MainActivity.sharedAvatars = this.avatars;

			ListView list = (ListView) this.activity.findViewById(R.id.imagesList);
			list.setAdapter(new AvatarsListAdapter(this.activity, R.layout.avatars_list, MainActivity.sharedAvatars));
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;			
		}
	}
}
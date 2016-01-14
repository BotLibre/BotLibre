package org.botlibre.sdk.activity.actions;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.AvatarActivity;
import org.botlibre.sdk.activity.AvatarsListAdapter;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.InstanceConfig;

public class HttpGetAvatarsAction extends HttpUIAction {

	InstanceConfig config;
	List<AvatarConfig> avatars;
	
	public HttpGetAvatarsAction(Activity activity, InstanceConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.avatars = MainActivity.connection.getAvatars(this.config);
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	@Override
	public void onPostExecute(String xml) {
		super.onPostExecute(xml);
		if (this.exception != null) {
			return;
		}
		try {
			MainActivity.avatars = this.avatars;

			ListView list = (ListView) this.activity.findViewById(R.id.avatarsList);
			list.setAdapter(new AvatarsListAdapter(this.activity, R.layout.avatars_list, MainActivity.avatars));
			
			list.setOnItemClickListener(new OnItemClickListener() {
	            @SuppressWarnings("rawtypes")
				@Override
	            public void onItemClick(AdapterView parent, View view, int position, long id) {
	            	((AvatarActivity)activity).resetTags();
	            }
	        });
			((AvatarActivity)activity).resetTags();
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;			
		}
	}
}
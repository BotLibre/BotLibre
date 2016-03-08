package org.botlibre.sdk.activity.actions;

import android.app.Activity;
import android.widget.EditText;

import com.paphus.botlibre.client.android.santabot.R;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.WebMediumUsersActivity;
import org.botlibre.sdk.config.UserAdminConfig;

public class HttpUserAdminAction extends HttpUIAction {

	UserAdminConfig config;
	
	public HttpUserAdminAction(Activity activity, UserAdminConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			MainActivity.connection.userAdmin(this.config);
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
		if (this.config.operation.equals("AddUser")) {
			((WebMediumUsersActivity)this.activity).users.add(this.config.operationUser);
		} else if (this.config.operation.equals("RemoveUser")) {
			((WebMediumUsersActivity)this.activity).users.remove(this.config.operationUser);
		} else if (this.config.operation.equals("AddAdmin")) {
			((WebMediumUsersActivity)this.activity).admins.add(this.config.operationUser);
		} else if (this.config.operation.equals("RemoveAdmin")) {
			((WebMediumUsersActivity)this.activity).admins.remove(this.config.operationUser);
		}
        EditText text = (EditText) this.activity.findViewById(R.id.adminText);
		text.setText("");
        text = (EditText) this.activity.findViewById(R.id.userText);
		text.setText("");
		((WebMediumUsersActivity)this.activity).resetView();
	}
}
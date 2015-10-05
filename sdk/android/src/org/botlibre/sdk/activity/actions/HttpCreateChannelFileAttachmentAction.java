package org.botlibre.sdk.activity.actions;

import android.app.Activity;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.livechat.LiveChatActivity;
import org.botlibre.sdk.config.MediaConfig;

public class HttpCreateChannelFileAttachmentAction extends HttpUIAction {

	MediaConfig config;
	String file;
	
	public HttpCreateChannelFileAttachmentAction(Activity activity, String file, MediaConfig config) {
		super(activity);
		this.config = config;
		this.file = file;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.config = MainActivity.connection.createChannelFileAttachment(this.file, this.config);
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
		((LiveChatActivity)this.activity).sendFile(config);
    }
}
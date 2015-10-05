package org.botlibre.sdk.activity.actions;

import android.app.Activity;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.MediaConfig;

public class HttpCreateChannelImageAttachmentAction extends HttpCreateChannelFileAttachmentAction {
	
	public HttpCreateChannelImageAttachmentAction(Activity activity, String file, MediaConfig config) {
		super(activity, file, config);
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.config = MainActivity.connection.createChannelImageAttachment(this.file, this.config);
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}
}
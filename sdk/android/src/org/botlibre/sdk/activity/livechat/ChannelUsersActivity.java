package org.botlibre.sdk.activity.livechat;

import org.botlibre.sdk.activity.WebMediumUsersActivity;

/**
 * Activity for a administering a channel's users.
 */
public class ChannelUsersActivity extends WebMediumUsersActivity {
	public String getType() {
		return "Channel";
	}
}

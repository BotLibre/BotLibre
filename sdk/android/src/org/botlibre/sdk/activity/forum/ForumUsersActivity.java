package org.botlibre.sdk.activity.forum;

import org.botlibre.sdk.activity.WebMediumUsersActivity;

/**
 * Activity for a administering a forum's users.
 */
public class ForumUsersActivity extends WebMediumUsersActivity {
	public String getType() {
		return "Forum";
	}
}

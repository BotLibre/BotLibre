package org.botlibre.sdk.activity;

/**
 * Activity for a administering a domain's users.
 */
public class DomainUsersActivity extends WebMediumUsersActivity {
	public String getType() {
		return "Domain";
	}
}

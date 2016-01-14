package org.botlibre.sdk.activity;

import org.botlibre.sdk.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Activity for a domain's admin functions.
 */
public class DomainAdminActivity extends WebMediumAdminActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_domain);
		
        resetView();
        
	}

	public void adminUsers(View view) {
        Intent intent = new Intent(this, DomainUsersActivity.class);		
        startActivity(intent);
	}

	public void editInstance(View view) {
        Intent intent = new Intent(this, EditDomainActivity.class);		
        startActivity(intent);
	}

	public void adminCategories(View view) {
		// TODO
	}
	
}

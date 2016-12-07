/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

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

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

import org.botlibre.sdk.config.DomainConfig;

import org.botlibre.sdk.R;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

/**
 * Activity for viewing a domain details.
 */
public class DomainActivity extends WebMediumActivity {

	public void admin() {
        Intent intent = new Intent(this, DomainAdminActivity.class);		
        startActivity(intent);
	}

	public void resetView() {
        setContentView(R.layout.activity_domain);

        super.resetView();
	}

	public void browse(View view) {
		MainActivity.type = MainActivity.defaultType;
		
		MainActivity.connection.setDomain((DomainConfig)MainActivity.instance);
		MainActivity.domain = (DomainConfig)MainActivity.instance;
		MainActivity.tags = null;
		MainActivity.categories = null;
		MainActivity.forumTags = null;
		MainActivity.forumCategories = null;
		MainActivity.channelTags = null;
		MainActivity.channelCategories = null;
		MainActivity.avatarTags = null;
		MainActivity.avatarCategories = null;
		MainActivity.scriptTags = null;
		MainActivity.scriptCategories = null;
		
        Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
	}
	
	public void openWebsite() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.WEBSITE + "/domain?id=" + MainActivity.instance.id));
		startActivity(intent);
	}
	
	public String getType() {
		return "Domain";
	}
	
}

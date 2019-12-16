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

package org.botlibre.sdk.activity.avatar;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.WebMediumActivity;

import org.botlibre.offline.R;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

/**
 * Activity for viewing a avatar's details.
 * To launch this activity from your app you can use the HttpFetchAction passing the forum id or name as a config.
 */
public class AvatarActivity extends WebMediumActivity {

	public void admin() {
        Intent intent = new Intent(this, AvatarAdminActivity.class);		
        startActivity(intent);
	}

	public void resetView() {
        setContentView(R.layout.activity_avatar);
        super.resetView();

        if (instance.isExternal) {
        	findViewById(R.id.testButton).setVisibility(View.GONE);
        }
	}
	
	public void openWebsite() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.WEBSITE + "/avatar?id=" + MainActivity.instance.id));
		startActivity(intent);
	}
	
	public String getType() {
		return "Avatar";
	}
	
	public void openTest(View view) {
        Intent intent = new Intent(this, AvatarTestActivity.class);		
        startActivity(intent);
	}
	
}

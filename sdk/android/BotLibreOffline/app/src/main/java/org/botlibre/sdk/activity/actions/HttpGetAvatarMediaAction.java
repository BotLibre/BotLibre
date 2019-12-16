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

package org.botlibre.sdk.activity.actions;

import java.util.List;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.avatar.AvatarEditorActivity;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.AvatarMedia;

import android.app.Activity;
import android.content.Intent;

public class HttpGetAvatarMediaAction extends HttpUIAction {
	AvatarConfig config;
	List<AvatarMedia> instances;
	boolean finish = false;

	public HttpGetAvatarMediaAction(Activity activity, AvatarConfig config, boolean finish) {
		super(activity);
		this.config = config;
		this.finish = finish;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			this.instances = MainActivity.connection.getAvatarMedia(config);
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
		MainActivity.avatarMedias = this.instances;
		if (this.finish) {
			this.activity.finish();
		}
		
        Intent intent = new Intent(this.activity, AvatarEditorActivity.class);		
        this.activity.startActivity(intent);
	}
}
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

import android.app.Activity;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.WebMediumAdminActivity;
import org.botlibre.sdk.config.WebMediumConfig;

public class HttpChangeIconAction extends HttpUIAction {

	WebMediumConfig config;
	String file;
	
	public HttpChangeIconAction(Activity activity, String file, WebMediumConfig config) {
		super(activity);
		this.config = config;
		this.file = file;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.config = MainActivity.connection.updateIcon(this.file, this.config);
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
		try {
			MainActivity.instance = this.config;
			((WebMediumAdminActivity)this.activity).resetView();
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;
		}
    }
}
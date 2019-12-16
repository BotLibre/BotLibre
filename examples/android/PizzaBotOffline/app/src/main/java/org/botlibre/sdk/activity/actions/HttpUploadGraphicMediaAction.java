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

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.GraphicConfig;

import android.app.Activity;

public class HttpUploadGraphicMediaAction extends HttpUIAction{

	GraphicConfig config;
	String file;
	
	public HttpUploadGraphicMediaAction(Activity activity, String file, GraphicConfig config) {
		super(activity);
		this.config = config;
		this.file = file;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			MainActivity.connection.createGraphicMedia(this.file, this.config);
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	@Override
	protected void onPostExecute(String xml) {
		this.activity.finish();
		super.onPostExecute(xml);
		if (this.exception != null) {
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;
		}
		HttpRefreshAction refresh = new HttpRefreshAction(this.activity,config);
		refresh.execute();
	}
}

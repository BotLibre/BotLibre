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
import android.content.Context;
import android.content.SharedPreferences;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.UserConfig;

public class HttpConnectAction extends HttpUIAction {
	UserConfig config;
	boolean finish;

	public HttpConnectAction(Activity activity, UserConfig config, boolean finish) {
		super(activity);
		this.config = config;
		this.finish = finish;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.config = MainActivity.connection.connect(config);
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	@Override
	protected void onPostExecute(String xml) {
		super.onPostExecute(xml);
		if (this.exception != null) {
			return;
		}
		try {
			MainActivity.user = this.config;
			if (this.activity instanceof MainActivity) {
				((MainActivity)this.activity).resetView();
			}
	    	
	    	if (this.finish) {		    	
		    	SharedPreferences.Editor cookies = this.activity.getPreferences(Context.MODE_PRIVATE).edit();
		    	cookies.putString("user", MainActivity.user.user);
		    	cookies.putString("token", MainActivity.user.token);
		    	cookies.commit();
		    	
		    	this.activity.finish();
	    	}
	
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
	    	
	    	SharedPreferences.Editor cookies = this.activity.getPreferences(Context.MODE_PRIVATE).edit();
	    	cookies.putString("user", null);
	    	cookies.putString("token", null);
	    	cookies.commit();
			return;			
		}
	}
	
}
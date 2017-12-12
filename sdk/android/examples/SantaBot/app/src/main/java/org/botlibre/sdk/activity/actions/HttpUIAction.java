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
import android.app.ProgressDialog;
import android.util.Log;

import org.botlibre.sdk.activity.MainActivity;

public abstract class HttpUIAction extends HttpAction {
	protected ProgressDialog dialog;

	public HttpUIAction(Activity activity) {
		super(activity);
	}
	
	@Override
	protected void onPreExecute() {
		this.dialog = new ProgressDialog(this.activity);
		this.dialog.setMessage("Processing..."); 
		this.dialog.show();
	}
	
	@Override
	protected void onPostExecute(String result) {
		try {
			if (this.dialog != null) {
				this.dialog.dismiss();
			}
			if (this.exception != null) {
				MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			}
		} catch (Exception exception) {
			Log.wtf("HttpUIAction", exception);
		}
	}
}
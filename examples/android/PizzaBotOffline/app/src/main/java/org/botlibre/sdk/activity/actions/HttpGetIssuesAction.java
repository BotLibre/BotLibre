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
import android.content.Intent;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.issuetracker.BrowseIssueActivity;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.IssueConfig;

import java.util.List;

public class HttpGetIssuesAction extends HttpUIAction {
	BrowseConfig config;
	List<IssueConfig> issues;
	boolean finish = false;

	public HttpGetIssuesAction(Activity activity, BrowseConfig config) {
		super(activity);
		this.config = config;
	}

	public HttpGetIssuesAction(Activity activity, BrowseConfig config, boolean finish) {
		super(activity);
		this.config = config;
		this.finish = finish;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.issues = MainActivity.connection.getIssues(this.config);
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
		MainActivity.issues = this.issues;
		
		if (this.finish) {
			this.activity.finish();
		}
		MainActivity.browseIssues = this.config;
        Intent intent = new Intent(this.activity, BrowseIssueActivity.class);
        this.activity.startActivity(intent);
	}
}
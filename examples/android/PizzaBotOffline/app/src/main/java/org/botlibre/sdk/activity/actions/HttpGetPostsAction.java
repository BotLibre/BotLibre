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

import android.app.Activity;
import android.content.Intent;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.forum.BrowsePostActivity;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ForumPostConfig;

public class HttpGetPostsAction extends HttpUIAction {
	BrowseConfig config;
	List<ForumPostConfig> posts;
	boolean finish = false;

	public HttpGetPostsAction(Activity activity, BrowseConfig config) {
		super(activity);
		this.config = config;
	}

	public HttpGetPostsAction(Activity activity, BrowseConfig config, boolean finish) {
		super(activity);
		this.config = config;
		this.finish = finish;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
		this.posts = MainActivity.connection.getPosts(this.config);
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
		MainActivity.posts = this.posts;
		
		if (this.finish) {
			this.activity.finish();
		}
		MainActivity.browsePosts = this.config;
        Intent intent = new Intent(this.activity, BrowsePostActivity.class);		
        this.activity.startActivity(intent);
	}
}
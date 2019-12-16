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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.botlibre.offline.R;
import java.util.ArrayList;
import java.util.List;
import org.botlibre.sdk.activity.CategoryListAdapter;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.ContentConfig;

public class HttpGetCategoriesAction extends HttpAction {
	ListView llview;
	ContentConfig config;
	Object[] categories;
	List<ContentConfig> listCategories;

	public HttpGetCategoriesAction(Activity activity, String type) {
		super(activity);
		this.config = new ContentConfig();
		this.config.type = type;
	}

	@Override
	protected String doInBackground(Void... params) {
		if (this.config.type.equals("Bot") && MainActivity.categories != null) {
			this.categories = MainActivity.categories;
		} else if (this.config.type.equals("Forum") && MainActivity.forumCategories != null) {
			this.categories = MainActivity.forumCategories;
		} else if (this.config.type.equals("Channel") && MainActivity.channelCategories != null) {
			this.categories = MainActivity.channelCategories;
		} else if (this.config.type.equals("Avatar") && MainActivity.avatarCategories != null) {
			this.categories = MainActivity.avatarCategories;
		} else if (this.config.type.equals("Script") && MainActivity.scriptCategories != null) {
			this.categories = MainActivity.scriptCategories;
		} else if (this.config.type.equals("Domain")&&MainActivity.domainCategories != null) {
			this.config.domain = null;
			this.categories = MainActivity.domainCategories;
		} else {
			try {
				this.categories = MainActivity.connection.getCategories(this.config).toArray();
			} catch (Exception exception) {
				this.exception = exception;
				this.categories = new Object[0];
			}
		}
		return "";
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void onPostExecute(String xml) {
		if (this.exception != null) {
			return;
		}
		if (this.config.type.equals("Bot")) {
			MainActivity.categories = this.categories;
		} else if (this.config.type.equals("Forum")) {
			MainActivity.forumCategories = this.categories;
		} else if (this.config.type.equals("Channel")) {
			MainActivity.channelCategories = this.categories;
		} else if (this.config.type.equals("Avatar")) {
			MainActivity.avatarCategories = this.categories;
		} else if (this.config.type.equals("Script")) {
			MainActivity.scriptCategories = this.categories;
		}else if(this.config.type.equals("Domain")) {
			MainActivity.domainCategories = this.categories;
		}
		
		//extracting everything from this.categories name,icon,description
		listCategories = new ArrayList<ContentConfig>();
		for (int i = 0; i < categories.length; i++) {
			listCategories.add((ContentConfig)this.categories[i]);
		}
		
		//extracting the names from this.categories
		Object[] names = new Object[this.categories.length];
		for (int index = 0; index < this.categories.length; index++) {
			names[index] = (((ContentConfig)this.categories[index]).name);
		}
		
		llview = (ListView) this.activity.findViewById(R.id.theListView);
        if (llview != null) {
	        ArrayAdapter adapter = new CategoryListAdapter(this.activity,
	                R.layout.image_list, listCategories);
	        llview.setAdapter(adapter);
        }
	}
}
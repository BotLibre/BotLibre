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

package android.content;

import java.util.ArrayList;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

/**
 * Stub class.
 */
public class Intent {
	public static final int ACTION_VIEW = 0;
	public static final int FLAG_ACTIVITY_NEW_TASK = 1;
	public static final int FLAG_ACTIVITY_CLEAR_TOP = 2;
	public static final int ACTION_GET_CONTENT = 3;
	public static final int ACTION_SEND = 4;
	public static final int EXTRA_EMAIL = 5;
	public static final int ACTION_PICK = 7;

	public Activity parent;
	public Class activity;
	public int id;
	public Uri uri;
	public String action;
	public String type;
	public String packageName;
	public int flags;
	
	public Intent(){
		
	}
	
	public Intent(Activity parent, Class activity) {
		this.parent = parent;
		this.activity = activity;
	}
	
	public Intent(int id) {
		this.id = id;
	}
	
	public Intent(int id, Uri uri) {
		this.id = id;
		this.uri = uri;
	}
	
	public Intent(String action, Uri uri) {
		this.action = action;
		this.uri = uri;
	}
	
	public Intent(String action2) {
		this.action = action2;
	}

	public String getPackage() {
		return packageName;
	}

	public void setPackage(String packageName) {
		this.packageName = packageName;
	}

	public void setFlags(int flag) {
		this.flags = flag;
	}

	public Uri getData() {
		return null;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public void putExtra(int id, String value) {
		
	}
	
	
	
	public ArrayList<String> getStringArrayListExtra(int id) {
		return new ArrayList();
	}

	public void putExtra(int extraEmail, String[] strings) {
	
	}

	public static Intent createChooser(Intent intent, String string) {
		return null;
	}

	public void putExtra(String string, String name) {
		
		
	}
public void putExtra(String string, boolean val) {
		
		
	}

public Bundle getExtras() {
	// TODO Auto-generated method stub
	return null;
}
}
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

package android.widget;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

/**
 * Stub class.
 */
public class ArrayAdapter<T> {
	public Object[] array;
	
	public ArrayAdapter() {
	}
	
	public ArrayAdapter(Activity activity, String id, Object[] array) {
		this.array = array;
	}
	public ArrayAdapter(Activity activity, int id, List items) {
		this.array = items.toArray();
	}
	public ArrayAdapter(Activity activity, String resourceId, List items) {
        this.array = items.toArray();
    }
	
	public ArrayAdapter(Activity activity, String id, String resourceId, List items) {
        this.array = items.toArray();
    }
	

	public T getItem(int index) {
		return (T)this.array[index];
	}
	
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
    	return null;
    }
}
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

import javax.swing.JComponent;

import android.app.Activity;
import android.view.View;

/**
 * Stub class.
 */
public class AutoCompleteTextView extends EditText {
	
	public AutoCompleteTextView(Activity activity) {
		this.activity = activity;
	}
	
	public AutoCompleteTextView(JComponent component) {
		super(component);
	}
	
	public void setInputType(int type) {
		
	}
	
	public void setThreshold(int threshold) {
		
	}

	public void setAdapter(ArrayAdapter adapter) {
		
	}
	
	public void setOnTouchListener(View.OnTouchListener listener) {
		
	}
	
	public void showDropDown() {
		
	}
}
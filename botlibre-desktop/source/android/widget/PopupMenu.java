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

import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

/**
 * Stub class.
 */
public class PopupMenu {
	protected OnMenuItemClickListener listener;
	protected Activity activity;
	protected View view;
	
	public interface OnMenuItemClickListener {
		boolean onMenuItemClick(MenuItem item);
	}
	
	public PopupMenu() {
		
	}
	
	public PopupMenu(Activity activity, View view) {
		this.activity = activity;
		this.view = view;
	}
	
	public MenuInflater getMenuInflater() {
		return new MenuInflater();
	}
	
	public Menu getMenu() {
		return null;
	}
	
	public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
		this.listener = listener;
	}
	
	public void show() {
		((JPopupMenu)view.component).show();
	}
}
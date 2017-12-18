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

package android.app;

import java.awt.Cursor;

/**
 * Stub class.
 */
public class ProgressDialog extends AlertDialog {
	public ProgressDialog(Activity activty) {
		super(activty);
	}
	
	public void show() {
        this.activity.frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void dismiss() {
        this.activity.frame.setCursor(Cursor.getDefaultCursor());
	}
	public void setCancelable(boolean cancelable){
		
	} 
}
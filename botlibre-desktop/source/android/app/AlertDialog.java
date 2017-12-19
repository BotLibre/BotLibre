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

import javax.swing.JOptionPane;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

/**
 * AlertDialog proxy using a Swing JOptionPane.
 */
public class AlertDialog extends DialogInterface {
	public static int BUTTON_NEUTRAL = 0;
	public static int BUTTON_POSITIVE = 1;
	public static int BUTTON_NEGATIVE = 2;
	
	protected String message = "Error";
	protected Activity activity;
	
	public static class Builder {
		Activity activity;
		
		public Builder(Activity activity) {
			this.activity = activity;
		}
		
		public AlertDialog create() {
			return new AlertDialog(activity);
		}
	}
	
	public AlertDialog() {
		
	}
	
	public AlertDialog(Activity activity) {
		this.activity = activity;
	}
	
	public void setView(View view) {
		
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setButton(int type, String label, OnClickListener onClickListener) {
		
	}
	
	public void show() {
		JOptionPane.showMessageDialog(this.activity.frame, this.message);
	}
	
	public void dismiss() {
		
	}
}
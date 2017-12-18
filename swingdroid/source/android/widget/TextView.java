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

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;

import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;

/**
 * TextView proxy for a Swing text view.
 */
public class TextView extends View {
	public static interface OnEditorActionListener {
		boolean onEditorAction(TextView v, int actionId, KeyEvent event);
	}
	
	public TextView() {
		
	}
	
	public TextView(JComponent component) {
		super(component);
	}
	
	public String getText() {
		return ((JLabel)this.component).getText();
	}
	
	public void setText(String text) {
		((JLabel)this.component).setText(text);
	}
	
	public void setTextColor(java.awt.Color color) {
		((JLabel)this.component).setForeground(color);
	}
//	public void setTextColor(Color color) {
//		((JLabel)this.component).setForeground(color.);
//	}

	public void setTypeface(String s, int n, int f) {
		((JLabel)this.component).setFont(new Font(s, n, f));
	}

}
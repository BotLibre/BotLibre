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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTextField;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView.OnEditorActionListener;

/**
 * Stub class.
 */
public class EditText extends TextView {
	
	public EditText() {
		this.component = new JTextField();
	}
	
	public EditText(Activity activity) {
		this.activity = activity;
		this.component = new JTextField();
	}
	
	public JTextField getEditeText(){
		return (JTextField) component;
	}
	
	public EditText(JComponent component) {
		this.component = component;
	}
	
	public String getText() {
		return ((JTextField)this.component).getText();
	}
	
	public void setText(String text) {
		((JTextField)this.component).setText(text);
	}
	
	public void setOnEditorActionListener(OnEditorActionListener listener) {
		((JTextField)this.component).addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				listener.onEditorAction(EditText.this, 0, new KeyEvent());
			}
		});
	}

	public void setHint(String string) {
		this.component = new HintTextField(string);
	}
}
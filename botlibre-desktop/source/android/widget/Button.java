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

import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import android.graphics.drawable.BitmapDrawable;
import android.view.View;

/**
 * Button proxy for a Swing button.
 */
public class Button extends View {
	
	public Button() {
		
	}
	
	public Button(JComponent component) {
		this.component = component;
	}
	
	public void setText(String text) {
		((JButton)this.component).setText(text);
	}
	
	public JButton getComponent(){
		return ((JButton)this.component);
	}
	
	public void setBackgroundDrawable(BitmapDrawable drawable) {
		
	}
	
	public void setBackgroundResource(String resource) {
		URL url = getClass().getResource("/res/drawable/" + resource + ".png");
		if (url != null) {
			ImageIcon backgroundImage = new ImageIcon(url);
			if (this.width > 0 && this.height > 0) {
				backgroundImage = new ImageIcon(backgroundImage.getImage().getScaledInstance(this.width, this.height, Image.SCALE_SMOOTH));
			}
			((JButton)this.component).setIcon(backgroundImage);
		}
	}

	public void disable(){
		((JButton)this.component).setEnabled(false);
	}
	
}
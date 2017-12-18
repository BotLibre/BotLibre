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
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import android.app.Activity.ImageLabel;
import android.graphics.Bitmap;
import android.view.View;

/**
 * Stub class.
 */
public class ImageView extends View {
	
	public ImageView() {
	}
	
	public ImageView(JComponent component) {
		super(component);
	}
	
	public void setImageBitmap(Bitmap bitmap) {
		
	}
	
	public static void setImage(String name, View view, int width, int height){
		if (view instanceof ImageView) {
			String imagePath = "res/drawable/" + name.toLowerCase() + ".png";
			URL url= ClassLoader.getSystemResource(imagePath);
			if (width > 0 && height > 0) {
				((ImageLabel)view.component).setIcon(new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
			} else {
				((ImageLabel)view.component).setIcon(new ImageIcon(url));
			}
		}
	}
	
	public void setImageResource(String resource, int width, int height) {
		URL url= ClassLoader.getSystemResource(resource);
		if (this.width > 0 && this.height > 0) {
			((ImageLabel)this.component).setIcon(new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH)));
		} else {
			((ImageLabel)this.component).setIcon(new ImageIcon(url));
		}
	}

	public void setImageResource(int imageId) {
		
	}
}
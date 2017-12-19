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

package org.botlibre.sdk.micro;

import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
public class ListRender extends DefaultListCellRenderer {
	private static final long serialVersionUID = 1L;
	Font font = new Font("helvitica", Font.PLAIN, 20);
	 public Map<String, ImageIcon> imageMap;
	    @Override
	    public Component getListCellRendererComponent(
	            JList list, Object value, int index,
	            boolean isSelected, boolean cellHasFocus) {

		JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	        label.setIcon(imageMap.get((String) value));
	        label.setHorizontalTextPosition(JLabel.RIGHT);
	        label.setFont(font);
	        return label;
	    }
	    public Map<String, ImageIcon> createImageMap(String[] list) {
	        Map<String, ImageIcon> map = new HashMap<>();
	        for (String s : list) {
	        	URL url = getClass().getClassLoader().getSystemResource("res/drawable/"+s.toLowerCase()+".png");
	        	ImageIcon icon = new ImageIcon(url);
	        	double widthRatio = 64.0 / icon.getIconWidth();
	            int height = (int)(icon.getIconHeight() * widthRatio);
	            
	            map.put(s, new ImageIcon(icon.getImage().getScaledInstance(64, height, Image.SCALE_SMOOTH)));
	        }
	        return map;
	    }
}

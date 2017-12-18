/******************************************************************************
 *
 *  Copyright 2017 Paphus Solutions Inc.
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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneLayout;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;



import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HintTextField;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.VideoView;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

/**
 * Stub class.
 */
public class Activity extends Context {
	public static final int LAYOUT_INFLATER_SERVICE = 0;
	public static final int RESULT_OK = 1;
	
	public static Activity active;
	
	public static Map<String, String> strings;
	
	public JFrame frame;
	Activity parent;
	JPanel panel;
	Map<String, View> views = new HashMap<>();
	
	public class ExitAction extends WindowAdapter implements ActionListener {
	
		public void actionPerformed(ActionEvent event) {
			active.popActivity();
		}
		
		public void windowClosing(WindowEvent event) {
			active.popActivity();
		}
	}
	
	/**
	 * Attempt to fix JLabel's odd resizing behavior.
	 */
	public class ResizingLabel extends JLabel {
		
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}
	}
	
	public class ImageLabel extends JLabel {
	    ImageIcon imageIcon;
	    
	    public ImageLabel() {
	        super();
	    }
	    
	    public ImageLabel(ImageIcon icon) {
	        super();
	        this.imageIcon = icon;
	    }
	    
	    public void setIcon(ImageIcon icon) {
	    	//super.setIcon(icon);
	        this.imageIcon = icon;
	    }
	    
	    @Override
	    public void paintComponent(Graphics graphics) {
	        super.paintComponent(graphics);
	        if (this.imageIcon == null) {
	        	return;
	        }
			float height = getHeight();
			float width = getWidth();
			float iconHeight = this.imageIcon.getIconHeight();
			float iconWidth = this.imageIcon.getIconWidth();
			float widthRatio = width / iconWidth;
			float heightRatio = height / iconHeight;
			if (widthRatio < heightRatio) {
				height = iconHeight * widthRatio;
				width = iconWidth * widthRatio;
			} else {
				height = iconHeight * heightRatio;
				width = iconWidth * heightRatio;
			}
			int xMargin = (int)(getWidth() - width) / 2;
			int yMargin = (int)(getHeight() - height) / 2;
			
	        graphics.drawImage(this.imageIcon.getImage(),xMargin,yMargin,(int)width,(int)height,this);
	    }
	}
	
	public void openFrame(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignore) {} 
		try {
			active = this;
			this.frame = new JFrame();
			this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			this.frame.setBounds(screenSize.width / 8, screenSize.height / 8, (int)(screenSize.width / 2), (int)(screenSize.height / 1.5));
		
			this.frame.setIconImage(new ImageIcon(getClass().getResource("/res/drawable-mdpi/ic_launcher.png")).getImage()); 
		
			this.panel = new JPanel();
			this.panel.setLayout(new GridBagLayout());
			
			this.frame.getContentPane().setLayout(new GridBagLayout());
			this.frame.getContentPane().add(this.panel, new GridBagConstraints(0,0,1,1, 1.0,1.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0)); 

			this.frame.addWindowListener(new ExitAction());
			
			onCreate(null);
			
			this.frame.setVisible(true);
			
		} catch (Exception failed) {
			failed.printStackTrace();
			System.exit(0);
		}
	}
	
	public void popActivity() {
		if (this.parent == null) {
			int dialogResult = JOptionPane.showConfirmDialog(this.frame, "Exit applicaiton?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
			if(dialogResult == JOptionPane.YES_OPTION) {
				exit();
			}
			return;
		}
		active = this.parent;
		
		this.frame.getContentPane().remove(this.panel);
		this.frame.getContentPane().add(this.parent.panel, new GridBagConstraints(0,0,1,1, 1.0,1.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0)); 
		
		active.onResume();
		
		this.frame.revalidate();
		this.frame.repaint();
	}
	
	public void pushActivity() {
		active = this;
		this.panel = new JPanel();
		this.panel.setLayout(new GridBagLayout());
		
		this.frame.getContentPane().remove(this.parent.panel);
		this.frame.getContentPane().add(this.panel, new GridBagConstraints(0,0,1,1, 1.0,1.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0)); 
		
		onCreate(null);
		
		this.frame.revalidate();
		this.frame.repaint();
	}
	
	public void setTitle(String title) {
		this.frame.setTitle(title);
	}
	
	public void startActivity(Intent intent) {
		try {
			Activity activity = (Activity)intent.activity.newInstance();
			activity.frame = active.frame;
			activity.parent = active;
			activity.pushActivity();
		} catch (Exception exception) {
			Log.wtf("Error", exception);
		}
	}
	
	public void startActivityForResult(Intent intent, int result) {
		
	}
	
	public void onCreate(Bundle savedInstanceState) {
		
	}
	
	public void onDestroy() {
		
	}
	
	public void onResume() {
		
	}

	public void onPostResume() {
		
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}
	
    public boolean onOptionsItemSelected(MenuItem item) {
    	return true;
    }
	
	public MenuInflater getMenuInflater() {
		return new MenuInflater();
	}
	
	public Intent getIntent() {
		return null;
	}
	
	public void finish() {
		popActivity();
	}
	
	public void log(Throwable exception) {
		exception.printStackTrace();
	}
	
	public void setContentView(String layout) {
		try {
			URL url = getClass().getResource("/res/layout/" + layout + ".xml");
			Reader inputReader = new InputStreamReader(url.openStream(), "UTF-8");
			StringWriter output = new StringWriter();
			int next = inputReader.read();
			while (next != -1) {
				output.write(next);
				next = inputReader.read();
			}
			String xml = output.toString();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			StringReader reader = new StringReader(xml);
			InputSource source = new InputSource(reader);
			Document document = parser.parse(source);
			Element root = document.getDocumentElement();

			processContentView(root, this.panel);
			
		} catch (Exception exception) {
			log(exception);
		}
	}

	public void loadStrings() {
		if (strings != null) {
			return;
		}
		strings = new HashMap<String, String>();
		try {
			URL url = getClass().getResource("/res/values/strings.xml");
			Reader inputReader = new InputStreamReader(url.openStream(), "UTF-8");
			StringWriter output = new StringWriter();
			int next = inputReader.read();
			while (next != -1) {
				output.write(next);
				next = inputReader.read();
			}
			String xml = output.toString();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			StringReader reader = new StringReader(xml);
			InputSource source = new InputSource(reader);
			Document document = parser.parse(source);
			Element root = document.getDocumentElement();

			NodeList children = root.getChildNodes();
			for (int index = 0; index < children.getLength(); index++) {
				Node node = children.item(index);
				if (node instanceof Element) {
					if (node.getNodeName().equals("string")) {
						String key = ((Element)node).getAttribute("name");
						String value = node.getTextContent();
						if (key != null && value != null && !value.isEmpty()) {
							strings.put(key, value);
						}
					}
				}
			}
			
		} catch (Exception exception) {
			log(exception);
		}
	}
	
	public String getString(String key) {
		loadStrings();
		if (key == null) {
			return "";
		}
		if (!key.startsWith("@string/")) {
			return key;
		}
		key = key.substring("@stirng/".length(), key.length());
		String value = strings.get(key);
		if (value == null) {
			return key;
		}
		return value;
	}
	
	public void processContentView(Element element, JPanel parent) {
		String orientation = element.getAttribute("android:orientation");
		if (orientation == null) {
			orientation = "vertical";
		}
		boolean vertical = orientation.equals("vertical");
		
		NodeList children = element.getChildNodes();
		int x = 0;
		int y = 0;
		boolean fill = true;
		for (int index = 0; index < children.getLength(); index++) {
			Node node = children.item(index);
			if (node instanceof Element) {
				Element child = (Element)node;
				//System.out.println(child.getNodeName());

				String id = child.getAttribute("android:id");
				View view = null;
				
				double xWeight = 0.0;
				double yWeight = 0.0;
				String layout_weight = child.getAttribute("android:layout_weight");
				if (layout_weight != null && !layout_weight.isEmpty()) {
					double value = Double.valueOf(layout_weight);
					if (vertical) {
						yWeight = value;
					} else {
						xWeight = value;
					}
				}
				String layout_width = child.getAttribute("android:layout_width");
				int width = 0;
				if (layout_width != null) {
					if (layout_width.equals("fill_parent") || layout_width.equals("match_parent")) {
						xWeight = 1.0;
					} else {
						width = getLayoutSize(layout_width);
					}
				}
				String layout_height = child.getAttribute("android:layout_height");
				int height = 0;
				if (layout_height != null) {
					if (layout_height.equals("fill_parent") || layout_height.equals("match_parent")) {
						yWeight = 1.0;
					} else {
						height = getLayoutSize(layout_height);
					}
				}
				if (yWeight != 0.0) {
					fill = false;
				}
				
				Insets insets = new Insets(4,4,4,4);

				String text = child.getAttribute("android:text");
				text = getString(text);
				String background = child.getAttribute("android:background");
				ImageIcon backgroundImage = null;
				if (background != null) {
					if (background.startsWith("@drawable/")) {
						background = background.substring("@drawable/".length(), background.length());
						URL url = getClass().getResource("/res/drawable/" + background + ".png");
						if (url != null) {
							backgroundImage = new ImageIcon(url);
							if (width > 0 && height > 0) {
								backgroundImage = new ImageIcon(backgroundImage.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
							}
						}
					}
				}
				
				if (child.getNodeName().equals("LinearLayout")) {
					JPanel panel = new JPanel();
					view = new View(panel);
					panel.setLayout(new GridBagLayout());
					processContentView((Element)child, panel);
					parent.add(panel, new GridBagConstraints(x,y,1,1, xWeight,yWeight, GridBagConstraints.CENTER,GridBagConstraints.BOTH, insets, 0,0));
				} else if (child.getNodeName().equals("RelativeLayout")) {
					JPanel panel = new JPanel();
					view = new View(panel);
					panel.setLayout(new GridBagLayout());
					processContentView((Element)child, panel);
					parent.add(panel, new GridBagConstraints(x,y,1,1, xWeight,yWeight, GridBagConstraints.CENTER,GridBagConstraints.BOTH, insets, 0,0));
				} else if (child.getNodeName().equals("ScrollView")) {
					JPanel panel = new JPanel();
					view = new View(panel);
					panel.setLayout(new GridBagLayout());
					processContentView((Element)child, panel);
					parent.add(panel, new GridBagConstraints(x,y,1,1, xWeight,yWeight, GridBagConstraints.CENTER,GridBagConstraints.BOTH, insets, 0,0));
				} else if (child.getNodeName().equals("ImageView")) {
					ImageLabel label = new ImageLabel();
					view = new ImageView(label);
					background = child.getAttribute("android:src");
					if (background != null) {
						if (background.startsWith("@drawable/")) {
							background = background.substring("@drawable/".length(), background.length());
							URL url = getClass().getResource("/res/drawable/" + background + ".png");
							if (url != null) {
								backgroundImage = new ImageIcon(url);
								if (width > 0 && height > 0) {
									backgroundImage = new ImageIcon(backgroundImage.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
								}
							}
						}
					}
					if (backgroundImage != null) {
						label.setIcon(backgroundImage);
					}
					parent.add(label, new GridBagConstraints(x,y,1,1, xWeight,yWeight, GridBagConstraints.CENTER,GridBagConstraints.BOTH, insets, 0,0));
				} else if (child.getNodeName().equals("VideoView")) {
					JFXPanel jfxPanel = new JFXPanel();
					view = new VideoView(jfxPanel);
					parent.add(jfxPanel, new GridBagConstraints(x,y,1,1, xWeight,yWeight, GridBagConstraints.CENTER,GridBagConstraints.BOTH, insets, 0,0));
				} else if (child.getNodeName().equals("TextView")) {
					JLabel label = new JLabel();
					view = new TextView(label);
					label.setText(text);
					parent.add(label, new GridBagConstraints(x,y,1,1, xWeight,yWeight, GridBagConstraints.CENTER,GridBagConstraints.BOTH, insets, 0,0));
				} else if (child.getNodeName().equals("ListView")) {
					JScrollPane scroll = new JScrollPane();
					scroll.setPreferredSize(new Dimension(40, 40));
					JList list = new JList();
					scroll.setViewportView(list);
					view = new ListView(list);
					parent.add(scroll, new GridBagConstraints(x,y,1,1, xWeight,yWeight, GridBagConstraints.CENTER,GridBagConstraints.BOTH, insets, 0,0));
				} else if (child.getNodeName().equals("AutoCompleteTextView")) {
					String hint = child.getAttribute("android:hint");
					JTextField field = new JTextField();
					if (hint != null && !hint.isEmpty()) {
						field = new HintTextField(hint);
					}
					view = new AutoCompleteTextView(field);
					if (text != null && !text.isEmpty()) {
						field.setText(text);
					}
					field.setPreferredSize(new Dimension(40, 40));
					parent.add(field, new GridBagConstraints(x,y,1,1, xWeight,yWeight, GridBagConstraints.CENTER,GridBagConstraints.BOTH, insets, 0,0));
				} else if (child.getNodeName().equals("WebView")) {
					//JScrollPane scroll = new JScrollPane();
					//scroll.setPreferredSize(new Dimension(40, 40));
					ResizingLabel label = new ResizingLabel();
					//JLabel label = new JLabel();
					//scroll.setViewportView(label);
					view = new WebView(label);
					label.setText(text);
					label.setFont(new Font("Serif", Font.PLAIN, 20));
					//label.setMaximumSize(new Dimension(5000, 40));
					//label.setPreferredSize(new Dimension(500, 40));
					parent.add(label, new GridBagConstraints(x,y,1,1, xWeight,yWeight, GridBagConstraints.CENTER,GridBagConstraints.BOTH, insets, 0,0));
				} else if (child.getNodeName().equals("EditText")) {
					String hint = child.getAttribute("android:hint");
					JTextField field = new JTextField();
					if (hint != null && !hint.isEmpty()) {
						field = new HintTextField(hint);
					}
					view = new EditText(field);
					if (text != null && !text.isEmpty()) {
						field.setText(text);
					}
					field.setPreferredSize(new Dimension(40, 40));
					field.setMinimumSize(new Dimension(40, 40));
					parent.add(field, new GridBagConstraints(x,y,1,1, xWeight,yWeight, GridBagConstraints.CENTER,GridBagConstraints.BOTH, insets, 0,0));
				} else if (child.getNodeName().equals("Button")) {
					JButton button = new JButton();
					view = new Button(button);
					String onClick = child.getAttribute("android:onClick");
					if (onClick != null && !onClick.isEmpty()) {
						final View actionView = view;
						button.setAction(new AbstractAction() {
							Method method;
							
							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									if (method == null) {
										method = Activity.this.getClass().getMethod(onClick, View.class);
									}
									method.invoke(Activity.this, actionView);
								} catch (Exception exception) {
									System.out.println(exception.getMessage());
									Log.wtf("Reflection error", exception);
								}
							}
						});
					}
					button.setText(text);
					if (backgroundImage != null) {
						button.setIcon(backgroundImage);
						button.setMargin(new Insets(0, 0, 0, 0));
					} else {
						button.setPreferredSize(new Dimension(40, 40));
					}
					//button.setBackground(new Color(arg0, arg1, arg2));
					parent.add(button, new GridBagConstraints(x,y,1,1, xWeight,yWeight, GridBagConstraints.CENTER,GridBagConstraints.BOTH, insets, 0,0));
				} else if (child.getNodeName().equals("ImageButton")) {
					JButton button = new JButton();
					view = new ImageButton(button);
					String onClick = child.getAttribute("android:onClick");
					if (onClick != null && !onClick.isEmpty()) {
						final View actionView = view;
						button.setAction(new AbstractAction() {
							Method method;
							
							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									if (method == null) {
										method = Activity.this.getClass().getMethod(onClick, View.class);
									}
									method.invoke(Activity.this, actionView);
								} catch (Exception exception) {
									Log.wtf("Reflection error", exception);
								}
							}
						});
					}
					button.setText(text);
					if (backgroundImage != null) {
						button.setIcon(backgroundImage);
						button.setMargin(new Insets(0, 0, 0, 0));
					} else {
						button.setPreferredSize(new Dimension(40, 40));
					}
					//button.setBackground(new Color(arg0, arg1, arg2));
					parent.add(button, new GridBagConstraints(x,y,1,1, xWeight,yWeight, GridBagConstraints.CENTER,GridBagConstraints.BOTH, insets, 0,0));
				} else if (child.getNodeName().equals("View")) {
					JPanel panel = new JPanel();
					view = new View(panel);
					parent.add(panel, new GridBagConstraints(x,y,1,1, xWeight,yWeight, GridBagConstraints.CENTER,GridBagConstraints.BOTH, insets, 0,0));
				} else if (child.getNodeName().equals("Spinner")) {
					JComboBox combo = new JComboBox();
					combo.setPreferredSize(new Dimension(40, 40));
					view = new Spinner(combo);
					parent.add(combo, new GridBagConstraints(x,y,1,1, xWeight,yWeight, GridBagConstraints.CENTER,GridBagConstraints.BOTH, insets, 0,0));
				} else if (child.getNodeName().equals("CheckBox")) {
					JCheckBox checkBox = new JCheckBox();
					checkBox.setPreferredSize(new Dimension(40, 40));
					view = new CheckBox(checkBox);
					checkBox.setText(text);
					parent.add(checkBox, new GridBagConstraints(x,y,1,1, xWeight,yWeight, GridBagConstraints.CENTER,GridBagConstraints.BOTH, insets, 0,0));
				}
				view.setWidth(width);
				view.setHeight(height);
				if (id != null && id.startsWith("@+id/") && view != null) {
					String key = id.substring("@+id/".length(), id.length());
					//System.out.println(key);
					this.views.put(key, view);
				}
				if (view != null) {
					view.activity = this;
				}
				if (vertical) {
					y++;
				} else{
					x++;
				}
			}
		}
		if (fill) {
			JPanel panel = new JPanel();
			parent.add(panel, new GridBagConstraints(x,y,1,1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		}
	}
	
	public View findViewById(String id) {
		return this.views.get(id);
	}
	
	public int getLayoutSize(String text) {
		text = text.replace("sp", "");
		text = text.replace("dp", "");
		text = text.replace("px", "");
		try {
			return Integer.valueOf(text);
		} catch (NumberFormatException exception) {
			return 0;
		}
	}
	
	public String getChildElementValue(Element element, String name) {
		NodeList list = ((Element)element).getElementsByTagName(name);
		if (list == null || list.getLength() == 0) {
			return null;
		}
		return list.item(0).getTextContent();
	}
    
    public void exit() {
    	this.frame.dispose();
        System.exit(0);
    }
    
    public SharedPreferences getPreferences(int mode) {
    	return new SharedPreferences();
    }
    
    public String getPackageName() {
    	return "";
    }
    
    public Object getSystemService(int id) {
    	return null;
    }
    
    public Context getApplicationContext() {
    	return this;
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    }
    
    public void openOptionsMenu() {
    	
    }
    
	public void onInit(int status) {
		
	}
}
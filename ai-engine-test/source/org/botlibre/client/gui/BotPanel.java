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
package org.botlibre.client.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.botlibre.Bot;

public class BotPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	/** Reference to Bot instance. **/
	protected Bot bot;
	
	protected List<ChildPanel> childPanels = new ArrayList<ChildPanel>();
	
	private JTabbedPane tabPane;
	
	public BotPanel(Bot bot)
	{
		setBot(bot);
		buildContent();
	}
		
    protected void buildContent() {
		buildTabPane();
    	setLayout(new GridBagLayout());
    	add(tabPane, new GridBagConstraints(0,0,1,1, 1.0,1.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));    	
    }

    protected void buildTabPane() {
      this.tabPane = new JTabbedPane();
      this.tabPane.setTabPlacement(JTabbedPane.RIGHT);
      ImageIcon icon = new ImageIcon(getClass().getResource("typing.gif"));
      icon = new ImageIcon(icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH));
      this.tabPane.addTab("Text", icon, new TextPanel(this));
      icon = new ImageIcon(getClass().getResource("chat.jpg"));
      icon = new ImageIcon(icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH));
      this.tabPane.addTab("IRC", icon, new IRCPanel(this));
      icon = new ImageIcon(getClass().getResource("www.png"));
      icon = new ImageIcon(icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH));
      this.tabPane.addTab("Http", icon, new HttpPanel(this));
      icon = new ImageIcon(getClass().getResource("Email.png"));
      icon = new ImageIcon(icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH));
      this.tabPane.addTab("Email", icon, new EmailPanel(this));
      icon = new ImageIcon(getClass().getResource("twitter.png"));
      icon = new ImageIcon(icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH));
      this.tabPane.addTab("Twitter", icon, new TwitterPanel(this));
      icon = new ImageIcon(getClass().getResource("eye.gif"));
      icon = new ImageIcon(icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH));
      this.tabPane.addTab("Vision", icon, new JPanel());
      icon = new ImageIcon(getClass().getResource("brain.gif"));
      icon = new ImageIcon(icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH));
      this.tabPane.addTab("Bot", icon, new ContextPanel(this));
      icon = new ImageIcon(getClass().getResource("DrawingHands.jpg"));
      icon = new ImageIcon(icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH));
      this.tabPane.addTab("Self", icon, new SelfPanel(this));
      icon = new ImageIcon(getClass().getResource("log-icon.png"));
      icon = new ImageIcon(icon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH));
      this.tabPane.addTab("Log", icon, new StatusPanel(this));
    }

	/**
	 * Return the associated Bot instance.
	 */
	public Bot getBot() {
		return bot;
	}

	/**
	 * Set the associated Bot instance.
	 */
	public void setBot(Bot bot) {
		this.bot = bot;
		for (ChildPanel child : getChildPanels()) {
			child.resetBotInstance();
		}
	}

	public List<ChildPanel> getChildPanels() {
		return childPanels;
	}

	public void setChildPanels(List<ChildPanel> childPanels) {
		this.childPanels = childPanels;
	}

}


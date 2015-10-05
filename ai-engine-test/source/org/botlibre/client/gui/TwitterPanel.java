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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.botlibre.sense.twitter.Twitter;
import org.botlibre.sense.twitter.TwitterDirectMessaging;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TwitterPanel extends ChildPanel 
{
	private static final long serialVersionUID = 1L;

	/** Reference to Bot panel. **/
	protected BotPanel BotPanel;
	
	protected JCheckBox enabledCheckBox;
	protected JTextField oauthKeyText;
	protected JTextField oauthSecretText;
	protected JTextField tokenText;
	protected JTextField tokenSecretText;
	protected JTextField usernameText;
	protected JTextField passwordText;
	protected JButton friendButton;
	protected JTextField friendText;
	protected JList friendsList;
	protected JList followersList;
	protected JList timelineList;
	
	public class EnableAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			try {
				Twitter twitter = getBot().awareness().getSense(Twitter.class);
				twitter.setOauthKey(TwitterPanel.this.oauthKeyText.getText());
				twitter.setOauthSecret(TwitterPanel.this.oauthSecretText.getText());		
				twitter.setToken(TwitterPanel.this.tokenText.getText());
				twitter.setTokenSecret(TwitterPanel.this.tokenSecretText.getText());
				twitter.setUserName(TwitterPanel.this.usernameText.getText());
				twitter.setIsEnabled(TwitterPanel.this.enabledCheckBox.isSelected());
				twitter = getBot().awareness().getSense(TwitterDirectMessaging.class);
				twitter.setOauthKey(TwitterPanel.this.oauthKeyText.getText());
				twitter.setOauthSecret(TwitterPanel.this.oauthSecretText.getText());		
				twitter.setToken(TwitterPanel.this.tokenText.getText());
				twitter.setTokenSecret(TwitterPanel.this.tokenSecretText.getText());
				twitter.setUserName(TwitterPanel.this.usernameText.getText());
				twitter.setIsEnabled(TwitterPanel.this.enabledCheckBox.isSelected());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignore) {}
				if (twitter.isEnabled()) {
					TwitterPanel.this.friendButton.setEnabled(TwitterPanel.this.enabledCheckBox.isSelected());
					if (TwitterPanel.this.enabledCheckBox.isSelected()) {
						TwitterPanel.this.followersList.setListData(new Vector<String>(twitter.getFollowers()));
						TwitterPanel.this.friendsList.setListData(new Vector<String>(twitter.getFriends()));
						TwitterPanel.this.timelineList.setListData(new Vector<String>(twitter.getTimeline()));
					}
				}
			} catch (NumberFormatException exception) {
				getBot().log(this, exception);
				resetState();
			}
		}
	}
	
	public class AddFollowerAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			Twitter twitter = getBot().awareness().getSense(Twitter.class);
			twitter.addFriend(TwitterPanel.this.friendText.getText().trim());
		}
	}

	public TwitterPanel(BotPanel BotPanel) {
		super(BotPanel);
	}
	
	public void resetState() {
		Twitter twitter = getBot().awareness().getSense(Twitter.class);
		if (twitter != null) {
			this.enabledCheckBox.setSelected(twitter.isEnabled());
			this.oauthKeyText.setText(twitter.getOauthKey());
			this.oauthSecretText.setText(twitter.getOauthSecret());
			this.tokenText.setText(twitter.getToken());
			this.tokenSecretText.setText(twitter.getTokenSecret());
			this.usernameText.setText(twitter.getUserName());
		}
		twitter = getBot().awareness().getSense(TwitterDirectMessaging.class);
		if (twitter != null) {
			this.enabledCheckBox.setSelected(twitter.isEnabled());
			this.oauthKeyText.setText(twitter.getOauthKey());
			this.oauthSecretText.setText(twitter.getOauthSecret());
			this.tokenText.setText(twitter.getToken());
			this.tokenSecretText.setText(twitter.getTokenSecret());
			this.usernameText.setText(twitter.getUserName());
		}
	}
	
    protected void buildContent() {
		setLayout(new GridBagLayout());
		
		this.enabledCheckBox = new JCheckBox();
		this.enabledCheckBox.setText("Twitter Enabled");
		this.enabledCheckBox.addActionListener(new EnableAction());
		
		this.oauthKeyText = new JTextField();
		this.oauthSecretText = new JTextField();
		this.tokenText = new JTextField();
		this.tokenSecretText = new JTextField();
		this.usernameText = new JTextField();
		this.passwordText = new JTextField();
		
		this.friendText = new JTextField();
		this.friendButton = new JButton();
		this.friendButton.setText("Add Friend");
		this.friendButton.addActionListener(new AddFollowerAction());
		this.friendsList = new JList();
		JScrollPane friendsScroll = new JScrollPane(this.friendsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.followersList = new JList();
		JScrollPane followersScroll = new JScrollPane(this.followersList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.timelineList = new JList();
		JScrollPane timelineScroll = new JScrollPane(this.timelineList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						
		add(this.enabledCheckBox, new GridBagConstraints(0,0,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Twitter User:"), new GridBagConstraints(0,1,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.usernameText, new GridBagConstraints(1,1,1,1, 1.0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Password:"), new GridBagConstraints(0,2,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.passwordText, new GridBagConstraints(1,2,1,1, 1.0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Oauth Key:"), new GridBagConstraints(0,3,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.oauthKeyText, new GridBagConstraints(1,3,1,1, 1.0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Oauth Secret:"), new GridBagConstraints(0,4,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.oauthSecretText, new GridBagConstraints(1,4,1,1, 1.0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Token:"), new GridBagConstraints(0,5,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.tokenText, new GridBagConstraints(1,5,1,1, 1.0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Token Secret:"), new GridBagConstraints(0,6,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.tokenSecretText, new GridBagConstraints(1,6,1,1, 1.0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.friendButton, new GridBagConstraints(0,7,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.friendText, new GridBagConstraints(1,7,1,1, 1.0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Friends:"), new GridBagConstraints(0,8,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Followers:"), new GridBagConstraints(1,8,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(friendsScroll, new GridBagConstraints(0,9,1,1, 0,0.5, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(followersScroll, new GridBagConstraints(1,9,1,1, 1.0,0.5, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Timeline:"), new GridBagConstraints(0,10,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(timelineScroll, new GridBagConstraints(0,11,2,1, 1.0,0.5, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		
		resetState();
	}
}
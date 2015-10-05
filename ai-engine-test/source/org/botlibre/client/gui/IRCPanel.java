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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.botlibre.sense.chat.IRC;
import org.botlibre.thought.language.Language.LanguageState;
import org.relayirc.chatengine.ChannelAdapter;
import org.relayirc.chatengine.ChannelEvent;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class IRCPanel extends ChildPanel 
{
	private static final long serialVersionUID = 1L;

	protected int maxLog = 10000;

	/** Reference to Bot panel. **/
	protected BotPanel BotPanel;
	
	protected JTextArea outputTextPane;
	protected JScrollPane outputScrollPane;
	protected JComboBox stateComboBox;
	protected JButton connectButton;
	protected JButton disconnectButton;
	protected JButton clearButton;
	protected JComboBox serverComboBox;
	protected JComboBox channelComboBox;
	protected JTextField nickText;
	
	public class ClearAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			outputTextPane.setText("");
		}
	}
	
	public class ConnectAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			// Default to listening.
			IRC sense = (IRC)getBot().awareness().getSense(IRC.class.getName());
			stateComboBox.setSelectedItem(sense.getLanguageState());
			sense.setServerName(((String)serverComboBox.getSelectedItem()).trim());
			sense.setChannelName(((String)channelComboBox.getSelectedItem()).trim());
			String nick = nickText.getText().trim();
			sense.setNick(nick);
			sense.setNickAlt(nick + "_");
			sense.setUserName(nick);
			sense.setRealName(nick);
			sense.connect();
		}
	}
	
	public class DisconnectAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			IRC sense = (IRC)getBot().awareness().getSense(IRC.class.getName());
			sense.disconnect();
		}
	}
		
	public class StateChangedAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			LanguageState state = (LanguageState)stateComboBox.getSelectedItem();

			IRC sense = (IRC)getBot().awareness().getSense(IRC.class.getName());
			sense.setLanguageState(state);
		}
	}
	
	public IRCPanel(BotPanel BotPanel) {
		super(BotPanel);
	}
	
	public void resetState() {	
		IRC sense = (IRC)getBot().awareness().getSense(IRC.class.getName());
		stateComboBox.setSelectedItem(sense.getLanguageState());
	}
	
    protected void buildContent() {
		setLayout(new GridBagLayout());
				
		this.outputTextPane = new JTextArea();
		this.outputTextPane.setEditable(false);
		this.outputScrollPane = new JScrollPane(this.outputTextPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		this.stateComboBox = new JComboBox(LanguageState.values());
		this.stateComboBox.addActionListener(new StateChangedAction());
		
		this.connectButton = new JButton("Connect");
		this.connectButton.addActionListener(new ConnectAction());
		
		this.disconnectButton = new JButton("Disconnect");
		this.disconnectButton.addActionListener(new DisconnectAction());
		
		this.clearButton = new JButton("Clear");
		this.clearButton.addActionListener(new ClearAction());
		
		this.serverComboBox = new JComboBox();
		this.serverComboBox.setEditable(true);
		this.serverComboBox.addItem("irc.freenode.org");
		this.serverComboBox.addItem("irc.icq.com");
		this.serverComboBox.addItem("irc.quakenet.org");
		this.serverComboBox.addItem("irc.efnet.org");
		this.serverComboBox.addItem("irc.undernet.org");
		this.channelComboBox = new JComboBox();
		this.channelComboBox.setEditable(true);
		this.channelComboBox.addItem("#Bot");
		this.channelComboBox.addItem("#ai");
		this.channelComboBox.addItem("##linux");
		this.channelComboBox.addItem("##politics");
		this.channelComboBox.addItem("#teens");
		this.channelComboBox.addItem("#20_something");
		this.channelComboBox.addItem("#30_something");
		this.channelComboBox.addItem("#40_something");
		this.channelComboBox.addItem("#christianity");
		this.channelComboBox.addItem("#buddhism");		
		
		this.nickText = new JTextField();
		
		add(this.outputScrollPane, new GridBagConstraints(0,0,1,15, 1.0,0.5, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.connectButton, new GridBagConstraints(1,0,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.disconnectButton, new GridBagConstraints(1,1,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.clearButton, new GridBagConstraints(1,2,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.stateComboBox, new GridBagConstraints(1,3,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Server:"), new GridBagConstraints(1,4,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.serverComboBox, new GridBagConstraints(1,5,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Channel:"), new GridBagConstraints(1,6,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.channelComboBox, new GridBagConstraints(1,7,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Nick:"), new GridBagConstraints(1,8,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.nickText, new GridBagConstraints(1,9,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		
		resetBotInstance();
	}
	
	/**
	 * Reset any state specific to the Bot instance when changed.
	 */
	public void resetBotInstance() {
		// Set a writer on the text entry into the text sense.
		final IRC sense = (IRC)getBot().awareness().getSense(IRC.class.getName());
		this.nickText.setText(sense.getNick());
		sense.getChannelListeners().add(new ChannelAdapter() {
			public void onMessage(ChannelEvent event) {
				String text = outputTextPane.getText();
				if (text.length() > maxLog) {
					text = text.substring(text.length() - maxLog);
				}				
				outputTextPane.setText(text + "\n"
						+ event.getOriginNick() + ": " + sense.trimSpecialChars((String)event.getValue()));
				resetState();
			}
		});
		resetState();		
	}
}
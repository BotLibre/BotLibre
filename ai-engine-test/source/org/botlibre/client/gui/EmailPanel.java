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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.botlibre.sense.email.Email;

public class EmailPanel extends ChildPanel 
{
	private static final long serialVersionUID = 1L;
	
	protected JCheckBox enabledCheckBox;
	protected JTextField emailAddressText;
	protected JTextField incomingHostText;
	protected JTextField incomingPortText;
	protected JTextField outgoingHostText;
	protected JTextField outgoingPortText;
	protected JTextField protocolText;
	protected JCheckBox isSSLRequiredCheckBox;
	protected JTextField usernameText;
	protected JTextField passwordText;
	protected JTextArea signatureText;
	
	public class EnableAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			try {
				Email email = getBot().awareness().getSense(Email.class);
				email.setEmailAddress(EmailPanel.this.emailAddressText.getText());
				email.setIncomingHost(EmailPanel.this.incomingHostText.getText());			
				email.setIncomingPort(Integer.valueOf(EmailPanel.this.incomingPortText.getText()));
				email.setOutgoingHost(EmailPanel.this.outgoingHostText.getText());
				email.setOutgoingPort(Integer.valueOf(EmailPanel.this.outgoingPortText.getText()));
				email.setProtocol(EmailPanel.this.protocolText.getText());
				email.setUsername(EmailPanel.this.usernameText.getText());
				email.setPassword(EmailPanel.this.passwordText.getText());
				email.setSignature(EmailPanel.this.signatureText.getText());
				email.setSSLRequired(EmailPanel.this.isSSLRequiredCheckBox.isSelected());
				if (email != null) {
					email.setIsEnabled(EmailPanel.this.enabledCheckBox.isSelected());
				}
			} catch (NumberFormatException exception) {
				getBot().log(this, exception);
				resetState();
			}
		}
	}

	public EmailPanel(BotPanel BotPanel) {
		super(BotPanel);
	}
	
	public void resetState() {	
		Email email = getBot().awareness().getSense(Email.class);
		if (email != null) {
			this.enabledCheckBox.setSelected(email.isEnabled());
			this.emailAddressText.setText(email.getEmailAddress());
			this.incomingHostText.setText(email.getIncomingHost());
			this.incomingPortText.setText(String.valueOf(email.getIncomingPort()));
			this.outgoingHostText.setText(email.getOutgoingHost());
			this.outgoingPortText.setText(String.valueOf(email.getOutgoingPort()));
			this.usernameText.setText(email.getUsername());
			this.passwordText.setText(email.getPassword());
			this.protocolText.setText(email.getProtocol());
			this.isSSLRequiredCheckBox.setSelected(email.isSSLRequired());
			this.signatureText.setText(email.getSignature());
		}
	}
	
    protected void buildContent() {
		setLayout(new GridBagLayout());
		
		this.enabledCheckBox = new JCheckBox();
		this.enabledCheckBox.setText("Email Enabled");
		this.enabledCheckBox.addActionListener(new EnableAction());
		
		this.emailAddressText = new JTextField();
		this.incomingHostText = new JTextField();
		this.incomingPortText = new JTextField();
		this.outgoingHostText = new JTextField();
		this.outgoingPortText = new JTextField();
		this.protocolText = new JTextField();
		this.usernameText = new JTextField();
		this.passwordText = new JTextField();
		
		this.signatureText = new JTextArea();
		JScrollPane signatureScrollPane = new JScrollPane(this.signatureText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		
		this.isSSLRequiredCheckBox = new JCheckBox();
		this.isSSLRequiredCheckBox.setText("SSL");
		
		add(this.enabledCheckBox, new GridBagConstraints(0,0,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Email Address:"), new GridBagConstraints(0,1,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.emailAddressText, new GridBagConstraints(1,1,1,1, 1.0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Incoming Host:"), new GridBagConstraints(0,2,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.incomingHostText, new GridBagConstraints(1,2,1,1, 1.0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Incoming Port:"), new GridBagConstraints(0,3,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.incomingPortText, new GridBagConstraints(1,3,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Outgoing Host:"), new GridBagConstraints(0,4,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.outgoingHostText, new GridBagConstraints(1,4,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Outgoing Port:"), new GridBagConstraints(0,5,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.outgoingPortText, new GridBagConstraints(1,5,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Protocol:"), new GridBagConstraints(0,6,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.protocolText, new GridBagConstraints(1,6,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.isSSLRequiredCheckBox, new GridBagConstraints(0,7,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Username:"), new GridBagConstraints(0,8,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.usernameText, new GridBagConstraints(1,8,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Password:"), new GridBagConstraints(0,9,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.passwordText, new GridBagConstraints(1,9,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Signature:"), new GridBagConstraints(0,10,1,1, 0,0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(signatureScrollPane, new GridBagConstraints(0,11,3,3, 1.0,1.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		
		resetState();
	}
}
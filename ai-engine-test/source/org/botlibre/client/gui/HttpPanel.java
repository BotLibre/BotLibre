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
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

import org.botlibre.api.sense.Sense;
import org.botlibre.sense.http.Http;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class HttpPanel extends ChildPanel 
{
	private static final long serialVersionUID = 1L;
	
	protected JComboBox websiteComboBox;
	protected JTextArea outputTextPane;
	protected JScrollPane outputScrollPane;
	protected JButton submitButton;
		
	public class SubmitAction extends AbstractAction implements ActionListener {
		private static final long serialVersionUID = 1L;
		
		public void actionPerformed(ActionEvent event) {
			inputWebsite();
		}
	}
	
	public HttpPanel(BotPanel BotPanel) {
		super(BotPanel);
	}
	
	public void initKeyMap(JTextArea text) {
		Keymap keyMap = JTextArea.addKeymap("EnterSubmit",  text.getKeymap());
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		keyMap.addActionForKeyStroke(key, new SubmitAction());
		text.setKeymap(keyMap);
	}

	/**
	 * Input the website.
	 */
	public void inputWebsite() {
		String input = ((String)websiteComboBox.getSelectedItem()).trim();
		if (input.length() == 0) {
			return;
		}
		if (input.indexOf("http://") == -1 && input.indexOf("https://") == -1) {
			input = "http://" + input;
		}
		try {
			final URL url = new URL(input);
			final Sense sense = getBot().awareness().getSense(Http.class);
			Thread thread = new Thread() {
				@Override
				public void run() {
					sense.input(url);
				}
			};
			thread.start();
		} catch (Exception badURL) {
			getBot().log(this, badURL);
			return;
		}
	}
	
    protected void buildContent() {
		setLayout(new GridBagLayout());
				
		this.websiteComboBox = new JComboBox();
		this.websiteComboBox.addItem("https://en.wiktionary.org/wiki/");
		this.websiteComboBox.addItem("http://www.freebase.com/view/en/");
		this.websiteComboBox.addItem("http://www.");
		this.websiteComboBox.setEditable(true);
		//initKeyMap(this.websiteComboBox);
				
		this.outputTextPane = new JTextArea();
		this.outputTextPane.setEditable(false);
		this.outputScrollPane = new JScrollPane(this.outputTextPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		this.submitButton = new JButton("Submit");
		this.submitButton.addActionListener(new SubmitAction());
		
		add(this.outputScrollPane, new GridBagConstraints(0,1,1,5, 1.0,1.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.websiteComboBox, new GridBagConstraints(0,0,1,1, 1.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.submitButton, new GridBagConstraints(1,0,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
	}
}
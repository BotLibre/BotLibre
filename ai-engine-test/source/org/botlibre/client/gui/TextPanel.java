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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.Writer;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Keymap;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.api.sense.Sense;
import org.botlibre.emotion.EmotionalState;
import org.botlibre.knowledge.BinaryData;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfCompiler;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.sense.text.TextInput;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.util.Utils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TextPanel extends ChildPanel 
{
	private static final long serialVersionUID = 1L;

	/** Reference to Bot panel. **/
	protected BotPanel BotPanel;
	
	protected JTextArea inputTextPane;
	protected JTextPane outputTextPane;
	protected JScrollPane inputScrollPane;
	protected JScrollPane outputScrollPane;
	protected JButton submitButton;
	protected JButton clearButton;
	protected JCheckBox correctionCheckBox;
	protected JCheckBox offensiveCheckBox;
	protected JComboBox stateComboBox;
	protected JComboBox emotionComboBox;
	protected JLabel avatarLabel;
	
	public class ClearAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			inputTextPane.setText("");
			outputTextPane.setText("");
		}
	}
	
	public class SubmitAction extends AbstractAction implements ActionListener {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent event) {
			try {
				String text = inputTextPane.getText();
				TextEntry sense = getBot().awareness().getSense(TextEntry.class);
				if ((text.length() > 3) && text.substring(0, 2).equals("&&")) {
					// Allow Self script execution.
					Network memory = getBot().memory().newMemory();
					Vertex result = SelfCompiler.getCompiler().evaluateExpression(
							text.substring(2, text.length()), sense.getUser(memory), memory.createVertex(Primitive.SELF), false, memory);
					memory.save();
					appendText("Script: " + text.substring(2, text.length()) + "\n");
					appendText("Result: " + Utils.escapeHTML(result.toString()) + "\n");
				} else {
					appendText("You: " + text + "\n");
		
					TextInput textInput = new TextInput(text);
					textInput.setCorrection(correctionCheckBox.isSelected());
					textInput.setOffended(offensiveCheckBox.isSelected());
					sense.input(textInput);
				}
				inputTextPane.setText("");
				correctionCheckBox.setSelected(false);
				offensiveCheckBox.setSelected(false);				
				outputScrollPane.getVerticalScrollBar().setValue(outputScrollPane.getVerticalScrollBar().getMaximum());
			} catch (Exception exception) {
				JOptionPane.showMessageDialog(TextPanel.this,
					exception.getMessage(),
				    "Input error",
				    JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public void appendText(String text) {
		try {			
			HTMLEditorKit kit = (HTMLEditorKit)outputTextPane.getEditorKit();
			StyledDocument document = (StyledDocument)outputTextPane.getDocument();
			kit.insertHTML((HTMLDocument)document, document.getLength(), text, 0, 0, null);
		} catch (Exception ignore) {}
	}
	
	public class StateChangedAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			LanguageState state = (LanguageState)stateComboBox.getSelectedItem();

			TextEntry text = getBot().awareness().getSense(TextEntry.class);
			text.setLanguageState(state);
		}
	}
	
	public class EmotionChangedAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			EmotionalState state = (EmotionalState)emotionComboBox.getSelectedItem();

			TextEntry text = getBot().awareness().getSense(TextEntry.class);
			text.setEmotionalState(state);
		}
	}
	
	public TextPanel(BotPanel BotPanel) {
		super(BotPanel);
	}
	
	public void resetState() {	
		TextEntry text = getBot().awareness().getSense(TextEntry.class);
		this.stateComboBox.setSelectedItem(text.getLanguageState());

		BinaryData image = getBot().avatar().getCurrentImage();
		if (image != null) {
		    ImageIcon icon = new ImageIcon(image.getImageIcon().getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH));
			this.avatarLabel.setIcon(icon);
		}
	}
	
	public void initKeyMap(JTextArea text) {
		Keymap keyMap = JTextArea.addKeymap("EnterSubmit",  text.getKeymap());
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		keyMap.addActionForKeyStroke(key, new SubmitAction());
		text.setKeymap(keyMap);
	}
	
    protected void buildContent() {
		setLayout(new GridBagLayout());
				
		this.inputTextPane = new JTextArea();
		initKeyMap(this.inputTextPane);
		this.inputScrollPane = new JScrollPane(this.inputTextPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		this.outputTextPane = new JTextPane();
		this.outputTextPane.setContentType("text/html");
	    HTMLEditorKit kit = new HTMLEditorKit();
	    HTMLDocument doc = new HTMLDocument();
	    this.outputTextPane.setEditorKit(kit);
	    this.outputTextPane.setDocument(doc);
		this.outputTextPane.setEditable(false);
		this.outputScrollPane = new JScrollPane(this.outputTextPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		DefaultCaret caret = (DefaultCaret) this.outputTextPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		this.submitButton = new JButton("Submit");
		this.submitButton.addActionListener(new SubmitAction());
		
		this.clearButton = new JButton("Clear");
		this.clearButton.addActionListener(new ClearAction());
		
		this.stateComboBox = new JComboBox(LanguageState.values());
		this.stateComboBox.addActionListener(new StateChangedAction());
		
		this.emotionComboBox = new JComboBox(EmotionalState.values());
		this.emotionComboBox.addActionListener(new EmotionChangedAction());

		this.correctionCheckBox = new JCheckBox("Correction");
		this.offensiveCheckBox = new JCheckBox("Offensive");
				
		this.avatarLabel = new JLabel();
		
		add(this.outputScrollPane, new GridBagConstraints(0,0,1,10, 1.0,1.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.inputScrollPane, new GridBagConstraints(0,10,1,10, 1.0,0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,50));
		add(this.submitButton, new GridBagConstraints(1,0,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.clearButton, new GridBagConstraints(1,1,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Language state:"), new GridBagConstraints(1,2,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.stateComboBox, new GridBagConstraints(1,3,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(new JLabel("Emote:"), new GridBagConstraints(1,4,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.emotionComboBox, new GridBagConstraints(1,5,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.correctionCheckBox, new GridBagConstraints(1,6,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.offensiveCheckBox, new GridBagConstraints(1,7,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.avatarLabel, new GridBagConstraints(1,8,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		
		resetBotInstance();		
	}
	
	/**
	 * Reset any state specific to the Bot instance when changed.
	 */
	public void resetBotInstance() {
		// Set a writer on the text entry into the text and voice senses.
		TextEntry text = getBot().awareness().getSense(TextEntry.class);
		if (text != null) {
			text.setWriter(new Writer() {
				public void write(char[] text, int start, int end) {
					String response = new String(text, start, end);
					//response = response.replace("<br/>", "\n");
					appendText("Bot: " + response + "\n");
					appendText("\n");
					//try {
					//	Thread.sleep(10);
					//} catch (Exception ignore) {}
					//outputScrollPane.getVerticalScrollBar().setValue(outputScrollPane.getVerticalScrollBar().getMaximum());
					//outputScrollPane.getVerticalScrollBar().setValue(outputScrollPane.getVerticalScrollBar().getMaximum());
					resetState();
				}
				public void flush() {
					
				}
				public void close() {
					
				}
			});
		}
		resetState();

		Sense sense = getBot().awareness().getSense(TextEntry.class);
		try {
			TextInput textInput = new TextInput(null);
			textInput.setCorrection(correctionCheckBox.isSelected());
			textInput.setOffended(offensiveCheckBox.isSelected());
			sense.input(textInput);
		} catch (Exception exception) {
			JOptionPane.showMessageDialog(TextPanel.this,
				exception.getMessage(),
			    "Input error",
			    JOptionPane.ERROR_MESSAGE);
		}
	}
}
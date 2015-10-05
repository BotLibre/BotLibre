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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.botlibre.LogListener;
import org.botlibre.Bot;
import org.botlibre.util.Utils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class StatusPanel extends ChildPanel 
{
	private static final long serialVersionUID = 1L;
	
	protected int maxLog = 10000;

	/** Reference to Bot panel. **/
	protected BotPanel BotPanel;
	
	protected JButton dumpStatusButton;
	protected JButton dumpMemoryButton;
	protected JButton clearButton;
	protected JTextArea textPane;
	protected JScrollPane scrollPane;
	protected JComboBox logLevelComboBox;

	public class ClearAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			textPane.setText("");
		}
	}
	
	public class LogLevelAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			getBot().setDebugLevel((Level)logLevelComboBox.getSelectedItem());
		}
	}
	
	public class DumpMemoryAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (textPane.getText().length() > 0) {
				textPane.setText(textPane.getText() + "\n");
			}
			textPane.setText(textPane.getText() + "Active Memory:\n");
			synchronized (getBot().memory()) {
				textPane.setText(textPane.getText() + getBot().memory().getActiveMemory().toString());
				textPane.setText(textPane.getText() + "\nShort Term Memory:\n");
				textPane.setText(textPane.getText() + getBot().memory().getShortTermMemory().toString());
				textPane.setText(textPane.getText() + "\nLong Term Memory:\n");
				textPane.setText(textPane.getText() + getBot().memory().getLongTermMemory().toString());
			}
		}
	}
	
	public class DumpStatusAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (textPane.getText().length() > 0) {
				textPane.setText(textPane.getText() + "\n");
			}
			textPane.setText(textPane.getText() + "Bot:\n");
			textPane.setText(textPane.getText() + getBot().fullToString());
		}
	}
	
	public StatusPanel(BotPanel BotPanel) {
		super(BotPanel);
	}
	
    protected void buildContent() {
		setLayout(new GridBagLayout());
		
		this.dumpStatusButton = new JButton("Dump Status");
		this.dumpStatusButton.addActionListener(new DumpStatusAction());
		
		this.dumpMemoryButton = new JButton("Dump Memory");
		this.dumpMemoryButton.addActionListener(new DumpMemoryAction());
		
		this.clearButton = new JButton("Clear");
		this.clearButton.addActionListener(new ClearAction());
				
		this.logLevelComboBox = new JComboBox();
		for (Level level : Bot.LEVELS) {
			this.logLevelComboBox.addItem(level);
		}
		this.logLevelComboBox.setSelectedItem(getBot().getDebugLevel());
		this.logLevelComboBox.addActionListener(new LogLevelAction());

		JLabel logLevelLabel = new JLabel();
		logLevelLabel.setText("Log Level:");
		
		this.textPane = new JTextArea();
		this.textPane.setEditable(false);
		this.scrollPane = new JScrollPane(this.textPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				
		add(this.scrollPane, new GridBagConstraints(0,0,1,6, 1.0,1.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.dumpStatusButton, new GridBagConstraints(1,0,1,1, 0,0, GridBagConstraints.EAST,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.dumpMemoryButton, new GridBagConstraints(1,1,1,1, 0,0, GridBagConstraints.EAST,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.clearButton, new GridBagConstraints(1,2,1,1, 0,0, GridBagConstraints.EAST,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(logLevelLabel, new GridBagConstraints(1,3,1,1, 0,0, GridBagConstraints.EAST,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.logLevelComboBox, new GridBagConstraints(1,4,1,1, 0,0, GridBagConstraints.EAST,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));

		resetBotInstance();
	}
	
	/**
	 * Reset any state specific to the Bot instance when changed.
	 */
	public void resetBotInstance() {
		getBot().addLogListener(new LogListener() {

			/**
			 * Notify a logging level change.
			 */
			public void logLevelChange(Level level) {
				
			}
			
			@Override
			public void log(Object source, String message, Level level, Object[] arguments) {
				StatusPanel.this.textPane.append(Utils.printDate(Calendar.getInstance()) + " - " + level + " -- " + source + ":" + message);
				for (Object argument : arguments) {
					StatusPanel.this.textPane.append(" - " + argument);
				}
				StatusPanel.this.textPane.append("\n");
			}

			@Override
			public void log(Throwable error) {
				StatusPanel.this.textPane.append(Utils.printDate(Calendar.getInstance()) + " - " + Level.SEVERE + " -- " + error.getMessage() + "\n");
				StringWriter writer = new StringWriter();
				PrintWriter printWriter = new PrintWriter(writer);
				error.printStackTrace(printWriter);
				printWriter.flush();
				writer.flush();
				String logText = writer.toString();
				if (logText.length() > StatusPanel.this.maxLog) {
					logText = logText.substring(logText.length() - StatusPanel.this.maxLog);
				}
				StatusPanel.this.textPane.append(writer.toString() + "\n");				
			}
			
		});
	}
}
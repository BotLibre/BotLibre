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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.botlibre.Bot;
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;

/**
 * Defines the client GUI interface.
 * Enables communication, inspection and debugging of Bot.
 */

public class BotFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	public static File lastDirectory;

	/** Reference to Bot instance. **/
	protected Bot bot;
	
	private JMenuBar menuBar;
	private BotPanel BotPanel;
	
	public class ExitAction extends WindowAdapter implements ActionListener {
	
		public void actionPerformed(ActionEvent event) {
			int value = JOptionPane.showConfirmDialog(BotFrame.this, "Do you wish the shutdown Bot?", "Shutdown", JOptionPane.YES_NO_CANCEL_OPTION);
			if (value == JOptionPane.CANCEL_OPTION) {
				return;
			}
			if (value == JOptionPane.YES_OPTION) {
				getBot().shutdown();
				dispose();
				System.exit(0);
			}
			dispose();
		}
		
		public void windowClosing(WindowEvent event) {
			actionPerformed(null);
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		}
	}
	
	public class ShutdownAction implements ActionListener {	
		public void actionPerformed(ActionEvent event) {
			getBot().shutdown();
		}
	}
	
	public class StartupAction implements ActionListener {	
		public void actionPerformed(ActionEvent event) {
			try {
				setBot(Bot.createInstance());
			} catch (Exception failed) {
				failed.printStackTrace();
				JOptionPane.showMessageDialog(BotFrame.this,
				    failed.toString(),
				    "Startup failed",
				    JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public class ImportAction implements ActionListener {	
		public void actionPerformed(ActionEvent event) {
			String database = (String)JOptionPane.showInputDialog(
					BotFrame.this,
                "Enter database name to import:",
                "Import Dialog",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");
			if (database == null) {
				return;
			}
			try {
				getBot().memory().importMemory(database);
			} catch (Exception failed) {
				failed.printStackTrace();
				JOptionPane.showMessageDialog(BotFrame.this,
				    failed.toString(),
				    "Import failed",
				    JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public class LoadChatFileAction implements ActionListener {	
		public void actionPerformed(ActionEvent event) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Select chat log file:");
			if (lastDirectory == null) {
		        lastDirectory = new File(".");
			}
			chooser.setCurrentDirectory(lastDirectory);
	        int value = chooser.showOpenDialog(BotFrame.this);
	        if (value != JFileChooser.APPROVE_OPTION) {
	        	return;
	        }
	        File file = chooser.getSelectedFile();
	        lastDirectory = file.getParentFile();
			try {
				if (file.getName().contains("aiml")) {
					Language language = getBot().mind().getThought(Language.class);
					if (language != null) {
						language.loadAIMLFileAsLog(file, "", false);
					}				
				} else {
					getBot().awareness().getSense(TextEntry.class).loadChatFile(file, "Chat Log", "", true, false);
				}
			} catch (Exception failed) {
				failed.printStackTrace();
				JOptionPane.showMessageDialog(BotFrame.this,
				    failed.toString(),
				    "Import failed",
				    JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public class LoadSelfFileAction implements ActionListener {	
		public void actionPerformed(ActionEvent event) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Select Self file:");
			if (lastDirectory == null) {
		        lastDirectory = new File(".");
			}
			chooser.setCurrentDirectory(lastDirectory);
	        int value = chooser.showOpenDialog(BotFrame.this);
	        if (value != JFileChooser.APPROVE_OPTION) {
	        	return;
	        }
	        File file = chooser.getSelectedFile();
	        lastDirectory = file.getParentFile();
			try {
				if (file.getName().contains("aiml")) {
					getBot().mind().getThought(Language.class).loadAIMLFile(file, true, false, "");					
				} else {
					getBot().mind().getThought(Language.class).loadSelfFile(file, "", true);
				}
			} catch (Exception failed) {
				failed.printStackTrace();
				JOptionPane.showMessageDialog(BotFrame.this,
				    failed.toString(),
				    "Import failed",
				    JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public class SwitchAction implements ActionListener {	
		public void actionPerformed(ActionEvent event) {
			String database = (String)JOptionPane.showInputDialog(
					BotFrame.this,
                "Enter database to switch to:",
                "Instance Switch Dialog",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");
			if (database == null) {
				return;
			}
			try {
				setBot(Bot.createInstanceFromPool(database, false));
			} catch (Exception failed) {
				failed.printStackTrace();
				JOptionPane.showMessageDialog(BotFrame.this,
				    failed.toString(),
				    "Switch failed",
				    JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public class CreateAction implements ActionListener {	
		public void actionPerformed(ActionEvent event) {
			String database = (String)JOptionPane.showInputDialog(
					BotFrame.this,
                "Enter database to create:",
                "Creation Dialog",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");
			if (database == null) {
				return;
			}
			try {
				getBot().memory().createMemory(database);
			} catch (Exception failed) {
				failed.printStackTrace();
				JOptionPane.showMessageDialog(BotFrame.this,
				    failed.toString(),
				    "Creation failed",
				    JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public class DestroyAction implements ActionListener {	
		public void actionPerformed(ActionEvent event) {
			String database = (String)JOptionPane.showInputDialog(
					BotFrame.this,
                "Enter database to destoy *** CAUTION THIS WILL DELETE ALL DATA ***:",
                "Destruction Dialog",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");
			if (database == null) {
				return;
			}
			try {
				getBot().memory().destroyMemory(database);
			} catch (Exception failed) {
				failed.printStackTrace();
				JOptionPane.showMessageDialog(BotFrame.this,
				    failed.toString(),
				    "Destruction failed",
				    JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public class BootstrapAction implements ActionListener {	
		public void actionPerformed(ActionEvent event) {
			try {
				new Bootstrap().bootstrapSystem(getBot(), true);
			} catch (Exception failed) {
				failed.printStackTrace();
				JOptionPane.showMessageDialog(BotFrame.this,
				    failed.toString(),
				    "Bootstrap failed",
				    JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public class RebootstrapAction implements ActionListener {	
		public void actionPerformed(ActionEvent event) {
			try {
				new Bootstrap().rebootstrapMemory(getBot().memory());
			} catch (Exception failed) {
				failed.printStackTrace();
				JOptionPane.showMessageDialog(BotFrame.this,
				    failed.toString(),
				    "Bootstrap failed",
				    JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public class DeleteAllAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			int option = JOptionPane.showConfirmDialog(
					BotFrame.this,
		            "*** CAUTION THIS WILL DELETE ALL DATA ***",
		            "Delete all content dialog",
		            JOptionPane.WARNING_MESSAGE);
			if (option != 0) {
				return;
			}
			if (getBot().memory().getMemoryName().equals("cache")) {
				Bot.systemCache.shutdown();
				Bot.systemCache = null;
			}
			getBot().memory().deleteMemory();
			getBot().shutdown();
			if (getBot().memory().getMemoryName().equals("cache")) {
				Bot.systemCache = Bot.createInstance(Bot.CONFIG_FILE, "cache", false);
			}
			setBot(Bot.createInstance(Bot.CONFIG_FILE, getBot().memory().getMemoryName(), false));
		}
	}
	
	public class SpawnAction implements ActionListener {	
		public void actionPerformed(ActionEvent event) {
			BotFrame frame = new BotFrame();
			frame.setVisible(true); 
		}
	}
	
	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignore) {}
		
		System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
		try {
			BotFrame frame = new BotFrame();
			frame.setVisible(true);
		} catch (Exception failed) {
			failed.printStackTrace();
			System.exit(0);
		}
	}
	
	public BotFrame() {	
        super("Bot");
        
        Bot.systemCache = Bot.createInstance(Bot.CONFIG_FILE, "cache", false);
        
		setBot(Bot.createInstance());
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(screenSize.width / 8, screenSize.height / 8, (int)(screenSize.width / 1.5), (int)(screenSize.height / 1.5));
		setIconImage(new ImageIcon(getClass().getResource("Bot.gif")).getImage());        
		addWindowListener(new ExitAction());
		
		buildMenus();
		buildContent();
	}
		
    protected void buildContent() {
		this.BotPanel = new BotPanel(getBot());
    	getContentPane().setLayout(new GridBagLayout());
    	getContentPane().add(this.BotPanel, new GridBagConstraints(0,0,1,1, 1.0,1.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));    	
    }
    	
    protected void buildMenus() {
	    this.menuBar = new JMenuBar();
		this.menuBar.setOpaque(true);
		JMenu fileMenu = buildFileMenu();
		JMenu editMenu = new JMenu("Edit");
		JMenu helpMenu = new JMenu("Help");
		
		this.menuBar.add(fileMenu);
		this.menuBar.add(editMenu);
		this.menuBar.add(helpMenu);
		
		setJMenuBar(this.menuBar);	
    }

    protected JMenu buildFileMenu() {
		JMenu fileMenu = new JMenu("Bot");

		JMenuItem spawnMenuItem = new JMenuItem("Spawn");
		spawnMenuItem.addActionListener(new SpawnAction());
		
		JMenuItem bootstrapMenuItem = new JMenuItem("Bootstrap");
		bootstrapMenuItem.addActionListener(new BootstrapAction());
		
		JMenuItem rebootstrapMenuItem = new JMenuItem("Rebootstrap");
		rebootstrapMenuItem.addActionListener(new RebootstrapAction());
		
		JMenuItem deleteMenuItem = new JMenuItem("Delete all content");
		deleteMenuItem.addActionListener(new DeleteAllAction());

		JMenuItem createMenuItem = new JMenuItem("Create...");
		createMenuItem.addActionListener(new CreateAction());
		
		JMenuItem destroyMenuItem = new JMenuItem("Destroy...");
		destroyMenuItem.addActionListener(new DestroyAction());
		
		JMenuItem importMenuItem = new JMenuItem("Import...");
		importMenuItem.addActionListener(new ImportAction());
		
		JMenuItem switchMenuItem = new JMenuItem("Switch...");
		switchMenuItem.addActionListener(new SwitchAction());
		
		JMenuItem startupMenuItem = new JMenuItem("Startup");
		startupMenuItem.addActionListener(new StartupAction());
		
		JMenuItem shutdownMenuItem = new JMenuItem("Shutdown");
		shutdownMenuItem.addActionListener(new ShutdownAction());
		
		JMenuItem loadChatLogMenuItem = new JMenuItem("Load chat log...");
		loadChatLogMenuItem.addActionListener(new LoadChatFileAction());
		
		JMenuItem loadSelfFileMenuItem = new JMenuItem("Load Self file...");
		loadSelfFileMenuItem.addActionListener(new LoadSelfFileAction());
				
		JMenuItem exitMenuItem = new JMenuItem("Exit");	
		exitMenuItem.addActionListener(new ExitAction());

		fileMenu.add(spawnMenuItem);
		fileMenu.add(bootstrapMenuItem);
		fileMenu.add(rebootstrapMenuItem);
		fileMenu.add(deleteMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(createMenuItem);
		fileMenu.add(destroyMenuItem);
		fileMenu.add(importMenuItem);
		fileMenu.add(switchMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(startupMenuItem);
		fileMenu.add(shutdownMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(loadChatLogMenuItem);
		fileMenu.add(loadSelfFileMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);
		
		return fileMenu;
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
		this.bot.mind().getThought(Language.class).setLearningMode(LearningMode.Disabled);
		if (this.BotPanel != null) {
			this.BotPanel.setBot(bot);
		}
	}
    
    public void exit() {
    	dispose();
        System.exit(0);
    }

}


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
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfCompiler;
import org.botlibre.self.SelfDecompiler;
import org.botlibre.thought.consciousness.Consciousness;
import org.botlibre.thought.language.Comprehension;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SelfPanel extends ChildPanel 
{
	private static final long serialVersionUID = 1L;

	/** Reference to Bot panel. **/
	protected BotPanel BotPanel;
	
	protected JTextArea codeTextPane;
	protected JScrollPane scrollPane;
	protected JTextArea errorTextPane;
	protected JScrollPane errorScrollPane;
	protected JButton newButton;
	protected JButton deleteButton;
	protected JButton resetButton;
	protected JComboBox stateMachineComboBox;
	protected JCheckBox comprehensionCheckBox;
	protected JCheckBox learningCheckBox;
	protected JCheckBox matchFirstCheckBox;
	protected JCheckBox grammarCheckBox;
	protected JCheckBox consciousnessCheckBox;
		
	public class DeleteAction extends AbstractAction implements ActionListener {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent event) {
			Vertex stateMachine = (Vertex)stateMachineComboBox.getSelectedItem();
			if (stateMachine == null) {
				resetState();
				return;				
			}
			Network memory = getBot().memory().newMemory();
			try {
				Vertex language = memory.createVertex(Language.class);
				stateMachine = memory.createVertex(stateMachine);
				stateMachine.setPinned(false);
				for (Relationship relationship : language.getRelationships(Primitive.STATE)) {
					if (relationship.getTarget().equals(stateMachine)) {
						language.internalRemoveRelationship(relationship);
						break;
					}
				}
				memory.save();
				resetState();
			} catch (Exception failed) {
				memory.clear();
				errorTextPane.setText(failed.getMessage());
				return;
			}
			errorTextPane.setText("");
		}
	}
	
	public class ResetAction extends AbstractAction implements ActionListener {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent event) {
			resetState();
		}
	}
	
	public class ComprehensionAction extends AbstractAction implements ActionListener {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent event) {
			getBot().mind().getThought(Comprehension.class).setEnabled(comprehensionCheckBox.isSelected());
		}
	}
	
	public class LearningAction extends AbstractAction implements ActionListener {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent event) {
			if (learningCheckBox.isSelected()) {
				getBot().mind().getThought(Language.class).setLearningMode(LearningMode.Everyone);
			} else {
				getBot().mind().getThought(Language.class).setLearningMode(LearningMode.Disabled);
			}
		}
	}
	
	public class ConsciousnessAction extends AbstractAction implements ActionListener {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent event) {
			if (consciousnessCheckBox.isSelected()) {
				getBot().mind().getThought(Consciousness.class).setEnabled(true);
			} else {
				getBot().mind().getThought(Consciousness.class).setEnabled(false);
			}
		}
	}
	
	public class GrammarAction extends AbstractAction implements ActionListener {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent event) {
			if (learningCheckBox.isSelected()) {
				getBot().mind().getThought(Language.class).setLearnGrammar(true);
			} else {
				getBot().mind().getThought(Language.class).setLearnGrammar(false);
			}
		}
	}
	
	public class MatchFirstAction extends AbstractAction implements ActionListener {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent event) {
			if (matchFirstCheckBox.isSelected()) {
				getBot().mind().getThought(Language.class).setCheckExactMatchFirst(true);
			} else {
				getBot().mind().getThought(Language.class).setCheckExactMatchFirst(false);
			}
		}
	}
	
	public class NewAction extends AbstractAction implements ActionListener {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent event) {
			String code = codeTextPane.getText();
			Network memory = getBot().memory().newMemory();
			try {
				Vertex stateMachine = SelfCompiler.getCompiler().parseStateMachine(code, true, memory);
				//stateMachine.pinDescendants();
				Vertex language = memory.createVertex(Language.class);
				language.addRelationship(Primitive.STATE, stateMachine);
				memory.save();
				errorTextPane.setText("");
				resetState();
			} catch (Exception failed) {
				memory.clear();
				errorTextPane.setText(failed.getMessage());
				getBot().log(this, failed);
				return;
			}
			errorTextPane.setText("");
		}
	}
	
	public class StateChangedAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			try {
				Vertex stateMachine = (Vertex)stateMachineComboBox.getSelectedItem();
				if (stateMachine == null) {
					return;
				}
				codeTextPane.setText(SelfDecompiler.getDecompiler().decompileStateMachine(stateMachine, stateMachine.getNetwork()));
			} catch (Exception failed) {
				failed.printStackTrace();
				errorTextPane.setText(failed.getMessage());
				return;
			}
			errorTextPane.setText("");
		}
	}
	
	public SelfPanel(BotPanel BotPanel) {
		super(BotPanel);
	}
	
	public void resetState() {
		try {
			Vector<Vertex> states = new Vector<Vertex>();
			Network memory = getBot().memory().newMemory();
			List<Relationship> relationships = memory.createVertex(Language.class).orderedRelationships(Primitive.STATE);
			if (relationships != null) {
				for (Relationship relationship : relationships) {
					states.add(relationship.getTarget());
				}
			}
			this.stateMachineComboBox.setModel(new DefaultComboBoxModel(states));
		} catch (Exception failed) {
			errorTextPane.setText(failed.getMessage());
			return;
		}
		errorTextPane.setText("");
	}
		
    protected void buildContent() {
		setLayout(new GridBagLayout());
				
		this.codeTextPane = new JTextArea();
		this.scrollPane = new JScrollPane(this.codeTextPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		this.errorTextPane = new JTextArea();
		this.errorTextPane.setEditable(false);
		this.errorScrollPane = new JScrollPane(this.errorTextPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		this.deleteButton = new JButton("Remove state machine");
		this.deleteButton.addActionListener(new DeleteAction());
		
		this.newButton = new JButton("New state machine");
		this.newButton.addActionListener(new NewAction());
		
		this.resetButton = new JButton("Refresh");
		this.resetButton.addActionListener(new ResetAction());

		this.comprehensionCheckBox = new JCheckBox("Comprehension");
		this.comprehensionCheckBox.addActionListener(new ComprehensionAction());
		this.comprehensionCheckBox.setSelected(true);
		this.learningCheckBox = new JCheckBox("Learning");
		this.learningCheckBox.addActionListener(new LearningAction());
		this.learningCheckBox.setSelected(false);
		this.matchFirstCheckBox = new JCheckBox("Match First");
		this.matchFirstCheckBox.addActionListener(new MatchFirstAction());
		this.matchFirstCheckBox.setSelected(true);
		this.consciousnessCheckBox = new JCheckBox("Consciousness");
		this.consciousnessCheckBox.addActionListener(new ConsciousnessAction());
		this.consciousnessCheckBox.setSelected(true);
		this.grammarCheckBox = new JCheckBox("Grammar");
		this.grammarCheckBox.addActionListener(new GrammarAction());
		this.grammarCheckBox.setSelected(true);
		
		this.stateMachineComboBox = new JComboBox();
		this.stateMachineComboBox.addActionListener(new StateChangedAction());
		
		add(this.scrollPane, new GridBagConstraints(0,0,1,10, 1.0,1.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.errorScrollPane, new GridBagConstraints(0,10,1,6, 1.0,0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,100));
		add(this.newButton, new GridBagConstraints(1,0,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.deleteButton, new GridBagConstraints(1,1,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.resetButton, new GridBagConstraints(1,2,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.stateMachineComboBox, new GridBagConstraints(1,3,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.comprehensionCheckBox, new GridBagConstraints(1,4,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.learningCheckBox, new GridBagConstraints(1,5,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.consciousnessCheckBox, new GridBagConstraints(1,6,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.grammarCheckBox, new GridBagConstraints(1,7,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.matchFirstCheckBox, new GridBagConstraints(1,8,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		
		resetState();
	}
}
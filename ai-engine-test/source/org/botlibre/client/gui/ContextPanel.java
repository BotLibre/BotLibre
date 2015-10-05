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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeModelListener;
import javax.swing.text.Keymap;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.context.Context;
import org.botlibre.sense.context.Selector;

public class ContextPanel extends ChildPanel
{
	private static final long serialVersionUID = 1L;
	
	protected JTree treePane;
	protected JLabel selectedLabel;
	protected JTextArea selectedPane;
	protected JLabel relationshipLabel;
	protected JTextArea relationshipPane;
	protected JLabel filterLabel;
	protected JTextArea filterPane;
	protected JScrollPane scrollPane;
	protected JButton selectButton;
	protected JButton relationshipButton;
	protected JButton associateButton;
	protected JButton instantiateButton;
	protected JButton refreshButton;
	protected JButton deleteButton;
	protected JButton referencesButton;
	protected JRadioButton shortTermButton;
	protected JRadioButton longTermButton;
	
	protected Vertex selection;
	protected Vertex relationship;
	
	public class SelectAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (treePane.getSelectionPath() == null) {
				return;
			}
			Object selected = treePane.getSelectionPath().getLastPathComponent();
			if (selected instanceof Relationship) {
				selection = ((Relationship)selected).getTarget();				
			} else if (selected instanceof Vertex) {
				selection = (Vertex)selected;				
			}
			if (selection != null) {
				selectedPane.setText(selection.toString());
				getBot().awareness().getSense(Context.class.getName()).input(selection);
			}
		}
	}
	
	public class RelationshipAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (treePane.getSelectionPath() == null) {
				return;
			}
			Object selected = treePane.getSelectionPath().getLastPathComponent();
			if (selected instanceof Relationship) {
				relationship = ((Relationship)selected).getTarget();				
			} else if (selected instanceof Vertex) {
				relationship = (Vertex)selected;				
			}
			if (relationship != null) {
				relationshipPane.setText(relationship.toString());				
			}
		}
	}
	
	public class DeleteAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (treePane.getSelectionPath() == null) {
				return;
			}
			Object selected = treePane.getSelectionPath().getLastPathComponent();
			if (selected instanceof Relationship) {
				Relationship relationship = (Relationship)selected;
				relationship.getSource().internalRemoveRelationship(relationship);
			} else if (selected instanceof Vertex) {
				Vertex vertex = (Vertex)selected;
				getSelectedNetwork().removeVertexAndReferences(vertex);
			}
			getBot().memory().save();
		}
	}
	
	/**
	 * Create a new vertex that is an instance of the current selection.
	 */
	public class InstantiateAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (treePane.getSelectionPath() == null) {
				return;
			}
			Object selected = treePane.getSelectionPath().getLastPathComponent();
			Vertex classification = null;
			if (selected instanceof Relationship) {
				classification = ((Relationship)selected).getTarget();				
			} else if (selected instanceof Vertex) {
				classification = (Vertex)selected;				
			}
			if (classification != null) {
				Network network = getBot().memory().getShortTermMemory();
				Vertex instance = network.createVertex();
				Vertex instantiationType = network.createVertex(Primitive.INSTANTIATION);
				classification = network.createVertex(classification);
				instance.addRelationship(instantiationType, classification);
				selection = instance;
				selectedPane.setText(selection.toString());
				getBot().memory().save();
				getBot().awareness().getSense(Context.class.getName()).input(selection);				
			}
		}
	}

	/**
	 * Associate the selected vertex with the selected relationship, with the current selection as target.
	 */
	public class AssociateAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			Object selected = treePane.getSelectionPath().getLastPathComponent();
			Vertex target = null;
			if (selected instanceof Relationship) {
				target = ((Relationship)selected).getTarget();				
			} else if (selected instanceof Vertex) {
				target = (Vertex)selected;				
			}
			if ((target != null) && (relationship != null) && (selection != null)) {
				Network network = getBot().memory().getShortTermMemory();
				Vertex source = network.createVertex(selection);
				Vertex relationshipType = network.createVertex(relationship);
				target = network.createVertex(target);
				source.addRelationship(relationshipType, target);
				getBot().memory().save();			
			}
		}
	}
	
	public class RefreshAction extends AbstractAction implements ActionListener {
		private static final long serialVersionUID = 1L;
		
		public void actionPerformed(ActionEvent event) {
			getSelectedNetwork().clear();
			resetState();
		}
	}
	
	public class ReferencesAction extends AbstractAction implements ActionListener {
		private static final long serialVersionUID = 1L;
		
		public void actionPerformed(ActionEvent event) {
			if (treePane.getSelectionPath() == null) {
				return;
			}
			Object selected = treePane.getSelectionPath().getLastPathComponent();
			if (!(selected instanceof Vertex)) {
				return;
			}
			long id = ((Vertex)selected).getId();
			filterPane.setText("jpql: Select v from Vertex v join v.allRelationships r where r.target.id = " + id);
			resetState();
		}
	}
	
	public class ShortTermAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			shortTermButton.setSelected(true);
			longTermButton.setSelected(false);
			resetState();
		}
	}
	
	public class LongTermAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			longTermButton.setSelected(true);
			shortTermButton.setSelected(false);
			resetState();
		}
	}
	
	public ContextPanel(BotPanel BotPanel) {
		super(BotPanel);
	}

	public Network getSelectedNetwork() {
		if (this.longTermButton.isSelected()) {
			return getBot().memory().getLongTermMemory();
		} else {
			return getBot().memory().getShortTermMemory();
		}
	}
	
	public void resetState() {
		TreeModel model = new TreeModel() {
			/** Stores list of root vertices. */
			List<Vertex> vertices;
			/** Stores list of relationships per vertex. */
			Map<Vertex, List<Relationship>> relationships = new HashMap<Vertex, List<Relationship>>();
			
			@SuppressWarnings("unchecked")
			List<Vertex> getVertices() {
				if (this.vertices == null) {
	    			this.vertices = new ArrayList<Vertex>();
	    			List<Vertex> result = null;
    				String filter = filterPane.getText().trim();
    				if ((filter.length() > 5) && (filter.substring(0, 5).equals("jpql:"))) {
    					result = getSelectedNetwork().findAllQuery(filter.substring(5, filter.length()));    					
    				} else if (filter.length() > 0) {
    					result = getSelectedNetwork().findAllLike(filter);
    				} else {
    					result = getSelectedNetwork().findAll();
    				}
	    			this.vertices.addAll(result);
	    		}
				return this.vertices;
			}
			
			List<Relationship> getRelationships(Vertex vertex) {
				List<Relationship> parentRelationships = this.relationships.get(vertex);
	    		if (parentRelationships == null) {
	    			parentRelationships = new ArrayList<Relationship>();
	    			Iterator<Relationship> iterator = vertex.orderedAllRelationships();
					while (iterator.hasNext()) {
						parentRelationships.add(iterator.next());
	    			}
	    			this.relationships.put(vertex, parentRelationships);
	    		}
	    		return parentRelationships;
			}
			
		    public Object getRoot() {
		    	return getBot().memory();
		    }

		    public Object getChild(Object parent, int index) {
		    	Object child = null;
		    	if (parent == getBot().memory()) {
		    		child = getVertices().get(index);
		    	} else if (parent instanceof Relationship) {
		    		if (index == 0) {
		    			child = ((Relationship)parent).getType();
		    		} else if (index == 1) {
		    			child = ((Relationship)parent).getTarget();
		    		} else {
		    			child = ((Relationship)parent).getMeta();
		    		}
		    	} else if (parent instanceof Vertex) {
		    		child = getRelationships((Vertex)parent).get(index);
		    	}
		    	return child;
		    }

		    public int getChildCount(Object parent) {
		    	int count = 0;
		    	if (parent == getBot().memory()) {
		    		count = getVertices().size();
		    	} else if (parent instanceof Relationship) {
		    		if (((Relationship)parent).getMeta() != null) {
		    			count = 3;
		    		} else {
		    			count = 2;
		    		}
		    	} else if (parent instanceof Vertex) {
		    		count = getRelationships((Vertex)parent).size();
		    	}
		    	return count;
		    }
		    public boolean isLeaf(Object node) {
		    	return false;
		    }

		    public void valueForPathChanged(TreePath path, Object newValue) {
		    	
		    }

		    public int getIndexOfChild(Object parent, Object child) {
		    	int index = -1;
		    	if (parent == getBot().memory()) {
		    		index = getVertices().indexOf(child);
		    	} else if (parent instanceof Relationship) {
		    		if (child == ((Relationship)parent).getType()) {
		    			index = 0;
		    		} else if (child == ((Relationship)parent).getTarget()) {
		    			index = 1;
		    		} else {
		    			index = 2;
		    		}
		    	} else if (parent instanceof Vertex) {
		    		index = getRelationships((Vertex)parent).indexOf(child);
		    	}
		    	return index;
		    }

		    public void addTreeModelListener(TreeModelListener l) {
		    	
		    }

		    public void removeTreeModelListener(TreeModelListener l) {
		    	
		    }

		};
		this.treePane.setModel(model);
	}
	
	public void initKeyMap(JTextArea text) {
		Keymap keyMap = JTextArea.addKeymap("EnterSubmit",  text.getKeymap());
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		keyMap.addActionForKeyStroke(key, new RefreshAction());
		text.setKeymap(keyMap);
	}
	
    protected void buildContent() {
		setLayout(new GridBagLayout());
				
		this.treePane = new JTree();
		this.scrollPane = new JScrollPane(this.treePane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.selectedPane = new JTextArea();
		this.selectedPane.setEditable(false);
		this.selectedPane.setBorder(LineBorder.createGrayLineBorder());
		
		this.selectedLabel = new JLabel();
		this.selectedLabel.setText("Selection:");
		
		this.relationshipPane = new JTextArea();
		this.relationshipPane.setEditable(false);
		this.relationshipPane.setBorder(LineBorder.createGrayLineBorder());
		
		this.relationshipLabel = new JLabel();
		this.relationshipLabel.setText("Relationship:");
		
		this.filterPane = new JTextArea();
		initKeyMap(this.filterPane);
		this.filterPane.setBorder(LineBorder.createGrayLineBorder());
		
		this.filterLabel = new JLabel();
		this.filterLabel.setText("Filter:");
		
		this.selectButton = new JButton("Select");
		this.selectButton.addActionListener(new SelectAction());
		
		this.relationshipButton = new JButton("Relationship");
		this.relationshipButton.addActionListener(new RelationshipAction());
		
		this.associateButton = new JButton("Associate");
		this.associateButton.addActionListener(new AssociateAction());
		
		this.instantiateButton = new JButton("Instantiate");
		this.instantiateButton.addActionListener(new InstantiateAction());
		
		this.refreshButton = new JButton("Refresh");
		this.refreshButton.addActionListener(new RefreshAction());
		
		this.referencesButton = new JButton("References");
		this.referencesButton.addActionListener(new ReferencesAction());
		
		this.deleteButton = new JButton("Delete");
		this.deleteButton.addActionListener(new DeleteAction());
		
		this.shortTermButton = new JRadioButton("Short Term");
		this.shortTermButton.addActionListener(new ShortTermAction());
		this.shortTermButton.setSelected(true);
		
		this.longTermButton = new JRadioButton("Long Term");
		this.longTermButton.addActionListener(new LongTermAction());
		
		add(this.scrollPane, new GridBagConstraints(0,0,2,10, 1.0,1.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.selectedLabel, new GridBagConstraints(0,11,1,1, 0,0, GridBagConstraints.WEST,GridBagConstraints.NONE, new Insets(4,4,4,4), 0,0));
		add(this.selectedPane, new GridBagConstraints(1,11,1,1, 1.0,0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.relationshipLabel, new GridBagConstraints(0,12,1,1, 0,0, GridBagConstraints.WEST,GridBagConstraints.NONE, new Insets(4,4,4,4), 0,0));
		add(this.relationshipPane, new GridBagConstraints(1,12,1,1, 1.0,0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.filterLabel, new GridBagConstraints(0,13,1,1, 0,0, GridBagConstraints.WEST,GridBagConstraints.NONE, new Insets(4,4,4,4), 0,0));
		add(this.filterPane, new GridBagConstraints(1,13,1,1, 1.0,0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.shortTermButton, new GridBagConstraints(2,0,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.longTermButton, new GridBagConstraints(2,1,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.refreshButton, new GridBagConstraints(2,2,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.referencesButton, new GridBagConstraints(2,3,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.selectButton, new GridBagConstraints(2,4,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.relationshipButton, new GridBagConstraints(2,5,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.associateButton, new GridBagConstraints(2,6,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.instantiateButton, new GridBagConstraints(2,7,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));
		add(this.deleteButton, new GridBagConstraints(2,8,1,1, 0.0,0.0, GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(4,4,4,4), 0,0));

		resetBotInstance();
	}
	
	/**
	 * Reset any state specific to the Bot instance when changed.
	 */
	public void resetBotInstance() {
		Context sense = (Context)getBot().awareness().getSense(Context.class.getName());
		sense.setSelector(new Selector() {
			public Object getSelection() {
				return selection;
			}
			
			public void setSelection(Object selection) {
				
			}
		});		
		resetState();
	}
}
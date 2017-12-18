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

package android.widget;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * ListView proxy for a Swing list.
 */
public class ListView extends View {
	public static int TRANSCRIPT_MODE_ALWAYS_SCROLL = -1;
	DefaultListModel model = new DefaultListModel();
	public ListView() {
		
	}
	public void transcriptMode(){
		int lastIndex = ((JList)this.component).getModel().getSize() - 1;
		if (lastIndex >= 0) {
		((JList)this.component).ensureIndexIsVisible(lastIndex);
		}
	}
	public ListView(JComponent component) {
		super(component);
	}
	
	public int getCheckedItemPosition() {
		return ((JList)this.component).getSelectedIndex();
	}
	
	public void setSelection(int index) {
		((JList)this.component).setSelectedIndex(index);
	}
	
	public void setAdapter(ArrayAdapter adapter) {
		
		for (Object element : adapter.array) {
			model.addElement(element);
		}
		((JList)this.component).setModel(model);
	}
	public void setList(String [] list){
		for(Object element : list){
			model.addElement(element);
		}
		((JList)this.component).setModel(model);
	}
	
	public void setOnItemSelectedListener(OnItemSelectedListener listener) {
		
	}
	
	public void setOnTouchListener(View.OnTouchListener listner) {
		
	}
	
	public void clear(){
		DefaultListModel listModel = (DefaultListModel) ((JList)this.component).getModel();
        listModel.removeAllElements();
	}
	
	public void addItem(String s){
		model.addElement(s);
		((JList)this.component).setModel(model);
	}
	
	public void setTranscriptMode(int mode) {
		((JList)this.component).ensureIndexIsVisible(((JList)this.component).getModel().getSize() - mode);
	}
	
	public int getCount() {
		return ((JList)this.component).getModel().getSize();
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		
		
	}

	public Object getItemAtPosition(int position) {
		return ((JList)this.component).getModel().getElementAt(position);
	}
	
	public void setCellRender(ListCellRenderer re){
		((JList)this.component).setCellRenderer(re);
	}
	
}
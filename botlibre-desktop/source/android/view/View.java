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

package android.view;

import java.awt.Component;

import javax.swing.JComponent;

import android.app.Activity;
import android.content.Context;

/**
 * View proxy for a Swing component.
 */
public class View {
	public static final int FOCUS_DOWN = 3;
	public static int GONE = 0;
	public static int VISIBLE = 1;
	
	public Activity activity;
	public Object tag;
	public int width;
	public int height;
	
	public static interface OnClickListener {
		public void onClick(View v);
	}
	public static interface OnTouchListener {
 	   boolean onTouch(View v, MotionEvent event);
	}
	
	public JComponent component;
	public OnClickListener onClickListener;
	
	public View() {
		
	}
	
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}



	public Component getComponent(){
		return this.component.getComponent(0);
	}
	public int getX(){
		return this.component.getX();
	}
	public int getY(){
		return this.component.getY();
	}
	
	public View(JComponent component) {
		this.component = component;
	}
	
	public Context getContext() {
		return this.activity;
	}
	
	public void post(Runnable runnable) {
		new Thread(runnable).start();
	}
	
	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}
	
	public void setOnTouchListener(OnTouchListener listener) {
		
	}
	
	public void setVisibility(int code) {
		if (code == GONE) {
			this.component.setVisible(false);
		} else if (code == VISIBLE) {
			this.component.setVisible(true);
		}
	}
	
	public View findViewById(String id) {
		return this.activity.findViewById(id);
	}
	
	public View getResources() {
		return this;
	}
	
	public int getCheckedItemPosition() {
		return 0;
	}

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}
	
	public int getVisibility() {
		return View.VISIBLE;
	}
	
	public void invalidateViews() {
		
	}
	
	public void setBackgroundResource(String resource) {
		
	}

	public void setEnabled(boolean code) {
		this.component.setEnabled(code);
	}

	public void setAlpha(float f) {

	}

	
}
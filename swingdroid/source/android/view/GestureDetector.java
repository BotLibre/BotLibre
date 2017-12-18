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

import android.content.Context;

/**
 * Stub class.
 */
public class GestureDetector {
	public OnGestureListener listner;
	public Context context;
	
	public static class OnGestureListener {
		public boolean onDoubleTapEvent(MotionEvent event) {
			return true;
		}
	};
	public static class SimpleOnGestureListener extends OnGestureListener {
	};
	
	public GestureDetector() {
		
	}
	
	public GestureDetector(Context context, OnGestureListener listner) {
		this.context = context;
		this.listner = listner;
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}
}
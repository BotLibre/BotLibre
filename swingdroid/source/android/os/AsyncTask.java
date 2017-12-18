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

package android.os;

/**
 * Stub class.
 */
public class AsyncTask<X, Y, T> {
	
	protected String doInBackground(Void... params) {
		return "";
	}
	
	protected void onPostExecute(String xml) {
		
	}
	
	protected void onPreExecute() {
		
	}
	
	public AsyncTask execute() {
		Thread thread = new Thread() {
			public void run() {
				onPreExecute();
				doInBackground();
				onPostExecute("");
			}
		};
		thread.start();
		return this;
	}
	
	public String get() {
		return "";
	}
}
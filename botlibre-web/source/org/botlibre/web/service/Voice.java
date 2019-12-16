/******************************************************************************
 *
 *  Copyright 2013-2019 Paphus Solutions Inc.
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
package org.botlibre.web.service;

public abstract class Voice {
	public static int MAX_SIZE = 800;
	public static int MAX_FILE_NAME_SIZE = 200;
	
	static Voice instance;

	public static Voice instance() {
		if (instance == null) {
			instance = new MaryVoice();
		}
		return instance;
	}	

	public abstract boolean speak(String voice, String mod, String text, String file);
	
	public void setVoice(String text) {}
	
	public String getDefault() {
		return "default";
	}
	
}

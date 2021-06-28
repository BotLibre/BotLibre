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

import java.util.HashMap;
import java.util.Map;

import org.botlibre.web.admin.BotInstance;

public abstract class Voice {
	public static int MAX_SIZE = 800;
	public static int MAX_FILE_NAME_SIZE = 200;
	
	static Map<String, Voice> voices;

	public static Voice instance(String speechProvider) {
		if (voices == null) {
			voices = new HashMap<>();
			voices.put(BotInstance.MARY, new MaryVoice());
			voices.put(BotInstance.BINGSPEECH, new MicrosoftVoice());
		}
		if (speechProvider == null || speechProvider.isEmpty()) {
			return voices.get(BotInstance.MARY);
		}
		Voice voice = voices.get(speechProvider);
		if (voice == null) {
			return voices.get(BotInstance.MARY);
		}
		return voice;
	}	

	public abstract boolean speak(String voice, String mod, String text, String file, String apiKey, String apiToken, String apiEndpoint);
	
	public void setVoice(String text) {}
	
	public String getDefault() {
		return "default";
	}
	
}

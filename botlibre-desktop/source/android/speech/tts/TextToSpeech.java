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

package android.speech.tts;

import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;

/**
 * Stub class.
 */
public class TextToSpeech {
	public static int SUCCESS = 0;
	public static int LANG_MISSING_DATA = 1;
	public static int LANG_NOT_SUPPORTED = 2;
	public static int QUEUE_FLUSH = 3;
	
	public static interface OnInitListener {
		
	}
	public static interface OnUtteranceCompletedListener {
		void onUtteranceCompleted(String utteranceId);
	}
	public static interface Engine {
		public static String KEY_PARAM_UTTERANCE_ID = "0";
	}
	
	public TextToSpeech() {
		
	}
	
	public TextToSpeech(Activity view, Activity parent) {
		
	}
	
	public void speak(String text, int code, HashMap<String, String> params) {
		
	}
	
	public void stop() {
		
	}
	
	public void shutdown() {
		
	}
	
	public int setLanguage(Locale language) {
		return 0;
	}
	
	public void setPitch(float pitch) {
		
	}
	
	public void setSpeechRate(float rate) {
		
	}
	
	public void setOnUtteranceCompletedListener(OnUtteranceCompletedListener listener) {
		
	}

	public int isLanguageAvailable(Locale locale) {
		
		return 0;
	}
}
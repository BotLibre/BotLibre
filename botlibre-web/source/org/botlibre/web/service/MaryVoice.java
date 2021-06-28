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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.sound.sampled.AudioInputStream;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.util.data.audio.MaryAudioUtils;

import org.apache.log4j.Logger;

import org.botlibre.web.admin.AdminDatabase;

public class MaryVoice extends Voice {
	public static String DEFAULT = "cmu-slt";
	
	/** Mary voice. */
	protected Map<String, MaryInterface> voices;
	
	/**
	 * Lazy initialize the Mary voice.
	 */
	public MaryInterface getVoice(String name, String mod) {
		if (this.voices == null) {
			AdminDatabase.instance().log(Level.INFO, "Allocating voices");
			this.voices = new HashMap<String, MaryInterface>();
			try {
				LocalMaryInterface mary = new LocalMaryInterface();
				Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
				for (String voice : mary.getAvailableVoices()) {
					mary = new LocalMaryInterface();
					mary.setVoice(voice);
					this.voices.put(voice, mary);
					AdminDatabase.instance().log(Level.INFO, "Loading voice", voice);
				}
			} catch (Exception exception) {
				AdminDatabase.instance().log(exception);
			}
			AdminDatabase.instance().log(Level.INFO, "Voices allocated");
		}
		if ((name == null) || name.isEmpty()) {
			name = DEFAULT;
		}
		MaryInterface voice = this.voices.get(name);
		if (voice == null) {
			if (name.equals("camille")) {
				name = "enst-camille";
			} else if (name.equals("camille-hsmm-hsmm")) {
				name = "enst-camille-hsmm";
			} else if (name.equals("jessica_voice")) {
				name = "upmc-jessica";
			} else if (name.equals("jessica_voice-hsmm")) {
				name = "upmc-jessica-hsmm";
			} else if (name.equals("pierre-voice")) {
				name = "upmc-pierre";
			} else if (name.equals("pierre-voice-hsmm")) {
				name = "upmc-pierre-hsmm";
			} else if (name.equals("cmu-nk")) {
				name = "cmu-nk-hsmm";
			}
			voice = this.voices.get(name);
		}
		if (voice == null) {
			name = DEFAULT;
			voice = this.voices.get(name);
			if (voice == null) {
				voice = this.voices.values().iterator().next();
			}
		}
		if (mod != null && !mod.isEmpty() && !mod.equals("default")) {
			String nameMod = name + mod;
			MaryInterface voiceMod = this.voices.get(nameMod);
			if (voiceMod == null) {
				try {
					voiceMod = new LocalMaryInterface();
					voiceMod.setVoice(name);
					String effect = "";
					if (mod.equals("robot")) {
						effect = "Robot";
					} else if (mod.equals("echo")) {
						effect = "Chorus";
					} else if (mod.equals("whisper")) {
						effect = "Whisper";
					} else if (mod.equals("child")) {
						effect = "TractScaler(amount:1.2)";
					}
					voiceMod.setAudioEffects(effect);
					this.voices.put(nameMod, voiceMod);
					AdminDatabase.instance().log(Level.INFO, "Loading voice mod", nameMod);
				} catch (Exception exception) {
					AdminDatabase.instance().log(exception);
				} 
			}
			voice = voiceMod;
		}
		return voice;
	}
	
	@Override
	public String getDefault() {
		return DEFAULT;
	}

	@Override
	public synchronized boolean speak(String voice, String mod, String text, String file, String apiKey, String apiToken, String apiEndpoint) {
		if ((text == null) || text.isEmpty()) {
			return false;
		}
		if (text.length() > MAX_SIZE) {
			text = text.substring(0, MAX_SIZE);
		}
		try {
	        AudioInputStream audio = getVoice(voice, mod).generateAudio(text);
	        File path = new File(file);
	        new File(path.getParent()).mkdirs();
	        MaryAudioUtils.writeWavFile(MaryAudioUtils.getSamplesAsDoubleArray(audio), file, audio.getFormat());
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
			return false;
		}
		return true;
	}
	
}

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
package org.botlibre.web.bean;

import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.BotInstance;

public class VoiceBean extends ServletBean {
	
	String speakFileName;
	String speakText;
	String nativeVoiceApiToken;
	
	public VoiceBean() {
	}
	
	public boolean getSpeak() {
		return speakFileName != null;
	}

	@Override
	public void disconnectInstance() {
		disconnect();
	}
	
	@Override
	public void disconnect() {
		this.speakFileName = null;
		this.speakText = null;
	}

	public String getVoice() {
		if (getBotBean().getInstance() == null) {
			return "";
		}
		getBotBean().checkOldConfig();
		return getBotBean().getInstance().getVoice();
	}

	public String getVoiceMod() {
		if (getBotBean().getInstance() == null) {
			return "";
		}
		getBotBean().checkOldConfig();
		return getBotBean().getInstance().getVoiceMod();
	}

	public boolean getNativeVoice() {
		if (getBotBean().getInstance() == null) {
			return false;
		}
		getBotBean().checkOldConfig();
		return getBotBean().getInstance().getNativeVoice();
	}

	public boolean getResponsiveVoice() {
		if (getBotBean().getInstance() == null) {
			return false;
		}
		getBotBean().checkOldConfig();
		return BotInstance.RESPONSIVEVOICE.equals(getBotBean().getInstance().getNativeVoiceProvider());
	}

	public String getNativeVoiceName() {
		if (getBotBean().getInstance() == null) {
			return "";
		}
		getBotBean().checkOldConfig();
		return getBotBean().getInstance().getNativeVoiceName();
	}

	public String getLanguage() {
		if (getBotBean().getInstance() == null) {
			return "";
		}
		getBotBean().checkOldConfig();
		return getBotBean().getInstance().getLanguage();
	}

	public String getPitch() {
		if (getBotBean().getInstance() == null) {
			return "";
		}
		getBotBean().checkOldConfig();
		return getBotBean().getInstance().getPitch();
	}

	public String getSpeechRate() {
		if (getBotBean().getInstance() == null) {
			return "";
		}
		getBotBean().checkOldConfig();
		return getBotBean().getInstance().getSpeechRate();
	}
	
	public String getVoiceCheckedString(String voice) {
		if (voice.equals(getVoice())) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getNativeVoiceCheckedString(String voice) {
		if (voice.equals(getNativeVoiceName())) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getVoiceModCheckedString(String mod) {
		if (mod.equals(getVoiceMod())) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public void save(String voice, String mod, boolean nativeVoice, boolean responsiveVoice, String language, String nativeVoiceName, Boolean bingSpeech, String nativeVoiceApiKey, Boolean qqSpeech, String nativeVoiceAppId, String voiceApiEndpoint) {
		voice = Utils.sanitize(voice);
		mod = Utils.sanitize(mod);
		language = Utils.sanitize(language);
		nativeVoiceName = Utils.sanitize(nativeVoiceName);
		nativeVoiceApiKey = Utils.sanitize(nativeVoiceApiKey);
		nativeVoiceAppId = Utils.sanitize(nativeVoiceAppId);
		voiceApiEndpoint = Utils.sanitize(voiceApiEndpoint);
		
		BotInstance instance = getBotBean().getInstance();
		String nativeVoiceProvider = null;
		if (responsiveVoice && Site.COMMERCIAL) {
			nativeVoiceProvider = BotInstance.RESPONSIVEVOICE;
		}
		else if (bingSpeech) {
			nativeVoiceProvider = BotInstance.BINGSPEECH;
			nativeVoiceApiToken = null;
		}
		else if (qqSpeech) {
			nativeVoiceProvider = BotInstance.QQSPEECH;
		}
		getBotBean().setInstance(AdminDatabase.instance().updateInstanceVoice(
				instance.getId(), voice, mod, nativeVoice, nativeVoiceProvider, nativeVoiceName, language, instance.getPitch(), instance.getSpeechRate(), nativeVoiceApiKey, nativeVoiceAppId, voiceApiEndpoint));
	}
	
	public void save(String voice, String mod, boolean nativeVoice, String language, String pitch, String speechRate) {
		BotInstance instance = getBotBean().getInstance();
		getBotBean().setInstance(AdminDatabase.instance().updateInstanceVoice(
				instance.getId(), voice == null ? instance.getVoice() : voice, mod, nativeVoice, instance.getNativeVoiceProvider(), instance.getNativeVoiceName(), language, pitch, speechRate, null, null, null));
	}

	public String getSpeakFileName() {
		return speakFileName;
	}

	public void setSpeakFileName(String speakFileName) {
		this.speakFileName = speakFileName;
	}

	public String getSpeakText() {
		if (speakText == null) {
			speakText = "This is a test, testing 1 2 3 4 5 6 7 8 9 10";
		}
		return speakText;
	}

	public void setSpeakText(String speakText) {
		this.speakText = speakText;
	}
	
	public boolean getBingSpeech() {
		if (getBotBean().getInstance() == null) {
			return false;
		}
		getBotBean().checkOldConfig();
		return BotInstance.BINGSPEECH.equals(getBotBean().getInstance().getNativeVoiceProvider());
	}
	
	public String getNativeVoiceApiKey() {
		if (getBotBean().getInstance() == null) {
			return "";
		}
		getBotBean().checkOldConfig();
		return getBotBean().getInstance().getNativeVoiceApiKey();
	}
	
	public String getNativeVoiceAppId() {
		if (getBotBean().getInstance() == null) {
			return "";
		}
		getBotBean().checkOldConfig();
		return getBotBean().getInstance().getNativeVoiceAppId();
	}
	
	public boolean getQQSpeech() {
		if (getBotBean().getInstance() == null) {
			return false;
		}
		getBotBean().checkOldConfig();
		return BotInstance.QQSPEECH.equals(getBotBean().getInstance().getNativeVoiceProvider());
	}
	
	public void setNativeVoiceApiToken(String token) {
		this.nativeVoiceApiToken = token;
	}
	
	public String getNativeVoiceApiToken() {
		return this.nativeVoiceApiToken;
	}
	
	public String getVoiceApiEndpoint() {
		if (getBotBean().getInstance() == null) {
			return "";
		}
		getBotBean().checkOldConfig();
		return getBotBean().getInstance().getVoiceApiEndpoint();
	}
}

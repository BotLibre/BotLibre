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

package org.botlibre.sdk.config;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * DTO for XML chat config.
 */
public class ChatResponse extends Config {	
	public String conversation;
	public String message;
	public String question;
	public String emote;
	public String action;
	public String pose;
	public String command;
	public String avatar;
	public String avatar2;
	public String avatar3;
	public String avatar4;
	public String avatar5;
	public String avatarType;
	public String avatarTalk;
	public String avatarTalkType;
	public String avatarAction;
	public String avatarActionType;
	public String avatarActionAudio;
	public String avatarActionAudioType;
	public String avatarAudio;
	public String avatarAudioType;
	public String avatarBackground;
	public String speech;
	
	public boolean isVideo() {
		return this.avatarType != null && this.avatarType.indexOf("video") != -1;
	}
	
	public boolean isVideoTalk() {
		return this.avatarTalkType != null && this.avatarTalkType.indexOf("video") != -1;
	}

	public void parseXML(Element element) {
		this.conversation = element.getAttribute("conversation");
		this.emote = element.getAttribute("emote");
		this.action = element.getAttribute("action");
		this.pose = element.getAttribute("pose");
		this.avatar = element.getAttribute("avatar");
		this.avatar2 = element.getAttribute("avatar2");
		this.avatar3 = element.getAttribute("avatar3");
		this.avatar4 = element.getAttribute("avatar4");
		this.avatar5 = element.getAttribute("avatar5");
		this.avatarType = element.getAttribute("avatarType");
		this.avatarTalk = element.getAttribute("avatarTalk");
		this.avatarTalkType = element.getAttribute("avatarTalkType");
		this.avatarAction = element.getAttribute("avatarAction");
		this.avatarActionType = element.getAttribute("avatarActionType");
		this.avatarActionAudio = element.getAttribute("avatarActionAudio");
		this.avatarActionAudioType = element.getAttribute("avatarActionAudioType");
		this.avatarAudio = element.getAttribute("avatarAudio");
		this.avatarAudioType = element.getAttribute("avatarAudioType");
		this.avatarBackground = element.getAttribute("avatarBackground");
		this.speech = element.getAttribute("speech");
		this.command = element.getAttribute("command");

		Node node = element.getElementsByTagName("message").item(0);
		if (node != null) {
			this.message = node.getTextContent();
		}
		node = element.getElementsByTagName("question").item(0);
		if (node != null) {
			this.question = node.getTextContent();
		}
	}
	
	public JSONObject getCommand(){
		if (this.command == null || this.command.equals("")){
			return null;
		}
		try {
			return new JSONObject(this.command);
		} catch (JSONException exception) {
			exception.printStackTrace();
		}
		return null;
	}
}
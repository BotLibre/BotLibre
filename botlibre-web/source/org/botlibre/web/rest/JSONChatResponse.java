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
package org.botlibre.web.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO for XML chat response.
 */
@XmlRootElement(name="response")
@XmlAccessorType(XmlAccessType.FIELD)
public class JSONChatResponse {
	public String message;
	public String question;
	public String log;
	public long conversation;
	public String emote;
	public String action;
	public String pose;
	public String avatar;
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
	public String command;
	
	public JSONChatResponse() {
	}

	public JSONChatResponse(ChatResponse response) {
		message = response.message;
		question = response.question;
		log = response.log;
		conversation = response.conversation;
		emote = response.emote;
		action = response.action;
		pose = response.pose;
		avatar = response.avatar;
		avatarType = response.avatarType;
		avatarTalk = response.avatarTalk;
		avatarTalkType = response.avatarTalkType;
		avatarAction = response.avatarAction;
		avatarActionType = response.avatarActionType;
		avatarActionAudio = response.avatarActionAudio;
		avatarActionAudioType = response.avatarActionAudioType;
		avatarAudio = response.avatarAudio;
		avatarAudioType = response.avatarAudioType;
		avatarBackground = response.avatarBackground;
		speech = response.speech;
		command = response.command;
	}
}

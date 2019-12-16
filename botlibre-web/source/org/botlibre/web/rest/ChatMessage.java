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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO for XML chat message.
 */
@XmlRootElement(name="chat")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChatMessage extends Config {
	public String message;
	@XmlAttribute
	public String emote;
	@XmlAttribute
	public String action;
	@XmlAttribute
	public long conversation;
	@XmlAttribute
	public boolean correction;
	@XmlAttribute
	public boolean offensive;
	@XmlAttribute
	public boolean disconnect;
	@XmlAttribute
	public boolean includeQuestion;
	@XmlAttribute
	public boolean speak;
	@XmlAttribute
	public String avatar;
	@XmlAttribute
	public String avatarFormat;
	@XmlAttribute
	public boolean avatarHD;
	@XmlAttribute
	public Boolean learn;
	@XmlAttribute
	public boolean debug;
	@XmlAttribute
	public String debugLevel;
	@XmlAttribute
	public boolean secure;
	@XmlAttribute
	public boolean plainText;
	@XmlAttribute
	public String info;
	@XmlAttribute
	public String language;
	@XmlAttribute
	public String voice;
	@XmlAttribute
	public String mod;
	
	public ChatMessage() {
		
	}
	
	public ChatMessage(JSONChatMessage json) {
		super(json);
		message = json.message;
		emote = json.emote;
		action = json.action;
		conversation = json.conversation;
		correction = json.correction;
		offensive = json.offensive;
		disconnect = json.disconnect;
		includeQuestion = json.includeQuestion;
		speak = json.speak;
		avatar = json.avatar;
		avatarFormat = json.avatarFormat;
		avatarHD = json.avatarHD;
		learn = json.learn;
		debug = json.debug;
		debugLevel = json.debugLevel;
		secure = json.secure;
		plainText = json.plainText;
		info = json.info;
		language = json.language;
		voice = json.voice;
		mod = json.mod;
		
	}
	public String toString() {
		return this.message + " : " + this.instance + " : " + this.conversation;
	}
}

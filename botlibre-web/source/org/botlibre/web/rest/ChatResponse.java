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
 * DTO for XML chat response.
 */
@XmlRootElement(name="response")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChatResponse {
	public String message;
	public String question;
	public String log;
	@XmlAttribute
	public long conversation;
	@XmlAttribute
	public String emote;
	@XmlAttribute
	public String action;
	@XmlAttribute
	public String pose;
	@XmlAttribute
	public String avatar;
	@XmlAttribute
	public String avatar2;
	@XmlAttribute
	public String avatar3;
	@XmlAttribute
	public String avatar4;
	@XmlAttribute
	public String avatar5;
	@XmlAttribute
	public String avatarType;
	@XmlAttribute
	public String avatarTalk;
	@XmlAttribute
	public String avatarTalkType;
	@XmlAttribute
	public String avatarAction;
	@XmlAttribute
	public String avatarActionType;
	@XmlAttribute
	public String avatarActionAudio;
	@XmlAttribute
	public String avatarActionAudioType;
	@XmlAttribute
	public String avatarAudio;
	@XmlAttribute
	public String avatarAudioType;
	@XmlAttribute
	public String avatarBackground;
	@XmlAttribute
	public String speech;
	@XmlAttribute
	public String command;
}

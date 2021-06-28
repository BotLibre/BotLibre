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

import org.botlibre.util.Utils;

import org.botlibre.web.bean.LiveChatBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.chat.ChatChannel.ChannelType;

/**
 * DTO for XML channel config.
 */
@XmlRootElement(name="channel")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChannelConfig extends WebMediumConfig {
	@XmlAttribute
	public String type;
	@XmlAttribute
	public String messages;
	@XmlAttribute
	public String usersOnline;
	@XmlAttribute
	public String adminsOnline;
	@XmlAttribute
	public String videoAccessMode;
	@XmlAttribute
	public String audioAccessMode;
	@XmlAttribute
	public String inviteAccessMode;
	
	public String getChannelType() {
		if (this.type == null || this.type.isEmpty()) {
			return ChannelType.ChatRoom.name();
		}
		return this.type;
	}
	
	public LiveChatBean getBean(LoginBean loginBean) {
		return loginBean.getBean(LiveChatBean.class);
	}
	
	public void sanitize() {
		type = Utils.sanitize(type);
		messages = Utils.sanitize(messages);
		usersOnline = Utils.sanitize(usersOnline);
		adminsOnline = Utils.sanitize(adminsOnline);
		videoAccessMode = Utils.sanitize(videoAccessMode);
		audioAccessMode = Utils.sanitize(audioAccessMode);
		inviteAccessMode = Utils.sanitize(inviteAccessMode);
	}
}

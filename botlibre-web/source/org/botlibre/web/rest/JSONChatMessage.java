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
 * DTO for XML chat message.
 */
@XmlRootElement(name="chat")
@XmlAccessorType(XmlAccessType.FIELD)
public class JSONChatMessage extends JSONConfig {
	public String message;
	public String emote;
	public String action;
	public long conversation;
	public boolean correction;
	public boolean offensive;
	public boolean disconnect;
	public boolean includeQuestion;
	public boolean speak;
	public String avatar;
	public String avatarFormat;
	public boolean avatarHD;
	public Boolean learn;
	public boolean debug;
	public String debugLevel;
	public boolean secure;
	public boolean plainText;
	public String info;
	public String language;
	public String voice;
	public String mod;
	
	public String toString() {
		return this.message + " : " + this.instance + " : " + this.conversation;
	}
}

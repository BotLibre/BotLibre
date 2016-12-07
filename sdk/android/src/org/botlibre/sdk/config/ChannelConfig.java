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

import java.io.StringWriter;

import org.w3c.dom.Element;

/**
 * DTO for XML channel config.
 */
public class ChannelConfig extends WebMediumConfig {
	public String type;
	public String videoAccessMode;
	public String audioAccessMode;
	public String messages;
	public String usersOnline;
	public String adminsOnline;

	@Override
	public String getType() {
		return "channel";
	}

	@Override
	public String stats() {
		return this.usersOnline + " users online, " + this.adminsOnline + " admins";
	}
	
	public WebMediumConfig credentials() {
		ChannelConfig config = new ChannelConfig();
		config.id = this.id;
		return config;
	}
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<channel");
		if (this.type != null && !this.type.equals("")) {
			writer.write(" type=\"" + this.type + "\"");
		}
		if (this.videoAccessMode != null && !this.videoAccessMode.equals("")) {
			writer.write(" videoAccessMode=\"" + this.videoAccessMode + "\"");
		}
		if (this.audioAccessMode != null && !this.audioAccessMode.equals("")) {
			writer.write(" audioAccessMode=\"" + this.audioAccessMode + "\"");
		}
		writeXML(writer);
		writer.write("</channel>");
		return writer.toString();
	}
	
	public void parseXML(Element element) {
		super.parseXML(element);
		this.type = element.getAttribute("type");
		this.videoAccessMode = element.getAttribute("videoAccessMode");
		this.audioAccessMode = element.getAttribute("audioAccessMode");
		this.messages = element.getAttribute("messages");
		this.usersOnline = element.getAttribute("usersOnline");
		this.adminsOnline = element.getAttribute("adminsOnline");
	}
}
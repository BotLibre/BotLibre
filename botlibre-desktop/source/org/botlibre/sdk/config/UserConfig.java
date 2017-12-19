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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.botlibre.sdk.SDKConnection;
import org.botlibre.sdk.util.Utils;

/**
 * DTO for XML user creation config.
 */
public class UserConfig extends Config {
	public String password;
	public String newPassword;
	public String hint;
	public String name;
	public boolean showName;
	public String email;
	public String website;
	public String bio;
	public boolean over18;
	public String avatar;
	
	public String connects;
	public String bots;
	public String posts;
	public String messages;
	public String forums;
	public String scripts;
	public String graphics;
	public String avatars;
	public String domains;
	public String channels;
	
	public String joined;
	public String lastConnect;
	public String type;
	public boolean isFlagged;
	public String flaggedReason;
	
	public String displayJoined() {
		try {
			SimpleDateFormat formater = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			Date date = formater.parse(joined);
			return Utils.displayDate(date);
		} catch (Exception exception) {
			return joined;
		}
	}
	
	public String displayLastConnect() {
		try {
			SimpleDateFormat formater = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			Date date = formater.parse(lastConnect);
			return Utils.displayTimestamp(date);
		} catch (Exception exception) {
			return lastConnect;
		}
	}
	
	public void addCredentials(SDKConnection connection) {
		this.application = connection.getCredentials().getApplicationId();
		if (connection.getDomain() != null) {
			this.domain = connection.getDomain().id;
		}
	}
	
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof UserConfig)) {
			return false;
		}
		return this.user != null && this.user.equals(((UserConfig)object).user);
	}

	public void parseXML(Element element) {
		this.user = element.getAttribute("user");
		this.name = element.getAttribute("name");
		this.showName = Boolean.valueOf(element.getAttribute("showName"));
		this.token = element.getAttribute("token");
		this.email = element.getAttribute("email");
		this.hint = element.getAttribute("hint");
		this.website = element.getAttribute("website");
		this.connects = element.getAttribute("connects");
		this.bots = element.getAttribute("bots");
		this.posts = element.getAttribute("posts");
		this.messages = element.getAttribute("messages");
		this.forums = element.getAttribute("forums");
		this.channels = element.getAttribute("channels");
		this.avatars = element.getAttribute("avatars");
		this.scripts = element.getAttribute("scripts");
		this.graphics = element.getAttribute("graphics");
		this.domains = element.getAttribute("domains");
		this.joined = element.getAttribute("joined");
		this.lastConnect = element.getAttribute("lastConnect");
		this.type = element.getAttribute("type");
		this.isFlagged = Boolean.valueOf(element.getAttribute("isFlagged"));
		
		Node node = element.getElementsByTagName("bio").item(0);
		if (node != null) {
			this.bio = node.getTextContent();
		}
		node = element.getElementsByTagName("avatar").item(0);
		if (node != null) {
			this.avatar = node.getTextContent();
		}
		node = element.getElementsByTagName("flaggedReason").item(0);
		if (node != null) {
			this.flaggedReason = node.getTextContent();
		}
	}
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<user");
		writeCredentials(writer);
		if (this.password != null) {
			writer.write(" password=\"" + this.password + "\"");
		}
		if (this.newPassword != null) {
			writer.write(" newPassword=\"" + this.newPassword + "\"");
		}
		if (this.hint != null) {
			writer.write(" hint=\"" + this.hint + "\"");
		}
		if (this.name != null) {
			writer.write(" name=\"" + this.name + "\"");
		}
		if (this.showName) {
			writer.write(" showName=\"" + this.showName + "\"");
		}
		if (this.email != null) {
			writer.write(" email=\"" + this.email + "\"");
		}
		if (this.website != null) {
			writer.write(" website=\"" + this.website + "\"");
		}
		if (this.over18) {
			writer.write(" over18=\"" + this.over18 + "\"");
		}
		writer.write(">");
		
		if (this.bio != null) {
			writer.write("<bio>");
			writer.write(Utils.escapeHTML(this.bio));
			writer.write("</bio>");
		}
		writer.write("</user>");
		return writer.toString();
	}
		
}
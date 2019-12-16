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

import org.botlibre.sdk.util.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * DTO for XML web medium config.
 */

public abstract class WebMediumConfig extends Config {
	public String id;
	public String name;
	public boolean isAdmin;
	public boolean isAdult;
	public boolean isPrivate;
	public boolean isHidden;
	public String accessMode;
	public boolean isFlagged;
	public boolean isExternal;
	public boolean isPaphus;
	public boolean showAds = true;
	public String forkAccessMode;
	public String contentRating;
	public String description;
	public String details;
	public String disclaimer;
	public String website;
	public String subdomain;
	public String tags;
	public String categories;
	public String flaggedReason;
	public String creator;
	public String creationDate;
	public String lastConnectedUser;
	public String license;
	public String avatar;
	public String script;
	public String graphic;
	public int thumbsUp = 0;
	public int thumbsDown = 0;
	public String stars = "0";
	public String connects;
	public String dailyConnects;
	public String weeklyConnects;
	public String monthlyConnects;
	
	public abstract String toXML();
	
	public abstract String getType();
	
	public abstract WebMediumConfig credentials();
	
	public String stats() {
		return "";
	}
	
	public boolean equals(Object object) {
		if (object instanceof WebMediumConfig) {
			if (this.id == null) {
				return super.equals(object);
			}
			return this.id.equals(((WebMediumConfig)object).id);
		}
		return false;
	}
	
	public String displayCreationDate() {
		try {
			SimpleDateFormat formater = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			Date date = formater.parse(creationDate);
			return Utils.displayDate(date);
		} catch (Exception exception) {
			return creationDate;
		}
	}
	
	public long getToken() {
		long token = 0;
		if ((this.token != null) && (this.token.equals(""))) {
			token = Long.valueOf(this.token);
		}
		return token;
	}

	public void writeXML(StringWriter writer) {
		writeCredentials(writer);
		if (this.id != null) {
			writer.write(" id=\"" + this.id + "\"");
		}
		if (this.name != null) {
			writer.write(" name=\"" + this.name + "\"");
		}
		if (this.isPrivate) {
			writer.write(" isPrivate=\"true\"");
		}
		if (this.isHidden) {
			writer.write(" isHidden=\"true\"");
		}
		if (this.accessMode != null && !this.accessMode.equals("")) {
			writer.write(" accessMode=\"" + this.accessMode + "\"");
		}
		if (this.contentRating != null && !this.contentRating.equals("")) {
			writer.write(" contentRating=\"" + this.contentRating + "\"");
		}
		if (this.forkAccessMode != null && !this.forkAccessMode.equals("")) {
			writer.write(" forkAccessMode=\"" + this.forkAccessMode + "\"");
		}

		if (this.stars != null && !this.stars.equals("")) {
			writer.write(" stars=\"" + this.stars + "\"");
		}
		if (this.isAdult) {
			writer.write(" isAdult=\"true\"");
		}
		if (this.isFlagged) {
			writer.write(" isFlagged=\"true\"");
		}
		if (this.isExternal) {
			writer.write(" isExternal=\"true\"");
		}
		if (this.showAds) {
			writer.write(" showAds=\"true\"");
		}
		writer.write(">");
		if (this.description != null) {
			writer.write("<description>");
			writer.write(Utils.escapeHTML(this.description));
			writer.write("</description>");
		}
		if (this.details != null) {
			writer.write("<details>");
			writer.write(Utils.escapeHTML(this.details));
			writer.write("</details>");
		}
		if (this.disclaimer != null) {
			writer.write("<disclaimer>");
			writer.write(Utils.escapeHTML(this.disclaimer));
			writer.write("</disclaimer>");
		}
		if (this.categories != null) {
			writer.write("<categories>");
			writer.write(this.categories);
			writer.write("</categories>");
		}
		if (this.tags != null) {
			writer.write("<tags>");
			writer.write(this.tags);
			writer.write("</tags>");
		}
		if (this.license != null) {
			writer.write("<license>");
			writer.write(this.license);
			writer.write("</license>");
		}
		if (this.website != null) {
			writer.write("<website>");
			writer.write(this.website);
			writer.write("</website>");
		}
		if (this.subdomain != null) {
			writer.write("<subdomain>");
			writer.write(this.subdomain);
			writer.write("</subdomain>");
		}
		if (this.flaggedReason != null) {
			writer.write("<flaggedReason>");
			writer.write(Utils.escapeHTML(this.flaggedReason));
			writer.write("</flaggedReason>");
		}
	}
	
	public void parseXML(Element element) {
		this.id = element.getAttribute("id");
		this.name = element.getAttribute("name");
		this.creationDate = element.getAttribute("creationDate");
		this.isPrivate = Boolean.valueOf(element.getAttribute("isPrivate"));
		this.isHidden = Boolean.valueOf(element.getAttribute("isHidden"));
		this.accessMode = element.getAttribute("accessMode");
		this.contentRating = element.getAttribute("contentRating");
		this.forkAccessMode = element.getAttribute("forkAccessMode");
		this.isAdmin = Boolean.valueOf(element.getAttribute("isAdmin"));
		this.isAdult = Boolean.valueOf(element.getAttribute("isAdult"));
		this.isFlagged = Boolean.valueOf(element.getAttribute("isFlagged"));
		this.isExternal = Boolean.valueOf(element.getAttribute("isExternal"));
		this.creator = element.getAttribute("creator");
		this.creationDate = element.getAttribute("creationDate");
		this.connects = element.getAttribute("connects");
		this.dailyConnects = element.getAttribute("dailyConnects");
		this.weeklyConnects = element.getAttribute("weeklyConnects");
		this.showAds = Boolean.valueOf(element.getAttribute("showAds"));
		this.monthlyConnects = element.getAttribute("monthlyConnects");
		if (element.getAttribute("thumbsUp") != null && element.getAttribute("thumbsUp").trim().length() > 0) {
			this.thumbsUp = Integer.valueOf(element.getAttribute("thumbsUp"));
		}
		if (element.getAttribute("thumbsDown") != null && element.getAttribute("thumbsDown").trim().length() > 0) {
			this.thumbsDown = Integer.valueOf(element.getAttribute("thumbsDown"));
		}
		if (element.getAttribute("stars") != null && element.getAttribute("stars").trim().length() > 0) {
			this.stars = element.getAttribute("stars");
		}
		
		Node node = element.getElementsByTagName("description").item(0);
		if (node != null) {
			this.description = node.getTextContent();
		}
		node = element.getElementsByTagName("details").item(0);
		if (node != null) {
			this.details = node.getTextContent();
		}
		node = element.getElementsByTagName("disclaimer").item(0);
		if (node != null) {
			this.disclaimer = node.getTextContent();
		}
		node = element.getElementsByTagName("categories").item(0);
		if (node != null) {
			this.categories = node.getTextContent();
		}
		node = element.getElementsByTagName("tags").item(0);
		if (node != null) {
			this.tags = node.getTextContent();
		}
		node = element.getElementsByTagName("flaggedReason").item(0);
		if (node != null) {
			this.flaggedReason = node.getTextContent();
		}
		node = element.getElementsByTagName("lastConnectedUser").item(0);
		if (node != null) {
			this.lastConnectedUser = node.getTextContent();
		}
		node = element.getElementsByTagName("license").item(0);
		if (node != null) {
			this.license = node.getTextContent();
		}
		node = element.getElementsByTagName("website").item(0);
		if (node != null) {
			this.website = node.getTextContent();
		}
		node = element.getElementsByTagName("subdomain").item(0);
		if (node != null) {
			this.subdomain = node.getTextContent();
		}
		node = element.getElementsByTagName("avatar").item(0);
		if (node != null) {
			this.avatar = node.getTextContent();
		}
	}
}
package org.botlibre.sdk.config;

import java.io.StringWriter;

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
	public String description;
	public String details;
	public String disclaimer;
	public String website;
	public String tags;
	public String categories;
	public String flaggedReason;
	public String creator;
	public String creationDate;
	public String lastConnectedUser;
	public String license;
	public String avatar;
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
			writer.write(this.description);
			writer.write("</description>");
		}
		if (this.details != null) {
			writer.write("<details>");
			writer.write(this.details);
			writer.write("</details>");
		}
		if (this.disclaimer != null) {
			writer.write("<disclaimer>");
			writer.write(this.disclaimer);
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
		if (this.flaggedReason != null) {
			writer.write("<flaggedReason>");
			writer.write(this.flaggedReason);
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
		node = element.getElementsByTagName("avatar").item(0);
		if (node != null) {
			this.avatar = node.getTextContent();
		}
	}
}
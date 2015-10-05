package org.botlibre.sdk.config;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * DTO for XML forum post config.
 */
public class ForumPostConfig extends Config {	
	public String id;
	public String topic;
	public String summary;
	public String details;
	public String detailsText;
	public String forum;
	public String tags;
	public boolean isAdmin;
	public boolean isFlagged;
	public String flaggedReason;
	public boolean isFeatured;
	public String creator;
	public String creationDate;
	public String views;
	public String dailyViews;
	public String weeklyViews;
	public String monthlyViews;
	public String replyCount;
	public String parent;
	public String avatar;
	public List<ForumPostConfig> replies;
	
	public boolean equals(Object object) {
		if (object instanceof ForumPostConfig) {
			if (this.id == null) {
				return super.equals(object);
			}
			return this.id.equals(((ForumPostConfig)object).id);
		}
		return false;
	}
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writeXML(writer);
		return writer.toString();
	}

	public void writeXML(StringWriter writer) {
		writer.write("<forum-post");
		writeCredentials(writer);
		if (this.id != null) {
			writer.write(" id=\"" + this.id + "\"");
		}
		if (this.parent != null) {
			writer.write(" parent=\"" + this.parent + "\"");
		}
		if (this.forum != null) {
			writer.write(" forum=\"" + this.forum + "\"");
		}
		if (this.isFeatured) {
			writer.write(" isFeatured=\"true\"");
		}
		writer.write(">");
		if (this.topic != null) {
			writer.write("<topic>");
			writer.write(this.topic);
			writer.write("</topic>");
		}
		if (this.details != null) {
			String text = this.details;
			text = text.replace("<", "&lt;");
			text = text.replace(">", "&gt;");
			writer.write("<details>");
			writer.write(text);
			writer.write("</details>");
		}
		if (this.tags != null) {
			writer.write("<tags>");
			writer.write(this.tags);
			writer.write("</tags>");
		}
		writer.write("</forum-post>");
	}
	
	public void parseXML(Element element) {
		this.id = element.getAttribute("id");
		this.parent = element.getAttribute("parent");
		this.forum = element.getAttribute("forum");
		this.views = element.getAttribute("views");
		this.dailyViews = element.getAttribute("dailyViews");
		this.weeklyViews = element.getAttribute("weeklyViews");
		this.monthlyViews = element.getAttribute("monthlyViews");
		this.isAdmin = Boolean.valueOf(element.getAttribute("isAdmin"));
		this.replyCount = element.getAttribute("replyCount");
		this.isFlagged = Boolean.valueOf(element.getAttribute("isFlagged"));
		this.isFeatured = Boolean.valueOf(element.getAttribute("isFeatured"));
		this.creator = element.getAttribute("creator");
		this.creationDate = element.getAttribute("creationDate");
		
		Node node = element.getElementsByTagName("summary").item(0);
		if (node != null) {
			this.summary = node.getTextContent();
		}
		node = element.getElementsByTagName("details").item(0);
		if (node != null) {
			this.details = node.getTextContent();
		}
		node = element.getElementsByTagName("detailsText").item(0);
		if (node != null) {
			this.detailsText = node.getTextContent();
		}
		node = element.getElementsByTagName("topic").item(0);
		if (node != null) {
			this.topic = node.getTextContent();
		}
		node = element.getElementsByTagName("tags").item(0);
		if (node != null) {
			this.tags = node.getTextContent();
		}
		node = element.getElementsByTagName("flaggedReason").item(0);
		if (node != null) {
			this.flaggedReason = node.getTextContent();
		}
		node = element.getElementsByTagName("avatar").item(0);
		if (node != null) {
			this.avatar = node.getTextContent();
		}
		NodeList nodes = element.getElementsByTagName("replies");
		if (nodes != null && nodes.getLength() > 0) {
			this.replies = new ArrayList<ForumPostConfig>();
			for (int index = 0; index < nodes.getLength(); index++) {
				Element reply = (Element)nodes.item(index);
				ForumPostConfig config = new ForumPostConfig();
				config.parseXML(reply);
				this.replies.add(config);
			}
		}
	}
}
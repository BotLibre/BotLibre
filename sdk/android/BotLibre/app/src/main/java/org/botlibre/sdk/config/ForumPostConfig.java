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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.botlibre.sdk.util.Utils;
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
	public int thumbsUp = 0;
	public int thumbsDown = 0;
	public String stars = "0";
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
		if (this.stars != null && !this.stars.equals("")) {
			writer.write(" stars=\"" + this.stars + "\"");
		}
		writer.write(">");
		if (this.topic != null) {
			writer.write("<topic>");
			writer.write(Utils.escapeHTML(this.topic));
			writer.write("</topic>");
		}
		if (this.details != null) {
			String text = this.details;
			writer.write("<details>");
			writer.write(Utils.escapeHTML(text));
			writer.write("</details>");
		}
		if (this.tags != null) {
			writer.write("<tags>");
			writer.write(this.tags);
			writer.write("</tags>");
		}
		if (this.flaggedReason != null) {
			writer.write("<flaggedReason>");
			writer.write(Utils.escapeHTML(this.flaggedReason));
			writer.write("</flaggedReason>");
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
		if (element.getAttribute("thumbsUp") != null && element.getAttribute("thumbsUp").trim().length() > 0) {
			this.thumbsUp = Integer.valueOf(element.getAttribute("thumbsUp"));
		}
		if (element.getAttribute("thumbsDown") != null && element.getAttribute("thumbsDown").trim().length() > 0) {
			this.thumbsDown = Integer.valueOf(element.getAttribute("thumbsDown"));
		}
		if (element.getAttribute("stars") != null && element.getAttribute("stars").trim().length() > 0) {
			this.stars = element.getAttribute("stars");
		}
		
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
	public ForumPostConfig credentials() {
		ForumPostConfig config = new ForumPostConfig();
		config.id = this.id;
		return config;
	}

	public String displayCreationDate() {
		try {
			SimpleDateFormat formater = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			Date date = formater.parse(creationDate);
			return Utils.displayTimestamp(date);
		} catch (Exception exception) {
			return creationDate;
		}
	}
}
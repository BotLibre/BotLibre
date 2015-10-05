package org.botlibre.sdk.config;

import java.io.StringWriter;

import org.w3c.dom.Element;

/**
 * DTO for XML forum config.
 */
public class ForumConfig extends WebMediumConfig {
	public String replyAccessMode;
	public String postAccessMode;
	public String posts;
	
	public String getType() {
		return "forum";
	}

	@Override
	public String stats() {
		return this.posts + " posts";
	}
	
	public WebMediumConfig credentials() {
		ForumConfig config = new ForumConfig();
		config.id = this.id;
		return config;
	}
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<forum");
		if (this.replyAccessMode != null && !this.replyAccessMode.equals("")) {
			writer.write(" replyAccessMode=\"" + this.replyAccessMode + "\"");
		}
		if (this.postAccessMode != null && !this.postAccessMode.equals("")) {
			writer.write(" postAccessMode=\"" + this.postAccessMode + "\"");
		}
		writeXML(writer);
		writer.write("</forum>");
		return writer.toString();
	}
	
	public void parseXML(Element element) {
		super.parseXML(element);
		this.replyAccessMode = element.getAttribute("replyAccessMode");
		this.postAccessMode = element.getAttribute("postAccessMode");
		this.posts = element.getAttribute("posts");
	}
}
package org.botlibre.sdk.config;

import java.io.StringWriter;

import org.w3c.dom.Element;

/**
 * DTO for XML channel config.
 */
public class ChannelConfig extends WebMediumConfig {
	public String type;
	public String messages;
	public String usersOnline;
	public String adminsOnline;
	
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
		writeXML(writer);
		writer.write("</channel>");
		return writer.toString();
	}
	
	public void parseXML(Element element) {
		super.parseXML(element);
		this.type = element.getAttribute("type");
		this.messages = element.getAttribute("messages");
		this.usersOnline = element.getAttribute("usersOnline");
		this.adminsOnline = element.getAttribute("adminsOnline");
	}
}
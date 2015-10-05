package org.botlibre.sdk.config;

import java.io.StringWriter;

import org.w3c.dom.Element;


/**
 * DTO for XML voice config.
 */
public class VoiceConfig extends Config {
	
	public String language;	
	public String pitch;
	public String speechRate;
	
	public void parseXML(Element element) {
		super.parseXML(element);
		
		this.language = element.getAttribute("language");
		this.pitch = element.getAttribute("pitch");
		this.speechRate = element.getAttribute("speechRate");
	}

	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<voice");
		writeCredentials(writer);

		if (this.language != null) {
			writer.write(" language=\"" + this.language + "\"");
		}
		if (this.pitch != null) {
			writer.write(" pitch=\"" + this.pitch + "\"");
		}
		if (this.speechRate != null) {
			writer.write(" speechRate=\"" + this.speechRate + "\"");
		}
		
		writer.write("/>");
		return writer.toString();
	}
}
package org.botlibre.sdk.config;

import java.io.StringWriter;

import org.w3c.dom.Element;


/**
 * DTO for XML bot mode config.
 */
public class BotModeConfig extends Config {
	
	public String mode;	
	public String bot;
	
	public void parseXML(Element element) {
		super.parseXML(element);
		
		this.mode = element.getAttribute("mode");
		this.bot = element.getAttribute("bot");
	}

	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<bot-mode");
		writeCredentials(writer);

		if (this.mode != null) {
			writer.write(" mode=\"" + this.mode + "\"");
		}
		if (this.bot != null) {
			writer.write(" bot=\"" + this.bot + "\"");
		}
		
		writer.write("/>");
		return writer.toString();
	}
}
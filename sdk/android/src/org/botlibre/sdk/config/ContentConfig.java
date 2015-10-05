package org.botlibre.sdk.config;

import java.io.StringWriter;

import org.w3c.dom.Element;


/**
 * DTO for XML content config.
 */
public class ContentConfig extends Config {
	
	public String type;	
	
	public void parseXML(Element element) {
		super.parseXML(element);
		
		this.type = element.getAttribute("type");
	}

	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<content");
		writeCredentials(writer);

		if (this.type != null) {
			writer.write(" type=\"" + this.type + "\"");
		}
		
		writer.write("/>");
		return writer.toString();
	}
}
package org.botlibre.sdk.config;

import java.io.StringWriter;

import org.w3c.dom.Element;

/**
 * DTO for XML domain config.
 */
public class DomainConfig extends WebMediumConfig {
	public String creationMode;
	
	public String getType() {
		return "domain";
	}
	
	public WebMediumConfig credentials() {
		DomainConfig config = new DomainConfig();
		config.id = this.id;
		return config;
	}
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<domain");
		if (this.creationMode != null && !this.creationMode.equals("")) {
			writer.write(" creationMode=\"" + this.creationMode + "\"");
		}
		writeXML(writer);
		writer.write("</domain>");
		return writer.toString();
	}
	
	public void parseXML(Element element) {
		super.parseXML(element);
		this.creationMode = element.getAttribute("creationMode");
	}
}
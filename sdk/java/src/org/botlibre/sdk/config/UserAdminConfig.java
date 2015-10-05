package org.botlibre.sdk.config;

import java.io.StringWriter;

import org.w3c.dom.Element;


/**
 * DTO for XML user admin config.
 */
public class UserAdminConfig extends Config {	
	public String operation;
	public String operationUser;
	
	public void parseXML(Element element) {
		super.parseXML(element);
		
		this.operation = element.getAttribute("operation");
		this.operationUser = element.getAttribute("operationUser");
	}

	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<user-admin");
		writeCredentials(writer);
		
		if (this.operation != null) {
			writer.write(" operation=\"" + this.operation + "\"");
		}
		if (this.operationUser != null) {
			writer.write(" operationUser=\"" + this.operationUser + "\"");
		}
		
		writer.write("/>");
		return writer.toString();
	}
}
package org.botlibre.sdk.config;

import java.io.StringWriter;


/**
 * DTO for XML training config.
 */
public class TrainingConfig extends Config {	
	public String operation;	
	public String question;	
	public String response;
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<training");
		writeCredentials(writer);
		
		if (this.operation != null) {
			writer.write(" operation=\"" + this.operation + "\"");
		}
		
		writer.write(">");

		if (this.question != null) {
			writer.write("<question>");
			writer.write(this.question);
			writer.write("</question>");
		}
		if (this.response != null) {
			writer.write("<response>");
			writer.write(this.response);
			writer.write("</response>");
		}
		
		writer.write("</training>");
		return writer.toString();
	}
}
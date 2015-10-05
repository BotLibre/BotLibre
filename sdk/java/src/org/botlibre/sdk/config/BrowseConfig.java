package org.botlibre.sdk.config;

import java.io.StringWriter;

/**
 * DTO for XML browse options.
 */
public class BrowseConfig extends Config {
	public String type;
	public String typeFilter;
	public String category;
	public String tag;
	public String filter;
	public String sort;
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<browse");
		writeCredentials(writer);
		writer.write(" type=\"" + this.type + "\"");
		if (this.typeFilter != null) {
			writer.write(" typeFilter=\"" + this.typeFilter + "\"");
		}
		if (this.sort != null) {
			writer.write(" sort=\"" + this.sort + "\"");
		}
		if ((this.category != null) && !this.category.equals("")) {
			writer.write(" category=\"" + this.category + "\"");
		}
		if ((this.tag != null) && !this.tag.equals("")) {
			writer.write(" tag=\"" + this.tag + "\"");
		}
		if ((this.filter != null) && !this.filter.equals("")) {
			writer.write(" filter=\"" + this.filter + "\"");
		}
		writer.write("/>");
		return writer.toString();
	}
}
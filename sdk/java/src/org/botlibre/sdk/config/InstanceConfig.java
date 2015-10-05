package org.botlibre.sdk.config;

import java.io.StringWriter;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * DTO for XML bot instance config.
 */
public class InstanceConfig extends WebMediumConfig {
	public String size;
	public boolean allowForking;
	public String template;
	
	public String getType() {
		return "instance";
	}

	@Override
	public String stats() {
		return this.connects + " connects, " + this.dailyConnects + " today, " + this.weeklyConnects + " week, " + this.monthlyConnects + " month";
	}
	
	public InstanceConfig credentials() {
		InstanceConfig config = new InstanceConfig();
		config.id = this.id;
		return config;
	}
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<instance");
		if (this.allowForking) {
			writer.write(" allowForking=\"true\"");
		}
		writeXML(writer);
		if (this.template != null) {
			writer.write("<template>");
			writer.write(this.template);
			writer.write("</template>");
		}
		writer.write("</instance>");
		return writer.toString();
	}
	
	public void parseXML(Element element) {
		super.parseXML(element);
		this.allowForking = Boolean.valueOf(element.getAttribute("allowForking"));
		this.size = element.getAttribute("size");
		
		Node node = element.getElementsByTagName("template").item(0);
		if (node != null) {
			this.template = node.getTextContent();
		}
	}
}
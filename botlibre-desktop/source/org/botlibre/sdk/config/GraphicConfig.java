package org.botlibre.sdk.config;

import java.io.StringWriter;

import org.w3c.dom.Element;

/**
 * DTO for XML graphic config.
 */
public class GraphicConfig extends WebMediumConfig{
	public String media;
	public String fileName;
	public String fileType;
	
	
	@Override
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<graphic");
		if (this.media!= null && !this.media.equals("")) {
			writer.write(" media=\"" + this.media + "\"");
		}
		if (this.fileName!= null && !this.fileName.equals("")) {
			writer.write(" fileName=\"" + this.fileName + "\"");
		}
		if (this.fileType != null && !this.fileName.equals("")) {
			writer.write(" fileType=\"" + this.fileType + "\"");
		}
		writeXML(writer);
		writer.write("</graphic>");
		return writer.toString();
	}

	@Override
	public String getType() {
		return "graphic";
	}

	@Override
	public WebMediumConfig credentials() {
		GraphicConfig config = new GraphicConfig();
		config.id = this.id;
		return config;
	}
	public void parseXML(Element element) {
		super.parseXML(element);
		this.media = element.getAttribute("media");
		this.fileName = element.getAttribute("fileName");
		this.fileType = element.getAttribute("fileType");

//		Node node = element.getElementsByTagName("media").item(0);
//		if (node != null) {
//			this.media = node.getTextContent();
//		}

	}
	public boolean isVideo() {
		return this.fileType != null && this.fileType.indexOf("video") != -1;
	
	}
	
	public boolean isAudio() {
		return this.fileType != null && this.fileType.indexOf("audio") != -1;
	}
}

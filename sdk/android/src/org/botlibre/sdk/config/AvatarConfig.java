package org.botlibre.sdk.config;

import java.io.StringWriter;

import org.w3c.dom.Element;


/**
 * DTO for XML avatar config.
 */
public class AvatarConfig extends Config {
	public long id;
	public String avatar;

	public boolean isDefault;
	
	public boolean ecstatic;
	public boolean happy;
	public boolean sad;
	public boolean crying;
	
	public boolean love;
	public boolean like;
	public boolean dislike;
	public boolean hate;
	
	public boolean courageous;
	public boolean confident;
	public boolean afraid;
	public boolean panic;
	
	public boolean serene;
	public boolean calm;
	public boolean anger;
	public boolean rage;
	
	public boolean surprise;
	public boolean bored;
	
	public boolean laughter;
	public boolean serious;
	
	public boolean none;
	
	public void parseXML(Element element) {
		super.parseXML(element);
		
		this.id = Long.valueOf(element.getAttribute("id"));
		this.avatar = element.getAttribute("avatar");
		this.isDefault = Boolean.valueOf(element.getAttribute("isDefault"));
		
		this.ecstatic = Boolean.valueOf(element.getAttribute("ecstatic"));
		this.happy = Boolean.valueOf(element.getAttribute("happy"));
		this.sad = Boolean.valueOf(element.getAttribute("sad"));
		this.crying = Boolean.valueOf(element.getAttribute("crying"));
		
		this.love = Boolean.valueOf(element.getAttribute("love"));
		this.like = Boolean.valueOf(element.getAttribute("like"));
		this.dislike = Boolean.valueOf(element.getAttribute("dislike"));
		this.hate = Boolean.valueOf(element.getAttribute("hate"));
		
		this.courageous = Boolean.valueOf(element.getAttribute("courageous"));
		this.confident = Boolean.valueOf(element.getAttribute("confident"));
		this.afraid = Boolean.valueOf(element.getAttribute("afraid"));
		this.panic = Boolean.valueOf(element.getAttribute("panic"));
		
		this.serene = Boolean.valueOf(element.getAttribute("serene"));
		this.calm = Boolean.valueOf(element.getAttribute("calm"));
		this.anger = Boolean.valueOf(element.getAttribute("anger"));
		this.rage = Boolean.valueOf(element.getAttribute("rage"));
		
		this.surprise = Boolean.valueOf(element.getAttribute("surprise"));
		this.bored = Boolean.valueOf(element.getAttribute("bored"));
		
		this.laughter = Boolean.valueOf(element.getAttribute("laughter"));
		this.serious = Boolean.valueOf(element.getAttribute("serious"));
		
		this.none = Boolean.valueOf(element.getAttribute("none"));
	}

	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<avatar");
		writeCredentials(writer);

		if (this.id != 0) {
			writer.write(" id=\"" + this.id + "\"");
		}
		if (this.isDefault) {
			writer.write(" isDefault=\"true\"");
		}
		
		if (this.ecstatic) {
			writer.write(" ecstatic=\"true\"");
		}
		if (this.happy) {
			writer.write(" happy=\"true\"");
		}
		if (this.sad) {
			writer.write(" sad=\"true\"");
		}
		if (this.crying) {
			writer.write(" crying=\"true\"");
		}

		if (this.love) {
			writer.write(" love=\"true\"");
		}
		if (this.like) {
			writer.write(" like=\"true\"");
		}
		if (this.dislike) {
			writer.write(" dislike=\"true\"");
		}
		if (this.hate) {
			writer.write(" hate=\"true\"");
		}

		if (this.courageous) {
			writer.write(" courageous=\"true\"");
		}
		if (this.confident) {
			writer.write(" confident=\"true\"");
		}
		if (this.afraid) {
			writer.write(" afraid=\"true\"");
		}
		if (this.panic) {
			writer.write(" panic=\"true\"");
		}

		if (this.serene) {
			writer.write(" serene=\"true\"");
		}
		if (this.calm) {
			writer.write(" calm=\"true\"");
		}
		if (this.anger) {
			writer.write(" anger=\"true\"");
		}
		if (this.rage) {
			writer.write(" rage=\"true\"");
		}

		if (this.surprise) {
			writer.write(" surprise=\"true\"");
		}
		if (this.bored) {
			writer.write(" bored=\"true\"");
		}

		if (this.laughter) {
			writer.write(" laughter=\"true\"");
		}
		if (this.serious) {
			writer.write(" serious=\"true\"");
		}

		if (this.none) {
			writer.write(" none=\"true\"");
		}
		
		writer.write("/>");
		return writer.toString();
	}
}
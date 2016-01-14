package org.botlibre.sdk.config;

import java.io.StringWriter;

import org.w3c.dom.Element;


/**
 * DTO for XML voice config.
 */
public class LearningConfig extends Config {
	
	public String learningMode;	
	public String correctionMode;
	public boolean enableComprehension;
	public boolean enableEmoting;
	public boolean enableEmotions;
	public boolean enableConsciousness;
	public boolean enableWiktionary;
	public boolean enableResponseMatch;
	public boolean learnGrammar;
	public boolean fixFormulaCase;
	public boolean checkExactMatchFirst;
	public int scriptTimeout;
	public int responseMatchTimeout;
	public String conversationMatchPercentage;
	public String discussionMatchPercentage;
	
	public void parseXML(Element element) {
		super.parseXML(element);
		
		this.learningMode = element.getAttribute("learningMode");
		this.correctionMode = element.getAttribute("correctionMode");
		this.enableComprehension = Boolean.valueOf(element.getAttribute("enableComprehension"));
		this.enableEmoting = Boolean.valueOf(element.getAttribute("enableEmoting"));
		this.enableEmotions = Boolean.valueOf(element.getAttribute("enableEmotions"));
		this.enableConsciousness = Boolean.valueOf(element.getAttribute("enableConsciousness"));
		this.enableWiktionary = Boolean.valueOf(element.getAttribute("enableWiktionary"));
		this.enableResponseMatch = Boolean.valueOf(element.getAttribute("enableResponseMatch"));
		this.learnGrammar = Boolean.valueOf(element.getAttribute("learnGrammar"));
		this.fixFormulaCase = Boolean.valueOf(element.getAttribute("fixFormulaCase"));
		this.checkExactMatchFirst = Boolean.valueOf(element.getAttribute("checkExactMatchFirst"));
		String value = element.getAttribute("scriptTimeout");
		if (value != null && value.length() > 0) {
			try {
				this.scriptTimeout = Integer.valueOf(value);
			} catch (Exception ignore) {}
		}
		value = element.getAttribute("responseMatchTimeout");
		if (value != null && value.length() > 0) {
			try {
				this.responseMatchTimeout = Integer.valueOf(value);
			} catch (Exception ignore) {}
		}
		this.conversationMatchPercentage = element.getAttribute("conversationMatchPercentage");
		this.discussionMatchPercentage = element.getAttribute("discussionMatchPercentage");
	}

	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<learning");
		writeCredentials(writer);

		if (this.learningMode != null) {
			writer.write(" learningMode=\"" + this.learningMode + "\"");
		}
		if (this.correctionMode != null) {
			writer.write(" correctionMode=\"" + this.correctionMode + "\"");
		}
		if (this.enableComprehension) {
			writer.write(" enableComprehension=\"" + this.enableComprehension + "\"");
		}
		if (this.enableEmoting) {
			writer.write(" enableEmoting=\"" + this.enableEmoting + "\"");
		}
		if (this.enableEmotions) {
			writer.write(" enableEmotions=\"" + this.enableEmotions + "\"");
		}
		if (this.enableConsciousness) {
			writer.write(" enableConsciousness=\"" + this.enableConsciousness + "\"");
		}
		if (this.enableWiktionary) {
			writer.write(" enableWiktionary=\"" + this.enableWiktionary + "\"");
		}
		if (this.enableResponseMatch) {
			writer.write(" enableResponseMatch=\"" + this.enableResponseMatch + "\"");
		}
		if (this.learnGrammar) {
			writer.write(" learnGrammar=\"" + this.learnGrammar + "\"");
		}
		if (this.fixFormulaCase) {
			writer.write(" fixFormulaCase=\"" + this.fixFormulaCase + "\"");
		}
		if (this.checkExactMatchFirst) {
			writer.write(" checkExactMatchFirst=\"" + this.checkExactMatchFirst + "\"");
		}
		if (this.scriptTimeout != 0) {
			writer.write(" scriptTimeout=\"" + this.scriptTimeout + "\"");
		}
		if (this.responseMatchTimeout != 0) {
			writer.write(" responseMatchTimeout=\"" + this.responseMatchTimeout + "\"");
		}
		if (this.conversationMatchPercentage != null && this.conversationMatchPercentage.length() > 0) {
			writer.write(" conversationMatchPercentage=\"" + this.conversationMatchPercentage + "\"");
		}
		if (this.discussionMatchPercentage != null && this.discussionMatchPercentage.length() > 0) {
			writer.write(" discussionMatchPercentage=\"" + this.discussionMatchPercentage + "\"");
		}
		
		writer.write("/>");
		return writer.toString();
	}
}
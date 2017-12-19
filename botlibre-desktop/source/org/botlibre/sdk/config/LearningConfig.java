/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package org.botlibre.sdk.config;

import java.io.StringWriter;

import org.w3c.dom.Element;


/**
 * DTO for XML voice config.
 */
public class LearningConfig extends Config {
	
	public String learningMode;
	public String learningRate;
	public String correctionMode;
	public boolean enableComprehension;
	public boolean enableEmoting;
	public boolean enableEmotions;
	public boolean enableConsciousness;
	public boolean enableWiktionary;
	public boolean enableResponseMatch;
	public boolean learnGrammar;
	public boolean synthesizeResponse;
	public boolean fixFormulaCase;
	public boolean checkExactMatchFirst;
	public int scriptTimeout;
	public int responseMatchTimeout;
	public String conversationMatchPercentage;
	public String discussionMatchPercentage;
	
	public void parseXML(Element element) {
		super.parseXML(element);
		
		this.learningMode = element.getAttribute("learningMode");
		this.learningRate = element.getAttribute("learningRate");
		this.correctionMode = element.getAttribute("correctionMode");
		this.enableComprehension = Boolean.valueOf(element.getAttribute("enableComprehension"));
		this.enableEmoting = Boolean.valueOf(element.getAttribute("enableEmoting"));
		this.enableEmotions = Boolean.valueOf(element.getAttribute("enableEmotions"));
		this.enableConsciousness = Boolean.valueOf(element.getAttribute("enableConsciousness"));
		this.enableWiktionary = Boolean.valueOf(element.getAttribute("enableWiktionary"));
		this.enableResponseMatch = Boolean.valueOf(element.getAttribute("enableResponseMatch"));
		this.learnGrammar = Boolean.valueOf(element.getAttribute("learnGrammar"));
		this.synthesizeResponse = Boolean.valueOf(element.getAttribute("synthesizeResponse"));
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
		writer.write(" enableComprehension=\"" + this.enableComprehension + "\"");
		writer.write(" enableEmoting=\"" + this.enableEmoting + "\"");
		writer.write(" enableEmotions=\"" + this.enableEmotions + "\"");
		writer.write(" enableConsciousness=\"" + this.enableConsciousness + "\"");
		writer.write(" enableWiktionary=\"" + this.enableWiktionary + "\"");
		writer.write(" enableResponseMatch=\"" + this.enableResponseMatch + "\"");
		writer.write(" learnGrammar=\"" + this.learnGrammar + "\"");
		writer.write(" synthesizeResponse=\"" + this.synthesizeResponse + "\"");
		writer.write(" fixFormulaCase=\"" + this.fixFormulaCase + "\"");
		writer.write(" checkExactMatchFirst=\"" + this.checkExactMatchFirst + "\"");
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
		if (this.learningRate != null && this.learningRate.length() > 0) {
			writer.write(" learningRate=\"" + this.learningRate + "\"");
		}
		
		writer.write("/>");
		return writer.toString();
	}
}
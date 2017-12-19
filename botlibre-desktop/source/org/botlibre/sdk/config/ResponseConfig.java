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

import org.botlibre.sdk.util.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * DTO for XML response config.
 */
public class ResponseConfig extends Config  {
	public String questionId;
	public String responseId;
	public String question;
	public String response;
	public String previous;
	public String onRepeat;
	public String label;
	public String topic;
	public String keywords;
	public String required;
	public String emotions;
	public String actions;
	public String poses;
	public boolean noRepeat;
	public boolean requirePrevious;
	public boolean requireTopic;
	public boolean flagged;
	public String correctness;
	public String command;

	public ResponseConfig() {
		
	}
	
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if ((object instanceof ResponseConfig) && (this.responseId != null) && !this.responseId.isEmpty()
				&& this.responseId.equals(((ResponseConfig)object).responseId)) {
			if (((this.questionId == null) || this.questionId.isEmpty())
					&& (((ResponseConfig)object).questionId == null) || ((ResponseConfig)object).questionId.isEmpty()) {
				return true;
			}
			if ((this.questionId != null) && !this.questionId.isEmpty()
					&& this.questionId.equals(((ResponseConfig)object).questionId)) {
				return true;
			}
		}
		return super.equals(object);
	}

	public String toXML() {
		StringWriter writer = new StringWriter();
		writeXML(writer);
		return writer.toString();
	}

	public void writeXML(StringWriter writer) {
		writer.write("<response");
		writeCredentials(writer);
		if (this.questionId != null) {
			writer.write(" questionId=\"" + this.questionId + "\"");
		}
		if (this.responseId != null) {
			writer.write(" responseId=\"" + this.responseId + "\"");
		}
		if (this.label != null) {
			writer.write(" label=\"" + this.label + "\"");
		}
		if (this.topic != null) {
			writer.write(" topic=\"" + this.topic + "\"");
		}
		if (this.keywords != null) {
			writer.write(" keywords=\"" + this.keywords + "\"");
		}
		if (this.required != null) {
			writer.write(" required=\"" + this.required + "\"");
		}
		if (this.emotions != null) {
			writer.write(" emotions=\"" + this.emotions + "\"");
		}
		if (this.actions != null) {
			writer.write(" actions=\"" + this.actions + "\"");
		}
		if (this.poses != null) {
			writer.write(" poses=\"" + this.poses + "\"");
		}
		if (this.correctness != null) {
			writer.write(" correctness=\"" + this.correctness + "\"");
		}
		writer.write(" noRepeat=\"" + this.noRepeat + "\"");
		writer.write(" requirePrevious=\"" + this.requirePrevious + "\"");
		writer.write(" requireTopic=\"" + this.requireTopic + "\"");
		writer.write(" flagged=\"" + this.flagged + "\"");
		writer.write(">");
		if (this.question != null) {
			writer.write("<question>");
			writer.write(Utils.escapeHTML(this.question));
			writer.write("</question>");
		}
		if (this.response != null) {
			writer.write("<response>");
			writer.write(Utils.escapeHTML(this.response));
			writer.write("</response>");
		}
		if (this.previous != null) {
			writer.write("<previous>");
			writer.write(Utils.escapeHTML(this.previous));
			writer.write("</previous>");
		}
		if (this.onRepeat != null) {
			writer.write("<onRepeat>");
			writer.write(Utils.escapeHTML(this.onRepeat));
			writer.write("</onRepeat>");
		}
		if (this.command != null) {
			writer.write("<command>");
			writer.write(Utils.escapeHTML(this.command));
			writer.write("</command>");
		}
		writer.write("</response>");
	}
	
	public void parseXML(Element element) {		
		this.questionId = element.getAttribute("questionId");
		this.responseId = element.getAttribute("responseId");
		this.label = element.getAttribute("label");
		this.topic = element.getAttribute("topic");
		this.keywords = element.getAttribute("keywords");
		this.required = element.getAttribute("required");
		this.emotions = element.getAttribute("emotions");
		this.actions = element.getAttribute("actions");
		this.poses = element.getAttribute("poses");
		this.type = element.getAttribute("type");
		this.correctness = element.getAttribute("correctness");
		if (element.getAttribute("noRepeat") != null) {
			this.noRepeat = Boolean.valueOf(element.getAttribute("noRepeat"));
		}
		if (element.getAttribute("flagged") != null) {
			this.flagged = Boolean.valueOf(element.getAttribute("flagged"));
		}
		if (element.getAttribute("requireTopic") != null) {
			this.requireTopic = Boolean.valueOf(element.getAttribute("requireTopic"));
		}
		if (element.getAttribute("requirePrevious") != null) {
			this.requirePrevious = Boolean.valueOf(element.getAttribute("requirePrevious"));
		}
		
		Node node = element.getElementsByTagName("question").item(0);
		if (node != null) {
			this.question = node.getTextContent();
		}
		node = element.getElementsByTagName("response").item(0);
		if (node != null) {
			this.response = node.getTextContent();
		}
		node = element.getElementsByTagName("command").item(0);
		if (node != null) {
			this.command = node.getTextContent();
			this.command = this.command.replace("&#34;", "\"");
		}
	}
	
//	public JSONObject getCommand(){
//		if (this.command == null || this.command.equals("")){
//			return null;
//		}
//		try {
//			return new JSONObject(this.command);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
}
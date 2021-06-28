/******************************************************************************
 *
 *  Copyright 2013-2019 Paphus Solutions Inc.
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
package org.botlibre.web.service;

import java.io.StringWriter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import org.botlibre.BotException;
import org.botlibre.util.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Entity
@IdClass(TranslationId.class)
public class Translation {
	
	@Id
	@Column(length=1024)
	public String text;
	
	@Id
	public String sourceLanguage;
	
	@Id
	public String targetLanguage;

	@Column(length=1024)
	public String translation;
	
	public Translation() {
		
	}
	
	public String toString() {
		return "Translation(" + this.text + ")";
	}
	
	public void parseXML(Element element) {
		this.sourceLanguage = element.getAttribute("sourceLanguage");
		this.targetLanguage = element.getAttribute("targetLanguage");
		
		Node node = element.getElementsByTagName("text").item(0);
		if (node != null) {
			this.text = node.getTextContent();
		}
		node = element.getElementsByTagName("translation").item(0);
		if (node != null) {
			this.translation = node.getTextContent();
		}
	}
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<translation");
		if (this.sourceLanguage != null) {
			writer.write(" sourceLanguage=\"" + this.sourceLanguage + "\"");
		}
		if (this.targetLanguage != null) {
			writer.write(" targetLanguage=\"" + this.targetLanguage + "\"");
		}
		writer.write(">");
		
		if (this.text != null) {
			writer.write("<text>");
			writer.write(Utils.escapeHTML(this.text));
			writer.write("</text>");
		}
		if (this.translation != null) {
			writer.write("<translation>");
			writer.write(Utils.escapeHTML(this.translation));
			writer.write("</translation>");
		}
		writer.write("</translation>");
		return writer.toString();
	}
	
	public void checkConstraints() {
		if ((this.text != null) && (this.text.length() >= 1024)) {
			throw new BotException("Text size limit exceeded");
		}
		if ((this.sourceLanguage != null) && (this.sourceLanguage.length() >= 10)) {
			throw new BotException("Source language size limit exceeded");
		}
		if ((this.targetLanguage != null) && (this.targetLanguage.length() >= 10)) {
			throw new BotException("Target language size limit exceeded");
		}
		if ((this.translation != null) && (this.translation.length() >= 1024)) {
			throw new BotException("Translation size limit exceeded");
		}
		Utils.checkScript(this.text);
		Utils.checkScript(this.translation);
	}
}

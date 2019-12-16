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
 * DTO for XML bot mode config.
 */
public class BotModeConfig extends Config {
	
	public String mode;	
	public String bot;
	
	public void parseXML(Element element) {
		super.parseXML(element);
		
		this.mode = element.getAttribute("mode");
		this.bot = element.getAttribute("bot");
	}

	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<bot-mode");
		writeCredentials(writer);

		if (this.mode != null) {
			writer.write(" mode=\"" + this.mode + "\"");
		}
		if (this.bot != null) {
			writer.write(" bot=\"" + this.bot + "\"");
		}
		
		writer.write("/>");
		return writer.toString();
	}
}
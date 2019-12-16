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
import org.w3c.dom.Node;


/**
 * DTO for XML content config.
 */
public class ContentConfig extends Config {
	
	public String type;
	public String name;
	public String icon;
	public String description;
	
	public void parseXML(Element element) {
		super.parseXML(element);
		
		this.type = element.getAttribute("type");
		this.name = element.getAttribute("name");
		this.icon = element.getAttribute("icon");
		
		Node node = element.getElementsByTagName("description").item(0);
		if (node != null) {
			this.description = node.getTextContent();
		}
	}

	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<content");
		writeCredentials(writer);

		if (this.type != null) {
			writer.write(" type=\"" + this.type + "\"");
		}
		
		writer.write("/>");
		return writer.toString();
	}
}
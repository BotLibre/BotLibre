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
 * DTO for XML media config.
 */
public class MediaConfig extends Config {
	public long id;
	public String name;
	public String type;
	public String file;
	public String key;
	
	public void parseXML(Element element) {
		super.parseXML(element);
		
		this.id = Long.valueOf(element.getAttribute("id"));
		this.name = element.getAttribute("name");
		this.type = element.getAttribute("type");
		this.file = element.getAttribute("file");
		this.key = element.getAttribute("key");
	}

	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<media");
		writeCredentials(writer);

		if (this.id != 0) {
			writer.write(" id=\"" + this.id + "\"");
		}
		if (this.name != null) {
			writer.write(" name=\"" + this.name + "\"");
		}
		if (this.type != null) {
			writer.write(" type=\"" + this.type + "\"");
		}
		if (this.file != null) {
			writer.write(" file=\"" + this.file + "\"");
		}
		if (this.key != null) {
			writer.write(" key=\"" + this.key + "\"");
		}
		
		writer.write("/>");
		return writer.toString();
	}
}
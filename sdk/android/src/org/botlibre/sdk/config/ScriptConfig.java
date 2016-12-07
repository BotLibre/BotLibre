/*  Copyright 2016 Paphus Solutions Inc.
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

public class ScriptConfig extends WebMediumConfig{
	public String language;

	public String getType() {
		return "script";
	}

	public WebMediumConfig credentials() {
		ScriptConfig config = new ScriptConfig();
		config.id = this.id;
		return config;
	}
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<script");
		if (this.language != null && !this.language.equals("")){
			writer.write(" language=\"" + this.language + "\"");
		}
		writeXML(writer);
		writer.write("</script>");
		
		return writer.toString();
	}
	
	public void parseXML(Element element){
		super.parseXML(element);
		Node node = element.getElementsByTagName("language").item(0);
		if (node != null) {
			this.language = node.getTextContent();
		}
	}
	
	@Override 
	public String toString() {
		return this.name;
	}
	


}

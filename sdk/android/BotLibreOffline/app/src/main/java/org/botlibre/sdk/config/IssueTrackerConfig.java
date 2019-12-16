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

import org.w3c.dom.Element;

import java.io.StringWriter;

/**
 * DTO for XML issue tracker config.
 */
public class IssueTrackerConfig extends WebMediumConfig {
	public String createAccessMode;
	public String issues;
	
	public String getType() {
		return "issuetracker";
	}

	@Override
	public String stats() {
		return this.issues + " issues";
	}
	
	public WebMediumConfig credentials() {
		IssueTrackerConfig config = new IssueTrackerConfig();
		config.id = this.id;
		return config;
	}
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<issue-tracker");
		if (this.createAccessMode != null && !this.createAccessMode.equals("")) {
			writer.write(" createAccessMode=\"" + this.createAccessMode + "\"");
		}
		writeXML(writer);
		writer.write("</issue-tracker>");
		return writer.toString();
	}
	
	public void parseXML(Element element) {
		super.parseXML(element);
		this.createAccessMode = element.getAttribute("createAccessMode");
		this.issues = element.getAttribute("issues");
	}
}
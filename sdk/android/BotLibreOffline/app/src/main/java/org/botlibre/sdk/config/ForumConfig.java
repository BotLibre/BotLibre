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
 * DTO for XML forum config.
 */
public class ForumConfig extends WebMediumConfig {
	public String replyAccessMode;
	public String postAccessMode;
	public String posts;
	
	public String getType() {
		return "forum";
	}

	@Override
	public String stats() {
		return this.posts + " posts";
	}
	
	public WebMediumConfig credentials() {
		ForumConfig config = new ForumConfig();
		config.id = this.id;
		return config;
	}
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<forum");
		if (this.replyAccessMode != null && !this.replyAccessMode.equals("")) {
			writer.write(" replyAccessMode=\"" + this.replyAccessMode + "\"");
		}
		if (this.postAccessMode != null && !this.postAccessMode.equals("")) {
			writer.write(" postAccessMode=\"" + this.postAccessMode + "\"");
		}
		writeXML(writer);
		writer.write("</forum>");
		return writer.toString();
	}
	
	public void parseXML(Element element) {
		super.parseXML(element);
		this.replyAccessMode = element.getAttribute("replyAccessMode");
		this.postAccessMode = element.getAttribute("postAccessMode");
		this.posts = element.getAttribute("posts");
	}
}
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.botlibre.sdk.util.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * DTO for XML input config.
 */
public class InputConfig extends Config {
	public String id;
	public String creationDate;
	public String speaker;
	public String target;
	public String value;
	
	public void parseXML(Element element) {
		this.id = element.getAttribute("id");
		this.creationDate = element.getAttribute("creationDate");
		this.speaker = element.getAttribute("speaker");
		this.target = element.getAttribute("target");
		
		Node node = element.getElementsByTagName("value").item(0);
		if (node != null) {
			this.value = node.getTextContent();
		}
	}

	public String displayCreationDate() {
		try {
			SimpleDateFormat formater = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			Date date = formater.parse(creationDate);
			return Utils.displayTime(date);
		} catch (Exception exception) {
			return creationDate;
		}
	}
}
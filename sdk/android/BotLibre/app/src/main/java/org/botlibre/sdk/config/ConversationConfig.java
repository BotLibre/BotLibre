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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.botlibre.sdk.util.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * DTO for XML conversation config.
 */
public class ConversationConfig extends Config {
	public String id;
	public String creationDate;
	public String type;
	
	public List<InputConfig> input;
	
	public void parseXML(Element element) {
		this.id = element.getAttribute("id");
		this.creationDate = element.getAttribute("creationDate");
		this.type = element.getAttribute("type");
		
		NodeList nodes = element.getElementsByTagName("input");
		if (nodes != null && nodes.getLength() > 0) {
			this.input = new ArrayList<InputConfig>();
			for (int index = 0; index < nodes.getLength(); index++) {
				Element node = (Element)nodes.item(index);
				InputConfig config = new InputConfig();
				config.parseXML(node);
				this.input.add(config);
			}
		}
	}

	public String displayCreationDate() {
		try {
			SimpleDateFormat formater = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			Date date = formater.parse(creationDate);
			return Utils.displayTimestamp(date);
		} catch (Exception exception) {
			return creationDate;
		}
	}
}
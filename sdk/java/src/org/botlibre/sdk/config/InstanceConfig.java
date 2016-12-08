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
 * DTO for XML bot instance config.
 */
public class InstanceConfig extends WebMediumConfig {
	public String size;
	public String instanceAvatar;
	public boolean allowForking;
	public boolean hasAPI;
	public String template;
	public int rank;
	public int wins;
	public int losses;
	
	public String getType() {
		return "instance";
	}

	@Override
	public String stats() {
		return this.connects + " connects, " + this.dailyConnects + " today, " + this.weeklyConnects + " week, " + this.monthlyConnects + " month";
	}

	@Override
	public InstanceConfig credentials() {
		InstanceConfig config = new InstanceConfig();
		config.id = this.id;
		return config;
	}
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<instance");
		if (this.allowForking) {
			writer.write(" allowForking=\"true\"");
		}
		if (this.instanceAvatar != null) {
			writer.write(" instanceAvatar=\"" + this.instanceAvatar + "\"");
		}
		writeXML(writer);
		if (this.template != null) {
			writer.write("<template>");
			writer.write(this.template);
			writer.write("</template>");
		}
		writer.write("</instance>");
		return writer.toString();
	}
	
	public void parseXML(Element element) {
		super.parseXML(element);
		this.allowForking = Boolean.valueOf(element.getAttribute("allowForking"));
		this.hasAPI = Boolean.valueOf(element.getAttribute("hasAPI"));
		this.size = element.getAttribute("size");
		this.instanceAvatar = element.getAttribute("instanceAvatar");
		if (element.getAttribute("rank") != null && element.getAttribute("rank").trim().length() > 0) {
			this.rank = Integer.valueOf(element.getAttribute("rank"));
		}
		if (element.getAttribute("wins") != null && element.getAttribute("wins").trim().length() > 0) {
			this.wins = Integer.valueOf(element.getAttribute("wins"));
		}
		if (element.getAttribute("losses") != null && element.getAttribute("losses").trim().length() > 0) {
			this.losses = Integer.valueOf(element.getAttribute("losses"));
		}
		
		Node node = element.getElementsByTagName("template").item(0);
		if (node != null) {
			this.template = node.getTextContent();
		}
	}
}
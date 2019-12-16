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
 * DTO for XML voice config.
 */
public class VoiceConfig extends Config {

	public String voice;
	public boolean nativeVoice;
	public String language;	
	public String pitch;
	public String speechRate;
	public String mod;
	
	public void parseXML(Element element) {
		super.parseXML(element);

		this.voice = element.getAttribute("voice");
		this.nativeVoice = Boolean.valueOf(element.getAttribute("nativeVoice"));
		this.language = element.getAttribute("language");
		this.pitch = element.getAttribute("pitch");
		this.speechRate = element.getAttribute("speechRate");
		this.mod = element.getAttribute("mod");
	}

	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<voice");
		writeCredentials(writer);

		if (this.voice != null) {
			writer.write(" voice=\"" + this.voice + "\"");
		}
		if (nativeVoice) {
			writer.write(" nativeVoice=\"" + this.nativeVoice + "\"");
		}
		if (this.language != null) {
			writer.write(" language=\"" + this.language + "\"");
		}
		if (this.pitch != null) {
			writer.write(" pitch=\"" + this.pitch + "\"");
		}
		if (this.speechRate != null) {
			writer.write(" speechRate=\"" + this.speechRate + "\"");
		}
		if(this.mod != null){
			writer.write(" mod=\"" + this.mod.toLowerCase() + "\"");
		}
		
		writer.write("/>");
		return writer.toString();
	}
}
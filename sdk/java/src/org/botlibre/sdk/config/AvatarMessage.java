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

import org.botlibre.sdk.util.Utils;

/**
 * An avatar message can be used to have an avatar speak a message.
 * The avatar message gives the avatar the text to speak, and returns a ChatResponse that includes the text-to-speech and video animation.
 * This object can be converted to and from XML for usage with the web API.
 */
public class AvatarMessage extends Config {
	public String message;
	public String avatar;
	public String emote;
	public String action;
	public String pose;
	public boolean speak;
	public String voice;
	public String format;
	public boolean hd;
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<avatar-message");
		writeCredentials(writer);
		if (this.avatar != null) {
			writer.write(" avatar=\"" + this.avatar + "\"");
		}
		if (this.emote != null) {
			writer.write(" emote=\"" + this.emote + "\"");
		}
		if (this.action != null) {
			writer.write(" action=\"" + this.action + "\"");
		}
		if (this.pose != null) {
			writer.write(" pose=\"" + this.pose + "\"");
		}
		if (this.format != null) {
			writer.write(" format=\"" + this.format + "\"");
		}
		if (this.voice != null) {
			writer.write(" voice=\"" + this.voice + "\"");
		}
		if (this.speak) {
			writer.write(" speak=\"" + this.speak + "\"");
		}
		if (this.hd) {
			writer.write(" hd=\"" + this.hd + "\"");
		}
		writer.write(">");
		
		if (this.message != null) {
			writer.write("<message>");
			writer.write(Utils.escapeHTML(this.message));
			writer.write("</message>");
		}
		writer.write("</avatar-message>");
		return writer.toString();
	}
}
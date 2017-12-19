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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import org.w3c.dom.Element;

/**
 * Represents a media file for an avatar (image, video, audio).
 * An avatar can have many media files that are tagged with emotions, actions, and poses.
 * This object can be converted to and from XML for usage with the web API.
 * The media is the short URL to the media file on the server.
 */
public class AvatarMedia extends Config {
	public String mediaId;
	public String name;
	public String type;
	public String media;
	public String emotions;
	public String actions;
	public String poses;
	public boolean hd;
	public boolean talking;
	
	public void parseXML(Element element) {
		super.parseXML(element);
		
		this.mediaId = element.getAttribute("mediaId");
		this.name = element.getAttribute("name");
		this.type = element.getAttribute("type");
		this.media = element.getAttribute("media");
		this.emotions = element.getAttribute("emotions");
		this.actions = element.getAttribute("actions");
		this.poses = element.getAttribute("poses");
		this.hd = Boolean.valueOf(element.getAttribute("hd"));
		this.talking = Boolean.valueOf(element.getAttribute("talking"));
	}
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<avatar-media");
		writeCredentials(writer);
		if (this.mediaId != null) {
			writer.write(" mediaId=\"" + this.mediaId + "\"");
		}
		if (this.name != null) {
			writer.write(" name=\"" + this.name + "\"");
		}
		if (this.type != null) {
			writer.write(" type=\"" + this.type + "\"");
		}
		if (this.emotions != null) {
			writer.write(" emotions=\"" + this.emotions + "\"");
		}
		if (this.actions != null) {
			writer.write(" actions=\"" + this.actions + "\"");
		}
		if (this.poses != null) {
			writer.write(" poses=\"" + this.poses + "\"");
		}
		writer.write(" hd=\"" + this.hd + "\"");
		writer.write(" talking=\"" + this.talking + "\"");
		writer.write("/>");
		return writer.toString();
	}
	
	public boolean getHD() {
		return hd;
	}
	public boolean isImage() {
		return this.type.contains("image") || this.type.isEmpty();
	}
	
	public boolean isVideo() {
		return this.type != null && this.type.indexOf("video") != -1;
	}
	
	public boolean isAudio() {
		return this.type != null && this.type.indexOf("audio") != -1;
	}
	public String getType() {
		return type;
	}
	public String getEmotions() {
		return emotions;
	}

	public void setEmotions(String emotions) {
		this.emotions = emotions;
	}
	public String getActions() {
		return actions;
	}
	
	public String getPoses() {
		return poses;
	}

	public void setPoses(String poses) {
		this.poses = poses;
	}

	public void setActions(String actions) {
		this.actions = actions;
	}
	public boolean getTalking() {
		return talking;
	}

	public void setTalking(boolean talking) {
		this.talking = talking;
	}
	@Override
	public String toString(){
		return "Info: --------------------------"
				+ "\nMedia-ID: " + mediaId
				+"\nName: " + name
				+"\nType: "+ type
				+"\nMedia: "+ media
				+"\nEmotion: "+ emotions
				+"\nAction: " + actions
				+"\nPose: " + poses
				+"\nTalking: " + talking;
	}
}

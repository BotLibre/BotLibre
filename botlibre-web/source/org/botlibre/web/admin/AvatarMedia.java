/******************************************************************************
 *
 *  Copyright 2013-2019 Paphus Solutions Inc.
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
package org.botlibre.web.admin;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.botlibre.web.rest.AvatarMediaConfig;

@Entity
public class AvatarMedia extends AbstractMedia {
	protected String emotions = "";
	protected String actions = "";
	protected String poses = "";
	protected boolean hd;
	protected boolean talking;
	@ManyToOne
	protected Avatar avatar;
	
	public AvatarMedia() { }

	public boolean getHD() {
		return hd;
	}

	public void setHD(boolean hd) {
		this.hd = hd;
	}

	public boolean getTalking() {
		return talking;
	}

	public void setTalking(boolean talking) {
		this.talking = talking;
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

	public void setActions(String actions) {
		this.actions = actions;
	}

	public String getPoses() {
		return poses;
	}

	public void setPoses(String poses) {
		this.poses = poses;
	}

	public Avatar getAvatar() {
		return avatar;
	}

	public void setAvatar(Avatar avatar) {
		this.avatar = avatar;
	}
	
	public AvatarMediaConfig toConfig() {
		AvatarMediaConfig config = new AvatarMediaConfig();
		config.mediaId = String.valueOf(this.mediaId);
		config.name = this.name;
		config.type = this.type;
		config.emotions = this.emotions;
		config.actions = this.actions;
		config.poses = this.poses;
		config.media = getFileName();
		config.hd = this.hd;
		config.talking = this.talking;
		return config;
	}
	
	public String toString() {
		return "AvatarMedia(" + this.name + ":" + this.type + ":" + this.hd + ":" + this.talking + ")";
	}
}

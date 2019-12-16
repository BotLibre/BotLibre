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
package org.botlibre.web.bean;

import java.util.ArrayList;
import java.util.List;

import org.botlibre.emotion.EmotionalState;
import org.botlibre.knowledge.BinaryData;

public class AvatarInfo implements Comparable<AvatarInfo> {
	long id;
	String fileName;
	BinaryData image;
	List<EmotionalState> tags = new ArrayList<EmotionalState>();
	
	@Override
	public int compareTo(AvatarInfo info) {
		if (this.id > info.id) {
			return 1;
		} else if (this.id < info.id) {
			return -1;
		}
		return 0;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public List<EmotionalState> getTags() {
		return tags;
	}
	public void setTags(List<EmotionalState> tags) {
		this.tags = tags;
	}
	public BinaryData getImage() {
		return image;
	}
	public void setImage(BinaryData image) {
		this.image = image;
	}	
	
}

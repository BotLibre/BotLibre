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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;

@Entity
@Cache(type=CacheType.WEAK)
public class Media {
	@Id
	@GeneratedValue
	protected long id;
	@Lob
	protected byte[] media;
	@Lob
	protected byte[] thumb;
	
	public Media() { }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public byte[] getThumb() {
		return thumb;
	}

	public void setThumb(byte[] thumb) {
		this.thumb = thumb;
	}

	public byte[] getMedia() {
		return media;
	}

	public void setMedia(byte[] media) {
		this.media = media;
	}
	
}

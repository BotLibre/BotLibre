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

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;

import org.botlibre.web.rest.GraphicConfig;
import org.botlibre.web.rest.WebMediumConfig;

@Entity
@AssociationOverrides({
	@AssociationOverride(name="admins", joinTable=@JoinTable(name="GRAPHIC_ADMINS")),
	@AssociationOverride(name="users", joinTable=@JoinTable(name="GRAPHIC_USERS")),
	@AssociationOverride(name="tags", joinTable=@JoinTable(name="GRAPHIC_TAGS")),
	@AssociationOverride(name="categories", joinTable=@JoinTable(name="GRAPHIC_CATEGORIES")),
	@AssociationOverride(name="errors", joinTable=@JoinTable(name="GRAPHIC_ERRORS"))
})
public class Graphic extends WebMedium {
	
	@OneToOne
	protected MediaFile media;
	
	public Graphic() {
	}

	public Graphic(String name) {
		super(name);
		this.name = name;
	}
	
	public WebMediumConfig buildBrowseConfig() {
		GraphicConfig config = new GraphicConfig();
		toBrowseConfig(config);
		return config;
	}

	@Override
	public String getTypeName() {
		return "Graphic";
	}

	public GraphicConfig buildConfig() {
		GraphicConfig config = new GraphicConfig();
		toConfig(config);
		config.lastConnectedUser = this.lastConnectedUser;
		if (this.media != null) {
			config.media = this.media.getFileName();
			config.fileName = this.media.getName();
			config.fileType = this.media.getType();
		}
		return config;
	}

	public MediaFile getMedia() {
		return media;
	}

	public void setMedia(MediaFile media) {
		this.media = media;
	}
	
	@Override
	public void preDelete(EntityManager em) {
		super.preDelete(em);
		if (this.media != null) {
			em.remove(this.media);
			em.remove(em.find(Media.class, this.media.getMediaId()));
		}
	}
}

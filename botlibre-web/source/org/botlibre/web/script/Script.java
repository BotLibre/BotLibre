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
package org.botlibre.web.script;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.Query;

import org.botlibre.util.Utils;

import org.botlibre.web.admin.WebMedium;
import org.botlibre.web.rest.ScriptConfig;
import org.botlibre.web.rest.ScriptSourceConfig;
import org.botlibre.web.rest.WebMediumConfig;

@Entity
@AssociationOverrides({
	@AssociationOverride(name="admins", joinTable=@JoinTable(name="SCRIPT_ADMINS")),
	@AssociationOverride(name="users", joinTable=@JoinTable(name="SCRIPT_USERS")),
	@AssociationOverride(name="tags", joinTable=@JoinTable(name="SCRIPT_TAGS")),
	@AssociationOverride(name="categories", joinTable=@JoinTable(name="SCRIPT_CATEGORIES")),
	@AssociationOverride(name="errors", joinTable=@JoinTable(name="SCRIPT_ERRORS"))
})
public class Script extends WebMedium {
	protected int size;
	protected String language = "";
	@OneToOne(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
	protected ScriptSource source;
	
	public Script() {
	}

	public Script(String name) {
		super(name);
		this.source = new ScriptSource(this);
	}
	
	public WebMediumConfig buildBrowseConfig() {
		ScriptConfig config = new ScriptConfig();
		toBrowseConfig(config);
		return config;
	}

	public ScriptConfig buildConfig() {
		ScriptConfig config = new ScriptConfig();
		toConfig(config);
		config.language = this.language;
		config.size = String.valueOf(this.size);
		config.version = this.source.getVersion();
		return config;
	}

	public String getForwarderAddress() {
		return "/script?id=" + getId() + "&file=true";
	}

	public String getLanguage() {
		return language;
	}
	
	public String getExt() {
		if (this.language == null || this.language.isEmpty()) {
			return "";
		} else if (this.language.equals("Chat Log")) {
			return "log";
		} else if (this.language.equals("Response List")) {
			return "res";
		} else if (this.language.equals("CSV List")) {
			return "cvs";
		} else if (this.language.equals("JavaScript")) {
			return "js";
		} else if (this.language.equals("Objective C")) {
			return "c";
		} else if (this.language.equals("Smalltalk")) {
			return "st";
		}
		return Utils.compress(this.language, 4).toLowerCase();
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getNextVersion() {
		if (this.source == null) {
			return "0.1";
		}
		String version = this.source.getVersion();
		int index = version.lastIndexOf('.');
		if (index != -1) {
			String major = version.substring(0, index);
			String minor = version.substring(index + 1, version.length());
			try {
				int value = Integer.valueOf(minor);
				version = major + "." + (value + 1);
			} catch (NumberFormatException ignore) {}
		}
		return version;
	}

	public ScriptSource getSource() {
		if (source == null) {
			source = new ScriptSource(this);
		}
		return source;
	}

	public void setSource(ScriptSource source) {
		this.source = source;
		if (source != null && source.getSource() != null) {
			this.size = source.getSource().length();
		}
	}

	public String getSourceCode() {
		if (this.source == null || this.source.getSource() == null) {
			return "";
		}
		return getSource().getSource();
	}

	public ScriptSourceConfig getSourceConfig() {
		if (this.source == null || this.source.getSource() == null) {
			return new ScriptSourceConfig();
		}
		return getSource().toConfig();
	}

	public void setSourceCode(String source) {
		this.size = source.length();
		getSource().setSource(source);
	}
	
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public void preDelete(EntityManager em) {
		super.preDelete(em);
		Query query = em.createQuery("Delete from ScriptSource p where p.script = :script and p.id <> :id");
		query.setParameter("script", detach());
		if (this.source == null) {
			query.setParameter("id", 0);
		} else {
			query.setParameter("id", this.source.getId());
		}
		query.executeUpdate();		
	}
	
}

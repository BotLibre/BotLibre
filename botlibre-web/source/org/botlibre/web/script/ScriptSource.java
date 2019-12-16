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

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.botlibre.web.admin.User;
import org.botlibre.web.rest.ScriptSourceConfig;

@Entity
public class ScriptSource {
	@Id
	@GeneratedValue
	protected long id;
	@Temporal(TemporalType.TIMESTAMP)
	protected Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	protected Date updateDate;
	protected String version;
	
	@ManyToOne(fetch=FetchType.LAZY)
	protected User creator;	
	@ManyToOne(fetch=FetchType.LAZY)
	protected Script script;
	
	@Lob
	protected String source;
	
	public ScriptSource() { }
	
	public ScriptSource(Script script) {
		this.script = script;
	}
	
	public ScriptSourceConfig toConfig() {
		ScriptSourceConfig sourceConfig = new ScriptSourceConfig();
		sourceConfig.source = this.source;
		if (this.creationDate != null) {
			sourceConfig.creationDate = this.creationDate.toString();
		}
		if (this.updateDate != null) {
			sourceConfig.updateDate = this.updateDate.toString();
		}
		if (this.creator != null) {
			sourceConfig.creator = this.creator.getUserId();
		}
		sourceConfig.versionName = this.version;
		return sourceConfig;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Script getScript() {
		return script;
	}

	public void setScript(Script script) {
		this.script = script;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public String getSource() {
		return source;
	}

	public String getEditSource() {
		if (source == null) {
			return "";
		}
		String text = source;
		text = text.replace("<", "&lt;");
		text = text.replace(">", "&gt;");
		return text;
	}

	public void setSource(String source) {
		this.source = source;
	}	
	
}

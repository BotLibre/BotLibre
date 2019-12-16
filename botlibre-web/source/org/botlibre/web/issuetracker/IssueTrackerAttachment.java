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
package org.botlibre.web.issuetracker;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.botlibre.util.Utils;

import org.botlibre.web.admin.AbstractMedia;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.admin.User;
import org.botlibre.web.bean.IssueTrackerBean;
import org.botlibre.web.rest.MediaConfig;

/**
 * Stores forum file/image attachments.
 */
@Entity
public class IssueTrackerAttachment extends AbstractMedia {
	@OneToOne(fetch=FetchType.LAZY)
	protected IssueTracker tracker;
	@OneToOne(fetch=FetchType.LAZY)
	protected User creator;
	@Temporal(TemporalType.TIMESTAMP)
	protected Date creationDate;
	@OneToOne(fetch=FetchType.LAZY)
	protected Domain domain;
	protected long key;
		
	public IssueTrackerAttachment() {
	}
	
	public MediaConfig toConfig() {
		MediaConfig config = new MediaConfig();
		config.id = String.valueOf(this.mediaId);
		config.name = this.name;
		config.type = this.type;
		config.file = getFileName();
		if (this.tracker != null) {
			config.instance = String.valueOf(this.tracker.getId());
		}
		config.key = String.valueOf(this.key);
		return config;
	}

	public long getKey() {
		return key;
	}

	public void setKey(long key) {
		this.key = key;
	}
	
	public void generateKey() {
		this.key = Math.abs(Utils.random().nextLong());
	}

	public IssueTracker getForum() {
		return tracker;
	}

	public void setTracker(IssueTracker tracker) {
		this.tracker = tracker;
	}

	public String getCreatorId() {
		if (this.creator == null) {
			return "";
		}
		return this.creator.getUserId();
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Date getCreationDate() {
		if (creationDate == null) {
			creationDate = new Date();
		}
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	@Override
	public String getFileName() {
		return "issuetracker?attachment=" + this.mediaId + "&key=" + this.key +"&name=" + IssueTrackerBean.encode(this.name);
	}
}

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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Query;

import org.botlibre.BotException;

import org.botlibre.web.admin.AccessMode;
import org.botlibre.web.admin.User;
import org.botlibre.web.admin.WebMedium;
import org.botlibre.web.rest.IssueTrackerConfig;
import org.botlibre.web.rest.WebMediumConfig;

/**
 * An IssueTracker is a tracking system for issues, service requests, bug, etc.
 * Each IssueTracker has its own set of Issues that it tracks that are specific to the product or service being tracked.
 */
@Entity
@AssociationOverrides({
	@AssociationOverride(name="admins", joinTable=@JoinTable(name="ISSUETRACKER_ADMINS")),
	@AssociationOverride(name="users", joinTable=@JoinTable(name="ISSUETRACKER_USERS")),
	@AssociationOverride(name="tags", joinTable=@JoinTable(name="ISSUETRACKER_TAGS")),
	@AssociationOverride(name="categories", joinTable=@JoinTable(name="ISSUETRACKER_CATEGORIES")),
	@AssociationOverride(name="errors", joinTable=@JoinTable(name="ISSUETRACKER_ERRORS"))
})
public class IssueTracker extends WebMedium {

	protected AccessMode createAccessMode = AccessMode.Everyone;
	protected int issues;
	protected int weeklyIssues;
	
	@ManyToMany
	@JoinTable(name = "ISSUETRACKER_SUBSCRIBERS")
	protected List<User> subscribers = new ArrayList<User>();
	
	public IssueTracker() {
	}

	public IssueTracker(String name) {
		super(name);
	}
	
	public WebMediumConfig buildBrowseConfig() {
		IssueTrackerConfig config = new IssueTrackerConfig();
		toBrowseConfig(config);
		return config;
	}

	public IssueTrackerConfig buildConfig() {
		IssueTrackerConfig config = new IssueTrackerConfig();
		toConfig(config);
		return config;
	}

	public String getForwarderAddress() {
		return "/issuetracker?id=" + getId();
	}

	@Override
	public void preDelete(EntityManager em) {
		super.preDelete(em);
		Query query = em.createQuery("Delete from Issue p where p.tracker = :tracker");
		query.setParameter("tracker", detach());
		query.executeUpdate();		
	}

	public void checkIssueAccess(User user) {
		if (!this.isPrivate && (this.createAccessMode == null) || (this.createAccessMode == AccessMode.Everyone)) {
			return;
		} else if (user == null) {
			throw new BotException("This " + getDisplayName() + " does not allow anonymous issues.");
		} else if (this.isPrivate || (this.createAccessMode == AccessMode.Members)) {
			if (!isUser(user) && !isAdmin(user)) {
				throw new BotException("You must be a member create issues in this " + getDisplayName() + ".");				
			}
		} else if (this.createAccessMode == AccessMode.Administrators) {
			if (!isAdmin(user)) {
				throw new BotException("You must be an administrator to create issues in this " + getDisplayName() + ".");				
			}
		}
	}

	public boolean isIssueAllowed(User user) {
		if (!this.isPrivate && (this.createAccessMode == null) || (this.createAccessMode == AccessMode.Everyone)) {
			return true;
		} else if (user == null) {
			return false;
		} else if (this.isPrivate || (this.createAccessMode == AccessMode.Members)) {
			if (!isUser(user) && !isAdmin(user)) {
				return false;			
			}
		} else if (this.createAccessMode == AccessMode.Administrators) {
			if (!isAdmin(user)) {
				return false;			
			}
		}
		return true;
	}

	public void incrementIssues() {
		this.issues = this.issues + 1;
		this.weeklyIssues = this.weeklyIssues + 1;
	}

	public int getIssues() {
		return issues;
	}

	public void setIssues(int issues) {
		this.issues = issues;
	}

	public int getWeeklyIssues() {
		return weeklyIssues;
	}

	public void setWeeklyIssues(int weeklyIssues) {
		this.weeklyIssues = weeklyIssues;
	}

	public List<User> getSubscribers() {
		return subscribers;
	}

	public void setSubscribers(List<User> subscribers) {
		this.subscribers = subscribers;
	}

	public AccessMode getCreateAccessMode() {
		return createAccessMode;
	}

	public void setCreateAccessMode(AccessMode createAccessMode) {
		this.createAccessMode = createAccessMode;
	}
	
}

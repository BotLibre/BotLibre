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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.botlibre.BotException;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

import org.botlibre.web.admin.ClientType;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.admin.Flaggable;
import org.botlibre.web.admin.Tag;
import org.botlibre.web.admin.TextDetails;
import org.botlibre.web.admin.User;
import org.botlibre.web.rest.IssueConfig;

@Entity
public class Issue extends Flaggable {
	@Temporal(TemporalType.TIMESTAMP)
	protected Date updatedDate;
	protected int views;
	protected int dailyViews;
	protected int weeklyViews;
	protected int monthlyViews;
	protected int restViews;
	protected int restDailyViews;
	protected int restWeeklyViews;
	protected int restMonthlyViews;
	@Column(length=1024)
	protected String title;
	protected IssueType type = IssueType.Issue;
	protected Priority priority = Priority.Medium;
	protected Status status = Status.Open;
	@Column(length=1024)
	protected String summary;
	@OneToOne(fetch=FetchType.LAZY)
	protected IssueTracker tracker;
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.LAZY)
	protected TextDetails details;
	protected boolean isHidden;
	protected boolean isPriority;
	@OneToOne(fetch=FetchType.LAZY)
	protected User creator;
	@ManyToMany
	@JoinTable(name = "ISSUE_TAGS")
	protected List<Tag> tags = new ArrayList<Tag>();
	@Transient
	protected String tagsString;
	@ManyToMany
	@JoinTable(name = "ISSUE_SUBSCRIBERS")
	protected List<User> subscribers = new ArrayList<User>();
	
	public enum IssueType { Issue, Bug, Feature, Task, ServiceRequest }

	public enum Priority { Low, Medium, High, Sever }
	
	public enum Status { Open, Rejected, Duplicate, Deferred, Assigned, Implemented, Closed }
	
	public Issue() {
		this.title = "";
	}

	public Issue(String topic) {
		this.title = topic;
		this.details = new TextDetails();
	}

	public IssueConfig buildConfig() {
		IssueConfig config = buildSummaryConfig();
		if (this.details != null) {
			config.details = this.details.getDetails();
			config.detailsText = getTextDetails();
		}
		return config;
	}

	public IssueConfig buildSummaryConfig() {
		IssueConfig config = new IssueConfig();
		config.id = String.valueOf(this.id);
		if (this.tracker != null) {
			config.tracker = String.valueOf(this.tracker.getId());
		}
		config.title = this.title;
		config.issueType = getType().name();
		config.priority = getPriority().name();
		config.status = getStatus().name();
		config.summary = this.summary;
		config.tags = getTagsString();
		config.isPriority = this.isPriority;
		config.isHidden = this.isHidden;
		config.isFlagged = this.isFlagged;
		config.flaggedReason = this.flaggedReason;
		if (this.creator != null) {
			config.creator = this.creator.getUserId();
		}
		if (this.creationDate != null) {
			config.creationDate = this.creationDate.toString();
		}
		config.thumbsUp = String.valueOf(this.thumbsUp);
		config.thumbsDown = String.valueOf(this.thumbsDown);
		config.stars = String.valueOf(this.stars);
		return config;
	}
	
	@Override
	public String getTypeName() {
		return "Issue";
	}
	
	@Override
	public boolean checkProfanity() {
		super.checkProfanity();
		return Utils.checkProfanity(this.title) || Utils.checkProfanity(this.details.getDetails());
	}

	@Override
	public void checkConstraints() {
		super.checkConstraints();
		if (this.title.length() >= 1024) {
			throw new BotException("Text size limit exceeded");
		}
		Utils.checkHTML(this.title);
		this.title = Utils.sanitize(this.title);
	}
	
	public List<User> getSubscribers() {
		return subscribers;
	}

	public void setSubscribers(List<User> subscribers) {
		this.subscribers = subscribers;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public IssueTracker getTracker() {
		return tracker;
	}

	public void setTracker(IssueTracker tracker) {
		this.tracker = tracker;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public Issue clone() {
		Issue post = (Issue)super.clone();
		if (post.details != null) {
			post.details = post.details.clone();
		}
		return post;
	}

	public TextDetails getDetails() {
		return details;
	}
	
	public String getTextDetails() {
		return Utils.formatHTMLOutput(getDetails().getDetails());
	}

	public void setTextDetails(String details) {
		getDetails().setDetails(details);
		String formated = getTextDetails();
		TextStream stream = new TextStream(formated);
		StringWriter writer = new StringWriter();
		int size = 0;
		for (int count = 0; count < 5; count++) {
			String line = stream.nextLine();
			if (size + line.length() < 500) {
				writer.write(line);
				size = size + line.length();
			} else {
				if (size == 0) {
					stream.reset();
					line = stream.upTo('.');
					if (line.length() > 500) {
						line = formated.substring(0, 500);
					}
					writer.write(line);
				}
				break;
			}
		}
		String text = writer.toString();
		text = Utils.sanitize(text);
		if (text.length() > 550) {
			text = text.substring(0, 550);
		}
		setSummary(Utils.sanitize(text));
	}

	public void setDetails(TextDetails details) {
		this.details = details;
	}

	public User getCreator() {
		return creator;
	}

	public String getUpdatedDateString() {
		return this.updatedDate.toString();
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public String getTagsString() {
		if (this.tagsString != null) {
			return tagsString;
		}
		if (this.tags.isEmpty()) {
			return "";
		}
		StringWriter writer = new StringWriter();
		int count = 0;
		for (Tag tag : this.tags) {
			writer.write(tag.getName());
			count++;
			if (count < this.tags.size()) {
				writer.write(", ");
			}
		}
		return writer.toString();
	}

	public void setTagsString(String tagsString) {
		this.tagsString = tagsString;
	}

	public String getTagLinks(String uri) {
		if (this.tags.isEmpty()) {
			return "";
		}
		StringWriter writer = new StringWriter();
		int count = 0;
		for (Tag tag : this.tags) {
			writer.write("<a href=\"");
			writer.write(uri);
			writer.write(String.valueOf(tag.getName()));
			writer.write("\">");
			writer.write(tag.getName());
			writer.write("</a>");
			count++;
			if (count < this.tags.size()) {
				writer.write(", ");
			}
		}
		return writer.toString();
	}

	@SuppressWarnings("unchecked")
	public void setTagsFromString(String csv, EntityManager em, Domain domain) {
		for (Tag tag : this.tags) {
			tag.setCount(tag.getCount() - 1);
		}
		if (csv == null || csv.length() == 0) {
			this.tags = new ArrayList<Tag>();
			return;
		}
		List<Tag> newTags = new ArrayList<Tag>();
		TextStream stream = new TextStream(csv.toLowerCase());
		stream.skipWhitespace();
		for (String word : Utils.csv(csv.toLowerCase())) {
			List<Tag> results = em
					.createQuery("Select t from Tag t where t.name = :name and t.type = :type and t.domain = :domain")
					.setParameter("type", getTypeName())
					.setParameter("name", word)
					.setParameter("domain", domain)
					.getResultList();
			Tag tag = null;
			if (results.isEmpty()) {
				tag = new Tag();
				tag.setName(word);
				tag.setType(getTypeName());
				tag.setDomain(domain);
				em.persist(tag);
			} else {
				tag = results.get(0);
			}
			if (!newTags.contains(tag)) {
				newTags.add(tag);
				tag.setCount(tag.getCount() + 1);
			}
		}
		this.tags = newTags;
		this.tagsString = null;
	}

	public void incrementViews(ClientType clientType) {
		this.views++;
		this.dailyViews++;
		this.weeklyViews++;
		this.monthlyViews++;
		if (clientType == ClientType.REST) {
			this.restViews++;
			this.restDailyViews++;
			this.restWeeklyViews++;
			this.restMonthlyViews++;
		}
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public boolean isPriority() {
		return isPriority;
	}

	public void setIsPriority(boolean isPriority) {
		this.isPriority = isPriority;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public void setIsHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public Priority getPriority() {
		if (this.priority == null) {
			return Priority.Low;
		}
		return this.priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Status getStatus() {
		if (this.status == null) {
			return Status.Open;
		}
		return this.status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public IssueType getType() {
		if (this.type == null) {
			return type.Issue;
		}
		return this.type;
	}

	public void setType(IssueType type) {
		this.type = type;
	}

	public int getViews() {
		return views;
	}

	public void setViews(int views) {
		this.views = views;
	}

	public int getDailyViews() {
		return dailyViews;
	}

	public void setDailyViews(int dailyViews) {
		this.dailyViews = dailyViews;
	}

	public int getWeeklyViews() {
		return weeklyViews;
	}

	public void setWeeklyViews(int weeklyViews) {
		this.weeklyViews = weeklyViews;
	}

	public int getMonthlyViews() {
		return monthlyViews;
	}

	public void setMonthlyViews(int monthlyViews) {
		this.monthlyViews = monthlyViews;
	}

	public int getRestViews() {
		return restViews;
	}

	public void setRestViews(int restViews) {
		this.restViews = restViews;
	}

	public int getRestDailyViews() {
		return restDailyViews;
	}

	public void setRestDailyViews(int restDailyViews) {
		this.restDailyViews = restDailyViews;
	}

	public int getRestWeeklyViews() {
		return restWeeklyViews;
	}

	public void setRestWeeklyViews(int restWeeklyViews) {
		this.restWeeklyViews = restWeeklyViews;
	}

	public int getRestMonthlyViews() {
		return restMonthlyViews;
	}

	public void setRestMonthlyViews(int restMonthlyViews) {
		this.restMonthlyViews = restMonthlyViews;
	}

	public String toString() {
		return getClass().getSimpleName() + "(" + this.title + ")";
	}
}

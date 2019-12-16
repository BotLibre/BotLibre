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
package org.botlibre.web.forum;

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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
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
import org.botlibre.web.bean.ForumPostBean;
import org.botlibre.web.bean.SessionProxyBean;
import org.botlibre.web.rest.ForumPostConfig;

@Entity
public class ForumPost extends Flaggable {
	@Temporal(TemporalType.TIMESTAMP)
	protected Date updatedDate;
	@Column(length=1024)
	protected String topic;
	@Column(length=1024)
	protected String summary;
	@OneToOne(fetch=FetchType.LAZY)
	protected Forum forum;
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.LAZY)
	protected TextDetails details;
	protected boolean isFeatured;
	protected int views;
	protected int dailyViews;
	protected int weeklyViews;
	protected int monthlyViews;
	protected int restViews;
	protected int restDailyViews;
	protected int restWeeklyViews;
	protected int restMonthlyViews;
	protected int replyCount;
	@OneToOne(fetch=FetchType.LAZY)
	protected ForumPost parent;
	@OneToMany(mappedBy="parent", cascade=CascadeType.REMOVE)
	@OrderBy("creationDate")
	protected List<ForumPost> replies = new ArrayList<ForumPost>();
	@OneToOne(fetch=FetchType.LAZY)
	protected User creator;
	@ManyToMany
	@JoinTable(name = "FORUMPOST_TAGS")
	protected List<Tag> tags = new ArrayList<Tag>();
	@Transient
	protected String tagsString;
	@ManyToMany
	@JoinTable(name = "FORUMPOST_SUBSCRIBERS")
	protected List<User> subscribers = new ArrayList<User>();

	public ForumPost() {
		this.topic = "";
	}

	public ForumPost(String topic) {
		this.topic = topic;
		this.details = new TextDetails();
	}

	public ForumPostConfig buildConfig() {
		ForumPostConfig config = buildSummaryConfig();
		if (this.details != null) {
			config.details = this.details.getDetails();
			config.detailsText = getTextDetails();
		}
		if (!this.replies.isEmpty()) {
			config.replies = new ArrayList<ForumPostConfig>();
			for (ForumPost reply : this.replies) {
				ForumPostConfig replyConfig = reply.buildSummaryConfig();
				config.replies.add(replyConfig);
			}
		}
		return config;
	}

	public ForumPostConfig buildSummaryConfig() {
		ForumPostConfig config = new ForumPostConfig();
		config.id = String.valueOf(this.id);
		if (this.forum != null) {
			config.forum = String.valueOf(this.forum.getId());
		}
		if (this.parent != null) {
			config.parent = String.valueOf(this.parent.getId());
		}
		config.topic = this.topic;
		config.summary = this.summary;
		config.tags = getTagsString();
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
		config.views = String.valueOf(this.views);
		config.dailyViews = String.valueOf(this.dailyViews);
		config.weeklyViews = String.valueOf(this.weeklyViews);
		config.monthlyViews = String.valueOf(this.monthlyViews);
		return config;
	}
	
	@Override
	public String getTypeName() {
		return "Post";
	}
	
	@Override
	public boolean checkProfanity() {
		super.checkProfanity();
		return Utils.checkProfanity(this.topic) || Utils.checkProfanity(this.details.getDetails());
	}

	@Override
	public void checkConstraints() {
		super.checkConstraints();
		if (this.topic.length() >= 1024) {
			throw new BotException("Text size limit exceeded");
		}
		Utils.checkHTML(this.topic);
		this.topic = Utils.sanitize(this.topic);
	}
	
	public List<User> getSubscribers() {
		return subscribers;
	}

	public void setSubscribers(List<User> subscribers) {
		this.subscribers = subscribers;
	}

	public ForumPost getParent() {
		return parent;
	}

	public void setParent(ForumPost parent) {
		this.parent = parent;
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

	public Forum getForum() {
		return forum;
	}

	public void setForum(Forum forum) {
		this.forum = forum;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public ForumPost clone() {
		ForumPost post = (ForumPost)super.clone();
		if (post.details != null) {
			post.details = post.details.clone();
		}
		return post;
	}

	public TextDetails getDetails() {
		return details;
	}

	public String printReplies(ForumPostBean forumPostBean, SessionProxyBean proxy) {
		StringWriter writer = new StringWriter();
		writer.write("<table cellspacing=\"5\"><tr>");
		int row = 0;
		for (ForumPost instance : getReplies()) {
			row++;
			if (row >= 2) {
				writer.write("</tr><tr>");
				row = 1;
			}
			writer.write("<td align=\"left\" valign=\"top\" class=\"user-thumb\">");
			writer.write("<a href=\"login?view-user=");
			writer.write(forumPostBean.encodeURI(instance.getCreator().getUserId()));
			writer.write(proxy.proxyString());
			writer.write("\">");
			writer.write("<img src=\"" + forumPostBean.getAvatarThumb(instance));
			writer.write("\" class=\"user-thumb\"/><br/></a>");
			writer.write("</td><td><table style=\"border-style:solid;border-color:grey;border-width:1px\">");
			writer.write("<tr><td height=\"40\" width=\"800\" align=\"left\" valign=\"top\">");
			writer.write("<a style=\"text-decoration:none;\" href=\"forum-post?id=");
			writer.write(String.valueOf(instance.getId()));
			writer.write(proxy.proxyString());
			writer.write("\">");
			writer.write("<span class='menu' style='font-weight:bold'>");
			writer.write("by ");
			writer.write(instance.getCreator().getUserHTML());
			writer.write(" posted ");
			writer.write(Utils.displayTimestamp(instance.getCreationDate()));
			writer.write("</span><br/>");
			if (instance.isFlagged()) {
				writer.write("<span style=\"color:red;font-weight:bold;\">This reply is flagged.</span><br/>");
			} else {
				writer.write(instance.getTextDetails());
				writer.write("<br/>");
			}
			writer.write("<span class=\"menu\">");
			if (instance.getUpdatedDate() != null) {
				writer.write("Updated: " + Utils.displayTimestamp(instance.getUpdatedDate()) + "<br/>");
			}
			writer.write("Thumbs up: " + instance.getThumbsUp() + ", thumbs down: " + instance.getThumbsDown() + ", stars: " + Utils.truncate(instance.getStars()) + "<br/>");

			writer.write("Views: " + instance.getViews() + ", today: " + instance.getDailyViews() + ", week: " + instance.getWeeklyViews() + ", month: " + instance.getMonthlyViews() + "<br/>");
			writer.write("</span></a>");
			if (!instance.getReplies().isEmpty()) {
				writer.write(instance.printReplies(forumPostBean, proxy));
			}
			writer.write("</td></tr></table></td>");
		}
		writer.write("</tr></table>");
		return writer.toString();
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

	public void addReply(ForumPost reply) {
		reply.setParent(this);
		getReplies().add(reply);
		this.replyCount++;
	}

	public void removeReply(ForumPost reply) {
		getReplies().remove(reply);
		this.replyCount--;
	}

	public List<ForumPost> getReplies() {
		return replies;
	}

	public void setReplies(List<ForumPost> replies) {
		this.replies = replies;
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

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public boolean isFeatured() {
		return isFeatured;
	}

	public void setFeatured(boolean isFeatured) {
		this.isFeatured = isFeatured;
	}

	public int getMonthlyViews() {
		return monthlyViews;
	}

	public void setMonthlyViews(int monthlyViews) {
		this.monthlyViews = monthlyViews;
	}

	public int getReplyCount() {
		return replyCount;
	}

	public void setReplyCount(int replyCount) {
		this.replyCount = replyCount;
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
	
	@Override
	public void preDelete(EntityManager em) {
		if (this.parent != null) {
			this.parent.removeReply(this);
		}
	}

	public String toString() {
		return getClass().getSimpleName() + "(" + this.topic + ")";
	}
}

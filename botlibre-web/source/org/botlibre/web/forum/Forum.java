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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;

import org.botlibre.BotException;

import org.botlibre.web.admin.AccessMode;
import org.botlibre.web.admin.BotMode;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.User;
import org.botlibre.web.admin.WebMedium;
import org.botlibre.web.rest.ForumConfig;
import org.botlibre.web.rest.WebMediumConfig;

@Entity
@AssociationOverrides({
	@AssociationOverride(name="admins", joinTable=@JoinTable(name="FORUM_ADMINS")),
	@AssociationOverride(name="users", joinTable=@JoinTable(name="FORUM_USERS")),
	@AssociationOverride(name="tags", joinTable=@JoinTable(name="FORUM_TAGS")),
	@AssociationOverride(name="categories", joinTable=@JoinTable(name="FORUM_CATEGORIES")),
	@AssociationOverride(name="errors", joinTable=@JoinTable(name="FORUM_ERRORS"))
})
public class Forum extends WebMedium {
	protected AccessMode postAccessMode = AccessMode.Everyone;
	protected AccessMode replyAccessMode = AccessMode.Everyone;
	protected int posts;
	protected int weeklyPosts;
	@OneToOne(fetch=FetchType.LAZY)
	protected BotInstance bot;
	protected BotMode botMode = BotMode.AnswerAndListen;
	@ManyToMany
	@JoinTable(name = "FORUM_SUBSCRIBERS")
	protected List<User> subscribers = new ArrayList<User>();
	
	public Forum() {
	}

	public Forum(String name) {
		super(name);
	}
	
	public WebMediumConfig buildBrowseConfig() {
		ForumConfig config = new ForumConfig();
		toBrowseConfig(config);
		config.posts = String.valueOf(this.posts);
		return config;
	}

	public ForumConfig buildConfig() {
		ForumConfig config = new ForumConfig();
		toConfig(config);
		config.posts = String.valueOf(this.posts);
		if (this.postAccessMode != null) {
			config.postAccessMode = getPostAccessMode().name();
		}
		if (this.replyAccessMode != null) {
			config.replyAccessMode = getReplyAccessMode().name();
		}
		return config;
	}

	public String getForwarderAddress() {
		return "/forum?id=" + getId();
	}

	public void checkPostAccess(User user) {
		if (!this.isPrivate && (this.postAccessMode == null) || (this.postAccessMode == AccessMode.Everyone)) {
			return;
		} else if (user == null) {
			throw new BotException("This " + getDisplayName() + " does not allow anonymous posting.");
		} else if (this.isPrivate || (this.postAccessMode == AccessMode.Members)) {
			if (!isUser(user) && !isAdmin(user)) {
				throw new BotException("You must be a member to post to this " + getDisplayName() + ".");				
			}
		} else if (this.postAccessMode == AccessMode.Administrators) {
			if (!isAdmin(user)) {
				throw new BotException("You must be an administrator to post to this " + getDisplayName() + ".");				
			}
		}
	}

	public void checkReplyAccess(User user) {
		if (!this.isPrivate && (this.replyAccessMode == null) || (this.replyAccessMode == AccessMode.Everyone)) {
			return;
		} else if (user == null) {
			throw new BotException("This " + getDisplayName() + " does not allow anonymous replies.");
		} else if (this.isPrivate || (this.replyAccessMode == AccessMode.Members)) {
			if (!isUser(user) && !isAdmin(user)) {
				throw new BotException("You must be a member to reply to this " + getDisplayName() + ".");				
			}
		} else if (this.replyAccessMode == AccessMode.Administrators) {
			if (!isAdmin(user)) {
				throw new BotException("You must be an administrator to reply to this " + getDisplayName() + ".");				
			}
		}
	}

	public boolean isPostAllowed(User user) {
		if (!this.isPrivate && (this.postAccessMode == null) || (this.postAccessMode == AccessMode.Everyone)) {
			return true;
		} else if (user == null) {
			return false;
		} else if (this.isPrivate || (this.postAccessMode == AccessMode.Members)) {
			if (!isUser(user) && !isAdmin(user)) {
				return false;			
			}
		} else if (this.postAccessMode == AccessMode.Administrators) {
			if (!isAdmin(user)) {
				return false;			
			}
		}
		return true;
	}

	public boolean isReplyAllowed(User user) {
		if (!this.isPrivate && (this.replyAccessMode == null) || (this.replyAccessMode == AccessMode.Everyone)) {
			return true;
		} else if (user == null) {
			return false;
		} else if (this.isPrivate || (this.replyAccessMode == AccessMode.Members)) {
			if (!isUser(user) && !isAdmin(user)) {
				return false;			
			}
		} else if (this.replyAccessMode == AccessMode.Administrators) {
			if (!isAdmin(user)) {
				return false;		
			}
		}
		return true;
	}
	
	public List<User> getSubscribers() {
		return subscribers;
	}

	public void setSubscribers(List<User> subscribers) {
		this.subscribers = subscribers;
	}

	public int getWeeklyPosts() {
		return weeklyPosts;
	}

	public void setWeeklyPosts(int weeklyPosts) {
		this.weeklyPosts = weeklyPosts;
	}

	public AccessMode getPostAccessMode() {
		if (postAccessMode == null) {
			return AccessMode.Everyone;
		}
		return postAccessMode;
	}

	public void setPostAccessMode(AccessMode postAccessMode) {
		this.postAccessMode = postAccessMode;
	}

	public AccessMode getReplyAccessMode() {
		if (replyAccessMode == null) {
			return AccessMode.Everyone;
		}
		return replyAccessMode;
	}

	public void setReplyAccessMode(AccessMode replyAccessMode) {
		this.replyAccessMode = replyAccessMode;
	}

	public boolean hasBot() {
		return this.bot != null;
	}
	
	public BotInstance getBot() {
		return bot;
	}

	public void setBot(BotInstance bot) {
		this.bot = bot;
	}

	public BotMode getBotMode() {
		if (this.botMode == null) {
			this.botMode = BotMode.AnswerAndListen;
		}
		return botMode;
	}

	public void setBotMode(BotMode botMode) {
		this.botMode = botMode;
	}

	public void incrementPosts() {
		this.posts = this.posts + 1;
		this.weeklyPosts = this.weeklyPosts + 1;
	}

	public int getPosts() {
		return posts;
	}

	public void setPosts(int posts) {
		this.posts = posts;
	}
	
	@Override
	public void preDelete(EntityManager em) {
		super.preDelete(em);
		Query query = em.createQuery("Delete from ForumPost p where p.forum = :forum");
		query.setParameter("forum", detach());
		query.executeUpdate();
		query = em.createQuery("Delete from ForumAttachment p where p.forum = :forum");
		query.setParameter("forum", detach());
		query.executeUpdate();
	}
	
}

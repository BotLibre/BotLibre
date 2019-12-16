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

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.botlibre.BotException;
import org.botlibre.util.Utils;

@Entity
public class UserMessage implements Cloneable {
	@Id
	@GeneratedValue
	protected Long id;
	@Temporal(TemporalType.TIMESTAMP)
	protected Date creationDate;
	@Column(length=1024)
	protected String subject = "";
	@Column(length=1024)
	protected String message = "";
	@OneToOne(fetch=FetchType.LAZY)
	protected User owner;
	@OneToOne(fetch=FetchType.LAZY)
	protected User creator;
	@OneToOne(fetch=FetchType.LAZY)
	protected User target;
	@OneToOne(fetch=FetchType.LAZY)
	protected UserMessage parent;
	
	public UserMessage() {
	}
	
	public UserMessage clone() {
		try {
			return (UserMessage)super.clone();
		} catch (CloneNotSupportedException ignore) {
			throw new InternalError(ignore.getMessage());
		}
	}
	
	public boolean checkProfanity() {
		return Utils.checkProfanity(this.subject) || Utils.checkProfanity(this.message);
	}
	
	public void checkConstraints() {
		if ((this.subject != null) && (this.subject.length() >= 1024)) {
			throw new BotException("Text size limit exceeded");
		}
		if ((this.message != null) && (this.message.length() >= 1024)) {
			throw new BotException("Text size limit exceeded");
		}
		Utils.checkHTML(this.subject);
		Utils.checkScript(this.message);
		this.subject = Utils.sanitize(this.subject);
		this.message = Utils.sanitize(this.message);
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getCreationDateString() {
		return Utils.printTimestamp(new java.sql.Timestamp(this.creationDate.getTime()));
	}

	public Date getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getMessageText() {
		return Utils.formatHTMLOutput(getMessage());
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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

	public String getTargetId() {
		if (this.target == null) {
			return "";
		}
		return this.target.getUserId();
	}

	public User getTarget() {
		return target;
	}

	public void setTarget(User target) {
		this.target = target;
	}

	public UserMessage getParent() {
		return parent;
	}

	public void setParent(UserMessage parent) {
		this.parent = parent;
	}

	public String toString() {
		return getClass().getSimpleName() + "(" + this.id + ")";
	}

	public UserMessage detach() {
		try {
			UserMessage detched = getClass().newInstance();
			detched.setId(getId());
			return detched;
		} catch (Exception ignore) {
			return this;
		}
	}
	
	public String getTypeName() {
		return getClass().getSimpleName();
	}
	
	@SuppressWarnings("unchecked")
	public void preDelete(EntityManager em) {
		Query query = em.createQuery("Select m from UserMessage m where m.parent = :parent");
		query.setParameter("parent", detach());
		List<UserMessage> children = query.getResultList();
		for (UserMessage child : children) {
			child.setParent(null);
		}		
	}
}

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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.botlibre.BotException;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

@Entity
public class Category implements Cloneable {
	@Id
	@GeneratedValue
	protected long id;
	protected String name;
	protected String type;
	protected int count;
	protected boolean secured;
	@Column(length=1024)
	protected String description;
	@Temporal(TemporalType.TIMESTAMP)
	protected Date creationDate;
	@OneToOne(fetch=FetchType.LAZY)
	protected User creator;
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.LAZY)
	protected AvatarImage avatar;
	@OneToOne(fetch=FetchType.LAZY)
	protected Domain domain;
	@ManyToMany
	@JoinTable(name = "CATEGORY_PARENTS")
	protected List<Category> parents = new ArrayList<Category>();
	@ManyToMany(mappedBy="parents")
	protected List<Category> children = new ArrayList<Category>();

	public Category() { }
	
	public Category(String name) {
		this.name = name;
	}
	
	public boolean isSecured() {
		return secured;
	}

	public void setSecured(boolean secured) {
		this.secured = secured;
	}

	public void checkConstraints() {
		if ((this.name.length() >= 255) || (this.description.length() >= 1024)) {
			throw new BotException("Text size limit exceeded");
		}
		Utils.checkHTML(this.name);
		Utils.checkHTML(this.description);
		this.name = Utils.sanitize(this.name);
		this.description = Utils.sanitize(this.description);
	}
	
	public Category clone() {
		try {
			return (Category)super.clone();
		} catch (CloneNotSupportedException ignore) {
			throw new InternalError(ignore.getMessage());
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public AvatarImage getAvatar() {
		return avatar;
	}

	public void setAvatar(AvatarImage avatar) {
		this.avatar = avatar;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAncestor(Category category) {
		if (this.equals(category)) {
			return true;
		}
		for (Category parent : getParents()) {
			if (parent.isAncestor(category)) {
				return true;
			}
		}
		return false;
	}

	public void addAncestors(List<Category> set) {
		if (set.contains(this)) {
			return;
		}
		set.add(this);
		for (Category parent : getParents()) {
			parent.addAncestors(set);
		}
	}

	public List<Category> getParents() {
		return parents;
	}

	public void setParents(List<Category> parents) {
		this.parents = parents;
	}

	public List<Category> getChildren() {
		return children;
	}

	public void setChildren(List<Category> children) {
		this.children = children;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getParentsString() {
		if (this.parents.isEmpty()) {
			return "";
		}
		StringWriter writer = new StringWriter();
		int count = 0;
		for (Category parent : this.parents) {
			writer.write(parent.getName());
			count++;
			if (count < this.parents.size()) {
				writer.write(", ");
			}
		}
		return writer.toString();
	}

	@SuppressWarnings("unchecked")
	public void setParentsFromString(String csv, EntityManager em) {
		if (csv.length() == 0) {
			this.parents = new ArrayList<Category>();
			return;
		}
		List<Category> newParents = new ArrayList<Category>();
		TextStream stream = new TextStream(csv);
		stream.skipWhitespace();
		while (!stream.atEnd()) {
			String word = stream.upTo(',');
			if (!stream.atEnd()) {
				stream.skip();
				stream.skipWhitespace();
			}
			if (word.trim().equals("")) {
				continue;
			}
			List<Category> results = em
					.createQuery("Select t from Category t where t.name = :name and t.type = :type and t.domain = :domain")
					.setParameter("type", getType())
					.setParameter("name", word)
					.setParameter("domain", getDomain())
					.getResultList();
			Category category = null;
			if (results.isEmpty()) {
				throw new BotException("Category does not exist - " + word);
			} else {
				category = results.get(0);
			}
			if (!newParents.contains(category)) {
				if (category.isAncestor(this)) {
					throw new BotException("Category cycle - " + word);
				}
				newParents.add(category);
			}
		}
		for (Category parent : this.parents) {
			if (!newParents.contains(parent)) {
				parent.getChildren().remove(this);
			}
		}
		this.parents = newParents;
		for (Category parent : newParents) {
			if (!parent.getChildren().contains(this)) {
				parent.getChildren().add(this);
			}
		}
	}
	
}

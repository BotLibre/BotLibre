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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.rest.DomainConfig;
import org.botlibre.web.rest.WebMediumConfig;

@Entity
@AssociationOverrides({
	@AssociationOverride(name="admins", joinTable=@JoinTable(name="DOMAIN_ADMINS")),
	@AssociationOverride(name="users", joinTable=@JoinTable(name="DOMAIN_USERS")),
	@AssociationOverride(name="tags", joinTable=@JoinTable(name="DOMAIN_TAGS")),
	@AssociationOverride(name="categories", joinTable=@JoinTable(name="DOMAIN_CATEGORIES")),
	@AssociationOverride(name="errors", joinTable=@JoinTable(name="DOMAIN_ERRORS"))
})
public class Domain extends WebMedium {
	protected AccessMode creationMode = AccessMode.Everyone;
	public enum AccountType { Trial, Basic, Premium, Professional, Enterprise, EnterprisePlus, Corporate, Dedicated, Private }
	protected AccountType accountType;
	@Temporal(TemporalType.TIMESTAMP)
	protected Date paymentDate;
	protected int paymentDuration;
	protected boolean subscription;
	protected boolean isActive;
	protected boolean isVerified;
	protected String billingAddress = "";
	protected String billingEmail = "";
	protected String billingName = "";
	@ManyToMany
	@JoinTable(name = "DOMAIN_PAYMENTS")
	protected List<Payment> payments = new ArrayList<Payment>();
	
	public Domain() {
	}

	public Domain(String name) {
		super(name);
	}
	
	public boolean isSubscription() {
		return subscription;
	}

	public void setSubscription(boolean subscription) {
		this.subscription = subscription;
	}

	@Override
	public String getDisplayName() {
		return "Workspace";
	}
	
	public String getForwarderAddress() {
		return "/domain?domain=" + getId();
	}
	
	public WebMediumConfig buildBrowseConfig() {
		DomainConfig config = new DomainConfig();
		toBrowseConfig(config);
		return config;
	}

	public DomainConfig buildConfig() {
		DomainConfig config = new DomainConfig();
		toConfig(config);
		if (this.creationMode != null) {
			config.creationMode = this.creationMode.name();
		}
		if (this.accountType != null) {
			config.accountType = this.accountType.name();
		}
		return config;
	}

	public AccessMode getCreationMode() {
		return creationMode;
	}

	public void setCreationMode(AccessMode creationMode) {
		this.creationMode = creationMode;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}

	public Date getPaymentDate() {
		return paymentDate;
	}

	public Date getPaymentExpiryDate() {
		if (this.paymentDate == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		if (isSubscription()) {
			calendar.add(Calendar.MONTH, 1);
		} else {
			calendar.setTime(this.paymentDate);
			calendar.add(Calendar.MONTH, this.paymentDuration);
		}
		return new Date(calendar.getTimeInMillis());
	}
		
	public boolean isExpired() {
		if (!Site.COMMERCIAL || (Site.DEDICATED && !Site.CLOUD)) {
			return false;
		}
		if (isActive() && isSubscription()) {
			return false;
		}
		Date expiry = getPaymentExpiryDate();
		if (expiry == null) {
			return true;
		}
		return expiry.getTime() < System.currentTimeMillis();
	}

	public boolean isAdmin(User user) {
		return (user != null) && (user.isSuperUser() || user.isAdminUser() || getAdmins().contains(user));
	}
	
	public void checkExpired() {
		if (isExpired()) {
			throw new BotException("This account has expired, please make a payment to re-activate it");
		}
	}

	public void checkCreation(User user) {
		if (Site.READONLY) {
			throw new BotException("This website is currently ungoing maintence, please try later.");
		}
		checkAccess(user);
		if (!this.isPrivate && (this.creationMode == null) || (this.creationMode == AccessMode.Everyone)) {
			return;
		} else if (user == null) {
			throw new BotException("This " + getDisplayName() + " does not allow anonymous content creation.");
		} else if (this.isPrivate || (this.creationMode == AccessMode.Members)) {
			if (!isUser(user) && !isAdmin(user)) {
				throw new BotException("You must be a member to create content in this " + getDisplayName() + ".");				
			}
		} else if (this.creationMode == AccessMode.Administrators) {
			if (!isAdmin(user)) {
				throw new BotException("You must be an administrator to create content this " + getDisplayName() + ".");				
			}
		}
	}

	public boolean isCreationAllowed(User user) {
		if (!isAllowed(user)) {
			return false;
		}
		if (!this.isPrivate && (this.creationMode == null) || (this.creationMode == AccessMode.Everyone)) {
			return true;
		} else if (user == null) {
			return false;
		} else if (this.isPrivate || (this.creationMode == AccessMode.Members)) {
			if (!isUser(user) && !isAdmin(user)) {
				return false;
			}
		} else if (this.creationMode == AccessMode.Administrators) {
			if (!isAdmin(user)) {
				return false;
			}
		}
		return true;
	}

	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}

	public int getPaymentDuration() {
		return paymentDuration;
	}

	public void setPaymentDuration(int paymentDuration) {
		this.paymentDuration = paymentDuration;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isVerified() {
		return isVerified;
	}

	public void setVerified(boolean isVerified) {
		this.isVerified = isVerified;
	}

	public String getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(String billingAddress) {
		this.billingAddress = billingAddress;
	}

	public String getBillingEmail() {
		return billingEmail;
	}

	public void setBillingEmail(String billingEmail) {
		this.billingEmail = billingEmail;
	}

	public String getBillingName() {
		return billingName;
	}

	public void setBillingName(String billingName) {
		this.billingName = billingName;
	}

	public List<Payment> getPayments() {
		return payments;
	}

	public void setPayments(List<Payment> payments) {
		this.payments = payments;
	}
	
	@Override
	public void preDelete(EntityManager em) {
		super.preDelete(em);
		Query query = em.createQuery("Delete from Tag p where p.domain = :domain");
		query.setParameter("domain", detach());
		query.executeUpdate();
		query = em.createQuery("Delete from Category p where p.domain = :domain");
		query.setParameter("domain", detach());
		query.executeUpdate();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setCategoriesFromString(String csv, EntityManager em) {
		if (csv == null) {
			throw new BotException("You must choose at least one category");			
		}
		List<Category> newCategories = new ArrayList<Category>();
		for (String word : Utils.csv(csv)) {
			List<Category> results = em
					.createQuery("Select t from Category t where t.name = :name and t.type = :type")
					.setParameter("type", getTypeName())
					.setParameter("name", word)
					.getResultList();
			Category category = null;
			if (results.isEmpty()) {
				throw new BotException("Category " + word + " does not exist");
			} else {
				category = results.get(0);
			}
			if (category.isSecured() && getCreator() != null && !(getCreator().isAdminUser() || getCreator().equals(category.getCreator()))) {
				throw new BotException("Only admins can use this category - " + word);
			}
			if (!newCategories.contains(category)) {
				newCategories.add(category);
			}
		}
		if (newCategories.isEmpty() && !(this instanceof Domain)) {
			throw new BotException("You must choose at least one category");			
		}
		List<Category> ancestors = new ArrayList<Category>();
		for (Category category : newCategories) {
			category.addAncestors(ancestors);
		}
		this.categories = ancestors;
		this.categoriesString = null;
	}
}

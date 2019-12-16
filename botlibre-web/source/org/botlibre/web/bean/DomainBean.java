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
package org.botlibre.web.bean;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AccessMode;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.AvatarImage;
import org.botlibre.web.admin.Category;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.admin.Domain.AccountType;
import org.botlibre.web.admin.Payment;
import org.botlibre.web.admin.Payment.PaymentStatus;
import org.botlibre.web.admin.Tag;
import org.botlibre.web.admin.User;
import org.botlibre.web.admin.WebMedium;
import org.botlibre.web.rest.DomainConfig;
import org.botlibre.web.rest.UpgradeConfig;
import org.botlibre.web.service.EmailService;

public class DomainBean extends WebMediumBean<Domain> {
	protected Domain wizardDomain;
	protected Payment payment;
	protected WizardState wizardState;
	
	public enum WizardState { User, Domain, Payment, Confirm, Complete }
	
	public DomainBean() {
	}

	@Override
	public String getPostAction() {
		return "domain";
	}
	
	public String getPaymentAmount() {
		double amount = 0;
		int months = this.payment.getPaymentDuration();
		if (months == 12) {
			months = 10;
		} else if (months == 24) {
			months = 18;
		}
		if (this.payment.getAccountType() == AccountType.Basic) {
			amount = months * 4.99;
		} else if (this.payment.getAccountType() == AccountType.Premium) {
			amount = months * 19.99;
		} else if (this.payment.getAccountType() == AccountType.Professional) {
			amount = months * 19.99;
		} else if (this.payment.getAccountType() == AccountType.Enterprise) {
			amount = months * 49.99;
		} else if (this.payment.getAccountType() == AccountType.EnterprisePlus) {
			amount = months * 99.99;
		}
		return String.format("%.02f", amount);
	}
	
	public String getPaymentDuration() {
		if (this.payment.getPaymentDuration() == 1) {
			return "1 month";
		}
		return "" + this.payment.getPaymentDuration() + " months";
	}
	
	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	}

	public Domain getWizardDomain() {
		return wizardDomain;
	}

	public void setWizardDomain(Domain wizardDomain) {
		this.wizardDomain = wizardDomain;
	}

	public WizardState getWizardState() {
		return wizardState;
	}

	public void setWizardState(WizardState wizardState) {
		this.wizardState = wizardState;
	}

	public List<Category> getAllCategories() {
		return this.loginBean.getAllCategories(null);
	}

	public String getAllCategoriesString() {
		StringWriter writer = new StringWriter();
		int count = 1;
		List<Category> categories = getAllDomainCategories();
		for (Category category : categories) {
			writer.write("\"");
			writer.write(category.getName());
			writer.write("\"");
			if (count < categories.size())
			writer.write(", ");
			count++;
		}
		return writer.toString();
	}

	public List<Category> getAllDomainCategories() {
		try {
			return AdminDatabase.instance().getAllCategories(getTypeName(), null);
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Category>();
		}
	}

	public String getAllTagsString() {
		StringWriter writer = new StringWriter();
		int count = 1;
		List<Tag> tags = getAllDomainTags();
		for (Tag tag : tags) {
			writer.write("\"");
			writer.write(tag.getName());
			writer.write("\"");
			if (count < tags.size())
			writer.write(", ");
			count++;
		}
		return writer.toString();
	}

	public List<Tag> getAllDomainTags() {
		try {
			return AdminDatabase.instance().getTags(getTypeName(), null);
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Tag>();
		}
	}

	public List<Tag> getAllTags() {
		try {
			return AdminDatabase.instance().getTags(null, getDomain());
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Tag>();
		}
	}

	@Override
	public String getEmbeddedBanner() {
		return "domain-banner.jsp";
	}

	@Override
	public boolean hasValidInstance() {
		return this.instance != null && this.instance.getId() != null && !Site.DOMAIN.equals(this.instance.getAlias());
	}

	public String getDomainSelected(String type) {
		Domain domain = getDomain();
		if (hasValidInstance()) {
			domain = getInstance();
		}
		if (Site.DOMAIN.equals(domain.getAlias())) {
			if (Site.ID.equals(type)) {
				return "selected=\"selected\"";
			} else {
				return "";
			}
		}
		if ((domain != null) && domain.getId().toString().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}
	
	public String domainInput() {
		Domain domain = getDomain();
		if (hasValidInstance()) {
			domain = getInstance();
		}
		if (domain == null || Site.DOMAIN.equals(domain.getAlias())) {
			return "";
		}
		return "<input name=\"domain\" type=\"hidden\" value=\"" + domain.getId() + "\"/>";
	}
	
	public String domainURL() {
		Domain domain = getDomain();
		if (hasValidInstance()) {
			domain = getInstance();
		}
		if (domain == null || Site.DOMAIN.equals(domain.getAlias())) {
			return "";
		}
		return "&domain=" + domain.getId();
	}
	
	public List<Domain> getUserInstances() {
		try {
			List<Domain> results = AdminDatabase.instance().getUserDomains(getUser());			
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Domain>();
		}
	}
	
	public Long getSelectedDomainId() {
		Domain domain = getDomain();
		Long id = domain.getId();
		boolean first = true;
		for (Domain instance : getUserInstances()) {
			if (first) {
				id = instance.getId();
				first = false;
			}
			if (instance.equals(domain)) {
				id = domain.getId();
				break;
			}
		}
		return id;
	}
	
	public List<Domain> getAllInstances(Domain domain) {
		return getAllSearchInstances();
	}		

	@Override
	public List<Domain> getAllSearchInstances() {
		try {
			List<Domain> results = AdminDatabase.instance().getAllDomains(this.page, this.pageSize, this.categoryFilter, this.nameFilter, this.userFilter, 
					this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, getUser());
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (results.size() < this.pageSize) {
					this.resultsSize = results.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllDomainsCount(this.categoryFilter, this.nameFilter, this.userFilter, 
							this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, getUser());
				}
			}
			if (this.instanceRestrict == InstanceRestrict.Active) {
				List<Domain> filtered =  new ArrayList<Domain>();
				for (Domain domain : results) {
					if (!domain.isExpired()) {
						filtered.add(domain);
					}
				}
				results = filtered;
			} else if (this.instanceRestrict == InstanceRestrict.Expired) {
				List<Domain> filtered =  new ArrayList<Domain>();
				for (Domain domain : results) {
					if (domain.isExpired()) {
						filtered.add(domain);
					}
				}
				results = filtered;
			}
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Domain>();
		}
	}
	
	public List<Domain> getPublicInstances() {
		try {
			List<Domain> results = AdminDatabase.instance().getAllDomains();			
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Domain>();
		}
	}

	@Override
	public String getAvatarThumb(AvatarImage avatar) {
		String file = super.getAvatarThumb(avatar);
		if (file.equals("images/bot-thumb.jpg")) {
			return "images/domain-thumb.jpg";
		}
		return file;
	}

	@Override
	public String getAvatarImage(AvatarImage avatar) {
		String file = super.getAvatarImage(avatar);
		if (file.equals("images/bot.png")) {
			return "images/domain.png";
		}
		return file;
	}

	public boolean removeCategory(String id) {
		try {
			checkLogin();
			checkInstance();
			checkAdminOrSuper();
			if ((id == null) || id.isEmpty()) {
				throw new BotException("Please select a category");
			}
			AdminDatabase.instance().deleteCategory(Long.valueOf(id));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean removeTag(String id) {
		try {
			checkLogin();
			checkInstance();
			checkAdminOrSuper();
			if ((id == null) || id.isEmpty()) {
				throw new BotException("Please select a tag");
			}
			AdminDatabase.instance().deleteTag(Long.valueOf(id));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	public void upgradeDomain(UpgradeConfig upgrade) {
		if (!isLoggedIn()) {
			throw new BotException("You must sign in first");
		}
		//if (getUser().getSource().equals("ios") && !upgrade.type.equals("AppleItunes")) {
		//	throw new BotException("iOS users can only upgrade from the iOS app");
		//}
		Payment payment = new Payment();
		payment.setAccountType(AccountType.valueOf(upgrade.userType));
		payment.setPaymentDate(new Date());
		payment.setPaymentDuration(1);
		payment.setToken(upgrade.token);
		payment.setUserId(getUser().getUserId());
		payment.setPaypalCc(upgrade.sku);
		payment.setPaypalSt(upgrade.orderToken);
		payment.setPaypalTx(upgrade.orderId);
		payment.setStatus(PaymentStatus.WaitingForPayment);
		payment = AdminDatabase.instance().createPayment(payment);
		
		if (upgrade.secret == null || !String.valueOf((Long.valueOf(upgrade.secret) - getUserId().length())).equals(Site.UPGRADE_SECRET)) {
			AdminDatabase.instance().log(Level.WARNING, "Upgrade failed: authorization", upgrade.orderId, upgrade.secret, getUserId(), Long.valueOf(upgrade.secret) - getUserId().length());
			throw new BotException("Upgrade failed authorization");
		}
		if (upgrade.type != null && upgrade.type.equals("GooglePlay") && (upgrade.orderId == null || !upgrade.orderId.startsWith("GPA"))) {
			AdminDatabase.instance().log(Level.WARNING, "Upgrade failed: suspicious transaction", upgrade.orderId);
			throw new BotException("This transaction seems suspicious, if your Google Play account is charged, please contact billing@botlibre.com");
		}
		if (upgrade.type != null && upgrade.type.equals("AppleItunes") && (upgrade.orderId == null || (upgrade.orderId.contains("-") || upgrade.orderId.contains(".")))) {
			AdminDatabase.instance().log(Level.WARNING, "Upgrade failed: suspicious transaction", upgrade.orderId);
			throw new BotException("This transaction seems suspicious, if your Apple account is charged, please contact billing@botlibre.com");
		}
		
		try {
			Domain domain = (Domain)getInstance().clone();
			if (payment.getAccountType() != AccountType.Trial) {
				if (domain.isExpired() || getWizardDomain().getAccountType() != payment.getAccountType()) {
					domain.setAccountType(payment.getAccountType());
					domain.setPaymentDate(payment.getPaymentDate());
					domain.setPaymentDuration(payment.getPaymentDuration());
				} else {
					domain.setPaymentDuration(domain.getPaymentDuration() + payment.getPaymentDuration());
				}
			} else {
				domain.setAccountType(payment.getAccountType());
				domain.setPaymentDate(payment.getPaymentDate());
				domain.setPaymentDuration(payment.getPaymentDuration());
			}
			domain.setSubscription(true);
			domain.setActive(true);
			domain = AdminDatabase.instance().updateDomain(domain, domain.getTagsString(), domain.getCategoriesString());
			
			payment.setStatus(PaymentStatus.Complete);
			this.instance = AdminDatabase.instance().updatePayment(this.instance, payment);
		} catch (Exception exception) {
			error(exception);
		}
	}

	public boolean wizard(String userId, String password, String password2, String dateOfBirth, String hint, String name, String ip, String email, String credentialsType, String credentialsUserID, String credentialsToken,
			String instance, String description, boolean isPrivate, boolean isHidden, String accessMode, String creationMode,
			String accountType, String duration,
			String tx, String st, String amt, String cc, String token) {
		try {
			if (!isLoggedIn() || (this.wizardState == WizardState.User)) {
				this.wizardState = WizardState.User;
				boolean success = this.loginBean.createUser(userId, password, password2, dateOfBirth, hint, name, ip, "web", "", "Private", email, "", "", false, Site.ADULT,
						credentialsType, credentialsUserID, credentialsToken, true);
				if (success) {
					this.wizardState = WizardState.Domain;
				}
				return false;
			} else if ((this.wizardState == null) || (this.wizardState == WizardState.Domain)) {
				this.wizardState = WizardState.Domain;
				Domain newInstance = new Domain(instance);
				newInstance.setDescription(description);
				newInstance.setPrivate(isPrivate);
				newInstance.setHidden(isHidden);
				newInstance.setAccessMode(AccessMode.valueOf(accessMode));
				newInstance.setCreationMode(AccessMode.valueOf(creationMode));
				setInstance(newInstance);
				setWizardDomain(newInstance);
				if (instance.equals("")) {
					throw new BotException("Invalid name");
				}
				AdminDatabase.instance().validateNewDomain(newInstance.getAlias(), description, "", Site.ADULT);
				this.wizardState = WizardState.Payment;
			} else if (this.wizardState == WizardState.Payment) {
				Date date = new Date();
				AccountType type = AccountType.valueOf(accountType);
				int value = 1;
				if (type != AccountType.Trial) {
					value = Integer.valueOf(duration);
				}
				if ((getWizardDomain().getId() != null) && (type == AccountType.Trial)) {
					throw new BotException("Free trial accounts must upgrade to paid accounts to renew");
				}
				Payment payment = new Payment();
				payment.setAccountType(type);
				payment.setPaymentDate(date);
				payment.setPaymentDuration(value);
				payment.setToken(String.valueOf(Math.abs(Utils.random().nextLong())));
				payment.setUserId(getUser().getUserId());
				payment.setStatus(PaymentStatus.WaitingForPayment);
				AdminDatabase.instance().createPayment(payment);
				setPayment(payment);

				Domain  domain = null;
				if (getWizardDomain().getId() == null) {
					getWizardDomain().setAccountType(AccountType.Trial);
					getWizardDomain().setPaymentDate(getPayment().getPaymentDate());
					getWizardDomain().setPaymentDuration(1);
					
					domain = AdminDatabase.instance().createDomain(getWizardDomain(), getUser().getUserId(), "", "", this.loginBean);

					Category category = new Category();
					category.setName("Misc");
					category.setDescription("Bots that have not been categorized");
					category.setType("Bot");
					category.setDomain(domain);
					AdminDatabase.instance().createCategory(category, getUser(), "");
					
					category = new Category();
					category.setName("Misc");
					category.setDescription("Forums that have not been categorized");
					category.setType("Forum");
					category.setDomain(domain);
					AdminDatabase.instance().createCategory(category, getUser(), "");
					
					category = new Category();
					category.setName("Misc");
					category.setDescription("Channels that have not been categorized");
					category.setType("Channel");
					category.setDomain(domain);
					AdminDatabase.instance().createCategory(category, getUser(), "");
					
					category = new Category();
					category.setName("Misc");
					category.setDescription("Scripts that have not been categorized");
					category.setType("Script");
					category.setDomain(domain);
					AdminDatabase.instance().createCategory(category, getUser(), "");
					
					category = new Category();
					category.setName("Misc");
					category.setDescription("Avatars that have not been categorized");
					category.setType("Avatar");
					category.setDomain(domain);
					AdminDatabase.instance().createCategory(category, getUser(), "");
					
					category = new Category();
					category.setName("Misc");
					category.setDescription("Graphics that have not been categorized");
					category.setType("Graphic");
					category.setDomain(domain);
					AdminDatabase.instance().createCategory(category, getUser(), "");
					
					category = new Category();
					category.setName("Misc");
					category.setDescription("Issue trackers that have not been categorized");
					category.setType("IssueTracker");
					category.setDomain(domain);
					AdminDatabase.instance().createCategory(category, getUser(), "");
					
					category = new Category();
					category.setName("Misc");
					category.setDescription("Analytics that have not been categorized");
					category.setType("Analytic");
					category.setDomain(domain);
					AdminDatabase.instance().createCategory(category, getUser(), "");
				}
				
				this.wizardState = WizardState.Confirm;
			} else if (this.wizardState == WizardState.Confirm) {
				if (!getUser().isSuperUser() && !getUser().isAdminUser() && (getWizardDomain().getId() != null && getPayment().getAccountType() != AccountType.Trial)) {
					if (tx == null) {
						throw new BotException("Must return from Paypal with verified payment, missing transaction id, please contact sales if you made a payment");
					}
					if (!getPayment().getToken().equals(token)) {
						throw new BotException("Payment token is missing or incorrect, please contact sales if you made a payment");
					}
					getPayment().setPaypalTx(tx);
					getPayment().setPaypalAmt(amt);
					getPayment().setPaypalSt(st);
					getPayment().setPaypalCc(cc);
				}
				if (getPayment() == null) {
					throw new BotException("Missing payment, please contact sales if you made a payment");
				}
				Domain  domain = null;
				domain = (Domain)getInstance().clone();
				if (getPayment().getAccountType() != AccountType.Trial) {
					if (domain.isExpired() || getWizardDomain().getAccountType() != getPayment().getAccountType()) {
						domain.setAccountType(getPayment().getAccountType());
						domain.setPaymentDate(getPayment().getPaymentDate());
						domain.setPaymentDuration(getPayment().getPaymentDuration());
					} else {
						domain.setPaymentDuration(domain.getPaymentDuration() + getPayment().getPaymentDuration());
					}
				} else {
					domain.setAccountType(getPayment().getAccountType());
					domain.setPaymentDate(getPayment().getPaymentDate());
					domain.setPaymentDuration(getPayment().getPaymentDuration());
				}
				domain.setActive(true);
				domain = AdminDatabase.instance().updateDomain(domain, domain.getTagsString(), domain.getCategoriesString());
				
				getPayment().setStatus(PaymentStatus.Complete);
				domain = AdminDatabase.instance().updatePayment(domain, getPayment());
				setInstance(domain);
				
				this.payment = null;
				this.wizardDomain = null;
				this.wizardState = WizardState.Complete;

				try {
					StringWriter writer = new StringWriter();
					writer.write("Hello " + domain.getCreator().getUserId()
							+ ",<p>\n\nThank you for subscribing to " + Site.NAME
							+ ".");
					writer.write("\n<p>");
					writer.write("If you need any help creating or integrating your bots please contact us at support@" + Site.EMAILHOST);
					
					writer.write("\n<p><br/><hr>"
							+ "\n<p>This is an automated email from " + Site.NAME + " - <a href=\"" + Site.SECUREURLLINK + "\">" + Site.SECUREURLLINK + "</a>."
							+ "\n<p>You can update your email preferences from your <a href=\"" + Site.SECUREURLLINK + "/login?sign-in=true\">profile page</a> (click edit).");
	
					EmailService.instance().sendEmail(domain.getCreator().getEmail(), "Thank you for subscribing to " + Site.NAME, null, writer.toString());
				} catch (Exception exception) {
					AdminDatabase.instance().log(exception);
				}
				return true;
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return false;
	}
	
	public boolean allowSubdomain() {
		return true;
	}

	public boolean createInstance(DomainConfig config) {
		try {
			Domain newInstance = new Domain(config.name);
			if (Site.COMMERCIAL && !isSuper()) {
				if (Site.DEDICATED) {
					throw new BotException("Only admin users can create workspaces.");
				} else {
					// Free trial
					//throw new BotException("You must create an account to create a workspace");
					Payment payment = new Payment();
					payment.setAccountType(AccountType.Trial);
					payment.setPaymentDate(new Date());
					payment.setPaymentDuration(1);
					payment.setToken(String.valueOf(Math.abs(Utils.random().nextLong())));
					payment.setUserId(getUser().getUserId());
					payment.setStatus(PaymentStatus.Complete);
					AdminDatabase.instance().createPayment(payment);
					
					newInstance.setAccountType(payment.getAccountType());
					newInstance.setPaymentDate(payment.getPaymentDate());
					newInstance.setPaymentDuration(payment.getPaymentDuration());
				}
			}
			if (Site.READONLY) {
				throw new BotException("This website is currently undergoing maintence, please try later.");
			}
			checkLogin();
			config.sanitize();
			setInstance(newInstance);
			updateFromConfig(newInstance, config);
			checkVerfied(config);
			if (config.creationMode != null) {
				newInstance.setCreationMode(AccessMode.valueOf(config.creationMode));
			}
			setSubdomain(config.subdomain, newInstance);
			//AdminDatabase.instance().validateNewDomain(newInstance.getAlias(), config.description, config.tags, config.isAdult);
			setInstance(AdminDatabase.instance().createDomain(newInstance, getUser().getUserId(), config.tags, config.categories, this.loginBean));

			Category category = new Category();
			category.setName("Misc");
			category.setDescription("Bots that have not been categorized");
			category.setType("Bot");
			category.setDomain(getInstance());
			AdminDatabase.instance().createCategory(category, getUser(), "");
			
			category = new Category();
			category.setName("Misc");
			category.setDescription("Forums that have not been categorized");
			category.setType("Forum");
			category.setDomain(getInstance());
			AdminDatabase.instance().createCategory(category, getUser(), "");
			
			category = new Category();
			category.setName("Misc");
			category.setDescription("Issue trackers that have not been categorized");
			category.setType("IssueTracker");
			category.setDomain(getInstance());
			AdminDatabase.instance().createCategory(category, getUser(), "");
			
			category = new Category();
			category.setName("Misc");
			category.setDescription("Channels that have not been categorized");
			category.setType("Channel");
			category.setDomain(getInstance());
			AdminDatabase.instance().createCategory(category, getUser(), "");
			
			category = new Category();
			category.setName("Misc");
			category.setDescription("Scripts that have not been categorized");
			category.setType("Script");
			category.setDomain(getInstance());
			AdminDatabase.instance().createCategory(category, getUser(), "");
			
			category = new Category();
			category.setName("Misc");
			category.setDescription("Avatars that have not been categorized");
			category.setType("Avatar");
			category.setDomain(getInstance());
			AdminDatabase.instance().createCategory(category, getUser(), "");
			
			category = new Category();
			category.setName("Misc");
			category.setDescription("Graphics that have not been categorized");
			category.setType("Graphic");
			category.setDomain(getInstance());
			AdminDatabase.instance().createCategory(category, getUser(), "");
			
			category = new Category();
			category.setName("Misc");
			category.setDescription("Analytics that have not been categorized");
			category.setType("Analytic");
			category.setDomain(getInstance());
			AdminDatabase.instance().createCategory(category, getUser(), "");
			
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean deleteInstance(boolean confirm) {
		try {
			checkInstance();
			if (!confirm) {
				throw new BotException("Must check 'I'm sure'");
			}
			if (this.loginBean.validateUser(getUser().getUserId(), getUser().getPassword(), getUser().getToken(), false, false) == 0) {
				return false;
			}
			Domain instance = (Domain)AdminDatabase.instance().validate(this.instance.getClass(), this.instance.getId(), getUser().getUserId());
			checkAdminOrSuper();
			List instances = AdminDatabase.instance().getAllAvatars(instance);
			if (!instances.isEmpty()) {
				if (isSuper()) {
					for (WebMedium content : (List<WebMedium>)instances) {
						AdminDatabase.instance().delete(content);
					}
				} else {
					throw new BotException("Workspace content must be deleted first. Workspace still has avatars.");
				}
			}
			instances = AdminDatabase.instance().getAllInstances(instance);
			if (!instances.isEmpty()) {
				if (isSuper()) {
					for (WebMedium content : (List<WebMedium>)instances) {
						AdminDatabase.instance().delete(content);
					}
				} else {
					throw new BotException("Workspace content must be deleted first. Workspace still has bots.");
				}
			}
			instances = AdminDatabase.instance().getAllForums(instance);
			if (!instances.isEmpty()) {
				if (isSuper()) {
					for (WebMedium content : (List<WebMedium>)instances) {
						AdminDatabase.instance().delete(content);
					}
				} else {
					throw new BotException("Workspace content must be deleted first. Workspace still has forums.");
				}
			}
			instances = AdminDatabase.instance().getAllChannels(instance);
			if (!instances.isEmpty()) {
				if (isSuper()) {
					for (WebMedium content : (List<WebMedium>)instances) {
						AdminDatabase.instance().delete(content);
					}
				} else {
					throw new BotException("Workspace content must be deleted first. Workspace still has channels.");
				}
			}
			instances = AdminDatabase.instance().getAllScripts(instance);
			if (!instances.isEmpty()) {
				if (isSuper()) {
					for (WebMedium content : (List<WebMedium>)instances) {
						AdminDatabase.instance().delete(content);
					}
				} else {
					throw new BotException("Workspace content must be deleted first. Workspace still has scripts.");
				}
			}
			instances = AdminDatabase.instance().getAllGraphics(instance);
			if (!instances.isEmpty()) {
				if (isSuper()) {
					for (WebMedium content : (List<WebMedium>)instances) {
						AdminDatabase.instance().delete(content);
					}
				} else {
					throw new BotException("Workspace content must be deleted first. Workspace still has graphics.");
				}
			}
			AdminDatabase.instance().delete(instance);
			this.instance = null;
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
		
	}

	public boolean updateDomain(DomainConfig config, boolean featured, boolean subscription) {
		try {
			checkLogin();
			checkInstance();
			checkAdminOrSuper();
			config.sanitize();
			Domain newInstance = (Domain)this.instance.clone();
			this.editInstance = newInstance;
			updateFromConfig(newInstance, config);
			if (config.creator != null && isSuperUser()) {
				User user = AdminDatabase.instance().validateUser(config.creator);
				newInstance.setCreator(user);
			}
			if (config.creationMode != null) {
				newInstance.setCreationMode(AccessMode.valueOf(config.creationMode));
			}
			if (isSuper()) {
				newInstance.setFeatured(featured);
				if (Site.COMMERCIAL) {
					newInstance.setSubscription(subscription);
				}
			}
			setSubdomain(config.subdomain, newInstance);
			setInstance(AdminDatabase.instance().updateDomain(newInstance, config.tags, config.categories));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public String isCreationModeSelected(String type) {
		AccessMode mode = AccessMode.Everyone;
		if (getEditInstance() != null) {
			mode = getEditInstance().getCreationMode();
		}
		if ((mode != null) && mode.name().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}
	
	@Override
	public Class<Domain> getType() {
		return Domain.class;
	}
	
	@Override
	public String getTypeName() {
		return "Domain";
	}
	
	@Override
	public String getDisplayName() {
		return "Workspace";
	}

	@Override
	public String getCreateURL() {
		return "create-domain.jsp";
	}

	@Override
	public String getSearchURL() {
		return "domains.jsp";
	}

	@Override
	public String getBrowseURL() {
		return "domains.jsp";
	}
	
	@Override
	public void writeRestrictOptions(StringWriter writer) {
		if (isSuper()) {
			writer.write("<option value='Expired' " + getInstanceRestrictCheckedString(InstanceRestrict.Expired) + ">expired</option>\n");
			writer.write("<option value='Active' " + getInstanceRestrictCheckedString(InstanceRestrict.Active) + ">active</option>\n");
			writer.write("<option value='Basic' " + getInstanceRestrictCheckedString(InstanceRestrict.Basic) + ">Basic</option>\n");
			writer.write("<option value='Premium' " + getInstanceRestrictCheckedString(InstanceRestrict.Premium) + ">Premium</option>\n");
			writer.write("<option value='Professional' " + getInstanceRestrictCheckedString(InstanceRestrict.Professional) + ">Professional</option>\n");
			writer.write("<option value='Enterprise' " + getInstanceRestrictCheckedString(InstanceRestrict.Enterprise) + ">Enterprise</option>\n");
		}
	}
	
	@Override
	public void writeInfoTabExtra2HTML(SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			if (Site.COMMERCIAL && (!Site.DEDICATED || Site.CLOUD) && (isAdmin() || isSuper())) {
				out.write("<br/>\n");
				out.write(loginBean.translate("Account Type"));
				out.write(": ");
				out.write(Site.getPaymentType(getDisplayInstance().getAccountType()));
				out.write("<br/>\n");
				out.write(loginBean.translate("Active"));
				out.write(": ");
				out.write(String.valueOf(getDisplayInstance().isActive()));
				out.write("<br/>\n");
				out.write(loginBean.translate("Active Date"));
				out.write(": ");
				out.write(String.valueOf(getDisplayInstance().getPaymentDate()));
				out.write("<br/>\n");
				out.write(loginBean.translate("Payment Months"));
				out.write(": ");
				out.write(String.valueOf(getDisplayInstance().getPaymentDuration()));
				out.write("<br/>\n");
				out.write(loginBean.translate("Expiry Date"));
				out.write(": ");
				out.write(String.valueOf(getDisplayInstance().getPaymentExpiryDate()));
				out.write("<br/>\n");
				out.write(loginBean.translate("Subscription"));
				out.write(": ");
				out.write(String.valueOf(getDisplayInstance().isSubscription()));
				out.write("<br/>\n");
				out.write(loginBean.translate("Verified"));
				out.write(": ");
				out.write(String.valueOf(getDisplayInstance().isVerified()));
				out.write("<br/>\n");
				out.write(loginBean.translate("Payments"));
				out.write(":");
				for (Payment payment : getDisplayInstance().getPayments()) {
					out.write("<br/>\n");
					out.write(String.valueOf(payment.getPaymentDate()));
					out.write(" : ");
					out.write(String.valueOf(payment.getAccountType()));
					out.write(" - ");
					out.write(String.valueOf(payment.getPaymentDuration()));
					out.write(" - ");
					out.write(loginBean.translate("months"));
					out.write(" - ");
					out.write(payment.getUserId());
				}
				out.write("<form action='domain' method='post' class='message'>\n");
				out.write(proxy.proxyInput());
				out.write(instanceInput());
				out.write("<input type='submit' name='makePayment' value='");
				out.write(loginBean.translate("Make Payment"));
				out.write("' title='Make a payment to upgrade or extend account'>");
				out.write("</form>\n");
				out.write("<br/>\n");
			}
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void writeBrowseThumb(StringWriter writer, Domain instance, boolean grid) {
		if (Site.COMMERCIAL && instance.isExpired() && !instance.isAdmin(getUser())) {
			return;
		}
		super.writeBrowseThumb(writer, instance, grid);
	}
	
}

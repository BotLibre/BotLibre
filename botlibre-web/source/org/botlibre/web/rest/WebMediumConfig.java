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
package org.botlibre.web.rest;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.botlibre.util.Utils;

import org.botlibre.web.admin.AccessMode;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.WebMediumBean;

/**
 * DTO for XML web medium config.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class WebMediumConfig extends Config {
	@XmlAttribute
	public String id;
	@XmlAttribute
	public String name;
	@XmlAttribute
	public String alias;
	@XmlAttribute
	public boolean isAdult;
	@XmlAttribute
	public boolean isPrivate;
	@XmlAttribute
	public boolean isHidden;
	@XmlAttribute
	public String accessMode;
	@XmlAttribute
	public String forkAccessMode;
	@XmlAttribute
	public String contentRating;
	@XmlAttribute
	public boolean isFlagged;
	@XmlAttribute
	public boolean isReviewed;
	@XmlAttribute
	public boolean isFeatured;
	@XmlAttribute
	public boolean isAdmin;
	@XmlAttribute
	public boolean showAds = true;
	@XmlAttribute
	public boolean isExternal;
	@XmlAttribute
	public boolean isPaphus;
	@XmlAttribute
	public String creator;
	@XmlAttribute
	public String creationDate;
	@XmlAttribute
	public String connects;
	@XmlAttribute
	public String dailyConnects;
	@XmlAttribute
	public String weeklyConnects;
	@XmlAttribute
	public String monthlyConnects;
	@XmlAttribute
	public String thumbsUp;
	@XmlAttribute
	public String thumbsDown;
	@XmlAttribute
	public String stars;
	
	public String website;
	public String subdomain;
	public String description;
	public String details;
	public String disclaimer;
	public String categories;
	public String tags;
	public String flaggedReason;
	public String reviewRejectionComments;
	public String lastConnectedUser;
	public String license;
	public String avatar;
	public String adCode;

	public String getCategories() {
		if (this.categories == null || this.categories.isEmpty()) {
			return "Misc";
		}
		return this.categories;
	}
	
	public String getAccessMode() {
		if (this.accessMode == null || this.accessMode.isEmpty()) {
			return AccessMode.Everyone.name();
		}
		return this.accessMode;
	}
	
	@SuppressWarnings("rawtypes")
	public abstract WebMediumBean getBean(LoginBean loginBean);

	@SuppressWarnings("rawtypes")
	public WebMediumBean validate(LoginBean loginBean, HttpServletRequest requestContext) throws Throwable {
		connect(loginBean, requestContext);
		WebMediumBean bean = getBean(loginBean);
		if ((this.id == null) || (this.id.isEmpty())) {
			bean.validateInstance(this.name);
		} else {
			bean.validateInstance(this.id);
		}
		if (loginBean.getError() != null) {
			throw loginBean.getError();
		}
		return bean;
	}
	
	public void sanitize() {
		super.sanitize();
		id = Utils.sanitize(id);
		name = Utils.sanitize(name);
		alias = Utils.sanitize(alias);
		accessMode = Utils.sanitize(accessMode);
		forkAccessMode = Utils.sanitize(forkAccessMode);
		contentRating = Utils.sanitize(contentRating);
		creator = Utils.sanitize(creator);
		creationDate = Utils.sanitize(creationDate);
		connects = Utils.sanitize(connects);
		dailyConnects = Utils.sanitize(dailyConnects);
		weeklyConnects = Utils.sanitize(weeklyConnects);
		monthlyConnects = Utils.sanitize(monthlyConnects);
		thumbsUp = Utils.sanitize(thumbsUp);
		thumbsDown = Utils.sanitize(thumbsDown);
		stars = Utils.sanitize(stars);
		
		website = Utils.sanitize(website);
		subdomain = Utils.sanitize(subdomain);
		description = Utils.sanitize(description);
		details = Utils.sanitize(details);
		disclaimer = Utils.sanitize(disclaimer);
		categories = Utils.sanitize(categories);
		tags = Utils.sanitize(tags);
		reviewRejectionComments = Utils.sanitize(reviewRejectionComments);
		flaggedReason = Utils.sanitize(flaggedReason);
		lastConnectedUser = Utils.sanitize(lastConnectedUser);
		license = Utils.sanitize(license);
		avatar = Utils.sanitize(avatar);
		adCode = Utils.sanitize(adCode);
	}
}

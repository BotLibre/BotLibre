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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.eclipse.persistence.annotations.Index;

import org.botlibre.web.Site;
import org.botlibre.web.bean.LoginBean;

/**
 * Allow a domain or subdomain to be forwarded to a page.
 */
@Entity
public class DomainForwarder {
	@Id
	@GeneratedValue
	protected Long id;
	@Index
	protected String webMediumType;
	@Index
	protected Long webMediumId;
	@Index
	protected String domain;
	@Index
	protected String subdomain;
	@Column(length=1024)
	protected String forwarderAddress;
	@OneToOne(fetch=FetchType.LAZY)
	protected User creator;
	
	public DomainForwarder() {
		
	}
	
	public DomainForwarder(WebMedium medium) {
		this.webMediumId = medium.getId();
		this.webMediumType = medium.getTypeName();
		this.creator = medium.getCreator();
		this.forwarderAddress = medium.getForwarderAddress();
	}
	
	public void init(WebMedium medium, LoginBean bean) {
		this.webMediumId = medium.getId();
		this.webMediumType = medium.getTypeName();
		this.creator = medium.getCreator();
		this.forwarderAddress = medium.getForwarderAddress() + "&application=" + bean.getUser().getApplicationId();
	}

	public String getForwarderAddress() {
		return forwarderAddress;
	}

	public void setForwarderAddress(String forwarderAddress) {
		this.forwarderAddress = forwarderAddress;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getWebMediumType() {
		return webMediumType;
	}

	public void setWebMediumType(String webMediumType) {
		this.webMediumType = webMediumType;
	}

	public Long getWebMediumId() {
		return webMediumId;
	}

	public void setWebMediumId(Long webMediumId) {
		this.webMediumId = webMediumId;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

	public String getServerName() {
		if (this.domain != null && !this.domain.isEmpty()) {
			return this.domain;
		}
		if (this.subdomain == null || this.subdomain.isEmpty()) {
			return "";
		}
		return subdomain + "." + Site.SERVER_NAME;
	}

	public String getURL() {
		if (Site.HTTPS_WILDCARD && (this.domain == null || this.domain.isEmpty())) {
			return "https://" + getServerName() + Site.URL_SUFFIX;
		} else {
			return "http://" + getServerName() + Site.URL_SUFFIX;
		}
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	public DomainForwarder clone() {
		try {
			return (DomainForwarder)super.clone();
		} catch (CloneNotSupportedException ignore) {
			throw new InternalError(ignore.getMessage());
		}
	}
	
}

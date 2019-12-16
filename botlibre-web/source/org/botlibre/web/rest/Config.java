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

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.bean.DomainBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.service.AppIDStats;
import org.botlibre.web.service.IPStats;
import org.botlibre.web.service.Stats;

/**
 * DTO for XML config.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {
	@XmlAttribute
	public String application;
	@XmlAttribute
	public String domain;
	@XmlAttribute
	public String user;
	@XmlAttribute
	public String password;
	@XmlAttribute
	public String token;
	@XmlAttribute
	public String instance;
	
	public Config() {
		
	}
	
	public Config(JSONConfig json) {
		application = json.application;
		domain = json.domain;
		user = json.user;
		password = json.password;
		token = json.token;
		instance = json.instance;
	}
	
	
	public void clearCredentials() {
		application = null;
		user = null;
		password = null;
		token = null;
		instance = null;
		domain = null;
	}
	
	public long getToken() {
		long token = 0;
		if ((this.token != null) && (!this.token.equals(""))) {
			try {
				token = Long.valueOf(this.token);
			} catch (Exception exception) {
				throw new BotException("Invalid sign in token, please sign out and back in");
			}
		}
		return token;
	}

	public void validateApplication(LoginBean loginBean, HttpServletRequest requestContext) throws Throwable {
		Stats.stats.apiCalls++;
		IPStats ipStat = IPStats.api(requestContext);
		loginBean.setApplicationId(this.application);
		String appUser = AdminDatabase.instance().validateApplicationId(this.application, ipStat);
		loginBean.setAppUser(appUser);
		if (appUser != null) {
			AppIDStats stat = AppIDStats.getStats(this.application, appUser);
			AppIDStats.checkMaxAPI(stat, appUser, null, null);
			stat.apiCalls++;
		}
	}

	public void connect(LoginBean loginBean, HttpServletRequest requestContext) throws Throwable {
		validateApplication(loginBean, requestContext);
		if ((this.user != null) && !this.user.equals("")) {
			loginBean.connect(this.user, this.password, this.getToken());
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
		}
		checkDomain(loginBean);
	}

	public void checkDomain(LoginBean loginBean) throws Throwable {
		if ((this.domain != null) && !this.domain.equals("")) {
			DomainBean bean = loginBean.getBean(DomainBean.class);
			bean.validateInstance(this.domain);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
		}
	}
	
	public void sanitize() {
		application = Utils.sanitize(application);
		domain = Utils.sanitize(domain);
		user = Utils.sanitize(user);
		instance = Utils.sanitize(instance);
	}
}

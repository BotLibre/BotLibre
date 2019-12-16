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
import javax.xml.bind.annotation.XmlRootElement;

import org.botlibre.BotException;

import org.botlibre.web.admin.ClientType;
import org.botlibre.web.bean.IssueBean;
import org.botlibre.web.bean.IssueTrackerBean;
import org.botlibre.web.bean.LoginBean;

/**
 * DTO for XML issue config.
 */
@XmlRootElement(name="issue")
@XmlAccessorType(XmlAccessType.FIELD)
public class IssueConfig extends Config {
	@XmlAttribute
	public String id;
	@XmlAttribute
	public String tracker;
	@XmlAttribute
	public String issueType;
	@XmlAttribute
	public String priority;
	@XmlAttribute
	public String status;
	@XmlAttribute
	public boolean isPriority;
	@XmlAttribute
	public boolean isHidden;
	@XmlAttribute
	public boolean isFlagged;
	@XmlAttribute
	public boolean isAdmin;
	@XmlAttribute
	public String creationDate;
	@XmlAttribute
	public String creator;
	@XmlAttribute
	public boolean subscribe;
	@XmlAttribute
	public String thumbsUp;
	@XmlAttribute
	public String thumbsDown;
	@XmlAttribute
	public String stars;
	
	public String title;
	public String summary;
	public String details;
	public String detailsText;
	public String tags;
	public String flaggedReason;
	public String avatar;

	public IssueBean validate(LoginBean loginBean, HttpServletRequest requestContext) throws Throwable {
		connect(loginBean, requestContext);
		IssueBean bean = loginBean.getBean(IssueBean.class);
		IssueTrackerBean forumBean = loginBean.getBean(IssueTrackerBean.class);
		bean.setIssueTrackerBean(forumBean);
		if ((this.id == null) || (this.id.isEmpty())) {
			throw new BotException("Missing issue id");
		}
		bean.validateInstance(this.id, ClientType.REST);	
		if (loginBean.getError() != null) {
			throw loginBean.getError();
		}
		return bean;
	}
}

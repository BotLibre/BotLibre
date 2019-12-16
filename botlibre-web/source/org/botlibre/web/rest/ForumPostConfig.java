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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.botlibre.BotException;

import org.botlibre.web.admin.ClientType;
import org.botlibre.web.bean.ForumBean;
import org.botlibre.web.bean.ForumPostBean;
import org.botlibre.web.bean.LoginBean;

/**
 * DTO for XML post config.
 */
@XmlRootElement(name="forum-post")
@XmlAccessorType(XmlAccessType.FIELD)
public class ForumPostConfig extends Config {
	@XmlAttribute
	public String id;
	@XmlAttribute
	public String forum;
	@XmlAttribute
	public String parent;
	@XmlAttribute
	public boolean isFlagged;
	@XmlAttribute
	public boolean isFeatured;
	@XmlAttribute
	public boolean isAdmin;
	@XmlAttribute
	public String replyCount;
	@XmlAttribute
	public String creationDate;
	@XmlAttribute
	public String creator;
	@XmlAttribute
	public String views;
	@XmlAttribute
	public String dailyViews;
	@XmlAttribute
	public String weeklyViews;
	@XmlAttribute
	public String monthlyViews;
	@XmlAttribute
	public boolean subscribe;
	@XmlAttribute
	public String thumbsUp;
	@XmlAttribute
	public String thumbsDown;
	@XmlAttribute
	public String stars;
	
	public String topic;
	public String summary;
	public String details;
	public String detailsText;
	public String tags;
	public String flaggedReason;
	public String avatar;
	
	public List<ForumPostConfig> replies;

	public ForumPostBean validate(LoginBean loginBean, HttpServletRequest requestContext) throws Throwable {
		connect(loginBean, requestContext);
		ForumPostBean bean = loginBean.getBean(ForumPostBean.class);
		ForumBean forumBean = loginBean.getBean(ForumBean.class);
		bean.setForumBean(forumBean);
		if ((this.id == null) || (this.id.isEmpty())) {
			throw new BotException("Missing post id");
		}
		bean.validateInstance(this.id, ClientType.REST);	
		if (loginBean.getError() != null) {
			throw loginBean.getError();
		}
		return bean;
	}
}

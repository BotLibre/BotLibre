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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.botlibre.util.Utils;

import org.botlibre.web.admin.AccessMode;
import org.botlibre.web.bean.IssueTrackerBean;
import org.botlibre.web.bean.LoginBean;

/**
 * DTO for XML IssueTracker config.
 */
@XmlRootElement(name="issuetracker")
@XmlAccessorType(XmlAccessType.FIELD)
public class IssueTrackerConfig extends WebMediumConfig {
	@XmlAttribute
	public String createAccessMode;
	@XmlAttribute
	public String issues;
	
	public String getCreateAccessMode() {
		if (this.createAccessMode == null || this.createAccessMode.isEmpty()) {
			return AccessMode.Everyone.name();
		}
		return this.createAccessMode;
	}
	
	public IssueTrackerBean getBean(LoginBean loginBean) {
		return loginBean.getBean(IssueTrackerBean.class);
	}
	
	public void sanitize() {
		createAccessMode = Utils.sanitize(createAccessMode);
		issues = Utils.sanitize(issues);
	}
}

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

import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.BotBean;

/**
 * DTO for XML instance config.
 */
@XmlRootElement(name="instance")
@XmlAccessorType(XmlAccessType.FIELD)
public class InstanceConfig extends WebMediumConfig {
	@XmlAttribute
	public String size;
	@XmlAttribute
	public Boolean enableLearning;
	@XmlAttribute
	public Boolean enableComprehension;
	@XmlAttribute
	public boolean allowForking;
	@XmlAttribute
	public boolean isArchived;
	@XmlAttribute
	public boolean hasAPI;
	@XmlAttribute
	public String wins;
	@XmlAttribute
	public String losses;
	@XmlAttribute
	public String rank;
	@XmlAttribute
	public String instanceAvatar;
	public String admin;
	public String template;
	@XmlAttribute
	public String channelType;
	
	public BotBean getBean(LoginBean loginBean) {
		return loginBean.getBotBean();
	}
	
	public void sanitize() {
		size = Utils.sanitize(size);
		wins = Utils.sanitize(wins);
		losses = Utils.sanitize(losses);
		rank = Utils.sanitize(rank);
		instanceAvatar = Utils.sanitize(instanceAvatar);
		admin = Utils.sanitize(admin);
		template = Utils.sanitize(template);
		channelType = Utils.sanitize(channelType);
	}
}

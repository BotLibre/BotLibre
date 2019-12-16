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

import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.LoginBean;

/**
 * DTO for XML response search options.
 */
@XmlRootElement(name="response-search")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResponseSearchConfig extends Config {
	@XmlAttribute
	public String responseType;
	@XmlAttribute
	public String inputType;
	@XmlAttribute
	public String filter;
	@XmlAttribute
	public String duration;
	@XmlAttribute
	public String restrict;
	@XmlAttribute
	public String page;
	
	public BotBean validate(LoginBean loginBean, HttpServletRequest requestContext) throws Throwable {
		connect(loginBean, requestContext);
		BotBean bean = loginBean.getBotBean();
		bean.validateInstance(this.instance);
		if (loginBean.getError() != null) {
			throw loginBean.getError();
		}
		return bean;
	}
}

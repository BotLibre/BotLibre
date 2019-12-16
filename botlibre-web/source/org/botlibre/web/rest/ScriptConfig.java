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
import org.botlibre.web.bean.ScriptBean;

/**
 * DTO for XML script config.
 */
@XmlRootElement(name="script")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScriptConfig extends WebMediumConfig {
	@XmlAttribute
	public String language;
	@XmlAttribute
	public String size;
	@XmlAttribute
	public String version;
	
	public ScriptBean getBean(LoginBean loginBean) {
		return loginBean.getBean(ScriptBean.class);
	}
	
	public void sanitize() {
		language = Utils.sanitize(language);
		size = Utils.sanitize(size);
		version = Utils.sanitize(version);
	}
}

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

/**
 * DTO for XML user upgrades.
 */
@XmlRootElement(name="upgrade")
@XmlAccessorType(XmlAccessType.FIELD)
public class UpgradeConfig extends Config {
	@XmlAttribute
	public String type;
	@XmlAttribute
	public String userType;
	@XmlAttribute
	public String orderId;
	@XmlAttribute
	public String orderToken;
	@XmlAttribute
	public String sku;
	@XmlAttribute
	public String secret;
	@XmlAttribute
	public String months;
	@XmlAttribute
	public boolean subscription;
	
	public UpgradeConfig() {
		
	}
}

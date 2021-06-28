/******************************************************************************
 *
 *  Copyright 2021 Paphus Solutions Inc.
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

import org.botlibre.web.Site;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;

/**
 * DTO for XML Braintree options.
 */

@XmlRootElement(name="braintree")
@XmlAccessorType(XmlAccessType.FIELD)
public class BraintreeConfig extends Config{
	@XmlAttribute
	public String customerId;
	@XmlAttribute 
	public String planId;
	@XmlAttribute 
	public String nonce;
	@XmlAttribute 
	public String subId;
	
    public static BraintreeGateway gateway = new BraintreeGateway(
            Environment.PRODUCTION,
            Site.BRAINTREE_MERCHANT_ID,
            Site.BRAINTREE_PUBLIC_KEY,
            Site.BRAINTREE_PRIVATE_KEY
    ); 
}

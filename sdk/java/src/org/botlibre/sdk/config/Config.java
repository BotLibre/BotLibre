/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
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

package org.botlibre.sdk.config;

import java.io.StringWriter;

import org.w3c.dom.Element;

import org.botlibre.sdk.SDKConnection;

/**
 * DTO for XML config.
 */
public class Config implements Cloneable {
	public String application;
	public String domain;
	public String user;
	public String token;
	public String instance;
	public String type;
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException exception) {
			throw new Error(exception);
		}
	}
	
	public void addCredentials(SDKConnection connection) {
		this.application = connection.getCredentials().getApplicationId();
		if (connection.getUser() != null) {
			this.user = connection.getUser().user;
			this.token = connection.getUser().token;
		}
		if (connection.getDomain() != null && this.domain == null) {
			this.domain = connection.getDomain().id;
		}
	}
	
	public String toXML() {
		return "<config/>";
	}
		
	public void parseXML(Element element) {
		this.application = element.getAttribute("application");
		this.domain = element.getAttribute("domain");
		this.user = element.getAttribute("user");
		this.token = element.getAttribute("token");
		this.instance = element.getAttribute("instance");
		this.type = element.getAttribute("type");
	}
	
	public void writeCredentials(StringWriter writer) {
		if (this.user != null && this.user.length() > 0) {
			writer.write(" user=\"" + this.user + "\"");
		}
		if (this.token != null && this.token.length() > 0) {
			writer.write(" token=\"" + this.token + "\"");
		}
		if (this.type != null && !this.type.equals("")) {
			writer.write(" type=\"" + this.type + "\"");
		}
		if (this.instance != null && !this.instance.equals("")) {
			writer.write(" instance=\"" + this.instance + "\"");
		}
		if (this.application != null && !this.application.equals("")) {
			writer.write(" application=\"" + this.application + "\"");
		}
		if (this.domain != null && !this.domain.equals("")) {
			writer.write(" domain=\"" + this.domain + "\"");
		}
	}
}
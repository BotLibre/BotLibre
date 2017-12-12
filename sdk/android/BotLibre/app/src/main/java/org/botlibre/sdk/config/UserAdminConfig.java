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


/**
 * DTO for XML user admin config.
 */
public class UserAdminConfig extends Config {	
	public String operation;
	public String operationUser;
	
	public void parseXML(Element element) {
		super.parseXML(element);
		
		this.operation = element.getAttribute("operation");
		this.operationUser = element.getAttribute("operationUser");
	}

	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<user-admin");
		writeCredentials(writer);
		
		if (this.operation != null) {
			writer.write(" operation=\"" + this.operation + "\"");
		}
		if (this.operationUser != null) {
			writer.write(" operationUser=\"" + this.operationUser + "\"");
		}
		
		writer.write("/>");
		return writer.toString();
	}
}
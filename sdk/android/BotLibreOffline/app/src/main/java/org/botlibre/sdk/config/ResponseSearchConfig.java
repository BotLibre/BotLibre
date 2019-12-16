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

/**
 * DTO for XML response search options.
 */
public class ResponseSearchConfig extends Config {
	public String responseType;
	public String inputType;
	public String filter;
	public String duration;
	public String restrict;
	public String page;
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<response-search");
		writeCredentials(writer);
		writer.write(" responseType=\"" + this.responseType + "\"");
		if (this.inputType != null) {
			writer.write(" inputType=\"" + this.inputType + "\"");
		}
		if (this.filter != null) {
			writer.write(" filter=\"" + this.filter + "\"");
		}
		if (this.duration != null) {
			writer.write(" duration=\"" + this.duration + "\"");
		}
		if (this.restrict != null) {
			writer.write(" restrict=\"" + this.restrict + "\"");
		}
		if ((this.page != null) && !this.page.equals("")) {
			writer.write(" page=\"" + this.page + "\"");
		}
		writer.write("/>");
		return writer.toString();
	}
}
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
 * DTO for XML browse options.
 */
public class BrowseConfig extends Config {
	public String type;
	public String typeFilter;
	public String category;
	public String tag;
	public String filter;
	public String userFilter;
	public String sort;
	public String restrict;
	public String page;
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<browse");
		writeCredentials(writer);
		writer.write(" type=\"" + this.type + "\"");
		if (this.typeFilter != null) {
			writer.write(" typeFilter=\"" + this.typeFilter + "\"");
		}
		if (this.userFilter != null) {
			writer.write(" userFilter=\"" + this.userFilter + "\"");
		}
		if (this.sort != null) {
			writer.write(" sort=\"" + this.sort + "\"");
		}
		if (this.restrict != null) {
			writer.write(" restrict=\"" + this.restrict + "\"");
		}
		if ((this.category != null) && !this.category.equals("")) {
			writer.write(" category=\"" + this.category + "\"");
		}
		if ((this.tag != null) && !this.tag.equals("")) {
			writer.write(" tag=\"" + this.tag + "\"");
		}
		if ((this.filter != null) && !this.filter.equals("")) {
			writer.write(" filter=\"" + this.filter + "\"");
		}
		if ((this.page != null) && !this.page.equals("")) {
			writer.write(" page=\"" + this.page + "\"");
		}
		writer.write("/>");
		return writer.toString();
	}
}
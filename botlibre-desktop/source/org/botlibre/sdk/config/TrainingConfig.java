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

import org.botlibre.sdk.util.Utils;


/**
 * DTO for XML training config.
 */
public class TrainingConfig extends Config {	
	public String operation;	
	public String question;	
	public String response;
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<training");
		writeCredentials(writer);
		
		if (this.operation != null) {
			writer.write(" operation=\"" + this.operation + "\"");
		}
		
		writer.write(">");

		if (this.question != null) {
			writer.write("<question>");
			writer.write(Utils.escapeHTML(this.question));
			writer.write("</question>");
		}
		if (this.response != null) {
			writer.write("<response>");
			writer.write(Utils.escapeHTML(this.response));
			writer.write("</response>");
		}
		
		writer.write("</training>");
		return writer.toString();
	}
}
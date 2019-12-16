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
package org.botlibre.web.admin;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class TextDetails implements Cloneable {
	@Id
	@GeneratedValue
	protected long id;
	
	@Lob
	protected String details;
	
	public TextDetails() { }
	
	public TextDetails clone() {
		try {
			return (TextDetails)super.clone();
		} catch (CloneNotSupportedException ignore) {
			throw new InternalError(ignore.getMessage());
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDetails() {
		return details;
	}

	public String getEditDetails() {
		if (this.details == null) {
			return "";
		}
		String text = this.details;
		text = text.replace("&", "&amp;");
		text = text.replace("<", "&lt;");
		text = text.replace(">", "&gt;");
		return text;
	}

	public void setDetails(String details) {
		this.details = details;
	}	
	
}

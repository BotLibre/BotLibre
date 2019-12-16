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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class ErrorMessage implements Cloneable {
	@Id
	@GeneratedValue
	protected Long id;
	@Temporal(TemporalType.TIMESTAMP)
	protected Date creationDate;
	@Column(length=1024)
	protected String message;
	
	public ErrorMessage() {
	}
	
	public ErrorMessage clone() {
		try {
			return (ErrorMessage)super.clone();
		} catch (CloneNotSupportedException ignore) {
			throw new InternalError(ignore.getMessage());
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		if (message != null && message.length() > 1020) {
			message = message.substring(0, 1020);
		}
		this.message = message;
	}
}

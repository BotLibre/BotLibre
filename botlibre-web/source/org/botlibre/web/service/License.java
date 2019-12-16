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
package org.botlibre.web.service;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.botlibre.util.Utils;

import org.botlibre.web.rest.LicenseConfig;

@Entity
public class License {

	@Id
	@GeneratedValue
	protected long id;

	@Column
	protected String client;

	@Column
	protected String status;

	@Column
	protected int cpus;

	@Column
	protected boolean sourceCode;

	@Column
	protected String licenseKey;

	@Column
	@Temporal(TemporalType.DATE)
	protected Date licenseDate;

	@Column
	@Temporal(TemporalType.DATE)
	protected Date expiryDate;
	
	public License() {
		
	}
	
	public License(LicenseConfig config) {
		if (config.id != null && !config.id.isEmpty()) {
			this.id = Integer.valueOf(config.id);
		}
		this.client = config.client;
		this.status = config.status;
		if (config.cpus != null && !config.cpus.isEmpty()) {
			this.cpus = Integer.valueOf(config.cpus);
		}
		this.sourceCode = config.sourceCode;
		this.licenseKey = config.licenseKey;
		if (config.licenseDate != null && !config.licenseDate.isEmpty()) {
			this.licenseDate = Utils.parseDate(config.licenseDate);
		}
		if (config.expiryDate != null && !config.expiryDate.isEmpty()) {
			this.expiryDate = Utils.parseDate(config.expiryDate);
		}
		this.licenseKey = config.licenseKey;
	}
	
	public LicenseConfig buildConfig() {
		LicenseConfig config = new LicenseConfig();
		config.id = String.valueOf(this.id);
		config.client = this.client;
		config.status = this.status;
		config.cpus = String.valueOf(this.cpus);
		config.sourceCode = this.sourceCode;
		if (this.licenseDate != null) {
			config.licenseDate = this.licenseDate.toString();
		}
		if (this.expiryDate != null) {
			config.expiryDate = this.expiryDate.toString();
		}
		config.licenseKey = this.licenseKey;
		return config;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLicenseKey() {
		return licenseKey;
	}

	public void setLicenseKey(String licenseKey) {
		this.licenseKey = licenseKey;
	}

	public Date getLicenseDate() {
		return licenseDate;
	}

	public void setLicenseDate(Date licenseDate) {
		this.licenseDate = licenseDate;
	}

	public int getCpus() {
		return cpus;
	}

	public void setCpus(int cpus) {
		this.cpus = cpus;
	}

	public boolean getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(boolean sourceCode) {
		this.sourceCode = sourceCode;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
	
}

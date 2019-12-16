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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.botlibre.util.Utils;
import org.botlibre.web.admin.Domain.AccountType;

@Entity
public class Payment {
	//tx=25G608537N677573S&st=Completed&amt=9.00&cc=USD&item_number=1&reqp=1&reqr=
	@Id
	@GeneratedValue
	protected long id;
	protected String paypalTx;
	protected String paypalSt;
	protected String paypalAmt;
	protected String paypalCc;
	protected String token;
	protected AccountType accountType;
	@Temporal(TemporalType.TIMESTAMP)
	protected Date paymentDate;
	protected int paymentDuration;
	protected String userId;
	protected Long domainId;
	protected PaymentStatus status;

	public enum PaymentStatus {WaitingForPayment, Complete}
	
	public Payment() { }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
	}

	public String getPaypalCc() {
		return paypalCc;
	}

	public void setPaypalCc(String paypalCc) {
		this.paypalCc = paypalCc;
	}

	public String getPaypalTx() {
		return paypalTx;
	}

	public void setPaypalTx(String paypalTx) {
		this.paypalTx = paypalTx;
	}

	public String getPaypalSt() {
		return paypalSt;
	}

	public void setPaypalSt(String paypalSt) {
		this.paypalSt = paypalSt;
	}

	public String getPaypalAmt() {
		return paypalAmt;
	}

	public void setPaypalAmt(String paypalAmt) {
		this.paypalAmt = paypalAmt;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}

	public Date getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}

	public int getPaymentDuration() {
		return paymentDuration;
	}

	public void setPaymentDuration(int paymentDuration) {
		this.paymentDuration = paymentDuration;
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	public String toString() {
		return getClass().getSimpleName() + ":" + getAccountType() + " - " + getPaymentDate() + " - " + getPaymentDuration();
	}
	
	public void checkConstraints() {
		this.paypalTx = Utils.sanitize(this.paypalTx);
		this.paypalSt = Utils.sanitize(this.paypalSt);
		this.paypalAmt = Utils.sanitize(this.paypalAmt);
		this.paypalCc = Utils.sanitize(this.paypalCc);
		this.token = Utils.sanitize(this.token);
		this.userId = Utils.sanitize(this.userId);
	}

}

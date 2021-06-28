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

import org.botlibre.BotException;
import org.botlibre.util.Utils;
import org.botlibre.web.admin.User.UserType;

@Entity
public class UserPayment {
	//tx=25G608537N677573S&st=Completed&amt=9.00&cc=USD&item_number=1&reqp=1&reqr=
	@Id
	@GeneratedValue
	protected long id;
	protected String cost;
	protected String token;
	protected UserType userType;
	protected boolean isSubscription;
	@Temporal(TemporalType.TIMESTAMP)
	protected Date paymentDate;
	protected int paymentDuration;
	protected String userId;
	protected UserPaymentStatus status;
	protected UserPaymentType type;

	protected String paypalTx;
	protected String paypalSt;
	protected String paypalAmt;
	protected String paypalCc;
	
	protected String bitCoinJSON;
	
	public enum UserPaymentStatus {WaitingForPayment, Complete, Rejected}
	public enum UserPaymentType {PayPal, BitCoin, GooglePlay, AppleItunes, Braintree, Amazon}
	
	public UserPayment() { 
		cost = "";
		token = "";
		userId = "";
		status = UserPaymentStatus.WaitingForPayment;
		type = UserPaymentType.PayPal;
		userType = UserType.Bronze;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getPaymentDuration() {
		return paymentDuration;
	}

	public void setPaymentDuration(int paymentDuration) {
		this.paymentDuration = paymentDuration;
	}

	public String getCost() {
		return cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

	public UserPaymentStatus getStatus() {
		return status;
	}

	public void setStatus(UserPaymentStatus status) {
		this.status = status;
	}

	public String getBitCoinJSON() {
		return bitCoinJSON;
	}

	public void setBitCoinJSON(String bitCoinJSON) {
		this.bitCoinJSON = bitCoinJSON;
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

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}
	
	public boolean isSubscription() {
		return isSubscription;
	}
	
	public void setSubscription(boolean isSubscribed) {
		this.isSubscription = isSubscribed;
	}

	public Date getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}
	
	public UserPaymentType getType() {
		return type;
	}

	public void setType(UserPaymentType type) {
		this.type = type;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String toString() {
		return getClass().getSimpleName() + ":" + getUserType() + " - " + getPaymentDate();
	}
	
	public void checkConstraints() {
		this.cost = Utils.sanitize(this.cost);
		this.token = Utils.sanitize(this.token);
		this.userId = Utils.sanitize(this.userId);
		this.paypalTx = Utils.sanitize(this.paypalTx);
		this.paypalSt = Utils.sanitize(this.paypalSt);
		this.paypalAmt = Utils.sanitize(this.paypalAmt);
		this.paypalCc = Utils.sanitize(this.paypalCc);
		this.bitCoinJSON = Utils.sanitize(this.bitCoinJSON);
	}
	
	public void updateCost() {
		if (this.userType == UserType.Diamond) {
			setCost(String.format("%.2f", 99.99 * this.paymentDuration));
		} else if (this.userType == UserType.Platinum) {
			setCost(String.format("%.2f", 49.99 * this.paymentDuration));
		} else if (this.userType == UserType.Gold) {
			setCost(String.format("%.2f", 4.99 * this.paymentDuration));
		} else if (this.userType == UserType.Bronze) {
			setCost(String.format("%.2f", 0.99 * this.paymentDuration));
		} else if (this.userType == UserType.Avatar) {
			setCost(String.format("%.2f", 49.99 * this.paymentDuration));
		} else {
			throw new BotException("Invalid upgrade type: " + userType);
		}
	}
	
}

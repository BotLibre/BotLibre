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
package org.botlibre.web.bean;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.Domain.AccountType;
import org.botlibre.web.admin.Payment;
import org.botlibre.web.admin.Payment.PaymentStatus;
import org.botlibre.web.admin.UserPayment;
import org.botlibre.web.admin.UserPayment.UserPaymentStatus;
import org.botlibre.web.admin.UserPayment.UserPaymentType;

public class TransactionBean extends ServletBean {
	
	public enum TransactionSort { Date, User, Transaction }
			
	public enum TransactionRestrict { None, Diamond, Platinum, Gold, Bronze }
	
	String userFilter = "";
	String transactionFilter = "";
	String ccFilter = "";
	TransactionSort transactionSort = TransactionSort.Date;
	UserPaymentType userPaymentType = null;
	UserPaymentStatus userPaymentStatus = null;
	AccountType accountType = null;
	PaymentStatus paymentStatus = null;
	TransactionRestrict transactionRestrict = TransactionRestrict.None;
	
	public String getSortCheckedString(TransactionSort sort) {
		if (sort == this.transactionSort) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getUserPaymentTypeCheckedString(UserPaymentType type) {
		if (type == this.userPaymentType) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getAccountTypeCheckedString(AccountType type) {
		if (type == this.accountType) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getUserPaymentStatusCheckedString(UserPaymentStatus filter) {
		if (filter == this.userPaymentStatus) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getPaymentStatusCheckedString(PaymentStatus filter) {
		if (filter == this.paymentStatus) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getRestrictCheckedString(TransactionRestrict restrict) {
		if (restrict == this.transactionRestrict) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public void resetSearch() {
		this.transactionSort = TransactionSort.Date;
		this.transactionRestrict = TransactionRestrict.None;
		this.userPaymentType = null;
		this.userPaymentStatus = null;
		this.accountType = null;
		this.paymentStatus = null;
		this.userFilter = "";
		this.transactionFilter = "";
		this.ccFilter = "";
		this.page = 0;
		this.resultsSize = 0;
	}
	
	public boolean isDefaults() {
		return this.transactionSort == TransactionSort.Date
			&& this.transactionRestrict == TransactionRestrict.None
			&& this.userPaymentType == null
			&& this.userPaymentStatus == null
			&& this.accountType == null
			&& this.paymentStatus == null
			&& this.userFilter.isEmpty()
			&& this.transactionFilter.isEmpty()
			&& this.ccFilter.isEmpty()
			&& this.page == 0;
	}
	
	public List<UserPayment> getAllUserPayments() {
		try {
			List<UserPayment> results = AdminDatabase.instance().getAllUserPayments(this.page, this.pageSize, this.userFilter, this.transactionFilter, this.ccFilter,
					this.userPaymentType, this.userPaymentStatus, this.transactionRestrict, this.transactionSort);
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (results.size() < this.pageSize) {
					this.resultsSize = results.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllUserPaymentCount(this.userFilter, this.transactionFilter, this.ccFilter,
							this.userPaymentType, this.userPaymentStatus, this.transactionRestrict, this.transactionSort);
				}
			}
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<UserPayment>();
		}
	}
	
	public List<Payment> getAllPayments() {
		try {
			List<Payment> results = AdminDatabase.instance().getAllPayments(this.page, this.pageSize, this.userFilter, this.transactionFilter, this.ccFilter,
					this.accountType, this.paymentStatus, this.transactionRestrict, this.transactionSort);
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (results.size() < this.pageSize) {
					this.resultsSize = results.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllPaymentCount(this.userFilter, this.transactionFilter, this.ccFilter,
							this.accountType, this.paymentStatus, this.transactionRestrict, this.transactionSort);
				}
			}
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Payment>();
		}
	}
	
	public String getUserFilter() {
		if (userFilter == null) {
			return "";
		}
		return userFilter;
	}

	public void setUserFilter(String userFilter) {
		this.userFilter = userFilter;
	}

	public String getTransactionFilter() {
		if (transactionFilter == null) {
			return "";
		}
		return transactionFilter;
	}

	public void setTransactionFilter(String transactionFilter) {
		this.transactionFilter = transactionFilter;
	}

	public String getCcFilter() {
		return ccFilter;
	}

	public void setCcFilter(String ccFilter) {
		this.ccFilter = ccFilter;
	}

	public TransactionSort getTransactionSort() {
		return transactionSort;
	}

	public void setTransactionSort(TransactionSort transactionSort) {
		this.transactionSort = transactionSort;
	}

	public TransactionRestrict getTransactionRestrict() {
		return transactionRestrict;
	}

	public void setTransactionRestrict(TransactionRestrict transactionRestrict) {
		this.transactionRestrict = transactionRestrict;
	}

	public UserPaymentType getUserPaymentType() {
		return userPaymentType;
	}

	public void setUserPaymentType(UserPaymentType userPaymentType) {
		this.userPaymentType = userPaymentType;
	}

	public UserPaymentStatus getUserPaymentStatus() {
		return userPaymentStatus;
	}

	public void setUserPaymentStatus(UserPaymentStatus userPaymentStatus) {
		this.userPaymentStatus = userPaymentStatus;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String searchUserPaymentHTML() {
		StringWriter writer = new StringWriter();
		List<UserPayment> instances = getAllUserPayments();
		writer.write("<span class='menu'>");
		writer.write(getResultsSize() + " ");
		writer.write(this.loginBean.translate("results"));
		writer.write(".<br/>");
		writePagingString(writer, instances);
		writer.write("</span>");
		writer.write("<br/>");
		writer.write("<table id='user' class='tablesorter'>\n");
		writer.write("<thead>\n");
		writer.write("<tr><th>User</th><th>Type</th><th>Status</th><th>UserType</th><th>Duration</th><th>Subscription</th><th>PaypalTx</th><th>PaymentDate</th><th>PaypalAmt</th>");
		writer.write("<th>PaypalCc</th><th>PaypalSt</th><th>Token</th><th>Id</th><th>BitCoinJSON</th></tr>\n");
		writer.write("</thead>\n");
		writer.write("<tbody>\n");
		for (UserPayment payment : instances) {
			writer.write("<tr>");
			writer.write("<td><a href='login?view-user=");
			writer.write(this.loginBean.encodeURI(payment.getUserId()));
			writer.write("'>");
			writer.write(String.valueOf(payment.getUserId()));
			writer.write("</a></td><td>");
			writer.write(String.valueOf(payment.getType()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getStatus()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getUserType()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getPaymentDuration()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.isSubscription()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getPaypalTx()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getPaymentDate()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getPaypalAmt()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getPaypalCc()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getPaypalSt()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getToken()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getId()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getBitCoinJSON()));
			writer.write("</td></tr>");
		}
		writer.write("</tbody>\n");
		writer.write("</table>\n");
		writer.write("<br/>");
		writer.write("<span class = menu>");
		writePagingString(writer, instances);
		writer.write("</span>");
		return writer.toString();
	}

	public String searchPaymentHTML() {
		StringWriter writer = new StringWriter();
		List<Payment> instances = getAllPayments();
		writer.write("<span class='menu'>");
		writer.write(getResultsSize() + " ");
		writer.write(this.loginBean.translate("results"));
		writer.write(".<br/>");
		writePagingString(writer, instances);
		writer.write("</span>");
		writer.write("<br/>");
		writer.write("<table id='user' class='tablesorter'>\n");
		writer.write("<thead>\n");
		writer.write("<tr><th>User</th><th>AccountType</th><th>Status</th><th>Duration</th><th>Subscription</th><th>PaypalTx</th><th>PaymentDate</th><th>PaypalAmt</th>");
		writer.write("<th>PaypalCc</th><th>PaypalSt</th><th>Token</th><th>ID</th><th>Domain</th></tr>\n");
		writer.write("</thead>\n");
		writer.write("<tbody>\n");
		for (Payment payment : instances) {
			writer.write("<tr>");
			writer.write("<td><a href='login?view-user=");
			writer.write(this.loginBean.encodeURI(payment.getUserId()));
			writer.write("'>");
			writer.write(String.valueOf(payment.getUserId()));
			writer.write("</a></td><td>");
			writer.write(String.valueOf(payment.getAccountType()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getStatus()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getPaymentDuration()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.isSubscription()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getPaypalTx()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getPaymentDate()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getPaypalAmt()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getPaypalCc()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getPaypalSt()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getToken()));
			writer.write("</td><td>");
			writer.write(String.valueOf(payment.getId()));
			writer.write("</td><td><a href='domain?id=");
			writer.write(this.loginBean.encodeURI(String.valueOf(payment.getDomainId())));
			writer.write("'>");
			writer.write(String.valueOf(payment.getDomainId()));
			writer.write("</a></td></tr>");
		}
		writer.write("</tbody>\n");
		writer.write("</table>\n");
		writer.write("<br/>");
		writer.write("<span class = menu>");
		writePagingString(writer, instances);
		writer.write("</span>");
		return writer.toString();
	}

	public String searchFormUserPaymentHTML() {
		StringWriter newWriter = new StringWriter();
		newWriter.write("<form action='" + getBrowseAction() + "' method='get' type='submit' class='search'>\n");
		newWriter.write("<span class='menu'>\n");
		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>");
		newWriter.write(this.loginBean.translate("User"));
		newWriter.write("</span>\n");
		newWriter.write("<input id='searchtext' name='user-filter' type='text' value='" + getUserFilter() + "' /></td>\n");
		newWriter.write("</div>\n");

		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>");
		newWriter.write(this.loginBean.translate("Transaction"));
		newWriter.write("</span>\n");
		newWriter.write("<input id='searchtext' name='transaction-filter' type='text' value='" + getTransactionFilter() + "' /></td>\n");
		newWriter.write("</div>\n");

		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>");
		newWriter.write(this.loginBean.translate("CC"));
		newWriter.write("</span>\n");
		newWriter.write("<input id='searchtext' name='cc-filter' type='text' value='" + getCcFilter() + "' /></td>\n");
		newWriter.write("</div>\n");

		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>");
		newWriter.write(this.loginBean.translate("Type"));
		newWriter.write("</span>\n");
		newWriter.write("<select id='searchselect' name='type' onchange='this.form.submit()'>\n");
		newWriter.write("<option value='' " + getUserPaymentTypeCheckedString(null) + "></option>\n");
		newWriter.write("<option value='PayPal' " + getUserPaymentTypeCheckedString(UserPaymentType.PayPal) + ">PayPal</option>\n");
		newWriter.write("<option value='GooglePlay' " + getUserPaymentTypeCheckedString(UserPaymentType.GooglePlay) + ">GooglePlay</option>\n");
		newWriter.write("<option value='AppleItunes' " + getUserPaymentTypeCheckedString(UserPaymentType.AppleItunes) + ">AppleItunes</option>\n");
		newWriter.write("<option value='BitCoin' " + getUserPaymentTypeCheckedString(UserPaymentType.BitCoin) + ">BitCoin</option>\n");
		newWriter.write("</select>\n");
		newWriter.write("</div>\n");

		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>");
		newWriter.write(this.loginBean.translate("Status"));
		newWriter.write("</span>\n");
		newWriter.write("<select id='searchselect' name='status' onchange='this.form.submit()'>\n");
		newWriter.write("<option value='' " + getUserPaymentStatusCheckedString(null) + "></option>\n");
		newWriter.write("<option value='Complete' " + getUserPaymentStatusCheckedString(UserPaymentStatus.Complete) + ">Complete</option>\n");
		newWriter.write("<option value='WaitingForPayment' " + getUserPaymentStatusCheckedString(UserPaymentStatus.WaitingForPayment) + ">WaitingForPayment</option>\n");
		newWriter.write("<option value='Rejected' " + getUserPaymentStatusCheckedString(UserPaymentStatus.Rejected) + ">Rejected</option>\n");
		newWriter.write("</select>\n");
		newWriter.write("</div>\n");

		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>");
		newWriter.write(this.loginBean.translate("Restrict"));
		newWriter.write("</span>\n");
		newWriter.write("<select id='searchselect' name='restrict' onchange='this.form.submit()'>\n");
		newWriter.write("<option value='None' " + getRestrictCheckedString(TransactionRestrict.None) + "></option>\n");
		newWriter.write("<option value='Diamond' " + getRestrictCheckedString(TransactionRestrict.Diamond) + ">Diamond</option>\n");
		newWriter.write("<option value='Platinum' " + getRestrictCheckedString(TransactionRestrict.Platinum) + ">Platinum</option>\n");
		newWriter.write("<option value='Gold' " + getRestrictCheckedString(TransactionRestrict.Gold) + ">Gold</option>\n");
		newWriter.write("<option value='Bronze' " + getRestrictCheckedString(TransactionRestrict.Bronze) + ">Bronze</option>\n");
		newWriter.write("</select>\n");
		newWriter.write("</div>\n");

		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>");
		newWriter.write(loginBean.translate("Sort"));
		newWriter.write("</span>\n");
		newWriter.write("<select id='searchselect' name='sort' onchange='this.form.submit()'>\n");
		newWriter.write("<option value='Date' " + getSortCheckedString(TransactionSort.Date) + ">date</option>\n");
		newWriter.write("<option value='User' " + getSortCheckedString(TransactionSort.User) + ">user</option>\n");
		newWriter.write("<option value='Transaction' " + getSortCheckedString(TransactionSort.Transaction) + ">transaction</option>\n");
		newWriter.write("</select>\n");
		newWriter.write("</div>\n");
		
		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<input style='display:none;position:absolute;' type='submit' name='search' value='' /></td>\n");
		newWriter.write("</div>\n");
		newWriter.write("</span>\n");
		newWriter.write("</form>\n");
		return newWriter.toString();	
	}

	public String searchFormPaymentHTML() {
		StringWriter newWriter = new StringWriter();
		newWriter.write("<form action='" + getBrowseAction() + "' method='get' type='submit' class='search'>\n");
		newWriter.write("<span class='menu'>\n");
		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>");
		newWriter.write(this.loginBean.translate("User"));
		newWriter.write("</span>\n");
		newWriter.write("<input id='searchtext' name='user-filter' type='text' value='" + getUserFilter() + "' /></td>\n");
		newWriter.write("</div>\n");

		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>");
		newWriter.write(this.loginBean.translate("Transaction"));
		newWriter.write("</span>\n");
		newWriter.write("<input id='searchtext' name='transaction-filter' type='text' value='" + getTransactionFilter() + "' /></td>\n");
		newWriter.write("</div>\n");

		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>");
		newWriter.write(this.loginBean.translate("CC"));
		newWriter.write("</span>\n");
		newWriter.write("<input id='searchtext' name='cc-filter' type='text' value='" + getCcFilter() + "' /></td>\n");
		newWriter.write("</div>\n");

		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>");
		newWriter.write(this.loginBean.translate("Account Type"));
		newWriter.write("</span>\n");
		newWriter.write("<select id='searchselect' name='account-type' onchange='this.form.submit()'>\n");
		newWriter.write("<option value='' " + getAccountTypeCheckedString(null) + "></option>\n");
		newWriter.write("<option value='Trial' " + getAccountTypeCheckedString(AccountType.Trial) + ">Trial</option>\n");
		newWriter.write("<option value='Basic' " + getAccountTypeCheckedString(AccountType.Basic) + ">Basic</option>\n");
		newWriter.write("<option value='Premium' " + getAccountTypeCheckedString(AccountType.Premium) + ">Premium</option>\n");
		newWriter.write("<option value='Professional' " + getAccountTypeCheckedString(AccountType.Professional) + ">Professional</option>\n");
		newWriter.write("<option value='Enterprise' " + getAccountTypeCheckedString(AccountType.Enterprise) + ">Enterprise</option>\n");
		newWriter.write("<option value='EnterprisePlus' " + getAccountTypeCheckedString(AccountType.EnterprisePlus) + ">EnterprisePlus</option>\n");
		newWriter.write("<option value='Dedicated' " + getAccountTypeCheckedString(AccountType.Dedicated) + ">Dedicated</option>\n");
		newWriter.write("<option value='Corporate' " + getAccountTypeCheckedString(AccountType.Corporate) + ">Corporate</option>\n");
		newWriter.write("</select>\n");
		newWriter.write("</div>\n");

		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>");
		newWriter.write(this.loginBean.translate("Status"));
		newWriter.write("</span>\n");
		newWriter.write("<select id='searchselect' name='payment-status' onchange='this.form.submit()'>\n");
		newWriter.write("<option value='' " + getPaymentStatusCheckedString(null) + "></option>\n");
		newWriter.write("<option value='Complete' " + getPaymentStatusCheckedString(PaymentStatus.Complete) + ">Complete</option>\n");
		newWriter.write("<option value='WaitingForPayment' " + getPaymentStatusCheckedString(PaymentStatus.WaitingForPayment) + ">WaitingForPayment</option>\n");
		newWriter.write("<option value='Rejected' " + getPaymentStatusCheckedString(PaymentStatus.Rejected) + ">Rejected</option>\n");
		newWriter.write("</select>\n");
		newWriter.write("</div>\n");

		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>");
		newWriter.write(this.loginBean.translate("Restrict"));
		newWriter.write("</span>\n");
		newWriter.write("<select id='searchselect' name='restrict' onchange='this.form.submit()'>\n");
		newWriter.write("<option value='None' " + getRestrictCheckedString(TransactionRestrict.None) + "></option>\n");
		newWriter.write("</select>\n");
		newWriter.write("</div>\n");

		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>");
		newWriter.write(loginBean.translate("Sort"));
		newWriter.write("</span>\n");
		newWriter.write("<select id='searchselect' name='sort' onchange='this.form.submit()'>\n");
		newWriter.write("<option value='Date' " + getSortCheckedString(TransactionSort.Date) + ">date</option>\n");
		newWriter.write("<option value='User' " + getSortCheckedString(TransactionSort.User) + ">user</option>\n");
		newWriter.write("<option value='Transaction' " + getSortCheckedString(TransactionSort.Transaction) + ">transaction</option>\n");
		newWriter.write("</select>\n");
		newWriter.write("</div>\n");
		
		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<input style='display:none;position:absolute;' type='submit' name='search' value='' /></td>\n");
		newWriter.write("</div>\n");
		newWriter.write("</span>\n");
		newWriter.write("</form>\n");
		return newWriter.toString();	
	}
	
	public  String getPostAction() {
		return "transaction";
	}
	
}

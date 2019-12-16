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
package org.botlibre.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.util.Utils;

import org.botlibre.web.admin.Domain.AccountType;
import org.botlibre.web.admin.Payment.PaymentStatus;
import org.botlibre.web.admin.UserPayment.UserPaymentStatus;
import org.botlibre.web.admin.UserPayment.UserPaymentType;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.TransactionBean;
import org.botlibre.web.bean.TransactionBean.TransactionRestrict;
import org.botlibre.web.bean.TransactionBean.TransactionSort;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/transaction")
@SuppressWarnings("serial")
public class TransactionServlet extends BeanServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PageStats.page(request);
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		LoginBean loginBean = (LoginBean)request.getSession().getAttribute("loginBean");
		if (loginBean == null) {
			// Require HTTP session.
			response.sendRedirect("index.jsp");
			return;
		}
		TransactionBean bean = loginBean.getBean(TransactionBean.class);
		try {
			String userFilter = (String)request.getParameter("user-filter");
			String transactionFilter = (String)request.getParameter("transaction-filter");
			String ccFilter = (String)request.getParameter("cc-filter");
			String restrict = (String)request.getParameter("restrict");
			String userPaymentType = (String)request.getParameter("type");
			String userPaymentStatus = (String)request.getParameter("status");
			String accountType = (String)request.getParameter("account-type");
			String paymentStatus = (String)request.getParameter("payment-status");
			String sort = (String)request.getParameter("sort");
			String page = (String) request.getParameter("page");

			if (userFilter != null) {
				bean.setUserFilter(Utils.sanitize(userFilter));
			}
			if (transactionFilter != null) {
				bean.setTransactionFilter(Utils.sanitize(transactionFilter));
			}
			if (ccFilter != null) {
				bean.setCcFilter(Utils.sanitize(ccFilter));
			}
			if (page != null) {
				bean.setPage(Integer.valueOf(page));
				request.getRequestDispatcher("tx.jsp").forward(request, response);
				return;
			}

			bean.setPage(0);
			bean.setResultsSize(0);
			if (sort != null && !sort.isEmpty()) {
				bean.setTransactionSort(TransactionSort.valueOf(Utils.capitalize(sort)));
			}
			if (userPaymentType != null) {
				if (userPaymentType.isEmpty()) {
					bean.setUserPaymentType(null);
				} else {
					bean.setUserPaymentType(UserPaymentType.valueOf(Utils.capitalize(userPaymentType)));
				}
			}
			if (accountType != null) {
				if (accountType.isEmpty()) {
					bean.setAccountType(null);
				} else {
					bean.setAccountType(AccountType.valueOf(Utils.capitalize(accountType)));
				}
			}
			if (userPaymentStatus != null) {
				if (userPaymentStatus.isEmpty()) {
					bean.setUserPaymentStatus(null);
				} else {
					bean.setUserPaymentStatus(UserPaymentStatus.valueOf(Utils.capitalize(userPaymentStatus)));
				}
			}
			if (paymentStatus != null) {
				if (paymentStatus.isEmpty()) {
					bean.setPaymentStatus(null);
				} else {
					bean.setPaymentStatus(PaymentStatus.valueOf(Utils.capitalize(paymentStatus)));
				}
			}
			if (restrict != null && !restrict.isEmpty()) {
				bean.setTransactionRestrict(TransactionRestrict.valueOf(Utils.capitalize(restrict)));
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		response.sendRedirect("tx.jsp");
	}
}

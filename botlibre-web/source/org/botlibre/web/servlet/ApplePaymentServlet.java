/******************************************************************************
 *
 *  Copyright 2013-2020 Paphus Solutions Inc.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.User;
import org.botlibre.web.admin.UserPayment;
import org.botlibre.web.admin.UserPayment.UserPaymentStatus;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.rest.UpgradeConfig;

@javax.servlet.annotation.WebServlet("/applePayment")
@SuppressWarnings("serial")
public class ApplePaymentServlet extends BeanServlet {
		
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		LoginBean bean = getLoginBean(request, response);
		if (bean == null) {
			bean = new LoginBean();
			request.getSession().setAttribute("loginBean", bean);
		}
		
		StringBuffer json = new StringBuffer();
		String line = null;
		
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				json.append(line);
			}
			
			JSONObject jsonObject = new JSONObject(json.toString());
			
			String notificationType = jsonObject.getString("notification_type");
			String origTxnId = jsonObject.getJSONObject("unified_receipt").getJSONArray("latest_receipt_info").getJSONObject(0).getString("original_transaction_id");
			String productId = jsonObject.getJSONObject("unified_receipt").getJSONArray("latest_receipt_info").getJSONObject(0).getString("product_id");
			String transactionId = jsonObject.getJSONObject("unified_receipt").getJSONArray("latest_receipt_info").getJSONObject(0).getString("transaction_id");
			
			AdminDatabase.instance().log(Level.INFO, "PAYMENT: received apple server notification: "
					+ "notificationType-" + notificationType + " origTxnId-" + origTxnId + " productId-" + productId + " transactionId-" + transactionId);
			
			UserPayment origPayment = AdminDatabase.instance().findPaymentTxn(origTxnId);
			if (origPayment == null) {
				AdminDatabase.instance().log(Level.INFO, "PAYMENT: original transaction to subscription was not found", origTxnId);
				return;
			}
			
			String usrId = origPayment.getUserId();
			AdminDatabase.instance().log(Level.INFO, "PAYMENT: upgrade for user " + usrId);
			
			UpgradeConfig config = new UpgradeConfig();
			config.type = "AppleItunes";
			
			if (productId.equals("com.botlibre.bronze.sub")) {
				config.userType = "Bronze";
			} else if (productId.equals("com.botlibre.gold.sub")) {
				config.userType = "Gold";
			} else if (productId.equals("com.botlibre.platinum.sub")) {
				config.userType = "Platinum";
			} else {
				AdminDatabase.instance().log(Level.INFO, "PAYMENT: invalid product id: " + productId);
				return;
			}
			
			config.orderId = "recurring";
			config.orderToken = transactionId;
			config.sku = productId;
			config.secret = Long.toString(Long.valueOf(Site.UPGRADE_SECRET) + usrId.length());
						
			config.months = "1";
			config.subscription = true;
			
			AdminDatabase.instance().log(Level.INFO, "PAYMENT: upgrade-user with apple notification " + config.userType);
			
			try {
				User usr = AdminDatabase.instance().findUser(usrId);
				if (usr == null) {
					AdminDatabase.instance().log(Level.INFO, "PAYMENT: user linked with orginal tranaction was not found");
					return;
				}
				
				UserPayment currPayment = AdminDatabase.instance().findPaymentTxn(transactionId);
				if (currPayment != null) {
					AdminDatabase.instance().log(Level.INFO, "PAYMENT: transaction " + transactionId + " already processed for user " + usrId);
					return;
				}
				
				boolean processed = true;
				if (!notificationType.equals("DID_RENEW")) {
					if (currPayment == null) {
						AdminDatabase.instance().log(Level.INFO, "PAYMENT: transaction has not been processed " + transactionId);
						processed = false;
					} 
				} else if (notificationType.equals("DID_RENEW") || !processed) {
					bean.setUser(usr);
					bean.upgradeUser(config);
					AdminDatabase.instance().log(Level.INFO, "PAYMENT: upgraded user " + usrId + " with user type " + config.userType);
					if (bean.getError() != null) {
						throw bean.getError();
					}
				}
			} catch (Throwable failed) {
				AdminDatabase.instance().log(Level.WARNING, "PAYMENT: Upgrade failed: exception", config.orderId, failed);
				bean.getPayment().setStatus(UserPaymentStatus.Rejected);
				AdminDatabase.instance().updatePayment(bean.getPayment());
			}
			
		} catch (Throwable failed) {
			AdminDatabase.instance().log(failed);
			bean.setError(failed);
		}
		
		response.setStatus(200);
		return;
	}
}



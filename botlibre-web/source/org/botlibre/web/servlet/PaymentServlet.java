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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.util.Utils;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.User.UserType;
import org.botlibre.web.admin.UserPayment;
import org.botlibre.web.admin.UserPayment.UserPaymentType;
import org.botlibre.web.bean.LoginBean;

import com.paypal.ipn.IPNMessage;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@javax.servlet.annotation.WebServlet("/payment")
@SuppressWarnings("serial")
public class PaymentServlet extends BeanServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		doPost(request, response);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		LoginBean bean = getLoginBean(request, response);
		
		if (bean == null) {
			bean = new LoginBean();
			request.getSession().setAttribute("loginBean", bean);
		}
		
		try {
			if (!bean.checkDomain(request, response)) {
				return;
			}
			
			String userType = Utils.sanitize((String)request.getParameter("userType"));
			String duration = Utils.sanitize((String)request.getParameter("duration"));
			String paymentType = Utils.sanitize((String)request.getParameter("paymentType")); 
			String cancel = Utils.sanitize((String)request.getParameter("cancel"));
			String next = Utils.sanitize((String)request.getParameter("next"));
			String complete = Utils.sanitize((String)request.getParameter("complete"));
			
			if (paymentType != null) {
				boolean isSubscribed = false;
				if (paymentType.equalsIgnoreCase("subscription")) {
					isSubscribed = true;
					duration = "1";
				} else if (paymentType.equalsIgnoreCase("onetime")) {
					isSubscribed = false;
				}
				AdminDatabase.instance().log(Level.INFO, "PAYMENT: payment information received: " + paymentType + " " + userType + " " + duration);
				if (cancel != null) {
					request.getRequestDispatcher("upgrade.jsp").forward(request, response);
					return;
				} else if (next != null) {
					if (userType.equalsIgnoreCase("upgradeBronze")) {
						bean.beginPayment(UserType.Bronze, UserPaymentType.PayPal, "", "", "", "", duration, isSubscribed);
					} else if (userType.equalsIgnoreCase("upgradeGold")) {
						bean.beginPayment(UserType.Gold, UserPaymentType.PayPal, "", "", "", "", duration, isSubscribed);
					} else if (userType.equalsIgnoreCase("upgradePlatinum")) {
						bean.beginPayment(UserType.Platinum, UserPaymentType.PayPal, "", "", "", "", duration, isSubscribed);
					} else {
						bean.beginPayment(UserType.Diamond, UserPaymentType.PayPal, "", "", "", "", duration, isSubscribed);
					}
					response.sendRedirect("upgrade-confirm.jsp");
					return;
				}
				response.sendRedirect("upgrade-payment.jsp");
				return;
			}
		
			if (complete != null) {
				if (bean.getUser().isSuperUser()) {
					bean.completeAdminPayment();
					AdminDatabase.instance().log(Level.INFO, "PAYMENT: " + bean.getPayment().getId() + " payment processed: Complete");
					response.sendRedirect("upgrade-complete.jsp");
				}
				return;
			}
			
			if (cancel != null) {
				request.getRequestDispatcher("upgrade.jsp").forward(request, response);
				return;
			}
			
			String checkPayment = (String)request.getParameter("checkPayment");
			if (checkPayment != null) {
				AdminDatabase.instance().log(Level.INFO, "PAYMENT: checking payment status");
				if (bean.checkPayment()) {
					response.sendRedirect("upgrade-complete.jsp");
				} else {
					response.sendRedirect("upgrade-confirm.jsp");
				}
				return;
			}
			
			boolean isIPN = false;
			Enumeration<String> headerNames = request.getHeaderNames();
			if (headerNames != null) {
				while (headerNames.hasMoreElements()) {
					String header = request.getHeader(headerNames.nextElement());
					if (header.equalsIgnoreCase("PayPal IPN ( https://www.paypal.com/ipn )")) {
						isIPN = true;
					}
				}
			}
			String custom = Utils.sanitize((String)request.getParameter("cm"));
			if (custom == null) {
				custom = Utils.sanitize((String)request.getParameter("custom"));  
			}
			String url = ""; 
			if (isIPN) {
				if (custom != null) {
					UserPayment payment = AdminDatabase.instance().findUserPayment(Long.valueOf(custom));
					bean.setUser(AdminDatabase.instance().findUser(payment.getUserId()));
				}
				if ((bean.getUser() != null) && ((bean.getUser().getUserId().equals("test2")) || 
						(bean.getUser().getUserId().equals("test")) || (bean.getUser().getUserId().equals("admin")) || 
						(bean.getUser().getUserId().equals("test1")) || (bean.getUser().getUserId().equals("testing")))) {
					url = "https://ipnpb.sandbox.paypal.com/cgi-bin/webscr?cmd=_notify-validate";
				} else {
					url = "https://ipnpb.paypal.com/cgi-bin/webscr?cmd=_notify-validate";
				}
				Enumeration<String> params = request.getParameterNames();
				if (params != null) {
					while(params.hasMoreElements()){
						String paramName = params.nextElement();
						String paramValue = request.getParameter(paramName);
						url += "&" + paramName + "=" + URLEncoder.encode(paramValue, StandardCharsets.UTF_8.toString());
					}
				}
				AdminDatabase.instance().log(Level.INFO, "PAYMENT: " + custom + " IPN: " + url);
			}
			String tx = Utils.sanitize((String)request.getParameter("tx")); 
			String amt = Utils.sanitize((String)request.getParameter("amt")); 
			String st = Utils.sanitize((String)request.getParameter("st")); 
			String cc = Utils.sanitize((String)request.getParameter("cc")); 
			String sub_date = Utils.sanitize((String)request.getParameter("payment_date"));
			String item_number = Utils.sanitize((String)request.getParameter("item_number"));
			String token = "";
			if (item_number != null) {
				for (int i = 0; i < item_number.length(); ++i) {
					if (!Character.isDigit(item_number.charAt(i))) {
						break;
					}
					token += item_number.charAt(i);
				}
			}
			
			if (tx == null) {
				tx = Utils.sanitize((String)request.getParameter("txn_id"));
			}
			if (amt == null) {
				amt = Utils.sanitize((String)request.getParameter("mc_gross")); 
			}
			if (st == null) {
				st = Utils.sanitize((String)request.getParameter("payment_status")); 
			}
			if (cc == null) {
				cc = Utils.sanitize((String)request.getParameter("mc_currency")); 
			}
			String tkn = Utils.sanitize((String)request.getParameter("token"));
			String txn_type = Utils.sanitize((String)request.getParameter("txn_type"));
			
			if (tkn != null) {
				AdminDatabase.instance().log(Level.INFO, "PAYMENT: " + custom + " recieved tkn for subscription: " + tkn);
				response.sendRedirect("upgrade-complete.jsp");
				response.setStatus(200);
				return;
			}
			
			final String postURL = url;
			final String payment_id = custom;
			final String transaction_id = tx;
			
			String ipn_track_id = Utils.sanitize((String) request.getParameter("ipn_track_id"));
			if (ipn_track_id != null) {
				isIPN = true;
				AdminDatabase.instance().log(Level.INFO, "PAYMENT: " + payment_id + " IPN: recieved Paypal IPN");
				Map<String,String> configMap = new HashMap<String,String>();
				if ((bean.getUser() != null) && ((bean.getUser().getUserId().equals("test2")) || 
						(bean.getUser().getUserId().equals("test")) || (bean.getUser().getUserId().equals("admin")) || 
						(bean.getUser().getUserId().equals("test1")) || (bean.getUser().getUserId().equals("testing")))) {
					configMap.put("mode", "sandbox");
				} else {
					configMap.put("mode", "live");
				}
				
				IPNMessage ipnlistener = new IPNMessage(request,configMap);
				boolean isIpnVerified = ipnlistener.validate();
				String transactionType = ipnlistener.getTransactionType();
				Map<String,String> map = ipnlistener.getIpnMap();
				AdminDatabase.instance().log(Level.INFO, "PAYMENT: " + payment_id + " IPN: TransactionType : " + transactionType + " - IPN verified : "
						+ isIpnVerified + " - IPN (name:value) pair : " + map + "  ");
				
				Thread thread = new Thread("New Thread") {
					public void run() {
						try {
							Thread.sleep(5000);
					
							String type = "application/x-www-form-urlencoded";
							String data = "";
							
							Map<String, String> headers = new HashMap<String, String>();
							headers.put("User-Agent", "JAVA-IPN-VerificationScript");
							
							String verifyIPN = Utils.httpPOST(postURL, type, data, headers);
							
							if (verifyIPN.equalsIgnoreCase("VERIFIED")) {
								AdminDatabase.instance().log(Level.INFO, "PAYMENT: " + payment_id + " IPN: Verified IPN message for Payment ID: " + payment_id + 
										(transactionType.equalsIgnoreCase("subscr_payment") ? " and transation-id: "+ transaction_id + " is verified." : ""));
							} else {
								AdminDatabase.instance().log(Level.INFO, "PAYMENT: " + payment_id + " IPN: Invalid IPN message for Payment ID: " + payment_id +
										(transactionType.equalsIgnoreCase("subscr_payment") ? " and transation-id: "+ transaction_id + " is invalid." : ""));
							}
						} catch (InterruptedException exception) {
							AdminDatabase.instance().log(exception);
						} catch (Exception exception) {
							AdminDatabase.instance().log(exception);
						}
					}
				};
				thread.start();
			}
			
			if (tx != null) {
				if (isIPN && !(txn_type.equalsIgnoreCase("subscr_payment") || txn_type.equals("web_accept"))) {
					return;
				}
				AdminDatabase.instance().log(Level.INFO, "PAYMENT: " + payment_id + " recieved payment, processing : tx: " + tx +
						" amt: " + amt + " st: " + st + " cc: " + cc + " payment_id: " + custom +  " isIPN: " + isIPN);
				response.setStatus(200);
				bean.confirmPayment(tx, amt, st, cc, custom, sub_date, isIPN, token, txn_type);
				response.sendRedirect("upgrade-complete.jsp");
				if (!isIPN) {
					return;
				}
			} else {
				response.sendRedirect("upgrade-complete.jsp");
			}
			
			response.setStatus(200);
			return;

		} catch (Throwable failed) {
			AdminDatabase.instance().log(failed);
			bean.setError(failed);
		}
	}

}

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
import org.botlibre.web.admin.Payment;
import org.botlibre.web.bean.DomainBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.SessionProxyBean;

import com.paypal.ipn.IPNMessage;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@javax.servlet.annotation.WebServlet("/upgradepayment")
@SuppressWarnings("serial")
public class UpgradePaymentServlet extends BeanServlet {
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		doPost(request, response);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		LoginBean loginBean = getLoginBean(request, response);
		if (loginBean == null) {
			loginBean = new LoginBean();
			request.getSession().setAttribute("loginBean", loginBean);
		}
		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		DomainBean bean = loginBean.getBean(DomainBean.class);
		loginBean.setActiveBean(bean);
		
		try {
			if (!loginBean.checkDomain(request, response)) {
				return;
			}
		
			String cancel = (String)request.getParameter("cancel");
			if (cancel != null) {
				if (!bean.hasValidInstance()) {
					bean.setInstance(null);
				}
				bean.setWizardDomain(null);
				bean.setWizardState(null);
				request.getRequestDispatcher("index.jsp").forward(request, response);
				return;
			}
			
			String next = Utils.sanitize((String)request.getParameter("next"));
			if (next != null) {
				if (bean.confirmPayment("", "", "", "", "", "", "", "")) {
					AdminDatabase.instance().log(Level.INFO, "PAYMENT: payment processed: Complete");
					response.sendRedirect("domain?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				} else {
					AdminDatabase.instance().log(Level.INFO, "PAYMENT: payment error");
					response.sendRedirect("create-domain.jsp");
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
			
			String item_number = Utils.sanitize((String)request.getParameter("item_number"));
			if (item_number == null) {
				item_number = Utils.sanitize((String)request.getParameter("item_number1"));
			}
			
			String url = "";
			if (isIPN) {
				if (item_number != null) {
					Payment userPayment = AdminDatabase.instance().findPayment(Long.valueOf(item_number));
					bean.setUser(AdminDatabase.instance().findUser(userPayment.getUserId()));
					AdminDatabase.instance().log(Level.INFO, "PAYMENT: " + item_number + " : " + userPayment.getUserId() + " IPN: - processing payment");
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
					while (params.hasMoreElements()) {
						String paramName = params.nextElement();
						String paramValue = request.getParameter(paramName);
						url += "&" + paramName + "=" + URLEncoder.encode(paramValue, StandardCharsets.UTF_8.toString());
					}
				}
			}
			
			String tx = Utils.sanitize((String)request.getParameter("tx"));
			String amt = Utils.sanitize((String)request.getParameter("amt"));
			String st = Utils.sanitize((String)request.getParameter("st"));
			String cc = Utils.sanitize((String)request.getParameter("cc"));
			String custom = Utils.sanitize((String)request.getParameter("cm"));
			String payment_date = Utils.sanitize((String)request.getParameter("payment_date"));
			String invoice = Utils.sanitize((String)request.getParameter("invoice"));
			String token = "";
			String txn_type = Utils.sanitize((String)request.getParameter("txn_type"));
			if (invoice != null) {
				for (int i = 0; i < invoice.length(); ++i) {
					if (!Character.isDigit(invoice.charAt(i))) {
						break;
					}
					token += invoice.charAt(i);
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
			if (custom == null) {
				custom = Utils.sanitize((String)request.getParameter("custom"));  
			}
			String tkn = Utils.sanitize((String)request.getParameter("token"));
						
			final String postURL = url;
			final String payment_id = item_number;
			final String transaction_id = tx;
			
			String ipn_track_id = Utils.sanitize((String) request.getParameter("ipn_track_id"));
			if (ipn_track_id != null) {
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
								AdminDatabase.instance().log(Level.INFO, "PAYMENT: " + payment_id + " IPN: Verified IPN message for payment ID: " + payment_id + 
										(transactionType.equalsIgnoreCase("subscr_payment") ? " and transation-id: " + transaction_id + " is verified." : ""));
							} else {
								AdminDatabase.instance().log(Level.INFO, "PAYMENT: " + payment_id + " IPN: Invalid IPN message for payment ID: " + payment_id +
										(transactionType.equalsIgnoreCase("subscr_payment") ? " and transation-id: " + transaction_id + " is invalid." : ""));
							}
						} catch (InterruptedException exception) {
							AdminDatabase.instance().log(exception);
						} catch (Exception exception) {
							AdminDatabase.instance().log(exception);
						}
					}
				};
				thread.start();
				response.setStatus(200);
			}
			
			if (tkn != null) {
				AdminDatabase.instance().log(Level.INFO, "PAYMENT: " + payment_id + " recieved tkn: " + tkn);
				response.sendRedirect("upgrade-complete.jsp");
				response.setStatus(200);
				return;
			}
			
			if (tx != null) {
				if (isIPN && !(txn_type.equalsIgnoreCase("subscr_payment") || txn_type.equals("web_accept"))) {
					return;
				}
				AdminDatabase.instance().log(Level.INFO, "PAYMENT: " + payment_id + " processing payment");
				if (!bean.confirmPayment(tx, amt, st, cc, custom, payment_id, payment_date, token)) {
					AdminDatabase.instance().log(Level.INFO, "PAYMENT: " + payment_id + " invalid payment: unable to process");
					response.sendRedirect("create-domain.jsp");
				} else {
					AdminDatabase.instance().log(Level.INFO, "PAYMENT: " + payment_id + " valid payment: processing complete");
					response.sendRedirect("upgrade-complete.jsp");
				}
				if (!isIPN) {
					return;
				}
			} else {
				response.sendRedirect("upgrade-complete.jsp");
				response.setStatus(200);
			}
			
			response.setStatus(200);
			return;
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		request.getRequestDispatcher("domain-search.jsp").forward(request, response);
	}

}

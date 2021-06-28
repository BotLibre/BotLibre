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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.bean.BrowseBean.InstanceFilter;
import org.botlibre.web.bean.DomainBean;
import org.botlibre.web.bean.DomainBean.WizardState;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.LoginBean.Page;
import org.botlibre.web.bean.SessionProxyBean;
import org.botlibre.web.rest.DomainConfig;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/domain")
@SuppressWarnings("serial")
public class DomainServlet extends BeanServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		LoginBean loginBean = getEmbeddedLoginBean(request, response);
		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		DomainBean bean = loginBean.getBean(DomainBean.class);
		loginBean.setActiveBean(bean);
		if (!loginBean.checkDomain(request, response)) {
			return;
		}
		
		String id = (String)request.getParameter("id");
		String details = (String)request.getParameter("details");
		if (id != null) {
			if (proxy != null) {
				proxy.setInstanceId(id);
			}
			if (loginBean.getDomainEmbedded()) {
				Domain instance = bean.getInstance();
				if (instance != null) {
					bean.validateInstance(id);
					if (!instance.equals(bean.getInstance())) {
						bean.setInstance(instance);
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						loginBean.error(new BotException("Invalid URL - " + request.getRequestURL()));
						try {
							request.getRequestDispatcher("404.jsp").forward(request, response);
							return;
						} catch (Exception exception) {
							loginBean.setError(exception);
							return;
						}
					}
				}
			}
			if (bean.validateInstance(id)) {
				loginBean.setCategoryType(Site.TYPE);
				bean.incrementConnects(ClientType.WEB);
				if (details == null) {
					request.getRequestDispatcher("index.jsp").forward(request, response);
					return;
				}
			}
			request.getRequestDispatcher("domain.jsp").forward(request, response);
			return;
		}
		if (details != null) {
			request.getRequestDispatcher("domain.jsp").forward(request, response);
			return;
		}
		String domain = (String)request.getParameter("domain");
		if (domain != null) {
			if (domain.equals("-") && request.getHeader("referer") != null) {
				response.sendRedirect(request.getHeader("referer"));
			} else if (domain.equals("paphus")) {
				bean.setInstance(null);
				response.sendRedirect("https://www.botlibre.biz");
			} else if (domain.equals("botlibre")) {
				bean.setInstance(null);
				response.sendRedirect("https://www.botlibre.com");
			} else if (domain.equals("botlibre-twitter")) {
				bean.setInstance(null);
				response.sendRedirect("http://twitter.botlibre.com");
			} else if (domain.equals("livechatlibre")) {
				bean.setInstance(null);
				response.sendRedirect("http://www.livechatlibre.com");
			} else if (domain.equals("forumslibre")) {
				bean.setInstance(null);
				response.sendRedirect("http://www.forumslibre.com");
			} else if (domain.equals("new")) {
				response.sendRedirect("create-domain.jsp");
			} else {
				if (loginBean.getDomainEmbedded()) {
					Domain instance = bean.getInstance();
					if (instance != null) {
						bean.validateInstance(domain);
						if (!instance.equals(bean.getInstance())) {
							bean.setInstance(instance);
							response.setStatus(HttpServletResponse.SC_FORBIDDEN);
							loginBean.error(new BotException("Invalid URL - " + request.getRequestURL()));
							try {
								request.getRequestDispatcher("404.jsp").forward(request, response);
								return;
							} catch (Exception exception) {
								loginBean.setError(exception);
								return;
							}
						}
					}
				}
				if (bean.validateInstance(domain)) {
					Cookie cookie = new Cookie("lastdomain", String.valueOf(id));
					cookie.setMaxAge(60*60*24*30);
					cookie.setPath("/");
					response.addCookie(cookie);
					bean.incrementConnects(ClientType.WEB);
					loginBean.setCategoryType(Site.TYPE);
				}
				request.getRequestDispatcher("index.jsp").forward(request, response);
			}
			return;
		}
		doPost(request, response);
	}
	
	//tx=25G608537N677573S&st=Completed&amt=9.00&cc=USD&item_number=1&reqp=1&reqr=
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		LoginBean loginBean = getLoginBean(request, response);
		if (loginBean == null) {
			loginBean = new LoginBean();
			request.getSession().setAttribute("loginBean", loginBean);
		}
		DomainBean bean = loginBean.getBean(DomainBean.class);
		loginBean.setActiveBean(bean);
		
		try {
			String postToken = (String)request.getParameter("postToken");
			if  (!loginBean.checkDomain(request, response)) {
				return;
			}
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (loginBean.getDomainEmbedded()) {
					Domain domain = bean.getInstance();
					if (instance != null) {
						bean.validateInstance(instance);
						if (!domain.equals(bean.getInstance())) {
							bean.setInstance(domain);
							response.setStatus(HttpServletResponse.SC_FORBIDDEN);
							loginBean.error(new BotException("Invalid URL - " + request.getRequestURL()));
							try {
								request.getRequestDispatcher("404.jsp").forward(request, response);
								return;
							} catch (Exception exception) {
								loginBean.setError(exception);
								return;
							}
						}
					}
				}
				if (bean.getInstance() == null || !String.valueOf(bean.getInstanceId()).equals(instance)) {
					bean.validateInstance(instance);
				}
			}
			String cancelInstance = (String)request.getParameter("cancel-instance");
			if (cancelInstance != null) {
				response.sendRedirect("domain?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
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
			String createDomain = (String)request.getParameter("create-domain");
			String editInstance = (String)request.getParameter("edit-instance");
			String saveInstance = (String)request.getParameter("save-instance");
			String deleteInstance = (String)request.getParameter("delete-instance");
			if (createDomain != null) {
				response.sendRedirect("create-domain.jsp");
				return;
			}
			if (editInstance != null) {
				if (!bean.editInstance(bean.getInstance().getId())) {
					response.sendRedirect("domain?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("edit-domain.jsp").forward(request, response);
				return;
			}
			
			if (checkCommon(bean, "domain?details=true&id=" + bean.getInstanceId(), request, response)) {
				return;
			}
			if (checkUserAdmin(bean, "domain-users.jsp", request, response)) {
				return;
			}
			
			String user = Utils.sanitize((String)request.getParameter("user"));
			String password = (String)request.getParameter("password");
			String password2 = (String)request.getParameter("password2");
			String hint = Utils.sanitize((String)request.getParameter("hint"));
			String dateOfBirth = Utils.sanitize((String)request.getParameter("dateOfBirth"));
			String credentialsType = (String)request.getParameter("credentials-type");
			String credentialsUserID = (String)request.getParameter("credentials-userid");
			String credentialsToken = (String)request.getParameter("credentials-token");
			String name = Utils.sanitize((String)request.getParameter("name"));
			String email = Utils.sanitize((String)request.getParameter("email"));
			
			DomainConfig config = new DomainConfig();
			updateParameters(config, request);
			String newInstance = (String)request.getParameter("newInstance");
			String isFeatured = (String)request.getParameter("isFeatured");
			String isSubscription = (String)request.getParameter("isSubscription");
			String delete = (String)request.getParameter("delete");
			config.creationMode = (String)request.getParameter("creationMode");
			config.subdomain = (String)request.getParameter("subdomain");
			String paymentType = Utils.sanitize((String)request.getParameter("paymentType"));
			String accountType = Utils.sanitize((String)request.getParameter("accountType"));
			String duration = Utils.sanitize((String)request.getParameter("duration"));
			boolean isSubscribed = false;
			if (paymentType != null) {
				if (paymentType.equalsIgnoreCase("subscription")) {
					isSubscribed = true;
					duration = "1";
				}
			}
			
			String tx = Utils.sanitize((String)request.getParameter("tx"));
			String amt = Utils.sanitize((String)request.getParameter("amt"));
			String st = Utils.sanitize((String)request.getParameter("st"));
			String cc = Utils.sanitize((String)request.getParameter("cc"));
			String custom = Utils.sanitize((String)request.getParameter("cm"));
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
			
			String next = (String)request.getParameter("next");
			if ((next != null) || (tx != null)) {
				//loginBean.verifyPostToken(postToken);
				if (!bean.wizard(user, password, password2, dateOfBirth, hint, name, BeanServlet.extractIP(request), email, credentialsType, credentialsUserID, credentialsToken,
						newInstance, Utils.sanitize(config.description), config.isPrivate, config.isHidden, Utils.sanitize(config.accessMode), Utils.sanitize(config.creationMode),
						accountType, duration, isSubscribed,
						tx, st, amt, cc, custom)) {
					response.sendRedirect("create-domain.jsp");
				} else {
					response.sendRedirect("domain?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			String makePayment = (String)request.getParameter("makePayment");
			if (makePayment != null) {
				bean.setWizardDomain(bean.getDomain());
				bean.setWizardState(WizardState.Payment);
				response.sendRedirect("create-domain.jsp");
				return;
			}

			String createInstance = (String)request.getParameter("create-instance");
			if (createInstance != null) {
				loginBean.verifyPostToken(postToken);
				config.name = newInstance;
				if (!bean.createInstance(config)) {
					response.sendRedirect("create-domain.jsp");
				} else {
					loginBean.setPageType(Page.Browse);
					loginBean.setCategoryType(Site.TYPE);
					response.sendRedirect("domain?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			if (saveInstance != null) {
				try {
					loginBean.verifyPostToken(postToken);
					if (!bean.updateDomain(config, "on".equals(isFeatured), "on".equals(isSubscription))) {
						request.getRequestDispatcher("edit-domain.jsp").forward(request, response);
					} else {
						response.sendRedirect("domain?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
					}
				} catch (Exception failed) {
					bean.error(failed);
					request.getRequestDispatcher("edit-domain.jsp").forward(request, response);
				}
				return;
			}
			if (deleteInstance != null) {
				loginBean.verifyPostToken(postToken);
				if (!bean.deleteInstance("on".equals(delete))) {
					response.sendRedirect("domain?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				} else {
					response.sendRedirect("index.jsp");
				}
				return;
			}
			String adminInstance = (String)request.getParameter("admin");
			if (adminInstance != null) {
				if (!bean.adminInstance(bean.getInstanceId())) {
					response.sendRedirect("domain?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("admin-domain.jsp").forward(request, response);
				return;
			}

			String category = (String)request.getParameter("category");
			if (category != null) {
				if (loginBean.getDomainEmbedded()) {
					throw new BotException("Cannot change domains for embedded domain");
				}
				bean.setInstance(null);
				bean.browseCategory(category);
				request.getRequestDispatcher("domains.jsp").forward(request, response);
				return;
			}
			String createCategory = (String)request.getParameter("create-category");
			if (createCategory != null) {
				loginBean.setCategoryType("Domain");
				loginBean.setActiveBean(bean);
				request.getRequestDispatcher("create-category.jsp").forward(request, response);
				return;
			}
			String editCategory = (String)request.getParameter("edit-category");
			if (editCategory != null) {
				loginBean.setCategoryType("Domain");
				loginBean.setActiveBean(bean);
				loginBean.setCategory(bean.getCategory());
				request.getRequestDispatcher("edit-category.jsp").forward(request, response);
				return;
			}
			String deleteCategory = (String)request.getParameter("delete-category");
			if (deleteCategory != null) {
				loginBean.verifyPostToken(postToken);
				loginBean.setCategoryType("Domain");
				loginBean.setActiveBean(bean);
				loginBean.setCategory(bean.getCategory());
				loginBean.deleteCategory();
				response.sendRedirect("domains.jsp");
				return;
			}
			
			String removeCategory = (String)request.getParameter("remove-category");
			category = (String)request.getParameter("selected-category");
			if (removeCategory != null) {
				loginBean.verifyPostToken(postToken);
				bean.removeCategory(category);
				request.getRequestDispatcher("domain-categories.jsp").forward(request, response);
				return;
			}
			String removeTag = (String)request.getParameter("remove-tag");
			String tag = (String)request.getParameter("selected-tag");
			if (removeTag != null) {
				loginBean.verifyPostToken(postToken);
				bean.removeTag(tag);
				request.getRequestDispatcher("domain-tags.jsp").forward(request, response);
				return;
			}

			if (checkSearchCommon(bean, "domain-search.jsp", request, response)) {
				return;
			}

			setSearchFields(request, bean);
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		request.getRequestDispatcher("domain-search.jsp").forward(request, response);
	}
}

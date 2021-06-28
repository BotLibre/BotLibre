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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.botlibre.BotException;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;
import org.botlibre.web.Site;
import org.botlibre.web.admin.ContentRating;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.BrowseBean;
import org.botlibre.web.bean.BrowseBean.DisplayOption;
import org.botlibre.web.bean.BrowseBean.InstanceFilter;
import org.botlibre.web.bean.BrowseBean.InstanceRestrict;
import org.botlibre.web.bean.BrowseBean.InstanceSort;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.SessionProxyBean;
import org.botlibre.web.bean.WebMediumBean;
import org.botlibre.web.rest.WebMediumConfig;
import org.botlibre.web.service.PageStats;

@SuppressWarnings("serial")
public abstract class BeanServlet extends HttpServlet {

	/**
	 * Secure: Check the URL should be allowed to be accessed.
	 * Allow URL to this website and external websites.
	 */
	public static URL safeURL(String url) throws MalformedURLException {
		if (checkSandboxURL(url)) {
			// Allow website URLs.
			return new URL(url);
		} else {
			return Utils.safeURL(url);
		}
	}
	
	/**
	 * Check if the URL is for this website.
	 */
	public static boolean checkSandboxURL(String url) {
		TextStream stream = new TextStream(url);
		String token = stream.upTo(':');
		if (!"http".equals(token) && !"https".equals(token)) {
			return false;
		}
		token = stream.next(3);
		if (token == null || !"://".equals(token)) {
			return false;
		}
		token = stream.upTo('/');
		if (token == null) {
			return false;
		}
		if (token.startsWith("www")) {
			token = token.substring("www".length() + 1, token.length());
		}
		String server = Site.URL;
		if (server.startsWith("www")) {
			server = server.substring("www".length() + 1, server.length());
		}
		if (server.indexOf("/") != -1) {
			// Remove app.
			server = server.substring(0, server.indexOf("/"));
		}
		if (server.equals(token)) {
			return true;
		}
		// Remove subdomain.
		if (token.indexOf(".") != -1) {
			token = token.substring(token.indexOf(".") + 1, token.length());
		}
		if (server.equals(token)) {
			return true;
		}
		return false;
	}
	
	public static String extractIP(HttpServletRequest requestContext) {
		// Support CloudFlare proxy IP.
		String ip = requestContext.getHeader("CF-Connecting-IP");
		if (ip != null && !ip.isEmpty()) {
			return ip;
		}
		return requestContext.getRemoteAddr();
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * Gets the login bean from the 'current' session.
	 */
	public LoginBean getLoginBean(HttpServletRequest request, HttpServletResponse response) {
		return getLoginBean(request, response, false);
	}

	/**
	 * Gets the login bean from the 'current' session.
	 */
	public LoginBean getLoginBean(HttpServletRequest request, HttpServletResponse response, boolean reset) {
		PageStats.page(request);
		LoginBean loginBean = (LoginBean)request.getSession().getAttribute("loginBean");
		if (loginBean == null && reset) {
			loginBean = new LoginBean();
			if (!loginBean.checkDomain(request, response)) {
				return null;
			}
			request.getSession().setAttribute("loginBean", loginBean);
			loginBean.initialize(getServletContext(), request, response);
		}
		if (loginBean != null) {
			loginBean.setHttps(request.isSecure());
		}
		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		if (proxy == null) {
			return loginBean;
		}
		String value = request.getParameter("proxy");
		if (value != null && !value.equals("null")) {
			try {
				Long proxyId = Long.valueOf(value);
				proxy.setBeanId(proxyId);
				loginBean = proxy.checkLoginBean(loginBean);
			} catch (Exception exception) {
				return null;
			}
		} else {
			proxy.setBeanId(null);
		}
		if (loginBean == null) {
			return null;
		}
		loginBean.initialize(getServletContext(), request, response);
		return loginBean;
	}
	
	public SessionProxyBean checkProxy(HttpServletRequest request, HttpServletResponse response) {
		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		if (proxy == null) {
			proxy = new SessionProxyBean();
			request.getSession().setAttribute("proxy", proxy);
		}
		return proxy;
	}

	/**
	 * Gets or creates the login bean for an embedded session.
	 */
	public LoginBean getEmbeddedLoginBean(HttpServletRequest request, HttpServletResponse response) {
		PageStats.page(request);
		LoginBean loginBean = (LoginBean)request.getSession().getAttribute("loginBean");
		SessionProxyBean proxy = checkProxy(request, response);
		String value = request.getParameter("proxy");
		if (value != null) {
			try {
				Long proxyId = Long.valueOf(value);
				proxy.setBeanId(proxyId);
			} catch (Exception ignore) { }
		} else {
			proxy.setBeanId(null);
		}
		String embedded = Utils.sanitize((String)request.getParameter("embedded"));
		String debug = Utils.sanitize((String)request.getParameter("debug"));
		String user = Utils.sanitize((String)request.getParameter("user"));
		String password = Utils.sanitize((String)request.getParameter("password"));
		String token = Utils.sanitize((String)request.getParameter("token"));
		String css = Utils.sanitize((String)request.getParameter("css"));
		String banner = Utils.sanitize((String)request.getParameter("banner"));
		String footer = Utils.sanitize((String)request.getParameter("footer"));
		String background = Utils.sanitize((String)request.getParameter("background"));
		String facebookLogin = Utils.sanitize((String)request.getParameter("facebookLogin"));
		String showLink = Utils.sanitize((String)request.getParameter("showLink"));
		String loginBanner = Utils.sanitize((String)request.getParameter("loginBanner"));
		String showAds = Utils.sanitize((String)request.getParameter("showAds"));
		if (debug != null && !debug.equals("false")) {
			loginBean.setEmbeddedDebug(true);
		}
		if (embedded != null && !embedded.equals("false")) {
			proxy.setBeanId(null);
			proxy.getLoginBean().setEmbedded(true);
			if (css != null) {
				proxy.getLoginBean().setCssURL(css);
			}
			if (banner != null) {
				proxy.getLoginBean().setBannerURL(banner);
			}
			if (footer != null) {
				proxy.getLoginBean().setFooterURL(footer);
			}
			if (background != null) {
				if (((background.length() == 3) || (background.length() == 6)) && ("1234567890aAbBcCdDeEfF".indexOf(background.charAt(0)) != -1)) {
					proxy.getLoginBean().setBackgroundColor("#" + background);
				} else {
					proxy.getLoginBean().setBackgroundColor(background);
				}
			}
			proxy.getLoginBean().setFacebookLogin(facebookLogin == null || !facebookLogin.equals("false"));
			proxy.getLoginBean().setShowLink(showLink == null || !showLink.equals("false"));
			proxy.getLoginBean().setLoginBanner(loginBanner == null || !loginBanner.equals("false"));
			proxy.getLoginBean().setShowAds(showAds != null && !showAds.equals("false"));
			long tokenValue = 0;
			if (token != null) {
				try {
					tokenValue = Long.valueOf(token);
				} catch (Exception ignore) {
					proxy.getLoginBean().setError(ignore);
				}
			}
			if (user != null) {
				proxy.getLoginBean().validateUser(user, password, tokenValue, false, false);
			} else if (loginBean != null) {
				proxy.getLoginBean().setUser(loginBean.getUser());
				proxy.getLoginBean().setLoggedIn(loginBean.isLoggedIn());
			}
			loginBean = proxy.getLoginBean();
		} else if (value != null) {
			loginBean = proxy.getLoginBean();
		}
		if (loginBean == null) {
			loginBean = new LoginBean();
			request.getSession().setAttribute("loginBean", loginBean);
			loginBean.initialize(getServletContext(), request, response);
		}
		return loginBean;
	}
	
	/**
	 * An HTTP session timeout occurred.
	 * Set the error and redirect to index.jsp
	 */
	public void httpSessionTimeout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LoginBean loginBean = new LoginBean();
		loginBean.setError(new BotException("HTTP session timout"));
		request.getSession().setAttribute("loginBean", loginBean);
		request.getRequestDispatcher("index.jsp").forward(request, response);
		return;
	}
	
	public String getFileName(Part part) {
		for (String cd : part.getHeader("content-disposition").split(";")) {
			if (cd.trim().startsWith("filename")) {
				return cd.substring(cd.indexOf('=') + 1).trim()
						.replace("\"", "");
			}
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public boolean checkCommon(WebMediumBean bean, String url, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String postToken = (String)request.getParameter("postToken");
		String unflag = (String)request.getParameter("unflag");
		String flag = (String)request.getParameter("flag");
		String thumbsup = (String)request.getParameter("thumbs-up");
		String thumbsdown = (String)request.getParameter("thumbs-down");
		String star1 = (String)request.getParameter("star1");
		String star2 = (String)request.getParameter("star2");
		String star3 = (String)request.getParameter("star3");
		String star4 = (String)request.getParameter("star4");
		String star5 = (String)request.getParameter("star5");
		String flagged = (String)request.getParameter("flagged");
		String flagReason = Utils.sanitize((String)request.getParameter("flag-reason"));
		String resetIcon = (String)request.getParameter("reset-icon");
		if (resetIcon != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.resetIcon();
			response.sendRedirect(url);
			return true;
		}
		if (flag != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.flagInstance(flagReason, flagged);
			response.sendRedirect(url);
			return true;
		}
		if (unflag != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			try {
				bean.unflagInstance();
			} catch (Exception failed) {
				bean.error(failed);
			}
			response.sendRedirect(url);
			return true;
		}
		if (thumbsup != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.thumbsUp();
			response.sendRedirect(url);
			return true;
		}
		if (thumbsdown != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.thumbsDown();
			response.sendRedirect(url);
			return true;
		}
		if (star1 != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.star(1);
			response.sendRedirect(url);
			return true;
		}
		if (star2 != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.star(2);
			response.sendRedirect(url);
			return true;
		}
		if (star3 != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.star(3);
			response.sendRedirect(url);
			return true;
		}
		if (star4 != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.star(4);
			response.sendRedirect(url);
			return true;
		}
		if (star5 != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.star(5);
			response.sendRedirect(url);
			return true;
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	public boolean checkSearchCommon(WebMediumBean bean, String url, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String postToken = (String)request.getParameter("postToken");
		String deleteAll = (String)request.getParameter("delete-all");
		boolean deleteConfirm = "on".equals(request.getParameter("delete-confirm"));
		if (deleteAll != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.deleteAll(request, deleteConfirm);
			response.sendRedirect(url);
			return true;
		}
		String reviewAll = (String)request.getParameter("review-all");
		if (reviewAll != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.reviewAll(request);
			response.sendRedirect(url);
			return true;
		}
		String hideAll = (String)request.getParameter("hide-all");
		if (hideAll != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.hideAll(request);
			response.sendRedirect(url);
			return true;
		}
		String privateAll = (String)request.getParameter("private-all");
		if (privateAll != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.privateAll(request);
			response.sendRedirect(url);
			return true;
		}

		String page = (String)request.getParameter("page");
		String userFilter = (String)request.getParameter("user-filter");
		if (page != null) {
			bean.setPage(Integer.valueOf(page));
			request.getRequestDispatcher(url).forward(request, response);
			return true;
		}
		if (userFilter != null) {
			bean.resetSearch();
			bean.setUserFilter(userFilter);
			bean.setInstanceFilter(InstanceFilter.Personal);
			request.getRequestDispatcher(url).forward(request, response);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("rawtypes")
	public boolean checkUserAdmin(WebMediumBean bean, String url, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String postToken = (String)request.getParameter("postToken");
		String addUser = (String)request.getParameter("addUser");
		String newUser = Utils.sanitize((String)request.getParameter("newUser"));
		if (addUser != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.addUser(newUser);
			response.sendRedirect(url);
			return true;
		}
		String removeUser = (String)request.getParameter("removeUser");
		String user = Utils.sanitize((String)request.getParameter("selected-user"));
		if (removeUser != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.removeUser(user);
			response.sendRedirect(url);
			return true;
		}
		String addAdmin = (String)request.getParameter("addAdmin");
		String newAdmin = Utils.sanitize((String)request.getParameter("newAdmin"));
		if (addAdmin != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.addAdmin(newAdmin);
			response.sendRedirect(url);
			return true;
		}
		String removeAdmin = (String)request.getParameter("removeAdmin");
		String selectedAdmin = Utils.sanitize((String)request.getParameter("selected-admin"));
		if (removeAdmin != null) {
			bean.getLoginBean().verifyPostToken(postToken);
			bean.removeAdmin(selectedAdmin);
			response.sendRedirect(url);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("rawtypes")
	public void setSearchFields(HttpServletRequest request, BrowseBean bean) {
		String instanceFilter = Utils.sanitize((String)request.getParameter("instance-filter"));
		String instanceRestrict = Utils.sanitize((String)request.getParameter("instance-restrict"));
		String nameFilter = Utils.sanitize((String)request.getParameter("name-filter"));
		String categoryFilter = Utils.sanitize((String)request.getParameter("category-filter"));
		String tagFilter = Utils.sanitize((String)request.getParameter("tag-filter"));
		String startFilter = Utils.sanitize((String)request.getParameter("start-filter"));
		String endFilter = Utils.sanitize((String)request.getParameter("end-filter"));
		String pageSize = Utils.sanitize((String)request.getParameter("page-size"));
		String instanceSort = Utils.sanitize((String)request.getParameter("instance-sort"));
		String contentRating = Utils.sanitize((String)request.getParameter("content-rating"));  
		String displayOption = Utils.sanitize((String)request.getParameter("display"));
		String myAvatars = Utils.sanitize((String)request.getParameter("my-instances"));
		if (myAvatars != null) {
			instanceFilter = "personal";
		}
		bean.setPage(0);
		bean.setResultsSize(0);
		bean.setUserFilter(null);
		
		if ("private".equals(instanceFilter)) {
			bean.setInstanceFilter(InstanceFilter.Private);
		} else if ("public".equals(instanceFilter)) {
			bean.setInstanceFilter(InstanceFilter.Public);
		} else if ("adult".equals(instanceFilter)) {
			bean.setInstanceFilter(InstanceFilter.Adult);
		} else if ("personal".equals(instanceFilter)) {
			bean.setInstanceFilter(InstanceFilter.Personal);
		}
		if (instanceSort != null) {
			if (instanceSort.equalsIgnoreCase("DayConnects")) {
				instanceSort = InstanceSort.DailyConnects.name();
			} else if (instanceSort.equalsIgnoreCase("WeekConnects")) {
				instanceSort = InstanceSort.WeeklyConnects.name();
			} else if (instanceSort.equalsIgnoreCase("MonthConnects")) {
				instanceSort = InstanceSort.MonthlyConnects.name();
			}
			bean.setInstanceSort(InstanceSort.valueOf(Utils.capitalize(instanceSort)));
		}
		if (instanceRestrict != null) {
			bean.setInstanceRestrict(InstanceRestrict.valueOf(Utils.capitalize(instanceRestrict)));
		}
		if (contentRating != null) {
			bean.getLoginBean().setContentRating(ContentRating.valueOf(Utils.capitalize(contentRating)));
		}

		if (categoryFilter != null) {
			bean.setCategoryFilter(categoryFilter);
		}
		if (tagFilter != null) {
			bean.setTagFilter(tagFilter);
		}
		if (nameFilter != null) {
			bean.setNameFilter(nameFilter);
		}
		if (startFilter != null) {
			bean.setStartFilter(startFilter);
		}
		if (endFilter != null) {
			bean.setEndFilter(endFilter);
		}
		if (pageSize != null && bean.isSuper()) {
			bean.setPageSize(Integer.valueOf(pageSize));
		}
		if ("grid".equals(displayOption)) {
			bean.setDisplayOption(DisplayOption.Grid);
		} else if ("details".equals(displayOption)) {
			bean.setDisplayOption(DisplayOption.Details);
		} else if ("header".equals(displayOption)) {
			bean.setDisplayOption(DisplayOption.Header);
		}
	}
	
	public void updateParameters(WebMediumConfig config, HttpServletRequest request) {
		config.name = (String)request.getParameter("name");
		config.alias = (String)request.getParameter("alias");
		config.creator = (String)request.getParameter("creator");
		config.description = (String)request.getParameter("description");
		config.details = (String)request.getParameter("details");
		config.disclaimer = (String)request.getParameter("disclaimer");
		config.website = (String)request.getParameter("website");
		config.license = (String)request.getParameter("license");
		config.tags = (String)request.getParameter("tags");
		config.categories = (String)request.getParameter("categories");
		config.isPrivate = "on".equals((String)request.getParameter("private"));
		config.isAdult = "on".equals((String)request.getParameter("isAdult"));
		config.isHidden = "on".equals((String)request.getParameter("hidden"));
		config.accessMode = (String)request.getParameter("accessMode");
		config.forkAccessMode = (String)request.getParameter("forkAccessMode");
		config.contentRating = (String)request.getParameter("contentRating");
		config.isReviewed = "on".equals((String)request.getParameter("isReviewed"));
		config.reviewRejectionComments = (String)request.getParameter("reviewRejectionComments");
		config.showAds = "on".equals((String)request.getParameter("showAds"));
		config.adCode = (String)request.getParameter("adCode");
	}
	
	@SuppressWarnings("rawtypes")
	public void uploadImage(HttpServletRequest request, WebMediumBean bean) {
		try {
			String postToken = (String)request.getParameter("postToken");
			bean.getLoginBean().verifyPostToken(postToken);
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (bean.getInstance() == null || !String.valueOf(bean.getInstanceId()).equals(instance)) {
					bean.validateInstance(instance);
				}
			}
			bean.checkLogin();
			bean.checkInstance();
			bean.checkAdmin();
			String uploadURL = (String)request.getParameter("upload-url");
			if (uploadURL != null && uploadURL.trim().length() > 0) {
				URL url = BeanServlet.safeURL(uploadURL);
				URLConnection connection = null;
				try {
					connection = url.openConnection();
				} catch (Exception exception) {
					throw new BotException("Invalid URL");
				}
				byte[] image = BotBean.loadImageFile(connection.getInputStream());
				bean.update(image);
			} else {
				byte[] image = null;
				Collection<Part> files = request.getParts();
				int count = 0;
				for (Part filePart : files) {
					if (!filePart.getName().equals("file")) {
						continue;
					}
					if (filePart != null) {
						InputStream stream = filePart.getInputStream();
						image = BotBean.loadImageFile(stream);
					}
					if ((image == null) || (image.length == 0)) {
						continue;
					}
					bean.update(image);
					count++;
				}
				if (count == 0) {
					throw new BotException("Please select the image file to upload");
				}
			}
		} catch (Throwable failed) {
			bean.getLoginBean().error(failed);
		}
	}
}

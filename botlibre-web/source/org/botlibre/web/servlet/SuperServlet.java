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
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.ContentRating;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.bean.DomainBean;
import org.botlibre.web.bean.IRCBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.rest.DomainConfig;
import org.botlibre.web.service.EmailService;
import org.botlibre.web.service.ForgetfulnessService;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/super")
@SuppressWarnings("serial")
public class SuperServlet extends BeanServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PageStats.page(request);
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		LoginBean loginBean = (LoginBean)request.getSession().getAttribute("loginBean");
		if (loginBean == null) {
			response.sendRedirect("index.jsp");
			return;
		}
		
		try {
			String postToken = (String)request.getParameter("postToken");
			String bootstrap = (String)request.getParameter("bootstrap");
			String password = (String)request.getParameter("database-password");
			if (Site.BOOTSTRAP && bootstrap != null && password != null && loginBean.getBootstrap()) {
				loginBean.verifyPostToken(postToken);
				Site.DATABASEPASSWORD = password;
				AdminDatabase.instance().updatePlatformSettings();
			}
			if (!loginBean.isSuperUser()) {
				response.sendRedirect("index.jsp");
				return;
			}
			String freeMemory = (String)request.getParameter("freeMemory");
			if (freeMemory != null) {
				loginBean.verifyPostToken(postToken);
				IRCBean.rooms.clear();
				ForgetfulnessService.freeMemory();
			}
			String runForgetfullness = (String)request.getParameter("runForgetfullness");
			if (runForgetfullness != null) {
				loginBean.verifyPostToken(postToken);
				ForgetfulnessService service = new ForgetfulnessService();
				ForgetfulnessService.runForgetfullness(service);
			}
			String migrate = (String)request.getParameter("migrate");
			if (migrate != null) {
				loginBean.verifyPostToken(postToken);
				loginBean.migrate();
			}
			String dropDead = (String)request.getParameter("dropDead");
			if (dropDead != null) {
				loginBean.verifyPostToken(postToken);
				loginBean.dropDead();
			}
			String initDatabase = (String)request.getParameter("initDatabase");
			if (initDatabase != null) {
				loginBean.verifyPostToken(postToken);
				loginBean.initDatabase();
			}
			String archiveInactive = (String)request.getParameter("archiveInactive");
			if (archiveInactive != null) {
				loginBean.verifyPostToken(postToken);
				loginBean.archiveInactive();
			}
			String cleanupJunk = (String)request.getParameter("cleanupJunk");
			if (cleanupJunk != null) {
				loginBean.verifyPostToken(postToken);
				loginBean.cleanupJunk();
			}
			String verifyEmail = (String)request.getParameter("verifyEmail");
			if (verifyEmail != null) {
				loginBean.verifyPostToken(postToken);
				loginBean.verifyAllUnverifiedEmail();
				response.sendRedirect("browse-user.jsp");
				return;
			}
			String allstats = (String)request.getParameter("allstats");
			if (allstats != null) {
				request.getRequestDispatcher("stats-all.jsp").forward(request, response);
				return;
			}
			String translations = (String)request.getParameter("translations");
			if (translations != null) {
				request.getRequestDispatcher("translation.jsp").forward(request, response);
				return;
			}
			String clearBanned = (String)request.getParameter("clearBanned");
			if (clearBanned != null) {
				loginBean.verifyPostToken(postToken);
				AdminDatabase.instance().clearBannedUsers();
			}
			String botstats = (String)request.getParameter("botstats");
			if (botstats != null) {
				loginBean.setSelectedStats(botstats);
				request.getRequestDispatcher("stats-bot.jsp").forward(request, response);
				return;
			}
			String livechatstats = (String)request.getParameter("livechatstats");
			if (livechatstats != null) {
				loginBean.setSelectedStats(livechatstats);
				request.getRequestDispatcher("stats-livechat.jsp").forward(request, response);
				return;
			}
			String ipstats = (String)request.getParameter("ipstats");
			if (ipstats != null) {
				loginBean.setSelectedStats(ipstats);
				request.getRequestDispatcher("stats-ip.jsp").forward(request, response);
				return;
			}
			String agentstats = (String)request.getParameter("agentstats");
			if (agentstats != null) {
				loginBean.setSelectedStats(agentstats);
				request.getRequestDispatcher("stats-agent.jsp").forward(request, response);
				return;
			}
			String appstats = (String)request.getParameter("appstats");
			if (appstats != null) {
				loginBean.setSelectedStats(appstats);
				request.getRequestDispatcher("stats-app.jsp").forward(request, response);
				return;
			}
			String pagestats = (String)request.getParameter("pagestats");
			if (pagestats != null) {
				loginBean.setSelectedStats(pagestats);
				request.getRequestDispatcher("stats-page.jsp").forward(request, response);
				return;
			}
			String referstats = (String)request.getParameter("referstats");
			if (referstats != null) {
				loginBean.setSelectedStats(referstats);
				request.getRequestDispatcher("stats-refer.jsp").forward(request, response);
				return;
			}
			String errorstats = (String)request.getParameter("errorstats");
			if (errorstats != null) {
				loginBean.setSelectedStats(errorstats);
				request.getRequestDispatcher("stats-error.jsp").forward(request, response);
				return;
			}
			String filter = (String)request.getParameter("filter");
			if (filter != null) {
				loginBean.setUserFilter(filter);
				response.sendRedirect("browse-user.jsp");
				return;
			}
			String settings = (String)request.getParameter("settings");
			if (settings != null) {
				loginBean.verifyPostToken(postToken);
				Site.URL_PREFIX = request.getParameter("URL_PREFIX");
				Site.URL_SUFFIX = request.getParameter("URL_SUFFIX");
				Site.SERVER_NAME = request.getParameter("SERVER_NAME");
				Site.SERVER_NAME2 = request.getParameter("SERVER_NAME2");
				Site.URL = request.getParameter("URL");
				Site.URLLINK = request.getParameter("URLLINK");
				Site.SECUREURLLINK = request.getParameter("SECUREURLLINK");
				Site.SANDBOXURLLINK = request.getParameter("SANDBOXURLLINK");
				Site.REDIRECT = request.getParameter("REDIRECT");
				Site.HTTPS = "on".equals(request.getParameter("HTTPS"));
				
				Site.PYTHONSERVER = request.getParameter("PYTHONSERVER");
				Site.BOOTSTRAP = "on".equals(request.getParameter("BOOTSTRAP"));
				Site.LOCK = "on".equals(request.getParameter("LOCK"));
				Site.READONLY = "on".equals(request.getParameter("READONLY"));
				Site.ADULT = "on".equals(request.getParameter("ADULT"));
				
				Site.CONTENT_RATING = ContentRating.valueOf(request.getParameter("CONTENT_RATING"));
				Site.NAME = request.getParameter("NAME");
				
				String newDomainName = request.getParameter("DOMAIN");
				if (!Site.DOMAIN.equals(newDomainName)) {
					AdminDatabase adminDatabase = AdminDatabase.instance();
					if (!adminDatabase.domainExists(newDomainName) && adminDatabase.domainExists(Site.DOMAIN)) {
						// Alter current default domain's name & alias if alias not already taken
						AdminDatabase.instance().log(Level.INFO, "Changing current default domain's name & alias");
						Domain newDefaultDomain = (Domain) adminDatabase.getDefaultDomain().clone();
						newDefaultDomain.setName(newDomainName);
						newDefaultDomain.setAlias(newDomainName);
						DomainConfig defaultDomainConfig = newDefaultDomain.buildConfig();
						adminDatabase.updateDomain(newDefaultDomain, defaultDomainConfig.tags, defaultDomainConfig.categories);
						adminDatabase.setDefaultDomain(newDefaultDomain);
						Site.DOMAIN = newDefaultDomain.getAlias();
					}
					else if (adminDatabase.domainExists(newDomainName)) {
						// Set the default domain to be the existing one with matching alias
						AdminDatabase.instance().log(Level.INFO, "Set default domain to a different existing domain");
						Domain newDefaultDomain = AdminDatabase.instance().validateDomain(newDomainName);
						adminDatabase.setDefaultDomain(newDefaultDomain);
						Site.DOMAIN = newDomainName;
					}
					else {
						Site.DOMAIN = newDomainName;
					}
					AdminDatabase.instance().log(Level.INFO, "New domain name: " + Site.DOMAIN);
				}
				
				Site.ID = request.getParameter("ID");
				Site.PREFIX = request.getParameter("PREFIX");
				Site.PERSISTENCE_PROTOCOL = request.getParameter("PERSISTENCE_PROTOCOL");
				Site.PERSISTENCE_HOST = request.getParameter("PERSISTENCE_HOST");
				Site.PERSISTENCE_PORT = request.getParameter("PERSISTENCE_PORT");
				Site.PERSISTENCE_UNIT = request.getParameter("PERSISTENCE_UNIT");
				Site.HASHTAG = request.getParameter("HASHTAG");
				Site.TYPE = request.getParameter("TYPE");

				Site.TWITTER = "on".equals(request.getParameter("TWITTER"));
				Site.FACEBOOK = "on".equals(request.getParameter("FACEBOOK"));
				Site.TELEGRAM = "on".equals(request.getParameter("TELEGRAM"));
				Site.SLACK = "on".equals(request.getParameter("SLACK"));
				Site.SKYPE = "on".equals(request.getParameter("SKYPE"));
				Site.WECHAT = "on".equals(request.getParameter("WECHAT"));
				Site.KIK = "on".equals(request.getParameter("KIK"));
				Site.EMAIL = "on".equals(request.getParameter("EMAIL"));
				Site.TIMERS = "on".equals(request.getParameter("TIMERS"));
				Site.FORGET = "on".equals(request.getParameter("FORGET"));
				Site.ADMIN = "on".equals(request.getParameter("ADMIN"));
				
				Site.VERIFYUSERS = "on".equals(request.getParameter("VERIFYUSERS"));
				Site.DEDICATED = "on".equals(request.getParameter("DEDICATED"));
				Site.CLOUD = "on".equals(request.getParameter("CLOUD"));
				Site.COMMERCIAL = "on".equals(request.getParameter("COMMERCIAL"));
				Site.ALLOW_SIGNUP = "on".equals(request.getParameter("ALLOW_SIGNUP"));
				Site.VERIFY_EMAIL = "on".equals(request.getParameter("VERIFY_EMAIL"));
				Site.ANONYMOUS_CHAT = "on".equals(request.getParameter("ANONYMOUS_CHAT"));
				Site.REQUIRE_TERMS = "on".equals(request.getParameter("REQUIRE_TERMS"));
				Site.AGE_RESTRICT = "on".equals(request.getParameter("AGE_RESTRICT"));
				Site.BACKLINK = "on".equals(request.getParameter("BACKLINK"));
				
				Site.WEEKLYEMAIL = "on".equals(request.getParameter("WEEKLYEMAIL"));
				Site.WEEKLYEMAILBOTS = "on".equals(request.getParameter("WEEKLYEMAILBOTS"));
				Site.WEEKLYEMAILCHANNELS = "on".equals(request.getParameter("WEEKLYEMAILCHANNELS"));
				Site.WEEKLYEMAILFORUMS = "on".equals(request.getParameter("WEEKLYEMAILFORUMS"));
				
				Site.EMAILHOST = request.getParameter("EMAILHOST");
				Site.EMAILSALES = request.getParameter("EMAILSALES");
				Site.EMAILPAYPAL = request.getParameter("EMAILPAYPAL");
				Site.SIGNATURE = request.getParameter("SIGNATURE");
				Site.EMAILBOT = request.getParameter("EMAILBOT");
				Site.EMAILSMTPHost = request.getParameter("EMAILSMTPHost");
				Site.EMAILSMTPPORT = Integer.valueOf(request.getParameter("EMAILSMTPPORT"));
				Site.EMAILUSER = request.getParameter("EMAILUSER");
				Site.EMAILPASSWORD = request.getParameter("EMAILPASSWORD");
				Site.EMAILSSL = "on".equals(request.getParameter("EMAILSSL"));
				EmailService.instance().init();

				Site.MEMORYLIMIT = Integer.valueOf(request.getParameter("MEMORYLIMIT"));
				Site.MAX_PROCCESS_TIME = Integer.valueOf(request.getParameter("MAX_PROCCESS_TIME"));
				Site.CONTENT_LIMIT = Integer.valueOf(request.getParameter("CONTENT_LIMIT"));
				Site.MAX_CREATES_PER_IP = Integer.valueOf(request.getParameter("MAX_CREATES_PER_IP"));
				Site.MAX_USER_MESSAGES = Integer.valueOf(request.getParameter("MAX_USER_MESSAGES"));
				Site.MAX_UPLOAD_SIZE = Integer.valueOf(request.getParameter("MAX_UPLOAD_SIZE"));
				Site.MAX_LIVECHAT_MESSAGES = Integer.valueOf(request.getParameter("MAX_LIVECHAT_MESSAGES"));
				Site.MAX_ATTACHMENTS = Integer.valueOf(request.getParameter("MAX_ATTACHMENTS"));
				Site.MAX_TRANSLATIONS = Integer.valueOf(request.getParameter("MAX_TRANSLATIONS"));
				Site.URL_TIMEOUT = Integer.valueOf(request.getParameter("URL_TIMEOUT"));
				Site.MAX_API = Integer.valueOf(request.getParameter("MAX_API"));
				Site.MAX_BRONZE = Integer.valueOf(request.getParameter("MAX_BRONZE"));
				Site.MAX_GOLD = Integer.valueOf(request.getParameter("MAX_GOLD"));
				Site.MAX_PLATINUM = Integer.valueOf(request.getParameter("MAX_PLATINUM"));
				Site.MAX_BOT_CACHE_SIZE = Integer.valueOf(request.getParameter("MAX_BOT_CACHE_SIZE"));
				Site.MAX_BOT_POOL_SIZE = Integer.valueOf(request.getParameter("MAX_BOT_POOL_SIZE"));
				Site.MAXTWEETIMPORT = Integer.valueOf(request.getParameter("MAXTWEETIMPORT"));
				
				Site.TWITTER_OAUTHKEY = request.getParameter("TWITTER_OAUTHKEY");
				Site.TWITTER_OAUTHSECRET = request.getParameter("TWITTER_OAUTHSECRET");
				Site.FACEBOOK_APPID = request.getParameter("FACEBOOK_APPID");
				Site.FACEBOOK_APPSECRET = request.getParameter("FACEBOOK_APPSECRET");
				Site.KEY = request.getParameter("KEY");
				Site.UPGRADE_SECRET = request.getParameter("UPGRADE_SECRET");
				Site.GOOGLEKEY = request.getParameter("GOOGLEKEY");
				Site.GOOGLECLIENTID = request.getParameter("GOOGLECLIENTID");
				Site.GOOGLECLIENTSECRET = request.getParameter("GOOGLECLIENTSECRET");
				Site.MICROSOFT_SPEECH_KEY = request.getParameter("MICROSOFT_SPEECH_KEY");
				Site.RESPONSIVEVOICE_KEY = request.getParameter("RESPONSIVEVOICE_KEY");
				Site.YANDEX_KEY = request.getParameter("YANDEX_KEY");
				
				AdminDatabase.instance().updatePlatformSettings();
			}
			String addTranslation = (String)request.getParameter("addTranslation");
			if (addTranslation != null) {
				loginBean.verifyPostToken(postToken);
				String text = (String)request.getParameter("text");
				String sourceLanguage = (String)request.getParameter("sourceLanguage");
				String targetLanguage = (String)request.getParameter("targetLanguage");
				String translation = (String)request.getParameter("translation");
				loginBean.addTranslation(text, sourceLanguage, targetLanguage, translation);
				response.sendRedirect("translation.jsp");
				return;
			}
			String removeTranslation = (String)request.getParameter("removeTranslations");
			if (removeTranslation != null) {
				loginBean.verifyPostToken(postToken);
				loginBean.removeTranslations(request);
				response.sendRedirect("translation.jsp");
				return;
			}
			String exportTranslations = (String)request.getParameter("exportTranslations");
			if (exportTranslations != null) {
				loginBean.verifyPostToken(postToken);
				loginBean.exportTranslations(response);
				response.sendRedirect("translation.jsp");
				return;
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		response.sendRedirect("super.jsp");
	}
}

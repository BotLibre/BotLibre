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
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.ChatBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.SessionProxyBean;
import org.botlibre.web.service.BeanManager;
import org.botlibre.web.service.IPStats;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet({"/chat", "/ChatServlet"})
@SuppressWarnings("serial")
public class ChatServlet extends BeanServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PageStats.page(request);
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		LoginBean loginBean = (LoginBean)request.getSession().getAttribute("loginBean");
		SessionProxyBean proxy = null;
		Long proxyId = null;
		try {
			proxy = checkProxy(request, response);
			String disconnect = request.getParameter("disconnect");
			if (disconnect != null) {
				LoginBean proxyBean = null;
				if (request.getParameter("proxy") != null) {
					proxyId = Long.valueOf((String)request.getParameter("proxy"));
					proxy.setBeanId(proxyId);
					proxyBean = proxy.getLoginBean();
				}
				request.getRequestDispatcher("chat-done.jsp").forward(request, response);
				if (proxyBean != null) {
					proxyBean.disconnect();
					BeanManager.manager().removeInstance(proxyId);
				}
				return;
			}
			
			String chat = request.getParameter("id");
			String greeting = request.getParameter("greeting");
			if (greeting == null) {
				greeting = request.getParameter("message");
			}
			String input = (String)request.getParameter("input");
			String correction = (String)request.getParameter("correction");
			String offensive = (String)request.getParameter("offensive");
			String learning = (String)request.getParameter("learning");
			String speakValue = (String)request.getParameter("speak");
			String emote = (String)request.getParameter("emote");
			String action = (String)request.getParameter("action");
			String debug = (String)request.getParameter("debug");
			String chatLog = (String)request.getParameter("chatLog");
			String embedded = (String)request.getParameter("embedded");
			String debugLevel = (String)request.getParameter("debugLevel");

			String value = request.getParameter("proxy");
			if (value != null) {
				proxyId = Long.valueOf(value);
				proxy.setBeanId(proxyId);
			}
			Map<String, String> properties = null;
			BotBean botBean = null;
			if (chat != null) {
				if (loginBean != null && loginBean.getBotBean().getInstance() != null
							&& loginBean.getBotBean().getInstance().isExternal()
							&& !loginBean.getBotBean().getInstance().hasAPI()) {
					loginBean.getBotBean().incrementConnects(ClientType.WEB);
					response.sendRedirect(loginBean.getBotBean().getInstance().getWebsiteURL());
					return;
				}
				proxy.setBeanId(null);
				if (embedded != null) {
					loginBean = proxy.cloneLoginBean(loginBean);
					proxyId = proxy.getBeanId();
				}
				
				properties = new HashMap<String, String>();
				properties.put("instance", chat);
				String application = Utils.sanitize((String)request.getParameter("application"));
				properties.put("application", application);
				String user = Utils.sanitize((String)request.getParameter("user"));
				properties.put("user", user);
				String password = Utils.sanitize((String)request.getParameter("password"));
				properties.put("password", password);
				String token = Utils.sanitize((String)request.getParameter("token"));
				properties.put("token", token);
				String css = Utils.sanitize((String)request.getParameter("css"));
				properties.put("css", css);
				String banner = Utils.sanitize((String)request.getParameter("banner"));
				properties.put("banner", banner);
				String footer = Utils.sanitize((String)request.getParameter("footer"));
				properties.put("footer", footer);
				String background = Utils.sanitize((String)request.getParameter("background"));
				properties.put("background", background);
				String focus = Utils.sanitize((String)request.getParameter("focus"));
				properties.put("focus", focus);
				String send = Utils.sanitize((String)request.getParameter("send"));
				properties.put("send", send);
				String prompt = Utils.sanitize((String)request.getParameter("prompt"));
				properties.put("prompt", prompt);
				String info = Utils.sanitize((String)request.getParameter("info"));
				properties.put("info", info);
				String avatar = Utils.sanitize((String)request.getParameter("avatar"));
				properties.put("avatar", avatar);
				String farewell = Utils.sanitize((String)request.getParameter("farewell"));
				String language = Utils.sanitize((String)request.getParameter("language"));
				String translate = Utils.sanitize((String)request.getParameter("translate"));
				properties.put("language", language);
				properties.put("translate", translate);
				properties.put("farewell", farewell);
				properties.put("avatarExpandable", request.getParameter("avatarExpandable"));
				properties.put("static", request.getParameter("static"));
				properties.put("allowCorrection", request.getParameter("allowCorrection"));
				properties.put("allowEmotes", request.getParameter("allowEmotes"));
				properties.put("loginBanner", request.getParameter("loginBanner"));
				properties.put("facebookLogin", request.getParameter("facebookLogin"));
				properties.put("showTitle", request.getParameter("showTitle"));
				properties.put("showLink", request.getParameter("showLink"));
				properties.put("showAds", request.getParameter("showAds"));
				properties.put("bubble", request.getParameter("bubble"));
				properties.put("chatLog", chatLog);
				properties.put("embedded", embedded);
				properties.put("debug", debug);
				properties.put("menubar", request.getParameter("menubar"));
				properties.put("chooseLanguage", request.getParameter("chooseLanguage"));
				properties.put("sendImage", request.getParameter("sendImage"));
				
				if (proxyId != null) {
					proxy.addProperties(proxyId, properties);
				}
			} else {
				// Check if the login bean was lost.
				loginBean = proxy.checkLoginBean(loginBean);
				if (loginBean == null) {
					properties = proxy.getProperties(proxyId);
					if (properties == null) {
						throw new BotException("Session lost, or chat id not specified.");
					}
					loginBean = proxy.getLoginBean();
					proxy.addProperties(proxy.getBeanId(), properties);
				} else {
					botBean = loginBean.getBotBean();
				}
			}
			if (properties != null) {
				loginBean = proxy.checkLoginBean(loginBean);
				if (loginBean == null) {
					loginBean = getEmbeddedLoginBean(request, response);
				}
				embedded = properties.get("embedded");
				debug = properties.get("debug");
				if (embedded != null) {
					proxyId = proxy.getBeanId();
				}
				loginBean.initialize(getServletContext(), request, response);
				botBean = loginBean.getBotBean();
				
				// Reset from properties.
				String instance = properties.get("instance");
				String focus = properties.get("focus");
				String send = properties.get("send");
				String prompt = properties.get("prompt");
				String avatar = properties.get("avatar");
				String farewell = properties.get("farewell");
				String avatarExpandable = properties.get("avatarExpandable");
				String staticHTML = properties.get("static");
				String allowCorrection = properties.get("allowCorrection");
				String allowEmotes = properties.get("allowEmotes");
				String loginBanner = properties.get("loginBanner");
				String facebookLogin = properties.get("facebookLogin");
				String showTitle = properties.get("showTitle");
				String showLink = properties.get("showLink");
				String info = properties.get("info");
				String showMenubar = properties.get("menubar");
				String showSendImage = properties.get("sendImage");
				String showChooseLanguage = properties.get("chooseLanguage");
				String showChatBubble = properties.get("bubble");
				String language = properties.get("language");
				String translate = properties.get("translate");
				chatLog = properties.get("chatLog");
				
				if ("false".equals(focus)) {
					loginBean.setFocus(!"false".equals(focus));
				}
				if (embedded != null && !embedded.equals("false")) {
					loginBean.setEmbedded(true);
				}
				if (debug != null && !debug.equals("false")) {
					loginBean.setEmbeddedDebug(true);
				}
				loginBean.setProperties(properties);
				if (botBean.validateInstance(instance)) {
					loginBean.checkEmbeddedAPI();
					botBean.connect(ClientType.WEB, request);
					botBean.outputAvatar();
				}
				ChatBean bean = loginBean.getBean(ChatBean.class);
				bean.setShowAvatar(!"false".equals(avatar));
				bean.setShowChatLog(!"false".equals(chatLog));
				if (farewell != null) {
					bean.setFarewell(Utils.sanitize(farewell));
				}
				if (greeting != null) {
					bean.setGreeting(Utils.sanitize(greeting));
				}
				if (avatarExpandable != null) {
					bean.setAvatarExpandable(!avatarExpandable.equals("false"));
				}
				if (allowCorrection != null) {
					bean.setAllowCorrection(!allowCorrection.equals("false"));
				}
				if (allowEmotes != null) {
					bean.setAllowEmotes(!allowEmotes.equals("false"));
				}
				if (loginBanner != null) {
					bean.setLoginBanner(loginBanner != null && !loginBanner.equals("false"));
				}
				if (facebookLogin != null) {
					loginBean.setFacebookLogin(!facebookLogin.equals("false"));
				}
				if (showLink != null) {
					loginBean.setShowLink(!showLink.equals("false"));
				}
				if (language != null) {
					loginBean.setLanguage(Utils.sanitize(language));
				}
				if (translate != null) {
					bean.setLanguage(Utils.sanitize(translate));
				}
				if (showTitle != null) {
					bean.setShowTitle(!showTitle.equals("false"));
				}
				if (showMenubar != null) {
					bean.setMenubar(!showMenubar.equals("false"));
				}
				if (showChooseLanguage != null) {
					bean.setShowChooseLanguage(!showChooseLanguage.equals("false"));
				}
				if (showChatBubble != null) {
					bean.setShowChatBubble(!showChatBubble.equals("false"));
				}
				if (showSendImage != null) {
					bean.setSendImage(!showSendImage.equals("false"));
				}
				if (staticHTML != null) {
					bean.setStaticHTML(!staticHTML.equals("false"));
				}
				if (speakValue != null) {
					if ("disable".equals(speakValue)) {
						bean.setSpeak(false);
						bean.setAllowSpeech(false);
					} else {
						bean.setSpeak(!"false".equals(speakValue));
					}
				}
				if (send != null) {
					bean.setSend(Utils.sanitize(send));
				}
				if (prompt != null) {
					bean.setPrompt(Utils.sanitize(prompt));
				}
				if (info != null) {
					bean.setInfo(Utils.sanitize(info));
				}
				if (bean.getStaticHTML() && loginBean.getError() == null) {
					bean.setFirstResponse(true);
					if ((greeting != null) && !greeting.isEmpty()) {
						bean.setResponse(Utils.sanitize(greeting));
						bean.setChatLog(new StringWriter());
						bean.speak();
					} else if (chat != null) {
						bean.greet();
					}
				}
				if (embedded != null && proxy != null && proxyId != null) {
					proxy.setBeanId(proxyId);
				}
				if (staticHTML != null && !staticHTML.equals(false)) {
					request.getRequestDispatcher("chat.jsp").forward(request, response);
					return;
				} else {
					request.getRequestDispatcher("dchat.jsp").forward(request, response);
				}
				return;
			}
			if (!botBean.isConnected()) {
				loginBean.checkEmbeddedAPI();
				botBean.connect(ClientType.WEB, request);
				if (!botBean.isConnected()) {
					throw new BotException("Not connected");
				}
			}
			ChatBean bean = loginBean.getBean(ChatBean.class);
			if (proxy.isRedirect()) {
				// Don't process conversation again on redirect (help, language).
				proxy.setRedirect(false);
				if (bean.getStaticHTML()) {
					request.getRequestDispatcher("chat.jsp").forward(request, response);
					return;
				} else {
					request.getRequestDispatcher("dchat.jsp").forward(request, response);
				}
				return;
			}
			if (chatLog != null) {
				bean.setShowChatLog("true".equals(chatLog));
			} else {
				if ((input != null) && !input.trim().isEmpty()) {
					if ("on".equals(debug)) {
						bean.setDebug(true);
						botBean.clearLog();
						botBean.setLogLevel(debugLevel);
					} else {
						bean.setDebug(false);
					}
					loginBean.checkEmbeddedAPI();
					bean.processEmote(emote);
					bean.processAction(action);
					bean.setStaticHTML(true);
					IPStats.botChats(request);
					bean.processInput(input, "on".equals(correction), "on".equals(offensive), "on".equals(learning));
					boolean speak = "on".equals(speakValue);
					bean.setSpeak(speak);
					botBean.outputAvatar();
				}
				bean.speak();
			}
		} catch (Exception failed) {
			AdminDatabase.instance().log(failed);
			if (loginBean != null) {
				loginBean.setError(failed);
			}
		}
		if (proxy != null && proxyId != null) {
			proxy.setBeanId(proxyId);
		}
		request.getRequestDispatcher("chat.jsp").forward(request, response);
	}
}

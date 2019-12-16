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

import org.botlibre.web.admin.ClientType;
import org.botlibre.web.bean.ChatBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.SessionProxyBean;
import org.botlibre.web.service.IPStats;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/embedded")
@SuppressWarnings("serial")
public class EmbeddedServlet extends BeanServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PageStats.page(request);
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		LoginBean loginBean = null;
		SessionProxyBean proxy = null;
		Long proxyId = null;
		try {
			String chat = request.getParameter("chat");
			String greeting = request.getParameter("greeting");
			if (greeting == null) {
				greeting = request.getParameter("message");
			}
			String input = request.getParameter("input");
			String correction = request.getParameter("correction");
			String speakValue = request.getParameter("speak");
			String disconnect = request.getParameter("disconnect");
			proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
			
			if (disconnect != null) {
				if (proxy != null) {
					loginBean = proxy.getLoginBean();
					loginBean.disconnect();
				}
				request.getRequestDispatcher("embedded.jsp").forward(request, response);
				return;
			}
			if (proxy == null) {
				proxy = checkProxy(request, response);
			}

			String value = request.getParameter("proxy");
			if (value != null) {
				proxyId = Long.valueOf(value);
				proxy.setBeanId(proxyId);
			}
			String popup = request.getParameter("popup");
			if (popup != null) {
				proxy.setBeanId(proxy.getLastBeanId());
			}
			Map<String, String> properties = null;
			BotBean botBean = null;
			if (chat != null) {
				proxy.setBeanId(null);
				loginBean = proxy.getLoginBean();
				proxyId = proxy.getBeanId();
				
				properties = new HashMap<String, String>();
				properties.put("instance", chat);
				String user = Utils.sanitize((String)request.getParameter("user"));
				properties.put("user", user);
				String password = (String)request.getParameter("password");
				properties.put("password", password);
				String token = (String)request.getParameter("token");
				properties.put("token", token);
				String css = Utils.sanitize((String)request.getParameter("css"));
				properties.put("css", css);
				String background = Utils.sanitize((String)request.getParameter("background"));
				properties.put("background", background);
				String focus = Utils.sanitize((String)request.getParameter("focus"));
				properties.put("focus", focus);
				String send = Utils.sanitize((String)request.getParameter("send"));
				properties.put("send", send);
				String prompt = Utils.sanitize((String)request.getParameter("prompt"));
				properties.put("prompt", prompt);
				String avatar = Utils.sanitize((String)request.getParameter("avatar"));
				properties.put("avatar", avatar);
				String farewell = Utils.sanitize((String)request.getParameter("farewell"));
				properties.put("farewell", farewell);
				proxy.addProperties(proxyId, properties);
			} else {
				// Check if the login bean was lost.
				loginBean = proxy.checkLoginBean();
				if (loginBean == null) {
					properties = proxy.getProperties(proxyId);
					if (properties == null) {
						throw new BotException("Session lost, or chat id not specified.");
					}
				} else {
					botBean = loginBean.getBotBean();
				}
			}
			if (properties != null) {
				loginBean = proxy.getLoginBean();
				proxyId = proxy.getBeanId();
				loginBean.initialize(getServletContext(), request, response);
				botBean = loginBean.getBotBean();
				
				// Reset from properties.
				String instance = properties.get("instance");
				String user = properties.get("user");
				String password = properties.get("password");
				String token = properties.get("token");
				String css = properties.get("css");
				String background = properties.get("background");
				String focus = properties.get("focus");
				String send = properties.get("send");
				String prompt = properties.get("prompt");
				String avatar = properties.get("avatar");
				String farewell = properties.get("farewell");
				
				if ("false".equals(focus)) {
					proxy.getLoginBean().setFocus(false);
				}
				if (css != null) {
					proxy.getLoginBean().setCssURL(css);
				}
				if (background != null) {
					if (((background.length() == 3) || (background.length() == 6)) && ("1234567890aAbBcCdDeEfF".indexOf(background.charAt(0)) != -1)) {
						proxy.getLoginBean().setBackgroundColor("#" + background);						
					} else {
						proxy.getLoginBean().setBackgroundColor(background);
					}
				}
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
				}
				botBean.validateInstance(instance);
				botBean.connect(ClientType.WEB, request);
				botBean.outputAvatar();
				ChatBean bean = loginBean.getBean(ChatBean.class);
				bean.setShowAvatar(!"false".equals(avatar));
				if (farewell != null) {
					bean.setFarewell(farewell);
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
					bean.setSend(send);
				}
				if (prompt != null) {
					bean.setPrompt(prompt);
				}
				if ((greeting != null) && !greeting.isEmpty()) {
					bean.setResponse(greeting);
					bean.setChatLog(new StringWriter());
				    bean.speak();
				} else if (chat != null) {
					bean.greet();
				}
				if (proxy != null && proxyId != null) {
					proxy.setBeanId(proxyId);
				}
				if (chat != null) {
					request.getRequestDispatcher("embedded.jsp").forward(request, response);
					return;
				}
			}
			if (!botBean.isConnected()) {
				botBean.connect(ClientType.WEB, request);
				if (!botBean.isConnected()) {
					throw new BotException("Not connected");
				}
			}
			String chatLog = (String)request.getParameter("chatLog");
			ChatBean bean = loginBean.getBean(ChatBean.class);
			if (chatLog != null) {
				bean.setShowChatLog("true".equals(chatLog));
			} else {
				if ((input != null) && !input.trim().isEmpty()) {
					IPStats.botChats(request);
					bean.processInput(input, "on".equals(correction), false, null);
					boolean speak = "on".equals(speakValue);
					bean.setSpeak(speak);
					botBean.outputAvatar();
				}
				bean.speak();
			}
		} catch (Throwable failed) {
			if (loginBean == null) {
				loginBean = new LoginBean();
			}
			loginBean.error(failed);
		}
		if (proxy != null && proxyId != null) {
			proxy.setBeanId(proxyId);
		}
		request.getRequestDispatcher("embedded.jsp").forward(request, response);
	}
}

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

import org.botlibre.BotException;

import org.botlibre.web.bean.EmbedTabBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/embed")
@SuppressWarnings("serial")
public class EmbedTabServlet extends BeanServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PageStats.page(request);
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		LoginBean loginBean = (LoginBean)request.getSession().getAttribute("loginBean");
		if (loginBean == null) {
			response.sendRedirect("index.jsp");
			return;
		}
		BotBean botBean = loginBean.getBotBean();
		EmbedTabBean bean = loginBean.getBean(EmbedTabBean.class);

		try {
			String postToken = (String)request.getParameter("postToken");
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (botBean.getInstance() == null || !String.valueOf(botBean.getInstanceId()).equals(instance)) {
					botBean.validateInstance(instance);
				}
			}
			botBean.checkLogin();
			String type = (String)request.getParameter("type");
			String caption = (String)request.getParameter("caption");
			String greeting = (String)request.getParameter("greeting");
			String farewell = (String)request.getParameter("farewell");
			String prompt = (String)request.getParameter("prompt");
			String send = (String)request.getParameter("send");
			String userName = (String)request.getParameter("user");
			String token = (String)request.getParameter("token");
			String password = (String)request.getParameter("password");
			String css = (String)request.getParameter("css");
			String customCss = (String)request.getParameter("customcss");
			String buttonCss = (String)request.getParameter("buttoncss");
			String banner = (String)request.getParameter("banner");
			String footer = (String)request.getParameter("footer");
			String width = (String)request.getParameter("width");
			String height = (String)request.getParameter("height");
			String offset = (String)request.getParameter("offset");
			String boxlocation = (String)request.getParameter("boxlocation");
			String color = (String)request.getParameter("color");
			String background = (String)request.getParameter("background");
			String website = (String)request.getParameter("website");
			String subdomain = (String)request.getParameter("subdomain");
			String chooseLanguage = (String)request.getParameter("chooselanguage");
			
			boolean avatar = "on".equals((String)request.getParameter("avatar"));
			boolean speak = "on".equals((String)request.getParameter("speak"));
			boolean bubble = "on".equals((String)request.getParameter("bubble"));
			boolean allowEmotes = "on".equals((String)request.getParameter("allowEmotes"));
			boolean allowSpeech = "on".equals((String)request.getParameter("allowSpeech"));
			boolean allowCorrection = "on".equals((String)request.getParameter("allowCorrection"));
			boolean loginBanner = "on".equals((String)request.getParameter("loginBanner"));
			boolean avatarExpandable = "on".equals((String)request.getParameter("avatarExpandable"));
			
			boolean staticHTML = "on".equals((String)request.getParameter("static"));
			boolean showAds = "on".equals((String)request.getParameter("showAds"));
			boolean showTitle = "on".equals((String)request.getParameter("showTitle"));
			boolean showLink = "on".equals((String)request.getParameter("showLink"));
			boolean facebookLogin = "on".equals((String)request.getParameter("facebookLogin"));
			boolean promptContactInfo = "on".equals((String)request.getParameter("promptContactInfo"));
			boolean chatLog = "on".equals((String)request.getParameter("chatlog"));
			boolean showAdvancedInfo = "on".equals((String)request.getParameter("showAdvancedInfo"));
			boolean showMenubar = "on".equals((String)request.getParameter("showMenubar"));
			boolean showBoxmax = "on".equals((String)request.getParameter("showBoxmax"));
			boolean showChooseLanguage = "on".equals((String)request.getParameter("showChooseLanguage"));
			boolean showSendImage = "on".equals((String)request.getParameter("showSendImage"));
			String codeToken = (String)request.getParameter("codeToken");
			String code = (String)request.getParameter("code");
			String runCode = (String)request.getParameter("run-code");
			String viewWebsite = (String)request.getParameter("view-on-website");
			if (runCode != null) {
				if (!bean.getCodeToken().equals(codeToken)) {
					throw new BotException("Invalid execution code");
				}
				bean.setCode(code);
				response.setHeader("X-XSS-Protection", "0");
			} else if (viewWebsite != null) {
				bean.setWebsite(website);
				response.sendRedirect("embedwebsite.jsp");
				return;
			} else {
				loginBean.verifyPostToken(postToken);
				bean.generateCode(subdomain, type, caption, greeting, farewell, prompt, send, userName, password, token, css, customCss, buttonCss,
						banner, footer, color, background, width, height, offset, boxlocation, chatLog, chooseLanguage,
						allowEmotes, allowCorrection, loginBanner, avatarExpandable, staticHTML, showAds, 
						avatar, bubble, speak, allowSpeech, facebookLogin, showTitle, showLink, promptContactInfo, showAdvancedInfo,
						showMenubar, showBoxmax, showChooseLanguage, showSendImage);
			}
		} catch (Exception failed) {
			botBean.error(failed);
		}
		response.sendRedirect("embedtab.jsp");
	}
}

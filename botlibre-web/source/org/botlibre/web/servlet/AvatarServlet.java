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

import org.botlibre.web.bean.AvatarBean;
import org.botlibre.web.bean.BrowseBean.InstanceFilter;
import org.botlibre.web.bean.DomainBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.LoginBean.Page;
import org.botlibre.web.bean.SessionProxyBean;
import org.botlibre.web.rest.AvatarConfig;

@javax.servlet.annotation.WebServlet("/avatar")
@SuppressWarnings("serial")
public class AvatarServlet extends BeanServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		LoginBean loginBean = getEmbeddedLoginBean(request, response);
		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		AvatarBean bean = loginBean.getBean(AvatarBean.class);
		loginBean.setActiveBean(bean);
		if (!loginBean.checkDomain(request, response)) {
			return;
		}

		String domain = (String)request.getParameter("domain");
		if (domain != null) {
			DomainBean domainBean = loginBean.getBean(DomainBean.class);
			if (domainBean.getInstance() == null || !String.valueOf(domainBean.getInstanceId()).equals(domain)) {
				domainBean.validateInstance(domain);
			}
		}
		
		String browse = (String)request.getParameter("id");
		if (browse != null) {
			if (proxy != null) {
				proxy.setInstanceId(browse);
			}
			bean.validateInstance(browse);
			request.getRequestDispatcher("avatar.jsp").forward(request, response);
			return;
		}
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		LoginBean loginBean = getLoginBean(request, response);
		if (loginBean == null) {
			httpSessionTimeout(request, response);
			return;
		}
		AvatarBean bean = loginBean.getBean(AvatarBean.class);
		loginBean.setActiveBean(bean);
		Boolean apiKeyError = false;
		
		try {
			String postToken = (String)request.getParameter("postToken");
			if (!loginBean.checkDomain(request, response)) {
				return;
			}
			String domain = (String)request.getParameter("domain");
			if (domain != null) {
				DomainBean domainBean = loginBean.getBean(DomainBean.class);
				if (domainBean.getInstance() == null || !String.valueOf(domainBean.getInstanceId()).equals(domain)) {
					domainBean.validateInstance(domain);
				}
			}
			String cancel = (String)request.getParameter("cancel");
			if (cancel != null) {
				response.sendRedirect("browse-avatar.jsp");
				return;
			}
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (bean.getInstance() == null || !String.valueOf(bean.getInstanceId()).equals(instance)) {
					bean.validateInstance(instance);
				}
			}
			String cancelInstance = (String)request.getParameter("cancel-instance");
			if (cancelInstance != null) {
				response.sendRedirect("avatar?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			String export = (String)request.getParameter("export");
			if (export != null) {
				loginBean.verifyPostToken(postToken);
				if (!bean.export(response)) {
					response.sendRedirect("avatar?id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			String copy = (String)request.getParameter("copy");
			if (copy != null) {
				bean.copyInstance();
				response.sendRedirect("create-avatar.jsp");
				return;
			}
			String createAvatar = (String)request.getParameter("create-avatar");
			String createAvatarLink = (String)request.getParameter("create-avatar-link");
			String createInstance = (String)request.getParameter("create-instance");
			String createLink = (String)request.getParameter("create-link");
			String editAvatar = (String)request.getParameter("edit-avatar");
			String embedAvatar = (String)request.getParameter("embed-avatar");
			String embed = (String)request.getParameter("embed");
			String runEmbedCode = (String)request.getParameter("run-embed-code");
			String editInstance = (String)request.getParameter("edit-instance");
			String saveInstance = (String)request.getParameter("save-instance");
			String deleteInstance = (String)request.getParameter("delete-instance");
			String saveMedia = (String)request.getParameter("save-media");
			String deleteMedia = (String)request.getParameter("delete-media");
			String selectAllMedia = (String)request.getParameter("select-all-media");
			String deleteBackground = (String)request.getParameter("delete-background");
			if (createAvatar != null) {
				response.sendRedirect("create-avatar.jsp");
				return;
			}
			if (createAvatarLink != null) {
				response.sendRedirect("create-avatar-link.jsp");
				return;
			}
			if (editAvatar != null) {
				response.sendRedirect("avatar-editor.jsp");
				return;
			}
			if (embedAvatar != null) {
				response.sendRedirect("avatar-embed.jsp");
				return;
			}
			if (embed != null) {
				loginBean.verifyPostToken(postToken);
				String user = Utils.sanitize((String)request.getParameter("user"));
				String password = Utils.sanitize((String)request.getParameter("password"));
				String token = Utils.sanitize((String)request.getParameter("token"));
				String text = Utils.sanitize((String)request.getParameter("speech"));
				String emotion = Utils.sanitize((String)request.getParameter("emotion"));
				String action = Utils.sanitize((String)request.getParameter("action"));
				String pose = Utils.sanitize((String)request.getParameter("pose"));
				String voice = Utils.sanitize((String)request.getParameter("voice"));
				String voiceMod = Utils.sanitize((String)request.getParameter("voice-mod"));
				String provider = Utils.sanitize((String)request.getParameter("provider"));
				String nativeVoiceName = Utils.sanitize((String)request.getParameter("native-voice-name"));
				String bingSpeechVoice = Utils.sanitize((String)request.getParameter("bingSpeechVoice"));
				String qqSpeechVoice = Utils.sanitize((String)request.getParameter("qqSpeechVoice"));
				String lang = Utils.sanitize((String)request.getParameter("language"));
				String width = Utils.sanitize((String)request.getParameter("width"));
				String height = Utils.sanitize((String)request.getParameter("height"));
				String background = Utils.sanitize((String)request.getParameter("background"));
				String bingApiKey = Utils.sanitize((String)request.getParameter("bingSpeechApiKey"));
				String bingApiEndpoint = Utils.sanitize((String)request.getParameter("bingSpeechApiEndpoint"));
				String qqAppId = Utils.sanitize((String)request.getParameter("qqSpeechAppId"));
				String qqApiKey = Utils.sanitize((String)request.getParameter("qqSpeechApiKey"));
				
				bean.setUserName(user);
				bean.setPassword(password);
				bean.setToken(token);
				bean.setEmbedVoice(voice);
				bean.setEmbedVoiceMod(voiceMod);
				bean.setEmbedNativeVoice(!("botlibre".equals(provider)));
				bean.setEmbedResponsiveVoice("responsive".equals(provider));
				bean.setEmbedBingSpeech("bing".equals(provider));
				bean.setEmbedQQSpeech("qq".equals(provider));
				if (bean.getEmbedBingSpeech()) {
					bean.setEmbedNativeVoiceName(bingSpeechVoice);
					bean.setEmbedNativeVoiceApiKey(bingApiKey);
					bean.setEmbedVoiceApiEndpoint(bingApiEndpoint);
					if (bean.isAdmin()) {
						bean.saveAvatarApiKeys();
					}
					if (bingApiKey == null || bingApiKey.isEmpty()) {
						apiKeyError = true;
						throw new Exception("Bing Speech API key is required.");
					}
				} else if (bean.getEmbedQQSpeech()) {
					bean.setEmbedNativeVoiceName(qqSpeechVoice);
					bean.setEmbedNativeVoiceAppId(qqAppId);
					bean.setEmbedNativeVoiceApiKey(qqApiKey);
					if (bean.isAdmin()) {
						bean.saveAvatarApiKeys();
					}
					if (qqApiKey == null || qqApiKey.isEmpty()) {
						apiKeyError = true;
						throw new Exception("QQ Speech App Key is required.");
					}
					if (qqAppId == null || qqAppId.isEmpty()) {
						apiKeyError = true;
						throw new Exception("QQ Speech App Id is required.");
					}
				} else {
					bean.setEmbedNativeVoiceName(nativeVoiceName);
				}
				bean.setEmbedLang(lang);
				bean.setEmbedSpeech(text.trim());
				bean.setEmbedEmotion(emotion);
				bean.setEmbedAction(action);
				bean.setEmbedPose(pose);
				bean.setEmbedWidth(width);
				bean.setEmbedHeight(height);
				bean.setEmbedBackground(background);
				bean.generateEmbedCode();
				response.sendRedirect("avatar-embed.jsp");
				return;
			}
			if (runEmbedCode != null) {
				loginBean.verifyPostToken(postToken);
				String code = (String)request.getParameter("embedcode");
				bean.setEmbedCode(code);
				// Allow JavaScript on Safari
				response.addHeader("X-XSS-Protection","0");
				response.sendRedirect("avatar-embed.jsp");
				return;
			}
			if (editInstance != null) {
				if (!bean.editInstance(bean.getInstanceId())) {
					response.sendRedirect("avatar?id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("edit-avatar.jsp").forward(request, response);
				return;
			}
			
			if (checkCommon(bean, "avatar?id=" + bean.getInstanceId(), request, response)) {
				return;
			}
			if (checkUserAdmin(bean, "avatar-users.jsp", request, response)) {
				return;
			}
			
			if (saveMedia != null) {
				loginBean.verifyPostToken(postToken);
				bean.saveMedia(request);
				response.sendRedirect("avatar-editor.jsp");
				return;
			}
			if (deleteMedia != null) {
				loginBean.verifyPostToken(postToken);
				bean.deleteMedia(request);
				response.sendRedirect("avatar-editor.jsp");
				return;
			}
			if (deleteBackground != null) {
				loginBean.verifyPostToken(postToken);
				bean.deleteBackground();
				response.sendRedirect("avatar-editor.jsp");
				return;
			}
			if (selectAllMedia != null) {
				bean.setSelectAll(!bean.isSelectAll());
				response.sendRedirect("avatar-editor.jsp");
				return;
			}
			
			String page = (String)request.getParameter("page");
			String userFilter = (String)request.getParameter("user-filter");

			String search = (String)request.getParameter("search-avatar");
			if (search != null) {
				bean.resetSearch();
				bean.setCategoryFilter(bean.getCategoryString());
				request.getRequestDispatcher("avatar-search.jsp").forward(request, response);
				return;
			}
			String category = (String)request.getParameter("category");
			if (category != null) {
				bean.browseCategory(category);
				request.getRequestDispatcher("browse-avatar.jsp").forward(request, response);
				return;
			}
			String create = (String)request.getParameter("create-avatar");
			if (create != null) {
				request.getRequestDispatcher("create-avatar.jsp").forward(request, response);
				return;
			}
			String createCategory = (String)request.getParameter("create-category");
			if (createCategory != null) {
				loginBean.setCategoryType("Avatar");
				loginBean.setActiveBean(bean);
				request.getRequestDispatcher("create-category.jsp").forward(request, response);
				return;
			}
			String editCategory = (String)request.getParameter("edit-category");
			if (editCategory != null) {
				loginBean.setCategoryType("Avatar");
				loginBean.setActiveBean(bean);
				loginBean.setCategory(bean.getCategory());
				request.getRequestDispatcher("edit-category.jsp").forward(request, response);
				return;
			}
			String deleteCategory = (String)request.getParameter("delete-category");
			if (deleteCategory != null) {
				loginBean.verifyPostToken(postToken);
				loginBean.setCategoryType("Avatar");
				loginBean.setActiveBean(bean);
				loginBean.setCategory(bean.getCategory());
				loginBean.deleteCategory();
				response.sendRedirect("browse-avatar.jsp");
				return;
			}
			
			AvatarConfig config = new AvatarConfig();
			updateParameters(config, request);
			String newdomain = (String)request.getParameter("newdomain");
			String delete = (String)request.getParameter("delete");
			String adVerified = (String)request.getParameter("adVerified");
			if (createInstance != null) {
				loginBean.verifyPostToken(postToken);
				config.name = (String)request.getParameter("newInstance");
				if (!bean.createInstance(config)) {
					response.sendRedirect("create-avatar.jsp");
				} else {
					loginBean.setPageType(Page.Browse);
					response.sendRedirect("avatar?id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			if (createLink != null) {
				loginBean.verifyPostToken(postToken);
				config.name = (String)request.getParameter("newInstance");
				if (!bean.createLink(config)) {
					response.sendRedirect("create-avatar-link.jsp");
				} else {
					loginBean.setPageType(Page.Browse);
					response.sendRedirect("avatar?id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			String isFeatured = (String)request.getParameter("isFeatured");
			if (saveInstance != null) {
				try {
					loginBean.verifyPostToken(postToken);
					if (!bean.updateAvatar(config, newdomain, "on".equals(adVerified), "on".equals(isFeatured))) {
						request.getRequestDispatcher("edit-avatar.jsp").forward(request, response);
					} else {
						response.sendRedirect("avatar?id=" + bean.getInstanceId() + proxy.proxyString());
					}
				} catch (Exception failed) {
					bean.error(failed);
					request.getRequestDispatcher("edit-avatar.jsp").forward(request, response);
				}
				return;
			}
			if (deleteInstance != null) {
				loginBean.verifyPostToken(postToken);
				if (!bean.deleteInstance("on".equals(delete))) {
					response.sendRedirect("avatar?id=" + bean.getInstanceId() + proxy.proxyString());
				} else {
					if (loginBean.getPageType() == Page.Search) {
						request.getRequestDispatcher("avatar-search.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("browse-avatar.jsp").forward(request, response);
					}
				}
				return;
			}
			String adminInstance = (String)request.getParameter("admin");
			if (adminInstance != null) {
				if (!bean.adminInstance(bean.getInstanceId())) {
					response.sendRedirect("avatar?id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("admin-avatar.jsp").forward(request, response);
				return;
			}

			if (checkSearchCommon(bean, "avatar-search.jsp", request, response)) {
				return;
			}
			
			setSearchFields(request, bean);
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		if(apiKeyError) {
			request.getRequestDispatcher("avatar-embed.jsp").forward(request, response);
		} else {
			request.getRequestDispatcher("avatar-search.jsp").forward(request, response);
		}
	}
}

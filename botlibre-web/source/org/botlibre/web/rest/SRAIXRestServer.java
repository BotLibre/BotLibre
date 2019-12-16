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
package org.botlibre.web.rest;

import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.bean.ChatBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.service.AppIDStats;
import org.botlibre.web.service.BeanManager;
import org.botlibre.web.service.IPStats;
import org.botlibre.web.service.Stats;

/**
 * Defines the Program AB SRAIX REST API.
 */
@Path("/")
public class SRAIXRestServer {
	
	public SRAIXRestServer() {
	}

	public void error(Throwable exception) {
		AdminDatabase.instance().log(exception);
		if (exception instanceof OutOfMemoryError) {
			AdminDatabase.outOfMemory();
		}
		throw new WebApplicationException(exception, Response.status(Status.BAD_REQUEST)
		         .entity(exception.getMessage())
		         .type(MediaType.TEXT_PLAIN)
		         .build());
	}

	@GET
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_XML)
	@Path("/talk-xml")
	public SRAIXResponse chat(
			@QueryParam("custid") String application,
			@QueryParam("botid") String instance,
			@QueryParam("input") String message,
			@QueryParam("secure") boolean secure,
			@QueryParam("plainText") boolean plainText,
			@Context HttpServletRequest requestContext) {
		
		AdminDatabase.instance().log(Level.INFO, "SRAIX chat", message, application, requestContext.getRemoteAddr());
		if (application == null || application.isEmpty()) {
			try {
				Stats.checkMaxAPI();
				Stats.stats.anonymousAPICalls++;
			} catch (Exception failed) {
				error(failed);
				return null;
			}
		}
		ChatMessage chat = new ChatMessage();
		chat.application = application;
		chat.instance = instance;
		chat.message = message;
		chat.secure = secure;
		chat.plainText = plainText;
		try {
			chat.conversation = Long.valueOf(application);
		} catch (Exception exception) {
			// ignore.
		}
		return chatMessage(chat, requestContext);
	}

	public SRAIXResponse chatMessage(ChatMessage message, HttpServletRequest requestContext) {
		Stats.stats.apiCalls++;
		IPStats.api(requestContext);
		try {
			long conversation = message.conversation;
			boolean firstRequest = false;
			ChatBean chatBean = (ChatBean)BeanManager.manager().getInstance(conversation);
			if (chatBean == null) {
				firstRequest = true;
				LoginBean loginBean = new LoginBean();
				message.connect(loginBean, requestContext);
				BotBean bean = loginBean.getBotBean();
				bean.validateInstance(message.instance);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.connect(ClientType.REST, requestContext);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				chatBean = loginBean.getBean(ChatBean.class);
				conversation = BeanManager.manager().addInstance(chatBean);
			}
			if (!chatBean.getBotBean().isConnected()) {
				chatBean.getBotBean().connect(ClientType.REST, requestContext);
			}
			String appId = chatBean.getLoginBean().getApplicationId();
			String appUser = chatBean.getLoginBean().getAppUser();
			if (message.application != null && !message.application.isEmpty() && !message.application.equals(appId)) {
				appUser = AdminDatabase.instance().validateApplicationId(message.application, null);
			}
			if (appUser != null) {
				AppIDStats stat = AppIDStats.getStats(appId, appUser);
				AppIDStats.checkMaxAPI(stat, appUser, chatBean.getBotBean().getInstanceId(), chatBean.getBotBean().getInstanceName());
				stat.apiCalls++;
			}
			chatBean.setSpeak(false);
			IPStats.botChats(requestContext);
			chatBean.setDebug(false);
			chatBean.processInput(message.message, message.correction, message.offensive, message.learn);
			SRAIXResponse response = new SRAIXResponse();
			response.status = "0";
			response.botid = String.valueOf(chatBean.getBotBean().getInstanceId());
			response.custid = String.valueOf(conversation);
			if (message.secure) {
				response.that = Utils.sanitize(chatBean.getResponse());
			} else {
				response.that = chatBean.getResponse();				
			}
			if (message.plainText) {
				response.that = Utils.stripTags(response.that);
			}
			response.input = message.message;
			if (firstRequest) {
				chatBean.getBotBean().poolInstance();
			}
			return response;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
}

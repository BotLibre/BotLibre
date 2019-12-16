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

import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.sense.BasicSense;
import org.botlibre.sense.alexa.Alexa;
import org.botlibre.sense.facebook.FacebookMessaging;
import org.botlibre.sense.google.GoogleAssistant;
import org.botlibre.sense.http.Http;
import org.botlibre.sense.kik.Kik;
import org.botlibre.sense.skype.Skype;
import org.botlibre.sense.slack.Slack;
import org.botlibre.sense.sms.Twilio;
import org.botlibre.sense.telegram.Telegram;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.sense.wechat.WeChat;
import org.botlibre.thought.language.Language;
import org.botlibre.util.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.Avatar;
import org.botlibre.web.admin.AvatarMedia;
import org.botlibre.web.admin.BotAttachment;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.Category;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.admin.ContentRating;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.admin.Friendship;
import org.botlibre.web.admin.Graphic;
import org.botlibre.web.admin.Tag;
import org.botlibre.web.admin.User;
import org.botlibre.web.admin.UserMessage;
import org.botlibre.web.admin.WebMedium;
import org.botlibre.web.bean.AvatarBean;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.BrowseBean.InstanceFilter;
import org.botlibre.web.bean.BrowseBean.InstanceRestrict;
import org.botlibre.web.bean.BrowseBean.InstanceSort;
import org.botlibre.web.bean.ChatBean;
import org.botlibre.web.bean.ChatLogBean;
import org.botlibre.web.bean.ChatLogImportBean;
import org.botlibre.web.bean.ChatWarBean;
import org.botlibre.web.bean.DomainBean;
import org.botlibre.web.bean.ForumBean;
import org.botlibre.web.bean.ForumPostBean;
import org.botlibre.web.bean.GraphicBean;
import org.botlibre.web.bean.InstanceAvatarBean;
import org.botlibre.web.bean.IssueBean;
import org.botlibre.web.bean.IssueTrackerBean;
import org.botlibre.web.bean.LearningBean;
import org.botlibre.web.bean.LiveChatBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.ScriptBean;
import org.botlibre.web.bean.SelfBean;
import org.botlibre.web.bean.TrainingBean;
import org.botlibre.web.bean.UserBean;
import org.botlibre.web.bean.UserBean.UserFilter;
import org.botlibre.web.bean.UserBean.UserSort;
import org.botlibre.web.bean.UserMessageBean;
import org.botlibre.web.bean.UserToUserMessageBean;
import org.botlibre.web.bean.VoiceBean;
import org.botlibre.web.bean.WebMediumBean;
import org.botlibre.web.chat.ChannelAttachment;
import org.botlibre.web.chat.ChatChannel;
import org.botlibre.web.chat.ChatChannel.ChannelType;
import org.botlibre.web.forum.Forum;
import org.botlibre.web.forum.ForumAttachment;
import org.botlibre.web.forum.ForumPost;
import org.botlibre.web.issuetracker.Issue;
import org.botlibre.web.issuetracker.IssueTracker;
import org.botlibre.web.issuetracker.IssueTrackerAttachment;
import org.botlibre.web.script.Script;
import org.botlibre.web.service.AppIDStats;
import org.botlibre.web.service.BeanManager;
import org.botlibre.web.service.BingSpeech;
import org.botlibre.web.service.BotStats;
import org.botlibre.web.service.BotTranslationService;
import org.botlibre.web.service.IPStats;
import org.botlibre.web.service.License;
import org.botlibre.web.service.Stats;
import org.botlibre.web.servlet.BeanServlet;

import com.sun.jersey.multipart.FormDataParam;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Defines a REST (JAXRS) server to provide web clients a set of BOTlibre
 * services for chat. The can be used from mobile or other client to interact
 * with a BOTlibre instance.
 */
@Path("/json")
public class JSONRestServer {
	
	public JSONRestServer() {
	}

	public void error(Throwable exception) {
		AdminDatabase.instance().log(exception);
		throw new WebApplicationException(exception, Response.status(Status.BAD_REQUEST)
				.entity(String.valueOf(exception.getMessage()))
				.type(MediaType.TEXT_PLAIN)
				.build());
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/twilio/{application}/{instance}")
	public TwilioSMSResponse twilio(
			@PathParam("application") String application,
			@PathParam("instance") String instance,
			@FormParam("MessageSid") String messageSid,
			@FormParam("SmsSid") String smsSid,
			@FormParam("AccountSid") String accountSid,
			@FormParam("messagingServiceSid") String messagingServiceSid,
			@FormParam("From") String from,
			@FormParam("To") String to,
			@FormParam("Body") String body,
			@FormParam("numMedia") String numMedia,
			@FormParam("MediaContentType") String mediaContentType,
			@FormParam("MediaUrl") String mediaUrl,
			@FormParam("FromCity") String fromCity,
			@FormParam("FromState") String fromState,
			@FormParam("FromZip") String fromZip,
			@FormParam("FromCountry") String fromCountry,
			@FormParam("ToCity") String toCity,
			@FormParam("ToState") String toState,
			@FormParam("ToZip") String toZip,
			@FormParam("ToCountry") String toCountry,
			@Context HttpServletRequest requestContext) {
		
		AdminDatabase.instance().log(Level.INFO, "API Twilio SMS", accountSid, from, to, body);

		Stats.stats.botSMSAPI++;
		LoginBean loginBean = new LoginBean();
		BotBean bean = null;
		Bot bot = null;
		InstanceConfig config = new InstanceConfig();
		config.id = instance;
		config.application = application;
		try {
			config.validateApplication(loginBean, null);
			config.user = loginBean.getAppUser();
			loginBean.setUser(new User(config.user, null));
			loginBean.validateUser(config.user, null, 0, false, true);
			loginBean.setLoggedIn(true);
			bean = loginBean.getBotBean();
			bean.validateInstance(config.id);
		} catch (Throwable exception) {
			error(exception);
			return null;
		}
		try {
			if (body == null) {
				body = "";
			}
			bot = bean.connectInstance();
			BotStats stats = BotStats.getStats(bean.getInstanceId(), bean.getInstanceName());
			Twilio sms = bot.awareness().getSense(Twilio.class);
			Language language = bot.mind().getThought(Language.class);
			resetStats(stats, sms, language);
			stats.smsProcessed++;
			Stats.stats.botSMSProcessed++;
			long startTime = System.currentTimeMillis();
			String reply = sms.processMessage(from, body);
			TwilioSMSResponse response = new TwilioSMSResponse();
			if (reply == null) {
				return null;
			} else if (reply.length() > 1600) {
				reply = reply.substring(0, 1600);
			}
			updateStats(stats, sms, language, startTime);
			resetStats(stats, sms, language);
			response.Message = reply;
			stats.smsSent++;
			Stats.stats.botSMSSent++;
			return response;
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
		} finally {
			if (bot != null) {
				int count = 0;
				while ((count < 50) && !bot.memory().getActiveMemory().isEmpty()) {
					count++;
					Utils.sleep(100);
				}
			}
			bean.disconnectInstance();
			loginBean.disconnect();
		}
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/twilio/voice/{application}/{instance}")
	public String twilioVoice(
			@PathParam("application") String application,
			@PathParam("instance") String instance,
			@FormParam("MessageSid") String messageSid,
			@FormParam("SmsSid") String smsSid,
			@FormParam("AccountSid") String accountSid,
			@FormParam("messagingServiceSid") String messagingServiceSid,
			@FormParam("From") String from,
			@FormParam("To") String to,
			@FormParam("Body") String body,
			@FormParam("numMedia") String numMedia,
			@FormParam("MediaContentType") String mediaContentType,
			@FormParam("MediaUrl") String mediaUrl,
			@FormParam("FromCity") String fromCity,
			@FormParam("FromState") String fromState,
			@FormParam("FromZip") String fromZip,
			@FormParam("FromCountry") String fromCountry,
			@FormParam("ToCity") String toCity,
			@FormParam("ToState") String toState,
			@FormParam("ToZip") String toZip,
			@FormParam("ToCountry") String toCountry,
			@FormParam("SpeechResult") String speechResult,
			@FormParam("Confidence") String confidence,
			@Context HttpServletRequest requestContext) {
		
		AdminDatabase.instance().log(Level.INFO, "API Twilio Voice", accountSid, from, to, body, speechResult, confidence);

		Stats.stats.botTwilioVoiceAPI++;
		LoginBean loginBean = new LoginBean();
		BotBean bean = null;
		Bot bot = null;
		InstanceConfig config = new InstanceConfig();
		config.id = instance;
		config.application = application;
		try {
			config.validateApplication(loginBean, null);
			config.user = loginBean.getAppUser();
			loginBean.setUser(new User(config.user, null));
			loginBean.validateUser(config.user, null, 0, false, true);
			loginBean.setLoggedIn(true);
			bean = loginBean.getBotBean();
			bean.validateInstance(config.id);
		} catch (Throwable exception) {
			error(exception);
			return null;
		}
		try {
			if (speechResult == null) {
				speechResult = "";
			}
			bot = bean.connectInstance();
			BotStats stats = BotStats.getStats(bean.getInstanceId(), bean.getInstanceName());
			Twilio twilio = bot.awareness().getSense(Twilio.class);
			Language language = bot.mind().getThought(Language.class);
			resetStats(stats, twilio, language);
			stats.twilioVoiceProcessed++;
			Stats.stats.botTwilioVoiceProcessed++;
			long startTime = System.currentTimeMillis();
			String twiml = twilio.processVoice(from, speechResult);
			if (twiml == null) {
				AdminDatabase.instance().log(Level.INFO, "Twilio null response", accountSid, from, to);
				return null;
			}
			updateStats(stats, twilio, language, startTime);
			resetStats(stats, twilio, language);
			AdminDatabase.instance().log(Level.INFO, "Twilio response", accountSid, from, to, twiml);
			return twiml;
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
		} finally {
			if (bot != null) {
				int count = 0;
				while ((count < 50) && !bot.memory().getActiveMemory().isEmpty()) {
					count++;
					Utils.sleep(100);
				}
			}
			bean.disconnectInstance();
			loginBean.disconnect();
		}
		return null;
	}

	@GET
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/twilio/{application}/{instance}")
	public TwilioSMSResponse twilioGET(
			@PathParam("application") String application,
			@PathParam("instance") String instance,
			@QueryParam("MessageSid") String messageSid,
			@QueryParam("SmsSid") String smsSid,
			@QueryParam("AccountSid") String accountSid,
			@QueryParam("messagingServiceSid") String messagingServiceSid,
			@QueryParam("From") String from,
			@QueryParam("To") String to,
			@QueryParam("Body") String body,
			@QueryParam("numMedia") String numMedia,
			@QueryParam("MediaContentType") String mediaContentType,
			@QueryParam("MediaUrl") String mediaUrl,
			@QueryParam("FromCity") String fromCity,
			@QueryParam("FromState") String fromState,
			@QueryParam("FromZip") String fromZip,
			@QueryParam("FromCountry") String fromCountry,
			@QueryParam("ToCity") String toCity,
			@QueryParam("ToState") String toState,
			@QueryParam("ToZip") String toZip,
			@QueryParam("ToCountry") String toCountry,
			@Context HttpServletRequest requestContext) {
		
		AdminDatabase.instance().log(Level.INFO, "API SMS", accountSid, from, to, body);

		Stats.stats.botSMSAPI++;
		LoginBean loginBean = new LoginBean();
		BotBean bean = null;
		Bot bot = null;
		InstanceConfig config = new InstanceConfig();
		config.id = instance;
		config.application = application;
		try {
			config.validateApplication(loginBean, null);
			config.user = loginBean.getAppUser();
			loginBean.setUser(new User(config.user, null));
			loginBean.validateUser(config.user, null, 0, false, true);
			loginBean.setLoggedIn(true);
			bean = loginBean.getBotBean();
			bean.validateInstance(config.id);
		} catch (Throwable exception) {
			error(exception);
			return null;
		}
		try {
			if (body == null) {
				body = "";
			}
			bot = bean.connectInstance();
			BotStats stats = BotStats.getStats(bean.getInstanceId(), bean.getInstanceName());
			Twilio sms = bot.awareness().getSense(Twilio.class);
			Language language = bot.mind().getThought(Language.class);
			resetStats(stats, sms, language);
			Stats.stats.botSMSProcessed++;
			stats.smsProcessed++;
			long startTime = System.currentTimeMillis();
			String reply = sms.processMessage(from, body);
			TwilioSMSResponse response = new TwilioSMSResponse();
			if (reply == null) {
				return null;
			} else if (reply.length() > 1600) {
				reply = reply.substring(0, 1600);
			}
			updateStats(stats, sms, language, startTime);
			resetStats(stats, sms, language);
			response.Message = reply;
			stats.smsSent++;
			Stats.stats.botSMSSent++;
			return response;
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
		} finally {
			if (bot != null) {
				int count = 0;
				while ((count < 50) && !bot.memory().getActiveMemory().isEmpty()) {
					count++;
					Utils.sleep(100);
				}
			}
			bean.disconnectInstance();
			loginBean.disconnect();
		}
		return null;
	}

	@GET
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/facebook/{application}/{instance}")
	public String getFacebook(
			@PathParam("application") String application,
			@PathParam("instance") String instance,
			@QueryParam("hub.mode") String mode,
			@QueryParam("hub.challenge") String challenge,
			@QueryParam("hub.verify_token") String token,
			@Context HttpServletRequest requestContext) {
		
		AdminDatabase.instance().log(Level.INFO, "API GET facebook", application, instance);
		
		LoginBean loginBean = new LoginBean();
		BotBean bean = null;
		InstanceConfig config = new InstanceConfig();
		config.id = instance;
		config.application = application;
		try {
			config.validateApplication(loginBean, null);
			config.user = loginBean.getAppUser();
			loginBean.setUser(new User(config.user, null));
			loginBean.validateUser(config.user, null, 0, false, true);
			loginBean.setLoggedIn(true);
			bean = loginBean.getBotBean();
			bean.validateInstance(config.id);
		} catch (Throwable exception) {
			error(exception);
			return null;
		}
		return challenge;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/facebook/{application}/{instance}")
	public void facebookJSON(
			@PathParam("application") String application,
			@PathParam("instance") String instance,
			String json,
			@Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API POST JSON facebook");
		AdminDatabase.instance().log(Level.INFO, json);
		
		Stats.stats.botFacebookAPI++;
		LoginBean loginBean = new LoginBean();
		BotBean bean = null;
		Bot bot = null;
		InstanceConfig config = new InstanceConfig();
		config.id = instance;
		config.application = application;
		try {
			config.validateApplication(loginBean, null);
			config.user = loginBean.getAppUser();
			loginBean.setUser(new User(config.user, null));
			loginBean.validateUser(config.user, null, 0, false, true);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			loginBean.setLoggedIn(true);
			bean = loginBean.getBotBean();
			bean.validateInstance(config.id);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
		} catch (Throwable exception) {
			error(exception);
			return;
		}
		try {
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json);
			JSONArray entries = root.getJSONArray("entry");
			
			bot = bean.connectInstance();
		
			Network memory = bot.memory().newMemory();
			for (int index = 0; index < entries.size(); index++) {
				JSONObject entry = entries.getJSONObject(index);
				JSONArray messaging = entry.getJSONArray("messaging");
				for (int index2 = 0; index2 < messaging.size(); index2++) {
					JSONObject message = messaging.getJSONObject(index2);
					//String fromUser = message.getJSONObject("sender").getString("name");
					if (message.get("timestamp") == null || message.get("sender") == null || (message.get("message") == null && message.get("postback") == null)) {
						continue;
					}
					BotStats stats = BotStats.getStats(bean.getInstanceId(), bean.getInstanceName());
					FacebookMessaging facebookMessaging = bot.awareness().getSense(FacebookMessaging.class);
					Language language = bot.mind().getThought(Language.class);
					resetStats(stats, facebookMessaging, language);
					if (!facebookMessaging.getFacebookMessenger()) {
						Exception exception = new BotException("Realtime messages disabled");
						error(exception);
						throw exception;
					}
					String timestamp = message.getString("timestamp");
					String fromUserId = message.getJSONObject("sender").getString("id");
					
					String text = "";
					
					if (message.get("message") != null) {
						String messageText = message.getJSONObject("message").optString("text");
						if (messageText != null) {
							text = messageText.trim();
						} else {
							JSONArray attachments = message.getJSONObject("message").optJSONArray("attachments");
							if (attachments != null && !attachments.isEmpty()) {
								JSONObject attachment = attachments.getJSONObject(0);
								String title = attachment.optString("title");
								if (title != null) {
									text = title.trim();
								}
							}
						}
					} else {
						JSONObject postback = message.optJSONObject("postback");
						if (postback != null) {
							String payload = postback.optString("payload").trim();
							if (payload != null && payload.length() > 0) {
								text = payload;
							}
						}
					}
					
					AdminDatabase.instance().log(Level.INFO, "Processing message", text, fromUserId, timestamp);
					Stats.stats.botFacebookMessagesProcessed++;
					stats.facebookMessagesProcessed++;
					long startTime = System.currentTimeMillis();
					String reply = facebookMessaging.inputFacebookMessengerMessage(text, facebookMessaging.getUserName(), fromUserId, message, memory);
					// Cannot wait too long otherwise Facebook will timeout and resend message.
					//if (reply == null || reply.isEmpty()) {
					//	continue;
					//}
					updateStats(stats, facebookMessaging, language, startTime);
					resetStats(stats, facebookMessaging, language);
					//if (createdTime.getTime() > max) {
					//	max = createdTime.getTime();
					//}
				}
			}
			memory.save();
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
		} finally {
			if (bot != null) {
				int count = 0;
				while ((count < 50) && !bot.memory().getActiveMemory().isEmpty()) {
					count++;
					Utils.sleep(100);
				}
			}
			bean.disconnectInstance();
			loginBean.disconnect();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/telegram/{application}/{instance}")
	public void telegram(
			@PathParam("application") String application,
			@PathParam("instance") String instance,
			String json,
			@Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API POST JSON telegram");
		AdminDatabase.instance().log(Level.INFO, json);

		Stats.stats.botTelegramAPI++;
		LoginBean loginBean = new LoginBean();
		BotBean bean = null;
		Bot bot = null;
		InstanceConfig config = new InstanceConfig();
		config.id = instance;
		config.application = application;
		try {
			config.validateApplication(loginBean, null);
			config.user = loginBean.getAppUser();
			loginBean.setUser(new User(config.user, null));
			loginBean.validateUser(config.user, null, 0, false, true);
			loginBean.setLoggedIn(true);
			bean = loginBean.getBotBean();
			bean.validateInstance(config.id);
		} catch (Throwable exception) {
			error(exception);
			return;
		}
		try {
			JSONObject result = (JSONObject)JSONSerializer.toJSON(json);
			if (result.get("message") == null) {
				return;
			}
			JSONObject message = result.getJSONObject("message");
			
			bot = bean.connectInstance();
			BotStats stats = BotStats.getStats(bean.getInstanceId(), bean.getInstanceName());
			Telegram telegram = bot.awareness().getSense(Telegram.class);
			Language language = bot.mind().getThought(Language.class);
			resetStats(stats, telegram, language);
			if (!telegram.getRealtimeMessages()) {
				Exception exception = new BotException("Realtime messages disabled");
				error(exception);
				throw exception;
			}
			Stats.stats.botTelegramMessagesProcessed++;
			stats.telegramMessagesProcessed++;
			if (message.get("chat") != null) {
				JSONObject chat = message.getJSONObject("chat");
				String chatType = chat.getString("type");
				if (Site.DISABLE_SUPERGROUP && "supergroup".equals(chatType)) {
					// Ignore supergroups as can have too many messages.
					return;
				}
			}
			Network memory = bot.memory().newMemory();
			long startTime = System.currentTimeMillis();
			String reply = telegram.processMessage(message, memory);
			// Cannot wait too long, otherwise Telegram may timeout.
			//if (reply == null || reply.isEmpty()) {
			//	return;
			//}
			updateStats(stats, telegram, language, startTime);
			resetStats(stats, telegram, language);
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
		} finally {
			if (bot != null) {
				int count = 0;
				while ((count < 50) && !bot.memory().getActiveMemory().isEmpty()) {
					count++;
					Utils.sleep(100);
				}
			}
			bean.disconnectInstance();
			loginBean.disconnect();
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GET
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/form-get-all-instances")
	public List<InstanceConfig> getAllInstances(
			@QueryParam("application") String application,
			@QueryParam("tag") String tag,
			@QueryParam("sort") String sort,
			@QueryParam("user") String user,
			@QueryParam("password") String password,
			@QueryParam("token") String token,
			@QueryParam("filterPrivate") boolean filterPrivate,
			@QueryParam("filterAdult") boolean filterAdult, @Context HttpServletRequest requestContext) {
		
		AdminDatabase.instance().log(Level.INFO, "API FORM get-all-instances");
		BrowseConfig config = new BrowseConfig();
		config.type = "Bot";
		config.application = application;
		config.tag = tag;
		config.sort = sort;
		config.filterPrivate = filterPrivate;
		config.filterAdult = filterAdult;
		config.user = user;
		config.password = password;
		config.token = token;
		return (List)getInstances(config, requestContext);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-all-instances")
	public List<InstanceConfig> getAllInstances(BrowseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "get-all-instances");
		config.type = "Bot";
		return (List)getInstances(config, requestContext);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-bots")
	public List<InstanceConfig> getBots(BrowseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-bots");
		config.type = "Bot";
		return (List)getInstances(config, requestContext);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-instances")
	public List<InstanceConfig> getBotInstances(BrowseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-instances");
		config.type = "Bot";
		return (List)getInstances(config, requestContext);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-forums")
	public List<ForumConfig> getForums(BrowseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-forums");
		config.type = "Forum";
		return (List)getInstances(config, requestContext);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-issuetrackers")
	public List<IssueTrackerConfig> getIssueTrackers(BrowseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-issuetrackers");
		config.type = "IssueTracker";
		return (List)getInstances(config, requestContext);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-domains")
	public List<DomainConfig> getDomains(BrowseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-domains");
		config.type = "Domain";
		return (List)getInstances(config, requestContext);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-channels")
	public List<ChannelConfig> getChannels(BrowseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-channels");
		config.type = "Channel";
		return (List)getInstances(config, requestContext);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-graphics")
	public List<GraphicConfig> getGraphics(BrowseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-graphics");
		config.type = "Graphic";
		return (List)getInstances(config, requestContext);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-avatars")
	public List<AvatarConfig> getAvatars(BrowseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-avatars");
		config.type = "Avatar";
		return (List)getInstances(config, requestContext);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-scripts")
	public List<ScriptConfig> getScripts(BrowseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-scripts");
		config.type = "Script";
		return (List)getInstances(config, requestContext);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<WebMediumConfig> getInstances(BrowseConfig config, @Context HttpServletRequest requestContext) {
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			InstanceFilter filter = InstanceFilter.Public;
			InstanceSort sort = InstanceSort.Connects;
			if (config.filterPrivate) {
				filter = InstanceFilter.Private;
			} else if (config.filterAdult) {
				filter = InstanceFilter.Adult;
			} else if ((config.typeFilter != null) && !config.typeFilter.isEmpty()) {
				if (config.typeFilter.equalsIgnoreCase("Private")) {
					filter = InstanceFilter.Private;
				} else if (config.typeFilter.equalsIgnoreCase("Personal")) {
					filter = InstanceFilter.Personal;
				} else if (config.typeFilter.equalsIgnoreCase("Featured")) {
					filter = InstanceFilter.Featured;
				}
			}
			String sortValue = config.sort;
			if (sortValue == null) {
				sortValue = "";
			} else {
				sortValue = sortValue.toLowerCase();
			}
			if ((sortValue.equals("by name") || sortValue.equals("name"))) {
				sort = InstanceSort.Name;
			} else if ((sortValue.equals("by chats today") || sortValue.equals("connects today") || sortValue.equals("dailyconnects"))) {
				sort = InstanceSort.DailyConnects;
			} else if ((sortValue.equals("weeklyconnects") || sortValue.equals("connects this week"))) {
				sort = InstanceSort.WeeklyConnects;
			} else if ((sortValue.equals("monthlyconnects") || sortValue.equals("connects this month"))) {
				sort = InstanceSort.MonthlyConnects;
			} else if (sortValue.equals("date")) {
				sort = InstanceSort.Date;
			} else if (sortValue.equals("stars")) {
				sort = InstanceSort.Stars;
			} else if (sortValue.equals("thumbs up")) {
				sort = InstanceSort.ThumbsUp;
			} else if (sortValue.equals("thumbs down")) {
				sort = InstanceSort.ThumbsDown;
			} else if (sortValue.equals("size")) {
				sort = InstanceSort.Size;
			} else if (sortValue.equals("rank")) {
				sort = InstanceSort.Rank;
			} else if (sortValue.equals("wins")) {
				sort = InstanceSort.Wins;
			} else if (sortValue.equals("losses")) {
				sort = InstanceSort.Losses;
			} else if (sortValue.equals("posts")) {
				sort = InstanceSort.Posts;
			} else if (sortValue.equals("messages")) {
				sort = InstanceSort.Messages;
			} else if (sortValue.equals("users online")) {
				sort = InstanceSort.Users;
			} else if ((sortValue.equals("last connect") || sortValue.equals("lastconnect"))) {
				sort = InstanceSort.LastConnect;
			}
			if (filter == InstanceFilter.Private) {
				if (config.user == null || config.user.length() == 0) {
					throw new BotException("You must sign in first");
				}
			}
			if (filter == InstanceFilter.Personal) {
				if (config.user == null || config.user.length() == 0) {
					if (config.userFilter == null || config.userFilter.length() == 0) {
						throw new BotException("Missing user filter");
					}
				}
			}
			WebMediumBean bean = null;
			if (config.type == null || config.type.length() == 0 || config.type.equals("Bot")) {
				bean = loginBean.getBotBean();
			} else if (config.type.equals("Forum")) {
				bean = loginBean.getBean(ForumBean.class);
			} else if (config.type.equals("Channel")) {
				bean = loginBean.getBean(LiveChatBean.class);
			} else if (config.type.equals("Domain")) {
				bean = loginBean.getBean(DomainBean.class);
			} else if (config.type.equals("Graphic")) {
				bean = loginBean.getBean(GraphicBean.class);
			} else if (config.type.equals("Avatar")) {
				bean = loginBean.getBean(AvatarBean.class);
			} else if (config.type.equals("Script")) {
				bean = loginBean.getBean(ScriptBean.class);
			} else {
				bean = loginBean.getBotBean();
			}
			bean.setInstanceFilter(filter);
			if (config.restrict != null && !config.restrict.isEmpty()) {
				String restrict = config.restrict;
				if (restrict.equals("forkable")) {
					restrict = "Forkable";
				} else if (restrict.equals("has website")) {
					restrict = "Website";
				} else if (restrict.equals("has subdomain")) {
					restrict = "Subdomain";
				} else if (restrict.equals("external link")) {
					restrict = "Link";
				}
				try {
					bean.setInstanceRestrict(InstanceRestrict.valueOf(config.restrict));
				} catch (Exception ignore) {}
			}
			bean.setInstanceSort(sort);
			if ((config.userFilter != null) && !config.userFilter.isEmpty()) {
				bean.setUserFilter(config.userFilter);
			} else if ((config.user != null) && !config.user.isEmpty()) {
				bean.setUserFilter(config.user);
			}
			if (config.page != null && !config.page.isEmpty()) {
				bean.setPage(Integer.valueOf(config.page));
			} else {
				bean.setPage(0);
			}
			if (config.contentRating == null) {
				loginBean.setContentRating(Site.CONTENT_RATING);
			} else {
				loginBean.setContentRating(ContentRating.valueOf(config.contentRating));
			}
			if (config.tag == null) {
				bean.setTagFilter("");
			} else {
				bean.setTagFilter(config.tag);
			}
			if (config.category == null) {
				bean.setCategoryFilter("");
			} else {
				bean.setCategoryFilter(config.category);
			}
			if (config.filter == null) {
				bean.setNameFilter("");
			} else {
				bean.setNameFilter(config.filter);
			}
			List<WebMedium> instances =  bean.getAllSearchInstances();
			return (List)toConfig(instances, bean);
		} catch (Throwable failed) {
			int random = Utils.random(10);
			if (!Site.COMMERCIAL && (random > 6) && failed.getMessage().contains("IP has been banned")) {
				InstanceConfig illegal = new InstanceConfig();
				illegal.name = "Illegal Usage";
				illegal.description = "Please report this app to legal@botlibre.com.  Your IP has been recorded and will be reported.";
				List instances = new ArrayList();
				instances.add(illegal);
				return instances;
			}
			error(failed);
			return null;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-forum-posts")
	public List<ForumPostConfig> getForumPosts(BrowseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-forum-posts");
		IPStats.api(requestContext);
		config.type = "ForumPost";
		try {
			InstanceFilter filter = InstanceFilter.Public;
			InstanceSort sort = InstanceSort.Connects;
			if ((config.typeFilter != null) && !config.typeFilter.isEmpty()) {
				if (config.typeFilter.equalsIgnoreCase("Personal")) {
					filter = InstanceFilter.Personal;
				} else if (config.typeFilter.equalsIgnoreCase("Featured")) {
					filter = InstanceFilter.Featured;
				}
			}
			if ((config.sort != null) && config.sort.equals("name")) {
				sort = InstanceSort.Name;
			} else if ((config.sort != null) && (config.sort.equals("views") || config.sort.equals("connects"))) {
				sort = InstanceSort.Connects;
			} else if ((config.sort != null) && (config.sort.equals("views today") || config.sort.equals("dailyConnects"))) {
				sort = InstanceSort.DailyConnects;
			} else if ((config.sort != null) && (config.sort.equals("weeklyConnects") || config.sort.equals("views this week"))) {
				sort = InstanceSort.WeeklyConnects;
			} else if ((config.sort != null) && (config.sort.equals("monthlyConnects") || config.sort.equals("views this month"))) {
				sort = InstanceSort.MonthlyConnects;
			} else if ((config.sort != null) && config.sort.equals("date")) {
				sort = InstanceSort.Date;
			} else if ((config.sort != null) && config.sort.equals("stars")) {
				sort = InstanceSort.Stars;
			} else if ((config.sort != null) && config.sort.equals("thumbs up")) {
				sort = InstanceSort.ThumbsUp;
			} else if ((config.sort != null) && config.sort.equals("thumbs down")) {
				sort = InstanceSort.ThumbsDown;
			}
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);

			if (filter == InstanceFilter.Private) {
				if (config.user == null || config.user.length() == 0) {
					throw new BotException("You must sign in first");
				}
			}
			if (filter == InstanceFilter.Personal) {
				if (config.user == null || config.user.length() == 0) {
					if (config.userFilter == null || config.userFilter.length() == 0) {
						throw new BotException("Missing user filter");
					}
				}
			}
			ForumPostBean bean = loginBean.getBean(ForumPostBean.class);
			ForumBean forumBean = loginBean.getBean(ForumBean.class);
			bean.setForumBean(forumBean);
			if (config.instance != null) {
				forumBean.validateInstance(config.instance);
			}
			bean.setInstanceFilter(filter);
			bean.setInstanceSort(sort);
			if ((config.userFilter != null) && !config.userFilter.isEmpty()) {
				bean.setUserFilter(config.userFilter);
			} else if ((config.user != null) && !config.user.isEmpty()) {
				bean.setUserFilter(config.user);
			}
			if (config.tag == null) {
				bean.setTagFilter("");
			} else {
				bean.setTagFilter(config.tag);
			}
			if (config.category == null) {
				bean.setCategoryFilter("");
			} else {
				bean.setCategoryFilter(config.category);
			}
			if (config.filter == null) {
				bean.setNameFilter("");
			} else {
				bean.setNameFilter(config.filter);
			}
			if (config.page != null && !config.page.isEmpty()) {
				bean.setPage(Integer.valueOf(config.page));
			} else {
				bean.setPage(0);
			}
			List<ForumPost> instances =  bean.getAllInstances();
			return (List)toConfig(instances, bean);
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-issues")
	public List<IssueConfig> getIssues(BrowseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-issues");
		IPStats.api(requestContext);
		config.type = "Issue";
		try {
			InstanceFilter filter = InstanceFilter.Public;
			InstanceSort sort = InstanceSort.Connects;
			if ((config.typeFilter != null) && !config.typeFilter.isEmpty()) {
				if (config.typeFilter.equalsIgnoreCase("Personal")) {
					filter = InstanceFilter.Personal;
				} else if (config.typeFilter.equalsIgnoreCase("Featured") || config.typeFilter.equalsIgnoreCase("Priority")) {
					filter = InstanceFilter.Featured;
				}
			}
			if ((config.sort != null) && config.sort.equals("name")) {
				sort = InstanceSort.Name;
			} else if ((config.sort != null) && (config.sort.equals("views") || config.sort.equals("connects"))) {
				sort = InstanceSort.Connects;
			} else if ((config.sort != null) && (config.sort.equals("views today") || config.sort.equals("dailyConnects"))) {
				sort = InstanceSort.DailyConnects;
			} else if ((config.sort != null) && (config.sort.equals("weeklyConnects") || config.sort.equals("views this week"))) {
				sort = InstanceSort.WeeklyConnects;
			} else if ((config.sort != null) && (config.sort.equals("monthlyConnects") || config.sort.equals("views this month"))) {
				sort = InstanceSort.MonthlyConnects;
			} else if ((config.sort != null) && config.sort.equals("date")) {
				sort = InstanceSort.Date;
			} else if ((config.sort != null) && config.sort.equals("stars")) {
				sort = InstanceSort.Stars;
			} else if ((config.sort != null) && config.sort.equals("thumbs up")) {
				sort = InstanceSort.ThumbsUp;
			} else if ((config.sort != null) && config.sort.equals("thumbs down")) {
				sort = InstanceSort.ThumbsDown;
			}
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);

			if (filter == InstanceFilter.Private) {
				if (config.user == null || config.user.length() == 0) {
					throw new BotException("You must sign in first");
				}
			}
			if (filter == InstanceFilter.Personal) {
				if (config.user == null || config.user.length() == 0) {
					if (config.userFilter == null || config.userFilter.length() == 0) {
						throw new BotException("Missing user filter");
					}
				}
			}
			IssueTrackerBean issueTrackerBean = loginBean.getBean(IssueTrackerBean.class);
			IssueBean bean = loginBean.getBean(IssueBean.class);
			bean.setIssueTrackerBean(issueTrackerBean);
			if (config.instance != null) {
				issueTrackerBean.validateInstance(config.instance);
			}
			bean.setInstanceFilter(filter);
			bean.setInstanceSort(sort);
			if ((config.userFilter != null) && !config.userFilter.isEmpty()) {
				bean.setUserFilter(config.userFilter);
			} else if ((config.user != null) && !config.user.isEmpty()) {
				bean.setUserFilter(config.user);
			}
			if (config.tag == null) {
				bean.setTagFilter("");
			} else {
				bean.setTagFilter(config.tag);
			}
			if (config.category == null) {
				bean.setCategoryFilter("");
			} else {
				bean.setCategoryFilter(config.category);
			}
			if (config.filter == null) {
				bean.setNameFilter("");
			} else {
				bean.setNameFilter(config.filter);
			}
			if (config.page != null && !config.page.isEmpty()) {
				bean.setPage(Integer.valueOf(config.page));
			} else {
				bean.setPage(0);
			}
			List<Issue> instances =  bean.getAllInstances();
			return (List)toConfig(instances, bean);
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-all-tags")
	public List<TagConfig> getAllTags(@Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-all-tags");
		ContentConfig config = new ContentConfig();
		config.type = "Bot";
		return getTags(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-tags")
	public List<TagConfig> getTags(ContentConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-tags");
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			List<Tag> tags;
			if ("User".equals(config.type)) {
				tags = AdminDatabase.instance().getTags(config.type, null);
			} else {
				tags = AdminDatabase.instance().getTags(config.type, loginBean.getDomain());
			}
			List<TagConfig> names = new ArrayList<TagConfig>(tags.size());
			for (Tag tag : tags) {
				names.add(new TagConfig(tag.getName()));
			}
			return names;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-all-categories")
	public List<CategoryConfig> getAllCategories(@Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-all-categories");
		ContentConfig config = new ContentConfig();
		config.type = "Bot";
		return getCategories(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-categories")
	public List<CategoryConfig> getCategories(ContentConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-categories");
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			List<Category> categories = AdminDatabase.instance().getAllCategories(config.type, loginBean.getDomain());
			List<CategoryConfig> names = new ArrayList<CategoryConfig>(categories.size());
			for (Category category : categories) {
				CategoryConfig categoryConfig = new CategoryConfig(category.getName());
				categoryConfig.description = category.getDescription();
				categoryConfig.icon = loginBean.getAvatarThumb(category);
				names.add(categoryConfig);
			}
			return names;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-instance-users")
	public List<UserConfig> getInstanceUsers(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-instance-users");
		return getUsers(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-forum-users")
	public List<UserConfig> getForumUsers(ForumConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-forum-users");
		return getUsers(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-issuetracker-users")
	public List<UserConfig> getIssueTrackerUsers(IssueTrackerConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-issuetracker-users");
		return getUsers(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-graphic-users")
	public List<UserConfig> getGraphicUsers(GraphicConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-graphic-users");
		return getUsers(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-avatar-users")
	public List<UserConfig> getAvatarUsers(AvatarConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-avatar-users");
		return getUsers(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-script-users")
	public List<UserConfig> getScriptUsers(ScriptConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-script-users");
		return getUsers(config, requestContext);
	}

	@SuppressWarnings("rawtypes")
	public List<UserConfig> getUsers(WebMediumConfig config, @Context HttpServletRequest requestContext) {
		try {
			LoginBean loginBean = new LoginBean();
			WebMediumBean bean = config.validate(loginBean, requestContext);
			bean.checkAdmin();
			try {
				List<UserConfig> users = new ArrayList<UserConfig>();
				for (User user : ((WebMedium)bean.getInstance()).getUsers()) {
					UserConfig userConfig = new UserConfig();
					userConfig.user = user.getUserId();
					users.add(userConfig);
				}
				return users;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-channel-users")
	public List<UserConfig> getChannelUsers(ChannelConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-channel-users");
		return getUsers(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-domain-users")
	public List<UserConfig> getDomainUsers(DomainConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-domain-users");
		return getUsers(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-domain-admins")
	public List<UserConfig> getDomainAdmins(DomainConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-domain-admins");
		return getAdmins(config, requestContext);
	}
	
	@SuppressWarnings("rawtypes")
	public List<UserConfig> getAdmins(WebMediumConfig config, @Context HttpServletRequest requestContext) {
		try {
			LoginBean loginBean = new LoginBean();
			WebMediumBean bean = config.validate(loginBean, requestContext);
			bean.checkAdmin();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			try {
				List<UserConfig> users = new ArrayList<UserConfig>();
				for (User user : ((WebMedium)bean.getInstance()).getAdmins()) {
					UserConfig userConfig = new UserConfig();
					userConfig.user = user.getUserId();					
					users.add(userConfig);
				}
				return users;
			} finally {
				bean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-instance-admins")
	public List<UserConfig> getInstanceAdmins(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-istance-admins");
		return getAdmins(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-forum-admins")
	public List<UserConfig> getForumAdmins(ForumConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-forum-admins");
		return getAdmins(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-issuetracker-admins")
	public List<UserConfig> getIssueTrackerAdmins(IssueTrackerConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-issuetracker-admins");
		return getAdmins(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-avatar-admins")
	public List<UserConfig> getAvatarAdmins(AvatarConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-avatar-admins");
		return getAdmins(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-script-admins")
	public List<UserConfig> getScriptAdmins(ScriptConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-script-admins");
		return getAdmins(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-graphic-admins")
	public List<UserConfig> getGraphicAdmins(GraphicConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-graphic-admins");
		return getAdmins(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-avatar-media")
	public List<AvatarMediaConfig> getAvatarMedia(AvatarConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-avatar-media");
		try {
			LoginBean loginBean = new LoginBean();
			AvatarBean bean = (AvatarBean)config.validate(loginBean, requestContext);
			bean.checkAdmin();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			try {
				List<AvatarMediaConfig> result = new ArrayList<AvatarMediaConfig>();
				for (AvatarMedia media : bean.getInstance().getMedia()) {
					result.add(media.toConfig());
				}
				return result;
			} finally {
				bean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-channel-admins")
	public List<UserConfig> getChannelAdmins(ChannelConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-channel-admins");
		return getAdmins(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-greetings")
	public List<ResponseConfig> getGreetings(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-greetings");
		try {
			LoginBean loginBean = new LoginBean();
			BotBean bean = (BotBean)config.validate(loginBean, requestContext);
			bean.checkAdmin();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.connect(ClientType.REST);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			TrainingBean trainingBean = loginBean.getBean(TrainingBean.class);
			try {
				List<ResponseConfig> greetings = new ArrayList<ResponseConfig>();
				for (Vertex greeting : trainingBean.getGreetings()) {
					greetings.add(new ResponseConfig((String)greeting.getData()));
				}
				return greetings;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-bot-scripts")
	public List<ScriptConfig> getBotScripts(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-bot-scripts");
		try {
			LoginBean loginBean = new LoginBean();
			BotBean bean = (BotBean)config.validate(loginBean, requestContext);
			bean.checkAdmin();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.connect(ClientType.REST);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			SelfBean selfBean = loginBean.getBean(SelfBean.class);
			try {
				List<ScriptConfig> scripts = new ArrayList<ScriptConfig>();
				for (Vertex state : selfBean.getLanguageStateMachines()) {
					ScriptConfig script = new ScriptConfig();
					script.id = String.valueOf(state.getId());
					script.name = state.getName();
					scripts.add(script);
				}
				return scripts;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-responses")
	public List<ResponseConfig> getResponses(ResponseSearchConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-responses");
		try {
			LoginBean loginBean = new LoginBean();
			BotBean bean = (BotBean)config.validate(loginBean, requestContext);
			bean.checkAdmin();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.connect(ClientType.REST);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			
			ChatLogBean chatLogBean = loginBean.getBean(ChatLogBean.class);
			try {
				List<ResponseConfig> responses = chatLogBean.processSearch(config);
				return responses;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-response")
	public ResponseConfig getResponse(ResponseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-response");
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			BotBean bean = loginBean.getBotBean();
			bean.validateInstance(config.instance);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.checkAdmin();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.connect(ClientType.REST);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			ChatLogBean chatLogBean = loginBean.getBean(ChatLogBean.class);
			
			try {
				return chatLogBean.getResponse(config);
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-conversations")
	public List<ConversationConfig> getConversations(ResponseSearchConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-conversations");
		try {
			LoginBean loginBean = new LoginBean();
			BotBean bean = (BotBean)config.validate(loginBean, requestContext);
			bean.checkAdmin();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.connect(ClientType.REST);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			ChatLogBean chatLogBean = loginBean.getBean(ChatLogBean.class);
			try {
				List<ConversationConfig> results = chatLogBean.processConversationSearch(config);
				return results;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-default-responses")
	public List<ResponseConfig> getDefaultResponses(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-default-responses");
		try {
			LoginBean loginBean = new LoginBean();
			BotBean bean = (BotBean)config.validate(loginBean, requestContext);
			bean.checkAdmin();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.connect(ClientType.REST);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			TrainingBean trainingBean = loginBean.getBean(TrainingBean.class);
			try {
				List<ResponseConfig> responses = new ArrayList<ResponseConfig>();
				for (Vertex response : trainingBean.getDefaultResponses()) {
					responses.add(new ResponseConfig((String)response.getData()));
				}
				return responses;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-voice")
	public VoiceConfig getVoice(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-voice");
		try {
			LoginBean loginBean = new LoginBean();
			BotBean bean = (BotBean)config.validate(loginBean, requestContext);
			bean.connect(ClientType.REST);
			VoiceBean voiceBean = loginBean.getBean(VoiceBean.class);
			VoiceConfig voice = new VoiceConfig();
			voice.voice = voiceBean.getVoice();
			voice.mod = voiceBean.getVoiceMod();
			voice.nativeVoice = voiceBean.getNativeVoice();
			voice.language = voiceBean.getLanguage();
			voice.pitch = voiceBean.getPitch();
			voice.speechRate = voiceBean.getSpeechRate();
			return voice;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-user-voice")
	public VoiceConfig getUserVoice(UserConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-user-voice");
		try {
			User user = AdminDatabase.instance().getUser(config.user);
			if (user == null) {
				throw new BotException("User does not exists - " + config.user);
			}
			VoiceConfig voiceConfig = new VoiceConfig();
			voiceConfig.voice = user.getVoice();
			voiceConfig.mod = user.getVoiceMod();
			voiceConfig.nativeVoice = user.isNativeVoice();
			voiceConfig.language = user.getLanguage();
			voiceConfig.pitch = user.getPitch();
			voiceConfig.speechRate = user.getSpeechRate();
			return voiceConfig;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-learning")
	public LearningConfig getLearning(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-voice");
		try {
			LoginBean loginBean = new LoginBean();
			BotBean bean = (BotBean)config.validate(loginBean, requestContext);
			bean.checkLogin();
			bean.checkInstance();
			bean.checkAdmin();
			bean.connect(ClientType.REST);
			LearningBean learningBean = loginBean.getBean(LearningBean.class);
			try {
				LearningConfig learningConfig = new LearningConfig();
				learningConfig.correctionMode = learningBean.getCorrectionMode();
				learningConfig.learningMode = learningBean.getLearningMode();
				learningConfig.enableComprehension = learningBean.getEnableComprehension();
				learningConfig.enableEmoting = learningBean.getEnableEmoting();
				learningConfig.enableEmotions = learningBean.getEnableEmotions();
				learningConfig.allowJavaScript = learningBean.getAllowJavaScript();
				learningConfig.disableFlag = learningBean.getDisableFlag();
				learningConfig.enableConsciousness = learningBean.getEnableConsciousness();
				learningConfig.enableWiktionary = learningBean.getEnableWiktionary();
				learningConfig.enableResponseMatch = learningBean.getEnableResponseMatch();
				learningConfig.learnGrammar = learningBean.getLearnGrammar();
				learningConfig.splitParagraphs = learningBean.getSplitParagraphs();
				learningConfig.synthesizeResponse = learningBean.getSynthesizeResponse();
				learningConfig.fixFormulaCase = learningBean.getFixFormulaCase();
				learningConfig.reduceQuestions = learningBean.getReduceQuestions();
				learningConfig.checkExactMatchFirst = learningBean.getCheckExactMatchFirst();
				learningConfig.scriptTimeout = String.valueOf(learningBean.getStateTimeout());
				learningConfig.responseMatchTimeout = String.valueOf(learningBean.getResponseMatchTimeout());
				learningConfig.conversationMatchPercentage = String.valueOf(learningBean.getConversationMatchPercentage());
				learningConfig.discussionMatchPercentage = String.valueOf(learningBean.getDiscussionMatchPercentage());
				learningConfig.learningRate = String.valueOf(learningBean.getLearningRatePercentage());
				return learningConfig;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-forum-bot-mode")
	public BotModeConfig getForumBotMode(ForumConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-forum-bot-mode");
		try {
			LoginBean loginBean = new LoginBean();
			ForumBean bean = (ForumBean)config.validate(loginBean, requestContext);
			try {
				BotModeConfig botModeConfig = new BotModeConfig();
				botModeConfig.mode = bean.getInstance().getBotMode().name();
				if (bean.getInstance().hasBot()) {
					botModeConfig.bot = bean.getInstance().getBot().getAlias();
				}
				return botModeConfig;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-script-source")
	public ScriptSourceConfig getScriptSource(ScriptConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-script-source");
		try {
			LoginBean loginBean = new LoginBean();
			ScriptBean bean = (ScriptBean)config.validate(loginBean, requestContext);
			try {
				return bean.getInstance().getSourceConfig();
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/delete-bot-script")
	public void deleteBotScript(ScriptSourceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API delete-bot-script");
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			BotBean bean = loginBean.getBotBean();
			bean.validateInstance(config.instance);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.checkAdmin();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.connect(ClientType.REST);
			SelfBean selfBean = loginBean.getBean(SelfBean.class);
			try {
				String[] scriptArray = config.id.split(",");
				for (String scriptId : scriptArray) {
					selfBean.removeSelectedState(scriptId);
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/up-bot-script")
	public void upBotScript(ScriptSourceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API up-bot-script");
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			BotBean bean = loginBean.getBotBean();
			bean.validateInstance(config.instance);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.checkAdmin();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.connect(ClientType.REST);
			SelfBean selfBean = loginBean.getBean(SelfBean.class);
			try {
				String[] scriptArray = config.id.split(",");
				for (String scriptId : scriptArray) {
					selfBean.moveSelectStateUp(scriptId);
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/down-bot-script")
	public void downBotScript(ScriptSourceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API down-bot-script");
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			BotBean bean = loginBean.getBotBean();
			bean.validateInstance(config.instance);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.checkAdmin();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.connect(ClientType.REST);
			SelfBean selfBean = loginBean.getBean(SelfBean.class);
			try {
				String[] scriptArray = config.id.split(",");
				for (String scriptId : scriptArray) {
					selfBean.moveSelectStateDown(scriptId);
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-bot-script-source")
	public ScriptSourceConfig getBotScriptSource(ScriptSourceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-bot-script-source");
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			BotBean bean = loginBean.getBotBean();
			bean.validateInstance(config.instance);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.checkAdmin();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.connect(ClientType.REST);
			SelfBean selfBean = loginBean.getBean(SelfBean.class);
			try {
				selfBean.editState(config.id);
				config.source = selfBean.getStateCode();
				return config;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/save-bot-script-source")
	public void saveBotScriptSource(ScriptSourceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API save-bot-script-source");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				BotBean bean = loginBean.getBean(BotBean.class);
				bean.validateInstance(config.instance);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.checkAdmin();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				SelfBean selfBean = loginBean.getBean(SelfBean.class);
				selfBean.compile(config.source, config.id, false, true);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/import-bot-script")
	public void importBotScript(ScriptConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API import-bot-script");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				BotBean bean = loginBean.getBean(BotBean.class);
				bean.validateInstance(config.instance);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.checkAdmin();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				SelfBean selfBean = loginBean.getBean(SelfBean.class);
				selfBean.importScript(config.id, true, false, false, false, true);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/import-bot-log")
	public void importBotLog(ScriptConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API import-bot-log");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				BotBean bean = loginBean.getBean(BotBean.class);
				bean.validateInstance(config.instance);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.checkAdmin();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				ChatLogImportBean chatLogBean = loginBean.getBean(ChatLogImportBean.class);
				chatLogBean.importChatLog(config.id, false, true);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/save-script-source")
	public void saveScriptSource(ScriptSourceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API save-script-source");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				ScriptBean bean = loginBean.getBean(ScriptBean.class);
				bean.validateInstance(config.instance);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.checkAdmin();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.updateScriptSource(config.source, config.version, config.versionName);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-channel-bot-mode")
	public BotModeConfig getChannelBotMode(ChannelConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-channel-bot-mode");
		try {
			LoginBean loginBean = new LoginBean();
			LiveChatBean bean = (LiveChatBean)config.validate(loginBean, requestContext);
			try {
				BotModeConfig botModeConfig = new BotModeConfig();
				botModeConfig.mode = bean.getInstance().getBotMode().name();
				if (bean.getInstance().hasBot()) {
					botModeConfig.bot = bean.getInstance().getBot().getAlias();
				}
				return botModeConfig;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/save-voice")
	public void saveVoice(VoiceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API save-voice");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				BotBean bean = loginBean.getBotBean();
				bean.validateInstance(config.instance);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.checkAdmin();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				VoiceBean voiceBean = loginBean.getBean(VoiceBean.class);
				voiceBean.save(config.voice, config.mod, config.nativeVoice, config.language, config.pitch, config.speechRate);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/save-user-voice")
	public void saveUserVoice(VoiceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API save-user-voice");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				UserBean userBean = loginBean.getBean(UserBean.class);
				if (config.user != null && config.user.length() > 0) {
					userBean.setUser(AdminDatabase.instance().updateUserInstanceVoice(config.user, config.voice, config.mod, config.nativeVoice, null, "", config.language, config.pitch, config.speechRate, null, null, null));
				}
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/save-bot-avatar")
	public void saveBotAvatar(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API save-bot-avatar");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				BotBean bean = loginBean.getBotBean();
				bean.validateInstance(config.id);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.checkAdmin();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				InstanceAvatarBean avatarBean = loginBean.getBean(InstanceAvatarBean.class);
				avatarBean.chooseAvatar(config.instanceAvatar);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/save-user-avatar")
	public void saveUserAvatar(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API save-user-avatar");
		LoginBean loginBean = new LoginBean();
		try {
			config.connect(loginBean, requestContext);
			UserBean userBean = loginBean.getBean(UserBean.class);
			if (config.instanceAvatar == null) {
				userBean.setUser(AdminDatabase.instance().updateUserInstanceAvatar(config.user, null));
			} else {
				userBean.setUser(AdminDatabase.instance().updateUserInstanceAvatar(config.user, Long.valueOf(config.instanceAvatar)));
			}
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		} finally {
			loginBean.disconnect();
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/save-learning")
	public void saveLearning(LearningConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API save-learning");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				BotBean bean = loginBean.getBotBean();
				bean.validateInstance(config.instance);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.checkAdmin();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.connect(ClientType.REST);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				LearningBean learningBean = loginBean.getBean(LearningBean.class);
				learningBean.save(config.learningMode, config.correctionMode,
						config.scriptTimeout, config.responseMatchTimeout, config.conversationMatchPercentage, config.discussionMatchPercentage,
						config.enableEmoting, config.enableEmotions, config.disableFlag, config.allowJavaScript, config.enableComprehension, config.enableConsciousness, config.enableWiktionary,
						config.enableResponseMatch, config.checkExactMatchFirst, config.checkSynonyms, config.fixFormulaCase, config.reduceQuestions, config.trackCase, config.aimlCompatibility, config.learnGrammar, config.splitParagraphs, 
						config.synthesizeResponse, config.learningRate, config.nlp, config.language, config.fragmentMatchPercentage, config.penalizeExtraWords, config.extraWordPenalty);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/save-forum-bot-mode")
	public void saveForumBotMode(BotModeConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API save-forum-bot-mode");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				ForumBean bean = loginBean.getBean(ForumBean.class);
				bean.validateInstance(config.instance);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.checkAdmin();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.saveBot(config.bot, config.mode);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/save-channel-bot-mode")
	public void saveChannelBotMode(BotModeConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API save-channel-bot-mode");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				LiveChatBean bean = loginBean.getBean(LiveChatBean.class);
				bean.validateInstance(config.instance);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.checkAdmin();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.saveSettings(null, null, config.bot, config.mode, null, null, null, null, null, null, null, null, null, null, null);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}
	
	@SuppressWarnings("rawtypes")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/user-admin")
	public void userAdmin(UserAdminConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API user-admin");
		try {
			if (config.operation == null) {
				return;
			}
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			WebMediumBean bean = null;
			if (config.type == null || config.type.length() == 0 || config.type.equals("Bot")) {
				bean = loginBean.getBotBean();
			} else if (config.type.equals("Forum")) {
				bean = loginBean.getBean(ForumBean.class);
			} else if (config.type.equals("Channel")) {
				bean = loginBean.getBean(LiveChatBean.class);
			} else if (config.type.equals("Domain")) {
				bean = loginBean.getBean(DomainBean.class);
			} else if (config.type.equals("Avatar")) {
				bean = loginBean.getBean(AvatarBean.class);
			} else if (config.type.equals("Script")) {
				bean = loginBean.getBean(ScriptBean.class);
			} else if (config.type.equals("Graphic")) {
				bean = loginBean.getBean(GraphicBean.class);
			} else if (config.type.equals("IssueTracker")) {
				bean = loginBean.getBean(IssueTrackerBean.class);
			}
			bean.validateInstance(config.instance);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.checkAdmin();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			try {
				if (config.operation.equals("AddUser")) {
					bean.addUser(config.operationUser);
				} else if (config.operation.equals("RemoveUser")) {
					String[] userIdArray = config.operationUser.split(",");
					for (String id : userIdArray) {
						bean.removeUser(id);
					}
				} else if (config.operation.equals("AddAdmin")) {
					bean.addAdmin(config.operationUser);
				} else if (config.operation.equals("RemoveAdmin")) {
					bean.removeAdmin(config.operationUser);
				} else if (config.operation.equals("AddOperator")) {
					((LiveChatBean) bean).addOperator(config.operationUser);
				} else if (config.operation.equals("RemoveOperator")) {
					String[] userIdArray = config.operationUser.split(",");
					for (String id : userIdArray) {
						((LiveChatBean) bean).removeOperator(id);
					}
				}
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/train-instance")
	public void trainInstance(TrainingConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API train-instance");
		try {
			if (config.operation == null) {
				return;
			}
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			BotBean bean = loginBean.getBotBean();
			bean.validateInstance(config.instance);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.checkAdmin();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.connect(ClientType.REST);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			TrainingBean trainingBean = loginBean.getBean(TrainingBean.class);
			try {
				if (config.operation.equals("AddGreeting")) {
					trainingBean.addGreeting(config.response);
				} else if (config.operation.equals("RemoveGreeting")) {
					trainingBean.removeGreeting(config.response);
				} else if (config.operation.equals("AddDefaultResponse")) {
					trainingBean.addDefaultResponses(config.response);
				} else if (config.operation.equals("RemoveDefaultResponse")) {
					trainingBean.removeDefaultResponses(config.response);
				} else if (config.operation.equals("AddResponse")) {
					trainingBean.addQuestionResponses(config.question, config.response);
				}
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/save-response")
	public ResponseConfig saveResponse(ResponseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API save-response");
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			BotBean bean = loginBean.getBotBean();
			bean.validateInstance(config.instance);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.checkAdmin();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.connect(ClientType.REST);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			ChatLogBean chatLogBean = loginBean.getBean(ChatLogBean.class);
			try {
				config = chatLogBean.processSave(config);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				config.clearCredentials();
				return config;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/delete-response")
	public void deleteResponse(ResponseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API delete-response");
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			BotBean bean = loginBean.getBotBean();
			bean.validateInstance(config.instance);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.checkAdmin();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.connect(ClientType.REST);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			ChatLogBean chatLogBean = loginBean.getBean(ChatLogBean.class);
			try {
				chatLogBean.processDelete(config);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/create-channel-attachment")
	public MediaConfig createChannelAttachment(
			@FormDataParam("file") InputStream stream,
			@FormDataParam("xml") MediaConfig config, @Context HttpServletRequest requestContext) {
		LoginBean loginBean = new LoginBean();
		try {
			AdminDatabase.instance().log(Level.INFO, "API create-channel-attachment", config.instance, config.name, config.type);
			if (stream == null) {
				throw new BotException("Missing file attachment");
			}
			config.connect(loginBean, requestContext);
			byte[] image = BotBean.loadImageFile(stream);
			LiveChatBean bean = loginBean.getBean(LiveChatBean.class);
			bean.validateInstance(config.instance);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			ChannelAttachment attachment = bean.addAttachment(image, config.name, config.type, config.user, config.token);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			return attachment.toConfig();
		} catch (Throwable failed) {
			error(failed);
			return null;
		} finally {
			loginBean.disconnect();
		}
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/create-bot-attachment")
	public MediaConfig createBotAttachment(
			@FormDataParam("file") InputStream stream,
			@FormDataParam("xml") MediaConfig config, @Context HttpServletRequest requestContext) {
		LoginBean loginBean = new LoginBean();
		try {
			AdminDatabase.instance().log(Level.INFO, "API create-bot-attachment", config.instance, config.name, config.type);
			if (stream == null) {
				throw new BotException("Missing file attachment");
			}
			config.connect(loginBean, requestContext);
			byte[] image = BotBean.loadImageFile(stream);
			BotBean bean = loginBean.getBotBean();
			bean.validateInstance(config.instance);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			BotAttachment attachment = bean.addAttachment(image, config.name, config.type, config.user, config.token);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			return attachment.toConfig();
		} catch (Throwable failed) {
			error(failed);
			return null;
		} finally {
			loginBean.disconnect();
		}
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/create-issuetracker-attachment")
	public MediaConfig createIssueAttachment(
			@FormDataParam("file") InputStream stream,
			@FormDataParam("xml") MediaConfig config, @Context HttpServletRequest requestContext) {
		LoginBean loginBean = new LoginBean();
		try {
			AdminDatabase.instance().log(Level.INFO, "API create-issuetracker-attachment", config.instance, config.name, config.type);
			if (stream == null) {
				throw new BotException("Missing file attachment");
			}
			config.connect(loginBean, requestContext);
			byte[] image = BotBean.loadImageFile(stream);
			IssueTrackerBean bean = loginBean.getBean(IssueTrackerBean.class);
			bean.validateInstance(config.instance);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			IssueTrackerAttachment attachment = bean.addAttachment(image, config.name, config.type, config.user, config.token);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			return attachment.toConfig();
		} catch (Throwable failed) {
			error(failed);
			return null;
		} finally {
			loginBean.disconnect();
		}
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/create-forum-attachment")
	public MediaConfig createForumAttachment(
			@FormDataParam("file") InputStream stream,
			@FormDataParam("xml") MediaConfig config, @Context HttpServletRequest requestContext) {
		LoginBean loginBean = new LoginBean();
		try {
			AdminDatabase.instance().log(Level.INFO, "API create-forum-attachment", config.instance, config.name, config.type);
			if (stream == null) {
				throw new BotException("Missing file attachment");
			}
			config.connect(loginBean, requestContext);
			byte[] image = BotBean.loadImageFile(stream);
			ForumBean bean = loginBean.getBean(ForumBean.class);
			bean.validateInstance(config.instance);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			ForumAttachment attachment = bean.addAttachment(image, config.name, config.type, config.user, config.token);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			return attachment.toConfig();
		} catch (Throwable failed) {
			error(failed);
			return null;
		} finally {
			loginBean.disconnect();
		}
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/update-instance-icon")
	public InstanceConfig updateInstanceIcon(
				@FormDataParam("file") InputStream stream,
				@FormDataParam("xml") InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-instance-icon");
		return (InstanceConfig)updateIcon(stream, config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/update-domain-icon")
	public DomainConfig updateDomainIcon(
				@FormDataParam("file") InputStream stream,
				@FormDataParam("xml") DomainConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-domain-icon");
		return (DomainConfig)updateIcon(stream, config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/update-channel-icon")
	public ChannelConfig updateChannelIcon(
				@FormDataParam("file") InputStream stream,
				@FormDataParam("xml") ChannelConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-channel-icon");
		return (ChannelConfig)updateIcon(stream, config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/update-forum-icon")
	public ForumConfig updateForumIcon(
			@FormDataParam("file") InputStream stream,
			@FormDataParam("xml") ForumConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-forum-icon");
		return (ForumConfig)updateIcon(stream, config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/update-issuetracker-icon")
	public ForumConfig updateIssueTrackerIcon(
			@FormDataParam("file") InputStream stream,
			@FormDataParam("xml") ForumConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-issuetracker-icon");
		return (ForumConfig)updateIcon(stream, config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/update-avatar-icon")
	public AvatarConfig updateAvatarIcon(
			@FormDataParam("file") InputStream stream,
			@FormDataParam("xml") AvatarConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-avatar-icon");
		return (AvatarConfig)updateIcon(stream, config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/update-script-icon")
	public ScriptConfig updateScriptIcon(
			@FormDataParam("file") InputStream stream,
			@FormDataParam("xml") ScriptConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-script-icon");
		return (ScriptConfig)updateIcon(stream, config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/update-graphic-icon")
	public GraphicConfig updateGraphicIcon(
			@FormDataParam("file") InputStream stream,
			@FormDataParam("xml") GraphicConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-graphic-icon");
		return (GraphicConfig)updateIcon(stream, config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/update-graphic-media")
	public GraphicConfig updateGraphicMedia(
			@FormDataParam("file") InputStream stream,
			@FormDataParam("xml") GraphicConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-graphic-media");
		try {
			if (stream == null) {
				throw new BotException("Missing file attachment");
			}
			LoginBean loginBean = new LoginBean();
			try {
				GraphicBean bean = (GraphicBean)config.validate(loginBean, requestContext);
				bean.checkAdmin();
				byte[] image = BotBean.loadImageFile(stream);
				bean.saveMedia(image, config.fileName, config.fileType);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				Graphic instance = (Graphic)bean.getInstance();
				config = instance.buildConfig();
				config.isAdmin = bean.isAdmin();
				config.avatar = bean.getAvatarImage((WebMedium)bean.getInstance());
				return config;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/create-avatar-media")
	public void createAvatarMedia(
			@FormDataParam("file") InputStream stream,
			@FormDataParam("xml") AvatarMediaConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API create-avatar-media");
		try {
			if (stream == null) {
				throw new BotException("Missing file attachment");
			}
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				byte[] image = BotBean.loadImageFile(stream);
				AvatarBean bean = loginBean.getBean(AvatarBean.class);
				bean.validateInstance(config.instance);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.checkAdmin();
				bean.addAvatarMedia(image, config.name, config.type);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
		}
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/save-avatar-background")
	public void saveAvatarBackground(
			@FormDataParam("file") InputStream stream,
			@FormDataParam("xml") AvatarMediaConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API save-avatar-background");
		try {
			if (stream == null) {
				throw new BotException("Missing file attachment");
			}
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				byte[] image = BotBean.loadImageFile(stream);
				AvatarBean bean = loginBean.getBean(AvatarBean.class);
				bean.validateInstance(config.instance);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.checkAdmin();
				bean.saveAvatarBackground(image, config.name, config.type);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/delete-avatar-media")
	public void deleteAvatarMedia(AvatarMediaConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API delete-avatar-media");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				AvatarBean bean = loginBean.getBean(AvatarBean.class);
				bean.validateInstance(config.instance);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.checkAdmin();
				bean.deleteMedia(Long.valueOf(config.mediaId));
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/delete-avatar-background")
	public void deleteAvatarBackground(AvatarConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API delete-avatar-background");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				AvatarBean bean = (AvatarBean)config.validate(loginBean, requestContext);
				bean.checkAdmin();
				bean.deleteBackground();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/save-avatar-media")
	public void saveAvatarMedia(AvatarMediaConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API save-avatar-media");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				AvatarBean bean = loginBean.getBean(AvatarBean.class);
				bean.validateInstance(config.instance);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.checkAdmin();
				bean.saveMedia(config);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public WebMediumConfig updateIcon(InputStream stream, WebMediumConfig config, @Context HttpServletRequest requestContext) {
		try {
			if (stream == null) {
				throw new BotException("Missing file attachment");
			}
			LoginBean loginBean = new LoginBean();
			try {
				WebMediumBean bean = config.validate(loginBean, requestContext);
				bean.checkAdmin();
				byte[] image = BotBean.loadImageFile(stream);
				bean.update(image);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				WebMedium instance = (WebMedium)bean.getInstance();
				config = instance.buildConfig();
				config.isAdmin = bean.isAdmin();
				config.avatar = bean.getAvatarImage((WebMedium)bean.getInstance());
				return config;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/update-user-icon")
	public UserConfig updateUserIcon(
			@FormDataParam("file") InputStream stream,
			@FormDataParam("xml") UserConfig config,
			@Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-user-icon");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				byte[] image = BotBean.loadImageFile(stream);
				loginBean.updateUser(image);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				UserConfig user = new UserConfig(loginBean.getUser(), true);
				user.avatar = loginBean.getAvatarImage(loginBean.getUser());
				return user;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-all-templates")
	public List<InstanceConfig> getAllTemplates() {
		AdminDatabase.instance().log(Level.INFO, "API get-all-templates");
		try {
			LoginBean loginBean = new LoginBean();
			BotBean bean = loginBean.getBotBean();
			List<BotInstance> instances = AdminDatabase.instance().getAllTemplates(new User(), ContentRating.Mature, bean.getDomain());
			return (List<InstanceConfig>)(List)toConfig((List)instances, bean);
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<WebMediumConfig> toConfig(List<WebMedium> instances, WebMediumBean bean) {
		List<WebMediumConfig> configs = new ArrayList<WebMediumConfig>();
		for (WebMedium instance : instances) {
			WebMediumConfig config = instance.buildBrowseConfig();
			config.avatar = bean.getAvatarThumb(instance);
			configs.add(config);
		}
		return configs;
	}
	
	public List<ForumPostConfig> toConfig(List<ForumPost> instances, ForumPostBean bean) {
		List<ForumPostConfig> configs = new ArrayList<ForumPostConfig>();
		for (ForumPost instance : instances) {
			ForumPostConfig config = new ForumPostConfig();
			config.id = String.valueOf(instance.getId());
			config.topic = instance.getTopic();
			config.summary = instance.getSummary();
			if (instance.getCreator() != null) {
				config.creator = instance.getCreator().getUserId();
			}
			config.creationDate = instance.getCreationDateString();
			config.avatar = bean.getAvatarThumb(instance);
			configs.add(config);
		}
		return configs;
	}
	
	public List<IssueConfig> toConfig(List<Issue> instances, IssueBean bean) {
		List<IssueConfig> configs = new ArrayList<IssueConfig>();
		for (Issue instance : instances) {
			IssueConfig config = new IssueConfig();
			config.id = String.valueOf(instance.getId());
			config.title = instance.getTitle();
			config.issueType = instance.getType().name();
			config.priority = instance.getPriority().name();
			config.status = instance.getStatus().name();
			config.summary = instance.getSummary();
			if (instance.getCreator() != null) {
				config.creator = instance.getCreator().getUserId();
			}
			config.creationDate = instance.getCreationDateString();
			config.avatar = bean.getAvatarThumb(instance);
			configs.add(config);
		}
		return configs;
	}

	@GET
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/form-chat")
	public ChatResponse chat(
			@QueryParam("application") String application,
			@QueryParam("instance") String instance,
			@QueryParam("message") String message,
			@QueryParam("emote") String emote,
			@QueryParam("user") String user,
			@QueryParam("password") String password,
			@QueryParam("token") String token,
			@QueryParam("conversation") long conversation,
			@QueryParam("correction") boolean correction,
			@QueryParam("offensive") boolean offensive,
			@QueryParam("disconnect") boolean disconnect,
			@QueryParam("includeQuestion") boolean includeQuestion,
			@QueryParam("speak") boolean speak,
			@QueryParam("secure") boolean secure,
			@QueryParam("plainText") boolean plainText,
			@QueryParam("store") String store,
			@QueryParam("language") String language,
			@Context HttpServletRequest requestContext) {
		
		AdminDatabase.instance().log(Level.INFO, "API FORM chat", message, application, requestContext.getRemoteAddr());
		if (store != null && store.equals("0")) {
			Stats.stats.badAPICalls++;
			error(new BotException("Invalid request"));
			return null;
		}
		if (application == null || application.isEmpty()) {
			try {
				Stats.checkMaxAPI();
				Stats.stats.anonymousAPICalls++;
			} catch (Throwable failed) {
				error(failed);
				return null;
			}
		}
		ChatMessage chat = new ChatMessage();
		chat.application = application;
		chat.instance = instance;
		chat.message = message;
		chat.emote = emote;
		chat.user = user;
		chat.password = password;
		chat.token = token;
		chat.conversation = conversation;
		chat.correction = correction;
		chat.offensive = offensive;
		chat.disconnect = disconnect;
		chat.includeQuestion = includeQuestion;
		chat.speak = speak;
		chat.secure = secure;
		chat.plainText = plainText;
		chat.language = language;
		return chatMessage(chat, requestContext, true);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/post-chat")
	public JSONChatResponse postChat(JSONChatMessage message, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API chat", message, message.application, requestContext.getRemoteAddr());
		return new JSONChatResponse(chatMessage(new ChatMessage(message), requestContext, false));
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/chat")
	public JSONChatResponse chat(JSONChatMessage message, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API chat", message, message.application, requestContext.getRemoteAddr());
		return new JSONChatResponse(chatMessage(new ChatMessage(message), requestContext, false));
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/command/{application}/{instance}")
	public String command(
			@PathParam("application") String application,
			@PathParam("instance") String instance,
			String json,
			@Context HttpServletRequest requestContext) {
		
		AdminDatabase.instance().log(Level.INFO, "API JSON command", application, instance, json, requestContext.getRemoteAddr());
		try {
			CommandMessage message = new CommandMessage();
			message.application = application;
			message.instance = instance;
			message.command = json;
			ChatResponse response = processCommand(message, requestContext);
			return response.command;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/command")
	public ChatResponse command(CommandMessage message, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API command", message, message.application, requestContext.getRemoteAddr());
		try {
			return processCommand(message, requestContext);
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	/**
	 * Process a JSON command message to a bot "bot service".
	 */
	public ChatResponse processCommand(CommandMessage message, @Context HttpServletRequest requestContext) throws Throwable {
		Stats.stats.apiCalls++;
		IPStats.api(requestContext);
		if (message.disconnect) {
			ChatBean chatBean = (ChatBean)BeanManager.manager().removeInstance(message.conversation);
			if (chatBean != null) {
				chatBean.getLoginBean().disconnect();
			}
			return new ChatResponse();
		}
		long conversation = message.conversation;
		boolean firstRequest = false;
		ChatBean chatBean = (ChatBean)BeanManager.manager().getInstance(conversation);
		boolean newBean = false;
		if (chatBean == null) {
			firstRequest = true;
			LoginBean loginBean = new LoginBean();
			message.connect(loginBean, requestContext);
			BotBean bean = loginBean.getBotBean();
			bean.validateInstance(message.instance);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			if (bean.getInstance().isExternal()) {
				throw new BotException("Cannot send commands to external bots");
			}
			chatBean = loginBean.getBean(ChatBean.class);
			newBean = true;
		}
		String appId = chatBean.getLoginBean().getApplicationId();
		String appUser = chatBean.getLoginBean().getAppUser();
		try {
			if (message.application != null && !message.application.isEmpty() && !message.application.equals(appId)) {
				appUser = AdminDatabase.instance().validateApplicationId(message.application, null);
			}
			if (appUser != null) {
				AppIDStats stat = AppIDStats.getStats(appId, appUser);
				AppIDStats.checkMaxAPI(stat, appUser, chatBean.getBotBean().getInstanceId(), chatBean.getBotBean().getInstanceName());
				stat.apiCalls++;
			}
			if (!chatBean.getBotBean().isConnected()) {
				chatBean.getBotBean().connect(ClientType.REST, requestContext);
				if (chatBean.getLoginBean().getError() != null) {
					throw chatBean.getLoginBean().getError();
				}
			}
		} catch (Exception exception) {
			if (!newBean) {
				BeanManager.manager().removeInstance(conversation);
			}
			throw exception;
		}
		boolean autoPool = false;
		if (firstRequest) {
			// Check if dead connects are common and autopool.
			BotStats stats = BotStats.getStats(chatBean.getBotBean().getInstanceId(), chatBean.getBotBean().getInstanceName());
			if ((stats.connects > 10) && (stats.connects >= stats.chats)) {
				autoPool = true;
			}
		}
		try {
			chatBean.setInfo(message.info);
			chatBean.initialBot();
			chatBean.setSpeak(message.speak);
			chatBean.setLanguage(message.language);
			chatBean.getBotBean().setAvatar(message.avatar);
			chatBean.getBotBean().setAvatarFormat(message.avatarFormat);
			chatBean.getBotBean().setAvatarHD(message.avatarHD);
			LearningBean learningBean = chatBean.getLoginBean().getBean(LearningBean.class);
			if (learningBean.getEnableEmoting() || chatBean.getBotBean().isAdmin()) {
				chatBean.processEmote(message.emote);
				chatBean.processAction(message.action);
			}
			if (message.info != null && !message.info.isEmpty()) {
				chatBean.processInfo(message.info);
			}
			IPStats.botChats(requestContext);
			if (message.debug) {
				chatBean.setDebug(true);
				chatBean.getBotBean().clearLog();
				chatBean.getBotBean().setLogLevel(message.debugLevel);
			} else {
				chatBean.setDebug(false);
			}
			chatBean.checkConversationId(message.conversation);
			chatBean.processCommand(message.command, message.correction, message.offensive, message.learn);
			chatBean.speak();
			chatBean.getBotBean().outputAvatar();
		} finally {
			// Defer adding the bean until the conversation id is known to allow continuing conversations across disconnects.
			if (newBean) {
				conversation = BeanManager.manager().addInstance(chatBean, chatBean.getConversation());
			}
		}
		ChatResponse response = new ChatResponse();
		if (message.secure) {
			response.message = Utils.sanitize(chatBean.getResponse());
		} else {
			response.message = chatBean.getResponse();
		}
		if (message.plainText) {
			response.message = Utils.stripTags(response.message);
		}
		response.conversation = conversation;
		response.avatar = chatBean.getBotBean().getAvatarFileName();
		response.avatar2 = chatBean.getBotBean().getAvatar2FileName();
		response.avatar3 = chatBean.getBotBean().getAvatar3FileName();
		response.avatar4 = chatBean.getBotBean().getAvatar4FileName();
		response.avatar5 = chatBean.getBotBean().getAvatar5FileName();
		response.avatarType = chatBean.getBotBean().getAvatarFileType();
		response.avatarTalk = chatBean.getBotBean().getAvatarTalkFileName();
		response.avatarTalkType = chatBean.getBotBean().getAvatarTalkFileType();
		response.avatarBackground = chatBean.getBotBean().getAvatarBackground();
		response.avatarAction = chatBean.getBotBean().getAvatarActionFileName();
		response.avatarActionType = chatBean.getBotBean().getAvatarActionFileType();
		response.avatarActionAudio = chatBean.getBotBean().getAvatarActionAudioFileName();
		response.avatarActionAudioType = chatBean.getBotBean().getAvatarActionAudioFileType();
		response.avatarAudio = chatBean.getBotBean().getAvatarAudioFileName();
		response.avatarAudioType = chatBean.getBotBean().getAvatarAudioFileType();
		response.emote = chatBean.getBotBean().getBot().mood().currentEmotionalState().name();
		response.action = chatBean.getBotBean().getBot().avatar().getAction();
		response.command = chatBean.getBotBean().getBot().avatar().getCommand();
		response.pose = chatBean.getBotBean().getBot().avatar().getPose();
		if (message.debug){
			response.log = chatBean.getBotBean().getLog();
		}
		if (message.speak) {
			response.speech = chatBean.getResponseFileName();
		}
		if (autoPool && firstRequest) {
			chatBean.getBotBean().poolInstance();
		}
		return response;
	}

	public ChatResponse chatMessage(ChatMessage message, HttpServletRequest requestContext, boolean autoPool) {
		try {
			Stats.stats.apiCalls++;
			IPStats.api(requestContext);
			if (message.disconnect) {
				ChatBean chatBean = (ChatBean)BeanManager.manager().removeInstance(message.conversation);
				if (chatBean != null) {
					chatBean.getLoginBean().disconnect();
				}
				return new ChatResponse();
			}
			long conversation = message.conversation;
			boolean firstRequest = false;
			ChatBean chatBean = (ChatBean)BeanManager.manager().getInstance(conversation);
			boolean newBean = false;
			if (chatBean == null) {
				firstRequest = true;
				LoginBean loginBean = new LoginBean();
				message.connect(loginBean, requestContext);
				BotBean bean = loginBean.getBotBean();
				bean.validateInstance(message.instance);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				if (bean.getInstance().isExternal()) {
					if (!bean.getInstance().hasAPI()) {
						throw new BotException("Cannot chat with external bots with no API");
					}
					return externalChatMessage(bean, message, requestContext, autoPool);
				}
				chatBean = loginBean.getBean(ChatBean.class);
				newBean = true;
			}
			String appId = chatBean.getLoginBean().getApplicationId();
			String appUser = chatBean.getLoginBean().getAppUser();
			try {
				if (message.application != null && !message.application.isEmpty() && !message.application.equals(appId)) {
					appUser = AdminDatabase.instance().validateApplicationId(message.application, null);
				}
				if (appUser != null) {
					AppIDStats stat = AppIDStats.getStats(appId, appUser);
					AppIDStats.checkMaxAPI(stat, appUser, chatBean.getBotBean().getInstanceId(), chatBean.getBotBean().getInstanceName());
					stat.apiCalls++;
				}
				if (!chatBean.getBotBean().isConnected()) {
					chatBean.getBotBean().connect(ClientType.REST, requestContext);
					if (chatBean.getLoginBean().getError() != null) {
						throw chatBean.getLoginBean().getError();
					}
				}
			} catch (Exception exception) {
				if (!newBean) {
					BeanManager.manager().removeInstance(conversation);
				}
				throw exception;
			}
			if (firstRequest && !autoPool) {
				// Check if dead connects are common and autopool.
				BotStats stats = BotStats.getStats(chatBean.getBotBean().getInstanceId(), chatBean.getBotBean().getInstanceName());
				if ((stats.connects > 10) && (stats.connects >= stats.chats)) {
					autoPool = true;
				}
			}
			try {
				chatBean.setInfo(message.info);
				chatBean.initialBot();
				chatBean.setSpeak(message.speak);
				chatBean.setLanguage(message.language);
				chatBean.setVoice(message.voice);
				chatBean.setMod(message.mod);
				chatBean.getBotBean().setAvatar(message.avatar);
				chatBean.getBotBean().setAvatarFormat(message.avatarFormat);
				chatBean.getBotBean().setAvatarHD(message.avatarHD);
				LearningBean learningBean = chatBean.getLoginBean().getBean(LearningBean.class);
				if (learningBean.getEnableEmoting() || chatBean.getBotBean().isAdmin()) {
					chatBean.processEmote(message.emote);
					chatBean.processAction(message.action);
				}
				if (message.info != null && !message.info.isEmpty()) {
					chatBean.processInfo(message.info);
				}
				IPStats.botChats(requestContext);
				if (message.debug) {
					chatBean.setDebug(true);
					chatBean.getBotBean().clearLog();
					chatBean.getBotBean().setLogLevel(message.debugLevel);
				} else {
					chatBean.setDebug(false);
				}
				chatBean.checkConversationId(message.conversation);
				TextEntry textEntry = chatBean.getBotBean().getBot().awareness().getSense(TextEntry.class);
				Language language = chatBean.getBotBean().getBot().mind().getThought(Language.class);
				BotStats stats = BotStats.getStats(chatBean.getBotBean().getInstanceId(), chatBean.getBotBean().getInstanceName());
				resetStats(stats, textEntry, language);
				chatBean.processInput(message.message, message.correction, message.offensive, message.learn);
				chatBean.speak();
				chatBean.getBotBean().outputAvatar();
				resetStats(stats, textEntry, language);
			} finally {
				// Defer adding the bean until the conversation id is known to allow continuing conversations across disconnects.
				if (newBean) {
					conversation = BeanManager.manager().addInstance(chatBean, chatBean.getConversation());
				}
			}
			ChatResponse response = new ChatResponse();
			if (message.includeQuestion) {
				response.question = message.message;
			}
			if (message.secure) {
				response.message = Utils.sanitize(chatBean.getResponse());
			} else {
				response.message = chatBean.getResponse();
			}
			if (message.plainText) {
				response.message = Utils.stripTags(response.message);
			}
			response.conversation = conversation;
			response.avatar = chatBean.getBotBean().getAvatarFileName();
			response.avatar2 = chatBean.getBotBean().getAvatar2FileName();
			response.avatar3 = chatBean.getBotBean().getAvatar3FileName();
			response.avatar4 = chatBean.getBotBean().getAvatar4FileName();
			response.avatar5 = chatBean.getBotBean().getAvatar5FileName();
			response.avatarType = chatBean.getBotBean().getAvatarFileType();
			response.avatarTalk = chatBean.getBotBean().getAvatarTalkFileName();
			response.avatarTalkType = chatBean.getBotBean().getAvatarTalkFileType();
			response.avatarBackground = chatBean.getBotBean().getAvatarBackground();
			response.avatarAction = chatBean.getBotBean().getAvatarActionFileName();
			response.avatarActionType = chatBean.getBotBean().getAvatarActionFileType();
			response.avatarActionAudio = chatBean.getBotBean().getAvatarActionAudioFileName();
			response.avatarActionAudioType = chatBean.getBotBean().getAvatarActionAudioFileType();
			response.avatarAudio = chatBean.getBotBean().getAvatarAudioFileName();
			response.avatarAudioType = chatBean.getBotBean().getAvatarAudioFileType();
			response.emote = chatBean.getBotBean().getBot().mood().currentEmotionalState().name();
			response.action = chatBean.getBotBean().getBot().avatar().getAction();
			response.command = chatBean.getBotBean().getBot().avatar().getCommand();
			response.pose = chatBean.getBotBean().getBot().avatar().getPose();
			if (message.debug){
				response.log = chatBean.getBotBean().getLog();
			}
			if (message.speak) {
				response.speech = chatBean.getResponseFileName();
			}
			if (autoPool && firstRequest) {
				chatBean.getBotBean().poolInstance();
			}
			return response;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	public Object searchJSON(String value, Object left, Object right) {
		if (left == null) {
			return null;
		}
		if (left.equals(value)) {
			return right;
		}
		if (left instanceof JSONObject && right instanceof JSONObject) {
			Set children = ((JSONObject)left).keySet();
			for (Object key : children) {
				Object leftChild = ((JSONObject)left).get(key);
				Object rightChlild = ((JSONObject)right).get(key);
				Object result = searchJSON(value, leftChild, rightChlild);
				if (result != null) {
					return result;
				}
			}
		} else if (left instanceof JSONArray && right instanceof JSONArray) {
			for (int index = 0; index < ((JSONObject)left).size() && index < ((JSONObject)right).size(); index++) {
				Object leftChild = ((JSONObject)left).get(index);
				Object rightChlild = ((JSONObject)right).get(index);
				Object result = searchJSON(value, leftChild, rightChlild);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
		
	/**
	 * Execute an API request to an external bot.
	 */
	public ChatResponse externalChatMessage(BotBean bean, ChatMessage message, HttpServletRequest requestContext, boolean autoPool) {
		try {
			Stats.stats.apiCalls++;
			IPStats.api(requestContext);
			String appId = bean.getLoginBean().getApplicationId();
			String appUser = bean.getLoginBean().getAppUser();
			if (message.application != null && !message.application.isEmpty() && !message.application.equals(appId)) {
				appUser = AdminDatabase.instance().validateApplicationId(message.application, null);
			}
			if (appUser != null) {
				AppIDStats stat = AppIDStats.getStats(appId, appUser);
				AppIDStats.checkMaxAPI(stat, appUser, bean.getInstanceId(), bean.getInstanceName());
				stat.apiCalls++;
			}
			IPStats.botChats(requestContext);

			BotInstance instance = bean.getInstance();
			String url = instance.getApiURL();
			if (message.message == null) {
				url = url.replace(":message", "");
			} else {
				String text = message.message;
				if (message.language != null && message.language.length() < 2) {
					String botLanguage = instance.getLanguage();
					if (botLanguage != null && botLanguage.length() < 2) {
						text = BotTranslationService.instance().translate(text, message.language, botLanguage);
					}
				}
				url = url.replace(":message", Utils.encodeURL(text));
			}
			if (message.conversation != 0) {
				url = url.replace(":conversation", "");
			} else {
				url = url.replace(":conversation", String.valueOf(message.conversation));
			}
			if (message.speak) {
				url = url.replace(":speak", "true");
			} else {
				url = url.replace(":speak", "");
			}
			AdminDatabase.instance().log(Level.INFO, "API external chat", url);
			InputStream stream = Utils.openStream(BeanServlet.safeURL(url), 20000);
			String result = Utils.loadTextFile(stream, "UTF-8", 1000000);
			AdminDatabase.instance().log(Level.INFO, "API external response", result);
			String conversation = null;
			String responseText = "";
			if (instance.getApiJSON()) {
				JSONObject json = (JSONObject)JSONSerializer.toJSON(result);
				if (instance.getApiResponse() != null && !instance.getApiResponse().isEmpty()) {
					JSONObject template = (JSONObject)JSONSerializer.toJSON(instance.getApiResponse());
					Object value = searchJSON(":response", template, json);
					if (value != null) {
						responseText = (String)value;
					}
					value = searchJSON(":conversation", template, json);
					if (value != null) {
						conversation = (String)value;
					}
				} else {
					Object value = json.get("message");
					if (value instanceof String) {
						responseText = (String)value;
					}
					value = json.get("conversation");
					if (value instanceof String) {
						conversation = (String)value;
					}
				}
			} else {
				Element dom = Utils.parseXML(result);
				if (dom != null) {
					if (instance.getApiResponse() != null && !instance.getApiResponse().isEmpty()) {
						Element template = Utils.parseXML(instance.getApiResponse());
						NodeList children = template.getChildNodes();
						for (int index = 0; index < children.getLength(); index++) {
							Node child = children.item(index);
							String context = child.getTextContent().trim();
							if (context.equals(":response")) {
								if (child instanceof Element) {
									NodeList list = dom.getElementsByTagName(child.getNodeName());
									if (list.getLength() > 0) {
										responseText = list.item(0).getTextContent().trim();
									}
								} else {
									responseText = dom.getAttribute(child.getNodeName()).trim();
								}
							} else if (context.equals(":conversation")) {
								if (child instanceof Element) {
									NodeList list = dom.getElementsByTagName(child.getNodeName());
									if (list.getLength() > 0) {
										conversation = list.item(0).getTextContent().trim();
									}
								} else {
									conversation = dom.getAttribute(child.getNodeName()).trim();
								}
							}
						}
					} else {
						NodeList list = dom.getElementsByTagName("message");
						if (list.getLength() > 0) {
							responseText = list.item(0).getTextContent().trim();
						}
						conversation = dom.getAttribute("conversation").trim();
					}
				}
			}

			if (responseText != null && !responseText.isEmpty() && message.language != null && message.language.length() != 2) {
				String botLanguage = instance.getLanguage();
				if (botLanguage != null && botLanguage.length() != 2) {
					responseText = BotTranslationService.instance().translate(responseText, botLanguage, message.language);
				}
			}

			ChatResponse response = bean.initAvatar();
			if (conversation != null && !conversation.isEmpty()) {
				try {
					message.conversation = Long.valueOf(conversation);
				} catch (NumberFormatException ignore) {}
			}
			if (message.speak) {
				VoiceBean voiceBean = bean.getLoginBean().getBean(VoiceBean.class);
				response.speech = ChatBean.speak(ChatBean.prepareSpeechText(responseText), voiceBean.getVoice(), voiceBean.getVoiceMod());
			}
			if (message.includeQuestion) {
				response.question = message.message;
			}
			if (message.secure) {
				response.message = Utils.sanitize(responseText);
			} else {
				response.message = responseText;
			}
			if (message.plainText) {
				response.message = Utils.stripTags(responseText);
			}
			response.conversation = message.conversation;
			return response;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/chat-settings")
	public ChatSettings chatSettings(ChatSettings settings, @Context HttpServletRequest requestContext) {
		try {
			Stats.stats.apiCalls++;
			IPStats.api(requestContext);
			long conversation = settings.conversation;
			ChatBean chatBean = (ChatBean)BeanManager.manager().getInstance(conversation);
			if (chatBean == null) {
				return settings;
			}
			if (!chatBean.getBotBean().isConnected()) {
				return settings;
			}
			LearningBean learningBean = chatBean.getLoginBean().getBean(LearningBean.class);
			settings.allowEmotes = learningBean.getEnableEmoting();
			settings.allowLearning = chatBean.showLearning();
			settings.learning = chatBean.getAllowLearning();
			settings.allowCorrection = chatBean.getLoginBean().getBotBean().isCorrectionAllow();
			return settings;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/avatar-message")
	public ChatResponse avatarMessage(AvatarMessage message, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API avatar message", message, message.application, requestContext.getRemoteAddr());
		try {
			LoginBean loginBean = new LoginBean();
			message.connect(loginBean, requestContext);
			AvatarBean bean = loginBean.getBean(AvatarBean.class);
			if (message.avatar != null && !message.avatar.isEmpty()) {
				bean.validateInstance(message.avatar);
			}
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			ChatResponse response = bean.processMessage(message);
			if (response.avatar == null) {
				response.avatar = message.avatar;
			}
			if (message.speak) {
				response.speech = ChatBean.speak(ChatBean.prepareSpeechText(message.message), message.voice, message.voiceMod);
			}
			return response;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create-instance")
	public InstanceConfig createInstance(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API create-instance", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			BotBean bean = loginBean.getBotBean();
			config.categories = config.getCategories();
			config.accessMode = config.getAccessMode();
			bean.createInstance(config, false, false, requestContext.getRemoteAddr());
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			AppIDStats stats = AppIDStats.getStats(loginBean.getApplicationId(), loginBean.getUserId());
			stats.botCreates++;
			// Also check for avatar setting.
			if (config.instanceAvatar != null && !config.instanceAvatar.isEmpty()) {
				InstanceAvatarBean avatarBean = loginBean.getBean(InstanceAvatarBean.class);
				avatarBean.chooseAvatar(config.instanceAvatar);
			}
			
			BotInstance instance = bean.getInstance();
			config = instance.buildConfig();
			config.isAdmin = true;
			config.avatar = bean.getAvatarImage(bean.getInstance());
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create-forum")
	public ForumConfig createForum(ForumConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API create-forum", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			ForumBean bean = loginBean.getBean(ForumBean.class);
			config.categories = config.getCategories();
			config.accessMode = config.getAccessMode();
			config.postAccessMode = config.getPostAccessMode();
			config.replyAccessMode = config.getReplyAccessMode();
			bean.createInstance(config);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			Forum instance = bean.getInstance();
			config = instance.buildConfig();
			config.isAdmin = true;
			config.avatar = bean.getAvatarImage(bean.getInstance());
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create-issuetracker")
	public IssueTrackerConfig createIssueTracker(IssueTrackerConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API create-issuetracker", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			IssueTrackerBean bean = loginBean.getBean(IssueTrackerBean.class);
			config.categories = config.getCategories();
			config.accessMode = config.getAccessMode();
			config.createAccessMode = config.getCreateAccessMode();
			bean.createInstance(config);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			IssueTracker instance = bean.getInstance();
			config = instance.buildConfig();
			config.isAdmin = true;
			config.avatar = bean.getAvatarImage(bean.getInstance());
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create-graphic")
	public GraphicConfig createGraphic(GraphicConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API create-graphic", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			GraphicBean bean = loginBean.getBean(GraphicBean.class);
			config.categories = config.getCategories();
			config.accessMode = config.getAccessMode();
			bean.createInstance(config);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			Graphic instance = bean.getInstance();
			config = instance.buildConfig();
			config.isAdmin = true;
			config.avatar = bean.getAvatarImage(bean.getInstance());
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create-script")
	public ScriptConfig createScript(ScriptConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API create-script", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			ScriptBean bean = loginBean.getBean(ScriptBean.class);
			config.categories = config.getCategories();
			config.accessMode = config.getAccessMode();
			bean.createInstance(config);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			Script instance = bean.getInstance();
			config = instance.buildConfig();
			config.isAdmin = true;
			config.avatar = bean.getAvatarImage(bean.getInstance());
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create-avatar")
	public AvatarConfig createAvatar(AvatarConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API create-script", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			AvatarBean bean = loginBean.getBean(AvatarBean.class);
			config.categories = config.getCategories();
			config.accessMode = config.getAccessMode();
			bean.createInstance(config);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			Avatar instance = bean.getInstance();
			config = instance.buildConfig();
			config.isAdmin = true;
			config.avatar = bean.getAvatarImage(bean.getInstance());
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create-user-avatar")
	public AvatarConfig createUserAvatar(AvatarConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API create-user-avatar", config.name);
		try {
			User user = AdminDatabase.instance().getUser(config.user);
			if (user == null) {
				throw new BotException("User does not exists - " + config.user);
			}
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			AvatarBean avatarBean = loginBean.getBean(AvatarBean.class);
			avatarBean.createUserAvatarInstance(user);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			Avatar instance = avatarBean.getInstance();
			config = instance.buildConfig();
			config.isAdmin = true;
			config.avatar = avatarBean.getAvatarImage(avatarBean.getInstance());
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create-user-message")
	public UserMessageConfig createUserMessage(UserMessageConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API create-user-message", config.subject);
		LoginBean loginBean = new LoginBean();
		try {
			config.connect(loginBean, requestContext);
			UserToUserMessageBean userToUserMessageBean = loginBean.getBean(UserToUserMessageBean.class);
			UserMessageConfig messageConfig = userToUserMessageBean.createUserMessage(config, requestContext);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			return messageConfig;
		} catch (Throwable failed) {
			error(failed);
			return null;
		} finally {
			loginBean.disconnect();
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("user-friendship")
	public UserFriendsConfig userFriendship(UserFriendsConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API user-friendship", "user friendship");
		UserFriendsConfig userFriendConfig = null;
		LoginBean loginBean = new LoginBean();
		try {
			config.connect(loginBean, requestContext);
			if ("AddFriendship".equals(config.action)) {
				Friendship friendship = loginBean.createUserFriendship(config.userFriend);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				userFriendConfig = new UserFriendsConfig();
				if (friendship != null) {
					userFriendConfig.action = "AddFriendship";
					userFriendConfig.friendship = String.valueOf(friendship.getId());
					userFriendConfig.userFriend = friendship.getFriend();
				}
			} else if ("RemoveFriendship".equals(config.action)) {
				String[] friendshipArray = config.userFriend.split(",");
				for (String friendId : friendshipArray) {
					loginBean.deleteUserFriendship(friendId);
				}
			}
			return userFriendConfig;
		} catch (Throwable failed) {
			error(failed);
			return null;
		} finally {
			loginBean.disconnect();
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("get-user-friendships")
	public List<UserFriendsConfig> getUserFrienships(UserFriendsConfig config, @Context HttpServletRequest requestContext) {
		LoginBean loginBean = new LoginBean();
		try {
			config.connect(loginBean, requestContext);
			List<UserFriendsConfig> friendsList = loginBean.getUserFriendships(config);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			return friendsList;
		} catch (Throwable failed) {
			error(failed);
			return null;
		} finally {
			loginBean.disconnect();
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("get-user-followers")
	public List<UserFriendsConfig> getUserFollowers(UserFriendsConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-user-followers", "get user followers");
		LoginBean loginBean = new LoginBean();
		try {
			config.connect(loginBean, requestContext);
			List<UserFriendsConfig> followersList = loginBean.getUserFollowers(config);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			return followersList;
		} catch (Throwable failed) {
			error(failed);
			return null;
		} finally {
			loginBean.disconnect();
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("get-all-users")
	public List<UserConfig> getAllPublicUsers(BrowseUserConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-all-users", "get all users");
		LoginBean loginBean = new LoginBean();
		try {
			config.connect(loginBean, requestContext);
			UserFilter filter = UserFilter.Public;
			UserSort sort = UserSort.Connects;
			List<UserConfig> publicUserConfigList = new ArrayList<UserConfig>();
			if ("Bot".equalsIgnoreCase(config.userFilter)) {
				filter = UserFilter.Bot;
				BrowseConfig browseConfig = new BrowseConfig();
				browseConfig.application = config.application;
				browseConfig.type = "Bot";
				browseConfig.typeFilter = "Public";
				browseConfig.filter = ((BrowseUserConfig) config).nameFilter;
				browseConfig.tag = ((BrowseUserConfig) config).tag;
				browseConfig.sort = ((BrowseUserConfig) config).sort;
				browseConfig.page = ((BrowseUserConfig) config).page;
				List<WebMediumConfig> botList = getInstances(browseConfig, requestContext);
				for(WebMediumConfig botConfig : botList) {
					UserConfig publicUserConfig = new UserConfig();
					publicUserConfig.user = "@" + botConfig.alias;
					publicUserConfig.name = botConfig.name;
					publicUserConfig.bio = botConfig.description;
					String connects = "";
					connects += botConfig.connects + "/" + botConfig.dailyConnects + "/" + botConfig.weeklyConnects + "/" + botConfig.monthlyConnects;
					publicUserConfig.connects = connects;
					publicUserConfig.avatar = botConfig.avatar;
					publicUserConfigList.add(publicUserConfig);
				}
				return publicUserConfigList;
			}
			String sortValue = config.sort;
			if (sortValue == null) {
				sortValue = "";
			} else {
				sortValue = sortValue.toLowerCase();
			}
			if ((sortValue.equals("by name") || sortValue.equals("name"))) {
				sort = UserSort.Name;
			} else if (sortValue.equals("date")) {
				sort = UserSort.Date;
			} else if (sortValue.equals("connects")) {
				sort = UserSort.Connects;
			} else if ((sortValue.equals("last connect") || sortValue.equals("lastconnect"))) {
				sort = UserSort.LastConnect;
			}
			UserBean bean = null;
			bean = loginBean.getBean(UserBean.class);
			bean.setUserFilter(filter);
			bean.setUserSort(sort);
			if (config.tag == null) {
				bean.setTagFilter("");
			} else {
				bean.setTagFilter(config.tag);
			}
			if (config.nameFilter == null) {
				bean.setNameFilter("");
			} else {
				bean.setNameFilter(config.nameFilter);
			}
			if (config.page != null && !config.page.isEmpty()) {
				bean.setPage(Integer.valueOf(config.page));
			} else {
				bean.setPage(0);
			}
			List<User> usersList =  bean.getAllInstances();
			for (User user : usersList) {
				UserConfig publicUserConfig = new UserConfig();
				publicUserConfig.user = user.getUserId();
				publicUserConfig.name = user.getName();
				publicUserConfig.bio = user.getBio();
				publicUserConfig.connects = String.valueOf(user.getConnects());
				publicUserConfig.lastConnect = String.valueOf(user.getLastConnected());
				publicUserConfig.avatar = loginBean.getAvatarThumb(user);
				publicUserConfigList.add(publicUserConfig);
			}
			return publicUserConfigList;
		} catch (Throwable failed) {
			error(failed);
			return null;
		} finally {
			loginBean.disconnect();
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("get-user-conversations")
	public List<UserMessageConfig> getUserConversations(UserMessageConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-user-conversations", config.user);
		LoginBean loginBean = new LoginBean();
		try {
			config.connect(loginBean, requestContext);
			UserMessageBean userMessageBean = loginBean.getBean(UserMessageBean.class);
			List<UserMessageConfig> userMessages = userMessageBean.getUserConversations(config);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			return userMessages;
		} catch (Throwable failed) {
			error(failed);
			return null;
		} finally {
			loginBean.disconnect();
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("get-user-to-user-messages")
	public List<UserMessageConfig> getUserToUserMessages(UserMessageConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-user-to-user-messages", "get user to user messages");
		LoginBean loginBean = new LoginBean();
		try {
			config.connect(loginBean, requestContext);
			UserToUserMessageBean userToUserMessageBean = loginBean.getBean(UserToUserMessageBean.class);
			List<UserMessageConfig> messages = userToUserMessageBean.getUserToUserMessages(config);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			return messages;
		} catch (Throwable failed) {
			error(failed);
			return null;
		} finally {
			loginBean.disconnect();
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("check-user-new-messages")
	public List<UserMessageConfig> checkUserNewMessages(UserMessageConfig config, @Context HttpServletRequest requestContext) throws ParseException {
		AdminDatabase.instance().log(Level.INFO, "API check-user-new-messages", "check user new messages");
		LoginBean loginBean = new LoginBean();
		try {
			config.connect(loginBean, requestContext);
			UserToUserMessageBean userToUserMessageBean = loginBean.getBean(UserToUserMessageBean.class);
			List<UserMessageConfig> messages = userToUserMessageBean.checkUserNewMessages(config);
			return messages;
		} catch (Throwable failed) {
			error(failed);
			return null;
		} finally {
			loginBean.disconnect();
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("delete-user-conversation")
	public UserMessageConfig deletUserConversation(UserMessageConfig config, @Context HttpServletRequest requestContext) throws ParseException {
		AdminDatabase.instance().log(Level.INFO, "API delete-user-conversation", "delete user conversation");
		LoginBean loginBean = new LoginBean();
		try {
			config.connect(loginBean, requestContext);
			UserToUserMessageBean userToUserMessageBean = loginBean.getBean(UserToUserMessageBean.class);
			if (userToUserMessageBean.deleteUserToUserMessages(true, config.target)) {
				return config;
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		} finally {
			loginBean.disconnect();
		}
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create-forum-post")
	public ForumPostConfig createForumPost(ForumPostConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API create-forum-post", config.topic);
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			ForumBean forumBean = loginBean.getBean(ForumBean.class);
			ForumPostBean bean = loginBean.getBean(ForumPostBean.class);
			bean.setForumBean(forumBean);
			forumBean.validateInstance(config.forum);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.createInstance(config.topic, config.details, config.subscribe, config.tags);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			ForumPost instance = bean.getInstance();
			config = instance.buildConfig();
			config.isAdmin = true;
			config.avatar = bean.getAvatarThumb(bean.getInstance());
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create-issue")
	public IssueConfig createIssue(IssueConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API create-issue", config.title);
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			IssueTrackerBean issueTrackerBean = loginBean.getBean(IssueTrackerBean.class);
			IssueBean bean = loginBean.getBean(IssueBean.class);
			bean.setIssueTrackerBean(issueTrackerBean);
			issueTrackerBean.validateInstance(config.tracker);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.createInstance(config.title, config.priority, config.issueType, config.status, config.details, config.subscribe, config.tags);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			Issue instance = bean.getInstance();
			config = instance.buildConfig();
			config.isAdmin = true;
			config.avatar = bean.getAvatarThumb(bean.getInstance());
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create-license")
	public LicenseConfig createLicense(LicenseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API create-license", config.client);
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			loginBean.checkSuper();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			License license = new License(config);
			license = AdminDatabase.instance().createLicense(license);
			config = license.buildConfig();
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("update-license")
	public LicenseConfig updateLicense(LicenseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-license", config.client);
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			loginBean.checkSuper();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			License license = new License(config);
			license = AdminDatabase.instance().updateLicense(license);
			config = license.buildConfig();
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("delete-license")
	public void deleteLicense(LicenseConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API delete-license", config.client);
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			loginBean.checkSuper();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			License license = new License(config);
			AdminDatabase.instance().deleteLicense(license);
			return;
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create-reply")
	public ForumPostConfig createReply(ForumPostConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API create-reply", config.topic);
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			ForumBean forumBean = loginBean.getBean(ForumBean.class);
			ForumPostBean bean = loginBean.getBean(ForumPostBean.class);
			bean.setForumBean(forumBean);
			bean.validateInstance(config.parent, ClientType.REST);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			bean.createReply(config.details, false);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			ForumPost instance = bean.getInstance();
			config = instance.buildConfig();
			config.isAdmin = true;
			config.avatar = bean.getAvatarThumb(bean.getInstance());
			if (config.replies != null) {
				int index = 0;				
				for (ForumPostConfig reply : config.replies) {
					reply.avatar = bean.getAvatarThumb(instance.getReplies().get(index));
					index++;
				}
			}
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("update-forum-post")
	public ForumPostConfig updateForumPost(ForumPostConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-forum-post", config.topic);
		try {
			LoginBean loginBean = new LoginBean();
			try {
				ForumPostBean bean = config.validate(loginBean, requestContext);
				bean.updateInstance(config.topic, config.details, config.tags, config.isFeatured, ClientType.REST);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				ForumPost instance = bean.getInstance();
				config = instance.buildConfig();
				config.isAdmin = true;
				config.avatar = bean.getAvatarThumb(bean.getInstance());
				if (config.replies != null) {
					int index = 0;
					for (ForumPostConfig reply : config.replies) {
						reply.avatar = bean.getAvatarThumb(instance.getReplies().get(index));
						index++;
					}
				}
				return config;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("update-issue")
	public IssueConfig updateIssue(IssueConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-issue", config.title);
		try {
			LoginBean loginBean = new LoginBean();
			try {
				IssueBean bean = config.validate(loginBean, requestContext);
				bean.updateInstance(config.title, config.priority, config.issueType, config.status, config.details, config.tags, config.isPriority, config.isHidden, ClientType.REST);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				Issue instance = bean.getInstance();
				config = instance.buildConfig();
				config.isAdmin = true;
				config.avatar = bean.getAvatarThumb(bean.getInstance());
				return config;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("delete-forum-post")
	public void deleteForumPost(ForumPostConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API delete-forum-post", config.topic);
		try {
			LoginBean loginBean = new LoginBean();
			ForumPostBean bean = config.validate(loginBean, requestContext);
			bean.deleteInstance(true);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			return;
		} catch (Throwable failed) {
			error(failed);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("delete-issue")
	public void deleteIssue(IssueConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API delete-issue", config.title);
		try {
			LoginBean loginBean = new LoginBean();
			IssueBean bean = config.validate(loginBean, requestContext);
			bean.deleteInstance(true);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			return;
		} catch (Throwable failed) {
			error(failed);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create-channel")
	public ChannelConfig createChannel(ChannelConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API create-channel", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			LiveChatBean bean = loginBean.getBean(LiveChatBean.class);
			config.categories = config.getCategories();
			config.accessMode = config.getAccessMode();
			config.type = config.getChannelType();
			bean.createInstance(config);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			ChatChannel instance = bean.getInstance();
			config = instance.buildConfig();
			config.isAdmin = true;
			config.avatar = bean.getAvatarImage(bean.getInstance());
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create-domain")
	public DomainConfig createDomain(DomainConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API create-domain", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			DomainBean bean = loginBean.getBean(DomainBean.class);
			config.accessMode = config.getAccessMode();
			config.creationMode = config.getCreationMode();
			bean.createInstance(config);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			Domain instance = bean.getInstance();
			config = instance.buildConfig();
			config.isAdmin = true;
			config.avatar = bean.getAvatarImage(bean.getInstance());
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("edit-instance")
	public InstanceConfig editInstance(InstanceConfig config, @Context HttpServletRequest requestContext) {
		return updateInstance(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("update-instance")
	public InstanceConfig updateInstance(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-instance", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			BotBean bean = (BotBean)config.validate(loginBean, requestContext);
			bean.updateInstance(config, config.domain, null, null, null, null, null, null, null, null, null);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			BotInstance instance = bean.getInstance();
			config = instance.buildConfig();
			config.isAdmin = bean.isAdmin();
			config.avatar = bean.getAvatarImage(bean.getInstance());
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("update-forum")
	public ForumConfig updateForum(ForumConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-forum", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			try {
				ForumBean bean = (ForumBean)config.validate(loginBean, requestContext);
				bean.updateForum(config, config.domain, null, null);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				Forum instance = bean.getInstance();
				config = instance.buildConfig();
				config.isAdmin = bean.isAdmin();
				config.avatar = bean.getAvatarImage(bean.getInstance());
				return config;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("update-issuetracker")
	public IssueTrackerConfig updateIssueTracker(IssueTrackerConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-issuetracker", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			try {
				IssueTrackerBean bean = (IssueTrackerBean)config.validate(loginBean, requestContext);
				bean.updateIssueTracker(config, config.domain, null, null);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				IssueTracker instance = bean.getInstance();
				config = instance.buildConfig();
				config.isAdmin = bean.isAdmin();
				config.avatar = bean.getAvatarImage(bean.getInstance());
				return config;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("update-graphic")
	public GraphicConfig updateGraphic(GraphicConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-graphic", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			try {
				GraphicBean bean = (GraphicBean)config.validate(loginBean, requestContext);
				bean.updateGraphic(config, config.domain, null, null);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				Graphic instance = bean.getInstance();
				config = instance.buildConfig();
				config.isAdmin = bean.isAdmin();
				config.avatar = bean.getAvatarImage(bean.getInstance());
				return config;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("update-script")
	public ScriptConfig updateScript(ScriptConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-script", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			try {
				ScriptBean bean = (ScriptBean)config.validate(loginBean, requestContext);
				bean.updateScript(config, config.domain, null, null);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				Script instance = bean.getInstance();
				config = instance.buildConfig();
				config.isAdmin = bean.isAdmin();
				config.avatar = bean.getAvatarImage(bean.getInstance());
				return config;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("update-avatar")
	public AvatarConfig updateAvatar(AvatarConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-avatar", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			try {
				AvatarBean bean = (AvatarBean)config.validate(loginBean, requestContext);
				config.categories = config.getCategories();
				config.accessMode = config.getAccessMode();
				bean.updateAvatar(config, config.domain, null, null);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				Avatar instance = bean.getInstance();
				config = instance.buildConfig();
				config.isAdmin = bean.isAdmin();
				config.avatar = bean.getAvatarImage(bean.getInstance());
				return config;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("update-channel")
	public ChannelConfig updateChannel(ChannelConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-channel", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			try {
				LiveChatBean bean = (LiveChatBean)config.validate(loginBean, requestContext);
				bean.updateInstance(config, config.domain, null, null);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				ChatChannel instance = bean.getInstance();
				config = instance.buildConfig();
				config.isAdmin = bean.isAdmin();
				config.avatar = bean.getAvatarImage(bean.getInstance());
				return config;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("update-domain")
	public DomainConfig updateDomain(DomainConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-domain", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			DomainBean bean = (DomainBean)config.validate(loginBean, requestContext);
			bean.updateDomain(config, false, false);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			Domain instance = bean.getInstance();
			config = instance.buildConfig();
			config.isAdmin = bean.isAdmin();
			config.avatar = bean.getAvatarImage(bean.getInstance());
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create-user")
	public UserConfig createUser(UserConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API create-user");
		try {
			LoginBean loginBean = new LoginBean();
			config.validateApplication(loginBean, requestContext);
			if (config.source == null || config.source.isEmpty()) {
				config.source = "api";
			}
			if (config.affiliate == null || config.affiliate.isEmpty()) {
				config.affiliate = loginBean.getAppUser();
			}
			loginBean.createUser(config.user, config.password, config.password, config.dateOfBirth, config.hint, config.name, requestContext.getRemoteAddr(), config.source, config.affiliate,
					config.userAccess, config.email, config.website, config.bio, config.displayName, config.over18,
					config.credentialsType, config.credentialsUserID, config.credentialsToken,
					true);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			User user = loginBean.getUser();
			AppIDStats stats = AppIDStats.getStats(loginBean.getApplicationId(), user.getUserId());
			stats.userCreates++;
			config.password = null;
			config.token = String.valueOf(user.getToken());
			config.avatar = loginBean.getAvatarImage(user);
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("verify-email")
	public UserConfig verifyEmail(UserConfig config, @Context HttpServletRequest requestContext) {
		try {
			LoginBean loginBean = new LoginBean();
			config.validateApplication(loginBean, requestContext);
			loginBean.connect(config.user, config.password, config.getToken());
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			updateUser(config, requestContext);
			User user = loginBean.getUser();
			user.checkApplicationId();
			loginBean.sendEmailVerify(user);
			UserConfig result = new UserConfig(user, true);
			return result;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("delete-user-account")
	public UserConfig deleteUserAccount(UserConfig config, @Context HttpServletRequest requestContext) {
		try {
			LoginBean loginBean = new LoginBean();
			config.validateApplication(loginBean, requestContext);
			loginBean.connect(config.user, config.password, 0);
			loginBean.setViewUser(loginBean.getUser());
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			checkUser(config, requestContext);
			loginBean.deleteUser();
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			return null;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("flag-instance")
	public void flagInstance(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API flag-instance");
		flag(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("flag-forum")
	public void flagForum(ForumConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API flag-forum");
		flag(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("flag-issuetracker")
	public void flagIssueTracker(IssueTrackerConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API flag-issuetracker");
		flag(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("flag-graphic")
	public void flagGraphic(GraphicConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API flag-graphic");
		flag(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("flag-script")
	public void flagScript(ScriptConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API flag-script");
		flag(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("flag-avatar")
	public void flagAvatar(AvatarConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API flag-avatar");
		flag(config, requestContext);
	}

	@SuppressWarnings("rawtypes")
	public void flag(WebMediumConfig config, @Context HttpServletRequest requestContext) {
		try {
			LoginBean loginBean = new LoginBean();
			try {
				WebMediumBean bean = config.validate(loginBean, requestContext);
				bean.flagInstance(config.flaggedReason, "on");
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-up-avatar")
	public void thumbsUpAvatar(AvatarConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-up-avatar");
		thumbsUp(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-up-instance")
	public void thumbsUpBot(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-up-instance");
		thumbsUp(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-up-graphic")
	public void thumbsUpGraphic(GraphicConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-up-graphic");
		thumbsUp(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-up-script")
	public void thumbsUpScript(ScriptConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-up-script");
		thumbsUp(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-up-domain")
	public void thumbsUpDomain(DomainConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-up-domain");
		thumbsUp(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-up-forum")
	public void thumbsUpForum(ForumConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-up-forum");
		thumbsUp(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("subscribe-forum")
	public void subscribeForum(ForumConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API subscribe-forum");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				ForumBean bean = (ForumBean)config.validate(loginBean, requestContext);
				bean.subscribe();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("unsubscribe-forum")
	public void unsubscribeForum(ForumConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API unsubscribe-forum");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				ForumBean bean = (ForumBean)config.validate(loginBean, requestContext);
				bean.unsubscribe();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("subscribe-post")
	public void subscribePost(ForumPostConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API subscribe-post");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				ForumPostBean bean = config.validate(loginBean, requestContext);
				bean.subscribe();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("unsubscribe-post")
	public void unsubscribePost(ForumPostConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API unsubscribe-post");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				ForumPostBean bean = config.validate(loginBean, requestContext);
				bean.unsubscribe();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("subscribe-issuetracker")
	public void subscribeIssueTracker(IssueTrackerConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API subscribe-issuetracker");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				IssueTrackerBean bean = (IssueTrackerBean)config.validate(loginBean, requestContext);
				bean.subscribe();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("unsubscribe-issuetracker")
	public void unsubscribeForum(IssueTrackerConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API unsubscribe-issuetracker");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				IssueTrackerBean bean = (IssueTrackerBean)config.validate(loginBean, requestContext);
				bean.unsubscribe();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("subscribe-issue")
	public void subscribeIssue(IssueConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API subscribe-issue");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				IssueBean bean = config.validate(loginBean, requestContext);
				bean.subscribe();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("unsubscribe-issue")
	public void unsubscribeIssue(IssueConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API unsubscribe-issue");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				IssueBean bean = config.validate(loginBean, requestContext);
				bean.unsubscribe();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-up-channel")
	public void thumbsUpChannel(ChannelConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-up-channel");
		thumbsUp(config, requestContext);
	}

	@SuppressWarnings("rawtypes")
	public void thumbsUp(WebMediumConfig config, @Context HttpServletRequest requestContext) {
		try {
			LoginBean loginBean = new LoginBean();
			try {
				WebMediumBean bean = config.validate(loginBean, requestContext);
				bean.thumbsUp();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-down-avatar")
	public void thumbsDownAvatar(AvatarConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-down-avatar");
		thumbsDown(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-down-instance")
	public void thumbsDownBot(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-down-instance");
		thumbsDown(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-down-graphic")
	public void thumbsDownGraphic(GraphicConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-down-graphic");
		thumbsDown(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-down-script")
	public void thumbsDownScript(ScriptConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-down-script");
		thumbsDown(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-down-domain")
	public void thumbsDownDomain(DomainConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-down-domain");
		thumbsDown(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-down-forum")
	public void thumbsDownForum(ForumConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-down-forum");
		thumbsDown(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-down-channel")
	public void thumbsDownChannel(ChannelConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-down-channel");
		thumbsDown(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-down-post")
	public void thumbsDownPost(ForumPostConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-down-post");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				ForumPostBean bean = config.validate(loginBean, requestContext);
				bean.thumbsDown();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-up-post")
	public void thumbsUpPost(ForumPostConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-up-post");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				ForumPostBean bean = config.validate(loginBean, requestContext);
				bean.thumbsUp();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("star-post")
	public void starPost(ForumPostConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API star-post");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				ForumPostBean bean = config.validate(loginBean, requestContext);
				bean.star(Integer.valueOf(config.stars));
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("star-issue")
	public void starIssue(IssueConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API star-issue");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				IssueBean bean = config.validate(loginBean, requestContext);
				bean.star(Integer.valueOf(config.stars));
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-down-issue")
	public void thumbsDownIssue(IssueConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-down-issue");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				IssueBean bean = config.validate(loginBean, requestContext);
				bean.thumbsDown();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("thumbs-up-issue")
	public void thumbsUpIssue(IssueConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API thumbs-up-issue");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				IssueBean bean = config.validate(loginBean, requestContext);
				bean.thumbsUp();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@SuppressWarnings("rawtypes")
	public void thumbsDown(WebMediumConfig config, @Context HttpServletRequest requestContext) {
		try {
			LoginBean loginBean = new LoginBean();
			try {
				WebMediumBean bean = config.validate(loginBean, requestContext);
				bean.thumbsDown();
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("star-avatar")
	public void starAvatar(AvatarConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API star-avatar");
		star(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("star-instance")
	public void starBot(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API star-instance");
		star(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("star-graphic")
	public void starGraphic(GraphicConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API star-graphic");
		star(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("star-script")
	public void starScript(ScriptConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API star-script");
		star(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("star-domain")
	public void starDomain(DomainConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API star-domain");
		star(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("star-forum")
	public void starForum(ForumConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API star-forum");
		star(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("star-issuetracker")
	public void starIssueTracker(IssueTrackerConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API star-issuetracker");
		star(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("star-channel")
	public void starChannel(ChannelConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API star-channel");
		star(config, requestContext);
	}

	@SuppressWarnings("rawtypes")
	public void star(WebMediumConfig config, @Context HttpServletRequest requestContext) {
		try {
			LoginBean loginBean = new LoginBean();
			try {
				WebMediumBean bean = config.validate(loginBean, requestContext);
				bean.star(Integer.valueOf(config.stars));
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("flag-user")
	public void flagUser(UserConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API flag-user");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				loginBean.flagUser("");
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("flag-forum-post")
	public void flagForumPost(ForumPostConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API flag-forum-post");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				ForumPostBean bean = config.validate(loginBean, requestContext);
				bean.flagInstance(config.flaggedReason, "on");
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("flag-issue")
	public void flagIssue(IssueConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API flag-issue");
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				IssueBean bean = config.validate(loginBean, requestContext);
				bean.flagInstance(config.flaggedReason, "on");
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("flag-domain")
	public void flagDomain(DomainConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API flag-domain");
		flag(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("flag-channel")
	public void flagChannel(ChannelConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API flag-channel");
		flag(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("delete-instance")
	public void deleteInstance(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API delete-instance");
		delete(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("delete-forum")
	public void deleteForum(ForumConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API delete-forum");
		delete(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("delete-issuetracker")
	public void deleteIssueTracker(IssueTrackerConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API delete-issuetracker");
		delete(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("delete-graphic")
	public void deleteGraphic(GraphicConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API delete-graphic");
		delete(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("delete-script")
	public void deleteScript(ScriptConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API delete-script");
		delete(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("delete-avatar")
	public void deleteAvatar(AvatarConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API delete-avatar-instance");
		delete(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("delete-domain")
	public void deleteDomain(DomainConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API delete-domain");
		delete(config, requestContext);
	}

	@SuppressWarnings("rawtypes")
	public void delete(WebMediumConfig config, @Context HttpServletRequest requestContext) {
		try {
			LoginBean loginBean = new LoginBean();
			try {
				WebMediumBean bean = config.validate(loginBean, requestContext);
				bean.deleteInstance(true);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				return;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("delete-channel")
	public void deleteChannel(ChannelConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API delete-channel");
		delete(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("edit-user")
	public void editUser(UserConfig config, @Context HttpServletRequest requestContext) {
		updateUser(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("update-user")
	public UserConfig updateUser(UserConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API update-user", config.user);
		try {
			LoginBean loginBean = new LoginBean();
			loginBean.connect(config.user, config.password, config.getToken());
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			loginBean.updateUser(config.password, config.newPassword, config.newPassword, config.hint, config.name, config.userAccess, config.tags, config.email, config.source, null, null, null,
					config.website, config.bio, config.showName, config.over18, config.adCode, null, "");
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			User user = loginBean.getUser();
			UserConfig result = new UserConfig(user, true);
			result.avatar = loginBean.getAvatarImage(user);
			return result;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("upgrade-user")
	public UserConfig upgradeUser(UpgradeConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API upgrade-user", config.user);
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			if (Site.COMMERCIAL) {
				DomainBean domainBean = loginBean.getBean(DomainBean.class);
				domainBean.upgradeDomain(config);
			} else {
				loginBean.upgradeUser(config);
			}
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			User user = loginBean.getUser();
			UserConfig result = new UserConfig(user, true);
			result.avatar = loginBean.getAvatarImage(user);
			return result;
		} catch (Throwable failed) {
			AdminDatabase.instance().log(Level.WARNING, "Upgrade failed: exception", config.orderId, failed);
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("chat-war")
	public InstanceConfig chatWar(ChatWarConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API chat-war", config.user);
		try {
			LoginBean loginBean = new LoginBean();
			config.connect(loginBean, requestContext);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			ChatWarBean bean = loginBean.getBean(ChatWarBean.class);
			bean.startWar(config.winner, config.looser, config.topic);
			bean.endWar(config.winner);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			InstanceConfig instanceConfig = bean.getInstance1().getInstance().buildConfig();
			bean.disconnect();
			return instanceConfig;
		} catch (Throwable failed) {
			AdminDatabase.instance().log(Level.WARNING, "Chat war failed: exception", failed);
			error(failed);
			return null;
		}
	}

	@GET
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/form-check-user")
	public UserConfig checkUser(
			@QueryParam("application") String application,
			@QueryParam("user") String user,
			@QueryParam("password") String password,
			@QueryParam("token") String token,
			@Context HttpServletRequest requestContext) {
		
		AdminDatabase.instance().log(Level.INFO, "API FORM check-user", user, application, requestContext.getRemoteAddr());
		UserConfig config = new UserConfig();
		config.application = application;
		config.user = user;
		config.password = password;
		config.token = token;
		return checkUser(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-bot-channel")
	public ChannelConfig getBotChannel(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-bot-channel", config.user, config.application, requestContext.getRemoteAddr());
		ChatChannel chatChannel  = null;
		ChannelConfig channelConfig = new ChannelConfig();
		try {
			LoginBean loginBean = new LoginBean();
			if (config.user != null) {
				loginBean.connect(config.user, config.password, config.getToken());
			}
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			LiveChatBean livechatBean = loginBean.getBean(LiveChatBean.class);
			BotBean botBean = loginBean.getBotBean();
			botBean.validateInstance(config.id);
			if ("live-chat".equals(config.channelType)) {
				if (botBean.getInstance() != null && !livechatBean.validateInstance(botBean.getInstanceName() + " Live Chat", botBean.getInstance().getDomain())) {
					loginBean.setError(null);
					livechatBean.createInstance(botBean.getInstanceName() + " Live Chat", ChannelType.OneOnOne, botBean.getBotBean());
				}
			} else if ("chat-room".equals(config.channelType)) {
				if (botBean.getInstance() != null && !livechatBean.validateInstance(botBean.getInstanceName() + " Chat Room", botBean.getInstance().getDomain())) {
					loginBean.setError(null);
					livechatBean.createInstance(botBean.getInstanceName() + " Chat Room", ChannelType.ChatRoom, botBean.getBotBean());
				}
			}			
			chatChannel = livechatBean.getInstance();
			if (chatChannel != null) {
				channelConfig = (ChannelConfig) chatChannel.buildConfig();
			}
		} catch (Throwable error) {
			error(error);
			return null;
		}	
		return channelConfig;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get-user-channel")
	public ChannelConfig getUserChannel(UserConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-user-channel", config.user, config.application, requestContext.getRemoteAddr());
		ChatChannel chatChannel  = null;
		ChannelConfig channelConfig = new ChannelConfig();
		LoginBean loginBean = new LoginBean();
		try {
			config.validateApplication(loginBean, requestContext);
			User user = null;
			if (config.password != null || config.token != null) {
				loginBean.connect(config.user, config.password, config.getToken());
				user = loginBean.getUser();
				user.checkApplicationId();
			} else {
				loginBean.viewUser(config.user);
				user = loginBean.getViewUser();
				user.checkApplicationId();
				if (config.user.startsWith("@")) {
					String botName = config.user.substring(1);
					if (!loginBean.getBotBean().validateInstance(botName)) {
						throw new BotException("User bot - " + botName + " doesn't exist");
					}
					BotInstance botInstance = loginBean.getBotBean().getInstance();
					InstanceConfig instanceConfig = new InstanceConfig();
					instanceConfig.id = botInstance.getId().toString();
					instanceConfig.user = config.user; 
					instanceConfig.name = botName;
					instanceConfig.password = user.getPassword();
					Long token = user.getToken();
					if (token != null) {
						instanceConfig.token = token.toString();
					}
					instanceConfig.channelType = config.channelType;
					channelConfig = getBotChannel(instanceConfig, requestContext);
					return channelConfig;
				}
				loginBean.connect(user.getUserId(), user.getPassword(), user.getToken());
			}
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			LiveChatBean livechatBean = loginBean.getBean(LiveChatBean.class);
			Domain domain = loginBean.getDomain();
			BotInstance botInstance = null;
			if (config.user.startsWith("@")) {
				config.user = config.user.substring(1);
				if (loginBean.getBotBean().validateInstance(config.user)) {
					botInstance = loginBean.getBotBean().getInstance();
				}
			} else {
				if (loginBean.getBotBean().validateInstance(config.user + " Bot")) {
					botInstance = loginBean.getBotBean().getInstance();
				}
			}
			if (botInstance == null) {
				InstanceConfig instanceConfig = new InstanceConfig();
				instanceConfig.user = config.user;
				instanceConfig.name = config.user + " Bot";
				instanceConfig.application = config.applicationId;
				instanceConfig.token = config.token;
				instanceConfig.domain = domain.getId().toString();
				instanceConfig.categories = "Personal";
				BotBean botBean = loginBean.getBotBean();
				boolean createInstance = botBean.createInstance(instanceConfig, false, false, requestContext.getRemoteAddr());
				if (createInstance) {
					botInstance = botBean.getInstance();
				}
			}
			if (botInstance == null) {
				return null;
			}
			Long botId = botInstance.getId();
			if ("live-chat".equals(config.channelType)) {
				if (user != null && !livechatBean.validateInstance(user.getUserId() + " Live Chat", domain)) {
					loginBean.setError(null);
					livechatBean.createUserChannelInstance(user.getUserId() + " Live Chat", ChannelType.OneOnOne, user, domain, botId);
				}
			} else if ("chat-room".equals(config.channelType)) {
				if (user != null && !livechatBean.validateInstance(user.getUserId() + " Chat Room", domain)) {
					loginBean.setError(null);
					livechatBean.createUserChannelInstance(user.getUserId() + " Chat Room", ChannelType.ChatRoom, user, domain, botId);
				}
			}
			chatChannel = livechatBean.getInstance();
			if (chatChannel != null) {
				channelConfig = (ChannelConfig) chatChannel.buildConfig();
			}
		} catch (Throwable error) {
			error(error);
			return null;
		}	
		return channelConfig;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("check-user")
	public UserConfig checkUser(UserConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API check-user", config.user, config.application, requestContext.getRemoteAddr());
		return processCheckUser(config, requestContext);
	}
	
	public UserConfig processCheckUser(UserConfig config, HttpServletRequest requestContext) {
		try {
			LoginBean loginBean = new LoginBean();
			if (config.credentialsType != null && !config.credentialsType.isEmpty()) {
				loginBean.credentialsConnect(config.credentialsType, config.credentialsUserID, config.credentialsToken);
			} else {
				loginBean.connect(config.user, config.password, config.getToken());
			}
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			User user = loginBean.getUser();
			user.checkApplicationId();
			UserConfig result = new UserConfig(user, true);
			result.avatar = loginBean.getAvatarImage(user);
			result.newMessage = hasNewMessage(user);
			return result;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	public boolean hasNewMessage(User user) {
		try {
			List<UserMessage> messagesList = AdminDatabase.instance().getNewUserMessages(user);
			if (!messagesList.isEmpty()) {
				return true;
			}
		} catch (Exception failed) {
			AdminDatabase.instance().log(failed);
		}
		return false;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("view-user")
	public UserConfig viewUser(UserConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API view-user", config.user);
		try {
			LoginBean loginBean = new LoginBean();
			config.validateApplication(loginBean, requestContext);
			loginBean.viewUser(config.user);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			User user = loginBean.getViewUser();
			UserConfig result = new UserConfig(user, false);
			result.avatar = loginBean.getAvatarImage(user);
			result.avatarThumb = loginBean.getAvatarThumb(user);
			return result;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("get-users")
	public List<UserConfig> getUsers(UserConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API get-users", config.user);
		try {
			String appUser = AdminDatabase.instance().validateApplicationId(config.application, null);
			if (appUser != null) {
				AppIDStats.getStats(config.application, appUser).apiCalls++;
			}
			LoginBean loginBean = new LoginBean();
			config.checkDomain(loginBean);
			List<User> users = loginBean.getUsers(config.user);
			if (loginBean.getError() != null) {
				throw loginBean.getError();
			}
			List<UserConfig> configs = new ArrayList<UserConfig>();
			for (User user : users) {
				UserConfig userConfig = new UserConfig(user, false);
				if (user.getUserId().startsWith("anonymous")) {
					userConfig.avatar = loginBean.getAvatarThumb((User)null);
				} else {
					try {
						userConfig.avatar = loginBean.getAvatarThumb(user);
					} catch (Exception missing) {
						userConfig.avatar = loginBean.getAvatarThumb((User)null);
					}
				}
				configs.add(userConfig);
			}
			return configs;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@GET
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/form-check-instance")
	public InstanceConfig checkInstance(
			@QueryParam("application") String application,
			@QueryParam("id") String id,
			@QueryParam("name") String name,
			@QueryParam("user") String user,
			@QueryParam("password") String password,
			@QueryParam("token") String token,
			@Context HttpServletRequest requestContext) {
		
		AdminDatabase.instance().log(Level.INFO, "API FORM check-instance", user);
		InstanceConfig config = new InstanceConfig();
		config.application = application;
		config.id = id;
		config.name = name;
		config.user = user;
		config.password = password;
		config.token = token;
		return checkInstance(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("init-avatar")
	public ChatResponse initAvatar(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API init-avatar", config.name);
		try {
			LoginBean loginBean = new LoginBean();
			try {
				BotBean bean = (BotBean)config.validate(loginBean, requestContext);
				return bean.initAvatar();
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("init-chat")
	public ChatResponse initAvatar(ChatMessage config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API init-chat", config.instance);
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				BotBean bean = loginBean.getBotBean();
				bean.validateInstance(config.instance);
				if (loginBean.getError() != null) {
					throw loginBean.getError();
				}
				bean.setAvatarFormat(config.avatarFormat);
				bean.setAvatarHD(config.avatarHD);
				return bean.initAvatar();
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("check-instance")
	public InstanceConfig checkInstance(InstanceConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API check-instance", config.name);
		return (InstanceConfig)check(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("check-forum")
	public ForumConfig checkForum(ForumConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API check-forum", config.name);
		return (ForumConfig)check(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("check-issuetracker")
	public IssueTrackerConfig checkIssueTracker(IssueTrackerConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API check-issuetracker", config.name);
		return (IssueTrackerConfig)check(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("check-graphic")
	public GraphicConfig checkGraphic(GraphicConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API check-graphic", config.name);
		return (GraphicConfig)check(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("check-script")
	public ScriptConfig checkScript(ScriptConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API check-script", config.name);
		return (ScriptConfig)check(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("check-avatar")
	public AvatarConfig checkAvatar(AvatarConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API check-avatar", config.name);
		return (AvatarConfig)check(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("check-forum-post")
	public ForumPostConfig checkForumPost(ForumPostConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API check-forum-post", config.id);
		try {
			LoginBean loginBean = new LoginBean();
			ForumPostBean bean = config.validate(loginBean, requestContext);
			ForumPost instance = bean.getInstance();
			config = instance.buildConfig();
			if (bean.isAdmin()) {
				config.isAdmin = true;
			}
			config.avatar = bean.getAvatarThumb(bean.getInstance());
			if (config.replies != null) {
				int index = 0;				
				for (ForumPostConfig reply : config.replies) {
					reply.avatar = bean.getAvatarThumb(instance.getReplies().get(index));
					index++;
				}
			}
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("check-issue")
	public IssueConfig checkIssue(IssueConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API check-issue", config.id);
		try {
			LoginBean loginBean = new LoginBean();
			IssueBean bean = config.validate(loginBean, requestContext);
			Issue instance = bean.getInstance();
			config = instance.buildConfig();
			if (bean.isAdmin()) {
				config.isAdmin = true;
			}
			config.avatar = bean.getAvatarThumb(bean.getInstance());
			return config;
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@GET
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Path("/translate")
	public String translate(
			@QueryParam("application") String application,
			@QueryParam("text") String text,
			@QueryParam("from") String from,
			@QueryParam("to") String to,
			@Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API translate", text, application, requestContext.getRemoteAddr());
		try {
			LoginBean loginBean = new LoginBean();
			try {
				SpeechConfig config = new SpeechConfig();
				config.application = application;
				config.connect(loginBean, requestContext);
				return BotTranslationService.instance().translate(text, from, to);
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@GET
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Path("/form-speak")
	public String speak(
			@QueryParam("application") String application,
			@QueryParam("text") String text,
			@QueryParam("voice") String voice,
			@QueryParam("mod") String mod,
			@QueryParam("apiKey") String apiKey,
			@QueryParam("apiEndpoint") String apiEndpoint,
			@QueryParam("apiToken") String apiToken,
			@QueryParam("provider") String provider,
			@QueryParam("instance") String instance,
			@QueryParam("embeddedAvatar") boolean embeddedAvatar,
			@Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API FORM speak", text, application, requestContext.getRemoteAddr());
		SpeechConfig config = new SpeechConfig();
		config.application = application;
		config.text = text;
		config.voice = voice;
		config.mod = mod;
		config.instance = instance;
		config.apiKey = apiKey;
		config.apiEndpoint = apiEndpoint;

		if (provider != null && provider.equals("bing")) {
			return processSpeakBing(config, embeddedAvatar, requestContext);
		} else if (provider != null && provider.equals("qq")) {
			return processSpeakQQ(config, embeddedAvatar, requestContext);
		} else {
			return processSpeak(config, requestContext); 
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_HTML)
	@Path("speak")
	public String speak(SpeechConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API speak", config.text, config.application, requestContext.getRemoteAddr());
		return processSpeak(config, requestContext);
	}

	public String processSpeak(SpeechConfig config, HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API speak", config.text, config.application, requestContext.getRemoteAddr());
		try {
			LoginBean loginBean = new LoginBean();
			try {
				config.connect(loginBean, requestContext);
				return ChatBean.speak(ChatBean.prepareSpeechText(config.text), config.voice, config.mod);
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public WebMediumConfig check(WebMediumConfig config, @Context HttpServletRequest requestContext) {
		try {
			LoginBean loginBean = new LoginBean();
			try {
				WebMediumBean bean = config.validate(loginBean, requestContext);
				bean.apiConnect();
				WebMedium instance = (WebMedium)bean.getInstance();
				config = instance.buildConfig();
				if (bean.isAdmin()) {
					config.isAdmin = true;
				}
				config.avatar = bean.getAvatarImage(instance);
				return config;
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("check-channel")
	public ChannelConfig checkChannel(ChannelConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API check-channel", config.name);
		return (ChannelConfig)check(config, requestContext);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("check-domain")
	public DomainConfig checkDomain(DomainConfig config, @Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API check-domain", config.name);
		return (DomainConfig)check(config, requestContext);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/slack/{application}/{instance}")
	public SlackJSONResponse slack(
			@PathParam("application") String application,
			@PathParam("instance") String instance,
			@FormParam("token") String token,
			@FormParam("team_id") String team_id,
			@FormParam("team_domain") String team_domain,
			@FormParam("channel_id") String channel_id,
			@FormParam("channel_name") String channel_name,
			@FormParam("timestamp") String timestamp,
			@FormParam("user_id") String user_id,
			@FormParam("user_name") String user_name,
			@FormParam("text") String text,
			@FormParam("trigger_word") String trigger_word,
			@FormParam("payload") String payload,
			@Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API POST slack", team_domain, user_name, text);

		Stats.stats.botSlackAPI++;
		if (payload!=null) {
			return null; //TODO: something with payload
		}
		// Ignore message if it came from slackbot, otherwise conversation will loop infinity.
		if (user_name.equals("slackbot")) {
			return null;
		}
		LoginBean loginBean = new LoginBean();
		BotBean bean = null;
		Bot bot = null;
		InstanceConfig config = new InstanceConfig();
		config.id = instance;
		config.application = application;
		
		try {
			config.validateApplication(loginBean, null);
			config.user = loginBean.getAppUser();
			loginBean.setUser(new User(config.user, null));
			loginBean.validateUser(config.user, null, 0, false, true);
			loginBean.setLoggedIn(true);
			bean = loginBean.getBotBean();
			bean.validateInstance(config.id);
		} catch (Throwable exception) {
			error(exception);
			return null;
		}
		try {
			if (text == null || text.isEmpty()) {
				return null;
			}
			bot = bean.connectInstance();
			BotStats stats = BotStats.getStats(bean.getInstanceId(), bean.getInstanceName());
			Slack slack = bot.awareness().getSense(Slack.class);
			Language language = bot.mind().getThought(Language.class);
			resetStats(stats, slack, language);
			stats.slackMessagesProcessed++;
			Stats.stats.botSlackMessagesProcessed++;
			long startTime = System.currentTimeMillis();
			String reply = slack.processMessage(user_id, user_name, channel_id, text, token);
			if (reply == null || reply.isEmpty()) {
				return null;
			}
			updateStats(stats, slack, language, startTime);
			resetStats(stats, slack, language);
			SlackJSONResponse response = new SlackJSONResponse("@" + user_name + " " + reply);
			return response;
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
		} finally {
			if (bot != null) {
				int count = 0;
				while ((count < 50) && !bot.memory().getActiveMemory().isEmpty()) {
					count++;
					Utils.sleep(100);
				}
			}
			bean.disconnectInstance();
			loginBean.disconnect();
		}
		
		return null;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN) //APPLICATION_FORM_URLENCODED
	@Path("/slack/{application}/{instance}")
	public String validateSlack(
			@PathParam("application") String application,
			@PathParam("instance") String instance,
			String json,
			@Context HttpServletRequest requestContext) {
		
		AdminDatabase.instance().log(Level.INFO, "API POST slack", application, instance);
		
		LoginBean loginBean = new LoginBean();
		BotBean bean = null;
		InstanceConfig config = new InstanceConfig();
		config.id = instance;
		config.application = application;
		try {
			config.validateApplication(loginBean, null);
			config.user = loginBean.getAppUser();
			loginBean.setUser(new User(config.user, null));
			loginBean.validateUser(config.user, null, 0, false, true);
			loginBean.setLoggedIn(true);
			bean = loginBean.getBotBean();
			bean.validateInstance(config.id);
		} catch (Throwable exception) {
			error(exception);
			return null;
		}
		
		//TODO: VERIFY SLACK APP TOKEN
		
		JSONObject root = (JSONObject)JSONSerializer.toJSON(json);
		
		if(root.optString("type").equals("url_verification")) {
			String challenge = root.optString("challenge");
			return challenge;
		} else if(root.optString("type").equals("event_callback")) {
			Bot bot = null;
			bot = bean.connectInstance();
			Slack slack = bot.awareness().getSense(Slack.class);
			slack.processSlackEvent(json);
			
			return "OK";
		}
		
		return null;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/skype/{application}/{instance}")
	public String skype(
			@PathParam("application") String application,
			@PathParam("instance") String instance,
			String json,
			@Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API POST skype", instance);

		Stats.stats.botSkypeAPI++;
		LoginBean loginBean = new LoginBean();
		BotBean bean = null;
		Bot bot = null;
		InstanceConfig config = new InstanceConfig();
		config.id = instance;
		config.application = application;
		try {
			config.validateApplication(loginBean, null);
			config.user = loginBean.getAppUser();
			loginBean.setUser(new User(config.user, null));
			loginBean.validateUser(config.user, null, 0, false, true);
			loginBean.setLoggedIn(true);
			bean = loginBean.getBotBean();
			bean.validateInstance(config.id);
		} catch (Throwable exception) {
			error(exception);
			return null;
		}
		try {
			bot = bean.connectInstance();
			BotStats stats = BotStats.getStats(bean.getInstanceId(), bean.getInstanceName());
			Skype skype = bot.awareness().getSense(Skype.class);
			Language language = bot.mind().getThought(Language.class);
			long startTime = System.currentTimeMillis();
			resetStats(stats, skype, language);
			stats.skypeMessagesProcessed++;
			Stats.stats.botSkypeMessagesProcessed++;
			String response = skype.processMessage(json);
			if (response == null || response.isEmpty()) {
				return null;
			}
			updateStats(stats, skype, language, startTime);
			resetStats(stats, skype, language);
			return response;
			
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
		} finally {
			if (bot != null) {
				int count = 0;
				while ((count < 50) && !bot.memory().getActiveMemory().isEmpty()) {
					count++;
					Utils.sleep(100);
				}
			}
			bean.disconnectInstance();
			loginBean.disconnect();
		}
		
		return null;
	}
	
	@POST
	@Consumes(MediaType.TEXT_XML)
	@Produces(MediaType.TEXT_XML)
	@Path("/wechat/{application}/{instance}")
	public WeChatXMLResponse wechat(
			@PathParam("application") String application,
			@PathParam("instance") String instance,
			WeChatXMLResponse message,
			@Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API POST wechat", instance);

		Stats.stats.botWeChatAPI++;
		LoginBean loginBean = new LoginBean();
		BotBean bean = null;
		Bot bot = null;
		InstanceConfig config = new InstanceConfig();
		config.id = instance;
		config.application = application;
		try {
			config.validateApplication(loginBean, null);
			config.user = loginBean.getAppUser();
			loginBean.setUser(new User(config.user, null));
			loginBean.validateUser(config.user, null, 0, false, true);
			loginBean.setLoggedIn(true);
			bean = loginBean.getBotBean();
			bean.validateInstance(config.id);
		} catch (Throwable exception) {
			error(exception);
			return null;
		}
		try {
			/*JAXBContext context = JAXBContext.newInstance(WeChatXMLResponse.class);
			Unmarshaller marshaller = context.createUnmarshaller();
			StringReader reader = new StringReader(xml);
			WeChatXMLResponse message = (WeChatXMLResponse)marshaller.unmarshal(reader);*/
		
			bot = bean.connectInstance();
			BotStats stats = BotStats.getStats(bean.getInstanceId(), bean.getInstanceName());
			WeChat wechat = bot.awareness().getSense(WeChat.class);
			Language language = bot.mind().getThought(Language.class);
			resetStats(stats, wechat, language);
			stats.wechatMessagesProcessed++;
			Stats.stats.botWeChatMessagesProcessed++;
			if (message.MsgType != null) {
				long startTime = System.currentTimeMillis();
				String response = null;
				if(message.MsgType.equalsIgnoreCase("text")) {
					response = wechat.processMessage(message.FromUserName, message.ToUserName, message.Content, message.FromUserName);
				} else if (message.MsgType.equalsIgnoreCase("voice") && message.Recognition != null) {
					response = wechat.processMessage(message.FromUserName, message.ToUserName, message.Recognition, message.FromUserName);
				} else if (message.MsgType.equalsIgnoreCase("event") && message.Event != null) {
					if(message.Event.equalsIgnoreCase("click") && message.EventKey != null) {
						response = wechat.processMessage(message.FromUserName, message.ToUserName, message.EventKey, message.FromUserName);
					}
				}
				if (response == null || response.isEmpty()) {
					return null;
				}
				updateStats(stats, wechat, language, startTime);
				resetStats(stats, wechat, language);
				WeChatXMLRichMediaResponse xmlResponse = new WeChatXMLRichMediaResponse();
				xmlResponse.ToUserName = message.FromUserName;
				xmlResponse.FromUserName = message.ToUserName;
				xmlResponse.CreateTime = message.CreateTime;
				xmlResponse.MsgType = "text";
				xmlResponse.Content = response;
				
				//Check for rich media message
				Element root = null; 
				boolean imageAndLinkFound = false;
				if ((response.indexOf('<') != -1) && (response.indexOf('>') != -1)) {
					try {
						root = bot.awareness().getSense(Http.class).parseHTML(response);
						
						NodeList nodes = root.getElementsByTagName("a");
						if (nodes.getLength() > 0) {
							String href = "";
							String imageUrl = null;
							String imageTitle = "...";
							String imageDesc = "...";
							int count = 0;
							for (int index = 0; index  < nodes.getLength(); index++) {
								Element node = (Element)nodes.item(index);
								NodeList imageNodes = node.getElementsByTagName("img");
								if (imageNodes.getLength() > 0) {
									//Link and image
									String src = ((Element)imageNodes.item(0)).getAttribute("src");
									if (src != null && !src.isEmpty()) {
										imageAndLinkFound = true;
										imageUrl = src;
										String title = ((Element)imageNodes.item(0)).getAttribute("title");
										if (title != null && !title.isEmpty()) {
											imageTitle = title;
										}
										String desc = ((Element)imageNodes.item(0)).getAttribute("alt");
										if (desc != null && !desc.isEmpty()) {
											imageDesc = desc;
										}
									}
									href = node.getAttribute("href");
									xmlResponse.addNewItem(imageTitle, imageDesc, imageUrl, href);
								} else {
									//Link but no image
									xmlResponse.MsgType = "link";
									xmlResponse.Content = null;
									String title = node.getAttribute("title");
									xmlResponse.Title = title != null ? title : "";
									String desc = node.getTextContent();
									xmlResponse.Description = desc != null ? desc : "";
									xmlResponse.Url = node.getAttribute("href");
								}
							}
						} else {
							//Image but no link
							nodes = root.getElementsByTagName("img");
							if(nodes.getLength() > 0) {
								xmlResponse.MsgType = "image";
								xmlResponse.Content = null;
								xmlResponse.PicUrl = ((Element)nodes.item(0)).getAttribute("src");
							}
						}
					} catch (Exception e) {
						AdminDatabase.instance().log(e);
					}
				}
				
				if(imageAndLinkFound) {
					xmlResponse.MsgType = "news";
					xmlResponse.Content = null;
				}
				
				return xmlResponse;
			}
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
		} finally {
			if (bot != null) {
				int count = 0;
				while ((count < 50) && !bot.memory().getActiveMemory().isEmpty()) {
					count++;
					Utils.sleep(100);
				}
			}
			bean.disconnectInstance();
			loginBean.disconnect();
		}
		
		return null;
	}
	
	@GET
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/wechat/{application}/{instance}")
	public String wechatGET(
			@PathParam("application") String application,
			@PathParam("instance") String instance,
			@QueryParam("signature") String signature,
			@QueryParam("timestamp") String timestamp,
			@QueryParam("nonce") String nonce,
			@QueryParam("echostr") String echostr,
			@Context HttpServletRequest requestContext
			) {
		
		AdminDatabase.instance().log(Level.INFO, "API GET wechat", instance, signature, timestamp, nonce, echostr);
		
		String token = "";
		LoginBean loginBean = new LoginBean();
		BotBean bean = null;
		Bot bot = null;
		InstanceConfig config = new InstanceConfig();
		config.id = instance;
		config.application = application;
		try {
			config.validateApplication(loginBean, null);
			config.user = loginBean.getAppUser();
			loginBean.setUser(new User(config.user, null));
			loginBean.validateUser(config.user, null, 0, false, true);
			loginBean.setLoggedIn(true);
			bean = loginBean.getBotBean();
			bean.validateInstance(config.id);
		} catch (Throwable exception) {
			error(exception);
			return null;
		}
		
		try {
			bot = bean.connectInstance();
			WeChat wechat = bot.awareness().getSense(WeChat.class);
			token = wechat.getUserToken();	
			if (token == null) {
				AdminDatabase.instance().log(Level.INFO, "wechat invalid token", token);
				return null;
			}
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
		} finally {
			bean.disconnectInstance();
			loginBean.disconnect();
		}
		AdminDatabase.instance().log(Level.INFO, "wechat echo", echostr);
		return echostr;
		/**You should check whether the HTTP request is from WeChat by verifying the signature. If the signature is correct, you should return the echostr.

				The signature will be generated in the following way using the token (that you provided), timestamp and nonce.

				1. Sort the 3 values of token, timestamp and nonce alphabetically.
				2. Combine the 3 parameters into one string, encrypt it using SHA-1.
				3. Compare the SHA-1 digest string with the signature from the request. If they are the same, the access request is from WeChat./
		
		//1
		ArrayList<String> values = new ArrayList<String>();
		values.add(token);
		values.add(timestamp);
		values.add(nonce);
		Collections.sort(values);
		
		//2
		String valuesString = "";
		for(String s : values) {
			valuesString = valuesString.concat(s);
		}
		java.security.MessageDigest d = null;
		try {
			d = java.security.MessageDigest.getInstance("SHA-1");
			d.reset();
			d.update(valuesString.getBytes());
			valuesString = Utils.bytesToHex(d.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		//3
		if(valuesString.equals(signature)) {
			return echostr;
		} else {
			return null;
		}*/
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/kik/{application}/{instance}")
	public String kik(
			@PathParam("application") String application,
			@PathParam("instance") String instance,
			String json,
			@Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API POST kik", instance);

		Stats.stats.botKikAPI++;
		LoginBean loginBean = new LoginBean();
		BotBean bean = null;
		Bot bot = null;
		InstanceConfig config = new InstanceConfig();
		config.id = instance;
		config.application = application;
		try {
			config.validateApplication(loginBean, null);
			config.user = loginBean.getAppUser();
			loginBean.setUser(new User(config.user, null));
			loginBean.validateUser(config.user, null, 0, false, true);
			loginBean.setLoggedIn(true);
			bean = loginBean.getBotBean();
			bean.validateInstance(config.id);
		} catch (Throwable exception) {
			error(exception);
			return null;
		}
		try {
			bot = bean.connectInstance();
			BotStats stats = BotStats.getStats(bean.getInstanceId(), bean.getInstanceName());
			Kik kik = bot.awareness().getSense(Kik.class);
			Language language = bot.mind().getThought(Language.class);
			resetStats(stats, kik, language);
			stats.kikMessagesProcessed++;
			Stats.stats.botKikMessagesProcessed++;
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json);
			
			JSONArray messages = root.getJSONArray("messages");
			
			for (int i = 0; i < messages.size(); i++) {
				JSONObject message = messages.getJSONObject(i);
				
				if (message.optString("type").equals("text")) {
					String from = message.optString("from");
					String target = bot.getName();
					String text = message.optString("body");
					String id = message.optString("chatId");
					long startTime = System.currentTimeMillis();
					String response = kik.processMessage(from, target, text, id);
					if (response == null || response.isEmpty()) {
						return null;
					}
					updateStats(stats, kik, language, startTime);
					resetStats(stats, kik, language);
				}
			}
			return "OK";
			
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
		} finally {
			if (bot != null) {
				int count = 0;
				while ((count < 50) && !bot.memory().getActiveMemory().isEmpty()) {
					count++;
					Utils.sleep(100);
				}
			}
			bean.disconnectInstance();
			loginBean.disconnect();
		}
		
		return null;
	}
	
	public void resetStats(BotStats stats, BasicSense sense, Language language) {
		sense.conversations = 0;
		sense.engaged = 0;
		language.defaultResponses = 0;
		language.confidence = 0;
		language.sentiment = 0;
	}
	
	public void updateStats(BotStats stats, BasicSense sense, Language language, long startTime) {
		stats.conversations = stats.conversations + sense.conversations;
		stats.engaged = stats.engaged + sense.engaged;
		stats.defaultResponses = stats.defaultResponses + language.defaultResponses;
		stats.confidence = stats.confidence + language.confidence;
		stats.sentiment = stats.sentiment + language.sentiment;
		stats.messages++;
		stats.chatTotalResponseTime = stats.chatTotalResponseTime + (System.currentTimeMillis() - startTime);
		Stats.stats.botChatTotalResponseTime = Stats.stats.botChatTotalResponseTime + (System.currentTimeMillis() - startTime);
		Stats.stats.botMessages++;
	}
	
	public String processSpeakQQ(SpeechConfig config, boolean avatar, HttpServletRequest requestContext) {	
		AdminDatabase.instance().log(Level.INFO, "API speak QQ", config.text, config.application, requestContext.getRemoteAddr());
		try {
			LoginBean loginBean = new LoginBean();
			try {
				if(!avatar) {
					InstanceConfig instanceConfig = new InstanceConfig();
					instanceConfig.application = config.application;
					instanceConfig.id = config.instance;
					BotBean bean = (BotBean)instanceConfig.validate(loginBean, requestContext);
					bean.connect(ClientType.REST);
					VoiceBean voiceBean = loginBean.getBean(VoiceBean.class);
					return ChatBean.speakQQ(ChatBean.prepareSpeechText(config.text), config.voice, voiceBean.getNativeVoiceApiKey(), voiceBean.getNativeVoiceAppId());
				} else {
					config.connect(loginBean, requestContext);
					AvatarBean bean = loginBean.getBean(AvatarBean.class);
					bean.validateInstance(config.instance);
					return ChatBean.speakQQ(ChatBean.prepareSpeechText(config.text), config.voice, bean.getEmbedNativeVoiceApiKey(), bean.getEmbedNativeVoiceAppId());
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	public String processSpeakBing(SpeechConfig config, boolean avatar, HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API speak bing", config.text, config.application, requestContext.getRemoteAddr());
		try {
			LoginBean loginBean = new LoginBean();
			try {
				String apiKey;
				String apiEndpoint;
				VoiceBean voiceBean = null;
				AvatarBean avatarBean = null;
				if(!avatar) {
					InstanceConfig instanceConfig = new InstanceConfig();
					instanceConfig.application = config.application;
					instanceConfig.id = config.instance;
					instanceConfig.instance = config.instance;
					
					BotBean bean;
					try {
						instanceConfig.validateApplication(loginBean, null);
						instanceConfig.user = loginBean.getAppUser();
						loginBean.setUser(new User(instanceConfig.user, null));
						loginBean.setLoggedIn(true);
						bean = loginBean.getBotBean();
						
						if (config.application.equals(Long.toString(AdminDatabase.getTemporaryApplicationId())) || loginBean.getUser().isPartnerUser()) {
							bean.validateInstance(instanceConfig.instance, true);
						} else {
							bean.validateInstance(instanceConfig.instance, false);
						}
						
					} catch (Throwable exception) {
						error(exception);
						return null;
					}
					
					voiceBean = loginBean.getBean(VoiceBean.class);
					apiKey = voiceBean.getNativeVoiceApiKey();
					apiEndpoint = voiceBean.getVoiceApiEndpoint();
				} else {
					config.connect(loginBean, requestContext);
					avatarBean = loginBean.getBean(AvatarBean.class);
					avatarBean.validateInstance(config.instance);
					
					apiKey = avatarBean.getEmbedNativeVoiceApiKey();
					apiEndpoint = avatarBean.getEmbedVoiceApiEndpoint();
				}
				if ((apiKey == null || apiKey.isEmpty()) && config.application.equals(Long.toString(AdminDatabase.getTemporaryApplicationId()))) {
					apiKey = Site.MICROSOFT_SPEECH_KEY;
					apiEndpoint = Site.MICROSOFT_SPEECH_ENDPOINT;
				}
				String token = avatar ? avatarBean.getEmbedNativeVoiceToken() : voiceBean.getNativeVoiceApiToken();
				
				if (config.apiKey != null && !config.apiKey.isEmpty()) {
					apiKey = config.apiKey;
				}
				if (config.apiEndpoint != null && !config.apiEndpoint.isEmpty()) {
					apiEndpoint = config.apiEndpoint;
				}
				
				String result = null;
				if (token != null) {
					result = ChatBean.speakBing(ChatBean.prepareSpeechText(config.text), config.voice, apiKey, token, apiEndpoint);
				}
				if (token == null || result == null) {
					//Try to get new token, then retry request.
					token = BingSpeech.getToken(apiKey, apiEndpoint);
					if(!avatar) {
						voiceBean.setNativeVoiceApiToken(token);
					} else {
						avatarBean.setEmbedNativeVoiceToken(token);
					}
					if (token==null) {
						return null;
					} else {
						return ChatBean.speakBing(ChatBean.prepareSpeechText(config.text), config.voice, apiKey, token, apiEndpoint);
					}
				} else {
					return result;
				}
			} finally {
				loginBean.disconnect();
			}
		} catch (Throwable failed) {
			error(failed);
			return null;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/alexa/{application}/{instance}")
	public String alexa(
			@PathParam("application") String application,
			@PathParam("instance") String instance,
			String json,
			@Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API POST alexa", instance);
		
		Stats.stats.botAlexaAPI++;
		LoginBean loginBean = new LoginBean();
		BotBean bean = null;
		Bot bot = null;
		InstanceConfig config = new InstanceConfig();
		config.id = instance;
		config.application = application;
		try {
			config.validateApplication(loginBean, null);
			config.user = loginBean.getAppUser();
			loginBean.setUser(new User(config.user, null));
			loginBean.validateUser(config.user, null, 0, false, true);
			loginBean.setLoggedIn(true);
			bean = loginBean.getBotBean();
			bean.validateInstance(config.id);
		} catch (Throwable exception) {
			error(exception);
			return null;
		}
		try {
			bot = bean.connectInstance();
			BotStats stats = BotStats.getStats(bean.getInstanceId(), bean.getInstanceName());
			Alexa alexa = bot.awareness().getSense(Alexa.class);
			Language language = bot.mind().getThought(Language.class);
			resetStats(stats, alexa, language);
			stats.alexaMessagesProcessed++;
			Stats.stats.botAlexaMessagesProcessed++;
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json);
			String userId = null;
			String sessionId = null;
			String requestType = null;
			String intentName = null;
			if(root!=null) {
				//Validate incoming request
				String signatureCertChainUrl = requestContext.getHeader("SignatureCertChainUrl");
				String signature = requestContext.getHeader("Signature");
				
				alexa.validateRequest(json, signature, signatureCertChainUrl);
				
				//Get user and session id
				JSONObject session = root.optJSONObject("session");
				if(session!=null) {
					sessionId = session.optString("sessionId");
					
					JSONObject user = session.optJSONObject("user");
					if(user!=null) {
						userId = user.optString("userId");
					}
				}
				
				//Get request
				JSONObject request = root.optJSONObject("request");
				if(request!=null) {
					requestType = request.optString("type");
					
					if(requestType.equals("LaunchRequest")) {
						return alexa.getJSONResponse(alexa.getLaunchResponse(), false, null, false).toString();
					}
					else if(requestType.equals("SessionEndedRequest")) {
						return alexa.getJSONResponse(alexa.getStopResponse(), false, null, true).toString();
					}
					else if(requestType.equals("IntentRequest")) {
						JSONObject intent = request.optJSONObject("intent");
						if(intent!=null) {
							intentName = intent.optString("name");
							
							if(intentName.equals("AMAZON.HelpIntent")) {
								return alexa.getJSONResponse(alexa.getHelpResponse(), false, null, false).toString();
							}
							else if(intentName.equals("AMAZON.CancelIntent")) {
								return alexa.getJSONResponse(alexa.getCancelResponse(), false, null, true).toString();
							}
							else if(intentName.equals("AMAZON.StopIntent")) {
								return alexa.getJSONResponse(alexa.getStopResponse(), false, null, true).toString();
							} else if(intentName.equals("AMAZON.FallbackIntent")) {
								return alexa.getJSONResponse(alexa.getFallbackResponse(), false, null, false).toString();
							}
							
							JSONObject slots = intent.optJSONObject("slots");
							Iterator<String> i = slots.keys();
							String slotName = i.next();
							String slotValue = slots.optJSONObject(slotName).optString("value");
							
							long startTime = System.currentTimeMillis();
							
							String response = alexa.processMessage(userId, slotValue, sessionId);
							if (response == null || response.isEmpty()) {
								return null;
							}
							updateStats(stats, alexa, language, startTime);
							resetStats(stats, alexa, language);
							
							boolean shouldEndSession;
							boolean isQuestion = response.contains("? ") || response.endsWith("?") || response.toLowerCase().contains("<button>") || response.toLowerCase().contains("<select>");

							// Strip HTML tags from response.
							response = Utils.stripTags(response);
							
							if (alexa.getAutoExit()) {
								if (isQuestion) {
									shouldEndSession = false;
								} else {
									shouldEndSession = true;
								}
							} else {
								shouldEndSession = alexa.isStopPhrase(slotValue);
							}
							
							if (!shouldEndSession && !isQuestion && alexa.getFollowupPrompt() != null && !alexa.getFollowupPrompt().isEmpty()) {
								response = response.concat(" ").concat(alexa.getFollowupPrompt());
							}
							
							return alexa.getJSONResponse(response, true, slotName, shouldEndSession).toString();
						}
					}
				}
			} 
			return null;
		} catch (Exception exception) {
			error(exception);
		} finally {
			if (bot != null) {
				int count = 0;
				while ((count < 50) && !bot.memory().getActiveMemory().isEmpty()) {
					count++;
					Utils.sleep(100);
				}
			}
			bean.disconnectInstance();
			loginBean.disconnect();
		}
		return null;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/googleassistant/{application}/{instance}")
	public String googleAssistant(
			@PathParam("application") String application,
			@PathParam("instance") String instance,
			String json,
			@Context HttpServletRequest requestContext) {
		AdminDatabase.instance().log(Level.INFO, "API POST google assistant", instance);
		
		Stats.stats.botGoogleAssistantAPI++;
		LoginBean loginBean = new LoginBean();
		BotBean bean = null;
		Bot bot = null;
		InstanceConfig config = new InstanceConfig();
		config.id = instance;
		config.application = application;
		try {
			config.validateApplication(loginBean, null);
			config.user = loginBean.getAppUser();
			loginBean.setUser(new User(config.user, null));
			loginBean.validateUser(config.user, null, 0, false, true);
			loginBean.setLoggedIn(true);
			bean = loginBean.getBotBean();
			bean.validateInstance(config.id);
		} catch (Throwable exception) {
			error(exception);
			return null;
		}
		try {
			bot = bean.connectInstance();
			BotStats stats = BotStats.getStats(bean.getInstanceId(), bean.getInstanceName());
			GoogleAssistant googleAssistant = bot.awareness().getSense(GoogleAssistant.class);
			Language language = bot.mind().getThought(Language.class);
			resetStats(stats, googleAssistant, language);
			stats.googleAssistantMessagesProcessed++;
			Stats.stats.botGoogleAssistantMessagesProcessed++;
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json);
			String userId = null;
			String message = null;
			String conversationId = null;
			String intentString = null;
			if (root != null) {
				JSONObject intent = root.optJSONObject("originalDetectIntentRequest");
				if (intent != null) {
					JSONObject payload = intent.optJSONObject("payload");
					if (payload != null) {
						JSONObject user = payload.optJSONObject("user");
						if (user != null) {
							userId = user.optString("userId");
							if (userId == null) {
								userId = user.optString("idToken");
							}
						}
						JSONObject conversation = payload.optJSONObject("conversation");
						if (conversation != null) {
							conversationId = conversation.optString("conversationId");
						}
						JSONArray inputs = payload.optJSONArray("inputs");
						if (inputs != null) {
							JSONObject input = inputs.optJSONObject(0);
							if (input != null) {
								intentString = input.optString("intent");
								JSONArray rawInputs = input.optJSONArray("rawInputs");
								if (rawInputs != null && !intentString.equals("actions.intent.MAIN")) {
									message = rawInputs.getJSONObject(0).optString("query");
								}
							}
						}
					}
				}
				long startTime = System.currentTimeMillis();
				String response = googleAssistant.processMessage(userId, message, conversationId);
				if (response == null || response.isEmpty()) {
					return null;
				}
				updateStats(stats, googleAssistant, language, startTime);
				resetStats(stats, googleAssistant, language);
				JSONObject jsonResponse = googleAssistant.getJSONResponse(
						response,
						!intentString.equals("actions.intent.CANCEL") && !googleAssistant.isStopPhrase(message));
				return jsonResponse.toString();
			} 
			return null;
		} catch (Exception exception) {
			error(exception);
		} finally {
			if (bot != null) {
				int count = 0;
				while ((count < 50) && !bot.memory().getActiveMemory().isEmpty()) {
					count++;
					Utils.sleep(100);
				}
			}
			bean.disconnectInstance();
			loginBean.disconnect();
		}
		return null;
	}
}

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
package org.botlibre.web.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.LogListener;
import org.botlibre.ProfanityException;
import org.botlibre.avatar.ImageAvatar;
import org.botlibre.knowledge.BinaryData;
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.knowledge.Primitive;
import org.botlibre.thought.language.Language;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AccessMode;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.Avatar;
import org.botlibre.web.admin.AvatarImage;
import org.botlibre.web.admin.AvatarMedia;
import org.botlibre.web.admin.BotAttachment;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.admin.Media;
import org.botlibre.web.admin.User;
import org.botlibre.web.admin.User.UserType;
import org.botlibre.web.admin.WebMedium;
import org.botlibre.web.rest.ChatResponse;
import org.botlibre.web.rest.InstanceConfig;
import org.botlibre.web.service.BotManager;
import org.botlibre.web.service.BotStatListener;
import org.botlibre.web.service.BotStats;
import org.botlibre.web.service.ErrorStats;
import org.botlibre.web.service.IPStats;
import org.botlibre.web.service.Stats;

public class BotBean extends WebMediumBean<BotInstance> {
	public static int MAX_LOG = 100000; // 100k
	public static int MAX_UPLOAD_SIZE = Site.MAX_UPLOAD_SIZE;
	public static int DEFAULT_BUFFER_SIZE = 1024 * 4;
	
	/** Reference to Bot instance. **/
	protected Long botId;
	
	boolean isConnected;
	StringWriter log;
	boolean showLog;
	LogListener listener;
	String avatarBackground;
	String avatarFileName;
	String avatar2FileName;
	String avatar3FileName;
	String avatar4FileName;
	String avatar5FileName;
	String avatarFileType;
	String avatarTalkFileName;
	String avatarTalkFileType;
	String avatarActionFileName;
	String avatarActionFileType;
	String avatarActionAudioFileName;
	String avatarActionAudioFileType;
	String avatarAudioFileName;
	String avatarAudioFileType;
	String template = "";
	String status = "";

	boolean avatarHD;
	String avatarFormat;
	String avatar;
	
	public BotBean() {
	}
	
	public boolean getAvatarHD() {
		return avatarHD;
	}

	public void setAvatarHD(boolean avatarHD) {
		this.avatarHD = avatarHD;
	}

	public String getAvatarFormat() {
		return avatarFormat;
	}

	public void setAvatarFormat(String avatarFormat) {
		this.avatarFormat = avatarFormat;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	@Override
	public boolean allowSubdomain() {
		return true;
	}

	@Override
	public String getPostAction() {
		return "bot";
	}

	@Override
	public String getBrowseAction() {
		return "browse";
	}
	
	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public BotAttachment addAttachment(byte[] image, String name, String type, String user, String token) {
		try {
			checkInstance();
			BotAttachment attachment = new BotAttachment();
			attachment.setName(Utils.sanitize(name));
			attachment.setType(Utils.sanitize(type));
			attachment.checkAttachmentType();
			attachment.generateKey();
			Media media = new Media();
			media.setMedia(image);
			return AdminDatabase.instance().addBotAttachment(attachment, media, this.instance, user, token);
		} catch (Exception failed) {
			error(failed);
			return null;
		}
	}
	
	/**
	 * Download the attachment media.
	 */
	public boolean downloadAttachment(HttpServletResponse response, String id, String key) {
		BotAttachment attachment = null;
		Media media = null;
		try {
			if (id == null || id.isEmpty()) {
				throw new BotException("Invalid attachment id");
			}
			if (key == null || key.isEmpty()) {
				throw new BotException("Invalid attachment key");
			}
			long mediaId = Long.valueOf(id);
			long keyId = Long.valueOf(key);
			attachment = AdminDatabase.instance().findBotAttachment(mediaId);
			if (attachment.getKey() != keyId) {
				throw new BotException("Invalid attachment key");
			}
			media = AdminDatabase.instance().findMedia(mediaId);
			if (media == null) {
				throw new BotException("Missing media");
			}
		} catch (Exception exception) {
			try {
				response.setContentType("plain/text");
				response.getWriter().write(exception.getMessage());
			} catch (Exception ignore) {}
			error(exception);
			return false;
		}
		try {
			attachment.checkAttachmentType();
			response.setContentType(attachment.getType());
			response.setHeader("Content-disposition","inline; filename=" + encodeURI(attachment.getName()));
			//response.setHeader("Content-disposition","attachment; filename=" + encodeURI(attachment.getName()));
			ServletOutputStream stream = response.getOutputStream();
			stream.write(media.getMedia());
			//stream.flush();
		} catch (Exception exception) {
			error(exception);
			return false;
		}
		return true;
	}
	
	/**
	 * Create a new bot instance.
	 */
	public boolean createInstance(InstanceConfig config, boolean isTemplate, boolean isSchema, String ip) {
		try {
			checkLogin();
			config.sanitize();
			BotInstance newInstance = new BotInstance(config.name);
			setInstance(newInstance);
			updateFromConfig(newInstance, config);
			newInstance.setDomain(getDomain());
			newInstance.setTemplate(isTemplate);
			checkVerfied(config);
			if (config.forkAccessMode == null) {
				newInstance.setAllowForking(config.allowForking);
				if (config.allowForking) {
					newInstance.setForkAccessMode(AccessMode.Users);
				} else {
					newInstance.setForkAccessMode(AccessMode.Administrators);
				}
			}
			newInstance.setSchema(isSchema);
			setTemplate(config.template);
			if (newInstance.getAdCode() == null || (config.adCode != null && !newInstance.getAdCode().equals(config.adCode))) {
				newInstance.setAdCodeVerified(false);
			}
			setSubdomain(config.subdomain, newInstance);
			Stats.stats.checkMaxCreates();
			IPStats stat = null;
			if (ip != null && !ip.isEmpty()) {
				stat = IPStats.getStats(ip);
				stat.checkMaxCreates();
			}
			//AdminDatabase.instance().validateNewInstance(newInstance.getAlias(), config.description, config.tags, Site.ADULT, getDomain());
			BotInstance templateInstance = null;
			if (config.template != null && !config.template.trim().isEmpty()) {
				templateInstance = AdminDatabase.instance().validateTemplate(config.template, getDomain());
				if (!templateInstance.isTemplate() && !isSuperUser()) {
					templateInstance.checkForkAccess(getUser());
				}
			} else {
				templateInstance = AdminDatabase.instance().getDefaultTemplate(getDomain());
			}
			if (templateInstance != null) {
				newInstance.setParentId(templateInstance.getId());
				if (!isSchema) {
					if (templateInstance.isSchema()) {
						isSchema = true;
						newInstance.setSchema(isSchema);
					} else {
						Bot.forceShutdown(templateInstance.getDatabaseName());
					}
				}
				templateInstance.checkAccess(getUser());
				if (templateInstance.getAvatar() != null) {
					AvatarImage avatar = new AvatarImage();
					avatar.setImage(templateInstance.getAvatar().getImage());
					newInstance.setAvatar(avatar);
				}
				newInstance.setInstanceAvatar(templateInstance.getInstanceAvatar());
				newInstance.setVoice(templateInstance.getVoice());
				newInstance.setVoiceMod(templateInstance.getVoiceMod());
				newInstance.setNativeVoice(templateInstance.getNativeVoice());
				newInstance.setNativeVoiceProvider(templateInstance.getNativeVoiceProvider());
				newInstance.setNativeVoiceName(templateInstance.getNativeVoiceName());
				newInstance.setLanguage(templateInstance.getLanguage());
				newInstance.setSpeechRate(templateInstance.getSpeechRate());
				newInstance.setPitch(templateInstance.getPitch());
			}
			setInstance(AdminDatabase.instance().createInstance(newInstance, getUser(), config.categories, config.tags, this.loginBean));
			Stats.stats.botCreates++;
			Stats.lastChat = System.currentTimeMillis();
			if (stat != null) {
				stat.botCreates++;
			}
			if (!newInstance.isExternal()) {
				try {
					Bot bot = Bot.createInstance();
					// TODO use system cache instead
					if (templateInstance == null) {
						bot.memory().createMemory(newInstance.getDatabaseName(), isSchema);
					} else {
						bot.memory().createMemoryFromTemplate(newInstance.getDatabaseName(), isSchema, templateInstance.getDatabaseName(), templateInstance.isSchema());
					}
					bot.memory().switchMemory(newInstance.getDatabaseName(), isSchema);
					new Bootstrap().renameMemory(bot.memory(), config.name, (templateInstance != null && !templateInstance.isAdmin(getUser())));
					bot.shutdown();
				} catch (Exception failed) {
					try {
						AdminDatabase.instance().delete(this.instance);
					} catch (Exception ignore) {}
					throw failed;
				}
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		setTemplate("");
		return true;
	}
	
	/**
	 * Create a directory link.
	 */
	public boolean createLink(InstanceConfig config, String ip,
				String apiURL, String apiPost, String apiResponse, boolean apiServerSide, boolean apiJSON) {
		try {
			checkLogin();
			config.sanitize();
			BotInstance newInstance = new BotInstance(config.name);
			newInstance.setDomain(getDomain());
			newInstance.setDescription(config.description);
			newInstance.setDetails(config.details);
			newInstance.setDisclaimer(config.disclaimer);
			newInstance.setWebsite(config.website);
			newInstance.setAdult(Site.ADULT);
			newInstance.setExternal(true);
			newInstance.setSchema(true);
			newInstance.setApiURL(apiURL);
			newInstance.setApiPost(apiPost);
			newInstance.setApiResponse(apiResponse);
			newInstance.setApiServerSide(apiServerSide);
			newInstance.setApiJSON(apiJSON);
			newInstance.setPaphus(config.website.contains("paphuslivechat") || config.website.contains("botlibre.biz"));
			newInstance.setTagsString(config.tags);
			newInstance.setCategoriesString(config.categories);
			setInstance(newInstance);
			checkVerfied(config);
			if (config.name.equals("")) {
				throw new BotException("Invalid name");
			}
			Stats.stats.checkMaxCreates();
			IPStats stat = null;
			if (ip != null && !ip.isEmpty()) {
				stat = IPStats.getStats(ip);
				stat.checkMaxCreates();
			}
			//AdminDatabase.instance().validateNewInstance(newInstance.getAlias(), config.description, config.tags, Site.ADULT, getDomain());
			setInstance(AdminDatabase.instance().createInstance(newInstance, getUser(), config.categories, config.tags, this.loginBean));
			Stats.stats.botCreates++;
			if (stat != null) {
				stat.botCreates++;
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Create a new bot instance.
	 */
	public boolean updateInstance(InstanceConfig config, String newdomain, String memoryLimit,
				Boolean template, Boolean featured, Boolean adVerified,
				String apiURL, String apiPost, String apiResponse, Boolean apiServerSide, Boolean apiJSON) {
		try {
			checkLogin();
			checkInstance();
			checkAdminOrSuper();
			config.sanitize();
			BotInstance newInstance = (BotInstance)this.instance.clone();
			this.editInstance = newInstance;
			updateFromConfig(newInstance, config);
			if (config.creator != null && isSuperUser()) {
				User user = AdminDatabase.instance().validateUser(config.creator);
				newInstance.setCreator(user);
			}
			if (newdomain != null && !newdomain.equals(this.instance.getDomain().getAlias())) {
				Domain domain = AdminDatabase.instance().validateDomain(newdomain);
				newInstance.setDomain(domain);
			}
			if (apiURL != null) {
				newInstance.setApiURL(Utils.sanitize(apiURL));
			}
			if (apiPost != null) {
				newInstance.setApiPost(Utils.sanitize(apiPost));
			}
			if (apiResponse != null) {
				newInstance.setApiResponse(Utils.sanitize(apiResponse));
			}
			if (apiServerSide != null) {
				newInstance.setApiServerSide(apiServerSide);
			}
			if (apiJSON != null) {
				newInstance.setApiJSON(apiJSON);
			}
			if (isSuper() && template != null) {
				newInstance.setTemplate(template);
			}
			if (isSuper() && featured != null) {
				newInstance.setFeatured(featured);
			}
			if (config.forkAccessMode == null) {
				newInstance.setAllowForking(config.allowForking);
				if (config.allowForking) {
					newInstance.setForkAccessMode(AccessMode.Users);
				} else {
					newInstance.setForkAccessMode(AccessMode.Administrators);
				}
			}
			if (newInstance.getAdCode() == null || (config.adCode != null && !newInstance.getAdCode().equals(config.adCode))) {
				newInstance.setAdCodeVerified(false);
			}
			if (adVerified != null && isSuper()) {
				newInstance.setAdCodeVerified(adVerified);
			}
			if ((memoryLimit != null) && (!memoryLimit.isEmpty())) {
				int limit = Integer.valueOf(memoryLimit);
				if (newInstance.getMemoryLimit() != limit) {
					if (isSuper()) {
					} else if (limit <= Site.MEMORYLIMIT) {
						
					} else if (Site.COMMERCIAL) {
						if (this.instance.getCreator().getType() == UserType.Diamond
								|| this.instance.getCreator().getType() == UserType.Partner
								|| this.instance.getCreator().getType() == UserType.Platinum) {
							if (limit > Site.MEMORYLIMIT * 2) {
								throw new BotException("Max knowledge limit is " + Site.MEMORYLIMIT * 2);
							}
						} else {
							throw new BotException("Max knowledge limit is " + Site.MEMORYLIMIT);
						}
					} else if (this.instance.getCreator().getType() == UserType.Diamond
							|| this.instance.getCreator().getType() == UserType.Partner) {
						if (limit > 300000) {
							throw new BotException("Max knowledge limit for Diamond accounts is 300,000");
						}
					} else if (this.instance.getCreator().getType() == UserType.Platinum) {
						if (limit > 250000) {
							throw new BotException("Max knowledge limit for Platinum accounts is 250,000");
						}
					} else if (this.instance.getCreator().getType() == UserType.Gold) {
						if (limit > 200000) {
							throw new BotException("Max knowledge limit for Gold accounts is 200,000");
						}
					} else if (this.instance.getCreator().getType() == UserType.Bronze) {
						if (limit > 150000) {
							throw new BotException("Max knowledge limit for Bronze accounts is 150,000");
						}
					} else {
						if (limit > 100000) {
							throw new BotException("Max knowledge limit for Basic accounts is 100,000");
						}
					}
					if (limit < 10000) {
						throw new BotException("knowledge limit must be larger than 10,000");
					}
					newInstance.setMemoryLimit(limit);
				}
			}
			setSubdomain(config.subdomain, newInstance);
			setInstance(AdminDatabase.instance().updateInstance(newInstance, config.categories, config.tags));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Fork a bot instance.
	 */
	public void forkInstance() {
		try {
			checkLogin();
			checkInstance();
			BotInstance parent = getInstance();
			BotInstance newInstance = new BotInstance(parent.getName());
			newInstance.setDescription(parent.getDescription());
			newInstance.setDetails(parent.getDetails());
			newInstance.setDisclaimer(parent.getDisclaimer());
			newInstance.setTagsString(parent.getTagsString());
			newInstance.setCategoriesString(parent.getCategoriesString());
			newInstance.setLicense(parent.getLicense());
			newInstance.setContentRating(parent.getContentRating());
			setInstance(newInstance);
			setTemplate(parent.getAlias());
			setForking(true);
		} catch (Exception failed) {
			error(failed);
		}
	}
	
	/**
	 * Delete the bot and its database.
	 */
	public boolean deleteInstance(boolean confirm) {
		try {
			if (!confirm) {
				throw new BotException("Must check 'I'm sure'");
			}
			checkLogin();
			checkInstance();
			BotInstance botInstance = AdminDatabase.instance().validate(getType(), this.instance.getId(), getUser().getUserId());
			checkAdminOrSuper();
			disconnect();
			try {
				if (!this.instance.isExternal() && !this.instance.isArchived()) {
					BotManager.manager().forceShutdown(this.instance.getDatabaseName());
					Utils.sleep(1000);
					Bot.forceShutdown(this.instance.getDatabaseName());
					Bot bot = Bot.createInstance();
					try {
						bot.memory().destroyMemory(this.instance.getDatabaseName(), this.instance.isSchema());
					} finally {
						bot.shutdown();
					}
				}
			} finally {
				AdminDatabase.instance().delete(botInstance);
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
		
	}

	/**
	 * Initialize the bot's avatar for a chat session.
	 * This can be done before the bot connects and processes its first message for a quick response.
	 */
	public ChatResponse initAvatar() {
		ChatResponse response = new ChatResponse();
		response.message = "";
		response.avatarType = "image/jpeg";
		Avatar avatar = this.instance.getInstanceAvatar();
		if (avatar == null) {
			response.avatar = getAvatarImage(this.instance);
		} else {			
			response.avatar = getAvatarImage(this.instance);
			if (avatar.getBackground() != null) {
				String background = avatar.getBackground().getFileName();
				response.avatar = background;
				response.avatarBackground = background;
			}
			List<AvatarMedia> matching = null;
			matching = avatar.getMedia("", "", "", "");
			if (!matching.isEmpty()) {
				AvatarMedia media = randomMatch(matching);
				response.avatar = media.getFileName();
				if (matching.size() > 1) {
					AvatarMedia media2 = randomMatch(matching);
					AvatarMedia media3 = randomMatch(matching);
					AvatarMedia media4 = randomMatch(matching);
					AvatarMedia media5 = randomMatch(matching);
					if (media != media2 || media != media3 || media != media4 || media != media5) {
						response.avatar2 = media2.getFileName();
						response.avatar3 = media3.getFileName();
						response.avatar4 = media4.getFileName();
						response.avatar5 = media5.getFileName();
					}
				}
				response.avatarType = media.getType();
			}
			matching = avatar.getMedia("", "", "", "talking");
			if (!matching.isEmpty()) {
				AvatarMedia media = randomMatch(matching);
				response.avatarTalk = media.getFileName();
				response.avatarTalkType = media.getType();
			}
		}
		return response;
	}
	
	public void writeAdminButtonHTML(SessionProxyBean proxy, Writer out) {
		if (this.instance == null || (this.instance.isExternal() && !this.instance.hasAPI()) || !isAdmin()) {
			return;
		}
		try {
			out.write("<a href='");
			out.write(getPostAction());
			out.write("?admin");
			out.write(proxy.proxyString());
			out.write(instanceString());
			out.write("' title=\"Administer the ");
			out.write(getTypeName().toLowerCase());
			out.write("'s user access and configuration\"><img src='images/admin.svg' class='toolbar'/></a>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeAdminMenuItemHTML(SessionProxyBean proxy, Writer out) {
		if (this.instance == null || (this.instance.isExternal() && !this.instance.hasAPI()) || !isAdmin()) {
			return;
		}
		try {
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='");
			out.write(getPostAction());
			out.write("?admin");
			out.write(proxy.proxyString());
			out.write(instanceString());
			out.write("' title=\"Administer the ");
			out.write(Utils.camelCaseToLowerCase(getTypeName()));
			out.write("'s user access and configuration\"><img src='images/admin.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Admin Console"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	/**
	 * Return all bot instances.
	 */
	public List<BotInstance> getAllInstances(Domain domain) {
		try {
			List<BotInstance> results = AdminDatabase.instance().getAllInstances(
					this.page, this.pageSize, this.categoryFilter, this.nameFilter, this.userFilter, this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, getUser(), domain, false);
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (results.size() < this.pageSize) {
					this.resultsSize = results.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllInstancesCount(
							this.categoryFilter, this.nameFilter, this.userFilter, this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, getUser(), domain);
				}
			}
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<BotInstance>();
		}
	}
	
	/**
	 * Return all featured instances.
	 */
	public List<BotInstance> getAllFeaturedInstances() {
		try {
			return AdminDatabase.instance().getAllInstances(
						0, 100, "", "", "", InstanceFilter.Featured, InstanceRestrict.None, InstanceSort.MonthlyConnects, this.loginBean.contentRating, "", null, getDomain(), false);
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<BotInstance>();
		}
	}

	public List<AvatarImage> getSharedAvatarImages() {
		try {
			return AdminDatabase.instance().getSharedAvatarImages(getDomain());	
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<AvatarImage>();
		}
	}

	public List<BotInstance> getAllTemplates() {
		try {
			if (isLoggedIn()) {
				return AdminDatabase.instance().getAllTemplates(getUser(), this.loginBean.getContentRating(), getDomain());
			} else {
				return new ArrayList<BotInstance>();
			}
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<BotInstance>();
		}
	}
	
	/**
	 * Validate and set the instance.
	 */
	public boolean editInstance(long id) {
		try {
			checkLogin();
			setInstance(AdminDatabase.instance().validate(getType(), id, getUser().getUserId()));
			checkAdminOrSuper();
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Connect to the specified bot instance.
	 */
	public void connect(ClientType type) {
		connect(type, null);
	}
	
	/**
	 * Connect to the specified bot instance.
	 */
	public void connect(ClientType type, HttpServletRequest request) {
		try {
			if (this.botId != null) {
				if ((this.instance != null) && getBot().memory().getMemoryName().equals(this.instance.getDatabaseName())) {
					return;
				}
				try {
					this.loginBean.disconnectInstance();
				} catch (Exception ignore) {
					AdminDatabase.instance().log(ignore);
				}
				this.botId = null;
			}
			if (this.instance == null) {
				throw new BotException("You must first select a bot");
			}
			if (!validateInstance(this.instance.getId().toString())) {
				return;
			}
			if (this.instance.isExternal()) {
				throw new BotException("Cannot connect to external bot");
			}
			if (this.instance.isArchived()) {
				throw new BotException("Cannot connect to archived bot");
			}
			IPStats.botConnect(request);
			incrementConnects(type);
			connectInstance();
			checkOldConfig();
		} catch (Exception failed) {
			error(failed);
			return;
		}
	}

	public Bot connectInstance() {
		Stats.stats.botConnects++;
		Stats.lastChat = System.currentTimeMillis();
		BotStats stats = BotStats.getStats(getInstanceId(), getInstanceName());
		stats.connects++;
		Bot bot = Bot.createInstanceFromPool(this.instance.getDatabaseName(), this.instance.isSchema());
		bot.setStats(new BotStatListener(this.instance.getId(), this.instance.getName()));
		try {
			this.instance.initialize(bot);
			int size = bot.memory().getLongTermMemory().size();
			stats.size = size;
			if (!isAdmin() && (size > this.instance.getMemoryLimit() * 2)) {
				if ((size/1000) != (this.instance.getMemorySize()/1000)) {
					AdminDatabase.instance().updateInstanceSize(this.instance, size);
				}
				throw new BotException("Memory size exceeded, bot has been suspended until nightly forgetfullness task runs");
			}
			if (!isSuper() && (size > this.instance.getMemoryLimit() * 5)) {
				if ((size/1000) != (this.instance.getMemorySize()/1000)) {
					AdminDatabase.instance().updateInstanceSize(this.instance, size);
				}
				throw new BotException("Memory size exceeded, bot has been suspended until nightly forgetfullness task runs");
			}
			this.log = new StringWriter();
			if (this.listener == null) {
				this.listener = new LogListener() {
					int counter = 0;
					
					void checkMaxLog() {
						counter++;
						if ((counter > 100) && (log.toString().length() > MAX_LOG)) {
							log = new StringWriter();
							counter = 0;
						}
					}
					@Override
					public void log(Object source, String message, Level level, Object[] arguments) {
						if (log == null) {
							return;
						}
						if (message.contains(ProfanityException.MESSAGE)) {
							return;
						}
						checkMaxLog();
						StringWriter writer = new StringWriter();
						writer.append(Utils.printDate(Calendar.getInstance()) + " - " + level + " -- " + source + ":" + message);
						for (Object argument : arguments) {
							writer.append(" - " + argument);
						}
						writer.append("\n");
						String text = writer.toString();
						log.append(text);
						if (level.intValue() >= Level.WARNING.intValue()) {
							BotStats stats = BotStats.getStats(instance.getId(), instance.getName());
							stats.errors++;
							addError(text);
							ErrorStats.error(message);
						}
					}
	
					@Override
					public void log(Throwable error) {
						if (log == null) {
							return;
						}
						checkMaxLog();
						log.append(Utils.printDate(Calendar.getInstance()) + " - " + Level.SEVERE + " -- " + error.getMessage() + "\n");
						StringWriter writer = new StringWriter();
						PrintWriter printWriter = new PrintWriter(writer);
						error.printStackTrace(printWriter);
						printWriter.flush();
						writer.flush();
						log.append(writer.toString() + "\n");
					}
					
					@Override
					public void logLevelChange(Level level) { }
				};
			}
			bot.addLogListener(this.listener);
			if ((size/1000) != (this.instance.getMemorySize()/1000)) {
				AdminDatabase.instance().updateInstanceSize(this.instance, size);
			}
			setBot(bot);
			setConnected(true);
			return bot;
		} catch (Error exception) {
			bot.pool();
			throw exception;
		} catch (RuntimeException exception) {
			bot.pool();
			throw exception;
		}
	}

	public void checkOldConfig() {
		if (this.instance == null || this.instance.isExternal()) {
			return;
		}
		if (this.instance.getVoice() == null) {
			boolean disconnect = false;
			if (!isConnected()) {
				connect(ClientType.WEB);
				disconnect = true;
			}
			if (this.instance.getVoice() == null) {
				Language language = getBot().mind().getThought(Language.class);
				Map<Primitive, Object> properties = language.clearVoiceProperties();
				setInstance(AdminDatabase.instance().updateInstanceVoice(
						this.instance.getId(), (String)properties.get(Primitive.VOICE), "", (Boolean)properties.get(Primitive.NATIVEVOICE),
						null, (String)properties.get(Primitive.NATIVEVOICENAME), (String)properties.get(Primitive.LANGUAGE),
						(String)properties.get(Primitive.PITCH), (String) properties.get(Primitive.SPEECHRATE), (String) properties.get(Primitive.NATIVEVOICEAPIKEY), (String) properties.get(Primitive.NATIVEVOICEAPPID), (String) properties.get(Primitive.VOICEAPIENDPOINT)));
				if (getBot().avatar() instanceof ImageAvatar) {
					Long avatarId = ((ImageAvatar)getBot().avatar()).clearAvatarProperty();
					if (avatarId != null) {
						setInstance(AdminDatabase.instance().updateInstanceAvatar(this.instance.getId(), avatarId));
					}
				}
			}
			if (disconnect) {
				disconnect();
			}
		}
	}
	
	@Override
	public void setLoginBean(LoginBean loginBean, ServletContext context) {
		this.loginBean = loginBean;
		loginBean.setBotBean(this);
	}
	
	public BotBean clone() {
		try {
			BotBean clone = (BotBean)super.clone();
			clone.isConnected = false;
			clone.botId = null;
			clone.log = null;
			clone.listener = null;
			clone.avatarFileName = null;
			clone.avatarTalkFileName = null;
			clone.avatarActionFileName = null;
			clone.avatarActionAudioFileName = null;
			clone.avatarAudioFileName = null;
			clone.template = "";
			this.status = "";
			return clone;
		} catch (Exception exception) {
			throw new Error(exception);
		}
	}

	@Override
	public void disconnectInstance() {
		disconnect();
	}
	
	/**
	 * Pool Bot instance.
	 */
	public void poolInstance() {
		if (this.botId == null) {
			return;
		}
		Bot bot = BotManager.manager().removeInstance(this.botId);
		if (bot != null) {
			try {
				int size = bot.memory().getLongTermMemory().size();
				if ((size/1000) != (this.instance.getMemorySize()/1000)) {
					AdminDatabase.instance().updateInstanceSize(this.instance, size);
				}
				bot.removeLogListener(this.listener);
				bot.pool();
			} catch (Exception exception) {
				AdminDatabase.instance().log(exception);
			}
		}
		this.botId = null;
	}
	
	/**
	 * Disconnect from the Bot instance.
	 */
	public void disconnect() {
		if (!this.isConnected) {
			return;
		}
		this.isConnected = false;
		poolInstance();
		this.log = null;
		this.listener = null;
		this.avatarBackground = null;

		this.avatarFileName = null;
		this.avatarFileType = null;
		this.avatarTalkFileName = null;
		this.avatarTalkFileType = null;
		this.avatarActionFileName = null;
		this.avatarActionFileType = null;
		this.avatarActionAudioFileName = null;
		this.avatarActionAudioFileType = null;
		this.avatarAudioFileName = null;
		this.avatarAudioFileType = null;
		
		this.template = "";
		this.status = "";
	}

	/**
	 * Save the error message.
	 */
	public void addError(String error) {
		if (this.instance != null) {
			this.instance = AdminDatabase.instance().addError(this.instance, error);
		}
	}

	/**
	 * Return the current log.
	 */
	public String getLog() {
		if (this.log == null) {
			return "";
		}
		this.log.flush();
		return this.log.toString();
	}

	/**
	 * Return the log level.
	 */
	public String getLogLevel() {
		Bot bot = getBot();
		if (bot == null) {
			return Bot.LEVELS[0].getName();
		}
		return bot.getDebugLevel().getName();
	}

	/**
	 * Return the html option selected attribute if the level is selected.
	 */
	public String isLogLevelSelected(String level) {
		if (getLogLevel().equals(level)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

	/**
	 * Return the log level.
	 */
	public void setLogLevel(String level) {
		getBot().setDebugLevel(Level.parse(level));
	}

	public void clearLog() {
		this.log = new StringWriter();
	}

	public void clearWarnings() {
		if (this.instance != null) {
			this.instance = AdminDatabase.instance().clearErrors(this.instance);
		}
	}

	/**
	 * Return the associated Bot instance.
	 */
	public Long getBotId() {
		return this.botId;
	}

	/**
	 * Return the associated Bot instance.
	 */
	public Bot getBot() {
		if (this.botId == null && this.instance == null) {
			throw new BotException("HTTP session has expired.  Please reconnect.");
		}
		Bot bot = BotManager.manager().getInstance(this.botId);
		if (bot == null) {
			AdminDatabase.instance().log(Level.INFO, "Instance lost, reconnecting", this, this.botId);
			bot = connectInstance();
			for (ServletBean bean : this.loginBean.getBeans().values()) {
				bean.reInitialize(bot);
			}
		}
		return bot;
	}

	/**
	 * Return the size of the instance database.
	 */
	public int getInstanceSize() {
		return getBot().memory().getLongTermMemory().size();
	}

	/**
	 * Set the associated Bot instance.
	 */
	public void setBot(Bot bot) {
		this.botId = BotManager.manager().addInstance(bot);
	}

	public boolean isConnected() {
		return isConnected;
	}
	
	public String getNotConnectedMessage() {
		return "<p style=\"color:#E00000;\">" + this.loginBean.translate("No bot has been selected, you need to first select your bot from")
					+ " <a href=\"browse.jsp\">" + this.loginBean.translate("browse") + "</a></p>";
	}
	
	public String getMustBeAdminMessage() {
		return "<p style=\"color:#E00000;\">" + this.loginBean.translate("Must be admin") + "</p>";
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public AvatarMedia randomMatch(List<AvatarMedia> matches) {
		List<AvatarMedia> formatMatches = new ArrayList<AvatarMedia>();
		String format = "mp4";
		if (this.avatarFormat != null && !this.avatarFormat.isEmpty()) {
			format = this.avatarFormat;
		}
		for (AvatarMedia media : matches) {
			if (media.getType().indexOf(format) != -1) {
				formatMatches.add(media);
			}
		}
		if (formatMatches.isEmpty()) {
			List<AvatarMedia> hdMatches = new ArrayList<AvatarMedia>();
			for (AvatarMedia media : matches) {
				if (this.avatarHD && media.getHD()) {
					hdMatches.add(media);
				} else if (!this.avatarHD && !media.getHD()) {
					hdMatches.add(media);
				}
			}
			if (!hdMatches.isEmpty()) {
				return Utils.random(hdMatches);
			}
			return Utils.random(matches);
		} else {
			List<AvatarMedia> hdMatches = new ArrayList<AvatarMedia>();
			for (AvatarMedia media : formatMatches) {
				if (this.avatarHD && media.getHD()) {
					hdMatches.add(media);
				} else if (!this.avatarHD && !media.getHD()) {
					hdMatches.add(media);
				}
			}
			if (!hdMatches.isEmpty()) {
				return Utils.random(hdMatches);
			}
			return Utils.random(formatMatches);
		}
	}
	
	public void outputAvatar() {
		Bot bot = getBot();
		if (bot == null) {
			return;
		}
		this.avatarFileType = "image/jpeg";
		this.avatarTalkFileName = null;
		this.avatarTalkFileType = null;
		this.avatarActionFileName = null;
		this.avatarActionFileType = null;
		this.avatarActionAudioFileName = null;
		this.avatarActionAudioFileType = null;
		this.avatarAudioFileName = null;
		this.avatarAudioFileType = null;
		String emotion = bot.mood().currentEmotionalState().name();
		String action = bot.avatar().getAction();
		String pose = bot.avatar().getPose();
		String status = "";
		if (!emotion.equals("NONE")) {
			status = status + emotion.toLowerCase();
		}
		if (action != null) {
			if (!status.isEmpty()) {
				status = status + " : ";
			}
			status = status + action;
		}
		if (pose != null) {
			if (!status.isEmpty()) {
				status = status + " : ";
			}
			status = status + pose;
		}
		this.status = status;
		Avatar avatar = this.instance.getInstanceAvatar();
		if (this.avatar != null && !this.avatar.isEmpty()) {
			AvatarBean bean = this.loginBean.getBean(AvatarBean.class);
			if (!String.valueOf(bean.getInstanceId()).equals(this.avatar)) {
				bean.validateInstance(this.avatar);
			}
			avatar = bean.getInstance();
			if (avatar == null) {
				avatar = this.instance.getInstanceAvatar();
			}
		}
		if (avatar == null) {
			BinaryData image = bot.avatar().getCurrentImage();
			this.avatarFileName = outputAvatar(image);
			if (this.avatarFileName == null) {
				this.avatarFileName = getAvatarImage(this.instance);
			}
		} else {
			this.avatarFileName = getAvatarImage(this.instance);
			if (avatar.getBackground() != null) {
				String background = avatar.getBackground().getFileName();
				this.avatarFileName = background;
				this.avatarBackground = background;
			}
			List<AvatarMedia> matching = null;

			// Audio
			if (action != null) {
				matching = avatar.getAudio(action, pose);
				if (!matching.isEmpty()) {
					AvatarMedia media = Utils.random(matching);
					this.avatarActionAudioFileName = media.getFileName();
					this.avatarActionAudioFileType = media.getType();
				}
			}
			matching = avatar.getAudio("", pose);
			if (!matching.isEmpty()) {
				AvatarMedia media = Utils.random(matching);
				this.avatarAudioFileName = media.getFileName();
				this.avatarAudioFileType = media.getType();
			}
			// Image/video
			if (action != null) {
				matching = avatar.getMedia(emotion, action, pose, "");
				if (!matching.isEmpty()) {
					AvatarMedia media = randomMatch(matching);
					this.avatarActionFileName = media.getFileName();
					this.avatarActionFileType = media.getType();
					if (media.isImage()) {
						this.avatarFileName = media.getFileName();
						this.avatarFileType = media.getType();
						return;					
					}
				}
			}
			matching = avatar.getMedia(emotion, "", pose, "");
			if (!matching.isEmpty()) {
				AvatarMedia media = randomMatch(matching);
				this.avatarFileName = media.getFileName();
				this.avatar2FileName = null;
				this.avatar3FileName = null;
				this.avatar4FileName = null;
				this.avatar5FileName = null;
				if (matching.size() > 1) {
					AvatarMedia media2 = randomMatch(matching);
					AvatarMedia media3 = randomMatch(matching);
					AvatarMedia media4 = randomMatch(matching);
					AvatarMedia media5 = randomMatch(matching);
					if (media != media2 || media != media3 || media != media4 || media != media5) {
						this.avatar2FileName = media2.getFileName();
						this.avatar3FileName = media3.getFileName();
						this.avatar4FileName = media4.getFileName();
						this.avatar5FileName = media5.getFileName();
					}
				}
				this.avatarFileType = media.getType();
			}
			matching = avatar.getMedia(emotion, "", pose, "talking");
			if (!matching.isEmpty()) {
				AvatarMedia media = randomMatch(matching);
				this.avatarTalkFileName = media.getFileName();
				this.avatarTalkFileType = media.getType();
			}
		}
	}
	
	public String getAvatarBackground() {
		return avatarBackground;
	}

	public void setAvatarBackground(String avatarBackground) {
		this.avatarBackground = avatarBackground;
	}

	public boolean hasTalkAvatar() {
		return this.avatarTalkFileName != null && this.avatarTalkFileType.contains("video");
	}

	public boolean hasActionVideo() {
		return this.avatarActionFileName != null && this.avatarActionFileType.contains("video");
	}
	
	public boolean isVideoAvatar() {
		return this.avatarFileName != null && this.avatarFileType.contains("video");		
	}

	public String outputAvatar(BinaryData image) {
		if (image != null) {
			try {
				String fileName = this.instance.getDatabaseName() + "-" + image.getId() + "-image.jpg";
				image.outputToFile(LoginBean.outputFilePath + "/avatars/" + fileName, false);
				return "avatars/" + fileName;
			} catch (IOException exception) {
				getBot().log(this, exception);
			}
		}
		return null;
	}

	public String getAvatarImage(WebMedium content) {
		if (content instanceof BotInstance) {
			BotInstance instance = (BotInstance)content;
			if (instance.getAvatar() == null && instance.getInstanceAvatar() != null && !instance.getInstanceAvatar().isFlagged()) {
				return getAvatarImage(instance.getInstanceAvatar().getAvatar());
			}
		}
		return super.getAvatarImage(content);
	}

	public String getAvatarThumb(WebMedium content) {
		if (content instanceof BotInstance) {
			BotInstance instance = (BotInstance)content;
			if (instance.getAvatar() == null && instance.getInstanceAvatar() != null && !instance.getInstanceAvatar().isFlagged()) {
				return getAvatarThumb(instance.getInstanceAvatar().getAvatar());
			}
		}
		return super.getAvatarThumb(content);
	}
	
	/**
	 * Process the image file.
	 */
	public static byte[] loadImageFile(InputStream stream) {
		return loadImageFile(stream, true, MAX_UPLOAD_SIZE);
	}
	
	/**
	 * Process the image file.
	 */
	public static byte[] loadImageFile(InputStream stream, boolean close) {
		return loadImageFile(stream, close, MAX_UPLOAD_SIZE);
	}
	
	/**
	 * Process the image file.
	 */
	public static byte[] loadImageFile(InputStream stream, boolean close, int max) {
		long count = 0;
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			int n;
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			while ((n = stream.read(buffer, 0, buffer.length)) != -1) {
				output.write(buffer,0,n);
				if(count > max){
					throw new BotException("File size limit exceeded: "+ max);
				}
				count+=n;
			}
			return output.toByteArray();
		} catch (BotException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new BotException(exception);
		} finally {
			if (close && stream != null) {
				try {
					stream.close();
				} catch (IOException ignore) {}
			}
		}
	}

	@Override
	public String getEmbeddedBanner() {
		return "instance-banner.jsp";
	}

	public String getAvatarFileName() {
		return avatarFileName;
	}
	
	public String getAvatarFileType() {
		return avatarFileType;
	}

	public String getAvatar2FileName() {
		return avatar2FileName;
	}

	public void setAvatar2FileName(String avatar2FileName) {
		this.avatar2FileName = avatar2FileName;
	}

	public String getAvatar3FileName() {
		return avatar3FileName;
	}

	public void setAvatar3FileName(String avatar3FileName) {
		this.avatar3FileName = avatar3FileName;
	}

	public String getAvatar4FileName() {
		return avatar4FileName;
	}

	public void setAvatar4FileName(String avatar4FileName) {
		this.avatar4FileName = avatar4FileName;
	}

	public String getAvatar5FileName() {
		return avatar5FileName;
	}

	public void setAvatar5FileName(String avatar5FileName) {
		this.avatar5FileName = avatar5FileName;
	}

	public String getAvatarTalkFileName() {
		return avatarTalkFileName;
	}
	
	public String getAvatarTalkFileType() {
		return avatarTalkFileType;
	}

	public void setAvatarFileName(String avatarFileName) {
		this.avatarFileName = avatarFileName;
	}

	public String getAvatarActionFileName() {
		return avatarActionFileName;
	}

	public void setAvatarActionFileName(String avatarActionFileName) {
		this.avatarActionFileName = avatarActionFileName;
	}

	public String getAvatarActionFileType() {
		return avatarActionFileType;
	}

	public void setAvatarActionFileType(String avatarActionFileType) {
		this.avatarActionFileType = avatarActionFileType;
	}

	public String getAvatarActionAudioFileName() {
		return avatarActionAudioFileName;
	}

	public void setAvatarActionAudioFileName(String avatarActionAudioFileName) {
		this.avatarActionAudioFileName = avatarActionAudioFileName;
	}

	public String getAvatarActionAudioFileType() {
		return avatarActionAudioFileType;
	}

	public void setAvatarActionAudioFileType(String avatarActionAudioFileType) {
		this.avatarActionAudioFileType = avatarActionAudioFileType;
	}

	public String getAvatarAudioFileName() {
		return avatarAudioFileName;
	}

	public void setAvatarAudioFileName(String avatarAudioFileName) {
		this.avatarAudioFileName = avatarAudioFileName;
	}

	public String getAvatarAudioFileType() {
		return avatarAudioFileType;
	}

	public void setAvatarAudioFileType(String avatarAudioFileType) {
		this.avatarAudioFileType = avatarAudioFileType;
	}
	
	@Override
	public Class<BotInstance> getType() {
		return BotInstance.class;
	}
	
	@Override
	public String getTypeName() {
		return "Bot";
	}
	
	@Override
	public void writeBrowseStats(StringWriter writer, BotInstance instance) {
		if (!Site.COMMERCIAL && !Site.DEDICATED && (!instance.isExternal() || instance.hasAPI())) {
			writer.write("Chat Bot Wars: rank " + instance.getRank() + ", wins " + instance.getWins() + ", losses " + instance.getLosses() + "<br/>\n");
		}
		writer.write("Knowledge: " + instance.getMemorySize() + " objects<br/>\n");
	}
	
	@Override
	public void writeSearchOptions(StringWriter writer) {
		writer.write("<option value='Size' " + getInstanceSortCheckedString(InstanceSort.Size) + ">size</option>\n");
		writer.write("<option value='Rank' " + getInstanceSortCheckedString(InstanceSort.Rank) + ">rank</option>\n");
		writer.write("<option value='Wins' " + getInstanceSortCheckedString(InstanceSort.Wins) + ">wins</option>\n");
		writer.write("<option value='Losses' " + getInstanceSortCheckedString(InstanceSort.Losses) + ">losses</option>\n");
	}
	
	@Override
	public void writeRestrictOptions(StringWriter writer) {
		writer.write("<option value='Forkable' " + getInstanceRestrictCheckedString(InstanceRestrict.Forkable) + ">forkable</option>\n");
		if (isSuper()) {
			writer.write("<option value='Twitter' " + getInstanceRestrictCheckedString(InstanceRestrict.Twitter) + ">Twitter</option>\n");
			writer.write("<option value='Facebook' " + getInstanceRestrictCheckedString(InstanceRestrict.Facebook) + ">Facebook</option>\n");
			writer.write("<option value='Skype' " + getInstanceRestrictCheckedString(InstanceRestrict.Skype) + ">Skype</option>\n");
			writer.write("<option value='WeChat' " + getInstanceRestrictCheckedString(InstanceRestrict.WeChat) + ">WeChat</option>\n");
			writer.write("<option value='Kik' " + getInstanceRestrictCheckedString(InstanceRestrict.Kik) + ">Kik</option>\n");
			writer.write("<option value='Telegram' " + getInstanceRestrictCheckedString(InstanceRestrict.Telegram) + ">Telegram</option>\n");
			writer.write("<option value='Slack' " + getInstanceRestrictCheckedString(InstanceRestrict.Slack) + ">Slack</option>\n");
			writer.write("<option value='Email' " + getInstanceRestrictCheckedString(InstanceRestrict.Email) + ">email</option>\n");
			writer.write("<option value='Timer' " + getInstanceRestrictCheckedString(InstanceRestrict.Timer) + ">timer</option>\n");
			writer.write("<option value='Schema' " + getInstanceRestrictCheckedString(InstanceRestrict.Schema) + ">schema</option>\n");
			writer.write("<option value='Database' " + getInstanceRestrictCheckedString(InstanceRestrict.Database) + ">database</option>\n");
			writer.write("<option value='Archived' " + getInstanceRestrictCheckedString(InstanceRestrict.Archived) + ">archived</option>\n");
		}
	}

	@Override
	public void writeToolbarPostExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (Site.COMMERCIAL || Site.DEDICATED || this.instance == null || embed
					|| (this.instance.isExternal() && !this.instance.hasAPI()) || this.instance.isArchived()) {
			return;
		}
		try {
			out.write("<a href='browse?browse-type=ChatWar&bot1=");
			out.write(String.valueOf(getDisplayInstance().getId()));
			out.write("' class='button' title='Start a chat bot war'><img src='images/war.png' class='toolbutton'/>");
			out.write(String.valueOf(getDisplayInstance().getRank()));
			out.write("</a>");
		} catch (Exception exception) {
			error(exception);
		}
	}

	@Override
	public void writeMenuPostExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (Site.COMMERCIAL || this.instance == null || embed
					|| (this.instance.isExternal() && !this.instance.hasAPI()) || this.instance.isArchived()) {
			return;
		}
		try {
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='browse?browse-type=ChatWar&bot1=");
			out.write(String.valueOf(getDisplayInstance().getId()));
			out.write("' class='button' title='Start a chat bot war'><img src='images/war.png' class='menu'/> ");
			out.write(this.loginBean.translate("Chat Bot Wars"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
			
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='login?send-message&user=@" + getInstance().getAlias() + instanceString() + "' title='");
			out.write(this.loginBean.translate("Send bot a message"));
			out.write("'><img src='images/round_message.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Send Message"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
			
			if (!Site.COMMERCIAL && loginBean.isLoggedIn()) {
				out.write("<tr class='menuitem'>\n");
				out.write("<td><a class='menuitem' href='login?add-new-friend&friend=@" + getInstance().getAlias() + instanceString() + this.loginBean.postTokenString() + "' title='");
				out.write(this.loginBean.translate("Add bot as friend"));
				out.write("'><img src='images/round_avatar.svg' class='menu'/> ");
				out.write(this.loginBean.translate("Add Friend"));
				out.write("</a></td>\n");
				out.write("</tr>\n");
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	
	public void writeMessageButtonHTML(Writer out) {
		if (getInstance() == null) {
			return;
		}
		try {
			out.write("<a href='login?send-message&user=@" + getInstance().getAlias() + "' title='");
			out.write(this.loginBean.translate("Send bot a message"));
			out.write("'><img src='images/round_message.svg' class='toolbar'/>");
			out.write("</a>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeAddFriendButtonHTML(Writer out) {
		if (getInstance() == null || Site.COMMERCIAL || !this.loginBean.isLoggedIn()) {
			return;
		}
		try {
			out.write("<a href='login?add-new-friend&friend=@" + getInstance().getAlias() + instanceString() + this.loginBean.postTokenString() + "' title='");
			out.write(this.loginBean.translate("Add bot as friend"));
			out.write("'><img src='images/round_avatar.svg' class='toolbar'/>");
			out.write("</a>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	@Override
	public void writeStatsTabExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			if (!Site.COMMERCIAL && !Site.DEDICATED && (!getDisplayInstance().isExternal() || getDisplayInstance().hasAPI())) {
				out.write("Chat Bot Wars: wins: ");
				out.write(String.valueOf(getDisplayInstance().getWins()));
				out.write(", losses: ");
				out.write(String.valueOf(getDisplayInstance().getLosses()));
				out.write(", rank: ");
				out.write(String.valueOf(getDisplayInstance().getRank()));
				out.write("<br/>\n");
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	@Override
	public String getCreateURL() {
		return "create-instance.jsp";
	}

	@Override
	public String getSearchURL() {
		return "instance-search.jsp";
	}

	@Override
	public String getBrowseURL() {
		return "browse.jsp";
	}
	
}

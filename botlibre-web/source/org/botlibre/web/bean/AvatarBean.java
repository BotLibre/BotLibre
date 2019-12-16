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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.botlibre.BotException;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AccessMode;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.Avatar;
import org.botlibre.web.admin.AvatarImage;
import org.botlibre.web.admin.AvatarMedia;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.admin.ContentRating;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.admin.Media;
import org.botlibre.web.admin.MediaFile;
import org.botlibre.web.admin.User;
import org.botlibre.web.rest.AvatarConfig;
import org.botlibre.web.rest.AvatarMediaConfig;
import org.botlibre.web.rest.AvatarMessage;
import org.botlibre.web.rest.ChatResponse;

public class AvatarBean extends WebMediumBean<Avatar> {

	boolean selectAll;

	protected String userName = "";
	protected String token = "";
	protected String password = "";
	
	protected String embedCode = "";
	protected String embedDisplayCode = "";
	protected String embedSpeech = "Welcome to my website";
	protected String embedWidth = "";
	protected String embedHeight = "";
	protected String embedBackground = "";
	protected String embedEmotion = "";
	protected String embedAction = "";
	protected String embedPose = "";
	protected String embedVoice = "";
	protected String embedVoiceMod = "";
	protected boolean embedNativeVoice = false;
	protected boolean embedResponsiveVoice = false;
	protected boolean embedBingSpeech = false;
	protected boolean embedQQSpeech = false;
	protected String embedNativeVoiceName = "";
	protected String embedNativeVoiceAppId = "";
	protected String embedNativeVoiceApiKey = "";
	protected String embedVoiceApiEndpoint = "";
	protected String embedNativeVoiceToken = "";
	protected String embedLang = "";

	boolean avatarHD;
	String avatarFormat;
	String avatar;
	
	public AvatarBean() {
	}

	/**
	 * Record the API access.
	 */
	public boolean apiConnect() {
		incrementConnects(ClientType.REST);
		return true;
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

	public boolean getEmbedResponsiveVoice() {
		return embedResponsiveVoice;
	}

	public void setEmbedResponsiveVoice(boolean embedResponsiveVoice) {
		this.embedResponsiveVoice = embedResponsiveVoice;
	}

	@Override
	public void disconnect() {
		super.disconnect();
		reset();
	}

	public void reset() {
		this.selectAll = false;
		this.embedCode = "";
		this.embedDisplayCode = "";
		this.embedSpeech = "Welcome to my website";
		this.embedEmotion = "";
		this.embedAction = "";
		this.embedPose = "";
		this.userName = "";
		this.token = "";
		this.password = "";
		this.embedNativeVoice = false;
		this.embedResponsiveVoice = false;
		this.embedBingSpeech = false;
		this.embedQQSpeech = false;
		this.embedNativeVoiceName = "";
		this.embedLang = "";
	}

	@Override
	public String getEmbeddedBanner() {
		return "avatar-banner.jsp";
	}	

	@Override
	public String getPostAction() {
		return "avatar";
	}
	
	public List<Avatar> getAllInstances(Domain domain) {
		try {
			List<Avatar> results = AdminDatabase.instance().getAllAvatars(this.page, this.pageSize, this.categoryFilter, this.nameFilter, 
					this.userFilter, this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, getUser(), domain, false);
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (results.size() < this.pageSize) {
					this.resultsSize = results.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllAvatarsCount(this.categoryFilter, this.nameFilter, this.userFilter, 
							this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, getUser(), domain, false);
				}
			}
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Avatar>();
		}
	}
	
	public List<Avatar> getAllLinkableInstances() {
		try {
			List<Avatar> privateAvatars = AdminDatabase.instance().getAllAvatars(0, 50, "", "", null, InstanceFilter.Personal, 
					InstanceRestrict.None, InstanceSort.MonthlyConnects, ContentRating.Adult, "", getUser(), getDomain(), true);
			List<Avatar> publicAvatars = AdminDatabase.instance().getAllAvatars(
					0, 50, "", "", null, InstanceFilter.Public, InstanceRestrict.None, InstanceSort.MonthlyConnects, this.loginBean.contentRating, "", getUser(), getDomain(), true);
			List<Avatar> results = new ArrayList<Avatar>(privateAvatars);
			results.addAll(publicAvatars);
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Avatar>();
		}
	}

	public List<Avatar> getAllFeaturedInstances() {
		try {
			return AdminDatabase.instance().getAllAvatars(
					0, 100, "", "", "", InstanceFilter.Featured, InstanceRestrict.None, InstanceSort.MonthlyConnects, this.loginBean.contentRating, "", null, getDomain(), false);
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Avatar>();
		}
	}
	
	/**
	 * Download the avatar's media as a zip file.
	 */
	public boolean export(HttpServletResponse response) {
		try {
			checkInstance();
			checkAdmin();
			response.setContentType("application/zip");
			response.setHeader("Content-disposition","attachment; filename=" + encodeURI(this.instance.getName() + ".zip"));
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ZipOutputStream zip = new ZipOutputStream(stream);
			
			ZipEntry entry = new ZipEntry("meta.xml");
			zip.putNextEntry(entry);
			AvatarConfig config = this.instance.buildConfig();
			JAXBContext context = JAXBContext.newInstance(AvatarConfig.class);
			StringWriter writer = new StringWriter();
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(config, writer);
			zip.write(writer.toString().getBytes("UTF-8"));
			zip.closeEntry();

			if (this.instance.getAvatar() != null) {
				entry = new ZipEntry("icon.jpg");
				zip.putNextEntry(entry);
				zip.write(this.instance.getAvatar().getImage());
				zip.closeEntry();
			}
			
			if (this.instance.getBackground() != null) {
				entry = new ZipEntry("background.jpg");
				zip.putNextEntry(entry);
				Media data = AdminDatabase.instance().findMedia(this.instance.getBackground().getMediaId());
				zip.write(data.getMedia());
				zip.closeEntry();
			}
			
			for (AvatarMedia media : this.instance.getMedia()) {
				String ext = "mp4";
				int index = media.getFileName().indexOf('.');
				if (index != -1) {
					ext = media.getFileName().substring(index + 1, media.getFileName().length());
				}
				entry = new ZipEntry(String.valueOf(media.getMediaId()) + "." + ext);
				zip.putNextEntry(entry);
				Media data = AdminDatabase.instance().findMedia(media.getMediaId());
				zip.write(data.getMedia());
				zip.closeEntry();

				entry = new ZipEntry(String.valueOf(media.getMediaId()) + ".xml");
				zip.putNextEntry(entry);
				AvatarMediaConfig mediaConfig = media.toConfig();
				context = JAXBContext.newInstance(AvatarMediaConfig.class);
				writer = new StringWriter();
				marshaller = context.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshaller.marshal(mediaConfig, writer);
				zip.write(writer.toString().getBytes("UTF-8"));
				zip.closeEntry();
			}
			
			OutputStream out = response.getOutputStream();
			zip.flush();
			byte[] bytes = stream.toByteArray();
			out.write(bytes, 0, bytes.length);
			out.flush();
			
		} catch (Exception exception) {
			error(exception);
			return false;
		}
		return true;
	}
	
	/**
	 * Copy instance.
	 */
	public void copyInstance() {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			Avatar parent = getInstance();
			Avatar newInstance = new Avatar(parent.getName());
			newInstance.setDescription(parent.getDescription());
			newInstance.setDetails(parent.getDetails());
			newInstance.setDisclaimer(parent.getDisclaimer());
			newInstance.setTagsString(parent.getTagsString());
			newInstance.setCategoriesString(parent.getCategoriesString());
			newInstance.setLicense(parent.getLicense());
			newInstance.setContentRating(parent.getContentRating());
			newInstance.setNativeVoiceApiKey(parent.getNativeVoiceApiKey());
			newInstance.setNativeVoiceAppId(parent.getNativeVoiceAppId());
			setInstance(newInstance);
			setForking(true);
		} catch (Exception failed) {
			error(failed);
		}
	}
	
	/**
	 * Import the avatar from a zip file.
	 */
	public boolean importAvatar(byte[] bytes) {
		try {
			checkLogin();
			
			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			ZipInputStream zip = new ZipInputStream(stream);
			
			ZipEntry entry = zip.getNextEntry();
			byte[] icon = null;
			byte[] background = null;
			boolean found = false;
			Map<String, byte[]> media = new HashMap<String, byte[]>();
			Map<String, AvatarMediaConfig> mediaConfig = new HashMap<String, AvatarMediaConfig>();
			while (entry != null) {
				byte[] fileBytes = BotBean.loadImageFile(zip, false);
				if (entry.getName().equals("meta.xml")) {
					JAXBContext context = JAXBContext.newInstance(AvatarConfig.class);
					Unmarshaller marshaller = context.createUnmarshaller();
					ByteArrayInputStream fileStream = new ByteArrayInputStream(fileBytes);
					AvatarConfig config = (AvatarConfig)marshaller.unmarshal(fileStream);
					if (!createInstance(config)) {
						return false;
					}
					found = true;
				} else if (entry.getName().equals("icon.jpg")) {
					icon = fileBytes;
				} else if (entry.getName().equals("background.jpg")) {
					background = fileBytes;
				} else if (entry.getName().indexOf(".xml") != -1) {
					JAXBContext context = JAXBContext.newInstance(AvatarMediaConfig.class);
					Unmarshaller marshaller = context.createUnmarshaller();
					ByteArrayInputStream fileStream = new ByteArrayInputStream(fileBytes);
					AvatarMediaConfig config = (AvatarMediaConfig)marshaller.unmarshal(fileStream);
					int index = entry.getName().lastIndexOf('.');
					String id = entry.getName();
					if (index != -1) {
						id = id.substring(0, index);
					}
					mediaConfig.put(id, config);
				} else {
					int index = entry.getName().lastIndexOf('.');
					String id = entry.getName();
					if (index != -1) {
						id = id.substring(0, index);
					}
					media.put(id,  fileBytes);
				}
				zip.closeEntry();
				entry = zip.getNextEntry();
			}
			zip.close();
			stream.close();
			
			if (!found) {
				throw new BotException("Missing avatar meta.xml file in export archive");
			}
			if (icon != null) {
				update(icon);
			}
			if (background != null) {
				saveAvatarBackground(background, "background", "image");
			}
			for (Entry<String, AvatarMediaConfig> mediaEntry : mediaConfig.entrySet()) {
				byte[] data = media.get(mediaEntry.getKey());
				if (data != null) {
					AvatarMedia avatarMedia = addAvatarMedia(data, mediaEntry.getValue().name, mediaEntry.getValue().type);
					if (avatarMedia == null) {
						return false;
					}
					mediaEntry.getValue().mediaId = String.valueOf(avatarMedia.getMediaId());
					saveMedia(mediaEntry.getValue());
				}
			}
		} catch (Exception exception) {
			error(exception);
			return false;
		}
		return true;
	}

	@Override
	public void writeMenuPostExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (this.instance == null || embed || this.instance.isExternal() || !isAdmin()) {
			return;
		}
		try {
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='avatar?copy=true");
			out.write(proxy.proxyString());
			out.write(instanceString());
			out.write("' class='button' title='Create a new avatar with the same details'><img src='images/copy.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Copy Details"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
			
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='avatar?export=true");
			out.write(proxy.proxyString());
			out.write(instanceString());
			out.write(this.loginBean.postTokenString());
			out.write("' class='button' title='Export and download the avatar and its media'><img src='images/download.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Export"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}

	@Override
	public String getAvatarThumb(AvatarImage avatar) {
		String file = super.getAvatarThumb(avatar);
		if (file.equals("images/bot-thumb.jpg")) {
			return "images/avatar-thumb.jpg";
		}
		return file;
	}

	@Override
	public String getAvatarImage(AvatarImage avatar) {
		String file = super.getAvatarImage(avatar);
		if (file.equals("images/bot.png")) {
			return "images/avatar.png";
		}
		return file;
	}

	public boolean createInstance(AvatarConfig config) {
		try {
			checkLogin();
			config.sanitize();
			Avatar newInstance = new Avatar(config.name);
			setInstance(newInstance);
			updateFromConfig(newInstance, config);
			newInstance.setDomain(getDomain());
			checkVerfied(config);
			//AdminDatabase.instance().validateNewAvatar(newInstance.getAlias(), config.description, config.tags, Site.ADULT, getDomain());
			setInstance(AdminDatabase.instance().createAvatar(newInstance, getUser(), config.categories, config.tags));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean createInstance(BotInstance bot) {
		try {
			checkLogin();
			Avatar newInstance = new Avatar(bot.getName());
			newInstance.setDomain(getDomain());
			newInstance.setDescription(bot.getDescription());
			newInstance.setDetails(bot.getDetails());
			newInstance.setDisclaimer(bot.getDisclaimer());
			newInstance.setWebsite(bot.getWebsite());
			newInstance.setLicense(bot.getLicense());
			newInstance.setAdult(bot.isAdult());
			newInstance.setPrivate(true);
			newInstance.setHidden(false);
			newInstance.setAccessMode(bot.getAccessMode());
			newInstance.setContentRating(bot.getContentRating());
			newInstance.setShowAds(bot.getShowAds());
			newInstance.setAdCode("");
			newInstance.setCategoriesString("Misc");
			setInstance(newInstance);
			setInstance(AdminDatabase.instance().createAvatar(newInstance, getUser(), "Misc", ""));
			if (bot.getAvatar() != null) {
				setInstance(AdminDatabase.instance().update(this.instance, bot.getAvatar().getImage()));
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	public boolean createUserAvatarInstance(User user) {
		try {
			checkLogin();
			Avatar newInstance = new Avatar(user.getUserId());
			newInstance.setDomain(getDomain());
			newInstance.setWebsite(user.getWebsite());
			newInstance.setPrivate(true);
			newInstance.setHidden(false);
			newInstance.setAdCode("");
			newInstance.setCategoriesString("Misc");
			setInstance(newInstance);
			setInstance(AdminDatabase.instance().createAvatar(newInstance, user, "Misc", ""));
			if (user.getAvatar() != null) {
				setInstance(AdminDatabase.instance().update(this.instance, user.getAvatar().getImage()));
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Create a directory link.
	 */
	public boolean createLink(AvatarConfig config) {
		try {
			checkLogin();
			config.sanitize();
			Avatar newInstance = new Avatar(config.name);
			newInstance.setDomain(getDomain());
			newInstance.setDescription(config.description);
			newInstance.setDetails(config.details);
			newInstance.setDisclaimer(config.disclaimer);
			newInstance.setWebsite(config.website);
			newInstance.setAdult(Site.ADULT);
			newInstance.setExternal(true);
			newInstance.setPaphus(config.website.contains("paphuslivechat") || config.website.contains("botlibre.biz"));
			newInstance.setTagsString(config.tags);
			newInstance.setCategoriesString(config.categories);
			setInstance(newInstance);
			checkVerfied(config);
			if (config.name.equals("")) {
				throw new BotException("Invalid name");
			}
			if (!config.website.contains("http")) {
				throw new BotException("You must enter a valid URL for an external avatar");
			}
			//AdminDatabase.instance().validateNewAvatar(newInstance.getAlias(), config.description, config.tags, Site.ADULT, getDomain());			
			setInstance(AdminDatabase.instance().createAvatar(newInstance, getUser(), config.categories, config.tags));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean updateAvatar(AvatarConfig config, String newdomain, Boolean adVerified, Boolean isFeatured) {
		try {
			checkLogin();
			checkInstance();
			checkAdminOrSuper();
			config.sanitize();
			Avatar newInstance = (Avatar)this.instance.clone();
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
			if (newInstance.getAdCode() == null || (config.adCode != null && !newInstance.getAdCode().equals(config.adCode))) {
				newInstance.setAdCodeVerified(false);
			}
			if (adVerified != null && isSuper()) {
				newInstance.setAdCodeVerified(adVerified);
			}
			if (isSuper() && isFeatured != null) { 
				newInstance.setFeatured(isFeatured);
			}
			setInstance(AdminDatabase.instance().updateAvatar(newInstance, config.categories, config.tags));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	@Override
	public Class<Avatar> getType() {
		return Avatar.class;
	}
	
	@Override
	public String getTypeName() {
		return "Avatar";
	}

	@Override
	public String getCreateURL() {
		return "create-avatar.jsp";
	}

	@Override
	public String getSearchURL() {
		return "avatar-search.jsp";
	}

	@Override
	public String getBrowseURL() {
		return "browse-avatar.jsp";
	}
	
	public String getEmbedWidth() {
		return embedWidth;
	}

	public void setEmbedWidth(String embedWidth) {
		this.embedWidth = embedWidth;
	}

	public String getEmbedHeight() {
		return embedHeight;
	}

	public void setEmbedHeight(String embedHeight) {
		this.embedHeight = embedHeight;
	}

	public String getEmbedBackground() {
		if (embedBackground == null) {
			return "";
		}
		return embedBackground;
	}
	
	@Override
	public void setInstance(Avatar avatar) {
		super.setInstance(avatar);
		this.embedHeight = "";
		this.embedWidth = "";
	}

	public void setEmbedBackground(String embedBackground) {
		this.embedBackground = embedBackground;
	}

	public AvatarMedia addAvatarMedia(byte[] data, String name, String type) {
		try {
			checkLogin();
			checkAdmin();
			checkInstance();
			Media media = new Media();
			media.setMedia(data);
			AvatarMedia avatarMedia = new AvatarMedia();
			avatarMedia.setName(Utils.sanitize(name));
			avatarMedia.setType(Utils.sanitize(type));
			avatarMedia.checkMediaType();
			setInstance(AdminDatabase.instance().addAvatarMedia(avatarMedia, media, this.instance));
			return avatarMedia;
		} catch (Exception failed) {
			error(failed);
			return null;
		}
	}
	
	public void saveAvatarBackground(byte[] data, String name, String type) {
		try {
			checkLogin();
			checkAdmin();
			checkInstance();
			Media media = new Media();
			media.setMedia(data);
			MediaFile mediaFile = new MediaFile();
			mediaFile.setName(Utils.sanitize(name));
			mediaFile.setType(Utils.sanitize(type));
			mediaFile.checkMediaType();
			setInstance(AdminDatabase.instance().updateAvatarBackground(mediaFile, media, this.instance));
		} catch (Exception failed) {
			error(failed);
		}
	}
	
	public void saveAvatarApiKeys() {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			setInstance(AdminDatabase.instance().updateAvatarApiKeys(embedNativeVoiceAppId, embedNativeVoiceApiKey, embedVoiceApiEndpoint, this.instance));
		} catch (Exception failed) {
			error(failed);
		}
	}

	@SuppressWarnings("unchecked")
	public void saveMedia(HttpServletRequest request) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			this.instance = (Avatar)this.instance.clone();
			this.instance.cloneMedia();
			for (AvatarMedia media : this.instance.getMedia()) {
				media.setTalking(false);
				media.setHD(false);
			}
			for (Object parameter : request.getParameterMap().entrySet()) {
				Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>)parameter;
				String key = entry.getKey();
				int index = key.indexOf(':');
				if ((index != -1) && (entry.getValue().length == 1)) {
					String type = key.substring(0, index);
					long id = Long.valueOf(key.substring(index + 1, key.length()));
					String value = Utils.sanitize(entry.getValue()[0]);
					AvatarMedia media = this.instance.getMedia(id);
					if (media == null) {
						throw new BotException("Missing media");
					}
					if (type.equals("emotions")) {
						media.setEmotions(value.trim().toUpperCase());
					} else if (type.equals("actions")) {
						media.setActions(value.trim());
					} else if (type.equals("poses")) {
						media.setPoses(value.trim());
					} else if (type.equals("talking")) {
						media.setTalking(true);
					} else if (type.equals("hd")) {
						media.setHD(true);
					}
				}
			}
			setInstance(AdminDatabase.instance().updateAvatarMedia(this.instance));
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void saveMedia(AvatarMediaConfig config) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			this.instance = (Avatar)this.instance.clone();
			this.instance.cloneMedia();
			AvatarMedia media = this.instance.getMedia(Long.valueOf(config.mediaId));
			if (media == null) {
				throw new BotException("Missing media");
			}
			media.setName(config.name);
			media.setEmotions(config.emotions.trim().toUpperCase());
			media.setActions(config.actions.trim());
			media.setPoses(config.poses.trim());
			media.setTalking(config.talking);
			media.setHD(config.hd);
			setInstance(AdminDatabase.instance().updateAvatarMedia(this.instance));
		} catch (Exception exception) {
			error(exception);
		}
	}

	@SuppressWarnings("unchecked")
	public void deleteMedia(HttpServletRequest request) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			Set<Long> ids = new HashSet<Long>();
			for (Object parameter : request.getParameterMap().entrySet()) {
				Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>)parameter;
				String key = entry.getKey();
				try {
					ids.add(Long.valueOf(key));
				} catch (NumberFormatException ignore) {}
			}
			if (ids.isEmpty()) {
				throw new BotException("Select media to delete.");
			}
			for (Long id : ids) {
				setInstance(AdminDatabase.instance().deleteAvatarMedia(id, this.instance));
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
	public void deleteMedia(long id) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			setInstance(AdminDatabase.instance().deleteAvatarMedia(id, this.instance));
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void deleteBackground() {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			setInstance(AdminDatabase.instance().deleteAvatarBackground(this.instance));
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public String getMediaFileName(AvatarMedia media) {
		String file = media.getFileName();
		if (file == null) {
			return "images/avatar.png";
		}
		return file;
	}

	public boolean isSelectAll() {
		return selectAll;
	}

	public void setSelectAll(boolean selectAll) {
		this.selectAll = selectAll;
	}
	
	public String getAvailableEmotionsString() {
		if (this.instance == null) {
			return "";
		}
		Collection<String> values = new HashSet<String>();
		for (AvatarMedia media : this.instance.getMedia()) {
			values.addAll(Utils.csv(media.getEmotions()));
		}
		values = new ArrayList<String>(values);
		Collections.sort((List<String>)values);
		StringWriter writer = new StringWriter();
		boolean first = true;
		for (String value : values) {
			if (!first) {
				writer.write(", ");
			} else {
				first = false;
			}
			writer.write("\"");
			writer.write(value.toLowerCase());
			writer.write("\"");
		}
		return writer.toString();		
	}
	
	public String getAvailableActionsString() {
		if (this.instance == null) {
			return "";
		}
		Collection<String> values = new HashSet<String>();
		for (AvatarMedia media : this.instance.getMedia()) {
			values.addAll(Utils.csv(media.getActions()));
		}
		values = new ArrayList<String>(values);
		Collections.sort((List<String>)values);
		StringWriter writer = new StringWriter();
		boolean first = true;
		for (String value : values) {
			if (!first) {
				writer.write(", ");
			} else {
				first = false;
			}
			writer.write("\"");
			writer.write(value.toLowerCase());
			writer.write("\"");
		}
		return writer.toString();		
	}
	
	public String getAvailablePosesString() {
		if (this.instance == null) {
			return "";
		}
		Collection<String> values = new HashSet<String>();
		for (AvatarMedia media : this.instance.getMedia()) {
			values.addAll(Utils.csv(media.getPoses()));
		}
		values = new ArrayList<String>(values);
		Collections.sort((List<String>)values);
		StringWriter writer = new StringWriter();
		boolean first = true;
		for (String value : values) {
			if (!first) {
				writer.write(", ");
			} else {
				first = false;
			}
			writer.write("\"");
			writer.write(value.toLowerCase());
			writer.write("\"");
		}
		return writer.toString();		
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

	public ChatResponse processMessage(AvatarMessage message) {
		this.avatarHD = message.hd;
		this.avatarFormat = message.format;
		ChatResponse response = new ChatResponse();
		response.message = message.message;
		response.avatarType = "image/jpeg";
		String emotion = message.emote;
		String action = message.action;
		String pose = message.pose;
		if (this.instance == null) {
			return response;
		} else {
			incrementConnects(ClientType.REST);
			response.avatar = getAvatarImage(this.instance);
			if (this.instance.getBackground() != null) {
				String background = this.instance.getBackground().getFileName();
				response.avatar = background;
				response.avatarBackground = background;
			}
			List<AvatarMedia> matching = null;

			// Audio
			if (action != null && !action.isEmpty()) {
				matching = this.instance.getAudio(action, pose);
				if (!matching.isEmpty()) {
					AvatarMedia media = Utils.random(matching);
					response.avatarActionAudio = media.getFileName();
					response.avatarActionAudioType = media.getType();
				}
			}
			matching = this.instance.getAudio("", pose);
			if (!matching.isEmpty()) {
				AvatarMedia media = Utils.random(matching);
				response.avatarAudio = media.getFileName();
				response.avatarAudioType = media.getType();
			}
			// Image/video
			if (action != null && !action.isEmpty()) {
				matching = this.instance.getMedia(emotion, action, pose, "");
				if (!matching.isEmpty()) {
					AvatarMedia media = randomMatch(matching);
					response.avatarAction = media.getFileName();
					response.avatarActionType = media.getType();
					if (media.isImage()) {
						response.avatar = media.getFileName();
						response.avatarType = media.getType();
						return response;
					}
				}
			}
			matching = this.instance.getMedia(emotion, "", pose, "");
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
			matching = this.instance.getMedia(emotion, "", pose, "talking");
			if (!matching.isEmpty()) {
				AvatarMedia media = randomMatch(matching);
				response.avatarTalk = media.getFileName();
				response.avatarTalkType = media.getType();
			}
		}
		return response;
	}

	public void generateEmbedCode() {
		if (getInstance() != null && (getInstance().isPrivate() || getInstance().getAccessMode() != AccessMode.Everyone)) {
			if (this.userName == null || this.userName.isEmpty()) {
				this.userName = getUserId();
				this.token = String.valueOf(getUser().getToken());
			}
		}
		if (isLoggedIn() && getUser().getApplicationId() == null) {
			getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
		}
		if (getEmbedWidth().isEmpty() && getInstance().getWidth() > 0) {
			setEmbedWidth(String.valueOf(getInstance().getWidth()));
		}
		
		StringWriter writer = new StringWriter();
		writer.write("<script type='text/javascript' src=\""+ Site.SECUREURLLINK + "/scripts/sdk.js\"></script>\n");
		if (this.embedResponsiveVoice) {
			writer.write("<script src='https://code.responsivevoice.org/responsivevoice.js'></script>\n");
		}
		writer.write("<script type='text/javascript'>\n");
		if (isLoggedIn()) {
			getUser().checkApplicationId();
		}
		if (isLoggedIn() && hasValidApplicationId()) {
			writer.write("SDK.applicationId = \"" + getUser().getApplicationId() + "\";\n");			
		} else {
			writer.write("SDK.applicationId = \"" + AdminDatabase.getTemporaryApplicationId() + "\";\n");
		}
		writer.write("var sdk = new SDKConnection();\n");
		writer.write("var web = new WebAvatar();\n");
		if (this.userName != null && !this.userName.isEmpty()) {
			writer.write("var user = new UserConfig();\n");
			writer.write("user.user = \"" + this.userName + "\";\n");
			if (this.token != null && !this.token.isEmpty()) {
				writer.write("user.token = \"" + this.token + "\";\n");
			} else if (this.password != null && !this.password.isEmpty()) {
				writer.write("user.password = \"" + Utils.encrypt(Utils.KEY, this.password) + "\";\n");
			}
			writer.write("sdk.connect(user, function() {\n");
		}
		writer.write("web.connection = sdk;\n");
		writer.write("web.avatar = \"" + getInstanceId() + "\";\n");
		writer.write("web.voice = \"" + getEmbedVoice() + "\";\n");
		writer.write("web.voiceMod = \"" + getEmbedVoiceMod() + "\";\n");
		if (this.embedNativeVoice) {
			writer.write("web.nativeVoice = true;\n");
			if (this.embedNativeVoiceName != null && !this.embedNativeVoiceName.isEmpty()) {
				writer.write("web.nativeVoiceName = \"" + getEmbedNativeVoiceName() + "\";\n");
			}
		}
		if (this.embedResponsiveVoice) {
			writer.write("SDK.initResponsiveVoice();\n");
		}
		if (this.embedBingSpeech) {
			writer.write("SDK.initBingSpeech(" + getInstanceId() + ", 'avatar');\n");
			//writer.write("web.bingSpeech = true;\n");
		}
		if (this.embedQQSpeech) {
			writer.write("SDK.initQQSpeech(" + getInstanceId() + ", 'avatar');\n");
			//writer.write("web.qqSpeech = true;\n");
		}
		if (this.embedLang != null && !this.embedLang.isEmpty()) {
			writer.write("web.lang = \"" + getEmbedLang() + "\";\n");
		}
		if (!getEmbedWidth().isEmpty()) {
			writer.write("web.width = \"" + getEmbedWidth() + "\";\n");
		}
		if (!getEmbedHeight().isEmpty()) {
			writer.write("web.height = \"" + getEmbedHeight() + "\";\n");
		}
		if (!getEmbedBackground().isEmpty()) {
			writer.write("web.background = \"" + getEmbedBackground() + "\";\n");
		}
		writer.write("web.createBox();\n");
		TextStream stream = new TextStream(this.embedSpeech);
		while (!stream.atEnd()) {
			String message = stream.nextLine().trim();
			if (!message.isEmpty()) {
				writer.write("web.addMessage(\"" + message + "\", \"" + this.embedEmotion + "\", \"" + this.embedAction + "\", \"" + this.embedPose + "\");\n");				
			}
		}
		if (this.embedResponsiveVoice) {
			writer.write("setTimeout(function() { web.processMessages(); }, 1000);\n");
		} else {
			writer.write("web.processMessages();\n");
		}
		if (this.userName != null && !this.userName.isEmpty()) {
			writer.write("});\n");
		}
		writer.write("</script>\n");	
		setEmbedCode(writer.toString());
	}

	public String getEmbedCode() {
		if (this.embedCode.isEmpty()) {
			generateEmbedCode();
		}
		return embedCode;
	}

	public void setEmbedCode(String code) {
		if (code == null) {
			code = "";
		}
		this.embedCode = code;
		this.embedDisplayCode = this.embedCode.replace("<", "&lt;").replace(">", "&gt;");
	}
	
	public String getVoiceCheckedString(String voice) {
		if (voice.equals(this.embedVoice)) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getVoiceModCheckedString(String mod) {
		if (mod.equals(this.embedVoiceMod)) {
			return "selected=\"selected\"";
		}
		return "";
	}

	public String getEmbedVoice() {
		return embedVoice;
	}

	public void setEmbedVoice(String embedVoice) {
		this.embedVoice = embedVoice;
	}
	
	public String getEmbedVoiceMod() {
		return embedVoiceMod;
	}

	public void setEmbedVoiceMod(String embedVoiceMod) {
		this.embedVoiceMod = embedVoiceMod;
	}

	public boolean getEmbedNativeVoice() {
		return embedNativeVoice;
	}

	public void setEmbedNativeVoice(boolean embedNativeVoice) {
		this.embedNativeVoice = embedNativeVoice;
	}

	public String getEmbedNativeVoiceName() {
		return embedNativeVoiceName;
	}

	public void setEmbedNativeVoiceName(String embedNativeVoiceName) {
		this.embedNativeVoiceName = embedNativeVoiceName;
	}
	
	public String getEmbedNativeVoiceAppId() {
		if(instance != null) {
			return instance.getNativeVoiceAppId(); 
		}
		else return "";
	}

	public void setEmbedNativeVoiceAppId(String embedNativeVoiceAppId) {
		this.embedNativeVoiceAppId = embedNativeVoiceAppId;
	}
	
	public String getEmbedNativeVoiceApiKey() {
		if(instance != null) {
			return instance.getNativeVoiceApiKey();
		}
		else return "";
	}

	public void setEmbedNativeVoiceApiKey(String embedNativeVoiceApiKey) {
		this.embedNativeVoiceApiKey = embedNativeVoiceApiKey;
	}
	
	public String getEmbedVoiceApiEndpoint() {
		if(instance != null) {
			return instance.getVoiceApiEndpoint();
		}
		else return "https://eastus.api.cognitive.microsoft.com/sts/v1.0/issueToken";
	}

	public void setEmbedVoiceApiEndpoint(String embedVoiceApiEndpoint) {
		this.embedVoiceApiEndpoint = embedVoiceApiEndpoint;
	}
	
	public String getEmbedNativeVoiceToken() {
		return embedNativeVoiceToken;
	}

	public void setEmbedNativeVoiceToken(String embedNativeVoiceToken) {
		this.embedNativeVoiceToken = embedNativeVoiceToken;
	}
	
	public String getNativeVoiceCheckedString(String voice) {
		if (voice.equals(this.embedNativeVoiceName)) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public void setEmbedBingSpeech(boolean embedBingSpeech) {
		this.embedBingSpeech = embedBingSpeech;
	}
	
	public boolean getEmbedBingSpeech() {
		return embedBingSpeech;
	}
	
	public void setEmbedQQSpeech(boolean embedQQSpeech) {
		this.embedQQSpeech = embedQQSpeech;
	}
	
	public boolean getEmbedQQSpeech() {
		return embedQQSpeech;
	}

	public String getEmbedLang() {
		return embedLang;
	}

	public void setEmbedLang(String embedLang) {
		this.embedLang = embedLang;
	}

	public String getEmbedDisplayCode() {
		return embedDisplayCode;
	}

	public void setEmbedDisplayCode(String embedDisplayCode) {
		this.embedDisplayCode = embedDisplayCode;
	}

	public String getEmbedSpeech() {
		return embedSpeech;
	}

	public void setEmbedSpeech(String embedSpeech) {
		this.embedSpeech = embedSpeech;
	}

	public String getEmbedEmotion() {
		return embedEmotion;
	}

	public void setEmbedEmotion(String embedEmotion) {
		this.embedEmotion = embedEmotion;
	}

	public String getEmbedAction() {
		return embedAction;
	}

	public void setEmbedAction(String embedAction) {
		this.embedAction = embedAction;
	}

	public String getEmbedPose() {
		return embedPose;
	}

	public void setEmbedPose(String embedPose) {
		this.embedPose = embedPose;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		if (userName == null) {
			userName = "";
		}
		this.userName = userName;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		if (token == null) {
			token = "";
		}
		this.token = token;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		if (password == null) {
			password = "";
		}
		this.password = password;
	}
	
}

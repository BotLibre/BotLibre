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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.AvatarImage;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.admin.ContentRating;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.admin.Graphic;
import org.botlibre.web.admin.Media;
import org.botlibre.web.admin.MediaFile;
import org.botlibre.web.admin.User;
import org.botlibre.web.admin.WebMedium;
import org.botlibre.web.rest.GraphicConfig;
import org.botlibre.web.rest.MediaFileConfig;

public class GraphicBean extends WebMediumBean<Graphic> {
	
	public GraphicBean() {
	}

	@Override
	public String getEmbeddedBanner() {
		return "graphic-banner.jsp";
	}	

	@Override
	public String getPostAction() {
		return "graphic";
	}

	/**
	 * Record the API access.
	 */
	public boolean apiConnect() {
		incrementConnects(ClientType.REST);
		return true;
	}
	
	/**
	 * Download the media file.
	 */
	public boolean downloadMedia(HttpServletResponse response) {
		try {
			checkInstance();
			if (this.instance.getMedia() == null) {
				throw new BotException("Media file is missing");
			}
			this.instance.getMedia().checkMediaType();
			response.setContentType(this.instance.getMedia().getType());
			response.setHeader("Content-disposition","attachment; filename=" + encodeURI(this.instance.getMedia().getName()));
			OutputStream out = response.getOutputStream();
			Media data = AdminDatabase.instance().findMedia(this.instance.getMedia().getMediaId());
			out.write(data.getMedia(), 0, data.getMedia().length);
			out.flush();
		} catch (Exception exception) {
			error(exception);
			return false;
		}
		return true;
	}

	public void saveMedia(byte[] data, String name, String type) {
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
			setInstance(AdminDatabase.instance().updateGraphicMedia(mediaFile, media, this.instance));
			
			if (mediaFile.isImage()) {
				setInstance(AdminDatabase.instance().update(this.instance, data));
			}
		} catch (Exception failed) {
			error(failed);
		}
	}
	
	public List<Graphic> getAllInstances(Domain domain) {
		try {
			List<Graphic> results = AdminDatabase.instance().getAllGraphics(this.page, this.pageSize, this.categoryFilter, this.nameFilter, this.userFilter, 
					this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, this.startFilter, this.endFilter, getUser(), domain, false);
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (results.size() < this.pageSize) {
					this.resultsSize = results.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllGraphicsCount(this.categoryFilter, this.nameFilter, this.userFilter, this.instanceFilter, 
							this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, this.startFilter, this.endFilter, getUser(), domain, false);
				}
			}
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Graphic>();
		}
	}
	
	public List<Graphic> getAllLinkableInstances() {
		try {
			List<Graphic> privateGraphics = AdminDatabase.instance().getAllGraphics(
					0, 100, "", "", "", InstanceFilter.Private, InstanceRestrict.None, InstanceSort.MonthlyConnects, ContentRating.Everyone, "", "", "", getUser(), getDomain(), true);
			List<Graphic> publicGraphics = AdminDatabase.instance().getAllGraphics(
					0, 100, "", "", "", InstanceFilter.Public, InstanceRestrict.None, InstanceSort.MonthlyConnects, ContentRating.Everyone, "", "", "", getUser(), getDomain(), true);
			List<Graphic> results = new ArrayList<Graphic>(privateGraphics);
			results.addAll(publicGraphics);
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Graphic>();
		}
	}

	public List<Graphic> getAllFeaturedInstances() {
		try {
			return AdminDatabase.instance().getAllGraphics(
					0, 100, "", "", "", InstanceFilter.Featured, InstanceRestrict.None, InstanceSort.MonthlyConnects, this.loginBean.contentRating, "", "", "", null, getDomain(), false);
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Graphic>();
		}
	}

	@Override
	public String getAvatarThumb(AvatarImage avatar) {
		String file = super.getAvatarThumb(avatar);
		if (file.equals("images/bot-thumb.jpg")) {
			return "images/graphic-thumb.jpg";
		}
		return file;
	}

	@Override
	public String getAvatarImage(AvatarImage avatar) {
		String file = super.getAvatarImage(avatar);
		if (file.equals("images/bot.png")) {
			return "images/graphic.png";
		}
		return file;
	}

	public String getAvatarThumb(WebMedium instance) {
		String file = super.getAvatarThumb(instance);
		if (file.equals("images/graphic-thumb.jpg") && ((Graphic)instance).getMedia() != null) {
			MediaFile media = ((Graphic)instance).getMedia();
			if (media.isAudio()) {
				return "images/audio-thumb.jpg";				
			} else if (media.isVideo()) {
				return "images/video-media-thumb.jpg";				
			}
		}
		return file;
	}

	public String getAvatarImage(WebMedium instance) {
		String file = super.getAvatarThumb(instance);
		if (file.equals("images/graphic.png") && ((Graphic)instance).getMedia() != null) {
			MediaFile media = ((Graphic)instance).getMedia();
			if (media.isAudio()) {
				return "images/audio.png";				
			} else if (media.isVideo()) {
				return "images/video-media.png";
			}
		}
		return file;
	}

	public boolean createInstance(GraphicConfig config) {
		try {
			checkLogin();
			config.sanitize();
			Graphic newInstance = new Graphic(config.name);
			setInstance(newInstance);
			updateFromConfig(newInstance, config);
			newInstance.setDomain(getDomain());
			checkVerfied(config);
			//AdminDatabase.instance().validateNewGraphic(newInstance.getAlias(), config.description, config.tags, Site.ADULT, getDomain());
			setInstance(AdminDatabase.instance().createGraphic(newInstance, getUser(), config.categories, config.tags));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Create a directory link.
	 */
	public boolean createLink(GraphicConfig config) {
		try {
			checkLogin();
			config.sanitize();
			Graphic newInstance = new Graphic(config.name);
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
				throw new BotException("You must enter a valid URL for an external graphic");
			}
			//AdminDatabase.instance().validateNewGraphic(newInstance.getAlias(), config.description, config.tags, config.isAdult, getDomain());			
			setInstance(AdminDatabase.instance().createGraphic(newInstance, getUser(), config.categories, config.tags));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean updateGraphic(GraphicConfig config, String newdomain, Boolean featured, Boolean adVerified) {
		try {
			checkLogin();
			checkInstance();
			checkAdminOrSuper();
			config.sanitize();
			Graphic newInstance = (Graphic)this.instance.clone();
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
			if (isSuper() && featured != null) { 
				newInstance.setFeatured(featured);
			}
			newInstance.setTagsString(config.tags);
			newInstance.setCategoriesString(config.categories);
			setInstance(AdminDatabase.instance().updateGraphic(newInstance, config.categories, config.tags));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	@Override
	public Class<Graphic> getType() {
		return Graphic.class;
	}
	
	@Override
	public String getTypeName() {
		return "Graphic";
	}

	@Override
	public String getCreateURL() {
		return "create-graphic.jsp";
	}

	@Override
	public String getSearchURL() {
		return "graphic-search.jsp";
	}

	@Override
	public String getBrowseURL() {
		return "browse-graphic.jsp";
	}

	public void writeIconHTML(SessionProxyBean proxy, Writer out) {
		try {
			out.write("<div style='float:right'>\n");
			if (isAdmin()) {
				out.write("<a onclick='return changeIcon()' href='#' style='text-decoration:none' title=\"Change the ");
				out.write(getTypeName().toLowerCase());
				out.write("'s display icon to a new image\">\n");
				out.write("<img src='");
				out.write(getAvatarImage(getDisplayInstance()));
				out.write("' class='small-icon'/>\n");
				out.write("</a>\n");
			} else if (isValidUser()) {
				out.write("<img src='");
				out.write(getAvatarImage(getDisplayInstance()));
				out.write("' class='small-icon'/>\n");
			} else {
				out.write("<img src='");
				out.write(getAvatarThumb(getDisplayInstance()));
				out.write("' class='small-icon'/>\n");
			}
			out.write("</div>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	/**
	 * Copy instance.
	 */
	public void copyInstance() {
		try {
			checkLogin();
			checkInstance();
			Graphic parent = getInstance();
			Graphic newInstance = new Graphic(parent.getName());
			newInstance.setDescription(parent.getDescription());
			newInstance.setDetails(parent.getDetails());
			newInstance.setDisclaimer(parent.getDisclaimer());
			newInstance.setTagsString(parent.getTagsString());
			newInstance.setCategoriesString(parent.getCategoriesString());
			newInstance.setLicense(parent.getLicense());
			newInstance.setContentRating(parent.getContentRating());
			setInstance(newInstance);
			setForking(true);
		} catch (Exception failed) {
			error(failed);
		}
	}
	
	/**
	 * Download the graphics media as a zip file.
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
			GraphicConfig config = this.instance.buildConfig();
			JAXBContext context = JAXBContext.newInstance(GraphicConfig.class);
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
			MediaFile media = this.instance.getMedia();
			String ext = "mp4";
			int index = media.getFileName().indexOf('.');
			if (index != -1) {
				ext = media.getFileName().substring(index + 1, media.getFileName().length());
			}
			entry = new ZipEntry("media." + ext);
			zip.putNextEntry(entry);
			Media data = AdminDatabase.instance().findMedia(media.getMediaId());
			zip.write(data.getMedia());
			zip.closeEntry();

			entry = new ZipEntry(String.valueOf("media.xml"));
			zip.putNextEntry(entry);
			MediaFileConfig mediaConfig = media.toConfig();
			context = JAXBContext.newInstance(MediaFileConfig.class);
			writer = new StringWriter();
			marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(mediaConfig, writer);
			zip.write(writer.toString().getBytes("UTF-8"));
			zip.closeEntry();
			
			OutputStream out = response.getOutputStream();
			zip.finish();
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
	 * Download all of the graphics media on a page as a zip file.
	 */
	public boolean exportAll(HttpServletRequest request, HttpServletResponse response) {
		try {
			checkSuper();
			Set<Long> ids = new HashSet<Long>();
			for (Object parameter : request.getParameterMap().entrySet()) {
				Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>)parameter;
				String key = entry.getKey();
				try {
					ids.add(Long.valueOf(key));
				} catch (NumberFormatException ignore) {}
			}
			if (ids.isEmpty()) {
				throw new BotException("Missing selection");
			}
			
			response.setContentType("application/zip");
			response.setHeader("Content-disposition","attachment; filename=" + encodeURI("Exported_Graphics.zip"));

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ZipOutputStream zip = new ZipOutputStream(stream);
			for (Long id : ids) {
				Graphic graphic = AdminDatabase.instance().validate(Graphic.class, id, getUserId());
				ZipEntry entry = new ZipEntry(graphic.getId() + "_meta.xml");
				zip.putNextEntry(entry);
				GraphicConfig config = graphic.buildConfig();
				JAXBContext context = JAXBContext.newInstance(GraphicConfig.class);
				StringWriter writer = new StringWriter();
				Marshaller marshaller = context.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshaller.marshal(config, writer);
				zip.write(writer.toString().getBytes("UTF-8"));
				zip.closeEntry();
	
				if (graphic.getAvatar() != null) {
					entry = new ZipEntry(graphic.getId() + "_icon.jpg");
					zip.putNextEntry(entry);
					zip.write(graphic.getAvatar().getImage());
					zip.closeEntry();
				}
				MediaFile media = graphic.getMedia();
				if (media != null) {
					String ext = "mp4";
					int index = media.getFileName().indexOf('.');
					if (index != -1) {
						ext = media.getFileName().substring(index + 1, media.getFileName().length());
					}
					entry = new ZipEntry(graphic.getId() + "_media." + ext);
					zip.putNextEntry(entry);
					Media data = AdminDatabase.instance().findMedia(media.getMediaId());
					zip.write(data.getMedia());
					zip.closeEntry();
		
					entry = new ZipEntry(String.valueOf(graphic.getId() + "_mediafile.xml"));
					zip.putNextEntry(entry);
					MediaFileConfig mediaConfig = media.toConfig();
					context = JAXBContext.newInstance(MediaFileConfig.class);
					writer = new StringWriter();
					marshaller = context.createMarshaller();
					marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
					marshaller.marshal(mediaConfig, writer);
					zip.write(writer.toString().getBytes("UTF-8"));
					zip.closeEntry();
				}
			}
			OutputStream out = response.getOutputStream();
			zip.finish();
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
	 * Import one or multiple graphics from a zip file.
	 */
	public boolean importGraphic(byte[] bytes) {
		try {
			checkLogin();
			
			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			ZipInputStream zip = new ZipInputStream(stream);
			
			ZipEntry entry = zip.getNextEntry();
			boolean found = false;
			while (entry != null) {
				byte[] icon = null;
				byte[] media = null;
				String name = "media";
				String type = "jpg";
				if (entry.getName().equals("meta.xml") || entry.getName().endsWith("_meta.xml")) {
					byte[] fileBytes = BotBean.loadImageFile(zip, false, isSuperUser() ? Site.MAX_UPLOAD_SIZE * 100 : Site.MAX_UPLOAD_SIZE);
					name = entry.getName().replaceAll("_meta", "").replaceAll(".xml", "");
					JAXBContext context = JAXBContext.newInstance(GraphicConfig.class);
					Unmarshaller marshaller = context.createUnmarshaller();
					ByteArrayInputStream fileStream = new ByteArrayInputStream(fileBytes);
					GraphicConfig config = (GraphicConfig)marshaller.unmarshal(fileStream);
					if (!createInstance(config)) {
						return false;
					}
					found = true;

					ByteArrayInputStream innerStream = new ByteArrayInputStream(bytes);
					ZipInputStream innerZip = new ZipInputStream(innerStream);
					
					ZipEntry innerEntry = innerZip.getNextEntry();
					// For each graphic meta file find the icon and media files.
					for(;innerEntry!=null; innerEntry = innerZip.getNextEntry()) {
						if (innerEntry.getName().equals("icon.jpg") || innerEntry.getName().endsWith(name + "_icon.jpg")) {
							fileBytes = BotBean.loadImageFile(innerZip, false);
							icon = fileBytes;
						} else if (innerEntry.getName().equals("media.xml") || innerEntry.getName().endsWith(name + "_mediafile.xml")) {
							fileBytes = BotBean.loadImageFile(innerZip, false);
							context = JAXBContext.newInstance(MediaFileConfig.class);
							marshaller = context.createUnmarshaller();
							fileStream = new ByteArrayInputStream(fileBytes);
							MediaFileConfig mediaConfig = (MediaFileConfig)marshaller.unmarshal(fileStream);
							type = mediaConfig.type;
							name = mediaConfig.name;
						} else if (innerEntry.getName().startsWith("media") || innerEntry.getName().contains(name + "_media")) {
							fileBytes = BotBean.loadImageFile(innerZip, false);
							media = fileBytes;
						}
						innerZip.closeEntry();
					}
					if (icon != null) {
						update(icon);
					}
					if (media != null) {
						saveMedia(media, name, type);
					}
				}
				zip.closeEntry();
				entry = zip.getNextEntry();
			}
			zip.close();
			stream.close();
			
			if (!found) {
				throw new BotException("Missing meta.xml or [id]_meta.xml file in export archive");
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
			out.write("<td><a class='menuitem' href='graphic?copy=true");
			out.write(proxy.proxyString());
			out.write(instanceString());
			out.write("' class='button' title='Create a new graphic with the same details'><img src='images/copy.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Copy Details"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
			
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='graphic?export=true");
			out.write(proxy.proxyString());
			out.write(instanceString());
			out.write(this.loginBean.postTokenString());
			out.write("' class='button' title='Export and download the graphic and its metadata'><img src='images/download.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Export"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void writeDetailsTabExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			if (!getDisplayInstance().isExternal()) {
				out.write("File Link: <a target='_blank' href='");
				out.write(Site.SECUREURLLINK);
				out.write("/graphic?file&id=");
				out.write(String.valueOf(getDisplayInstance().getId()));
				out.write(getDisplayInstance().getMedia() == null ? "" : "&name=" + getDisplayInstance().getMedia().getName());
				out.write("'>");
				out.write(Site.SECUREURLLINK);
				out.write("/graphic?file&id=");
				out.write(String.valueOf(getDisplayInstance().getId()));
				out.write(getDisplayInstance().getMedia() == null ? "" : "&name=" + getDisplayInstance().getMedia().getName());
				out.write("</a><br/>\n");
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
}

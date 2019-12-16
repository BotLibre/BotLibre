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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
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
import org.botlibre.web.admin.Domain;
import org.botlibre.web.admin.User;
import org.botlibre.web.rest.ScriptConfig;
import org.botlibre.web.script.Script;
import org.botlibre.web.script.ScriptSource;

public class ScriptBean extends WebMediumBean<Script> {
	String languageFilter = "";
	Long viewSource;
	
	public ScriptBean() {
	}

	/**
	 * Record the API access.
	 */
	public boolean apiConnect() {
		incrementConnects(ClientType.REST);
		return true;
	}
	
	@Override
	public boolean allowSubdomain() {
		return true;
	}
	
	public String getAllLanguagesString() {
		return "\"AIML\", \"Self\", \"ChatScript\", \"Chat Log\", \"Response List\", \"CSV List\", \"HTML\", \"XML\", \"CSS\", \"JavaScript\", \"JSON\", \"CSV\", \"Set\", \"Map\", \"Properties\", \"JQuery\", \"Java\", \"PHP\", \"C#\", \"C\", \"Objective C\", \"Smalltalk\", \"SQL\"";
	}

	@Override
	public String getEmbeddedBanner() {
		return "script-banner.jsp";
	}

	@Override
	public String getPostAction() {
		return "script";
	}
	
	public boolean hasEmptyToolbar(boolean embed) {
		return false;
	}

	@Override
	public void writeToolbarExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		writeVersionsButtonHTML(proxy, out);
	}

	@Override
	public void writeMenuExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		writeVersionsMenuItemHTML(proxy, out);
	}
	
	public void writeVersionsMenuItemHTML(SessionProxyBean proxy, Writer out) {
		if (getInstance() == null || getInstance().isExternal()) {
			return;
		}
		try {
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='");
			out.write(getPostAction());
			out.write("?versions=true");
			out.write(proxy.proxyString());
			out.write("&id=" + getInstanceId());
			out.write("' title=\"View and administer the script's version history\"><img src='images/version.svg' class='menu'/> Version History</a></td>\n");
			out.write("</tr>\n");
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
			Script parent = getInstance();
			Script newInstance = new Script(parent.getName());
			newInstance.setDescription(parent.getDescription());
			newInstance.setDetails(parent.getDetails());
			newInstance.setDisclaimer(parent.getDisclaimer());
			newInstance.setTagsString(parent.getTagsString());
			newInstance.setCategoriesString(parent.getCategoriesString());
			newInstance.setLicense(parent.getLicense());
			newInstance.setContentRating(parent.getContentRating());
			newInstance.setLanguage(parent.getLanguage());
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
			ScriptConfig config = this.instance.buildConfig();
			JAXBContext context = JAXBContext.newInstance(ScriptConfig.class);
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
			String source = this.instance.getSourceCode();
			String ext = this.instance.getExt();
			entry = new ZipEntry("source." + ext);
			zip.putNextEntry(entry);
			zip.write(source.getBytes("UTF-8"));
			zip.closeEntry();
			
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
	 * Import the script meta-data and source from a zip file.
	 */
	public boolean importScript(byte[] bytes) {
		try {
			checkLogin();
			
			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			ZipInputStream zip = new ZipInputStream(stream);
			
			ZipEntry entry = zip.getNextEntry();
			byte[] icon = null;
			byte[] source = null;
			boolean found = false;
			while (entry != null) {
				byte[] fileBytes = BotBean.loadImageFile(zip, false);
				if (entry.getName().equals("meta.xml")) {
					JAXBContext context = JAXBContext.newInstance(ScriptConfig.class);
					Unmarshaller marshaller = context.createUnmarshaller();
					ByteArrayInputStream fileStream = new ByteArrayInputStream(fileBytes);
					ScriptConfig config = (ScriptConfig)marshaller.unmarshal(fileStream);
					if (!createInstance(config)) {
						return false;
					}
					found = true;
				} else if (entry.getName().equals("icon.jpg")) {
					icon = fileBytes;
				} else if (entry.getName().startsWith("source")) {
					source = fileBytes;
				}
				zip.closeEntry();
				entry = zip.getNextEntry();
			}
			zip.close();
			stream.close();
			
			if (!found) {
				throw new BotException("Missing meta.xml file in export archive");
			}
			if (icon != null) {
				update(icon);
			}
			if (source != null) {
				updateScriptSource(new String(source, "UTF-8"), false, "0.1");
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
			out.write("<td><a class='menuitem' href='script?copy=true");
			out.write(proxy.proxyString());
			out.write(instanceString());
			out.write("' class='button' title='Create a new script with the same details'><img src='images/copy.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Copy Details"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
			
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='script?export=true");
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

	public void writeVersionsButtonHTML(SessionProxyBean proxy, Writer out) {
		if (getInstance() == null || getInstance().isExternal()) {
			return;
		}
		try {
			out.write("<a href='");
			out.write(getPostAction());
			out.write("?versions=true");
			out.write(proxy.proxyString());
			out.write("&id=" + getInstanceId());
			out.write("' title=\"View and administer the script's version history\"><img src='images/version.svg' class='toolbar'/></a>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	@Override
	public void writeInfoTabExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			out.write("<span>Language: <a href='script?language-filter=");
			out.write(getDisplayInstance().getLanguage());
			out.write("'>");
			out.write(getDisplayInstance().getLanguage());
			out.write("</a></span><br/>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}

	@Override
	public void writeDetailsTabExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			if (!getDisplayInstance().isExternal()) {
				out.write("File Link: <a target='_blank' href='");
				out.write(Site.SANDBOXURLLINK);
				out.write("/script?file&id=");
				out.write(String.valueOf(getDisplayInstance().getId()));
				out.write("'>");
				out.write(Site.SANDBOXURLLINK);
				out.write("/script?file&id=");
				out.write(String.valueOf(getDisplayInstance().getId()));
				out.write("</a><br/>\n");
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	@Override
	public void writeBrowseStats(StringWriter writer, Script instance) {
		writer.write("Language: " + instance.getLanguage() + "<br/>\n");
	}
	
	@Override
	public void writeSearchFields(StringWriter writer) {
		writer.write("<div class='search-div'><span class='search-span'>Language</span> ");
		writer.write("<select id='searchselect' name='language-filter' onchange='this.form.submit()'>\n");
		writer.write("<option value='' " + getLanguageCheckedString("") + "></option>\n");
		writer.write("<option value='AIML' " + getLanguageCheckedString("AIML") + ">AIML</option>\n");
		writer.write("<option value='Self' " + getLanguageCheckedString("Self") + ">Self</option>\n");
		writer.write("<option value='ChatScript' " + getLanguageCheckedString("ChatScript") + ">ChatScript</option>\n");
		writer.write("<option value='Chat Log' " + getLanguageCheckedString("Chat Log") + ">Chat Log</option>\n");
		writer.write("<option value='Response List' " + getLanguageCheckedString("Response List") + ">Response List</option>\n");
		writer.write("<option value='CSV List' " + getLanguageCheckedString("CSV List") + ">CSV List</option>\n");
		writer.write("<option value='HTML' " + getLanguageCheckedString("HTML") + ">HTML</option>\n");
		writer.write("<option value='CSS' " + getLanguageCheckedString("CSS") + ">CSS</option>\n");
		writer.write("<option value='XML' " + getLanguageCheckedString("XML") + ">XML</option>\n");
		writer.write("<option value='JSON' " + getLanguageCheckedString("JSON") + ">JSON</option>\n");
		writer.write("<option value='CSV' " + getLanguageCheckedString("CSV") + ">CSV</option>\n");
		writer.write("<option value='Set' " + getLanguageCheckedString("Set") + ">Set</option>\n");
		writer.write("<option value='Map' " + getLanguageCheckedString("Map") + ">Map</option>\n");
		writer.write("<option value='Properties' " + getLanguageCheckedString("Properties") + ">Properties</option>\n");
		writer.write("<option value='JavaScript' " + getLanguageCheckedString("JavaScript") + ">JavaScript</option>\n");
		writer.write("<option value='JQuery' " + getLanguageCheckedString("JQuery") + ">JQuery</option>\n");
		writer.write("<option value='Java' " + getLanguageCheckedString("Java") + ">Java</option>\n");
		writer.write("<option value='PHP' " + getLanguageCheckedString("PHP") + ">PHP</option>\n");
		writer.write("<option value='C#' " + getLanguageCheckedString("C#") + ">C#</option>\n");
		writer.write("<option value='Objective C' " + getLanguageCheckedString("Objective C") + ">Objective C</option>\n");
		writer.write("<option value='Smalltalk' " + getLanguageCheckedString("Smalltalk") + ">Smalltalk</option>\n");
		writer.write("<option value='SQL' " + getLanguageCheckedString("SQL") + ">SQL</option>\n");
		writer.write("</select>\n");
		writer.write("</div>\n");
	}
	
	public String getLanguageCheckedString(String language) {
		if (language != null && language.equals(this.languageFilter)) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getSourceLanguage() {
		if (this.displayInstance == null) {
			return "";
		}
		String language = this.displayInstance.getLanguage();
		if (language == null) {
			return "";
		}
		if (language.equalsIgnoreCase("aiml") || language.equalsIgnoreCase("xml")) {
			return "xml";
		} else if (language.equalsIgnoreCase("html")) {
			return "html";
		} else if (language.equalsIgnoreCase("java")) {
			return "java";
		} else if (language.equalsIgnoreCase("javascript") || language.equalsIgnoreCase("json")) {
			return "javascript";
		} else if (language.equalsIgnoreCase("c")) {
			return "c_cpp";
		} else if (language.equalsIgnoreCase("Objective C")) {
			return "objectivec";
		} else if (language.equalsIgnoreCase("C#")) {
			return "csharp";
		} else if (language.equalsIgnoreCase("php")) {
			return "php";
		} else if (language.equalsIgnoreCase("css")) {
			return "css";
		} else if (language.equalsIgnoreCase("groovy")) {
			return "groovy";
		} else if (language.equalsIgnoreCase("perl")) {
			return "perl";
		} else if (language.equalsIgnoreCase("python")) {
			return "python";
		} else if (language.equalsIgnoreCase("python")) {
			return "python";
		} else if (language.equalsIgnoreCase("ruby")) {
			return "ruby";
		} else if (language.equalsIgnoreCase("scala")) {
			return "scala";
		} else if (language.equalsIgnoreCase("sql")) {
			return "sql";
		} else if (language.equalsIgnoreCase("Visual Basic")) {
			return "vbscript";
		} else if (language.equalsIgnoreCase("Self")) {
			return "self";
		}
		return "text";
	}

	@SuppressWarnings("unchecked")
	public void deleteVersions(HttpServletRequest request, boolean confirm) {
		try {
			checkLogin();
			checkInstance();
			checkAdminOrSuper();
			if (!confirm) {
				throw new BotException("Must check 'I'm sure'");
			}
			Set<Long> ids = new HashSet<Long>();
			for (Object parameter : request.getParameterMap().entrySet()) {
				Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>)parameter;
				String key = entry.getKey();
				try {
					ids.add(Long.valueOf(key));
				} catch (NumberFormatException ignore) {}
			}
			if (ids.isEmpty()) {
				throw new BotException("Select versions to delete.");
			}
			for (Long id : ids) {
				if (this.instance.getSource() != null && this.instance.getSource().getId() == id) {
					throw new BotException("Cannot delete current version.");
				}
				AdminDatabase.instance().deleteScriptSource(id);
			}
		} catch (Exception exception) {
			error(exception);
		}
	}

	@SuppressWarnings("unchecked")
	public void viewVersion(HttpServletRequest request) {
		try {
			List<Long> ids = new ArrayList<Long>();
			for (Object parameter : request.getParameterMap().entrySet()) {
				Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>)parameter;
				String key = entry.getKey();
				try {
					ids.add(Long.valueOf(key));
				} catch (NumberFormatException ignore) {}
			}
			if (ids.isEmpty()) {
				throw new BotException("Select version to view.");
			}
			if (ids.size() > 1) {
				throw new BotException("Select only a single version to view.");
			}
			this.viewSource = ids.get(0);
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void setViewSource(Long viewSource) {
		this.viewSource = viewSource;
	}

	public ScriptSource getViewSource() {
		try {
			if (this.viewSource != null) {
				return AdminDatabase.instance().validateScriptVersion(this.viewSource);
			}
		} catch (Exception exception) {
			error(exception);
		}
		return null;
	}
	
	public List<Script> getAllInstances(Domain domain) {
		try {
			List<Script> results = AdminDatabase.instance().getAllScripts(this.page, this.pageSize, this.languageFilter, this.categoryFilter, this.nameFilter,
					this.userFilter, this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, getUser(), domain, false);
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (results.size() < this.pageSize) {
					this.resultsSize = results.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllScriptsCount(this.languageFilter, this.categoryFilter, this.nameFilter,
							this.userFilter, this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, getUser(), domain, false);
				}
			}
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Script>();
		}
	}

	public List<Script> getAllFeaturedInstances() {
		try {
			return AdminDatabase.instance().getAllScripts(
					0, 100, "", "", "", "", InstanceFilter.Featured, InstanceRestrict.None, InstanceSort.MonthlyConnects, this.loginBean.contentRating, "", null, getDomain(), false);
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Script>();
		}
	}

	public List<ScriptSource> getAllScriptVersions() {
		try {
			return AdminDatabase.instance().getAllScriptsVersions(getInstance());
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<ScriptSource>();
		}
	}

	@Override
	public String getAvatarThumb(AvatarImage avatar) {
		String file = super.getAvatarThumb(avatar);
		if (file.equals("images/bot-thumb.jpg")) {
			return "images/script-thumb.jpg";
		}
		return file;
	}

	@Override
	public String getAvatarImage(AvatarImage avatar) {
		String file = super.getAvatarImage(avatar);
		if (file.equals("images/bot.png")) {
			return "images/script.png";
		}
		return file;
	}

	public boolean createInstance(ScriptConfig config) {
		try {
			checkLogin();
			config.sanitize();
			Script newInstance = new Script(config.name);
			newInstance.setLanguage(config.language);
			setInstance(newInstance);
			updateFromConfig(newInstance, config);
			newInstance.setDomain(getDomain());
			checkVerfied(config);
			setSubdomain(config.subdomain, newInstance);
			//AdminDatabase.instance().validateNewScript(newInstance.getAlias(), config.description, config.tags, Site.ADULT, getDomain());
			setInstance(AdminDatabase.instance().createScript(newInstance, getUser(), config.categories, config.tags, this.loginBean));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Create a directory link.
	 */
	public boolean createLink(ScriptConfig config) {
		try {
			checkLogin();
			config.sanitize();
			Script newInstance = new Script(config.name);
			newInstance.setDomain(getDomain());
			newInstance.setLanguage(config.language);
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
				throw new BotException("You must enter a valid URL for an external script");
			}
			//AdminDatabase.instance().validateNewScript(newInstance.getAlias(), config.description, config.tags, config.isAdult, getDomain());			
			setInstance(AdminDatabase.instance().createScript(newInstance, getUser(), config.categories, config.tags, this.loginBean));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean updateScript(ScriptConfig config, String newdomain, Boolean featured, Boolean adVerified) {
		try {
			checkLogin();
			checkInstance();
			checkAdminOrSuper();
			config.sanitize();
			Script newInstance = (Script)this.instance.clone();
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
			newInstance.setLanguage(config.language);
			if (newInstance.getAdCode() == null || (config.adCode != null && !newInstance.getAdCode().equals(config.adCode))) {
				newInstance.setAdCodeVerified(false);
			}
			if (adVerified != null && isSuper()) {
				newInstance.setAdCodeVerified(adVerified);
			}
			if (isSuper() && featured != null) { 
				newInstance.setFeatured(featured);
			}
			setSubdomain(config.subdomain, newInstance);
			setInstance(AdminDatabase.instance().updateScript(newInstance, config.categories, config.tags));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	public boolean updateScriptSource(InputStream stream, String encoding, boolean version, String versionName) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			versionName= Utils.sanitize(versionName);
			String source = Utils.loadTextFile(stream, encoding, Site.MAX_UPLOAD_SIZE);
			if (source == null) {
				throw new BotException("Invalid source code");
			}
			setInstance(AdminDatabase.instance().updateScript(this.instance, source, version, versionName, getUser()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	public boolean updateScriptSource(String source, boolean version, String versionName) {
		try {
			versionName = Utils.sanitize(versionName);
			checkLogin();
			checkInstance();
			checkAdmin();
			setInstance(AdminDatabase.instance().updateScript(this.instance, source, version, versionName, getUser()));
		} catch (Exception exception) {
			error(exception);
			return false;
		}
		return true;
	}
	
	/**
	 * Download the script.
	 */
	public boolean downloadScriptSource(HttpServletResponse response) {
		try {
			checkInstance();
			response.setContentType("text/plain");
			response.setHeader("Content-disposition","attachment; filename=" + encodeURI(this.instance.getName()) + "." + this.instance.getExt());
			PrintWriter writer = response.getWriter();
			writer.write(this.instance.getSourceCode());
			//writer.flush();
		} catch (Exception exception) {
			error(exception);
			return false;
		}
		return true;
	}

	public void resetSearch() {
		super.resetSearch();
		this.languageFilter = "";
	}
	
	public String getLanguageFiler() {
		return languageFilter;
	}

	public void setLanguageFiler(String languageFilter) {
		this.languageFilter = languageFilter;
	}

	@Override
	public Class<Script> getType() {
		return Script.class;
	}
	
	@Override
	public String getTypeName() {
		return "Script";
	}

	@Override
	public String getCreateURL() {
		return "create-script.jsp";
	}

	@Override
	public String getSearchURL() {
		return "script-search.jsp";
	}

	@Override
	public String getBrowseURL() {
		return "browse-script.jsp";
	}
	
}

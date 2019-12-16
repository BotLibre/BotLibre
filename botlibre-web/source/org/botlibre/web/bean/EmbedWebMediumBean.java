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

import java.io.StringWriter;
import java.util.Map;

import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AccessMode;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.WebMedium;

public abstract class EmbedWebMediumBean extends ServletBean {
	
	protected String subdomain = "";
	protected String type = "link";
	protected String caption = "Chat Now";
	protected String userName = "";
	protected String token = "";
	protected String password = "";
	protected String css = "";
	protected String buttoncss = "";
	protected String customCss = "";
	protected String banner = "";
	protected String footer = "";
	protected String background = "#fff";
	protected String color = "#009900";
	protected String height = "";
	protected String width = "";
	protected String offset = "";
	protected String location = "bottom-right";
	protected String prompt = "You say";
	protected String send = "Send";
	protected String website = "";
	protected String language = "";
	protected boolean showAds = false;
	protected boolean loginBanner = false;
	protected boolean facebookLogin = false;
	protected boolean showTitle = true;
	protected boolean showLink = Site.BACKLINK;
	protected boolean promptContactInfo = false;
	protected boolean advancedInfo = false;
	protected boolean showMenubar = true;
	protected boolean boxMax = true;
	protected boolean chooseLanguage = true;
	protected boolean sendImage = true;
	protected boolean emailChatLog = true;
	protected boolean showChatLog = true;
	
	/** Used to secure execution of code. */
	protected String codeToken = "";
	protected String code = "";
	protected String displayCode = "";
	protected long instanceId;
	
	public EmbedWebMediumBean() {
	}
	
	public String getCodeToken() {
		return codeToken;
	}

	public void setCodeToken(String codeToken) {
		this.codeToken = codeToken;
	}
	
	public String generateCodeToken() {
		this.codeToken = String.valueOf(Utils.random().nextLong());
		return this.codeToken;
	}

	public void setAdvancedInfo(boolean advancedInfo) {
		this.advancedInfo = advancedInfo;
	}
	
	public boolean getAdvancedInfo() {
		return advancedInfo;
	}
	
	public boolean getShowChatLog() {
		return showChatLog;
	}

	public void setShowChatLog(boolean showChatLog) {
		this.showChatLog = showChatLog;
	}
	
	public void setBoxMax(boolean boxmax) {
		this.boxMax= boxmax;
	}
	public boolean getBoxmax() {
		return boxMax;
	}
	public void setChooseLanguage(boolean language) {
		this.chooseLanguage = language;
	}
	public boolean getChooseLanguage() {
		return chooseLanguage;
	}
	public void setSendImage(boolean sendImage) {
		this.sendImage = sendImage;
	}
	public boolean getSendImage() {
		return  sendImage;
	}
	public void setEmailChatLog(boolean emailChatLog) {
		this.emailChatLog = emailChatLog;
	}
	public boolean getEmailChatLog() {
		return emailChatLog; 
	}
	public String getOffset() {
		return offset;
	}
	public void setOffset(String offset) {
		this.offset = offset;
	}

	@SuppressWarnings("rawtypes")
	public abstract WebMediumBean getBean();
	
	public WebMedium getInstance() {
		return (WebMedium)getBean().getInstance();
	}
	
	public boolean getFacebookLogin() {
		return facebookLogin;
	}

	public void setFacebookLogin(boolean facebookLogin) {
		this.facebookLogin = facebookLogin;
	}

	public boolean getLoginBanner() {
		return loginBanner;
	}

	public void setLoginBanner(boolean loginBanner) {
		this.loginBanner = loginBanner;
	}

	public boolean getShowTitle() {
		return showTitle;
	}

	public void setShowTitle(boolean showTitle) {
		this.showTitle = showTitle;
	}

	public boolean getShowLink() {
		return showLink;
	}

	public void setShowLink(boolean showLink) {
		this.showLink = showLink;
	}

	public boolean getPromptContactInfo() {
		return promptContactInfo;
	}

	public void setPromptContactInfo(boolean promptContactInfo) {
		this.promptContactInfo = promptContactInfo;
	}

	public String getBanner() {
		return banner;
	}

	public void setBanner(String banner) {
		this.banner = banner;
	}

	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public boolean getShowAds() {
		return showAds;
	}

	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public void setShowAds(boolean showAds) {
		this.showAds = showAds;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getWebsite() {
		if (this.website.isEmpty() && getBotBean().getInstance() != null) {
			this.website = getBotBean().getInstance().getWebsite();
		}
		return website;
	}

	public void setWebsite(String website) {
		this.website = Utils.checkURL(website);
	}

	public String getSend() {
		return send;
	}

	public void setSend(String send) {
		this.send = send;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getCustomCss() {
		return customCss;
	}

	public void setCustomCss(String customcss) {
		this.customCss = customcss;
	}
	
	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}
	
	public void setShowMenubar(boolean menubar) {
		this.showMenubar = menubar;
	}
	
	public boolean getShowMenubar() {
		return showMenubar;
	}

	public String getCode() {
		if (getInstance() != null && (this.code.isEmpty() || getInstance().getId() != this.instanceId)) {
			this.disconnect();
			this.instanceId = getInstance().getId();
			if (getInstance() != null && (getInstance().isPrivate() || getInstance().getAccessMode() != AccessMode.Everyone)) {
				if (this.userName == null || this.userName.isEmpty()) {
					this.userName = getUserId();
					this.token = String.valueOf(getUser().getToken());
				}
			}
			if (getBean().isAdmin()) {
				this.subdomain = getInstance().getSubdomain();
				if (getInstance().getDomainForwarder() != null) {
					Map<String, String> properties = getLoginBean().parseProperties(getInstance().getDomainForwarder().getForwarderAddress());
					setProperties(properties);
				}
			}
			generateCode(); 
		}
		return code;
	}
	
	public void setProperties(Map<String, String> properties) {
		if (properties.containsKey("css")) {
			this.css = properties.get("css");
		}
		if (properties.containsKey("buttoncss")) {
			this.buttoncss = properties.get("buttoncss");
		}
		if (properties.containsKey("banner")) {
			this.banner = properties.get("banner");
		}
		if (properties.containsKey("footer")) {
			this.footer = properties.get("footer");
		}
		if (properties.containsKey("background")) {
			this.background = properties.get("background");
		}
		if (properties.containsKey("prompt")) {
			this.prompt = properties.get("prompt");
		}
		if (properties.containsKey("showAds")) {
			this.showAds = properties.get("showAds").equals("true");
		}
		if (properties.containsKey("send")) {
			this.send = properties.get("send");
		}
		if (properties.containsKey("loginBanner")) {
			this.loginBanner = properties.get("loginBanner").equals("true");
		}
		if (properties.containsKey("facebookLogin")) {
			this.facebookLogin = properties.get("facebookLogin").equals("true");
		}
		if (properties.containsKey("showTitle")) {
			this.showTitle = properties.get("showTitle").equals("true");
		}
		if (properties.containsKey("showLink")) {
			this.showLink = properties.get("showLink").equals("true");
		}
	}

	public void setCode(String code) {
		if (code == null) {
			code = "";
		}
		this.code = code;
		this.displayCode = this.code.replace("<", "&lt;").replace(">", "&gt;");
	}
	
	public String getDisplayCode() {
		return displayCode;
	}

	public void setDisplayCode(String displayCode) {
		this.displayCode = displayCode;
	}

	public String getTypeSelectedString(String type) {
		if (type.equals(this.type)) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getCssSelectedString(String css) {
		if (css.equals(this.css)) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getLanguageSelectedString(String language) {
		if (language.equals(this.language)) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getButtonCssSelectedString(String buttoncss) {
		if (buttoncss.equals(this.buttoncss)) {
			return "selected=\"selected\"";
		}
		return "";
	}

	public String getLocationSelectedString(String location) {
		if (location.equals(this.location)) {
			return "selected=\"selected\"";
		}
		return "";
	}

	public void writeURL(StringWriter writer) {
		if (isLoggedIn() && getUser().getApplicationId() == null) {
			getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
		}
		
		if (this.subdomain != null && !this.subdomain.isEmpty()) {
			if (Site.HTTPS_WILDCARD) {
				writer.write("https://");
			} else {
				writer.write("http://");
			}
			if (this.subdomain.indexOf('.') == -1) {
				writer.write(this.subdomain);
				writer.write(".");
				writer.write(Site.SERVER_NAME);
				writer.write(Site.URL_SUFFIX);
			} else {
				writer.write(this.subdomain);
			}
		} else {
			writer.write(Site.SECUREURLLINK);
			writer.write(getEmbedURL(true));
		}
	}
	
	public abstract String getEmbedURL(boolean embedded);

	public void generateCode() {
		int height = 400;
		StringWriter writer = new StringWriter();
		if (type.equals("link")) {
			writer.write("<script>\n");
			writer.write("function popupwindow(url, title, w, h) {\n");
			writer.write("\tvar left = (screen.width/2)-(w/2);\n");
			writer.write("\tvar top = 100;\n");
			writer.write("\twindow.open(url, title, 'scrollbars=yes, resizable=yes, toolbar=no, location=no, directories=no, status=no, menubar=no, copyhistory=no, width='+w+', height='+h+', top='+top+', left='+left);\n");
			writer.write("\treturn false;\n");
			writer.write("}\n");
			writer.write("</script>\n");
			writer.write("<a href=\"chat\"");
			writer.write("onclick=\"popupwindow('");
			writeURL(writer);
			writer.write("', 'child', 600, " + height + "); return false;\">");
			writer.write(this.caption);
			writer.write("</a>\n");
		} else if (type.equals("button")) {
			writer.write("<script>\n");
			writer.write("function popupwindow(url, title, w, h) {\n");
			writer.write("\tvar left = (screen.width/2)-(w/2);\n");
			writer.write("\tvar top = 100;\n");
			writer.write("\twindow.open(url, title, 'scrollbars=yes, resizable=yes, toolbar=no, location=no, directories=no, status=no, menubar=no, copyhistory=no, width='+w+', height='+h+', top='+top+', left='+left);\n");
			writer.write("\treturn false;\n");
			writer.write("}\n");
			writer.write("</script>\n");
			writer.write("<form onsubmit=\"popupwindow('");
			writeURL(writer);
			writer.write("', 'child', 600, " + height + "); return false;\">\n");
			writer.write("<input style=\"color:#fff;background-color:#009900;cursor: pointer;font: 13px/30px Arial, Helvetica, sans-serif;");
			writer.write("height: 40px;border: 0;margin: 10px 10px 0 0;font-weight:bold;\" type=\"submit\" name=\"chat\" value=\"");
			writer.write(this.caption);
			writer.write("\">\n");
			writer.write("</form>\n");
		} else if (type.equals("bar")) {
			writer.write("<script>\n");
			writer.write("function popupwindow(url, title, w, h) {\n");
			writer.write("\tvar left = screen.width-w-10;\n");
			writer.write("\tvar top = screen.height-h-100;\n");
			writer.write("\twindow.open(url, title, 'scrollbars=yes, resizable=yes, toolbar=no, location=no, directories=no, status=no, menubar=no, copyhistory=no, width='+w+', height='+h+', top='+top+', left='+left);\n");
			writer.write("\treturn false;\n");
			writer.write("}\n");
			writer.write("</script>\n");
			writer.write("<form onsubmit=\"popupwindow('");
			writeURL(writer);
			writer.write("', 'child', 600, " + height + "); return false;\">\n");
			writer.write("<input style=\"color:#fff;background-color:#009900;cursor: pointer;font: 13px/30px Arial, Helvetica, sans-serif;");
			writer.write("border:0;outline:0;margin:10px 10px 0 0;font-weight:bold;position:fixed;bottom:0px;right:10px;z-index:52;\" type=\"submit\" name=\"chat\" value=\"");
			writer.write(this.caption);
			writer.write("\">\n");
			writer.write("</form>\n");
		} else if (type.equals("bubble")) {
			writer.write("<script>\n");
			writer.write("function popupwindow(url, title, w, h) {\n");
			writer.write("\tvar left = screen.width-w-10;\n");
			writer.write("\tvar top = screen.height-h-100;\n");
			writer.write("\twindow.open(url, title, 'scrollbars=yes, resizable=yes, toolbar=no, location=no, directories=no, status=no, menubar=no, copyhistory=no, width='+w+', height='+h+', top='+top+', left='+left);\n");
			writer.write("\treturn false;\n");
			writer.write("}\n");
			writer.write("function hideChatNow() {\n");
			writer.write("\tdocument.getElementById('chatnowbubble').style.visibility = 'hidden';\n");
			writer.write("\treturn false;\n");
			writer.write("}\n");
			writer.write("</script>\n");
			writer.write("<div id=\"chatnowbubble\" style=\"background-image: url(" + Site.SECUREURLLINK + "/images/chatnow.png);background-repeat: no-repeat;background-size:200px 145px;width: 200px;");
			writer.write("height: 145px;border: 0px;outline: 0;background-color: transparent;margin: 0 0 0 0;position:fixed;bottom:10px;right:10px;z-index:52;text-align:center;\">\n");
			writer.write("\t<a style=\"color:#000;font-size:10px;cursor: pointer;text-decoration: none;float:right;margin:6px 12px 0px 0px;\" onclick=\"hideChatNow();return false;\">X</a><br/>\n");
			writer.write("\t<br/>\n");
			writer.write("\t<a style=\"color:white;font-size:20px;cursor: pointer;text-decoration: none;\" onclick=\"popupwindow('");
			writeURL(writer);
			writer.write("','child', 600, " + height + ");return false;\">");
			writer.write(this.caption);
			writer.write("</a>\n");
			writer.write("</div>\n");
		} else if (type.equals("frame")) {
			writer.write("<iframe src=\"");
			writeURL(writer);
			writer.write("\" width=\"620\" height=\"" + height + "\" frameborder=\"0\" scrolling=\"auto\"></iframe>");
		}
		
		setCode(writer.toString());
	}

	@Override
	public void disconnect() {
		this.userName = "";
		this.token = "";
		this.password = "";
		this.banner = "";
		this.footer = "";
		this.showAds = false;
		this.subdomain = "";
		this.type = "link";
		this.caption = "Chat Now";
		this.css = "";
		this.customCss = "";
		this.background = "#fff";
		this.color = "#009900";
		this.height = "";
		this.width = "";
		this.offset = "";
		this.location = "bottom-right";
		this.prompt = "You say";
		this.send = "Send";
		this.website = "";
		this.loginBanner = false;
		this.facebookLogin = false;
		this.showTitle = true;
		this.showLink = Site.BACKLINK;
		this.promptContactInfo = false;
		this.showChatLog = true;

		this.code = "";
		this.displayCode = "";
	}
}

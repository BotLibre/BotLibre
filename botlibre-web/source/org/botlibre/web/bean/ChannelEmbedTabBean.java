/******************************************************************************
 *
 *  Copyright 2013-2021 Paphus Solutions Inc.
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

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.User.UserType;

public class ChannelEmbedTabBean extends EmbedWebMediumBean {	
	protected String landing = "chat";
	protected boolean bubble = false;
	protected boolean online = true;
	protected boolean chatLog = true;
	protected boolean avatar = false;
	
	public ChannelEmbedTabBean() {
		this.type = "box";
		this.promptContactInfo = true;
	}
	
	public boolean getOnline() {
		return online;
	}
	
	public void setOnline(boolean online) {
		this.online = online;
	}
	
	public boolean getBubble() {
		return bubble;
	}

	public void setBubble(boolean bubble) {
		this.bubble = bubble;
	}

	@Override
	public LiveChatBean getBean() {
		return getLoginBean().getBean(LiveChatBean.class);
	}

	public String getLanding() {
		return landing;
	}

	public void setLanding(String landing) {
		this.landing = landing;
	}

	public String getLandingSelectedString(String landing) {
		if (landing.equals(this.landing)) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public boolean getAvatar() {
		return avatar;
	}
	
	public void setAvatar(boolean avatar) {
		this.avatar = avatar;
	}

	public void generateCode(String subdomain, String type, String caption, String landing, String userName, String password, String token,
				String css, String customcss, String buttoncss, String banner, String footer, String color, String background,
				String width, String height, String offset, String location, String language, boolean chatLog, boolean online, boolean bubble,
				boolean showAds, boolean loginBanner, String prompt, String send, boolean facebookLogin, boolean showTitle, boolean showLink,
				boolean promptContactInfo, boolean showAdvancedInfo, boolean showMenubar, boolean showBoxmax, boolean showSendImage,
				boolean showEmailChatLog, boolean avatar) {
		try {
			if (send == null) {
				send = "";
			}
			this.type = Utils.sanitize(type.trim());
			this.caption = Utils.sanitize(caption.trim());
			this.landing = Utils.sanitize(landing);
			if (userName == null) {
				this.userName = "";
				this.token = "";
				this.password = "";
			} else {
				this.userName = Utils.sanitize(userName);
				this.token = Utils.sanitize(token);
				this.password = Utils.sanitize(password);
			}
			this.prompt = Utils.sanitize(prompt.trim());
			this.send = Utils.sanitize(send.trim());
			this.loginBanner = loginBanner;
			this.facebookLogin = facebookLogin;
			this.showTitle = showTitle;
			this.css = Utils.sanitize(css.trim());
			if (customcss != null) {
				this.customCss = Utils.sanitize(customcss.trim());
			} else {
				this.customCss = "";
			}
			this.buttoncss = Utils.sanitize(buttoncss.trim());
			this.banner = Utils.sanitize(banner.trim());
			this.footer = Utils.sanitize(footer.trim());
			this.color = Utils.sanitize(color.trim());
			this.background = Utils.sanitize(background.trim());
			this.width = Utils.sanitize(width.trim());
			this.height = Utils.sanitize(height.trim());
			this.offset = Utils.sanitize(offset.trim());
			this.location = Utils.sanitize(location.trim());
			this.showChatLog = chatLog;
			this.avatar = avatar;
			this.online = online;
			this.bubble = bubble;
			this.promptContactInfo = promptContactInfo;
			this.showAds = showAds;
			this.advancedInfo = showAdvancedInfo;
			this.showMenubar = showMenubar;
			this.boxMax = showBoxmax;
			this.sendImage = showSendImage;
			this.emailChatLog = showEmailChatLog;
			this.language = Utils.sanitize(language);
			if (!Site.COMMERCIAL && !showLink && (getUser() == null
						|| getUser().getType() == UserType.Basic
						|| getUser().getType() == UserType.Bronze
						|| getUser().getType() == UserType.Gold)) {
				throw new BotException("You must upgrade to Platinum to remove the backlink, or use Bot Libre for Business");
			}
			if (Site.BACKLINK) {
				this.showLink = showLink;
			}
			if (subdomain != null && !subdomain.isEmpty() && getLoginBean().getBean(LiveChatBean.class).isAdmin()) {
				this.subdomain = Utils.sanitize(subdomain);
				getBean().setSubdomain(subdomain, this);
			} else {
				this.subdomain = "";
			}
			
			generateCode();
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public String getEmbedURL(boolean embedded) {
		if (isLoggedIn() && getUser().getApplicationId() == null) {
			getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
		}
		
		StringWriter writer = new StringWriter();
		writer.write("/livechat?");
		if (embedded) {
			writer.write("embedded=true");
		}
		writer.write("&id=");
		writer.write(String.valueOf(getLoginBean().getBean(LiveChatBean.class).getInstance().getId()));
		if (this.landing.equals("chat")) {
			writer.write("&chat=true");
		}
		if (!this.userName.isEmpty()) {
			writer.write("&user=");
			writer.write(encodeURI(this.userName));
		}
		if (!this.token.isEmpty()) {
			writer.write("&token=");
			writer.write(this.token);
		}
		if (!this.password.isEmpty()) {
			writer.write("&password=");
			writer.write(Utils.encrypt(Utils.KEY, this.password));
		}
		if(this.bubble) {
			writer.write("&bubble=true");
		} else {
			writer.write("&bubble=false");
		}
		if (isLoggedIn() && getUser().getApplicationId() != null) {
			writer.write("&application=" + getUser().getApplicationId());
		}
		if (!this.loginBanner) {
			writer.write("&loginBanner=false");
		}
		if (!this.facebookLogin) {
			writer.write("&facebookLogin=false");
		}
		if (!this.showTitle) {
			writer.write("&showTitle=false");
		}
		if (!this.showLink) {
			writer.write("&showLink=false");
		}
		if (this.showAds) {
			writer.write("&showAds=true");
		}
		if (this.showChatLog) {
			writer.write("&chatLog=true");
		} else {
			writer.write("&chatLog=false");
		}
		if (this.showMenubar) {
			writer.write("&menubar=true");
		} else {
			writer.write("&menubar=false");
		}
		if (this.sendImage) {
			writer.write("&sendImage=true");
		} else {
			writer.write("&sendImage=false");
		}
		if (this.avatar) {
			writer.write("&avatar=true");
		}
		writer.write("&background=");
		writer.write(encodeURI(this.background));
		writer.write("&prompt=");
		writer.write(encodeURI(this.prompt));
		writer.write("&send=");
		writer.write(encodeURI(this.send));
		if (!this.css.isEmpty()) {
			writer.write("&css=");
			if (this.css.equals("chatlog")) {
				setCss("chatlog");
				setCustomCss(Site.SECUREURLLINK + "/css/chatlog.css");
				writer.write(Site.SECUREURLLINK + "/css/chatlog.css");
			} else if (this.css.equals("social_chat")) {
				setCss("social_chat");
				setCustomCss(Site.SECUREURLLINK + "/css/social_chat.css");
				writer.write(Site.SECUREURLLINK + "/css/social_chat.css");
			} else if (this.css.equals("chatroom")) {
				setCss("chatroom");
				setCustomCss(Site.SECUREURLLINK + "/css/chatroom.css");
				writer.write(Site.SECUREURLLINK + "/css/chatroom.css");
			} else if (this.css.equals("blue_chat")) {
				setCss("blue_chat");
				setCustomCss(Site.SECUREURLLINK + "/css/blue_chat.css");
				writer.write(Site.SECUREURLLINK + "/css/blue_chat.css");
			} else if (this.css.equals("pink_chat")) {
				setCss("pink_chat");
				setCustomCss(Site.SECUREURLLINK + "/css/pink_chat.css");
				writer.write(Site.SECUREURLLINK + "/css/pink_chat.css");
			} else if (this.css.equals("custom_chat")) {
				setCss("custom_chat");
				if (this.customCss != "") {
					if (this.customCss.startsWith("http") || this.customCss.startsWith("https")) {
						writer.write(this.customCss);
					} else {
						setCustomCss("");
						throw new BotException("Invalid URL");
					}
				}
			}
		} else {
			setCss("chatlog");
			setCustomCss(Site.SECUREURLLINK + "/css/chatlog.css");
			writer.write("&css=");
			writer.write(Site.SECUREURLLINK + "/css/chatlog.css");
		}
		if (!this.banner.isEmpty()) {
			writer.write("&banner=");
			writer.write(encodeURI(this.banner));
		}
		if (!this.footer.isEmpty()) {
			writer.write("&footer=");
			writer.write(encodeURI(this.footer));
		}
		if (!this.language.isEmpty()) {
			writer.write("&language=");
			writer.write(encodeURI(this.language));
			writer.write("&translate=");
			writer.write(encodeURI(this.language));
		}
		return writer.toString();
	}

	public void generateCode() {
		int height = 500;
		if (!this.avatar && !this.showChatLog) {
			height = 260;
		} else if (this.loginBanner) {
			height = 600;
		}
		if (!this.height.isEmpty()) {
			height = Integer.parseInt(this.height);
		}
		int width = 700;
		if (!this.width.isEmpty()) {
			width = Integer.parseInt(this.width);
		}
		String colorStyle = "";
		if (!this.color.trim().isEmpty()) {
			colorStyle = "background-color:" + this.color;
		}
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
			writer.write("<a href=\"chat\" ");
			writer.write("onclick=\"popupwindow('");
			writeURL(writer);
			writer.write("', 'child', 700, " + height + "); return false;\">");
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
			writer.write("', 'child', 700, " + height + "); return false;\">\n");
			writer.write("<input style=\"color:#fff;" + colorStyle + ";cursor: pointer;font: 13px/30px Arial, Helvetica, sans-serif;");
			writer.write("height: 40px;border: 0;margin: 10px 10px 0 0;font-weight:bold;\" type=\"submit\" name=\"chat\" value=\"");
			writer.write(this.caption);
			writer.write("\">\n");
			writer.write("</form>\n");
		} else if (type.equals("box")) {
			if (isLoggedIn() && getUser().getApplicationId() == null) {
				getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
			}
			writer.write("<script type='text/javascript' src='" + Site.SECUREURLLINK + "/scripts/sdk.js'></script>\n");
			writer.write("<script type='text/javascript'>\n");
			if (isLoggedIn() && getUser().getApplicationId() != null) {
				writer.write("SDK.applicationId = \"" + getUser().getApplicationId() + "\";\n");
			}
			if (!this.language.trim().isEmpty()) {
				writer.write("SDK.lang = \"" + this.language + "\";\n");
			}
			writer.write("var sdk = new SDKConnection();\n");
			if (this.userName != null && !this.userName.isEmpty()) {
				writer.write("var user = new UserConfig();\n");
				writer.write("user.user = \"" + this.userName + "\";\n");
				if (this.token != null && !this.token.isEmpty()) {
					writer.write("user.token = \"" + this.token + "\";\n");
				} else if (this.password != null && !this.password.isEmpty()) {
					writer.write("user.password = \"" + Utils.encrypt(Utils.KEY, this.password) + "\";\n");
				}
				writer.write("sdk.user = user;\n");
			}
			writer.write("var livechat = new WebLiveChatListener();\n");
			writer.write("livechat.sdk = sdk;\n");
			writer.write("livechat.instance = \"" + getBean().getInstanceId() + "\";\n");
			writer.write("livechat.instanceName = \"" + getBean().getInstanceName() + "\";\n");
			writer.write("livechat.prefix = \"" + Site.PREFIX + "chat\";\n");
			writer.write("livechat.caption = \"" + this.caption + "\";\n");
			writer.write("livechat.chatLogType = \"log\";\n");
			if (!this.height.isEmpty()) {
				writer.write("livechat.height = \"" + this.height + "\";\n");				
			}
			if (!this.width.isEmpty()) {
				writer.write("livechat.width = \"" + this.width + "\";\n");				
			}
			if (!this.offset.isEmpty()) {
				writer.write("livechat.offset = \"" + this.offset + "\";\n");				
			}
			if (!this.location.isEmpty()) {
				writer.write("livechat.boxLocation = \"" + this.location + "\";\n");				
			}
			if (!this.color.trim().isEmpty()) {
				writer.write("livechat.color = \"" + this.color + "\";\n");				
			}
			if (!this.background.trim().isEmpty()) {
				writer.write("livechat.background = \"" + this.background + "\";\n");
			}
			if(!this.css.trim().isEmpty()) {
				if (this.css.equals("chatlog")) {
					writer.write("livechat.css = \"" + Site.SECUREURLLINK + "/css/chatlog.css" + "\";\n");
				} else if (this.css.equals("social_chat")) {
					writer.write("livechat.css = \"" + Site.SECUREURLLINK  + "/css/social_chat.css" + "\";\n");
				} else if (this.css.equals("chatroom")) {
					writer.write("livechat.css = \"" + Site.SECUREURLLINK + "/css/chatroom.css" + "\";\n");
				} else if (this.css.equals("blue_chat")) {
					writer.write("livechat.css = \"" + Site.SECUREURLLINK + "/css/blue_chat.css" +"\";\n");
				} else if (this.css.equals("pink_chat")) {
					writer.write("livechat.css = \"" + Site.SECUREURLLINK + "/css/pink_chat.css" + "\";\n");
				} else if (this.css.equals("custom_chat")) {
					if (this.customCss != "" && this.customCss.startsWith("http") || this.customCss.startsWith("https")) {
						writer.write("livechat.css = \"" + this.customCss +  "\";\n");
					}
				} 
			} else {
				writer.write("livechat.css = \"" + Site.SECUREURLLINK + "/css/chatlog.css" + "\";\n");
			}
			if (this.buttoncss.equals("blue_round_button")) {
				writer.write("livechat.buttoncss ='" + Site.SECUREURLLINK + "/css/blue_round_button.css'; \n");
			} else if (this.buttoncss.equals("red_round_button")) {
				writer.write("livechat.buttoncss = '" + Site.SECUREURLLINK + "/css/red_round_button.css'; \n");
			} else if (this.buttoncss.equals("green_round_button")) {
				writer.write("livechat.buttoncss = '" + Site.SECUREURLLINK + "/css/green_round_button.css'; \n");
			} else if (this.buttoncss.equals("blue_bot_button")) {
				writer.write("livechat.buttoncss = '" + Site.SECUREURLLINK + "/css/blue_bot_button.css'; \n");
			} else if (this.buttoncss.equals("red_bot_button")) {
				writer.write("livechat.buttoncss = '" + Site.SECUREURLLINK + "/css/red_bot_button.css'; \n");
			} else if (this.buttoncss.equals("green_bot_button")) {
				writer.write("livechat.buttoncss = '" + Site.SECUREURLLINK + "/css/green_bot_button.css'; \n");
			} else if (this.buttoncss.equals("purple_chat_button")) {
				writer.write("livechat.buttoncss = '" + Site.SECUREURLLINK + "/css/purple_chat_button.css'; \n");
			} else if (this.buttoncss.equals("red_chat_button")) {
				writer.write("livechat.buttoncss = '" + Site.SECUREURLLINK + "/css/red_chat_button.css'; \n");
			} else if (this.buttoncss.equals("green_chat_button")) {
				writer.write("livechat.buttoncss = '" + Site.SECUREURLLINK + "/css/green_chat_button.css'; \n");
			} else if (this.buttoncss.equals("square_chat_button")) {
				writer.write("livechat.buttoncss = '" + Site.SECUREURLLINK + "/css/square_chat_button.css'; \n");
			} else if (this.buttoncss.equals("round_chat_button")) {
				writer.write("livechat.buttoncss = '" + Site.SECUREURLLINK + "/css/round_chat_button.css'; \n");
			} else {
				writer.write("livechat.buttoncss = '" + Site.SECUREURLLINK + "/css/blue_round_button.css'; \n");
			}
			writer.write("livechat.version = 8.5;\n");
			if (this.bubble) {
				writer.write("livechat.bubble = true;\n");
			} else {
				writer.write("livechat.bubble = false;\n");
			}
			if (Site.BACKLINK) {
				writer.write("livechat.backlink = " + this.showLink + ";\n");
			}
			if (this.showChatLog) {
				writer.write("livechat.chatLog = true;\n");
			} else {
				writer.write("livechat.chatLog = false;\n");
			}
			if (this.promptContactInfo) {
				writer.write("livechat.promptContactInfo = true;\n");
			}
			if (this.showMenubar) {
				writer.write("livechat.showMenubar = true;\n");
			} else {
				writer.write("livechat.showMenubar = false;\n");
			}
			if (this.boxMax) {
				writer.write("livechat.showBoxmax = true;\n");
			} else {
				writer.write("livechat.showBoxmax = false;\n");
			}
			if (this.sendImage) {
				writer.write("livechat.showSendImage = true;\n");
			} else {
				writer.write("livechat.showSendImage = false;\n");
			}
			if (this.emailChatLog) {
				writer.write("livechat.emailChatLog = true;\n");
			} else {
				writer.write("livechat.emailChatLog = false;\n");
			}
			if (this.online) {
				writer.write("livechat.online = true;\n");
				writer.write("livechat.linkUsers = false;\n");
			} else {
				writer.write("livechat.online = false;\n");
			}
			if (!getBean().getInstance().getEmailAddress().isEmpty()) {
				writer.write("livechat.emailChatLog = true;\n");
				writer.write("livechat.promptEmailChatLog = true;\n");
			}
			if (getBean().getInstance().isChatRoom()) {
				writer.write("livechat.chatroom = true;\n");
			}		
			if (this.avatar) {
				writer.write("livechat.avatar = true;\n");
			}
			if (this.showChatLog) {
				writer.write("livechat.chatLog = true;\n");
			} 
			else {
				writer.write("livechat.chatLog = false;\n");
			}
			writer.write("livechat.popupURL = \"");
			writeURL(writer);
			writer.write("\";\n");
			writer.write("livechat.createBox();\n");
			writer.write("</script>\n");
		} else if (type.equals("frame")) {
			writer.write("<iframe src=\"");
			writeURL(writer);
			//writer.write("\" width=\"700\" height=\"" + height + "\" frameborder=\"0\" scrolling=\"auto\"></iframe>");
			//writer.write("\" width=\"" + width + "\" height=\"" + height + "\" frameborder=\"0\" scrolling=\"auto\"></iframe>");
			if (!this.avatar && this.showChatLog) {
				writer.write("\" width=\"" + width + "\" height=\"" + height + "\" frameborder=\"0\" overflow-x=\"hidden\" scrolling=\"hidden\"></iframe>");
			} else if (!this.avatar && !this.showChatLog) {
				writer.write("\" width=\"" + width + "\" height=\"" + height + "\" frameborder=\"0\" scrolling=\"hidden\"></iframe>");
			} else {
				writer.write("\" width=\"" + width + "\" height=\"" + height + "\" frameborder=\"0\" scrolling=\"auto\"></iframe>");
			}
		}
		setCode(writer.toString());
	}

	@Override
	public void disconnect() {
		super.disconnect();
		this.type = "box";
		this.landing = "chat";
		this.bubble = true;
		this.online = true;
		this.promptContactInfo = true;
		this.advancedInfo = false;
		this.showMenubar = true;
		this.boxMax = true;
		this.sendImage = true;
		this.emailChatLog = true;
		this.showChatLog = true;
		this.language = "";
		this.avatar = false;
	}
}

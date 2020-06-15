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

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.User.UserType;

public class EmbedTabBean extends EmbedWebMediumBean {
	protected String greeting = "";
	protected String farewell = "";
	protected boolean avatar = true;
	protected boolean bubble = true;
	protected boolean speak = true;
	protected boolean allowSpeech = true;
	protected boolean allowEmotes = false;
	protected boolean allowCorrection = false;
	protected boolean avatarExpandable = false;
	protected boolean staticHTML = false;
	
	public EmbedTabBean() {
		this.type = "box";
		this.loginBanner = false;
	}
	
	public void setProperties(Map<String, String> properties) {
		super.setProperties(properties);
		if (properties.containsKey("greeting")) {
			this.greeting = properties.get("greeting");
		}
		if (properties.containsKey("farewell")) {
			this.farewell = properties.get("farewell");
			
		}
		if (properties.containsKey("avatar")) {
			this.avatar = properties.get("avatar").equals("true");
		}
		if (properties.containsKey("speak")) {
			this.speak = properties.get("speak").equals("true");
			this.allowSpeech = !properties.get("speak").equals("disable");
		}
		if (properties.containsKey("allowEmotes")) {
			this.allowEmotes = properties.get("allowEmotes").equals("true");
		}
		if (properties.containsKey("allowCorrection")) {
			this.allowCorrection = properties.get("allowCorrection").equals("true");
		}
		if (properties.containsKey("avatarExpandable")) {
			this.avatarExpandable = properties.get("avatarExpandable").equals("true");
		}
		if (properties.containsKey("static")) {
			this.staticHTML = properties.get("static").equals("true");
		}
	}

	@Override
	public BotBean getBean() {
		return getBotBean();
	}

	public boolean getAllowEmotes() {
		return allowEmotes;
	}

	public boolean getBubble() {
		return bubble;
	}

	public void setBubble(boolean bubble) {
		this.bubble = bubble;
	}

	public void setAllowEmotes(boolean allowEmotes) {
		this.allowEmotes = allowEmotes;
	}

	public boolean getAllowCorrection() {
		return allowCorrection;
	}

	public void setAllowCorrection(boolean allowCorrection) {
		this.allowCorrection = allowCorrection;
	}

	public boolean getAvatarExpandable() {
		return avatarExpandable;
	}

	public void setAvatarExpandable(boolean avatarExpandable) {
		this.avatarExpandable = avatarExpandable;
	}

	public boolean getStaticHTML() {
		return staticHTML;
	}

	public void setStaticHTML(boolean staticHTML) {
		this.staticHTML = staticHTML;
	}

	public boolean getAllowSpeech() {
		return allowSpeech;
	}

	public void setAllowSpeech(boolean allowSpeech) {
		this.allowSpeech = allowSpeech;
	}

	public String getGreeting() {
		return greeting;
	}

	public void setGreeting(String greeting) {
		this.greeting = greeting;
	}

	public String getFarewell() {
		return farewell;
	}

	public void setFarewell(String farewell) {
		this.farewell = farewell;
	}

	public boolean getAvatar() {
		return avatar;
	}

	public void setAvatar(boolean avatar) {
		this.avatar = avatar;
	}

	public boolean getSpeak() {
		return speak;
	}

	public void setSpeak(boolean speak) {
		this.speak = speak;
	}

	public void generateCode(String subdomain, String type, String caption, String greeting, String farewell, String prompt, String send,
				String userName, String password, String token, String css, String customcss, String buttoncss, String banner, String footer, String color, String background,
				String width, String height, String offset, String location, boolean chatLog, String language,
				boolean allowEmotes, boolean allowCorrection, boolean loginBanner, boolean avatarExpandable, boolean staticHTML, boolean showAds, 
				boolean avatar, boolean bubble, boolean speak, boolean allowSpeech, boolean facebookLogin, boolean showTitle, boolean showLink,
				boolean promptContactInfo, boolean showAdvancedInfo, boolean showMenubar, boolean showBoxmax, boolean showChooseLanguage,
				boolean showSendImage) {
		try {
			if (send == null) {
				send = "";
			}
			this.type = Utils.sanitize(type.trim());
			this.caption = Utils.sanitize(caption.trim());
			this.greeting = Utils.sanitize(greeting.trim());
			this.prompt = Utils.sanitize(prompt.trim());
			this.send = Utils.sanitize(send.trim());
			this.farewell = Utils.sanitize(farewell.trim());
			if (userName == null) {
				this.userName = "";
				this.token = "";
				this.password = "";
			} else {
				this.userName = Utils.sanitize(userName);
				this.token = Utils.sanitize(token);
				this.password = password;
			}
			this.css = Utils.sanitize(css.trim());
			if (customcss != null) {
				this.customCss = Utils.sanitize(customcss.trim());
			} else {
				this.customCss = "";
			}
			this.buttoncss  = Utils.sanitize(buttoncss.trim());
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
			this.bubble = bubble;
			this.speak = speak;
			this.promptContactInfo = promptContactInfo;
			this.allowSpeech = allowSpeech;
			this.allowEmotes = allowEmotes;
			this.allowCorrection = allowCorrection;
			this.loginBanner = loginBanner;
			this.facebookLogin = facebookLogin;
			this.showTitle = showTitle;
			this.avatarExpandable = avatarExpandable;
			this.staticHTML = staticHTML;
			this.showAds = showAds;
			this.advancedInfo = showAdvancedInfo;
			this.showMenubar = showMenubar;
			this.boxMax = showBoxmax;
			this.chooseLanguage = showChooseLanguage;
			this.sendImage = showSendImage;
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
			if (subdomain != null && !subdomain.isEmpty() && getBotBean().isAdmin()) {
				this.subdomain = Utils.sanitize(subdomain);
				getBotBean().setSubdomain(subdomain, this);
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
		writer.write("/chat?");
		writer.write("&id=");
		writer.write(String.valueOf(getBotBean().getInstanceId()));
		if (embedded || this.staticHTML) {
			writer.write("&embedded=true");
		}
		if (!this.avatar) {
			writer.write("&avatar=false");
		}
		if (this.showChatLog) {
			writer.write("&chatLog=true");
		} else {
			writer.write("&chatLog=false");
		}
		if (!this.allowSpeech) {
			writer.write("&speak=disable");
		} else {
			if (!this.speak) {
				writer.write("&speak=false");
			}
		}
		if (this.staticHTML) {
			writer.write("&static=true");
			writer.write("&chat=true");
		}
		if (this.avatarExpandable) {
			writer.write("&avatarExpandable=true");
		}
		if (this.allowCorrection) {
			writer.write("&allowCorrection=true");
		}
		if (this.allowEmotes) {
			writer.write("&allowEmotes=true");
		}
		if (this.loginBanner) {
			writer.write("&loginBanner=true");
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
		if (isLoggedIn() && getUser().getApplicationId() != null) {
			writer.write("&application=" + getUser().getApplicationId());
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
		if(this.showMenubar) {
			writer.write("&menubar=true");
		} else {
			writer.write("&menubar=false");
		}
		if(this.chooseLanguage) {
			writer.write("&chooseLanguage=true");
		} else {
			writer.write("&chooseLanguage=false");
		}
		if(this.sendImage) {
			writer.write("&sendImage=true");
		} else {
			writer.write("&sendImage=false");
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
		if (!this.greeting.isEmpty()) {
			writer.write("&greeting=");
			writer.write(encodeURI(this.greeting));
		}
		if (!this.farewell.isEmpty()) {
			writer.write("&farewell=");
			writer.write(encodeURI(this.farewell));
		}
		if (!this.language.isEmpty()) {
			writer.write("&language=");
			writer.write(encodeURI(this.language));
		}
		return writer.toString();
	}

	@Override
	public void generateCode() {
		int height = 550;
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
		
		if (this.css.equals("chatlog")) {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/chatlog.css' type='text/css'>\n");
		} else if (this.css.equals("social_chat")) {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/social_chat.css' type='text/css'>\n");
		} else if (this.css.equals("chatroom")) {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/chatroom.css' type='text/css'>\n");
		} else if (this.css.equals("blue_chat")) {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/blue_chat.css' type='text/css'>\n");
		} else if (this.css.equals("pink_chat")) {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/pink_chat.css' type='text/css'>\n");
		} else if (this.css.equals("custom_chat")) {
			if (this.customCss != "" && this.customCss.startsWith("http") || this.customCss.startsWith("https")) {
				writer.write("<link rel='stylesheet' href='" + this.customCss + "' type='text/css'>\n");
			}
		} else {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/chatlog.css' type='text/css'>\n");
		}
		
		if (this.buttoncss.equals("blue_round_button")) {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/blue_round_button.css' type='text/css'>\n");
		} else if (this.buttoncss.equals("red_round_button")) {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/red_round_button.css' type='text/css'>\n");
		} else if (this.buttoncss.equals("green_round_button")) {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/green_round_button.css' type='text/css'>\n");
		} else if (this.buttoncss.equals("blue_bot_button")) {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/blue_bot_button.css' type='text/css'>\n");
		} else if (this.buttoncss.equals("red_bot_button")) {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/red_bot_button.css' type='text/css'>\n");
		} else if (this.buttoncss.equals("green_bot_button")) {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/green_bot_button.css' type='text/css'>\n");
		} else if (this.buttoncss.equals("purple_chat_button")) {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/purple_chat_button.css' type='text/css'>\n");
		} else if (this.buttoncss.equals("red_chat_button")) {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/red_chat_button.css' type='text/css'>\n");
		} else if (this.buttoncss.equals("green_chat_button")) {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/green_chat_button.css' type='text/css'>\n");
		} else if (this.buttoncss.equals("square_chat_button")) {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/square_chat_button.css' type='text/css'>\n");
		} else if (this.buttoncss.equals("round_chat_button")) {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/round_chat_button.css' type='text/css'>\n");
		} else {
			writer.write("<link rel='stylesheet' href='" + Site.SECUREURLLINK + "/css/blue_round_button.css' type='text/css'>\n");
		}
		
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
			writer.write("', 'child', " + width + ", " + height + "); return false;\">");
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
			writer.write("', 'child', " + width + ", " + height + "); return false;\">\n");
			writer.write("<input style=\"color:#fff;" + colorStyle + ";cursor: pointer;font: 13px/30px Arial, Helvetica, sans-serif;");
			writer.write("height: 40px;border: 0;margin: 10px 10px 0 0;font-weight:bold;\" type=\"submit\" name=\"chat\" value=\"");
			writer.write(this.caption);
			writer.write("\">\n");
			writer.write("</form>\n");
		} else if (type.equals("frame")) {
			writer.write("<iframe src=\"");
			writeURL(writer);
			if (!this.avatar && this.showChatLog) {
				writer.write("\" width=\"" + width + "\" height=\"" + height + "\" frameborder=\"0\" overflow-x=\"hidden\" scrolling=\"hidden\"></iframe>");
			} else if (!this.avatar && !this.showChatLog) {
				writer.write("\" width=\"" + width + "\" height=\"" + height + "\" frameborder=\"0\" scrolling=\"hidden\"></iframe>");
			} else {
				writer.write("\" width=\"" + width + "\" height=\"" + height + "\" frameborder=\"0\" scrolling=\"auto\"></iframe>");
			}
		} else if (type.equals("box")) {
			if (isLoggedIn() && getUser().getApplicationId() == null) {
				getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
			}
			writer.write("<style>\n");
			writer.write("// You can customize the css styles here\n");
			writer.write("#" + Site.PREFIX + "box {} #" + Site.PREFIX + "boxbar {} #" + Site.PREFIX + "boxbarmax {} #" + Site.PREFIX + "boxmin {} #" + Site.PREFIX + "boxmax {} #" + Site.PREFIX + "boxclose {} #" + Site.PREFIX + "bubble-text {} #" + Site.PREFIX + "box-input {}\n");
			writer.write("</style>\n");
			if (this.allowSpeech) {
				VoiceBean voiceBean = this.loginBean.getBean(VoiceBean.class);
				if (voiceBean.getResponsiveVoice()) {
					writer.write("<script src='https://code.responsivevoice.org/responsivevoice.js?key=" + Site.RESPONSIVEVOICE_KEY + "'></script>\n");
				}
			}
			writer.write("<script type='text/javascript' src='" + Site.SECUREURLLINK + "/scripts/sdk.js'></script>\n");
			writer.write("<script type='text/javascript'>\n");
			if (isLoggedIn() && getUser().getApplicationId() != null) {
				writer.write("SDK.applicationId = \"" + getUser().getApplicationId() + "\";\n");
				writer.write("SDK.backlinkURL = \"" + Site.URLLINK + "/login?affiliate=" + getUserId() + "\";\n");
			}
			if (!this.language.trim().isEmpty()) {
				writer.write("SDK.lang = \"" + this.language + "\";\n");
			}
			LearningBean learningBean = this.loginBean.getBean(LearningBean.class);
			if (learningBean.getAllowJavaScript()) {
				writer.write("SDK.secure = false;\n");
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
				writer.write("sdk.connect(user, function() {\n");
			}
			writer.write("var web = new WebChatbotListener();\n");
			writer.write("web.connection = sdk;\n");
			writer.write("web.instance = \"" + getBotBean().getInstanceId() + "\";\n");
			writer.write("web.instanceName = \"" + getBotBean().getInstanceName() + "\";\n");
			writer.write("web.prefix = \"" + Site.PREFIX + "\";\n");
			writer.write("web.caption = \"" + this.caption + "\";\n");
			if (!this.greeting.isEmpty()) {
				writer.write("web.greeting = \"" + this.greeting + "\";\n");
			}
			if (!this.height.isEmpty()) {
				writer.write("web.height = " + this.height + ";\n");
			}
			if (!this.width.isEmpty()) {
				writer.write("web.width = " + this.width + ";\n");
			}
			if (!this.offset.isEmpty()) {
				writer.write("web.offset = " + this.offset + ";\n");
			}
			if (!this.location.isEmpty()) {
				writer.write("web.boxLocation = \"" + this.location + "\";\n");
			}
			if (!this.color.trim().isEmpty()) {
				writer.write("web.color = \"" + this.color + "\";\n");
			}
			if (!this.background.trim().isEmpty()) {
				writer.write("web.background = \"" + this.background + "\";\n");
			}
			if(!this.css.trim().isEmpty()) {
				if (this.css.equals("chatlog")) {
					writer.write("web.css = \"" + Site.SECUREURLLINK + "/css/chatlog.css" + "\";\n");
				} else if (this.css.equals("social_chat")) {
					writer.write("web.css = \"" + Site.SECUREURLLINK  + "/css/social_chat.css" + "\";\n");
				} else if (this.css.equals("chatroom")) {
					writer.write("web.css = \"" + Site.SECUREURLLINK + "/css/chatroom.css" + "\";\n");
				} else if (this.css.equals("blue_chat")) {
					writer.write("web.css = \"" + Site.SECUREURLLINK + "/css/blue_chat.css" + "\";\n");
				} else if (this.css.equals("pink_chat")) {
					writer.write("web.css = \"" + Site.SECUREURLLINK + "/css/pink_chat.css" + "\";\n");
				} else if (this.css.equals("custom_chat")) {
					if (this.customCss != "" && this.customCss.startsWith("http") || this.customCss.startsWith("https")) {
						writer.write("web.css = \"" + this.customCss +  "\";\n");
					}
				}
			} else {
				writer.write("web.css = \"" + Site.SECUREURLLINK + "/css/chatlog.css" + "\";\n");
			}
			writer.write("web.version = 6.0;\n");
			if (this.bubble) {
				writer.write("web.bubble = true;\n");
			}
			if (Site.BACKLINK) {
				writer.write("web.backlink = " + this.showLink + ";\n");
			}
			if (this.promptContactInfo) {
				writer.write("web.promptContactInfo = true;\n");
			}
			if (this.showMenubar) {
				writer.write("web.showMenubar = true;\n");
			} else {
				writer.write("web.showMenubar = false;\n");
			}
			if (this.boxMax) {
				writer.write("web.showBoxmax = true;\n");
			} else {
				writer.write("web.showBoxmax = false;\n");
			}
			if (this.sendImage) {
				writer.write("web.showSendImage = true;\n");
			} else {
				writer.write("web.showSendImage = false;\n");
			}
			if (this.chooseLanguage) {
				writer.write("web.showChooseLanguage = true;\n");
			} else {
				writer.write("web.showChooseLanguage = false;\n");
			}
			if (!this.speak) {
				writer.write("web.speak = false;\n");
			}
			if (!this.allowSpeech) {
				writer.write("web.allowSpeech = false;\n");
			} else {
				VoiceBean voiceBean = this.loginBean.getBean(VoiceBean.class);
				if (voiceBean.getNativeVoice()) {
					writer.write("web.nativeVoice = true;\n");
					writer.write("web.nativeVoiceName = \"" + voiceBean.getNativeVoiceName() + "\";\n");
					writer.write("web.lang = \"" + voiceBean.getLanguage() + "\";\n");
					if (this.language.trim().isEmpty() || this.language.equals("en")) {
						writer.write("SDK.lang = \"" + voiceBean.getLanguage() + "\";\n");
					}
				}
				if (voiceBean.getResponsiveVoice()) {
					writer.write("SDK.initResponsiveVoice();\n");
				}
				if (voiceBean.getBingSpeech()) {
					writer.write("SDK.initBingSpeech('" + this.loginBean.getBotBean().getInstanceId() + "');\n");
				}
			}
			if (!this.avatar) {
				writer.write("web.avatar = false;\n");
			}
			if (this.showChatLog) {
				writer.write("web.chatLog = true;\n");
			} 
			else {
				writer.write("web.chatLog = false;\n");
			}
			writer.write("web.popupURL = \"");
			writeURL(writer);
			writer.write("\";\n");
			writer.write("web.createBox();\n");
			if (this.userName != null && !this.userName.isEmpty()) {
				writer.write("});\n");
			}
			writer.write("</script>\n");
		} else if (type.equals("div")) {
			if (isLoggedIn() && getUser().getApplicationId() == null) {
				getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
			}
			if (!this.background.trim().isEmpty()) {
				writer.write("<div id='bot-div' style='background-color:" + this.background + "'>\n");
			} else {
				writer.write("<div id='bot-div'>\n");
			}
			if (this.avatar) {
				String style = "height:300px;";
				if (!this.height.isEmpty()) {
					style = "height:" + this.height + "px;";
				}
				writer.write("  <div id='avatar-image-div' style='display:none;'>\n");
				writer.write("    <img id='avatar' style='" + style + "'/>\n");
				writer.write("  </div>\n");
				writer.write("  <div id='avatar-video-div' style='display:none;background-repeat: no-repeat;'>\n");
				writer.write("    <video id='avatar-video' autoplay preload='auto' style='background:transparent;" + style + "'>\n");
				writer.write("      Video format not supported by your browser (try Chrome)\n");
				writer.write("    </video>\n");
				writer.write("  </div>\n");
				writer.write("  <div id='avatar-canvas-div' style='display:none'>\n");
				writer.write("    <canvas id='avatar-canvas' style='background:transparent;" + style + "'>\n");
				writer.write("      Canvas not supported by your browser (try Chrome)\n");
				writer.write("    </canvas>\n");
				writer.write("  </div>\n");
			}
			writer.write("  <div>\n");
			writer.write("    <div style='max-height:100px;overflow:auto;margin:8px;'>\n");
			writer.write("      <span id='response'></span><br/>\n");
			writer.write("    </div>\n");
			writer.write("    <span style='display:block;overflow:hidden;margin:2px;padding-right:4px'><input id='chat' type='text' style='width:100%'/></span>\n");
			writer.write("  </div>\n");
			writer.write("</div>\n");
			writer.write("<script type='text/javascript' src='" + Site.SECUREURLLINK + "/scripts/sdk.js'></script>\n");
			writer.write("<script type='text/javascript'>\n");
			if (isLoggedIn() && getUser().getApplicationId() != null) {
				writer.write("SDK.applicationId = \"" + getUser().getApplicationId() + "\";\n");			
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
				writer.write("sdk.connect(user, function() {\n");
			}
			writer.write("var web = new WebChatbotListener();\n");
			writer.write("web.connection = sdk;\n");
			writer.write("web.instance = '" + getBotBean().getInstanceId() + "';\n");
			writer.write("web.instanceName = '" + getBotBean().getInstanceName() + "';\n");
			if (!this.speak) {
				writer.write("web.speak = false;\n");
			}
			if (!this.allowSpeech) {
				writer.write("web.allowSpeech = false;\n");
			} else {
				VoiceBean voiceBean = this.loginBean.getBean(VoiceBean.class);
				if (voiceBean.getNativeVoice()) {
					writer.write("web.nativeVoice = true;\n");
					writer.write("web.nativeVoiceName = \"" + voiceBean.getNativeVoiceName() + "\";\n");
					writer.write("web.lang = \"" + voiceBean.getLanguage() + "\";\n");
					writer.write("SDK.lang = \"" + voiceBean.getLanguage() + "\";\n");
				}
			}
			writer.write("web.greet();\n");
			writer.write("document.getElementById('chat').addEventListener('keypress', function(event) {\n");
			writer.write("  if (event.keyCode == 13) {\n");
			writer.write("    web.sendMessage();\n");
			writer.write("    return false;\n");
			writer.write("  }\n");
			writer.write("});\n");
			if (this.userName != null && !this.userName.isEmpty()) {
				writer.write("});\n");
			}
			writer.write("</script>\n");
		}
		setCode(writer.toString());
	}

	@Override
	public void disconnectInstance() {
		disconnect();
	}
	
	@Override
	public void disconnect() {
		super.disconnect();
		this.type = "box";
		this.greeting = "";
		this.farewell = "";
		this.avatar = true;
		this.bubble = true;
		this.speak = true;
		this.allowSpeech = true;
		this.allowEmotes = false;
		this.allowCorrection = false;
		this.loginBanner = false;
		this.avatarExpandable = false;
		this.staticHTML = false;
		this.advancedInfo = false;
		this.showMenubar = true;
		this.boxMax = true;
		this.chooseLanguage = true;
		this.sendImage = true;
		this.showChatLog = true;
	}
}

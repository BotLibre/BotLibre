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

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.User.UserType;

public class ForumEmbedTabBean extends EmbedWebMediumBean {
			
	public ForumEmbedTabBean() {
		this.caption = "Forum";
	}
	
	@Override
	public ForumBean getBean() {
		return getLoginBean().getBean(ForumBean.class);
	}

	@Override
	public void disconnect() {
		super.disconnect();
		this.caption = "Forum";
	}

	public void generateCode(String subdomain, String type, String caption, String userName, String password, String token, String css,
					String banner, String footer, String color, String background, boolean showAds,
					boolean facebookLogin, boolean loginBanner, boolean showLink) {
		try {
			this.type = Utils.sanitize(type.trim());
			this.caption = Utils.sanitize(caption.trim());
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
			this.banner = Utils.sanitize(banner.trim());
			this.footer = Utils.sanitize(footer.trim());
			this.color = Utils.sanitize(color.trim());
			this.background = Utils.sanitize(background.trim());
			this.loginBanner = loginBanner;
			this.facebookLogin = facebookLogin;
			this.showAds = showAds;
			if (!Site.COMMERCIAL && !showLink && (getUser() == null || getUser().getType() == UserType.Basic)) {
				throw new BotException("You must upgrade to Bronze or Gold to remove the backlink");
			}
			if (Site.BACKLINK) {
				this.showLink = showLink;
			}
			if (subdomain != null && !subdomain.isEmpty() && getLoginBean().getBean(ForumBean.class).isAdmin()) {
				this.subdomain = Utils.sanitize(subdomain);
				getLoginBean().getBean(ForumBean.class).setSubdomain(subdomain, this);
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
		writer.write("/forum?");
		if (embedded) {
			writer.write("embedded=true");
		}
		writer.write("&id=");
		writer.write(String.valueOf(getLoginBean().getBean(ForumBean.class).getInstance().getId()));
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
		writer.write("&background=");
		writer.write(encodeURI(this.background));
		if (!this.css.isEmpty()) {
			writer.write("&css=");
			writer.write(encodeURI(this.css));
		}
		if (!this.banner.isEmpty()) {
			writer.write("&banner=");
			writer.write(encodeURI(this.banner));
		}
		if (!this.footer.isEmpty()) {
			writer.write("&footer=");
			writer.write(encodeURI(this.footer));
		}
		return writer.toString();
	}

	public void generateCode() {
		int height = 600;
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
			writer.write("', 'child', 700, " + height + "); return false;\">\n");
			writer.write("<input style=\"color:#fff;" + colorStyle + ";cursor: pointer;font: 13px/30px Arial, Helvetica, sans-serif;");
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
			writer.write("','child', 700, " + height + ");return false;\">");
			writer.write(this.caption);
			writer.write("</a>\n");
			writer.write("</div>\n");
		} else if (type.equals("frame")) {
			writer.write("<iframe src=\"");
			writeURL(writer);
			writer.write("\" width=\"700\" height=\"" + height + "\" frameborder=\"0\" scrolling=\"auto\"></iframe>");
		}
		setCode(writer.toString());
	}
}

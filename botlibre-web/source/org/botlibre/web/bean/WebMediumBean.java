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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.CorrectionMode;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AccessMode;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.AvatarImage;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.Category;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.admin.ContentRating;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.admin.DomainForwarder;
import org.botlibre.web.admin.User.UserType;
import org.botlibre.web.admin.WebMedium;
import org.botlibre.web.bean.LoginBean.Page;
import org.botlibre.web.rest.WebMediumConfig;

@SuppressWarnings("unchecked")
public abstract class WebMediumBean<T extends WebMedium> extends BrowseBean<T> {
		
	public WebMediumBean() {
	}

	public String getInstanceName() {
		if (this.instance == null) {
			return "";
		}
		return this.instance.getName();
	}
	
	public boolean allowSubdomain() {
		return false;
	}
	
	public void updateFromConfig(WebMedium instance, WebMediumConfig config) {
		if (config.name != null) {
			instance.setName(config.name);
		}
		if (config.alias != null) {
			instance.setAlias(config.alias);
		}
		if (config.description != null) {
			instance.setDescription(config.description);
		}
		if (config.details != null) {
			instance.setDetails(config.details);
		}
		if (config.disclaimer != null) {
			instance.setDisclaimer(config.disclaimer);
		}
		if (config.license != null) {
			instance.setLicense(config.license);
		}
		if (config.website != null) {
			instance.setWebsite(config.website);
			if (instance.isExternal()) {
				instance.setPaphus(config.website.contains("paphuslivechat") || config.website.contains("botlibre.biz"));
			}
		}
		instance.setPrivate(config.isPrivate);
		instance.setHidden(config.isHidden);
		if (config.accessMode != null) {
			instance.setAccessMode(AccessMode.valueOf(config.accessMode));
		}
		if (config.forkAccessMode != null) {
			instance.setForkAccessMode(AccessMode.valueOf(config.forkAccessMode));
			if (instance.getForkAccessMode() == AccessMode.Users) {
				instance.setAllowForking(true);
			}
		}
		if (config.contentRating != null) {
			if (!config.isPrivate && !Site.ADULT && ContentRating.valueOf(config.contentRating) == ContentRating.Adult) {
				throw new BotException("Adult content must be private.");
			}
			instance.setContentRating(ContentRating.valueOf(config.contentRating));
		}
		if (config.tags != null) {
			instance.setTagsString(config.tags);
		}
		if (config.categories != null) {
			instance.setCategoriesString(config.categories);
		}
		if (Site.ADULT) {
			instance.setAdult(true);
		} else if (isSuper()) {
			instance.setAdult(config.isAdult);
		}
		if (Site.ADCODE) {
			if (!Site.COMMERCIAL && (!config.showAds || (config.adCode != null && !config.adCode.isEmpty())) && getUser().getType() == UserType.Basic) {
				throw new BotException("Only Bronze and Gold accounts can disable ads, or set ad code.");
			}
			instance.setShowAds(config.showAds);
			instance.setAdCode(config.adCode);
		}
		if (config.name.equals("")) {
			throw new BotException("Invalid name");
		}
	}
	
	public void writeHeadMetaHTML(Writer out) {
		if (getDisplayInstance() == null) {
			return;
		}
		try {
			out.write("<meta name='description' content='");
			out.write(Utils.removeCRs(Utils.escapeQuotes(Utils.stripTags(getDisplayInstance().getDescription()))));
			out.write("'/>\n");
			out.write("<meta name='keywords' content='");
			out.write(Utils.removeCRs(Utils.escapeQuotes(Utils.stripTags(getDisplayInstance().getTagsString()))));
			out.write("'/>\n");
			out.write("<link rel='image_src' href='");
			out.write(Site.SECUREURLLINK);
			out.write("/");
			out.write(getAvatarImage(getDisplayInstance()));
			out.write("'>\n");
			out.write("<meta property='og:image' content='");
			out.write(Site.SECUREURLLINK);
			out.write("/");
			out.write(getAvatarImage(getDisplayInstance()));
			out.write("' />\n");
			
			// Twitter Card
			out.write("<meta name='twitter:card' content='summary' />\n");
			out.write("<meta name='twitter:title' content='");
			out.write(getDisplayInstance().getName());
			out.write("'>\n");
			out.write("<meta name='twitter:description' content='");
			out.write(Utils.removeCRs(Utils.escapeQuotes(Utils.stripTags(getDisplayInstance().getDescription()))));
			out.write("'>\n");
			out.write("<meta name='twitter:image' content='");
			out.write(Site.SECUREURLLINK);
			out.write("/");
			out.write(getAvatarImage(getDisplayInstance()));
			out.write("' />\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeExternalHTML(Writer out) {
		if (getDisplayInstance() == null || !getDisplayInstance().isExternal()) {
			return;
		}
		try {
			if (!getDisplayInstance().isPaphus()) {
				out.write("<p><span>");
				out.write(this.loginBean.translate("This " + Utils.camelCaseToLowerCase(getDisplayName())
						+ " is hosted externally"));
				out.write(".</span></p>\n");
			} else {
				out.write("<p><span>");
				out.write(this.loginBean.translate("This " + Utils.camelCaseToLowerCase(getDisplayName())
						+ " is hosted on"));
				out.write(" <a target='_blank' href='https://www.botlibre.biz'>Bot Libre for Business</a>.</span></p>\n");
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeTabLabelsHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (getDisplayInstance() == null) {
			return;
		}
		try {
			out.write("<ul class='ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all'>\n");
			out.write("<li class='ui-state-default ui-corner-top ui-tabs-active ui-state-active'><a href='#tabs-1' class='ui-tabs-anchor'>");
			out.write(this.loginBean.translate("Info"));
			out.write("</a></li>\n");
			out.write("<li class='ui-state-default ui-corner-top'><a href='#tabs-2' class='ui-tabs-anchor'>");
			out.write(this.loginBean.translate("Details"));
			out.write("</a></li>\n");
			out.write("<li class='ui-state-default ui-corner-top'><a href='#tabs-3' class='ui-tabs-anchor'>");
			out.write(this.loginBean.translate("Stats"));
			out.write("</a></li>\n");
			out.write("</ul>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeTabsHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (getDisplayInstance() == null) {
			return;
		}
		try {
			out.write("<div id='tabs' class='ui-tabs ui-widget ui-widget-content ui-corner-all'>\n");
			writeTabLabelsHTML(proxy, embed, out);
			writeInfoTabHTML(proxy, embed, out);
			writeDetailsTabHTML(proxy, embed, out);
			writeStatsTabHTML(proxy, embed, out);
			out.write("</div>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void writeInfoTabExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
	}

	public void writeInfoTabExtra2HTML(SessionProxyBean proxy, boolean embed, Writer out) {
	}

	public void writeDetailsTabExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
	}

	public void writeStatsTabExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
	}
	
	public void writeInfoTabHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			out.write("<div id='tabs-1' class='ui-tabs-panel ui-widget-content ui-corner-bottom'>\n");
			writeIconHTML(proxy, out);
			out.write("<p>\n");
			out.write("<span class='description'>");
			out.write(getDisplayInstance().getDescriptionText());
			out.write("</span>\n");
			out.write("</p>\n");
			out.write("<p>\n");
			writeInfoTabExtraHTML(proxy, embed, out);
			out.write("<span>");
			out.write(this.loginBean.translate("Alias"));
			out.write(":</span> @");
			out.write(getDisplayInstance().getAlias());
			out.write("<br/>\n");
			if (!getDisplayInstance().getWebsite().isEmpty()) {
				out.write("<span>");
				out.write(this.loginBean.translate("Website"));
				out.write(":</span> <a target='_blank' href='");
				out.write(getDisplayInstance().getWebsiteURL());
				out.write("'>");
				out.write(getDisplayInstance().getWebsite());
				out.write("</a><br/>\n");
			}
			if (getDisplayInstance().getDomainForwarder() != null) {
				out.write("<span>");
				out.write(this.loginBean.translate("Subdomain"));
				out.write(":</span> <a target='_blank' href='");
				out.write(getDisplayInstance().getDomainForwarder().getURL());
				out.write("'>");
				out.write(getDisplayInstance().getSubdomain());
				out.write("</a><br/>\n");
			}
			if (!getDisplayInstance().getCategories().isEmpty()) {
				if (!embed) {
					out.write("<span>");
					out.write(this.loginBean.translate("Categories"));
					out.write(":</span> ");
					out.write(getDisplayInstance().getCategoryLinks(getBrowseAction() + "?category="));
					out.write("<br/>\n");
				} else {
					out.write("<span>");
					out.write(this.loginBean.translate("Categories"));
					out.write(":</span> ");
					out.write(getDisplayInstance().getCategoriesString());
					out.write("<br/>\n");
				}
			}
			if (!getDisplayInstance().getTags().isEmpty()) {
					if (!embed) {
						out.write("<span>");
						out.write(this.loginBean.translate("Tags"));
						out.write(":</span> ");
						out.write(getDisplayInstance().getTagLinks(getBrowseAction() +"?tag-filter="));
						out.write("<br/>\n");
					} else {
						out.write("<span>");
						out.write(this.loginBean.translate("Tags"));
						out.write(":</span> ");
						out.write(getDisplayInstance().getTagsString());
						out.write("<br/>\n");
					}
			}
			if (!embed && getDisplayInstance().getDomain() != null && !getDisplayInstance().getDomain().equals(getDomain())) {
				out.write("<span>");
				out.write(this.loginBean.translate("Workspace"));
				out.write(":</span> <a href='domain?id=");
				out.write(String.valueOf(getDisplayInstance().getDomain().getId()));
				out.write("'>");
				out.write(getDisplayInstance().getDomain().getName());
				out.write("</a><br/>\n");
			}
			if (!getDisplayInstance().getDisclaimer().isEmpty()) {
				out.write("<span>");
				out.write(this.loginBean.translate("Disclaimer"));
				out.write(":</span><br/>\n");
				out.write(Utils.formatHTMLOutput(getDisplayInstance().getDisclaimer()));
				out.write("<br/>\n");
			}
			out.write("<span>");
			out.write(this.loginBean.translate("Content Rating"));
			out.write(":</span> ");
			out.write(getDisplayInstance().getContentRating().name());
			out.write("<br/>\n");
			
			writeInfoTabExtra2HTML(proxy, embed, out);
			
			out.write("</p>\n");
			out.write("</div>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writEditAdCodeHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			if (Site.ADCODE && !Site.COMMERCIAL) {
				out.write(this.loginBean.translate("Ad Code"));
				out.write(" <input type='checkbox' ");
				boolean showAds = !getEditInstance().getAdCode().isEmpty() || !getEditInstance().getShowAds();
				if (showAds) {
					out.write("checked ");
				}
				out.write("onclick=\"document.getElementById('adCode').style.display=(this.checked ? 'inline' : 'none');\" title=\"");
				out.write(this.loginBean.translate("You can display ads on you're " + Utils.camelCaseToLowerCase(getDisplayName())
							+ "'s pages"));
				out.write("\"><br/>");
				out.write("<div id='adCode' ");
				if (!showAds) {
					out.write("style='display:none' ");
				} else {
					out.write("style='display:inline' ");
				}
				out.write(">\n<input name='showAds' type='checkbox' ");
				if (getEditInstance().getShowAds()) {
					out.write("checked");
				}
				out.write(" title=\"");
				out.write(this.loginBean.translate("Bronze and Gold accounts can remove ads from their pages"));
				out.write("\">");
				out.write(this.loginBean.translate("Show Ads"));
				out.write("<br/>\n");
				out.write("<input name='adVerified' type='checkbox' ");
				if (getEditInstance().isAdCodeVerified()) {
					out.write("checked");
				}
				if (!isSuper()) {
					out.write("disabled");
				}
				out.write(" title='");
				out.write(this.loginBean.translate("If your ad contains JavaScript it must be verified before it is used"));
				out.write("'>");
				out.write(this.loginBean.translate("Ad Verified"));
				out.write("</input><br/>\n");
				out.write("<textarea name='adCode' placeholder=\"");
				out.write(this.loginBean.translate("You can add your own ad code from Google Adsense or any other advertising provider to your "
							+ Utils.camelCaseToLowerCase(getTypeName())
							+ "'s pages, and make money.  Only allowed for Bronze and Gold accounts."));
				out.write("\"");
				out.write(" type='textarea' >");
				out.write(getEditInstance().getAdCode());
				out.write("</textarea><br/>");
				out.write("</div>\n");
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeCreateAdCodeHTML(boolean error, SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			if (Site.ADCODE && !Site.COMMERCIAL) {
				out.write(this.loginBean.translate("Ad Code"));
				out.write(" <input type='checkbox' ");
				boolean showAds = error && (!this.instance.getAdCode().isEmpty() || !this.instance.getShowAds());
				if (showAds) {
					out.write("checked ");
				}
				out.write("onclick=\"document.getElementById('adCode').style.display=(this.checked ? 'inline' : 'none');\" title=\"");
				out.write(this.loginBean.translate("You can display ads on you're " + Utils.camelCaseToLowerCase(getDisplayName()) + "'s pages"));
				out.write("\"><br/>");
				out.write("<div id='adCode' ");
				if (!showAds) {
					out.write("style='display:none' ");
				} else {
					out.write("style='display:inline' ");
				}
				out.write(">\n<input name='showAds' type='checkbox' ");
				if (!error || getInstance().getShowAds()) {
					out.write("checked");
				}
				out.write(" title=\"");
				out.write(this.loginBean.translate("Bronze and Gold accounts can remove ads from their pages"));
				out.write("\">");
				out.write(this.loginBean.translate("Show Ads"));
				out.write("<br/>");
				out.write("<textarea name='adCode' placeholder=\"");
				out.write(this.loginBean.translate("You can add your own ad code from Google Adsense or any other advertising provider to your "
							+ Utils.camelCaseToLowerCase(getDisplayName())
							+ "'s pages, and make money.  Only allowed for Bronze and Gold accounts."));
				out.write("\"");
				out.write(" type='textarea' >");
				if (error) {
					out.write(this.instance.getAdCode());
				}
				out.write("</textarea><br/>");
				out.write("</div>\n");
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeCreateCommonHTML(boolean error, boolean link, SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			out.write(this.loginBean.translate("Alias"));
			out.write("<br/>\n");
			out.write("<input type='text' name='alias' placeholder='");
			out.write(this.loginBean.translate("unique name"));
			out.write("' title='");
			out.write(this.loginBean.translate("Unique alphanumeric lowercase name"));
			out.write("' value='");
			out.write((!error) ? "" : this.instance.getAlias());
			out.write("' /><br/>\n");
			
			out.write(this.loginBean.translate("Description"));
			out.write("<br/>\n");
			out.write("<textarea name='description' placeholder='");
			out.write(this.loginBean.translate("optional description"));
			out.write("' title='");
			out.write(this.loginBean.translate("Optional description"));
			out.write("' >");
			out.write((!error) ? "" : this.instance.getDescription());
			out.write("</textarea><br/>\n");

			out.write(this.loginBean.translate("Details"));
			out.write(" <input type='checkbox' ");
			boolean showDetails = error && !this.instance.getDetails().isEmpty();
			if (showDetails) {
				out.write("checked ");
			}
			out.write("onclick=\"document.getElementById('details').style.display=(this.checked ? 'inline' : 'none');\" title=\"");
			out.write(this.loginBean.translate("You can enter optional additional information"));
			out.write("\">");
			out.write("<div id='details' ");
			if (!showDetails) {
				out.write("style='display:none' ");
			} else {
				out.write("style='display:inline' ");
			}
			out.write("><br/><textarea placeholder='");
			out.write(this.loginBean.translate("optional additional information"));
			out.write("' title='");
			out.write(this.loginBean.translate("Optional additional information"));
			out.write("' name='details' type='textarea' >");
			if (error) {
				out.write(this.instance.getDetails());
			}
			out.write("</textarea></div><br/>\n");

			out.write(this.loginBean.translate("Disclaimer"));
			out.write(" <input type='checkbox' ");
			boolean showDisclaimer = error && !this.instance.getDisclaimer().isEmpty();
			if (showDisclaimer) {
				out.write("checked ");
			}
			out.write("onclick=\"document.getElementById('disclaimer').style.display=(this.checked ? 'inline' : 'none');\" title=\"");
			out.write(this.loginBean.translate("You can enter optional legal information"));
			out.write("\">");
			out.write("<div id='disclaimer' ");
			if (!showDisclaimer) {
				out.write("style='display:none' ");
			} else {
				out.write("style='display:inline' ");
			}
			out.write("><br/><textarea placeholder='");
			out.write(this.loginBean.translate("optional legal information"));
			out.write("' title='");
			out.write(this.loginBean.translate("Optional legal information"));
			out.write("' name='disclaimer' type='textarea' >");
			if (error) {
				out.write(this.instance.getDisclaimer());
			}
			out.write("</textarea></div><br/>\n");
			
			if (!link) {
				out.write(this.loginBean.translate("License"));
				out.write("<br/><input id='license' name='license' type='text' value='");
				if (error) {
					out.write(this.instance.getLicense());
				}
				out.write("' placeholder='");
				out.write(this.loginBean.translate("optional license to release the " + Utils.camelCaseToLowerCase(getDisplayName()) + " and all of its content under"));
				out.write("' /><br/>\n");
				out.write("<script>\n");
		  		out.write("$( '#license' ).autocomplete({\n");
				out.write("source: ['Copyright ");
				out.write(getUser().getUserId());
				out.write(", all rights reserved', 'Public Domain', 'Creative Commons Attribution 3.0 Unported License', 'GNU General Public License 3.0', 'Apache License, Version 2.0', 'Eclipse Public License 1.0' ],\n");
				out.write("minLength: 0\n");
				out.write("}).on('focus', function(event) {\n");
				out.write("	var self = this;\n");
				out.write("	$(self).autocomplete('search', '');\n");
				out.write("});\n");
				out.write("</script>\n");
			}
			
			if (link) {
				out.write("<span class='required'>");
				out.write(this.loginBean.translate("Website"));
				out.write("</span><br/>\n");
				out.write("<input id='website' class='required' name='website' type='text' value='");
			} else {
				out.write("<span>");
				out.write(this.loginBean.translate("Website"));
				out.write("</span><br/>\n");
				out.write("<input id='website' name='website' type='text' value='");
			}
			if (error) {
				out.write(this.instance.getWebsite());
			}
			if (link) {
				out.write("' placeholder='");
				out.write(this.loginBean.translate("enter the URL the " + Utils.camelCaseToLowerCase(getDisplayName()) + " is hosted on"));
			} else {
				out.write("' placeholder='");
				out.write(this.loginBean.translate("if this " + Utils.camelCaseToLowerCase(getDisplayName()) + " has its own website, you can enter it here"));
			}
			out.write("' /><br/>\n");

			if (!link && allowSubdomain()) {
				out.write(this.loginBean.translate("Website Subdomain (or domain)"));
				out.write("<br/>\n");
				out.write("<input id='subdomain' name='subdomain' type='text' value='");
				if (error) {
					out.write(this.instance.getSubdomain());
				}
				out.write("' placeholder=\"");
				out.write(this.loginBean.translate("you can choose a subdomain to host your " + Utils.camelCaseToLowerCase(getDisplayName()) + "'s own website, or give a domain that you have registered and forward to this server's ip address"));
				out.write("\" /><br/>\n");
			}

			out.write("<span class='required'>");
			out.write(this.loginBean.translate("Categories"));
			out.write("</span><br/>\n");
			out.write("<input id='categories' name='categories' type='text' value='");
			out.write((!error) ? getCategoryString() : this.instance.getCategoriesString());
			out.write("' placeholder='");
			out.write(this.loginBean.translate("comma seperated list of categories to categorize the " + Utils.camelCaseToLowerCase(getDisplayName()) + " under"));
			out.write("' /><br/>\n");
			out.write("<script>\n");
			out.write("$(function() {\n");
			out.write("	var availableCategories = [");
			out.write(getAllCategoriesString());
			out.write("];\n");
			out.write("	multiDropDown('#categories', availableCategories);\n");
			out.write("});\n");
			out.write("</script>\n");

			out.write(this.loginBean.translate("Tags"));
			out.write("<br/>\n");
			out.write("<input id='tags' name='tags' type='text' value='");
			if (error) {
				out.write(this.instance.getEditTagsString());
			}
			out.write("' placeholder='");
			out.write(this.loginBean.translate("optional comma seperated list of tags to tag the " + Utils.camelCaseToLowerCase(getDisplayName()) + " under"));
			out.write("' /><br/>\n");
			out.write("<script>\n");
			out.write("$(function() {\n");
			out.write("	var availableTags = [");
			out.write(getAllTagsString());
			out.write("];\n");
			out.write("	multiDropDown('#tags', availableTags);\n");
			out.write("});\n");
			out.write("</script>\n");

			if (!link) {
				out.write("<input name='private' type='checkbox' ");
				if (error && this.instance.isPrivate()) {
					out.write("checked");
				}
				out.write(" title='");
				out.write(this.loginBean.translate("A private " + Utils.camelCaseToLowerCase(getDisplayName())
							+ " is not visible to the public, only to the user and users they grant access"));
				out.write("'>");
				out.write(this.loginBean.translate("Private"));
				out.write("<br/>\n");
				out.write("<input name='hidden' type='checkbox' ");
				if (error && instance.isHidden()) {
					out.write("checked");
				}
				out.write(" title='");
				out.write(this.loginBean.translate("A hidden " + Utils.camelCaseToLowerCase(getDisplayName())
							+ " is not displayed in the browse directory"));
				out.write("'>");
				out.write(this.loginBean.translate("Hidden"));
				out.write("<br/>\n");
				
				out.write("<table>\n");
				
				out.write("<tr>\n");
				out.write("<td>\n");
				out.write(this.loginBean.translate("Access Mode"));
				out.write("</td>\n");
				out.write("<td><select name='accessMode' title='");
				out.write(this.loginBean.translate("Define who can access this " + Utils.camelCaseToLowerCase(getDisplayName())));
				out.write("'>\n");
				out.write("<option value='Everyone' ");
				out.write((!error) ? "selected" : isAccessModeSelected("Everyone"));
				out.write(">");
				out.write(this.loginBean.translate("Everyone"));
				out.write("</option>\n");
				out.write("<option value='Users' ");
				out.write((!error) ? "" : isAccessModeSelected("Users"));
				out.write(">");
				out.write(this.loginBean.translate("Users"));
				out.write("</option>\n");
				out.write("<option value='Members' ");
				out.write((!error) ? "" : isAccessModeSelected("Members"));
				out.write(">");
				out.write(this.loginBean.translate("Members"));
				out.write("</option>\n");
				out.write("<option value='Administrators' ");
				out.write((!error) ? "" : isAccessModeSelected("Administrators"));
				out.write(">");
				out.write(this.loginBean.translate("Administrators"));
				out.write("</option>\n");
				out.write("</select></td>\n");
				out.write("</tr>\n");
				
				out.write("<tr>\n");
				out.write("<td>\n");
				out.write(this.loginBean.translate("Fork Access Mode"));
				out.write("</td>\n");
				out.write("<td><select name='forkAccessMode' title='");
				out.write(this.loginBean.translate("Define who can fork (copy) this " + Utils.camelCaseToLowerCase(getDisplayName())));
				out.write("'>\n");
				out.write("<option value='Users' ");
				out.write((!error) ? "" : isForkAccessModeSelected("Users"));
				out.write(">");
				out.write(this.loginBean.translate("Users"));
				out.write("</option>\n");
				out.write("<option value='Members' ");
				out.write((!error) ? "" : isForkAccessModeSelected("Members"));
				out.write(">");
				out.write(this.loginBean.translate("Members"));
				out.write("</option>\n");
				out.write("<option value='Administrators' ");
				out.write((!error) ? "selected" : isForkAccessModeSelected("Administrators"));
				out.write(">");
				out.write(this.loginBean.translate("Administrators"));
				out.write("</option>\n");
				out.write("<option value='Disabled' ");
				out.write((!error) ? "" : isForkAccessModeSelected("Disabled"));
				out.write(">");
				out.write(this.loginBean.translate("Disabled"));
				out.write("</option>\n");
				out.write("</select></td>\n");
				out.write("</tr>\n");
			} else {
				out.write("<table>\n");
			}
			
			out.write("<tr>\n");
			out.write("<td>\n");
			out.write(this.loginBean.translate("Content Rating"));
			out.write("</td>\n");
			out.write("<td><select name='contentRating' title='Rate the ");
			out.write(Utils.camelCaseToLowerCase(getDisplayName()));
			out.write("'s content restriction level'>\n");
			if (!Site.ADULT) {
				out.write("<option value='Everyone' ");
				out.write((!error) ? "" : isContentRatingSelected("Everyone"));
				out.write(">");
				out.write(this.loginBean.translate("Everyone"));
				out.write("</option>\n");
			}
			out.write("<option value='Teen' ");
			out.write((!error) ? "selected" : isContentRatingSelected("Teen"));
			out.write(">");
			out.write(this.loginBean.translate("Teen"));
			out.write("</option>\n");
			out.write("<option value='Mature' ");
			out.write((!error) ? "" : isContentRatingSelected("Mature"));
			out.write(">");
			out.write(this.loginBean.translate("Mature"));
			out.write("</option>\n");
			if (Site.ADULT) {
				out.write("<option value='Adult' ");
				out.write((!error) ? "" : isContentRatingSelected("Adult"));
				out.write(">");
				out.write(this.loginBean.translate("Adult"));
				out.write("</option>\n");
				out.write("</select></td>\n");
			}
			out.write("</tr>\n");
			
			if (link) {
				out.write("</table>\n");
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeEditNameHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			out.write(this.loginBean.translate("Name"));
			out.write("<br/>\n");
			out.write("<input name='name' title=\"");
			out.write(this.loginBean.translate("You can change your " + Utils.camelCaseToLowerCase(getDisplayName())
					+ "'s name"));
			out.write("\" type='text' value='");
			out.write(getEditInstance().getName());
			out.write("' /><br/>\n");
			
			out.write(this.loginBean.translate("Alias"));
			out.write("<br/>\n");
			out.write("<input name='alias' title=\"");
			out.write(this.loginBean.translate("You can change your " + Utils.camelCaseToLowerCase(getDisplayName())
					+ "'s alias (must be unique)"));
			out.write("\" type='text' value='");
			out.write(getEditInstance().getAlias());
			out.write("' /><br/>\n");
			
			if (isSuper()) {
				out.write(this.loginBean.translate("Creator"));
				out.write("<br/>\n");
				out.write("<input name='creator' type='text' value='");
				out.write(getEditInstance().getCreatorUserId());
				out.write("' /><br/>\n");
			}
			
			if (!(getEditInstance() instanceof Domain)) {
				out.write(this.loginBean.translate("Workspace"));
				out.write("<br/>\n");
				out.write("<input name='newdomain' title=\"");
				out.write(this.loginBean.translate("You can switch your " + Utils.camelCaseToLowerCase(getDisplayName())
						+ "'s workspace that it is listed under"));
				out.write("\" type='text' value='");
				out.write(getEditInstance().getDomain().getAlias());
				out.write("' /><br/>\n");
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeEditCommonHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			boolean link = getEditInstance().isExternal();
			out.write(this.loginBean.translate("Description"));
			out.write("<br/>\n");
			out.write("<textarea name='description' placeholder='");
			out.write(this.loginBean.translate("optional description"));
			out.write("' title='");
			out.write(this.loginBean.translate("Optional description"));
			out.write("' >");
			out.write(getEditInstance().getDescription());
			out.write("</textarea><br/>\n");

			out.write(this.loginBean.translate("Details"));
			out.write(" <input type='checkbox' ");
			boolean showDetails = !getEditInstance().getDetails().isEmpty();
			if (showDetails) {
				out.write("checked ");
			}
			out.write("onclick=\"document.getElementById('details').style.display=(this.checked ? 'inline' : 'none');\" title=\"");
			out.write(this.loginBean.translate("You can enter optional additional information"));
			out.write("\">");
			out.write("<div id='details' ");
			if (!showDetails) {
				out.write("style='display:none' ");
			} else {
				out.write("style='display:inline' ");
			}
			out.write("><br/><textarea placeholder='");
			out.write(this.loginBean.translate("optional additional information"));
			out.write("' title='");
			out.write(this.loginBean.translate("Optional additional information"));
			out.write("' name='details' type='textarea' >");
			out.write(getEditInstance().getDetails());
			out.write("</textarea></div><br/>\n");

			out.write(this.loginBean.translate("Disclaimer"));
			out.write(" <input type='checkbox' ");
			boolean showDisclaimer = !getEditInstance().getDisclaimer().isEmpty();
			if (showDisclaimer) {
				out.write("checked ");
			}
			out.write("onclick=\"document.getElementById('disclaimer').style.display=(this.checked ? 'inline' : 'none');\" title=\"");
			out.write(this.loginBean.translate("You can enter optional legal information"));
			out.write("\">");
			out.write("<div id='disclaimer' ");
			if (!showDisclaimer) {
				out.write("style='display:none' ");
			} else {
				out.write("style='display:inline' ");
			}
			out.write("><br/><textarea placeholder='");
			out.write(this.loginBean.translate("optional legal information"));
			out.write("' title='");
			out.write(this.loginBean.translate("Optional legal information"));
			out.write("' name='disclaimer' type='textarea' >");
			out.write(getEditInstance().getDisclaimer());
			out.write("</textarea></div><br/>\n");
			
			if (!link) {
				out.write(this.loginBean.translate("License"));
				out.write("<br/><input id='license' name='license' type='text' value='");
				out.write(getEditInstance().getLicense());
				out.write("' placeholder='");
				out.write(this.loginBean.translate("optional license to release the " + Utils.camelCaseToLowerCase(getDisplayName())
							+ " and all of its content under"));
				out.write("' ");
				out.write("title='");
				out.write(this.loginBean.translate("Optional license to release the " + Utils.camelCaseToLowerCase(getDisplayName())
						+ " and all of its content under"));
						out.write("' /><br/>\n");
				out.write("<script>\n");
		  		out.write("$( '#license' ).autocomplete({\n");
				out.write("source: ['Copyright ");
				out.write(getUser().getUserId());
				out.write(", all rights reserved', 'Public Domain', 'Creative Commons Attribution 3.0 Unported License', 'GNU General Public License 3.0', 'Apache License, Version 2.0', 'Eclipse Public License 1.0' ],\n");
				out.write("minLength: 0\n");
				out.write("}).on('focus', function(event) {\n");
				out.write("	var self = this;\n");
				out.write("	$(self).autocomplete('search', '');\n");
				out.write("});\n");
				out.write("</script>\n");
			}
		
			if (link) {
				out.write("<span class='required'>");
				out.write(this.loginBean.translate("Website"));
				out.write("</span><br/>\n");
				out.write("<input id='website' class='required' name='website' type='text' value='");
			} else {
				out.write("<span>");
				out.write(this.loginBean.translate("Website"));
				out.write("</span><br/>\n");
				out.write("<input id='website' name='website' type='text' value='");
			}
			out.write(getEditInstance().getWebsite());
			if (link) {
				out.write("' placeholder='");
				out.write(this.loginBean.translate("enter the URL the " + getTypeName().toLowerCase() + " is hosted on"));
				out.write("'");
				out.write(" title='");
				out.write(this.loginBean.translate("Enter the URL the " + getTypeName().toLowerCase() + " is hosted on"));
				out.write("'");
			} else {
				out.write("' placeholder='");
				out.write(this.loginBean.translate("if this " + getTypeName().toLowerCase() + " has its own website, you can enter it here"));
				out.write("'");
				out.write(" title='");
				out.write(this.loginBean.translate("If this " + getTypeName().toLowerCase() + " has its own website, you can enter it here"));
				out.write("'");
			}
			out.write(" /><br/>\n");
		
			if (!link && allowSubdomain()) {
				out.write(this.loginBean.translate("Website Subdomain (or domain)"));
				out.write("<br/>\n");
				out.write("<input id='subdomain' name='subdomain' type='text' value='");
				out.write(getEditInstance().getSubdomain());
				out.write("' placeholder=\"");
				out.write(this.loginBean.translate("you can choose a subdomain to host your " + getTypeName().toLowerCase()
						+ "'s own website, or give a domain that you have registered and forward to this server's ip address"));
				out.write("\" ");
				out.write("title=\"");
				out.write(this.loginBean.translate("You can choose a subdomain to host your " + getTypeName().toLowerCase()
						+ "'s own website, or give a domain that you have registered and forward to this server's ip address"));
				out.write("\" /><br/>");
			}
			
			out.write("<span class='required'>");
			out.write(this.loginBean.translate("Categories"));
			out.write("</span><br/>\n");
			out.write("<input id='categories' name='categories' type='text' value='");
			out.write(getEditInstance().getCategoriesString());
			out.write("' placeholder='");
			out.write(this.loginBean.translate("comma seperated list of categories to categorize the " + getTypeName().toLowerCase()
					+ " under"));
			out.write("' /><br/>\n");
			out.write("<script>\n");
			out.write("$(function() {\n");
			out.write("	var availableCategories = [");
			out.write(getAllCategoriesString());
			out.write("];\n");
			out.write("	multiDropDown('#categories', availableCategories);\n");
			out.write("});\n");
			out.write("</script>\n");

			out.write(this.loginBean.translate("Tags"));
			out.write("<br/>\n");
			out.write("<input id='tags' name='tags' type='text' value='");
			out.write(getEditInstance().getEditTagsString());
			out.write("' placeholder='");
			out.write(this.loginBean.translate("optional comma seperated list of tags to tag the " + getTypeName().toLowerCase()
					+ " under"));
			out.write("'");
			out.write(" /><br/>\n");
			out.write("<script>\n");
			out.write("$(function() {\n");
			out.write("	var availableTags = [");
			out.write(getAllTagsString());
			out.write("];\n");
			out.write("	multiDropDown('#tags', availableTags);\n");
			out.write("});\n");
			out.write("</script>\n");

			if (isSuper()) {
				out.write("<input name='isFeatured' type='checkbox' ");
				if (getEditInstance().isFeatured()) {
					out.write("checked");
				}
				out.write("/>");
				out.write(this.loginBean.translate("Is Featured"));
				out.write("<br/>\n");
				
				out.write("<input name='isAdult' type='checkbox' ");
				if (getEditInstance().isAdult()) {
					out.write("checked");
				}
				out.write("/>");
				out.write(this.loginBean.translate("Is Adult"));
				out.write("<br/>\n");
				
				if (Site.COMMERCIAL && (getEditInstance() instanceof Domain)) {
					out.write("<input name='isSubscription' type='checkbox' ");
					if (((Domain)getEditInstance()).isSubscription()) {
						out.write("checked");
					}
					out.write("/>");
					out.write(this.loginBean.translate("Subscription"));
					out.write("<br/>\n");
				}
			}
			
			if (!link) {
				out.write("<input name='private' type='checkbox' ");
				if (getEditInstance().isPrivate()) {
					out.write("checked");
				}
				out.write(" title='");
				out.write(this.loginBean.translate("A private " + getTypeName().toLowerCase()
						+ " is not visible to the public, only to the user and users they grant access"));
				out.write("'/>");
				out.write(this.loginBean.translate("Private"));
				out.write("<br/>\n");
				out.write("<input name='hidden' type='checkbox' ");
				if (getEditInstance().isHidden()) {
					out.write("checked");
				}
				out.write(" title='");
				out.write(this.loginBean.translate("A hidden " + getTypeName().toLowerCase()
						+ " is not displayed in the browse directory"));
				out.write("'/>");
				out.write(this.loginBean.translate("Hidden"));
				out.write("<br/>\n");
				
				out.write("<table>\n");
				
				out.write("<tr>\n");
				out.write("<td>\n");
				out.write(this.loginBean.translate("Access Mode"));
				out.write("</td>\n");
				out.write("<td><select name='accessMode' title='");
				out.write(this.loginBean.translate("Define who can access this " + getTypeName().toLowerCase()));
				out.write("'>\n");
				out.write("<option value='Everyone' ");
				out.write(isAccessModeSelected("Everyone"));
				out.write(">");
				out.write(this.loginBean.translate("Everyone"));
				out.write("</option>\n");
				out.write("<option value='Users' ");
				out.write(isAccessModeSelected("Users"));
				out.write(">");
				out.write(this.loginBean.translate("Users"));
				out.write("</option>\n");
				out.write("<option value='Members' ");
				out.write(isAccessModeSelected("Members"));
				out.write(">");
				out.write(this.loginBean.translate("Members"));
				out.write("</option>\n");
				out.write("<option value='Administrators' ");
				out.write(isAccessModeSelected("Administrators"));
				out.write(">");
				out.write(this.loginBean.translate("Administrators"));
				out.write("</option>\n");
				out.write("</select></td>\n");
				out.write("</tr>\n");
				
				out.write("<tr>\n");
				out.write("<td>\n");
				out.write(this.loginBean.translate("Fork Access Mode"));
				out.write("</td>\n");
				out.write("<td><select name='forkAccessMode' title='");
				out.write(this.loginBean.translate("Define who can fork (copy) this " + getTypeName().toLowerCase()));
				out.write("'>\n");
				out.write("<option value='Users' ");
				out.write(isForkAccessModeSelected("Users"));
				out.write(">");
				out.write(this.loginBean.translate("Users"));
				out.write("</option>\n");
				out.write("<option value='Members' ");
				out.write(isForkAccessModeSelected("Members"));
				out.write(">");
				out.write(this.loginBean.translate("Members"));
				out.write("</option>\n");
				out.write("<option value='Administrators' ");
				out.write(isForkAccessModeSelected("Administrators"));
				out.write(">");
				out.write(this.loginBean.translate("Administrators"));
				out.write("</option>\n");
				out.write("<option value='Disabled' ");
				out.write(isForkAccessModeSelected("Disabled"));
				out.write(">");
				out.write(this.loginBean.translate("Disabled"));
				out.write("</option>\n");
				out.write("</select></td>\n");
				out.write("</tr>\n");
			} else {
				out.write("<table>\n");
			}
			
			out.write("<tr>\n");
			out.write("<td>\n");
			out.write(this.loginBean.translate("Content Rating"));
			out.write("</td>\n");
			out.write("<td><select name='contentRating' title='");
			out.write(this.loginBean.translate("Rate the " + getTypeName().toLowerCase()
					+ "'s content restriction level"));
			out.write("'>\n");
			if (!Site.ADULT) {
				out.write("<option value='Everyone' ");
				out.write(isContentRatingSelected("Everyone"));
				out.write(">");
				out.write(this.loginBean.translate("Everyone"));
				out.write("</option>\n");
			}
			out.write("<option value='Teen' ");
			out.write(isContentRatingSelected("Teen"));
			out.write(">");
			out.write(this.loginBean.translate("Teen"));
			out.write("</option>\n");
			out.write("<option value='Mature' ");
			out.write(isContentRatingSelected("Mature"));
			out.write(">");
			out.write(this.loginBean.translate("Mature"));
			out.write("</option>\n");
			if (Site.ALLOW_ADULT) {
				out.write("<option value='Adult' ");
				out.write(isContentRatingSelected("Adult"));
				out.write(">");
				out.write(this.loginBean.translate("Adult"));
				out.write("</option>\n");
			}
			out.write("</select></td>\n");
			out.write("</tr>\n");

			if (link) {
				out.write("</table>\n");
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeDetailsTabHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			out.write("<div id='tabs-2' class='ui-tabs-hide'>\n");
			out.write("<p>\n");
			out.write("<span>\n");
			if (!getDisplayInstance().getDetails().isEmpty()) {
				out.write("<span class='details'>");
				out.write(Utils.formatHTMLOutput(getDisplayInstance().getDetails()));
				out.write("</span><br/>\n");
			}
			out.write(this.loginBean.translate("License"));
			out.write(": ");
			out.write(getDisplayInstance().getLicense());
			out.write("<br/>\n");
			out.write(this.loginBean.translate("Created"));
			out.write(": ");
			out.write(Utils.displayDate(getDisplayInstance().getCreationDate()));
			out.write("<br/>\n");
			out.write(this.loginBean.translate("Creator"));
			out.write(": <a href='login?view-user=");
			out.write(encodeURI(getDisplayInstance().getCreatorUserId()));
			out.write(proxy.proxyString());
			out.write("'>");
			out.write(getDisplayInstance().getCreatorUserId());
			out.write("</a> : <a class='menu' href='login?send-message=");
			out.write(encodeURI(getDisplayInstance().getName()));
			out.write("&user=");
			out.write(encodeURI(getDisplayInstance().getCreatorUserId()));
			out.write(proxy.proxyString());
			out.write("' title='");
			out.write(this.loginBean.translate("Send a message to the " + Utils.camelCaseToLowerCase(getDisplayName()) + "s creator"));
			out.write("'>");
			out.write(this.loginBean.translate("Send Message"));
			out.write("</a><br/>\n");
			if (getDisplayInstance().getParentId() != null) {
				try {
					BotInstance parent = AdminDatabase.instance().validate(BotInstance.class, getDisplayInstance().getParentId(), getUserId());
					if (parent.isTemplate() || this.loginBean.isEmbedded()) {
						out.write(parent.getName());
					} else {
						out.write(this.loginBean.translate("Forked from"));
						out.write(": <a href='bot?id=");
						out.write(String.valueOf(parent.getId()));
						out.write("'>");
						out.write(parent.getName());
						out.write("</a>");
					}
					out.write("<br/>\n");
				} catch (Exception missing) {
					//
				}
			}
			if (!embed && !getDisplayInstance().isExternal()) {
				out.write(this.loginBean.translate("Access"));
				out.write(": ");
				out.write(String.valueOf(getDisplayInstance().getAccessMode()));
				if (getDisplayInstance().isHidden()) {
					out.write(" (");
					out.write(this.loginBean.translate("hidden"));
					out.write(")");
				}
				if (getDisplayInstance().isPrivate()) {
					out.write(" (");
					out.write(this.loginBean.translate("private"));
					out.write(")");
				}
				out.write("<br/>\n");
				out.write(this.loginBean.translate("Id"));
				out.write(": ");
				out.write(String.valueOf(getDisplayInstance().getId()));
				out.write("<br/>\n");
				if (isSuper() && getDisplayInstance().getDomainForwarder() != null) {
					out.write(this.loginBean.translate("Forwarder"));
					out.write(": ");
					out.write(getDisplayInstance().getDomainForwarder().getForwarderAddress());
					out.write("<br/>\n");
				}
				out.write(this.loginBean.translate("Link"));
				out.write(": <a target='_blank' href='");
				out.write(Site.SECUREURLLINK);
				out.write("/");
				out.write(getPostAction());
				out.write("?id=");
				out.write(String.valueOf(getDisplayInstance().getId()));
				out.write("'>");
				out.write(Site.SECUREURLLINK);
				out.write("/");
				out.write(getPostAction());
				out.write("?id=");
				out.write(String.valueOf(getDisplayInstance().getId()));
				out.write("</a><br/>\n");
				out.write(this.loginBean.translate("Embedded Link"));
				out.write(": <a target='_blank' href='");
				out.write(Site.SECUREURLLINK);
				out.write("/");
				out.write(getPostAction());
				out.write("?id=");
				out.write(String.valueOf(getDisplayInstance().getId()));
				out.write("&embedded=true'>");
				out.write(Site.SECUREURLLINK);
				out.write("/");
				out.write(getPostAction());
				out.write("?id=");
				out.write(String.valueOf(getDisplayInstance().getId()));
				out.write("&embedded=true</a><br/>\n");
			}
			writeDetailsTabExtraHTML(proxy, embed, out);
			out.write("</span>\n");
			out.write("</p>\n");
			out.write("</div>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeStatsTabHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			out.write("<div id='tabs-3' class='ui-tabs-hide'>\n");
			out.write("<p>\n");
			out.write("<span>\n");
			writeStatsTabExtraHTML(proxy, embed, out);
			out.write(this.loginBean.translate("Connects"));
			out.write(": ");
			out.write(String.valueOf(getDisplayInstance().getConnects()));
			out.write(", ");
			out.write(this.loginBean.translate("today"));
			out.write(": ");
			out.write(String.valueOf(getDisplayInstance().getDailyConnects()));
			out.write(", ");
			out.write(this.loginBean.translate("week"));
			out.write(": ");
			out.write(String.valueOf(getDisplayInstance().getWeeklyConnects()));
			out.write(", ");
			out.write(this.loginBean.translate("month"));
			out.write(": ");
			out.write(String.valueOf(getDisplayInstance().getMonthlyConnects()));
			out.write("<br/>\n");		
			out.write(this.loginBean.translate("API Connects"));
			out.write(": ");
			out.write(String.valueOf(getDisplayInstance().getRestConnects()));
			out.write(", ");
			out.write(this.loginBean.translate("today"));
			out.write(": ");
			out.write(String.valueOf(getDisplayInstance().getRestDailyConnects()));
			out.write(", ");
			out.write(this.loginBean.translate("week"));
			out.write(": ");
			out.write(String.valueOf(getDisplayInstance().getRestWeeklyConnects()));
			out.write(", ");
			out.write(this.loginBean.translate("month"));
			out.write(": ");
			out.write(String.valueOf(getDisplayInstance().getRestMonthlyConnects()));
			out.write("<br/>\n");
			out.write(this.loginBean.translate("Last Connect"));
			out.write(": ");
			out.write(Utils.displayTimestamp(getDisplayInstance().getOldLastConnected()));
			if (isAdmin()) {
				out.write(" ");
				out.write(this.loginBean.translate("by"));
				out.write(" ");
				out.write(String.valueOf(getDisplayInstance().getOldLastConnectedUser()));
			}
			out.write("</span>\n");
			out.write("</p>\n");
			out.write("</div>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public boolean showAds() {
		return (!this.loginBean.isEmbedded() || this.loginBean.getShowAds())
				&& (!this.loginBean.isMobile())
				&& (!Site.COMMERCIAL)
				&& (this.loginBean.getUser() == null || this.loginBean.getUser().getType() == UserType.Basic)
				&& this.instance != null
				&& this.instance.getShowAds();
	}
	
	public void writeAd(Writer out) {
		if (!showAds() || getAdCode().isEmpty()) {
			return;
		}
		try {
			out.write("<br/>\n");
			out.write(getAdCode());
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeMenuExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		
	}
	
	public void writeMenuPostExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		
	}

	public void writeMenuButtonHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (this.instance == null) {
			return;
		}
		try {
			out.write("<span class='dropt'>\n");
			out.write("<div style='text-align:left;bottom:36px'>\n");
			out.write("<table>\n");
			writeAdminMenuItemHTML(proxy, out);
			writeMenuExtraHTML(proxy, embed, out);
			writeEditMenuItemHTML(proxy, out);
			writeDeleteMenuItemHTML(proxy, out);
			writeFlagMenuItemHTML(proxy, embed, out);
			writeChangeIconMenuItemHTML(proxy, out);
			writeResetIconMenuItemHTML(proxy, out);
			writeThumbsMenuItemsHTML(proxy, embed, out);
			writeStarMenuItemHTML(proxy, embed, out);
			writeMenuPostExtraHTML(proxy, embed, out);
			out.write("</table>\n");
			out.write("</div>\n");
			super.writeMenuButtonHTML(proxy, embed, out);
			out.write("</span>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void writeIconHTML(SessionProxyBean proxy, Writer out) {
		try {
			out.write("<div style='float:right'>\n");
			out.write("<span class='dropt'>\n");
			out.write("<div style='right:4px'>\n");
			if (isAdmin()) {
				out.write("<a onclick='return changeIcon()' href='#' style='text-decoration:none' title=\"");
				out.write(this.loginBean.translate("Change the " + Utils.camelCaseToLowerCase(getDisplayName())
						+ "'s display icon to a new image"));
				out.write("\">\n");
				out.write("<img src='");
				out.write(getAvatarImage(getDisplayInstance()));
				out.write("' class='big-icon'/>\n");
				out.write("</a>\n");
			} else {
				out.write("<img src='");
				out.write(getAvatarImage(getDisplayInstance()));
				out.write("' class='big-icon'/>\n");
			}
			out.write("</div>\n");
			if (isAdmin()) {
				out.write("<a onclick='return changeIcon()' href='#' style='text-decoration:none' title=\"");
				out.write(this.loginBean.translate("Change the " + Utils.camelCaseToLowerCase(getDisplayName())
						+ "'s display icon to a new image"));
				out.write("\">\n");
				out.write("<img src='");
				out.write(getAvatarImage(getDisplayInstance()));
				out.write("' class='small-icon'/>\n");
				out.write("</a>\n");
			} else {
				out.write("<img src='");
				out.write(getAvatarImage(getDisplayInstance()));
				out.write("' class='small-icon'/>\n");
			}
			out.write("</span></div>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeAdminMenuItemHTML(SessionProxyBean proxy, Writer out) {
		if (this.instance == null || this.instance.isExternal() || !isAdmin()) {
			return;
		}
		try {
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='");
			out.write(getPostAction());
			out.write("?admin");
			out.write(proxy.proxyString());
			out.write(instanceString());
			out.write("' title=\"");
			out.write(this.loginBean.translate("Administer the " + Utils.camelCaseToLowerCase(getDisplayName()) + "'s user access and configuration"));
			out.write("\"><img src='images/admin.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Admin Console"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeEditMenuItemHTML(SessionProxyBean proxy, Writer out) {
		if (this.instance == null || !(isAdmin() || isSuper())) {
			return;
		}
		try {
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='");
			out.write(getPostAction());
			out.write("?edit-instance");
			out.write(proxy.proxyString());
			out.write(instanceString());
			out.write("' title=\"");
			out.write(this.loginBean.translate("Edit the " + Utils.camelCaseToLowerCase(getDisplayName())
					+ "'s detail information such as its license and description"));
			out.write("\"><img src='images/edit.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Edit Details"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeAdminButtonHTML(SessionProxyBean proxy, Writer out) {
		if (this.instance == null || this.instance.isExternal() || !isAdmin()) {
			return;
		}
		try {
			out.write("<a href='");
			out.write(getPostAction());
			out.write("?admin");
			out.write(proxy.proxyString());
			out.write(instanceString());
			out.write("' title=\"");
			out.write(this.loginBean.translate("Administer the " + Utils.camelCaseToLowerCase(getDisplayName()) + "'s user access and configuration"));
			out.write("\"><img src='images/admin.svg' class='toolbar'/></a>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeChangeIconMenuItemHTML(SessionProxyBean proxy, Writer out) {
		if (this.instance == null || !isAdmin()) {
			return;
		}
		try {
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' onclick='return changeIcon()' href='#'");
			out.write("' title=\"");
			out.write(this.loginBean.translate("Change the " + Utils.camelCaseToLowerCase(getDisplayName()) + "'s display icon to a new image"));
			out.write("\"><img src='images/icon.jpg' class='menu'/> ");
			out.write(this.loginBean.translate("Change Icon"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeResetIconMenuItemHTML(SessionProxyBean proxy, Writer out) {
		if (this.instance == null || !isAdmin()) {
			return;
		}
		try {
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='");
			out.write(getPostAction());
			out.write("?reset-icon");
			out.write(this.loginBean.postTokenString());
			out.write(proxy.proxyString());
			out.write(instanceString());
			out.write("' title=\"");
			out.write(this.loginBean.translate("Reset the " + Utils.camelCaseToLowerCase(getDisplayName()) + "'s display icon to its default image"));
			out.write("\"><img src='images/empty.png' class='menu'/> ");
			out.write(this.loginBean.translate("Reset Icon"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
		
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeChangeIconFormHTML(SessionProxyBean proxy, Writer out) {
		if (this.instance == null || !isAdmin()) {
			return;
		}
		try {
			out.write("<form id='icon-upload-form' action='");
			out.write(getPostAction());
			out.write("-icon-upload' method='post' enctype='multipart/form-data' style='display:none'>\n");
			out.write(this.loginBean.postTokenInput());
			out.write(proxy.proxyInput());
			out.write(instanceInput());
			out.write("</form>\n");
			out.write("<script>var changeIcon = function () { SDK.application = '");
			out.write(String.valueOf(AdminDatabase.getTemporaryApplicationId()));
			out.write("'; return GraphicsUploader.openUploadDialog(document.getElementById('icon-upload-form'), 'Change Icon'); }</script>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void checkCategory(Category category) {
		Category channelCategory = null;
		try {
			channelCategory = AdminDatabase.instance().findCategory(category.getName(), getTypeName(), category.getDomain());
		} catch (Exception exception) {}
		if (channelCategory == null) {
			for (Category parent : category.getParents()) {
				checkCategory(parent);
			}
			channelCategory = category.clone();
			channelCategory.setAvatar(null);
			channelCategory.setId(0);
			channelCategory.setType(getTypeName());
			channelCategory.setParents(new ArrayList<Category>());
			channelCategory.setChildren(new ArrayList<Category>());
			channelCategory = AdminDatabase.instance().createCategory(channelCategory, channelCategory.getCreator(), category.getParentsString());
			if (category.getAvatar() != null) {
				AdminDatabase.instance().updateCategory(channelCategory.getId(), category.getAvatar().getImage());
			}
		}
	}

	public String getAdCode() {
		if (this.instance != null) {
			if (this.instance.hasAdCode()) {
				if (this.instance.isAdCodeVerified()) {
					return this.instance.getAdCode();
				}
				return Utils.sanitize(this.instance.getAdCode());
			}
			if (this.instance.getCreator().hasAdCode()) {
				if (this.instance.getCreator().isAdCodeVerified()) {
					return this.instance.getCreator().getAdCode();
				}
				return Utils.sanitize(this.instance.getCreator().getAdCode());
			}
			DomainBean domainBean = loginBean.getBean(DomainBean.class);
			if (domainBean.getInstance() != null) {
				if (domainBean.getInstance().isAdCodeVerified()) {
					return domainBean.getInstance().getAdCode();
				}
				return Utils.sanitize(domainBean.getInstance().getAdCode());
			}
		}
		return "";
	}
	
	public String getDisplayInstanceName() {
		if (this.displayInstance == null) {
			return "";
		}
		return this.displayInstance.getName();
	}
	
	public void setSubdomain(String subdomain, WebMedium instance) {
		if (subdomain == null) {
			return;
		}
		if (!Utils.isAlphaNumeric(subdomain)) {
			throw new BotException("Invalid character in subdomain, only use alpha-numeric characters");
		}
		if (!subdomain.isEmpty() && !subdomain.equalsIgnoreCase(this.instance.getSubdomain())) {
			subdomain = subdomain.toLowerCase();
			if (this.instance.getDomainForwarder() == null) {
				DomainForwarder forwarder = new DomainForwarder(instance);
				if (isLoggedIn() && getUser().getApplicationId() == null) {
					getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
				}
				forwarder.setForwarderAddress(forwarder.getForwarderAddress() + "&application=" + getUser().getApplicationId());
				instance.setDomainForwarder(forwarder);
			}
			if (subdomain.indexOf('.') == -1) {
				instance.getDomainForwarder().setSubdomain(subdomain);
				instance.getDomainForwarder().setDomain(null);
			} else {
				instance.getDomainForwarder().setDomain(subdomain);
				instance.getDomainForwarder().setSubdomain(null);
			}
			DomainForwarder forwarder = AdminDatabase.instance().findDomainForwarder(subdomain);
			if (forwarder != null) {
				throw new BotException("This domain or subdomain is already in use by another instance - " + subdomain);
			}
		} else if (subdomain.isEmpty() && this.instance.getDomainForwarder() != null) {
			instance.setDomainForwarder(null);
		}
	}
	
	public void setSubdomain(String subdomain, EmbedWebMediumBean embed) {
		checkLogin();
		checkInstance();
		checkAdmin();
		WebMedium newInstance = (WebMedium)this.instance.clone();
		subdomain = subdomain.toLowerCase();
		if (!subdomain.equals(this.instance.getSubdomain())) {
			DomainForwarder forwarder = AdminDatabase.instance().findDomainForwarder(subdomain);
			if (forwarder != null) {
				throw new BotException("This domain or subdomain is already in use by another instance - " + subdomain);
			}
		}
		if (!Utils.isAlphaNumeric(subdomain)) {
			throw new BotException("Invalid character in subdomain, only use alpha-numeric characters");
		}
		if (subdomain.length() <= 2) {
			throw new BotException("Subdomain must be greater than 2 characters");
		}
		if (this.instance.getDomainForwarder() == null) {
			newInstance.setDomainForwarder(new DomainForwarder(newInstance));
		}
		if (subdomain.indexOf('.') == -1) {
			newInstance.getDomainForwarder().setSubdomain(subdomain);
			newInstance.getDomainForwarder().setDomain(null);
		} else {
			newInstance.getDomainForwarder().setDomain(subdomain);
			newInstance.getDomainForwarder().setSubdomain(null);
		}
		newInstance.getDomainForwarder().setForwarderAddress(embed.getEmbedURL(false));
		setInstance((T)AdminDatabase.instance().updateForwarder(newInstance));
	}

	@Override
	public List<T> getAllInstances() {
		if (getUser() != null && (getUser().isSuperUser() || getUser().isAdminUser()) && getDomain().getAlias().equals(Site.DOMAIN)) {
			// Let super see all instances of search.
			return getAllInstances(null);
		}
		return getAllInstances(getDomain());
	}

	public List<T> getAllDomainInstances() {
		return getAllInstances(getDomain());
	}
	
	public abstract List<T> getAllInstances(Domain domain);

	public List<BotInstance> getBots() {
		try {
			return AdminDatabase.instance().getAllInstances(
						0, this.pageSize, "", "", getUserId(), InstanceFilter.Personal, InstanceRestrict.None, InstanceSort.Name, ContentRating.Adult, "", getUser(), null, false);
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<BotInstance>();
		}
	}
	
	public void checkAdmin() {
		if (!isSuper() && Site.READONLY) {
			throw new BotException("This website is currently undergoing maintence, please try later.");
		}
		if (!isAdmin()) {
			throw new BotException("Must be admin user");
		}
	}
	
	public void checkAdminOrSuper() {
		if (isSuper()) {
			return;
		}
		if (Site.READONLY) {
			throw new BotException("This website is currently undergoing maintence, please try later.");
		}
		if (!isAdmin()) {
			throw new BotException("Must be admin user");
		}
	}

	public boolean isAdmin() {
		if (!isLoggedIn()) {
			return false;
		}
		if (getUser() == null || this.instance == null) {
			return false;
		}
		return this.instance.isAdmin(getUser());
	}

	public boolean isMember() {
		if (!isLoggedIn()) {
			return false;
		}
		if (getUser() == null || this.instance == null) {
			return false;
		}
		return this.instance.isAdmin(getUser()) || this.instance.isUser(getUser());
	}

	public boolean isCorrectionAllow() {
		Bot bot = getBot();
		if (bot == null) {
			return false;
		}
		CorrectionMode correctionMode = bot.mind().getThought(Language.class).getCorrectionMode();
		if (correctionMode == CorrectionMode.Disabled) {
			return false;
		} else if (!isAdmin() && (correctionMode == CorrectionMode.Administrators)) {
			return false;					
		} else if (!isLoggedIn() && (correctionMode == CorrectionMode.Users)) {
			return false;
		}
		return true;
	}

	public boolean isValidUser() {
		if (this.instance == null) {
			return true;
		}
		return this.instance.isAllowed(getUser());
	}

	public String isAccessModeSelected(String type) {
		AccessMode mode = AccessMode.Everyone;
		if (getEditInstance() != null) {
			mode = getEditInstance().getAccessMode();
		}
		if (mode.name().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

	public String isForkAccessModeSelected(String type) {
		AccessMode mode = AccessMode.Everyone;
		if (getEditInstance() != null) {
			mode = getEditInstance().getForkAccessMode();
		}
		if (mode.name().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

	public String isContentRatingSelected(String type) {
		ContentRating mode = ContentRating.Teen;
		if (getEditInstance() != null) {
			mode = getEditInstance().getContentRating();
		}
		if (mode.name().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

	public boolean validateInstance(String instance, Domain domain) {
		return validateInstance(instance, domain, false);
	}
	
	public boolean validateInstance(String instance, Domain domain, boolean force) {
		try {
			long id = 0;
			try {
				id = Long.valueOf(instance);
			} catch (NumberFormatException exception) {}
			setInstance(null);
			if (id == 0) {
				setInstance((T)AdminDatabase.instance().validate((Class<WebMedium>)getType(), instance, getUserId(), domain));
			} else {
				setInstance((T)AdminDatabase.instance().validate((Class<WebMedium>)getType(), id, getUserId()));
			}
			if (this.instance.isTemplate() && !isSuper()) {
				throw new BotException("Invalid access to template instance.");
			}
			if (!force) {
				this.instance.checkAccess(getUser());
			}
		} catch (Exception failed) {
			if ((this.instance != null) && (this.instance.isPrivate())) {
				this.displayInstance = null;
			}
			this.instance = null;
			error(failed);
			return false;
		}
		return true;
	}

	public boolean validateInstance(String instance) {
		return validateInstance(instance, getDomain(), false);
	}
	
	public boolean validateInstance(String instance, boolean force) {
		return validateInstance(instance, getDomain(), force);
	}

	/**
	 * Lets the bean record stats for an API access.
	 */
	public boolean apiConnect() {
		return false;
	}
	
	public boolean incrementConnects(ClientType type) {
		if (this.instance == null) {
			error(new BotException("Missing " + getTypeName()));
			return false;
		}
		try {
			setInstance((T)AdminDatabase.instance().incrementConnects(this.instance, type, getUser()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public String getAvatarThumb(WebMedium instance) {
		if ((instance != null) && (!instance.isFlagged() || isSuper())) {
			return getAvatarThumb(instance.getAvatar());
		}
		return getAvatarThumb((AvatarImage)null);
	}

	public String getAvatarImage(WebMedium instance) {
		if ((instance != null) && (!instance.isFlagged() || isSuper())) {
			return getAvatarImage(instance.getAvatar());
		}
		return getAvatarImage((AvatarImage)null);
	}

	public boolean deleteInstance(boolean confirm) {
		try {
			checkInstance();
			if (!confirm) {
				throw new BotException("Must check 'I'm sure'");
			}
			if (this.loginBean.validateUser(getUser().getUserId(), getUser().getPassword(), getUser().getToken(), false, false) == 0) {
				return false;
			}
			T instance = (T)AdminDatabase.instance().validate(this.instance.getClass(), this.instance.getId(), getUser().getUserId());
			checkAdminOrSuper();
			AdminDatabase.instance().delete(instance);
			this.instance = null;
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
		
	}
	
	/**
	 * Reset the icon to null.
	 */
	public boolean resetIcon() {
		try {
			checkLogin();
			checkInstance();
			checkAdminOrSuper();
			setInstance(AdminDatabase.instance().resetIcon(this.instance));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Update the icon.
	 */
	public boolean update(byte[] image) {
		try {
			checkLogin();
			checkInstance();
			checkAdminOrSuper();
			setInstance(AdminDatabase.instance().update(this.instance, image));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean addUser(String userid) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			if ((userid == null) || userid.isEmpty()) {
				throw new BotException("Please select a user");
			}
			setInstance((T)AdminDatabase.instance().addUser(this.instance, userid));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean removeUser(String userid) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			if ((userid == null) || userid.isEmpty()) {
				throw new BotException("Please select a user");
			}
			setInstance((T)AdminDatabase.instance().removeUser(this.instance, userid));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean addAdmin(String userid) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			if ((userid == null) || userid.isEmpty()) {
				throw new BotException("Please select a user");
			}
			setInstance((T)AdminDatabase.instance().addAdmin(this.instance, userid));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean removeAdmin(String userid) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			if ((userid == null) || userid.isEmpty()) {
				throw new BotException("Please select a user");
			}
			setInstance((T)AdminDatabase.instance().removeAdmin(this.instance, userid));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean editInstance(long id) {
		try {
			checkLogin();
			checkAdminOrSuper();
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean adminInstance(long id) {
		try {
			checkLogin();
			setInstance((T)AdminDatabase.instance().validate((Class<WebMedium>)getType(), id, getUser().getUserId()));
			checkAdmin();
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	public String categoryHeaderHTML() {
		if (getCategory() == null) {
			return "";
		}
		String browse = getBrowseAction();
		StringWriter writer = new StringWriter();
		writer.write("<div id='admin-topper' align='left'>\n");
		writer.write("<div class='clearfix'>\n");
		writer.write("<a class='menu' href='" + browse + "?category=root'>");
		writer.write("<span>Browse</span>");
		writer.write("</a> : \n");
		if (!getCategory().getParents().isEmpty()) {
			for (Category category : getCategory().getParents()) {
				writer.write("<a class='menu' href='" + browse + "?category=" + encodeURI(category.getName()) + "'>");
				writer.write("<span>" + category.getName() + "</span>");
				writer.write("</a>");
				writer.write(" : ");
			}
		}
		writer.write("<a class='menu' href='" + browse + "?category=" + encodeURI(getCategory().getName()) + "'>");
		writer.write("<span>" + getCategory().getName() + "</span>");
		writer.write("</a>");
		writer.write("</div>\n");
		writer.write("</div>\n");
		return writer.toString();
	}
	
	public void browseBannerHTML(Writer writer, SessionProxyBean proxy) {
		if (getDisplayInstance() == null) {
			return;
		}
		try {
			String browse = getBrowseAction();
			writer.write("<div id='admin-topper' align='left'><div class='clearfix'><a class='menu' href='");
			writer.write(browse);
			if (this.loginBean.getPageType() == Page.Search) {
				writer.write("?browse-type=" + getTypeName() + "&search=true'><span>Search</span></a>");
			} else {
				writer.write("?category=root'><span>Browse</span></a>");
				if (!getDisplayInstance().getCategories().isEmpty()) {
					writer.write(" : ");
					int count = 0;
					for (Iterator<Category> iterator = getDisplayInstance().getCategories().iterator(); iterator.hasNext() && count < 3; ) {
						Category category = iterator.next();
						if (!category.getChildren().isEmpty() && !Collections.disjoint(getDisplayInstance().getCategories(), category.getChildren())) {
							continue;
						}
						if (count > 0) {
							writer.write(" : ");
						}
						count++;
						writer.write("<a class='menu' href='");
						writer.write(browse);
						writer.write("?category=");
						writer.write(Utils.encodeURL(category.getName()));
						writer.write("'><span>");
						writer.write(category.getName());
						writer.write("</span></a>");
					}
				}
			}
			writer.write(" : <a href='");
			writer.write(browse);
			writer.write("?id=");
			writer.write(String.valueOf(getDisplayInstance().getId()));
			writer.write(proxy.proxyString());
			writer.write("'>");
			writer.write(getDisplayInstance().getName());
			writer.write("</a></div> </div>");
		} catch (IOException exception) {
			error(exception);
		}
	}

	public String browseCategoriesHTML() {
		String browse = getBrowseAction();
		StringWriter writer = new StringWriter();
		if (getCategory() != null) {
			writer.write("<h1><img src='");
			writer.write(getAvatarImage(getCategory()));
			writer.write("' class='admin-banner-pic'> ");
			writer.write(this.loginBean.translate(getCategory().getName()));
			writer.write("</h1>\n");
			writer.write("<p>" + this.loginBean.translate(getCategory().getDescription()) + "</p>\n");
		}
		List<Category> categories = getChildCategories();
		if (!categories.isEmpty()) {
			writer.write("<h3>");
			writer.write(loginBean.translate("Categories"));
			writer.write("</h3>\n");
		}
		int count = 0;
		int max = loginBean.isMobile() ? 8 : 20;
		for (Category category : categories) {
			count++;
			if (!getShowAllCategories() && count > max) {
				break;
			}
			writer.write("<div class='browse-div'>\n");
			if (!loginBean.isMobile()) {
				writer.write("<span class='dropt'>\n");
			}
			writer.write("<table style='border-style:solid;border-color:grey;border-width:1px'>\n");
			writer.write("<tr><td class='category-thumb' align='center' valign='middle'>\n");
			writer.write("<a href='" + browse + "?category=" + encodeURI(category.getName()) + "'>");
			writer.write("<img class='category-thumb' src='" + getAvatarThumb(category) + "' alt='" + category.getName() + "' />");
			writer.write("</a>\n");
			writer.write("</td></tr>\n");
			writer.write("</table>\n");
			if (!loginBean.isMobile()) {
				writer.write("<div style='text-align:left'>\n");
				writer.write("<a class='menu' href='" + browse + "?category=" + encodeURI(category.getName()) + "'>");
				writer.write("<b><span class='category-thumb'>" + category.getName() + "</span></b>");
				writer.write("</a><br/>\n");
				writer.write("<span>" + this.loginBean.translate(category.getDescription()) + "</span>\n");
				writer.write("</div>\n");
			}
			if (!loginBean.isMobile()) {
				writer.write("</span>\n");
			}
			writer.write("<div class='category-thumb'>\n");
			writer.write("<a class='menu' href='" + browse + "?category=" + encodeURI(category.getName()) + "'>");
			writer.write("<span>" + this.loginBean.translate(category.getName()) + "</span>");
			writer.write("</a>\n");
			writer.write("</div>\n");
			writer.write("</div>\n");
		}
		if (!getShowAllCategories() && (count > max)) {
			writer.write("<br/><a class='menu' href='" + browse + "?category=more'><span>");
			writer.write(this.loginBean.translate("more"));
			writer.write("</span></a>\n");
		} else if (getShowAllCategories() && (count > max)) {
			writer.write("<br/><a class='menu' href='" + browse + "?category=less'><span>");
			writer.write(this.loginBean.translate("less"));
			writer.write("</span></a>\n");
		}
		writer.write("<br/><form action='" + browse + "' method='post' class='message' style='display:inline'>\n");
		writer.write(this.loginBean.postTokenInput());
		if (getDomain().isCreationAllowed(this.loginBean.getUser()) && this.loginBean.getDomain().isAdmin(getUser())) {
			writer.write("<input name='create-category' type='submit' value='");
			writer.write(this.loginBean.translate("New Category"));
			writer.write("'/>\n");
		}
		if (getCategory() != null && (this.loginBean.isAdmin() || getCategory().getCreator().equals(this.loginBean.getUser()))) {
			writer.write("<input name='edit-category' type='submit' value='");
			writer.write(this.loginBean.translate("Edit Category"));
			writer.write("'/>\n");
		}
		if (getCategory() != null && (this.loginBean.isAdmin() || getCategory().getCreator().equals(this.loginBean.getUser()))) {
			writer.write("<input name='delete-category' type='submit' value='");
			writer.write(this.loginBean.translate("Delete Category"));
			writer.write("'/>\n");
		}
		writer.write("</form>\n");
		return writer.toString();
	}

	public String browseFeaturedHTML() {
		StringWriter writer = new StringWriter();
		List<T> instances = getAllFeaturedInstances();
		if (!instances.isEmpty()) {
			writer.write("<br/>\n");
			writer.write("<h3>");
			writer.write(loginBean.translate("Featured"));
			writer.write(" " + getDisplayName() + "s</h3>\n");
			for (T instance : instances) {
				writeBrowseThumb(writer, instance, true);
			}
			writer.write("<br/>");
		}
		return writer.toString();
	}
	
	public String browseHTML() {
		StringWriter writer = new StringWriter();
		resetSearch();
		setCategoryFilter(getCategoryString());
		List<T> instances = getAllDomainInstances();
		if (getCategory() != null && getCategory().getCount() != getResultsSize()) {
			setCategory(AdminDatabase.instance().updateCategoryCount(getCategory(), getResultsSize()));
		}
		
		writer.write("<span class='menu'>");
		writer.write(getResultsSize() + " ");
		writer.write(loginBean.translate("results"));
		writer.write(".<br/>");
		writePagingString(writer, instances);
		writer.write("</span>");

		writer.write("<br/>");
		for (T instance : instances) {
			writeBrowseThumb(writer, instance, getDisplayOption() == DisplayOption.Grid);
		}
		writer.write("<br/>");
		
		writer.write("<span class = menu>");
		writePagingString(writer, instances);
		writer.write("</span>");
		
		return writer.toString();
	}
	
	public String searchHTML() {
		StringWriter writer = new StringWriter();
		List<T> instances = getAllSearchInstances();
		
		writer.write("<span class='menu'>");
		writer.write(getResultsSize() + " ");
		writer.write(loginBean.translate("results"));
		writer.write(".<br/>");
		writePagingString(writer, instances);
		writer.write("</span>");

		writer.write("<br/>");
		for (T instance : instances) {
			writeBrowseThumb(writer, instance, getDisplayOption() == DisplayOption.Grid);
		}
		writer.write("<br/>");
		
		writer.write("<span class = menu>");
		writePagingString(writer, instances);
		writer.write("</span>");
		
		return writer.toString();
	}
	
	public void writeBrowseStats(StringWriter writer, T instance) {
		
	}
	
	public void writeSearchOptions(StringWriter writer) {
		
	}
	
	public void writeRestrictOptions(StringWriter writer) {
		
	}
	
	public void writeSearchFields(StringWriter writer) {
		
	}
	
	public void writeBrowseLink(StringWriter writer, T instance, boolean bold) {
		if (instance.isFlagged()) {
			writer.write("<a class='menu' href='" + getBrowseAction() + "?id=" + instance.getId() + "'>");
			writer.write("<span style='color:red;" + (bold ? "font-weight:bold' class='browse-thumb'" : "margin: 0 0 0;'" ) + ">" + instance.getName() + "</span>");
			writer.write("</a>\n");
		} else {
			writer.write("<a class='menu' href='" + getBrowseAction() + "?id=" + instance.getId() + "'>");
			writer.write("<span " + (bold ? "class='browse-thumb' style='font-weight:bold'" : "style='margin: 0 0 0;'" ) + ">" + instance.getNameHTML() + "</span>");
			writer.write("</a>\n");
		}
	}
	
	public void writeBrowseImage(StringWriter writer, T instance) {
		writer.write("<a href='" + getBrowseAction() + "?id=" + instance.getId() + "'>");
		writer.write("<img class='browse-thumb' src='" + getAvatarThumb(instance) + "' alt='" + instance.getName() + "'/>");
		writer.write("</a>\n");
	}
	
	public void writeBrowseThumb(StringWriter writer, T instance, boolean grid) {
		if (grid) {
			writer.write("<div class='browse-div'>\n");
		} else {
			writer.write("<div class='browse-list-div'>\n");
		}
		try {
			if (grid) {
				if (!this.loginBean.isMobile()) {
					writer.write("<span class='dropt'>\n");
				}
				writer.write("<table style='border-style:solid;border-color:grey;border-width:1px'>\n");
				writer.write("<tr><td class='browse-thumb' align='center' valign='middle'>\n");
			} else {
				writer.write("<td class='browse-thumb' align='center' valign='top'>");
			}
			writeBrowseImage(writer, instance);
			writer.write("</td>");
			if (grid) {
				writer.write("</tr>\n</table>\n");
			} else {
				writer.write("<td style='border-style:solid;border-color:grey;border-width:1px'>");
			}
			if (!grid || !this.loginBean.isMobile()) {
				writer.write("<div style='text-align:left'>\n");
				writeBrowseLink(writer, instance, true);
				writer.write("<br/>\n");
				writer.write(instance.getDescription() + "<br/>\n");
				writer.write("<span class='details'>\n");
				writer.write(this.loginBean.translate("Alias"));
				writer.write(": @" + instance.getAlias() + "<br/>\n");
				writer.write(this.loginBean.translate("Categories"));
				writer.write(": " + instance.getCategoriesString() + "<br/>\n");
				if (!instance.getTags().isEmpty()) {
					writer.write("Tags: " + instance.getTagsString() + "<br/>\n");
				}
				if ((instance.getDomain() != null) && !instance.getDomain().equals(getDomain())) {
					writer.write(this.loginBean.translate("Workspace"));
					writer.write(": " + instance.getDomain().getName() + "<br/>\n");
				}
				writer.write(this.loginBean.translate("Created"));
				writer.write(": " + Utils.displayDate(instance.getCreationDate()) + ", by: ");
				writer.write(instance.getCreatorUserId() + "<br/>\n");
				writer.write(this.loginBean.translate("Thumbs up"));
				writer.write(": " + instance.getThumbsUp() + ", ");
				writer.write(this.loginBean.translate("thumbs down"));
				writer.write(": " + instance.getThumbsDown() + ", ");
				writer.write(this.loginBean.translate("stars"));
				writer.write(": " + Utils.truncate(instance.getStars()) + "<br/>\n");
				writeBrowseStats(writer, instance);
				writer.write(this.loginBean.translate("Connects"));
				writer.write(": " + instance.getConnects());
				writer.write(", ");
				writer.write(this.loginBean.translate("today"));
				writer.write(": " + instance.getDailyConnects());
				writer.write(", ");
				writer.write(this.loginBean.translate("week"));
				writer.write(": " + instance.getWeeklyConnects());
				writer.write(", ");
				writer.write(this.loginBean.translate("month"));
				writer.write(": " + instance.getMonthlyConnects() + "<br/>");
				/*writer.write(this.loginBean.translate("API Connects"));
				writer.write(": " + instance.getRestConnects() + ", ");
				writer.write(this.loginBean.translate("today"));
				writer.write(": " + instance.getRestDailyConnects() + ", ");
				writer.write(this.loginBean.translate("week"));
				writer.write(": " + instance.getRestWeeklyConnects() + ", ");
				writer.write(this.loginBean.translate("month"));
				writer.write(": " + instance.getRestMonthlyConnects() + "<br/>\n");*/
				writer.write(this.loginBean.translate("Last Connect"));
				writer.write(": " + Utils.displayTimestamp(instance.getOldLastConnected()));
				if (instance.isFlagged()) {
					writer.write("<br/><span style='color:red;font-weight:bold;'>");
					writer.write(this.loginBean.translate("This " + Utils.camelCaseToLowerCase(getDisplayName()) + " is flagged."));
					writer.write("</span>\n");
				}
				writer.write("</span>\n");
				writer.write("</div>\n");
			}
			if (grid) {
				if (!this.loginBean.isMobile()) {
					writer.write("</span>\n");
				}
				writer.write("<div class='browse-thumb'>\n");
				writeBrowseLink(writer, instance, false);
				writer.write("</div>\n");
			} else {
				writer.write("</td>\n");
			}
		} catch (Exception exception) {
			writer.write(instance.getId().toString());
		}
		writer.write("</div>\n");
	}

	public String searchFormHTML() {
		StringWriter writer = new StringWriter();
		writer.write("<form action='" + getBrowseAction() + "' method='get' class='search'>\n");
		writer.write("<span class='menu'>\n");
		writer.write("<input type='radio' name='instance-filter' ");
		writer.write(getInstanceFilterCheckedString(InstanceFilter.Public));
		writer.write(" title='Show all public " + Utils.camelCaseToLowerCase(getDisplayName()) + "s create by all users' value='public' onClick='this.form.submit()'>");
		writer.write(this.loginBean.translate("public " + Utils.camelCaseToLowerCase(getDisplayName()) + "s"));
		writer.write("</input>\n");
		writer.write("<input type='radio' name='instance-filter' ");
		writer.write(getInstanceFilterCheckedString(InstanceFilter.Private));
		writer.write(" title='Show all private " + Utils.camelCaseToLowerCase(getDisplayName()) + "s this user has access to' value='private' onClick='this.form.submit()'>");
		writer.write(this.loginBean.translate("private " + Utils.camelCaseToLowerCase(getDisplayName()) + "s"));
		writer.write("</input>\n");
		writer.write("<input type='radio' name='instance-filter' ");
		writer.write(getInstanceFilterCheckedString(InstanceFilter.Personal));
		writer.write(" title='Show all " + Utils.camelCaseToLowerCase(getDisplayName()) + "s this user is the administrator for' value='personal' onClick='this.form.submit()'>");
		writer.write(this.loginBean.translate("my " + Utils.camelCaseToLowerCase(getDisplayName()) + "s"));
		writer.write("</input>\n");
		writer.write("<br/>\n");
		
		writer.write("<div class='search-div'>\n");
		writer.write("<span class='search-span'>Name</span>\n");
		writer.write("<input id='searchtext' name='name-filter' type='text' value='" + getNameFilter() + "' title='Filter by any name containing the text' /></td>\n");
		writer.write("</div>\n");
		
		writer.write("<div class='search-div'>\n");
		writer.write("<span class='search-span'>");
		writer.write(this.loginBean.translate("Categories"));
		writer.write("</span>\n");
		writer.write("<input id='categories' name='category-filter' type='text' value='" + getCategoryFilter() + "' title='Filter by a comma seperated list of category names' onfocus='this.searchfocus = true;' onmouseup='if(this.searchfocus) {this.select(); this.searchfocus = false;}'/></td>\n");
		writer.write("<script>\n");
		writer.write("$( '#categories' ).autocomplete({ source: [" + getAllCategoriesString() + "], minLength: 0 }).on('focus', function(event) { var self = this; $(self).autocomplete('search', ''); });");
		writer.write("</script>\n");
		writer.write("</div>\n");
		writer.write("<div class='search-div'>\n");
		writer.write("<span class='search-span'>");
		writer.write(this.loginBean.translate("Tags"));
		writer.write("</span>\n");
		writer.write("<input id='tags' name='tag-filter' type='text' value='" + getTagFilter() + "' title='Filter by a comma seperated list of tag names' onfocus='this.searchfocus = true;' onmouseup='if(this.searchfocus) {this.select(); this.searchfocus = false;}'/></td>\n");
		writer.write("<script>\n");
		writer.write("$( '#tags' ).autocomplete({ source: [" + getAllTagsString() + "], minLength: 0 }).on('focus', function(event) { var self = this; $(self).autocomplete('search', ''); });");
		writer.write("</script>\n");
		writer.write("</div>\n");
		writeSearchFields(writer);
		writer.write("<div class='search-div'>\n");
		writer.write("<span class='search-span'>");
		writer.write(this.loginBean.translate("Display"));
		writer.write("</span>\n");
		writer.write("<select id='searchselect' name='display' onchange='this.form.submit()'>\n");
		writer.write("<option value='grid' " + getDisplayCheckedString(DisplayOption.Header) + ">");
		writer.write(this.loginBean.translate("Grid"));
		writer.write("</option>\n");
		writer.write("<option value='details' " + getDisplayCheckedString(DisplayOption.Details) + ">");
		writer.write(this.loginBean.translate("List"));
		writer.write("</option>\n");
		writer.write("</select>\n");
		writer.write("</div>\n");

		if (this.loginBean.isAdmin()) {
			writer.write("<div class='search-div'>\n");
			writer.write("<span class='search-span'>");
			writer.write(this.loginBean.translate("Restrict"));
			writer.write("</span>\n");
			writer.write("<select id='searchselect' name='instance-restrict' onchange='this.form.submit()'>\n");
			writer.write("<option value='None' " + getInstanceRestrictCheckedString(InstanceRestrict.None) + "></option>\n");
			writeRestrictOptions(writer);
			writer.write("<option value='Website' " + getInstanceRestrictCheckedString(InstanceRestrict.Website) + ">");
			writer.write(this.loginBean.translate("has website"));
			writer.write("</option>\n");
			writer.write("<option value='Subdomain' " + getInstanceRestrictCheckedString(InstanceRestrict.Subdomain) + ">");
			writer.write(this.loginBean.translate("has subdomain"));
			writer.write("</option>\n");
			writer.write("<option value='Link' " + getInstanceRestrictCheckedString(InstanceRestrict.Link) + ">");
			writer.write(this.loginBean.translate("external link"));
			writer.write("</option>\n");
			if (!Site.COMMERCIAL) {
				writer.write("<option value='Diamond' " + getInstanceRestrictCheckedString(InstanceRestrict.Diamond) + ">");
				writer.write(this.loginBean.translate("Diamond"));
				writer.write("</option>\n");
				writer.write("<option value='Platinum' " + getInstanceRestrictCheckedString(InstanceRestrict.Platinum) + ">");
				writer.write(this.loginBean.translate("Platinum"));
				writer.write("</option>\n");
				writer.write("<option value='Gold' " + getInstanceRestrictCheckedString(InstanceRestrict.Gold) + ">");
				writer.write(this.loginBean.translate("Gold"));
				writer.write("</option>\n");
				writer.write("<option value='Bronze' " + getInstanceRestrictCheckedString(InstanceRestrict.Bronze) + ">");
				writer.write(this.loginBean.translate("Bronze"));
				writer.write("</option>\n");
			}
			if (isSuper()) {
				writer.write("<option value='Hidden' " + getInstanceRestrictCheckedString(InstanceRestrict.Hidden) + ">");
				writer.write(this.loginBean.translate("hidden"));
				writer.write("</option>\n");
				writer.write("<option value='Ad' " + getInstanceRestrictCheckedString(InstanceRestrict.Ad) + ">");
				writer.write(this.loginBean.translate("AD code"));
				writer.write("</option>\n");
				writer.write("<option value='AdUnverified' " + getInstanceRestrictCheckedString(InstanceRestrict.AdUnverified) + ">");
				writer.write(this.loginBean.translate("AD unverified"));
				writer.write("</option>\n");
				writer.write("<option value='Flagged' " + getInstanceRestrictCheckedString(InstanceRestrict.Flagged) + ">");
				writer.write(this.loginBean.translate("flagged"));
				writer.write("</option>\n");
				writer.write("<option value='Icon' " + getInstanceRestrictCheckedString(InstanceRestrict.Icon) + ">");
				writer.write(this.loginBean.translate("has icon"));
				writer.write("</option>\n");
				writer.write("<option value='Adult' " + getInstanceRestrictCheckedString(InstanceRestrict.Adult) + ">");
				writer.write(this.loginBean.translate("adult"));
				writer.write("</option>\n");
			}
			writer.write("</select>\n");
			writer.write("</div>\n");
		}
		
		writer.write("<div class='search-div'>\n");
		writer.write("<span class='search-span'>");
		writer.write(this.loginBean.translate("Sort"));
		writer.write("</span>\n");
		writer.write("<select id='searchselect' name='instance-sort' onchange='this.form.submit()'>\n");
		writer.write("<option value='Name' " + getInstanceSortCheckedString(InstanceSort.Name) + ">");
		writer.write(this.loginBean.translate("name"));
		writer.write("</option>\n");
		writer.write("<option value='Date' " + getInstanceSortCheckedString(InstanceSort.Date) + ">");
		writer.write(this.loginBean.translate("date"));
		writer.write("</option>\n");
		writeSearchOptions(writer);
		writer.write("<option value='ThumbsUp' " + getInstanceSortCheckedString(InstanceSort.ThumbsUp) + ">");
		writer.write(this.loginBean.translate("thumbs up"));
		writer.write("</option>\n");
		writer.write("<option value='ThumbsDown' " + getInstanceSortCheckedString(InstanceSort.ThumbsDown) + ">");
		writer.write(this.loginBean.translate("thumbs down"));
		writer.write("</option>\n");
		writer.write("<option value='Stars' " + getInstanceSortCheckedString(InstanceSort.Stars) + ">");
		writer.write(this.loginBean.translate("stars"));
		writer.write("</option>\n");
		writer.write("<option value='LastConnect' " + getInstanceSortCheckedString(InstanceSort.LastConnect) + ">");
		writer.write(this.loginBean.translate("last connect"));
		writer.write("</option>\n");
		writer.write("<option value='Connects' " + getInstanceSortCheckedString(InstanceSort.Connects) + ">");
		writer.write(this.loginBean.translate("connects"));
		writer.write("</option>\n");
		writer.write("<option value='DailyConnects' " + getInstanceSortCheckedString(InstanceSort.DailyConnects) + ">");
		writer.write(this.loginBean.translate("connects today"));
		writer.write("</option>\n");
		writer.write("<option value='WeeklyConnects' " + getInstanceSortCheckedString(InstanceSort.WeeklyConnects) + ">");
		writer.write(this.loginBean.translate("connects this week"));
		writer.write("</option>\n");
		writer.write("<option value='MonthlyConnects' " + getInstanceSortCheckedString(InstanceSort.MonthlyConnects) + ">");
		writer.write(this.loginBean.translate("connects this month"));
		writer.write("</option>\n");
		writer.write("</select>\n");
		writer.write("</div>\n");
		
		writer.write("<div class='search-div'>\n");
		writer.write("<span class='search-span'>");
		writer.write(this.loginBean.translate("Rating"));
		writer.write("</span>\n");
		writer.write("<select id='searchselect' name='content-rating' onchange='this.form.submit()'>\n");
		writer.write("<option value='Everyone' " + this.loginBean.getContentRatingCheckedString(ContentRating.Everyone) + ">");
		writer.write(this.loginBean.translate("Everyone"));
		writer.write("</option>\n");
		writer.write("<option value='Teen' " + this.loginBean.getContentRatingCheckedString(ContentRating.Teen) + ">");
		writer.write(this.loginBean.translate("Teen"));
		writer.write("</option>\n");
		writer.write("<option value='Mature' " + this.loginBean.getContentRatingCheckedString(ContentRating.Mature) + ">");
		writer.write(this.loginBean.translate("Mature"));
		writer.write("</option>\n");
		writer.write("</select>\n");
		writer.write("</div>\n");
		
		writer.write("<input style='display:none;position:absolute;' type='submit' name='search' value='");
		writer.write(this.loginBean.translate("Search"));
		writer.write("'>\n");
		writer.write("</span>\n");
		writer.write("<br/>\n");
		writer.write("</form>\n");

		return writer.toString();
	}
}

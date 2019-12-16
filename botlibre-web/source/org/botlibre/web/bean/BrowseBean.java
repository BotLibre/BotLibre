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
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.AvatarImage;
import org.botlibre.web.admin.Category;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.admin.Flaggable;
import org.botlibre.web.admin.Tag;
import org.botlibre.web.rest.WebMediumConfig;

public abstract class BrowseBean<T extends Flaggable> extends ServletBean {
	public enum InstanceFilter { Private, Public, Personal, Adult, Featured, Template }
	public enum InstanceSort { Date, Name, Size,
		Rank, Wins, Losses,
		ThumbsUp, ThumbsDown, Stars, Posts, Messages, Users,
		LastConnect, Connects, DailyConnects, WeeklyConnects, MonthlyConnects }
	public enum InstanceRestrict { None, Link, Website, Subdomain, Hidden, Flagged, Icon, Adult,
			Admin, Partner, Diamond, Platinum, Gold, Bronze,
			Ad, AdUnverified,
			Twitter, Facebook, Telegram, Email, Slack, Skype, WeChat, Kik, Timer,
			Forkable, Schema, Database, Archived,
			Expired, Active, Trial, Basic, Premium, Professional, Enterprise, EnterprisePlus, Corporate, Dedicated, Private }
	public enum DisplayOption { Header, Details, Grid }
	
	protected InstanceSort instanceSort = InstanceSort.MonthlyConnects;
	protected InstanceRestrict instanceRestrict = InstanceRestrict.None;
	protected InstanceFilter instanceFilter = InstanceFilter.Public;
	protected DisplayOption displayOption = DisplayOption.Grid;
	protected String userFilter = null;
	protected String nameFilter = "";
	protected String categoryFilter = "";
	protected String tagFilter = "";
	protected boolean forking = false;
	
	protected T instance;
	protected T editInstance;
	protected T displayInstance;
	
	protected Category category;
	protected boolean showAllCategories;
	
	public BrowseBean() {
	}

	public void disconnect() {
		this.instance = null;
		this.displayInstance = null;
		this.category = null;
		this.forking = false;
		resetSearch();
	}

	public boolean getShowAllCategories() {
		return showAllCategories;
	}

	public void setShowAllCategories(boolean showAllCategories) {
		this.showAllCategories = showAllCategories;
	}

	public boolean getForking() {
		return forking;
	}

	public void setForking(boolean forking) {
		this.forking = forking;
	}

	public Category getCategory() {
		if (this.category != null && this.category.getDomain() != null && !this.category.getDomain().equals(getDomain())) {
			this.category = null;
		}
		return this.category;
	}

	public String getCategoryString() {
		if (getCategory() == null) {
			return "";
		}
		return this.category.getName();
	}
	
	public static String getColorsString() {
		return "'white', 'ivory', 'red', 'pink', 'green', 'blue', 'navy', 'teal', 'purple', 'yellow', 'lightyellow', 'darkyellow', 'brown', 'tan', 'grey', 'lightgrey', 'darkgrey', 'silver', 'black', '#fff', '#000'";
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public void browseCategory(String name) {
		try {
			resetSearch();
			if (name.equals("root")) {
				setCategory(null);
				setCategoryFilter(null);
				return;
			} else if (name.equals("more")) {
				setShowAllCategories(true);
				return;
			} else if (name.equals("less")) {
				setShowAllCategories(false);
				return;
			}
			Category category =  AdminDatabase.instance().findCategory(name, getTypeName(), getDomain());
			setCategory(category);
			setCategoryFilter(name);
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void resetSearch() {
		this.instanceSort = InstanceSort.MonthlyConnects;
		this.instanceFilter = InstanceFilter.Public;
		this.instanceRestrict = InstanceRestrict.None;
		this.displayOption = DisplayOption.Grid;
		this.userFilter = null;
		this.categoryFilter = "";
		this.nameFilter = "";
		this.tagFilter = "";
		this.page = 0;
		this.resultsSize = 0;
	}

	public boolean isDefaults() {
		return this.instanceSort == InstanceSort.MonthlyConnects
			&& this.instanceFilter == InstanceFilter.Public
			&& this.instanceRestrict == InstanceRestrict.None
			&& this.displayOption == DisplayOption.Grid
			&& (this.userFilter == null || this.userFilter.isEmpty())
			&& this.categoryFilter.isEmpty()
			&& this.nameFilter.isEmpty()
			&& this.tagFilter.isEmpty()
			&& this.page == 0;
	}
	
	public String getCategoryFilter() {
		if (categoryFilter == null) {
			return "";
		}
		return categoryFilter;
	}

	public void setCategoryFilter(String categoryFilter) {
		if (categoryFilter == null) {
			categoryFilter = "";
		}
		this.categoryFilter = categoryFilter;
	}

	public Domain getDomain() {
		return this.loginBean.getDomain();
	}

	public void setEditInstance(T editInstance) {
		this.editInstance = editInstance;
	}
	
	public String instanceString() {
		if (this.instance == null) {
			return "";
		}
		return "&instance=" + getInstanceId();
	}
	
	public String instanceInput() {
		if (this.instance == null) {
			return "";
		}
		return "<input name=\"instance\" type=\"hidden\" value=\"" + getInstanceId() + "\"/>";
	}
	
	public void writeStarButtonHTML(boolean embed, Writer out) {
		if (getInstance() == null || embed) {
			return;
		}
		try {
			out.write("<a href='#' class='button' onclick=\"");
			if (getLoginBean().isLoggedIn()) {
				out.write("$('#dialog-star').dialog('open'); return false;\" title='");
				out.write(this.loginBean.translate("Rate the " + Utils.camelCaseToLowerCase(getDisplayName())));
			} else {
				out.write("SDK.showError('");
				out.write(this.loginBean.translate("You must sign in first"));
				out.write("'); return false;\" title='");
				out.write(this.loginBean.translate("Rate the " + Utils.camelCaseToLowerCase(getDisplayName())));
			}
			out.write("'><img src='images/star.svg' class='toolbutton'/>");
			out.write(String.valueOf(Utils.truncate(getDisplayInstance().getStars())));
			out.write("/5</a>");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeStarMenuItemHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (getInstance() == null || embed) {
			return;
		}
		try {
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='#' onclick=\"");
			if (getLoginBean().isLoggedIn()) {
				out.write("$('#dialog-star').dialog('open'); return false;\" title='");
				out.write(this.loginBean.translate("Rate the " + Utils.camelCaseToLowerCase(getDisplayName())));
			} else {
				out.write("SDK.showError('");
				out.write(this.loginBean.translate("You must sign in first"));
				out.write("'); return false;\" title='");
				out.write(this.loginBean.translate("Rate the " + Utils.camelCaseToLowerCase(getDisplayName())));
			}
			out.write("'><img src='images/star.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Rate"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}

	public boolean isAdmin() {
		return false;
	}
	
	public void writeDeleteButtonHTML(Writer out) {
		if (getInstance() == null || !(isAdmin() || isSuper())) {
			return;
		}
		try {
			out.write("<a href='#' onclick=\"$('#dialog-delete').dialog('open'); return false;\" title='");
			out.write(this.loginBean.translate("Permently delete the " + Utils.camelCaseToLowerCase(getDisplayName())
					+ " and all of its data"));
			out.write("'><img src='images/remove.svg' class='toolbar'/>");
			out.write("</a>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeMessageButtonHTML(Writer out) {
		// Only for bot bean.
	} 
	
	public void writeAddFriendButtonHTML(Writer out) {
		// Only for bot bean.
	}
	
	public void writeDeleteMenuItemHTML(SessionProxyBean proxy, Writer out) {
		if (getInstance() == null || !(isAdmin() || isSuper())) {
			return;
		}
		try {
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='#' onclick=\"$('#dialog-delete').dialog('open'); return false;\" title='");
			out.write(this.loginBean.translate("Permently delete the " + Utils.camelCaseToLowerCase(getDisplayName())
					+ " and all of its data"));
			out.write("'><img src='images/remove.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Delete"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeFlagButtonHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (getInstance() == null || embed) {
			return;
		}
		try {
			if (!getInstance().isFlagged()) {
				if (getLoginBean().isLoggedIn()) {
					if (!isAdmin() || isSuper()) {
						out.write("<a onclick=\"$('#dialog-flag').dialog('open'); return false;\" href='#' title='");
						out.write(this.loginBean.translate("Flag " + Utils.camelCaseToLowerCase(getDisplayName())
								+ " as offensive, or in violation of site rules"));
						out.write("'><img src='images/flag2.svg' class='toolbar'/></a>\n");
					}
				} else {
					out.write("<a onclick=\"SDK.showError('");
					out.write(this.loginBean.translate("You must sign in first"));
					out.write("'); return false;\" title='");
					out.write(this.loginBean.translate("You must sign in first"));
					out.write("'><img src='images/flag2.svg' class='toolbar'/></a>\n");
				}
			} else if (isSuper()) {
				out.write("<a href='");
				out.write(getPostAction());
				out.write("?unflag");
				out.write(this.loginBean.postTokenString());
				if (proxy != null) {
					out.write(proxy.proxyString());
				}
				out.write(instanceString());
				out.write("' title='");
				out.write(this.loginBean.translate("Unflag"));
				out.write("'><img src='images/unflag2.svg' class='toolbar'/></a>\n");
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeFlagMenuItemHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (getInstance() == null || embed) {
			return;
		}
		try {
			if (!getInstance().isFlagged()) {
				if (getLoginBean().isLoggedIn()) {
					if (!isAdmin() || isSuper()) {
						out.write("<tr class='menuitem'>\n");
						out.write("<td><a class='menuitem' onclick=\"$('#dialog-flag').dialog('open'); return false;\" href='#' title='");
						out.write(this.loginBean.translate("Flag " + Utils.camelCaseToLowerCase(getDisplayName())
								+ " as offensive, or in violation of site rules"));
						out.write("'><img src='images/flag2.svg' class='menu'/> ");
						out.write(this.loginBean.translate("Flag"));
						out.write("</a></td>");
						out.write("</tr>\n");
					}
				} else {
					out.write("<tr class='menuitem'>\n");
					out.write("<td><a class='menuitem' href='#' onclick=\"SDK.showError('");
					out.write(this.loginBean.translate("You must sign in first"));
					out.write("'); return false;\" title='");
					out.write(this.loginBean.translate("You must sign in first"));
					out.write("'><img src='images/flag2.svg' class='menu'/> ");
					out.write(this.loginBean.translate("Flag"));
					out.write("</a></td>\n");
					out.write("</tr>\n");
				}
			} else if (isSuper()) {
				out.write("<tr class='menuitem'>\n");
				out.write("<td><a class='menuitem' href='");
				out.write(getPostAction());
				out.write("?unflag");
				out.write(this.loginBean.postTokenString());
				if (proxy != null) {
					out.write(proxy.proxyString());
				}
				out.write(instanceString());
				out.write("' title='");
				out.write(this.loginBean.translate("Unflag"));
				out.write("'><img src='images/unflag2.svg' class='menu'/> ");
				out.write(this.loginBean.translate("Unflag"));
				out.write("</a></td>\n");
				out.write("</tr>\n");
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeMenuButtonHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (getInstance() == null) {
			return;
		}
		try {
			out.write("<a href='#' onclick='return false'><img src='images/menu.png' class='toolbar'/></a>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeThumbsButtonsHTML(boolean embed, Writer out) {
		if (getInstance() == null || embed) {
			return;
		}
		try {
			out.write("<input id='thumbs-up' class='hidden' type='submit' name='thumbs-up'>");
			out.write("<input id='thumbs-down' class='hidden' type='submit' name='thumbs-down'>");
			out.write("<a href='#' class='button' onclick=\"");
			if (getLoginBean().isLoggedIn()) {
				out.write("$('#thumbs-up').click(); return false;\" title='");
				out.write(this.loginBean.translate("Vote the " + Utils.camelCaseToLowerCase(getDisplayName())));
			} else {
				out.write("SDK.showError('");
				out.write(this.loginBean.translate("You must sign in first"));
				out.write("'); return false;\" title='");
				out.write(this.loginBean.translate("Vote the " + Utils.camelCaseToLowerCase(getDisplayName())));
			}
			out.write(" up'><img src='images/thumbs-up.png' class='toolbutton'/>");
			out.write(String.valueOf(getDisplayInstance().getThumbsUp()));
			out.write("</a>\n");
			out.write("<a href='#' class='button' onclick=\"");
			if (getLoginBean().isLoggedIn()) {
				out.write("$('#thumbs-down').click(); return false;\" title='");
				out.write(this.loginBean.translate("Vote the " + Utils.camelCaseToLowerCase(getDisplayName())));
			} else {
				out.write("SDK.showError('");
				out.write(this.loginBean.translate("You must sign in first"));
				out.write("'); return false;\" title='");
				out.write(this.loginBean.translate("Vote the " + Utils.camelCaseToLowerCase(getDisplayName())));
			}
			out.write(" down'><img src='images/thumbs-down.png' class='toolbutton'/>");
			out.write(String.valueOf(getDisplayInstance().getThumbsDown()));
			out.write("</a>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeThumbsMenuItemsHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (getInstance() == null || embed) {
			return;
		}
		try {
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='#' onclick=\"");
			if (getLoginBean().isLoggedIn()) {
				out.write("$('#thumbs-up').click(); return false;\" title='");
				out.write(this.loginBean.translate("Vote the " + Utils.camelCaseToLowerCase(getDisplayName())));
			} else {
				out.write("SDK.showError('You must sign in first'); return false;\" title='");
				out.write(this.loginBean.translate("Vote the " + Utils.camelCaseToLowerCase(getDisplayName())));
			}
			out.write(" up'><img src='images/thumbs-up.png' class='menu'/> ");
			out.write(this.loginBean.translate("Thumbs Up"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
			
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='#' onclick=\"");
			if (getLoginBean().isLoggedIn()) {
				out.write("$('#thumbs-down').click(); return false;\" title='");
				out.write(this.loginBean.translate("Vote the " + Utils.camelCaseToLowerCase(getDisplayName())));
			} else {
				out.write("SDK.showError('");
				out.write(this.loginBean.translate("You must sign in first"));
				out.write("'); return false;\" title='");
				out.write(this.loginBean.translate("Vote the " + Utils.camelCaseToLowerCase(getDisplayName())));
			}
			out.write(" down'><img src='images/thumbs-down.png' class='menu'/> ");
			out.write(this.loginBean.translate("Thumbs Down"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public boolean hasEmptyToolbar(boolean embed) {
		return embed && !isAdmin();
	}
	
	public void writeAdminButtonHTML(SessionProxyBean proxy, Writer out) {
	}
	
	public void writeToolbarExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		
	}
	
	public void writeToolbarPostExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		
	}
	
	public void writeEditButtonHTML(SessionProxyBean proxy, Writer out) {
		if (this.instance == null || !(isAdmin() || isSuper())) {
			return;
		}
		try {
			out.write("<a href='");
			out.write(getPostAction());
			out.write("?edit-instance=true");
			out.write(proxy.proxyString());
			out.write(instanceString());
			out.write("' title=\"");
			out.write(this.loginBean.translate("Edit the " + Utils.camelCaseToLowerCase(getDisplayName())
					+ "'s detail information such as its license and description"));
			out.write("\"><img src='images/edit.svg' class='toolbar'/></a>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeToolbarHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (hasEmptyToolbar(embed)) {
			return;
		}
		try {
			out.write("<div style='position:relative;margin-top:12px;margin-bottom:12px'>\n");
			out.write("<form action='");
			out.write(getPostAction());
			out.write("' method='post' class='message'>\n");
			out.write(this.loginBean.postTokenInput());
			out.write(proxy.proxyInput());
			out.write(instanceInput());
			writeMenuButtonHTML(proxy, embed, out);
			writeAdminButtonHTML(proxy, out);
			if (!getLoginBean().isMobile()) {
				writeToolbarExtraHTML(proxy, embed, out);
				writeEditButtonHTML(proxy, out);
				writeDeleteButtonHTML(out);
				writeMessageButtonHTML(out);
				writeAddFriendButtonHTML(out);
				writeFlagButtonHTML(proxy, embed, out);
			}
			writeThumbsButtonsHTML(embed, out);
			writeStarButtonHTML(embed, out);
			writeToolbarPostExtraHTML(proxy, embed, out);
			out.write(" <br/> \n"); // For some reason Chrome wants a space here...
			out.write("</form>\n");
			out.write("</div>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeStarDialogHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (getInstance() == null || embed) {
			return;
		}
		try {
			out.write("<div id='dialog-star' title='");
			out.write(this.loginBean.translate("Rate"));
			out.write("' class='dialog'>\n");
			out.write("<form action='");
			out.write(getPostAction());
			out.write("' method='post' class='message'>\n");
			out.write(this.loginBean.postTokenInput());
			if (proxy != null) {
				out.write(proxy.proxyInput());
			}
			out.write(instanceInput());
			out.write("<input id='star-icon' class='icon' type='submit' name='star1' value='' title='");
			out.write(this.loginBean.translate("Terrible"));
			out.write("'>\n");
			out.write("<input id='star-icon' class='icon' type='submit' name='star2' value='' title='");
			out.write(this.loginBean.translate("Bad"));
			out.write("'>\n");
			out.write("<input id='star-icon' class='icon' type='submit' name='star3' value='' title='");
			out.write(this.loginBean.translate("Okay"));
			out.write("'>\n");
			out.write("<input id='star-icon' class='icon' type='submit' name='star4' value='' title='");
			out.write(this.loginBean.translate("Good"));
			out.write("'>\n");
			out.write("<input id='star-icon' class='icon' type='submit' name='star5' value='' title='");
			out.write(this.loginBean.translate("Great"));
			out.write("'>\n");
			out.write("</form>\n");
			out.write("</div>\n");

			out.write("<script>\n");
			out.write("$(function() { $('#dialog-star').dialog({ autoOpen: false, modal: true }); });\n");
			out.write("</script>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeDeleteDialogHTML(SessionProxyBean proxy, Writer out) {
		if (getInstance() == null) {
			return;
		}
		try {
			out.write("<div id='dialog-delete' title='");
			out.write(this.loginBean.translate("Delete"));
			out.write("' class='dialog'>\n");
			out.write("<form action='");
			out.write(getPostAction());
			out.write("' method='post' class='message'>\n");
			out.write(this.loginBean.postTokenInput());
			if (proxy != null) {
				out.write(proxy.proxyInput());
			}
			out.write(instanceInput());
			out.write("<input id='delete-confirm' type='checkbox' name='delete' title='");
			out.write(this.loginBean.translate("Caution, this will permently delete the " + Utils.camelCaseToLowerCase(getDisplayName())
					+ " and all of its data."));
			out.write("'>");
			out.write(this.loginBean.translate("I'm sure"));
			out.write("</input><br/>\n");
			out.write("<input class='delete' onclick='return deleteInstance()' name='delete-instance' type='submit' value='");
			out.write(this.loginBean.translate("Delete"));
			out.write("' title='");
			out.write(this.loginBean.translate("Permently delete the " + Utils.camelCaseToLowerCase(getDisplayName())
					+ " and all of its data."));
			out.write("'/>\n");
			out.write("<input id='cancel-delete' onclick=\"$('#dialog-delete').dialog('close'); return false;\" class='cancel' name='cancel' type='submit' value='");
			out.write(this.loginBean.translate("Cancel"));
			out.write("'/>\n");
			out.write("</form>\n");
			out.write("</div>\n");

			out.write("<script>\n");
			out.write("var deleteInstance = function() {\n");
			out.write("if (document.getElementById('delete-confirm').checked) {\nreturn true;\n} else {\nSDK.showError(\"");
			out.write(this.loginBean.translate("You must click 'I\'m sure' ").replace('"', '\''));
			out.write("\");\nreturn false;\n}\n}\n");
			out.write("$(function() { $('#dialog-delete').dialog({ autoOpen: false, modal: true }); });\n");
			out.write("</script>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeFlagDialogHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (getInstance() == null || embed) {
			return;
		}
		try {		
			out.write("<div id='dialog-flag' title='");
			out.write(this.loginBean.translate("Flag"));
			out.write("' class='dialog'>\n");
			out.write("<form action='");
			out.write(getPostAction());
			out.write("' method='post' class='message'>\n");
			out.write(this.loginBean.postTokenInput());
			if (proxy != null) {
				out.write(proxy.proxyInput());
			}
			out.write(instanceInput());
			out.write("<input id='flag-confirm' type='checkbox' name='flagged' title='");
			out.write(this.loginBean.translate("Do not misuse the flag option, flaging valid " + Utils.camelCaseToLowerCase(getDisplayName())
					+ "s can cause your account to be disabled"));
			out.write("'>");
			out.write(this.loginBean.translate("Flag " + Utils.camelCaseToLowerCase(getDisplayName())
					+ " as offensive, or in violation of site rules"));
			out.write("</input><br/>\n");
			out.write("<input id='flag-reason' name='flag-reason' type='text' placeholder='");
			out.write(this.loginBean.translate("reason"));
			out.write("'/><br/>\n");
			out.write("<input onclick='return flagInstance()' class='delete' name='flag' type='submit' value='");
			out.write(this.loginBean.translate("Flag"));
			out.write("' title='");
			out.write(this.loginBean.translate("Flag " + Utils.camelCaseToLowerCase(getDisplayName())
					+ " as offensive, or in violation of site rules"));
			out.write("'/>\n");
			out.write("<input id='cancel-flag' onclick=\"$('#dialog-flag').dialog('close'); return false;\" class='cancel' name='cancel' type='submit' value='");
			out.write(this.loginBean.translate("Cancel"));
			out.write("'/>\n");
			out.write("</form>\n");
			out.write("</div>\n");
			
			out.write("<script>\n");
			out.write("var flagInstance = function() {\n");
			out.write("if (document.getElementById('flag-confirm').checked && document.getElementById('flag-reason').value != '') {\nreturn true;\n");
			out.write("} else {\nSDK.showError(\"");
			out.write(this.loginBean.translate("You must click 'Flag' and enter reason").replace('"', '\''));
			out.write("\");\nreturn false;\n}\n}\n");
			out.write("$(function() { $('#dialog-flag').dialog({ autoOpen: false, modal: true }); });\n");
			out.write("</script>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeFlaggedHTML(Writer out) {
		if (getDisplayInstance() == null || !getDisplayInstance().isFlagged()) {
			return;
		}
		try {
			out.write("<p style='color:red;font-weight:bold;'>");
			out.write(this.loginBean.translate("This " + Utils.camelCaseToLowerCase(getDisplayName())
					+ " has been flagged for:"));
			out.write(" ");
			out.write(getDisplayInstance().getFlaggedReason());
			out.write(" ");
			out.write(this.loginBean.translate("by"));
			out.write(" ");
			out.write(getDisplayInstance().getFlaggedUser());
			out.write(".</p>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}

	public T getDisplayInstance() {
		return displayInstance;
	}

	public T getEditInstance() {
		if (this.editInstance == null) {
			return this.instance;
		}
		return this.editInstance;
	}

	public void setDisplayInstance(T displayInstance) {
		this.displayInstance = displayInstance;
	}

	public String getEmbeddedBanner() {
		return "forum-banner.jsp";
	}
	
	public InstanceRestrict getInstanceRestrict() {
		return instanceRestrict;
	}

	public void setInstanceRestrict(InstanceRestrict instanceRestrict) {
		this.instanceRestrict = instanceRestrict;
	}

	public InstanceFilter getInstanceFilter() {
		return instanceFilter;
	}
	
	public void setInstanceFilter(InstanceFilter instanceFilter) {
		this.instanceFilter = instanceFilter;
	}

	public InstanceSort getInstanceSort() {
		return instanceSort;
	}

	public void setInstanceSort(InstanceSort instanceSort) {
		this.instanceSort = instanceSort;
	}
	
	public String getInstanceFilterCheckedString(InstanceFilter filter) {
		if (this.instanceFilter == filter) {
			return "checked=\"checked\"";
		}
		return "";
	}
		
	public String getInstanceSortCheckedString(InstanceSort sort) {
		if (sort == this.instanceSort) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getInstanceRestrictCheckedString(InstanceRestrict restrict) {
		if (restrict == this.instanceRestrict) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getDisplayCheckedString(DisplayOption display) {
		if (display == this.displayOption) {
			return "selected=\"selected\"";
		}
		return "";
	}

	public DisplayOption getDisplayOption() {
		return displayOption;
	}

	public void setDisplayOption(DisplayOption displayOption) {
		this.displayOption = displayOption;
	}

	public List<Tag> getAllTags() {
		try {
			return AdminDatabase.instance().getTags(getTypeName(), getDomain());
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Tag>();
		}
	}

	public List<Category> getChildCategories() {
		if (this.category == null) {
			return getRootCategories();
		}
		return this.category.getChildren();
	}
	
	public List<Category> getRootCategories() {
		try {
			return AdminDatabase.instance().getRootCategories(getTypeName(), getDomain());
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Category>();
		}
	}

	public List<Category> getAllCategories() {
		return this.loginBean.getAllCategories(getTypeName());
	}

	public String getAllTagsString() {
		StringWriter writer = new StringWriter();
		int count = 1;
		List<Tag> tags = getAllTags();
		for (Tag tag : tags) {
			writer.write("\"");
			writer.write(tag.getName());
			writer.write("\"");
			if (count < tags.size())
			writer.write(", ");
			count++;
		}
		return writer.toString();
	}

	public List<T> getAllInstances() {
		return new ArrayList<T>();
	}

	public List<T> getAllSearchInstances() {
		return getAllInstances();
	}

	public List<T> getAllFeaturedInstances() {
		return new ArrayList<T>();
	}

	public String getAllCategoriesString() {
		return this.loginBean.getAllCategoriesString(getTypeName());
	}

	public String getAvatarThumb(AvatarImage avatar) {
		return this.loginBean.getAvatarThumb(avatar, 192);
	}

	public String getAvatarImage(AvatarImage avatar) {
		return this.loginBean.getAvatarImage(avatar);
	}

	public String getAvatarThumb(Category category) {
		return this.loginBean.getAvatarThumb(category);
	}

	public String getAvatarImage(Category category) {
		return this.loginBean.getAvatarImage(category);
	}
	
	public String getTagFilter() {
		return tagFilter;
	}

	public void setTagFilter(String tagFilter) {
		if (tagFilter == null) {
			tagFilter = "";
		}
		this.tagFilter = tagFilter;
	}

	public String getUserFilter() {
		return userFilter;
	}

	public void setUserFilter(String userFilter) {
		this.userFilter = userFilter;
	}

	public String getNameFilter() {
		if (nameFilter == null) {
			return "";
		}
		return nameFilter;
	}

	public void setNameFilter(String nameFilter) {
		this.nameFilter = nameFilter;
	}

	public long getInstanceId() {
		if (this.instance == null || this.instance.getId() == null) {
			return 0;
		}
		return this.instance.getId();
	}

	public boolean hasValidInstance() {
		return this.instance != null && this.instance.getId() != null;
	}

	public T getInstance() {
		return instance;
	}

	public void setInstance(T instance) {
		this.instance = instance;
		this.displayInstance = instance;
		this.editInstance = instance;
	}

	public boolean isSuper() {
		return this.loginBean.isSuper();
	}

	public boolean isSuperUser() {
		return this.loginBean.isSuperUser();
	}
	
	public abstract Class<? extends Flaggable> getType();
	
	public abstract String getTypeName();
	
	public String getDisplayName() {
		return getTypeName();
	}
	
	public abstract String getCreateURL();
	
	public abstract String getSearchURL();
	
	public abstract String getBrowseURL();
	
	public void checkLogin() {
		this.loginBean.checkLogin();
	}
	
	public void checkVerfied(WebMediumConfig config) {
		this.loginBean.checkVerified(config);
	}
	
	public void checkSuper() {
		this.loginBean.checkSuper();
	}	
	
	public void checkInstance() {
		if (this.instance == null) {
			throw new BotException("Missing " + getDisplayName());
		}
	}

	public boolean flagInstance(String flagReason, String flagged) {
		try {
			flagReason = Utils.sanitize(flagReason);
			if (!"on".equals(flagged)) {
				throw new BotException("You must check 'Flag'");
			}
			if (flagReason == null || flagReason.equals("reason") || flagReason.isEmpty()) {
				throw new BotException("You must enter the reason for flagging the " + getDisplayName());
			}
			checkLogin();
			checkInstance();
			setInstance(AdminDatabase.instance().flag(this.instance, getUser().getUserId(), flagReason));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean unflagInstance() {
		try {
			checkSuper();
			checkInstance();
			setInstance(AdminDatabase.instance().unflag(this.instance, getUser().getUserId()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean thumbsUp() {
		try {
			checkLogin();
			checkInstance();
			setInstance(AdminDatabase.instance().thumbsUp(this.instance, getUser().getUserId()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean thumbsDown() {
		try {
			checkLogin();
			checkInstance();
			setInstance(AdminDatabase.instance().thumbsDown(this.instance, getUser().getUserId()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean star(int stars) {
		try {
			if (stars < 1 || stars > 5) {
				throw new BotException("Star rating must be between 1 and 5");
			}
			checkLogin();
			checkInstance();
			setInstance(AdminDatabase.instance().star(this.instance, getUser().getUserId(), stars));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	public abstract String getPostAction();

}

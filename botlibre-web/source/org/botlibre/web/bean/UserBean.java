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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.util.Utils;
import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.admin.User;

public class UserBean extends ServletBean {
	public enum UserFilter { Public, Friends, Private, Bot }
	
	public enum UserSort { Date, Name, LastConnect, Connects, Followers, Friends, Affiliates }
		
	public enum UserRestrict { None, Website, Flagged, Banned,
			Admin, Partner, Diamond, Platinum, Gold, Bronze,
			Ad, AdUnverified, Expired,
			All, Email, Verified, AppID, Icon }

	public enum DisplayOption { Header, Details, Grid }

	String categoryType = Site.TYPE;
	
	User instance;
	User editInstance;
	User displayInstance;
	
	String nameFilter = "";
	String emailFilter = "";
	DisplayOption displayOption = DisplayOption.Grid;
	UserSort userSort = UserSort.Connects;
	UserFilter userFilter = UserFilter.Public;
	UserRestrict userRestrict = UserRestrict.None;
	private String speakText;
	private String tagFilter;
	
	public String getDisplayCheckedString(DisplayOption display) {
		if (display == this.displayOption) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getInstanceSortCheckedString(UserSort sort) {
		if (sort == this.userSort) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getInstanceRestrictCheckedString(UserRestrict restrict) {
		if (restrict == this.userRestrict) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public void resetSearch() {
		this.userSort = UserSort.Connects;
		this.userRestrict = UserRestrict.None;
		this.displayOption = DisplayOption.Grid;
		this.nameFilter = "";
		this.emailFilter = "";
		this.page = 0;
		this.resultsSize = 0;
	}
	
	public boolean isDefaults() {
		return this.userSort == UserSort.Connects
			&& this.userRestrict == UserRestrict.None
			&& this.displayOption == DisplayOption.Grid
			&& this.nameFilter.isEmpty()
			&& this.emailFilter.isEmpty()
			&& this.page == 0;
	}
	
	public List<User> getAllInstances() {
		try {
			boolean isSuperUser = false;
			if (this.getUser() != null) {
				isSuperUser = this.getUser().isSuperUser();
			}
			List<User> results = AdminDatabase.instance().getAllUsers(this.page, this.pageSize, this.nameFilter, this.emailFilter, this.tagFilter,
					this.userFilter, this.userRestrict, this.userSort, this.getUser(), isSuperUser);
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (results.size() < this.pageSize) {
					this.resultsSize = results.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllUserCount(this.nameFilter, this.emailFilter, this.tagFilter,
							this.userFilter, this.userRestrict, this.userSort, this.getUser(), isSuperUser);
				}
			}
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<User>();
		}
	}
	
	public DisplayOption getDisplayOption() {
		return displayOption;
	}
	
	public void setDisplayOption(DisplayOption displayOption){
		this.displayOption = displayOption;
	}
	
	public String getTagFilter() {
		if (this.tagFilter == null) {
			this.tagFilter = "";
		}
		return this.tagFilter;
	}

	public void setTagFilter(String tagFilter) {
		if (tagFilter == null) {
			tagFilter = "";
		}
		this.tagFilter = tagFilter;
	}
	
	public UserFilter getUserFilter() {
		return userFilter;
	}

	public void setUserFilter(UserFilter userFilter) {
		this.userFilter = userFilter;
	}
	
	public UserSort getUserSort() {
		return userSort;
	}

	public void setUserSort(UserSort userSort) {
		this.userSort = userSort;
	}
	
	public UserRestrict getUserRestrict() {
		return userRestrict;
	}

	public void setUserRestrict(UserRestrict userRestrict) {
		this.userRestrict = userRestrict;
	}
	
	public String getNameFilter() {
		if (nameFilter == null) {
			return "";
		}
		return nameFilter;
	}
	 
	public void setNameFilter(String nameFilter){
		this.nameFilter = nameFilter;
	}
	
	public String getEmailFilter() {
		if (emailFilter == null) {
			return "";
		}
		return emailFilter;
	}
	 
	public void setEmailFilter(String emailFilter){
		this.emailFilter = emailFilter;
	}
	
	public String searchFormUserHTML() {
		StringWriter newWriter = new StringWriter();
		boolean isSuperUser = this.getLoginBean().isSuper();
		newWriter.write("<form action='" + getBrowseAction() + "' method='get' class='search'>\n");
		newWriter.write("<span class='menu'>\n");
		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>\n");
		newWriter.write(loginBean.translate("Name"));
		newWriter.write("</span>\n");
		newWriter.write("<input id='searchtext' name='name-filter' type='text' value='" + getNameFilter() + "' title='Filter by any name containing the text' /></td>\n");
		newWriter.write("</div>\n");
		if (isSuperUser) {
			newWriter.write("<div class='search-div'>\n");
			newWriter.write("<span class='search-span'>Email</span>\n");
			newWriter.write("<input id='searchtext' name='email-filter' type='text' value='" + getEmailFilter() + "' title='Filter by any email containing the text' /></td>\n");
			newWriter.write("</div>\n");
		}
		
		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>\n");
		newWriter.write(loginBean.translate("Tags"));
		newWriter.write("</span>\n");
		newWriter.write("<input id='tags' name='tag-filter' type='text' value='" + getTagFilter() + "' title='Filter by any tag containing the text' /></td>\n");
		newWriter.write("</div>\n");
		
		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>");
		newWriter.write(loginBean.translate("Display"));
		newWriter.write("</span>\n");
		newWriter.write("<select id='searchselect' name='display' onchange='this.form.submit()'>\n");
		newWriter.write("<option value='grid' " + getDisplayCheckedString(DisplayOption.Header) + ">Grid</option>\n");
		newWriter.write("<option value='details' " + getDisplayCheckedString(DisplayOption.Details) + ">List</option>\n");
		newWriter.write("</select>\n");
		newWriter.write("</div>\n");
		
		if (isSuperUser) {
			newWriter.write("<div class='search-div'>\n");
			newWriter.write("<span class='search-span'>");
			newWriter.write(loginBean.translate("Restrict"));
			newWriter.write("</span>\n");
			newWriter.write("<select id='searchselect' name='restrict' onchange='this.form.submit()'>\n");
			newWriter.write("<option value='None' " + getInstanceRestrictCheckedString(UserRestrict.None) + ">none</option>\n");
			newWriter.write("<option value='Expired' " + getInstanceRestrictCheckedString(UserRestrict.Expired) + ">expired</option>\n");
			newWriter.write("<option value='Flagged' " + getInstanceRestrictCheckedString(UserRestrict.Flagged) + ">flagged</option>\n");
			newWriter.write("<option value='Banned' " + getInstanceRestrictCheckedString(UserRestrict.Banned) + ">banned</option>\n");
			newWriter.write("<option value='Admin' " + getInstanceRestrictCheckedString(UserRestrict.Admin) + ">admin</option>\n");
			newWriter.write("<option value='Partner' " + getInstanceRestrictCheckedString(UserRestrict.Partner) + ">partner</option>\n");
			newWriter.write("<option value='Diamond' " + getInstanceRestrictCheckedString(UserRestrict.Diamond) + ">diamond</option>\n");
			newWriter.write("<option value='Platinum' " + getInstanceRestrictCheckedString(UserRestrict.Platinum) + ">platinum</option>\n");
			newWriter.write("<option value='Gold' " + getInstanceRestrictCheckedString(UserRestrict.Gold) + ">gold</option>\n");
			newWriter.write("<option value='Bronze' " + getInstanceRestrictCheckedString(UserRestrict.Bronze) + ">bronze</option>\n");
			newWriter.write("<option value='Ad' " + getInstanceRestrictCheckedString(UserRestrict.Ad) + ">ad</option>\n");
			newWriter.write("<option value='AdUnverified' " + getInstanceRestrictCheckedString(UserRestrict.AdUnverified) + ">ad unverified</option>\n");
			newWriter.write("<option value='Email' " + getInstanceRestrictCheckedString(UserRestrict.Email) + ">email</option>\n");
			newWriter.write("<option value='Website' " + getInstanceRestrictCheckedString(UserRestrict.Website) + ">website</option>\n");
			newWriter.write("<option value='Icon' " + getInstanceRestrictCheckedString(UserRestrict.Icon) + ">icon</option>\n");
			newWriter.write("<option value='Verified' " + getInstanceRestrictCheckedString(UserRestrict.Verified) + ">verified</option>\n");
			newWriter.write("<option value='AppID' " + getInstanceRestrictCheckedString(UserRestrict.AppID) + ">app id</option>\n");
			newWriter.write("</select>\n");
			newWriter.write("</div>\n");
		}
		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>");
		newWriter.write(loginBean.translate("Sort"));
		newWriter.write("</span>\n");
		newWriter.write("<select id='searchselect' name='sort' onchange='this.form.submit()'>\n");
		newWriter.write("<option value='Name' " + getInstanceSortCheckedString(UserSort.Name) + ">name</option>\n");
		newWriter.write("<option value='Date' " + getInstanceSortCheckedString(UserSort.Date) + ">date</option>\n");
		if (isSuperUser) {
			newWriter.write("<option value='Affiliates' " + getInstanceSortCheckedString(UserSort.Affiliates) + ">affiliates</option>\n");
			newWriter.write("<option value='Friends' " + getInstanceSortCheckedString(UserSort.Friends) + ">friends</option>\n");
		}
		newWriter.write("<option value='LastConnect' " + getInstanceSortCheckedString(UserSort.LastConnect) + ">last connect</option>\n");
		newWriter.write("<option value='Connects' " + getInstanceSortCheckedString(UserSort.Connects) + ">connects</option>\n");
		newWriter.write("<option value='Followers' " + getInstanceSortCheckedString(UserSort.Followers) + ">followers</option>\n");
		newWriter.write("</select>\n");
		newWriter.write("</div>\n");
		
		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<input style='display:none;position:absolute;' type='submit' name='search' value='' /></td>\n");
		newWriter.write("</div>\n");
		newWriter.write("</span>\n");
		newWriter.write("</form>\n");
		return newWriter.toString();	
	}

	public String searchHTML() {
		StringWriter writer = new StringWriter();
		
		if (this.loginBean.isSuper()) {
			// Add admin form.
			writer.write("<form action='" + getPostAction() + "' method='post' class='message'>\n");
			writer.write(loginBean.postTokenInput());
			writer.write("<input name='export-all' type='submit' value='");
			writer.write(this.loginBean.translate("Export"));
			writer.write("' title='");
			writer.write(this.loginBean.translate("Export all users to a file"));
			writer.write("'/>\n");
			writer.write("<br>\n");
		}
	
		List<User> instances = getAllInstances();
		writer.write("<span class='menu'>");
		writer.write(getResultsSize() + " ");
		writer.write(this.loginBean.translate("results"));
		writer.write(".<br/>");
		writePagingString(writer, instances);
		writer.write("</span>");
		writer.write("<br/>");
		for (User instance : instances) {
			writeBrowseThumb(writer, instance, getDisplayOption() == DisplayOption.Grid);
		}
		writer.write("<br/>");
		writer.write("<span class = menu>");
		writePagingString(writer, instances);
		writer.write("</span>");

		if (this.loginBean.isSuper()) {
			writer.write("</form>\n");
		}
		
		return writer.toString();
	}
	
	public  String getPostAction() {
		return "user";
	}
	
	public void writeBrowseThumb(StringWriter writer, User instance, boolean grid) {
		if (grid) {
			writer.write("<div class='browse-div'>\n");
		} else {
			writer.write("<div class='browse-list-div'>\n");
		}
		if (grid) {
			if (!loginBean.isMobile()) {
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
		if (!grid || !loginBean.isMobile()) {
			boolean isSuperUser = this.getLoginBean().isSuperUser(); 
			writer.write("<div style='text-align:left'>\n");
			writeBrowseLink(writer, instance, true);
			writer.write("<br/>\n");
			if (!instance.getBio().trim().equals("")) {
				writer.write(instance.getBio() + "<br/>\n");
			}
			writer.write("<span class='details'>\n");
			if (instance.getShouldDisplayName()) {
				writer.write("Name:  " + instance.getName() + "<br/>\n");
			}
			if (isSuperUser) {
				writer.write("Email:  " + instance.getEmail() + "<br/>\n");
				writer.write("Account Type: " + instance.getType() + "<br/>\n");
				writer.write("Upgrade Date: " + Utils.displayTimestamp(instance.getUpgradeDate()) + "<br/>\n");
				writer.write("Expiry Date: " + Utils.displayTimestamp(instance.getExpiryDate()) + "<br/>\n");
				writer.write("Joined: " + Utils.displayDate(instance.getCreationDate()) + "<br/>\n");
			}
			writer.write("Connects:  " + String.valueOf(instance.getConnects()) + "<br/>\n");
			writer.write("Last Connect:  " + Utils.displayTimestamp(instance.getOldLastConnected()) + "\n");
			writer.write("</span>\n");
			writer.write("</div>\n");
		}
		if (grid) {
			if (!loginBean.isMobile()) {
				writer.write("</span>\n");
			}
			writer.write("<div class='browse-thumb'>\n");
			writeBrowseLink(writer, instance, false);
			writer.write("</div>\n");
		} else {
			writer.write("</td>\n");
		}
		writer.write("</div>\n");
	}
	
	public void writeBrowseImage(StringWriter writer, User instance) {
		writer.write("<a href='" + getBrowseAction() + "?id=" + instance.getUserId() + "'>");
		writer.write("<img class='browse-thumb' src='" + this.getLoginBean().getAvatarThumb(instance) + "' alt='" + instance.getUserId() + "'/>");
		writer.write("</a>\n");
	}
	
	public void writeBrowseLink(StringWriter writer, User instance, boolean bold) {
		if (instance.isFlagged()) {
			writer.write("<a class='menu' href='" + getBrowseAction() + "?id=" + instance.getUserId() + "'>");
			writer.write("<span style='color:red;" + (bold ? "font-weight:bold' class='browse-thumb'" : "margin: 0 0 0;'" ) + ">" + instance.getUserId() + "</span>");
			writer.write("</a>\n");
		} else {
			writer.write("<a class='menu' href='" + getBrowseAction() + "?id=" + instance.getUserId() + "'>");
			writer.write("<span " + (bold ? "class='browse-thumb' style='font-weight:bold'" : "style='margin: 0 0 0;'" ) + ">" + instance.getUserHTML() + "</span>");
			writer.write("</a>\n");
		}
	}
	
	public String getAvatarText() {
		if (getUser() == null || getUser().getInstanceAvatar() == null) {
			return "";
		}
		return getUser().getInstanceAvatar().getId() + " : " + getUser().getInstanceAvatar().getName();
	}
	
	public void chooseAvatar(String choice) {
		try {
			if (choice == null) {
				choice = "";
			}
			if (choice.indexOf(':') != -1) {
				choice = choice.substring(0, choice.indexOf(':'));
			}
			choice = choice.trim();
			if (choice.isEmpty()) {
				setUser(AdminDatabase.instance().updateUserInstanceAvatar(getUser().getUserId(), null));
				return;
			}
			AvatarBean avatarBean = getLoginBean().getBean(AvatarBean.class);
			if (!avatarBean.validateInstance(choice)) {
				return;
			}
			avatarBean.incrementConnects(ClientType.WEB);
			setUser(AdminDatabase.instance().updateUserInstanceAvatar(getUser().getUserId(), avatarBean.getInstance().getId()));
		} catch (Exception failed) {
			error(failed);
		}
	}
	
	public void saveVoice(String voice, String mod, boolean nativeVoice, boolean responsiveVoice, String language, String nativeVoiceName, Boolean bingSpeech, String nativeVoiceApiKey, Boolean qqSpeech, String nativeVoiceAppId, String voiceApiEndpoint) {
		voice = Utils.sanitize(voice);
		mod = Utils.sanitize(mod);
		language = Utils.sanitize(language);
		nativeVoiceName = Utils.sanitize(nativeVoiceName);
		nativeVoiceApiKey = Utils.sanitize(nativeVoiceApiKey);
		nativeVoiceAppId = Utils.sanitize(nativeVoiceAppId);
		voiceApiEndpoint = Utils.sanitize(voiceApiEndpoint);
		
		User user = this.getUser();
		String nativeVoiceProvider = null;
		if (responsiveVoice && Site.COMMERCIAL) {
			nativeVoiceProvider = User.RESPONSIVEVOICE;
		}
		else if (bingSpeech) {
			nativeVoiceProvider = User.BINGSPEECH;
		}
		else if (qqSpeech) {
			nativeVoiceProvider = User.QQSPEECH;
		}
		if (user != null) {
			setUser(AdminDatabase.instance().updateUserInstanceVoice(user.getUserId(), voice, mod, nativeVoice, nativeVoiceProvider, nativeVoiceName, language, user.getPitch(), user.getSpeechRate(), nativeVoiceApiKey, nativeVoiceAppId, voiceApiEndpoint));
		}
	}
	
	public String getVoiceCheckedString(String voice) {
		if (getUser() != null && voice.equals(getUser().getVoice())) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getVoiceModCheckedString(String mod) {
		if (getUser() != null && mod.equals(getUser().getVoiceMod())) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getSpeakText() {
		if (speakText == null) {
			speakText = "This is a test, testing 1 2 3 4 5 6 7 8 9 10";
		}
		return speakText;
	}

	
	public String getNativeVoiceCheckedString(String voice) {
		if (getUser() != null && voice.equals(getUser().getNativeVoiceName())) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public boolean getResponsiveVoice() {
		if (getUser() == null) {
			return false;
		}
		return User.RESPONSIVEVOICE.equals(getUser().getNativeVoiceProvider());
	}
	
	public boolean getBingSpeech() {
		if (getUser() == null) {
			return false;
		}
		return User.BINGSPEECH.equals(getUser().getNativeVoiceProvider());
	}
	
	public boolean getQQSpeech() {
		if (getUser() == null) {
			return false;
		}
		return User.QQSPEECH.equals(getUser().getNativeVoiceProvider());
	}

	/**
	 * Download all of the user information to a csv file.
	 */
	public boolean exportAll(HttpServletRequest request, HttpServletResponse response) {
		try {
			this.loginBean.checkSuper();

			response.setContentType("text/plain");
			response.setHeader("Content-disposition","attachment; filename=" + encodeURI("Exported_Users.csv"));

			PrintWriter writer = response.getWriter();
			writer.write("User ID, Name, Gender, Email, EmailNotices, Date of Birth, Upgrade Date, Expiry Date, Creation Date\n");
			this.page = 0;
			List<User> instances = getAllInstances();
			int count = 0;
			int total = this.resultsSize;
			while (count < total) {
				count = count + instances.size();
				for (User user : instances) {
					writer.write(user.getUserId());
					writer.write(", ");
					writer.write(user.getName().replace(",", " "));
					writer.write(", ");
					writer.write(user.getGender());
					writer.write(", ");
					writer.write(user.getEmail().replace(",", " "));
					writer.write(", ");
					writer.write(String.valueOf(user.getEmailNotices()));
					writer.write(", ");
					if (user.getDateOfBirth() != null) {
						writer.write(new java.sql.Date(user.getDateOfBirth().getTime()).toString());
					}
					writer.write(", ");
					if (user.getUpgradeDate() != null) {
						writer.write(new java.sql.Date(user.getUpgradeDate().getTime()).toString());
					}
					writer.write(", ");
					if (user.getExpiryDate() != null) {
						writer.write(new java.sql.Date(user.getExpiryDate().getTime()).toString());
					}
					writer.write(", ");
					writer.write(new java.sql.Date(user.getCreationDate().getTime()).toString());
					writer.write("\n");
				}
				if (count < total) {
					this.page++;
					instances = getAllInstances();
				}
			}
			this.page = 0;
			writer.flush();
		} catch (Exception exception) {
			error(exception);
			return false;
		}
		return true;
	}
}

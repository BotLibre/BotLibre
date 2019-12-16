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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.botlibre.BotException;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.handinteractive.mobile.UAgentInfo;
import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.AvatarImage;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.Category;
import org.botlibre.web.admin.ContentRating;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.admin.DomainForwarder;
import org.botlibre.web.admin.Migrate;
import org.botlibre.web.admin.Payment;
import org.botlibre.web.admin.Tag;
import org.botlibre.web.admin.User;
import org.botlibre.web.admin.Friendship;
import org.botlibre.web.admin.User.CredentialsType;
import org.botlibre.web.admin.User.UserAccess;
import org.botlibre.web.admin.User.UserType;
import org.botlibre.web.admin.UserMessage;
import org.botlibre.web.admin.UserPayment;
import org.botlibre.web.admin.UserPayment.UserPaymentStatus;
import org.botlibre.web.admin.UserPayment.UserPaymentType;
import org.botlibre.web.rest.UpgradeConfig;
import org.botlibre.web.rest.UserFriendsConfig;
import org.botlibre.web.rest.WebMediumConfig;
import org.botlibre.web.service.AppIDStats;
import org.botlibre.web.service.EmailService;
import org.botlibre.web.service.IPStats;
import org.botlibre.web.service.Stats;
import org.botlibre.web.service.Translation;
import org.botlibre.web.service.TranslationService;
import org.botlibre.web.servlet.BeanServlet;

import facebook4j.Facebook;
import facebook4j.FacebookFactory;
import facebook4j.auth.AccessToken;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

@SuppressWarnings("rawtypes")
public class LoginBean extends ServletBean {
	public static String outputFilePath = "";
	public static String jqueryHeader;
	
	protected BotBean botBean;
	
	User user;
	User viewUser;
	User editUser;
	UserMessage userMessage;
	Category category;
	String categoryType = Site.TYPE;

	Map<Class, ServletBean> beans = new HashMap<Class, ServletBean>();
	
	boolean isInitialized;
	boolean bootstrap;
	boolean isLoggedIn;
	boolean help = true;
	boolean showLanguage;
	boolean passwordReset;
	Throwable error;
	boolean isEmbedded;
	boolean isEmbeddedDebug;
	boolean domainEmbedded;
	String backgroundColor = "#fff";
	String cssURL = "";
	String bannerURL = "";
	String footerURL = "";
	boolean focus = true;
	boolean fullScreen = true;
	Boolean mobile;
	BrowseBean activeBean;
	Domain domain;
	boolean ageConfirmed;
	boolean minor;
	String userFilter = "all";
	boolean showBanner = true;
	boolean showAds = false;
	boolean greet = true;
	boolean showLink = true;
	boolean facebookLogin = true;
	boolean loginBanner = true;
	boolean isHttps = false;
	boolean isSandbox = false;

	String redirect;
	String language = Site.LANG;
		
	UserPayment payment;
	
	Page pageType = Page.Home;
	public ContentRating contentRating = Site.CONTENT_RATING;
	
	boolean newSession = true;
	
	String applicationId;
	String appUser;
	String affiliate;
	
	String selectedStats;
	
	/** Token used to secure form post access. */
	String postToken;

	public enum Page { Features, Home, Browse, Search, Chat, Admin, Create }	
	
	public LoginBean() {
		this.loginBean = this;
		generatePostToken();
	}
	
	public void generatePostToken() {
		this.postToken = String.valueOf(Utils.random().nextLong());
	}
	
	public String postTokenInput() {
		return "<input name=\"postToken\" type=\"hidden\" value=\"" + getPostToken() + "\"/>";
	}
	
	public String postTokenString() {
		return "&postToken=" + getPostToken();
	}
	
	public void verifyPostToken(String token) {
		if (!this.postToken.equals(token)) {
			throw new BotException("Invalid POST token verifier. Session timed out or invalid.");
		}
	}
	
	public String getPostToken() {
		return postToken;
	}

	public void setPostToken(String postToken) {
		this.postToken = postToken;
	}

	public boolean isSandbox() {
		return isSandbox;
	}

	public void setSandbox(boolean isSandbox) {
		this.isSandbox = isSandbox;
	}
	
	public ContentRating getContentRating() {
		return contentRating;
	}

	public void setContentRating(ContentRating contentRating) {
		this.contentRating = contentRating;
	}
	
	public String getContentRatingCheckedString(ContentRating contentRating) {
		if (contentRating == this.contentRating) {
			return "selected=\"selected\"";
		}
		return "";
	}

	public boolean getBootstrap() {
		return bootstrap;
	}

	public void setBootstrap(boolean bootstrap) {
		this.bootstrap = bootstrap;
	}

	public boolean getDomainEmbedded() {
		return domainEmbedded;
	}

	public void setDomainEmbedded(boolean domainEmbedded) {
		this.domainEmbedded = domainEmbedded;
	}

	public String getAffiliate() {
		return affiliate;
	}

	public void setAffiliate(String affiliate) {
		this.affiliate = affiliate;
	}

	public boolean isHttps() {
		return isHttps;
	}

	public void setHttps(boolean isHttps) {
		this.isHttps = isHttps;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public boolean getLoginBanner() {
		return loginBanner;
	}

	public void setLoginBanner(boolean loginBanner) {
		this.loginBanner = loginBanner;
	}

	public boolean getShowLink() {
		return showLink;
	}

	public void setShowLink(boolean showLink) {
		this.showLink = showLink;
	}

	public boolean getFacebookLogin() {
		return facebookLogin;
	}

	public void setFacebookLogin(boolean facebookLogin) {
		this.facebookLogin = facebookLogin;
	}

	public String getSelectedStats() {
		return selectedStats;
	}

	public void setSelectedStats(String selectedStats) {
		this.selectedStats = selectedStats;
	}

	public String getAppUser() {
		return appUser;
	}

	public void setAppUser(String appUser) {
		this.appUser = appUser;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public void checkSessions(HttpServletRequest request) {
		if (newSession) {
			newSession = false;
			Stats.session(request);
		}
	}
	
	public String getHeaderClass(String type) {
		if (this.activeBean != null && this.activeBean.getTypeName().equals(type)) {
			return " class=\"blue\" ";
		}
		return "";
	}
	
	public String getCreateURL() {
		if (this.categoryType.equals("Bot")) {
			return "browse?browse-type=Bot&create=true";
		} else if (this.categoryType.equals("Analytic")) {
			return "browse?browse-type=Analytic&create=true";
		} else if (this.categoryType.equals("Avatar")) {
			return "browse?browse-type=Avatar&create=true";
		} else if (this.categoryType.equals("Graphic")) {
			return "browse?browse-type=Graphic&create=true";
		} else if (this.categoryType.equals("Script")) {
			return "browse?browse-type=Script&create=true";
		} else if (this.categoryType.equals("Channel")) {
			return "browse?browse-type=Channel&create=true";
		} else if (this.categoryType.equals("Forum")) {
			return "browse?browse-type=Forum&create=true";
		} else if (this.categoryType.equals("IssueTracker")) {
			return "browse?browse-type=IssueTracker&create=true";
		} else if (this.categoryType.equals("Domain")) {
			return "browse?browse-type=Domain&create=true";
		} else if (this.categoryType.equals("User")) {
			return "browse?browse-type=User&create=true";
		}
		return "create-instance.jsp";
	}
	
	public String getSearchURL() {
		if (this.categoryType.equals("Bot")) {
			return "browse?browse-type=Bot&search=true";
		} else if (this.categoryType.equals("Analytic")) {
			return "browse?browse-type=Analytic&search=true";
		} else if (this.categoryType.equals("Avatar")) {
			return "browse?browse-type=Avatar&search=true";
		} else if (this.categoryType.equals("Graphic")) {
			return "browse?browse-type=Graphic&search=true";
		} else if (this.categoryType.equals("Script")) {
			return "browse?browse-type=Script&search=true";
		} else if (this.categoryType.equals("Channel")) {
			return "browse?browse-type=Channel&search=true";
		} else if (this.categoryType.equals("Forum")) {
			return "browse?browse-type=Forum&search=true";
		} else if (this.categoryType.equals("IssueTracker")) {
			return "browse?browse-type=IssueTracker&search=true";
		} else if (this.categoryType.equals("Domain")) {
			return "browse?browse-type=Domain&search=true";
		} else if (this.categoryType.equals("User")) {
			return "browse?browse-type=User&search=true";
		}
		return "instance-search.jsp";
	}
	
	public String getBrowseURL() {
		if (this.categoryType.equals("Bot")) {
			return "browse?browse-type=Bot&browse=true";
		} else if (this.categoryType.equals("Analytic")) {
			return "browse?browse-type=Analytic&browse=true";
		} else if (this.categoryType.equals("Avatar")) {
			return "browse?browse-type=Avatar&browse=true";
		} else if (this.categoryType.equals("Graphic")) {
			return "browse?browse-type=Graphic&browse=true";
		} else if (this.categoryType.equals("Script")) {
			return "browse?browse-type=Script&browse=true";
		} else if (this.categoryType.equals("Channel")) {
			return "browse?browse-type=Channel&browse=true";
		} else if (this.categoryType.equals("Forum")) {
			return "browse?browse-type=Forum&browse=true";
		} else if (this.categoryType.equals("IssueTracker")) {
			return "browse?browse-type=IssueTracker&browse=true";
		} else if (this.categoryType.equals("Domain")) {
			return "browse?browse-type=Domain&browse=true";
		} else if (this.categoryType.equals("User")) {
			return "browse?browse-type=User&browse=true";
		} 
		return "browse.jsp";
	}
	
	public void sendMessage(String user, String subject) {
		this.userMessage = new UserMessage();
		this.userMessage.setTarget(new User(user));
		this.userMessage.setSubject(subject);
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	public boolean getGreet() {
		return greet;
	}

	public void setGreet(boolean greet) {
		this.greet = greet;
	}

	public boolean getShowAds() {
		return showAds;
	}

	public void setShowAds(boolean showAds) {
		this.showAds = showAds;
	}

	public String getUserFilter() {
		return userFilter;
	}

	public void setUserFilter(String userFilter) {
		this.userFilter = userFilter;
	}

	public String getFilterSelectedString(String filter) {
		if (filter.equals(this.userFilter)) {
			return "selected=\"selected\"";
		}
		return "";
	}

	public UserMessage getUserMessage() {
		return userMessage;
	}

	public void setUserMessage(UserMessage userMessage) {
		this.userMessage = userMessage;
	}

	public String isUserTypeSelected(String type) {
		if (getViewUser() == null) {
			return "";
		}
		if (getViewUser().getType().name().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}
	
	public User getEditUser() {
		return editUser;
	}

	public void setEditUser(User editUser) {
		this.editUser = editUser;
	}

	public boolean isAgeConfirmed() {
		return ageConfirmed;
	}

	public boolean isMinor() {
		return minor;
	}

	public void setMinor(boolean minor) {
		this.minor = minor;
	}

	public void setAgeConfirmed(boolean ageConfirmed) {
		this.ageConfirmed = ageConfirmed;
	}

	public boolean getShowBanner() {
		return showBanner;
	}

	public void setShowBanner(boolean showBanner) {
		this.showBanner = showBanner;
	}

	public String getCategoryType() {
		return categoryType;
	}

	public void setCategoryType(String categoryType) {
		this.categoryType = categoryType;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public List<UserMessage> getAllUserMessages() {
		try {
			checkLogin();
			return AdminDatabase.instance().getAllUserMessages(getUser());
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<UserMessage>();
		}
	}
	
	public int getNewMessageCount() {
		try {
			checkLogin();
			List<UserMessage> messages = getAllUserMessages();
			if (messages.isEmpty()) {
				return 0;
			}
			int count = 0;
			Date lastConnect = getUser().getOldLastConnected();
			for (UserMessage message : messages) {
				if (message.getCreationDate().after(lastConnect)) {
					count++;
				} else {
					break;
				}
			}
			return count;
		} catch (Exception exception) {
			error(exception);
			return 0;
		}
	}

	public List<UserMessage> getAllSentMessages() {
		try {
			checkLogin();
			return AdminDatabase.instance().getAllSentMessages(getUser());
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<UserMessage>();
		}
	}

	public List<Category> getAllCategories(String type) {
		try {
			return AdminDatabase.instance().getAllCategories(type, getDomain());
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Category>();
		}
	}

	public List<Payment> getAllPayments() {
		try {
			return AdminDatabase.instance().getAllPayments();
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Payment>();
		}
	}

	public List<UserPayment> getAllUserPayments() {
		try {
			return AdminDatabase.instance().getAllUserPayments();
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<UserPayment>();
		}
	}

	public String getBotUserId(String userId) {
		try {
			User botUser = AdminDatabase.instance().getUser(userId);
			if (botUser != null && botUser.isBot()) {
				BotBean bean = loginBean.getBotBean();
				String botUserId = userId.substring(1);
				bean.validateInstance(botUserId);
				BotInstance instance = getBotBean().getInstance();
				if (instance != null) {
					String botId = String.valueOf(instance.getId());
					return botId;
				}
			}
			return "";
		} catch (Exception failed) {
			error(failed);
			return null;
		}
	}
	
	public List<Friendship> getUserFriendships(String userId) {
		try {
			return AdminDatabase.instance().getUserFriendships(userId);
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Friendship>();
		}
	}
	
	public List<UserFriendsConfig> getUserFriendships(UserFriendsConfig config) {
		AdminDatabase.instance().log(Level.INFO, "API get-user-friendships", "get user friendships");
		try {
			User friend = AdminDatabase.instance().getUser(config.userFriend);
			if ((friend != null && friend.isPrivate()) && (!loginBean.isAdmin() && !friend.getUserId().equals(loginBean.getUser().getUserId()))) {
				throw new BotException("Private user - " + config.userFriend);
			}
			List<UserFriendsConfig> friendsList = new ArrayList<UserFriendsConfig>();
			List<Friendship> friendships = AdminDatabase.instance().getUserFriendships(config.userFriend);
			for (Friendship friendship : friendships) {
				UserFriendsConfig userFriendConfig = new UserFriendsConfig();
				userFriendConfig.friendship = String.valueOf(friendship.getId());
				userFriendConfig.userFriend = friendship.getFriend();
				friendsList.add(userFriendConfig);
			}
			return friendsList;
		} catch (Exception failed) {
			error(failed);
			return null;
		}
	}
	
	public List<Friendship> getUserFollowers(String userId) {
		try {
			return AdminDatabase.instance().getUserFollowers(userId);
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Friendship>();
		}
	}
	
	public List<UserFriendsConfig> getUserFollowers(UserFriendsConfig config) {
		try {
			User friend = AdminDatabase.instance().getUser(config.userFriend);
			if ((friend != null && friend.isPrivate()) && (!loginBean.isAdmin() && !friend.getUserId().equals(loginBean.getUser().getUserId()))) {
				throw new BotException("Private user - " + config.userFriend);
			}
			List<Friendship> friendshipList = AdminDatabase.instance().getUserFollowers(config.user);
			List<UserFriendsConfig> followersList = new ArrayList<UserFriendsConfig>();
			for (Friendship followers : friendshipList) {
				UserFriendsConfig userFriendConfig = new UserFriendsConfig();
				userFriendConfig.friendship = String.valueOf(followers.getId());
				userFriendConfig.userFriend = followers.getUserId();
				followersList.add(userFriendConfig);
			}
			return followersList;
		} catch (Exception failed) {
			error(failed);
			return null;
		}
	}
	
	/**
	 * Get all the users in the csv list.
	 * Used by REST, live chat.
	 */
	public List<User> getUsers(String list) {
		try {
			List<User> users = new ArrayList<User>();
			if (list == null) {
				return users;
			}
			TextStream stream = new TextStream(list);
			while (!stream.atEnd()) {
				stream.skipWhitespace();
				String id = stream.upTo(',');
				if (!stream.atEnd()) {
					stream.skip();
					stream.skipWhitespace();
				}
				if (!id.isEmpty()) {
					boolean isBot = false;
					if (id.startsWith("#")) {
						id = id.substring(1, id.length());
						try {
							Long.valueOf(id);
							isBot = true;
						} catch (NumberFormatException exception) {}
					}
					if (!isBot) {
						try {
							User user = AdminDatabase.instance().validateUser(id);
							users.add(user);
						} catch (Exception missing) {
							isBot = true;
						}
					}
					if (isBot) {
						try {
							if (getBotBean().validateInstance(id)) {
								BotInstance instance = getBotBean().getInstance();
								User user = new User();
								user.setUserId(String.valueOf(instance.getName()));
								user.setName(instance.getName());
								if (instance.getAvatar() == null && instance.getInstanceAvatar() != null) {
									user.setAvatar(instance.getInstanceAvatar().getAvatar());
								} else {
									user.setAvatar(instance.getAvatar());
								}
								user.setBio(instance.getDescription());
								user.setCreationDate(instance.getCreationDate());
								user.setLastConnected(instance.getLastConnected());
								user.setConnects(instance.getConnects());
								user.setType(instance.getCreator().getType());
								user.setIsBot(true);
								user.setInstanceAvatar(instance.getInstanceAvatar());
								users.add(user);
							} else {
								User user = new User();
								user.setUserId(id);
								users.add(user);
							}
						} catch (Exception ignore) {
							User user = new User();
							user.setUserId(id);
							users.add(user);
						}
					}
				}
			}
			setError(null);
			return users;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<User>();
		}
	}

	public String getAllCategoriesString() {
		return getAllCategoriesString(getCategoryType());
	}

	public String getAllCategoriesString(String type) {
		StringWriter writer = new StringWriter();
		int count = 1;
		List<Category> categories = getAllCategories(type);
		for (Category category : categories) {
			writer.write("\"");
			writer.write(category.getName());
			writer.write("\"");
			if (count < categories.size())
			writer.write(", ");
			count++;
		}
		return writer.toString();
	}

	public Domain getDomain() {
		DomainBean domainBean = getBean(DomainBean.class);
		if ((domainBean.getInstance() != null) && (domainBean.getInstance().getId() != null)) {
			return domainBean.getInstance();
		}
		if (this.domain == null) {
			this.domain = AdminDatabase.instance().getDefaultDomain();
		}
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	public LoginBean clone() {
		try {
			LoginBean clone = (LoginBean)super.clone();
			clone.beans = new HashMap<Class, ServletBean>();
			if (clone.botBean != null) {
				clone.botBean = (BotBean)clone.botBean.clone();
				clone.botBean.loginBean = clone;
			}
			return clone;
		} catch (Exception exception) {
			throw new Error(exception);
		}
	}
	
	public void checkMobile(HttpServletRequest request) {
		if (this.mobile == null) {
			String userAgent = request.getHeader("User-Agent");
			String httpAccept = request.getHeader("Accept");
	
			UAgentInfo detector = new UAgentInfo(userAgent, httpAccept);
	
			if (detector.detectMobileQuick()) {
				this.mobile = true;
			} else {
				this.mobile = false;
			}
		}
	}

	public boolean isFullScreen() {
		return fullScreen;
	}

	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
	}

	public boolean isMobile() {
		if (mobile == null) {
			return false;
		}
		return mobile;
	}

	public void setMobile(boolean mobile) {
		this.mobile = mobile;
	}

	public boolean getFocus() {
		return focus;
	}

	public void setFocus(boolean focus) {
		this.focus = focus;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public static String getJQueryHeader() {
		if (jqueryHeader == null) {
			jqueryHeader =
				"\t<link rel=\"stylesheet\" href=\"scripts/jquery/jquery-ui.min.css\">\n"
				+ "\t<script src=\"scripts/jquery/jquery.js\"></script>\n"
				+ "\t<script src=\"scripts/jquery/jquery-ui.min.js\"></script>\n"
				+ "\t<script src=\"scripts/webui.js\"></script>\n"
				+ "\t<script>"
				+ "\t$(function() {"
				+ "\t$( \"#accordion\" ).accordion();"
				+ "\t});"
				+ "\t$(function() {"
				+ "\t$( \"#tabs\" ).tabs();"
				+ "\t});"
				+ "\t</script>";
		}
		return jqueryHeader;
	}

	public String getCssURL() {
		return cssURL;
	}

	public void setCssURL(String cssURL) {
		this.cssURL = cssURL;
	}

	public String getBannerURL() {
		return bannerURL;
	}

	public void setBannerURL(String bannerURL) {
		this.bannerURL = bannerURL;
	}

	public String getFooterURL() {
		return footerURL;
	}

	public void setFooterURL(String footerURL) {
		this.footerURL = footerURL;
	}

	public String getAvatarImage(AvatarImage avatar) {
		if (avatar != null) {
			String fileName = "avatars/" + "a" + avatar.getId() + ".jpg";
			String path = LoginBean.outputFilePath + "/" + fileName;
			byte[] image = avatar.getImage();
			if (image != null) {
				File file = new File(path);
				if (!file.exists()) {
					try {
						FileOutputStream stream = new FileOutputStream(file);
						stream.write(image);
						stream.flush();
						stream.close();
					} catch (IOException exception) {
						error(exception);
						return "images/bot.png";
					}
				}
				return fileName;
			}
		}
		return "images/bot.png";
	}

	public String getAvatarThumb(AvatarImage avatar, int size) {
		if (avatar != null) {
			String fileName = "avatars/" + "at" + avatar.getId() + ".jpg";
			String path = LoginBean.outputFilePath + "/" + fileName;
			byte[] image = avatar.getThumb();
			if (image == null && avatar.getImage() != null) {
				image = Utils.createThumb(avatar.getImage(), size);
				avatar.setThumb(image);
				AdminDatabase.instance().updateAvatar(avatar);
			}
			if (image != null) {
				File file = new File(path);
				if (!file.exists()) {
					try {
						FileOutputStream stream = new FileOutputStream(file);
						stream.write(image);
						stream.flush();
						stream.close();
					} catch (IOException exception) {
						error(exception);
						return "images/bot-thumb.jpg";
					}
				}
				return fileName;
			}
		}
		return "images/bot-thumb.jpg";
	}

	public String getAvatarImage(User user) {
		if (user != null) {
			try {
				user = AdminDatabase.instance().validateUser(user.getUserId());
				String image = getAvatarImage(user.getAvatar());
				if (user.isBot() && "images/bot.png".equals(image)) {
					return "images/bot.png";
				}
				if (image.equals("images/bot.png")) {
					return "images/user.png";
				}
				return image;
			} catch (Exception ignore) {}
		}
		return "images/user.png";
	}

	public String getAvatarThumb(User user) {
		if (user != null) {
			try {
				if (!user.isBot()) {
					user = AdminDatabase.instance().validateUser(user.getUserId());
				}
				String image = getAvatarThumb(user.getAvatar(), 192);
				if (user.isBot() && "images/bot-thumb.jpg".equals(image)) {
					return "images/bot-thumb.jpg";
				}
				if (image.equals("images/bot-thumb.jpg")) {
					return "images/user-thumb.jpg";
				}
				return image;
			} catch (Exception ignore) {}
		}
		return "images/user-thumb.jpg";
	}

	public String getAvatarThumb(Category category) {
		String image = getAvatarThumb(category.getAvatar(), 146);
		if (image.equals("images/bot-thumb.jpg")) {
			return "images/category-thumb.jpg";
		}
		return image;
	}

	public String getAvatarImage(Category category) {
		String image = getAvatarImage(category.getAvatar());
		if (image.equals("images/bot.png")) {
			return "images/category.png";
		}
		return image;
	}

	public void setBotBean(BotBean botBean) {
		this.botBean = botBean;
	}

	public BotBean getBotBean() {
		if (this.botBean == null) {
			this.botBean = new BotBean();
			this.botBean.setLoginBean(this);
		}
		return this.botBean;
	}
	
	public void initialize(ServletContext context, HttpServletRequest request, HttpServletResponse response) {
		if (!this.isInitialized) {
			this.isInitialized = true;
			if ((outputFilePath == null) || outputFilePath.equals("")) {
				outputFilePath = context.getRealPath("");
			}
			this.isHttps = request.isSecure();
			String server = request.getServerName().toLowerCase();
			if (!server.equals(Site.SERVER_NAME) && !server.equals(Site.SERVER_NAME2)) {
				this.isSandbox = true;
			}
			if (!isLoggedIn()) {
				Cookie[] cookies = request.getCookies();
				String user = null;
				long token = 0;
				String domain = null;
				if (cookies != null) {
					for (int i = 0; i < cookies.length; i++) {
						Cookie cookie = cookies[i];
						if (cookie.getName().equals("user")) {
							user = cookie.getValue();
						} else if (cookie.getName().equals("token")) {
							long value = Long.valueOf(cookie.getValue());
							if (value != 0) {
								token = value;
							}
						} else if (cookie.getName().equals("lastdomain")) {
							domain = cookie.getValue();
						}
					}
				}
				if ((user != null) && (token != 0)) {
					// Cookies do not work from included jsp...
					long newToken = connect(user, null, token, domain);
					if (newToken == 0) {
						Cookie cookie = new Cookie("token", "");
						cookie.setMaxAge(0);
						cookie.setPath("/");
						response.addCookie(cookie);
						cookie = new Cookie("lastdomain", "");
						cookie.setMaxAge(0);
						cookie.setPath("/");
						response.addCookie(cookie);
						cookie = new Cookie("user", "");
						cookie.setMaxAge(0);
						cookie.setPath("/");
						response.addCookie(cookie);
						
						cookie = new Cookie("token", "");
						cookie.setMaxAge(0);
						response.addCookie(cookie);
						cookie = new Cookie("lastdomain", "");
						cookie.setMaxAge(0);
						response.addCookie(cookie);
						cookie = new Cookie("user", "");
						cookie.setMaxAge(0);
						response.addCookie(cookie);
					} else if (newToken != token) {
						Cookie cookie = new Cookie("token", String.valueOf(newToken));
						cookie.setMaxAge(60*60*24*30);
						response.addCookie(cookie);
					}
				}
			}
		}
	}
	
	public Page getPageType() {
		return pageType;
	}

	public void setPageType(Page pageType) {
		this.pageType = pageType;
	}

	public boolean getHelp() {
		return help;
	}

	public void setHelp(boolean help) {
		this.help = help;
	}

	public boolean blockUser() {
		try {
			checkSuper();
			setViewUser(AdminDatabase.instance().blockUser(this.viewUser.getUserId()));
			if (this.viewUser.getIP() != null) {
				AdminDatabase.bannedIPs.put(this.viewUser.getIP(), this.viewUser.getIP());
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean unblockUser() {
		try {
			checkSuper();
			setViewUser(AdminDatabase.instance().unblockUser(this.viewUser.getUserId()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	public Long getUserAppId() {
		if (isLoggedIn() && getUser().getApplicationId() == null) {
			getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
		}
		if (isLoggedIn() && hasValidApplicationId()) {
			return getUser().getApplicationId();
		}
		return AdminDatabase.getTemporaryApplicationId();
	}

	public boolean resetAppId() {
		try {
			checkAdmin();
			AppIDStats oldStats = AppIDStats.getStats(String.valueOf(getViewUser().getApplicationId()), getViewUser().getUserId());
			setViewUser(AdminDatabase.instance().resetAppId(this.viewUser.getUserId()));
			AppIDStats stats = AppIDStats.getStats(String.valueOf(getViewUser().getApplicationId()), getViewUser().getUserId());
			stats.apiCalls = oldStats.apiCalls;
			stats.overLimit = oldStats.overLimit;
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean deleteUser() {
		try {
			checkAdmin();
			EntityManager em = AdminDatabase.instance().getFactory().createEntityManager();
			try {
				Query query = em.createQuery("Select count(p) from BotInstance p where p.creator = :user");
				query.setParameter("user", this.viewUser);
				if (((Number)query.getSingleResult()).intValue() > 0) {
					throw new BotException("You must first delete your bots");
				}
				query = em.createQuery("Select count(p) from ChatChannel p where p.creator = :user");
				query.setParameter("user", this.viewUser);
				if (((Number)query.getSingleResult()).intValue() > 0) {
					throw new BotException("You must first delete your channels");
				}
				query = em.createQuery("Select count(p) from Forum p where p.creator = :user");
				query.setParameter("user", this.viewUser);
				if (((Number)query.getSingleResult()).intValue() > 0) {
					throw new BotException("You must first delete your forums");
				}
				query = em.createQuery("Select count(p) from Analytic p where p.creator = :user");
				query.setParameter("user", this.viewUser);
				if (((Number)query.getSingleResult()).intValue() > 0) {
					throw new BotException("You must first delete your analytics");
				}
				query = em.createQuery("Select count(p) from Graphic p where p.creator = :user");
				query.setParameter("user", this.viewUser);
				if (((Number)query.getSingleResult()).intValue() > 0) {
					throw new BotException("You must first delete your graphics");
				}
				query = em.createQuery("Select count(p) from Avatar p where p.creator = :user");
				query.setParameter("user", this.viewUser);
				if (((Number)query.getSingleResult()).intValue() > 0) {
					throw new BotException("You must first delete your avatars");
				}
				query = em.createQuery("Select count(p) from Script p where p.creator = :user");
				query.setParameter("user", this.viewUser);
				if (((Number)query.getSingleResult()).intValue() > 0) {
					throw new BotException("You must first delete your scripts");
				}
				query = em.createQuery("Select count(p) from IssueTracker p where p.creator = :user");
				query.setParameter("user", this.viewUser);
				if (((Number)query.getSingleResult()).intValue() > 0) {
					throw new BotException("You must first delete your issue trackers");
				}
			} finally {
				em.close();
			}
			AdminDatabase.instance().deleteUser(this.viewUser.getUserId());
			setViewUser(null);
			if (!isSuper()) {
				logout();
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean deleteUserMessage() {
		try {
			checkLogin();
			AdminDatabase.instance().deleteUserMessage(this.userMessage);
			setUserMessage(null);
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean flagUser(String reason) {
		try {
			checkLogin();
			setViewUser(AdminDatabase.instance().flagUser(this.viewUser.getUserId(), this.user.getUserId(), reason));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean unflagUser() {
		try {
			checkLogin();
			setViewUser(AdminDatabase.instance().unflagUser(this.viewUser.getUserId()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public void checkLogin() {
		if (!this.isLoggedIn() || (this.user == null)) {
			throw new BotException("You must login first");
		}
	}

	public void checkAdmin() {
		checkLogin();
		if (!isSuperUser() && !getUser().equals(this.viewUser)) {
			throw new BotException("Illegal access");
		}
	}

	public void checkVerified(WebMediumConfig config) {
		if (Site.READONLY) {
			throw new BotException("This website is currently undergoing maintence, please try later.");
		}
		if (Site.VERIFY_EMAIL && !this.user.isVerified() && this.user.isBasic()
				&& config.application == null
				&& !config.isPrivate && !config.isHidden) {
			throw new BotException("To prevent spam, you must verify your email address before creating public content. You can verify your email address from your user page.");
		}
	}

	/**
	 * Verify the domains allows access;
	 */
	public boolean checkDomainAccess() {
		try {
			getDomain().checkAccess(getUser());
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public void checkSuper() {
		checkLogin();
		if (!isSuper()) {
			throw new BotException("Illegal access");
		}
	}

	public void checkSuperUser() {
		checkLogin();
		if (!isSuperUser()) {
			throw new BotException("Illegal access");
		}
	}

	public boolean isSuper() {
		if (!this.isLoggedIn || this.user == null) {
			return false;
		}
		return this.user.isSuperUser() || this.user.isAdminUser();
	}

	public boolean isSuperUser() {
		if (!this.isLoggedIn || this.user == null) {
			return false;
		}
		return this.user.isSuperUser();
	}

	public boolean isAdmin() {
		if (!this.isLoggedIn) {
			return false;
		}
		return getDomain().isAdmin(getUser());
	}
	
	/**
	 * Connect to the specified bot instance.
	 */
	public long connect(String user, String password, long token) {
		return connect(user, password, token, null);
	}
	
	/**
	 * Connect to the user using Facebook or Google credentials.
	 */
	public long credentialsConnect(String credentialsType, String credentialsUserID, String credentialsToken) {
		Stats.stats.userConnects++;
		try {
			if ((credentialsUserID == null) || credentialsUserID.isEmpty()) {
				throw new BotException("User ID not set");
			}
			if ((credentialsToken == null) || credentialsToken.isEmpty()) {
				throw new BotException("Access token not set");
			}
			if (credentialsToken.length() > 1024) {
				// Truncate for now.
				credentialsToken = credentialsToken.substring(0, 1024);
			}
			if (credentialsType.equals(CredentialsType.Facebook.name())) {
				Facebook facebook = new FacebookFactory().getInstance();
				facebook.setOAuthAppId(Site.FACEBOOK_APPID, Site.FACEBOOK_APPSECRET);
				facebook.setOAuthAccessToken(new AccessToken(credentialsToken, null));
				if (!facebook.getMe().getId().equals(credentialsUserID)) {
					throw new BotException("Facebook ID does not match access token");
				}
				facebook.setOAuthAccessToken(null);
			}
			try {
				setUser(AdminDatabase.instance().validateCredentialsUser(credentialsUserID, credentialsToken));
			} catch (Exception failed) {
				error(failed);
				return 0;
			}
			setLoggedIn(true);
			if (Site.COMMERCIAL && !isSuper()) {
				List<Domain> domains = AdminDatabase.instance().getUserDomains(getUser());
				if (!domains.isEmpty()) {
					//if (domain == null || domain.isEmpty()) {
						getBean(DomainBean.class).setInstance(domains.get(0));
					//} else {
					//	getBean(DomainBean.class).validateInstance(domain);
					//}
				}
			}
		} catch (Exception failed) {
			error(failed);
			return 0;
		}
		return getUser().getToken();
	}
	
	/**
	 * Connect to the specified bot instance.
	 */
	public long connect(String user, String password, long token, String domain) {
		Stats.stats.userConnects++;
		try {
			if (user != null) {
				user = user.trim();
			}
			if (password != null) {
				password = password.trim();
			}
			setEditUser(new User(user, password));
			if ((user == null) || user.equals("") || user.equals("anonymous")) {
				throw new BotException("Invalid user - " + user);
			}
			if (((password == null) || password.equals("")) && token == 0) {
				throw new BotException("Invalid password");
			}
			if (validateUser(user, password, token, true, false) == 0) {
				Utils.sleep(1000);
				return 0;
			}
			setEditUser(null);
			setLoggedIn(true);
			if (Site.COMMERCIAL && !isSuper()) {
				List<Domain> domains = AdminDatabase.instance().getUserDomains(getUser());
				if (!domains.isEmpty()) {
					//if (domain == null || domain.isEmpty()) {
						getBean(DomainBean.class).setInstance(domains.get(0));
					//} else {
					//	getBean(DomainBean.class).validateInstance(domain);
					//}
				}
			}
		} catch (Exception failed) {
			error(failed);
			return 0;
		}
		return getUser().getToken();
	}
	
	/**
	 * Lets the super user become any user.
	 */
	public void becomeUser() {
		checkSuperUser();
		try {
			if (getViewUser().isSuperUser()) {
				throw new BotException("Cannot become super user");
			}
			setUser(new User(getViewUser().getUserId(), null));
			validateUser(getViewUser().getUserId(), null, 0, false, true);
			setLoggedIn(true);
			if (Site.COMMERCIAL && !isSuperUser()) {
				List<Domain> domains = AdminDatabase.instance().getUserDomains(getUser());
				if (!domains.isEmpty()) {
					getBean(DomainBean.class).setInstance(domains.get(0));
				}
			}
		} catch (Exception failed) {
			error(failed);
			return;
		}
	}
	
	public void logout() {
		this.isLoggedIn = false;
		this.user = null;
		this.editUser = null;
		this.greet = false;
	}
	
	/**
	 * Validate and set the user and password.
	 */
	public long validateUser(String user, String password, long token, boolean reset, boolean wasSuper) {
		try {
			if (user != null) {
				user = user.trim();
			}
			if (password != null) {
				password = password.trim();
			}
			setUser(AdminDatabase.instance().validateUser(user, password, token, reset, wasSuper));
		} catch (Exception failed) {
			error(failed);
			return 0;
		}
		return getUser().getToken();
	}
	
	/**
	 * Validate and set the view user.
	 */
	public boolean viewUser(String user) {
		try {
			setViewUser(AdminDatabase.instance().validateUser(user));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean viewMessage(String message) {
		try {
			setUserMessage(AdminDatabase.instance().validateUserMessage(message));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Validate and set the instance.
	 */
	public boolean editUser() {
		try {
			checkLogin();
			if (isSuperUser() && getViewUser() != null) {
				setEditUser(getViewUser());
			} else {
				setEditUser(getUser());
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Create a new user.
	 */
	public boolean createUser(String userId, String password, String password2, String dateOfBirth, String hint, String name, String ip, String source, String affiliate,
			String userAccess, String email, String website, String bio, boolean displayName, boolean over18,
			String credentialsType, String credentialsUserID, String credentialsToken,
			boolean terms) {
		try {
			if (Site.READONLY) {
				throw new BotException("This website is currently undergoing maintence, please try later.");
			}
			if (userId != null) {
				userId = userId.trim().toLowerCase();
			}
			if (password != null) {
				password = password.trim();
			}
			if (password2 != null) {
				password2 = password2.trim();
			}
			if (hint != null) {
				hint = hint.trim();
			}
			if (name != null) {
				name = name.trim();
			}
			if (email != null) {
				email = email.trim().toLowerCase();
			}
			if (userAccess != null) {
				userAccess = userAccess.trim();
			}
			if (website != null) {
				website = website.trim();
			}
			User user = new User(userId, password);
			if (credentialsType != null && !credentialsType.isEmpty()) {
				if (credentialsToken.length() > 1024) {
					// Truncate for now.
					credentialsToken = credentialsToken.substring(0, 1024);
				}
				user.setCredentialsType(CredentialsType.valueOf(credentialsType));
				user.setCredentialsUserID(credentialsUserID);
				user.setCredentialsToken(credentialsToken);
			}
			Date birthDate = null;
			if (dateOfBirth != null) {
				birthDate = Utils.parseDate(dateOfBirth);
				user.setDateOfBirth(birthDate);
			}
			user.setHint(hint);
			user.setName(name);
			if (userAccess != null && !userAccess.isEmpty()) {
				user.setAccess(UserAccess.valueOf(userAccess));
			}
			user.setShoulsDisplayName(displayName);
			user.setSource(source);
			if (affiliate == null || affiliate.isEmpty()) {
				if (this.affiliate != null && !this.affiliate.isEmpty()) {
					affiliate = this.affiliate;
				} else {
					DomainBean domainBean = loginBean.getBean(DomainBean.class);
					if (domainBean.getInstance() != null && domainBean.getInstance().getCreator() != null) {
						affiliate = domainBean.getInstance().getCreator().getUserId();
					}
				}
			}
			user.setAffiliate(affiliate);
			user.setIP(ip);
			user.setEmail(email);
			user.setWebsite(website);
			user.setBio(bio);
			user.setOver18(over18);
			setUser(user);
			if (!terms) {
				throw new BotException("You must accept our terms of service");
			}
			if (Site.AGE_RESTRICT && dateOfBirth != null) {
				if (birthDate == null) {
					throw new BotException("Please enter a valid date of birth");
				}
				if (System.currentTimeMillis() - birthDate.getTime() < (Utils.YEAR)) {
					throw new BotException("Please enter a valid date of birth");
				}
				if (System.currentTimeMillis() - birthDate.getTime() < (Utils.YEAR * 13)) {
					this.minor = true;
					throw new BotException("You must be 13 years old or older to access this service");
				} else if (this.minor) {
					throw new BotException("You must be 13 years old or older to access this service (you cannot change your date of birth once entered)");
				}
			}
			if ((userId == null) || userId.equals("") || userId.equals("anonymous")) {
				throw new BotException("Invalid user id");
			}
			if (userId.indexOf('@') != -1) {
				throw new BotException("User id cannot contain @, such as an email address.  Just use your user name, i.e. jonsmith not jonsmith@mail.com");
			}
			if (!Utils.isAlphaNumeric(userId)) {
				throw new BotException("Invalid character in user id, only use alpha-numeric characters");
			}
			if (user.getCredentialsType() == null) {
				if ((password == null) || password.equals("")) {
					throw new BotException("Invalid password");
				}
				if (password.equals("password")) {
					throw new BotException("Password cannot be 'password'");
				}
				if (password.length() < 8) {
					throw new BotException("Passwords must be at least 8 characters");
				}
				if (!password.equals(password2)) {
					throw new BotException("Passwords do not match");
				}
			} else {
				if (AdminDatabase.instance().checkCredentialsUser(credentialsUserID) != null) {
					throw new BotException("User already exists for this " + credentialsType + " account, just sign in");
				}
				if (user.getCredentialsType() == CredentialsType.Facebook) {
					Facebook facebook = new FacebookFactory().getInstance();
					facebook.setOAuthAppId(Site.FACEBOOK_APPID, Site.FACEBOOK_APPSECRET);
					facebook.setOAuthAccessToken(new AccessToken(credentialsToken, null));
					if (!facebook.getMe().getId().equals(credentialsUserID)) {
						throw new BotException("Facebook ID does not match access token");
					}
					facebook.setOAuthAccessToken(null);
				}
			}
			if (Site.VERIFYUSERS) {
				if ((name == null) || name.equals("")) {
					throw new BotException("Invalid name");
				}
				if ((email == null) || email.equals("") || !email.contains("@")) {
					throw new BotException("Invalid email");
				}
			}
			Stats.stats.userCreates++;
			user = AdminDatabase.instance().createUser(user);
			setUser(user);
			setLoggedIn(true);
			setUser(sendEmailVerify(getUser()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	/**
	 * Verify the user accepted the terms and their age.
	 */
	public boolean verifyAnonymous(String dateOfBirth, boolean terms) {
		try {
			Date birthDate = null;
			if (dateOfBirth != null) {
				birthDate = Utils.parseDate(dateOfBirth);
			}
			if (!terms) {
				throw new BotException("You must accept our terms of service");
			}
			if (Site.AGE_RESTRICT && dateOfBirth != null) {
				if (birthDate == null) {
					throw new BotException("Please enter a valid date of birth");
				}
				if (System.currentTimeMillis() - birthDate.getTime() < (Utils.YEAR)) {
					throw new BotException("Please enter a valid date of birth");
				}
				if (System.currentTimeMillis() - birthDate.getTime() < (Utils.YEAR * 13)) {
					this.minor = true;
					throw new BotException("You must be 13 years old or older to access this service");
				} else if (this.minor) {
					throw new BotException("You must be 13 years old or older to access this service (you cannot change your date of birth once entered)");
				}
			}
			setAgeConfirmed(true);
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public User sendEmailVerify(User user) {
		user = AdminDatabase.instance().resetUserVerifyToken(user.getUserId());
		if (user.hasEmail()) {
			EmailService.instance().sendEmail(user.getEmail(), "Welcome to " + Site.NAME,
					"Welcome " + user.getUserId() + " to " + Site.NAME + "\nPlease verify your email address by clicking the following link, "
						+ Site.SECUREURLLINK + "/login?verify-user=" + loginBean.encodeURI(getUser().getUserId()) + "&token=" + user.getVerifyToken()
						+ "\n\nIf you did not create this account, please disregard this message."
						+ "\n\n------"
						+ "\n\nThis is an automated email from " + Site.NAME + " - " + Site.SECUREURLLINK + ".",

					"Welcome " + user.getUserId() + " to " + Site.NAME + "\n<br/>Please verify your email address by clicking the following link, <a href=\""
						+ Site.SECUREURLLINK + "/login?verify-user=" + loginBean.encodeURI(user.getUserId()) + "&token=" + user.getVerifyToken() + "\">"
						+ Site.SECUREURLLINK + "/login?verify-user=" + loginBean.encodeURI(user.getUserId()) + "&token=" + user.getVerifyToken() + "</a>"
						+ "\n<p>If you did not create this account, please disregard this message."
						+ "\n<p><hr>"
						+ "\n<p>This is an automated email from " + Site.NAME + " - <a href=\"" + Site.SECUREURLLINK + "\">" + Site.SECUREURLLINK + "</a>.");
		}
		return user;
	}

	public void sendEmailUpdateVerify() {
		User user = getEditUser();
		if (user != null) {
			user = AdminDatabase.instance().resetUserVerifyToken(user.getUserId());
			setEditUser(user);
		} else {
			user = AdminDatabase.instance().resetUserVerifyToken(getUser().getUserId());
			setUser(user);
		}
		if (user.hasEmail()) {
			EmailService.instance().sendEmail(user.getEmail(), "Email verfication from " + Site.NAME,
					"This email address was updated by " + user.getUserId() + " on " + Site.NAME + "\nPlease verify your email address by clicking the following link, "
						+ Site.SECUREURLLINK + "/login?verify-user=" + loginBean.encodeURI(user.getUserId()) + "&token=" + user.getVerifyToken()
						+ "\n\nIf you did not update this account, please disregard this message."
						+ "\n\n------"
						+ "\n\nThis is an automated email from " + Site.NAME + " - " + Site.SECUREURLLINK + ".",

					"This email address was updated by " + user.getUserId() + " on " + Site.NAME + "\n<br/>Please verify your email address by clicking the following link, <a href=\""
						+ Site.SECUREURLLINK + "/login?verify-user=" + loginBean.encodeURI(user.getUserId()) + "&token=" + user.getVerifyToken() + "\">"
						+ Site.SECUREURLLINK + "/login?verify-user=" + loginBean.encodeURI(user.getUserId()) + "&token=" + user.getVerifyToken() + "</a>"
						+ "\n<p>If you did not update this account, please disregard this message."
						+ "\n<p><hr>"
						+ "\n<p>This is an automated email from " + Site.NAME + " - <a href=\"" + Site.SECUREURLLINK + "\">" + Site.SECUREURLLINK + "</a>.");
		}		
	}

	public void resetPassword(String userid, String email) {
		try {
			User user = AdminDatabase.instance().validateUser(userid);
			if (user.getEmail().isEmpty()) {
				throw new BotException("User does not have a registered email address, please contact support@" + Site.EMAILHOST + ".");
			}
			if (!user.getEmail().equalsIgnoreCase(email)) {
				throw new BotException("Email address does not match the user's registered email address.");
			}
			user = AdminDatabase.instance().resetUserVerifyToken(userid);
			EmailService.instance().sendEmail(user.getEmail(), "Password reset request for " + Site.NAME,
					"A request to reset the password for the user " + user.getUserId() + " of " + Site.NAME + " was submitted\nReset your password by clicking the following link, "
						+ Site.SECUREURLLINK + "/login?verify-reset-password=" + loginBean.encodeURI(user.getUserId()) + "&token=" + user.getVerifyToken()
						+ "\n\nIf you did not request a password reset, please disregard this message."
						+ "\n\n------"
						+ "\n\nThis is an automated email from " + Site.NAME + " - " + Site.SECUREURLLINK + ".",
						
					"A request to reset the password for the user " + user.getUserId() + " of " + Site.NAME + " was submitted\n<br/>Reset your password by clicking the following link, <a href=\""
							+ Site.SECUREURLLINK + "/login?verify-reset-password=" + loginBean.encodeURI(user.getUserId()) + "&token=" + user.getVerifyToken() + "\">"
							+ Site.SECUREURLLINK + "/login?verify-reset-password=" + loginBean.encodeURI(user.getUserId()) + "&token=" + user.getVerifyToken() + "</a>"
							+ "\n<p>If you did not request a password reset, please disregard this message."
							+ "\n<p><hr>"
							+ "\n<p>This is an automated email from " + Site.NAME + " - <a href=\"" + Site.SECUREURLLINK + "\">" + Site.SECUREURLLINK + "</a>.");
			
			throw new BotException("An email has been sent to " + email + " please check your email and click on the reset link.\nIt may take a few minutes for the email to be delivered.");
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void contact(String email, String name, String business, String details, boolean demo) {
		try {
			EmailService.instance().sendEmail(Site.EMAILSALES, "Contact request from " + Site.NAME,
					"A contact request was received from " + getUserId() + " on " + Site.NAME
						+ "\n\nEmail: " + email
						+ "\nName: " + name
						+ "\nBusiness Name: " + business
						+ "\nDemo: " + demo
						+ "\nDetails:\n" + details
						+ "\n\n------"
						+ "\n\nThis is an automated email from " + Site.NAME + " - " + Site.SECUREURLLINK + ".",
						
					"A contact request was received from " + getUserId() + " on " + Site.NAME
						+ "\n\n<p>Email: " + email
						+ "\n<br>Name: " + name
						+ "\n<br>Business Name: " + business
						+ "\n<br>Demo: " + demo
						+ "\n<br>Details:\n" + details
						+ "\n\n<p><hr>"
						+ "\n<p>This is an automated email from " + Site.NAME + " - <a href=\"" + Site.SECUREURLLINK + "\">" + Site.SECUREURLLINK + "</a>.");
			
			throw new BotException("Message sent, a sales person will contact you as soon as possible");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public Friendship createUserFriendship(String userTargetId) {
		try {
			checkLogin();
			User friend = AdminDatabase.instance().getUser(userTargetId);
			if ((friend != null && friend.isPrivate()) && (!loginBean.isAdmin() && !friend.getUserId().equals(loginBean.getUser().getUserId()))) {
				throw new BotException("Cannot friend private profiles - " + userTargetId);
			}
			User botUser = checkBotUser(userTargetId);
			if (botUser != null) {
				userTargetId = "@" + botBean.getInstance().getAlias();
			}
			Friendship friendship = AdminDatabase.instance().createUserFriendship(loginBean.getUser().getUserId(), userTargetId);
			if (botUser != null) {
				// Process bot's greeting.
				createUserGreeting(userTargetId, "Friend");
			}
			return friendship;
		} catch (Exception failed) {
			error(failed);
		}
		return null;
	}
	
	public boolean deleteUserFriendship(String friendId) {
		try {
			checkLogin();
			AdminDatabase.instance().removeUserFriendship(loginBean.getUserId(), friendId);
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Create a greeting message from the bot to the user on a friend request.
	 */
	public UserMessage createUserGreeting(String target, String subject) {
		UserMessage newUserMessage = null;
		try {
			checkLogin();
			if (target != null) {
				target = target.trim();
			}
			if (subject != null) {
				subject = subject.trim();
			}
			Stats.stats.userMessages++;
			
			String reply = "";
			User botUser = checkBotUser(target);
			if (botUser == null) {
				throw new BotException("Invalid user - " + target);
			}
			ChatBean chatBean = this.loginBean.getBean(ChatBean.class);
			boolean speak = chatBean.getSpeak();
			chatBean.setSpeak(false);
			chatBean.processInput(null, false, false, false);
			chatBean.setSpeak(speak);
			reply = chatBean.getResponse();
			if (reply != null && !reply.isEmpty()) {
				// Send greeting.
				UserMessage replyMessage = new UserMessage();
				replyMessage.setSubject(subject);
				replyMessage.setMessage(reply);
				replyMessage.setOwner(getUser());
				replyMessage.setCreator(botUser);
				replyMessage.setTarget(getUser());
				replyMessage = AdminDatabase.instance().createUserMessage(replyMessage);
			
				if (getUser().hasEmail() && getUser().isVerified() && getUser().getEmailMessages() && !getUser().isBot()) {
					StringWriter writer = new StringWriter();
					writer.write("You have received a message on ");
					writer.write(Site.NAME);
					writer.write(" from ");
					writer.write(botUser.getUserId());
					writer.write(".\n<p>");
					writer.write("To view this message on ");
					writer.write(Site.NAME);
					writer.write(" click <a href=\"" + Site.SECUREURLLINK + "/login?browse-user-messages=true\">here</a>.\n<p>");
					writer.write("\n<br/>Message follows:\n\n<br/><p>");
					writer.write("<b>" + subject + "</b>\n<p>");
					writer.write(replyMessage.getMessageText());
					writer.write("\n<p>");
					writer.write("\n<br/><hr>");
					writer.write("\n<p>This is an automated email from " + Site.NAME + " - <a href=\"" + Site.SECUREURLLINK + "\">" + Site.SECUREURLLINK + "</a>.");
					writer.write("\n<p>You can update your email preferences from your <a href=\"" + Site.SECUREURLLINK + "/login?sign-in=true\">profile page</a> (click edit).");
					writer.write("\n<p><a href=\"" + Site.SECUREURLLINK + "/login?unsubscribe=messages&user="
								+ getUser().getUserId() + "&token=" + getUser().getVerifyToken() + "\">Unsubscribe from messages notices</a>.");
					writer.write("\n<p><a href=\"" + Site.SECUREURLLINK + "/login?unsubscribe=all&user="
							+ getUser().getUserId() + "&token=" + getUser().getVerifyToken() + "\">Unsubscribe from all notices</a>.");
					EmailService.instance().sendEmail(getUser().getEmail(), "You have received a message on " + Site.NAME + " - " + subject, null, writer.toString());
				}
			}
		} catch (Exception failed) {
			error(failed);
			return null;
		}
		return newUserMessage;
	}
	
	public User checkBotUser(String userId) {
		if (userId.startsWith("@")) {
			BotBean botBean = this.loginBean.getBotBean();
			String botId = userId.substring(1, userId.length());
			if (!botBean.validateInstance(botId)) {
				throw new BotException("Bot - " + botId + " does not exist");
			}
			userId = "@" + botBean.getInstance().getAlias();
			User botUser = AdminDatabase.instance().getUser(userId);
			if (botUser == null) {
				botUser = new User();
				botUser.setUserId(userId);
				botUser.setIsBot(true);
				BotInstance bot = botBean.getInstance();
				botUser.setName(bot.getName());
				if (bot.isPrivate()) {
					botUser.setAccess(User.UserAccess.Private);
				} else if (bot.isHidden()) {
					botUser.setAccess(User.UserAccess.Friends);
				} else {
					botUser.setAccess(User.UserAccess.Everyone);
				}
				botUser.setBio(bot.getDescription());
				botUser.setCreationDate(bot.getCreationDate());
				// Should set tags.
				//botUser.setTags(bot.getTags());
				AdminDatabase.instance().createUser(botUser);
				if (bot.getAvatar() != null) {
					botUser = AdminDatabase.instance().updateUser(botUser.getUserId(), bot.getAvatar().getImage());
				} else if (bot.getInstanceAvatar() != null && bot.getInstanceAvatar().getAvatar() != null) {
					botUser = AdminDatabase.instance().updateUser(botUser.getUserId(), bot.getInstanceAvatar().getAvatar().getImage());
				}
			}
			return botUser;
		}
		return null;
	}
	
	public UserMessage createUserMessage(String target, String subject, String message, String ip) {
		UserMessage newUserMessage = null;
		try {
			checkLogin();
			User targetUser = AdminDatabase.instance().getUser(target);
			if ((targetUser != null && targetUser.isPrivate()) && (!loginBean.isAdmin() && !targetUser.getUserId().equals(loginBean.getUser().getUserId()))) {
				throw new BotException("Cannot message private users - " + target);
			}
			
			if (Site.VERIFY_EMAIL && !this.user.isVerified() && this.user.isBasic()) {
				throw new BotException("To prevent spam, you must verify your email address before sending a message.");
			}
			if (target != null) {
				target = target.trim();
			}
			if (subject != null) {
				subject = subject.trim();
			}
			Stats.stats.userMessages++;
			IPStats stat = null;
			if (ip != null && !ip.isEmpty()) {
				stat = IPStats.getStats(ip);
				stat.checkMaxUserMessages();
				stat.userMessages++;
			}
			
			User user = checkBotUser(target);
			String reply = "";
			if (user != null) {
				target = "@" + botBean.getInstance().getAlias();
				ChatBean chatBean = this.loginBean.getBean(ChatBean.class);
				boolean speak = chatBean.getSpeak();
				chatBean.setSpeak(false);
				chatBean.processInput(message, false, false, false);
				chatBean.setSpeak(speak);
				reply = chatBean.getResponse();
			}
			
			newUserMessage = new UserMessage();
			newUserMessage.setSubject(subject);
			newUserMessage.setMessage(message);
			this.userMessage = newUserMessage;
			user = AdminDatabase.instance().validateUser(target);
			newUserMessage.setTarget(user);
			newUserMessage.setCreator(getUser());
			newUserMessage.setOwner(getUser());
			newUserMessage = AdminDatabase.instance().createUserMessage(newUserMessage);

			if (!user.equals(getUser())) {
				UserMessage targetMessage = new UserMessage();
				targetMessage.setSubject(subject);
				targetMessage.setMessage(message);
				targetMessage.setOwner(user);
				targetMessage.setCreator(getUser());
				targetMessage.setTarget(user);
				targetMessage = AdminDatabase.instance().createUserMessage(targetMessage);
			}
			if (user.isBot()) {
				// Send reply.
				UserMessage replyMessage = new UserMessage();
				if (!subject.startsWith("RE:")) {
					replyMessage.setSubject("RE: " + subject);
				} else {
					replyMessage.setSubject(subject);
				}
				replyMessage.setMessage(reply);
				replyMessage.setOwner(getUser());
				replyMessage.setCreator(user);
				replyMessage.setTarget(getUser());
				replyMessage.setParent(newUserMessage);
				replyMessage = AdminDatabase.instance().createUserMessage(replyMessage);
			}
			
			this.userMessage = null;
			if (user.hasEmail() && user.isVerified() && user.getEmailMessages() && !user.isBot()) {
				StringWriter writer = new StringWriter();
				writer.write("You have received a message on ");
				writer.write(Site.NAME);
				writer.write(" from ");
				writer.write(getUser().getUserId());
				writer.write(".\n<p>");
				writer.write("To view this message on ");
				writer.write(Site.NAME);
				writer.write(" click <a href=\"" + Site.SECUREURLLINK + "/login?browse-user-messages=true\">here</a>.\n<p>");
				writer.write("\n<br/>Message follows:\n\n<br/><p>");
				writer.write("<b>" + subject + "</b>\n<p>");
				writer.write(newUserMessage.getMessageText());
				writer.write("\n<p>");
				writer.write("\n<br/><hr>");
				writer.write("\n<p>This is an automated email from " + Site.NAME + " - <a href=\"" + Site.SECUREURLLINK + "\">" + Site.SECUREURLLINK + "</a>.");
				writer.write("\n<p>You can update your email preferences from your <a href=\"" + Site.SECUREURLLINK + "/login?sign-in=true\">profile page</a> (click edit).");
				writer.write("\n<p><a href=\"" + Site.SECUREURLLINK + "/login?unsubscribe=messages&user="
							+ user.getUserId() + "&token=" + user.getVerifyToken() + "\">Unsubscribe from messages notices</a>.");
				writer.write("\n<p><a href=\"" + Site.SECUREURLLINK + "/login?unsubscribe=all&user="
						+ user.getUserId() + "&token=" + user.getVerifyToken() + "\">Unsubscribe from all notices</a>.");
				EmailService.instance().sendEmail(user.getEmail(), "You have received a message on " + Site.NAME + " - " + subject, null, writer.toString());
			}
		} catch (Exception failed) {
			error(failed);
			return null;
		}
		return newUserMessage;
	}

	public UserMessage createUserMessageReply(String reply) {
		UserMessage message = null;
		try {
			checkLogin();
			message = new UserMessage();
			String subject = this.userMessage.getSubject();
			if (!subject.startsWith("RE:")) {
				subject = "RE: " + subject;
			}
			User target = this.userMessage.getCreator();
			message.setSubject(subject);
			message.setParent(this.userMessage);
			message.setMessage(reply);
			message.setOwner(getUser());
			message.setCreator(getUser());
			message.setTarget(target);
			message = AdminDatabase.instance().createUserMessage(message);
			// Need a message for both sender and receiver.
			if (!this.userMessage.getCreator().equals(getUser()) && !target.isBot()) {
				UserMessage targetMessage = new UserMessage();
				targetMessage.setSubject(subject);
				targetMessage.setParent(this.userMessage);
				// TODO parent
				targetMessage.setMessage(reply);
				targetMessage.setOwner(target);
				targetMessage.setCreator(getUser());
				targetMessage.setTarget(target);
				targetMessage = AdminDatabase.instance().createUserMessage(targetMessage);
			}

			if (target.isBot()) {
				BotBean botBean = this.loginBean.getBotBean();
				String botId = target.getUserId().substring(1, target.getUserId().length());
				if (!botBean.validateInstance(botId)) {
					throw new BotException("Bot - " + botId + " does not exist");
				}
				ChatBean chatBean = this.loginBean.getBean(ChatBean.class);
				boolean speak = chatBean.getSpeak();
				chatBean.setSpeak(false);
				chatBean.processInput(reply, false, false, false);
				chatBean.setSpeak(speak);
				String botReply = chatBean.getResponse();

				// Send reply.
				UserMessage replyMessage = new UserMessage();
				replyMessage.setSubject(subject);
				replyMessage.setMessage(botReply);
				replyMessage.setOwner(getUser());
				replyMessage.setCreator(target);
				replyMessage.setTarget(getUser());
				replyMessage.setParent(message);
				replyMessage = AdminDatabase.instance().createUserMessage(replyMessage);
			}
			
			this.userMessage = null;
			if (target.hasEmail() && target.isVerified() && target.getEmailMessages() && !target.isBot()) {
				StringWriter writer = new StringWriter();
				writer.write("You have received a reply on ");
				writer.write(Site.NAME);
				writer.write(" from ");
				writer.write(getUser().getUserId());
				writer.write(".\n<p>");
				writer.write("To view this message on ");
				writer.write(Site.NAME);
				writer.write(" click <a href=\"" + Site.SECUREURLLINK + "/login?browse-user-messages=true\">here</a>.\n<p>");
				writer.write("\n<br/>Message follows:\n\n<br/><p>");
				writer.write("<b>" + subject + "</b>\n<p>");
				writer.write(message.getMessageText());
				writer.write("\n<p>");
				writer.write("\n<br/><hr>");
				writer.write("\n<p>This is an automated email from " + Site.NAME + " - <a href=\"" + Site.SECUREURLLINK + "\">" + Site.SECUREURLLINK + "</a>.");
				writer.write("\n<p>You can update your email preferences from your <a href=\"" + Site.SECUREURLLINK + "/login?sign-in=true\">profile page</a> (click edit).");
				writer.write("\n<p><a href=\"" + Site.SECUREURLLINK + "/login?unsubscribe=messages&user="
						+ user.getUserId() + "&token=" + user.getVerifyToken() + "\">Unsubscribe from messages notices</a>.");
				writer.write("\n<p><a href=\"" + Site.SECUREURLLINK + "/login?unsubscribe=all&user="
						+ user.getUserId() + "&token=" + user.getVerifyToken() + "\">Unsubscribe from all notices</a>.");
				EmailService.instance().sendEmail(target.getEmail(), "You have received a reply on " + Site.NAME + " - " + subject, null, writer.toString());
			}
		} catch (Exception failed) {
			error(failed);
			return null;
		}
		return message;
	}

	public boolean createCategory(String name, String description, String parents, boolean isSecure) {
		try {
			checkLogin();
			getDomain().checkCreation(this.user);
			if (name != null) {
				name = name.trim();
			}
			if (description != null) {
				description = description.trim();
			}
			if (parents != null) {
				parents = parents.trim();
			}
			Category category = new Category();
			category.setName(name);
			category.setDescription(description);
			category.setSecured(isSecure);
			category.setType(getCategoryType());
			if (!getCategoryType().equals("Domain")) {
				category.setDomain(getDomain());
			}
			else if (getCategoryType().equals("Domain")) {
				category.setDomain(AdminDatabase.instance().getDefaultDomain());
			}
			setCategory(category);
			if ((name == null) || name.equals("")) {
				throw new BotException("Invalid category");
			}
			if (!isAdmin() && ((parents == null) || parents.isEmpty())) {
				throw new BotException("Only administrators can create a root category");
			}
			category = AdminDatabase.instance().createCategory(category, getUser(), parents);
			setCategory(category);
			getActiveBean().setCategory(category);
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	public List<String> getLanguages() {
		List<String> languages = new ArrayList<String>(TranslationService.instance().getLanguages());
		Collections.sort(languages);
		return languages;
	}

	public boolean addTranslation(String textOrId, String sourceLanguage, String targetLanguage, String translation) {
		try {
			checkLogin();
			checkSuper();
			Translation instance = new Translation();
			instance.text = textOrId;
			instance.sourceLanguage = sourceLanguage;
			instance.targetLanguage = targetLanguage;
			instance.translation = translation;
			AdminDatabase.instance().updateTranslation(instance);
			TranslationService.instance().clear(instance);
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public List<String> lookupVertices(HttpServletRequest request, String prefix) {
		List<String> inputs = new ArrayList<String>();
		for (Object parameter : request.getParameterMap().entrySet()) {
			Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>)parameter;
			String key = entry.getKey();
			if (key.startsWith(prefix)) {
				inputs.add(key.substring(prefix.length(), key.length()));
			}
		}
		return inputs;
	}

	public boolean removeTranslations(HttpServletRequest request) {
		try {
			checkLogin();
			checkSuper();
			Set<Integer> ids = new HashSet<Integer>();
			for (String id : lookupVertices(request, "id-")) {
				ids.add(Integer.valueOf(id));
			}
			for (Translation transaction : AdminDatabase.instance().getAllTranslations()) {
				if (ids.contains(System.identityHashCode(transaction))) {
					AdminDatabase.instance().deleteTranslation(transaction);
					TranslationService.instance().clear(transaction);
				}
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean exportTranslations(HttpServletResponse response) {
		try {
			checkLogin();
			checkSuper();
			response.setContentType("text/plain");
			response.setHeader("Content-disposition","attachment; filename=translations.xml");
			PrintWriter writer = response.getWriter();
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
			writer.write("<translations>\r\n");
			for (Translation transaction : AdminDatabase.instance().getAllTranslations()) {
				writer.write(transaction.toXML());
				writer.write("\r\n");
			}
			writer.write("</translations>\r\n");
		} catch (Exception exception) {
			error(exception);
			return false;
		}
		return true;
	}
	
	public List<Element> getLocalElementsByTagName(String tag, Element element) {
		NodeList children = element.getChildNodes();
		List<Element> local = new ArrayList<Element>();
		for (int index = 0; index < children.getLength(); index++) {
			Node child = children.item(index);
			if (child.getNodeName().equals(tag)) {
				local.add((Element)child);
			}
		}
		return local;
	}
	
	public boolean importTranslations(InputStream stream) {
		try {
			checkLogin();
			checkSuper();
			String text = Utils.loadTextFile(stream, "", Site.MAX_UPLOAD_SIZE);
			if (text == null) {
				throw new BotException("Invalid file");
			}
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			StringReader reader = new StringReader(text);
			InputSource source = new InputSource(reader);
			Document document = parser.parse(source);
			Element root = document.getDocumentElement();
			List<Element> translations = getLocalElementsByTagName("translation", root);
			for (Element element : translations) {
				Translation translation = new Translation();
				translation.parseXML(element);
				AdminDatabase.instance().updateTranslation(translation);
				TranslationService.instance().clear(translation);
			}			
		} catch (Exception failed) {
			error(new BotException(failed.toString()));
			return false;
		}
		return true;
	}
	
	/**
	 * Update a category.
	 */
	public boolean updateCategory(String name, String description, String parents, boolean isSecure) {
		try {
			checkLogin();
			checkCategoryAdmin();
			Category category = getCategory().clone();
			if (name != null) name = name.trim();
			category.setName(name);
			category.setDescription(description.trim());
			category.setSecured(isSecure);
			setCategory(AdminDatabase.instance().updateCategory(category, parents));
			if (category.getType().equals("Bot")) {
				getBotBean().setCategory(getCategory());
			} else if (category.getType().equals("Forum")) {
				getBean(ForumBean.class).setCategory(getCategory());
			} else if (category.getType().equals("IssueTracker")) {
				getBean(IssueTrackerBean.class).setCategory(getCategory());
			} else if (category.getType().equals("Channel")) {
				getBean(LiveChatBean.class).setCategory(getCategory());
			} else if (category.getType().equals("Script")) {
				getBean(ScriptBean.class).setCategory(getCategory());
			} else if (category.getType().equals("Avatar")) {
				getBean(AvatarBean.class).setCategory(getCategory());
			} else if (category.getType().equals("Graphic")) {
				getBean(GraphicBean.class).setCategory(getCategory());
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Delete a category.
	 */
	public boolean deleteCategory() {
		try {
			checkLogin();
			checkCategoryAdmin();
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		try {
			Category category = getCategory();
			AdminDatabase.instance().deleteCategory(category.getId());
			setCategory(null);
			if (category.getType().equals("Bot")) {
				getBotBean().setCategory(null);
			} else if (category.getType().equals("Forum")) {
				getBean(ForumBean.class).setCategory(null);
			} else if (category.getType().equals("IssueTracker")) {
				getBean(IssueTrackerBean.class).setCategory(null);
			} else if (category.getType().equals("Channel")) {
				getBean(LiveChatBean.class).setCategory(null);
			} else if (category.getType().equals("Script")) {
				getBean(ScriptBean.class).setCategory(null);
			} else if (category.getType().equals("Avatar")) {
				getBean(AvatarBean.class).setCategory(null);
			} else if (category.getType().equals("Graphic")) {
				getBean(GraphicBean.class).setCategory(null);
			} else if (category.getType().equals("Domain")) {
				getBean(DomainBean.class).setCategory(null);
			}
		} catch (Exception failed) {
			error(new BotException("Category deletion failed, ensure category is empty first"));
			return false;
		}
		return true;
	}
	
	/**
	 * Update a user.
	 */
	public boolean updateUser(String oldPassword, String newPassword, String newPassword2, String hint, String name, String userAccess, String tags,
			String email, String source, Boolean emailNotices, Boolean emailMessages, Boolean emailSummary, String website, String bio, Boolean displayName, Boolean over18,
			String adCode, Boolean verifiedPayment, String type) {
		try {
			checkLogin();
			if (getEditUser() == null) {
				setEditUser(getUser());
			}
			User user = getEditUser().clone();
			setEditUser(user);
			if (oldPassword != null) {
				oldPassword = oldPassword.trim();
			}
			if (newPassword != null) {
				newPassword = newPassword.trim();
				user.setPassword(newPassword);
				user.resetToken();
			}
			if (newPassword2 != null) {
				newPassword2 = newPassword2.trim();
			}
			if (hint != null) {
				hint = hint.trim();
				user.setHint(hint);
			}
			if (name != null) {
				name = name.trim();
				user.setName(name);
			}
			if (userAccess != null) {
				userAccess = userAccess.trim();
				user.setAccess(UserAccess.valueOf(userAccess));
			}
			boolean newEmail = false;
			if (email != null) {
				email = email.trim().toLowerCase();
				newEmail = !user.getEmail().equalsIgnoreCase(email);
				user.setEmail(email);
				if (newEmail) {
					user.setVerified(false);
				}
			}
			if (source != null) {
				source = source.trim();
				user.setSource(source);
			}
			if (website != null) {
				website = website.trim();
				user.setWebsite(website);
			}
			if (displayName != null) {
				user.setShoulsDisplayName(displayName);
			}
			if (emailNotices != null) {
				user.setEmailNotices(emailNotices);
			}
			if (emailMessages != null) {
				user.setEmailMessages(emailMessages);
			}
			if (emailSummary != null) {
				user.setEmailSummary(emailSummary);
			}
			if (bio != null) {
				user.setBio(bio);
			}
			if (over18 != null) {
				user.setOver18(over18);
			}
			if (!Site.COMMERCIAL && (adCode != null && !adCode.isEmpty()) && getUser().getType() == UserType.Basic) {
				throw new BotException("Only Bronze and Gold accounts can disable ads, or set ad code.");
			}
			if (adCode != null) {
				user.setAdCode(adCode);
			}
			if (isSuper() && verifiedPayment != null) {
				user.setVerifiedPayment(verifiedPayment);
				if (!user.getType().equals(UserType.valueOf(type))) {
					user.setUpgradeDate(new Date());
				}
				user.setType(UserType.valueOf(type));
			}
			if (newPassword != null && !newPassword.isEmpty() && !newPassword.equals(newPassword2)) {
				throw new BotException("New passwords do not match");
			}
			boolean reset = isSuper() && !user.isSuperUser() && !user.isAdminUser();
			setEditUser(AdminDatabase.instance().updateUser(user, oldPassword, reset, tags));
			if (user.equals(getUser())) {
				setUser(getEditUser());
			}
			if (newEmail) {
				sendEmailUpdateVerify();
			}
			setViewUser(getEditUser());
			setEditUser(null);
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Update a user's ip address.
	 */
	public boolean updateIP(String ip) {
		try {
			checkLogin();
			if (getUser().getIP() == null || !getUser().getIP().equals(ip)) {
				setUser(AdminDatabase.instance().updateUserIP(getUserId(), ip));
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Update a user's avatar.
	 */
	public boolean updateUser(byte[] image) {
		try {
			checkLogin();
			if (isSuper()) {
				setViewUser(AdminDatabase.instance().updateUser(getViewUser().getUserId(), image));
			} else {
				setViewUser(AdminDatabase.instance().updateUser(getUser().getUserId(), image));
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean verifyUser(String userId, String token) {
		try {
			if (userId != null) {
				userId = userId.trim();
			}
			if (token != null) {
				token = token.trim();
			}
			setViewUser(AdminDatabase.instance().verifyUser(userId, token));
			if (getUserId().equals(userId)) {
				getUser().setVerified(true);
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean unsubscribe(String unsubscribe, String user, String token) {
		try {
			if (user != null) {
				user = user.trim();
			}
			if (token != null) {
				token = token.trim();
			}
			setViewUser(AdminDatabase.instance().unsubscribe(unsubscribe, user, token));
			if (getUserId().equals(user)) {
				setUser(getViewUser());
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean verifyResetPassword(String userId, String token) {
		try {
			setUser(AdminDatabase.instance().resetPassword(userId, token));
			this.isLoggedIn = true;
			this.passwordReset = true;
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean resetPasswordComplete(String password, String password2) {
		try {
			setUser(AdminDatabase.instance().resetPasswordComplete(getUser().getUserId(), password, password2));
			this.passwordReset = false;
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	public void checkCategoryAdmin() {
		if (!isAdmin() && getCategory() != null && !getCategory().getCreator().equals(getUser())) {
			throw new BotException("Must be category admin");
		}
	}
	
	/**
	 * Update a category's avatar.
	 */
	public boolean updateCategory(byte[] image) {
		try {
			checkLogin();
			checkCategoryAdmin();
			setCategory(AdminDatabase.instance().updateCategory(getCategory().getId(), image));
			if (this.category.getType().equals("Bot")) {
				getBotBean().setCategory(getCategory());
			} else if (this.category.getType().equals("Forum")) {
				getBean(ForumBean.class).setCategory(getCategory());
			} else if (this.category.getType().equals("IssueTracker")) {
				getBean(IssueTrackerBean.class).setCategory(getCategory());
			} else if (this.category.getType().equals("Channel")) {
				getBean(LiveChatBean.class).setCategory(getCategory());
			} else if (this.category.getType().equals("Script")) {
				getBean(ScriptBean.class).setCategory(getCategory());
			} else if (this.category.getType().equals("Avatar")) {
				getBean(AvatarBean.class).setCategory(getCategory());
			} else if (this.category.getType().equals("Graphic")) {
				getBean(GraphicBean.class).setCategory(getCategory());
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public void disconnect() {
		if (this.botBean != null) {
			this.botBean.disconnect();
		}
		for (ServletBean bean : this.beans.values()) {
			bean.disconnect();
		}
	}

	public void disconnectInstance() {
		if (this.botBean != null) {
			this.botBean.disconnectInstance();
		}
		for (ServletBean bean : this.beans.values()) {
			bean.disconnectInstance();
		}
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

	public String getUserId() {
		if (this.user == null) {
			return "";
		}
		return this.user.getUserId();
	}

	public User getUser() {
		return user;
	}

	public UserPayment getPayment() {
		return payment;
	}
	
	public String getAllUserTagsString() {
		List<Tag> tags = null;
		try {
			tags = AdminDatabase.instance().getTags("User", null);
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
			return "";
		}
		StringWriter writer = new StringWriter();
		int count = 1;
		for (Tag tag : tags) {
			writer.write("\"");
			writer.write(tag.getName());
			writer.write("\"");
			if (count < tags.size()) {
				writer.write(", ");
			}
			count++;
		}
		return writer.toString();
	}
	
	public void beginPayment(UserType userType, UserPaymentType type, String orderId, String token, String st, String sku) {
		try {
			checkLogin();
			this.payment = new UserPayment();
			this.payment.setStatus(UserPaymentStatus.WaitingForPayment);
			this.payment.setType(type);
			this.payment.setUserType(userType);
			this.payment.setPaymentDate(new Date(Calendar.getInstance().getTimeInMillis()));
			this.payment.setToken(String.valueOf(Math.abs(Utils.random().nextLong())));
			this.payment.setUserId(getUserId());
			this.payment.setPaypalTx(orderId);
			this.payment.setToken(token);
			this.payment.setPaypalSt(st);
			this.payment.setPaypalCc(sku);
			if (userType == UserType.Diamond) {
				this.payment.setCost("99.99");
			} else if (userType == UserType.Platinum) {
				this.payment.setCost("49.99");
			} else if (userType == UserType.Gold) {
				this.payment.setCost("4.99");
			} else if (userType == UserType.Bronze) {
				this.payment.setCost("0.99");
			} else {
				throw new BotException("Invalid upgrade type: " + userType);
			}
			this.user = AdminDatabase.instance().addPayment(this.user, this.payment);
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void confirmPayment(String tx, String amt, String st, String cc, String id) {
		try {
			this.payment = AdminDatabase.instance().findUserPayment(Long.valueOf(id));
			this.payment.setStatus(UserPaymentStatus.Complete);
			this.payment.setType(UserPaymentType.PayPal);
			this.payment.setPaypalTx(tx);
			this.payment.setPaypalAmt(amt);
			this.payment.setPaypalSt(st);
			this.payment.setPaypalCc(cc);
			this.user = AdminDatabase.instance().updatePayment(this.payment);
			this.user.setType(this.payment.getUserType());
			this.user.setUpgradeDate(new Date());
			this.user = AdminDatabase.instance().updateUser(this.user, null, true, null);
			this.payment = null;
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void confirmUpgradePayment(String type, String orderId, String token, String st, String sku, String id) {
		try {
			this.payment = AdminDatabase.instance().findUserPayment(Long.valueOf(id));
			this.payment.setStatus(UserPaymentStatus.Complete);
			this.payment.setType(UserPaymentType.valueOf(type));
			this.payment.setPaypalTx(orderId);
			this.payment.setToken(token);
			this.payment.setPaypalSt(st);
			this.payment.setPaypalCc(sku);
			this.user = AdminDatabase.instance().updatePayment(this.payment);
			this.user.setType(this.payment.getUserType());
			this.user.setUpgradeDate(new Date());
			this.user = AdminDatabase.instance().updateUser(this.user, null, true, null);
			this.payment = null;
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void upgradeUser(UpgradeConfig upgrade) {
		if (!isLoggedIn()) {
			throw new BotException("You must sign in first");
		}
		//if (getUser().getSource().equals("ios") && !upgrade.type.equals("AppleItunes")) {
		//	throw new BotException("iOS users can only upgrade from the iOS app");
		//}
		beginPayment(UserType.valueOf(upgrade.userType), UserPaymentType.valueOf(upgrade.type), upgrade.orderId, upgrade.token, upgrade.orderToken, upgrade.sku);
		if (upgrade.secret == null || !String.valueOf((Long.valueOf(upgrade.secret) - getUserId().length())).equals(Site.UPGRADE_SECRET)) {
			AdminDatabase.instance().log(Level.WARNING, "Upgrade failed: authorization", upgrade.orderId, upgrade.secret, getUserId(), Long.valueOf(upgrade.secret) - getUserId().length());
			throw new BotException("Upgrade failed authorization");
		}
		if (upgrade.type != null && upgrade.type.equals("GooglePlay") && (upgrade.orderId == null || !upgrade.orderId.startsWith("GPA"))) {
			AdminDatabase.instance().log(Level.WARNING, "Upgrade failed: suspicious transaction", upgrade.orderId);
			throw new BotException("This transaction seems suspicious, if your Google Play account is charged, please contact billing@botlibre.com");
		}
		if (upgrade.type != null && upgrade.type.equals("AppleItunes") && (upgrade.orderId == null || (upgrade.orderId.contains("-") || upgrade.orderId.contains(".")))) {
			AdminDatabase.instance().log(Level.WARNING, "Upgrade failed: suspicious transaction", upgrade.orderId);
			throw new BotException("This transaction seems suspicious, if your Apple account is charged, please contact billing@botlibre.com");
		}
		confirmUpgradePayment(upgrade.type, upgrade.orderId, upgrade.token, upgrade.orderToken, upgrade.sku, String.valueOf(this.payment.getId()));
	}
	
	public boolean checkPayment() {
		try {
			if (this.payment == null) {
				throw new BotException("Missing payment");
			}
			UserPayment payment = AdminDatabase.instance().findUserPayment(this.payment.getId());
			if (payment.getStatus() != UserPaymentStatus.Complete) {
				throw new BotException("Payment has not been processed yet");
			}
			this.user = AdminDatabase.instance().validateUser(getUserId());
			this.payment = null;
			return true;
		} catch (Exception exception) {
			error(exception);
			return false;
		}
	}
	
	/*
	 * {"order":
	 * 		{"id":null,"created_at":null,"status":"completed","event":null,
	 * 			"total_btc":
	 * 				{"cents":100000000.0,"currency_iso":"BTC"},
	 * 			"total_native":
	 * 				{"cents":27650.0,"currency_iso":"CAD"},
	 * 			"total_payout":
	 * 				{"cents":22564.0,"currency_iso":"USD"},
	 * 			"custom":"123456789",
	 * 			"receive_address":"1CnKPf6GNGwVyAYd3bhkckULc7ej1UkLSf",
	 * 			"button":
	 * 				{"type":"buy_now","subscription":false,"repeat":null,"name":"Test Item","description":null,"id":null},
	 * 			"transaction":
	 * 				{"id":"55368fcdea6cf3d428006201", "hash":"4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b","confirmations":0}
	 * 		}
	 * }
	 */
	public void confirmBCPayment(String json) {
		try {
			JSONObject object = (JSONObject)JSONSerializer.toJSON(json);
			String id = object.getJSONObject("order").getString("custom");
			this.payment = AdminDatabase.instance().findUserPayment(Long.valueOf(id));
			this.payment.setStatus(UserPaymentStatus.Complete);
			this.payment.setType(UserPaymentType.BitCoin);
			this.payment.setBitCoinJSON(json);
			this.user = AdminDatabase.instance().updatePayment(this.payment);
			this.user.setType(this.payment.getUserType());
			this.user.setUpgradeDate(new Date());
			this.user = AdminDatabase.instance().updateUser(this.user, null, true, null);
			this.payment = null;
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void setPayment(UserPayment payment) {
		this.payment = payment;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getErrorMessage() {
		if (this.error == null) {
			return "error";
		}
		String message = this.error.getMessage();
		if (message == null || message.isEmpty()) {
			return "error";
		}
		if (message.indexOf('<') != -1) {
			message = message.replace("<", "&lt;");
		}
		if (message.indexOf('>') != -1) {
			message = message.replace(">", "&gt;");
		}
		return message;
	}

	public Throwable getError() {
		return error;
	}

	public void setError(Throwable error) {
		this.error = error;
	}

	public void error(Throwable error) {
		AdminDatabase.instance().log(error);
		// Ensure the first error.
		if (this.error == null) {
			this.error = error;
		}
	}
	
	public static void setOutputFilePath(String path) {
		outputFilePath = path;
	}

	public String activePage(Page pageType) {
		if (this.pageType == pageType) {
			return "class=\"active\"";
		}
		return "";
	}

	public String activePage2(Page pageType) {
		if (this.pageType == pageType) {
			return "id=\"navigation3\"";
		}
		return "id=\"navigation2\"";
	}
	
	/**
	 * Check if the URL is using an old domain name and should be redirected.
	 * Also check for an http/https Chrome bug.
	 */
	public boolean checkRedirect(HttpServletRequest request, HttpServletResponse response) {
		String server = request.getServerName().toLowerCase();
		// Redirect old domain.
		if (!Site.REDIRECT.isEmpty() && server.endsWith(Site.REDIRECT)) {
			response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
			error(new BotException("Invalid URL - " + request.getRequestURL()));
			try {
				AdminDatabase.instance().log(Level.INFO, "Redirecting URL", request.getRequestURL());
				String url = request.getRequestURL().toString().replace(Site.REDIRECT, Site.SERVER_NAME);
				if (request.getQueryString() != null) {
					url = url + "?" + request.getQueryString();
				}
				response.sendRedirect(url);
				return true;
			} catch (Exception exception) {
				setError(exception);
			}
			return true;
		}
		// Check if cookies are enabled for http and redirect to https.
		if (!request.isSecure() && Site.HTTPS) {
			if ((server.equals(Site.SERVER_NAME) || server.equals(Site.URL))
						&& (request.getQueryString() == null || !request.getQueryString().contains("file"))) {
				boolean found = false;
				if (request.getCookies() != null) {
					for (Cookie cookie : request.getCookies()) {
						if (cookie.getName().equalsIgnoreCase("JSESSIONID")) {
							found = true;
						}
					}
				}
				if (!found) {
					try {
						AdminDatabase.instance().log(Level.INFO, "Redirecting to https", request.getRequestURL());
						String url = request.getRequestURL().toString().replace("http://", "https://");
						if (request.getQueryString() != null) {
							url = url + "?" + request.getQueryString();
						}
						response.sendRedirect(url);
						return true;
					} catch (Exception exception) {
						setError(exception);
					}
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean checkEmbed(HttpServletRequest request, HttpServletResponse response) {
		if (checkRedirect(request, response)) {
			return true;
		}
		if (isEmbedded()) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			error(new BotException("Invalid URL - " + request.getRequestURL()));
			try {
				request.getRequestDispatcher("404.jsp").forward(request, response);
				return true;
			} catch (Exception exception) {
				setError(exception);
			}
			return true;
		}
		String server = request.getServerName().toLowerCase();
		if (server.startsWith("www.") || server.startsWith(Site.SANDBOX + ".") ||
					server.startsWith("email.") || server.startsWith("twitter.") || server.startsWith("facebook.")) {
			server = server.substring(server.indexOf('.') + 1, server.length());
		}
		if (Site.LOCK && !server.equals(Site.SERVER_NAME) && !server.equals(Site.SERVER_NAME2) && !server.equals(Site.URL)) {
			String subdomain = server;
			if (server.indexOf(Site.SERVER_NAME) != -1 || server.indexOf(Site.SERVER_NAME2) != -1) {
				subdomain = server.substring(0, server.indexOf('.'));
			}
			if (subdomain.length() == 2) {
				if (TranslationService.instance().checkLanguage(subdomain)) {
					this.language = subdomain;
					return false;
				}
			}
			if (Site.SERVER_NAME.equals("localhost")) {
				return false;
			}
			if (getDomainEmbedded()) {
				return false;
			}
			try {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return true;
			} catch (Exception exception) {
				setError(exception);
			}
		}
		return false;
	}
	
	public boolean checkForward(HttpServletRequest request, HttpServletResponse response) {
		if (checkRedirect(request, response)) {
			return true;
		}
		String server = request.getServerName().toLowerCase();
		if (server.startsWith("www.") || server.startsWith(Site.SANDBOX + ".")
					|| server.startsWith("email.") || server.startsWith("twitter.") || server.startsWith("facebook.")) {
			server = server.substring(server.indexOf('.') + 1, server.length());
		}
		if (Site.LOCK && !server.equals(Site.SERVER_NAME) && !server.equals(Site.SERVER_NAME2) && !server.equals(Site.URL)) {
			String subdomain = server;
			if (server.indexOf(Site.SERVER_NAME) != -1 || server.indexOf(Site.SERVER_NAME) != -1) {
				subdomain = server.substring(0, server.indexOf('.'));
			}
			if (subdomain.length() == 2) {
				if (TranslationService.instance().checkLanguage(subdomain)) {
					this.language = subdomain;
					return false;
				}
			}
			if (getDomainEmbedded()) {
				return false;
			}
			try {
				DomainForwarder forwarder = AdminDatabase.instance().findDomainForwarder(subdomain);
				if (forwarder == null) {
					if (Site.SERVER_NAME.equals("localhost")) {
						return false;
					}
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				} else {
					setSandbox(true);
					AdminDatabase.instance().log(Level.INFO, "Forwarding URL", request.getRequestURL(), forwarder.getForwarderAddress());
					if (forwarder.getForwarderAddress().contains("domain?")) {
						setDomainEmbedded(true);
					} else {
						setEmbedded(true);
					}
					setProperties(parseProperties(forwarder.getForwarderAddress()));
					request.getRequestDispatcher(forwarder.getForwarderAddress()).forward(request, response);
				}
				return true;
			} catch (Exception exception) {
				setError(exception);
			}
		}
		return false;
	}
	
	public boolean checkDomain(HttpServletRequest request, HttpServletResponse response) {
		if (isEmbedded()) {
			return true;
		}
		if (checkRedirect(request, response)) {
			return false;
		}
		String server = request.getServerName().toLowerCase();
		if (server.startsWith("www.") || server.startsWith(Site.SANDBOX + ".")
					|| server.startsWith("email.") || server.startsWith("twitter.") || server.startsWith("facebook.")) {
			server = server.substring(server.indexOf('.') + 1, server.length());
		}
		if (Site.LOCK && !server.equals(Site.SERVER_NAME) && !server.equals(Site.SERVER_NAME2) && !server.equals(Site.URL)) {
			String subdomain = server;
			if (server.indexOf(Site.SERVER_NAME) != -1 || server.indexOf(Site.SERVER_NAME2) != -1) {
				subdomain = server.substring(0, server.indexOf('.'));
			}
			if (subdomain.length() == 2) {
				if (TranslationService.instance().checkLanguage(subdomain)) {
					this.language = subdomain;
					return true;
				}
			}
			if (getDomainEmbedded()) {
				return true;
			}
			DomainForwarder forwarder = AdminDatabase.instance().findDomainForwarder(subdomain);
			if (forwarder == null) {
				if (Site.SERVER_NAME.equals("localhost")) {
					return true;
				}
				try {
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
					return false;
				} catch (Exception exception) { }
			}
			setSandbox(true);
			setEmbedded(true);
			setProperties(parseProperties(forwarder.getForwarderAddress()));
		}
		return true;
	}
	
	public Map<String, String> parseProperties(String url) {
		Map<String, String> properties = new HashMap<String, String>();
		TextStream stream = new TextStream(url);
		stream.skipTo('?', true);
		while (!stream.atEnd()) {
			String property = stream.upTo('&');
			if (!property.isEmpty()) {
				int index = property.indexOf('=');
				String key, value;
				if (index == -1) {
					key = ServletBean.decode(property);
					value = ServletBean.decode(property);
				} else {
					key = ServletBean.decode(property.substring(0, index));
					value = ServletBean.decode(property.substring(index + 1, property.length()));
				}
				properties.put(key, value);
			}
			stream.skip();
		}
		return properties;
	}
	
	public void embedHTML(String url, Writer out) {
		if (url == null || url.isEmpty() || !isSandbox()) {
			return;
		}
		try {
			if (url.startsWith("http")) {
				url = url.replace("&amp;", "&");
				BufferedReader in = new BufferedReader(new InputStreamReader(BeanServlet.safeURL(url).openStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					out.write(inputLine);
				}
				in.close();
			} else {
				out.write(url);
			}
			out.write("\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void embedCSS(String url, Writer out) {
		if (url == null || url.isEmpty()) {
			return;
		}
		try {
			if (url.startsWith("http")) {
				out.write("<link rel='stylesheet' href='");
				out.write(url);
				out.write("' type='text/css'>");
			} else {
				out.write("<style>");
				out.write(url);
				out.write("</style>");
			}
			out.write("\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void setProperties(Map<String, String> properties) {
		String css = properties.get("css");
		String banner = properties.get("banner");
		String footer = properties.get("footer");
		String background = properties.get("background");
		String showAds = properties.get("showAds");
		String application = properties.get("application");
		String token = properties.get("token");
		String user = properties.get("user");
		String password = properties.get("password");
		String facebookLogin = properties.get("facebookLogin");
		String showLink = properties.get("showLink");
		String loginBanner = properties.get("loginBanner");
		setCssURL(css == null ? "" : css);
		setBannerURL(banner == null ? "" : banner);
		setFooterURL(footer == null ? "" : footer);
		setFacebookLogin(facebookLogin == null || !facebookLogin.equals("false"));
		setShowLink(showLink == null || !showLink.equals("false"));
		setLoginBanner(loginBanner == null || !loginBanner.equals("false"));
		if (background != null) {
			if (((background.length() == 3) || (background.length() == 6)) && ("1234567890aAbBcCdDeEfF".indexOf(background.charAt(0)) != -1)) {
				setBackgroundColor("#" + background);
			} else {
				setBackgroundColor(background);
			}
		} else {
			setBackgroundColor("#fff");
		}
		setShowAds(showAds != null && !showAds.equals("false"));
		long tokenValue = 0;
		if (token != null) {
			try {
				tokenValue = Long.valueOf(token);
			} catch (Exception ignore) {
				setError(ignore);
			}
		}
		if (user != null) {
			validateUser(user, password, tokenValue, false, false);
		}
		if (application != null) {
			setApplicationId(application);
			String appUser = AdminDatabase.instance().validateApplicationId(application, null);
			setAppUser(appUser);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ServletBean> T getBean(Class<T> type) {
		T bean = (T)this.beans.get(type);
		if (bean == null) {
			try {
				bean = type.newInstance();
			} catch (Exception exception) {
				throw new Error(exception);
			}
			bean.setLoginBean(this);
			this.beans.put(type, bean);
			if ((this.botBean != null) && this.botBean.isConnected()) {
				bean.initialize(this.botBean.getBot());
			}
		}
		return bean;
	}

	public Map<Class, ServletBean> getBeans() {
		return beans;
	}

	public boolean getShowLanguage() {
		return showLanguage;
	}

	public void setShowLanguage(boolean showLanguage) {
		this.showLanguage = showLanguage;
	}

	public User getViewUser() {
		if (viewUser == null) {
			return getUser();
		}
		return viewUser;
	}

	public void setViewUser(User viewUser) {
		this.viewUser = viewUser;
	}

	public boolean isEmbedded() {
		return isEmbedded;
	}

	public void setEmbedded(boolean isEmbedded) {
		this.isEmbedded = isEmbedded;
	}
	
	public boolean isEmbeddedDebug() {
		return isEmbeddedDebug;
	}

	public void setEmbeddedDebug(boolean isEmbeddedDebug) {
		this.isEmbeddedDebug = isEmbeddedDebug;
	}

	public void checkEmbeddedAPI() {
		if (isEmbedded()) {
			String appUser = getAppUser();
			if (appUser == null) {
				appUser = AdminDatabase.instance().validateApplicationId(getApplicationId(), null);
				setAppUser(appUser);
			}
			AppIDStats stat = AppIDStats.getStats(getApplicationId(), appUser);
			AppIDStats.checkMaxAPI(stat, appUser, null, null);
			stat.apiCalls++;
		}
	}

	public BrowseBean getActiveBean() {
		if (this.activeBean == null) {
			return Site.defaultBean();
		}
		return activeBean;
	}

	public String getActiveCategory() {
		if (this.activeBean == null) {
			return "";
		}
		Category category = this.activeBean.getCategory();
		if (category == null) {
			return "";
		}
		return category.getName();
	}

	public void setActiveBean(BrowseBean activeBean) {
		this.activeBean = activeBean;
	}

	public void migrate() {
		//throw new BotException("Disabled");
		AdminDatabase.instance().shutdown();
		new Migrate().migrate7();
	}

	public void dropDead() {
		//throw new BotException("Disabled");
		//AdminDatabase.instance().shutdown();
		new Migrate().dropDead();
	}

	public void initDatabase() {
		//throw new BotException("Disabled");
		new Migrate().initDatabase();
	}

	public void archiveInactive() {
		//throw new BotException("Disabled");
		//AdminDatabase.instance().shutdown();
		new Migrate().archiveInactive();
	}

	public void cleanupJunk() {
		//AdminDatabase.instance().shutdown();
		AdminDatabase.instance().cleanupJunk();
	}

	public void verifyAllUnverifiedEmail() {
		List<User> users = AdminDatabase.instance().getUnverifiedUsers();
		for (User user: users) {
			if (user.hasEmail() && !user.isVerified()) {
				sendEmailVerify(user);
				Utils.sleep(100);
			}
		}
	}
	
	public String isUserAccessModeSelected(String type) {
		UserAccess mode = UserAccess.Friends;
		if (getEditUser() != null && getEditUser().getAccess() != null) {
				mode = getEditUser().getAccess();
			}
			if (mode.name().equals(type)) {
				return "selected=\"selected\"";
			} else {
				return "";
			}
		}
	
	public boolean isTranslationRequired() {
		if (this.language == null || this.language.isEmpty() || this.language.equals("en")) {
			return false;
		}
		return true;
	}
	
	public String translate(String textOrId) {
		if (this.language == null || this.language.isEmpty() || this.language.equals("en")) {
			return textOrId;
		}
		return TranslationService.instance().translate(textOrId, "en", this.language);
	}

	public void resetTags() {
		//AdminDatabase.instance().resetTagInstanceCount(getBean(BotBean.class).getDomain());
	}
	
}

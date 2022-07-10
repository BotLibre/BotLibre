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
package org.botlibre.web.servlet;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.User;
import org.botlibre.web.admin.UserPayment;
import org.botlibre.web.admin.User.UserType;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.SessionProxyBean;
import org.botlibre.web.service.PageStats;
import org.botlibre.web.service.Stats;
import org.botlibre.web.service.TranslationService;

@javax.servlet.annotation.WebServlet("/login")
@SuppressWarnings("serial")
public class LoginServlet extends BeanServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// TODO: Temp fix to prevent crawler dos attack.
		if (request.getParameter("proxy") != null) {
			PageStats.page(request);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		LoginBean bean = getLoginBean(request, response);
		if (bean == null) {
			bean = new LoginBean();
			request.getSession().setAttribute("loginBean", bean);
		}
		SessionProxyBean proxy = checkProxy(request, response);

		try {
			String postToken = (String)request.getParameter("postToken");
			if (!bean.checkDomain(request, response)) {
				return;
			}
			
			String help = (String)request.getParameter("help");
			String language = (String)request.getParameter("language");
			String lang = (String)request.getParameter("lang");
			String confirmAdult = (String)request.getParameter("confirm-adult");
			String mobile = (String)request.getParameter("mobile");
			String fullscreen = (String)request.getParameter("fullscreen");
			String showBanner = (String)request.getParameter("showBanner");
			String verifyAnonymous = (String)request.getParameter("verify-anonymous");
			String referer = request.getHeader("referer");
			
			String affiliate = (String)request.getParameter("affiliate");
			if (affiliate != null) {
				bean.setAffiliate(Utils.sanitize(affiliate));
			}

			try {
				if (help != null) {
					bean.setHelp(!bean.getHelp());
					if (referer == null) {
						return;
					}
					proxy.setRedirect(true);
					response.sendRedirect(referer);
					return;
				} else if (lang != null) {
					if (!TranslationService.instance().checkLanguage(lang)) {
						bean.setError(new BotException("Missing translation for language - " + lang));
					} else {
						bean.setLanguage(lang);
					}
					if (referer == null) {
						return;
					}
					proxy.setRedirect(true);
					response.sendRedirect(referer);
					return;
				} else if (language != null) {
					bean.setShowLanguage(!bean.getShowLanguage());
					if (referer == null) {
						return;
					}
					proxy.setRedirect(true);
					response.sendRedirect(referer);
					return;
				} else if (confirmAdult != null) {
					bean.setAgeConfirmed(true);
					if (referer == null) {
						return;
					}
					proxy.setRedirect(true);
					response.sendRedirect(referer);
					return;
				} else if (mobile != null || fullscreen != null) {
					bean.setMobile("true".equals(mobile));
					bean.setFullScreen("true".equals(fullscreen));
					if (referer == null) {
						return;
					}
					proxy.setRedirect(true);
					response.sendRedirect(referer);
					return;
				} else if (showBanner != null) {
					bean.setShowBanner("true".equals(showBanner));
					if (referer == null) {
						return;
					}
					proxy.setRedirect(true);
					response.sendRedirect(referer);
					return;
				} else if (verifyAnonymous != null) {
					String dateOfBirth = (String)request.getParameter("dateOfBirth");
					String terms = (String)request.getParameter("terms");
					bean.verifyAnonymous(dateOfBirth, "on".equals(terms));
					if (referer == null) {
						return;
					}
					proxy.setRedirect(true);
					response.sendRedirect(referer);
					return;
				}
			} catch (Exception ignore) {
				AdminDatabase.instance().log(ignore);
				return;
			}
			proxy.setRedirect(false);

			String download = (String)request.getParameter("download");
			if (download != null) {
				Stats.stats.webDownloads++;
				response.sendRedirect("download/botlibre-web.zip");
				return;
			}
			String downloadLink = (String)request.getParameter("download-link");
			if (downloadLink != null) {
				response.sendRedirect("download.jsp");
				return;
			}
			String demo = (String)request.getParameter("demo");
			if (demo != null) {
				response.sendRedirect("contact.jsp");
				return;
			}
			String sales = (String)request.getParameter("sales");
			if (sales != null) {
				response.sendRedirect("contact.jsp");
				return;
			}
			String contact = (String)request.getParameter("contact");
			String email = (String)request.getParameter("email");
			String name = (String)request.getParameter("name");
			String business = (String)request.getParameter("business");
			String details = (String)request.getParameter("details");
			String demoRequest = (String)request.getParameter("demoRequest");
			String spamCheck = (String)request.getParameter("spamCheck");
			String newsletterCheck = (String)request.getParameter("newsletterCheck");

			if (contact != null) {
				if (!"ok".equals(spamCheck)) {
					throw new BotException("Your request has been rejected as spam");
				}

				bean.verifyPostToken(postToken);
				bean.contact(email, name, business, details, "on".equals(demoRequest));

				if ("on".equals(newsletterCheck)) {
					Cookie cookie = new Cookie("newsletter-popup-shown", "true");
					cookie.setMaxAge(60*60*24*365*5);
					response.addCookie(cookie);
					response.sendRedirect("/");
					return;
				}
				response.sendRedirect("contact.jsp");
				return;
			}

			String subscribeLater = (String)request.getParameter("subscribeLater");
			if (subscribeLater != null) {
				Cookie cookie = new Cookie("newsletter-popup-shown", "true");
				cookie.setMaxAge(60*60*3);
				response.addCookie(cookie);
				response.sendRedirect("/");
				return;
			}
			
			String upgradeBronze = (String)request.getParameter("upgradeBronze");
			String upgradeGold = (String)request.getParameter("upgradeGold");
			String upgradePlatinum = (String)request.getParameter("upgradePlatinum");
			String upgradeDiamond = (String)request.getParameter("upgradeDiamond");
			if (upgradeBronze != null) {
				bean.setPayment(new UserPayment());
				bean.getPayment().setUserType(UserType.Bronze);
				bean.getPayment().setPaymentDuration(1);
				bean.getPayment().updateCost();
				response.sendRedirect("upgrade-payment.jsp");
				return;
			} else if (upgradeGold != null) {
				bean.setPayment(new UserPayment());
				bean.getPayment().setUserType(UserType.Gold);
				bean.getPayment().setPaymentDuration(1);
				bean.getPayment().updateCost();
				response.sendRedirect("upgrade-payment.jsp");
				return;
			} else if (upgradePlatinum != null) {
				bean.setPayment(new UserPayment());
				bean.getPayment().setUserType(UserType.Platinum);
				bean.getPayment().setPaymentDuration(1);
				bean.getPayment().updateCost();
				response.sendRedirect("upgrade-payment.jsp");
				return;
			} else if (upgradeDiamond != null) {
				bean.setPayment(new UserPayment());
				bean.getPayment().setUserType(UserType.Diamond);
				bean.getPayment().setPaymentDuration(1);
				bean.getPayment().updateCost();
				response.sendRedirect("upgrade-payment.jsp");
				return;
			}

			String user = (String)request.getParameter("user");
			String password = (String)request.getParameter("password");
			String password2 = (String)request.getParameter("password2");
			String remember = (String)request.getParameter("remember");
			String newPassword = (String)request.getParameter("new-password");
			String newPassword2 = (String)request.getParameter("new-password2");
			String oldPassword = (String)request.getParameter("old-password");
			
			String cancel = (String)request.getParameter("cancel");
			if (cancel != null) {
				request.getRequestDispatcher("index.jsp").forward(request, response);
				return;
			}
			String cancelUser = (String)request.getParameter("cancel-user");
			if (cancelUser != null) {
				response.sendRedirect("login?view-user=" + bean.getViewUser().getUserId() + proxy.proxyString());
				return;
			}
			String cancelUserMessage = (String)request.getParameter("cancel-user-message");
			if (cancelUserMessage != null) {
				request.getRequestDispatcher("browse-user-message.jsp").forward(request, response);
				return;
			}
			String viewUser = (String)request.getParameter("view-user");
			String viewMessage = (String)request.getParameter("view-message");
			if (viewUser != null) {
				if (!bean.viewUser(viewUser)) {
					request.getRequestDispatcher("user.jsp").forward(request, response);
					return;
				}
				if (viewUser.startsWith("@")) {
					String botId = bean.getBotUserId(viewUser);
					if (botId != null && !botId.isEmpty()) {
						response.sendRedirect("browse?id=" + botId + proxy.proxyString());
						return;
					}
				}
				request.getRequestDispatcher("user.jsp").forward(request, response);
				return;
			}
			
			String friend = (String)request.getParameter("friend");
			String userFriend = (String)request.getParameter("add-new-friend");
			if (userFriend != null) {
				bean.verifyPostToken(postToken);
				String friendName = "";
				if (friend != null) {
					friendName = friend;
				} else {
					if (bean.getViewUser() != null) {
						friendName = bean.getViewUser().getUserId();
					}
				}
				bean.createUserFriendship(friendName);
				response.sendRedirect("login?browse-user-friends" + proxy.proxyString());
				return;
			}
			
			if (viewMessage != null) {
				if (!bean.viewMessage(viewMessage)) {
					request.getRequestDispatcher("browse-user-message.jsp").forward(request, response);
					return;
				}
				request.getRequestDispatcher("user-message.jsp").forward(request, response);
				return;
			}
			String requestResetUser = (String)request.getParameter("request-reset-password");
			if (requestResetUser != null) {
				request.getRequestDispatcher("reset-password.jsp").forward(request, response);
				return;
			}
			String resetUser = (String)request.getParameter("reset-password");
			if (resetUser != null) {
				try {
					List<User> users = AdminDatabase.instance().findUserByEmail(email.trim());
					for (User usr : users) {
						bean.resetPassword(usr.getUserId().trim(), email.trim());
					}
				} catch (Exception e) {
					bean.error(e);
				}
				request.getRequestDispatcher("reset-password.jsp").forward(request, response);
				return;
			}
			String verifyResetUser = (String)request.getParameter("verify-reset-password");
			if (verifyResetUser != null) {
				String token = (String)request.getParameter("token");
				if (bean.verifyResetPassword(verifyResetUser, token)) {
					request.getRequestDispatcher("verify-reset-password.jsp").forward(request, response);
				} else {
					request.getRequestDispatcher("login.jsp").forward(request, response);
				}
				return;
			}
			String resetPasswordComplete = (String)request.getParameter("reset-password-complete");
			if (resetPasswordComplete != null) {
				bean.verifyPostToken(postToken);
				if (!bean.resetPasswordComplete(password.trim(), password2.trim())) {
					request.getRequestDispatcher("verify-reset-password.jsp").forward(request, response);
				} else {
					request.getRequestDispatcher("login.jsp").forward(request, response);
				}
				return;
			}
			String unsubscribe = (String)request.getParameter("unsubscribe");
			String verfiyToken = (String)request.getParameter("token");
			if (unsubscribe != null) {
				bean.unsubscribe(unsubscribe, user, verfiyToken);
				request.getRequestDispatcher("unsubscribe.jsp").forward(request, response);
				return;
			}
			String verifyUser = (String)request.getParameter("verify-user");
			if (verifyUser != null) {
				bean.verifyUser(verifyUser, verfiyToken);
				request.getRequestDispatcher("verify-user.jsp").forward(request, response);
				return;
			}
			String sendVerify = (String)request.getParameter("send-verify");
			if (sendVerify != null) {
				bean.verifyPostToken(postToken);
				if (bean.isSuper()) {
					bean.setViewUser(bean.sendEmailVerify(bean.getViewUser()));
				} else {
					bean.setUser(bean.sendEmailVerify(bean.getUser()));
					bean.setViewUser(bean.getUser());
				}
				if (bean.getViewUser().hasEmail()) {
					bean.setError(new BotException("A verification email has been sent.\nPlease check your email and click on the link to verfiy your address."));
				} else {
					bean.setError(new BotException("Your account does not have an email address set, please edit your account."));
				}
				response.sendRedirect("login?view-user=" + bean.getViewUser().getUserId() + proxy.proxyString());
				return;
			}
			String browseUserMessages = (String)request.getParameter("browse-user-messages");
			if (browseUserMessages != null) {
				request.getRequestDispatcher("browse-user-message.jsp").forward(request, response);
				return;
			}
			
			String browseUserFriends = (String)request.getParameter("browse-user-friends");
			if (browseUserFriends != null) {
				request.getRequestDispatcher("friendships.jsp").forward(request, response);
				return;
			}
			
			String browseUserStats= (String)request.getParameter("browse-user-stats");
			if (browseUserStats!= null) {
				request.getRequestDispatcher("user-stats.jsp").forward(request, response);
				return;
			}
			
			String createUserMessage = (String)request.getParameter("create-user-message");
			if (createUserMessage != null) {
				bean.setUserMessage(null);
				request.getRequestDispatcher("create-user-message.jsp").forward(request, response);
				return;
			}
			String sendMessage = (String)request.getParameter("send-message");
			if (sendMessage != null) {
				bean.sendMessage(user, sendMessage);
				request.getRequestDispatcher("create-user-message.jsp").forward(request, response);
				return;
			}
			String replyUserMessage = (String)request.getParameter("reply-user-message");
			if (replyUserMessage != null) {
				request.getRequestDispatcher("create-user-message-reply.jsp").forward(request, response);
				return;
			}
			String createNewUserMessage = (String)request.getParameter("create-new-user-message");
			String target = (String)request.getParameter("target");
			String subject = (String)request.getParameter("subject");
			String message = (String)request.getParameter("message");
			if (createNewUserMessage != null) {
				bean.verifyPostToken(postToken);
				String token = (String)request.getParameter("token");
				if (token == null || !token.equals(String.valueOf(bean.hashCode()))) {
					AdminDatabase.instance().log(Level.WARNING, "spam", BeanServlet.extractIP(request));
					response.sendRedirect("banned.html");
					return;
				}
				if (bean.createUserMessage(target, subject, message, BeanServlet.extractIP(request)) != null) {
					request.getRequestDispatcher("browse-user-message.jsp").forward(request, response);
				} else {
					request.getRequestDispatcher("create-user-message.jsp").forward(request, response);
				}
				return;
			}
			String createUserMessageReply = (String)request.getParameter("create-user-message-reply");
			if (createUserMessageReply != null) {
				bean.verifyPostToken(postToken);
				if (bean.createUserMessageReply(message) != null) {
					request.getRequestDispatcher("browse-user-message.jsp").forward(request, response);
				} else {
					request.getRequestDispatcher("create-user-message-reply.jsp").forward(request, response);
				}
				return;
			}
			
			String createUser = (String)request.getParameter("create-user");
			String editUser = (String)request.getParameter("edit-user");
			String saveUser = (String)request.getParameter("save-user");
			String userDetails = (String)request.getParameter("user-details");
			String signIn = (String)request.getParameter("sign-in");
			String signUp = (String)request.getParameter("sign-up");
			if (editUser != null) {
				if (!bean.editUser()) {
					request.getRequestDispatcher("user.jsp").forward(request, response);
					return;
				}
				request.getRequestDispatcher("edit-user.jsp").forward(request, response);
				return;
			}
			if (userDetails != null) {
				bean.viewUser(bean.getUserId());
				request.getRequestDispatcher("user.jsp").forward(request, response);
				return;
			}
			String unflagUser = (String)request.getParameter("unflag-user");
			String blockUser = (String)request.getParameter("block-user");
			String unblockUser = (String)request.getParameter("unblock-user");
			String becomeUser = (String)request.getParameter("become-user");
			String flagUser = (String)request.getParameter("flag-user");
			String flagged = (String)request.getParameter("flagged");
			String flagReason = (String)request.getParameter("flag-reason");
			String upload = (String)request.getParameter("upload");
			if (upload != null) {
				try {
					throw new BotException("Please select the file to upload (wait for hover)");
				} catch (Exception failed) {
					bean.error(failed);
				}
				response.sendRedirect("login?view-user=" + bean.getViewUser().getUserId() + proxy.proxyString());
				return;
			}
			if (flagUser != null) {
				try {
					bean.verifyPostToken(postToken);
					if (!"on".equals(flagged)) {
						throw new BotException("You must check 'Flag'");
					}
					if (flagReason == null || flagReason.equals("reason") || flagReason.isEmpty()) {
						throw new BotException("You must enter the reason for flagging the user");
					}
					bean.flagUser(flagReason);
				} catch (Exception failed) {
					bean.error(failed);
				}
				response.sendRedirect("login?view-user=" + bean.getViewUser().getUserId() + proxy.proxyString());
				return;
			}
			if (unflagUser != null) {
				try {
					bean.verifyPostToken(postToken);
					bean.unflagUser();
				} catch (Exception failed) {
					bean.error(failed);
				}
				response.sendRedirect("login?view-user=" + bean.getViewUser().getUserId() + proxy.proxyString());
				return;
			}
			if (blockUser != null) {
				try {
					bean.verifyPostToken(postToken);
					bean.blockUser();
				} catch (Exception failed) {
					bean.error(failed);
				}
				response.sendRedirect("login?view-user=" + bean.getViewUser().getUserId() + proxy.proxyString());
				return;
			}
			if (unblockUser != null) {
				try {
					bean.verifyPostToken(postToken);
					bean.unblockUser();
				} catch (Exception failed) {
					bean.error(failed);
				}
				response.sendRedirect("login?view-user=" + bean.getViewUser().getUserId() + proxy.proxyString());
				return;
			}
			if (becomeUser != null) {
				try {
					bean.verifyPostToken(postToken);
					bean.becomeUser();
				} catch (Exception failed) {
					bean.error(failed);
				}
				request.getRequestDispatcher("login.jsp").forward(request, response);
				return;
			}
			String resetAppId = (String)request.getParameter("reset-app-id");
			if (resetAppId != null) {
				try {
					bean.verifyPostToken(postToken);
					bean.resetAppId();
				} catch (Exception failed) {
					bean.error(failed);
				}
				response.sendRedirect("login?view-user=" + bean.getViewUser().getUserId() + proxy.proxyString());
				return;
			}
			String deleteUserMessage = (String)request.getParameter("delete-user-message");
			if (deleteUserMessage != null) {
				bean.verifyPostToken(postToken);
				if (bean.deleteUserMessage()) {
					request.getRequestDispatcher("browse-user-message.jsp").forward(request, response);
				} else {
					request.getRequestDispatcher("user-message.jsp").forward(request, response);
				}
				return;
			}
			String deleteUser = (String)request.getParameter("delete-user");
			String confirmDelete = (String)request.getParameter("confirmDelete");
			if (deleteUser != null) {
				try {
					bean.verifyPostToken(postToken);
					if (!"on".equals(confirmDelete)) {
						throw new BotException("You must check 'Delete'");
					}
					bean.deleteUser();
				} catch (Exception failed) {
					bean.error(failed);
					response.sendRedirect("login?view-user=" + bean.getViewUser().getUserId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("login.jsp").forward(request, response);
				return;
			}
			String connect = (String)request.getParameter("connect");

			String credentialsType = (String)request.getParameter("credentials-type");
			String credentialsUserID = (String)request.getParameter("credentials-userid");
			String credentialsToken = (String)request.getParameter("credentials-token");
			String dateOfBirth = (String)request.getParameter("dateOfBirth");
			String hint = (String)request.getParameter("hint");
			String displayName = (String)request.getParameter("display-name");
			String website = (String)request.getParameter("website");
			String bio = (String)request.getParameter("bio");
			String over18 = (String)request.getParameter("over18");
			String adCode = (String)request.getParameter("adCode");
			String userAccess = (String)request.getParameter("userAccess");
			String userTags = (String)request.getParameter("tags");
			
			String logout = (String)request.getParameter("logout");
			
			if (createUser != null) {
				bean.verifyPostToken(postToken);
				String ip = BeanServlet.extractIP(request);
				AdminDatabase.instance().log(Level.INFO, "create user", ip);
				if (AdminDatabase.bannedIPs.containsKey(BeanServlet.extractIP(request))) {
					AdminDatabase.instance().log(Level.WARNING, "banned", ip);
					response.sendRedirect("banned.html");
					return;
				}
				String token = (String)request.getParameter("token");
				if (token == null || !token.equals(String.valueOf(bean.hashCode()))) {
					AdminDatabase.instance().log(Level.WARNING, "banned", BeanServlet.extractIP(request));
					response.sendRedirect("banned.html");
					return;
				}
				String terms = (String)request.getParameter("terms");
				if (!bean.createUser(user, password, password2, dateOfBirth, hint, name, "", "", ip, "web", "", userAccess, email, website, bio, "on".equals(displayName), "on".equals(over18),
							credentialsType, credentialsUserID, credentialsToken,
							null, null, null,
							"on".equals(terms))) {
					request.getRequestDispatcher("create-user.jsp").forward(request, response);
				} else {
					request.getRequestDispatcher("login.jsp").forward(request, response);
				}
				return;
			}
			if (signIn != null) {
				bean.setRedirect(request.getHeader("referer"));
				request.getRequestDispatcher("login.jsp").forward(request, response);
				return;
			}
			if (signUp != null) {
				bean.setRedirect(request.getHeader("referer"));
				request.getRequestDispatcher("create-user.jsp").forward(request, response);
				return;
			}
			String emailNotices = (String)request.getParameter("email-notices");
			String emailMessages = (String)request.getParameter("email-messages");
			String emailSummary = (String)request.getParameter("email-summary");
			String type = (String)request.getParameter("type");
			String plan = (String)request.getParameter("plan");
			String verifiedPayment = (String)request.getParameter("verifiedPayment");
			Boolean isSubscribed = null;
			if (plan != null) {
				if (!plan.equals("onetime")) {
					isSubscribed = true;
				} else {
					isSubscribed = false;
				}
			}
			if (saveUser != null) {
				bean.verifyPostToken(postToken);
				if (!bean.updateUser(oldPassword, newPassword, newPassword2, hint, name, null, null, userAccess, userTags, email, "web", "on".equals(emailNotices), "on".equals(emailMessages), "on".equals(emailSummary),
							website, bio, "on".equals(displayName), "on".equals(over18), adCode, "on".equals(verifiedPayment), type, isSubscribed)) {
					request.getRequestDispatcher("edit-user.jsp").forward(request, response);
				} else {
					response.sendRedirect("login?view-user=" + bean.getViewUser().getUserId() + proxy.proxyString());
				}
				return;
			}
			if (logout != null) {
				bean.disconnect();
				bean.logout();
				if (!proxy.isProxy() && !bean.isEmbedded()) {
					request.getSession().invalidate();
				}
				Cookie cookie = new Cookie("token", null);
				cookie.setMaxAge(0);
				response.addCookie(cookie);
				cookie = new Cookie("user", null);
				cookie.setMaxAge(0);
				response.addCookie(cookie);
				if (!bean.checkDomain(request, response)) {
					return;
				}
				if (bean.isEmbedded()) {
					request.getRequestDispatcher("login.jsp").forward(request, response);
				} else {
					response.sendRedirect("login.jsp");
				}
				return;
			} else if (connect != null) {
				bean.verifyPostToken(postToken);
				long token = 0;
				if (credentialsType != null && !credentialsType.isEmpty()) {
					token = bean.credentialsConnect(credentialsType, credentialsUserID, credentialsToken);
				} else {
					token = bean.connect(user, password, 0);
				}
				if ((token != 0) && "on".equals(remember)) {
					Cookie userCookie = new Cookie("user", user);
					Cookie passwordCookie = new Cookie("token", String.valueOf(token));
					// Set expiry date after 30 days for both the cookies.
					userCookie.setMaxAge(60*60*24*30); 
					passwordCookie.setMaxAge(60*60*24*30); 
					response.addCookie(userCookie);
					response.addCookie(passwordCookie);
				}
				if (bean.isLoggedIn()) {
					bean.setAgeConfirmed(true);
					if (bean.getUser().getIP() == null) {
						bean.updateIP(BeanServlet.extractIP(request));
					}
				}
				request.getRequestDispatcher("login.jsp").forward(request, response);
				return;
			}
		} catch (Throwable failed) {
			AdminDatabase.instance().log(failed);
			bean.setError(failed);
		}
		try {
			request.getRequestDispatcher("index.jsp").forward(request, response);
			return;
		} catch (Throwable failed) {
			AdminDatabase.instance().log(failed);
		}
		response.sendRedirect("index.jsp");
	}
}

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
package org.botlibre.web.service;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.sense.facebook.Facebook;
import org.botlibre.sense.facebook.FacebookMessaging;
import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.UserMessage;
import org.botlibre.web.admin.User.UserType;

public class FacebookService extends Service {
	public static int PAUSE = 10000;
	
	public static int SLEEP = 1000 * 60 * 10; // 10 minutes
	public static int PLATINUM_SLEEP = 1000 * 60 * 10; // 10 minutes.
	public static int GOLD_SLEEP = 1000 * 60 * 60; // 60 minutes.
	
	protected static FacebookService instance = new FacebookService();

	protected Thread bronzeChecker;
	protected Thread platinumChecker;
	protected Thread goldChecker;
	
	public FacebookService() {
	}

	public void startCheckingProfile() {
		setEnabled(true);
		this.checker = new Thread() {
			@Override
			public void run() {
				try {
					while (isEnabled()) {
						Utils.sleep(PAUSE);
						long start = System.currentTimeMillis();
						try {
							Stats.stats.lastFacebookRun = new Timestamp(System.currentTimeMillis());
							AdminDatabase.instance().log(Level.INFO, "Checking Facebook profiles");
							List<BotInstance> instances = AdminDatabase.instance().getAllFacebookInstances();
							checkInstances(instances);
						} catch (Throwable exception) {
							AdminDatabase.instance().log(Level.INFO, "exception");
							AdminDatabase.instance().log(exception);
						}
						long time = (System.currentTimeMillis() - start) / 1000;
						Stats.stats.facebookRuns++;
						AdminDatabase.instance().log(Level.INFO, "Done checking Facebook profiles", time);
						Utils.sleep(SLEEP);
						if (checker != this) {
							break;
						}
					}
				} catch (Throwable exception) {
					AdminDatabase.instance().log(Level.SEVERE, "Facebook profile checker failure");
					AdminDatabase.instance().log(exception);
				}
				AdminDatabase.instance().log(Level.INFO, "Facebook profile checker stopped");
			}
		};
		AdminDatabase.instance().log(Level.INFO, "Profile checker running");
		this.checker.start();

	}
	
	public static FacebookService instance() {
		return instance;
	}
	
	public Thread getPlatinumChecker() {
		return platinumChecker;
	}

	public void setPlatinumChecker(Thread platinumChecker) {
		this.platinumChecker = platinumChecker;
	}

	public Thread getBronzeChecker() {
		return bronzeChecker;
	}

	public void setBronzeChecker(Thread bronzeChecker) {
		this.bronzeChecker = bronzeChecker;
	}

	public Thread getGoldChecker() {
		return goldChecker;
	}

	public void setGoldChecker(Thread goldChecker) {
		this.goldChecker = goldChecker;
	}

	public void checkInstances(List<BotInstance> instances) {
		for (BotInstance instance : instances) {
			if (!isEnabled()) {
				break;
			}
			//if (!instance.getName().equals("testbot")) {
			//	continue;
			//}
			if ((instance.getMemoryLimit() > 0) && (instance.getMemorySize() > instance.getMemoryLimit() * 2)) {
				AdminDatabase.instance().log(Level.WARNING, "Memory size exceeded");
				continue;
			}
			// Check if the user or domain has expired.
			if (instance.getCreator().isExpired() || instance.getDomain().isExpired()) {
				disconnectExpired(instance);
				continue;
			}
			Bot bot = null;
			try {
				bot = connectInstance(instance);
				//bot.setDebugLevel(Level.FINE);
				Facebook facebook = bot.awareness().getSense(Facebook.class);
				facebook.checkProfile();
				Stats.stats.botFacebookPosts = Stats.stats.botFacebookPosts + facebook.getPosts();
				Stats.stats.botFacebookProcessed = Stats.stats.botFacebookProcessed + facebook.getPostsProcessed();
				Stats.stats.botFacebookLikes = Stats.stats.botFacebookLikes + facebook.getLikes();
				BotStats stats = BotStats.getStats(instance.getId(), instance.getName());
				stats.facebookPosts = stats.facebookPosts + facebook.getPosts();
				stats.facebookProcessed = stats.facebookProcessed + facebook.getPostsProcessed();
				stats.facebookLikes = stats.facebookLikes + facebook.getLikes();
				facebook.setPosts(0);
				facebook.setPostsProcessed(0);
				facebook.setLikes(0);

				if (facebook.getReplyToMessages()) {
					FacebookMessaging facebookMessaging = bot.awareness().getSense(FacebookMessaging.class);
					facebookMessaging.checkProfile();
					Stats.stats.botFacebookMessagesProcessed = Stats.stats.botFacebookMessagesProcessed + facebook.getMessagesProcessed();
					stats = BotStats.getStats(instance.getId(), instance.getName());
					stats.facebookMessagesProcessed = stats.facebookMessagesProcessed + facebook.getMessagesProcessed();
					facebookMessaging.setMessagesProcessed(0);
				}

				Utils.sleep(10000);
				int count = 0;
				while ((count < 30) && !bot.memory().getActiveMemory().isEmpty()) {
					count++;
					Utils.sleep(10000);
				}
				AdminDatabase.instance().updateInstanceSize(instance, bot.memory().getLongTermMemory().size());
			} catch (Throwable exception) {
				AdminDatabase.instance().log(Level.SEVERE, "Facebook profile failure", instance);  
				AdminDatabase.instance().log(exception);
			} finally {
				if (bot != null) {
					bot.shutdown();
				}
			}
		}
	}

	@Override
	public void disconnectExpired(BotInstance instance) {
		AdminDatabase.instance().addError(instance, "Account expired, disconnecting bot from Facebook");
		AdminDatabase.instance().updateInstanceFacebook(instance.getId(), false);
		instance.setEnableFacebook(false);
		
		StringWriter writer = new StringWriter();
		writer.write("<p>Your bot ");
		writer.write(instance.getName());
		writer.write(" has been disconnect from Facebook, because your account has expired.</p>\n");
		writer.write("<p>Please upgrade your account.</p>\n");
		
		String message = writer.toString();
		String subject = "Your Facebook bot has been disconnected";
		
		UserMessage userMessage = new UserMessage();
		userMessage.setSubject(subject);
		userMessage.setMessage(message);
		userMessage.setOwner(instance.getCreator());
		userMessage.setCreator(AdminDatabase.instance().validateUser("admin"));
		userMessage.setTarget(instance.getCreator());
		AdminDatabase.instance().createUserMessage(userMessage);
		
		if (instance.getCreator().hasEmail() && instance.getCreator().isVerified() && instance.getCreator().getEmailNotices()) {
			EmailService.instance().sendEmail(instance.getCreator().getEmail(), subject, null, message);
		}
	}
	
}

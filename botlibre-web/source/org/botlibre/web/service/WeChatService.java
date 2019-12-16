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
import org.botlibre.sense.wechat.WeChat;
import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.UserMessage;
import org.botlibre.web.admin.User.UserType;

public class WeChatService extends Service {
	public static int PAUSE = 10000;
	
	public static int SLEEP = 1000 * 60 * 15; // 15 minutes
	public static int PLATINUM_SLEEP = 1000 * 60 * 15; // 15 minutes.
	public static int GOLD_SLEEP = 1000 * 60 * 60; // 60 minutes.
	
	protected static WeChatService instance = new WeChatService();

	protected Thread bronzeChecker;
	protected Thread platinumChecker;
	protected Thread goldChecker;

	public WeChatService() {
	}
	
	public void startCheckingProfile() {
		setEnabled(true);
		this.checker = new Thread() {
			@Override
			public void run() {
				try {
					while (isEnabled()) {
						Utils.sleep(PAUSE);
						Calendar calendar = Calendar.getInstance();
						if (calendar.get(Calendar.HOUR_OF_DAY) == 2) {
							long start = System.currentTimeMillis();
							try {
								Stats.stats.lastWeChatRun = new Timestamp(System.currentTimeMillis());
								AdminDatabase.instance().log(Level.INFO, "Checking WeChat profiles");
								List<BotInstance> instances = AdminDatabase.instance().getAllWeChatInstances(UserType.Bronze);
								checkInstances(instances);
								instances = AdminDatabase.instance().getAllWeChatInstances(UserType.Basic);
								checkInstances(instances);
							} catch (Throwable exception) {
								AdminDatabase.instance().log(Level.INFO, "exception");
								AdminDatabase.instance().log(exception);
							}
							long time = (System.currentTimeMillis() - start) / 1000;
							Stats.stats.wechatRuns++;
							AdminDatabase.instance().log(Level.INFO, "Done checking WeChat profiles", time);
						}
						Utils.sleep(SLEEP);
						if (checker != this) {
							break;
						}
					}
				} catch (Throwable exception) {
					AdminDatabase.instance().log(Level.SEVERE, "WeChat profile checker failure");  
					AdminDatabase.instance().log(exception);
				}
				AdminDatabase.instance().log(Level.INFO, "WeChat profile checker stopped");   		
			}
		};
		AdminDatabase.instance().log(Level.INFO, "Profile checker running");
		this.checker.start();

		this.platinumChecker = new Thread() {
			@Override
			public void run() {
				try {
					while (isEnabled()) {
						Utils.sleep(PAUSE);
						long start = System.currentTimeMillis();
						try {
							Stats.stats.lastWeChatRun = new Timestamp(System.currentTimeMillis());
							AdminDatabase.instance().log(Level.INFO, "Checking Platinum WeChat profiles");
							List<BotInstance> instances = AdminDatabase.instance().getAllWeChatInstances(UserType.Admin);
							checkInstances(instances);
							instances = AdminDatabase.instance().getAllWeChatInstances(UserType.Partner);
							checkInstances(instances);
							instances = AdminDatabase.instance().getAllWeChatInstances(UserType.Platinum);
							checkInstances(instances);
						} catch (Exception exception) {
							AdminDatabase.instance().log(Level.INFO, "exception");
							AdminDatabase.instance().log(exception);
						}
						long time = (System.currentTimeMillis() - start) / 1000;
						Stats.stats.platinumWeChatRuns++;
						AdminDatabase.instance().log(Level.INFO, "Done checking Platinum WeChat profiles", time);
						Utils.sleep(PLATINUM_SLEEP);
						if (platinumChecker != this) {
							break;
						}
					}
				} catch (Throwable exception) {
					AdminDatabase.instance().log(Level.SEVERE, "Platinum WeChat profile checker failure");  
					AdminDatabase.instance().log(exception);
				}
				AdminDatabase.instance().log(Level.INFO, "Platinum WeChat profile checker stopped");   		
			}
		};
		AdminDatabase.instance().log(Level.INFO, "Platinum profile checker running");
		this.platinumChecker.start();

		this.goldChecker = new Thread() {
			@Override
			public void run() {
				try {
					while (isEnabled()) {
						Utils.sleep(PAUSE);
						long start = System.currentTimeMillis();
						try {
							Stats.stats.lastWeChatRun = new Timestamp(System.currentTimeMillis());
							AdminDatabase.instance().log(Level.INFO, "Checking Gold WeChat profiles");
							List<BotInstance> instances = AdminDatabase.instance().getAllWeChatInstances(UserType.Gold);
							checkInstances(instances);
						} catch (Exception exception) {
							AdminDatabase.instance().log(Level.INFO, "exception");
							AdminDatabase.instance().log(exception);
						}
						long time = (System.currentTimeMillis() - start) / 1000;
						Stats.stats.goldWeChatRuns++;
						AdminDatabase.instance().log(Level.INFO, "Done checking Gold WeChat profiles", time);
						Utils.sleep(GOLD_SLEEP);
						if (goldChecker != this) {
							break;
						}
					}
				} catch (Throwable exception) {
					AdminDatabase.instance().log(Level.SEVERE, "Gold WeChat profile checker failure");  
					AdminDatabase.instance().log(exception);
				}
				AdminDatabase.instance().log(Level.INFO, "Gold WeChat profile checker stopped");   		
			}
		};
		AdminDatabase.instance().log(Level.INFO, "Gold profile checker running");
		this.goldChecker.start();
	}
	
	public static WeChatService instance() {
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
			if ((instance.getMemoryLimit() > 0) && (instance.getMemorySize() > instance.getMemoryLimit() * 2)) {
				AdminDatabase.instance().log(Level.WARNING, "Memory size exceeded");
				continue;
			}
			// Check if the user or domain has expired.
			if (instance.getCreator().isExpired() || instance.getDomain().isExpired()) {
				disconnectExpired(instance);
				continue;
			}
			Bot bot = connectInstance(instance);
			try {
				WeChat wechat = bot.awareness().getSense(WeChat.class);
				wechat.checkProfile();
				//Stats.stats.botSkypePosts = Stats.stats.botSkypePosts + skype.getPosts();
				Stats.stats.botWeChatMessagesProcessed = Stats.stats.botWeChatMessagesProcessed + wechat.getMessagesProcessed();
				BotStats stats = BotStats.getStats(instance.getId(), instance.getName());
				//stats.skypePosts = stats.skypePosts + skype.getPosts();
				stats.wechatMessagesProcessed = stats.wechatMessagesProcessed + wechat.getMessagesProcessed();
				//skype.setPosts(0);
				wechat.setMessagesProcessed(0);
				
				Utils.sleep(10000);
				int count = 0;
				while ((count < 30) && !bot.memory().getActiveMemory().isEmpty()) {
					count++;
					Utils.sleep(10000);
				}
				AdminDatabase.instance().updateInstanceSize(instance, bot.memory().getLongTermMemory().size());
			} catch (Throwable exception) {
				AdminDatabase.instance().log(Level.SEVERE, "WeChat profile failure", instance);  
				AdminDatabase.instance().log(exception);
			} finally {
				bot.shutdown();
			}
		}
	}

	@Override
	public void disconnectExpired(BotInstance instance) {
		AdminDatabase.instance().addError(instance, "Account expired, disconnecting bot from WeChat");
		AdminDatabase.instance().updateInstanceWeChat(instance.getId(), false);
		instance.setEnableWeChat(false);
		
		StringWriter writer = new StringWriter();
		writer.write("<p>Your bot ");
		writer.write(instance.getName());
		writer.write(" has been disconnect from WeChat, because your account has expired.</p>\n");
		writer.write("<p>Please upgrade your account.</p>\n");
		
		String message = writer.toString();
		String subject = "Your WeChat has been disconnected";
		
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

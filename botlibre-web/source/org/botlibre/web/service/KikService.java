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
import org.botlibre.sense.kik.Kik;
import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.UserMessage;
import org.botlibre.web.admin.User.UserType;

public class KikService extends Service {
	public static int PAUSE = 10000;
	
	public static int SLEEP = 1000 * 60 * 15; // 15 minutes
	public static int PLATINUM_SLEEP = 1000 * 60 * 15; // 15 minutes.
	public static int GOLD_SLEEP = 1000 * 60 * 60; // 60 minutes.
	
	protected static KikService instance = new KikService();

	protected Thread bronzeChecker;
	protected Thread platinumChecker;
	protected Thread goldChecker;

	public KikService() {
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
								Stats.stats.lastKikRun = new Timestamp(System.currentTimeMillis());
								AdminDatabase.instance().log(Level.INFO, "Checking Kik profiles");
								List<BotInstance> instances = AdminDatabase.instance().getAllKikInstances(UserType.Bronze);
								checkInstances(instances);
								instances = AdminDatabase.instance().getAllKikInstances(UserType.Basic);
								checkInstances(instances);
							} catch (Throwable exception) {
								AdminDatabase.instance().log(Level.INFO, "exception");
								AdminDatabase.instance().log(exception);
							}
							long time = (System.currentTimeMillis() - start) / 1000;
							Stats.stats.kikRuns++;
							AdminDatabase.instance().log(Level.INFO, "Done checking Kik profiles", time);
						}
						Utils.sleep(SLEEP);
						if (checker != this) {
							break;
						}
					}
				} catch (Throwable exception) {
					AdminDatabase.instance().log(Level.SEVERE, "Kik profile checker failure");  
					AdminDatabase.instance().log(exception);
				}
				AdminDatabase.instance().log(Level.INFO, "Kik profile checker stopped");   		
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
							Stats.stats.lastKikRun = new Timestamp(System.currentTimeMillis());
							AdminDatabase.instance().log(Level.INFO, "Checking Platinum Kik profiles");
							List<BotInstance> instances = AdminDatabase.instance().getAllKikInstances(UserType.Admin);
							checkInstances(instances);
							instances = AdminDatabase.instance().getAllKikInstances(UserType.Partner);
							checkInstances(instances);
							instances = AdminDatabase.instance().getAllKikInstances(UserType.Platinum);
							checkInstances(instances);
						} catch (Exception exception) {
							AdminDatabase.instance().log(Level.INFO, "exception");
							AdminDatabase.instance().log(exception);
						}
						long time = (System.currentTimeMillis() - start) / 1000;
						Stats.stats.platinumKikRuns++;
						AdminDatabase.instance().log(Level.INFO, "Done checking Platinum Kik profiles", time);
						Utils.sleep(PLATINUM_SLEEP);
						if (platinumChecker != this) {
							break;
						}
					}
				} catch (Throwable exception) {
					AdminDatabase.instance().log(Level.SEVERE, "Platinum Kik profile checker failure");  
					AdminDatabase.instance().log(exception);
				}
				AdminDatabase.instance().log(Level.INFO, "Platinum Kik profile checker stopped");   		
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
							Stats.stats.lastKikRun = new Timestamp(System.currentTimeMillis());
							AdminDatabase.instance().log(Level.INFO, "Checking Gold Kik profiles");
							List<BotInstance> instances = AdminDatabase.instance().getAllKikInstances(UserType.Gold);
							checkInstances(instances);
						} catch (Exception exception) {
							AdminDatabase.instance().log(Level.INFO, "exception");
							AdminDatabase.instance().log(exception);
						}
						long time = (System.currentTimeMillis() - start) / 1000;
						Stats.stats.goldKikRuns++;
						AdminDatabase.instance().log(Level.INFO, "Done checking Gold Kik profiles", time);
						Utils.sleep(GOLD_SLEEP);
						if (goldChecker != this) {
							break;
						}
					}
				} catch (Throwable exception) {
					AdminDatabase.instance().log(Level.SEVERE, "Gold Kik profile checker failure");  
					AdminDatabase.instance().log(exception);
				}
				AdminDatabase.instance().log(Level.INFO, "Gold Kik profile checker stopped");   		
			}
		};
		AdminDatabase.instance().log(Level.INFO, "Gold profile checker running");
		this.goldChecker.start();
	}
	
	public static KikService instance() {
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
			Bot bot = connectInstance(instance);
			try {
				Kik kik = bot.awareness().getSense(Kik.class);
				kik.checkProfile();
				//Stats.stats.botSkypePosts = Stats.stats.botSkypePosts + skype.getPosts();
				Stats.stats.botKikMessagesProcessed = Stats.stats.botKikMessagesProcessed + kik.getMessagesProcessed();
				BotStats stats = BotStats.getStats(instance.getId(), instance.getName());
				//stats.skypePosts = stats.skypePosts + skype.getPosts();
				stats.kikMessagesProcessed = stats.kikMessagesProcessed + kik.getMessagesProcessed();
				//skype.setPosts(0);
				kik.setMessagesProcessed(0);
				
				Utils.sleep(10000);
				int count = 0;
				while ((count < 30) && !bot.memory().getActiveMemory().isEmpty()) {
					count++;
					Utils.sleep(10000);
				}
				AdminDatabase.instance().updateInstanceSize(instance, bot.memory().getLongTermMemory().size());
			} catch (Throwable exception) {
				AdminDatabase.instance().log(Level.SEVERE, "Kik profile failure", instance);  
				AdminDatabase.instance().log(exception);
			} finally {
				bot.shutdown();
			}
		}
	}

	@Override
	public void disconnectExpired(BotInstance instance) {
		AdminDatabase.instance().addError(instance, "Account expired, disconnecting bot from Kik");
		AdminDatabase.instance().updateInstanceKik(instance.getId(), false);
		instance.setEnableKik(false);
		
		StringWriter writer = new StringWriter();
		writer.write("<p>Your bot ");
		writer.write(instance.getName());
		writer.write(" has been disconnect from Kik, because your account has expired.</p>\n");
		writer.write("<p>Please upgrade your account.</p>\n");
		
		String message = writer.toString();
		String subject = "Your Kik has been disconnected";
		
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

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
import org.botlibre.sense.twitter.Twitter;
import org.botlibre.sense.twitter.TwitterDirectMessaging;
import org.botlibre.thought.language.Language;
import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.UserMessage;
import org.botlibre.web.admin.User.UserType;

public class TwitterService extends Service {
	public static int PAUSE = 5000;
	
	public static int SLEEP = 1000 * 60 * 10; // 10 minutes
	public static int PLATINUM_SLEEP = 1000 * 60 * 10; // 10 minutes.
	public static int GOLD_SLEEP = 1000 * 60 * 60; // 60 minutes.
	
	protected static TwitterService instance = new TwitterService();
	
	protected Thread platinumChecker;
	protected Thread bronzeChecker;
	protected Thread goldChecker;
	
	public TwitterService() {
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
							Stats.stats.lastTwitterRun = new Timestamp(System.currentTimeMillis());
							AdminDatabase.instance().log(Level.INFO, "Checking Twitter profiles");
							List<BotInstance> instances = AdminDatabase.instance().getAllTwitterInstances();
							checkInstances(instances);
						} catch (Throwable exception) {
							AdminDatabase.instance().log(Level.INFO, "exception");
							AdminDatabase.instance().log(exception);
						}
						long time = (System.currentTimeMillis() - start) / 1000;
						Stats.stats.twitterRuns++;
						AdminDatabase.instance().log(Level.INFO, "Done checking Twitter profiles", time);
						Utils.sleep(SLEEP);
						if (checker != this) {
							break;
						}
					}
				} catch (Throwable exception) {
					AdminDatabase.instance().log(Level.SEVERE, "Twitter profile checker failure");
					AdminDatabase.instance().log(exception);
				}
				AdminDatabase.instance().log(Level.INFO, "Twitter Profile checker stopped");
			}
		};
		AdminDatabase.instance().log(Level.INFO, "Twitter profile checker running");
		this.checker.start();
	}
	
	public static TwitterService instance() {
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
			Bot bot = null;
			try {
				bot = connectInstance(instance);
				//bot.setDebugLevel(Level.FINE);
				Twitter twitter = bot.awareness().getSense(Twitter.class);
				Language language = bot.mind().getThought(Language.class);
				language.defaultResponses = 0;
				language.confidence = 0;
				language.sentiment = 0;
				twitter.conversations = 0;
				twitter.engaged = 0;
				twitter.checkProfile();
				Stats.stats.botTweets = Stats.stats.botTweets + twitter.getTweets();
				Stats.stats.botRetweets = Stats.stats.botRetweets + twitter.getRetweets();
				Stats.stats.botTweetsProcessed = Stats.stats.botTweetsProcessed + twitter.getTweetsProcessed();
				BotStats stats = BotStats.getStats(instance.getId(), instance.getName());
				stats.tweets = stats.tweets + twitter.getTweets();
				stats.retweets = stats.retweets + twitter.getRetweets();
				stats.tweetsProcessed = stats.tweetsProcessed + twitter.getTweetsProcessed();
				twitter.setTweets(0);
				twitter.setRetweets(0);
				twitter.setTweetsProcessed(0);
				if (twitter.getReplyToMessages()) {
					twitter = bot.awareness().getSense(TwitterDirectMessaging.class);
					twitter.checkProfile();
					Stats.stats.botDirectMessagesProcessed = Stats.stats.botDirectMessagesProcessed + twitter.getTweetsProcessed();
					stats = BotStats.getStats(instance.getId(), instance.getName());
					stats.conversations = stats.conversations + twitter.conversations;
					stats.engaged = stats.engaged + twitter.engaged;
					stats.defaultResponses = stats.defaultResponses + language.defaultResponses;
					stats.confidence = stats.confidence + language.confidence;
					stats.sentiment = stats.sentiment + language.sentiment;
					stats.messages = stats.messages + twitter.getTweetsProcessed();
					stats.directMessagesProcessed = stats.directMessagesProcessed + twitter.getTweetsProcessed();
					Stats.stats.botMessages = Stats.stats.botMessages + twitter.getTweetsProcessed();
					Stats.stats.botConversations = Stats.stats.botConversations + twitter.conversations;
					twitter.conversations = 0;
					twitter.engaged = 0;
					language.defaultResponses = 0;
					language.confidence = 0;
					language.sentiment = 0;
					twitter.setTweets(0);
					twitter.setRetweets(0);
					twitter.setTweetsProcessed(0);
				}
				Utils.sleep(10000);
				int count = 0;
				while ((count < 30) && !bot.memory().getActiveMemory().isEmpty()) {
					count++;
					Utils.sleep(10000);
				}
				AdminDatabase.instance().updateInstanceSize(instance, bot.memory().getLongTermMemory().size());
			} catch (Throwable exception) {
				AdminDatabase.instance().log(Level.SEVERE, "Twitter profile failure", instance);  
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
		AdminDatabase.instance().addError(instance, "Account expired, disconnecting bot from Twitter");
		AdminDatabase.instance().updateInstanceTwitter(instance.getId(), false);
		instance.setEnableTwitter(false);
		
		StringWriter writer = new StringWriter();
		writer.write("<p>Your bot ");
		writer.write(instance.getName());
		writer.write(" has been disconnect from Twitter, because your account has expired.</p>\n");
		writer.write("<p>Please upgrade your account.</p>\n");
		
		String message = writer.toString();
		String subject = "Your Twitterbot has been disconnected";
		
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

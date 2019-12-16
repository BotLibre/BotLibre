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
import java.util.Calendar;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.LogListener;
import org.botlibre.ProfanityException;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.UserMessage;

public class Service {
	public static int SLEEP = 1000 * 60 * 20; // 20 minutes.
	
	/** Checker thread. */
	protected Thread checker;
	
	protected boolean isEnabled = true;
	

	public Thread getChecker() {
		return checker;
	}

	public void setChecker(Thread checker) {
		this.checker = checker;
	}

	public Bot connectInstance(final BotInstance instance) {
		Bot bot = Bot.createInstanceFromPool(instance.getDatabaseName(), instance.isSchema());
		bot.setStats(new BotStatListener(instance.getId(), instance.getName()));
		instance.initialize(bot);
		int size = bot.memory().getLongTermMemory().size();
		if ((size/1000) != (instance.getMemorySize()/1000)) {
			AdminDatabase.instance().updateInstanceSize(instance, size);
		}
		bot.addLogListener(new LogListener() {
			@Override
			public void log(Object source, String message, Level level, Object[] arguments) {
				if (level.intValue() >= Level.WARNING.intValue()) {
					if (message.contains(ProfanityException.MESSAGE)) {
						return;
					}
					ErrorStats.error(message);
					BotStats stats = BotStats.getStats(instance.getId(), instance.getName());
					stats.errors++;
					StringWriter writer = new StringWriter();
					writer.append(Utils.printDate(Calendar.getInstance()) + " - " + level + " -- " + source + ":" + message);
					for (Object argument : arguments) {
						writer.append(" - " + argument);
					}
					String text = writer.toString();
					AdminDatabase.instance().addError(instance, text);
					if (message.indexOf("Twitter.connect") != -1) {
						if (instance.getEnableTwitter() && message.indexOf("credentials") != -1) {
							AdminDatabase.instance().addError(instance, "Twitter credentials incorrect, disconnecting bot from Twitter");
							AdminDatabase.instance().updateInstanceTwitter(instance.getId(), false);
							instance.setEnableTwitter(false);
							emailTwitterDisconnect(instance);
						}
						if (instance.getEnableTwitter() && message.indexOf("URI requested is invalid") != -1) {
							AdminDatabase.instance().addError(instance, "Twitter user no longer exists, disconnecting bot from Twitter");
							AdminDatabase.instance().updateInstanceTwitter(instance.getId(), false);
							instance.setEnableTwitter(false);
							emailTwitterDisconnect(instance);
						}
					} else if (instance.getEnableTwitter() && message.indexOf("Twitter") != -1) {
						if (message.indexOf("Your account is suspended") != -1) {
							AdminDatabase.instance().addError(instance, "Your Twitter account has been suspended by Twitter, disconnecting bot from Twitter");
							AdminDatabase.instance().updateInstanceTwitter(instance.getId(), false);
							instance.setEnableTwitter(false);
							emailTwitterDisconnect(instance);
						}
						if (message.indexOf("this account is temporarily locked") != -1) {
							AdminDatabase.instance().addError(instance, "Your Twitter account has been locked by Twitter, disconnecting bot from Twitter");
							AdminDatabase.instance().updateInstanceTwitter(instance.getId(), false);
							instance.setEnableTwitter(false);
							emailTwitterDisconnect(instance);
						}
					} else if (instance.getEnableEmail() && message.indexOf("javax.mail.AuthenticationFailedException") != -1) {
						if (message.indexOf("Invalid credentials") != -1) {
							AdminDatabase.instance().addError(instance, "Email credentials incorrect, disconnecting bot from email");
							AdminDatabase.instance().updateInstanceEmail(instance.getId(), false);
							instance.setEnableEmail(false);
							emailEmailDisconnect(instance);
						}
					} else if (instance.getEnableFacebook() && message.indexOf("Facebook") != -1) {
						if (message.indexOf("Error validating access token") != -1) {
							AdminDatabase.instance().addError(instance, "Facebook credentials expired or incorrect, you must reauthenticate, disconnecting bot from Facebook");
							AdminDatabase.instance().updateInstanceFacebook(instance.getId(), false);
							instance.setEnableFacebook(false);
							emailFacebookDisconnect(instance);
						}
						if (instance.getEnableFacebook() && message.indexOf("active access token") != -1) {
							AdminDatabase.instance().addError(instance, "Facebook credentials expired or incorrect, you must reauthenticate, disconnecting bot from Facebook");
							AdminDatabase.instance().updateInstanceFacebook(instance.getId(), false);
							instance.setEnableFacebook(false);
							emailFacebookDisconnect(instance);
						}
						if (instance.getEnableFacebook() && message.indexOf("Error validating access token: Session has expired") != -1) {
							AdminDatabase.instance().addError(instance, "Facebook credentials expired, you must reauthenticate, disconnecting bot from Facebook");
							AdminDatabase.instance().updateInstanceFacebook(instance.getId(), false);
							instance.setEnableFacebook(false);
							emailFacebookDisconnect(instance);
						}
						if (instance.getEnableFacebook() && message.indexOf("This Page access token belongs to a Page that has been deleted") != -1) {
							AdminDatabase.instance().addError(instance, "Facebook Page has been deleted, disconnecting bot from Facebook");
							AdminDatabase.instance().updateInstanceFacebook(instance.getId(), false);
							instance.setEnableFacebook(false);
							emailFacebookDisconnect(instance);
						}
					}
				}
			}
	
			@Override
			public void log(Throwable error) {}
			
			@Override
			public void logLevelChange(Level level) { }
		});
		return bot;
	}
	
	public void disconnectExpired(BotInstance instance) {
		// Subclass should disconnect from the service.
	}
	
	public void emailTwitterDisconnect(BotInstance instance) {
		StringWriter writer = new StringWriter();
		writer.write("<p>Your bot ");
		writer.write(instance.getName());
		writer.write(" has been disconnect from Twitter, because Twitter rejected its login credentials.</p>\n");
		writer.write("<p>Please verify your login credentials and reconnect your bot to Twitter from your bot's Twitter properties page.</p>\n");
		
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
	
	public void emailFacebookDisconnect(BotInstance instance) {
		StringWriter writer = new StringWriter();
		writer.write("<p>Your bot ");
		writer.write(instance.getName());
		writer.write(" has been disconnect from Facebook, because its Facebook autorization expired or was rejected.</p>\n");
		writer.write("<p>Please reauthorize and reconnect your bot to Facebook from your bot's Facebook properties page.</p>\n");
		
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
	
	public void emailEmailDisconnect(BotInstance instance) {
		StringWriter writer = new StringWriter();
		writer.write("<p>Your bot ");
		writer.write(instance.getName());
		writer.write(" has been disconnect from email, because its login credentials failed.</p>\n");
		writer.write("<p>Please verify your login credentials and reconnect your bot to email from your bot's Email properties page.</p>\n");
		
		String message = writer.toString();
		String subject = "Your email bot has been disconnected";
		
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

	public boolean isEnabled() {
		return isEnabled && !Site.READONLY;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
}

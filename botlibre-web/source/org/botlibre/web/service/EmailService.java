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
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.botlibre.Bot;
import org.botlibre.sense.email.Email;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.User.UserType;
import org.botlibre.web.admin.UserMessage;

public class EmailService extends Service {
	public static int PAUSE = 15000;
	
	public static int SLEEP = 1000 * 60 * 5; // 5 minutes.
	public static int PLATINUM_SLEEP = 1000 * 60 * 5; // 5 minutes.
	public static int GOLD_SLEEP = 1000 * 60 * 60; // 60 minutes.
	
	protected static EmailService instance = new EmailService();
	
	protected Thread bronzeChecker;
	protected Thread platinumChecker;
	protected Thread goldChecker;

	public static int MAX_USER_EMAIL = 50;
	public static int MAX_SIZE = 1000;
	public static Map<String, Integer> emails = new ConcurrentHashMap<String, Integer>();
	
	// Email settings
	protected String outgoingHost;
	protected int outgoingPort;
	protected String username;
	protected String password;
	protected boolean ssl;
	
	public EmailService() {
		init();
	}
	
	public void init() {
		this.outgoingHost = Site.EMAILSMTPHost;
		this.outgoingPort = Site.EMAILSMTPPORT;
		this.username = Site.EMAILUSER;
		this.password = Site.EMAILPASSWORD;
		this.ssl = Site.EMAILSSL;
		
		//this.outgoingHost = "";
		//this.outgoingPort = 587;
		//this.username = "";
		//this.password = "";
		//this.ssl = false;
	}
	
	public static void main(String[] args) {
		//Site.EMAILBOT = "bot@botlibre.com";
		EmailService.instance().sendEmail("test@botlibre.com", "Test", null, "Email test.");
	}
	
	public static void reset() {
		emails = new ConcurrentHashMap<String, Integer>();
	}
	
	public static boolean checkEmails(String email) {
		Integer count = emails.get(email);
		if (emails.size() > MAX_SIZE) {
			AdminDatabase.instance().log(Level.INFO, "Clearing email");
			emails.clear();
		}
		if (count == null) {
			emails.put(email, 0);
			return true;
		} else if (count < MAX_USER_EMAIL) {
			count = Integer.valueOf(count.intValue() + 1);
			emails.put(email, count);
			return true;
		} else {
			return false;
		}
	}
	
	public void sendEmail(final String address, final String subject, final String text,  final String html) {
		// Do not send more than a set number of emails to the same address. (avoid spam)
		if (!checkEmails(address)) {
			return;
		}
		Runnable task = new Runnable() {
			@Override
			public void run() {
				Store store = null;
				try {
					Stats.stats.emails++;
					AdminDatabase.instance().log(Level.INFO, "Sending email", address, subject);
					//store = connectStore();
					connectSession();
					Session session = connectSession();
		
					MimeMessage message = new MimeMessage(session);
					message.setFrom(new InternetAddress(Site.EMAILBOT, Site.NAME));
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
					message.setSubject(subject);
					if (html != null) {
						message.setContent(html, "text/html; charset=UTF-8");
					} else {
						message.setText(text);
					}
		
					// Send message
					Transport.send(message);
				} catch (Throwable messagingException) {
					AdminDatabase.instance().log(messagingException);
				} finally {
					try {
						if (store != null) {
							store.close();
						}
					} catch (Exception ignore) {}
				}
			}
		};
		Thread thread = new Thread(task);
		thread.start();
	}
	
	public Session connectSession() {
		Properties props = new Properties();
		Session session = null;
		if (ssl) {
			props.put("mail.smtp.host", this.outgoingHost);
			props.put("mail.smtp.port", this.outgoingPort);
			props.put("mail.smtp.socketFactory.port", this.outgoingPort);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.ssl.trust", this.outgoingHost);
			//props.put("mail.smtp.localhost", "botlibre.com");
			 
			session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
		} else {
			//props.put("mail.debug", "true");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", this.outgoingHost);
			props.put("mail.smtp.port", this.outgoingPort);
			props.put("mail.smtp.ssl.trust", this.outgoingHost);
			//props.put("mail.smtp.socketFactory.fallback", "true");
			 
			session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
			//session.setDebug(true);
		}
		return session;
	}

	public void startCheckingEmail() {
		setEnabled(true);
		this.checker = new Thread() {
			@Override
			public void run() {
				try {
					while (isEnabled()) {
						Utils.sleep(PAUSE);
						long start = System.currentTimeMillis();
						try {
							Stats.stats.lastEmailRun = new Timestamp(System.currentTimeMillis());
							AdminDatabase.instance().log(Level.INFO, "Checking email");
							List<BotInstance> instances = AdminDatabase.instance().getAllEmailInstances();
							checkInstances(instances);
						} catch (Exception exception) {
							AdminDatabase.instance().log(exception);
						}
						long time = (System.currentTimeMillis() - start) / 1000;
						Stats.stats.emailRuns++;
						AdminDatabase.instance().log(Level.INFO, "Done checking email", time);
						Utils.sleep(SLEEP);
						if (checker != this) {
							break;
						}
					}
				} catch (Throwable exception) {
					AdminDatabase.instance().log(exception);
				}
				AdminDatabase.instance().log(Level.INFO, "Email checker stopped");
			}
		};
		AdminDatabase.instance().log(Level.INFO, "Email checker running");
		this.checker.start();
	}

	public static EmailService instance() {
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
				Email sense = bot.awareness().getSense(Email.class);
				sense.checkEmail();
				Stats.stats.botEmails = Stats.stats.botEmails + sense.getEmails();
				Stats.stats.botEmailsProcessed = Stats.stats.botEmailsProcessed + sense.getEmailsProcessed();
				BotStats stats = BotStats.getStats(instance.getId(), instance.getName());
				stats.emails = stats.emails + sense.getEmails();
				stats.emailsProcessed = stats.emailsProcessed + sense.getEmailsProcessed();
				sense.setEmails(0);
				sense.setEmailsProcessed(0);
				Utils.sleep(10000);
				AdminDatabase.instance().updateInstanceSize(instance, bot.memory().getLongTermMemory().size());
			} catch (Throwable exception) {
				AdminDatabase.instance().log(Level.SEVERE, "Email profile failure", instance);  
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
		AdminDatabase.instance().addError(instance, "Account expired, disconnecting bot from email");
		AdminDatabase.instance().updateInstanceTelegram(instance.getId(), false);
		instance.setEnableEmail(false);
		
		StringWriter writer = new StringWriter();
		writer.write("<p>Your bot ");
		writer.write(instance.getName());
		writer.write(" has been disconnect from email, because your account has expired.</p>\n");
		writer.write("<p>Please upgrade your account.</p>\n");
		
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
	
}

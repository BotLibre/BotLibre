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
package org.botlibre.web.admin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.ProfanityException;
import org.botlibre.knowledge.database.DatabaseMemory;
import org.botlibre.sense.email.Email;
import org.botlibre.sense.facebook.Facebook;
import org.botlibre.sense.google.Google;
import org.botlibre.sense.http.Freebase;
import org.botlibre.sense.twitter.Twitter;
import org.botlibre.thought.forgetfulness.Forgetfulness;
import org.botlibre.thought.language.Language;
import org.botlibre.util.Utils;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.botlibre.web.Site;
import org.botlibre.web.admin.Domain.AccountType;
import org.botlibre.web.admin.Payment.PaymentStatus;
import org.botlibre.web.admin.User.UserType;
import org.botlibre.web.admin.UserPayment.UserPaymentStatus;
import org.botlibre.web.admin.UserPayment.UserPaymentType;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.BrowseBean.InstanceFilter;
import org.botlibre.web.bean.BrowseBean.InstanceRestrict;
import org.botlibre.web.bean.BrowseBean.InstanceSort;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.MemoryBean;
import org.botlibre.web.bean.SelfBean;
import org.botlibre.web.bean.TransactionBean.TransactionRestrict;
import org.botlibre.web.bean.TransactionBean.TransactionSort;
import org.botlibre.web.bean.UserBean.UserFilter;
import org.botlibre.web.bean.UserBean.UserRestrict;
import org.botlibre.web.bean.UserBean.UserSort;
import org.botlibre.web.bean.UserMessageBean.UserMessageSort;
import org.botlibre.web.chat.ChannelAttachment;
import org.botlibre.web.chat.ChatChannel;
import org.botlibre.web.chat.ChatMessage;
import org.botlibre.web.forum.Forum;
import org.botlibre.web.forum.ForumAttachment;
import org.botlibre.web.forum.ForumPost;
import org.botlibre.web.issuetracker.Issue;
import org.botlibre.web.issuetracker.IssueTracker;
import org.botlibre.web.issuetracker.IssueTrackerAttachment;
import org.botlibre.web.rest.InstanceConfig;
import org.botlibre.web.rest.UserMessageConfig;
import org.botlibre.web.script.Script;
import org.botlibre.web.script.ScriptSource;
import org.botlibre.web.service.AdminService;
import org.botlibre.web.service.AppIDStats;
import org.botlibre.web.service.BotStats;
import org.botlibre.web.service.BotTranslation;
import org.botlibre.web.service.EmailService;
import org.botlibre.web.service.ErrorStats;
import org.botlibre.web.service.FacebookService;
import org.botlibre.web.service.ForgetfulnessService;
import org.botlibre.web.service.IPStats;
import org.botlibre.web.service.KikService;
import org.botlibre.web.service.License;
import org.botlibre.web.service.LiveChatStats;
import org.botlibre.web.service.PageStats;
import org.botlibre.web.service.ReferrerStats;
import org.botlibre.web.service.SkypeService;
import org.botlibre.web.service.SlackService;
import org.botlibre.web.service.Stats;
import org.botlibre.web.service.TelegramService;
import org.botlibre.web.service.TimerService;
import org.botlibre.web.service.Translation;
import org.botlibre.web.service.TranslationId;
import org.botlibre.web.service.TwitterService;
import org.botlibre.web.service.WeChatService;

@SuppressWarnings("unchecked")
public class AdminDatabase {
	public static boolean DATABASEFAILURE = false;
//	public static boolean RECREATE_DATABASE = true;
//	public static String DATABASE_USER = "postgres";
//	public static String DATABASE_PASSWORD = "password";
//	public static String IMPORT_URL = "jdbc:postgresql:";
//	public static String DATABASE_URL = "jdbc:postgresql:botlibre";
//	public static String DATABASE_DRIVER = "org.postgresql.Driver";
	
	public static Map<String, String> bannedIPs = new HashMap<String, String>();
	
	public static ConcurrentMap<Long, Long> tokens = new ConcurrentHashMap<Long, Long>();
	
	protected static AdminDatabase instance = new AdminDatabase();

	public static long temporaryApplicationId = Math.abs(Utils.random().nextLong());
	public static boolean outOfMemory;
	
	protected EntityManagerFactory factory;
	protected Logger log;
	protected Calendar lastDateCheck;
	protected Calendar lastForumDateCheck;
	protected Calendar lastForumPostDateCheck;
	protected Calendar lastChannelDateCheck;
	
	protected Domain defaultDomain;
	
	@SuppressWarnings("rawtypes")
	protected ConcurrentMap locks = new ConcurrentHashMap();

	static {
		Logger root = Logger.getLogger("org.botlibre.web.admin");
		root.setUseParentHandlers(false);
		StreamHandler out = new StreamHandler(System.out, new SimpleFormatter()) {
			@Override
			public void publish(LogRecord record) {
				super.publish(record);
				flush();
			}
		};
		out.setLevel(Level.ALL);
		root.addHandler(out);
		try {
			FileHandler file = new FileHandler(Site.ID + ".log", 5000000, 50);
			file.setFormatter(new SimpleFormatter());
			file.setLevel(Level.ALL);
			root.addHandler(file);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		try {
			FileHandler file = new FileHandler(Site.ID + ".err", 5000000, 50);
			file.setFormatter(new SimpleFormatter());
			file.setLevel(Level.WARNING);
			root.addHandler(file);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	public static AdminDatabase instance() {
		return instance;
	}

	public static long getTemporaryApplicationId() {
		return temporaryApplicationId;
	}

	public static void resetTemporaryApplicationId() {
		temporaryApplicationId = Math.abs(Utils.random().nextLong());
	}

	public static void main(String args[]) {
		//encryptPassword("password");
		//System.out.println(new AdminDatabase().obfuscate("password"));
	}
	
//	public static void createAdminDatabase() {
//		try {
//			Class.forName(DATABASE_DRIVER);
//			Connection connection = DriverManager.getConnection(IMPORT_URL, DATABASE_USER, DATABASE_PASSWORD);
//			connection.createStatement().execute("create database botlibre");
//			connection.close();
//		} catch (Exception failed) {
//			failed.printStackTrace();
//			throw new RuntimeException(failed);
//		}
//	}
	
	public static void outOfMemory() {
		outOfMemory = true;
		ForgetfulnessService.freeMemory();
		User admin = AdminDatabase.instance().validateUser("admin");
		EmailService.instance().sendEmail(admin.getEmail(), "Fatal out of memory error occurred on: " + Site.NAME, null, "The server ran out of memory, please reboot it.");
	}

	public AdminDatabase() {
		this.log = Logger.getLogger("org.botlibre.web.admin");
		this.log.setLevel(Level.INFO);
	}
	
	public Object getLock(Object key) {
		Object lock = this.locks.get(key);
		if (lock == null) {
			Object newLock = new Object();
			lock = this.locks.putIfAbsent(key, newLock);
			if (lock == null) {
				lock = newLock;
			}
		}
		return lock;
	}
	
	public static Long getToken(Long id) {
		Long token = tokens.get(id);
		if (token == null) {
			Long newToken = Utils.random().nextLong();
			token = tokens.putIfAbsent(id, newToken);
			if (token == null) {
				token = newToken;
			}
		}
		return token;
	}
	
	public Logger getLog() {
		return log;
	}
	
	public void log(Throwable exception) {
		ErrorStats.error(exception);
		if (!(exception instanceof ProfanityException)) {
			// Don't count profanity.
			Stats.stats.errors++;
		}
		if (this.log.getLevel().intValue() > Level.WARNING.intValue()) {
			if (exception instanceof OutOfMemoryError) {
				AdminDatabase.outOfMemory();
			}
			return;
		}
		this.log.log(Level.WARNING, exception.toString());
		if (exception instanceof OutOfMemoryError) {
			AdminDatabase.outOfMemory();
		}
		if (!(exception instanceof BotException)) {
			exception.printStackTrace();
			StringWriter writer = new StringWriter();
			PrintWriter printer = new PrintWriter(writer);
			exception.printStackTrace(printer);
			printer.flush();
			String stack = writer.toString();
			this.log.log(Level.WARNING, stack.substring(0, Math.min(200, stack.length() - 1)));
		}
	}
	
	public void log(Level level, String message, Object... arguments) {
		log(level, message, null, (Object[])arguments);
	}
	
	public void log(Level level, String message, Object source, Object[] arguments) {
		if (this.log.getLevel().intValue() > level.intValue()) {
			return;
		}
		if (level.intValue() >= Level.WARNING.intValue()) {
			ErrorStats.error(message);
		}
		StringWriter writer = new StringWriter();
		if (source != null) {
			writer.write(source.toString());
			writer.write(": ");
		}
		writer.write(message);
		for (Object argument : arguments) {
			writer.write(" - ");
			writer.write(String.valueOf(argument));
		}
		this.log.log(level, writer.toString());
	}

	public void setLog(Logger log) {
		this.log = log;
	}
	
	public void shutdown() {
		AdminService.instance().setEnabled(false);
		ForgetfulnessService.instance().setEnabled(false);
		TwitterService.instance().setEnabled(false);
		FacebookService.instance().setEnabled(false);
		TelegramService.instance().setEnabled(false);
		EmailService.instance().setEnabled(false);
		SlackService.instance().setEnabled(false);
		SkypeService.instance().setEnabled(false);
		WeChatService.instance().setEnabled(false);
		KikService.instance().setEnabled(false);
		TimerService.instance().setEnabled(false);
		if (this.factory != null) {
			this.factory.close();
			this.factory = null;
		}
	}
	
	@SuppressWarnings("deprecation")
	public EntityManagerFactory getFactory() {
		if (this.factory == null) {
			synchronized (this) {
				if (this.factory == null) {
					log(Level.INFO, "Bot Libre - starting", Site.VERSION);
					log(Level.INFO, "Bot Libre AI - starting", Bot.VERSION);
					
					System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
					
					restorePlatformDatabaseSettings();
					DatabaseMemory.SCHEMA_URL_PREFIX = Site.getDatabaseUrl() + Site.PERSISTENCE_UNIT + "_bots" + "?currentSchema=";
					DatabaseMemory.DATABASE_URL = Site.getDatabaseUrl() + Site.PERSISTENCE_UNIT + "_bots";
					DatabaseMemory.DATABASE_USER = Site.DATABASEUSER;
					DatabaseMemory.DATABASE_PASSWORD = Site.DATABASEPASSWORD;
					
					Map<String, String> properties = new HashMap<String, String>();
					properties.put(PersistenceUnitProperties.JDBC_URL, Site.getDatabaseUrl() + Site.PERSISTENCE_UNIT);
					properties.put(PersistenceUnitProperties.JDBC_USER, Site.DATABASEUSER);
					properties.put(PersistenceUnitProperties.JDBC_PASSWORD, Site.DATABASEPASSWORD);
					
					/**properties.put(PersistenceUnitProperties.JDBC_DRIVER, DATABASE_DRIVER);
					properties.put(PersistenceUnitProperties.JDBC_URL, DATABASE_URL);
					properties.put(PersistenceUnitProperties.JDBC_USER, DATABASE_USER);
					properties.put(PersistenceUnitProperties.JDBC_PASSWORD, DATABASE_PASSWORD);
					if (RECREATE_DATABASE) {
						//properties.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.DROP_AND_CREATE);
						properties.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.CREATE_OR_EXTEND);
					}*/
					//properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "fine");
					EntityManager em =  null;
					try {
						this.factory = Persistence.createEntityManagerFactory(Site.PERSISTENCE_UNIT, properties);
						em =  this.factory.createEntityManager();
					} catch (Exception exception) {
						try {
							// If the database does not exist, create it the first time the server starts.
							new Migrate().initDatabase();
							this.factory = Persistence.createEntityManagerFactory(Site.PERSISTENCE_UNIT, properties);
							em =  this.factory.createEntityManager();
						} catch (Exception error) {
							DATABASEFAILURE = true;
							// If database still fails, clear factory to allow password reset.
							this.factory = null;
							if (error instanceof RuntimeException) {
								throw (RuntimeException)error;
							}
							throw new BotException(error);
						}
					}
					DATABASEFAILURE = false;
					
					// Read botplatform.xml to initialize server configuration.
					restorePlatformOtherSettings();
					
					Forgetfulness.MAX_SIZE = Site.MEMORYLIMIT;
					Language.MAX_PROCCESS_TIME = Site.MAX_PROCCESS_TIME;
					Utils.KEY = Site.KEY;
					Utils.URL_TIMEOUT = Site.URL_TIMEOUT;
					Email.SIGNATURE = Site.SIGNATURE;
					Freebase.KEY = Site.GOOGLEKEY;
					Google.KEY = Site.GOOGLEKEY;
					Google.CLIENTID = Site.GOOGLECLIENTID;
					Google.CLIENTSECRET = Site.GOOGLECLIENTSECRET;
					Bot.PROGRAM = Site.NAME;
					Bot.VERSION = Site.VERSION;
					Bot.POOL_SIZE = Site.MAX_BOT_POOL_SIZE;
					
					List<Profanity> profanity = em.createQuery("Select p from Profanity p").getResultList();
					for (Profanity word : profanity) {
						Utils.profanityMap.put(word.getProfanity(), "****");
					}
					List<IPBanned> banned = em.createQuery("Select b from IPBanned b").getResultList();
					for (IPBanned ip : banned) {
						bannedIPs.put(ip.getIP(), ip.getIP());
					}
					em.close();
					
					if (Site.TWITTER) {
						Twitter.oauthKey = Site.TWITTER_OAUTHKEY;
						Twitter.oauthSecret = Site.TWITTER_OAUTHSECRET;
						if (!new File("twitter.disable").exists()) {
							TwitterService.instance().startCheckingProfile();
						}
					}
					if (Site.FACEBOOK) {
						Facebook.oauthKey = Site.FACEBOOK_APPID;
						Facebook.oauthSecret = Site.FACEBOOK_APPSECRET;
						if (!new File("facebook.disable").exists()) {
							FacebookService.instance().startCheckingProfile();
						}
					}
					if (Site.TELEGRAM) {
						if (!new File("telegram.disable").exists()) {
							TelegramService.instance().startCheckingProfile();
						}
					}
					if (Site.SLACK) {
						if (!new File("slack.disable").exists()) {
							SlackService.instance().startCheckingProfile();
						}
					}
					if (Site.SKYPE) {
						if (!new File("skype.disable").exists()) {
							//SkypeService.instance().startCheckingProfile();
						}
					}
					if (Site.WECHAT) {
						if (!new File("wechat.disable").exists()) {
							//WeChatService.instance().startCheckingProfile();
						}
					}
					if (Site.KIK) {
						if (!new File("kik.disable").exists()) {
							//KikService.instance().startCheckingProfile();
						}
					}
					if (Site.EMAIL) {
						if (!new File("email.disable").exists()) {
							EmailService.instance().startCheckingEmail();
						}
					}
					if (Site.TIMERS) {
						if (!new File("timers.disable").exists()) {
							TimerService.instance().startCheckingProfile();
						}
					}
					if (Site.FORGET) {
						ForgetfulnessService.instance().startChecking();
					}
					if (Site.ADMIN) {
						if (!new File("admin.disable").exists()) {
							AdminService.instance().startChecking();
						}
					}
					try {
						BotInstance cache = validate(BotInstance.class, "cache", "admin", new LoginBean().getDomain());
						if (cache != null) {
							Bot.systemCache = Bot.createInstance(Bot.CONFIG_FILE, cache.getDatabaseName(), cache.isSchema());
							Bot.systemCache.setName(cache.getName());
							if (cache.isAdult()) {
								Bot.systemCache.setFilterProfanity(false);
							}
						}
					} catch (Exception missing) {}
					try {
						AdminDatabase.instance().log(Level.INFO, "Checking python database");
						String result = "";
						// setting up database, send xml to python with user and pass.
						String host = obfuscate(Site.PERSISTENCE_HOST);
						String dbname = obfuscate(Site.PERSISTENCE_UNIT);
						String port = obfuscate(Site.PERSISTENCE_PORT);
						String user = obfuscate(Site.DATABASEUSER);
						String password = null;
						if (Site.OBFUSCATE_DATABASEPASSWORD == null || Site.OBFUSCATE_DATABASEPASSWORD.isEmpty()) {
							password = obfuscate(Site.DATABASEPASSWORD);
						} else {
							password = Site.OBFUSCATE_DATABASEPASSWORD;
						}
						
						StringWriter writer = new StringWriter();
						writer.write("<database host=\"");
						writer.write(host);
						writer.write("\" dbname=\"");
						writer.write(dbname);
						writer.write("\" port=\"");
						writer.write(port);
						writer.write("\" user=\"");
						writer.write(user);
						writer.write("\" password=\"");
						writer.write(password);
						writer.write("\" />");
						
						MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
						multipartEntity.addPart("xml", new StringBody(writer.toString(), "text/xml", Charset.defaultCharset()));

						HttpClient httpclient = new DefaultHttpClient();
						HttpResponse response = null;

						HttpPost httppost = new HttpPost(Site.PYTHONSERVER + "setup-database");

						httppost.setEntity(multipartEntity);
						response = httpclient.execute(httppost);

						HttpEntity entity = response.getEntity();
						if (entity != null) {
							result = EntityUtils.toString(entity, HTTP.UTF_8);
						}

						if ((response.getStatusLine().getStatusCode() != 200)
								&& (response.getStatusLine().getStatusCode() != 204)) {
							Exception exception = new Exception("" + response.getStatusLine().getStatusCode() + " : " + result);
							throw exception;
						}
					} catch (Exception exception) {}
				}
			}
		}
		return this.factory;
	}
	
	private String obfuscate(String obfuscate) {
		byte[] strToteArray = obfuscate.getBytes();
		byte[] result = new byte[strToteArray.length];
		// Store result in reverse order into the
		// result byte[]
		for (int i = 0; i < strToteArray.length; i++)
			result[i] = strToteArray[strToteArray.length - i - 1];

		String wordReversed = new String(result);

		char[] chars = wordReversed.toCharArray();

		StringBuffer hex = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			hex.append(Integer.toHexString((int) chars[i]));
		}
		return hex.toString();
	}
	
	@SuppressWarnings("rawtypes")
	public int getAllInstancesCount(String categoryFilter, String nameFilter, String viewUser, InstanceFilter filter, InstanceRestrict restrict, 
			InstanceSort sort, ContentRating content, String tagFilter, User user, Domain domain) {
		log(Level.FINE, "count all instances");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaQuery criteria = buildSearchQuery(BotInstance.class, em, true, categoryFilter, nameFilter, viewUser, filter, restrict, sort, content, tagFilter, user, domain, false);
			Query query = em.createQuery(criteria);
			if (criteria == null) {
				return 0;
			}
			return ((Number)query.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}
	
	/**
	 * Search and delete junk content such as empty scripts, graphics and avatars.
	 */
	public void cleanupJunk() {
		log(Level.INFO, "cleanup");
		EntityManager em =  getFactory().createEntityManager();
		try {
			List<Script> scripts = em.createQuery("Select s from Script s where s.isExternal = false and s.size = 0").getResultList();
			try {
				for (Script script : scripts) {
					if (script.getSourceCode() == null || script.getSourceCode().isEmpty()) {
						boolean empty = true;
						for (ScriptSource source : getAllScriptsVersions(script)) {
							if (source.getSource() != null && !source.getSource().isEmpty()) {
								empty = false;
							}
						}
						if (empty) {
							log(Level.INFO, "cleanup", script);
							delete(script);
						}
					}
				}
			} catch (Exception exception) {
				AdminDatabase.instance().log(Level.INFO, "Cleanup exception");
				AdminDatabase.instance().log(exception);
			}
			List<Graphic> graphics = em.createQuery("Select s from Graphic s where s.isExternal = false and s.media is null").getResultList();
			try {
				for (Graphic graphic : graphics) {
					log(Level.INFO, "cleanup", graphic);
					delete(graphic);
				}
			} catch (Exception exception) {
				AdminDatabase.instance().log(Level.INFO, "Cleanup exception");
				AdminDatabase.instance().log(exception);
			}
			List<Avatar> avatars = em.createQuery("Select s from Avatar s where s.isExternal = false and s.background is null and s.avatar is null").getResultList();
			try {
				for (Avatar avatar : avatars) {
					if (avatar.getMedia().isEmpty()) {
						log(Level.INFO, "cleanup", avatar);
						delete(avatar);
					}
				}
			} catch (Exception exception) {
				AdminDatabase.instance().log(Level.INFO, "Cleanup exception");
				AdminDatabase.instance().log(exception);
			}
			List<Forum> forums = em.createQuery("Select s from Forum s where s.isExternal = false and s.posts = 0").getResultList();
			try {
				for (Forum forum : forums) {
					if (AdminDatabase.instance().getAllForumPostsCount(forum, 0, 100, "", "", null, null, "", "", null, null) == 0) {
						log(Level.INFO, "cleanup", forum);
						delete(forum);
					}
				}
			} catch (Exception exception) {
				AdminDatabase.instance().log(Level.INFO, "Cleanup exception");
				AdminDatabase.instance().log(exception);
			}
		} finally {
			em.close();
		}
	}
	
	/**
	 * Daily reset is run nightly to perform maintenance tasks.
	 * This includes recording daily stats, and deleting old messages, attachments, and translations if over max size limits.
	 */
	public void checkDailyReset() {
		if ((this.lastDateCheck == null) || (this.lastDateCheck.get(Calendar.DAY_OF_YEAR) != (Calendar.getInstance().get(Calendar.DAY_OF_YEAR)))) {
			EntityManager em =  getFactory().createEntityManager();
			try {
				log(Level.INFO, "daily reset");
				resetTemporaryApplicationId();
				Stats.stats.totalMemory = Runtime.getRuntime().totalMemory();
				Stats.stats.freeMemory = Runtime.getRuntime().freeMemory();
				this.lastDateCheck = Calendar.getInstance();
				// Reset stats.
				em.getTransaction().begin();
				try {
					int count = ((Number)em.createQuery("Select count(b) from BotInstance b").getSingleResult()).intValue();
					Stats.stats.bots = count;
					count = ((Number)em.createQuery("Select count(b) from BotInstance b where b.archived = false").getSingleResult()).intValue();
					Stats.stats.activeBots = count;
					count = ((Number)em.createQuery("Select count(b) from BotInstance b where b.enableFacebook = true").getSingleResult()).intValue();
					Stats.stats.facebookBots = count;
					count = ((Number)em.createQuery("Select count(b) from BotInstance b where b.enableTelegram = true").getSingleResult()).intValue();
					Stats.stats.telegramBots = count;
					count = ((Number)em.createQuery("Select count(b) from BotInstance b where b.enableSlack = true").getSingleResult()).intValue();
					Stats.stats.slackBots = count;
					count = ((Number)em.createQuery("Select count(b) from BotInstance b where b.enableSkype = true").getSingleResult()).intValue();
					Stats.stats.skypeBots = count;
					count = ((Number)em.createQuery("Select count(b) from BotInstance b where b.enableWeChat = true").getSingleResult()).intValue();
					Stats.stats.wechatBots = count;
					count = ((Number)em.createQuery("Select count(b) from BotInstance b where b.enableKik = true").getSingleResult()).intValue();
					Stats.stats.kikBots = count;
					count = ((Number)em.createQuery("Select count(b) from BotInstance b where b.enableTwitter = true").getSingleResult()).intValue();
					Stats.stats.twitterBots = count;
					count = ((Number)em.createQuery("Select count(b) from BotInstance b where b.enableEmail = true").getSingleResult()).intValue();
					Stats.stats.emailBots = count;
					count = ((Number)em.createQuery("Select count(b) from BotInstance b where b.enableAlexa = true").getSingleResult()).intValue();
					Stats.stats.alexaBots = count;
					count = ((Number)em.createQuery("Select count(b) from BotInstance b where b.enableGoogleAssistant = true").getSingleResult()).intValue();
					Stats.stats.googleAssistantBots = count;
					em.persist(Stats.stats);
					for (AppIDStats stat : new HashMap<String, AppIDStats>(AppIDStats.stats).values()) {
						em.persist(stat);
					}
					for (IPStats stat : new HashMap<String, IPStats>(IPStats.stats).values()) {
						em.persist(stat);
					}
					for (PageStats stat : new HashMap<String, PageStats>(PageStats.stats).values()) {
						em.persist(stat);
					}
					for (BotStats stat : new HashMap<Long, BotStats>(BotStats.stats).values()) {
						em.persist(stat);
					}
					for (LiveChatStats stat : new HashMap<Long, LiveChatStats>(LiveChatStats.stats).values()) {
						em.persist(stat);
					}
					for (ReferrerStats stat : new HashMap<String, ReferrerStats>(ReferrerStats.stats).values()) {
						em.persist(stat);
					}
					for (ErrorStats stat : new HashMap<String, ErrorStats>(ErrorStats.stats).values()) {
						em.persist(stat);
					}
					em.getTransaction().commit();
					
					em.getTransaction().begin();
					long rows = ((Number)em.createQuery("Select count(s) from AppIDStats s").getSingleResult()).longValue();
					if (rows > Stats.MAX_ROWS) {
						long trim = rows - Stats.MAX_ROWS;
						em.createNativeQuery("delete from appidstats where id in (select id from appidstats order by date limit " + trim + ")").executeUpdate();
					}
					rows = ((Number)em.createQuery("Select count(s) from IPStats s").getSingleResult()).longValue();
					if (rows > Stats.MAX_ROWS) {
						long trim = rows - Stats.MAX_ROWS;
						em.createNativeQuery("delete from ipstats where id in (select id from ipstats order by date limit " + trim + ")").executeUpdate();
					}
					rows = ((Number)em.createQuery("Select count(s) from PageStats s").getSingleResult()).longValue();
					if (rows > Stats.MAX_ROWS) {
						long trim = rows - Stats.MAX_ROWS;
						em.createNativeQuery("delete from PageStats where id in (select id from PageStats order by date limit " + trim + ")").executeUpdate();
					}
					rows = ((Number)em.createQuery("Select count(s) from BotStats s").getSingleResult()).longValue();
					if (rows > Stats.MAX_ROWS) {
						long trim = rows - Stats.MAX_ROWS;
						em.createNativeQuery("delete from BotStats where id in (select id from BotStats order by date limit " + trim + ")").executeUpdate();
					}
					rows = ((Number)em.createQuery("Select count(s) from ReferrerStats s").getSingleResult()).longValue();
					if (rows > Stats.MAX_ROWS) {
						long trim = rows - Stats.MAX_ROWS;
						em.createNativeQuery("delete from ReferrerStats where id in (select id from ReferrerStats order by date limit " + trim + ")").executeUpdate();
					}
					rows = ((Number)em.createQuery("Select count(s) from ErrorStats s").getSingleResult()).longValue();
					if (rows > Stats.MAX_ROWS) {
						long trim = rows - Stats.MAX_ROWS;
						em.createNativeQuery("delete from ErrorStats where id in (select id from ErrorStats order by date limit " + trim + ")").executeUpdate();
					}
					em.getTransaction().commit();
				} catch (Exception exception) {
					AdminDatabase.instance().log(Level.INFO, "Statistics exception");
					AdminDatabase.instance().log(exception);
					if (em.getTransaction().isActive()) {
						em.getTransaction().rollback();
					}
				}
				// Cleanup live chat/attachments.
				em.clear();
				em.getTransaction().begin();
				try {
					long count = ((Number)em.createQuery("Select count(c) from ChatMessage c").getSingleResult()).longValue();
					if (count > Site.MAX_LIVECHAT_MESSAGES) {
						java.sql.Date lastYear = new java.sql.Date(System.currentTimeMillis() - Utils.YEAR);
						log(Level.INFO, "deleting old chat messages", lastYear);
						em.createNativeQuery("delete from chatmessage where creationdate < '" + lastYear + "'").executeUpdate();
						em.getTransaction().commit();
						em.getTransaction().begin();
						List<Object[]> rows = em.createNativeQuery("select c.channel_id, count(c) from chatmessage c group by c.channel_id having count(c) > " + Site.MAX_LIVECHAT_MESSAGES).getResultList();
						for (Object[] row : rows) {
							long trim = ((Number)row[1]).intValue() - Site.MAX_LIVECHAT_MESSAGES;
							log(Level.INFO, "deleting old chat messages", row[0], trim);
							em.createNativeQuery("delete from chatmessage where id in (select id from chatmessage where channel_id = " + ((Number)row[0]).longValue() + " order by creationdate limit " + trim + ")").executeUpdate();
						}
						em.getTransaction().commit();
						em.getTransaction().begin();
					}
					count = ((Number)em.createQuery("Select count(c) from ChannelAttachment c").getSingleResult()).longValue();
					if (count > Site.MAX_ATTACHMENTS) {
						java.sql.Date lastYear = new java.sql.Date(System.currentTimeMillis() - Utils.YEAR);
						log(Level.INFO, "deleting old chat attachments", lastYear);
						em.createNativeQuery("delete from channelattachment where creationdate < '" + lastYear + "'").executeUpdate();
						em.getTransaction().commit();
						em.getTransaction().begin();
						List<Object[]> rows = em.createNativeQuery("select c.channel_id, count(c) from channelattachment c group by c.channel_id having count(c) > " + Site.MAX_ATTACHMENTS).getResultList();
						for (Object[] row : rows) {
							long trim = ((Number)row[1]).intValue() - Site.MAX_ATTACHMENTS;
							log(Level.INFO, "deleting old chat attachments", row[0], trim);
							em.createNativeQuery("delete from channelattachment where mediaId in (select mediaId from channelattachment where channel_id = " + ((Number)row[0]).longValue() + " order by creationdate limit " + trim + ")").executeUpdate();
						}
						em.getTransaction().commit();
						em.getTransaction().begin();
					}
					count = ((Number)em.createQuery("Select count(c) from BotAttachment c").getSingleResult()).longValue();
					if (count > Site.MAX_ATTACHMENTS) {
						java.sql.Date lastYear = new java.sql.Date(System.currentTimeMillis() - Utils.YEAR);
						log(Level.INFO, "deleting old bot attachments", lastYear);
						em.createNativeQuery("delete from botattachment where creationdate < '" + lastYear + "'").executeUpdate();
						em.getTransaction().commit();
						em.getTransaction().begin();
						List<Object[]> rows = em.createNativeQuery("select c.bot_id, count(c) from botattachment c group by c.bot_id having count(c) > " + Site.MAX_ATTACHMENTS).getResultList();
						for (Object[] row : rows) {
							long trim = ((Number)row[1]).intValue() - Site.MAX_ATTACHMENTS;
							log(Level.INFO, "deleting old bot attachments", row[0], trim);
							em.createNativeQuery("delete from botattachment where mediaId in (select mediaId from botattachment where bot_id = " + ((Number)row[0]).longValue() + " order by creationdate limit " + trim + ")").executeUpdate();
						}
						em.getTransaction().commit();
						em.getTransaction().begin();
					}
					// Don't delete.
					/**count = ((Number)em.createQuery("Select count(c) from IssueTrackerAttachment c").getSingleResult()).longValue();
					if (count > Site.MAX_ATTACHMENTS) {
						java.sql.Date lastYear = new java.sql.Date(System.currentTimeMillis() - Utils.YEAR);
						log(Level.INFO, "deleting old tracker attachments", lastYear);
						em.createNativeQuery("delete from issuetrackerattachment where creationdate < '" + lastYear + "'").executeUpdate();
						em.getTransaction().commit();
						em.getTransaction().begin();
						List<Object[]> rows = em.createNativeQuery("select c.tracker_id, count(c) from issuetrackerattachment c group by c.tracker_id having count(c) > " + Site.MAX_ATTACHMENTS).getResultList();
						for (Object[] row : rows) {
							long trim = ((Number)row[1]).intValue() - Site.MAX_ATTACHMENTS;
							log(Level.INFO, "deleting old tracker attachments", row[0], trim);
							em.createNativeQuery("delete from issuetrackerattachment where mediaId in (select mediaId from issuetrackerattachment where tracker_id = " + ((Number)row[0]).longValue() + " order by creationdate limit " + trim + ")").executeUpdate();
						}
						em.getTransaction().commit();
						em.getTransaction().begin();
					}*/
					em.getTransaction().commit();
				} catch (Exception exception) {
					AdminDatabase.instance().log(Level.INFO, "Cleanup exception");
					AdminDatabase.instance().log(exception);
					if (em.getTransaction().isActive()) {
						em.getTransaction().rollback();
					}
				}
				// Cleanup translations.
				em.clear();
				em.getTransaction().begin();
				try {
					long count = ((Number)em.createQuery("Select count(c) from BotTranslation c").getSingleResult()).longValue();
					if (count > Site.MAX_TRANSLATIONS) {
						long trim = count - Site.MAX_TRANSLATIONS;
						log(Level.INFO, "deleting old translations", trim);
						trim = Math.min(trim, 100000);
						em.createNativeQuery("delete from bottranslation where text in (select text from bottranslation where creationdate is null limit " + trim + ")").executeUpdate();
						em.createNativeQuery("delete from bottranslation where creationdate in (select creationdate from bottranslation order by creationdate limit " + trim + ")").executeUpdate();
					}
					em.getTransaction().commit();
				} catch (Exception exception) {
					AdminDatabase.instance().log(Level.INFO, "Translations exception");
					AdminDatabase.instance().log(exception);
					if (em.getTransaction().isActive()) {
						em.getTransaction().rollback();
					}
				}
				// Email summaries.
				em.clear();
				try {
					emailExpiredDomains();
				} catch (Exception exception) {
					AdminDatabase.instance().log(Level.INFO, "Email expired exception");
					AdminDatabase.instance().log(exception);
				}
				try {
					if (this.lastDateCheck.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
						weeklySummaryEmail();
					}
				} catch (Exception exception) {
					AdminDatabase.instance().log(Level.INFO, "Weekly summary exception");
					AdminDatabase.instance().log(exception);
				}
				// Reset connects.
				em.getTransaction().begin();
				try {
					em.createQuery("Update BotInstance set dailyConnects = 0, restDailyConnects = 0").executeUpdate();
					em.createQuery("Update Forum set dailyConnects = 0, restDailyConnects = 0").executeUpdate();
					em.createQuery("Update ChatChannel set dailyConnects = 0, restDailyConnects = 0").executeUpdate();
					em.createQuery("Update Domain set dailyConnects = 0, restDailyConnects = 0").executeUpdate();
					em.createQuery("Update ForumPost set dailyViews = 0, restDailyViews = 0").executeUpdate();
					em.createQuery("Update Avatar set dailyConnects = 0, restDailyConnects = 0").executeUpdate();
					em.createQuery("Update Script set dailyConnects = 0, restDailyConnects = 0").executeUpdate();
					em.createQuery("Update Graphic set dailyConnects = 0, restDailyConnects = 0").executeUpdate();
					em.createQuery("Update IssueTracker set dailyConnects = 0, restDailyConnects = 0").executeUpdate();
					if (this.lastDateCheck.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
						em.createQuery("Update BotInstance set weeklyConnects = 0, restWeeklyConnects = 0").executeUpdate();
						em.createQuery("Update Forum set weeklyConnects = 0, restWeeklyConnects = 0, weeklyPosts = 0").executeUpdate();
						em.createQuery("Update ChatChannel set weeklyConnects = 0, restWeeklyConnects = 0").executeUpdate();
						em.createQuery("Update Domain set weeklyConnects = 0, restWeeklyConnects = 0").executeUpdate();
						em.createQuery("Update ForumPost set weeklyViews = 0, restWeeklyViews = 0").executeUpdate();
						em.createQuery("Update Avatar set weeklyConnects = 0, restWeeklyConnects = 0").executeUpdate();
						em.createQuery("Update Script set weeklyConnects = 0, restWeeklyConnects = 0").executeUpdate();
						em.createQuery("Update Graphic set weeklyConnects = 0, restWeeklyConnects = 0").executeUpdate();
						em.createQuery("Update IssueTracker set weeklyConnects = 0, restWeeklyConnects = 0").executeUpdate();
					}
					if (this.lastDateCheck.get(Calendar.DAY_OF_MONTH) == 1) {
						em.createQuery("Update BotInstance set monthlyConnects = 0, restMonthlyConnects = 0").executeUpdate();
						em.createQuery("Update Forum set monthlyConnects = 0, restMonthlyConnects = 0").executeUpdate();
						em.createQuery("Update ChatChannel set monthlyConnects = 0, restMonthlyConnects = 0").executeUpdate();
						em.createQuery("Update Domain set monthlyConnects = 0, restMonthlyConnects = 0").executeUpdate();
						em.createQuery("Update ForumPost set monthlyViews = 0, restMonthlyViews = 0").executeUpdate();
						em.createQuery("Update Avatar set monthlyConnects = 0, restMonthlyConnects = 0").executeUpdate();
						em.createQuery("Update Script set monthlyConnects = 0, restMonthlyConnects = 0").executeUpdate();
						em.createQuery("Update Graphic set monthlyConnects = 0, restMonthlyConnects = 0").executeUpdate();
						em.createQuery("Update IssueTracker set monthlyConnects = 0, restMonthlyConnects = 0").executeUpdate();
					}
					em.getTransaction().commit();
				} catch (Exception exception) {
					AdminDatabase.instance().log(Level.INFO, "Reset exception");
					AdminDatabase.instance().log(exception);
					if (em.getTransaction().isActive()) {
						em.getTransaction().rollback();
					}
				}
				em.clear();
				getFactory().getCache().evict(BotInstance.class);
				getFactory().getCache().evict(Forum.class);
				getFactory().getCache().evict(ForumPost.class);
				getFactory().getCache().evict(ChatChannel.class);
				getFactory().getCache().evict(Avatar.class);
				getFactory().getCache().evict(Script.class);
				getFactory().getCache().evict(IssueTracker.class);
				getFactory().getCache().evict(Domain.class);
			} finally {
				EmailService.reset();
				Stats.reset();
				AppIDStats.reset();
				BotStats.reset();
				LiveChatStats.reset();
				IPStats.reset();
				PageStats.reset();
				ReferrerStats.reset();
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				em.close();
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<BotInstance> getAllInstances(int page, int pageSize, String categoryFilter, String nameFilter, String viewUser,
				InstanceFilter filter, InstanceRestrict restrict, InstanceSort sort, ContentRating content, String tagFilter, User user, Domain domain, boolean includeDefaultDomain) {
		log(Level.FINE, "all instances");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaQuery criteria = buildSearchQuery(BotInstance.class, em, false, categoryFilter, nameFilter, viewUser, filter, restrict, sort, content, tagFilter, user, domain, includeDefaultDomain);
			if (criteria == null) {
				return new ArrayList<BotInstance>();
			}
			Query query = em.createQuery(criteria);
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public CriteriaQuery buildSearchQuery(Class type, EntityManager em, boolean count, String categoryFilter, String nameFilter, String viewUser,
				InstanceFilter filter, InstanceRestrict restrict, InstanceSort sort, ContentRating content, String tagFilter, User user, Domain domain, boolean includeDefaultDomain) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery criteria = cb.createQuery();
		Root root = criteria.from(type);
		buildSearchCriteria(cb, criteria, root, count, sort);
		Predicate where = buildSearchWhere(cb, criteria, root, categoryFilter, nameFilter, viewUser, filter, restrict, content, tagFilter, user, domain, includeDefaultDomain);
		if (where == null) {
			return null;
		}
		if (sort == InstanceSort.Rank) {
			where = cb.and(where, cb.equal(root.get("isFlagged"), false));
		}
		criteria.where(where);
		return criteria;
	}
	
	@SuppressWarnings("rawtypes")
	public void buildSearchCriteria(CriteriaBuilder cb, CriteriaQuery criteria, Root root, boolean count, InstanceSort sort) {
		criteria.distinct(true);
		if (!count) {
			if (sort == InstanceSort.Connects) {
				criteria.orderBy(cb.desc(root.get("connects")), cb.asc(root.get("name")));
			} else if (sort == InstanceSort.DailyConnects) {
				criteria.orderBy(cb.desc(root.get("dailyConnects")), cb.desc(root.get("weeklyConnects")), cb.asc(root.get("name")));
			} else if (sort == InstanceSort.WeeklyConnects) {
				criteria.orderBy(cb.desc(root.get("weeklyConnects")), cb.desc(root.get("monthlyConnects")), cb.asc(root.get("name")));
			} else if (sort == InstanceSort.MonthlyConnects) {
				criteria.orderBy(cb.desc(root.get("monthlyConnects")), cb.desc(root.get("connects")), cb.asc(root.get("name")));
			} else if (sort == InstanceSort.Date) {
				criteria.orderBy(cb.desc(root.get("creationDate")), cb.asc(root.get("name")));
			} else if (sort == InstanceSort.Size) {
				criteria.orderBy(cb.desc(root.get("memorySize")), cb.asc(root.get("name")));
			} else if (sort == InstanceSort.Rank) {
				criteria.orderBy(cb.desc(root.get("rank")), cb.desc(root.get("wins")), cb.desc(root.get("connects")));
			} else if (sort == InstanceSort.Wins) {
				criteria.orderBy(cb.desc(root.get("wins")), cb.desc(root.get("connects")));
			} else if (sort == InstanceSort.Losses) {
				criteria.orderBy(cb.desc(root.get("losses")), cb.desc(root.get("connects")));
			} else if (sort == InstanceSort.Posts) {
				criteria.orderBy(cb.desc(root.get("posts")), cb.asc(root.get("name")));
			} else if (sort == InstanceSort.Messages) {
				criteria.orderBy(cb.desc(root.get("messages")), cb.asc(root.get("name")));
			} else if (sort == InstanceSort.Users) {
				criteria.orderBy(cb.desc(root.get("connectedUsersCount")), cb.asc(root.get("name")));
			} else if (sort == InstanceSort.LastConnect) {
				criteria.orderBy(cb.desc(root.get("lastConnected")), cb.asc(root.get("name")));
			} else if (sort == InstanceSort.ThumbsUp) {
				criteria.orderBy(cb.desc(root.get("thumbsUp")), cb.desc(root.get("stars")), cb.desc(root.get("connects")));
			} else if (sort == InstanceSort.ThumbsDown) {
				criteria.orderBy(cb.desc(root.get("thumbsDown")), cb.asc(root.get("stars")), cb.asc(root.get("name")));
			} else if (sort == InstanceSort.Stars) {
				criteria.orderBy(cb.desc(root.get("stars")), cb.desc(root.get("thumbsUp")), cb.desc(root.get("connects")));
			} else {
				criteria.orderBy(cb.asc(root.get("name")));
			}
		} else {
			criteria.select(cb.count(root));
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Predicate buildSearchWhere(CriteriaBuilder cb, CriteriaQuery criteria, Root root, String categoryFilter, String nameFilter, String viewUser,
				InstanceFilter filter, InstanceRestrict restrict, ContentRating content, String tagFilter, User user, Domain domain, boolean includeDefaultDomain) {
		Predicate where = cb.conjunction();
		// Tags
		if (!tagFilter.trim().isEmpty()) {
			Path path = root.join("tags");
			where = cb.and(where, (cb.lower(path.get("name")).in(Utils.csv(tagFilter.toLowerCase()))));
		}
		// Categories
		if (!categoryFilter.trim().isEmpty()) {
			Path path = root.join("categories");
			where = cb.and(where, (cb.lower(path.get("name")).in(Utils.csv(categoryFilter.toLowerCase()))));
		}
		// Private vs public
		if (filter == InstanceFilter.Private) {
			if (user == null) {
				return null;
			}
			if (user.isSuperUser()) {
				where = cb.and(where, (cb.equal(root.get("isPrivate"), true)));
			} else {
				criteria.distinct(true);
				Path admins = root.join("admins", JoinType.LEFT);
				Path users = root.join("users", JoinType.LEFT);
				where = cb.and(where, (cb.equal(root.get("isPrivate"), true)));
				where = cb.and(where, cb.or(cb.equal(admins.get("userId"), user.getUserId()), cb.equal(users.get("userId"), user.getUserId())));
			}
		} else if (filter == InstanceFilter.Adult) {
			if ((user == null) || !user.isOver18()) {
				return null;
			}
			where = cb.and(where, (cb.equal(root.get("isPrivate"), false)));
			where = cb.and(where, (cb.equal(root.get("isHidden"), false)));
			where = cb.and(where, (cb.equal(root.get("isAdult"), true)));
		} else if (filter == InstanceFilter.Featured) {
			where = cb.and(where, (cb.equal(root.get("isFeatured"), true)));
			where = cb.and(where, (cb.equal(root.get("isPrivate"), false)));
			where = cb.and(where, (cb.equal(root.get("isHidden"), false)));
		} else if (filter == InstanceFilter.Personal) {
			if (viewUser == null) {
				if (user == null) {
					return null;
				}
				viewUser = user.getUserId();
			}
			if ((user == null) || (!user.getUserId().equals(viewUser) && !user.isSuperUser())) {
				where = cb.and(where, (cb.equal(root.get("isPrivate"), false)));
				where = cb.and(where, (cb.equal(root.get("isHidden"), false)));
			} else {
				content = null;
			}
			Path path = root.join("admins");
			where = cb.and(where, (cb.equal(path.get("userId"), viewUser)));
		} else  {
			where = cb.and(where, (cb.equal(root.get("isPrivate"), false)));
			if (user == null) {
				where = cb.and(where, (cb.equal(root.get("isHidden"), false)));
			} else {
				if (!user.isSuperUser()) {
					where = cb.and(where, cb.or(cb.equal(root.get("isHidden"), false), cb.equal(root.get("creator"), user)));
				}
			}
		}
		if (content == ContentRating.Everyone) {
			where = cb.and(where, (cb.equal(root.get("contentRating"), ContentRating.Everyone)));
		} else if (content == ContentRating.Teen ) {
			where = cb.and(where, cb.or(
					cb.equal(root.get("contentRating"), ContentRating.Teen), 
					cb.equal(root.get("contentRating"), ContentRating.Everyone),
					root.get("contentRating").isNull()));
		} else if (content == ContentRating.Mature ) {
			where = cb.and(where, cb.or(
					cb.equal(root.get("contentRating"), ContentRating.Teen),
					cb.equal(root.get("contentRating"), ContentRating.Everyone),
					cb.equal(root.get("contentRating"), ContentRating.Mature),
					root.get("contentRating").isNull()));
		} else if (content == ContentRating.Adult ) {
			where = cb.and(where, cb.or(
					cb.equal(root.get("contentRating"), ContentRating.Teen),
					cb.equal(root.get("contentRating"), ContentRating.Everyone),
					cb.equal(root.get("contentRating"), ContentRating.Mature),
					cb.equal(root.get("contentRating"), ContentRating.Adult),
					root.get("contentRating").isNull()));
		}
		if ((restrict != null) && restrict != InstanceRestrict.None) {
			if (restrict == InstanceRestrict.Ad) {
				where = cb.and(where, cb.isNotNull(root.get("adCode")));
				where = cb.and(where, cb.notEqual(root.get("adCode"), ""));
			} else if (restrict == InstanceRestrict.AdUnverified) {
				where = cb.and(where, cb.equal(root.get("adCodeVerified"), false));
				where = cb.and(where, cb.isNotNull(root.get("adCode")));
				where = cb.and(where, cb.notEqual(root.get("adCode"), ""));
			} else if (restrict == InstanceRestrict.Admin) {
				where = cb.and(where, cb.equal(root.get("creator").get("type"), UserType.Admin));
			} else if (restrict == InstanceRestrict.Partner) {
				where = cb.and(where, cb.equal(root.get("creator").get("type"), UserType.Partner));
			} else if (restrict == InstanceRestrict.Diamond) {
				where = cb.and(where, cb.equal(root.get("creator").get("type"), UserType.Diamond));
			} else if (restrict == InstanceRestrict.Platinum) {
				where = cb.and(where, cb.equal(root.get("creator").get("type"), UserType.Platinum));
			} else if (restrict == InstanceRestrict.Gold) {
				where = cb.and(where, cb.equal(root.get("creator").get("type"), UserType.Gold));
			} else if (restrict == InstanceRestrict.Bronze) {
				where = cb.and(where, cb.equal(root.get("creator").get("type"), UserType.Bronze));
			} else if (restrict == InstanceRestrict.Trial) {
				where = cb.and(where, cb.equal(root.get("accountType"), AccountType.Trial));
			} else if (restrict == InstanceRestrict.Basic) {
				where = cb.and(where, cb.equal(root.get("accountType"), AccountType.Basic));
			} else if (restrict == InstanceRestrict.Premium) {
				where = cb.and(where, cb.equal(root.get("accountType"), AccountType.Premium));
			} else if (restrict == InstanceRestrict.Professional) {
				where = cb.and(where, cb.equal(root.get("accountType"), AccountType.Professional));
			} else if (restrict == InstanceRestrict.Enterprise) {
				where = cb.and(where, cb.equal(root.get("accountType"), AccountType.Enterprise));
			} else if (restrict == InstanceRestrict.EnterprisePlus) {
				where = cb.and(where, cb.equal(root.get("accountType"), AccountType.EnterprisePlus));
			} else if (restrict == InstanceRestrict.Corporate) {
				where = cb.and(where, cb.equal(root.get("accountType"), AccountType.Corporate));
			} else if (restrict == InstanceRestrict.Dedicated) {
				where = cb.and(where, cb.equal(root.get("accountType"), AccountType.Dedicated));
			} else if (restrict == InstanceRestrict.Private) {
				where = cb.and(where, cb.equal(root.get("accountType"), AccountType.Private));
			} else if (restrict == InstanceRestrict.Forkable) {
				where = cb.and(where, cb.equal(root.get("allowForking"), true));
			} else if (restrict == InstanceRestrict.Link) {
				where = cb.and(where, cb.equal(root.get("isExternal"), true));
			} else if (restrict == InstanceRestrict.Website) {
				where = cb.and(where, cb.isNotNull(root.get("website")));
				where = cb.and(where, cb.notEqual(root.get("website"), ""));
			} else if (restrict == InstanceRestrict.Subdomain) {
				where = cb.and(where, cb.isNotNull(root.get("domainForwarder")));
			} else if (restrict == InstanceRestrict.Twitter) {
				where = cb.and(where, cb.equal(root.get("enableTwitter"), true));
			} else if (restrict == InstanceRestrict.Facebook) {
				where = cb.and(where, cb.equal(root.get("enableFacebook"), true));
			} else if (restrict == InstanceRestrict.Telegram) {
				where = cb.and(where, cb.equal(root.get("enableTelegram"), true));
			} else if (restrict == InstanceRestrict.Slack) {
				where = cb.and(where, cb.equal(root.get("enableSlack"), true));
			} else if (restrict == InstanceRestrict.Skype) {
				where = cb.and(where, cb.equal(root.get("enableSkype"), true));
			} else if (restrict == InstanceRestrict.WeChat) {
				where = cb.and(where, cb.equal(root.get("enableWeChat"), true));
			} else if (restrict == InstanceRestrict.Kik) {
				where = cb.and(where, cb.equal(root.get("enableKik"), true));
			} else if (restrict == InstanceRestrict.Timer) {
				where = cb.and(where, cb.equal(root.get("enableTimers"), true));
			} else if (restrict == InstanceRestrict.Email) {
				where = cb.and(where, cb.equal(root.get("enableEmail"), true));
			} else if (restrict == InstanceRestrict.Hidden) {
				where = cb.and(where, cb.equal(root.get("isHidden"), true));
			} else if (restrict == InstanceRestrict.Flagged) {
				where = cb.and(where, cb.equal(root.get("isFlagged"), true));
			} else if (restrict == InstanceRestrict.Icon) {
				where = cb.and(where, cb.isNotNull(root.get("avatar")));
			} else if (restrict == InstanceRestrict.Adult) {
				where = cb.and(where, cb.equal(root.get("contentRating"), ContentRating.Adult));
			} else if (restrict == InstanceRestrict.Schema) {
				where = cb.and(where, cb.equal(root.get("isSchema"), true));
			} else if (restrict == InstanceRestrict.Database) {
				where = cb.and(where, cb.equal(root.get("isSchema"), false));
			} else if (restrict == InstanceRestrict.Archived) {
				where = cb.and(where, cb.equal(root.get("archived"), true));
			}
		}
		if (filter == InstanceFilter.Template) {
			where = cb.and(where, (cb.equal(root.get("isTemplate"), true)));
		} else if ((user == null) || !user.isSuperUser()) {
			where = cb.and(where, (cb.equal(root.get("isTemplate"), false)));
		}
		if ((nameFilter != null) && !nameFilter.trim().isEmpty()) {
			where = cb.and(where, (cb.like(cb.lower(root.get("name")), "%" + nameFilter.trim().toLowerCase() + "%")));
		}
		if (domain != null && ((user == null) || ((filter != InstanceFilter.Personal) || !user.getUserId().equals(viewUser)))) {
			if (includeDefaultDomain) {
				where = cb.and(where, cb.or(cb.equal(root.get("domain"), getDefaultDomain().detach()), cb.equal(root.get("domain"), domain.detach())));
			} else {
				where = cb.and(where, cb.equal(root.get("domain"), domain.detach()));
			}
		}
		return where;
	}
	
	@SuppressWarnings("rawtypes")
	public int getAllForumsCount(String categoryFilter, String nameFilter, String viewUser, InstanceFilter filter, InstanceRestrict restrict, 
			InstanceSort sort, ContentRating content, String tagFilter, User user, Domain domain) {
		log(Level.FINE, "count all forums");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaQuery criteria = buildSearchQuery(Forum.class, em, true, categoryFilter, nameFilter, viewUser, filter, restrict, sort, content, tagFilter, user, domain, false);
			Query query = em.createQuery(criteria);
			if (criteria == null) {
				return 0;
			}
			return ((Number)query.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public int getAllIssueTrackersCount(String categoryFilter, String nameFilter, String viewUser, InstanceFilter filter, InstanceRestrict restrict, 
			InstanceSort sort, ContentRating content, String tagFilter, User user, Domain domain) {
		log(Level.FINE, "count all issuetracker");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaQuery criteria = buildSearchQuery(IssueTracker.class, em, true, categoryFilter, nameFilter, viewUser, filter, restrict, sort, content, tagFilter, user, domain, false);
			Query query = em.createQuery(criteria);
			if (criteria == null) {
				return 0;
			}
			return ((Number)query.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public int getAllDomainsCount(String categoryFilter, String nameFilter, String viewUser, InstanceFilter filter, InstanceRestrict restrict, 
			InstanceSort sort, ContentRating content, String tagFilter, User user) {
		log(Level.FINE, "count all domains");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaQuery criteria = buildSearchQuery(Domain.class, em, true, categoryFilter, nameFilter, viewUser, filter, restrict, sort, content, tagFilter, user, null, false);
			Query query = em.createQuery(criteria);
			query.setHint("eclipselink.read-only", "true");
			if (criteria == null) {
				return 0;
			}
			return ((Number)query.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public int getAllAvatarsCount(String categoryFilter, String nameFilter,
				String viewUser, InstanceFilter filter, InstanceRestrict restrict, InstanceSort sort, ContentRating content, String tagFilter, User user, Domain domain, boolean includeDefaultDomain) {
		log(Level.FINE, "count all avatars");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaQuery criteria = buildSearchQuery(Avatar.class, em, true, categoryFilter, nameFilter, viewUser, filter, restrict, sort, content, tagFilter, user, domain, includeDefaultDomain);
			Query query = em.createQuery(criteria);
			if (criteria == null) {
				return 0;
			}
			return ((Number)query.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public int getAllGraphicsCount(String categoryFilter, String nameFilter,
				String viewUser, InstanceFilter filter, InstanceRestrict restrict, InstanceSort sort, ContentRating content, String tagFilter, User user, Domain domain, boolean includeDefaultDomain) {
		log(Level.FINE, "count all graphics");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaQuery criteria = buildSearchQuery(Graphic.class, em, true, categoryFilter, nameFilter, viewUser, filter, restrict, sort, content, tagFilter, user, domain, includeDefaultDomain);
			Query query = em.createQuery(criteria);
			if (criteria == null) {
				return 0;
			}
			return ((Number)query.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public int getAllScriptsCount(String languageFilter, String categoryFilter, String nameFilter,
				String viewUser, InstanceFilter filter, InstanceRestrict restrict, InstanceSort sort, ContentRating content, String tagFilter, User user, Domain domain, boolean includeDeafaultDomain) {
		log(Level.FINE, "count all scripts");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(Script.class);
			buildSearchCriteria(cb, criteria, root, true, sort);
			Predicate where = buildSearchWhere(cb, criteria, root, categoryFilter, nameFilter, viewUser, filter, restrict, content, tagFilter, user, domain, includeDeafaultDomain);
			if (where == null) {
				return 0;
			}
			if ((languageFilter != null) && !languageFilter.trim().isEmpty()) {
				where = cb.and(where, cb.equal(root.get("language"), languageFilter.trim()));
			}
			criteria.where(where);
			Query query = em.createQuery(criteria);
			return ((Number)query.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<Forum> getAllForums(int page, int pageSize, String categoryFilter, String nameFilter, String viewUser, InstanceFilter filter, InstanceRestrict restrict,
				InstanceSort sort, ContentRating content, String tagFilter, User user, Domain domain) {
		log(Level.FINE, "all forums");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaQuery criteria = buildSearchQuery(Forum.class, em, false, categoryFilter, nameFilter, viewUser, filter, restrict, sort, content, tagFilter, user, domain, false);
			if (criteria == null) {
				return new ArrayList<Forum>();
			}
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<IssueTracker> getAllIssueTrackers(int page, int pageSize, String categoryFilter, String nameFilter, String viewUser, InstanceFilter filter, InstanceRestrict restrict,
				InstanceSort sort, ContentRating content, String tagFilter, User user, Domain domain) {
		log(Level.FINE, "all issuetracker");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaQuery criteria = buildSearchQuery(IssueTracker.class, em, false, categoryFilter, nameFilter, viewUser, filter, restrict, sort, content, tagFilter, user, domain, false);
			if (criteria == null) {
				return new ArrayList<IssueTracker>();
			}
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<Domain> getAllDomains(int page, int pageSize, String categoryFilter, String nameFilter, String viewUser, InstanceFilter filter, InstanceRestrict restrict,
				InstanceSort sort, ContentRating content, String tagFilter, User user) {
		log(Level.FINE, "all domain");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaQuery criteria = buildSearchQuery(Domain.class, em, false, categoryFilter, nameFilter, viewUser, filter, restrict, sort, content, tagFilter, user, null, false);
			if (criteria == null) {
				return new ArrayList<Domain>();
			}
			Query query = em.createQuery(criteria);
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<Avatar> getAllAvatars(int page, int pageSize, String categoryFilter, String nameFilter,
				String viewUser, InstanceFilter filter, InstanceRestrict restrict, InstanceSort sort, ContentRating content, String tagFilter, User user, Domain domain, boolean includeDefaultDomain) {
		log(Level.FINE, "all avatars");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaQuery criteria = buildSearchQuery(Avatar.class, em, false, categoryFilter, nameFilter, viewUser, filter, restrict, sort, content, tagFilter, user, domain, includeDefaultDomain);
			if (criteria == null) {
				return new ArrayList<Avatar>();
			}
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<Graphic> getAllGraphics(int page, int pageSize, String categoryFilter, String nameFilter,
				String viewUser, InstanceFilter filter, InstanceRestrict restrict, InstanceSort sort, ContentRating content, String tagFilter, User user, Domain domain, boolean includeDefaultDomain) {
		log(Level.FINE, "all graphics");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaQuery criteria = buildSearchQuery(Graphic.class, em, false, categoryFilter, nameFilter, viewUser, filter, restrict, sort, content, tagFilter, user, domain, includeDefaultDomain);
			if (criteria == null) {
				return new ArrayList<Graphic>();
			}
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<Script> getAllScripts(int page, int pageSize, String languageFilter, String categoryFilter, String nameFilter,
				String viewUser, InstanceFilter filter, InstanceRestrict restrict, InstanceSort sort, ContentRating content, String tagFilter, User user, Domain domain, boolean includeDefaultDomain) {
		log(Level.FINE, "all scripts");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(Script.class);
			buildSearchCriteria(cb, criteria, root, false, sort);
			Predicate where = buildSearchWhere(cb, criteria, root, categoryFilter, nameFilter, viewUser, filter, restrict, content, tagFilter, user, domain, includeDefaultDomain);
			if (where == null) {
				return new ArrayList<Script>();
			}
			if ((languageFilter != null) && !languageFilter.trim().isEmpty()) {
				where = cb.and(where, cb.equal(root.get("language"), languageFilter.trim()));
			}
			criteria.where(where);
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public int getAllChannelsCount(String categoryFilter, String nameFilter, String viewUser, InstanceFilter filter, InstanceRestrict restrict, 
				InstanceSort sort, ContentRating content, String tagFilter, User user, Domain domain) {
		log(Level.FINE, "count all channels");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaQuery criteria = buildSearchQuery(ChatChannel.class, em, true, categoryFilter, nameFilter, viewUser, filter, restrict, sort, content, tagFilter, user, domain, false);
			if (criteria == null) {
				return 0;
			}
			Query query = em.createQuery(criteria);
			return ((Number)query.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<ChatChannel> getAllChannels(int page, int pageSize, String categoryFilter, String nameFilter, String viewUser, InstanceFilter filter,
			InstanceRestrict restrict, InstanceSort sort, ContentRating content, String tagFilter, User user, Domain domain) {
		log(Level.FINE, "all channels");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaQuery criteria = buildSearchQuery(ChatChannel.class, em, false, categoryFilter, nameFilter, viewUser, filter, restrict, sort, content, tagFilter, user, domain, false);
			if (criteria == null) {
				return new ArrayList<ChatChannel>();
			}
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<ForumPost> getAllForumPosts(Forum forum, int page, int pageSize, String nameFilter, String viewUser,
				InstanceFilter filter, InstanceSort sort, String categoryFilter, String tagFilter, User user, Domain domain) {
		log(Level.FINE, "all forum posts");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(ForumPost.class);
			if (sort == InstanceSort.Connects) {
				criteria.orderBy(cb.desc(root.get("views")), cb.desc(root.get("creationDate")));
			} else if (sort == InstanceSort.DailyConnects) {
				criteria.orderBy(cb.desc(root.get("dailyViews")), cb.desc(root.get("creationDate")));
			} else if (sort == InstanceSort.WeeklyConnects) {
				criteria.orderBy(cb.desc(root.get("weeklyViews")), cb.desc(root.get("creationDate")));
			} else if (sort == InstanceSort.MonthlyConnects) {
				criteria.orderBy(cb.desc(root.get("monthlyViews")), cb.desc(root.get("creationDate")));
			} else if (sort == InstanceSort.Date) {
				criteria.orderBy(cb.desc(root.get("creationDate")));
			} else if (sort == InstanceSort.ThumbsUp) {
				criteria.orderBy(cb.desc(root.get("thumbsUp")));
			} else if (sort == InstanceSort.ThumbsDown) {
				criteria.orderBy(cb.desc(root.get("thumbsDown")));
			} else if (sort == InstanceSort.Stars) {
				criteria.orderBy(cb.desc(root.get("stars")));
			} else {
				criteria.orderBy(cb.asc(root.get("topic")), cb.asc(root.get("creationDate")));
			}
			Predicate where = cb.conjunction();
			if (filter == InstanceFilter.Personal) {
				if (viewUser == null) {
					if (user == null) {
						return new ArrayList<ForumPost>();
					}
					viewUser = user.getUserId();
				}
				Path path = root.join("creator");
				where = cb.and(where, (cb.equal(path, new User(viewUser))));
			} else if (filter == InstanceFilter.Featured) {
				where = cb.and(where, (cb.equal(root.get("isFeatured"), true)));
			}
			// Tags
			if (!tagFilter.trim().isEmpty()) {
				Path path = root.join("tags");
				where = cb.and(where, (path.get("name").in(Utils.csv(tagFilter))));
			}
			// Categories
			if (!categoryFilter.trim().isEmpty()) {
				Path path = root.join("forum").join("categories");
				where = cb.and(where, (path.get("name").in(Utils.csv(categoryFilter))));
			}
			if ((nameFilter != null) && !nameFilter.trim().isEmpty()) {
				where = cb.and(where, (cb.like(cb.lower(root.get("topic")), "%" + nameFilter.toLowerCase() + "%")));
			}
			if (forum != null) {
				where = cb.and(where, (cb.equal(root.get("forum"), forum.detach())));
			} else if (user == null || !(user.isSuperUser() || user.isAdminUser())) {
				where = cb.and(where, (cb.equal(root.get("forum").get("isPrivate"), false)));
				where = cb.and(where, (cb.equal(root.get("forum").get("isHidden"), false)));
				Predicate accessMode = (cb.equal(root.get("forum").get("accessMode"), AccessMode.Everyone));
				accessMode = cb.or(accessMode, (root.get("forum").get("accessMode").isNull()));
				if (user != null) {
					accessMode = cb.or(accessMode, (cb.equal(root.get("forum").get("accessMode"), AccessMode.Users)));
				}
				where = cb.and(where, accessMode);
				if (!Site.ADULT) {
					where = cb.and(where, (cb.equal(root.get("forum").get("isAdult"), false)));
				}
				if (user == null || !user.isSuperUser() || !domain.getAlias().equals(Site.DOMAIN)) {
					where = cb.and(where, cb.equal(root.get("domain"), domain.detach()));
				}
			}
			criteria.where(where);
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public int getAllForumPostsCount(Forum forum, int page, int pageSize, String nameFilter, String viewUser,
				InstanceFilter filter, InstanceSort sort, String categoryFilter, String tagFilter, User user, Domain domain) {
		log(Level.FINE, "all forum posts count");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(ForumPost.class);
			criteria.select(cb.count(root));
			Predicate where = cb.conjunction();
			if (filter == InstanceFilter.Personal) {
				if (viewUser == null) {
					if (user == null) {
						return 0;
					}
					viewUser = user.getUserId();
				}
				Path path = root.join("creator");
				where = cb.and(where, (cb.equal(path, new User(viewUser))));
			} else if (filter == InstanceFilter.Featured) {
				where = cb.and(where, (cb.equal(root.get("isFeatured"), true)));
			}
			// Tags
			if (!tagFilter.trim().isEmpty()) {
				Path path = root.join("tags");
				where = cb.and(where, (path.get("name").in(Utils.csv(tagFilter))));
			}
			// Categories
			if (!categoryFilter.trim().isEmpty()) {
				Path path = root.join("forum").join("categories");
				where = cb.and(where, (path.get("name").in(Utils.csv(categoryFilter))));
			}
			if ((nameFilter != null) && !nameFilter.trim().isEmpty()) {
				where = cb.and(where, (cb.like(cb.lower(root.get("topic")), "%" + nameFilter.toLowerCase() + "%")));
			}
			if (forum != null) {
				where = cb.and(where, (cb.equal(root.get("forum"), forum.detach())));
			} else if (user == null || !(user.isSuperUser() || user.isAdminUser())) {
				where = cb.and(where, (cb.equal(root.get("forum").get("isPrivate"), false)));
				where = cb.and(where, (cb.equal(root.get("forum").get("isHidden"), false)));
				Predicate accessMode = (cb.equal(root.get("forum").get("accessMode"), AccessMode.Everyone));
				accessMode = cb.or(accessMode, (root.get("forum").get("accessMode").isNull()));
				if (user != null) {
					accessMode = cb.or(accessMode, (cb.equal(root.get("forum").get("accessMode"), AccessMode.Users)));
				}
				where = cb.and(where, accessMode);
				if (!Site.ADULT) {
					where = cb.and(where, (cb.equal(root.get("forum").get("isAdult"), false)));
				}
				if (user == null || !user.isSuperUser() || !domain.getAlias().equals(Site.DOMAIN)) {
					where = cb.and(where, cb.equal(root.get("domain"), domain.detach()));
				}
			}
			criteria.where(where);
			Query query = em.createQuery(criteria);
			return ((Number)query.getSingleResult()).intValue();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<Issue> getAllIssues(IssueTracker tracker, int page, int pageSize, String nameFilter, String viewUser,
				InstanceFilter filter, InstanceSort sort, String categoryFilter, String tagFilter, User user, Domain domain) {
		log(Level.FINE, "all issues");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(Issue.class);
			if (sort == InstanceSort.Connects) {
				criteria.orderBy(cb.desc(root.get("views")), cb.desc(root.get("creationDate")));
			} else if (sort == InstanceSort.DailyConnects) {
				criteria.orderBy(cb.desc(root.get("dailyViews")), cb.desc(root.get("creationDate")));
			} else if (sort == InstanceSort.WeeklyConnects) {
				criteria.orderBy(cb.desc(root.get("weeklyViews")), cb.desc(root.get("creationDate")));
			} else if (sort == InstanceSort.MonthlyConnects) {
				criteria.orderBy(cb.desc(root.get("monthlyViews")), cb.desc(root.get("creationDate")));
			} else if (sort == InstanceSort.Date) {
				criteria.orderBy(cb.desc(root.get("creationDate")));
			} else if (sort == InstanceSort.ThumbsUp) {
				criteria.orderBy(cb.desc(root.get("thumbsUp")));
			} else if (sort == InstanceSort.ThumbsDown) {
				criteria.orderBy(cb.desc(root.get("thumbsDown")));
			} else if (sort == InstanceSort.Stars) {
				criteria.orderBy(cb.desc(root.get("stars")));
			} else {
				criteria.orderBy(cb.asc(root.get("title")), cb.asc(root.get("creationDate")));
			}
			Predicate where = cb.conjunction();
			if (filter == InstanceFilter.Personal) {
				if (viewUser == null) {
					if (user == null) {
						return new ArrayList<Issue>();
					}
					viewUser = user.getUserId();
				}
				Path path = root.join("creator");
				where = cb.and(where, (cb.equal(path, new User(viewUser))));
			} else if (filter == InstanceFilter.Featured) {
				where = cb.and(where, (cb.equal(root.get("isPriority"), true)));
			}
			// Tags
			if (!tagFilter.trim().isEmpty()) {
				Path path = root.join("tags");
				where = cb.and(where, (path.get("name").in(Utils.csv(tagFilter))));
			}
			// Categories
			if (!categoryFilter.trim().isEmpty()) {
				Path path = root.join("forum").join("categories");
				where = cb.and(where, (path.get("name").in(Utils.csv(categoryFilter))));
			}
			if ((nameFilter != null) && !nameFilter.trim().isEmpty()) {
				where = cb.and(where, (cb.like(cb.lower(root.get("title")), "%" + nameFilter.toLowerCase() + "%")));
			}
			if (tracker != null) {
				where = cb.and(where, (cb.equal(root.get("tracker"), tracker.detach())));
			} else if (user == null || !(user.isSuperUser() || user.isAdminUser())) {
				where = cb.and(where, (cb.equal(root.get("tracker").get("isPrivate"), false)));
				where = cb.and(where, (cb.equal(root.get("tracker").get("isHidden"), false)));
				Predicate accessMode = (cb.equal(root.get("tracker").get("accessMode"), AccessMode.Everyone));
				accessMode = cb.or(accessMode, (root.get("tracker").get("accessMode").isNull()));
				if (user != null) {
					accessMode = cb.or(accessMode, (cb.equal(root.get("tracker").get("accessMode"), AccessMode.Users)));
				}
				where = cb.and(where, accessMode);
				if (!Site.ADULT) {
					where = cb.and(where, (cb.equal(root.get("tracker").get("isAdult"), false)));
				}
				if (user == null || !user.isSuperUser() || !domain.getAlias().equals(Site.DOMAIN)) {
					where = cb.and(where, cb.equal(root.get("domain"), domain.detach()));
				}
			}
			if (user == null) {
				where = cb.and(where, (cb.equal(root.get("isHidden"), false)));
			} else if (!(tracker.isAdmin(user)) ){
				where = cb.and(where, cb.or((cb.equal(root.get("creator"), user.detach())),(cb.equal(root.get("isHidden"), false))));
			}
			criteria.where(where);
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public int getAllIssuesCount(IssueTracker tracker, int page, int pageSize, String nameFilter, String viewUser,
				InstanceFilter filter, InstanceSort sort, String categoryFilter, String tagFilter, User user, Domain domain) {
		log(Level.FINE, "all issues count");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(Issue.class);
			criteria.select(cb.count(root));
			Predicate where = cb.conjunction();
			if (filter == InstanceFilter.Personal) {
				if (viewUser == null) {
					if (user == null) {
						return 0;
					}
					viewUser = user.getUserId();
				}
				Path path = root.join("creator");
				where = cb.and(where, (cb.equal(path, new User(viewUser))));
			} else if (filter == InstanceFilter.Featured) {
				where = cb.and(where, (cb.equal(root.get("isPriority"), true)));
			}
			// Tags
			if (!tagFilter.trim().isEmpty()) {
				Path path = root.join("tags");
				where = cb.and(where, (path.get("name").in(Utils.csv(tagFilter))));
			}
			// Categories
			if (!categoryFilter.trim().isEmpty()) {
				Path path = root.join("forum").join("categories");
				where = cb.and(where, (path.get("name").in(Utils.csv(categoryFilter))));
			}
			if ((nameFilter != null) && !nameFilter.trim().isEmpty()) {
				where = cb.and(where, (cb.like(cb.lower(root.get("title")), "%" + nameFilter.toLowerCase() + "%")));
			}
			if (tracker != null) {
				where = cb.and(where, (cb.equal(root.get("tracker"), tracker.detach())));
			} else if (user == null || !(user.isSuperUser() || user.isAdminUser())) {
				where = cb.and(where, (cb.equal(root.get("tracker").get("isPrivate"), false)));
				where = cb.and(where, (cb.equal(root.get("tracker").get("isHidden"), false)));
				Predicate accessMode = (cb.equal(root.get("tracker").get("accessMode"), AccessMode.Everyone));
				accessMode = cb.or(accessMode, (root.get("tracker").get("accessMode").isNull()));
				if (user != null) {
					accessMode = cb.or(accessMode, (cb.equal(root.get("tracker").get("accessMode"), AccessMode.Users)));
				}
				where = cb.and(where, accessMode);
				if (!Site.ADULT) {
					where = cb.and(where, (cb.equal(root.get("tracker").get("isAdult"), false)));
				}
				if (user == null || !user.isSuperUser() || !domain.getAlias().equals(Site.DOMAIN)) {
					where = cb.and(where, cb.equal(root.get("domain"), domain.detach()));
				}
			}
			if (user == null || !(tracker.isAdmin(user))) {
				where = cb.and(where, (cb.equal(root.get("isHidden"), false)));
			}
			criteria.where(where);
			Query query = em.createQuery(criteria);
			return ((Number)query.getSingleResult()).intValue();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}

	@SuppressWarnings("rawtypes")
	public List<ChannelAttachment> getAllChannelAttachments(ChatChannel channel, int page, int pageSize, String nameFilter, Calendar duration, String viewUser, InstanceFilter filter, InstanceSort sort, User user, Domain domain) {
		log(Level.INFO, "all channel attachments");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(ChannelAttachment.class);
			criteria.orderBy(cb.asc(root.get("creationDate")));
			Predicate where = cb.conjunction();
			if (filter == InstanceFilter.Personal) {
				if (viewUser == null) {
					if (user == null) {
						return new ArrayList<ChannelAttachment>();
					}
					viewUser = user.getUserId();
				}
				Path path = root.join("creator");
				where = cb.and(where, (cb.equal(path, new User(viewUser))));
			}
			if ((nameFilter != null) && !nameFilter.trim().isEmpty()) {
				where = cb.and(where, (cb.like(cb.lower(root.get("name")), "%" + nameFilter.toLowerCase() + "%")));
			}
			if (channel != null) {
				where = cb.and(where, (cb.equal(root.get("channel"), channel.detach())));
			} else {
				where = cb.and(where, cb.equal(root.get("domain"), domain.detach()));
			}
			if (duration != null) {
				where = cb.and(where, (cb.greaterThanOrEqualTo(root.get("creationDate"), duration.getTime())));
			}
			criteria.where(where);
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}

	@SuppressWarnings("rawtypes")
	public List<BotAttachment> getAllBotAttachments(BotInstance instance, int page, int pageSize, Calendar duration, String viewUser, InstanceFilter filter, InstanceSort sort, User user, Domain domain) {
		log(Level.INFO, "all bot attachments");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(BotAttachment.class);
			criteria.orderBy(cb.asc(root.get("creationDate")));
			Predicate where = cb.conjunction();
			if (filter == InstanceFilter.Personal) {
				if (viewUser == null) {
					if (user == null) {
						return new ArrayList<BotAttachment>();
					}
					viewUser = user.getUserId();
				}
				Path path = root.join("creator");
				where = cb.and(where, (cb.equal(path, new User(viewUser))));
			}
			if (instance != null) {
				where = cb.and(where, (cb.equal(root.get("bot"), instance.detach())));
			} else {
				where = cb.and(where, cb.equal(root.get("domain"), domain.detach()));
			}
			if (duration != null) {
				where = cb.and(where, (cb.greaterThanOrEqualTo(root.get("creationDate"), duration.getTime())));
			}
			criteria.where(where);
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}

	@SuppressWarnings("rawtypes")
	public List<ForumAttachment> getAllForumAttachments(Forum instance, int page, int pageSize, Calendar duration, String viewUser, InstanceFilter filter, InstanceSort sort, User user, Domain domain) {
		log(Level.INFO, "all forum attachments");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(ForumAttachment.class);
			criteria.orderBy(cb.asc(root.get("creationDate")));
			Predicate where = cb.conjunction();
			if (filter == InstanceFilter.Personal) {
				if (viewUser == null) {
					if (user == null) {
						return new ArrayList<ForumAttachment>();
					}
					viewUser = user.getUserId();
				}
				Path path = root.join("creator");
				where = cb.and(where, (cb.equal(path, new User(viewUser))));
			}
			if (instance != null) {
				where = cb.and(where, (cb.equal(root.get("forum"), instance.detach())));
			} else {
				where = cb.and(where, cb.equal(root.get("domain"), domain.detach()));
			}
			if (duration != null) {
				where = cb.and(where, (cb.greaterThanOrEqualTo(root.get("creationDate"), duration.getTime())));
			}
			criteria.where(where);
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}

	@SuppressWarnings("rawtypes")
	public List<IssueTrackerAttachment> getAllIssueTrackerAttachments(IssueTracker instance, int page, int pageSize, Calendar duration, String viewUser, InstanceFilter filter, InstanceSort sort, User user, Domain domain) {
		log(Level.INFO, "all issue tracker attachments");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(IssueTrackerAttachment.class);
			criteria.orderBy(cb.asc(root.get("creationDate")));
			Predicate where = cb.conjunction();
			if (filter == InstanceFilter.Personal) {
				if (viewUser == null) {
					if (user == null) {
						return new ArrayList<IssueTrackerAttachment>();
					}
					viewUser = user.getUserId();
				}
				Path path = root.join("creator");
				where = cb.and(where, (cb.equal(path, new User(viewUser))));
			}
			if (instance != null) {
				where = cb.and(where, (cb.equal(root.get("tracker"), instance.detach())));
			} else {
				where = cb.and(where, cb.equal(root.get("domain"), domain.detach()));
			}
			if (duration != null) {
				where = cb.and(where, (cb.greaterThanOrEqualTo(root.get("creationDate"), duration.getTime())));
			}
			criteria.where(where);
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<ChatMessage> getAllChats(ChatChannel channel, int page, int pageSize, String nameFilter, Calendar duration, String viewUser, 
			InstanceFilter filter, InstanceSort sort, User user, Domain domain) {
		log(Level.FINE, "all chats");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(ChatMessage.class);
			if (sort == InstanceSort.Date) {
				criteria.orderBy(cb.asc(root.get("creationDate")));
			} else {
				criteria.orderBy(cb.asc(root.get("message")), cb.desc(root.get("creationDate")));
			}
			Predicate where = cb.conjunction();
			if (filter == InstanceFilter.Personal) {
				if (viewUser == null) {
					if (user == null) {
						return new ArrayList<ChatMessage>();
					}
					viewUser = user.getUserId();
				}
				Path path = root.join("creator");
				where = cb.and(where, (cb.equal(path, new User(viewUser))));
			}
			if ((nameFilter != null) && !nameFilter.trim().isEmpty()) {
				where = cb.and(where, (cb.like(cb.lower(root.get("message")), "%" + nameFilter.toLowerCase() + "%")));
			}
			if (channel != null) {
				where = cb.and(where, (cb.equal(root.get("channel"), channel.detach())));
			} else {
				where = cb.and(where, cb.equal(root.get("domain"), domain.detach()));
			}
			if (duration != null) {
				where = cb.and(where, (cb.greaterThanOrEqualTo(root.get("creationDate"), duration.getTime())));
			}
			criteria.where(where);
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}

	public List<BotInstance> getAllTemplates(User user, ContentRating content, Domain domain) {
		log(Level.FINE, "all templates");

		return AdminDatabase.instance().getAllInstances(
					0, 100, "", "", user.getUserId(), InstanceFilter.Template, InstanceRestrict.None, InstanceSort.Name, content, "", user, domain, true);
	}

	public List<BotInstance> getAllInstances(Domain domain) {
		log(Level.FINE, "all instances");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("domain", domain.detach());
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("rawtypes")
	public List<Domain> getAllDomains() {
		log(Level.FINE, "all domains");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(Domain.class);
			criteria.orderBy(cb.asc(root.get("name")));			
			Predicate where = cb.conjunction();
			where = cb.and(where, (cb.equal(root.get("isPrivate"), false)));
			where = cb.and(where, (cb.equal(root.get("isHidden"), false)));
			criteria.where(where);
			Query query = em.createQuery(criteria);
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<User> getAllUsers(int page, int pageSize, String nameFilter, String emailFilter, String tagFilter,
			UserFilter userFilter, UserRestrict restrictFilter, 
			UserSort sortFilter, User user, boolean isSuperUser) {
		log(Level.FINE, "all users", restrictFilter);
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(User.class);
			buildUserSearchCriteria(cb, criteria, root, false, sortFilter);
			Predicate where = buildUserSearchWhere(cb, criteria, root, nameFilter, emailFilter, tagFilter, userFilter, restrictFilter, user, isSuperUser); 
			if (where == null) return new ArrayList<User>();
			criteria.where(where);
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public int getAllUserCount(String nameFilter, String emailFilter, String tagFilter, UserFilter userFilter, 
			UserRestrict  restrictFilter, UserSort sortFilter, User user,  boolean isSuperUser) {
		log(Level.FINE, "count all users");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(User.class);
			buildUserSearchCriteria(cb, criteria, root, true, sortFilter);
			Predicate where = buildUserSearchWhere(cb, criteria, root, nameFilter, emailFilter, tagFilter, userFilter, restrictFilter, user, isSuperUser);
			if (where == null) return 0;
			criteria.where(where);
			Query query = em.createQuery(criteria);
			return ((Number)query.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Predicate buildUserSearchWhere(CriteriaBuilder cb, CriteriaQuery criteria, Root root, String nameFilter, String emailFilter,
			String tagFilter, UserFilter userFilter, UserRestrict restrictFilter, User user, boolean isSuperUser) {		
		Predicate where = cb.conjunction();
		if (user == null) {
			where = cb.and(where, cb.equal(root.get("access"), User.UserAccess.Everyone));
		}
		if (user != null && !user.isSuperUser()) {
			where = cb.and(where, cb.equal(root.get("access"), User.UserAccess.Everyone));
		}
		if (nameFilter != null && !nameFilter.isEmpty()) {
			where = cb.and(where, 
					cb.or((cb.like(cb.lower(root.get("name")), "%" + nameFilter.trim().toLowerCase() + "%")), 
							(cb.like(cb.lower(root.get("userId")), "%" + nameFilter.trim().toLowerCase() + "%"))));
		}
		if (isSuperUser && emailFilter != null && !emailFilter.isEmpty()) {
			where = cb.and(where, cb.like(cb.lower(root.get("email")), "%" + emailFilter.trim().toLowerCase() + "%"));
		}
		if (!tagFilter.trim().isEmpty()) {
			Path path = root.join("tags");
			where = cb.and(where, (cb.lower(path.get("name")).in(Utils.csv(tagFilter.toLowerCase()))));
		}
		if ((restrictFilter != null) && restrictFilter != UserRestrict.None) {
			if (restrictFilter == UserRestrict.Ad) {
				where = cb.and(where, cb.isNotNull(root.get("adCode")));
				where = cb.and(where, cb.notEqual(root.get("adCode"), ""));
			} else if (restrictFilter == UserRestrict.AdUnverified) {
				where = cb.and(where, cb.equal(root.get("adCodeVerified"), false));
				where = cb.and(where, cb.isNotNull(root.get("adCode")));
				where = cb.and(where, cb.notEqual(root.get("adCode"), ""));
			} else if (restrictFilter == UserRestrict.Admin) {
				where = cb.and(where, cb.equal(root.get("type"), UserRestrict.Admin));
			} else if (restrictFilter == UserRestrict.Partner) {
				where = cb.and(where, cb.equal(root.get("type"), UserRestrict.Partner));
			} else if (restrictFilter == UserRestrict.Diamond) {
				where = cb.and(where, cb.equal(root.get("type"), UserRestrict.Diamond));
			} else if (restrictFilter == UserRestrict.Platinum) {
				where = cb.and(where, cb.equal(root.get("type"), UserRestrict.Platinum));
			} else if (restrictFilter == UserRestrict.Gold) {
				where = cb.and(where, cb.equal(root.get("type"), UserRestrict.Gold));
			} else if (restrictFilter == UserRestrict.Bronze) {
				where = cb.and(where, cb.equal(root.get("type"), UserRestrict.Bronze));
			} else if (restrictFilter == UserRestrict.Flagged) {
				where = cb.and(where, cb.equal(root.get("isFlagged"), true));
			} else if (restrictFilter == UserRestrict.Icon) {
				where = cb.and(where, cb.isNotNull(root.get("avatar")));
			} else if (restrictFilter == UserRestrict.Banned) {
				where = cb.and(where, cb.equal(root.get("isBlocked"), true));
			} else if (restrictFilter == UserRestrict.Verified) {
				where = cb.and(where, cb.equal(root.get("isVerified"), true));
			} else if (restrictFilter == UserRestrict.AppID) {
				where = cb.and(where, cb.isNotNull(root.get("applicationId")));
			} else if (restrictFilter == UserRestrict.Website) {
				where = cb.and(where, cb.isNotNull(root.get("website")));
				where = cb.and(where, cb.notEqual(root.get("website"), ""));
			} else if (restrictFilter == UserRestrict.Email) {
				where = cb.and(where, cb.isNotNull(root.get("email")));
				where = cb.and(where, cb.notEqual(root.get("email"), "")); 
			} else if (restrictFilter == UserRestrict.Expired) {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.YEAR, -1);
				Date prevYear = calendar.getTime();
				where = cb.and(where, cb.isNotNull(root.get("upgradeDate")));
				where = cb.and(where, cb.lessThan(root.get("upgradeDate"), prevYear));
			}
		}
		return where;
	}
	
	@SuppressWarnings("rawtypes")
	public void buildUserSearchCriteria(CriteriaBuilder cb, CriteriaQuery criteria, Root root, boolean count, UserSort sort) {
		criteria.distinct(true);
		if (!count) {
			if (sort == org.botlibre.web.bean.UserBean.UserSort.Connects) {
				criteria.orderBy(cb.desc(root.get("connects")), cb.asc(root.get("name")));
			} else if (sort == org.botlibre.web.bean.UserBean.UserSort.Date) {
				criteria.orderBy(cb.desc(root.get("creationDate")), cb.asc(root.get("name")));
			} else if (sort == org.botlibre.web.bean.UserBean.UserSort.LastConnect) {
				criteria.orderBy(cb.desc(root.get("lastConnected")), cb.asc(root.get("name")));
			} else if (sort == org.botlibre.web.bean.UserBean.UserSort.Affiliates) {
				criteria.orderBy(cb.desc(root.get("affiliates")), cb.asc(root.get("name")));
			} else {
				criteria.orderBy(cb.asc(root.get("name")));
			}
		} else {
			criteria.select(cb.count(root));
		}
	}

	
	@SuppressWarnings("rawtypes")
	public List<UserMessage> getAllUserMessages(int page, int pageSize, String filter, String userFilter,
			UserMessageSort sortFilter, User user, boolean sent) {
		log(Level.FINE, "all user messages", user);
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(UserMessage.class);
			buildUserMessageSearchCriteria(cb, criteria, root, false, sortFilter, sent);
			Predicate where = buildUserMessageSearchWhere(cb, criteria, root, filter, userFilter, user, sent); 
			if (where == null) {
				return new ArrayList<UserMessage>();
			}
			criteria.where(where);
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<UserMessage> getNewUserMessages(User user) {
		log(Level.FINE, "new user messages", user);
		EntityManager em = getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select t from UserMessage t where t.owner = :user and t.creationDate > :date order by t.creationDate asc");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("user", user);
			query.setParameter("date", user.getOldLastConnected());
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<UserMessage> checkUserNewMessages(UserMessageConfig messageConfig) {
		log(Level.FINE, "check user new messages", messageConfig.target);
		EntityManager em = getFactory().createEntityManager();
		try {
			User user = (User)em.find(User.class, messageConfig.user);
			User creatorUser = (User)em.find(User.class, messageConfig.creator);
			User targetUser = (User)em.find(User.class, messageConfig.target);
			if (user == null) {
				throw new BotException("User does not exist - " + messageConfig.user);
			}
			if (creatorUser == null) {
				throw new BotException("User does not exist - " + messageConfig.creator);
			}
			if (targetUser == null) {
				throw new BotException("User does not exist - " + messageConfig.target);
			}
			Date date = null;
			java.sql.Timestamp timestamp = null;
			try {
				SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				date = formater.parse(messageConfig.creationDate);
				timestamp = new java.sql.Timestamp(date.getTime());
			} catch (ParseException exception) {
				log(exception);
			}
			Query query = em.createQuery("Select t from UserMessage t where t.creator = :creatorUser and t.owner = :targetUser and t.target = :targetUser and t.creationDate > :date order by t.creationDate asc");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("creatorUser", creatorUser);
			query.setParameter("targetUser", targetUser);
			query.setParameter("date", timestamp);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public User getUser(String userId) {
		log(Level.FINE, "get user", userId);
		EntityManager em =  getFactory().createEntityManager();
		try {
			User user = (User)em.find(User.class, userId);
			return user;
		} finally {
			em.close();
		}
	}
	
	public List<UserMessage> getUserToUserMessages(int page, int pageSize, String filter, UserMessageConfig messageConfig) {
		log(Level.FINE, "user to user messages", messageConfig.target);
		EntityManager em =  getFactory().createEntityManager();
		try {
			String userId = messageConfig.user;
			User user = (User)em.find(User.class, userId);
			if (user == null) {
				throw new BotException("Friend does not exist - " + userId);
			}
			String targetUserId = messageConfig.target;
			User targetUser = (User)em.find(User.class, targetUserId);
			if (targetUser == null) {
				throw new BotException("Friend does not exist - " + targetUserId);
			}
			Query query = em.createQuery("Select t from UserMessage t where (((t.creator = :user and t.owner = :user and t.target = :targetUser) or (t.creator = :targetUser and t.owner = :user and t.target = :user)) and ((t.subject like :filter) or (t.message like :filter))) order by t.creationDate desc");
			query.setHint("eclipselink.read-only", "true");
			String likeFilter = "%" + filter.trim().toLowerCase() + "%";
			query.setParameter("filter", likeFilter);
			query.setParameter("user", user);
			query.setParameter("targetUser", targetUser);
			query.setMaxResults(pageSize + 1);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public int getAllUserMessageCount(String filter, String userFilter, UserMessageSort sortFilter, User user, boolean sent) {
		log(Level.FINE, "count all user messages");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(UserMessage.class);
			buildUserMessageSearchCriteria(cb, criteria, root, true, sortFilter, sent);
			Predicate where = buildUserMessageSearchWhere(cb, criteria, root, filter, userFilter, user, sent);
			if (where == null) return 0;
			criteria.where(where);
			Query query = em.createQuery(criteria);
			return ((Number)query.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Predicate buildUserMessageSearchWhere(CriteriaBuilder cb, CriteriaQuery criteria, Root root, String filter, String userFilter, User user, boolean sent) {		
		Predicate where = cb.conjunction();	
		where = cb.and(where, cb.equal(root.get("owner"), user));
		if (filter != null && !filter.isEmpty()) {
			where = cb.and(where, 
					cb.or((cb.like(cb.lower(root.get("subject")), "%" + filter.trim().toLowerCase() + "%")), 
							(cb.like(cb.lower(root.get("message")), "%" + filter.trim().toLowerCase() + "%"))));
		}
		if (userFilter != null && !userFilter.isEmpty()) {
			if (sent) {
				where = cb.and(where, cb.like(cb.lower(root.get("target").get("userId")), "%" + userFilter.trim().toLowerCase() + "%"));
			} else {
				where = cb.and(where, cb.like(cb.lower(root.get("creator").get("userId")), "%" + userFilter.trim().toLowerCase() + "%"));
			}
		}
		return where;
	}
	
	@SuppressWarnings("rawtypes")
	public void buildUserMessageSearchCriteria(CriteriaBuilder cb, CriteriaQuery criteria, Root root, boolean count, UserMessageSort sort, boolean sent) {
		criteria.distinct(true);
		if (!count) {
			if (sort == UserMessageSort.Date) {
				criteria.orderBy(cb.desc(root.get("creationDate")), cb.asc(root.get("subject")));
			} else if (sort == UserMessageSort.User) {
				if (sent) {
					criteria.orderBy(cb.asc(root.get("target").get("userId")), cb.desc(root.get("creationDate")));
				} else {
					criteria.orderBy(cb.asc(root.get("creator").get("userId")), cb.desc(root.get("creationDate")));
				}
			} else if (sort == UserMessageSort.Subject) {
				criteria.orderBy(cb.asc(root.get("subject")), cb.desc(root.get("creationDate")));
			} else {
				criteria.orderBy(cb.desc(root.get("creationDate")));
			}
		} else {
			criteria.select(cb.count(root));
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<UserPayment> getAllUserPayments(int page, int pageSize, String userFilter, String transactionFilter, String ccFilter,
			UserPaymentType userPaymentType, UserPaymentStatus userPaymentStatus, TransactionRestrict transactionRestrict, TransactionSort transactionSort) {
		log(Level.FINE, "all user payments");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(UserPayment.class);
			buildUserPaymentSearchCriteria(cb, criteria, root, false, transactionSort);
			Predicate where = buildUserPaymentSearchWhere(cb, criteria, root, userFilter, transactionFilter, ccFilter, userPaymentType, userPaymentStatus, transactionRestrict); 
			if (where == null) {
				return new ArrayList<UserPayment>();
			}
			criteria.where(where);
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public int getAllUserPaymentCount(String userFilter, String transactionFilter, String ccFilter,
			UserPaymentType userPaymentType, UserPaymentStatus userPaymentStatus, TransactionRestrict transactionRestrict, TransactionSort transactionSort) {
		log(Level.FINE, "count all user payments");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(UserPayment.class);
			buildUserPaymentSearchCriteria(cb, criteria, root, true, transactionSort);
			Predicate where = buildUserPaymentSearchWhere(cb, criteria, root, userFilter, transactionFilter, ccFilter, userPaymentType, userPaymentStatus, transactionRestrict);
			if (where == null) {
				return 0;
			}
			criteria.where(where);
			Query query = em.createQuery(criteria);
			return ((Number)query.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Predicate buildUserPaymentSearchWhere(CriteriaBuilder cb, CriteriaQuery criteria, Root root, String userFilter, String transactionFilter, String ccFilter,
			UserPaymentType userPaymentType, UserPaymentStatus userPaymentStatus, TransactionRestrict transactionRestrict) {		
		Predicate where = cb.conjunction();
		if (userFilter != null && !userFilter.isEmpty()) {
			where = cb.and(where, (cb.like(cb.lower(root.get("userId")), "%" + userFilter.trim().toLowerCase() + "%")));
		}
		if (transactionFilter != null && !transactionFilter.isEmpty()) {
			where = cb.and(where, (cb.like(cb.lower(root.get("paypalTx")), "%" + transactionFilter.trim().toLowerCase() + "%")));
		}
		if (ccFilter != null && !ccFilter.isEmpty()) {
			where = cb.and(where, (cb.like(cb.lower(root.get("paypalCc")), "%" + ccFilter.trim().toLowerCase() + "%")));
		}
		if (userPaymentType != null) {
			where = cb.and(where, cb.equal(root.get("type"), userPaymentType));
		}
		if (userPaymentStatus != null) {
			where = cb.and(where, cb.equal(root.get("status"), userPaymentStatus));
		}
		if ((transactionRestrict != null) && transactionRestrict != TransactionRestrict.None) {
			if (transactionRestrict == TransactionRestrict.Diamond) {
				where = cb.and(where, cb.equal(root.get("userType"), UserType.Diamond));
			} else if (transactionRestrict == TransactionRestrict.Platinum) {
				where = cb.and(where, cb.equal(root.get("userType"), UserType.Platinum));
			} else if (transactionRestrict == TransactionRestrict.Gold) {
				where = cb.and(where, cb.equal(root.get("userType"), UserType.Gold));
			} else if (transactionRestrict == TransactionRestrict.Bronze) {
				where = cb.and(where, cb.equal(root.get("userType"), UserType.Bronze));
			}
		}
		return where;
	}
	
	@SuppressWarnings("rawtypes")
	public void buildUserPaymentSearchCriteria(CriteriaBuilder cb, CriteriaQuery criteria, Root root, boolean count, TransactionSort sort) {
		criteria.distinct(true);
		if (!count) {
			if (sort == TransactionSort.User) {
				criteria.orderBy(cb.asc(root.get("userId")), cb.desc(root.get("paymentDate")));
			} else if (sort == TransactionSort.Date) {
				criteria.orderBy(cb.desc(root.get("paymentDate")), cb.asc(root.get("userId")));
			} else if (sort == TransactionSort.Transaction) {
				criteria.orderBy(cb.asc(root.get("paypalTx")), cb.desc(root.get("paymentDate")));
			} else {
				criteria.orderBy(cb.desc(root.get("paymentDate")));
			}
		} else {
			criteria.select(cb.count(root));
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<Payment> getAllPayments(int page, int pageSize, String userFilter, String transactionFilter, String ccFilter,
			AccountType paymentType, PaymentStatus paymentStatus, TransactionRestrict transactionRestrict, TransactionSort transactionSort) {
		log(Level.FINE, "all payments");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(Payment.class);
			buildPaymentSearchCriteria(cb, criteria, root, false, transactionSort);
			Predicate where = buildPaymentSearchWhere(cb, criteria, root, userFilter, transactionFilter, ccFilter, paymentType, paymentStatus, transactionRestrict); 
			if (where == null) {
				return new ArrayList<Payment>();
			}
			criteria.where(where);
			Query query = em.createQuery(criteria);
			query.setMaxResults(pageSize);
			if (page != 0) {
				query.setFirstResult(Math.max(0, pageSize * page));
			}
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public int getAllPaymentCount(String userFilter, String transactionFilter, String ccFilter,
			AccountType paymentType, PaymentStatus paymentStatus, TransactionRestrict transactionRestrict, TransactionSort transactionSort) {
		log(Level.FINE, "count all payments");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(Payment.class);
			buildUserPaymentSearchCriteria(cb, criteria, root, true, transactionSort);
			Predicate where = buildPaymentSearchWhere(cb, criteria, root, userFilter, transactionFilter, ccFilter, paymentType, paymentStatus, transactionRestrict);
			if (where == null) {
				return 0;
			}
			criteria.where(where);
			Query query = em.createQuery(criteria);
			return ((Number)query.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Predicate buildPaymentSearchWhere(CriteriaBuilder cb, CriteriaQuery criteria, Root root, String userFilter, String transactionFilter, String ccFilter,
			AccountType paymentType, PaymentStatus paymentStatus, TransactionRestrict transactionRestrict) {		
		Predicate where = cb.conjunction();
		if (userFilter != null && !userFilter.isEmpty()) {
			where = cb.and(where, (cb.like(cb.lower(root.get("userId")), "%" + userFilter.trim().toLowerCase() + "%")));
		}
		if (transactionFilter != null && !transactionFilter.isEmpty()) {
			where = cb.and(where, (cb.like(cb.lower(root.get("paypalTx")), "%" + transactionFilter.trim().toLowerCase() + "%")));
		}
		if (ccFilter != null && !ccFilter.isEmpty()) {
			where = cb.and(where, (cb.like(cb.lower(root.get("paypalCc")), "%" + ccFilter.trim().toLowerCase() + "%")));
		}
		if (paymentType != null) {
			where = cb.and(where, cb.equal(root.get("accountType"), paymentType));
		}
		if (paymentStatus != null) {
			where = cb.and(where, cb.equal(root.get("status"), paymentStatus));
		}
		return where;
	}
	
	@SuppressWarnings("rawtypes")
	public void buildPaymentSearchCriteria(CriteriaBuilder cb, CriteriaQuery criteria, Root root, boolean count, TransactionSort sort) {
		criteria.distinct(true);
		if (!count) {
			if (sort == TransactionSort.User) {
				criteria.orderBy(cb.asc(root.get("userId")), cb.desc(root.get("paymentDate")));
			} else if (sort == TransactionSort.Date) {
				criteria.orderBy(cb.desc(root.get("paymentDate")), cb.asc(root.get("userId")));
			} else if (sort == TransactionSort.Transaction) {
				criteria.orderBy(cb.asc(root.get("paypalTx")), cb.desc(root.get("paymentDate")));
			} else {
				criteria.orderBy(cb.desc(root.get("paymentDate")));
			}
		} else {
			criteria.select(cb.count(root));
		}
	}

	@SuppressWarnings("rawtypes")
	public List<User> getUnverifiedUsers() {
		log(Level.FINE, "unverified users");
		EntityManager em = getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(User.class);
			Predicate where = cb.conjunction();
			where = cb.and(where, cb.notEqual(root.get("email"), ""));
			where = cb.and(where, cb.equal(root.get("isVerified"), false));
			criteria.where(where);
			Query query = em.createQuery(criteria);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("rawtypes")
	public List<License> getAllLicenses() {
		log(Level.FINE, "all licenses");
		EntityManager em = getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(License.class);
			criteria.orderBy(cb.desc(root.get("licenseDate")));
			Query query = em.createQuery(criteria);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("rawtypes")
	public List<Payment> getAllPayments() {
		log(Level.FINE, "all payments");
		EntityManager em = getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(Payment.class);
			criteria.orderBy(cb.desc(root.get("paymentDate")));
			Query query = em.createQuery(criteria);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("rawtypes")
	public List<UserPayment> getAllUserPayments() {
		log(Level.FINE, "all user payments");
		EntityManager em = getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(UserPayment.class);
			criteria.orderBy(cb.desc(root.get("paymentDate")));
			Query query = em.createQuery(criteria);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List<Domain> getUserDomains(User user) {
		if (user == null) {
			return new ArrayList<Domain>();
		}
		log(Level.FINE, "all domains");
		EntityManager em =  getFactory().createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteria = cb.createQuery();
			Root root = criteria.from(Domain.class);
			criteria.orderBy(cb.asc(root.get("name")));
			Predicate where = cb.conjunction();
			Path admins = root.join("admins", JoinType.LEFT);
			Path users = root.join("users", JoinType.LEFT);
			where = cb.and(where, cb.or(cb.equal(admins.get("userId"), user.getUserId()), cb.equal(users.get("userId"), user.getUserId())));
			criteria.where(where);
			Query query = em.createQuery(criteria);
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<Forum> getAllForums(Domain domain) {
		log(Level.FINE, "all forum");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from Forum p where p.domain = :domain order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("domain", domain.detach());
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<Forum> getAllIssueTrackers(Domain domain) {
		log(Level.FINE, "all issuetracker");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from IssueTracker p where p.domain = :domain order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("domain", domain.detach());
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<Avatar> getAllAvatars(Domain domain) {
		log(Level.FINE, "all avatars");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from Avatar p where p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("domain", domain.detach());
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<ScriptSource> getAllScriptsVersions(Script script) {
		log(Level.FINE, "all script versions", script);
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from ScriptSource p where p.script = :script order by p.creationDate desc");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("script", script.detach());
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public ScriptSource validateScriptVersion(Long id) {
		log(Level.FINE, "script version", id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			ScriptSource instance = em.find(ScriptSource.class, id);
			if (instance == null) {
				throw new BotException("Script version does not exist - " + id);
			}
			return instance;
		} finally {
			em.close();
		}
	}

	public List<Script> getAllScripts(Domain domain) {
		log(Level.FINE, "all scripts");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from Script p where p.domain = :domain order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("domain", domain.detach());
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<Graphic> getAllGraphics(Domain domain) {
		log(Level.FINE, "all graphic");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from Graphic p where p.domain = :domain order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("domain", domain.detach());
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<Translation> getAllTranslations() {
		log(Level.FINE, "all translations");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select t from Translation t order by t.sourceLanguage, t.targetLanguage, t.text");
			query.setHint("eclipselink.read-only", "true");
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<Object[]> groupBotTranslations() {
		log(Level.FINE, "group bot translations");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select t.sourceLanguage, t.targetLanguage, count(t) from BotTranslation t group by t.sourceLanguage, t.targetLanguage order by count(t)");
			query.setHint("eclipselink.read-only", "true");
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<Object[]> groupTranslations() {
		log(Level.FINE, "group translations");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select t.sourceLanguage, t.targetLanguage, count(t) from Translation t group by t.sourceLanguage, t.targetLanguage order by count(t)");
			query.setHint("eclipselink.read-only", "true");
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public int countBotTranslations() {
		log(Level.FINE, "count bot translations");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select count(t) from BotTranslation t");
			query.setHint("eclipselink.read-only", "true");
			return ((Number)query.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}

	public int countTranslations() {
		log(Level.FINE, "count translations");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select count(t) from Translation t");
			query.setHint("eclipselink.read-only", "true");
			return ((Number)query.getSingleResult()).intValue();
		} finally {
			em.close();
		}
	}

	public List<ChatChannel> getAllChannels(Domain domain) {
		log(Level.FINE, "all channels");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from ChatChannel p where p.domain = :domain order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("domain", domain.detach());
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<Issue> getAllPriorityIssues(IssueTracker tracker, Domain domain) {
		log(Level.FINE, "all priority issues");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = null;
			if (tracker == null) {
				query = em.createQuery("Select p from Issue p join p.tracker f where f.isPrivate = false and f.isHidden = false and (f.accessMode = :mode or f.accessMode is null) and f.isAdult = false and p.isPriority = true and p.isHidden = false and p.domain = :domain order by p.creationDate desc");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("mode", AccessMode.Everyone);
				query.setParameter("domain", domain);
			} else {
				query = em.createQuery("Select p from Issue p where p.tracker = :tracker and p.isPriority = true and p.isHidden = false and p.domain = :domain order by p.creationDate desc");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("tracker", tracker.detach());
				query.setParameter("domain", domain);
			}
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<ForumPost> getAllFeaturedForumPosts(Forum forum, Domain domain) {
		log(Level.FINE, "all featured forum posts");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = null;
			if (forum == null) {
				query = em.createQuery("Select p from ForumPost p join p.forum f where f.isPrivate = false and f.isHidden = false and (f.accessMode = :mode or f.accessMode is null) and f.isAdult = false and p.isFeatured = true and p.domain = :domain order by p.creationDate desc");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("mode", AccessMode.Everyone);
				query.setParameter("domain", domain);
			} else {
				query = em.createQuery("Select p from ForumPost p where p.forum = :forum and p.isFeatured = true and p.domain = :domain order by p.creationDate desc");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("forum", forum.detach());
				query.setParameter("domain", domain);
			}
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<BotInstance> getAllTwitterInstances() {
		log(Level.FINE, "all twitter");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableTwitter = true order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<BotInstance> getAllTwitterInstances(UserType type) {
		log(Level.FINE, "all twitter", type);
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableTwitter = true and p.creator.type = :type order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("type", type);
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<BotInstance> getAllOverLimitInstances() {
		log(Level.FINE, "all over limit instances");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.archived = false and p.memorySize > p.memoryLimit order by p.memorySize");
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<BotInstance> getAllFacebookInstances() {
		log(Level.FINE, "all facebook");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableFacebook = true order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<BotInstance> getAllTelegramInstances() {
		log(Level.FINE, "all telegram");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableTelegram = true order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<BotInstance> getAllTimerInstances() {
		log(Level.FINE, "all timers");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableTimers = true order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<BotInstance> getAllSlackInstances() {
		log(Level.FINE, "all slack");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableSlack = true order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<BotInstance> getAllSkypeInstances() {
		log(Level.FINE, "all skype");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableSkype = true order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<BotInstance> getAllWeChatInstances() {
		log(Level.FINE, "all wechat");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableWeChat = true order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<BotInstance> getAllKikInstances() {
		log(Level.FINE, "all kik");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableKik = true order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<BotInstance> getAllAlexaInstances() {
		log(Level.FINE, "all alexa");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableAlexa = true order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<BotInstance> getAllGoogleAssistantInstances() {
		log(Level.FINE, "all google assistant");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableGoogleAssistant = true order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<BotInstance> getAllFacebookInstances(UserType type) {
		log(Level.FINE, "all facebook", type);
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableFacebook = true and p.creator.type = :type order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("type", type);
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<BotInstance> getAllTelegramInstances(UserType type) {
		log(Level.FINE, "all telegram", type);
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableTelegram = true and p.creator.type = :type order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("type", type);
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<BotInstance> getAllTimerInstances(UserType type) {
		log(Level.FINE, "all timer", type);
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableTimers = true and p.creator.type = :type order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("type", type);
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<BotInstance> getAllSlackInstances(UserType type) {
		log(Level.FINE, "all slack", type);
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableSlack = true and p.creator.type = :type order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("type", type);
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<BotInstance> getAllSkypeInstances(UserType type) {
		log(Level.FINE, "all skype", type);
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableSkype = true and p.creator.type = :type order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("type", type);
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<BotInstance> getAllWeChatInstances(UserType type) {
		log(Level.FINE, "all wechat", type);
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableWeChat = true and p.creator.type = :type order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("type", type);
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<BotInstance> getAllKikInstances(UserType type) {
		log(Level.FINE, "all kik", type);
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableKik = true and p.creator.type = :type order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("type", type);
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<BotInstance> getAllEmailInstances() {
		log(Level.FINE, "all email");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableEmail = true order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<BotInstance> getAllEmailInstances(UserType type) {
		log(Level.FINE, "all email", type);
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.enableEmail = true and p.creator.type = :type order by p.name");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("type", type);
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<AvatarImage> getSharedAvatarImages(Domain domain) {
		log(Level.FINE, "shared avatars");
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select a from AvatarImage a where a.domain = :domain and a.shared = true order by a.name");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("domain", domain);
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<Tag> getTags(String type, Domain domain) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			if (domain == null) {
				Query query = em.createQuery("Select t from Tag t where t.type = :type order by t.count desc, t.name");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("type", type);
				query.setMaxResults(100);
				return query.getResultList();
			} else if (type == null) {
				Query query = em.createQuery("Select t from Tag t where t.domain = :domain order by t.type desc, t.name");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("domain", domain);
				return query.getResultList();
			} else {
				Query query = em.createQuery("Select t from Tag t where t.type = :type and t.domain = :domain order by t.count desc, t.name");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("type", type);
				query.setParameter("domain", domain);
				query.setMaxResults(100);
				return query.getResultList();
			}
		} finally {
			em.close();
		}
	}
	
	public List<Category> getRootCategories(String type, Domain domain) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select t from Category t left join t.parents p where p.id is null and t.type = :type and t.domain = :domain order by t.name");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("type", type);
			query.setParameter("domain", domain);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<Category> getAllCategories(String type, Domain domain) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			if (type == null) {
				Query query = em.createQuery("Select t from Category t where t.domain = :domain order by t.type, t.name");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("domain", domain);
				return query.getResultList();
			} else if (type.equals("Domain")) {
				Query query = em.createQuery("Select t from Category t where t.type = :type order by t.count desc, t.name");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("type", type);
				return query.getResultList();
			} else {
				Query query = em.createQuery("Select t from Category t where t.type = :type and t.domain = :domain order by t.count desc, t.name");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("type", type);
				query.setParameter("domain", domain);
				return query.getResultList();
			}
		} finally {
			em.close();
		}
	}
	
	public List<UserMessage> getAllUserMessages(User user) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select t from UserMessage t where t.owner = :user and t.target = :user order by t.creationDate desc");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("user", user);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<Friendship> getUserFriendships(String userId) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select t from Friendship t where t.userId = :userId");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("userId", userId);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<Friendship> getUserFollowers(String userId) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select t from Friendship t where t.friend = :userId");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("userId", userId);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<UserMessage> getAllSentMessages(User user) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select t from UserMessage t where t.owner = :user and t.creator = :user order by t.creationDate desc");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("user", user);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<Stats> getAllStats() {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select s from Stats s order by s.date desc");
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(1000);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<LiveChatStats> getAllLiveChatStats(String id) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select s from LiveChatStats s where s.channelId = :id order by s.date desc");
			query.setParameter("id", Long.valueOf(id));
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(100);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<BotStats> getAllBotStats(String id) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select s from BotStats s where s.botId = :id order by s.date desc");
			query.setParameter("id", Long.valueOf(id));
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(100);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<IPStats> getAllIPStats(String ip) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select s from IPStats s where s.ip = :ip order by s.date desc");
			query.setParameter("ip", ip);
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(100);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<IPStats> getAllAgentStats(String agent) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select s from IPStats s where s.agent = :agent order by s.date desc");
			query.setParameter("agent", agent);
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(100);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	/**
	 * Old method to load the server configuration properties from the local xml file "botplatform.xml".
	 * This allows the server configuration to be changed without requiring rebuilding/deploying.
	 * Basic encryption is used for passwords/keys.
	 */
	public void oldRestorePlatformSettings() {
		try {
			File file = new File("botplatform.xml");
			// Read config xml to initialize plugins.
			log(Level.INFO, "Loading legacy bootstrap file botplatform.xml");
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			Document document = parser.parse(file);
			Element root = document.getDocumentElement();
	
			// Parse and initialize settings.
	
			try {
				if (root.getElementsByTagName("DATABASEUSER").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("DATABASEUSER").item(0);
					String value = element.getTextContent().trim();
					Site.DATABASEUSER = value;
				}
				
				if (root.getElementsByTagName("DATABASEPASSWORD").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("DATABASEPASSWORD").item(0);
					String value = element.getTextContent().trim();
					// Check if encrypted from && prefix.
					if (value.startsWith("__")) {
						try {
							Site.DATABASEPASSWORD = Utils.decrypt(Site.KEY2, value.substring(2, value.length()));
						} catch (Exception exception) {
							Site.DATABASEPASSWORD = value;
						}
					} else {
						Site.DATABASEPASSWORD = value;
					}
				}
				
				if (root.getElementsByTagName("URL_PREFIX").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("URL_PREFIX").item(0);
					String value = element.getTextContent().trim();
					Site.URL_PREFIX = value;
				}
				
				if (root.getElementsByTagName("URL_SUFFIX").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("URL_SUFFIX").item(0);
					String value = element.getTextContent().trim();
					Site.URL_SUFFIX = value;
				}
				
				if (root.getElementsByTagName("SERVER_NAME").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("SERVER_NAME").item(0);
					String value = element.getTextContent().trim();
					Site.SERVER_NAME = value;
				}
				
				if (root.getElementsByTagName("SERVER_NAME2").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("SERVER_NAME2").item(0);
					String value = element.getTextContent().trim();
					Site.SERVER_NAME2 = value;
				}
				
				if (root.getElementsByTagName("URL").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("URL").item(0);
					String value = element.getTextContent().trim();
					Site.URL = value;
				}
				
				if (root.getElementsByTagName("URLLINK").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("URLLINK").item(0);
					String value = element.getTextContent().trim();
					Site.URLLINK = value;
				}
				
				if (root.getElementsByTagName("SECUREURLLINK").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("SECUREURLLINK").item(0);
					String value = element.getTextContent().trim();
					Site.SECUREURLLINK = value;
				}
				
				if (root.getElementsByTagName("REDIRECT").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("REDIRECT").item(0);
					String value = element.getTextContent().trim();
					Site.REDIRECT = value;
				}
	
				if (root.getElementsByTagName("HTTPS").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("HTTPS").item(0);
					String value = element.getTextContent().trim();
					Site.HTTPS = Boolean.valueOf(value);
				}
				
				if (root.getElementsByTagName("PYTHONSERVER").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("PYTHONSERVER").item(0);
					String value = element.getTextContent().trim();
					Site.PYTHONSERVER = value;
				}
	
				if (root.getElementsByTagName("BOOTSTRAP").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("BOOTSTRAP").item(0);
					String value = element.getTextContent().trim();
					Site.BOOTSTRAP = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("LOCK").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("LOCK").item(0);
					String value = element.getTextContent().trim();
					Site.LOCK = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("READONLY").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("READONLY").item(0);
					String value = element.getTextContent().trim();
					Site.READONLY = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("ADULT").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("ADULT").item(0);
					String value = element.getTextContent().trim();
					Site.ADULT = Boolean.valueOf(value);
				}
				
				if (root.getElementsByTagName("CONTENT_RATING").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("CONTENT_RATING").item(0);
					String value = element.getTextContent().trim();
					Site.CONTENT_RATING = ContentRating.valueOf(value);
				}
				
				if (root.getElementsByTagName("NAME").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("NAME").item(0);
					String value = element.getTextContent().trim();
					Site.NAME = value;
				}
				
				if (root.getElementsByTagName("DOMAIN").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("DOMAIN").item(0);
					String value = element.getTextContent().trim();
					Site.DOMAIN = value;
				}
				
				if (root.getElementsByTagName("ID").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("ID").item(0);
					String value = element.getTextContent().trim();
					Site.ID = value;
				}
				
				if (root.getElementsByTagName("PREFIX").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("PREFIX").item(0);
					String value = element.getTextContent().trim();
					Site.PREFIX = value;
				}
				
				if (root.getElementsByTagName("PERSISTENCE_PROTOCOL").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("PERSISTENCE_PROTOCOL").item(0);
					String value = element.getTextContent().trim();
					Site.PERSISTENCE_PROTOCOL = value;
				}
				
				if (root.getElementsByTagName("PERSISTENCE_HOST").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("PERSISTENCE_HOST").item(0);
					String value = element.getTextContent().trim();
					Site.PERSISTENCE_HOST = value;
				}
				
				if (root.getElementsByTagName("PERSISTENCE_PORT").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("PERSISTENCE_PORT").item(0);
					String value = element.getTextContent().trim();
					Site.PERSISTENCE_PORT = value;
				}
				
				if (root.getElementsByTagName("PERSISTENCE_UNIT").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("PERSISTENCE_UNIT").item(0);
					String value = element.getTextContent().trim();
					Site.PERSISTENCE_UNIT = value;
				}
				
				if (root.getElementsByTagName("HASHTAG").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("HASHTAG").item(0);
					String value = element.getTextContent().trim();
					Site.HASHTAG = value;
				}
				
				if (root.getElementsByTagName("TYPE").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("TYPE").item(0);
					String value = element.getTextContent().trim();
					Site.TYPE = value;
				}
				
				if (root.getElementsByTagName("TWITTER").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("TWITTER").item(0);
					String value = element.getTextContent().trim();
					Site.TWITTER = Boolean.valueOf(value);
				}
				
				if (root.getElementsByTagName("FACEBOOK").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("FACEBOOK").item(0);
					String value = element.getTextContent().trim();
					Site.FACEBOOK = Boolean.valueOf(value);
				}
				
				if (root.getElementsByTagName("TELEGRAM").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("TELEGRAM").item(0);
					String value = element.getTextContent().trim();
					Site.TELEGRAM = Boolean.valueOf(value);
				}
				
				if (root.getElementsByTagName("SLACK").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("SLACK").item(0);
					String value = element.getTextContent().trim();
					Site.SLACK = Boolean.valueOf(value);
				}
				
				if (root.getElementsByTagName("SLACK").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("SLACK").item(0);
					String value = element.getTextContent().trim();
					Site.SLACK = Boolean.valueOf(value);
				}
				
				if (root.getElementsByTagName("SKYPE").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("SKYPE").item(0);
					String value = element.getTextContent().trim();
					Site.SKYPE = Boolean.valueOf(value);
				}
				
				if (root.getElementsByTagName("WECHAT").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("WECHAT").item(0);
					String value = element.getTextContent().trim();
					Site.WECHAT = Boolean.valueOf(value);
				}
				
				if (root.getElementsByTagName("KIK").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("KIK").item(0);
					String value = element.getTextContent().trim();
					Site.KIK = Boolean.valueOf(value);
				}
				
				if (root.getElementsByTagName("EMAIL").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("EMAIL").item(0);
					String value = element.getTextContent().trim();
					Site.EMAIL = Boolean.valueOf(value);
				}
				
				if (root.getElementsByTagName("TIMERS").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("TIMERS").item(0);
					String value = element.getTextContent().trim();
					Site.TIMERS = Boolean.valueOf(value);
				}
				
				if (root.getElementsByTagName("FORGET").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("FORGET").item(0);
					String value = element.getTextContent().trim();
					Site.FORGET = Boolean.valueOf(value);
				}
				
				if (root.getElementsByTagName("ADMIN").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("ADMIN").item(0);
					String value = element.getTextContent().trim();
					Site.ADMIN = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("VERIFYUSERS").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("VERIFYUSERS").item(0);
					String value = element.getTextContent().trim();
					Site.VERIFYUSERS = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("DEDICATED").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("DEDICATED").item(0);
					String value = element.getTextContent().trim();
					Site.DEDICATED = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("CLOUD").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("CLOUD").item(0);
					String value = element.getTextContent().trim();
					Site.CLOUD = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("COMMERCIAL").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("COMMERCIAL").item(0);
					String value = element.getTextContent().trim();
					Site.COMMERCIAL = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("ALLOW_SIGNUP").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("ALLOW_SIGNUP").item(0);
					String value = element.getTextContent().trim();
					Site.ALLOW_SIGNUP = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("VERIFY_EMAIL").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("VERIFY_EMAIL").item(0);
					String value = element.getTextContent().trim();
					Site.VERIFY_EMAIL = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("ANONYMOUS_CHAT").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("ANONYMOUS_CHAT").item(0);
					String value = element.getTextContent().trim();
					Site.ANONYMOUS_CHAT = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("REQUIRE_TERMS").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("REQUIRE_TERMS").item(0);
					String value = element.getTextContent().trim();
					Site.REQUIRE_TERMS = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("AGE_RESTRICT").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("AGE_RESTRICT").item(0);
					String value = element.getTextContent().trim();
					Site.AGE_RESTRICT = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("BACKLINK").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("BACKLINK").item(0);
					String value = element.getTextContent().trim();
					Site.BACKLINK = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("WEEKLYEMAIL").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("WEEKLYEMAIL").item(0);
					String value = element.getTextContent().trim();
					Site.WEEKLYEMAIL = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("WEEKLYEMAILBOTS").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("WEEKLYEMAILBOTS").item(0);
					String value = element.getTextContent().trim();
					Site.WEEKLYEMAILBOTS = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("WEEKLYEMAILCHANNELS").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("WEEKLYEMAILCHANNELS").item(0);
					String value = element.getTextContent().trim();
					Site.WEEKLYEMAILCHANNELS = Boolean.valueOf(value);
				}
	
				if (root.getElementsByTagName("WEEKLYEMAILFORUMS").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("WEEKLYEMAILFORUMS").item(0);
					String value = element.getTextContent().trim();
					Site.WEEKLYEMAILFORUMS = Boolean.valueOf(value);
				}
				
				if (root.getElementsByTagName("EMAILHOST").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("EMAILHOST").item(0);
					String value = element.getTextContent().trim();
					Site.EMAILHOST = value;
				}
				
				if (root.getElementsByTagName("EMAILSALES").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("EMAILSALES").item(0);
					String value = element.getTextContent().trim();
					Site.EMAILSALES = value;
				}
				
				if (root.getElementsByTagName("EMAILPAYPAL").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("EMAILPAYPAL").item(0);
					String value = element.getTextContent().trim();
					Site.EMAILPAYPAL = value;
				}
				
				if (root.getElementsByTagName("SIGNATURE").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("SIGNATURE").item(0);
					String value = element.getTextContent().trim();
					Site.SIGNATURE = value;
				}
				
				if (root.getElementsByTagName("EMAILBOT").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("EMAILBOT").item(0);
					String value = element.getTextContent().trim();
					Site.EMAILBOT = value;
				}
				
				if (root.getElementsByTagName("EMAILSMTPHost").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("EMAILSMTPHost").item(0);
					String value = element.getTextContent().trim();
					Site.EMAILSMTPHost = value;
				}
				
				if (root.getElementsByTagName("EMAILSMTPPORT").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("EMAILSMTPPORT").item(0);
					String value = element.getTextContent().trim();
					Site.EMAILSMTPPORT = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("EMAILUSER").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("EMAILUSER").item(0);
					String value = element.getTextContent().trim();
					Site.EMAILUSER = value;
				}
				
				if (root.getElementsByTagName("EMAILPASSWORD").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("EMAILPASSWORD").item(0);
					String value = element.getTextContent().trim();
					// Check if encrypted from && prefix.
					if (value.startsWith("__")) {
						try {
							Site.EMAILPASSWORD = Utils.decrypt(Site.KEY2, value.substring(2, value.length()));
						} catch (Exception exception) {
							Site.EMAILPASSWORD = value;
						}
					} else {
						Site.EMAILPASSWORD = value;
					}
				}
	
				if (root.getElementsByTagName("EMAILSSL").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("EMAILSSL").item(0);
					String value = element.getTextContent().trim();
					Site.EMAILSSL = Boolean.valueOf(value);
				}
				
				if (root.getElementsByTagName("MEMORYLIMIT").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("MEMORYLIMIT").item(0);
					String value = element.getTextContent().trim();
					Site.MEMORYLIMIT = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("MAX_PROCCESS_TIME").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("MAX_PROCCESS_TIME").item(0);
					String value = element.getTextContent().trim();
					Site.MAX_PROCCESS_TIME = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("CONTENT_LIMIT").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("CONTENT_LIMIT").item(0);
					String value = element.getTextContent().trim();
					Site.CONTENT_LIMIT = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("MAX_CREATES_PER_IP").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("MAX_CREATES_PER_IP").item(0);
					String value = element.getTextContent().trim();
					Site.MAX_CREATES_PER_IP = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("MAX_USER_MESSAGES").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("MAX_USER_MESSAGES").item(0);
					String value = element.getTextContent().trim();
					Site.MAX_USER_MESSAGES = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("MAX_UPLOAD_SIZE").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("MAX_UPLOAD_SIZE").item(0);
					String value = element.getTextContent().trim();
					Site.MAX_UPLOAD_SIZE = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("MAX_LIVECHAT_MESSAGES").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("MAX_LIVECHAT_MESSAGES").item(0);
					String value = element.getTextContent().trim();
					Site.MAX_LIVECHAT_MESSAGES = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("MAX_ATTACHMENTS").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("MAX_ATTACHMENTS").item(0);
					String value = element.getTextContent().trim();
					Site.MAX_ATTACHMENTS = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("MAX_TRANSLATIONS").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("MAX_TRANSLATIONS").item(0);
					String value = element.getTextContent().trim();
					Site.MAX_TRANSLATIONS = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("URL_TIMEOUT").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("URL_TIMEOUT").item(0);
					String value = element.getTextContent().trim();
					Site.URL_TIMEOUT = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("MAX_API").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("MAX_API").item(0);
					String value = element.getTextContent().trim();
					Site.MAX_API = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("MAX_BRONZE").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("MAX_BRONZE").item(0);
					String value = element.getTextContent().trim();
					Site.MAX_BRONZE = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("MAX_GOLD").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("MAX_GOLD").item(0);
					String value = element.getTextContent().trim();
					Site.MAX_GOLD = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("MAX_PLATINUM").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("MAX_PLATINUM").item(0);
					String value = element.getTextContent().trim();
					Site.MAX_PLATINUM = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("MAX_BOT_CACHE_SIZE").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("MAX_BOT_CACHE_SIZE").item(0);
					String value = element.getTextContent().trim();
					Site.MAX_BOT_CACHE_SIZE = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("MAX_BOT_POOL_SIZE").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("MAX_BOT_POOL_SIZE").item(0);
					String value = element.getTextContent().trim();
					Site.MAX_BOT_POOL_SIZE = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("MAXTWEETIMPORT").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("MAXTWEETIMPORT").item(0);
					String value = element.getTextContent().trim();
					Site.MAXTWEETIMPORT = Integer.valueOf(value);
				}
				
				if (root.getElementsByTagName("TWITTER_OAUTHKEY").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("TWITTER_OAUTHKEY").item(0);
					String value = element.getTextContent().trim();
					// Check if encrypted from && prefix.
					if (value.startsWith("__")) {
						try {
							Site.TWITTER_OAUTHKEY = Utils.decrypt(Site.KEY2, value.substring(2, value.length()));
						} catch (Exception exception) {
							Site.TWITTER_OAUTHKEY = value;
						}
					} else {
						Site.TWITTER_OAUTHKEY = value;
					}
				}
				
				if (root.getElementsByTagName("TWITTER_OAUTHSECRET").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("TWITTER_OAUTHSECRET").item(0);
					String value = element.getTextContent().trim();
					// Check if encrypted from && prefix.
					if (value.startsWith("__")) {
						try {
							Site.TWITTER_OAUTHSECRET = Utils.decrypt(Site.KEY2, value.substring(2, value.length()));
						} catch (Exception exception) {
							Site.TWITTER_OAUTHSECRET = value;
						}
					} else {
						Site.TWITTER_OAUTHSECRET = value;
					}
				}
				
				if (root.getElementsByTagName("FACEBOOK_APPID").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("FACEBOOK_APPID").item(0);
					String value = element.getTextContent().trim();
					// Check if encrypted from && prefix.
					if (value.startsWith("__")) {
						try {
							Site.FACEBOOK_APPID = Utils.decrypt(Site.KEY2, value.substring(2, value.length()));
						} catch (Exception exception) {
							Site.FACEBOOK_APPID = value;
						}
					} else {
						Site.FACEBOOK_APPID = value;
					}
				}
				
				if (root.getElementsByTagName("FACEBOOK_APPSECRET").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("FACEBOOK_APPSECRET").item(0);
					String value = element.getTextContent().trim();
					// Check if encrypted from && prefix.
					if (value.startsWith("__")) {
						try {
							Site.FACEBOOK_APPSECRET = Utils.decrypt(Site.KEY2, value.substring(2, value.length()));
						} catch (Exception exception) {
							Site.FACEBOOK_APPSECRET = value;
						}
					} else {
						Site.FACEBOOK_APPSECRET = value;
					}
				}
				
				if (root.getElementsByTagName("KEY").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("KEY").item(0);
					String value = element.getTextContent().trim();
					// Check if encrypted from && prefix.
					if (value.startsWith("__")) {
						try {
							Site.KEY = Utils.decrypt(Site.KEY2, value.substring(2, value.length()));
						} catch (Exception exception) {
							Site.KEY = value;
						}
					} else {
						Site.KEY = value;
					}
				}
				
				if (root.getElementsByTagName("UPGRADE_SECRET").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("UPGRADE_SECRET").item(0);
					String value = element.getTextContent().trim();
					// Check if encrypted from && prefix.
					if (value.startsWith("__")) {
						try {
							Site.UPGRADE_SECRET = Utils.decrypt(Site.KEY2, value.substring(2, value.length()));
						} catch (Exception exception) {
							Site.UPGRADE_SECRET = value;
						}
					} else {
						Site.UPGRADE_SECRET = value;
					}
				}
				
				if (root.getElementsByTagName("GOOGLEKEY").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("GOOGLEKEY").item(0);
					String value = element.getTextContent().trim();
					// Check if encrypted from && prefix.
					if (value.startsWith("__")) {
						try {
							Site.GOOGLEKEY = Utils.decrypt(Site.KEY2, value.substring(2, value.length()));
						} catch (Exception exception) {
							Site.GOOGLEKEY = value;
						}
					} else {
						Site.GOOGLEKEY = value;
					}
				}
				
				if (root.getElementsByTagName("GOOGLECLIENTID").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("GOOGLECLIENTID").item(0);
					String value = element.getTextContent().trim();
					// Check if encrypted from && prefix.
					if (value.startsWith("__")) {
						try {
							Site.GOOGLECLIENTID = Utils.decrypt(Site.KEY2, value.substring(2, value.length()));
						} catch (Exception exception) {
							Site.GOOGLECLIENTID = value;
						}
					} else {
						Site.GOOGLECLIENTID = value;
					}
				}
				
				if (root.getElementsByTagName("GOOGLECLIENTSECRET").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("GOOGLECLIENTSECRET").item(0);
					String value = element.getTextContent().trim();
					// Check if encrypted from && prefix.
					if (value.startsWith("__")) {
						try {
							Site.GOOGLECLIENTSECRET = Utils.decrypt(Site.KEY2, value.substring(2, value.length()));
						} catch (Exception exception) {
							Site.GOOGLECLIENTSECRET = value;
						}
					} else {
						Site.GOOGLECLIENTSECRET = value;
					}
				}
				
				if (root.getElementsByTagName("MICROSOFT_SPEECH_KEY").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("MICROSOFT_SPEECH_KEY").item(0);
					String value = element.getTextContent().trim();
					// Check if encrypted from && prefix.
					if (value.startsWith("__")) {
						try {
							Site.MICROSOFT_SPEECH_KEY = Utils.decrypt(Site.KEY2, value.substring(2, value.length()));
						} catch (Exception exception) {
							Site.MICROSOFT_SPEECH_KEY = value;
						}
					} else {
						Site.MICROSOFT_SPEECH_KEY = value;
					}
				}
				
				if (root.getElementsByTagName("RESPONSIVEVOICE_KEY").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("RESPONSIVEVOICE_KEY").item(0);
					String value = element.getTextContent().trim();
					// Check if encrypted from && prefix.
					if (value.startsWith("__")) {
						try {
							Site.RESPONSIVEVOICE_KEY = Utils.decrypt(Site.KEY2, value.substring(2, value.length()));
						} catch (Exception exception) {
							Site.RESPONSIVEVOICE_KEY = value;
						}
					} else {
						Site.RESPONSIVEVOICE_KEY = value;
					}
				}
				
				if (root.getElementsByTagName("YANDEX_KEY").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("YANDEX_KEY").item(0);
					String value = element.getTextContent().trim();
					// Check if encrypted from && prefix.
					if (value.startsWith("__")) {
						try {
							Site.YANDEX_KEY = Utils.decrypt(Site.KEY2, value.substring(2, value.length()));
						} catch (Exception exception) {
							Site.YANDEX_KEY = value;
						}
					} else {
						Site.YANDEX_KEY = value;
					}
				}
				
			} catch (Exception exception) {
				log(Level.SEVERE, "Parsing botplatform.xml bootstrap file failed");
				log(exception);
			}
		} catch (Exception exception) {
			log(Level.INFO, "Invalid botplatform.xml bootstrap file, using default settings");
			log(exception);
		}
	}
	
	/**
	 * Load the server database settings from "conf/botlibre.xml".
	 * This allows the server configuration to be changed without requiring rebuilding/deploying.
	 * Basic encryption is used for the server password.
	 */
	public void restorePlatformDatabaseSettings() {
		// use legacy botplatform.xml if it exists
		File oldFile = new File("botplatform.xml");
		if (oldFile.exists()) {
			oldRestorePlatformSettings();
			return;
		}
		
		// load database password and persistence unit from conf/botlibre.xml
		File confFile = new File("../conf/botplatform.xml");
		if (!confFile.exists()) {
			log(Level.INFO, "conf/botplatform.xml not found, using default database password settings");
		}
		else {
			try {
				// Read config xml to initialize plugins.
				log(Level.INFO, "Loading file conf/botplatform.xml");
				
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder parser = factory.newDocumentBuilder();
				Document document = parser.parse(confFile);
				Element root = document.getDocumentElement();
		
				// Parse and initialize settings.
				
				if (root.getElementsByTagName("DATABASEUSER").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("DATABASEUSER").item(0);
					String value = element.getTextContent().trim();
					Site.DATABASEUSER = value;
				}
				
				if (root.getElementsByTagName("DATABASEPASSWORD").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("DATABASEPASSWORD").item(0);
					String value = element.getTextContent().trim();
					Site.DATABASEPASSWORD = decryptIfEncrypted(value);
				}
				
				if (root.getElementsByTagName("PERSISTENCE_PROTOCOL").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("PERSISTENCE_PROTOCOL").item(0);
					String value = element.getTextContent().trim();
					Site.PERSISTENCE_PROTOCOL = value;
				}
				
				if (root.getElementsByTagName("PERSISTENCE_HOST").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("PERSISTENCE_HOST").item(0);
					String value = element.getTextContent().trim();
					Site.PERSISTENCE_HOST = value;
				}
				
				if (root.getElementsByTagName("PERSISTENCE_PORT").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("PERSISTENCE_PORT").item(0);
					String value = element.getTextContent().trim();
					Site.PERSISTENCE_PORT = value;
				}
				
				if (root.getElementsByTagName("PERSISTENCE_UNIT").getLength() > 0) {
					Element element = (Element) root.getElementsByTagName("PERSISTENCE_UNIT").item(0);
					String value = element.getTextContent().trim();
					Site.PERSISTENCE_UNIT = value;
				}
				
			} catch (Exception exception) {
				log(Level.INFO, "Invalid conf/botplatform.xml file, using default database settings");
				log(exception);
			}
		}
	}
	
	/**
	 * Load the server configuration properties from the database.
	 * This allows the server configuration to be changed without requiring rebuilding/deploying.
	 * Basic encryption is used for passwords/keys.
	 */
	public void restorePlatformOtherSettings() {
		// use legacy botplatform.xml if it exists
		File oldFile = new File("botplatform.xml");
		if (oldFile.exists()) {
			oldRestorePlatformSettings();
			return;
		}
		
		log(Level.INFO, "Loading platform settings from database");
		
		EntityManager em = getFactory().createEntityManager();
		BotPlatform botPlatform = em.find(BotPlatform.class, 1);
		
		if (botPlatform == null) {
			log(Level.INFO, "Missing database entry for platform settings, using default settings");
			return;
		}
		
		if (DATABASEFAILURE) {
			log(Level.INFO, "Database failure, using default setings");
			return;
		}
		
		try {
			Site.URL_PREFIX = botPlatform.URL_PREFIX;
			Site.URL_SUFFIX = botPlatform.URL_SUFFIX;
			Site.SERVER_NAME = botPlatform.SERVER_NAME;
			Site.SERVER_NAME2 = botPlatform.SERVER_NAME2;
			Site.URL = botPlatform.URL;
			Site.URLLINK = botPlatform.URLLINK;
			Site.SECUREURLLINK = botPlatform.SECUREURLLINK;
			Site.REDIRECT = botPlatform.REDIRECT;
			Site.HTTPS = botPlatform.HTTPS;
			Site.PYTHONSERVER = botPlatform.PYTHONSERVER;
			Site.BOOTSTRAP = botPlatform.BOOTSTRAP;
			Site.LOCK = botPlatform.LOCK;
			Site.READONLY = botPlatform.READONLY;
			Site.ADULT = botPlatform.ADULT;
			Site.CONTENT_RATING = ContentRating.valueOf(botPlatform.CONTENT_RATING);
			Site.NAME = botPlatform.NAME;
			Site.DOMAIN = botPlatform.DOMAIN;
			Site.ID = botPlatform.ID;
			Site.PREFIX = botPlatform.PREFIX;
			Site.HASHTAG = botPlatform.HASHTAG;
			Site.TYPE = botPlatform.TYPE;
			Site.TWITTER = botPlatform.TWITTER;
			Site.FACEBOOK = botPlatform.FACEBOOK;
			Site.TELEGRAM = botPlatform.TELEGRAM;
			Site.SLACK = botPlatform.SLACK;
			Site.SLACK = botPlatform.SLACK;
			Site.SKYPE = botPlatform.SKYPE;
			Site.WECHAT = botPlatform.WECHAT;
			Site.KIK = botPlatform.KIK;
			Site.EMAIL = botPlatform.EMAIL;
			Site.TIMERS = botPlatform.TIMERS;
			Site.FORGET = botPlatform.FORGET;
			Site.ADMIN = botPlatform.ADMIN;
			Site.VERIFYUSERS = botPlatform.VERIFYUSERS;
			Site.DEDICATED = botPlatform.DEDICATED;
			Site.CLOUD = botPlatform.CLOUD;
			Site.COMMERCIAL = botPlatform.COMMERCIAL;
			Site.ALLOW_SIGNUP = botPlatform.ALLOW_SIGNUP;
			Site.VERIFY_EMAIL = botPlatform.VERIFY_EMAIL;
			Site.ANONYMOUS_CHAT = botPlatform.ANONYMOUS_CHAT;
			Site.REQUIRE_TERMS = botPlatform.REQUIRE_TERMS;
			Site.AGE_RESTRICT = botPlatform.AGE_RESTRICT;
			Site.BACKLINK = botPlatform.BACKLINK;
			Site.WEEKLYEMAIL = botPlatform.WEEKLYEMAIL;
			Site.WEEKLYEMAILBOTS = botPlatform.WEEKLYEMAILBOTS;
			Site.WEEKLYEMAILCHANNELS = botPlatform.WEEKLYEMAILCHANNELS;
			Site.WEEKLYEMAILFORUMS = botPlatform.WEEKLYEMAILFORUMS;
			Site.EMAILHOST = botPlatform.EMAILHOST;
			Site.EMAILSALES = botPlatform.EMAILSALES;
			Site.EMAILPAYPAL = botPlatform.EMAILPAYPAL;
			Site.SIGNATURE = botPlatform.SIGNATURE;
			Site.EMAILBOT = botPlatform.EMAILBOT;
			Site.EMAILSMTPHost = botPlatform.EMAILSMTPHost;
			Site.EMAILSMTPPORT = botPlatform.EMAILSMTPPORT;
			Site.EMAILUSER = botPlatform.EMAILUSER;
			Site.EMAILPASSWORD = decryptIfEncrypted(botPlatform.EMAILPASSWORD);
			Site.EMAILSSL = botPlatform.EMAILSSL;
			Site.MEMORYLIMIT = botPlatform.MEMORYLIMIT;
			Site.MAX_PROCCESS_TIME = botPlatform.MAX_PROCCESS_TIME;
			Site.CONTENT_LIMIT = botPlatform.CONTENT_LIMIT;
			Site.MAX_CREATES_PER_IP = botPlatform.MAX_CREATES_PER_IP;
			Site.MAX_USER_MESSAGES = botPlatform.MAX_USER_MESSAGES;
			Site.MAX_UPLOAD_SIZE = botPlatform.MAX_UPLOAD_SIZE;
			Site.MAX_LIVECHAT_MESSAGES = botPlatform.MAX_LIVECHAT_MESSAGES;
			Site.MAX_ATTACHMENTS = botPlatform.MAX_ATTACHMENTS;
			Site.MAX_TRANSLATIONS = botPlatform.MAX_TRANSLATIONS;
			Site.URL_TIMEOUT = botPlatform.URL_TIMEOUT;
			Site.MAX_API = botPlatform.MAX_API;
			Site.MAX_BRONZE = botPlatform.MAX_BRONZE;
			Site.MAX_GOLD = botPlatform.MAX_GOLD;
			Site.MAX_PLATINUM = botPlatform.MAX_PLATINUM;
			Site.MAX_BOT_CACHE_SIZE = botPlatform.MAX_BOT_CACHE_SIZE;
			Site.MAX_BOT_POOL_SIZE = botPlatform.MAX_BOT_POOL_SIZE;
			Site.MAXTWEETIMPORT = botPlatform.MAXTWEETIMPORT;
			Site.TWITTER_OAUTHKEY = decryptIfEncrypted(botPlatform.TWITTER_OAUTHKEY);
			Site.TWITTER_OAUTHSECRET = decryptIfEncrypted(botPlatform.TWITTER_OAUTHSECRET);
			Site.FACEBOOK_APPID = decryptIfEncrypted(botPlatform.FACEBOOK_APPID);
			Site.FACEBOOK_APPSECRET = decryptIfEncrypted(botPlatform.FACEBOOK_APPSECRET);
			Site.KEY = decryptIfEncrypted(botPlatform.KEY);
			Site.UPGRADE_SECRET = decryptIfEncrypted(botPlatform.UPGRADE_SECRET);
			Site.GOOGLEKEY = decryptIfEncrypted(botPlatform.GOOGLEKEY);
			Site.GOOGLECLIENTID = decryptIfEncrypted(botPlatform.GOOGLECLIENTID);
			Site.GOOGLECLIENTSECRET = decryptIfEncrypted(botPlatform.GOOGLECLIENTSECRET);
			Site.MICROSOFT_SPEECH_KEY = decryptIfEncrypted(botPlatform.MICROSOFT_SPEECH_KEY);
			Site.RESPONSIVEVOICE_KEY = decryptIfEncrypted(botPlatform.RESPONSIVEVOICE_KEY);
			Site.YANDEX_KEY = decryptIfEncrypted(botPlatform.YANDEX_KEY);
			
		} catch (Exception exception) {
			log(Level.INFO, "Invalid database entry for platform settings, using default settings");
			log(exception);
		}
	}
	
	/**
	 * Load the server configuration properties from the database, and database settings from "conf/botlibre.xml".
	 * This allows the server configuration to be changed without requiring rebuilding/deploying.
	 * Basic encryption is used for passwords/keys.
	 */
	public void restorePlatformSettings() {
		restorePlatformDatabaseSettings();
		restorePlatformOtherSettings();
	}
	
	private String decryptIfEncrypted(String inp) {
		if (!inp.startsWith("__")) {
			return inp;
		}
		try {
			return Utils.decrypt(Site.KEY2, inp.substring(2, inp.length()));
		} catch (Exception exception) {
			return inp;
		}
	}
	
	/**
	 * Save the server configuration properties to the local xml file "botplatform.xml".
	 * This allows the server configuration to be changed without requiring rebuilding/deploying.
	 * Basic encryption is used for passwords/keys.
	 */
	public void updatePlatformSettings() {
		log(Level.INFO, "Saving database password and persistence unit to conf/botplatform.xml");
		
		File file = new File("../conf/botplatform.xml");
		if (file.exists()) {
			file.delete();
		}
		try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
			writer.write("<platform-settings>\n");
			
			writer.write("\t<DATABASEUSER>");
			writer.write(Site.DATABASEUSER);
			writer.write("</DATABASEUSER>\n");
			
			writer.write("\t<DATABASEPASSWORD>");
			writer.write("__" + Utils.encrypt(Site.KEY2, Site.DATABASEPASSWORD));
			writer.write("</DATABASEPASSWORD>\n");
			
			writer.write("\t<PERSISTENCE_PROTOCOL>");
			writer.write(Site.PERSISTENCE_PROTOCOL);
			writer.write("</PERSISTENCE_PROTOCOL>\n");
			
			writer.write("\t<PERSISTENCE_HOST>");
			writer.write(Site.PERSISTENCE_HOST);
			writer.write("</PERSISTENCE_HOST>\n");
			
			writer.write("\t<PERSISTENCE_PORT>");
			writer.write(Site.PERSISTENCE_PORT);
			writer.write("</PERSISTENCE_PORT>\n");
			
			writer.write("\t<PERSISTENCE_UNIT>");
			writer.write(Site.PERSISTENCE_UNIT);
			writer.write("</PERSISTENCE_UNIT>\n");
			
			writer.write("</platform-settings>\n");
			
			writer.flush();
		} catch (Exception exception) {
			log(Level.SEVERE, "Error writing conf/botplatform.xml");
			log(exception);
		}
		
		
		log(Level.INFO, "Saving platform settings to database");
		
		BotPlatform botPlatform = new BotPlatform();
		botPlatform.URL_PREFIX = Site.URL_PREFIX;
		botPlatform.URL_SUFFIX = Site.URL_SUFFIX;
		botPlatform.SERVER_NAME = Site.SERVER_NAME;
		botPlatform.SERVER_NAME2 = Site.SERVER_NAME2;
		botPlatform.URL = Site.URL;
		botPlatform.URLLINK = Site.URLLINK;
		botPlatform.SECUREURLLINK = Site.SECUREURLLINK;
		botPlatform.SANDBOXURLLINK = Site.SANDBOXURLLINK;
		botPlatform.REDIRECT = Site.REDIRECT;
		botPlatform.HTTPS = Site.HTTPS;
		botPlatform.PYTHONSERVER = Site.PYTHONSERVER;
		botPlatform.BOOTSTRAP = Site.BOOTSTRAP;
		botPlatform.LOCK = Site.LOCK;
		botPlatform.READONLY = Site.READONLY;
		botPlatform.ADULT = Site.ADULT;
		botPlatform.CONTENT_RATING = Site.CONTENT_RATING.toString();
		botPlatform.NAME = Site.NAME;
		botPlatform.DOMAIN = Site.DOMAIN;
		botPlatform.ID = Site.ID;
		botPlatform.PREFIX = Site.PREFIX;
		botPlatform.HASHTAG = Site.HASHTAG;
		botPlatform.TYPE = Site.TYPE;
		botPlatform.TWITTER = Site.TWITTER;
		botPlatform.FACEBOOK = Site.FACEBOOK;
		botPlatform.TELEGRAM = Site.TELEGRAM;
		botPlatform.SLACK = Site.SLACK;
		botPlatform.SKYPE = Site.SKYPE;
		botPlatform.WECHAT = Site.WECHAT;
		botPlatform.KIK = Site.KIK;
		botPlatform.EMAIL = Site.EMAIL;
		botPlatform.TIMERS = Site.TIMERS;
		botPlatform.FORGET = Site.FORGET;
		botPlatform.ADMIN = Site.ADMIN;
		botPlatform.VERIFYUSERS = Site.VERIFYUSERS;
		botPlatform.DEDICATED = Site.DEDICATED;
		botPlatform.CLOUD = Site.CLOUD;
		botPlatform.COMMERCIAL = Site.COMMERCIAL;
		botPlatform.ALLOW_SIGNUP = Site.ALLOW_SIGNUP;
		botPlatform.VERIFY_EMAIL = Site.VERIFY_EMAIL;
		botPlatform.ANONYMOUS_CHAT = Site.ANONYMOUS_CHAT;
		botPlatform.REQUIRE_TERMS = Site.REQUIRE_TERMS;
		botPlatform.AGE_RESTRICT = Site.AGE_RESTRICT;
		botPlatform.BACKLINK = Site.BACKLINK;
		botPlatform.WEEKLYEMAIL = Site.WEEKLYEMAIL;
		botPlatform.WEEKLYEMAILBOTS = Site.WEEKLYEMAILBOTS;
		botPlatform.WEEKLYEMAILCHANNELS = Site.WEEKLYEMAILCHANNELS;
		botPlatform.WEEKLYEMAILFORUMS = Site.WEEKLYEMAILFORUMS;
		botPlatform.EMAILHOST = Site.EMAILHOST;
		botPlatform.EMAILSALES = Site.EMAILSALES;
		botPlatform.EMAILPAYPAL = Site.EMAILPAYPAL;
		botPlatform.SIGNATURE = Site.SIGNATURE;
		botPlatform.EMAILBOT = Site.EMAILBOT;
		botPlatform.EMAILSMTPHost = Site.EMAILSMTPHost;
		botPlatform.EMAILSMTPPORT = Site.EMAILSMTPPORT;
		botPlatform.EMAILUSER = Site.EMAILUSER;
		botPlatform.EMAILPASSWORD = "__" + Utils.encrypt(Site.KEY2, Site.EMAILPASSWORD);
		botPlatform.EMAILSSL = Site.EMAILSSL;
		botPlatform.MEMORYLIMIT = Site.MEMORYLIMIT;
		botPlatform.MAX_PROCCESS_TIME = Site.MAX_PROCCESS_TIME;
		botPlatform.CONTENT_LIMIT = Site.CONTENT_LIMIT;
		botPlatform.MAX_CREATES_PER_IP = Site.MAX_CREATES_PER_IP;
		botPlatform.MAX_USER_MESSAGES = Site.MAX_USER_MESSAGES;
		botPlatform.MAX_UPLOAD_SIZE = Site.MAX_UPLOAD_SIZE;
		botPlatform.MAX_LIVECHAT_MESSAGES = Site.MAX_LIVECHAT_MESSAGES;
		botPlatform.MAX_ATTACHMENTS = Site.MAX_ATTACHMENTS;
		botPlatform.MAX_TRANSLATIONS = Site.MAX_TRANSLATIONS;
		botPlatform.URL_TIMEOUT = Site.URL_TIMEOUT;
		botPlatform.MAX_API = Site.MAX_API;
		botPlatform.MAX_BRONZE = Site.MAX_BRONZE;
		botPlatform.MAX_GOLD = Site.MAX_GOLD;
		botPlatform.MAX_PLATINUM = Site.MAX_PLATINUM;
		botPlatform.MAX_BOT_CACHE_SIZE = Site.MAX_BOT_CACHE_SIZE;
		botPlatform.MAX_BOT_POOL_SIZE = Site.MAX_BOT_POOL_SIZE;
		botPlatform.MAXTWEETIMPORT = Site.MAXTWEETIMPORT;
		botPlatform.TWITTER_OAUTHKEY = "__" + Utils.encrypt(Site.KEY2, Site.TWITTER_OAUTHKEY);
		botPlatform.TWITTER_OAUTHSECRET = "__" + Utils.encrypt(Site.KEY2, Site.TWITTER_OAUTHSECRET);
		botPlatform.FACEBOOK_APPID = "__" + Utils.encrypt(Site.KEY2, Site.FACEBOOK_APPID);
		botPlatform.FACEBOOK_APPSECRET = "__" + Utils.encrypt(Site.KEY2, Site.FACEBOOK_APPSECRET);
		botPlatform.KEY = "__" + Utils.encrypt(Site.KEY2, Site.KEY);
		botPlatform.UPGRADE_SECRET = "__" + Utils.encrypt(Site.KEY2, Site.UPGRADE_SECRET);
		botPlatform.GOOGLEKEY = "__" + Utils.encrypt(Site.KEY2, Site.GOOGLEKEY);
		botPlatform.GOOGLECLIENTID = "__" + Utils.encrypt(Site.KEY2, Site.GOOGLECLIENTID);
		botPlatform.GOOGLECLIENTSECRET = "__" + Utils.encrypt(Site.KEY2, Site.GOOGLECLIENTSECRET);
		botPlatform.MICROSOFT_SPEECH_KEY = "__" + Utils.encrypt(Site.KEY2, Site.MICROSOFT_SPEECH_KEY);
		botPlatform.RESPONSIVEVOICE_KEY = "__" + Utils.encrypt(Site.KEY2, Site.RESPONSIVEVOICE_KEY);
		botPlatform.YANDEX_KEY = "__" + Utils.encrypt(Site.KEY2, Site.YANDEX_KEY);
		
		EntityManager em = getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			em.merge(botPlatform);
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}
	
	public List<String> getLanguages() {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select distinct t.targetLanguage from Translation t");
			query.setHint("eclipselink.read-only", "true");
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public boolean checkLanguageTranslation(String language) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select count(t) from Translation t where t.targetLanguage = :language");
			query.setParameter("language", language);
			query.setHint("eclipselink.read-only", "true");
			int count = ((Number)query.getSingleResult()).intValue();
			return count > 0;
		} finally {
			em.close();
		}
	}
	
	public List<ReferrerStats> getAllReferStats(String refer) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select s from ReferrerStats s where s.page = :refer order by s.date desc");
			query.setParameter("refer", refer);
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(100);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<AppIDStats> getAllAppIDStats(String appID) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select s from AppIDStats s where s.appID = :appID order by s.date desc");
			query.setParameter("appID", appID);
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(100);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<PageStats> getAllPageStats(String page) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select s from PageStats s where s.page = :page order by s.date desc");
			query.setParameter("page", page);
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(100);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<ErrorStats> getAllErrorStats(String message) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select s from ErrorStats s where s.message = :message order by s.date desc");
			query.setParameter("message", message);
			query.setHint("eclipselink.read-only", "true");
			query.setMaxResults(100);
			return query.getResultList();
		} finally {
			em.close();
		}
	}
	
	/**
	 * This is no longer used.
	 */
	public void resetTagInstanceCount(Domain domain) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Query query = em.createNativeQuery("Update Tag t set count = (Select count(p) from BotInstance p join PANODRAINSTANCE_TAGS pt on (pt.BotInstance_id = p.id) join Tag t2 on (pt.tags_id = t2.id) where p.domain_id = ? and t2.id = t.id) where t.type = 'Bot' and t.domain_id = ?");
			query.setParameter(1, domain.getId());
			query.setParameter(2, domain.getId());
			query.executeUpdate();
			query = em.createNativeQuery("Update Tag t set count = (Select count(p) from Forum p join FORUM_TAGS pt on (pt.Forum_id = p.id) join Tag t2 on (pt.tags_id = t2.id) where p.domain_id = ? and t2.id = t.id) where t.type = 'Forum' and t.domain_id = ?");
			query.setParameter(1, domain.getId());
			query.setParameter(2, domain.getId());
			query.executeUpdate();
			query = em.createNativeQuery("Update Tag t set count = (Select count(p) from ForumPost p join FORUMPOST_TAGS pt on (pt.ForumPost_id = p.id) join Tag t2 on (pt.tags_id = t2.id) where p.domain_id = ? and t2.id = t.id) where t.type = 'Post' and t.domain_id = ?");
			query.setParameter(1, domain.getId());
			query.setParameter(2, domain.getId());
			query.executeUpdate();
			query = em.createNativeQuery("Update Tag t set count = (Select count(p) from ChatChannel p join CHAT_TAGS pt on (pt.ChatChannel_id = p.id) join Tag t2 on (pt.tags_id = t2.id) where p.domain_id = ? and t2.id = t.id) where t.type = 'Channel' and t.domain_id = ?");
			query.setParameter(1, domain.getId());
			query.setParameter(2, domain.getId());
			query.executeUpdate();
			query = em.createQuery("Update Tag t set t.type = 'Post' where t.count = 0 and t.domain = :domain");
			query.setParameter("domain", domain);
			query.executeUpdate();
			query = em.createNativeQuery("Update Tag t set count = (Select count(p) from ForumPost p join FORUMPOST_TAGS pt on (pt.ForumPost_id = p.id) join Tag t2 on (pt.tags_id = t2.id) where p.domain_id = ? and t2.id = t.id) where t.type = 'Post' and t.domain_id = ?");
			query.setParameter(1, domain.getId());
			query.setParameter(2, domain.getId());
			query.executeUpdate();
			query = em.createQuery("Delete from Tag t where t.count = 0 and t.domain = :domain");
			query.setParameter("domain", domain);
			query.executeUpdate();
			em.getTransaction().commit();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User createUser(User user) {
		log(Level.INFO, "create user",  user.getUserId());
		if (user.checkProfanity()) {
			throw BotException.offensive();
		}
		user.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			if (em.find(User.class, user.getUserId()) != null) {
				throw new BotException("User already exists - " + user.getUserId());
			}
			user.setCreationDate(new Date());
			if (user.getAffiliate() != null || !user.getAffiliate().isEmpty()) {
				User affiliate = em.find(User.class, user.getAffiliate());
				if (affiliate != null) {
					Query query = em.createQuery("Select count(u) from User u where u.affiliate = :affiliate");
					query.setParameter("affiliate", affiliate.getUserId());
					int count = ((Number)query.getSingleResult()).intValue();
					affiliate.setAffiliates(count + 1);
				}
			}
			em.persist(user);
			user.resetToken();
			user.setEncryptedPassword(encrypt(user.getUserId(), user.getPassword()));
			user.setPassword(null);
			em.getTransaction().commit();
			user.setEncryptedPassword(null);
			return user;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public byte[] encrypt(String user, String password) {
		if (password == null) {
			return null;
		}
		try {
			// Encrypt the userid using the password, so the password is never stored.
			String passphrase = password;
			MessageDigest digest = MessageDigest.getInstance("SHA");
			digest.update(passphrase.getBytes());
			SecretKeySpec key = new SecretKeySpec(digest.digest(), 0, 16, "AES");
			Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
			aes.init(Cipher.ENCRYPT_MODE, key);
			byte[] ciphertext = aes.doFinal(user.getBytes());
			return ciphertext;
		} catch (Exception failed) {
			log(failed);
			return null;
		}
	}
	
	public User updateUser(User newUser, String oldPassword, boolean reset, String tags) {
		log(Level.INFO, "update user",  newUser.getUserId());
		newUser.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User user = em.find(User.class, newUser.getUserId());
			if (user == null) {
				throw new BotException("User does not exist - " + newUser.getUserId());
			}
			AvatarImage avatar = user.getAvatar();
			byte[] pwd = user.getEncryptedPassword();
			user = em.merge(newUser);
			if (tags != null) {
				user.setTagsFromString(tags, em);
			}
			user.setEncryptedPassword(pwd);
			user.setAvatar(avatar);
			if (!newUser.getPassword().isEmpty()) {
				if (!reset && !Arrays.equals(user.getEncryptedPassword(), encrypt(newUser.getUserId(), oldPassword))) {
					throw new BotException("Invalid password");
				}
				user.setEncryptedPassword(encrypt(newUser.getUserId(), newUser.getPassword()));
			}
			user.setPassword(null);
			em.getTransaction().commit();
			user.setEncryptedPassword(null);
			return user;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User updateUserIP(String userid, String ip) {
		log(Level.INFO, "update user ip",  userid);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User user = em.find(User.class, userid);
			if (user == null) {
				throw new BotException("User does not exist - " + userid);
			}
			user.setIP(ip);
			em.getTransaction().commit();
			user.setPassword(null);
			user.setEncryptedPassword(null);
			return user;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Category updateCategory(Category category, String parents) {
		log(Level.INFO, "update category", category);
		if (!category.getDomain().isAdult() && (Utils.checkProfanity(category.getDescription()))) {
			throw BotException.offensive();
		}
		category.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Category existingCategory = em.find(Category.class, category.getId());
			if (existingCategory == null) {
				throw new BotException("Category does not exist - " + category.getId());
			}
			existingCategory.setSecured(category.isSecured());
			existingCategory.setName(category.getName());
			existingCategory.setDescription(category.getDescription());
			existingCategory.setParentsFromString(parents, em);
			em.getTransaction().commit();
			return existingCategory;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Translation updateTranslation(Translation instance) {
		log(Level.INFO, "update translation", instance);
		instance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Translation existing = em.find(Translation.class, new TranslationId(instance));
			if (existing == null) {
				existing = instance;
				em.persist(instance);
			} else {
				em.merge(instance);
			}
			em.getTransaction().commit();
			return existing;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotTranslation updateBotTranslation(BotTranslation instance) {
		log(Level.INFO, "update bot translation", instance);
		instance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotTranslation existing = em.find(BotTranslation.class, new TranslationId(instance));
			if (existing == null) {
				existing = instance;
				em.persist(instance);
			} else {
				em.merge(instance);
			}
			em.getTransaction().commit();
			return existing;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public void deleteTranslation(Translation instance) {
		log(Level.INFO, "delete translation", instance);
		instance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Translation existing = em.find(Translation.class, new TranslationId(instance));
			if (existing == null) {
				throw new BotException("Missing translation - " + instance.text);
			} else {
				em.remove(existing);
			}
			em.getTransaction().commit();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance createInstance(BotInstance newInstance, User user, String categories, String tags, LoginBean bean) {
		log(Level.INFO, "create instance",  newInstance.getName());
		newInstance.getDomain().checkExpired();
		newInstance.getDomain().checkCreation(user);
		checkConstraints(newInstance, tags);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Query query = em.createQuery("Select p from BotInstance p where p.alias = :alias and p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", newInstance.getAlias());
			query.setParameter("domain", newInstance.getDomain());
			List<BotInstance> results = query.getResultList();
			if (!results.isEmpty()) {
				// Append user to alias.
				newInstance.setAlias(newInstance.getAlias() + "-" + user.getUserId());
				query.setParameter("alias", newInstance.getAlias());
				query.setParameter("domain", newInstance.getDomain());
				results = query.getResultList();
				if (!results.isEmpty()) {
					throw new BotException("Bot already exists - " + newInstance.getAlias());
				}
			}
			newInstance.setCreationDate(new Date());
			em.persist(newInstance);
			newInstance.setDatabaseId(newInstance.getId());
			User managedUser = em.find(User.class, user.getUserId());
			newInstance.getAdmins().add(managedUser);
			int count = getAllInstancesCount("", "", user.getUserId(), InstanceFilter.Personal, InstanceRestrict.None, InstanceSort.Name, ContentRating.Adult, "", user, null);
			if (count >= user.getContentLimit()) {
				throw new BotException("You must upgrade to create more than " + user.getContentLimit() + " bots");
			}
			count = count + 1;
			managedUser.setInstances(count);
			newInstance.setCreator(managedUser);
			newInstance.setCategoriesFromString(categories, em);
			newInstance.setTagsFromString(tags, em);
			newInstance.incrementConnects(ClientType.WEB, user);
			if (newInstance.getDomainForwarder() != null) {
				newInstance.getDomainForwarder().init(newInstance, bean);
			}
			em.getTransaction().commit();
			return newInstance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Forum createForum(Forum newInstance, User user, String categories, String tags, LoginBean bean) {
		log(Level.INFO, "create forum",  newInstance.getName());
		newInstance.getDomain().checkExpired();
		newInstance.getDomain().checkCreation(user);
		checkConstraints(newInstance, tags);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Query query = em.createQuery("Select p from Forum p where p.alias = :alias and p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", newInstance.getAlias());
			query.setParameter("domain", newInstance.getDomain());
			List<Forum> results = query.getResultList();
			if (!results.isEmpty()) {
				// Append user to alias.
				newInstance.setAlias(newInstance.getAlias() + "-" + user.getUserId());
				query.setParameter("alias", newInstance.getAlias());
				query.setParameter("domain", newInstance.getDomain());
				results = query.getResultList();
				if (!results.isEmpty()) {
					throw new BotException("Forum already exists - " + newInstance.getAlias());
				}
			}
			newInstance.setCreationDate(new Date());
			em.persist(newInstance);
			User managedUser = em.find(User.class, user.getUserId());
			int count = getAllForumsCount("", "", user.getUserId(), InstanceFilter.Personal, InstanceRestrict.None, InstanceSort.Name, ContentRating.Adult, "", user, null);
			if (count >= user.getContentLimit()) {
				throw new BotException("You must upgrade to create more than " + user.getContentLimit() + " forums");
			}
			count = count + 1;
			managedUser.setForums(count);
			newInstance.getAdmins().add(managedUser);
			newInstance.setCreator(managedUser);
			newInstance.setCategoriesFromString(categories, em);
			newInstance.setTagsFromString(tags, em);
			newInstance.incrementConnects(ClientType.WEB, user);
			if (newInstance.getDomainForwarder() != null) {
				newInstance.getDomainForwarder().init(newInstance, bean);
			}
			em.getTransaction().commit();
			return newInstance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public IssueTracker createIssueTracker(IssueTracker newInstance, User user, String categories, String tags, LoginBean bean) {
		log(Level.INFO, "create issuetracker",  newInstance.getName());
		newInstance.getDomain().checkExpired();
		newInstance.getDomain().checkCreation(user);
		checkConstraints(newInstance, tags);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Query query = em.createQuery("Select p from IssueTracker p where p.alias = :alias and p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", newInstance.getAlias());
			query.setParameter("domain", newInstance.getDomain());
			List<Forum> results = query.getResultList();
			if (!results.isEmpty()) {
				// Append user to alias.
				newInstance.setAlias(newInstance.getAlias() + "-" + user.getUserId());
				query.setParameter("alias", newInstance.getAlias());
				query.setParameter("domain", newInstance.getDomain());
				results = query.getResultList();
				if (!results.isEmpty()) {
					throw new BotException("IssueTracker already exists - " + newInstance.getAlias());
				}
			}
			newInstance.setCreationDate(new Date());
			em.persist(newInstance);
			User managedUser = em.find(User.class, user.getUserId());
			int count = getAllIssueTrackersCount("", "", user.getUserId(), InstanceFilter.Personal, InstanceRestrict.None, InstanceSort.Name, ContentRating.Adult, "", user, null);
			if (count >= user.getContentLimit()) {
				throw new BotException("You must upgrade to create more than " + user.getContentLimit() + " issue trackers");
			}
			count = count + 1;
			managedUser.setIssueTrackers(count);
			newInstance.getAdmins().add(managedUser);
			newInstance.setCreator(managedUser);
			newInstance.setCategoriesFromString(categories, em);
			newInstance.setTagsFromString(tags, em);
			newInstance.incrementConnects(ClientType.WEB, user);
			if (newInstance.getDomainForwarder() != null) {
				newInstance.getDomainForwarder().init(newInstance, bean);
			}
			em.getTransaction().commit();
			return newInstance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Avatar createAvatar(Avatar newInstance, User user, String categories, String tags) {
		log(Level.INFO, "create avatar",  newInstance.getName());
		newInstance.getDomain().checkExpired();
		newInstance.getDomain().checkCreation(user);
		if (!newInstance.isAdult() && (newInstance.checkProfanity() || Utils.checkProfanity(tags))) {
			throw BotException.offensive();
		}
		Utils.checkHTML(tags);
		tags = Utils.sanitize(tags);
		newInstance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Query query = em.createQuery("Select p from Avatar p where p.alias = :alias and p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", newInstance.getAlias());
			query.setParameter("domain", newInstance.getDomain());
			List<Avatar> results = query.getResultList();
			if (!results.isEmpty()) {
				// Append user to alias.
				newInstance.setAlias(newInstance.getAlias() + "-" + user.getUserId());
				query.setParameter("alias", newInstance.getAlias());
				query.setParameter("domain", newInstance.getDomain());
				results = query.getResultList();
				if (!results.isEmpty()) {
					throw new BotException("Avatar already exists - " + newInstance.getAlias());
				}
			}
			newInstance.setCreationDate(new Date());
			em.persist(newInstance);
			User managedUser = em.find(User.class, user.getUserId());
			int count = getAllAvatarsCount("", "", user.getUserId(), InstanceFilter.Personal, InstanceRestrict.None, InstanceSort.Name, ContentRating.Adult, "", user, null, false);
			if (count >= user.getContentLimit()) {
				throw new BotException("You must upgrade to create more than " + user.getContentLimit() + " avatars");
			}
			count = count + 1;
			managedUser.setAvatars(count);
			newInstance.getAdmins().add(managedUser);
			newInstance.setCreator(managedUser);
			newInstance.setCategoriesFromString(categories, em);
			newInstance.setTagsFromString(tags, em);
			newInstance.incrementConnects(ClientType.WEB, user);
			em.getTransaction().commit();
			return newInstance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Graphic createGraphic(Graphic newInstance, User user, String categories, String tags) {
		log(Level.INFO, "create graphic",  newInstance.getName());
		newInstance.getDomain().checkExpired();
		newInstance.getDomain().checkCreation(user);
		if (!newInstance.isAdult() && (newInstance.checkProfanity() || Utils.checkProfanity(tags))) {
			throw BotException.offensive();
		}
		Utils.checkHTML(tags);
		tags = Utils.sanitize(tags);
		newInstance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Query query = em.createQuery("Select p from Graphic p where p.alias = :alias and p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", newInstance.getAlias());
			query.setParameter("domain", newInstance.getDomain());
			List<Graphic> results = query.getResultList();
			if (!results.isEmpty()) {
				// Append user to alias.
				newInstance.setAlias(newInstance.getAlias() + "-" + user.getUserId());
				query.setParameter("alias", newInstance.getAlias());
				query.setParameter("domain", newInstance.getDomain());
				results = query.getResultList();
				if (!results.isEmpty()) {
					throw new BotException("Graphic already exists - " + newInstance.getAlias());
				}
			}
			newInstance.setCreationDate(new Date());
			em.persist(newInstance);
			User managedUser = em.find(User.class, user.getUserId());
			int count = getAllGraphicsCount("", "", user.getUserId(), InstanceFilter.Personal, InstanceRestrict.None, InstanceSort.Name, ContentRating.Adult, "", user, null, false);
			if (count >= (user.getContentLimit() * 10)) {
				throw new BotException("You must upgrade to create more than " + (user.getContentLimit() * 10) + " graphics");
			}
			count = count + 1;
			managedUser.setGraphics(count);
			newInstance.getAdmins().add(managedUser);
			newInstance.setCreator(managedUser);
			newInstance.setCategoriesFromString(categories, em);
			newInstance.setTagsFromString(tags, em);
			newInstance.incrementConnects(ClientType.WEB, user);
			em.getTransaction().commit();
			return newInstance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Script createScript(Script newInstance, User user, String categories, String tags, LoginBean bean) {
		log(Level.INFO, "create script",  newInstance.getName());
		newInstance.getDomain().checkExpired();
		newInstance.getDomain().checkCreation(user);
		checkConstraints(newInstance, tags);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Query query = em.createQuery("Select p from Script p where p.alias = :alias and p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", newInstance.getAlias());
			query.setParameter("domain", newInstance.getDomain());
			List<Script> results = query.getResultList();
			if (!results.isEmpty()) {
				// Append user to alias.
				newInstance.setAlias(newInstance.getAlias() + "-" + user.getUserId());
				query.setParameter("alias", newInstance.getAlias());
				query.setParameter("domain", newInstance.getDomain());
				results = query.getResultList();
				if (!results.isEmpty()) {
					throw new BotException("Script already exists - " + newInstance.getAlias());
				}
			}
			Date now = new Date();
			newInstance.setCreationDate(now);
			newInstance.getSource().setCreationDate(now);
			newInstance.getSource().setVersion("0.1");
			em.persist(newInstance);
			User managedUser = em.find(User.class, user.getUserId());
			int count = getAllScriptsCount("", "", "", user.getUserId(), InstanceFilter.Personal, InstanceRestrict.None, InstanceSort.Name, ContentRating.Adult, "", user, null, false);
			if (count >= (user.getContentLimit() * 10)) {
				throw new BotException("You must upgrade to create more than " + (user.getContentLimit() * 10) + " scripts");
			}
			count = count + 1;
			managedUser.setScripts(count);
			newInstance.getAdmins().add(managedUser);
			newInstance.setCreator(managedUser);
			newInstance.setCategoriesFromString(categories, em);
			newInstance.setTagsFromString(tags, em);
			newInstance.getSource().setCreator(managedUser);
			newInstance.incrementConnects(ClientType.WEB, user);
			if (newInstance.getDomainForwarder() != null) {
				newInstance.getDomainForwarder().init(newInstance, bean);
			}
			em.getTransaction().commit();
			return newInstance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public ChatChannel createChannel(ChatChannel newInstance, User user, String categories, String tags, LoginBean bean) {
		log(Level.INFO, "create channel",  newInstance.getName());
		newInstance.getDomain().checkExpired();
		newInstance.getDomain().checkCreation(user);
		checkConstraints(newInstance, tags);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Query query = em.createQuery("Select p from ChatChannel p where p.alias = :alias and p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", newInstance.getAlias());
			query.setParameter("domain", newInstance.getDomain());
			List<ChatChannel> results = query.getResultList();
			if (!results.isEmpty()) {
				// Append user to alias.
				newInstance.setAlias(newInstance.getAlias() + "-" + user.getUserId());
				query.setParameter("alias", newInstance.getAlias());
				query.setParameter("domain", newInstance.getDomain());
				results = query.getResultList();
				if (!results.isEmpty()) {
					throw new BotException("Channel already exists - " + newInstance.getAlias());
				}
			}
			newInstance.setCreationDate(new Date());
			em.persist(newInstance);
			User managedUser = em.find(User.class, user.getUserId());
			int count = getAllChannelsCount("", "", user.getUserId(), InstanceFilter.Personal, InstanceRestrict.None, InstanceSort.Name, ContentRating.Adult, "", user, null);
			if (count >= user.getContentLimit()) {
				throw new BotException("You must upgrade to create more than " + user.getContentLimit() + " channels");
			}
			count = count + 1;
			managedUser.setChannels(count);
			newInstance.getAdmins().add(managedUser);
			newInstance.setCreator(managedUser);
			newInstance.setCategoriesFromString(categories, em);
			newInstance.setTagsFromString(tags, em);
			newInstance.incrementConnects(ClientType.WEB, user);
			if (newInstance.getDomainForwarder() != null) {
				newInstance.getDomainForwarder().init(newInstance, bean);
			}
			em.getTransaction().commit();
			return newInstance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Domain createDomain(Domain newInstance, String userId, String tags, String categories, LoginBean bean) {
		log(Level.INFO, "create domain",  newInstance.getName());
		checkConstraints(newInstance, tags);
		validateUser(userId);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Query query = em.createQuery("Select p from Domain p where p.alias = :alias");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", newInstance.getAlias());
			List<Domain> results = query.getResultList();
			if (!results.isEmpty()) {
				// Append user to alias.
				newInstance.setAlias(newInstance.getAlias() + "-" + userId);
				query.setParameter("alias", newInstance.getAlias());
				results = query.getResultList();
				if (!results.isEmpty()) {
					throw new BotException("Workspace already exists - " + newInstance.getAlias());
				}
			}
			newInstance.setCategoriesFromString(categories, em);
			newInstance.setTagsFromString(tags, em);
			newInstance.setCreationDate(new Date());
			newInstance.setActive(true);
			em.persist(newInstance);
			User user = em.find(User.class, userId);
			if (newInstance.getAccountType() == AccountType.Premium) {
				user.setType(UserType.Bronze);
			} else if (newInstance.getAccountType() == AccountType.Professional) {
				user.setType(UserType.Gold);
			} else if (newInstance.getAccountType() == AccountType.Enterprise) {
				user.setType(UserType.Platinum);
			} else if (newInstance.getAccountType() == AccountType.EnterprisePlus) {
				user.setType(UserType.Diamond);
			}
			int count = getAllDomainsCount("", "", user.getUserId(), InstanceFilter.Personal, InstanceRestrict.None, InstanceSort.Name, ContentRating.Adult, "", user);
			if (count >= user.getContentLimit()) {
				throw new BotException("You must upgrade to create more than " + user.getContentLimit() + " workspaces");
			}
			count = count + 1;
			user.setDomains(count);
			newInstance.getAdmins().add(user);
			newInstance.setCreator(user);
			newInstance.incrementConnects(ClientType.WEB, user);
			if (newInstance.getDomainForwarder() != null) {
				newInstance.getDomainForwarder().init(newInstance, bean);
			}
			em.getTransaction().commit();
			return newInstance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public License createLicense(License license) {
		log(Level.INFO, "create license", license.getClient());
		EntityManager em = getFactory().createEntityManager();
		try {
			license.setLicenseKey(String.valueOf(Math.abs(Utils.random().nextLong())));
			em.getTransaction().begin();
			em.persist(license);
			em.getTransaction().commit();
			return license;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public License updateLicense(License license) {
		log(Level.INFO, "update license", license.getClient());
		EntityManager em = getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			License managed = em.find(License.class, license.getId());
			if (managed == null) {
				throw new BotException("Missing license");
			}
			em.merge(license);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public void deleteLicense(License license) {
		log(Level.INFO, "delete license", license.getClient());
		EntityManager em = getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			License managed = em.find(License.class, license.getId());
			if (managed == null) {
				throw new BotException("Missing license");
			}
			em.remove(managed);
			em.getTransaction().commit();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Payment createPayment(Payment newInstance) {
		log(Level.INFO, "create payment", newInstance.getPaypalTx());
		newInstance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(newInstance);
			em.getTransaction().commit();
			return newInstance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Domain updatePayment(Domain domain, Payment instance) {
		log(Level.INFO, "update payment", instance.getPaypalTx());
		instance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Payment managed = em.find(Payment.class, instance.getId());
			if (managed == null) {
				throw new BotException("Missing payment");
			}
			em.merge(instance);
			Domain managedDomain = em.find(Domain.class, domain.getId());
			managed.setDomainId(managedDomain.getId());
			managedDomain.getPayments().add(managed);
			em.getTransaction().commit();
			return managedDomain;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Avatar addAvatarMedia(AvatarMedia avatarMedia, Media media, Avatar avatar) {
		log(Level.INFO, "create avatar media", avatar);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Avatar managed = em.find(Avatar.class, avatar.getId());
			if (managed == null) {
				throw new BotException("Missing avatar");
			}
			em.persist(media);
			avatarMedia.setMediaId(media.getId());
			em.persist(avatarMedia);
			avatarMedia.setAvatar(managed);
			avatar.getMedia().size(); // force instantiation for some reason
			managed.getMedia().size();
			managed.getMedia().add(avatarMedia);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public ChannelAttachment addChannelAttachment(ChannelAttachment attachment, Media media, ChatChannel channel, String userId, String token) {
		log(Level.INFO, "create channel attachment", channel);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			ChatChannel managed = em.find(ChatChannel.class, channel.getId());
			em.persist(media);
			attachment.setMediaId(media.getId());
			em.persist(attachment);
			attachment.setChannel(managed);
			attachment.setDomain(managed.getDomain());
			if (userId != null && !userId.isEmpty()) {
				User user = em.find(User.class, userId);
				if (user == null) {
					throw new BotException("Missing user");
				}
				if (!String.valueOf(user.getToken()).equals(token.trim())) {
					throw new BotException("Invalid user token");
				}
				attachment.setCreator(user);
			} else if (channel.isChatRoom()) {
				throw new BotException("User id required");
			}
			attachment.setCreationDate(new Date());
			em.getTransaction().commit();
			return attachment;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotAttachment addBotAttachment(BotAttachment attachment, Media media, BotInstance channel, String userId, String token) {
		log(Level.INFO, "create bot attachment", channel);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance managed = em.find(BotInstance.class, channel.getId());
			em.persist(media);
			attachment.setMediaId(media.getId());
			em.persist(attachment);
			attachment.setBot(managed);
			attachment.setDomain(managed.getDomain());
			if (userId != null && !userId.isEmpty()) {
				User user = em.find(User.class, userId);
				if (user == null) {
					throw new BotException("Missing user");
				}
				if (!String.valueOf(user.getToken()).equals(token.trim())) {
					throw new BotException("Invalid user token");
				}
				attachment.setCreator(user);
			}
			attachment.setCreationDate(new Date());
			em.getTransaction().commit();
			return attachment;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public ForumAttachment addForumAttachment(ForumAttachment attachment, Media media, Forum forum, String userId, String token) {
		log(Level.INFO, "create forum attachment", forum);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Forum managed = em.find(Forum.class, forum.getId());
			em.persist(media);
			attachment.setMediaId(media.getId());
			em.persist(attachment);
			attachment.setForum(managed);
			attachment.setDomain(managed.getDomain());
			if (userId != null && !userId.isEmpty()) {
				User user = em.find(User.class, userId);
				if (user == null) {
					throw new BotException("Missing user");
				}
				if (!String.valueOf(user.getToken()).equals(token.trim())) {
					throw new BotException("Invalid user token");
				}
				attachment.setCreator(user);
			} else {
				throw new BotException("User id required");
			}
			attachment.setCreationDate(new Date());
			em.getTransaction().commit();
			return attachment;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public IssueTrackerAttachment addIssueTrackerAttachment(IssueTrackerAttachment attachment, Media media, IssueTracker tracker, String userId, String token) {
		log(Level.INFO, "create issuetracker attachment", tracker);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			IssueTracker managed = em.find(IssueTracker.class, tracker.getId());
			em.persist(media);
			attachment.setMediaId(media.getId());
			em.persist(attachment);
			attachment.setTracker(managed);
			attachment.setDomain(managed.getDomain());
			if (userId != null && !userId.isEmpty()) {
				User user = em.find(User.class, userId);
				if (user == null) {
					throw new BotException("Missing user");
				}
				if (!String.valueOf(user.getToken()).equals(token.trim())) {
					throw new BotException("Invalid user token");
				}
				attachment.setCreator(user);
			} else {
				throw new BotException("User id required");
			}
			attachment.setCreationDate(new Date());
			em.getTransaction().commit();
			return attachment;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Avatar updateAvatarBackground(MediaFile mediaFile, Media media, Avatar avatar) {
		log(Level.INFO, "update avatar background", avatar);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Avatar managed = em.find(Avatar.class, avatar.getId());
			em.persist(media);
			mediaFile.setMediaId(media.getId());
			em.persist(mediaFile);
			managed.setBackground(mediaFile);

			byte[] image = media.getMedia();
			int[] dimensions = Utils.getDimensions(image);
			if (dimensions != null) {
				managed.setWidth(dimensions[0]);
				managed.setHeight(dimensions[1]);
			}
			
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Avatar updateAvatarApiKeys(String appId, String apiKey, String apiEndpoint, Avatar avatar) {
		log(Level.INFO, "update avatar api keys", avatar);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Avatar managed = em.find(Avatar.class, avatar.getId());

			managed.setNativeVoiceAppId(appId);
			managed.setNativeVoiceApiKey(apiKey);
			if (apiEndpoint != null) {
				managed.setVoiceApiEndpoint(apiEndpoint);
			}
			
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Graphic updateGraphicMedia(MediaFile mediaFile, Media media, Graphic graphic) {
		log(Level.INFO, "update graphic media", graphic);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Graphic managed = em.find(Graphic.class, graphic.getId());
			em.persist(media);
			mediaFile.setMediaId(media.getId());
			em.persist(mediaFile);
			if (managed.getMedia() != null) {
				MediaFile old = managed.getMedia();
				managed.setMedia(null);
				em.remove(old);
				em.remove(em.find(Media.class, old.getMediaId()));
			}
			managed.setMedia(mediaFile);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Avatar updateAvatarMedia(Avatar avatar) {
		log(Level.INFO, "update avatar media", avatar);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Avatar managed = em.find(Avatar.class, avatar.getId());
			for (AvatarMedia media : avatar.getMedia()) {
				em.merge(media);
			}
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Avatar deleteAvatarMedia(long id, Avatar avatar) {
		log(Level.INFO, "delete avatar media", avatar);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Avatar managed = em.find(Avatar.class, avatar.getId());
			AvatarMedia avatarMedia = managed.getMedia(id);
			if (avatarMedia == null) {
				throw new BotException("Missing media: " + id);				
			}
			managed.getMedia().remove(avatarMedia);
			em.remove(avatarMedia);
			Media media = em.find(Media.class, avatarMedia.getMediaId());
			if (media != null) {
				em.remove(media);
			}
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Avatar deleteAvatarBackground(Avatar avatar) {
		log(Level.INFO, "delete avatar background", avatar);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Avatar managed = em.find(Avatar.class, avatar.getId());
			MediaFile mediaFile = managed.getBackground();
			if (mediaFile == null) {
				throw new BotException("Missing background");
			}
			managed.setBackground(null);
			em.remove(mediaFile);
			Media media = em.find(Media.class, mediaFile.getMediaId());
			if (media != null) {
				em.remove(media);
			}
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User addPayment(User user, UserPayment newInstance) {
		log(Level.INFO, "create payment", newInstance.getToken());
		newInstance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User managed = em.find(User.class, user.getUserId());
			newInstance.setUserId(user.getUserId());
			managed.getPayments().add(newInstance);
			em.persist(newInstance);
			em.getTransaction().commit();
			managed.setEncryptedPassword(null);
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User updatePayment(UserPayment instance) {
		log(Level.INFO, "update payment", instance.getToken());
		instance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User managed = em.find(User.class, instance.getUserId());
			em.merge(instance);
			em.getTransaction().commit();
			managed.setEncryptedPassword(null);
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public UserPayment findUserPayment(long id) {
		log(Level.FINE, "find user payment", id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			UserPayment instance = em.find(UserPayment.class, id);
			if (instance == null) {
				throw new BotException("User payment does not exists - " + id);
			}
			return instance;
		} finally {
			em.close();
		}
	}
	
	public Payment findPayment(long id) {
		log(Level.FINE, "find payment", id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			Payment instance = em.find(Payment.class, id);
			if (instance == null) {
				throw new BotException("Payment does not exists - " + id);
			}
			return instance;
		} finally {
			em.close();
		}
	}
	
	public Category createCategory(Category newInstance, User user, String parents) {
		log(Level.INFO, "create category", newInstance.getName());
		newInstance.getDomain().checkExpired();
		newInstance.getDomain().checkCreation(user);
		if (!newInstance.getDomain().isAdult() && (Utils.checkProfanity(newInstance.getName()) || Utils.checkProfanity(newInstance.getDescription()))) {
			throw BotException.offensive();
		}
		newInstance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Query query = em.createQuery("Select p from Category p where p.type = :type and p.name = :name and p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("name", newInstance.getName());
			query.setParameter("type", newInstance.getType());
			query.setParameter("domain", newInstance.getDomain());
			List<BotInstance> results = query.getResultList();
			if (!results.isEmpty()) {
				throw new BotException("Category already exists - " + newInstance.getName());
			}
			newInstance.setCreationDate(new Date());
			User managedUser = em.find(User.class, user.getUserId());
			newInstance.setCreator(managedUser);
			newInstance.setParentsFromString(parents, em);
			em.persist(newInstance);
			em.getTransaction().commit();
			return newInstance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public ForumPost createForumPost(ForumPost newInstance, String userId, String tags, Domain domain) {
		log(Level.INFO, "create forum post",  newInstance.getTopic());
		newInstance.getDomain().checkExpired();
		if (!newInstance.getForum().isAdult() && (newInstance.checkProfanity() || Utils.checkProfanity(tags))) {
			throw BotException.offensive();
		}
		Utils.checkHTML(tags);
		tags = Utils.sanitize(tags);
		newInstance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			newInstance.setTagsFromString(tags, em, domain);
			newInstance.setCreationDate(new Date());
			ForumPost parent = newInstance.getParent();
			if (parent != null) {
				parent = em.find(ForumPost.class, parent.getId());
				parent.addReply(newInstance);
			}
			Forum forum = newInstance.getForum();
			forum = em.find(Forum.class, forum.getId());
			newInstance.setForum(forum);
			forum.incrementPosts();
			em.persist(newInstance);
			User user = em.find(User.class, userId);
			user.setPosts(user.getPosts() + 1);
			newInstance.setCreator(user);
			em.getTransaction().commit();
			if (parent != null) {
				return parent;
			}
			return newInstance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Issue createIssue(Issue newInstance, String userId, String tags, Domain domain) {
		log(Level.INFO, "create issue",  newInstance.getTitle());
		newInstance.getDomain().checkExpired();
		if (!newInstance.getTracker().isAdult() && (newInstance.checkProfanity() || Utils.checkProfanity(tags))) {
			throw BotException.offensive();
		}
		Utils.checkHTML(tags);
		tags = Utils.sanitize(tags);
		newInstance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			newInstance.setTagsFromString(tags, em, domain);
			newInstance.setCreationDate(new Date());
			IssueTracker tracker = newInstance.getTracker();
			tracker = em.find(IssueTracker.class, tracker.getId());
			newInstance.setTracker(tracker);
			tracker.incrementIssues();
			em.persist(newInstance);
			User user = em.find(User.class, userId);
			user.setIssues(user.getIssues() + 1);
			newInstance.setCreator(user);
			em.getTransaction().commit();
			return newInstance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public ChatMessage createChatMessage(ChatMessage newInstance) {
		log(Level.INFO, "create chat message",  newInstance.getMessage());
		if (!newInstance.getChannel().isAdult() && newInstance.checkProfanity()) {
			throw BotException.offensive();
		}
		newInstance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			newInstance.setCreationDate(new Date());
			ChatChannel channel = newInstance.getChannel();
			channel = em.find(ChatChannel.class, channel.getId());
			newInstance.setChannel(channel);
			channel.setMessages(channel.getMessages() + 1);
			em.persist(newInstance);
			if (newInstance.getCreator() != null) {
				User user = em.find(User.class, newInstance.getCreator().getUserId());
				user.setMessages(user.getMessages() + 1);
				newInstance.setCreator(user);
			}
			if (newInstance.getTarget() != null) {
				User user = em.find(User.class, newInstance.getTarget().getUserId());
				newInstance.setTarget(user);
			}
			em.getTransaction().commit();
			return newInstance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Friendship createUserFriendship(String userId, String userTargetId) {
		log(Level.INFO, "create user friendship", userTargetId);
		EntityManager em =  getFactory().createEntityManager();
		Friendship friendship = null;
		try {
			em.getTransaction().begin();
			if (userId.equals(userTargetId)) {
				throw new BotException("You cannot add yourself as a friend - " + userId);
			}
			User friend = (User)em.find(User.class, userTargetId);
			if (friend == null) {
				throw new BotException("User does not exist - " + userTargetId);
			}
			List<Friendship> friendsList = getUserFriendships(userId);
			for (Friendship currFriendship : friendsList) {
				if (userTargetId.equals(currFriendship.getFriend())) {
					throw new BotException("Friendship already exists - " + currFriendship.getFriend());
				}
			}
			friendship = new Friendship();
			friendship.setUserId(userId);
			friendship.setFriend(userTargetId);
			em.persist(friendship);
			em.getTransaction().commit();
			return friendship;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public boolean removeUserFriendship(String userId, String friendId) {
		log(Level.WARNING, "deleting user friendship",  friendId);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User userInstance = em.find(User.class, userId);
			if (userInstance == null) {
				throw new BotException("User does not exist - " + userId);
			}
			User friendInstance = em.find(User.class, friendId);
			if (friendInstance == null) {
				throw new BotException("User does not exist - " + friendId);
			}
			Query query = em.createQuery("Delete from Friendship friendship where friendship.userId = :user and friendship.friend = :friend");
			query.setParameter("user", userInstance.getUserId());
			query.setParameter("friend", friendInstance.getUserId());
			query.executeUpdate();
			em.getTransaction().commit();
			return true;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public UserMessage getUserMessage(Long id) {
		log(Level.WARNING, "get user message",  id);
		EntityManager em =  getFactory().createEntityManager();
		UserMessage userMessage = null;
		try {
			em.getTransaction().begin();
			userMessage = (UserMessage) em.find(UserMessage.class, id);
			if (userMessage == null) {
				throw new BotException("Message does not exist - " + id);
			}
			return userMessage;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public UserMessage createUserMessage(UserMessage newInstance) {
		log(Level.INFO, "create user message",  newInstance.getMessage());
		if (!Site.ADULT && newInstance.checkProfanity()) {
			throw BotException.offensive();
		}
		newInstance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			newInstance.setCreationDate(new Date());
			em.persist(newInstance);
			if (newInstance.getOwner() != null) {
				User user = em.find(User.class, newInstance.getOwner().getUserId());
				newInstance.setOwner(user);
			}
			if (newInstance.getCreator() != null) {
				User user = em.find(User.class, newInstance.getCreator().getUserId());
				newInstance.setCreator(user);
			}
			if (newInstance.getTarget() != null) {
				User user = em.find(User.class, newInstance.getTarget().getUserId());
				newInstance.setTarget(user);
			}
			if (newInstance.getParent() != null) {
				UserMessage parent = em.find(UserMessage.class, newInstance.getParent().getId());
				newInstance.setParent(parent);
			}
			em.getTransaction().commit();
			return newInstance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstance(BotInstance updatedInstance, String categories, String tags) {
		log(Level.INFO, "update instance",  updatedInstance.getName());
		checkConstraints(updatedInstance, tags);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, updatedInstance.getId());
			if (instance == null) {
				throw new BotException("Bot does not exist - " + updatedInstance.getId());
			}
			if (!instance.getAlias().equals(updatedInstance.getAlias())) {	
				Query query = em.createQuery("Select p from BotInstance p where p.alias = :alias and p.domain = :domain");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("alias", updatedInstance.getAlias());
				query.setParameter("domain", updatedInstance.getDomain());
				List<BotInstance> results = query.getResultList();
				if (!results.isEmpty()) {
					throw new BotException("Bot already exists - " + updatedInstance.getAlias());
				}
				instance.setAlias(updatedInstance.getAlias());
			}
			instance.setName(updatedInstance.getName());
			if (!instance.getCreator().equals(updatedInstance.getCreator())) {
				instance.getAdmins().remove(instance.getCreator());
				instance.setCreator(em.find(User.class, updatedInstance.getCreator().getUserId()));
				instance.getAdmins().add(instance.getCreator());
			}
			if (!instance.getDomain().equals(updatedInstance.getDomain())) {
				instance.setDomain(em.find(Domain.class, updatedInstance.getDomain().getId()));
			}
			update(instance, updatedInstance, tags, categories, em);
			instance.setApiURL(updatedInstance.getApiURL());
			instance.setApiPost(updatedInstance.getApiPost());
			instance.setApiResponse(updatedInstance.getApiResponse());
			instance.setApiServerSide(updatedInstance.getApiServerSide());
			instance.setApiServerSide(updatedInstance.getApiJSON());
			instance.setMemoryLimit(updatedInstance.getMemoryLimit());
			if (updatedInstance.getDomainForwarder() != null) {
				instance.setDomainForwarder(em.merge(updatedInstance.getDomainForwarder()));
			} else {
				instance.setDomainForwarder(null);
			}
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceSize(BotInstance bot, int size) {
		log(Level.INFO, "update instance size", bot.getName(), size);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, bot.getId());
			if (instance == null) {
				throw new BotException("Bot does not exist - " + bot.getId());
			}
			// Avoid too much updating.
			if (!(size < (instance.getMemorySize() - 100) || size > (instance.getMemorySize() + 100))) {
				return instance;
			}
			instance.setMemorySize(size);
			if (instance.getMemoryLimit() <= 0) {
				instance.setMemoryLimit(Site.MEMORYLIMIT);
			}
			synchronized (getLock(instance.getId())) {
				em.getTransaction().commit();
			}
			return instance;
		} catch (BotException exception) {
			throw exception;
		} catch (Exception ignore) {
			// Ignore concurrency errors.
			return bot;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance clearErrors(BotInstance instance) {
		log(Level.INFO, "clear errors", instance.getName());
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance managed = em.find(BotInstance.class, instance.getId());
			if (managed == null) {
				throw new BotException("Bot does not exist - " + instance.getId());
			}
			managed.getErrors().clear();
			em.getTransaction().commit();
			return managed;
		} catch (BotException exception) {
			throw exception;
		} catch (Exception ignore) {
			ignore.printStackTrace();
			// Ignore concurrency errors.
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance addError(BotInstance instance, String error) {
		log(Level.INFO, "add error", instance.getName(), error);
		synchronized (getLock(instance.getId())) {
			EntityManager em =  getFactory().createEntityManager();
			try {
				em.getTransaction().begin();
				BotInstance managed = em.find(BotInstance.class, instance.getId());
				if (managed == null) {
					throw new BotException("Bot does not exist - " + instance.getId());
				}
				if (error != null && error.length() > 1024) {
					error = error.substring(0, 1024);
				}
				ErrorMessage message = new ErrorMessage();
				message.setCreationDate(new Date());
				message.setMessage(error);
				em.persist(message);
				managed.addError(message);
				em.getTransaction().commit();
				return managed;
			} finally {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				em.close();
			}
		}
	}
	
	public void update(WebMedium instance, WebMedium updatedInstance, String tags, String categories, EntityManager em) {
		instance.setAdult(updatedInstance.isAdult());
		instance.setPrivate(updatedInstance.isPrivate());
		instance.setWebsite(updatedInstance.getWebsite());
		instance.setHidden(updatedInstance.isHidden());
		instance.setAccessMode(updatedInstance.getAccessMode());
		instance.setForkAccessMode(updatedInstance.getForkAccessMode());
		instance.setContentRating(updatedInstance.getContentRating());
		instance.setContentVerified(updatedInstance.isContentVerified());
		instance.setTemplate(updatedInstance.isTemplate());
		instance.setFeatured(updatedInstance.isFeatured());
		instance.setAllowForking(updatedInstance.getAllowForking());
		instance.setName(updatedInstance.getName());
		instance.setAlias(updatedInstance.getAlias());
		instance.setDescription(updatedInstance.getDescription());
		instance.setDetails(updatedInstance.getDetails());
		instance.setDisclaimer(updatedInstance.getDisclaimer());
		instance.setLicense(updatedInstance.getLicense());
		instance.setShowAds(updatedInstance.getShowAds());
		instance.setAdCodeVerified(updatedInstance.isAdCodeVerified());
		instance.setAdCode(updatedInstance.getAdCode());
		if (categories != null) {
			instance.setCategoriesFromString(categories, em);
		}
		instance.setTagsFromString(tags, em);
	}
	
	public void checkConstraints(WebMedium instance, String tags) {
		if (!instance.isAdult() && (instance.checkProfanity() || Utils.checkProfanity(tags, Utils.EVERYONE))) {
			throw BotException.offensive();
		}
		Utils.checkHTML(tags);
		tags = Utils.sanitize(tags);
		instance.checkConstraints();
	}
	
	public Domain updateDomain(Domain updatedInstance, String tags, String categories) {
		log(Level.INFO, "update", updatedInstance);
		checkConstraints(updatedInstance, tags);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Domain instance = (Domain)em.find(updatedInstance.getClass(), updatedInstance.getId());
			if (instance == null) {
				throw new BotException(updatedInstance.getDisplayName() + " does not exist - " + updatedInstance.getId());
			}
			if (!instance.getAlias().equals(updatedInstance.getAlias())) {
				Query query = em.createQuery("Select p from Domain p where p.alias = :alias");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("alias", updatedInstance.getAlias());
				List<BotInstance> results = query.getResultList();
				if (!results.isEmpty()) {
					throw new BotException(updatedInstance.getDisplayName() + " already exists - " + updatedInstance.getAlias());
				}
				instance.setAlias(updatedInstance.getAlias());
			}
			instance.setName(updatedInstance.getName());
			if (instance.getCreator() == null) {
				instance.setCreator(em.find(User.class, updatedInstance.getCreator().getUserId()));
				if (!instance.getAdmins().contains(instance.getCreator())) {
					instance.getAdmins().add(instance.getCreator());
				}
			} else if (!instance.getCreator().equals(updatedInstance.getCreator())) {
				instance.getAdmins().remove(instance.getCreator());
				instance.setCreator(em.find(User.class, updatedInstance.getCreator().getUserId()));
				if (!instance.getAdmins().contains(instance.getCreator())) {
					instance.getAdmins().add(instance.getCreator());
				}
			}
			update(instance, updatedInstance, tags, categories, em);
			instance.setCreationMode(updatedInstance.getCreationMode());
			instance.setActive(updatedInstance.isActive());
			instance.setPaymentDate(updatedInstance.getPaymentDate());
			instance.setPaymentDuration(updatedInstance.getPaymentDuration());
			instance.setAccountType(updatedInstance.getAccountType());
			if (updatedInstance.getDomainForwarder() != null) {
				instance.setDomainForwarder(em.merge(updatedInstance.getDomainForwarder()));
			} else {
				instance.setDomainForwarder(null);
			}
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public boolean domainExists(String alias) {
		EntityManager em = getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from Domain p where p.alias = :alias");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", alias);
			List<BotInstance> results = query.getResultList();
			return !results.isEmpty();
		} finally {
			em.close();
		}
	}
	
	public Forum updateForum(Forum updatedInstance, String categories, String tags) {
		log(Level.INFO, "update", updatedInstance);
		checkConstraints(updatedInstance, tags);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Forum instance = (Forum)em.find(Forum.class, updatedInstance.getId());
			if (instance == null) {
				throw new BotException(updatedInstance.getDisplayName() + " does not exist - " + updatedInstance.getId());
			}
			if (!instance.getAlias().equals(updatedInstance.getAlias())) {	
				Query query = em.createQuery("Select p from Forum p where p.alias = :alias and p.domain = :domain");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("alias", updatedInstance.getAlias());
				query.setParameter("domain", updatedInstance.getDomain());
				List<BotInstance> results = query.getResultList();
				if (!results.isEmpty()) {
					throw new BotException("Forum already exists - " + updatedInstance.getAlias());
				}
				instance.setAlias(updatedInstance.getAlias());
			}
			instance.setName(updatedInstance.getName());
			if (!instance.getCreator().equals(updatedInstance.getCreator())) {
				instance.getAdmins().remove(instance.getCreator());
				instance.setCreator(em.find(User.class, updatedInstance.getCreator().getUserId()));
				instance.getAdmins().add(instance.getCreator());
			}
			if (!instance.getDomain().equals(updatedInstance.getDomain())) {
				instance.setDomain(em.find(Domain.class, updatedInstance.getDomain().getId()));
			}
			update(instance, updatedInstance, tags, categories, em);
			instance.setPostAccessMode(updatedInstance.getPostAccessMode());
			instance.setReplyAccessMode(updatedInstance.getReplyAccessMode());
			if (updatedInstance.getDomainForwarder() != null) {
				instance.setDomainForwarder(em.merge(updatedInstance.getDomainForwarder()));
			} else {
				instance.setDomainForwarder(null);
			}
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public IssueTracker updateIssueTracker(IssueTracker updatedInstance, String categories, String tags) {
		log(Level.INFO, "update", updatedInstance);
		checkConstraints(updatedInstance, tags);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			IssueTracker instance = (IssueTracker)em.find(IssueTracker.class, updatedInstance.getId());
			if (instance == null) {
				throw new BotException(updatedInstance.getDisplayName() + " does not exist - " + updatedInstance.getId());
			}
			if (!instance.getAlias().equals(updatedInstance.getAlias())) {	
				Query query = em.createQuery("Select p from IssueTracker p where p.alias = :alias and p.domain = :domain");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("alias", updatedInstance.getAlias());
				query.setParameter("domain", updatedInstance.getDomain());
				List<BotInstance> results = query.getResultList();
				if (!results.isEmpty()) {
					throw new BotException("IssueTracker already exists - " + updatedInstance.getAlias());
				}
				instance.setAlias(updatedInstance.getAlias());
			}
			instance.setName(updatedInstance.getName());
			if (!instance.getCreator().equals(updatedInstance.getCreator())) {
				instance.getAdmins().remove(instance.getCreator());
				instance.setCreator(em.find(User.class, updatedInstance.getCreator().getUserId()));
				instance.getAdmins().add(instance.getCreator());
			}
			if (!instance.getDomain().equals(updatedInstance.getDomain())) {
				instance.setDomain(em.find(Domain.class, updatedInstance.getDomain().getId()));
			}
			update(instance, updatedInstance, tags, categories, em);
			instance.setCreateAccessMode(updatedInstance.getCreateAccessMode());
			if (updatedInstance.getDomainForwarder() != null) {
				instance.setDomainForwarder(em.merge(updatedInstance.getDomainForwarder()));
			} else {
				instance.setDomainForwarder(null);
			}
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public <T extends WebMedium> T updateForwarder(T updatedInstance) {
		log(Level.INFO, "update forwarder", updatedInstance);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			T instance = (T)em.find(updatedInstance.getClass(), updatedInstance.getId());
			if (instance == null) {
				throw new BotException(updatedInstance.getDisplayName() + " does not exist - " + updatedInstance.getId());
			}
			if (updatedInstance.getDomainForwarder() != null) {
				instance.setDomainForwarder(em.merge(updatedInstance.getDomainForwarder()));
			} else {
				instance.setDomainForwarder(null);
			}
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Avatar updateAvatar(Avatar updatedInstance, String categories, String tags) {
		log(Level.INFO, "update", updatedInstance);
		if (!updatedInstance.isAdult() && (updatedInstance.checkProfanity() || Utils.checkProfanity(tags))) {
			throw BotException.offensive();
		}
		Utils.checkHTML(tags);
		tags = Utils.sanitize(tags);
		updatedInstance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Avatar instance = (Avatar)em.find(Avatar.class, updatedInstance.getId());
			if (instance == null) {
				throw new BotException(updatedInstance.getTypeName() + " does not exist - " + updatedInstance.getId());
			}
			if (!instance.getAlias().equals(updatedInstance.getAlias())) {	
				Query query = em.createQuery("Select p from Avatar p where p.alias = :alias and p.domain = :domain");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("alias", updatedInstance.getAlias());
				query.setParameter("domain", updatedInstance.getDomain());
				List<BotInstance> results = query.getResultList();
				if (!results.isEmpty()) {
					throw new BotException("Avatar already exists - " + updatedInstance.getAlias());
				}
				instance.setAlias(updatedInstance.getAlias());
			}
			instance.setName(updatedInstance.getName());
			if (!instance.getCreator().equals(updatedInstance.getCreator())) {
				instance.getAdmins().remove(instance.getCreator());
				instance.setCreator(em.find(User.class, updatedInstance.getCreator().getUserId()));
				instance.getAdmins().add(instance.getCreator());
			}
			if (!instance.getDomain().equals(updatedInstance.getDomain())) {
				instance.setDomain(em.find(Domain.class, updatedInstance.getDomain().getId()));
			}
			update(instance, updatedInstance, tags, categories, em);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Graphic updateGraphic(Graphic updatedInstance, String categories, String tags) {
		log(Level.INFO, "update", updatedInstance);
		if (!updatedInstance.isAdult() && (updatedInstance.checkProfanity() || Utils.checkProfanity(tags))) {
			throw BotException.offensive();
		}
		Utils.checkHTML(tags);
		tags = Utils.sanitize(tags);
		updatedInstance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Graphic instance = (Graphic)em.find(Graphic.class, updatedInstance.getId());
			if (instance == null) {
				throw new BotException(updatedInstance.getTypeName() + " does not exist - " + updatedInstance.getId());
			}
			if (!instance.getAlias().equals(updatedInstance.getAlias())) {	
				Query query = em.createQuery("Select p from Graphic p where p.alias = :alias and p.domain = :domain");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("alias", updatedInstance.getAlias());
				query.setParameter("domain", updatedInstance.getDomain());
				List<BotInstance> results = query.getResultList();
				if (!results.isEmpty()) {
					throw new BotException("Graphic already exists - " + updatedInstance.getAlias());
				}
				instance.setAlias(updatedInstance.getAlias());
			}
			instance.setName(updatedInstance.getName());
			if (!instance.getCreator().equals(updatedInstance.getCreator())) {
				instance.getAdmins().remove(instance.getCreator());
				instance.setCreator(em.find(User.class, updatedInstance.getCreator().getUserId()));
				instance.getAdmins().add(instance.getCreator());
			}
			if (!instance.getDomain().equals(updatedInstance.getDomain())) {
				instance.setDomain(em.find(Domain.class, updatedInstance.getDomain().getId()));
			}
			update(instance, updatedInstance, tags, categories, em);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Script updateScript(Script updatedInstance, String categories, String tags) {
		log(Level.INFO, "update", updatedInstance);
		checkConstraints(updatedInstance, tags);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Script instance = (Script)em.find(Script.class, updatedInstance.getId());
			if (instance == null) {
				throw new BotException(updatedInstance.getDisplayName() + " does not exist - " + updatedInstance.getId());
			}
			if (!instance.getAlias().equals(updatedInstance.getAlias())) {
				Query query = em.createQuery("Select p from Script p where p.alias = :alias and p.domain = :domain");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("alias", updatedInstance.getAlias());
				query.setParameter("domain", updatedInstance.getDomain());
				List<BotInstance> results = query.getResultList();
				if (!results.isEmpty()) {
					throw new BotException("Script already exists - " + updatedInstance.getAlias());
				}
				instance.setAlias(updatedInstance.getAlias());
			}
			instance.setName(updatedInstance.getName());
			if (!instance.getCreator().equals(updatedInstance.getCreator())) {
				instance.getAdmins().remove(instance.getCreator());
				instance.setCreator(em.find(User.class, updatedInstance.getCreator().getUserId()));
				instance.getAdmins().add(instance.getCreator());
			}
			if (!instance.getDomain().equals(updatedInstance.getDomain())) {
				instance.setDomain(em.find(Domain.class, updatedInstance.getDomain().getId()));
			}
			update(instance, updatedInstance, tags, categories, em);
			instance.setLanguage(updatedInstance.getLanguage());
			if (updatedInstance.getDomainForwarder() != null) {
				instance.setDomainForwarder(em.merge(updatedInstance.getDomainForwarder()));
			} else {
				instance.setDomainForwarder(null);
			}
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Script updateScript(Script updatedInstance, String sourceCode, boolean version, String versionName, User user) {
		log(Level.INFO, "update source", updatedInstance);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Script instance = (Script)em.find(Script.class, updatedInstance.getId());
			if (instance == null) {
				throw new BotException(updatedInstance.getDisplayName() + " does not exist - " + updatedInstance.getId());
			}
			ScriptSource source = instance.getSource();
			Date now = new Date();
			User managedUser = em.find(User.class, user.getUserId());
			if (version) {
				source = new ScriptSource(instance);
				source.setCreationDate(now);
				source.setVersion(versionName);
				source.setSource(sourceCode);
				em.persist(source);
				instance.setSource(source);
			} else {
				source.setSource(sourceCode);
			}
			instance.setSize(sourceCode.length());
			source.setUpdateDate(now);
			source.setCreator(managedUser);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public ChatChannel updateChannel(ChatChannel updatedInstance, String categories, String tags) {
		log(Level.INFO, "update", updatedInstance);
		checkConstraints(updatedInstance, tags);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			ChatChannel instance = (ChatChannel)em.find(updatedInstance.getClass(), updatedInstance.getId());
			if (instance == null) {
				throw new BotException(updatedInstance.getTypeName() + " does not exist - " + updatedInstance.getId());
			}
			if (!instance.getAlias().equals(updatedInstance.getAlias())) {	
				Query query = em.createQuery("Select p from ChatChannel p where p.alias = :alias and p.domain = :domain");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("alias", updatedInstance.getAlias());
				query.setParameter("domain", updatedInstance.getDomain());
				List<BotInstance> results = query.getResultList();
				if (!results.isEmpty()) {
					throw new BotException("Channel already exists - " + updatedInstance.getAlias());
				}
				instance.setAlias(updatedInstance.getAlias());
			}
			instance.setName(updatedInstance.getName());
			if (!instance.getCreator().equals(updatedInstance.getCreator())) {
				instance.getAdmins().remove(instance.getCreator());
				instance.setCreator(em.find(User.class, updatedInstance.getCreator().getUserId()));
				instance.getAdmins().add(instance.getCreator());
			}
			if (!instance.getDomain().equals(updatedInstance.getDomain())) {
				instance.setDomain(em.find(Domain.class, updatedInstance.getDomain().getId()));
			}
			update(instance, updatedInstance, tags, categories, em);
			instance.setType(updatedInstance.getType());
			instance.setVideoAccessMode(updatedInstance.getVideoAccessMode());
			instance.setAudioAccessMode(updatedInstance.getAudioAccessMode());
			if (updatedInstance.getDomainForwarder() != null) {
				instance.setDomainForwarder(em.merge(updatedInstance.getDomainForwarder()));
			} else {
				instance.setDomainForwarder(null);
			}
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public ChatChannel updateChannelSettings(ChatChannel updatedInstance, Long botId) {
		log(Level.INFO, "update settings", updatedInstance);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			ChatChannel instance = (ChatChannel)em.find(updatedInstance.getClass(), updatedInstance.getId());
			if (instance == null) {
				throw new BotException(updatedInstance.getTypeName() + " does not exist - " + updatedInstance.getId());
			}
			instance.setWelcomeMessage(updatedInstance.getWelcomeMessage());
			instance.setStatusMessage(updatedInstance.getStatusMessage());
			instance.setBotMode(updatedInstance.getBotMode());
			instance.setBot(updatedInstance.getBot());
			instance.setEmailAddress(updatedInstance.getEmailAddress());
			instance.setEmailUserName(updatedInstance.getEmailUserName());
			instance.setEmailPassword(updatedInstance.getEmailPassword());
			instance.setEmailProtocol(updatedInstance.getEmailProtocol());
			instance.setEmailSSL(updatedInstance.getEmailSSL());
			instance.setEmailIncomingHost(updatedInstance.getEmailIncomingHost());
			instance.setEmailIncomingPort(updatedInstance.getEmailIncomingPort());
			instance.setEmailOutgoingHost(updatedInstance.getEmailOutgoingHost());
			instance.setEmailOutgoingPort(updatedInstance.getEmailOutgoingPort());
			instance.setEmailTopic(updatedInstance.getEmailTopic());
			instance.setEmailBody(updatedInstance.getEmailBody());
			if (botId == null) {
				instance.setBot(null);
			} else {
				BotInstance bot = em.find(BotInstance.class, botId);
				if (bot == null) {
					throw new BotException(new BotInstance().getTypeName() + " does not exist - " + botId);
				}
				instance.setBot(bot);
			}
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Forum updateForumBot(Forum updatedInstance, Long botId) {
		log(Level.INFO, "update bot", updatedInstance);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Forum instance = (Forum)em.find(updatedInstance.getClass(), updatedInstance.getId());
			if (instance == null) {
				throw new BotException(updatedInstance.getDisplayName() + " does not exist - " + updatedInstance.getId());
			}
			instance.setBotMode(updatedInstance.getBotMode());
			instance.setBot(updatedInstance.getBot());
			if (botId == null) {
				instance.setBot(null);
			} else {
				BotInstance bot = em.find(BotInstance.class, botId);
				if (bot == null) {
					throw new BotException(new BotInstance().getTypeName() + " does not exist - " + botId);
				}
				instance.setBot(bot);
			}
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public WebMedium addUser(WebMedium instance, String userid) {
		log(Level.INFO, "add user", instance, userid);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			WebMedium managed = (WebMedium)em.find(instance.getClass(), instance.getId());
			if (managed == null) {
				throw new BotException(instance.getDisplayName() + " does not exist - " + instance.getId());
			}
			User user = (User)em.find(User.class, userid);
			if (user == null) {
				throw new BotException("User does not exist - " + userid);
			}
			if (managed.getUsers().contains(user)) {
				throw new BotException("User already added");
			}
			managed.getUsers().add(user);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public WebMedium addAdmin(WebMedium instance, String userid) {
		log(Level.INFO, "add user", instance, userid);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			WebMedium managed = (WebMedium)em.find(instance.getClass(), instance.getId());
			if (managed == null) {
				throw new BotException(instance.getDisplayName() + " does not exist - " + instance.getId());
			}
			User user = (User)em.find(User.class, userid);
			if (user == null) {
				throw new BotException("User does not exist - " + userid);
			}
			if (managed.getAdmins().contains(user)) {
				throw new BotException("User already added");
			}
			managed.getAdmins().add(user);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public ChatChannel addChannelOperator(ChatChannel instance, String userid) {
		log(Level.INFO, "add user", instance, userid);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			ChatChannel managed = (ChatChannel)em.find(instance.getClass(), instance.getId());
			if (managed == null) {
				throw new BotException(instance.getTypeName() + " does not exist - " + instance.getId());
			}
			User user = (User)em.find(User.class, userid);
			if (user == null) {
				throw new BotException("User does not exist - " + userid);
			}
			if (managed.getOperators().contains(user)) {
				throw new BotException("User already added");
			}
			managed.getOperators().add(user);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Forum addForumSubscriber(Forum instance, String userid) {
		log(Level.INFO, "add subscriber", instance, userid);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Forum managed = (Forum)em.find(instance.getClass(), instance.getId());
			if (managed == null) {
				throw new BotException(instance.getDisplayName() + " does not exist - " + instance.getId());
			}
			User user = (User)em.find(User.class, userid);
			if (user == null) {
				throw new BotException("User does not exist - " + userid);
			}
			if (managed.getSubscribers().contains(user)) {
				throw new BotException("User already added");				
			}
			managed.getSubscribers().add(user);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public ForumPost addForumPostSubscriber(ForumPost instance, String userid) {
		log(Level.INFO, "add subscriber", instance, userid);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			ForumPost managed = (ForumPost)em.find(instance.getClass(), instance.getId());
			if (managed == null) {
				throw new BotException(instance.getTypeName() + " does not exist - " + instance.getId());
			}
			User user = (User)em.find(User.class, userid);
			if (user == null) {
				throw new BotException("User does not exist - " + userid);
			}
			if (managed.getSubscribers().contains(user)) {
				throw new BotException("User already added");
			}
			managed.getSubscribers().add(user);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Forum removeForumSubscriber(Forum instance, String userid) {
		log(Level.INFO, "remove subscriber", instance, userid);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Forum managed = (Forum)em.find(instance.getClass(), instance.getId());
			if (managed == null) {
				throw new BotException(instance.getDisplayName() + " does not exist - " + instance.getId());
			}
			User user = (User)em.find(User.class, userid);
			if (user == null) {
				throw new BotException("User does not exist - " + userid);
			}
			managed.getSubscribers().remove(user);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public ForumPost removeForumPostSubscriber(ForumPost instance, String userid) {
		log(Level.INFO, "remove subscriber", instance, userid);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			ForumPost managed = (ForumPost)em.find(instance.getClass(), instance.getId());
			if (managed == null) {
				throw new BotException(instance.getDisplayName() + " does not exist - " + instance.getId());
			}
			User user = (User)em.find(User.class, userid);
			if (user == null) {
				throw new BotException("User does not exist - " + userid);
			}
			managed.getSubscribers().remove(user);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public IssueTracker addIssueTrackerSubscriber(IssueTracker instance, String userid) {
		log(Level.INFO, "add subscriber", instance, userid);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			IssueTracker managed = (IssueTracker)em.find(instance.getClass(), instance.getId());
			if (managed == null) {
				throw new BotException(instance.getDisplayName() + " does not exist - " + instance.getId());
			}
			User user = (User)em.find(User.class, userid);
			if (user == null) {
				throw new BotException("User does not exist - " + userid);
			}
			if (managed.getSubscribers().contains(user)) {
				throw new BotException("User already added");
			}
			managed.getSubscribers().add(user);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Issue addIssueSubscriber(Issue instance, String userid) {
		log(Level.INFO, "add subscriber", instance, userid);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Issue managed = (Issue)em.find(instance.getClass(), instance.getId());
			if (managed == null) {
				throw new BotException(instance.getTypeName() + " does not exist - " + instance.getId());
			}
			User user = (User)em.find(User.class, userid);
			if (user == null) {
				throw new BotException("User does not exist - " + userid);
			}
			if (managed.getSubscribers().contains(user)) {
				throw new BotException("User already added");
			}
			managed.getSubscribers().add(user);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public IssueTracker removeIssueTrackerSubscriber(IssueTracker instance, String userid) {
		log(Level.INFO, "remove subscriber", instance, userid);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			IssueTracker managed = (IssueTracker)em.find(instance.getClass(), instance.getId());
			if (managed == null) {
				throw new BotException(instance.getDisplayName() + " does not exist - " + instance.getId());
			}
			User user = (User)em.find(User.class, userid);
			if (user == null) {
				throw new BotException("User does not exist - " + userid);
			}
			managed.getSubscribers().remove(user);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Issue removeIssueSubscriber(Issue instance, String userid) {
		log(Level.INFO, "remove subscriber", instance, userid);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Issue managed = (Issue)em.find(instance.getClass(), instance.getId());
			if (managed == null) {
				throw new BotException(instance.getTypeName() + " does not exist - " + instance.getId());
			}
			User user = (User)em.find(User.class, userid);
			if (user == null) {
				throw new BotException("User does not exist - " + userid);
			}
			managed.getSubscribers().remove(user);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public WebMedium removeUser(WebMedium instance, String userid) {
		log(Level.INFO, "remove user", instance, userid);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			WebMedium managed = (WebMedium)em.find(instance.getClass(), instance.getId());
			if (managed == null) {
				throw new BotException(instance.getDisplayName() + " does not exist - " + instance.getId());
			}
			User user = (User)em.find(User.class, userid);
			if (user == null) {
				throw new BotException("User does not exist - " + userid);
			}
			managed.getUsers().remove(user);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public WebMedium removeAdmin(WebMedium instance, String userid) {
		log(Level.INFO, "remove user", instance, userid);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			WebMedium managed = (WebMedium)em.find(instance.getClass(), instance.getId());
			if (managed == null) {
				throw new BotException(instance.getDisplayName() + " does not exist - " + instance.getId());
			}
			String[] adminIdArray = userid.split(",");
			for (String id : adminIdArray) {
				User user = (User)em.find(User.class, id);
				if (user == null) {
					throw new BotException("User does not exist - " + id);
				}
				managed.getAdmins().remove(user);
				if (managed.getAdmins().isEmpty()) {
					throw new BotException("Cannot remove only admin");
				}
			}
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public ChatChannel removeChannelOperator(ChatChannel instance, String userid) {
		log(Level.INFO, "remove user", instance, userid);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			ChatChannel managed = (ChatChannel)em.find(instance.getClass(), instance.getId());
			if (managed == null) {
				throw new BotException(instance.getDisplayName() + " does not exist - " + instance.getId());
			}
			User user = (User)em.find(User.class, userid);
			if (user == null) {
				throw new BotException("User does not exist - " + userid);
			}
			managed.getOperators().remove(user);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public ForumPost updateForumPost(ForumPost updatedInstance, String userId, String tags, Domain domain) {
		log(Level.INFO, "update forum post",  updatedInstance.getTopic());
		if (!updatedInstance.getForum().isAdult() && (updatedInstance.checkProfanity() || Utils.checkProfanity(tags))) {
			throw BotException.offensive();
		}
		Utils.checkHTML(tags);
		tags = Utils.sanitize(tags);
		updatedInstance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			ForumPost instance = em.find(ForumPost.class, updatedInstance.getId());
			if (instance == null) {
				throw new BotException("Forum post does not exist - " + updatedInstance.getId());
			}
			instance.setTopic(updatedInstance.getTopic());
			instance.setFeatured(updatedInstance.isFeatured());
			instance.setSummary(updatedInstance.getSummary());
			instance.getDetails().setDetails(updatedInstance.getDetails().getDetails());
			instance.setUpdatedDate(new Date());
			instance.setTagsFromString(tags, em, domain);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Issue updateIssue(Issue updatedInstance, String userId, String tags, Domain domain) {
		log(Level.INFO, "update issue",  updatedInstance.getTitle());
		if (!updatedInstance.getTracker().isAdult() && (updatedInstance.checkProfanity() || Utils.checkProfanity(tags))) {
			throw BotException.offensive();
		}
		Utils.checkHTML(tags);
		tags = Utils.sanitize(tags);
		updatedInstance.checkConstraints();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Issue instance = em.find(Issue.class, updatedInstance.getId());
			if (instance == null) {
				throw new BotException("Issue does not exist - " + updatedInstance.getId());
			}
			instance.setTitle(updatedInstance.getTitle());
			instance.setType(updatedInstance.getType());
			instance.setPriority(updatedInstance.getPriority());
			instance.setStatus(updatedInstance.getStatus());
			instance.setIsPriority(updatedInstance.isPriority());
			instance.setIsHidden(updatedInstance.isHidden());
			instance.setSummary(updatedInstance.getSummary());
			instance.getDetails().setDetails(updatedInstance.getDetails().getDetails());
			instance.setUpdatedDate(new Date());
			instance.setTagsFromString(tags, em, domain);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User blockUser(String userId) {
		log(Level.WARNING, "blocking user",  userId);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User instance = em.find(User.class, userId);
			if (instance == null) {
				throw new BotException("User does not exist - " + userId);
			}
			instance.setBlocked(true);
			IPBanned banned = new IPBanned();
			banned.setIP(instance.getIP());
			em.persist(banned);
			em.getTransaction().commit();
			instance.setEncryptedPassword(null);
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User unblockUser(String userId) {
		log(Level.WARNING, "unblocking user",  userId);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User instance = em.find(User.class, userId);
			if (instance == null) {
				throw new BotException("User does not exist - " + userId);
			}
			instance.setBlocked(false);
			em.getTransaction().commit();
			instance.setEncryptedPassword(null);
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User resetAppId(String userId) {
		log(Level.INFO, "resetting app id",  userId);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User instance = em.find(User.class, userId);
			if (instance == null) {
				throw new BotException("User does not exist - " + userId);
			}
			instance.resetApplicationId();
			em.getTransaction().commit();
			instance.setEncryptedPassword(null);
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User deleteUser(String userId) {
		log(Level.WARNING, "deleting user",  userId);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User instance = em.find(User.class, userId);
			if (instance == null) {
				throw new BotException("User does not exist - " + userId);
			}

			Query query = em.createQuery("Delete from UserMessage m where m.owner = :user");
			query.setParameter("user", instance.detach());
			query.executeUpdate();
			query = em.createQuery("Delete from UserMessage m where m.creator = :user");
			query.setParameter("user", instance.detach());
			query.executeUpdate();
			query = em.createQuery("Delete from UserMessage m where m.target = :user");
			query.setParameter("user", instance.detach());
			query.executeUpdate();

			query = em.createQuery("Delete from ChatMessage m where m.creator = :user");
			query.setParameter("user", instance.detach());
			query.executeUpdate();
			query = em.createQuery("Delete from ChatMessage m where m.target = :user");
			query.setParameter("user", instance.detach());
			query.executeUpdate();
			
			query = em.createQuery("Delete from BotAttachment m where m.creator = :user");
			query.setParameter("user", instance.detach());
			query.executeUpdate();
			query = em.createQuery("Delete from ChannelAttachment m where m.creator = :user");
			query.setParameter("user", instance.detach());
			query.executeUpdate();
			query = em.createQuery("Delete from ForumAttachment m where m.creator = :user");
			query.setParameter("user", instance.detach());
			query.executeUpdate();
			query = em.createQuery("Delete from IssueTrackerAttachment m where m.creator = :user");
			query.setParameter("user", instance.detach());
			query.executeUpdate();
			
			query = em.createQuery("Delete from Vote m where m.user = :user");
			query.setParameter("user", instance.detach());
			query.executeUpdate();
			
			query = em.createQuery("Delete from Friendship fr where fr.userId = :userId or fr.friend = :userId");
			query.setParameter("userId", userId);
			query.executeUpdate();
			em.remove(instance);
			em.getTransaction().commit();
			instance.setEncryptedPassword(null);
			return instance;
		} catch (Exception exception) {
			log(exception);
			throw new BotException("You must first delete your content (bots, forums, channels)");
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public UserMessage deleteUserMessage(UserMessage message) {
		log(Level.WARNING, "deleting message",  message.getId());
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			UserMessage instance = em.find(UserMessage.class, message.getId());
			if (instance == null) {
				throw new BotException("Message does not exist - " + message.getId());
			}
			instance.preDelete(em);
			em.remove(instance);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public boolean deleteUserToUserMessages(UserMessageConfig config) {
		log(Level.WARNING, "deleting user to user messages",  config.user);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User user = em.find(User.class, config.user);
			if (user == null) {
				throw new BotException("User does not exist - " + config.user);
			}
			User targetUser = em.find(User.class, config.target);
			if (targetUser == null) {
				throw new BotException("User does not exist - " + config.target);
			}
			
			Query query = em.createQuery("Delete from UserMessage message where message.owner = :owner and ((message.target = :target and message.creator = :owner) or (message.target = :owner and message.creator = :target))");
			query.setParameter("owner", user.detach());
			query.setParameter("target", targetUser.detach());
			query.executeUpdate();
			em.getTransaction().commit();
			return true;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public ScriptSource deleteScriptSource(Long id) {
		log(Level.WARNING, "deleting script source",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			ScriptSource instance = em.find(ScriptSource.class, id);
			if (instance == null) {
				throw new BotException("Script version does not exist - " + id);
			}
			em.remove(instance);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User flagUser(String userId, String user, String reason) {
		log(Level.WARNING, "flag user",  userId);
		if (Utils.checkProfanity(reason)) {
			throw BotException.offensive();
		}
		Utils.checkHTML(reason);
		reason = Utils.sanitize(reason);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User instance = em.find(User.class, userId);
			if (instance == null) {
				throw new BotException("User does not exist - " + userId);
			}
			instance.setFlagged(true);
			instance.setFlaggedReason(reason);
			instance.setFlaggedUser(user);
			em.getTransaction().commit();
			instance.setEncryptedPassword(null);
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User unflagUser(String userId) {
		log(Level.WARNING, "unflag user",  userId);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User instance = em.find(User.class, userId);
			if (instance == null) {
				throw new BotException("User does not exist - " + userId);
			}
			instance.setFlagged(false);
			instance.setFlaggedReason("");
			instance.setFlaggedUser("");
			em.getTransaction().commit();
			instance.setEncryptedPassword(null);
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public <T extends Flaggable> T flag(T flagged, String userId, String reason) {
		log(Level.WARNING, "flagging", flagged);
		if (Utils.checkProfanity(reason)) {
			throw BotException.offensive();
		}
		Utils.checkHTML(reason);
		reason = Utils.sanitize(reason);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			T instance = (T)em.find(flagged.getClass(), flagged.getId());
			if (instance == null) {
				throw new BotException(flagged.getDisplayName() + " does not exist - " + flagged.getId());
			}
			instance.setFlagged(true);
			instance.setFlaggedReason(reason);
			instance.setFlaggedUser(userId);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public <T extends Flaggable> T thumbsUp(T flagged, String userId) {
		log(Level.FINE, "thumbsup", flagged);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			T instance = (T)em.find(flagged.getClass(), flagged.getId());
			if (instance == null) {
				throw new BotException(flagged.getDisplayName() + " does not exist - " + flagged.getId());
			}
			Vote vote = em.find(Vote.class, new VoteId(userId, flagged.getId()));
			if (vote == null) {
				vote = new Vote(userId, flagged.getId());
				vote.setThumbsUp(true);
				vote.setStars(5);
				int votes = instance.getThumbsUp() + instance.getThumbsDown();
				instance.setStars(((instance.getStars() * votes) + 5f) / (votes + 1));
				em.persist(vote);
			} else {
				if (vote.isThumbsUp()) {
					throw new BotException("You already voted thumbs up");
				}
				vote.setThumbsUp(true);
				instance.setThumbsDown(instance.getThumbsDown() - 1);
			}
			instance.setThumbsUp(instance.getThumbsUp() + 1);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateWins(BotInstance bot, int wins, int losses, int rank) {
		log(Level.FINE, "vote", instance);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, bot.getId());
			if (instance == null) {
				throw new BotException("Bot does not exist - " + bot.getId());
			}
			instance.setWins(wins);
			instance.setLosses(losses);
			instance.setRank(rank);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public <T extends Flaggable> T thumbsDown(T flagged, String userId) {
		log(Level.FINE, "thumbsdown", flagged);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			T instance = (T)em.find(flagged.getClass(), flagged.getId());
			if (instance == null) {
				throw new BotException(flagged.getDisplayName() + " does not exist - " + flagged.getId());
			}
			Vote vote = em.find(Vote.class, new VoteId(userId, flagged.getId()));
			if (vote == null) {
				vote = new Vote(userId, flagged.getId());
				vote.setThumbsUp(false);
				vote.setStars(2);
				int votes = instance.getThumbsUp() + instance.getThumbsDown();
				instance.setStars(((instance.getStars() * votes) + 2f) / (votes + 1));
				em.persist(vote);
			} else {
				if (!vote.isThumbsUp()) {
					throw new BotException("You already voted thumbs down");
				}
				vote.setThumbsUp(false);
				instance.setThumbsUp(instance.getThumbsUp() - 1);
			}
			instance.setThumbsDown(instance.getThumbsDown() + 1);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public <T extends Flaggable> T star(T flagged, String userId, int stars) {
		log(Level.FINE, "star", stars, flagged);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			T instance = (T)em.find(flagged.getClass(), flagged.getId());
			if (instance == null) {
				throw new BotException(flagged.getDisplayName() + " does not exist - " + flagged.getId());
			}
			Vote vote = em.find(Vote.class, new VoteId(userId, flagged.getId()));
			int votes = instance.getThumbsUp() + instance.getThumbsDown();
			if (vote == null) {
				vote = new Vote(userId, flagged.getId());
				vote.setStars(stars);
				instance.setStars(((instance.getStars() * votes) + (float)stars) / (votes + 1));
				if (stars < 3) {
					instance.setThumbsDown(instance.getThumbsDown() + 1);
				} else {
					instance.setThumbsUp(instance.getThumbsUp() + 1);
				}
				em.persist(vote);
			} else {
				instance.setStars(((instance.getStars() * votes) - (float)vote.getStars() + (float)stars) / votes);
				vote.setStars(stars);
			}
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public <T extends Flaggable> T unflag(T flagged, String userId) {
		log(Level.WARNING, "unflagging", flagged);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			T instance = (T)em.find(flagged.getClass(), flagged.getId());
			if (instance == null) {
				throw new BotException(flagged.getClass().getSimpleName() + " does not exist - " + flagged.getId());
			}
			instance.setFlagged(false);
			instance.setFlaggedReason("");
			instance.setFlaggedUser("");
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public DomainForwarder findDomainForwarder(String subdomain) {
		log(Level.FINE, "find domain forwarder",  subdomain);
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select d from DomainForwarder d where d.subdomain = :subdomain or d.domain = :subdomain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("subdomain", subdomain);
			List<DomainForwarder> results = query.getResultList();
			if (results.isEmpty()) {
				return null;
			}
			return results.get(0);
		} finally {
			em.close();
		}
	}
	
	public AvatarImage findAvatar(long id) {
		log(Level.FINE, "find image",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			AvatarImage avatar = em.find(AvatarImage.class, id);
			if (avatar == null) {
				throw new BotException("Image does not exist - " + id);
			}
			return avatar;
		} finally {
			em.close();
		}
	}
	
	public Media findMedia(long id) {
		log(Level.FINE, "find media",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			Media media = em.find(Media.class, id);
			if (media == null) {
				throw new BotException("Media does not exist - " + id);
			}
			return media;
		} finally {
			em.close();
		}
	}
	
	public Translation findTranslation(TranslationId id) {
		log(Level.FINE, "find translation",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put("eclipselink.read-only", "true");
			return em.find(Translation.class, id, properties);
		} finally {
			em.close();
		}
	}
	
	public BotTranslation findBotTranslation(TranslationId id) {
		log(Level.FINE, "find translation",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put("eclipselink.read-only", "true");
			return em.find(BotTranslation.class, id, properties);
		} finally {
			em.close();
		}
	}
	
	public ChannelAttachment findChannelAttachment(long id) {
		log(Level.FINE, "find channel attachment",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			ChannelAttachment attachment = em.find(ChannelAttachment.class, id);
			if (attachment == null) {
				throw new BotException("Attachment does not exist - " + id);
			}
			return attachment;
		} finally {
			em.close();
		}
	}
	
	public BotAttachment findBotAttachment(long id) {
		log(Level.FINE, "find bot attachment",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			BotAttachment attachment = em.find(BotAttachment.class, id);
			if (attachment == null) {
				throw new BotException("Attachment does not exist - " + id);
			}
			return attachment;
		} finally {
			em.close();
		}
	}
	
	public ForumAttachment findForumAttachment(long id) {
		log(Level.FINE, "find forum attachment",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			ForumAttachment attachment = em.find(ForumAttachment.class, id);
			if (attachment == null) {
				throw new BotException("Attachment does not exist - " + id);
			}
			return attachment;
		} finally {
			em.close();
		}
	}
	
	public IssueTrackerAttachment findIssueTrackerAttachment(long id) {
		log(Level.FINE, "find issuetracker attachment",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			IssueTrackerAttachment attachment = em.find(IssueTrackerAttachment.class, id);
			if (attachment == null) {
				throw new BotException("Attachment does not exist - " + id);
			}
			return attachment;
		} finally {
			em.close();
		}
	}
	
	public Tag findTag(String tag, String type, Domain domain) {
		log(Level.FINE, "find tag", tag);
		EntityManager em =  getFactory().createEntityManager();
		try {
			try {
				Query query = em.createQuery("Select t from Tag t where t.name = :name and t.type = :type and t.domain = :domain");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("name", tag);
				query.setParameter("type", type);
				query.setParameter("domain", domain);
				return (Tag)query.getSingleResult();
			} catch (Exception exception) {
				throw new BotException("Tag does not exist - " + tag);
			}
		} finally {
			em.close();
		}
	}
	
	public Category findCategory(String name, String type, Domain domain) {
		log(Level.FINE, "find category", name);
		EntityManager em =  getFactory().createEntityManager();
		try {
			try {
				Query query = em.createQuery("Select t from Category t where t.name = :name and t.type = :type and t.domain = :domain");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("name", name);
				query.setParameter("type", type);
				query.setParameter("domain", domain);
				return (Category)query.getSingleResult();
			} catch (Exception exception) {
				throw new BotException("Category does not exist - " + name);
			}
		} finally {
			em.close();
		}
	}
	
	public void shareAvatar(AvatarImage avatar) {
		log(Level.INFO, "share image",  avatar.getName());
		if (Utils.checkProfanity(avatar.getName()) || Utils.checkProfanity(avatar.getLicense())) {
			throw BotException.offensive();
		}
		Utils.checkHTML(avatar.getName());
		Utils.checkHTML(avatar.getLicense());
		avatar.setName(Utils.sanitize(avatar.getName()));
		avatar.setLicense(Utils.sanitize(avatar.getLicense()));
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(avatar);
			em.getTransaction().commit();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public AvatarImage updateAvatar(AvatarImage avatar) {
		log(Level.INFO, "update avatar",  avatar);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			em.merge(avatar);
			em.getTransaction().commit();
			return avatar;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User updateUser(String userId, byte[] image) {
		log(Level.INFO, "update image",  userId);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User user = em.find(User.class, userId);
			if (user == null) {
				throw new BotException("User does not exist - " + userId);
			}
			if (image != null) {
				AvatarImage avatar = new AvatarImage();
				avatar.setImage(image);
				user.setAvatar(avatar);
			}
			em.getTransaction().commit();
			user.setEncryptedPassword(null);
			return user;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User verifyUser(String userId, String token) {
		log(Level.INFO, "verify user", userId, token);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User user = em.find(User.class, userId);
			if (user == null) {
				throw new BotException("User does not exist - " + userId);
			}
			if (!String.valueOf(user.getVerifyToken()).equals(token)) {
				throw new BotException("Email verification failed, tokens do not match");
			}
			user.setVerified(true);
			em.getTransaction().commit();
			user.setEncryptedPassword(null);
			return user;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User unsubscribe(String unsubscribe, String userId, String token) {
		log(Level.INFO, "unsubscribe user", unsubscribe, userId, token);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User user = em.find(User.class, userId);
			if (user == null) {
				throw new BotException("User does not exist - " + userId);
			}
			if (!String.valueOf(user.getVerifyToken()).equals(token)) {
				throw new BotException("Email verification failed, tokens do not match");
			}
			if (unsubscribe.equals("messages")) {
				user.setEmailMessages(false);
			} else if (unsubscribe.equals("summary")) {
				user.setEmailSummary(false);
			} else if (unsubscribe.equals("all")) {
				user.setEmailSummary(false);
				user.setEmailMessages(false);
				user.setEmailNotices(false);
			}
			em.getTransaction().commit();
			user.setEncryptedPassword(null);
			return user;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User resetPassword(String userId, String token) {
		log(Level.INFO, "reset password", userId);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User user = em.find(User.class, userId);
			if (user == null) {
				throw new BotException("User does not exist - " + userId);
			}
			if (!String.valueOf(user.getVerifyToken()).equals(token)) {
				throw new BotException("Password reset verification failed, tokens do not match");
			}
			em.getTransaction().commit();
			user.setEncryptedPassword(null);
			return user;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User resetPasswordComplete(String userId, String password, String password2) {
		log(Level.INFO, "reset password complete", userId);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User user = em.find(User.class, userId);
			if (user == null) {
				throw new BotException("User does not exist - " + userId);
			}
			if (!password.equals(password2)) {
				throw new BotException("Passwords do not match");
			}
			user.setEncryptedPassword(encrypt(user.getUserId(), password));
			user.setPassword(null);
			em.getTransaction().commit();
			user.setEncryptedPassword(null);
			return user;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User resetUserVerifyToken(String userId) {
		log(Level.INFO, "reset user verify token", userId);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User user = em.find(User.class, userId);
			if (user == null) {
				throw new BotException("User does not exist - " + userId);
			}
			user.resetVerifyToken();
			em.getTransaction().commit();
			user.setEncryptedPassword(null);
			return user;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Category updateCategory(Long id, byte[] image) {
		log(Level.INFO, "update category", id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Category instance = em.find(Category.class, id);
			if (instance == null) {
				throw new BotException("Category does not exist - " + id);
			}
			if (image != null) {
				AvatarImage avatar = new AvatarImage();
				avatar.setImage(image);
				instance.setAvatar(avatar);
			}
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Category updateCategoryCount(Category category, int count) {
		log(Level.INFO, "update category count", category.getName(), count);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Category instance = em.find(Category.class, category.getId());
			if (instance == null) {
				throw new BotException("Category does not exist - " + category.getId());
			}
			instance.setCount(count);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public <T extends WebMedium> T resetIcon(T medium) {
		log(Level.INFO, "reset image", medium);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			T instance = (T)em.find(medium.getClass(), medium.getId());
			if (instance == null) {
				throw new BotException(medium.getDisplayName() + " does not exist - " + medium.getId());
			}
			instance.setAvatar(null);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public <T extends WebMedium> T update(T medium, byte[] image) {
		log(Level.INFO, "update image", medium);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			T instance = (T)em.find(medium.getClass(), medium.getId());
			if (instance == null) {
				throw new BotException(medium.getDisplayName() + " does not exist - " + medium.getId());
			}
			if (image != null) {
				AvatarImage avatar = new AvatarImage();
				avatar.setImage(image);
				instance.setAvatar(avatar);
			}
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceTwitter(long id, boolean tweet) {
		log(Level.INFO, "update twitter",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, id);
			if (instance == null) {
				throw new BotException("Bot does not exist - " + id);
			}
			instance.setEnableTwitter(tweet);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceTimers(long id, boolean enable) {
		log(Level.INFO, "update timers",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, id);
			if (instance == null) {
				throw new BotException("Bot does not exist - " + id);
			}
			instance.setEnableTimers(enable);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceFacebook(long id, boolean enable) {
		log(Level.INFO, "update facebook",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, id);
			if (instance == null) {
				throw new BotException("Bot does not exist - " + id);
			}
			instance.setEnableFacebook(enable);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceTelegram(long id, boolean enable) {
		log(Level.INFO, "update telegram",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, id);
			if (instance == null) {
				throw new BotException("Bot does not exist - " + id);
			}
			instance.setEnableTelegram(enable);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceSlack(long id, boolean enable) {
		log(Level.INFO, "update slack",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, id);
			if (instance == null) {
				throw new BotException("Bot does not exist - " + id);
			}
			instance.setEnableSlack(enable);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceSkype(long id, boolean enable) {
		log(Level.INFO, "update skype",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, id);
			if (instance == null) {
				throw new BotException("Bot does not exist - " + id);
			}
			instance.setEnableSkype(enable);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceWeChat(long id, boolean enable) {
		log(Level.INFO, "update wechat",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, id);
			if (instance == null) {
				throw new BotException("Bot does not exist - " + id);
			}
			instance.setEnableWeChat(enable);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceKik(long id, boolean enable) {
		log(Level.INFO, "update kik",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, id);
			if (instance == null) {
				throw new BotException("Bot does not exist - " + id);
			}
			instance.setEnableKik(enable);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceWolframAlpha(long id, boolean enable) {
		log(Level.INFO, "update wolfram alpha",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, id);
			if (instance == null) {
				throw new BotException("Bot does not exist - " + id);
			}
			instance.setEnableWolframAlpha(enable);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceAlexa(long id, boolean enable) {
		log(Level.INFO, "update alexa",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, id);
			if (instance == null) {
				throw new BotException("Bot does not exist - " + id);
			}
			instance.setEnableAlexa(enable);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceGoogleAssistant(long id, boolean enable) {
		log(Level.INFO, "update google assistant",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, id);
			if (instance == null) {
				throw new BotException("Bot does not exist - " + id);
			}
			instance.setEnableGoogleAssistant(enable);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceEmail(long id, boolean email) {
		log(Level.INFO, "update email",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, id);
			if (instance == null) {
				throw new BotException("Bot does not exist - " + id);
			}
			instance.setEnableEmail(email);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User updateUserInstanceVoice(String id, String voice, String mod, Boolean nativeVoice, String nativeVoiceProvider, String nativeVoiceName, String language, String pitch, String speechRate, String nativeVoiceApiKey, String nativeVoiceAppId, String voiceApiEndpoint) {
		log(Level.INFO, "update voice",  id, voice);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User user = em.find(User.class, id);
			if (user == null) {
				throw new BotException("User does not exist - " + id);
			}
			user.setVoice(voice == null ? "" : voice);
			user.setVoiceMod(mod == null ? "" : mod);
			user.setNativeVoice(nativeVoice == null ? false : nativeVoice);
			user.setNativeVoiceProvider(nativeVoiceProvider);
			user.setNativeVoiceName(nativeVoiceName == null ? "" : nativeVoiceName);
			user.setNativeVoiceApiKey(nativeVoiceApiKey == null ? "": nativeVoiceApiKey);
			user.setNativeVoiceAppId(nativeVoiceAppId == null ? "": nativeVoiceAppId);
			user.setVoiceApiEndpoint(voiceApiEndpoint == null ? "": voiceApiEndpoint);
			user.setLanguage(language == null ? "en" : language);
			user.setPitch(pitch == null ? "" : pitch);
			user.setSpeechRate(speechRate == null ? "" : speechRate);
			em.getTransaction().commit();
			return user;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceVoice(long id, String voice, String mod, Boolean nativeVoice, String nativeVoiceProvider, String nativeVoiceName, String language, String pitch, String speechRate, String nativeVoiceApiKey, String nativeVoiceAppId, String voiceApiEndpoint) {
		log(Level.INFO, "update voice",  id, voice);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, id);
			if (instance == null) {
				throw new BotException("Bot does not exist - " + id);
			}
			instance.setVoice(voice == null ? "" : voice);
			instance.setVoiceMod(mod == null ? "" : mod);
			instance.setNativeVoice(nativeVoice == null ? false : nativeVoice);
			instance.setNativeVoiceProvider(nativeVoiceProvider);
			instance.setNativeVoiceName(nativeVoiceName == null ? "" : nativeVoiceName);
			instance.setNativeVoiceApiKey(nativeVoiceApiKey == null ? "": nativeVoiceApiKey);
			instance.setNativeVoiceAppId(nativeVoiceAppId == null ? "": nativeVoiceAppId);
			instance.setVoiceApiEndpoint(voiceApiEndpoint == null ? "": voiceApiEndpoint);
			instance.setLanguage(language == null ? "en" : language);
			instance.setPitch(pitch == null ? "" : pitch);
			instance.setSpeechRate(speechRate == null ? "" : speechRate);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceSettings(long id, Boolean allowJavaScript, Boolean disableFlag) {
		log(Level.INFO, "update settings",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, id);
			if (instance == null) {
				throw new BotException("Bot does not exist - " + id);
			}
			if (allowJavaScript != null) {
				instance.setAllowJavaScript(allowJavaScript);
			}
			if (disableFlag != null) {
				instance.setDisableFlag(disableFlag);
			}
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceSchema(long id, boolean isSchema) {
		log(Level.INFO, "update schema",  id, isSchema);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, id);
			if (instance == null) {
				throw new BotException("Bot does not exist - " + id);
			}
			instance.setSchema(isSchema);
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User updateUserInstanceAvatar(String id, Long avatarId) {
		log(Level.INFO, "update avatar",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			User user = em.find(User.class, id);
			//UserBean userBean = em.find(UserBean.class, id);
			if (user == null) {
				throw new BotException("User does not exist - " + id);
			}
			if (avatarId != null) {
				Avatar avatar = em.find(Avatar.class, avatarId);
				if (avatar == null) {
					throw new BotException("Avatar does not exist - " + avatarId);
				}
				user.setInstanceAvatar(avatar);
			} else {
				user.setInstanceAvatar(null);
			}
			em.getTransaction().commit();
			return user;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance updateInstanceAvatar(long id, Long avatarId) {
		log(Level.INFO, "update avatar",  id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotInstance instance = em.find(BotInstance.class, id);
			if (instance == null) {
				throw new BotException("Bot does not exist - " + id);
			}
			if (avatarId != null) {
				Avatar avatar = em.find(Avatar.class, avatarId);
				if (avatar == null) {
					throw new BotException("Avatar does not exist - " + avatarId);
				}
				instance.setInstanceAvatar(avatar);
			} else {
				instance.setInstanceAvatar(null);
			}
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public void delete(Flaggable instance) {
		log(Level.INFO, "delete", instance);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Flaggable managed = em.find(instance.getClass(), instance.getId());
			if (managed == null) {
				throw new BotException(instance.getDisplayName() + " does not exist - " + instance.getId());
			}
			managed.preDelete(em);
			em.remove(managed);
			em.getTransaction().commit();
			return;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public void deleteChannelAttachment(ChannelAttachment instance) {
		log(Level.INFO, "delete", instance);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			ChannelAttachment managed = em.find(instance.getClass(), instance.getMediaId());
			if (managed == null) {
				throw new BotException("Attachment does not exist - " + instance.getMediaId());
			}
			Media media = em.find(Media.class, managed.getMediaId());
			if (media != null) {
				em.remove(media);
			}
			em.remove(managed);
			em.getTransaction().commit();
			return;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public void deleteBotAttachment(BotAttachment instance) {
		log(Level.INFO, "delete", instance);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			BotAttachment managed = em.find(instance.getClass(), instance.getMediaId());
			if (managed == null) {
				throw new BotException("Attachment does not exist - " + instance.getMediaId());
			}
			Media media = em.find(Media.class, managed.getMediaId());
			if (media != null) {
				em.remove(media);
			}
			em.remove(managed);
			em.getTransaction().commit();
			return;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public void deleteIssueTrackerAttachment(IssueTrackerAttachment instance) {
		log(Level.INFO, "delete", instance);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			IssueTrackerAttachment managed = em.find(instance.getClass(), instance.getMediaId());
			if (managed == null) {
				throw new BotException("Attachment does not exist - " + instance.getMediaId());
			}
			Media media = em.find(Media.class, managed.getMediaId());
			if (media != null) {
				em.remove(media);
			}
			em.remove(managed);
			em.getTransaction().commit();
			return;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public void deleteForumAttachment(ForumAttachment instance) {
		log(Level.INFO, "delete", instance);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			ForumAttachment managed = em.find(instance.getClass(), instance.getMediaId());
			if (managed == null) {
				throw new BotException("Attachment does not exist - " + instance.getMediaId());
			}
			Media media = em.find(Media.class, managed.getMediaId());
			if (media != null) {
				em.remove(media);
			}
			em.remove(managed);
			em.getTransaction().commit();
			return;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public void deleteCategory(Long id) {
		log(Level.INFO, "delete category", id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Category managed = em.find(Category.class, id);
			if (managed == null) {
				throw new BotException("Category does not exist - " + id);
			}
			List<Category> parents = managed.getParents();
			List<Category> childrens = managed.getChildren();
			for (Category parent : parents) {
				parent.getChildren().remove(managed);
			}
			for (Category child : childrens) {
				child.getParents().remove(managed);
			}
			if (managed.type.equals("Bot")) {
				Query query = em.createQuery("Select b from BotInstance b join b.categories c where c.id = :id");
				query.setParameter("id", id);
				List<BotInstance> bots = query.getResultList();
				for (BotInstance bot : bots) {
					bot.getCategories().remove(managed);
				}
			}
			else if (managed.type.equals("Avatar")) {
				Query query = em.createQuery("Select a from Avatar a join a.categories c where c.id = :id");
				query.setParameter("id", id);
				List<Avatar> avatars = query.getResultList();
				for (Avatar avatar : avatars) {
					avatar.getCategories().remove(managed);
				}
			}
			else if (managed.type.equals("Script")) {
				Query query = em.createQuery("Select sc from Script sc join sc.categories c where c.id = :id");
				query.setParameter("id", id);
				List<Script> scripts = query.getResultList();
				for (Script script : scripts) {
					script.getCategories().remove(managed);
				}
			}
			else if (managed.type.equals("Channel")) {
				Query query = em.createQuery("Select cch from ChatChannel cch join cch.categories c where c.id = :id");
				query.setParameter("id", id);
				List<ChatChannel> channels = query.getResultList();
				for (ChatChannel channel : channels) {
					channel.getCategories().remove(managed);
				}
			}
			else if (managed.type.equals("Forum")) {
				Query query = em.createQuery("Select fr from Forum fr join fr.categories c where c.id = :id");
				query.setParameter("id", id);
				List<Forum> forums = query.getResultList();
				for (Forum forum : forums) {
					forum.getCategories().remove(managed);
				}
			}
			else if (managed.type.equals("IssueTracker")) {
				Query query = em.createQuery("Select t from IssueTracker t join t.categories c where c.id = :id");
				query.setParameter("id", id);
				List<IssueTracker> trackers = query.getResultList();
				for (IssueTracker tracker : trackers) {
					tracker.getCategories().remove(managed);
				}
			}
			else if (managed.type.equals("Graphic")) {
				Query query = em.createQuery("Select gr from Graphic gr join gr.categories c where c.id = :id");
				query.setParameter("id", id);
				List<Graphic> graphics = query.getResultList();
				for (Graphic graphic : graphics) {
					graphic.getCategories().remove(managed);
				}
			}
			else if (managed.type.equals("Domain")) {
				Query query = em.createQuery("Select d from Domain d join d.categories c where c.id = :id");
				query.setParameter("id", id);
				List<Domain> domains = query.getResultList();
				for (Domain domain : domains) {
					domain.getCategories().remove(managed);
				}
			}
			em.remove(managed);
			em.getTransaction().commit();
			return;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public void deleteTag(Long id) {
		log(Level.INFO, "delete tag", id);
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Tag managed = em.find(Tag.class, id);
			if (managed == null) {
				throw new BotException("Tag does not exist - " + id);
			}
			if (managed.type.equals("Bot")) {
				Query query = em.createQuery("Select b from BotInstance b join b.tags c where c.id = :id");
				query.setParameter("id", id);
				List<BotInstance> bots = query.getResultList();
				for (BotInstance bot : bots) {
					bot.getTags().remove(managed);
				}
			} else if (managed.type.equals("Avatar")) {
				Query query = em.createQuery("Select a from Avatar a join a.tags c where c.id = :id");
				query.setParameter("id", id);
				List<Avatar> avatars = query.getResultList();
				for (Avatar avatar : avatars) {
					avatar.getTags().remove(managed);
				}
			} else if (managed.type.equals("Script")) {
				Query query = em.createQuery("Select sc from Script sc join sc.tags c where c.id = :id");
				query.setParameter("id", id);
				List<Script> scripts = query.getResultList();
				for (Script script : scripts) {
					script.getTags().remove(managed);
				}
			} else if (managed.type.equals("Channel")) {
				Query query = em.createQuery("Select cch from ChatChannel cch join cch.tags c where c.id = :id");
				query.setParameter("id", id);
				List<ChatChannel> channels = query.getResultList();
				for (ChatChannel channel : channels) {
					channel.getTags().remove(managed);
				}
			} else if (managed.type.equals("Forum")) {
				Query query = em.createQuery("Select fr from Forum fr join fr.tags c where c.id = :id");
				query.setParameter("id", id);
				List<Forum> forums = query.getResultList();
				for (Forum forum : forums) {
					forum.getTags().remove(managed);
				}
			} else if (managed.type.equals("IssueTracker")) {
				Query query = em.createQuery("Select t from IssueTracker t join t.tags c where c.id = :id");
				query.setParameter("id", id);
				List<IssueTracker> trackers = query.getResultList();
				for (IssueTracker tracker : trackers) {
					tracker.getTags().remove(managed);
				}
			} else if (managed.type.equals("Graphic")) {
				Query query = em.createQuery("Select gr from Graphic gr join gr.tags c where c.id = :id");
				query.setParameter("id", id);
				List<Graphic> graphics = query.getResultList();
				for (Graphic graphic : graphics) {
					graphic.getTags().remove(managed);
				}
			} else if (managed.type.equals("Domain")) {
				Query query = em.createQuery("Select d from Domain d join d.tags c where c.id = :id");
				query.setParameter("id", id);
				List<Domain> domains = query.getResultList();
				for (Domain domain : domains) {
					domain.getTags().remove(managed);
				}
			}
			em.remove(managed);
			em.getTransaction().commit();
			return;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public void clearBannedUsers() {
		log(Level.INFO, "clear banned users");
		bannedIPs.clear();
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			List<IPBanned> banned = em.createQuery("Select b from IPBanned b").getResultList();
			for (IPBanned ip : banned) {
				em.remove(ip);
			}
			em.getTransaction().commit();
			return;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public <T extends WebMedium> T incrementConnects(T instance, ClientType clientType, User user) {
		if (user != null && (user.isSuperUser() || user.isAdminUser())) {
			return instance;
		}
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			T managed = (T)em.find(instance.getClass(), instance.getId());
			if (managed == null) {
				throw new BotException(instance.getDisplayName() + " does not exist - " + instance.getId());
			}
			managed.incrementConnects(clientType, user);
			synchronized (getLock(instance.getId())) {
				em.getTransaction().commit();
			}
			return managed;
		} catch (BotException exception) {
			throw exception;
		} catch (Exception ignore) {
			// Ignore concurrency errors.
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public ChatChannel updateConnected(ChatChannel instance, int connected) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			ChatChannel managed = (ChatChannel)em.find(ChatChannel.class, instance.getId());
			if (managed == null) {
				throw new BotException(instance.getDisplayName() + " does not exist - " + instance.getId());
			}
			managed.setConnectedUsersCount(connected);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public ChatChannel updateConnectedAdmins(ChatChannel instance, int connected) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			ChatChannel managed = (ChatChannel)em.find(ChatChannel.class, instance.getId());
			if (managed == null) {
				throw new BotException(instance.getTypeName() + " does not exist - " + instance.getId());
			}
			managed.setConnectedAdminsCount(connected);
			em.getTransaction().commit();
			return managed;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public <T extends WebMedium> T validate(Class<T> type, long id, String userId) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			if (id == 0) {
				throw new BotException("Invalid " + type.newInstance().getDisplayName());
			}
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put("eclipselink.read-only", "true");
			T instance = em.find(type, id, properties);
			if (instance == null) {
				throw new BotException(type.newInstance().getDisplayName() + " does not exist - " + id);
			}
			return instance;
		} catch (IllegalAccessException exception) {
			throw new Error(exception);
		} catch (InstantiationException exception) {
			throw new Error(exception);
		} finally {
			em.close();
		}
	}
	
	public <T extends WebMedium> T validate(Class<T> type, String alias, String userId, Domain domain) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			if ((alias == null) || alias.isEmpty()) {
				throw new BotException("Invalid " + type.newInstance().getDisplayName());
			}
			List<T> results = null;
			if (type.equals(Domain.class)) {
				Query query = em.createQuery("Select p from " + type.getSimpleName() + " p where p.alias = :alias");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("alias", alias);
				results = query.getResultList();
				if (results.isEmpty()) {
					query = em.createQuery("Select p from " + type.getSimpleName() + " p where p.name = :name");
					query.setHint("eclipselink.read-only", "true");
					query.setParameter("name", alias);
					results = query.getResultList();
				}
			} else {
				Query query = em.createQuery("Select p from " + type.getSimpleName() + " p where p.alias = :alias and p.domain = :domain");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("alias", alias);
				query.setParameter("domain", domain);
				results = query.getResultList();
				if (results.isEmpty()) {
					query = em.createQuery("Select p from " + type.getSimpleName() + " p where p.name = :name and p.domain = :domain");
					query.setHint("eclipselink.read-only", "true");
					query.setParameter("name", alias);
					query.setParameter("domain", domain);
					results = query.getResultList();
				}
			}
			if (results.isEmpty()) {
				throw new BotException(type.newInstance().getDisplayName() + " does not exist - " + alias);
			}
			T instance = results.get(0);
			return instance;
		} catch (IllegalAccessException exception) {
			throw new Error(exception);
		} catch (InstantiationException exception) {
			throw new Error(exception);
		} finally {
			em.close();
		}
	}
	
	public ForumPost validateForumPost(long id, String userId, ClientType clientType) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			if (id == 0) {
				throw new BotException("Invalid forum post");
			}
			em.getTransaction().begin();
			ForumPost instance = em.find(ForumPost.class, id);
			if (instance == null) {
				throw new BotException("Forum post does not exist - " + id);
			}
			User user = em.find(User.class, userId);
			instance.getForum().checkAccess(user);
			if ((user == null) || !(user.isSuperUser() || user.isAdminUser())) {
				instance.incrementViews(clientType);
			}
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public Issue validateIssue(long id, String userId, ClientType clientType) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			if (id == 0) {
				throw new BotException("Invalid issue");
			}
			em.getTransaction().begin();
			Issue instance = em.find(Issue.class, id);
			if (instance == null) {
				throw new BotException("Issue does not exist - " + id);
			}
			User user = em.find(User.class, userId);
			instance.getTracker().checkAccess(user);
			if ((user == null) || !(user.isSuperUser() || user.isAdminUser())) {
				instance.incrementViews(clientType);
			}
			em.getTransaction().commit();
			return instance;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public BotInstance getDefaultTemplate(Domain domain) {
		try {
			return validateTemplate("template", domain);
		} catch (BotException missing) {
			return null;
		}
		
	}
	
	public void validateNewInstance(String alias, String description, String tags, boolean isAdult, Domain domain) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from BotInstance p where p.alias = :alias and p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", alias);
			query.setParameter("domain", domain);
			List<BotInstance> results = query.getResultList();
			if (!results.isEmpty()) {
				throw new BotException("Bot already exists - " + alias);
			}
			if (!isAdult && (Utils.checkProfanity(alias) || Utils.checkProfanity(description) || Utils.checkProfanity(tags))) {
				throw BotException.offensive();
			}
		} finally {
			em.close();
		}
	}
	
	public void validateNewDomain(String alias, String description, String tags, boolean isAdult) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from Domain p where p.alias = :alias");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", alias);
			List<Forum> results = query.getResultList();
			if (!results.isEmpty()) {
				throw new BotException("Workspace already exists - " + alias);
			}
			if (!isAdult && (Utils.checkProfanity(alias) || Utils.checkProfanity(description) || Utils.checkProfanity(tags))) {
				throw BotException.offensive();
			}
		} finally {
			em.close();
		}
	}
	
	public void validateNewForum(String alias, String description, String tags, boolean isAdult, Domain domain) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from Forum p where p.alias = :alias and p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", alias);
			query.setParameter("domain", domain);
			List<Forum> results = query.getResultList();
			if (!results.isEmpty()) {
				throw new BotException("Forum already exists - " + alias);
			}
			if (!isAdult && (Utils.checkProfanity(alias) || Utils.checkProfanity(description) || Utils.checkProfanity(tags))) {
				throw BotException.offensive();
			}
		} finally {
			em.close();
		}
	}
	
	public void validateNewIssueTracker(String alias, String description, String tags, boolean isAdult, Domain domain) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from IssueTracker p where p.alias = :alias and p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", alias);
			query.setParameter("domain", domain);
			List<Forum> results = query.getResultList();
			if (!results.isEmpty()) {
				throw new BotException("IssueTracker already exists - " + alias);
			}
			if (!isAdult && (Utils.checkProfanity(alias) || Utils.checkProfanity(description) || Utils.checkProfanity(tags))) {
				throw BotException.offensive();
			}
		} finally {
			em.close();
		}
	}
	
	public void validateNewAvatar(String alias, String description, String tags, boolean isAdult, Domain domain) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from Avatar p where p.alias = :alias and p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", alias);
			query.setParameter("domain", domain);
			List<Avatar> results = query.getResultList();
			if (!results.isEmpty()) {
				throw new BotException("Avatar already exists - " + alias);
			}
			if (!isAdult && (Utils.checkProfanity(alias) || Utils.checkProfanity(description) || Utils.checkProfanity(tags))) {
				throw BotException.offensive();
			}
		} finally {
			em.close();
		}
	}
	
	public void validateNewGraphic(String alias, String description, String tags, boolean isAdult, Domain domain) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from Graphic p where p.alias = :alias and p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", alias);
			query.setParameter("domain", domain);
			List<Avatar> results = query.getResultList();
			if (!results.isEmpty()) {
				throw new BotException("Graphic already exists - " + alias);
			}
			if (!isAdult && (Utils.checkProfanity(alias) || Utils.checkProfanity(description) || Utils.checkProfanity(tags))) {
				throw BotException.offensive();
			}
		} finally {
			em.close();
		}
	}
	
	public void validateNewScript(String alias, String description, String tags, boolean isAdult, Domain domain) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from Script p where p.alias = :alias and p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", alias);
			query.setParameter("domain", domain);
			List<Forum> results = query.getResultList();
			if (!results.isEmpty()) {
				throw new BotException("Script already exists - " + alias);
			}
			if (!isAdult && (Utils.checkProfanity(alias) || Utils.checkProfanity(description) || Utils.checkProfanity(tags))) {
				throw BotException.offensive();
			}
		} finally {
			em.close();
		}
	}
	
	public void validateNewChannel(String alias, String description, String tags, boolean isAdult, Domain domain) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from ChatChannel p where p.alias = :alias and p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", alias);
			query.setParameter("domain", domain);
			List<Forum> results = query.getResultList();
			if (!results.isEmpty()) {
				throw new BotException("Channel already exists - " + alias);
			}
			if (!isAdult && (Utils.checkProfanity(alias) || Utils.checkProfanity(description) || Utils.checkProfanity(tags))) {
				throw BotException.offensive();
			}
		} finally {
			em.close();
		}
	}
	
	public void validateNewForumPost(String topic, String details, String tags, boolean isAdult) {
		if (!isAdult && (Utils.checkProfanity(details) || Utils.checkProfanity(topic) || Utils.checkProfanity(tags))) {
			throw BotException.offensive();
		}
	}
	
	public void validateNewIssue(String title, String details, String tags, boolean isAdult) {
		if (!isAdult && (Utils.checkProfanity(details) || Utils.checkProfanity(title) || Utils.checkProfanity(tags))) {
			throw BotException.offensive();
		}
	}
	
	public BotInstance validateTemplate(String template, Domain domain) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			em.getTransaction().begin();
			Query query = em.createQuery("Select p from BotInstance p where p.alias = :alias and p.domain = :domain");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", template);
			query.setParameter("domain", domain);
			List<BotInstance> results = query.getResultList();
			if (results.isEmpty()) {
				// Check by name for backward compatibility.
				query = em.createQuery("Select p from BotInstance p where p.name = :name and p.domain = :domain");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("name", template);
				query.setParameter("domain", domain);
				results = query.getResultList();
				if (results.isEmpty()) {
					// Also check default domain.
					domain = validateDomain(Site.DOMAIN);
					query = em.createQuery("Select p from BotInstance p where p.alias = :alias and p.domain = :domain");
					query.setHint("eclipselink.read-only", "true");
					query.setParameter("alias", template);
					query.setParameter("domain", domain);
					results = query.getResultList();
					if (results.isEmpty()) {
						// Also check BOT libre.
						try {
							domain = validateDomain("BOT libre!");
							query = em.createQuery("Select p from BotInstance p where p.alias = :alias and p.domain = :domain");
							query.setHint("eclipselink.read-only", "true");
							query.setParameter("alias", template);
							query.setParameter("domain", domain);
							results = query.getResultList();
						} catch (Exception ignmore) {}
						if (results.isEmpty()) {
							throw new BotException("Template does not exist - " + template);
						}
					}
				}
			}
			BotInstance instance = results.get(0);
			return instance;
		} finally {
			em.close();
		}
	}
	
	public User validateUser(String id) {
		if (id != null) {
			id = id.trim();
		}
		EntityManager em =  getFactory().createEntityManager();
		try {
			User user = em.find(User.class, id);
			if (user == null) {
				if (id.equals("admin")) {
					user = new User("admin");
					user.setSuperUser(true);
					user.setType(UserType.Admin);
					user.setPassword("password");
					createUser(user);
				} else {
					throw new BotException("Invalid user - " + id);
				}
			}
			user.setEncryptedPassword(null);
			return user;
		} finally {
			em.close();
		}
	}

	public User applicationUser(String id) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Long applicationId = null;
			try {
				applicationId = Long.valueOf(id);
			} catch (Exception exception) {
				return null;
			}
			Query query = em.createQuery("Select u from User u where u.applicationId = :applicationId");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("applicationId", applicationId);
			List<User> results = query.getResultList();
			if (results.isEmpty()) {
				return null;
			}
			return results.get(0);
		} finally {
			em.close();
		}
	}
	
	public UserMessage validateUserMessage(String id) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Long messageId = null;
			try {
				messageId = Long.valueOf(id);
			} catch (Exception exception) {
				throw new BotException("Invalid message - " + id);
			}
			UserMessage message = em.find(UserMessage.class, messageId);
			if (message == null) {
				throw new BotException("Invalid message - " + id);
			}
			return message;
		} finally {
			em.close();
		}
	}
	
	public String validateApplicationId(String id, IPStats stat) {
		if ((id == null) || id.isEmpty() || id.equals("null")) {
			Stats.stats.badAPICalls++;
			throw new BotException("Missing application id.  You can obtain an application id from your user page.");
			//return "anonymous";
		}
		if (stat != null) {
			if (stat.badAPI > Stats.MAX_BAD_API) {
				Stats.stats.badAPICalls++;
				stat.badAPI++;
				Utils.sleep(5000);
				//if (stat.badAPI > (Stats.MAX_BAD_API * 2)) {
				//	Utils.sleep(10000);
				//}
				throw new BotException("IP has been banned for the day, max invalid app ID attempts");				
			}
		}
		long applicationId = -1;
		try {
			applicationId = Long.valueOf(id);
		} catch (Exception exception) {
			Stats.stats.badAPICalls++;
			if (stat != null) {
				stat.badAPI++;
			}
			throw new BotException("Invalid application id - '" + id + "'");
		}
		if (applicationId == temporaryApplicationId) {
			return Site.DOMAIN;
		}
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select u from User u where u.applicationId = :id");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("id", applicationId);
			List<User> results = query.getResultList();
			if (results.isEmpty()) {
				Stats.stats.badAPICalls++;
				if (stat != null) {
					stat.badAPI++;
				}
				throw new BotException("Invalid application id - '" + id + "'");
			}
			User user = results.get(0);
			String userid = user.getUserId();
			// Check if the user has an active domain.
			if (Site.COMMERCIAL && !user.isAdminUser() && !user.isPartnerUser()) {
				query = em.createQuery("Select d from Domain d where d.creator = :id and d.isActive = true");
				query.setParameter("id", new User(userid));
				List<Domain> domains = query.getResultList();
				boolean found = false;
				for (Domain domain : domains) {
					if (!domain.isExpired()) {
						found = true;
					}
				}
				if (!found) {
					// Search for domains the user is an admin of.
					query = em.createQuery("Select d from Domain d join d.admins a where a.userId = :id and d.isActive = true");
					query.setParameter("id", userid);
					domains = query.getResultList();
					for (Domain domain : domains) {
						if (!domain.isExpired()) {
							found = true;
						}
					}
					if (!found) {
						// Search for domains the user is an admin of.
						query = em.createQuery("Select d from Domain d join d.users a where a.userId = :id and d.isActive = true");
						query.setParameter("id", userid);
						domains = query.getResultList();
						for (Domain domain : domains) {
							if (!domain.isExpired()) {
								found = true;
							}
						}
						if (!found) {
							throw new BotException("User does not have an active workspace or your workspace has expired - " + userid);
						}
					}
				}
			}
			return userid;
		} finally {
			em.close();
		}
	}
	
	public Domain getDefaultDomain() {
		if (this.defaultDomain == null) {
			synchronized(this) {
				if (this.defaultDomain == null) {
					try {
						log(Level.INFO, "Site.DOMAIN: " + Site.DOMAIN);
						this.defaultDomain = validateDomain(Site.DOMAIN);
					} catch (Exception missing) {
						log(Level.INFO, "Creating default domain");
						Domain domain = new Domain(Site.DOMAIN);
						domain.alias = Site.DOMAIN;
						this.defaultDomain = createDomain(domain, "admin", "", "", null);
						
						User user = new User();
						user.setUserId("admin");
						user.setSuperUser(true);
						user.setType(UserType.Admin);
		
						Category category = new Category();
						category.setName("Misc");
						category.setDescription("Bots that have not been categorized");
						category.setType("Bot");
						category.setDomain(this.defaultDomain);
						AdminDatabase.instance().createCategory(category, user, "");
						
						category = new Category();
						category.setName("Misc");
						category.setDescription("Forums that have not been categorized");
						category.setType("Forum");
						category.setDomain(this.defaultDomain);
						AdminDatabase.instance().createCategory(category, user, "");
						
						category = new Category();
						category.setName("Misc");
						category.setDescription("Issue trackers that have not been categorized");
						category.setType("IssueTracker");
						category.setDomain(this.defaultDomain);
						AdminDatabase.instance().createCategory(category, user, "");
						
						category = new Category();
						category.setName("Misc");
						category.setDescription("Channels that have not been categorized");
						category.setType("Channel");
						category.setDomain(this.defaultDomain);
						AdminDatabase.instance().createCategory(category, user, "");
						
						category = new Category();
						category.setName("Misc");
						category.setDescription("Scripts that have not been categorized");
						category.setType("Script");
						category.setDomain(this.defaultDomain);
						AdminDatabase.instance().createCategory(category, user, "");
						
						category = new Category();
						category.setName("Misc");
						category.setDescription("Analytics that have not been categorized");
						category.setType("Analytic");
						category.setDomain(this.defaultDomain);
						AdminDatabase.instance().createCategory(category, user, "");
						
						category = new Category();
						category.setName("Misc");
						category.setDescription("Avatars that have not been categorized");
						category.setType("Avatar");
						category.setDomain(this.defaultDomain);
						AdminDatabase.instance().createCategory(category, user, "");
						
						category = new Category();
						category.setName("Misc");
						category.setDescription("Graphics that have not been categorized");
						category.setType("Graphic");
						category.setDomain(this.defaultDomain);
						AdminDatabase.instance().createCategory(category, user, "");
		
						log(Level.INFO, "Creating default template");
						try {
							// Create default template.
							LoginBean bean = new LoginBean();
							bean.setUser(user);
							bean.setLoggedIn(true);
							bean.setDomain(this.defaultDomain);
							BotBean botBean = bean.getBotBean();
							InstanceConfig config = new InstanceConfig();
							config.name = "template";
							config.categories = "";
							config.tags = "template";
							botBean.createInstance(config, true, true,"");
						
							// Initialize.
							botBean.connect(ClientType.WEB);
							if (bean.getError() != null) {
								throw bean.getError();
							}
							log(Level.INFO, "Initializing template database");
							bean.getBean(MemoryBean.class).processDeleteAll();
							if (bean.getError() != null) {
								throw bean.getError();
							}
							log(Level.INFO, "Initializing template scripts");
							bean.getBean(SelfBean.class).processRebootstrap();
							if (bean.getError() != null) {
								throw bean.getError();
							}
						} catch (Throwable exception) {
							exception.printStackTrace();
							log(exception);
						}
					}
				}
			}
		}
		return this.defaultDomain;
	}
	
	public void setDefaultDomain(Domain domain) {
		this.defaultDomain = domain;
	}
	
	public Domain validateDomain(String alias) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select p from Domain p where p.alias = :alias");
			query.setHint("eclipselink.read-only", "true");
			query.setParameter("alias", alias);
			List<Domain> results = query.getResultList();
			if (results.isEmpty()) {
				query = em.createQuery("Select p from Domain p where p.alias = :alias");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("alias", alias.replace(" ", "").toLowerCase());
				results = query.getResultList();
				if (results.isEmpty()) {
					query = em.createQuery("Select p from Domain p where p.name = :name");
					query.setHint("eclipselink.read-only", "true");
					query.setParameter("name", alias);
					results = query.getResultList();
					if (results.isEmpty()) {
						throw new BotException("Invalid workspace - " + alias);
					}
				}
			}
			return results.get(0);
		} finally {
			em.close();
		}
	}
	
	public User checkCredentialsUser(String credentialsUserID) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			User user = null;
			Query query = em.createQuery("Select u from User u where u.credentialsUserID = :credentialsUserID");
			query.setParameter("credentialsUserID", credentialsUserID);
			List<User> users = query.getResultList();
			if ((users == null) || users.isEmpty()) {
				return null;
			} else {
				user = users.get(0);
			}
			user.setEncryptedPassword(null);
			return user;
		} finally {
			em.close();
		}
	}
	
	public User validateCredentialsUser(String credentialsUserID, String credentialsToken) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			User user = null;
			Query query = em.createQuery("Select u from User u where u.credentialsUserID = :credentialsUserID");
			query.setParameter("credentialsUserID", credentialsUserID);
			List<User> users = query.getResultList();
			if ((users == null) || users.isEmpty()) {
				throw new BotException("User does not exist, please sign up");
			} else {
				user = users.get(0);
			}
			if (user.isBlocked()) {
				throw new BotException("User is blocked " + credentialsUserID);
			}
			// Tokens are different, must be used to verify with Facebook/Google.
			//if ((user.getCredentialsType() != CredentialsType.Facebook) && !user.getCredentialsToken().equals(credentialsToken)) {
			//	throw new BotException("Invalid credentials token");
			//}
			em.getTransaction().begin();
			user.setConnects(user.getConnects() + 1);
			user.setLastConnected(new Date());
			em.getTransaction().commit();
			user.setEncryptedPassword(null);
			return user;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	public User validateUser(String id, String password, long token, boolean reset, boolean wasSuper) {
		EntityManager em =  getFactory().createEntityManager();
		try {
			User user = em.find(User.class, id);
			if (user == null) {
				user = em.find(User.class, id.toLowerCase());
				if (user == null) {
					List<User> users = null;
					if (id.contains("@")) {
						Query query = em.createQuery("Select u from User u where lower(u.email) = :email");
						query.setParameter("email", id.toLowerCase());
						users = query.getResultList();
					}
					if ((users == null) || users.isEmpty()) {
						throw new BotException("Invalid user - " + id);
					} else {
						user = users.get(0);
					}
				}
			}
			if (user.isBlocked()) {
				throw new BotException("User is blocked - " + id);
			}
			if (!wasSuper) {
				if ((password != null) && (!password.equals("")) && user.getEncryptedPassword() != null) {
					if (!Arrays.equals(user.getEncryptedPassword(), encrypt(user.getUserId(), password))) {
						// May have been already encrypted as hex.
						if (!Arrays.equals(user.getEncryptedPassword(), encrypt(user.getUserId(), Utils.decrypt(id, password)))) {
							if (!Arrays.equals(user.getEncryptedPassword(), encrypt(user.getUserId(), Utils.decrypt(Utils.KEY, password)))) {
								throw new BotException("Invalid password");
							}
						}
					}
				} else if ((token != 0) && (user.getToken() != token)) {
					throw new BotException("Invalid or expired cookie, please sign in");
				} else if (token == 0) {
					throw new BotException("Missing password or token");
				}
				em.getTransaction().begin();
				user.setConnects(user.getConnects() + 1);
				user.setLastConnected(new Date());
				if (reset && user.getTokenReset() == null
							|| (System.currentTimeMillis() - user.getTokenReset().getTime() > (Utils.DAY * 60))) {
					user.resetToken();
				}
				em.getTransaction().commit();
			}
			user.setEncryptedPassword(null);
			return user;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}

	public void weeklySummaryEmail() {
		if (!Site.WEEKLYEMAIL) {
			return;
		}
		log(Level.INFO, "sending weekly user email");

		Set<User> users = new HashSet<User>();

		Calendar today = Calendar.getInstance();
		Calendar last = Calendar.getInstance();
		last.add(Calendar.DAY_OF_MONTH, -7);
		EntityManager em =  getFactory().createEntityManager();
		try {
			if (Site.WEEKLYEMAILBOTS) {
				Query query = em.createQuery("Select distinct u from User u, BotInstance p where p.creator = u and p.weeklyConnects > 0");
				query.setHint("eclipselink.read-only", "true");
				users.addAll(query.getResultList());
			}

			if (Site.WEEKLYEMAILCHANNELS) {
				Query query = em.createQuery("Select distinct u from User u, ChatChannel p where p.creator = u and p.weeklyConnects > 0");
				query.setHint("eclipselink.read-only", "true");
				users.addAll(query.getResultList());
			}

			if (Site.WEEKLYEMAILFORUMS) {
				Query query = em.createQuery("Select distinct u from User u, Forum p where p.creator = u and p.weeklyPosts > 0");
				query.setHint("eclipselink.read-only", "true");
				users.addAll(query.getResultList());
				
				query = em.createQuery("Select distinct u from Forum p join p.subscribers u where p.weeklyPosts > 0");
				query.setHint("eclipselink.read-only", "true");
				users.addAll(query.getResultList());
				
				query = em.createQuery("Select distinct u from User u, ForumPost p join p.replies r where p.creator = u and r.creationDate > :last");
				query.setHint("eclipselink.read-only", "true");
				query.setParameter("last", new java.sql.Date(last.getTimeInMillis()));
				users.addAll(query.getResultList());
			}
		} finally {
			em.close();
		}
		
		int day = today.get(Calendar.DAY_OF_MONTH);
		String date = today.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + day;
		if (day == 1 || day == 21 || day == 31) {
			date = date + "st";
		} else if (day == 2 || day == 22) {
			date = date + "nd";
		} else if (day == 3 || day == 23) {
			date = date + "rd";
		} else {
			date = date + "th";
		}

		for (User user: users) {
			if (user.hasEmail() && user.isVerified() && user.getEmailSummary()) {
				em =  getFactory().createEntityManager();
				try {
					StringWriter writer = new StringWriter();
					writer.write("Hi " + user.getUserId()
							+ "<p>\n\nHere is your weekly activity summary for your content on " + Site.NAME
							+ " for the week ending on " + date);

					if (Site.WEEKLYEMAILBOTS) {
						Query query = em.createQuery("Select p from BotInstance p where p.creator = :user and p.weeklyConnects > 0 order by p.weeklyConnects desc");
						query.setHint("eclipselink.read-only", "true");
						query.setParameter("user", user);
						List<BotInstance> instances = query.getResultList();
						if (instances.size() > 0) {
							writer.write("\n\n<h2>Bots</h2>");
						}
						for (BotInstance bot : instances) {
							writer.write("\n<p>");
							writer.write("<a href=\"" + Site.SECUREURLLINK + "/browse?id=" + bot.getId() + "\"><h3>" + bot.getName() + "</h3></a>");
							if (bot.getWeeklyConnects() == 1) {
								writer.write(bot.getWeeklyConnects() + " connect this week.");
							} else {
								writer.write(bot.getWeeklyConnects() + " connects this week.");
							}
							if (bot.getErrors().size() == 1) {
								writer.write("\n<p>This bot has 1 warning or error.");
							} else if (bot.getErrors().size() > 1) {
								writer.write("\n<p>This bot has " + bot.getErrors().size() + " warnings or errors.");
							}
						}
					}

					if (Site.WEEKLYEMAILCHANNELS) {
						Query query = em.createQuery("Select p from ChatChannel p where p.creator = :user and p.weeklyConnects > 0 order by p.weeklyConnects desc");
						query.setHint("eclipselink.read-only", "true");
						query.setParameter("user", user);
						List<ChatChannel> channels = query.getResultList();
						if (channels.size() > 0) {
							writer.write("\n\n<h2>Channels</h2>");
						}
						for (ChatChannel content : channels) {
							writer.write("\n<p>");
							writer.write("<a href=\"" + Site.SECUREURLLINK + "/livechat?id=" + content.getId() + "\"><h3>" + content.getName() + "</h3></a>");
							if (content.getWeeklyConnects() == 1) {
								writer.write(content.getWeeklyConnects() + " connect this week.");
							} else {
								writer.write(content.getWeeklyConnects() + " connects this week.");
							}
							if (content.getErrors().size() == 1) {
								writer.write("\n<p>This channel has 1 warning or error.");
							} else if (content.getErrors().size() > 1) {
								writer.write("\n<p>This channel has " + content.getErrors().size() + " warnings or errors.");
							}
						}
					}

					if (Site.WEEKLYEMAILFORUMS) {
						Query query = em.createQuery("Select p from Forum p where p.creator = :user and p.weeklyPosts > 0 order by p.weeklyPosts desc");
						query.setHint("eclipselink.read-only", "true");
						query.setParameter("user", user);
						List<Forum> forums = query.getResultList();
						if (forums.size() > 0) {
							writer.write("\n\n<h2>Forums</h2>");
						}
						for (Forum content : forums) {
							writer.write("\n<p>");
							writer.write("<a href=\"" + Site.SECUREURLLINK + "/forum?id=" + content.getId() + "\"><h3>" + content.getName() + "</h3></a>");
							if (content.getWeeklyPosts() == 1) {
								writer.write(content.getWeeklyPosts() + " post this week.");
							} else {
								writer.write(content.getWeeklyPosts() + " posts this week.");
							}
							if (content.getErrors().size() == 1) {
								writer.write("\n<p>This forum has 1 warning or error.");
							} else if (content.getErrors().size() > 1) {
								writer.write("\n<p>This forum has " + content.getErrors().size() + " warnings or errors.");
							}
						}
					}

					if (Site.WEEKLYEMAILFORUMS) {
						Query query = em.createQuery("Select distinct p from ForumPost p join p.replies r where p.creator = :user and r.creationDate > :last order by p.creationDate");
						query.setHint("eclipselink.read-only", "true");
						query.setParameter("user", user);
						query.setParameter("last", new java.sql.Date(last.getTimeInMillis()));
						List<ForumPost> posts = query.getResultList();
						if (posts.size() > 0) {
							writer.write("\n\n<h2>Forum Posts</h2>");
						}
						for (ForumPost content : posts) {
							writer.write("\n<p>");
							writer.write("<a href=\"" + Site.SECUREURLLINK + "/forum-post?id=" + content.getId() + "\"><h3>" + content.getTopic() + "</h3></a>");
							int count  = 0;
							for (ForumPost reply : content.getReplies()) {
								if (reply.getCreationDate().getTime() > last.getTimeInMillis()) {
									count++;
								}
							}
							if (count == 1) {
								writer.write(count + " reply this week.");
							} else {
								writer.write(count + " replies this week.");
							}
						}
					}

					if (Site.WEEKLYEMAILFORUMS) {
						Query query = em.createQuery("Select distinct p from Forum p join p.subscribers u where u.userId = :userid and p.weeklyPosts > 0 order by p.weeklyPosts desc");
						query.setHint("eclipselink.read-only", "true");
						query.setParameter("userid", user.getUserId());
						List<Forum> forums = query.getResultList();
						if (forums.size() > 0) {
							writer.write("\n\n<h2>Forums Subscriptions</h2>");
						}
						for (Forum content : forums) {
							writer.write("\n<p>");
							writer.write("<a href=\"" + Site.SECUREURLLINK + "/forum?id=" + content.getId() + "\"><h3>" + content.getName() + "</h3></a>");
							if (content.getWeeklyPosts() == 1) {
								writer.write(content.getWeeklyPosts() + " post this week.");
							} else {
								writer.write(content.getWeeklyPosts() + " posts this week.");
							}
						
							query = em.createQuery("Select distinct p from ForumPost p where p.forum = :fourm and p.creationDate > :last order by p.creationDate");
							query.setHint("eclipselink.read-only", "true");
							query.setParameter("fourm", content);
							query.setParameter("last", new java.sql.Date(last.getTimeInMillis()));
							List<ForumPost> posts = query.getResultList();
							boolean header = false;
							Map<ForumPost, Integer> replies = new HashMap<ForumPost, Integer>();
							for (ForumPost post : posts) {
								if (post.getParent() == null) {
									if (!header) {
										writer.write("\n\n<h3>New Posts</h3>");
										header = true;
									}
									writer.write("\n<p>");
									writer.write("<a href=\"" + Site.SECUREURLLINK + "/forum-post?id=" + post.getId() + "\">" + post.getTopic() + "</a>");
								} else {
									Integer count = replies.get(post.getParent());
									if (count == null) {
										count = 0;
									}
									count = count + 1;
									replies.put(post.getParent(), count);
								}
							}
							if (!replies.isEmpty()) {
								writer.write("\n\n<h3>New Replies</h3>");
								header = true;
							}
							for (Entry<ForumPost, Integer> entry : replies.entrySet()) {
								ForumPost post = entry.getKey();
								writer.write("\n<p>");
								if (entry.getValue() == 1) {
									writer.write(entry.getValue() + " new reply to: ");
								} else {
									writer.write(entry.getValue() + " new replies to: ");
								}
								writer.write("<a href=\"" + Site.SECUREURLLINK + "/forum-post?id=" + post.getId() + "\">" + post.getTopic() + "</a>");
							}
						}
					}
					
					writer.write("\n<p><br/><hr>"
							+ "\n<p>This is an automated email from " + Site.NAME + " - <a href=\"" + Site.SECUREURLLINK + "\">" + Site.SECUREURLLINK + "</a>."
							+ "\n<p>You can update your email preferences from your <a href=\"" + Site.SECUREURLLINK + "/login?sign-in=true\">profile page</a> (click edit).");
					writer.write("\n<p><a href=\"" + Site.SECUREURLLINK + "/login?unsubscribe=summary&user="
							+ user.getUserId() + "&token=" + user.getVerifyToken() + "\">Unsubscribe from weekly summary</a>.");
					writer.write("\n<p><a href=\"" + Site.SECUREURLLINK + "/login?unsubscribe=all&user="
						+ user.getUserId() + "&token=" + user.getVerifyToken() + "\">Unsubscribe from all notices</a>.");
					EmailService.instance().sendEmail(user.getEmail(), "This week on " + Site.NAME, null, writer.toString());
				} finally {
					em.close();
				}
					
				Utils.sleep(100);
			}
		}
	}

	public void emailExpiredDomains() {
		if (!Site.COMMERCIAL) {
			return;
		}
		log(Level.INFO, "sending expired domain emails");

		List<Domain> domains = null;

		EntityManager em =  getFactory().createEntityManager();
		try {
			Query query = em.createQuery("Select distinct d from Domain d where d.isActive = true");
			query.setHint("eclipselink.read-only", "true");
			domains = query.getResultList();
		} finally {
			em.close();
		}

		for (Domain domain : domains) {
			long time = domain.getPaymentExpiryDate().getTime() - new Date().getTime();
			if (time > 0 && (time < (Utils.DAY * 15)) && (time > (Utils.DAY * 14)) && domain.getCreator() != null) {
				log(Level.INFO, "about to expire domain", domain, domain.getCreator().getUserId());
				if (domain.getCreator().hasEmail()) {
					log(Level.INFO, "sending about to expire domain email", domain, domain.getCreator().getUserId(), domain.getCreator().getEmail());
					try {
						StringWriter writer = new StringWriter();
						writer.write("Hello " + domain.getCreator().getUserId()
								+ ",<p>\n\nYour workspace " + domain.getName() + " on " + Site.NAME
								+ " expires in 15 days.");
						writer.write("\n<p>");
						writer.write("Please go to your workspace page to make a payment - <a href=\"" + Site.SECUREURLLINK + "/domain?id=" + domain.getId() + "\">" + domain.getName() + "</a>");
						writer.write("\n<br/>You can also email " + Site.EMAILSALES + " to setup automatic payments.");
						
						writer.write("\n<p><br/><hr>"
								+ "\n<p>This is an automated email from " + Site.NAME + " - <a href=\"" + Site.SECUREURLLINK + "\">" + Site.SECUREURLLINK + "</a>."
								+ "\n<p>You can update your email preferences from your <a href=\"" + Site.SECUREURLLINK + "/login?sign-in=true\">profile page</a> (click edit).");
		
						EmailService.instance().sendEmail(domain.getCreator().getEmail(), "Your workspace expires soon on " + Site.NAME, null, writer.toString());
					} catch (Exception exception) {
						log(exception);
					}
				}
				Utils.sleep(100);
				
				StringWriter writer = new StringWriter();
				writer.write("Workspace " + domain.getName() + " on " + Site.NAME
						+ " expires today.");
				writer.write("\n<p>");
				writer.write("<a href=\"" + Site.SECUREURLLINK + "/domain?id=" + domain.getId() + "\">" + domain.getName()
						+ "</a> - " + domain.getCreator() + " - " + String.valueOf(domain.getCreator().getEmail()));
				
				EmailService.instance().sendEmail(Site.EMAILSALES, "Workspace has expired on " + Site.NAME, null, writer.toString());
				Utils.sleep(100);
			} else if (time > 0 && time < Utils.DAY && domain.getCreator() != null) {
				log(Level.INFO, "expired domain", domain, domain.getCreator().getUserId());
				if (domain.getCreator().hasEmail()) {
					log(Level.INFO, "sending expired domain email", domain, domain.getCreator().getUserId(), domain.getCreator().getEmail());
					try {
						StringWriter writer = new StringWriter();
						writer.write("Hello " + domain.getCreator().getUserId()
								+ ",<p>\n\nYour workspace " + domain.getName() + " on " + Site.NAME
								+ " expires today.");
						writer.write("\n<p>");
						writer.write("Please go to your workspace page to make a payment - <a href=\"" + Site.SECUREURLLINK + "/domain?id=" + domain.getId() + "\">" + domain.getName() + "</a>");
						
						writer.write("\n<p><br/><hr>"
								+ "\n<p>This is an automated email from " + Site.NAME + " - <a href=\"" + Site.SECUREURLLINK + "\">" + Site.SECUREURLLINK + "</a>."
								+ "\n<p>You can update your email preferences from your <a href=\"" + Site.SECUREURLLINK + "/login?sign-in=true\">profile page</a> (click edit).");
		
						EmailService.instance().sendEmail(domain.getCreator().getEmail(), "Your workspace has expired on " + Site.NAME, null, writer.toString());
					} catch (Exception exception) {
						log(exception);
					}
				}
				Utils.sleep(100);
				
				StringWriter writer = new StringWriter();
				writer.write("Domain " + domain.getName() + " on " + Site.NAME
						+ " expires today.");
				writer.write("\n<p>");
				writer.write("<a href=\"" + Site.SECUREURLLINK + "/domain?id=" + domain.getId() + "\">" + domain.getName()
						+ "</a> - " + domain.getCreator() + " - " + String.valueOf(domain.getCreator().getEmail()));
				
				EmailService.instance().sendEmail(Site.EMAILSALES, "Workspace has expired on " + Site.NAME, null, writer.toString());
				Utils.sleep(100);
			} else if (time < 0 ) {
				em =  getFactory().createEntityManager();
				try {
					em.getTransaction().begin();
					Domain instance = (Domain)em.find(Domain.class, domain.getId());
					if (instance != null) {
						instance.setActive(false);
					}
					em.getTransaction().commit();
				} finally {
					if (em.getTransaction().isActive()) {
						em.getTransaction().rollback();
					}
					em.close();
				}
			}
		}
	}

}

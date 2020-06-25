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
package org.botlibre.web;

import org.botlibre.web.bean.BrowseBean;
import org.botlibre.web.admin.ContentRating;
import org.botlibre.web.admin.Domain.AccountType;
import org.botlibre.web.bean.BotBean;

/**
 * This class provides server specific settings.
 * You can configure settings here, or bootstrap through the botplatform.xml file.
 */
public class Site {
	public static String VERSION = "8.0.4-2020-03-26";
	
	/*public static String URL_SUFFIX = "";
	public static String URL_PREFIX = "";
	public static String URL = "bots.domain.com";
	public static String URLLINK = "http://bots.domain.com";
	public static String SECUREURLLINK = "https://bots.domain.com";
	public static String SANDBOX = "sandbox";
	public static String SANDBOXURLLINK = "http://sandbox.domain.com";
	public static String SERVER_NAME = "bots.domain.com";
	public static String SERVER_NAME2 = "bots.domain.com";
	public static String REDIRECT = "";
	public static boolean HTTPS = true;*/
	
	public static String URL_PREFIX = "";
	public static String URL_SUFFIX = "";
	public static String SERVER_NAME = "localhost";
	public static String SERVER_NAME2 = "localhost";
	public static String URL = "localhost";
	public static String URLLINK = "http://localhost";
	public static String SECUREURLLINK = "http://localhost";
	public static String SANDBOX = "sandbox";
	public static String SANDBOXURLLINK = "http://sandbox.localhost";
	public static String REDIRECT = "";
	public static boolean HTTPS = false;
	
	/*public static String URL_PREFIX = "";
	public static String URL_SUFFIX = "";
	public static String SERVER_NAME = "192.168.0.16";
	public static String SERVER_NAME2 = "192.168.0.16";
	public static String URL = "192.168.0.16";
	public static String URLLINK = "http://192.168.0.16";
	public static String SECUREURLLINK = "http://192.168.0.16";
	public static String SANDBOX = "sandbox";
	public static String SANDBOXURLLINK = "http://192.168.0.16";
	public static String REDIRECT = "";
	public static boolean HTTPS = false;*/
	
	public static boolean HTTPS_WILDCARD = false;

	public static String PYTHONSERVER = "";

	// Allow the server to be bootstrapped if it fails to connect to the database.
	public static boolean BOOTSTRAP = true;
	// Do not allow access to the website from any other domains.
	public static boolean LOCK = false;
	// Do not allow any changes (set when migrating).
	public static boolean READONLY = false;
	// Disables profanity filter.
	public static boolean ADULT = false;
	// Allows disable of profanity filter for private content.
	public static boolean ALLOW_ADULT = true;
	// Default content rating.
	public static ContentRating CONTENT_RATING = ContentRating.Teen;
	public static String NAME = "Bot Libre Platform";
	// Default workspace name.
	public static String DOMAIN = "Bot Libre Platform";
	// Internal site id.
	public static String ID = "botlibreplatform";
	// JavaScript embed prefix.
	public static String PREFIX = "botplatform";
	// JPA persistence unit.
	public static String PERSISTENCE_UNIT = "botlibreplatform";
	// JPA connection protocol to porsgres running on container
	public static String PERSISTENCE_PROTOCOL = "jdbc:postgresql://app-db:5432/";
	// Twitter hash tag.
	public static String HASHTAG = "botlibre";
	// Default content type.
	public static String TYPE = "Bot";
	// Default language for translation.
	public static String LANG = "en";
	// Enable bot Twitter support.
	public static boolean TWITTER = true;
	// Enable bot Facebook support.
	public static boolean FACEBOOK = true;
	// Enable bot Telegram support.
	public static boolean TELEGRAM = true;
	// Enable bot Slack support.
	public static boolean SLACK = true;
	// Enable bot Skype support.
	public static boolean SKYPE = true;
	// Enable bot WeChat support.
	public static boolean WECHAT = true;
	// Enable bot Kik support.
	public static boolean KIK = true;
	// Enable bot Email support.
	public static boolean EMAIL = true;
	// Enable bot timers.
	public static boolean TIMERS = true;
	// Enable bot forgetfulness.
	public static boolean FORGET = true;
	// Enable server admin services.
	public static boolean ADMIN = true;
	// Enable user emails.
	public static boolean WEEKLYEMAIL = true;
	public static boolean WEEKLYEMAILBOTS = true;
	public static boolean WEEKLYEMAILCHANNELS = true;
	public static boolean WEEKLYEMAILFORUMS = true;
	// Require user's to give name/email on sign up.
	public static boolean VERIFYUSERS = true;
	// Configure Enterprise Bot Platform.
	public static boolean DEDICATED = true;
	// Configure Cloud Bot Platform.
	public static boolean CLOUD = false;
	// Configure commercial vs free open.
	public static boolean COMMERCIAL = true;
	// Disable user sign up.
	public static boolean ALLOW_SIGNUP = true;
	// Require verified email to create public content.
	public static boolean VERIFY_EMAIL = false;
	// Disable anonymous chat, require sign in.
	public static boolean ANONYMOUS_CHAT = true;
	// Require terms to be accepted before chat.
	public static boolean REQUIRE_TERMS = false;
	// Require age check before chat.
	public static boolean AGE_RESTRICT = false;
	// Allow user ads.
	public static boolean ADCODE = false;
	// Require backlink in embed code.
	public static boolean BACKLINK = false;
	// Bot default memory limit.
	public static int MEMORYLIMIT = 250000;
	// Bot max script process time.
	public static int MAX_PROCCESS_TIME = 200000;
	// Bot/content creation limit per user.
	public static int CONTENT_LIMIT = 1000;
	
	public static String EMAILHOST = "domain.com";
	public static String EMAILBOT = "bot@domain.com";
	public static String EMAILSALES = "sales@domain.com";
	public static String SIGNATURE = "";
	// Server email settings.
	public static String EMAILPAYPAL = "billing@domain.com";
	public static String EMAILSMTPHost = "smtp.domain.com";
	public static int EMAILSMTPPORT = 8025;
	public static String EMAILUSER = "bot@domain.com";
	public static String EMAILPASSWORD = "password";
	public static boolean EMAILSSL = false;
	
	public static int MAX_CREATES_PER_IP = 50;
	public static int MAX_USER_MESSAGES = 500;
	public static int MAX_UPLOAD_SIZE = 5000000; // 5meg
	public static int MAX_LIVECHAT_MESSAGES = 1000000;
	public static int MAX_ATTACHMENTS = 5000;
	public static int MAX_TRANSLATIONS = 1000000;
	public static int URL_TIMEOUT = 20000; // 20 seconds
	
	public static int MAX_API = 2500;
	public static int MAX_BRONZE = 5000;
	public static int MAX_GOLD = 10000;
	public static int MAX_PLATINUM = 50000;
	public static int MAX_BOT_CACHE_SIZE = 120;
	public static int MAX_BOT_POOL_SIZE = 30;
	public static int MAXTWEETIMPORT = 500;
	
	//Disable Telegram supergroups as they can DOS server with too many messages.
	public static boolean DISABLE_SUPERGROUP = false;
	
	public static String TWITTER_OAUTHKEY = "";
	public static String TWITTER_OAUTHSECRET = "";
	
	public static String FACEBOOK_APPID = "";
	public static String FACEBOOK_APPSECRET = "";
	
	public static String KEY = "12345";
	public static String KEY2 = "12345";
	public static String UPGRADE_SECRET = "12345";
	
	public static String GOOGLEKEY = "";
	public static String GOOGLECLIENTID = "";
	public static String GOOGLECLIENTSECRET = "";
	
	public static String MICROSOFT_SPEECH_KEY = "";
	public static String MICROSOFT_SPEECH_ENDPOINT = "https://eastus.api.cognitive.microsoft.com/sts/v1.0/issueToken";
	
	public static String RESPONSIVEVOICE_KEY = "";
	
	public static String YANDEX_KEY = "";
	
	public static String DATABASEPASSWORD = "password";
	public static String OBFUSCATE_DATABASEPASSWORD = "";

	@SuppressWarnings("rawtypes")
	public static BrowseBean defaultBean() {
		return new BotBean();
	}
	
	public static String getPaymentType(AccountType type) {
		if (type == null) {
			return "";
		}
		return type.toString();
	}
}

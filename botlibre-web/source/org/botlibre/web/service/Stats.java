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

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.servlet.http.HttpServletRequest;

import org.botlibre.BotException;

@Entity
public class Stats {
	public static int MAX_ROWS = 500000;
	public static int MAX_CREATES = 1000;
	public static int MAX_ANONYMOUS_API = 500;
	public static int MAX_BAD_API = 20;
	public static Stats stats = new Stats();
	
	public static long lastChat;
	
	@Id
	@GeneratedValue
	public long id;
	public Timestamp date;
	
	public int apiCalls;
	public int apiOverLimit;
	public int badAPICalls;
	public int anonymousAPICalls;
	public int anonymousAPIOverLimit;
	public int sessions;
	public int pages;
	public int errors;
	
	public int bots;
	public int activeBots;
	
	public int twitterBots;
	public int facebookBots;
	public int telegramBots;
	public int slackBots;
	public int skypeBots;
	public int wechatBots;
	public int kikBots;
	public int emailBots;
	public int alexaBots;
	public int googleAssistantBots;
	
	public int botTweets;
	public int botRetweets;
	public int botTweetsProcessed;
	public int botDirectMessagesProcessed;
	public int botFacebookPosts;
	public int botFacebookProcessed;
	public int botFacebookAPI;
	public int botFacebookMessagesProcessed;
	public int botFacebookLikes;
	public int botTelegramPosts;
	public int botTelegramAPI;
	public int botTelegramMessagesProcessed;
	public int botSlackPosts;
	public int botSlackMessagesProcessed;
	public int botSlackAPI;
	public int botSkypeMessagesProcessed;
	public int botSkypeAPI;
	public int botWeChatMessagesProcessed;
	public int botWeChatAPI;
	public int botKikMessagesProcessed;
	public int botKikAPI;
	public int botAlexaMessagesProcessed;
	public int botAlexaAPI;
	public int botGoogleAssistantMessagesProcessed;
	public int botGoogleAssistantAPI;
	public int botSMSSent;
	public int botSMSProcessed;
	public int botSMSAPI;
	public int botTwilioVoiceAPI;
	public int botTwilioVoiceProcessed;
	public int botTwilioVoiceCalls;
	public int botEmails;
	public int botEmailsProcessed;
	public int botChats;
	public int botConnects;
	public int botCreates;
	public int botMessages;
	public int botConversations;

	public long botChatTimeouts;
	public long botChatTotalResponseTime;
	
	public int userConnects;
	public int userCreates;
	public int userMessages;
	
	public int forumPosts;
	public int forumPostViews;
	public int forumCreates;
	
	public int issues;
	public int issueViews;
	public int issueTrackerCreates;
	
	public int chatMessages;
	public int chatRooms;
	public int chatConnects;
	public int chatCreates;
	
	public int twitterRuns;
	public int platinumTwitterRuns;
	public int bronzeTwitterRuns;
	public int goldTwitterRuns;
	public Timestamp lastTwitterRun;
	
	public int facebookRuns;
	public int platinumFacebookRuns;
	public int bronzeFacebookRuns;
	public int goldFacebookRuns;
	public Timestamp lastFacebookRun;

	public int telegramRuns;
	public int platinumTelegramRuns;
	public int bronzeTelegramRuns;
	public int goldTelegramRuns;
	public Timestamp lastTelegramRun;

	public int timerRuns;
	public int platinumTimerRuns;
	public int bronzeTimerRuns;
	public int goldTimerRuns;
	public Timestamp lastTimerRun;
	
	public int slackRuns;
	public int platinumSlackRuns;
	public int bronzeSlackRuns;
	public int goldSlackRuns;
	public Timestamp lastSlackRun;
	
	public int skypeRuns;
	public int platinumSkypeRuns;
	public int bronzeSkypeRuns;
	public int goldSkypeRuns;
	public Timestamp lastSkypeRun;
	
	public int wechatRuns;
	public int platinumWeChatRuns;
	public int bronzeWeChatRuns;
	public int goldWeChatRuns;
	public Timestamp lastWeChatRun;
	
	public int kikRuns;
	public int platinumKikRuns;
	public int bronzeKikRuns;
	public int goldKikRuns;
	public Timestamp lastKikRun;
	
	public int emailRuns;
	public int platinumEmailRuns;
	public int bronzeEmailRuns;
	public int goldEmailRuns;
	public Timestamp lastEmailRun;
	public int emails;
	
	public int memoryFrees;
	public long totalMemory;
	public long freeMemory;
	public int webDownloads;
	public int desktopDownloads;
	
	public int analyticImageUpload;
	public int analyticTestImageUpload;
	public int analyticTest;
	public int analyticTraining;
	public int analyticTestMediaProcessing;
	public int analyticTrainingBusy;
	public int analyticTestMediaBusy;
	public int analyticBinaryUpload;

	
	public static void reset() {
		stats = new Stats();
	}

	public void checkMaxCreates() {
		if (botCreates >= MAX_CREATES) {
			throw new BotException("Maximum daily instance creation exceeded.\nTo prevent spam attacks, bot creation is disabled for today.");					
		}
	}
	
	public static void checkMaxAPI() {
		if (stats.anonymousAPICalls > MAX_ANONYMOUS_API) {
			Stats.stats.badAPICalls++;
			stats.anonymousAPIOverLimit++;
			throw new BotException("Daily maximum anonymous API calls reached, please use your application id (available from your user page)");
		}
	}
	
	public static void page(HttpServletRequest request) {
		if (request == null) {
			return;
		}
		String ip = request.getRemoteAddr();
		if (ip == null || ip.isEmpty()) {
			return;
		}
		stats.pages++;
		IPStats.page(request);
		PageStats.page(request);
		ReferrerStats.page(request);
	}
	
	public static void session(HttpServletRequest request) {
		if (request == null) {
			return;
		}
		String ip = request.getRemoteAddr();
		if (ip == null || ip.isEmpty()) {
			return;
		}
		stats.sessions++;
		IPStats.session(request);
	}
	
	public Stats() {
		this.date = new Timestamp(System.currentTimeMillis());
	}
	
}

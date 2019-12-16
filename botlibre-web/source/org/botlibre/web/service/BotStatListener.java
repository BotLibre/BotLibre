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

public class BotStatListener implements org.botlibre.Stats {
	public Long id;
	public String name;
	
	public BotStatListener(Long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public void stat(String stat) {
		BotStats stats = BotStats.getStats(this.id, this.name);
		if (stat.equals("sms")) {
			Stats.stats.botSMSSent++;
			stats.smsSent++;
		} else if (stat.equals("call")) {
			Stats.stats.botTwilioVoiceCalls++;
			stats.twilioVoiceCalls++;
		} else if (stat.equals("email")) {
			Stats.stats.botEmails++;
			stats.emails++;
		} else if (stat.equals("twitter.tweet")) {
			Stats.stats.botTweets++;
			stats.tweets++;
		} else if (stat.equals("facebook.post")) {
			Stats.stats.botFacebookPosts++;
			stats.facebookPosts++;
		} else if (stat.equals("telegram.post")) {
			Stats.stats.botTelegramPosts++;
			stats.telegramPosts++;
		}
	}
}

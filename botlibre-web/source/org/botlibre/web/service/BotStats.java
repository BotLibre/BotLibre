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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class BotStats implements Comparable<BotStats> {
	public static int MAX_SIZE = 1000;
	public static Map<Long, BotStats> stats = new ConcurrentHashMap<Long, BotStats>();

	@Id
	@GeneratedValue
	public long id;
	public Timestamp date;
	public Long botId;
	public String botName;

	public int size;
	public int connects;
	public int api;
	public int apiOverLimit;
	public int errors;
	public int chats;
	public int livechats;
	public long chatTotalResponseTime;
	public int imports;
	public int tweets;
	public int retweets;
	public int tweetsProcessed;
	public int directMessagesProcessed;
	public int emails;
	public int emailsProcessed;
	public int facebookPosts;
	public int facebookMessagesProcessed;
	public int facebookProcessed;
	public int facebookLikes;
	public int telegramPosts;
	public int telegramMessagesProcessed;
	public int slackPosts;
	public int slackMessagesProcessed;
	public int skypeMessagesProcessed;
	public int wechatMessagesProcessed;
	public int kikMessagesProcessed;
	public int alexaMessagesProcessed;
	public int googleAssistantMessagesProcessed;
	public int smsSent;
	public int smsProcessed;
	public int twilioVoiceCalls;
	public int twilioVoiceProcessed;
	public int whatsappSent;
	public int whatsappProcessed;
	
	public int conversations;
	public int messages;
	public int engaged;
	public int defaultResponses;
	public int confidence;
	public float sentiment;
	
	public static void reset() {
		stats = new ConcurrentHashMap<Long, BotStats>();
	}
	
	public static BotStats getStats(Long botId, String name) {
		if (stats.size() > MAX_SIZE) {
			// Clear bottom half from map.
			List<BotStats> copy = new ArrayList<BotStats>(stats.values());
			stats.clear();
			Collections.sort(copy);
			for (int index = copy.size() - 1; index > (copy.size() / 2); index--) {
				BotStats stat = (BotStats)copy.get(index);
				stats.put(stat.botId, stat);
			}
		}
		BotStats stat = stats.get(botId);
		if (stat == null) {
			stat = new BotStats();
			stat.botId = botId;
			stat.botName = name;
			stats.put(botId, stat);
		}
		return stat;
	}
	
	public static List<BotStats> sortedStats() {
		List<BotStats> sorted = new ArrayList<BotStats>(stats.values());
		Collections.sort(sorted);
		return sorted;
	}
	
	public BotStats() {
		this.date = new Timestamp(System.currentTimeMillis());
	}
	
	public int compareTo(BotStats stat) {
		return Integer.valueOf(connects).compareTo(stat.connects);
	}
}

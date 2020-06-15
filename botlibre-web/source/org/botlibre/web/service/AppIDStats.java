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

import org.botlibre.BotException;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.User;
import org.botlibre.web.admin.User.UserType;

@Entity
public class AppIDStats implements Comparable<AppIDStats> {
	public static int MAX_SIZE = 1000;
	public static Map<String, AppIDStats> stats = new ConcurrentHashMap<String, AppIDStats>();

	@Id
	@GeneratedValue
	public long id;
	public Timestamp date;
	public String appID;
	public String userId;
	public long userCreates;
	public long botCreates;
	
	public int apiCalls;
	public int overLimit;
	
	public static void reset() {
		stats = new ConcurrentHashMap<String, AppIDStats>();
	}

	public static void checkMaxAPI(AppIDStats stat, String appUser, Long botId, String botName) {
		if (appUser.equals(Site.DOMAIN)) {
			return;
		}
		// Also check bot API.
		BotStats botState = null;
		if (botId != null) {
			botState = BotStats.getStats(botId, botName);
		}
		if (stat.apiCalls > Site.MAX_API || (botState != null && botState.api > Site.MAX_API)) {
			User user = null;
			if (stat.appID != null && !stat.appID.isEmpty()) {
				user = AdminDatabase.instance().applicationUser(stat.appID);
			}
			if (user != null && !user.isExpired() && (user.getType() == UserType.Diamond || user.getType() == UserType.Partner || user.getType() == UserType.Admin)) {
				return;
			}
			int multiplier = 1;
			/*if (Utils.checkLowMemory()) {
				multiplier = 2;
			}*/
			int limit = Site.MAX_API * multiplier;
			if (user != null && !user.isExpired()) {
				if (user.getType() == UserType.Platinum) {
					limit = Site.MAX_PLATINUM * multiplier;
				} else if (user.getType() == UserType.Gold) {
					limit = Site.MAX_GOLD * multiplier;
				} else if (user.getType() == UserType.Bronze) {
					limit = Site.MAX_BRONZE * multiplier;
				}
			}
			if (user == null) {
				Stats.stats.apiOverLimit++;
				stat.overLimit++;
				throw new BotException("Daily maximum anonymous API calls reached, please use your application ID");
			}
			if (stat.apiCalls > limit) {
				Stats.stats.apiOverLimit++;
				stat.overLimit++;
				throw new BotException("Daily maximum API calls reached, please upgrade your account");
			}
			// Also check bot API.
			if (botState != null) {
				if (botState.api > limit) {
					Stats.stats.apiOverLimit++;
					botState.apiOverLimit++;
					stat.overLimit++;
					throw new BotException("Daily maximum per bot API calls reached, please upgrade your account");
				}
			}
		}
		if (botState != null) {
			botState.api++;
		}
	}
	
	public static AppIDStats getStats(String appID, String userId) {
		if (appID == null) {
			appID = "";
		}
		if (stats.size() > MAX_SIZE) {
			// Clear bottom half from map.
			List<AppIDStats> copy = new ArrayList<AppIDStats>(stats.values());
			stats.clear();
			Collections.sort(copy);
			for (int index = copy.size() - 1; index > (copy.size() / 2); index--) {
				AppIDStats stat = (AppIDStats)copy.get(index);
				stats.put(stat.appID, stat);
			}
		}
		AppIDStats stat = stats.get(appID);
		if (stat == null) {
			stat = new AppIDStats();
			stat.appID = appID;
			stat.userId = userId;
			stats.put(appID, stat);
		}
		return stat;
	}
	
	public static List<AppIDStats> sortedStats() {
		List<AppIDStats> sorted = new ArrayList<AppIDStats>(stats.values());
		Collections.sort(sorted);
		return sorted;
	}
	
	public AppIDStats() {
		this.date = new Timestamp(System.currentTimeMillis());
	}
	
	public int compareTo(AppIDStats stat) {
		return Integer.valueOf(apiCalls).compareTo(stat.apiCalls);
	}
}

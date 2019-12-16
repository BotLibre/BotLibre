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
public class LiveChatStats implements Comparable<LiveChatStats> {
	public static int MAX_SIZE = 1000;
	public static Map<Long, LiveChatStats> stats = new ConcurrentHashMap<Long, LiveChatStats>();

	@Id
	@GeneratedValue
	public long id;
	public Timestamp date;
	public Long channelId;
	public String channelName;
	
	public int connects;
	public int messages;
	public int joins;
	
	public static void reset() {
		stats = new ConcurrentHashMap<Long, LiveChatStats>();
	}
	
	public static LiveChatStats getStats(Long channelId, String name) {
		if (stats.size() > MAX_SIZE) {
			stats.clear();
		}
		LiveChatStats stat = stats.get(channelId);
		if (stat == null) {
			stat = new LiveChatStats();
			stat.channelId = channelId;
			stat.channelName = name;
			stats.put(channelId, stat);
		}
		return stat;
	}
	
	public static List<LiveChatStats> sortedStats() {
		List<LiveChatStats> sorted = new ArrayList<LiveChatStats>(stats.values());
		Collections.sort(sorted);
		return sorted;
	}
	
	public LiveChatStats() {
		this.date = new Timestamp(System.currentTimeMillis());
	}
	
	public int compareTo(LiveChatStats stat) {
		return Integer.valueOf(connects).compareTo(stat.connects);
	}
}

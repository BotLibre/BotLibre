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
import javax.servlet.http.HttpServletRequest;

import org.botlibre.util.Utils;
import org.botlibre.web.Site;

@Entity
public class ReferrerStats implements Comparable<ReferrerStats> {
	public static int MAX_SIZE = 1000;
	public static Map<String, ReferrerStats> stats = new ConcurrentHashMap<String, ReferrerStats>();

	@Id
	@GeneratedValue
	public long id;
	public Timestamp date;
	public String page;
	public int refers;
	
	public static void reset() {
		stats = new ConcurrentHashMap<String, ReferrerStats>();
	}
	
	public static ReferrerStats getStats(String url) {
		if (url != null && url.length() > 150) {
			url = url.substring(0, 150);
		}
		if (stats.size() > MAX_SIZE) {
			stats.clear();
		}
		url = Utils.sanitize(url);
		ReferrerStats stat = stats.get(url);
		if (stat == null) {
			stat = new ReferrerStats();
			stat.page = url;
			stats.put(url, stat);
		}
		return stat;
	}
	
	public static List<ReferrerStats> sortedStats() {
		List<ReferrerStats> sorted = new ArrayList<ReferrerStats>(stats.values());
		Collections.sort(sorted);
		return sorted;
	}
	
	public static void page(HttpServletRequest request) {		
		if (request == null) {
			return;
		}
		String url = request.getHeader("referer");
		if (url == null || url.isEmpty() || url.contains(Site.SERVER_NAME) || url.contains(Site.SERVER_NAME2)) {
			return;
		}
		ReferrerStats stat = getStats(url);
		stat.refers++;		
	}
	
	public ReferrerStats() {
		this.date = new Timestamp(System.currentTimeMillis());
	}
	
	public int compareTo(ReferrerStats stat) {
		return Integer.valueOf(refers).compareTo(stat.refers);
	}
}

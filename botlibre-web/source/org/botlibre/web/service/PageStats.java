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
import java.util.logging.Level;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.servlet.http.HttpServletRequest;

import org.botlibre.util.Utils;
import org.botlibre.web.admin.AdminDatabase;

@Entity
public class PageStats implements Comparable<PageStats> {
	public static int MAX_SIZE = 1000;
	public static Map<String, PageStats> stats = new ConcurrentHashMap<String, PageStats>();

	@Id
	@GeneratedValue
	public long id;
	public Timestamp date;
	public String page;
	public int hits;
	
	public static void reset() {
		stats = new ConcurrentHashMap<String, PageStats>();
	}
	
	public static PageStats getStats(String url) {
		if (stats.size() > MAX_SIZE) {
			stats.clear();
		}
		url = Utils.sanitize(url);
		PageStats stat = stats.get(url);
		if (stat == null) {
			stat = new PageStats();
			stat.page = url;
			stats.put(url, stat);
		}
		return stat;
	}
	
	public static List<PageStats> sortedStats() {
		List<PageStats> sorted = new ArrayList<PageStats>(stats.values());
		Collections.sort(sorted);
		return sorted;
	}
	
	public static void page(HttpServletRequest request) {
		if (request == null) {
			return;
		}
		String url = request.getRequestURL().toString();
		if (url == null || url.isEmpty()) {
			return;
		}
		AdminDatabase.instance().log(Level.INFO, "Page", url + (request.getQueryString() == null ? "" : "?" + request.getQueryString()));
		PageStats stat = getStats(url);
		stat.hits++;
	}
	
	public PageStats() {
		this.date = new Timestamp(System.currentTimeMillis());
	}
	
	public int compareTo(PageStats stat) {
		return Integer.valueOf(hits).compareTo(stat.hits);
	}
}

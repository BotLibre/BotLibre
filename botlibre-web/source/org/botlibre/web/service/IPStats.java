/******************************************************************************
 *
 *  Copyright 2013-2020 Paphus Solutions Inc.
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

import org.botlibre.BotException;
import org.botlibre.util.Utils;
import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.servlet.BeanServlet;

@Entity
public class IPStats implements Comparable<IPStats> {
	public static int MAX_USER_MESSAGES = 20;
	public static int MAX_SIZE = 1000;
	public static Map<String, IPStats> stats = new ConcurrentHashMap<String, IPStats>();

	@Id
	@GeneratedValue
	public long id;
	public Timestamp date;
	public String ip;
	public String agent;
	
	public int pages;
	public int sessions;
	public int api;
	public int badAPI;
	public int botConnects;
	public int botChats;
	public int botCreates;
	public int userMessages;

	public void checkMaxCreates() {
		if (botCreates >= Site.MAX_CREATES_PER_IP) {
			throw new BotException("Maximum daily instance creation exceeded for this IP.\nTo prevent spam attacks, bot creation is disabled for this IP for today.");
		}
	}

	public void checkMaxUserMessages() {
		if (userMessages >= Site.MAX_USER_MESSAGES) {
			throw new BotException("Your IP has been temporary blocked from sending user messages as a spam prevention percaution.");
		}
	}
	
	public static void reset() {
		stats = new ConcurrentHashMap<String, IPStats>();
	}
	
	public static IPStats getStats(String ip) {
		if (stats.size() > MAX_SIZE) {
			// Clear bottom half from map.
			List<IPStats> copy = new ArrayList<IPStats>(stats.values());
			stats.clear();
			Collections.sort(copy);
			for (int index = copy.size() - 1; index > (copy.size() / 2); index--) {
				IPStats stat = (IPStats)copy.get(index);
				stats.put(stat.ip, stat);
			}
		}
		IPStats stat = stats.get(ip);
		if (stat == null) {
			stat = new IPStats();
			stat.ip = ip;
			stats.put(ip, stat);
		}
		return stat;
	}
	
	public static List<IPStats> sortedStats() {
		List<IPStats> sorted = new ArrayList<IPStats>(stats.values());
		Collections.sort(sorted);
		return sorted;
	}
	
	public static void botConnect(HttpServletRequest request) {
		if (request == null) {
			return;
		}
		String agent = AgentStats.extractAgent(request);
		String referer = request.getHeader("referer");
		if (referer == null) {
			referer = "unknown";
		}
		AdminDatabase.instance().getLog().log(Level.INFO, "user-agent: " + agent);
		AdminDatabase.instance().getLog().log(Level.INFO, "referer: " + referer);
		String ip = BeanServlet.extractIP(request);
		if (ip == null || ip.isEmpty()) {
			return;
		}
		IPStats stat = getStats(ip);
		stat.botConnects++;
	}
	
	public static void botChats(HttpServletRequest request) {
		if (request == null) {
			return;
		}
		String ip = BeanServlet.extractIP(request);
		if (ip == null || ip.isEmpty()) {
			return;
		}
		IPStats stat = getStats(ip);
		stat.botChats++;
	}
	
	public static IPStats api(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		AgentStats.api(request);
		String ip = BeanServlet.extractIP(request);
		if (ip == null || ip.isEmpty()) {
			return null;
		}
		IPStats stat = getStats(ip);
		stat.api++;
		return stat;
	}
	
	public static void page(HttpServletRequest request) {
		if (request == null) {
			return;
		}
		AgentStats.page(request);
		String ip = BeanServlet.extractIP(request);
		if (ip == null || ip.isEmpty()) {
			return;
		}
		IPStats stat = getStats(ip);
		stat.pages++;
	}
	
	public static void session(HttpServletRequest request) {
		if (request == null) {
			return;
		}
		AgentStats.session(request);
		String agent = AgentStats.extractAgent(request);
		String ip = BeanServlet.extractIP(request);
		if (ip == null || ip.isEmpty()) {
			return;
		}
		AdminDatabase.instance().log(Level.INFO, "connect", ip, agent);
		IPStats stat = getStats(ip);
		if (stat.agent == null) {
			stat.agent = Utils.sanitize(agent);
		}
		stat.sessions++;
	}
	
	public IPStats() {
		this.date = new Timestamp(System.currentTimeMillis());
	}
	
	public int compareTo(IPStats stat) {
		return Integer.valueOf(pages).compareTo(stat.pages);
	}
}

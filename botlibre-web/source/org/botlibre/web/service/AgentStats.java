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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.servlet.http.HttpServletRequest;

import org.botlibre.util.Utils;

@Entity
public class AgentStats implements Comparable<AgentStats> {
	public static int MAX_SIZE = 1000;
	public static Map<String, AgentStats> stats = new ConcurrentHashMap<String, AgentStats>();

	@Id
	@GeneratedValue
	public long id;
	public Timestamp date;
	public String agent;
	
	public int pages;
	public int sessions;
	public int api;
	
	public static void reset() {
		stats = new ConcurrentHashMap<String, AgentStats>();
	}
	
	public static AgentStats getStats(String agent) {
		if (stats.size() > MAX_SIZE) {
			// Clear bottom half from map.
			List<AgentStats> copy = new ArrayList<AgentStats>(stats.values());
			stats.clear();
			Collections.sort(copy);
			for (int index = copy.size() - 1; index > (copy.size() / 2); index--) {
				AgentStats stat = (AgentStats)copy.get(index);
				stats.put(stat.agent, stat);
			}
		}
		AgentStats stat = stats.get(agent);
		if (stat == null) {
			stat = new AgentStats();
			stat.agent = Utils.sanitize(agent);
			stats.put(agent, stat);
		}
		return stat;
	}
	
	public static List<AgentStats> sortedStats() {
		List<AgentStats> sorted = new ArrayList<AgentStats>(stats.values());
		Collections.sort(sorted);
		return sorted;
	}
	
	public static String extractAgent(HttpServletRequest request) {
		String agent = request.getHeader("user-agent");
		if (agent == null) {
			agent = "unknown";
		} else if (agent.length() > 255) {
			agent = agent.substring(0, 255);
		}
		return agent;
	}
	
	public static AgentStats api(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		String agent = extractAgent(request);
		AgentStats stat = getStats(agent);
		stat.api++;
		return stat;
	}
	
	public static void page(HttpServletRequest request) {
		if (request == null) {
			return;
		}
		String agent = extractAgent(request);
		AgentStats stat = getStats(agent);
		stat.pages++;
	}
	
	public static void session(HttpServletRequest request) {
		if (request == null) {
			return;
		}
		String agent = extractAgent(request);
		AgentStats stat = getStats(agent);
		stat.sessions++;
	}
	
	public AgentStats() {
		this.date = new Timestamp(System.currentTimeMillis());
	}
	
	public int compareTo(AgentStats stat) {
		return Integer.valueOf(pages).compareTo(stat.pages);
	}
}

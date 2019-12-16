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

import org.botlibre.util.Utils;

@Entity
public class ErrorStats implements Comparable<ErrorStats> {
	public static int MAX_SIZE = 1000;
	public static Map<String, ErrorStats> stats = new ConcurrentHashMap<String, ErrorStats>();

	@Id
	@GeneratedValue
	public long id;
	public Timestamp date;
	public String message;
	public int errors;
	
	public static void reset() {
		stats = new ConcurrentHashMap<String, ErrorStats>();
	}
	
	public static ErrorStats getStats(String message) {
		if (stats.size() > MAX_SIZE) {
			stats.clear();
		}
		message = Utils.sanitize(message);
		ErrorStats stat = stats.get(message);
		if (stat == null) {
			stat = new ErrorStats();
			stat.message = message;
			stats.put(message, stat);
		}
		return stat;
	}
	
	public static List<ErrorStats> sortedStats() {
		List<ErrorStats> sorted = new ArrayList<ErrorStats>(stats.values());
		Collections.sort(sorted);
		return sorted;
	}
	
	public static void error(Throwable exception) {
		if (exception == null) {
			return;
		}
		error(exception.toString());
	}
	
	public static void error(String message) {
		if (message == null || message.isEmpty()) {
			return;
		}
		if (message.length() > 150) {
			message = message.substring(0, 150);
		}
		ErrorStats stat = getStats(message);
		stat.errors++;
	}
	
	public ErrorStats() {
		this.date = new Timestamp(System.currentTimeMillis());
	}
	
	public int compareTo(ErrorStats stat) {
		return Integer.valueOf(this.errors).compareTo(stat.errors);
	}
}

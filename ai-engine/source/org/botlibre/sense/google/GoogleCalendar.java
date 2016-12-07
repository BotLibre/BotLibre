/******************************************************************************
 *
 *  Copyright 2016 Paphus Solutions Inc.
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
package org.botlibre.sense.google;

import java.util.Date;
import java.util.logging.Level;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;

/**
 * Sense to connect with Google Calendar services.
 */
public class GoogleCalendar extends Google {
	
	public GoogleCalendar(boolean enabled) {
		super(enabled);
	}
	
	public GoogleCalendar() {
		this(false);
	}
    
	/**
	 * Fetch the calendar events for the time period and convert JSON to objects.
	 */
	public Vertex getEvents(String calendar, Date from, Date to, Network network) {
		String url = "https://www.googleapis.com/calendar/v3/calendars/" + calendar + "/events?timeMax=" + printDate(to) + "&timeMin=" + printDate(from);
		log("GET events", Level.INFO, url);
		return requestJSON(url, "items", network);
	}
    
	/**
	 * Insert the event into the calendar.
	 */
	public Vertex insertEvent(String calendar, Vertex event, Network network) {
		String url = "https://www.googleapis.com/calendar/v3/calendars/" + calendar + "/events?";
		log("POST insert event", Level.INFO, url);
		return postJSON(url, event, network);
	}
    
	/**
	 * Delete the event from the calendar.
	 */
	public Vertex deleteEvent(String calendar, String event, Network network) {
		String url = "https://www.googleapis.com/calendar/v3/calendars/" + calendar + "/events/" + event;
		log("DELETE event", Level.INFO, url);
		return delete(url, network);
	}

	/**
	 * Self API.
	 * Return the calendar events objects for the time period.
	 */
	public Vertex getEvents(Vertex source, Vertex from, Vertex to) {
		if (!(from.getData() instanceof Date) || !(to.getData() instanceof Date)) {
			return null;
		}
		Network network = source.getNetwork();
		return getEvents("primary", (Date)from.getData(), (Date)to.getData(), network);
	}

	/**
	 * Self API.
	 * Return the calendar events objects for the time period.
	 */
	public Vertex getEvents(Vertex source, Vertex calendar, Vertex from, Vertex to) {
		if (!(from.getData() instanceof Date) || !(to.getData() instanceof Date)) {
			return null;
		}
		Network network = source.getNetwork();
		return getEvents(calendar.printString(), (Date)from.getData(), (Date)to.getData(), network);
	}

	/**
	 * Self API.
	 * Insert the event into the calendar.
	 */
	public Vertex insertEvent(Vertex source, Vertex event) {
		Network network = source.getNetwork();
		return insertEvent("primary", event, network);
	}

	/**
	 * Self API.
	 * Delete the event from the calendar.
	 */
	public Vertex deleteEvent(Vertex source, Vertex event) {
		Network network = source.getNetwork();
		return deleteEvent("primary", event.printString(), network);
	}

	/**
	 * Self API.
	 * Delete the event from the calendar.
	 */
	public Vertex deleteEvent(Vertex source, Vertex calendar, Vertex event) {
		Network network = source.getNetwork();
		return deleteEvent(calendar.printString(), event.printString(), network);
	}

	/**
	 * Self API.
	 * Insert the event into the calendar.
	 */
	public Vertex insertEvent(Vertex source, Vertex calendar, Vertex event) {
		Network network = source.getNetwork();
		return insertEvent(calendar.printString(), event, network);
	}
}
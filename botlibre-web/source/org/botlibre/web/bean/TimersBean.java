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
package org.botlibre.web.bean;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.botlibre.BotException;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.sense.timer.Timer;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;

public class TimersBean extends ServletBean {
	
	public TimersBean() {
	}

	public boolean getEnableTimers() {
		return getBot().awareness().getSense(Timer.class).getEnableTimers();
	}

	public int getTimerHours() {
		return getBot().awareness().getSense(Timer.class).getTimerHours();
	}

	public String getTimers() {
		List<Vertex> timers = getBot().awareness().getSense(Timer.class).getTimers(getBot().memory().newMemory());
		if (timers == null || timers.isEmpty()) {
			return "";
		}
		StringWriter writer = new StringWriter();
		Iterator<Vertex> iterator = timers.iterator();
		while (iterator.hasNext()) {
			Vertex message = iterator.next();
			writer.write(message.printString());	
			if (iterator.hasNext()) {
				writer.write("\n");				
			}
		}
		return writer.toString();
	}

	public void save(boolean enableTimers, String timerHours, String timers) throws Exception {
		timers = Utils.sanitize(timers);
		Timer sense = getBot().awareness().getSense(Timer.class);
		sense.setEnableTimers(enableTimers);
		int hours = 0;
		try {
			hours = Integer.valueOf(timerHours);
		} catch (NumberFormatException exception) {
			throw new BotException("Invalid timer hours number - " + timerHours + " - " + exception.getMessage());
		}
		sense.setTimerHours(hours);
		
		TextStream stream = new TextStream(timers.trim());
		List<String> messages = new ArrayList<String>();
		while (!stream.atEnd()) {
			String message = stream.upToAny("\n").trim();
			if (!message.isEmpty()) {
				messages.add(message);
			}
			stream.skip();
			stream.skipWhitespace();
		}
		sense.saveProperties(messages);
		getBotBean().setInstance(AdminDatabase.instance().updateInstanceTimers(getBotBean().getInstance().getId(), enableTimers));
	}

	@Override
	public void disconnectInstance() {
		disconnect();
	}
	
	@Override
	public void disconnect() {
	}

	public void runsTimers() {
		getBot().setDebugLevel(Level.FINE);
		Timer sense = getBot().awareness().getSense(Timer.class);
		sense.checkTimers();
	}
}

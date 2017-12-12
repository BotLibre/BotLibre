/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
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
package org.botlibre.tool;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicTool;

/**
 * Return current date and time.
 */

public class Watch extends BasicTool {

	public Watch() {
	}

	public Vertex time(Vertex source) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear(Calendar.YEAR);
		calendar.clear(Calendar.MONTH);
		calendar.clear(Calendar.DATE);
		calendar.clear(Calendar.MILLISECOND);
		return source.getNetwork().createVertex(new Time(calendar.getTimeInMillis()));
	}

	public Vertex date(Vertex source) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear(Calendar.HOUR);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MILLISECOND);
		return source.getNetwork().createVertex(new java.sql.Date(calendar.getTimeInMillis()));
	}

	public Vertex interval(Vertex source, Vertex style, Vertex from, Vertex to) throws Exception {
		SimpleDateFormat formater = new SimpleDateFormat();
		return interval(source, style, from, to, formater);
	}

	public Vertex interval(Vertex source, Vertex style, Vertex from, Vertex to, Vertex format) throws Exception {
		SimpleDateFormat formater = new SimpleDateFormat(format.getDataValue());
		return interval(source, style, from, to, formater);
	}

	public Vertex interval(Vertex source, Vertex style, Vertex from, Vertex to, SimpleDateFormat formater) throws Exception {
		Calendar fromDate = Calendar.getInstance();
		fromDate.setTime(formater.parse(from.getDataValue()));
		Calendar toDate = Calendar.getInstance();
		toDate.setTime(formater.parse(to.getDataValue()));
		String styleText = style.getDataValue();
		long value = 0;
		if (styleText.equals("years")) {
			value = toDate.get(Calendar.YEAR) - fromDate.get(Calendar.YEAR);
		} else if (styleText.equals("months")) {
			value = (toDate.get(Calendar.YEAR) * 12 + toDate.get(Calendar.MONTH)) - (fromDate.get(Calendar.YEAR) * 12 + fromDate.get(Calendar.MONTH));
		} else if (styleText.equals("weeks")) {
			value = (toDate.get(Calendar.YEAR) * 52 + toDate.get(Calendar.WEEK_OF_YEAR)) - (fromDate.get(Calendar.YEAR) * 52 + fromDate.get(Calendar.WEEK_OF_YEAR));
		} else if (styleText.equals("days")) {
			value = (toDate.get(Calendar.YEAR) * 365 + toDate.get(Calendar.DAY_OF_YEAR)) - (fromDate.get(Calendar.YEAR) * 365 + fromDate.get(Calendar.DAY_OF_YEAR));
		} else if (styleText.equals("hours")) {
			value = ((toDate.get(Calendar.YEAR) * 365 + toDate.get(Calendar.DAY_OF_YEAR)) * 24 + toDate.get(Calendar.HOUR_OF_DAY))
					- ((fromDate.get(Calendar.YEAR) * 365 + fromDate.get(Calendar.DAY_OF_YEAR)) * 24 + fromDate.get(Calendar.HOUR_OF_DAY));
		} else if (styleText.equals("minutes")) {
			long toMinutes = (((toDate.get(Calendar.YEAR) * 365 + toDate.get(Calendar.DAY_OF_YEAR)) * 24 + toDate.get(Calendar.HOUR_OF_DAY)) * 60) + toDate.get(Calendar.MINUTE);
			long fromMintues = (((fromDate.get(Calendar.YEAR) * 365 + fromDate.get(Calendar.DAY_OF_YEAR)) * 24 + fromDate.get(Calendar.HOUR_OF_DAY)) * 60) + fromDate.get(Calendar.MINUTE);
			value = toMinutes - fromMintues;
		} else if (styleText.equals("seconds")) {
			value = (((((toDate.get(Calendar.YEAR) * 365 + toDate.get(Calendar.DAY_OF_YEAR)) * 24 + toDate.get(Calendar.HOUR_OF_DAY)) * 60) + toDate.get(Calendar.MINUTE)) * 60 + toDate.get(Calendar.SECOND))
					- (((((fromDate.get(Calendar.YEAR) * 365 + fromDate.get(Calendar.DAY_OF_YEAR)) * 24 + fromDate.get(Calendar.HOUR_OF_DAY)) * 60) + fromDate.get(Calendar.MINUTE)) * 60 + fromDate.get(Calendar.SECOND));
		}
		return style.getNetwork().createVertex(value);
	}

	public Vertex add(Vertex source, Vertex time, Vertex date, Vertex part) throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime((Date)date.getData());
		if (part.is(Primitive.DAY)) {
			calendar.add(Calendar.DATE, ((Number)time.getData()).intValue());
		} else if (part.is(Primitive.WEEK)) {
			calendar.add(Calendar.DATE, ((Number)time.getData()).intValue() * 7);
		} else if (part.is(Primitive.MONTH)) {
			calendar.add(Calendar.MONTH, ((Number)time.getData()).intValue());
		} else if (part.is(Primitive.YEAR)) {
			calendar.add(Calendar.YEAR, ((Number)time.getData()).intValue());
		}
		return time.getNetwork().createVertex(new java.sql.Date(calendar.getTimeInMillis()));
	}

	public Vertex dateWithFormat(Vertex source, Vertex vertex) {
		String text = vertex.getDataValue();
		SimpleDateFormat format = new SimpleDateFormat(text);
		text = format.format(new Date());
		return vertex.getNetwork().createVertex(text);
	}
	
	public Vertex date(Vertex source, Vertex vertex) {
		Calendar date = Calendar.getInstance();
		String text = vertex.getDataValue();
		if (text.contains("%a")) {
			text = text.replace("%a", date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US));
		}
		if (text.contains("%A")) {
			text = text.replace("%A", date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US));
		}
		if (text.contains("%b")) {
			text = text.replace("%b", date.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US));
		}
		if (text.contains("%B")) {
			text = text.replace("%B", date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US));
		}
		if (text.contains("%c")) {
			text = text.replace("%c", new Date().toString());
		}
		if (text.contains("%d")) {
			text = text.replace("%d", String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
		}
		if (text.contains("%H")) {
			text = text.replace("%H", String.valueOf(date.get(Calendar.HOUR_OF_DAY)));
		}
		if (text.contains("%I")) {
			text = text.replace("%I", String.valueOf(date.get(Calendar.HOUR) + 1));
		}
		if (text.contains("%j")) {
			text = text.replace("%j", String.valueOf(date.get(Calendar.DAY_OF_YEAR)));
		}
		if (text.contains("%m")) {
			text = text.replace("%m", String.valueOf(date.get(Calendar.MONTH) + 1));
		}
		if (text.contains("%M")) {
			text = text.replace("%M", String.valueOf(date.get(Calendar.MINUTE)));
		}
		if (text.contains("%p")) {
			text = text.replace("%p", date.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.US));
		}
		if (text.contains("%S")) {
			text = text.replace("%S", String.valueOf(date.get(Calendar.SECOND)));
		}
		if (text.contains("%U")) {
			text = text.replace("%U", String.valueOf(date.get(Calendar.WEEK_OF_YEAR)));
		}
		if (text.contains("%w")) {
			text = text.replace("%w", String.valueOf(date.get(Calendar.DAY_OF_WEEK)));
		}
		if (text.contains("%W")) {
			text = text.replace("%W", String.valueOf(date.get(Calendar.WEEK_OF_YEAR)));
		}
		if (text.contains("%x")) {
			text = text.replace("%x", new java.sql.Date(date.getTimeInMillis()).toString());
		}
		if (text.contains("%X")) {
			text = text.replace("%X", new java.sql.Time(date.getTimeInMillis()).toString());
		}
		if (text.contains("%y")) {
			text = text.replace("%y", String.valueOf(date.get(Calendar.YEAR)));
		}
		if (text.contains("%Y")) {
			text = text.replace("%Y", String.valueOf(date.get(Calendar.YEAR)));
		}
		if (text.contains("%Z")) {
			text = text.replace("%Z", String.valueOf(date.getTimeZone()));
		}
		return vertex.getNetwork().createVertex(text);
	}
	
}
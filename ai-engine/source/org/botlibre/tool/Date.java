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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.botlibre.BotException;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicTool;
import org.botlibre.util.Utils;

/**
 * Return current date and time.
 */

public class Date extends BasicTool {
	
	static long millennium;
	
	protected List<SimpleDateFormat> patterns;
	protected List<SimpleDateFormat> yearPatterns;
	
	public static long millennium() {
		if (millennium == 0) {
			millennium = Utils.parseDate("1000-01-01").getTime();
		}
		return millennium;
	}

	public Date() {
	}

	public Vertex time(Vertex source) {
		Calendar calendar = Calendar.getInstance();
		Time time = Utils.parseTime(
				calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND));
		return source.getNetwork().createVertex(time);
	}

	public Vertex time(Vertex source, Vertex value) {
		if (value.getData() instanceof java.util.Date) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime((java.util.Date)value.getData());
			Time time = Utils.parseTime(
					calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND));
			return source.getNetwork().createVertex(time);
		}
		java.sql.Time time = Utils.parseTime(value.printString());
		return source.getNetwork().createVertex(time);
	}
	
	public Vertex getTimeZone(Vertex source) {
		return source.getNetwork().createVertex(TimeZone.getDefault().getID());
	}
	
	public Vertex getTimeZone(Vertex source, Vertex timezone) {
		return source.getNetwork().createVertex(TimeZone.getTimeZone(timezone.getDataValue()).getID());
	}
	
	public Vertex setTimeZone(Vertex source, Vertex date, Vertex timezone) {
		if (!(date.getData() instanceof java.util.Date)) {
			return date;
		}
		java.util.Date value = (java.util.Date)date.getData();
		Calendar calendar = Calendar.getInstance();
		if (value instanceof Time) {
			// Must use current day to account for daylight saving time, etc.
			Calendar calendar2 = Calendar.getInstance();
			System.out.println(calendar.get(Calendar.HOUR_OF_DAY));
			calendar2.setTime(value);
			calendar.set(Calendar.HOUR_OF_DAY, calendar2.get(Calendar.HOUR_OF_DAY));
			calendar.set(Calendar.MINUTE, calendar2.get(Calendar.MINUTE));
			calendar.set(Calendar.SECOND, calendar2.get(Calendar.SECOND));
			calendar.get(Calendar.HOUR_OF_DAY); // Calendar bug...
		} else {
			calendar.setTime(value);
		}
		calendar.setTimeZone(TimeZone.getTimeZone(timezone.getDataValue()));
		if (value instanceof Time) {
			return source.getNetwork().createVertex(Utils.parseTime(
					calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND)));
		} else if (value instanceof java.sql.Date) {
			return source.getNetwork().createVertex(Utils.parseDate(
					calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE)));
		} else if (value instanceof java.sql.Timestamp) {
			java.sql.Timestamp timestamp = (java.sql.Timestamp)value;
			String nanosString;
	        if (timestamp.getNanos() == 0) {
	            nanosString = "0";
	        } else {
	            nanosString = Utils.buildZeroPrefixAndTruncTrailZeros(timestamp.getNanos(), 9);
	        }
			return source.getNetwork().createVertex(Utils.parseTimestamp(
					calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE)
					+ calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND)
					+ "." + nanosString));
		}
		java.sql.Timestamp timestamp = (java.sql.Timestamp)value;
		String milisString;
        if (timestamp.getNanos() == 0) {
        	milisString = "0";
        } else {
        	milisString = Utils.buildZeroPrefixAndTruncTrailZeros(timestamp.getNanos(), 3);
        }
		return source.getNetwork().createVertex(Utils.parseTimestamp(
				calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE)
				+ calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND)
				+ "." + milisString));
	}

	public Vertex date(Vertex source) {
		Calendar calendar = Calendar.getInstance();
		java.sql.Date date = Utils.parseDate(
				calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE));
		return source.getNetwork().createVertex(date);
	}

	public Vertex date(Vertex source, Vertex value) {
		if (value.getData() instanceof java.util.Date) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime((java.util.Date)value.getData());
			java.sql.Date date = Utils.parseDate(
					calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE));
			return source.getNetwork().createVertex(date);
		}
		String text = value.printString();
		text = text.replace(" / ", "/");
		text = text.replace(" - ", "-");
		java.sql.Date date = Utils.parseDate(text);
		return source.getNetwork().createVertex(date);
	}

	public Vertex timestamp(Vertex source) {
		Calendar calendar = Calendar.getInstance();
		return source.getNetwork().createVertex(new java.sql.Timestamp(calendar.getTimeInMillis()));
	}

	/**
	 * Attempt to parse the date/time value in any format.
	 */
	public Vertex any(Vertex source, Vertex value) {
		String text = value.printString();
		// Allow / or - or .
		text = text.replace("/", "-");
		text = text.replace(".", "-");
		// Pattern spaces -
		text = text.replace(" - ", "-");
		// Pattern * ignores :
		text = text.replace(":", " ");
		// Ensure space before am/pm
		if (text.contains("am")) {
			text = text.replace("0am", "0 am");
			text = text.replace("1am", "1 am");
			text = text.replace("2am", "2 am");
			text = text.replace("3am", "3 am");
			text = text.replace("4am", "4 am");
			text = text.replace("5am", "5 am");
			text = text.replace("6am", "6 am");
			text = text.replace("7am", "7 am");
			text = text.replace("8am", "8 am");
			text = text.replace("9am", "9 am");
		}
		if (text.contains("pm")) {
			text = text.replace("0pm", "0 pm");
			text = text.replace("1pm", "1 pm");
			text = text.replace("2pm", "2 pm");
			text = text.replace("3pm", "3 pm");
			text = text.replace("4pm", "4 pm");
			text = text.replace("5pm", "5 pm");
			text = text.replace("6pm", "6 pm");
			text = text.replace("7pm", "7 pm");
			text = text.replace("8pm", "8 pm");
			text = text.replace("9pm", "9 pm");
		}
		// ignore 1st 3rd 4th
		if (text.contains("st") || text.contains("rd") || text.contains("st")) {
			text = text.replace("1st", "1");
			text = text.replace("1th", "1");
			text = text.replace("2nd", "2");
			text = text.replace("2th", "2");
			text = text.replace("3rd", "3");
			text = text.replace("3th", "3");
			text = text.replace("4th", "4");
			text = text.replace("5th", "5");
			text = text.replace("6th", "6");
			text = text.replace("7th", "7");
			text = text.replace("8th", "8");
			text = text.replace("9th", "9");
			text = text.replace("1 st", "1");
			text = text.replace("1 th", "1");
			text = text.replace("2 nd", "2");
			text = text.replace("2 th", "2");
			text = text.replace("3 rd", "3");
			text = text.replace("3 th", "3");
			text = text.replace("4 th", "4");
			text = text.replace("5 th", "5");
			text = text.replace("6 th", "6");
			text = text.replace("7 th", "7");
			text = text.replace("8 th", "8");
			text = text.replace("9 th", "9");
		}
		
		if (this.patterns == null) {
			List<SimpleDateFormat> patterns = new ArrayList<SimpleDateFormat>();
			patterns.add(new SimpleDateFormat("yyyy-MM-dd HH mm ss Z"));
			patterns.add(new SimpleDateFormat("yyyy-MM-dd hh mm ss a"));
			patterns.add(new SimpleDateFormat("yyyy-MM-dd HH mm ss"));
			patterns.add(new SimpleDateFormat("yyyy-MM-dd HH mm Z"));
			patterns.add(new SimpleDateFormat("yyyy-MM-dd hh mm a"));
			patterns.add(new SimpleDateFormat("yyyy-MM-dd hh a"));
			patterns.add(new SimpleDateFormat("yyyy-MM-dd HH mm"));
			patterns.add(new SimpleDateFormat("dd-MM-yyyy HH mm ss"));
			patterns.add(new SimpleDateFormat("MMM d yyyy HH mm ss Z"));
			patterns.add(new SimpleDateFormat("d MMM yyyy HH mm ss Z"));
			patterns.add(new SimpleDateFormat("yyyy-MM-dd"));
			patterns.add(new SimpleDateFormat("dd-MM-yyyy"));
			patterns.add(new SimpleDateFormat("MMM d yyyy"));
			patterns.add(new SimpleDateFormat("d MMM yyyy"));
			for (SimpleDateFormat pattern : patterns) {
				pattern.setLenient(false);
			}
			this.patterns = patterns;

			patterns = new ArrayList<SimpleDateFormat>();
			patterns.add(new SimpleDateFormat("dd-MM-yy HH mm ss Z"));
			patterns.add(new SimpleDateFormat("dd-MM-yy HH mm ss"));
			patterns.add(new SimpleDateFormat("MMM d yy HH mm ss Z"));
			patterns.add(new SimpleDateFormat("d MMM yy HH mm ss Z"));
			patterns.add(new SimpleDateFormat("dd-MM-yy"));
			patterns.add(new SimpleDateFormat("yy-MM-dd"));
			patterns.add(new SimpleDateFormat("MM-dd-yy"));
			for (SimpleDateFormat pattern : patterns) {
				pattern.setLenient(false);
			}
			this.yearPatterns = patterns;
		}

		Timestamp date = null;
		for (SimpleDateFormat pattern : this.patterns) {
		    try {
		    	synchronized (pattern) {
		    		date = new Timestamp(pattern.parse(text).getTime());
		    	}
		        break;
		    } catch (Exception exception) {}
		}
		if (date == null) {
			return null;
		}
		if (date.getTime() < millennium()) {
			for (SimpleDateFormat pattern : this.yearPatterns) {
			    try {
			    	synchronized (pattern) {
			    		date = new Timestamp(pattern.parse(text).getTime());
			    	}
			        break;
			    } catch (Exception exception) { }
			}
		}
		return source.getNetwork().createVertex(date);
	}

	public Vertex timestamp(Vertex source, Vertex value) {
		if (value.getData() instanceof java.util.Date) {
			return source.getNetwork().createVertex(new Timestamp(((java.util.Date)value.getData()).getTime()));
		}
		Timestamp date = Utils.parseTimestamp(value.printString());
		return source.getNetwork().createVertex(date);
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
			value = (toDate.get(Calendar.YEAR) * 52 + toDate.get(Calendar.WEEK_OF_YEAR)) - (fromDate.get(Calendar.YEAR) * 52 + fromDate.get(Calendar.WEEK_OF_YEAR)) - 1;
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
		return source.getNetwork().createVertex(value);
	}

	public Vertex add(Vertex source, Vertex date, Vertex part, Vertex time) throws Exception {
		if (!(date.getData() instanceof java.util.Date)) {
			throw new BotException("Expected date object not " + date.printString());
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime((java.util.Date)date.getData());
		boolean datePart = false;
		boolean timePart = false;
		if (part.is(Primitive.DAY)) {
			calendar.add(Calendar.DATE, ((Number)time.getData()).intValue());
			datePart = true;
		} else if (part.is(Primitive.WEEK)) {
			calendar.add(Calendar.DATE, ((Number)time.getData()).intValue() * 7);
			datePart = true;
		} else if (part.is(Primitive.MONTH)) {
			calendar.add(Calendar.MONTH, ((Number)time.getData()).intValue());
			datePart = true;
		} else if (part.is(Primitive.YEAR)) {
			calendar.add(Calendar.YEAR, ((Number)time.getData()).intValue());
			datePart = true;
		} else if (part.is(Primitive.HOUR)) {
			calendar.add(Calendar.HOUR, ((Number)time.getData()).intValue());
			timePart = true;
		} else if (part.is(Primitive.MINUTE)) {
			calendar.add(Calendar.MINUTE, ((Number)time.getData()).intValue());
			timePart = true;
		} else if (part.is(Primitive.SECOND)) {
			calendar.add(Calendar.SECOND, ((Number)time.getData()).intValue());
			timePart = true;
		}
		if (date.getData() instanceof java.sql.Date && datePart) {
			return source.getNetwork().createVertex(new java.sql.Date(calendar.getTimeInMillis()));
		} else if (date.getData() instanceof java.sql.Time && timePart) {
			return source.getNetwork().createVertex(new java.sql.Time(calendar.getTimeInMillis()));
		}
		return source.getNetwork().createVertex(new java.sql.Timestamp(calendar.getTimeInMillis()));
	}

	public Vertex set(Vertex source, Vertex date, Vertex part, Vertex value) throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime((java.util.Date)date.getData());
		if (part.is(Primitive.DAY)) {
			calendar.set(Calendar.DATE, ((Number)value.getData()).intValue());
		} else if (part.is(Primitive.MONTH)) {
			calendar.set(Calendar.MONTH, ((Number)value.getData()).intValue() - 1);
		} else if (part.is(Primitive.YEAR)) {
			calendar.set(Calendar.YEAR, ((Number)value.getData()).intValue());
		} else if (part.is(Primitive.HOUR)) {
			calendar.set(Calendar.HOUR_OF_DAY, ((Number)value.getData()).intValue());
		} else if (part.is(Primitive.MINUTE)) {
			calendar.set(Calendar.MINUTE, ((Number)value.getData()).intValue());
		} else if (part.is(Primitive.SECOND)) {
			calendar.set(Calendar.SECOND, ((Number)value.getData()).intValue());
		}
		if (date.getData() instanceof java.sql.Date) {
			return source.getNetwork().createVertex(new java.sql.Date(calendar.getTimeInMillis()));
		} else if (date.getData() instanceof java.sql.Time) {
			return source.getNetwork().createVertex(new java.sql.Time(calendar.getTimeInMillis()));
		}
		return source.getNetwork().createVertex(new java.sql.Timestamp(calendar.getTimeInMillis()));
	}

	public Vertex get(Vertex source, Vertex date, Vertex part) throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime((java.util.Date)date.getData());
		int value = 0;
		if (part.is(Primitive.DAY)) {
			value = calendar.get(Calendar.DATE);
		} else if (part.is(Primitive.WEEK)) {
			value = calendar.get(Calendar.WEEK_OF_YEAR);
		} else if (part.is(Primitive.MONTH)) {
			value = calendar.get(Calendar.MONTH) + 1;
		} else if (part.is(Primitive.YEAR)) {
			value = calendar.get(Calendar.YEAR);
		} else if (part.is(Primitive.HOUR)) {
			value = calendar.get(Calendar.HOUR_OF_DAY);
		} else if (part.is(Primitive.MINUTE)) {
			value = calendar.get(Calendar.MINUTE);
		} else if (part.is(Primitive.SECOND)) {
			value = calendar.get(Calendar.SECOND);
		}
		return source.getNetwork().createVertex(value);
	}
	
	public Vertex printDate(Vertex source, Vertex date, Vertex format) {
		return printDate(source, date, format, null, null);
	}

	public Vertex printDate(Vertex source, Vertex date, Vertex format, Vertex timezone, Vertex locale) {
		String text = format.getDataValue();
		Locale localeValue = Locale.US;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime((java.util.Date)date.getData());
		if (timezone != null && !timezone.is(Primitive.NULL)) {
			calendar.setTimeZone(TimeZone.getTimeZone("GMT" + timezone.printString()));
		}
		if (locale != null && !locale.is(Primitive.NULL)) {
			localeValue = Locale.forLanguageTag(locale.printString());
		}
		SimpleDateFormat formater = new SimpleDateFormat(text, localeValue);
		text = formater.format(Utils.parseTimestamp(
				calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE) + " "
				+ calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND)));
		return source.getNetwork().createVertex(text);
	}
	
	public Vertex printAIMLDate(Vertex source, Vertex date, Vertex format) {
		return printAIMLDate(source, date, format, null, null);
	}
	
	public Vertex printAIMLDate(Vertex source, Vertex date, Vertex format, Vertex timezone, Vertex locale) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime((java.util.Date)date.getData());
		Locale localeValue = Locale.US;
		if (timezone != null && !timezone.is(Primitive.NULL)) {
			calendar.setTimeZone(TimeZone.getTimeZone("GMT" + timezone.printString()));
		}
		if (locale != null && !locale.is(Primitive.NULL)) {
			localeValue = Locale.forLanguageTag(locale.printString());
		}
		String text = format.getDataValue();
		if (text.contains("%a")) {
			text = text.replace("%a", calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, localeValue));
		}
		if (text.contains("%A")) {
			text = text.replace("%A", calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, localeValue));
		}
		if (text.contains("%b")) {
			text = text.replace("%b", calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, localeValue));
		}
		if (text.contains("%B")) {
			text = text.replace("%B", calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, localeValue));
		}
		if (text.contains("%c")) {
			java.sql.Timestamp timestamp = Utils.parseTimestamp(
					calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE) + " "
					+ calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND));
			text = text.replace("%c", timestamp.toString());
		}
		if (text.contains("%d")) {
			text = text.replace("%d", String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
		}
		if (text.contains("%H")) {
			text = text.replace("%H", String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)));
		}
		if (text.contains("%I")) {
			text = text.replace("%I", String.valueOf(calendar.get(Calendar.HOUR) + 1));
		}
		if (text.contains("%j")) {
			text = text.replace("%j", String.valueOf(calendar.get(Calendar.DAY_OF_YEAR)));
		}
		if (text.contains("%m")) {
			text = text.replace("%m", String.valueOf(calendar.get(Calendar.MONTH) + 1));
		}
		if (text.contains("%M")) {
			text = text.replace("%M", String.valueOf(calendar.get(Calendar.MINUTE)));
		}
		if (text.contains("%p")) {
			text = text.replace("%p", calendar.getDisplayName(Calendar.AM_PM, Calendar.SHORT, localeValue));
		}
		if (text.contains("%S")) {
			text = text.replace("%S", String.valueOf(calendar.get(Calendar.SECOND)));
		}
		if (text.contains("%U")) {
			text = text.replace("%U", String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR)));
		}
		if (text.contains("%w")) {
			text = text.replace("%w", String.valueOf(calendar.get(Calendar.DAY_OF_WEEK)));
		}
		if (text.contains("%W")) {
			text = text.replace("%W", String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR)));
		}
		if (text.contains("%x")) {
			java.sql.Date value = Utils.parseDate(
					calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE));
			text = text.replace("%x", value.toString());
		}
		if (text.contains("%X")) {
			Time time = Utils.parseTime(
					calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND));
			text = text.replace("%X", time.toString());
		}
		if (text.contains("%y")) {
			text = text.replace("%y", String.valueOf(calendar.get(Calendar.YEAR)));
		}
		if (text.contains("%Y")) {
			text = text.replace("%Y", String.valueOf(calendar.get(Calendar.YEAR)));
		}
		if (text.contains("%Z")) {
			text = text.replace("%Z", String.valueOf(calendar.getTimeZone()));
		}
		return source.getNetwork().createVertex(text);
	}
	
}
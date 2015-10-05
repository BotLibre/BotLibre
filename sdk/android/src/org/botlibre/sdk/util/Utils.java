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

package org.botlibre.sdk.util;

import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class.
 */
public class Utils {
	
	public static Pattern httpRegex = Pattern.compile("\\b(?:https?|ftp|file):\\/\\/[a-z0-9-+&@#\\/%?=~_|!:,.;]*[a-z0-9-+&@#\\/%=~_|]", Pattern.CASE_INSENSITIVE);
	public static Pattern wwwRegex = Pattern.compile("((www\\.)[^\\s]+)", Pattern.CASE_INSENSITIVE);
	public static Pattern emailRegex = Pattern.compile("(([a-zA-Z0-9_\\-\\.]+)@[a-zA-Z_]+?(?:\\.[a-zA-Z]{2,6}))+", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Replace links with HTML links.
	 * Includes http, www, images, video, audio, email address.
	 */
	public static String linkHTML(String text) {
		if (text == null || text.length() == 0) {
			return "";
		}
		boolean http = text.indexOf("http") != -1;
		boolean www = text.indexOf("www.") != -1;
		boolean email = text.indexOf("@") != -1;
		if (!http && !www && !email) {
			return text;
		}
		if (text.indexOf("<") != -1 && text.indexOf(">") != -1) {
			return text;
		}
		if (http) {
			Matcher matcher = httpRegex.matcher(text);
			StringBuffer sb = new StringBuffer();
			while (matcher.find()) {
				String url = matcher.group();
		    	if (url.indexOf(".png") != -1 || url.indexOf(".jpg") != -1 || url.indexOf(".jpeg") != -1 || url.indexOf(".gif") != -1) {
		    		url = "<a href='" + url + "' target='_blank'><img src='" + url + "' height='50'></a>";
		    	} else if (url.indexOf(".mp4") != -1 || url.indexOf(".webm") != -1 || url.indexOf(".ogg") != -1) {
		    		url = "<a href='" + url + "' target='_blank'><video src='" + url + "' height='50'></a>";
		    	} else if (url.indexOf(".wav") != -1 || url.indexOf(".mp3") != -1) {
		    		url = "<a href='" + url + "' target='_blank'><audio src='" + url + "' controls>audio</a>";
		    	} else {
		    		url = "<a href='" + url + "' target='_blank'>" + url + "</a>";
		    	}
				matcher.appendReplacement(sb, url);
			}
			matcher.appendTail(sb);
			text = sb.toString();
		} else if (www) {
			Matcher matcher = wwwRegex.matcher(text);
			StringBuffer sb = new StringBuffer();
			while (matcher.find()) {
				String url = matcher.group();
				matcher.appendReplacement(sb, "<a href='http://" + url + "' target='_blank'>" + url + "</a>");
			}
			matcher.appendTail(sb);
			text = sb.toString();
		}
		
		if (email) {
			Matcher matcher = emailRegex.matcher(text);
			StringBuffer sb = new StringBuffer();
			while (matcher.find()) {
				String address = matcher.group();
				matcher.appendReplacement(sb, "<a href='mailto://" + address + "' target='_blank'>" + address + "</a>");
			}
			matcher.appendTail(sb);
			text = sb.toString();
		}
		return text;
	}
	
	/**
	 * Strip the HTML tags from the text.
	 */
	public static String stripTags(String html) {
		if (html == null) {
			return "";
		}
		if ((html.indexOf('<') == -1) || (html.indexOf('>') == -1)) {
			return html;
		}		
		StringWriter writer = new StringWriter();
		TextStream stream = new TextStream(html);
		while (!stream.atEnd()) {
			String text = stream.upTo('<');
			writer.write(text);
			int position = stream.getPosition();
			stream.skip();
			String word = stream.nextWord();
			if (word != null) {
				if (word.equals("p")) {
					writer.write("\n\n");
				} else if (word.equals("br")) {
					writer.write("\n");
				} else if (word.equals("div")) {
					writer.write("\n");
				}
				stream.skipTo('>');
				if (stream.atEnd()) {
					stream.setPosition(position);
					writer.write(stream.upToEnd());
				} else {
					stream.skip();
				}
			}
		}
		return writer.toString();
	}
}
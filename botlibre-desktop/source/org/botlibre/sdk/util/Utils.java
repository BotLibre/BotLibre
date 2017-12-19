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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.botlibre.BotException;

/**
 * Utility class.
 */
public class Utils {
	public static Random random = new Random();
	
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
	 * Replace reserved HTML character with their HTML escape codes.
	 */
	public static String escapeHTML(String html) {
		return html.replace("&", "&amp;")
	    	.replace("<", "&lt;")
	    	.replace(">", "&gt;")
	    	.replace("\"", "&quot;")
	    	.replace("`", "&#96;")
	    	.replace("'", "&#39;");
	}

	/**
	 * Strip the HTML tags from the text.
	 */
	public static String stripTags(String html) {
		if (html == null) {
			return "";
		}
		String result = html;
		if ((html.indexOf('<') != -1) && (html.indexOf('>') != -1)) {
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
			result = writer.toString();
		}
		if (result.contains("&")) {
			result = result.replace("&#34;", "\"");
			result = result.replace("&#96;", "`");
			result = result.replace("&#39;", "'");
			result = result.replace("&#64;", "@");
			result = result.replace("&#61;", "=");
			result = result.replace("&lt;", "<");
			result = result.replace("&gt;", ">");
		}
		return result;
	}

	/**
	 * Format the text that may be HTML, or may be text, or markup, or a mix.
	 */
	public static String formatHTMLOutput(String text) {
		if (text == null) {
			return "";
		}
		int index = text.indexOf('<');
		int index2 = text.indexOf('>');
		boolean isHTML = (index != -1) && (index2 > index);
		boolean isMixed = isHTML && text.contains("[code]");
		if (isHTML && !isMixed) {
			return text;
		}
		if (!isMixed && ((index != -1) || (index2 != -1))) {
			text = text.replace("<", "&lt;");
			text = text.replace(">", "&gt;");
		}
		TextStream stream = new TextStream(text);
		StringWriter writer = new StringWriter();
		boolean bullet = false;
		boolean nbullet = false;
		while (!stream.atEnd()) {
			String line = stream.nextLine();
			if (!isMixed && (line.contains("http://") || line.contains("https://"))) {
				line = Utils.linkHTML(line);
			}
			TextStream lineStream = new TextStream(line);
			boolean firstWord = true;
			boolean span = false;
			boolean cr = true;
			while (!lineStream.atEnd()) {
				while (!isMixed && firstWord && lineStream.peek() == ' ') {
					lineStream.next();
					writer.write("&nbsp;");
				}
				String whitespace = lineStream.nextWhitespace();
				writer.write(whitespace);
				if (lineStream.atEnd()) {
					break;
				}
				String word = lineStream.nextWord();
				if (!isMixed && nbullet && firstWord && !word.equals("#")) {
					writer.write("</ol>\n");
					nbullet = false;
				} else if (!isMixed && bullet && firstWord && !word.equals("*")) {
					writer.write("</ul>\n");
					bullet = false;
				}
				if (firstWord && word.equals("[")) {
					String peek = lineStream.peekWord();
					if ("code".equals(peek)) {
						lineStream.nextWord();
						String next = lineStream.nextWord();
						String lang = "javascript";
						int lines = 20;
						if ("lang".equals(next)) {
							lineStream.skip();
							lang = lineStream.nextWord();
							if ("\"".equals(lang)) {
								lang = lineStream.nextWord();
								lineStream.skip();
							}
							next = lineStream.nextWord();
						}
						if ("lines".equals(next)) {
							lineStream.skip();
							String value = lineStream.nextWord();
							if ("\"".equals(value)) {
								value = lineStream.nextWord();
								lineStream.skip();
							}
							lineStream.skip();
							try {
								lines = Integer.valueOf(value);
							} catch (NumberFormatException ignore) {}
						}
						String id = "code" + stream.getPosition();
						writer.write("<div style=\"width:100%;height:" + lines * 14 + "px;max-width:none\" id=\"" + id + "\">");
						String code = lineStream.upToAll("[code]");
						if (code.indexOf('<') != -1) {
							code = code.replace("<", "&lt;");
						}
						if (code.indexOf('>') != -1) {
							code = code.replace(">", "&gt;");
						}
						writer.write(code);
						while (lineStream.atEnd() && !stream.atEnd()) {
							line = stream.nextLine();
							lineStream = new TextStream(line);
							while (lineStream.peek() == ':') {
								lineStream.next();
								writer.write("&nbsp;&nbsp;&nbsp;&nbsp;");								
							}
							code = lineStream.upToAll("[code]");
							if (code.indexOf('<') != -1) {
								code = code.replace("<", "&lt;");
							}
							if (code.indexOf('>') != -1) {
								code = code.replace(">", "&gt;");
							}
							writer.write(code);
						}
						lineStream.skip("[code]".length());
						writer.write("</div>\n");

						writer.write("<script>\n");
						writer.write("var " + id + " = ace.edit('" + id + "');\n");
						writer.write(id + ".getSession().setMode('ace/mode/" + lang + "');\n");
						writer.write(id + ".setReadOnly(true);\n");
						writer.write("</script>\n");
					} else {
						writer.write(word);
					}
				} else if (!isMixed && firstWord && word.equals("=")) {
					int count = 2;
					String token = word;
					while (!lineStream.atEnd() && lineStream.peek() == '=') {
						lineStream.skip();
						count++;
						token = token + "=";
					}
					String header = lineStream.upToAll(token);
					if (lineStream.atEnd()) {
						writer.write(token);
						writer.write(header);
					} else {
						lineStream.skip(token.length());
						writer.write("<h");
						writer.write(String.valueOf(count));
						writer.write(">");
						writer.write(header);
						writer.write("</h");
						writer.write(String.valueOf(count));
						writer.write(">");
						cr = false;
					}
				} else if (!isMixed && firstWord && word.equals(":")) {
					span = true;
					int indent = 1;
					while (!lineStream.atEnd() && lineStream.peek() == ':') {
						lineStream.skip();				
						indent++;
					}
					writer.write("<span style=\"display:inline-block;text-indent:");
					writer.write(String.valueOf(indent * 20));
					writer.write("px;\">");	
				} else if (!isMixed && firstWord && word.equals("*")) {
					if (!bullet) {
						writer.write("<ul>");
						bullet = true;
					}
					writer.write("<li>");
					cr = false;
				} else if (!isMixed && firstWord && word.equals("#")) {
					if (!nbullet) {
						writer.write("<ol>");
						nbullet = true;
					}
					writer.write("<li>");
					cr = false;
				} else {
					writer.write(word);
				}
				firstWord = false;
			}
			if (!isMixed && span) {
				writer.write("</span>");
			}
			if (!isMixed && cr) { 
				writer.write("<br/>\n");
			}
		}
		if (!isMixed && bullet) {
			writer.write("</ul>");
		}
		if (!isMixed && nbullet) {
			writer.write("</ol>");
		}
		return writer.toString();
	}
	
	public static String displayTimestamp(Date date) {
		if (date == null) {
			return "";
		}
		StringWriter writer = new StringWriter();
		Calendar today = Calendar.getInstance();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
				&& calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
			writer.write("Today");
		} else if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
				&& calendar.get(Calendar.DAY_OF_YEAR) == (today.get(Calendar.DAY_OF_YEAR) - 1)) {
			writer.write("Yesterday");
		} else {
			writer.write(calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US));
			writer.write(" ");
			writer.write(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
			if (calendar.get(Calendar.YEAR) != today.get(Calendar.YEAR)) {
				writer.write(" ");
				writer.write(String.valueOf(calendar.get(Calendar.YEAR)));
			}
		}
		writer.write(", ");
		writer.write(String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)));
		writer.write(":");
		if (calendar.get(Calendar.MINUTE) < 10) {
			writer.write("0");
		}
		writer.write(String.valueOf(calendar.get(Calendar.MINUTE)));
		
		return writer.toString();
	}
	
	public static String displayTime(Date date) {
		if (date == null) {
			return "";
		}
		StringWriter writer = new StringWriter();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		writer.write(String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)));
		writer.write(":");
		if (calendar.get(Calendar.MINUTE) < 10) {
			writer.write("0");
		}
		writer.write(String.valueOf(calendar.get(Calendar.MINUTE)));
		writer.write(":");
		if (calendar.get(Calendar.SECOND) < 10) {
			writer.write("0");
		}
		writer.write(String.valueOf(calendar.get(Calendar.SECOND)));
		
		return writer.toString();
	}
	
	public static String displayDate(Date date) {
		if (date == null) {
			return "";
		}
		StringWriter writer = new StringWriter();
		Calendar today = Calendar.getInstance();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
				&& calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
			writer.write("Today");
		} else if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
				&& calendar.get(Calendar.DAY_OF_YEAR) == (today.get(Calendar.DAY_OF_YEAR) - 1)) {
			writer.write("Yesterday");
		} else {
			writer.write(calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US));
			writer.write(" ");
			writer.write(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
			if (calendar.get(Calendar.YEAR) != today.get(Calendar.YEAR)) {
				writer.write(" ");
				writer.write(String.valueOf(calendar.get(Calendar.YEAR)));
			}
		}
		
		return writer.toString();
	}
	
	/**
	 * Capitalize the first character of the string.
	 */
	public static String capitalize(String text) {
		if (text.length() == 0) {
			return text;
		}
		return Character.toUpperCase(text.charAt(0)) + text.substring(1, text.length());
	}
	
	public static Random random() {
		return random;
	}
	
	public static int random(int max) {
		return random().nextInt(max);
	}
	
	public static <T> T random(List<T> list) {
		if ((list == null) || list.isEmpty()) {
			return null;
		}
		return list.get(random().nextInt(list.size()));
	}
	/**
	 * Get the contents of the stream to a .self file and parse it.
	 */
	public static String loadTextFile(InputStream stream, String encoding, int maxSize, boolean finish) {
		if (encoding.trim().isEmpty()) {
			encoding = "UTF-8";
		}

	    // FEFF because this is the Unicode char represented by the UTF-8 byte order mark (EF BB BF).
	    String UTF8_BOM = "\uFEFF";
	    
		StringWriter writer = new StringWriter();
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(stream, encoding);
			int size = 0;
			int next = reader.read();
			boolean first = true;
			while (next >= 0) {
				if (first && next == UTF8_BOM.charAt(0)) {
					// skip
				} else {
					writer.write(next);
				}
				next = reader.read();
				if (size > maxSize) {
					throw new BotException("File size limit exceeded: " + size + " > " + maxSize + " token: " + next);
				}
				size++;
			}
		} catch (IOException exception) {
			throw new BotException("IO Error: " + exception.getMessage(), exception);
		} finally {
			if (reader != null && finish) {
				try {
					reader.close();
				} catch (IOException ignore) {}
			}
			if (stream != null && finish) {
				try {
					stream.close();
				} catch (IOException ignore) {}
			}
		}
		return writer.toString();
	}
	public static <T> T random(Collection<T> collection) {
		if ((collection == null) || collection.isEmpty()) {
			return null;
		}
		int value = random().nextInt(collection.size());
		int index = 0;
		for (T element : collection) {
			if (index == value) {
				return element;				
			}
			index++;
		}
		return null;
	}
}
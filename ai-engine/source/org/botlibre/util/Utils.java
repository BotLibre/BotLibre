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
package org.botlibre.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.persistence.internal.helper.Helper;
import org.owasp.encoder.Encode;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.botlibre.BotException;

/**
 * Helper utility class.
 */
public class Utils {
	public static long MINUTE = 60 * 1000;
	public static long HOUR = 60 * 60 * 1000;
	public static long DAY = 24 * 60 * 60 * 1000;
	public static int URL_TIMEOUT = 5000;
	public static String KEY = "changethis";
	
	public static Map<String, String> profanityMap = new HashMap<String, String>();
	
	public static ThreadLocal<Random> random = new ThreadLocal<Random>();
	
	public static DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
	public static PolicyFactory sanitizer;
	
	static {
		profanityMap.put("fuck", "frig");
		profanityMap.put("fuckk", "frig");
		profanityMap.put("f***", "frig");
		profanityMap.put("fucker", "hoser");
		profanityMap.put("fucked", "frigged");
		profanityMap.put("fuckin", "frigging");
		profanityMap.put("fucking", "frigging");
		profanityMap.put("fuking", "frigging");
		profanityMap.put("motherfucking", "frigging");
		profanityMap.put("motherfuckin", "frigging");
		profanityMap.put("fuck's", "frigs");
		profanityMap.put("fucks", "frigs");
		profanityMap.put("fuc", "frig");
		profanityMap.put("bitch", "girl");
		profanityMap.put("b****", "girl");
		profanityMap.put("asshole", "bum");
		profanityMap.put("dumbass", "idiot");
		profanityMap.put("pussy", "kitty");
		profanityMap.put("pussyy", "kitty");
		profanityMap.put("cunt", "privates");
		profanityMap.put("clit", "privates");
		profanityMap.put("vagina", "privates");
		//profanityMap.put("crotch", "privates");
		profanityMap.put("dick", "privates");
		profanityMap.put("cock", "privates");
		profanityMap.put("penis", "privates");
		//profanityMap.put("kiss", "smooch");
		//profanityMap.put("kisses", "smooches");
		profanityMap.put("boobs", "privates");
		profanityMap.put("titties", "privates");
		profanityMap.put("nipples", "privates");
		profanityMap.put("nipple", "privates");
		profanityMap.put("titts", "privates");
		profanityMap.put("tits", "privates");
		profanityMap.put("tit", "privates");
		profanityMap.put("whore", "harlot");
		profanityMap.put("shit", "poop");
		profanityMap.put("holyshit", "poop");
		profanityMap.put("crap", "poop");
		profanityMap.put("sh*t", "poop");
		profanityMap.put("bullshit", "bull poop");
		profanityMap.put("dammit", "darnit");
		profanityMap.put("damnit", "darnit");
		profanityMap.put("nigga", "african");
		profanityMap.put("nigger", "african");
		profanityMap.put("cum", "come");
		profanityMap.put("horny", "happy");
		profanityMap.put("masterbate", "play");
		profanityMap.put("masterbated", "played");
		profanityMap.put("masterbating", "playing");
		profanityMap.put("testicles", "privates");
		profanityMap.put("testicle", "privates");
	}
	
	public static Pattern httpRegex = Pattern.compile("\\b(?:https?|ftp|file):\\/\\/[a-z0-9-+&@#\\/%?=~_|!:,.;]*[a-z0-9-+&@#\\/%=~_|]", Pattern.CASE_INSENSITIVE);
	public static Pattern wwwRegex = Pattern.compile("((www\\.)[^\\s]+)", Pattern.CASE_INSENSITIVE);
	public static Pattern emailRegex = Pattern.compile("(([a-zA-Z0-9_\\-\\.]+)@[a-zA-Z_]+?(?:\\.[a-zA-Z]{2,6}))+", Pattern.CASE_INSENSITIVE);
	
	public static Random random() {
		Random value = random.get();
		if (value == null) {
			value = new Random();
			random.set(value);
		}
		return value;
	}
	
	public static PolicyFactory sanitizer() {
		if (sanitizer == null) {
			sanitizer = Sanitizers.FORMATTING.and(Sanitizers.LINKS).and(Sanitizers.BLOCKS).and(Sanitizers.IMAGES).and(Sanitizers.STYLES);
			PolicyFactory html = new HtmlPolicyBuilder()
				.allowElements("table", "tr", "td", "thead", "tbody", "th", "font")
				.allowAttributes("color").globally()
				.allowAttributes("bgcolor").globally()
				.allowAttributes("align").globally()
				.toFactory();
			sanitizer = sanitizer.and(html);
		}
		return sanitizer;
	}
	
	public static boolean checkMaxMemory() {
		return Runtime.getRuntime().totalMemory() >= Runtime.getRuntime().maxMemory();
	}
			
	public static boolean checkLowMemory() {
		return checkLowMemory(0.2);
	}
	
	public static boolean checkLowMemory(double ratio) {
		return (Runtime.getRuntime().totalMemory() >= Runtime.getRuntime().maxMemory())
				&& (Runtime.getRuntime().freeMemory() < (Runtime.getRuntime().maxMemory() * ratio));
	}
	
	public static String encodeURL(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (Exception exception) {
			return "";
		}		
	}
	
	public static String decodeURL(String url) {
		try {
			return URLDecoder.decode(url, "UTF-8");
		} catch (Exception exception) {
			return "";
		}		
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
	
	/**
	 * Check if the text contains any profanity.
	 */
	public static boolean isProfanity(String text) {
		if ((text == null) || text.isEmpty()) {
			return false;
		}
		text = text.toLowerCase();
		for (String profanity : profanityMap.keySet()) {
			// Ignore short words, as they may be part of other real word.
			if ((profanity.length() > 3) && (text.indexOf(profanity) != -1)) {
				return true;
			}
		}
		return checkProfanity(text);
	}
	
	/**
	 * If the word is profanity, map it to something less offensive.
	 */
	public static String mapProfanity(String word) {
		String mapping = profanityMap.get(word.toLowerCase());
		if (mapping != null) {
			return mapping;
		}
		return word;
	}
	
	/**
	 * If the word is profanity, map it to something less offensive.
	 */
	public static String translateProfanity(String text) {
		if ((text == null) || text.isEmpty()) {
			return text;
		}
		String lowerText = text.toLowerCase();
		String translation = text;
		for (String profanity : profanityMap.keySet()) {
			if (lowerText.indexOf(profanity) != -1) {
				StringWriter writer = new StringWriter();
				TextStream stream = new TextStream(text);
				while (!stream.atEnd()) {
					writer.write(stream.nextWhitespace());
					String word = stream.nextWord();
					if (word != null) {
						writer.write(mapProfanity(word));
					}
				}
				return writer.toString();
			}
		}
		return translation;
	}
	
	/**
	 * Strip the html tags from the text.
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
	
	public static List<String> csv(String csv) {
		return new TextStream(csv).csv();
	}
	
	/**
	 * Escape HTML elements.
	 */
	public static String escapeHTML(String html) {
		/*if (html == null) {
			return "";
		}
		if ((html.indexOf('<') == -1) && (html.indexOf('>') == -1)) {
			return html;
		}
		html = html.replace("<", "&lt;");
		html = html.replace(">", "&gt;");
		return html;*/
		return Encode.forHtml(html);
	}
	
	/**
	 * Escape quotes using \".
	 */
	public static String escapeQuotes(String text) {
		if (text == null) {
			return "";
		}
		if (text.indexOf('"') == -1) {
			return text;
		}
		text = text.replace("\"", "&quot;");
		return text;
	}
	
	/**
	 * Remove cr.
	 */
	public static String removeCRs(String text) {
		if (text == null) {
			return "";
		}
		if ((text.indexOf("\n") == -1) && (text.indexOf("\r") == -1) && (text.indexOf("\f") == -1)) {
			return text;
		}
		text = text.replace("\n", "");
		text = text.replace("\r", "");
		text = text.replace("\f", "");
		return text;
	}
	
	/**
	 * If the word is profanity, map it to something less offensive.
	 */
	public static boolean checkProfanity(String text) {
		if ((text == null) || text.isEmpty()) {
			return false;
		}
		String lowerText = text.toLowerCase();
		for (String profanity : profanityMap.keySet()) {
			if (lowerText.indexOf(profanity) != -1) {
				TextStream stream = new TextStream(lowerText);
				while (!stream.atEnd()) {
					String word = stream.nextWord();
					if (word != null) {
						if (profanityMap.containsKey(word)) {
							return true;
						}
					}
				}
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Check if the text contains a script.
	 */
	public static void checkScript(String text) {
		if (containsScript(text)) {
			throw new BotException("For security reasons, script and iframe tags are not allowed");
		}
	}
	
	/**
	 * Check if the text contains a script.
	 */
	public static boolean containsScript(String text) {
		if ((text == null) || text.isEmpty()) {
			return false;
		}
		text = text.toLowerCase();
		if (text.indexOf("<script") != -1) {
			return true;
		}
		if (text.indexOf("<iframe") != -1) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check if the text contains a HTML.
	 */
	public static void checkHTML(String text) {
		if (containsHTML(text)) {
			throw new BotException("HTML tag characters are not allowed");
		}
	}
	
	/**
	 * Check if the text contains HTML tags.
	 */
	public static boolean containsHTML(String text) {
		if ((text == null) || text.isEmpty()) {
			return false;
		}
		return (text.indexOf('<') != -1) || (text.indexOf('>') != -1);
	}
	
	/**
	 * Ensure the string is a valid url.
	 */
	public static String checkURL(String url) {
		if (url == null || url.isEmpty()) {
			return url;
		}
		if (!url.startsWith("http")) {
			url = "http://" + url;
		}
		return url;
	}
	
	
	public static String linkHTML(String text) {
		if (text == null || text.isEmpty()) {
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
		    	if (url.indexOf(".png") != -1 || url.indexOf(".jpg") != -1 || url.indexOf(".jpeg") != -1 || url.indexOf(".gif") != -1
		    			|| url.indexOf(".PNG") != -1 || url.indexOf(".JPG") != -1 || url.indexOf(".JPEG") != -1 || url.indexOf(".GIF") != -1) {
		    		url = "<a href='" + url + "' target='_blank'><img src='" + url + "' height='50'></a>";
		    	} else if (url.indexOf(".mp4") != -1 || url.indexOf(".webm") != -1 || url.indexOf(".ogg") != -1
		    			|| url.indexOf(".MP4") != -1 || url.indexOf(".WEBM") != -1 || url.indexOf(".OGG") != -1) {
		    		url = "<a href='" + url + "' target='_blank'><video src='" + url + "' height='50'></a>";
		    	} else if (url.indexOf(".wav") != -1 || url.indexOf(".mp3") != -1
		    			|| url.indexOf(".WAV") != -1 || url.indexOf(".MP3") != -1) {
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
	    
	    // http://, https://, ftp://
	    //var urlPattern = /\b(?:https?|ftp):\/\/[a-z0-9-+&@#\/%?=~_|!:,.;]*[a-z0-9-+&@#\/%=~_|]/gim;

	    // www. 
	    // var wwwPattern = /(^|[^\/])(www\.[\S]+(\b|$))/gim;

	    // name@domain.com
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
	 * Tokenize the sentence into its words.
	 */
	public static List<String> getWords(String text) {
		List<String> words = new ArrayList<String>();
		TextStream stream = new TextStream(text);
		while (!stream.atEnd()) {
			String word = stream.nextWord();
			if (word != null) {
				words.add(word);
			}
		}
		return words;
	}
	
	public static Element parseXML(String xml) {
		try {
			DocumentBuilder parser = xmlFactory.newDocumentBuilder();
			StringReader reader = new StringReader(xml);
			InputSource source = new InputSource(reader);
			Document document = parser.parse(source);
			return document.getDocumentElement();
		} catch (Exception exception) {
			return null;
		}
	}

	/**
	 * Parse the date of the form, "yyyy-MM-dd".
	 */
	public static java.sql.Date parseDate(String value) {
		return Helper.dateFromString(value);
	}

	/**
	 * Parse the time of the form, "HH:mm:ss.N".
	 */
	public static Time parseTime(String value) {
		return Helper.timeFromString(value);
	}

	/**
	 * Parse the date of the form, "yyyy-MM-dd HH:mm:ss.N".
	 */
	public static Timestamp parseTimestamp(String value) {
		return Helper.timestampFromString(value);
	}

    public static String buildZeroPrefixAndTruncTrailZeros(int number, int totalDigits) {
        String zeros = "000000000";
        String numbString = Integer.toString(number);
        numbString = zeros.substring(0, (totalDigits - numbString.length())) + numbString;
        char[] numbChar = new char[numbString.length()];
        numbString.getChars(0, numbString.length(), numbChar, 0);
        int truncIndex = totalDigits - 1;
        while (numbChar[truncIndex] == '0') {
            truncIndex--;
        }
        return new String(numbChar, 0, truncIndex + 1);
    }
	
	/**
	 * Parse the date of the format.
	 */
	public static Calendar parseDate(String value, String format) throws ParseException {
		Calendar date = Calendar.getInstance();
		date.setTime(new SimpleDateFormat(format).parse(value));
		return date;
	}

	/**
	 * Print the date in the form, "yyyy-MM-dd HH:mm:ss.S".
	 */
	public static String printDate(Calendar date) {
		if (date == null) {
			return "";
		}
		return Helper.printCalendar(date);
	}

	/**
	 * Print the time in the format.
	 */
	public static String printTime(Time time, String format) {
		if (time == null) {
			return "";
		}

		return new SimpleDateFormat(format).format(time);
	}

	/**
	 * Print the time in the format.
	 */
	public static String printDate(Date date, String format) {
		if (date == null) {
			return "";
		}

		return new SimpleDateFormat(format).format(date);
	}

	/**
	 * Print the date in the form, "yyyy-MM-dd HH:mm:ss.N".
	 */
	public static String printTimestamp(Timestamp timestamp) {
		if (timestamp == null) {
			return "";
		}
		return Helper.printTimestamp(timestamp);
	}

	/**
	 * Print the date in the form, "yyyy-MM-dd HH:mm:ss.N".
	 */
	public static String printDate(Date date) {
		if (date == null) {
			return "";
		}
		return Helper.printTimestamp(new Timestamp(date.getTime()));
	}
	
	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ignore) {
			ignore.printStackTrace();
		}
	}
	
	/**
	 * Compress the text to be a proper identifier within the size limit.
	 * Replace space with '-' and remove any non alpha numerics.
	 */
	public static String compress(String text, int size) {
		TextStream stream = new TextStream(text);
		StringWriter writer = new StringWriter(text.length());
		int count = 0;
		while (!stream.atEnd()) {
			if (count >= size) {
				break;
			}
			char next = stream.next();
			if (Character.isLetter(next) || Character.isDigit(next) || (next == '_')) {
				writer.write(next);
			} else {
				writer.write('_');				
			}
			count++;
		}
		return writer.toString();
	}
	
	/**
	 * Truncate the string.
	 */
	public static String truncate(String text, int size) {
		if (text.length() <= size) {
			return text;
		}
		return text.substring(0, size);
	}
	
	public static double truncate(double value) {
		return Math.round(value * 100) / 100d;
	}
	
	/**
	 * Get the contents of the stream to a .self file and parse it.
	 */
	public static String loadTextFile(InputStream stream, String encoding, int maxSize) {
		if (encoding.trim().isEmpty()) {
			encoding = "UTF-8";
		}
		StringWriter writer = new StringWriter();
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(stream, encoding);
			int size = 0;
			int next = reader.read();
			while (next >= 0) {
				writer.write(next);
				next = reader.read();
				if (size > maxSize) {
					throw new BotException("File size limit exceeded: " + size + " > " + maxSize + " token: " + next);
				}
				size++;
			}
		} catch (IOException exception) {
			throw new BotException("IO Error", exception);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignore) {}
			}
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException ignore) {}
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
	
	/**
	 * Reduce the sentence to a simple form.
	 */
	public static String reduce(String sentence) {
		if (sentence.length() == 0) {
			return sentence;
		}
		int terminate = sentence.length();
		while ((terminate > 0) && TextStream.TERMINATORS.indexOf(sentence.charAt(terminate - 1)) != -1) {
			terminate--;
		}
		StringWriter writer = new StringWriter();
		TextStream stream = new TextStream(sentence);
		boolean first = true;
		boolean ignore = false;
		String previous = null;
		while (stream.getPosition() < terminate) {
			String word = stream.nextWord();
			if (word == null) {
				break;
			}
			word = word.toLowerCase();
			if (word.equals("'") && "what".equals(previous)) {
				if ("s".equals(stream.peekWord())) {
					writer.write(" is");
					stream.nextWord();
					continue;
				}
			}
			if (!first && !ignore) {
				writer.write(" ");
			} else {
				first = false;
			}
			if (word.equals("whats")) {
				writer.write("what is");
			} else if (!TextStream.IGNORABLE.contains(word)) {
				writer.write(word);
				ignore = false;
			} else {
				ignore = true;
			}
			previous = word;
		}
		return writer.toString();
	}
	
	/**
	 * Return if the string only contains English characters.
	 */
	public static boolean isEnglish(String word) {
		if (word.length() == 0) {
			return false;
		}
		for (int index = 0; index < word.length(); index++) {
			char next = word.charAt(index);
			if (!((next >= 'a' && next <= 'z') || (next >= 'A' && next <= 'Z')
					|| (next == ' ') || (next == '-') || (next == '\''))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Check if a capitalized word.
	 */
	public static boolean isCapitalized(String text) {
		if (text.isEmpty()) {
			return false;
		}
		boolean isCaps = Character.isUpperCase(text.charAt(0));
		if (!isCaps) {
			return false;
		}
		if (text.length() == 1) {
			return isCaps;
		}
		return !isCaps(text);
	}
	
	/**
	 * Check if the text is all upper case.
	 */
	public static boolean isCaps(String text) {
		boolean hasCaps = false;
		for (int index = 0; index < text.length(); index++) {
			char character = text.charAt(index);
			if (Character.isLetter(character)) {
				if (!Character.isUpperCase(character)) {
					return false;
				}
				hasCaps = true;
			}
		}
		return hasCaps;
	}
	
	public static boolean isAlphaNumeric(String text) {
		TextStream stream = new TextStream(text);
		stream.skipToAny("!@#$%^&*()+={}[]|\'\" \t\n`~<>?/:;");
		if (!stream.atEnd()) {
			return false;
		}
		return true;
	}
	
	public static String encrypt(String key, String password) {
		try {
			// Encrypt the password using the key.
			String passphrase = key;
			MessageDigest digest = MessageDigest.getInstance("SHA");
			digest.update(passphrase.getBytes());
			SecretKeySpec secret = new SecretKeySpec(digest.digest(), 0, 16, "AES");
			Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
			aes.init(Cipher.ENCRYPT_MODE, secret);
			byte[] ciphertext = aes.doFinal(password.getBytes());
			return bytesToHex(ciphertext);
		} catch (Exception failed) {
			return null;
		}		
	}
	
	public static String decrypt(String key, String ciphertext) {
		try {
			// Encrypt the password using the key.
			String passphrase = key;
			MessageDigest digest = MessageDigest.getInstance("SHA");
			digest.update(passphrase.getBytes());
			SecretKeySpec secret = new SecretKeySpec(digest.digest(), 0, 16, "AES");
			Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
			aes.init(Cipher.DECRYPT_MODE, secret);
			byte[] password = aes.doFinal(hexToBytes(ciphertext));
			return new String(password);
		} catch (Exception failed) {
			return null;
		}		
	}
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static byte[] hexToBytes(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	private final static char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
	
	private static int[]  toInt   = new int[128];
	
	static {
	    for(int i=0; i< ALPHABET.length; i++){
	        toInt[ALPHABET[i]]= i;
	    }
	}
	
	public static String encodeBase64(byte[] buf){
	    int size = buf.length;
	    char[] ar = new char[((size + 2) / 3) * 4];
	    int a = 0;
	    int i=0;
	    while(i < size){
	        byte b0 = buf[i++];
	        byte b1 = (i < size) ? buf[i++] : 0;
	        byte b2 = (i < size) ? buf[i++] : 0;
	
	        int mask = 0x3F;
	        ar[a++] = ALPHABET[(b0 >> 2) & mask];
	        ar[a++] = ALPHABET[((b0 << 4) | ((b1 & 0xFF) >> 4)) & mask];
	        ar[a++] = ALPHABET[((b1 << 2) | ((b2 & 0xFF) >> 6)) & mask];
	        ar[a++] = ALPHABET[b2 & mask];
	    }
	    switch(size % 3){
	        case 1: ar[--a]  = '=';
	        case 2: ar[--a]  = '=';
	    }
	    return new String(ar);
	}
	
   public static byte[] decodeBase64(String s){
        int delta = s.endsWith( "==" ) ? 2 : s.endsWith( "=" ) ? 1 : 0;
        byte[] buffer = new byte[s.length()*3/4 - delta];
        int mask = 0xFF;
        int index = 0;
        for(int i=0; i< s.length(); i+=4){
            int c0 = toInt[s.charAt( i )];
            int c1 = toInt[s.charAt( i + 1)];
            buffer[index++]= (byte)(((c0 << 2) | (c1 >> 4)) & mask);
            if(index >= buffer.length){
                return buffer;
            }
            int c2 = toInt[s.charAt( i + 2)];
            buffer[index++]= (byte)(((c1 << 4) | (c2 >> 2)) & mask);
            if(index >= buffer.length){
                return buffer;
            }
            int c3 = toInt[s.charAt( i + 3 )];
            buffer[index++]= (byte)(((c2 << 6) | c3) & mask);
        }
        return buffer;
    }
	
	public static InputStream openStream(URL url) throws IOException {
	    return openStream(url, URL_TIMEOUT);
	}
	
	public static InputStream openStream(URL url, int timeout) throws IOException {
	    URLConnection connection = url.openConnection();
	    connection.setConnectTimeout(timeout);
	    connection.setReadTimeout(timeout);
	    return connection.getInputStream();
	}
	
	public static byte[] createThumb(byte[] image, int size) {
		try {
			BufferedImage source = ImageIO.read(new ByteArrayInputStream(image));
			if (source == null) {
				return null;
			}			
			float height = source.getHeight();
			float width = source.getWidth();
			float max = size;
			if (height <= size && width <= size) {
				return image;
			}
			if (height > width) {
				width = max * (width / height);
				height = size;
			} else {
				height = max * (height / width);
				width = max;
			}
			Image scaled = source.getScaledInstance((int)width, (int)height, BufferedImage.SCALE_SMOOTH);
			BufferedImage thumb = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_RGB);
			thumb.createGraphics().drawImage(scaled, 0, 0, thumb.getWidth(), thumb.getHeight(), Color.WHITE, null);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ImageIO.write(thumb, "jpg", output);
			return output.toByteArray();
		} catch (IOException exception) {
			exception.printStackTrace();
			return null;
		}
	}
	
	public static int[] getDimensions(byte[] image) {
		try {
			BufferedImage source = ImageIO.read(new ByteArrayInputStream(image));
			if (source == null) {
				return null;
			}
			int[] dimensions = new int[2];
			dimensions[1] = source.getHeight();
			dimensions[0] = source.getWidth();
			return dimensions;
		} catch (IOException exception) {
			exception.printStackTrace();
			return null;
		}
	}
}

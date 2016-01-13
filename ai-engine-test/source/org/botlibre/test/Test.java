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
package org.botlibre.test;

import java.io.File;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.ParseException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.botlibre.Bot;
import org.botlibre.aiml.AIMLParser;
import org.botlibre.api.knowledge.Memory;
import org.botlibre.api.knowledge.Network;
import org.botlibre.knowledge.BasicNetwork;
import org.botlibre.knowledge.serialized.SerializedMemory;
import org.botlibre.knowledge.xml.NetworkXMLParser;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Used to test the system.
 */

public class Test {

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		try {
			test();
			//testJDBC();
			//testEscapeHTML();
			//testFBDate();
			//System.out.println(java.text.NumberFormat.getInstance().parse("1000,200,300,400,500,600,700,800,900e27"));
			//System.out.println(new java.sql.Date(System.currentTimeMillis() - (Utils.DAY * 30)));
			//testStrip();
			//testQuotes();
			//testReduce();
			//testAIML();
			//testEncrypt();
			//testParseDate();
			//testStream();
			//listAllVoices();
			//testVoice();
			//testVoiceRec();
			//testNetworkXMLParser();
			//testSerializedMemory();
			//testTextEntry();
			//testXML();
			//testBot();
			//testSentence();
			//testTweet();
			//testLinkHTML();
		} catch (Throwable error) {
			error.printStackTrace();
		}
		System.out.println("Time: " + (System.currentTimeMillis() - start));
	}
	
	public static void test() {
		System.out.println((System.nanoTime() % 1000000) * 1000);
	}
	
	public static void testJDBC() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("user", "postgres");
		properties.setProperty("password", "password");
		Connection connection = DriverManager.getConnection("jdbc:postgresql:botlibre_bots?currentSchema=foo", properties);
		//connection.createStatement().executeUpdate("CREATE EXTENSION dblink");
		connection.createStatement().execute("select public.dblink_connect('dbconnection','dbname=test user=postgres password=password')");
	}
	
	public static void testEscapeHTML() throws Exception {
		System.out.println(org.owasp.encoder.Encode.forJavaScriptAttribute("I'm a dog"));
	}
	
	public static void testFBDate() throws Exception {
		System.out.println(Utils.parseDate("2015-08-13T19:39:38+0000", "yyyy-MM-dd'T'HH:mm:ssX").getTime());
	}
	
	public static void testLowMemory() {
		for (int i = 0; i < 100000; i++) {
			Utils.checkLowMemory();
		}
	}
	
	public static void testBaseline() {
		for (int i = 0; i < 100000; i++) {
			Utils.csv("hello, world");
		}
	}
	
	public static void testTweet() {
		testTweet("short");
		testTweet("1 1234567890"
				+ "2 1234567890"
				+ "3 1234567890"
				+ "4 1234567890"
				+ "5 1234567890"
				+ "6 1234567890"
				+ "7 1234567890"
				+ "8 1234567890"
				+ "9 1234567890"
				+ "10 1234567890"
				+ "11 1234567890"
				+ "12 1234567890"
				+ "13 1234567890"
				+ "14 1234567890"
				);
		testTweet("short http://www.xyz.com");
		testTweet("1 1234567890"
				+ "2 1234567890"
				+ "3 1234567890"
				+ "4 1234567890"
				+ "5 1234567890"
				+ "6 1234567890"
				+ "7 1234567890"
				+ "8 1234567890"
				+ "9 1234567890"
				+ "10 1234567890"
				+ "11 1234567890"
				+ "12 1234567890"
				+ "13 1234567890"
				+ "14 1234567890"
				+ " http://www.xyz.com"
				);
		testTweet("1 1234567890"
				+ "2 1234567890"
				+ "3 1234567890"
				+ "4 1234567890"
				+ "5 1234567890"
				+ "6 1234567890"
				+ "7 1234567890"
				+ "8 1234567890"
				+ "9 1234567890"
				+ "10 1234567890"
				+ "11 1234567890"
				+ "12 1234567890"
				+ "13 1234567890"
				+ "14 1234567890"
				+ " http://www.xyz.com"
				+ "10 1234567890"
				+ "11 1234567890"
				+ "12 1234567890"
				+ "13 1234567890"
				+ "14 1234567890"
				);
	}
	
	public static void testTweet(String text) {
		if (text.length() > 140) {
			int index =  text.indexOf("http://");
			if (index == -1) {
				text = text.substring(0, 140);
			} else if (index > 120) {
				text = text.substring(0, 120) + " " + text.substring(index, text.length());
			}
		}
		System.out.println(text);
		System.out.println(text.length());
	}
	
	public static void testLinkHTML() throws ParseException {
		System.out.println(Utils.linkHTML("http nothing @ foo@ ftp: www blah"));
		System.out.println(Utils.linkHTML("http://www.foo.com me@foo.com http://www.x.y/img.png http://www.x.y/vid.ogg"));
		System.out.println(Utils.linkHTML("www.foo.com me@foo.com www.x.y/vid.ogg"));
	}
	
	public static void testQuotes() throws ParseException {
		TextStream stream = new TextStream("\"hello\"\"there\" xxx");
		System.out.println(stream.nextQuotesExcludeDoubleQuote());
		System.out.println(stream.nextQuotes());
		System.out.println(stream.nextQuotes());
	}

	public static void testStrip() throws ParseException {
		System.out.println(Utils.stripTags("<html><body><span style=\"font-family:Verdana; color:#000000; font-size:10pt;\"><div>2+2? 8*8?</div><div>1+1?</div><div>Hi<br></div></span></body></html>"));
	}
	
	public static void testReduce() throws ParseException {
		System.out.println(Utils.reduce("What's U-P 'you?'\t\n\r     (5.5+7-9)/8 \"eh\" a,b,c???"));		
	}
	
	public static void testAIML() throws ParseException {
		AIMLParser.parser().parseAIML(new File("C:/Projects/Paphus/BOTlibre/BOTlibre/source/com/paphus/botlibre/scripts/aiml/ai.aiml"), false, false, false, false, null, "", null);
	}
	
	public static void testParseDate() throws ParseException {
		System.out.println(Utils.parseDate("2014-01-19T21:09:00+09:00", "yyyy-MM-dd'T'HH:mm:ssX"));		
	}

	public static void testEncrypt() {
		System.out.println(Utils.encrypt("foo", "bar"));
		System.out.println(Utils.decrypt("foo", Utils.encrypt("foo", "bar")));
	}
	
	/**
	 * Test the TextStream API.
	 */
	public static void testStream() {
		TextStream stream = new TextStream("a b c.v @foo joe@foo.com joe@foo.com. joe@foo.com,joe@foo.com james108@foo.com http://www.w.com. http://www.w.com, \"http://www.w.com\" 'http://www.w.com' www.foo.com");
		while (!stream.atEnd()) {
			System.out.println(stream.nextWord());
		}
		stream = new TextStream("hello world");
		stream.upToAny("abcd");
		stream.skip();
		stream.upToAny("abcd");
		while (!stream.atEnd()) {
			System.out.println(stream.nextWord());
		}
		stream = new TextStream("\"hello\", world.");
		while (!stream.atEnd()) {
			System.out.println(stream.nextWord());
		}
		stream = new TextStream("how are you\r\n\rhi\n\n.");
		while (!stream.atEnd()) {
			System.out.println(stream.nextLine());
		}
		stream = new TextStream("hello mr.");
		stream.skipTo('.', false);
		System.out.println(stream.peekPreviousWord());
		stream = new TextStream("hello mr.");
		stream.skipTo('.', true);
		System.out.println(stream.peekPreviousWord());
		stream = new TextStream("hello mr. fuz.zy A. A! a. bones. whats up? a... not!!!");
		while (!stream.atEnd()) {
			System.out.println(stream.nextSentence());
		}
		stream = new TextStream("hello mr. fuz.zy A. A! 22.2 33,333 -3 + +4 3-4+5. a. http://www.xxx.yyy bo_nes. wh-ats up? a... not!!!");
		while (!stream.atEnd()) {
			System.out.println(stream.nextSentence());
		}
		stream = new TextStream("Ã°Â¡Â¨Â¸Ã¦Â¼Â¢Ã£ï¿½Å¸Ã¥â‚¬Â±Ã­â€¢Å“Ã¬Å¾ï¿½Ã¦Â¼Â¢Ã¥Â­â€”Ã¦Â±â€°Ã¥Â­â€”Ã¦Â¼Â¢Ã¥Â­â€”Ã£ï¿½â€žÃ¦â€“Â¹Ã£ï¿½ÂªÃ£ï¿½Â©Ã£ï¿½Â®Ã£ï¿½Å¸Ã£â€šï¿½Ã£ï¿½Â§Ã£ï¿½â„¢Ã£â‚¬â€š ojca_ojcc@yahoo.caÃ£ï¿½Â§Ã£ï¿½â€�Ã¦Â³Â¨Ã¦â€“â€¡Ã£ï¿½ï¿½Ã£ï¿½Â Ã£ï¿½â€¢Ã£ï¿½â€žÃ£â‚¬â€šÃ¯Â¼â€˜Ã§Å¡Â¿6Ã£Æ’â€°Ã£Æ’Â«Ã£ï¿½Â§Ã£ï¿½â„¢Ã£â‚¬â€š Ã¦Â³Â¨Ã¦â€“â€¡Ã£ï¿½â€”Ã£ï¿½Å¸Ã£â€šâ€šÃ£ï¿½Â¡Ã£ï¿½Â¯Ã£â‚¬ï¿½Ã£â€šâ€šÃ£ï¿½Â¡Ã£ï¿½Â¤Ã£ï¿½ï¿½Ã¥Â¤Â§Ã¤Â¼Å¡");
		while (!stream.atEnd()) {
			System.out.println(stream.nextSentence());
		}
		stream = new TextStream("-1.0 + -1. a+b 2b+2c a-a a_a 1_1 1-2 a.a _aa +1.2_2 ac_22 a123_22 aa__aa --aa --2 +1");
		while (!stream.atEnd()) {
			System.out.println(stream.nextWord());
		}
	}
	
	/**
	 * Test sentence processing.
	 */
	public static void testSentence() {
		System.out.println(Utils.getWords("Hello what is 1 + (2*3) http://www.foo/bluee? 123 hey"));
		System.out.println(Utils.getWords("Hello what is 1 + (2*3) http://www.foo/bluee?	f"));
		System.out.println(Utils.getWords("Hello what is 1 + (2*3) http://www.foo/bluee?"));
		String a = new TextStream("abc").next(2);
		a = new TextStream("abc").next(1);
		a = new TextStream("abc").next(3);
		a = new TextStream("abc").next(4);
		a = new TextStream("abc").peek(1);
		a = new TextStream("abc").peek(2);
		a = new TextStream("abc").peek(3);
		a = new TextStream("abc").peek(4);
		TextStream s = new TextStream("abc");
		s.skip(4);
		a = s.upToEnd();
		s = new TextStream("abc");
		s.skip(1);
		a = s.upToEnd();
		s = new TextStream("abc");
		s.skip(2);
		a = s.upToEnd();
		s = new TextStream("abc");
		s.skip(0);
		a = s.upToEnd();
		System.out.println(a);
	}
	
	/**
	 * Store and restore a network through serialization.
	 */
	public static void testSerializedMemory() {
		Memory memory = new SerializedMemory();
		//Network network = memory.getLongTermMemory();
		
		//bootstrapNetwork(network);
		
		memory.save();
		System.out.println(memory.getLongTermMemory());
		
		memory = new SerializedMemory();
		memory.restore();
		System.out.println(memory.getLongTermMemory());
	}
	
	/**
	 * Store and restore a network through xml.
	 */
	public static void testNetworkXMLParser() {
		Network network = new BasicNetwork();
		
		//bootstrapNetwork(network);

		File file = new File("test.xml");
		NetworkXMLParser.instance().toXML(network, file);
		System.out.println(network);
		System.out.println(NetworkXMLParser.instance().toXML(network));
		
		Network parsedNetwork = NetworkXMLParser.instance().parse(file);
		System.out.println(parsedNetwork);
	}
	
	public static void testTextEntry() {
		TextEntry entry = new TextEntry();
		entry.setWriter(new OutputStreamWriter(System.out));
		entry.input("hello");
	}
		
	public static void testXML() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			URL url = Bot.class.getResource("config.xml");
			Document document = parser.parse(url.toString());      
			System.out.println(document);
			Element root = document.getDocumentElement();
			System.out.println(root);
			System.out.println(root.getElementsByTagName("mind"));
			System.out.println(root.getElementsByTagName("memory"));
      
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	
	public static void testBot() {
		Bot bot = Bot.createInstance();
		System.out.println(bot);
		bot.shutdown();
	}

}


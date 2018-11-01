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
package org.botlibre.test;

import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

/**
 * Test Self scripting and API.
 */

public class TestUtils extends TextTest {

	@org.junit.Test
	public void testTextStream() {
		TextStream stream = new TextStream("Here's the story of a lovely lady Who was bringing up three very lovely girls. All of them had hair of gold, like their mother, The youngest one in curls. Here's the story, of a man named Brady, "
    	        + "Who was busy with three boys of his own, They were four men, living all together, Yet they were all alone. Till the one day when the lady met this fellow And they knew it was much more than a hunch, That this group must somehow form a family. "
    	        + "That's the way we all became the Brady Bunch. The Brady Bunch, The Brady Bunch, That's the way we became the Brady Bunch. ");
		String next = stream.nextParagraph(200);
		checkResponse(next, "Here's the story of a lovely lady Who was bringing up three very lovely girls. All of them had hair of gold, like their mother, The youngest one in curls.");
		next = stream.nextParagraph(200);
		checkResponse(next, "Here's the story, of a man named Brady, Who was busy with three boys of his own, They were four men, living all together, Yet they were all alone.");
		next = stream.nextParagraph(200);
		checkResponse(next, "Till the one day when the lady met this fellow And they knew it was much more than a hunch, That this group must somehow form a family. That's the way we all became the Brady Bunch.");
		next = stream.nextParagraph(50);
		checkResponse(next, "The Brady Bunch, The Brady Bunch, That's the way w");
		next = stream.nextParagraph(50);
		checkResponse(next, "e became the Brady Bunch. ");
		next = stream.nextParagraph(50);
		if (next != null) {
			fail("Expecting null.");
		}
		stream = new TextStream("\"text\";");
		stream.next();
		next = stream.nextQuotes();
		checkResponse(next, "text");
		stream = new TextStream("\"text\"");
		stream.next();
		next = stream.nextQuotes();
		checkResponse(next, "text");
		stream = new TextStream("\'text\'");
		stream.next();
		next = stream.nextStringQuotes();
		checkResponse(next, "text");
	}

	@org.junit.Test
	public void testStripTags() {
		String test = "This is html <p>yo</p> <p>paragraphs eh <span>test</span> </p> ok";
		String result = "This is html   ok";
		String text = Utils.stripTag(test, "p");
		if (!text.equals(result)) {
			fail(text);
		}
		
		test = "<p> here is a <button>button</button>!</p>";
		result = "<p> here is a !</p>";
		text = Utils.stripTag(test, "button");
		if (!text.equals(result)) {
			fail(text);
		}
		
		test = "Do not say <p class='nospeech'>this</p>";
		result = "Do not say ";
		text = Utils.stripTagClass(test, "nospeech");
		if (!text.equals(result)) {
			fail(text);
		}
		
		test = "Do not say <p class=\"nospeech\">this";
		result = "Do not say ";
		text = Utils.stripTagClass(test, "nospeech");
		if (!text.equals(result)) {
			fail(text);
		}
		
		test = "Do not say <p class=\"nos";
		result = "Do not say <p class=\"nos";
		text = Utils.stripTagClass(test, "nospeech");
		if (!text.equals(result)) {
			fail(text);
		}
		
		test = "Do not say <p >hello</p>";
		result = "Do not say <p >hello</p>";
		text = Utils.stripTagClass(test, "nospeech");
		if (!text.equals(result)) {
			fail(text);
		}
		
		test = "Do not say <button>hello</button>";
		result = "Do not say <button>hello</button>";
		text = Utils.stripTagClass(test, "nospeech");
		if (!text.equals(result)) {
			fail(text);
		}
		
		test = "Do not say <p>hello</p>";
		result = "Do not say <p>hello</p>";
		text = Utils.stripTagClass(test, "nospeech");
		if (!text.equals(result)) {
			fail(text);
		}
		
		test = "Do not say <p>hello <p class=\"nospeech\">world yo</p></p>";
		result = "Do not say <p>hello </p>";
		text = Utils.stripTagClass(test, "nospeech");
		if (!text.equals(result)) {
			fail(text);
		}

		test = "<p>Take a look <a href=\"http://www.test.com/foobar\" target=\"_blank\">Click here</a>.</p>";
		result = "Take a look Click here.";
		String strippedText = Utils.stripTag(test, "button");
		strippedText = Utils.stripTag(test, "select");
		strippedText = Utils.stripTag(test, "script");
		strippedText = Utils.stripTagClass(test, "nospeech");
		strippedText = Utils.stripTags(strippedText);
		if (!strippedText.trim().equals(result)) {
			fail(strippedText);
		}
		
	}
}


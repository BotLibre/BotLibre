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

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.botlibre.Bot;
import org.botlibre.sense.text.TextEntry;

/**
 * Test mathematical processing.
 */

public class TestMath extends TextTest {
	
	@BeforeClass
	public static void setup() throws Exception {
		bootstrap();
	}
	
	/**
	 * Test addition.
	 */
	@org.junit.Test
	public void testWords() throws Exception {
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		loadConsciousness(text, output);
		
		text.input("one plus two");
		String response = waitForOutput(output);
		if (!response.equals("1 + 2 = 3")) {
			fail("incorrect:" + response);			
		}

		loadConsciousness(text, output);
		
		text.input("what is seven divided by eight");
		response = waitForOutput(output);
		if (!response.equals("7 / 8 = 0.875") && !response.equals("7 ÷ 8 = 0.875")
				&& !response.equals("Seven divided 8 = 0.875")) {
			fail("incorrect:" + response);			
		}
		
		loadConsciousness(text, output);
		
		text.input("two to the power of five");
		response = waitForOutput(output);
		if (!response.equals("2^5 = 32") && !response.equals("Two power 5 = 32") && !response.equals("Two power five = 32")) {
			fail("incorrect:" + response);			
		}
		bot.shutdown();
	}
	
	public void loadConsciousness(TextEntry text, List<String> output) {
		text.input("1 + 2 + 3 / 7 * 8^2 + 5");
		waitForOutput(output);
	}
	
	/**
	 * Test addition.
	 */
	@org.junit.Test
	public void testAddition() throws Exception {
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		text.input("1 + 1");
		String response = waitForOutput(output);
		if (!response.equals("1 + 1 = 2") && !response.equals("1 + 1 = two")) {
			fail("incorrect:" + response);			
		}
		text.input("3 + 4?");
		response = waitForOutput(output);
		if (!response.equals("3 + 4 = 7")) {
			fail("incorrect:" + response);			
		}
		text.input("0 + 4?");
		response = waitForOutput(output);
		if (!response.equals("0 + 4 = 4")) {
			fail("incorrect:" + response);			
		}
		text.input("4 + 0?");
		response = waitForOutput(output);
		if (!response.equals("4 + 0 = 4")) {
			fail("incorrect:" + response);			
		}
		text.input("4 + 12?");
		response = waitForOutput(output);
		if (!response.equals("4 + 12 = 16")) {
			fail("incorrect:" + response);			
		}
		text.input("13 + 4?");
		response = waitForOutput(output);
		if (!response.equals("13 + 4 = 17")) {
			fail("incorrect:" + response);			
		}
		text.input("13 + 454?");
		response = waitForOutput(output);
		if (!response.equals("13 + 454 = 467")) {
			fail("incorrect:" + response);			
		}
		text.input("what is 9 + 99?");
		response = waitForOutput(output);
		if (!response.equals("9 + 99 = 108")) {
			fail("incorrect:" + response);			
		}
		text.input("what is 1 + 1 + 2 + 4 + 7?");
		response = waitForOutput(output);
		if (!response.equals("1 + 1 + 2 + 4 + 7 = 15")) {
			fail("incorrect:" + response);			
		}
		bot.shutdown();
	}
	
	/**
	 * Test subtraction.
	 */
	@org.junit.Test
	public void testSubtraction() throws Exception {
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		text.input("1 - 1");
		String response = waitForOutput(output);
		if (!response.equals("1 - 1 = 0")) {
			fail("incorrect:" + response);			
		}
		text.input("3 - 4?");
		response = waitForOutput(output);
		if (!response.equals("3 - 4 = -1")) {
			fail("incorrect:" + response);			
		}
		text.input("0 - 4?");
		response = waitForOutput(output);
		if (!response.equals("0 - 4 = -4")) {
			fail("incorrect:" + response);			
		}
		text.input("4 - 0?");
		response = waitForOutput(output);
		if (!response.equals("4 - 0 = 4")) {
			fail("incorrect:" + response);			
		}
		text.input("4 - 12?");
		response = waitForOutput(output);
		if (!response.equals("4 - 12 = -8")) {
			fail("incorrect:" + response);			
		}
		text.input("13 - 4?");
		response = waitForOutput(output);
		if (!response.equals("13 - 4 = 9")) {
			fail("incorrect:" + response);			
		}
		text.input("13 - 454?");
		response = waitForOutput(output);
		if (!response.equals("13 - 454 = -441")) {
			fail("incorrect:" + response);			
		}
		text.input("what is 9 - 99?");
		response = waitForOutput(output);
		if (!response.equals("9 - 99 = -90")) {
			fail("incorrect:" + response);			
		}
		text.input("what is 1 - 1 - 2 - 4 - 7?");
		response = waitForOutput(output);
		if (!response.equals("1 - 1 - 2 - 4 - 7 = -13")) {
			fail("incorrect:" + response);			
		}
		bot.shutdown();
	}
	
	/**
	 * Test EDMAS.
	 */
	@org.junit.Test
	public void testEDMAS() throws Exception {
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		text.input("1 * 1");
		String response = waitForOutput(output);
		if (!response.equals("1 * 1 = 1")) {
			fail("incorrect:" + response);			
		}
		text.input("3 / 4?");
		response = waitForOutput(output);
		if (!response.equals("3 / 4 = 0.75")) {
			fail("incorrect:" + response);			
		}
		text.input("0 / 4?");
		response = waitForOutput(output);
		if (!response.equals("0 / 4 = 0")) {
			fail("incorrect:" + response);			
		}
		text.input("4 * 0?");
		response = waitForOutput(output);
		if (!response.equals("4 * 0 = 0")) {
			fail("incorrect:" + response);			
		}
		text.input("4 + 12 - 2?");
		response = waitForOutput(output);
		if (!response.equals("4 + 12 - 2 = 14")) {
			fail("incorrect:" + response);			
		}
		text.input("13 - 4 / 2?");
		response = waitForOutput(output);
		if (!response.equals("13 - 4 / 2 <br/> = 13 - 2 <br/> = 11")) {
			fail("incorrect:" + response);			
		}
		text.input("13 - 454 * 2 / 3 - 2 + 2?");
		response = waitForOutput(output);
		if (!response.equals("13 - 454 * 2 / 3 - 2 + 2 <br/> = 13 - 908 / 3 - 2 + 2 <br/> = 13 - 302.6666666667 - 2 + 2 <br/> = -289.6666666667")) {
			fail("incorrect:" + response);			
		}
		text.input("what is 7 * 7 - 1 / 2 + 6.6?");
		response = waitForOutput(output);
		if (!response.equals("7 * 7 - 1 / 2 + 6.6 <br/> = 49 - 0.5 + 6.6 <br/> = 55.1")) {
			fail("incorrect:" + response);			
		}
		text.input("what is 2^2 + 2 / 2 * 2 - 2 + 6.6?");
		response = waitForOutput(output);
		if (!response.equals("2^2 + 2 / 2 * 2 - 2 + 6.6 <br/> = 4 + 1 * 2 - 2 + 6.6 <br/> = 4 + 2 - 2 + 6.6 <br/> = 10.6")) {
			fail("incorrect:" + response);			
		}
		text.input("what is 2 * 2^3 + 2 / 2^2^2 * 2 - 2 + 6.6?");
		response = waitForOutput(output);
		if (!response.equals("2 * 2^3 + 2 / 2^2^2 * 2 - 2 + 6.6 <br/> = 2 * 8 + 2 / 2^2^2 * 2 - 2 + 6.6 <br/> = 16 + 2 / 4^2 * 2 - 2 + 6.6 <br/> = 16 + 2 / 16 * 2 - 2 + 6.6 <br/> = 16 + 0.125 * 2 - 2 + 6.6 <br/> = 16 + 0.25 - 2 + 6.6 <br/> = 20.85")) {
			fail("incorrect:" + response);			
		}
		bot.shutdown();
	}
	
	/**
	 * Test BEDMAS.
	 */
	@org.junit.Test
	public void testBEDMAS() throws Exception {
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		text.input("(1 * 1)");
		String response = waitForOutput(output);
		if (!response.equals("(1 * 1) = 1")) {
			fail("incorrect:" + response);			
		}
		text.input("(3 + 4)?");
		response = waitForOutput(output);
		if (!response.equals("(3 + 4) = 7")) {
			fail("incorrect:" + response);			
		}
		text.input("(0 / 4) + 2?");
		response = waitForOutput(output);
		if (!response.equals("(0 / 4) + 2 <br/> = 0 + 2 <br/> = 2")
				&& !response.equals("(0 / 4) + 2 <br/> = 0 + 2 <br/> = two")) {
			fail("incorrect:" + response);			
		}
		text.input("2 * (4 * 0)?");
		response = waitForOutput(output);
		if (!response.equals("2 * (4 * 0) <br/> = 2 * 0 <br/> = 0")) {
			fail("incorrect:" + response);			
		}
		text.input("4 + (12 - 2)?");
		response = waitForOutput(output);
		if (!response.equals("4 + (12 - 2) <br/> = 4 + 10 <br/> = 14")) {
			fail("incorrect:" + response);			
		}
		text.input("(13 - 4) / 2?");
		response = waitForOutput(output);
		if (!response.equals("(13 - 4) / 2 <br/> = 9 / 2 <br/> = 4.5")) {
			fail("incorrect:" + response);			
		}
		text.input("((13 - 454) * 2) / 3 - (2 + 2)?");
		response = waitForOutput(output);
		if (!response.equals("((13 - 454) * 2) / 3 - (2 + 2) <br/> = (-441 * 2) / 3 - (2 + 2) <br/> = -882 / 3 - (2 + 2) <br/> = -294 - 4 <br/> = -298")) {
			fail("incorrect:" + response);			
		}
		text.input("what is 7 * (((7 - 1) / 2) + 6.6)?");
		response = waitForOutput(output);
		if (!response.equals("7 * (((7 - 1) / 2) + 6.6) <br/> = 7 * ((6 / 2) + 6.6) <br/> = 7 * (3 + 6.6) <br/> = 7 * 9.6 <br/> = 67.2")) {
			fail("incorrect:" + response);
		}
		text.input("what is 2^(2 + 2) / 2 * (2 - 2) + 6.6?");
		response = waitForOutput(output);
		if (!response.equals("2^(2 + 2) / 2 * (2 - 2) + 6.6 <br/> = 2^4 / 2 * (2 - 2) + 6.6 <br/> = 8 * 0 + 6.6 <br/> = 6.6")) {
			fail("incorrect:" + response);			
		}
		text.input("(2) + (2)");
		response = waitForOutput(output);
		if (!response.equals("(2) + (2) <br/> = 2 + (2) <br/> = 2 + 2 <br/> = 4")) {
			fail("incorrect:" + response);			
		}
		text.input("((2+2))");
		response = waitForOutput(output);
		if (!response.equals("((2 + 2)) <br/> = (4) <br/> = 4")) {
			fail("incorrect:" + response);			
		}
		text.input("what is (2 * (2^(3 + 2))) / 2^2^2 * (2 - 2 + 6.6)?");
		response = waitForOutput(output);
		if (!response.equals("(2 * (2^(3 + 2))) / 2^2^2 * (2 - 2 + 6.6) <br/> = (2 * (2^5)) / 2^2^2 * (2 - 2 + 6.6) <br/> = (2 * 32) / 2^2^2 * (2 - 2 + 6.6) <br/> = 64 / 2^2^2 * (2 - 2 + 6.6) <br/> = 64 / 4^2 * (2 - 2 + 6.6) <br/> = 64 ÷ 16 * (2 - 2 + 6.6) <br/> = 4 * 6.6 <br/> = 26.4")
				&& !response.equals("(2 * (2^(3 + 2))) / 2^2^2 * (2 - 2 + 6.6) <br/> = (2 * (2^5)) / 2^2^2 * (2 - 2 + 6.6) <br/> = (2 * 32) / 2^2^2 * (2 - 2 + 6.6) <br/> = 64 / 2^2^2 * (2 - 2 + 6.6) <br/> = 64 / 4^2 * (2 - 2 + 6.6) <br/> = 64 / 16 * (2 - 2 + 6.6) <br/> = 4 * 6.6 <br/> = 26.4")) {
			fail("incorrect:" + response);			
		}
		text.input("4 - 2^(2+2)");
		response = waitForOutput(output);
		if (!response.equals("4 - 2^(2 + 2) <br/> = 4 - 2^4 <br/> = 4 - 16 <br/> = -12")) {
			fail("incorrect:" + response);			
		}		
		bot.shutdown();
	}
	
	/**
	 * Test functions.
	 */
	@org.junit.Test
	public void testFuntions() throws Exception {
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		loadConsciousness(text, output);
		
		text.input("sqrt 4");
		String response = waitForOutput(output);
		if (!response.equals("Sqrt 4 = 2") && !response.equals("Sqrt 4 = two")) {
			fail("incorrect:" + response);			
		}
		
		text.input("sqrt(4)");
		response = waitForOutput(output);
		if (!response.equals("Sqrt (4) <br/> = sqrt 4 <br/> = 2")) {
			fail("incorrect:" + response);			
		}

		text.input("(sqrt(4))");
		response = waitForOutput(output);
		if (!response.equals("(sqrt (4)) <br/> = (sqrt 4) <br/> = 2")) {
			fail("incorrect:" + response);			
		}
		
		text.input("((sqrt (4))) + 2");
		response = waitForOutput(output);
		if (!response.equals("((sqrt (4))) + 2 <br/> = ((sqrt 4)) + 2 <br/> = (2) + 2 <br/> = 2 + 2 <br/> = 4")) {
			fail("incorrect:" + response);			
		}

		text.input("sqrt 4 + 4 / sqrt (2+2)");
		response = waitForOutput(output);
		if (!response.equals("Sqrt 4 + 4 / sqrt (2 + 2) <br/> = 2 + 4 / sqrt 4 <br/> = 2 + 4 / 2 <br/> = 2 + 2 <br/> = 4")) {
			fail("incorrect:" + response);			
		}

		text.input("sqrt sqrt 4");
		response = waitForOutput(output);
		if (!response.equals("Sqrt sqrt 4 <br/> = sqrt 2 <br/> = 1.4142135623730951") && !response.equals("Sqrt sqrt 4 <br/> = √ 2 <br/> = 1.4142135623730951")) {
			fail("incorrect:" + response);			
		}

		text.input("sqrt 4 + 2");
		response = waitForOutput(output);
		if (!response.equals("Sqrt 4 + 2 = 4")) {
			fail("incorrect:" + response);			
		}

		text.input("2 + sqrt 4");
		response = waitForOutput(output);
		if (!response.equals("2 + sqrt 4 <br/> = 2 + 2 <br/> = 4")) {
			fail("incorrect:" + response);			
		}

		text.input("2 * sqrt 4");
		response = waitForOutput(output);
		if (!response.equals("2 * sqrt 4 <br/> = 2 * 2 <br/> = 4")) {
			fail("incorrect:" + response);			
		}

		text.input("sqrt 4 / 2");
		response = waitForOutput(output);
		if (!response.equals("Sqrt 4 / 2 = 1")) {
			fail("incorrect:" + response);			
		}

		text.input("2 + sqrt 4 / 2");
		response = waitForOutput(output);
		if (!response.equals("2 + sqrt 4 / 2 <br/> = 2 + 2 / 2 <br/> = 2 + 1 <br/> = 3")) {
			fail("incorrect:" + response);			
		}

		text.input("abs 55");
		response = waitForOutput(output);
		if (!response.equals("Abs 55 = 55")) {
			fail("incorrect:" + response);			
		}

		text.input("abs -55");
		response = waitForOutput(output);
		if (!response.equals("Abs -55 = 55")) {
			fail("incorrect:" + response);			
		}

		text.input("pi + 2");
		response = waitForOutput(output);
		if (!response.equals("Pi + 2 = 5.141592653589793")) {
			fail("incorrect:" + response);			
		}

		text.input("pi^2");
		response = waitForOutput(output);
		if (!response.equals("Pi^2 = 9.869604401089358")) {
			fail("incorrect:" + response);			
		}

		text.input("square root of pi");
		response = waitForOutput(output);
		if (!response.equals("Sqrt pi = 1.7724538509055159")
				&& !response.equals("Sqrt π = 1.7724538509055159")) {
			fail("incorrect:" + response);			
		}

		text.input("what is sqrt pi * 8 + tan 66");
		response = waitForOutput(output);
		if (!response.equals("Sqrt pi * 8 + tan 66 <br/> = 14.1796308072441272 + 0.02656051777603939 <br/> = 14.20619132502016659")
				&& !response.equals("Sqrt π * 8 + tan 66 <br/> = 14.1796308072441272 + 0.02656051777603939 <br/> = 14.20619132502016659")) {
			fail("incorrect:" + response);			
		}

		text.input("cosine 5");
		response = waitForOutput(output);
		if (!response.equals("Cos 5 = 0.28366218546322625")
				&& !response.equals("Cosine 5 = 0.28366218546322625")) {
			fail("incorrect:" + response);			
		}

		text.input("cos (9/0)");
		response = waitForOutput(output);
		if (!response.equals("Cos (9 / 0) <br/> = cos ∞ <br/> = undefined")) {
			fail("incorrect:" + response);			
		}

		text.input("sin (1/0)");
		response = waitForOutput(output);
		if (!response.equals("Sin (1 / 0) <br/> = sin ∞ <br/> = 0")) {
			fail("incorrect:" + response);			
		}

		text.input("sin (0/0)");
		response = waitForOutput(output);
		if (!response.equals("Sin (0 / 0) <br/> = sin undefined <br/> = undefined")) {
			fail("incorrect:" + response);			
		}

		text.input("tan 5");
		response = waitForOutput(output);
		if (!response.equals("Tan 5 = -3.380515006246586")) {
			fail("incorrect:" + response);			
		}

		text.input("atan 5");
		response = waitForOutput(output);
		if (!response.equals("Atan 5 = 1.373400766945016")) {
			fail("incorrect:" + response);			
		}

		text.input("tanh 5");
		response = waitForOutput(output);
		if (!response.equals("Tanh 5 = -3.380515006246586")) {
			fail("incorrect:" + response);			
		}

		text.input("sinh 5");
		response = waitForOutput(output);
		if (!response.equals("Sinh 5 = 74.20321057778875")) {
			fail("incorrect:" + response);			
		}

		text.input("cosh 5");
		response = waitForOutput(output);
		if (!response.equals("Cosh 5 = 74.20994852478785")) {
			fail("incorrect:" + response);			
		}

		text.input("arc sine 5");
		response = waitForOutput(output);
		if (!response.equals("Asin 5 = undefined")) {
			fail("incorrect:" + response);			
		}

		text.input("asin 1");
		response = waitForOutput(output);
		if (!response.equals("Asin 1 = 1.5707963267948966")) {
			fail("incorrect:" + response);			
		}

		text.input("acos 1");
		response = waitForOutput(output);
		if (!response.equals("Acos 1 = 0")) {
			fail("incorrect:" + response);			
		}

		text.input("arc cosine 5");
		response = waitForOutput(output);
		if (!response.equals("Acos 5 = undefined")) {
			fail("incorrect:" + response);			
		}

		text.input("log 5");
		response = waitForOutput(output);
		if (!response.equals("Log 5 = 0.6989700043360189")) {
			fail("incorrect:" + response);			
		}

		text.input("ln 5");
		response = waitForOutput(output);
		if (!response.equals("Ln 5 = 1.6094379124341003")) {
			fail("incorrect:" + response);			
		}

		text.input("floor 6.6");
		response = waitForOutput(output);
		if (!response.equals("Floor 6.6 = 6")) {
			fail("incorrect:" + response);			
		}

		text.input("ceil 6.6");
		response = waitForOutput(output);
		if (!response.equals("Ceil 6.6 = 7")) {
			fail("incorrect:" + response);			
		}

		text.input("round 6.6");
		response = waitForOutput(output);
		if (!response.equals("Round 6.6 = 7")) {
			fail("incorrect:" + response);			
		}

		text.input("round up 6.6");
		response = waitForOutput(output);
		if (!response.equals("Ceil 6.6 = 7")) {
			fail("incorrect:" + response);			
		}
		
		bot.shutdown();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}

}


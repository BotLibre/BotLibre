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

import java.io.StringWriter;
import java.math.BigInteger;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.sense.BasicTool;
import org.botlibre.util.TextStream;

/**
 * Return current date and time.
 */

public class Utils extends BasicTool {

	public Utils() {
	}

	public Vertex id(Network network) {
		return network.createVertex(getBot().memory().getMemoryName());
	}

	public Vertex size(Network network) {
		return network.createVertex(BigInteger.valueOf(getBot().memory().getLongTermMemory().size()));
	}

	public Vertex version(Network network) {
		return network.createVertex(Bot.VERSION);
	}

	public Vertex program(Network network) {
		return network.createVertex(Bot.PROGRAM + " - " + Bot.VERSION);
	}

	public Vertex uppercase(Vertex text) {
		return text.getNetwork().createVertex(text.printString().toUpperCase());
	}

	public Vertex lowercase(Vertex text) {
		return text.getNetwork().createVertex(text.printString().toLowerCase());
	}

	public Vertex sentence(Vertex text) {
		return text.getNetwork().createVertex(org.botlibre.util.Utils.capitalize(text.printString()));
	}

	public Vertex explode(Vertex text) {
		return text.getNetwork().createVertex(explode(text.printString()));
	}

	public Vertex normalize(Vertex text) {
		return text.getNetwork().createVertex(normalize(text.printString()));
	}

	public Vertex denormalize(Vertex text) {
		return text.getNetwork().createVertex(denormalize(text.printString()));
	}

	public static String formal(String text) {
		StringWriter writer = new StringWriter();
		TextStream stream = new TextStream(text);
		while (!stream.atEnd()) {
			writer.write(org.botlibre.util.Utils.capitalize(stream.nextWord()));
			if (stream.skipWhitespace()) {
				writer.write(" ");
			}
		}
		return writer.toString();
	}

	public static String explode(String text) {
		StringWriter writer = new StringWriter();
		TextStream stream = new TextStream(text);
		boolean first = true;
		while (!stream.atEnd()) {
			if (!first) {
				writer.write(' ');
			} else {
				first = false;				
			}
			char next = stream.next();
			writer.write(next);
		}
		return writer.toString();
	}

	public static String normalize(String text) {
		StringWriter writer = new StringWriter();
		TextStream stream = new TextStream(text);
		boolean first = true;
		while (!stream.atEnd()) {
			if (!first) {
				writer.write(' ');
			} else {
				first = false;
			}
			String next = stream.nextSimpleWord();
			if (next.equals(".")) {
				writer.write("dot");				
			} else if (next.equals(",")) {
				writer.write("comma");				
			} else if (next.equals("/")) {
				writer.write("slash");				
			} else if (next.equals(":")) {
				writer.write("colon");				
			} else if (next.equals("@")) {
				writer.write("at");				
			} else if (next.equals("-")) {
				writer.write("dash");				
			} else {
				writer.write(next);
			}
		}
		return writer.toString();
	}

	public static String denormalize(String text) {
		StringWriter writer = new StringWriter();
		TextStream stream = new TextStream(text);
		boolean space = false;
		while (!stream.atEnd()) {
			String next = stream.nextSimpleWord();
			if (next.equals("dot")) {
				writer.write(".");
				space = false;
			} else if (next.equals("comma")) {
				writer.write(",");
				space = false;
			} else if (next.equals("slash")) {
				writer.write("/");
				space = false;
			} else if (next.equals("colon")) {
				writer.write(":");
				space = false;
			} else if (next.equals("at")) {
				writer.write("@");
				space = false;
			} else if (next.equals("dash")) {
				writer.write("-");
				space = false;
			} else {
				if (space) {
					writer.write(' ');
				}
				writer.write(next);
				space = true;
			}
		}
		return writer.toString();
	}

	public Vertex formal(Vertex fragment) {
		return fragment.getNetwork().createVertex(formal(fragment.printString()));
	}

	public static String gender(String text) {
		StringWriter writer = new StringWriter();
		TextStream stream = new TextStream(text);
		while (!stream.atEnd()) {
			String word = stream.nextWord().toLowerCase();
			if (word.equals("he")) {
				word = "she";
			} else if (word.equals("she")) {
				word = "he";
			} else if (word.equals("his")) {
				word = "her";
			} else if (word.equals("her")) {
				word = "his";
			} else if (word.equals("him")) {
				word = "her";
			} else if (word.equals("boy")) {
				word = "girl";
			} else if (word.equals("girl")) {
				word = "boy";
			} else if (word.equals("girlfriend")) {
				word = "boyfriend";
			} else if (word.equals("boyfriend")) {
				word = "girlfriend";
			}
			writer.write(word);
			if (stream.skipWhitespace()) {
				writer.write(" ");
			}
		}
		return writer.toString();
	}

	public Vertex gender(Vertex fragment) {
		return fragment.getNetwork().createVertex(gender(fragment.printString()));
	}

	public static String person(String text) {
		StringWriter writer = new StringWriter();
		TextStream stream = new TextStream(text);
		while (!stream.atEnd()) {
			String word = stream.nextWord().toLowerCase();
			if (word.equals("i")) {
				word = "you";
			} else if (word.equals("me")) {
				word = "you";
			} else if (word.equals("myself")) {
				word = "yourself";
			} else if (word.equals("mine")) {
				word = "yours";
			} else if (word.equals("my")) {
				word = "your";
			} else if (word.equals("you")) {
				word = "I";
			} else if (word.equals("yourself")) {
				word = "myself";
			} else if (word.equals("yours")) {
				word = "mine";
			} else if (word.equals("your")) {
				word = "my";
			} else if (word.equals("am")) {
				word = "are";
			} else if (word.equals("are")) {
				word = "am";
			}
			writer.write(word);
			if (stream.skipWhitespace()) {
				writer.write(" ");
			}
		}
		return writer.toString();
	}

	public Vertex person(Vertex fragment) {
		return fragment.getNetwork().createVertex(person(fragment.printString()));
	}

	public static String person2(String text) {
		StringWriter writer = new StringWriter();
		TextStream stream = new TextStream(text);
		while (!stream.atEnd()) {
			String word = stream.nextWord().toLowerCase();
			if (word.equals("i")) {
				word = "he";
			} else if (word.equals("me")) {
				word = "him";
			} else if (word.equals("myself")) {
				word = "himself";
			} else if (word.equals("mine")) {
				word = "his";
			} else if (word.equals("my")) {
				word = "his";
			} else if (word.equals("he")) {
				word = "I";
			} else if (word.equals("him")) {
				word = "me";
			} else if (word.equals("himself")) {
				word = "myself";
			} else if (word.equals("his")) {
				word = "my";
			} else if (word.equals("her")) {
				word = "I";
			} else if (word.equals("herself")) {
				word = "myself";
			}
			writer.write(word);
			if (stream.skipWhitespace()) {
				writer.write(" ");
			}
		}
		return writer.toString();
	}

	public Vertex person2(Vertex fragment) {
		return fragment.getNetwork().createVertex(person2(fragment.printString()));
	}
	
}
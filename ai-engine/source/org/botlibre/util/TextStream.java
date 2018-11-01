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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides a useful text stream class.
 */

public class TextStream {
	public static final String WHITESPACE =" \t\n\r\f";
	public static final String HTTP =" \t\n\r\f\"{}<>";
	public static final String TOKENS =" \t\n\r\f.,:;!()?[]{}+=^&*\"`~|/\\<>";
	public static final String TERMINATORS =".?!。？！";
	public static Set<String> ABBREVIATIONS = new HashSet<String>(Arrays.asList(new String[]{"mr","ms", "mrs", "dr", "inc", "sr", "jr", "st", "vs", "mt", "ltd", "co"}));
	public static Set<String> IGNORABLE = new HashSet<String>(Arrays.asList(new String[]{"'","`", "\"", ","}));
	
	/**
	 * The text being streamed.
	 */
	protected String text;
	
	/**
	 * The current stream index into the text.
	 */
	protected int index;
	
	public TextStream(String text) {
		this.index = 0;
		this.text = text;
	}
	
	public void reset() {
		this.index = 0;
	}

	public char last() {
		if (this.index <= 1) {
			return (char)0;
		}
		return this.text.charAt(this.index - 2);
	}
	
	public char current() {
		if (atEnd()) {
			this.text.charAt(this.text.length() - 1);
		}
		if (this.index <= 0) {
			return (char)0;
		}
		return this.text.charAt(this.index - 1);
	}
	
	public char peekPrevious() {
		if (this.index <= 1) {
			return (char)0;
		}
		return this.text.charAt(this.index - 2);
	}
	
	public char peek() {
		if (atEnd()) {
			return (char)-1;
		}
		return this.text.charAt(this.index);
	}
	
	public int peekCodePoint() {
		if (atEnd()) {
			return (int)-1;
		}
		return this.text.codePointAt(this.index);
	}
	
	public char next() {
		if (this.index >= this.text.length()) {
			return (char)-1;
		}
		char next = this.text.charAt(this.index);
		this.index++;
		return next;
	}
	
	public char previous() {
		this.index--;
		char previous = current();
		return previous;
	}
	
	public void skip() {
		if (this.index >= this.text.length()) {
			return;
		}
		this.index++;
	}
	
	public void skip(int count) {
		this.index = Math.min(this.index + count, this.text.length());
	}
	
	public void backup(int count) {
		this.index = Math.max(this.index - count, 0);
	}
	
	public void backup() {
		this.index--;
	}
	
	public String peek(int count) {
		int start = this.index;		
		int end = Math.min(this.index + count, this.text.length());
		return this.text.substring(start, end);
	}
	
	public String previous(int count) {
		int end = this.index - 2;
		if (end <= 0) {
			return "";
		}
		int start = Math.max(end - count, 0);
		return this.text.substring(start, end);
	}
	
	public String next(int count) {
		int start = this.index;		
		this.index = Math.min(this.index + count, this.text.length());
		return this.text.substring(start, this.index);
	}
	
	public boolean atEnd() {
		return this.index >= this.text.length();
	}
	
	public boolean atStart() {
		return this.index == 0;
	}

	public String upTo(char token) {
		return upTo(token, false);
	}

	public void skipTo(char token) {
		skipTo(token, false);
	}

	public String upToEnd() {
		int start = this.index;
		this.index = this.text.length();
		return this.text.substring(start, this.index);
	}
	
	public String upTo(char token, boolean including) {
		return upTo(token, including, false);
	}
	
	public String upTo(char token, boolean including, boolean resetIfNotFound) {
		int start = this.index;
		boolean found = skipTo(token, including);
		if (resetIfNotFound && !found) {
			this.index = start;
			return "";
		}
		return this.text.substring(start, this.index);
	}
	
	public boolean skipTo(char token, boolean including) {
		boolean found = false;
		while (!atEnd()) {
			if (peek() == token) {
				found = true;
				break;
			}
			skip();
		}
		if (found && including) {
			skip();
		}
		return found;
	}
	
	public void backupTo(char token) {
		backupTo(token, false);
	}
	
	public void backupTo(char token, boolean including) {
		boolean found = false;
		while (this.index > 0) {
			if (current() == token) {
				found = true;
				break;
			}
			backup();
		}
		if (found && including) {
			backup();
		}
	}

	public String upToAny(String tokens) {
		return upToAny(tokens, false);
	}
	
	public String upToAny(String tokens, boolean including) {
		int start = this.index;
		skipToAny(tokens, including);
		return this.text.substring(start, this.index);
	}
	
	public void skipToAny(String tokens) {
		skipToAny(tokens, false);
	}
	
	public void skipToAny(String tokens, boolean including) {
		boolean found = false;
		while (!atEnd()) {
			if (tokens.indexOf(peek()) != -1) {
				found = true;
				break;
			}
			skip();
		}
		if (found && including) {
			skip();
		}
	}

	public String upToAll(String tokens) {
		return upToAll(tokens, false);
	}

	public String upToAll(String tokens, boolean including) {
		return upToAll(tokens, including, false);
	}
	
	public String upToAll(String tokens, boolean including, boolean resetIfNotFound) {
		int start = this.index;
		boolean found = skipToAll(tokens, including);
		if (resetIfNotFound && !found) {
			this.index = start;
			return "";
		}
		return this.text.substring(start, this.index);
	}
	
	public void skipToAll(String tokens) {
		skipToAll(tokens, false);
	}
	
	public boolean skipToAll(String tokens, boolean including) {
		int tokenIndex = 0;
		boolean found = false;
		while (!atEnd()) {
			if (peek() == tokens.charAt(tokenIndex)) {
				tokenIndex++;
				if (tokenIndex == tokens.length()) {
					found = true;
					break;
				}
			} else {
				tokenIndex = 0;
			}
			skip();
		}
		if (found) {
			if (including) {
				skip();
			} else {
				this.index = index - (tokens.length() - 1);
			}
		}
		return found;
	}

	public void backupToAll(String tokens) {
		backupToAll(tokens, false);
	}
	
	public void backupToAll(String tokens, boolean including) {
		int tokenIndex = 1;
		int length = tokens.length();
		boolean found = false;
		while (this.index > 0) {
			if (current() == tokens.charAt(length - tokenIndex)) {
				if (tokenIndex == tokens.length()) {
					found = true;
					break;
				}
				tokenIndex++;
			} else {
				tokenIndex = 1;
			}
			backup();
		}
		if (found) {
			if (including) {
				backup();
			} else {
				this.index = index + (length - 1);
			}
		}
	}

	public void backupToAny(String tokens) {
		backupToAny(tokens, false);
	}
	
	public void backupToAny(String tokens, boolean including) {
		boolean found = false;
		while (!atStart()) {
			if (tokens.indexOf(peekPrevious()) != -1) {
				found = true;
				break;
			}
			backup();
		}
		if (found && including) {
			backup();
		}
	}
	
	public String peekWord() {
		int position = this.index;
		String word = nextWord();
		this.index = position;
		return word;
	}
	
	public List<String> allWords() {
		List<String> words = new ArrayList<String>();
		while (!atEnd()) {
			String word = nextWord();
			if (word != null) {
				words.add(word);
			}
		}
		return words;
	}
	
	public List<String> csv() {
		List<String> words = new ArrayList<String>();
		skipWhitespace();
		while (!atEnd()) {
			String word = upTo(',');
			if (!atEnd()) {
				skip();
				skipWhitespace();
			}
			word = word.trim();
			if ((word != null) && !word.isEmpty()) {
				words.add(word);
			}
		}
		return words;
	}
	
	public String peekPreviousWord() {
		int original = this.index;
		backupWhitespace();
		int start = this.index;
		if (atStart()) {
			return null;
		}
		char current = current();
		if ((TOKENS.indexOf(current) != -1) || (isWordSymbol(current))) {
			previous();
			String word = String.valueOf(current);
			this.index = original;
			return word;
		}
		backupToAny(TOKENS);
		String word = this.text.substring(Math.max(this.index - 1, 0), start);
		this.index = original;
		return word;
	}
	
	public boolean isWordSymbol(char character) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(character);
		return (Character.isIdeographic(character)
				|| (block == Character.UnicodeBlock.HIRAGANA)
				|| (block == Character.UnicodeBlock.KATAKANA)
				|| (block == Character.UnicodeBlock.HANGUL_SYLLABLES)
				|| (block == Character.UnicodeBlock.HANGUL_JAMO))
				&& !Character.isDigit(character);
	}
	
	public String nextQuotes() {
		if (atEnd()) {
			return "";
		}
		int start = this.index;
		skipQuotes();
		int end = this.index - 1;
		if (atEnd() && current() != '"') {
			end = this.index;
		}
		String quotes = this.text.substring(start, end);
		return quotes;
	}
	
	public String nextStringQuotes() {
		if (atEnd()) {
			return "";
		}
		int start = this.index;
		skipStringQuotes();
		int end = this.index - 1;
		if (atEnd() && current() != '\'') {
			end = this.index;
		}
		String quotes = this.text.substring(start, end);
		return quotes;
	}
	
	public String nextStringDoubleQuotes() {
		if (atEnd()) {
			return "";
		}
		int start = this.index;
		skipStringDoubleQuotes();
		int end = this.index - 1;
		if (atEnd() && current() != '"') {
			end = this.index;
		}
		String quotes = this.text.substring(start, end);
		return quotes;
	}
	
	public String nextStringWithBracketsDoubleQuotes() {
		if (atEnd()) {
			return "";
		}
		int start = this.index;
		skipStringWithBracketsDoubleQuotes();
		int end = this.index - 1;
		if (atEnd() && current() != '"') {
			end = this.index;
		}
		String quotes = this.text.substring(start, end);
		return quotes;
	}
	
	public String nextQuotesExcludeDoubleQuote() {
		String quotes = nextQuotes();
		if (quotes.contains("\"\"")) {
			quotes = quotes.replace("\"\"", "\"");
		}
		return quotes;
	}
	
	public void skipStringQuotes() {
		if (atEnd()) {
			return;
		}
		char next = next();
		while (!atEnd() && (next != '\'')) {
			if ((next == '\\')) {
				skip();
			}
			if (next == '{') {
				skipStringBrackets();
			}
			next = next();
		}
	}
	
	public void skipStringDoubleQuotes() {
		if (atEnd()) {
			return;
		}
		char next = next();
		while (!atEnd() && (next != '"')) {
			if ((next == '\\')) {
				skip();
			}
			next = next();
		}
	}
	
	public void skipStringWithBracketsDoubleQuotes() {
		if (atEnd()) {
			return;
		}
		char next = next();
		while (!atEnd() && (next != '"')) {
			if ((next == '\\')) {
				skip();
			}
			if (next == '{') {
				skipStringDoubleQuoteBrackets();
			}
			next = next();
		}
	}
	
	public void skipQuotes() {
		if (atEnd()) {
			return;
		}
		char next = next();
		char peek = peek();
		while (!atEnd() && ((next != '"') || (peek == '"'))) {
			if ((next == '"') && (peek == '"')) {
				skip();
			}
			if (next == '{') {
				skipBrackets();
			}
			next = next();
			peek = peek();
		}
	}
	
	public void skipBrackets() {
		if (atEnd()) {
			return;
		}
		char next = next();
		while (!atEnd() && (next != '}')) {
			if (next == '"') {
				skipQuotes();
			}
			next = next();
		}
	}
	
	public void skipStringBrackets() {
		if (atEnd()) {
			return;
		}
		char next = next();
		while (!atEnd() && (next != '}')) {
			if (next == '\'') {
				skipStringQuotes();
			}
			next = next();
		}
	}
	
	public void skipStringDoubleQuoteBrackets() {
		if (atEnd()) {
			return;
		}
		char next = next();
		while (!atEnd() && (next != '}')) {
			if ((next == '\\')) {
				skip();
			}
			if (next == '"') {
				skipStringWithBracketsDoubleQuotes();
			}
			if (next == '{') {
				skipStringDoubleQuoteBrackets();
			}
			next = next();
		}
	}
	
	public String nextWord() {
		skipWhitespace();
		if (atEnd()) {
			return null;
		}
		//int peek = peekCodePoint();
		char peek = peek();
		boolean isSign = (peek == '-') || (peek == '+');
		// Check for "1-1" vs "1 - -1"
		if (isSign && !atStart()) {
			backup();
			char previous = peek();
			if (Character.isLetterOrDigit(previous) || previous == ')') {
				skip();
				skip();
				return String.valueOf(peek);
			}
			skip();
		}
		if (isWordSymbol(peek) || (!Character.isLetterOrDigit(peek) && peek != '_' && peek != '#' && peek != '@' && !isSign)) {			
			skip();
			Character.UnicodeBlock block = Character.UnicodeBlock.of(peek);
			if (block == Character.UnicodeBlock.HIGH_SURROGATES) {
				skip();
				return this.text.substring(this.index - 2, this.index);
			}
			return String.valueOf(peek);
		}
		// Check url.
		if (peek == 'h') {
			if (peek(7).equals("http://") || peek(8).equals("https://")) {
				return upToAny(HTTP);
			}
		}
		int start = this.index;
		boolean wasDigit = Character.isDigit(peek);
		skip();
		while (!atEnd()) {
			//peek = peekCodePoint();
			peek = peek();
			boolean isDigit = Character.isDigit(peek);
			if (isWordSymbol(peek)) {
				break;
			}
			// "-1" is ok
			if (isSign && !isDigit) {
				break;
			} else {
				// Allow "1.1", "1,000", "ab-ba", "ab_ba", "#abc"
				if (!Character.isLetter(peek) && !isDigit && (peek != '_') && (peek != '#')) {
					if (wasDigit) {
						if ((peek != '.') && (peek != ',') && (peek != '@')) {
							break;
						}
					} else {
						if ((peek != '-') && (peek != '_') && (peek != '@') && (peek != '.')) {
							break;
						}
					}
				}
			}
			wasDigit = isDigit;
			skip();
			// Could have been "1.", "1.a"
			if ((((peek == '.') && !Character.isLetter(peek())) || (peek == ',')) && !Character.isDigit(peek())) {
				backup();
				break;
			}
			isSign = false;
		}
		return this.text.substring(start, this.index);
	}
	
	public String nextSimpleWord() {
		skipWhitespace();
		if (atEnd()) {
			return null;
		}
		char peek = peek();
		if (isWordSymbol(peek) || (!Character.isLetterOrDigit(peek))) {			
			skip();
			Character.UnicodeBlock block = Character.UnicodeBlock.of(peek);
			if (block == Character.UnicodeBlock.HIGH_SURROGATES) {
				skip();
				return this.text.substring(this.index - 2, this.index);
			}
			return String.valueOf(peek);
		}
		int start = this.index;
		skip();
		while (!atEnd()) {
			peek = peek();
			boolean isDigit = Character.isDigit(peek);
			if (isWordSymbol(peek)) {
				break;
			}
			if (!Character.isLetter(peek) && !isDigit) {
				break;
			}
			skip();
		}
		return this.text.substring(start, this.index);
	}
	
	public void skipWord() {
		skipWhitespace();
		if (atEnd()) {
			return;
		}
		if (TOKENS.indexOf(peek()) != -1) {
			skip();
		}
		// Check url.
		if (peek() == 'h') {
			if (peek(7).equals("http://")) {
				skipToAny(WHITESPACE);
			}
		}
		skipToAny(TOKENS);
	}
	
	/**
	 * Return the next paragraph text including full sentences up to the max text size.
	 */
	public String nextParagraph(int max) {
		skipWhitespace();
		if (atEnd()) {
			return null;
		}
		int start = this.index;
		int last = this.index;
		while (!atEnd()) {
			skipSentence();
			if ((this.index - start) > max) {
				if (start == last) {
					this.index = start + max;
				} else {
					this.index = last;
				}
				break;
			}
			last = this.index;
		}
		return this.text.substring(start, this.index);
	}
	
	public String nextSentence() {
		skipWhitespace();
		if (atEnd()) {
			return null;
		}
		int start = this.index;
		skipSentence();
		return this.text.substring(start, this.index);
	}
	
	public void skipSentence() {
		skipWhitespace();
		if (atEnd()) {
			return;
		}
		skipToAny(TERMINATORS, true);
		while (!atEnd()) {
			char peek = peek();
			boolean done = WHITESPACE.indexOf(peek) != -1;
			if (done) {
				if (current() != '.') {
					break;
				}
				backup();
				String word = peekPreviousWord();
				skip();
				if (word != null && (ABBREVIATIONS.contains(word.toLowerCase())
						|| ((word.length() == 1) && (Character.isUpperCase(word.charAt(0)))))) {
					done = false;
				} else {
					break;
				}
			} else if (isWordSymbol(peek)) {
				break;
			} else if (peek == '<') {
				// Allow HTML as whitespace.
				break;
			}
			skipToAny(TERMINATORS, true);
		}
	}
	
	public String nextWhitespace() {
		int start = this.index;
		while (!atEnd() && (WHITESPACE.indexOf(peek()) != -1)) {
			skip();
		}
		return this.text.substring(start, this.index);
	}
	
	public boolean skipWhitespace() {
		boolean found = false;
		while (!atEnd() && (Character.isWhitespace(peek()))) {
			skip();
			found = true;
		}
		return found;
	}
	
	public void backupWhitespace() {
		while (!atStart() && (WHITESPACE.indexOf(current()) != -1)) {
			backup();
		}
	}
	
	public String nextLine() {
		return upToAll("\n", true);
	}
	
	public String currentLine() {
		int position = this.index;
		backupToAll("\n");
		String line = nextLine();
		this.index = position;
		return line;
	}
	
	public int currentLineNumber() {
		int position = this.index;
		this.index = 0;
		int count = 0;
		int last = this.index;
		while (this.index < position) {
			skipLine();
			if (this.index < position) {
				count++;
			}
			if (this.index == last) {
				break;
			}
			last = this.index;
		}
		this.index = position;
		return count + 1;
	}
	
	public int currentLineColumn() {
		int position = this.index;
		backupToAll("\n");
		int column = position - this.index;
		this.index = position;
		return column + 1;
	}
	
	public void skipLine() {
		skipToAll("\n", true);
	}

	public int getPosition() {
		return index;
	}

	public void setPosition(int index) {
		this.index = index;
	}

	public String getText() {
		return text;
	}
	
	public String toString() {
		return peek(text.length());
	}
	
}
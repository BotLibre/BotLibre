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
package org.botlibre.self;

import java.io.StringWriter;

import org.botlibre.BotException;
import org.botlibre.util.TextStream;

/**
 * Self compiler exception.
 */
public class SelfParseException extends BotException {
	private static final long serialVersionUID = 7237444806218859374L;
	
	protected String code;
	protected int lineNumber;
	protected int columnNumber;
	protected String line;
	
	public SelfParseException(String message) {
		super(message);
	}
	
	public SelfParseException(String message, TextStream stream) {
		super(message);
		initFromStream(stream);
	}
	
	public void initFromStream(TextStream stream) {
		this.code = stream.getText();
		this.lineNumber = stream.currentLineNumber();
		this.columnNumber = stream.currentLineColumn();
		this.line = stream.currentLine();
	}
	
	public SelfParseException(String message, TextStream stream, Throwable exception) {
		this(message, stream);
		initCause(exception);
	}
	
	public SelfParseException(String message, Throwable exception) {
		super(message);
		initCause(exception);
	}
	
	public static SelfParseException invalidCharacter(char found, char expected, TextStream stream) {
		return new SelfParseException("Invalid character expected '" + expected + "' found '" + found + "'", stream); 
	}
	
	public static SelfParseException unexpectedEndOfFile(char expected, TextStream stream) {
		return new SelfParseException("Unexpected end of file, expected '" + expected + "'", stream); 
	}
	
	public static SelfParseException invalidWord(String found, String expected, TextStream stream) {
		return new SelfParseException("Invalid word expected '" + expected + "' found '" + found + "'", stream); 
	}

	public String getMessage() {
		StringWriter writer = new StringWriter();
		writer.write(super.getMessage());
		writer.write("\n");
		writer.write("Line: \"" + this.line + "\"");
		writer.write("\n");
		writer.write("Line number: " + this.lineNumber);
		writer.write("\n");
		writer.write("Line column: " + this.columnNumber);
		writer.write("\n");
		writer.flush();
		return writer.toString();
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	public void setColumnNumber(int columnNumber) {
		this.columnNumber = columnNumber;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}
}

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
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;

/**
 * Self execution exception.
 * Exception thrown when a Self equation evaluation fails.
 */
public class SelfExecutionException extends BotException {
	private static final long serialVersionUID = 7237444806218859374L;
	
	protected Object lineNumber;
	protected Object line;
	
	public SelfExecutionException(String message) {
		super(message);
	}
	
	public SelfExecutionException(Vertex equation, Exception exception) {
		super(exception);
		Vertex source = equation.getRelationship(Primitive.SOURCE);
		if (source != null) {
			this.line = source.getData();
		}
		Vertex number = equation.getRelationship(Primitive.LINE_NUMBER);
		if (number != null) {
			this.lineNumber = number.getData();
		}
	}
	
	public SelfExecutionException(Vertex equation, String message) {
		super(message);
		Vertex source = equation.getRelationship(Primitive.SOURCE);
		if (source != null) {
			this.line = source.getData();
		}
		Vertex number = equation.getRelationship(Primitive.LINE_NUMBER);
		if (number != null) {
			this.lineNumber = number.getData();
		}
	}

	public String getMessage() {
		StringWriter writer = new StringWriter();
		writer.write(super.getMessage());
		if (this.line != null) {
			writer.write("\n");
			writer.write("Line: \"" + String.valueOf(this.line) + "\"");
		}
		if (this.lineNumber != null) {
			writer.write("\n");
			writer.write("Line number: " + String.valueOf(this.lineNumber));
		}
		writer.flush();
		return writer.toString();
	}
	
	public Object getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(Object lineNumber) {
		this.lineNumber = lineNumber;
	}

	public Object getLine() {
		return line;
	}

	public void setLine(Object line) {
		this.line = line;
	}
}

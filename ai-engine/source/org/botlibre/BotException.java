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
package org.botlibre;

/**
 * Root to most exceptions thrown from the Bot system.
 * Provides nested exception and extra debug info.
 */

public class BotException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
		
	public BotException() {
		super();
	}
	
	public BotException(String message) {
		super(message);
	}
	
	public BotException(Exception exception) {
		super(exception.getMessage(), exception);
	}
	
	public BotException(String message, Exception exception) {
		super(message, exception);
	}
	
	public static BotException offensive() {
		BotException exception = new BotException("Profanity, offensive or sexual language is not permitted.");
		throw exception;
	}
}


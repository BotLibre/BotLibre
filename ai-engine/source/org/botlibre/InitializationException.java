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
 * Thrown when an exception occurs during startup.
 */

public class InitializationException extends BotException {
	
	private static final long serialVersionUID = 1L;
	
	public InitializationException() {
		super();
		
	}
	
	public InitializationException(String message) {
		super(message);
	}
	
	public InitializationException(Exception exception) {
		super(exception);
		exception.printStackTrace();
	}
}



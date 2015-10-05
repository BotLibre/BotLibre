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

package org.botlibre.sdk;

/**
 * Listener interface for a LiveChatConnection.
 * This gives asynchronous notification when a channel receives a message, or notice.
 */
public interface LiveChatListener {
	/**
	 * A user message was received from the channel.
	 */
	void message(String message);
	
	/**
	 * An informational message was received from the channel.
	 * Such as a new user joined, private request, etc.
	 */	
	void info(String message);

	/**
	 * An error message was received from the channel.
	 * This could be an access error, or message failure.
	 */	
	void error(String message);
	
	/**
	 * Notification that the connection was closed.
	 */
	void closed();
	
	/**
	 * The channels users changed (user joined, left, etc.)
	 * This contains a comma separated values (CSV) list of the current channel users.
	 * It can be passed to the SDKConnection.getUsers() API to obtain the UserConfig info for the users.
	 */
	void updateUsers(String usersCSV);
}
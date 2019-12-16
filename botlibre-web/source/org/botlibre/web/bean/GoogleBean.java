/******************************************************************************
 *
 *  Copyright 2013-2019 Paphus Solutions Inc.
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
package org.botlibre.web.bean;

import org.botlibre.BotException;
import org.botlibre.sense.google.GoogleCalendar;

import twitter4j.TwitterException;

public class GoogleBean extends ServletBean {
	
	protected boolean isAuthorising = false;
	
	public GoogleBean() {
	}

	public boolean isAuthorising() {
		return isAuthorising;
	}

	public void setAuthorising(boolean isAuthorising) {
		this.isAuthorising = isAuthorising;
	}

	public void cancelAuthorisation() throws TwitterException {
		this.isAuthorising = false;
	}

	@Override
	public void disconnectInstance() {
		disconnect();
	}
	
	@Override
	public void disconnect() {
		this.isAuthorising = false;
	}

	public boolean isAuthorized() {
		return getBot().awareness().getSense(GoogleCalendar.class).isAuthorized();
	}

	public String getGoogleAccountId() {
		return getBot().awareness().getSense(GoogleCalendar.class).getGoogleAccountId();
	}

	public void connect(String authCode) {
		try {
			this.isAuthorising = false;
			GoogleCalendar sense = getBot().awareness().getSense(GoogleCalendar.class);
			sense.resetRefreshToken(authCode.trim());
			sense.saveProperties();
		} catch (Exception exception) {
			throw new BotException(exception.getMessage());
		}
	}

	public void clear() {
		GoogleCalendar sense = getBot().awareness().getSense(GoogleCalendar.class);
		sense.setRefreshToken("");
		sense.saveProperties();
	}
	
	public String getAuthURL() {
		return getBot().awareness().getSense(GoogleCalendar.class).getAuthURL();
	}
}

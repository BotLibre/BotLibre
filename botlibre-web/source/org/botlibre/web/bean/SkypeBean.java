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

import java.util.logging.Level;

import org.botlibre.sense.skype.Skype;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;

public class SkypeBean extends ServletBean {

	public SkypeBean() {
	}
	
	public String getToken() {
		return getBot().awareness().getSense(Skype.class).getToken();
	}
	
	public String getAppId() {
		return getBot().awareness().getSense(Skype.class).getAppId();
	}
	
	public String getAppPassword() {
		return getBot().awareness().getSense(Skype.class).getAppPassword();
	}
	
	public void connect() throws Exception {

		getBotBean().setInstance(AdminDatabase.instance().updateInstanceSkype(getBotBean().getInstance().getId(), true));
	}
	
	public void save(/*String token,*/ String appId, String appPassword) {
		appId = Utils.sanitize(appId);
		/*if (!getBotBean().getInstance().isAdult() && Utils.checkProfanity(autoPosts)) {
			throw BotException.offensive();
		}*/
		Skype sense = getBot().awareness().getSense(Skype.class);
		//sense.setToken(token.trim());
		sense.setAppId(appId.trim());
		sense.setAppPassword(appPassword.trim());
		
		sense.saveProperties();
	}
	
	public String getWebhook() {
		String hook = null;
		if (Site.HTTPS) {
			hook = Site.SECUREURLLINK + "/rest/api/skype/";
		} else {
			hook = Site.URLLINK + "/rest/api/skype/";
		}
		if (getUser().getApplicationId() == null) {
			getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
		}
		hook = hook + getUser().getApplicationId();
		hook = hook + "/" + getBotBean().getInstanceId();
		return hook;
	}
	
	public void clear() {
		Skype sense = getBot().awareness().getSense(Skype.class);
		sense.setToken("");
		sense.setAppId("");
		sense.setAppPassword("");

		sense.saveProperties();
	}
	
	@Override
	public void disconnectInstance() {
		disconnect();
	}

	@Override
	public void disconnect() {
	}
	
	public void checkStatus() {
		getBot().setDebugLevel(Level.FINE);
		Skype skype = getBot().awareness().getSense(Skype.class);
		skype.checkProfile();
	}

	public void disable() {
		getBot().awareness().getSense(Skype.class).setIsEnabled(false);
		
		getBotBean().setInstance(AdminDatabase.instance().updateInstanceSkype(getBotBean().getInstance().getId(), false));
	}
}

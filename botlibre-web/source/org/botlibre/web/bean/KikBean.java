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

import org.botlibre.sense.kik.Kik;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;

public class KikBean extends ServletBean {

	public KikBean() {
	}
	
	public String getUsername() {
		return getBot().awareness().getSense(Kik.class).getUsername();
	}
	
	public String getApiKey() {
		return getBot().awareness().getSense(Kik.class).getApiKey();
	}
	
	public void connect() throws Exception {

		getBotBean().setInstance(AdminDatabase.instance().updateInstanceKik(getBotBean().getInstance().getId(), true));
	}
	
	public void save(/*String token,*/ String username, String apiKey) {
		username = Utils.sanitize(username);
		apiKey = Utils.sanitize(apiKey);
		/*if (!getBotBean().getInstance().isAdult() && Utils.checkProfanity(autoPosts)) {
			throw BotException.offensive();
		}*/
		Kik sense = getBot().awareness().getSense(Kik.class);
		
		sense.setUsername(username.trim());
		sense.setApiKey(apiKey.trim());
		
		sense.saveProperties();
	}
	
	public String getWebhook() {
		String hook = null;
		if (Site.HTTPS) {
			hook = Site.SECUREURLLINK + "/rest/api/kik/";
		} else {
			hook = Site.URLLINK + "/rest/api/kik/";
		}
		if (getUser().getApplicationId() == null) {
			getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
		}
		hook = hook + getUser().getApplicationId();
		hook = hook + "/" + getBotBean().getInstanceId();
		return hook;
	}
	
	public void clear() {
		Kik sense = getBot().awareness().getSense(Kik.class);
		
		sense.setUsername("");
		sense.setApiKey("");

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
		Kik kik = getBot().awareness().getSense(Kik.class);
		kik.checkProfile();
	}
	
	public void configure() { 
		Kik kik = getBot().awareness().getSense(Kik.class);
		kik.configure(getWebhook());
	}

	public void disable() {
		getBot().awareness().getSense(Kik.class).setIsEnabled(false);
		
		getBotBean().setInstance(AdminDatabase.instance().updateInstanceKik(getBotBean().getInstance().getId(), false));
	}
}

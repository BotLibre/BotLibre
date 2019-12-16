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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.botlibre.sense.google.GoogleAssistant;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;

public class GoogleAssistantBean extends ServletBean {

	public GoogleAssistantBean() {
	}
	
	public void connect() throws Exception {
		getBotBean().setInstance(AdminDatabase.instance().updateInstanceGoogleAssistant(getBotBean().getInstance().getId(), true));
	}
	
	public void save(String stopPhrases) {
		stopPhrases = Utils.sanitize(stopPhrases);
		GoogleAssistant sense = getBot().awareness().getSense(GoogleAssistant.class);
		
		stopPhrases.replace(",", "\n");
		TextStream stream = new TextStream(stopPhrases.trim());
		sense.setStopPhrases(new ArrayList<String>());
		while (!stream.atEnd()) {
			String rss = stream.upToAny("\n").trim();
			sense.getStopPhrases().add(rss);
			stream.skip();
			stream.skipWhitespace();
		}
		
		sense.saveProperties();
	}
	
	public String getWebhook() {
		String hook = null;
		if (Site.HTTPS) {
			hook = Site.SECUREURLLINK + "/rest/api/googleassistant/";
		} else {
			hook = Site.URLLINK + "/rest/api/googleassistant/";
		}
		if (getUser().getApplicationId() == null) {
			getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
		}
		hook = hook + getUser().getApplicationId();
		hook = hook + "/" + getBotBean().getInstanceId();
		return hook;
	}
	
	public void clear() {
		GoogleAssistant sense = getBot().awareness().getSense(GoogleAssistant.class);
		//sense.setToken("");

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
		/*getBot().setDebugLevel(Level.FINE);
		Skype skype = getBot().awareness().getSense(Skype.class);
		skype.checkProfile();*/
	}

	public void disable() {
		getBot().awareness().getSense(GoogleAssistant.class).setIsEnabled(false);
		
		getBotBean().setInstance(AdminDatabase.instance().updateInstanceGoogleAssistant(getBotBean().getInstance().getId(), false));
	}
	
	public String getStopPhrases() {
		StringWriter writer = new StringWriter();
		Iterator<String> iterator = getBot().awareness().getSense(GoogleAssistant.class).getStopPhrases().iterator();
		while (iterator.hasNext()) {
			writer.write(iterator.next());
			if (iterator.hasNext()) {
				writer.write("\n");				
			}
		}
		return writer.toString();
	}
}

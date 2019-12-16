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

import org.botlibre.sense.alexa.Alexa;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;

public class AlexaBean extends ServletBean {

	public AlexaBean() {
	}
	
	public void connect() throws Exception {
		getBotBean().setInstance(AdminDatabase.instance().updateInstanceAlexa(getBotBean().getInstance().getId(), true));
	}
	
	public void save(String launchResponse, String helpResponse, String cancelResponse, String stopResponse, String fallbackResponse, String followupPrompt, boolean autoExit, String stopPhrases) {
		Alexa sense = getBot().awareness().getSense(Alexa.class);

		sense.setLaunchResponse(Utils.sanitize(launchResponse.trim()));
		sense.setHelpResponse(Utils.sanitize(helpResponse.trim()));
		sense.setCancelResponse(Utils.sanitize(cancelResponse.trim()));
		sense.setStopResponse(Utils.sanitize(stopResponse.trim()));
		sense.setFallbackResponse(Utils.sanitize(fallbackResponse.trim()));
		sense.setFollowupPrompt(Utils.sanitize(followupPrompt.trim()));
		
		sense.setAutoExit(autoExit);
		
		stopPhrases.replace(",", "\n");
		TextStream stream = new TextStream(stopPhrases.trim());
		sense.setStopPhrases(new ArrayList<String>());
		while (!stream.atEnd()) {
			String rss = Utils.sanitize(stream.upToAny("\n").trim());
			sense.getStopPhrases().add(rss);
			stream.skip();
			stream.skipWhitespace();
		}
		
		sense.saveProperties();
	}
	
	public void setDefaults() {
		Alexa sense = getBot().awareness().getSense(Alexa.class);
		
		sense.setLaunchResponse("Hello, say 'start' to begin.");
		sense.setHelpResponse("Say 'start' to begin or 'exit' to exit.");
		sense.setCancelResponse("Goodbye.");
		sense.setStopResponse("Goodbye.");
		sense.setFallbackResponse("Say 'start' to begin or 'exit' to exit.");
		sense.setFollowupPrompt("Ask another question or say 'exit' to end the conversation.");

		sense.setStopPhrases(new ArrayList<String>());
		sense.getStopPhrases().add("bye");
		sense.getStopPhrases().add("goodbye");
		sense.getStopPhrases().add("exit");
		sense.getStopPhrases().add("quit");
		sense.getStopPhrases().add("stop");
		sense.getStopPhrases().add("cancel");
		
		sense.saveProperties();
	}
	
	public String getWebhook() {
		String hook = null;
		if (Site.HTTPS) {
			hook = Site.SECUREURLLINK + "/rest/api/alexa/";
		} else {
			hook = Site.URLLINK + "/rest/api/alexa/";
		}
		if (getUser().getApplicationId() == null) {
			getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
		}
		hook = hook + getUser().getApplicationId();
		hook = hook + "/" + getBotBean().getInstanceId();
		return hook;
	}
	
	public void clear() {
		Alexa sense = getBot().awareness().getSense(Alexa.class);
		sense.setLaunchResponse("");

		sense.saveProperties();
	}
	
	@Override
	public void disconnectInstance() {
		disconnect();
	}
	
	@Override
	public void disconnect() {
	}

	public void disable() {
		getBot().awareness().getSense(Alexa.class).setIsEnabled(false);
		getBotBean().setInstance(AdminDatabase.instance().updateInstanceAlexa(getBotBean().getInstance().getId(), false));
	}
	
	public String getLaunchResponse() {
		return getBot().awareness().getSense(Alexa.class).getLaunchResponse();
	}
	
	public String getHelpResponse() {
		return getBot().awareness().getSense(Alexa.class).getHelpResponse();
	}
	
	public String getCancelResponse() {
		return getBot().awareness().getSense(Alexa.class).getCancelResponse();
	}
	
	public String getStopResponse() {
		return getBot().awareness().getSense(Alexa.class).getStopResponse();
	}
	
	public String getFallbackResponse() {
		return getBot().awareness().getSense(Alexa.class).getFallbackResponse();
	}
	
	public String getFollowupPrompt() {
		return getBot().awareness().getSense(Alexa.class).getFollowupPrompt();
	}
	
	public boolean getAutoExit() {
		return getBot().awareness().getSense(Alexa.class).getAutoExit();
	}
	
	public String getStopPhrases() {
		StringWriter writer = new StringWriter();
		Iterator<String> iterator = getBot().awareness().getSense(Alexa.class).getStopPhrases().iterator();
		while (iterator.hasNext()) {
			writer.write(iterator.next());
			if (iterator.hasNext()) {
				writer.write("\n");				
			}
		}
		return writer.toString();
	}
}

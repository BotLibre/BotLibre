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

import org.botlibre.sense.sms.Twilio;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;

public class SMSBean extends ServletBean {
	
	public SMSBean() {
	}

	public String getSid() {
		return getBot().awareness().getSense(Twilio.class).getSid();
	}

	public String getPhone() {
		return getBot().awareness().getSense(Twilio.class).getPhone();
	}

	public String getSecret() {
		return getBot().awareness().getSense(Twilio.class).getSecret();
	}

	public void save(String sid, String secret, String phone) {
		sid = Utils.sanitize(sid);
		secret = Utils.sanitize(secret);
		phone = Utils.sanitize(phone);
		Twilio sense = getBot().awareness().getSense(Twilio.class);
		sense.setSid(sid.trim());
		sense.setSecret(secret.trim());
		phone = phone.replace("-", "");
		phone = phone.replace(" ", "");
		sense.setPhone(phone.trim());
		sense.saveProperties();
	}
	
	public String getWebhook() {
		String hook = null;
		if (Site.HTTPS) {
			hook = Site.SECUREURLLINK + "/rest/api/twilio/";
		} else {
			hook = Site.URLLINK + "/rest/api/twilio/";
		}
		if (getUser().getApplicationId() == null) {
			getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
		}
		hook = hook + getUser().getApplicationId();
		hook = hook + "/" + getBotBean().getInstanceId();
		return hook;
	}
	
	public String getVoiceWebhook() {
		String hook = null;
		if (Site.HTTPS) {
			hook = Site.SECUREURLLINK + "/rest/api/twilio/voice/";
		} else {
			hook = Site.URLLINK + "/rest/api/twilio/voice/";
		}
		if (getUser().getApplicationId() == null) {
			getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
		}
		hook = hook + getUser().getApplicationId();
		hook = hook + "/" + getBotBean().getInstanceId();
		return hook;
	}

	public void clear() {
		Twilio sense = getBot().awareness().getSense(Twilio.class);
		sense.setSid("");
		sense.setSecret("");
		sense.setPhone("");
		sense.saveProperties();
	}

	@Override
	public void disconnectInstance() {
		disconnect();
	}

	@Override
	public void disconnect() {
	}
}

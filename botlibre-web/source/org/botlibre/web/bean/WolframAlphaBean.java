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

import org.botlibre.sense.wolframalpha.WolframAlpha;
import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;

public class WolframAlphaBean extends ServletBean {

	public WolframAlphaBean() {
	}
	
	public String getAppId() {
		return getBot().awareness().getSense(WolframAlpha.class).getAppId();
	}
	
	public void connect() throws Exception {

		getBotBean().setInstance(AdminDatabase.instance().updateInstanceWolframAlpha(getBotBean().getInstance().getId(), true));
	}
	
	public void save(String appId) {
		appId = Utils.sanitize(appId);
		
		WolframAlpha sense = getBot().awareness().getSense(WolframAlpha.class);
		sense.setAppId(appId.trim());
		
		sense.saveProperties();
	}
	
	public void clear() {
		WolframAlpha sense = getBot().awareness().getSense(WolframAlpha.class);
		sense.setAppId("");

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
		getBot().awareness().getSense(WolframAlpha.class).setIsEnabled(false);
		
		getBotBean().setInstance(AdminDatabase.instance().updateInstanceWolframAlpha(getBotBean().getInstance().getId(), false));
	}
}

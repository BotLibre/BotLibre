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

import org.botlibre.sense.wechat.WeChat;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;

public class WeChatBean extends ServletBean {
	
	public WeChatBean() {
	}
	
	public String getUserToken() {
		return getBot().awareness().getSense(WeChat.class).getUserToken();
	}
	
	public String getAppId() {
		return getBot().awareness().getSense(WeChat.class).getAppId();
	}
	
	public String getAppPassword() {
		return getBot().awareness().getSense(WeChat.class).getAppPassword();
	}
	
	public Boolean getInternational() {
		return getBot().awareness().getSense(WeChat.class).getInternational();
	}
	
	public String getMenu() {
		return getBot().awareness().getSense(WeChat.class).getMenu();
	}
	
	public void connect() throws Exception {

		getBotBean().setInstance(AdminDatabase.instance().updateInstanceWeChat(getBotBean().getInstance().getId(), true));
	}
	
	public void save(String userToken, String appId, String appPassword, Boolean international, String menu) {
		userToken = Utils.sanitize(userToken);
		appId = Utils.sanitize(appId);
		menu = Utils.sanitize(menu);
		
		/*if (!getBotBean().getInstance().isAdult() && Utils.checkProfanity(autoPosts)) {
			throw BotException.offensive();
		}*/
		WeChat sense = getBot().awareness().getSense(WeChat.class);
		sense.setUserToken(userToken.trim());
		sense.setAppId(appId.trim());
		sense.setAppPassword(appPassword.trim());
		sense.setInternational(international);
		
		boolean newMenu = !sense.getMenu().trim().equals(menu.trim());
		sense.setMenu(menu.trim());
		
		sense.saveProperties();
		
		if (newMenu) {
			sense.updateMenu(menu.trim());
		}
	}
	
	public String getWebhook() {
		String hook = null;
		if (Site.HTTPS) {
			hook = Site.SECUREURLLINK + "/rest/api/wechat/";
		} else {
			hook = Site.URLLINK + "/rest/api/wechat/";
		}
		if (getUser().getApplicationId() == null) {
			getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
		}
		hook = hook + getUser().getApplicationId();
		hook = hook + "/" + getBotBean().getInstanceId();
		return hook;
	}
	
	public void clear() {
		WeChat sense = getBot().awareness().getSense(WeChat.class);
		sense.setUserToken("");
		sense.setAppId("");
		sense.setAppPassword("");
		sense.setInternational(false);

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
		WeChat wechat = getBot().awareness().getSense(WeChat.class);
		wechat.checkProfile();
	}

	public void disable() {
		getBot().awareness().getSense(WeChat.class).setIsEnabled(false);
		
		getBotBean().setInstance(AdminDatabase.instance().updateInstanceWeChat(getBotBean().getInstance().getId(), false));
	}
}

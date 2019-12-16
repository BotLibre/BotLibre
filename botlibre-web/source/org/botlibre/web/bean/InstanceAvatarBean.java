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

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.ClientType;

public class InstanceAvatarBean extends ServletBean {
	
	public InstanceAvatarBean() {
	}

	/**
	 * Disconnect from the Bot instance.
	 */
	@Override
	public void disconnectInstance() {
		disconnect();
	}
	
	public void chooseAvatar(String choice) {
		try {
			if (choice == null) {
				choice = "";
			}
			if (choice.indexOf(':') != -1) {
				choice = choice.substring(0, choice.indexOf(':'));
			}
			choice = choice.trim();
			if (choice.isEmpty()) {
				getBotBean().setInstance(AdminDatabase.instance().updateInstanceAvatar(getBotBean().getInstance().getId(), null));
				return;
			}
			AvatarBean avatarBean = getLoginBean().getBean(AvatarBean.class);
			if (!avatarBean.validateInstance(choice)) {
				return;
			}
			avatarBean.incrementConnects(ClientType.WEB);

			getBotBean().setInstance(AdminDatabase.instance().updateInstanceAvatar(getBotBean().getInstance().getId(), avatarBean.getInstance().getId()));
		} catch (Exception failed) {
			error(failed);
		}
	}
	
	public String getAvatarText() {
		BotInstance instance = getBotBean().getInstance();
		if (instance.getInstanceAvatar() == null) {
			return "";
		}
		return instance.getInstanceAvatar().getId() + " : " + instance.getInstanceAvatar().getName();
	}
	
}

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
package org.botlibre.web.service;

import java.util.logging.Level;

import org.botlibre.Bot;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;

public class BotManager extends InstanceManager<Bot> {
	protected static BotManager instance = new BotManager();

	public static BotManager manager() {
		return instance;
	}
	
	public BotManager() {
		this.maxSize = Site.MAX_BOT_CACHE_SIZE;
	}
	
	public void shutdown(Bot instance) {
		try {
			instance.pool();
		} catch (Throwable exception) {
			AdminDatabase.instance().log(exception);
		}
	}
	
	public void forceShutdown(String name) {
		for (InstanceInfo info : this.instances.values()) {
			if (info.instance.memory().getMemoryName().equals(name)) {
				AdminDatabase.instance().log(Level.WARNING, "forced shutdown", info.instance);
				removeInstance(info.key);
				info.instance.shutdown();
			}
		}
	}
	
}

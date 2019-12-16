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

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.factories.SessionManager;
import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.knowledge.database.DatabaseMemory;
import org.botlibre.thought.forgetfulness.Forgetfulness;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.bean.IRCBean;

public class ForgetfulnessService extends Service {
	public static int MAX_CACHE_SIZE = 200000;
	public static int SLEEP = 1000 * 60 * 5; // 5 minutes
	public static int START = 1; // 1am
	protected static ForgetfulnessService instance = new ForgetfulnessService();
	
	public static Timestamp lastRun;

	public static void freeMemory() {
		AdminDatabase.instance().log(Level.WARNING, "Low memory detected, clearing all caches");
		Stats.stats.memoryFrees++;
		AdminDatabase.instance().log(Level.WARNING, "Clearing instance cache");
		BotManager.manager().clearCache();
		// Reduce size to free memory.
		BotManager.manager().setMaxSize((int)(Site.MAX_BOT_CACHE_SIZE * 0.8));
		AdminDatabase.instance().log(Level.WARNING, "Clearing bean cache");
		BeanManager.manager().clearCache();
		AdminDatabase.instance().log(Level.WARNING, "Clearing instance pool");
		Bot.clearPool();

		AdminDatabase.instance().log(Level.WARNING, "Clearing instance caches");
		for (DatabaseMemory.SessionInfo info : DatabaseMemory.sessions.values()) {
			if (info.session != null) {
				info.session.getIdentityMapAccessor().initializeAllIdentityMaps();
			}
		}
		for (Object value : SessionManager.getManager().getSessions().values()) {
			((Session)value).getIdentityMapAccessor().initializeAllIdentityMaps();
		}

		AdminDatabase.instance().log(Level.WARNING, "Running GC");
		System.gc();
		AdminDatabase.instance().log(Level.WARNING, "Running finalization");
		System.runFinalization();
		Utils.sleep(5000);
		Bot.clearPool();
		AdminDatabase.instance().log(Level.WARNING, "Done");
	}
	
	public ForgetfulnessService() {
	}

	public void startChecking() {
		setEnabled(true);
		this.checker = new Thread() {
			@Override
			public void run() {
				try {
					while (isEnabled()) {
						lastRun = new Timestamp(System.currentTimeMillis());
						AdminDatabase.instance().log(Level.INFO, "Current memory (total, free)", Runtime.getRuntime().totalMemory(), Runtime.getRuntime().freeMemory());
						if (Utils.checkLowMemory()) {
							IRCBean.rooms.clear();
							freeMemory();
						}
						Calendar calendar = Calendar.getInstance();
						if (calendar.get(Calendar.HOUR_OF_DAY) == 1) {
							runForgetfullness(ForgetfulnessService.this);
						}
						Utils.sleep(SLEEP);
						if (checker != this) {
							break;
						}
					}
				} catch (Throwable exception) {
					AdminDatabase.instance().log(Level.SEVERE, "Forgetfulness checker failure");
					AdminDatabase.instance().log(exception);
				}
				AdminDatabase.instance().log(Level.INFO, "Forgetfulness checker stopped");
			}
		};
		AdminDatabase.instance().log(Level.INFO, "Forgetfulness checker running");
		this.checker.start();
	}

	public static ForgetfulnessService instance() {
		return instance;
	}
	
	public static void runForgetfullness(ForgetfulnessService service) {
		IRCBean.rooms.clear();
		long start = System.currentTimeMillis();
		try {
			AdminDatabase.instance().log(Level.INFO, "Checking forgetfulness");
			if (Utils.checkLowMemory()) {
				freeMemory();
			}
			// Check cache first.
			try {
				Bot cache = Bot.getSystemCache();
				if (cache != null) {
					//bot.setDebugLevel(Level.FINE);
					Forgetfulness forgetfulness = cache.mind().getThought(Forgetfulness.class);
					forgetfulness.setMaxSize(MAX_CACHE_SIZE);
					Network memory = cache.memory().newMemory();
					forgetfulness.forget(memory, true);
					Utils.sleep(1000);
				}
			} catch (Throwable exception) {
				AdminDatabase.instance().log(Level.SEVERE, "Forgetfulness checker failure");  
				AdminDatabase.instance().log(exception);
			}
			
			List<BotInstance> instances = AdminDatabase.instance().getAllOverLimitInstances();
			for (BotInstance instance : instances) {
				try {
					if (!service.isEnabled()) {
						break;
					}
					Bot bot = service.connectInstance(instance);
					try {
						AdminDatabase.instance().addError(instance, "Bot max memory exceeded - " + bot.memory().getLongTermMemory().size() + " : " + instance.getMemoryLimit());
						//bot.setDebugLevel(Level.FINE);
						Forgetfulness forgetfulness = bot.mind().getThought(Forgetfulness.class);
						Network memory = bot.memory().newMemory();
						forgetfulness.forget(memory, true);
						bot.memory().getLongTermMemory().clear();
						AdminDatabase.instance().updateInstanceSize(instance, bot.memory().getLongTermMemory().size());
						AdminDatabase.instance().addError(instance, "Bot memory reduced to - " + bot.memory().getLongTermMemory().size());
					} finally {
						bot.shutdown();
					}
					Utils.sleep(1000);
				} catch (Throwable exception) {
					AdminDatabase.instance().log(Level.SEVERE, "Forgetfulness failure", instance.getName());
					AdminDatabase.instance().log(exception);
				}
			}
			if (Utils.checkLowMemory()) {
				freeMemory();
			}
		} catch (Throwable exception) {
			AdminDatabase.instance().log(Level.SEVERE, "SEVERE");
			AdminDatabase.instance().log(exception);
		}
		long time = (System.currentTimeMillis() - start) / 1000;
		AdminDatabase.instance().log(Level.INFO, "Done checking forgetfulness", time);
	}
	
}

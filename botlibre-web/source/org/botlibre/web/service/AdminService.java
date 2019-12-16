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

import java.util.Calendar;
import java.util.logging.Level;

import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;

public class AdminService extends Service {
	public static int SLEEP = 1000 * 60 * 10; // 10 minutes
	public static int START = 1; // 1am
	protected static AdminService instance = new AdminService();
	
	public AdminService() {
	}

	public void startChecking() {
		setEnabled(true);
	    this.checker = new Thread() {
	    	@Override
	    	public void run() {
				try {
	    			while (isEnabled()) {
	    				Calendar calendar = Calendar.getInstance();
	    				if (calendar.get(Calendar.HOUR_OF_DAY) == 1) {
		    				long start = System.currentTimeMillis();
		    				try {
		    	    			AdminDatabase.instance().log(Level.INFO, "Checking admin");
		    	    			AdminDatabase.instance().checkDailyReset();
		    				} catch (Throwable exception) {
		    	    			AdminDatabase.instance().log(Level.INFO, "Admin exception");
		    					AdminDatabase.instance().log(exception);
		    				}
		    				long time = (System.currentTimeMillis() - start) / 1000;
			    			AdminDatabase.instance().log(Level.INFO, "Done checking admin", time);
	    				}
	    	    		Utils.sleep(SLEEP);
	    	    		if (checker != this) {
	    	    			break;
	    	    		}
	    			}
				} catch (Throwable exception) {
	    			AdminDatabase.instance().log(Level.SEVERE, "Admin checker failure");  
					AdminDatabase.instance().log(exception);
				}
    			AdminDatabase.instance().log(Level.INFO, "Admin checker stopped");   		
	    	}
	    };
	    AdminDatabase.instance().log(Level.INFO, "Admin checker running");
	    this.checker.start();
	}

	public static AdminService instance() {
		return instance;
	}
	
}

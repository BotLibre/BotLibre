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

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.bean.ServletBean;


public class BeanManager extends InstanceManager<ServletBean> {
	protected static BeanManager instance = new BeanManager();

	public static BeanManager manager() {
		return instance;
	}
	
	public BeanManager() {
		this.maxSize = 200;
	}
	
	public void shutdown(ServletBean instance) {
		try {
			instance.getLoginBean().disconnect();
		} catch (Throwable exception) {
			AdminDatabase.instance().log(exception);
		}
	}
	
}

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

import java.util.HashMap;
import java.util.Map;

import org.botlibre.web.service.BeanManager;

public class SessionProxyBean {

	protected Long beanId;
	protected Long lastBeanId;
	protected boolean redirect;
	protected String instanceId;
	protected Map<Long, Map<String, String>> properties;
	
	public SessionProxyBean() {
	}
	
	public void addProperties(Long proxy, Map<String,String> local) {
		if (this.properties == null) {
			this.properties = new HashMap<Long, Map<String,String>>();
		}
		this.properties.put(proxy, local);
	}
	
	public Map<String,String> getProperties(Long proxy) {
		if (this.properties == null) {
			return null;
		}
		return this.properties.get(proxy);
	}
	
	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public boolean isProxy() {
		return beanId != null;
	}

	public LoginBean getLoginBean() {
		LoginBean bean = null;
		if (this.beanId != null) {
			bean = (LoginBean)BeanManager.manager().getInstance(this.beanId);
		}
		if (bean == null) {
			bean = new LoginBean();
			this.beanId = null;
			this.beanId = BeanManager.manager().addInstance(bean);
			this.lastBeanId = this.beanId;
		}
		return bean;
	}

	public LoginBean checkLoginBean() {
		LoginBean bean = null;
		if (this.beanId != null) {
			bean = (LoginBean)BeanManager.manager().getInstance(this.beanId);
		}
		return bean;
	}

	public LoginBean checkLoginBean(LoginBean loginBean) {
		if (this.beanId == null) {
			return loginBean;
		}
		LoginBean bean = (LoginBean)BeanManager.manager().getInstance(this.beanId);
		if (bean != null) {
			return bean;
		}
		if (loginBean == null) {
			return null;
		}
		bean = (LoginBean)loginBean.clone();
		this.beanId = null;
		this.beanId = BeanManager.manager().addInstance(bean);
		this.lastBeanId = this.beanId;
		return bean;
	}

	public LoginBean cloneLoginBean(LoginBean loginBean) {
		if (this.beanId != null) {
			LoginBean bean = (LoginBean)BeanManager.manager().getInstance(this.beanId);
			if (bean != null) {
				return bean;
			}
		}
		if (loginBean == null) {
			loginBean = new LoginBean();
		}
		LoginBean bean = (LoginBean)loginBean.clone();
		this.beanId = null;
		this.beanId = BeanManager.manager().addInstance(bean);
		this.lastBeanId = this.beanId;
		return bean;
	}

	public void setLoginBean(LoginBean loginBean) {
		if (this.beanId != null) {
			LoginBean bean = (LoginBean)BeanManager.manager().getInstance(this.beanId);
			if (bean == loginBean) {
				return;
			}
		}
		this.beanId = BeanManager.manager().addInstance(loginBean);
		this.lastBeanId = this.beanId;
	}

	public Long getBeanId() {
		return beanId;
	}
	
	public String proxyString() {
		//if (this.beanId == null) {
			return "";
		//}
		//return "&proxy=" + getBeanId();
	}
	
	public String proxyInput() {
		//if (this.beanId == null) {
			return "";
		//}
		//return "<input name=\"proxy\" type=\"hidden\" value=\"" + getBeanId() + "\"/>";
	}

	public void clear() {
		this.beanId = null;
	}
	
	public void setBeanId(Long beanId) {
		this.beanId = beanId;
		if (beanId != null) {
			this.lastBeanId = this.beanId;
		}
	}

	public boolean isRedirect() {
		return redirect;
	}

	public void setRedirect(boolean redirect) {
		this.redirect = redirect;
	}

	public Long getLastBeanId() {
		return lastBeanId;
	}

	public void setLastBeanId(Long lastBeanId) {
		this.lastBeanId = lastBeanId;
	}
}

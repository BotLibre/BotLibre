/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
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

package org.botlibre.sdk;

/**
 * Credential used to establish a connection.
 * Requires the url, and an application id.
 * You can obtain your application id from your user details page on the hosting website.
 */
public class Credentials {
	public String host = "";
	public String app = "";
	public String url = "";
	/**
	 * Your application's unique identifier.
	 * You can obtain your application id from your user details page on the hosting website.
	 */
	public String applicationId = "";

	public Credentials() {
	}

	/**
	 * Creates a new credentials for the service host url, and the application id.
	 */
	public Credentials(String url, String applicationId) {
		this.url = url;
		this.applicationId = applicationId;
	}

	public String getUrl() {
		return url;
	}
	
	/**
	 * Sets the hosted service server url, i.e. http://www.paphuslivechat.com
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	public String getHost() {
		return host;
	}

	/**
	 * Sets the server host name, i.e. www.paphuslivechat.com
	 */
	public void setHost(String host) {
		this.host = host;
	}

	public String getApp() {
		return app;
	}

	/**
	 * Sets an app url postfix, this is normally not required, i.e. "".
	 */
	public void setApp(String app) {
		this.app = app;
	}

	/**
	 * Returns your application's unique identifier.
	 */
	public String getApplicationId() {
		return applicationId;
	}

	/**
	 * Sets your application's unique identifier.
	 * You can obtain your application id from your user details page on the hosting website.
	 */
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
}
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
 * Credentials for use with hosted services on the BOT libre website, a free bot hosting service.
 * http://www.botlibre.com
 */
public class BOTlibreCredentials extends Credentials {
	public static String DOMAIN = "www.botlibre.com";
	//public static String DOMAIN = "192.168.0.11:9080";
	public static String APP = "";
	//public static String APP = "/botlibre";
	public static String PATH = "/rest/botlibre";

	public BOTlibreCredentials(String applicationId) {
		this.host = DOMAIN;
		this.app = APP;
		this.url = "http://" + DOMAIN + APP + PATH;
		this.applicationId = applicationId;
	}
}
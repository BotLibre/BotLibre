/******************************************************************************
 *
 *  Copyright 2016 Paphus Solutions Inc.
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
package org.botlibre.sense.google;

import java.util.HashMap;
import java.util.Map;

import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.sense.http.Http;
import org.botlibre.util.Utils;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;


/**
 * Sense to connect with Google API services.
 */

public abstract class Google extends Http {
	public static String KEY = "";
	public static String CLIENTID = "";
	public static String CLIENTSECRET = "";
	
	private String refreshToken;	
	
	public Google(boolean enabled) {
		this.isEnabled = enabled;
	}
	
	public Google() {
		this(false);
	}
	
	/**
	 * Fetch properties.
	 */
	@Override
	public void awake() {
		this.refreshToken = this.bot.memory().getProperty(getClass().getSimpleName() + ".refreshToken");
		if (this.refreshToken != null && !this.refreshToken.isEmpty()) {
			this.refreshToken = Utils.decrypt(Utils.KEY, this.refreshToken);
		}
		if (this.refreshToken == null) {
			this.refreshToken = "";
		}
	}

	public void saveProperties() {
		Network memory = getBot().memory().newMemory();
		if (this.refreshToken == null || this.refreshToken.isEmpty()) {
			memory.saveProperty(getClass().getSimpleName() + ".refreshToken", "", true);
		} else {
			memory.saveProperty(getClass().getSimpleName() + ".refreshToken", Utils.encrypt(Utils.KEY, this.refreshToken), true);
		}
		memory.save();
	}
	
	public boolean isAuthorized() {
		return this.refreshToken != null && !this.refreshToken.isEmpty();
	}
	
	public String getAuthURL() {
		return "https://accounts.google.com/o/oauth2/auth?client_id=" + CLIENTID + "&redirect_uri=urn:ietf:wg:oauth:2.0:oob&scope=https://www.googleapis.com/auth/calendar&response_type=code";
	}
	
	public void resetRefreshToken(String authCode) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("code", authCode);
        params.put("client_id", CLIENTID);
        params.put("client_secret", CLIENTSECRET);
        params.put("redirect_uri", "urn:ietf:wg:oauth:2.0:oob");
        params.put("grant_type", "authorization_code");
        String json = Utils.httpPOST("https://accounts.google.com/o/oauth2/token", params);
		JSON root = (JSON)JSONSerializer.toJSON(json);
		if (!(root instanceof JSONObject)) {
			throw new BotException("Invalid response");
		}
		this.refreshToken = ((JSONObject)root).getString("refresh_token");
	}
	
	public String getGoogleAccountId() {
		try {
			String accessToken = newAccessToken();
			String json = Utils.httpGET("https://www.googleapis.com/calendar/v3/calendars/primary?" + "&access_token=" + accessToken);
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json);
	        return root.getString("id");
		} catch (Exception exception) {
			return null;
		}
	}

	public String newAccessToken() {
		try {
	        Map<String, String> params = new HashMap<String, String>();
	        params.put("refresh_token", this.refreshToken);
	        params.put("client_id", CLIENTID);
	        params.put("client_secret", CLIENTSECRET);
	        //params.put("redirect_uri", "urn:ietf:wg:oauth:2.0:oob");
	        params.put("grant_type", "refresh_token");
	        String json = Utils.httpPOST("https://accounts.google.com/o/oauth2/token", params);
			JSON root = (JSON)JSONSerializer.toJSON(json);
			if (!(root instanceof JSONObject)) {
				return null;
			}
			return ((JSONObject)root).getString("access_token");
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}

	/**
	 * Send a DELETE request the URL.
	 */
	@Override
	public Vertex delete(String url, Network network) {
		String accessToken = newAccessToken();
		if (!url.contains("?")) {
			url = url + "?";
		}
		return super.delete(url + "&access_token=" + accessToken, network);
	}
	
	/**
	 * Return the JSON data object from the URL.
	 */
	@Override
	public Vertex requestJSON(String url, String attribute, Map<String, String> headers, Network network) {
		String accessToken = newAccessToken();
		if (!url.contains("?")) {
			url = url + "?";
		}
		return super.requestJSON(url + "&access_token=" + accessToken, attribute, headers, network);
	}
	
	/**
	 * Return the JSON data object from the URL.
	 */
	@Override
	public int countJSON(String url, String attribute, Network network) {
		String accessToken = newAccessToken();
		if (!url.contains("?")) {
			url = url + "?";
		}
		return super.countJSON(url + "&access_token=" + accessToken, attribute, network);
	}

	/**
	 * POST the JSON object and return the JSON data from the URL.
	 */
	@Override
	public Vertex postJSON(String url, Vertex jsonObject, Map<String, String> headers, Network network) {
		String accessToken = newAccessToken();
		if (!url.contains("?")) {
			url = url + "?";
		}
		return super.postJSON(url + "&access_token=" + accessToken, jsonObject, headers, network);
	}

	/**
	 * PUT the JSON object and return the JSON data from the URL.
	 */
	@Override
	public Vertex putJSON(String url, Vertex jsonObject, Network network) {
		String accessToken = newAccessToken();
		if (!url.contains("?")) {
			url = url + "?";
		}
		return super.putJSON(url + "&access_token=" + accessToken, jsonObject, network);
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
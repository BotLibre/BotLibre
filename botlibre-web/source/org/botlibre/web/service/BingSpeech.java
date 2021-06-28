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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.botlibre.BotException;
import org.botlibre.util.Utils;
import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;

public class BingSpeech {
	public static int MAX_SIZE = 800;
	public static int MAX_FILE_NAME_SIZE = 200;
	public static String BING_SPEECH_URL = "https://speech.platform.bing.com/synthesize";
	public static String MICROSOFT_SPEECH_URL = ".tts.speech.microsoft.com/cognitiveservices/v1";
	
	private static Map<String, String> tokens = new ConcurrentHashMap<>();
	
	public static synchronized boolean speak(String voice, String text, String file, String apiKey, String token, String apiEndpoint, boolean useOldUrl) {
		if ((text == null) || text.isEmpty()) {
			return false;
		}
		if (text.length() > MAX_SIZE) {
			text = text.substring(0, MAX_SIZE);
		}
		if ((apiKey == null || apiKey.isEmpty()) && Site.COMMERCIAL) {
			if (Stats.stats.speechAPI > Site.MAX_SPEECH_API) {
				return false;
			}
			Stats.stats.speechAPI++;
			apiKey = Site.MICROSOFT_SPEECH_KEY;
			apiEndpoint = Site.MICROSOFT_SPEECH_ENDPOINT;
		}
		if (token == null && apiKey != null && !apiKey.isEmpty()) {
			token = tokens.get(apiKey);
			if (token == null) {
				token = BingSpeech.getToken(apiKey, apiEndpoint);
				if (token != null) {
					token = tokens.put(apiKey, token);
				}
			}
		}
		if (apiKey == null || apiKey.isEmpty() || token == null) {
			return false;
		}
		boolean result = speak2(voice, text, file, apiKey, token, apiEndpoint, useOldUrl);
		if (!result) {
			// Try to get new token, then retry request.
			token = BingSpeech.getToken(apiKey, apiEndpoint);
			if (token == null) {
				return false;
			} else {
				token = tokens.put(apiKey, token);
				result = speak2(voice, text, file, apiKey, token, apiEndpoint, useOldUrl);
			}
		}
		return result;
	}
	
	public static synchronized boolean speak2(String voice, String text, String file, String apiKey, String token, String apiEndpoint, boolean useOldUrl) {
		try {
			HttpResponse response = null;

			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", "Bearer " + token);
			headers.put("X-Microsoft-OutputFormat", "audio-16khz-128kbitrate-mono-mp3");
			
			if (useOldUrl) {
				response = Utils.httpPOSTReturnResponse(BING_SPEECH_URL, "application/ssml+xml", "<speak version='1.0' xml:lang='en-US'><voice name='Microsoft Server Speech Text to Speech Voice (" + voice + ")'>" + text + "</voice></speak>", headers);
			} else {
				String region = apiEndpoint.substring(apiEndpoint.indexOf("://"), apiEndpoint.indexOf('.'));
				response = Utils.httpPOSTReturnResponse("https" + region + MICROSOFT_SPEECH_URL, "application/ssml+xml", "<speak version='1.0' xml:lang='en-US'><voice name='Microsoft Server Speech Text to Speech Voice (" + voice + ")'>" + text + "</voice></speak>", headers);
			}
			if (response != null) {
				try {
					if ((response.getStatusLine().getStatusCode() < 200) || (response.getStatusLine().getStatusCode() > 302)) {
						if (!useOldUrl) {
							return speak2(voice, text, file, apiKey, token, apiEndpoint, true);
						} else {
							throw new Exception(response.toString());
						}
					}
					File path = new File(file);
					new File(path.getParent()).mkdirs();
					InputStream inputStream = response.getEntity().getContent();
					FileOutputStream outputStream = new FileOutputStream(path);
					int size = 0;
					byte[] readBuffer = new byte[32768];
					while ((size = inputStream.read(readBuffer)) > 0) {
						outputStream.write(readBuffer, 0, size);
					}
					outputStream.close();
					inputStream.close();
				} finally {
					EntityUtils.consume(response.getEntity());
				}
			} else {
				return false;
			}

		} catch (Exception exception) {	
			AdminDatabase.instance().log(exception);
			return false;
		}
		return true;
	}
	
	public static String getToken(String apiKey, String apiEndpoint) {
		String response = null;
		
		try {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Ocp-Apim-Subscription-Key", apiKey);
			response = Utils.httpPOST(apiEndpoint, "application/x-www-form-urlencoded", "", headers);
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
		}
		return response;
	}
}

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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;
import com.sun.jersey.core.util.Base64;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class QQSpeech {
	
	public static int MAX_SIZE = 800;
	public static int MAX_FILE_NAME_SIZE = 200;
	public static String QQ_SPEECH_URL = "https://api.ai.qq.com/fcgi-bin/aai/aai_tts";
	
	public static synchronized boolean speak(String voice, String text, String file, String apiKey, String appId) {
		if ((text == null) || text.isEmpty()) {
			return false;
		}
		if (text.length() > MAX_SIZE) {
			text = text.substring(0, MAX_SIZE);
		}
		try {
			
			TreeMap<String, String> params = new TreeMap<String, String>();
			params.put("app_id", appId);
			params.put("speaker", voice);
			params.put("format", "3");
			params.put("volume", "0");
			params.put("speed", "100");
			params.put("text", text);
			params.put("aht", "0");
			params.put("apc", "58");
			params.put("time_stamp", String.valueOf(new Date().getTime() / 1000));
			params.put("nonce_str", UUID.randomUUID().toString().replace("-", ""));
			
			params.put("sign", getQQRequestSignature(apiKey, appId, params));
			
			String paramString = "";
			try {
				for (Map.Entry<String, String> entry : params.entrySet()) {	
					paramString = paramString.concat(entry.getKey()).concat("=").concat(URLEncoder.encode(entry.getValue(), "UTF-8")).concat("&");		
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			String response = Utils.httpPOST(QQ_SPEECH_URL, "application/x-www-form-urlencoded", paramString);
			
			JSONObject root = (JSONObject)JSONSerializer.toJSON(response);
			JSONObject data = root.getJSONObject("data");
			
			if(data!=null) {
				String speech = data.getString("speech");
				if(speech != null && !speech.isEmpty()) {
					File path = new File(file);
					new File(path.getParent()).mkdirs();
					
					byte[] audioData = Base64.decode(speech);
					FileOutputStream fos = new FileOutputStream(path);
				    fos.write(audioData);
				    fos.close();
				} else {
					throw new Exception(response);
				}
			} else {
				throw new Exception(response);
			}
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
			return false;
		}
		return true;
	}
	
	private static String getQQRequestSignature(String appKey, String appId, TreeMap<String, String> params) {
		String paramString = "";
		
		//Splice parameter pairs into a url-encoded string
		try {
			for (Map.Entry<String, String> entry : params.entrySet()) {	
				paramString = paramString.concat(entry.getKey()).concat("=").concat(URLEncoder.encode(entry.getValue(), "UTF-8")).concat("&");		
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		//Concatenate app key
		paramString = paramString.concat("app_key=").concat(appKey);
		
		//Perform MD5 operation
		String signature = "";
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(paramString.getBytes());
			byte[] signatureBytes = md.digest();
			signature = DatatypeConverter.printHexBinary(signatureBytes).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return signature;	
	}
}

package org.botlibre.test;

import java.util.HashMap;
import java.util.Map;

import org.botlibre.util.Utils;

public class GoogleTest {
	static String clientId = "";
	static String clientSecret = "";
	static String redirectUri= "";
	
	static String accessToken = "";
	static String refreshtoken = "";
	
	static int step = 3;
	
	/** A new token is require per request. */
	static String authCode = "";
	
    public static void main(String[] args) throws Exception {
    	if (step == 0) {
	    	System.out.println("open this link in a web browser");
	        System.out.println("https://accounts.google.com/o/oauth2/auth?client_id="
	        		+ clientId
	        		+ "&redirect_uri=urn:ietf:wg:oauth:2.0:oob&scope=https://www.googleapis.com/auth/calendar.readonly&response_type=code");
    	} else if (step == 1) {
	        Map<String, String> params = new HashMap<String, String>();
	        params.put("code", authCode);
	        params.put("client_id", clientId);
	        params.put("client_secret", clientSecret);
	        params.put("redirect_uri", "urn:ietf:wg:oauth:2.0:oob");
	        params.put("grant_type", "authorization_code");
	        String result = Utils.httpPOST("https://accounts.google.com/o/oauth2/token", params);
        
	        System.out.println(result);
    	} else if (step == 2) {
	        Map<String, String> params = new HashMap<String, String>();
	        params.put("refresh_token", refreshtoken);
	        params.put("client_id", clientId);
	        params.put("client_secret", clientSecret);
	        //params.put("redirect_uri", "urn:ietf:wg:oauth:2.0:oob");
	        params.put("grant_type", "refresh_token");
	        String result = Utils.httpPOST("https://accounts.google.com/o/oauth2/token", params);
        
	        System.out.println(result);
    	} else if (step == 3) {
	        //String result = Utils.httpGET("https://www.googleapis.com/calendar/v3/calendars/primary?access_token=" + accessToken);
	        String result = Utils.httpGET("https://www.googleapis.com/calendar/v3/calendars/primary/events?timeMax=2016-10-05T00%3A00%3A00-07%3A00&timeMin=2016-10-06T00%3A00%3A00-07%3A00&access_token=" + accessToken);
        
	        System.out.println(result);
    	}
    	
    }

}

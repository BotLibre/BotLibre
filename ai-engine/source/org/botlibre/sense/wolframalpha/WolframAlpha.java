/******************************************************************************
 *
 *  Copyright 2017 Paphus Solutions Inc.
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

package org.botlibre.sense.wolframalpha;

import java.io.StringReader;
import java.net.URLEncoder;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.http.Http;
import org.botlibre.util.Utils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class WolframAlpha extends Http {
	
	protected static String URL_PREFIX = "https://api.wolframalpha.com/v1/result?i=";
	protected static String DOMAIN = "api.wolframalpha.com";
	
	protected String appId = "";
	
	public WolframAlpha(boolean enabled) {
		this.isEnabled = enabled;
	}
	
	public WolframAlpha() {
		this(false);
	}
	
	@Override
	public void awake() {
		super.awake();
		Http http = (Http)getBot().awareness().getSense(Http.class);
		http.getDomains().put(DOMAIN, this);

		String enabled = this.bot.memory().getProperty("WolframAlpha.enabled");
		if (enabled != null) {
			setIsEnabled(Boolean.valueOf(enabled));
		}
		
		this.appId = this.bot.memory().getProperty("WolframAlpha.appId");
		if (this.appId == null) {
			this.appId = "";
		}
	}
	
	public String getAppId() {
		return appId;
	}
	
	public void setAppId(String appId) {
		this.appId = appId;
		
	}
	
	public void saveProperties() {
		Network memory = getBot().memory().newMemory();
		memory.saveProperty("WolframAlpha.enabled", String.valueOf(isEnabled()), true);
		memory.saveProperty("WolframAlpha.appId", this.appId, true);
		memory.save();
	}
	
	/**
	 * Post, process the post request.
	 */
	@Override
	public void output(Vertex output) {
		
	}
	
	/**
	 * Self API
	 * Send a query to Wolfram Alpha
	 * Called from Self.
	 */
	public Vertex query(Vertex source, Vertex query) {
		if (this.appId == null || this.appId.trim().isEmpty()) {
			return null;
		}
		String text = query.getDataValue();
		if (text != null) {
			try {
				log("Lookup", Level.INFO, text);
				String url = URL_PREFIX + URLEncoder.encode(text, "UTF-8") + "&appid=" + this.appId;
				log("HTTP GET", Level.FINE, url);
				String result = Utils.httpGET(url);
				log("Result", Level.INFO, result);
				
				//DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    //DocumentBuilder builder = factory.newDocumentBuilder();
			    //InputSource input = new InputSource(new StringReader(result));
			    //Document xml = builder.parse(input);
			    
			    //String resultText = "";
			    
			    //if (xml != null && xml.getChildNodes().getLength() >= 0 && xml.getChildNodes().item(0).getChildNodes().getLength()>0) {
			    //	resultText = xml.getChildNodes().item(0).getChildNodes().item(1).getTextContent();
			    //}
			    
				Network network = query.getNetwork();
				Vertex definition = network.createVertex(result);
				return definition;
			} catch (Exception e) {
				log(e);
				return null;
			}
			
		}
		return null;
	}
}

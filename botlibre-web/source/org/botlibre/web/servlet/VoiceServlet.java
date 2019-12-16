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
package org.botlibre.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.Site;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.VoiceBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/voice")
@SuppressWarnings("serial")
public class VoiceServlet extends BeanServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PageStats.page(request);
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		LoginBean loginBean = (LoginBean)request.getSession().getAttribute("loginBean");
		if (loginBean == null) {
			response.sendRedirect("index.jsp");
			return;
		}
		BotBean botBean = loginBean.getBotBean();
		VoiceBean voiceBean = loginBean.getBean(VoiceBean.class);

		String save = (String)request.getParameter("save");
		String voice = request.getParameter("voice");
		String voiceMod = request.getParameter("voice-mod");
		String provider = (String)request.getParameter("provider");
		Boolean nativeVoice = !("botlibre".equals(provider));
		Boolean responsiveVoice = "responsive".equals(provider);
		String nativeVoiceName = request.getParameter("native-voice-name");
		String language = request.getParameter("language");
		Boolean bingSpeech = "bing".equals(provider);
		String bingSpeechApiKey = request.getParameter("bingSpeechApiKey");
		String bingSpeechApiEndpoint = request.getParameter("bingApiEndpoint");
		String bingSpeechVoice = request.getParameter("bingSpeechVoice");
		Boolean qqSpeech = "qq".equals(provider);
		String qqSpeechApiKey = request.getParameter("qqSpeechApiKey");
		String qqSpeechVoice = request.getParameter("qqSpeechVoice");
		String nativeSpeechApiKey = bingSpeech ? bingSpeechApiKey : qqSpeechApiKey;
		String nativeSpeechAppId = request.getParameter("qqSpeechAppId");
		
		try {
			String postToken = (String)request.getParameter("postToken");
			loginBean.verifyPostToken(postToken);
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (botBean.getInstance() == null || !String.valueOf(botBean.getInstanceId()).equals(instance)) {
					botBean.validateInstance(instance);
				}
			}
			voiceBean.setSpeakFileName(null);
			if (!botBean.isConnected() && (botBean.getInstance() == null || !botBean.getInstance().isExternal())) {
				response.sendRedirect("voice.jsp");
				return;
			}
			if (save != null) {
				String voiceName = nativeVoiceName;
				if(bingSpeech) { 
					voiceName = bingSpeechVoice; 
				}
				else if(qqSpeech) { 
					voiceName = qqSpeechVoice;
				}
				botBean.checkAdmin();
				voiceBean.save(voice, voiceMod, nativeVoice, responsiveVoice, language, voiceName, bingSpeech, nativeSpeechApiKey, qqSpeech, nativeSpeechAppId, bingSpeechApiEndpoint);
			
				if (responsiveVoice && !Site.COMMERCIAL) {
					throw new Exception("ResponsiveVoice is only offered through our commercial website botlibre.biz");
				}
				
				if (bingSpeech) { 
					if (nativeSpeechApiKey == null || nativeSpeechApiKey.isEmpty()) {
						throw new Exception("Microsoft Speech API key is required.");
					}
					if (bingSpeechApiEndpoint == null || bingSpeechApiEndpoint.isEmpty()) {
						throw new Exception("Microsoft Speech API endpoint is required.");
					}
				}
				else if (qqSpeech) { 
					if (nativeSpeechApiKey == null || nativeSpeechApiKey.isEmpty()) {
						throw new Exception("QQ Speech App Key is required.");
					}
					if (nativeSpeechAppId == null || nativeSpeechAppId.isEmpty()) {
						throw new Exception("QQ Speech App Id is required.");
					}
				}
			}
		} catch (Exception failed) {
			botBean.error(failed);
		}
		response.sendRedirect("voice.jsp");
	}
}

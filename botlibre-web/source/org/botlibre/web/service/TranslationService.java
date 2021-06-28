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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.botlibre.BotException;
import org.botlibre.util.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;


public class TranslationService {
	public static int MAX_SIZE = 10000;
	protected static TranslationService instance = new TranslationService();
	
	protected Map<TranslationId, String> translations = new ConcurrentHashMap<TranslationId, String>();
	protected Set<String> languages;
	
	public TranslationService() {
	}

	public static TranslationService instance() {
		return instance;
	}
	
	public String translate(String textOrId, String sourceLanguage, String targetLanguage) {
		TranslationId id = new TranslationId();
		id.text = textOrId;
		id.sourceLanguage = sourceLanguage;
		id.targetLanguage = targetLanguage;
		String translated = this.translations.get(id);
		if (translated == null) {
			Translation translation = AdminDatabase.instance().findTranslation(id);
			if (translation == null) {
				try {
					if (Stats.stats.translations > Site.MAX_TRANSLATION_API) {
						throw new BotException("Max translations");
					}
					// translate with appropriate api 
					translated = translateAPI(textOrId, sourceLanguage, targetLanguage);
					Stats.stats.translations++;
					if (translated != null) {
						translation = new Translation();
						translation.sourceLanguage = sourceLanguage;
						translation.targetLanguage = targetLanguage;
						translation.text = textOrId;
						translation.translation = translated;
						AdminDatabase.instance().updateTranslation(translation);
						cacheTranslation(id, translated);
						return translated;
					}
				} catch (Exception exception) {
					Stats.stats.translationErrors++;
					AdminDatabase.instance().log(exception);
				}
				cacheTranslation(id, "");
				return textOrId;
			}
			Stats.stats.cachedTranslations++;
			cacheTranslation(id, translation.translation);
			return translation.translation;
		}
		Stats.stats.cachedTranslations++;
		if (translated.isEmpty()) {
			return textOrId;
		}
		return translated;
	}
	
	public String translateAPI(String textOrId, String sourceLanguage, String targetLanguage) throws Exception {
		if ((Site.MICROSOFT_TRANSLATION_KEY != null) && (!Site.MICROSOFT_TRANSLATION_KEY.isEmpty())) {
			return microsoftTranslate(textOrId, sourceLanguage, targetLanguage);
		} else if ((Site.YANDEX_KEY != null) && (!Site.YANDEX_KEY.isEmpty())) {
			return yandexTranslate(textOrId, sourceLanguage, targetLanguage);
		}
		return null;
	}
	
	public void cacheTranslation(TranslationId id, String translation) {
		if (this.translations.size() > MAX_SIZE) {
			return;
		}
		this.translations.put(id, translation);
	}
	
	public String yandexTranslate(String textOrId, String sourceLanguage, String targetLanguage) throws Exception {
		if (sourceLanguage == null || targetLanguage == null) {
			return null;
		}
		if (sourceLanguage.length() < 2 || targetLanguage.length() < 2) {
			return null;
		}
		if (sourceLanguage.length() > 2) {
			sourceLanguage = sourceLanguage.substring(0, 2);
		}
		if (targetLanguage.length() > 2) {
			targetLanguage = targetLanguage.substring(0, 2);
		}
		if (targetLanguage.equals(sourceLanguage)) {
			return null;
		}
		if (textOrId == null || textOrId.isEmpty()) {
			return null;
		}
		AdminDatabase.instance().log(Level.INFO, "translating", sourceLanguage, targetLanguage, textOrId);
		// Use Yandex.
		String url = "https://translate.yandex.net/api/v1.5/tr/translate?key=";
		url = url + Site.YANDEX_KEY;
		url = url + "&format=html&text=" + Utils.encodeURL(textOrId);
		url = url + "&lang=" + sourceLanguage + "-" + targetLanguage;
		String xml = Utils.httpGET(url);
		Element root = Utils.parseXML(xml);
		NodeList list = root.getElementsByTagName("text");
		if (list.getLength() == 0) {
			return null;
		}
		String text = list.item(0).getTextContent().trim();
		if (text.isEmpty()) {
			return null;
		}
		AdminDatabase.instance().log(Level.INFO, "translation", text);
		return text;
	}
	
	public String microsoftTranslate(String textOrId, String sourceLanguage, String targetLanguage) throws Exception {
		if (sourceLanguage == null || targetLanguage == null) {
			return null;
		}
		if (sourceLanguage.length() < 2 || targetLanguage.length() < 2) {
			return null;
		}
		if (sourceLanguage.length() > 2) {
			sourceLanguage = sourceLanguage.substring(0, 2);
		}
		if (targetLanguage.length() > 2) {
			targetLanguage = targetLanguage.substring(0, 2);
		}
		if (targetLanguage.equals(sourceLanguage)) {
			return null;
		}
		if (textOrId == null || textOrId.isEmpty()) {
			return null;
		}
		AdminDatabase.instance().log(Level.INFO, "translating", sourceLanguage, targetLanguage, textOrId);
		
		// Use Microsoft Azure 
		String url = "https://api.cognitive.microsofttranslator.com/translate?api-version=3.0";
		url = url + "&to=" + targetLanguage;
		url = url + "&textType=html";
		
		String type = "application/json; charset=UTF-8";
		String data = "[{\"Text\":\""+ Utils.escapeQuotesJS(textOrId) +"\"}]";
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Ocp-Apim-Subscription-Key", Site.MICROSOFT_TRANSLATION_KEY);
		headers.put("Ocp-Apim-Subscription-Region", "eastus");
		
		String jsonRetn = Utils.httpPOST(url, type, data, headers);
			
		if ((jsonRetn.indexOf("},\"translations\":[{\"text\":\"") == -1) || (jsonRetn.indexOf("\",\"to\":\""+ targetLanguage +"\"}]}]") == -1)) {
			return null;
		}
		
		int startInx = 27 + jsonRetn.indexOf("},\"translations\":[{\"text\":\"");
		int endInx = jsonRetn.indexOf("\",\"to\":\""+ targetLanguage +"\"}]}]");
		String text = jsonRetn.subSequence(startInx, endInx).toString();
		
		if (text.isEmpty()) {
			return null;
		}
		text = text.replaceAll("(\\\\r\\\\n|\\\\n)", "<br/>");
		text = text.replaceAll("\\\\\"", "\"");
		AdminDatabase.instance().log(Level.INFO, "translation", text);
		return text;
	}
	
	public void clear(Translation translation) {
		this.translations.remove(new TranslationId(translation));
		this.languages = null;
	}
	
	public Set<String> getLanguages() {
		if (this.languages == null) {
			Set<String> languages = new HashSet<String>(AdminDatabase.instance().getLanguages());
			this.languages = languages;
		}
		return this.languages;
	}
	
	public boolean checkLanguage(String code) {
		return getLanguages().contains(code);
	}
	
}

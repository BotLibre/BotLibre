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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

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
					// Use Yandex.
					translated = yandexTranslate(textOrId, sourceLanguage, targetLanguage);
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
					AdminDatabase.instance().log(exception);
				}
				cacheTranslation(id, "");
				return textOrId;
			}
			cacheTranslation(id, translation.translation);
			return translation.translation;
		}
		if (translated.isEmpty()) {
			return textOrId;
		}
		return translated;
	}
	
	public void cacheTranslation(TranslationId id, String translation) {
		if (this.translations.size() > MAX_SIZE) {
			return;
		}
		this.translations.put(id, translation);
	}
	
	public String yandexTranslate(String textOrId, String sourceLanguage, String targetLanguage) {
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
		try {
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
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
			return null;
		}
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

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

import java.util.logging.Level;

import org.botlibre.BotException;
import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;


public class BotTranslationService extends TranslationService {
	protected static BotTranslationService instance = new BotTranslationService();
	
	public BotTranslationService() {
	}

	public static BotTranslationService instance() {
		return instance;
	}
	
	public String translate(String textOrId, String sourceLanguage, String targetLanguage) {
		TranslationId id = new TranslationId();
		id.text = textOrId;
		id.sourceLanguage = sourceLanguage;
		id.targetLanguage = targetLanguage;
		String translated = this.translations.get(id);
		if (translated == null) {
			BotTranslation translation = AdminDatabase.instance().findBotTranslation(id);
			if (translation == null) {
				try {
					if (Stats.stats.botTranslations > Site.MAX_TRANSLATION_API) {
						throw new BotException("Max translations");
					}
					// translate with appropriate api 
					translated = translateAPI(textOrId, sourceLanguage, targetLanguage);
					Stats.stats.botTranslations++;
					if (translated != null) {
						translation = new BotTranslation();
						translation.sourceLanguage = sourceLanguage;
						translation.targetLanguage = targetLanguage;
						translation.text = textOrId;
						translation.translation = translated;
						translation.creationDate = System.currentTimeMillis();
						AdminDatabase.instance().updateBotTranslation(translation);
						cacheTranslation(id, translated);
						return translated;
					}
				} catch (Exception exception) {
					Stats.stats.botTranslationErrors++;
					AdminDatabase.instance().log(exception);
				}
				cacheTranslation(id, "");
				return textOrId;
			}
			Stats.stats.cachedBotTranslations++;
			cacheTranslation(id, translation.translation);
			return translation.translation;
		}
		Stats.stats.cachedBotTranslations++;
		if (translated.isEmpty()) {
			return textOrId;
		}
		return translated;
	}
	
	public void clear(BotTranslation translation) {
		this.translations.remove(new TranslationId(translation));
		this.languages = null;
	}
	
}

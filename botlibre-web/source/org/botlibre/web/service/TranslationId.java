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

import javax.persistence.Entity;

@Entity
public class TranslationId {
	
	public String text;
	
	public String sourceLanguage;
	
	public String targetLanguage;
	
	public TranslationId() {
		
	}
	
	public TranslationId(Translation translation) {
		this.text = translation.text;
		this.sourceLanguage = translation.sourceLanguage;
		this.targetLanguage = translation.targetLanguage;
	}
	
	public TranslationId(BotTranslation translation) {
		this.text = translation.text;
		this.sourceLanguage = translation.sourceLanguage;
		this.targetLanguage = translation.targetLanguage;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof TranslationId) {
			TranslationId TranslationId = (TranslationId)object;
			if (this.text == null || this.sourceLanguage == null || this.targetLanguage == null) {
				return super.equals(object);
			}
			return this.text.equals(TranslationId.text)
					&& this.sourceLanguage.equals(TranslationId.sourceLanguage) && this.targetLanguage.equals(TranslationId.targetLanguage);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		if (this.text == null || this.sourceLanguage == null || this.targetLanguage == null) {
			return super.hashCode();
		}
		return this.text.hashCode() & this.targetLanguage.hashCode();
	}

	@Override
	public String toString() {
		return "TranslationId(" + this.text + ")";
	}
}

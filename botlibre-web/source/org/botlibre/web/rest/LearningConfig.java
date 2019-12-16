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
package org.botlibre.web.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO for XML learning config.
 */
@XmlRootElement(name="learning")
@XmlAccessorType(XmlAccessType.FIELD)
public class LearningConfig extends Config {
	@XmlAttribute
	public String learningMode;
	@XmlAttribute
	public String correctionMode;
	@XmlAttribute
	public boolean enableComprehension;
	@XmlAttribute
	public boolean enableConsciousness;
	@XmlAttribute
	public boolean enableWiktionary;
	@XmlAttribute
	public boolean enableEmoting;
	@XmlAttribute
	public Boolean allowJavaScript;
	@XmlAttribute
	public Boolean disableFlag;
	@XmlAttribute
	public Boolean enableEmotions;
	@XmlAttribute
	public Boolean enableResponseMatch;
	@XmlAttribute
	public Boolean learnGrammar;
	@XmlAttribute
	public Boolean splitParagraphs;
	@XmlAttribute
	public Boolean synthesizeResponse;
	@XmlAttribute
	public Boolean fixFormulaCase;
	@XmlAttribute
	public Boolean reduceQuestions;
	@XmlAttribute
	public Boolean trackCase;
	@XmlAttribute
	public Boolean aimlCompatibility;
	@XmlAttribute
	public Boolean checkExactMatchFirst;
	@XmlAttribute
	public Boolean checkSynonyms;
	@XmlAttribute
	public String scriptTimeout;
	@XmlAttribute
	public String responseMatchTimeout;
	@XmlAttribute
	public String conversationMatchPercentage;
	@XmlAttribute
	public String discussionMatchPercentage;
	@XmlAttribute
	public String learningRate;
	@XmlAttribute
	public String nlp;
	@XmlAttribute
	public String language;
	@XmlAttribute
	public String fragmentMatchPercentage;
	@XmlAttribute
	public Boolean penalizeExtraWords;
	@XmlAttribute
	public String extraWordPenalty;
}

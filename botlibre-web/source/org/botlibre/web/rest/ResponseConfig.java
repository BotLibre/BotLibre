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
 * DTO for XML response config.
 */
@XmlRootElement(name="response")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResponseConfig extends Config {
	@XmlAttribute
	public String type;
	@XmlAttribute
	public String parentQuestionId;
	@XmlAttribute
	public String parentResponseId;
	@XmlAttribute
	public String questionId;
	@XmlAttribute
	public String responseId;
	@XmlAttribute
	public String metaId;
	public String question;
	public String response;
	public String previous;
	public String next;
	public String onRepeat;
	public String command;
	public String think;
	public String condition;
	@XmlAttribute
	public String sentiment;
	@XmlAttribute
	public String label;
	@XmlAttribute
	public String topic;
	@XmlAttribute
	public String keywords;
	@XmlAttribute
	public String required;
	@XmlAttribute
	public String emotions;
	@XmlAttribute
	public String actions;
	@XmlAttribute
	public String poses;
	@XmlAttribute
	public Boolean noRepeat;
	@XmlAttribute
	public Boolean requirePrevious;
	@XmlAttribute
	public Boolean requireTopic;
	@XmlAttribute
	public Boolean exclusiveTopic;
	@XmlAttribute
	public Boolean flagged;
	@XmlAttribute
	public String correctness;
	@XmlAttribute
	public Boolean autoReduce;
	@XmlAttribute
	public String displayHTML;

	public ResponseConfig() {
		
	}
	public ResponseConfig(String value) {
		this.response = value;
	}
}

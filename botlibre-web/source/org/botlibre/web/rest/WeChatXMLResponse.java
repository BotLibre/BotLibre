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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO for XML WeChat response
 */
@XmlRootElement(name="xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class WeChatXMLResponse {
	@XmlElement
	public String ToUserName;
	
	@XmlElement
	public String FromUserName;
	
	@XmlElement
	public String CreateTime;
	
	@XmlElement
	public String MsgType;
	
	@XmlElement
	public String Content;
	
	@XmlElement
	public String MsgId;
	
	@XmlElement
	public String MediaId;
	
	@XmlElement
	public String Format;
	
	@XmlElement
	public String Recognition;
	
	@XmlElement
	public String Event;
	
	@XmlElement
	public String EventKey;
	
	public String toString() {
		return "ToUsername: " + ToUserName + ", FromUserName: " + FromUserName + ", CreateTime: " + CreateTime + ", MsgType: " + MsgType + ", Content: " + Content + ", MsgId: " + MsgId;
	}
}


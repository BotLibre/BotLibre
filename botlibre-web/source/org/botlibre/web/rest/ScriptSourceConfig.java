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
 * DTO for XML script source code.
 */
@XmlRootElement(name="script-source")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScriptSourceConfig extends Config {
	@XmlAttribute
	public String id;
	@XmlAttribute
	public String creationDate;
	@XmlAttribute
	public String updateDate;
	@XmlAttribute
	public boolean version;
	@XmlAttribute
	public String versionName;
	@XmlAttribute
	public String creator;
	
	public String source;
}

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
 * DTO for XML browse options.
 */
@XmlRootElement(name="browse")
@XmlAccessorType(XmlAccessType.FIELD)
public class BrowseConfig extends Config {
	@XmlAttribute
	public boolean filterPrivate;
	@XmlAttribute
	public boolean filterAdult;
	@XmlAttribute
	public String type;
	@XmlAttribute
	public String typeFilter;
	@XmlAttribute
	public String userFilter;
	@XmlAttribute
	public String tag;
	@XmlAttribute
	public String category;
	@XmlAttribute
	public String filter;
	@XmlAttribute
	public String contentRating;
	@XmlAttribute
	public String restrict;
	@XmlAttribute
	public String sort;
	@XmlAttribute
	public String page;
}

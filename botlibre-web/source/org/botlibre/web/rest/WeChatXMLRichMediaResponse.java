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

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class WeChatXMLRichMediaResponse extends WeChatXMLResponse {

	@XmlElement
	private String ArticleCount;
	
	@XmlElementWrapper(name="Articles")
	@XmlElement
	public ArrayList<WeChatRichMediaItem> item;
	
	@XmlElement
	public String PicUrl;	
	
	@XmlElement
	public String Title;
	
	@XmlElement
	public String Description;
	
	@XmlElement
	public String Url;
	
	public WeChatXMLRichMediaResponse() {
		item = null;
		PicUrl = null;
		Title = null;
		Description = null;
		Url = null;
	}
	
	public void addNewItem(String title, String description, String picUrl, String url) {
		if(item == null) {
			item = new ArrayList<WeChatRichMediaItem>();
		}
		WeChatRichMediaItem newItem = new WeChatRichMediaItem(title, description, picUrl, url);
		item.add(newItem);
		ArticleCount = "" + item.size();
	}
	
	@XmlRootElement(name="item")
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class WeChatRichMediaItem {
		@XmlElement
		public String Title;
		
		@XmlElement
		public String Description;
		
		@XmlElement
		public String PicUrl;
		
		@XmlElement
		public String Url;
		
		@SuppressWarnings("unused")
		public WeChatRichMediaItem() {
			Title = "";
			Description = "";
			PicUrl = "";
			Url = "";
		}
		
		public WeChatRichMediaItem(String title, String description, String picUrl, String url) {
			Title = title;
			Description = description;
			PicUrl = picUrl;
			Url = url;
		}
	}
}


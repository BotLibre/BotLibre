/******************************************************************************
 *
 *  Copyright 2017 Paphus Solutions Inc.
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

package org.botlibre.sense.skype;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class SkypeActivity {
	public String type;
	public String id;
	public String serviceURL;
	public String channelId;
	public String timestamp;
	public String conversationId;
	public String conversationName;
	public String fromId;
	public String fromName;
	public String recipientId;
	public String recipientName;
	public String text;
	
	public SkypeActivity(String json) {
		JSONObject root = (JSONObject)JSONSerializer.toJSON(json);
		
		this.type = root.optString("type");
		this.id = root.optString("id");
		this.timestamp = root.optString("timestamp");
		this.serviceURL = root.optString("serviceUrl");
		this.channelId = root.optString("channelId");
		
		JSONObject from = root.optJSONObject("from");
		if(from != null) {
			this.fromId = from.optString("id");
			this.fromName = from.optString("name");
		}
		
		JSONObject conversation = root.optJSONObject("conversation");
		if(conversation != null) {
			this.conversationId = conversation.optString("id");
			this.conversationName = conversation.optString("name");
		}
		
		JSONObject recipient = root.optJSONObject("recipient");
		if(recipient != null) {
			this.recipientId = recipient.optString("id");
			this.recipientName = recipient.optString("name");
		}
		
		this.text = root.optString("text");
	}

	public SkypeActivity() {
	}
	
}

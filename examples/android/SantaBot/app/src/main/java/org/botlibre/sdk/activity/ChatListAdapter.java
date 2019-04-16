/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
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

package org.botlibre.sdk.activity;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.botlibre.santabot.R;
import org.botlibre.sdk.SDKConnection;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.ChatConfig;
import org.botlibre.sdk.config.ChatResponse;
import org.botlibre.sdk.util.Utils;

public class ChatListAdapter extends ArrayAdapter<Object> {
	 
	Activity activity;
 
    public ChatListAdapter(Activity activity, int resourceId, List<Object> items) {
        super(activity, resourceId, items);
        this.activity = activity;
    }
 
    class ChatListViewHolder {
        ImageView userView;
        TextView messageView;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	ChatListViewHolder holder = null;
        Object message = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater)this.activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.chat_list, null);
            holder = new ChatListViewHolder();
            holder.userView = (ImageView)convertView.findViewById(R.id.userView);
            holder.messageView = (TextView) convertView.findViewById(R.id.messageView);
            convertView.setTag(holder);
        } else {
            holder = (ChatListViewHolder) convertView.getTag();
        }
        
        if (message instanceof ChatConfig) {
        	ChatConfig config = (ChatConfig)message;
            holder.messageView.setText(config.message);
            if (MainActivity.user != null && MainActivity.user.avatar != null) {
            	HttpGetImageAction.fetchImage(this.activity, MainActivity.user.avatar, holder.userView);
            } else {
            	HttpGetImageAction.fetchImage(this.activity, SDKConnection.defaultUserImage(), holder.userView);            	
            }
        } else if (message instanceof ChatResponse) {
        	ChatResponse config = (ChatResponse)message;
            holder.messageView.setText(Utils.stripTags(config.message));
            String avatar = ((ChatActivity)this.activity).getAvatarIcon(config);
            HttpGetImageAction.fetchImage(this.activity, avatar, holder.userView);
        }
 
        return convertView;
    }
}
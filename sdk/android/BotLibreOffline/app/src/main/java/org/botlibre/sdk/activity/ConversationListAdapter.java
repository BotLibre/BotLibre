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

import org.botlibre.sdk.config.ConversationConfig;
import org.botlibre.sdk.config.InputConfig;
import org.botlibre.sdk.util.Utils;

import org.botlibre.offline.R;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ConversationListAdapter extends ArrayAdapter<Object> {
	 
	Activity activity;
 
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public ConversationListAdapter(Activity activity, int resourceId, List items) {
        super(activity, resourceId, items);
        this.activity = activity;
    }
 
    class ResponseListViewHolder {
        TextView speakerView;
        TextView inputView;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	ResponseListViewHolder holder = null;
    	Object config = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater)this.activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.conversation_list, null);
            holder = new ResponseListViewHolder();
            holder.speakerView = (TextView)convertView.findViewById(R.id.speakerView);
            holder.inputView = (TextView) convertView.findViewById(R.id.inputView);
            convertView.setTag(holder);
        } else {
            holder = (ResponseListViewHolder) convertView.getTag();
        }
        
        if (config instanceof InputConfig) {
        	InputConfig input = (InputConfig)config;
            holder.speakerView.setText(input.speaker + " - " + input.displayCreationDate());
            holder.speakerView.setTypeface(null, Typeface.NORMAL);
            holder.inputView.setText(input.value);
        	holder.inputView.setVisibility(View.VISIBLE);
        } else {
        	ConversationConfig conversation = (ConversationConfig)config;
        	String type = conversation.type;
        	if (type == null) {
        		type = "";
        	} else {
        		type = Utils.capitalize(type);
        	}
            holder.speakerView.setText(type + ", " + conversation.displayCreationDate());
            holder.speakerView.setTypeface(null, Typeface.BOLD);
        	holder.inputView.setVisibility(View.GONE);
        }
 
        return convertView;
    }
}
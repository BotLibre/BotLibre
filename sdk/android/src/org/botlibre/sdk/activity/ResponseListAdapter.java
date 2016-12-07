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

import org.botlibre.sdk.config.ResponseConfig;

import org.botlibre.sdk.R;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ResponseListAdapter extends ArrayAdapter<ResponseConfig> {
	 
	Activity activity;
 
    public ResponseListAdapter(Activity activity, int resourceId, List<ResponseConfig> items) {
        super(activity, resourceId, items);
        this.activity = activity;
    }
 
    class ResponseListViewHolder {
        TextView questionView;
        TextView responseView;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	ResponseListViewHolder holder = null;
    	ResponseConfig config = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater)this.activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.response_list, null);
            holder = new ResponseListViewHolder();
            holder.questionView = (TextView)convertView.findViewById(R.id.questionView);
            holder.responseView = (TextView) convertView.findViewById(R.id.responseView);
            convertView.setTag(holder);
        } else {
            holder = (ResponseListViewHolder) convertView.getTag();
        }

        if (config.question != null) {
        	holder.questionView.setText(config.question);
        } else {
        	holder.questionView.setVisibility(View.GONE);
        }
        if (config.response != null) {
        	holder.responseView.setText(config.response);
        }
        if (config.flagged) {
        	holder.responseView.setTextColor(Color.RED);
        } else if (config.correctness != null && config.correctness.startsWith("-")) {
        	holder.responseView.setTextColor(Color.parseColor("#47092E"));
        } else {
        	holder.responseView.setTextColor(Color.BLUE);
        }
 
        return convertView;
    }
}
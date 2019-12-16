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

package org.botlibre.sdk.activity;

import java.util.List;

import org.botlibre.sdk.config.OfflineTemplateConfig;

import org.botlibre.offline.pizzabot.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomListViewAdapter extends ArrayAdapter<OfflineTemplateConfig> {
	 
    Context context;
 
    public CustomListViewAdapter(Context context, int resourceId,
            List<OfflineTemplateConfig> items) {
        super(context, resourceId, items);
        this.context = context;
    }
     
    /*private view holder class*/
    private class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
        TextView txtDec;
    }
     
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        OfflineTemplateConfig rowItem = getItem(position);
         
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_imager, null);
            holder = new ViewHolder();
            holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
            holder.txtDec = (TextView) convertView.findViewById(R.id.dec);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
                 
        holder.txtTitle.setText(rowItem.getTitle());
        holder.txtDec.setText(rowItem.getDec());
        holder.imageView.setImageResource(rowItem.getImageId());
         
        return convertView;
    }
}
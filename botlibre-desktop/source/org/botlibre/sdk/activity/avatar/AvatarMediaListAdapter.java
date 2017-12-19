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

package org.botlibre.sdk.activity.avatar;

import java.util.List;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetVideoAction;
import org.botlibre.sdk.config.AvatarMedia;
import org.botlibre.sdk.util.Utils;

import org.botlibre.sdk.R;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

public class AvatarMediaListAdapter extends ArrayAdapter<AvatarMedia> {
	 
	Activity activity;
 
    public AvatarMediaListAdapter(Activity activity, int resourceId, List<AvatarMedia> items) {
        super(activity, resourceId, items);
        this.activity = activity;
    }
 
    public AvatarMediaListAdapter(Activity activity, String resourceId, List<AvatarMedia> items) {
        super(activity, resourceId, items);
        this.activity = activity;
    }
 
    
    class ImageListViewHolder {
        ImageView imageView;
        TextView nameView;
        TextView typeView;
        TextView emotionsView;
        TextView actionsView;
        TextView posesView;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	ImageListViewHolder holder = null;
    	AvatarMedia config = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater)this.activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.avatar_media_list, null);
            holder = new ImageListViewHolder();
            holder.imageView = (ImageView)convertView.findViewById(R.id.imageView);
            holder.nameView = (TextView) convertView.findViewById(R.id.nameView);
            holder.typeView = (TextView)convertView.findViewById(R.id.typeView);
            holder.emotionsView = (TextView)convertView.findViewById(R.id.emotionsView);
            holder.actionsView = (TextView)convertView.findViewById(R.id.actionsView);
            holder.posesView = (TextView)convertView.findViewById(R.id.posesView);
            convertView.setTag(holder);
        } else {
            holder = (ImageListViewHolder) convertView.getTag();
        }
        
        holder.nameView.setText(config.name);
        holder.typeView.setText(Utils.stripTags(config.type));
        holder.emotionsView.setText(config.emotions);
        holder.actionsView.setText(config.actions);
        holder.posesView.setText(config.poses);
        if (MainActivity.showImages) {
        	if (config.isVideo()) {
            	holder.imageView.setImageResource(R.drawable.video,80,80);
        	} else if (config.isAudio()) {
            	holder.imageView.setImageResource(R.drawable.audio,80,80);
        	} else {
        		HttpGetImageAction.fetchImage(this.activity, config.media, holder.imageView);
        	}
        } else {
        	holder.imageView.setVisibility(View.GONE);
        }
 
        return convertView;
    }
}
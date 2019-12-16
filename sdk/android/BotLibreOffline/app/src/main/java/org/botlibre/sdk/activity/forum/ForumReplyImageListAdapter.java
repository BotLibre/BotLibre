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

package org.botlibre.sdk.activity.forum;

import java.util.List;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.botlibre.offline.R;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.ForumPostConfig;

public class ForumReplyImageListAdapter extends ArrayAdapter<ForumPostConfig> {
	 
	Activity activity;
 
    public ForumReplyImageListAdapter(Activity activity, int resourceId, List<ForumPostConfig> items) {
        super(activity, resourceId, items);
        this.activity = activity;
    }
 
    class ReplyImageListViewHolder {
        ImageView imageView;
        TextView creationDateView;
        TextView summaryView;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	ReplyImageListViewHolder holder = null;
    	ForumPostConfig config = getItem(position);
  
        LayoutInflater mInflater = (LayoutInflater)this.activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.forumreply_list, null);
            holder = new ReplyImageListViewHolder();
            holder.imageView = (ImageView)convertView.findViewById(R.id.imageView);
            holder.creationDateView = (TextView)convertView.findViewById(R.id.creationDateView);
            holder.summaryView = (TextView)convertView.findViewById(R.id.summaryView);
            convertView.setTag(holder);
        } else {
            holder = (ReplyImageListViewHolder) convertView.getTag();
        }

        holder.creationDateView.setText("Reply by " + config.creator + " posted " + config.displayCreationDate());
        if (config.summary != null) {
        	holder.summaryView.setText(Html.fromHtml(config.summary));
        } else {
        	holder.summaryView.setText("");
        }
        if (MainActivity.showImages && config.avatar != null) {
        	HttpGetImageAction.fetchImage(this.activity, config.avatar, holder.imageView);
        } else {
        	holder.imageView.setVisibility(View.GONE);
        }
 
        return convertView;
    }
}
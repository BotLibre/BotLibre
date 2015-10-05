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

import org.botlibre.sdk.activity.R;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.ForumPostConfig;

public class ForumPostImageListAdapter extends ArrayAdapter<ForumPostConfig> {
	 
	Activity activity;
 
    public ForumPostImageListAdapter(Activity activity, int resourceId, List<ForumPostConfig> items) {
        super(activity, resourceId, items);
        this.activity = activity;
    }
 
    class PostImageListViewHolder {
        ImageView imageView;
        TextView creatorView;
        TextView topicView;
        TextView creationDateView;
        TextView summaryView;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	PostImageListViewHolder holder = null;
    	ForumPostConfig config = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater)this.activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.forumpost_list, null);
            holder = new PostImageListViewHolder();
            holder.imageView = (ImageView)convertView.findViewById(R.id.imageView);
            holder.creatorView = (TextView)convertView.findViewById(R.id.creatorView);
            holder.creationDateView = (TextView)convertView.findViewById(R.id.creationDateView);
            holder.topicView = (TextView) convertView.findViewById(R.id.topicView);
            holder.summaryView = (TextView)convertView.findViewById(R.id.summaryView);
            convertView.setTag(holder);
        } else {
            holder = (PostImageListViewHolder) convertView.getTag();
        }

        holder.topicView.setText(config.topic);
        holder.creatorView.setText(config.creator);
        holder.creationDateView.setText(config.creationDate);
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
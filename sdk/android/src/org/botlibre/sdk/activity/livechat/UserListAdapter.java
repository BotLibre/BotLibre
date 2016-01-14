package org.botlibre.sdk.activity.livechat;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.UserConfig;

public class UserListAdapter extends ArrayAdapter<UserConfig> {
	 
	Activity activity;
 
    public UserListAdapter(Activity activity, int resourceId, List<UserConfig> items) {
        super(activity, resourceId, items);
        this.activity = activity;
    }
 
    class ReplyImageListViewHolder {
        ImageView imageView;
        TextView nameView;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	ReplyImageListViewHolder holder = null;
    	UserConfig config = getItem(position);
  
        LayoutInflater mInflater = (LayoutInflater)this.activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.user_list, null);
            holder = new ReplyImageListViewHolder();
            holder.imageView = (ImageView)convertView.findViewById(R.id.imageView);
            holder.nameView = (TextView)convertView.findViewById(R.id.nameView);
            convertView.setTag(holder);
        } else {
            holder = (ReplyImageListViewHolder) convertView.getTag();
        }
        holder.nameView.setText(config.user);
        if (MainActivity.showImages && config.avatar != null) {
        	HttpGetImageAction.fetchImage(this.activity, config.avatar, holder.imageView);
        } else {
        	holder.imageView.setVisibility(View.GONE);
        }
 
        return convertView;
    }
}
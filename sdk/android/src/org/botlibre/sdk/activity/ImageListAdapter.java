package org.botlibre.sdk.activity;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.botlibre.sdk.R;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.WebMediumConfig;
import org.botlibre.sdk.util.Utils;

public class ImageListAdapter extends ArrayAdapter<WebMediumConfig> {
	 
	Activity activity;
 
    public ImageListAdapter(Activity activity, int resourceId, List<WebMediumConfig> items) {
        super(activity, resourceId, items);
        this.activity = activity;
    }
 
    class ImageListViewHolder {
        ImageView imageView;
        TextView nameView;
        TextView descriptionView;
        TextView statView;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	ImageListViewHolder holder = null;
        WebMediumConfig config = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater)this.activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.image_list, null);
            holder = new ImageListViewHolder();
            holder.imageView = (ImageView)convertView.findViewById(R.id.imageView);
            holder.nameView = (TextView) convertView.findViewById(R.id.nameView);
            holder.descriptionView = (TextView)convertView.findViewById(R.id.descriptionView);
            holder.statView = (TextView)convertView.findViewById(R.id.statView);
            convertView.setTag(holder);
        } else {
            holder = (ImageListViewHolder) convertView.getTag();
        }
        
        holder.nameView.setText(config.name);
        holder.descriptionView.setText(Utils.stripTags(config.description));
        holder.statView.setText(config.stats());
        if (MainActivity.showImages) {
        	HttpGetImageAction.fetchImage(this.activity, config.avatar, holder.imageView);
        } else {
        	holder.imageView.setVisibility(View.GONE);
        }
 
        return convertView;
    }
}
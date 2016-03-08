package org.botlibre.sdk.activity;

import java.util.List;

import com.paphus.botlibre.client.android.santabot.R;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.AvatarConfig;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class AvatarsListAdapter extends ArrayAdapter<AvatarConfig> {
	 
	Activity activity;
 
    public AvatarsListAdapter(Activity activity, int resourceId, List<AvatarConfig> items) {
        super(activity, resourceId, items);
        this.activity = activity;
    }
 
    private class ViewHolder {
        ImageView imageView;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        AvatarConfig config = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater)this.activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.avatars_list, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView)convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
 
        HttpGetImageAction.fetchImage(this.activity, config.avatar, holder.imageView);
 
        return convertView;
    }
}
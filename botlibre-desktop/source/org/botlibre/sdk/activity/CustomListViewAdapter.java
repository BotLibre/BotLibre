package org.botlibre.sdk.activity;

import java.util.ArrayList;
import java.util.List;

import org.botlibre.sdk.config.OfflineTemplateConfig;

import org.botlibre.sdk.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomListViewAdapter extends ArrayAdapter<OfflineTemplateConfig> {
	List<OfflineTemplateConfig> list;
    Activity context;
 
	public CustomListViewAdapter(Activity context, String resourceId, List<OfflineTemplateConfig> items) {
		super(context, resourceId, items);
		list = items;
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
                 
        holder.txtTitle.setText(rowItem.getTitle() + "asfagaef");
        holder.txtDec.setText(rowItem.getDec());
        holder.imageView.setImageResource(rowItem.getImageId(),50,50);
         
        return convertView;
    }
}
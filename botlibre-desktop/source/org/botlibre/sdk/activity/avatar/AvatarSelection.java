package org.botlibre.sdk.activity.avatar;
import java.util.ArrayList;

import org.botlibre.sdk.activity.ChatActivity;
import org.botlibre.sdk.activity.CustomListViewAdapter;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.OfflineTemplateConfig;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class AvatarSelection extends Activity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_view);
//		final int [] imges = {R.drawable.eddie, R.drawable.jul,R.drawable.robot};
		final String [] names = {"Eddie","Julie","Robot"};
		((TextView) findViewById(R.id.theTitle)).setText("Select Avatar");
		final ListView listView = (ListView) (findViewById(R.id.theListView));
		ArrayList<OfflineTemplateConfig> items = new ArrayList<OfflineTemplateConfig>();
		for (int i = 0; i < names.length; i++) {
//			items.add(new OfflineTemplateConfig(imges[i],names[i],null,null));
		}
//		CustomListViewAdapter adapter = new CustomListViewAdapter(this, R.layout.list_item_imager, items);
//		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				OfflineTemplateConfig template = (OfflineTemplateConfig) (listView.getItemAtPosition(position));
				saveSelectedAvatar(template.getTitle());
				MainActivity.readZipAvatars(AvatarSelection.this, template.getTitle() + ".zip");
				ChatActivity.activity.finish();
				Intent i = new Intent(AvatarSelection.this,ChatActivity.class);
				startActivity(i);
				finish();
			}});
	}
	public static void saveSelectedAvatar(String nameOfAvatar){
		SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
		cookies.putString("nameOfAvatar", MainActivity.nameOfAvatar = nameOfAvatar);
		cookies.commit();
	}
}

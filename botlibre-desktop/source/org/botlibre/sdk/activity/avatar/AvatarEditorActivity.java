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

import org.botlibre.sdk.activity.LibreActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpAddAvatarMediaAction;
import org.botlibre.sdk.activity.actions.HttpDeleteAvatarBackgroundAction;
import org.botlibre.sdk.activity.actions.HttpDeleteAvatarMediaAction;
import org.botlibre.sdk.activity.actions.HttpGetAvatarMediaAction;
import org.botlibre.sdk.activity.actions.HttpSaveAvatarBackgroundAction;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.AvatarMedia;
import org.botlibre.sdk.util.Utils;

import org.botlibre.sdk.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

/**
 * Activity for adding and tagging media for an avatar.
 */
public class AvatarEditorActivity extends LibreActivity {
    public static boolean hd;
	
	protected List<AvatarMedia> media;
	protected AvatarConfig instance;
    protected String childActivity = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_avatar_editor);
		resetView();
	}
	
	public void resetView() {
		this.instance = (AvatarConfig)MainActivity.instance;
		
		this.media = MainActivity.avatarMedias;
		
		TextView title = (TextView) findViewById(R.id.title);
		title.setText(Utils.stripTags(this.instance.name));

		ListView list = (ListView) findViewById(R.id.mediaList);
		list.setAdapter(new AvatarMediaListAdapter(this, R.layout.avatar_media_list, this.media));
		GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTapEvent(MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					ListView list = (ListView) findViewById(R.id.mediaList);
			        int index = list.getCheckedItemPosition();
			        if (index < 0) {
						return false;
			        } else {
			        	tagMedia();
			        }
					return true;
				}
				return false;
			}
		};
		final GestureDetector listDetector = new GestureDetector(this, listener);
		list.setOnTouchListener(new View.OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return listDetector.onTouchEvent(event);
			}
		});
	}
	
	@Override
	public void onResume() {
		this.media = MainActivity.avatarMedias;

		ListView list = (ListView) findViewById(R.id.mediaList);
		list.setAdapter(new AvatarMediaListAdapter(this, R.layout.avatar_media_list, this.media));
		
		super.onResume();
	}
	
	@Override
	public void onPostResume() {
		super.onPostResume();
		
		if (MainActivity.wasDelete) {
			MainActivity.wasDelete = false;
			HttpAction action = new HttpGetAvatarMediaAction(this, (AvatarConfig)MainActivity.instance.credentials(), true);
			action.execute();
		}
	}

	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.layout.menu_avatar_editor, popup.getMenu());
	    onPrepareOptionsMenu(popup.getMenu());
	    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
	        @Override
	        public boolean onMenuItemClick(MenuItem item) {
	            return onOptionsItemSelected(item);
	        }
	    });
	    popup.show();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.menu_avatar_editor, menu);
        return true;
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menuHD).setChecked(hd);
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
	    case R.id.menuAdd:
	    	addMedia();
	        return true;
	    case R.id.menuAddImage:
	    	addImage();
	        return true;
	    case R.id.menuDelete:
	    	deleteMedia();
	        return true;
	    case R.id.menuTag:
	    	tagMedia();
	        return true;
	    case R.id.menuBackground:
	    	setBackground();
	        return true;
	    case R.id.menuClearBackground:
	    	clearBackground();
	        return true;
	    case R.id.menuHD:
	    	hd = !hd;
	        return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void tagMedia() {
        ListView list = (ListView) findViewById(R.id.mediaList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select the media to tag", this);
        	return;
        }
        MainActivity.avatarMedia = this.media.get(index);
                
        Intent intent = new Intent(this, AvatarMediaActivity.class);		
        startActivity(intent);
    }
    
    public void addMedia(View view) {
    	addMedia();
    }
    
    public void addMedia() {
		Intent upload = new Intent(Intent.ACTION_GET_CONTENT);
		upload.setType("*/*");
		this.childActivity = "addMedia";
		try {
			startActivityForResult(upload, 1);
		} catch (Exception notFound) {
			upload = new Intent(Intent.ACTION_GET_CONTENT);
			upload.setType("file/*");
			try {
				startActivityForResult(upload, 1);
			} catch (Exception stillNotFound) {
				upload = new Intent(Intent.ACTION_GET_CONTENT);
				upload.setType("image/*");
				startActivityForResult(upload, 1);
			}
		}
    }
    
    public void addImage() {
		Intent upload = new Intent(Intent.ACTION_PICK);
		upload.setType("image/*");
		this.childActivity = "addMedia";
		try {
			startActivityForResult(upload, 1);
		} catch (Exception notFound) {
			this.childActivity = "addMedia";
			upload = new Intent(Intent.ACTION_GET_CONTENT);
			upload.setType("*/*");
			startActivityForResult(upload, 1);
		}
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
	    if (resultCode != RESULT_OK) {
			return;
		}
	    if (data == null || data.getData() == null) {
			this.childActivity = "";
			return;
		}
	    
		try {
			String file = MainActivity.getFilePathFromURI(this, data.getData());
			boolean fileSize = MainActivity.getFileSize(file, this);
	        AvatarMedia config = new AvatarMedia();
	        config.instance = this.instance.id;
			config.name = MainActivity.getFileNameFromPath(file);
			config.type = MainActivity.getFileTypeFromPath(file);
			config.hd = hd;
			if (this.childActivity.equals("addMedia")) {
				if(fileSize){
				HttpAction action = new HttpAddAvatarMediaAction(this, file, config);
				action.execute();}
			} else {
				HttpAction action = new HttpSaveAvatarBackgroundAction(this, file, config);
				action.execute();
			}
			this.childActivity = "";
		} catch (Exception exception) {
			this.childActivity = "";
			MainActivity.error(exception.getMessage(), exception, this);
			return;
		}
    }
    
    public void deleteMedia(View view) {
    	deleteMedia();
    }
    
    public void deleteMedia() {
        ListView list = (ListView) findViewById(R.id.mediaList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select the media to delete", this);
        	return;
        }
        AvatarMedia config = this.media.get(index);
        AvatarMedia config2 = new AvatarMedia();
        config2.mediaId = config.mediaId;
        config2.instance = this.instance.id;
        
        HttpAction action = new HttpDeleteAvatarMediaAction(AvatarEditorActivity.this, config2);
    	action.execute();
    }
    
    public void setBackground() {
		Intent upload = new Intent(Intent.ACTION_GET_CONTENT);
		upload.setType("image/*");
		this.childActivity = "setBackground";
		try {
			startActivityForResult(upload, 1);
		} catch (Exception notFound) {
			this.childActivity = "setBackground";
			upload = new Intent(Intent.ACTION_GET_CONTENT);
			upload.setType("*/*");
			startActivityForResult(upload, 1);
		}
    }
    
    public void clearBackground() {
        AvatarConfig config = (AvatarConfig)this.instance.credentials();
        
        HttpAction action = new HttpDeleteAvatarBackgroundAction(AvatarEditorActivity.this, config);
    	action.execute();
    }
}

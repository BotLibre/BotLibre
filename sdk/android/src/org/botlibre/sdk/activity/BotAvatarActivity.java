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

package org.botlibre.sdk.activity;

import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpCreateAction;
import org.botlibre.sdk.activity.actions.HttpFetchBotAvatarAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.activity.actions.HttpSaveBotAvatarAction;
import org.botlibre.sdk.activity.avatar.AvatarActivity;
import org.botlibre.sdk.activity.avatar.AvatarTestActivity;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.util.Utils;

import org.botlibre.sdk.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

/**
 * Activity for administering a bot's avatar.
 */
public class BotAvatarActivity extends LibreActivity {
	public static boolean create;
	
	InstanceConfig instance;
	AvatarConfig avatar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_avatar);
        this.instance = (InstanceConfig)MainActivity.instance;
        
        HttpGetImageAction.fetchImage(this, this.instance.avatar, findViewById(R.id.icon));
        
        if (this.instance.instanceAvatar != null && !this.instance.instanceAvatar.isEmpty()) {
        	AvatarConfig config = new AvatarConfig();
        	config.id = this.instance.instanceAvatar;
            HttpFetchBotAvatarAction action = new HttpFetchBotAvatarAction(this, config);
            action.execute();
        } else {
        	HttpGetImageAction.fetchImage(this, this.instance.avatar, findViewById(R.id.imageView));
        }
        create = false;
        MainActivity.browsing = false;
	}
	
	@Override
	public void onResume() {
		AvatarConfig oldAvatar = this.avatar;
		if (create && (MainActivity.instance instanceof AvatarConfig)) {
			this.avatar = (AvatarConfig)MainActivity.instance;
	    	InstanceConfig config = this.instance.credentials();
	    	config.instanceAvatar = this.avatar.id;
	    	this.instance.instanceAvatar = this.avatar.id;
	    	if (this.instance.avatar != null && this.instance.avatar.equals(oldAvatar.avatar)) {
	    		this.instance.avatar = this.avatar.avatar;
	    	}
	    	
	    	HttpAction action = new HttpSaveBotAvatarAction(this, config);
	        action.execute();
		}
		create = false;
		if (MainActivity.browsing && (MainActivity.instance instanceof AvatarConfig)) {
			this.avatar = (AvatarConfig)MainActivity.instance;
	    	InstanceConfig config = this.instance.credentials();
	    	config.instanceAvatar = this.avatar.id;
	    	this.instance.instanceAvatar = this.avatar.id;
	    	if (this.instance.avatar != null && oldAvatar != null && this.instance.avatar.equals(oldAvatar.avatar)) {
	    		this.instance.avatar = this.avatar.avatar;
	    	}
	    	
	    	HttpAction action = new HttpSaveBotAvatarAction(this, config);
	        action.execute();
	        
        	AvatarConfig avatarConfig = (AvatarConfig)this.avatar.credentials();
            action = new HttpFetchBotAvatarAction(this, avatarConfig);
            action.execute();
		}
		MainActivity.browsing = false;
		if ((MainActivity.instance instanceof InstanceConfig) && MainActivity.instance.id.equals(this.instance.id)) {
			this.instance = (InstanceConfig)MainActivity.instance;
		} else {
			MainActivity.instance = this.instance;
		}
		MainActivity.searching = false;
		resetAvatar(this.avatar);
		super.onResume();
	}
	
	public void resetAvatar(AvatarConfig config) {
		this.avatar = config;
		if (this.avatar == null) {
			((TextView)findViewById(R.id.nameText)).setText("");
        	HttpGetImageAction.fetchImage(this, this.instance.avatar, findViewById(R.id.imageView));
		} else {
			((TextView)findViewById(R.id.nameText)).setText(Utils.stripTags(this.avatar.name));
			HttpGetImageAction.fetchImage(this, this.avatar.avatar, findViewById(R.id.imageView));
		}
	}

	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.layout.menu_bot_avatar, popup.getMenu());
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
        menuInflater.inflate(R.layout.menu_bot_avatar, menu);
        return true;
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
	    case R.id.menuBrowse:
	    	browse(null);
	        return true;
	    case R.id.menuCreate:
	    	create(null);
	        return true;
	    case R.id.menuTest:
	    	test();
	        return true;
        case R.id.menuEdit:
        	edit();
            return true;
        case R.id.menuClear:
        	clear();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	public void browse(View view) {
		MainActivity.browsing = true;
		BrowseConfig config = new BrowseConfig();
		config.type = "Avatar";
		config.typeFilter = "Featured";
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}
 
    public void edit() {
    	if (this.avatar == null) {
    		MainActivity.showMessage("This bot does not have an avatar.", this);
    		return;
    	}
    	MainActivity.instance = this.avatar;
        Intent intent = new Intent(this, AvatarActivity.class);
        startActivity(intent);
    }
 
    public void create(View view) {
    	AvatarConfig config = new AvatarConfig();
    	config.name = this.instance.name;
    	config.description = this.instance.description;
    	config.details = this.instance.details;
    	config.disclaimer = this.instance.disclaimer;
    	config.categories = "Misc";
    	config.license = this.instance.license;
    	config.accessMode = this.instance.accessMode;
    	config.isPrivate = true;
    	config.isHidden = this.instance.isHidden;
		
    	create = true;
    	HttpAction action = new HttpCreateAction(this, config, false);
        action.execute();
    }
 
    public void test() {
    	if (this.avatar == null) {
    		MainActivity.showMessage("This bot does not have an avatar.", this);
    		return;
    	}
    	MainActivity.instance = this.avatar;
        Intent intent = new Intent(this, AvatarTestActivity.class);
        startActivity(intent);
    }
    
    public void clear() {
    	InstanceConfig config = this.instance.credentials();
    	config.instanceAvatar = null;
    	
    	HttpAction action = new HttpSaveBotAvatarAction(this, config);
        action.execute();
        
        resetAvatar(null);
    }
}

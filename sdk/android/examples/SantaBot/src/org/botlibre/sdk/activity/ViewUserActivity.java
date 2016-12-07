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

import java.io.StringWriter;

import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpChangeUserIconAction;
import org.botlibre.sdk.activity.actions.HttpFlagUserAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.config.WebMediumConfig;
import org.botlibre.sdk.util.Utils;

import org.botlibre.sdk.R;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

/**
 * Activity for viewing a user's details.
 */
public class ViewUserActivity extends LibreActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user);
        resetView();
		MainActivity.searching = false;
	}
	
	@Override
	public void onResume() {
		MainActivity.searching = false;
		if (MainActivity.user != null && MainActivity.user.equals(MainActivity.viewUser)) {
			MainActivity.viewUser = MainActivity.user;
		}
		resetView();
		super.onResume();
	}
	
	public void resetView() {
        UserConfig user = MainActivity.viewUser;
        if (user == null) {
        	return;
        }
        
        TextView text = (TextView) findViewById(R.id.title);
        text.setText(user.user);
        
        text = (TextView) findViewById(R.id.userText);
        text.setText(user.user);
        text = (TextView) findViewById(R.id.nameText);
        if (user.showName) {
        	text.setText(user.name);
        } else {
        	text.setVisibility(View.GONE);
        }
        text = (TextView) findViewById(R.id.websiteText);
        if (user.website == null || user.website.length() == 0) {
        	text.setVisibility(View.GONE);
        } else {
        	text.setText(user.website);
        }
        text = (TextView) findViewById(R.id.joinedText);
        text.setText("Joined " + user.displayJoined());
        text = (TextView) findViewById(R.id.connectsText);
        text.setText(user.connects + " connects");
        text = (TextView) findViewById(R.id.lastConnectText);
        text.setText("Last connected " + user.displayLastConnect());
        
        text = (TextView) findViewById(R.id.contentText);
        StringWriter writer = new StringWriter();
        if (user.bots != null && !"0".equals(user.bots)) {
        	writer.write("" + user.bots + " bots, ");
        }
        if (user.avatars != null && !"0".equals(user.avatars)) {
        	writer.write("" + user.avatars + " avatars, ");
        }
        if (user.channels != null && !"0".equals(user.channels)) {
        	writer.write("" + user.channels + " channels, ");
        }
        if (user.forums != null && !"0".equals(user.forums)) {
        	writer.write("" + user.forums + " forums, ");
        }
        if (user.domains != null && !"0".equals(user.domains)) {
        	writer.write("" + user.domains + " domains, ");
        }
        if (user.scripts != null && !"0".equals(user.scripts)) {
        	writer.write("" + user.scripts + " scripts, ");
        }
        if (user.graphics != null && !"0".equals(user.graphics)) {
        	writer.write("" + user.graphics + " graphics");
        }
        text.setText(writer.toString());
        
        text = (TextView) findViewById(R.id.statsText);
        text.setText(user.posts + " posts, " + user.messages + " messages");
        
        text = (TextView) findViewById(R.id.typeText);
        text.setText(user.type + " account");
        
        WebView web = (WebView) findViewById(R.id.bioText);
        web.loadDataWithBaseURL(null, Utils.formatHTMLOutput(user.bio), "text/html", "utf-8", null);        
        web.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
            	try {
            		view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            	} catch (Exception failed) {
            		return false;
            	}
                return true;
            }
        });

        if (!user.isFlagged) {
	        findViewById(R.id.flaggedLabel).setVisibility(View.GONE);
        } else {
	        findViewById(R.id.imageView).setVisibility(View.GONE);        	
        }
        
        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageActivity.image = MainActivity.viewUser.avatar;
		        Intent intent = new Intent(ViewUserActivity.this, ImageActivity.class);
		        startActivity(intent);
			}
		});
        
        HttpGetImageAction.fetchImage(this, MainActivity.viewUser.avatar, (ImageView)findViewById(R.id.icon));
        HttpGetImageAction.fetchImage(this, MainActivity.viewUser.avatar, (ImageView)findViewById(R.id.imageView));
	}

	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.menu_view_user, menu);
        return true;
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
        for (int index = 0; index < menu.size(); index++) {
    	    menu.getItem(index).setEnabled(true);        	
        }
        if (MainActivity.user == null || MainActivity.user == MainActivity.viewUser) {
    	    MenuItem item = menu.findItem(R.id.menuFlag);
    	    if (item != null) {
    	    	item.setEnabled(false);
    	    }
        }
        if (MainActivity.user != MainActivity.viewUser) {
    	    menu.findItem(R.id.menuChangeIcon).setEnabled(false);
    	    menu.findItem(R.id.menuEditUser).setEnabled(false);
        }
	    return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
         
        switch (item.getItemId())
        {
	    case R.id.menuChangeIcon:
	    	changeIcon();
	        return true;
	    case R.id.menuEditUser:
	    	editUser();
	        return true;
	    case R.id.menuFlag:
	    	flag();
	        return true;
        case R.id.website:
        	openWebsite();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	public void openWebsite() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.WEBSITE + "/login?view-user=" + MainActivity.viewUser.user));
		startActivity(intent);
	}

	public void editUser() {
        Intent intent = new Intent(this, EditUserActivity.class);
        startActivity(intent);
	}

	public void flag() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to flag a user", this);
        	return;
        }
        final EditText text = new EditText(this);
        MainActivity.prompt("Enter reason for flagging the user", this, text, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            WebMediumConfig instance = MainActivity.instance.credentials();
	            instance.flaggedReason = text.getText().toString();
	            if (instance.flaggedReason.trim().length() == 0) {
	            	MainActivity.error("You must enter a valid reason for flagging the user", null, ViewUserActivity.this);
	            	return;
	            }
	            
	            HttpAction action = new HttpFlagUserAction(ViewUserActivity.this, MainActivity.viewUser);
	        	action.execute();
	        }
        });
	}

	public void changeIcon() {
		Intent upload = new Intent(Intent.ACTION_GET_CONTENT);
		upload.setType("image/*");
		startActivityForResult(upload, 1);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		try {
			String file = MainActivity.getFilePathFromURI(this, data.getData());
			HttpAction action = new HttpChangeUserIconAction(this, file, MainActivity.user);
			action.execute().get();
    		if (action.getException() != null) {
    			throw action.getException();
    		}
		} catch (Exception exception) {
			MainActivity.error(exception.getMessage(), exception, this);
			return;
		}
	}
	
	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.layout.menu_view_user, popup.getMenu());
	    onPrepareOptionsMenu(popup.getMenu());
	    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
	        @Override
	        public boolean onMenuItemClick(MenuItem item) {
	            return onOptionsItemSelected(item);
	        }
	    });
	    popup.show();
	}
}

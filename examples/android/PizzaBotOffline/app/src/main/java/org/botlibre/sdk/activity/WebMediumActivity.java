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

import org.botlibre.sdk.activity.MainActivity.LaunchType;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchUserAction;
import org.botlibre.sdk.activity.actions.HttpFlagAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpStarAction;
import org.botlibre.sdk.activity.actions.HttpThumbsDownAction;
import org.botlibre.sdk.activity.actions.HttpThumbsUpAction;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.config.WebMediumConfig;
import org.botlibre.sdk.util.Utils;

import org.botlibre.offline.pizzabot.R;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

/**
 * Generic activity for viewing a content's details.
 */
@SuppressLint("DefaultLocale")
public abstract class WebMediumActivity extends LibreActivity {
	protected WebMediumConfig instance;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.instance = MainActivity.instance;
        super.onCreate(savedInstanceState); 
		resetView();
	}
	
	@Override
	public void onResume() {
		if (MainActivity.instance != null && MainActivity.instance.id.equals(this.instance.id)) {
			this.instance = MainActivity.instance;
		} else {
			MainActivity.instance = this.instance;
		}
		resetView();
		super.onResume();
	}
	
	@Override
	public void onPostResume() {
		super.onPostResume();
		
		if (MainActivity.wasDelete) {
			MainActivity.wasDelete = false;
			finish();
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_webmedium, menu);
        return true;
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isAdmin = (MainActivity.user != null) && this.instance.isAdmin;
        if (!isAdmin || this.instance.isExternal) {
        	menu.findItem(R.id.menuAdmin).setEnabled(false);
        }
        if (isAdmin || this.instance.isFlagged) {
        	menu.findItem(R.id.menuFlag).setEnabled(false);
        }
	    return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {        
	    case R.id.menuAdmin:
	    	admin();
	        return true;        
        case R.id.menuFlag:
        	flag();
            return true;
        case R.id.menuCreator:
        	viewCreator();
            return true;
        case R.id.menuStar:
        	star();
            return true;
        case R.id.menuThumbsUp:
        	thumbsUp();
            return true;
        case R.id.menuThumbsDown:
        	thumbsDown();
            return true;
        case R.id.website:
        	openWebsite();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	public void resetView() {
		if (this.instance == null) {
			return;
		}
		((TextView) findViewById(R.id.title)).setText(this.instance.name);
        HttpGetImageAction.fetchImage(this, this.instance.avatar, findViewById(R.id.icon));
        
        if (!this.instance.isFlagged) {
	        findViewById(R.id.flaggedLabel).setVisibility(View.GONE);
        } else {
	        findViewById(R.id.imageView).setVisibility(View.GONE);        	
        }
        TextView textView = (TextView) findViewById(R.id.nameLabel);
        textView.setText(Utils.stripTags(this.instance.name));
        boolean isAdmin = (MainActivity.user != null) && this.instance.isAdmin;
        if (isAdmin) {
        	findViewById(R.id.adminButton).setVisibility(View.VISIBLE);
        } else {
        	findViewById(R.id.adminButton).setVisibility(View.GONE);
        }
        if (this.instance.isExternal) {
        	findViewById(R.id.adminButton).setVisibility(View.GONE);
        }
        if (!isAdmin && MainActivity.launchType != LaunchType.Browse) {
        	findViewById(R.id.menuButton).setVisibility(View.GONE);
        }
        
        textView = (TextView) findViewById(R.id.websiteLabel);
        if (this.instance.website == null || this.instance.website.isEmpty()) {
        	textView.setVisibility(View.GONE);
        } else {
            textView.setText(this.instance.website);
        }
        
        textView = (TextView) findViewById(R.id.subdomainLabel);
        if (this.instance.subdomain == null || this.instance.subdomain.isEmpty()) {
        	textView.setVisibility(View.GONE);
        } else {
        	String subdomain = "";
        	if (this.instance.subdomain.contains(".")) {
        		subdomain = "http://" + this.instance.subdomain;
        	} else {
        		subdomain = "http://" + this.instance.subdomain + "." + MainActivity.SERVER;
        	}
        	if (subdomain.equals(this.instance.website)) {
            	textView.setVisibility(View.GONE);
        	} else {
        		textView.setText(subdomain);
        	}
        }
        
        WebView web = (WebView) findViewById(R.id.descriptionLabel);
        web.loadDataWithBaseURL(null, Utils.formatHTMLOutput(this.instance.description), "text/html", "utf-8", null);        
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
        web = (WebView) findViewById(R.id.detailsLabel);
        WebSettings webSettings = web.getSettings();
        webSettings.setDefaultFontSize(10);
        web.loadDataWithBaseURL(null, Utils.formatHTMLOutput(this.instance.details), "text/html", "utf-8", null);        
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
        web = (WebView) findViewById(R.id.disclaimerLabel);
        webSettings = web.getSettings();
        webSettings.setDefaultFontSize(10);
        web.loadDataWithBaseURL(null, Utils.formatHTMLOutput(this.instance.disclaimer), "text/html", "utf-8", null);        
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
        textView = (TextView) findViewById(R.id.categoriesLabel);
        if (textView != null) {
        	textView.setText(this.instance.categories);
        }
        textView = (TextView) findViewById(R.id.tagsLabel);
        if (this.instance.tags == null || this.instance.tags.isEmpty()) {
        	textView.setVisibility(View.GONE);
        } else {
            textView.setText(this.instance.tags);
        }
        
        textView = (TextView) findViewById(R.id.licenseLabel);
        if (this.instance.license == null || this.instance.license.isEmpty()) {
        	textView.setVisibility(View.GONE);
        } else {
            textView.setText(this.instance.license);
        }
        
        textView = (TextView) findViewById(R.id.thumbsupLabel);
        textView.setText(String.valueOf(this.instance.thumbsUp));
        textView = (TextView) findViewById(R.id.thumbsdownLabel);
        textView.setText(String.valueOf(this.instance.thumbsDown));
        textView = (TextView) findViewById(R.id.starsLabel);
        textView.setText(String.valueOf(this.instance.stars));

        textView = (TextView) findViewById(R.id.connectsLabel);
        if (this.instance.connects != null && this.instance.connects.length() > 0) {
        	textView.setText(this.instance.connects + " conects, " + this.instance.dailyConnects + " today, " + this.instance.weeklyConnects + " week, " + this.instance.monthlyConnects + " month");
        } else {
        	textView.setText("");
        }

        textView = (TextView) findViewById(R.id.creatorLabel);
        textView.setText("by " + this.instance.creator + ", " + this.instance.displayCreationDate());
        
        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageActivity.image = instance.avatar;
		        Intent intent = new Intent(WebMediumActivity.this, ImageActivity.class);
		        startActivity(intent);
			}
		});
        
        HttpGetImageAction.fetchImage(this, this.instance.avatar, (ImageView)findViewById(R.id.imageView));
	}

	public abstract String getType();

	public void viewCreator() {
		UserConfig config = new UserConfig();
		config.user = this.instance.creator;

        HttpAction action = new HttpFetchUserAction(this, config);
    	action.execute();
	}
	
	/**
	 * Flag the instance.
	 */
	@SuppressLint("DefaultLocale")
	public void flag() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to flag a " + getType().toLowerCase(), this);
        	return;
        }
        final EditText text = new EditText(this);
        MainActivity.prompt("Enter reason for flagging the " + getType().toLowerCase() + " as offensive", this, text, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            WebMediumConfig instance = WebMediumActivity.this.instance.credentials();
	            instance.flaggedReason = text.getText().toString();
	            if (instance.flaggedReason.trim().length() == 0) {
	            	MainActivity.error("You must enter a valid reason for flagging the " + getType().toLowerCase(), null, WebMediumActivity.this);
	            	return;
	            }
	            
	            HttpFlagAction action = new HttpFlagAction(WebMediumActivity.this, instance);
	        	action.execute();
	        }
        });
	}
	
	public void thumbsUp(View view) {
		thumbsUp();
	}
	
	public void thumbsUp() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to thumbs up a " + getType().toLowerCase(), this);
        	return;
        }
        HttpThumbsUpAction action = new HttpThumbsUpAction(this, this.instance.credentials());
    	action.execute();
	}
	
	public void thumbsDown(View view) {
		thumbsDown();
	}
	
	public void thumbsDown() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to thumbs down a " + getType().toLowerCase(), this);
        	return;
        }
        HttpThumbsDownAction action = new HttpThumbsDownAction(this, this.instance.credentials());
    	action.execute();
	}
	
	public void openWebsite() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.WEBSITE + "/browse?id=" + this.instance.id));
		startActivity(intent);
	}

	public void star(View view) {
		star();
	}
	
	public void star() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to rate a " + getType().toLowerCase(), this);
        	return;
        }
        
        final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog_stars);
		dialog.setTitle("Rate " + getType());
		final int[] stars = new int[1];
		stars[0] = 0;
		dialog.findViewById(R.id.oneStar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
				star(1);
			}
		});
		dialog.findViewById(R.id.twoStar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
				star(2);
			}
		});
		dialog.findViewById(R.id.threeStar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
				star(3);
			}
		});
		dialog.findViewById(R.id.fourStar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
				star(4);
			}
		});
		dialog.findViewById(R.id.fiveStar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
				star(5);
			}
		});

		dialog.show();
	}

	public void star(int stars) {
		if (stars == 0) {
			return;
		}
		
        WebMediumConfig config = this.instance.credentials();
        config.stars = String.valueOf(stars);
        HttpStarAction action = new HttpStarAction(this, config);
    	action.execute();
	}
	
	public void admin(View view) {
		admin();
	}
	
	public abstract void admin();

	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.menu.menu_webmedium, popup.getMenu());
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

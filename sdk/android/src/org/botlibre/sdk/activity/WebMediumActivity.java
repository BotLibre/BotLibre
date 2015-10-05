package org.botlibre.sdk.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.botlibre.sdk.activity.actions.HttpFlagAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.WebMediumConfig;

/**
 * Generic activity for viewing a content's details.
 */
@SuppressLint("DefaultLocale")
public abstract class WebMediumActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		resetView();
	}
	
	@Override
	public void onResume() {
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

	public void resetView() {		
        WebMediumConfig instance = MainActivity.instance;
        
        setTitle(instance.name);
        
        if (!instance.isFlagged) {
	        findViewById(R.id.flaggedLabel).setVisibility(View.GONE);
        } else {
	        findViewById(R.id.imageView).setVisibility(View.GONE);        	
        }
        
        TextView textView = (TextView) findViewById(R.id.websiteLabel);
        textView.setText(instance.website);
        
        WebView web = (WebView) findViewById(R.id.descriptionLabel);
        web.loadDataWithBaseURL(null, instance.description, "text/html", "utf-8", null);        
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
        web.loadDataWithBaseURL(null, instance.details, "text/html", "utf-8", null);        
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
        web.loadDataWithBaseURL(null, instance.disclaimer, "text/html", "utf-8", null);        
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
        TextView text = (TextView) findViewById(R.id.categoriesLabel);
        if (text != null) {
	        text.setText(instance.categories);
        }
        text = (TextView) findViewById(R.id.tagsLabel);
        text.setText(instance.tags);
        text = (TextView) findViewById(R.id.licenseLabel);
        text.setText(instance.license);
        text = (TextView) findViewById(R.id.creatorLabel);
        text.setText("by " + instance.creator);

        HttpGetImageAction.fetchImage(this, instance.avatar, (ImageView)findViewById(R.id.imageView));
	}
	
	public abstract String getType();
	
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
	            WebMediumConfig instance = MainActivity.instance.credentials();
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

	public void menu(View view) {
		openOptionsMenu();
	}
	
}

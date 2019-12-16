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

package org.botlibre.sdk.activity.issuetracker;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

import org.botlibre.sdk.activity.LibreActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpDeleteIssueAction;
import org.botlibre.sdk.activity.actions.HttpFetchIssueAction;
import org.botlibre.sdk.activity.actions.HttpFetchUserAction;
import org.botlibre.sdk.activity.actions.HttpFlagIssueAction;
import org.botlibre.sdk.activity.actions.HttpStarIssueAction;
import org.botlibre.sdk.activity.actions.HttpSubscribeIssueAction;
import org.botlibre.sdk.activity.actions.HttpThumbsDownIssueAction;
import org.botlibre.sdk.activity.actions.HttpThumbsUpIssueAction;
import org.botlibre.sdk.activity.actions.HttpUnsubscribeIssueAction;
import org.botlibre.sdk.config.IssueConfig;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.util.Utils;

import org.botlibre.offline.pizzabot.R;

/**
 * Activity for previewing an issue.
 */
@SuppressLint("DefaultLocale")
public class IssuePreviewActivity extends LibreActivity {
	public static String title;
	public static String details;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		resetView();
	}

	public void resetView() {
        setContentView(R.layout.activity_issue_preview);

		((TextView) findViewById(R.id.title)).setText(Utils.stripTags(title));

        final WebView web = (WebView) findViewById(R.id.detailsLabel);
        web.loadDataWithBaseURL(null, details, "text/html", "utf-8", null);
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
	}
	
}

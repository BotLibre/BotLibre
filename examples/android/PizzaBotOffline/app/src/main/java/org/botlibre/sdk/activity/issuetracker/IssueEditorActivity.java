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

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;

import org.botlibre.sdk.activity.LibreActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpCreateIssueTrackerBitmapAttachmentAction;
import org.botlibre.sdk.activity.actions.HttpCreateIssueTrackerFileAttachmentAction;
import org.botlibre.sdk.activity.actions.HttpCreateIssueTrackerImageAttachmentAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetTagsAction;
import org.botlibre.sdk.activity.actions.HttpUpdateIssueAction;
import org.botlibre.sdk.config.IssueConfig;
import org.botlibre.sdk.config.IssueTrackerConfig;
import org.botlibre.sdk.config.MediaConfig;
import org.botlibre.sdk.util.Utils;

import java.io.File;
import java.util.Arrays;

import org.botlibre.offline.pizzabot.R;

/**
 * Activity for editing an issue.
 */
public class IssueEditorActivity extends LibreActivity {
	protected static final int ATTACH_IMAGE = 1;
	protected static final int ATTACH_FILE = 2;
	protected static final int ATTACH_CAMERA = 3;

	Menu menu;

	/**
	 * Append header markup to details editor.
	 */
	public void h1(View view) {
		EditText text = (EditText) findViewById(R.id.detailsText);
		text.setText(text.getText().toString() + "\n= header =\n");
	}

	/**
	 * Append header markup to details editor.
	 */
	public void h2(View view) {
		EditText text = (EditText) findViewById(R.id.detailsText);
		text.setText(text.getText().toString() + "\n== header ==\n");
	}

	/**
	 * Append header markup to details editor.
	 */
	public void h3(View view) {
		EditText text = (EditText) findViewById(R.id.detailsText);
		text.setText(text.getText().toString() + "\n=== header ===\n");
	}

	/**
	 * Append bullet markup to details editor.
	 */
	public void bullet(View view) {
		EditText text = (EditText) findViewById(R.id.detailsText);
		text.setText(text.getText().toString() + "\n* bullet");
	}

	/**
	 * Append bullet markup to details editor.
	 */
	public void nbullet(View view) {
		EditText text = (EditText) findViewById(R.id.detailsText);
		text.setText(text.getText().toString() + "\n# bullet");
	}

	/**
	 * Append indent markup to details editor.
	 */
	public void indent(View view) {
		EditText text = (EditText) findViewById(R.id.detailsText);
		text.setText(text.getText().toString() + "\n: ");
	}

	/**
	 * Resize, upload, and append an camera picture to the details.
	 */
	public void attachCamera(View view) {
		Intent upload = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(upload, ATTACH_CAMERA);
	}

	/**
	 * Resize, upload, and append an image to the details.
	 */
	public void attachImage(View view) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		//File out = Environment.getExternalStorageDirectory();
		//out = new File(out, "issue.jpg");
		//intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(out));
		startActivityForResult(intent, ATTACH_IMAGE);
	}

	/**
	 * Upload, and append a file to the details.
	 */
	public void attachFile(View view) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		try {
			startActivityForResult(intent, ATTACH_FILE);
		} catch (Exception notFound) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("file/*");
			startActivityForResult(intent, ATTACH_FILE);
		}
	}

	public void appendFile(MediaConfig media) {
		String file = "\nattachment:\nhttp://"
				+ MainActivity.connection.getCredentials().host + MainActivity.connection.getCredentials().app + "/" + media.file + "\n";
		EditText text = (EditText) findViewById(R.id.detailsText);
		text.setText(text.getText().toString() + file);
	}

	/**
	 * Preview the current editor text.
	 */
	public void preview(View view) {
		EditText text = (EditText) findViewById(R.id.titleText);
		IssuePreviewActivity.title = text.getText().toString();
		text = (EditText) findViewById(R.id.detailsText);
		IssuePreviewActivity.details = Utils.formatHTMLOutput(text.getText().toString());

		Intent intent = new Intent(this, IssuePreviewActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK) {
			return;
		}
		if (data == null) {
			return;
		}
		try {
			if (requestCode == ATTACH_CAMERA) {
				Bitmap bitmap = (Bitmap) data.getExtras().get("data");
				System.out.println(bitmap);
				MediaConfig config = new MediaConfig();
				config.name = "issue.jpg";
				config.type = "image/jpeg";
				config.instance = MainActivity.instance.id;
				HttpAction action = new HttpCreateIssueTrackerBitmapAttachmentAction(this, bitmap, config);
				action.execute().get();
			} else if (requestCode == ATTACH_IMAGE || requestCode == ATTACH_FILE) {
				if (data.getData() == null) {
					return;
				}
				String file = MainActivity.getFilePathFromURI(this, data.getData());
				MediaConfig config = new MediaConfig();
				config.name = MainActivity.getFileNameFromPath(file);
				config.type = MainActivity.getFileTypeFromPath(file);
				config.instance = MainActivity.instance.id;
				if (requestCode == ATTACH_IMAGE) {
					HttpAction action = new HttpCreateIssueTrackerImageAttachmentAction(this, file, config);
					action.execute().get();
				} else {
					HttpAction action = new HttpCreateIssueTrackerFileAttachmentAction(this, file, config);
					action.execute().get();
				}
			}
		} catch (Exception exception) {
			MainActivity.error(exception.getMessage(), exception, this);
			return;
		}
	}

	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.menu_edit_issue, popup.getMenu());
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
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_edit_issue, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		this.menu = menu;
		//resetMenu();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		switch (item.getItemId())
		{
			case R.id.menuAttachCamera:
				attachCamera(null);
				return true;
			case R.id.menuAttachImage:
				attachImage(null);
				return true;
			case R.id.menuAttachFile:
				attachFile(null);
				return true;
			case R.id.menuH1:
				h1(null);
				return true;
			case R.id.menuH2:
				h2(null);
				return true;
			case R.id.menuH3:
				h3(null);
				return true;
			case R.id.menuBullet:
				bullet(null);
				return true;
			case R.id.menuNBullet:
				nbullet(null);
				return true;
			case R.id.menuIndent:
				indent(null);
				return true;
			case R.id.menuPreview:
				preview(null);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}

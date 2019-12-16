/******************************************************************************
 *
 *  Copyright 2017 Paphus Solutions Inc.
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

package org.botlibre.sdk.activity.graphic;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpUIAction;
import org.botlibre.sdk.config.GraphicConfig;
import org.botlibre.sdk.config.WebMediumConfig;

import org.botlibre.offline.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public class ImageFetchAction extends HttpUIAction{
	WebMediumConfig config;
	public ImageFetchAction(Activity activity, WebMediumConfig config) {
		super(activity);
		this.config = config;
	}
	@Override
	protected String doInBackground(Void... arg0) {
		try {
			this.config = MainActivity.connection.fetch(this.config);
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}
	@Override
	public void onPostExecute(String xml) {
		super.onPostExecute(xml);
		if (this.exception != null) {
			return;
		}
		try {
			MainActivity.gInstance = (GraphicConfig) this.config;
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		    // Get the layout inflater
		    LayoutInflater inflater = activity.getLayoutInflater();

		    // Inflate and set the layout for the dialog
		    // Pass null as the parent view because its going in the dialog layout
		    View  view = inflater.inflate(R.layout.custom_avatar_view,null);
		    
		    builder.setView(view);
		   
		    HttpGetImageAction.fetchImage(activity, MainActivity.gInstance.media, view.findViewById(R.id.dImage));
		    builder.setCancelable(false);
		    // Add action buttons
		    builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
		               @Override
		               public void onClick(DialogInterface dialog, int id) {
		            	   	Toast.makeText(activity, MainActivity.gInstance.name + " Selected", Toast.LENGTH_SHORT).show();
		            	   	BrowseGraphicActivity.selected = true;
		    				activity.finish();
		               }
		           })
		           .setNegativeButton("Back", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		            	   BrowseGraphicActivity.selected = false;
		               }
		           });
		    builder.create().show();
		} catch (Exception e) { MainActivity.showMessage(e.getMessage(), activity); }
	}
}

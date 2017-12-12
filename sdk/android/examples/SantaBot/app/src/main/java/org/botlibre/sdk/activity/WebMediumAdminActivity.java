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
import org.botlibre.sdk.activity.actions.HttpChangeIconAction;
import org.botlibre.sdk.activity.actions.HttpDeleteAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.WebMediumConfig;
import org.botlibre.sdk.util.Utils;

import org.botlibre.santabot.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Generic activity for a content's admin functions.
 */
public abstract class WebMediumAdminActivity extends LibreActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityCompat.requestPermissions(WebMediumAdminActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
	}
	
	@Override
	public void onResume() {
		resetView();
		super.onResume();
	}
	
	public void resetView() {	
        WebMediumConfig instance = (WebMediumConfig)MainActivity.instance;
        if (instance == null) {
        	return;
        }

		((TextView) findViewById(R.id.title)).setText(Utils.stripTags(instance.name));
        HttpGetImageAction.fetchImage(this, instance.avatar, findViewById(R.id.icon));  
	}

	@SuppressLint("Override")
	public void onRequestPermissionsResult(int requestCode,
	                                       String permissions[], int[] grantResults) {
	    switch (requestCode) {
	        case 1: {
	          if (grantResults.length > 0
	                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {        
	            } else {
	                Toast.makeText(WebMediumAdminActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
	            }
	            return;
	        }
	    }
	}
	
	
	public void changeIcon(View view) {
		Intent upload = new Intent(Intent.ACTION_PICK);
		upload.setType("image/*");
		startActivityForResult(upload, 1);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		try {
			String file = MainActivity.getFilePathFromURI(this, data.getData());
			HttpAction action = new HttpChangeIconAction(this, file, MainActivity.instance.credentials());
			action.execute().get();
    		if (action.getException() != null) {
    			throw action.getException();
    		}
		} catch (Exception exception) {
			MainActivity.error(exception.getMessage(), exception, this);
			return;
		}
	}
	
	/**
	 * Delete the instance.
	 */
	public void delete(View view) {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to delete a " + MainActivity.instance.getType(), this);
        	return;
        }
		MainActivity.confirm("Are you sure you want to permanently delete this " + MainActivity.instance.getType() + "?", this, new Dialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {			        
		        HttpAction action = new HttpDeleteAction(WebMediumAdminActivity.this, MainActivity.instance.credentials());
		    	action.execute();
			}
		});
	}
	
}

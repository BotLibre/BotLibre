package org.botlibre.sdk.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpChangeIconAction;
import org.botlibre.sdk.activity.actions.HttpDeleteAction;
import org.botlibre.sdk.config.WebMediumConfig;

/**
 * Generic activity for a content's admin functions.
 */
public abstract class WebMediumAdminActivity extends Activity {
	
	public void resetView() {		
        WebMediumConfig instance = (WebMediumConfig)MainActivity.instance;

        setTitle("Admin: " + instance.name);    
	}

	public void changeIcon(View view) {
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

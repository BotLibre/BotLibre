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
import org.botlibre.sdk.activity.WebMediumAdminActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpUploadGraphicMediaAction;
import org.botlibre.sdk.config.GraphicConfig;

import org.botlibre.offline.pizzabot.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class GraphicAdminActivity extends WebMediumAdminActivity {
	private boolean addImage = false;
	protected String childActivity = "";
	protected GraphicConfig instance;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin_graphic);
		
		resetView();
	}
	
	public void adminUsers(View view) {
		Intent intent = new Intent(this, GraphicUsersActivity.class);
		startActivity(intent);
	}
	
	public void editInstance(View view) {
		Intent intent = new Intent(this, EditGraphicActivity.class);
		startActivity(intent);
	}
	public void adminMedia(View view){
		Intent intent = new Intent(this, GraphicMediaActivity.class);
		startActivity(intent);
	}
	public void adminUpload(View view){
		this.instance = (GraphicConfig)MainActivity.instance;
		addMedia();
	}
	public void addMedia() {
		addImage =true;
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
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(addImage){
			if (resultCode != RESULT_OK) {
				return;
			}
		    if (data == null || data.getData() == null) {
				this.childActivity = "";
				return;
			}
		    
			try {
				String file = MainActivity.getFilePathFromURI(this, data.getData());  
		        GraphicConfig config = new GraphicConfig();
		        config.id = this.instance.id;
				config.fileName = MainActivity.getFileNameFromPath(file);
				config.fileType = MainActivity.getFileTypeFromPath(file);
				if (this.childActivity.equals("addMedia")) {
					HttpAction action = new HttpUploadGraphicMediaAction(this, file, config);
					action.execute().get();
				}
				this.childActivity = "";
			} catch (Exception exception) {
				this.childActivity = "";
				MainActivity.error(exception.getMessage(), exception, this);
				return;
			}
			addImage=false;
		}
	}
		
}

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
package org.botlibre.sdk.activity.graphic;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.WebMediumActivity;
import org.botlibre.sdk.activity.WebMediumAdminActivity;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpUpdateAction;
import org.botlibre.sdk.config.GraphicConfig;
import org.botlibre.sdk.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class GraphicActivity extends WebMediumActivity{
	private Button playBtn,downloadBtn;
	private GraphicConfig gInstance = (GraphicConfig) MainActivity.instance;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		ActivityCompat.requestPermissions(GraphicActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
		super.onCreate(savedInstanceState);
	}
	
	@SuppressLint("Override")
	public void onRequestPermissionsResult(int requestCode,
	                                       String permissions[], int[] grantResults) {
	    switch (requestCode) {
	        case 1: {
	          if (grantResults.length > 0
	                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {        
	            } else {
	                Toast.makeText(GraphicActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
	            }
	            return;
	        }
	    }
	}
	@Override
	public String getType() {
		return "Graphic";
	}

	@Override
	public void admin() {
		Intent intent = new Intent(this, GraphicAdminActivity.class);		
        startActivity(intent);
	}

	@Override
	public void onResume() {
		resetView();
		super.onResume();
	}
	
	public void openWebsite() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.WEBSITEHTTPS + "/graphic?id=" + MainActivity.instance.id));
		startActivity(intent);
	}
	public void resetView() {
		setContentView(R.layout.activity_graphic);
		
		
		playBtn = (Button)findViewById(R.id.playButton);
		downloadBtn = (Button)findViewById(R.id.downloadButton);
		try{
			super.resetView();
			
		}catch(Exception e){
			MainActivity.showMessage(e.getMessage(), this);
		}
		
		
		if (instance.isExternal) {
			findViewById(R.id.playButton).setVisibility(View.GONE);
			findViewById(R.id.downloadButton).setVisibility(View.GONE);
		}
		
		if(gInstance.isVideo() || gInstance.isAudio()){
			downloadBtn.setVisibility(View.VISIBLE);
			playBtn.setText(R.string.play);
		}else {
			downloadBtn.setVisibility(View.VISIBLE);
			playBtn.setText(R.string.view);
		}
		
	}
	public void openView(View view) {
        Intent intent = new Intent(this, GraphicMediaActivity.class);
        startActivity(intent);
	}
	
	
	//can be used if you want to open the file after downloading... still on testing
//	protected void openFile(String fileName) {
//	    Intent install = new Intent(Intent.ACTION_VIEW);
//	    install.setDataAndType(Uri.fromFile(new File(fileName)),"MIME-TYPE");
//	    startActivity(install);
//	}
	
	
	
	
	public void downloadFile(View v){

		if(gInstance.fileName.equals("")){
			MainActivity.showMessage("Missing file!", this);
			return;
		}
		
		String url=MainActivity.WEBSITE +"/"+ gInstance.media;
		
		try{
			
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		request.setTitle(gInstance.fileName);
		request.setDescription(MainActivity.WEBSITE);
		
		//		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);  only download thro wifi.
		
		request.allowScanningByMediaScanner();
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		
		Toast.makeText(GraphicActivity.this, "Downloading " + gInstance.fileName, Toast.LENGTH_SHORT).show();
		
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, gInstance.fileName);
		DownloadManager manager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
		
		BroadcastReceiver onComplete=new BroadcastReceiver() {
		    public void onReceive(Context ctxt, Intent intent) {
		        Toast.makeText(GraphicActivity.this, gInstance.fileName+" Downloaded!", Toast.LENGTH_SHORT).show();
		    }
		};
		manager.enqueue(request);
		registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		
		}catch(Exception e){
			MainActivity.showMessage(e.getMessage(), this);
		}
	}
}

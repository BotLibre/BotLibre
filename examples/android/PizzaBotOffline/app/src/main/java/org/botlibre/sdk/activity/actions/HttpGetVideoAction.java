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

package org.botlibre.sdk.activity.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.botlibre.sdk.SDKConnection;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.avatar.GetAvatarAction;
import org.botlibre.sdk.micro.MicroConnection;

import android.app.Activity;
import android.net.Uri;

public class HttpGetVideoAction extends HttpGetImageAction {
	
	String video;
	
	public static Uri fetchVideo(Activity activity, String video) {
		if(!MainActivity.online){
			 File file = getFile(GetAvatarAction.nameOfFile,video, activity);
			 return Uri.fromFile(file);
		}
		
		if (video == null) {
			return null;
		}
		
    	if (downloading) {
    		return null;
    	}
	    File file = getFile(video, activity);
	    if (file.exists()) {
	    	if (downloading) {
	    		return null;
	    	}
	    	return Uri.fromFile(file);
	    }
			HttpGetVideoAction action = new HttpGetVideoAction(activity, video);
			try {
				action.execute().get();
				if (action.getException() != null) {
					throw action.getException();
				}
			} catch (Exception exception) {
				if (MainActivity.DEBUG) {
					exception.printStackTrace();
				}
			}
		return Uri.fromFile(file);
	}
	
	public HttpGetVideoAction(Activity activity, String video) {
		super(activity);
		this.video = video;
	}

	@Override
	protected String doInBackground(Void... params) {
        try {
	        URL url = MainActivity.connection.fetchImage(this.video);

		    File file = getFile(this.video, this.activity);
		    if (!file.exists()) {
		    	downloading = true;
		    	file.createNewFile();
		    	FileOutputStream outputStream = new FileOutputStream(file);
			    InputStream stream = url.openConnection().getInputStream();
		    	byte[] bytes= new byte[1024];
		    	int count = 0;
	            while (count != -1) {
	            	count = stream.read(bytes, 0, 1024);
		            if (count == -1) {
		            	break;
		            }
		            outputStream.write(bytes, 0, count);
	            }
	            stream.close();
	            outputStream.close();
		    	downloading = false;
		    }
	        return "";
        } catch (Exception exception) {
    		if (MainActivity.DEBUG) {
    			exception.printStackTrace();
    		}
	    	downloading = false;
        }
        return null;
	}
}
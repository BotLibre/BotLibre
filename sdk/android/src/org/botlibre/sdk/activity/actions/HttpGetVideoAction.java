package org.botlibre.sdk.activity.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import android.app.Activity;
import android.net.Uri;

import org.botlibre.sdk.activity.MainActivity;

public class HttpGetVideoAction extends HttpGetImageAction {
	
	String video;
	
	public static Uri fetchVideo(Activity activity, String video) {
		if (video == null) {
			return null;
		}
    	if (downloading) {
    		return null;
    	}
	    File file = getFile(video, activity);
	    if (file.exists()) {
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
		return null;
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
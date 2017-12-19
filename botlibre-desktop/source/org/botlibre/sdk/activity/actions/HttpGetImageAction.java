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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.micro.ListRender;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class HttpGetImageAction extends HttpAction {
	public static int MAX_HEIGHT = 1200;
	public static int MAX_WIDTH = 600;
	public static int MAX_FILES = 100;
	
	public static volatile boolean downloading;
	
	static File cacheDir;	
	static Map<String, SoftReference<Bitmap>> imageCache = new Hashtable<String, SoftReference<Bitmap>>();
	
	String image;
	Bitmap bmp;
	View view;
	
	public static void setImage(String name, View view, int width, int height){
		if (view instanceof ImageView) {
			String imagePath = "res/drawable/" + name + ".png";
			((ImageView) view).setImageResource(imagePath, width, height);
		}
	}
	
	public static void setImage(int imageId, View view){
		if (view instanceof ImageView) {
			((ImageView) view).setImageResource(imageId);
		}
	}
	
	public static void clearFileCache(Context context) {
		try {
			File[] files = getFileCacheDir(context).listFiles();
	        if (files == null) {
	            return;
	        }
	        for (File file : files) {
	        	if (file.isDirectory()) {
	        		File[] nested = file.listFiles();
	    	        for (File nestedFile : nested) {
	    	        	if (!nestedFile.isDirectory()) {
	    	        		nestedFile.delete();
	    	        	}
	    	        }
	        	} else {
	        		file.delete();
	        	}
	        }
		} catch (Exception failed) {
			Log.wtf(failed.toString(), failed);
		}
    }
	
    public static File getFileCacheDir(Context context) {
    	if (cacheDir == null) {
    		try {
		        //if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
		        //    cacheDir = new File(android.os.Environment.getExternalStorageDirectory().toString() + "/botlibre");
		        //} else {
		            cacheDir = context.getCacheDir();
		        //}
		        if (!cacheDir.exists()) {
		            cacheDir.mkdirs();
		        }
    		} catch (Exception failed) {
    			failed.printStackTrace();
    			return null;
    		}
    	}
    	return cacheDir;
    }
    
    public static File getFile(String filename, Context context) {
    	File dir = getFileCacheDir(context);
    	if (dir == null) {
    		return null;
    	}
    	
    	File file = new File(dir.getAbsolutePath() + "/" + filename);
    	dir = new File(file.getParent());
    	if (!dir.exists()) {
    		dir.mkdir();
    	} else {
	        File[] files = dir.listFiles();
	    	if (files.length > MAX_FILES) {
    	        for (File nestedFile : files) {
    	        	if (!nestedFile.isDirectory()) {
    	        		nestedFile.delete();
    	        	}
    	        }	    		
	    	}
    	}
    	return file;
    }
    public static File getFile(String dirName, String filename, Context context) {
//    	System.out.println("The File Name: " + filename);
    	File dir = getFileCacheDir(context);
    	if (dir == null) {
    		return null;
    	}
    	
    	File file = new File(dir.getAbsolutePath() + "/" + dirName + "/" + filename);
    	dir = new File(file.getParent());
    	if (!dir.exists()) {
    		dir.mkdir();
    	} else {
	        File[] files = dir.listFiles();
	    	if (files.length > MAX_FILES) {
    	        for (File nestedFile : files) {
    	        	if (!nestedFile.isDirectory()) {
    	        		nestedFile.delete();
    	        	}
    	        }	    		
	    	}
    	}
    	return file;
    }
	
	public static void fetchImage(Activity activity, String image, View view) {
		if (image == null) {
			return;
		}
		SoftReference<Bitmap> ref = imageCache.get(image);
		if (ref != null) {
			Bitmap bmp = ref.get();
			if (bmp != null) {
				if (view instanceof ImageView) {
					((ImageView)view).setImageBitmap(bmp);
				} else if (view instanceof Button) {
					((Button)view).setBackgroundDrawable(new BitmapDrawable(view.getResources(), bmp));
				}
		        return;
			}
		}
        HttpGetImageAction action = new HttpGetImageAction(activity, image, view);
    	try {
    		action.execute().get();
    		action.postExecute();
    		if (action.getException() != null) {
    			throw action.getException();
    		}
    	} catch (Exception exception) {
    		if (MainActivity.DEBUG) {
    			exception.printStackTrace();
    		}
    		return;
    	}
	}
	
	public HttpGetImageAction(Activity activity) {
		super(activity);
	}
	
	public HttpGetImageAction(Activity activity, String image, View view) {
		super(activity);
		this.image = image;
		this.view = view;
	}

	@Override
	protected String doInBackground(Void... params) {
        try {
	        URL url = MainActivity.connection.fetchImage(this.image);
		    
		    /*BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    long start = System.currentTimeMillis();
		    InputStream stream = url.openConnection().getInputStream();
		    BitmapFactory.decodeStream(stream, null, options);
		    stream.close();
		    System.out.println("decodeStream1:" + (System.currentTimeMillis() - start));

	        int height = options.outHeight;
	        int width = options.outWidth;
	        options.inPreferredConfig = Bitmap.Config.RGB_565;
	        int inSampleSize = 1;

	        if (height > MAX_HEIGHT) {
	            inSampleSize = Math.round((float)height / (float)MAX_HEIGHT);
	        }

	        int expectedWidth = width / inSampleSize;

	        if (expectedWidth > MAX_WIDTH) {
	            inSampleSize = Math.round((float)width / (float)MAX_WIDTH);
	        }

		    options.inSampleSize = inSampleSize;
		    options.inJustDecodeBounds = false;

		    start = System.currentTimeMillis();
		    stream = url.openConnection().getInputStream();
		    BitmapFactory.decodeStream(stream, null, options).recycle();
		    stream.close();
		    System.out.println("decodeStream2:" + (System.currentTimeMillis() - start));

		    start = System.currentTimeMillis();
		    stream = url.openConnection().getInputStream();
		    BitmapFactory.decodeStream(stream).recycle();
		    stream.close();
		    System.out.println("decodeStream-full:" + (System.currentTimeMillis() - start));*/

		    File file = getFile(this.image, this.activity);
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

		    BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    InputStream stream = new FileInputStream(file);
		    BitmapFactory.decodeStream(stream, null, options);
		    stream.close();

	        int height = options.outHeight;
	        int width = options.outWidth;
	        options.inPreferredConfig = Bitmap.Config.RGB_565;
	        int inSampleSize = 1;
	        if (height > MAX_HEIGHT) {
	            inSampleSize = Math.round((float)height / (float)MAX_HEIGHT);
	        }
	        int expectedWidth = width / inSampleSize;
	        if (expectedWidth > MAX_WIDTH) {
	            inSampleSize = Math.round((float)width / (float)MAX_WIDTH);
	        }
		    options.inSampleSize = inSampleSize;
		    options.inJustDecodeBounds = false;

		    stream = new FileInputStream(file);
		    this.bmp = BitmapFactory.decodeStream(stream, null, options);
		    stream.close();
			    
	        imageCache.put(this.image, new SoftReference<Bitmap>(this.bmp));
	        return "";
        } catch (Exception exception) {
    		if (MainActivity.DEBUG) {
    			exception.printStackTrace();
    		}
        }
        return null;
	}

	protected void postExecute() {
		if (view instanceof ImageView) {
			((ImageView)view).setImageBitmap(bmp);
		} else if (view instanceof Button) {
			((Button)view).setBackgroundDrawable(new BitmapDrawable(view.getResources(), bmp));					
		}
	}
}
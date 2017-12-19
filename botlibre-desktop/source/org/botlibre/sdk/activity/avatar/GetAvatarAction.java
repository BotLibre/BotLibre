package org.botlibre.sdk.activity.avatar;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.botlibre.BotException;
import org.botlibre.avatar.BasicAvatar;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.AvatarMedia;
import org.botlibre.sdk.util.Utils;
import org.w3c.dom.Element;
import android.app.Activity;
import android.util.Log;


public class GetAvatarAction {
	public AvatarConfig instance;
	private Activity activity;
	public static String nameOfFile;
	
	public boolean readZipFile(String fileName){
		String [] array = fileName.split("\\.");
		nameOfFile = array[0];
//		System.out.println("Name of the file: " + nameOfFile);
		InputStream fileURL = BasicAvatar.class.getResourceAsStream(fileName);
		return importAvatar(fileURL);
	}
	public GetAvatarAction(Activity activity){
		this.activity = activity;
	}

	/**
	 * Import the avatar from a zip file.
	 */
	public boolean importAvatar(InputStream stream) {
		try {
			ZipInputStream zip = new ZipInputStream(stream);
			ZipEntry entry = null;
			boolean found = false;
			while ((entry = zip.getNextEntry()) != null) {
				if (entry.getName().equals("meta.xml")) {
					found = true;
					instance = new AvatarConfig();
					Element root = MainActivity.connection.parse(Utils.loadTextFile(zip, "", 10000,false));
					instance.parseXML(root);
//					System.out.println("AvatarName: "+instance.name);
				}else if (entry.getName().equals("icon.jpg")) {
					getFile(instance.icon = entry.getName(), this.activity, zip);
				} else if (entry.getName().equals("background.jpg")) {
					getFile(instance.background = entry.getName(), this.activity, zip);
				} else if (entry.getName().indexOf(".xml") != -1) {
					int index = entry.getName().lastIndexOf('.');
					String id = entry.getName();
					if (index != -1) {
						id = id.substring(0, index);
					}
					AvatarMedia config = new AvatarMedia();
					Element root = MainActivity.connection.parse(Utils.loadTextFile(zip, "", 10000, false));
					config.parseXML(root);
					instance.mediaConfig.put(id, config);
				} else {
					 try{
						 getFile(entry.getName(), this.activity, zip);
					 }catch(IOException ex){
						ex.printStackTrace();
					 }
				}
//				System.out.println(entry.getName());
			}
			zip.close();
			stream.close();
			if (!found) {
				throw new BotException("Missing avatar meta.xml file in export archive");
			}
			
						
		} catch (Exception exception) {
			Log.e("AvatarBean","ex: " +exception.toString());
		}
		return true;
	}
	
	public void getFile(String name, Activity activity, InputStream zip) throws IOException{
		File file = HttpGetImageAction.getFile(nameOfFile,name, this.activity);
	    if (!file.exists()) {
	    	file.createNewFile();
	    	System.out.println("File Exctracted: "+ file.getAbsolutePath());
	    	FileOutputStream outputStream = new FileOutputStream(file);
	    	byte[] bytes= new byte[1024];
	    	int count = 0;
            while (count != -1) {
            	count = zip.read(bytes, 0, 1024);
	            if (count == -1) {
	            	break;
	            }
	            outputStream.write(bytes, 0, count);
            }
            outputStream.close();
	    }
	}

}

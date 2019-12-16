/******************************************************************************
 *
 *  Copyright 2013-2019 Paphus Solutions Inc.
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
package org.botlibre.web.admin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.botlibre.BotException;
import org.botlibre.web.bean.LoginBean;

@MappedSuperclass
public class AbstractMedia implements Cloneable {
	protected static List<String> MEDIA_EXTENSIONS = Arrays.asList(
			"jpg", "bmp", "jpeg", "gif", "png", "svg", "tiff", "wav", "x-wav", "mp3", "mp4", "webm", "ogv", "ogg", "mpeg", "svg", "svg+xml", "jfif", "image", "video", "audio");
	protected static List<String> ATTACHMENT_EXTENSIONS = Arrays.asList(
			"jpg", "bmp", "jpeg", "gif", "png", "svg", "tiff", "wav", "x-wav", "mp3", "mp4", "webm", "ogv", "ogg", "mpeg", "svg", "svg+xml", "jfif", "image", "video", "audio",
			"zip", "txt", "pdf", "plain", "x-zip-compressed");

	@Id
	protected long mediaId;
	protected String name = "";
	protected String type = "image/jpeg";
	@Transient
	protected String fileName;
	
	public AbstractMedia() { }
	
	public AbstractMedia clone() {
		try {
			return (AbstractMedia)super.clone();
		} catch (CloneNotSupportedException ignore) {
			throw new InternalError(ignore.getMessage());
		}
	}
	
	public boolean isImage() {
		return this.type.contains("image") || this.type.isEmpty();
	}
	
	public boolean isAudio() {
		return this.type.contains("audio");
	}
	
	public boolean isVideo() {
		return this.type.contains("video");
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getMediaId() {
		return mediaId;
	}

	public void setMediaId(long mediaId) {
		this.mediaId = mediaId;
	}
	
	/**
	 * Secure: Verify the attachment type is an allowed type.
	 */
	public void checkMediaType() {
		if (this.type == null) {
			throw new BotException("Media type is null");
		}
		String ext = this.type;
		if (this.type.indexOf('/') != -1) {
			ext = this.type.substring(this.type.indexOf('/') + 1, this.type.length());
		}
		if (!MEDIA_EXTENSIONS.contains(ext.toLowerCase())) {
			throw new BotException("Unsupported media type - " + ext);
		}
		int index = this.name.lastIndexOf('.');
		if (index != -1) {
			ext = this.name.substring(index + 1, this.name.length());
			if (!MEDIA_EXTENSIONS.contains(ext.toLowerCase())) {
				throw new BotException("Unsupported media type - " + ext);
			}
		}
	}
	
	/**
	 * Secure: Verify the attachment type is an allowed type.
	 */
	public void checkAttachmentType() {
		if (this.type == null) {
			throw new BotException("Unsupported attachment type - null");
		}
		String ext = this.type;
		if (this.type.indexOf('/') != -1) {
			ext = this.type.substring(this.type.indexOf('/') + 1, this.type.length());
		}
		if (!ATTACHMENT_EXTENSIONS.contains(ext.toLowerCase())) {
			throw new BotException("Unsupported attachment type, must be media, txt, pdf, or zip - " + ext);
		}
		int index = this.name.lastIndexOf('.');
		if (index != -1) {
			ext = this.name.substring(index + 1, this.name.length());
			if (!ATTACHMENT_EXTENSIONS.contains(ext.toLowerCase())) {
				throw new BotException("Unsupported attachment type, must be media, txt, pdf, or zip - " + ext);
			}
		}
	}

	public String getFileName() {
		if (this.fileName == null && this.mediaId != 0) {
			String ext = this.type;
			// Secure: whitebox allowed extensions and types.
			// Prevent non media types such as .jsp/.html files.
			if (this.type == null) {
				throw new BotException("Unsupported media type - null");
			} else if (this.type.equalsIgnoreCase("audio/mpeg")) {
				ext = "mp3";
			} else if (this.type.equalsIgnoreCase("video/mpeg")) {
				ext = "mp4";
			} else if (this.type.equalsIgnoreCase("audio/x-wav")) {
				ext = "wav";
			} else if (this.type.indexOf('/') != -1) {
				ext = this.type.substring(this.type.indexOf('/') + 1, this.type.length());
				if (!MEDIA_EXTENSIONS.contains(ext.toLowerCase())) {
					throw new BotException("Unsupported media type - " + ext);
				}
			} else {
				if (!MEDIA_EXTENSIONS.contains(ext.toLowerCase())) {
					throw new BotException("Unsupported media type - " + this.type);
				}
			}
			String fileName = "media/" + "a" + this.mediaId + "." + ext;
			String path = LoginBean.outputFilePath + "/" + fileName;
			File file = new File(path);
			if (!file.exists()) {
				Media media = AdminDatabase.instance().findMedia(this.mediaId);
				byte[] image = media.getMedia();
				if (image != null) {
					if (!file.exists()) {
						try {
							FileOutputStream stream = new FileOutputStream(file);
							stream.write(image);
							stream.flush();
							stream.close();
						} catch (IOException exception) {
							AdminDatabase.instance().log(exception);
						}
					}
				}
			}
			this.fileName = fileName;
		}
		return this.fileName;
	}
	
}

package org.botlibre.knowledge;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.swing.ImageIcon;

import org.botlibre.BotException;
import org.botlibre.api.knowledge.Data;

public class BinaryData implements Serializable, Data {
	
	private static final long serialVersionUID = 1L;

	protected long id;
	
	protected byte[] bytes;
	
	protected transient ImageIcon icon;
	
	protected transient Object cache;
	
	public BinaryData() { }
	
	public BinaryData(String id) {
		this.id = Long.valueOf(id);
	}

	public int hashCode() {
		if (this.id == 0) {
			return super.hashCode();
		}
		return (int)this.id;
	}
	
	public boolean equals(Object image) {
		if (!(image instanceof BinaryData)) {
			return false;
		}
		if (this.id == 0 || ((BinaryData)image).id == 0) {
			return super.equals(image);
		}
		return this.id == ((BinaryData)image).id;
	}

	public Object getCache() {
		return cache;
	}

	public void setCache(Object cache) {
		this.cache = cache;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public void setImage(InputStream stream, int maxSize) {
		try {
			ByteArrayOutputStream writer = new ByteArrayOutputStream();
			int next = stream.read();
			int size = 0;
			while (next != -1) {
				writer.write(next);
				if (size > maxSize) {
					throw new BotException("File size limit exceeded: " + maxSize);
				}
				next = stream.read();
				size++;
			}
			this.bytes = writer.toByteArray();
		} catch (Exception exception) {
			throw new BotException(exception);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException ignore) {}
			}
		}
	}

	public void outputToFile(String path, boolean overWrite) throws IOException {
		if (this.bytes != null) {
			File file = new File(path);
			if (overWrite || !file.exists()) {
				FileOutputStream stream = new FileOutputStream(file);
				stream.write(this.bytes);
				stream.flush();
				stream.close();
			}
		}
	}
	
	public ImageIcon getImageIcon() {
		if (this.icon == null) {
			this.icon = new ImageIcon(this.bytes);
		}
		return this.icon;
	}
	
	
}

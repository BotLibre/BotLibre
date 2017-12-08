package org.botlibre.knowledge;

import java.io.Serializable;

import org.botlibre.api.knowledge.Data;

public class TextData implements Serializable, Data {
	
	private static final long serialVersionUID = 1L;

	protected long id;
	
	protected String text;
	
	public TextData() { }
	
	public TextData(String id) {
		this.id = Long.valueOf(id);
	}

	public int hashCode() {
		if (this.id == 0) {
			return super.hashCode();
		}
		return (int)this.id;
	}
	
	public boolean equals(Object data) {
		if (!(data instanceof TextData)) {
			return false;
		}
		if (this.id == 0 || ((TextData)data).id == 0) {
			return super.equals(data);
		}
		return this.id == ((TextData)data).id;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	
}

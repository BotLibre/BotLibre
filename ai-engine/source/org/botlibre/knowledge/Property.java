package org.botlibre.knowledge;

import java.io.Serializable;

public class Property implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected String property;
	
	protected String value;
	
	protected boolean startup;
	
	public Property() { }
	
	public Property(String property, String value, boolean startup) {
		this.property = property;
		this.value = value;
		this.startup = startup;
	}

	public int hashCode() {
		if (this.property == null) {
			return super.hashCode();
		}
		return this.property.hashCode();
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Property)) {
			return false;
		}
		if (this.property == null || ((Property)object).property == null) {
			return super.equals(object);
		}
		return this.property.equals(((Property)object).property);
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isStartup() {
		return startup;
	}

	public void setStartup(boolean startup) {
		this.startup = startup;
	}
	
	
}

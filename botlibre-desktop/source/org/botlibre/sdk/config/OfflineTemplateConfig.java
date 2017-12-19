package org.botlibre.sdk.config;

public class OfflineTemplateConfig {
	private String imageId;
	private int id_;
	private String id;
	private String title;
	private String dec;
	
	public OfflineTemplateConfig(){

	}
	
	public OfflineTemplateConfig(String imageId, String title, String dec, String id){
		this.setImageId(imageId);
		this.setTitle(title);
		this.setDec(dec);
		this.setId(id);
	}
	public OfflineTemplateConfig(String imageId, String title, String dec, String id,int id_){
		this.setImageId(imageId);
		this.setTitle(title);
		this.setDec(dec);
		this.setId(id);
	}
	public OfflineTemplateConfig(String imageId, String title, String id,int id_){
		this.setImageId(imageId);
		this.setTitle(title);
		this.setDec("");
		this.setId(id);
	}


	public String getImageId() {
		return imageId;
	}


	public void setImageId(String imageId) {
		this.imageId = imageId;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public String toString(){
		return title + "\n" + dec;
		
	}


	public String getDec() {
		return dec;
	}


	public void setDec(String dec) {
		this.dec = dec;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public int getId_() {
		return id_;
	}


	public void setId_(int id_) {
		this.id_ = id_;
	}



}

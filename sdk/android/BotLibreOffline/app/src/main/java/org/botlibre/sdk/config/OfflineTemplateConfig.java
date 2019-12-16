/******************************************************************************
 *
 *  Copyright 2017 Paphus Solutions Inc.
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
 
package org.botlibre.sdk.config;

import org.botlibre.sdk.activity.MainActivity;

import android.content.Context;
import android.content.SharedPreferences;

public class OfflineTemplateConfig {
	private int imageId;
	private int id_;
	private String id;
	private String title;
	private String dec;
	
	public OfflineTemplateConfig(){

	}
	
	public OfflineTemplateConfig(int imageId, String title, String dec, String id){
		this.setImageId(imageId);
		this.setTitle(title);
		this.setDec(dec);
		this.setId(id);
	}
	public OfflineTemplateConfig(int imageId, String title, String dec, String id,int id_){
		this.setImageId(imageId);
		this.setTitle(title);
		this.setDec(dec);
		this.setId(id);
		this.setId_(id_);
	}

	public int getImageId() {
		return imageId;
	}


	public void setImageId(int imageId) {
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

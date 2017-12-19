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

package org.botlibre.sdk.micro;

import java.io.Serializable;

public class Preferences implements Serializable{
	private static final long serialVersionUID = 1L;
	public String nameOfAvatar;
	public String name;
	public String number;
	public transient int id;
	public Preferences(String name, String number){
		this.name = name;
		this.number = number;
	}
	public Preferences(String name, String number, String nameOfAvatar){
		this.name = name;
		this.number = number;
		this.nameOfAvatar = nameOfAvatar;
	}
	
	public String getNameOfAvatar(){
		return nameOfAvatar;
	}
	public void setNameOfAvatar(String nameOfAvatar){
		this.nameOfAvatar = nameOfAvatar;
	}
	public String getName(){
		return name;
	}
	public String getNumber(){
		return number;
	}
	public void SetName(String name){
		this.name = name;
	}
	public void setNumber(String number){
		this.number = number;
	}
}

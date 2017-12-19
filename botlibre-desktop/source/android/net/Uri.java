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

package android.net;

import java.io.File;

/**
 * Stub class.
 */
public class Uri {
	public String uri;
	
	public Uri(String uri){
		this.uri = uri;
	}
	public Uri(){
		parse("test");
	}
	
	public static Uri fromFile(File file) {
		return parse(file.toURI().toString());
	}
	
	public static Uri parse(String address) {
		Uri uri = new Uri(address);
		return uri;
	}
}
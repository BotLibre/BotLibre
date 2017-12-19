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

package android.util;

/**
 * Stub class.
 */
public class Log {
	
	public static void wtf(String message, Exception exception) {
		System.out.println(message);
		exception.printStackTrace();
	}
	
	public static void wtf(String label, String message) {
		System.out.println(label + " - " + message);
	}
	
	public static void e(String label, String message) {
		System.out.println(label + " - " + message);
	}
}
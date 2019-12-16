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
package org.botlibre.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

/**
 * Add a copyright header to a source tree.
 */
public class CopyrightFiles {
	
	static String LICENSE = "/******************************************************************************\r\n" +
" *\r\n" +
" *  Copyright 2013-2019 Paphus Solutions Inc.\r\n" +
" *\r\n" +
" *  Licensed under the Eclipse Public License, Version 1.0 (the \"License\");\r\n" +
" *  you may not use this file except in compliance with the License.\r\n" +
" *  You may obtain a copy of the License at\r\n" +
" *\r\n" +
" *      http://www.eclipse.org/legal/epl-v10.html\r\n" +
" *\r\n" +
" *  Unless required by applicable law or agreed to in writing, software\r\n" +
" *  distributed under the License is distributed on an \"AS IS\" BASIS,\r\n" +
" *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\r\n" +
" *  See the License for the specific language governing permissions and\r\n" +
" *  limitations under the License.\r\n" +
" *\r\n" +
" ******************************************************************************/\r\n";
	
	static boolean REPLACE_LICENSE = true;

	public static void main(String[] args) throws Exception {
		copyrightDir("D:/Projects/BotLibrePlatform/source/");
	}

	public static void copyrightDir(String dir) throws Exception {
		File[] files = new File(dir).listFiles();
		for (File file : files) {
			String name = file.getName();
			if (file.isDirectory()) {
				copyrightDir(file.getAbsolutePath());
			} else if (name.endsWith(".java")) {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String result = "";
				String line = "";
				boolean first = true;
				boolean skip = false;
				boolean foundPackage = false;
				while ((line = reader.readLine()) != null) {
					if (!REPLACE_LICENSE && first && line.contains("/**")) {
						skip = true;
						break;
					} else {
						first = false;
					}
					if (REPLACE_LICENSE) {
						if (!foundPackage) {
							if (line.contains("package")) {
								foundPackage = true;
								result = result + line + "\r\n";
							}
						} else {
							result = result + line + "\r\n";
						}
					} else {
						result = result + line + "\r\n";
					}
				}
				result = LICENSE + result;
				reader.close();
				if (!foundPackage) {
					System.out.println(name);
					continue;
				}
				if (skip) {
					System.out.println(name);
					continue;
				}

				file.delete();
				FileOutputStream output = new FileOutputStream(file);
				output.write(result.getBytes());
				output.flush();
				output.close();
			}
		}
	}
}
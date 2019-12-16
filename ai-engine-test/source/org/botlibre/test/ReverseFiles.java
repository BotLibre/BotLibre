/******************************************************************************
 *
 *  Copyright 2016 Paphus Solutions Inc.
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Copy a set of files number name%02d in inverse order.
 */
public class ReverseFiles {

	public static void main(String[] args) throws Exception {
		String dir = "D:/Projects/Paphus/Avatars/blink5";
		File[] files = new File(dir).listFiles();
		String prefix = "blink";
		String suffix = ".png";
		int max = 0;
		for (File file : files) {
			String name = file.getName();
			int index = name.indexOf('.');
			String number = name.substring(index - 2, index);
			int value = 0;
			try {
				value = Integer.valueOf(number);
			} catch (Exception exception) {}
			if (value > max) {
				max = value;
			}
		}
		System.out.println(max);
		for (int index = 0; index < (max - 1); index++) {
			String padding = "";
			if ((max - index - 1) < 100) {
				padding = "0";
			}
			if ((max - index - 1) < 10) {
				padding = "00";
			}
			String padding2 = "";
			if ((max + 1 + index) < 100) {
				padding2 = "0";
			}
			if ((max + 1 + index) < 10) {
				padding2 = "00";
			}
			String source = prefix + padding + (max - index - 1) + suffix;
			System.out.println(source);
			String target = prefix + padding2 + (max + 1 + index) + suffix;
			System.out.println(target);
			Path sourcePath = new File(dir + "/" + source).toPath();
			Path targetPath = new File(dir + "/" + target).toPath();
			Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
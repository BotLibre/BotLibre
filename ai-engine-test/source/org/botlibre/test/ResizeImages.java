/******************************************************************************
 *
 *  Copyright 2018 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *	  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/
package org.botlibre.test;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * Resize all images in a directory tree.
 */
public class ResizeImages {

	public static void main(String[] args) throws Exception {
		//resizeImages("D:/VirtualBox VMs/shared/tensorflow-for-poets-2/tf_files/data");
	}

	public static void resizeImages(String dir) throws Exception {
		File[] files = new File(dir).listFiles();
		for (File file : files) {
			String name = file.getName();
			if (file.isDirectory()) {
				resizeImages(file.getAbsolutePath());
			} else if (name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg")) {
				BufferedImage image = ImageIO.read(file);
				BufferedImage resizeImage = resize(image, 598);
				if (resizeImage != null) {
					System.out.println("Resizing: " + file.getPath());
					String newFileName = file.getPath();
					//newFileName = file.getPath().replace(".jp", "+resized.jp");
					//newFileName = newFileName.replace(".JP", "+resized.JP");
					ImageIO.write(resizeImage, "jpg", new File(newFileName));
				}
			}
		}
	}
	
	public static BufferedImage resize(BufferedImage image, int max) {
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		int newW = width;
		int newH = height;
		
		if (width > max) {
			newW = max;
			newH = (int)(((float)max) / ((float)width) * ((float)height));
		} else {
			return null;
		}
		
		Image tmp = image.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage newImage = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);

		Graphics2D g2d = newImage.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return newImage;
	}
}
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
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * Rotate all images in a directory tree.
 */
public class RotateImages {

	public static void main(String[] args) throws Exception {
		rotateImages("D:/VirtualBox VMs/shared/tensorflow-for-poets-2/tf_files/data");
	}

	public static void rotateImages(String dir) throws Exception {
		File[] files = new File(dir).listFiles();
		for (File file : files) {
			String name = file.getName();
			if (file.isDirectory()) {
				rotateImages(file.getAbsolutePath());
			} else if (name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg")) {
				System.out.println("Rotating: " + file.getPath());
				BufferedImage image = ImageIO.read(file);
				
				// 90
				BufferedImage rotated = rotateImage(image, 90);
				String newFileName = file.getPath().replace(".jp", "+90.jp");
				newFileName = newFileName.replace(".JP", "+90.JP");
				System.out.println("90: " + newFileName);
				ImageIO.write(rotated, "jpg", new File(newFileName));
				// 180
				rotated = rotateImage(image, 180);
				newFileName = file.getPath().replace(".jp", "+180.jp");
				newFileName = newFileName.replace(".JP", "+180.JP");
				System.out.println("180: " + newFileName);
				ImageIO.write(rotated, "jpg", new File(newFileName));
				// 270
				rotated = rotateImage(image, 270);
				newFileName = file.getPath().replace(".jp", "+270.jp");
				newFileName = newFileName.replace(".JP", "+270.JP");
				System.out.println("270: " + newFileName);
				ImageIO.write(rotated, "jpg", new File(newFileName));
			}
		}
	}
	
	public static BufferedImage rotateImage(BufferedImage image, double degrees) {
		double sin = Math.abs(Math.sin(Math.toRadians(degrees)));
		double cos = Math.abs(Math.cos(Math.toRadians(degrees)));

		int width = image.getWidth(null);
		int height = image.getHeight(null);

		int newWidth = (int) Math.floor(width*cos + height*sin);
		int newHeight = (int) Math.floor(height*cos + width*sin);

		BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = rotated.createGraphics();

		g.translate((newWidth-width)/2, (newHeight-height)/2);
		g.rotate(Math.toRadians(degrees), width/2, height/2);
		g.drawRenderedImage(image, null);
		g.dispose();

		return rotated;
	}
}
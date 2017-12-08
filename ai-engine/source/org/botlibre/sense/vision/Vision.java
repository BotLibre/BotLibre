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
package org.botlibre.sense.vision;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.BinaryData;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicSense;
import org.botlibre.util.Utils;
import org.ddogleg.nn.FactoryNearestNeighbor;
import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.struct.FastQueue;

import boofcv.alg.color.ColorHsv;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.alg.feature.color.GHistogramFeatureOps;
import boofcv.alg.feature.color.Histogram_F64;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.Planar;

/**
 * The Vision sense loads and processes images.
 * Vision uses the BoofCV library to analyze, process, recognize, and classify images.
 */

public class Vision extends BasicSense {
	public static int IMAGE_SIZE = 300;
	public static int MAX_IMAGE_SIZE = 5000000; // 5meg
	
	public Vision() {
	}
	
	/**
	 * Load an image from a URL.
	 */
	public Vertex loadImage(String urlPath, Network network) {
	    byte[] image = loadImageBytes(urlPath);
	    return loadImage(image, network);
	}
	
	/**
	 * Load an image from a URL.
	 */
	public byte[] loadImageBytes(String urlPath) {
		try {
			URL url = new URL(urlPath);
			URLConnection connection = null;
			try {
				connection = url.openConnection();
			} catch (Exception exception) {
				throw new BotException("Invalid URL");
			}
		    byte[] image = Utils.loadBinaryFile(connection.getInputStream(), true, MAX_IMAGE_SIZE);
		    image = Utils.createThumb(image, IMAGE_SIZE, true);
		    return image;
		} catch (Exception error) {
			log(error);
			throw new BotException(error);
		}
	}
	
	/**
	 * Load an image from a file.
	 */
	public byte[] loadImageFileBytes(String filePath) {
		try {
			File file = new File(filePath);
			FileInputStream stream = new FileInputStream(file);
		    byte[] image = Utils.loadBinaryFile(stream, true, MAX_IMAGE_SIZE);
		    image = Utils.createThumb(image, IMAGE_SIZE, true);
		    return image;
		} catch (Exception error) {
			log(error);
			throw new BotException(error);
		}
	}
	
	/**
	 * Load an image from a file.
	 */
	public Vertex loadImageFile(String filePath, Network network) {
		try {
		    byte[] image = loadImageFileBytes(filePath);
		    return loadImage(image, network);
		} catch (Exception error) {
			log(error);
			throw new BotException(error);
		}
	}
	
	/**
	 * Load binary image.
	 */
	public Vertex loadImage(byte[] image, Network network) {
		BinaryData data = new BinaryData();
		data.setBytes(image);
		return network.createVertex(data);
	}
	
	/**
	 * Self API. Load an image from the URL and find the closest matching image.
	 */
	@SuppressWarnings("unchecked")
	public Vertex matchImage(byte[] image, Vertex tag, double error, Network network) throws IOException {
    	double[] histogram = coupledHueSat(image);
    	
		List<double[]> points = new ArrayList<double[]>();
		List<Vertex> images = tag.orderedRelations(Primitive.IMAGE);
		for (Vertex vertex : images) {
			Object value = vertex.getData();
			if (!(value instanceof BinaryData)) {
				continue;
			}
			BinaryData data = (BinaryData)network.findData((BinaryData)value);
		    points.add(coupledHueSat(data.getBytes()));
		}

		// Use a generic NN search algorithm.  This uses Euclidean distance as a distance metric.
		NearestNeighbor<Vertex> nn = FactoryNearestNeighbor.exhaustive();
		FastQueue<NnData<Vertex>> results = new FastQueue(NnData.class, true);

		nn.init(histogram.length);
		nn.setPoints(points, images);
		nn.findNearest(histogram, -1, 1, results);
		NnData<Vertex> best = results.get(0);
		log("Image match", Level.FINE, best.distance);
		if (best.distance > error) {
			return null;
		}
		return best.data;
	}

	/**
	 * HSV stores color information in Hue and Saturation while intensity is in Value.  This computes a 2D histogram
	 * from hue and saturation only, which makes it lighting independent.
	 */
	public double[] coupledHueSat(byte[] image) throws IOException {
		Planar<GrayF32> rgb = new Planar<GrayF32>(GrayF32.class,1,1,3);
		Planar<GrayF32> hsv = new Planar<GrayF32>(GrayF32.class,1,1,3);

		BufferedImage buffered = ImageIO.read(new ByteArrayInputStream(image));
		if (buffered == null) {
			throw new RuntimeException("Can't load image!");
		}

		rgb.reshape(buffered.getWidth(), buffered.getHeight());
		hsv.reshape(buffered.getWidth(), buffered.getHeight());

		ConvertBufferedImage.convertFrom(buffered, rgb, true);
		ColorHsv.rgbToHsv_F32(rgb, hsv);

		Planar<GrayF32> hs = hsv.partialSpectrum(0,1);

		// The number of bins is an important parameter.  Try adjusting it
		Histogram_F64 histogram = new Histogram_F64(12,12);
		histogram.setRange(0, 0, 2.0 * Math.PI); // range of hue is from 0 to 2PI
		histogram.setRange(1, 0, 1.0);         // range of saturation is from 0 to 1

		// Compute the histogram
		GHistogramFeatureOps.histogram(hs,histogram);

		UtilFeature.normalizeL2(histogram); // normalize so that image size doesn't matter

		return histogram.value;
	}
	
	/**
	 * Self API. Load an image object from the URL.
	 */
	public Vertex loadImage(Vertex source, Vertex url) {
		log("Loading image", Level.FINE, url);
	    try {
	    	return loadImage(url.printString(), source.getNetwork());
	    } catch (Exception exception) {
	    	return null;
	    }
	}
	
	/**
	 * Self API. Find the closest matching image on the tag object with the URL image.
	 */
	public Vertex matchImage(Vertex source, Vertex url, Vertex tag, Vertex error) {
		log("Matching image", Level.FINE, url);
	    try {
	    	byte[] image = loadImageBytes(url.printString());
		    image = Utils.createThumb(image, IMAGE_SIZE, true);
	    	return matchImage(image, tag, ((Number)error.getData()).doubleValue(), source.getNetwork());
	    } catch (Exception exception) {
	    	return null;
	    }
	}
}
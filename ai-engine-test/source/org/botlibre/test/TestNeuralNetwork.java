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

import org.botlibre.analytics.deeplearning.NeuralNetwork;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

/**
 * Test Neural Network.
 */

public class TestNeuralNetwork extends TextTest {
	
	private static void print(double[] inputs, double[] outputs, double[] expectedOutputs) {

		System.out.print("in:");
		for (int index = 0; index < inputs.length; index++) {
			System.out.print(" "+inputs[index]);
		}
		System.out.print("  out:");
		for (int index = 0; index < expectedOutputs.length; index++) {
			System.out.print(" (" + expectedOutputs[index] + ")");
			System.out.print(" " + outputs[index]);
			System.out.print(" err:"+ ((int)((Math.abs((expectedOutputs[index] - outputs[index]) / 2)) * 100)));
		}
		System.out.println();
	}

	@org.junit.Test
	public void testTicTacToe() {
		int[] layerSizes = new int[] { 9, 9, 9 };
		double[][] expectedResults = new double[][] {
			{ 1, 1, 0, 0, 0, 0, 2, 2, 0 }, { 0, 0, 1, 0, 0, 0, 0, 0, 0 },
			{ 0, 1, 1, 0, 0, 0, 2, 2, 0 }, { 1, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 1, 1, 0, 2, 2, 0 }, { 0, 0, 0, 0, 0, 1, 0, 0, 0 },
			{ 0, 0, 0, 0, 1, 1, 2, 2, 0 }, { 0, 0, 0, 1, 0, 0, 0, 0, 0 },
			{ 2, 2, 0, 0, 0, 0, 1, 1, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1 },
			{ 2, 2, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 0, 0, 0, 0, 1, 0, 0 },
		};
		double errorRate = test(expectedResults, layerSizes);
		if (errorRate != 0) {
			fail("Error > 0: " + errorRate);
		}
	}

	@org.junit.Test
	public void testAND() {
		int[] layerSizes = new int[] { 2, 2, 1 };
		double[][] expectedResults = new double[][] {
			{ 0, 1, }, { 0 },
			{ 1, 1, }, { 1 },
			{ 1, 0, }, { 0 },
			{ 0, 0, }, { 0 },
		};
		double errorRate = test(expectedResults, layerSizes);
		if (errorRate != 0) {
			fail("Error > 0: " + errorRate);
		}
	}

	@org.junit.Test
	public void testOR() {
		int[] layerSizes = new int[] { 2, 2, 1 };
		double[][] expectedResults = new double[][] {
			{ 0, 1, }, { 1 },
			{ 1, 1, }, { 1 },
			{ 1, 0, }, { 1 },
			{ 0, 0, }, { 0 },
		};
		double errorRate = test(expectedResults, layerSizes);
		if (errorRate != 0) {
			fail("Error > 0: " + errorRate);
		}
	}

	@org.junit.Test
	/**
	 * This does not work currently...
	 */
	public void testXOR() {
		int[] layerSizes = new int[] { 2, 2, 1 };
		double[][] expectedResults = new double[][] {
			{ 0, 0, 0 }, { 1 },
			{ 0, 0, 1 }, { 0 },
			{ 0, 1, 0 }, { 0 },
			{ 0, 1, 1 }, { 0 },
			{ 1, 0, 0 }, { 0 },
			{ 1, 0, 1 }, { 0 },
			{ 1, 1, 0 }, { 0 },
			{ 1, 1, 1 }, { 1 },
		};
		double errorRate = test(expectedResults, layerSizes);
		if (errorRate != 0) {
			fail("Error > 0: " + errorRate);
		}
	}

	public double test(double[][] expectedResults, int[] layerSizes) {
		int totalError = 0;
		int totalCount = 0;
		for (int runs = 0; runs <= 10; runs++) {
			NeuralNetwork net = new NeuralNetwork(layerSizes);
			double[] inputs = net.getInputs();
			double[] outputs = net.getOutputs();
	
			double[] expectedOutputs;
	
			int pass = 0;
			int passError = 0;
			int passCount = 0;
			for ( ; pass < 100000; pass++) {
				passError = 0;
				passCount = 0;
				for (int index = 0; (index+1) < expectedResults.length; index += 2) {
					System.arraycopy(expectedResults[index], 0, inputs, 0, inputs.length);
					expectedOutputs = expectedResults[index+1];
	
					net.forwardPropagate();
					int error = 0;
					int count = 0;
					for (int index2 = 0; index2 < outputs.length; index2++) {
						error = error + ((int)((Math.abs((expectedOutputs[index2] - outputs[index2]) / 2)) * 100));
						count++;
					}
					passError = passError + error;
					passCount = passCount + count;
					// Only back propagate if incorrect.
					//if ((error / count) != 0) {
						net.backPropagate(expectedOutputs);
					//}
				}
				//System.out.println("Run: " + pass);
				//System.out.println("Error rate: " + (passError / passCount));
				if ((passError / passCount) == 0) {
					break;
				}
			}
			totalError = totalError + passError;
			totalCount = totalCount + passCount;
			System.out.println("Runs: " + pass);
			System.out.println("Total % error rate: " + (passError / passCount));
		}
		System.out.println("Final % error rate: " + (totalError / totalCount));
		return totalError / totalCount;
	}
}


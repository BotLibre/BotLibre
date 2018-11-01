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

package org.botlibre.analytics.deeplearning;

/**
 * Uses the Sigmoid function.
 * Sigmoid is a non-linear (S curve) function bounded by 0 and +1.
 * See, https://en.wikipedia.org/wiki/Sigmoid_function
 */
public class SigmoidActivationFunction implements ActivationFunction {
	@Override
	public double calculate(double value) {
		return sigmoid(value);
	}

	@Override
	public double calculateInverse(double value) {
		double result = sigmoid(value);
		return result * (1.0d - result);
	}
	
	private double sigmoid(double value) {
		return 1.0d / (1.0d + Math.exp(-value));
	}
}

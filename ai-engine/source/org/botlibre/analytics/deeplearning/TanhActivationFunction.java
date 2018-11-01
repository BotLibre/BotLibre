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
 * Uses the Tanh function.
 * Tanh is a non-linear (S curve) function bounded by -1 and +1.
 * See, https://en.wikipedia.org/wiki/Hyperbolic_function#Hyperbolic_tangent
 */
public class TanhActivationFunction implements ActivationFunction {
	@Override
	public double calculate(double value) {
		return tanh(value);
	}

	@Override
	public double calculateInverse(double value) {
		double result = tanh(value);
		return 1.0 - (result * result);
	}
	
	private double tanh(double value) {
		return Math.tanh(value);
	}
}

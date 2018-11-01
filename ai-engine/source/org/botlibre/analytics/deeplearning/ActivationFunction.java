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
 * The activation function calculates a node's value based on the input value.
 * An inverse calculation is also required for back propagation.
 * The inverse function should equal the derivative of the calculation function.
 * See, https://en.wikipedia.org/wiki/Activation_function
 */
public interface ActivationFunction {
	double calculate(double value);
	double calculateInverse(double value);
}

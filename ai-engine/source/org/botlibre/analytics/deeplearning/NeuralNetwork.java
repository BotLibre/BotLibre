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

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Random;

/**
 * This is a basic neural network implementation.
 * It supports n layers with m nodes, and configurable activation functions.
 * It can be used to implement machine learning for problems that can be reduced to an input array of doubles, and output array of doubles.
 * For example a Tic Tac Toe game can be represented as an array our double representing X and O values at each position,
 * and the output could represent to index of the position that is the best move.
 */
public class NeuralNetwork implements Serializable {
	static final long serialVersionUID = 42L;
	
	protected int numberOfLayers;
	protected double[] learningRates = new double[] { 0.001 };
	protected double momentum = 0.8;
	protected ActivationFunction[] activationFunctions;
	protected int[] layerSizes;
	protected int outputLayer;
	protected double[][] neuronValue;
	protected double[][] threshold;
	protected double[][][] weight;
	protected double[][][] lastWeightChange;
	protected double[][] errorGradient;
	protected Random random = new Random();

	public NeuralNetwork() {
		this(new int[] { 2, 2, 2 });
	}
	
	public NeuralNetwork(int[] layerSizes) {
		this.activationFunctions = new ActivationFunction[] { new TanhActivationFunction() };
		//this.activationFunctions = new ActivationFunction[] { new SigmoidActivationFunction() };
		this.layerSizes = layerSizes;

		this.numberOfLayers = layerSizes.length;
		this.outputLayer = this.numberOfLayers - 1;

		this.neuronValue = new double[this.numberOfLayers][];
		this.threshold = new double[this.numberOfLayers][];
		this.weight = new double[this.numberOfLayers-1][][];
		this.lastWeightChange = new double[this.numberOfLayers-1][][];
		this.errorGradient = new double[this.numberOfLayers][];
		for (int layer = 0; layer < this.numberOfLayers; layer++) {
			this.neuronValue[layer] = new double[layerSizes[layer]];
			this.threshold[layer] = new double[layerSizes[layer]];
			this.errorGradient[layer] = new double[layerSizes[layer]];
			if (layer > 0) {
				int prevLayer = layer-1;
				this.weight[prevLayer] = new double[layerSizes[prevLayer]][];
				this.lastWeightChange[prevLayer] = new double[layerSizes[prevLayer]][];
				for (int prevNeuron = 0; prevNeuron < this.weight[prevLayer].length; prevNeuron++) {
					this.weight[prevLayer][prevNeuron] = new double[layerSizes[layer]];
					this.lastWeightChange[prevLayer][prevNeuron] = new double[layerSizes[layer]];
				}
			}
		}

		randomize();
	}

	public void setLearningRate(double learningRate) {
		setLearningRate(new double[] { learningRate });
	}

	public void setLearningRate(double[] learningRate) {
		this.learningRates = learningRate;
	}

	public double getLearningRate(int layer) {
		if (layer < learningRates.length) {
			return learningRates[layer];
		}
		return learningRates[0];
	}

	public void setMomentum(double momentum) {
		this.momentum = momentum;
	}

	public double getMomentum() {
		return this.momentum;
	}

	public void setActivationFunction(ActivationFunction activationFunction) {
		setActivationFunction(new ActivationFunction[] { activationFunction });
	}

	public void setActivationFunction(ActivationFunction[] activationFunctions) {
		this.activationFunctions = activationFunctions;
	}

	public ActivationFunction getActivationFunction(int layer) {
		if (layer < activationFunctions.length) {
			return activationFunctions[layer];
		}
		return activationFunctions[0];
	}

	public double[] getInputs() {
		return this.neuronValue[0];
	}

	public double[] getOutputs() {
		return this.neuronValue[this.numberOfLayers-1];
	}

	/**
	 * Initialize the network with random weights.
	 */
	public void randomize() {
		for (int layer = 1, prevLayer = 0; layer < this.numberOfLayers; layer++, prevLayer++) {
			for (int neuron = 0; neuron < this.layerSizes[layer]; neuron++) {
				this.threshold[layer][neuron] = (this.random.nextDouble() - 0.5)/2;
				for (int prevNeuron = 0; prevNeuron < this.layerSizes[prevLayer]; prevNeuron++) {
					this.weight[prevLayer][prevNeuron][neuron] = (this.random.nextDouble() - 0.5) / 2;
					this.lastWeightChange[prevLayer][prevNeuron][neuron] = 0.0;
				}
			}
		}
	}

	/**
	 * Compute the output values from the input values.
	 */
	public void forwardPropagate() {
		for (int layer = 1, prevLayer = 0; layer < this.numberOfLayers; layer++, prevLayer++) {
			ActivationFunction function = getActivationFunction(layer);
			for (int neuron = 0; neuron < this.layerSizes[layer]; neuron++) {
				double sum = 0.0;
				for (int prevNeuron = 0; prevNeuron < (this.layerSizes[prevLayer]); prevNeuron++) {
					sum = sum + (this.neuronValue[prevLayer][prevNeuron] * this.weight[prevLayer][prevNeuron][neuron]);
				}
				this.neuronValue[layer][neuron] = function.calculate(sum - this.threshold[layer][neuron]);
			}
		}
	}

	/**
	 * Compute the error in each output from the expected values and back propagate.
	 */
	public void backPropagate(double[] expectedOutputs) {
		double momentum = getMomentum();
		for (int layer = this.numberOfLayers-1, prevLayer = this.numberOfLayers - 2, nextLayer = this.numberOfLayers;
				layer > 0;
				layer--, prevLayer--, nextLayer--) {
			double prevLearningRate = getLearningRate(prevLayer);
			ActivationFunction function = getActivationFunction(layer);
			for (int neuron = 0; neuron < this.layerSizes[layer]; neuron++) {
				if (layer == this.outputLayer) {
					this.errorGradient[layer][neuron] =
							function.calculateInverse(this.neuronValue[layer][neuron])
								* (expectedOutputs[neuron] - this.neuronValue[layer][neuron]);
				} else {
					double sum = 0.0;
					for (int nextNeuron = 0; nextNeuron < this.layerSizes[nextLayer]; nextNeuron++) {
						sum += this.errorGradient[nextLayer][nextNeuron] * this.weight[layer][neuron][nextNeuron];
					}
					this.errorGradient[layer][neuron] = function.calculateInverse(this.neuronValue[layer][neuron]) * sum;
				}
				for (int prevNeuron = 0; prevNeuron < this.layerSizes[prevLayer]; prevNeuron ++) {
					double weightChange = prevLearningRate
							* this.neuronValue[prevLayer][prevNeuron]
							* this.errorGradient[layer][neuron];
					this.weight[prevLayer][prevNeuron][neuron] +=
						weightChange + (this.lastWeightChange[prevLayer][prevNeuron][neuron] * momentum);
					this.lastWeightChange[prevLayer][prevNeuron][neuron] = weightChange;
				}
				this.threshold[layer][neuron] -= prevLearningRate * this.errorGradient[layer][neuron];
			}
		}
	}
	
	public String printLayer(double[] layer) {
		StringWriter writer = new StringWriter();
		writer.write("[");
		boolean first = true;
		for (double value : layer) {
			if (first) {
				first = false;
			} else {
				writer.write(", ");
			}
			writer.write(String.valueOf((((int)(value * 100)))/100.0));
		}
		writer.write("]");
		return writer.toString();
	}
}

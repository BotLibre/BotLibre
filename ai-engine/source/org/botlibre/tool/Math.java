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
package org.botlibre.tool;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicTool;

/**
 * Does math.
 */

public class Math extends BasicTool {
	
	public static Math instance = new Math();

	public Math() {
	}
	
	public Vertex plus(Vertex source, Vertex left, Vertex right) {
		return add(source, left, right);
	}
	
	public Vertex minus(Vertex source, Vertex left, Vertex right) {
		return subtract(source, left, right);
	}
	
	public Vertex min(Vertex source, Vertex[] values) {
		Vertex min = null;
		double minValue = 0;
		for (Vertex vertex : values) {
			if ((vertex.getData() instanceof Number) && (min == null || ((Number)vertex.getData()).doubleValue() < minValue)) {
				min = vertex;
				minValue = ((Number)vertex.getData()).doubleValue();
			}
		}
		return min;
	}
	
	public Vertex max(Vertex source, Vertex[] values) {
		Vertex max = null;
		double maxValue = 0;
		for (Vertex vertex : values) {
			if ((vertex.getData() instanceof Number) && (max == null || ((Number)vertex.getData()).doubleValue() > maxValue)) {
				max = vertex;
				maxValue = ((Number)vertex.getData()).doubleValue();
			}
		}
		return max;
	}

	public Vertex add(Vertex source, Vertex left, Vertex right) {
		Object result = null;
		if ((left.getData() instanceof BigInteger) && (right.getData() instanceof BigInteger)) {
			BigInteger leftNumber = (BigInteger)left.getData();
			BigInteger rightNumber = (BigInteger)right.getData();
			result = leftNumber.add(rightNumber);			
		} else if ((left.getData() instanceof BigDecimal) || (right.getData() instanceof BigDecimal)) {
			BigDecimal leftNumber = null;
			BigDecimal rightNumber = null;
			if (left.getData() instanceof BigInteger) {
				leftNumber = new BigDecimal((BigInteger)left.getData());
			} else if (left.getData() instanceof BigDecimal) {
				leftNumber = (BigDecimal)left.getData();				
			}
			if (right.getData() instanceof BigInteger) {
				rightNumber = new BigDecimal((BigInteger)right.getData());
			} else if (right.getData() instanceof BigDecimal) {
				rightNumber = (BigDecimal)right.getData();				
			}
			if ((rightNumber != null) && (leftNumber != null)) {
				result = leftNumber.add(rightNumber);
			}
		} else if ((left.getData() instanceof Number) && (right.getData() instanceof Number)) {
			if ((left.getData() instanceof Double) || (right.getData() instanceof Double)) {
				result = ((Number)left.getData()).doubleValue() + ((Number)right.getData()).doubleValue();
			} else if ((left.getData() instanceof Long) || (right.getData() instanceof Long)) {
				result = ((Number)left.getData()).longValue() + ((Number)right.getData()).longValue();
			} else {
				result = ((Number)left.getData()).intValue() + ((Number)right.getData()).intValue();
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (right.is(Primitive.UNDEFINED)) {
			return right;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left;
		}
		if (right.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return right;
		}
		return left.getNetwork().createVertex(checkInteger(result));
	}

	public Vertex subtract(Vertex source, Vertex left, Vertex right) {
		Object result = null;
		if ((left.getData() instanceof BigInteger) && (right.getData() instanceof BigInteger)) {
			BigInteger leftNumber = (BigInteger)left.getData();
			BigInteger rightNumber = (BigInteger)right.getData();
			result = leftNumber.subtract(rightNumber);			
		} else if ((left.getData() instanceof BigDecimal) || (right.getData() instanceof BigDecimal)) {
			BigDecimal leftNumber = null;
			BigDecimal rightNumber = null;
			if (left.getData() instanceof BigInteger) {
				leftNumber = new BigDecimal((BigInteger)left.getData());
			} else if (left.getData() instanceof BigDecimal) {
				leftNumber = (BigDecimal)left.getData();				
			}
			if (right.getData() instanceof BigInteger) {
				rightNumber = new BigDecimal((BigInteger)right.getData());
			} else if (right.getData() instanceof BigDecimal) {
				rightNumber = (BigDecimal)right.getData();				
			}
			if ((rightNumber != null) && (leftNumber != null)) {
				result = leftNumber.subtract(rightNumber);
			}
		} else if ((left.getData() instanceof Number) && (right.getData() instanceof Number)) {
			if ((left.getData() instanceof Double) || (right.getData() instanceof Double)) {
				result = ((Number)left.getData()).doubleValue() +- ((Number)right.getData()).doubleValue();
			} else if ((left.getData() instanceof Long) || (right.getData() instanceof Long)) {
				result = ((Number)left.getData()).longValue() - ((Number)right.getData()).longValue();
			} else {
				result = ((Number)left.getData()).intValue() - ((Number)right.getData()).intValue();
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (right.is(Primitive.UNDEFINED)) {
			return right;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left;
		}
		if (right.is(Primitive.INFINITY)) {
			return left.getNetwork().createVertex(Primitive.NINFINITY);
		}
		if (right.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(Primitive.INFINITY);
		}
		return left.getNetwork().createVertex(checkInteger(result));
	}

	public Vertex multiply(Vertex source, Vertex left, Vertex right) {
		Object result = null;
		if ((left.getData() instanceof BigInteger) && (right.getData() instanceof BigInteger)) {
			BigInteger leftNumber = (BigInteger)left.getData();
			BigInteger rightNumber = (BigInteger)right.getData();
			result = leftNumber.multiply(rightNumber);			
		} else if ((left.getData() instanceof BigDecimal) || (right.getData() instanceof BigDecimal)) {
			BigDecimal leftNumber = null;
			BigDecimal rightNumber = null;
			if (left.getData() instanceof BigInteger) {
				leftNumber = new BigDecimal((BigInteger)left.getData());
			} else if (left.getData() instanceof BigDecimal) {
				leftNumber = (BigDecimal)left.getData();				
			}
			if (right.getData() instanceof BigInteger) {
				rightNumber = new BigDecimal((BigInteger)right.getData());
			} else if (right.getData() instanceof BigDecimal) {
				rightNumber = (BigDecimal)right.getData();				
			}
			if ((rightNumber != null) && (leftNumber != null)) {
				result = leftNumber.multiply(rightNumber);
			}
		} else if ((left.getData() instanceof Number) && (right.getData() instanceof Number)) {
			if ((left.getData() instanceof Double) || (right.getData() instanceof Double)) {
				result = ((Number)left.getData()).doubleValue() * ((Number)right.getData()).doubleValue();
			} else if ((left.getData() instanceof Long) || (right.getData() instanceof Long)) {
				result = ((Number)left.getData()).longValue() * ((Number)right.getData()).longValue();
			} else {
				result = ((Number)left.getData()).intValue() * ((Number)right.getData()).intValue();
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (right.is(Primitive.UNDEFINED)) {
			return right;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left;
		}
		if (right.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return right;
		}
		return left.getNetwork().createVertex(checkInteger(result));
	}

	public Vertex divide(Vertex source, Vertex left, Vertex right) {
		Object result = null;
		if ((left.getData() instanceof BigInteger) && (right.getData() instanceof BigInteger)) {
			BigInteger leftNumber = (BigInteger)left.getData();
			BigInteger rightNumber = (BigInteger)right.getData();
			if (rightNumber.signum() == 0) {
				if (leftNumber.signum() == 0) {
					return left.getNetwork().createVertex(Primitive.UNDEFINED);
				} else if (leftNumber.signum() < 0) {
					return left.getNetwork().createVertex(Primitive.NINFINITY);
				}
				return left.getNetwork().createVertex(Primitive.INFINITY);
			}
			try {
				BigInteger[] remainder = leftNumber.divideAndRemainder(rightNumber);
				if (remainder[1].signum() == 0) {
					result = remainder[0];
				}
			} catch (Exception failedAgain) {
				return left.getNetwork().createVertex(Primitive.INFINITY);
			}
		}
		if (result == null && (left.getData() instanceof Number) && (right.getData() instanceof Number)) {
			if ((left.getData() instanceof BigInteger) || (left.getData() instanceof BigDecimal)
						|| (right.getData() instanceof BigInteger) || (right.getData() instanceof BigDecimal)) {
				BigDecimal leftNumber = null;
				BigDecimal rightNumber = null;
				if (left.getData() instanceof BigInteger) {
					leftNumber = new BigDecimal((BigInteger)left.getData());
				} else if (left.getData() instanceof BigDecimal) {
					leftNumber = (BigDecimal)left.getData();
				} else {
					leftNumber = new BigDecimal(((Number)left.getData()).doubleValue());
				}
				if (right.getData() instanceof BigInteger) {
					rightNumber = new BigDecimal((BigInteger)right.getData());
				} else if (right.getData() instanceof BigDecimal) {
					rightNumber = (BigDecimal)right.getData();
				} else {
					rightNumber = new BigDecimal(((Number)right.getData()).doubleValue());
				}
				if ((rightNumber != null) && (leftNumber != null)) {
					if (rightNumber.signum() == 0) {
						if (leftNumber.signum() == 0) {
							return left.getNetwork().createVertex(Primitive.UNDEFINED);
						} else if (leftNumber.signum() < 0) {
							return left.getNetwork().createVertex(Primitive.NINFINITY);
						}
						return left.getNetwork().createVertex(Primitive.INFINITY);
					}
					try {
						result = leftNumber.divide(rightNumber);
					} catch (Exception failed) {
						try {
							result = leftNumber.divide(rightNumber, 10, BigDecimal.ROUND_UP);
						} catch (Exception failedAgain) {
							return left.getNetwork().createVertex(Primitive.INFINITY);
						}
					}
				}
			} else if ((left.getData() instanceof Number) && (right.getData() instanceof Number)) {
				if (((Number)right.getData()).doubleValue() == 0) {
					double leftValue = ((Number)left.getData()).doubleValue();
					if (leftValue == 0) {
						return left.getNetwork().createVertex(Primitive.UNDEFINED);
					}
					if (leftValue < 0) {
						return left.getNetwork().createVertex(Primitive.NINFINITY);
					}
					return left.getNetwork().createVertex(Primitive.INFINITY);
				}
				if ((left.getData() instanceof Double) || (right.getData() instanceof Double)) {
					result = ((Number)left.getData()).doubleValue() / ((Number)right.getData()).doubleValue();
				} else if ((left.getData() instanceof Long) || (right.getData() instanceof Long)) {
					result = ((Number)left.getData()).longValue() / ((Number)right.getData()).longValue();
				} else {
					result = ((Number)left.getData()).intValue() / ((Number)right.getData()).intValue();
				}
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (right.is(Primitive.UNDEFINED)) {
			return right;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left;
		}
		if (right.is(Primitive.INFINITY) || right.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(BigInteger.valueOf(0));
		}
		return left.getNetwork().createVertex(checkInteger(result));
	}

	public Vertex power(Vertex source, Vertex left, Vertex right) {
		if ((left.getData() instanceof Number) && (right.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.pow(((Number)left.getData()).doubleValue(), ((Number)right.getData()).doubleValue());
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left;
		}
		if (right.is(Primitive.INFINITY) || right.is(Primitive.NINFINITY)) {
			return right;
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}

	public Vertex sqrt(Vertex source, Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.sqrt((((Number)left.getData()).doubleValue()));
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);				
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left;
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Vertex ln(Vertex source, Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.log((((Number)left.getData()).doubleValue()));
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);				
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(Primitive.INFINITY);
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Vertex log(Vertex source, Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.log10((((Number)left.getData()).doubleValue()));
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);				
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(Primitive.INFINITY);
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Vertex round(Vertex source, Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.round((((Number)left.getData()).doubleValue()));
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);				
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(Primitive.INFINITY);
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Vertex random(Vertex source, Vertex max) {
		if ((max.getData() instanceof Number)) {
			int value = (((Number)max.getData()).intValue());
			return source.getNetwork().createVertex(org.botlibre.util.Utils.random(value));
		}
		return source.getNetwork().createVertex(0);
	}
	
	public Vertex random(Vertex source) {
		return source.getNetwork().createVertex(org.botlibre.util.Utils.random().nextDouble());
	}
	
	public Vertex floor(Vertex source, Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.floor((((Number)left.getData()).doubleValue()));
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(Primitive.INFINITY);
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Vertex ceil(Vertex source, Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.ceil((((Number)left.getData()).doubleValue()));
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);				
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(Primitive.INFINITY);
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Vertex abs(Vertex source, Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.abs((((Number)left.getData()).doubleValue()));
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);				
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(Primitive.INFINITY);
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Vertex sin(Vertex source, Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.sin((((Number)left.getData()).doubleValue()));
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);				
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(BigInteger.valueOf(0));
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Vertex cos(Vertex source, Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.cos((((Number)left.getData()).doubleValue()));
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);				
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(Primitive.UNDEFINED);
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Vertex tan(Vertex source, Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.tan((((Number)left.getData()).doubleValue()));
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);				
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(Primitive.UNDEFINED);
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Vertex atan(Vertex source, Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.atan((((Number)left.getData()).doubleValue()));
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);				
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(Primitive.UNDEFINED);
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Vertex asin(Vertex source, Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.asin((((Number)left.getData()).doubleValue()));
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);				
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(BigInteger.valueOf(0));
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Vertex acos(Vertex source, Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.acos((((Number)left.getData()).doubleValue()));
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);				
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(Primitive.UNDEFINED);
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Vertex sinh(Vertex source, Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.sinh((((Number)left.getData()).doubleValue()));
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);				
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(BigInteger.valueOf(0));
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Vertex cosh(Vertex source, Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.cosh((((Number)left.getData()).doubleValue()));
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);				
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(Primitive.UNDEFINED);
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Vertex tanh(Vertex source, Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = java.lang.Math.tanh((((Number)left.getData()).doubleValue()));
				if (Double.isInfinite(result)) {
					return left.getNetwork().createVertex(Primitive.INFINITY);				
				}
				BigDecimal decimal = BigDecimal.valueOf(result);
				return left.getNetwork().createVertex(checkInteger(decimal));
			} catch (Exception failed) {
				return left.getNetwork().createVertex(Primitive.UNDEFINED);
			}
		}
		if (left.is(Primitive.UNDEFINED)) {
			return left;
		}
		if (left.is(Primitive.INFINITY) || left.is(Primitive.NINFINITY)) {
			return left.getNetwork().createVertex(Primitive.UNDEFINED);
		}
		return left.getNetwork().createVertex(Primitive.NULL);
	}
	
	public Object checkInteger(Object result) {
		if (result == null) {
			return Primitive.NULL;
		}
		if (result instanceof BigDecimal) {
			BigDecimal decimal = (BigDecimal)result;
			decimal = decimal.stripTrailingZeros();
			if (decimal.signum() == 0 || decimal.scale() <= 0) {
				result = decimal.toBigInteger();
			} else {
				result = decimal;
			}
		}
		return result;
	}
	
}
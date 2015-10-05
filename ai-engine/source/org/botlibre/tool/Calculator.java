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

public class Calculator extends BasicTool {

	public Calculator() {
	}
	
	public Vertex plus(Vertex left, Vertex right) {
		return add(left, right);
	}
	
	public Vertex minus(Vertex left, Vertex right) {
		return subtract(left, right);
	}

	public Vertex add(Vertex left, Vertex right) {
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

	public Vertex subtract(Vertex left, Vertex right) {
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

	public Vertex multiply(Vertex left, Vertex right) {
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

	public Vertex divide(Vertex left, Vertex right) {
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
		if (result == null) {
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
		}
		return left.getNetwork().createVertex(checkInteger(result));
	}

	public Vertex power(Vertex left, Vertex right) {
		if ((left.getData() instanceof Number) && (right.getData() instanceof Number)) {
			try {
				double result = Math.pow(((Number)left.getData()).doubleValue(), ((Number)right.getData()).doubleValue());
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

	public Vertex sqrt(Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = Math.sqrt((((Number)left.getData()).doubleValue()));
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
	
	public Vertex ln(Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = Math.log((((Number)left.getData()).doubleValue()));
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
	
	public Vertex log(Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = Math.log10((((Number)left.getData()).doubleValue()));
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
	
	public Vertex round(Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = Math.round((((Number)left.getData()).doubleValue()));
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
	
	public Vertex floor(Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = Math.floor((((Number)left.getData()).doubleValue()));
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
	
	public Vertex ceil(Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = Math.ceil((((Number)left.getData()).doubleValue()));
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
	
	public Vertex abs(Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = Math.abs((((Number)left.getData()).doubleValue()));
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
	
	public Vertex sin(Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = Math.sin((((Number)left.getData()).doubleValue()));
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
	
	public Vertex cos(Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = Math.cos((((Number)left.getData()).doubleValue()));
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
	
	public Vertex tan(Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = Math.tan((((Number)left.getData()).doubleValue()));
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
	
	public Vertex atan(Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = Math.atan((((Number)left.getData()).doubleValue()));
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
	
	public Vertex asin(Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = Math.asin((((Number)left.getData()).doubleValue()));
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
	
	public Vertex acos(Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = Math.acos((((Number)left.getData()).doubleValue()));
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
	
	public Vertex sinh(Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = Math.sinh((((Number)left.getData()).doubleValue()));
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
	
	public Vertex cosh(Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = Math.cosh((((Number)left.getData()).doubleValue()));
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
	
	public Vertex tanh(Vertex left) {
		if ((left.getData() instanceof Number)) {
			try {
				double result = Math.tanh((((Number)left.getData()).doubleValue()));
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
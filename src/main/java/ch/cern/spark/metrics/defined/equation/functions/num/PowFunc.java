package ch.cern.spark.metrics.defined.equation.functions.num;

import java.text.ParseException;

import ch.cern.spark.metrics.defined.equation.ValueComputable;

public class PowFunc extends BiNumericFunction{
	
	public static char REPRESENTATION = '^';

	public PowFunc(ValueComputable v1, ValueComputable v2) throws ParseException {
		super(v1, v2);
	}

	@Override
	public float compute(float value1, float value2) {
		return (float) Math.pow(value1, value2);
	}

	@Override
	public String toString() {
		return "(" + v1 + " " + REPRESENTATION + " " + v2 + ")";
	}

	@Override
	public String getFunctionRepresentation() {
		return REPRESENTATION + "";
	}

}

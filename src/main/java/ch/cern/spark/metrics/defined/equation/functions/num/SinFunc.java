package ch.cern.spark.metrics.defined.equation.functions.num;

import java.text.ParseException;

import ch.cern.spark.metrics.defined.equation.ValueComputable;

public class SinFunc extends NumericFunction {
	
	public static String REPRESENTATION = "sin";

	public SinFunc(ValueComputable v) throws ParseException {
		super(v);
	}

	@Override
	public float compute(float value) {
		return (float) Math.sin(Math.toRadians(value));
	}
	
	@Override
	public String toString() {
		return REPRESENTATION + "(" + v + ")";
	}

	@Override
	public String getFunctionRepresentation() {
		return REPRESENTATION + "";
	}

}

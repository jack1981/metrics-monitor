package ch.cern.spark.metrics.defined.equation.functions.num;

import java.text.ParseException;

import ch.cern.spark.metrics.defined.equation.ValueComputable;

public class AbsFunc extends NumericFunction {
	
	public static String REPRESENTATION = "abs";

	public AbsFunc(ValueComputable v) throws ParseException {
		super(v);
	}

	@Override
	public float compute(float value) {
		return (float) Math.abs(value);
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

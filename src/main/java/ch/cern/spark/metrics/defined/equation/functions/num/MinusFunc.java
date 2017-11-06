package ch.cern.spark.metrics.defined.equation.functions.num;

import java.text.ParseException;

import ch.cern.spark.metrics.defined.equation.ValueComputable;

public class MinusFunc extends NumericFunction {
	
	public static char REPRESENTATION = '-';

	public MinusFunc(ValueComputable v) throws ParseException {
		super(v);
	}

	@Override
	public float compute(float value) {
		return -value;
	}
	
	@Override
	public String toString() {
		return "-" + v;
	}

	@Override
	public String getFunctionRepresentation() {
		return REPRESENTATION + "";
	}

}

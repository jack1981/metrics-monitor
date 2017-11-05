package ch.cern.spark.metrics.defined.equation.functions.num;

import java.text.ParseException;

import ch.cern.spark.metrics.defined.equation.Computable;
import ch.cern.spark.metrics.value.Value;

public class MinusFunc extends NumericFunction {
	
	public static char REPRESENTATION = '-';

	public MinusFunc(Computable<? extends Value> v) throws ParseException {
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

package ch.cern.spark.metrics.defined.equation.functions.num;

import java.text.ParseException;

import ch.cern.spark.metrics.defined.equation.Computable;
import ch.cern.spark.metrics.value.Value;

public class TanFunc extends NumericFunction {
	
	public static String REPRESENTATION = "tan";

	public TanFunc(Computable<? extends Value> v) throws ParseException {
		super(v);
	}

	@Override
	public float compute(float value) {
		return (float) Math.tan(Math.toRadians(value));
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

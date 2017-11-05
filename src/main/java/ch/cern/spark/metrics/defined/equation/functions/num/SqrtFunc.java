package ch.cern.spark.metrics.defined.equation.functions.num;

import java.text.ParseException;

import ch.cern.spark.metrics.defined.equation.Computable;
import ch.cern.spark.metrics.value.Value;

public class SqrtFunc extends NumericFunction {
	
	public static String REPRESENTATION = "sqrt";

	public SqrtFunc(Computable<? extends Value> v) throws ParseException {
		super(v);
	}

	@Override
	public float compute(float value) {
		return (float) Math.sqrt(value);
	}

	@Override
	public String getFunctionRepresentation() {
		return REPRESENTATION;
	}
	
	@Override
	public String toString() {
		return REPRESENTATION + "(" + v + ")";
	}

}

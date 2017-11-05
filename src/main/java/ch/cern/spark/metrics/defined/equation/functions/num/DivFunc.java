package ch.cern.spark.metrics.defined.equation.functions.num;

import java.text.ParseException;

import ch.cern.spark.metrics.defined.equation.Computable;
import ch.cern.spark.metrics.value.Value;

public class DivFunc extends BiNumericalFunction{
	
	public static char REPRESENTATION = '/';

	public DivFunc(Computable<? extends Value> v1, Computable<? extends Value> v2) throws ParseException {
		super(v1, v2);
	}

	@Override
	public float compute(float value1, float value2) {
		return value1 / value2;
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

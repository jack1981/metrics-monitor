package ch.cern.spark.metrics.defined.equation.functions.num;

import java.text.ParseException;
import java.time.Instant;
import java.util.Optional;

import ch.cern.spark.metrics.defined.DefinedMetricStore;
import ch.cern.spark.metrics.defined.equation.Computable;
import ch.cern.spark.metrics.defined.equation.ComputationException;
import ch.cern.spark.metrics.value.FloatValue;
import ch.cern.spark.metrics.value.Value;

public abstract class NumericFunction implements Computable<FloatValue>{
	
	protected Computable<FloatValue> v;

	public NumericFunction(Computable<? extends Value> v) throws ParseException {
		this.v = toFloatValue(v);
	}

	@SuppressWarnings("unchecked")
	private Computable<FloatValue> toFloatValue(Computable<? extends Value> input) throws ParseException {
		Class<? extends Value> inputClass = input.returnType();
		
		if(!inputClass.equals(FloatValue.class))
			throw new ParseException("Function " + getFunctionRepresentation() + " expects float value", 0);
		
		return (Computable<FloatValue>) input;
	}

	@Override
	public FloatValue compute(DefinedMetricStore store, Instant time) throws ComputationException {
		Optional<Float> value = v.compute(store, time).getAsFloat();
		
		return new FloatValue(compute(value.get()));
	}
	
	@Override
	public Class<FloatValue> returnType() {
		return FloatValue.class;
	}

	public abstract String getFunctionRepresentation();
	
	public abstract float compute(float value);
	
}

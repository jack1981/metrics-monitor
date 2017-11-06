package ch.cern.spark.metrics.defined.equation.functions.num;

import java.text.ParseException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import ch.cern.spark.metrics.defined.DefinedMetricStore;
import ch.cern.spark.metrics.defined.equation.Computable;
import ch.cern.spark.metrics.defined.equation.ComputationException;
import ch.cern.spark.metrics.value.FloatValue;
import ch.cern.spark.metrics.value.Value;

public abstract class BiNumericalFunction implements Computable<FloatValue>{
	
	protected Computable<FloatValue> v1;
	
	protected Computable<FloatValue> v2;

	public BiNumericalFunction(Computable<? extends Value> v1, Computable<? extends Value> v2) throws ParseException {
		this.v1 = toFloatValue(v1, " as first argument");
		this.v2 = toFloatValue(v2, " as second argument");
	}
	
	@SuppressWarnings("unchecked")
	private Computable<FloatValue> toFloatValue(Computable<? extends Value> input, String subix) throws ParseException {
		Class<? extends Value> inputClass = input.returnType();
		
		if(!inputClass.equals(FloatValue.class))
			throw new ParseException("Function " + getFunctionRepresentation() + " expects float value" + subix, 0);
		
		return (Computable<FloatValue>) input;
	}

	@Override
	public FloatValue compute(DefinedMetricStore store, Instant time) throws ComputationException {
		List<ComputationException> exceptions = new LinkedList<>();
		
		FloatValue value1 = null;
		try {
			value1 = v1.compute(store, time);
		} catch (ComputationException e) {
			exceptions.add(e);
		}
		
		FloatValue value2 = null;
		try {
			value2 = v2.compute(store, time);
		} catch (ComputationException e) {
			exceptions.add(e);
		}
		
		if(exceptions.size() > 0)
			throw new ComputationException(exceptions);
		
		return new FloatValue(compute(value1.getAsFloat().get(), value2.getAsFloat().get()));
	}

	@Override
	public Class<? extends Value> returnType() {
		return FloatValue.class;
	}
	
	public abstract String getFunctionRepresentation();

	public abstract float compute(float value1, float value2);
	
}

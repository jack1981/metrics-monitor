package ch.cern.spark.metrics.defined.equation.functions.num;

import java.text.ParseException;
import java.time.Instant;

import ch.cern.spark.metrics.defined.DefinedMetricStore;
import ch.cern.spark.metrics.defined.equation.ValueComputable;
import ch.cern.spark.metrics.value.FloatValue;
import ch.cern.spark.metrics.value.Value;

public abstract class NumericFunction implements ValueComputable{
	
	protected ValueComputable v;

	public NumericFunction(ValueComputable v) throws ParseException {
		checkIfFloatValue(v, " as input argument");
		
		this.v = v;
	}
	
	private void checkIfFloatValue(ValueComputable input, String subix) throws ParseException {
		Class<? extends Value> inputClass = input.returnType();
		
		if(!inputClass.equals(FloatValue.class))
			throw new ParseException("Function " + getFunctionRepresentation() + " expects float value" + subix, 0);
	}

	@Override
	public Value compute(DefinedMetricStore store, Instant time) {
		Value value = v.compute(store, time);		
		
		if(value.getAsException().isPresent())
			return value;
		
		return new FloatValue(compute(value.getAsFloat().get()));
	}
	
	@Override
	public Class<FloatValue> returnType() {
		return FloatValue.class;
	}

	public abstract String getFunctionRepresentation();
	
	public abstract float compute(float value);
	
}

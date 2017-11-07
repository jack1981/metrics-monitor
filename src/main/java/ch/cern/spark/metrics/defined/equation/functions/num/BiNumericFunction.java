package ch.cern.spark.metrics.defined.equation.functions.num;

import java.text.ParseException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import ch.cern.spark.metrics.defined.DefinedMetricStore;
import ch.cern.spark.metrics.defined.equation.ValueComputable;
import ch.cern.spark.metrics.value.ExceptionValue;
import ch.cern.spark.metrics.value.FloatValue;
import ch.cern.spark.metrics.value.Value;

public abstract class BiNumericFunction implements ValueComputable{
	
	protected ValueComputable v1;
	
	protected ValueComputable v2;

	public BiNumericFunction(ValueComputable v1, ValueComputable v2) throws ParseException {
		checkIfFloatValue(v1, " as first argument");
		checkIfFloatValue(v2, " as second argument");
		
		this.v1 = v1;
		this.v2 = v2;
	}
	
	private void checkIfFloatValue(ValueComputable input, String subix) throws ParseException {
		Class<? extends Value> inputClass = input.returnType();
		
		if(!inputClass.equals(FloatValue.class))
			throw new ParseException("Function " + getFunctionRepresentation() + " expects float value" + subix, 0);
	}

	@Override
	public Value compute(DefinedMetricStore store, Instant time) {
		List<ExceptionValue> exceptions = new LinkedList<>();
		
		Value value1 = v1.compute(store, time);		
		if(value1.getAsException().isPresent())
			exceptions.add(new ExceptionValue("first argument: " + value1.getAsException().get() + "."));
		Optional<Float> floatOption1 = value1.getAsFloat();
		if(!value1.getAsException().isPresent() && !floatOption1.isPresent())
			exceptions.add(new ExceptionValue("first argument: received value is not float."));
		
		Value value2 = v2.compute(store, time);
		if(value2.getAsException().isPresent())
			exceptions.add(new ExceptionValue("second argument: " + value2.getAsException().get() + "."));
		Optional<Float> floatOption2 = value2.getAsFloat();
		if(!value2.getAsException().isPresent() && !floatOption2.isPresent())
			exceptions.add(new ExceptionValue("second argument: received value is not float."));
		
		if(exceptions.size() > 0)
			return new ExceptionValue(exceptions);
		
		return new FloatValue(compute(floatOption1.get(), value2.getAsFloat().get()));
	}

	@Override
	public Class<FloatValue> returnType() {
		return FloatValue.class;
	}
	
	public abstract String getFunctionRepresentation();

	public abstract float compute(float value1, float value2);
	
}

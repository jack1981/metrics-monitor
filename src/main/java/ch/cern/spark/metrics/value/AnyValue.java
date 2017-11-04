package ch.cern.spark.metrics.value;

import java.util.Optional;

public class AnyValue extends Value {

	private static final long serialVersionUID = -3934521845229661541L;

	@Override
	public Optional<Float> getAsFloat() {
		return Optional.of((float) Math.random()) ;
	}
	
	@Override
	public Optional<String> getAsString() {
		return Optional.of("anyString");
	}
	
	@Override
	public Optional<Boolean> getAsBoolean() {
		return Optional.of(true);
	}
	
	@Override
	public String toString() {
		return "1";
	}
	
}

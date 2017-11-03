package ch.cern.spark.metrics.value;

import java.util.Optional;

public class BooleanValue extends Value {

	private static final long serialVersionUID = 6026199196915653369L;

	private boolean booleanValue;
	
	public BooleanValue(boolean value){
		this.booleanValue = value;
	}

	@Override
	public Optional<Boolean> getAsBoolean() {
		return Optional.of(booleanValue);
	}
	
	@Override
	public int compareTo(Value other) {
		if(other.getAsBoolean().isPresent())
			return booleanValue == other.getAsBoolean().get() ? 0 : -1;
		
		return Integer.MIN_VALUE;
	}

	public static BooleanValue from(String value_string) {
		return new BooleanValue(Boolean.parseBoolean(value_string));
	}

	@Override
	public String toString() {
		return "BooleanValue [" + booleanValue + "]";
	}

}

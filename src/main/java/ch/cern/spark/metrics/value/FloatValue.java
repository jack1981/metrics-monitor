package ch.cern.spark.metrics.value;

import java.util.Optional;

public class FloatValue extends Value {

	private static final long serialVersionUID = 6026199196915653369L;

	private float floatValue;
	
	public FloatValue(float value){
		this.floatValue = value;
	}
	
	public FloatValue(double value) {
		this.floatValue = (float) value;
	}

	@Override
	public Optional<Float> getAsFloat() {
		return Optional.of(floatValue);
	}
	
	@Override
	public int compareTo(Value other) {
		if(other.getAsFloat().isPresent())
			return Float.compare(floatValue, other.getAsFloat().get());
		
		return Integer.MIN_VALUE;
	}

	public static FloatValue from(String value_string) {
		return new FloatValue(Float.parseFloat(value_string));
	}

	@Override
	public String toString() {
		return "FloatValue [" + floatValue + "]";
	}

}

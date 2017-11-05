package ch.cern.spark.metrics.value;

import java.time.Instant;
import java.util.Optional;

import ch.cern.spark.metrics.defined.DefinedMetricStore;

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
	public Value compute(DefinedMetricStore store, Instant time) {
		return new FloatValue(floatValue);
	}

	@Override
	public Optional<Float> getAsFloat() {
		return Optional.of(this.floatValue);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(floatValue);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FloatValue other = (FloatValue) obj;
		if (Float.floatToIntBits(floatValue) != Float.floatToIntBits(other.floatValue))
			return false;
		return true;
	}

	public static FloatValue from(String value_string) {
		return new FloatValue(Float.parseFloat(value_string));
	}

	@Override
	public String toString() {
		return Float.toString(floatValue);
	}
	
}

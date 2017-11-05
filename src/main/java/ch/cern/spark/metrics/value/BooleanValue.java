package ch.cern.spark.metrics.value;

import java.time.Instant;
import java.util.Optional;

import ch.cern.spark.metrics.defined.DefinedMetricStore;
import ch.cern.spark.metrics.defined.equation.ComputationException;

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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (booleanValue ? 1231 : 1237);
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
		BooleanValue other = (BooleanValue) obj;
		if (booleanValue != other.booleanValue)
			return false;
		return true;
	}

	public static BooleanValue from(String value_string) {
		return new BooleanValue(Boolean.parseBoolean(value_string));
	}

	@Override
	public String toString() {
		return Boolean.toString(booleanValue);
	}

	@Override
	public Value compute(DefinedMetricStore store, Instant time) throws ComputationException {
		return new BooleanValue(booleanValue);
	}

}

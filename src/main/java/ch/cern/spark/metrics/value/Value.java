package ch.cern.spark.metrics.value;
import java.io.Serializable;
import java.util.Optional;

public abstract class Value implements Serializable, Cloneable, Comparable<Value> {

	private static final long serialVersionUID = -5082571575744839753L;

	public Optional<Float> getAsFloat() {
		return Optional.empty();
	}
	
	public Optional<String> getAsString() {
		return Optional.empty();
	}
	
	public Optional<Exception> getAsException() {
		return Optional.empty();
	}

}

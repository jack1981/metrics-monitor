package ch.cern.spark.metrics.value;

import java.util.Optional;

public class StringValue extends Value {

	private static final long serialVersionUID = 6026199196915653369L;

	private String stringValue;
	
	public StringValue(String value){
		this.stringValue = value;
	}

	@Override
	public Optional<String> getAsString() {
		return Optional.of(stringValue);
	}
	
	@Override
	public int compareTo(Value other) {
		if(other.getAsString().isPresent())
			return stringValue.compareTo(other.getAsString().get());
		
		return Integer.MIN_VALUE;
	}

	@Override
	public String toString() {
		return "StringValue [" + stringValue + "]";
	}

}

package ch.cern.spark.metrics.value;

import java.util.Optional;

public class ExceptionValue extends Value {

	private static final long serialVersionUID = 8938782791564766439L;

	private Exception exception;
	
	public ExceptionValue(Exception exception) {
		this.exception = exception;
	}

	@Override
	public Optional<Exception> getAsException() {
		return Optional.of(exception);
	}
	
	@Override
	public int compareTo(Value o) {
		return 0;
	}
	
	@Override
	public String toString() {
		return "Exception: " + exception;
	}

}

package ch.cern.spark.metrics.value;

import java.time.Instant;
import java.util.Optional;

import ch.cern.spark.metrics.defined.DefinedMetricStore;
import ch.cern.spark.metrics.defined.equation.ComputationException;

public class ExceptionValue extends Value {

	private static final long serialVersionUID = 8938782791564766439L;

	private Exception exception;

	public ExceptionValue(String message) {
		this.exception = new Exception(message);
	}

	public ExceptionValue(Exception exception) {
		this.exception = exception;
	}

	@Override
	public Optional<Exception> getAsException() {
		return Optional.of(exception);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((exception == null) ? 0 : exception.hashCode());
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
		ExceptionValue other = (ExceptionValue) obj;
		if (exception == null) {
			if (other.exception != null)
				return false;
		} else if (!exception.equals(other.exception))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ExceptionValue [" + exception + "]";
	}

	@Override
	public Value compute(DefinedMetricStore store, Instant time) throws ComputationException {
		return new ExceptionValue(exception);
	}

}

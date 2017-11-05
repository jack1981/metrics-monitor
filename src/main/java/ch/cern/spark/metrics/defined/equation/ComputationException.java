package ch.cern.spark.metrics.defined.equation;

import java.util.LinkedList;
import java.util.List;

public class ComputationException extends Exception {

	private static final long serialVersionUID = -5363749169688550737L;
	
	private List<ComputationException> exceptions;

	public ComputationException(String message) {
		super(message);
	}

	public ComputationException(ComputationException... inputExceptions) {
		exceptions = new LinkedList<>();
		
		for (ComputationException exception : inputExceptions)
			exceptions.addAll(exception.getExceptions());
	}

	public ComputationException(List<ComputationException> inputExceptions) {
		exceptions = new LinkedList<>();
		
		for (ComputationException exception : inputExceptions)
			exceptions.addAll(exception.getExceptions());
	}

	private List<ComputationException> getExceptions() {
		if(exceptions == null) {
			exceptions = new LinkedList<>();
			exceptions.add(this);
		}
		
		return exceptions;
	}
	
	@Override
	public String toString() {
		if(exceptions != null)
			return "ComputationExceptions: " + exceptions;
		else
			return super.getMessage();
	}
	
	@Override
	public String getMessage() {
		return toString();
	}
	
}

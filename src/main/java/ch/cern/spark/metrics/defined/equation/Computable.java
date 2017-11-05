package ch.cern.spark.metrics.defined.equation;

import java.time.Instant;

import ch.cern.spark.metrics.defined.DefinedMetricStore;
import ch.cern.spark.metrics.value.Value;

public interface Computable<T extends Value> {

	public T compute(DefinedMetricStore store, Instant time) throws ComputationException;
	
}

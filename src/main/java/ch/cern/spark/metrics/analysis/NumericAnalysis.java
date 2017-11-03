package ch.cern.spark.metrics.analysis;

import java.time.Instant;

import ch.cern.spark.metrics.results.AnalysisResult;
import ch.cern.spark.metrics.value.Value;

public abstract class NumericAnalysis extends Analysis {

    private static final long serialVersionUID = -1822474093334300773L;

	@Override
	public AnalysisResult process(Instant timestamp, Value value) {
		return process(timestamp, value.getAsFloat().get());
	}

    public abstract AnalysisResult process(Instant timestamp, double value);

}
    

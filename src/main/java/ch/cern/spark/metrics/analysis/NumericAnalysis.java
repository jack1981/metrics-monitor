package ch.cern.spark.metrics.analysis;

import java.time.Instant;

import ch.cern.spark.metrics.Metric;
import ch.cern.spark.metrics.results.AnalysisResult;
import ch.cern.spark.metrics.value.ExceptionValue;

public abstract class NumericAnalysis extends Analysis {

    private static final long serialVersionUID = -1822474093334300773L;

	@Override
	public AnalysisResult process(Metric metric) {
		if(!metric.getValue().getAsFloat().isPresent()) {
			AnalysisResult result = new AnalysisResult(); 
			
			ExceptionValue exception = new ExceptionValue("Metric is not numeric. Current analysis requires numeric values.");
			Metric exceptionMetric = new Metric(metric.getInstant(), exception, metric.getIDs());
			
			result.setAnalyzedMetric(exceptionMetric);
			
	        return result;
		}
		
		return process(metric.getInstant(), metric.getValue().getAsFloat().get());
	}

    public abstract AnalysisResult process(Instant timestamp, double value);

}
    

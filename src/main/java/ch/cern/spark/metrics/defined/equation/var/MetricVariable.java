package ch.cern.spark.metrics.defined.equation.var;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Predicate;

import ch.cern.properties.ConfigurationException;
import ch.cern.properties.Properties;
import ch.cern.spark.metrics.Metric;
import ch.cern.spark.metrics.defined.DefinedMetricStore;
import ch.cern.spark.metrics.defined.equation.ValueComputable;
import ch.cern.spark.metrics.filter.MetricsFilter;
import ch.cern.spark.metrics.value.ExceptionValue;
import ch.cern.spark.metrics.value.FloatValue;
import ch.cern.spark.metrics.value.Value;

public abstract class MetricVariable implements ValueComputable, Predicate<Metric> {

	protected String name;
	
	private MetricsFilter filter;

	protected Duration expirePeriod;

	public MetricVariable(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public MetricVariable config(Properties properties) throws ConfigurationException {
		filter = MetricsFilter.build(properties.getSubset("filter"));
		
		if(properties.containsKey("expire") && properties.getProperty("expire").toLowerCase().equals("never"))
			expirePeriod = null;
		else
			expirePeriod = properties.getPeriod("expire", Duration.ofMinutes(10));
		
		return this;
	}

	@Override
	public boolean test(Metric metric) {
		return filter.test(metric);
	}

	public abstract void updateStore(DefinedMetricStore store, Metric metric);

	@Override
	public Value compute(DefinedMetricStore store, Instant time){
		try {
			return new FloatValue(computeValue(store, time));
		}catch(ComputationException e) {
			return new ExceptionValue("MetricVariable (name: " + name + "): " + e.getMessage());
		}
	}
	
	protected abstract float computeValue(DefinedMetricStore store, Instant time) throws ComputationException;
	
}

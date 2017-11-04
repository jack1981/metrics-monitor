package ch.cern.spark.metrics.defined;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.apache.kafka.common.config.ConfigException;

import ch.cern.properties.ConfigurationException;
import ch.cern.properties.Properties;
import ch.cern.spark.Pair;
import ch.cern.spark.metrics.DatedValue;
import ch.cern.spark.metrics.Metric;
import ch.cern.spark.metrics.filter.MetricsFilter;
import ch.cern.spark.metrics.value.ExceptionValue;
import ch.cern.spark.metrics.value.FloatValue;
import ch.cern.spark.metrics.value.Value;

public class Variable implements Predicate<Metric>{

	private String name;
	
	private MetricsFilter filter;
	
	public enum Operation {SUM, AVG, WEIGHTED_AVG, MIN, MAX, COUNT, DIFF};
	private Operation aggregateOperation;

	private Duration expirePeriod;

	public Variable(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public Variable config(Properties properties) throws ConfigurationException {
		filter = MetricsFilter.build(properties.getSubset("filter"));
		
		if(properties.containsKey("expire") && properties.getProperty("expire").toLowerCase().equals("never"))
			expirePeriod = null;
		else
			expirePeriod = properties.getPeriod("expire", Duration.ofMinutes(10));
		
		String aggregateVal = properties.getProperty("aggregate");
		if(aggregateVal != null)
			try{
				aggregateOperation = Operation.valueOf(aggregateVal.toUpperCase());
			}catch(IllegalArgumentException e) {
				throw new ConfigException("Variable " + name + ": aggregation operation (" + aggregateVal + ") not available");
			}
		
		return this;
	}

	@Override
	public boolean test(Metric metric) {
		return filter.test(metric);
	}

	public void updateStore(DefinedMetricStore store, Metric metric) {	
		if(aggregateOperation == null)
			store.updateValue(name, metric.getValue(), metric.getInstant());
		else
			store.updateAggregatedValue(name, metric.getIDs().hashCode(), metric.getValue(), metric.getInstant());
	}

	public Value compute(DefinedMetricStore store, Instant time) {
		Optional<Instant> oldestUpdate = Optional.empty();
		if(expirePeriod != null)
			oldestUpdate = Optional.of(time.minus(expirePeriod));
		store.purge(name, oldestUpdate);
		
		Value val = null;
		if(aggregateOperation == null)
			val = store.getValue(name);
		else
			switch (aggregateOperation) {
			case SUM:
				val = sumAggregation(store.getAggregatedValues(name));
				break;
			case COUNT:
				val = new FloatValue(store.getAggregatedValues(name).size());
				break;
			case AVG:
				val = averageAggregation(store.getAggregatedValues(name));
				break;
			case WEIGHTED_AVG:
				val = weightedAverageAggregation(store.getAggregatedDatedValues(name), time);
				break;
			case MIN:
				val = minAggregation(store.getAggregatedValues(name));
				break;
			case MAX:
				val = maxAggregation(store.getAggregatedValues(name));
				break;
			case DIFF:
				val = differenceAggregation(store.getAggregatedDatedValues(name));
				break;
			default:
				val = new ExceptionValue("Agreggation operation (" + aggregateOperation + ") is not available.");
				break;
			}
		
		if(val.getAsException().isPresent())
			val = new ExceptionValue("Variable (" + name + "): " + val.getAsException().get().getMessage());
		
		return val;
	}

	private Value averageAggregation(List<Value> aggregatedValues) {
		DoubleStream doubleStream = toDoubleStream(aggregatedValues);
		
		OptionalDouble average = doubleStream.average();
		
		if(average.isPresent())
			return new FloatValue(average.getAsDouble());
		else
			return new ExceptionValue("Average aggregation: no float values.");
	}
	
	private Value minAggregation(List<Value> aggregatedValues) {
		DoubleStream doubleStream = toDoubleStream(aggregatedValues);
		
		OptionalDouble average = doubleStream.min();
		
		if(average.isPresent())
			return new FloatValue(average.getAsDouble());
		else
			return new ExceptionValue("Minimum aggregation: no float values.");
	}
	
	private Value maxAggregation(List<Value> aggregatedValues) {
		DoubleStream doubleStream = toDoubleStream(aggregatedValues);
		
		OptionalDouble average = doubleStream.max();
		
		if(average.isPresent())
			return new FloatValue(average.getAsDouble());
		else
			return new ExceptionValue("Maximum aggregation: no float values.");
	}

	private FloatValue sumAggregation(List<Value> aggregatedValues) {
		DoubleStream doubleStream = toDoubleStream(aggregatedValues);
		
		return new FloatValue(doubleStream.sum());
	}

	private DoubleStream toDoubleStream(List<Value> aggregatedValues) {
		return aggregatedValues.stream()
				.filter(val -> val.getAsFloat().isPresent())
				.mapToDouble(val -> val.getAsFloat().get());
	}

	private Value weightedAverageAggregation(List<DatedValue> values, Instant time) {
        Optional<Pair<Double, Double>> pairSum = values.stream().filter(value -> value.getValue().getAsFloat().isPresent())
				.map(value -> {
					double weight = computeWeight(time, value.getInstant());
			    	
		    		return new Pair<Double, Double>(weight, weight * value.getValue().getAsFloat().get());
				})
				.reduce((p1, p2) -> new Pair<Double, Double>(p1.first + p2.first, p1.second + p2.second));

		if(!pairSum.isPresent())
			return new ExceptionValue("Weighted average aggregation: there are no float values to comute.");
		
		double totalWeights = pairSum.get().first;
		double weightedValues = pairSum.get().second;
		
		return new FloatValue(weightedValues / totalWeights);
	}

    private float computeWeight(Instant time, Instant metric_timestamp) {
        Duration time_difference = Duration.between(time, metric_timestamp).abs();
        
        if(expirePeriod.compareTo(time_difference) < 0)
        		return 0;
        				
        return (float) (expirePeriod.getSeconds() - time_difference.getSeconds()) / (float) expirePeriod.getSeconds();
    }
    
	private Value differenceAggregation(List<DatedValue> aggregatedDatedValues) {
		if(aggregatedDatedValues.size() < 2) {
			return new ExceptionValue("Difference aggregation: there is no previous value.");
		}else {
			List<DatedValue> sorted = aggregatedDatedValues.stream()
												.filter(value -> value.getValue().getAsFloat().isPresent())
												.sorted()
												.collect(Collectors.toList());
			
			Float lastValue = sorted.get(sorted.size() - 1).getValue().getAsFloat().get();
			Float previousValue = sorted.get(sorted.size() - 2).getValue().getAsFloat().get();

			return new FloatValue(lastValue - previousValue);
		}
	}
	
}

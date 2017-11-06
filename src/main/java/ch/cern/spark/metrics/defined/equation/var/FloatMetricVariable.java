package ch.cern.spark.metrics.defined.equation.var;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.apache.kafka.common.config.ConfigException;

import ch.cern.properties.ConfigurationException;
import ch.cern.properties.Properties;
import ch.cern.spark.Pair;
import ch.cern.spark.metrics.DatedValue;
import ch.cern.spark.metrics.Metric;
import ch.cern.spark.metrics.defined.DefinedMetricStore;
import ch.cern.spark.metrics.value.FloatValue;
import ch.cern.spark.metrics.value.Value;

public class FloatMetricVariable extends MetricVariable{
	
	public enum Operation {SUM, AVG, WEIGHTED_AVG, MIN, MAX, COUNT, DIFF};
	protected Operation aggregateOperation;

	public FloatMetricVariable(String name) {
		super(name);
	}
	
	@Override
	public MetricVariable config(Properties properties) throws ConfigurationException {
		super.config(properties);
		
		String aggregateVal = properties.getProperty("aggregate");
		if(aggregateVal != null)
			try{
				aggregateOperation = Operation.valueOf(aggregateVal.toUpperCase());
			}catch(IllegalArgumentException e) {
				throw new ConfigException("Variable " + name + ": aggregation operation (" + aggregateVal + ") not available");
			}
		
		return this;
	}

	public float computeValue(DefinedMetricStore store, Instant time) throws ComputationException {
		Optional<Instant> oldestUpdate = Optional.empty();
		if(expirePeriod != null)
			oldestUpdate = Optional.of(time.minus(expirePeriod));
		store.purge(name, oldestUpdate);
		
		Double val = null;
		if(aggregateOperation == null) {
			Value valOpt = store.getValue(name);
			
			if(valOpt.getAsFloat().isPresent())
				val = valOpt.getAsFloat().get().doubleValue();
			else if(valOpt.getAsException().isPresent())
				throw new ComputationException(valOpt.getAsException().get());
			else
				throw new ComputationException("value is not of type float.");
		}else {
			switch (aggregateOperation) {
			case SUM:
				val = sumAggregation(store.getAggregatedValues(name));
				break;
			case COUNT:
				val = (double) store.getAggregatedValues(name).size();
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
				throw new ComputationException("Agreggation operation (" + aggregateOperation + ") is not available.");
			}
		}

		return val.floatValue();
	}

	private double averageAggregation(List<Value> aggregatedValues) throws ComputationException {
		DoubleStream doubleStream = toDoubleStream(aggregatedValues);
		
		OptionalDouble average = doubleStream.average();
		
		if(average.isPresent())
			return average.getAsDouble();
		else
			throw new ComputationException("Average aggregation: no float values.");
	}
	
	private Double minAggregation(List<Value> aggregatedValues) throws ComputationException {
		DoubleStream doubleStream = toDoubleStream(aggregatedValues);
		
		OptionalDouble average = doubleStream.min();
		
		if(average.isPresent())
			return average.getAsDouble();
		else
			throw new ComputationException("Minimum aggregation: no float values.");
	}
	
	private Double maxAggregation(List<Value> aggregatedValues) throws ComputationException {
		DoubleStream doubleStream = toDoubleStream(aggregatedValues);
		
		OptionalDouble average = doubleStream.max();
		
		if(average.isPresent())
			return average.getAsDouble();
		else
			throw new ComputationException("Maximum aggregation: no float values.");
	}

	private Double sumAggregation(List<Value> aggregatedValues) {
		DoubleStream doubleStream = toDoubleStream(aggregatedValues);
		
		return doubleStream.sum();
	}

	private DoubleStream toDoubleStream(List<Value> aggregatedValues) {
		return aggregatedValues.stream()
				.filter(val -> val.getAsFloat().isPresent())
				.mapToDouble(val -> val.getAsFloat().get());
	}

	private Double weightedAverageAggregation(List<DatedValue> values, Instant time) throws ComputationException {
        Optional<Pair<Double, Double>> pairSum = values.stream().filter(value -> value.getValue().getAsFloat().isPresent())
				.map(value -> {
					double weight = computeWeight(time, value.getInstant());
			    	
		    		return new Pair<Double, Double>(weight, weight * value.getValue().getAsFloat().get());
				})
				.reduce((p1, p2) -> new Pair<Double, Double>(p1.first + p2.first, p1.second + p2.second));

		if(!pairSum.isPresent())
			throw new ComputationException("Weighted average aggregation: there are no float values to compute.");
		
		double totalWeights = pairSum.get().first;
		double weightedValues = pairSum.get().second;
		
		return weightedValues / totalWeights;
	}

    private float computeWeight(Instant time, Instant metric_timestamp) {
        Duration time_difference = Duration.between(time, metric_timestamp).abs();
        
        if(expirePeriod.compareTo(time_difference) < 0)
        		return 0;
        				
        return (float) (expirePeriod.getSeconds() - time_difference.getSeconds()) / (float) expirePeriod.getSeconds();
    }
    
	private Double differenceAggregation(List<DatedValue> aggregatedDatedValues) throws ComputationException {
		if(aggregatedDatedValues.size() < 2) {
			throw new ComputationException("Difference aggregation: there is no previous value.");
		}else {
			List<DatedValue> sorted = aggregatedDatedValues.stream()
												.filter(value -> value.getValue().getAsFloat().isPresent())
												.sorted()
												.collect(Collectors.toList());
			
			double lastValue = sorted.get(sorted.size() - 1).getValue().getAsFloat().get();
			double previousValue = sorted.get(sorted.size() - 2).getValue().getAsFloat().get();

			return lastValue - previousValue;
		}
	}

	@Override
	public void updateStore(DefinedMetricStore store, Metric metric) {	
		if(!metric.getValue().getAsFloat().isPresent())
			return;
		
		if(aggregateOperation == null)
			store.updateValue(name, metric.getValue(), metric.getInstant());
		else
			store.updateAggregatedValue(name, metric.getIDs().hashCode(), metric.getValue(), metric.getInstant());
	}

	@Override
	public Class<FloatValue> returnType() {
		return FloatValue.class;
	}
	
}

package ch.cern.spark.metrics.defined;

import java.io.Serializable;
import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ch.cern.properties.ConfigurationException;
import ch.cern.properties.Properties;
import ch.cern.spark.Pair;
import ch.cern.spark.metrics.Metric;
import ch.cern.spark.metrics.defined.equation.Equation;
import ch.cern.spark.metrics.defined.equation.var.MetricVariable;
import ch.cern.spark.metrics.value.Value;

public class DefinedMetric implements Serializable{

	private static final long serialVersionUID = 82179461944060520L;

	private String name;

	private Set<String> metricsGroupBy;
	
	private Set<String> variablesWhen;
	
	private Equation equation;

	public DefinedMetric(String name) {
		this.name = name;
	}

	public DefinedMetric config(Properties properties) throws ConfigurationException {		
		String groupByVal = properties.getProperty("metrics.groupby");
		if(groupByVal != null)
			metricsGroupBy = Arrays.stream(groupByVal.split(",")).map(String::trim).collect(Collectors.toSet());
		
		Properties variablesProperties = properties.getSubset("variables");
		Set<String> variableNames = variablesProperties.getUniqueKeyFields();
		
		String equationString = properties.getProperty("value");
		try {
			if(equationString == null && variableNames.size() == 1)	
				equation = new Equation(variableNames.iterator().next(), variablesProperties);
			else if(equationString == null)
				throw new ConfigurationException("Value must be specified.");
			else
				equation = new Equation(equationString, variablesProperties);
		} catch (ParseException e) {
			throw new ConfigurationException("Problem parsing value: " + e.getMessage());
		}
		
		variablesWhen = new HashSet<String>();
		String whenValue = properties.getProperty("when");
		if(whenValue != null && whenValue.toUpperCase().equals("ANY"))
			variablesWhen.addAll(variableNames);
		else if(whenValue != null && whenValue.toUpperCase().equals("BATCH"))
			variablesWhen = null;
		else if(whenValue != null)
			variablesWhen.addAll(Arrays.stream(whenValue.split(",")).map(String::trim).collect(Collectors.toSet()));
		else
			variablesWhen.add(variableNames.stream().sorted().findFirst().get());
		
		return this;
	}

	public boolean testIfApplyForAnyVariable(Metric metric) {
		return equation.getVariables().values().stream()
				.filter(variable -> variable.test(metric))
				.count() > 0;
	}

	public Map<String, MetricVariable> getVariablesToUpdate(Metric metric) {
		return equation.getVariables().entrySet().stream()
				.filter(entry -> entry.getValue().test(metric))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	public boolean shouldBeTrigeredByUpdate(Metric metric) {
		if(isTriggerOnEveryBatch())
			return false;
		
		return equation.getVariables().entrySet().stream()
				.filter(entry -> variablesWhen.contains(entry.getKey()))
				.map(entry -> entry.getValue())
				.filter(variable -> variable.test(metric))
				.count() > 0;
	}
	
	private boolean isTriggerOnEveryBatch() {
		return variablesWhen == null;
	}

	public void updateStore(DefinedMetricStore store, Metric metric) {
		Map<String, MetricVariable> variablesToUpdate = getVariablesToUpdate(metric);
		
		for (MetricVariable variableToUpdate : variablesToUpdate.values())
			variableToUpdate.updateStore(store, metric);
	}

	public Optional<Metric> generateByUpdate(DefinedMetricStore store, Metric metric, Map<String, String> groupByMetricIDs) {
		if(!shouldBeTrigeredByUpdate(metric))
			return Optional.empty();
		
		return Optional.of(generate(store, metric.getInstant(), groupByMetricIDs));
	}
	
	public Optional<Metric> generateByBatch(DefinedMetricStore store, Instant time, Map<String, String> groupByMetricIDs) {
		if(!isTriggerOnEveryBatch())
			return Optional.empty();
		
		return Optional.of(generate(store, time, groupByMetricIDs));
	}
	
	private Metric generate(DefinedMetricStore store, Instant time, Map<String, String> groupByMetricIDs) {		
		Map<String, String> metricIDs = new HashMap<>(groupByMetricIDs);
		metricIDs.put("$defined_metric", name);
		
		Value value = equation.compute(store, time);
			
		return new Metric(time, value, metricIDs);
	}

	public Optional<Map<String, String>> getGroupByMetricIDs(Map<String, String> metricIDs) {
		if(metricsGroupBy == null)
			return Optional.of(new HashMap<>());
		
		if(metricsGroupBy.contains("ALL"))
			return Optional.of(metricIDs);
		
		Map<String, String> values = metricsGroupBy.stream()
			.map(id -> new Pair<String, String>(id, metricIDs.get(id)))
			.filter(pair -> pair.second() != null)
			.collect(Collectors.toMap(Pair::first, Pair::second));
		
		return values.size() == metricsGroupBy.size() ? Optional.of(values) : Optional.empty();
	}
	
	public String getName() {
		return name;
	}

	public Equation getEquation() {
		return equation;
	}

	protected Map<String, MetricVariable> getVariables() {
		return equation.getVariables();
	}
	
	protected Set<String> getVariablesWhen() {
		return variablesWhen;
	}

}

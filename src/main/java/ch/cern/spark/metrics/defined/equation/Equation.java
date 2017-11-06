package ch.cern.spark.metrics.defined.equation;

import java.text.ParseException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import ch.cern.properties.ConfigurationException;
import ch.cern.properties.Properties;
import ch.cern.spark.metrics.defined.DefinedMetricStore;
import ch.cern.spark.metrics.defined.equation.var.MetricVariable;
import ch.cern.spark.metrics.value.ExceptionValue;
import ch.cern.spark.metrics.value.Value;

public class Equation implements Computable<Value>{
	
	private static EquationParser parser = new EquationParser();

	private Computable<? extends Value> formula;
	
	private Map<String, MetricVariable<? extends Value>> variables = new HashMap<>();

	public Equation(String equationString, Properties variablesProperties) throws ParseException, ConfigurationException {
		this.formula = parser.parse(equationString, variablesProperties, variables);
	}
	
	@Override
	public Value compute(DefinedMetricStore store, Instant time) {
		try {
			return formula.compute(store, time);
		} catch (ComputationException e) {
			return new ExceptionValue(e);
		}
	}

	@Override
	public String toString() {
		return formula.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((formula == null) ? 0 : formula.hashCode());
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
		Equation other = (Equation) obj;
		if (formula == null) {
			if (other.formula != null)
				return false;
		} else if (!formula.equals(other.formula))
			return false;
		return true;
	}

	public Map<String, MetricVariable<?>> getVariables() {
		return variables;
	}

	@Override
	public Class<Value> returnType() {
		return Value.class;
	}

}

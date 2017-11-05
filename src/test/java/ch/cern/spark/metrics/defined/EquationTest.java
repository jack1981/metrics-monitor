package ch.cern.spark.metrics.defined;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.time.Instant;

import org.junit.Test;

import ch.cern.properties.ConfigurationException;
import ch.cern.properties.Properties;
import ch.cern.spark.metrics.defined.equation.Equation;
import ch.cern.spark.metrics.value.FloatValue;

public class EquationTest {

	@Test
	public void eval() throws ParseException, ConfigurationException {
		assertEquals(30f, new Equation("(5+10)*2", new Properties()).compute(null, null).getAsFloat().get(), 0.000f);
		assertEquals(45f, new Equation("(5+10) * (3)", new Properties()).compute(null, null).getAsFloat().get(), 0.000f);
		assertEquals(35f, new Equation("5+10 * (3)", new Properties()).compute(null, null).getAsFloat().get(), 0.000f);
	}
	
	@Test
	public void evalWithVariables() throws ParseException, ConfigurationException {
		Instant time = Instant.now();
		Properties props = new Properties();
		props.setProperty("x", "");
		props.setProperty("var1", "");
		props.setProperty("var2", "");
		DefinedMetricStore store = new DefinedMetricStore();;
	
		store.updateValue("x", new FloatValue(10), time);
		assertEquals(30f, new Equation("(5+x)*2", props).compute(store, time).getAsFloat().get(), 0.000f);
		
		store.updateValue("var1", new FloatValue(3), time);	
		assertEquals(39f, new Equation("(var1+10) * (var1)", props).compute(store, time).getAsFloat().get(), 0.000f);
		
		store.updateValue("var1", new FloatValue(5), time);
		store.updateValue("var2", new FloatValue(10), time);
		assertEquals(35f, new Equation("var1 + var2 * (3)", props).compute(store, time).getAsFloat().get(), 0.000f);
		
		store.updateValue("x", new FloatValue(5), time);
		store.updateValue("y", new FloatValue(10), time);
		try {
			new Equation("x+y * (z)", props);
			
			fail("z it is not defined, aquation cannot be parsed");
		}catch(ParseException e) {}
	}

	@Test
	public void evalWithVariablesAndFormulas() throws ParseException, ConfigurationException {
		Instant time = Instant.now();
		Properties props = new Properties();;
		props.setProperty("x", "");
		props.setProperty("y", "");
		DefinedMetricStore store = new DefinedMetricStore();
		
		store.updateValue("x", new FloatValue(9), time);
		assertEquals(9f, new Equation("abs(x)", props).compute(store, time).getAsFloat().get(), 0.01f);
		store.updateValue("x", new FloatValue(-9), time);
		assertEquals(9f, new Equation("abs(x)", props).compute(store, time).getAsFloat().get(), 0.01f);
		
		store.updateValue("x", new FloatValue(10), time);
		assertEquals(3.16f, new Equation("sqrt(x)", props).compute(store, time).getAsFloat().get(), 0.01f);
		
		store.updateValue("x", new FloatValue(10), time);
		assertEquals(0.17f, new Equation("sin(x)", props).compute(store, time).getAsFloat().get(), 0.01f);
		
		store.updateValue("x", new FloatValue(10), time);
		assertEquals(0.98f, new Equation("cos(x)", props).compute(store, time).getAsFloat().get(), 0.01f);
		
		store.updateValue("x", new FloatValue(10), time);
		assertEquals(0.17f, new Equation("tan(x)", props).compute(store, time).getAsFloat().get(), 0.01f);
		
		store.updateValue("x", new FloatValue(10), time);
		store.updateValue("y", new FloatValue(2), time);
		assertEquals(2.57f, new Equation("sin(x) + cos(x) + sqrt(y)", props).compute(store, time).getAsFloat().get(), 0.01f);
	}
	
}

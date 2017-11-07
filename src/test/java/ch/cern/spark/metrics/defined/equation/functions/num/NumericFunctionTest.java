package ch.cern.spark.metrics.defined.equation.functions.num;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.time.Instant;

import org.junit.Test;

import ch.cern.spark.metrics.defined.DefinedMetricStore;
import ch.cern.spark.metrics.defined.equation.ValueComputable;
import ch.cern.spark.metrics.value.ExceptionValue;
import ch.cern.spark.metrics.value.FloatValue;
import ch.cern.spark.metrics.value.StringValue;
import ch.cern.spark.metrics.value.Value;

public class NumericFunctionTest {
	
	@Test
	public void shouldCompute() throws ParseException {
		ValueComputable value = new ValueComputable() {
			
			@Override
			public Class<? extends Value> returnType() {
				return FloatValue.class;
			}
			
			@Override
			public Value compute(DefinedMetricStore store, Instant time) {
				return new FloatValue(10);
			}
		};
		
		NumericFunction func = new NumericFunctionImp(value);
		
		Value result = func.compute(null, null);
		
		assertTrue(result.getAsFloat().isPresent());
		assertEquals(10, result.getAsFloat().get(), 0.0f);
	}
	
	@Test
	public void shouldReturnExceptionValueIFDifferentType() throws ParseException {
		ValueComputable value = new ValueComputable() {
			
			@Override
			public Class<? extends Value> returnType() {
				return FloatValue.class;
			}
			
			@Override
			public Value compute(DefinedMetricStore store, Instant time) {
				return new StringValue("");
			}
		};
		
		NumericFunction func = new NumericFunctionImp(value);
		
		Value result = func.compute(null, null);
		
		assertTrue(result.getAsException().isPresent());
		assertEquals("received value is not float.", result.getAsException().get());
	}
	
	@Test
	public void shouldReturnSameExceptionValueIfExceptionValueIsReceived() throws ParseException {
		ValueComputable value = new ValueComputable() {
			
			@Override
			public Class<? extends Value> returnType() {
				return FloatValue.class;
			}
			
			@Override
			public Value compute(DefinedMetricStore store, Instant time) {
				return new ExceptionValue("exception message");
			}
		};
		
		NumericFunction func = new NumericFunctionImp(value);
		
		Value result = func.compute(null, null);
		
		assertTrue(result.getAsException().isPresent());
		assertEquals("exception message", result.getAsException().get());
	}
	
	private static class NumericFunctionImp extends NumericFunction{

		public NumericFunctionImp(ValueComputable v) throws ParseException {
			super(v);
		}

		@Override
		public String getFunctionRepresentation() {
			return null;
		}

		@Override
		public float compute(float value) {
			return value;
		}
		
	}
	
}

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

public class BiNumericFunctionTest {
	
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
		
		BiNumericFunctionImp func = new BiNumericFunctionImp(value, value);
		
		Value result = func.compute(null, null);
		
		assertTrue(result.getAsFloat().isPresent());
		assertEquals(20, result.getAsFloat().get(), 0.0f);
	}
	
	@Test
	public void shouldReturnExceptionValueIFDifferentType() throws ParseException {
		ValueComputable stringValue = new ValueComputable() {
			
			@Override
			public Class<? extends Value> returnType() {
				return FloatValue.class;
			}
			
			@Override
			public Value compute(DefinedMetricStore store, Instant time) {
				return new StringValue("");
			}
		};
		
		ValueComputable floatValue = new ValueComputable() {
			
			@Override
			public Class<? extends Value> returnType() {
				return FloatValue.class;
			}
			
			@Override
			public Value compute(DefinedMetricStore store, Instant time) {
				return new FloatValue(10);
			}
		};
		
		BiNumericFunctionImp func = new BiNumericFunctionImp(stringValue, floatValue);
		Value result = func.compute(null, null);
		assertTrue(result.getAsException().isPresent());
		assertEquals("first argument: received value is not float.", result.getAsException().get());
		
		func = new BiNumericFunctionImp(floatValue, stringValue);
		result = func.compute(null, null);
		assertTrue(result.getAsException().isPresent());
		assertEquals("second argument: received value is not float.", result.getAsException().get());
		
		func = new BiNumericFunctionImp(stringValue, stringValue);
		result = func.compute(null, null);
		assertTrue(result.getAsException().isPresent());
		assertEquals("Several exceptions: ("
						+ "first argument: received value is not float. "
						+ "second argument: received value is not float. )", result.getAsException().get());
	}
	
	@Test
	public void shouldReturnExceptionValueIfExceptionValuesAreReceived() throws ParseException {
		ValueComputable exceptionValue = new ValueComputable() {
			
			@Override
			public Class<? extends Value> returnType() {
				return FloatValue.class;
			}
			
			@Override
			public Value compute(DefinedMetricStore store, Instant time) {
				return new ExceptionValue("exception message");
			}
		};
		
		ValueComputable floatValue = new ValueComputable() {
			
			@Override
			public Class<? extends Value> returnType() {
				return FloatValue.class;
			}
			
			@Override
			public Value compute(DefinedMetricStore store, Instant time) {
				return new FloatValue(10);
			}
		};
		
		BiNumericFunctionImp func = new BiNumericFunctionImp(exceptionValue, floatValue);
		Value result = func.compute(null, null);
		assertTrue(result.getAsException().isPresent());
		assertEquals("first argument: exception message.", result.getAsException().get());
		
		func = new BiNumericFunctionImp(floatValue, exceptionValue);
		result = func.compute(null, null);
		assertTrue(result.getAsException().isPresent());
		assertEquals("second argument: exception message.", result.getAsException().get());
		
		func = new BiNumericFunctionImp(exceptionValue, exceptionValue);
		result = func.compute(null, null);
		assertTrue(result.getAsException().isPresent());
		assertEquals("Several exceptions: ("
						+ "first argument: exception message. "
						+ "second argument: exception message. )", result.getAsException().get());
	}
	
	@Test
	public void shouldMixExceptions() throws ParseException {
		ValueComputable exceptionValue = new ValueComputable() {
			
			@Override
			public Class<? extends Value> returnType() {
				return FloatValue.class;
			}
			
			@Override
			public Value compute(DefinedMetricStore store, Instant time) {
				return new ExceptionValue("exception message");
			}
		};
		
		ValueComputable stringValue = new ValueComputable() {
			
			@Override
			public Class<? extends Value> returnType() {
				return FloatValue.class;
			}
			
			@Override
			public Value compute(DefinedMetricStore store, Instant time) {
				return new StringValue("");
			}
		};
		
		BiNumericFunctionImp func = new BiNumericFunctionImp(exceptionValue, stringValue);
		Value result = func.compute(null, null);
		assertTrue(result.getAsException().isPresent());
		assertEquals("Several exceptions: ("
						+ "first argument: exception message. "
						+ "second argument: received value is not float. )", result.getAsException().get());
	}
	
	private static class BiNumericFunctionImp extends BiNumericFunction{

		public BiNumericFunctionImp(ValueComputable v1, ValueComputable v2) throws ParseException {
			super(v1, v2);
		}

		@Override
		public String getFunctionRepresentation() {
			return null;
		}

		@Override
		public float compute(float value1, float value2) {
			return value1 + value2;
		}
		
	}
	
}

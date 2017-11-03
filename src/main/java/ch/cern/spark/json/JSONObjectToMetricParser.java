package ch.cern.spark.json;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.function.FlatMapFunction;

import ch.cern.properties.ConfigurationException;
import ch.cern.properties.Properties;
import ch.cern.spark.Pair;
import ch.cern.spark.metrics.Metric;
import ch.cern.spark.metrics.value.ExceptionValue;
import ch.cern.spark.metrics.value.FloatValue;
import ch.cern.spark.metrics.value.Value;

public class JSONObjectToMetricParser implements FlatMapFunction<JSONObject, Metric>{

    private static final long serialVersionUID = -5490112720236337434L;
    
    public static String ATTRIBUTES_PARAM = "attributes";
    private List<Pair<String, String>> attributes;
    
    public static String VALUE_ATTRIBUTES_PARAM = "value.attributes";
    private List<Pair<String, String>> value_attributes;
    
    public static String TIMESTAMP_FORMAT_PARAM = "timestamp.format";
    public static String TIMESTAMP_FORMAT_DEFAULT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private String timestamp_format_pattern;
    private transient DateTimeFormatter timestamp_format;

    public static String TIMESTAMP_ATTRIBUTE_PARAM = "timestamp.attribute";
    private String timestamp_attribute;
    
    public JSONObjectToMetricParser(Properties properties) throws ConfigurationException {
    		timestamp_attribute = properties.getProperty(TIMESTAMP_ATTRIBUTE_PARAM);    
    		if(timestamp_attribute == null)
    			throw new ConfigurationException(TIMESTAMP_ATTRIBUTE_PARAM + " must be configured.");
    		
    		timestamp_format_pattern = properties.getProperty(TIMESTAMP_FORMAT_PARAM, TIMESTAMP_FORMAT_DEFAULT);
    		if(timestamp_format_pattern != null && !timestamp_format_pattern.equals("epoch-ms") && !timestamp_format_pattern.equals("epoch-s"))
	    		try {
	    			new DateTimeFormatterBuilder()
					.appendPattern(timestamp_format_pattern)
					.toFormatter()
					.withZone(ZoneOffset.systemDefault());
	    		}catch(Exception e) {
	    			throw new ConfigurationException(TIMESTAMP_FORMAT_PARAM + " must be epoch-ms, epoch-s or a pattern compatible with DateTimeFormatterBuilder.");
	    		}
    		
    		value_attributes = new LinkedList<>();
        String value_attributes_value = properties.getProperty(VALUE_ATTRIBUTES_PARAM);
		if(value_attributes_value != null) {
			String[] attributesValues = value_attributes_value.split("\\s");
			
			for (String attribute : attributesValues) 
				value_attributes.add(new Pair<String, String>(attribute, attribute));
		}
		Properties valueAttributesWithAlias = properties.getSubset(VALUE_ATTRIBUTES_PARAM);
		for (Map.Entry<Object, Object> pair : valueAttributesWithAlias.entrySet()) {
			String alias = (String) pair.getKey();
			String key = (String) pair.getValue();
			
			value_attributes.add(new Pair<String, String>(alias, key));
		}
        if(value_attributes.isEmpty())
			throw new ConfigurationException(VALUE_ATTRIBUTES_PARAM + " must be configured.");
        
        attributes = new LinkedList<>();
        String attributesValue = properties.getProperty(ATTRIBUTES_PARAM);
		if(attributesValue != null) {
			String[] attributesValues = attributesValue.split("\\s");
			
			for (String attribute : attributesValues) 
				attributes.add(new Pair<String, String>(attribute, attribute));
		}
		Properties attributesWithAlias = properties.getSubset(ATTRIBUTES_PARAM);
		for (Map.Entry<Object, Object> pair : attributesWithAlias.entrySet()) {
			String alias = (String) pair.getKey();
			String key = (String) pair.getValue();
			
			attributes.add(new Pair<String, String>(alias, key));
		}
    }

    @Override
    public Iterator<Metric> call(JSONObject jsonObject) {
		Map<String, String> ids = new HashMap<>();
		for(Pair<String, String> attribute : attributes) {
			String alias = attribute.first;
			String key = attribute.second;
			
			String value = jsonObject.getProperty(key);
			
			if(value != null)
				ids.put(alias, value);
		}
		
		Exception timestampException = null;
    		String timestamp_string = jsonObject.getProperty(timestamp_attribute);
		Instant timestamp;
		try {
			timestamp = toDate(timestamp_string);
		} catch (DateTimeParseException e) {
			timestampException = new Exception("DateTimeParseException: " + e.getMessage() 
						+ " for key " + timestamp_attribute 
						+ " with value (" + timestamp_string + ")");
			
			timestamp = Instant.now();
		}
		
		List<Metric> metrics = new LinkedList<>();
		for (Pair<String, String> value_attribute : value_attributes) {
			String alias = value_attribute.first;
			String key = value_attribute.second;
			
			Map<String, String> metric_ids = new HashMap<>(ids);
	        metric_ids.put("$value_attribute", alias);
			
			if(timestampException != null) {
				metrics.add(new Metric(timestamp, new ExceptionValue(timestampException), metric_ids));
				continue;
			}
			
			String value_string = jsonObject.getProperty(key);
			if(value_string == null) {
				Exception exception = new Exception("No metric was generated for value key \"" + key + "\", "
																		+ "document does not contian such key.");
				
				metrics.add(new Metric(timestamp, new ExceptionValue(exception), metric_ids));
				continue;
			}
			
	        Value value = FloatValue.from(value_string);
	        
	        metrics.add(new Metric(timestamp, value, metric_ids));
		}
        
        return metrics.iterator();
    }

    private Instant toDate(String date_string) throws DateTimeParseException {
    		if(date_string == null || date_string.length() == 0)
    			throw new DateTimeParseException("No data to parse", "", 0);
    	
    		try {
	    		if(timestamp_format_pattern.equals("epoch-ms"))
	    			return Instant.ofEpochMilli(Long.valueOf(date_string));
	    		
	    		if(timestamp_format_pattern.equals("epoch-s"))
	    			return Instant.ofEpochSecond(Long.valueOf(date_string));
    		}catch(Exception e) {
    			throw new DateTimeParseException(e.getClass().getName() + ": " + e.getMessage(), date_string, 0);
    		}

		if(timestamp_format == null)
    			timestamp_format = new DateTimeFormatterBuilder()
							.appendPattern(timestamp_format_pattern)
							.toFormatter()
							.withZone(ZoneOffset.systemDefault());
			
		TemporalAccessor temporalAccesor = timestamp_format.parse(date_string);
		
		if(temporalAccesor.isSupported(ChronoField.INSTANT_SECONDS)) {
			return Instant.from(temporalAccesor);
		}else{
			return LocalDate.from(temporalAccesor).atStartOfDay(ZoneOffset.systemDefault()).toInstant();
		}
    }

}

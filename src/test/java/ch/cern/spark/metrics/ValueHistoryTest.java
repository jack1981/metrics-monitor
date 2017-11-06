package ch.cern.spark.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import ch.cern.spark.metrics.ValueHistory.Store_;
import ch.cern.spark.metrics.value.BooleanValue;
import ch.cern.spark.metrics.value.ExceptionValue;
import ch.cern.spark.metrics.value.FloatValue;
import ch.cern.spark.metrics.value.StringValue;
import ch.cern.spark.metrics.value.Value;
import ch.cern.utils.TimeUtils;

public class ValueHistoryTest {
    
    @Test
    public void floatValueSerializationSize() throws IOException, ParseException{
        ValueHistory.Store_ store = new Store_();
        store.history = new ValueHistory(Duration.ofSeconds(60));
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);   
        out.writeObject(store);
        out.flush();
        byte[] bytesBefore = bos.toByteArray();
        
        int numberOfRecords = 10;
        for (int i = 0; i < numberOfRecords; i++) 
            store.history.add(Instant.ofEpochSecond(Instant.now().getEpochSecond()), new FloatValue(Math.random()));
        
        bos = new ByteArrayOutputStream();
        out = new ObjectOutputStream(bos);   
        out.writeObject(store);
        out.flush();
        byte[] bytesAfter = bos.toByteArray();
        
        int sizePerRecord = (bytesAfter.length - bytesBefore.length) / numberOfRecords;
        assertEquals(25, sizePerRecord);
    }
    
    @Test
    public void stringValueSerializationSize() throws IOException, ParseException{
        ValueHistory.Store_ store = new Store_();
        store.history = new ValueHistory(Duration.ofSeconds(60));
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);   
        out.writeObject(store);
        out.flush();
        byte[] bytesBefore = bos.toByteArray();
        
        int numberOfRecords = 10;
        for (int i = 0; i < numberOfRecords; i++) 
            store.history.add(Instant.ofEpochSecond(Instant.now().getEpochSecond()), new StringValue("something"));
        
        bos = new ByteArrayOutputStream();
        out = new ObjectOutputStream(bos);   
        out.writeObject(store);
        out.flush();
        byte[] bytesAfter = bos.toByteArray();
        
        int sizePerRecord = (bytesAfter.length - bytesBefore.length) / numberOfRecords;
        assertEquals(29, sizePerRecord);
    }
    
    @Test
    public void booleanValueSerializationSize() throws IOException, ParseException{
        ValueHistory.Store_ store = new Store_();
        store.history = new ValueHistory(Duration.ofSeconds(60));
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);   
        out.writeObject(store);
        out.flush();
        byte[] bytesBefore = bos.toByteArray();
        
        int numberOfRecords = 10;
        for (int i = 0; i < numberOfRecords; i++) 
            store.history.add(Instant.ofEpochSecond(Instant.now().getEpochSecond()), new BooleanValue(true));
        
        bos = new ByteArrayOutputStream();
        out = new ObjectOutputStream(bos);   
        out.writeObject(store);
        out.flush();
        byte[] bytesAfter = bos.toByteArray();
        
        int sizePerRecord = (bytesAfter.length - bytesBefore.length) / numberOfRecords;
        assertEquals(22, sizePerRecord);
    }
    
    @Test
    public void exceptionValueSerializationSize() throws IOException, ParseException{
        ValueHistory.Store_ store = new Store_();
        store.history = new ValueHistory(Duration.ofSeconds(60));
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);   
        out.writeObject(store);
        out.flush();
        byte[] bytesBefore = bos.toByteArray();
        
        int numberOfRecords = 10;
        for (int i = 0; i < numberOfRecords; i++) 
            store.history.add(Instant.ofEpochSecond(Instant.now().getEpochSecond()), new ExceptionValue("message"));
        
        bos = new ByteArrayOutputStream();
        out = new ObjectOutputStream(bos);   
        out.writeObject(store);
        out.flush();
        byte[] bytesAfter = bos.toByteArray();
        
        int sizePerRecord = (bytesAfter.length - bytesBefore.length) / numberOfRecords;
        assertEquals(28, sizePerRecord);
    }
    
    @Test
    public void saveAndLoad() throws IOException, ParseException, ClassNotFoundException{
        Store_ store = new Store_();
        store.history = new ValueHistory(Duration.ofSeconds(60));
        int numberOfRecords = 10;
        for (int i = 0; i < numberOfRecords; i++) 
            store.history.add(Instant.ofEpochSecond(Instant.now().getEpochSecond()), (float) Math.random());
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);   
        out.writeObject(store);
        out.flush();
        byte[] bytes = bos.toByteArray();
        out.close();
        
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Store_ restoredStore = (ValueHistory.Store_) ois.readObject();
        ois.close();
        
        assertNotSame(store.history.getDatedValues(), restoredStore.history.getDatedValues());
        assertEquals(store.history.getPeriod(), restoredStore.history.getPeriod());
        assertEquals(store.history.getDatedValues(), restoredStore.history.getDatedValues());
    }
    
    @Test
    public void expiration() throws Exception{
        ValueHistory history = new ValueHistory(Duration.ofSeconds(60));
        
        history.add(TimeUtils.toInstant("2017-04-01 11:18:12"), 9f);
        history.add(TimeUtils.toInstant("2017-04-01 11:18:56"), 10f);
        history.add(TimeUtils.toInstant("2017-04-01 11:19:12"), 11f);
        history.add(TimeUtils.toInstant("2017-04-01 11:19:31"), 12f);
        history.add(TimeUtils.toInstant("2017-04-01 11:20:01"), 13f);
        history.add(TimeUtils.toInstant("2017-04-01 11:20:10"), 14f);
        
        history.purge(TimeUtils.toInstant("2017-04-01 11:20:22"));
        
        List<Value> returnedValues = history.getDatedValues().stream().map(value -> value.getValue()).collect(Collectors.toList());
        
        List<FloatValue> expected = Arrays.asList(new FloatValue(12f), new FloatValue(13f), new FloatValue(14f));
        
        Assert.assertEquals(expected, returnedValues);
    }

    @Test
    public void getHourlyValues() throws Exception{
        ValueHistory history = new ValueHistory(Duration.ofSeconds(50));
        
        history.add(TimeUtils.toInstant("2017-04-01 09:20:12"), 9f);
        history.add(TimeUtils.toInstant("2017-04-01 10:20:56"), 10f);
        history.add(TimeUtils.toInstant("2017-04-01 10:21:34"), 11f);
        history.add(TimeUtils.toInstant("2017-04-01 10:22:31"), 12f);
        history.add(TimeUtils.toInstant("2017-04-01 11:20:01"), 13f);
        history.add(TimeUtils.toInstant("2017-04-01 11:20:10"), 14f);
        
        List<Value> returnedValues = history.getHourlyValues(TimeUtils.toInstant("2017-04-01 10:20:02"));
        
        List<FloatValue> expected = Arrays.asList(new FloatValue(9f), new FloatValue(10f), new FloatValue(13f), new FloatValue(14f));
        
        Assert.assertEquals(expected, returnedValues);
    }
    
    @Test
    public void getDaylyValues() throws Exception{
        ValueHistory history = new ValueHistory(Duration.ofSeconds(50));
        
        history.add(TimeUtils.toInstant("2016-03-07 10:20:12"), 9f);
        history.add(TimeUtils.toInstant("2017-04-07 10:20:56"), 10f);
        history.add(TimeUtils.toInstant("2017-04-08 10:21:34"), 11f);
        history.add(TimeUtils.toInstant("2017-04-09 10:22:31"), 12f);
        history.add(TimeUtils.toInstant("2017-04-09 10:20:01"), 13f);
        history.add(TimeUtils.toInstant("2017-04-10 11:20:10"), 14f);
        
        List<Value> returnedValues = history.getDaylyValues(TimeUtils.toInstant("2017-04-01 10:20:02"));
        
        List<FloatValue> expected = Arrays.asList(new FloatValue(9f), new FloatValue(10f), new FloatValue(13f));
        
        Assert.assertEquals(expected, returnedValues);
    }
    
    @Test
    public void getWeeklyValues() throws Exception{
        ValueHistory history = new ValueHistory(Duration.ofSeconds(50));
        
        history.add(TimeUtils.toInstant("2016-03-05 10:20:12"), 9f);
        history.add(TimeUtils.toInstant("2017-04-03 10:20:56"), 10f);
        history.add(TimeUtils.toInstant("2017-04-03 10:21:34"), 11f);
        history.add(TimeUtils.toInstant("2017-04-10 10:22:31"), 12f);
        history.add(TimeUtils.toInstant("2017-04-10 10:20:01"), 13f);
        history.add(TimeUtils.toInstant("2017-04-17 11:20:10"), 14f);
        
        List<Value> returnedValues = history.getWeeklyValues(TimeUtils.toInstant("2017-04-17 10:20:02"));
        
        List<FloatValue> expected = Arrays.asList(new FloatValue(10f), new FloatValue(13f));
        
        Assert.assertEquals(expected, returnedValues);
    }
    
}

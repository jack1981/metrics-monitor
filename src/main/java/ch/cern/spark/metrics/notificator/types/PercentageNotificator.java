package ch.cern.spark.metrics.notificator.types;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.cern.components.RegisterComponent;
import ch.cern.properties.ConfigurationException;
import ch.cern.properties.Properties;
import ch.cern.spark.Pair;
import ch.cern.spark.metrics.notifications.Notification;
import ch.cern.spark.metrics.notificator.Notificator;
import ch.cern.spark.metrics.results.AnalysisResult.Status;
import ch.cern.spark.metrics.store.HasStore;
import ch.cern.spark.metrics.store.Store;
import ch.cern.utils.TimeUtils;

@RegisterComponent("percentage")
public class PercentageNotificator extends Notificator implements HasStore {
    
    private static final long serialVersionUID = -7890231998987060652L;
    
    private String STATUSES_PARAM = "statuses";
    private Set<Status> expectedStatuses;
    
    private static String PERIOD_PARAM = "period";
    private static Duration PERIOD_DEFAULT = Duration.ofMinutes(15);
    private Duration period = PERIOD_DEFAULT;
    
    private String PERCENTAGE_PARAM = "percentage";
    private String PERCENTAGE_DEFAULT = "90";
    private float percentage;
    
    private List<Pair<Instant, Boolean>> hits;
    
    public PercentageNotificator() {
        hits = new LinkedList<>();
    }

    @Override
    public void config(Properties properties) throws ConfigurationException {
        super.config(properties);
        
        expectedStatuses = Stream.of(properties.getProperty(STATUSES_PARAM).split(","))
									        		.map(String::trim)
									        		.map(String::toUpperCase)
									        		.map(Status::valueOf)
									        		.collect(Collectors.toSet());
        
        period = properties.getPeriod(PERIOD_PARAM, PERIOD_DEFAULT);
        
        String percentage_s = properties.getProperty(PERCENTAGE_PARAM, PERCENTAGE_DEFAULT);
        percentage = Float.valueOf(percentage_s);
    }
    
    @Override
    public void load(Store store) {
        if(store == null || !(store instanceof Store_))
            return;
        
        Store_ data = (Store_) store;
        
        hits = data.hits;
    }

    @Override
    public Store save() {
        Store_ store = new Store_();
        
        store.hits = hits;
        
        return store;
    }

    @Override
    public Optional<Notification> process(Status status, Instant timestamp) {
        removeExpiredHits(timestamp);
        
        hits.add(new Pair<Instant, Boolean>(timestamp, isExpectedStatus(status)));
        
        Duration coveredPeriod = getCoveredPeriod(timestamp);
        if(coveredPeriod.compareTo(period) < 0)
            return Optional.empty();
        
        if(raise(timestamp)){
            Notification notification = new Notification();
            notification.setReason("Metric has been " + percentage + "% of the last "
                    + TimeUtils.toString(period) + " in state " + expectedStatuses + ".");
            
            hits = new LinkedList<>();
            
            return Optional.of(notification);
        }else{
            return Optional.empty();
        }
    }

    private Duration getCoveredPeriod(Instant timestamp) {
    		Instant oldestTimestamp = getOldestTimestamp();
    		
        if(oldestTimestamp == null)
            return Duration.ZERO;
        
        return Duration.between(oldestTimestamp, timestamp).abs();
    }

    private Instant getOldestTimestamp() {
    		Instant oldestTimestamp = null;
        
        for (Pair<Instant, Boolean> hit : hits)
            if(oldestTimestamp == null || oldestTimestamp.compareTo(hit.first) > 0)
                oldestTimestamp = hit.first;
        
        return oldestTimestamp;
    }

    private void removeExpiredHits(Instant timestamp) {
        // It needs to store a longer period than the configured period
        // Because percentage is calculated for, as minimum, the period
        Duration extendedPeriod = Duration.ofSeconds((long) (period.getSeconds() * 1.2));
        
        Instant expirationTime = timestamp.minus(extendedPeriod);
        
        Iterator<Pair<Instant, Boolean>> it = hits.iterator();
        while(it.hasNext())
            if(it.next().first.isBefore(expirationTime))
                it.remove();
    }

    private boolean isExpectedStatus(Status status) {
        return expectedStatuses.contains(status);
    }

    private boolean raise(Instant currentTime) {
        float percentageOfHits = computePercentageOfHits(currentTime);
        
        return percentageOfHits > (percentage / 100f);
    }

    private float computePercentageOfHits(Instant currentTime) {
        float counter_true = 0;
        float counter = 0;
        
        for (Pair<Instant, Boolean> pair : hits)
            if(isInPeriod(currentTime, pair.first)){
                counter++;
                
                if(pair.second)
                    counter_true++;
            }
                
        
        return counter_true / counter;
    }

    private boolean isInPeriod(Instant currentTime, Instant hitTime) {
    		Instant oldestTime = currentTime.minus(period);
        
        return hitTime.isAfter(oldestTime) && hitTime.isBefore(currentTime)
        			|| hitTime.equals(oldestTime) || hitTime.equals(currentTime);
    }

    public static class Store_ implements Store{
        private static final long serialVersionUID = -1907347033980904180L;
        
        List<Pair<Instant, Boolean>> hits;
    }

}

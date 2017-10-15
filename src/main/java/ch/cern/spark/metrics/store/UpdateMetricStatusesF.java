package ch.cern.spark.metrics.store;

import java.io.IOException;
import java.time.Instant;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function4;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.StateSpec;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;

import ch.cern.spark.metrics.ComputeIDsForMetricsF;
import ch.cern.spark.metrics.Metric;
import ch.cern.spark.metrics.MetricStatusesS;
import ch.cern.spark.metrics.MonitorIDMetricIDs;
import ch.cern.spark.metrics.monitors.Monitor;
import ch.cern.spark.metrics.monitors.Monitors;
import ch.cern.spark.metrics.results.AnalysisResult;
import scala.Tuple2;

public class UpdateMetricStatusesF
        implements Function4<Time, MonitorIDMetricIDs, Optional<Metric>, State<MetricStore>, Optional<AnalysisResult>> {

    private static final long serialVersionUID = 3156649511706333348L;
    
    public static String DATA_EXPIRATION_PARAM = "data.expiration";
    public static java.time.Duration DATA_EXPIRATION_DEFAULT = java.time.Duration.ofHours(3);

    private Monitors monitorsCache;
    
    public UpdateMetricStatusesF(Monitors monitorsCache) {
        this.monitorsCache = monitorsCache;
    }

    @Override
    public Optional<AnalysisResult> call(
            Time time, MonitorIDMetricIDs ids, Optional<Metric> metricOpt, State<MetricStore> storeState) 
            throws Exception {
        
        Monitor monitor = monitorsCache.get(ids.getMonitorID());
        
        if(storeState.isTimingOut())
            return Optional.of(AnalysisResult.buildTimingOut(ids, monitor, Instant.ofEpochMilli(time.milliseconds())));
        
        if(!metricOpt.isPresent())
            return Optional.absent();
        
        MetricStore store = getMetricStore(storeState);
        Metric metric = metricOpt.get();
        
        store.updateLastestTimestamp(metric.getInstant());
        
        AnalysisResult result = monitor.process(store, metric);
        result.setAnalyzedMetric(metric);
        
        storeState.update(store);
        
        return Optional.of(result);
    }
    
    private MetricStore getMetricStore(State<MetricStore> storeState) {
        return storeState.exists() ? storeState.get() : new MetricStore();
    }

    public static MetricStatusesS apply(
    			JavaDStream<Metric> metrics,
            Monitors monitorsCache, 
            JavaRDD<Tuple2<MonitorIDMetricIDs, MetricStore>> initialMetricStores, 
            java.time.Duration dataExpirationPeriod) throws IOException {
    	
    		JavaPairDStream<MonitorIDMetricIDs, Metric> metricsWithID = metrics.flatMapToPair(new ComputeIDsForMetricsF(monitorsCache));
        
        StateSpec<MonitorIDMetricIDs, Metric, MetricStore, AnalysisResult> statusSpec = StateSpec
                .function(new UpdateMetricStatusesF(monitorsCache))
                .initialState(initialMetricStores.rdd())
                .timeout(new Duration(dataExpirationPeriod.toMillis()));
        
        MetricStatusesS statuses = new MetricStatusesS(metricsWithID.mapWithState(statusSpec));
        
        return statuses;
    }

}

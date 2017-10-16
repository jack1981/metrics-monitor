package ch.cern.spark;

import java.io.IOException;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.Function4;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaDStream;

import ch.cern.spark.json.JSONObject;
import ch.cern.spark.json.JSONParser;
import ch.cern.spark.metrics.Sink;

public class Stream<V> {

	private JavaDStream<V> stream;
	
	protected Stream(JavaDStream<V> stream) {
		this.stream = stream;
	}
	
	public static<V> Stream<V> from(JavaDStream<V> input){
		return new Stream<>(input);
	}
	
	public<K> PairStream<K, V> toPair(PairFlatMapFunction<V, K, V> function) {
		return PairStream.from(stream.flatMapToPair(function));
	}

	public<K, S, R> StatusStream<K, V, S, R> mapWithState(
			String id,
			PairFlatMapFunction<V, K, V> toPairFunction, 
			Function4<Time, K, Optional<V>, State<S>, Optional<R>> updateStatusFunction) throws ClassNotFoundException, IOException {

		PairStream<K, V> keyValuePairs = toPair(toPairFunction);
		
		return keyValuePairs.mapWithState(id, updateStatusFunction);
	}

	public Stream<V> union(Stream<V> input) {
		return Stream.from(stream.union(input.stream));
	}
	
	public<R> Stream<R> map(Function<V, R> mapFunction) {
		return Stream.from(stream.map(mapFunction));
	}
	
	public void foreachRDD(VoidFunction<JavaRDD<V>> function) {
		stream.foreachRDD(function);
	}
	
	public<R> Stream<R> transform(Function2<JavaRDD<V>, Time, JavaRDD<R>> transformFunc) {
		return Stream.from(stream.transform(transformFunc));
	}

	public Stream<JSONObject> asJSON() {
		return map(JSONParser::parse);
	}

	public Stream<String> asString() {
		return map(Object::toString);
	}

	public JavaDStream<V> asJavaDStream() {
		return stream;
	}

	public <R> Stream<R> mapS(Function<Stream<V>, Stream<R>> mapStreamFucntion) throws Exception {
		return mapStreamFucntion.call(this);
	}
	
	public void sink(Sink<V> sink) {
		sink.sink(this);
	}
	
	public void save(String id) {
		foreachRDD(rdd -> RDDHelper.save(rdd, id));
	}
	
	public JavaSparkContext getSparkContext() {
		return JavaSparkContext.fromSparkContext(stream.context().sparkContext());
	}
	
}

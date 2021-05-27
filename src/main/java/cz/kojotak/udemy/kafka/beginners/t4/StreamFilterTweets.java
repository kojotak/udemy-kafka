package cz.kojotak.udemy.kafka.beginners.t4;

import static org.apache.kafka.streams.StreamsConfig.*;

import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

import com.google.gson.JsonParser;

import cz.kojotak.udemy.kafka.beginners.Config;

public class StreamFilterTweets {

	public static void main(String[] args) {
		Properties properties = new Properties();
		properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, Config.BOOTSTRAP_SERVERS);
		properties.setProperty(APPLICATION_ID_CONFIG, "demo-kafka-streams");
		properties.setProperty(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
		properties.setProperty(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());

		StreamsBuilder streamsBuilder = new StreamsBuilder();
		KStream<String, String> inputTopic = streamsBuilder.stream("twitter_tweets");
		KStream<String, String> filteredStream = inputTopic.filter(
				(k,json)-> extractUserFollowers(json) > 10000  );
		filteredStream.to("important_tweets");
		
		KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);
	}
	
	private static Integer extractUserFollowers(String json) {
		try {
			return new JsonParser()
					.parse(json)
					.getAsJsonObject()
					.get("user")
					.getAsJsonObject()
					.get("followers_count")
					.getAsInt();
		}catch(Exception e) {
			return 0;
		}
	}

}

package cz.kojotak.udemy.kafka.beginners.t2;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import static cz.kojotak.udemy.kafka.beginners.Config.*;
public class TwitterProducer implements Runnable {

	Logger logger = LoggerFactory.getLogger(getClass());
	List<String> terms = Lists.newArrayList("kafka", "java", "bitcoin");
	public TwitterProducer() {
	}

	//before starting...
	//kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --topic twitter_tweets --partitions 6 --replication-factor 1
	//kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic twitter_tweets
	public static void main(String[] args) {
		new TwitterProducer().run();
	}

	@Override
	public void run() {
		BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);

		Client client = createTwitterProducer(msgQueue);
		client.connect();
		
		KafkaProducer<String,String> producer = createKafkaProducer();
		
		Runtime.getRuntime().addShutdownHook( new Thread(()->{
			logger.info("stopping application...");
			client.stop();
			producer.close();
			logger.info("...done");
		}));
		
		// on a different thread, or multiple different threads....
		while (!client.isDone()) {
		  try {
			String msg = msgQueue.take();
			logger.info("msg: " + msg);
			producer.send(new ProducerRecord<>("twitter_tweets", null, msg), new Callback() {

				@Override
				public void onCompletion(RecordMetadata metadata, Exception exception) {
					if(exception!=null) {
						logger.error("bad day", exception);
					}
				}
				
			});
		  } catch (InterruptedException e) {
			  logger.error("error happened ", e);
			  client.stop();
		  }
		}
	}

	private KafkaProducer<String, String> createKafkaProducer() {
		Properties  properties = new Properties();
		properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		properties.setProperty(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		//chapter 63 safe producer
		properties.setProperty(ENABLE_IDEMPOTENCE_CONFIG, "true");
		properties.setProperty(ACKS_CONFIG, "all");
		properties.setProperty(RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
		properties.setProperty(MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");
		
		//chapter 66 high throughput
		properties.setProperty(COMPRESSION_TYPE_CONFIG, "snappy");
		properties.setProperty(LINGER_MS_CONFIG, "20");
		properties.setProperty(BATCH_SIZE_CONFIG, Integer.toString(32*1024));
		
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
		return producer;
	}

	// from https://github.com/twitter/hbc
	public Client createTwitterProducer(BlockingQueue<String> msgQueue ) {

		/**
		 * Declare the host you want to connect to, the endpoint, and authentication
		 * (basic auth or oauth)
		 */
		Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
		StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
		// Optional: set up some followings and track terms
		List<Long> followings = Lists.newArrayList(1234L, 566788L);
		hosebirdEndpoint.followings(followings);
		hosebirdEndpoint.trackTerms(terms);

		// These secrets should be read from a config file
		Authentication hosebirdAuth = new OAuth1(TWITTER_API_KEY, TWITTER_API_SECRET, TWITTER_TOKEN, TWITTER_SECRET);

		ClientBuilder builder = new ClientBuilder()
				.name("Hosebird-Client-01") 
				.hosts(hosebirdHosts)
				.authentication(hosebirdAuth)
				.endpoint(hosebirdEndpoint)
				.processor(new StringDelimitedProcessor(msgQueue)); 

		return builder.build();
	}
}

package cz.kojotak.udemy.kafka.beginners.t3;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParser;

import cz.kojotak.udemy.kafka.beginners.Config;
import cz.kojotak.udemy.kafka.beginners.t1.ProducerDemo;

import static cz.kojotak.udemy.kafka.beginners.Config.*;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
public class ElasticSearchConsumer {

	public static void main(String[] args) throws IOException, InterruptedException {
		Logger logger = LoggerFactory.getLogger(ElasticSearchConsumer.class);
		try(RestHighLevelClient client = createClient()){
			
			KafkaConsumer<String, String> consumer = createConsumer("twitter_tweets");
			while(true) {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
				
				logger.info("received " + records.count() + " records");
				
				for(ConsumerRecord<String, String> record : records) {
					//1st strategy to create an ID to achieve idempotency
					//String id = record.topic() + record.partition() + record.offset();
					
					//2nd strategy - rely on twitter's tweet id
					String id = extractId( record.value() );
					
					String json = record.value();
					IndexRequest indexRequest = new IndexRequest("twitter","tweets",id)
							.source(json, XContentType.JSON);
				
					IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
					
					logger.info("id from response: " + indexResponse.getId());
					//use Bonsai's console to check the result: /twitter/tweets/x3i-onkBtJl9PCZyNywo (use the logger id instead)
					Thread.sleep(1000);
				}
				
				logger.info("commiting offset...");
				consumer.commitSync();
				logger.info("commiting offset... done");
				Thread.sleep(1000);
			}
		}
	}
	
	private static String extractId(String json) {
		return new JsonParser()
				.parse(json)
				.getAsJsonObject()
				.get("id_str")
				.getAsString();
	}

	private static KafkaConsumer<String,String> createConsumer(String topic){
		Properties properties = new Properties();
		properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, Config.BOOTSTRAP_SERVERS);
		properties.setProperty(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(GROUP_ID_CONFIG, "kafka-demo-elasticsearch");
		properties.setProperty(AUTO_OFFSET_RESET_CONFIG, "earliest"); //other options: latest and none
		
		//chapter 79 - manual commit of offsets
		properties.setProperty(ENABLE_AUTO_COMMIT_CONFIG, "false"); //disable default auto commit
		properties.setProperty(MAX_POLL_RECORDS_CONFIG, "10"); 
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String,String>(properties);
		consumer.subscribe(Collections.singleton(topic));
		return consumer;
	}
	
	private static RestHighLevelClient createClient() {
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(BONSAI_USERNAME, BONSAI_PASSWORD));
		
		RestClientBuilder builder = RestClient
				.builder(new HttpHost(BONSAI_HOSTNAME, 443, "https"))
				.setHttpClientConfigCallback(b->b.setDefaultCredentialsProvider(credentialsProvider));
		
		return new RestHighLevelClient(builder);
	}

}

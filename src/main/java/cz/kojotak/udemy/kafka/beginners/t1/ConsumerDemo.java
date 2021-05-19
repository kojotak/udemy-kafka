package cz.kojotak.udemy.kafka.beginners.t1;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
public class ConsumerDemo {
	
	private static final String GROUP_ID = "my-fourth-application";

	public static void main(String[] args) {
		Logger logger = LoggerFactory.getLogger(ConsumerDemo.class);
		
		Properties properties = new Properties();
		properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, ProducerDemo.BOOTSTRAP_SERVERS);
		properties.setProperty(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(GROUP_ID_CONFIG, GROUP_ID);
		properties.setProperty(AUTO_OFFSET_RESET_CONFIG, "earliest"); //other options: latest and none
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String,String>(properties);
		consumer.subscribe(Collections.singleton(ProducerDemo.TOPIC)); //can subscribe to more topics
		
		while(true) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
			for(ConsumerRecord<String, String> record : records) {
				logger.info("Key: " + record.key() + ", value: " + record.value()
				+ ", partition: " + record.partition() + ", offset: " + record.offset());
			}
		}
	}
}

package cz.kojotak.udemy.kafka.beginners.t1;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
public class ConsumerDemoAssignSeek {
	
	private static final String GROUP_ID = "my-fifth-application";

	public static void main(String[] args) {
		Logger logger = LoggerFactory.getLogger(ConsumerDemoAssignSeek.class);
		
		Properties properties = new Properties();
		properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, ProducerDemo.BOOTSTRAP_SERVERS);
		properties.setProperty(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(AUTO_OFFSET_RESET_CONFIG, "earliest"); //other options: latest and none
		//group id is missing in this example
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String,String>(properties);
		
		//instead of subscribing we will...
		TopicPartition partitionToReadFrom = new TopicPartition(ProducerDemo.TOPIC, 0);
		consumer.assign(Arrays.asList(partitionToReadFrom));
		long offsetToReadFrom = 15L;
		consumer.seek(partitionToReadFrom, offsetToReadFrom);
		
		int messagesToRead = 5;
		int messagesReadSoFar = 0;
		boolean keepOnReading = true;
		
		while(keepOnReading) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
			for(ConsumerRecord<String, String> record : records) {
				messagesReadSoFar ++;
				logger.info("Key: " + record.key() + ", value: " + record.value()
				+ ", partition: " + record.partition() + ", offset: " + record.offset());
				if(messagesReadSoFar >= messagesToRead) {
					keepOnReading = false;
					break; //for loop
				}
			}
		}
		
		//this will result in:
//		[main] INFO cz.kojotak.udemy.kafka.beginners.t1.ConsumerDemoAssignSeek - Key: Key_2, value: hello world 2, partition: 0, offset: 15
//		[main] INFO cz.kojotak.udemy.kafka.beginners.t1.ConsumerDemoAssignSeek - Key: Key_3, value: hello world 3, partition: 0, offset: 16
//		[main] INFO cz.kojotak.udemy.kafka.beginners.t1.ConsumerDemoAssignSeek - Key: Key_4, value: hello world 4, partition: 0, offset: 17
//		[main] INFO cz.kojotak.udemy.kafka.beginners.t1.ConsumerDemoAssignSeek - Key: Key_5, value: hello world 5, partition: 0, offset: 18
//		[main] INFO cz.kojotak.udemy.kafka.beginners.t1.ConsumerDemoAssignSeek - Key: Key_6, value: hello world 6, partition: 0, offset: 19
	}
}

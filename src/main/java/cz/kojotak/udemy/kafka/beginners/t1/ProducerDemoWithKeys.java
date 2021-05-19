package cz.kojotak.udemy.kafka.beginners.t1;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * to run example:
 *
 * C:\work\kafka_2.12-2.8.0\bin\windows>kafka-console-consumer.bat --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-third-app
 */
public class ProducerDemoWithKeys {

	private static final String bootstrapServers = "127.0.0.1:9092";
	private static final String topic = "first_topic";

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		Logger logger = LoggerFactory.getLogger(ProducerDemoWithKeys.class);
		
		Properties  properties = new Properties();
		properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		
		try(KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties)){
			
			for(int i=0; i<10; i++) {
				
				String value = "hello world " + i;
				String key = "Key_" + i;
			
				ProducerRecord<String,String> record = new ProducerRecord<String,String>(topic, key, value);
				
				
				logger.info("Key: " + key);
				
				producer.send(record, (recordMetadata, e)->{
					if(e==null) {
						logger.info("received new metadata:\n" 
								+ "Topic: " + recordMetadata.topic() + "\n"
								+ "Partition: " + recordMetadata.partition() + "\n"
								+ "Offset: " + recordMetadata.offset() + "\n"
								+ "Timestamp: " + recordMetadata.timestamp() + "\n"
								);
					} else {
						logger.error("Error while producing: ", e);
					}
				}).get(); //don't do this in production
			}
			
			producer.flush(); //force sending message
		}
	}

}

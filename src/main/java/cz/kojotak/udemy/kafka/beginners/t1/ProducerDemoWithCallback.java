package cz.kojotak.udemy.kafka.beginners.t1;

import java.util.Properties;

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
public class ProducerDemoWithCallback {

	private static final String bootstrapServers = "127.0.0.1:9092";
	private static final String topic = "first_topic";

	public static void main(String[] args) {
		Logger logger = LoggerFactory.getLogger(ProducerDemoWithCallback.class);
		
		Properties  properties = new Properties();
		properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		
		try(KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties)){
			for(int i=0; i<10; i++) {
				ProducerRecord<String,String> record = new ProducerRecord<String,String>(topic, "hello world " + i);
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
				}); 
			}
			
			producer.flush(); //force sending message
		}
	}

}

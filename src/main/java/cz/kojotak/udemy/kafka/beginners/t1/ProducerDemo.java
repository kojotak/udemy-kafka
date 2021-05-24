package cz.kojotak.udemy.kafka.beginners.t1;

import java.util.Properties;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import cz.kojotak.udemy.kafka.beginners.Config;

/**
 * to run example:
 *
 * C:\work\kafka_2.12-2.8.0\bin\windows>kafka-console-consumer.bat --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-third-app
 */
public class ProducerDemo {

	public static final String TOPIC = "first_topic";

	public static void main(String[] args) {
		Properties  properties = new Properties();
		properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, Config.BOOTSTRAP_SERVERS);
		properties.setProperty(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
		
		ProducerRecord<String,String> record = new ProducerRecord<String,String>(TOPIC, "hello world");
		producer.send(record); //asynchronous, will not send message
		
		producer.flush(); //force sending message
		producer.close(); //
	}

}

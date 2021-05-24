package cz.kojotak.udemy.kafka.beginners.t2;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

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

public class TwitterProducer implements Runnable {

	Logger logger = LoggerFactory.getLogger(getClass());
	public TwitterProducer() {
	}

	public static void main(String[] args) {
		new TwitterProducer().run();
	}

	@Override
	public void run() {
		BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);

		Client client = createTwitterProducer(msgQueue);
		client.connect();
		
		// on a different thread, or multiple different threads....
		while (!client.isDone()) {
		  try {
			String msg = msgQueue.take();
			logger.info("msg: " + msg);
		  } catch (InterruptedException e) {
			  logger.error("error happened ", e);
			  client.stop();
		  }
		}
	}

	String consumerKey = "YRwNvbb7rfqZvSc9XJjkyNI2r";
	String consumerSecret = "qprfXtMCOMmAXpQ9EPi6c1vcVyd17e7CUew1dFZrZmmfdabYGs";
	String token = "18406353-99CFGDoZeAsZVWyYrNHz40jTnO07n9tku9UJqpIv8";
	String secret = "FDP8XKrtNCaVTmib04TpgKVe3QfVaFJHmgbJa6pNLVJp8";

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
		List<String> terms = Lists.newArrayList("twitter", "api");
		hosebirdEndpoint.followings(followings);
		hosebirdEndpoint.trackTerms(terms);

		// These secrets should be read from a config file
		Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);

		ClientBuilder builder = new ClientBuilder()
				.name("Hosebird-Client-01") 
				.hosts(hosebirdHosts)
				.authentication(hosebirdAuth)
				.endpoint(hosebirdEndpoint)
				.processor(new StringDelimitedProcessor(msgQueue)); 

		return builder.build();
	}
}

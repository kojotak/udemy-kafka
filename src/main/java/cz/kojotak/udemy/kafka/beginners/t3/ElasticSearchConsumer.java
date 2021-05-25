package cz.kojotak.udemy.kafka.beginners.t3;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cz.kojotak.udemy.kafka.beginners.Config.*;

import java.io.IOException;
public class ElasticSearchConsumer {

	public static void main(String[] args) throws IOException {
		Logger logger = LoggerFactory.getLogger(ElasticSearchConsumer.class);
		RestHighLevelClient client = createClient();
		
		String json = "{\"foo\":1}";
		
		IndexRequest indexRequest = new IndexRequest("twitter","tweets")
				.source(json, XContentType.JSON);
		IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
		String id = indexResponse.getId();
		logger.info("id from response: " + id);
		client.close();
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

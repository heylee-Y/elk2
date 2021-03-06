package elasticsearch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class Iatacode {

	public final static String HOST = "10.140.8.212";
	public final static int PORT = 9300;
	public final static String START = "2018-07-30T15:59:59.000Z";
	public final static String END = "2018-08-30T15:59:59.000Z";

	TransportClient client;

	/**
	 * newgen_rs-balances-service
	 * 
	 */
	private void init() throws UnknownHostException {
		// on startup
		// TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT-2"));
		// Settings settings = Settings.builder().put("cluster.name", "name").build();
		client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(
				new TransportAddress(InetAddress.getByName(Iatacode.HOST), Iatacode.PORT));
		System.out.println("Elasticsearch connect info: " + client.nodeName());
	}

	private void close() {
		// on shutdown
		client.close();
	}

	public static void main(String[] args) throws IOException {

		Iatacode es = new Iatacode();
		es.init();

		// CEST +02:00 中欧夏令时
		QueryBuilder qbTime = QueryBuilders.rangeQuery("@timestamp").from(Iatacode.START).to(Iatacode.END);

//		List<String> lines = Files.readAllLines(Paths.get("c://Users/wangjm@iata.org/Desktop/4Countury.txt"));
		List<String> lines = Files.readAllLines(Paths.get("c://Users/wangjm@iata.org/Desktop/null.txt"));

		File file = new File("c:/Users/wangjm@iata.org/Desktop/iatacode.txt");
		FileWriter fw = new FileWriter(file);

		for (String INCid : lines) {
			QueryBuilder qbINCid = QueryBuilders.boolQuery().must(qbTime)
					.must(QueryBuilders.matchPhraseQuery("message", INCid))
					.must(QueryBuilders.matchPhraseQuery("message", "API Response"));

			SearchResponse response4INC = es.client.prepareSearch("newgen_bsplink*").setTypes("log")
					.setQuery(qbINCid).addSort("@timestamp", SortOrder.DESC).get();

			SearchHits hits4INC = response4INC.getHits();

			String regx = "([a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12})";
			String regx2 = "code(.*?)title";
			String regx3 = "phone\":\"(.*?)\"";
			Pattern pattern = Pattern.compile(regx);
			Pattern pattern2 = Pattern.compile(regx2);
			Pattern pattern3 = Pattern.compile(regx3);
			Matcher match = null;
			
			fw.write(INCid);
			System.out.println(INCid);

			for (SearchHit hit : hits4INC) {
				
				System.out.println(hit.getSourceAsString());
				match = pattern.matcher(hit.getSourceAsString());
				if (match.find()) {
					// Query log by ID and is not known reason
//					 System.out.println(match.group(1));
					QueryBuilder qbID = QueryBuilders.boolQuery()
							.must(QueryBuilders.matchPhraseQuery("message", match.group(1)))
							.must(QueryBuilders.matchPhraseQuery("message", "API Response:"))
							.must(qbTime);

					SearchResponse response4RMEid = es.client.prepareSearch("newgen_bsplink*").setTypes("log").setQuery(qbID)
							.get();

					SearchHits hits4RMEid = response4RMEid.getHits();

					for (SearchHit hitRME : hits4RMEid) {

						System.out.println(hitRME.getSourceAsString());
						match = pattern2.matcher(hitRME.getSourceAsString());

						fw.write("  ");
						
						fw.write("BSPLink");

						fw.write("  ");
						match.find();
						System.out.println(match.group(1));
						fw.write(match.group(1));
						fw.write("\r\n");
					
					}
					
					if (hits4RMEid.totalHits<=0) {
						fw.write("NONE");
						fw.write("\r\n");
					}
					System.out.println(
							"---------------------------------Payload--------------------------------------------");
				} else {
					fw.write("NONE");
					fw.write("\r\n");
				}

			}
			
			fw.write(INCid);
			
			for (SearchHit hit : hits4INC) {
				match = pattern.matcher(hit.getSourceAsString());
				if (match.find()) {
					// Query log by ID and is not known reason
//					 System.out.println(match.group(1));
					QueryBuilder qbID = QueryBuilders.boolQuery()
							.must(QueryBuilders.matchPhraseQuery("message", match.group(1)))
							.must(QueryBuilders.matchPhraseQuery("message", "API Response:"))
							.must(qbTime);

					SearchResponse response4RMEid = es.client.prepareSearch("newgen_featurespace-system*").setTypes("log").setQuery(qbID)
							.get();

					SearchHits hits4RMEid = response4RMEid.getHits();

					for (SearchHit hitRME : hits4RMEid) {

						System.out.println(hitRME.getSourceAsString());
						match = pattern2.matcher(hitRME.getSourceAsString());

						fw.write("  ");
						
						fw.write("RME");

						fw.write("  ");
						match.find();
						System.out.println(match.group(1));
						fw.write(match.group(1));
						fw.write("\r\n");
					
					}
					
					if (hits4RMEid.totalHits<=0) {
						fw.write("NONE");
						fw.write("\r\n");
					}
					System.out.println(
							"---------------------------------Payload--------------------------------------------");
				} else {
					fw.write("NONE");
					fw.write("\r\n");
				}

			}

		}

		fw.close();
		es.close();
	}



}

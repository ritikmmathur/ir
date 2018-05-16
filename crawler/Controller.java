package project;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {

	public static void prepareFetchFile() {
		Map<String, StatusDetail> fetchUrlDetail = MyCrawler.getFetchUrlDetail();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File("fetch_foxnews.csv"));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		StringBuilder builder = new StringBuilder();
		for (Entry<String, StatusDetail> entry : fetchUrlDetail.entrySet()) {
			builder.append(entry.getKey() + ",");
			builder.append(entry.getValue().getCode());
			builder.append('\n');
		}
		pw.write(builder.toString());
		pw.close();
	}

	public static void prepareVisitFile() {
		List<PageDetail> visitPageDetail = MyCrawler.getVisitPageDetailList();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File("visit_foxnews.csv"));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		StringBuilder builder = new StringBuilder();
		for (PageDetail pageDetail : visitPageDetail) {
			builder.append(pageDetail.getUrl() + ",");
			builder.append(pageDetail.getSize() + " KB" + ",");
			builder.append(pageDetail.getNumberOfOutGoingUrl() + ",");
			builder.append(pageDetail.getContentType());
			builder.append('\n');
		}
		pw.write(builder.toString());
		pw.close();
	}
	
	public static Set<String> getUniqueURL(List<String> urlList){
		Set<String> uniqueUrlSet = new HashSet<>();
		for(String s : urlList) {
			if(!uniqueUrlSet.contains(s)) {
				uniqueUrlSet.add(s);
			}
		}
		return uniqueUrlSet;
	}
	
	public static Integer getURLWithinWebsite(Set<String> urlList) {
		int count = 0;
		for(String s : urlList) {
			if(s.startsWith("https://www.foxnews.com")) {
				count++;
			}
		}
		return count;
	}
	
	public static void prepareCrawlReport() {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File("crawlreport_foxnews.txt"));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		Map<String, StatusDetail> fetchUrlDetail = MyCrawler.getFetchUrlDetail();
		Integer sucFetchCount = 0;
		Integer abFetchCount = 0;
		Map<Integer, StatusType> statusCountMap = new HashMap<>();
		for (Entry<String, StatusDetail> entry : fetchUrlDetail.entrySet()) {
			Integer val = entry.getValue().getCode();
			if (val / 100 == 2) {
				sucFetchCount++;
			} else {
				abFetchCount++;
			}
			if (statusCountMap.containsKey(val)) {
				StatusType statusType = statusCountMap.get(val);
				Integer count = statusType.getCount();
				statusType.setCount(count + 1);
				statusCountMap.put(val, statusType);
			} else {
				StatusType statusType = new StatusType(entry.getValue().getCode(), entry.getValue().getMessage(), 1);
				statusCountMap.put(val, statusType);
			}
		}

		List<PageDetail> visitPageDetail = MyCrawler.getVisitPageDetailList();
		Integer range1 = 0;
		Integer range2 = 0;
		Integer range3 = 0;
		Integer range4 = 0;
		Integer range5 = 0;
		Map<String, Integer> cTypeMap = new HashMap<>();
		List<String> outGoingURLList = new ArrayList<>();
		for (PageDetail pageDetail : visitPageDetail) {
			outGoingURLList.addAll(pageDetail.getOutGoingURLList());
			if (pageDetail.getSize() < 1) {
				range1++;
			} else if (pageDetail.getSize() < 10) {
				range2++;
			} else if (pageDetail.getSize() < 100) {
				range3++;
			} else if (pageDetail.getSize() < 1024) {
				range4++;
			} else {
				range5++;
			}
			if(cTypeMap.containsKey(pageDetail.getContentType().toLowerCase())){
				Integer count = cTypeMap.get(pageDetail.getContentType().toLowerCase());
				cTypeMap.put(pageDetail.getContentType().toLowerCase(), count+1);
			} else {
				cTypeMap.put(pageDetail.getContentType().toLowerCase(), 1);
			}
		}
		Set<String> uniqueOutGoingURLList = getUniqueURL(outGoingURLList);
		Integer uniqueURLWithinWebsiteCount = getURLWithinWebsite(uniqueOutGoingURLList);
		StringBuilder builder = new StringBuilder();
		builder.append("Name: Ritika Mathur");
		builder.append("\n");
		builder.append("USC ID: 6233030641");
		builder.append("\n");
		builder.append("News site crawled: foxnews.com");
		builder.append("\n");
		builder.append("\n");
		builder.append("Fetch Statistics");
		builder.append("\n");
		builder.append("================");
		builder.append("\n");
		builder.append("# fetches attempted:" + fetchUrlDetail.size());
		builder.append("\n");
		builder.append("# fetches succeeded: " + sucFetchCount);
		builder.append("\n");
		builder.append("# fetched aborted or failed: " + abFetchCount);
		builder.append("\n");
		builder.append("\n");
		builder.append("Outgoing URLs");
		builder.append("\n");
		builder.append("=============");
		builder.append("\n");
		builder.append("Total URLs extracted: " + outGoingURLList.size());
		builder.append("\n");
		builder.append("# unique URLs extracted: " + uniqueOutGoingURLList.size());
		builder.append("\n");
		builder.append("# unique URLs within news site: " + uniqueURLWithinWebsiteCount);
		builder.append("\n");
		builder.append("# unique URLs outside news site: " + (uniqueOutGoingURLList.size() - uniqueURLWithinWebsiteCount));
		builder.append("\n");
		builder.append("\n");
		builder.append("Status Codes");
		builder.append("\n");
		builder.append("============");
		builder.append("\n");
		for(Entry<Integer, StatusType> entry : statusCountMap.entrySet()) {
			builder.append(entry.getValue().getCode()+" " + entry.getValue().getMessage() + " :" + entry.getValue().getCount());
			builder.append("\n");			
		}
		builder.append("\n");
		builder.append("File Sizes");
		builder.append("\n");
		builder.append("==========");
		builder.append("\n");
		builder.append("< 1KB: " + range1);
		builder.append("\n");
		builder.append("1KB ~ <10KB: " + range2);
		builder.append("\n");
		builder.append("10KB ~ <100KB: " + range3);
		builder.append("\n");
		builder.append("100KB ~ <1MB: " + range4);
		builder.append("\n");
		builder.append(">= 1MB: " + range5);
		builder.append("\n");
		builder.append("\n");
		builder.append("Content Types");
		builder.append("\n");
		builder.append("=============");
		builder.append("\n");
		for(Entry<String, Integer> entry : cTypeMap.entrySet()) {
			builder.append(entry.getKey() + " : " + entry.getValue());
			builder.append("\n");			
		}
		pw.write(builder.toString());
		pw.close();

	}

	public static void main(String[] args) throws Exception {
		try {
			String crawlStorageFolder = "/data/crawl";

			int numberOfCrawlers = 7;
			CrawlConfig config = new CrawlConfig();
			config.setMaxDepthOfCrawling(16);
			config.setMaxPagesToFetch(20000);
			config.setCrawlStorageFolder(crawlStorageFolder);
			config.setIncludeBinaryContentInCrawling(true);
			/*
			 * Instantiate the controller for this crawl.
			 */
			PageFetcher pageFetcher = new PageFetcher(config);
			RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
			RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
			CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
			/*
			 * For each crawl, you need to add some seed urls. These are the
			 * first URLs that are fetched and then the crawler starts following
			 * links which are found in these pages
			 */
			controller.addSeed("https://www.foxnews.com/");
			
			/*
			 * Start the crawl. This is a blocking operation, meaning that your
			 * code will reach the line after this only when crawling is
			 * finished.
			 */

			controller.start(MyCrawler.class, numberOfCrawlers);

		} catch (Exception e) {

		}
		prepareFetchFile();
		prepareVisitFile();
		prepareCrawlReport();
	}
}

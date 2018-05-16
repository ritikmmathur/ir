package project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler {
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|mp3|mp3|zip|gz))$");
	private static Map<String, StatusDetail> FETCH_URL_DETAIL = new HashMap<>();
	private static List<PageDetail> VISIT_PAGE_DETAIL = new ArrayList<>();
	private static Integer COUNT = 0;

	public static Map<String, StatusDetail> getFetchUrlDetail() {
		return FETCH_URL_DETAIL;
	}

	public static List<PageDetail> getVisitPageDetailList() {
		return VISIT_PAGE_DETAIL;
	}

	@Override
	protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
		String url = webUrl.getURL();
		synchronized (FETCH_URL_DETAIL) {
			FETCH_URL_DETAIL.put(url, new StatusDetail(statusCode, statusDescription));
		}
	}

	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		return !FILTERS.matcher(href).matches() && href.startsWith("https://www.foxnews.com/") && !href.contains(".css");
	}

	@Override
	public void visit(Page page) {
		synchronized (COUNT) {
			COUNT++;
			System.out.println(COUNT + "::" + page.getWebURL().getURL());
		}
		try {
			PageDetail pageDetail = new PageDetail(page);
			synchronized (VISIT_PAGE_DETAIL) {
				VISIT_PAGE_DETAIL.add(pageDetail);
			}
		} catch (Exception e) {
			System.out.println("Error:  " + page.getWebURL().getURL() + e);
		}
	}

}

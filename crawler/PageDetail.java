package project;

import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class PageDetail {
	private String url;
	private Float size;
	private Integer numberOfOutGoingUrl;
	private Set<String> outGoingURLList;
	private String contentType;

	public PageDetail(Page page) {
		setUrl(page.getWebURL().getURL());
		setSize(new Float(page.getContentData().length / 1024.00));
		
		try {
			if ((page.getParseData() instanceof HtmlParseData)) {
				HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
				setNumberOfOutGoingUrl(htmlParseData.getOutgoingUrls().size());
				setOutGoingURLList(htmlParseData.getOutgoingUrls());
			} else {
				setNumberOfOutGoingUrl(0);
				setOutGoingURLList(new HashSet<>());
			}
		} catch (Exception e) {
			setNumberOfOutGoingUrl(0);
			setOutGoingURLList(new HashSet<>());
		}

		if (page.getContentType().contains(";")) {
			setContentType(page.getContentType().split(";")[0]);
		} else {
			setContentType(page.getContentType());
		}
	}

	public String getUrl() {
		return this.url;
	}

	private void setUrl(String url) {
		this.url = url;
	}

	public Float getSize() {
		return this.size;
	}

	private void setSize(Float size) {
		this.size = size;
	}

	public Integer getNumberOfOutGoingUrl() {
		return this.numberOfOutGoingUrl;
	}

	private void setNumberOfOutGoingUrl(Integer numberOfOutGoingUrl) {
		this.numberOfOutGoingUrl = numberOfOutGoingUrl;
	}

	public String getContentType() {
		return this.contentType;
	}

	private void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Set<String> getOutGoingURLList() {
		return this.outGoingURLList;
	}

	private void setOutGoingURLList(Set<WebURL> outGoingWebURLList) {
		Set<String> outGoingURLList = new HashSet<>();
		for (WebURL webUrl : outGoingWebURLList) {
			outGoingURLList.add(webUrl.getURL());
		}
		this.outGoingURLList = outGoingURLList;
	}
}

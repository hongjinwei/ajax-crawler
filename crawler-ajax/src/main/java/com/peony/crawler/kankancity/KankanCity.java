package com.peony.crawler.kankancity;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.peony.util.StringUtils;
import com.peony.util.http.HttpQuery;

public class KankanCity {

	private static final Logger LOGGER = LoggerFactory.getLogger(KankanCity.class);

	private static String[] domains = { "http://cq.kankancity.com/", "http://zj.kankancity.com/", "http://js.kankancity.com/",
			"http://sc.kankancity.com/", "http://hb.kankancity.com/", "http://hn.kankancity.com/" };

	private static String[] regions = { "重庆", "浙江", "江苏", "四川", "湖北", "湖南" };

	private static final List<KankanAjaxPage> ajaxPgaes = new ArrayList<KankanAjaxPage>();

	private static int p = 0;
	private static int threadNumber = 1;
	private static int step = 0;
	private static int thread = 0;
	
	static{
		init();
	}
	
	private static String formatAjaxPgaeByTag(String tag, String domain) {
		return domain + "tagslist/" + tag + "/0.js";
	}

	private static List<KankanAjaxPage> getCityAjaxPages(String domain, String region) {
		List<KankanAjaxPage> pages = new ArrayList<KankanAjaxPage>();
		try {
			String html = HttpQuery.getInstance().get(domain).asString();
			Document doc = Jsoup.parse(html);
			Elements elements = doc.getElementsByAttributeValue("class", "sitenav_channel ff_yh");
			if (elements.size() > 0) {
				Elements eles = elements.get(0).getElementsByAttributeValue("target", "_self").select("a");
				for (Element e : eles) {
					try {
						String href = e.attr("href");
						String tag = StringUtils.match(href, "tags/(\\d+)/").get(0);
						String cityname = e.text();
						LOGGER.info(region + " " + cityname + " " + tag);
						String url = formatAjaxPgaeByTag(tag, domain);
						KankanAjaxPage newPgae = new KankanAjaxPage();
						newPgae.setCity(cityname);
						newPgae.setRegion(region);
						newPgae.setUrl(url);
						newPgae.setDomain(domain);
						pages.add(newPgae);
					} catch (Exception ex) {
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return pages;
	}

	private static void init() {
		for (int i = 0; i < domains.length; i++) {
			String region = regions[i];
			String domain = domains[i];
			List<KankanAjaxPage> pages = getCityAjaxPages(domain, region);
			ajaxPgaes.addAll(pages);
		}
		step = ajaxPgaes.size();
	}

	public static void registerThreadNumber(int n) {
		threadNumber = (n == 0) ? 1 : n;
		if (ajaxPgaes.size() <= 0) {
			step = 0;
		} else {
			step = ajaxPgaes.size() / threadNumber + 1;
		}
	}

	synchronized public static List<KankanAjaxPage> getAjaxPages() {
		List<KankanAjaxPage> ans = new ArrayList<KankanAjaxPage>();
		if (p >= ajaxPgaes.size() && thread >= threadNumber) {
			p = 0;
			thread = 0;
		}
		for (int i = 0; i < step && p < ajaxPgaes.size(); i++) {
			ans.add(ajaxPgaes.get(p++));
		}
		thread++;
		return ans;
	}

}

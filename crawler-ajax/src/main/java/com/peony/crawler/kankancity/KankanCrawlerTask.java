package com.peony.crawler.kankancity;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.peony.crawler.CommonUtils;
import com.peony.crawler.model.CrawlerTask;
import com.peony.crawler.model.ParseResult;
import com.peony.crawler.model.WebPage;
import com.peony.util.http.HttpQuery;

public class KankanCrawlerTask implements CrawlerTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(KankanCrawlerTask.class);

	private ParseResult parsePage(JSONObject item, String domain) {
		try {
			String title = item.getString("title");
			String time = item.getString("updated_at");
			Timestamp publish_time;
			try {
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = df.parse(time);
				publish_time = new Timestamp(date.getTime());
			} catch (Exception e) {
				LOGGER.error("解析时间失败！" + time + e.getMessage(), e);
				publish_time = new Timestamp(System.currentTimeMillis());
			}
			String id = item.getString("id");
			String type = item.getString("type");
			if (type.equals("article")) {
				String contentUrl = domain + "news/" + id.substring(0, 3) + "/" + id + ".shtml";
				String html = HttpQuery.getInstance().get(contentUrl).asString();
				String content = Jsoup.parse(html).select("article").get(0).text();
				String summary = CommonUtils.getSummary(content);
				WebPage page = new WebPage();

				page.setDownloadDate(new Timestamp(System.currentTimeMillis()));
				page.setSummary(summary);
				page.setPublishDate(publish_time);
				page.setTitle(title);
				page.setUrl(contentUrl);
				page.setWebSite("看看城事");
				page.setType(1);
				page.setIndexedStatus(3);

				ParseResult pr = new ParseResult();
				pr.setContent(content);
				pr.setPage(page);
				return pr;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	private void crawl(KankanAjaxPage ajaxPage) {
		String url = ajaxPage.getUrl();
		String domain = ajaxPage.getDomain();

		List<ParseResult> pages = new ArrayList<ParseResult>();
		try {
			String data = HttpQuery.getInstance().get(url).asString();
			JSONObject obj = JSONObject.parseObject(data);
			JSONArray contents = obj.getJSONArray("data");
			for (int i = 0; i < contents.size(); i++) {
				try {
					JSONObject item = contents.getJSONObject(i);
					ParseResult result = parsePage(item, domain);
					if (result != null) {
						pages.add(result);
					}
				} catch (Exception e) {
				}
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			String baseInfo = ajaxPage.getRegion() + ajaxPage.getCity();
			CommonUtils.trySaveParseResults(pages, baseInfo);
		}
	}

	@Override
	public void run() {
		List<KankanAjaxPage> pages = KankanCity.getAjaxPages();
		LOGGER.info(pages.toString());
		for (KankanAjaxPage page : pages) {
			LOGGER.info("开始任务" + page);
			crawl(page);
			LOGGER.info("任务结束" + page);
		}
	}

}

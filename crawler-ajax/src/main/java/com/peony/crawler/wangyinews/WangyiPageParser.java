package com.peony.crawler.wangyinews;

import java.sql.Timestamp;
import java.util.Date;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.peony.util.http.BaseHttpException;
import com.peony.util.http.HttpQuery;

public class WangyiPageParser {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(WangyiCrawlerTask.class);

	private final static String rawContentUrl = "http://j.news.163.com/hy/doc.s?info=2&type=10&hash=&docid=%s";
	private final static String pageUrlPrefix = "http://j.news.163.com";
	static HttpQuery browser = HttpQuery.getInstance();

	public static String getHtmlFromUrl(String url) throws BaseHttpException {
		return browser.get(url).asString();
	}

	/**
	 * 获取内容的docID参数,并与rawContentUrl format之后返回,这个是页面内容的ajax请求地址,如果出错，返回""
	 * 
	 * @param obj
	 */
	public static String getContentUrl(JSONObject obj) {
		try {
			String docID = obj.getString("docID");
			String contentUrl = String.format(rawContentUrl, docID);
			return contentUrl;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return "";
	}

	/**
	 * 根据contentUrl获取content内容，这是一个JSON String，处理成JSON
	 * 
	 * @param contentUrl
	 * @return
	 */
	public static JSONObject getContentJSON(String contentUrl) {
		try {
			String html = getHtmlFromUrl(contentUrl);
			JSONObject obj = JSONObject.parseObject(html);
			return obj;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}

	/**
	 * 返回doc的内容，解析失败返回""
	 * 
	 * @param obj
	 * @param docUrl
	 * @return
	 */
	public static String getContent(JSONObject obj) throws Exception{
		String content =  obj.getString("content");
		content = Jsoup.parse(content).body().text();
		return content;
	}

	public static Timestamp getPublishDate(JSONObject obj) throws Exception {
			long publishDate = obj.getLongValue("publish_time");
			Date d = new Date(publishDate);
			Timestamp date = new Timestamp(d.getTime());
			return date;
	}

	/**
	 * 获取这个页面的URL，这个和contentUrl不同，他是直接访问的，由AJAX渲染后的页面,真实访问的页面地址
	 * 
	 * @param obj
	 * @return
	 */
	public static String getPageUrl(JSONObject obj) throws Exception {
		return pageUrlPrefix + obj.getString("url_163");
	}

	/**
	 * 截取文章前200个字作为summary
	 * 
	 * @param content
	 * @return
	 */
	public static String getSummary(String content) throws Exception {
		int len = content.length();
		int end = (len > 200) ? 200 : len;
		return content.substring(0, end);
	}
}

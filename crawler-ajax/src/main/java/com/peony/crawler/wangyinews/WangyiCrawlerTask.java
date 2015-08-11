package com.peony.crawler.wangyinews;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.peony.crawler.CommonUtils;
import com.peony.crawler.SystemProps;
import com.peony.crawler.WebPageRepository;
import com.peony.crawler.model.CrawlerTask;
import com.peony.crawler.model.ParseResult;
import com.peony.crawler.model.WebPage;
import com.peony.crawler.model.WebPageManager;
import com.peony.util.TimerUtils;
import com.peony.util.cache.CacheClient;
import com.peony.util.http.BaseHttpException;
import com.peony.util.http.HttpQuery;

public class WangyiCrawlerTask implements CrawlerTask {

	/**
	 * 新闻显示条数的限制,默认100
	 */
	private int limit = 100;

	/**
	 * 文档服务器地址
	 */
	private String baseUrl = "http://119.254.110.32:8080/HBaseDfs/dfs";

	/**
	 * 初始网易新闻信息ajax请求网址,得到的是JSON字符串
	 */
	private final String rawUrl = "http://j.news.163.com/hy/newshot.s?newchannel=news&channel=10&offset=0&limit=%d&city=%s";

	static HttpQuery browser = HttpQuery.getInstance();

	/**
	 * 日志管理对象
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(WangyiCrawlerTask.class);

	/**
	 * 构造方法
	 * 
	 * @param limit
	 */
	public WangyiCrawlerTask(int limit) {
		this.limit = limit;
	}

	/**
	 * 根据城市名，获取得到新闻主页面的url
	 * 
	 * @return
	 */
	private String createUrl(String city) {
		return String.format(rawUrl, limit, city);
	}

	/**
	 * 解析页面中的一项JSON数据，并包装到WebPage当中，并返回WebPage
	 * 
	 * @param obj
	 * @param session
	 */
	private ParseResult parsePage(JSONObject obj) {

		WebPage page = new WebPage();
		String content = "";
		String pageUrl = "";
		try {
			// 每个页面的内容依然是ajax，这里得到的是JSON数据的URL
			String contentUrl = WangyiPageParser.getContentUrl(obj);
			pageUrl = WangyiPageParser.getPageUrl(obj);
			String title = obj.getString("title");
			String webSite = "网易新闻";
			JSONObject contentJSON = WangyiPageParser.getContentJSON(contentUrl);
			content = WangyiPageParser.getContent(contentJSON);
			Timestamp publishDate = WangyiPageParser.getPublishDate(contentJSON);
			// 不用自带的summary，截取content前200个字符获取summary
			String summary = WangyiPageParser.getSummary(content);
			// 如果content或者title为空，则不保存页面
			if (content.equals("") || title.equals("") || title == null) {
				return null;
			}

			page.setTitle(title);
			page.setUrl(pageUrl);
			page.setWebSite(webSite);
			page.setDownloadDate(new Timestamp(System.currentTimeMillis()));
			page.setTitle(title);
			page.setSummary(summary);
			page.setPublishDate(publishDate);
			page.setType(1);
			page.setIndexedStatus(3);
		} catch (Exception e) {
			LOGGER.error("网易新闻页面信息提取失败！");
			return null;
		}

		if (content.equals("") || content == null)
			return null;

		ParseResult res = new ParseResult();
		res.setPage(page);
		res.setContent(content);
		return res;
	}

	/**
	 * 根据ajax请求链接爬取内容
	 * 解析流程是：从rawUrl拼接上城市和limit组成一个原始页面的ajax请求url，获取内容解析成JSONArray，
	 * array中的一项包含有title,和url_163属性("/docs/10/2015052804/AQM4VA369001VA37.html")
	 * 和rawPageUrl："http://j.news.163.com"拼接后得到访问的页面
	 * 我们在url缓存，存入mysql以及HBase的url都是这个pageUrl
	 * docID("AQM4VA369001VA37")，利用docID填充入rawContentUrl
	 * ："http://j.news.163.com/hy/doc.s?info=2&type=10&hash=&docid=%s"
	 * 组成一条新闻的ajax请求，请求返回的是一个json字符串
	 * 解析之后，得到contentJSON，根据这个数据得到里面的content，publish_date字段
	 * 
	 * @param url
	 *            从createUrl中获取到的拼接好的字符串,这是一条某城市新闻的ajax请求页面
	 * 
	 * @author BAO
	 */
	private void craw(String url, List<ParseResult> list) {
		String html = null;
		try {
			html = WangyiPageParser.getHtmlFromUrl(url);
		} catch (BaseHttpException e1) {
			LOGGER.error(e1.getMessage() + "获取AJAX页面失败", e1);
			return;
		}

		// 解析ajax请求主页的json，如果失败就直接返回
		JSONArray array = null;
		try {
			array = JSONArray.parseArray(html);
		} catch (Exception e) {
			LOGGER.error(e.getMessage() + "页面JSON解析失败", e);
			return;
		}

		for (int i = 0; i < array.size(); i++) {
			JSONObject obj = null;
			try {
				obj = array.getJSONObject(i);
			} catch (Exception e) {
				LOGGER.error(e.getMessage() + "JSON内容获取失败", e);
			}
			// 如果obj为null，就跳过
			if (obj == null)
				continue;

			ParseResult result = parsePage(obj);

			if (result != null) {
				list.add(result);
			}

			if (!SystemProps.isTest()) {
				TimerUtils.delayForSeconds(2);
			}
		}
	}

	private void trySaveParseResults(List<ParseResult> results) {
		CacheClient session = CommonUtils.getCacheClient();
		int count = 0;
		try {
			for (int i = 0; i < results.size(); i++) {
				ParseResult result = results.get(i);
				try {
					if (!CommonUtils.checkUrl(session, result.getPage().getUrl())) {
						WebPage page = result.getPage();
						String content = result.getContent();
						// 保存文章到HBase文档服务器
						try {
							if (SystemProps.storeable()) {
								CommonUtils.storage(true, page.getId(), content, true);
							}
						} catch (Exception e) {
							LOGGER.error("保存至文档服务器失败！ ： " + e.getMessage());
							continue;
						}
						// 保存页面到sql数据库
						// WebPageRepository.save(page);
						try {
							WebPageManager.getInstance().savePage(page);
						} catch (SQLException e) {
							LOGGER.error(e.getMessage() + "保存至sql数据库失败", e);
							continue;
						} catch (Exception e) {
							LOGGER.error(e.getMessage() + "保存至sql数据库失败", e);
							continue;
						}

						// 保存contentUrl，即AJAX的JSON数据的URL
						CommonUtils.storeUrl(session, page.getUrl());
						count++;
					}
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		} finally {
			LOGGER.info("存放爬取结果" + count + "条");

			try {
				CommonUtils.recycleCacheClient(session);
			} catch (Exception e) {
				LOGGER.error(e.getMessage() + "释放cacheclient失败");
			}
		}
	}

	public void run() {
		try {
			List<String> cityNames = City.getCityNames();
			LOGGER.info("开始爬取网易新闻，城市列表：" + cityNames);
			for (String cityname : cityNames) {
				List<ParseResult> results = new ArrayList<ParseResult>();
				try {
					LOGGER.info("启动网易新闻AJAX爬取  城市：" + cityname);
					craw(createUrl(cityname), results);
					LOGGER.info("网易新闻AJAX爬取  城市：" + cityname + "完毕");
				} finally {
					try {
						LOGGER.info("开始存放爬取结果 城市：" + cityname);
						trySaveParseResults(results);
						LOGGER.info("存放网易新闻结束 城市：" + cityname);
					} catch (Exception e) {
						LOGGER.error(e.getMessage() + "存放网易新闻：" + cityname + " 时出现错误！", e);
					} catch (Throwable e) {
						LOGGER.error(e.getMessage() + "存放网易新闻：" + cityname + " 时出现严重错误！", e);
					}
				}
				TimerUtils.delayForSeconds(30);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage() + "网易新闻AJAX爬虫出现未知异常！", e);
		}
	}
}

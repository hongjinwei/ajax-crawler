package com.peony.crawler.toutiao;

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
import com.peony.util.http.HttpQuery;

public class ToutiaoCrawlerTask implements CrawlerTask {

	private String jsonPage = "http://toutiao.com/api/article/recent/?source=2&count=%d&category=%s&utm_source=toutiao";
	private String baseUrl = "http://119.254.110.32:8080/HBaseDfs/dfs";
	private static final Logger LOGGER = LoggerFactory.getLogger(ToutiaoCrawlerTask.class);
	/**
	 * 每页新闻的条数,默认20条
	 */
	private int count = 20;

	private static HttpQuery browser = HttpQuery.getInstance();

	public ToutiaoCrawlerTask(int c) {
		this.count = c;
	}

	private String createUrl(String category) {
		String url = String.format(jsonPage, this.count, category);
		return url;
	}

	private String handleSahreUrl(String url) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < url.length()) {
			if (url.charAt(i) != '?') {
				sb.append(url.charAt(i));
			} else {
				break;
			}
			i++;
		}
		return sb.toString();
	}

	public void craw(String url, List<ParseResult> results) {
		String data = null;
		try {
			data = browser.get(url).asString();
		} catch (Exception e) {
			LOGGER.error(e.getMessage() + "url请求失败 ：" + url, e);
			return;
		}
		JSONArray array = null;
		try {
			JSONObject obj = JSONObject.parseObject(data);
			array = obj.getJSONArray("data");
		} catch (Exception e) {
			LOGGER.error(e.getMessage() + "获取json数据失败！", e);
			return;
		}

		String title = null;
		Timestamp publish_date = null;
		String pageUrl = null;
		String webSite = "今日头条";
		for (int i = 0; i < array.size(); i++) {
			try {
				JSONObject item = array.getJSONObject(i);
				title = item.getString("title");
				publish_date = new Timestamp(item.getLong("publish_time") * 1000);
				String shareUrl = item.getString("share_url");
				pageUrl = handleSahreUrl(shareUrl);
				System.out.println(pageUrl);
			} catch (Exception e) {
				LOGGER.error(e.getMessage() + "页面解析失败！", e);
				continue;
			}

			// 如果没有信息就跳过此条
			if (pageUrl == null || title == null || publish_date == null) {
				continue;
			}

			// 获取内容页面
			String content = null;
			try {
				content = browser.get(pageUrl).asString();
			} catch (Exception e) {
				LOGGER.error(e.getMessage() + "获取content失败", e);
				continue;
			}

			if (content == null)
				continue;

			WebPage page = new WebPage();
			page.setTitle(title);
			page.setDownloadDate(new Timestamp(System.currentTimeMillis()));
			page.setPublishDate(publish_date);
			page.setUrl(pageUrl);
			page.setType(1);
			page.setWebSite(webSite);
			page.setIndexedStatus(0);

			ParseResult res = new ParseResult();
			res.setPage(page);
			res.setContent(content);
			results.add(res);

			TimerUtils.delayForSeconds(5);
		}
	}

	// private void trySaveParseResults(List<ParseResult> list) {
	// LOGGER.info("开始保存");
	// CacheClient session = CommonUtils.getCacheClient();
	// LOGGER.info("获取mina成功");
	// int count = 0;
	// try {
	// for (int i = 0; i < list.size(); i++) {
	// ParseResult result = list.get(i);
	// try {
	// if (!CommonUtils.checkUrl(session, result.getPage().getUrl())) {
	// WebPage page = result.getPage();
	// String content = result.getContent();
	//
	// // 保存文章到HBase文档服务器
	// try {
	// if (SystemProps.storeable()) {
	// CommonUtils.storage(true, page.getId(), content, false);
	// }
	// } catch (Exception e) {
	// LOGGER.error(e.getMessage() + "保存至文档服务器失败！");
	// continue;
	// }
	//
	// // 保存页面到sql数据库
	// // WebPageRepository.save(page);
	//
	// try {
	// WebPageManager.getInstance().savePage(page);
	// } catch (SQLException e) {
	// LOGGER.error(e.getMessage(), e);
	// continue;
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// LOGGER.error(e.getMessage(), e);
	// continue;
	// }
	//
	// // 保存contentUrl，即AJAX的JSON数据的URL
	// CommonUtils.storeUrl(session, page.getUrl());
	// count++;
	// }
	// } catch (Exception e) {
	// LOGGER.error(e.getMessage(), e);
	// }
	// }
	// } finally {
	// LOGGER.info("存放爬取结果" + count + "条");
	// CommonUtils.recycleCacheClient(session);
	// }
	// }

	@Override
	public void run() {
		List<String> categories = Category.getCategories();
		for (String category : categories) {
			List<ParseResult> list = new ArrayList<ParseResult>();
			try {
				LOGGER.info("开始爬取板块:" + category);
				craw(createUrl(category), list);
				LOGGER.info("爬取板块:" + category + "结束");
			} catch (Exception e) {
				LOGGER.error(e.getMessage() + "爬取" + category + "板块时发生异常：", e);
			} finally {
				try {
					LOGGER.info("开始保存爬取结果 category:" + category);
					CommonUtils.trySaveParseResults(list, "今日头条 ");
					LOGGER.info("保存板块" + category + "结束 ");
				} catch (Exception e) {
					LOGGER.error(e.getMessage() + "存放今日头条" + category + "板块时出现错误", e);
				} catch (Throwable e) {
					LOGGER.error(e.getMessage() + "存放今日头条" + category + "板块时出现严重错误！", e);
				}
			}
			TimerUtils.delayForSeconds(30);
		}
	}
}

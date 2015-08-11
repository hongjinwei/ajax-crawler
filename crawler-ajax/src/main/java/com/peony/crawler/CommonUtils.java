package com.peony.crawler;

import java.sql.SQLException;
import java.util.List;

import md.base.storage.WebPageStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.peony.crawler.model.ParseResult;
import com.peony.crawler.model.WebPage;
import com.peony.crawler.model.WebPageManager;
import com.peony.util.StringUtils;
import com.peony.util.cache.CacheClient;
import com.peony.util.cache.CacheClientPool;
import com.peony.util.cache.CacheClientPoolFactory;

public class CommonUtils {

	/**
	 * 日志管理对象
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

	private static CacheClientPool pool = CacheClientPoolFactory.getObject();

	private static final String docUrl = "http://rtr-fjlbdp9p.pek2.qingcloud.com:8080/HBaseDfs/dfs";

	/**
	 * @return 获取MinaSession对象
	 */
	public static CacheClient getCacheClient() {
		try {
			if (!SystemProps.cacheable()) {
				return null;
			}
			return pool.getResource();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	public static void recycleCacheClient(CacheClient mc) {
		if (mc != null) {
			pool.returnResource(mc);
		}
	}

	/**
	 * 在缓存中检测URL，如果session为空，或者检测失败，都返回false，不会写入URL到缓存中
	 * 
	 * @param mc
	 *            MemMinaClient对象
	 * @param url
	 *            待检测的URL
	 * @return true URL缓存命中，false 没命中
	 */
	public static boolean checkUrl(CacheClient mc, String url) {
		try {
			long s = System.currentTimeMillis();
			if (mc != null && mc.checkUrl(url, false)) {
				return true;
			}
			long e = System.currentTimeMillis();
			if ((e - s) > 100) {
				LOGGER.info("检测URL耗时：" + (e - s));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * 将一个URL保存到缓存中
	 * 
	 * @param mc
	 *            MemMinaClient对象
	 * @param url
	 *            待缓存的url
	 */
	public static void storeUrl(CacheClient mc, String url) {
		// 如果设置了storeurl参数为false，就不保存url
		if (!SystemProps.storeurl())
			return;

		if (mc != null) {
			try {
				long s = System.currentTimeMillis();
				mc.storeUrl(url);
				long e = System.currentTimeMillis();
				if ((e - s) > 100) {
					LOGGER.info("缓存URL耗时：" + (e - s));
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * 保存到文档服务器,如果baseUrl为空，就不保存
	 * 
	 * @param baseUrl
	 *            文档服务器地址
	 * @param comp
	 *            boolean值，是否压缩，一般为true
	 * @param id
	 *            文章id
	 * @param content
	 *            文章完整内容
	 * @param isPureText
	 *            是否是纯文本
	 * @throws Exception
	 */
	public static void storage(String baseUrl, boolean comp, String id, String content, boolean isPureText) throws Exception {
		if (StringUtils.isEmpty(baseUrl)) {
			return;
		}
		WebPageStorage storage = new WebPageStorage(comp);
		storage.useHttpFileSystem(baseUrl);
		storage.put(id, content, true, isPureText);
	}

	/**
	 * 使用默认的文档服务器地址
	 * 
	 * @param comp
	 * @param id
	 * @param content
	 * @param isPureText
	 * @throws Exception
	 */
	public static void storage(boolean comp, String id, String content, boolean isPureText) throws Exception {
		WebPageStorage storage = new WebPageStorage(comp);
		storage.useHttpFileSystem(docUrl);
		storage.put(id, content, true, isPureText);
	}

	/**
	 * 截取文章前200个字作为summary
	 * 
	 * @param content
	 * @return
	 */
	public static String getSummary(String content) throws Exception {
		if (content == null) {
			return null;
		}
		int len = content.length();
		int end = (len > 200) ? 200 : len;
		return content.substring(0, end);
	}

	public static void trySaveParseResults(List<ParseResult> results, String baseInfo) {
		CacheClient session = getCacheClient();
		int count = 0;
		try {
			for (int i = 0; i < results.size(); i++) {
				ParseResult result = results.get(i);
				try {
					if (!checkUrl(session, result.getPage().getUrl())) {
						WebPage page = result.getPage();
						String content = result.getContent();
						// 保存文章到HBase文档服务器
						try {
							if (SystemProps.storeable()) {
								storage(true, page.getId(), content, true);
							}
						} catch (Exception e) {
							LOGGER.error(baseInfo + "保存至文档服务器失败！ ： " + e.getMessage());
							continue;
						}
						// 保存页面到sql数据库
						// WebPageRepository.save(page);
						try {
							WebPageManager.getInstance().savePage(page);
						} catch (SQLException e) {
							LOGGER.error(e.getMessage() + baseInfo + "保存至sql数据库失败", e);
							continue;
						} catch (Exception e) {
							LOGGER.error(e.getMessage() + baseInfo + "保存至sql数据库失败", e);
							continue;
						}

						// 保存contentUrl，即AJAX的JSON数据的URL
						storeUrl(session, page.getUrl());
						count++;
					}
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		} finally {
			LOGGER.info(baseInfo + "存放爬取结果" + count + "条");
			try {
				CommonUtils.recycleCacheClient(session);
			} catch (Exception e) {
				LOGGER.error(e.getMessage() + baseInfo + "释放cacheclient失败");
			}
		}
	}
}

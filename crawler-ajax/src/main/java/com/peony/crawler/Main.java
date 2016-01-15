package com.peony.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.peony.crawler.kankancity.KankanCrawlerTaskService;
import com.peony.crawler.model.CrawlerTaskService;
import com.peony.crawler.model.ServerInitException;
import com.peony.crawler.toutiao.ToutiaoCrawlerTaskService;
import com.peony.crawler.wangyinews.WangyiCrawlerTaskService;
import com.peony.util.base.New;

public class Main {

	private static List<CrawlerTaskService> taskService = new ArrayList<CrawlerTaskService>();
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	private static ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(5);

	private static int threadPoolSize = 5;

	public static int getThreadPoolSize() {
		return threadPoolSize;
	}

	private static boolean initSqlConnection() {
		boolean flag = true;
		try {
			New.getInstance(ConnectionManager.class).init();
		} catch (ServerInitException e) {
			LOGGER.error(e.getMessage(), e);
			flag = false;
		}
		return flag;
	}

	static {
		taskService.add(new WangyiCrawlerTaskService());
		taskService.add(new ToutiaoCrawlerTaskService());
		taskService.add(new KankanCrawlerTaskService());
	}

	public static void setTest() {
		System.setProperty("cacheable", "false");
		System.setProperty("storeable", "false");
		System.setProperty("storeurl", "false");
		System.setProperty("test", "true");
	}

	public static void main(String[] args) {
		// setTest();
		if (!initSqlConnection()) {
			return;
		}
		for (CrawlerTaskService service : taskService) {
			service.start(threadPool);
		}
	}
}

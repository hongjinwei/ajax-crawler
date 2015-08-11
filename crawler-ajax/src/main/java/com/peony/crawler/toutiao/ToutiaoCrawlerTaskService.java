package com.peony.crawler.toutiao;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.peony.crawler.model.CrawlerTaskService;

public class ToutiaoCrawlerTaskService implements CrawlerTaskService {

	/**
	 * 一页条数的限制
	 */
	private static final int count = 10;
	private static final int threadNumber = 1;
	private static final Logger LOGGER = LoggerFactory.getLogger(ToutiaoCrawlerTaskService.class);

	private static final int sleepHour = 1;
	
	private static int getThreadNumber() {
		return threadNumber;
	}
	
	public void start(ScheduledExecutorService threadPool) {
		if (!Category.init(threadNumber)) {
			LOGGER.error("Category信息初始化失败！！！");
			return ;
		}
		for (int i = 0; i < getThreadNumber(); i++) {
			threadPool.scheduleWithFixedDelay(new ToutiaoCrawlerTask(count), 0, sleepHour, TimeUnit.HOURS);
		}
	}
}

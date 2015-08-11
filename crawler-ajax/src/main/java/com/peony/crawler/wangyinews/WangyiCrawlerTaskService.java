package com.peony.crawler.wangyinews;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.peony.crawler.model.CrawlerTaskService;
import com.peony.crawler.wangyinews.City;
import com.peony.crawler.wangyinews.WangyiCrawlerTask;

public class WangyiCrawlerTaskService implements CrawlerTaskService{

	// 每个页面新闻最多的条数限制
	private static int limit = 10;
	// 运行周期，一般一小时运行一次
	private static int sleepHour = 1;
	// 线程数目,目前只支持单线程
	private static int threadNumber = 3;

	private static final Logger LOGGER = LoggerFactory.getLogger(WangyiCrawlerTaskService.class);

	// 总共开启所有城市数目之和的线程数,防止被封ip，一般只需要1个线程就可以
	private static int getThreadNumber() {
		return threadNumber;
	}

//	private static ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(getThreadPoolSize());

	/**
	 * 每天运行一次
	 */
	public void start(ScheduledExecutorService threadPool) {
		if (!City.init(threadNumber)) {
			LOGGER.error("City信息初始化失败！！！");
			return ;
		}
		for (int i = 0; i < getThreadNumber(); i++) {
			threadPool.scheduleWithFixedDelay(new WangyiCrawlerTask(limit), 0, sleepHour, TimeUnit.HOURS);
		}
	}

}

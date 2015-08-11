package com.peony.crawler.kankancity;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.peony.crawler.model.CrawlerTaskService;

public class KankanCrawlerTaskService implements CrawlerTaskService {

	private static final int threadNumber = 3;

	private static final int sleepHour = 1;

	private static int getThreadNumber() {
		return threadNumber;
	}

	@Override
	public void start(ScheduledExecutorService threadPool) {
		KankanCity.registerThreadNumber(threadNumber);
		for (int i = 0; i < getThreadNumber(); i++) {
			threadPool.scheduleWithFixedDelay(new KankanCrawlerTask(), 0, sleepHour, TimeUnit.HOURS);
		}
	}

}

package com.peony.crawler.model;

import java.util.concurrent.ScheduledExecutorService;

public interface CrawlerTaskService {
	//public void start();
	
	public void start(ScheduledExecutorService pool);
}
